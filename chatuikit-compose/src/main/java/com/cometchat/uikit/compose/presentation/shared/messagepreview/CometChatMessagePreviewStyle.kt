package com.cometchat.uikit.compose.presentation.shared.messagepreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for [CometChatMessagePreview] composable.
 *
 * This immutable data class defines the visual appearance of the message preview
 * component used to display quoted/replied-to messages within message bubbles.
 *
 * Use the companion object's factory functions to create instances:
 * - [default] for a neutral style
 * - [incoming] for incoming (left-aligned) message previews
 * - [outgoing] for outgoing (right-aligned) message previews
 *
 * @property backgroundColor The background color of the preview container
 * @property strokeWidth The stroke width of the preview border
 * @property cornerRadius The corner radius of the preview container
 * @property strokeColor The stroke color of the preview border
 * @property padding The internal padding of the preview container
 * @property separatorColor The color of the vertical separator bar
 * @property titleTextColor The color of the sender name text
 * @property titleTextStyle The text style for the sender name
 * @property subtitleTextColor The color of the message content preview text
 * @property subtitleTextStyle The text style for the message content preview
 * @property closeIconTint The tint color of the close/dismiss icon
 * @property messageIconTint The tint color of the message type icon
 */
@Immutable
data class CometChatMessagePreviewStyle(
    val backgroundColor: Color,
    val strokeWidth: Dp,
    val cornerRadius: Dp,
    val strokeColor: Color,
    val padding: Dp,
    val separatorColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val closeIconTint: Color,
    val messageIconTint: Color
) {
    companion object {
        /**
         * Creates a default message preview style using CometChat theme tokens.
         *
         * @param backgroundColor The background color of the preview container
         * @param strokeWidth The stroke width of the preview border
         * @param cornerRadius The corner radius of the preview container
         * @param strokeColor The stroke color of the preview border
         * @param padding The internal padding of the preview container
         * @param separatorColor The color of the vertical separator bar
         * @param titleTextColor The color of the sender name text
         * @param titleTextStyle The text style for the sender name
         * @param subtitleTextColor The color of the message content preview text
         * @param subtitleTextStyle The text style for the message content preview
         * @param closeIconTint The tint color of the close/dismiss icon
         * @param messageIconTint The tint color of the message type icon
         * @return A new [CometChatMessagePreviewStyle] instance with default values
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            strokeWidth: Dp = 0.dp,
            cornerRadius: Dp = 0.dp,
            strokeColor: Color = CometChatTheme.colorScheme.borderColorDefault,
            padding: Dp = 8.dp,
            separatorColor: Color = CometChatTheme.colorScheme.borderColorDefault,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            closeIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            messageIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary
        ): CometChatMessagePreviewStyle = CometChatMessagePreviewStyle(
            backgroundColor = backgroundColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            strokeColor = strokeColor,
            padding = padding,
            separatorColor = separatorColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            closeIconTint = closeIconTint,
            messageIconTint = messageIconTint
        )

        /**
         * Creates a style for incoming (left-aligned) message previews.
         *
         * Uses a darker neutral background (neutralColor400) to differentiate from
         * the incoming message bubble background, with primary color separator and
         * highlighted title text.
         *
         * @return A new [CometChatMessagePreviewStyle] configured for incoming messages
         */
        @Composable
        fun incoming(
            backgroundColor: Color = CometChatTheme.colorScheme.neutralColor400,
            strokeWidth: Dp = 0.dp,
            cornerRadius: Dp = 8.dp,
            strokeColor: Color = Color.Transparent,
            padding: Dp = 8.dp,
            separatorColor: Color = CometChatTheme.colorScheme.primary,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            titleTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            closeIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            messageIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary
        ): CometChatMessagePreviewStyle = CometChatMessagePreviewStyle(
            backgroundColor = backgroundColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            strokeColor = strokeColor,
            padding = padding,
            separatorColor = separatorColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            closeIconTint = closeIconTint,
            messageIconTint = messageIconTint
        )

        /**
         * Creates a style for outgoing (right-aligned) message previews.
         *
         * Uses extended primary color background with white text and icons
         * for contrast on the primary-colored outgoing bubble.
         *
         * @return A new [CometChatMessagePreviewStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(
            backgroundColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor800,
            strokeWidth: Dp = 0.dp,
            cornerRadius: Dp = 8.dp,
            strokeColor: Color = Color.Transparent,
            padding: Dp = 8.dp,
            separatorColor: Color = Color.White,
            titleTextColor: Color = Color.White,
            titleTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            subtitleTextColor: Color = Color.White,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            closeIconTint: Color = Color.White,
            messageIconTint: Color = Color.White
        ): CometChatMessagePreviewStyle = CometChatMessagePreviewStyle(
            backgroundColor = backgroundColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            strokeColor = strokeColor,
            padding = padding,
            separatorColor = separatorColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            closeIconTint = closeIconTint,
            messageIconTint = messageIconTint
        )
    }
}
