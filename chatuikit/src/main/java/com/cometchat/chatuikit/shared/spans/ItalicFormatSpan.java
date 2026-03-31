package com.cometchat.chatuikit.shared.spans;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

import com.cometchat.chatuikit.shared.views.richtexttoolbar.FormatType;

/**
 * Span for italic formatting in WYSIWYG rich text editing.
 * <p>
 * This span extends {@link StyleSpan} with {@link Typeface#ITALIC} to render text
 * in italic style. It implements {@link RichTextFormatSpan} to enable format detection
 * and markdown conversion.
 * </p>
 * <p>
 * When applied to text, this span displays the text in italic without showing any
 * markdown syntax markers (like _). The markdown markers are only generated
 * when converting to markdown for sending messages.
 * </p>
 *
 * @see RichTextFormatSpan
 * @see FormatType#ITALIC
 */
public class ItalicFormatSpan extends StyleSpan implements RichTextFormatSpan {

    /**
     * Creates a new ItalicFormatSpan that renders text in italic style.
     */
    public ItalicFormatSpan() {
        super(Typeface.ITALIC);
    }

    /**
     * Returns the format type associated with this span.
     *
     * @return {@link FormatType#ITALIC} indicating this is an italic format span.
     */
    @Override
    public FormatType getFormatType() {
        return FormatType.ITALIC;
    }
}
