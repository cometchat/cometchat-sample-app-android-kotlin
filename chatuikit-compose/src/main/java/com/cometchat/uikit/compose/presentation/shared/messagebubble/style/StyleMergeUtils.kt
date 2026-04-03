package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sentinel constants indicating that a CommonProperty has not been explicitly set.
 *
 * When a bubble-specific style is created via its factory function (e.g.,
 * [CometChatTextBubbleStyle.incoming]), CommonProperties default to these sentinel
 * values. During style merging via [mergeWithBase], sentinel values are replaced
 * with the corresponding value from the base [CometChatMessageBubbleStyle].
 *
 * If a developer explicitly sets a CommonProperty (e.g., `backgroundColor = Color.Red`),
 * that value is NOT a sentinel and takes precedence over the base style.
 */
internal val UNSET_COLOR = Color.Unspecified
internal val UNSET_DP = Dp.Unspecified
internal val UNSET_TEXT_STYLE = TextStyle.Default
internal val UNSET_PADDING: PaddingValues = object : PaddingValues {
    override fun calculateBottomPadding(): Dp = 0.dp
    override fun calculateTopPadding(): Dp = 0.dp
    override fun calculateLeftPadding(layoutDirection: androidx.compose.ui.unit.LayoutDirection): Dp = 0.dp
    override fun calculateRightPadding(layoutDirection: androidx.compose.ui.unit.LayoutDirection): Dp = 0.dp
    override fun toString(): String = "UNSET_PADDING"
}

/**
 * Merges a bubble-specific style with a base [CometChatMessageBubbleStyle].
 *
 * For each CommonProperty (backgroundColor, cornerRadius, strokeWidth, strokeColor,
 * padding, senderNameTextColor, senderNameTextStyle, threadIndicatorTextColor,
 * threadIndicatorTextStyle, threadIndicatorIconTint, timestampTextColor, timestampTextStyle):
 * - If the bubble-specific value is a sentinel (unset), use the base value.
 * - If the bubble-specific value is explicitly set, use that value.
 *
 * Content-specific properties (e.g., textColor, linkColor for text bubbles) are
 * always preserved from the original [bubbleStyle].
 *
 * The returned instance is the same concrete type as [bubbleStyle], produced via
 * `copy()` on the data class.
 *
 * @param T The concrete bubble-specific style type extending [CometChatMessageBubbleStyle].
 * @param bubbleStyle The bubble-specific style whose sentinel CommonProperties should be filled.
 * @param base The base message bubble style providing fallback values for sentinel properties.
 * @return A new instance of [T] with sentinel CommonProperties replaced by [base] values.
 */
@Suppress("UNCHECKED_CAST")
fun <T : CometChatMessageBubbleStyle> mergeWithBase(
    bubbleStyle: T,
    base: CometChatMessageBubbleStyle
): T {
    val mergedBg = if (bubbleStyle.backgroundColor == UNSET_COLOR) base.backgroundColor else bubbleStyle.backgroundColor
    val mergedRadius = if (bubbleStyle.cornerRadius == UNSET_DP) base.cornerRadius else bubbleStyle.cornerRadius
    val mergedStrokeW = if (bubbleStyle.strokeWidth == UNSET_DP) base.strokeWidth else bubbleStyle.strokeWidth
    val mergedStrokeC = if (bubbleStyle.strokeColor == UNSET_COLOR) base.strokeColor else bubbleStyle.strokeColor
    val mergedPadding = if (bubbleStyle.padding == UNSET_PADDING) base.padding else bubbleStyle.padding
    val mergedSenderNameColor = if (bubbleStyle.senderNameTextColor == UNSET_COLOR) base.senderNameTextColor else bubbleStyle.senderNameTextColor
    val mergedSenderNameStyle = if (bubbleStyle.senderNameTextStyle == UNSET_TEXT_STYLE) base.senderNameTextStyle else bubbleStyle.senderNameTextStyle
    val mergedThreadColor = if (bubbleStyle.threadIndicatorTextColor == UNSET_COLOR) base.threadIndicatorTextColor else bubbleStyle.threadIndicatorTextColor
    val mergedThreadStyle = if (bubbleStyle.threadIndicatorTextStyle == UNSET_TEXT_STYLE) base.threadIndicatorTextStyle else bubbleStyle.threadIndicatorTextStyle
    val mergedThreadIcon = if (bubbleStyle.threadIndicatorIconTint == UNSET_COLOR) base.threadIndicatorIconTint else bubbleStyle.threadIndicatorIconTint
    val mergedTimestampColor = if (bubbleStyle.timestampTextColor == UNSET_COLOR) base.timestampTextColor else bubbleStyle.timestampTextColor
    val mergedTimestampStyle = if (bubbleStyle.timestampTextStyle == UNSET_TEXT_STYLE) base.timestampTextStyle else bubbleStyle.timestampTextStyle

    return when (bubbleStyle) {
        is CometChatTextBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatImageBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatVideoBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatAudioBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatFileBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatDeleteBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatActionBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatCallActionBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatMeetCallBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatPollBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatStickerBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        is CometChatCollaborativeBubbleStyle -> bubbleStyle.copy(
            backgroundColor = mergedBg,
            cornerRadius = mergedRadius,
            strokeWidth = mergedStrokeW,
            strokeColor = mergedStrokeC,
            padding = mergedPadding,
            senderNameTextColor = mergedSenderNameColor,
            senderNameTextStyle = mergedSenderNameStyle,
            threadIndicatorTextColor = mergedThreadColor,
            threadIndicatorTextStyle = mergedThreadStyle,
            threadIndicatorIconTint = mergedThreadIcon,
            timestampTextColor = mergedTimestampColor,
            timestampTextStyle = mergedTimestampStyle
        ) as T

        else -> bubbleStyle // Unknown type, return as-is
    }
}
