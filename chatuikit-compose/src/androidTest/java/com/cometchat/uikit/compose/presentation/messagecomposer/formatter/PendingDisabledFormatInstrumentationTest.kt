package com.cometchat.uikit.compose.presentation.messagecomposer.formatter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.core.formatter.RichTextEditorController
import com.cometchat.uikit.core.formatter.RichTextFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for pending and disabled format behavior.
 *
 * Feature: rich-text-formatting-parity (Tasks 17.1–17.4)
 *
 * Tests pending format applied on type, cleared after typing, disabled format
 * prevents inheritance, and cursor movement clears both. Uses
 * [RichTextEditorController] directly.
 */
@RunWith(AndroidJUnit4::class)
class PendingDisabledFormatInstrumentationTest {

    private lateinit var controller: RichTextEditorController

    @Before
    fun setUp() {
        controller = RichTextEditorController()
    }

    // ==================== Helpers ====================

    private fun typeChar(ch: Char) {
        val currentText = controller.state.text
        val cursorPos = controller.state.selectionStart
        val newText = currentText.substring(0, cursorPos) + ch +
            currentText.substring(cursorPos)
        controller.onTextChanged(newText, cursorPos + 1, cursorPos + 1)
    }

    private fun typeText(text: String) {
        for (ch in text) {
            typeChar(ch)
        }
    }

    private fun moveCursor(pos: Int) {
        controller.onTextChanged(controller.state.text, pos, pos)
    }


    // ==================== 17.1 Pending format: tap Bold, type character, verify bold ====================

    /**
     * Place cursor → tap Bold → type "a" → verify "a" is bold.
     *
     * **Validates: Requirements 12.1, 12.2**
     */
    @Test
    fun pendingFormat_appliedToNextTypedCharacter() {
        // Tap Bold with no selection (pending format)
        controller.toggleFormat(RichTextFormat.BOLD)
        assertTrue(
            "BOLD should be in pendingFormats",
            RichTextFormat.BOLD in controller.state.pendingFormats
        )

        // Type "a"
        typeChar('a')

        // Verify "a" is bold
        assertEquals("a", controller.state.text)
        val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
        assertEquals("Should have one bold span", 1, boldSpans.size)
        assertEquals(0, boldSpans[0].start)
        assertEquals(1, boldSpans[0].end)

        // Pending formats should be cleared after consumption
        assertTrue(
            "pendingFormats should be cleared after typing",
            controller.state.pendingFormats.isEmpty()
        )
    }

    // ==================== 17.2 Pending format cleared after typing ====================

    /**
     * Tap Bold → type "a" → type "b" → verify "b" is NOT bold (pending consumed).
     * Since "a" was typed at the end of the bold span, "b" inherits bold from the span.
     * This test verifies the pending format mechanism: pending is consumed on first char,
     * but subsequent chars inherit from the span (which is expected behavior).
     *
     * To truly test that pending is consumed and NOT re-applied, we test with cursor
     * movement between characters.
     *
     * **Validates: Requirement 12.3**
     */
    @Test
    fun pendingFormat_clearedAfterTyping() {
        // Tap Bold (pending)
        controller.toggleFormat(RichTextFormat.BOLD)
        assertTrue(controller.state.pendingFormats.contains(RichTextFormat.BOLD))

        // Type "a" — consumes pending
        typeChar('a')
        assertTrue("pendingFormats should be empty after typing", controller.state.pendingFormats.isEmpty())

        // The bold span now covers [0,1). Typing "b" at position 1 will inherit bold
        // from the span (this is correct behavior — span extension, not pending).
        // To verify pending was truly consumed, we check the state directly.
        assertEquals("a", controller.state.text)

        // Now disable bold so next char won't be bold
        controller.toggleFormat(RichTextFormat.BOLD) // disable (cursor is inside bold span)
        assertTrue(
            "BOLD should be in disabledFormats",
            RichTextFormat.BOLD in controller.state.disabledFormats
        )

        // Type "b" — should NOT be bold (disabled format prevents inheritance)
        typeChar('b')
        assertEquals("ab", controller.state.text)

        // Verify "a" is bold but "b" is not
        val formatsAtA = controller.state.spanManager.getFormatsAt(0)
        assertTrue("'a' should be bold", RichTextFormat.BOLD in formatsAtA)

        val formatsAtB = controller.state.spanManager.getFormatsAt(1)
        assertFalse("'b' should NOT be bold", RichTextFormat.BOLD in formatsAtB)
    }

