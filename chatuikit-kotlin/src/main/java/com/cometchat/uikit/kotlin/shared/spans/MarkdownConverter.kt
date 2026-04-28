package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.cometchat.uikit.core.formatter.MarkdownRenderer
import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Converts between span-formatted [Editable] and markdown strings.
 *
 * - [toMarkdown]: walks the Editable, collects [RichTextFormatSpan] boundaries,
 *   and emits markdown delimiters per the mapping table.
 * - [fromMarkdown]: uses [MarkdownRenderer.parse] from chatuikit-core to get
 *   segments, then applies corresponding format spans to a [SpannableStringBuilder].
 *
 * Handles nested/overlapping spans, link URL metadata, and code block fences
 * on separate lines.
 *
 * @see RichTextFormatSpan
 * @see MarkdownRenderer
 */
object MarkdownConverter {

    // ── Markdown delimiter mapping ──────────────────────────────────────

    private data class DelimiterPair(val open: String, val close: String)

    /** Inline formats that wrap text with open/close delimiters. */
    private val INLINE_DELIMITERS = mapOf(
        RichTextFormat.BOLD to DelimiterPair("**", "**"),
        RichTextFormat.ITALIC to DelimiterPair("_", "_"),
        RichTextFormat.STRIKETHROUGH to DelimiterPair("~~", "~~"),
        RichTextFormat.UNDERLINE to DelimiterPair("<u>", "</u>"),
        RichTextFormat.INLINE_CODE to DelimiterPair("`", "`")
    )

    /**
     * Canonical ordering for nested inline delimiters.
     * Outermost first so that `**_text_**` is produced rather than `_**text**_`.
     */
    private val INLINE_FORMAT_ORDER = listOf(
        RichTextFormat.BOLD,
        RichTextFormat.ITALIC,
        RichTextFormat.STRIKETHROUGH,
        RichTextFormat.UNDERLINE,
        RichTextFormat.INLINE_CODE
    )

    // ── toMarkdown ──────────────────────────────────────────────────────

    /**
     * Converts a span-formatted [Editable] into a markdown string.
     *
     * Algorithm:
     * 1. Split the editable into lines.
     * 2. For each line, check for block-level spans (CODE_BLOCK, BULLET_LIST,
     *    ORDERED_LIST, BLOCKQUOTE) and emit the appropriate prefix/fence.
     * 3. Within each line, collect inline span boundaries and emit open/close
     *    delimiters at the correct character positions.
     */
    fun toMarkdown(editable: Editable): String {
        if (editable.isEmpty()) return ""

        val text = editable.toString()
        val lines = splitIntoLines(text)
        val result = StringBuilder()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val lineStart = line.first
            val lineEnd = line.second

            // Check for code block span covering this line
            val codeBlockSpan = findBlockSpan(editable, lineStart, lineEnd, RichTextFormat.CODE_BLOCK)
            if (codeBlockSpan != null) {
                val cbStart = editable.getSpanStart(codeBlockSpan)
                val cbEnd = editable.getSpanEnd(codeBlockSpan)

                // Collect all lines covered by this code block span
                result.append("```\n")
                var j = i
                while (j < lines.size) {
                    val cbLine = lines[j]
                    if (cbLine.first >= cbEnd) break
                    val cbLineText = text.substring(cbLine.first, cbLine.second)
                    result.append(cbLineText)
                    j++
                    if (j < lines.size && lines[j].first < cbEnd) {
                        result.append("\n")
                    }
                }
                result.append("\n```")
                i = j
                if (i < lines.size) result.append("\n")
                continue
            }

            // Check for block-level line spans
            val bulletSpan = findBlockSpan(editable, lineStart, lineEnd, RichTextFormat.BULLET_LIST)
            val orderedSpan = findBlockSpan(editable, lineStart, lineEnd, RichTextFormat.ORDERED_LIST)
            val blockquoteSpan = findBlockSpan(editable, lineStart, lineEnd, RichTextFormat.BLOCKQUOTE)

            when {
                bulletSpan != null -> {
                    result.append("- ")
                    result.append(convertInlineSpans(editable, lineStart, lineEnd))
                }
                orderedSpan != null -> {
                    val number = if (orderedSpan is NumberedListFormatSpan) orderedSpan.number else 1
                    result.append("$number. ")
                    result.append(convertInlineSpans(editable, lineStart, lineEnd))
                }
                blockquoteSpan != null -> {
                    result.append("> ")
                    result.append(convertInlineSpans(editable, lineStart, lineEnd))
                }
                else -> {
                    result.append(convertInlineSpans(editable, lineStart, lineEnd))
                }
            }

            i++
            if (i < lines.size) result.append("\n")
        }

