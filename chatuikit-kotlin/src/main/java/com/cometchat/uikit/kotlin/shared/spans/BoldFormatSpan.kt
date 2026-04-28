package com.cometchat.uikit.kotlin.shared.spans

import android.graphics.Typeface
import android.os.Build
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Span for bold formatting in WYSIWYG rich text editing.
 *
 * Uses font weight 700 (API 28+) or [Typeface.BOLD] fallback to render text
 * in bold style. Implements [RichTextFormatSpan] to enable format detection
 * and markdown conversion.
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.BOLD
 */
class BoldFormatSpan : MetricAffectingSpan(), RichTextFormatSpan {

    override fun updateMeasureState(textPaint: TextPaint) {
        applyBold(textPaint)
    }

    override fun updateDrawState(textPaint: TextPaint) {
        applyBold(textPaint)
    }

    private fun applyBold(paint: TextPaint) {
        val currentTypeface = paint.typeface
        val isItalic = currentTypeface?.isItalic == true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val base = currentTypeface ?: Typeface.DEFAULT
            paint.typeface = Typeface.create(base, BOLD_WEIGHT, isItalic)
        } else {
            val style = if (isItalic) Typeface.BOLD_ITALIC else Typeface.BOLD
            val base = currentTypeface ?: Typeface.DEFAULT
            paint.typeface = Typeface.create(base, style)
        }
    }

    override fun getFormatType(): RichTextFormat = RichTextFormat.BOLD

    companion object {
        private const val BOLD_WEIGHT = 700
    }
}
