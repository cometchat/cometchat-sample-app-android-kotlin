package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import com.cometchat.uikit.compose.presentation.shared.mentions.ComposeMentionInsertionState
import com.cometchat.uikit.core.formatter.ComposerInputController
import com.cometchat.uikit.core.formatter.ComposerSegment
import com.cometchat.uikit.core.formatter.RichTextEditorController
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.SegmentComposerController

/**
 * Compose implementation of [ComposerInputController] for the segment-based message composer.
 *
 * The composer has no single input — each [ComposerSegment.Normal] owns its own
 * [RichTextEditorController]. This facade resolves the **currently focused** Normal segment's
 * controller on each call, so a custom trailing-toolbar button always operates on the segment the
 * user is editing. When no Normal segment is focused (e.g. the caret is inside a code block, or the
 * composer is empty) mutations are no-ops and reads return empty/collapsed values.
 *
 * [onMutated] is invoked after every mutation so the composer can trigger the recomposition that
 * resyncs each segment's `TextFieldValue` from its controller (the same `formatVersion` bump the
 * built-in format buttons use).
 */
internal class ComposeComposerInputController(
    private val segmentController: SegmentComposerController,
    private val mentionInsertionState: ComposeMentionInsertionState? = null,
    private val onMutated: () -> Unit,
) : ComposerInputController {

    private val activeController: RichTextEditorController?
        get() = (segmentController.focusedSegment as? ComposerSegment.Normal)?.controller

    override val text: String
        get() = activeController?.state?.text ?: ""

    override val selection: IntRange
        get() = activeController?.let { it.state.selectionStart..it.state.selectionEnd } ?: IntRange.EMPTY

    override val isCursorCollapsed: Boolean
        get() = activeController?.state?.isCursorCollapsed ?: true

    override fun insertAtCursor(textToInsert: String) {
        val controller = activeController ?: return
        controller.insertAtCursor(textToInsert)
        onMutated()
    }

    override fun replaceSelection(replacement: String) {
        val controller = activeController ?: return
        controller.replaceSelection(replacement)
        onMutated()
    }

    override fun toggleFormat(format: RichTextFormat) {
        val controller = activeController ?: return
        controller.toggleFormat(format)
        onMutated()
    }

    /**
     * Mention ranges (in the focused segment's coordinates, matching [text]/[selection]) from the
     * composer's mention state, so a custom style can skip mentions. Empty when mentions are
     * disabled or none are present.
     */
    override fun mentionRanges(): List<IntRange> =
        mentionInsertionState?.getMentionsManager()?.getMentions()
            ?.filter { it.spanEnd > it.spanStart }
            ?.map { it.spanStart until it.spanEnd }
            ?: emptyList()
}
