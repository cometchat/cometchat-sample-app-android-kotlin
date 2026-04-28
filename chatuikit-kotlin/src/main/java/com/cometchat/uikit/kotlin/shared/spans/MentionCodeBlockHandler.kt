package com.cometchat.uikit.kotlin.shared.spans

import android.text.Editable
import android.text.Spanned
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles consuming and restoring mention spans when code formatting
 * (CodeBlock or InlineCode) is toggled on or off.
 *
 * When code formatting is applied to a range containing [NonEditableSpan] mentions,
 * each mention is replaced with a [ConsumedMentionSpan] that preserves the original
 * mention data. When code formatting is removed, consumed mentions are restored
 * back to [NonEditableSpan] instances.
 *
 * @see ConsumedMentionSpan
 * @see NonEditableSpan
 */
object MentionCodeBlockHandler {

    /**
     * Finds all [NonEditableSpan] instances in the given range and replaces
     * each with a [ConsumedMentionSpan] that stores the original mention data.
     *
     * Called when CodeBlock or InlineCode formatting is applied to a range.
     *
     * @param editable The Editable containing the mention spans.
     * @param start    The start index (inclusive) of the range.
     * @param end      The end index (exclusive) of the range.
     */
    fun consumeMentionsInRange(editable: Editable, start: Int, end: Int) {
        if (start < 0 || end < 0 || start >= end || start > editable.length || end > editable.length) return

        val mentionSpans = editable.getSpans(start, end, NonEditableSpan::class.java)
        for (span in mentionSpans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            // Only consume spans that actually overlap with the range
            if (spanStart >= end || spanEnd <= start) continue

            val consumed = ConsumedMentionSpan(
                id = span.getId(),
                text = span.getText(),
                suggestionItem = span.getSuggestionItem(),
                textAppearance = span.getTextAppearance()
            )
            editable.removeSpan(span)
            editable.setSpan(consumed, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * Finds all [ConsumedMentionSpan] instances in the given range and restores
     * each to a [NonEditableSpan] if [ConsumedMentionSpan.canRestore] returns true.
     *
     * Called when CodeBlock or InlineCode formatting is removed from a range.
     *
     * @param editable The Editable containing the consumed mention spans.
     * @param start    The start index (inclusive) of the range.
     * @param end      The end index (exclusive) of the range.
     */
    fun restoreMentionsInRange(editable: Editable, start: Int, end: Int) {
        if (start < 0 || end < 0 || start >= end || start > editable.length || end > editable.length) return

        val consumedSpans = editable.getSpans(start, end, ConsumedMentionSpan::class.java)
        for (consumed in consumedSpans) {
            if (!consumed.canRestore()) continue

            val spanStart = editable.getSpanStart(consumed)
            val spanEnd = editable.getSpanEnd(consumed)
            // Only restore spans that actually overlap with the range
            if (spanStart >= end || spanEnd <= start) continue

            editable.removeSpan(consumed)
            val restored = NonEditableSpan(
                consumed.id,
                consumed.text ?: "",
                consumed.textAppearance
            )
            editable.setSpan(restored, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * Metadata key used to store consumed mention data in message metadata.
     */
    const val CONSUMED_MENTIONS_KEY = "consumed_mentions"

    /**
     * Extracts all [ConsumedMentionSpan] instances from the Editable and returns
     * their data as a [JSONArray]. Each entry contains the mention's id, text,
     * start position, and end position.
     *
     * Returns `null` if no [ConsumedMentionSpan] instances are found.
     *
     * @param editable The Editable to scan for consumed mention spans.
     * @return A JSONArray of consumed mention data, or null if none found.
     */
    fun extractConsumedMentionMetadata(editable: Editable): JSONArray? {
        val spans = editable.getSpans(0, editable.length, ConsumedMentionSpan::class.java)
        if (spans.isEmpty()) return null

        val array = JSONArray()
        for (span in spans) {
            val start = editable.getSpanStart(span)
            val end = editable.getSpanEnd(span)
            if (start < 0 || end < 0) continue

            val obj = JSONObject()
            obj.put("id", span.id.code)
            obj.put("text", span.text ?: "")
            obj.put("start", start)
            obj.put("end", end)
            array.put(obj)
        }
        return if (array.length() > 0) array else null
    }

    /**
     * Reads consumed mention data from message metadata and applies
     * [ConsumedMentionSpan] instances at the stored positions in the Editable.
     *
     * @param editable The Editable to apply consumed mention spans to.
     * @param metadata The message metadata JSONObject (may be null).
     */
    fun restoreConsumedMentionsFromMetadata(editable: Editable, metadata: JSONObject?) {
        if (metadata == null || !metadata.has(CONSUMED_MENTIONS_KEY)) return

        val array = metadata.optJSONArray(CONSUMED_MENTIONS_KEY) ?: return
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val idCode = obj.optInt("id", 0)
            val text = obj.optString("text", "")
            val start = obj.optInt("start", -1)
            val end = obj.optInt("end", -1)

            if (idCode == 0 || start < 0 || end < 0 || start >= end) continue
            if (start > editable.length || end > editable.length) continue

            val span = ConsumedMentionSpan(
                id = idCode.toChar(),
                text = text,
                suggestionItem = null,
                textAppearance = null
            )
            editable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}
