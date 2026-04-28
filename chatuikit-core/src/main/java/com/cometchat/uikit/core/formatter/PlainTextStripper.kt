package com.cometchat.uikit.core.formatter

/**
 * Strips all markdown syntax from a raw markdown string, returning clean plain text.
 * Uses [MarkdownRenderer.parse] internally to ensure consistency with the rendering pipeline.
 *
 * Primary use case: push notifications where formatted text is not supported and
 * markdown markers (**, _, ~~, etc.) would clutter the notification text.
 *
 * For any valid markdown string, [strip] produces the same result as parsing with
 * [MarkdownRenderer.parse] and then extracting plain text from the resulting segments
 * (round-trip consistency property).
 */
object PlainTextStripper {

    /**
     * Strips all markdown syntax markers from [markdown] and returns clean plain text.
     *
     * - Inline markers (`**`, `_`, `~~`, `<u>`, `` ` ``, `[text](url)`) are removed
     * - Fenced code block markers (`` ``` ``) and language identifiers are removed
     * - List prefixes (`- `, `1. `) are removed
     * - Blockquote prefixes (`> `) are removed
     * - Link syntax `[display](url)` outputs only the display text
     * - Non-markdown text, whitespace between words, and newlines between paragraphs are preserved
     *
     * @param markdown The raw markdown string to strip
     * @return Clean plain text with all markdown syntax removed
     */
    fun strip(markdown: String): String {
        if (markdown.isEmpty()) return ""

        val segments = MarkdownRenderer.parse(markdown)
        return segments.joinToString(" ") { segment ->
            when (segment) {
                is MarkdownRenderer.RenderedSegment.Text -> {
                    MarkdownRenderer.parseInline(segment.text).first
                }
                is MarkdownRenderer.RenderedSegment.CodeBlock -> {
                    segment.code
                }
                is MarkdownRenderer.RenderedSegment.BulletItem -> {
                    MarkdownRenderer.parseInline(segment.text).first
                }
                is MarkdownRenderer.RenderedSegment.OrderedItem -> {
                    MarkdownRenderer.parseInline(segment.text).first
                }
                is MarkdownRenderer.RenderedSegment.Blockquote -> {
                    MarkdownRenderer.parseInline(segment.text).first
                }
            }
        }
    }
}
