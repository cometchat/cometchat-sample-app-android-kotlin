package com.cometchat.uikit.compose.presentation.shared.mediarecorder.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatAudioVisualizer.
 * Contains all visual styling properties for the audio visualizer component.
 *
 * The audio visualizer displays vertical bars (chunks) that respond to audio amplitude
 * during recording. Bars are centered vertically and expand equally upward and downward
 * from the center.
 *
 * Matches Figma design specifications:
 * - Bar width: 2dp
 * - Bar spacing: ~2.694dp (rounded to 3dp)
 * - Bar heights: 2dp, 4dp, 6dp, 8dp, 10dp, 14dp, 16dp
 * - Bar color: #6852D6 (primary/highlight)
 * - Corner radius: 1000dp (fully rounded)
 *
 * **Validates: Requirements 2.5**
 *
 * @param chunkColor Color of the visualizer bars (deprecated, use barColor)
 * @param barColor Color of inactive/default bars
 * @param activeBarColor Color of active bars (during playback progress)
 * @param chunkWidth Width of each individual bar
 * @param chunkSpacing Horizontal spacing between bars
 * @param chunkMinHeight Minimum height of bars (when amplitude is zero)
 * @param chunkMaxHeight Maximum height of bars (when amplitude is at maximum)
 * @param chunkCornerRadius Corner radius for rounded bar edges
 * @param chunkCount Number of bars to display in the visualizer
 */
@Immutable
data class CometChatAudioVisualizerStyle(
    val chunkColor: Color,
    val barColor: Color,
    val activeBarColor: Color,
    val chunkWidth: Dp,
    val chunkSpacing: Dp,
    val chunkMinHeight: Dp,
    val chunkMaxHeight: Dp,
    val chunkCornerRadius: Dp,
    val chunkCount: Int
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Matches Figma design specifications for the Voice Record Element.
         *
         * Default values (matching Figma):
         * - barColor: Primary color with 20% opacity (inactive bars)
         * - activeBarColor: Primary color (active bars during playback)
         * - chunkWidth: 2.dp (as per Figma)
         * - chunkSpacing: 2.694.dp (~2.7dp in Figma)
         * - chunkMinHeight: 2.dp (as per Figma)
         * - chunkMaxHeight: 16.dp (as per Figma - max bar height is 16dp)
         * - chunkCornerRadius: 1000.dp (fully rounded as per Figma)
         * - chunkCount: 45 (matching Figma waveform bar count)
         */
        @Composable
        fun default(
            chunkColor: Color = CometChatTheme.colorScheme.primary,
            barColor: Color = CometChatTheme.colorScheme.primary.copy(alpha = 0.2f),
            activeBarColor: Color = CometChatTheme.colorScheme.primary,
            chunkWidth: Dp = 2.dp,
            chunkSpacing: Dp = 2.7.dp,
            chunkMinHeight: Dp = 2.dp,
            chunkMaxHeight: Dp = 16.dp,
            chunkCornerRadius: Dp = 1000.dp,
            chunkCount: Int = 45
        ): CometChatAudioVisualizerStyle = CometChatAudioVisualizerStyle(
            chunkColor = chunkColor,
            barColor = barColor,
            activeBarColor = activeBarColor,
            chunkWidth = chunkWidth,
            chunkSpacing = chunkSpacing,
            chunkMinHeight = chunkMinHeight,
            chunkMaxHeight = chunkMaxHeight,
            chunkCornerRadius = chunkCornerRadius,
            chunkCount = chunkCount
        )
    }
}
