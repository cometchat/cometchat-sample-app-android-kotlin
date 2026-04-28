package com.cometchat.uikit.core.formatter

/**
 * Manages an ordered list of [ComposerSegment]s (normal rich text + code blocks).
 * Mirrors the Flutter SegmentComposerController architecture.
 *
 * Normal segments use [RichTextEditorController] for WYSIWYG formatting.
 * Code segments hold plain monospace text with no formatting.
 *
 * Platform-agnostic — shared by Jetpack Compose and Kotlin XML UI kits.
 */
class SegmentComposerController {

    interface Listener {
        fun onSegmentsChanged()
    }

    private var listener: Listener? = null
    private var nextId = 0
    private val _segments = mutableListOf<ComposerSegment>()

    /** Read-only view of current segments. */
    val segments: List<ComposerSegment> get() = _segments.toList()

    /** ID of the currently focused segment (managed by UI layer). */
    var focusedSegmentId: String? = null
        private set

    /**
     * ID of the segment that should receive focus after the next rebuild.
     * Used for deferred focus (e.g., after code block exit when the target
     * normal segment might be hidden and needs a rebuild to become visible).
     */
    var pendingFocusSegmentId: String? = null
        private set

    init {
        _segments.add(ComposerSegment.Normal(id = genId()))
    }

    fun setListener(listener: Listener?) { this.listener = listener }
    private fun genId(): String = "seg${nextId++}"
    private fun notifyChanged() { listener?.onSegmentsChanged() }

    // ==================== Focus ====================

    fun setFocusedSegment(segmentId: String) { focusedSegmentId = segmentId }

    /**
     * Requests focus on a segment by setting both [focusedSegmentId] and [pendingFocusSegmentId].
     * The UI layer consumes [pendingFocusSegmentId] to actually move keyboard focus.
     */
    fun focusSegment(segmentId: String) {
        focusedSegmentId = segmentId
        pendingFocusSegmentId = segmentId
        notifyChanged()
    }

    /** Clears the pending focus after the UI has consumed it. */
    fun consumePendingFocus(): String? {
        val id = pendingFocusSegmentId
        pendingFocusSegmentId = null
        return id
    }

    val focusedSegment: ComposerSegment?
        get() = _segments.find { it.id == focusedSegmentId }

    val isTypingInCode: Boolean
        get() = focusedSegment is ComposerSegment.Code

    // ==================== Code Block Operations ====================

    /**
     * Checks if the focused Normal segment has a ``` pattern at line start.
     * If detected, removes the backticks and inserts a Code segment.
     * Should be called after text changes in a Normal segment.
     * Returns true if a code block was inserted.
     */
    fun detectAndInsertCodeBlockShortcut(): Boolean {
        val focused = focusedSegment as? ComposerSegment.Normal ?: return false
        val controller = focused.controller
        if (controller.detectTripleBacktickShortcut()) {
            // The backticks have been removed from the text by the controller.
            // Now insert a code block.
            insertCodeBlock()
            return true
        }
        return false
    }

