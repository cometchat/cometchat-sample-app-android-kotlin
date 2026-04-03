package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.mentions.MentionTextStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.BubbleStyles
import com.cometchat.uikit.compose.presentation.shared.messagebubble.BubbleFactory
import com.cometchat.uikit.compose.presentation.shared.messagebubble.InternalContentRenderer
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudioBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCallActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatDeleteBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFileBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatPollBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatStickerBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideoBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.mergeWithBase
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Special key used for deleted messages in factory lookup.
 */
private const val DELETED_KEY = "deleted"

/**
 * Derives the factory lookup key from a message.
 * Deleted messages (deletedAt > 0) return "deleted".
 * All others return "category_type".
 * 
 * This function is used by callers (MessageListItem, ThreadHeader, etc.) to look up
 * the appropriate factory from the factory map before passing it to CometChatMessageBubble.
 */
internal fun buildFactoryKey(message: BaseMessage): String {
    return if (message.deletedAt > 0) {
        DELETED_KEY
    } else {
        "${message.category}_${message.type}"
    }
}

/**
 * A smart message bubble composable that automatically renders content based on message type.
 *
 * This composable provides:
 * - Automatic content rendering based on message type via [bubbleFactories]
 * - Slot-based customization for all bubble areas
 * - Factory-driven slot views: each [BubbleFactory] provides default slot views
 *   that can be overridden per-slot via explicit parameters
 * - Factory-driven styling: each factory can provide a custom [CometChatMessageBubbleStyle]
 *   via [BubbleFactory.getBubbleStyle]
 * - Alignment support (LEFT, RIGHT, CENTER)
 * - Theming via [CometChatMessageBubbleStyle]
 *
 * ## Avatar Visibility and Leading View Resolution
 *
 * The [shouldShowDefaultAvatar] parameter controls whether the factory's default leading view
 * (typically an avatar) should be shown. This is computed by the parent component based on:
 * - Message alignment (outgoing messages never show avatar)
 * - Conversation type (user vs group conversations)
 * - The hideAvatar flag from CometChatMessageList
 *
 * ### Leading View Resolution Logic
 *
 * The leading view is resolved using the following priority order:
 *
 * 1. **Explicit leadingView provider**: If a custom [leadingView] composable is passed,
 *    it is always used regardless of [shouldShowDefaultAvatar] or factory configuration.
 *    This allows complete customization of the leading view slot.
 *
 * 2. **shouldShowDefaultAvatar check**: If no explicit [leadingView] is provided and
 *    [shouldShowDefaultAvatar] is `false`, the leading view slot will be empty (null).
 *    This is the mechanism used to hide avatars for outgoing messages or user conversations.
 *
 * 3. **Factory's getLeadingView**: If no explicit [leadingView] is provided and
 *    [shouldShowDefaultAvatar] is `true`, the factory's [ComposeBubbleFactory.getLeadingView]
 *    method is called to provide the default leading view (typically an avatar).
 *
 * This resolution order ensures that:
 * - Custom leading views always take precedence (Requirement 1.2)
 * - Avatar visibility can be controlled without affecting custom views (Requirement 1.1)
 * - Factory-provided leading views are used as the default fallback (Requirement 4.1)
 *
 * ## Style Resolution Hierarchy — Three-Tier Priority Chain
 *
 * The effective style is resolved in the following priority order:
 * 1. **Factory style** (highest): [BubbleFactory.getBubbleStyle] — when non-null, used directly
 * 2. **Explicit incoming/outgoing style**: [incomingMessageBubbleStyle] for LEFT-aligned,
 *    [outgoingMessageBubbleStyle] for RIGHT-aligned — when non-null, used instead of alignment default
 * 3. **Alignment-based default** (lowest): [CometChatMessageBubbleStyle.incoming],
 *    [CometChatMessageBubbleStyle.outgoing], or [CometChatMessageBubbleStyle.default]
 *
 * Per-bubble-type styles (e.g., [CometChatTextBubbleStyle]) are merged on top of the resolved
 * base style when available.
 *
 * This allows clients to customize bubble styling at three levels:
 * - Per-message-type via factory's [getBubbleStyle]
 * - Per-direction via [incomingMessageBubbleStyle] / [outgoingMessageBubbleStyle]
 * - Global defaults via alignment-based styles
 *
 * ## Bubble Structure (matching XML layout)
 *
 * The bubble structure follows the XML layout from `cometchat_message_bubble_left.xml`:
 * - Leading view (avatar) - outside the bubble
 * - Header view (sender name) - outside the bubble
 * - Bubble container (MaterialCardView equivalent) containing:
 *   - Reply view (quoted message)
 *   - Content view (message content)
 *   - Status info view (timestamp, receipts)
 *   - Bottom view (moderation)
 * - Footer view (reactions) - outside the bubble
 * - Thread view (reply count) - outside the bubble
 *
 * ## Factory-Based Rendering
 *
 * When no explicit slot composable is provided, the bubble queries the factory for each slot:
 * 1. The caller (MessageListItem, ThreadHeader, etc.) filters the appropriate factory from the map
 * 2. The single factory is passed to this composable via the [factory] parameter
 * 3. Calls factory slot methods (getLeadingView, getHeaderView, etc.)
 * 4. Falls back to InternalContentRenderer if no factory is provided (null)
 *
 * ## Slot Customization
 *
 * Explicit slot parameters override factory-provided slots:
 *
 * ```kotlin
 * CometChatMessageBubble(
 *     message = message,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     headerView = { Text("Custom Header") },  // Overrides factory's getHeaderView
 *     contentView = { MyCustomContent(message) }  // Overrides factory's ContentView
 * )
 * ```
 *
 * @param message The message to display
 * @param alignment The bubble alignment (LEFT, RIGHT, CENTER)
 * @param modifier Modifier for the root composable
 * @param style Optional visual style configuration. When null, uses factory's getBubbleStyle
 *   or alignment-based defaults
 * @param factory The [BubbleFactory] for this message type, or null to use internal rendering.
 *   The caller is responsible for filtering the appropriate factory from the factory map
 *   using [buildFactoryKey] before passing it to this composable.
 * @param shouldShowDefaultAvatar Controls whether the factory's default leading view (avatar)
 *   should be rendered. This parameter is typically computed by the parent component
 *   ([MessageListItem]) based on message alignment, conversation type, and the hideAvatar flag.
 *
 *   **Resolution behavior:**
 *   - When an explicit [leadingView] is provided, it takes precedence regardless of this flag
 *   - When `true` and no explicit [leadingView] is provided, the factory's leading view is used
 *   - When `false` and no explicit [leadingView] is provided, the leading view slot is empty
 *
 *   **Default value:** `true` (show avatar when factory provides one)
 *
 * @param timeStampAlignment Controls where the timestamp is displayed in message bubbles.
 *   This affects both the header view and the status info view:
 *
 *   - [UIKitConstants.TimeStampAlignment.TOP]: Timestamp is shown in the header view
 *     alongside the sender name, and hidden from the status info view. Useful for
 *     designs where time should appear at the top of the bubble.
 *
 *   - [UIKitConstants.TimeStampAlignment.BOTTOM]: Timestamp is shown in the status info
 *     view alongside the receipt indicator, and hidden from the header view. This is
 *     the standard chat pattern where time appears at the bottom of the bubble.
 *
 *   **Default value:** [UIKitConstants.TimeStampAlignment.BOTTOM]
 * @param hideModerationView When true, the moderation indicator in the bottom view is hidden
 *   regardless of the message's moderation status. When false (default), the moderation
 *   indicator is shown for messages with "disapproved" moderation status.
 * @param leadingView Optional composable to override factory's leading view
 * @param headerView Optional composable to override factory's header view
 * @param replyView Optional composable to override factory's reply view
 * @param contentView Optional composable to override factory's content view
 * @param bottomView Optional composable to override factory's bottom view
 * @param statusInfoView Optional composable to override factory's status info view
 * @param threadView Optional composable to override factory's thread view
 * @param footerView Optional composable to override factory's footer view
 * @param onThreadRepliesClick Callback when thread indicator is clicked (passed to factory)
 * @param onReactionClick Callback when a reaction is clicked (passed to factory)
 * @param onReactionLongClick Callback when a reaction is long-clicked (passed to factory)
 * @param onAddMoreReactionsClick Callback when add reaction button is clicked (passed to factory)
 * @param textBubbleStyle Optional style for text message bubbles (used by internal rendering)
 * @param imageBubbleStyle Optional style for image message bubbles (used by internal rendering)
 * @param videoBubbleStyle Optional style for video message bubbles (used by internal rendering)
 * @param audioBubbleStyle Optional style for audio message bubbles (used by internal rendering)
 * @param fileBubbleStyle Optional style for file message bubbles (used by internal rendering)
 * @param deleteBubbleStyle Optional style for deleted message bubbles (used by internal rendering)
 * @param actionBubbleStyle Optional style for action message bubbles (used by internal rendering)
 * @param callActionBubbleStyle Optional style for call action bubbles (used by internal rendering)
 * @param meetCallBubbleStyle Optional style for meet call bubbles (used by internal rendering)
 * @param pollBubbleStyle Optional style for poll message bubbles (used by internal rendering)
 * @param stickerBubbleStyle Optional style for sticker message bubbles (used by internal rendering)
 * @param collaborativeBubbleStyle Optional style for collaborative bubbles (used by internal rendering)
 *
 * @see CometChatMessageBubbleStyle
 * @see BubbleFactory.getBubbleStyle
 */
