package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.ReactionEvent
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.MessageStatus
import com.cometchat.uikit.core.utils.PinSaveUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for CometChatThreadHeader component.
 *
 * This ViewModel is located in chatuikit-core to be shared between
 * chatuikit-kotlin (View-based) and chatuikit-jetpack (Compose-based) modules.
 *
 * Manages state and real-time updates for the thread header using StateFlow,
 * including:
 * - Parent message state
 * - Reply count tracking
 * - Real-time message events (sent, received, updated)
 *
 * ## Key Responsibilities
 * 1. Hold parent message state
 * 2. Track reply count
 * 3. Listen for SDK message events
 * 4. Listen for UIKit local events
 * 5. Update parent message on edits/deletes
 * 6. Handle reaction updates
 *
 * ## StateFlow Observables
 * - [parentMessageListStateFlow]: Parent message list (single-item list)
 * - [replyCountStateFlow]: Current reply count
 * - [sentMessage]: Sent message events (SharedFlow for one-time events)
 * - [receiveMessage]: Received message events (SharedFlow for one-time events)
 * - [updateParentMessage]: Parent message updates (SharedFlow for one-time events)
 *
 * ## Usage (Compose)
 * ```kotlin
 * @Composable
 * fun ThreadHeaderScreen(parentMessage: BaseMessage) {
 *     val viewModel: CometChatThreadHeaderViewModel = viewModel()
 *
 *     LaunchedEffect(parentMessage) {
 *         viewModel.setParentMessage(parentMessage)
 *     }
 *
 *     val replyCount by viewModel.replyCountStateFlow.collectAsState()
 *     val messageList by viewModel.parentMessageListStateFlow.collectAsState()
 * }
 * ```
 *
 * ## Usage (View-based with LiveData adapter)
 * ```kotlin
 * val viewModel: CometChatThreadHeaderViewModel by viewModels()
 * viewModel.setParentMessage(parentMessage)
 *
 * // Observe StateFlow as LiveData using asLiveData() extension
 * viewModel.replyCountStateFlow.asLiveData().observe(viewLifecycleOwner) { count ->
 *     updateReplyCountUI(count)
 * }
 * ```
 */
