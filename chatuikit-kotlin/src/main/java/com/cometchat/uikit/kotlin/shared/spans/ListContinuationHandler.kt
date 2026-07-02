package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.text.Editable
import android.text.Spanned

/**
 * Manages list and blockquote continuation behavior on Enter key press.
 *
 * Handles two scenarios:
 * - **Non-empty line**: Inserts a new line with the appropriate prefix
 *   (bullet, next number, or blockquote marker) and applies the corresponding span.
 * - **Empty prefix-only line**: Removes the prefix and exits the current mode.
 *
 * Also provides [renumberList] to maintain sequential numbering across a
 * contiguous block of [NumberedListFormatSpan] items.
 *
 * Validates: Requirements 10.1–10.6, 11.1
 *
 * @see BulletListFormatSpan
 * @see NumberedListFormatSpan
 * @see BlockquoteFormatSpan
 */
object ListContinuationHandler {

    /**
     * Called after a newline is inserted. Checks the previous line for a
     * list or blockquote span and either continues the mode on the new line
     * or exits the mode if the previous line was empty (prefix only).
     *
     * @param editable  The editable text to modify.
     * @param cursorPos The cursor position **after** the newline character
     *                  has already been inserted (i.e. the start of the new line).
     * @param context   Optional context for theme-aware span creation.
     * @return `true` if the handler modified the editable; `false` if no
     *         list/blockquote handling was needed.
     */
    fun handleNewline(editable: Editable, cursorPos: Int, context: Context?): Boolean {
        if (cursorPos <= 0 || cursorPos > editable.length) return false

        // The newline was already inserted at cursorPos - 1.
        // We need to inspect the *previous* line (the line before the newline).
        val newlineIndex = cursorPos - 1
        val prevLineEnd = newlineIndex // exclusive – points at the '\n'
        val prevLineStart = findLineStart(editable, prevLineEnd)

        // If the previous line is completely blank (no content at all),
        // remove any list/blockquote spans at that position and delete the
        // newline we just typed to exit the mode.
        if (prevLineStart >= prevLineEnd) {
            // Remove any list/blockquote format spans at the blank line position
            val spansAtPos = editable.getSpans(prevLineStart, prevLineStart + 1, RichTextFormatSpan::class.java)
            for (span in spansAtPos) {
                if (span is BulletListFormatSpan || span is NumberedListFormatSpan || span is BlockquoteFormatSpan) {
                    editable.removeSpan(span)
                }
            }
            // Also remove zero-length MARK_MARK spans at the position
            val zeroLenSpans = editable.getSpans(prevLineStart, prevLineStart, RichTextFormatSpan::class.java)
            for (span in zeroLenSpans) {
                if (span is BulletListFormatSpan || span is NumberedListFormatSpan || span is BlockquoteFormatSpan) {
                    editable.removeSpan(span)
                }
            }

            if (newlineIndex >= 0 && newlineIndex < editable.length && editable[newlineIndex] == '\n') {
                editable.delete(newlineIndex, newlineIndex + 1)
            }
            return true
        }

        // Check for bullet list span on the previous line
        val bulletSpans = editable.getSpans(prevLineStart, prevLineEnd, BulletListFormatSpan::class.java)
        if (bulletSpans.isNotEmpty()) {
            val span = bulletSpans[0]
            return handleBulletContinuation(editable, span, prevLineStart, prevLineEnd, cursorPos, context)
        }

        // Check for numbered list span on the previous line
        val numberedSpans = editable.getSpans(prevLineStart, prevLineEnd, NumberedListFormatSpan::class.java)
        if (numberedSpans.isNotEmpty()) {
            val span = numberedSpans[0]
            return handleNumberedContinuation(editable, span, prevLineStart, prevLineEnd, cursorPos, context)
        }

        // Check for blockquote span on the previous line
        val blockquoteSpans = editable.getSpans(prevLineStart, prevLineEnd, BlockquoteFormatSpan::class.java)
        if (blockquoteSpans.isNotEmpty()) {
            val span = blockquoteSpans[0]
            return handleBlockquoteContinuation(editable, span, prevLineStart, prevLineEnd, cursorPos, context)
        }

        return false
    }

