package com.cometchat.chatuikit.shared.spans;

import android.text.style.UnderlineSpan;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for underline formatting in WYSIWYG rich text editing.
 * <p>
 * This span extends {@link UnderlineSpan} to render text with an underline.
 * It implements {@link RichTextFormatSpan} to enable format detection
 * and markdown conversion.
 * </p>
 * <p>
 * When applied to text, this span displays the text with an underline without
 * showing any HTML-style syntax markers (like &lt;u&gt;). The markers are only
 * generated when converting to markdown for sending messages.
 * </p>
 *
 * @see RichTextFormatSpan
 * @see FormatType#UNDERLINE
 */
public class UnderlineFormatSpan extends UnderlineSpan implements RichTextFormatSpan {

    /**
     * Creates a new UnderlineFormatSpan that renders text with an underline.
     */
    public UnderlineFormatSpan() {
        super();
    }

    /**
     * Returns the format type associated with this span.
     *
     * @return {@link FormatType#UNDERLINE} indicating this is an underline format span.
     */
    @Override
    public FormatType getFormatType() {
        return FormatType.UNDERLINE;
    }
}
