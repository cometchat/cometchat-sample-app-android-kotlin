package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AudioWaveformBars(
    amplitudes: List<Float>,
    progress: Float,
    playedColor: Color,
    unplayedColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 24.dp
) {
    Canvas(
        modifier = modifier.fillMaxWidth().height(height)
            .semantics { contentDescription = "Audio waveform" }
    ) {
        if (amplitudes.isEmpty()) return@Canvas
        val barCount = amplitudes.size
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidthPx = 2.5.dp.toPx()
        val totalBarsWidth = barCount * barWidthPx
        val totalSpacing = canvasWidth - totalBarsWidth
        val spacingPx = if (barCount > 1) totalSpacing / (barCount - 1) else 0f
        val minBarHeight = canvasHeight * 0.06f
        val maxBarHeight = canvasHeight * 0.95f
        val cornerRadius = 1.5.dp.toPx()
        val playedBarCount = kotlin.math.ceil(progress.coerceIn(0f, 1f) * barCount).toInt()

        var ampMin = 1f; var ampMax = 0f
        for (a in amplitudes) { val c = a.coerceIn(0.15f, 1.0f); if (c < ampMin) ampMin = c; if (c > ampMax) ampMax = c }
        val ampRange = (ampMax - ampMin).coerceAtLeast(0.01f)

        for (i in 0 until barCount) {
            val raw = amplitudes[i].coerceIn(0.15f, 1.0f)
            val stretched = ((raw - ampMin) / ampRange).coerceIn(0f, 1f)
            val barHeight = minBarHeight + (stretched * (maxBarHeight - minBarHeight))
            val top = (canvasHeight - barHeight) / 2f
            val left = i * (barWidthPx + spacingPx)
            val color = if (i < playedBarCount) playedColor else unplayedColor
            drawRoundRect(color = color, topLeft = Offset(left, top), size = Size(barWidthPx, barHeight), cornerRadius = CornerRadius(cornerRadius, cornerRadius))
        }
    }
}
