package com.cometchat.uikit.kotlin.presentation.stickerkeyboard.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.kotlin.R

/**
 * RecyclerView adapter for displaying stickers in a grid layout.
 *
 * This adapter uses Glide for image loading with GIF support.
 * It handles loading states and click events for sticker selection.
 *
 * @param onStickerClick Callback invoked when a sticker is clicked
 */
class StickerGridAdapter(
    private val onStickerClick: ((Sticker) -> Unit)? = null
) : ListAdapter<Sticker, StickerGridAdapter.StickerViewHolder>(StickerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_sticker_grid_item, parent, false)
        return StickerViewHolder(view)
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivSticker: ImageView = itemView.findViewById(R.id.iv_sticker)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)

        fun bind(sticker: Sticker) {
            // Show loading indicator
            progressBar.visibility = View.VISIBLE

            // Build fallback request for non-GIF images
            val fallbackRequest = Glide.with(itemView.context)
                .load(sticker.url)

            // Load sticker image with Glide
            // Try loading as drawable (handles both GIF and static images)
            // Use error() for fallback to avoid callback issues
            Glide.with(itemView.context)
                .load(sticker.url)
                .error(fallbackRequest)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(ivSticker)

            // Set click listener
            itemView.setOnClickListener {
                onStickerClick?.invoke(sticker)
            }

            // Set content description for accessibility
            itemView.contentDescription = sticker.name.ifEmpty { 
                itemView.context.getString(R.string.cometchat_sticker) 
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private class StickerDiffCallback : DiffUtil.ItemCallback<Sticker>() {
        override fun areItemsTheSame(oldItem: Sticker, newItem: Sticker): Boolean {
            return oldItem.url == newItem.url && oldItem.setName == newItem.setName
        }

        override fun areContentsTheSame(oldItem: Sticker, newItem: Sticker): Boolean {
            return oldItem == newItem
        }
    }
}
