package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context
import android.text.Editable
import android.text.SpanWatcher
import android.text.Spannable
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import com.cometchat.uikit.core.formatter.RichTextFormat
import java.util.EnumSet

/**
 * Manages format span boundaries during text changes in the WYSIWYG editor.
 *
 * Implements [SpanWatcher] to automatically handle span extension, shrinking,
 * and splitting when text is inserted or deleted within formatted regions.
 * Attach to an [Editable] via [attachTo] to enable automatic span management.
 *
 * Key behaviors:
 * - **Span Extension:** Text inserted within a formatted region extends the span.
 * - **Span Shrinking:** Text deleted from a formatted region shrinks the span;
 *   empty spans are removed.
 * - **Span Splitting:** Newline inserted within an inline span splits it into two.
 * - **Pending Formats:** Format toggled with no selection is stored as pending
 *   and applied to newly typed text.
 * - **Explicitly Disabled:** Format turned OFF while cursor is inside a formatted
 *   span prevents re-inheritance until cursor moves.
 *
 * Validates: Requirements 7.1, 7.2, 7.3
 *
 * @param context Optional context for theme-aware span creation.
 */
class FormatSpanWatcher(private var context: Context? = null) : SpanWatcher {

    /** Flag to prevent recursive span modifications. */
    private var isModifying = false

    /**
     * Pending formats to apply to newly typed text.
     *
     * When a format button is clicked with no text selected, the format is added
     * to this set. When the user types, these formats are applied to the new characters.
     */
    private val pendingFormats: MutableSet<RichTextFormat> = EnumSet.noneOf(RichTextFormat::class.java)

    /**
     * Explicitly disabled formats.
     *
     * When a user clicks a format button to turn OFF a format while the cursor
     * is inside formatted text, the format is added here. This prevents the format
     * from being re-detected from the existing span and shown as active.
     * Cleared when the cursor moves to a new position.
     */
    private val explicitlyDisabledFormats: MutableSet<RichTextFormat> = EnumSet.noneOf(RichTextFormat::class.java)

    // region Public API

    /**
     * Sets the context for creating theme-aware spans.
     */
    fun setContext(context: Context?) {
        this.context = context
    }

    /**
     * Returns a copy of the current pending formats.
     */
    fun getPendingFormats(): Set<RichTextFormat> =
        if (pendingFormats.isEmpty()) emptySet() else EnumSet.copyOf(pendingFormats)

    /**
     * Returns a copy of the explicitly disabled formats.
     */
    fun getExplicitlyDisabledFormats(): Set<RichTextFormat> =
        if (explicitlyDisabledFormats.isEmpty()) emptySet() else EnumSet.copyOf(explicitlyDisabledFormats)

    /**
     * Toggles a pending format.
     *
     * - If already pending → removed from pending, added to explicitly disabled.
     * - If explicitly disabled → removed from disabled, added to pending.
     * - Otherwise → added to pending.
     *
     * @return `true` if the format is now pending (active), `false` if removed/disabled.
     */
    fun togglePendingFormat(formatType: RichTextFormat): Boolean {
        return when {
            pendingFormats.contains(formatType) -> {
                pendingFormats.remove(formatType)
                explicitlyDisabledFormats.add(formatType)
                false
            }
            explicitlyDisabledFormats.contains(formatType) -> {
                explicitlyDisabledFormats.remove(formatType)
                pendingFormats.add(formatType)
                true
            }
            else -> {
                pendingFormats.add(formatType)
                true
            }
        }
    }

    /**
     * Checks if a format is pending.
     */
    fun isPendingFormat(formatType: RichTextFormat): Boolean = formatType in pendingFormats

    /**
     * Checks if a format is explicitly disabled.
     */
    fun isExplicitlyDisabled(formatType: RichTextFormat): Boolean = formatType in explicitlyDisabledFormats

    /**
     * Clears all pending formats and explicitly disabled formats.
     * Should be called when the cursor moves to a new position.
     */
    fun clearPendingFormats() {
        pendingFormats.clear()
        explicitlyDisabledFormats.clear()
    }

    /**
     * Sets the pending formats from a set, clearing explicitly disabled.
     */
    fun setPendingFormats(formats: Set<RichTextFormat>?) {
        pendingFormats.clear()
        explicitlyDisabledFormats.clear()
        if (formats != null) {
            pendingFormats.addAll(formats)
        }
    }

