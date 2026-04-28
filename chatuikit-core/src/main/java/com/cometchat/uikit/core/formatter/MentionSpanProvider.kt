package com.cometchat.uikit.core.formatter

/**
 * Abstraction for querying and manipulating mention spans in the text editor.
 *
 * The [RichTextEditorController] is platform-agnostic and does not know about
 * concrete mention span types (e.g., NonEditableSpan). This interface allows
 * the Compose or XML UI layer to provide mention awareness so the controller
 * can convert mentions to [ConsumedMentionSpan] when code formatting is applied,
 * and restore them when code formatting is removed.
 */
interface MentionSpanProvider {

    /**
     * Represents a mention found in a text range.
     *
     * @param start Start index of the mention in the plain text (inclusive)
     * @param end End index of the mention in the plain text (exclusive)
     * @param id The mention tracking character (e.g., '@')
     * @param displayText The visible display text of the mention
     * @param suggestionItem The original suggestion item (typed as Any? for cross-module compatibility)
     * @param textAppearance The original text appearance (typed as Any? for cross-module compatibility)
     */
    data class MentionInfo(
        val start: Int,
        val end: Int,
        val id: Char,
        val displayText: String,
        val suggestionItem: Any?,
        val textAppearance: Any?
    )

    /**
     * Returns all mentions whose ranges overlap with [start]..[end].
     *
     * @param start Inclusive start of the query range
     * @param end Exclusive end of the query range
     * @return List of mentions in the range, ordered by start position
     */
    fun getMentionsInRange(start: Int, end: Int): List<MentionInfo>

    /**
     * Removes the mention span at the given range, converting it to plain text.
     * The text content remains unchanged; only the span/tracking metadata is removed.
     *
     * @param start Inclusive start of the mention
     * @param end Exclusive end of the mention
     */
    fun removeMentionSpan(start: Int, end: Int)

    /**
     * Restores a mention span from a [ConsumedMentionSpan] at the given range.
     * Re-creates the original NonEditableSpan (or equivalent) using the stored
     * mention data. The text content remains unchanged; only the span/tracking
     * metadata is re-applied.
     *
     * @param start Inclusive start of the mention in the plain text
     * @param end Exclusive end of the mention in the plain text
     * @param consumed The [ConsumedMentionSpan] holding the original mention data
     */
    fun restoreMentionSpan(start: Int, end: Int, consumed: ConsumedMentionSpan)
}
