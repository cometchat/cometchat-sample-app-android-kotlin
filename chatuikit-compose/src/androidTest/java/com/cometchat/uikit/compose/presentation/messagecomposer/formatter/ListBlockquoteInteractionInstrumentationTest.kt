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
 * Instrumentation tests for list and blockquote interactions.
 *
 * Feature: rich-text-formatting-parity (Tasks 16.1–16.7)
 *
 * Tests auto-continuation on Enter, empty item exits list, and inline formats
 * inside list items. Uses [RichTextEditorController] directly to test the
 * list/blockquote logic.
 */
@RunWith(AndroidJUnit4::class)
class ListBlockquoteInteractionInstrumentationTest {

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

    private fun pressEnter() {
        val currentText = controller.state.text
        val cursorPos = controller.state.selectionStart
        val newText = currentText.substring(0, cursorPos) + "\n" +
            currentText.substring(cursorPos)
        controller.onTextChanged(newText, cursorPos + 1, cursorPos + 1)
    }

    private fun moveCursor(pos: Int) {
        controller.onTextChanged(controller.state.text, pos, pos)
    }

    private fun selectRange(start: Int, end: Int) {
        controller.onTextChanged(controller.state.text, start, end)
    }


    // ==================== 16.1 Bullet list auto-continuation on Enter ====================

    /**
     * Activate Bullet List → type text → press Enter → verify new bullet item created.
     *
     * **Validates: Requirement 19.1**
     */
    @Test
    fun bulletList_autoContinuesOnEnter() {
        controller.toggleFormat(RichTextFormat.BULLET_LIST)
        typeText("first item")
        pressEnter()

        val text = controller.state.text
        assertTrue("Should start with bullet prefix", text.startsWith("- "))
        assertTrue("Should have auto-continued bullet", text.contains("\n- "))
        assertEquals("- first item\n- ", text)
    }

    // ==================== 16.2 Ordered list auto-continuation with sequential numbering ====================

    /**
     * Activate Ordered List → type text → press Enter → verify next number (2.) created.
     *
     * **Validates: Requirement 19.2**
     */
    @Test
    fun orderedList_autoContinuesWithSequentialNumbering() {
        controller.toggleFormat(RichTextFormat.ORDERED_LIST)
        typeText("first")
        pressEnter()

        val text = controller.state.text
        assertEquals("1. first\n2. ", text)

        typeText("second")
        pressEnter()

        val text2 = controller.state.text
        assertEquals("1. first\n2. second\n3. ", text2)
    }

    // ==================== 16.3 Empty bullet list item exits list on Enter ====================

    /**
     * Create bullet list → press Enter on empty item → verify list exited.
     *
     * **Validates: Requirement 19.3**
     */
    @Test
    fun emptyBulletListItem_exitsListOnEnter() {
        controller.toggleFormat(RichTextFormat.BULLET_LIST)
        typeText("first")
        pressEnter()
        // Now at "- first\n- " — press Enter on empty "- "
        pressEnter()

        val text = controller.state.text
        assertEquals("- first\n", text)
    }

    // ==================== 16.4 Empty ordered list item exits list on Enter ====================

    /**
     * Create ordered list → press Enter on empty item → verify list exited.
     *
     * **Validates: Requirement 19.4**
     */
    @Test
    fun emptyOrderedListItem_exitsListOnEnter() {
        controller.toggleFormat(RichTextFormat.ORDERED_LIST)
        typeText("first")
        pressEnter()
        // Now at "1. first\n2. " — press Enter on empty "2. "
        pressEnter()

        val text = controller.state.text
        assertEquals("1. first\n", text)
    }

    // ==================== 16.5 Blockquote auto-continuation on Enter ====================

    /**
     * Activate Blockquote → type text → press Enter → verify new blockquote line.
     *
     * **Validates: Requirement 19.5**
     */
    @Test
    fun blockquote_autoContinuesOnEnter() {
        controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
        typeText("quoted text")
        pressEnter()

        val text = controller.state.text
        assertEquals("> quoted text\n> ", text)
    }

    // ==================== 16.6 Empty blockquote line exits blockquote on Enter ====================

    /**
     * Create blockquote → press Enter on empty line → verify blockquote exited.
     *
     * **Validates: Requirement 19.6**
     */
    @Test
    fun emptyBlockquoteLine_exitsBlockquoteOnEnter() {
        controller.toggleFormat(RichTextFormat.BLOCKQUOTE)
        typeText("quoted")
        pressEnter()
        // Now at "> quoted\n> " — press Enter on empty "> "
        pressEnter()

        val text = controller.state.text
        assertEquals("> quoted\n", text)
    }

    // ==================== 16.7 Inline formats work inside list items ====================

    /**
     * Activate Bullet List → type text → select → apply Bold → verify bold inside list item.
     *
     * **Validates: Requirement 20.2**
     */
    @Test
    fun inlineFormats_workInsideListItems() {
        controller.toggleFormat(RichTextFormat.BULLET_LIST)
        typeText("bold item")

        // Text is now "- bold item"
        assertEquals("- bold item", controller.state.text)

        // Select "bold" (positions 2..6 in "- bold item")
        selectRange(2, 6)
        controller.toggleFormat(RichTextFormat.BOLD)

        // Verify bold span exists on the "bold" text
        val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
        assertEquals("Should have one bold span", 1, boldSpans.size)
        assertEquals(2, boldSpans[0].start)
        assertEquals(6, boldSpans[0].end)

        // Verify the markdown serialization nests correctly: "- **bold** item"
        val markdown = controller.toMarkdown()
        assertTrue(
            "Markdown should have bold inside list prefix",
            markdown.contains("- **bold** item")
        )
    }

    // ==================== Additional: Multiple list items with formatting ====================

    /**
     * Verify that multiple list items can each have inline formatting.
     */
    @Test
    fun multipleListItems_eachCanHaveInlineFormatting() {
        controller.toggleFormat(RichTextFormat.BULLET_LIST)
        typeText("one")
        pressEnter()
        typeText("two")

        // Text: "- one\n- two"
        assertEquals("- one\n- two", controller.state.text)

        // Bold "one" (positions 2..5)
        selectRange(2, 5)
        controller.toggleFormat(RichTextFormat.BOLD)

        // Italic "two" (positions 8..11)
        selectRange(8, 11)
        controller.toggleFormat(RichTextFormat.ITALIC)

        val boldSpans = controller.state.spans.filter { RichTextFormat.BOLD in it.formats }
        assertEquals("Should have bold span on 'one'", 1, boldSpans.size)

        val italicSpans = controller.state.spans.filter { RichTextFormat.ITALIC in it.formats }
        assertEquals("Should have italic span on 'two'", 1, italicSpans.size)
    }
}