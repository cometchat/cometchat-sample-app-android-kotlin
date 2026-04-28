package com.cometchat.uikit.kotlin.shared.spans

import android.graphics.Typeface
import android.text.style.StyleSpan
import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Span for italic formatting in WYSIWYG rich text editing.
 *
 * Extends [StyleSpan] with [Typeface.ITALIC] to render text in italic style.
 * Implements [RichTextFormatSpan] to enable format detection and markdown conversion.
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat.ITALIC
 */
class ItalicFormatSpan : StyleSpan(Typeface.ITALIC), RichTextFormatSpan {

    override fun getFormatType(): RichTextFormat = RichTextFormat.ITALIC
}
