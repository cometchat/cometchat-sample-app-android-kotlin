package com.cometchat.uikit.kotlin.presentation.conversations.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
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
     * @return SpannableString with Android text spans and no raw markdown markers
     */
    fun render(context: Context, markdown: String): SpannableString {
        if (markdown.isEmpty()) return SpannableString("")

        val segments = MarkdownRenderer.parse(markdown)
        val builder = SpannableStringBuilder()

        for ((index, segment) in segments.withIndex()) {
            if (index > 0 && builder.isNotEmpty()) {
                builder.append("\n")
            }
            when (segment) {
                is MarkdownRenderer.RenderedSegment.Text -> {
                    appendText(builder, segment.text)
                }
                is MarkdownRenderer.RenderedSegment.CodeBlock -> {
                    appendCodeBlock(builder, segment.code)
                }
                is MarkdownRenderer.RenderedSegment.BulletItem -> {
                    appendBulletItem(builder, segment.text)
                }
                is MarkdownRenderer.RenderedSegment.OrderedItem -> {
                    appendOrderedItem(builder, segment.number, segment.text)
                }
                is MarkdownRenderer.RenderedSegment.Blockquote -> {
                    appendBlockquote(builder, segment.text)
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
    private fun appendText(builder: SpannableStringBuilder, rawText: String) {
        val (stripped, spans) = MarkdownRenderer.parseInline(rawText)
        val start = builder.length
        builder.append(stripped)
        applyInlineSpans(builder, start, spans)
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
    private fun appendBulletItem(builder: SpannableStringBuilder, rawText: String) {
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
    }

    /**
     * Appends an ordered list item with a number prefix and inline spans.
     */
    private fun appendOrderedItem(
        builder: SpannableStringBuilder,
        number: Int,
        rawText: String
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
    }

    /**
     * Appends a blockquote segment with a "▎" vertical bar prefix and inline spans.
     */
    private fun appendBlockquote(builder: SpannableStringBuilder, rawText: String) {
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