    /**
     * Marks a format as explicitly disabled without span update.
     */
    fun disableFormat(formatType: RichTextFormat) {
        pendingFormats.remove(formatType)
        explicitlyDisabledFormats.add(formatType)
    }

    /**
     * Marks a format as explicitly disabled and updates span flags to prevent
     * auto-extension at the cursor position.
     */
    fun disableFormatWithSpanUpdate(editable: Editable, format: RichTextFormat, cursorPos: Int) {
        pendingFormats.remove(format)
        explicitlyDisabledFormats.add(format)
        updateSpanFlagsAtPosition(editable, cursorPos, inclusive = false, targetFormat = format)
    }

    /**
     * Enables a format (removes from disabled, adds to pending).
     */
    fun enableFormat(formatType: RichTextFormat) {
        explicitlyDisabledFormats.remove(formatType)
        pendingFormats.add(formatType)
    }

    /**
     * Clears the explicitly disabled state for a format without adding to pending.
     */
    fun clearExplicitlyDisabled(formatType: RichTextFormat) {
        explicitlyDisabledFormats.remove(formatType)
    }

    /**
     * Enables a format and updates span flags to allow auto-extension.
     */
    fun enableFormatWithSpanUpdate(editable: Editable, format: RichTextFormat, cursorPos: Int) {
        explicitlyDisabledFormats.remove(format)
        pendingFormats.add(format)
        updateSpanFlagsAtPosition(editable, cursorPos, inclusive = true, targetFormat = format)
    }

    // endregion

    // region SpanWatcher overrides

    override fun onSpanAdded(text: Spannable, what: Any, start: Int, end: Int) {
        // No action needed — span management is handled through text change callbacks
    }

    override fun onSpanRemoved(text: Spannable, what: Any, start: Int, end: Int) {
        // No action needed
    }

    override fun onSpanChanged(
        text: Spannable, what: Any,
        ostart: Int, oend: Int,
        nstart: Int, nend: Int
    ) {
        // Main span management is done in handleTextChanged
    }

    // endregion

    // region Text change handling

    /**
     * Handles text changes to manage span extension, shrinking, and splitting.
     *
     * Should be called from a TextWatcher's `afterTextChanged` callback.
     * Also applies pending formats to newly typed text.
     *
     * @param editable The editable text that was changed.
     * @param start    The start position of the change.
     * @param before   The length of text that was removed.
     * @param count    The length of text that was inserted.
     */
    fun handleTextChanged(editable: Editable?, start: Int, before: Int, count: Int) {
        if (isModifying || editable == null) return

        isModifying = true
        try {
            when {
                count > before -> {
                    val insertedLength = count - before
                    handleTextInserted(editable, start, insertedLength)

                    // Apply pending formats to newly typed text
                    if (pendingFormats.isNotEmpty() && insertedLength > 0) {
                        applyPendingFormats(editable, start, start + insertedLength)
                    }
                }
                before > count -> {
                    val deletedLength = before - count
                    handleTextDeleted(editable, start, deletedLength)
                }
                // count == before → replacement, spans adjust automatically
            }
        } finally {
            isModifying = false
        }
    }

    // endregion

    // region Attach / Detach

