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
 * Style configuration for [CometChatFilesBubble] composable — the ENG-36737 multi-attachment
 * file-card stack bubble.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of the stacked file cards (translucent card overlay, file-type icon plate,
 * name/meta text, download icon), the "+N more"/"Show less" toggle, and the shared caption
 * underneath.
 *
 * Note: [cardBackgroundColor] only applies when the message carries MULTIPLE files — a single
 * file card sits directly on the bubble background (design-final, matches iOS).
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
 * CometChatFilesBubble(
 *     message = mediaMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatFilesBubbleStyle.incoming()
 * )
 * ```
 *
 * @property cardBackgroundColor The translucent overlay color of each file card (multiples only)
 * @property cardCornerRadius The corner radius of each file card
 * @property itemSpacing The vertical spacing between file cards
 * @property fileIconBackgroundColor The background color of the file-type icon plate
 * @property fileIconCornerRadius The corner radius of the file-type icon plate
 * @property fileIconSize The size of the file-type icon plate
 * @property titleTextColor The color of the file name text
 * @property titleTextStyle The text style for the file name
 * @property subtitleTextColor The color of the file size/extension text
 * @property subtitleTextStyle The text style for the file size/extension
 * @property downloadIconTint The tint color of the per-card download icon
 * @property toggleTextColor The text color of the "+N more"/"Show less" toggle
 * @property toggleTextStyle The text style for the toggle
 * @property captionTextColor The color of the caption text under the cards
 * @property captionTextStyle The text style for the caption
 */
@Immutable
data class CometChatFilesBubbleStyle(
    // Content-specific properties ONLY
    val cardBackgroundColor: Color,
    val cardCornerRadius: Dp,
    val itemSpacing: Dp,
    val fileIconBackgroundColor: Color,
    val fileIconCornerRadius: Dp,
    val fileIconSize: Dp,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val downloadIconTint: Color,
    val toggleTextColor: Color,
    val toggleTextStyle: TextStyle,
    val captionTextColor: Color,
    val captionTextStyle: TextStyle,
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
         * Creates a default files bubble style using CometChat theme tokens.
         *
         * @param cardBackgroundColor The translucent overlay color of each file card
         * @param cardCornerRadius The corner radius of each file card
         * @param itemSpacing The vertical spacing between file cards
         * @param fileIconBackgroundColor The background color of the file-type icon plate
         * @param fileIconCornerRadius The corner radius of the file-type icon plate
         * @param fileIconSize The size of the file-type icon plate
         * @param titleTextColor The color of the file name text
         * @param titleTextStyle The text style for the file name
         * @param subtitleTextColor The color of the file size/extension text
         * @param subtitleTextStyle The text style for the file size/extension
         * @param downloadIconTint The tint color of the per-card download icon
         * @param toggleTextColor The text color of the "+N more"/"Show less" toggle
         * @param toggleTextStyle The text style for the toggle
         * @param captionTextColor The color of the caption text under the cards
         * @param captionTextStyle The text style for the caption
         * @return A new [CometChatFilesBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            cardBackgroundColor: Color = Color.Black.copy(alpha = 0.04f),
            cardCornerRadius: Dp = 12.dp,
            itemSpacing: Dp = 4.dp,
            // DS spec: 40dp white rounded (r12) plate with a ~26dp file-type glyph.
            fileIconBackgroundColor: Color = Color.White,
            fileIconCornerRadius: Dp = 12.dp,
            fileIconSize: Dp = 40.dp,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption2Regular,
            downloadIconTint: Color = CometChatTheme.colorScheme.primary,
            toggleTextColor: Color = CometChatTheme.colorScheme.primary,
            toggleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            captionTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            captionTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
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
        ): CometChatFilesBubbleStyle = CometChatFilesBubbleStyle(
            cardBackgroundColor = cardBackgroundColor,
            cardCornerRadius = cardCornerRadius,
            itemSpacing = itemSpacing,
            fileIconBackgroundColor = fileIconBackgroundColor,
            fileIconCornerRadius = fileIconCornerRadius,
            fileIconSize = fileIconSize,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            downloadIconTint = downloadIconTint,
            toggleTextColor = toggleTextColor,
            toggleTextStyle = toggleTextStyle,
            captionTextColor = captionTextColor,
            captionTextStyle = captionTextStyle,
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
         * Creates a style for incoming (left-aligned) multi-file messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages — a subtle
         * dark card tint on the light incoming bubble.
         *
         * @return A new [CometChatFilesBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatFilesBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) multi-file messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use a white-tint card overlay and white text for
         * contrast on the tinted outgoing bubble background.
         *
         * @return A new [CometChatFilesBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatFilesBubbleStyle = default(
            cardBackgroundColor = Color.White.copy(alpha = 0.16f),
            titleTextColor = Color.White,
            subtitleTextColor = Color.White.copy(alpha = 0.8f),
            downloadIconTint = Color.White,
            toggleTextColor = Color.White,
            captionTextColor = Color.White.copy(alpha = 0.8f)
        )
    }
}
