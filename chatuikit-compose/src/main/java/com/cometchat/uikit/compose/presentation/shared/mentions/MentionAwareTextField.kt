package com.cometchat.uikit.compose.presentation.shared.mentions

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.cometchat.uikit.core.mentions.SelectedMention

/**
 * State holder for MentionAwareTextField.
 *
 * This class manages the state of a text field with mention support, including:
 * - Tracking selected mentions and their positions
 * - Validating cursor positions to prevent cursor inside mentions
 * - Handling backspace to delete entire mentions
 * - Building annotated strings with styled mentions
 *
 * ## Basic Usage
 *
 * ```kotlin
 * // Create state using remember
 * val mentionState = rememberMentionTextFieldState()
 *
 * // Or with initial text
 * val mentionState = rememberMentionTextFieldState(initialText = "Hello ")
 *
 * // Access current text
 * val currentText = mentionState.textFieldValue.text
 *
 * // Get processed text for sending (with underlying mention format)
 * val processedText = mentionState.getProcessedText()
 *
 * // Get all selected mentions
 * val mentions = mentionState.getMentions()
 *
 * // Clear all state
 * mentionState.clear()
 * ```
 *
 * ## Mention Management
 *
 * ```kotlin
 * // Add a mention
 * mentionState.addMention(SelectedMention(
 *     id = "user123",
 *     name = "John Doe",
 *     promptText = "@John Doe",
 *     underlyingText = "<@uid:user123>",
 *     spanStart = 0,
 *     spanEnd = 9
 * ))
 *
 * // Remove a mention
 * mentionState.removeMention("user123")
 *
 * // Check if position is inside a mention
 * val isInMention = mentionState.isPositionInMention(cursorPosition)
 * ```
 *
 * @param initialText Initial text content for the text field
 *
 * @see MentionAwareTextField Composable that uses this state
 * @see SelectedMention Data class representing a selected mention
 * @see insertMentionIntoState Helper function to insert mentions
 */