    /**
     * Renumbers all [NumberedListFormatSpan] instances in the contiguous
     * numbered list block that contains [lineStart], maintaining sequential
     * ordering starting from 1.
     *
     * A contiguous block is defined as a sequence of numbered list spans
     * where there is no non-newline gap between consecutive spans.
     *
     * @param editable  The editable text to modify.
     * @param lineStart A position within the numbered list block to renumber.
     */
    fun renumberList(editable: Editable, lineStart: Int) {
        if (editable.isEmpty()) return

        val allSpans = editable.getSpans(0, editable.length, NumberedListFormatSpan::class.java)
        if (allSpans.isNullOrEmpty()) return

        // Sort spans by their start position
        val sorted = allSpans.sortedBy { editable.getSpanStart(it) }

        // Find contiguous groups and renumber the group containing lineStart
        var groupStart = 0
        while (groupStart < sorted.size) {
            // Build a contiguous group
            var groupEnd = groupStart
            while (groupEnd + 1 < sorted.size) {
                val currentEnd = editable.getSpanEnd(sorted[groupEnd])
                val nextStart = editable.getSpanStart(sorted[groupEnd + 1])
                if (currentEnd < 0 || nextStart < 0) break
                if (hasNonNewlineGap(editable, currentEnd, nextStart)) break
                groupEnd++
            }

            // Check if lineStart falls within this group's range
            val groupSpanStart = editable.getSpanStart(sorted[groupStart])
            val groupSpanEnd = editable.getSpanEnd(sorted[groupEnd])
            if (groupSpanStart >= 0 && groupSpanEnd >= 0 &&
                lineStart >= groupSpanStart && lineStart <= groupSpanEnd
            ) {
                // Renumber this group sequentially from 1
                var number = 1
                for (i in groupStart..groupEnd) {
                    if (sorted[i].number != number) {
                        sorted[i].setNumber(number)
                    }
                    number++
                }
                return
            }

            groupStart = groupEnd + 1
        }
    }

    // ==================== Bullet List ====================

    private fun handleBulletContinuation(
        editable: Editable,
        span: BulletListFormatSpan,
        prevLineStart: Int,
        prevLineEnd: Int,
        cursorPos: Int,
        context: Context?
    ): Boolean {
        val lineContent = getLineContent(editable, prevLineStart, prevLineEnd)

        return if (lineContent.isBlank()) {
            // Empty line (prefix only) → exit bullet list mode
            exitListMode(editable, span, prevLineStart, prevLineEnd)
            true
        } else {
            // Non-empty → continue with a new bullet item
            val newSpan = if (context != null) BulletListFormatSpan(context) else BulletListFormatSpan()
            insertContinuationSpan(editable, newSpan, cursorPos)
            true
        }
    }

    // ==================== Numbered List ====================

    private fun handleNumberedContinuation(
        editable: Editable,
        span: NumberedListFormatSpan,
        prevLineStart: Int,
        prevLineEnd: Int,
        cursorPos: Int,
        context: Context?
    ): Boolean {
        val lineContent = getLineContent(editable, prevLineStart, prevLineEnd)

        return if (lineContent.isBlank()) {
            // Empty line (prefix only) → exit numbered list mode
            exitListMode(editable, span, prevLineStart, prevLineEnd)
            true
        } else {
            // Non-empty → continue with next sequential number
            val nextNumber = span.number + 1
            val newSpan = if (context != null) {
                NumberedListFormatSpan(nextNumber, context)
            } else {
                NumberedListFormatSpan(nextNumber)
            }
            insertContinuationSpan(editable, newSpan, cursorPos)
            // Renumber the list block to keep numbering consistent
            renumberList(editable, cursorPos)
            true
        }
    }

    // ==================== Blockquote ====================

    private fun handleBlockquoteContinuation(
        editable: Editable,
        span: BlockquoteFormatSpan,
        prevLineStart: Int,
        prevLineEnd: Int,
        cursorPos: Int,
        context: Context?
    ): Boolean {
        val lineContent = getLineContent(editable, prevLineStart, prevLineEnd)

        return if (lineContent.isBlank()) {
            // Empty line (prefix only) → exit blockquote mode
            exitListMode(editable, span, prevLineStart, prevLineEnd)
            true
        } else {
            // Non-empty → continue blockquote on the new line
            val newSpan = if (context != null) BlockquoteFormatSpan(context) else BlockquoteFormatSpan()
            insertContinuationSpan(editable, newSpan, cursorPos)
            true
        }
    }

    // ==================== Shared Helpers ====================

