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
 * @property shimmerTextColor The text color used in the shimmer "Thinking" state
 * @property shimmerTextStyle The text style used in the shimmer "Thinking" state
 * @property errorBackgroundColor The background color of the error indicator card
 * @property errorTextColor The text color of the error message
 * @property errorTextStyle The text style of the error message
 * @property errorIconTint The tint color of the error icon
 */
@Immutable
data class CometChatAIAssistantBubbleStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val textStyle: TextStyle,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color,
    val shimmerTextColor: Color = Color.Unspecified,
    val shimmerTextStyle: TextStyle = TextStyle.Default,
    val errorBackgroundColor: Color = Color.Unspecified,
    val errorTextColor: Color = Color.Unspecified,
    val errorTextStyle: TextStyle = TextStyle.Default,
    val errorIconTint: Color = Color.Unspecified
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
         * @param shimmerTextColor The text color for the shimmer "Thinking" state
         * @param shimmerTextStyle The text style for the shimmer "Thinking" state
         * @param errorBackgroundColor The background color of the error indicator
         * @param errorTextColor The text color of the error message
         * @param errorTextStyle The text style of the error message
         * @param errorIconTint The tint color of the error icon
         * @return A new [CometChatAIAssistantBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Transparent by default so the inner AI bubble never paints over the outer
            // message-bubble wrapper. The wrapper controls the visible color: transparent
            // in 1:1 agent chats, and the filled incoming color in groups (matches the
            // Kotlin UIKit, where the inner bubble is transparent unless explicitly styled).
            backgroundColor: Color = Color.Transparent,
            textColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent,
            shimmerTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            shimmerTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            errorBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            errorTextColor: Color = CometChatTheme.colorScheme.errorColor,
            errorTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            errorIconTint: Color = CometChatTheme.colorScheme.errorColor
        ): CometChatAIAssistantBubbleStyle = CometChatAIAssistantBubbleStyle(
            backgroundColor = backgroundColor,
            textColor = textColor,
            textStyle = textStyle,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            shimmerTextColor = shimmerTextColor,
            shimmerTextStyle = shimmerTextStyle,
            errorBackgroundColor = errorBackgroundColor,
            errorTextColor = errorTextColor,
            errorTextStyle = errorTextStyle,
            errorIconTint = errorIconTint
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
         * @param shimmerTextColor The text color for the shimmer "Thinking" state
         * @param shimmerTextStyle The text style for the shimmer "Thinking" state
         * @param errorBackgroundColor The background color of the error indicator
         * @param errorTextColor The text color of the error message
         * @param errorTextStyle The text style of the error message
         * @param errorIconTint The tint color of the error icon
         * @return A new [CometChatAIAssistantBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(
            // Transparent so the outer message-bubble wrapper controls the visible color
            // (transparent in 1:1 agent chats, filled incoming color in groups).
            backgroundColor: Color = Color.Transparent,
            textColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent,
            shimmerTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            shimmerTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            errorBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            errorTextColor: Color = CometChatTheme.colorScheme.errorColor,
            errorTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            errorIconTint: Color = CometChatTheme.colorScheme.errorColor
        ): CometChatAIAssistantBubbleStyle = CometChatAIAssistantBubbleStyle(
            backgroundColor = backgroundColor,
            textColor = textColor,
            textStyle = textStyle,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            shimmerTextColor = shimmerTextColor,
            shimmerTextStyle = shimmerTextStyle,
            errorBackgroundColor = errorBackgroundColor,
            errorTextColor = errorTextColor,
            errorTextStyle = errorTextStyle,
            errorIconTint = errorIconTint
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
         * @param shimmerTextColor The text color for the shimmer "Thinking" state
         * @param shimmerTextStyle The text style for the shimmer "Thinking" state
         * @param errorBackgroundColor The background color of the error indicator
         * @param errorTextColor The text color of the error message
         * @param errorTextStyle The text style of the error message
         * @param errorIconTint The tint color of the error icon
         * @return A new [CometChatAIAssistantBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(
            // Transparent so the outer message-bubble wrapper controls the visible color.
            backgroundColor: Color = Color.Transparent,
            textColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent,
            shimmerTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            shimmerTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            errorBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            errorTextColor: Color = CometChatTheme.colorScheme.errorColor,
            errorTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            errorIconTint: Color = CometChatTheme.colorScheme.errorColor
        ): CometChatAIAssistantBubbleStyle = CometChatAIAssistantBubbleStyle(
            backgroundColor = backgroundColor,
            textColor = textColor,
            textStyle = textStyle,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            shimmerTextColor = shimmerTextColor,
            shimmerTextStyle = shimmerTextStyle,
            errorBackgroundColor = errorBackgroundColor,
            errorTextColor = errorTextColor,
            errorTextStyle = errorTextStyle,
            errorIconTint = errorIconTint
        )
    }
}
