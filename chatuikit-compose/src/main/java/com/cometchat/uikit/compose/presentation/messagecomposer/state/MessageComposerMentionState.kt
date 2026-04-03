package com.cometchat.uikit.compose.presentation.messagecomposer.state

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatMentionsFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem
import com.cometchat.uikit.compose.presentation.shared.mentions.ComposeMentionInsertionState
import com.cometchat.uikit.compose.presentation.shared.mentions.ComposeMentionState
import com.cometchat.uikit.compose.presentation.shared.mentions.detectMention

/**
 * Stable state holder for mention functionality in the message composer.
 * 
 * This class encapsulates all mention-related state and provides a complete solution for:
 * - Detecting mentions in text input
 * - Managing suggestion list visibility and loading state
 * - Tracking selected mentions and enforcing mention limits
 * - Processing text before sending messages
 * - Efficient recomposition through proper state management
 * 
 * The class uses Compose's [Stable] annotation to help the compiler optimize recomposition
 * by indicating that the class's public properties are stable and won't change unexpectedly.
 * 
 * @param textFormatters List of text formatters for mention detection and formatting
 * @param mentionLimit Maximum number of mentions allowed per message (default: 10)
 */
@Stable
class MessageComposerMentionState(
    private val textFormatters: List<CometChatTextFormatter>,
    private val mentionLimit: Int = DEFAULT_MENTION_LIMIT
) {
    // Internal insertion state for tracking mentions
    private val insertionState = ComposeMentionInsertionState()
    
    // ==================== Core State Properties ====================
    
    /**
     * Current mention detection state.
     * Contains information about whether a mention is being typed,
     * the query string, trigger index, and active formatter.
     */
    var mentionDetectionState by mutableStateOf(ComposeMentionState.INACTIVE)
        private set
    
    /**
     * Whether the suggestion list should be visible.
     */
    var isShowingSuggestions by mutableStateOf(false)
        private set
    
    /**
     * Current list of suggestion items to display.
     */
    var suggestions by mutableStateOf<List<SuggestionItem>>(emptyList())
        private set
    
    /**
     * Whether suggestions are currently being loaded.
     */
    var isLoadingSuggestions by mutableStateOf(false)
        private set
    
    /**
     * Current query string for filtering suggestions.
     */
    var currentQuery by mutableStateOf("")
        private set
    
    /**
     * Info message to display (e.g., mention limit warning).
     */
    var infoMessage by mutableStateOf("")
        private set
    
    /**
     * Whether the info message should be visible.
     */
    var isInfoMessageVisible by mutableStateOf(false)
        private set
    
    /**
     * Count of currently selected mentions.
     */
    private var selectedMentionCount by mutableIntStateOf(0)
    
    // ==================== Derived State Properties ====================
    
    /**
     * Whether the mention limit has been reached.
     * Uses derivedStateOf for efficient recomposition - only recomposes
     * when the actual value changes, not on every state read.
     */
    val isMentionLimitReached: Boolean by derivedStateOf {
        selectedMentionCount >= mentionLimit
    }
    
    /**
     * Whether suggestions should be shown.
     * Combines multiple conditions using derivedStateOf for efficiency.
     */
    val shouldShowSuggestions: Boolean by derivedStateOf {
        isShowingSuggestions && 
        mentionDetectionState.isActive && 
        !isMentionLimitReached &&
        (suggestions.isNotEmpty() || isLoadingSuggestions)
    }
    
    /**
     * Number of remaining mentions allowed.
     */
    val remainingMentions: Int by derivedStateOf {
        (mentionLimit - selectedMentionCount).coerceAtLeast(0)
    }
    
    /**
     * Whether any mentions are currently selected.
     */
    val hasMentions: Boolean by derivedStateOf {
        selectedMentionCount > 0
    }
    
    // ==================== Public Methods ====================
    
    /**
     * Updates the mention detection state based on text field changes.
     * 
     * This method should be called whenever the text field value changes.
     * It detects if a mention is being typed and triggers the appropriate
     * search on the active formatter.
     * 
     * @param textFieldValue The current text field value
     * @param context The context for triggering search
     */
    fun onTextFieldValueChange(textFieldValue: TextFieldValue, context: Context) {
        // Detect mention in the current text
        mentionDetectionState = detectMention(
            text = textFieldValue.text,
            cursorPosition = textFieldValue.selection.start,
            textFormatters = textFormatters
        )
        
        // Update current query
        currentQuery = mentionDetectionState.query
        
        if (mentionDetectionState.isActive && !isMentionLimitReached) {
            // Trigger search on the active formatter
            mentionDetectionState.activeFormatter?.let { formatter ->
                isShowingSuggestions = true
                isLoadingSuggestions = true
                formatter.search(context, mentionDetectionState.query)
            }
        } else {
            // Hide suggestions if not active or limit reached
            if (isMentionLimitReached && mentionDetectionState.isActive) {
                showMentionLimitInfo()
            } else {
                hideSuggestions()
            }
        }
        
        // Sync insertion state with current text
        insertionState.syncWithText(textFieldValue.text)
        updateSelectedMentionCount()
    }
    
    /**
     * Updates the suggestions list from the formatter.
     * Call this when the formatter's suggestion list changes.
     * 
     * @param newSuggestions The new list of suggestions
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
     * @param context The context for notifying formatters
     * @return New TextFieldValue with the mention inserted
     */
    fun onSuggestionSelected(
        currentValue: TextFieldValue,
        suggestionItem: SuggestionItem,
        mentionStyle: SpanStyle? = null,
        context: Context? = null
    ): TextFieldValue {
        val formatter = mentionDetectionState.activeFormatter ?: return currentValue
        
        // Check mention limit before inserting
        if (isMentionLimitReached) {
            showMentionLimitInfo()
            return currentValue
        }
        
        val newValue = insertionState.insertMention(
            currentValue = currentValue,
            mentionState = mentionDetectionState,
            suggestionItem = suggestionItem,
            formatter = formatter,
            mentionStyle = mentionStyle
        )
        
        // Update selected mention count
        updateSelectedMentionCount()
        
        // Notify formatter about selection
        context?.let { ctx ->
            formatter.onItemClick(ctx, suggestionItem, null, null)
        }
        
        // Reset suggestion state
        hideSuggestions()
        mentionDetectionState = ComposeMentionState.INACTIVE
        currentQuery = ""
        
        return newValue
    }
    
    /**
     * Processes the text before sending a message.
     * 
     * This method:
     * 1. Replaces prompt text with underlying text for mentions
     * 2. Updates the message text if it's a TextMessage
     * 3. Calls handlePreMessageSend on all formatters
     * 
     * @param context The context
     * @param text The text to process
     * @param message The message being sent (will be modified)
     * @return The processed text ready for sending
     */
    fun processBeforeSend(context: Context, text: String, message: BaseMessage): String {
        // Get processed text with underlying mention format
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
     * Useful for preview or validation purposes.
     * 
     * @param text The text to process
     * @return The processed text with mentions replaced
     */
    fun getProcessedText(text: String): String {
        return insertionState.getProcessedText(text)
    }
    
    /**
     * Gets all selected suggestion items.
     * 
     * @return List of currently selected suggestion items
     */
    fun getSelectedSuggestionItems(): List<SuggestionItem> {
        return insertionState.getSelectedSuggestionItems()
    }
    
    /**
     * Gets the insertion state for advanced operations.
     * 
     * @return The underlying ComposeMentionInsertionState
     */
    fun getInsertionState(): ComposeMentionInsertionState = insertionState
    
    /**
     * Handles scroll to bottom in the suggestion list.
     * Used for pagination/loading more suggestions.
     */
    fun onScrollToBottom() {
        mentionDetectionState.activeFormatter?.onScrollToBottom()
    }
    
    /**
     * Dismisses the suggestion list without clearing other state.
     */
    fun dismissSuggestions() {
        isShowingSuggestions = false
        suggestions = emptyList()
        isLoadingSuggestions = false
    }
    
    /**
     * Clears all tracked mentions and resets the state.
     * Call this when the message is sent or the composer is cleared.
     * 
     * @param context Optional context for clearing formatter selected lists.
     *                If null, formatter lists won't be cleared.
     */
    fun clear(context: Context? = null) {
        insertionState.clear()
        mentionDetectionState = ComposeMentionState.INACTIVE
        isShowingSuggestions = false
        suggestions = emptyList()
        isLoadingSuggestions = false
        currentQuery = ""
        infoMessage = ""
        isInfoMessageVisible = false
        selectedMentionCount = 0
        
        // Clear selected lists on all formatters if context is provided
        context?.let { ctx ->
            textFormatters.forEach { formatter ->
                formatter.setSelectedList(ctx, emptyList())
            }
        }
    }
    
    /**
     * Hides the info message.
     */
    fun hideInfoMessage() {
        isInfoMessageVisible = false
        infoMessage = ""
    }
    
    // ==================== Private Helper Methods ====================
    
    private fun hideSuggestions() {
        isShowingSuggestions = false
        suggestions = emptyList()
        isLoadingSuggestions = false
    }
    
    private fun showMentionLimitInfo() {
        infoMessage = "You can mention up to $mentionLimit users at a time"
        isInfoMessageVisible = true
        hideSuggestions()
    }
    
    private fun updateSelectedMentionCount() {
        selectedMentionCount = insertionState.getSelectedSuggestionItems().size
    }
    
    companion object {
        /**
         * Default maximum number of mentions allowed per message.
         */
        const val DEFAULT_MENTION_LIMIT = 10
    }
}

/**
 * Remembers a [MessageComposerMentionState] instance.
 * 
 * This composable function creates and remembers a MessageComposerMentionState
 * that survives recomposition. The state is recreated if the textFormatters
 * or mentionLimit parameters change.
 * 
 * Usage:
 * ```kotlin
 * val mentionState = rememberMessageComposerMentionState(
 *     textFormatters = listOf(CometChatMentionsFormatter(context)),
 *     mentionLimit = 10
 * )
 * ```
 * 
 * @param textFormatters List of text formatters to use for mention detection
 * @param mentionLimit Maximum number of mentions allowed (default: 10)
 * @return A remembered MessageComposerMentionState instance
 */
@Composable
fun rememberMessageComposerMentionState(
    textFormatters: List<CometChatTextFormatter>,
    mentionLimit: Int = MessageComposerMentionState.DEFAULT_MENTION_LIMIT
): MessageComposerMentionState {
    return remember(textFormatters, mentionLimit) {
        MessageComposerMentionState(
            textFormatters = textFormatters,
            mentionLimit = mentionLimit
        )
    }
}

/**
 * Remembers a [MessageComposerMentionState] with a key for more control over recreation.
 * 
 * This variant allows specifying additional keys that will trigger state recreation
 * when they change. Useful when the state needs to be reset based on external factors
 * like user or group changes.
 * 
 * @param key1 Additional key for state recreation
 * @param textFormatters List of text formatters to use
 * @param mentionLimit Maximum number of mentions allowed
 * @return A remembered MessageComposerMentionState instance
 */
@Composable
fun rememberMessageComposerMentionState(
    key1: Any?,
    textFormatters: List<CometChatTextFormatter>,
    mentionLimit: Int = MessageComposerMentionState.DEFAULT_MENTION_LIMIT
): MessageComposerMentionState {
    return remember(key1, textFormatters, mentionLimit) {
        MessageComposerMentionState(
            textFormatters = textFormatters,
            mentionLimit = mentionLimit
        )
    }
}
