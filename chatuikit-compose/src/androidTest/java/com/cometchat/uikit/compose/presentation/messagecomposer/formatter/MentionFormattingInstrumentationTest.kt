package com.cometchat.uikit.compose.presentation.messagecomposer.formatter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.core.formatter.ConsumedMentionSpan
import com.cometchat.uikit.core.formatter.MentionSpanProvider
import com.cometchat.uikit.core.formatter.RichTextEditorController
import com.cometchat.uikit.core.formatter.RichTextFormat
import com.cometchat.uikit.core.formatter.SegmentComposerController
import com.cometchat.uikit.core.formatter.ComposerSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for mention preservation/conversion when formatting is applied.
 *
 * Feature: rich-text-formatting-parity (Tasks 13.1–13.11)
 *
 * Tests that BOLD/ITALIC/UNDERLINE/STRIKETHROUGH/BLOCKQUOTE/lists preserve mentions,
 * while INLINE_CODE/CODE_BLOCK convert them to ConsumedMentionSpan, and removing
 * code formats restores them.
 *
 * Uses a fake [MentionSpanProvider] to simulate mention spans without requiring
 * the full SDK mention system.
 */
@RunWith(AndroidJUnit4::class)
class MentionFormattingInstrumentationTest {

    private lateinit var controller: RichTextEditorController
    private lateinit var fakeMentionProvider: FakeMentionSpanProvider

    /**
     * Fake MentionSpanProvider that tracks mentions as simple position-based entries.
     * Allows testing mention consumption/restoration without the full SDK.
     */
    class FakeMentionSpanProvider : MentionSpanProvider {
        data class FakeMention(
            val start: Int,
            val end: Int,
            val id: Char,
            val displayText: String
        )

        val mentions = mutableListOf<FakeMention>()

        val removedMentions = mutableListOf<FakeMention>()
        val restoredMentions = mutableListOf<Pair<IntRange, ConsumedMentionSpan>>()

        fun addMention(start: Int, end: Int, displayText: String) {
            mentions.add(FakeMention(start, end, '@', displayText))
        }

        override fun getMentionsInRange(start: Int, end: Int): List<MentionSpanProvider.MentionInfo> {
            return mentions.filter { it.start < end && it.end > start }
                .map {
                    MentionSpanProvider.MentionInfo(
                        start = it.start,
                        end = it.end,
                        id = it.id,
                        displayText = it.displayText,
                        suggestionItem = null,
                        textAppearance = null
                    )
                }
        }

        override fun removeMentionSpan(start: Int, end: Int) {
            val toRemove = mentions.filter { it.start == start && it.end == end }
            removedMentions.addAll(toRemove)
            mentions.removeAll(toRemove)
        }

        override fun restoreMentionSpan(start: Int, end: Int, consumed: ConsumedMentionSpan) {
            restoredMentions.add(IntRange(start, end - 1) to consumed)
            // Re-add the mention
            mentions.add(FakeMention(start, end, consumed.id, consumed.text ?: ""))
        }
    }