open class CometChatThreadHeaderViewModel(
    private val enableListeners: Boolean = true
) : ViewModel() {

    companion object {
        private val TAG = CometChatThreadHeaderViewModel::class.java.simpleName
    }

    // ==================== State ====================

    /**
     * The parent message of the thread.
     * This is the original message that started the thread conversation.
     */
    private var _parentMessage: BaseMessage? = null

    /**
     * Internal list holding the parent message.
     * Contains a single item (the parent message) when set.
     */
    protected val messageList: MutableList<BaseMessage> = mutableListOf()

    /**
     * Flag to control reaction visibility.
     * When true, reaction updates are ignored.
     */
    var hideReaction: Boolean = false

    /**
     * Unique listener ID for CometChat SDK message events.
     */
    protected var listenerId: String = ""

    /**
     * Job for UIKit message events subscription.
     */
    private var messageEventsJob: Job? = null

    // ==================== StateFlow ====================

    /**
     * StateFlow for the parent message list.
     * Contains a single-item list with the parent message.
     */
    private val _parentMessageListStateFlow = MutableStateFlow<List<BaseMessage>>(emptyList())
    val parentMessageListStateFlow: StateFlow<List<BaseMessage>> = _parentMessageListStateFlow.asStateFlow()

    /**
     * StateFlow for the current reply count.
     */
    private val _replyCountStateFlow = MutableStateFlow(0)
    val replyCountStateFlow: StateFlow<Int> = _replyCountStateFlow.asStateFlow()

    /**
     * SharedFlow for sent message events.
     */
    private val _sentMessage = MutableSharedFlow<BaseMessage>()
    val sentMessage: SharedFlow<BaseMessage> = _sentMessage.asSharedFlow()

    /**
     * SharedFlow for received message events.
     */
    private val _receiveMessage = MutableSharedFlow<BaseMessage>()
    val receiveMessage: SharedFlow<BaseMessage> = _receiveMessage.asSharedFlow()

    /**
     * SharedFlow for parent message updates.
     */
    private val _updateParentMessage = MutableSharedFlow<BaseMessage>()
    val updateParentMessage: SharedFlow<BaseMessage> = _updateParentMessage.asSharedFlow()

    // ==================== LiveData (for View-based UI) ====================

    /**
     * LiveData for the parent message list.
     * Converts the StateFlow to LiveData for View-based UI compatibility.
     */
    val parentMessageListLiveData: LiveData<List<BaseMessage>> = _parentMessageListStateFlow.asLiveData()

    /**
     * LiveData for the current reply count.
     * Converts the StateFlow to LiveData for View-based UI compatibility.
     */
    val replyCount: LiveData<Int> = _replyCountStateFlow.asLiveData()

    // ==================== Public Methods ====================

    /**
     * Sets the parent message for the thread header.
     *
     * @param parentMessage The parent message to display. If null, no action is taken.
     */
    fun setParentMessage(parentMessage: BaseMessage?) {
        if (parentMessage != null) {
            this._parentMessage = parentMessage
            _replyCountStateFlow.value = parentMessage.replyCount
            messageList.clear()
            messageList.add(parentMessage)
            _parentMessageListStateFlow.value = messageList.toList()
        }
    }

    /**
     * Gets the current parent message.
     *
     * @return The parent message, or null if not set.
     */
    fun getCurrentParentMessage(): BaseMessage? = _parentMessage

    // ==================== Reply Count Methods ====================

    /**
     * Increments the reply count by 1.
     */
    protected fun incrementParentMessageReplyCount() {
        _replyCountStateFlow.value = _replyCountStateFlow.value + 1
    }

    // ==================== Update Methods ====================

    /**
     * Updates the parent message in the list.
     *
     * @param baseMessage The updated message. If null, no action is taken.
     */
    fun updateParentMessageInList(baseMessage: BaseMessage?) {
        val parent = _parentMessage
        if (baseMessage != null && parent != null) {
            var emitted = baseMessage
            // Thread message update - refresh the parent message display
            if (baseMessage.parentMessageId > 0 && baseMessage.parentMessageId == parent.id) {
                if (messageList.isNotEmpty()) {
                    messageList[0] = parent
                    _parentMessageListStateFlow.value = messageList.toList()
                }
            }
            // Parent message itself updated. An edit/moderation payload carries the new content
            // but not the viewer's pin/save state — carry it over from the copy this header holds,
            // and adopt the result as the current parent so later carries start from the latest
            // content rather than the one the screen was opened with.
            if (baseMessage.id == parent.id) {
                val reconciled = PinSaveUtils.carryPinSaveForward(parent, baseMessage)
                _parentMessage = reconciled
                emitted = reconciled
                if (messageList.isNotEmpty()) {
                    messageList[0] = reconciled
                    _parentMessageListStateFlow.value = messageList.toList()
                }
            }
            viewModelScope.launch {
                _receiveMessage.emit(emitted)
            }
        }
    }

    /**
     * Applies a pin/save change to the parent this header shows.
     *
     * The message list only reconciles the rows it has loaded, and the thread's parent is not one of
     * them — it lives here. Without this, pinning/saving the parent from inside the thread (or from
     * another device) left the header's indicator stale until the screen was reopened.
     *
     * Same contract as the message list's `applyPinSaveEcho`: the delivered [echo] may be partial,
     * so only the pin/save attributes are taken from it and applied to a [BaseMessage.clone] of the
     * held parent — never to the held instance, so the StateFlow sees a new value.
     */
    private fun applyParentPinSaveChange(echo: BaseMessage, applyTo: (BaseMessage) -> Unit) {
        val parent = _parentMessage ?: return
        if (echo.id != parent.id) return
        val updated = parent.clone().apply(applyTo)
        _parentMessage = updated
        if (messageList.isNotEmpty()) {
            messageList[0] = updated
            _parentMessageListStateFlow.value = messageList.toList()
        }
    }

    // ==================== Protected Emitters ====================

    /**
     * Emits a sent message event.
     */
    protected fun emitSentMessage(message: BaseMessage) {
        viewModelScope.launch {
            _sentMessage.emit(message)
        }
    }

    /**
     * Emits a receive message event.
     */
    protected fun emitReceiveMessage(message: BaseMessage) {
        viewModelScope.launch {
            _receiveMessage.emit(message)
        }
    }

    /**
     * Emits an update parent message event.
     */
    protected fun emitUpdateParentMessage(message: BaseMessage) {
        viewModelScope.launch {
            _updateParentMessage.emit(message)
        }
    }

    // ==================== Listener Methods ====================

    /**
     * Adds SDK message listener for real-time server-pushed events.
     */
    open fun addListener() {
        if (!enableListeners) return
        
        // Remove previous listener to prevent duplicate event handling
        if (listenerId.isNotEmpty()) {
            CometChat.removeMessageListener(listenerId)
        }
        
        listenerId = "ThreadHeader_${System.currentTimeMillis()}"

        CometChat.addMessageListener(listenerId, object : CometChat.MessageListener() {
            override fun onTextMessageReceived(message: TextMessage) {
                handleMessageReceived(message)
            }

            override fun onMediaMessageReceived(message: MediaMessage) {
                handleMessageReceived(message)
            }

            override fun onCustomMessageReceived(message: CustomMessage) {
                handleMessageReceived(message)
            }

            override fun onMessageEdited(message: BaseMessage) {
                handleMessageEdited(message)
            }

            override fun onMessageDeleted(message: BaseMessage) {
                handleMessageDeleted(message)
            }

            override fun onMessagesDelivered(messageReceipt: MessageReceipt) {
                handleMessageReceipt(messageReceipt)
            }

            override fun onMessagesRead(messageReceipt: MessageReceipt) {
                handleMessageReceipt(messageReceipt)
            }

            override fun onMessagesDeliveredToAll(messageReceipt: MessageReceipt) {
                handleMessageReceipt(messageReceipt)
            }

            override fun onMessagesReadByAll(messageReceipt: MessageReceipt) {
                handleMessageReceipt(messageReceipt)
            }

            override fun onMessageReactionAdded(reactionEvent: ReactionEvent) {
                handleReactionAdded(reactionEvent)
            }

            override fun onMessageReactionRemoved(reactionEvent: ReactionEvent) {
                handleReactionRemoved(reactionEvent)
            }

            // Pin/save state of the parent. On the acting device these arrive as the SDK's
            // self-echo off the REST response; from another device as a realtime action. The
            // event is the assertion — a "pinned"/"saved" echo without a timestamp still counts.
            override fun onMessagePinned(message: BaseMessage) {
                applyParentPinSaveChange(message) { updated ->
                    updated.pinnedAt = if (message.pinnedAt > 0) message.pinnedAt
                    else System.currentTimeMillis() / 1000
                    message.pinnedBy?.let { updated.pinnedBy = it }
                }
            }

            override fun onMessageUnpinned(message: BaseMessage) {
                applyParentPinSaveChange(message) { updated ->
                    updated.pinnedAt = 0
                    updated.pinnedBy = null
                }
            }

            override fun onMessageSaved(message: BaseMessage) {
                applyParentPinSaveChange(message) { updated ->
                    updated.savedAt = if (message.savedAt > 0) message.savedAt
                    else System.currentTimeMillis() / 1000
                }
            }

            override fun onMessageUnsaved(message: BaseMessage) {
                applyParentPinSaveChange(message) { updated -> updated.savedAt = 0 }
            }
        })
    }

    /**
     * Adds UIKit local event listeners for inter-component communication.
     */
    open fun addLocalEventListeners() {
        if (!enableListeners) return
        
        // Cancel previous subscription to prevent duplicate event handling
        messageEventsJob?.cancel()
        
        messageEventsJob = viewModelScope.launch {
            CometChatEvents.messageEvents.collect { event ->
                when (event) {
                    is CometChatMessageEvent.MessageSent -> {
                        if (event.status == MessageStatus.SUCCESS) {
                            handleUIMessageSent(event.message)
                        }
                    }
                    is CometChatMessageEvent.MessageEdited -> {
                        if (event.status == MessageStatus.SUCCESS) {
                            handleMessageEdited(event.message)
                        }
                    }
                    is CometChatMessageEvent.MessageDeleted -> {
                        handleMessageDeleted(event.message)
                    }
                    else -> { /* Ignore other message events */ }
                }
            }
        }
    }

    /**
     * Removes the SDK message listener and UIKit event subscriptions.
     */
    open fun removeListener() {
        if (listenerId.isNotEmpty()) {
            CometChat.removeMessageListener(listenerId)
        }
        messageEventsJob?.cancel()
        messageEventsJob = null
    }

    // ==================== Event Handlers ====================

    /**
     * Handles incoming message received from SDK listener.
     * Skips messages sent by the current user to prevent double-counting.
     */
    private fun handleMessageReceived(message: BaseMessage) {
        _parentMessage?.let { parent ->
            if (message.parentMessageId > 0 && message.parentMessageId == parent.id) {
                // Skip messages sent by the current user - handled by handleUIMessageSent
                val loggedInUser = CometChat.getLoggedInUser()
                if (loggedInUser != null && message.sender?.uid == loggedInUser.uid) {
                    return@let
                }

                incrementParentMessageReplyCount()
                updateParentMessageInList(message)
            }
        }
    }

    /**
     * Handles UI-initiated message sent event.
     */
    private fun handleUIMessageSent(message: BaseMessage) {
        _parentMessage?.let { parent ->
            if (message.parentMessageId > 0 && message.parentMessageId == parent.id) {
                incrementParentMessageReplyCount()
            }
        }
    }

    /**
     * Handles message edited event.
     */
    private fun handleMessageEdited(message: BaseMessage) {
        // An edit arrives flagless and would otherwise un-follow the thread this header is showing;
        // an edit that leaves the user @-mentioned in a reply subscribes them. Reconcile either way
        // before the edited copy is shown.
        com.cometchat.uikit.core.utils.CometChatThreadSubscription.applyEditedMessageSubscription(
            editedMessage = message,
            heldMessage = _parentMessage?.takeIf { it.id == message.id },
            parentMessage = _parentMessage
        )
        _parentMessage?.let { parent ->
            if (message.id == parent.id) {
                updateParentMessageInList(message)
            }
        }
    }

    /**
     * Handles message deleted event.
     */
    private fun handleMessageDeleted(message: BaseMessage) {
        _parentMessage?.let { parent ->
            if (message.id == parent.id) {
                updateParentMessageInList(message)
            }
        }
    }

    /**
     * Handles message receipt events.
     */
    private fun handleMessageReceipt(receipt: MessageReceipt) {
        _parentMessage?.let { parent ->
            if (receipt.messageId == parent.id) {
                if (receipt.deliveredAt > 0) {
                    parent.deliveredAt = receipt.deliveredAt
                }
                if (receipt.readAt > 0) {
                    parent.readAt = receipt.readAt
                }
                updateParentMessageInList(parent)
            }
        }
    }

    /**
     * Handles reaction added event.
     */
    private fun handleReactionAdded(reactionEvent: ReactionEvent) {
        if (hideReaction) return

        _parentMessage?.let { parent ->
            val reactionMessageId = reactionEvent.reaction?.messageId
            if (reactionMessageId != null && reactionMessageId == parent.id) {
                updateParentMessageInList(parent)
            }
        }
    }

    /**
     * Handles reaction removed event.
     */
    private fun handleReactionRemoved(reactionEvent: ReactionEvent) {
        if (hideReaction) return

        _parentMessage?.let { parent ->
            val reactionMessageId = reactionEvent.reaction?.messageId
            if (reactionMessageId != null && reactionMessageId == parent.id) {
                updateParentMessageInList(parent)
            }
        }
    }

    // ==================== Lifecycle ====================

    override fun onCleared() {
        super.onCleared()
        removeListener()
        messageList.clear()
        _parentMessage = null
    }
}
