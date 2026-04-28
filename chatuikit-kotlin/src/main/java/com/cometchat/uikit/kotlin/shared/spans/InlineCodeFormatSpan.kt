package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import androidx.annotation.ColorInt
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * Span for inline code with background styling in WYSIWYG rich text editing.
 *
 * Extends [MetricAffectingSpan] to provide monospace font and custom text/background
 * colors for inline code, while allowing character-level cursor movement and normal
 * backspace behavior.
 *
 * When applied to text, this span displays the text with:
 * - A subtle background color (from CometChatTheme)
 * - Monospace font for code appearance
 * - Custom text color
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.INLINE_CODE
 */
class InlineCodeFormatSpan : MetricAffectingSpan, RichTextFormatSpan {

    @ColorInt
    private var backgroundColor: Int

    @ColorInt
    private var textColor: Int

    @ColorInt
    private var strokeColor: Int

    private var cornerRadius: Float
    private var padding: Float
    private var strokeWidth: Float
    private var backgroundAlpha: Int
    private var isSenderBubble: Boolean = false

    private val context: Context?

    /**
     * Creates a new InlineCodeFormatSpan with default styling.
     */
    constructor() {
        this.backgroundColor = DEFAULT_BACKGROUND_COLOR
        this.textColor = DEFAULT_TEXT_COLOR
        this.strokeColor = DEFAULT_STROKE_COLOR
        this.cornerRadius = DEFAULT_CORNER_RADIUS
        this.padding = DEFAULT_PADDING
        this.strokeWidth = DEFAULT_STROKE_WIDTH
        this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA
        this.context = null
    }

    /**
     * Creates a new InlineCodeFormatSpan with context for theme colors.
     */
    constructor(context: Context) {
        this.context = context
        this.backgroundColor = CometChatTheme.getBackgroundColor3(context)
        this.textColor = CometChatTheme.getPrimaryColor(context)
        this.strokeColor = CometChatTheme.getStrokeColorDefault(context)
        this.cornerRadius = DEFAULT_CORNER_RADIUS
        this.padding = DEFAULT_PADDING
        this.strokeWidth = DEFAULT_STROKE_WIDTH
        this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA
    }

    /**
     * Creates a new InlineCodeFormatSpan with custom styling.
     */
    constructor(
        @ColorInt backgroundColor: Int,
        @ColorInt textColor: Int,
        cornerRadius: Float,
        padding: Float
    ) {
        this.backgroundColor = backgroundColor
        this.textColor = textColor
        this.strokeColor = DEFAULT_STROKE_COLOR
        this.cornerRadius = cornerRadius
        this.padding = padding
        this.strokeWidth = DEFAULT_STROKE_WIDTH
        this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA
        this.context = null
    }

    override fun getFormatType(): RichTextFormat = RichTextFormat.INLINE_CODE

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.typeface = Typeface.MONOSPACE
    }

    override fun updateDrawState(tp: TextPaint) {
        tp.typeface = Typeface.MONOSPACE

        val txtColor = getEffectiveTextColor()
        if (txtColor != 0) {
            tp.color = txtColor
        }

        val bgColor = getEffectiveBackgroundColor()
        if (bgColor != 0) {
            tp.bgColor = bgColor
        }
    }

    @ColorInt
    private fun getEffectiveBackgroundColor(): Int {
        if (backgroundColor != 0) return backgroundColor
        if (context != null) return CometChatTheme.getNeutralColor200(context)
        return DEFAULT_BACKGROUND_COLOR
    }

    @ColorInt
    private fun getEffectiveTextColor(): Int {
        if (textColor != 0) return textColor
        if (context != null) return CometChatTheme.getTextColorPrimary(context)
        return DEFAULT_TEXT_COLOR
    }

    // region Getters and Setters

    fun getBackgroundColor(): Int = backgroundColor
    fun setBackgroundColor(@ColorInt color: Int) { backgroundColor = color }

    fun getTextColor(): Int = textColor
    fun setTextColor(@ColorInt color: Int) { textColor = color }

    fun getCornerRadius(): Float = cornerRadius
    fun setCornerRadius(radius: Float) { cornerRadius = radius }

    fun getStrokeColor(): Int = strokeColor
    fun setStrokeColor(@ColorInt color: Int) { strokeColor = color }

    fun getStrokeWidth(): Float = strokeWidth
    fun setStrokeWidth(width: Float) { strokeWidth = width }

    fun getPadding(): Float = padding
    fun setPadding(value: Float) { padding = value }

    fun getBackgroundAlpha(): Int = backgroundAlpha
    fun setBackgroundAlpha(alpha: Int) { backgroundAlpha = alpha }

    fun isSenderBubble(): Boolean = isSenderBubble

    fun setSenderBubble(senderBubble: Boolean) {
        this.isSenderBubble = senderBubble
        if (context != null) {
            if (senderBubble) {
                backgroundColor = CometChatTheme.getExtendedPrimaryColor700(context)
                strokeColor = CometChatTheme.getExtendedPrimaryColor600(context)
                backgroundAlpha = DEFAULT_BACKGROUND_ALPHA
            } else {
                backgroundColor = CometChatTheme.getBackgroundColor3(context)
                textColor = CometChatTheme.getPrimaryColor(context)
                val darkStroke = CometChatTheme.getStrokeColorDark(context)
                strokeColor = if (darkStroke != 0) darkStroke else DEFAULT_STROKE_COLOR
                backgroundAlpha = DEFAULT_BACKGROUND_ALPHA
            }
        }
    }

    // endregion

    companion object {
        private const val DEFAULT_BACKGROUND_COLOR = 0xFFFCEAE8.toInt()
        private const val DEFAULT_TEXT_COLOR = 0xFFC41E3A.toInt()
        private const val DEFAULT_STROKE_COLOR = 0xFFDDDDDD.toInt()
        private const val DEFAULT_CORNER_RADIUS = 4f
        private const val DEFAULT_PADDING = 8f
        private const val DEFAULT_STROKE_WIDTH = 1f
        private const val DEFAULT_BACKGROUND_ALPHA = 255
    }
}
