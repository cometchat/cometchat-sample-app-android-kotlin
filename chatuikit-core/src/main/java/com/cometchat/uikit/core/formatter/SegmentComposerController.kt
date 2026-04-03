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

    fun toggleCodeBlock() {
        when (val focused = focusedSegment) {
            is ComposerSegment.Code -> removeCodeSegment(focused)
            is ComposerSegment.Normal -> insertCodeBlock()
            null -> insertCodeBlock()
        }
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

        if (nextNormal != null) _segments.remove(nextNormal)
        _segments.remove(segment)

        if (prevNormal != null) {
            prevNormal.controller.clear()
            if (merged.isNotEmpty()) {
                prevNormal.controller.onTextChanged(merged, cursorPos, cursorPos)
            }
            focusedSegmentId = prevNormal.id
            pendingFocusSegmentId = prevNormal.id
        } else {
            val newNormal = ComposerSegment.Normal(id = genId())
            if (merged.isNotEmpty()) {
                newNormal.controller.onTextChanged(merged, cursorPos, cursorPos)
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
        if (!newText.endsWith("\n\n\n")) return false

        // Trim the 3 trailing newlines
        segment.text = newText.substring(0, newText.length - 3)

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

    /** Disabled formats. Code → all except CODE_BLOCK disabled. */
    val toolbarDisabledFormats: Set<RichTextFormat>
        get() = if (isTypingInCode) {
            RichTextFormat.entries.toSet() - setOf(RichTextFormat.CODE_BLOCK)
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
}
