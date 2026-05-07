package com.cometchat.uikit.compose.presentation.messagecomposer.formatter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.core.formatter.RichTextEditorController
import com.cometchat.uikit.core.formatter.RichTextFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for WYSIWYG rendering in the composer input field.
 *
 * Feature: rich-text-formatting-parity (Tasks 12.1–12.9)
 *
 * These tests verify that applying a format produces the correct controller state
 * (spans with correct formats) which drives the SpanBasedVisualTransformation
 * to produce the correct SpanStyle output. Since SpanBasedVisualTransformation
 * is a pure mapping from RichTextFormat → SpanStyle, we verify the controller
 * state that feeds it.
 */
@RunWith(AndroidJUnit4::class)
class WysiwygRenderingInstrumentationTest {

    private lateinit var controller: RichTextEditorController

    @Before
    fun setUp() {
        controller = RichTextEditorController()
    }

    // ==================== Helpers ====================

    private fun typeText(text: String) {
        for (ch in text) {
            val currentText = controller.state.text
            val cursorPos = controller.state.selectionStart
            val newText = currentText.substring(0, cursorPos) + ch +
                currentText.substring(cursorPos)
            controller.onTextChanged(newText, cursorPos + 1, cursorPos + 1)
        }
    }

    private fun selectRange(start: Int, end: Int) {
        controller.onTextChanged(controller.state.text, start, end)
    }

    private fun moveCursor(pos: Int) {
        controller.onTextChanged(controller.state.text, pos, pos)
    }


    // ==================== 12.1 Bold renders with FontWeight.Bold ====================

    /**
     * Type text → select → tap Bold → verify span contains BOLD format.
     * SpanBasedVisualTransformation maps BOLD → SpanStyle(fontWeight=FontWeight.Bold).
     *
     * **Validates: Requirement 1.1**
     */
    @Test
    fun boldText_rendersWithBoldFormat() {
        typeText("hello world")
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.BOLD)

