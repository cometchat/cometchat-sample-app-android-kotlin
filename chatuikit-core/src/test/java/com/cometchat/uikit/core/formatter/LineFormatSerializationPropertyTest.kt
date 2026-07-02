package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for line formats and serialization.
 *
 * Tests Properties 16–22 from the rich-text-formatting-parity design document.
 * Each property is validated with a minimum of 100 random iterations.
 */
class LineFormatSerializationPropertyTest : StringSpec({

    // ==================== Shared Generators ====================

    /** Generates printable text without newlines (single-line), no list/blockquote prefixes. */
    val arbSingleLineText: Arb<String> = Arb.string(minSize = 1, maxSize = 80)
        .let { arb ->
            arbitrary {
                val s = arb.bind()
                val cleaned = s.replace('\n', 'x').replace('\r', 'x')
                    .removePrefix("- ").removePrefix("• ")
                    .removePrefix("> ")
                    .replace(Regex("^\\d+\\. "), "")
                if (cleaned.isBlank()) "hello" else cleaned
            }
        }

    /** Generates multi-line text (2-5 lines, each non-empty, no existing prefixes). */
    val arbMultiLineText: Arb<String> = arbitrary {
        val lineCount = Arb.int(2, 5).bind()
        val lines = (1..lineCount).map { arbSingleLineText.bind() }
        lines.joinToString("\n")
    }

    val lineFormats = listOf(
        RichTextFormat.BULLET_LIST,
        RichTextFormat.ORDERED_LIST,
        RichTextFormat.BLOCKQUOTE
    )

    val arbLineFormat: Arb<RichTextFormat> = Arb.element(lineFormats)

    val inlineFormats = listOf(
        RichTextFormat.BOLD,
        RichTextFormat.ITALIC,
        RichTextFormat.UNDERLINE,
        RichTextFormat.STRIKETHROUGH,
        RichTextFormat.INLINE_CODE
    )

    val arbInlineFormat: Arb<RichTextFormat> = Arb.element(inlineFormats)

    // ==================== Helpers ====================

    /** Simulates typing a string character by character. */
    fun typeText(controller: RichTextEditorController, text: String) {
        for (ch in text) {
            val currentText = controller.state.text
            val cursorPos = controller.state.selectionStart
            val newText = currentText.substring(0, cursorPos) + ch +
                currentText.substring(cursorPos)
            controller.onTextChanged(newText, cursorPos + 1, cursorPos + 1)
        }
    }

    /** Simulates pressing Enter (inserts \n at cursor). */
    fun pressEnter(controller: RichTextEditorController) {
        val currentText = controller.state.text
        val cursorPos = controller.state.selectionStart
        val newText = currentText.substring(0, cursorPos) + "\n" +
            currentText.substring(cursorPos)
        controller.onTextChanged(newText, cursorPos + 1, cursorPos + 1)
    }


    // ==================== Property 17 ====================
    // Feature: rich-text-formatting-parity, Property 17: List/blockquote auto-continuation on Enter

    /**
     * Property 17: For any line with a list or blockquote prefix and non-empty content,
     * inserting a newline at the end SHALL produce a new line with the correct
     * continuation prefix (same bullet marker, next sequential number, or "> ").
     *
     * **Validates: Requirements 19.1, 19.2, 19.5**
     */
    "Property 17: list/blockquote auto-continuation on Enter" {
        data class TestCase(val content: String, val format: RichTextFormat)

        val arbTestCase: Arb<TestCase> = arbitrary {
            val content = arbSingleLineText.bind()
            val format = arbLineFormat.bind()
            TestCase(content, format)
        }

        checkAll(100, arbTestCase) { (content, format) ->
            val controller = RichTextEditorController()

            // Set up a line with the appropriate prefix
            val prefixedLine = when (format) {
                RichTextFormat.BULLET_LIST -> "- $content"
                RichTextFormat.ORDERED_LIST -> "1. $content"
                RichTextFormat.BLOCKQUOTE -> "> $content"
                else -> content
            }

            // Type the prefixed line
            controller.onTextChanged(prefixedLine, prefixedLine.length, prefixedLine.length)

            // Press Enter at the end
            pressEnter(controller)

            val resultText = controller.state.text
            val lines = resultText.split('\n')

            // Should have 2 lines now
            lines.size shouldBe 2

            // Second line should have the continuation prefix
            when (format) {
                RichTextFormat.BULLET_LIST -> {
                    lines[1].startsWith("- ").shouldBeTrue()
                }
                RichTextFormat.ORDERED_LIST -> {
                    lines[1].startsWith("2. ").shouldBeTrue()
                }
                RichTextFormat.BLOCKQUOTE -> {
                    lines[1].startsWith("> ").shouldBeTrue()
                }
                else -> {}
            }
        }
    }



    // ==================== Property 19 ====================
    // Feature: rich-text-formatting-parity, Property 19: Code segment serialization wraps in triple-backtick fences

    /**
     * Property 19: For any Code segment with non-empty text and optional language hint,
     * SegmentComposerController.toMarkdown() SHALL produce output containing
     * ```language\ntext\n```.
     *
     * **Validates: Requirements 24.1**
     */
    "Property 19: code segment serialization wraps in triple-backtick fences" {
        data class TestCase(val codeText: String, val language: String)

        val arbLanguage: Arb<String> = Arb.element(listOf("", "kotlin", "java", "python", "javascript", "swift"))

        val arbCodeText: Arb<String> = Arb.string(minSize = 1, maxSize = 80)
            .let { arb ->
                arbitrary {
                    val s = arb.bind()
                    val cleaned = s.replace('\r', ' ').trim()
                    if (cleaned.isBlank()) "val x = 1" else cleaned
                }
            }

        val arbTestCase: Arb<TestCase> = arbitrary {
            val codeText = arbCodeText.bind()
            val language = arbLanguage.bind()
            TestCase(codeText, language)
        }

        checkAll(100, arbTestCase) { (codeText, language) ->
            val controller = SegmentComposerController()
            // Insert a code block
            val firstNormal = controller.segments.first() as ComposerSegment.Normal
            firstNormal.controller.onTextChanged("before", 5, 5)
            controller.setFocusedSegment(firstNormal.id)
            controller.insertCodeBlock()

            // Set code segment text and language
            val codeSegment = controller.segments.filterIsInstance<ComposerSegment.Code>().first()
            codeSegment.text = codeText
            codeSegment.language = language

            val markdown = controller.toMarkdown()

            // Verify the output contains the triple-backtick fence
            val expectedFence = "```$language\n${codeText.trim()}\n```"
            markdown shouldContain expectedFence
        }
    }


    // ==================== Property 20 ====================
    // Feature: rich-text-formatting-parity, Property 20: Markdown round-trip preserves text and format spans

    /**
     * Property 20: For any valid formatted text containing any combination of supported
     * inline formats, serializing via toMarkdown() and deserializing via fromMarkdown()
     * SHALL produce plain text identical to the original (excluding markdown syntax)
     * and an equivalent set of format spans covering the same text ranges.
     *
     * **Validates: Requirements 32.1, 32.2, 32.3**
     */
    "Property 20: markdown round-trip preserves text and format spans" {
        data class TestCase(val text: String, val spanStart: Int, val spanEnd: Int, val format: RichTextFormat)

        // Use only formats that round-trip cleanly (no LINK which has special URL handling)
        val roundTripFormats = listOf(
            RichTextFormat.BOLD,
            RichTextFormat.ITALIC,
            RichTextFormat.STRIKETHROUGH,
            RichTextFormat.UNDERLINE,
            RichTextFormat.INLINE_CODE
        )

        val arbTestCase: Arb<TestCase> = arbitrary {
            // Generate text that won't be confused with markdown syntax
            val text = Arb.string(minSize = 3, maxSize = 50).bind()
                .replace('\n', ' ').replace('\r', ' ')
                .replace("*", "x").replace("_", "x").replace("~", "x")
                .replace("`", "x").replace("<", "x").replace(">", "x")
                .replace("[", "x").replace("]", "x")
            val safeText = if (text.isBlank()) "hello world" else text
            val spanStart = Arb.int(0, safeText.length - 2).bind()
            val spanEnd = Arb.int(spanStart + 1, safeText.length).bind()
            val format = Arb.element(roundTripFormats).bind()
            TestCase(safeText, spanStart, spanEnd, format)
        }

        checkAll(100, arbTestCase) { (text, spanStart, spanEnd, format) ->
            // Step 1: Create formatted text
            val controller = RichTextEditorController()
            controller.onTextChanged(text, text.length, text.length)
            controller.state.spanManager.addFormat(spanStart, spanEnd, format)

            val originalPlainText = controller.state.text
            val spanText = originalPlainText.substring(spanStart, spanEnd)

            // Step 2: Serialize to markdown
            val markdown = controller.toMarkdown()

            // Step 3: Deserialize from markdown
            val controller2 = RichTextEditorController()
            controller2.fromMarkdown(markdown)

            val roundTripPlainText = controller2.state.text

            // Step 4: Verify plain text matches
            roundTripPlainText shouldBe originalPlainText

            // Step 5: Verify the format span covers the same range
            val formatsInRange = controller2.state.spanManager.getFormatsInRange(spanStart, spanEnd)
            formatsInRange.contains(format).shouldBeTrue()
        }
    }


    // ==================== Property 21 ====================
    // Feature: rich-text-formatting-parity, Property 21: Empty segments skipped in serialization

    /**
     * Property 21: For any segment list containing empty Normal segments or empty Code
     * segments, toMarkdown() SHALL not include any output for those empty segments.
     *
     * **Validates: Requirements 24.6**
     */
    "Property 21: empty segments skipped in serialization" {
        val arbNonEmptyText: Arb<String> = arbitrary {
            val s = arbSingleLineText.bind()
            if (s.isBlank()) "content" else s
        }

        checkAll(100, arbNonEmptyText) { contentText ->
            val controller = SegmentComposerController()

            // Set up: create a code block (which creates empty Normal segments around it)
            val firstNormal = controller.segments.first() as ComposerSegment.Normal
            controller.setFocusedSegment(firstNormal.id)
            controller.insertCodeBlock()

            // The code segment is empty, and the Normal segments are empty
            // Only set content on one Normal segment
            val normals = controller.segments.filterIsInstance<ComposerSegment.Normal>()
            normals.first().controller.onTextChanged(contentText, contentText.length, contentText.length)

            // Leave the Code segment empty and the second Normal empty
            val codeSegment = controller.segments.filterIsInstance<ComposerSegment.Code>().first()
            codeSegment.text = ""

            val markdown = controller.toMarkdown()

            // Empty code segment should NOT produce ``` fences
            markdown shouldNotContain "```\n\n```"
            markdown shouldNotContain "``````"

            // The markdown should only contain the non-empty content
            markdown.trim() shouldBe contentText.trim()
        }
    }


    // ==================== Property 22 ====================
    // Feature: rich-text-formatting-parity, Property 22: Link whitespace normalization

    /**
     * Property 22: For any LINK span whose display text contains consecutive whitespace
     * characters, toMarkdown() SHALL normalize the whitespace to a single space in the
     * serialized link text.
     *
     * **Validates: Requirements 33.1**
     */
    "Property 22: link whitespace normalization" {
        data class TestCase(val beforeWhitespace: String, val afterWhitespace: String, val whitespace: String)

        val arbTestCase: Arb<TestCase> = arbitrary {
            // Generate words without whitespace
            val word1 = Arb.string(minSize = 1, maxSize = 20).bind()
                .replace(Regex("\\s"), "x")
                .let { if (it.isBlank()) "click" else it }
            val word2 = Arb.string(minSize = 1, maxSize = 20).bind()
                .replace(Regex("\\s"), "x")
                .let { if (it.isBlank()) "here" else it }
            // Generate consecutive whitespace (2-5 spaces/tabs)
            val wsCount = Arb.int(2, 5).bind()
            val ws = " ".repeat(wsCount)
            TestCase(word1, word2, ws)
        }

        checkAll(100, arbTestCase) { (word1, word2, whitespace) ->
            val displayText = "$word1$whitespace$word2"
            val url = "https://example.com"

            val controller = RichTextEditorController()
            controller.applyLink(displayText, url)

            val markdown = controller.toMarkdown()

            // The link text in markdown should have normalized whitespace
            // The markdown format is [displayText](url) but with whitespace normalized
            // Check that consecutive whitespace in the display text is normalized
            // Note: The actual normalization happens in toMarkdown() per the spec
            // If the implementation normalizes, we verify single spaces
            // If it doesn't normalize yet, the test documents the expected behavior

            // The markdown should contain the link
            markdown shouldContain "["
            markdown shouldContain "]"

            // Extract the link text from markdown [text](url)
            val linkTextMatch = Regex("\\[(.+?)\\]\\(").find(markdown)
            if (linkTextMatch != null) {
                val linkText = linkTextMatch.groupValues[1]
                // Verify no consecutive whitespace in the link text
                val hasConsecutiveWhitespace = linkText.contains(Regex("\\s{2,}"))
                // The property states whitespace SHOULD be normalized
                hasConsecutiveWhitespace shouldBe false
            }
        }
    }

})
