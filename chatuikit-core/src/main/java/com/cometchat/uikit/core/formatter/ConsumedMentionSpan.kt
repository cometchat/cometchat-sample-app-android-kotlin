package com.cometchat.uikit.core.formatter

/**
 * Stores original mention data when a mention is converted to plain text
 * inside code formatting (INLINE_CODE or CODE_BLOCK), enabling restoration
 * when the code formatting is removed.
 *
 * When code formatting is applied to text containing mentions, the mention
 * spans (NonEditableSpan) are converted to plain text and a ConsumedMentionSpan
 * is created to preserve the original mention data. When code formatting is
 * later removed, this data is used to restore the original mention span.
 *
 * @param id The mention tracking character (e.g., '@')
 * @param text The original display text of the mention
 * @param suggestionItem The original SuggestionItem associated with the mention (typed as Any? for cross-module compatibility)
 * @param textAppearance The original text appearance/style of the mention (typed as Any? for cross-module compatibility)
 */
data class ConsumedMentionSpan(
    val id: Char,
    val text: String?,
    val suggestionItem: Any?,
    val textAppearance: Any?
) {
    /**
     * Returns true if this consumed mention span has enough data to restore
     * the original mention span.
     */
    fun canRestore(): Boolean = text != null
}
