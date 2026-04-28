package com.cometchat.uikit.core.formatter

/**
 * Manages a list of [RichTextSpan] objects representing formatting applied to plain text.
 * Handles adding/removing formats, adjusting spans on text edits, normalization, and
 * serialization to markdown.
 *
 * This is the core engine — platform-agnostic, shared by Jetpack Compose and Kotlin XML UI kits.
 */
class RichTextSpanManager {

    private val _spans = mutableListOf<RichTextSpan>()

    /** Read-only snapshot of current spans, sorted by start position. */
    val spans: List<RichTextSpan> get() = _spans.toList()

    // ==================== Link URL Storage ====================

    /**
     * Maps a link span's start position to its URL.
     * Updated whenever link spans are added, shifted, or removed.
     */
    private val _linkUrlMap = mutableMapOf<Int, String>()

    /** Read-only snapshot of link URL map. */
    val linkUrlMap: Map<Int, String> get() = _linkUrlMap.toMap()

    /** Stores a URL for a link span starting at [start]. */
    fun setLinkUrl(start: Int, url: String) {
        _linkUrlMap[start] = url
    }

    /**
     * Returns the URL for the LINK span that contains [position], or null if
     * the position is not inside a link span or no URL is stored.
     */
    fun getLinkUrlAt(position: Int): String? {
        val linkSpan = findLinkSpanAt(position) ?: return null
        return _linkUrlMap[linkSpan.start]
    }

    /**
     * Returns the LINK span that contains [position], or null.
     */
    fun findLinkSpanAt(position: Int): RichTextSpan? {
        return _spans.firstOrNull { span ->
            RichTextFormat.LINK in span.formats && span.contains(position)
        }
    }

    /**
     * Removes the URL entry for a link span starting at [start].
     */
    fun removeLinkUrl(start: Int) {
        _linkUrlMap.remove(start)
    }

    // ==================== Format Operations ====================

    /**
     * Adds a format to the given range. Handles overlapping spans by splitting and merging.
     */
    fun addFormat(start: Int, end: Int, format: RichTextFormat) {
        if (start >= end) return

        val newSpans = mutableListOf<RichTextSpan>()

        for (span in _spans) {
            if (!span.overlaps(start, end)) {
                newSpans.add(span)
            } else {
                // Part before overlap
                if (span.start < start) {
                    newSpans.add(RichTextSpan(span.start, start, span.formats))
                }
                // Overlapping part — add the new format
                val overlapStart = maxOf(span.start, start)
                val overlapEnd = minOf(span.end, end)
                newSpans.add(RichTextSpan(overlapStart, overlapEnd, span.formats + format))
                // Part after overlap
                if (span.end > end) {
                    newSpans.add(RichTextSpan(end, span.end, span.formats))
                }
            }
        }

        // Fill gaps in the range not covered by existing spans
        val coveredRanges = _spans.filter { it.overlaps(start, end) }
            .map { maxOf(it.start, start) to minOf(it.end, end) }
            .sortedBy { it.first }

        var cursor = start
        for ((covStart, covEnd) in coveredRanges) {
            if (cursor < covStart) {
                newSpans.add(RichTextSpan(cursor, covStart, setOf(format)))
            }
            cursor = covEnd
        }
        if (cursor < end) {
            newSpans.add(RichTextSpan(cursor, end, setOf(format)))
        }

        _spans.clear()
        _spans.addAll(newSpans)
        normalize()
    }

    /**
     * Removes a format from the given range. Splits spans at boundaries if needed.
     */
    fun removeFormat(start: Int, end: Int, format: RichTextFormat) {
        if (start >= end) return

        val newSpans = mutableListOf<RichTextSpan>()

        for (span in _spans) {
            if (!span.overlaps(start, end)) {
                newSpans.add(span)
            } else {
                // Part before overlap — keep original formats
                if (span.start < start) {
                    newSpans.add(RichTextSpan(span.start, start, span.formats))
                }
                // Overlapping part — remove the format
                val overlapStart = maxOf(span.start, start)
                val overlapEnd = minOf(span.end, end)
                val remaining = span.formats - format
                if (remaining.isNotEmpty()) {
                    newSpans.add(RichTextSpan(overlapStart, overlapEnd, remaining))
                }
                // Part after overlap — keep original formats
                if (span.end > end) {
                    newSpans.add(RichTextSpan(end, span.end, span.formats))
                }
            }
        }

        _spans.clear()
        _spans.addAll(newSpans)
        normalize()
    }

