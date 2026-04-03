package com.cometchat.uikit.compose.presentation.shared.mentions

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import com.cometchat.uikit.core.mentions.MentionInserter
import com.cometchat.uikit.core.mentions.SelectedMention
import com.cometchat.uikit.core.mentions.SelectedMentionsManager
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem

/**
 * State holder for mention insertion in Compose.
 * 
 * This class manages the state of inserted mentions and provides methods
 * for inserting, tracking, and processing mentions.
 */
class ComposeMentionInsertionState {
    private val mentionsManager = SelectedMentionsManager()
    private val selectedSuggestionItems = mutableMapOf<String, SuggestionItem>()
    
    // Map of mention ID to its span style for rendering
    private val mentionStyles = mutableMapOf<String, SpanStyle>()
    
    companion object {
        private const val TAG = "MentionInsertionState"
    }
    
    /**
     * Inserts a mention into the text field value.
     * 
     * @param currentValue The current text field value
     * @param mentionState The current mention detection state
     * @param suggestionItem The suggestion item to insert
     * @param formatter The text formatter
     * @param mentionStyle The style to apply to the mention
     * @return New TextFieldValue with the mention inserted
     */
    fun insertMention(
        currentValue: TextFieldValue,
        mentionState: ComposeMentionState,
        suggestionItem: SuggestionItem,
        formatter: CometChatTextFormatter,
        mentionStyle: SpanStyle? = null
    ): TextFieldValue {
        if (!mentionState.isActive) return currentValue
        
        Log.d(TAG, "insertMention: currentText='${currentValue.text}', triggerIndex=${mentionState.triggerIndex}, cursorPos=${mentionState.cursorPosition}")
        Log.d(TAG, "insertMention: suggestionItem id=${suggestionItem.id}, name=${suggestionItem.name}, promptText='${suggestionItem.promptText}', underlyingText='${suggestionItem.underlyingText}'")
        
        val result = MentionInserter.calculateInsertion(
            currentText = currentValue.text,
            triggerIndex = mentionState.triggerIndex,
            cursorPosition = mentionState.cursorPosition,
            promptText = suggestionItem.promptText,
            underlyingText = suggestionItem.underlyingText
        )
        
        Log.d(TAG, "insertMention: result newText='${result.newText}', spanStart=${result.spanStart}, spanEnd=${result.spanEnd}, newCursorPos=${result.newCursorPosition}")
        
        // Track the mention
        val selectedMention = SelectedMention(
            id = suggestionItem.id,
            name = suggestionItem.name,
            promptText = suggestionItem.promptText,
            underlyingText = suggestionItem.underlyingText,
            spanStart = result.spanStart,
            spanEnd = result.spanEnd
        )
        mentionsManager.addMention(selectedMention)
        selectedSuggestionItems[suggestionItem.id] = suggestionItem
        
        Log.d(TAG, "insertMention: tracked mention - promptText='${suggestionItem.promptText}', spanStart=${result.spanStart}, spanEnd=${result.spanEnd}")
        Log.d(TAG, "insertMention: total tracked mentions=${mentionsManager.getMentions().size}")
        
        // Store style for rendering
        mentionStyle?.let { mentionStyles[suggestionItem.id] = it }
        
        return TextFieldValue(
            text = result.newText,
            selection = TextRange(result.newCursorPosition)
        )
    }
    
