package com.cometchat.pro.uikit.ui_resources.utils.sticker_header

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface StickyHeaderAdapter<T : RecyclerView.ViewHolder?> {
    fun getHeaderId(var1: Int): Long
    fun onCreateHeaderViewHolder(var1: ViewGroup?): T
    fun onBindHeaderViewHolder(var1: Any, var2: Int, var3: Long)
}