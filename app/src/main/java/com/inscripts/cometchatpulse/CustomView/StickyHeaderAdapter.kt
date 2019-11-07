package com.inscripts.cometchatpulse.CustomView

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

interface StickyHeaderAdapter<T: androidx.recyclerview.widget.RecyclerView.ViewHolder> {

     fun getHeaderId(var1: Int): Long

     fun onCreateHeaderViewHolder(var1: ViewGroup): T

     fun onBindHeaderViewHolder(var1: T, var2: Int, var3: Long)
}