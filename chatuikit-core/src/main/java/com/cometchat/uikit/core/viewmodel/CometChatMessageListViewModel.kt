package com.cometchat.uikit.core.viewmodel

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.ReactionEvent
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.CometChatMessageOption
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatConversationEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.MessageStatus
import com.cometchat.chat.helpers.CometChatHelper
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.resources.soundmanager.CometChatSoundManager
import com.cometchat.uikit.core.resources.soundmanager.Sound
import com.cometchat.uikit.core.state.MessageAlignment
import com.cometchat.uikit.core.state.MessageDeleteState
import com.cometchat.uikit.core.state.MessageFlagState
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.state.ConversationStarterUIState
import com.cometchat.uikit.core.state.ConversationSummaryUIState
import com.cometchat.uikit.core.state.SmartRepliesUIState
import com.cometchat.uikit.core.utils.getDefaultMessagesCategories
import com.cometchat.uikit.core.utils.getDefaultMessagesTypes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the CometChatMessageList component.
 *
 * This ViewModel manages message state, fetching, real-time updates, and user interactions
 * for displaying messages in a chat interface. It is designed to be shared between
 * `chatuikit-kotlin` (XML Views) and `chatuikit-jetpack` (Compose) UI modules.
 *
 * ## Architecture
 *
 * The ViewModel follows the MVVM pattern with unidirectional data flow:
 * - UI observes [StateFlow] properties for reactive updates
 * - User actions trigger methods that update state through the repository
 * - Real-time events from CometChat SDK are handled via listeners
 *
 * ## ListOperations Interface
 *
 * This ViewModel implements [ListOperations]<[BaseMessage]> for standardized list manipulation.
 * All list operation methods are open for client override, enabling custom behavior:
 *
 * ```kotlin
 * class CustomMessageListViewModel(repository: MessageListRepository) :
 *     CometChatMessageListViewModel(repository) {
 *
 *     override fun addItem(item: BaseMessage) {
 *         // Custom logic before adding
 *         super.addItem(item)
 *         // Custom logic after adding
 *     }
 * }
 * ```
 *
 * ## State Flows
 *
 * The ViewModel exposes several [StateFlow] properties for UI observation:
 *
 * | StateFlow | Type | Description |
 * |-----------|------|-------------|
 * | [uiState] | [MessageListUIState] | Current UI state (Loading, Loaded, Empty, Error) |
 * | [messages] | List<[BaseMessage]> | Current list of messages |
 * | [hasMorePreviousMessages] | Boolean | Whether more older messages are available |
 * | [hasMoreNewMessages] | Boolean | Whether more newer messages are available |
 * | [isInProgress] | Boolean | Whether a fetch operation is in progress |
 * | [scrollToMessageId] | Long? | Message ID to scroll to (null when no scroll needed) |
 * | [unreadMessageAnchor] | [BaseMessage]? | First unread message for anchor display |
 * | [unreadCount] | Int | Number of unread messages |
 * | [deleteState] | [MessageDeleteState] | State of delete operation |
 * | [flagState] | [MessageFlagState] | State of flag/report operation |
 * | [typingUsers] | List<[User]> | Users currently typing in the conversation |
 *
 * ## Usage
 *
 * ```kotlin
 * // Create ViewModel
 * val viewModel = CometChatMessageListViewModel(repository)
 *
 * // Configure for user conversation
 * viewModel.setUser(user)
 *
 * // Or configure for group conversation
 * viewModel.setGroup(group)
 *
 * // Fetch messages
 * viewModel.fetchMessages()
 *
 * // Observe state in UI
 * viewModel.messages.collect { messages ->
 *     // Update UI with messages
 * }
 *
 * // Use ListOperations methods
 * viewModel.addItem(newMessage)
 * viewModel.removeItem(oldMessage)
 * viewModel.updateItem(updatedMessage) { it.id == updatedMessage.id }
 * viewModel.batch {
 *     add(message1)
 *     add(message2)
 *     remove(deletedMessage)
 * }
 * ```
 *
 * ## Real-time Updates
 *
 * When [enableListeners] is `true`, the ViewModel automatically registers CometChat
 * listeners for:
 * - New messages (text, media, custom)
 * - Message edits and deletions
 * - Delivery and read receipts
 * - Typing indicators
 * - Reactions
 * - Group actions (member joined, left, kicked, banned, scope changed)
 * - Call actions
 * - Connection status changes
 *
 * ## Threading Support
 *
 * For threaded conversations, pass the parent message ID when configuring:
 * ```kotlin
 * viewModel.setUser(user, parentMessageId = parentMessage.id)
 * ```
 *
 * ## Receipt Handling
 *
 * The ViewModel provides comprehensive support for delivery and read receipts:
 *
 * ### Delivery Receipts
 * - [markAsDelivered] is called automatically when messages are received via real-time listeners
 * - Only sends receipts for messages from other users (not own messages)
 * - Respects the [setDisableReceipt] configuration
 *
 * ### Read Receipts
 * - [markLastMessageAsRead] marks individual messages as read with full validation:
 *   - Checks if message is already read (`readAt == 0`)
 *   - Handles thread context (main conversation vs thread view)
 *   - Updates local message state on success
 * - [markConversationRead] marks the entire conversation as read and resets [unreadCount]
 *
 * ### Receipt Events
 * UI layers should observe [messageReadEvent] to trigger UIKit helper notifications:
 * ```kotlin
 * viewModel.messageReadEvent.collect { message ->
 *     CometChatUIKitHelper.onMessageRead(message)
 * }
 * ```
 *
 * ### Disabling Receipts
 * For privacy-focused applications, receipts can be disabled:
 * ```kotlin
 * viewModel.setDisableReceipt(true)
 * ```
 *
 * @param repository The [MessageListRepository] for message data operations.
 * @param enableListeners Whether to enable CometChat real-time listeners.
 *                        Set to `false` for unit testing to avoid SDK dependencies.
 *
 * @see MessageListRepository
 * @see MessageListUIState
 * @see MessageAlignment
 * @see ListOperations
 */
