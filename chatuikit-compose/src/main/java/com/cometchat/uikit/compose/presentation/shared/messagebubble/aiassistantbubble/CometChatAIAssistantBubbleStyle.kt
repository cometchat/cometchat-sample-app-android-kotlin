package com.cometchat.uikit.compose.presentation.shared.messagebubble.aiassistantbubble

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for the AI assistant bubble component.
 *
 * This immutable data class defines the visual appearance of AI assistant
 * message bubbles, matching the XML attributes defined in
 * att_cometchat_ai_assistant_bubble.xml.
 *
 * Use the companion object's factory functions to create instances:
 * - [default] for a neutral style
 * - [incoming] for incoming (left-aligned) AI assistant bubbles
 * - [outgoing] for outgoing (right-aligned) AI assistant bubbles
 *
 * @property backgroundColor The background color of the AI assistant bubble
 * @property textColor The color of the text content
 * @property textStyle The text style for the content
 * @property cornerRadius The corner radius of the bubble container
 * @property strokeWidth The stroke width of the bubble border
 * @property strokeColor The stroke color of the bubble border
 */
@Immutable
data class CometChatAIAssistantBubbleStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val textStyle: TextStyle,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color
) {
    companion object {
        /**
         * Creates a default AI assistant bubble style using CometChat theme tokens.
         *
         * @param backgroundColor The background color of the bubble
         * @param textColor The color of the text content
         * @param textStyle The text style for the content
         * @param cornerRadius The corner radius of the bubble container
         * @param strokeWidth The stroke width of the bubble border
         * @param strokeColor The stroke color of the bubble border
         * @return A new [CometChatAIAssistantBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            textColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent
        ): CometChatAIAssistantBubbleStyle = CometChatAIAssistantBubbleStyle(
            backgroundColor = backgroundColor,
            textColor = textColor,
            textStyle = textStyle,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor
        )

        /**
         * Creates a style for incoming (left-aligned) AI assistant bubbles.
         *
         * @param backgroundColor The background color of the bubble
         * @param textColor The color of the text content
         * @param textStyle The text style for the content
         * @param cornerRadius The corner radius of the bubble container
         * @param strokeWidth The stroke width of the bubble border
         * @param strokeColor The stroke color of the bubble border
         * @return A new [CometChatAIAssistantBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            textColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent
        ): CometChatAIAssistantBubbleStyle = CometChatAIAssistantBubbleStyle(
            backgroundColor = backgroundColor,
            textColor = textColor,
            textStyle = textStyle,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor
        )

        /**
         * Creates a style for outgoing (right-aligned) AI assistant bubbles.
         *
         * @param backgroundColor The background color of the bubble
         * @param textColor The color of the text content
         * @param textStyle The text style for the content
         * @param cornerRadius The corner radius of the bubble container
         * @param strokeWidth The stroke width of the bubble border
         * @param strokeColor The stroke color of the bubble border
         * @return A new [CometChatAIAssistantBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            textColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent
        ): CometChatAIAssistantBubbleStyle = CometChatAIAssistantBubbleStyle(
            backgroundColor = backgroundColor,
            textColor = textColor,
            textStyle = textStyle,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor
        )
    }
}
