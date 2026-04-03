package com.cometchat.uikit.compose.shared.mentions

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
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem
import kotlin.math.abs

/**
 * Represents a selected mention in the text field.
 *
 * @property id The unique identifier (e.g., user ID or "all" for mention all)
 * @property name The display name of the mentioned user
 * @property promptText The text shown in the input (e.g., "@John")
 * @property underlyingText The text stored for processing (e.g., "<@uid:user123>")
 * @property range The character range of the mention in the text
 */
data class SelectedMention(
    val id: String,
    val name: String,
    val promptText: String,
    val underlyingText: String,
    val range: IntRange
)

/**
 * State class for mention-aware text field in Compose.
 *
 * This class provides complete state management for a text field with mention support:
 * - Cursor position validation (prevents cursor inside mentions)
 * - Mention insertion with proper formatting
 * - Backspace handling for mention deletion
 * - Tracking of selected mentions
 *
 * Based on the View-based CometChatEditText behavior from the design document.
 */
class MentionTextFieldState {

    /**
     * The current text field value.
     */
    var textFieldValue by mutableStateOf(TextFieldValue())
        private set

    /**
     * List of currently selected mentions in the text.
     */
    var selectedMentions by mutableStateOf<List<SelectedMention>>(emptyList())
        private set

    /**
     * Map of mention styles by ID for rendering.
     */
    private val mentionStyles = mutableMapOf<String, SpanStyle>()

    /**
     * Previous text length for detecting backspace.
     */
    private var previousTextLength = 0

    /**
     * Previous cursor position for detecting cursor movement.
     */
    private var previousCursorPosition = 0

    /**
     * Updates the text field value with cursor position validation.
     *
     * If the cursor is placed inside a mention span, it will be moved to the nearest edge.
     * If backspace is detected at a mention boundary, the entire mention is deleted.
     *
     * @param newValue The new text field value
     * @param onMentionDeleted Optional callback when a mention is deleted via backspace
     * @return The validated text field value (may have adjusted cursor position)
     */
    fun onValueChange(
        newValue: TextFieldValue,
        onMentionDeleted: ((SelectedMention) -> Unit)? = null
    ): TextFieldValue {
        val text = newValue.text
        val cursorPosition = newValue.selection.start
        val isBackspace = text.length < previousTextLength && 
                          previousTextLength - text.length == 1 &&
                          cursorPosition == previousCursorPosition - 1

        // Handle backspace at mention boundary
        if (isBackspace) {
            val deletedMention = handleBackspaceAtMentionBoundary(
                text = text,
                cursorPosition = cursorPosition
            )
            if (deletedMention != null) {
                onMentionDeleted?.invoke(deletedMention)
                return textFieldValue // Return current value, deletion already applied
            }
        }

        // Sync mentions with current text (remove mentions that no longer exist)
        syncMentionsWithText(text)

        // Validate cursor position
        val validatedValue = validateCursorPosition(newValue)

        // Update state
        textFieldValue = validatedValue
        previousTextLength = validatedValue.text.length
        previousCursorPosition = validatedValue.selection.start

        return validatedValue
    }

    /**
     * Validates cursor position and moves it outside mention spans if needed.
     *
     * From design document:
     * - If cursor is inside a mention span, move it to nearest edge
     * - Prevent cursor from being placed inside mentions
     */
    private fun validateCursorPosition(value: TextFieldValue): TextFieldValue {
        val cursorStart = value.selection.start
        val cursorEnd = value.selection.end

        for (mention in selectedMentions) {
            val spanStart = mention.range.first
            val spanEnd = mention.range.last + 1 // IntRange is inclusive, span end is exclusive

            // Check if cursor start is inside the span
            if (cursorStart > spanStart && cursorStart < spanEnd) {
                // Move cursor to nearest edge
                val newPosition = if (abs(cursorStart - spanStart) < abs(cursorStart - spanEnd)) {
                    spanStart
                } else {
                    spanEnd
                }
                return value.copy(selection = TextRange(newPosition))
            }

            // Handle selection range crossing span
            if (cursorEnd > spanStart && cursorEnd < spanEnd && cursorStart != cursorEnd) {
                return value.copy(selection = TextRange(cursorStart, spanStart))
            }
        }

        return value
    }