    /**
     * Inserts a continuation span on the new line starting at [cursorPos].
     * The span covers from [cursorPos] to the end of that new line.
     */
    private fun insertContinuationSpan(
        editable: Editable,
        span: RichTextFormatSpan,
        cursorPos: Int
    ) {
        val lineEnd = findLineEnd(editable, cursorPos)
        val spanEnd = maxOf(cursorPos, lineEnd)

        if (spanEnd > cursorPos) {
            editable.setSpan(span, cursorPos, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        } else {
            // Empty new line — use SPAN_MARK_MARK so it expands when text is typed
            editable.setSpan(span, cursorPos, cursorPos, Spanned.SPAN_MARK_MARK)
        }
    }

    /**
     * Exits list/blockquote mode by removing the span from the previous
     * (empty) line and deleting the empty prefix line content plus the
     * newline that was just inserted.
     */
    private fun exitListMode(
        editable: Editable,
        span: Any,
        prevLineStart: Int,
        prevLineEnd: Int
    ) {
        val spanStart = editable.getSpanStart(span)
        val spanEnd = editable.getSpanEnd(span)

        if (spanStart < 0 || spanEnd < 0) return

        // The user pressed Enter on a blank/empty line that has a list/blockquote span.
        // We need to:
        // 1. Truncate the span so it no longer covers the blank line area
        // 2. Delete the newline that was just typed (at prevLineEnd or cursorPos-1)
        
        // Find the newline that was just inserted (it's at cursorPos - 1, which is
        // after prevLineEnd). We need to delete it to "undo" the Enter.
        // The newline is at prevLineEnd position (the end of the blank line).
        val newlineToDelete = prevLineEnd
        
        // First, truncate the span to end before the blank line area.
        // The blank line starts at prevLineStart. The content before it ends at
        // prevLineStart - 1 (the newline that created the blank line).
        // We want the span to cover up to prevLineStart (exclusive), which means
        // it ends at the newline character that separates content from the blank line.
        editable.removeSpan(span)
        
        if (spanStart < prevLineStart && prevLineStart > 0) {
            // Re-apply span to the content portion only (before the blank line)
            val newSpan = cloneSpan(span)
            if (newSpan != null) {
                // End at prevLineStart - 1 to exclude the trailing newline,
                // or at prevLineStart to include it (spans typically include the newline)
                val truncatedEnd = prevLineStart
                if (spanStart < truncatedEnd) {
                    editable.setSpan(newSpan, spanStart, truncatedEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
            }
        }

        // Delete the newline that was just typed
        if (newlineToDelete >= 0 && newlineToDelete < editable.length && editable[newlineToDelete] == '\n') {
            editable.delete(newlineToDelete, newlineToDelete + 1)
        }
    }

    /**
     * Re-applies a span of the same type to the range before the removed line.
     */
    private fun reapplySpanBefore(editable: Editable, original: Any, start: Int, end: Int) {
        if (start >= end) return
        val newSpan = cloneSpan(original) ?: return
        editable.setSpan(newSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    /**
     * Re-applies a span of the same type to the range after the removed line.
     */
    private fun reapplySpanAfter(editable: Editable, original: Any, start: Int, end: Int) {
        val clampedEnd = minOf(end, editable.length)
        if (start >= clampedEnd) return
        val newSpan = cloneSpan(original) ?: return
        editable.setSpan(newSpan, start, clampedEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    }

    /**
     * Creates a shallow clone of a list/blockquote span preserving its styling.
     */
    private fun cloneSpan(span: Any): Any? {
        return when (span) {
            is BulletListFormatSpan -> BulletListFormatSpan().also {
                it.setBulletColor(span.getBulletColor())
                it.setBulletRadius(span.getBulletRadius())
                it.setGapWidth(span.getGapWidth())
            }
            is NumberedListFormatSpan -> NumberedListFormatSpan(span.number).also {
                it.setTextColor(span.getTextColor())
                it.setGapWidth(span.getGapWidth())
            }
            is BlockquoteFormatSpan -> BlockquoteFormatSpan().also {
                it.setStripeColor(span.getStripeColor())
                it.setStripeWidth(span.getStripeWidth())
                it.setGapWidth(span.getGapWidth())
                it.setBackgroundColor(span.getBackgroundColor())
            }
            else -> null
        }
    }

    /**
     * Finds the start of the line containing [position].
     * Returns the index of the first character on that line.
     */
    private fun findLineStart(text: CharSequence, position: Int): Int {
        if (position <= 0) return 0
        var i = position - 1
        while (i > 0 && text[i] != '\n') i--
        return if (i == 0 && text[0] != '\n') 0 else i + 1
    }

    /**
     * Finds the end of the line containing [position].
     * Returns the index of the newline character or the text length.
     */
    private fun findLineEnd(text: CharSequence, position: Int): Int {
        val length = text.length
        if (position >= length) return length
        var i = position
        while (i < length && text[i] != '\n') i++
        return i
    }

    /**
     * Extracts the text content of a line (excluding any trailing newline).
     */
    private fun getLineContent(text: CharSequence, lineStart: Int, lineEnd: Int): String {
        if (lineStart >= lineEnd || lineStart < 0 || lineEnd > text.length) return ""
        return text.subSequence(lineStart, lineEnd).toString()
    }

    /**
     * Returns `true` if there is any non-newline character between [start] and [end].
     */
    private fun hasNonNewlineGap(text: CharSequence, start: Int, end: Int): Boolean {
        for (i in start until end) {
            if (i < text.length && text[i] != '\n') return true
        }
        return false
    }
}
