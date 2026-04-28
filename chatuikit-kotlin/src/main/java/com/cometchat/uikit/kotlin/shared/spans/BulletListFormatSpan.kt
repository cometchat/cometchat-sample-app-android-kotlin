package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Span for bullet list items in WYSIWYG rich text editing.
 *
 * Extends [LeadingMarginSpan.Standard] to provide visual bullet point rendering
 * with appropriate indentation. Implements [RichTextFormatSpan] to enable format
 * detection and markdown conversion.
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.BULLET_LIST
 */
class BulletListFormatSpan : LeadingMarginSpan.Standard, RichTextFormatSpan {

    @ColorInt
    private var bulletColor: Int
    private var bulletRadius: Int
    private var gapWidth: Int
    private val context: Context?

    /**
     * Creates a new BulletListFormatSpan with default styling.
     */
    constructor() : super(DEFAULT_LEADING_MARGIN) {
        this.bulletColor = 0
        this.bulletRadius = DEFAULT_BULLET_RADIUS
        this.gapWidth = DEFAULT_GAP_WIDTH
        this.context = null
    }

    /**
     * Creates a new BulletListFormatSpan with context for theme colors.
     */
    constructor(context: Context) : super(DEFAULT_LEADING_MARGIN) {
        this.context = context
        this.bulletColor = CometChatTheme.getTextColorPrimary(context)
        this.bulletRadius = DEFAULT_BULLET_RADIUS
        this.gapWidth = DEFAULT_GAP_WIDTH
    }

    /**
     * Creates a new BulletListFormatSpan with custom styling.
     */
    constructor(
        @ColorInt bulletColor: Int,
        bulletRadius: Int,
        gapWidth: Int
    ) : super(bulletRadius * 2 + gapWidth) {
        this.bulletColor = bulletColor
        this.bulletRadius = bulletRadius
        this.gapWidth = gapWidth
        this.context = null
    }

    /**
     * Creates a new BulletListFormatSpan with custom leading margin.
     */
    constructor(leadingMargin: Int) : super(leadingMargin) {
        this.bulletColor = 0
        this.bulletRadius = DEFAULT_BULLET_RADIUS
        this.gapWidth = DEFAULT_GAP_WIDTH
        this.context = null
    }

    override fun getFormatType(): RichTextFormat = RichTextFormat.BULLET_LIST

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint,
        x: Int, dir: Int, top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout?
    ) {
        if (first) {
            val originalStyle = paint.style
            val originalColor = paint.color

            paint.style = Paint.Style.FILL
            val effectiveColor = getEffectiveBulletColor()
            if (effectiveColor != 0) {
                paint.color = effectiveColor
            }

            val bulletCenterY = (top + bottom) / 2f
            val bulletCenterX = if (dir > 0) {
                x + gapWidth.toFloat()
            } else {
                x - gapWidth.toFloat()
            }

            canvas.drawCircle(bulletCenterX, bulletCenterY, bulletRadius.toFloat(), paint)

            paint.style = originalStyle
            paint.color = originalColor
        }
    }

    @ColorInt
    private fun getEffectiveBulletColor(): Int {
        if (bulletColor != 0) return bulletColor
        if (context != null) return CometChatTheme.getTextColorPrimary(context)
        return 0
    }

    // region Getters and Setters

    fun getBulletColor(): Int = bulletColor
    fun setBulletColor(@ColorInt color: Int) { bulletColor = color }

    fun getBulletRadius(): Int = bulletRadius
    fun setBulletRadius(radius: Int) { bulletRadius = radius }

    fun getGapWidth(): Int = gapWidth
    fun setGapWidth(width: Int) { gapWidth = width }

    // endregion

    companion object {
        private const val DEFAULT_BULLET_RADIUS = 4
        private const val DEFAULT_GAP_WIDTH = 16
        private const val DEFAULT_LEADING_MARGIN = 48
    }
}