    /**
     * Returns all formats active at the given cursor position.
     */
    fun getFormatsAt(position: Int): Set<RichTextFormat> {
        val formats = mutableSetOf<RichTextFormat>()
        for (span in _spans) {
            if (span.contains(position)) {
                formats.addAll(span.formats)
            }
        }
        return formats
    }

    /**
     * Returns formats that are active across the entire given range.
     * A format is "active" only if every character in the range has that format.
     */
    fun getFormatsInRange(start: Int, end: Int): Set<RichTextFormat> {
        if (start >= end) return emptySet()

        // Collect all formats that appear anywhere in the range
        val candidateFormats = mutableSetOf<RichTextFormat>()
        for (span in _spans) {
            if (span.overlaps(start, end)) {
                candidateFormats.addAll(span.formats)
            }
        }
        if (candidateFormats.isEmpty()) return emptySet()

        // For each candidate, verify it covers the entire range
        return candidateFormats.filter { format ->
            isFormatCoveringRange(start, end, format)
        }.toSet()
    }

    private fun isFormatCoveringRange(start: Int, end: Int, format: RichTextFormat): Boolean {
        val relevant = _spans.filter { it.overlaps(start, end) && format in it.formats }
            .sortedBy { it.start }
        if (relevant.isEmpty()) return false

        var cursor = start
        for (span in relevant) {
            val spanStart = maxOf(span.start, start)
            if (spanStart > cursor) return false // gap
            cursor = maxOf(cursor, minOf(span.end, end))
        }
        return cursor >= end
    }

    // ==================== Text Edit Adjustments ====================

    /**
     * Adjusts spans when text is inserted at [position] with [length] characters.
     *
     * Critical behavior: when inserting at a span's END position (span.end == position),
     * the span is EXTENDED to include the new text. This is what makes typing at the end
     * of a bold span continue the bold formatting — matching Flutter's behavior.
     *
     * - Spans entirely before the insertion (span.end < position): unchanged
     * - Spans whose end == position: extended (end += length)
     * - Spans that contain the position (start < position < end): extended (end += length)
     * - Spans starting at or after the position: shifted right
     */
    fun onTextInserted(position: Int, length: Int) {
        if (length <= 0) return

        // Shift link URL map keys that are at or after the insertion point
        val shiftedUrls = mutableMapOf<Int, String>()
        for ((key, url) in _linkUrlMap) {
            if (key >= position) {
                shiftedUrls[key + length] = url
            } else {
                shiftedUrls[key] = url
            }
        }
        _linkUrlMap.clear()
        _linkUrlMap.putAll(shiftedUrls)

        val newSpans = mutableListOf<RichTextSpan>()
        for (span in _spans) {
            when {
                // Span ends strictly before insertion — no change
                span.end < position -> newSpans.add(span)
                // Span starts strictly after insertion — shift right
                span.start > position -> newSpans.add(span.shift(length))
                // Span starts at insertion point and ends at insertion point (empty or at boundary)
                // AND span.start == position — shift right (don't extend a span that starts here)
                span.start == position && span.end == position -> newSpans.add(span.shift(length))
                // Span starts at insertion point but extends beyond — shift start stays, extend end
                span.start == position -> newSpans.add(span.copy(end = span.end + length))
                // LINK spans should NOT extend when typing at their end boundary.
                // Links have fixed boundaries — new text after a link should be plain.
                span.end == position && RichTextFormat.LINK in span.formats -> newSpans.add(span)
                // Span contains or ends at the insertion point — extend end
                else -> newSpans.add(span.copy(end = span.end + length))
            }
        }
        _spans.clear()
        _spans.addAll(newSpans)
    }

