package com.cometchat.uikit.core.formatter

/**
 * Parses markdown text into structured segments for display in message bubbles.
 * Handles both block-level elements (code blocks, lists, blockquotes) and
 * inline formatting (bold, italic, underline, strikethrough, inline code, links).
 *
 * Platform-agnostic — shared by Jetpack Compose and Kotlin XML UI kits.
 * The UI layer maps [RenderedSegment] and [InlineSpan] to platform-specific styled text.
 */
object MarkdownRenderer {

    /**
     * A block-level segment of rendered markdown.
     */
    sealed class RenderedSegment {
        /** Normal text block with inline formatting spans. */
        data class Text(
            val text: String,
            val spans: List<InlineSpan> = emptyList()
        ) : RenderedSegment()

        /** Fenced code block. */
        data class CodeBlock(
            val code: String,
            val language: String = ""
        ) : RenderedSegment()

        /** Bullet list item (content may have inline spans). */
        data class BulletItem(
            val text: String,
            val spans: List<InlineSpan> = emptyList()
        ) : RenderedSegment()

        /** Ordered list item (content may have inline spans). */
        data class OrderedItem(
            val number: Int,
            val text: String,
            val spans: List<InlineSpan> = emptyList()
        ) : RenderedSegment()

        /** Blockquote line (content may have inline spans). */
        data class Blockquote(
            val text: String,
            val spans: List<InlineSpan> = emptyList()
        ) : RenderedSegment()
    }

    /**
     * An inline formatting span within a text segment.
     * [start] and [end] are relative to the parent segment's text.
     */
    data class InlineSpan(
        val start: Int,
        val end: Int,
        val format: RichTextFormat,
        /** For LINK format, holds the URL. */
        val url: String? = null
    )

    /**
     * Parses a markdown string into a list of [RenderedSegment]s.
     * Handles fenced code blocks first, then processes remaining text
     * line-by-line for block-level elements, and finally parses inline formatting.
     */
    fun parse(markdown: String): List<RenderedSegment> {
        if (markdown.isBlank()) return listOf(RenderedSegment.Text(""))

        val segments = mutableListOf<RenderedSegment>()
        val codeBlockRegex = Regex("```(\\w*)\\n([\\s\\S]*?)\\n?```")

        var cursor = 0
        for (match in codeBlockRegex.findAll(markdown)) {
            // Process text before this code block
            if (match.range.first > cursor) {
                val before = markdown.substring(cursor, match.range.first).trimEnd('\n')
                if (before.isNotEmpty()) {
                    segments.addAll(parseBlocks(before))
                }
            }
            segments.add(RenderedSegment.CodeBlock(
                code = match.groupValues[2],
                language = match.groupValues[1]
            ))
            cursor = match.range.last + 1
        }

        // Process remaining text after last code block
        if (cursor < markdown.length) {
            val remaining = markdown.substring(cursor).trimStart('\n')
            if (remaining.isNotEmpty()) {
                segments.addAll(parseBlocks(remaining))
            }
        }

        return segments
    }

    /**
     * Parses non-code-block text into block-level segments (lists, quotes, paragraphs).
     */
    private fun parseBlocks(text: String): List<RenderedSegment> {
        val segments = mutableListOf<RenderedSegment>()
        val lines = text.split("\n")
        val paragraphBuffer = StringBuilder()

        fun flushParagraph() {
            if (paragraphBuffer.isNotEmpty()) {
                val content = paragraphBuffer.toString()
                segments.add(RenderedSegment.Text(content, parseInlineSpans(content)))
                paragraphBuffer.clear()
            }
        }

        for (line in lines) {
            // Ordered list: "1. content"
            val orderedMatch = Regex("^(\\d+)\\. (.*)$").find(line)
            if (orderedMatch != null) {
                flushParagraph()
                val num = orderedMatch.groupValues[1].toIntOrNull() ?: 1
                val content = orderedMatch.groupValues[2]
                segments.add(RenderedSegment.OrderedItem(num, content, parseInlineSpans(content)))
                continue
            }

            // Bullet list: "- content"
            if (line.startsWith("- ")) {
                flushParagraph()
                val content = line.substring(2)
                segments.add(RenderedSegment.BulletItem(content, parseInlineSpans(content)))
                continue
            }

            // Blockquote: "> content"
            if (line.startsWith("> ")) {
                flushParagraph()
                val content = line.substring(2)
                segments.add(RenderedSegment.Blockquote(content, parseInlineSpans(content)))
                continue
            }

            // Regular text line — accumulate into paragraph
            if (paragraphBuffer.isNotEmpty()) paragraphBuffer.append("\n")
            paragraphBuffer.append(line)
        }

        flushParagraph()
        return segments
    }