open class CometChatMessageListViewModel(
    private val repository: MessageListRepository,
    private val enableListeners: Boolean = true
) : ViewModel(), ListOperations<BaseMessage> {

    
    // ========================================
    // State Flows
    // ========================================
    
    /**
     * The current conversation ID map.
     *
     * Contains:
     * - [UIKitConstants.MapId.RECEIVER_ID]: The user UID or group GUID
     * - [UIKitConstants.MapId.RECEIVER_TYPE]: "user" or "group"
     * - [UIKitConstants.MapId.PARENT_MESSAGE_ID]: Thread parent ID (if in thread)
     *
     * This map is used for:
     * - Inter-component communication
     * - Event filtering to ensure events are for the current conversation
     * - Panel management identification
     */
    private val _idMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val idMap: StateFlow<Map<String, String>> = _idMap.asStateFlow()
    
    /**
     * The current UI state of the message list.
     *
     * Possible states:
     * - [MessageListUIState.Loading]: Initial loading or refreshing
     * - [MessageListUIState.Loaded]: Messages loaded successfully
     * - [MessageListUIState.Empty]: No messages in the conversation
     * - [MessageListUIState.Error]: An error occurred during fetching
     */
    private val _uiState = MutableStateFlow<MessageListUIState>(MessageListUIState.Loading)
    val uiState: StateFlow<MessageListUIState> = _uiState.asStateFlow()
    
    /**
     * The current list of messages in the conversation.
     *
     * Messages are ordered chronologically with the newest message at the end.
     * The list is updated automatically when:
     * - Messages are fetched via [fetchMessages] or [fetchNextMessages]
     * - New messages are received in real-time
     * - Messages are edited, deleted, or have reactions added/removed
     */
    private val _messages = MutableStateFlow<List<BaseMessage>>(emptyList())
    val messages: StateFlow<List<BaseMessage>> = _messages.asStateFlow()
    
    /**
     * Delegate for list operations.
     * Handles internal list manipulation with message ID-based equality checking.
     */
    private val listDelegate = ListOperationsDelegate(
        stateFlow = _messages,
        equalityChecker = { a, b -> a.id == b.id }
    )
    
    /**
     * Whether there are more previous (older) messages available to fetch.
     *
     * Check this before calling [fetchMessages] to avoid unnecessary requests.
     * This becomes `false` when the oldest message in the conversation is reached.
     */
    private val _hasMorePreviousMessages = MutableStateFlow(true)
    val hasMorePreviousMessages: StateFlow<Boolean> = _hasMorePreviousMessages.asStateFlow()
    
    /**
     * Whether there are more next (newer) messages available to fetch.
     *
     * This is typically `true` when the user has scrolled up and new messages
     * have arrived that are not yet in the current list.
     */
    private val _hasMoreNewMessages = MutableStateFlow(false)
    val hasMoreNewMessages: StateFlow<Boolean> = _hasMoreNewMessages.asStateFlow()
    
    /**
     * Whether a fetch operation is currently in progress.
     *
     * Use this to show loading indicators during pagination.
     * Only one fetch operation can be in progress at a time.
     */
    private val _isInProgress = MutableStateFlow(false)
    val isInProgress: StateFlow<Boolean> = _isInProgress.asStateFlow()
    
    /**
     * The message ID to scroll to, or `null` if no scroll is needed.
     *
     * Set by [goToMessage] and should be cleared by the UI after scrolling
     * by calling [clearScrollToMessage].
     */
    private val _scrollToMessageId = MutableStateFlow<Long?>(null)
    val scrollToMessageId: StateFlow<Long?> = _scrollToMessageId.asStateFlow()
    
    /**
     * The first unread message, used to display an "unread messages" anchor.
     *
     * This is set when [startFromUnreadMessages] is enabled and there are
     * unread messages in the conversation.
     */
    private val _unreadMessageAnchor = MutableStateFlow<BaseMessage?>(null)
    val unreadMessageAnchor: StateFlow<BaseMessage?> = _unreadMessageAnchor.asStateFlow()
    
    /**
     * The number of unread messages in the conversation.
     *
     * This is fetched from the conversation metadata when [fetchMessagesWithUnreadCount]
     * is called.
     */
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    /**
     * The current state of a delete operation.
     *
     * States:
     * - [MessageDeleteState.Idle]: No delete operation in progress
     * - [MessageDeleteState.InProgress]: Delete operation is running
     * - [MessageDeleteState.Success]: Delete completed successfully
     * - [MessageDeleteState.Error]: Delete failed with an error
     *
     * Call [resetDeleteState] to return to [MessageDeleteState.Idle] after handling.
     */
    private val _deleteState = MutableStateFlow<MessageDeleteState>(MessageDeleteState.Idle)
    val deleteState: StateFlow<MessageDeleteState> = _deleteState.asStateFlow()
    
    /**
     * The current state of a flag/report operation.
     *
     * States:
     * - [MessageFlagState.Idle]: No flag operation in progress
     * - [MessageFlagState.InProgress]: Flag operation is running
     * - [MessageFlagState.Success]: Flag completed successfully
     * - [MessageFlagState.Error]: Flag failed with an error
     *
     * Call [resetFlagState] to return to [MessageFlagState.Idle] after handling.
     */
    private val _flagState = MutableStateFlow<MessageFlagState>(MessageFlagState.Idle)
    val flagState: StateFlow<MessageFlagState> = _flagState.asStateFlow()
    
    /**
     * List of users currently typing in the conversation.
     *
     * Updated automatically via typing indicator listeners.
     * Users are added when they start typing and removed when they stop.
     */
    private val _typingUsers = MutableStateFlow<List<User>>(emptyList())
    val typingUsers: StateFlow<List<User>> = _typingUsers.asStateFlow()
    
    // ========================================
    // Smart Replies State
    // ========================================
    
    /**
     * The current list of smart reply suggestions.
     *
     * This list is populated when [fetchSmartReplies] is called and cleared
     * when [clearSmartReplies] is called or when a new message is sent.
     */
    private val _smartReplies = MutableStateFlow<List<String>>(emptyList())
    val smartReplies: StateFlow<List<String>> = _smartReplies.asStateFlow()
    
    /**
     * The current UI state for smart replies.
     *
     * States:
     * - [SmartRepliesUIState.Idle]: No request in progress
     * - [SmartRepliesUIState.Loading]: Fetching smart replies
     * - [SmartRepliesUIState.Loaded]: Smart replies available
     * - [SmartRepliesUIState.Error]: Fetch failed
     */
    private val _smartRepliesUIState = MutableStateFlow<SmartRepliesUIState>(SmartRepliesUIState.Idle)
    val smartRepliesUIState: StateFlow<SmartRepliesUIState> = _smartRepliesUIState.asStateFlow()
    
    // ========================================
    // Conversation Starter State
    // ========================================
    
    /**
     * The current list of conversation starter suggestions.
     *
     * This list is populated when [fetchConversationStarter] is called and
     * cleared when [clearConversationStarter] is called or when a message is sent.
     */
    private val _conversationStarterReplies = MutableStateFlow<List<String>>(emptyList())
    val conversationStarterReplies: StateFlow<List<String>> = _conversationStarterReplies.asStateFlow()
    
    /**
     * The current UI state for conversation starters.
     *
     * States:
     * - [ConversationStarterUIState.Idle]: No request in progress
     * - [ConversationStarterUIState.Loading]: Fetching conversation starters
     * - [ConversationStarterUIState.Loaded]: Conversation starters available
     * - [ConversationStarterUIState.Error]: Fetch failed
     */
    private val _conversationStarterUIState = MutableStateFlow<ConversationStarterUIState>(ConversationStarterUIState.Idle)
    val conversationStarterUIState: StateFlow<ConversationStarterUIState> = _conversationStarterUIState.asStateFlow()
    
    /**
     * Event emitted when conversation starters should be removed from UI.
     *
     * This is emitted when a message is added to the conversation, indicating
     * that the conversation has started and starters are no longer needed.
     */
    private val _removeConversationStarter = MutableSharedFlow<Unit>()
    val removeConversationStarter: SharedFlow<Unit> = _removeConversationStarter.asSharedFlow()
    
    // ========================================
    // Conversation Summary State
    // ========================================
    
    /**
     * The current conversation summary text.
     *
     * This is populated when [fetchConversationSummary] is called and cleared
     * when [dismissConversationSummary] is called.
     */
    private val _conversationSummary = MutableStateFlow<String?>(null)
    val conversationSummary: StateFlow<String?> = _conversationSummary.asStateFlow()
    
    /**
     * The current UI state for conversation summary.
     *
     * States:
     * - [ConversationSummaryUIState.Idle]: No request in progress
     * - [ConversationSummaryUIState.Loading]: Fetching conversation summary
     * - [ConversationSummaryUIState.Loaded]: Conversation summary available
     * - [ConversationSummaryUIState.Error]: Fetch failed
     */
    private val _conversationSummaryUIState = MutableStateFlow<ConversationSummaryUIState>(ConversationSummaryUIState.Idle)
    val conversationSummaryUIState: StateFlow<ConversationSummaryUIState> = _conversationSummaryUIState.asStateFlow()
    
    /**
     * Event emitted when conversation summary should be removed from UI.
     *
     * This is emitted when the user dismisses the summary.
     */
    private val _removeConversationSummary = MutableSharedFlow<Unit>()
    val removeConversationSummary: SharedFlow<Unit> = _removeConversationSummary.asSharedFlow()
    
    // Events
    
    /**
     * Event emitted when the UI should scroll to the bottom of the message list.
     *
     * This is emitted when a new message is received and the user is at the bottom.
     */
    private val _scrollToBottomEvent = MutableSharedFlow<Unit>()
    val scrollToBottomEvent: SharedFlow<Unit> = _scrollToBottomEvent.asSharedFlow()
    
    /**
     * Event emitted when a message sound should be played.
     *
     * The boolean value indicates whether to play the sound (`true`) or not.
     */
    private val _playSoundEvent = MutableSharedFlow<Boolean>()
    val playSoundEvent: SharedFlow<Boolean> = _playSoundEvent.asSharedFlow()
    
    /**
     * Event emitted when a message has been successfully marked as read.
     *
     * UI layers should observe this event to:
     * - Trigger UIKit helper notifications (e.g., `CometChatUIKitHelper.onMessageRead()`)
     * - Update any external state that depends on read receipt success
     * - Synchronize read status with other UI components
     *
     * This event is emitted after:
     * - [markLastMessageAsRead] successfully marks a message as read
     * - [markConversationRead] successfully marks the conversation as read
     *
     * The emitted [BaseMessage] contains the message that was marked as read,
     * with its `readAt` timestamp updated to reflect the read time.
     *
     * Example usage in Compose:
     * ```kotlin
     * LaunchedEffect(Unit) {
     *     viewModel.messageReadEvent.collect { message ->
     *         CometChatUIKitHelper.onMessageRead(message)
     *     }
     * }
     * ```
     *
     * Example usage in XML Views:
     * ```kotlin
     * lifecycleScope.launch {
     *     viewModel.messageReadEvent.collect { message ->
     *         CometChatUIKitHelper.onMessageRead(message)
     *     }
     * }
     * ```
     *
     * @see markLastMessageAsRead
     * @see markConversationRead
     */
    private val _messageReadEvent = MutableSharedFlow<BaseMessage>()
    val messageReadEvent: SharedFlow<BaseMessage> = _messageReadEvent.asSharedFlow()
    
    /**
     * Event emitted when a message is deleted via UIKit events.
     *
     * UI layers should observe this event to update related components
     * when a message is deleted from another component (e.g., message actions menu).
     *
     * @see handleMessageDeletedEvent
     */
    private val _messageDeleted = MutableSharedFlow<BaseMessage>()
    val messageDeleted: SharedFlow<BaseMessage> = _messageDeleted.asSharedFlow()

    /**
     * Event emitted when a message is updated in-place (e.g., reply count changed).
     *
     * StateFlow conflation can suppress emissions when the same object reference is
     * mutated in-place. This SharedFlow guarantees delivery so the UI layer can
     * call notifyItemChanged / trigger recomposition for the affected message.
     */
    private val _messageUpdated = MutableSharedFlow<BaseMessage>()
    val messageUpdated: SharedFlow<BaseMessage> = _messageUpdated.asSharedFlow()
    
    /**
     * Event emitted when a message sender's full user details are fetched.
     *
     * UI can observe this to navigate to user profile or start a conversation.
     * This event is emitted by [fetchMessageSender] after successfully fetching
     * the user details from the CometChat server.
     *
     * @see fetchMessageSender
     */
    private val _messageSenderFetched = MutableSharedFlow<User>()
    val messageSenderFetched: SharedFlow<User> = _messageSenderFetched.asSharedFlow()
    
    /**
     * Event emitted when a message needs special processing.
     *
     * This is used for custom message type handling where the UI
     * needs to perform additional processing on the message data.
     * UI can observe this to handle messages that require special
     * treatment (e.g., custom interactive messages, form messages, etc.).
     *
     * @see emitProcessMessageData
     */
    private val _processMessageData = MutableSharedFlow<BaseMessage>()
    val processMessageData: SharedFlow<BaseMessage> = _processMessageData.asSharedFlow()
    
    /** Job for UIKit message events subscription. */
    private var messageEventsJob: Job? = null
    
    /** Job for UIKit group events subscription. */
    private var groupEventsJob: Job? = null
    
    /** Job for UIKit call events subscription. */
    private var callEventsJob: Job? = null
    
    // ========================================
    // Navigation State
    // ========================================
    
    /**
     * Track the latest message ID for real-time message guards.
     *
     * This is used to determine if the user is at the "latest" position in the
     * conversation. When a real-time message is received, it is only added to
     * the list if the user is at the latest position (i.e., the last message ID
     * equals this value or the list is empty).
     *
     * Updated when:
     * - [fetchMessagesWithUnreadCount] fetches the conversation's last message
     * - A new message is successfully added to the list
     */
    private var latestMessageId: Long = -1
    
    /**
     * Track the last read message ID for unread anchor detection.
     *
     * This is used to find the first unread message in the conversation,
     * which is displayed as an "unread messages" anchor/separator.
     *
     * Updated when [fetchMessagesWithUnreadCount] fetches the conversation metadata.
     */
    private var lastReadMessageId: Long = -1
    
    /**
     * Whether to highlight the scrolled-to message.
     *
     * When [goToMessage] is called with `highlight = true`, this flow emits `true`
     * to indicate the UI should visually highlight the target message.
     * The UI should call [clearHighlightScroll] after the highlight animation completes.
     */
    private val _highlightScroll = MutableStateFlow(false)
    val highlightScroll: StateFlow<Boolean> = _highlightScroll.asStateFlow()
    
    /**
     * The configured message ID to navigate to on initial load.
     *
     * When set to a value > 0, [fetchMessagesWithUnreadCount] will call
     * [goToMessage] with this ID instead of starting from the most recent
     * or unread messages.
     *
     * This is typically set via [setUser] or [setGroup] parameters.
     */
    private var gotoMessageId: Long = 0
    
    // ========================================
    // Configuration
    // ========================================
    
    /** The user for 1-on-1 conversations, or `null` for group conversations. */
    private var user: User? = null
    
    /** The group for group conversations, or `null` for 1-on-1 conversations. */
    private var group: Group? = null
    
    /** Parent message ID for threaded conversations, or `-1` for main conversation. */
    private var parentMessageId: Long = -1
    
    /** Whether to disable sending read receipts for viewed messages. */
    private var disableReceipt: Boolean = false
    
    /** Whether to disable reaction event handling. */
    private var disableReactions: Boolean = false
    
    /** Whether to completely hide deleted messages instead of showing "message deleted". */
    private var hideDeleteMessage: Boolean = false
    
    /** Whether to start from the first unread message instead of the most recent. */
    private var startFromUnreadMessages: Boolean = false
    
    /** Minimum unread count to trigger starting from unread messages. */
    private var unreadThreshold: Int = 30
    
    /** Whether to disable playing sounds for incoming messages. */
    private var disableSoundForMessages: Boolean = false
    
    /** Custom sound resource ID for incoming messages, or 0 for default. */
    @RawRes private var customSoundForMessages: Int = 0
    
    /** Unique tag for CometChat listeners, used for cleanup. */
    private var listenersTag: String? = null
    
    /** Sound manager for playing message sounds. */
    private var soundManager: CometChatSoundManager? = null
    
    /** Whether to enable AI conversation starters when the message list is empty. */
    private var enableConversationStarter: Boolean = false
    
    /** Whether to enable AI smart replies for incoming messages. */
    private var enableSmartReplies: Boolean = false
    
    /** Delay in milliseconds before fetching smart replies after receiving a message. */
    private var smartRepliesDelay: Int = 10000
    
    /** Keywords to filter messages for smart replies. Empty list means all messages. */
    private var smartReplyKeywords: List<String> = emptyList()
    
    /** Job for the delayed smart replies fetch. */
    private var smartRepliesJob: Job? = null
    
    /** Whether to enable AI conversation summary feature. */
    private var enableConversationSummary: Boolean = true
    
    /** 
     * Flag to track if this is the first fetch operation.
     * Used to determine when to mark the conversation as read on initial load.
     */
    private var firstFetch: Boolean = true

    /**
     * The configured message types for filtering messages.
     * Initialized with defaults and updated by setUser()/setGroup().
     */
    private var messagesTypes: List<String> = getDefaultMessagesTypes()

    /**
     * The configured message categories for filtering messages.
     * Initialized with defaults and updated by setUser()/setGroup().
     */
    private var messagesCategories: List<String> = getDefaultMessagesCategories()

    // ========================================
    // Custom Message Options
    // ========================================

    /**
     * Callback that completely replaces the default options for a message.
     *
     * When set, this callback is invoked with the [BaseMessage] being long-pressed.
     * - If it returns a non-null list, that list is used as the **entire** set of options
     *   (default options are discarded).
     * - If it returns `null`, the default options are shown as usual.
     *
     * Takes precedence over [addOptionsCallback] — when [setOptions] returns a non-null
     * list, [addOptions] is **not** invoked.
     */
    private var setOptionsCallback: ((BaseMessage) -> List<CometChatMessageOption>?)? = null

    /**
     * Callback that appends extra options after the default (or filtered) options.
     *
     * Invoked only when [setOptionsCallback] is `null` or returns `null`.
     * The returned list is concatenated to the end of the default options.
     */
    private var addOptionsCallback: ((BaseMessage) -> List<CometChatMessageOption>)? = null

    /**
     * Sets a callback that **replaces** the default message options for a given message.
     *
     * @param callback A function that receives the [BaseMessage] and returns either:
     *   - A non-null list of [CometChatMessageOption] to **replace** all default options, or
     *   - `null` to fall back to the default options.
     */
    fun setOptions(callback: (BaseMessage) -> List<CometChatMessageOption>?) {
        setOptionsCallback = callback
    }

    /**
     * Sets a callback that **appends** additional options to the default message options.
     *
     * @param callback A function that receives the [BaseMessage] and returns a list of
     *   [CometChatMessageOption] to append after the default options.
     */
    fun addOptions(callback: (BaseMessage) -> List<CometChatMessageOption>) {
        addOptionsCallback = callback
    }

    /**
     * Resolves the final list of message options for a given message.
     *
     * Resolution order:
     * 1. If [setOptionsCallback] is set and returns a non-null list → use that list as-is.
     * 2. Otherwise, use [defaultOptions] and append the result of [addOptionsCallback] (if set).
     *
     * @param message The message being long-pressed.
     * @param defaultOptions The default options (already filtered by visibility settings).
     * @return The final list of options to display.
     */
    fun resolveMessageOptions(
        message: BaseMessage,
        defaultOptions: List<CometChatMessageOption>
    ): List<CometChatMessageOption> {
        // setOptions takes full precedence when it returns non-null
        setOptionsCallback?.invoke(message)?.let { return it }

        // Otherwise use defaults + addOptions
        val additional = addOptionsCallback?.invoke(message) ?: emptyList()
        return defaultOptions + additional
    }

    // ========================================
    // Default Message Option Click Handling
    // ========================================

    /**
     * Event emitted when a delete confirmation is requested for a message.
     *
     * The presentation layer should observe this event to show a confirmation dialog.
     * When the user confirms, the presentation layer should call [deleteMessage] to
     * perform the actual deletion.
     *
     * @see handleMessageOptionClick
     * @see deleteMessage
     */
    private val _deleteConfirmationRequest = MutableSharedFlow<BaseMessage>()
    val deleteConfirmationRequest: SharedFlow<BaseMessage> = _deleteConfirmationRequest.asSharedFlow()

    /**
     * Event emitted when a message translation completes successfully.
     *
     * The presentation layer should observe this event to update the message bubble
     * with the translated text. The emitted [BaseMessage] has its metadata updated
     * with the `translated_message` field.
     *
     * @see handleMessageOptionClick
     */
    private val _messageTranslated = MutableSharedFlow<BaseMessage>()
    val messageTranslated: SharedFlow<BaseMessage> = _messageTranslated.asSharedFlow()

    /**
     * Event emitted when an error occurs during message operations.
     *
     * The presentation layer should observe this event to display error messages
     * to the user. This is used for errors that occur during operations like
     * translation, deletion, etc.
     *
     * @see translateMessage
     * @see deleteMessage
     */
    private val _onError = MutableSharedFlow<CometChatException>()
    val onError: SharedFlow<CometChatException> = _onError.asSharedFlow()

    /**
     * Functional interface for formatting message text.
     *
     * This callback allows the presentation layer to provide text formatting logic
     * (e.g., applying mention formatters, markdown rendering) without coupling the
     * ViewModel to UI-specific formatter implementations.
     *
     * The callback receives:
     * - [context]: Android context for resource access
     * - [message]: The message being formatted
     * - [formattingType]: The type of formatting to apply (MESSAGE_BUBBLE, MESSAGE_COMPOSER, etc.)
     * - [alignment]: The message bubble alignment (RIGHT for sent, LEFT for received)
     * - [text]: The original text to format
     *
     * @return The formatted text as a [CharSequence] (may include spans for styling)
     */
    fun interface TextFormatterCallback {
        fun formatText(
            context: Context,
            message: BaseMessage,
            formattingType: UIKitConstants.FormattingType,
            alignment: UIKitConstants.MessageBubbleAlignment,
            text: String
        ): CharSequence
    }

    /**
     * Handles a default message option click by dispatching to the appropriate
     * business-logic action.
     *
     * This method handles options that don't require UI context (Activity, navigation,
     * dialogs). Options that require UI context (REPLY_IN_THREAD, SHARE,
     * MESSAGE_INFORMATION, REPORT, MESSAGE_PRIVATELY) are NOT handled here and
     * return `false` so the presentation layer can handle them.
     *
     * @param context The Android context, needed for clipboard operations and SDK calls.
     * @param optionId The option ID from [UIKitConstants.MessageOption].
     * @param message The [BaseMessage] the option was clicked on.
     * @param textFormatterCallback Optional callback for formatting text when copying.
     *        If provided, the formatted text will be copied to clipboard instead of raw text.
     *        This enables proper handling of mentions, markdown, and other text formatting.
     * @return `true` if the option was handled by the ViewModel, `false` if it should
     *         be handled by the presentation layer.
     *
     * @see onMessageEdit
     * @see onMessageReply
     * @see markMessageAsUnread
     * @see deleteMessage
     */
    fun handleMessageOptionClick(
        context: Context,
        optionId: String,
        message: BaseMessage,
        textFormatterCallback: TextFormatterCallback? = null
    ): Boolean {
        when (optionId) {
            UIKitConstants.MessageOption.COPY -> copyMessage(context, message, textFormatterCallback)
            UIKitConstants.MessageOption.EDIT -> onMessageEdit(message)
            UIKitConstants.MessageOption.REPLY,
            UIKitConstants.MessageOption.REPLY_TO_MESSAGE -> onMessageReply(message)
            UIKitConstants.MessageOption.DELETE -> requestDeleteMessage(message)
            UIKitConstants.MessageOption.MARK_AS_UNREAD -> markMessageAsUnread(message)
            UIKitConstants.MessageOption.TRANSLATE -> translateMessage(message)
            else -> return false
        }
        return true
    }

    /**
     * Copies the text content of a message to the system clipboard.
     *
     * If a [textFormatterCallback] is provided, the formatted text (with mentions,
     * markdown, etc. applied) will be copied. Otherwise, the raw message text is copied.
     *
     * Only copies text from [TextMessage] instances. For other message types,
     * this method is a no-op.
     *
     * @param context The Android context for accessing [android.content.ClipboardManager].
     * @param message The [BaseMessage] to copy text from.
     * @param textFormatterCallback Optional callback for formatting the text before copying.
     *        Uses [UIKitConstants.FormattingType.MESSAGE_BUBBLE] and
     *        [UIKitConstants.MessageBubbleAlignment.RIGHT] for formatting.
     */
    private fun copyMessage(
        context: Context,
        message: BaseMessage,
        textFormatterCallback: TextFormatterCallback? = null
    ) {
        if (message is TextMessage) {
            val rawText = message.text ?: return
            
            // Use formatted text if formatter callback is provided, otherwise use raw text
            val textToCopy: CharSequence = textFormatterCallback?.formatText(
                context,
                message,
                UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                UIKitConstants.MessageBubbleAlignment.RIGHT,
                rawText
            )
                ?: rawText
            
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("message", textToCopy.toString())
            clipboard?.setPrimaryClip(clip)
        }
    }

    /**
     * Emits a delete confirmation request for the given message.
     *
     * The presentation layer should observe [deleteConfirmationRequest] to show
     * a confirmation dialog. When confirmed, call [deleteMessage] to perform
     * the actual deletion.
     *
     * @param message The [BaseMessage] to request deletion for.
     *
     * @see deleteConfirmationRequest
     * @see deleteMessage
     */
    private fun requestDeleteMessage(message: BaseMessage) {
        viewModelScope.launch {
            _deleteConfirmationRequest.emit(message)
        }
    }

    /**
     * Translates a message using the CometChat message translation extension.
     *
     * Calls the `message-translation` extension via [CometChat.callExtension] to
     * translate the message text. On success, updates the message metadata with
     * the `translated_message` field and emits the updated message via
     * [messageTranslated] and updates it in the message list.
     *
     * @param message The [BaseMessage] to translate.
     *
     * @see messageTranslated
     */
    private fun translateMessage(message: BaseMessage) {
        if (message !is TextMessage) return

        viewModelScope.launch {
            try {
                val body = org.json.JSONObject()
                body.put("msgId", message.id)
                body.put("text", message.text)
                body.put("languages", org.json.JSONArray().put(java.util.Locale.getDefault().language))

                val result = suspendCancellableCoroutine { cont ->
                    CometChat.callExtension(
                        "message-translation",
                        "POST",
                        "/v2/translate",
                        body,
                        object : CometChat.CallbackListener<org.json.JSONObject>() {
                            override fun onSuccess(response: org.json.JSONObject) {
                                cont.resume(response)
                            }

                            override fun onError(e: CometChatException) {
                                cont.resumeWithException(e)
                            }
                        }
                    )
                }

                val translations = result
                    .optJSONObject("data")
                    ?.optJSONArray("translations")

                if (translations != null && translations.length() > 0) {
                    val translatedText = translations
                        .getJSONObject(0)
                        .optString("message_translated", "")

                    if (translatedText.isNotEmpty()) {
                        val metadata = message.metadata ?: org.json.JSONObject()
                        metadata.put("translated_message", translatedText)
                        message.metadata = metadata

                        updateMessage(message)
                        _messageTranslated.emit(message)
                    }
                }
            } catch (e: Exception) {
                _onError.emit(
                    e as? CometChatException ?: CometChatException(
                        "TRANSLATION_ERROR",
                        e.message ?: "Translation failed"
                    )
                )
            }
        }
    }

    
    // ========================================
    // Public Configuration Methods
    // ========================================
    
    /**
     * Configures the ViewModel for a user (1-on-1) conversation.
     *
     * This method:
     * 1. Stores the user reference and clears any group reference
     * 2. Configures the repository with the user and message filters
     * 3. Registers CometChat listeners for real-time updates (if enabled)
     * 4. Optionally scrolls to a specific message
     *
     * @param user The [User] to display messages for.
     * @param parentMessageId Parent message ID for threaded conversations.
     *                        Use `-1` for the main conversation (default).
     * @param gotoMessageId Optional message ID to scroll to after loading.
     *                      Use `0` to not scroll to any specific message (default).
     * @param messagesRequestBuilder Optional custom [MessagesRequest.MessagesRequestBuilder]
     *                               for advanced message filtering.
     *
     * @see setGroup
     */
    fun setUser(
        user: User,
        parentMessageId: Long = -1,
        gotoMessageId: Long = 0,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder? = null
    ) {
        this.user = user
        this.group = null
        this.parentMessageId = parentMessageId
        this.gotoMessageId = gotoMessageId
        
        // Always use defaults for types and categories
        val effectiveTypes = getDefaultMessagesTypes()
        val effectiveCategories = getDefaultMessagesCategories()
        
        // Store types and categories for getter access
        this.messagesTypes = effectiveTypes
        this.messagesCategories = effectiveCategories
        
        // Regenerate ID map after configuration change
        _idMap.value = generateIdMap()
        
        repository.configureForUser(user, effectiveTypes, effectiveCategories, parentMessageId, messagesRequestBuilder)
        
        // Always subscribe to UIKit local events (these don't depend on SDK)
        removeLocalEventListeners()
        addLocalEventListeners()
        
        if (enableListeners) {
            removeListeners()
            addListeners()
        }
    }
    
    /**
     * Configures the ViewModel for a group conversation.
     *
     * This method:
     * 1. Stores the group reference and clears any user reference
     * 2. Configures the repository with the group and message filters
     * 3. Registers CometChat listeners for real-time updates (if enabled)
     * 4. Optionally scrolls to a specific message
     *
     * @param group The [Group] to display messages for.
     * @param parentMessageId Parent message ID for threaded conversations.
     *                        Use `-1` for the main conversation (default).
     * @param gotoMessageId Optional message ID to scroll to after loading.
     *                      Use `0` to not scroll to any specific message (default).
     * @param messagesRequestBuilder Optional custom [MessagesRequest.MessagesRequestBuilder]
     *                               for advanced message filtering.
     *
     * @see setUser
     */
    fun setGroup(
        group: Group,
        parentMessageId: Long = -1,
        gotoMessageId: Long = 0,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder? = null
    ) {
        this.group = group
        this.user = null
        this.parentMessageId = parentMessageId
        this.gotoMessageId = gotoMessageId
        
        // Always use defaults for types and categories
        val effectiveTypes = getDefaultMessagesTypes()
        val effectiveCategories = getDefaultMessagesCategories()
        
        // Store types and categories for getter access
        this.messagesTypes = effectiveTypes
        this.messagesCategories = effectiveCategories
        
        // Regenerate ID map after configuration change
        _idMap.value = generateIdMap()
        
        repository.configureForGroup(group, effectiveTypes, effectiveCategories, parentMessageId, messagesRequestBuilder)
        
        // Always subscribe to UIKit local events (these don't depend on SDK)
        removeLocalEventListeners()
        addLocalEventListeners()
        
        if (enableListeners) {
            removeListeners()
            addListeners()
        }
    }
    
    /**
     * Sets whether to disable sending read receipts.
     *
     * When `true`, the ViewModel will not send read receipts when messages are viewed.
     * This is useful for privacy-focused applications.
     *
     * @param disable `true` to disable read receipts, `false` to enable (default).
     */
    fun setDisableReceipt(disable: Boolean) { disableReceipt = disable }
    
    /**
     * Sets whether to disable reaction event handling.
     *
     * When `true`, reaction added and removed events will be ignored,
     * and the message list will not update when reactions change.
     * This is useful for applications that don't want to show reactions
     * or want to implement custom reaction handling.
     *
     * Note: This only affects event handling. The [addReaction] and
     * [removeReaction] methods will still work - they just won't
     * trigger automatic list updates via events.
     *
     * @param disable `true` to disable reaction handling, `false` to enable (default).
     */
    fun setDisableReactions(disable: Boolean) { disableReactions = disable }
    
    /**
     * Sets whether to completely hide deleted messages.
     *
     * When `true`, deleted messages are removed from the list entirely.
     * When `false` (default), deleted messages show "This message was deleted".
     *
     * @param hide `true` to hide deleted messages, `false` to show placeholder.
     */
    fun setHideDeleteMessage(hide: Boolean) { hideDeleteMessage = hide }
    
    /**
     * Sets whether to start from the first unread message.
     *
     * When `true` and there are unread messages above the threshold,
     * the message list will scroll to the first unread message on load.
     *
     * @param start `true` to start from unread messages, `false` to start from most recent.
     *
     * @see setUnreadThreshold
     */
    fun setStartFromUnreadMessages(start: Boolean) { startFromUnreadMessages = start }
    
    /**
     * Sets the minimum unread count to trigger starting from unread messages.
     *
     * Only applies when [setStartFromUnreadMessages] is `true`.
     *
     * @param threshold Minimum unread count (default is 30).
     */
    fun setUnreadThreshold(threshold: Int) { unreadThreshold = threshold }
    
    /**
     * Sets whether to disable playing sounds for incoming messages.
     *
     * @param disable `true` to disable sounds, `false` to enable (default).
     */
    fun setDisableSoundForMessages(disable: Boolean) { disableSoundForMessages = disable }
    
    /**
     * Sets a custom sound resource for incoming messages.
     *
     * @param rawRes Raw resource ID of the sound file, or `0` for default sound.
     */
    fun setCustomSoundForMessages(@RawRes rawRes: Int) { customSoundForMessages = rawRes }
    
    /**
     * Sets whether to enable AI conversation starters.
     *
     * When `true` and the message list is empty, AI-generated conversation
     * starter suggestions will be fetched and displayed to help users
     * begin conversations.
     *
     * @param enable `true` to enable conversation starters, `false` to disable (default).
     *
     * @see setEnableSmartReplies
     * @see fetchConversationStarter
     * @see clearConversationStarter
     */
    fun setEnableConversationStarter(enable: Boolean) { 
        enableConversationStarter = enable 
        if (!enable) {
            clearConversationStarter()
        }
    }
    
    /**
     * Sets whether smart replies feature is enabled.
     *
     * When enabled, smart reply suggestions will be fetched automatically
     * when text messages are received from other users.
     *
     * @param enable `true` to enable smart replies, `false` to disable.
     *
     * @see setSmartRepliesDelay
     * @see setSmartReplyKeywords
     */
    fun setEnableSmartReplies(enable: Boolean) {
        enableSmartReplies = enable
        if (!enable) {
            clearSmartReplies()
        }
    }
    
    /**
     * Sets the delay before fetching smart replies after receiving a message.
     *
     * This delay allows for multiple messages to arrive before triggering
     * the smart replies fetch. If another message arrives before the delay
     * expires, the timer is reset.
     *
     * @param delayMs Delay in milliseconds. Default is 10000 (10 seconds).
     */
    fun setSmartRepliesDelay(delayMs: Int) {
        smartRepliesDelay = delayMs
    }
    
    /**
     * Sets keywords to filter messages for smart replies.
     *
     * When keywords are set, smart replies will only be fetched if the
     * received message contains at least one of the keywords (case-insensitive).
     * An empty list means all messages will trigger smart replies.
     *
     * @param keywords List of keywords to filter messages. Empty list for no filtering.
     */
    fun setSmartReplyKeywords(keywords: List<String>) {
        smartReplyKeywords = keywords
    }
    
    // ========================================
    // Smart Replies Methods
    // ========================================
    
    /**
     * Fetches AI-generated smart reply suggestions for the current conversation.
     *
     * This method calls [CometChat.getSmartReplies] to get contextual reply
     * suggestions based on the conversation history.
     *
     * The method:
     * 1. Checks if smart replies are enabled
     * 2. Sets UI state to Loading
     * 3. Calls the CometChat API
     * 4. On success: Updates [smartReplies] and sets state to Loaded
     * 5. On failure: Sets state to Error
     *
     * @see clearSmartReplies
     */
    fun fetchSmartReplies() {
        if (!enableSmartReplies) return
        
        val receiverId = user?.uid ?: group?.guid ?: return
        val receiverType = if (user != null) {
            CometChatConstants.RECEIVER_TYPE_USER
        } else {
            CometChatConstants.RECEIVER_TYPE_GROUP
        }
        
        _smartRepliesUIState.value = SmartRepliesUIState.Loading
        
        viewModelScope.launch {
            try {
                val result = suspendCancellableCoroutine<HashMap<String, String>> { continuation ->
                    CometChat.getSmartReplies(
                        receiverId,
                        receiverType,
                        null, // configuration - use default
                        object : CometChat.CallbackListener<HashMap<String, String>>() {
                            override fun onSuccess(response: HashMap<String, String>) {
                                continuation.resume(response)
                            }
                            
                            override fun onError(exception: CometChatException) {
                                continuation.resumeWithException(exception)
                            }
                        }
                    )
                }
                
                // Extract values from the HashMap as a list
                val replies = result.values.toList()
                _smartReplies.value = replies
                _smartRepliesUIState.value = SmartRepliesUIState.Loaded(replies)
                
            } catch (e: CometChatException) {
                _smartRepliesUIState.value = SmartRepliesUIState.Error(e)
            }
        }
    }
    
    /**
     * Clears the current smart reply suggestions.
     *
     * This method:
     * 1. Cancels any pending smart replies fetch
     * 2. Clears the [smartReplies] list
     * 3. Sets UI state to Idle
     *
     * Called automatically when:
     * - A new message is sent
     * - Smart replies are disabled
     * - Conversation starter is removed
     */
    fun clearSmartReplies() {
        smartRepliesJob?.cancel()
        smartRepliesJob = null
        _smartReplies.value = emptyList()
        _smartRepliesUIState.value = SmartRepliesUIState.Idle
    }
    
    /**
     * Schedules a smart replies fetch after receiving a message.
     *
     * This method implements a debounce pattern - if another message arrives
     * before the delay expires, the previous job is cancelled and a new one
     * is scheduled.
     *
     * @param message The received text message.
     */
    private fun scheduleSmartRepliesFetch(message: TextMessage) {
        // Cancel any existing scheduled fetch
        smartRepliesJob?.cancel()
        
        // Schedule new fetch with delay
        smartRepliesJob = viewModelScope.launch {
            delay(smartRepliesDelay.toLong())
            fetchSmartReplies()
        }
    }
    
    /**
     * Checks if smart replies should be fetched for a message.
     *
     * Smart replies are fetched if:
     * 1. Smart replies are enabled
     * 2. Message is a TextMessage
     * 3. Message is from another user (not own message)
     * 4. We're in main conversation (not thread view)
     * 5. Message contains a keyword (if keywords are configured)
     *
     * @param message The message to check.
     * @return `true` if smart replies should be fetched, `false` otherwise.
     */
    private fun shouldFetchSmartReplies(message: BaseMessage): Boolean {
        // Check if enabled
        if (!enableSmartReplies) return false
        
        // Check if TextMessage
        if (message !is TextMessage) return false
        
        // Check if from another user
        if (message.sender?.uid == getLoggedInUserUid()) return false
        
        // Check if in main conversation
        if (parentMessageId != -1L) return false
        
        // Check keywords if configured
        if (smartReplyKeywords.isNotEmpty()) {
            val messageText = message.text.lowercase()
            return smartReplyKeywords.any { keyword ->
                messageText.contains(keyword.lowercase())
            }
        }
        
        return true
    }
    
    // ========================================
    // Conversation Starter Methods
    // ========================================
    
    /**
     * Fetches AI-generated conversation starter suggestions for the current conversation.
     *
     * This method calls [CometChat.getConversationStarter] to get suggestions
     * for starting a new conversation.
     *
     * The method:
     * 1. Checks if conversation starters are enabled
     * 2. Checks if we're in main conversation (not thread)
     * 3. Sets UI state to Loading
     * 4. Calls the CometChat API
     * 5. On success: Updates [conversationStarterReplies] and sets state to Loaded
     * 6. On failure: Sets state to Error
     *
     * @see clearConversationStarter
     */
    fun fetchConversationStarter() {
        if (!enableConversationStarter) return
        
        // Only fetch in main conversation
        if (parentMessageId != -1L) return
        
        val receiverId = user?.uid ?: group?.guid ?: return
        val receiverType = if (user != null) {
            CometChatConstants.RECEIVER_TYPE_USER
        } else {
            CometChatConstants.RECEIVER_TYPE_GROUP
        }
        
        _conversationStarterUIState.value = ConversationStarterUIState.Loading
        
        viewModelScope.launch {
            try {
                val result = suspendCancellableCoroutine<List<String>> { continuation ->
                    CometChat.getConversationStarter(
                        receiverId,
                        receiverType,
                        null, // configuration - use default
                        object : CometChat.CallbackListener<List<String>>() {
                            override fun onSuccess(response: List<String>) {
                                continuation.resume(response)
                            }
                            
                            override fun onError(exception: CometChatException) {
                                continuation.resumeWithException(exception)
                            }
                        }
                    )
                }
                
                _conversationStarterReplies.value = result
                _conversationStarterUIState.value = ConversationStarterUIState.Loaded(result)
                
            } catch (e: CometChatException) {
                _conversationStarterUIState.value = ConversationStarterUIState.Error(e)
            }
        }
    }
    
    /**
     * Clears the current conversation starter suggestions.
     *
     * This method:
     * 1. Clears the [conversationStarterReplies] list
     * 2. Sets UI state to Idle
     * 3. Emits [removeConversationStarter] event
     *
     * Called automatically when:
     * - A message is added to the conversation
     * - Conversation starters are disabled
     */
    fun clearConversationStarter() {
        _conversationStarterReplies.value = emptyList()
        _conversationStarterUIState.value = ConversationStarterUIState.Idle
        
        viewModelScope.launch {
            _removeConversationStarter.emit(Unit)
        }
    }
    
    /**
     * Checks conditions and fetches conversation starters if appropriate.
     *
     * Conversation starters are fetched if:
     * 1. Feature is enabled
     * 2. Message list is empty
     * 3. We're in main conversation (not thread)
     */
    private fun checkAndFetchConversationStarter() {
        if (!enableConversationStarter) return
        if (_messages.value.isNotEmpty()) return
        if (parentMessageId != -1L) return
        
        fetchConversationStarter()
    }
    
    /**
     * Sets whether to enable AI conversation summary.
     *
     * When `true`, AI-generated conversation summaries will be available
     * to provide quick overviews of the conversation when there are many
     * unread messages (above [unreadThreshold]).
     *
     * When set to `false`, any existing conversation summary is dismissed.
     *
     * @param enable `true` to enable conversation summary, `false` to disable.
     *               Default is `true` (matching Java implementation).
     *
     * @see fetchConversationSummary
     * @see dismissConversationSummary
     * @see setUnreadThreshold
     */
    fun setEnableConversationSummary(enable: Boolean) { 
        enableConversationSummary = enable 
        if (!enable) {
            dismissConversationSummary()
        }
    }
    
    // ========================================
    // Conversation Summary Methods
    // ========================================
    
    /**
     * Fetches AI-generated conversation summary for the current conversation.
     *
     * This method calls [CometChat.getConversationSummary] to get a summary
     * of the conversation history.
     *
     * The method:
     * 1. Checks if conversation summary is enabled
     * 2. Checks if we're in main conversation (not thread)
     * 3. Sets UI state to Loading
     * 4. Calls the CometChat API
     * 5. On success: Updates [conversationSummary] and sets state to Loaded
     * 6. On failure: Sets state to Error
     *
     * @see dismissConversationSummary
     */
    fun fetchConversationSummary() {
        if (!enableConversationSummary) return
        
        // Only fetch in main conversation
        if (parentMessageId != -1L) return
        
        val receiverId = user?.uid ?: group?.guid ?: return
        val receiverType = if (user != null) {
            CometChatConstants.RECEIVER_TYPE_USER
        } else {
            CometChatConstants.RECEIVER_TYPE_GROUP
        }
        
        _conversationSummaryUIState.value = ConversationSummaryUIState.Loading
        
        viewModelScope.launch {
            try {
                val result = suspendCancellableCoroutine<String> { continuation ->
                    CometChat.getConversationSummary(
                        receiverId,
                        receiverType,
                        object : CometChat.CallbackListener<String>() {
                            override fun onSuccess(response: String) {
                                continuation.resume(response)
                            }
                            
                            override fun onError(exception: CometChatException) {
                                continuation.resumeWithException(exception)
                            }
                        }
                    )
                }
                
                _conversationSummary.value = result
                _conversationSummaryUIState.value = ConversationSummaryUIState.Loaded(result)
                
            } catch (e: CometChatException) {
                _conversationSummaryUIState.value = ConversationSummaryUIState.Error(e)
            }
        }
    }
    
    /**
     * Dismisses the current conversation summary.
     *
     * This method:
     * 1. Clears the [conversationSummary]
     * 2. Sets UI state to Idle
     * 3. Emits [removeConversationSummary] event
     *
     * Called when:
     * - User manually dismisses the summary
     * - Conversation summary is disabled
     */
    fun dismissConversationSummary() {
        _conversationSummary.value = null
        _conversationSummaryUIState.value = ConversationSummaryUIState.Idle
        
        viewModelScope.launch {
            _removeConversationSummary.emit(Unit)
        }
    }
    
    /**
     * Checks conditions and fetches conversation summary if appropriate.
     *
     * Conversation summary is fetched if:
     * 1. Feature is enabled
     * 2. Unread count exceeds threshold (default 30)
     * 3. We're in main conversation (not thread)
     *
     * @param unreadCount The number of unread messages in the conversation.
     */
    private fun checkAndFetchConversationSummary(unreadCount: Int) {
        if (!enableConversationSummary) return
        if (unreadCount <= unreadThreshold) return
        if (parentMessageId != -1L) return
        
        fetchConversationSummary()
    }
    
    /**
     * Initializes the sound manager for playing message sounds.
     *
     * This should be called from the UI layer with an application context.
     *
     * @param context The Android context (application context recommended).
     */
    fun initSoundManager(context: Context) {
        if (soundManager == null) {
            soundManager = CometChatSoundManager(context.applicationContext)
        }
    }

    
    // ========================================
    // Message Fetching
    // ========================================
    
    /**
     * Fetches previous (older) messages from the repository.
     *
     * This method:
     * 1. Checks if more messages are available and no fetch is in progress
     * 2. Sets [isInProgress] to `true` during the operation
     * 3. Shows loading state only on initial fetch (when message list is empty)
     * 4. Updates [messages] and [uiState] based on the result
     * 5. Filters out duplicates when merging new messages with existing ones
     *
     * The method is safe to call multiple times - it will be ignored if a fetch
     * is already in progress or if all messages have been loaded.
     *
     * @see fetchNextMessages
     * @see fetchMessagesWithUnreadCount
     */
    fun fetchMessages() {
        if (!_hasMorePreviousMessages.value || _isInProgress.value) return
        
        viewModelScope.launch {
            _isInProgress.value = true
            
            if (_messages.value.isEmpty()) {
                _uiState.value = MessageListUIState.Loading
            }
            
            repository.fetchPreviousMessages()
                .onSuccess { newMessages ->
                    _hasMorePreviousMessages.value = newMessages.isNotEmpty()
                    // Filter out duplicates by message ID before merging
                    val existingIds = _messages.value.map { it.id }.toSet()
                    val uniqueNewMessages = newMessages.filter { it.id !in existingIds }
                    _messages.value = uniqueNewMessages + _messages.value
                    _uiState.value = if (_messages.value.isEmpty()) {
                        MessageListUIState.Empty
                    } else {
                        MessageListUIState.Loaded
                    }
                    
                    // Update latestMessageId to the last message in the list
                    // This is critical for the real-time message guard to work correctly
                    _messages.value.lastOrNull()?.let { lastMessage ->
                        latestMessageId = lastMessage.id
                    }
                    
                    // Mark conversation as read on first fetch
                    // This matches the Java implementation behavior in CometChatMessageList.setList():
                    // if (gotoMessageId == -1 || firstUnreadMessage != null && firstUnreadMessage.getId() > -1)
                    if (firstFetch) {
                        firstFetch = false
                        val firstUnreadMessage = getFirstUnreadMessage()
                        if (gotoMessageId <= 0 || (firstUnreadMessage != null && firstUnreadMessage.id > -1)) {
                            markConversationRead()
                        }
                        
                        // Fetch conversation starters if message list is empty
                        checkAndFetchConversationStarter()
                        
                        // Signal UI to scroll to bottom after initial load
                        // With reverseLayout = false, newest messages are at the bottom
                        // so we need to explicitly scroll there
                        viewModelScope.launch {
                            _scrollToBottomEvent.emit(Unit)
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.value = MessageListUIState.Error(error)
                }
            
            _isInProgress.value = false
        }
    }
    
    /**
     * Fetches next (newer) messages from the last message in the list.
     *
     * This method is used to load messages that arrived while the user was
     * scrolled up in the conversation. It fetches messages newer than the
     * most recent message in the current list.
     *
     * Note: Filters out duplicates by message ID before merging.
     *
     * @see fetchMessages
     */
    fun fetchNextMessages() {
        val lastMessage = _messages.value.lastOrNull() ?: return
        if (_isInProgress.value) return
        
        viewModelScope.launch {
            _isInProgress.value = true
            
            repository.fetchNextMessages(lastMessage.id)
                .onSuccess { newMessages ->
                    _hasMoreNewMessages.value = newMessages.isNotEmpty()
                    // Filter out duplicates by message ID before merging
                    val existingIds = _messages.value.map { it.id }.toSet()
                    val uniqueNewMessages = newMessages.filter { it.id !in existingIds }
                    _messages.value = _messages.value + uniqueNewMessages
                    
                    // Update latestMessageId to the last message in the list
                    // This is critical for the real-time message guard to work correctly
                    _messages.value.lastOrNull()?.let { newestMessage ->
                        latestMessageId = newestMessage.id
                    }
                    
                    // Mark conversation as read if there's a first unread message
                    // This matches the Java implementation behavior in CometChatMessageList.notifyRangeChangedAtEnd()
                    val firstUnreadMessage = getFirstUnreadMessage()
                    if (firstUnreadMessage != null && firstUnreadMessage.id > -1) {
                        markConversationRead()
                    }
                }
            
            _isInProgress.value = false
        }
    }
    
    /**
     * Fetches messages with consideration for unread message count.
     *
     * This method implements the following branching logic:
     * 1. Fetches the conversation to get `lastReadMessageId`, `unreadCount`, and `lastMessage`
     * 2. Stores `latestMessageId` from `conversation.lastMessage.id`
     * 3. Stores `lastReadMessageId` from `conversation.lastReadMessageId`
     * 4. IF `gotoMessageId > 0`, THEN calls `goToMessage(gotoMessageId)`
     * 5. ELSE IF `startFromUnreadMessages && lastReadMessageId > 0 && unreadCount > 0`,
     *    THEN calls `goToMessage(lastReadMessageId, highlight=false)`
     * 6. ELSE calls `fetchMessages()` with the unread count
     *
     * Use this method instead of [fetchMessages] when you want to support
     * the "start from unread" feature or navigate to a specific message.
     *
     * @see setStartFromUnreadMessages
     * @see setUnreadThreshold
     */
    fun fetchMessagesWithUnreadCount() {
        viewModelScope.launch {
            val id = user?.uid ?: group?.guid ?: return@launch
            val type = if (user != null) CometChatConstants.RECEIVER_TYPE_USER else CometChatConstants.RECEIVER_TYPE_GROUP
            
            repository.getConversation(id, type)
                .onSuccess { conversation ->
                    // Store unread count
                    _unreadCount.value = conversation.unreadMessageCount
                    
                    // Store latestMessageId from conversation.lastMessage.id
                    val lastMessage = conversation.lastMessage
                    latestMessageId = conversation.latestMessageId
                    
                    // Store lastReadMessageId from conversation.lastReadMessageId
                    lastReadMessageId = conversation.lastReadMessageId
                    
                    // Branching logic:
                    // 1. Check gotoMessageId > 0 first
                    if (gotoMessageId > 0) {
                        goToMessage(gotoMessageId)
                    }
                    // 2. Check startFromUnreadMessages && lastReadMessageId > 0 && unreadCount > 0
                    else if (startFromUnreadMessages && lastReadMessageId > 0 && conversation.unreadMessageCount > 0) {
                        goToMessage(lastReadMessageId, highlight = false)
                    }
                    // 3. Otherwise call fetchMessages()
                    else {
                        fetchMessages()
                    }
                    
                    // Check for conversation summary based on unread threshold
                    checkAndFetchConversationSummary(conversation.unreadMessageCount)
                }
                .onFailure {
                    // On error, still try to navigate to gotoMessageId if set, otherwise fetch messages
                    if (gotoMessageId > 0) {
                        goToMessage(gotoMessageId)
                    } else {
                        fetchMessages()
                    }
                }
        }
    }
    
    /**
     * Navigates to a specific message by ID, fetching surrounding messages for context.
     *
     * This method:
     * 1. Sets the UI state to [MessageListUIState.Loading]
     * 2. Fetches the target message from the server
     * 3. Fetches messages surrounding the target (older and newer)
     * 4. Replaces the current message list with the combined snapshot
     * 5. Updates pagination flags ([hasMorePreviousMessages], [hasMoreNewMessages])
     * 6. Emits the target message ID to [scrollToMessageId] for UI scrolling
     * 7. Rebuilds the messages request for continued pagination
     *
     * If the highlight parameter is `true`, the [highlightScroll] flow will emit `true`
     * to indicate the UI should visually highlight the target message.
     *
     * @param messageId The unique ID of the message to navigate to.
     * @param highlight Whether to highlight the message after scrolling (default `true`).
     *
     * @see clearScrollToMessage
     * @see clearHighlightScroll
     * @see highlightScroll
     */
    fun goToMessage(messageId: Long, highlight: Boolean = true) {
        viewModelScope.launch {
            // Step 1: Set uiState to Loading
            _uiState.value = MessageListUIState.Loading
            
            // Step 2: Fetch the target message
            repository.getMessage(messageId)
                .onSuccess { targetMessage ->
                    // Step 3: Fetch surrounding messages
                    repository.fetchSurroundingMessages(messageId)
                        .onSuccess { result ->
                            // Step 4: Combine messages in chronological order: older + target + newer
                            val combinedMessages = result.olderMessages + 
                                listOf(result.targetMessage) + 
                                result.newerMessages
                            
                            // Step 5: Clear and replace message list
                            _messages.value = combinedMessages
                            
                            // Step 5.1: Detect and emit unread message anchor
                            handleUnreadMessageState()
                            
                            // Step 6: Update pagination flags
                            _hasMorePreviousMessages.value = result.hasMorePrevious
                            _hasMoreNewMessages.value = result.hasMoreNext
                            
                            // Step 7: Emit scrollToMessageId
                            _scrollToMessageId.value = messageId
                            
                            // Step 8: Set highlight flag if requested
                            if (highlight) {
                                _highlightScroll.value = true
                            }
                            
                            // Step 9: Rebuild request from oldest message ID for continued pagination
                            if (combinedMessages.isNotEmpty()) {
                                val oldestMessageId = combinedMessages.first().id
                                repository.rebuildRequestFromMessageId(oldestMessageId)
                            }
                            
                            // Step 10: Update UI state to Loaded
                            _uiState.value = if (combinedMessages.isEmpty()) {
                                MessageListUIState.Empty
                            } else {
                                MessageListUIState.Loaded
                            }
                        }
                        .onFailure { error ->
                            // Handle error from fetchSurroundingMessages
                            _uiState.value = MessageListUIState.Error(error)
                        }
                }
                .onFailure { error ->
                    // Handle error from getMessage
                    _uiState.value = MessageListUIState.Error(error)
                }
        }
    }
    
    /**
     * Clears the scroll to message request.
     *
     * Call this from the UI after successfully scrolling to the requested message.
     *
     * @see goToMessage
     */
    fun clearScrollToMessage() {
        _scrollToMessageId.value = null
    }
    
    /**
     * Clears the highlight scroll flag.
     *
     * Call this from the UI after the highlight animation completes.
     *
     * @see goToMessage
     * @see highlightScroll
     */
    fun clearHighlightScroll() {
        _highlightScroll.value = false
    }

    
    // ========================================
    // ListOperations Interface Implementation
    // ========================================
    
    /**
     * Adds a single message to the end of the list.
     *
     * This is the [ListOperations] interface method. For message-specific
     * validation (checking if message belongs to current conversation),
     * use [addMessage] instead.
     *
     * @param item The [BaseMessage] to add.
     *
     * @see addMessage
     * @see ListOperations.addItem
     */
    override fun addItem(item: BaseMessage) {
        listDelegate.addItem(item)
        _uiState.value = MessageListUIState.Loaded
    }
    
    /**
     * Adds multiple messages to the end of the list.
     *
     * @param items The list of [BaseMessage] to add.
     *
     * @see ListOperations.addItems
     */
    override fun addItems(items: List<BaseMessage>) {
        listDelegate.addItems(items)
        if (items.isNotEmpty()) {
            _uiState.value = MessageListUIState.Loaded
        }
    }
    
    /**
     * Removes a message from the list by ID.
     *
     * @param item The [BaseMessage] to remove (matched by ID).
     * @return `true` if the message was found and removed, `false` otherwise.
     *
     * @see ListOperations.removeItem
     */
    override fun removeItem(item: BaseMessage): Boolean {
        val result = listDelegate.removeItem(item)
        if (_messages.value.isEmpty()) {
            _uiState.value = MessageListUIState.Empty
        }
        return result
    }
    
    /**
     * Removes a message at the specified index.
     *
     * @param index The index of the message to remove.
     * @return The removed [BaseMessage], or `null` if index is out of bounds.
     *
     * @see ListOperations.removeItemAt
     */
    override fun removeItemAt(index: Int): BaseMessage? {
        val result = listDelegate.removeItemAt(index)
        if (_messages.value.isEmpty()) {
            _uiState.value = MessageListUIState.Empty
        }
        return result
    }
    
    /**
     * Updates a message in the list that matches the predicate.
     *
     * @param item The new [BaseMessage] to replace with.
     * @param predicate Function to find the message to update.
     * @return `true` if a message was found and updated, `false` otherwise.
     *
     * @see ListOperations.updateItem
     */
    override fun updateItem(item: BaseMessage, predicate: (BaseMessage) -> Boolean): Boolean {
        return listDelegate.updateItem(item, predicate)
    }
    
    /**
     * Removes all messages from the list.
     *
     * Note: This only clears the list. For a full reset including pagination state,
     * use [clear] instead.
     *
     * @see clear
     * @see ListOperations.clearItems
     */
    override fun clearItems() {
        listDelegate.clearItems()
        _uiState.value = MessageListUIState.Empty
    }
    
    /**
     * Returns a copy of all messages in the list.
     *
     * @return Immutable list of all [BaseMessage] items.
     *
     * @see ListOperations.getItems
     */
    override fun getItems(): List<BaseMessage> = listDelegate.getItems()
    
    /**
     * Returns the message at the specified index.
     *
     * @param index The index of the message.
     * @return The [BaseMessage] at the index, or `null` if out of bounds.
     *
     * @see ListOperations.getItemAt
     */
    override fun getItemAt(index: Int): BaseMessage? = listDelegate.getItemAt(index)
    
    /**
     * Returns the number of messages in the list.
     *
     * @return The message count.
     *
     * @see ListOperations.getItemCount
     */
    override fun getItemCount(): Int = listDelegate.getItemCount()
    
    /**
     * Moves a message to the top (index 0) of the list.
     *
     * If the message doesn't exist, it is added at the top.
     * Note: For message lists, this is typically used for pinned messages
     * or priority messages.
     *
     * @param item The [BaseMessage] to move to top.
     *
     * @see ListOperations.moveItemToTop
     */
    override fun moveItemToTop(item: BaseMessage) {
        listDelegate.moveItemToTop(item)
        _uiState.value = MessageListUIState.Loaded
    }
    
    /**
     * Performs multiple operations in a single batch, emitting only once.
     *
     * Critical for performance when receiving many updates rapidly
     * (e.g., multiple messages from listener within a second).
     *
     * Example usage:
     * ```kotlin
     * viewModel.batch {
     *     add(message1)
     *     add(message2)
     *     remove(deletedMessage)
     *     update(editedMessage) { it.id == editedMessage.id }
     * }
     * ```
     *
     * @param operations Lambda that performs multiple list operations.
     *
     * @see ListOperations.batch
     */
    override fun batch(operations: ListOperationsBatchScope<BaseMessage>.() -> Unit) {
        listDelegate.batch(operations)
        _uiState.value = if (_messages.value.isEmpty()) {
            MessageListUIState.Empty
        } else {
            MessageListUIState.Loaded
        }
    }
    
    // ========================================
    // Legacy Message Operations (for backward compatibility)
    // ========================================
    
    /**
     * Adds a message to the list if it belongs to the current conversation.
     *
     * The message is validated against the current conversation context
     * (user/group and thread) before being added. If the message doesn't
     * belong to the current conversation, it is ignored.
     *
     * This method is called automatically by real-time listeners when new
     * messages are received, but can also be called manually.
     *
     * Note: This method checks for duplicates by message ID before adding.
     * If a message with the same ID already exists, it will be updated instead.
     *
     * @param message The [BaseMessage] to add to the list.
     *
     * @see addItem
     * @see updateMessage
     * @see removeMessage
     */
    open fun addMessage(message: BaseMessage) {
        if (isMessageForCurrentChat(message) && isThreadedMessageForCurrentChat(message)) {
            // Check if message already exists to prevent duplicates
            // First check by muid (for optimistic messages that haven't received server ID yet)
            val muid = message.muid
            if (!muid.isNullOrEmpty()) {
                val existingByMuid = _messages.value.indexOfFirst { it.muid == muid }
                if (existingByMuid >= 0) {
                    // Message with same muid exists, update it instead
                    updateMessage(message)
                    return
                }
            }
            
            // Then check by ID (for messages that have server ID)
            if (message.id > 0) {
                val existingById = _messages.value.indexOfFirst { it.id == message.id }
                if (existingById >= 0) {
                    // Message exists, update it instead
                    updateMessage(message)
                    return
                }
            }
            
            // New message, add to end
            addItem(message)
            
            // Clear conversation starters when first message is added
            if (_conversationStarterReplies.value.isNotEmpty()) {
                clearConversationStarter()
            }
        }
    }
    
    /**
     * Updates an existing message in the list.
     *
     * Finds the message by ID and replaces it with the updated version.
     * If the message is not found, the list remains unchanged.
     *
     * This method is called automatically when:
     * - A message is edited
     * - Reactions are added or removed
     * - Read/delivery receipts are received
     *
     * @param message The updated [BaseMessage] with the same ID as the original.
     *
     * @see updateItem
     * @see addMessage
     * @see removeMessage
     */
    open fun updateMessage(message: BaseMessage) {
        updateItem(message) { it.id == message.id }
    }
    
    /**
     * Removes a message from the list.
     *
     * Finds and removes the message by ID. If the list becomes empty after
     * removal, the UI state is updated to [MessageListUIState.Empty].
     *
     * @param message The [BaseMessage] to remove (matched by ID).
     *
     * @see removeItem
     * @see addMessage
     * @see updateMessage
     * @see clear
     */
    open fun removeMessage(message: BaseMessage) {
        removeItem(message)
    }
    
    /**
     * Updates the reply count of a parent message.
     *
     * This method increments the `replyCount` property of the message with the
     * given ID. It is called when a new thread reply is sent to update the
     * parent message's reply count in the main conversation view.
     *
     * The method:
     * 1. Finds the message with the given ID in the current list
     * 2. Increments its `replyCount` by 1
     * 3. Updates the message in the list
     *
     * If the message is not found in the list, the method does nothing.
     * This is expected behavior when the parent message is not visible
     * (e.g., user has scrolled away from it).
     *
     * Note: This method only updates the local state. The server-side reply
     * count is updated automatically when the reply is sent.
     *
     * @param parentMessageId The ID of the parent message to update.
     *
     * @see handleMessageSentEvent
     */
    open fun updateReplyCount(parentMessageId: Long) {
        val parentMessage = _messages.value.find { it.id == parentMessageId }
        if (parentMessage == null) {
            android.util.Log.d("ThreadReplyDebug", "updateReplyCount: parent message $parentMessageId NOT found in list (${_messages.value.size} messages)")
            return
        }
        
        val oldCount = parentMessage.replyCount
        parentMessage.replyCount = oldCount + 1
        android.util.Log.d("ThreadReplyDebug", "updateReplyCount: parent=$parentMessageId, oldCount=$oldCount, newCount=${parentMessage.replyCount}")
        
        // Emit via SharedFlow so the UI layer is guaranteed to receive the update,
        // even though StateFlow conflation would suppress it (same object reference).
        viewModelScope.launch {
            _messageUpdated.emit(parentMessage)
        }
    }
    
    /**
     * Clears all messages and resets the state.
     *
     * This method:
     * 1. Clears the message list
     * 2. Sets UI state to [MessageListUIState.Empty]
     * 3. Resets the repository's pagination state
     * 4. Resets pagination flags
     *
     * Use this when switching conversations or when a full refresh is needed.
     *
     * @see clearItems
     */
    open fun clear() {
        clearItems()
        repository.resetRequest()
        _hasMorePreviousMessages.value = true
        _hasMoreNewMessages.value = false
    }
    
    // ========================================
    // Message Actions
    // ========================================
    
    /**
     * Deletes a message from the server.
     *
     * This method:
     * 1. Sets [deleteState] to [MessageDeleteState.InProgress]
     * 2. Calls the repository to delete the message
     * 3. On success: removes or updates the message based on [hideDeleteMessage] setting
     * 4. Sets [deleteState] to [MessageDeleteState.Success] or [MessageDeleteState.Error]
     *
     * After handling the result, call [resetDeleteState] to return to idle.
     *
     * @param message The [BaseMessage] to delete.
     *
     * @see resetDeleteState
     * @see setHideDeleteMessage
     */
    fun deleteMessage(message: BaseMessage) {
        viewModelScope.launch {
            _deleteState.value = MessageDeleteState.InProgress
            
            try {
                repository.deleteMessage(message)
                    .onSuccess { deletedMessage ->
                        if (hideDeleteMessage) {
                            removeMessage(deletedMessage)
                        } else {
                            updateMessage(deletedMessage)
                        }
                        _deleteState.value = MessageDeleteState.Success(deletedMessage)
                    }
                    .onFailure { error ->
                        _deleteState.value = MessageDeleteState.Error(error)
                        _onError.emit(
                            error as? CometChatException ?: CometChatException(
                                "DELETE_ERROR",
                                error.message ?: "Delete failed"
                            )
                        )
                    }
            } catch (e: Exception) {
                _deleteState.value = MessageDeleteState.Error(e)
                _onError.emit(
                    CometChatException("DELETE_ERROR", e.message ?: "Delete failed")
                )
            }
        }
    }
    
    /**
     * Resets the delete state to [MessageDeleteState.Idle].
     *
     * Call this after handling a delete success or error to prepare for
     * the next delete operation.
     *
     * @see deleteMessage
     */
    fun resetDeleteState() {
        _deleteState.value = MessageDeleteState.Idle
    }
    
    /**
     * Flags/reports a message for moderation.
     *
     * This method:
     * 1. Sets [flagState] to [MessageFlagState.InProgress]
     * 2. Calls the repository to flag the message
     * 3. Sets [flagState] to [MessageFlagState.Success] or [MessageFlagState.Error]
     *
     * After handling the result, call [resetFlagState] to return to idle.
     *
     * @param message The [BaseMessage] to flag.
     * @param reason The reason for flagging (e.g., "spam", "harassment").
     * @param remark Additional remarks or context (default empty string).
     *
     * @see resetFlagState
     */
    fun flagMessage(message: BaseMessage, reason: String, remark: String = "") {
        viewModelScope.launch {
            _flagState.value = MessageFlagState.InProgress
            
            repository.flagMessage(message.id, reason, remark)
                .onSuccess {
                    _flagState.value = MessageFlagState.Success
                }
                .onFailure { error ->
                    _flagState.value = MessageFlagState.Error(error)
                }
        }
    }
    
    /**
     * Resets the flag state to [MessageFlagState.Idle].
     *
     * Call this after handling a flag success or error to prepare for
     * the next flag operation.
     *
     * @see flagMessage
     */
    fun resetFlagState() {
        _flagState.value = MessageFlagState.Idle
    }
    
    /**
     * Adds a reaction to a message.
     *
     * On success, the message in the list is automatically updated with
     * the new reaction.
     *
     * @param message The [BaseMessage] to react to.
     * @param emoji The reaction emoji string (e.g., "👍", "❤️").
     *
     * @see removeReaction
     */
    fun addReaction(message: BaseMessage, emoji: String) {
        viewModelScope.launch {
            repository.addReaction(message.id, emoji)
                .onSuccess { updatedMessage ->
                    updateMessage(updatedMessage)
                }
        }
    }
    
    /**
     * Removes a reaction from a message.
     *
     * On success, the message in the list is automatically updated with
     * the reaction removed.
     *
     * @param message The [BaseMessage] to remove the reaction from.
     * @param emoji The reaction emoji string to remove.
     *
     * @see addReaction
     */
    fun removeReaction(message: BaseMessage, emoji: String) {
        viewModelScope.launch {
            repository.removeReaction(message.id, emoji)
                .onSuccess { updatedMessage ->
                    updateMessage(updatedMessage)
                }
        }
    }
    
    /**
     * Marks a message as delivered.
     *
     * This sends a delivery receipt to the message sender, indicating that
     * the message has been received on the current user's device. The receipt
     * is only sent if:
     * - [disableReceipt] is `false`
     * - The message has a sender (not null)
     * - The message sender is not the current user
     *
     * This method is typically called automatically when a new message is
     * received via real-time listeners in [handleIncomingMessage].
     *
     * @param message The [BaseMessage] to mark as delivered.
     *
     * @see markMessageAsRead
     * @see setDisableReceipt
     */
    fun markAsDelivered(message: BaseMessage) {
        if (disableReceipt) return
        val senderUid = message.sender?.uid ?: return // Don't mark if sender is null
        if (senderUid == getLoggedInUserUid()) return
        
        viewModelScope.launch {
            repository.markAsDelivered(message)
        }
    }
    
    /**
     * Marks a message as read.
     *
     * This sends a read receipt to the message sender. The receipt is only
     * sent if:
     * - [disableReceipt] is `false`
     * - The message sender is not the current user
     *
     * @param message The [BaseMessage] to mark as read.
     *
     * @see markMessageAsUnread
     * @see setDisableReceipt
     */
    fun markMessageAsRead(message: BaseMessage) {
        if (!disableReceipt && message.sender?.uid != CometChat.getLoggedInUser()?.uid) {
            viewModelScope.launch {
                repository.markAsRead(message)
            }
        }
    }
    
    /**
     * Marks the last visible message as read with full validation.
     *
     * This method is called by the UI when the user scrolls to the bottom of the
     * message list or when a new message becomes visible. It performs comprehensive
     * validation before sending a read receipt to ensure:
     *
     * 1. Read receipts are enabled ([disableReceipt] is `false`)
     * 2. The message is from another user (not the current user's own message)
     * 3. The message hasn't already been read ([readAt] == 0)
     * 4. The message belongs to the correct thread context
     *
     * ## Thread Context Handling
     *
     * The method handles both main conversation and threaded conversation contexts:
     *
     * - **Main conversation** ([parentMessageId] == -1): Only marks messages that are
     *   NOT thread replies (message.parentMessageId == 0)
     * - **Thread view** ([parentMessageId] > -1): Only marks messages that belong to
     *   the current thread (message.parentMessageId == parentMessageId)
     *
     * ## Success Behavior
     *
     * On successful read receipt:
     * 1. Updates the message's [readAt] timestamp locally via [updateMessageReadAt]
     * 2. Emits the message to [messageReadEvent] for UI layer notifications
     *
     * ## Usage
     *
     * ```kotlin
     * // In UI layer when user scrolls to bottom
     * val lastVisibleMessage = messages.lastOrNull()
     * lastVisibleMessage?.let { viewModel.markLastMessageAsRead(it) }
     * ```
     *
     * @param message The [BaseMessage] to mark as read.
     *
     * @see markMessageAsRead
     * @see markConversationRead
     * @see messageReadEvent
     * @see updateMessageReadAt
     */
    fun markLastMessageAsRead(message: BaseMessage) {
        // Condition 1: Check if receipts are disabled
        if (disableReceipt) return
        
        // Condition 2: Check if message sender is the current user
        val senderUid = message.sender?.uid ?: return
        if (senderUid == getLoggedInUserUid()) return
        
        // Condition 3: Check if message is already read
        if (message.readAt != 0L) return
        
        // Condition 4: Thread context check
        val shouldMark = when {
            // Main conversation: only mark messages with parentMessageId == 0
            parentMessageId == -1L -> message.parentMessageId == 0L
            // Thread view: only mark messages matching the thread
            else -> message.parentMessageId == parentMessageId
        }
        
        if (!shouldMark) return
        
        viewModelScope.launch {
            repository.markAsRead(message)
                .onSuccess {
                    // Update local message state with current timestamp
                    updateMessageReadAt(message.id, System.currentTimeMillis() / 1000)
                    // Emit event for UI/helper notifications
                    _messageReadEvent.emit(message)
                    // Emit UIKit event so ConversationListViewModel can clear unread count
                    CometChatEvents.emitMessageEvent(CometChatMessageEvent.MessageRead(message))
                }
        }
    }
    
    /**
     * Updates a message's readAt timestamp in the local message list.
     *
     * This method is called after a successful [markAsRead] operation to update
     * the local state without requiring a server round-trip. It finds the message
     * by ID and updates its [readAt] property to the specified timestamp.
     *
     * The update is performed by mapping over the message list and creating a
     * new list with the updated message. Messages that don't match the ID are
     * left unchanged.
     *
     * @param messageId The unique ID of the message to update.
     * @param timestamp The Unix timestamp (in seconds) to set as the read time.
     *
     * @see markLastMessageAsRead
     */
    private fun updateMessageReadAt(messageId: Long, timestamp: Long) {
        _messages.value = _messages.value.map { message ->
            if (message.id == messageId) {
                message.apply { readAt = timestamp }
            } else {
                message
            }
        }
    }
    
    /**
     * Marks a message as unread.
     *
     * This allows users to mark a message for later attention.
     * The conversation will show as having unread messages.
     *
     * @param message The [BaseMessage] to mark as unread.
     *
     * @see markMessageAsRead
     */
    fun markMessageAsUnread(message: BaseMessage) {
        viewModelScope.launch {
            repository.markAsUnread(message)
                .onSuccess { conversation ->
                    Log.d("CometChatMessageListVM", "markMessageAsUnread() - SUCCESS for conversationId=${message.conversationId}, unreadCount=${conversation.unreadMessageCount}")
                    // Set the unread message anchor for UI to display the "New" separator
                    _unreadMessageAnchor.value = message
                    
                    // Emit ConversationUpdated event with the conversation returned from SDK
                    // This has the correct unread count
                    CometChatEvents.emitConversationEvent(CometChatConversationEvent.ConversationUpdated(conversation))
                }
                .onFailure { error ->
                    Log.e("CometChatMessageListVM", "markMessageAsUnread() - FAILED: ${error.message}")
                }
        }
    }
    
    /**
     * Marks the entire conversation as read by marking the last message as read.
     *
     * This method:
     * 1. Gets the last message in the current message list
     * 2. Calls [CometChat.markAsRead] on that message
     * 3. On success, resets [unreadCount] to 0
     *
     * The UI should observe [unreadCount] to update the scroll-to-bottom button's
     * badge count when this method succeeds.
     *
     * This is typically called when:
     * - The user scrolls to the bottom of the message list
     * - The user clicks the scroll-to-bottom button
     * - The message list is first loaded and the user is at the bottom
     *
     * @see markMessageAsRead
     * @see unreadCount
     */
    fun markConversationRead() {
        Log.d("CometChatMessageListVM", "markConversationRead() called")
        val lastMessage = _messages.value.lastOrNull() ?: run {
            Log.d("CometChatMessageListVM", "markConversationRead() - No messages in list, returning")
            return
        }
        
        Log.d("CometChatMessageListVM", "markConversationRead() - lastMessage.id=${lastMessage.id}, conversationId=${lastMessage.conversationId}")
        
        // Reset local unread count
        _unreadCount.value = 0
        
        // If last message is from current user, we don't need to send a read receipt to the server
        // but we still need to notify ConversationListViewModel to clear the unread badge
        if (lastMessage.sender?.uid == getLoggedInUserUid()) {
            Log.d("CometChatMessageListVM", "markConversationRead() - Last message is from current user, emitting MessageRead event without server call")
            viewModelScope.launch {
                // Emit UIKit event so ConversationListViewModel can clear unread count
                CometChatEvents.emitMessageEvent(CometChatMessageEvent.MessageRead(lastMessage))
                Log.d("CometChatMessageListVM", "markConversationRead() - MessageRead event emitted for conversationId=${lastMessage.conversationId}")
            }
            return
        }
        
        // Only send read receipt to server if receipts are enabled
        if (disableReceipt) {
            Log.d("CometChatMessageListVM", "markConversationRead() - Receipts disabled, emitting local event only")
            viewModelScope.launch {
                CometChatEvents.emitMessageEvent(CometChatMessageEvent.MessageRead(lastMessage))
            }
            return
        }
        
        Log.d("CometChatMessageListVM", "markConversationRead() - Calling repository.markAsRead()")
        viewModelScope.launch {
            repository.markAsRead(lastMessage)
                .onSuccess {
                    Log.d("CometChatMessageListVM", "markConversationRead() - markAsRead SUCCESS, emitting MessageRead event")
                    _messageReadEvent.emit(lastMessage)
                    // Emit UIKit event so ConversationListViewModel can clear unread count
                    CometChatEvents.emitMessageEvent(CometChatMessageEvent.MessageRead(lastMessage))
                    Log.d("CometChatMessageListVM", "markConversationRead() - MessageRead event emitted for conversationId=${lastMessage.conversationId}")
                }
                .onFailure { error ->
                    Log.e("CometChatMessageListVM", "markConversationRead() - markAsRead FAILED: ${error.message}")
                }
        }
    }
    
    /**
     * Resets the unread count to zero without sending a read receipt.
     *
     * This is useful when the UI needs to clear the unread count locally
     * (e.g., when the user clicks the scroll-to-bottom button) without
     * necessarily sending a read receipt to the server.
     *
     * @see markConversationRead
     * @see unreadCount
     */
    fun resetUnreadCount() {
        _unreadCount.value = 0
    }
    
    /**
     * Fetches the full user details of a message sender.
     *
     * This method is useful when you need complete user information
     * (e.g., for navigating to their profile or starting a new conversation)
     * that may not be available in the message's sender object.
     *
     * The method:
     * 1. Extracts the sender UID from the message
     * 2. Calls [CometChat.getUser] to fetch full user details
     * 3. On success: Emits the user to [messageSenderFetched]
     * 4. On failure: Handles error gracefully (logs or ignores)
     *
     * Does nothing if:
     * - Message is null
     * - Message sender is null
     * - Sender UID is empty
     *
     * @param message The [BaseMessage] whose sender to fetch.
     *
     * @see messageSenderFetched
     */
    fun fetchMessageSender(message: BaseMessage?) {
        val senderUid = message?.sender?.uid
        if (senderUid.isNullOrEmpty()) return
        
        viewModelScope.launch {
            try {
                val user = suspendCancellableCoroutine<User> { continuation ->
                    CometChat.getUser(
                        senderUid,
                        object : CometChat.CallbackListener<User>() {
                            override fun onSuccess(user: User) {
                                continuation.resume(user)
                            }
                            
                            override fun onError(exception: CometChatException) {
                                continuation.resumeWithException(exception)
                            }
                        }
                    )
                }
                
                _messageSenderFetched.emit(user)
                
            } catch (e: CometChatException) {
                // Handle error gracefully - log or ignore
                // The UI can observe messageSenderFetched for successful fetches
            }
        }
    }
    
    /**
     * Triggers the message edit flow.
     *
     * This method emits a [CometChatMessageEvent.MessageEdited] event with
     * status [MessageStatus.IN_PROGRESS] to signal to the MessageComposer
     * that edit mode should be activated for the given message.
     *
     * The MessageComposer should observe [CometChatEvents.messageEvents]
     * and handle this event by:
     * 1. Populating the input field with the message text
     * 2. Showing edit mode UI indicators
     * 3. Storing the message reference for the edit operation
     *
     * @param message The [BaseMessage] to edit.
     *
     * @see CometChatMessageEvent.MessageEdited
     */
    fun onMessageEdit(message: BaseMessage) {
        CometChatEvents.emitMessageEvent(
            CometChatMessageEvent.MessageEdited(message, MessageStatus.IN_PROGRESS)
        )
    }
    
    /**
     * Triggers the message reply flow.
     *
     * This method emits a [CometChatMessageEvent.ReplyToMessage] event with
     * status [MessageStatus.IN_PROGRESS] to signal to the MessageComposer
     * that reply mode should be activated for the given message.
     *
     * The MessageComposer should observe [CometChatEvents.messageEvents]
     * and handle this event by:
     * 1. Showing the reply preview UI
     * 2. Storing the message reference for the reply operation
     * 3. Setting up the reply metadata for the outgoing message
     *
     * @param message The [BaseMessage] to reply to.
     *
     * @see CometChatMessageEvent.ReplyToMessage
     */
    fun onMessageReply(message: BaseMessage) {
        CometChatEvents.emitMessageEvent(
            CometChatMessageEvent.ReplyToMessage(message, MessageStatus.IN_PROGRESS)
        )
    }
    
    /**
     * Clears the configured go-to message ID.
     *
     * This method resets [gotoMessageId] to 0, preventing subsequent
     * [fetchMessagesWithUnreadCount] calls from navigating to a specific message.
     *
     * Call this after the initial navigation is complete to ensure
     * future fetches start from the normal position.
     *
     * @see gotoMessageId
     * @see fetchMessagesWithUnreadCount
     */
    fun clearGoToMessageId() {
        gotoMessageId = 0
    }
    
    /**
     * Emits a message for custom processing.
     *
     * UI can observe [processMessageData] to handle messages that
     * require special treatment (e.g., custom interactive messages,
     * form messages, etc.).
     *
     * @param message The [BaseMessage] to process.
     *
     * @see processMessageData
     */
    fun emitProcessMessageData(message: BaseMessage) {
        viewModelScope.launch {
            _processMessageData.emit(message)
        }
    }

    
    // ========================================
    // Helper Methods
    // ========================================
    
    /**
     * Checks if a message belongs to the current conversation.
     *
     * For user conversations, checks if the message is:
     * - From the configured user to the current user, OR
     * - From the current user to the configured user
     *
     * For group conversations, checks if the message's receiver is the configured group.
     *
     * @param message The [BaseMessage] to check.
     * @return `true` if the message belongs to the current conversation, `false` otherwise.
     */
    private fun isMessageForCurrentChat(message: BaseMessage): Boolean {
        val id = user?.uid ?: group?.guid ?: return false
        return when (message.receiverType) {
            CometChatConstants.RECEIVER_TYPE_USER -> {
                id == message.sender?.uid || 
                (id == message.receiverUid && message.sender?.uid == getLoggedInUserUid())
            }
            CometChatConstants.RECEIVER_TYPE_GROUP -> {
                id == message.receiverUid
            }
            else -> false
        }
    }
    
    /**
     * Checks if a message belongs to the current thread context.
     *
     * For main conversations (parentMessageId == -1):
     * - Returns `true` if the message has no parent (parentMessageId == 0 or -1)
     *
     * For threaded conversations:
     * - Returns `true` if the message's parentMessageId matches the configured parentMessageId
     *
     * @param message The [BaseMessage] to check.
     * @return `true` if the message belongs to the current thread context, `false` otherwise.
     */
    private fun isThreadedMessageForCurrentChat(message: BaseMessage): Boolean {
        return if (parentMessageId == -1L) {
            message.parentMessageId == 0L || message.parentMessageId == -1L
        } else {
            message.parentMessageId == parentMessageId
        }
    }
    
    /**
     * Checks if a call event is for the current conversation.
     *
     * For user conversations: checks if the call is with the configured user
     * For group conversations: checks if the call is for the configured group
     *
     * Call events should only be processed in the main conversation (not thread view).
     *
     * @param call The [Call] from the event.
     * @return `true` if the call is for the current conversation, `false` otherwise.
     */
    private fun isCallForCurrentChat(call: Call): Boolean {
        // Only process in main conversation (not thread view)
        if (parentMessageId != -1L) return false
        
        return when {
            user != null -> {
                // For user conversations, check if call involves the configured user
                val callReceiverId = call.receiverUid
                val callInitiator = call.callInitiator as? User
                val callSenderId = callInitiator?.uid
                val currentUserId = user?.uid
                val loggedInUserId = getLoggedInUserUid()
                
                // Call is for current chat if:
                // - Receiver is the configured user, OR
                // - Sender is the configured user and receiver is logged-in user
                callReceiverId == currentUserId || 
                (callSenderId == currentUserId && callReceiverId == loggedInUserId)
            }
            group != null -> {
                // For group conversations, check if call is for the configured group
                call.receiverUid == group?.guid
            }
            else -> false
        }
    }
    
    /**
     * Determines the alignment for a message in the UI.
     *
     * The alignment determines how the message bubble is positioned:
     * - [MessageAlignment.LEFT]: Incoming messages from other users (aligned to the left)
     * - [MessageAlignment.RIGHT]: Outgoing messages from the current user (aligned to the right)
     * - [MessageAlignment.CENTER]: Action/system messages (centered, no bubble)
     *
     * ## Alignment Rules
     *
     * 1. Messages with category `ACTION` → [MessageAlignment.CENTER]
     * 2. Messages with category `CALL` → [MessageAlignment.CENTER]
     * 3. Messages where sender is current user → [MessageAlignment.RIGHT]
     * 4. All other messages → [MessageAlignment.LEFT]
     *
     * @param message The [BaseMessage] to get alignment for.
     * @return The [MessageAlignment] for the message.
     *
     * @see MessageAlignment
     */
    fun getMessageAlignment(message: BaseMessage): MessageAlignment {
        return when {
            message.category == CometChatConstants.CATEGORY_ACTION -> MessageAlignment.CENTER
            message.category == CometChatConstants.CATEGORY_CALL -> MessageAlignment.CENTER
            message.sender?.uid == CometChat.getLoggedInUser()?.uid -> MessageAlignment.RIGHT
            else -> MessageAlignment.LEFT
        }
    }
    
    /**
     * Plays the incoming message sound if sound is not disabled.
     *
     * Uses the custom sound if set, otherwise uses the default incoming message sound.
     */
    private fun playIncomingMessageSound() {
        if (!disableSoundForMessages) {
            viewModelScope.launch {
                _playSoundEvent.emit(true)
            }
            soundManager?.play(Sound.INCOMING_MESSAGE_FROM_OTHER, customSoundForMessages)
        }
    }
    
    /**
     * Finds the first unread message in the current message list.
     *
     * This method iterates through the messages to find the first message that:
     * 1. Has an ID greater than [lastReadMessageId]
     * 2. Was NOT sent by the logged-in user (incoming message)
     * 3. Is NOT deleted (deletedAt == 0)
     *
     * The first unread message is used to display an "unread messages" anchor/separator
     * in the UI, helping users identify where new messages begin.
     *
     * @return The first unread [BaseMessage], or `null` if:
     *         - [lastReadMessageId] is not set (< 0)
     *         - No messages meet the unread criteria
     *         - All messages after lastReadMessageId are from the current user or deleted
     *
     * @see lastReadMessageId
     * @see unreadMessageAnchor
     */
    private fun getFirstUnreadMessage(): BaseMessage? {
        if (lastReadMessageId < 0) return null
        
        val loggedInUserUid = getLoggedInUserUid()
        
        return _messages.value.firstOrNull { message ->
            // Message ID must be greater than lastReadMessageId
            message.id > lastReadMessageId &&
            // Skip messages from the logged-in user
            message.sender?.uid != loggedInUserUid &&
            // Skip deleted messages (deletedAt > 0 means deleted)
            message.deletedAt == 0L
        }
    }
    
    /**
     * Gets the UID of the currently logged-in user.
     * 
     * This method is protected to allow overriding in tests where the CometChat SDK
     * may not be initialized.
     * 
     * @return The UID of the logged-in user, or null if no user is logged in or SDK is not initialized.
     */
    protected open fun getLoggedInUserUid(): String? {
        return try {
            CometChat.getLoggedInUser()?.uid
        } catch (e: Exception) {
            // SDK not initialized, treat as no logged-in user
            null
        }
    }
    
    /**
     * Generates the ID map based on current conversation configuration.
     *
     * The map contains:
     * - RECEIVER_ID: user.uid for user conversations, group.guid for group conversations
     * - RECEIVER_TYPE: "user" or "group"
     * - PARENT_MESSAGE_ID: Only included if parentMessageId > 0 (threaded conversation)
     *
     * @return An immutable map identifying the current conversation context.
     */
    private fun generateIdMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        
        when {
            user != null -> {
                map[UIKitConstants.MapId.RECEIVER_ID] = user!!.uid
                map[UIKitConstants.MapId.RECEIVER_TYPE] = CometChatConstants.RECEIVER_TYPE_USER
            }
            group != null -> {
                map[UIKitConstants.MapId.RECEIVER_ID] = group!!.guid
                map[UIKitConstants.MapId.RECEIVER_TYPE] = CometChatConstants.RECEIVER_TYPE_GROUP
            }
        }
        
        // Only include parent message ID if in a threaded conversation
        if (parentMessageId > 0) {
            map[UIKitConstants.MapId.PARENT_MESSAGE_ID] = parentMessageId.toString()
        }
        
        return map.toMap() // Return immutable copy
    }
    
    /**
     * Returns the current conversation ID map.
     *
     * This is a convenience method that returns the current value of [idMap].
     * For reactive updates, observe [idMap] StateFlow instead.
     *
     * @return The current ID map identifying the conversation context.
     *
     * @see idMap
     */
    fun getIdMap(): Map<String, String> = _idMap.value

    /**
     * Returns the currently configured message types.
     *
     * These types are used when building the MessagesRequest to filter
     * which message types are fetched from the server.
     *
     * @return List of message type strings (e.g., "text", "image", "video")
     */
    fun getTypes(): List<String> = messagesTypes

    /**
     * Returns the currently configured message categories.
     *
     * These categories are used when building the MessagesRequest to filter
     * which message categories are fetched from the server.
     *
     * @return List of message category strings (e.g., "message", "action", "call")
     */
    fun getCategories(): List<String> = messagesCategories
    
    /**
     * Checks if an event's ID map matches the current conversation context.
     *
     * This method compares the provided event ID map with the current conversation's
     * ID map to determine if the event should be processed by this ViewModel.
     *
     * Matching rules:
     * 1. RECEIVER_ID must match exactly
     * 2. RECEIVER_TYPE must match exactly
     * 3. If current conversation is a thread (parentMessageId > 0):
     *    - Event must also have matching PARENT_MESSAGE_ID
     * 4. If current conversation is main (parentMessageId <= 0):
     *    - Event should NOT have PARENT_MESSAGE_ID, OR it should be "0" or "-1"
     *
     * @param eventIdMap The ID map from an incoming event.
     * @return `true` if the event is for the current conversation, `false` otherwise.
     */
    fun matchesIdMap(eventIdMap: Map<String, String>): Boolean {
        val currentMap = _idMap.value
        
        // Check receiver ID and type
        if (currentMap[UIKitConstants.MapId.RECEIVER_ID] != eventIdMap[UIKitConstants.MapId.RECEIVER_ID]) {
            return false
        }
        if (currentMap[UIKitConstants.MapId.RECEIVER_TYPE] != eventIdMap[UIKitConstants.MapId.RECEIVER_TYPE]) {
            return false
        }
        
        // Check thread context
        val currentParentId = currentMap[UIKitConstants.MapId.PARENT_MESSAGE_ID]
        val eventParentId = eventIdMap[UIKitConstants.MapId.PARENT_MESSAGE_ID]
        
        return when {
            // Current is a thread - event must match the same thread
            currentParentId != null && currentParentId.toLongOrNull() ?: 0 > 0 -> {
                currentParentId == eventParentId
            }
            // Current is main conversation - event should not be for a thread
            else -> {
                eventParentId == null || 
                eventParentId.toLongOrNull()?.let { it <= 0 } ?: true
            }
        }
    }
    
    /**
     * Handles the unread message state by finding and emitting the first unread message.
     *
     * This method checks the conditions for displaying an unread message anchor:
     * 1. [lastReadMessageId] must be greater than 0 (unread tracking is enabled)
     * 2. [parentMessageId] must be -1 (not in a threaded conversation)
     *
     * If both conditions are met, it calls [getFirstUnreadMessage] to find the first
     * unread message and emits it to [_unreadMessageAnchor]. If conditions are not met
     * or no unread message is found, it emits `null`.
     *
     * The unread anchor is disabled for threaded conversations because threads have
     * their own read state management and showing an unread anchor would be confusing.
     *
     * This method should be called after the message list is populated or updated,
     * such as after [goToMessage] or [fetchMessages] completes.
     *
     * @see getFirstUnreadMessage
     * @see unreadMessageAnchor
     * @see lastReadMessageId
     * @see parentMessageId
     */
    private fun handleUnreadMessageState() {
        // Check if unread tracking is enabled (lastReadMessageId > 0)
        // and we're not in a threaded conversation (parentMessageId == -1)
        if (lastReadMessageId > 0 && parentMessageId == -1L) {
            // Find and emit the first unread message
            _unreadMessageAnchor.value = getFirstUnreadMessage()
        } else {
            // Conditions not met - emit null to clear any existing anchor
            _unreadMessageAnchor.value = null
        }
    }
    
    // ========================================
    // Missed Messages Handling (Reconnection)
    // ========================================
    
    /**
     * Fetches messages that were missed while the user was disconnected.
     *
     * This method is called when the connection is re-established after being offline.
     * It performs two operations in sequence:
     * 1. Updates the existing message list by fetching action messages (edits, deletes)
     *    that occurred while offline via [updateListByActionMessages]
     * 2. Fetches new messages that arrived while offline via [fetchNextMessagesOnReconnect]
     *
     * This ensures the message list is fully synchronized with the server state after
     * reconnection, including both modifications to existing messages and new messages.
     *
     * The method is a no-op if the message list is empty, as there's nothing to update
     * and [fetchMessages] should be called instead for initial loading.
     *
     * @see updateListByActionMessages
     * @see fetchNextMessagesOnReconnect
     */
    private fun fetchMissedMessages() {
        // Skip if message list is empty - use fetchMessages() for initial load instead
        if (_messages.value.isEmpty()) return
        
        viewModelScope.launch {
            // Step 1: Update existing messages with any edits/deletes that occurred while offline
            updateListByActionMessages()
            
            // Step 2: Fetch new messages that arrived while offline
            fetchNextMessagesOnReconnect()
        }
    }
    
    /**
     * Updates the message list by fetching action messages (edits, deletes) since the last message.
     *
     * This method fetches ACTION category messages from the server starting from the
     * last message ID in the current list. For each action message, it extracts the
     * `actionOn` property which contains the updated message data, and updates the
     * corresponding message in the list.
     *
     * This is used during reconnection to sync any message modifications that occurred
     * while the user was offline.
     *
     * @see fetchMissedMessages
     * @see fetchNextMessagesOnReconnect
     */
    private suspend fun updateListByActionMessages() {
        // Get the last message ID from the message list
        val lastMessageId = _messages.value.lastOrNull()?.id ?: return
        
        // Fetch action messages (edits, deletes) since the last message
        repository.fetchActionMessages(lastMessageId)
            .onSuccess { actionMessages ->
                // For each action message, extract actionOn and update the corresponding message
                for (actionMessage in actionMessages) {
                    // Check if it's an Action type with category ACTION
                    if (actionMessage.category == CometChatConstants.CATEGORY_ACTION && 
                        actionMessage is Action) {
                        // Extract the actionOn property which contains the updated message data
                        val actionOn = actionMessage.actionOn
                        if (actionOn is BaseMessage) {
                            // Update the corresponding message in the list
                            updateMessage(actionOn)
                        }
                    }
                }
            }
            .onFailure { error ->
                // Log error but continue - don't block the reconnection flow
                // The fetchNextMessagesOnReconnect will still run
            }
    }
    
    /**
     * Fetches new messages that arrived while the user was disconnected.
     *
     * This method fetches messages newer than the last message in the current list
     * and appends them to the end of the list. It also updates [hasMoreNewMessages]
     * based on whether any messages were returned.
     *
     * This is used during reconnection to fetch messages that arrived while the
     * user was offline.
     *
     * @see fetchMissedMessages
     * @see updateListByActionMessages
     */
    private suspend fun fetchNextMessagesOnReconnect() {
        // Get last message ID from message list
        val lastMessageId = _messages.value.lastOrNull()?.id ?: return
        
        // Call repository.fetchNextMessages(lastMessageId)
        repository.fetchNextMessages(lastMessageId)
            .onSuccess { newMessages ->
                // Add returned messages to end of list, filtering out duplicates
                if (newMessages.isNotEmpty()) {
                    val existingIds = _messages.value.map { it.id }.toSet()
                    val uniqueNewMessages = newMessages.filter { it.id !in existingIds }
                    _messages.value = _messages.value + uniqueNewMessages
                }
                // Update hasMoreNewMessages based on result
                _hasMoreNewMessages.value = newMessages.isNotEmpty()
            }
            .onFailure { error ->
                // Log error but don't block - reconnection flow should continue
                // hasMoreNewMessages remains unchanged on failure
            }
    }

    
    // ========================================
    // UIKit Local Event Listeners
    // ========================================
    
    /**
     * Adds UIKit local event listeners for inter-component communication.
     *
     * This subscribes to [CometChatEvents.messageEvents] to handle UI-initiated
     * message actions from other components (e.g., MessageComposer).
     *
     * These are DIFFERENT from SDK listeners:
     * - SDK listeners handle server-pushed events (real-time messages from other users)
     * - UIKit events handle UI-initiated actions (user sends/edits/deletes via UI)
     *
     * @see removeLocalEventListeners
     */
    private fun addLocalEventListeners() {
        messageEventsJob = viewModelScope.launch {
            CometChatEvents.messageEvents.collect { event ->
                when (event) {
                    is CometChatMessageEvent.MessageSent -> handleMessageSentEvent(event)
                    is CometChatMessageEvent.MessageEdited -> handleMessageEditedEvent(event)
                    is CometChatMessageEvent.MessageDeleted -> handleMessageDeletedEvent(event)
                    else -> { /* Ignore other message events */ }
                }
            }
        }
        
        // Subscribe to UIKit group events
        groupEventsJob = viewModelScope.launch {
            CometChatEvents.groupEvents.collect { event ->
                when (event) {
                    is CometChatGroupEvent.MembersAdded -> handleMembersAddedEvent(event)
                    is CometChatGroupEvent.MemberKicked -> handleMemberKickedEvent(event)
                    is CometChatGroupEvent.MemberBanned -> handleMemberBannedEvent(event)
                    is CometChatGroupEvent.MemberUnbanned -> handleMemberUnbannedEvent(event)
                    is CometChatGroupEvent.MemberScopeChanged -> handleMemberScopeChangedEvent(event)
                    else -> { /* Ignore other group events */ }
                }
            }
        }
        
        // Subscribe to UIKit call events
        callEventsJob = viewModelScope.launch {
            CometChatEvents.callEvents.collect { event ->
                when (event) {
                    is CometChatCallEvent.OutgoingCall -> handleOutgoingCallEvent(event)
                    is CometChatCallEvent.CallAccepted -> handleCallAcceptedEvent(event)
                    is CometChatCallEvent.CallRejected -> handleCallRejectedEvent(event)
                    is CometChatCallEvent.CallEnded -> handleCallEndedEvent(event)
                }
            }
        }
    }
    
    /**
     * Removes UIKit local event listeners.
     *
     * @see addLocalEventListeners
     */
    private fun removeLocalEventListeners() {
        messageEventsJob?.cancel()
        messageEventsJob = null
        
        groupEventsJob?.cancel()
        groupEventsJob = null
        
        callEventsJob?.cancel()
        callEventsJob = null
    }
    
    /**
     * Handles the MessageSent UIKit event.
     *
     * This event is emitted by MessageComposer when a user sends a message.
     * The event goes through three statuses:
     * 1. IN_PROGRESS: Message is being sent (optimistic update)
     * 2. SUCCESS: Message was sent successfully (update with server response)
     * 3. ERROR: Message failed to send (show error state)
     *
     * @param event The MessageSent event containing the message and status.
     */
    private fun handleMessageSentEvent(event: CometChatMessageEvent.MessageSent) {
        val message = event.message
        
        // Check if message belongs to current conversation
        if (!isMessageForCurrentChat(message)) return
        
        // Check if this is a thread reply in main conversation
        // In this case, we only update the reply count, not add the message
        val isThreadReplyInMainConversation = parentMessageId == -1L && message.parentMessageId > 0
        
        if (isThreadReplyInMainConversation) {
            // Thread reply sent from main conversation - only update reply count on SUCCESS
            android.util.Log.d("ThreadReplyDebug", "handleMessageSentEvent: Thread reply in main conv, status=${event.status}, parentMessageId=${message.parentMessageId}")
            if (event.status == MessageStatus.SUCCESS) {
                updateReplyCount(message.parentMessageId)
            }
            return
        }
        
        // Check thread context for non-thread-reply messages
        if (!isThreadedMessageForCurrentChat(message)) return
        
        when (event.status) {
            MessageStatus.IN_PROGRESS -> {
                // Optimistic update: Add message immediately
                addMessage(message)
                // Clear smart replies when user sends a message
                clearSmartReplies()
            }
            MessageStatus.SUCCESS -> {
                // Update the optimistic message with server response
                updateMessageOnSuccess(message)
                
                // Update latestMessageId for real-time message guards
                latestMessageId = message.id
            }
            MessageStatus.ERROR -> {
                // Update message to show error state
                updateMessage(message)
            }
        }
    }
    
    /**
     * Updates a message after successful send.
     *
     * This handles the case where the optimistic message (with temp ID)
     * needs to be replaced with the server response (with real ID).
     *
     * @param message The successfully sent message from server.
     */
    private fun updateMessageOnSuccess(message: BaseMessage) {
        // First try to update by muid (temp ID used for optimistic updates)
        val muid = message.muid
        if (!muid.isNullOrEmpty()) {
            val updated = updateItem(message) { it.muid == muid }
            if (updated) return
        }
        
        // Fall back to update by ID
        val updated = updateItem(message) { it.id == message.id }
        if (!updated) {
            // Message not found, add it
            addMessage(message)
        }
    }
    
    /**
     * Handles the MessageEdited UIKit event.
     *
     * This event is emitted when a user edits a message via UI.
     * Only processes SUCCESS status as the edit is already applied on server.
     *
     * @param event The MessageEdited event containing the edited message.
     */
    private fun handleMessageEditedEvent(event: CometChatMessageEvent.MessageEdited) {
        // Only process successful edits
        if (event.status != MessageStatus.SUCCESS) return
        
        val message = event.message
        
        // Check if message belongs to current conversation
        if (!isMessageForCurrentChat(message)) return
        
        // Update the message in the list
        updateMessage(message)
    }
    
    /**
     * Handles the MessageDeleted UIKit event.
     *
     * This event is emitted when a user deletes a message via UI.
     * The message is either removed or updated based on [hideDeleteMessage] setting.
     *
     * @param event The MessageDeleted event containing the deleted message.
     */
    private fun handleMessageDeletedEvent(event: CometChatMessageEvent.MessageDeleted) {
        val message = event.message
        
        // Check if message belongs to current conversation
        if (!isMessageForCurrentChat(message)) return
        
        if (hideDeleteMessage) {
            // Remove the message completely
            removeMessage(message)
        } else {
            // Update to show "message deleted" state
            updateMessage(message)
        }
        
        // Emit event for external observers
        viewModelScope.launch {
            _messageDeleted.emit(message)
        }
    }
    
    /**
     * Checks if a group event is for the current group conversation.
     *
     * @param eventGroup The group from the event.
     * @return `true` if the event is for the current group, `false` otherwise.
     */
    private fun isEventForCurrentGroup(eventGroup: Group): Boolean {
        // Only process if we're in a group conversation
        val currentGroup = group ?: return false
        
        // Check if the event is for the current group
        return currentGroup.guid == eventGroup.guid
    }
    
    /**
     * Handles the MembersAdded UIKit event.
     *
     * This event is emitted when the current user adds members to a group via UI.
     * Each action message (one per added member) is added to the message list.
     *
     * @param event The MembersAdded event containing action messages and group info.
     */
    private fun handleMembersAddedEvent(event: CometChatGroupEvent.MembersAdded) {
        // Check if event is for current group
        if (!isEventForCurrentGroup(event.group)) return
        
        // Only process in main conversation (not thread view)
        if (parentMessageId != -1L) return
        
        // Add each action message to the list
        event.actions.forEach { action ->
            addMessage(action)
        }
    }
    
    /**
     * Handles the MemberKicked UIKit event.
     *
     * This event is emitted when the current user kicks a member from a group via UI.
     * The action message is added to the message list.
     *
     * @param event The MemberKicked event containing the action message.
     */
    private fun handleMemberKickedEvent(event: CometChatGroupEvent.MemberKicked) {
        // Check if event is for current group
        if (!isEventForCurrentGroup(event.group)) return
        
        // Only process in main conversation (not thread view)
        if (parentMessageId != -1L) return
        
        // Add the action message to the list
        addMessage(event.action)
    }
    
    /**
     * Handles the MemberBanned UIKit event.
     *
     * This event is emitted when the current user bans a member from a group via UI.
     * The action message is added to the message list.
     *
     * @param event The MemberBanned event containing the action message.
     */
    private fun handleMemberBannedEvent(event: CometChatGroupEvent.MemberBanned) {
        // Check if event is for current group
        if (!isEventForCurrentGroup(event.group)) return
        
        // Only process in main conversation (not thread view)
        if (parentMessageId != -1L) return
        
        // Add the action message to the list
        addMessage(event.action)
    }
    
    /**
     * Handles the MemberUnbanned UIKit event.
     *
     * This event is emitted when the current user unbans a member from a group via UI.
     * The action message is added to the message list.
     *
     * @param event The MemberUnbanned event containing the action message.
     */
    private fun handleMemberUnbannedEvent(event: CometChatGroupEvent.MemberUnbanned) {
        // Check if event is for current group
        if (!isEventForCurrentGroup(event.group)) return
        
        // Only process in main conversation (not thread view)
        if (parentMessageId != -1L) return
        
        // Add the action message to the list
        addMessage(event.action)
    }
    
    /**
     * Handles the MemberScopeChanged UIKit event.
     *
     * This event is emitted when the current user changes a member's scope via UI.
     * The action message is added to the message list.
     *
     * @param event The MemberScopeChanged event containing the action message.
     */
    private fun handleMemberScopeChangedEvent(event: CometChatGroupEvent.MemberScopeChanged) {
        // Check if event is for current group
        if (!isEventForCurrentGroup(event.group)) return
        
        // Only process in main conversation (not thread view)
        if (parentMessageId != -1L) return
        
        // Add the action message to the list
        addMessage(event.action)
    }
    
    // ========================================
    // UIKit Call Event Handlers
    // ========================================
    
    /**
     * Handles the OutgoingCall UIKit event.
     *
     * This event is emitted when the current user initiates an outgoing call via UI.
     * The call message is added to the message list.
     *
     * @param event The OutgoingCall event containing the call.
     */
    private fun handleOutgoingCallEvent(event: CometChatCallEvent.OutgoingCall) {
        val call = event.call
        
        // Check if call is for current conversation
        if (!isCallForCurrentChat(call)) return
        
        // Add the call message to the list
        addMessage(call)
    }
    
    /**
     * Handles the CallAccepted UIKit event.
     *
     * This event is emitted when the current user accepts a call via UI.
     * The call message is updated or added to the message list.
     *
     * @param event The CallAccepted event containing the call.
     */
    private fun handleCallAcceptedEvent(event: CometChatCallEvent.CallAccepted) {
        val call = event.call
        
        // Check if call is for current conversation
        if (!isCallForCurrentChat(call)) return
        
        // Try to update existing call message, or add if not found
        val updated = updateItem(call) { it.id == call.id }
        if (!updated) {
            addMessage(call)
        }
    }
    
    /**
     * Handles the CallRejected UIKit event.
     *
     * This event is emitted when the current user rejects a call via UI.
     * The call message is updated or added to the message list.
     *
     * @param event The CallRejected event containing the call.
     */
    private fun handleCallRejectedEvent(event: CometChatCallEvent.CallRejected) {
        val call = event.call
        
        // Check if call is for current conversation
        if (!isCallForCurrentChat(call)) return
        
        // Try to update existing call message, or add if not found
        val updated = updateItem(call) { it.id == call.id }
        if (!updated) {
            addMessage(call)
        }
    }
    
    /**
     * Handles the CallEnded UIKit event.
     *
     * This event is emitted when the current user ends a call via UI.
     * The call message is updated or added to the message list.
     *
     * @param event The CallEnded event containing the call.
     */
    private fun handleCallEndedEvent(event: CometChatCallEvent.CallEnded) {
        val call = event.call
        
        // Check if call is for current conversation
        if (!isCallForCurrentChat(call)) return
        
        // Try to update existing call message, or add if not found
        val updated = updateItem(call) { it.id == call.id }
        if (!updated) {
            addMessage(call)
        }
    }

    
    // ========================================
    // Listeners
    // ========================================
    
    /**
     * Adds CometChat listeners for real-time updates.
     */
    private fun addListeners() {
        listenersTag = "MessageList_${System.currentTimeMillis()}"
        
        listenersTag?.let { tag ->
            // Message listener
            CometChat.addMessageListener(tag, object : CometChat.MessageListener() {
                override fun onTextMessageReceived(message: TextMessage) {
                    handleIncomingMessage(message)
                }
                
                override fun onMediaMessageReceived(message: MediaMessage) {
                    handleIncomingMessage(message)
                }
                
                override fun onCustomMessageReceived(message: CustomMessage) {
                    handleIncomingMessage(message)
                }
                
                override fun onMessageEdited(message: BaseMessage) {
                    if (isMessageForCurrentChat(message)) {
                        updateMessage(message)
                    }
                }
                
                override fun onMessageDeleted(message: BaseMessage) {
                    if (isMessageForCurrentChat(message)) {
                        if (hideDeleteMessage) {
                            removeMessage(message)
                        } else {
                            updateMessage(message)
                        }
                    }
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
                
                override fun onTypingStarted(typingIndicator: TypingIndicator) {
                    handleTypingStarted(typingIndicator)
                }
                
                override fun onTypingEnded(typingIndicator: TypingIndicator) {
                    handleTypingEnded(typingIndicator)
                }
                
                override fun onMessageReactionAdded(reactionEvent: ReactionEvent) {
                    handleReactionAdded(reactionEvent)
                }
                
                override fun onMessageReactionRemoved(reactionEvent: ReactionEvent) {
                    handleReactionRemoved(reactionEvent)
                }
            })
            
            // Group listener
            CometChat.addGroupListener(tag, object : CometChat.GroupListener() {
                override fun onGroupMemberJoined(action: Action, joinedUser: User, joinedGroup: Group) {
                    if (isMessageForCurrentChat(action)) {
                        addMessage(action)
                    }
                }
                
                override fun onGroupMemberLeft(action: Action, leftUser: User, leftGroup: Group) {
                    if (isMessageForCurrentChat(action)) {
                        addMessage(action)
                    }
                }
                
                override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, kickedFrom: Group) {
                    if (isMessageForCurrentChat(action)) {
                        addMessage(action)
                    }
                }
                
                override fun onGroupMemberBanned(action: Action?, bannedUser: User?, bannedBy: User?, group: Group?) {
                    action?.let {
                        if (isMessageForCurrentChat(it)) {
                            addMessage(it)
                        }
                    }
                }
                
                override fun onGroupMemberScopeChanged(
                    action: Action?,
                    updatedBy: User?,
                    updatedUser: User?,
                    scopeChangedTo: String?,
                    scopeChangedFrom: String?,
                    group: Group?
                ) {
                    action?.let {
                        if (isMessageForCurrentChat(it)) {
                            addMessage(it)
                        }
                    }
                }
            })
            
            // Call listener
            CometChat.addCallListener(tag, object : CometChat.CallListener() {
                override fun onIncomingCallReceived(call: Call) {
                    // Call actions are handled via message listener
                }
                
                override fun onOutgoingCallAccepted(call: Call) {
                    // Call actions are handled via message listener
                }
                
                override fun onOutgoingCallRejected(call: Call) {
                    // Call actions are handled via message listener
                }
                
                override fun onIncomingCallCancelled(call: Call) {
                    // Call actions are handled via message listener
                }
                
                override fun onCallEndedMessageReceived(call: Call) {
                    if (isMessageForCurrentChat(call)) {
                        addMessage(call)
                    }
                }
            })
            
            // Connection listener
            CometChat.addConnectionListener(tag, object : CometChat.ConnectionListener {
                override fun onConnected() {
                    // Fetch missed messages on reconnection
                    // Only call fetchMissedMessages if we have existing messages
                    // Otherwise, use fetchMessages for initial load
                    if (_messages.value.isNotEmpty()) {
                        fetchMissedMessages()
                    } else {
                        fetchMessages()
                    }
                }
                
                override fun onConnecting() {}
                override fun onDisconnected() {}
                override fun onFeatureThrottled() {}
                override fun onConnectionError(error: CometChatException?) {}
            })
        }
    }
    
    /**
     * Removes all CometChat listeners.
     */
    private fun removeListeners() {
        listenersTag?.let { tag ->
            CometChat.removeMessageListener(tag)
            CometChat.removeGroupListener(tag)
            CometChat.removeCallListener(tag)
            CometChat.removeConnectionListener(tag)
        }
    }
    
    /**
     * Handles an incoming real-time message.
     *
     * This method implements a guard to prevent adding messages when the user is not
     * viewing the latest messages (i.e., they have scrolled up in the conversation).
     *
     * The guard logic:
     * - IF the message list is empty OR the last message ID equals [latestMessageId],
     *   THEN the user is at the "latest" position and the message is added
     * - ELSE the user has scrolled up, so the message is NOT added and
     *   [hasMoreNewMessages] is set to true to indicate new messages are available
     *
     * When a message is added, [latestMessageId] is updated to the new message's ID.
     *
     * @param message The incoming [BaseMessage] to handle.
     *
     * @see latestMessageId
     * @see hasMoreNewMessages
     */
    private fun handleIncomingMessage(message: BaseMessage) {
        android.util.Log.d("ThreadReplyDebug", "handleIncomingMessage: id=${message.id}, parentMessageId=${message.parentMessageId}, category=${message.category}, isForCurrentChat=${isMessageForCurrentChat(message)}, currentParentMessageId=$parentMessageId")

        if (!isMessageForCurrentChat(message)) {
            android.util.Log.d("ThreadReplyDebug", "handleIncomingMessage: NOT for current chat, skipping")
            return
        }

        // If this is a thread reply arriving in the main conversation,
        // update the parent message's reply count but don't add the message to the list.
        if (parentMessageId == -1L && message.parentMessageId > 0) {
            android.util.Log.d("ThreadReplyDebug", "handleIncomingMessage: Thread reply detected in main conversation, updating reply count for parent=${message.parentMessageId}")
            updateReplyCount(message.parentMessageId)
            return
        }

        if (!isThreadedMessageForCurrentChat(message)) {
            android.util.Log.d("ThreadReplyDebug", "handleIncomingMessage: NOT for current thread context, skipping")
            return
        }

        // Mark as delivered first (before adding to list)
        markAsDelivered(message)
        
        // Check if user is at the "latest" position
        val messages = _messages.value
        val isAtLatestPosition = messages.isEmpty() || messages.lastOrNull()?.id == latestMessageId
        
        if (isAtLatestPosition) {
            // User is at latest position - add the message and update latestMessageId
            addMessage(message)
            latestMessageId = message.id
            
            // Only emit scrollToBottomEvent for messages from OTHER users (not own messages)
            // This matches Java behavior where newMessageCount is only incremented for other users' messages
            val isFromOtherUser = message.sender?.uid != getLoggedInUserUid()
            if (isFromOtherUser) {
                playIncomingMessageSound()
                viewModelScope.launch {
                    _scrollToBottomEvent.emit(Unit)
                }
                
                // Trigger smart replies for text messages from other users
                if (shouldFetchSmartReplies(message)) {
                    scheduleSmartRepliesFetch(message as TextMessage)
                }
            }
        } else {
            // User has scrolled up - don't add message, just indicate more messages available
            _hasMoreNewMessages.value = true
        }
    }
    
    /**
     * Handles message receipts with proper validation for current chat context.
     * 
     * For USER chats: Validates that the receipt sender matches the current chat user ID
     * For GROUP chats: Validates that the receipt receiver matches the current group ID
     * 
     * Receipt types handled:
     * - DELIVERED / DELIVERED_TO_ALL: Updates deliveredAt timestamp
     * - READ / READ_BY_ALL: Updates readAt timestamp
     */
    private fun handleMessageReceipt(receipt: MessageReceipt) {
        if (disableReceipt) return
        
        val currentId = user?.uid ?: group?.guid ?: return
        
        // Validate receipt is for current chat and correct receipt type
        when (receipt.receiverType) {
            CometChatConstants.RECEIVER_TYPE_USER -> {
                // For user chats, the sender of the receipt should match the current chat user
                // Only process DELIVERED and READ receipts (not TO_ALL variants)
                if (receipt.sender?.uid != currentId) return
                
                when (receipt.receiptType) {
                    MessageReceipt.RECEIPT_TYPE_DELIVERED -> setDeliveryReceipts(receipt)
                    MessageReceipt.RECEIPT_TYPE_READ -> setReadReceipts(receipt)
                }
            }
            CometChatConstants.RECEIVER_TYPE_GROUP -> {
                // For group chats, the receiver ID should match the current group
                // Only process DELIVERED_TO_ALL and READ_BY_ALL receipts
                if (receipt.receiverId != currentId) return
                
                when (receipt.receiptType) {
                    MessageReceipt.RECEIPT_TYPE_DELIVERED_TO_ALL -> setDeliveryReceipts(receipt)
                    MessageReceipt.RECEIPT_TYPE_READ_BY_ALL -> setReadReceipts(receipt)
                }
            }
        }
    }
    
    /**
     * Updates deliveredAt timestamp for all messages up to and including the receipt message ID.
     * Iterates from the end of the list and stops when it finds a message that already has deliveredAt set.
     */
    private fun setDeliveryReceipts(receipt: MessageReceipt) {
        var isDelivered = false
        val updatedMessages = _messages.value.toMutableList()
        
        for (i in updatedMessages.indices.reversed()) {
            val message = updatedMessages[i]
            if (message.deliveredAt == 0L || message.id == receipt.messageId) {
                isDelivered = true
                message.deliveredAt = receipt.deliveredAt
            } else if (isDelivered) {
                break
            }
        }
        
        _messages.value = updatedMessages
    }
    
    /**
     * Updates readAt timestamp for all messages up to and including the receipt message ID.
     * Iterates from the end of the list and stops when it finds a message that already has readAt set.
     */
    private fun setReadReceipts(receipt: MessageReceipt) {
        var isRead = false
        val updatedMessages = _messages.value.toMutableList()
        
        for (i in updatedMessages.indices.reversed()) {
            val message = updatedMessages[i]
            if (message.readAt == 0L || message.id == receipt.messageId) {
                isRead = true
                message.readAt = receipt.readAt
            } else if (isRead) {
                break
            }
        }
        
        _messages.value = updatedMessages
    }
    
    private fun handleTypingStarted(typingIndicator: TypingIndicator) {
        val id = user?.uid ?: group?.guid ?: return
        val indicatorId = when (typingIndicator.receiverType) {
            CometChatConstants.RECEIVER_TYPE_USER -> typingIndicator.sender?.uid
            CometChatConstants.RECEIVER_TYPE_GROUP -> typingIndicator.receiverId
            else -> null
        }
        
        if (indicatorId == id) {
            typingIndicator.sender?.let { sender ->
                val currentList = _typingUsers.value.toMutableList()
                if (currentList.none { it.uid == sender.uid }) {
                    currentList.add(sender)
                    _typingUsers.value = currentList
                }
            }
        }
    }
    
    private fun handleTypingEnded(typingIndicator: TypingIndicator) {
        typingIndicator.sender?.let { sender ->
            _typingUsers.value = _typingUsers.value.filter { it.uid != sender.uid }
        }
    }
    
    private fun handleReactionAdded(reactionEvent: ReactionEvent) {
        // Guard: Skip if reactions are disabled
        if (disableReactions) return
        
        val messageId = reactionEvent.reaction?.messageId ?: return
        val conversationId = reactionEvent.conversationId ?: return
        
        // Check if reaction is for current conversation
        val currentConversationId = when {
            user != null -> "user_${user?.uid}"
            group != null -> "group_${group?.guid}"
            else -> return
        }
        
        // Simplified check - just verify the message is in our list
        val existingMessage = _messages.value.find { it.id == messageId }
        if (existingMessage != null) {
            // Fetch updated message to get new reaction state
            viewModelScope.launch {
                repository.getMessage(messageId)
                    .onSuccess { updatedMessage ->
                        updateMessage(updatedMessage)
                    }
            }
        }
    }
    
    private fun handleReactionRemoved(reactionEvent: ReactionEvent) {
        // Guard: Skip if reactions are disabled
        if (disableReactions) return
        
        val messageId = reactionEvent.reaction?.messageId ?: return
        
        // Check if the message is in our list
        val existingMessage = _messages.value.find { it.id == messageId }
        if (existingMessage != null) {
            // Fetch updated message to get new reaction state
            viewModelScope.launch {
                repository.getMessage(messageId)
                    .onSuccess { updatedMessage ->
                        updateMessage(updatedMessage)
                    }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}
