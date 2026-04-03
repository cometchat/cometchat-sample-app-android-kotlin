package com.cometchat.uikit.kotlin.presentation.messagelist.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.StyleRes
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.Reaction
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleStyles
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubble
import com.cometchat.uikit.core.domain.model.CometChatMessageOption
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble.CometChatActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble.CometChatCallActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.CometChatMessageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.InternalContentRenderer
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header.StickyHeaderAdapter
import com.cometchat.uikit.kotlin.shared.resources.utils.unread_message_decoration.NewMessageIndicatorDecorationAdapter
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import java.text.SimpleDateFormat

/**
 * Data class representing configurable margins for message bubbles.
 *
 * Each margin value defaults to -1, which indicates "no change" (the margin
 * will not be applied). Positive values will be applied as pixel margins.
 *
 * @property top Top margin in pixels, or -1 to not apply
 * @property bottom Bottom margin in pixels, or -1 to not apply
 * @property start Start margin in pixels, or -1 to not apply
 * @property end End margin in pixels, or -1 to not apply
 */
data class BubbleMargins(
    val top: Int = -1,
    val bottom: Int = -1,
    val start: Int = -1,
    val end: Int = -1
)

// ========================================
// Callback Type Aliases
// ========================================

/**
 * Callback invoked when a message is long-pressed.
 *
 * @param options List of available message options from the factory
 * @param message The message that was long-pressed
 * @param factory The BubbleFactory for this message type (may be null)
 * @param bubble The CometChatMessageBubble view
 */
typealias OnMessageLongClick = (
    options: List<CometChatMessageOption>,
    message: BaseMessage,
    factory: BubbleFactory?,
    bubble: CometChatMessageBubble
) -> Unit

/**
 * Functional interface for thread reply click events.
 * Invoked when the user clicks on the thread view to view replies.
 */
fun interface ThreadReplyClick {
    /**
     * Called when the thread reply view is clicked.
     *
     * @param context The Android context
     * @param message The message with thread replies
     * @param factory The BubbleFactory for this message type (may be null)
     */
    fun onThreadReplyClick(
        context: Context,
        message: BaseMessage,
        factory: BubbleFactory?
    )
}

/**
 * Callback invoked when a reaction is clicked.
 *
 * @param reaction The reaction that was clicked
 * @param message The message the reaction belongs to
 */
typealias OnReactionClick = (reaction: Reaction, message: BaseMessage) -> Unit

/**
 * Callback invoked when a reaction is long-pressed.
 *
 * @param reaction The reaction that was long-pressed
 * @param message The message the reaction belongs to
 */
typealias OnReactionLongClick = (reaction: Reaction, message: BaseMessage) -> Unit

/**
 * Callback invoked when the "add more reactions" button is clicked.
 *
 * @param message The message to add a reaction to
 */
typealias OnAddMoreReactionsClick = (message: BaseMessage) -> Unit

/**
 * Callback invoked when a message preview is clicked.
 *
 * @param message The message whose preview was clicked
 */
typealias OnMessagePreviewClick = (message: BaseMessage) -> Unit

/**
 * RecyclerView adapter for displaying messages in a chat list.
 *
 * This adapter handles:
 * - View type calculation based on BubbleFactory (category + type) and alignment
 * - ViewHolder creation with factory references
 * - Data binding for CometChatMessageBubble components
 * - Sticky headers for date separators
 * - New message indicators for unread messages
 *
 * ## View Type Format
 *
 * View types are calculated as: `factoryId + alignmentSuffix`
 * - Factory ID: Unique integer assigned per factory key (category_type)
 * - Alignment suffix: "1" (LEFT), "2" (RIGHT), "3" (CENTER)
 * - Special types: "4" (STREAM), "10000" (IGNORE)
 *
 * ## Factory Key Format
 *
 * Factory keys are derived from message: `{category}_{type}`
 * Examples: "message_text", "message_image", "action_groupMember", "call_audio"
 *
 * ## BubbleFactory Management
 *
 * The adapter starts with an empty bubbleFactories map. Factories are optionally
 * registered by the consumer. If no factory exists for a message's category_type,
 * null is passed to CometChatMessageBubble which handles default rendering internally.
 *
 * @see BubbleFactory
 * @see StickyHeaderAdapter
 * @see NewMessageIndicatorDecorationAdapter
 */
