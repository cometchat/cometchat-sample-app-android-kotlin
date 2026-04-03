package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar

/**
 * RecyclerView adapter for displaying voter avatars in poll options.
 */
class ImageAndCountAdapter(
    private val context: Context
) : RecyclerView.Adapter<ImageAndCountAdapter.ViewHolder>() {

    private var images: List<ImageTextPoJo> = emptyList()
    @StyleRes private var avatarStyle: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cometchat_image_and_count_row_layout, parent, false)
        val overlap = 20
        val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
        layoutParams?.setMargins(0, 0, if (viewType == 0) 0 else -overlap, 0)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemViewType(position: Int): Int = if (position == 0) 0 else 1

    override fun getItemCount(): Int = images.size

    fun setList(images: List<ImageTextPoJo>) {
        if (images.isNotEmpty()) {
            this.images = images
            notifyDataSetChanged()
        }
    }

    fun setAvatarStyle(@StyleRes style: Int) {
        this.avatarStyle = style
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: CometChatAvatar = itemView.findViewById(R.id.cometchatAvatar)

        init {
            avatar.radius = context.resources.getDimension(R.dimen.cometchat_radius_max)
        }

        fun bind(imageTextPoJo: ImageTextPoJo) {
            avatar.setAvatar(imageTextPoJo.text, imageTextPoJo.imageUrl)
            if (avatarStyle != 0) {
                avatar.setStyle(avatarStyle)
            }
        }
    }
}
