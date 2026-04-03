package com.cometchat.uikit.kotlin.presentation.users.utils

import androidx.recyclerview.widget.DiffUtil
import com.cometchat.chat.models.User

/**
 * DiffUtil callback for efficient RecyclerView updates when the users list changes.
 */
class UsersDiffCallback(
    private val oldList: List<User>,
    private val newList: List<User>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uid == newList[newItemPosition].uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldList[oldItemPosition]
        val newUser = newList[newItemPosition]
        
        return oldUser.uid == newUser.uid &&
            oldUser.name == newUser.name &&
            oldUser.avatar == newUser.avatar &&
            oldUser.status == newUser.status
    }
}
