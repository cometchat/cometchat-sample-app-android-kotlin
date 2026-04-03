package com.cometchat.uikit.kotlin.shared.mentions

import android.text.Editable
import android.text.Spanned
import android.util.Log
import android.widget.EditText
import com.cometchat.uikit.core.mentions.SelectedMention
import com.cometchat.uikit.core.mentions.SelectedMentionsManager
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.spans.NonEditableSpan

/**
 * Helper class for inserting mentions into an EditText with NonEditableSpan formatting.
 * 
 * This class handles:
 * - Inserting formatted mention text with NonEditableSpan
 * - Tracking selected mentions for message processing
 * - Processing text before sending to replace spans with underlying text
 */
class MentionInsertionHelper(
    private val editText: EditText
) {
    private val mentionsManager = SelectedMentionsManager()
    private val selectedSuggestionItems = mutableMapOf<String, SuggestionItem>()

    companion object {
        private const val TAG = "MentionInsertionHelper"
    }

    /**
     * Inserts a mention at the specified position.
     * 
     * This method preserves existing spans in the EditText by modifying the
     * Editable in-place rather than replacing the entire content.
     * 
     * @param suggestionItem The suggestion item to insert
     * @param formatter The text formatter that triggered the mention
     * @param triggerIndex The index of the tracking character
     * @param cursorPosition The current cursor position
     */
    fun insertMention(
        suggestionItem: SuggestionItem,
        formatter: CometChatTextFormatter,
        triggerIndex: Int,
        cursorPosition: Int
    ) {
        val editable = editText.text ?: return
        
        Log.d(TAG, "insertMention: currentText='${editable}', triggerIndex=$triggerIndex, cursorPos=$cursorPosition")
        Log.d(TAG, "insertMention: suggestionItem id=${suggestionItem.id}, name=${suggestionItem.name}, promptText='${suggestionItem.promptText}', underlyingText='${suggestionItem.underlyingText}'")
        
        val promptText = suggestionItem.promptText
        
        // Create the span with styling from the suggestion item
        val span = NonEditableSpan(
            formatter.getTrackingCharacter(),
            promptText,
            suggestionItem
        )
        
        // Calculate the text to replace (from trigger to cursor)
        val replaceStart = triggerIndex
        val replaceEnd = cursorPosition
        
        // Add space after mention if not already present
        val mentionWithSpace = if (promptText.endsWith(" ")) promptText else "$promptText "
        
        Log.d(TAG, "insertMention: replacing [$replaceStart, $replaceEnd) with '$mentionWithSpace'")
        
        // Replace text in-place (preserves existing spans)
        editable.replace(replaceStart, replaceEnd, mentionWithSpace)
        
        // Calculate span boundaries (without the trailing space)
        val spanStart = replaceStart
        val spanEnd = replaceStart + promptText.length
        
        Log.d(TAG, "insertMention: applying span at [$spanStart, $spanEnd)")
        
        // Apply the span
        editable.setSpan(
            span,
            spanStart,
            spanEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // Move cursor after the mention and space
        val newCursorPosition = replaceStart + mentionWithSpace.length
        editText.setSelection(newCursorPosition)
        
        // Track the mention
        val selectedMention = SelectedMention(
            id = suggestionItem.id,
            name = suggestionItem.name,
            promptText = promptText,
            underlyingText = suggestionItem.underlyingText,
            spanStart = spanStart,
            spanEnd = spanEnd
        )
        mentionsManager.addMention(selectedMention)
        selectedSuggestionItems[suggestionItem.id] = suggestionItem
        
        Log.d(TAG, "insertMention: tracked mention, total=${mentionsManager.getMentions().size}")
        
        // Notify formatter of selection
        formatter.setSelectedList(editText.context, selectedSuggestionItems.values.toList())
    }

    /**
     * Gets the processed text for sending, replacing spans with underlying text.
     * 
     * @return The text with mentions replaced by their underlying format
     */
    fun getProcessedText(): String {
        val editable = editText.text ?: return ""
        val text = editable.toString()
        
        Log.d(TAG, "getProcessedText: text='$text', length=${text.length}")
        
        // Get all NonEditableSpans and replace their text with underlying text
        val spans = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
        
        Log.d(TAG, "getProcessedText: found ${spans.size} NonEditableSpans")
        
        if (spans.isEmpty()) {
            Log.d(TAG, "getProcessedText: no spans found, returning original text")
            return text
        }
        
        // Sort spans by position (reverse order for replacement)
        val sortedSpans = spans.sortedByDescending { editable.getSpanStart(it) }
        
        val result = StringBuilder(text)
        for (span in sortedSpans) {
            val start = editable.getSpanStart(span)
            val end = editable.getSpanEnd(span)
            val suggestionItem = span.getSuggestionItem()
            
            Log.d(TAG, "getProcessedText: span at [$start, $end), suggestionItem=${suggestionItem?.id}")
            
            if (suggestionItem != null && start >= 0 && end <= result.length) {
                val textAtPosition = result.substring(start, end)
                Log.d(TAG, "getProcessedText: replacing '$textAtPosition' with '${suggestionItem.underlyingText}'")
                result.replace(start, end, suggestionItem.underlyingText)
            } else {
                Log.w(TAG, "getProcessedText: invalid span - suggestionItem=${suggestionItem != null}, start=$start, end=$end, resultLength=${result.length}")
            }
        }
        
        Log.d(TAG, "getProcessedText: final result='$result'")
        return result.toString()
    }

    /**
     * Gets all selected suggestion items.
     */
    fun getSelectedSuggestionItems(): List<SuggestionItem> {
        return selectedSuggestionItems.values.toList()
    }

    /**
     * Gets the selected mentions manager.
     */
    fun getMentionsManager(): SelectedMentionsManager = mentionsManager

    /**
     * Clears all tracked mentions.
     */
    fun clear() {
        Log.d(TAG, "clear: clearing all mentions (was ${mentionsManager.getMentions().size})")
        mentionsManager.clear()
        selectedSuggestionItems.clear()
    }

    /**
     * Updates the tracked mentions based on current EditText content.
     * Call this after text changes to sync the tracked mentions.
     */
    fun syncWithEditText() {
        val editable = editText.text ?: return
        val spans = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
        
        Log.d(TAG, "syncWithEditText: found ${spans.size} spans in EditText")
        
        // Clear and rebuild from current spans
        mentionsManager.clear()
        selectedSuggestionItems.clear()
        
        for (span in spans) {
            val start = editable.getSpanStart(span)
            val end = editable.getSpanEnd(span)
            val suggestionItem = span.getSuggestionItem()
            
            Log.d(TAG, "syncWithEditText: span at [$start, $end), suggestionItem=${suggestionItem?.id}")
            
            if (suggestionItem != null) {
                val selectedMention = SelectedMention(
                    id = suggestionItem.id,
                    name = suggestionItem.name,
                    promptText = suggestionItem.promptText,
                    underlyingText = suggestionItem.underlyingText,
                    spanStart = start,
                    spanEnd = end
                )
                mentionsManager.addMention(selectedMention)
                selectedSuggestionItems[suggestionItem.id] = suggestionItem
            }
        }
        
        Log.d(TAG, "syncWithEditText: after sync, ${mentionsManager.getMentions().size} mentions tracked")
    }

    /**
     * Checks if the cursor is within a mention span.
     */
    fun isCursorInMention(): Boolean {
        val editable = editText.text ?: return false
        val cursorPosition = editText.selectionStart
        val spans = editable.getSpans(cursorPosition, cursorPosition, NonEditableSpan::class.java)
        return spans.isNotEmpty()
    }

    /**
     * Gets the mention at the current cursor position.
     */
    fun getMentionAtCursor(): SuggestionItem? {
        val editable = editText.text ?: return null
        val cursorPosition = editText.selectionStart
        val spans = editable.getSpans(cursorPosition, cursorPosition, NonEditableSpan::class.java)
        return spans.firstOrNull()?.getSuggestionItem()
    }
}

/**
 * Extension function to create a MentionInsertionHelper for an EditText.
 */
fun EditText.createMentionInsertionHelper(): MentionInsertionHelper {
    return MentionInsertionHelper(this)
}
