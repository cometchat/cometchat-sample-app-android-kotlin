package com.cometchat.uikit.core.mentions

/**
 * MentionInserter handles the insertion of formatted mentions into text.
 * 
 * This utility class provides methods to insert mentions with proper formatting
 * and store the underlying text for message processing.
 */
object MentionInserter {

    /**
     * Result of a mention insertion operation.
     * 
     * @property newText The text after insertion
     * @property newCursorPosition The cursor position after insertion
     * @property promptText The display text (e.g., "@username")
     * @property underlyingText The underlying text for message processing (e.g., "<@uid:123>")
     */
    data class InsertionResult(
        val newText: String,
        val newCursorPosition: Int,
        val promptText: String,
        val underlyingText: String,
        val spanStart: Int,
        val spanEnd: Int
    )

    /**
     * Calculates the result of inserting a mention into text.
     * 
     * @param currentText The current text
     * @param triggerIndex The index of the tracking character
     * @param cursorPosition The current cursor position
     * @param promptText The display text to insert (e.g., "@username ")
     * @param underlyingText The underlying text for processing (e.g., "<@uid:123>")
     * @return InsertionResult with the new text and cursor position
     */
    fun calculateInsertion(
        currentText: String,
        triggerIndex: Int,
        cursorPosition: Int,
        promptText: String,
        underlyingText: String
    ): InsertionResult {
        // Build new text by replacing the trigger + query with the prompt text
        val beforeTrigger = if (triggerIndex > 0) currentText.substring(0, triggerIndex) else ""
        val afterCursor = if (cursorPosition < currentText.length) {
            currentText.substring(cursorPosition)
        } else ""
        
        // Add a space after the mention if not already present
        val mentionWithSpace = if (promptText.endsWith(" ")) promptText else "$promptText "
        
        val newText = beforeTrigger + mentionWithSpace + afterCursor
        val newCursorPosition = beforeTrigger.length + mentionWithSpace.length
        
        return InsertionResult(
            newText = newText,
            newCursorPosition = newCursorPosition,
            promptText = promptText,
            underlyingText = underlyingText,
            spanStart = beforeTrigger.length,
            spanEnd = beforeTrigger.length + promptText.trimEnd().length
        )
    }

    /**
     * Replaces prompt text with underlying text for message sending.
     * 
     * This method processes the text before sending to replace visible
     * mention text with the underlying format expected by the SDK.
     * 
     * @param text The text with visible mentions
     * @param mentions Map of prompt text to underlying text
     * @return The processed text with underlying mention format
     */
    fun replacePromptsWithUnderlying(
        text: String,
        mentions: Map<String, String>
    ): String {
        var result = text
        for ((prompt, underlying) in mentions) {
            result = result.replace(prompt, underlying)
        }
        return result
    }
}

/**
 * Data class representing a selected mention.
 * 
 * @property id The unique identifier (e.g., user ID)
 * @property name The display name
 * @property promptText The text shown in the input (e.g., "@username")
 * @property underlyingText The text stored for processing (e.g., "<@uid:123>")
 * @property spanStart The start index of the span in the text
 * @property spanEnd The end index of the span in the text
 */
data class SelectedMention(
    val id: String,
    val name: String,
    val promptText: String,
    val underlyingText: String,
    val spanStart: Int,
    val spanEnd: Int
)

/**
 * Manager for tracking selected mentions in a text input.
 * 
 * This class maintains a list of selected mentions and provides methods
 * to add, remove, and process mentions.
 */
class SelectedMentionsManager {
    private val selectedMentions = mutableListOf<SelectedMention>()

    /**
     * Adds a selected mention.
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
     * Removes a mention at a specific position.
     */
    fun removeMentionAt(position: Int) {
        selectedMentions.removeAll { position in it.spanStart..it.spanEnd }
    }

    /**
     * Gets all selected mentions.
     */
    fun getMentions(): List<SelectedMention> = selectedMentions.toList()

    /**
     * Clears all selected mentions.
     */
    fun clear() {
        selectedMentions.clear()
    }

    /**
     * Gets the prompt to underlying text mapping.
     */
    fun getPromptToUnderlyingMap(): Map<String, String> {
        return selectedMentions.associate { it.promptText to it.underlyingText }
    }

    /**
     * Updates span positions after text changes.
     * 
     * @param changeStart The start position of the text change
     * @param changeLength The length of the change (positive for insertion, negative for deletion)
     */
    fun updatePositions(changeStart: Int, changeLength: Int) {
        val updatedMentions = selectedMentions.mapNotNull { mention ->
            when {
                // Mention is before the change - no update needed
                mention.spanEnd < changeStart -> mention
                
                // Mention is after the change - shift positions
                mention.spanStart >= changeStart -> mention.copy(
                    spanStart = mention.spanStart + changeLength,
                    spanEnd = mention.spanEnd + changeLength
                )
                
                // Change is within the mention - remove it (mention was edited)
                else -> null
            }
        }
        selectedMentions.clear()
        selectedMentions.addAll(updatedMentions)
    }

    /**
     * Checks if a position is within a mention span.
     */
    fun isPositionInMention(position: Int): Boolean {
        return selectedMentions.any { position in it.spanStart..it.spanEnd }
    }

    /**
     * Gets the mention at a specific position.
     */
    fun getMentionAt(position: Int): SelectedMention? {
        return selectedMentions.find { position in it.spanStart..it.spanEnd }
    }
}
