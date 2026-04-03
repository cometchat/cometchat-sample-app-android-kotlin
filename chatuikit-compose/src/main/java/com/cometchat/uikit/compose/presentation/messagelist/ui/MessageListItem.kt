package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.messagelist.style.CometChatMessageListStyle
import com.cometchat.uikit.compose.presentation.messagelist.utils.getMessageAlignment
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.BubbleStyles
import com.cometchat.uikit.compose.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatMessageBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.buildFactoryKey
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.state.MessageAlignment

/**
 * Determines if avatar should be shown for a given message.
 *
 * This function implements the avatar visibility logic that matches the XML chatuikit behavior:
 *
 * ## Rules:
 * 1. If [hideAvatar] is true, always return false (master override)
 * 2. If [alignment] is RIGHT (outgoing message), always return false
 * 3. If [alignment] is CENTER (action/system message), always return false
 * 4. If [alignment] is LEFT (incoming message):
 *    - For group conversations: return true (show avatar)
 *    - For user conversations: return false (hide avatar)
 *
 * ## Decision Table:
 * | Alignment | hideAvatar | isGroupConversation | Show Avatar? |
 * |-----------|------------|---------------------|--------------|
 * | RIGHT     | false      | true                | NO           |
 * | RIGHT     | false      | false               | NO           |
 * | RIGHT     | true       | true                | NO           |
 * | RIGHT     | true       | false               | NO           |
 * | CENTER    | false      | true                | NO           |
 * | CENTER    | false      | false               | NO           |
 * | CENTER    | true       | true                | NO           |
 * | CENTER    | true       | false               | NO           |
 * | LEFT      | false      | true                | YES          |
 * | LEFT      | false      | false               | NO           |
 * | LEFT      | true       | true                | NO           |
 * | LEFT      | true       | false               | NO           |
 *
 * @param alignment The message bubble alignment (LEFT for incoming, RIGHT for outgoing, CENTER for action)
 * @param hideAvatar The master hide avatar flag from CometChatMessageList
 * @param isGroupConversation Whether the current conversation is a group (true) or user (false)
 * @return true if avatar should be shown, false otherwise
 *
 * @see UIKitConstants.MessageBubbleAlignment
 */
internal fun shouldShowAvatar(
    alignment: UIKitConstants.MessageBubbleAlignment,
    hideAvatar: Boolean,
    isGroupConversation: Boolean
): Boolean {
    // Rule 1: Master override - if hideAvatar is true, never show avatar
    if (hideAvatar) return false

    // Rules 2, 3, 4: Alignment-based visibility
    return when (alignment) {
        // Rule 2: Outgoing messages never show avatar
        UIKitConstants.MessageBubbleAlignment.RIGHT -> false
        // Rule 3: Action/system messages never show avatar
        UIKitConstants.MessageBubbleAlignment.CENTER -> false
        // Rule 4: Incoming messages show avatar only in group conversations
        UIKitConstants.MessageBubbleAlignment.LEFT -> isGroupConversation
    }
}