@Composable
fun CometChatMessageBubble(
    message: BaseMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatMessageBubbleStyle? = null,
    factory: BubbleFactory? = null,
    shouldShowDefaultAvatar: Boolean = true,
    timeStampAlignment: UIKitConstants.TimeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM,
    hideModerationView: Boolean = false,
    leadingView: (@Composable () -> Unit)? = null,
    headerView: (@Composable () -> Unit)? = null,
    replyView: (@Composable () -> Unit)? = null,
    contentView: (@Composable () -> Unit)? = null,
    bottomView: (@Composable () -> Unit)? = null,
    statusInfoView: (@Composable () -> Unit)? = null,
    threadView: (@Composable () -> Unit)? = null,
    footerView: (@Composable () -> Unit)? = null,
    onThreadRepliesClick: ((BaseMessage) -> Unit)? = null,
    onReactionClick: ((BaseMessage, String) -> Unit)? = null,
    onReactionLongClick: ((BaseMessage, String) -> Unit)? = null,
    onAddMoreReactionsClick: ((BaseMessage) -> Unit)? = null,
    // Style parameters for internal rendering (passed from MessageList)
    textBubbleStyle: CometChatTextBubbleStyle? = null,
    imageBubbleStyle: CometChatImageBubbleStyle? = null,
    videoBubbleStyle: CometChatVideoBubbleStyle? = null,
    audioBubbleStyle: CometChatAudioBubbleStyle? = null,
    fileBubbleStyle: CometChatFileBubbleStyle? = null,
    deleteBubbleStyle: CometChatDeleteBubbleStyle? = null,
    actionBubbleStyle: CometChatActionBubbleStyle? = null,
    callActionBubbleStyle: CometChatCallActionBubbleStyle? = null,
    meetCallBubbleStyle: CometChatMeetCallBubbleStyle? = null,
    pollBubbleStyle: CometChatPollBubbleStyle? = null,
    stickerBubbleStyle: CometChatStickerBubbleStyle? = null,
    collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle? = null,
    // NEW parameters for parity with chatuikit-kotlin
    hideReceipts: Boolean = false,
    hideReactions: Boolean = false,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    timeFormat: String? = null,
    dateTimeFormatter: ((Long) -> String)? = null,
    onMessagePreviewClick: ((BaseMessage) -> Unit)? = null,
    incomingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    outgoingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    bubbleStyles: BubbleStyles = BubbleStyles(),
    onLongClick: (() -> Unit)? = null,
    // Mention callbacks for text message bubbles
    onMentionClick: ((User) -> Unit)? = null,
    onMentionAllClick: (() -> Unit)? = null,
    mentionTextStyle: MentionTextStyle? = null,
    // Highlight parameters for jump-to-parent-message feature
    highlightedMessageId: Long = -1L,
    highlightAlpha: Float = 0f
) {
    // Check if factory provides a complete bubble replacement via getBubbleView
    // When getBubbleView returns non-null, it replaces the entire CometChatMessageBubble
    // including all slots (leading, header, content, footer, etc.)
    val customBubbleView = factory?.getBubbleView(message, alignment)
    if (customBubbleView != null) {
        // Render the custom bubble view and return early
        // The custom view is responsible for the complete bubble UI
        customBubbleView()
        return
    }

    // Wire onDispose lifecycle for custom factories.
    // When a factory is present, call factory.onDispose(message) when the composable
    // leaves the composition, allowing the factory to release resources (e.g., media playback).
    if (factory != null) {
        DisposableEffect(message.id) {
            onDispose {
                try {
                    factory.onDispose(message)
                } catch (e: Exception) {
                    android.util.Log.e(
                        "CometChatMessageBubble",
                        "Factory onDispose threw for message ${message.id}: ${e.message}",
                        e
                    )
                }
            }
        }
    }

    // Step 1: Determine which per-bubble-type style applies to this message
    val contentStyle: CometChatMessageBubbleStyle? = resolveContentStyleForMessage(
        message, alignment, factory,
        textBubbleStyle, imageBubbleStyle, videoBubbleStyle, audioBubbleStyle,
        fileBubbleStyle, deleteBubbleStyle, actionBubbleStyle, callActionBubbleStyle,
        meetCallBubbleStyle, pollBubbleStyle, stickerBubbleStyle, collaborativeBubbleStyle
    )

    // Step 2: Three-tier priority chain for the outer container
    // Priority 1 (highest): Factory style from ComposeBubbleFactory.getBubbleStyle()
    // Priority 2: Explicit incoming/outgoing style for LEFT/RIGHT alignment
    // Priority 3 (lowest): Alignment-based default
    //
    // Within each tier, per-bubble-type contentStyle is merged when available.
    // Note: We always fall back to alignment-based defaults so that outgoing bubbles
    // get primary backgroundColor and incoming get backgroundColor3. When messageBubbleStyle
    // is provided, it's merged INTO the alignment default — developer overrides on
    // messageBubbleStyle take effect, but alignment-specific colors are preserved when
    // the developer hasn't overridden them.
    val alignmentDefault = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.LEFT -> CometChatMessageBubbleStyle.incoming()
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatMessageBubbleStyle.outgoing()
        UIKitConstants.MessageBubbleAlignment.CENTER -> CometChatMessageBubbleStyle.default(
            backgroundColor = Color.Transparent
        )
    }

    // Three-tier style resolution:
    // Tier 1 (highest): Factory style
    // Tier 2: Explicit incoming/outgoing style based on alignment
    // Tier 3 (lowest): Alignment-based default
    val baseStyle = factory?.getBubbleStyle(message, alignment)
        ?: when (alignment) {
            UIKitConstants.MessageBubbleAlignment.LEFT ->
                incomingMessageBubbleStyle ?: alignmentDefault
            UIKitConstants.MessageBubbleAlignment.RIGHT ->
                outgoingMessageBubbleStyle ?: alignmentDefault
            else -> alignmentDefault
        }

    // Merge per-bubble-type contentStyle on top of the resolved base style
    // Special handling for stickers: they should always have transparent background
    // even when no explicit stickerBubbleStyle is provided
    val effectiveStyle = when {
        contentStyle != null -> mergeWithBase(contentStyle, baseStyle)
        // Stickers need transparent background for the outer wrapper (like Java implementation)
        message.category == CometChatConstants.CATEGORY_CUSTOM && 
        message.type == InternalContentRenderer.EXTENSION_STICKER -> 
            CometChatMessageBubbleStyle(
                backgroundColor = Color.Transparent,
                cornerRadius = baseStyle.cornerRadius,
                strokeWidth = baseStyle.strokeWidth,
                strokeColor = baseStyle.strokeColor,
                padding = baseStyle.padding,
                senderNameTextColor = baseStyle.senderNameTextColor,
                senderNameTextStyle = baseStyle.senderNameTextStyle,
                threadIndicatorTextColor = baseStyle.threadIndicatorTextColor,
                threadIndicatorTextStyle = baseStyle.threadIndicatorTextStyle,
                threadIndicatorIconTint = baseStyle.threadIndicatorIconTint,
                timestampTextColor = baseStyle.timestampTextColor,
                timestampTextStyle = baseStyle.timestampTextStyle,
                dateStyle = baseStyle.dateStyle,
                messageReceiptStyle = baseStyle.messageReceiptStyle,
                avatarStyle = baseStyle.avatarStyle,
                reactionStyle = baseStyle.reactionStyle,
                mentionStyle = baseStyle.mentionStyle,
                moderationViewStyle = baseStyle.moderationViewStyle,
                aiAssistantBubbleStyle = baseStyle.aiAssistantBubbleStyle,
                messagePreviewStyle = baseStyle.messagePreviewStyle,
                textBubbleStyle = baseStyle.textBubbleStyle,
                imageBubbleStyle = baseStyle.imageBubbleStyle,
                videoBubbleStyle = baseStyle.videoBubbleStyle,
                fileBubbleStyle = baseStyle.fileBubbleStyle,
                audioBubbleStyle = baseStyle.audioBubbleStyle,
                deleteBubbleStyle = baseStyle.deleteBubbleStyle,
                stickerBubbleStyle = baseStyle.stickerBubbleStyle,
                pollBubbleStyle = baseStyle.pollBubbleStyle,
                collaborativeBubbleStyle = baseStyle.collaborativeBubbleStyle,
                meetCallBubbleStyle = baseStyle.meetCallBubbleStyle
            )
        else -> baseStyle
    }

    // Determine if minimal slots should be used (action, call, deleted messages)
    // Minimal slots means no header, footer, statusInfo, thread, reply views
    // This only applies when using internal rendering (no factory)
    val useMinimalSlots = factory == null && InternalContentRenderer.shouldUseMinimalSlots(message)

    // Resolve leading view with avatar visibility consideration:
    // 1. If explicit leadingView is provided, always use it (custom provider override)
    // 2. If shouldShowDefaultAvatar is false, don't show any avatar
    // 3. If factory exists, use factory's leading view
    // 4. For internal rendering (no factory), use InternalContentRenderer.DefaultLeadingView
    val resolvedLeading: (@Composable () -> Unit)? = when {
        leadingView != null -> leadingView
        !shouldShowDefaultAvatar -> null
        factory != null -> factory.getLeadingView(message, alignment, effectiveStyle)
        else -> {
            // Internal rendering mode: use default leading view when shouldShowDefaultAvatar is true
            { InternalContentRenderer.DefaultLeadingView(message, alignment, effectiveStyle) }
        }
    }
    
    // Debug logging - trace leading view resolution
    android.util.Log.d("AvatarVisibility", "CometChatMessageBubble - Message: ${message.id}, alignment: $alignment, shouldShowDefaultAvatar: $shouldShowDefaultAvatar, hasCustomLeadingView: ${leadingView != null}, hasFactory: ${factory != null}, resolvedLeading is null: ${resolvedLeading == null}")

    // Compute showTime values based on timeStampAlignment:
    // - When TOP: show time in header, hide in status info
    // - When BOTTOM: hide time in header, show in status info
    val showTimeInHeader = timeStampAlignment == UIKitConstants.TimeStampAlignment.TOP
    val showTimeInStatusInfo = timeStampAlignment == UIKitConstants.TimeStampAlignment.BOTTOM

    // Resolve header view with timeStampAlignment consideration:
    // If explicit headerView is provided, use it. Otherwise, use factory's header view
    // or fall back to InternalContentRenderer.DefaultHeaderView with computed showTime.
    // For minimal slots (action/call messages), return null to hide the header.
    val resolvedHeader = when {
        headerView != null -> headerView
        useMinimalSlots -> null  // No header for action/call messages (center bubbles)
        else -> factory?.getHeaderView(message, alignment, effectiveStyle, showTimeInHeader)
            ?: { InternalContentRenderer.DefaultHeaderView(message, alignment, effectiveStyle, showTime = showTimeInHeader) }
    }

    // Resolve other slots: explicit parameter > factory slot > null
    // For minimal slots (action/call messages), return null to hide these views.
    val resolvedReply = when {
        replyView != null -> replyView
        useMinimalSlots -> null  // No reply view for action/call messages (center bubbles)
        else -> factory?.getReplyView(message, alignment, effectiveStyle)
            ?: {
                InternalContentRenderer.DefaultReplyView(
                    message = message,
                    alignment = alignment,
                    style = effectiveStyle,
                    textFormatters = textFormatters,
                    incomingMessagePreviewStyle = effectiveStyle.messagePreviewStyle
                        ?: bubbleStyles.incomingMessagePreviewStyle,
                    outgoingMessagePreviewStyle = effectiveStyle.messagePreviewStyle
                        ?: bubbleStyles.outgoingMessagePreviewStyle,
                    onMessagePreviewClick = onMessagePreviewClick
                )
            }
    }
    val resolvedBottom = bottomView ?: factory?.getBottomView(message, alignment, effectiveStyle, hideModerationView)

    // Resolve status info view with timeStampAlignment consideration:
    // If explicit statusInfoView is provided, use it. Otherwise, use factory's status info view
    // or fall back to InternalContentRenderer.DefaultStatusInfoView with computed showTime.
    // For minimal slots (action/call messages) or meeting messages, return null to hide the status info.
    val shouldHideStatusInfo = InternalContentRenderer.shouldHideStatusInfo(message, useMinimalSlots)
    val resolvedStatusInfo = when {
        statusInfoView != null -> statusInfoView
        shouldHideStatusInfo -> null  // No status info for action/call/meeting messages
        else -> factory?.getStatusInfoView(message, alignment, effectiveStyle, showTimeInStatusInfo)
            ?: { InternalContentRenderer.DefaultStatusInfoView(
                message = message,
                alignment = alignment,
                style = effectiveStyle,
                showTime = showTimeInStatusInfo,
                hideReceipts = hideReceipts,
                timeFormat = timeFormat,
                dateTimeFormatter = dateTimeFormatter
            ) }
    }

    // Resolve thread view: explicit parameter > factory slot > internal default > null
    // For minimal slots (action/call messages), return null to hide the thread view.
    val resolvedThread = when {
        threadView != null -> threadView
        useMinimalSlots -> null  // No thread view for action/call messages (center bubbles)
        else -> factory?.getThreadView(message, alignment, effectiveStyle, onThreadRepliesClick)
            ?: {
                InternalContentRenderer.DefaultThreadView(
                    message = message,
                    alignment = alignment,
                    style = effectiveStyle,
                    onThreadClick = onThreadRepliesClick
                )
            }
    }
    
    // Resolve footer view: explicit parameter > factory slot > default footer > null
    // For minimal slots (action/call messages), return null to hide the footer view.
    // When hideReactions is true, skip the footer (reactions) regardless of message reactions.
    val resolvedFooter: (@Composable () -> Unit)? = when {
        footerView != null -> footerView
        useMinimalSlots -> null  // No footer for action/call messages (center bubbles)
        hideReactions -> null    // Skip footer when reactions are hidden
        else -> factory?.getFooterView(
            message, alignment, effectiveStyle,
            onReactionClick, onReactionLongClick, onAddMoreReactionsClick
        ) ?: if (!message.reactions.isNullOrEmpty()) {
            {
                InternalContentRenderer.DefaultFooterView(
                    message = message,
                    alignment = alignment,
                    style = effectiveStyle,
                    onReactionClick = onReactionClick,
                    onReactionLongClick = onReactionLongClick,
                    onAddMoreReactionsClick = onAddMoreReactionsClick
                )
            }
        } else null
    }

    // Determine horizontal arrangement based on alignment
    val horizontalArrangement = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.LEFT -> Arrangement.Start
        UIKitConstants.MessageBubbleAlignment.RIGHT -> Arrangement.End
        UIKitConstants.MessageBubbleAlignment.CENTER -> Arrangement.Center
    }

    // Accessibility description
    val accessibilityDescription = remember(message.id, message.category, message.type) {
        "Message bubble: ${message.category} ${message.type}"
    }

    // Calculate highlight background color if this message is highlighted
    // Uses extended primary color with alpha for highlight effect (matching Java implementation)
    val primaryColor = CometChatTheme.colorScheme.extendedPrimaryColor800
    val highlightBackgroundColor = remember(highlightedMessageId, highlightAlpha, message.id, primaryColor) {
        if (message.id == highlightedMessageId && highlightAlpha > 0f) {
            primaryColor.copy(alpha = highlightAlpha)
        } else {
            Color.Transparent
        }
    }

    // Outer Box for highlight background - fills full width matching Java's parent LinearLayout
    // The parent LinearLayout in Java has layout_width="match_parent" so highlight covers full row
    // We use fillMaxWidth() and no horizontal padding to ensure edge-to-edge highlight
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(highlightBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .semantics { contentDescription = accessibilityDescription },
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.Top
        ) {
            // Leading view (avatar) - only for LEFT alignment typically
            if (alignment == UIKitConstants.MessageBubbleAlignment.LEFT) {
                resolvedLeading?.invoke()
            }

            // Main bubble content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 4.dp)
            ) {
            // Header view (sender name) - outside the bubble
            resolvedHeader?.invoke()

            // Bubble container with styling — uses effectiveStyle for the outer container.
            // Per-bubble-type styles (e.g., CometChatTextBubbleStyle) extend
            // CometChatMessageBubbleStyle and inherit wrapper properties, so the style
            // parameter already contains the correct values for backgroundColor,
            // cornerRadius, strokeWidth, strokeColor, and padding.
            val shape = RoundedCornerShape(effectiveStyle.cornerRadius)
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(effectiveStyle.backgroundColor, shape)
                    .then(
                        if (effectiveStyle.strokeWidth > 0.dp) {
                            Modifier.border(effectiveStyle.strokeWidth, effectiveStyle.strokeColor, shape)
                        } else {
                            Modifier
                        }
                    )
                    .padding(effectiveStyle.padding)
            ) {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    // Reply view (quoted message) - inside the bubble
                    resolvedReply?.invoke()

                    // Content view resolution: explicit slot > factory > internal rendering > fallback
                    // Priority 1: Explicit contentView slot always wins
                    // Priority 2: Factory-provided content (when factory exists for this message type)
                    // Priority 3: Internal rendering via InternalContentRenderer
                    // Priority 4: FallbackBubble for unknown types
                    when {
                        contentView != null -> contentView()
                        factory != null -> factory.getContentView(message, alignment, effectiveStyle, textFormatters).invoke()
                        else -> {
                            // Create BubbleStyles from individual style parameters
                            val bubbleStyles = BubbleStyles(
                                messageBubbleStyle = style,
                                textBubbleStyle = textBubbleStyle,
                                imageBubbleStyle = imageBubbleStyle,
                                videoBubbleStyle = videoBubbleStyle,
                                audioBubbleStyle = audioBubbleStyle,
                                fileBubbleStyle = fileBubbleStyle,
                                deleteBubbleStyle = deleteBubbleStyle,
                                actionBubbleStyle = actionBubbleStyle,
                                callActionBubbleStyle = callActionBubbleStyle,
                                meetCallBubbleStyle = meetCallBubbleStyle,
                                pollBubbleStyle = pollBubbleStyle,
                                stickerBubbleStyle = stickerBubbleStyle,
                                collaborativeBubbleStyle = collaborativeBubbleStyle
                            )
                            // Try internal rendering, fall back to FallbackBubble if type is unknown
                            val rendered = InternalContentRenderer.renderContent(
                                message = message,
                                alignment = alignment,
                                styles = bubbleStyles,
                                textFormatters = textFormatters,
                                onLongClick = onLongClick,
                                onMentionClick = onMentionClick,
                                onMentionAllClick = onMentionAllClick,
                                mentionTextStyle = mentionTextStyle
                            )
                            if (!rendered) {
                                FallbackBubble(message)
                            }
                        }
                    }

                    // Status info view (timestamp, receipts) - inside the bubble per XML layout
                    // Wrapped in Box with end alignment to match XML's android:layout_gravity="bottom|end"
                    if (resolvedStatusInfo != null) {
                        Box(modifier = Modifier.align(Alignment.End)) {
                            resolvedStatusInfo()
                        }
                    }

                    // Bottom view (moderation) - inside the bubble
                    resolvedBottom?.invoke()
                }
            }

            // Footer view (reactions) - outside the bubble
            // Uses negative top margin (-8dp) to create overlapping effect with the bubble
            // This matches the XML layout's android:layout_marginTop="-8dp" on footer_view_layout
            if (resolvedFooter != null) {
                Box(
                    modifier = Modifier
                        .offset(y = (-8).dp)
                        .defaultMinSize(minHeight = 8.dp)
                ) {
                    resolvedFooter()
                }
            }

            // Thread view (reply count) - outside the bubble
            resolvedThread?.invoke()
        }

        // Leading view for RIGHT alignment (if needed for symmetry)
        if (alignment == UIKitConstants.MessageBubbleAlignment.RIGHT) {
            resolvedLeading?.invoke()
        }
    }
    } // End of outer Box for highlight
}

