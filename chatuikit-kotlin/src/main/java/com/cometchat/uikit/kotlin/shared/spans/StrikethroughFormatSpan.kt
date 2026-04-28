package com.cometchat.uikit.kotlin.shared.spans

import android.text.style.StrikethroughSpan
import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Span for strikethrough formatting in WYSIWYG rich text editing.
 *
 * Extends [StrikethroughSpan] to render text with a strikethrough line.
 * Implements [RichTextFormatSpan] to enable format detection and markdown conversion.
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.STRIKETHROUGH
 */
class StrikethroughFormatSpan : StrikethroughSpan(), RichTextFormatSpan {

    override fun getFormatType(): RichTextFormat = RichTextFormat.STRIKETHROUGH
}
