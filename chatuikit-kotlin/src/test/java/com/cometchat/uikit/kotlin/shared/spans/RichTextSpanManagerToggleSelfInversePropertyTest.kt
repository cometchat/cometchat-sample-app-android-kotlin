package com.cometchat.uikit.kotlin.shared.spans

import android.text.SpannableStringBuilder
import com.cometchat.uikit.core.formatter.RichTextFormat
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Property-based tests for [RichTextSpanManager.toggleFormat] self-inverse behaviour.
 *
 * Feature: v6-rich-text-composer, Property 3: SpanManager toggleFormat is self-inverse
 *
 * **Validates: Requirements 8.3, 8.4**
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class RichTextSpanManagerToggleSelfInversePropertyTest {

    /**
     * Inline format types that can be applied to arbitrary character ranges.
     * Block formats are excluded because they operate on whole lines.
     */
    private val inlineFormats = listOf(
        RichTextFormat.BOLD,
        RichTextFormat.ITALIC,
        RichTextFormat.UNDERLINE,
        RichTextFormat.STRIKETHROUGH
    )

    private val arbFormat: Arb<RichTextFormat> = Arb.element(inlineFormats)

    /**
     * Describes a pre-existing format span to seed onto the Editable before toggling.
     */
    data class PreExistingSpan(
        val start: Int,
        val end: Int,
        val format: RichTextFormat
    )

    /**
     * Full test case: text with pre-existing spans, plus the toggle range and format.
     */
    data class ToggleTestCase(
        val text: String,
        val preExistingSpans: List<PreExistingSpan>,
        val toggleStart: Int,
        val toggleEnd: Int,
        val toggleFormat: RichTextFormat
    )

    /**
     * Generator for a single pre-existing span given a known text length.
     */
    private fun arbPreExistingSpan(textLen: Int): Arb<PreExistingSpan> = arbitrary {
        val start = Arb.int(0 until textLen).bind()
        val end = Arb.int(start + 1..textLen).bind()
        val format = arbFormat.bind()
        PreExistingSpan(start, end, format)
    }

    /**
     * Generator for the full toggle self-inverse test case.
     * Produces random text (1–80 chars), 0–4 pre-existing format spans,
     * and a random toggle range + format.
     */
    private val arbTestCase: Arb<ToggleTestCase> = arbitrary {
        val text = Arb.string(minSize = 1, maxSize = 80).bind()
        val len = text.length
        val spanCount = Arb.int(0..4).bind()
        val preSpans = if (spanCount > 0) {
            Arb.list(arbPreExistingSpan(len), spanCount..spanCount).bind()
        } else {
            emptyList()
        }
        val toggleStart = Arb.int(0 until len).bind()
        val toggleEnd = Arb.int(toggleStart + 1..len).bind()
        val toggleFmt = arbFormat.bind()
        ToggleTestCase(text, preSpans, toggleStart, toggleEnd, toggleFmt)
    }

    /**
     * Snapshots the presence/absence of a specific format at every position in the Editable.
     */
    private fun snapshotFormat(
        editable: SpannableStringBuilder,
        format: RichTextFormat
    ): List<Boolean> {
        return (0 until editable.length).map { pos ->
            format in RichTextSpanManager.getFormatsAt(editable, pos)
        }
    }

    /**
     * Builds a [SpannableStringBuilder] from the test case, applying all pre-existing spans.
     */
    private fun buildEditable(tc: ToggleTestCase): SpannableStringBuilder {
        val editable = SpannableStringBuilder(tc.text)
        for (span in tc.preExistingSpans) {
            RichTextSpanManager.applyFormat(editable, span.start, span.end, span.format, null)
        }
        return editable
    }

    /**
     * Property 3: toggleFormat is self-inverse.
     *
     * For any valid Editable with random pre-existing format spans, any valid
     * range [start, end), and any inline format type, calling toggleFormat twice
     * on the same range and format SHALL produce an Editable where the toggled
     * format is present at exactly the same positions as the original.
     *
     * **Validates: Requirements 8.3, 8.4**
     */
    @Test
    fun `Property 3 - toggleFormat twice restores original format state at every position`() {
        runBlocking {
            checkAll(100, arbTestCase) { tc ->
                val editable = buildEditable(tc)

                // Snapshot the format presence before toggling
                val before = snapshotFormat(editable, tc.toggleFormat)

                // Toggle once
                RichTextSpanManager.toggleFormat(
                    editable, tc.toggleFormat, tc.toggleStart, tc.toggleEnd, null
                )
                // Toggle again (same range, same format)
                RichTextSpanManager.toggleFormat(
                    editable, tc.toggleFormat, tc.toggleStart, tc.toggleEnd, null
                )

                // Snapshot after double-toggle
                val after = snapshotFormat(editable, tc.toggleFormat)

                after shouldBe before
            }
        }
    }
}
