package com.cometchat.uikit.core.formatter

/**
 * Orchestrates rich text editing operations. Bridges the UI layer (Compose or XML)
 * with the core span engine ([RichTextSpanManager]) via [RichTextEditorState].
 *
 * Key behaviors (matching Flutter's RichTextEditingController):
 *
 * 1. **Typing inside a formatted span** — new characters inherit the span's formats.
 *    The span extends automatically because [RichTextSpanManager.onTextInserted] extends
 *    spans whose end == insertion position.
 *
 * 2. **Pending formats (cursor toggle)** — when cursor is collapsed and user taps Bold,
 *    BOLD is added to pendingFormats. The NEXT typed character gets BOLD applied.
 *    Pending formats persist until the user moves the cursor or taps the button again.
 *
 * 3. **Disabled formats** — when cursor is inside a bold span and user taps Bold OFF,
 *    BOLD is added to disabledFormats. New text at that position won't inherit BOLD
 *    even though the surrounding span has it.
 *
 * 4. **Selection toggle** — selecting text and tapping Bold adds/removes BOLD across
 *    the entire selection range.
 *
 * 5. **Line-based formats** — bullet list, ordered list, blockquote are stored as text
 *    prefixes ("- ", "1. ", "> "). Toggling adds/removes the prefix.
 *
 * Platform-agnostic — shared by Jetpack Compose and Kotlin XML UI kits.
 */
