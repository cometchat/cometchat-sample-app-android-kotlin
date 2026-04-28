package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineBackgroundSpan
import android.text.style.LineHeightSpan
import androidx.annotation.ColorInt
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Span for code blocks in WYSIWYG rich text editing.
 *
 * Implements [LineBackgroundSpan] to draw a unified background behind code block text,
 * [LineHeightSpan] for vertical padding/margin, and [LeadingMarginSpan] for horizontal
 * indentation. Tracks line positions to draw rounded corners only on the first and last
 * lines, creating a continuous block appearance.
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.CODE_BLOCK
 */
class CodeBlockFormatSpan : LeadingMarginSpan, RichTextFormatSpan,
    LineBackgroundSpan, LineHeightSpan {

    @ColorInt
    private var backgroundColor: Int

    @ColorInt
    private var borderColor: Int

    private var borderWidth: Float
    private var cornerRadius: Float
    private var padding: Float
    private var horizontalPadding: Float

    private val context: Context?

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val rectF = RectF()

    /**
     * Creates a new CodeBlockFormatSpan with default styling.
     */
    constructor() {
        this.backgroundColor = DEFAULT_BACKGROUND_COLOR
        this.borderColor = DEFAULT_BORDER_COLOR
        this.borderWidth = DEFAULT_BORDER_WIDTH
        this.cornerRadius = DEFAULT_CORNER_RADIUS
        this.padding = DEFAULT_PADDING
        this.horizontalPadding = DEFAULT_HORIZONTAL_PADDING
        this.context = null
    }

    /**
     * Creates a new CodeBlockFormatSpan with context for theme colors.
     */
    constructor(context: Context) {
        this.context = context
        this.backgroundColor = CometChatTheme.getBackgroundColor2(context)
        this.borderColor = CometChatTheme.getStrokeColorDefault(context)
        this.borderWidth = DEFAULT_BORDER_WIDTH
        this.cornerRadius = DEFAULT_CORNER_RADIUS
        this.padding = DEFAULT_PADDING
        this.horizontalPadding = DEFAULT_HORIZONTAL_PADDING
    }

    /**
     * Creates a new CodeBlockFormatSpan with custom styling.
     */
    constructor(
        @ColorInt backgroundColor: Int,
        @ColorInt borderColor: Int,
        borderWidth: Float,
        cornerRadius: Float
    ) {
        this.backgroundColor = backgroundColor
        this.borderColor = borderColor
        this.borderWidth = borderWidth
        this.cornerRadius = cornerRadius
        this.padding = DEFAULT_PADDING
        this.horizontalPadding = DEFAULT_HORIZONTAL_PADDING
        this.context = null
    }

    override fun getFormatType(): RichTextFormat = RichTextFormat.CODE_BLOCK

    // region LineHeightSpan

    override fun chooseHeight(
        text: CharSequence, start: Int, end: Int,
        spanstartv: Int, lineHeight: Int,
        fm: Paint.FontMetricsInt
    ) {
        if (text is Spanned) {
            val spanStart = text.getSpanStart(this)
            val spanEnd = text.getSpanEnd(this)

            val margin = DEFAULT_VERTICAL_MARGIN.toInt()
            val internalPad = padding.toInt()

            val isFirstLine = (spanStart in start until end) || (start == 0 && spanStart == 0)
            val isLastLine = (spanEnd in (start + 1)..end) || (end >= text.length && spanEnd >= text.length)

            if (isFirstLine) {
                fm.ascent -= internalPad
                fm.top -= internalPad
                if (spanStart > 0) {
                    fm.ascent -= margin
                    fm.top -= margin
                }
            }

            if (isLastLine) {
                fm.descent += internalPad
                fm.bottom += internalPad
                if (spanEnd < text.length) {
                    fm.descent += margin
                    fm.bottom += margin
                }
            }
        }
    }

    // endregion

    // region LeadingMarginSpan

    override fun getLeadingMargin(first: Boolean): Int = horizontalPadding.toInt()

    override fun drawLeadingMargin(
        c: Canvas, p: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout?
    ) {
        // No custom drawing needed — the margin offset is sufficient
    }

    // endregion

    // region LineBackgroundSpan

    override fun drawBackground(
        canvas: Canvas, paint: Paint,
        left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int, lineNumber: Int
    ) {
        if (text !is Spanned) {
            drawCodeBlockBackground(canvas, left, right, top, bottom, isFirstLine = true, isLastLine = true)
            return
        }

        val spanStart = text.getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        if (end <= spanStart || start >= spanEnd) return

        // Offset for blockquote if present
        var adjustedLeft = left
        val blockquoteSpans = text.getSpans(start, end, BlockquoteFormatSpan::class.java)
        if (blockquoteSpans.isNotEmpty()) {
            adjustedLeft = left + blockquoteSpans[0].getLeadingMargin(true)
        }

        val isFirstLine = (spanStart in start until end) || (start == 0 && spanStart == 0)

        var isLastLine = false
        val textLen = text.length

        if (spanStart == 0 && spanEnd >= textLen && isFirstLine) isLastLine = true
        if (!isLastLine && spanEnd <= end) isLastLine = true
        if (!isLastLine && end >= textLen && spanEnd >= textLen) isLastLine = true
        if (!isLastLine && end > spanEnd) isLastLine = true

        if (!isLastLine && isFirstLine) {
            val spanContent = text.subSequence(spanStart, minOf(spanEnd, textLen)).toString()
            if (!spanContent.contains("\n")) isLastLine = true
        }
        if (!isLastLine && lineNumber == 0 && !text.toString().contains("\n")) isLastLine = true

        var adjustedBottom = bottom
        if (!isLastLine) {
            adjustedBottom = bottom + Math.ceil(padding.toDouble()).toInt()
        } else {
            if (spanEnd > 0 && spanEnd <= text.length
                && text[spanEnd - 1] == '\n' && spanEnd >= text.length
            ) {
                adjustedBottom = bottom + (bottom - top)
            }
            if (spanEnd < text.length) {
                adjustedBottom = bottom - DEFAULT_VERTICAL_MARGIN.toInt()
            }
        }

        var adjustedTop = top
        if (isFirstLine && spanStart > 0) {
            adjustedTop = top + DEFAULT_VERTICAL_MARGIN.toInt()
        }

        drawCodeBlockBackground(canvas, adjustedLeft, right, adjustedTop, adjustedBottom, isFirstLine, isLastLine)
    }

    // endregion

    private fun drawCodeBlockBackground(
        canvas: Canvas, left: Int, right: Int,
        top: Int, bottom: Int,
        isFirstLine: Boolean, isLastLine: Boolean
    ) {
        val bgColor = getBackgroundColor()
        backgroundPaint.color = bgColor

        val cursorOverflow = 4f
        val rectTop = if (isFirstLine) top - cursorOverflow else top.toFloat()
        val rectBottom = if (isLastLine) bottom + cursorOverflow else bottom.toFloat()

        rectF.set(left.toFloat(), rectTop, right.toFloat(), rectBottom)

        when {
            isFirstLine && isLastLine -> {
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint)
            }
            isFirstLine -> {
                drawRoundRectWithCorners(canvas, rectF, backgroundPaint, cornerRadius, cornerRadius, 0f, 0f)
            }
            isLastLine -> {
                drawRoundRectWithCorners(canvas, rectF, backgroundPaint, 0f, 0f, cornerRadius, cornerRadius)
            }
            else -> {
                canvas.drawRect(rectF, backgroundPaint)
            }
        }

        // Draw border
        val borderCol = getBorderColor()
        if (borderCol != 0 && borderWidth > 0) {
            borderPaint.color = borderCol
            borderPaint.strokeWidth = borderWidth

            if (isFirstLine && isLastLine) {
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)
            } else {
                val leftX = rectF.left + borderWidth / 2
                val rightX = rectF.right - borderWidth / 2

                when {
                    isFirstLine -> {
                        val topPath = Path()
                        topPath.moveTo(leftX, rectF.bottom)
                        topPath.lineTo(leftX, rectF.top + cornerRadius)
                        topPath.arcTo(rectF.left, rectF.top, rectF.left + cornerRadius * 2, rectF.top + cornerRadius * 2, 180f, 90f, false)
                        topPath.lineTo(rectF.right - cornerRadius, rectF.top)
                        topPath.arcTo(rectF.right - cornerRadius * 2, rectF.top, rectF.right, rectF.top + cornerRadius * 2, 270f, 90f, false)
                        topPath.lineTo(rightX, rectF.bottom)
                        canvas.drawPath(topPath, borderPaint)
                    }
                    isLastLine -> {
                        val bottomPath = Path()
                        bottomPath.moveTo(leftX, rectF.top)
                        bottomPath.lineTo(leftX, rectF.bottom - cornerRadius)
                        bottomPath.arcTo(rectF.left, rectF.bottom - cornerRadius * 2, rectF.left + cornerRadius * 2, rectF.bottom, 180f, -90f, false)
                        bottomPath.lineTo(rectF.right - cornerRadius, rectF.bottom)
                        bottomPath.arcTo(rectF.right - cornerRadius * 2, rectF.bottom - cornerRadius * 2, rectF.right, rectF.bottom, 90f, -90f, false)
                        bottomPath.lineTo(rightX, rectF.top)
                        canvas.drawPath(bottomPath, borderPaint)
                    }
                    else -> {
                        canvas.drawLine(leftX, rectF.top, leftX, rectF.bottom, borderPaint)
                        canvas.drawLine(rightX, rectF.top, rightX, rectF.bottom, borderPaint)
                    }
                }
            }
        }
    }

    private fun drawRoundRectWithCorners(
        canvas: Canvas, rect: RectF, paint: Paint,
        topLeft: Float, topRight: Float,
        bottomRight: Float, bottomLeft: Float
    ) {
        val path = Path()
        val radii = floatArrayOf(
            topLeft, topLeft,
            topRight, topRight,
            bottomRight, bottomRight,
            bottomLeft, bottomLeft
        )
        path.addRoundRect(rect, radii, Path.Direction.CW)
        canvas.drawPath(path, paint)
    }

    // region Getters and Setters

    @ColorInt
    fun getBackgroundColor(): Int {
        if (backgroundColor != 0) return backgroundColor
        if (context != null) return CometChatTheme.getNeutralColor200(context)
        return DEFAULT_BACKGROUND_COLOR
    }

    fun setBackgroundColor(@ColorInt color: Int) { backgroundColor = color }

    @ColorInt
    fun getBorderColor(): Int {
        if (borderColor != 0) return borderColor
        if (context != null) return CometChatTheme.getStrokeColorDefault(context)
        return DEFAULT_BORDER_COLOR
    }

    fun setBorderColor(@ColorInt color: Int) { borderColor = color }

    fun getBorderWidth(): Float = borderWidth
    fun setBorderWidth(width: Float) { borderWidth = width }

    fun getCornerRadius(): Float = cornerRadius
    fun setCornerRadius(radius: Float) { cornerRadius = radius }

    fun getPadding(): Float = padding
    fun setPadding(value: Float) { padding = value }

    fun getHorizontalPadding(): Float = horizontalPadding
    fun setHorizontalPadding(value: Float) { horizontalPadding = value }

    // endregion

    companion object {
        const val DEFAULT_CORNER_RADIUS = 16f
        const val DEFAULT_BORDER_WIDTH = 1f
        const val DEFAULT_PADDING = 12f
        const val DEFAULT_HORIZONTAL_PADDING = 24f
        const val DEFAULT_BACKGROUND_COLOR = 0xFFF8F8F8.toInt()
        const val DEFAULT_BORDER_COLOR = 0xFFDDDDDD.toInt()
        const val DEFAULT_VERTICAL_MARGIN = 16f

        fun getCodeTypeface(): Typeface = Typeface.MONOSPACE
    }
}
