package com.cometchat.uikit.kotlin.shared.spans

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.cometchat.uikit.kotlin.shared.formatters.SuggestionItem
import com.cometchat.uikit.kotlin.shared.formatters.style.PromptTextStyle
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [MentionCodeBlockHandler].
 *
 * Tests cover:
 * - consumeMentionsInRange: replacing NonEditableSpan with ConsumedMentionSpan
 * - restoreMentionsInRange: restoring ConsumedMentionSpan back to NonEditableSpan
 * - Round-trip: consume then restore preserves original mention data
 * - Edge cases: invalid ranges, no spans in range, non-restorable consumed spans
 *
 * **Validates: Requirements 14.1, 14.2**
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MentionCodeBlockHandlerTest {

    private fun createMentionSpan(
        id: Char = '@',
        text: String = "@John",
        style: PromptTextStyle? = null
    ): NonEditableSpan {
        return NonEditableSpan(id, text, style)
    }

    @Test
    fun `consumeMentionsInRange replaces NonEditableSpan with ConsumedMentionSpan`() {
        val editable = SpannableStringBuilder("Hello @John how are you")
        val mentionSpan = createMentionSpan()
        editable.setSpan(mentionSpan, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        MentionCodeBlockHandler.consumeMentionsInRange(editable, 0, editable.length)

        val nonEditableSpans = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
        val consumedSpans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(0, nonEditableSpans.size)
        assertEquals(1, consumedSpans.size)
        assertEquals('@', consumedSpans[0].id)
        assertEquals("@John", consumedSpans[0].text)
    }

    @Test
    fun `consumeMentionsInRange preserves span positions`() {
        val editable = SpannableStringBuilder("Hello @John how are you")
        val mentionSpan = createMentionSpan()
        editable.setSpan(mentionSpan, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        MentionCodeBlockHandler.consumeMentionsInRange(editable, 0, editable.length)

        val consumedSpans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(6, editable.getSpanStart(consumedSpans[0]))
        assertEquals(11, editable.getSpanEnd(consumedSpans[0]))
    }

    @Test
    fun `consumeMentionsInRange stores suggestion item`() {
        val style = PromptTextStyle().setColor(0xFF0000)
        val suggestion = SuggestionItem(
            id = "user1", name = "John", leadingIconUrl = null,
            status = null, promptText = "@John", underlyingText = "<@uid:user1>",
            data = null, promptTextStyle = style
        )
        val editable = SpannableStringBuilder("Hello @John")
        val mentionSpan = NonEditableSpan('@', "@John", suggestion)
        editable.setSpan(mentionSpan, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        MentionCodeBlockHandler.consumeMentionsInRange(editable, 0, editable.length)

        val consumed = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(suggestion, consumed[0].suggestionItem)
        assertEquals(style, consumed[0].textAppearance)
    }

    @Test
    fun `consumeMentionsInRange only affects spans in range`() {
        val editable = SpannableStringBuilder("@A text @B more")
        val spanA = createMentionSpan(text = "@A")
        val spanB = createMentionSpan(text = "@B")
        editable.setSpan(spanA, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(spanB, 8, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Only consume in the range covering @A
        MentionCodeBlockHandler.consumeMentionsInRange(editable, 0, 5)

        val nonEditable = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
        val consumed = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(1, nonEditable.size)
        assertEquals("@B", nonEditable[0].getText())
        assertEquals(1, consumed.size)
        assertEquals("@A", consumed[0].text)
    }

    @Test
    fun `restoreMentionsInRange restores ConsumedMentionSpan to NonEditableSpan`() {
        val editable = SpannableStringBuilder("Hello @John how are you")
        val consumed = ConsumedMentionSpan('@', "@John", null, null)
        editable.setSpan(consumed, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        MentionCodeBlockHandler.restoreMentionsInRange(editable, 0, editable.length)

        val consumedSpans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        val nonEditableSpans = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
        assertEquals(0, consumedSpans.size)
        assertEquals(1, nonEditableSpans.size)
        assertEquals('@', nonEditableSpans[0].getId())
        assertEquals("@John", nonEditableSpans[0].getText())
    }

    @Test
    fun `restoreMentionsInRange preserves span positions`() {
        val editable = SpannableStringBuilder("Hello @John how are you")
        val consumed = ConsumedMentionSpan('@', "@John", null, null)
        editable.setSpan(consumed, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        MentionCodeBlockHandler.restoreMentionsInRange(editable, 0, editable.length)

        val restored = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
        assertEquals(6, editable.getSpanStart(restored[0]))
        assertEquals(11, editable.getSpanEnd(restored[0]))
    }

    @Test
    fun `restoreMentionsInRange skips non-restorable spans`() {
        val editable = SpannableStringBuilder("Hello text")
        // id = '\u0000' makes canRestore() return false
        val consumed = ConsumedMentionSpan('\u0000', "@John", null, null)
        editable.setSpan(consumed, 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        MentionCodeBlockHandler.restoreMentionsInRange(editable, 0, editable.length)

        val consumedSpans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        val nonEditableSpans = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
        // Non-restorable consumed span should remain
        assertEquals(1, consumedSpans.size)
        assertEquals(0, nonEditableSpans.size)
    }

    @Test
    fun `restoreMentionsInRange skips spans with null text`() {
        val editable = SpannableStringBuilder("Hello text")
        val consumed = ConsumedMentionSpan('@', null, null, null)
        editable.setSpan(consumed, 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        MentionCodeBlockHandler.restoreMentionsInRange(editable, 0, editable.length)

        val consumedSpans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(1, consumedSpans.size)
    }

    @Test
    fun `round-trip consume then restore preserves mention data`() {
        val style = PromptTextStyle().setColor(0x00FF00)
        val editable = SpannableStringBuilder("Hello @John world")
        val mentionSpan = NonEditableSpan('@', "@John", style)
        editable.setSpan(mentionSpan, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        MentionCodeBlockHandler.consumeMentionsInRange(editable, 0, editable.length)
        MentionCodeBlockHandler.restoreMentionsInRange(editable, 0, editable.length)

        val restored = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
        assertEquals(1, restored.size)
        assertEquals('@', restored[0].getId())
        assertEquals("@John", restored[0].getText())
        assertEquals(style, restored[0].getTextAppearance())
        assertEquals(6, editable.getSpanStart(restored[0]))
        assertEquals(11, editable.getSpanEnd(restored[0]))
    }

    @Test
    fun `consumeMentionsInRange handles multiple mentions`() {
        val editable = SpannableStringBuilder("@A and @B here")
        val spanA = createMentionSpan(text = "@A")
        val spanB = createMentionSpan(text = "@B")
        editable.setSpan(spanA, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(spanB, 7, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        MentionCodeBlockHandler.consumeMentionsInRange(editable, 0, editable.length)

        val consumed = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(2, consumed.size)
    }

    @Test
    fun `consumeMentionsInRange with no mentions does nothing`() {
        val editable = SpannableStringBuilder("Hello world")

        MentionCodeBlockHandler.consumeMentionsInRange(editable, 0, editable.length)

        val consumed = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(0, consumed.size)
    }

    @Test
    fun `consumeMentionsInRange with invalid range does nothing`() {
        val editable = SpannableStringBuilder("Hello @John")
        val mentionSpan = createMentionSpan()
        editable.setSpan(mentionSpan, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // start >= end
        MentionCodeBlockHandler.consumeMentionsInRange(editable, 5, 5)

        val nonEditable = editable.getSpans(0, editable.length, NonEditableSpan::class.java)
        assertEquals(1, nonEditable.size)
    }

    @Test
    fun `restoreMentionsInRange with invalid range does nothing`() {
        val editable = SpannableStringBuilder("Hello @John")
        val consumed = ConsumedMentionSpan('@', "@John", null, null)
        editable.setSpan(consumed, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // negative start
        MentionCodeBlockHandler.restoreMentionsInRange(editable, -1, 11)

        val consumedSpans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(1, consumedSpans.size)
    }

    // ── extractConsumedMentionMetadata tests ──────────────────────────

    @Test
    fun `extractConsumedMentionMetadata returns null when no consumed spans`() {
        val editable = SpannableStringBuilder("Hello world")
        val result = MentionCodeBlockHandler.extractConsumedMentionMetadata(editable)
        assertNull(result)
    }

    @Test
    fun `extractConsumedMentionMetadata returns JSONArray with span data`() {
        val editable = SpannableStringBuilder("Hello @John world")
        val consumed = ConsumedMentionSpan('@', "@John", null, null)
        editable.setSpan(consumed, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val result = MentionCodeBlockHandler.extractConsumedMentionMetadata(editable)

        assertNotNull(result)
        assertEquals(1, result!!.length())
        val obj = result.getJSONObject(0)
        assertEquals('@'.code, obj.getInt("id"))
        assertEquals("@John", obj.getString("text"))
        assertEquals(6, obj.getInt("start"))
        assertEquals(11, obj.getInt("end"))
    }

    @Test
    fun `extractConsumedMentionMetadata handles multiple consumed spans`() {
        val editable = SpannableStringBuilder("@A and @B here")
        val spanA = ConsumedMentionSpan('@', "@A", null, null)
        val spanB = ConsumedMentionSpan('#', "@B", null, null)
        editable.setSpan(spanA, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(spanB, 7, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val result = MentionCodeBlockHandler.extractConsumedMentionMetadata(editable)

        assertNotNull(result)
        assertEquals(2, result!!.length())
    }

    // ── restoreConsumedMentionsFromMetadata tests ─────────────────────

    @Test
    fun `restoreConsumedMentionsFromMetadata does nothing with null metadata`() {
        val editable = SpannableStringBuilder("Hello world")
        MentionCodeBlockHandler.restoreConsumedMentionsFromMetadata(editable, null)

        val spans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(0, spans.size)
    }

    @Test
    fun `restoreConsumedMentionsFromMetadata does nothing without consumed_mentions key`() {
        val editable = SpannableStringBuilder("Hello world")
        val metadata = JSONObject().put("other_key", "value")
        MentionCodeBlockHandler.restoreConsumedMentionsFromMetadata(editable, metadata)

        val spans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(0, spans.size)
    }

    @Test
    fun `restoreConsumedMentionsFromMetadata applies ConsumedMentionSpan at correct positions`() {
        val editable = SpannableStringBuilder("Hello @John world")
        val array = JSONArray()
        array.put(JSONObject().apply {
            put("id", '@'.code)
            put("text", "@John")
            put("start", 6)
            put("end", 11)
        })
        val metadata = JSONObject().put(MentionCodeBlockHandler.CONSUMED_MENTIONS_KEY, array)

        MentionCodeBlockHandler.restoreConsumedMentionsFromMetadata(editable, metadata)

        val spans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(1, spans.size)
        assertEquals('@', spans[0].id)
        assertEquals("@John", spans[0].text)
        assertEquals(6, editable.getSpanStart(spans[0]))
        assertEquals(11, editable.getSpanEnd(spans[0]))
    }

    @Test
    fun `restoreConsumedMentionsFromMetadata skips entries with invalid positions`() {
        val editable = SpannableStringBuilder("Hello")
        val array = JSONArray()
        // start > editable.length
        array.put(JSONObject().apply {
            put("id", '@'.code)
            put("text", "@John")
            put("start", 10)
            put("end", 15)
        })
        val metadata = JSONObject().put(MentionCodeBlockHandler.CONSUMED_MENTIONS_KEY, array)

        MentionCodeBlockHandler.restoreConsumedMentionsFromMetadata(editable, metadata)

        val spans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(0, spans.size)
    }

    @Test
    fun `restoreConsumedMentionsFromMetadata skips entries with zero id`() {
        val editable = SpannableStringBuilder("Hello @John world")
        val array = JSONArray()
        array.put(JSONObject().apply {
            put("id", 0)
            put("text", "@John")
            put("start", 6)
            put("end", 11)
        })
        val metadata = JSONObject().put(MentionCodeBlockHandler.CONSUMED_MENTIONS_KEY, array)

        MentionCodeBlockHandler.restoreConsumedMentionsFromMetadata(editable, metadata)

        val spans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        assertEquals(0, spans.size)
    }

    // ── extract → restore round-trip test ─────────────────────────────

    @Test
    fun `extract then restore round-trip preserves consumed mention data`() {
        // Set up editable with a consumed mention span
        val editable = SpannableStringBuilder("Hello @John world")
        val consumed = ConsumedMentionSpan('@', "@John", null, null)
        editable.setSpan(consumed, 6, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Extract metadata
        val array = MentionCodeBlockHandler.extractConsumedMentionMetadata(editable)
        assertNotNull(array)
        val metadata = JSONObject().put(MentionCodeBlockHandler.CONSUMED_MENTIONS_KEY, array)

        // Create a fresh editable (simulating edit mode re-population)
        val freshEditable = SpannableStringBuilder("Hello @John world")

        // Restore from metadata
        MentionCodeBlockHandler.restoreConsumedMentionsFromMetadata(freshEditable, metadata)

        val spans = freshEditable.getSpans(0, freshEditable.length, ConsumedMentionSpan::class.java)
        assertEquals(1, spans.size)
        assertEquals('@', spans[0].id)
        assertEquals("@John", spans[0].text)
        assertEquals(6, freshEditable.getSpanStart(spans[0]))
        assertEquals(11, freshEditable.getSpanEnd(spans[0]))
    }
}
