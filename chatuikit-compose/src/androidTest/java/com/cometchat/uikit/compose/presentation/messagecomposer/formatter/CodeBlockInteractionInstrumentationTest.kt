package com.cometchat.uikit.compose.presentation.messagecomposer.formatter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.core.formatter.ComposerSegment
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.SegmentComposerController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for code block interactions.
 *
 * Feature: rich-text-formatting-parity (Tasks 15.1–15.5)
 *
 * Tests code block insertion with selection, insertion with no selection,
 * toggle off, toolbar disabled state inside code block, and backspace on
 * empty code block. Uses [SegmentComposerController] directly.
 */
@RunWith(AndroidJUnit4::class)
class CodeBlockInteractionInstrumentationTest {

    private lateinit var segmentController: SegmentComposerController

    @Before
    fun setUp() {
        segmentController = SegmentComposerController()
    }

    // ==================== Helpers ====================

    private fun getFirstNormal(): ComposerSegment.Normal {
        return segmentController.segments.filterIsInstance<ComposerSegment.Normal>().first()
    }

    private fun focusFirstNormal() {
        val normal = getFirstNormal()
        segmentController.setFocusedSegment(normal.id)
    }

    private fun typeIntoNormal(text: String) {
        val normal = getFirstNormal()
        normal.controller.onTextChanged(text, text.length, text.length)
    }


    // ==================== 15.1 Code block insertion with text selected ====================

    /**
     * Type text → select portion → tap Code Block → verify selected text in Code segment,
     * remaining in Normal segments.
     *
     * **Validates: Requirement 6.1**
     */
    @Test
    fun codeBlockInsertion_withSelection_extractsSelectedText() {
        focusFirstNormal()
        val normal = getFirstNormal()

        // Type text and select a portion
        normal.controller.onTextChanged("Hello World Foo", 15, 15)
        normal.controller.onTextChanged("Hello World Foo", 6, 11) // select "World"

        segmentController.toggleCodeBlock()

        // Verify segments: Normal("Hello") + Code("World") + Normal("Foo") or similar
        val codeSegments = segmentController.segments.filterIsInstance<ComposerSegment.Code>()
        assertEquals("Should have one Code segment", 1, codeSegments.size)
        assertEquals("Code segment should contain selected text", "World", codeSegments[0].text)

        // Verify Normal segments exist before and after
        val normalSegments = segmentController.segments.filterIsInstance<ComposerSegment.Normal>()
        assertTrue("Should have Normal segments around Code", normalSegments.size >= 2)

        // Verify the before-normal has "Hello " (trailing space preserved from split at position 6)
        val beforeNormal = normalSegments[0]
        assertEquals("Hello ", beforeNormal.controller.state.text)

        // Verify the after-normal has " Foo" (leading space preserved from split at position 11)
        val afterNormal = normalSegments[1]
        assertEquals(" Foo", afterNormal.controller.state.text)
    }

    // ==================== 15.2 Code block insertion with no selection ====================

    /**
     * Type text → place cursor at end → tap Code Block → verify empty Code segment created.
     *
     * **Validates: Requirement 6.2**
     */
    @Test
    fun codeBlockInsertion_noSelection_createsEmptyCodeBlock() {
        focusFirstNormal()
        val normal = getFirstNormal()

        // Type text with cursor at end
        normal.controller.onTextChanged("Hello World", 11, 11)

        segmentController.toggleCodeBlock()

        // Verify a Code segment was created
        val codeSegments = segmentController.segments.filterIsInstance<ComposerSegment.Code>()
        assertEquals("Should have one Code segment", 1, codeSegments.size)
        assertEquals("Code segment should be empty (cursor was at end)", "", codeSegments[0].text)

        // Verify the original text is preserved in a Normal segment
        val normalSegments = segmentController.segments.filterIsInstance<ComposerSegment.Normal>()
        assertTrue("Should have Normal segments", normalSegments.isNotEmpty())
        assertTrue(
            "Original text should be preserved",
            normalSegments.any { it.controller.state.text == "Hello World" }
        )
    }

    // ==================== 15.3 Code block toggle off merges text back ====================

    /**
     * Create code block with text → tap Code Block again → verify text merged back
     * into Normal segment.
     *
     * **Validates: Requirement 6.4**
     */
    @Test
    fun codeBlockToggleOff_mergesTextBackToNormal() {
        focusFirstNormal()
        val normal = getFirstNormal()

        // Type text and select all to create code block with content
        normal.controller.onTextChanged("code content", 12, 12)
        normal.controller.onTextChanged("code content", 0, 12) // select all

        segmentController.toggleCodeBlock()

        // Verify Code segment exists with content
        val codeSegments = segmentController.segments.filterIsInstance<ComposerSegment.Code>()
        assertEquals(1, codeSegments.size)
        assertEquals("code content", codeSegments[0].text)

        // Focus the code segment and toggle off
        segmentController.setFocusedSegment(codeSegments[0].id)
        segmentController.toggleCodeBlock()

        // Verify no Code segments remain
        val remainingCode = segmentController.segments.filterIsInstance<ComposerSegment.Code>()
        assertTrue("No Code segments should remain after toggle off", remainingCode.isEmpty())

        // Verify text is merged back into Normal
        val normalSegments = segmentController.segments.filterIsInstance<ComposerSegment.Normal>()
        assertTrue("Should have Normal segments", normalSegments.isNotEmpty())
        val allText = normalSegments.joinToString("") { it.controller.state.text }
        assertTrue("Merged text should contain original code content", allText.contains("code content"))
    }

