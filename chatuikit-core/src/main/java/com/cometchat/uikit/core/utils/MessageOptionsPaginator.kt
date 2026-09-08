package com.cometchat.uikit.core.utils

/**
 * Shared pagination logic for the message-options popup menu (used by both the
 * Compose and Kotlin-View UIKits so their behaviour stays identical).
 *
 * The rule:
 *  - If the number of options is within [MAX_VISIBLE_OPTIONS], a single page shows
 *    them all with no navigation rows.
 *  - Otherwise page 1 shows the first [MAX_VISIBLE_OPTIONS] real options followed by a
 *    [Slot.More] row. Tapping "More" advances to the next page, which
 *    lists the remaining options and ends with a [Slot.Back] row. If those remaining
 *    options still overflow, that page also gets its own [Slot.More] (before "Back"),
 *    chaining to a further page.
 *
 * The algorithm is deliberately generic over element type: it returns page layouts as
 * lists of [Slot]s referencing source indices, so each UIKit maps [Slot.Item] to its own
 * row type ([com.cometchat.uikit.core.domain.model.CometChatMessageOption] in Compose,
 * a `MenuItem` in the View kit) and renders its own "More"/"Back" rows. With today's
 * option counts this resolves to exactly two pages, but it is not hardcoded to two.
 */
object MessageOptionsPaginator {

    /** Maximum number of real options shown on the first page before a "More" row is added. */
    const val MAX_VISIBLE_OPTIONS = 6

    /** Stable id used for the synthetic "More" navigation row (never a real option). */
    const val MORE_ID = "cometchat_option_more"

    /** Stable id used for the synthetic "Back" navigation row (never a real option). */
    const val BACK_ID = "cometchat_option_back"

    /**
     * A single position within a rendered page.
     */
    sealed interface Slot {
        /** A real option, identified by its index into the source options list. */
        data class Item(val index: Int) : Slot

        /** Navigation row that advances to the next page. */
        data object More : Slot

        /** Navigation row that returns to the previous page. */
        data object Back : Slot
    }

    /**
     * Splits [size] options into pages of [Slot]s.
     *
     * @param size the number of real options to paginate
     * @param maxVisible the cap of real options on the first page (defaults to [MAX_VISIBLE_OPTIONS])
     * @return the ordered pages; the returned list always has at least one page (possibly empty)
     */
    fun paginate(size: Int, maxVisible: Int = MAX_VISIBLE_OPTIONS): List<List<Slot>> {
        require(maxVisible >= 1) { "maxVisible must be >= 1" }

        // Fits on a single page — no navigation rows.
        if (size <= maxVisible) {
            return listOf((0 until size).map { Slot.Item(it) })
        }

        val pages = mutableListOf<List<Slot>>()
        var index = 0
        var firstPage = true

        while (index < size) {
            val page = mutableListOf<Slot>()

            if (firstPage) {
                // First page: up to maxVisible real options, then "More".
                val end = minOf(index + maxVisible, size)
                for (i in index until end) page.add(Slot.Item(i))
                index = end
                page.add(Slot.More)
                firstPage = false
            } else {
                val remaining = size - index
                if (remaining <= maxVisible) {
                    // Last page: remaining real options, then "Back".
                    for (i in index until size) page.add(Slot.Item(i))
                    index = size
                    page.add(Slot.Back)
                } else {
                    // Middle page: reserve one slot for "More" and one for "Back".
                    val end = index + (maxVisible - 1)
                    for (i in index until end) page.add(Slot.Item(i))
                    index = end
                    page.add(Slot.More)
                    page.add(Slot.Back)
                }
            }

            pages.add(page)
        }

        return pages
    }
}
