package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.state.ChatHistoryUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing AI Assistant Chat History.
 * Handles message fetching, deletion, real-time event listeners, and UI state management.
 * Uses StateFlow for state management and SharedFlow for one-shot events.
 *
 * This ViewModel makes direct CometChat SDK calls (no repository pattern),
 * matching the reference Java implementation.
 *
 * @param enableListeners Whether to enable CometChat listeners (set to false for testing)
 */
open class CometChatAIAssistantChatHistoryViewModel(
    private val enableListeners: Boolean = true
) : ViewModel() {

    // --- State Streams ---

    /** Internal mutable list of messages. */
    private val messageArrayList = mutableListOf<BaseMessage>()

    /** Observable list of messages for the UI. */
    private val _messages = MutableStateFlow<List<BaseMessage>>(emptyList())
    val messages: StateFlow<List<BaseMessage>> = _messages.asStateFlow()

    /** Current UI state (Loading, Empty, Error, Content). */
    private val _uiState = MutableStateFlow<ChatHistoryUIState>(ChatHistoryUIState.Empty)
    val uiState: StateFlow<ChatHistoryUIState> = _uiState.asStateFlow()

    /** Delete operation state. */
    private val _deleteState = MutableSharedFlow<UIKitConstants.DeleteState>(extraBufferCapacity = 3)
    val deleteState: SharedFlow<UIKitConstants.DeleteState> = _deleteState.asSharedFlow()

    /** Emits the position of a removed message for adapter notification. */
    private val _removeMessagePosition = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val removeMessagePosition: SharedFlow<Int> = _removeMessagePosition.asSharedFlow()

    /** Whether more messages are available for pagination. */
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    /** Whether a fetch operation is currently in progress. */
    private val _isInProgress = MutableStateFlow(false)
    val isInProgress: StateFlow<Boolean> = _isInProgress.asStateFlow()

    /** Emits the count of newly prepended messages for adapter range notification. */
    private val _messagesRangeChanged = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messagesRangeChanged: SharedFlow<Int> = _messagesRangeChanged.asSharedFlow()

    // --- Internal State ---

    private var messagesRequest: MessagesRequest? = null
    private var messageRequestBuilder: MessagesRequest.MessagesRequestBuilder? = null
    private var user: User? = null
    private var group: Group? = null
    private var listenerTag: String? = null
    private var messageEventsJob: Job? = null

    // --- Public Methods ---

    /**
     * Sets the User context and triggers the first message fetch.
     * Builds a MessagesRequest filtered by the User UID with limit=20,
     * categories=[MESSAGE], types=[TEXT], hideDeletedMessages=true, hideReplies=true.
     *
     * @param user The User whose chat history to display
     */
    fun setUser(user: User) {
        this.user = user
        initializeMessagesRequest()
        fetchMessages()
    }

    /**
     * Sets the Group context without triggering a fetch.
     * Builds a MessagesRequest filtered by the Group GUID with the same filters.
     *
     * @param group The Group whose chat history to display
     */
    fun setGroup(group: Group) {
        this.group = group
        initializeMessagesRequest()
    }

    /**
     * Fetches the next page of messages using MessagesRequest.fetchPrevious().
     * Guards against concurrent fetches via [_isInProgress].
     * Reverses results and prepends to the internal list.
     * Sets hasMore=false on empty response.
     * Emits Loading state on first fetch when list is empty.
     */
    fun fetchMessages() {
        if (messagesRequest == null) return
        if (!_hasMore.value) return
        if (_isInProgress.value) return

        _isInProgress.value = true

        if (messageArrayList.isEmpty()) {
            _uiState.value = ChatHistoryUIState.Loading
        }

        messagesRequest?.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(fetchedMessages: List<BaseMessage>) {
                val hasResults = fetchedMessages.isNotEmpty()
                _hasMore.value = hasResults

                if (hasResults) {
                    val reversed = fetchedMessages.reversed()
                    messageArrayList.addAll(reversed)
                    _messages.value = ArrayList(messageArrayList)
                    _messagesRangeChanged.tryEmit(reversed.size)
                }

                _isInProgress.value = false

                if (messageArrayList.isEmpty()) {
                    _uiState.value = ChatHistoryUIState.Empty
                } else {
                    _uiState.value = ChatHistoryUIState.Content(ArrayList(messageArrayList))
                }
            }

            override fun onError(exception: CometChatException) {
                _isInProgress.value = false
                _uiState.value = ChatHistoryUIState.Error(exception)
            }
        })
    }

    /**
     * Deletes a chat history message via the CometChat SDK.
     * Emits INITIATED_DELETE, then SUCCESS_DELETE or FAILURE_DELETE.
     * On success, removes the message from the local list and fires a UIKit
     * MessageDeleted event for inter-component communication.
     *
     * @param baseMessage The message to delete
     */
    fun deleteChatHistoryItem(baseMessage: BaseMessage) {
        _deleteState.tryEmit(UIKitConstants.DeleteState.INITIATED_DELETE)

        CometChat.deleteMessage(baseMessage.id, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(deletedMessage: BaseMessage) {
                _deleteState.tryEmit(UIKitConstants.DeleteState.SUCCESS_DELETE)
                // Remove the original message from the local list immediately
                remove(baseMessage)
                // Emit UIKit event for inter-component communication
                // Equivalent to Java's CometChatUIKitHelper.onMessageDeleted()
                CometChatEvents.emitMessageEvent(
                    CometChatMessageEvent.MessageDeleted(deletedMessage)
                )
            }

            override fun onError(exception: CometChatException) {
                _deleteState.tryEmit(UIKitConstants.DeleteState.FAILURE_DELETE)
            }
        })
    }

    /**
     * Removes a message from the internal list and emits the removed position.
     * Matches by message ID for reliable identification regardless of object reference.
     * Transitions to Empty state if the list becomes empty after removal.
     *
     * @param baseMessage The message to remove
     */
    fun remove(baseMessage: BaseMessage) {
        val oldIndex = messageArrayList.indexOfFirst { it.id == baseMessage.id }
        if (oldIndex == -1) return

        messageArrayList.removeAt(oldIndex)
        _messages.value = ArrayList(messageArrayList)
        _removeMessagePosition.tryEmit(oldIndex)

        if (messageArrayList.isEmpty()) {
            _uiState.value = ChatHistoryUIState.Empty
        } else {
            _uiState.value = ChatHistoryUIState.Content(ArrayList(messageArrayList))
        }
    }

    /**
     * Registers real-time event listeners.
     * - SDK MessageListener for server-pushed deletion events
     * - UIKit CometChatEvents.messageEvents for local inter-component deletion events
     */
    fun addListeners() {
        if (!enableListeners) return

        listenerTag = "ChatHistory_${System.currentTimeMillis()}"

        // SDK Listener — receives server-pushed real-time events
        CometChat.addMessageListener(listenerTag!!, object : CometChat.MessageListener() {
            override fun onMessageDeleted(message: BaseMessage) {
                remove(message)
            }
        })

        // UIKit Local Events — receives UI-initiated events from other components
        messageEventsJob = viewModelScope.launch {
            CometChatEvents.messageEvents.collect { event ->
                when (event) {
                    is CometChatMessageEvent.MessageDeleted -> remove(event.message)
                    else -> { /* ignore other events */ }
                }
            }
        }
    }

    /**
     * Unregisters all event listeners and cancels the message events job.
     */
    fun removeListeners() {
        listenerTag?.let { CometChat.removeMessageListener(it) }
        messageEventsJob?.cancel()
        messageEventsJob = null
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }

    // --- Private Methods ---

    /**
     * Initializes the MessagesRequest with the appropriate UID or GUID filter.
     * Only builds the request once (guards against re-initialization).
     */
    private fun initializeMessagesRequest() {
        if (messageRequestBuilder == null) {
            messageRequestBuilder = MessagesRequest.MessagesRequestBuilder()
                .setLimit(20)
                .setCategories(listOf(UIKitConstants.MessageCategory.MESSAGE))
                .setTypes(listOf(UIKitConstants.MessageType.TEXT))
                .hideDeletedMessages(true)
                .hideReplies(true)

            if (user != null) {
                messagesRequest = messageRequestBuilder!!.setUID(user!!.uid).build()
            } else if (group != null) {
                messagesRequest = messageRequestBuilder!!.setGUID(group!!.guid).build()
            }
        }
    }
}
