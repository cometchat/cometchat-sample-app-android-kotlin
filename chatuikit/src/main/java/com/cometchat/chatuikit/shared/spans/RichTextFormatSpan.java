package com.cometchat.chatuikit.shared.spans;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Base interface for all rich text format spans.
 * <p>
 * This interface allows identification of format type for any span used in
 * WYSIWYG rich text editing. All custom format spans (BoldFormatSpan,
 * ItalicFormatSpan, StrikethroughFormatSpan, etc.) implement this interface
 * to enable format detection and markdown conversion.
 * </p>
 * <p>
 * The interface is used by:
 * <ul>
 *   <li>{@code RichTextSpanManager} - to detect active formats at cursor position</li>
 *   <li>{@code MarkdownConverter} - to convert spans to markdown syntax</li>
 *   <li>{@code FormatStateManager} - to track and update toolbar button states</li>
 * </ul>
 * </p>
 *
 * @see FormatType
 */
public interface RichTextFormatSpan {

    /**
     * Returns the format type associated with this span.
     * <p>
     * This method is used to identify what kind of formatting this span represents,
     * enabling format detection, toolbar state updates, and markdown conversion.
     * </p>
     *
     * @return The {@link FormatType} that this span represents.
     */
    FormatType getFormatType();
}
