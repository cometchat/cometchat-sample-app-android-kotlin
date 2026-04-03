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
 * Style configuration for [CometChatVideoBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of video message bubbles, including video styling, play button styling,
 * caption styling, grid layout styling, and overlay styling.
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
 * CometChatVideoBubble(
 *     message = videoMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatVideoBubbleStyle.incoming()
 * )
 * ```
 *
 * @property videoCornerRadius The corner radius of video thumbnails
 * @property videoStrokeWidth The stroke width of video thumbnail borders
 * @property videoStrokeColor The stroke color of video thumbnail borders
 * @property playIconTint The tint color of the play icon
 * @property playIconBackgroundColor The background color of the play button overlay
 * @property progressIndicatorColor The color of the loading progress indicator
 * @property captionTextColor The color of caption text
 * @property captionTextStyle The text style for captions
 * @property gridSpacing The spacing between grid items
 * @property maxGridWidth The maximum width of the grid layout
 * @property moreOverlayBackgroundColor The background color of the "+N" overlay
 * @property moreOverlayTextColor The text color of the "+N" overlay
 * @property moreOverlayTextStyle The text style for the "+N" overlay
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
data class CometChatVideoBubbleStyle(
    // Content-specific properties ONLY
    val videoCornerRadius: Dp,
    val videoStrokeWidth: Dp,
    val videoStrokeColor: Color,
    val playIconTint: Color,
    val playIconBackgroundColor: Color,
    val progressIndicatorColor: Color,
    val captionTextColor: Color,
    val captionTextStyle: TextStyle,
    val gridSpacing: Dp,
    val maxGridWidth: Dp,
    val moreOverlayBackgroundColor: Color,
    val moreOverlayTextColor: Color,
    val moreOverlayTextStyle: TextStyle,
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
         * Creates a default video bubble style using CometChat theme tokens.
         *
         * @param videoCornerRadius The corner radius of video thumbnails
         * @param videoStrokeWidth The stroke width of video thumbnail borders
         * @param videoStrokeColor The stroke color of video thumbnail borders
         * @param playIconTint The tint color of the play icon
         * @param playIconBackgroundColor The background color of the play button overlay
         * @param progressIndicatorColor The color of the loading progress indicator
         * @param captionTextColor The color of caption text
         * @param captionTextStyle The text style for captions
         * @param gridSpacing The spacing between grid items
         * @param maxGridWidth The maximum width of the grid layout
         * @param moreOverlayBackgroundColor The background color of the "+N" overlay
         * @param moreOverlayTextColor The text color of the "+N" overlay
         * @param moreOverlayTextStyle The text style for the "+N" overlay
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
         * @return A new [CometChatVideoBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            // Content-specific defaults (keep theme-based values)
            videoCornerRadius: Dp = 8.dp,
            videoStrokeWidth: Dp = 0.dp,
            videoStrokeColor: Color = Color.Transparent,
            playIconTint: Color = Color.White,
            playIconBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            progressIndicatorColor: Color = CometChatTheme.colorScheme.iconTintSecondary,
            captionTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            captionTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            gridSpacing: Dp = 2.dp,
            maxGridWidth: Dp = 240.dp,
            moreOverlayBackgroundColor: Color = Color.Black.copy(alpha = 0.6f),
            moreOverlayTextColor: Color = Color.White,
            moreOverlayTextStyle: TextStyle = CometChatTheme.typography.heading2Bold,
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
        ): CometChatVideoBubbleStyle = CometChatVideoBubbleStyle(
            videoCornerRadius = videoCornerRadius,
            videoStrokeWidth = videoStrokeWidth,
            videoStrokeColor = videoStrokeColor,
            playIconTint = playIconTint,
            playIconBackgroundColor = playIconBackgroundColor,
            progressIndicatorColor = progressIndicatorColor,
            captionTextColor = captionTextColor,
            captionTextStyle = captionTextStyle,
            gridSpacing = gridSpacing,
            maxGridWidth = maxGridWidth,
            moreOverlayBackgroundColor = moreOverlayBackgroundColor,
            moreOverlayTextColor = moreOverlayTextColor,
            moreOverlayTextStyle = moreOverlayTextStyle,
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
         * Creates a style for incoming (left-aligned) video messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages.
         *
         * @return A new [CometChatVideoBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatVideoBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) video messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white caption text for contrast on
         * primary-colored backgrounds.
         *
         * @return A new [CometChatVideoBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatVideoBubbleStyle = default(
            captionTextColor = Color.White
        )
    }
}
