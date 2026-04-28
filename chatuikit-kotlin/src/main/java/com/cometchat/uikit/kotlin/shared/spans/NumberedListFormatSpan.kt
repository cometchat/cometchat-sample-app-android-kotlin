package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Span for numbered list items in WYSIWYG rich text editing.
 *
 * Extends [LeadingMarginSpan.Standard] to provide visual numbered list rendering
 * with appropriate indentation. Implements [RichTextFormatSpan] to enable format
 * detection and markdown conversion.
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.ORDERED_LIST
 */
class NumberedListFormatSpan : LeadingMarginSpan.Standard, RichTextFormatSpan {

    var number: Int
        private set

    @ColorInt
    private var textColor: Int
    private var gapWidth: Int
    private val context: Context?

    /**
     * Creates a new NumberedListFormatSpan with the specified item number.
     */
    constructor(number: Int) : super(calculateLeadingMargin(number)) {
        this.number = number
        this.textColor = 0
        this.gapWidth = DEFAULT_GAP_WIDTH
        this.context = null
    }

    /**
     * Creates a new NumberedListFormatSpan with context for theme colors.
     */
    constructor(number: Int, context: Context) : super(calculateLeadingMargin(number)) {
        this.number = number
        this.context = context
        this.textColor = CometChatTheme.getTextColorPrimary(context)
        this.gapWidth = DEFAULT_GAP_WIDTH
    }

    /**
     * Creates a new NumberedListFormatSpan with custom styling.
     */
    constructor(
        number: Int,
        @ColorInt textColor: Int,
        gapWidth: Int
    ) : super(calculateLeadingMargin(number)) {
        this.number = number
        this.textColor = textColor
        this.gapWidth = gapWidth
        this.context = null
    }

    /**
     * Creates a new NumberedListFormatSpan with custom leading margin.
     */
    constructor(number: Int, leadingMargin: Int) : super(leadingMargin) {
        this.number = number
        this.textColor = 0
        this.gapWidth = DEFAULT_GAP_WIDTH
        this.context = null
    }

    override fun getFormatType(): RichTextFormat = RichTextFormat.ORDERED_LIST

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint,
        x: Int, dir: Int, top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout?
    ) {
        if (first) {
            val originalStyle = paint.style
            val originalColor = paint.color
            val originalTypeface = paint.typeface

            paint.style = Paint.Style.FILL
            val effectiveColor = getEffectiveTextColor()
            if (effectiveColor != 0) {
                paint.color = effectiveColor
            }

            val numberText = "$number."

            val drawX = if (dir > 0) {
                x + (gapWidth / 4f)
            } else {
                val textWidth = paint.measureText(numberText)
                x - textWidth - (gapWidth / 4f)
            }

            canvas.drawText(numberText, drawX, baseline.toFloat(), paint)

            paint.style = originalStyle
            paint.color = originalColor
            paint.typeface = originalTypeface
        }
    }

    @ColorInt
    private fun getEffectiveTextColor(): Int {
        if (textColor != 0) return textColor
        if (context != null) return CometChatTheme.getTextColorPrimary(context)
        return 0
    }

    // region Getters and Setters

    fun setNumber(value: Int) { number = value }

    fun getTextColor(): Int = textColor
    fun setTextColor(@ColorInt color: Int) { textColor = color }

    fun getGapWidth(): Int = gapWidth
    fun setGapWidth(width: Int) { gapWidth = width }

    // endregion

    companion object {
        private const val DEFAULT_GAP_WIDTH = 16
        private const val DEFAULT_LEADING_MARGIN = 48
        private const val MARGIN_PER_EXTRA_DIGIT = 16

        private fun calculateLeadingMargin(number: Int): Int {
            val digits = Math.abs(number).toString().length
            val extraDigits = maxOf(0, digits - 1)
            return DEFAULT_LEADING_MARGIN + (extraDigits * MARGIN_PER_EXTRA_DIGIT)
        }
    }
}
