package com.cometchat.chatuikit.shared.views.aiassistant;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;

public class CustomInlineCodeSpan extends ReplacementSpan {
    private static final String TAG = "CustomInlineCodeSpan";

    // Margins (space outside the background)
    private final float marginTop = 10f;
    private final float marginLeft = 10f;
    private final float marginRight = 10f;
    private final float marginBottom = 10f;

    // Padding (space inside the background)
    private final float paddingLeft = 2f;
    private final float paddingRight = 2f;
    private final float paddingTop = 2f;
    private final float paddingBottom = 2f;

    private final int textSize;
    private final int bgColor;
    private final int textColor;
    private final float cornerRadius;

    public CustomInlineCodeSpan(
            @Dimension int textSize,
            @ColorInt int bgColor,
            @ColorInt int textColor,
            float cornerRadius
    ) {
        this.textSize = textSize;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.cornerRadius = cornerRadius;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        float textWidth = paint.measureText(text, start, end);
        // Total width = margins + padding + text
        return Math.round(marginLeft + marginRight + textWidth + 2 * 2);
    }
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {

        float textWidth = paint.measureText(text, start, end);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        // Background vertical bounds including padding
        float rectTop = y + fontMetrics.ascent - 2;
        float rectBottom = y + fontMetrics.descent + 2;

        // Background horizontal bounds including margins and padding
        float rectLeft = x + marginLeft;
        float rectRight = rectLeft + textWidth + 2 * 2;

        // Draw background
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
        bgPaint.setAlpha(40); // optional soft transparency
        RectF rect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint);

        // Draw text (inside the background, offset by horizontal padding)
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        canvas.drawText(text, start, end, rectLeft + 2, y, paint);
    }
}
