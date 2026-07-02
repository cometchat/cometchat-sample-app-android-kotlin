package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.text.Editable
import android.text.Spanned
import com.cometchat.uikit.core.formatter.RichTextFormat

/**
 * Centralized span manager that handles applying, removing, and toggling
 * format spans on an [Editable].
 *
 * All methods guard against invalid ranges (start >= end, out of bounds)
 * and silently return without modification.
 *
 * Used by:
 * - [FormatSpanWatcher] to apply pending formats to newly typed characters
 * - Toolbar click handling to toggle formats on selected text
 * - [MarkdownConverter] indirectly via format detection
 *
 * @see RichTextFormatSpan
 * @see RichTextFormat
 */
object RichTextSpanManager {

    /**
     * Inline format types that apply to character ranges within a line.
     */
    private val TEXT_STYLE_FORMATS = setOf(
        RichTextFormat.BOLD,
        RichTextFormat.ITALIC,
        RichTextFormat.UNDERLINE,
        RichTextFormat.STRIKETHROUGH,
        RichTextFormat.INLINE_CODE,
        RichTextFormat.LINK
    )

    /**
     * Applies the corresponding [RichTextFormatSpan] for the given [format]
     * to the specified range `[start, end)` of the [editable].
     *
     * Silently returns if the range is invalid (start >= end or out of bounds).
     *
     * @param editable The Editable to apply the format to.
     * @param start    The start index (inclusive).
     * @param end      The end index (exclusive).
     * @param format   The format type to apply.
     * @param context  Optional context for theme-aware span creation.
     */
    fun applyFormat(
        editable: Editable,
        start: Int,
        end: Int,
        format: RichTextFormat,
        context: Context?
    ) {
        if (!isValidRange(editable, start, end)) return
        val span = createSpan(format, context)
        editable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    /**
     * Removes all [RichTextFormatSpan] instances of the specified [format]
     * from the given range `[start, end)` of the [editable].
     *
     * Silently returns if the range is invalid (start >= end or out of bounds).
     *
     * @param editable The Editable to remove the format from.
     * @param start    The start index (inclusive).
     * @param end      The end index (exclusive).
     * @param format   The format type to remove.
     */
    fun removeFormat(
        editable: Editable,
        start: Int,
        end: Int,
        format: RichTextFormat,
        context: Context? = null
    ) {
        if (!isValidRange(editable, start, end)) return

        val spans = editable.getSpans(start, end, RichTextFormatSpan::class.java)
        for (span in spans) {
            if (span.getFormatType() != format) continue

            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)

            editable.removeSpan(span)

            // Re-apply to portions outside the removal range
            if (spanStart < start) {
                val newSpan = createSpan(format, context)
                editable.setSpan(newSpan, spanStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (spanEnd > end) {
                val newSpan = createSpan(format, context)
                editable.setSpan(newSpan, end, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    /**
     * Toggles the specified [format] on the given range. If the range already
     * has the format, it is removed; otherwise it is added.
     *
     * Silently returns if the range is invalid (selStart >= selEnd or out of bounds).
     *
     * @param editable The Editable to toggle the format on.
     * @param format   The format type to toggle.
     * @param selStart The selection start index (inclusive).
     * @param selEnd   The selection end index (exclusive).
     * @param context  Optional context for theme-aware span creation.
     */
    fun toggleFormat(
        editable: Editable,
        format: RichTextFormat,
        selStart: Int,
        selEnd: Int,
        context: Context?
    ) {
        if (!isValidRange(editable, selStart, selEnd)) return

        // Check if the format fully covers the entire selection range.
        // "Fully covers" means every position in [selStart, selEnd) has the format.
        val fullyCovered = (selStart until selEnd).all { pos ->
            format in getFormatsAt(editable, pos)
        }

        if (fullyCovered) {
            // Remove only spans that fully cover the toggle range. This preserves
            // pre-existing partial spans that were layered underneath, ensuring
            // toggle is self-inverse.
            val spans = editable.getSpans(selStart, selEnd, RichTextFormatSpan::class.java)
            for (span in spans) {
                if (span.getFormatType() != format) continue
                val spanStart = editable.getSpanStart(span)
                val spanEnd = editable.getSpanEnd(span)
                // Only remove spans that cover the entire toggle range
                if (spanStart <= selStart && spanEnd >= selEnd) {
                    editable.removeSpan(span)
                    // Re-apply portions outside the toggle range
                    if (spanStart < selStart) {
                        val leftSpan = createSpan(format, context)
                        editable.setSpan(leftSpan, spanStart, selStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    if (spanEnd > selEnd) {
                        val rightSpan = createSpan(format, context)
                        editable.setSpan(rightSpan, selEnd, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        } else {
            applyFormat(editable, selStart, selEnd, format, context)
        }
    }

    /**
     * Returns the set of [RichTextFormat] types active at the given [position].
     *
     * Returns an empty set if the position is out of bounds.
     *
     * @param editable The Editable to query.
     * @param position The cursor position to check.
     * @return Set of active format types at the position.
     */
    fun getFormatsAt(editable: Editable, position: Int): Set<RichTextFormat> {
        if (position < 0 || position > editable.length) return emptySet()

        val result = mutableSetOf<RichTextFormat>()
        // Query spans that touch the position. Use max(position-1, 0) as start
        // so we also catch spans that end exactly at position (cursor at span boundary).
        // Use position+1 as end so we also catch spans that start exactly at position.
        val queryStart = maxOf(position - 1, 0)
        val queryEnd = minOf(position + 1, editable.length)
        val spans = editable.getSpans(queryStart, queryEnd, RichTextFormatSpan::class.java)
        for (span in spans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            // Include if position is inside the span OR at the span's end boundary.
            // At the end boundary the cursor is logically "continuing" the span,
            // so newly typed characters will inherit the format.
            if (position in spanStart..spanEnd && spanStart < spanEnd) {
                result.add(span.getFormatType())
            }
        }
        return result
    }

    /**
     * Returns the set of [RichTextFormat] types present anywhere in the
     * given range `[start, end)` of the [editable].
     *
     * Returns an empty set if the range is invalid.
     *
     * @param editable The Editable to query.
     * @param start    The start index (inclusive).
     * @param end      The end index (exclusive).
     * @return Set of format types found in the range.
     */
    fun getFormatsInRange(editable: Editable, start: Int, end: Int): Set<RichTextFormat> {
        if (!isValidRange(editable, start, end)) return emptySet()

        val result = mutableSetOf<RichTextFormat>()
        val spans = editable.getSpans(start, end, RichTextFormatSpan::class.java)
        for (span in spans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            // Check that the span actually overlaps with the range
            if (spanStart < end && spanEnd > start) {
                result.add(span.getFormatType())
            }
        }
        return result
    }

    /**
     * Returns `true` if the given [format] is an inline text style format
     * (BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, INLINE_CODE, LINK).
     *
     * @param format The format type to check.
     * @return `true` if the format is an inline text style.
     */
    fun isTextStyleFormat(format: RichTextFormat): Boolean {
        return format in TEXT_STYLE_FORMATS
    }

    /**
     * Factory method that creates the appropriate [RichTextFormatSpan] instance
     * for the given [format] type.
     *
     * @param format  The format type to create a span for.
     * @param context Optional context for theme-aware span creation.
     * @return A new [RichTextFormatSpan] instance for the format.
     * @throws IllegalArgumentException if the format type is not recognized.
     */
    fun createSpan(format: RichTextFormat, context: Context?): RichTextFormatSpan {
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
            RichTextFormat.LINK -> LinkFormatSpan("")
        }
    }

    /**
     * Applies a [LinkFormatSpan] with the given [url] to the specified range
     * `[start, end)` of the [editable]. Any existing [LinkFormatSpan] instances
     * in the range are removed first to avoid stacking.
     *
     * @param editable The Editable to apply the link to.
     * @param start    The start index (inclusive).
     * @param end      The end index (exclusive).
     * @param url      The URL to store in the link span.
     * @param context  Optional context for theme-aware span creation.
     */
    fun applyLinkFormat(
        editable: Editable,
        start: Int,
        end: Int,
        url: String,
        context: Context? = null
    ) {
        if (!isValidRange(editable, start, end)) return

        // Remove any existing link spans in the range
        removeFormat(editable, start, end, RichTextFormat.LINK)

        val linkSpan = if (context != null) LinkFormatSpan(url, context) else LinkFormatSpan(url)
        editable.setSpan(linkSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    /**
     * Returns the URL from a [LinkFormatSpan] found in the given range, or
     * `null` if no link span exists in the range.
     *
     * @param editable The Editable to query.
     * @param start    The start index (inclusive).
     * @param end      The end index (exclusive).
     * @return The URL string, or `null` if no link is found.
     */
    fun getLinkUrl(editable: Editable, start: Int, end: Int): String? {
        if (start < 0 || end < 0 || start > editable.length || end > editable.length) return null

        val spans = editable.getSpans(start, end, LinkFormatSpan::class.java)
        return spans.firstOrNull()?.url
    }

    /**
     * Validates that the range [start, end) is valid for the given editable.
     */
    private fun isValidRange(editable: Editable, start: Int, end: Int): Boolean {
        return start >= 0 && end >= 0 && start < end && start <= editable.length && end <= editable.length
    }
}