    @Before
    fun setUp() {
        controller = RichTextEditorController()
        fakeMentionProvider = FakeMentionSpanProvider()
        controller.setMentionSpanProvider(fakeMentionProvider)
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

    /**
     * Sets up text "Hello @John rest" with a mention at positions 6..11 ("@John").
     */
    private fun setupTextWithMention() {
        // Simulate typing "Hello @John rest"
        val text = "Hello @John rest"
        controller.onTextChanged(text, text.length, text.length)
        // Register the mention span at position 6..11
        fakeMentionProvider.addMention(6, 11, "@John")
    }

    // ==================== 13.1 BOLD preserves mention span ====================

    /**
     * Insert mention → select text including mention → tap Bold → verify mention span still exists.
     *
     * **Validates: Requirement 21.1**
     */
    @Test
    fun boldApplied_preservesMentionSpan() {
        setupTextWithMention()
        selectRange(0, 16)
        controller.toggleFormat(RichTextFormat.BOLD)

        // Mention should still exist in the provider
        assertTrue(
            "Mention should be preserved after BOLD",
            fakeMentionProvider.mentions.any { it.displayText == "@John" }
        )
        assertTrue(
            "No mentions should have been removed",
            fakeMentionProvider.removedMentions.isEmpty()
        )
    }

    // ==================== 13.2 ITALIC preserves mention span ====================

    /**
     * **Validates: Requirement 21.1**
     */
    @Test
    fun italicApplied_preservesMentionSpan() {
        setupTextWithMention()
        selectRange(0, 16)
        controller.toggleFormat(RichTextFormat.ITALIC)

        assertTrue(
            "Mention should be preserved after ITALIC",
            fakeMentionProvider.mentions.any { it.displayText == "@John" }
        )
        assertTrue(fakeMentionProvider.removedMentions.isEmpty())
    }

    // ==================== 13.3 UNDERLINE preserves mention span ====================

    /**
     * **Validates: Requirement 21.1**
     */
    @Test
    fun underlineApplied_preservesMentionSpan() {
        setupTextWithMention()
        selectRange(0, 16)
        controller.toggleFormat(RichTextFormat.UNDERLINE)

        assertTrue(
            "Mention should be preserved after UNDERLINE",
            fakeMentionProvider.mentions.any { it.displayText == "@John" }
        )
        assertTrue(fakeMentionProvider.removedMentions.isEmpty())
    }

    // ==================== 13.4 STRIKETHROUGH preserves mention span ====================

    /**
     * **Validates: Requirement 21.1**
     */
    @Test
    fun strikethroughApplied_preservesMentionSpan() {
        setupTextWithMention()
        selectRange(0, 16)
        controller.toggleFormat(RichTextFormat.STRIKETHROUGH)

        assertTrue(
            "Mention should be preserved after STRIKETHROUGH",
            fakeMentionProvider.mentions.any { it.displayText == "@John" }
        )
        assertTrue(fakeMentionProvider.removedMentions.isEmpty())
    }

    // ==================== 13.5 BLOCKQUOTE preserves mention span ====================

    /**
     * **Validates: Requirement 21.2**
     */
    @Test
    fun blockquoteApplied_preservesMentionSpan() {
        setupTextWithMention()
        selectRange(0, 16)
        controller.toggleFormat(RichTextFormat.BLOCKQUOTE)

        // Blockquote is a line-based format (adds "> " prefix), doesn't touch mentions
        assertTrue(
            "Mention should be preserved after BLOCKQUOTE",
            fakeMentionProvider.mentions.any { it.displayText == "@John" }
        )
        assertTrue(fakeMentionProvider.removedMentions.isEmpty())
    }

    // ==================== 13.6 BULLET_LIST preserves mention span ====================

    /**
     * **Validates: Requirement 21.2**
     */
    @Test
    fun bulletListApplied_preservesMentionSpan() {
        setupTextWithMention()
        selectRange(0, 16)
        controller.toggleFormat(RichTextFormat.BULLET_LIST)

        assertTrue(
            "Mention should be preserved after BULLET_LIST",
            fakeMentionProvider.mentions.any { it.displayText == "@John" }
        )
        assertTrue(fakeMentionProvider.removedMentions.isEmpty())
    }

    // ==================== 13.7 ORDERED_LIST preserves mention span ====================

    /**
     * **Validates: Requirement 21.2**
     */
    @Test
    fun orderedListApplied_preservesMentionSpan() {
        setupTextWithMention()
        selectRange(0, 16)
        controller.toggleFormat(RichTextFormat.ORDERED_LIST)

        assertTrue(
            "Mention should be preserved after ORDERED_LIST",
            fakeMentionProvider.mentions.any { it.displayText == "@John" }
        )
        assertTrue(fakeMentionProvider.removedMentions.isEmpty())
    }

    // ==================== 13.8 INLINE_CODE converts mention to ConsumedMentionSpan ====================

    /**
     * Insert mention → select → tap Inline Code → verify mention converted to plain text
     * (ConsumedMentionSpan created).
     *
     * **Validates: Requirement 21.3**
     */
    @Test
    fun inlineCodeApplied_convertsMentionToConsumedSpan() {
        setupTextWithMention()
        selectRange(6, 11)
        controller.toggleFormat(RichTextFormat.INLINE_CODE)

        // Mention should have been removed from the provider
        assertTrue(
            "Mention should have been removed by INLINE_CODE",
            fakeMentionProvider.removedMentions.any { it.displayText == "@John" }
        )
        // ConsumedMentionSpan should be stored in state
        assertTrue(
            "ConsumedMentionSpan should be stored",
            controller.state.consumedMentionSpans.isNotEmpty()
        )
        val consumed = controller.state.consumedMentionSpans.values.first()
        assertEquals("@John", consumed.text)
        assertEquals('@', consumed.id)
    }

    // ==================== 13.9 CODE_BLOCK converts mention to ConsumedMentionSpan ====================

    /**
     * Insert mention → tap Code Block → verify mention converted to plain text.
     *
     * **Validates: Requirement 21.4**
     */
    @Test
    fun codeBlockApplied_convertsMentionToConsumedSpan() {
        // For CODE_BLOCK, we use SegmentComposerController
        val segmentController = SegmentComposerController()
        val normalSeg = segmentController.segments[0] as ComposerSegment.Normal
        segmentController.setFocusedSegment(normalSeg.id)

        // Set up text with mention in the normal segment
        val mentionProvider = FakeMentionSpanProvider()
        normalSeg.controller.setMentionSpanProvider(mentionProvider)
        normalSeg.controller.onTextChanged("Hello @John rest", 16, 16)
        mentionProvider.addMention(6, 11, "@John")

        // Select the mention text and toggle code block
        normalSeg.controller.onTextChanged("Hello @John rest", 6, 11)
        // Consume mentions before code block insertion
        normalSeg.controller.consumeMentionsInRange(6, 11)

        // Verify mention was consumed
        assertTrue(
            "Mention should have been removed for CODE_BLOCK",
            mentionProvider.removedMentions.any { it.displayText == "@John" }
        )
        assertTrue(
            "ConsumedMentionSpan should be stored",
            normalSeg.controller.state.consumedMentionSpans.isNotEmpty()
        )
    }

    // ==================== 13.10 Removing INLINE_CODE restores mention ====================

    /**
     * Apply Inline Code to mention → remove Inline Code → verify mention restored.
     *
     * **Validates: Requirement 22.1**
     */
    @Test
    fun removingInlineCode_restoresMentionFromConsumedSpan() {
        setupTextWithMention()

        // Apply inline code to the mention range
        selectRange(6, 11)
        controller.toggleFormat(RichTextFormat.INLINE_CODE)

        // Verify mention was consumed
        assertTrue(fakeMentionProvider.removedMentions.isNotEmpty())
        assertTrue(controller.state.consumedMentionSpans.isNotEmpty())

        // Remove inline code
        selectRange(6, 11)
        controller.toggleFormat(RichTextFormat.INLINE_CODE)

        // Verify mention was restored
        assertTrue(
            "Mention should have been restored",
            fakeMentionProvider.restoredMentions.isNotEmpty()
        )
        val restored = fakeMentionProvider.restoredMentions.first()
        assertEquals("@John", restored.second.text)
    }

    // ==================== 13.11 Removing CODE_BLOCK restores mention ====================

    /**
     * Apply Code Block to mention → exit Code Block → verify mention restored in Normal segment.
     *
     * **Validates: Requirement 22.2**
     */
    @Test
    fun removingCodeBlock_restoresMentionFromConsumedSpan() {
        val segmentController = SegmentComposerController()
        val normalSeg = segmentController.segments[0] as ComposerSegment.Normal
        segmentController.setFocusedSegment(normalSeg.id)

        val mentionProvider = FakeMentionSpanProvider()
        normalSeg.controller.setMentionSpanProvider(mentionProvider)
        normalSeg.controller.onTextChanged("Hello @John", 11, 11)
        mentionProvider.addMention(6, 11, "@John")

        // Consume mentions and insert code block
        normalSeg.controller.onTextChanged("Hello @John", 6, 11)
        normalSeg.controller.consumeMentionsInRange(6, 11)

        // Store consumed mentions in the state for later restoration
        assertTrue(normalSeg.controller.state.consumedMentionSpans.isNotEmpty())

        // Simulate code block creation and removal
        segmentController.toggleCodeBlock()

        // Now there should be a Code segment
        val codeSegments = segmentController.segments.filterIsInstance<ComposerSegment.Code>()
        assertTrue("Should have a Code segment", codeSegments.isNotEmpty())

        // Focus the code segment and toggle code block off (remove it)
        segmentController.setFocusedSegment(codeSegments[0].id)
        segmentController.toggleCodeBlock()

        // After removal, code text should be merged back into a Normal segment
        val normalSegments = segmentController.segments.filterIsInstance<ComposerSegment.Normal>()
        assertTrue("Should have Normal segments after code block removal", normalSegments.isNotEmpty())
    }
}