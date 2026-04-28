package com.cometchat.uikit.kotlin.shared.spans

import android.text.style.UnderlineSpan
import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Span for underline formatting in WYSIWYG rich text editing.
 *
 * Extends [UnderlineSpan] to render text with an underline.
 * Implements [RichTextFormatSpan] to enable format detection and markdown conversion.
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.UNDERLINE
 */
class UnderlineFormatSpan : UnderlineSpan(), RichTextFormatSpan {

    override fun getFormatType(): RichTextFormat = RichTextFormat.UNDERLINE
}
