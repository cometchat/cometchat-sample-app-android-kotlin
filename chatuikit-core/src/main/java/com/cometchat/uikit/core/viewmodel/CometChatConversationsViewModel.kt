package com.cometchat.uikit.core.viewmodel

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.helpers.CometChatHelper
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants

import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatConversationEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.CometChatUserEvent
import com.cometchat.uikit.core.events.MessageStatus
import com.cometchat.uikit.core.state.DeleteState
import com.cometchat.uikit.core.state.UIState
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
 * ViewModel that holds UI state and calls use cases.
 * Does NOT fetch data directly - delegates to use cases.
 * 
 * Implements [ListOperations] interface for standardized list manipulation.
 * All list operation methods are open for client override.
 * 
 * @param getConversationListUseCase Use case for fetching conversations
 * @param deleteConversationUseCase Use case for deleting conversations
 * @param refreshConversationListUseCase Use case for refreshing the list
 * @param enableListeners Whether to enable CometChat listeners (set to false for testing)
 */
open class CometChatConversationsViewModel(
    private val getConversationListUseCase: GetConversationListUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val refreshConversationListUseCase: RefreshConversationListUseCase,
    private val enableListeners: Boolean = true
) : ViewModel(), ListOperations<Conversation> {
    
    // UI State
    private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    
    // Conversations list
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    
    // Fetching state - prevents concurrent fetches
    private var isFetching = false
    
    // Has more data flag - tracks if there are more conversations to fetch
    private var hasMoreData = true
    
    // List operations delegate - handles internal list manipulation
    private val listDelegate = ListOperationsDelegate(
        stateFlow = _conversations,
        equalityChecker = { a, b -> a.conversationId == b.conversationId }
    )
    
    // Typing indicators
    private val _typingIndicators = MutableStateFlow<Map<String, TypingIndicator>>(emptyMap())
    val typingIndicators: StateFlow<Map<String, TypingIndicator>> = _typingIndicators.asStateFlow()
    
    // Typing indicator debounce job
    private var typingDebounceJob: Job? = null
    private val typingIndicatorHashMap = mutableMapOf<String, TypingIndicator>()
    
    // Typing indicator debounce delay (1 second, matching Java implementation)
    private val TYPING_INDICATOR_DEBOUNCER = 1000L
    
    // Selection state
    private val _selectedConversations = MutableStateFlow<Set<Conversation>>(emptySet())
    val selectedConversations: StateFlow<Set<Conversation>> = _selectedConversations.asStateFlow()
    
    // Delete state
    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()
    
    // Sound playback event - emits true when a sound should be played
    private val _playSoundEvent = MutableSharedFlow<Boolean>()
    val playSoundEvent: SharedFlow<Boolean> = _playSoundEvent.asSharedFlow()
    
    // Scroll to top event - emits when list should scroll to top (new message received)
    private val _scrollToTopEvent = MutableSharedFlow<Unit>()
    val scrollToTopEvent: SharedFlow<Unit> = _scrollToTopEvent.asSharedFlow()
    
    // Configuration
    private var conversationsRequest: ConversationsRequest? = null
    private var conversationsRequestBuilder: ConversationsRequest.ConversationsRequestBuilder? = null
    private var disableReceipt = false
    private var disableSoundForMessages = false
    @RawRes private var customSoundForMessage: Int = 0
    private var listenersTag: String? = null
    private var soundManager: CometChatSoundManager? = null
    
    // Local event listener jobs
    private var conversationEventsJob: Job? = null
    private var groupEventsJob: Job? = null
    private var userEventsJob: Job? = null
    private var messageEventsJob: Job? = null
    private var callEventsJob: Job? = null
    
    init {
        if (enableListeners) {
            addListeners()
        }
        fetchConversations()
    }
    
    /**
     * Fetches conversations with pagination support.
     * Shows loading state only on initial fetch.
     * Uses client's request builder if set, otherwise creates a default one.
     * Prevents concurrent fetches using isFetching flag.
     */
    fun fetchConversations() {
        // Prevent concurrent fetches and don't fetch if no more data
        if (isFetching || !hasMoreData) return
        
        viewModelScope.launch {
            isFetching = true
            
            if (_conversations.value.isEmpty()) {
                _uiState.value = UIState.Loading
            }
            
            // Create request on first fetch, reuse for pagination
            // Use client's builder if provided, otherwise create default
            if (conversationsRequest == null) {
                val builder = conversationsRequestBuilder 
                    ?: ConversationsRequest.ConversationsRequestBuilder().setLimit(30)
                conversationsRequest = builder.build()
            }
            
            conversationsRequest?.let { request ->
                getConversationListUseCase(request)
                    .onSuccess { newConversations ->
                        // Only append if we got new conversations
                        if (newConversations.isNotEmpty()) {
                            val updatedList = _conversations.value + newConversations
                            _conversations.value = updatedList
                            _uiState.value = UIState.Content(updatedList)
                        } else {
                            // No new conversations means we've reached the end
                            hasMoreData = false
                            if (_conversations.value.isEmpty()) {
                                _uiState.value = UIState.Empty
                            }
                        }
                    }
                    .onFailure { exception ->
                        _uiState.value = UIState.Error(exception as CometChatException)
                    }
                
                isFetching = false
            } ?: run {
                isFetching = false
            }
        }
    }
    
    /**
     * Refreshes the conversation list from the beginning.
     * Clears existing data and fetches fresh.
     * This is a silent refresh - it does not show loading shimmer.
     * Uses client's request builder if set, otherwise creates a default one.
     */
    fun refreshList() {
        // Reset pagination state for fresh fetch
        hasMoreData = true
        isFetching = false
        
        viewModelScope.launch {
            // Silent refresh - don't show loading state, keep existing data visible
            // Only show loading if list is empty
            if (_conversations.value.isEmpty()) {
                _uiState.value = UIState.Loading
            }
            
            // Use client's builder if provided, otherwise create default
            val builder = conversationsRequestBuilder 
                ?: ConversationsRequest.ConversationsRequestBuilder().setLimit(30)
            
            refreshConversationListUseCase(builder)
                .onSuccess { conversations ->
                    _conversations.value = conversations
                    _uiState.value = if (conversations.isEmpty()) {
                        UIState.Empty
                    } else {
                        UIState.Content(conversations)
                    }
                    
                    // Create new request for pagination
                    conversationsRequest = builder.build()
                    
                    // Emit scroll to top event after refresh (new conversation at top)
                    _scrollToTopEvent.emit(Unit)
                }
                .onFailure { exception ->
                    // Only show error if list is empty, otherwise keep existing data
                    if (_conversations.value.isEmpty()) {
                        _uiState.value = UIState.Error(exception as CometChatException)
                    }
                }
        }
    }
    
    /**
     * Deletes a conversation.
     * Updates delete state throughout the operation.
     */
    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.InProgress
            
            deleteConversationUseCase(conversation)
                .onSuccess {
                    _conversations.value = _conversations.value.filter { 
                        it.conversationId != conversation.conversationId 
                    }
                    _deleteState.value = DeleteState.Success
                    
                    if (_conversations.value.isEmpty()) {
                        _uiState.value = UIState.Empty
                    }
                }
                .onFailure { exception ->
                    _deleteState.value = DeleteState.Failure(exception as CometChatException)
                }
        }
    }
    
    /**
     * Resets delete state to idle.
     * Call after handling delete success/failure.
     */
    fun resetDeleteState() {
        _deleteState.value = DeleteState.Idle
    }
    
    /**
     * Checks if a message should increment the unread count.
     * For CustomMessage, checks metadata for `incrementUnreadCount` boolean,
     * falling back to `willUpdateConversation()` method.
     * 
     * @param message The message to check
     * @return true if unread count should be incremented
     */
    open fun willUpdateIncrementUnreadCount(message: BaseMessage): Boolean {
        if (message is CustomMessage) {
            val metadata = message.metadata
            if (metadata != null && metadata.has("incrementUnreadCount")) {
                return try {
                    metadata.getBoolean("incrementUnreadCount")
                } catch (e: Exception) {
                    false
                }
            }
            return message.willUpdateConversation()
        }
        return false
    }
    
    /**
     * Determines if a custom message should update the conversation.
     * Returns true if the message should increment unread count OR if settings allow custom messages.
     * 
     * @param message The custom message to check
     * @return true if the conversation should be updated
     */
    open fun shouldUpdateConversationForCustomMessage(message: CustomMessage): Boolean {
        return willUpdateIncrementUnreadCount(message) || 
            CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCustomMessages()
    }
    
    /**
     * Checks if a message is a threaded reply.
     * A message is threaded if it has a parentMessageId > 0.
     * 
     * @param message The message to check
     * @return true if the message is a threaded reply
     */
    open fun isThreadedMessage(message: BaseMessage): Boolean {
        return message.parentMessageId > 0
    }
    
    /**
     * Checks if a conversation should be added to the list based on the conversation type filter.
     * Returns true if:
     * - No filter is set (conversationsRequest is null or conversationType is null)
     * - Filter is set to BOTH
     * - Conversation type matches the filter
     * 
     * @param conversation The conversation to check
     * @return true if the conversation should be added to the list
     */
    private fun isAddToConversationList(conversation: Conversation?): Boolean {
        if (conversation == null) return false
        
        val request = conversationsRequest ?: return true
        val filterType = request.conversationType ?: return true
        
        if (filterType.equals(UIKitConstants.ConversationType.BOTH, ignoreCase = true)) {
            return true
        }
        
        return conversation.conversationType.equals(filterType, ignoreCase = true)
    }
    
    /**
     * Selects or deselects a conversation based on selection mode.
     */
    fun selectConversation(conversation: Conversation, mode: UIKitConstants.SelectionMode) {
        when (mode) {
            UIKitConstants.SelectionMode.SINGLE -> {
                _selectedConversations.value = setOf(conversation)
            }
            UIKitConstants.SelectionMode.MULTIPLE -> {
                val current = _selectedConversations.value.toMutableSet()
                if (current.contains(conversation)) {
                    current.remove(conversation)
                } else {
                    current.add(conversation)
                }
                _selectedConversations.value = current
            }
            UIKitConstants.SelectionMode.NONE -> {
                // Do nothing
            }
        }
    }
    
    /**
     * Clears all selected conversations.
     */
    fun clearSelection() {
        _selectedConversations.value = emptySet()
    }
    
    /**
     * Returns the list of currently selected conversations.
     */
    fun getSelectedConversations(): List<Conversation> = 
        _selectedConversations.value.toList()
    
    /**
     * Sets the conversations request builder for customizing fetch parameters.
     * This will store the builder and reset the current request to use the new configuration.
     * The builder will be used for both initial fetch and refresh operations.
     *
     * @param builder The custom request builder provided by the client
     */
    fun setConversationsRequestBuilder(
        builder: ConversationsRequest.ConversationsRequestBuilder
    ) {
        conversationsRequestBuilder = builder
        conversationsRequest = builder.build()
    }
    
    /**
     * Sets whether to disable read receipts.
     */
    fun setDisableReceipt(disable: Boolean) {
        disableReceipt = disable
    }
    
    /**
     * Sets whether to disable sound for incoming messages.
     */
    fun setDisableSoundForMessages(disable: Boolean) {
        disableSoundForMessages = disable
    }
    
    /**
     * Sets a custom sound resource for incoming messages.
     * Pass 0 to use the default sound.
     *
     * @param rawRes The raw resource ID of the custom sound.
     */
    fun setCustomSoundForMessage(@RawRes rawRes: Int) {
        customSoundForMessage = rawRes
    }
    
    /**
     * Initializes the sound manager with the given context.
     * Must be called before sounds can be played.
     *
     * @param context The application context.
     */
    fun initSoundManager(context: Context) {
        if (soundManager == null) {
            soundManager = CometChatSoundManager(context.applicationContext)
        }
    }
    
    /**
     * Plays the incoming message sound if sound is not disabled.
     */
    private fun playIncomingMessageSound() {
        if (!disableSoundForMessages) {
            viewModelScope.launch {
                _playSoundEvent.emit(true)
            }
            soundManager?.play(Sound.INCOMING_MESSAGE_FROM_OTHER, customSoundForMessage)
        }
    }

    
    /**
     * Adds CometChat listeners for real-time updates.
     */
    private fun addListeners() {
        listenersTag = "ConversationList_${System.currentTimeMillis()}"
        
        listenersTag?.let { tag ->
            // Message listener for real-time message updates
            CometChat.addMessageListener(tag, object : CometChat.MessageListener() {
                override fun onTextMessageReceived(message: TextMessage) {
                    checkAndUpdateConversation(message, true)
                }
                
                override fun onMediaMessageReceived(message: MediaMessage) {
                    checkAndUpdateConversation(message, true)
                }
                
                override fun onCustomMessageReceived(message: CustomMessage) {
                    checkAndUpdateConversation(message, true)
                }
                
                override fun onMessageEdited(message: BaseMessage) {
                    val conversation = CometChatHelper.getConversationFromMessage(message)
                    if (conversation != null) {
                        updateConversation(conversation, isActionMessage = false)
                    }
                }
                
                override fun onMessageDeleted(message: BaseMessage) {
                    val conversation = CometChatHelper.getConversationFromMessage(message)
                    if (conversation != null) {
                        updateConversation(conversation, isActionMessage = false)
                    }
                }
                
                override fun onMessagesDelivered(messageReceipt: MessageReceipt) {
                    if (!disableReceipt) {
                        updateDeliveredReceipts(messageReceipt)
                    }
                }
                
                override fun onMessagesRead(messageReceipt: MessageReceipt) {
                    if (!disableReceipt) {
                        updateReadReceipts(messageReceipt)
                    }
                }
                
                override fun onTypingStarted(typingIndicator: TypingIndicator) {
                    addTypingIndicator(typingIndicator)
                }
                
                override fun onTypingEnded(typingIndicator: TypingIndicator) {
                    removeTypingIndicator(typingIndicator)
                }
            })
            
            // User listener for online/offline status
            CometChat.addUserListener(tag, object : CometChat.UserListener() {
                override fun onUserOnline(user: User) {
                    updateUserStatus(user)
                }
                
                override fun onUserOffline(user: User) {
                    updateUserStatus(user)
                }
            })
            
            // Group listener for group updates
            CometChat.addGroupListener(tag, object : CometChat.GroupListener() {
                override fun onGroupMemberJoined(
                    action: Action,
                    joinedUser: User,
                    joinedGroup: Group
                ) {
                    updateConversationForGroupAction(action, false)
                }
                
                override fun onGroupMemberLeft(
                    action: Action,
                    leftUser: User,
                    leftGroup: Group
                ) {
                    val isCurrentUser = isCurrentUser(leftUser)
                    updateConversationForGroupAction(action, isCurrentUser)
                }
                
                override fun onGroupMemberKicked(
                    action: Action,
                    kickedUser: User,
                    kickedBy: User,
                    kickedFrom: Group
                ) {
                    val isCurrentUser = isCurrentUser(kickedUser)
                    updateConversationForGroupAction(action, isCurrentUser)
                }
                
                override fun onMemberAddedToGroup(
                    action: Action,
                    addedby: User,
                    userAdded: User,
                    addedTo: Group
                ) {
                    // If current user was added, update the group scope
                    if (isCurrentUser(userAdded)) {
                        addedTo.scope = CometChatConstants.SCOPE_PARTICIPANT
                        addedTo.setHasJoined(true)
                        action.actionFor = addedTo
                    } else {
                        // Get existing group from conversation if available
                        val existingGroup = getGroupFromConversation(addedTo.guid)
                        if (existingGroup != null) {
                            action.actionFor = existingGroup
                        } else {
                            addedTo.setHasJoined(true)
                            addedTo.scope = CometChatConstants.SCOPE_PARTICIPANT
                            action.actionFor = addedTo
                        }
                    }
                    updateConversationForGroupAction(action, false)
                }

                override fun onGroupMemberScopeChanged(
                    action: Action?,
                    updatedBy: User?,
                    updatedUser: User?,
                    scopeChangedTo: String?,
                    scopeChangedFrom: String?,
                    group: Group?
                ) {
                    if (action != null && updatedUser != null && group != null && scopeChangedTo != null) {
                        // If current user's scope changed, update the group
                        if (isCurrentUser(updatedUser)) {
                            group.scope = scopeChangedTo
                            group.setHasJoined(true)
                            updateGroupInConversation(group)
                        }
                        updateConversationForGroupAction(action, false)
                    }
                }

                override fun onGroupMemberBanned(
                    action: Action?,
                    bannedUser: User?,
                    bannedBy: User?,
                    group: Group?
                ) {
                    if (action != null && bannedUser != null) {
                        val isCurrentUser = isCurrentUser(bannedUser)
                        updateConversationForGroupAction(action, isCurrentUser)
                    }
                }
            })
            
            // Connection listener for reconnection
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
            
            // Call listener for call events (SDK listener for server-pushed events)
            CometChat.addCallListener(tag, object : CometChat.CallListener() {
                override fun onIncomingCallReceived(call: Call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities()) {
                        updateConversationWithCall(call)
                    }
                }
                
                override fun onOutgoingCallAccepted(call: Call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities()) {
                        updateConversationWithCall(call)
                    }
                }
                
                override fun onOutgoingCallRejected(call: Call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities()) {
                        updateConversationWithCall(call)
                    }
                }
                
                override fun onIncomingCallCancelled(call: Call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities()) {
                        updateConversationWithCall(call)
                    }
                }
                
                override fun onCallEndedMessageReceived(call: Call) {
                    if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities()) {
                        updateConversationWithCall(call)
                    }
                }
            })
        }
        
        // Add local event listeners
        addLocalEventListeners()
    }
    
    /**
     * Adds local event listeners for UI-triggered events.
     * These events are emitted by other UI components (e.g., when a message is sent from MessageComposer).
     */
    private fun addLocalEventListeners() {
        // Conversation events (e.g., conversation deleted from another screen)
        conversationEventsJob = viewModelScope.launch {
            CometChatEvents.conversationEvents.collect { event ->
                when (event) {
                    is CometChatConversationEvent.ConversationDeleted -> {
                        removeConversation(event.conversation)
                    }
                    is CometChatConversationEvent.ConversationUpdated -> {
                        updateConversationInList(event.conversation)
                    }
                }
            }
        }
        
        // Group events (e.g., group deleted, user left group from another screen)
        groupEventsJob = viewModelScope.launch {
            CometChatEvents.groupEvents.collect { event ->
                when (event) {
                    is CometChatGroupEvent.GroupDeleted -> {
                        removeGroup(event.group)
                    }
                    is CometChatGroupEvent.GroupLeft -> {
                        removeGroup(event.group)
                    }
                    is CometChatGroupEvent.MemberJoined -> {
                        updateGroupInConversation(event.group)
                    }
                    is CometChatGroupEvent.MembersAdded -> {
                        updateGroupInConversation(event.group)
                        // Update conversation for each action message
                        event.actions.forEach { action ->
                            updateConversationForGroupAction(action, false)
                        }
                    }
                    is CometChatGroupEvent.MemberKicked -> {
                        updateGroupInConversation(event.group)
                        updateConversationForGroupAction(event.action, false)
                    }
                    is CometChatGroupEvent.MemberBanned -> {
                        updateGroupInConversation(event.group)
                        updateConversationForGroupAction(event.action, false)
                    }
                    is CometChatGroupEvent.MemberUnbanned -> {
                        updateGroupInConversation(event.group)
                        updateConversationForGroupAction(event.action, false)
                    }
                    is CometChatGroupEvent.MemberScopeChanged -> {
                        updateGroupInConversation(event.group)
                        updateConversationForGroupAction(event.action, false)
                    }
                    is CometChatGroupEvent.OwnershipChanged -> {
                        updateGroupInConversation(event.group)
                    }
                    is CometChatGroupEvent.GroupCreated -> {
                        // Refresh list to show new group conversation
                        refreshList()
                    }
                }
            }
        }
        
        // User events (e.g., user blocked/unblocked from another screen)
        userEventsJob = viewModelScope.launch {
            CometChatEvents.userEvents.collect { event ->
                when (event) {
                    is CometChatUserEvent.UserBlocked -> {
                        // Remove user conversation if blocked users are not included
                        // Note: This depends on conversationsRequest configuration
                        removeUser(event.user)
                    }
                    is CometChatUserEvent.UserUnblocked -> {
                        // Update user status in conversation
                        updateUserStatus(event.user)
                    }
                }
            }
        }
        
        // Message events (e.g., message sent from MessageComposer)
        messageEventsJob = viewModelScope.launch {
            Log.d("CometChatConvListVM", "Started collecting messageEvents")
            CometChatEvents.messageEvents.collect { event ->
                Log.d("CometChatConvListVM", "Received messageEvent: ${event::class.simpleName}")
                when (event) {
                    is CometChatMessageEvent.MessageSent -> {
                        if (event.status == MessageStatus.SUCCESS) {
                            checkAndUpdateConversation(event.message, false)
                        }
                    }
                    is CometChatMessageEvent.MessageEdited -> {
                        if (event.status == MessageStatus.SUCCESS) {
                            checkAndUpdateConversation(event.message, false)
                        }
                    }
                    is CometChatMessageEvent.MessageDeleted -> {
                        checkAndUpdateConversation(event.message, false)
                    }
                    is CometChatMessageEvent.MessageRead -> {
                        Log.d("CometChatConvListVM", "Received MessageRead event - conversationId=${event.message.conversationId}, messageId=${event.message.id}")
                        clearUnreadCountForMessage(event.message)
                    }
                    is CometChatMessageEvent.TextMessageReceived -> {
                        checkAndUpdateConversation(event.message, true)
                    }
                    is CometChatMessageEvent.MediaMessageReceived -> {
                        checkAndUpdateConversation(event.message, true)
                    }
                    is CometChatMessageEvent.CustomMessageReceived -> {
                        checkAndUpdateConversation(event.message, true)
                    }
                    else -> {
                        // Other message events don't affect conversation list
                    }
                }
            }
        }
        
        // Call events (e.g., outgoing call initiated from CallButton)
        // These are UIKit local events for UI-initiated call actions
        callEventsJob = viewModelScope.launch {
            CometChatEvents.callEvents.collect { event ->
                if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnCallActivities()) {
                    when (event) {
                        is CometChatCallEvent.OutgoingCall -> updateConversationWithCall(event.call)
                        is CometChatCallEvent.CallAccepted -> updateConversationWithCall(event.call)
                        is CometChatCallEvent.CallRejected -> updateConversationWithCall(event.call)
                        is CometChatCallEvent.CallEnded -> updateConversationWithCall(event.call)
                    }
                }
            }
        }
    }
    
    /**
     * Removes all CometChat listeners.
     */
    private fun removeListeners() {
        listenersTag?.let { tag ->
            CometChat.removeMessageListener(tag)
            CometChat.removeUserListener(tag)
            CometChat.removeGroupListener(tag)
            CometChat.removeConnectionListener(tag)
            CometChat.removeCallListener(tag)
        }
    }
    

    
    /**
     * Updates conversation receipt status for delivered receipts.
     * Matches the Java implementation's updateDeliveredReceipts() method logic.
     * 
     * For USER conversations: matches by receipt.sender.uid == conversationWith.uid
     * For GROUP conversations: matches by receipt.receiverId == conversationWith.guid AND DELIVERED_TO_ALL type
     * 
     * @param receipt The message receipt containing delivery information
     */
    private fun updateDeliveredReceipts(receipt: MessageReceipt) {
        viewModelScope.launch {
            _conversations.value = _conversations.value.map { conversation ->
                val lastMessage = conversation.lastMessage
                
                // Skip if no last message, already delivered, or message ID doesn't match
                if (lastMessage == null || 
                    lastMessage.deliveredAt != 0L || 
                    lastMessage.id != receipt.messageId) {
                    return@map conversation
                }
                
                val shouldUpdate = when (receipt.receiverType) {
                    UIKitConstants.ReceiverType.USER -> {
                        // For user conversations: match by sender UID
                        conversation.conversationType == CometChatConstants.RECEIVER_TYPE_USER &&
                            receipt.sender?.uid == (conversation.conversationWith as? User)?.uid
                    }
                    UIKitConstants.ReceiverType.GROUP -> {
                        // For group conversations: match by receiver ID and DELIVERED_TO_ALL type
                        conversation.conversationType == CometChatConstants.RECEIVER_TYPE_GROUP &&
                            receipt.receiptType == MessageReceipt.RECEIPT_TYPE_DELIVERED_TO_ALL &&
                            receipt.receiverId == (conversation.conversationWith as? Group)?.guid
                    }
                    else -> false
                }
                
                if (shouldUpdate) {
                    // Clone to create new reference for Compose recomposition
                    conversation.clone().apply {
                        this.lastMessage = lastMessage.apply {
                            deliveredAt = receipt.deliveredAt
                        }
                    }
                } else {
                    conversation
                }
            }
        }
    }

    /**
     * Updates conversation receipt status for read receipts.
     * Matches the Java implementation's updateReadReceipts() method logic.
     * 
     * For USER conversations: matches by receipt.sender.uid == conversationWith.uid
     * For GROUP conversations: matches by receipt.receiverId == conversationWith.guid AND READ_BY_ALL type
     * 
     * Also clears unread count when the receipt is from the logged-in user.
     * 
     * @param receipt The message receipt containing read information
     */
    private fun updateReadReceipts(receipt: MessageReceipt) {
        viewModelScope.launch {
            val loggedInUser = getLoggedInUserSafe()
            val isReceiptFromLoggedInUser = loggedInUser != null && 
                receipt.sender?.uid?.equals(loggedInUser.uid, ignoreCase = true) == true
            
            _conversations.value = _conversations.value.map { conversation ->
                val lastMessage = conversation.lastMessage
                
                when (receipt.receiverType) {
                    UIKitConstants.ReceiverType.USER -> {
                        // For user conversations: match by sender UID
                        if (conversation.conversationType == CometChatConstants.RECEIVER_TYPE_USER &&
                            receipt.sender?.uid == (conversation.conversationWith as? User)?.uid) {
                            
                            // Check if we should update readAt timestamp
                            if (lastMessage != null && 
                                lastMessage.readAt == 0L && 
                                lastMessage.id == receipt.messageId) {
                                // Clone to create new reference for Compose recomposition
                                conversation.clone().apply {
                                    this.lastMessage = lastMessage.apply {
                                        readAt = receipt.readAt
                                    }
                                }
                            } else if (isReceiptFromLoggedInUser) {
                                // Clear unread count when receipt is from logged-in user
                                conversation.clone().apply {
                                    unreadMessageCount = 0
                                }
                            } else {
                                conversation
                            }
                        } else {
                            conversation
                        }
                    }
                    UIKitConstants.ReceiverType.GROUP -> {
                        // For group conversations: match by receiver ID and READ_BY_ALL type
                        if (conversation.conversationType == CometChatConstants.RECEIVER_TYPE_GROUP &&
                            receipt.receiptType == MessageReceipt.RECEIPT_TYPE_READ_BY_ALL &&
                            receipt.receiverId == (conversation.conversationWith as? Group)?.guid) {
                            
                            // Check if we should update readAt timestamp
                            if (lastMessage != null && 
                                lastMessage.readAt == 0L && 
                                lastMessage.id == receipt.messageId) {
                                // Clone to create new reference for Compose recomposition
                                conversation.clone().apply {
                                    this.lastMessage = lastMessage.apply {
                                        readAt = receipt.readAt
                                    }
                                }
                            } else {
                                conversation
                            }
                        } else if (isReceiptFromLoggedInUser) {
                            // Clear unread count when receipt is from logged-in user (for group conversations)
                            conversation.clone().apply {
                                unreadMessageCount = 0
                            }
                        } else {
                            conversation
                        }
                    }
                    else -> conversation
                }
            }
        }
    }

    /**
     * Updates conversation receipt status (legacy method - kept for compatibility).
     */
    private fun updateConversationReceipt(receipt: MessageReceipt) {
        viewModelScope.launch {
            _conversations.value = _conversations.value.map { conversation ->
                if (conversation.lastMessage?.id == receipt.messageId) {
                    // Clone to create new reference for Compose recomposition
                    conversation.clone()
                } else {
                    conversation
                }
            }
        }
    }
    
    /**
     * Updates unread count for a conversation.
     */
    private fun updateConversationUnreadCount(conversation: Conversation, count: Int) {
        Log.d("CometChatConvListVM", "updateConversationUnreadCount() - conversationId=${conversation.conversationId}, newCount=$count")
        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversation.conversationId) {
                // Clone to create new reference for Compose recomposition
                Log.d("CometChatConvListVM", "updateConversationUnreadCount() - Updated conversation ${it.conversationId} unreadCount from ${it.unreadMessageCount} to $count")
                it.clone().apply { unreadMessageCount = count }
            } else {
                it
            }
        }
    }
    
    /**
     * Updates a conversation in the list without moving it to top.
     * Used for updating conversation properties like unread count from external events.
     * 
     * @param conversation The conversation with updated properties
     */
    private fun updateConversationInList(conversation: Conversation) {
        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversation.conversationId) {
                // Clone and update unread count (matching Java behavior)
                it.clone().apply { 
                    unreadMessageCount = conversation.unreadMessageCount 
                }
            } else {
                it
            }
        }
    }
    
    /**
     * Adds a typing indicator and updates the conversation's isReceiverTyping property.
     * Updates immediately without debouncing.
     */
    private fun addTypingIndicator(typingIndicator: TypingIndicator) {
        val key = getTypingIndicatorKey(typingIndicator)
        typingIndicatorHashMap[key] = typingIndicator
        // Update immediately when typing starts
        _typingIndicators.value = typingIndicatorHashMap.toMap()
    }
    
    /**
     * Removes a typing indicator and updates the conversation's isReceiverTyping property.
     * Uses debouncing to prevent flickering when multiple users are typing.
     */
    private fun removeTypingIndicator(typingIndicator: TypingIndicator) {
        val key = getTypingIndicatorKey(typingIndicator)
        typingIndicatorHashMap.remove(key)
        
        // Cancel any pending debounce job
        typingDebounceJob?.cancel()
        
        // Debounce the update when typing ends to prevent flickering
        typingDebounceJob = viewModelScope.launch {
            delay(TYPING_INDICATOR_DEBOUNCER)
            _typingIndicators.value = typingIndicatorHashMap.toMap()
        }
    }
    
    /**
     * Gets the entity ID (user UID or group GUID) from a conversation.
     */
    private fun getConversationEntityId(conversation: Conversation): String? {
        return when (conversation.conversationType) {
            UIKitConstants.ConversationType.USERS -> (conversation.conversationWith as? User)?.uid
            UIKitConstants.ConversationType.GROUPS -> (conversation.conversationWith as? Group)?.guid
            else -> null
        }
    }
    
    /**
     * Gets a unique key for a typing indicator.
     */
    private fun getTypingIndicatorKey(typingIndicator: TypingIndicator): String {
        return "${typingIndicator.receiverType}_${typingIndicator.receiverId}_${typingIndicator.sender.uid}"
    }
    
    /**
     * Updates user status in conversations.
     */
    private fun updateUserStatus(user: User) {
        viewModelScope.launch {
            _conversations.value = _conversations.value.map { conversation ->
                if (conversation.conversationType == UIKitConstants.ConversationType.USERS) {
                    val conversationUser = conversation.conversationWith as? User
                    if (conversationUser?.uid == user.uid) {
                        // Clone to create new reference for Compose recomposition
                        conversation.clone().apply { 
                            conversationWith = user 
                        }
                    } else {
                        conversation
                    }
                } else {
                    conversation
                }
            }
        }
    }
    
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
     * Checks if the given user is the current logged-in user.
     */
    private fun isCurrentUser(user: User?): Boolean {
        val loggedInUser = getLoggedInUserSafe() ?: return false
        return user?.uid?.equals(loggedInUser.uid, ignoreCase = true) == true
    }
    
    /**
     * Gets a group from the conversation list by GUID.
     */
    private fun getGroupFromConversation(guid: String): Group? {
        for (conversation in _conversations.value) {
            if (conversation.conversationType == UIKitConstants.ConversationType.GROUPS) {
                val group = conversation.conversationWith as? Group
                if (group?.guid == guid) {
                    return group
                }
            }
        }
        return null
    }
    
    /**
     * Updates conversation for group actions (member joined, left, kicked, banned, etc.).
     * Converts the action to a conversation and updates the list.
     * 
     * @param action The group action message
     * @param isRemove Whether to remove the conversation (e.g., current user left/kicked/banned)
     */
    private fun updateConversationForGroupAction(action: Action, isRemove: Boolean) {
        viewModelScope.launch {
            // Null safety check - ensure action has required fields before calling SDK
            // The SDK's CometChatHelper.getConversationFromMessage can crash if receiverType is null
            if (action.receiverType.isNullOrEmpty() || action.receiverUid.isNullOrEmpty()) {
                return@launch
            }
            
            val conversation = try {
                CometChatHelper.getConversationFromMessage(action)
            } catch (e: Exception) {
                Log.e("CometChatConvListVM", "Error getting conversation from action: ${e.message}")
                null
            } ?: return@launch
            
            if (isRemove) {
                removeConversation(conversation)
            } else {
                // Only update if settings allow group actions
                if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnGroupActions()) {
                    updateConversation(conversation, isActionMessage = true)
                }
            }
        }
    }
    
    /**
     * Updates conversation with a call event.
     * Converts the call to a conversation and updates the list.
     * 
     * @param call The call to update the conversation with
     */
    private fun updateConversationWithCall(call: Call) {
        viewModelScope.launch {
            // Null safety check - ensure call has required fields before calling SDK
            if (call.receiverType.isNullOrEmpty() || call.receiverUid.isNullOrEmpty()) {
                Log.w("CometChatConvListVM", "Skipping updateConversationWithCall: call has null receiverType or receiverUid")
                return@launch
            }
            
            val conversation = try {
                CometChatHelper.getConversationFromMessage(call)
            } catch (e: Exception) {
                Log.e("CometChatConvListVM", "Error getting conversation from call: ${e.message}")
                null
            } ?: return@launch
            
            updateConversation(conversation, isActionMessage = false)
        }
    }
    
    /**
     * Removes a conversation from the list.
     */
    private fun removeConversation(conversation: Conversation) {
        val currentList = _conversations.value
        val newList = currentList.filter { it.conversationId != conversation.conversationId }
        _conversations.value = newList
        
        if (newList.isEmpty()) {
            _uiState.value = UIState.Empty
        } else {
            _uiState.value = UIState.Content(newList)
        }
    }
    
    /**
     * Removes a group conversation from the list.
     */
    private fun removeGroup(group: Group) {
        val currentList = _conversations.value
        val newList = currentList.filter { conversation ->
            if (conversation.conversationType == UIKitConstants.ConversationType.GROUPS) {
                val conversationGroup = conversation.conversationWith as? Group
                conversationGroup?.guid != group.guid
            } else {
                true
            }
        }
        _conversations.value = newList
        
        if (newList.isEmpty()) {
            _uiState.value = UIState.Empty
        } else {
            _uiState.value = UIState.Content(newList)
        }
    }
    
    /**
     * Removes a user conversation from the list.
     */
    private fun removeUser(user: User) {
        val currentList = _conversations.value
        val newList = currentList.filter { conversation ->
            if (conversation.conversationType == UIKitConstants.ConversationType.USERS) {
                val conversationUser = conversation.conversationWith as? User
                conversationUser?.uid != user.uid
            } else {
                true
            }
        }
        _conversations.value = newList
        
        if (newList.isEmpty()) {
            _uiState.value = UIState.Empty
        } else {
            _uiState.value = UIState.Content(newList)
        }
    }
    
    /**
     * Checks if the message should update the conversation based on threading and settings.
     * For threaded messages, checks shouldUpdateOnMessageReplies() setting.
     * Matches the Java implementation's checkAndUpdateConversation() method.
     * 
     * @param message The message to check
     * @param markAsDeliver Whether to mark the message as delivered (true for incoming messages)
     */
    private fun checkAndUpdateConversation(message: BaseMessage, markAsDeliver: Boolean) {
        if (isThreadedMessage(message)) {
            if (CometChatUIKit.getConversationUpdateSettings().shouldUpdateOnMessageReplies()) {
                handleMessageUpdate(message, markAsDeliver)
            }
        } else {
            handleMessageUpdate(message, markAsDeliver)
        }
    }
    
    /**
     * Handles message update with custom message filtering.
     * For CustomMessage, checks shouldUpdateConversationForCustomMessage().
     * Matches the Java implementation's handleMessageUpdate() method.
     * 
     * @param message The message to update
     * @param markAsDeliver Whether to mark the message as delivered
     */
    private fun handleMessageUpdate(message: BaseMessage, markAsDeliver: Boolean) {
        if (message is CustomMessage) {
            if (shouldUpdateConversationForCustomMessage(message)) {
                updateMessageDeliveryStatus(message, markAsDeliver)
            }
        } else {
            updateMessageDeliveryStatus(message, markAsDeliver)
        }
    }
    
    /**
     * Updates the conversation with the message and optionally marks as delivered.
     * Matches the Java implementation's updateMessageDeliveryStatus() method.
     * 
     * @param message The message to update
     * @param markAsDeliver Whether to mark the message as delivered and play sound
     */
    private fun updateMessageDeliveryStatus(message: BaseMessage, markAsDeliver: Boolean) {
        if (markAsDeliver) {
            processMessage(message)
        } else {
            val conversation = CometChatHelper.getConversationFromMessage(message)
            if (conversation != null) {
                updateConversation(conversation, isActionMessage = false)
            }
        }
    }
    
    /**
     * Processes an incoming message by marking it as delivered and updating the conversation.
     * Matches the Java implementation's processMessage() method.
     * 
     * @param message The message to process
     */
    private fun processMessage(message: BaseMessage) {
        markAsDeliverInternally(message)
        val conversation = CometChatHelper.getConversationFromMessage(message)
        if (conversation != null) {
            updateConversation(conversation, isActionMessage = false)
        }
        playIncomingMessageSound()
    }
    
    /**
     * Marks a message as delivered internally if conditions are met.
     * Only marks as delivered if:
     * - The message sender is not the logged-in user
     * - Receipts are not disabled
     * Matches the Java implementation's markAsDeliverInternally() method.
     * 
     * @param message The message to mark as delivered
     */
    private fun markAsDeliverInternally(message: BaseMessage) {
        val loggedInUser = getLoggedInUserSafe() ?: return
        val senderUid = message.sender?.uid ?: return
        
        if (!senderUid.equals(loggedInUser.uid, ignoreCase = true) && !disableReceipt) {
            CometChat.markAsDelivered(message)
        }
    }
    
    /**
     * Clears unread count for a conversation based on a message.
     */
    private fun clearUnreadCountForMessage(message: BaseMessage) {
        val conversationId = message.conversationId
        val currentList = _conversations.value
        
        Log.d("CometChatConvListVM", "clearUnreadCountForMessage() - conversationId=$conversationId, currentListSize=${currentList.size}")
        
        val conversation = currentList.find { it.conversationId == conversationId }
        if (conversation != null) {
            Log.d("CometChatConvListVM", "clearUnreadCountForMessage() - Found conversation, current unreadCount=${conversation.unreadMessageCount}, updating to 0")
            updateConversationUnreadCount(conversation, 0)
        } else {
            Log.d("CometChatConvListVM", "clearUnreadCountForMessage() - Conversation NOT found in list. Available conversationIds: ${currentList.map { it.conversationId }}")
        }
    }
    
    /**
     * Updates a conversation in the list with proper handling of last message and unread count.
     * Matches the Java implementation's update() method logic.
     * 
     * @param conversation The conversation to update
     * @param isActionMessage Whether this is an action message (group action)
     */
    private fun updateConversation(conversation: Conversation, isActionMessage: Boolean) {
        if (conversation.lastMessage == null) return
        
        val currentList = _conversations.value
        val existingIndex = currentList.indexOfFirst { it.conversationId == conversation.conversationId }
        
        if (existingIndex >= 0) {
            val oldConversation = currentList[existingIndex]
            val loggedInUser = getLoggedInUserSafe()
            val lastMessage = conversation.lastMessage
            val isSentByMe = loggedInUser != null && 
                lastMessage?.sender?.uid?.equals(loggedInUser.uid, ignoreCase = true) == true
            
            // Clone the conversation to create a new reference for Compose recomposition
            val updatedConversation = conversation.clone()
            
            // Preserve the conversationWith from old conversation (it has more complete data)
            updatedConversation.conversationWith = oldConversation.conversationWith
            
            if (isActionMessage) {
                // For action messages, preserve the unread count
                updatedConversation.unreadMessageCount = oldConversation.unreadMessageCount
            } else if (isSentByMe) {
                // For messages sent by current user, preserve unread count
                updatedConversation.unreadMessageCount = oldConversation.unreadMessageCount
            } else {
                // For messages from others, check if it's a new message
                val isNewMessage = oldConversation.lastMessage?.id != lastMessage?.id
                val isNotRead = lastMessage?.readAt == 0L
                
                if (isNewMessage && isNotRead) {
                    // Increment unread count for new unread messages
                    updatedConversation.unreadMessageCount = oldConversation.unreadMessageCount + 1
                } else {
                    updatedConversation.unreadMessageCount = oldConversation.unreadMessageCount
                }
            }
            
            // Move updated conversation to top efficiently
            val newList = currentList.toMutableList().apply {
                removeAt(existingIndex)
                add(0, updatedConversation)
            }
            
            _conversations.value = newList
            _uiState.value = UIState.Content(newList)
            
            // Emit scroll to top event when conversation moves to top
            viewModelScope.launch {
                _scrollToTopEvent.emit(Unit)
            }
        } else {
            // Conversation not in list, check if it should be added based on filter
            if (isAddToConversationList(conversation)) {
                val updatedConversation = conversation.clone()
                val loggedInUser = getLoggedInUserSafe()
                val lastMessage = conversation.lastMessage
                val isSentByMe = loggedInUser != null && 
                    lastMessage?.sender?.uid?.equals(loggedInUser.uid, ignoreCase = true) == true
                
                // Set unread count to 1 for new conversations from others (not action messages)
                if (!isSentByMe && !isActionMessage && lastMessage !is Action) {
                    updatedConversation.unreadMessageCount = 1
                }
                
                val newList = buildList {
                    add(updatedConversation)
                    addAll(currentList)
                }
                
                _conversations.value = newList
                _uiState.value = UIState.Content(newList)
                
                // Emit scroll to top event for new conversation added at top
                viewModelScope.launch {
                    _scrollToTopEvent.emit(Unit)
                }
            }
        }
    }
    
    /**
     * Updates group in conversations.
     */
    private fun updateGroupInConversation(group: Group) {
        viewModelScope.launch {
            _conversations.value = _conversations.value.map { conversation ->
                if (conversation.conversationType == UIKitConstants.ConversationType.GROUPS) {
                    val conversationGroup = conversation.conversationWith as? Group
                    if (conversationGroup?.guid == group.guid) {
                        // Clone to create new reference for Compose recomposition
                        conversation.clone().apply { 
                            conversationWith = group 
                        }
                    } else {
                        conversation
                    }
                } else {
                    conversation
                }
            }
        }
    }
    
    // ==================== ListOperations Interface Implementation ====================
    // All methods are open for client override
    
    /**
     * Adds a single conversation to the list.
     * Override to add custom validation or processing.
     *
     * @param item The conversation to add
     */
    override fun addItem(item: Conversation) {
        listDelegate.addItem(item)
        updateUIStateFromList()
    }
    
    /**
     * Adds multiple conversations to the list.
     * Override to add custom validation or processing.
     *
     * @param items The conversations to add
     */
    override fun addItems(items: List<Conversation>) {
        listDelegate.addItems(items)
        updateUIStateFromList()
    }
    
    /**
     * Removes a conversation from the list.
     * Override to add custom confirmation or logging.
     *
     * @param item The conversation to remove
     * @return true if removed, false if not found
     */
    override fun removeItem(item: Conversation): Boolean {
        val result = listDelegate.removeItem(item)
        if (result) {
            updateUIStateFromList()
        }
        return result
    }
    
    /**
     * Removes a conversation at the specified index.
     * Override to add custom confirmation or logging.
     *
     * @param index The index of the conversation to remove
     * @return The removed conversation, or null if index is out of bounds
     */
    override fun removeItemAt(index: Int): Conversation? {
        val result = listDelegate.removeItemAt(index)
        if (result != null) {
            updateUIStateFromList()
        }
        return result
    }
    
    /**
     * Updates a conversation matching the predicate.
     * Override to add custom validation or transformation.
     *
     * @param item The new conversation to replace with
     * @param predicate Function to find the conversation to update
     * @return true if updated, false if no match found
     */
    override fun updateItem(item: Conversation, predicate: (Conversation) -> Boolean): Boolean {
        val result = listDelegate.updateItem(item, predicate)
        if (result) {
            updateUIStateFromList()
        }
        return result
    }
    
    /**
     * Removes all conversations from the list.
     * Override to add custom confirmation or cleanup.
     */
    override fun clearItems() {
        listDelegate.clearItems()
        updateUIStateFromList()
    }
    
    /**
     * Returns a copy of all conversations in the list.
     *
     * @return Immutable list of all conversations
     */
    override fun getItems(): List<Conversation> {
        return listDelegate.getItems()
    }
    
    /**
     * Returns the conversation at the specified index.
     *
     * @param index The index of the conversation
     * @return The conversation at the index, or null if out of bounds
     */
    override fun getItemAt(index: Int): Conversation? {
        return listDelegate.getItemAt(index)
    }
    
    /**
     * Returns the number of conversations in the list.
     *
     * @return The conversation count
     */
    override fun getItemCount(): Int {
        return listDelegate.getItemCount()
    }
    
    /**
     * Moves a conversation to the top of the list.
     * Override to add custom logic or scroll behavior.
     *
     * @param item The conversation to move to top
     */
    override fun moveItemToTop(item: Conversation) {
        listDelegate.moveItemToTop(item)
        updateUIStateFromList()
    }
    
    /**
     * Performs multiple operations in a single batch, emitting only once.
     * Critical for performance when receiving many updates rapidly.
     * Override to add custom batch processing logic.
     *
     * @param operations Lambda that performs multiple list operations
     */
    override fun batch(operations: ListOperationsBatchScope<Conversation>.() -> Unit) {
        listDelegate.batch(operations)
        updateUIStateFromList()
    }
    
    /**
     * Updates the UI state based on the current list contents.
     */
    private fun updateUIStateFromList() {
        val currentList = _conversations.value
        _uiState.value = if (currentList.isEmpty()) {
            UIState.Empty
        } else {
            UIState.Content(currentList)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        if (enableListeners) {
            removeListeners()
        }
        // Cancel local event listener jobs
        conversationEventsJob?.cancel()
        groupEventsJob?.cancel()
        userEventsJob?.cancel()
        messageEventsJob?.cancel()
        callEventsJob?.cancel()
        
        // Cancel list delegate debounce operations
        listDelegate.cancel()
        
        typingDebounceJob?.cancel()
        soundManager?.release()
        soundManager = null
    }
}
