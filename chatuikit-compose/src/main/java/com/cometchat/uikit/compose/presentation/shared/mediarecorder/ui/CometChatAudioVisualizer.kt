package com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cometchat.uikit.compose.presentation.shared.mediarecorder.style.CometChatAudioVisualizerStyle

/**
 * CometChatAudioVisualizer displays real-time audio amplitude as animated vertical bars.
 * Bars are centered vertically and expand equally upward and downward from the center.
 *
 * Features:
 * - Configurable bar count, width, spacing, and colors via [CometChatAudioVisualizerStyle]
 * - Soft transition algorithm for smooth bar height changes using [AmplitudeHistory]
 * - Bars respond to amplitude values (0.0 to 1.0)
 * - Support for static display when not animating
 * - Bars are centered vertically, expanding equally up and down from the middle
 * - Progress-based coloring for playback visualization (active vs inactive bars)
 *
 * Bar Height Calculation:
 * ```
 * barHeight = minHeight + (amplitude × (maxHeight - minHeight))
 * ```
 *
 * Vertical Centering:
 * ```
 * topY = centerY - (barHeight / 2)
 * ```
 *
 * **Validates: Requirements 2.1, 2.3, 2.4, 2.5, 2.6, 2.7**
 *
 * @param modifier Modifier for the visualizer
 * @param amplitude Current audio amplitude (0.0 to 1.0)
 * @param style Style configuration for the visualizer
 * @param isAnimating Whether the visualizer should animate (false for paused/static state)
 * @param progress Playback progress (0.0 to 1.0) for showing active/inactive bars
 */
@Composable
fun CometChatAudioVisualizer(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    style: CometChatAudioVisualizerStyle = CometChatAudioVisualizerStyle.default(),
    isAnimating: Boolean = true,
    progress: Float = 0f
) {
    // Clamp amplitude to valid range [0.0, 1.0]
    val clampedAmplitude = amplitude.coerceIn(0f, 1f)
    val clampedProgress = progress.coerceIn(0f, 1f)
    
    // Create and remember amplitude history for soft transitions
    val amplitudeHistory = remember { AmplitudeHistory(style.chunkCount) }
    
    // Update amplitude history when animating
    if (isAnimating) {
        amplitudeHistory.add(clampedAmplitude)
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(style.chunkMaxHeight)
            .semantics {
                contentDescription = "Audio visualizer showing recording amplitude"
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2
        
        val chunkWidthPx = style.chunkWidth.toPx()
        val chunkSpacingPx = style.chunkSpacing.toPx()
        val chunkMinHeightPx = style.chunkMinHeight.toPx()
        val chunkMaxHeightPx = style.chunkMaxHeight.toPx()
        val chunkCornerRadiusPx = style.chunkCornerRadius.toPx()
        
        // Calculate total width needed for all bars
        val totalBarsWidth = style.chunkCount * chunkWidthPx + (style.chunkCount - 1) * chunkSpacingPx
        
        // Start position to center the bars horizontally
        val startX = (canvasWidth - totalBarsWidth) / 2
        
        // Calculate which bar index corresponds to the current progress
        val progressBarIndex = (clampedProgress * style.chunkCount).toInt()
        
        for (i in 0 until style.chunkCount) {
            // Get smoothed amplitude for this bar from history
            val barAmplitude = if (isAnimating) {
                amplitudeHistory.getSmoothedAmplitude(i)
            } else {
                // When not animating, show static bars with predefined pattern
                getStaticBarAmplitude(i, style.chunkCount)
            }
            
            // Calculate bar height using the formula:
            // barHeight = minHeight + (amplitude × (maxHeight - minHeight))
            // **Validates: Requirements 2.1, 2.3**
            val barHeight = calculateBarHeight(
                amplitude = barAmplitude,
                minHeight = chunkMinHeightPx,
                maxHeight = chunkMaxHeightPx
            )
            
            // Calculate bar position (centered vertically)
            // topY = centerY - (barHeight / 2)
            // **Validates: Requirements 2.3**
            val x = startX + i * (chunkWidthPx + chunkSpacingPx)
            val halfHeight = barHeight / 2
            val topY = centerY - halfHeight
            
            // Determine bar color based on progress (active vs inactive)
            // Bars before progress position are active (full color)
            // Bars after progress position are inactive (dimmed)
            val barColor = if (clampedProgress > 0f && i <= progressBarIndex) {
                style.activeBarColor
            } else {
                style.barColor
            }
            
            // Draw the bar with rounded corners
            // **Validates: Requirements 2.5**
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, topY),
                size = Size(chunkWidthPx, barHeight),
                cornerRadius = CornerRadius(chunkCornerRadiusPx, chunkCornerRadiusPx)
            )
        }
    }
}

