package com.cometchat.uikit.compose.presentation.shared.messagebubble.aiassistantbubble

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

/**
 * Custom inline code span that renders inline `code` with a rounded
 * background rectangle, matching the XML reference implementation.
 */
internal class CustomInlineCodeSpan(
    private val textSize: Int,
    private val bgColor: Int,
    private val textColor: Int,
    private val cornerRadius: Float
) : ReplacementSpan() {

    private val marginLeft = 10f
    private val marginRight = 10f
    private val paddingHorizontal = 2f

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val textWidth = paint.measureText(text, start, end)
        return Math.round(marginLeft + marginRight + textWidth + paddingHorizontal * 2)
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val textWidth = paint.measureText(text, start, end)
        val fontMetrics = paint.fontMetrics

        val rectTop = y + fontMetrics.ascent - 2
        val rectBottom = y + fontMetrics.descent + 2
        val rectLeft = x + marginLeft
        val rectRight = rectLeft + textWidth + paddingHorizontal * 2

        // Draw background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            alpha = 40
        }
        canvas.drawRoundRect(
            RectF(rectLeft, rectTop, rectRight, rectBottom),
            cornerRadius, cornerRadius, bgPaint
        )

        // Draw text
        paint.color = textColor
        paint.textSize = textSize.toFloat()
        canvas.drawText(text, start, end, rectLeft + paddingHorizontal, y.toFloat(), paint)
    }
}
