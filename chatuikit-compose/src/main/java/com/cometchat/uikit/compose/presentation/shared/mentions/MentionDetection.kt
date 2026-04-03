package com.cometchat.uikit.compose.presentation.shared.mentions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.cometchat.uikit.core.mentions.MentionDetectionState
import com.cometchat.uikit.core.mentions.MentionDetector
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter

/**
 * State holder for mention detection in Compose.
 * 
 * @property isActive Whether a mention is currently being typed
 * @property query The search query (text after tracking character)
 * @property triggerIndex The index of the tracking character
 * @property cursorPosition The current cursor position
 * @property activeFormatter The formatter that triggered the mention
 */
data class ComposeMentionState(
    val isActive: Boolean = false,
    val query: String = "",
    val triggerIndex: Int = -1,
    val cursorPosition: Int = -1,
    val activeFormatter: CometChatTextFormatter? = null
) {
    companion object {
        val INACTIVE = ComposeMentionState()
    }
}

/**
 * Remembers and manages mention detection state for a text field.
 * 
 * @param textFieldValue The current text field value
 * @param textFormatters List of text formatters to check for tracking characters
 * @return The current mention detection state
 */
@Composable
fun rememberMentionDetectionState(
    textFieldValue: TextFieldValue,
    textFormatters: List<CometChatTextFormatter>
): ComposeMentionState {
    var mentionState by remember { mutableStateOf(ComposeMentionState.INACTIVE) }
    
    // Create detectors for each formatter
    val detectors = remember(textFormatters) {
        textFormatters.associate { formatter ->
            formatter.getTrackingCharacter() to MentionDetector(formatter.getTrackingCharacter())
        }
    }
    
    LaunchedEffect(textFieldValue.text, textFieldValue.selection) {
        val text = textFieldValue.text
        val cursorPosition = textFieldValue.selection.start
        
        var newState = ComposeMentionState.INACTIVE
        
        for (formatter in textFormatters) {
            if (formatter.getDisableSuggestions()) continue
            
            val trackingChar = formatter.getTrackingCharacter()
            val detector = detectors[trackingChar] ?: continue
            
            val result = detector.detectMention(text, cursorPosition)
            if (result.isActive) {
                newState = ComposeMentionState(
                    isActive = true,
                    query = result.query,
                    triggerIndex = result.triggerIndex,
                    cursorPosition = result.cursorPosition,
                    activeFormatter = formatter
                )
                break
            }
        }
        
        mentionState = newState
    }
    
    return mentionState
}

/**
 * Detects mentions in a simple string with cursor position.
 * 
 * @param text The current text
 * @param cursorPosition The cursor position
 * @param textFormatters List of text formatters
 * @return The mention detection state
 */
fun detectMention(
    text: String,
    cursorPosition: Int,
    textFormatters: List<CometChatTextFormatter>
): ComposeMentionState {
    for (formatter in textFormatters) {
        if (formatter.getDisableSuggestions()) continue
        
        val detector = MentionDetector(formatter.getTrackingCharacter())
        val result = detector.detectMention(text, cursorPosition)
        
        if (result.isActive) {
            return ComposeMentionState(
                isActive = true,
                query = result.query,
                triggerIndex = result.triggerIndex,
                cursorPosition = result.cursorPosition,
                activeFormatter = formatter
            )
        }
    }
    
    return ComposeMentionState.INACTIVE
}

/**
 * Creates a new TextFieldValue with a mention inserted.
 * 
 * @param currentValue The current text field value
 * @param mentionState The current mention state
 * @param mentionText The text to insert (e.g., "@username ")
 * @return New TextFieldValue with the mention inserted
 */
fun insertMention(
    currentValue: TextFieldValue,
    mentionState: ComposeMentionState,
    mentionText: String
): TextFieldValue {
    if (!mentionState.isActive) return currentValue
    
    val text = currentValue.text
    
    // Validate bounds to prevent StringIndexOutOfBoundsException
    val safeTriggerIndex = mentionState.triggerIndex.coerceIn(0, text.length)
    val safeCursorPosition = mentionState.cursorPosition.coerceIn(0, text.length)
    
    val newText = buildString {
        append(text.substring(0, safeTriggerIndex))
        append(mentionText)
        if (safeCursorPosition < text.length) {
            append(text.substring(safeCursorPosition))
        }
    }
    
    val newCursorPosition = safeTriggerIndex + mentionText.length
    
    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursorPosition)
    )
}
