package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineBackgroundSpan
import androidx.annotation.ColorInt
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Span for blockquote formatting in WYSIWYG rich text editing.
 *
 * Extends [LeadingMarginSpan.Standard] to indent text and implements
 * [LineBackgroundSpan] to draw the left stripe indicator and an optional
 * background fill.
 *
 * Drawing the stripe via [LineBackgroundSpan.drawBackground] instead of
 * [LeadingMarginSpan.drawLeadingMargin] ensures the stripe is always
 * positioned at the absolute left edge of the text area.
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.BLOCKQUOTE
 */
class BlockquoteFormatSpan : LeadingMarginSpan.Standard, RichTextFormatSpan,
    LineBackgroundSpan {

    @ColorInt
    private var stripeColor: Int

    @ColorInt
    private var backgroundColor: Int

    private var stripeWidth: Int
    private var gapWidth: Int
    private val context: Context?

    private val rectF = RectF()
    private val path = Path()
    private val radii = FloatArray(8)

    /** Fallback constructor with no theme awareness. */
    constructor() : super(DEFAULT_LEADING_MARGIN) {
        this.context = null
        this.stripeColor = DEFAULT_STRIPE_COLOR
        this.backgroundColor = 0
        this.stripeWidth = DEFAULT_STRIPE_WIDTH
        this.gapWidth = DEFAULT_GAP_WIDTH
    }

    /** Composer constructor — uses stroke dark for the stripe, no background. */
    constructor(context: Context) : super(DEFAULT_LEADING_MARGIN) {
        this.context = context
        this.stripeColor = CometChatTheme.getStrokeColorDark(context)
        this.backgroundColor = 0
        this.stripeWidth = DEFAULT_STRIPE_WIDTH
        this.gapWidth = DEFAULT_GAP_WIDTH
    }

    /**
     * Message-bubble constructor — colors differ for sender vs receiver.
     */
    constructor(context: Context, isSenderBubble: Boolean) : super(DEFAULT_LEADING_MARGIN) {
        this.context = context
        this.stripeWidth = DEFAULT_STRIPE_WIDTH
        this.gapWidth = DEFAULT_GAP_WIDTH
        if (isSenderBubble) {
            this.stripeColor = CometChatTheme.getColorWhite(context)
            this.backgroundColor = SENDER_BACKGROUND_COLOR
        } else {
            this.stripeColor = CometChatTheme.getStrokeColorHighlight(context)
            this.backgroundColor = CometChatTheme.getBackgroundColor3(context)
        }
    }

    constructor(
        @ColorInt stripeColor: Int,
        stripeWidth: Int,
        gapWidth: Int
    ) : super(stripeWidth + gapWidth) {
        this.context = null
        this.stripeColor = stripeColor
        this.backgroundColor = 0
        this.stripeWidth = stripeWidth
        this.gapWidth = gapWidth
    }

    constructor(leadingMargin: Int) : super(leadingMargin) {
        this.context = null
        this.stripeColor = DEFAULT_STRIPE_COLOR
        this.backgroundColor = 0
        this.stripeWidth = DEFAULT_STRIPE_WIDTH
        this.gapWidth = DEFAULT_GAP_WIDTH
    }

    override fun getFormatType(): RichTextFormat = RichTextFormat.BLOCKQUOTE

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint,
        x: Int, dir: Int, top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout?
    ) {
        // Intentionally empty — stripe drawing is handled by drawBackground().
    }

    override fun drawBackground(
        canvas: Canvas, paint: Paint,
        left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int, lineNumber: Int
    ) {
        if (text !is Spanned) return

        val spanStart = text.getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        if (end <= spanStart || start >= spanEnd) return

        val originalStyle = paint.style
        val originalColor = paint.color
        val originalAntiAlias = paint.isAntiAlias
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        val isFirstLine = start <= spanStart
        val isLastDrawnLine = (spanEnd in (start + 1)..end) || end >= spanEnd
        val hasTrailingNewline = isLastDrawnLine && spanEnd > 0
                && spanEnd <= text.length && text[spanEnd - 1] == '\n'

        val drawTop = if (isFirstLine) top - VERTICAL_PADDING else top.toFloat()
        val drawBottom: Float = if (hasTrailingNewline) {
            val lineHeight = bottom - top
            bottom + lineHeight + if (isLastDrawnLine) VERTICAL_PADDING else 0f
        } else {
            if (isLastDrawnLine) bottom + VERTICAL_PADDING else bottom.toFloat()
        }

        val cornerRadius = resolveCornerRadius()
        val topR = if (isFirstLine) cornerRadius else 0f
        val bottomR = if (hasTrailingNewline || isLastDrawnLine) cornerRadius else 0f

        // Draw background fill
        if (backgroundColor != 0) {
            paint.color = backgroundColor
            rectF.set(left.toFloat(), drawTop, right.toFloat(), drawBottom)
            drawRoundRectWithCorners(canvas, rectF, paint, topR, topR, bottomR, bottomR)
        }

        // Draw the stripe
        val effectiveColor = getEffectiveStripeColor()
        if (effectiveColor != 0) {
            paint.color = effectiveColor
            val stripeRight = left + stripeWidth.toFloat()

            if (backgroundColor != 0) {
                canvas.save()
                rectF.set(left.toFloat(), drawTop, right.toFloat(), drawBottom)
                path.reset()
                radii[0] = topR; radii[1] = topR
                radii[2] = topR; radii[3] = topR
                radii[4] = bottomR; radii[5] = bottomR
                radii[6] = bottomR; radii[7] = bottomR
                path.addRoundRect(rectF, radii, Path.Direction.CW)
                canvas.clipPath(path)
                canvas.drawRect(left.toFloat(), drawTop, stripeRight, drawBottom, paint)
                canvas.restore()
            } else {
                canvas.drawRect(left.toFloat(), drawTop, stripeRight, drawBottom, paint)
            }
        }

        paint.style = originalStyle
        paint.color = originalColor
        paint.isAntiAlias = originalAntiAlias
    }

    private fun drawRoundRectWithCorners(
        canvas: Canvas, rect: RectF, paint: Paint,
        topLeft: Float, topRight: Float,
        bottomRight: Float, bottomLeft: Float
    ) {
        path.reset()
        radii[0] = topLeft;  radii[1] = topLeft
        radii[2] = topRight; radii[3] = topRight
        radii[4] = bottomRight; radii[5] = bottomRight
        radii[6] = bottomLeft;  radii[7] = bottomLeft
        path.addRoundRect(rect, radii, Path.Direction.CW)
        canvas.drawPath(path, paint)
    }

    private fun resolveCornerRadius(): Float {
        if (context != null) {
            return context.resources.getDimension(R.dimen.cometchat_radius_2)
        }
        return DEFAULT_CORNER_RADIUS
    }

    @ColorInt
    private fun getEffectiveStripeColor(): Int {
        if (stripeColor != 0) return stripeColor
        if (context != null) return CometChatTheme.getStrokeColorDark(context)
        return 0
    }

    // region Getters and Setters

    fun getStripeColor(): Int = stripeColor
    fun setStripeColor(@ColorInt color: Int) { stripeColor = color }

    fun getBackgroundColor(): Int = backgroundColor
    fun setBackgroundColor(@ColorInt color: Int) { backgroundColor = color }

    fun getStripeWidth(): Int = stripeWidth
    fun setStripeWidth(width: Int) { stripeWidth = width }

    fun getGapWidth(): Int = gapWidth
    fun setGapWidth(width: Int) { gapWidth = width }

    // endregion

    companion object {
        private const val DEFAULT_STRIPE_WIDTH = 7
        private const val DEFAULT_GAP_WIDTH = 16
        private const val DEFAULT_LEADING_MARGIN = 32

        @ColorInt
        private const val SENDER_BACKGROUND_COLOR = 0x33FFFFFF

        @ColorInt
        private const val DEFAULT_STRIPE_COLOR = 0xFF888888.toInt()

        private const val DEFAULT_CORNER_RADIUS = 8f
        private const val VERTICAL_PADDING = 8f
    }
}