    /**
     * Splits any INLINE_CODE span that contains the newline at [newlinePosition].
     * Inline code is a single-line format — it should end before the newline.
     * The span is trimmed to end at [newlinePosition] (the position of the \n character).
     */
    fun splitInlineCodeAtNewline(newlinePosition: Int) {
        val newSpans = mutableListOf<RichTextSpan>()
        for (span in _spans) {
            if (RichTextFormat.INLINE_CODE in span.formats &&
                span.start < newlinePosition && span.end > newlinePosition) {
                // Trim the inline code span to end at the newline position
                newSpans.add(span.copy(end = newlinePosition))
            } else {
                newSpans.add(span)
            }
        }
        _spans.clear()
        _spans.addAll(newSpans)
    }

    /**
     * Adjusts spans when text is deleted from [start] to [end].
     * Spans within the deletion are removed. Overlapping spans are trimmed.
     */
    fun onTextDeleted(start: Int, end: Int) {
        if (start >= end) return
        val deleteLength = end - start

        // Adjust link URL map keys for the deletion
        val adjustedUrls = mutableMapOf<Int, String>()
        for ((key, url) in _linkUrlMap) {
            when {
                key >= end -> adjustedUrls[key - deleteLength] = url
                key >= start -> { /* key is within deletion range — remove it */ }
                else -> adjustedUrls[key] = url
            }
        }
        _linkUrlMap.clear()
        _linkUrlMap.putAll(adjustedUrls)

        val newSpans = mutableListOf<RichTextSpan>()
        for (span in _spans) {
            when {
                // Span is entirely before deletion — no change
                span.end <= start -> newSpans.add(span)
                // Span is entirely after deletion — shift left
                span.start >= end -> newSpans.add(span.shift(-deleteLength))
                // Span is entirely within deletion — remove it
                span.start >= start && span.end <= end -> { /* skip */ }
                // Span overlaps deletion start — trim end
                span.start < start && span.end <= end -> {
                    newSpans.add(span.copy(end = start))
                }
                // Span overlaps deletion end — trim start, shift
                span.start >= start && span.end > end -> {
                    newSpans.add(RichTextSpan(start, span.end - deleteLength, span.formats))
                }
                // Span contains entire deletion — shrink
                span.start < start && span.end > end -> {
                    newSpans.add(span.copy(end = span.end - deleteLength))
                }
            }
        }
        _spans.clear()
        _spans.addAll(newSpans)
        normalize()
    }

    // ==================== Serialization ====================

    /**
     * Converts the plain text + spans into markdown.
     * Inline formats wrap text with markers. Line-based formats (lists, blockquote)
     * are already stored as line prefixes in the text, so they pass through as-is.
     */
    fun toMarkdown(plainText: String): String {
        if (_spans.isEmpty()) return plainText

        // Build a list of "events" (format open/close) sorted by position
        data class FormatEvent(val position: Int, val format: RichTextFormat, val isOpen: Boolean)

        val events = mutableListOf<FormatEvent>()
        for (span in _spans) {
            for (format in span.formats) {
                if (isInlineFormat(format)) {
                    events.add(FormatEvent(span.start, format, true))
                    events.add(FormatEvent(span.end, format, false))
                }
            }
        }

        // Deduplicate events at the same position for the same format
        val deduped = events.groupBy { Triple(it.position, it.format, it.isOpen) }
            .map { it.value.first() }
            .sortedWith(compareBy<FormatEvent> { it.position }
                .thenBy { if (it.isOpen) 0 else 1 })

        // Collect link span ranges for whitespace normalization (Requirement 33.1)
        val linkRanges = _spans.filter { RichTextFormat.LINK in it.formats }
            .map { it.start to it.end }

        // Build the output by walking through the text and inserting markers
        val sb = StringBuilder()
        var cursor = 0
        // Group events by position
        val eventsByPos = deduped.groupBy { it.position }
        val positions = eventsByPos.keys.sorted()

        for (pos in positions) {
            // Append text up to this position
            if (pos > cursor) {
                val textChunk = plainText.substring(cursor, pos.coerceAtMost(plainText.length))
                sb.append(normalizeIfInLink(textChunk, cursor, linkRanges))
            }
            cursor = pos

            val eventsAtPos = eventsByPos[pos] ?: continue
            // Close events first (reverse order of opening), then open events
            val closes = eventsAtPos.filter { !it.isOpen }.sortedByDescending { formatPriority(it.format) }
            val opens = eventsAtPos.filter { it.isOpen }.sortedBy { formatPriority(it.format) }

            for (e in closes) {
                if (e.format == RichTextFormat.LINK) {
                    // Find the link span that ends at this position to get its URL
                    val linkSpan = _spans.firstOrNull { span ->
                        RichTextFormat.LINK in span.formats && span.end == pos
                    }
                    val url = linkSpan?.let { _linkUrlMap[it.start] } ?: ""
                    sb.append("](${url})")
                } else {
                    sb.append(closingMarker(e.format))
                }
            }
            for (e in opens) {
                sb.append(openingMarker(e.format))
            }
        }

        // Append remaining text
        if (cursor < plainText.length) {
            val textChunk = plainText.substring(cursor)
            sb.append(normalizeIfInLink(textChunk, cursor, linkRanges))
        }

        return sb.toString()
    }