    /**
     * Detects pasted fenced code blocks (```...```) in the focused Normal segment.
     * If found, strips the backticks and places the code content into a Code segment.
     * Returns true if a code block was detected and converted.
     *
     * Must be called BEFORE detectAndInsertCodeBlockShortcut() so that pasting
     * ```code``` is handled as a full code block, not as a ``` shortcut.
     */
    fun detectAndConvertPastedCodeBlocks(): Boolean {
        val focused = focusedSegment as? ComposerSegment.Normal ?: return false
        val idx = _segments.indexOf(focused)
        if (idx < 0) return false

        val fullText = focused.controller.state.text
        if (!fullText.contains("```")) return false

        // Check if text contains both opening and closing ``` 
        // Find first ``` and last ``` — they must be different positions
        val firstBacktick = fullText.indexOf("```")
        val lastBacktick = fullText.lastIndexOf("```")
        if (firstBacktick == lastBacktick) return false // only one set of ```, not a complete fence
        if (lastBacktick < firstBacktick + 3) return false // too close together

        // Extract code content between the fences
        val afterOpening = firstBacktick + 3
        // Skip optional language identifier and newline after opening ```
        var contentStart = afterOpening
        // Skip word chars (language) then optional newline
        while (contentStart < lastBacktick && fullText[contentStart].isLetterOrDigit()) contentStart++
        if (contentStart < lastBacktick && fullText[contentStart] == '\n') contentStart++

        val codeContent = fullText.substring(contentStart, lastBacktick).trimEnd('\n')

        // Check if there's text before or after the fences
        val textBefore = fullText.substring(0, firstBacktick).trim()
        val textAfter = fullText.substring(lastBacktick + 3).trim()

        // Build new segment list
        val newSegments = mutableListOf<ComposerSegment>()
        for (i in 0 until idx) newSegments.add(_segments[i])

        // Normal segment before (with text if any, empty otherwise)
        val beforeNormal = ComposerSegment.Normal(id = genId())
        if (textBefore.isNotEmpty()) {
            beforeNormal.controller.onTextChanged(textBefore, textBefore.length, textBefore.length)
        }
        newSegments.add(beforeNormal)

        // Code segment with the extracted content
        val codeSegment = ComposerSegment.Code(id = genId(), text = codeContent)
        newSegments.add(codeSegment)

        // Normal segment after
        val afterNormal = ComposerSegment.Normal(id = genId())
        if (textAfter.isNotEmpty()) {
            afterNormal.controller.onTextChanged(textAfter, textAfter.length, textAfter.length)
        }
        newSegments.add(afterNormal)

        for (i in (idx + 1) until _segments.size) newSegments.add(_segments[i])

        _segments.clear()
        _segments.addAll(newSegments)

        // Focus the code segment so the user sees their pasted code
        focusedSegmentId = codeSegment.id
        pendingFocusSegmentId = codeSegment.id
        notifyChanged()
        return true
    }

    fun toggleCodeBlock() {
        when (val focused = focusedSegment) {
            is ComposerSegment.Code -> removeCodeSegment(focused)
            is ComposerSegment.Normal -> insertCodeBlock()
            null -> insertCodeBlock()
        }
    }

    /**
     * Converts ALL text from the focused Normal segment into a code block.
     * Strips line format prefixes ("> ", "- ", "N. ") before inserting.
     * The Normal segment is cleared completely — no text remains in it.
     */
    fun convertAllToCodeBlock() {
        val focusedIdx = _segments.indexOfFirst { it.id == focusedSegmentId }
        val idx = if (focusedIdx >= 0) focusedIdx else _segments.size - 1
        val current = _segments.getOrNull(idx) as? ComposerSegment.Normal ?: return

        val fullText = current.controller.state.text
        android.util.Log.d("SegmentDebug", "convertAllToCodeBlock: focusedIdx=$focusedIdx, idx=$idx, segmentCount=${_segments.size}")
        android.util.Log.d("SegmentDebug", "convertAllToCodeBlock: fullText='${fullText.take(100)}', length=${fullText.length}")
        android.util.Log.d("SegmentDebug", "convertAllToCodeBlock: segments=${_segments.map { when(it) { is ComposerSegment.Normal -> "Normal(${it.id}, text='${it.controller.state.text.take(30)}')" ; is ComposerSegment.Code -> "Code(${it.id}, text='${it.text.take(30)}')" } }}")
        
        if (fullText.isEmpty()) {
            android.util.Log.d("SegmentDebug", "convertAllToCodeBlock: fullText is empty, falling back to insertCodeBlock")
            insertCodeBlock()
            return
        }

        // Strip line format prefixes
        val stripped = stripLineFormatPrefixes(fullText)

        // Build new segment list with FRESH Normal segments (not reusing old ones
        // to avoid BasicTextField tfv state holding stale text)
        val newSegments = mutableListOf<ComposerSegment>()
        for (i in 0 until idx) newSegments.add(_segments[i])

        // Fresh empty Normal before code (new ID forces new remember key)
        val beforeNormal = ComposerSegment.Normal(id = genId())
        newSegments.add(beforeNormal)

        val codeSegment = ComposerSegment.Code(id = genId(), text = stripped)
        newSegments.add(codeSegment)

        val afterSegment = ComposerSegment.Normal(id = genId())
        newSegments.add(afterSegment)

        for (i in (idx + 1) until _segments.size) newSegments.add(_segments[i])

        _segments.clear()
        _segments.addAll(newSegments)
        focusedSegmentId = codeSegment.id
        pendingFocusSegmentId = codeSegment.id
        notifyChanged()
    }

