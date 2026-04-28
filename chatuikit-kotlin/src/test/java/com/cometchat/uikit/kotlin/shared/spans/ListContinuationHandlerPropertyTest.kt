package com.cometchat.uikit.kotlin.shared.spans

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.cometchat.uikit.core.formatter.RichTextFormat
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
 * Property-based tests for [ListContinuationHandler].
 *
 * Feature: v6-rich-text-composer, Properties 7, 8, 9
 *
 * - Property 7: List continuation on non-empty lines
 * - Property 8: List exit on empty prefix lines
 * - Property 9: Numbered list sequential renumbering
 *
 * **Validates: Requirements 10.1–10.6, 11.1**
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ListContinuationHandlerPropertyTest {

    /**
     * The three block format types that ListContinuationHandler handles.
     */
    private enum class ListType { BULLET, NUMBERED, BLOCKQUOTE }

    private val arbListType: Arb<ListType> = Arb.element(ListType.entries)

    /**
     * Generates non-empty line content (no newlines, at least 1 non-blank char).
     */
    private val arbNonEmptyContent: Arb<String> = arbitrary {
        // Generate a string of 1–50 printable chars, then ensure it's not blank
        var s = Arb.string(minSize = 1, maxSize = 50).bind()
        // Replace any newlines and ensure non-blank
        s = s.replace('\n', 'x')
        if (s.isBlank()) s = "a"
        s
    }

    /**
     * Creates a span for the given list type.
     */
    private fun createSpan(type: ListType, number: Int = 1): RichTextFormatSpan {
        return when (type) {
            ListType.BULLET -> BulletListFormatSpan()
            ListType.NUMBERED -> NumberedListFormatSpan(number)
            ListType.BLOCKQUOTE -> BlockquoteFormatSpan()
        }
    }

    /**
     * Returns the expected RichTextFormat for a given list type.
     */
    private fun expectedFormat(type: ListType): RichTextFormat {
        return when (type) {
            ListType.BULLET -> RichTextFormat.BULLET_LIST
            ListType.NUMBERED -> RichTextFormat.ORDERED_LIST
            ListType.BLOCKQUOTE -> RichTextFormat.BLOCKQUOTE
        }
    }

    // ==================== Property 7 ====================

    /**
     * Property 7: List continuation on non-empty lines.
     *
     * For any non-empty bullet list line, non-empty numbered list line, or
     * non-empty blockquote line, inserting a newline at the end of the line
     * via [ListContinuationHandler.handleNewline] SHALL produce a new line
     * with the appropriate prefix and the corresponding format span.
     *
     * **Validates: Requirements 10.1, 10.2, 10.5**
     */
    @Test
    fun `Property 7 - handleNewline on non-empty list line creates new prefixed line`() {
        runBlocking {
            checkAll(100, arbListType, arbNonEmptyContent) { listType, content ->
                val editable = SpannableStringBuilder(content)
                val span = createSpan(listType)
                editable.setSpan(span, 0, content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                // Simulate Enter: insert newline at the end
                editable.insert(content.length, "\n")
                val cursorPos = content.length + 1

                val handled = ListContinuationHandler.handleNewline(editable, cursorPos, null)

                handled shouldBe true

                // After handleNewline, there should be at least 2 spans of this format:
                // the original on the first line and the continuation on the new line.
                val format = expectedFormat(listType)
                val allSpans = editable.getSpans(
                    0,
                    editable.length,
                    RichTextFormatSpan::class.java
                )
                val matchingSpans = allSpans.filter { it.getFormatType() == format }
                matchingSpans.size shouldBeGreaterThan 1

                // Find the span that starts at or after cursorPos (the continuation span)
                val continuationSpan = matchingSpans.firstOrNull {
                    editable.getSpanStart(it) >= cursorPos
                }
                continuationSpan shouldNotBe null
            }
        }
    }

    // ==================== Property 8 ====================

    /**
     * Property 8: List exit on empty prefix lines.
     *
     * For any bullet list line, numbered list line, or blockquote line that
     * contains only the prefix and no content (empty/blank), inserting a
     * newline via [ListContinuationHandler.handleNewline] SHALL remove the
     * prefix from that line and not insert a new prefixed line.
     *
     * **Validates: Requirements 10.3, 10.4, 10.6**
     */
    @Test
    fun `Property 8 - handleNewline on empty prefix line removes prefix and exits mode`() {
        runBlocking {
            checkAll(100, arbListType) { listType ->
                // Create an editable with an empty line that has a list/blockquote span
                // (simulates a line with only the prefix, no user content)
                val editable = SpannableStringBuilder("")
                val span = createSpan(listType)
                // Apply span as MARK_MARK on empty content (zero-length span)
                editable.setSpan(span, 0, 0, Spanned.SPAN_MARK_MARK)

                // Simulate Enter: insert newline at position 0
                editable.insert(0, "\n")
                val cursorPos = 1

                val handled = ListContinuationHandler.handleNewline(editable, cursorPos, null)

                handled shouldBe true

                // After exit, there should be no spans of this format type remaining
                // on the area where the empty line was
                val format = expectedFormat(listType)
                val remainingSpans = editable.getSpans(
                    0,
                    editable.length,
                    RichTextFormatSpan::class.java
                )
                val matchingSpans = remainingSpans.filter { it.getFormatType() == format }

                // The empty prefix line's span should have been removed
                // (there should be no continuation span on a new line either)
                matchingSpans.size shouldBe 0
            }
        }
    }

    // ==================== Property 9 ====================

    /**
     * Property 9: Numbered list sequential renumbering.
     *
     * For any contiguous block of numbered list items with random initial
     * numbers, calling [ListContinuationHandler.renumberList] SHALL produce
     * sequential numbering starting from 1 for all items in the block.
     *
     * **Validates: Requirement 11.1**
     */
    @Test
    fun `Property 9 - renumberList produces sequential numbering from 1`() {
        runBlocking {
            checkAll(100, Arb.int(2..10)) { itemCount ->
                // Build a multi-line editable with numbered list items
                val lines = mutableListOf<String>()
                val spans = mutableListOf<NumberedListFormatSpan>()

                for (i in 0 until itemCount) {
                    val lineText = "Item ${i + 1}"
                    lines.add(lineText)
                    // Use random initial numbers (not sequential) to test renumbering
                    val randomNumber = (i + 1) * 3 + 7 // arbitrary non-sequential numbers
                    spans.add(NumberedListFormatSpan(randomNumber))
                }

                val fullText = lines.joinToString("\n")
                val editable = SpannableStringBuilder(fullText)

                // Apply spans to each line
                var offset = 0
                for (i in 0 until itemCount) {
                    val lineLen = lines[i].length
                    editable.setSpan(
                        spans[i],
                        offset,
                        offset + lineLen,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    offset += lineLen + 1 // +1 for '\n'
                }

                // Call renumberList starting from position 0
                ListContinuationHandler.renumberList(editable, 0)

                // Verify all spans are now numbered sequentially from 1
                val allSpans = editable.getSpans(
                    0,
                    editable.length,
                    NumberedListFormatSpan::class.java
                )
                val sorted = allSpans.sortedBy { editable.getSpanStart(it) }

                sorted.size shouldBe itemCount
                for (i in sorted.indices) {
                    sorted[i].number shouldBe (i + 1)
                }
            }
        }
    }

    /**
     * Property 9 (extended): After inserting a new item in the middle of a
     * numbered list and calling renumberList, all items remain sequentially
     * numbered from 1.
     *
     * **Validates: Requirement 11.1**
     */
    @Test
    fun `Property 9 - renumberList after mid-list insertion maintains sequential numbering`() {
        runBlocking {
            checkAll(100, Arb.int(3..8), Arb.int(1..7)) { itemCount, insertIdx ->
                val clampedInsertIdx = insertIdx.coerceIn(1, itemCount - 1)

                // Build initial list with correct sequential numbering
                val lines = (1..itemCount).map { "Item $it" }
                val fullText = lines.joinToString("\n")
                val editable = SpannableStringBuilder(fullText)

                // Apply numbered list spans
                var offset = 0
                for (i in 0 until itemCount) {
                    val lineLen = lines[i].length
                    val span = NumberedListFormatSpan(i + 1)
                    editable.setSpan(span, offset, offset + lineLen, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                    offset += lineLen + 1
                }

                // Find the insertion point (end of the line at clampedInsertIdx - 1)
                var insertOffset = 0
                for (i in 0 until clampedInsertIdx) {
                    insertOffset += lines[i].length + 1
                }

                // Insert a new line with a numbered span
                val newLineText = "\nNew item"
                editable.insert(insertOffset - 1, newLineText)
                val newSpan = NumberedListFormatSpan(999) // intentionally wrong number
                editable.setSpan(
                    newSpan,
                    insertOffset,
                    insertOffset + "New item".length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )

                // Renumber from the beginning
                ListContinuationHandler.renumberList(editable, 0)

                // Verify sequential numbering
                val allSpans = editable.getSpans(
                    0,
                    editable.length,
                    NumberedListFormatSpan::class.java
                )
                val sorted = allSpans.sortedBy { editable.getSpanStart(it) }

                sorted.size shouldBe (itemCount + 1)
                for (i in sorted.indices) {
                    sorted[i].number shouldBe (i + 1)
                }
            }
        }
    }
}
