package com.cometchat.uikit.core.formatter

/**
 * Represents a single segment in the multi-segment composer.
 * Each segment is either a normal rich text segment or a code block segment.
 *
 * Normal segments use [RichTextEditorController] for WYSIWYG formatting.
 * Code segments hold plain text with an optional language hint.
 */
sealed class ComposerSegment {
    abstract val id: String

    /**
     * Normal rich text segment with span-based formatting.
     */
    data class Normal(
        override val id: String,
        val controller: RichTextEditorController = RichTextEditorController()
    ) : ComposerSegment()

    /**
     * Code block segment — plain text, monospace, no formatting.
     */
    data class Code(
        override val id: String,
        var text: String = "",
        var language: String = ""
    ) : ComposerSegment()
}
