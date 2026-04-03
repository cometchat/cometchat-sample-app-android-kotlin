package com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatInlineAudioWaveform.
 * Contains all visual styling properties for the waveform visualization component.
 *
 * This style class follows the CometChat Compose Component Architecture Guide
 * and provides full customization of the waveform's appearance.
 *
 * Features:
 * - During Recording: New bars appear on the RIGHT and scroll LEFT
 * - During Playback: Bars progressively change from grey to purple to show progress
 * - Configurable bar count, width, spacing, min/max height, colors
 *
 * Bar Height Calculation:
 * barHeight = minHeight + (amplitude × (maxHeight - minHeight))
 *
 * Vertical Centering:
 * topY = centerY - (barHeight / 2)
 *
 * **Validates: Requirements 3.5, 10.3, 10.4, 10.5**
 *
 * @param barColor Default/inactive bar color (grey) - used for bars that haven't been played yet
 * @param recordingBarColor Bar color during recording (purple) - used for all bars while recording
 * @param playingBarColor Active bar color during playback (purple) - used for bars that have been played
 * @param barWidth Width of each waveform bar
 * @param barSpacing Spacing between adjacent bars
 * @param barMinHeight Minimum height of bars (when amplitude is 0)
 * @param barMaxHeight Maximum height of bars (when amplitude is 1)
 * @param barCornerRadius Corner radius for rounded bar ends
 * @param barCount Number of bars to display in the waveform
 */
@Immutable
data class CometChatInlineAudioWaveformStyle(
    val barColor: Color,
    val recordingBarColor: Color,
    val playingBarColor: Color,
    val barWidth: Dp,
    val barSpacing: Dp,
    val barMinHeight: Dp,
    val barMaxHeight: Dp,
    val barCornerRadius: Dp,
    val barCount: Int
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * All colors are derived from the current theme.
         *
         * Default values:
         * - barColor: neutralColor300 (grey for inactive/unplayed bars)
         * - recordingBarColor: primary (purple for recording bars)
         * - playingBarColor: primary (purple for played bars)
         * - barWidth: 2.dp
         * - barSpacing: 2.dp
         * - barMinHeight: 2.dp
         * - barMaxHeight: 16.dp
         * - barCornerRadius: 1.dp
         * - barCount: 45
         *
         * **Validates: Requirements 10.3, 10.4, 10.5** - Default values from CometChatTheme
         */
        @Composable
        fun default(
            barColor: Color = CometChatTheme.colorScheme.neutralColor300,
            recordingBarColor: Color = CometChatTheme.colorScheme.primary,
            playingBarColor: Color = CometChatTheme.colorScheme.primary,
            barWidth: Dp = 2.dp,
            barSpacing: Dp = 2.dp,
            barMinHeight: Dp = 2.dp,
            barMaxHeight: Dp = 16.dp,
            barCornerRadius: Dp = 1.dp,
            barCount: Int = 45
        ): CometChatInlineAudioWaveformStyle = CometChatInlineAudioWaveformStyle(
            barColor = barColor,
            recordingBarColor = recordingBarColor,
            playingBarColor = playingBarColor,
            barWidth = barWidth,
            barSpacing = barSpacing,
            barMinHeight = barMinHeight,
            barMaxHeight = barMaxHeight,
            barCornerRadius = barCornerRadius,
            barCount = barCount
        )
    }
}
