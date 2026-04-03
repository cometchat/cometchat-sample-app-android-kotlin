package com.cometchat.uikit.compose.shared.mentions

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A mention-aware text field composable that handles @mention detection and formatting.
 *
 * This composable integrates with [MentionTextFieldState] to provide:
 * - Mention detection when user types the tracking character (@)
 * - Cursor position validation (prevents cursor inside mentions)
 * - Backspace handling for mention deletion
 * - Styled text display with mentions using AnnotatedString
 *
 * Based on the View-based CometChatEditText behavior from the design document.
 *
 * @param mentionState The state object managing mention text field state
 * @param onMentionDetected Callback when a mention is detected (query string, trigger index)
 * @param onMentionContextLost Callback when mention context is lost (e.g., cursor moved away)
 * @param mentionStyle The SpanStyle to apply to mentions
 * @param trackingCharacter The character that triggers mention detection (default: '@')
 * @param modifier Modifier for the text field
 * @param enabled Whether the text field is enabled
 * @param readOnly Whether the text field is read-only
 * @param textStyle The text style for the input
 * @param placeholder Placeholder composable shown when text is empty
 * @param placeholderText Placeholder text shown when text is empty (alternative to placeholder)
 * @param placeholderStyle Style for the placeholder text
 * @param keyboardOptions Keyboard options for the text field
 * @param keyboardActions Keyboard actions for the text field
 * @param singleLine Whether the text field is single line
 * @param maxLines Maximum number of lines
 * @param minLines Minimum number of lines
 * @param visualTransformation Visual transformation for the text
 * @param interactionSource Interaction source for the text field
 * @param cursorBrush Brush for the cursor
 * @param onTextChange Optional callback for text changes (raw text)
 * @param onMentionDeleted Optional callback when a mention is deleted
 */
@Composable
fun MentionAwareTextField(
    mentionState: MentionTextFieldState,
    onMentionDetected: (query: String, triggerIndex: Int) -> Unit,
    onMentionContextLost: () -> Unit,
    mentionStyle: SpanStyle,
    trackingCharacter: Char = '@',
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    placeholderText: String? = null,
    placeholderStyle: TextStyle = textStyle.copy(color = Color.Gray),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(Color.Black),
    onTextChange: ((String) -> Unit)? = null,
    onMentionDeleted: ((SelectedMention) -> Unit)? = null
) {
    // Track previous cursor position for mention detection
    var previousCursorPosition by remember { mutableIntStateOf(0) }
    
    // Track if mention context is currently active
    var isMentionContextActive by remember { mutableStateOf(false) }
    
    // Track the current trigger index when mention is active
    var currentTriggerIndex by remember { mutableIntStateOf(-1) }
    
    // Focus requester for managing focus
    val focusRequester = remember { FocusRequester() }
    
    // Track focus state
    var isFocused by remember { mutableStateOf(false) }

    // Build annotated string with styled mentions
    val annotatedString = remember(mentionState.textFieldValue.text, mentionState.selectedMentions) {
        mentionState.buildAnnotatedString(mentionStyle)
    }

    Box(modifier = modifier) {
        BasicTextField(
            value = mentionState.textFieldValue.copy(
                annotatedString = annotatedString
            ),
            onValueChange = { newValue ->
                // Process the value change through mention state
                val validatedValue = mentionState.onValueChange(newValue, onMentionDeleted)
                
                // Notify text change
                onTextChange?.invoke(validatedValue.text)
                
                // Handle mention detection
                handleMentionDetection(
                    text = validatedValue.text,
                    cursorPosition = validatedValue.selection.start,
                    previousCursorPosition = previousCursorPosition,
                    trackingCharacter = trackingCharacter,
                    mentionState = mentionState,
                    isMentionContextActive = isMentionContextActive,
                    currentTriggerIndex = currentTriggerIndex,
                    onMentionDetected = { query, triggerIndex ->
                        isMentionContextActive = true
                        currentTriggerIndex = triggerIndex
                        onMentionDetected(query, triggerIndex)
                    },
                    onMentionContextLost = {
                        if (isMentionContextActive) {
                            isMentionContextActive = false
                            currentTriggerIndex = -1
                            onMentionContextLost()
                        }
                    }
                )
                
                previousCursorPosition = validatedValue.selection.start
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (!focusState.isFocused && isMentionContextActive) {
                        // Lost focus, clear mention context
                        isMentionContextActive = false
                        currentTriggerIndex = -1
                        onMentionContextLost()
                    }
                },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = cursorBrush,
            decorationBox = { innerTextField ->
                Box {
                    // Show placeholder when text is empty
                    if (mentionState.textFieldValue.text.isEmpty()) {
                        if (placeholder != null) {
                            placeholder()
                        } else if (placeholderText != null) {
                            Text(
                                text = placeholderText,
                                style = placeholderStyle
                            )
                        }
                    }
                    innerTextField()
                }
            }
        )
    }
    
    // Handle cursor position changes for mention context validation
    LaunchedEffect(mentionState.textFieldValue.selection) {
        if (isMentionContextActive && isFocused) {
            val cursorPosition = mentionState.textFieldValue.selection.start
            val text = mentionState.textFieldValue.text
            
            // Check if cursor is still in valid mention context
            val (isValid, triggerIndex) = isValidMentionContext(
                text = text,
                cursorPosition = cursorPosition,
                trackingCharacter = trackingCharacter,
                mentionState = mentionState
            )
            
            if (!isValid) {
                isMentionContextActive = false
                currentTriggerIndex = -1
                onMentionContextLost()
            } else if (triggerIndex != currentTriggerIndex) {
                // Trigger index changed, update context
                currentTriggerIndex = triggerIndex
                val query = getQueryString(text, cursorPosition, trackingCharacter, triggerIndex)
                onMentionDetected(query, triggerIndex)
            }
        }
    }
}