    /**
     * Attaches this watcher to an [Editable], removing any existing watcher first.
     */
    fun attachTo(editable: Editable?) {
        if (editable == null) return
        val existing = editable.getSpans(0, editable.length, FormatSpanWatcher::class.java)
        for (watcher in existing) {
            editable.removeSpan(watcher)
        }
        editable.setSpan(this, 0, editable.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    }

    /**
     * Detaches this watcher from an [Editable].
     */
    fun detachFrom(editable: Editable?) {
        editable?.removeSpan(this)
    }

    // endregion

    // region Private — text insertion handling

    private fun handleTextInserted(editable: Editable, insertPosition: Int, insertedLength: Int) {
        val insertEnd = minOf(insertPosition + insertedLength, editable.length)
        val insertedText = editable.subSequence(insertPosition, insertEnd)
        val hasNewline = insertedText.contains('\n')

        val spans = editable.getSpans(0, editable.length, RichTextFormatSpan::class.java)

        for (span in spans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            val formatType = span.getFormatType()
            val isFormatDisabled = formatType in explicitlyDisabledFormats

            val wasInsideSpan = insertPosition > spanStart && insertPosition <= spanEnd
            val wasAtSpanEnd = insertPosition == spanEnd

            // Don't extend link spans at boundary — user types after a link
            if (formatType == RichTextFormat.LINK && wasAtSpanEnd) continue

            // Numbered list: truncate at newline so each line gets its own span
            if (hasNewline && formatType == RichTextFormat.ORDERED_LIST && wasInsideSpan) {
                truncateSpanAtNewline(editable, span, insertPosition, insertedLength)
                continue
            }

            // Bullet list: truncate at newline
            if (hasNewline && formatType == RichTextFormat.BULLET_LIST && wasInsideSpan) {
                truncateSpanAtNewline(editable, span, insertPosition, insertedLength)
                continue
            }

            // Blockquote spans extend across newlines (block-level)

            if (wasInsideSpan && !wasAtSpanEnd) {
                // Insertion strictly inside the span

                // If format is disabled and it's a text style, shrink back auto-extension
                if (isFormatDisabled && RichTextSpanManager.isTextStyleFormat(formatType)) {
                    val originalSpanEnd = spanEnd - insertedLength
                    if (insertPosition >= originalSpanEnd) {
                        val spanFlags = editable.getSpanFlags(span)
                        editable.removeSpan(span)
                        if (originalSpanEnd > spanStart) {
                            editable.setSpan(span, spanStart, originalSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        continue
                    }
                }

                if (hasNewline && shouldSplitOnNewline(span)) {
                    splitSpanAtNewline(editable, span, insertPosition, insertedLength)
                } else if (formatType == RichTextFormat.CODE_BLOCK) {
                    // Extend associated LeadingMarginSpan for code blocks
                    extendLeadingMarginSpansForCodeBlock(editable, spanStart, spanEnd)
                }
            } else if (wasAtSpanEnd && spanStart < spanEnd && !isFormatDisabled) {
                // Insertion at end boundary — manually extend
                if (!hasNewline || !shouldSplitOnNewline(span)) {
                    extendSpanToIncludeInsertedText(editable, span, spanStart, spanEnd, insertedLength)
                } else {
                    extendSpanToIncludeInsertedText(editable, span, spanStart, spanEnd, insertedLength)
                    val updatedStart = editable.getSpanStart(span)
                    val updatedEnd = editable.getSpanEnd(span)
                    if (updatedStart >= 0 && updatedEnd > updatedStart) {
                        splitSpanAtNewline(editable, span, insertPosition, insertedLength)
                    }
                }
            }
        }
    }

    // endregion

    // region Private — text deletion handling

    private fun handleTextDeleted(editable: Editable, deleteStart: Int, deletedLength: Int) {
        val spans = editable.getSpans(0, editable.length, RichTextFormatSpan::class.java)
        for (span in spans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            if (spanStart < 0 || spanEnd < 0 || spanStart >= spanEnd) {
                editable.removeSpan(span)
            }
        }

        // If all text cleared, reset state
        if (editable.isEmpty()) {
            clearPendingFormats()
            // Clean up lingering monospace TypefaceSpans from code blocks
            val typefaceSpans = editable.getSpans(0, 0, android.text.style.TypefaceSpan::class.java)
            for (ts in typefaceSpans) {
                if (ts.family == "monospace") {
                    editable.removeSpan(ts)
                }
            }
        }
    }

    // endregion

    // region Private — pending format application

    private fun applyPendingFormats(editable: Editable, start: Int, end: Int) {
        if (start >= end) return

        val insertedText = editable.subSequence(start, end)
        val isOnlyNewline = insertedText.length == 1 && insertedText[0] == '\n'

        for (formatType in pendingFormats) {
            // Skip list/blockquote/inline-code formats for newline characters
            if (isOnlyNewline && formatType in SKIP_ON_NEWLINE_FORMATS) continue

            // Skip if range is already covered by an existing span of the same type
            if (isRangeCoveredByFormat(editable, start, end, formatType)) continue

            // For block formats (list/blockquote), check the entire line — don't add
            // a second span if one already exists (e.g., from ListContinuationHandler)
            if (formatType in BLOCK_FORMATS && isLineAlreadyCoveredByBlockFormat(editable, start, formatType)) continue

            when (formatType) {
                RichTextFormat.ORDERED_LIST -> {
                    val nextNumber = calculateNextListNumber(editable, start)
                    val span = if (context != null) {
                        NumberedListFormatSpan(nextNumber, context!!)
                    } else {
                        NumberedListFormatSpan(nextNumber)
                    }
                    editable.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                RichTextFormat.BULLET_LIST -> {
                    val span = if (context != null) BulletListFormatSpan(context!!) else BulletListFormatSpan()
                    editable.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                RichTextFormat.INLINE_CODE -> {
                    val span = if (context != null) InlineCodeFormatSpan(context!!) else InlineCodeFormatSpan()
                    editable.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                RichTextFormat.BLOCKQUOTE -> {
                    val span = if (context != null) BlockquoteFormatSpan(context!!) else BlockquoteFormatSpan()
                    editable.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                else -> {
                    RichTextSpanManager.applyFormat(editable, start, end, formatType, context)
                }
            }
        }
        // Pending formats remain active until toggled off
    }

    /**
     * Checks if the line containing [position] already has a block format span
     * of the given type. Prevents duplicate list/blockquote spans on the same line.
     */
    private fun isLineAlreadyCoveredByBlockFormat(editable: Editable, position: Int, formatType: RichTextFormat): Boolean {
        val lineStart = findLineStartStatic(editable, position)
        val lineEnd = findLineEndStatic(editable, position)
        val searchEnd = maxOf(lineEnd, lineStart + 1).coerceAtMost(editable.length)
        val spans = editable.getSpans(lineStart, searchEnd, RichTextFormatSpan::class.java)
        return spans.any { it.getFormatType() == formatType }
    }

    private fun isRangeCoveredByFormat(
        editable: Editable, start: Int, end: Int, formatType: RichTextFormat
    ): Boolean {
        val spans = editable.getSpans(start, end, RichTextFormatSpan::class.java)
        return spans.any { span ->
            span.getFormatType() == formatType &&
                editable.getSpanStart(span) <= start &&
                editable.getSpanEnd(span) >= end
        }
    }

    private fun calculateNextListNumber(editable: Editable, position: Int): Int {
        if (position <= 0) return 1

        // Find start of current line
        var currentLineStart = position
        while (currentLineStart > 0 && editable[currentLineStart - 1] != '\n') {
            currentLineStart--
        }
        if (currentLineStart == 0) return 1

        // Find previous line
        val prevLineEnd = currentLineStart - 1
        var prevLineStart = prevLineEnd
        while (prevLineStart > 0 && editable[prevLineStart - 1] != '\n') {
            prevLineStart--
        }

        val spans = editable.getSpans(prevLineStart, prevLineEnd + 1, NumberedListFormatSpan::class.java)
        if (spans.isNullOrEmpty()) return 1

        return spans.maxOf { it.number } + 1
    }

    // endregion

    // region Private — span manipulation helpers

    private fun truncateSpanAtNewline(
        editable: Editable, span: RichTextFormatSpan,
        insertPosition: Int, insertedLength: Int
    ) {
        val spanStart = editable.getSpanStart(span)
        val spanFlags = editable.getSpanFlags(span)
        val insertEnd = insertPosition + insertedLength

        var newlinePos = -1
        for (i in insertPosition until minOf(insertEnd, editable.length)) {
            if (editable[i] == '\n') {
                newlinePos = i
                break
            }
        }
        if (newlinePos == -1) return

        if (spanStart < newlinePos) {
            editable.removeSpan(span)
            editable.setSpan(span, spanStart, newlinePos, spanFlags)
        } else {
            editable.removeSpan(span)
        }
    }

    private fun extendSpanToIncludeInsertedText(
        editable: Editable, span: RichTextFormatSpan,
        spanStart: Int, spanEnd: Int, insertedLength: Int
    ) {
        val spanFlags = editable.getSpanFlags(span)
        val newEnd = minOf(spanEnd + insertedLength, editable.length)

        if (newEnd > spanStart) {
            editable.removeSpan(span)
            editable.setSpan(span, spanStart, newEnd, spanFlags)

            if (span.getFormatType() == RichTextFormat.CODE_BLOCK) {
                extendLeadingMarginSpans(editable, spanStart, spanEnd, newEnd)
            }
        }
    }

    private fun extendLeadingMarginSpans(
        editable: Editable, spanStart: Int, originalEnd: Int, newEnd: Int
    ) {
        val marginSpans = editable.getSpans(spanStart, originalEnd, LeadingMarginSpan.Standard::class.java)
        for (marginSpan in marginSpans) {
            if (marginSpan is RichTextFormatSpan) continue // Don't touch list/blockquote spans
            val marginStart = editable.getSpanStart(marginSpan)
            val marginEnd = editable.getSpanEnd(marginSpan)
            val marginFlags = editable.getSpanFlags(marginSpan)
            if (marginStart == spanStart && marginEnd == originalEnd) {
                editable.removeSpan(marginSpan)
                editable.setSpan(marginSpan, marginStart, newEnd, marginFlags)
            }
        }
    }

    private fun extendLeadingMarginSpansForCodeBlock(
        editable: Editable, spanStart: Int, spanEnd: Int
    ) {
        val marginSpans = editable.getSpans(spanStart, spanEnd, LeadingMarginSpan.Standard::class.java)
        for (marginSpan in marginSpans) {
            if (marginSpan is RichTextFormatSpan) continue
            val marginStart = editable.getSpanStart(marginSpan)
            val marginEnd = editable.getSpanEnd(marginSpan)
            val marginFlags = editable.getSpanFlags(marginSpan)
            if (marginStart == spanStart && marginEnd < spanEnd) {
                editable.removeSpan(marginSpan)
                editable.setSpan(marginSpan, marginStart, spanEnd, marginFlags)
            }
        }
    }

    private fun splitSpanAtNewline(
        editable: Editable, span: RichTextFormatSpan,
        insertPosition: Int, insertedLength: Int
    ) {
        val spanStart = editable.getSpanStart(span)
        val spanEnd = editable.getSpanEnd(span)
        val spanFlags = editable.getSpanFlags(span)
        val formatType = span.getFormatType()
        val insertEnd = insertPosition + insertedLength

        var newlinePos = -1
        for (i in insertPosition until minOf(insertEnd, editable.length)) {
            if (editable[i] == '\n') {
                newlinePos = i
                break
            }
        }
        if (newlinePos == -1) return

        editable.removeSpan(span)

        // First half (before newline)
        if (spanStart < newlinePos) {
            createSpanCopy(span)?.let {
                editable.setSpan(it, spanStart, newlinePos, spanFlags)
            }
        }

        // Second half (after newline)
        val afterNewline = newlinePos + 1
        if (afterNewline < spanEnd) {
            createSpanCopy(span)?.let {
                editable.setSpan(it, afterNewline, spanEnd, spanFlags)
            }
        }

        // Continue inline text styles on the new line via pending
        if (shouldContinueFormatOnNewLine(formatType)) {
            pendingFormats.add(formatType)
            explicitlyDisabledFormats.remove(formatType)
        }
    }

    // endregion

    // region Private — span flag management

    private fun updateSpanFlagsAtPosition(
        editable: Editable, position: Int, inclusive: Boolean, targetFormat: RichTextFormat
    ) {
        if (position < 0 || position > editable.length) return

        val spans = editable.getSpans(0, editable.length, RichTextFormatSpan::class.java)
        for (span in spans) {
            if (span.getFormatType() != targetFormat) continue

            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)

            if (spanStart <= position && position <= spanEnd) {
                val newFlags = if (inclusive) {
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                } else {
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                }

                val currentFlags = editable.getSpanFlags(span)
                if ((currentFlags and Spanned.SPAN_INCLUSIVE_INCLUSIVE) !=
                    (newFlags and Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                ) {
                    editable.removeSpan(span)
                    editable.setSpan(span, spanStart, spanEnd, newFlags)

                    // Also update associated LeadingMarginSpan for code blocks
                    if (targetFormat == RichTextFormat.CODE_BLOCK) {
                        val marginSpans = editable.getSpans(
                            spanStart, spanEnd, LeadingMarginSpan.Standard::class.java
                        )
                        for (marginSpan in marginSpans) {
                            if (marginSpan is BlockquoteFormatSpan) continue
                            val marginStart = editable.getSpanStart(marginSpan)
                            val marginEnd = editable.getSpanEnd(marginSpan)
                            if (marginStart == spanStart && marginEnd == spanEnd) {
                                editable.removeSpan(marginSpan)
                                editable.setSpan(marginSpan, marginStart, marginEnd, newFlags)
                            }
                        }
                    }
                }
            }
        }
    }

    // endregion

    // region Private — format classification helpers

    private fun shouldSplitOnNewline(span: RichTextFormatSpan): Boolean {
        return when (span.getFormatType()) {
            RichTextFormat.BOLD,
            RichTextFormat.ITALIC,
            RichTextFormat.STRIKETHROUGH,
            RichTextFormat.UNDERLINE,
            RichTextFormat.INLINE_CODE,
            RichTextFormat.LINK -> true
            RichTextFormat.CODE_BLOCK,
            RichTextFormat.BULLET_LIST,
            RichTextFormat.ORDERED_LIST,
            RichTextFormat.BLOCKQUOTE -> false
        }
    }

    private fun shouldContinueFormatOnNewLine(formatType: RichTextFormat): Boolean {
        return when (formatType) {
            RichTextFormat.BOLD,
            RichTextFormat.ITALIC,
            RichTextFormat.STRIKETHROUGH,
            RichTextFormat.UNDERLINE -> true
            else -> false
        }
    }

    private fun createSpanCopy(original: RichTextFormatSpan): RichTextFormatSpan? {
        return when (original.getFormatType()) {
            RichTextFormat.BOLD -> BoldFormatSpan()
            RichTextFormat.ITALIC -> ItalicFormatSpan()
            RichTextFormat.UNDERLINE -> UnderlineFormatSpan()
            RichTextFormat.STRIKETHROUGH -> StrikethroughFormatSpan()
            RichTextFormat.INLINE_CODE -> {
                if (context != null) InlineCodeFormatSpan(context!!) else InlineCodeFormatSpan()
            }
            RichTextFormat.CODE_BLOCK -> {
                if (context != null) CodeBlockFormatSpan(context!!) else CodeBlockFormatSpan()
            }
            RichTextFormat.LINK -> {
                val url = if (original is LinkFormatSpan) original.url else ""
                LinkFormatSpan(url)
            }
            RichTextFormat.BULLET_LIST -> {
                if (context != null) BulletListFormatSpan(context!!) else BulletListFormatSpan()
            }
            RichTextFormat.ORDERED_LIST -> {
                val number = if (original is NumberedListFormatSpan) original.number else 1
                if (context != null) NumberedListFormatSpan(number, context!!) else NumberedListFormatSpan(number)
            }
            RichTextFormat.BLOCKQUOTE -> {
                if (context != null) BlockquoteFormatSpan(context!!) else BlockquoteFormatSpan()
            }
        }
    }

    // endregion

    // region Static helpers

    companion object {
        /** Formats that should not be applied to newline-only insertions. */
        private val SKIP_ON_NEWLINE_FORMATS = setOf(
            RichTextFormat.ORDERED_LIST,
            RichTextFormat.BULLET_LIST,
            RichTextFormat.BLOCKQUOTE,
            RichTextFormat.INLINE_CODE
        )

        /** Block-level formats that apply to entire lines (list, blockquote). */
        private val BLOCK_FORMATS = setOf(
            RichTextFormat.ORDERED_LIST,
            RichTextFormat.BULLET_LIST,
            RichTextFormat.BLOCKQUOTE,
            RichTextFormat.CODE_BLOCK
        )

        /** Finds the start of the line containing [position]. */
        private fun findLineStartStatic(text: CharSequence, position: Int): Int {
            if (position <= 0) return 0
            var i = position - 1
            while (i > 0 && text[i] != '\n') i--
            return if (i == 0 && text[0] != '\n') 0 else i + 1
        }

        /** Finds the end of the line containing [position]. */
        private fun findLineEndStatic(text: CharSequence, position: Int): Int {
            val length = text.length
            if (position >= length) return length
            var i = position
            while (i < length && text[i] != '\n') i++
            return i
        }

        /**
         * Extends a span to include newly inserted text at its boundary.
         */
        @JvmStatic
        fun extendSpan(editable: Editable?, span: RichTextFormatSpan?, newEnd: Int) {
            if (editable == null || span == null) return
            val spanStart = editable.getSpanStart(span)
            val spanFlags = editable.getSpanFlags(span)
            if (spanStart < 0 || newEnd <= spanStart || newEnd > editable.length) return
            editable.removeSpan(span)
            editable.setSpan(span, spanStart, newEnd, spanFlags)
        }

        /**
         * Shrinks a span by adjusting its boundaries. Removes if empty.
         */
        @JvmStatic
        fun shrinkSpan(editable: Editable?, span: RichTextFormatSpan?, newStart: Int, newEnd: Int) {
            if (editable == null || span == null) return
            if (newStart >= newEnd) {
                editable.removeSpan(span)
                return
            }
            val spanFlags = editable.getSpanFlags(span)
            val clampedStart = maxOf(0, newStart)
            val clampedEnd = minOf(editable.length, newEnd)
            if (clampedStart >= clampedEnd) {
                editable.removeSpan(span)
                return
            }
            editable.removeSpan(span)
            editable.setSpan(span, clampedStart, clampedEnd, spanFlags)
        }
    }

    // endregion
}
