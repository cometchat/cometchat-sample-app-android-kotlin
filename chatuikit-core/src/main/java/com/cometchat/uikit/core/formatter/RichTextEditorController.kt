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
    private var mentionSpanProvider: MentionSpanProvider? = null

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    /**
     * Sets the mention span provider that allows the controller to query and
     * manipulate mention spans when code formatting is applied or removed.
     */
    fun setMentionSpanProvider(provider: MentionSpanProvider?) {
        this.mentionSpanProvider = provider
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
                // Check if inline code was active at the newline position before splitting
                val hadInlineCode = RichTextFormat.INLINE_CODE in manager.getFormatsAt(prefixLen)
                // Inline code should NOT span across newlines — end the span at the newline
                manager.splitInlineCodeAtNewline(prefixLen)
                // Re-add inline code as pending so it continues on the new line
                if (hadInlineCode) {
                    state.pendingFormats.add(RichTextFormat.INLINE_CODE)
                }
                handleNewlineAutoContinuation(newSelectionStart)
            }
        }

        // Detect and convert markdown shortcuts (e.g., **bold**, _italic_, etc.)
        detectAndConvertInlineMarkdownShortcuts()
        detectAndConvertLinePrefixMarkdownShortcuts()
        detectAndConvertLinkMarkdownShortcut()

        // When text is fully cleared, reset all format state so toolbar deselects everything
        if (state.text.isEmpty()) {
            state.pendingFormats.clear()
            state.disabledFormats.clear()
            state.spanManager.clear()
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
     *
     * Expected behavior: "- hello\n- \n" → "- hello\n" with cursor on the new empty line.
     * The empty prefix ("- ") and the newline the user just typed are removed,
     * but the newline separating the content line from the (now empty) new line is kept.
     */
    private fun removeEmptyLinePrefix(lineStart: Int, cursorPos: Int) {
        val text = state.text
        val afterCursor = text.substring(cursorPos)

        // Remove from lineStart (start of the empty prefix line) to cursorPos (after the \n just typed).
        // Keep the \n before lineStart so the cursor lands on a new empty line.
        val removeFrom = lineStart
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

    /**
     * Handles multi-line paste into a line that has a list/blockquote prefix.
     * Adds the same prefix to each new line in the pasted text.
     *
     * For example, pasting "line1\nline2\nline3" into a blockquote line produces:
     * "> line1\n> line2\n> line3"
     */
    private fun handleMultiLinePasteContinuation(insertStart: Int, insertEnd: Int) {
        val text = state.text

        // Find the line that contains the insertion point
        val lineStart = text.lastIndexOf('\n', (insertStart - 1).coerceAtLeast(0)) + 1
        val lineText = text.substring(lineStart, text.indexOf('\n', lineStart).let { if (it < 0) text.length else it })

        // Determine the prefix of the current line
        val prefix = when {
            lineText.startsWith("- ") -> "- "
            lineText.startsWith("> ") -> "> "
            lineText.matches(Regex("^\\d+\\. .*")) -> null // Ordered list needs special handling
            else -> return // No prefix on current line, nothing to do
        }

        // For ordered lists, we need to increment the number for each line
        val isOrderedList = prefix == null && lineText.matches(Regex("^\\d+\\. .*"))
        val baseNumber = if (isOrderedList) {
            Regex("^(\\d+)\\. ").find(lineText)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        } else 0

        // Find newlines within the inserted range and add prefixes
        val insertedText = text.substring(insertStart, insertEnd)
        if (!insertedText.contains("\n")) return

        val sb = StringBuilder()
        val lines = insertedText.split("\n")
        var lineNumber = baseNumber + 1

        for ((i, line) in lines.withIndex()) {
            if (i > 0) {
                sb.append("\n")
                // Add prefix to each new line
                if (isOrderedList) {
                    sb.append("${lineNumber}. ")
                    lineNumber++
                } else if (prefix != null) {
                    sb.append(prefix)
                }
            }
            sb.append(line)
        }

        val newInsertedText = sb.toString()
        if (newInsertedText == insertedText) return // No change needed

        // Replace the inserted range with the prefixed version
        val newText = text.substring(0, insertStart) + newInsertedText + text.substring(insertEnd)
        val lengthDiff = newInsertedText.length - insertedText.length

        // Adjust spans for the extra characters
        if (lengthDiff > 0) {
            // We need to adjust spans for each prefix insertion point
            // Simple approach: delete the old range and re-insert with new length
            state.spanManager.onTextDeleted(insertStart, insertEnd)
            state.spanManager.onTextInserted(insertStart, newInsertedText.length)
        }

        state.setText(newText)
        val newCursorPos = (state.selectionStart + lengthDiff).coerceIn(0, newText.length)
        state.setSelectionInternal(newCursorPos, newCursorPos)
    }

    // ==================== Markdown Shortcut Detection ====================

    /**
     * Scans the full text for completed inline markdown patterns and converts the
     * FIRST match found. This is cursor-position-independent — it doesn't matter
     * where the user typed; if the text contains a valid pattern, it converts.
     *
     * Patterns detected (checked in order, first match wins):
     * - <u>text</u> → UNDERLINE
     * - **text** → BOLD (but NOT inside already-formatted spans)
     * - ~~text~~ → STRIKETHROUGH
     * - _text_ → ITALIC (but NOT __text__)
     * - `text` → INLINE_CODE (but NOT ```)
     *
     * Only converts text that is NOT already formatted with the target format
     * (prevents re-converting text that was already shortcut-converted).
     */
    private fun detectAndConvertInlineMarkdownShortcuts() {
        val text = state.text
        if (text.length < 3) return

        // Try each pattern — first match wins
        if (tryConvertPatternBold(text)) return
        if (tryConvertPatternItalic(text)) return
        if (tryConvertPatternStrikethrough(text)) return
        if (tryConvertPatternUnderline(text)) return
        if (tryConvertPatternInlineCode(text)) return
    }

    private fun tryConvertPatternBold(text: String): Boolean {
        // Match **text** but not ***
        val regex = Regex("\\*\\*(.+?)\\*\\*")
        val match = regex.find(text) ?: return false
        val content = match.groupValues[1]
        if (content.isEmpty()) return false
        // Don't convert if the content is already bold (avoid re-triggering)
        val contentStartInText = match.range.first + 2
        if (state.spanManager.getFormatsAt(contentStartInText).contains(RichTextFormat.BOLD)) return false

        applyInlineShortcut(
            match.range.first, contentStartInText,
            contentStartInText + content.length, match.range.last + 1,
            content, RichTextFormat.BOLD
        )
        return true
    }

    private fun tryConvertPatternItalic(text: String): Boolean {
        // Match _text_ but NOT __text__ (double underscore)
        val regex = Regex("(?<!_)_([^_]+)_(?!_)")
        val match = regex.find(text) ?: return false
        val content = match.groupValues[1]
        if (content.isEmpty()) return false
        val contentStartInText = match.range.first + 1
        if (state.spanManager.getFormatsAt(contentStartInText).contains(RichTextFormat.ITALIC)) return false

        applyInlineShortcut(
            match.range.first, contentStartInText,
            contentStartInText + content.length, match.range.last + 1,
            content, RichTextFormat.ITALIC
        )
        return true
    }

    private fun tryConvertPatternStrikethrough(text: String): Boolean {
        val regex = Regex("~~(.+?)~~")
        val match = regex.find(text) ?: return false
        val content = match.groupValues[1]
        if (content.isEmpty()) return false
        val contentStartInText = match.range.first + 2
        if (state.spanManager.getFormatsAt(contentStartInText).contains(RichTextFormat.STRIKETHROUGH)) return false

        applyInlineShortcut(
            match.range.first, contentStartInText,
            contentStartInText + content.length, match.range.last + 1,
            content, RichTextFormat.STRIKETHROUGH
        )
        return true
    }

    private fun tryConvertPatternUnderline(text: String): Boolean {
        val regex = Regex("<u>(.+?)</u>")
        val match = regex.find(text) ?: return false
        val content = match.groupValues[1]
        if (content.isEmpty()) return false
        val contentStartInText = match.range.first + 3 // "<u>" is 3 chars
        if (state.spanManager.getFormatsAt(contentStartInText).contains(RichTextFormat.UNDERLINE)) return false

        applyInlineShortcut(
            match.range.first, contentStartInText,
            contentStartInText + content.length, match.range.last + 1,
            content, RichTextFormat.UNDERLINE
        )
        return true
    }

    private fun tryConvertPatternInlineCode(text: String): Boolean {
        // Match `text` but NOT ``` (triple backtick)
        val regex = Regex("(?<!`)`([^`]+)`(?!`)")
        val match = regex.find(text) ?: return false
        val content = match.groupValues[1]
        if (content.isEmpty()) return false
        val openBacktick = match.range.first
        val contentStartInText = openBacktick + 1
        if (state.spanManager.getFormatsAt(contentStartInText).contains(RichTextFormat.INLINE_CODE)) return false

        applyInlineShortcut(
            openBacktick, contentStartInText,
            contentStartInText + content.length, match.range.last + 1,
            content, RichTextFormat.INLINE_CODE
        )
        return true
    }

    /**
     * Applies an inline markdown shortcut conversion for symmetric markers (same open/close length).
     * Removes markers, applies format to the content text.
     */
    private fun applyInlineShortcut(
        openMarkerStart: Int,
        contentStart: Int,
        contentEnd: Int,
        closeMarkerEnd: Int,
        content: String,
        format: RichTextFormat
    ) {
        val manager = state.spanManager

        // Build new text without markers
        val text = state.text
        val newText = text.substring(0, openMarkerStart) + content + text.substring(closeMarkerEnd)

        // Adjust spans: delete the entire marked range, then re-insert just the content
        manager.onTextDeleted(openMarkerStart, closeMarkerEnd)
        manager.onTextInserted(openMarkerStart, content.length)

        // Apply the format to the content range
        manager.addFormat(openMarkerStart, openMarkerStart + content.length, format)

        // Update state
        state.setText(newText)
        val newCursor = openMarkerStart + content.length
        state.setSelectionInternal(newCursor, newCursor)

        // Set pending format so toolbar shows active state and next typed char continues format
        state.pendingFormats.add(format)
    }

    /**
     * Detects line-prefix markdown shortcuts at the start of the current line.
     * Patterns detected:
     * - "- " at line start → BULLET_LIST
     * - "1. " at line start → ORDERED_LIST
     * - "> " at line start → BLOCKQUOTE
     *
     * Triggers when the space after the prefix is typed.
     */
    private fun detectAndConvertLinePrefixMarkdownShortcuts() {
        val text = state.text
        val cursor = state.selectionStart
        if (!state.isCursorCollapsed || cursor < 2) return

        // Find the start of the current line
        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val lineContent = text.substring(lineStart, cursor)

        // Check if the line ONLY contains the prefix (user just typed it)
        when {
            lineContent == "- " -> {
                // Already in the correct format — the prefix IS the format representation.
                // The existing toggleLineFormat logic uses "- " as the prefix for BULLET_LIST.
                // We don't need to remove it; just ensure activeFormats reflects it.
                // The activeFormats getter already checks for line prefixes via the span system,
                // but line formats are stored as text prefixes, not spans.
                // Nothing to do here — the auto-continuation system handles this.
            }
            lineContent == "> " -> {
                // Same as above — blockquote prefix is already the format representation.
            }
            lineContent.matches(Regex("^\\d+\\. $")) -> {
                // Same as above — ordered list prefix is already the format representation.
            }
        }
    }

    /**
     * Detects [text](url) link markdown shortcut ending at cursor.
     * Removes markers, inserts display text with LINK format and URL metadata.
     */
    private fun detectAndConvertLinkMarkdownShortcut() {
        val text = state.text
        val cursor = state.selectionStart
        if (!state.isCursorCollapsed || cursor < 5) return // minimum: [x](y) = 6 chars

        // Check if cursor is right after a closing )
        if (text[cursor - 1] != ')') return

        // Find the matching ( for the URL part
        val urlEnd = cursor - 1
        val urlOpenParen = text.lastIndexOf("](", urlEnd - 1)
        if (urlOpenParen < 0) return

        val urlStart = urlOpenParen + 2
        if (urlStart >= urlEnd) return // empty URL
        val url = text.substring(urlStart, urlEnd)
        if (url.isEmpty()) return

        // Find the opening [ for the display text
        val bracketClose = urlOpenParen
        // The [ should be before the ]( 
        val displayTextEnd = bracketClose
        val bracketOpen = text.lastIndexOf('[', displayTextEnd - 1)
        if (bracketOpen < 0) return

        val displayTextStart = bracketOpen + 1
        if (displayTextStart >= displayTextEnd) return // empty display text
        val displayText = text.substring(displayTextStart, displayTextEnd)
        if (displayText.isEmpty()) return

        // Ensure the [ is not preceded by another [ (avoid [[text]])
        // and that there are no unmatched brackets in between
        if (displayText.contains('[') || displayText.contains(']')) return
        if (url.contains('(') || url.contains(')')) return

        val manager = state.spanManager

        // Build new text: replace [text](url) with just text
        val fullMatchStart = bracketOpen
        val fullMatchEnd = cursor
        val newText = text.substring(0, fullMatchStart) + displayText + text.substring(fullMatchEnd)

        // Adjust spans
        manager.onTextDeleted(fullMatchStart, fullMatchEnd)
        manager.onTextInserted(fullMatchStart, displayText.length)

        // Apply LINK format and store URL
        val linkStart = fullMatchStart
        val linkEnd = fullMatchStart + displayText.length
        manager.addFormat(linkStart, linkEnd, RichTextFormat.LINK)
        manager.setLinkUrl(linkStart, url)

        // Update state
        state.setText(newText)
        state.setSelectionInternal(linkEnd, linkEnd)
    }

    /**
     * Detects ``` at line start for code block shortcut.
     * Returns true if the pattern was detected (caller should handle code block insertion).
     * This is called by SegmentComposerController, not directly by onTextChanged.
     */
    fun detectTripleBacktickShortcut(): Boolean {
        val text = state.text
        val cursor = state.selectionStart
        if (!state.isCursorCollapsed || cursor < 3) return false

        // Find the start of the current line
        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val lineContent = text.substring(lineStart, cursor)

        // Check if the line contains exactly ```
        if (lineContent == "```") {
            // Remove the ``` from the text
            val newText = text.substring(0, lineStart) + text.substring(cursor)
            val manager = state.spanManager
            manager.onTextDeleted(lineStart, cursor)
            state.setText(newText)
            state.setSelectionInternal(lineStart.coerceAtMost(newText.length), lineStart.coerceAtMost(newText.length))
            return true
        }
        return false
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

        // Links are immune to inline formatting — reject if cursor is inside a LINK span
        if (format != RichTextFormat.LINK) {
            val linkSpan = state.spanManager.findLinkSpanAt(state.selectionStart)
                ?: if (state.selectionStart > 0) state.spanManager.findLinkSpanAt(state.selectionStart - 1) else null
            if (linkSpan != null) return
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

        // Links are immune to inline formatting — if the entire selection is within
        // a single LINK span, reject the format toggle
        if (format != RichTextFormat.LINK) {
            val linkSpan = state.spanManager.findLinkSpanAt(start)
            if (linkSpan != null && linkSpan.start <= start && linkSpan.end >= end) return
        }

        val manager = state.spanManager
        val isActive = manager.getFormatsInRange(start, end).contains(format)

        if (isActive) {
            manager.removeFormat(start, end, format)
            // When removing INLINE_CODE, restore any consumed mentions in the range
            if (format == RichTextFormat.INLINE_CODE) {
                restoreMentionsInRange(start, end)
            }
        } else {
            // When applying INLINE_CODE, convert any mentions in the range to plain text
            if (format == RichTextFormat.INLINE_CODE) {
                consumeMentionsInRange(start, end)
            }
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
            // Determine starting number for ordered list by counting preceding numbered lines
            val startNumber = if (format == RichTextFormat.ORDERED_LIST && lineStart > 0) {
                val textBefore = text.substring(0, lineStart)
                val precedingLines = textBefore.split('\n')
                val lastNumberedLine = precedingLines.lastOrNull { it.matches(Regex("^\\d+\\. .*")) }
                val lastNumber = lastNumberedLine?.let { Regex("^(\\d+)\\. ").find(it)?.groupValues?.get(1)?.toIntOrNull() } ?: 0
                lastNumber + 1
            } else {
                1
            }
            lines.mapIndexed { index, line ->
                val cleanLine = line
                    .removePrefix("- ").removePrefix("• ")
                    .let { it.replace(Regex("^\\d+\\. "), "") }
                    .removePrefix("> ")
                when (format) {
                    RichTextFormat.BULLET_LIST -> "- $cleanLine"
                    RichTextFormat.ORDERED_LIST -> "${startNumber + index}. $cleanLine"
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

    // ==================== Mention Consumption ====================

    /**
     * Converts all mention spans in the given range to plain text and stores
     * their data as [ConsumedMentionSpan] entries for later restoration.
     *
     * Called when INLINE_CODE or CODE_BLOCK formatting is applied to a range
     * that may contain mentions. For BOLD, ITALIC, UNDERLINE, STRIKETHROUGH,
     * BLOCKQUOTE, and list formats, mentions are preserved intact.
     *
     * @param start Inclusive start of the range
     * @param end Exclusive end of the range
     */
    fun consumeMentionsInRange(start: Int, end: Int) {
        val provider = mentionSpanProvider ?: return
        val mentions = provider.getMentionsInRange(start, end)
        if (mentions.isEmpty()) return

        for (mention in mentions) {
            // Create a ConsumedMentionSpan storing the original mention data
            val consumed = ConsumedMentionSpan(
                id = mention.id,
                text = mention.displayText,
                suggestionItem = mention.suggestionItem,
                textAppearance = mention.textAppearance
            )
            // Store keyed by start position so we can find it later for restoration
            state.consumedMentionSpans[mention.start] = consumed

            // Remove the mention span (converts to plain text visually)
            provider.removeMentionSpan(mention.start, mention.end)
        }
    }

    // ==================== Mention Restoration ====================

    /**
     * Restores all consumed mention spans in the given range back to their
     * original mention spans (NonEditableSpan) using the data stored in
     * [ConsumedMentionSpan] entries.
     *
     * Called when INLINE_CODE or CODE_BLOCK formatting is removed from a range
     * that previously had mentions converted to plain text.
     *
     * @param start Inclusive start of the range
     * @param end Exclusive end of the range
     */
    fun restoreMentionsInRange(start: Int, end: Int) {
        val provider = mentionSpanProvider ?: return
        val consumedEntries = state.consumedMentionSpans.entries
            .filter { (pos, _) -> pos in start until end }
            .toList()
        if (consumedEntries.isEmpty()) return

        for ((pos, consumed) in consumedEntries) {
            if (!consumed.canRestore()) continue
            val mentionEnd = pos + (consumed.text?.length ?: 0)
            provider.restoreMentionSpan(pos, mentionEnd, consumed)
            state.consumedMentionSpans.remove(pos)
        }
    }

    // ==================== Link ====================

    fun applyLink(displayText: String, url: String) {
        val text = state.text
        val selStart = state.selectionStart
        val selEnd = state.selectionEnd

        val linkStart = selStart
        val linkEnd = selStart + displayText.length

        if (selStart != selEnd) {
            val newText = text.substring(0, selStart) + displayText + text.substring(selEnd)
            state.spanManager.onTextDeleted(selStart, selEnd)
            state.spanManager.onTextInserted(selStart, displayText.length)
            state.spanManager.addFormat(linkStart, linkEnd, RichTextFormat.LINK)
            state.spanManager.setLinkUrl(linkStart, url)
            state.setText(newText)
            state.setSelectionInternal(linkEnd, linkEnd)
        } else {
            val newText = text.substring(0, selStart) + displayText + text.substring(selStart)
            state.spanManager.onTextInserted(selStart, displayText.length)
            state.spanManager.addFormat(linkStart, linkEnd, RichTextFormat.LINK)
            state.spanManager.setLinkUrl(linkStart, url)
            state.setText(newText)
            state.setSelectionInternal(linkEnd, linkEnd)
        }

        // Links are immune to inline formatting — strip any inherited inline formats
        // from the link range (bold, italic, underline, strikethrough, inline code
        // may have been inherited from surrounding spans via onTextInserted)
        val inlineFormatsToStrip = setOf(
            RichTextFormat.BOLD, RichTextFormat.ITALIC, RichTextFormat.UNDERLINE,
            RichTextFormat.STRIKETHROUGH, RichTextFormat.INLINE_CODE
        )
        for (fmt in inlineFormatsToStrip) {
            state.spanManager.removeFormat(linkStart, linkEnd, fmt)
        }

        // Clear any pending formats so they don't bleed into text typed after the link
        state.pendingFormats.clear()
        state.disabledFormats.clear()

        notifyChanged()
    }

    /**
     * Removes the LINK format from the span at [spanStart]..[spanEnd] but keeps the text.
     */
    fun removeLink(spanStart: Int, spanEnd: Int) {
        state.spanManager.removeFormat(spanStart, spanEnd, RichTextFormat.LINK)
        state.spanManager.removeLinkUrl(spanStart)
        notifyChanged()
    }

    /**
     * Replaces an existing link span at [oldStart]..[oldEnd] with new display text and URL.
     * Removes the old link, replaces the text, and applies a new link span.
     */
    fun editLink(oldStart: Int, oldEnd: Int, newDisplayText: String, newUrl: String) {
        val text = state.text

        // Remove old link format and URL
        state.spanManager.removeFormat(oldStart, oldEnd, RichTextFormat.LINK)
        state.spanManager.removeLinkUrl(oldStart)

        // Replace the text in the range
        val newText = text.substring(0, oldStart) + newDisplayText + text.substring(oldEnd)
        state.spanManager.onTextDeleted(oldStart, oldEnd)
        state.spanManager.onTextInserted(oldStart, newDisplayText.length)

        // Apply new link format and URL
        val linkEnd = oldStart + newDisplayText.length
        state.spanManager.addFormat(oldStart, linkEnd, RichTextFormat.LINK)
        state.spanManager.setLinkUrl(oldStart, newUrl)

        // Strip inherited inline formats from the link range
        val inlineFormatsToStrip = setOf(
            RichTextFormat.BOLD, RichTextFormat.ITALIC, RichTextFormat.UNDERLINE,
            RichTextFormat.STRIKETHROUGH, RichTextFormat.INLINE_CODE
        )
        for (fmt in inlineFormatsToStrip) {
            state.spanManager.removeFormat(oldStart, linkEnd, fmt)
        }

        state.setText(newText)
        state.setSelectionInternal(linkEnd, linkEnd)
        notifyChanged()
    }

    // ==================== Serialization ====================

    fun toMarkdown(): String = state.spanManager.toMarkdown(state.text)

    fun fromMarkdown(markdown: String) {
        val (plainText, spans) = state.spanManager.fromMarkdown(markdown)
        // Save link URLs that were parsed during fromMarkdown before clearing
        val parsedLinkUrls = state.spanManager.linkUrlMap.toMap()
        state.spanManager.clear()
        for (span in spans) {
            for (format in span.formats) {
                state.spanManager.addFormat(span.start, span.end, format)
            }
        }
        // Restore link URLs after re-adding formats
        for ((start, url) in parsedLinkUrls) {
            state.spanManager.setLinkUrl(start, url)
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