    /**
     * Parses markdown text into plain text + spans.
     * Returns a Pair of (plainText, list of spans).
     */
    fun fromMarkdown(markdown: String): Pair<String, List<RichTextSpan>> {
        val plainBuilder = StringBuilder()
        val parsedSpans = mutableListOf<RichTextSpan>()

        // Process inline formats using regex
        var remaining = markdown

        // Parse inline patterns in order of marker length (longest first to avoid conflicts)
        data class InlinePattern(val regex: Regex, val format: RichTextFormat, val openLen: Int, val closeLen: Int)

        val patterns = listOf(
            InlinePattern(Regex("```([\\s\\S]*?)```"), RichTextFormat.CODE_BLOCK, 3, 3),
            InlinePattern(Regex("\\*\\*(.+?)\\*\\*"), RichTextFormat.BOLD, 2, 2),
            InlinePattern(Regex("~~(.+?)~~"), RichTextFormat.STRIKETHROUGH, 2, 2),
            InlinePattern(Regex("<u>(.+?)</u>"), RichTextFormat.UNDERLINE, 3, 4),
            InlinePattern(Regex("(?<!_)_([^_]+)_(?!_)"), RichTextFormat.ITALIC, 1, 1),
            InlinePattern(Regex("`([^`]+)`"), RichTextFormat.INLINE_CODE, 1, 1),
            InlinePattern(Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)"), RichTextFormat.LINK, 0, 0),
        )

        // Simple single-pass approach: find all matches, sort by position, strip markers
        data class Match(val start: Int, val end: Int, val format: RichTextFormat,
                         val contentStart: Int, val contentEnd: Int)

        val allMatches = mutableListOf<Match>()
        for (p in patterns) {
            p.regex.findAll(remaining).forEach { m ->
                allMatches.add(Match(
                    m.range.first, m.range.last + 1, p.format,
                    m.range.first + p.openLen,
                    m.range.last + 1 - p.closeLen
                ))
            }
        }

        // Sort by start position, remove overlapping
        val sorted = allMatches.sortedBy { it.start }.toMutableList()
        val filtered = mutableListOf<Match>()
        var lastEnd = 0
        for (m in sorted) {
            if (m.start >= lastEnd) {
                filtered.add(m)
                lastEnd = m.end
            }
        }

        // Build plain text and spans
        var srcCursor = 0
        for (m in filtered) {
            // Text before this match
            if (m.start > srcCursor) {
                plainBuilder.append(remaining.substring(srcCursor, m.start))
            }
            // Content of the match (without markers)
            val content = if (m.format == RichTextFormat.LINK) {
                // For links, extract display text (group 1) and URL (group 2)
                val linkRegex = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")
                val matchResult = linkRegex.find(remaining.substring(m.start, m.end))
                matchResult?.groupValues?.get(1) ?: remaining.substring(m.contentStart, m.contentEnd)
            } else {
                remaining.substring(m.contentStart, m.contentEnd)
            }
            val spanStart = plainBuilder.length
            plainBuilder.append(content)
            val spanEnd = plainBuilder.length
            if (spanEnd > spanStart) {
                parsedSpans.add(RichTextSpan(spanStart, spanEnd, setOf(m.format)))
                // For links, also extract and store the URL
                if (m.format == RichTextFormat.LINK) {
                    val linkRegex = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")
                    val matchResult = linkRegex.find(remaining.substring(m.start, m.end))
                    val url = matchResult?.groupValues?.get(2) ?: ""
                    if (url.isNotEmpty()) {
                        _linkUrlMap[spanStart] = url
                    }
                }
            }
            srcCursor = m.end
        }
        // Remaining text
        if (srcCursor < remaining.length) {
            plainBuilder.append(remaining.substring(srcCursor))
        }

        return plainBuilder.toString() to parsedSpans
    }