    /**
     * Handles backspace at mention boundary.
     *
     * From design document:
     * - If cursor is right after a mention and backspace is pressed
     * - Delete the entire mention span
     * - Notify listeners of span deletion
     *
     * @return The deleted mention if one was deleted, null otherwise
     */
    private fun handleBackspaceAtMentionBoundary(
        text: String,
        cursorPosition: Int
    ): SelectedMention? {
        // Find mention that ends at or just before the cursor position
        val mentionToDelete = selectedMentions.find { mention ->
            val spanEnd = mention.range.last + 1
            // Check if cursor was right after the mention and a character was deleted from it
            cursorPosition >= mention.range.first && cursorPosition < spanEnd
        }

        if (mentionToDelete != null) {
            // Delete the entire mention
            val spanStart = mentionToDelete.range.first
            val spanEnd = mentionToDelete.range.last + 1
            
            // Validate bounds
            val safeSpanStart = spanStart.coerceIn(0, text.length)
            val safeSpanEnd = spanEnd.coerceIn(0, text.length + 1) // +1 because one char was already deleted
            
            val newText = buildString {
                if (safeSpanStart > 0) {
                    append(text.substring(0, safeSpanStart))
                }
                if (safeSpanEnd <= text.length) {
                    append(text.substring(safeSpanEnd.coerceAtMost(text.length)))
                }
            }

            // Remove the mention from tracking
            selectedMentions = selectedMentions.filter { it.id != mentionToDelete.id }
            
            // Update positions of remaining mentions
            updateMentionPositionsAfterDeletion(safeSpanStart, spanEnd - spanStart)

            // Update text field value
            textFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(safeSpanStart)
            )
            previousTextLength = newText.length
            previousCursorPosition = safeSpanStart

            return mentionToDelete
        }