    /**
     * Parses inline markdown formatting within a text string.
     * Returns spans with positions relative to the plain text (markers stripped).
     *
     * Note: The returned spans reference positions in the ORIGINAL text (with markers).
     * The caller is responsible for stripping markers and adjusting positions,
     * OR we strip markers here and return clean text + adjusted spans.
     *
     * We take the second approach: [parseInline] returns the clean text and spans.
     */
    fun parseInlineSpans(text: String): List<InlineSpan> {
        val result = parseInline(text)
        return result.second
    }

    /**
     * Strips inline markdown markers and returns (plainText, spans).
     * Supports nested/combined formats like `**_bold italic_**`.
     *
     * Strategy: iteratively strip the outermost format layer, record spans
     * relative to the progressively stripped text, until no more markers remain.
     */
    fun parseInline(text: String): Pair<String, List<InlineSpan>> {
        if (text.isEmpty()) return text to emptyList()

        // Each pattern strips one layer of markers per pass.
        data class FormatPattern(
            val regex: Regex,
            val format: RichTextFormat,
            val openLen: Int,
            val closeLen: Int,
            val urlGroup: Int? = null // group index for URL (link only)
        )

        val patterns = listOf(
            FormatPattern(Regex("\\*\\*(.+?)\\*\\*"), RichTextFormat.BOLD, 2, 2),
            FormatPattern(Regex("~~(.+?)~~"), RichTextFormat.STRIKETHROUGH, 2, 2),
            FormatPattern(Regex("<u>(.+?)</u>"), RichTextFormat.UNDERLINE, 3, 4),
            FormatPattern(Regex("(?<!_)_([^_]+)_(?!_)"), RichTextFormat.ITALIC, 1, 1),
            FormatPattern(Regex("`([^`]+)`"), RichTextFormat.INLINE_CODE, 1, 1),
            FormatPattern(Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)"), RichTextFormat.LINK, 0, 0, urlGroup = 2),
        )

        // We'll accumulate spans as (start, end, format, url) in terms of the FINAL plain text.
        // To do this we iteratively strip markers, keeping a mapping from current positions
        // to final positions.

        // Pass-based approach: each pass strips one format's markers and records spans.
        // After all passes, the text is fully stripped and spans reference the final text.

        var current = text
        // Offset map: for each position in `current`, what's the cumulative marker chars removed
        // We track removals as a list of (position-in-current, chars-removed)
        data class Removal(val pos: Int, val len: Int)

        val allRemovals = mutableListOf<Removal>() // accumulated across passes
        val spans = mutableListOf<InlineSpan>()

        // Run multiple passes until no more markers are found
        var changed = true
        while (changed) {
            changed = false
            for (pattern in patterns) {
                val match = pattern.regex.find(current) ?: continue
                changed = true

                val matchStart = match.range.first
                val matchEnd = match.range.last + 1
                val content = match.groupValues[1]
                val url = pattern.urlGroup?.let { match.groupValues[it] }

                // For LINK, the entire match [text](url) is replaced by just the display text
                val openMarkerLen: Int
                val closeMarkerLen: Int
                if (pattern.format == RichTextFormat.LINK) {
                    openMarkerLen = 1 // "["
                    closeMarkerLen = matchEnd - (matchStart + 1 + content.length) // "](url)"
                } else {
                    openMarkerLen = pattern.openLen
                    closeMarkerLen = pattern.closeLen
                }

                // Strip markers from current text
                val before = current.substring(0, matchStart)
                val inner = current.substring(matchStart + openMarkerLen, matchEnd - closeMarkerLen)
                val after = current.substring(matchEnd)
                current = before + inner + after

                // Record removals for position mapping
                allRemovals.add(Removal(matchStart, openMarkerLen))
                allRemovals.add(Removal(matchEnd - closeMarkerLen - openMarkerLen + matchStart, closeMarkerLen))

                // The span in the current (partially stripped) text
                val spanStart = matchStart
                val spanEnd = matchStart + inner.length
                if (spanEnd > spanStart) {
                    spans.add(InlineSpan(spanStart, spanEnd, pattern.format, url))
                }

                break // restart from first pattern after each strip to handle nesting
            }
        }

        // Now `current` is the fully stripped plain text, and all span positions
        // reference this final text. However, spans recorded in earlier passes
        // may have positions that shifted due to later removals. We need to adjust.
        //
        // Actually, since we strip markers from `current` before recording the span,
        // and subsequent passes operate on the already-stripped text, the span positions
        // are already relative to the final plain text at the time of recording.
        // But later passes may strip MORE markers that shift earlier spans.
        //
        // Fix: re-parse from scratch with a cleaner approach.

        // --- Clean re-implementation: single-pass recursive stripping ---
        return parseInlineClean(text)
    }

