package com.cometchat.uikit.kotlin.presentation.messagecomposer.ui

import android.widget.EditText
import com.cometchat.uikit.core.formatter.ComposerInputController
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.kotlin.shared.spans.NonEditableSpan

/**
 * Classic (View/XML) implementation of [ComposerInputController] over the composer's input
 * [EditText]. The `Editable` is the source of truth, so text edits made here flow through the
 * composer's own `TextWatcher` (mention detection, span bookkeeping) and the field repaints
 * without any manual refresh.
 *
 * [onToggleFormat] delegates to the composer's own format handling so built-in formats keep their
 * code/list/mention special-casing (rather than re-implementing it here).
 */
internal class ClassicComposerInputController(
    private val editText: EditText,
    private val onToggleFormat: (RichTextFormat) -> Unit,
) : ComposerInputController {

    override val text: String
        get() = editText.editableText?.toString() ?: ""

    override val selection: IntRange
        get() = editText.selectionStart.coerceAtLeast(0)..editText.selectionEnd.coerceAtLeast(0)

    override val isCursorCollapsed: Boolean
        get() = editText.selectionStart == editText.selectionEnd

    override fun insertAtCursor(textToInsert: String) {
        val editable = editText.editableText ?: return
        val start = minOf(editText.selectionStart, editText.selectionEnd).coerceIn(0, editable.length)
        val end = maxOf(editText.selectionStart, editText.selectionEnd).coerceIn(start, editable.length)
        editable.replace(start, end, textToInsert)
        editText.setSelection((start + textToInsert.length).coerceAtMost(editText.editableText?.length ?: 0))
    }

    override fun replaceSelection(replacement: String) = insertAtCursor(replacement)

    override fun toggleFormat(format: RichTextFormat) = onToggleFormat(format)

    override fun mentionRanges(): List<IntRange> {
        val editable = editText.editableText ?: return emptyList()
        return editable.getSpans(0, editable.length, NonEditableSpan::class.java)
            .map { editable.getSpanStart(it)..editable.getSpanEnd(it) }
    }
}