    /**
     * Converts only the cursor paragraph from a Normal segment into a code block.
     * Strips line format prefixes from the extracted paragraph.
     * Remaining text stays in the Normal segment(s).
     */
    fun convertCursorParagraphToCodeBlock() {
        val focused = focusedSegment as? ComposerSegment.Normal ?: return
        val idx = _segments.indexOf(focused)
        if (idx < 0) return

        val fullText = focused.controller.state.text
        val cursorPos = focused.controller.state.selectionStart

        android.util.Log.d("SegmentDebug", "convertCursorParagraph: cursorPos=$cursorPos, textLen=${fullText.length}")

        // Check if text has any line format prefixes (blockquote/list)
        val hasLineFormats = fullText.lines().any { line ->
            line.startsWith("> ") || line.startsWith("- ") || line.startsWith("• ") ||
                line.matches(Regex("^\\d+\\. .*"))
        }

        // If no line formats or single paragraph, convert ALL text to code block
        if (!hasLineFormats || !fullText.contains('\n')) {
            android.util.Log.d("SegmentDebug", "convertCursorParagraph: no line formats or single para, converting all")
            convertAllToCodeBlock()
            return
        }

        // Has line formats + multiple paragraphs → only convert cursor paragraph
        val safeCursor = cursorPos.coerceIn(0, fullText.length)
        val paraStart = if (safeCursor > 0) {
            val nl = fullText.lastIndexOf('\n', (safeCursor - 1).coerceAtLeast(0))
            if (nl < 0) 0 else nl + 1
        } else 0
        val paraEnd = fullText.indexOf('\n', safeCursor).let { if (it < 0) fullText.length else it }

        var textBefore = if (paraStart > 0) fullText.substring(0, paraStart - 1) else ""
        val paragraph = fullText.substring(paraStart, paraEnd)
        val textAfter = if (paraEnd < fullText.length) fullText.substring(paraEnd + 1) else ""

        // Trim trailing empty format-only lines from textBefore
        // (e.g., a lone "> " or "- " line between paragraphs)
        while (textBefore.isNotEmpty()) {
            val lastLineStart = textBefore.lastIndexOf('\n') + 1
            val lastLine = textBefore.substring(lastLineStart)
            val strippedLine = lastLine.removePrefix("> ").removePrefix("- ").removePrefix("• ")
                .let { it.replace(Regex("^\\d+\\. "), "") }
            if (strippedLine.isBlank()) {
                textBefore = if (lastLineStart > 0) textBefore.substring(0, lastLineStart - 1) else ""
            } else {
                break
            }
        }

        val strippedParagraph = stripLineFormatPrefixes(paragraph)

        android.util.Log.d("SegmentDebug", "convertCursorParagraph: before='${textBefore.take(30)}', para='${strippedParagraph.take(30)}', after='${textAfter.take(30)}'")

        val newSegments = mutableListOf<ComposerSegment>()
        for (i in 0 until idx) newSegments.add(_segments[i])

        if (textBefore.isNotEmpty()) {
            val beforeNormal = ComposerSegment.Normal(id = genId())
            beforeNormal.controller.onTextChanged(textBefore, textBefore.length, textBefore.length)
            newSegments.add(beforeNormal)
        } else {
            newSegments.add(ComposerSegment.Normal(id = genId()))
        }

        val codeSegment = ComposerSegment.Code(id = genId(), text = strippedParagraph)
        newSegments.add(codeSegment)

        if (textAfter.isNotEmpty()) {
            val afterNormal = ComposerSegment.Normal(id = genId())
            afterNormal.controller.onTextChanged(textAfter, textAfter.length, textAfter.length)
            newSegments.add(afterNormal)
        } else {
            newSegments.add(ComposerSegment.Normal(id = genId()))
        }

        for (i in (idx + 1) until _segments.size) newSegments.add(_segments[i])

        _segments.clear()
        _segments.addAll(newSegments)
        focusedSegmentId = codeSegment.id
        pendingFocusSegmentId = codeSegment.id
        notifyChanged()
    }

