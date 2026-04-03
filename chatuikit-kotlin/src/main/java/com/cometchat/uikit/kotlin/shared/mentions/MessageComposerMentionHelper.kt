package com.cometchat.uikit.kotlin.shared.mentions

import android.content.Context
import android.text.Editable
import android.widget.EditText
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.mentions.TextFormatterProcessor
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.spans.NonEditableSpan

/**
 * Helper class that integrates mention functionality with the message composer.
 * 
 * This class provides a complete solution for:
 * - Detecting mentions in text input
 * - Inserting formatted mentions with NonEditableSpan
 * - Processing text before sending (replacing spans with underlying text)
 * - Calling handlePreMessageSend on all formatters
 * 
 * @param editText The EditText for message input
 * @param textFormatters List of text formatters to use
 */
class MessageComposerMentionHelper(
    private val editText: EditText,
    private val textFormatters: List<CometChatTextFormatter>
) {
    companion object {
        private const val TAG = "MsgComposerMentionHelper"
    }
    
    private val mentionInsertionHelper = MentionInsertionHelper(editText)
    private var mentionTextWatcher: MentionTextWatcher? = null
    private var onMentionDetectedCallback: ((MentionTextWatcher.MentionDetectionResult) -> Unit)? = null
    private var onSuggestionSelectedCallback: ((SuggestionItem) -> Unit)? = null

    /**
     * Initializes the mention helper and starts listening for mention triggers.
     * 
     * @param onMentionDetected Callback when a mention is detected or cleared
     */
    fun initialize(onMentionDetected: (MentionTextWatcher.MentionDetectionResult) -> Unit) {
        android.util.Log.d(TAG, "initialize: setting up mention detection with ${textFormatters.size} formatters")
        onMentionDetectedCallback = onMentionDetected
        
        mentionTextWatcher = MentionTextWatcher(
            editText = editText,
            textFormatters = textFormatters,
            onMentionDetected = { result ->
                android.util.Log.d(TAG, "initialize: mention detected callback - isActive=${result.isActive}, query='${result.query}'")
                onMentionDetectedCallback?.invoke(result)
            }
        )
        
        editText.addTextChangedListener(mentionTextWatcher)
        android.util.Log.d(TAG, "initialize: text watcher added to EditText")
    }

    /**
     * Handles selection of a suggestion item.
     * 
     * This method temporarily disables mention detection during insertion
     * to prevent flicker caused by the TextWatcher triggering again.
     * 
     * @param suggestionItem The selected suggestion item
     * @param detectionResult The current mention detection result
     */
    fun onSuggestionSelected(
        suggestionItem: SuggestionItem,
        detectionResult: MentionTextWatcher.MentionDetectionResult
    ) {
        val formatter = detectionResult.formatter ?: return
        
        // Disable detection temporarily to prevent flicker during insertion
        mentionTextWatcher?.setDetectionEnabled(false)
        
        mentionInsertionHelper.insertMention(
            suggestionItem = suggestionItem,
            formatter = formatter,
            triggerIndex = detectionResult.triggerIndex,
            cursorPosition = detectionResult.cursorPosition
        )
        
        // Notify formatter of item click
        formatter.onItemClick(
            editText.context,
            suggestionItem,
            formatter.getUser(),
            formatter.getGroup()
        )
        
        onSuggestionSelectedCallback?.invoke(suggestionItem)
        
        // Re-enable detection after text change has propagated
        // Using post to ensure it runs after the text change is fully processed
        editText.post {
            mentionTextWatcher?.setDetectionEnabled(true)
        }
    }

    /**
     * Sets a callback for when a suggestion is selected.
     */
    fun setOnSuggestionSelectedCallback(callback: (SuggestionItem) -> Unit) {
        onSuggestionSelectedCallback = callback
    }

    /**
     * Processes the text before sending a message.
     * 
     * This method:
     * 1. Gets the processed text with spans replaced by underlying text
     * 2. Calls handlePreMessageSend on all formatters
     * 
     * @param context The context
     * @param message The message being sent (will be modified with processed text)
     * @return The processed text
     */
    fun processBeforeSend(context: Context, message: BaseMessage): String {
        // Get processed text (spans replaced with underlying text)
        val processedText = mentionInsertionHelper.getProcessedText()
        
        // Update message text if it's a TextMessage
        if (message is TextMessage) {
            message.text = processedText
        }
        
        // Call handlePreMessageSend on all formatters
        for (formatter in textFormatters) {
            formatter.handlePreMessageSend(context, message)
        }
        
        return processedText
    }

    /**
     * Gets the processed text without modifying any message.
     * 
     * @return The text with spans replaced by underlying text
     */
    fun getProcessedText(): String {
        return mentionInsertionHelper.getProcessedText()
    }

    /**
     * Gets all selected suggestion items.
     */
    fun getSelectedSuggestionItems(): List<SuggestionItem> {
        return mentionInsertionHelper.getSelectedSuggestionItems()
    }

    /**
     * Clears all tracked mentions and resets the helper.
     */
    fun clear() {
        mentionInsertionHelper.clear()
    }

    /**
     * Removes the text watcher and cleans up resources.
     */
    fun cleanup() {
        mentionTextWatcher?.let { editText.removeTextChangedListener(it) }
        mentionTextWatcher = null
        clear()
    }

    /**
     * Manually triggers mention detection.
     * Useful when cursor position changes without text change.
     */
    fun checkMention() {
        mentionTextWatcher?.checkMention()
    }

    /**
     * Gets the mention insertion helper for advanced operations.
     */
    fun getMentionInsertionHelper(): MentionInsertionHelper = mentionInsertionHelper

    /**
     * Syncs the tracked mentions with the current EditText content.
     */
    fun syncMentions() {
        mentionInsertionHelper.syncWithEditText()
    }
}

/**
 * Extension function to create a MessageComposerMentionHelper for an EditText.
 */
fun EditText.createMessageComposerMentionHelper(
    textFormatters: List<CometChatTextFormatter>
): MessageComposerMentionHelper {
    return MessageComposerMentionHelper(this, textFormatters)
}
