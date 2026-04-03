package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import com.cometchat.uikit.core.events.CometChatUserEvent
import com.cometchat.uikit.core.state.MessageHeaderUIState
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
 * ViewModel for the CometChatMessageHeader component.
 * Manages UI state for displaying user or group conversation headers.
 * 
 * This ViewModel is shared by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * implementations, ensuring consistent behavior across both UI frameworks.
 * 
 * Features:
 * - User/Group data management with real-time updates
 * - Typing indicator handling with debounce
 * - Online/offline status tracking for users
 * - Member count tracking for groups
 * - SDK and UIKit local event listeners for real-time updates
 * 
 * @param getUserUseCase Use case for fetching user data
 * @param getGroupUseCase Use case for fetching group data
 * @param enableListeners Whether to enable CometChat listeners (set to false for testing)
 */
open class CometChatMessageHeaderViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val getGroupUseCase: GetGroupUseCase,
    private val enableListeners: Boolean = true
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<MessageHeaderUIState>(MessageHeaderUIState.Loading)
    val uiState: StateFlow<MessageHeaderUIState> = _uiState.asStateFlow()

    // User state
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    // Group state
    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group.asStateFlow()

    // Typing indicator state
    private val _typingIndicator = MutableStateFlow<TypingIndicator?>(null)
    val typingIndicator: StateFlow<TypingIndicator?> = _typingIndicator.asStateFlow()

    // Member count for groups
    private val _memberCount = MutableStateFlow(0)
    val memberCount: StateFlow<Int> = _memberCount.asStateFlow()

    // Error events - emitted for UI to handle via callback
    private val _errorEvent = MutableSharedFlow<CometChatException>()
    val errorEvent: SharedFlow<CometChatException> = _errorEvent.asSharedFlow()

    // Listener tag for SDK listeners
    private var listenersTag: String? = null

    // Typing debounce job
    private var typingDebounceJob: Job? = null
    
    // Typing indicator debounce delay (1 second, matching Java implementation)
    private val TYPING_INDICATOR_DEBOUNCER = 1000L

    // Current entity ID for listener filtering
    private var currentId: String? = null
    
    // Local event listener jobs
    private var userEventsJob: Job? = null
    private var groupEventsJob: Job? = null

    init {
        if (enableListeners) {
            addListeners()
        }
    }

    /**
     * Sets the user for the message header.
     * Clears any existing group data and updates the UI state.
     * 
     * @param user The User object to display in the header
     */
    fun setUser(user: User) {
        _user.value = user
        _group.value = null
        currentId = user.uid
        _uiState.value = MessageHeaderUIState.UserContent(user)
    }

    /**
     * Sets the group for the message header.
     * Clears any existing user data and updates the UI state.
     * 
     * @param group The Group object to display in the header
     */
    fun setGroup(group: Group) {
        _group.value = group
        _user.value = null
        currentId = group.guid
        _memberCount.value = group.membersCount
        _uiState.value = MessageHeaderUIState.GroupContent(group)
    }

    /**
     * Refreshes user data from the server.
     * Used for reconnection scenarios or manual refresh.
     * 
     * @param uid The user ID to refresh
     */
    fun refreshUser(uid: String) {
        viewModelScope.launch {
            getUserUseCase(uid)
                .onSuccess { user ->
                    setUser(user)
                }
                .onFailure { e ->
                    _errorEvent.emit(e as CometChatException)
                }
        }
    }

    /**
     * Refreshes group data from the server.
     * Used for reconnection scenarios or manual refresh.
     * 
     * @param guid The group ID to refresh
     */
    fun refreshGroup(guid: String) {
        viewModelScope.launch {
            getGroupUseCase(guid)
                .onSuccess { group ->
                    setGroup(group)
                }
                .onFailure { e ->
                    _errorEvent.emit(e as CometChatException)
                }
        }
    }

    /**
     * Adds all CometChat SDK listeners and UIKit local event listeners.
     * Called during initialization if enableListeners is true.
     */
    private fun addListeners() {
        listenersTag = "MessageHeader_${System.currentTimeMillis()}"

        listenersTag?.let { tag ->
            addSDKListeners(tag)
            addUIKitLocalEventListeners(tag)
        }
    }

    /**
     * Adds CometChat SDK listeners for real-time updates.
     * Includes user presence, group events, message (typing), and connection listeners.
     */
    private fun addSDKListeners(tag: String) {
        // User presence listener for online/offline status
        CometChat.addUserListener(tag, object : CometChat.UserListener() {
            override fun onUserOnline(user: User) {
                if (user.uid == currentId && !isBlocked(_user.value)) {
                    _user.value = user
                    _uiState.value = MessageHeaderUIState.UserContent(user)
                }
            }

            override fun onUserOffline(user: User) {
                if (user.uid == currentId && !isBlocked(_user.value)) {
                    _user.value = user
                    _uiState.value = MessageHeaderUIState.UserContent(user)
                }
            }
        })

        // Group listener for member changes
        CometChat.addGroupListener(tag, object : CometChat.GroupListener() {
            override fun onGroupMemberJoined(action: Action, joinedUser: User, joinedGroup: Group) {
                if (joinedGroup.guid == currentId) {
                    _group.value = joinedGroup
                    _memberCount.value = joinedGroup.membersCount
                    _uiState.value = MessageHeaderUIState.GroupContent(joinedGroup)
                }
            }

            override fun onGroupMemberLeft(action: Action, leftUser: User, leftGroup: Group) {
                if (leftGroup.guid == currentId) {
                    _group.value = leftGroup
                    _memberCount.value = leftGroup.membersCount
                    _uiState.value = MessageHeaderUIState.GroupContent(leftGroup)
                }
            }

            override fun onGroupMemberKicked(
                action: Action,
                kickedUser: User,
                kickedBy: User,
                kickedFrom: Group
            ) {
                if (kickedFrom.guid == currentId) {
                    _group.value = kickedFrom
                    _memberCount.value = kickedFrom.membersCount
                    _uiState.value = MessageHeaderUIState.GroupContent(kickedFrom)
                }
            }

            override fun onGroupMemberBanned(
                action: Action,
                bannedUser: User,
                bannedBy: User,
                bannedFrom: Group
            ) {
                if (bannedFrom.guid == currentId) {
                    _group.value = bannedFrom
                    _memberCount.value = bannedFrom.membersCount
                    _uiState.value = MessageHeaderUIState.GroupContent(bannedFrom)
                }
            }

            override fun onMemberAddedToGroup(
                action: Action,
                addedBy: User,
                userAdded: User,
                addedTo: Group
            ) {
                if (addedTo.guid == currentId) {
                    _group.value = addedTo
                    _memberCount.value = addedTo.membersCount
                    _uiState.value = MessageHeaderUIState.GroupContent(addedTo)
                }
            }
        })

        // Message listener for typing indicators
        CometChat.addMessageListener(tag, object : CometChat.MessageListener() {
            override fun onTypingStarted(typingIndicator: TypingIndicator) {
                if (!isBlocked(_user.value)) {
                    handleTypingIndicator(typingIndicator, true)
                }
            }

            override fun onTypingEnded(typingIndicator: TypingIndicator) {
                if (!isBlocked(_user.value)) {
                    handleTypingIndicator(typingIndicator, false)
                }
            }
        })

        // Connection listener for reconnection
        CometChat.addConnectionListener(tag, object : CometChat.ConnectionListener {
            override fun onConnected() {
                refreshMessageHeader()
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

            override fun onConnectionError(e: CometChatException?) {
                // No action needed
            }
        })
    }

    /**
     * Adds UIKit local event listeners for UI-triggered events.
     * These events are emitted by other UI components (e.g., when a user is blocked).
     * Uses Flow-based event collection from CometChatEvents.
     */
    private fun addUIKitLocalEventListeners(tag: String) {
        // User block/unblock events
        userEventsJob = viewModelScope.launch {
            CometChatEvents.userEvents.collect { event ->
                when (event) {
                    is CometChatUserEvent.UserBlocked -> {
                        if (event.user.uid == currentId) {
                            _user.value = event.user
                            _uiState.value = MessageHeaderUIState.UserContent(event.user)
                        }
                    }
                    is CometChatUserEvent.UserUnblocked -> {
                        if (event.user.uid == currentId) {
                            _user.value = event.user
                            _uiState.value = MessageHeaderUIState.UserContent(event.user)
                        }
                    }
                }
            }
        }

        // Group events from other UI components
        groupEventsJob = viewModelScope.launch {
            CometChatEvents.groupEvents.collect { event ->
                when (event) {
                    is CometChatGroupEvent.MembersAdded -> {
                        if (event.group.guid == currentId) {
                            _group.value = event.group
                            _memberCount.value = event.group.membersCount
                            _uiState.value = MessageHeaderUIState.GroupContent(event.group)
                        }
                    }
                    is CometChatGroupEvent.MemberKicked -> {
                        if (event.group.guid == currentId) {
                            _group.value = event.group
                            _memberCount.value = event.group.membersCount
                            _uiState.value = MessageHeaderUIState.GroupContent(event.group)
                        }
                    }
                    is CometChatGroupEvent.MemberBanned -> {
                        if (event.group.guid == currentId) {
                            _group.value = event.group
                            _memberCount.value = event.group.membersCount
                            _uiState.value = MessageHeaderUIState.GroupContent(event.group)
                        }
                    }
                    is CometChatGroupEvent.OwnershipChanged -> {
                        if (event.group.guid == currentId) {
                            _group.value = event.group
                            _uiState.value = MessageHeaderUIState.GroupContent(event.group)
                        }
                    }
                    is CometChatGroupEvent.MemberJoined -> {
                        if (event.group.guid == currentId) {
                            _group.value = event.group
                            _memberCount.value = event.group.membersCount
                            _uiState.value = MessageHeaderUIState.GroupContent(event.group)
                        }
                    }
                    is CometChatGroupEvent.GroupLeft -> {
                        if (event.group.guid == currentId) {
                            _group.value = event.group
                            _memberCount.value = event.group.membersCount
                            _uiState.value = MessageHeaderUIState.GroupContent(event.group)
                        }
                    }
                    is CometChatGroupEvent.MemberUnbanned -> {
                        if (event.group.guid == currentId) {
                            _group.value = event.group
                            _memberCount.value = event.group.membersCount
                            _uiState.value = MessageHeaderUIState.GroupContent(event.group)
                        }
                    }
                    is CometChatGroupEvent.MemberScopeChanged -> {
                        if (event.group.guid == currentId) {
                            _group.value = event.group
                            _uiState.value = MessageHeaderUIState.GroupContent(event.group)
                        }
                    }
                    else -> {
                        // Other group events don't affect message header
                    }
                }
            }
        }
    }

    /**
     * Handles typing indicator events with debounce for typing end.
     * 
     * @param typingIndicator The typing indicator from the SDK
     * @param isTyping True if typing started, false if typing ended
     */
    private fun handleTypingIndicator(typingIndicator: TypingIndicator, isTyping: Boolean) {
        val matchesCurrentConversation = when {
            typingIndicator.receiverType == CometChatConstants.RECEIVER_TYPE_USER ->
                typingIndicator.sender.uid == currentId
            else -> typingIndicator.receiverId == currentId
        }

        if (matchesCurrentConversation) {
            if (isTyping) {
                // Cancel any pending debounce job
                typingDebounceJob?.cancel()
                _typingIndicator.value = typingIndicator
            } else {
                // Debounce typing end to prevent flickering
                typingDebounceJob?.cancel()
                typingDebounceJob = viewModelScope.launch {
                    delay(TYPING_INDICATOR_DEBOUNCER)
                    _typingIndicator.value = null
                }
            }
        }
    }

    /**
     * Refreshes the current message header data.
     * Called on reconnection to ensure data is up-to-date.
     */
    private fun refreshMessageHeader() {
        _user.value?.let { refreshUser(it.uid) }
        _group.value?.let { refreshGroup(it.guid) }
    }

    /**
     * Checks if a user is blocked (either blocked by me or has blocked me).
     * 
     * @param user The user to check, or null
     * @return True if the user is blocked in either direction, false otherwise
     */
    private fun isBlocked(user: User?): Boolean {
        return user?.let { it.isBlockedByMe || it.isHasBlockedMe } ?: false
    }

    /**
     * Removes all CometChat SDK listeners and cancels UIKit local event listener jobs.
     * Called when the ViewModel is cleared.
     */
    fun removeListeners() {
        listenersTag?.let { tag ->
            CometChat.removeUserListener(tag)
            CometChat.removeGroupListener(tag)
            CometChat.removeMessageListener(tag)
            CometChat.removeConnectionListener(tag)
        }
        
        // Cancel local event listener jobs
        userEventsJob?.cancel()
        groupEventsJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        typingDebounceJob?.cancel()
        removeListeners()
    }
}