    /**
     * Extracts the paragraph at the cursor position from a Code segment.
     * The extracted paragraph becomes a Normal segment, optionally with a line format applied.
     * Remaining text stays in code block(s).
     *
     * @param cursorPosition The cursor position within the code block text
     * @param applyFormat Optional line format to apply to the extracted paragraph (null = plain text)
     */
    fun extractParagraphFromCodeBlock(cursorPosition: Int, applyFormat: RichTextFormat? = null) {
        val focused = focusedSegment as? ComposerSegment.Code ?: return
        val idx = _segments.indexOf(focused)
        if (idx < 0) return

        val fullText = focused.text
        android.util.Log.d("SegmentDebug", "extractParagraph: cursorPos=$cursorPosition, textLen=${fullText.length}, applyFormat=$applyFormat")

        // Find the paragraph boundaries at cursor position
        val safeCursor = cursorPosition.coerceIn(0, fullText.length)
        val paraStart = if (safeCursor > 0) {
            val nl = fullText.lastIndexOf('\n', (safeCursor - 1).coerceAtLeast(0))
            if (nl < 0) 0 else nl + 1
        } else 0
        val paraEnd = fullText.indexOf('\n', safeCursor).let { if (it < 0) fullText.length else it }

        val textBefore = if (paraStart > 0) fullText.substring(0, paraStart - 1) else ""
        val paragraph = fullText.substring(paraStart, paraEnd)
        val textAfter = if (paraEnd < fullText.length) fullText.substring(paraEnd + 1) else ""

        android.util.Log.d("SegmentDebug", "extractParagraph: before='${textBefore.take(30)}', para='${paragraph.take(30)}', after='${textAfter.take(30)}'")

        // Apply line format prefix if requested
        val formattedParagraph = when (applyFormat) {
            RichTextFormat.BLOCKQUOTE -> "> $paragraph"
            RichTextFormat.BULLET_LIST -> "- $paragraph"
            RichTextFormat.ORDERED_LIST -> "1. $paragraph"
            else -> paragraph
        }

        // Build new segment list
        val newSegments = mutableListOf<ComposerSegment>()
        for (i in 0 until idx) newSegments.add(_segments[i])

        // Code block for text before cursor paragraph (if any)
        if (textBefore.isNotEmpty()) {
            // Ensure there's a Normal before the code block (invariant)
            if (newSegments.isEmpty() || newSegments.last() !is ComposerSegment.Normal) {
                newSegments.add(ComposerSegment.Normal(id = genId()))
            }
            newSegments.add(ComposerSegment.Code(id = genId(), text = textBefore))
        }

        // Normal segment for the extracted paragraph
        val extractedNormal = ComposerSegment.Normal(id = genId())
        if (formattedParagraph.isNotEmpty()) {
            extractedNormal.controller.onTextChanged(formattedParagraph, formattedParagraph.length, formattedParagraph.length)
        }
        newSegments.add(extractedNormal)

        // Code block for text after cursor paragraph (if any)
        if (textAfter.isNotEmpty()) {
            newSegments.add(ComposerSegment.Code(id = genId(), text = textAfter))
            newSegments.add(ComposerSegment.Normal(id = genId()))
        }

        // Add remaining segments after the original code block
        for (i in (idx + 1) until _segments.size) newSegments.add(_segments[i])

        android.util.Log.d("SegmentDebug", "extractParagraph: result=${newSegments.size} segments")

        _segments.clear()
        _segments.addAll(newSegments)
        focusedSegmentId = extractedNormal.id
        pendingFocusSegmentId = extractedNormal.id
        // Merge any adjacent Normal segments that resulted from the extraction
        mergeAdjacentNormals()
        notifyChanged()
    }

