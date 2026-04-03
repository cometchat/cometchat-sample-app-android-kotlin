package com.cometchat.uikit.kotlin.presentation.shared.inlineaudiorecorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.cometchat.uikit.kotlin.theme.CometChatTheme

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
 * **Validates: Requirements 3.1, 3.3, 3.4, 3.5, 3.6, 3.7, 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 5.4, 14.2**
 */
class CometChatInlineAudioWaveform @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ==================== Style Properties ====================
    
    @ColorInt
    private var barColor: Int = CometChatTheme.getNeutralColor300(context)
    
    @ColorInt
    private var recordingBarColor: Int = CometChatTheme.getPrimaryColor(context)
    
    @ColorInt
    private var playingBarColor: Int = CometChatTheme.getPrimaryColor(context)
    
    private var barWidth: Float = dpToPx(2f)
    private var barSpacing: Float = dpToPx(2f)
    private var barMinHeight: Float = dpToPx(2f)
    private var barMaxHeight: Float = dpToPx(16f)
    private var barCornerRadius: Float = dpToPx(1f)
    private var barCount: Int = 45
    
    // ==================== State Properties ====================
    
    private var amplitudes: List<Float> = emptyList()
    private var progress: Float = 0f
    private var isRecording: Boolean = false
    private var isPlaying: Boolean = false
    
    // ==================== Paint Objects ====================
    
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val barRect = RectF()
    
    // ==================== Seek Callback ====================
    
    private var onSeekListener: ((Float) -> Unit)? = null
    
    // ==================== Gesture Detection ====================
    
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            handleSeek(e.x)
            return true
        }
        
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            handleSeek(e2.x)
            return true
        }
    })
    
    init {
        // Set content description for accessibility
        contentDescription = "Audio waveform"
    }
    
    // ==================== Public API ====================
    
    /**
     * Sets the amplitude values for the waveform.
     * @param amplitudes List of amplitude values (0.0 to 1.0)
     */
    fun setAmplitudes(amplitudes: List<Float>) {
        this.amplitudes = amplitudes
        invalidate()
    }
    
    /**
     * Sets the playback progress.
     * @param progress Progress value (0.0 to 1.0)
     */
    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 1f)
        invalidate()
    }
    
    /**
     * Sets whether the waveform is in recording mode.
     * @param isRecording True if recording, false otherwise
     */
    fun setRecording(isRecording: Boolean) {
        this.isRecording = isRecording
        contentDescription = if (isRecording) {
            "Audio waveform showing recording amplitude"
        } else if (isPlaying) {
            "Audio waveform showing playback progress"
        } else {
            "Audio waveform"
        }
        invalidate()
    }
    
    /**
     * Sets whether the waveform is in playing mode.
     * @param isPlaying True if playing, false otherwise
     */
    fun setPlaying(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        contentDescription = if (isRecording) {
            "Audio waveform showing recording amplitude"
        } else if (isPlaying) {
            "Audio waveform showing playback progress"
        } else {
            "Audio waveform"
        }
        invalidate()
    }
    
    /**
     * Sets the seek listener for tap/drag seeking.
     * @param listener Callback with progress value (0.0 to 1.0)
     */
    fun setOnSeekListener(listener: ((Float) -> Unit)?) {
        this.onSeekListener = listener
    }
    
    /**
     * Applies a style to the waveform.
     * @param style The style to apply
     */
    fun applyStyle(style: CometChatInlineAudioRecorderStyle) {
        barColor = style.barColor
        recordingBarColor = style.recordingBarColor
        playingBarColor = style.playingBarColor
        barWidth = style.barWidth.toFloat()
        barSpacing = style.barSpacing.toFloat()
        barMinHeight = style.barMinHeight.toFloat()
        barMaxHeight = style.barMaxHeight.toFloat()
        barCornerRadius = style.barCornerRadius.toFloat()
        barCount = style.barCount
        invalidate()
    }
    
    // ==================== Style Setters ====================
    
    fun setBarColor(@ColorInt color: Int) {
        barColor = color
        invalidate()
    }
    
    fun setRecordingBarColor(@ColorInt color: Int) {
        recordingBarColor = color
        invalidate()
    }
    
    fun setPlayingBarColor(@ColorInt color: Int) {
        playingBarColor = color
        invalidate()
    }
    
    fun setBarWidth(width: Float) {
        barWidth = width
        invalidate()
    }
    
    fun setBarSpacing(spacing: Float) {
        barSpacing = spacing
        invalidate()
    }
    
    fun setBarMinHeight(height: Float) {
        barMinHeight = height
        invalidate()
    }
    
    fun setBarMaxHeight(height: Float) {
        barMaxHeight = height
        invalidate()
    }
    
    fun setBarCornerRadius(radius: Float) {
        barCornerRadius = radius
        invalidate()
    }
    
    fun setBarCount(count: Int) {
        barCount = count
        invalidate()
    }
    
    // ==================== Touch Handling ====================
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Only handle touch if seeking is enabled (not recording)
        if (!isRecording && onSeekListener != null) {
            gestureDetector.onTouchEvent(event)
            return true
        }
        return super.onTouchEvent(event)
    }
    
    private fun handleSeek(x: Float) {
        if (width > 0 && onSeekListener != null) {
            val seekProgress = (x / width).coerceIn(0f, 1f)
            onSeekListener?.invoke(seekProgress)
        }
    }
    
    // ==================== Drawing ====================
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()
        val centerY = canvasHeight / 2
        
        // Prepare amplitudes for display
        val displayAmplitudes = prepareAmplitudesForDisplay(amplitudes, barCount)
        
        // Calculate total width needed for all bars
        val totalBarsWidth = barCount * barWidth + (barCount - 1) * barSpacing
        
        // Start position to center the bars horizontally
        val startX = (canvasWidth - totalBarsWidth) / 2
        
        // Calculate which bar index corresponds to the current progress
        val progressBarIndex = (progress * barCount).toInt()
        
        for (i in 0 until barCount) {
            // Get amplitude for this bar
            val barAmplitude = displayAmplitudes.getOrElse(i) { 0f }.coerceIn(0f, 1f)
            
            // Calculate bar height using the formula:
            // barHeight = minHeight + (amplitude × (maxHeight - minHeight))
            val barHeight = calculateBarHeight(barAmplitude, barMinHeight, barMaxHeight)
            
            // Calculate bar position (centered vertically)
            val x = startX + i * (barWidth + barSpacing)
            val halfHeight = barHeight / 2
            val topY = centerY - halfHeight
            
            // Determine bar color based on state
            val color = when {
                isRecording -> recordingBarColor
                isPlaying || progress > 0f -> {
                    if (i <= progressBarIndex) playingBarColor else barColor
                }
                else -> barColor
            }
            
            barPaint.color = color
            
            // Draw the bar with rounded corners
            barRect.set(x, topY, x + barWidth, topY + barHeight)
            canvas.drawRoundRect(barRect, barCornerRadius, barCornerRadius, barPaint)
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Prepares amplitude values for display by fitting them to the bar count.
     */
    private fun prepareAmplitudesForDisplay(amplitudes: List<Float>, barCount: Int): List<Float> {
        if (amplitudes.isEmpty()) {
            return List(barCount) { 0f }
        }
        
        if (amplitudes.size <= barCount) {
            // Pad with zeros on the left (new bars appear on right during recording)
            val padding = barCount - amplitudes.size
            return List(padding) { 0f } + amplitudes
        }
        
        // Sample evenly if we have more amplitudes than bars
        val result = mutableListOf<Float>()
        val step = amplitudes.size.toFloat() / barCount
        
        for (i in 0 until barCount) {
            val index = (i * step).toInt().coerceIn(0, amplitudes.size - 1)
            result.add(amplitudes[index])
        }
        
        return result
    }
    
    /**
     * Calculates bar height based on amplitude.
     * barHeight = minHeight + (amplitude × (maxHeight - minHeight))
     */
    private fun calculateBarHeight(amplitude: Float, minHeight: Float, maxHeight: Float): Float {
        val height = minHeight + (amplitude * (maxHeight - minHeight))
        return height.coerceIn(minHeight, maxHeight)
    }
    
    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
    
    // ==================== Measurement ====================
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = barMaxHeight.toInt() + paddingTop + paddingBottom
        
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> widthSize
            else -> widthSize
        }
        
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }
        
        setMeasuredDimension(width, height)
    }
}
