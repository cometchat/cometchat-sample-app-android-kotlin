package com.cometchat.uikit.kotlin.shared.spans

import android.text.SpannableStringBuilder
import com.cometchat.uikit.core.formatter.RichTextFormat
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Property-based tests for [RichTextSpanManager.applyFormat] and [RichTextSpanManager.removeFormat].
 *
 * Feature: v6-rich-text-composer, Property 2: SpanManager addFormat/removeFormat correctness
 *
 * **Validates: Requirements 8.1, 8.2, 8.5**
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class RichTextSpanManagerAddRemoveFormatPropertyTest {

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

    data class FormatTestCase(
        val text: String,
        val start: Int,
        val end: Int,
        val format: RichTextFormat
    )

    private val arbTestCase: Arb<FormatTestCase> = arbitrary {
        val text = Arb.string(minSize = 1, maxSize = 100).bind()
        val len = text.length
        val start = Arb.int(0 until len).bind()
        val end = Arb.int(start + 1..len).bind()
        val format = arbFormat.bind()
        FormatTestCase(text, start, end, format)
    }

    /**
     * Property 2a: After applyFormat, getFormatsAt returns the format for every
     * position in [start, end).
     *
     * For any valid Editable text, any valid range [start, end), and any inline
     * format type, calling applyFormat followed by getFormatsAt for every position
     * in [start, end) SHALL return a set containing that format.
     *
     * **Validates: Requirements 8.1, 8.5**
     */
    @Test
    fun `Property 2 - after applyFormat, getFormatsAt returns the format for every position in range`() {
        runBlocking {
            checkAll(100, arbTestCase) { tc ->
                val editable = SpannableStringBuilder(tc.text)

                RichTextSpanManager.applyFormat(editable, tc.start, tc.end, tc.format, null)

                for (pos in tc.start until tc.end) {
                    val formatsAtPos = RichTextSpanManager.getFormatsAt(editable, pos)
                    formatsAtPos shouldContain tc.format
                }
            }
        }
    }

    /**
     * Property 2b: After applyFormat then removeFormat, getFormatsAt does NOT
     * return the format for any position in [start, end).
     *
     * For any valid Editable text, any valid range [start, end), and any inline
     * format type, calling applyFormat then removeFormat on the same range SHALL
     * result in getFormatsAt not returning that format for any position in the range.
     *
     * **Validates: Requirements 8.2, 8.5**
     */
    @Test
    fun `Property 2 - after applyFormat then removeFormat, getFormatsAt does not return the format`() {
        runBlocking {
            checkAll(100, arbTestCase) { tc ->
                val editable = SpannableStringBuilder(tc.text)

                RichTextSpanManager.applyFormat(editable, tc.start, tc.end, tc.format, null)
                RichTextSpanManager.removeFormat(editable, tc.start, tc.end, tc.format)

                for (pos in tc.start until tc.end) {
                    val formatsAtPos = RichTextSpanManager.getFormatsAt(editable, pos)
                    formatsAtPos shouldNotContain tc.format
                }
            }
        }
    }
}