    /**
     * Inserts a code block at the focused normal segment.
     *
     * Three scenarios per the spec:
     * A) Selected text → selected text becomes code content
     * B) No selection, cursor on a line → entire line extracted into code
     * C) Empty segment → empty code block inserted
     *
     * Invariant: there is ALWAYS a normal segment before and after every code segment.
     */
    fun insertCodeBlock() {
        val focusedIdx = _segments.indexOfFirst { it.id == focusedSegmentId }
        val idx = if (focusedIdx >= 0) focusedIdx else _segments.size - 1
        val current = _segments.getOrNull(idx)

        var textBefore = ""
        var codeContent = ""
        var textAfter = ""

        if (current is ComposerSegment.Normal) {
            val state = current.controller.state
            val fullText = state.text
            val selStart = state.selectionStart
            val selEnd = state.selectionEnd

            if (selStart != selEnd) {
                // Scenario A: selection → selected text becomes code
                textBefore = fullText.substring(0, selStart)
                codeContent = fullText.substring(selStart, selEnd)
                textAfter = fullText.substring(selEnd)
            } else if (fullText.isNotEmpty() && selStart >= fullText.length) {
                // Scenario B1: cursor at end of text → keep all text, empty code block below
                textBefore = fullText
                codeContent = ""
                textAfter = ""
            } else if (fullText.isNotEmpty()) {
                // Scenario B2: cursor in middle → extract current line into code
                val lineStart = fullText.lastIndexOf('\n', (selStart - 1).coerceAtLeast(0)).let {
                    if (it < 0) 0 else it + 1
                }
                val lineEnd = fullText.indexOf('\n', selStart).let {
                    if (it < 0) fullText.length else it
                }
                codeContent = fullText.substring(lineStart, lineEnd)
                textBefore = if (lineStart > 0) fullText.substring(0, lineStart - 1) else ""
                textAfter = if (lineEnd < fullText.length) fullText.substring(lineEnd + 1) else ""
            }
            // Scenario C: empty text → codeContent stays ""

            // Strip line format prefixes ("> ", "- ", "N. ") from code content
            // since code blocks don't use line formatting
            codeContent = stripLineFormatPrefixes(codeContent)
            textBefore = stripLineFormatPrefixes(textBefore)
            textAfter = stripLineFormatPrefixes(textAfter)
        }

        val newSegments = mutableListOf<ComposerSegment>()
        for (i in 0 until idx) newSegments.add(_segments[i])

        // Before-text normal segment (reuse existing)
        if (current is ComposerSegment.Normal) {
            current.controller.clear()
            if (textBefore.isNotEmpty()) {
                current.controller.onTextChanged(textBefore, textBefore.length, textBefore.length)
            }
            newSegments.add(current)
        }

        val codeSegment = ComposerSegment.Code(id = genId(), text = codeContent)
        newSegments.add(codeSegment)

        val afterSegment = ComposerSegment.Normal(id = genId())
        if (textAfter.isNotEmpty()) {
            afterSegment.controller.onTextChanged(textAfter, textAfter.length, textAfter.length)
        }
        newSegments.add(afterSegment)

        for (i in (idx + 1) until _segments.size) newSegments.add(_segments[i])

        _segments.clear()
        _segments.addAll(newSegments)
        focusedSegmentId = codeSegment.id
        pendingFocusSegmentId = codeSegment.id
        notifyChanged()
    }