    // ==================== 17.3 Disabled format prevents inheritance ====================

    /**
     * Type bold text → place cursor inside → tap Bold OFF → type "x" → verify "x" is NOT bold.
     *
     * **Validates: Requirements 13.1, 13.2**
     */
    @Test
    fun disabledFormat_preventsInheritance() {
        // Type bold text
        controller.toggleFormat(RichTextFormat.BOLD)
        typeText("hello")

        // Verify all text is bold
        val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
        assertEquals(1, boldSpans.size)
        assertEquals(0, boldSpans[0].start)
        assertEquals(5, boldSpans[0].end)

        // Cursor is at position 5 (end of "hello"), inside the bold span
        // Tap Bold OFF → adds to disabledFormats
        controller.toggleFormat(RichTextFormat.BOLD)
        assertTrue(
            "BOLD should be in disabledFormats",
            RichTextFormat.BOLD in controller.state.disabledFormats
        )

        // Type "x" — should NOT be bold
        typeChar('x')
        assertEquals("hellox", controller.state.text)

        // Verify "hello" is bold but "x" is not
        val formatsAtHello = controller.state.spanManager.getFormatsAt(2)
        assertTrue("'hello' should still be bold", RichTextFormat.BOLD in formatsAtHello)

        val formatsAtX = controller.state.spanManager.getFormatsAt(5)
        assertFalse("'x' should NOT be bold", RichTextFormat.BOLD in formatsAtX)

        // disabledFormats should be cleared after consumption
        assertTrue(
            "disabledFormats should be cleared after typing",
            controller.state.disabledFormats.isEmpty()
        )
    }

    // ==================== 17.4 Cursor movement clears pending and disabled formats ====================

    /**
     * Tap Bold (pending) → move cursor → verify pending cleared.
     * Also: disable format → move cursor → verify disabled cleared.
     *
     * **Validates: Requirement 12.4**
     */
    @Test
    fun cursorMovement_clearsPendingAndDisabledFormats() {
        // Test pending format cleared on cursor movement
        typeText("hi")
        controller.toggleFormat(RichTextFormat.ITALIC)
        assertTrue(
            "ITALIC should be in pendingFormats",
            RichTextFormat.ITALIC in controller.state.pendingFormats
        )

        // Move cursor to position 0
        moveCursor(0)
        assertTrue(
            "pendingFormats should be cleared after cursor movement",
            controller.state.pendingFormats.isEmpty()
        )

        // Test disabled format cleared on cursor movement
        controller.toggleFormat(RichTextFormat.BOLD)
        typeText("bold")
        // Cursor is at end of "bold" text, inside bold span
        controller.toggleFormat(RichTextFormat.BOLD) // disable
        assertTrue(
            "BOLD should be in disabledFormats",
            RichTextFormat.BOLD in controller.state.disabledFormats
        )

        // Move cursor
        moveCursor(0)
        assertTrue(
            "disabledFormats should be cleared after cursor movement",
            controller.state.disabledFormats.isEmpty()
        )
    }

    // ==================== Additional: Multiple pending formats ====================

    /**
     * Verify that multiple pending formats can be set and all are applied to the next character.
     */
    @Test
    fun multiplePendingFormats_allAppliedToNextChar() {
        controller.toggleFormat(RichTextFormat.BOLD)
        controller.toggleFormat(RichTextFormat.ITALIC)
        controller.toggleFormat(RichTextFormat.UNDERLINE)

        assertTrue(RichTextFormat.BOLD in controller.state.pendingFormats)
        assertTrue(RichTextFormat.ITALIC in controller.state.pendingFormats)
        assertTrue(RichTextFormat.UNDERLINE in controller.state.pendingFormats)

        typeChar('X')

        assertEquals("X", controller.state.text)
        val spans = controller.state.spans
        assertEquals(1, spans.size)
        assertTrue(RichTextFormat.BOLD in spans[0].formats)
        assertTrue(RichTextFormat.ITALIC in spans[0].formats)
        assertTrue(RichTextFormat.UNDERLINE in spans[0].formats)

        // All pending formats consumed
        assertTrue(controller.state.pendingFormats.isEmpty())
    }
}