    /**
     * Gets the processed text for sending, replacing mentions with underlying text.
     * Uses span positions for accurate replacement instead of string matching.
     */
    fun getProcessedText(text: String): String {
        Log.d(TAG, "getProcessedText: input text='$text', length=${text.length}")
        
        val mentions = mentionsManager.getMentions()
        Log.d(TAG, "getProcessedText: tracked mentions count=${mentions.size}")
        
        if (mentions.isEmpty()) {
            Log.d(TAG, "getProcessedText: no mentions tracked, returning original text")
            return text
        }
        
        // Log all tracked mentions
        mentions.forEachIndexed { index, mention ->
            Log.d(TAG, "getProcessedText: mention[$index] id=${mention.id}, promptText='${mention.promptText}', underlyingText='${mention.underlyingText}', spanStart=${mention.spanStart}, spanEnd=${mention.spanEnd}")
        }
        
        // Sort by position descending to replace from end to start (preserves indices)
        val sortedMentions = mentions.sortedByDescending { it.spanStart }
        
        val result = StringBuilder(text)
        for (mention in sortedMentions) {
            val start = mention.spanStart
            val end = mention.spanEnd
            
            Log.d(TAG, "getProcessedText: processing mention '${mention.promptText}' at [$start, $end)")
            
            // Validate bounds
            if (start >= 0 && end <= result.length && start < end) {
                // Verify the text at position matches the prompt text
                val textAtPosition = result.substring(start, end)
                val expectedText = mention.promptText.take(end - start)
                
                Log.d(TAG, "getProcessedText: textAtPosition='$textAtPosition', expectedText='$expectedText', match=${textAtPosition == expectedText}")
                
                if (textAtPosition == expectedText) {
                    Log.d(TAG, "getProcessedText: replacing '$textAtPosition' with '${mention.underlyingText}'")
                    result.replace(start, end, mention.underlyingText)
                } else {
                    Log.w(TAG, "getProcessedText: text mismatch! textAtPosition='$textAtPosition' != expectedText='$expectedText'")
                }
            } else {
                Log.w(TAG, "getProcessedText: invalid bounds! start=$start, end=$end, resultLength=${result.length}")
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
     * Gets the mentions manager.
     */
    fun getMentionsManager(): SelectedMentionsManager = mentionsManager
    
    /**
     * Clears all tracked mentions.
     */
    fun clear() {
        Log.d(TAG, "clear: clearing all mentions (was ${mentionsManager.getMentions().size})")
        mentionsManager.clear()
        selectedSuggestionItems.clear()
        mentionStyles.clear()
    }
    
    /**
     * Builds an AnnotatedString with styled mentions.
     * 
     * @param text The text to annotate
     * @param defaultMentionStyle The default style for mentions
     * @return AnnotatedString with styled mentions
     */
    fun buildAnnotatedText(
        text: String,
        defaultMentionStyle: SpanStyle
    ): AnnotatedString {
        val mentions = mentionsManager.getMentions()
        if (mentions.isEmpty()) {
            return AnnotatedString(text)
        }
        
        return buildAnnotatedString {
            var lastEnd = 0
            val sortedMentions = mentions.sortedBy { it.spanStart }
            
            for (mention in sortedMentions) {
                // Validate span positions
                if (mention.spanStart < lastEnd || mention.spanEnd > text.length) {
                    continue
                }
                
                // Append text before mention
                if (mention.spanStart > lastEnd) {
                    append(text.substring(lastEnd, mention.spanStart))
                }
                
                // Append styled mention
                val style = mentionStyles[mention.id] ?: defaultMentionStyle
                withStyle(style) {
                    val mentionText = if (mention.spanEnd <= text.length) {
                        text.substring(mention.spanStart, mention.spanEnd)
                    } else {
                        mention.promptText
                    }
                    append(mentionText)
                }
                
                // Add annotation for click handling
                addStringAnnotation(
                    tag = "mention",
                    annotation = mention.id,
                    start = mention.spanStart,
                    end = mention.spanEnd
                )
                
                lastEnd = mention.spanEnd
            }
            
            // Append remaining text
            if (lastEnd < text.length) {
                append(text.substring(lastEnd))
            }
        }
    }
    
    /**
     * Updates mention positions after text changes.
     */
    fun updatePositions(changeStart: Int, changeLength: Int) {
        Log.d(TAG, "updatePositions: changeStart=$changeStart, changeLength=$changeLength")
        mentionsManager.updatePositions(changeStart, changeLength)
    }
    
    /**
     * Syncs the state with the current text.
     * Removes mentions that are no longer in the text.
     */
    fun syncWithText(text: String) {
        Log.d(TAG, "syncWithText: text='$text', length=${text.length}")
        val mentions = mentionsManager.getMentions().toList()
        Log.d(TAG, "syncWithText: checking ${mentions.size} mentions")
        
        for (mention in mentions) {
            Log.d(TAG, "syncWithText: checking mention '${mention.promptText}' at [${mention.spanStart}, ${mention.spanEnd})")
            
            // Check if the mention text is still present at the expected position
            if (mention.spanEnd > text.length) {
                // Span extends beyond text - mention was partially deleted
                Log.d(TAG, "syncWithText: removing mention '${mention.promptText}' - spanEnd ${mention.spanEnd} > textLength ${text.length}")
                mentionsManager.removeMention(mention.id)
                selectedSuggestionItems.remove(mention.id)
                mentionStyles.remove(mention.id)
            } else if (mention.spanStart >= 0 && mention.spanEnd <= text.length && mention.spanStart < mention.spanEnd) {
                val textAtPosition = text.substring(mention.spanStart, mention.spanEnd)
                // Check if the text at the span position matches the prompt text
                // Use exact match for the span length
                val expectedText = mention.promptText.take(mention.spanEnd - mention.spanStart)
                Log.d(TAG, "syncWithText: textAtPosition='$textAtPosition', expectedText='$expectedText'")
                
                if (textAtPosition != expectedText) {
                    // Also check if the full promptText exists anywhere in the text
                    // This handles cases where the mention was moved but still exists
                    val containsPrompt = text.contains(mention.promptText)
                    Log.d(TAG, "syncWithText: text mismatch, containsPrompt=$containsPrompt")
                    
                    if (!containsPrompt) {
                        Log.d(TAG, "syncWithText: removing mention '${mention.promptText}' - text mismatch and not found in text")
                        mentionsManager.removeMention(mention.id)
                        selectedSuggestionItems.remove(mention.id)
                        mentionStyles.remove(mention.id)
                    }
                } else {
                    Log.d(TAG, "syncWithText: mention '${mention.promptText}' is valid")
                }
            }
        }
        
        Log.d(TAG, "syncWithText: after sync, ${mentionsManager.getMentions().size} mentions remain")
    }
}

/**
 * Remembers a ComposeMentionInsertionState.
 */
@Composable
fun rememberMentionInsertionState(): ComposeMentionInsertionState {
    return remember { ComposeMentionInsertionState() }
}