    /**
     * Removes a code segment and merges surrounding normal segments.
     * Separator logic: newlines only between non-empty parts.
     * Cursor at: prevText.length + sep1.length + codeText.length
     *
     * Also restores any consumed mention spans that were stored when the
     * code block was originally created.
     */
    fun removeCodeSegment(segment: ComposerSegment.Code) {
        val idx = _segments.indexOf(segment)
        if (idx < 0) return

        val prevNormal = _segments.getOrNull(idx - 1) as? ComposerSegment.Normal
        val nextNormal = _segments.getOrNull(idx + 1) as? ComposerSegment.Normal
        val codeText = segment.text
        val prevText = prevNormal?.controller?.state?.text ?: ""
        val nextText = nextNormal?.controller?.state?.text ?: ""

        val sep1 = if (prevText.isNotEmpty() && codeText.isNotEmpty()) "\n" else ""
        val sep2 = if ((prevText.isNotEmpty() || codeText.isNotEmpty()) && nextText.isNotEmpty()) "\n" else ""
        val merged = "$prevText$sep1$codeText$sep2$nextText"
        val cursorPos = prevText.length + sep1.length + codeText.length

        // Collect consumed mention spans from the previous normal segment
        // and adjust their positions for the merged text offset
        val consumedMentions = mutableMapOf<Int, ConsumedMentionSpan>()
        if (prevNormal != null) {
            val offset = prevText.length + sep1.length
            for ((pos, consumed) in prevNormal.controller.state.consumedMentionSpans) {
                // Mentions that were in the code range: adjust by the merge offset
                consumedMentions[pos + offset] = consumed
            }
        }
        // Also collect from the next normal segment (adjust by prevText + sep1 + codeText + sep2)
        if (nextNormal != null) {
            val offset = prevText.length + sep1.length + codeText.length + sep2.length
            for ((pos, consumed) in nextNormal.controller.state.consumedMentionSpans) {
                consumedMentions[pos + offset] = consumed
            }
        }

        if (nextNormal != null) _segments.remove(nextNormal)
        _segments.remove(segment)

        if (prevNormal != null) {
            prevNormal.controller.clear()
            if (merged.isNotEmpty()) {
                prevNormal.controller.onTextChanged(merged, cursorPos, cursorPos)
            }
            // Transfer adjusted consumed mentions to the merged normal segment
            // and restore them via the provider
            if (consumedMentions.isNotEmpty()) {
                prevNormal.controller.state.consumedMentionSpans.putAll(consumedMentions)
                prevNormal.controller.restoreMentionsInRange(0, merged.length)
            }
            focusedSegmentId = prevNormal.id
            pendingFocusSegmentId = prevNormal.id
        } else {
            val newNormal = ComposerSegment.Normal(id = genId())
            if (merged.isNotEmpty()) {
                newNormal.controller.onTextChanged(merged, cursorPos, cursorPos)
            }
            // Transfer adjusted consumed mentions and restore
            if (consumedMentions.isNotEmpty()) {
                newNormal.controller.state.consumedMentionSpans.putAll(consumedMentions)
                newNormal.controller.restoreMentionsInRange(0, merged.length)
            }
            _segments.add(0, newNormal)
            focusedSegmentId = newNormal.id
            pendingFocusSegmentId = newNormal.id
        }

        if (_segments.isEmpty()) {
            val fresh = ComposerSegment.Normal(id = genId())
            _segments.add(fresh)
            focusedSegmentId = fresh.id
            pendingFocusSegmentId = fresh.id
        }
        notifyChanged()
    }