/**
 * Fallback composable for unsupported message types.
 */
@Composable
private fun FallbackBubble(message: BaseMessage) {
    Text(
        text = "Unsupported: ${message.category}_${message.type}",
        style = CometChatTheme.typography.caption1Regular,
        color = CometChatTheme.colorScheme.textColorTertiary,
        modifier = Modifier.padding(12.dp)
    )
}

/**
 * Maps a message's category and type to the corresponding per-bubble-type style parameter.
 *
 * This function is part of the three-tier priority chain for style resolution:
 * - Priority 3 (highest): Factory style
 * - Priority 2: Content style (resolved by this function)
 * - Priority 1 (lowest): messageBubbleStyle
 *
 * Returns the explicitly-set per-bubble-type style, or null if:
 * - A factory is present (factory handles its own styling)
 * - The message type is unknown/unrecognized
 * - No explicit style was set for the message type
 *
 * For deleted messages (deletedAt > 0), returns [deleteBubbleStyle] regardless of message type.
 *
 * This function does NOT create alignment-based defaults — that's handled by the fallback chain.
 *
 * @param message The message to resolve the style for
 * @param alignment The bubble alignment (unused in current implementation, reserved for future use)
 * @param factory The bubble factory, if any. When non-null, this function returns null
 * @param textBubbleStyle Style for text messages
 * @param imageBubbleStyle Style for image messages
 * @param videoBubbleStyle Style for video messages
 * @param audioBubbleStyle Style for audio messages
 * @param fileBubbleStyle Style for file messages
 * @param deleteBubbleStyle Style for deleted messages
 * @param actionBubbleStyle Style for action messages
 * @param callActionBubbleStyle Style for call action messages
 * @param meetCallBubbleStyle Style for meet/call messages
 * @param pollBubbleStyle Style for poll messages
 * @param stickerBubbleStyle Style for sticker messages
 * @param collaborativeBubbleStyle Style for collaborative (document/whiteboard) messages
 * @return The resolved [CometChatMessageBubbleStyle] or null
 */