class MentionTextFieldState(
    initialText: String = ""
) {
    private val selectedMentions = mutableListOf<SelectedMention>()
    
    /**
     * Current text field value with text and selection.
     */
    var textFieldValue by mutableStateOf(TextFieldValue(initialText))
        private set
    
    /**
     * Updates the text field value with cursor validation.
     * If the new cursor position is inside a mention, it will be moved to the nearest edge.
     *
     * @param newValue The new text field value
     * @return The adjusted text field value with valid cursor position
     */
    fun updateTextFieldValue(newValue: TextFieldValue): TextFieldValue {
        val adjustedValue = validateAndAdjustCursor(newValue)
        textFieldValue = adjustedValue
        return adjustedValue
    }
    
    /**
     * Sets the text field value directly without cursor validation.
     * Use this for programmatic updates where cursor position is already valid.
     */
    fun setValueDirectly(value: TextFieldValue) {
        textFieldValue = value
    }
    
    /**
     * Validates cursor position and adjusts if inside a mention.
     * Moves cursor to the nearest edge of the mention span.
     */
    private fun validateAndAdjustCursor(value: TextFieldValue): TextFieldValue {
        val cursorPosition = value.selection.start
        val selectionEnd = value.selection.end
        
        // Check if cursor is inside any mention
        for (mention in selectedMentions) {
            // Cursor is inside mention (not at edges)
            if (cursorPosition > mention.spanStart && cursorPosition < mention.spanEnd) {
                // Move to nearest edge
                val distanceToStart = cursorPosition - mention.spanStart
                val distanceToEnd = mention.spanEnd - cursorPosition
                val newPosition = if (distanceToStart <= distanceToEnd) {
                    mention.spanStart
                } else {
                    mention.spanEnd
                }
                return value.copy(selection = TextRange(newPosition))
            }
            
            // Handle selection range crossing mention
            if (selectionEnd > mention.spanStart && selectionEnd < mention.spanEnd) {
                return value.copy(selection = TextRange(cursorPosition, mention.spanStart))
            }
        }
        
        return value
    }
    
    /**
     * Handles text changes and detects if a mention should be deleted.
     * When backspace is pressed at the end of a mention, the entire mention is deleted.
     *
     * @param oldValue The previous text field value
     * @param newValue The new text field value
     * @return Pair of (adjusted TextFieldValue, deleted mention or null)
     */
    fun handleTextChange(
        oldValue: TextFieldValue,
        newValue: TextFieldValue
    ): Pair<TextFieldValue, SelectedMention?> {
        val oldText = oldValue.text
        val newText = newValue.text
        val oldCursor = oldValue.selection.start
        val newCursor = newValue.selection.start
        
        // Detect backspace (single character deletion)
        if (oldText.length - newText.length == 1 && newCursor < oldCursor) {
            // Check if we deleted from the end of a mention
            val deletedMention = findMentionEndingAt(oldCursor)
            if (deletedMention != null) {
                // Delete the entire mention
                val mentionStart = deletedMention.spanStart
                val mentionEnd = deletedMention.spanEnd
                
                // Build new text without the mention
                val textBeforeMention = if (mentionStart > 0) oldText.substring(0, mentionStart) else ""
                val textAfterMention = if (mentionEnd < oldText.length) oldText.substring(mentionEnd) else ""
                val resultText = textBeforeMention + textAfterMention
                
                // Remove the mention from tracking
                removeMention(deletedMention.id)
                
                // Update positions of remaining mentions
                val deletedLength = mentionEnd - mentionStart
                updateMentionPositions(mentionStart, -deletedLength)
                
                val adjustedValue = TextFieldValue(
                    text = resultText,
                    selection = TextRange(mentionStart)
                )
                textFieldValue = adjustedValue
                return Pair(adjustedValue, deletedMention)
            }
        }
        
        // Sync mentions with text changes (remove mentions that no longer match)
        syncMentionsWithText(newText)
        
        // Update mention positions based on text changes
        if (oldText.length != newText.length) {
            val changeStart = minOf(oldCursor, newCursor)
            val lengthDiff = newText.length - oldText.length
            updateMentionPositions(changeStart, lengthDiff)
        }
        
        val adjustedValue = validateAndAdjustCursor(newValue)
        textFieldValue = adjustedValue
        return Pair(adjustedValue, null)
    }
    
    /**
     * Finds a mention that ends at the given position.
     */
    private fun findMentionEndingAt(position: Int): SelectedMention? {
        return selectedMentions.find { it.spanEnd == position }
    }
    
    /**
     * Adds a mention to the tracking list.
     */
    fun addMention(mention: SelectedMention) {
        // Remove any existing mention with the same ID
        selectedMentions.removeAll { it.id == mention.id }
        selectedMentions.add(mention)
    }
    
    /**
     * Removes a mention by ID.
     */
    fun removeMention(id: String) {
        selectedMentions.removeAll { it.id == id }
    }
    
    /**
     * Gets all selected mentions.
     */
    fun getMentions(): List<SelectedMention> = selectedMentions.toList()
    
    /**
     * Clears all mentions.
     */
    fun clearMentions() {
        selectedMentions.clear()
    }
    
    /**
     * Clears all state (text and mentions).
     */
    fun clear() {
        textFieldValue = TextFieldValue("")
        selectedMentions.clear()
    }
    
    /**
     * Updates mention positions after text changes.
     *
     * @param changeStart The position where the change occurred
     * @param lengthDiff The difference in length (positive for insertion, negative for deletion)
     */
    private fun updateMentionPositions(changeStart: Int, lengthDiff: Int) {
        val updatedMentions = selectedMentions.mapNotNull { mention ->
            when {
                // Mention is entirely before the change - no update needed
                mention.spanEnd <= changeStart -> mention
                
                // Mention is entirely after the change - shift positions
                mention.spanStart >= changeStart -> mention.copy(
                    spanStart = mention.spanStart + lengthDiff,
                    spanEnd = mention.spanEnd + lengthDiff
                )
                
                // Change is within the mention - remove it (mention was edited)
                else -> null
            }
        }
        selectedMentions.clear()
        selectedMentions.addAll(updatedMentions)
    }
    
    /**
     * Syncs mentions with the current text, removing any that no longer match.
     */
    private fun syncMentionsWithText(text: String) {
        val validMentions = selectedMentions.filter { mention ->
            // Check bounds
            if (mention.spanStart < 0 || mention.spanEnd > text.length || mention.spanStart >= mention.spanEnd) {
                return@filter false
            }
            
            // Check if the text at the position matches the prompt text
            val textAtPosition = text.substring(mention.spanStart, mention.spanEnd)
            textAtPosition == mention.promptText
        }
        
        if (validMentions.size != selectedMentions.size) {
            selectedMentions.clear()
            selectedMentions.addAll(validMentions)
        }
    }
    
    /**
     * Checks if a position is inside a mention.
     */
    fun isPositionInMention(position: Int): Boolean {
        return selectedMentions.any { position > it.spanStart && position < it.spanEnd }
    }
    
    /**
     * Gets the mention at a specific position.
     */
    fun getMentionAt(position: Int): SelectedMention? {
        return selectedMentions.find { position in it.spanStart..it.spanEnd }
    }
    
    /**
     * Builds an AnnotatedString with styled mentions.
     *
     * @param mentionStyle The style to apply to mentions
     * @param selfMentionStyle Optional style for self-mentions
     * @param loggedInUserId The logged-in user's ID for identifying self-mentions
     * @return AnnotatedString with styled mentions
     */
    fun buildAnnotatedString(
        mentionStyle: SpanStyle,
        selfMentionStyle: SpanStyle? = null,
        loggedInUserId: String? = null
    ): AnnotatedString {
        val text = textFieldValue.text
        if (selectedMentions.isEmpty()) {
            return AnnotatedString(text)
        }
        
        return buildAnnotatedString {
            append(text)
            
            for (mention in selectedMentions.sortedBy { it.spanStart }) {
                // Validate bounds
                if (mention.spanStart >= 0 && mention.spanEnd <= text.length && mention.spanStart < mention.spanEnd) {
                    val isSelf = mention.id == loggedInUserId
                    val style = if (isSelf && selfMentionStyle != null) selfMentionStyle else mentionStyle
                    
                    addStyle(style, mention.spanStart, mention.spanEnd)
                    addStringAnnotation(
                        tag = MENTION_TAG,
                        annotation = mention.id,
                        start = mention.spanStart,
                        end = mention.spanEnd
                    )
                }
            }
        }
    }
    
    /**
     * Gets the processed text for sending, replacing prompt text with underlying text.
     */
    fun getProcessedText(): String {
        var result = textFieldValue.text
        
        // Sort mentions by position in reverse order to maintain correct indices during replacement
        val sortedMentions = selectedMentions.sortedByDescending { it.spanStart }
        
        for (mention in sortedMentions) {
            if (mention.spanStart >= 0 && mention.spanEnd <= result.length) {
                val before = if (mention.spanStart > 0) result.substring(0, mention.spanStart) else ""
                val after = if (mention.spanEnd < result.length) result.substring(mention.spanEnd) else ""
                result = before + mention.underlyingText + after
            }
        }
        
        return result
    }
}

