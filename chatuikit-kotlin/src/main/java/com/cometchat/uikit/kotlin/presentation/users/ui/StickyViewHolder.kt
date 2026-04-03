package com.cometchat.uikit.kotlin.presentation.users.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.uikit.kotlin.databinding.CometchatUserListStickyHeaderBinding

/**
 * ViewHolder for sticky header items in the users list.
 */
class StickyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val binding: CometchatUserListStickyHeaderBinding =
        CometchatUserListStickyHeaderBinding.bind(itemView)
}
