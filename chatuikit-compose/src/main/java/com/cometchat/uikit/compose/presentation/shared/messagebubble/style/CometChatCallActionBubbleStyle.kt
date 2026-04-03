package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatCallActionBubble composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of call action message bubbles, which display call-related messages like
 * incoming, outgoing, and missed calls.
 *
 * Common wrapper properties (backgroundColor, cornerRadius, strokeWidth, strokeColor, padding,
 * senderName styling, threadIndicator styling) are inherited from the parent class.
 *
 * Call action bubbles are typically centered and don't have sender name or thread indicators.
 *
 * Use the companion object's factory functions to create instances:
 * - [default] for the standard call action bubble style
 * - [incoming] for incoming (left-aligned) messages
 * - [outgoing] for outgoing (right-aligned) messages
 */
@Immutable
data class CometChatCallActionBubbleStyle(
    // Content-specific properties ONLY
    val textColor: Color,
    val textStyle: TextStyle,
    val iconTint: Color,
    val missedCallTextColor: Color,
    val missedCallTextStyle: TextStyle,
    val missedCallBackgroundColor: Color,
    val missedCallIconTint: Color,
    // Common properties passed to parent via override
    override val backgroundColor: Color,
    override val cornerRadius: Dp,
    override val strokeWidth: Dp,
    override val strokeColor: Color,
    override val padding: PaddingValues,
    override val senderNameTextColor: Color,
    override val senderNameTextStyle: TextStyle,
    override val threadIndicatorTextColor: Color,
    override val threadIndicatorTextStyle: TextStyle,
    override val threadIndicatorIconTint: Color,
    override val timestampTextColor: Color,
    override val timestampTextStyle: TextStyle
) : CometChatMessageBubbleStyle(
    backgroundColor = backgroundColor,
    cornerRadius = cornerRadius,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
    padding = padding,
    senderNameTextColor = senderNameTextColor,
    senderNameTextStyle = senderNameTextStyle,
    threadIndicatorTextColor = threadIndicatorTextColor,
    threadIndicatorTextStyle = threadIndicatorTextStyle,
    threadIndicatorIconTint = threadIndicatorIconTint,
    timestampTextColor = timestampTextColor,
    timestampTextStyle = timestampTextStyle
) {
    companion object {
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            textColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            textStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            iconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            missedCallTextColor: Color = CometChatTheme.colorScheme.errorColor,
            missedCallTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            missedCallBackgroundColor: Color = CometChatTheme.colorScheme.errorColor.copy(alpha = 0.1f),
            missedCallIconTint: Color = CometChatTheme.colorScheme.errorColor,
            // Common properties — use simple defaults (no style resolution/merging for call action bubbles)
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            cornerRadius: Dp = 1000.dp,
            strokeWidth: Dp = 1.dp,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            padding: PaddingValues = PaddingValues(8.dp, 4.dp),
            senderNameTextColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            senderNameTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            threadIndicatorTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            threadIndicatorTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            threadIndicatorIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            timestampTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            timestampTextStyle: TextStyle = CometChatTheme.typography.caption1Regular
        ): CometChatCallActionBubbleStyle = CometChatCallActionBubbleStyle(
            textColor = textColor,
            textStyle = textStyle,
            iconTint = iconTint,
            missedCallTextColor = missedCallTextColor,
            missedCallTextStyle = missedCallTextStyle,
            missedCallBackgroundColor = missedCallBackgroundColor,
            missedCallIconTint = missedCallIconTint,
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            padding = padding,
            senderNameTextColor = senderNameTextColor,
            senderNameTextStyle = senderNameTextStyle,
            threadIndicatorTextColor = threadIndicatorTextColor,
            threadIndicatorTextStyle = threadIndicatorTextStyle,
            threadIndicatorIconTint = threadIndicatorIconTint,
            timestampTextColor = timestampTextColor,
            timestampTextStyle = timestampTextStyle
        )

        @Composable
        fun incoming(): CometChatCallActionBubbleStyle = default()

        @Composable
        fun outgoing(): CometChatCallActionBubbleStyle = default(
            textColor = CometChatTheme.colorScheme.colorWhite,
            iconTint = CometChatTheme.colorScheme.colorWhite
        )
    }
}
