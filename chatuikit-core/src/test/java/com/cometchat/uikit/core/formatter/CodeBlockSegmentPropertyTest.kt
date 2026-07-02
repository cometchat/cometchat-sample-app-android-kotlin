package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for code block segment management.
 *
 * Tests Properties 10–15 from the rich-text-formatting-parity design document.
 * Each property is validated with a minimum of 100 random iterations.
 */
class CodeBlockSegmentPropertyTest : StringSpec({

    // ==================== Shared Generators ====================

    /** Generates printable text without newlines (single-line) and without markdown triggers. */
    val arbSingleLineText: Arb<String> = Arb.string(minSize = 1, maxSize = 100)
        .let { arb ->
            arbitrary {
                val s = arb.bind()
                // Replace newlines and markdown-triggering characters to avoid shortcut detection
                val cleaned = s.replace('\n', 'x').replace('\r', 'x')
                    .replace('*', 'x').replace('_', 'x').replace('~', 'x').replace('`', 'x')
                if (cleaned.isEmpty()) "a" else cleaned
            }
        }

    /** Generates multi-line text (2-5 lines, each non-empty and markdown-safe). */
    val arbMultiLineText: Arb<String> = arbitrary {
        val lineCount = Arb.int(2, 5).bind()
        val lines = (1..lineCount).map {
            val line = arbSingleLineText.bind()
            // Ensure lines don't start with line format prefixes
            line.removePrefix("> ").removePrefix("- ").removePrefix("• ")
                .let { it.replace(Regex("^\\d+\\. "), "") }
                .let { if (it.isBlank()) "text" else it }
        }
        lines.joinToString("\n")
    }

    /** Generates random code content (may contain newlines, no markdown triggers). */
    val arbCodeContent: Arb<String> = Arb.string(minSize = 1, maxSize = 80)
        .let { arb ->
            arbitrary {
                val s = arb.bind()
                val cleaned = s.replace('\r', '\n')
                    .replace('*', 'x').replace('_', 'x').replace('~', 'x').replace('`', 'x')
                    .replace('[', 'x').replace(']', 'x')
                if (cleaned.isEmpty()) "code" else cleaned
            }
        }

    // ==================== Helpers ====================

    /** Checks the Normal segment invariant: at least one Normal, and every Code has Normal before and after. */
    fun checkNormalInvariant(segments: List<ComposerSegment>) {
        // At least one Normal segment
        segments.any { it is ComposerSegment.Normal }.shouldBeTrue()

        // Every Code segment has Normal before and after
        segments.forEachIndexed { index, segment ->
            if (segment is ComposerSegment.Code) {
                (index > 0).shouldBeTrue()
                segments[index - 1].shouldBeInstanceOf<ComposerSegment.Normal>()
                (index < segments.size - 1).shouldBeTrue()
                segments[index + 1].shouldBeInstanceOf<ComposerSegment.Normal>()
            }
        }
    }


    // ==================== Property 12 ====================
    // Feature: rich-text-formatting-parity, Property 12: Code block insertion with cursor in middle extracts current line

    /**
     * Property 12: For any multi-line Normal segment and cursor positioned in the middle
     * of a line, calling insertCodeBlock() SHALL produce a Code segment whose text equals
     * the line at the cursor position.
     *
     * **Validates: Requirements 6.3, 6.5**
     */
    "Property 12: code block insertion with cursor in middle extracts current line" {
        data class TestCase(val text: String, val lineIndex: Int, val cursorOffset: Int)

        val arbTestCase: Arb<TestCase> = arbitrary {
            val text = arbMultiLineText.bind()
            val lines = text.split('\n')
            val lineIndex = Arb.int(0, lines.size - 1).bind()
            val line = lines[lineIndex]
            // Cursor within the line (not at the very end of the full text)
            val cursorOffset = Arb.int(0, line.length.coerceAtLeast(0)).bind()
            TestCase(text, lineIndex, cursorOffset)
        }

        checkAll(100, arbTestCase) { (text, lineIndex, cursorOffset) ->
            val lines = text.split('\n')
            // Calculate absolute cursor position
            var absolutePos = 0
            for (i in 0 until lineIndex) {
                absolutePos += lines[i].length + 1 // +1 for '\n'
            }
            absolutePos += cursorOffset

            // Skip if cursor is at the very end of text (that's Property 11's scenario)
            if (absolutePos >= text.length) return@checkAll

            val controller = SegmentComposerController()
            val firstNormal = controller.segments.first() as ComposerSegment.Normal
            firstNormal.controller.onTextChanged(text, absolutePos, absolutePos)
            controller.setFocusedSegment(firstNormal.id)

            controller.insertCodeBlock()

            // Find the Code segment
            val codeSegments = controller.segments.filterIsInstance<ComposerSegment.Code>()
            codeSegments.size shouldBe 1
            val codeSegment = codeSegments.first()

            // Code segment text should equal the line at the cursor
            codeSegment.text shouldBe lines[lineIndex]

            // Normal segment invariant must hold
            checkNormalInvariant(controller.segments)
        }
    }


    // ==================== Property 13 ====================
    // Feature: rich-text-formatting-parity, Property 13: Code block removal merges text back into Normal segments

    /**
     * Property 13: For any Code segment with text, calling removeCodeSegment() SHALL
     * merge the code text into the surrounding Normal segment(s), with the cursor
     * positioned at the end of the merged code text.
     *
     * **Validates: Requirements 6.4**
     */
    "Property 13: code block removal merges text back into Normal segments" {
        checkAll(100, arbCodeContent) { codeText ->
            val controller = SegmentComposerController()
            // Set up: insert a code block first
            val firstNormal = controller.segments.first() as ComposerSegment.Normal
            firstNormal.controller.onTextChanged("before", 5, 5)
            controller.setFocusedSegment(firstNormal.id)
            controller.insertCodeBlock()

            // Find the Code segment and set its text
            val codeSegment = controller.segments.filterIsInstance<ComposerSegment.Code>().first()
            codeSegment.text = codeText

            // Remove the code segment
            controller.removeCodeSegment(codeSegment)

            // No Code segments should remain
            controller.segments.filterIsInstance<ComposerSegment.Code>().size shouldBe 0

            // The merged Normal segment should contain the code text
            val allNormalText = controller.segments
                .filterIsInstance<ComposerSegment.Normal>()
                .joinToString("") { it.controller.state.text }
            allNormalText.contains(codeText).shouldBeTrue()

            // Normal segment invariant must hold
            checkNormalInvariant(controller.segments)
        }
    }

    // ==================== Property 14 ====================
    // Feature: rich-text-formatting-parity, Property 14: Double-newline exit trims trailing newlines and moves focus

    /**
     * Property 14: For any Code segment, when the text ends with "\n\n",
     * handleCodeTextChanged() SHALL trim the two trailing newlines and set
     * focusedSegmentId to the next Normal segment (creating one if needed).
     *
     * **Validates: Requirements 8.1, 8.2, 8.3**
     */
    "Property 14: double-newline exit trims trailing newlines and moves focus" {
        checkAll(100, arbCodeContent) { baseText ->
            // Ensure baseText doesn't already end with \n\n
            val cleanBase = baseText.trimEnd('\n')
            val textWithDoubleNewline = "$cleanBase\n\n"

            val controller = SegmentComposerController()
            val firstNormal = controller.segments.first() as ComposerSegment.Normal
            firstNormal.controller.onTextChanged("before", 5, 5)
            controller.setFocusedSegment(firstNormal.id)
            controller.insertCodeBlock()

            val codeSegment = controller.segments.filterIsInstance<ComposerSegment.Code>().first()

            // Simulate typing that ends with double newline
            val handled = controller.handleCodeTextChanged(codeSegment, textWithDoubleNewline)

            // Should have been handled (double newline detected)
            handled.shouldBeTrue()

            // Code segment text should have trailing newlines trimmed
            codeSegment.text shouldBe cleanBase

            // Focus should be on a Normal segment (not the Code segment)
            val focusedSeg = controller.segments.find { it.id == controller.focusedSegmentId }
            focusedSeg.shouldBeInstanceOf<ComposerSegment.Normal>()

            // The focused Normal should be after the Code segment
            val codeIdx = controller.segments.indexOf(codeSegment)
            val focusedIdx = controller.segments.indexOf(focusedSeg)
            focusedIdx shouldBe codeIdx + 1
        }
    }

    // ==================== Property 15 ====================
    // Feature: rich-text-formatting-parity, Property 15: Normal segment invariant

    /**
     * Property 15: For any sequence of segment operations (insert, remove, clear),
     * the segment list SHALL always contain at least one Normal segment, and every
     * Code segment SHALL have a Normal segment immediately before and after it.
     *
     * **Validates: Requirements 6.5, 35.3**
     */
    "Property 15: Normal segment invariant holds after random operations" {
        // Operations: 0=insertCodeBlock, 1=removeCodeSegment, 2=clear
        val arbOps: Arb<List<Int>> = Arb.list(Arb.int(0, 2), 1..10)

        checkAll(100, arbOps, arbSingleLineText) { ops, text ->
            val controller = SegmentComposerController()

            // Set up initial text
            val firstNormal = controller.segments.first() as ComposerSegment.Normal
            firstNormal.controller.onTextChanged(text, text.length, text.length)
            controller.setFocusedSegment(firstNormal.id)

            for (op in ops) {
                when (op) {
                    0 -> {
                        // Insert code block — focus a Normal segment first
                        val normals = controller.segments.filterIsInstance<ComposerSegment.Normal>()
                        if (normals.isNotEmpty()) {
                            val target = normals.first()
                            controller.setFocusedSegment(target.id)
                            controller.insertCodeBlock()
                        }
                    }
                    1 -> {
                        // Remove a code segment if one exists
                        val codes = controller.segments.filterIsInstance<ComposerSegment.Code>()
                        if (codes.isNotEmpty()) {
                            controller.removeCodeSegment(codes.first())
                        }
                    }
                    2 -> {
                        controller.clear()
                    }
                }

                // Invariant must hold after EVERY operation
                checkNormalInvariant(controller.segments)
            }
        }
    }
})