/**
 * Remembers a MentionTextFieldState.
 *
 * @param initialText Initial text content
 */
@Composable
fun rememberMentionTextFieldState(
    initialText: String = ""
): MentionTextFieldState {
    return remember { MentionTextFieldState(initialText) }
}

/**
 * Visual transformation that applies mention styling to the text.
 *
 * @param state The MentionTextFieldState containing mention information
 * @param mentionStyle The style to apply to mentions
 * @param selfMentionStyle Optional style for self-mentions
 * @param loggedInUserId The logged-in user's ID for identifying self-mentions
 */
class MentionVisualTransformation(
    private val state: MentionTextFieldState,
    private val mentionStyle: SpanStyle,
    private val selfMentionStyle: SpanStyle? = null,
    private val loggedInUserId: String? = null
) : VisualTransformation {
    
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = state.buildAnnotatedString(
            mentionStyle = mentionStyle,
            selfMentionStyle = selfMentionStyle,
            loggedInUserId = loggedInUserId
        )
        
        return TransformedText(
            annotatedString,
            OffsetMapping.Identity
        )
    }
}


/**
 * A text field composable with built-in mention support.
 *
 * This composable wraps BasicTextField and provides:
 * - Mention detection when typing tracking character (e.g., @)
 * - Cursor validation to prevent cursor inside mentions
 * - Backspace handling to delete entire mentions
 * - Visual transformation for mention styling
 * - Callbacks for mention-related events
 *
 * ## Basic Usage
 *
 * ```kotlin
 * @Composable
 * fun MessageInput() {
 *     val mentionState = rememberMentionTextFieldState()
 *
 *     MentionAwareTextField(
 *         state = mentionState,
 *         onValueChange = { newValue ->
 *             // Handle text changes
 *         },
 *         modifier = Modifier.fillMaxWidth(),
 *         mentionStyle = SpanStyle(
 *             color = MaterialTheme.colorScheme.primary,
 *             background = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
 *         )
 *     )
 * }
 * ```
 *
 * ## With Self-Mention Styling
 *
 * ```kotlin
 * MentionAwareTextField(
 *     state = mentionState,
 *     onValueChange = { /* ... */ },
 *     mentionStyle = SpanStyle(color = Color.Blue),
 *     selfMentionStyle = SpanStyle(color = Color.Red),
 *     loggedInUserId = currentUserId,
 *     onMentionDeleted = { deletedMention ->
 *         // Handle mention deletion
 *     }
 * )
 * ```
 *
 * @param state The MentionTextFieldState for managing text and mentions
 * @param onValueChange Callback when text value changes
 * @param modifier Modifier for the text field
 * @param enabled Whether the text field is enabled
 * @param readOnly Whether the text field is read-only
 * @param textStyle Text style for the input
 * @param keyboardOptions Keyboard options
 * @param keyboardActions Keyboard actions
 * @param singleLine Whether to restrict to single line
 * @param maxLines Maximum number of lines
 * @param minLines Minimum number of lines
 * @param mentionStyle Style to apply to mentions (default: blue with 20% alpha background)
 * @param selfMentionStyle Optional style for self-mentions (when logged-in user is mentioned)
 * @param loggedInUserId The logged-in user's ID for identifying self-mentions
 * @param cursorBrush Brush for the cursor
 * @param interactionSource Interaction source for the text field
 * @param onMentionDeleted Callback when a mention is deleted via backspace
 * @param onCursorPositionChanged Callback when cursor position changes
 * @param decorationBox Decoration box for custom styling
 *
 * @see MentionTextFieldState State holder for this composable
 * @see rememberMentionTextFieldState Remember function for creating state
 * @see insertMentionIntoState Helper function to insert mentions
 */
@Composable
fun MentionAwareTextField(
    state: MentionTextFieldState,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    mentionStyle: SpanStyle = SpanStyle(
        color = Color(0xFF3B82F6),
        background = Color(0xFF3B82F6).copy(alpha = 0.2f)
    ),
    selfMentionStyle: SpanStyle? = null,
    loggedInUserId: String? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onMentionDeleted: ((SelectedMention) -> Unit)? = null,
    onCursorPositionChanged: ((Int) -> Unit)? = null,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = { innerTextField -> innerTextField() }
) {
    // Track previous value for change detection
    var previousValue by remember { mutableStateOf(state.textFieldValue) }
    
    // Create visual transformation for mention styling
    val visualTransformation = remember(state, mentionStyle, selfMentionStyle, loggedInUserId) {
        MentionVisualTransformation(
            state = state,
            mentionStyle = mentionStyle,
            selfMentionStyle = selfMentionStyle,
            loggedInUserId = loggedInUserId
        )
    }
    
    // Notify cursor position changes
    LaunchedEffect(state.textFieldValue.selection) {
        onCursorPositionChanged?.invoke(state.textFieldValue.selection.start)
    }
    
    BasicTextField(
        value = state.textFieldValue,
        onValueChange = { newValue ->
            // Handle text change with mention deletion detection
            val (adjustedValue, deletedMention) = state.handleTextChange(previousValue, newValue)
            
            // Notify about deleted mention
            deletedMention?.let { onMentionDeleted?.invoke(it) }
            
            // Update previous value for next comparison
            previousValue = adjustedValue
            
            // Notify parent about the change
            onValueChange(adjustedValue)
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        cursorBrush = cursorBrush,
        interactionSource = interactionSource,
        decorationBox = decorationBox
    )
}

/**
 * Inserts a mention into the MentionTextFieldState.
 *
 * This function handles the insertion of a mention at the current trigger position,
 * replacing the trigger character and query with the mention prompt text.
 *
 * @param state The MentionTextFieldState to update
 * @param mentionState The current mention detection state
 * @param id The unique identifier for the mention (e.g., user ID)
 * @param name The display name
 * @param promptText The text to display in the field (e.g., "@John")
 * @param underlyingText The text to use when processing (e.g., "<@uid:123>")
 * @return The updated TextFieldValue
 */
fun insertMentionIntoState(
    state: MentionTextFieldState,
    mentionState: ComposeMentionState,
    id: String,
    name: String,
    promptText: String,
    underlyingText: String
): TextFieldValue {
    if (!mentionState.isActive) return state.textFieldValue
    
    val currentText = state.textFieldValue.text
    val triggerIndex = mentionState.triggerIndex
    val cursorPosition = mentionState.cursorPosition
    
    // Validate bounds
    val safeTriggerIndex = triggerIndex.coerceIn(0, currentText.length)
    val safeCursorPosition = cursorPosition.coerceIn(0, currentText.length)
    
    // Build new text
    val beforeTrigger = if (safeTriggerIndex > 0) currentText.substring(0, safeTriggerIndex) else ""
    val afterCursor = if (safeCursorPosition < currentText.length) currentText.substring(safeCursorPosition) else ""
    
    // Add space after mention if not already present
    val mentionWithSpace = if (promptText.endsWith(" ")) promptText else "$promptText "
    
    val newText = beforeTrigger + mentionWithSpace + afterCursor
    val newCursorPosition = beforeTrigger.length + mentionWithSpace.length
    
    // Create and add the mention
    val mention = SelectedMention(
        id = id,
        name = name,
        promptText = promptText,
        underlyingText = underlyingText,
        spanStart = beforeTrigger.length,
        spanEnd = beforeTrigger.length + promptText.length
    )
    state.addMention(mention)
    
    // Update the text field value
    val newValue = TextFieldValue(
        text = newText,
        selection = TextRange(newCursorPosition)
    )
    state.setValueDirectly(newValue)
    
    return newValue
}
