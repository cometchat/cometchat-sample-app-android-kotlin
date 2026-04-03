package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SeekableWaveform(
    amplitudes: List<Float>,
    progress: Float,
    playedColor: Color,
    unplayedColor: Color,
    onSeek: ((Float) -> Unit)?,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    height: Dp = 24.dp
) {
    var localProgress by remember { mutableFloatStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(progress) { if (!isDragging) localProgress = progress }

    val gestureModifier = if (enabled && onSeek != null) {
        modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val sp = (offset.x / size.width).coerceIn(0f, 1f)
                    localProgress = sp; onSeek(sp)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false; onSeek(localProgress) },
                    onHorizontalDrag = { change, _ ->
                        localProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                )
            }
    } else modifier

    AudioWaveformBars(
        amplitudes = amplitudes,
        progress = if (isDragging) localProgress else progress,
        playedColor = playedColor, unplayedColor = unplayedColor,
        modifier = gestureModifier, height = height
    )
}