@Composable
internal fun resolveContentStyleForMessage(
    message: BaseMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    factory: BubbleFactory?,
    textBubbleStyle: CometChatTextBubbleStyle?,
    imageBubbleStyle: CometChatImageBubbleStyle?,
    videoBubbleStyle: CometChatVideoBubbleStyle?,
    audioBubbleStyle: CometChatAudioBubbleStyle?,
    fileBubbleStyle: CometChatFileBubbleStyle?,
    deleteBubbleStyle: CometChatDeleteBubbleStyle?,
    actionBubbleStyle: CometChatActionBubbleStyle?,
    callActionBubbleStyle: CometChatCallActionBubbleStyle?,
    meetCallBubbleStyle: CometChatMeetCallBubbleStyle?,
    pollBubbleStyle: CometChatPollBubbleStyle?,
    stickerBubbleStyle: CometChatStickerBubbleStyle?,
    collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle?
): CometChatMessageBubbleStyle? {
    // If factory handles this message, don't resolve content style here
    if (factory != null) return null

    // Deleted messages
    if (message.deletedAt > 0) return deleteBubbleStyle

    return when (message.category) {
        CometChatConstants.CATEGORY_MESSAGE -> when (message.type) {
            CometChatConstants.MESSAGE_TYPE_TEXT -> textBubbleStyle
            CometChatConstants.MESSAGE_TYPE_IMAGE -> imageBubbleStyle
            CometChatConstants.MESSAGE_TYPE_VIDEO -> videoBubbleStyle
            CometChatConstants.MESSAGE_TYPE_AUDIO -> audioBubbleStyle
            CometChatConstants.MESSAGE_TYPE_FILE -> fileBubbleStyle
            else -> null
        }
        CometChatConstants.CATEGORY_ACTION -> actionBubbleStyle
        CometChatConstants.CATEGORY_CALL -> callActionBubbleStyle
        CometChatConstants.CATEGORY_CUSTOM -> when (message.type) {
            InternalContentRenderer.EXTENSION_POLLS -> pollBubbleStyle
            InternalContentRenderer.EXTENSION_STICKER -> stickerBubbleStyle
            InternalContentRenderer.EXTENSION_DOCUMENT,
            InternalContentRenderer.EXTENSION_WHITEBOARD -> collaborativeBubbleStyle
            else -> null
        }
        UIKitConstants.MessageType.MEETING -> meetCallBubbleStyle
        else -> null
    }
}
