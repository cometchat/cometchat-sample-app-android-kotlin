package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for [SegmentComposerController].
 *
 * Uses kotest property testing with Arb generators to verify universal
 * correctness properties across randomly generated inputs.
 */
class SegmentComposerControllerPropertyTest : FunSpec({

    // ==================== Helpers ====================

    /**
     * Builds a controller with a random number of code blocks inserted (0..maxCodeBlocks),
     * and sets random text on Normal segments. Returns the controller.
     */
    fun buildRandomController(numCodeBlocks: Int, normalTexts: List<String>): SegmentComposerController {
        val controller = SegmentComposerController()

        // Insert code blocks one at a time, focusing the last Normal segment each time
        repeat(numCodeBlocks) {
            val lastNormal = controller.segments.filterIsInstance<ComposerSegment.Normal>().lastOrNull()
            if (lastNormal != null) {
                controller.setFocusedSegment(lastNormal.id)
                controller.insertCodeBlock()
            }
        }

        // Set text on Normal segments using the provided texts
        val normals = controller.segments.filterIsInstance<ComposerSegment.Normal>()
        for ((i, normal) in normals.withIndex()) {
            val text = normalTexts.getOrElse(i) { "" }
            if (text.isNotEmpty()) {
                normal.controller.onTextChanged(text, text.length, text.length)
            }
        }

        return controller
    }

    // ==================== Property 1: activeFormats delegation ====================

    /**
     * **Validates: Requirements 2.3, 5.1**
     *
     * Property 1: Toolbar activeFormats reflects focused segment.
     * For any segment configuration, focusing each segment should make activeFormats return:
     * - Normal → that segment's controller.state.activeFormats
     * - Code → {CODE_BLOCK}
     */
    test("Property 1: activeFormats reflects focused segment for random configurations") {
        checkAll(100, Arb.int(0..3), Arb.string(0..20), Arb.string(0..20), Arb.string(0..20), Arb.string(0..20)) {
            numCodeBlocks, text1, text2, text3, text4 ->

            val normalTexts = listOf(text1, text2, text3, text4)
            val controller = buildRandomController(numCodeBlocks, normalTexts)

            for (segment in controller.segments) {
                controller.setFocusedSegment(segment.id)

                when (segment) {
                    is ComposerSegment.Normal -> {
                        controller.activeFormats shouldBe segment.controller.state.activeFormats
                    }
                    is ComposerSegment.Code -> {
                        controller.activeFormats shouldBe setOf(RichTextFormat.CODE_BLOCK)
                    }
                }
            }
        }
    }

    // ==================== Property 2: disabledFormats delegation ====================

    /**
     * **Validates: Requirements 2.4, 5.2**
     *
     * Property 2: Toolbar disabledFormats reflects focused segment.
     * For any segment configuration, focusing each segment should make toolbarDisabledFormats return:
     * - Code → all RichTextFormat entries except CODE_BLOCK
     * - Normal → that segment's controller.state.toolbarDisabledFormats
     */
    test("Property 2: toolbarDisabledFormats reflects focused segment for random configurations") {
        // When in code block: inline formats disabled, but line formats + CODE_BLOCK stay enabled
        val codeBlockDisabledFormats = setOf(
            RichTextFormat.BOLD,
            RichTextFormat.ITALIC,
            RichTextFormat.UNDERLINE,
            RichTextFormat.STRIKETHROUGH,
            RichTextFormat.INLINE_CODE,
            RichTextFormat.LINK
        )

        checkAll(100, Arb.int(0..3), Arb.string(0..20), Arb.string(0..20), Arb.string(0..20), Arb.string(0..20)) {
            numCodeBlocks, text1, text2, text3, text4 ->

            val normalTexts = listOf(text1, text2, text3, text4)
            val controller = buildRandomController(numCodeBlocks, normalTexts)

            for (segment in controller.segments) {
                controller.setFocusedSegment(segment.id)

                when (segment) {
                    is ComposerSegment.Code -> {
                        controller.toolbarDisabledFormats shouldBe codeBlockDisabledFormats
                    }
                    is ComposerSegment.Normal -> {
                        controller.toolbarDisabledFormats shouldBe segment.controller.state.toolbarDisabledFormats
                    }
                }
            }
        }
    }

    // ==================== Property 3: Markdown round-trip ====================

    /**
     * **Validates: Requirements 6.2, 10.1**
     *
     * Property 3: Markdown serialization round-trip preserves content.
     * Generate random segment lists with text, serialize to markdown, parse back,
     * verify text content matches.
     */
    test("Property 3: markdown round-trip preserves content for random segments") {
        checkAll(100, Arb.int(0..3), Arb.string(1..30), Arb.string(1..30), Arb.string(1..30)) {
            numCodeBlocks, normalText, codeText1, codeText2 ->

            // Filter out strings that contain markdown-significant characters
            // to avoid interference with parsing (backticks, newlines)
            val safeNormalText = normalText.filter { it != '`' && it != '\n' && it != '\r' }
            val safeCodeText1 = codeText1.filter { it != '`' && it != '\n' && it != '\r' }
            val safeCodeText2 = codeText2.filter { it != '`' && it != '\n' && it != '\r' }

            if (safeNormalText.isNotBlank() || safeCodeText1.isNotBlank()) {
                val controller = SegmentComposerController()

                // Set text on the first Normal segment
                if (safeNormalText.isNotBlank()) {
                    val firstNormal = controller.segments.first() as ComposerSegment.Normal
                    firstNormal.controller.onTextChanged(safeNormalText, safeNormalText.length, safeNormalText.length)
                    controller.setFocusedSegment(firstNormal.id)
                }

                // Insert code blocks with text
                val codeTexts = listOf(safeCodeText1, safeCodeText2).take(numCodeBlocks.coerceAtMost(2))
                for (codeText in codeTexts) {
                    val lastNormal = controller.segments.filterIsInstance<ComposerSegment.Normal>().lastOrNull()
                    if (lastNormal != null) {
                        controller.setFocusedSegment(lastNormal.id)
                        controller.insertCodeBlock()
                        // Set text on the newly created code segment
                        val codeSegments = controller.segments.filterIsInstance<ComposerSegment.Code>()
                        val latestCode = codeSegments.lastOrNull()
                        if (latestCode != null && codeText.isNotBlank()) {
                            latestCode.text = codeText
                        }
                    }
                }

                val markdown = controller.toMarkdown()

                // Parse the markdown back: split by fenced code blocks
                val parts = mutableListOf<Pair<String, String>>() // (type, content)
                val codeBlockRegex = Regex("```(\\w*)\\n(.*?)\\n```", RegexOption.DOT_MATCHES_ALL)
                var lastEnd = 0
                for (match in codeBlockRegex.findAll(markdown)) {
                    val beforeCode = markdown.substring(lastEnd, match.range.first).trim()
                    if (beforeCode.isNotEmpty()) {
                        parts.add("normal" to beforeCode)
                    }
                    parts.add("code" to match.groupValues[2].trim())
                    lastEnd = match.range.last + 1
                }
                val trailing = markdown.substring(lastEnd).trim()
                if (trailing.isNotEmpty()) {
                    parts.add("normal" to trailing)
                }

                // Verify: each original non-empty segment's text content appears in parsed parts
                val originalNormalTexts = controller.segments
                    .filterIsInstance<ComposerSegment.Normal>()
                    .map { it.controller.toMarkdown().trim() }
                    .filter { it.isNotEmpty() }

                val originalCodeTexts = controller.segments
                    .filterIsInstance<ComposerSegment.Code>()
                    .map { it.text.trim() }
                    .filter { it.isNotEmpty() }

                val parsedNormalTexts = parts.filter { it.first == "normal" }.map { it.second }
                val parsedCodeTexts = parts.filter { it.first == "code" }.map { it.second }

                parsedNormalTexts shouldBe originalNormalTexts
                parsedCodeTexts shouldBe originalCodeTexts
            }
        }
    }

    // ==================== Property 4: hasContent correctness ====================

    /**
     * **Validates: Requirements 6.4**
     *
     * Property 4: hasContent correctly reflects segment state.
     * For any segment combination, hasContent returns true iff at least one Normal
     * has non-blank text OR at least one Code segment exists.
     */
    test("Property 4: hasContent matches expected value for random segment configurations") {
        checkAll(100, Arb.int(0..3), Arb.string(0..20), Arb.string(0..20), Arb.string(0..20), Arb.string(0..20)) {
            numCodeBlocks, text1, text2, text3, text4 ->

            val normalTexts = listOf(text1, text2, text3, text4)
            val controller = buildRandomController(numCodeBlocks, normalTexts)

            val hasAnyNormalWithText = controller.segments
                .filterIsInstance<ComposerSegment.Normal>()
                .any { it.controller.state.text.isNotBlank() }

            val hasAnyCodeSegment = controller.segments
                .any { it is ComposerSegment.Code }

            val expected = hasAnyNormalWithText || hasAnyCodeSegment

            controller.hasContent shouldBe expected
        }
    }

    // ==================== Property 5: clear() invariant ====================

    /**
     * **Validates: Requirements 6.3, 10.3**
     *
     * Property 5: clear() resets to single empty Normal segment.
     * For any controller state, calling clear() results in exactly one Normal segment
     * with empty text, and focusedSegmentId and pendingFocusSegmentId both point to it.
     */
    test("Property 5: clear() always resets to single empty Normal with correct focus") {
        checkAll(100, Arb.int(0..3), Arb.string(0..20), Arb.string(0..20), Arb.string(0..20), Arb.string(0..20)) {
            numCodeBlocks, text1, text2, text3, text4 ->

            val normalTexts = listOf(text1, text2, text3, text4)
            val controller = buildRandomController(numCodeBlocks, normalTexts)

            // Call clear
            controller.clear()

            // Verify exactly one segment
            controller.segments shouldHaveSize 1

            // Verify it's a Normal segment with empty text
            val onlySegment = controller.segments.first()
            onlySegment.shouldBeInstanceOf<ComposerSegment.Normal>()
            (onlySegment as ComposerSegment.Normal).controller.state.text shouldBe ""

            // Verify focus IDs point to the single segment
            controller.focusedSegmentId shouldBe onlySegment.id
            controller.pendingFocusSegmentId shouldBe onlySegment.id
        }
    }
})
