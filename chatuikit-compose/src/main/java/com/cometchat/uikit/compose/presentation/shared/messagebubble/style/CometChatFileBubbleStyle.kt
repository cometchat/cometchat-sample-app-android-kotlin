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
 * Style configuration for [CometChatFileBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of file message bubbles, including file icon styling, text styling,
 * download button styling, and multi-file layout styling.
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
 * CometChatFileBubble(
 *     message = mediaMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatFileBubbleStyle.incoming()
 * )
 * ```
 *
 * @property innerCornerRadius The corner radius for inner items in multi-file layout
 * @property itemSpacing The spacing between file items in multi-file layout
 * @property titleTextColor The color of the file name text
 * @property titleTextStyle The text style for the file name
 * @property subtitleTextColor The color of the file size/extension text
 * @property subtitleTextStyle The text style for the file size/extension
 * @property fileIconBackgroundColor The background color of the file type icon
 * @property fileIconCornerRadius The corner radius of the file type icon background
 * @property fileIconSize The size of the file type icon
 * @property downloadIconTint The tint color of the download icon
 * @property downloadAllButtonBackgroundColor The background color of the "Download All" button
 * @property downloadAllButtonTextColor The text color of the "Download All" button
 * @property downloadAllButtonTextStyle The text style for the "Download All" button
 * @property downloadAllButtonCornerRadius The corner radius of the "Download All" button
 * @property downloadAllButtonHeight The height of the "Download All" button
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
data class CometChatFileBubbleStyle(
    // Content-specific properties ONLY
    val innerCornerRadius: Dp,
    val itemSpacing: Dp,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val fileIconBackgroundColor: Color,
    val fileIconCornerRadius: Dp,
    val fileIconSize: Dp,
    val downloadIconTint: Color,
    val downloadAllButtonBackgroundColor: Color,
    val downloadAllButtonTextColor: Color,
    val downloadAllButtonTextStyle: TextStyle,
    val downloadAllButtonCornerRadius: Dp,
    val downloadAllButtonHeight: Dp,
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
         * Creates a default file bubble style using CometChat theme tokens.
         *
         * @param innerCornerRadius The corner radius for inner items in multi-file layout
         * @param itemSpacing The spacing between file items in multi-file layout
         * @param titleTextColor The color of the file name text
         * @param titleTextStyle The text style for the file name
         * @param subtitleTextColor The color of the file size/extension text
         * @param subtitleTextStyle The text style for the file size/extension
         * @param fileIconBackgroundColor The background color of the file type icon
         * @param fileIconCornerRadius The corner radius of the file type icon background
         * @param fileIconSize The size of the file type icon
         * @param downloadIconTint The tint color of the download icon
         * @param downloadAllButtonBackgroundColor The background color of the "Download All" button
         * @param downloadAllButtonTextColor The text color of the "Download All" button
         * @param downloadAllButtonTextStyle The text style for the "Download All" button
         * @param downloadAllButtonCornerRadius The corner radius of the "Download All" button
         * @param downloadAllButtonHeight The height of the "Download All" button
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
         * @return A new [CometChatFileBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            innerCornerRadius: Dp = 2.dp,
            itemSpacing: Dp = 1.dp,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption2Regular,
            fileIconBackgroundColor: Color = Color.White,
            fileIconCornerRadius: Dp = 4.dp,
            fileIconSize: Dp = 32.dp,
            downloadIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            downloadAllButtonBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            downloadAllButtonTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            downloadAllButtonTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            downloadAllButtonCornerRadius: Dp = 8.dp,
            downloadAllButtonHeight: Dp = 40.dp,
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
        ): CometChatFileBubbleStyle = CometChatFileBubbleStyle(
            innerCornerRadius = innerCornerRadius,
            itemSpacing = itemSpacing,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            fileIconBackgroundColor = fileIconBackgroundColor,
            fileIconCornerRadius = fileIconCornerRadius,
            fileIconSize = fileIconSize,
            downloadIconTint = downloadIconTint,
            downloadAllButtonBackgroundColor = downloadAllButtonBackgroundColor,
            downloadAllButtonTextColor = downloadAllButtonTextColor,
            downloadAllButtonTextStyle = downloadAllButtonTextStyle,
            downloadAllButtonCornerRadius = downloadAllButtonCornerRadius,
            downloadAllButtonHeight = downloadAllButtonHeight,
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
         * Creates a style for incoming (left-aligned) file messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages.
         *
         * @return A new [CometChatFileBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatFileBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) file messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white colors for text and icons
         * to contrast with the typically darker outgoing message bubble background.
         *
         * @return A new [CometChatFileBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatFileBubbleStyle = default(
            titleTextColor = Color.White,
            subtitleTextColor = Color.White.copy(alpha = 0.8f),
            downloadIconTint = Color.White
        )
    }
}
