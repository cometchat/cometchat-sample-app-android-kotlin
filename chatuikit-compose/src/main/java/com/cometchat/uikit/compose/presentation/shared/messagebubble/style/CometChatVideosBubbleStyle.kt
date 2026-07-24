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
 * Style configuration for [CometChatVideosBubble] composable — the ENG-36737 multi-attachment
 * video grid bubble.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of the video grid (tile corners, spacing, placeholder, "+N" overflow overlay),
 * the per-tile play badge and duration chip, and the shared caption underneath.
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
 * CometChatVideosBubble(
 *     message = mediaMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatVideosBubbleStyle.incoming()
 * )
 * ```
 *
 * @property tileCornerRadius The corner radius of each grid tile
 * @property gridSpacing The spacing between grid tiles
 * @property tilePlaceholderColor The background color shown while a tile's thumbnail loads
 * @property moreOverlayBackgroundColor The background color of the "+N" overflow overlay
 * @property moreOverlayTextColor The text color of the "+N" overflow overlay
 * @property moreOverlayTextStyle The text style for the "+N" overflow overlay
 * @property playBadgeBackgroundColor The background color of the centered play badge
 * @property playIconTint The tint color of the play glyph inside the badge
 * @property durationChipBackgroundColor The background color of the duration chip
 * @property durationTextColor The text color of the duration chip
 * @property durationTextStyle The text style for the duration chip
 * @property showVideoDuration Whether the duration chip is shown on video tiles
 * @property captionTextColor The color of the caption text under the grid
 * @property captionTextStyle The text style for the caption
 */
@Immutable
data class CometChatVideosBubbleStyle(
    // Content-specific properties ONLY
    val tileCornerRadius: Dp,
    val gridSpacing: Dp,
    val tilePlaceholderColor: Color,
    val moreOverlayBackgroundColor: Color,
    val moreOverlayTextColor: Color,
    val moreOverlayTextStyle: TextStyle,
    val playBadgeBackgroundColor: Color,
    val playIconTint: Color,
    val durationChipBackgroundColor: Color,
    val durationTextColor: Color,
    val durationTextStyle: TextStyle,
    val showVideoDuration: Boolean,
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
         * Creates a default videos bubble style using CometChat theme tokens.
         *
         * @param tileCornerRadius The corner radius of each grid tile
         * @param gridSpacing The spacing between grid tiles
         * @param tilePlaceholderColor The background color shown while a tile's thumbnail loads
         * @param moreOverlayBackgroundColor The background color of the "+N" overflow overlay
         * @param moreOverlayTextColor The text color of the "+N" overflow overlay
         * @param moreOverlayTextStyle The text style for the "+N" overflow overlay
         * @param playBadgeBackgroundColor The background color of the centered play badge
         * @param playIconTint The tint color of the play glyph inside the badge
         * @param durationChipBackgroundColor The background color of the duration chip
         * @param durationTextColor The text color of the duration chip
         * @param durationTextStyle The text style for the duration chip
         * @param showVideoDuration Whether the duration chip is shown on video tiles
         * @param captionTextColor The color of the caption text under the grid
         * @param captionTextStyle The text style for the caption
         * @return A new [CometChatVideosBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            tileCornerRadius: Dp = 8.dp,
            gridSpacing: Dp = 2.dp,
            tilePlaceholderColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            moreOverlayBackgroundColor: Color = Color.Black.copy(alpha = 0.6f),
            moreOverlayTextColor: Color = Color.White,
            moreOverlayTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            playBadgeBackgroundColor: Color = Color.Black.copy(alpha = 0.45f),
            playIconTint: Color = Color.White,
            durationChipBackgroundColor: Color = Color.Black.copy(alpha = 0.55f),
            durationTextColor: Color = Color.White,
            durationTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            showVideoDuration: Boolean = true,
            captionTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
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
        ): CometChatVideosBubbleStyle = CometChatVideosBubbleStyle(
            tileCornerRadius = tileCornerRadius,
            gridSpacing = gridSpacing,
            tilePlaceholderColor = tilePlaceholderColor,
            moreOverlayBackgroundColor = moreOverlayBackgroundColor,
            moreOverlayTextColor = moreOverlayTextColor,
            moreOverlayTextStyle = moreOverlayTextStyle,
            playBadgeBackgroundColor = playBadgeBackgroundColor,
            playIconTint = playIconTint,
            durationChipBackgroundColor = durationChipBackgroundColor,
            durationTextColor = durationTextColor,
            durationTextStyle = durationTextStyle,
            showVideoDuration = showVideoDuration,
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
         * Creates a style for incoming (left-aligned) multi-video messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages.
         *
         * @return A new [CometChatVideosBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatVideosBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) multi-video messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white caption text for contrast on
         * primary-colored backgrounds.
         *
         * @return A new [CometChatVideosBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatVideosBubbleStyle = default(
            captionTextColor = Color.White
        )
    }
}
