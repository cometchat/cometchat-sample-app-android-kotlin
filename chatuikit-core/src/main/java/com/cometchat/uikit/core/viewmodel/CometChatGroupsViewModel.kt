package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.events.CometChatGroupEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.state.GroupsUIState
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
 * ViewModel for managing groups list state.
 * Implements ListOperations for standardized list manipulation.
 * Uses StateFlow for reactive state management.
 *
 * @param fetchGroupsUseCase Use case for fetching groups
 * @param joinGroupUseCase Use case for joining groups
 * @param enableListeners Whether to enable CometChat listeners (set to false for testing)
 */
open class CometChatGroupsViewModel(
    private val fetchGroupsUseCase: FetchGroupsUseCase,
    private val joinGroupUseCase: JoinGroupUseCase,
    private val enableListeners: Boolean = true
) : ViewModel(), ListOperations<Group> {

    // UI State
    private val _uiState = MutableStateFlow<GroupsUIState>(GroupsUIState.Loading)
    val uiState: StateFlow<GroupsUIState> = _uiState.asStateFlow()

    // Groups list
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    // Selection state
    private val _selectedGroups = MutableStateFlow<Set<Group>>(emptySet())
    val selectedGroups: StateFlow<Set<Group>> = _selectedGroups.asStateFlow()

    // Events
    private val _groupJoinedEvent = MutableSharedFlow<Group>()
    val groupJoinedEvent: SharedFlow<Group> = _groupJoinedEvent.asSharedFlow()

    private val _groupCreatedEvent = MutableSharedFlow<Group>()
    val groupCreatedEvent: SharedFlow<Group> = _groupCreatedEvent.asSharedFlow()

    // Scroll to top event
    private val _scrollToTopEvent = MutableSharedFlow<Unit>()
    val scrollToTopEvent: SharedFlow<Unit> = _scrollToTopEvent.asSharedFlow()

    // List operations delegate
    private val listDelegate = ListOperationsDelegate(
        stateFlow = _groups,
        equalityChecker = { a, b -> a.guid == b.guid }
    )

    // Configuration
    private var groupsRequest: GroupsRequest? = null
    private var groupsRequestBuilder: GroupsRequest.GroupsRequestBuilder? = null
    private var searchRequestBuilder: GroupsRequest.GroupsRequestBuilder? = null
    private var isFetching = false
    private var hasMoreData = true
    private var listenersTag: String? = null

    // Local event listener job
    private var groupEventsJob: Job? = null
    
    // Search debounce job - cancels pending search when new search is initiated
    private var searchJob: Job? = null

    init {
        if (enableListeners) {
            addListeners()
        }
        fetchGroups()
    }

    // ==================== ListOperations Implementation ====================

    override fun addItem(item: Group) = listDelegate.addItem(item)
    override fun addItems(items: List<Group>) = listDelegate.addItems(items)
    override fun removeItem(item: Group) = listDelegate.removeItem(item)
    override fun removeItemAt(index: Int) = listDelegate.removeItemAt(index)
    override fun updateItem(item: Group, predicate: (Group) -> Boolean) =
        listDelegate.updateItem(item, predicate)
    override fun clearItems() = listDelegate.clearItems()
    override fun getItems() = listDelegate.getItems()
    override fun getItemAt(index: Int) = listDelegate.getItemAt(index)
    override fun getItemCount() = listDelegate.getItemCount()
    override fun moveItemToTop(item: Group) = listDelegate.moveItemToTop(item)
    override fun batch(operations: ListOperationsBatchScope<Group>.() -> Unit) =
        listDelegate.batch(operations)


    // ==================== Public API Methods ====================

    /**
     * Fetches groups with pagination support.
     * Shows loading state only on initial fetch.
     * Prevents concurrent fetches using isFetching flag.
     */
    fun fetchGroups() {
        if (isFetching || !hasMoreData) return

        viewModelScope.launch {
            isFetching = true

            if (_groups.value.isEmpty()) {
                _uiState.value = GroupsUIState.Loading
            }

            if (groupsRequest == null) {
                val builder = groupsRequestBuilder
                    ?: GroupsRequest.GroupsRequestBuilder().setLimit(30)
                groupsRequest = builder.build()
            }

            groupsRequest?.let { request ->
                fetchGroupsUseCase(request)
                    .onSuccess { newGroups ->
                        if (newGroups.isNotEmpty()) {
                            // Create defensive copies to prevent SDK from mutating our list items
                            val copiedGroups = newGroups.map { createGroupCopy(it) }
                            val updatedList = _groups.value + copiedGroups
                            _groups.value = updatedList
                            _uiState.value = GroupsUIState.Content(updatedList)
                        } else {
                            hasMoreData = false
                            if (_groups.value.isEmpty()) {
                                _uiState.value = GroupsUIState.Empty
                            }
                        }
                    }
                    .onFailure { exception ->
                        _uiState.value = GroupsUIState.Error(exception as CometChatException)
                    }

                isFetching = false
            } ?: run {
                isFetching = false
            }
        }
    }

    /**
     * Searches for groups based on the provided keyword.
     * Implements seamless search experience with:
     * - Debouncing (300ms) to reduce API calls during rapid typing
     * - Keeps existing content visible during search (no flicker)
     * - Conditional loading state (only when list is empty)
     * - Atomic list replacement when results arrive
     *
     * @param query The search keyword for finding groups
     */
    fun searchGroups(query: String?) {
        // Cancel any pending search job (debouncing)
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            // Reset pagination state for new search
            hasMoreData = true
            groupsRequest = null
            
            if (query.isNullOrEmpty()) {
                // Reset to normal fetch - clear list and fetch fresh
                _groups.value = emptyList()
                isFetching = false
                fetchGroups()
                return@launch
            }
            
            // Debounce: wait 300ms before making API call
            // This reduces unnecessary API calls during rapid typing
            delay(300)
            
            // Only show loading state if list is currently empty AND we're not already showing empty state
            // This prevents flicker when:
            // 1. Content is already displayed (list not empty)
            // 2. Empty state is already displayed (previous search had no results)
            if (_groups.value.isEmpty() && _uiState.value !is GroupsUIState.Empty) {
                _uiState.value = GroupsUIState.Loading
            }
            
            val builder = searchRequestBuilder ?: groupsRequestBuilder
                ?: GroupsRequest.GroupsRequestBuilder().setLimit(30)
            
            // Build request with search keyword
            val searchRequest = builder.setSearchKeyWord(query).build()
            
            fetchGroupsUseCase(searchRequest)
                .onSuccess { groups ->
                    // Create defensive copies to prevent SDK from mutating our list items
                    val copiedGroups = groups.map { createGroupCopy(it) }
                    
                    // Atomic list replacement - replace entire list at once
                    // This prevents flicker from clear-then-add pattern
                    _groups.value = copiedGroups
                    
                    // Direct state transition: Content → Empty without Loading in between
                    _uiState.value = if (copiedGroups.isEmpty()) {
                        GroupsUIState.Empty
                    } else {
                        GroupsUIState.Content(copiedGroups)
                    }
                    
                    // Update request for pagination
                    groupsRequest = searchRequest
                }
                .onFailure { exception ->
                    // Only show error if we don't have existing content
                    // This provides graceful degradation - keep showing old results on error
                    if (_groups.value.isEmpty()) {
                        _uiState.value = GroupsUIState.Error(exception as CometChatException)
                    }
                }
        }
    }

    /**
     * Refreshes the group list from the beginning.
     * This is a silent refresh - it does not show loading shimmer to maintain scroll position.
     * Uses client's request builder if set, otherwise creates a default one.
     */
    fun refreshList() {
        // Reset pagination state for fresh fetch
        hasMoreData = true
        isFetching = false

        viewModelScope.launch {
            // Silent refresh - don't show loading state, keep existing data visible
            // Only show loading if list is empty
            if (_groups.value.isEmpty()) {
                _uiState.value = GroupsUIState.Loading
            }

            // Use client's builder if provided, otherwise create default
            val builder = groupsRequestBuilder
                ?: GroupsRequest.GroupsRequestBuilder().setLimit(30)
            
            // Build a fresh request for the refresh
            val refreshRequest = builder.build()
            
            fetchGroupsUseCase(refreshRequest)
                .onSuccess { groups ->
                    // Create defensive copies to prevent SDK from mutating our list items
                    val copiedGroups = groups.map { createGroupCopy(it) }
                    _groups.value = copiedGroups
                    _uiState.value = if (copiedGroups.isEmpty()) {
                        GroupsUIState.Empty
                    } else {
                        GroupsUIState.Content(copiedGroups)
                    }
                    
                    // Create new request for pagination
                    groupsRequest = builder.build()
                }
                .onFailure { exception ->
                    // Only show error if list is empty, otherwise keep existing data
                    if (_groups.value.isEmpty()) {
                        _uiState.value = GroupsUIState.Error(exception as CometChatException)
                    }
                }
        }
    }

    /**
     * Joins a group with the specified parameters.
     *
     * @param group The group to join
     * @param password Optional password for password-protected groups
     */
    fun joinGroup(group: Group, password: String? = null) {
        viewModelScope.launch {
            joinGroupUseCase(group.guid, group.groupType, password)
                .onSuccess { joinedGroup ->
                    _groupJoinedEvent.emit(joinedGroup)
                    updateItem(joinedGroup) { it.guid == joinedGroup.guid }
                }
                .onFailure { /* Error handled via onError callback */ }
        }
    }

    /**
     * Selects a group based on selection mode.
     * In SINGLE mode, clears previous selection before selecting new group.
     * In MULTIPLE mode, toggles selection state.
     *
     * @param group The group to select
     * @param mode The selection mode (SINGLE or MULTIPLE)
     */
    fun selectGroup(group: Group, mode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.MULTIPLE) {
        val current = _selectedGroups.value.toMutableSet()
        
        when (mode) {
            UIKitConstants.SelectionMode.SINGLE -> {
                // In SINGLE mode, clear all and select only this group
                current.clear()
                current.add(group)
            }
            UIKitConstants.SelectionMode.MULTIPLE -> {
                // In MULTIPLE mode, toggle selection
                if (current.any { it.guid == group.guid }) {
                    current.removeAll { it.guid == group.guid }
                } else {
                    current.add(group)
                }
            }
            UIKitConstants.SelectionMode.NONE -> {
                // Do nothing in NONE mode
                return
            }
        }
        _selectedGroups.value = current
    }

    /**
     * Deselects a group.
     *
     * @param group The group to deselect
     */
    fun deselectGroup(group: Group) {
        val current = _selectedGroups.value.toMutableSet()
        current.removeAll { it.guid == group.guid }
        _selectedGroups.value = current
    }

    /**
     * Clears all selected groups.
     */
    fun clearSelection() {
        _selectedGroups.value = emptySet()
    }

    /**
     * Returns the list of currently selected groups.
     */
    fun getSelectedGroups(): List<Group> = _selectedGroups.value.toList()

    /**
     * Sets the groups request builder for customizing fetch parameters.
     *
     * @param builder The custom request builder
     */
    fun setGroupsRequestBuilder(builder: GroupsRequest.GroupsRequestBuilder) {
        groupsRequestBuilder = builder
        groupsRequest = builder.build()
    }

    /**
     * Sets the search request builder for customizing search parameters.
     *
     * @param builder The custom search request builder
     */
    fun setSearchRequestBuilder(builder: GroupsRequest.GroupsRequestBuilder) {
        searchRequestBuilder = builder
    }

    // ==================== Private Methods ====================

    /**
     * Adds CometChat SDK listeners for real-time group updates.
     * Registers GroupListener for member events and ConnectionListener for reconnection.
     * Also subscribes to local group events via CometChatEvents flow.
     */
    private fun addListeners() {
        listenersTag = "GroupsList_${System.currentTimeMillis()}"

        listenersTag?.let { tag ->
            // CometChat Group Listener for SDK events
            CometChat.addGroupListener(tag, object : CometChat.GroupListener() {
                override fun onGroupMemberJoined(
                    action: Action,
                    joinedUser: User,
                    joinedGroup: Group
                ) {
                    if (joinedUser.uid == CometChatUIKit.getLoggedInUser()?.uid) {
                        joinedGroup.setHasJoined(true)
                    }
                    updateGroupInList(joinedGroup)
                }

                override fun onGroupMemberLeft(
                    action: Action,
                    leftUser: User,
                    leftGroup: Group
                ) {
                    updateGroupInList(leftGroup)
                }

                override fun onGroupMemberKicked(
                    action: Action,
                    kickedUser: User,
                    kickedBy: User,
                    kickedFrom: Group
                ) {
                    updateGroupInList(kickedFrom)
                }

                override fun onGroupMemberBanned(
                    action: Action,
                    bannedUser: User,
                    bannedBy: User,
                    bannedFrom: Group
                ) {
                    updateGroupInList(bannedFrom)
                }

                override fun onGroupMemberUnbanned(
                    action: Action,
                    unbannedUser: User,
                    unbannedBy: User,
                    unbannedFrom: Group
                ) {
                    updateGroupInList(unbannedFrom)
                }

                override fun onGroupMemberScopeChanged(
                    action: Action,
                    updatedBy: User,
                    updatedUser: User,
                    scopeChangedTo: String,
                    scopeChangedFrom: String,
                    group: Group
                ) {
                    updateGroupInList(group)
                }

                override fun onMemberAddedToGroup(
                    action: Action,
                    addedBy: User,
                    userAdded: User,
                    addedTo: Group
                ) {
                    updateGroupInList(addedTo)
                }
            })

            // Connection listener for reconnection handling
            CometChat.addConnectionListener(tag, object : CometChat.ConnectionListener {
                override fun onConnected() {
                    refreshList()
                }

                override fun onConnecting() {
                    // No action needed
                }

                override fun onDisconnected() {
                    // No action needed
                }

                override fun onFeatureThrottled() {
                    // No action needed
                }

                override fun onConnectionError(error: CometChatException?) {
                    // No action needed
                }
            })
        }

        // Subscribe to local group events via CometChatEvents flow
        addLocalEventListeners()
    }

    /**
     * Adds local event listeners for UI-triggered group events.
     * These events are emitted by other UI components (e.g., when a group is created).
     */
    private fun addLocalEventListeners() {
        groupEventsJob = viewModelScope.launch {
            CometChatEvents.groupEvents.collect { event ->
                when (event) {
                    is CometChatGroupEvent.GroupCreated -> {
                        viewModelScope.launch {
                            _groupCreatedEvent.emit(event.group)
                            addToTop(event.group)
                        }
                    }

                    is CometChatGroupEvent.GroupLeft -> {
                        if (event.user.uid == CometChatUIKit.getLoggedInUser()?.uid) {
                            if (event.group.groupType == CometChatConstants.GROUP_TYPE_PRIVATE) {
                                removeItem(event.group)
                            } else {
                                updateGroupInList(event.group)
                            }
                        }
                    }

                    is CometChatGroupEvent.GroupDeleted -> {
                        removeItem(event.group)
                    }

                    is CometChatGroupEvent.MemberJoined -> {
                        updateGroupInList(event.group)
                    }

                    is CometChatGroupEvent.MembersAdded -> {
                        updateGroupInList(event.group)
                    }

                    is CometChatGroupEvent.MemberKicked -> {
                        updateGroupInList(event.group)
                    }

                    is CometChatGroupEvent.MemberBanned -> {
                        updateGroupInList(event.group)
                    }

                    is CometChatGroupEvent.MemberUnbanned -> {
                        updateGroupInList(event.group)
                    }

                    is CometChatGroupEvent.MemberScopeChanged -> {
                        updateGroupInList(event.group)
                    }

                    is CometChatGroupEvent.OwnershipChanged -> {
                        updateGroupInList(event.group)
                    }
                }
            }
        }
    }

    /**
     * Updates a group in the list if it exists.
     * Also updates the UI state accordingly.
     * 
     * Note: We create a defensive copy of the Group object to ensure
     * DiffUtil/Compose detects the change. The SDK may mutate objects in place,
     * so comparing the same object reference would not detect changes.
     */
    private fun updateGroupInList(group: Group) {
        val currentList = _groups.value
        val index = currentList.indexOfFirst { it.guid == group.guid }
        if (index >= 0) {
            // Create a defensive copy of the Group to ensure DiffUtil detects changes
            // The SDK may mutate the same object reference, so we need a new instance
            val groupCopy = createGroupCopy(group)
            val newList = currentList.toMutableList()
            newList[index] = groupCopy
            _groups.value = newList.toList()
        }
        updateUIState()
    }

    /**
     * Creates a defensive copy of a Group object.
     * This ensures that DiffUtil/Compose can detect changes even if
     * the SDK mutates the original object in place.
     */
    private fun createGroupCopy(group: Group): Group {
        return Group(
            group.guid,
            group.name,
            group.groupType,
            group.password,
            group.icon,
            group.description
        ).apply {
            owner = group.owner
            metadata = group.metadata
            createdAt = group.createdAt
            updatedAt = group.updatedAt
            setHasJoined(group.isJoined)
            membersCount = group.membersCount
            joinedAt = group.joinedAt
            tags = group.tags
            scope = group.scope
        }
    }

    /**
     * Adds a group to the top of the list if it doesn't already exist.
     * Emits scroll to top event after adding.
     */
    private fun addToTop(group: Group) {
        if (_groups.value.none { it.guid == group.guid }) {
            // Create defensive copy to prevent SDK from mutating our list item
            val groupCopy = createGroupCopy(group)
            moveItemToTop(groupCopy)
            viewModelScope.launch {
                _scrollToTopEvent.emit(Unit)
            }
        }
        updateUIState()
    }

    /**
     * Updates the UI state based on the current groups list.
     */
    private fun updateUIState() {
        _uiState.value = if (_groups.value.isEmpty()) {
            GroupsUIState.Empty
        } else {
            GroupsUIState.Content(_groups.value)
        }
    }

    /**
     * Removes all CometChat SDK listeners and cancels local event jobs.
     */
    private fun removeListeners() {
        listenersTag?.let { tag ->
            CometChat.removeGroupListener(tag)
            CometChat.removeConnectionListener(tag)
        }
        groupEventsJob?.cancel()
        groupEventsJob = null
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
        searchJob?.cancel()
        listDelegate.cancel()
    }
}
