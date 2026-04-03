package com.cometchat.uikit.kotlin.shared.mentions

import com.cometchat.uikit.kotlin.shared.spans.NonEditableSpan

/**
 * Abstract class for handling text change events in CometChatEditText.
 * 
 * This class provides callbacks for text changes and span deletion events,
 * enabling proper handling of mention spans in the message composer.
 * 
 * Implementations should override the methods they need to handle.
 */
abstract class CometChatTextWatcher {

    /**
     * Called when the text is about to change.
     * 
     * @param s The current text before the change
     * @param start The starting position of the change
     * @param count The number of characters being replaced
     * @param after The number of characters that will replace the old text
     */
    open fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Default empty implementation
    }

    /**
     * Called when the text has changed.
     * 
     * @param s The text after the change
     * @param start The starting position of the change
     * @param before The number of characters that were replaced
     * @param count The number of new characters
     */
    open fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Default empty implementation
    }

    /**
     * Called after the text has changed.
     * 
     * @param s The editable text after all changes
     */
    open fun afterTextChanged(s: android.text.Editable?) {
        // Default empty implementation
    }

    /**
     * Called when a NonEditableSpan (mention) is deleted from the text.
     * 
     * This callback is triggered when the user deletes a mention span,
     * either by pressing backspace at the span boundary or by selecting
     * and deleting the span.
     * 
     * @param span The NonEditableSpan that was deleted
     */
    open fun onSpanDeleted(span: NonEditableSpan) {
        // Default empty implementation
    }

    companion object {
        private const val TAG = "CometChatTextWatcher"
    }
}
