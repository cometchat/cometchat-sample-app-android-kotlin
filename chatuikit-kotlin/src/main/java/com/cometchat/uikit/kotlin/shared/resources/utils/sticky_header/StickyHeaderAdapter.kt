package com.cometchat.uikit.kotlin.shared.resources.utils.sticky_header

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Interface for adapters that support sticky headers.
 * Adapters implementing this interface can provide header information
 * to StickyHeaderDecoration for drawing sticky headers on RecyclerView.
 *
 * @param T The type of ViewHolder used for headers
 */
interface StickyHeaderAdapter<T : RecyclerView.ViewHolder> {

    companion object {
        const val NO_HEADER_ID = -1L
    }

    /**
     * Returns the header ID for the item at the given position.
     * Items with the same header ID will be grouped under the same header.
     *
     * @param position The adapter position
     * @return The header ID, or NO_HEADER_ID if no header should be shown
     */
    fun getHeaderId(position: Int): Long

    /**
     * Creates a new ViewHolder for the header view.
     *
     * @param parent The parent ViewGroup
     * @return A new header ViewHolder
     */
    fun onCreateHeaderViewHolder(parent: ViewGroup): T

    /**
     * Binds data to the header ViewHolder.
     *
     * @param holder The header ViewHolder
     * @param position The adapter position of the first item in this header group
     * @param headerId The header ID
     */
    fun onBindHeaderViewHolder(holder: T, position: Int, headerId: Long)
}