/**
 * Returns a static amplitude pattern for non-animating visualizer.
 * Creates a wave-like pattern matching the Figma design exactly.
 * Bar heights from Figma: 2, 6, 6, 8, 8, 8, 16, 14, 10, 8, 6, 6, 8, 6, 8, 8, 10, 14, 10, 8, 6, 6, 4, 2, 2, 2, 2, 2, 4, 4, 6, 6, 8, 8, 10, 10, 8, 8, 8, 8, 6, 6, 6, 6, 6
 *
 * @param barIndex The index of the bar
 * @param totalBars Total number of bars
 * @return Amplitude value for this bar (0.0 to 1.0)
 */
private fun getStaticBarAmplitude(barIndex: Int, totalBars: Int): Float {
    // Exact pattern from Figma design (heights: 2-16dp, normalized to 0-1 range)
    // Formula: (height - minHeight) / (maxHeight - minHeight) = (height - 2) / (16 - 2) = (height - 2) / 14
    val pattern = listOf(
        0.00f,  // 2dp
        0.29f,  // 6dp
        0.29f,  // 6dp
        0.43f,  // 8dp
        0.43f,  // 8dp
        0.43f,  // 8dp
        1.00f,  // 16dp
        0.86f,  // 14dp
        0.57f,  // 10dp
        0.43f,  // 8dp
        0.29f,  // 6dp
        0.29f,  // 6dp
        0.43f,  // 8dp
        0.29f,  // 6dp
        0.43f,  // 8dp
        0.43f,  // 8dp
        0.57f,  // 10dp
        0.86f,  // 14dp
        0.57f,  // 10dp
        0.43f,  // 8dp
        0.29f,  // 6dp
        0.29f,  // 6dp
        0.14f,  // 4dp
        0.00f,  // 2dp
        0.00f,  // 2dp
        0.00f,  // 2dp
        0.00f,  // 2dp
        0.00f,  // 2dp
        0.14f,  // 4dp
        0.14f,  // 4dp
        0.29f,  // 6dp
        0.29f,  // 6dp
        0.43f,  // 8dp
        0.43f,  // 8dp
        0.57f,  // 10dp
        0.57f,  // 10dp
        0.43f,  // 8dp
        0.43f,  // 8dp
        0.43f,  // 8dp
        0.43f,  // 8dp
        0.29f,  // 6dp
        0.29f,  // 6dp
        0.29f,  // 6dp
        0.29f,  // 6dp
        0.29f   // 6dp
    )
    return pattern.getOrElse(barIndex % pattern.size) { 0.29f }
}

/**
 * Calculates bar height based on amplitude using the formula:
 * barHeight = minHeight + (amplitude × (maxHeight - minHeight))
 *
 * **Validates: Requirements 2.1, 2.6, 2.7**
 *
 * @param amplitude Normalized amplitude value (0.0 to 1.0)
 * @param minHeight Minimum bar height in pixels
 * @param maxHeight Maximum bar height in pixels
 * @return Calculated bar height in pixels, clamped to [minHeight, maxHeight]
 */
fun calculateBarHeight(amplitude: Float, minHeight: Float, maxHeight: Float): Float {
    val height = minHeight + (amplitude * (maxHeight - minHeight))
    return height.coerceIn(minHeight, maxHeight)
}

/**
 * Maintains amplitude history for soft transition animation.
 * Uses a circular buffer to store recent amplitude values.
 *
 * The soft transition algorithm ensures smooth bar height changes between
 * consecutive amplitude updates by distributing amplitude values across bars.
 *
 * **Validates: Requirements 2.4**
 *
 * @param size Number of amplitude values to store (typically matches chunk count)
 */
class AmplitudeHistory(private val size: Int = 20) {
    private val history = FloatArray(size) { 0f }
    private var index = 0
    
    /**
     * Adds a new amplitude value to the history buffer.
     * Values are clamped to [0.0, 1.0] range.
     *
     * @param amplitude The amplitude value to add
     */
    fun add(amplitude: Float) {
        history[index] = amplitude.coerceIn(0f, 1f)
        index = (index + 1) % size
    }
    
    /**
     * Gets the smoothed amplitude for a specific bar index.
     * Uses the circular buffer to provide different amplitude values
     * for each bar, creating a wave-like visualization effect.
     *
     * @param barIndex The index of the bar (0 to size-1)
     * @return The smoothed amplitude value for this bar
     */
    fun getSmoothedAmplitude(barIndex: Int): Float {
        // Calculate the history index for this bar
        // This creates a wave effect where each bar shows a different
        // point in the amplitude history
        val historyIndex = (index + barIndex) % size
        return history[historyIndex]
    }
    
    /**
     * Clears all amplitude history, resetting all values to 0.
     */
    fun clear() {
        history.fill(0f)
        index = 0
    }
}
