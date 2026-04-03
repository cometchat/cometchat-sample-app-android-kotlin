package com.cometchat.uikit.kotlin.presentation.shared.shimmer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * CometChatShimmerAdapter is a simple RecyclerView adapter that displays
 * shimmer placeholder items for loading states.
 */
class CometChatShimmerAdapter(
    private val itemCount: Int,
    @LayoutRes private val layoutRes: Int
) : RecyclerView.Adapter<CometChatShimmerAdapter.ViewHolder>() {

    companion object {
        private val TAG = CometChatShimmerAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // No binding needed for shimmer placeholders
    }

    override fun getItemCount(): Int = itemCount

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