    /** Backspace on empty code block → remove it. Returns true if handled. */
    fun handleBackspaceOnEmptyCodeBlock(segment: ComposerSegment.Code): Boolean {
        if (segment.text.isNotEmpty()) return false
        removeCodeSegment(segment)
        return true
    }

    /** Backspace on empty normal segment → focus previous code segment. Returns true if handled. */
    fun handleBackspaceOnEmptyNormalSegment(segment: ComposerSegment.Normal): Boolean {
        if (segment.controller.state.text.isNotEmpty()) return false
        val idx = _segments.indexOf(segment)
        if (idx < 0) return false

        val normalCount = _segments.count { it is ComposerSegment.Normal }
        if (normalCount <= 1) return false

        // Search backwards for a preceding code segment
        var prevCodeIdx = -1
        for (i in (idx - 1) downTo 0) {
            val seg = _segments[i]
            if (seg is ComposerSegment.Code) { prevCodeIdx = i; break }
            if (seg is ComposerSegment.Normal && seg.controller.state.text.isNotEmpty()) break
        }
        if (prevCodeIdx < 0) return false

        _segments.remove(segment)
        val prevCode = _segments[prevCodeIdx.coerceAtMost(_segments.size - 1)]
        focusedSegmentId = prevCode.id
        pendingFocusSegmentId = prevCode.id
        notifyChanged()
        return true
    }

    /**
     * Called when code segment text changes. Detects triple-Enter (\n\n\n)
     * at the end and exits the code block.
     * Returns true if exit was triggered.
     */
    fun handleCodeTextChanged(segment: ComposerSegment.Code, newText: String): Boolean {
        segment.text = newText
        if (!newText.endsWith("\n\n")) return false

        // Trim the 2 trailing newlines
        segment.text = newText.substring(0, newText.length - 2)

        // Find or create the next normal segment to focus
        val idx = _segments.indexOf(segment)
        val nextNormal = _segments.getOrNull(idx + 1) as? ComposerSegment.Normal
        val targetId = if (nextNormal != null) {
            nextNormal.id
        } else {
            val newNormal = ComposerSegment.Normal(id = genId())
            _segments.add(idx + 1, newNormal)
            newNormal.id
        }
        focusedSegmentId = targetId
        pendingFocusSegmentId = targetId
        notifyChanged()
        return true
    }

    // ==================== Formatting ====================

    /** Active formats. Code segments return {CODE_BLOCK} so toolbar highlights it. */
    val activeFormats: Set<RichTextFormat>
        get() = when (val focused = focusedSegment) {
            is ComposerSegment.Normal -> focused.controller.state.activeFormats
            is ComposerSegment.Code -> setOf(RichTextFormat.CODE_BLOCK)
            else -> emptySet()
        }

    /** Disabled formats. Code → inline formats disabled, line formats stay enabled. */
    val toolbarDisabledFormats: Set<RichTextFormat>
        get() = if (isTypingInCode) {
            // Inside code block: disable inline formats but keep CODE_BLOCK (to deselect),
            // and keep line formats (BULLET_LIST, ORDERED_LIST, BLOCKQUOTE) enabled
            // so the user can switch from code block to a line format.
            setOf(
                RichTextFormat.BOLD,
                RichTextFormat.ITALIC,
                RichTextFormat.UNDERLINE,
                RichTextFormat.STRIKETHROUGH,
                RichTextFormat.INLINE_CODE,
                RichTextFormat.LINK
            )
        } else {
            when (val focused = focusedSegment) {
                is ComposerSegment.Normal -> focused.controller.state.toolbarDisabledFormats
                else -> emptySet()
            }
        }

    // ==================== Content Queries ====================

    /** True if any segment has content. Code block existence = content (per spec). */
    val hasContent: Boolean
        get() = _segments.any { segment ->
            when (segment) {
                is ComposerSegment.Normal -> segment.controller.state.text.isNotBlank()
                is ComposerSegment.Code -> true
            }
        }

