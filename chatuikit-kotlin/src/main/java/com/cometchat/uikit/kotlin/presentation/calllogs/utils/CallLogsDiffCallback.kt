package com.cometchat.uikit.kotlin.presentation.calllogs.utils

import androidx.recyclerview.widget.DiffUtil
import com.cometchat.calls.model.CallLog

/**
 * DiffUtil.Callback implementation for efficient CallLog list updates.
 * Compares call logs by their session ID for identity and content equality.
 */
class CallLogsDiffCallback(
    private val oldList: List<CallLog>,
    private val newList: List<CallLog>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        // Using object equality like the original Java implementation
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        // Compare relevant fields for content equality
        return oldItem == newItem &&
                oldItem.initiatedAt == newItem.initiatedAt &&
                oldItem.endedAt == newItem.endedAt &&
                oldItem.status == newItem.status &&
                oldItem.type == newItem.type
    }
}
