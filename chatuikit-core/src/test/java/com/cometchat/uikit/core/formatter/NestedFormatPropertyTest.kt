package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for nested format combinations.
 *
 * Tests Properties 23–25 from the rich-text-formatting-parity design document.
 * Each property is validated with a minimum of 100 random iterations.
 */
class NestedFormatPropertyTest : StringSpec({

    // ==================== Shared Generators ====================

    /** Generates printable text without newlines, no markdown-conflicting chars. */
    val arbSafeText: Arb<String> = Arb.string(minSize = 2, maxSize = 80)
        .let { arb ->
            arbitrary {
                val s = arb.bind()
                val cleaned = s.replace('\n', 'x').replace('\r', 'x')
                    .replace("*", "x").replace("_", "x").replace("~", "x")
                    .replace("`", "x").replace("<", "x").replace(">", "x")
                    .replace("[", "x").replace("]", "x")
                    .removePrefix("- ").removePrefix("• ")
                    .removePrefix("> ")
                    .replace(Regex("^\\d+\\. "), "")
                if (cleaned.isBlank() || cleaned.length < 2) "hello world" else cleaned
            }
        }

    val compatibleFormats = listOf(
        RichTextFormat.BOLD,
        RichTextFormat.ITALIC,
        RichTextFormat.UNDERLINE,
        RichTextFormat.STRIKETHROUGH
    )

    val inlineFormats = listOf(
        RichTextFormat.BOLD,
        RichTextFormat.ITALIC,
        RichTextFormat.UNDERLINE,
        RichTextFormat.STRIKETHROUGH,
        RichTextFormat.INLINE_CODE
    )

    val arbInlineFormat: Arb<RichTextFormat> = Arb.element(inlineFormats)

    val lineFormats = listOf(
        RichTextFormat.BULLET_LIST,
        RichTextFormat.ORDERED_LIST,
        RichTextFormat.BLOCKQUOTE
    )

    val arbLineFormat: Arb<RichTextFormat> = Arb.element(lineFormats)

    // ==================== Property 23 ====================
    // Feature: rich-text-formatting-parity, Property 23: Multiple compatible formats can coexist on same range

    /**
     * Property 23: For any text range, applying all of {BOLD, ITALIC, UNDERLINE,
     * STRIKETHROUGH} SHALL result in getFormatsInRange() returning all four formats
     * for that range.
     *
     * **Validates: Requirements 20.1**
     */
    "Property 23: multiple compatible formats can coexist on same range" {
        data class TestCase(val text: String, val spanStart: Int, val spanEnd: Int)

        val arbTestCase: Arb<TestCase> = arbitrary {
            val text = arbSafeText.bind()
            val spanStart = Arb.int(0, text.length - 2).bind()
            val spanEnd = Arb.int(spanStart + 1, text.length).bind()
            TestCase(text, spanStart, spanEnd)
        }

        checkAll(100, arbTestCase) { (text, spanStart, spanEnd) ->
            val manager = RichTextSpanManager()

            // Apply all four compatible formats to the same range
            for (format in compatibleFormats) {
                manager.addFormat(spanStart, spanEnd, format)
            }

            // Verify all four formats are present in the range
            val formatsInRange = manager.getFormatsInRange(spanStart, spanEnd)
            for (format in compatibleFormats) {
                formatsInRange.contains(format) shouldBe true
            }

            // Also verify at each individual position within the range
            for (pos in spanStart until spanEnd) {
                val formatsAtPos = manager.getFormatsAt(pos)
                for (format in compatibleFormats) {
                    formatsAtPos.contains(format) shouldBe true
                }
            }
        }
    }


    // ==================== Property 24 ====================
    // Feature: rich-text-formatting-parity, Property 24: Inline formats work inside line-based format contexts

    /**
     * Property 24: For any text line with a list or blockquote prefix, applying an
     * inline format (BOLD, ITALIC, etc.) to the content portion SHALL result in the
     * inline format being present in the span data for that range.
     *
     * **Validates: Requirements 20.2**
     */
    "Property 24: inline formats work inside line-based format contexts" {
        data class TestCase(
            val content: String,
            val lineFormat: RichTextFormat,
            val inlineFormat: RichTextFormat
        )

        val arbTestCase: Arb<TestCase> = arbitrary {
            val content = arbSafeText.bind()
            val lineFormat = arbLineFormat.bind()
            val inlineFormat = arbInlineFormat.bind()
            TestCase(content, lineFormat, inlineFormat)
        }

        checkAll(100, arbTestCase) { (content, lineFormat, inlineFormat) ->
            val controller = RichTextEditorController()

            // Build text with the line-based prefix
            val prefixedLine = when (lineFormat) {
                RichTextFormat.BULLET_LIST -> "- $content"
                RichTextFormat.ORDERED_LIST -> "1. $content"
                RichTextFormat.BLOCKQUOTE -> "> $content"
                else -> content
            }

            // Set up text
            controller.onTextChanged(prefixedLine, prefixedLine.length, prefixedLine.length)

            // Determine the content range (after the prefix)
            val prefixLen = when (lineFormat) {
                RichTextFormat.BULLET_LIST -> 2  // "- "
                RichTextFormat.ORDERED_LIST -> 3 // "1. "
                RichTextFormat.BLOCKQUOTE -> 2   // "> "
                else -> 0
            }
            val contentStart = prefixLen
            val contentEnd = prefixedLine.length

            // Apply inline format to the content portion
            controller.state.spanManager.addFormat(contentStart, contentEnd, inlineFormat)

            // Verify the inline format is present in the span data for that range
            val formatsInRange = controller.state.spanManager.getFormatsInRange(contentStart, contentEnd)
            formatsInRange.contains(inlineFormat) shouldBe true

            // Also verify at each position within the content range
            for (pos in contentStart until contentEnd) {
                val formatsAtPos = controller.state.spanManager.getFormatsAt(pos)
                formatsAtPos.contains(inlineFormat) shouldBe true
            }
        }
    }

    // ==================== Property 25 ====================
    // Feature: rich-text-formatting-parity, Property 25: Nested formats serialize with proper marker nesting

    /**
     * Property 25: For any text with nested inline formats inside a list item
     * (e.g., bold text in a bullet item), toMarkdown() SHALL emit the inline
     * markdown markers inside the list prefix (e.g., "- **bold item**").
     *
     * **Validates: Requirements 24.5**
     */
    "Property 25: nested formats serialize with proper marker nesting" {
        data class TestCase(
            val content: String,
            val lineFormat: RichTextFormat,
            val inlineFormat: RichTextFormat
        )

        val arbTestCase: Arb<TestCase> = arbitrary {
            val content = arbSafeText.bind()
            val lineFormat = arbLineFormat.bind()
            // Use only formats with clear markdown markers (not INLINE_CODE which
            // could conflict with backtick parsing in edge cases)
            val inlineFormat = Arb.element(
                listOf(
                    RichTextFormat.BOLD,
                    RichTextFormat.ITALIC,
                    RichTextFormat.UNDERLINE,
                    RichTextFormat.STRIKETHROUGH
                )
            ).bind()
            TestCase(content, lineFormat, inlineFormat)
        }

        checkAll(100, arbTestCase) { (content, lineFormat, inlineFormat) ->
            val controller = RichTextEditorController()

            // Build text with the line-based prefix
            val prefixedLine = when (lineFormat) {
                RichTextFormat.BULLET_LIST -> "- $content"
                RichTextFormat.ORDERED_LIST -> "1. $content"
                RichTextFormat.BLOCKQUOTE -> "> $content"
                else -> content
            }

            // Set up text
            controller.onTextChanged(prefixedLine, prefixedLine.length, prefixedLine.length)

            // Determine the content range (after the prefix)
            val prefixLen = when (lineFormat) {
                RichTextFormat.BULLET_LIST -> 2  // "- "
                RichTextFormat.ORDERED_LIST -> 3 // "1. "
                RichTextFormat.BLOCKQUOTE -> 2   // "> "
                else -> 0
            }
            val contentStart = prefixLen
            val contentEnd = prefixedLine.length

            // Apply inline format to the content portion
            controller.state.spanManager.addFormat(contentStart, contentEnd, inlineFormat)

            // Serialize to markdown
            val markdown = controller.toMarkdown()

            // Determine expected markers
            val openMarker = when (inlineFormat) {
                RichTextFormat.BOLD -> "**"
                RichTextFormat.ITALIC -> "_"
                RichTextFormat.UNDERLINE -> "<u>"
                RichTextFormat.STRIKETHROUGH -> "~~"
                else -> ""
            }
            val closeMarker = when (inlineFormat) {
                RichTextFormat.BOLD -> "**"
                RichTextFormat.ITALIC -> "_"
                RichTextFormat.UNDERLINE -> "</u>"
                RichTextFormat.STRIKETHROUGH -> "~~"
                else -> ""
            }

            // Determine expected prefix in markdown
            val expectedPrefix = when (lineFormat) {
                RichTextFormat.BULLET_LIST -> "- "
                RichTextFormat.ORDERED_LIST -> "1. "
                RichTextFormat.BLOCKQUOTE -> "> "
                else -> ""
            }

            // The markdown should have the prefix followed by the inline markers wrapping the content
            // e.g., "- **bold item**" or "> _italic text_"
            val expectedMarkdown = "$expectedPrefix$openMarker$content$closeMarker"
            markdown shouldBe expectedMarkdown
        }
    }
})
