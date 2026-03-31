package com.cometchat.chatuikit.shared.spans;

import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for bold formatting in WYSIWYG rich text editing.
 * <p>
 * This span uses font weight 700 (API 28+) or {@link Typeface#BOLD} fallback
 * to render text in bold style. It implements {@link RichTextFormatSpan} to
 * enable format detection and markdown conversion.
 * </p>
 *
 * @see RichTextFormatSpan
 * @see FormatType#BOLD
 */
public class BoldFormatSpan extends MetricAffectingSpan implements RichTextFormatSpan {

    private static final int BOLD_WEIGHT = 700;

    /**
     * Creates a new BoldFormatSpan that renders text with font weight 700.
     */
    public BoldFormatSpan() {
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint textPaint) {
        applyBold(textPaint);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint textPaint) {
        applyBold(textPaint);
    }

    private void applyBold(@NonNull TextPaint paint) {
        Typeface currentTypeface = paint.getTypeface();
        boolean isItalic = currentTypeface != null && currentTypeface.isItalic();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface base = currentTypeface != null ? currentTypeface : Typeface.DEFAULT;
            paint.setTypeface(Typeface.create(base, BOLD_WEIGHT, isItalic));
        } else {
            int style = isItalic ? Typeface.BOLD_ITALIC : Typeface.BOLD;
            Typeface base = currentTypeface != null ? currentTypeface : Typeface.DEFAULT;
            paint.setTypeface(Typeface.create(base, style));
        }
    }

    /**
     * Returns the format type associated with this span.
     *
     * @return {@link FormatType#BOLD} indicating this is a bold format span.
     */
    @Override
    public FormatType getFormatType() {
        return FormatType.BOLD;
    }
}