        return null
    }

    /**
     * Inserts a mention at the current trigger position.
     *
     * From design document:
     * - Find the trigger character position
     * - Replace text from trigger to cursor with prompt text
     * - Add space after inserted mention
     * - Move cursor after the inserted mention
     *
     * @param suggestionItem The suggestion item to insert
     * @param triggerIndex The index of the tracking character
     * @param mentionStyle Optional style for the mention
     */
    fun insertMention(
        suggestionItem: SuggestionItem,
        triggerIndex: Int,
        mentionStyle: SpanStyle? = null
    ) {
        val text = textFieldValue.text
        val cursorPosition = textFieldValue.selection.start

        // Validate bounds
        val safeTriggerIndex = triggerIndex.coerceIn(0, text.length)
        val safeCursorPosition = cursorPosition.coerceIn(0, text.length)

        val promptText = suggestionItem.promptText
        val promptTextWithSpace = if (promptText.endsWith(" ")) promptText else "$promptText "

        // Build new text
        val newText = buildString {
            if (safeTriggerIndex > 0) {
                append(text.substring(0, safeTriggerIndex))
            }
            append(promptTextWithSpace)
            if (safeCursorPosition < text.length) {
                append(text.substring(safeCursorPosition))
            }
        }

        // Calculate span range
        val spanStart = safeTriggerIndex
        val spanEnd = safeTriggerIndex + promptText.trimEnd().length - 1 // -1 for inclusive range

        // Create selected mention
        val selectedMention = SelectedMention(
            id = suggestionItem.id,
            name = suggestionItem.name,
            promptText = promptText.trimEnd(),
            underlyingText = suggestionItem.underlyingText,
            range = spanStart..spanEnd
        )

        // Update mention positions for existing mentions after the insertion point
        val insertionLength = promptTextWithSpace.length - (safeCursorPosition - safeTriggerIndex)
        updateMentionPositionsAfterInsertion(safeTriggerIndex, insertionLength)

        // Add the new mention
        selectedMentions = selectedMentions + selectedMention

        // Store style if provided
        mentionStyle?.let { mentionStyles[suggestionItem.id] = it }

        // Update text field value
        val newCursorPosition = safeTriggerIndex + promptTextWithSpace.length
        textFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
        previousTextLength = newText.length
        previousCursorPosition = newCursorPosition
    }

    /**
     * Syncs the selected mentions with the current text.
     * Removes mentions that are no longer present or have been modified.
     */
    private fun syncMentionsWithText(text: String) {
        selectedMentions = selectedMentions.filter { mention ->
            val spanStart = mention.range.first
            val spanEnd = mention.range.last + 1

            // Check bounds
            if (spanStart < 0 || spanEnd > text.length || spanStart >= spanEnd) {
                return@filter false
            }

            // Check if the text at the position matches the prompt text
            val textAtPosition = text.substring(spanStart, spanEnd)
            textAtPosition == mention.promptText
        }
    }

    /**
     * Updates mention positions after text insertion.
     */
    private fun updateMentionPositionsAfterInsertion(insertionStart: Int, insertionLength: Int) {
        selectedMentions = selectedMentions.map { mention ->
            when {
                // Mention is before insertion - no change
                mention.range.last < insertionStart -> mention
                // Mention is after insertion - shift positions
                mention.range.first >= insertionStart -> mention.copy(
                    range = (mention.range.first + insertionLength)..(mention.range.last + insertionLength)
                )
                // Insertion is within mention - this shouldn't happen with proper cursor validation
                else -> mention
            }
        }
    }

    /**
     * Updates mention positions after text deletion.
     */
    private fun updateMentionPositionsAfterDeletion(deletionStart: Int, deletionLength: Int) {
        selectedMentions = selectedMentions.mapNotNull { mention ->
            when {
                // Mention is before deletion - no change
                mention.range.last < deletionStart -> mention
                // Mention is after deletion - shift positions back
                mention.range.first >= deletionStart + deletionLength -> mention.copy(
                    range = (mention.range.first - deletionLength)..(mention.range.last - deletionLength)
                )
                // Deletion overlaps with mention - remove it
                else -> null
            }
        }
    }

    /**
     * Builds an AnnotatedString with styled mentions for display.
     *
     * @param defaultMentionStyle The default style for mentions
     * @return AnnotatedString with styled mentions
     */
    fun buildAnnotatedString(defaultMentionStyle: SpanStyle): AnnotatedString {
        val text = textFieldValue.text
        if (selectedMentions.isEmpty()) {
            return AnnotatedString(text)
        }

        return buildAnnotatedString {
            var lastEnd = 0
            val sortedMentions = selectedMentions.sortedBy { it.range.first }

            for (mention in sortedMentions) {
                val spanStart = mention.range.first
                val spanEnd = mention.range.last + 1

                // Validate bounds
                if (spanStart < lastEnd || spanEnd > text.length) {
                    continue
                }

                // Append text before mention
                if (spanStart > lastEnd) {
                    append(text.substring(lastEnd, spanStart))
                }

                // Append styled mention
                val style = mentionStyles[mention.id] ?: defaultMentionStyle
                withStyle(style) {
                    append(text.substring(spanStart, spanEnd))
                }

                // Add annotation for identification
                addStringAnnotation(
                    tag = MENTION_ANNOTATION_TAG,
                    annotation = mention.id,
                    start = length - (spanEnd - spanStart),
                    end = length
                )

                lastEnd = spanEnd
            }

            // Append remaining text
            if (lastEnd < text.length) {
                append(text.substring(lastEnd))
            }
        }
    }

    /**
     * Gets the processed text for sending, replacing prompt text with underlying text.
     *
     * @return The text with mentions replaced by their underlying format
     */
    fun getProcessedText(): String {
        var result = textFieldValue.text
        // Sort by position descending to replace from end to start (preserves indices)
        val sortedMentions = selectedMentions.sortedByDescending { it.range.first }
        
        for (mention in sortedMentions) {
            val spanStart = mention.range.first
            val spanEnd = mention.range.last + 1
            
            if (spanStart >= 0 && spanEnd <= result.length) {
                result = result.substring(0, spanStart) + 
                         mention.underlyingText + 
                         result.substring(spanEnd)
            }
        }
        
        return result
    }

    /**
     * Gets the map of prompt text to underlying text for all mentions.
     */
    fun getPromptToUnderlyingMap(): Map<String, String> {
        return selectedMentions.associate { it.promptText to it.underlyingText }
    }

    /**
     * Checks if a position is within a mention span.
     */
    fun isPositionInMention(position: Int): Boolean {
        return selectedMentions.any { position in it.range }
    }

    /**
     * Gets the mention at a specific position.
     */
    fun getMentionAt(position: Int): SelectedMention? {
        return selectedMentions.find { position in it.range }
    }

    /**
     * Updates the text field value directly (for initialization or external updates).
     * This bypasses the normal validation and should be used carefully.
     */
    fun updateTextFieldValue(value: TextFieldValue) {
        textFieldValue = value
        previousTextLength = value.text.length
        previousCursorPosition = value.selection.start
        syncMentionsWithText(value.text)
    }

    /**
     * Clears all state.
     */
    fun clear() {
        textFieldValue = TextFieldValue()
        selectedMentions = emptyList()
        mentionStyles.clear()
        previousTextLength = 0
        previousCursorPosition = 0
    }

    /**
     * Sets the style for a specific mention.
     */
    fun setMentionStyle(mentionId: String, style: SpanStyle) {
        mentionStyles[mentionId] = style
    }

    /**
     * Gets the count of selected mentions.
     */
    fun getMentionCount(): Int = selectedMentions.size

    companion object {
        /**
         * Annotation tag used for mention annotations in AnnotatedString.
         */
        const val MENTION_ANNOTATION_TAG = "MENTION"
    }
}

/**
 * Remembers a MentionTextFieldState instance.
 */
@Composable
fun rememberMentionTextFieldState(): MentionTextFieldState {
    return remember { MentionTextFieldState() }
}
