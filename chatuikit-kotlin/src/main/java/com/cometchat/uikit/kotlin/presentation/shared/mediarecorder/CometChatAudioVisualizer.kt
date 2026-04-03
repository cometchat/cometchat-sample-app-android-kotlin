package com.cometchat.uikit.kotlin.presentation.shared.mediarecorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * CometChatAudioVisualizer displays real-time audio amplitude as animated vertical bars.
 * Bars are centered vertically and expand equally upward and downward from the center.
 *
 * Features:
 * - Configurable bar count, width, spacing, and colors via style properties
 * - Soft transition algorithm for smooth bar height changes using [AmplitudeHistory]
 * - Bars respond to amplitude values (0.0 to 1.0)
 * - Support for static display when not animating
 * - Bars are centered vertically, expanding equally up and down from the middle
 * - Progress-based coloring for playback visualization (active vs inactive bars)
 *
 * Matches Figma design specifications:
 * - Bar width: 2dp
 * - Bar spacing: ~2.694dp (rounded to 2.7dp)
 * - Bar heights: 2dp, 4dp, 6dp, 8dp, 10dp, 14dp, 16dp
 * - Bar color: #6852D6 (primary/highlight)
 * - Corner radius: 1000dp (fully rounded)
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
 * **Validates: Requirements 2.1, 2.3, 2.4, 2.5, 2.6, 2.7, 10.2**
 *
 * Usage:
 * ```kotlin
 * val visualizer = CometChatAudioVisualizer(context)
 * visualizer.setAmplitude(0.5f) // Set amplitude from 0.0 to 1.0
 * visualizer.setAnimating(true) // Enable animation
 * visualizer.setProgress(0.5f) // Set playback progress for active/inactive coloring
 * ```
 */
class CometChatAudioVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint for drawing bars
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Reusable RectF for drawing rounded rectangles
    private val rectF = RectF()

    // Style properties (legacy)
    @ColorInt
    private var chunkColor: Int = CometChatTheme.getPrimaryColor(context)

    // New Figma-matching style properties
    @ColorInt
    private var barColor: Int = adjustAlpha(CometChatTheme.getPrimaryColor(context), 0.2f)
    
    @ColorInt
    private var activeBarColor: Int = CometChatTheme.getPrimaryColor(context)

    @Dimension
    private var chunkWidth: Float = context.resources.getDimension(R.dimen.cometchat_2dp)

    @Dimension
    private var chunkSpacing: Float = 2.7f * context.resources.displayMetrics.density // ~2.7dp as per Figma

    @Dimension
    private var chunkMinHeight: Float = context.resources.getDimension(R.dimen.cometchat_2dp)

    @Dimension
    private var chunkMaxHeight: Float = context.resources.getDimension(R.dimen.cometchat_16dp)

    @Dimension
    private var chunkCornerRadius: Float = 1000f * context.resources.displayMetrics.density // Fully rounded as per Figma

    private var chunkCount: Int = 45 // Matching Figma waveform bar count

    // Animation state
    private var amplitude: Float = 0f
    private var isAnimating: Boolean = true
    
    // Playback progress (0.0 to 1.0) for active/inactive bar coloring
    private var progress: Float = 0f

    // Amplitude history for soft transitions
    private var amplitudeHistory: AmplitudeHistory

    init {
        amplitudeHistory = AmplitudeHistory(chunkCount)
        paint.color = chunkColor

        // Set content description for accessibility
        contentDescription = context.getString(R.string.cometchat_audio_visualizer_description)
    }
    
    /**
     * Adjusts the alpha of a color.
     */
    private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = (android.graphics.Color.alpha(color) * factor).toInt()
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha, red, green, blue)
    }

    /**
     * Sets the current audio amplitude.
     * Values are clamped to [0.0, 1.0] range.
     *
     * @param amplitude The amplitude value (0.0 to 1.0)
     */
    fun setAmplitude(amplitude: Float) {
        this.amplitude = amplitude.coerceIn(0f, 1f)
        if (isAnimating) {
            amplitudeHistory.add(this.amplitude)
            invalidate()
        }
    }

    /**
     * Gets the current amplitude value.
     *
     * @return The current amplitude (0.0 to 1.0)
     */
    fun getAmplitude(): Float = amplitude

    /**
     * Sets whether the visualizer should animate.
     * When false, bars display at a static height using the Figma waveform pattern.
     *
     * @param animating True to enable animation, false for static display
     */
    fun setAnimating(animating: Boolean) {
        if (this.isAnimating != animating) {
            this.isAnimating = animating
            if (!animating) {
                amplitudeHistory.clear()
            }
            invalidate()
        }
    }

    /**
     * Gets whether the visualizer is currently animating.
     *
     * @return True if animating, false otherwise
     */
    fun isAnimating(): Boolean = isAnimating
    
    /**
     * Sets the playback progress for active/inactive bar coloring.
     * Bars before the progress position are shown in activeBarColor,
     * bars after are shown in barColor (dimmed).
     *
     * @param progress The playback progress (0.0 to 1.0)
     */
    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 1f)
        invalidate()
    }
    
    /**
     * Gets the current playback progress.
     *
     * @return The current progress (0.0 to 1.0)
     */
    fun getProgress(): Float = progress

    /**
     * Sets the color for the visualizer bars (legacy method).
     * This sets the activeBarColor and derives barColor with 20% opacity.
     *
     * @param color The color to use for bars
     */
    fun setChunkColor(@ColorInt color: Int) {
        chunkColor = color
        activeBarColor = color
        barColor = adjustAlpha(color, 0.2f)
        paint.color = color
        invalidate()
    }

    /**
     * Gets the current chunk color.
     *
     * @return The current bar color
     */
    @ColorInt
    fun getChunkColor(): Int = chunkColor
    
    /**
     * Sets the color for inactive bars (bars after progress position).
     *
     * @param color The color for inactive bars
     */
    fun setBarColor(@ColorInt color: Int) {
        barColor = color
        invalidate()
    }
    
    /**
     * Gets the current inactive bar color.
     *
     * @return The inactive bar color
     */
    @ColorInt
    fun getBarColor(): Int = barColor
    
    /**
     * Sets the color for active bars (bars before progress position).
     *
     * @param color The color for active bars
     */
    fun setActiveBarColor(@ColorInt color: Int) {
        activeBarColor = color
        invalidate()
    }
    
    /**
     * Gets the current active bar color.
     *
     * @return The active bar color
     */
    @ColorInt
    fun getActiveBarColor(): Int = activeBarColor

    /**
     * Sets the width of each visualizer bar.
     *
     * @param width The bar width in pixels
     */
    fun setChunkWidth(@Dimension width: Float) {
        chunkWidth = width
        invalidate()
    }

    /**
     * Gets the current chunk width.
     *
     * @return The bar width in pixels
     */
    @Dimension
    fun getChunkWidth(): Float = chunkWidth

    /**
     * Sets the spacing between visualizer bars.
     *
     * @param spacing The spacing in pixels
     */
    fun setChunkSpacing(@Dimension spacing: Float) {
        chunkSpacing = spacing
        invalidate()
    }

    /**
     * Gets the current chunk spacing.
     *
     * @return The spacing in pixels
     */
    @Dimension
    fun getChunkSpacing(): Float = chunkSpacing

    /**
     * Sets the minimum height for visualizer bars.
     *
     * @param height The minimum height in pixels
     */
    fun setChunkMinHeight(@Dimension height: Float) {
        chunkMinHeight = height
        invalidate()
    }

    /**
     * Gets the current minimum chunk height.
     *
     * @return The minimum height in pixels
     */
    @Dimension
    fun getChunkMinHeight(): Float = chunkMinHeight

    /**
     * Sets the maximum height for visualizer bars.
     *
     * @param height The maximum height in pixels
     */
    fun setChunkMaxHeight(@Dimension height: Float) {
        chunkMaxHeight = height
        invalidate()
    }

    /**
     * Gets the current maximum chunk height.
     *
     * @return The maximum height in pixels
     */
    @Dimension
    fun getChunkMaxHeight(): Float = chunkMaxHeight

    /**
     * Sets the corner radius for visualizer bars.
     *
     * @param radius The corner radius in pixels
     */
    fun setChunkCornerRadius(@Dimension radius: Float) {
        chunkCornerRadius = radius
        invalidate()
    }

    /**
     * Gets the current chunk corner radius.
     *
     * @return The corner radius in pixels
     */
    @Dimension
    fun getChunkCornerRadius(): Float = chunkCornerRadius

    /**
     * Sets the number of visualizer bars to display.
     *
     * @param count The number of bars
     */
    fun setChunkCount(count: Int) {
        if (count != chunkCount && count > 0) {
            chunkCount = count
            amplitudeHistory = AmplitudeHistory(count)
            invalidate()
        }
    }

    /**
     * Gets the current chunk count.
     *
     * @return The number of bars
     */
    fun getChunkCount(): Int = chunkCount

    /**
     * Applies a style to the visualizer.
     *
     * @param style The style to apply
     */
    fun setStyle(style: CometChatMediaRecorderStyle) {
        if (style.recordingChunkColor != 0) {
            chunkColor = style.recordingChunkColor
            activeBarColor = style.recordingChunkColor
            barColor = adjustAlpha(style.recordingChunkColor, 0.2f)
            paint.color = chunkColor
        }
        if (style.barColor != 0) {
            barColor = style.barColor
        }
        if (style.activeBarColor != 0) {
            activeBarColor = style.activeBarColor
        }
        if (style.chunkWidth > 0) {
            chunkWidth = style.chunkWidth.toFloat()
        }
        if (style.chunkSpacing > 0) {
            chunkSpacing = style.chunkSpacing.toFloat()
        }
        if (style.chunkMinHeight > 0) {
            chunkMinHeight = style.chunkMinHeight.toFloat()
        }
        if (style.chunkMaxHeight > 0) {
            chunkMaxHeight = style.chunkMaxHeight.toFloat()
        }
        if (style.chunkCornerRadius > 0) {
            chunkCornerRadius = style.chunkCornerRadius.toFloat()
        }
        if (style.chunkCount > 0 && style.chunkCount != chunkCount) {
            chunkCount = style.chunkCount
            amplitudeHistory = AmplitudeHistory(chunkCount)
        }
        invalidate()
    }

    /**
     * Clears the amplitude history and resets the visualizer.
     */
    fun reset() {
        amplitude = 0f
        amplitudeHistory.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()
        val centerY = canvasHeight / 2f

        // Calculate total width needed for all bars
        val totalBarsWidth = chunkCount * chunkWidth + (chunkCount - 1) * chunkSpacing

        // Start position to center the bars horizontally
        val startX = (canvasWidth - totalBarsWidth) / 2f
        
        // Calculate which bar index corresponds to the current progress
        val progressBarIndex = (progress * chunkCount).toInt()

        for (i in 0 until chunkCount) {
            // Get smoothed amplitude for this bar from history
            val barAmplitude = if (isAnimating) {
                amplitudeHistory.getSmoothedAmplitude(i)
            } else {
                // When not animating, show static bars with Figma waveform pattern
                getStaticBarAmplitude(i, chunkCount)
            }

            // Calculate bar height using the formula:
            // barHeight = minHeight + (amplitude × (maxHeight - minHeight))
            // **Validates: Requirements 2.1, 2.3, 2.6, 2.7**
            val barHeight = calculateBarHeight(
                amplitude = barAmplitude,
                minHeight = chunkMinHeight,
                maxHeight = chunkMaxHeight
            )

            // Calculate bar position (centered vertically)
            // topY = centerY - (barHeight / 2)
            // **Validates: Requirements 2.3**
            val x = startX + i * (chunkWidth + chunkSpacing)
            val halfHeight = barHeight / 2f
            val topY = centerY - halfHeight
            val bottomY = centerY + halfHeight
            
            // Determine bar color based on progress (active vs inactive)
            // Bars before progress position are active (full color)
            // Bars after progress position are inactive (dimmed)
            val currentBarColor = if (progress > 0f && i <= progressBarIndex) {
                activeBarColor
            } else {
                barColor
            }
            paint.color = currentBarColor

            // Draw the bar with rounded corners
            // **Validates: Requirements 2.5**
            rectF.set(x, topY, x + chunkWidth, bottomY)
            canvas.drawRoundRect(rectF, chunkCornerRadius, chunkCornerRadius, paint)
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
        val pattern = floatArrayOf(
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
        return if (barIndex < pattern.size) pattern[barIndex] else pattern[barIndex % pattern.size]
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (chunkCount * chunkWidth + (chunkCount - 1) * chunkSpacing).toInt() +
                paddingLeft + paddingRight
        val desiredHeight = chunkMaxHeight.toInt() + paddingTop + paddingBottom

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    companion object {
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
    }
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