    val hasCodeBlocks: Boolean
        get() = _segments.any { it is ComposerSegment.Code }

    // ==================== Serialization ====================

    /** Serializes all segments to markdown. Empty segments skipped. */
    fun toMarkdown(): String {
        val parts = mutableListOf<String>()
        for (segment in _segments) {
            when (segment) {
                is ComposerSegment.Normal -> {
                    val md = segment.controller.toMarkdown().trim()
                    if (md.isNotEmpty()) parts.add(md)
                }
                is ComposerSegment.Code -> {
                    val code = segment.text.trim()
                    if (code.isNotEmpty()) {
                        parts.add("```${segment.language}\n$code\n```")
                    }
                }
            }
        }
        return parts.joinToString("\n")
    }

    fun toPlainText(): String {
        return _segments.mapNotNull { segment ->
            when (segment) {
                is ComposerSegment.Normal -> segment.controller.state.text.takeIf { it.isNotEmpty() }
                is ComposerSegment.Code -> segment.text.takeIf { it.isNotEmpty() }
            }
        }.joinToString("\n")
    }

    // ==================== Lifecycle ====================

    /** Resets to a single empty normal segment. ID counter NOT reset (per spec). */
    fun clear() {
        _segments.clear()
        val newNormal = ComposerSegment.Normal(id = genId())
        _segments.add(newNormal)
        focusedSegmentId = newNormal.id
        pendingFocusSegmentId = newNormal.id
        notifyChanged()
    }

    // ==================== Helpers ====================

    /**
     * Strips line format prefixes from each line of the text.
     * Removes "> " (blockquote), "- " (bullet), and "N. " (ordered list) prefixes.
     */
    private fun stripLineFormatPrefixes(text: String): String {
        if (text.isEmpty()) return text
        return text.lines().joinToString("\n") { line ->
            line.removePrefix("> ")
                .removePrefix("- ")
                .removePrefix("• ")
                .let { it.replace(Regex("^\\d+\\. "), "") }
        }
    }

    /**
     * Merges consecutive Normal segments into a single Normal segment.
     * Joins their text with newlines. Preserves focus on the segment that
     * was focused before the merge.
     */
    private fun mergeAdjacentNormals() {
        if (_segments.size <= 1) return

        val merged = mutableListOf<ComposerSegment>()
        var focusInMergedId: String? = null

        var i = 0
        while (i < _segments.size) {
            val seg = _segments[i]
            if (seg is ComposerSegment.Normal) {
                // Collect consecutive Normals
                val normals = mutableListOf(seg)
                var j = i + 1
                while (j < _segments.size && _segments[j] is ComposerSegment.Normal) {
                    normals.add(_segments[j] as ComposerSegment.Normal)
                    j++
                }
                if (normals.size > 1) {
                    // Merge into one
                    val texts = normals.map { it.controller.state.text }
                    val combinedText = texts.filter { it.isNotEmpty() }.joinToString("\n")
                    val newNormal = ComposerSegment.Normal(id = genId())
                    if (combinedText.isNotEmpty()) {
                        newNormal.controller.onTextChanged(combinedText, combinedText.length, combinedText.length)
                    }
                    merged.add(newNormal)
                    // If any of the merged normals was focused, focus the new one
                    if (normals.any { it.id == focusedSegmentId }) {
                        focusInMergedId = newNormal.id
                    }
                    i = j
                } else {
                    merged.add(seg)
                    i++
                }
            } else {
                merged.add(seg)
                i++
            }
        }

        if (merged.size != _segments.size) {
            _segments.clear()
            _segments.addAll(merged)
            if (focusInMergedId != null) {
                focusedSegmentId = focusInMergedId
                pendingFocusSegmentId = focusInMergedId
            }
            notifyChanged()
        }
    }
}