    /**
     * Clean implementation: repeatedly find and strip the first (leftmost) format marker,
     * recording each span's position in the final plain text.
     */
    private fun parseInlineClean(text: String): Pair<String, List<InlineSpan>> {
        if (text.isEmpty()) return text to emptyList()

        data class FormatPattern(
            val regex: Regex,
            val format: RichTextFormat,
            val openLen: Int,
            val closeLen: Int,
            val urlGroup: Int? = null
        )

        val patterns = listOf(
            FormatPattern(Regex("\\*\\*(.+?)\\*\\*"), RichTextFormat.BOLD, 2, 2),
            FormatPattern(Regex("~~(.+?)~~"), RichTextFormat.STRIKETHROUGH, 2, 2),
            FormatPattern(Regex("<u>(.+?)</u>"), RichTextFormat.UNDERLINE, 3, 4),
            FormatPattern(Regex("(?<!_)_([^_]+)_(?!_)"), RichTextFormat.ITALIC, 1, 1),
            FormatPattern(Regex("`([^`]+)`"), RichTextFormat.INLINE_CODE, 1, 1),
            FormatPattern(Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)"), RichTextFormat.LINK, 0, 0, urlGroup = 2),
        )

        // Track spans as (startInOriginal, endInOriginal, format, url) where positions
        // are in the CURRENT (progressively stripped) text at time of discovery.
        // We also track all marker removals so we can adjust earlier spans.
        data class RawSpan(var start: Int, var end: Int, val format: RichTextFormat, val url: String?)

        val rawSpans = mutableListOf<RawSpan>()
        var current = text

        var found = true
        while (found) {
            found = false
            // Find the leftmost match across all patterns
            var bestMatch: MatchResult? = null
            var bestPattern: FormatPattern? = null
            for (pattern in patterns) {
                val m = pattern.regex.find(current)
                if (m != null && (bestMatch == null || m.range.first < bestMatch.range.first)) {
                    bestMatch = m
                    bestPattern = pattern
                }
            }
            if (bestMatch == null || bestPattern == null) break
            found = true

            val matchStart = bestMatch.range.first
            val matchEnd = bestMatch.range.last + 1
            val content = bestMatch.groupValues[1]
            val url = bestPattern.urlGroup?.let { bestMatch.groupValues[it] }

            val openLen: Int
            val closeLen: Int
            if (bestPattern.format == RichTextFormat.LINK) {
                openLen = 1 // "["
                closeLen = matchEnd - (matchStart + 1 + content.length) // "](url)"
            } else {
                openLen = bestPattern.openLen
                closeLen = bestPattern.closeLen
            }

            val innerStart = matchStart + openLen
            val innerEnd = matchEnd - closeLen
            val innerText = current.substring(innerStart, innerEnd)

            // Strip markers: remove open marker and close marker
            val before = current.substring(0, matchStart)
            val after = current.substring(matchEnd)
            current = before + innerText + after

            // Adjust all previously recorded spans for the removal
            val openRemovalPos = matchStart
            val closeRemovalPos = matchStart + innerText.length // position of close marker after open removal

            for (s in rawSpans) {
                // Adjust for open marker removal at openRemovalPos (openLen chars removed)
                if (s.start > openRemovalPos) s.start -= openLen
                else if (s.start == openRemovalPos) { /* no change */ }
                if (s.end > openRemovalPos) s.end -= openLen

                // Adjust for close marker removal at closeRemovalPos (closeLen chars removed)
                if (s.start > closeRemovalPos) s.start -= closeLen
                if (s.end > closeRemovalPos) s.end -= closeLen
            }

            // Record this span (positions in the now-stripped text)
            val spanStart = matchStart
            val spanEnd = matchStart + innerText.length
            if (spanEnd > spanStart) {
                rawSpans.add(RawSpan(spanStart, spanEnd, bestPattern.format, url))
            }
        }

        val spans = rawSpans.map { InlineSpan(it.start, it.end, it.format, it.url) }
        return current to spans
    }
}
