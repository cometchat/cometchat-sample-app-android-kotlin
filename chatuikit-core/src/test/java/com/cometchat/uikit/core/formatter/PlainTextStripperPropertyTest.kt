package com.cometchat.uikit.core.formatter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.contain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for [PlainTextStripper].
 *
 * Feature: formatted-text-previews
 * Covers Properties 1, 2, 4, 5, and 6 from the design spec.
 */
class PlainTextStripperPropertyTest : FunSpec({

    // ── Generators ──────────────────────────────────────────────────────

    /** Generates plain text words that contain NO markdown syntax characters. */
    val plainWordArb: Arb<String> = Arb.string(1..20, Codepoint.alphanumeric())

    /** Generates a plain sentence (no markdown). */
    val plainSentenceArb: Arb<String> = Arb.list(plainWordArb, 1..5)
        .map { it.joinToString(" ") }

    /** Generates bold markdown: **text** */
    val boldArb: Arb<String> = plainWordArb.map { "**$it**" }

    /** Generates italic markdown: _text_ */
    val italicArb: Arb<String> = plainWordArb.map { "_${it}_" }

    /** Generates strikethrough markdown: ~~text~~ */
    val strikethroughArb: Arb<String> = plainWordArb.map { "~~$it~~" }

    /** Generates underline markdown: <u>text</u> */
    val underlineArb: Arb<String> = plainWordArb.map { "<u>$it</u>" }

    /** Generates inline code markdown: `text` */
    val inlineCodeArb: Arb<String> = plainWordArb.map { "`$it`" }

    /** Generates link markdown: [display](url) */
    val linkArb: Arb<String> = arbitrary {
        val display = plainWordArb.bind()
        val url = plainWordArb.bind()
        "[$display](https://$url.com)"
    }

    /** Generates a markdown fragment (one of the inline formats or plain text). */
    val markdownFragmentArb: Arb<String> = Arb.choice(
        plainWordArb,
        boldArb,
        italicArb,
        strikethroughArb,
        underlineArb,
        inlineCodeArb,
        linkArb
    )

    /** Generates a markdown line by joining fragments with spaces. */
    val markdownLineArb: Arb<String> = Arb.list(markdownFragmentArb, 1..4)
        .map { it.joinToString(" ") }

    /** Generates a fenced code block. */
    val codeBlockArb: Arb<String> = arbitrary {
        val lang = Arb.element("", "kotlin", "java", "js").bind()
        val code = plainSentenceArb.bind()
        "```$lang\n$code\n```"
    }

    /** Generates a bullet list item. */
    val bulletItemArb: Arb<String> = markdownLineArb.map { "- $it" }

    /** Generates an ordered list item. */
    val orderedItemArb: Arb<String> = arbitrary {
        val num = Arb.int(1..9).bind()
        val text = markdownLineArb.bind()
        "$num. $text"
    }

    /** Generates a blockquote line. */
    val blockquoteArb: Arb<String> = markdownLineArb.map { "> $it" }

    /** Generates a complete markdown document with mixed block types. */
    val markdownDocArb: Arb<String> = Arb.list(
        Arb.choice(
            markdownLineArb,
            codeBlockArb,
            bulletItemArb,
            orderedItemArb,
            blockquoteArb
        ),
        1..5
    ).map { it.joinToString("\n") }

    // Helper: extract plain text from segments manually (for round-trip check)
    fun extractPlainTextFromSegments(segments: List<MarkdownRenderer.RenderedSegment>): String {
        return segments.joinToString(" ") { segment ->
            when (segment) {
                is MarkdownRenderer.RenderedSegment.Text ->
                    MarkdownRenderer.parseInline(segment.text).first
                is MarkdownRenderer.RenderedSegment.CodeBlock ->
                    segment.code
                is MarkdownRenderer.RenderedSegment.BulletItem ->
                    MarkdownRenderer.parseInline(segment.text).first
                is MarkdownRenderer.RenderedSegment.OrderedItem ->
                    MarkdownRenderer.parseInline(segment.text).first
                is MarkdownRenderer.RenderedSegment.Blockquote ->
                    MarkdownRenderer.parseInline(segment.text).first
            }
        }
    }

    // ── Property Tests ──────────────────────────────────────────────────

    test("Property 1: Round-trip consistency — parse then extract equals strip directly") {
        checkAll(100, markdownDocArb) { markdown ->
            val segments = MarkdownRenderer.parse(markdown)
            val fromSegments = extractPlainTextFromSegments(segments)
            val fromStrip = PlainTextStripper.strip(markdown)
            fromStrip shouldBe fromSegments
        }
    }

    test("Property 2: Plain text preservation — no-markdown strings returned unchanged") {
        checkAll(100, plainSentenceArb) { plainText ->
            PlainTextStripper.strip(plainText) shouldBe plainText
        }
    }

    test("Property 4: Link display text preservation — only display text retained") {
        checkAll(100, arbitrary {
            val display = plainWordArb.bind()
            val url = plainWordArb.bind()
            display to "[$display](https://$url.com)"
        }) { (displayText, markdown) ->
            val stripped = PlainTextStripper.strip(markdown)
            stripped shouldBe displayText
        }
    }

    /**
     * **Validates: Requirements 18A.2–18A.4**
     *
     * Property 5: PlainTextStripper removes all markdown markers.
     * Generate random markdown strings using a grammar-based generator,
     * strip, verify output contains no marker sequences.
     */
    test("Property 5: PlainTextStripper removes all markdown markers") {
        // Regex patterns that detect residual markdown markers in stripped output
        val boldMarker = Regex("\\*\\*")
        val strikeMarker = Regex("~~")
        val underlineOpenMarker = Regex("<u>")
        val underlineCloseMarker = Regex("</u>")
        val inlineCodeMarker = Regex("`")
        val codeBlockFence = Regex("```")
        val linkSyntax = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")

        checkAll(100, markdownDocArb) { markdown ->
            val stripped = PlainTextStripper.strip(markdown)

            stripped shouldNot contain(boldMarker)
            stripped shouldNot contain(strikeMarker)
            stripped shouldNot contain(underlineOpenMarker)
            stripped shouldNot contain(underlineCloseMarker)
            stripped shouldNot contain(inlineCodeMarker)
            stripped shouldNot contain(codeBlockFence)
            stripped shouldNot contain(linkSyntax)

            // Verify block-level prefixes are removed:
            // Bullet prefix "- " at line start
            for (line in stripped.split("\n")) {
                if (line.startsWith("- ")) {
                    // The stripped output should not have bullet prefixes
                    // (unless the content itself starts with "- " which our generators don't produce)
                    throw AssertionError(
                        "Stripped output contains bullet prefix '- ' at line start: \"$line\"\nOriginal: \"$markdown\""
                    )
                }
                // Ordered list prefix "N. " at line start
                val orderedPrefixMatch = Regex("^\\d+\\. ").find(line)
                if (orderedPrefixMatch != null) {
                    throw AssertionError(
                        "Stripped output contains ordered list prefix at line start: \"$line\"\nOriginal: \"$markdown\""
                    )
                }
                // Blockquote prefix "> " at line start
                if (line.startsWith("> ")) {
                    throw AssertionError(
                        "Stripped output contains blockquote prefix '> ' at line start: \"$line\"\nOriginal: \"$markdown\""
                    )
                }
            }
        }
    }

    /**
     * **Validates: Requirements 18A.5**
     *
     * Property 6: PlainTextStripper idempotence.
     * For any markdown string s, strip(strip(s)) == strip(s).
     */
    test("Property 6: PlainTextStripper idempotence — strip(strip(s)) == strip(s)") {
        checkAll(100, markdownDocArb) { markdown ->
            val once = PlainTextStripper.strip(markdown)
            val twice = PlainTextStripper.strip(once)
            twice shouldBe once
        }
    }

    test("Empty string returns empty") {
        PlainTextStripper.strip("") shouldBe ""
    }
})