/**
 * A composable that renders a single message item in the message list.
 *
 * This composable:
 * - Determines message alignment (LEFT, RIGHT, CENTER) based on sender
 * - Uses the base [messageBubbleStyle][CometChatMessageListStyle.messageBubbleStyle] for outer bubble styling
 * - Constructs bubble factories with per-bubble-type styles from [CometChatMessageListStyle]
 * - Delegates slot rendering to the [BubbleFactory] system
 * - Allows per-slot overrides via provider callbacks
 *
 * ## Avatar Visibility
 *
 * Avatar visibility is controlled by the combination of [hideAvatar] and [isGroupConversation]:
 * - Outgoing messages (RIGHT alignment) never show avatars
 * - Action messages (CENTER alignment) never show avatars
 * - Incoming messages (LEFT alignment) show avatars only in group conversations
 * - When [hideAvatar] is true, avatars are hidden regardless of other settings
 *
 * The [shouldShowAvatar] function computes the final visibility based on these rules.
 *
 * ## Style Propagation
 *
 * The [CometChatMessageListStyle] contains:
 * - [messageBubbleStyle][CometChatMessageListStyle.messageBubbleStyle]: Base style for the outer bubble container
 * - Per-bubble-type styles (textBubbleStyle, imageBubbleStyle, etc.): Nullable styles that
 *   are passed to [CometChatMessageBubble] for internal rendering
 *
 * Per-bubble-type styles are passed directly to [CometChatMessageBubble] which uses them
 * during internal rendering. When a style is non-null, it overrides the alignment-based default.
 * When null, alignment-based defaults (incoming/outgoing) are used.
 *
 * ## Slot Resolution Order
 *
 * For each slot, the resolution order is:
 * 1. Explicit provider callback (if set) — receives message + alignment
 * 2. Factory slot method (from [BubbleFactory]) — provides default composable
 * 3. Nothing (if factory returns null for that slot)
 *
 * ```kotlin
 * MessageListItem(
 *     message = message,
 *     leadingView = { msg, alignment ->
 *         // This overrides the factory's getLeadingView
 *         if (alignment == MessageAlignment.LEFT) {
 *             CometChatAvatar(user = msg.sender)
 *         }
 *     }
 * )
 * ```
 *
 * @param message The message to display
 * @param loggedInUser The currently logged-in user for alignment determination
 * @param style The message list style containing messageBubbleStyle and per-bubble-type styles
 * @param bubbleFactories Map of factory key to [BubbleFactory] for custom content rendering.
 *   When null or empty, internal rendering is used for all standard message types.
 *   Custom factories can be provided for custom message types or to override default rendering.
 * @param hideAvatar Whether to hide the avatar in the leading view (master override)
 * @param isGroupConversation Whether the current conversation is a group conversation.
 *   When true, incoming messages will show avatars by default.
 *   When false (user conversation), incoming messages will hide avatars by default.
 * @param hideReceipts Whether to hide read/delivery receipts
 * @param hideGroupActionMessages Whether to skip rendering group action messages
 * @param hideModerationView Whether to hide the moderation indicator in the bottom view.
 *   When true, the moderation indicator is not rendered regardless of the message's
 *   moderation status. When false (default), the moderation indicator is shown for
 *   messages with "disapproved" moderation status.
 * @param timeStampAlignment Controls where the timestamp is displayed in message bubbles.
 *   When [UIKitConstants.TimeStampAlignment.TOP], the timestamp is shown in the header view.
 *   When [UIKitConstants.TimeStampAlignment.BOTTOM], the timestamp is shown in the status info view.
 * @param leadingView Optional provider to override factory's leading view
 * @param headerView Optional provider to override factory's header view
 * @param replyView Optional provider to override factory's reply view
 * @param contentView Optional provider to override factory's content view
 * @param bottomView Optional provider to override factory's bottom view
 * @param statusInfoView Optional provider to override factory's status info view
 * @param threadView Optional provider to override factory's thread view
 * @param footerView Optional provider to override factory's footer view
 * @param onMessageClick Callback when message is clicked
 * @param onMessageLongClick Callback when message is long-clicked
 * @param onThreadRepliesClick Callback when thread indicator is clicked
 * @param onReactionClick Callback when a reaction is clicked
 * @param onReactionLongClick Callback when a reaction is long-clicked
 * @param onAddMoreReactionsClick Callback when add reaction button is clicked
 * @param modifier Modifier for the root composable
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MessageListItem(
    message: BaseMessage,
    loggedInUser: User?,
    style: CometChatMessageListStyle,
    bubbleFactories: Map<String, BubbleFactory>? = null,
    hideAvatar: Boolean = false,
    isGroupConversation: Boolean = false,
    hideReceipts: Boolean = false,
    hideGroupActionMessages: Boolean = false,
    hideModerationView: Boolean = false,
    timeStampAlignment: UIKitConstants.TimeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM,
    leadingView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    headerView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    replyView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    contentView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    bottomView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    statusInfoView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    threadView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    footerView: (@Composable (BaseMessage, MessageAlignment) -> Unit)? = null,
    onMessageClick: ((BaseMessage) -> Unit)? = null,
    onMessageLongClick: ((BaseMessage) -> Unit)? = null,
    onThreadRepliesClick: ((BaseMessage) -> Unit)? = null,
    onReactionClick: ((BaseMessage, String) -> Unit)? = null,
    onReactionLongClick: ((BaseMessage, String) -> Unit)? = null,
    onAddMoreReactionsClick: ((BaseMessage) -> Unit)? = null,
    // NEW parameters for parity with chatuikit-kotlin (Task 11.1)
    hideReactions: Boolean = false,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    timeFormat: String? = null,
    dateTimeFormatter: ((Long) -> String)? = null,
    onMessagePreviewClick: ((BaseMessage) -> Unit)? = null,
    bubbleStyles: BubbleStyles = BubbleStyles(),
    incomingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    outgoingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    // NEW parameter for alignment propagation (Task 11.2)
    messageListAlignment: UIKitConstants.MessageListAlignment = UIKitConstants.MessageListAlignment.STANDARD,
    // Highlight parameters for jump-to-parent-message feature
    highlightedMessageId: Long = -1L,
    highlightAlpha: Float = 0f,
    modifier: Modifier = Modifier
) {
    // Determine message alignment
    val alignment = remember(message.id, message.sender?.uid, loggedInUser?.uid) {
        getMessageAlignment(message, loggedInUser)
    }

    // Skip rendering if hideGroupActionMessages is true and this is an action message
    if (hideGroupActionMessages && message.category == CometChatConstants.CATEGORY_ACTION) {
        return
    }

    // Filter the appropriate factory from the map based on message type
    // When bubbleFactories is null or empty, factory will be null and InternalContentRenderer is used
    val factory = remember(message.id, message.deletedAt, bubbleFactories) {
        bubbleFactories?.get(buildFactoryKey(message))
    }

    // Convert MessageAlignment to UIKitConstants.MessageBubbleAlignment
    // When messageListAlignment is LEFT_ALIGNED, override alignment to LEFT
    // regardless of sender (except for CENTER-aligned action/call messages)
    val bubbleAlignment = remember(alignment, messageListAlignment) {
        if (messageListAlignment == UIKitConstants.MessageListAlignment.LEFT_ALIGNED) {
            // LEFT_ALIGNED mode: force all non-action/call messages to LEFT
            when (alignment) {
                MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
                else -> UIKitConstants.MessageBubbleAlignment.LEFT
            }
        } else {
            // STANDARD mode: use normal alignment based on sender
            when (alignment) {
                MessageAlignment.LEFT -> UIKitConstants.MessageBubbleAlignment.LEFT
                MessageAlignment.RIGHT -> UIKitConstants.MessageBubbleAlignment.RIGHT
                MessageAlignment.CENTER -> UIKitConstants.MessageBubbleAlignment.CENTER
            }
        }
    }

    // Accessibility description
    val accessibilityDescription = remember(message.id, alignment) {
        val direction = when (alignment) {
            MessageAlignment.LEFT -> "Incoming"
            MessageAlignment.RIGHT -> "Outgoing"
            MessageAlignment.CENTER -> "System"
        }
        "$direction message from ${message.sender?.name ?: "Unknown"}"
    }

    // Determine if this is an action or call message that should not have long-click
    // ACTION and CALL category messages should not respond to long-press gestures
    // Bug fix for ENG-32209: Reply Option Available for Action/System Messages
    val isActionOrCallMessage = remember(message.id, message.category) {
        message.category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true) ||
        message.category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)
    }

    // Compute whether to show the default avatar based on alignment, hideAvatar, and conversation type.
    // This follows the avatar visibility rules:
    // - Outgoing messages (RIGHT) never show avatar
    // - Action messages (CENTER) never show avatar
    // - Incoming messages (LEFT) show avatar only in group conversations
    // - hideAvatar=true overrides all and hides avatar
    val shouldShowDefaultAvatar = remember(bubbleAlignment, hideAvatar, isGroupConversation) {
        val result = shouldShowAvatar(
            alignment = bubbleAlignment,
            hideAvatar = hideAvatar,
            isGroupConversation = isGroupConversation
        )
        // Debug logging - remove after testing
        android.util.Log.d("AvatarVisibility", "Message: ${message.id}, Alignment: $bubbleAlignment, hideAvatar: $hideAvatar, isGroupConversation: $isGroupConversation, shouldShowDefaultAvatar: $result")
        result
    }

    // Convert provider callbacks to direct composable lambdas for CometChatMessageBubble.
    // When a provider is set, wrap it. When null, pass null so the factory handles it.
    //
    // Style resolution: Pass messageBubbleStyle as the base style for the three-tier priority chain.
    // CometChatMessageBubble resolves the effective style as:
    //   Priority 3 (highest): Factory style from getBubbleStyle()
    //   Priority 2: Bubble-specific style merged with messageBubbleStyle
    //   Priority 1 (lowest): messageBubbleStyle
    //   Fallback: alignment-based defaults (incoming/outgoing)
    //
    // Per-bubble-type styles are passed from CometChatMessageListStyle to CometChatMessageBubble
    // for internal rendering. When a style is non-null, it overrides the alignment-based default.
    // When null, CometChatMessageBubble uses incoming()/outgoing()/default() based on alignment.
    CometChatMessageBubble(
        message = message,
        alignment = bubbleAlignment,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onMessageClick?.invoke(message) },
                onLongClick = if (isActionOrCallMessage) null else {
                    { onMessageLongClick?.invoke(message) }
                }
            )
            .semantics { contentDescription = accessibilityDescription },
        style = style.messageBubbleStyle, // Pass messageBubbleStyle as base
        factory = factory,
        shouldShowDefaultAvatar = shouldShowDefaultAvatar,
        timeStampAlignment = timeStampAlignment,
        hideModerationView = hideModerationView,

        // Pass per-bubble-type styles from CometChatMessageListStyle for internal rendering
        // These styles are used when no factory is provided for a message type
        // Requirements 5.1, 5.2, 5.3: Style propagation from MessageList to MessageBubble
        textBubbleStyle = style.textBubbleStyle,
        imageBubbleStyle = style.imageBubbleStyle,
        videoBubbleStyle = style.videoBubbleStyle,
        audioBubbleStyle = style.audioBubbleStyle,
        fileBubbleStyle = style.fileBubbleStyle,
        deleteBubbleStyle = style.deleteBubbleStyle,
        actionBubbleStyle = style.actionBubbleStyle,
        callActionBubbleStyle = style.callActionBubbleStyle,
        meetCallBubbleStyle = style.meetCallBubbleStyle,
        pollBubbleStyle = style.pollBubbleStyle,
        stickerBubbleStyle = style.stickerBubbleStyle,
        collaborativeBubbleStyle = style.collaborativeBubbleStyle,

        // Slot overrides: only set when user provides a custom provider
        leadingView = leadingView?.let { provider ->
            { provider(message, alignment) }
        },
        headerView = headerView?.let { provider ->
            { provider(message, alignment) }
        },
        replyView = replyView?.let { provider ->
            { provider(message, alignment) }
        },
        contentView = contentView?.let { provider ->
            { provider(message, alignment) }
        },
        bottomView = bottomView?.let { provider ->
            { provider(message, alignment) }
        },
        statusInfoView = statusInfoView?.let { provider ->
            { provider(message, alignment) }
        },
        threadView = threadView?.let { provider ->
            { provider(message, alignment) }
        },
        footerView = footerView?.let { provider ->
            { provider(message, alignment) }
        },

        // Pass through callbacks for factory slot methods
        onThreadRepliesClick = onThreadRepliesClick,
        onReactionClick = onReactionClick,
        onReactionLongClick = onReactionLongClick,
        onAddMoreReactionsClick = onAddMoreReactionsClick,

        // Pass through new parity parameters (Task 11.1)
        hideReactions = hideReactions,
        textFormatters = textFormatters,
        timeFormat = timeFormat,
        dateTimeFormatter = dateTimeFormatter,
        onMessagePreviewClick = onMessagePreviewClick,
        bubbleStyles = bubbleStyles,
        incomingMessageBubbleStyle = incomingMessageBubbleStyle,
        outgoingMessageBubbleStyle = outgoingMessageBubbleStyle,
        onLongClick = { onMessageLongClick?.invoke(message) },
        
        // Highlight parameters for jump-to-parent-message feature
        highlightedMessageId = highlightedMessageId,
        highlightAlpha = highlightAlpha
    )
}