class RichTextEditorController(
    val state: RichTextEditorState = RichTextEditorState()
) {

    interface Listener {
        fun onStateChanged()
    }

    private var listener: Listener? = null

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    private fun notifyChanged() {
        listener?.onStateChanged()
    }

    // ==================== Text Change Handling ====================

    /**
     * Called by the UI layer whenever the text field value changes.
     * Detects insertions/deletions via diff, adjusts spans, applies pending formats.
     */
    fun onTextChanged(newText: String, newSelectionStart: Int, newSelectionEnd: Int) {
        val oldText = state.text
        val manager = state.spanManager

        if (newText == oldText) {
            // Only selection changed — update selection and clear pending/disabled
            state.setSelection(newSelectionStart, newSelectionEnd)
            notifyChanged()
            return
        }

        // Diff: find common prefix and suffix
        val prefixLen = commonPrefixLength(oldText, newText)
        val suffixLen = commonSuffixLength(oldText, newText, prefixLen)

        val oldEnd = oldText.length - suffixLen
        val newEnd = newText.length - suffixLen

        // Save pending/disabled before any state mutation
        val hadPendingFormats = state.pendingFormats.toSet()
        val hadDisabledFormats = state.disabledFormats.toSet()

        // Process deletion
        if (oldEnd > prefixLen) {
            manager.onTextDeleted(prefixLen, oldEnd)
        }

        // Process insertion
        if (newEnd > prefixLen) {
            val insertLen = newEnd - prefixLen
            manager.onTextInserted(prefixLen, insertLen)

            // Determine which formats to apply to the inserted text.
            // Priority: pending formats > inherited from span (minus disabled)
            val formatsToApply = if (hadPendingFormats.isNotEmpty()) {
                hadPendingFormats
            } else {
                // After onTextInserted, the span that was at `prefixLen` has been extended
                // to include the new text. But if the user had disabled formats, we need
                // to remove those from the inherited set.
                // Check what formats the span at prefixLen has AFTER the insert.
                val inherited = manager.getFormatsAt(prefixLen)
                (inherited - hadDisabledFormats).filter { isInlineFormat(it) }.toSet()
            }

            if (formatsToApply.isNotEmpty()) {
                // The span manager already extended existing spans via onTextInserted.
                // If the inserted text is inside an existing span, it already has those formats.
                // We only need to ADD formats that come from pending (not already on the span).
                val existingFormats = manager.getFormatsAt(prefixLen)
                val newFormats = formatsToApply - existingFormats
                for (format in newFormats) {
                    manager.addFormat(prefixLen, prefixLen + insertLen, format)
                }

                // If disabled formats exist, REMOVE them from the inserted range
                // (the span was extended by onTextInserted, inheriting all formats,
                // but the user explicitly disabled some)
                for (format in hadDisabledFormats) {
                    if (format in existingFormats) {
                        manager.removeFormat(prefixLen, prefixLen + insertLen, format)
                    }
                }
            } else if (hadDisabledFormats.isNotEmpty()) {
                // No formats to apply, but some were disabled — remove them from inserted range
                val existingFormats = manager.getFormatsAt(prefixLen)
                for (format in hadDisabledFormats) {
                    if (format in existingFormats) {
                        manager.removeFormat(prefixLen, prefixLen + insertLen, format)
                    }
                }
            }
        }

        // Update state — use internal setter that does NOT clear pending/disabled
        state.setText(newText)
        state.setSelectionInternal(newSelectionStart, newSelectionEnd)

        // Clear pending formats after they've been consumed by an insertion
        if (newEnd > prefixLen && hadPendingFormats.isNotEmpty()) {
            state.pendingFormats.clear()
        }
        // Clear disabled formats after they've been consumed
        if (newEnd > prefixLen && hadDisabledFormats.isNotEmpty()) {
            state.disabledFormats.clear()
        }

        // Auto-continue list/blockquote when a newline is inserted
        if (newEnd > prefixLen) {
            val insertedText = newText.substring(prefixLen, newEnd)
            if (insertedText == "\n") {
                handleNewlineAutoContinuation(newSelectionStart)
            }
        }

        notifyChanged()
    }

    // ==================== List / Blockquote Auto-Continuation ====================

    /**
     * After a newline is inserted, checks if the previous line had a list or blockquote
     * prefix and auto-continues it on the new line.
     *
     * - Ordered list: "1. first\n" → "1. first\n2. "
     * - Bullet list: "- first\n" → "- first\n- "
     * - Blockquote: "> first\n" → "> first\n> "
     *
     * If the previous line was an empty prefix (e.g., "2. " with no content after it),
     * the prefix is removed instead (exit list/quote mode).
     */
    private fun handleNewlineAutoContinuation(cursorPos: Int) {
        val text = state.text
        if (cursorPos <= 0 || cursorPos > text.length) return

        // The newline is at cursorPos - 1
        if (text.getOrNull(cursorPos - 1) != '\n') return

        // Find the previous line
        val textBeforeNewline = text.substring(0, cursorPos - 1)
        val lastLineStart = textBeforeNewline.lastIndexOf('\n') + 1
        val previousLine = textBeforeNewline.substring(lastLineStart)

        // Check ordered list: "1. content"
        val orderedMatch = Regex("^(\\d+)\\. (.*)$").find(previousLine)
        if (orderedMatch != null) {
            val currentNumber = orderedMatch.groupValues[1].toIntOrNull() ?: 0
            val content = orderedMatch.groupValues[2]
            if (content.isNotBlank()) {
                // Add next numbered item
                val nextPrefix = "${currentNumber + 1}. "
                insertAutoContinuationPrefix(cursorPos, nextPrefix)
            } else {
                // Empty numbered line — remove prefix (exit list mode)
                removeEmptyLinePrefix(lastLineStart, cursorPos)
            }
            return
        }

        // Check bullet list: "- content" or "• content"
        val bulletMatch = Regex("^([•\\-]) (.*)$").find(previousLine)
        if (bulletMatch != null) {
            val marker = bulletMatch.groupValues[1]
            val content = bulletMatch.groupValues[2]
            if (content.isNotBlank()) {
                insertAutoContinuationPrefix(cursorPos, "$marker ")
            } else {
                removeEmptyLinePrefix(lastLineStart, cursorPos)
            }
            return
        }

        // Check blockquote: "> content"
        if (previousLine.startsWith("> ")) {
            val content = previousLine.substring(2)
            if (content.isNotBlank()) {
                insertAutoContinuationPrefix(cursorPos, "> ")
            } else {
                removeEmptyLinePrefix(lastLineStart, cursorPos)
            }
            return
        }
    }

    /**
     * Inserts a prefix (e.g., "2. ", "- ", "> ") at the cursor position after a newline.
     */
    private fun insertAutoContinuationPrefix(cursorPos: Int, prefix: String) {
        val text = state.text
        val newText = text.substring(0, cursorPos) + prefix + text.substring(cursorPos)
        // Adjust spans for the inserted prefix
        state.spanManager.onTextInserted(cursorPos, prefix.length)
        state.setText(newText)
        state.setSelectionInternal(cursorPos + prefix.length, cursorPos + prefix.length)
    }

    /**
     * Removes an empty prefix line (e.g., "2. " with no content) and the preceding newline.
     * This exits list/quote mode when the user presses Enter on an empty prefixed line.
     */
    private fun removeEmptyLinePrefix(lineStart: Int, cursorPos: Int) {
        val text = state.text
        val afterCursor = text.substring(cursorPos)

        // We need to remove: the empty prefix line content + the newline we just added.
        // The empty prefix line runs from lineStart to cursorPos-1 (the \n we just typed).
        // Also remove the \n before the empty prefix line if it exists.
        val removeFrom = if (lineStart > 0) lineStart - 1 else lineStart
        val removeTo = cursorPos

        val newText = text.substring(0, removeFrom) + afterCursor
        val deleteLen = removeTo - removeFrom
        if (deleteLen > 0) {
            state.spanManager.onTextDeleted(removeFrom, removeTo)
        }
        state.setText(newText)
        state.setSelectionInternal(
            removeFrom.coerceIn(0, newText.length),
            removeFrom.coerceIn(0, newText.length)
        )
    }

    // ==================== Format Toggle ====================

    /**
     * Toggles a format. Behavior depends on whether there's a selection or just a cursor.
     */
    fun toggleFormat(format: RichTextFormat) {
        // Check compatibility
        if (!FormatCompatibility.isCompatible(format, state.activeFormats - setOf(format))) {
            return
        }

        if (state.isCursorCollapsed) {
            toggleFormatAtCursor(format)
        } else {
            toggleFormatOnSelection(format)
        }
        notifyChanged()
    }

    private fun toggleFormatAtCursor(format: RichTextFormat) {
        if (!isInlineFormat(format)) {
            toggleLineFormat(format)
            return
        }

        // Check formats at cursor AND at cursor-1 to handle span boundary
        // (cursor at span.end is "just after" the span, but logically still "in" it)
        val formatsAtCursor = state.spanManager.getFormatsAt(state.selectionStart)
        val formatsBeforeCursor = if (state.selectionStart > 0) {
            state.spanManager.getFormatsAt(state.selectionStart - 1)
        } else {
            emptySet()
        }
        val currentSpanFormats = formatsAtCursor + formatsBeforeCursor

        val isActiveInSpan = format in currentSpanFormats
        val isPending = format in state.pendingFormats
        val isDisabled = format in state.disabledFormats

        if (isActiveInSpan && !isDisabled) {
            // Format is active from span — disable it so new text won't inherit
            state.pendingFormats.remove(format)
            state.disabledFormats.add(format)
        } else if (isPending) {
            // Format was pending — remove it (toggle off)
            state.pendingFormats.remove(format)
        } else {
            // Format is not active — add as pending
            state.disabledFormats.remove(format)
            state.pendingFormats.add(format)
        }
    }

    private fun toggleFormatOnSelection(format: RichTextFormat) {
        val start = state.selectionStart
        val end = state.selectionEnd

        if (!isInlineFormat(format)) {
            toggleLineFormat(format)
            return
        }

        val manager = state.spanManager
        val isActive = manager.getFormatsInRange(start, end).contains(format)

        if (isActive) {
            manager.removeFormat(start, end, format)
        } else {
            manager.addFormat(start, end, format)
        }
    }

    /**
     * Toggles a line-based format (bullet list, ordered list, blockquote).
     */
    private fun toggleLineFormat(format: RichTextFormat) {
        val text = state.text
        val selStart = state.selectionStart
        val selEnd = state.selectionEnd

        val lineStart = text.lastIndexOf('\n', (selStart - 1).coerceAtLeast(0)) + 1
        val lineEnd = text.indexOf('\n', selEnd).let { if (it == -1) text.length else it }
        val affectedText = text.substring(lineStart, lineEnd)
        val lines = affectedText.split('\n')

        val allHaveFormat = lines.all { line ->
            when (format) {
                RichTextFormat.BULLET_LIST -> line.startsWith("- ") || line.startsWith("• ")
                RichTextFormat.ORDERED_LIST -> line.matches(Regex("^\\d+\\. .*"))
                RichTextFormat.BLOCKQUOTE -> line.startsWith("> ")
                else -> false
            }
        }

        val newLines = if (allHaveFormat) {
            lines.map { line ->
                when (format) {
                    RichTextFormat.BULLET_LIST -> line.removePrefix("- ").removePrefix("• ")
                    RichTextFormat.ORDERED_LIST -> line.replace(Regex("^\\d+\\. "), "")
                    RichTextFormat.BLOCKQUOTE -> line.removePrefix("> ")
                    else -> line
                }
            }
        } else {
            lines.mapIndexed { index, line ->
                val cleanLine = line
                    .removePrefix("- ").removePrefix("• ")
                    .let { it.replace(Regex("^\\d+\\. "), "") }
                    .removePrefix("> ")
                when (format) {
                    RichTextFormat.BULLET_LIST -> "- $cleanLine"
                    RichTextFormat.ORDERED_LIST -> "${index + 1}. $cleanLine"
                    RichTextFormat.BLOCKQUOTE -> "> $cleanLine"
                    else -> cleanLine
                }
            }
        }

        val newAffectedText = newLines.joinToString("\n")
        val newText = text.substring(0, lineStart) + newAffectedText + text.substring(lineEnd)
        val lengthDiff = newAffectedText.length - affectedText.length

        state.setText(newText)
        // Both selStart and selEnd are within the affected line range,
        // so shift both by the full lengthDiff (prefix added or removed).
        state.setSelectionInternal(
            (selStart + lengthDiff).coerceIn(0, newText.length),
            (selEnd + lengthDiff).coerceIn(0, newText.length)
        )
    }

    // ==================== Link ====================

    fun applyLink(displayText: String, url: String) {
        val text = state.text
        val selStart = state.selectionStart
        val selEnd = state.selectionEnd

        if (selStart != selEnd) {
            val newText = text.substring(0, selStart) + displayText + text.substring(selEnd)
            state.spanManager.onTextDeleted(selStart, selEnd)
            state.spanManager.onTextInserted(selStart, displayText.length)
            state.spanManager.addFormat(selStart, selStart + displayText.length, RichTextFormat.LINK)
            state.setText(newText)
            state.setSelectionInternal(selStart + displayText.length, selStart + displayText.length)
        } else {
            val newText = text.substring(0, selStart) + displayText + text.substring(selStart)
            state.spanManager.onTextInserted(selStart, displayText.length)
            state.spanManager.addFormat(selStart, selStart + displayText.length, RichTextFormat.LINK)
            state.setText(newText)
            state.setSelectionInternal(selStart + displayText.length, selStart + displayText.length)
        }
        notifyChanged()
    }

    // ==================== Serialization ====================

    fun toMarkdown(): String = state.spanManager.toMarkdown(state.text)

    fun fromMarkdown(markdown: String) {
        val (plainText, spans) = state.spanManager.fromMarkdown(markdown)
        state.spanManager.clear()
        for (span in spans) {
            for (format in span.formats) {
                state.spanManager.addFormat(span.start, span.end, format)
            }
        }
        state.setText(plainText)
        state.setSelectionInternal(plainText.length, plainText.length)
        notifyChanged()
    }

    fun clear() {
        state.clear()
        notifyChanged()
    }

    // ==================== Helpers ====================

    private fun isInlineFormat(format: RichTextFormat): Boolean = when (format) {
        RichTextFormat.BOLD, RichTextFormat.ITALIC, RichTextFormat.UNDERLINE,
        RichTextFormat.STRIKETHROUGH, RichTextFormat.INLINE_CODE,
        RichTextFormat.CODE_BLOCK, RichTextFormat.LINK -> true
        RichTextFormat.BULLET_LIST, RichTextFormat.ORDERED_LIST,
        RichTextFormat.BLOCKQUOTE -> false
    }

    private fun commonPrefixLength(a: String, b: String): Int {
        val limit = minOf(a.length, b.length)
        for (i in 0 until limit) {
            if (a[i] != b[i]) return i
        }
        return limit
    }

    private fun commonSuffixLength(a: String, b: String, prefixLen: Int): Int {
        val maxSuffix = minOf(a.length - prefixLen, b.length - prefixLen)
        for (i in 0 until maxSuffix) {
            if (a[a.length - 1 - i] != b[b.length - 1 - i]) return i
        }
        return maxSuffix
    }
}
