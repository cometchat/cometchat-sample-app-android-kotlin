package com.cometchat.uikit.core.mentions

/**
 * MentionDetector handles detection of mention triggers in text input.
 * 
 * This utility class detects when a user types a tracking character (e.g., @)
 * and extracts the query text for searching suggestions.
 * 
 * Features:
 * - Detects tracking character at cursor position
 * - Extracts query text after tracking character
 * - Tracks cursor position for mention insertion
 * - Handles multiple mentions in the same message
 */
class MentionDetector(
    private val trackingCharacter: Char = '@'
) {
    /**
     * Result of mention detection.
     * 
     * @property isActive Whether a mention is currently being typed
     * @property query The search query (text after tracking character)
     * @property triggerIndex The index of the tracking character in the text
     * @property cursorPosition The current cursor position
     */
    data class MentionDetectionResult(
        val isActive: Boolean,
        val query: String,
        val triggerIndex: Int,
        val cursorPosition: Int
    ) {
        companion object {
            val INACTIVE = MentionDetectionResult(
                isActive = false,
                query = "",
                triggerIndex = -1,
                cursorPosition = -1
            )
        }
    }

    /**
     * Detects if a mention is being typed at the current cursor position.
     * 
     * A mention is detected when:
     * 1. The tracking character is found before the cursor
     * 2. The tracking character is either at the start or preceded by a space/newline
     * 3. The mention is NOT closed by a space immediately after the tracking character
     * 
     * This implementation supports names with spaces (e.g., "@John Paul") by:
     * - Searching backwards for the tracking character
     * - Only stopping at the tracking character, not at spaces
     * - Checking if the character immediately after @ is a space (which closes the mention)
     * 
     * @param text The current text in the input field
     * @param cursorPosition The current cursor position
     * @return MentionDetectionResult with detection status and query
     */
    fun detectMention(text: String, cursorPosition: Int): MentionDetectionResult {
        if (text.isEmpty() || cursorPosition <= 0 || cursorPosition > text.length) {
            return MentionDetectionResult.INACTIVE
        }

        // Search backwards from cursor to find tracking character
        // Don't stop at spaces - we want to support names like "John Paul"
        var triggerIndex = -1
        for (i in cursorPosition - 1 downTo 0) {
            val char = text[i]
            
            // Found tracking character
            if (char == trackingCharacter) {
                // Verify it's at start or preceded by space/newline
                if (i == 0 || text[i - 1] == ' ' || text[i - 1] == '\n') {
                    // Check if the character immediately after @ is a space (closes mention)
                    if (i < text.length - 1 && (text[i + 1] == ' ' || text[i + 1] == '\n')) {
                        // @ followed by space - no active mention
                        return MentionDetectionResult.INACTIVE
                    }
                    triggerIndex = i
                }
                break
            }
            
            // Stop if we hit a newline (mentions don't span lines)
            if (char == '\n') {
                break
            }
        }

        if (triggerIndex == -1) {
            return MentionDetectionResult.INACTIVE
        }

        // Extract query (text between tracking character and cursor)
        val query = text.substring(triggerIndex + 1, cursorPosition)
        
        // Check if query ends with multiple consecutive spaces (user typed space after completing name)
        // This handles the case where user types "@John " - we should hide suggestions
        if (query.isNotEmpty()) {
            val lastChar = query.last()
            val secondLastChar = if (query.length > 1) query[query.length - 2] else null
            // If last two chars are spaces, close the mention
            if (lastChar == ' ' && secondLastChar == ' ') {
                return MentionDetectionResult.INACTIVE
            }
        }

        return MentionDetectionResult(
            isActive = true,
            query = query,
            triggerIndex = triggerIndex,
            cursorPosition = cursorPosition
        )
    }

    /**
     * Checks if the given character is the tracking character.
     */
    fun isTrackingCharacter(char: Char): Boolean = char == trackingCharacter

    /**
     * Gets the tracking character.
     */
    fun getTrackingCharacter(): Char = trackingCharacter

    /**
     * Calculates the text range to replace when inserting a mention.
     * 
     * @param text The current text
     * @param detectionResult The mention detection result
     * @return Pair of (startIndex, endIndex) for replacement
     */
    fun getReplacementRange(text: String, detectionResult: MentionDetectionResult): Pair<Int, Int> {
        if (!detectionResult.isActive) {
            return Pair(-1, -1)
        }
        return Pair(detectionResult.triggerIndex, detectionResult.cursorPosition)
    }
}

/**
 * State holder for mention detection in a text input.
 * 
 * This class maintains the state of mention detection and provides
 * methods to update and query the state.
 */
class MentionDetectionState(
    private val detector: MentionDetector = MentionDetector()
) {
    private var currentResult: MentionDetector.MentionDetectionResult = 
        MentionDetector.MentionDetectionResult.INACTIVE

    /**
     * Updates the detection state based on text and cursor changes.
     * 
     * @param text The current text
     * @param cursorPosition The current cursor position
     * @return The updated detection result
     */
    fun update(text: String, cursorPosition: Int): MentionDetector.MentionDetectionResult {
        currentResult = detector.detectMention(text, cursorPosition)
        return currentResult
    }

    /**
     * Gets the current detection result.
     */
    fun getCurrentResult(): MentionDetector.MentionDetectionResult = currentResult

    /**
     * Checks if a mention is currently active.
     */
    fun isActive(): Boolean = currentResult.isActive

    /**
     * Gets the current search query.
     */
    fun getQuery(): String = currentResult.query

    /**
     * Gets the trigger index.
     */
    fun getTriggerIndex(): Int = currentResult.triggerIndex

    /**
     * Resets the detection state.
     */
    fun reset() {
        currentResult = MentionDetector.MentionDetectionResult.INACTIVE
    }

    /**
     * Gets the replacement range for inserting a mention.
     */
    fun getReplacementRange(text: String): Pair<Int, Int> {
        return detector.getReplacementRange(text, currentResult)
    }

    /**
     * Gets the tracking character.
     */
    fun getTrackingCharacter(): Char = detector.getTrackingCharacter()
}
