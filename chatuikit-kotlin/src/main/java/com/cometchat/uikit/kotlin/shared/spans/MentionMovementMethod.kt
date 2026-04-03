package com.cometchat.uikit.kotlin.shared.spans

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView

/**
 * A custom movement method for handling mention tag clicks.
 * Ensures that clicks are only registered when the touch is within the span's bounds.
 */
class MentionMovementMethod private constructor() : LinkMovementMethod() {

    private var touchedSpan: Any? = null

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            // Get touch coordinates
            var x = event.x.toInt()
            var y = event.y.toInt()

            // Adjust for padding
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            // Adjust for scrolling
            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout ?: return false

            // Get line and offset
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            // Find the spans at the touch position
            val spans = buffer.getSpans(off, off, TagSpan::class.java)

            if (spans.isNotEmpty()) {
                val clickedSpan = spans[0]
                val spanStart = layout.getPrimaryHorizontal(buffer.getSpanStart(clickedSpan))
                val spanEnd = layout.getPrimaryHorizontal(buffer.getSpanEnd(clickedSpan))

                // Ensure the click is within the span's bounds
                if (x >= spanStart && x <= spanEnd) {
                    if (action == MotionEvent.ACTION_UP) {
                        if (touchedSpan == clickedSpan) {
                            clickedSpan.onClick(widget)
                        }
                        touchedSpan = null
                    } else {
                        touchedSpan = clickedSpan
                    }
                    return true
                }
            }

            // Clear touchedSpan if not within bounds
            touchedSpan = null
        }

        return super.onTouchEvent(widget, buffer, event)
    }

    companion object {
        @Volatile
        private var instance: MentionMovementMethod? = null

        fun getInstance(): MentionMovementMethod {
            return instance ?: synchronized(this) {
                instance ?: MentionMovementMethod().also { instance = it }
            }
        }
    }
}
