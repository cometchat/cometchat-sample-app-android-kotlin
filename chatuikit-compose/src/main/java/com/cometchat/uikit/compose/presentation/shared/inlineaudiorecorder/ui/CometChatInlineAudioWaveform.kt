package com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cometchat.uikit.compose.presentation.shared.inlineaudiorecorder.style.CometChatInlineAudioWaveformStyle

/**
 * CometChatInlineAudioWaveform displays audio amplitude as animated vertical bars.
 *
 * Features:
 * - During Recording: New bars appear on the RIGHT and scroll LEFT
 * - During Playback: Bars progressively change from grey to purple to show progress
 * - Seeking: Tap or drag on waveform to seek to position
 * - Configurable bar count, width, spacing, min/max height, colors
 *
 * Bar Height Calculation:
 * barHeight = minHeight + (amplitude × (maxHeight - minHeight))
 *
 * Vertical Centering:
 * topY = centerY - (barHeight / 2)
 *
 * **Validates: Requirements 3.1, 3.3, 3.4, 3.5, 3.6, 3.7, 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6**
 *
 * @param modifier Modifier for the waveform
 * @param amplitudes List of amplitude values (0.0 to 1.0) for each bar
 * @param progress Playback progress (0.0 to 1.0) for showing active/inactive bars
 * @param isRecording Whether currently recording (bars appear on right, scroll left)
 * @param isPlaying Whether currently playing (shows progress coloring)
 * @param style Style configuration for the waveform
 * @param onSeek Callback when user taps or drags on waveform (progress 0.0 to 1.0)
 */
@Composable
fun CometChatInlineAudioWaveform(
    modifier: Modifier = Modifier,
    amplitudes: List<Float> = emptyList(),
    progress: Float = 0f,
    isRecording: Boolean = false,
    isPlaying: Boolean = false,
    style: CometChatInlineAudioWaveformStyle = CometChatInlineAudioWaveformStyle.default(),
    onSeek: ((Float) -> Unit)? = null
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    
    // Prepare amplitudes for display
    // During recording: show most recent amplitudes (new bars on right)
    // During playback: show stored amplitudes
    val displayAmplitudes = remember(amplitudes, style.barCount) {
        prepareAmplitudesForDisplay(amplitudes, style.barCount)
    }
    
    // Determine if seeking is enabled (only in COMPLETED or PLAYING states)
    val seekingEnabled = onSeek != null && !isRecording
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .then(
                if (seekingEnabled) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // Calculate seek position from tap
                                // **Validates: Requirements 5.1, 5.2, 5.5**
                                val seekProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                onSeek?.invoke(seekProgress)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                // Calculate seek position from drag
                                // **Validates: Requirements 5.3, 5.4, 5.5**
                                change.consume()
                                val seekProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                                onSeek?.invoke(seekProgress)
                            }
                        }
                } else {
                    Modifier
                }
            )
            .semantics {
                contentDescription = when {
                    isRecording -> "Audio waveform showing recording amplitude"
                    isPlaying -> "Audio waveform showing playback progress"
                    else -> "Audio waveform"
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2
        
        val barWidthPx = style.barWidth.toPx()
        val barSpacingPx = style.barSpacing.toPx()
        val barMinHeightPx = style.barMinHeight.toPx()
        val barMaxHeightPx = style.barMaxHeight.toPx()
        val barCornerRadiusPx = style.barCornerRadius.toPx()
        
        // Calculate total width needed for all bars
        val totalBarsWidth = style.barCount * barWidthPx + (style.barCount - 1) * barSpacingPx
        
        // Start position to center the bars horizontally
        val startX = (canvasWidth - totalBarsWidth) / 2
        
        // Calculate which bar index corresponds to the current progress
        // **Validates: Requirements 4.2, 4.3**
        val progressBarIndex = (clampedProgress * style.barCount).toInt()
        
        for (i in 0 until style.barCount) {
            // Get amplitude for this bar
            val barAmplitude = displayAmplitudes.getOrElse(i) { 0f }.coerceIn(0f, 1f)
            
            // Calculate bar height using the formula:
            // barHeight = minHeight + (amplitude × (maxHeight - minHeight))
            // **Validates: Requirements 3.4, 3.6, 3.7**
            val barHeight = calculateBarHeight(
                amplitude = barAmplitude,
                minHeight = barMinHeightPx,
                maxHeight = barMaxHeightPx
            )
            
            // Calculate bar position (centered vertically)
            // topY = centerY - (barHeight / 2)
            // **Validates: Requirements 3.4**
            val x = startX + i * (barWidthPx + barSpacingPx)
            val halfHeight = barHeight / 2
            val topY = centerY - halfHeight
            
            // Determine bar color based on state
            // **Validates: Requirements 3.5, 4.2, 4.3, 4.4**
            val barColor = when {
                isRecording -> {
                    // During recording: all bars use recording color (purple)
                    style.recordingBarColor
                }
                isPlaying || clampedProgress > 0f -> {
                    // During playback: bars before progress are playing color (purple)
                    // bars after progress are default color (grey)
                    if (i <= progressBarIndex) {
                        style.playingBarColor
                    } else {
                        style.barColor
                    }
                }
                else -> {
                    // Static state (COMPLETED, not playing): all bars grey
                    style.barColor
                }
            }
            
            // Draw the bar with rounded corners
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, topY),
                size = Size(barWidthPx, barHeight),
                cornerRadius = CornerRadius(barCornerRadiusPx, barCornerRadiusPx)
            )
        }
    }
}