    // ==================== Normalization ====================

    /**
     * Normalizes spans: removes empty spans, sorts by start, merges adjacent spans
     * with identical format sets.
     */
    fun normalize() {
        // Remove empty spans
        _spans.removeAll { it.isEmpty || it.formats.isEmpty() }
        // Sort by start
        _spans.sortBy { it.start }
        // Merge adjacent spans with same formats
        val merged = mutableListOf<RichTextSpan>()
        for (span in _spans) {
            val last = merged.lastOrNull()
            if (last != null && last.end == span.start && last.formats == span.formats) {
                merged[merged.lastIndex] = last.copy(end = span.end)
            } else {
                merged.add(span)
            }
        }
        _spans.clear()
        _spans.addAll(merged)
    }

    /** Removes all spans. */
    fun clear() {
        _spans.clear()
        _linkUrlMap.clear()
    }

    // ==================== Helpers ====================

    private fun isInlineFormat(format: RichTextFormat): Boolean = when (format) {
        RichTextFormat.BOLD, RichTextFormat.ITALIC, RichTextFormat.UNDERLINE,
        RichTextFormat.STRIKETHROUGH, RichTextFormat.INLINE_CODE,
        RichTextFormat.CODE_BLOCK, RichTextFormat.LINK -> true
        RichTextFormat.BULLET_LIST, RichTextFormat.ORDERED_LIST,
        RichTextFormat.BLOCKQUOTE -> false
    }

    /**
     * Normalizes consecutive whitespace to a single space if the text chunk
     * falls within a LINK span range. Per Requirement 33.1.
     */
    private fun normalizeIfInLink(
        textChunk: String,
        chunkStart: Int,
        linkRanges: List<Pair<Int, Int>>
    ): String {
        val chunkEnd = chunkStart + textChunk.length
        val isInLink = linkRanges.any { (start, end) ->
            chunkStart >= start && chunkEnd <= end
        }
        return if (isInLink) {
            textChunk.replace(Regex("\\s{2,}"), " ")
        } else {
            textChunk
        }
    }

    /** Priority for nesting order (lower = outermost). */
    private fun formatPriority(format: RichTextFormat): Int = when (format) {
        RichTextFormat.CODE_BLOCK -> 0
        RichTextFormat.BOLD -> 1
        RichTextFormat.ITALIC -> 2
        RichTextFormat.UNDERLINE -> 3
        RichTextFormat.STRIKETHROUGH -> 4
        RichTextFormat.INLINE_CODE -> 5
        RichTextFormat.LINK -> 6
        else -> 10
    }

    private fun openingMarker(format: RichTextFormat): String = when (format) {
        RichTextFormat.BOLD -> "**"
        RichTextFormat.ITALIC -> "_"
        RichTextFormat.UNDERLINE -> "<u>"
        RichTextFormat.STRIKETHROUGH -> "~~"
        RichTextFormat.INLINE_CODE -> "`"
        RichTextFormat.CODE_BLOCK -> "```\n"
        RichTextFormat.LINK -> "[" // simplified — link URL handled separately
        else -> ""
    }

    private fun closingMarker(format: RichTextFormat): String = when (format) {
        RichTextFormat.BOLD -> "**"
        RichTextFormat.ITALIC -> "_"
        RichTextFormat.UNDERLINE -> "</u>"
        RichTextFormat.STRIKETHROUGH -> "~~"
        RichTextFormat.INLINE_CODE -> "`"
        RichTextFormat.CODE_BLOCK -> "\n```"
        RichTextFormat.LINK -> "]" // simplified
        else -> ""
    }
}
