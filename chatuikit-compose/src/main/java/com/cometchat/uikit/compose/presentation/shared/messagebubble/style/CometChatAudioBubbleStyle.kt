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
 * Style configuration for [CometChatAudioBubble] composable.
 *
 * This immutable data class extends [CometChatMessageBubbleStyle] and defines the visual
 * appearance of audio message bubbles, including play/pause button styling, audio wave
 * animation styling, and subtitle styling.
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
 * CometChatAudioBubble(
 *     message = audioMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatAudioBubbleStyle.incoming()
 * )
 * ```
 *
 * @property playIconTint The tint color of the play icon
 * @property pauseIconTint The tint color of the pause icon
 * @property buttonBackgroundColor The background color of the play/pause button
 * @property audioWaveColor The color of the audio wave animation
 * @property subtitleTextColor The color of subtitle text (duration/file size)
 * @property subtitleTextStyle The text style for subtitle text
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
data class CometChatAudioBubbleStyle(
    // Content-specific properties ONLY
    val playIconTint: Color,
    val pauseIconTint: Color,
    val buttonBackgroundColor: Color,
    val audioWaveColor: Color,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    // New waveform progress properties
    val playedBarColor: Color,
    val unplayedBarColor: Color,
    val durationTextColor: Color,
    val durationTextStyle: TextStyle,
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
         * Creates a default audio bubble style using CometChat theme tokens.
         *
         * @param playIconTint The tint color of the play icon
         * @param pauseIconTint The tint color of the pause icon
         * @param buttonBackgroundColor The background color of the play/pause button
         * @param audioWaveColor The color of the audio wave animation
         * @param subtitleTextColor The color of subtitle text (duration/file size)
         * @param subtitleTextStyle The text style for subtitle text
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
         * @return A new [CometChatAudioBubbleStyle] instance with default values
         */
        @Composable
        fun default(
            playIconTint: Color = CometChatTheme.colorScheme.primary,
            pauseIconTint: Color = CometChatTheme.colorScheme.primary,
            buttonBackgroundColor: Color = Color.White,
            audioWaveColor: Color = CometChatTheme.colorScheme.iconTintSecondary,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            playedBarColor: Color = CometChatTheme.colorScheme.primary,
            unplayedBarColor: Color = CometChatTheme.colorScheme.primary.copy(alpha = 0.5f),
            durationTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            durationTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
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
        ): CometChatAudioBubbleStyle = CometChatAudioBubbleStyle(
            playIconTint = playIconTint,
            pauseIconTint = pauseIconTint,
            buttonBackgroundColor = buttonBackgroundColor,
            audioWaveColor = audioWaveColor,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            playedBarColor = playedBarColor,
            unplayedBarColor = unplayedBarColor,
            durationTextColor = durationTextColor,
            durationTextStyle = durationTextStyle,
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
         * Creates a style for incoming (left-aligned) audio messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use theme defaults for incoming messages.
         *
         * @return A new [CometChatAudioBubbleStyle] configured for incoming messages
         */
        @Composable
        fun incoming(): CometChatAudioBubbleStyle = default()

        /**
         * Creates a style for outgoing (right-aligned) audio messages.
         *
         * CommonProperties use sentinel values (filled from messageBubbleStyle during merge).
         * Content-specific properties use white colors for audio wave and subtitle
         * text to contrast with the typically darker outgoing message bubble background.
         *
         * @return A new [CometChatAudioBubbleStyle] configured for outgoing messages
         */
        @Composable
        fun outgoing(): CometChatAudioBubbleStyle = default(
            playedBarColor = Color.White,
            unplayedBarColor = Color.White.copy(alpha = 0.5f),
            durationTextColor = Color.White,
            audioWaveColor = Color.White,
            subtitleTextColor = Color.White
        )
    }
}
