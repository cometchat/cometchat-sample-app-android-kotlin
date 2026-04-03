package com.cometchat.uikit.core.formatter

/**
 * Represents a formatting span over a range of text.
 * Each span tracks a start/end position and the set of formats applied to that range.
 *
 * Spans are immutable — operations on [RichTextSpanManager] produce new span lists.
 *
 * @param start Inclusive start index in the plain text
 * @param end Exclusive end index in the plain text
 * @param formats Set of formats applied to this range
 */
data class RichTextSpan(
    val start: Int,
    val end: Int,
    val formats: Set<RichTextFormat>
) {
    /** True if this span covers zero or negative length. */
    val isEmpty: Boolean get() = start >= end

    /** Length of the span. */
    val length: Int get() = (end - start).coerceAtLeast(0)

    /** True if this span contains the given position. */
    fun contains(position: Int): Boolean = position in start until end

    /** True if this span overlaps with the given range. */
    fun overlaps(rangeStart: Int, rangeEnd: Int): Boolean =
        start < rangeEnd && end > rangeStart

    /** Returns a copy shifted by the given offset. */
    fun shift(offset: Int): RichTextSpan =
        copy(start = start + offset, end = end + offset)
}
