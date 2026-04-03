package com.cometchat.uikit.compose.presentation.shared.mediarecorder

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme
import kotlin.math.sin
import kotlin.random.Random

/**
 * @deprecated This component is deprecated and will be removed in a future release.
 * Use [com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui.CometChatAudioVisualizer] instead,
 * which provides improved audio visualization with proper amplitude history and soft transitions.
 *
 * AudioWaveformView displays frequency bars that respond to audio amplitude.
 * Bars are centered vertically and expand equally upward and downward from the center.
 * Bar heights are determined by the current audio amplitude level.
 *
 * Features:
 * - Configurable bar count, width, spacing, and colors
 * - Bars respond to audio amplitude (0.0 to 1.0)
 * - Animated bar heights based on amplitude changes
 * - Support for paused state (static bars)
 * - Bars are centered vertically, expanding equally up and down from the middle
 *
 * @param modifier Modifier for the waveform view
 * @param amplitude Current audio amplitude (0.0 to 1.0)
 * @param barCount Number of bars to display
 * @param barColor Color of the bars
 * @param barWidth Width of each bar
 * @param barSpacing Spacing between bars
 * @param maxBarHeight Maximum total height of bars (expands equally up and down from center)
 * @param minBarHeight Minimum visible height of bars
 * @param isAnimating Whether the waveform should animate (false for paused state)
 */
@Deprecated(
    message = "Use CometChatAudioVisualizer from com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui instead",
    replaceWith = ReplaceWith(
        "CometChatAudioVisualizer(modifier, amplitude, style, isAnimating)",
        "com.cometchat.uikit.compose.presentation.shared.mediarecorder.ui.CometChatAudioVisualizer"
    )
)
@Composable
fun AudioWaveformView(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    barCount: Int = 20,
    barColor: Color = CometChatTheme.colorScheme.primary,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 2.dp,
    maxBarHeight: Dp = 32.dp,
    minBarHeight: Dp = 4.dp,
    isAnimating: Boolean = true
) {
    // Clamp amplitude to valid range
    val clampedAmplitude = amplitude.coerceIn(0f, 1f)
    
    // Animation for wave effect
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_animation")
    val animationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    
    // Generate random offsets for each bar to create natural variation
    val barOffsets = remember(barCount) {
        List(barCount) { Random.nextFloat() * 2f * Math.PI.toFloat() }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(maxBarHeight)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2
        
        val barWidthPx = barWidth.toPx()
        val barSpacingPx = barSpacing.toPx()
        val maxBarHeightPx = maxBarHeight.toPx()
        val minBarHeightPx = minBarHeight.toPx()
        
        // Calculate total width needed for all bars
        val totalBarsWidth = barCount * barWidthPx + (barCount - 1) * barSpacingPx
        
        // Start position to center the bars
        val startX = (canvasWidth - totalBarsWidth) / 2
        
        for (i in 0 until barCount) {
            // Calculate bar height based on amplitude and animation
            val barHeight = if (isAnimating && clampedAmplitude > 0) {
                // Create wave effect with amplitude modulation
                val waveValue = sin(animationPhase + barOffsets[i]).toFloat()
                val normalizedWave = (waveValue + 1f) / 2f // Normalize to 0-1
                val heightFactor = minBarHeightPx + (clampedAmplitude * normalizedWave * (maxBarHeightPx - minBarHeightPx))
                heightFactor.coerceIn(minBarHeightPx, maxBarHeightPx)
            } else {
                // Static bars when paused or no amplitude
                val staticFactor = if (clampedAmplitude > 0) {
                    // Show static bars at current amplitude level
                    minBarHeightPx + (clampedAmplitude * 0.5f * (maxBarHeightPx - minBarHeightPx))
                } else {
                    // Minimum height when no amplitude
                    minBarHeightPx
                }
                staticFactor.coerceIn(minBarHeightPx, maxBarHeightPx)
            }
            
            // Calculate bar position (centered vertically)
            val x = startX + i * (barWidthPx + barSpacingPx)
            val halfHeight = barHeight / 2
            val topY = centerY - halfHeight
            
            // Draw the bar with rounded corners
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, topY),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(barWidthPx / 2, barWidthPx / 2)
            )
        }
    }
}
