package com.cometchat.uikit.kotlin.shared.spans

import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Base interface for all rich text format spans.
 *
 * This interface allows identification of format type for any span used in
 * WYSIWYG rich text editing. All custom format spans (BoldFormatSpan,
 * ItalicFormatSpan, StrikethroughFormatSpan, etc.) implement this interface
 * to enable format detection and markdown conversion.
 *
 * Used by:
 * - [RichTextSpanManager] to detect active formats at cursor position
 * - [MarkdownConverter] to convert spans to markdown syntax
 * - Toolbar state management to track and update button states
 *
 * @see RichTextFormat
 */
interface RichTextFormatSpan {

    /**
     * Returns the format type associated with this span.
     *
     * This method is used to identify what kind of formatting this span represents,
     * enabling format detection, toolbar state updates, and markdown conversion.
     *
     * @return The [RichTextFormat] that this span represents.
     */
    fun getFormatType(): RichTextFormat
}
