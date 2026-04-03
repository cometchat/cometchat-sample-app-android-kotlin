package com.cometchat.uikit.kotlin.presentation.stickerkeyboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.kotlin.R

/**
 * RecyclerView.Adapter for ViewPager2 that displays sticker grids.
 *
 * This adapter does NOT use FragmentStateAdapter, allowing the sticker keyboard
 * to be used in Dialog, BottomSheet, or any embedded view context without
 * requiring a FragmentActivity.
 *
 * Each page is a RecyclerView with a GridLayoutManager (4 columns) displaying
 * stickers from a single sticker set.
 *
 * @param onStickerClick Callback invoked when a sticker is clicked
 */
class StickerPagerAdapter(
    private val onStickerClick: ((Sticker) -> Unit)? = null
) : RecyclerView.Adapter<StickerPagerAdapter.PageViewHolder>() {

    private var stickerSets: List<StickerSet> = emptyList()

    /**
     * Updates the list of sticker sets.
     *
     * @param sets The new list of sticker sets to display
     */
    fun submitList(sets: List<StickerSet>) {
        stickerSets = sets
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = stickerSets.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val recyclerView = RecyclerView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = GridLayoutManager(context, 4)
            clipToPadding = false
            setPadding(
                resources.getDimensionPixelSize(R.dimen.cometchat_padding_2),
                resources.getDimensionPixelSize(R.dimen.cometchat_padding_2),
                resources.getDimensionPixelSize(R.dimen.cometchat_padding_2),
                resources.getDimensionPixelSize(R.dimen.cometchat_padding_2)
            )
        }
        return PageViewHolder(recyclerView)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(stickerSets[position])
    }

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView as RecyclerView
        private val gridAdapter = StickerGridAdapter(onStickerClick)

        init {
            recyclerView.adapter = gridAdapter
        }

        fun bind(stickerSet: StickerSet) {
            gridAdapter.submitList(stickerSet.stickers)
        }
    }
}
