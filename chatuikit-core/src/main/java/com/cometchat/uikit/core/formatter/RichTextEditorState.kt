package com.cometchat.uikit.core.formatter

/**
 * Observable state for the rich text editor.
 * Holds the plain text, formatting spans, cursor/selection info,
 * pending formats (for cursor-only toggle), and disabled formats.
 *
 * Platform-agnostic — shared by Jetpack Compose and Kotlin XML UI kits.
 * UI layers observe this state and render accordingly.
 */
class RichTextEditorState {

    private val _spanManager = RichTextSpanManager()

    /** Current plain text (no markdown markers). */
    var text: String = ""
        private set

    /** Current selection start (inclusive). */
    var selectionStart: Int = 0
        private set

    /** Current selection end (exclusive). Same as selectionStart when cursor is collapsed. */
    var selectionEnd: Int = 0
        private set

    /**
     * Formats that will be applied to the next typed character when cursor is collapsed.
     * Cleared when the user explicitly moves the cursor (not during text changes).
     */
    val pendingFormats: MutableSet<RichTextFormat> = mutableSetOf()

    /**
     * Formats explicitly disabled at the cursor. When the cursor is inside a bold span
     * and the user toggles bold off, BOLD is added here so new text won't inherit it.
     * Cleared when the user explicitly moves the cursor.
     */
    val disabledFormats: MutableSet<RichTextFormat> = mutableSetOf()

    /** Read-only snapshot of current spans. */
    val spans: List<RichTextSpan> get() = _spanManager.spans

    /** The underlying span manager (for direct operations). */
    val spanManager: RichTextSpanManager get() = _spanManager

    /** True when cursor is collapsed (no selection range). */
    val isCursorCollapsed: Boolean get() = selectionStart == selectionEnd

    /**
     * Returns the set of formats currently "active" — either from spans at the cursor
     * position, or from pending formats (when cursor is collapsed).
     */
    val activeFormats: Set<RichTextFormat>
        get() {
            val spanFormats = if (isCursorCollapsed) {
                // Check position and position-1 to handle cursor at span boundary
                val atPos = _spanManager.getFormatsAt(selectionStart)
                val beforePos = if (selectionStart > 0) _spanManager.getFormatsAt(selectionStart - 1) else emptySet()
                atPos + beforePos
            } else {
                _spanManager.getFormatsInRange(selectionStart, selectionEnd)
            }
            // Active = (span formats + pending) - disabled
            return (spanFormats + pendingFormats) - disabledFormats
        }

    /**
     * Returns formats that should be grayed out / non-clickable in the toolbar,
     * based on the current active formats and compatibility rules.
     */
    val toolbarDisabledFormats: Set<RichTextFormat>
        get() = FormatCompatibility.getDisabledFormats(activeFormats)

    // ==================== Mutations ====================

    /** Sets the text content. Does NOT adjust spans — use [RichTextEditorController] for edits. */
    fun setText(newText: String) {
        text = newText
    }

    /**
     * Updates the selection range. Clears pending/disabled formats when selection changes.
     * Used when the user explicitly moves the cursor (not during text input).
     */
    fun setSelection(start: Int, end: Int) {
        val changed = start != selectionStart || end != selectionEnd
        selectionStart = start
        selectionEnd = end
        if (changed) {
            pendingFormats.clear()
            disabledFormats.clear()
        }
    }

    /**
     * Updates the selection range WITHOUT clearing pending/disabled formats.
     * Used internally by the controller during text change processing,
     * where pending/disabled formats are managed explicitly.
     */
    fun setSelectionInternal(start: Int, end: Int) {
        selectionStart = start
        selectionEnd = end
    }

    /** Resets everything to empty state. */
    fun clear() {
        text = ""
        selectionStart = 0
        selectionEnd = 0
        pendingFormats.clear()
        disabledFormats.clear()
        _spanManager.clear()
    }
}