        return result.toString()
    }

    /**
     * Converts inline format spans within a character range to markdown text.
     *
     * Collects all [RichTextFormatSpan] boundaries in the range, sorts them,
     * and emits open/close delimiters at the correct positions.
     */
    private fun convertInlineSpans(editable: Editable, start: Int, end: Int): String {
        val spans = editable.getSpans(start, end, RichTextFormatSpan::class.java)
            .filter { span ->
                val format = span.getFormatType()
                format in INLINE_DELIMITERS || format == RichTextFormat.LINK
            }

        if (spans.isEmpty()) {
            return editable.subSequence(start, end).toString()
        }

        // Build boundary events: (position, isOpen, format, span)
        data class Event(
            val position: Int,
            val isOpen: Boolean,
            val format: RichTextFormat,
            val span: RichTextFormatSpan
        )

        val events = mutableListOf<Event>()
        for (span in spans) {
            val spanStart = maxOf(editable.getSpanStart(span), start)
            val spanEnd = minOf(editable.getSpanEnd(span), end)
            if (spanStart >= spanEnd) continue

            events.add(Event(spanStart, true, span.getFormatType(), span))
            events.add(Event(spanEnd, false, span.getFormatType(), span))
        }

        // Sort: by position, then closes before opens at same position,
        // then by format order (for consistent nesting)
        events.sortWith(compareBy<Event> { it.position }
            .thenBy { if (it.isOpen) 1 else 0 }
            .thenBy { formatOrder(it.format) })

        val text = editable.toString()
        val result = StringBuilder()
        var cursor = start

        for (event in events) {
            // Append plain text up to this event position
            if (event.position > cursor) {
                result.append(text, cursor, event.position)
                cursor = event.position
            }

            val format = event.format
            if (format == RichTextFormat.LINK) {
                val linkSpan = event.span as? LinkFormatSpan
                if (event.isOpen) {
                    result.append("[")
                } else {
                    val url = linkSpan?.url ?: ""
                    result.append("](")
                    result.append(url)
                    result.append(")")
                }
            } else {
                val delimiters = INLINE_DELIMITERS[format] ?: continue
                if (event.isOpen) {
                    result.append(delimiters.open)
                } else {
                    result.append(delimiters.close)
                }
            }
        }

        // Append remaining text
        if (cursor < end) {
            result.append(text, cursor, end)
        }

        return result.toString()
    }

    /** Returns a sort key for consistent delimiter nesting order. */
    private fun formatOrder(format: RichTextFormat): Int {
        return INLINE_FORMAT_ORDER.indexOf(format).let { if (it < 0) 100 else it }
    }

    // ── fromMarkdown ────────────────────────────────────────────────────

    /**
     * Parses a markdown string into a span-formatted [Editable].
     *
     * Uses [MarkdownRenderer.parse] to get structured segments, then applies
     * corresponding [RichTextFormatSpan] instances to a [SpannableStringBuilder].
     */
    fun fromMarkdown(markdown: String, context: Context?): Editable {
        if (markdown.isEmpty()) return SpannableStringBuilder("")

        val segments = MarkdownRenderer.parse(markdown)
        val builder = SpannableStringBuilder()

        for ((index, segment) in segments.withIndex()) {
            if (index > 0 && builder.isNotEmpty()) {
                builder.append("\n")
            }

            when (segment) {
                is MarkdownRenderer.RenderedSegment.Text -> {
                    appendTextSegment(builder, segment.text, segment.spans, context)
                }
                is MarkdownRenderer.RenderedSegment.CodeBlock -> {
                    appendCodeBlockSegment(builder, segment.code, context)
                }
                is MarkdownRenderer.RenderedSegment.BulletItem -> {
                    appendBulletItemSegment(builder, segment.text, segment.spans, context)
                }
                is MarkdownRenderer.RenderedSegment.OrderedItem -> {
                    appendOrderedItemSegment(builder, segment.number, segment.text, segment.spans, context)
                }
                is MarkdownRenderer.RenderedSegment.Blockquote -> {
                    appendBlockquoteSegment(builder, segment.text, segment.spans, context)
                }
            }
        }

        return builder
    }

    /**
     * Appends a plain text segment with inline format spans.
     *
     * [MarkdownRenderer.parse] stores raw markdown in [RenderedSegment.Text.text]
     * and inline span positions relative to the **stripped** text. We must call
     * [MarkdownRenderer.parseInline] to obtain the stripped plain text so that
     * the appended content contains no markdown markers.
     */
    private fun appendTextSegment(
        builder: SpannableStringBuilder,
        text: String,
        inlineSpans: List<MarkdownRenderer.InlineSpan>,
        context: Context?
    ) {
        val (strippedText, strippedSpans) = MarkdownRenderer.parseInline(text)
        val segmentStart = builder.length
        builder.append(strippedText)
        applyInlineSpans(builder, segmentStart, strippedSpans, context)
    }

    /** Appends a code block segment with a [CodeBlockFormatSpan]. */
    private fun appendCodeBlockSegment(
        builder: SpannableStringBuilder,
        code: String,
        context: Context?
    ) {
        val segmentStart = builder.length
        builder.append(code)
        val segmentEnd = builder.length

        val span = if (context != null) CodeBlockFormatSpan(context) else CodeBlockFormatSpan()
        builder.setSpan(span, segmentStart, segmentEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    /** Appends a bullet list item with a [BulletListFormatSpan]. */
    private fun appendBulletItemSegment(
        builder: SpannableStringBuilder,
        text: String,
        inlineSpans: List<MarkdownRenderer.InlineSpan>,
        context: Context?
    ) {
        val (strippedText, strippedSpans) = MarkdownRenderer.parseInline(text)
        val segmentStart = builder.length
        builder.append(strippedText)
        val segmentEnd = builder.length

        val span = if (context != null) BulletListFormatSpan(context) else BulletListFormatSpan()
        builder.setSpan(span, segmentStart, segmentEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        applyInlineSpans(builder, segmentStart, strippedSpans, context)
    }

    /** Appends an ordered list item with a [NumberedListFormatSpan]. */
    private fun appendOrderedItemSegment(
        builder: SpannableStringBuilder,
        number: Int,
        text: String,
        inlineSpans: List<MarkdownRenderer.InlineSpan>,
        context: Context?
    ) {
        val (strippedText, strippedSpans) = MarkdownRenderer.parseInline(text)
        val segmentStart = builder.length
        builder.append(strippedText)
        val segmentEnd = builder.length

        val span = if (context != null) NumberedListFormatSpan(number, context) else NumberedListFormatSpan(number)
        builder.setSpan(span, segmentStart, segmentEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        applyInlineSpans(builder, segmentStart, strippedSpans, context)
    }

    /** Appends a blockquote segment with a [BlockquoteFormatSpan]. */
    private fun appendBlockquoteSegment(
        builder: SpannableStringBuilder,
        text: String,
        inlineSpans: List<MarkdownRenderer.InlineSpan>,
        context: Context?
    ) {
        val (strippedText, strippedSpans) = MarkdownRenderer.parseInline(text)
        val segmentStart = builder.length
        builder.append(strippedText)
        val segmentEnd = builder.length

        val span = if (context != null) BlockquoteFormatSpan(context) else BlockquoteFormatSpan()
        builder.setSpan(span, segmentStart, segmentEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        applyInlineSpans(builder, segmentStart, strippedSpans, context)
    }

    /**
     * Applies inline format spans from [MarkdownRenderer.InlineSpan] to the builder.
     * Offsets are relative to [segmentStart].
     */
    private fun applyInlineSpans(
        builder: SpannableStringBuilder,
        segmentStart: Int,
        inlineSpans: List<MarkdownRenderer.InlineSpan>,
        context: Context?
    ) {
        for (inlineSpan in inlineSpans) {
            val spanStart = segmentStart + inlineSpan.start
            val spanEnd = segmentStart + inlineSpan.end
            if (spanStart < 0 || spanEnd > builder.length || spanStart >= spanEnd) continue

            val formatSpan = createFormatSpan(inlineSpan.format, context, inlineSpan.url)
            builder.setSpan(formatSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * Creates the appropriate [RichTextFormatSpan] for a given format type.
     */
    private fun createFormatSpan(
        format: RichTextFormat,
        context: Context?,
        url: String? = null
    ): RichTextFormatSpan {
        return when (format) {
            RichTextFormat.BOLD -> BoldFormatSpan()
            RichTextFormat.ITALIC -> ItalicFormatSpan()
            RichTextFormat.UNDERLINE -> UnderlineFormatSpan()
            RichTextFormat.STRIKETHROUGH -> StrikethroughFormatSpan()
            RichTextFormat.INLINE_CODE -> {
                if (context != null) InlineCodeFormatSpan(context) else InlineCodeFormatSpan()
            }
            RichTextFormat.CODE_BLOCK -> {
                if (context != null) CodeBlockFormatSpan(context) else CodeBlockFormatSpan()
            }
            RichTextFormat.BULLET_LIST -> {
                if (context != null) BulletListFormatSpan(context) else BulletListFormatSpan()
            }
            RichTextFormat.ORDERED_LIST -> {
                if (context != null) NumberedListFormatSpan(1, context) else NumberedListFormatSpan(1)
            }
            RichTextFormat.BLOCKQUOTE -> {
                if (context != null) BlockquoteFormatSpan(context) else BlockquoteFormatSpan()
            }
            RichTextFormat.LINK -> {
                val linkUrl = url ?: ""
                if (context != null) LinkFormatSpan(linkUrl, context) else LinkFormatSpan(linkUrl)
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Splits text into line ranges as pairs of (startIndex, endIndex).
     * Each range excludes the trailing newline character.
     */
    private fun splitIntoLines(text: String): List<Pair<Int, Int>> {
        val lines = mutableListOf<Pair<Int, Int>>()
        var start = 0
        for (i in text.indices) {
            if (text[i] == '\n') {
                lines.add(start to i)
                start = i + 1
            }
        }
        // Add the last line (even if empty, to handle trailing content)
        if (start <= text.length) {
            lines.add(start to text.length)
        }
        return lines
    }

    /**
     * Finds a block-level [RichTextFormatSpan] of the given format type
     * that covers any part of the line range [lineStart, lineEnd).
     */
    private fun findBlockSpan(
        editable: Editable,
        lineStart: Int,
        lineEnd: Int,
        format: RichTextFormat
    ): RichTextFormatSpan? {
        val spans = editable.getSpans(lineStart, lineEnd, RichTextFormatSpan::class.java)
        for (span in spans) {
            if (span.getFormatType() == format) {
                val spanStart = editable.getSpanStart(span)
                val spanEnd = editable.getSpanEnd(span)
                // Verify the span actually overlaps with the line
                if (spanStart < lineEnd && spanEnd > lineStart) {
                    return span
                }
            }
        }
        return null
    }
}
