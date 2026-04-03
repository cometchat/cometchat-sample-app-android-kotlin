package com.cometchat.uikit.kotlin.presentation.stickerkeyboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cometchat.uikit.core.domain.model.StickerSet
import com.cometchat.uikit.kotlin.R

/**
 * RecyclerView adapter for displaying sticker set tabs in a horizontal scrollable bar.
 *
 * Each tab shows the icon of the sticker set (first sticker's URL).
 * The selected tab has a visual indicator below it.
 *
 * @param onTabClick Callback invoked when a tab is clicked, provides the index
 */
class StickerTabAdapter(
    private val onTabClick: ((Int) -> Unit)? = null
) : ListAdapter<StickerSet, StickerTabAdapter.TabViewHolder>(StickerSetDiffCallback()) {

    private var selectedIndex: Int = 0

    @ColorInt
    private var activeIndicatorColor: Int = 0

    /**
     * Sets the currently selected tab index.
     *
     * @param index The index of the selected tab
     */
    fun setSelectedIndex(index: Int) {
        val previousIndex = selectedIndex
        selectedIndex = index
        if (previousIndex != index) {
            notifyItemChanged(previousIndex)
            notifyItemChanged(index)
        }
    }

    /**
     * Sets the color for the active tab indicator.
     *
     * @param color The color to use for the active indicator
     */
    fun setActiveIndicatorColor(@ColorInt color: Int) {
        activeIndicatorColor = color
        notifyItemChanged(selectedIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_sticker_tab_item, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedIndex)
    }

    inner class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivTabIcon: ImageView = itemView.findViewById(R.id.iv_tab_icon)
        private val viewIndicator: View = itemView.findViewById(R.id.view_indicator)

        fun bind(stickerSet: StickerSet, isSelected: Boolean) {
            // Load tab icon using Glide
            Glide.with(itemView.context)
                .load(stickerSet.iconUrl)
                .placeholder(R.drawable.cometchat_progress_drawable)
                .into(ivTabIcon)

            // Show/hide selection indicator
            viewIndicator.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            if (isSelected && activeIndicatorColor != 0) {
                viewIndicator.setBackgroundColor(activeIndicatorColor)
            }

            // Set click listener
            itemView.setOnClickListener {
                @Suppress("DEPRECATION")
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTabClick?.invoke(position)
                }
            }

            // Set content description for accessibility
            itemView.contentDescription = itemView.context.getString(
                R.string.cometchat_sticker_set
            ) + ": " + stickerSet.name
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private class StickerSetDiffCallback : DiffUtil.ItemCallback<StickerSet>() {
        override fun areItemsTheSame(oldItem: StickerSet, newItem: StickerSet): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: StickerSet, newItem: StickerSet): Boolean {
            return oldItem == newItem
        }
    }
}
