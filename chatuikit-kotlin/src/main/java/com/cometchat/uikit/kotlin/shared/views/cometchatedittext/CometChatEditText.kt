package com.cometchat.uikit.kotlin.shared.views.cometchatedittext

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.cometchat.uikit.kotlin.shared.mentions.CometChatTextWatcher
import com.cometchat.uikit.kotlin.shared.spans.NonEditableSpan
import kotlin.math.abs

/**
 * Custom EditText with span-aware cursor handling and backspace deletion.
 * 
 * This EditText provides special handling for NonEditableSpan (mention spans):
 * - Prevents cursor from being placed inside a span
 * - Moves cursor to nearest span edge when positioned inside
 * - Deletes entire span when backspace is pressed at span boundary
 * - Notifies listeners when spans are deleted
 * 
 * Based on the design document specifications for mention handling in the message composer.
 */
class CometChatEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    /**
     * Text watcher for receiving span deletion callbacks.
     */
    private var _textWatcher: CometChatTextWatcher? = null

    /**
     * Listener for media content (images, etc.) pasted into the EditText.
     */
    var onEditTextMediaListener: OnEditTextMediaListener? = null

    /**
     * Flag to prevent recursive selection changes.
     */
    private var isAdjustingSelection = false

    /**
     * Internal text watcher for detecting backspace and span deletion.
     */
    private val internalTextWatcher = object : TextWatcher {
        private var beforeText: CharSequence? = null
        private var beforeLength = 0

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeText = s?.toString()
            beforeLength = s?.length ?: 0
            _textWatcher?.beforeTextChanged(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Detect single character deletion (backspace)
            if (before == 1 && count == 0) {
                deleteSpanIfNeeded()
            }
            _textWatcher?.onTextChanged(s, start, before, count)
        }

        override fun afterTextChanged(s: Editable?) {
            _textWatcher?.afterTextChanged(s)
        }
    }

    init {
        addTextChangedListener(internalTextWatcher)
    }

    /**
     * Handles cursor position changes to prevent cursor from being inside spans.
     * 
     * When the cursor is positioned inside a NonEditableSpan:
     * - Calculates the nearest edge (start or end) of the span
     * - Moves the cursor to that edge
     * 
     * When a selection range crosses a span:
     * - Adjusts the selection to end at the span start
     */
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (isAdjustingSelection) {
            super.onSelectionChanged(selStart, selEnd)
            return
        }

        val editable = text ?: run {
            super.onSelectionChanged(selStart, selEnd)
            return
        }

        val spans = editable.getSpans(0, editable.length, NonEditableSpan::class.java)

        for (span in spans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)

            // Skip invalid spans
            if (spanStart < 0 || spanEnd < 0 || spanStart >= spanEnd) continue

            // If cursor is inside span (not at edges), move it to nearest edge
            if (selStart > spanStart && selStart < spanEnd) {
                val newPosition = if (abs(selStart - spanStart) < abs(selStart - spanEnd)) {
                    spanStart
                } else {
                    // Move to position after span (spanEnd, not spanEnd + 1 to avoid out of bounds)
                    minOf(spanEnd, editable.length)
                }
                
                isAdjustingSelection = true
                try {
                    setSelection(newPosition)
                } finally {
                    isAdjustingSelection = false
                }
                return
            }

            // Handle selection range crossing span
            if (selStart != selEnd && selEnd > spanStart && selEnd < spanEnd) {
                isAdjustingSelection = true
                try {
                    setSelection(selStart, spanStart)
                } finally {
                    isAdjustingSelection = false
                }
                return
            }
        }

        super.onSelectionChanged(selStart, selEnd)
    }

    /**
     * Checks if a span needs to be deleted after a backspace operation.
     * 
     * This is called when a single character is deleted. It checks if:
     * - The cursor is right after a span
     * - The span text no longer matches the original text (character was deleted from span)
     * 
     * If both conditions are met, the entire span is deleted and the listener is notified.
     */
    private fun deleteSpanIfNeeded() {
        val selectionStart = selectionStart
        val editable = text ?: return

        val spans = editable.getSpans(0, editable.length, NonEditableSpan::class.java)

        for (span in spans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)

            // Skip invalid spans
            if (spanStart < 0 || spanEnd < 0 || spanStart >= spanEnd) continue

            // Get current span text
            val currentSpanText = try {
                editable.substring(spanStart, spanEnd)
            } catch (e: Exception) {
                continue
            }

            val originalSpanText = span.getText()

            // If cursor is at or right after span end and span text doesn't match original
            // This means a character was deleted from the span
            if (selectionStart >= spanStart && selectionStart <= spanEnd && 
                currentSpanText != originalSpanText) {
                
                // Delete entire span
                try {
                    editable.delete(spanStart, spanEnd)
                    _textWatcher?.onSpanDeleted(span)
                } catch (e: Exception) {
                    // Ignore deletion errors
                }
                return
            }
        }
    }

    /**
     * Sets the text watcher for receiving text change and span deletion callbacks.
     * 
     * @param watcher The CometChatTextWatcher to set, or null to remove
     */
    fun setTextWatcher(watcher: CometChatTextWatcher?) {
        this._textWatcher = watcher
    }

    /**
     * Gets the current text watcher.
     * 
     * @return The current CometChatTextWatcher, or null if not set
     */
    fun getTextWatcher(): CometChatTextWatcher? = _textWatcher

    /**
     * Interface for receiving media content pasted into the EditText.
     */
    interface OnEditTextMediaListener {
        /**
         * Called when media content is pasted.
         * 
         * @param uri The URI of the media content
         * @param mimeType The MIME type of the content
         */
        fun onMediaSelected(uri: android.net.Uri, mimeType: String)
    }

    companion object {
        private const val TAG = "CometChatEditText"
    }
}