        val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
        assertEquals("Should have exactly one bold span", 1, boldSpans.size)
        assertEquals(0, boldSpans[0].start)
        assertEquals(5, boldSpans[0].end)
        assertTrue(RichTextFormat.BOLD in boldSpans[0].formats)
    }

    // ==================== 12.2 Italic renders with FontStyle.Italic ====================

    /**
     * Type text → select → tap Italic → verify span contains ITALIC format.
     * SpanBasedVisualTransformation maps ITALIC → SpanStyle(fontStyle=FontStyle.Italic).
     *
     * **Validates: Requirement 2.1**
     */
    @Test
    fun italicText_rendersWithItalicFormat() {
        typeText("hello world")
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.ITALIC)

        val italicSpans = controller.state.spans.filter { RichTextFormat.ITALIC in it.formats }
        assertEquals("Should have exactly one italic span", 1, italicSpans.size)
        assertEquals(0, italicSpans[0].start)
        assertEquals(5, italicSpans[0].end)
        assertTrue(RichTextFormat.ITALIC in italicSpans[0].formats)
    }

    // ==================== 12.3 Underline renders with TextDecoration.Underline ====================

    /**
     * Type text → select → tap Underline → verify span contains UNDERLINE format.
     * SpanBasedVisualTransformation maps UNDERLINE → SpanStyle(textDecoration=TextDecoration.Underline).
     *
     * **Validates: Requirement 3.1**
     */
    @Test
    fun underlineText_rendersWithUnderlineFormat() {
        typeText("hello world")
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.UNDERLINE)

        val underlineSpans = controller.state.spans.filter { RichTextFormat.UNDERLINE in it.formats }
        assertEquals("Should have exactly one underline span", 1, underlineSpans.size)
        assertEquals(0, underlineSpans[0].start)
        assertEquals(5, underlineSpans[0].end)
        assertTrue(RichTextFormat.UNDERLINE in underlineSpans[0].formats)
    }

    // ==================== 12.4 Strikethrough renders with TextDecoration.LineThrough ====================

    /**
     * Type text → select → tap Strikethrough → verify span contains STRIKETHROUGH format.
     * SpanBasedVisualTransformation maps STRIKETHROUGH → SpanStyle(textDecoration=TextDecoration.LineThrough).
     *
     * **Validates: Requirement 4.1**
     */
    @Test
    fun strikethroughText_rendersWithStrikethroughFormat() {
        typeText("hello world")
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.STRIKETHROUGH)

        val strikeSpans = controller.state.spans.filter { RichTextFormat.STRIKETHROUGH in it.formats }
        assertEquals("Should have exactly one strikethrough span", 1, strikeSpans.size)
        assertEquals(0, strikeSpans[0].start)
        assertEquals(5, strikeSpans[0].end)
        assertTrue(RichTextFormat.STRIKETHROUGH in strikeSpans[0].formats)
    }

    // ==================== 12.5 Inline code renders with monospace and background ====================

    /**
     * Type text → select → tap Inline Code → verify span contains INLINE_CODE format.
     * SpanBasedVisualTransformation maps INLINE_CODE → SpanStyle(fontFamily=Monospace, background=BackgroundColor3).
     *
     * **Validates: Requirements 5.1, 5.5**
     */
    @Test
    fun inlineCodeText_rendersWithInlineCodeFormat() {
        typeText("hello world")
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.INLINE_CODE)

        val codeSpans = controller.state.spans.filter { RichTextFormat.INLINE_CODE in it.formats }
        assertEquals("Should have exactly one inline code span", 1, codeSpans.size)
        assertEquals(0, codeSpans[0].start)
        assertEquals(5, codeSpans[0].end)
        assertTrue(RichTextFormat.INLINE_CODE in codeSpans[0].formats)
    }

    // ==================== 12.6 Link renders with primary color and underline ====================

    /**
     * Insert link via applyLink → verify span contains LINK format.
     * SpanBasedVisualTransformation maps LINK → SpanStyle(color=primary, textDecoration=Underline).
     *
     * **Validates: Requirement 15.4**
     */
    @Test
    fun linkText_rendersWithLinkFormat() {
        typeText("click here for info")
        selectRange(6, 10)
        controller.applyLink("here", "https://example.com")

        val linkSpans = controller.state.spans.filter { RichTextFormat.LINK in it.formats }
        assertEquals("Should have exactly one link span", 1, linkSpans.size)
        assertTrue(RichTextFormat.LINK in linkSpans[0].formats)
    }

    // ==================== 12.7 Combined UNDERLINE + STRIKETHROUGH ====================

    /**
     * Select text → apply Underline → apply Strikethrough → verify both formats on same span.
     * SpanBasedVisualTransformation combines via TextDecoration.combine(Underline, LineThrough).
     *
     * **Validates: Requirement 20.1**
     */
    @Test
    fun combinedUnderlineStrikethrough_rendersBothDecorations() {
        typeText("hello world")
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.UNDERLINE)
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.STRIKETHROUGH)

        val combinedSpans = controller.state.spans.filter {
            RichTextFormat.UNDERLINE in it.formats && RichTextFormat.STRIKETHROUGH in it.formats
        }
        assertEquals("Should have a span with both UNDERLINE and STRIKETHROUGH", 1, combinedSpans.size)
        assertEquals(0, combinedSpans[0].start)
        assertEquals(5, combinedSpans[0].end)
    }

    // ==================== 12.8 Code block segment renders with monospace ====================

    /**
     * Tap Code Block → verify code segment is created.
     * CodeSegmentTextField renders with monospace font, BackgroundColor2 background,
     * StrokeColorDefault border, 16dp corners.
     *
     * **Validates: Requirements 7.1, 7.2, 7.3**
     */
    @Test
    fun codeBlockSegment_createsCodeSegment() {
        // Use SegmentComposerController to test code block creation
        val segmentController = com.cometchat.uikit.core.formatter.SegmentComposerController()
        val normalSegment = segmentController.segments[0]
        segmentController.setFocusedSegment(normalSegment.id)

        // Type some text into the normal segment
        val normal = normalSegment as com.cometchat.uikit.core.formatter.ComposerSegment.Normal
        normal.controller.onTextChanged("some code", 9, 9)
        normal.controller.onTextChanged("some code", 0, 9) // select all

        segmentController.toggleCodeBlock()

        // Verify a Code segment was created
        val codeSegments = segmentController.segments.filterIsInstance<com.cometchat.uikit.core.formatter.ComposerSegment.Code>()
        assertTrue("Should have at least one Code segment", codeSegments.isNotEmpty())
    }

    // ==================== 12.9 Removing format preserves other formats ====================

    /**
     * Apply Bold + Italic → remove Bold → verify Italic still active.
     *
     * **Validates: Requirements 1.4, 20.3**
     */
    @Test
    fun removingFormat_preservesOtherFormats() {
        typeText("hello")
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.BOLD)
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.ITALIC)

        // Verify both formats present
        val bothSpans = controller.state.spans.filter {
            RichTextFormat.BOLD in it.formats && RichTextFormat.ITALIC in it.formats
        }
        assertEquals("Should have span with both BOLD and ITALIC", 1, bothSpans.size)

        // Remove Bold
        selectRange(0, 5)
        controller.toggleFormat(RichTextFormat.BOLD)

        // Verify Italic still present, Bold removed
        val italicSpans = controller.state.spans.filter { RichTextFormat.ITALIC in it.formats }
        assertEquals("Italic should still be present", 1, italicSpans.size)
        assertEquals(0, italicSpans[0].start)
        assertEquals(5, italicSpans[0].end)

        val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
        assertTrue("Bold should be removed", boldSpans.isEmpty())
    }
}