package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.cometchat.uikit.core.utils.WaveformUtils

/**
 * Custom View that draws 28 vertical waveform bars on a Canvas with
 * two-tone progress coloring. Bars at or below the progress threshold
 * use [playedColor], remaining bars use [unplayedColor].
 *
 * Equivalent of the Compose [AudioWaveformBars] composable.
 */
class AudioWaveformBarsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val BAR_COUNT = 28
        private const val BAR_WIDTH_DP = 2.5f
        private const val CORNER_RADIUS_DP = 1.5f
        private const val MIN_BAR_FRACTION = 0.06f
        private const val MAX_BAR_FRACTION = 0.95f
    }

    private var barHeights: List<Float> = WaveformUtils.generatePlaceholder(BAR_COUNT)
    private var progress: Float = 0f
    @ColorInt private var playedColor: Int = Color.WHITE
    @ColorInt private var unplayedColor: Int = Color.GRAY
    private var onSeekListener: ((Float) -> Unit)? = null

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barRect = RectF()
    private val density = context.resources.displayMetrics.density
    private val barWidthPx = BAR_WIDTH_DP * density
    private val cornerRadiusPx = CORNER_RADIUS_DP * density

    fun setBarHeights(heights: List<Float>) {
        barHeights = heights
        invalidate()
    }

    fun setProgress(fraction: Float) {
        progress = fraction.coerceIn(0f, 1f)
        invalidate()
    }

    fun setPlayedWaveColor(@ColorInt color: Int) {
        playedColor = color
        invalidate()
    }

    fun setUnplayedWaveColor(@ColorInt color: Int) {
        unplayedColor = color
        invalidate()
    }

    fun setOnSeekListener(listener: ((Float) -> Unit)?) {
        onSeekListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (barHeights.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val count = barHeights.size
        val totalBarsWidth = count * barWidthPx
        val totalSpacing = w - totalBarsWidth
        val spacingPx = if (count > 1) totalSpacing / (count - 1) else 0f
        if (barWidthPx <= 0f) return

        val minBarHeight = h * MIN_BAR_FRACTION
        val maxBarHeight = h * MAX_BAR_FRACTION

        // Contrast stretching — find min/max to use full visual range
        var ampMin = 1f
        var ampMax = 0f
        for (a in barHeights) {
            val c = a.coerceIn(0.15f, 1.0f)
            if (c < ampMin) ampMin = c
            if (c > ampMax) ampMax = c
        }
        val ampRange = (ampMax - ampMin).coerceAtLeast(0.01f)

        val playedBarCount = kotlin.math.ceil(progress * count.toDouble()).toInt()

        for (i in 0 until count) {
            val raw = barHeights[i].coerceIn(0.15f, 1.0f)
            val stretched = ((raw - ampMin) / ampRange).coerceIn(0f, 1f)
            val barHeight = minBarHeight + (stretched * (maxBarHeight - minBarHeight))

            val left = i * (barWidthPx + spacingPx)
            val top = (h - barHeight) / 2f
            val right = left + barWidthPx
            val bottom = top + barHeight

            barPaint.color = if (i < playedBarCount) playedColor else unplayedColor
            barRect.set(left, top, right, bottom)
            canvas.drawRoundRect(barRect, cornerRadiusPx, cornerRadiusPx, barPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (onSeekListener == null) return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            val fraction = (event.x / width.toFloat()).coerceIn(0f, 1f)
            onSeekListener?.invoke(fraction)
            return true
        }
        return true // consume all touch events when seeking is enabled
    }
}