/**
 * Handles mention detection based on text changes and cursor position.
 *
 * From design document:
 * - Detect when user types tracking character (@) at valid positions
 * - Valid positions: start of text, after space/newline
 * - Do not trigger when cursor is inside an existing mention span
 */
private fun handleMentionDetection(
    text: String,
    cursorPosition: Int,
    previousCursorPosition: Int,
    trackingCharacter: Char,
    mentionState: MentionTextFieldState,
    isMentionContextActive: Boolean,
    currentTriggerIndex: Int,
    onMentionDetected: (query: String, triggerIndex: Int) -> Unit,
    onMentionContextLost: () -> Unit
) {
    if (text.isEmpty()) {
        onMentionContextLost()
        return
    }
    
    // Check if cursor is inside a mention - if so, don't detect
    if (mentionState.isPositionInMention(cursorPosition)) {
        onMentionContextLost()
        return
    }
    
    // Find tracking character position by scanning backwards from cursor
    val (foundTrigger, triggerIndex) = findTrackingCharacter(
        text = text,
        cursorPosition = cursorPosition,
        trackingCharacter = trackingCharacter,
        mentionState = mentionState
    )
    
    if (foundTrigger && triggerIndex >= 0) {
        // Valid mention context found
        val query = getQueryString(text, cursorPosition, trackingCharacter, triggerIndex)
        onMentionDetected(query, triggerIndex)
    } else {
        // No valid mention context
        onMentionContextLost()
    }
}

/**
 * Finds the tracking character position by scanning backwards from cursor.
 *
 * From design document:
 * - Scan backwards from cursor to find tracking character
 * - Check if we're inside a span - if so, stop
 * - Valid only if at start or preceded by whitespace
 * - Check if followed by space (mention closed)
 *
 * @return Pair of (found, triggerIndex)
 */
private fun findTrackingCharacter(
    text: String,
    cursorPosition: Int,
    trackingCharacter: Char,
    mentionState: MentionTextFieldState
): Pair<Boolean, Int> {
    if (text.isEmpty() || cursorPosition <= 0) {
        return Pair(false, -1)
    }
    
    // Scan backwards from cursor to find tracking character
    for (i in (cursorPosition - 1) downTo 0) {
        val char = text[i]
        
        // Check if we're inside a mention span - if so, stop
        if (mentionState.isPositionInMention(i)) {
            return Pair(false, -1)
        }
        
        // Check if this is the tracking character
        if (char == trackingCharacter) {
            // Valid only if at start or preceded by whitespace
            if (i == 0 || text[i - 1] == ' ' || text[i - 1] == '\n') {
                // Check if followed by space (mention closed) - but only if there's text after
                if (i < cursorPosition - 1) {
                    val nextChar = text[i + 1]
                    if (nextChar == ' ' || nextChar == '\n') {
                        // Mention is closed (space after @ with no query)
                        return Pair(false, -1)
                    }
                }
                return Pair(true, i)
            }
            // Found tracking character but not at valid position
            return Pair(false, -1)
        }
        
        // If we hit whitespace before finding tracking character, stop
        if (char == ' ' || char == '\n') {
            return Pair(false, -1)
        }
    }
    
    return Pair(false, -1)
}

/**
 * Checks if the current cursor position is in a valid mention context.
 *
 * @return Pair of (isValid, triggerIndex)
 */
private fun isValidMentionContext(
    text: String,
    cursorPosition: Int,
    trackingCharacter: Char,
    mentionState: MentionTextFieldState
): Pair<Boolean, Int> {
    return findTrackingCharacter(text, cursorPosition, trackingCharacter, mentionState)
}

/**
 * Extracts the query string from the text.
 *
 * From design document:
 * - Query is the text between tracking character and cursor
 *
 * @param text The full text
 * @param cursorPosition Current cursor position
 * @param trackingCharacter The tracking character
 * @param triggerIndex The index of the tracking character
 * @return The query string (text after @ and before cursor)
 */
private fun getQueryString(
    text: String,
    cursorPosition: Int,
    trackingCharacter: Char,
    triggerIndex: Int
): String {
    if (text.isEmpty() || triggerIndex < 0 || triggerIndex >= cursorPosition) {
        return ""
    }
    
    return try {
        // Query is text between tracking character (exclusive) and cursor (exclusive)
        text.substring(triggerIndex + 1, cursorPosition)
    } catch (e: Exception) {
        ""
    }
}

/**
 * Extension function to create a TextFieldValue with an AnnotatedString.
 * This preserves the selection while using the styled annotated string.
 */
private fun TextFieldValue.copy(annotatedString: androidx.compose.ui.text.AnnotatedString): TextFieldValue {
    return TextFieldValue(
        annotatedString = annotatedString,
        selection = this.selection,
        composition = this.composition
    )
}

