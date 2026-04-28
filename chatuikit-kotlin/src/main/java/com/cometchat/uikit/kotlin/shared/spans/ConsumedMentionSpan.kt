package com.cometchat.uikit.kotlin.shared.spans

import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.formatters.style.PromptTextStyle

/**
 * A marker span that stores original mention data when a mention is converted
 * to plain text inside code formatting (CodeBlock or InlineCode).
 *
 * When code formatting is applied to a range containing NonEditableSpan mentions,
 * each mention is replaced with a ConsumedMentionSpan preserving the original data.
 * When code formatting is removed, the ConsumedMentionSpan can be restored back
 * to a NonEditableSpan via [canRestore].
 */
data class ConsumedMentionSpan(
    val id: Char,
    val text: String?,
    val suggestionItem: SuggestionItem?,
    val textAppearance: PromptTextStyle?
) {
    /**
     * Returns true if this consumed mention has enough data to be restored
     * back to a NonEditableSpan.
     */
    fun canRestore(): Boolean = id != '\u0000' && !text.isNullOrEmpty()
}
