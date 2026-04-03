package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.BanGroupMemberUseCase
import com.cometchat.uikit.core.domain.usecase.ChangeMemberScopeUseCase
import com.cometchat.uikit.core.domain.usecase.FetchGroupMembersUseCase
import com.cometchat.uikit.core.domain.usecase.KickGroupMemberUseCase
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import com.cometchat.uikit.core.state.DialogState
import com.cometchat.uikit.core.state.GroupMembersEvent
import com.cometchat.uikit.core.state.GroupMembersUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for CometChatGroupMembers component.
 * Manages group member list state, member actions (kick/ban/scope change),
 * and real-time updates via CometChat listeners.
 * 
 * Implements [ListOperations] interface for standardized list manipulation.
 * All list operation methods are open for client override.
 * 
 * @param fetchGroupMembersUseCase Use case for fetching group members
 * @param kickGroupMemberUseCase Use case for kicking members
 * @param banGroupMemberUseCase Use case for banning members
 * @param changeMemberScopeUseCase Use case for changing member scope
 * @param enableListeners Whether to enable CometChat listeners (set to false for testing)
 */
open class CometChatGroupMembersViewModel(
    private val fetchGroupMembersUseCase: FetchGroupMembersUseCase,
    private val kickGroupMemberUseCase: KickGroupMemberUseCase,
    private val banGroupMemberUseCase: BanGroupMemberUseCase,
    private val changeMemberScopeUseCase: ChangeMemberScopeUseCase,
    private val enableListeners: Boolean = true
) : ViewModel(), ListOperations<GroupMember> {
    
    // ==================== State Flows ====================
    
    // UI State
    private val _uiState = MutableStateFlow<GroupMembersUIState>(GroupMembersUIState.Loading)
    val uiState: StateFlow<GroupMembersUIState> = _uiState.asStateFlow()
    
    // Members list
    private val _members = MutableStateFlow<List<GroupMember>>(emptyList())
    val members: StateFlow<List<GroupMember>> = _members.asStateFlow()
    
    // Has more data flag
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Selection state
    private val _selectedMembers = MutableStateFlow<Map<String, GroupMember>>(emptyMap())
    val selectedMembers: StateFlow<Map<String, GroupMember>> = _selectedMembers.asStateFlow()
    
    // Dialog state
    private val _dialogState = MutableStateFlow<DialogState>(DialogState.Hidden)
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()
    
    // Events (one-time emissions)
    private val _events = MutableSharedFlow<GroupMembersEvent>()
    val events: SharedFlow<GroupMembersEvent> = _events.asSharedFlow()
    
    // ==================== Configuration ====================
    
    private var currentGroup: Group? = null
    private var groupMembersRequestBuilder: GroupMembersRequest.GroupMembersRequestBuilder? = null
    private var searchGroupMembersRequestBuilder: GroupMembersRequest.GroupMembersRequestBuilder? = null
    private var currentSearchKeyword: String? = null
    private var excludeOwner: Boolean = false
    private var limit: Int = 30
    private var isFetching = false
    private var listenersTag: String? = null
    private var connectionListenerAttached = false
    private var groupEventsJob: Job? = null
    
    // Search debounce job - cancels pending search when new search is initiated
    private var searchJob: Job? = null
    
    // Logged in user (lazy to avoid SDK initialization issues in tests)
    private val loggedInUser: User? by lazy { getLoggedInUserSafe() }
    
    // List operations delegate
    private val listDelegate = ListOperationsDelegate(
        stateFlow = _members,
        equalityChecker = { a, b -> a.uid == b.uid }
    )
    
    // ==================== ListOperations Implementation ====================
    
    override fun addItem(item: GroupMember) = listDelegate.addItem(item)
    
    override fun addItems(items: List<GroupMember>) = listDelegate.addItems(items)
    
    override fun removeItem(item: GroupMember): Boolean = listDelegate.removeItem(item)
    
    override fun removeItemAt(index: Int): GroupMember? = listDelegate.removeItemAt(index)
    
    override fun updateItem(item: GroupMember, predicate: (GroupMember) -> Boolean): Boolean =
        listDelegate.updateItem(item, predicate)
    
    override fun clearItems() = listDelegate.clearItems()
    
    override fun getItems(): List<GroupMember> = listDelegate.getItems()
    
    override fun getItemAt(index: Int): GroupMember? = listDelegate.getItemAt(index)
    
    override fun getItemCount(): Int = listDelegate.getItemCount()
    
    override fun moveItemToTop(item: GroupMember) = listDelegate.moveItemToTop(item)
    
    override fun batch(operations: ListOperationsBatchScope<GroupMember>.() -> Unit) =
        listDelegate.batch(operations)
    
    // ==================== Public API ====================
    
    /**
     * Sets the group for which to fetch members.
     * This must be called before fetching members.
     * 
     * @param group The group object
     */
    open fun setGroup(group: Group) {
        this.currentGroup = group
        
        // Initialize request builders if not already set
        if (groupMembersRequestBuilder == null) {
            groupMembersRequestBuilder = GroupMembersRequest.GroupMembersRequestBuilder(group.guid)
                .setLimit(limit)
        }
        
        if (searchGroupMembersRequestBuilder == null) {
            searchGroupMembersRequestBuilder = GroupMembersRequest.GroupMembersRequestBuilder(group.guid)
        }
        
        // Add listeners if enabled
        if (enableListeners && listenersTag == null) {
            addListeners()
        }
    }
    
    /**
     * Sets a custom request builder for fetching members.
     * 
     * @param builder The custom request builder
     */
    open fun setGroupMembersRequestBuilder(builder: GroupMembersRequest.GroupMembersRequestBuilder) {
        this.groupMembersRequestBuilder = builder
        currentGroup?.let { group ->
            this.groupMembersRequestBuilder = builder.setGuid(group.guid)
        }
    }
    
    /**
     * Sets a custom request builder for searching members.
     * 
     * @param builder The custom search request builder
     */
    open fun setSearchRequestBuilder(builder: GroupMembersRequest.GroupMembersRequestBuilder) {
        this.searchGroupMembersRequestBuilder = builder
    }
    
    /**
     * Sets whether to exclude the group owner from the list.
     * 
     * @param exclude true to exclude owner
     */
    open fun setExcludeOwner(exclude: Boolean) {
        this.excludeOwner = exclude
    }
    
    /**
     * Fetches group members with pagination support.
     * Shows loading state only on initial fetch.
     */
    open fun fetchGroupMembers() {
        if (isFetching || !_hasMore.value) return
        
        val group = currentGroup ?: run {
            _uiState.value = GroupMembersUIState.Error(
                CometChatException("ERR_GROUP_NOT_SET", "Group must be set before fetching members")
            )
            return
        }
        
        viewModelScope.launch {
            isFetching = true
            _isLoading.value = true
            
            if (_members.value.isEmpty()) {
                _uiState.value = GroupMembersUIState.Loading
            }
            
            val result = fetchGroupMembersUseCase(
                guid = group.guid,
                limit = limit,
                searchKeyword = currentSearchKeyword
            )
            
            result.fold(
                onSuccess = { fetchedMembers ->
                    var members = fetchedMembers
                    
                    // Exclude owner if configured
                    if (excludeOwner && group.owner != null) {
                        members = members.filter { it.uid != group.owner }
                    }
                    
                    _hasMore.value = members.isNotEmpty()
                    
                    if (members.isNotEmpty()) {
                        addItems(members)
                    }
                    
                    // Update UI state
                    _uiState.value = if (_members.value.isEmpty()) {
                        GroupMembersUIState.Empty
                    } else {
                        GroupMembersUIState.Content(_members.value)
                    }
                    
                    // Attach connection listener on first successful fetch
                    if (!connectionListenerAttached && enableListeners) {
                        addConnectionListener()
                        connectionListenerAttached = true
                    }
                },
                onFailure = { error ->
                    _uiState.value = GroupMembersUIState.Error(
                        error as? CometChatException ?: CometChatException(
                            "ERR_FETCH_FAILED",
                            error.message ?: "Failed to fetch members"
                        )
                    )
                }
            )
            
            isFetching = false
            _isLoading.value = false
        }
    }
    
    /**
     * Searches for members by keyword.
     * Implements seamless search experience with:
     * - Debouncing (300ms) to reduce API calls during rapid typing
     * - Keeps existing content visible during search (no flicker)
     * - Conditional loading state (only when list is empty)
     * - Atomic list replacement when results arrive
     * 
     * @param query The search keyword (null to clear search)
     */
    open fun searchGroupMembers(query: String?) {
        // Cancel any pending search job (debouncing)
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            // Reset pagination state for new search
            _hasMore.value = true
            isFetching = false
            fetchGroupMembersUseCase.resetRequest()
            
            if (query.isNullOrEmpty()) {
                // Reset to normal fetch - clear list and fetch fresh
                currentSearchKeyword = null
                _members.value = emptyList()
                fetchGroupMembers()
                return@launch
            }
            
            // Debounce: wait 300ms before making API call
            // This reduces unnecessary API calls during rapid typing
            delay(300)
            
            currentSearchKeyword = query
            
            val group = currentGroup ?: run {
                _uiState.value = GroupMembersUIState.Error(
                    CometChatException("ERR_GROUP_NOT_SET", "Group must be set before searching members")
                )
                return@launch
            }
            
            // Only show loading state if list is currently empty AND we're not already showing empty state
            // This prevents flicker when:
            // 1. Content is already displayed (list not empty)
            // 2. Empty state is already displayed (previous search had no results)
            if (_members.value.isEmpty() && _uiState.value !is GroupMembersUIState.Empty) {
                _uiState.value = GroupMembersUIState.Loading
            }
            
            val result = fetchGroupMembersUseCase(
                guid = group.guid,
                limit = limit,
                searchKeyword = query
            )
            
            result.fold(
                onSuccess = { fetchedMembers ->
                    var members = fetchedMembers
                    
                    // Exclude owner if configured
                    if (excludeOwner && group.owner != null) {
                        members = members.filter { it.uid != group.owner }
                    }
                    
                    _hasMore.value = members.isNotEmpty()
                    
                    // Atomic list replacement - replace entire list at once
                    // This prevents flicker from clear-then-add pattern
                    _members.value = members
                    
                    // Direct state transition: Content → Empty without Loading in between
                    _uiState.value = if (members.isEmpty()) {
                        GroupMembersUIState.Empty
                    } else {
                        GroupMembersUIState.Content(members)
                    }
                    
                    // Attach connection listener on first successful fetch
                    if (!connectionListenerAttached && enableListeners) {
                        addConnectionListener()
                        connectionListenerAttached = true
                    }
                },
                onFailure = { error ->
                    // Only show error if we don't have existing content
                    // This provides graceful degradation - keep showing old results on error
                    if (_members.value.isEmpty()) {
                        _uiState.value = GroupMembersUIState.Error(
                            error as? CometChatException ?: CometChatException(
                                "ERR_SEARCH_FAILED",
                                error.message ?: "Failed to search members"
                            )
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Refreshes the member list from the beginning.
     */
    open fun refreshList() {
        isFetching = false
        _hasMore.value = true
        currentSearchKeyword = null
        clearItems()
        fetchGroupMembersUseCase.resetRequest()
        fetchGroupMembers()
    }
    
    /**
     * Kicks a member from the group.
     * Shows confirmation dialog first via [showKickConfirmation].
     * 
     * @param member The member to kick
     */
    open fun kickMember(member: GroupMember) {
        val group = currentGroup ?: return
        
        viewModelScope.launch {
            _dialogState.value = DialogState.Hidden
            _isLoading.value = true
            
            val result = kickGroupMemberUseCase(
                guid = group.guid,
                uid = member.uid
            )
            
            result.fold(
                onSuccess = {
                    // Update group member count
                    group.membersCount = group.membersCount - 1
                    
                    // Remove from list
                    removeItem(member)
                    
                    // Emit event
                    _events.emit(GroupMembersEvent.MemberKicked(member))
                    
                    // Update UI state
                    _uiState.value = if (_members.value.isEmpty()) {
                        GroupMembersUIState.Empty
                    } else {
                        GroupMembersUIState.Content(_members.value)
                    }
                    
                    // Trigger CometChat group events (safe for testing)
                    try {
                        val action = createActionMessage(member, group, CometChatConstants.ActionKeys.ACTION_KICKED)
                        CometChatEvents.emitGroupEvent(
                            CometChatGroupEvent.MemberKicked(action, member, loggedInUser ?: member, group)
                        )
                    } catch (_: Throwable) {
                        // SDK not initialized (e.g., in unit tests)
                    }
                },
                onFailure = { error ->
                    _uiState.value = GroupMembersUIState.Error(
                        error as? CometChatException ?: CometChatException(
                            "ERR_KICK_FAILED",
                            error.message ?: "Failed to kick member"
                        )
                    )
                }
            )
            
            _isLoading.value = false
        }
    }
    
    /**
     * Bans a member from the group.
     * Shows confirmation dialog first via [showBanConfirmation].
     * 
     * @param member The member to ban
     */
    open fun banMember(member: GroupMember) {
        val group = currentGroup ?: return
        
        viewModelScope.launch {
            _dialogState.value = DialogState.Hidden
            _isLoading.value = true
            
            val result = banGroupMemberUseCase(
                guid = group.guid,
                uid = member.uid
            )
            
            result.fold(
                onSuccess = {
                    // Update group member count
                    group.membersCount = group.membersCount - 1
                    
                    // Remove from list
                    removeItem(member)
                    
                    // Emit event
                    _events.emit(GroupMembersEvent.MemberBanned(member))
                    
                    // Update UI state
                    _uiState.value = if (_members.value.isEmpty()) {
                        GroupMembersUIState.Empty
                    } else {
                        GroupMembersUIState.Content(_members.value)
                    }
                    
                    // Trigger CometChat group events (safe for testing)
                    try {
                        val action = createActionMessage(member, group, CometChatConstants.ActionKeys.ACTION_BANNED)
                        CometChatEvents.emitGroupEvent(
                            CometChatGroupEvent.MemberBanned(action, member, loggedInUser ?: member, group)
                        )
                    } catch (_: Throwable) {
                        // SDK not initialized (e.g., in unit tests)
                    }
                },
                onFailure = { error ->
                    _uiState.value = GroupMembersUIState.Error(
                        error as? CometChatException ?: CometChatException(
                            "ERR_BAN_FAILED",
                            error.message ?: "Failed to ban member"
                        )
                    )
                }
            )
            
            _isLoading.value = false
        }
    }
    
    /**
     * Changes a member's scope in the group.
     * Shows scope selection dialog first via [showScopeSelection].
     * 
     * @param member The member whose scope to change
     * @param newScope The new scope (admin/moderator/participant)
     */
    open fun changeMemberScope(member: GroupMember, newScope: String) {
        val group = currentGroup ?: return
        
        viewModelScope.launch {
            _dialogState.value = DialogState.Hidden
            _isLoading.value = true
            
            val oldScope = member.scope
            
            val result = changeMemberScopeUseCase(
                guid = group.guid,
                uid = member.uid,
                scope = newScope
            )
            
            result.fold(
                onSuccess = {
                    // Create a new GroupMember with updated scope (avoid mutating in-place
                    // so DiffUtil/Compose detect the change via different object reference)
                    val updatedMember = userToGroupMember(member, isScopeUpdate = true, scope = newScope)
                    
                    // Update in list
                    updateItem(updatedMember) { it.uid == member.uid }
                    
                    // Emit event
                    _events.emit(GroupMembersEvent.MemberScopeChanged(updatedMember, newScope))
                    
                    // Update UI state
                    _uiState.value = GroupMembersUIState.Content(_members.value)
                    
                    // Trigger CometChat group events (safe for testing)
                    try {
                        val action = createActionMessage(member, group, CometChatConstants.ActionKeys.ACTION_SCOPE_CHANGED)
                        action.newScope = newScope
                        action.oldScope = oldScope
                        CometChatEvents.emitGroupEvent(
                            CometChatGroupEvent.MemberScopeChanged(action, updatedMember, newScope, oldScope, group)
                        )
                    } catch (_: Throwable) {
                        // SDK not initialized (e.g., in unit tests)
                    }
                },
                onFailure = { error ->
                    _uiState.value = GroupMembersUIState.Error(
                        error as? CometChatException ?: CometChatException(
                            "ERR_SCOPE_CHANGE_FAILED",
                            error.message ?: "Failed to change member scope"
                        )
                    )
                }
            )
            
            _isLoading.value = false
        }
    }
    
    // ==================== Selection Management ====================
    
    /**
     * Selects a member.
     * 
     * @param member The member to select
     */
    open fun selectMember(member: GroupMember) {
        _selectedMembers.value = _selectedMembers.value + (member.uid to member)
    }
    
    /**
     * Deselects a member.
     * 
     * @param member The member to deselect
     */
    open fun deselectMember(member: GroupMember) {
        _selectedMembers.value = _selectedMembers.value - member.uid
    }
    
    /**
     * Clears all selections.
     */
    open fun clearSelection() {
        _selectedMembers.value = emptyMap()
    }
    
    /**
     * Checks if a member is selected.
     * 
     * @param member The member to check
     * @return true if selected
     */
    open fun isSelected(member: GroupMember): Boolean {
        return _selectedMembers.value.containsKey(member.uid)
    }
    
    // ==================== Dialog Management ====================
    
    /**
     * Shows kick confirmation dialog.
     * 
     * @param member The member to kick
     */
    open fun showKickConfirmation(member: GroupMember) {
        _dialogState.value = DialogState.ConfirmKick(member)
    }
    
    /**
     * Shows ban confirmation dialog.
     * 
     * @param member The member to ban
     */
    open fun showBanConfirmation(member: GroupMember) {
        _dialogState.value = DialogState.ConfirmBan(member)
    }
    
    /**
     * Shows scope selection dialog.
     * 
     * @param member The member whose scope to change
     */
    open fun showScopeSelection(member: GroupMember) {
        _dialogState.value = DialogState.SelectScope(member, member.scope)
    }
    
    /**
     * Dismisses any open dialog.
     */
    open fun dismissDialog() {
        _dialogState.value = DialogState.Hidden
    }
    
    // ==================== CometChat Listeners ====================
    
    /**
     * Adds CometChat group and user listeners for real-time updates.
     */
    private fun addListeners() {
        listenersTag = System.currentTimeMillis().toString()
        
        // Group listener — matches the original Java CometChat.addGroupListener pattern
        CometChat.addGroupListener(listenersTag!!, object : CometChat.GroupListener() {
            override fun onGroupMemberJoined(action: Action, joinedUser: User, joinedGroup: Group) {
                if (joinedGroup != null && joinedGroup.guid == currentGroup?.guid) {
                    updateGroupMember(joinedUser, isRemoved = false, isScopeUpdate = false, action = action)
                }
            }
            
            override fun onGroupMemberLeft(action: Action, leftUser: User, leftGroup: Group) {
                if (leftGroup != null && leftGroup.guid == currentGroup?.guid) {
                    updateGroupMember(leftUser, isRemoved = true, isScopeUpdate = false, action = action)
                }
            }
            
            override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, kickedFrom: Group) {
                if (kickedFrom != null && kickedFrom.guid == currentGroup?.guid) {
                    updateGroupMember(kickedUser, isRemoved = true, isScopeUpdate = false, action = action)
                }
            }
            
            override fun onGroupMemberBanned(action: Action, bannedUser: User, bannedBy: User, bannedFrom: Group) {
                if (bannedFrom != null && bannedFrom.guid == currentGroup?.guid) {
                    updateGroupMember(bannedUser, isRemoved = true, isScopeUpdate = false, action = action)
                }
            }
            
            override fun onGroupMemberScopeChanged(
                action: Action,
                updatedBy: User,
                updatedUser: User,
                scopeChangedTo: String,
                scopeChangedFrom: String,
                group: Group
            ) {
                if (group != null && group.guid == currentGroup?.guid) {
                    updateGroupMember(updatedUser, isRemoved = false, isScopeUpdate = true, action = action)
                }
            }
            
            override fun onMemberAddedToGroup(action: Action, addedBy: User, userAdded: User, addedTo: Group) {
                if (addedTo != null && addedTo.guid == currentGroup?.guid) {
                    updateGroupMember(userAdded, isRemoved = false, isScopeUpdate = false, action = action)
                }
            }
        })
        
        // Local UIKit event listener — matches the original Java CometChatGroupEvents.addGroupListener pattern
        addLocalEventListeners()
        
        // User listener for online/offline status
        CometChat.addUserListener(listenersTag!!, object : CometChat.UserListener() {
            override fun onUserOnline(user: User) {
                handleUserStatusChange(user, UIKitConstants.UserStatus.ONLINE)
            }
            
            override fun onUserOffline(user: User) {
                handleUserStatusChange(user, UIKitConstants.UserStatus.OFFLINE)
            }
        })
    }
    
    /**
     * Adds local UIKit event listeners for group events emitted by the same application.
     * Equivalent to CometChatGroupEvents.addGroupListener in the original Java implementation.
     * Handles events like scope changes, kicks, bans, member additions from other screens.
     */
    private fun addLocalEventListeners() {
        groupEventsJob = viewModelScope.launch {
            CometChatEvents.groupEvents.collect { event ->
                val eventGroup = when (event) {
                    is CometChatGroupEvent.MemberKicked -> event.group
                    is CometChatGroupEvent.MemberBanned -> event.group
                    is CometChatGroupEvent.MemberScopeChanged -> event.group
                    is CometChatGroupEvent.MembersAdded -> event.group
                    is CometChatGroupEvent.MemberUnbanned -> event.group
                    is CometChatGroupEvent.MemberJoined -> event.group
                    is CometChatGroupEvent.OwnershipChanged -> event.group
                    else -> null
                }
                if (eventGroup?.guid != currentGroup?.guid || currentGroup == null) return@collect

                when (event) {
                    // ccGroupMemberBanned
                    is CometChatGroupEvent.MemberBanned -> {
                        removeGroupMember(userToGroupMember(event.user, isScopeUpdate = false))
                    }
                    // ccGroupMemberKicked
                    is CometChatGroupEvent.MemberKicked -> {
                        removeGroupMember(userToGroupMember(event.user, isScopeUpdate = false))
                    }
                    // ccGroupMemberScopeChanged
                    is CometChatGroupEvent.MemberScopeChanged -> {
                        updateGroupMember(userToGroupMember(event.user, isScopeUpdate = true, scope = event.newScope))
                    }
                    // ccGroupMemberAdded
                    is CometChatGroupEvent.MembersAdded -> {
                        event.users.forEach { user ->
                            addToTop(userToGroupMember(user, isScopeUpdate = false))
                        }
                    }
                    // ccGroupMemberUnBanned
                    is CometChatGroupEvent.MemberUnbanned -> {
                        setGroup(event.group)
                        addToTop(userToGroupMember(event.user, isScopeUpdate = false))
                    }
                    // ccGroupMemberJoined
                    is CometChatGroupEvent.MemberJoined -> {
                        setGroup(event.group)
                        addToTop(userToGroupMember(event.user, isScopeUpdate = false))
                    }
                    // ccOwnershipChanged
                    is CometChatGroupEvent.OwnershipChanged -> {
                        setGroup(event.group)
                        updateGroupMember(event.newOwner)
                    }
                    else -> { /* GroupCreated, GroupDeleted, GroupLeft not relevant here */ }
                }
            }
        }
    }
    
    /**
     * Adds connection listener to refresh list on reconnection.
     */
    private fun addConnectionListener() {
        CometChat.addConnectionListener(listenersTag!!, object : CometChat.ConnectionListener {
            override fun onConnected() {
                refreshList()
            }
            
            override fun onConnecting() {}
            override fun onDisconnected() {}
            override fun onFeatureThrottled() {}
            override fun onConnectionError(e: CometChatException) {}
        })
    }
    
    /**
     * Removes all CometChat listeners.
     */
    private fun removeListeners() {
        listenersTag?.let { tag ->
            CometChat.removeGroupListener(tag)
            CometChat.removeUserListener(tag)
            CometChat.removeConnectionListener(tag)
        }
    }
    
    // ==================== Event Handlers ====================
    
    /**
     * Unified routing method for SDK group listener events.
     * Matches the original Java updateGroupMember(User, boolean, boolean, Action) pattern.
     *
     * @param user The user involved in the event
     * @param isRemoved true if the user was removed (left/kicked/banned)
     * @param isScopeUpdate true if this is a scope change event
     * @param action The Action message containing scope info
     */
    private fun updateGroupMember(user: User, isRemoved: Boolean, isScopeUpdate: Boolean, action: Action) {
        if (!isRemoved && !isScopeUpdate) {
            // Joined or added — add to top with old scope
            addToTop(userToGroupMember(user, isScopeUpdate = false, scope = action.oldScope))
        } else if (isRemoved && !isScopeUpdate) {
            // Left, kicked, or banned — remove
            removeGroupMember(userToGroupMember(user, isScopeUpdate = false, scope = action.oldScope))
        } else if (!isRemoved) {
            // Scope changed — update with new scope
            updateGroupMember(userToGroupMember(user, isScopeUpdate = true, scope = action.newScope))
        }
    }
    
    /**
     * Updates an existing group member in the list.
     * Matches the original Java updateGroupMember(GroupMember) method.
     */
    private fun updateGroupMember(member: GroupMember) {
        if (updateItem(member) { it.uid == member.uid }) {
            _uiState.value = GroupMembersUIState.Content(_members.value)
        }
    }
    
    /**
     * Adds a group member to the top of the list if not already present.
     * Matches the original Java addToTop(GroupMember) method.
     */
    private fun addToTop(member: GroupMember) {
        moveItemToTop(member)
    }
    
    /**
     * Removes a group member from the list.
     * Matches the original Java removeGroupMember(GroupMember) method.
     * Updates state to Empty if list becomes empty.
     */
    private fun removeGroupMember(member: GroupMember) {
        removeItem(member)
        _uiState.value = if (_members.value.isEmpty()) {
            GroupMembersUIState.Empty
        } else {
            GroupMembersUIState.Content(_members.value)
        }
    }
    
    private fun handleUserStatusChange(user: User, status: String) {
        val index = _members.value.indexOfFirst { it.uid == user.uid }
        if (index >= 0) {
            val existing = _members.value[index]
            val updatedMember = GroupMember(existing.uid, existing.scope).apply {
                name = existing.name
                avatar = existing.avatar
                this.status = status
                role = existing.role
                metadata = existing.metadata
                statusMessage = existing.statusMessage
                lastActiveAt = existing.lastActiveAt
                tags = existing.tags
            }
            updateGroupMember(updatedMember)
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Safely gets the logged-in user, returning null if SDK is not initialized.
     */
    private fun getLoggedInUserSafe(): User? {
        return try {
            CometChatUIKit.getLoggedInUser()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Converts a User to a GroupMember.
     * Matches the original Java MembersUtils.userToGroupMember(user, isScopeUpdate, newScope) pattern.
     *
     * @param user The user to convert
     * @param isScopeUpdate If true, uses the provided scope; if false, uses SCOPE_PARTICIPANT
     * @param scope The scope to assign (only used when isScopeUpdate is true)
     */
    private fun userToGroupMember(
        user: User,
        isScopeUpdate: Boolean,
        scope: String = ""
    ): GroupMember {
        val memberScope = if (isScopeUpdate) scope else CometChatConstants.SCOPE_PARTICIPANT
        return GroupMember(user.uid, memberScope).apply {
            name = user.name
            avatar = user.avatar
            status = user.status
        }
    }
    
    /**
     * Creates an Action message for group events.
     */
    private fun createActionMessage(member: GroupMember, group: Group, actionType: String): Action {
        return Action().apply {
            action = actionType
            conversationId = "group_${group.guid}"
            message = ""
            rawData = null
            oldScope = member.scope
            actionBy = loggedInUser ?: member
            actionOn = member
            actionFor = group
        }
    }
    
    // ==================== Lifecycle ====================
    
    override fun onCleared() {
        super.onCleared()
        removeListeners()
        groupEventsJob?.cancel()
        searchJob?.cancel()
        listDelegate.cancel()
    }
}
