package com.cometchat.uikit.compose.presentation.shared.mentions

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem

/**
 * State holder for mention functionality in the message composer.
 * 
 * This class provides a complete solution for:
 * - Detecting mentions in text input
 * - Inserting formatted mentions
 * - Processing text before sending
 * - Calling handlePreMessageSend on all formatters
 */
class MessageComposerMentionState(
    private val textFormatters: List<CometChatTextFormatter>
) {
    private val insertionState = ComposeMentionInsertionState()
    
    var mentionDetectionState by mutableStateOf(ComposeMentionState.INACTIVE)
        private set
    
    var isShowingSuggestions by mutableStateOf(false)
        private set
    
    var suggestions by mutableStateOf<List<SuggestionItem>>(emptyList())
        private set
    
    var isLoadingSuggestions by mutableStateOf(false)
        private set

    /**
     * Updates the mention detection state based on text field changes.
     * 
     * @param textFieldValue The current text field value
     * @param context The context for triggering search
     */
    fun onTextFieldValueChange(textFieldValue: TextFieldValue, context: Context) {
        mentionDetectionState = detectMention(
            text = textFieldValue.text,
            cursorPosition = textFieldValue.selection.start,
            textFormatters = textFormatters
        )
        
        if (mentionDetectionState.isActive) {
            // Trigger search on the active formatter
            mentionDetectionState.activeFormatter?.let { formatter ->
                isShowingSuggestions = true
                isLoadingSuggestions = true
                formatter.search(context, mentionDetectionState.query)
            }
        } else {
            isShowingSuggestions = false
            suggestions = emptyList()
        }
    }

    /**
     * Updates the suggestions list.
     * Call this when the formatter's suggestion list changes.
     */
    fun updateSuggestions(newSuggestions: List<SuggestionItem>) {
        suggestions = newSuggestions
        isLoadingSuggestions = false
    }

    /**
     * Handles selection of a suggestion item.
     * 
     * @param currentValue The current text field value
     * @param suggestionItem The selected suggestion item
     * @param mentionStyle The style to apply to the mention
     * @return New TextFieldValue with the mention inserted
     */
    fun onSuggestionSelected(
        currentValue: TextFieldValue,
        suggestionItem: SuggestionItem,
        mentionStyle: SpanStyle? = null
    ): TextFieldValue {
        val formatter = mentionDetectionState.activeFormatter ?: return currentValue
        
        val newValue = insertionState.insertMention(
            currentValue = currentValue,
            mentionState = mentionDetectionState,
            suggestionItem = suggestionItem,
            formatter = formatter,
            mentionStyle = mentionStyle
        )
        
        // Reset state
        isShowingSuggestions = false
        suggestions = emptyList()
        mentionDetectionState = ComposeMentionState.INACTIVE
        
        return newValue
    }

    /**
     * Processes the text before sending a message.
     * 
     * @param context The context
     * @param text The text to process
     * @param message The message being sent (will be modified)
     * @return The processed text
     */
    fun processBeforeSend(context: Context, text: String, message: BaseMessage): String {
        // Get processed text
        val processedText = insertionState.getProcessedText(text)
        
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
     */
    fun getProcessedText(text: String): String {
        return insertionState.getProcessedText(text)
    }

    /**
     * Gets all selected suggestion items.
     */
    fun getSelectedSuggestionItems(): List<SuggestionItem> {
        return insertionState.getSelectedSuggestionItems()
    }

    /**
     * Clears all tracked mentions and resets the state.
     */
    fun clear() {
        insertionState.clear()
        mentionDetectionState = ComposeMentionState.INACTIVE
        isShowingSuggestions = false
        suggestions = emptyList()
        isLoadingSuggestions = false
    }

    /**
     * Dismisses the suggestion list.
     */
    fun dismissSuggestions() {
        isShowingSuggestions = false
        suggestions = emptyList()
    }

    /**
     * Gets the insertion state for advanced operations.
     */
    fun getInsertionState(): ComposeMentionInsertionState = insertionState

    /**
     * Handles scroll to bottom in the suggestion list (for pagination).
     */
    fun onScrollToBottom() {
        mentionDetectionState.activeFormatter?.onScrollToBottom()
    }
}

/**
 * Remembers a MessageComposerMentionState.
 * 
 * @param textFormatters List of text formatters to use
 */
@Composable
fun rememberMessageComposerMentionState(
    textFormatters: List<CometChatTextFormatter>
): MessageComposerMentionState {
    return remember(textFormatters) {
        MessageComposerMentionState(textFormatters)
    }
}
