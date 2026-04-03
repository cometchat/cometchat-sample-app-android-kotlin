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
 * Style configuration for [CometChatActionBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of action message bubbles, which display system action messages like
 * "User joined the group" or "User left".
 *
 * Action bubbles are typically centered and have a distinct visual style to differentiate
 * them from regular messages. They don't typically show sender name or thread indicators,
 * but the properties are inherited from the parent class for consistency.
 *
 * Common wrapper properties (backgroundColor, cornerRadius, strokeWidth, strokeColor, padding,
 * senderName styling, threadIndicator styling) are inherited from the parent class.
 *
 * Use the companion object's factory functions to create instances:
 * - [default] for a neutral style
 * - [incoming] for incoming (left-aligned) messages
 * - [outgoing] for outgoing (right-aligned) messages
 *
 * Example usage:
 * ```kotlin
 * CometChatActionBubble(
 *     message = actionMessage,
 *     style = CometChatActionBubbleStyle.default()
 * )
 * ```
 *
 * @property textColor The color of the action text
 * @property textStyle The text style for the action text
 * @property backgroundColor The background color of the bubble (inherited from parent)
 * @property cornerRadius The corner radius of the bubble (inherited from parent)
 * @property strokeWidth The stroke width of the bubble border (inherited from parent)
 * @property strokeColor The stroke color of the bubble border (inherited from parent)
 * @property padding The internal padding of the bubble content (inherited from parent)
 * @property senderNameTextColor The color of sender name text (inherited from parent)
 * @property senderNameTextStyle The text style for sender name (inherited from parent)
 * @property threadIndicatorTextColor The color of thread indicator text (inherited from parent)
 * @property threadIndicatorTextStyle The text style for thread indicator (inherited from parent)
 * @property threadIndicatorIconTint The tint color of thread indicator icon (inherited from parent)
 */
@Immutable
data class CometChatActionBubbleStyle(
    // Content-specific properties ONLY
    val textColor: Color,
    val textStyle: TextStyle,
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
        /**
         * Creates a default action bubble style using CometChat theme tokens.
         *
         * @param textColor The color of the action text
         * @param textStyle The text style for the action text
         * @param backgroundColor The background color of the bubble
         * @param cornerRadius The corner radius of the bubble (action bubbles typically use smaller radius)
         * @param strokeWidth The stroke width of the bubble border
         * @param strokeColor The stroke color of the bubble border
         * @param padding The internal padding of the bubble content
         * @param senderNameTextColor The color of sender name text
         * @param senderNameTextStyle The text style for sender name
         * @param threadIndicatorTextColor The color of thread indicator text
         * @param threadIndicatorTextStyle The text style for thread indicator
         * @param threadIndicatorIconTint The tint color of thread indicator icon
         * @return A new [CometChatActionBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            textColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            textStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            // Common properties — use simple defaults (no style resolution/merging for action bubbles)
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
        ): CometChatActionBubbleStyle = CometChatActionBubbleStyle(
            textColor = textColor,
            textStyle = textStyle,
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

        /**
         * Creates a style for incoming (left-aligned) action messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages.
         *
         * @return A new [CometChatActionBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatActionBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) action messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white text for contrast.
         *
         * @return A new [CometChatActionBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatActionBubbleStyle = default(
            textColor = CometChatTheme.colorScheme.colorWhite
        )
    }
}
