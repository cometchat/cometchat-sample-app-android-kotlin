package com.cometchat.uikit.compose.presentation.shared.messagebubble.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for [CometChatDeleteBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of deleted message bubbles, including text styling (italic), icon tint,
 * and background styling.
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
 * CometChatDeleteBubble(
 *     message = baseMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatDeleteBubbleStyle.incoming()
 * )
 * ```
 *
 * @property textColor The color of the "This message was deleted" text
 * @property textStyle The text style for the deleted message text (italic by default)
 * @property iconTint The tint color of the delete icon
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
data class CometChatDeleteBubbleStyle(
    // Content-specific properties ONLY
    val textColor: Color,
    val textStyle: TextStyle,
    val iconTint: Color,
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
         * Creates a default delete bubble style using CometChat theme tokens.
         *
         * @param textColor The color of the "This message was deleted" text
         * @param textStyle The text style for the deleted message text (italic by default)
         * @param iconTint The tint color of the delete icon
         * @param backgroundColor The background color of the bubble
         * @param cornerRadius The corner radius of the bubble
         * @param strokeWidth The stroke width of the bubble border
         * @param strokeColor The stroke color of the bubble border
         * @param padding The internal padding of the bubble content
         * @param senderNameTextColor The color of sender name text
         * @param senderNameTextStyle The text style for sender name
         * @param threadIndicatorTextColor The color of thread indicator text
         * @param threadIndicatorTextStyle The text style for thread indicator
         * @param threadIndicatorIconTint The tint color of thread indicator icon
         * @return A new [CometChatDeleteBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            textColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular.copy(fontStyle = FontStyle.Italic),
            iconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            // CommonProperties default to sentinels (filled from messageBubbleStyle during merge)
            backgroundColor: Color = UNSET_COLOR,
            cornerRadius: Dp = UNSET_DP,
            strokeWidth: Dp = UNSET_DP,
            strokeColor: Color = UNSET_COLOR,
            padding: PaddingValues = UNSET_PADDING,
            senderNameTextColor: Color = UNSET_COLOR,
            senderNameTextStyle: TextStyle = UNSET_TEXT_STYLE,
            threadIndicatorTextColor: Color = UNSET_COLOR,
            threadIndicatorTextStyle: TextStyle = UNSET_TEXT_STYLE,
            threadIndicatorIconTint: Color = UNSET_COLOR,
            timestampTextColor: Color = UNSET_COLOR,
            timestampTextStyle: TextStyle = UNSET_TEXT_STYLE
        ): CometChatDeleteBubbleStyle = CometChatDeleteBubbleStyle(
            textColor = textColor,
            textStyle = textStyle,
            iconTint = iconTint,
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
         * Creates a style for incoming (left-aligned) deleted messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages.
         *
         * @return A new [CometChatDeleteBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatDeleteBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) deleted messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white text/icon with alpha for contrast.
         *
         * @return A new [CometChatDeleteBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatDeleteBubbleStyle = default(
            textColor = CometChatTheme.colorScheme.colorWhite.copy(alpha = 0.7f),
            iconTint = CometChatTheme.colorScheme.colorWhite.copy(alpha = 0.7f)
        )
    }
}
