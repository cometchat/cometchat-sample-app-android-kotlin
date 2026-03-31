package com.cometchat.chatuikit.shared.spans;

import android.text.style.StrikethroughSpan;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for strikethrough formatting in WYSIWYG rich text editing.
 * <p>
 * This span extends {@link StrikethroughSpan} to render text with a strikethrough
 * line. It implements {@link RichTextFormatSpan} to enable format detection
 * and markdown conversion.
 * </p>
 * <p>
 * When applied to text, this span displays the text with a strikethrough line
 * without showing any markdown syntax markers (like ~~). The markdown markers
 * are only generated when converting to markdown for sending messages.
 * </p>
 *
 * @see RichTextFormatSpan
 * @see FormatType#STRIKETHROUGH
 */
public class StrikethroughFormatSpan extends StrikethroughSpan implements RichTextFormatSpan {

    /**
     * Creates a new StrikethroughFormatSpan that renders text with strikethrough style.
     */
    public StrikethroughFormatSpan() {
        super();
    }

    /**
     * Returns the format type associated with this span.
     *
     * @return {@link FormatType#STRIKETHROUGH} indicating this is a strikethrough format span.
     */
    @Override
    public FormatType getFormatType() {
        return FormatType.STRIKETHROUGH;
    }
}
