package com.cometchat.uikit.kotlin.presentation.groupmembers.utils

import androidx.recyclerview.widget.DiffUtil
import com.cometchat.chat.models.GroupMember

/**
 * DiffUtil callback for efficient RecyclerView updates of group members.
 * Compares members by their unique UID and content.
 */
class GroupMembersDiffCallback(
    private val oldList: List<GroupMember>,
    private val newList: List<GroupMember>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uid == newList[newItemPosition].uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem.uid == newItem.uid &&
            oldItem.name == newItem.name &&
            oldItem.avatar == newItem.avatar &&
            oldItem.scope == newItem.scope &&
            oldItem.status == newItem.status
    }
}