/**
 * Prepares amplitude values for display by fitting them to the bar count.
 * 
 * During recording: Shows most recent amplitudes (new bars appear on right, scroll left)
 * During playback: Shows stored amplitudes distributed across bars
 *
 * **Validates: Requirements 3.3, 3.8, 4.1**
 *
 * @param amplitudes List of amplitude values
 * @param barCount Number of bars to display
 * @return List of amplitude values sized to barCount
 */
private fun prepareAmplitudesForDisplay(amplitudes: List<Float>, barCount: Int): List<Float> {
    if (amplitudes.isEmpty()) {
        // Return list of zeros for empty amplitudes
        return List(barCount) { 0f }
    }
    
    if (amplitudes.size <= barCount) {
        // If we have fewer amplitudes than bars, pad with zeros on the left
        // This makes new bars appear on the right during recording
        // **Validates: Requirements 3.3**
        val padding = barCount - amplitudes.size
        return List(padding) { 0f } + amplitudes
    }
    
    // If we have more amplitudes than bars, sample evenly
    // This distributes the amplitude history across all bars
    val result = mutableListOf<Float>()
    val step = amplitudes.size.toFloat() / barCount
    
    for (i in 0 until barCount) {
        val index = (i * step).toInt().coerceIn(0, amplitudes.size - 1)
        result.add(amplitudes[index])
    }
    
    return result
}

/**
 * Calculates bar height based on amplitude using the formula:
 * barHeight = minHeight + (amplitude × (maxHeight - minHeight))
 *
 * **Validates: Requirements 3.4, 3.6, 3.7**
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
 * Calculates seek position from tap/drag position.
 * 
 * seekPositionMs = (tapX / waveformWidth) × duration
 *
 * **Validates: Requirements 5.2, 5.5**
 *
 * @param tapX X position of tap/drag
 * @param waveformWidth Total width of waveform
 * @param durationMs Total duration in milliseconds
 * @return Seek position in milliseconds, clamped to [0, durationMs]
 */
fun calculateSeekPosition(tapX: Float, waveformWidth: Float, durationMs: Long): Long {
    if (waveformWidth <= 0f || durationMs <= 0L) return 0L
    val progress = (tapX / waveformWidth).coerceIn(0f, 1f)
    return (progress * durationMs).toLong().coerceIn(0L, durationMs)
}

/**
 * Calculates progress from seek position.
 *
 * @param positionMs Current position in milliseconds
 * @param durationMs Total duration in milliseconds
 * @return Progress value (0.0 to 1.0)
 */
fun calculateProgress(positionMs: Long, durationMs: Long): Float {
    if (durationMs <= 0L) return 0f
    return (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
}