class MessageAdapter @JvmOverloads constructor(
    context: Context? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    StickyHeaderAdapter<MessageAdapter.DateItemHolder>,
    NewMessageIndicatorDecorationAdapter<MessageAdapter.NewMessageIndicatorViewHolder> {

    /**
     * Internal context storage. Set from constructor or from first ViewHolder creation.
     */
    private var _context: Context? = context

    /**
     * Gets the context, either from constructor or from first ViewHolder creation.
     */
    private val context: Context
        get() = _context ?: throw IllegalStateException("Context not available. Use constructor with context or wait for onCreateViewHolder.")

    /**
     * Sets the context if not already set. Called from onCreateViewHolder.
     */
    private fun ensureContext(parent: ViewGroup) {
        if (_context == null) {
            _context = parent.context
        }
    }

    // ========================================
    // View Type Constants
    // ========================================

    companion object {
        /**
         * Alignment suffix for LEFT-aligned messages (incoming).
         * Appended to factory ID to form view type.
         */
        private const val LEFT_MESSAGE = "1"

        /**
         * Alignment suffix for RIGHT-aligned messages (outgoing).
         * Appended to factory ID to form view type.
         */
        private const val RIGHT_MESSAGE = "2"

        /**
         * Alignment suffix for CENTER-aligned messages (action/call).
         * Appended to factory ID to form view type.
         */
        private const val CENTER_MESSAGE = "3"

        /**
         * View type for stream messages (AI assistant responses).
         */
        private const val STREAM_MESSAGE = "4"

        /**
         * View type for ignored messages (hidden group actions).
         * When hideGroupActionMessage is true and message category is ACTION.
         */
        private const val IGNORE_MESSAGE = "10000"
    }

    // ========================================
    // Data
    // ========================================

    /**
     * The list of messages to display.
     */
    private var messages: MutableList<BaseMessage> = mutableListOf()

    // ========================================
    // BubbleFactory Management
    // ========================================

    /**
     * Map of factory key (category_type) to BubbleFactory.
     * Empty by default - factories are optionally registered by consumer.
     */
    private var bubbleFactories: MutableMap<String, BubbleFactory> = mutableMapOf()

    /**
     * Maps factory key (category_type) to assigned view type ID.
     * Used to ensure consistent view type IDs for the same factory key.
     */
    private val factoryViewTypeHashMap: MutableMap<String, Int> = mutableMapOf()

    /**
     * Maps view type ID to BubbleFactory for retrieval in onCreateViewHolder.
     * May contain null values if no factory is registered for a message type.
     */
    private val viewTypeFactoryHashMap: MutableMap<Int, BubbleFactory?> = mutableMapOf()

    /**
     * Maps view type ID to factory key (category_type) for retrieval in onCreateViewHolder.
     * Used to extract the factory key from a view type during ViewHolder creation.
     * Populated in getItemViewType, consumed in onCreateViewHolder.
     */
    private val viewTypeToFactoryKeyMap: MutableMap<Int, String> = mutableMapOf()

    // ========================================
    // Style Configuration Properties (Task 11.1)
    // ========================================

    /**
     * Style object for incoming (left-aligned) message bubbles.
     * Applied to CometChatMessageBubble for messages from other users.
     * When null, the component uses CometChatMessageBubbleStyle.incoming(context) as default.
     */
    var incomingMessageBubbleStyle: CometChatMessageBubbleStyle? = null

    /**
     * Style object for outgoing (right-aligned) message bubbles.
     * Applied to CometChatMessageBubble for messages from the logged-in user.
     * When null, the component uses CometChatMessageBubbleStyle.outgoing(context) as default.
     */
    var outgoingMessageBubbleStyle: CometChatMessageBubbleStyle? = null

    /**
     * Style object for date separator headers.
     * Applied to CometChatDate view in sticky headers.
     * When null, the component uses CometChatDateStyle.default(context) as default.
     */
    var dateSeparatorStyleObject: CometChatDateStyle? = null

    /**
     * Style resource ID for moderation status view.
     * Applied to the bottom view showing moderation status for outgoing messages.
     */
    @StyleRes
    var moderationViewStyle: Int = 0

    /**
     * Style object for action/system message bubbles.
     * Applied to CometChatActionBubble for centered system messages (e.g., "User joined the group").
     * When null, the component uses CometChatActionBubbleStyle.default(context) as default.
     */
    var actionBubbleStyle: CometChatActionBubbleStyle? = null

    /**
     * Style object for call action bubbles.
     * Applied to CometChatCallActionBubble for call-related system messages.
     * When null, the component uses CometChatCallActionBubbleStyle.default(context) as default.
     */
    var callActionBubbleStyle: CometChatCallActionBubbleStyle? = null

    // ========================================
    // Cached Bubble Styles
    // ========================================

    /**
     * Cached BubbleStyles instance containing loaded per-bubble-type styles.
     * Lazily loaded when first needed, invalidated when style objects change.
     */
    private var cachedBubbleStyles: BubbleStyles? = null

    /**
     * Gets or creates the BubbleStyles instance with loaded per-bubble-type styles.
     *
     * This method uses the style objects directly (or defaults if null) and caches them for reuse.
     * The cache is invalidated when style objects change.
     *
     * @return The BubbleStyles instance with loaded styles
     */
    private fun getOrCreateBubbleStyles(): BubbleStyles {
        cachedBubbleStyles?.let { return it }

        val resolvedActionStyle = actionBubbleStyle ?: CometChatActionBubbleStyle.default(context)
        val resolvedCallActionStyle = callActionBubbleStyle ?: CometChatCallActionBubbleStyle.default(context)

        val styles = BubbleStyles(
            actionBubbleStyle = resolvedActionStyle,
            callActionBubbleStyle = resolvedCallActionStyle
        )
        cachedBubbleStyles = styles
        return styles
    }

    /**
     * Invalidates the cached bubble styles, forcing them to be reloaded on next access.
     * Call this when style objects change.
     */
    private fun invalidateBubbleStylesCache() {
        cachedBubbleStyles = null
    }

    // ========================================
    // Margin Configuration Properties (Task 11.2)
    // ========================================

    /**
     * Configurable margins for left-aligned (incoming) message bubbles.
     * Values of -1 indicate no change to the default margins.
     */
    var leftBubbleMargins: BubbleMargins = BubbleMargins()

    /**
     * Configurable margins for right-aligned (outgoing) message bubbles.
     * Values of -1 indicate no change to the default margins.
     */
    var rightBubbleMargins: BubbleMargins = BubbleMargins()

    // ========================================
    // Timestamp Configuration Properties (Task 11.3)
    // ========================================

    /**
     * Controls the position of timestamps on message bubbles.
     * - TOP: Timestamp displayed in the header view
     * - BOTTOM: Timestamp displayed in the status info view
     */
    var timeStampAlignment: UIKitConstants.TimeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM

    /**
     * Custom time format for message timestamps.
     * Default format is "h:mm a" (e.g., "2:30 PM").
     */
    var timeFormat: SimpleDateFormat = SimpleDateFormat("h:mm a", java.util.Locale.getDefault())

    /**
     * Custom date format for date separators.
     * If null, the default format is used.
     */
    var dateSeparatorFormat: SimpleDateFormat? = null

    /**
     * Custom date/time formatter callback for date separators.
     * Allows custom formatting for today, yesterday, and other days.
     */
    var dateTimeFormatter: DateTimeFormatterCallback? = null

    // ========================================
    // Avatar Configuration Properties (Task 11.4)
    // ========================================

    /**
     * When true, shows avatar for all left-aligned messages regardless of conversation type.
     */
    var showAvatar: Boolean = false

    /**
     * When true, shows avatar for left-aligned messages in 1-on-1 (user) conversations.
     * Only applies when showAvatar is false.
     */
    var showLeftBubbleUserAvatar: Boolean = false

    /**
     * When true, shows avatar for left-aligned messages in group conversations.
     * Only applies when showAvatar is false.
     */
    var showLeftBubbleGroupAvatar: Boolean = true

    // ========================================
    // Behavior Flags (Task 11.5)
    // ========================================

    /**
     * When true, read receipts are not shown on messages.
     */
    var disableReadReceipt: Boolean = false

    /**
     * When true, group action messages return IGNORE_MESSAGE view type.
     */
    var hideGroupActionMessage: Boolean = false

    /**
     * When true, reaction footer is hidden on all messages.
     */
    var disableReactions: Boolean = false

    /**
     * When true, moderation status view is hidden on outgoing messages.
     */
    var hideModerationView: Boolean = false

    /**
     * When true, enables agent chat mode with simplified UI:
     * - Header view is not created for left-aligned messages
     * - Footer view (reactions) is hidden
     * - Thread view is not created for right-aligned messages
     * - Long press callback is not invoked
     */
    var isAgentChat: Boolean = false

    /**
     * When true, thread view is hidden on all messages.
     * Used by CometChatThreadHeader to hide thread indicators on the parent message bubble.
     */
    var hideThreadView: Boolean = false

    // ========================================
    // Text Formatter Configuration
    // ========================================

    /**
     * List of text formatters for message text rendering.
     * These formatters are applied to text messages to handle mentions, links, markdown, etc.
     * Propagated from CometChatMessageList via AdditionParameter.
     */
    var textFormatters: List<CometChatTextFormatter> = emptyList()

    // ========================================
    // Highlight Configuration (Task 11.6)
    // ========================================

    /**
     * The ID of the message to highlight (e.g., when jumping to a quoted message).
     * Set to -1 to disable highlighting.
     */
    var highlightedMessageId: Long = -1L

    /**
     * Alpha value for the highlight background color (0.0 to 1.0).
     */
    var highlightAlpha: Float = 0.3f

    // ========================================
    // Conversation Context Properties (Task 11.7)
    // ========================================

    /**
     * User for 1-on-1 conversations. Used for message options.
     */
    var user: User? = null

    /**
     * Group for group conversations. Used for message options.
     */
    var group: Group? = null

    /**
     * Controls the overall alignment of messages in the list.
     * - STANDARD: Outgoing messages on right, incoming on left
     * - LEFT_ALIGNED: All messages aligned to the left
     */
    var listAlignment: UIKitConstants.MessageListAlignment = UIKitConstants.MessageListAlignment.STANDARD

    /**
     * The layout direction for RTL support.
     * When set to View.LAYOUT_DIRECTION_RTL, LEFT and RIGHT alignments are swapped.
     */
    var layoutDirection: Int = View.LAYOUT_DIRECTION_LTR

    // ========================================
    // New Message Indicator Configuration
    // ========================================

    /**
     * Custom view for the unread message indicator.
     * If null, the default layout (cometchat_new_message_indicator) is used.
     */
    var customUnreadHeaderView: View? = null

    // ========================================
    // Callbacks
    // ========================================

    /**
     * Callback invoked when a message is long-pressed.
     * Only invoked for non-deleted messages (deletedAt == 0).
     * Parameters: options, message, factory, bubble
     */
    var onMessageLongClick: OnMessageLongClick? = null

    /**
     * Callback invoked when the thread reply view is clicked.
     * Allows navigation to the thread view for a message with replies.
     */
    var threadReplyClick: ThreadReplyClick? = null

    /**
     * Callback invoked when a reaction is clicked.
     * Parameters: reaction, message
     */
    var onReactionClick: OnReactionClick? = null

    /**
     * Callback invoked when a reaction is long-pressed.
     * Parameters: reaction, message
     */
    var onReactionLongClick: OnReactionLongClick? = null

    /**
     * Callback invoked when the "add more reactions" button is clicked.
     * Parameter: message
     */
    var onAddMoreReactionsClick: OnAddMoreReactionsClick? = null

    /**
     * Callback invoked when a message preview is clicked.
     * Parameter: message
     */
    var onMessagePreviewClick: OnMessagePreviewClick? = null

    // ========================================
    // Adapter Methods
    // ========================================

    override fun getItemCount(): Int = messages.size

    /**
     * Calculates the view type for a message at the given position.
     *
     * View type format: factoryId + alignmentSuffix
     * - Factory ID is derived from message category_type, not from registered factories
     * - Alignment suffix is based on message category, sender UID, and listAlignment
     *
     * Special cases:
     * - Stream messages return STREAM_MESSAGE (4)
     * - Hidden group actions return IGNORE_MESSAGE (10000)
     *
     * @param position The position of the message in the list
     * @return The calculated view type integer
     */
    override fun getItemViewType(position: Int): Int {
        val baseMessage = messages[position]

        // 1. Handle stream messages
        if (baseMessage.category == UIKitConstants.MessageCategory.STREAM &&
            baseMessage.type == UIKitConstants.MessageType.STREAM) {
            return STREAM_MESSAGE.toInt()
        }

        // 2. Get factory key from message (checks deletedAt first, then category_type)
        val factoryKey = BubbleFactory.getFactoryKey(baseMessage)

        // 3. Get or assign factory ID (based on message category_type, not factory registration)
        val factoryId = factoryViewTypeHashMap.getOrPut(factoryKey) {
            factoryViewTypeHashMap.size + 1
        }

        // 4. Determine alignment suffix
        val alignmentSuffix = calculateAlignmentSuffix(baseMessage)

        // 5. Handle IGNORE_MESSAGE case (returns early)
        if (alignmentSuffix == IGNORE_MESSAGE) {
            return IGNORE_MESSAGE.toInt()
        }

        // 6. Combine factory ID + alignment suffix
        val viewType = "$factoryId$alignmentSuffix".toInt()

        // 7. Store factory reference if available (may be null if no factory registered)
        val factory = bubbleFactories[factoryKey]
        viewTypeFactoryHashMap[viewType] = factory

        // 8. Store factory key for extraction in onCreateViewHolder
        viewTypeToFactoryKeyMap[viewType] = factoryKey

        return viewType
    }

    /**
     * Calculates the alignment suffix based on message category, sender UID, and listAlignment.
     *
     * Rules:
     * - ACTION category with hideGroupActionMessage: IGNORE_MESSAGE
     * - ACTION or CALL category: CENTER_MESSAGE
     * - LEFT_ALIGNED mode: LEFT_MESSAGE (regardless of sender)
     * - Sender UID matches logged-in user: RIGHT_MESSAGE
     * - Otherwise: LEFT_MESSAGE
     *
     * @param message The message to calculate alignment for
     * @return The alignment suffix string
     */
    private fun calculateAlignmentSuffix(message: BaseMessage): String {
        return when {
            // ACTION messages with hideGroupActionMessage enabled
            message.category == CometChatConstants.CATEGORY_ACTION -> {
                if (hideGroupActionMessage) IGNORE_MESSAGE else CENTER_MESSAGE
            }
            // CALL messages are always centered
            message.category == CometChatConstants.CATEGORY_CALL -> CENTER_MESSAGE
            // LEFT_ALIGNED mode forces all messages to left
            listAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED -> LEFT_MESSAGE
            // Standard alignment based on sender
            message.sender?.uid == CometChat.getLoggedInUser()?.uid -> RIGHT_MESSAGE
            // Default to left for incoming messages
            else -> LEFT_MESSAGE
        }
    }

    /**
     * Determines the message alignment enum based on message properties.
     *
     * @param message The message to determine alignment for
     * @return The MessageBubbleAlignment enum value
     */
    internal fun getMessageAlignment(message: BaseMessage): UIKitConstants.MessageBubbleAlignment {
        // Action and call messages are always centered
        if (message.category == CometChatConstants.CATEGORY_ACTION ||
            message.category == CometChatConstants.CATEGORY_CALL) {
            return UIKitConstants.MessageBubbleAlignment.CENTER
        }

        // LEFT_ALIGNED mode forces all messages to left
        if (listAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED) {
            return UIKitConstants.MessageBubbleAlignment.LEFT
        }

        // Standard alignment based on sender
        val loggedInUser = CometChat.getLoggedInUser()
        return if (message.sender?.uid == loggedInUser?.uid) {
            UIKitConstants.MessageBubbleAlignment.RIGHT
        } else {
            UIKitConstants.MessageBubbleAlignment.LEFT
        }
    }

    /**
     * Adjusts the alignment for RTL (Right-to-Left) layout direction.
     *
     * When the layout direction is RTL:
     * - LEFT alignment is swapped to RIGHT
     * - RIGHT alignment is swapped to LEFT
     * - CENTER alignment remains unchanged
     *
     * @param alignment The original alignment
     * @return The adjusted alignment for RTL, or the original if not RTL
     */
    internal fun adjustAlignmentForRTL(alignment: UIKitConstants.MessageBubbleAlignment): UIKitConstants.MessageBubbleAlignment {
        if (layoutDirection != View.LAYOUT_DIRECTION_RTL) {
            return alignment
        }

        return when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> UIKitConstants.MessageBubbleAlignment.RIGHT
            UIKitConstants.MessageBubbleAlignment.RIGHT -> UIKitConstants.MessageBubbleAlignment.LEFT
            UIKitConstants.MessageBubbleAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
        }
    }

    /**
     * Extracts the alignment from a view type by reading the last digit.
     *
     * View types are encoded as "{factoryId}{alignmentSuffix}" where:
     * - '1' = LEFT alignment
     * - '2' = RIGHT alignment
     * - '3' = CENTER alignment
     *
     * @param viewType The view type to extract alignment from
     * @return The MessageBubbleAlignment, defaults to LEFT if suffix is unrecognized
     */
    internal fun extractAlignmentFromViewType(viewType: Int): UIKitConstants.MessageBubbleAlignment {
        val suffix = viewType.toString().last()
        return when (suffix) {
            '1' -> UIKitConstants.MessageBubbleAlignment.LEFT
            '2' -> UIKitConstants.MessageBubbleAlignment.RIGHT
            '3' -> UIKitConstants.MessageBubbleAlignment.CENTER
            else -> UIKitConstants.MessageBubbleAlignment.LEFT
        }
    }

    /**
     * Determines whether the sender name should be hidden for a message.
     *
     * Rules:
     * - LEFT_ALIGNED mode: Always show name (return false)
     * - STANDARD mode: Show name only for group messages on left or right side
     * - Otherwise: Hide name (return true)
     *
     * @param message The message to check
     * @param alignment The bubble alignment
     * @return true if the name should be hidden, false if it should be shown
     */
    internal fun isHideName(message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment): Boolean {
        // LEFT_ALIGNED mode: always show name
        if (listAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED) {
            return false
        }

        // STANDARD mode: show name only for group messages on left or right side
        if (listAlignment == UIKitConstants.MessageListAlignment.STANDARD &&
            (alignment == UIKitConstants.MessageBubbleAlignment.LEFT || alignment == UIKitConstants.MessageBubbleAlignment.RIGHT) &&
            message.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
            return false
        }

        return true
    }

    /**
     * Determines whether read receipts should be shown for a message.
     *
     * Read receipts are shown when:
     * - Layout direction is NOT RTL
     * - disableReadReceipt is false
     * - Alignment is NOT LEFT (only show for outgoing messages)
     * - Message is NOT deleted
     *
     * @param message The message to check
     * @param alignment The bubble alignment
     * @return true if read receipts should be shown
     */
    internal fun shouldShowReadReceipt(message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment): Boolean {
        return layoutDirection != View.LAYOUT_DIRECTION_RTL &&
               !disableReadReceipt &&
               alignment != UIKitConstants.MessageBubbleAlignment.LEFT &&
               message.deletedAt == 0L
    }

    /**
     * Determines whether the avatar should be shown for a message.
     *
     * Avatar visibility is controlled by:
     * 1. [showAvatar] - Global override: if true, always show avatar for left-aligned messages
     * 2. [showLeftBubbleUserAvatar] - Show avatar in 1-on-1 (user) conversations
     * 3. [showLeftBubbleGroupAvatar] - Show avatar in group conversations
     *
     * @param message The message to check
     * @return true if avatar should be shown for this message
     */
    internal fun shouldShowAvatarForMessage(message: BaseMessage): Boolean {
        // Global override - if showAvatar is true, always show
        if (showAvatar) return true
        
        // Check based on conversation type
        return when (message.receiverType) {
            CometChatConstants.RECEIVER_TYPE_USER -> showLeftBubbleUserAvatar
            CometChatConstants.RECEIVER_TYPE_GROUP -> showLeftBubbleGroupAvatar
            else -> false
        }
    }

    // ========================================
    // ViewHolder Creation (Task 2)
    // ========================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Ensure context is available for backward compatibility
        ensureContext(parent)
        
        val viewTypeString = viewType.toString()

        return when {
            viewTypeString == STREAM_MESSAGE -> createStreamViewHolder(parent)
            viewTypeString == IGNORE_MESSAGE || viewTypeString.endsWith(IGNORE_MESSAGE) -> {
                EmptyRowHolder(createEmptyView(parent.context))
            }
            else -> {
                // Extract factory key from view type mapping (populated in getItemViewType)
                val factoryKey = viewTypeToFactoryKeyMap[viewType]
                    ?: throw IllegalStateException(
                        "No factory key mapping for viewType $viewType. " +
                        "This should never happen - getItemViewType must be called before onCreateViewHolder."
                    )
                
                // Extract alignment from view type suffix
                val alignment = extractAlignmentFromViewType(viewType)
                
                // Get factory (may be null if no factory registered for this message type)
                val factory = viewTypeFactoryHashMap[viewType]
                
                // Create row with pre-initialized bubble
                val (rowRoot, bubble) = createMessageRowWithPreInitializedBubble(
                    parent.context,
                    alignment,
                    factoryKey,
                    factory
                )

                MessageViewHolder(rowRoot, bubble, factory)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages.getOrNull(position) ?: return

        when (holder) {
            is MessageViewHolder -> {
                val alignment = getMessageAlignment(message)
                holder.bind(message, alignment, position)
            }
            is StreamBubbleViewHolder -> {
                holder.bind(message, position)
            }
            // EmptyRowHolder needs no binding
        }
    }

    /**
     * Called when a ViewHolder is recycled. Releases resources held by the message bubble.
     *
     * For MessageViewHolder, this calls messageBubble.onRecycled() which internally
     * notifies the factory to release resources (e.g., cancel image loads, stop animations).
     *
     * @param holder The ViewHolder being recycled
     */
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is MessageViewHolder -> holder.messageBubble.onRecycled()
        }
    }

    // ========================================
    // ViewHolder Classes (Task 2)
    // ========================================

    /**
     * Single ViewHolder for all message types (LEFT, RIGHT, CENTER).
     * The row layout handles alignment (gravity, margins).
     * CometChatMessageBubble handles all rendering internally based on factory and alignment.
     */
    inner class MessageViewHolder(
        private val rowRoot: LinearLayout,
        val messageBubble: CometChatMessageBubble,
        private val factory: BubbleFactory?
    ) : RecyclerView.ViewHolder(rowRoot) {

        /**
         * Binds the message data to the ViewHolder.
         *
         * @param message The message to bind
         * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
         * @param position Position in the list
         */
        fun bind(message: BaseMessage, alignment: UIKitConstants.MessageBubbleAlignment, position: Int) {
            // Apply highlight if needed - the highlight color is based on highlightAlpha
            if (message.id == highlightedMessageId) {
                rowRoot.setBackgroundColor(getHighlightColor())
            } else {
                rowRoot.setBackgroundColor(Color.TRANSPARENT)
            }

            // Pass text formatters to bubble before binding
            messageBubble.setTextFormatters(textFormatters)

            // Pass date/time formatting configuration to bubble before binding
            messageBubble.setTimeFormat(timeFormat)
            messageBubble.setDateTimeFormatter(dateTimeFormatter)
            messageBubble.setTimeStampAlignment(timeStampAlignment)

            // Pass per-bubble-type styles to bubble before binding
            // This includes loaded actionBubbleStyle and callActionBubbleStyle
            messageBubble.setBubbleStyles(getOrCreateBubbleStyles())

            // Pass incoming/outgoing message bubble style objects to bubble
            messageBubble.setIncomingMessageBubbleStyleObject(incomingMessageBubbleStyle)
            messageBubble.setOutgoingMessageBubbleStyleObject(outgoingMessageBubbleStyle)

            // Pass visibility settings to bubble before binding
            // These are applied during bindViews() via InternalContentRenderer
            messageBubble.setReceiptsVisibility(
                if (disableReadReceipt) View.GONE else View.VISIBLE
            )
            messageBubble.setReactionVisibility(
                if (disableReactions) View.GONE else View.VISIBLE
            )

            // Set message preview click listener BEFORE bindViews() so it's available
            // when InternalContentRenderer.bindReplyView() is called
            if (message.deletedAt == 0L) {
                messageBubble.setOnMessagePreviewClickListener { clickedMessage ->
                    onMessagePreviewClick?.invoke(clickedMessage)
                }
            } else {
                messageBubble.setOnMessagePreviewClickListener(null)
            }

            // Bubble handles all slot binding internally based on alignment
            messageBubble.bindViews(message, alignment, this, position)

            // Bind reaction callbacks to the footer view after bindViews has created it
            if (!disableReactions) {
                val footerView = messageBubble.getFooterView()
                if (footerView != null && footerView.childCount > 0) {
                    val footerContent = footerView.getChildAt(0)
                    if (footerContent != null) {
                        InternalContentRenderer.bindReactionCallbacks(
                            footerContent,
                            0, // default reaction style
                            onReactionClick?.let { click ->
                                com.cometchat.uikit.kotlin.presentation.shared.reaction.OnReactionClick { reaction, msg ->
                                    val reactionObj = com.cometchat.chat.models.Reaction().apply {
                                        this.reaction = reaction
                                    }
                                    click.invoke(reactionObj, msg)
                                }
                            },
                            onReactionLongClick?.let { longClick ->
                                com.cometchat.uikit.kotlin.presentation.shared.reaction.OnReactionLongClick { reaction, msg ->
                                    val reactionObj = com.cometchat.chat.models.Reaction().apply {
                                        this.reaction = reaction
                                    }
                                    longClick.invoke(reactionObj, msg)
                                }
                            },
                            onAddMoreReactionsClick?.let { addMore ->
                                com.cometchat.uikit.kotlin.presentation.shared.reaction.OnAddMoreReactionsClick { msg ->
                                    addMore.invoke(msg)
                                }
                            }
                        )
                    }
                }
            }

            // Apply avatar visibility based on showAvatar setting
            // Avatar is only shown for left-aligned messages (incoming)
            if (alignment == UIKitConstants.MessageBubbleAlignment.LEFT) {
                val shouldShowAvatarForMessage = shouldShowAvatarForMessage(message)
                messageBubble.setAvatarVisibility(
                    if (shouldShowAvatarForMessage) View.VISIBLE else View.GONE
                )
            } else {
                // Hide avatar for right-aligned and center-aligned messages
                messageBubble.setAvatarVisibility(View.GONE)
            }

            // Apply header (sender name) visibility based on conversation type
            // In 1-on-1 conversations, hide the sender name for incoming messages
            if (alignment == UIKitConstants.MessageBubbleAlignment.LEFT && isHideName(message, alignment)) {
                messageBubble.setHeaderViewVisibility(View.GONE)
            }

            // Agent chat mode: Apply visibility restrictions
            if (isAgentChat) {
                // Hide header view for left-aligned messages (incoming)
                if (alignment == UIKitConstants.MessageBubbleAlignment.LEFT) {
                    messageBubble.setHeaderViewVisibility(View.GONE)
                }
                
                // Hide thread view for right-aligned messages (outgoing)
                if (alignment == UIKitConstants.MessageBubbleAlignment.RIGHT) {
                    messageBubble.setThreadViewVisibility(View.GONE)
                    messageBubble.setOnThreadViewClickListener(null)
                }
                
                // Hide footer view (reactions) for all messages
                messageBubble.setFooterViewVisibility(View.GONE)
                
                // Skip long press callback - set null listener
                rowRoot.setOnLongClickListener(null)
            } else {
                // Normal mode: Set long click listener (only for non-deleted, non-action/call messages)
                // ACTION and CALL category messages should not have long-click interactions
                val isActionOrCallMessage = message.category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true) ||
                                            message.category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)

                if (isActionOrCallMessage) {
                    // Disable long-click for action/call messages
                    rowRoot.setOnLongClickListener(null)
                } else {
                    rowRoot.setOnLongClickListener {
                        if (message.deletedAt == 0L) {
                            onMessageLongClick?.invoke(emptyList(), message, factory, messageBubble)
                            true
                        } else {
                            false
                        }
                    }
                }
                
                // Set thread reply click listener (normal mode only)
                // hideThreadView takes precedence - used by ThreadHeader to hide thread indicators
                if (hideThreadView) {
                    messageBubble.setThreadViewVisibility(View.GONE)
                    messageBubble.setOnThreadViewClickListener(null)
                } else if (message.replyCount > 0 && message.deletedAt == 0L) {
                    messageBubble.setThreadViewVisibility(View.VISIBLE)
                    messageBubble.setOnThreadViewClickListener {
                        threadReplyClick?.onThreadReplyClick(context, message, factory)
                    }
                } else {
                    messageBubble.setThreadViewVisibility(View.GONE)
                    messageBubble.setOnThreadViewClickListener(null)
                }
                // Note: setOnMessagePreviewClickListener is now called BEFORE bindViews()
                // to ensure the listener is available when InternalContentRenderer.bindReplyView() is called
            }
        }
    }

    /**
     * ViewHolder for stream messages (AI assistant responses).
     * Uses a dedicated stream bubble layout.
     */
    inner class StreamBubbleViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        // TODO: Bind to actual stream bubble view when layout is available
        // private val binding = CometchatStreamBubbleBinding.bind(itemView)

        /**
         * Binds the stream message data to the ViewHolder.
         *
         * @param streamMessage The stream message to bind
         * @param position Position in the list
         */
        fun bind(streamMessage: BaseMessage, position: Int) {
            // Stream message binding will be implemented when stream bubble layout is available
            // For now, this is a placeholder that handles visibility based on deletion status
            if (streamMessage.deletedAt == 0L) {
                itemView.visibility = View.VISIBLE
                // TODO: Bind stream message data when layout is available
                // binding.streamBubble.setStyle(aiAssistantBubbleStyle)
                // binding.streamBubble.setStreamMessage(streamMessage)
                // binding.streamBubble.setBackgroundColor(aiAssistantBubbleBackgroundColor)
                // binding.streamBubble.setAvatar(streamMessage.sender.name, streamMessage.sender.avatar)
                // binding.streamBubble.setAvatarStyle(aiAssistantBubbleAvatarStyle)
            } else {
                itemView.visibility = View.GONE
            }
        }
    }

    /**
     * ViewHolder for ignored messages (hidden group actions).
     * This is an empty ViewHolder that takes no space.
     */
    class EmptyRowHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // ========================================
    // Row Layout Creation (Placeholder - Task 3)
    // ========================================

    /**
     * Creates the row layout with appropriate alignment based on view type suffix.
     *
     * @param context The Android context
     * @param viewType The view type ID
     * @return Pair of row root LinearLayout and CometChatMessageBubble
     */
    private fun createMessageRow(context: Context, viewType: Int): Pair<LinearLayout, CometChatMessageBubble> {
        val viewTypeString = viewType.toString()

        return when {
            viewTypeString.endsWith(RIGHT_MESSAGE) -> createRightAlignedRow(context)
            viewTypeString.endsWith(CENTER_MESSAGE) -> createCenterAlignedRow(context)
            else -> createLeftAlignedRow(context) // LEFT_MESSAGE or fallback
        }
    }

    /**
     * Creates a left-aligned row layout for incoming messages.
     *
     * The row has:
     * - Gravity.START for left alignment
     * - Padding: 4dp start, 8dp vertical, 16dp end (matching Java reference)
     * - Message bubble with MATCH_PARENT width
     * - Configurable left bubble margins applied
     *
     * @param context The Android context
     * @return Pair of row root LinearLayout and CometChatMessageBubble
     */
    private fun createLeftAlignedRow(context: Context): Pair<LinearLayout, CometChatMessageBubble> {
        val padding4 = context.resources.getDimensionPixelSize(R.dimen.cometchat_4dp)
        val padding8 = context.resources.getDimensionPixelSize(R.dimen.cometchat_8dp)
        val padding16 = context.resources.getDimensionPixelSize(R.dimen.cometchat_16dp)

        val rowParent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.START
            setPadding(padding4, padding8, padding16, padding8)  // Match Java: 4dp, 8dp, 16dp, 8dp
        }

        val messageBubble = CometChatMessageBubble(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Apply left bubble margins
        applyBubbleMargins(messageBubble, leftBubbleMargins)

        rowParent.addView(messageBubble)
        return Pair(rowParent, messageBubble)
    }

    /**
     * Creates a right-aligned row layout for outgoing messages.
     *
     * The row has:
     * - Gravity.END for right alignment
     * - Padding: 4dp start, 8dp vertical, 4dp end (matching Java reference)
     * - Message bubble with MATCH_PARENT width and 50dp marginStart
     * - Configurable right bubble margins applied
     *
     * @param context The Android context
     * @return Pair of row root LinearLayout and CometChatMessageBubble
     */
    private fun createRightAlignedRow(context: Context): Pair<LinearLayout, CometChatMessageBubble> {
        val padding4 = context.resources.getDimensionPixelSize(R.dimen.cometchat_4dp)
        val padding8 = context.resources.getDimensionPixelSize(R.dimen.cometchat_8dp)
        val margin50 = context.resources.getDimensionPixelSize(R.dimen.cometchat_50dp)

        val rowParent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.END
            setPadding(padding4, padding8, padding4, padding8)  // Match Java: 4dp, 8dp, 4dp, 8dp
        }

        val messageBubble = CometChatMessageBubble(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = margin50
            }
        }

        // Apply right bubble margins
        applyBubbleMargins(messageBubble, rightBubbleMargins)

        rowParent.addView(messageBubble)
        return Pair(rowParent, messageBubble)
    }

    /**
     * Creates a center-aligned row layout for action/call messages.
     *
     * The row has:
     * - Gravity.CENTER for center alignment
     * - Padding: 0 horizontal, 8dp vertical (matching Java reference)
     * - Message bubble with WRAP_CONTENT width and 16dp horizontal margins
     *
     * @param context The Android context
     * @return Pair of row root LinearLayout and CometChatMessageBubble
     */
    private fun createCenterAlignedRow(context: Context): Pair<LinearLayout, CometChatMessageBubble> {
        val margin16 = context.resources.getDimensionPixelSize(R.dimen.cometchat_16dp)

        val rowParent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            // No vertical padding for center messages (matching Java reference)
        }

        val messageBubble = CometChatMessageBubble(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = margin16
                marginEnd = margin16
            }
        }

        rowParent.addView(messageBubble)
        return Pair(rowParent, messageBubble)
    }

    /**
     * Creates a row layout with a pre-initialized message bubble.
     *
     * This method creates a message bubble using the new constructor that accepts
     * alignment and factoryKey parameters, enabling layout inflation and view creation
     * to happen immediately during ViewHolder creation rather than during binding.
     *
     * The row layout is created based on alignment:
     * - LEFT: Gravity.START, standard padding, MATCH_PARENT width
     * - RIGHT: Gravity.END, standard padding, MATCH_PARENT width with 50dp marginStart
     * - CENTER: Gravity.CENTER, no padding, WRAP_CONTENT width with 16dp horizontal margins
     *
     * @param context The Android context
     * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
     * @param factoryKey The factory key (e.g., "message_text", "custom_extension_poll")
     * @param factory The BubbleFactory for this message type (may be null)
     * @return Pair of row root LinearLayout and pre-initialized CometChatMessageBubble
     */
    private fun createMessageRowWithPreInitializedBubble(
        context: Context,
        alignment: UIKitConstants.MessageBubbleAlignment,
        factoryKey: String,
        factory: BubbleFactory?
    ): Pair<LinearLayout, CometChatMessageBubble> {
        val padding4 = context.resources.getDimensionPixelSize(R.dimen.cometchat_4dp)
        val padding8 = context.resources.getDimensionPixelSize(R.dimen.cometchat_8dp)
        val padding16 = context.resources.getDimensionPixelSize(R.dimen.cometchat_16dp)
        val margin50 = context.resources.getDimensionPixelSize(R.dimen.cometchat_50dp)
        val margin16 = context.resources.getDimensionPixelSize(R.dimen.cometchat_16dp)

        // Create row layout based on alignment using RecyclerView.LayoutParams
        val rowParent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            when (alignment) {
                UIKitConstants.MessageBubbleAlignment.LEFT -> {
                    gravity = Gravity.START
                    setPadding(padding4, padding8, padding16, padding8)  // Match Java: 4dp, 8dp, 16dp, 8dp
                }
                UIKitConstants.MessageBubbleAlignment.RIGHT -> {
                    gravity = Gravity.END
                    setPadding(padding4, padding8, padding4, padding8)   // Match Java: 4dp, 8dp, 4dp, 8dp
                }
                UIKitConstants.MessageBubbleAlignment.CENTER -> {
                    gravity = Gravity.CENTER
                    // No vertical padding for center messages (matching Java reference)
                }
            }
        }

        // Create bubble using default constructor, then set properties BEFORE createViews
        val messageBubble = CometChatMessageBubble(context).apply {
            layoutParams = when (alignment) {
                UIKitConstants.MessageBubbleAlignment.LEFT -> {
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                UIKitConstants.MessageBubbleAlignment.RIGHT -> {
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = margin50
                    }
                }
                UIKitConstants.MessageBubbleAlignment.CENTER -> {
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = margin16
                        marginEnd = margin16
                    }
                }
            }
            // Set factory FIRST (before createViews) - this is the key fix!
            setBubbleFactory(factory)
            // Set alignment (inflates layout)
            setMessageAlignment(alignment)
            // NOW create views - factory is available
            createViews(factoryKey)
        }

        // Apply configurable margins based on alignment
        when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT -> applyBubbleMargins(messageBubble, leftBubbleMargins)
            UIKitConstants.MessageBubbleAlignment.RIGHT -> applyBubbleMargins(messageBubble, rightBubbleMargins)
            UIKitConstants.MessageBubbleAlignment.CENTER -> { /* No configurable margins for CENTER */ }
        }

        rowParent.addView(messageBubble)
        return Pair(rowParent, messageBubble)
    }

    /**
     * Applies configurable margins to a message bubble.
     *
     * Only margins with values != -1 are applied. This allows selective
     * margin configuration where -1 means "keep default".
     *
     * @param messageBubble The bubble to apply margins to
     * @param margins The BubbleMargins configuration
     */
    private fun applyBubbleMargins(messageBubble: CometChatMessageBubble, margins: BubbleMargins) {
        val layoutParams = messageBubble.layoutParams as? LinearLayout.LayoutParams ?: return
        if (margins.top != -1) layoutParams.topMargin = margins.top
        if (margins.bottom != -1) layoutParams.bottomMargin = margins.bottom
        if (margins.start != -1) layoutParams.marginStart = margins.start
        if (margins.end != -1) layoutParams.marginEnd = margins.end
        messageBubble.layoutParams = layoutParams
    }

    /**
     * Creates an empty view for ignored messages.
     */
    private fun createEmptyView(context: Context): View {
        return View(context).apply {
            layoutParams = ViewGroup.LayoutParams(0, 0)
            visibility = View.GONE
        }
    }

    /**
     * Creates a StreamBubbleViewHolder for stream messages.
     * TODO: Implement proper stream bubble layout in Task 3
     */
    private fun createStreamViewHolder(parent: ViewGroup): StreamBubbleViewHolder {
        // For now, create a placeholder view until stream bubble layout is available
        val view = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return StreamBubbleViewHolder(view)
    }

    /**
     * Calculates the highlight background color based on extended primary color 800 and alpha.
     * This matches the Java chatuikit implementation which uses getExtendedPrimaryColor800.
     */
    private fun getHighlightColor(): Int {
        val baseColor = CometChatTheme.getExtendedPrimaryColor800(context)
        return ColorUtils.setAlphaComponent(baseColor, (highlightAlpha * 255).toInt())
    }

    // ========================================
    // Highlight Methods
    // ========================================

    /**
     * Sets the highlighted message by its ID. This method allows highlighting a
     * specific message in the message list. When a message is highlighted, it is
     * visually distinguished from other messages to draw attention to it.
     * 
     * This method sets the highlight state and applies the initial highlight color
     * directly to the ViewHolder's background without calling notifyItemChanged(),
     * which would interfere with the subsequent fade-out animation.
     *
     * @param messageId The id of the message to be highlighted.
     * @param position The position of the message in the list.
     * @param recyclerView The RecyclerView to find the ViewHolder from.
     */
    fun setHighlightedMessage(messageId: Long, position: Int, recyclerView: RecyclerView? = null) {
        this.highlightedMessageId = messageId
        // Set initial alpha to 1.0 for full highlight - animation will fade this out
        this.highlightAlpha = 1.0f
        // Apply highlight directly to ViewHolder without notifyItemChanged to avoid rebind
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
        viewHolder?.itemView?.setBackgroundColor(getHighlightColor())
    }

    /**
     * Updates the highlight alpha for the message at the specified position. This
     * method adjusts the transparency of the highlight effect applied to a message.
     * A higher alpha value results in a more opaque highlight, while a lower value
     * makes the highlight more transparent.
     *
     * @param alpha The alpha value for the highlight (0.0 to 1.0).
     * @param position The position of the message in the list.
     * @param recyclerView Optional RecyclerView (unused, kept for API compatibility)
     */
    fun updateHighlightAlpha(alpha: Float, position: Int, recyclerView: RecyclerView? = null) {
        this.highlightAlpha = alpha
        // Note: This method is kept for API compatibility but the animation
        // now updates the view background directly in CometChatMessageList
    }

    /**
     * Clears the highlight from the message at the specified position. This method
     * removes any highlighting applied to a message, returning it to its normal
     * appearance.
     *
     * @param position The position of the message in the list.
     */
    fun clearHighlight(position: Int) {
        this.highlightedMessageId = -1L
        this.highlightAlpha = 0f
        notifyItemChanged(position)
    }

    // ========================================
    // StickyHeaderAdapter Implementation (Task 6)
    // ========================================

    /**
     * ViewHolder for date separator headers.
     * Contains a CometChatDate view for displaying the message date.
     */
    class DateItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * The CometChatDate view for displaying the date.
         */
        val txtMessageDate: CometChatDate = itemView.findViewById(R.id.txt_message_date)
    }

    /**
     * Returns a unique header ID based on the message's sent date.
     * Messages with the same date will have the same header ID.
     *
     * @param position The adapter position
     * @return The header ID (date in YYYYMMDD format), or NO_HEADER_ID if invalid
     */
    override fun getHeaderId(position: Int): Long {
        if (messages.size <= position) return StickyHeaderAdapter.NO_HEADER_ID
        val message = messages[position]
        // Convert sentAt (seconds) to milliseconds for getDateId
        return Utils.getDateId(message.sentAt * 1000)
    }

    /**
     * Creates a new ViewHolder for the date header.
     * Inflates the date header layout containing a CometChatDate view.
     *
     * @param parent The parent ViewGroup
     * @return A new DateItemHolder
     */
    override fun onCreateHeaderViewHolder(parent: ViewGroup): DateItemHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_message_date_header, parent, false)
        return DateItemHolder(view)
    }

    /**
     * Binds the message date to the header ViewHolder.
     * Applies date formatting, custom formatter callback, and styling.
     *
     * @param holder The DateItemHolder to bind
     * @param position The adapter position of the first item in this header group
     * @param headerId The header ID (date in YYYYMMDD format)
     */
    override fun onBindHeaderViewHolder(holder: DateItemHolder, position: Int, headerId: Long) {
        if (messages.size <= position) return
        val message = messages[position]

        // Apply internal padding to the date view (8dp horizontal, 4dp vertical)
        holder.txtMessageDate.setPadding(
            context.resources.getDimensionPixelSize(R.dimen.cometchat_8dp),
            context.resources.getDimensionPixelSize(R.dimen.cometchat_4dp),
            context.resources.getDimensionPixelSize(R.dimen.cometchat_8dp),
            context.resources.getDimensionPixelSize(R.dimen.cometchat_4dp)
        )
        
        // Apply external top margin to the header for spacing from messages above
        val topMargin = context.resources.getDimensionPixelSize(R.dimen.cometchat_8dp)
        (holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = topMargin

        if (message.sentAt > 0) {
            // Apply custom date format if set
            dateSeparatorFormat?.let { holder.txtMessageDate.setDateFormat(it) }
            
            // Apply custom date/time formatter callback if set
            dateTimeFormatter?.let { holder.txtMessageDate.setDateTimeFormatterCallback(it) }
            
            // Set the date using DAY_DATE pattern (shows Today, Yesterday, or full date)
            holder.txtMessageDate.setDate(message.sentAt, DatePattern.DAY_DATE)
        } else {
            // Show "Updating" text for messages without a valid timestamp
            holder.txtMessageDate.setDateText(context.getString(R.string.cometchat_updating))
        }

        // Apply date separator style if set
        dateSeparatorStyleObject?.let { style ->
            holder.txtMessageDate.setStyle(style)
        }
    }

    // ========================================
    // NewMessageIndicatorDecorationAdapter Implementation (Task 7)
    // ========================================

    /**
     * ViewHolder for new message indicator.
     * Contains the view that displays the "New" indicator between read and unread messages.
     */
    class NewMessageIndicatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * Returns the BaseMessage at the given position for indicator positioning.
     * The decoration uses this to determine where to draw the new message indicator.
     *
     * @param position The adapter position
     * @return The BaseMessage at the position, or null if out of bounds
     */
    override fun getNewMessageIndicatorId(position: Int): BaseMessage? {
        return messages.getOrNull(position)
    }

    /**
     * Creates a new ViewHolder for the new message indicator.
     * Uses customUnreadHeaderView if set, otherwise inflates the default layout.
     *
     * @param parent The parent ViewGroup
     * @return A new NewMessageIndicatorViewHolder
     */
    override fun onCreateNewMessageViewHolder(parent: ViewGroup): NewMessageIndicatorViewHolder {
        val view = customUnreadHeaderView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_new_message_indicator, parent, false)
        return NewMessageIndicatorViewHolder(view)
    }

    /**
     * Binds the new message indicator to the ViewHolder.
     * The indicator shows where unread messages begin in the conversation.
     *
     * @param holder The NewMessageIndicatorViewHolder to bind
     * @param position The adapter position
     * @param messageId The ID of the message at this position
     */
    override fun onBindNewMessageViewHolder(holder: NewMessageIndicatorViewHolder, position: Int, messageId: Long) {
        // The default layout is self-contained and doesn't need additional binding.
        // Custom views can be styled externally via customUnreadHeaderView.
    }

    // ========================================
    // BubbleFactory Management Methods
    // ========================================

    /**
     * Sets the bubble factories map, replacing any existing factories.
     *
     * @param factories Map of factory key (category_type) to BubbleFactory
     */
    fun setBubbleFactories(factories: Map<String, BubbleFactory>) {
        bubbleFactories = factories.toMutableMap()
        // Clear view type mappings to force recalculation
        factoryViewTypeHashMap.clear()
        viewTypeFactoryHashMap.clear()
        viewTypeToFactoryKeyMap.clear()
        notifyDataSetChanged()
    }

    /**
     * Removes a bubble factory for the given category and type.
     *
     * @param category The message category
     * @param type The message type
     */
    fun removeBubbleFactory(category: String, type: String) {
        val key = BubbleFactory.getKey(category, type)
        bubbleFactories.remove(key)
        notifyDataSetChanged()
    }

    /**
     * Gets the current bubble factories map.
     *
     * @return The bubble factories map (read-only copy)
     */
    fun getBubbleFactories(): Map<String, BubbleFactory> = bubbleFactories.toMap()

    /**
     * Gets the factory for a specific view type.
     * Used internally by ViewHolder creation.
     *
     * @param viewType The view type ID
     * @return The BubbleFactory or null if not registered
     */
    internal fun getFactoryForViewType(viewType: Int): BubbleFactory? {
        return viewTypeFactoryHashMap[viewType]
    }

    // ========================================
    // List Operations (Task 8)
    // ========================================

    /**
     * DiffUtil callback for efficient list updates.
     *
     * Compares messages by:
     * - areItemsTheSame: Compares message IDs
     * - areContentsTheSame: Uses object equality like ConversationsDiffCallback
     *
     * This enables efficient RecyclerView updates by only notifying changes
     * for items that have actually changed.
     */
    private class MessageDiffCallback(
        private val oldList: List<BaseMessage>,
        private val newList: List<BaseMessage>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        /**
         * Checks if two items represent the same message.
         * Uses message ID for identity comparison.
         */
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }

        /**
         * Checks if the contents of two items are the same.
         * Compares mutable fields that can change after initial load:
         * replyCount, editedAt, deletedAt, readAt, deliveredAt, reactions, and metadata.
         * Reference equality alone is insufficient because the SDK mutates objects in-place.
         */
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            val old = oldList[oldPos]
            val new = newList[newPos]
            // When the same object reference appears in both lists, we cannot
            // trust that its mutable fields haven't changed (the SDK mutates
            // BaseMessage in-place). Always return false to force a rebind.
            if (old === new) {
                android.util.Log.d("ThreadReplyDebug", "areContentsTheSame: SAME REF for id=${old.id}, forcing rebind")
                return false
            }
            return old == new
        }
    }

    /**
     * Replaces the entire message list using DiffUtil for efficient updates.
     *
     * DiffUtil calculates the minimal set of changes needed to transform
     * the old list into the new list, then dispatches those changes to
     * the adapter (insertions, removals, moves, updates).
     *
     * @param newMessages The new list of messages
     */
    fun setMessageList(newMessages: List<BaseMessage>) {
        android.util.Log.d("ThreadReplyDebug", "setMessageList: oldSize=${messages.size}, newSize=${newMessages.size}")
        val diffCallback = MessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messages = newMessages.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Adds a message at a specific position or at the end of the list.
     *
     * Uses DiffUtil for efficient updates by creating a new list with
     * the message inserted at the specified position.
     *
     * @param message The message to add
     * @param position The position to insert at (defaults to end of list)
     */
    fun addMessage(message: BaseMessage, position: Int = messages.size) {
        val newList = messages.toMutableList().apply {
            add(position.coerceIn(0, size), message)
        }
        setMessageList(newList)
    }

    /**
     * Updates an existing message by ID.
     *
     * Finds the message with the same ID and replaces it with the new message.
     * Uses DiffUtil for efficient updates.
     *
     * @param message The updated message (must have the same ID as the existing message)
     */
    fun updateMessage(message: BaseMessage) {
        val newList = messages.map { if (it.id == message.id) message else it }
        setMessageList(newList)
    }

    /**
     * Removes a message by ID.
     *
     * Filters out the message with the specified ID.
     * Uses DiffUtil for efficient updates.
     *
     * @param messageId The ID of the message to remove
     */
    fun removeMessage(messageId: Long) {
        val newList = messages.filter { it.id != messageId }
        setMessageList(newList)
    }

    /**
     * Removes a message at a specific position.
     *
     * @param position The position of the message to remove
     */
    fun removeMessageAt(position: Int) {
        if (position in messages.indices) {
            val newList = messages.toMutableList().apply {
                removeAt(position)
            }
            setMessageList(newList)
        }
    }

    /**
     * Clears all messages from the list.
     *
     * Uses DiffUtil for efficient updates by setting an empty list.
     */
    fun clearMessages() {
        setMessageList(emptyList())
    }

    /**
     * Gets the current list of messages.
     *
     * @return The current messages list (defensive copy)
     */
    fun getMessages(): List<BaseMessage> = messages.toList()

    /**
     * Gets a message at a specific position.
     *
     * @param position The position
     * @return The message at the position, or null if out of bounds
     */
    fun getMessage(position: Int): BaseMessage? = messages.getOrNull(position)

    /**
     * Finds the position of a message by ID.
     *
     * @param messageId The message ID to find
     * @return The position, or -1 if not found
     */
    fun findMessagePosition(messageId: Long): Int {
        return messages.indexOfFirst { it.id == messageId }
    }

    // ========================================
    // Helper Methods for View Type Checking
    // ========================================

    /**
     * Checks if a view type represents a LEFT-aligned message.
     *
     * @param viewType The view type to check
     * @return true if LEFT-aligned
     */
    internal fun isLeftViewType(viewType: Int): Boolean {
        return viewType.toString().endsWith(LEFT_MESSAGE)
    }

    /**
     * Checks if a view type represents a RIGHT-aligned message.
     *
     * @param viewType The view type to check
     * @return true if RIGHT-aligned
     */
    internal fun isRightViewType(viewType: Int): Boolean {
        return viewType.toString().endsWith(RIGHT_MESSAGE)
    }

    /**
     * Checks if a view type represents a CENTER-aligned message.
     *
     * @param viewType The view type to check
     * @return true if CENTER-aligned
     */
    internal fun isCenterViewType(viewType: Int): Boolean {
        return viewType.toString().endsWith(CENTER_MESSAGE)
    }

    /**
     * Checks if a view type represents a STREAM message.
     *
     * @param viewType The view type to check
     * @return true if STREAM message
     */
    internal fun isStreamViewType(viewType: Int): Boolean {
        return viewType.toString() == STREAM_MESSAGE
    }

    /**
     * Checks if a view type represents an IGNORE message.
     *
     * @param viewType The view type to check
     * @return true if IGNORE message
     */
    internal fun isIgnoreViewType(viewType: Int): Boolean {
        return viewType.toString() == IGNORE_MESSAGE || viewType.toString().endsWith(IGNORE_MESSAGE)
    }

    // ========================================
    // Backward Compatibility Methods
    // ========================================

    /**
     * Alias for setMessageList for backward compatibility.
     * @param messages The list of messages to display
     */
    fun setMessages(messages: List<BaseMessage>) {
        setMessageList(messages)
    }

    /**
     * Callback for item click events (backward compatibility).
     */
    private var onItemClickListener: ((BaseMessage, Int) -> Unit)? = null

    /**
     * Callback for item long click events (backward compatibility).
     */
    private var onItemLongClickListener: ((BaseMessage, Int) -> Boolean)? = null

    /**
     * Sets the callback for item click events (backward compatibility).
     * @param listener The callback to invoke when an item is clicked
     */
    fun setOnItemClickListener(listener: ((BaseMessage, Int) -> Unit)?) {
        onItemClickListener = listener
    }

    /**
     * Sets the callback for item long click events (backward compatibility).
     * @param listener The callback to invoke when an item is long-clicked
     */
    fun setOnItemLongClickListener(listener: ((BaseMessage, Int) -> Boolean)?) {
        onItemLongClickListener = listener
    }

    /**
     * Sets margins for all message bubbles (backward compatibility).
     * @param top Top margin in dp
     * @param bottom Bottom margin in dp
     * @param left Left/start margin in dp
     * @param right Right/end margin in dp
     */
    fun setBubbleMargin(top: Int, bottom: Int, left: Int, right: Int) {
        leftBubbleMargins = BubbleMargins(top, bottom, left, right)
        rightBubbleMargins = BubbleMargins(top, bottom, left, right)
    }

    /**
     * Sets margins for left-aligned (incoming) message bubbles (backward compatibility).
     * @param top Top margin in dp
     * @param bottom Bottom margin in dp
     * @param left Left/start margin in dp
     * @param right Right/end margin in dp
     */
    fun setLeftBubbleMargin(top: Int, bottom: Int, left: Int, right: Int) {
        leftBubbleMargins = BubbleMargins(top, bottom, left, right)
    }

    /**
     * Sets margins for right-aligned (outgoing) message bubbles (backward compatibility).
     * @param top Top margin in dp
     * @param bottom Bottom margin in dp
     * @param left Left/start margin in dp
     * @param right Right/end margin in dp
     */
    fun setRightBubbleMargin(top: Int, bottom: Int, left: Int, right: Int) {
        rightBubbleMargins = BubbleMargins(top, bottom, left, right)
    }

    // ========================================
    // BubbleViewProvider Methods (Backward Compatibility)
    // ========================================

    // View providers are stored but not used in the new architecture
    // They are kept for backward compatibility with CometChatMessageList
    private var leadingViewProvider: Any? = null
    private var headerViewProvider: Any? = null
    private var replyViewProvider: Any? = null
    private var contentViewProvider: Any? = null
    private var bottomViewProvider: Any? = null
    private var statusInfoViewProvider: Any? = null
    private var threadViewProvider: Any? = null
    private var footerViewProvider: Any? = null

    fun setLeadingViewProvider(provider: Any?) { leadingViewProvider = provider }
    fun setHeaderViewProvider(provider: Any?) { headerViewProvider = provider }
    fun setReplyViewProvider(provider: Any?) { replyViewProvider = provider }
    fun setContentViewProvider(provider: Any?) { contentViewProvider = provider }
    fun setBottomViewProvider(provider: Any?) { bottomViewProvider = provider }
    fun setStatusInfoViewProvider(provider: Any?) { statusInfoViewProvider = provider }
    fun setThreadViewProvider(provider: Any?) { threadViewProvider = provider }
    fun setFooterViewProvider(provider: Any?) { footerViewProvider = provider }
}
