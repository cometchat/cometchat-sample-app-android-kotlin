package com.cometchat.uikit.kotlin.presentation.conversations.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.LeadingMarginSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import com.cometchat.uikit.core.formatter.MarkdownRenderer
import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Renders markdown text into a fully formatted [SpannableString] for display
 * in conversation list subtitles. Uses [MarkdownRenderer.parse] to obtain
 * structured segments and converts them into standard Android text spans
 * (StyleSpan, StrikethroughSpan, UnderlineSpan, TypefaceSpan, etc.).
 *
 * The output contains no raw markdown markers — all formatting is expressed
 * via Android spans so that [CometChatConversationListItem.bindSubtitle]
 * displays rich text in the subtitleTextView.
 */
object ConversationSubtitleRenderer {

    /** Leading margin indent (in pixels) for list items and blockquotes. */
    private const val BLOCK_INDENT_PX = 16

    /**
     * Parses [markdown] via [MarkdownRenderer.parse] and produces a
     * [SpannableString] with full rich text formatting applied.
     *
     * @param context Android context (reserved for future theming)
     * @param markdown Raw markdown string from a TextMessage
     * @param formatterSpans Output of the [CometChatTextFormatter] pass that [markdown] was taken
     *   from (null when no formatters ran). Its spans — mentions above all — are re-applied on top
     *   of the marker-stripped text, remapped to their new offsets. Formatters must run BEFORE
     *   markdown parsing (as the text bubble does) for those offsets to line up.
     * @return SpannableString with Android text spans and no raw markdown markers
     */
    @JvmOverloads
    fun render(
        context: Context,
        markdown: String,
        formatterSpans: Spanned? = null
    ): SpannableString {
        if (markdown.isEmpty()) return SpannableString("")

        val segments = MarkdownRenderer.parse(markdown)
        val builder = SpannableStringBuilder()
        // Segments are matched back to their slice of [markdown] in order, so a repeated line
        // styles its own occurrence rather than the first one.
        var searchFrom = 0
        fun sourceStartOf(rawText: String): Int {
            val start = markdown.indexOf(rawText, searchFrom)
            if (start >= 0) searchFrom = start + rawText.length
            return start
        }

        for ((index, segment) in segments.withIndex()) {
            if (index > 0 && builder.isNotEmpty()) {
                builder.append("\n")
            }
            when (segment) {
                is MarkdownRenderer.RenderedSegment.Text -> {
                    appendText(builder, segment.text, formatterSpans, sourceStartOf(segment.text))
                }
                is MarkdownRenderer.RenderedSegment.CodeBlock -> {
                    appendCodeBlock(builder, segment.code)
                }
                is MarkdownRenderer.RenderedSegment.BulletItem -> {
                    appendBulletItem(builder, segment.text, formatterSpans, sourceStartOf(segment.text))
                }
                is MarkdownRenderer.RenderedSegment.OrderedItem -> {
                    appendOrderedItem(builder, segment.number, segment.text, formatterSpans, sourceStartOf(segment.text))
                }
                is MarkdownRenderer.RenderedSegment.Blockquote -> {
                    appendBlockquote(builder, segment.text, formatterSpans, sourceStartOf(segment.text))
                }
            }
        }

        return SpannableString(builder)
    }

    // ── Segment handlers ────────────────────────────────────────────────

    /**
     * Appends a plain text segment, stripping inline markdown markers and
     * applying corresponding Android spans.
     */
    private fun appendText(
        builder: SpannableStringBuilder,
        rawText: String,
        formatterSpans: Spanned? = null,
        sourceStart: Int = -1
    ) {
        val (stripped, spans) = MarkdownRenderer.parseInline(rawText)
        val start = builder.length
        builder.append(stripped)
        applyInlineSpans(builder, start, spans)
        applyFormatterSpans(builder, start, rawText, stripped, formatterSpans, sourceStart)
    }

