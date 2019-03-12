package com.inscripts.cometchatpulse.CustomView

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

interface StickyHeaderAdapter<T:RecyclerView.ViewHolder> {

     fun getHeaderId(var1: Int): Long

     fun onCreateHeaderViewHolder(var1: ViewGroup): T

     fun onBindHeaderViewHolder(var1: T, var2: Int, var3: Long)
}