    // ==================== 15.4 Toolbar disabled state inside code block ====================

    /**
     * Focus code segment → verify inline toolbar buttons disabled, line formats remain enabled.
     *
     * **Validates: Requirements 9.2, 9.3**
     *
     * Inside a code block, only inline formats (BOLD, ITALIC, UNDERLINE, STRIKETHROUGH,
     * INLINE_CODE, LINK) are disabled. Line formats (BULLET_LIST, ORDERED_LIST, BLOCKQUOTE)
     * stay enabled so the user can switch from code block to a line format. CODE_BLOCK
     * itself also stays enabled (to allow toggling off).
     */
    @Test
    fun insideCodeBlock_allFormatsDisabledExceptCodeBlock() {
        focusFirstNormal()
        val normal = getFirstNormal()

        // Create a code block
        normal.controller.onTextChanged("code", 4, 4)
        normal.controller.onTextChanged("code", 0, 4)
        segmentController.toggleCodeBlock()

        // Focus the code segment
        val codeSegment = segmentController.segments.filterIsInstance<ComposerSegment.Code>().first()
        segmentController.setFocusedSegment(codeSegment.id)

        // Verify active formats
        val activeFormats = segmentController.activeFormats
        assertTrue("CODE_BLOCK should be active", RichTextFormat.CODE_BLOCK in activeFormats)

        // Verify disabled formats — only inline formats are disabled inside code block
        val disabledFormats = segmentController.toolbarDisabledFormats
        val expectedDisabled = setOf(
            RichTextFormat.BOLD,
            RichTextFormat.ITALIC,
            RichTextFormat.UNDERLINE,
            RichTextFormat.STRIKETHROUGH,
            RichTextFormat.INLINE_CODE,
            RichTextFormat.LINK
        )
        for (format in expectedDisabled) {
            assertTrue(
                "$format should be disabled inside code block",
                format in disabledFormats
            )
        }

        // Line formats should remain enabled (user can switch from code to line format)
        val expectedEnabled = setOf(
            RichTextFormat.CODE_BLOCK,
            RichTextFormat.BULLET_LIST,
            RichTextFormat.ORDERED_LIST,
            RichTextFormat.BLOCKQUOTE
        )
        for (format in expectedEnabled) {
            assertFalse(
                "$format should NOT be disabled inside code block",
                format in disabledFormats
            )
        }
    }

    // ==================== 15.5 Backspace on empty code block removes it ====================

    /**
     * Create empty code block → press Backspace → verify code block removed,
     * focus on preceding Normal.
     *
     * **Validates: Requirements 6.6, 35.1**
     */
    @Test
    fun backspaceOnEmptyCodeBlock_removesIt() {
        focusFirstNormal()
        val normal = getFirstNormal()

        // Create an empty code block (cursor at end of text)
        normal.controller.onTextChanged("Hello", 5, 5)
        segmentController.toggleCodeBlock()

        // Verify Code segment exists and is empty
        val codeSegments = segmentController.segments.filterIsInstance<ComposerSegment.Code>()
        assertEquals(1, codeSegments.size)
        assertEquals("", codeSegments[0].text)

        // Simulate backspace on empty code block
        val handled = segmentController.handleBackspaceOnEmptyCodeBlock(codeSegments[0])
        assertTrue("Backspace should be handled", handled)

        // Verify Code segment is removed
        val remainingCode = segmentController.segments.filterIsInstance<ComposerSegment.Code>()
        assertTrue("Code segment should be removed", remainingCode.isEmpty())

        // Verify focus moved to a Normal segment
        val focusedId = segmentController.focusedSegmentId
        assertNotNull("Should have a focused segment", focusedId)
        val focusedSegment = segmentController.segments.find { it.id == focusedId }
        assertTrue("Focused segment should be Normal", focusedSegment is ComposerSegment.Normal)
    }

    // ==================== Additional: Normal segment invariant after operations ====================

    /**
     * Verify the invariant: there is always a Normal segment before and after every Code segment.
     */
    @Test
    fun segmentInvariant_normalBeforeAndAfterCode() {
        focusFirstNormal()
        val normal = getFirstNormal()

        normal.controller.onTextChanged("test", 4, 4)
        normal.controller.onTextChanged("test", 0, 4)
        segmentController.toggleCodeBlock()

        val segments = segmentController.segments
        for (i in segments.indices) {
            if (segments[i] is ComposerSegment.Code) {
                assertTrue(
                    "Code segment at index $i should have Normal before it",
                    i > 0 && segments[i - 1] is ComposerSegment.Normal
                )
                assertTrue(
                    "Code segment at index $i should have Normal after it",
                    i < segments.size - 1 && segments[i + 1] is ComposerSegment.Normal
                )
            }
        }

        // At least one Normal segment should always exist
        assertTrue(
            "Should always have at least one Normal segment",
            segments.any { it is ComposerSegment.Normal }
        )
    }
}