    /**
     * Appends a code block segment with monospace typeface.
     */
    private fun appendCodeBlock(builder: SpannableStringBuilder, code: String) {
        val start = builder.length
        builder.append(code)
        val end = builder.length
        builder.setSpan(
            TypefaceSpan("monospace"),
            start, end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    /**
     * Appends a bullet list item with a bullet prefix character and inline spans.
     */
    private fun appendBulletItem(
        builder: SpannableStringBuilder,
        rawText: String,
        formatterSpans: Spanned? = null,
        sourceStart: Int = -1
    ) {
        val (stripped, spans) = MarkdownRenderer.parseInline(rawText)
        val start = builder.length
        builder.append("• ")
        builder.append(stripped)
        val end = builder.length
        builder.setSpan(
            LeadingMarginSpan.Standard(BLOCK_INDENT_PX),
            start, end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        // Inline spans offset by 2 for the "• " prefix
        applyInlineSpans(builder, start + 2, spans)
        applyFormatterSpans(builder, start + 2, rawText, stripped, formatterSpans, sourceStart)
    }

    /**
     * Appends an ordered list item with a number prefix and inline spans.
     */
    private fun appendOrderedItem(
        builder: SpannableStringBuilder,
        number: Int,
        rawText: String,
        formatterSpans: Spanned? = null,
        sourceStart: Int = -1
    ) {
        val (stripped, spans) = MarkdownRenderer.parseInline(rawText)
        val prefix = "$number. "
        val start = builder.length
        builder.append(prefix)
        builder.append(stripped)
        val end = builder.length
        builder.setSpan(
            LeadingMarginSpan.Standard(BLOCK_INDENT_PX),
            start, end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        applyInlineSpans(builder, start + prefix.length, spans)
        applyFormatterSpans(builder, start + prefix.length, rawText, stripped, formatterSpans, sourceStart)
    }

    /**
     * Appends a blockquote segment with a "▎" vertical bar prefix and inline spans.
     */
    private fun appendBlockquote(
        builder: SpannableStringBuilder,
        rawText: String,
        formatterSpans: Spanned? = null,
        sourceStart: Int = -1
    ) {
        val (stripped, spans) = MarkdownRenderer.parseInline(rawText)
        val start = builder.length
        builder.append("▎ ")
        builder.append(stripped)
        val end = builder.length
        builder.setSpan(
            LeadingMarginSpan.Standard(BLOCK_INDENT_PX),
            start, end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.setSpan(
            StyleSpan(Typeface.ITALIC),
            start, end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        applyInlineSpans(builder, start + 2, spans)
        applyFormatterSpans(builder, start + 2, rawText, stripped, formatterSpans, sourceStart)
    }

    // ── Formatter span overlay ──────────────────────────────────────────

    /**
     * Re-applies the spans a [CometChatTextFormatter] produced (mention spans, chiefly) on top of
     * this segment's stripped text. [sourceStart] is where the segment's raw text begins in the
     * formatter output — the offsets those spans are indexed against — and stripping the markdown
     * markers shifts everything left of it, so each offset is remapped through [buildPositionMap].
     */
    private fun applyFormatterSpans(
        builder: SpannableStringBuilder,
        segmentStart: Int,
        rawText: String,
        stripped: String,
        formatterSpans: Spanned?,
        sourceStart: Int
    ) {
        if (formatterSpans == null || sourceStart < 0) return
        val positionMap = buildPositionMap(rawText, stripped)
        val sourceEnd = sourceStart + rawText.length

        // CharacterStyle only — the formatter's SpannableStringBuilder also carries non-visual
        // bookkeeping spans that must not be copied onto the caption.
        for (span in formatterSpans.getSpans(sourceStart, sourceEnd, CharacterStyle::class.java)) {
            val spanStart = formatterSpans.getSpanStart(span)
            val spanEnd = formatterSpans.getSpanEnd(span)
            if (spanStart >= sourceEnd || spanEnd <= sourceStart) continue

            val relativeStart = (spanStart - sourceStart).coerceAtLeast(0)
            val relativeEnd = (spanEnd - sourceStart).coerceAtMost(rawText.length)
            val start = segmentStart + mapPosition(relativeStart, positionMap, stripped.length)
            val end = segmentStart + mapPosition(relativeEnd, positionMap, stripped.length)
            if (start in 0 until end && end <= builder.length) {
                builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    /**
     * Maps each position in [original] to its position in [stripped] (same text minus the markdown
     * markers), by walking both in step and only advancing the stripped cursor on a match.
     */
    private fun buildPositionMap(original: String, stripped: String): IntArray {
        val map = IntArray(original.length + 1) { -1 }
        var strippedIdx = 0
        var origIdx = 0
        while (origIdx < original.length && strippedIdx < stripped.length) {
            if (original[origIdx] == stripped[strippedIdx]) {
                map[origIdx] = strippedIdx
                strippedIdx++
            }
            origIdx++
        }
        map[original.length] = stripped.length
        return map
    }

    /** Maps one position through [buildPositionMap], falling back to the nearest mapped one. */
    private fun mapPosition(pos: Int, map: IntArray, strippedLength: Int): Int {
        if (pos < 0) return 0
        if (pos >= map.size) return strippedLength
        if (map[pos] >= 0) return map[pos]
        for (i in pos downTo 0) {
            if (map[i] >= 0) return map[i]
        }
        return 0
    }

    // ── Inline span application ─────────────────────────────────────────

    /**
     * Converts [MarkdownRenderer.InlineSpan] entries into standard Android
     * text spans and applies them to [builder] offset by [segmentStart].
     */
    private fun applyInlineSpans(
        builder: SpannableStringBuilder,
        segmentStart: Int,
        inlineSpans: List<MarkdownRenderer.InlineSpan>
    ) {
        for (span in inlineSpans) {
            val start = segmentStart + span.start
            val end = segmentStart + span.end
            if (start < 0 || end > builder.length || start >= end) continue

            val androidSpan = toAndroidSpan(span.format) ?: continue
            builder.setSpan(androidSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * Maps a [RichTextFormat] to the corresponding standard Android span.
     * Returns `null` for formats that have no simple single-span representation
     * (LINK is rendered as styled text via underline + italic).
     */
    private fun toAndroidSpan(format: RichTextFormat): Any? {
        return when (format) {
            RichTextFormat.BOLD -> StyleSpan(Typeface.BOLD)
            RichTextFormat.ITALIC -> StyleSpan(Typeface.ITALIC)
            RichTextFormat.STRIKETHROUGH -> StrikethroughSpan()
            RichTextFormat.UNDERLINE -> UnderlineSpan()
            RichTextFormat.INLINE_CODE -> TypefaceSpan("monospace")
            RichTextFormat.CODE_BLOCK -> TypefaceSpan("monospace")
            RichTextFormat.LINK -> UnderlineSpan() // display link text with underline styling
            RichTextFormat.BULLET_LIST,
            RichTextFormat.ORDERED_LIST,
            RichTextFormat.BLOCKQUOTE -> null // handled at block level
        }
    }
}
