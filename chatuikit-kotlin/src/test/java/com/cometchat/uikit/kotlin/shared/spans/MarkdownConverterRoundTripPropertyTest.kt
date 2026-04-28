package com.cometchat.uikit.kotlin.shared.spans

import android.text.SpannableStringBuilder
import com.cometchat.uikit.core.formatter.RichTextFormat
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Property-based tests for [MarkdownConverter] round-trip preservation.
 *
 * Feature: v6-rich-text-composer, Property 4: Markdown round-trip preservation
 *
 * Generates random Editables with random combinations of inline format spans,
 * converts to markdown via [MarkdownConverter.toMarkdown], parses back via
 * [MarkdownConverter.fromMarkdown], and verifies that:
 * (a) plain text content is identical
 * (b) format spans cover the same text ranges
 *
 * **Validates: Requirements 9.1–9.12**
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MarkdownConverterRoundTripPropertyTest {

    /**
     * Inline format types tested for round-trip. These are the formats that
     * wrap character ranges with open/close markdown delimiters.
     *
     * INLINE_CODE is excluded because MarkdownRenderer's backtick regex
     * (`[^`]+`) does not support nested inline formats inside code spans,
     * and code content may conflict with other marker characters.
     *
     * LINK is excluded because it requires URL metadata that complicates
     * generation without adding round-trip coverage value.
     */
    private val inlineFormats = listOf(
        RichTextFormat.BOLD,
        RichTextFormat.ITALIC,
        RichTextFormat.UNDERLINE,
        RichTextFormat.STRIKETHROUGH
    )

    private val arbFormat: Arb<RichTextFormat> = Arb.element(inlineFormats)

    /**
     * Characters safe for round-trip testing. Excludes markdown-special characters
     * (*, _, ~, <, >, `, [, ], (, ), -, #, ., !) and newlines to keep the test
     * focused on span round-trip rather than markdown parsing edge cases.
     */
    private val safeChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf(' ')

    /** Non-space safe characters used to guarantee at least one visible char. */
    private val safeNonSpaceChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    /**
     * Generates a random string composed only of safe (non-markdown) characters.
     * Guarantees at least one non-space character so that MarkdownRenderer.parse()
     * does not treat the string as blank.
     * Minimum length 1, maximum length 60.
     */
    private val arbSafeText: Arb<String> = arbitrary {
        val len = Arb.int(1..60).bind()
        val result = buildString {
            repeat(len) {
                append(safeChars[Arb.int(safeChars.indices).bind()])
            }
        }
        // Ensure at least one non-space character
        if (result.isBlank()) {
            val pos = Arb.int(result.indices).bind()
            val ch = safeNonSpaceChars[Arb.int(safeNonSpaceChars.indices).bind()]
            result.substring(0, pos) + ch + result.substring(pos + 1)
        } else {
            result
        }
    }

    /**
     * Describes a format span to apply to the Editable before round-tripping.
     */
    data class SpanSpec(
        val start: Int,
        val end: Int,
        val format: RichTextFormat
    )

    /**
     * Full test case: text with non-overlapping format spans.
     */
    data class RoundTripTestCase(
        val text: String,
        val spans: List<SpanSpec>
    )

    /**
     * Generates non-overlapping span specs for a given text length.
     * Spans are placed sequentially so they never overlap, which avoids
     * ambiguity in the round-trip comparison.
     */
    private fun arbNonOverlappingSpans(textLen: Int): Arb<List<SpanSpec>> = arbitrary {
        if (textLen < 2) return@arbitrary emptyList()

        val spanCount = Arb.int(0..minOf(4, textLen / 2)).bind()
        if (spanCount == 0) return@arbitrary emptyList()

        val specs = mutableListOf<SpanSpec>()
        var cursor = 0

        repeat(spanCount) {
            val remaining = textLen - cursor
            if (remaining < 2) return@repeat

            // Leave room for at least 1-char span
            val start = Arb.int(cursor..minOf(cursor + remaining / 2, textLen - 2)).bind()
            val maxEnd = minOf(start + 20, textLen) // cap span length
            if (start + 1 > maxEnd) return@repeat
            val end = Arb.int(start + 1..maxEnd).bind()
            val format = arbFormat.bind()

            specs.add(SpanSpec(start, end, format))
            cursor = end + 1 // gap of at least 1 char between spans
        }

        specs
    }

    /**
     * Generator for the full round-trip test case.
     */
    private val arbTestCase: Arb<RoundTripTestCase> = arbitrary {
        val text = arbSafeText.bind()
        val spans = arbNonOverlappingSpans(text.length).bind()
        RoundTripTestCase(text, spans)
    }

    /**
     * Extracts all [RichTextFormatSpan] instances from an Editable and returns
     * them as a sorted list of (start, end, format) triples for comparison.
     */
    private fun extractSpanSpecs(editable: android.text.Editable): List<Triple<Int, Int, RichTextFormat>> {
        val spans = editable.getSpans(0, editable.length, RichTextFormatSpan::class.java)
        return spans.map { span ->
            Triple(
                editable.getSpanStart(span),
                editable.getSpanEnd(span),
                span.getFormatType()
            )
        }.sortedWith(compareBy({ it.first }, { it.second }, { it.third.name }))
    }

    /**
     * Property 4: Markdown round-trip preservation.
     *
     * For any valid span-formatted Editable containing any combination of
     * Bold, Italic, Underline, and Strikethrough format spans, converting to
     * markdown via toMarkdown() and then parsing back via fromMarkdown() SHALL
     * produce an Editable with an equivalent set of format spans covering the
     * same text ranges, and the plain text content SHALL be identical.
     *
     * **Validates: Requirements 9.1–9.12**
     */
    @Test
    fun `Property 4 - markdown round-trip preserves plain text and format spans`() {
        runBlocking {
            checkAll(100, arbTestCase) { tc ->
                // Build the original Editable with format spans
                val original = SpannableStringBuilder(tc.text)
                for (spec in tc.spans) {
                    RichTextSpanManager.applyFormat(
                        original, spec.start, spec.end, spec.format, null
                    )
                }

                // Convert to markdown
                val markdown = MarkdownConverter.toMarkdown(original)

                // Parse back from markdown
                val restored = MarkdownConverter.fromMarkdown(markdown, null)

                // (a) Plain text content must be identical
                restored.toString() shouldBe original.toString()

                // (b) Format spans must cover the same text ranges
                val originalSpecs = extractSpanSpecs(original)
                val restoredSpecs = extractSpanSpecs(restored)

                restoredSpecs shouldBe originalSpecs
            }
        }
    }
}
