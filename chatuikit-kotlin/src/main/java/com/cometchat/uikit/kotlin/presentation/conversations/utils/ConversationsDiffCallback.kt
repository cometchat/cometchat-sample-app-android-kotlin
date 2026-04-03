package com.cometchat.uikit.kotlin.presentation.conversations.utils

import androidx.recyclerview.widget.DiffUtil
import com.cometchat.chat.models.Conversation

/**
 * DiffUtil callback for efficient RecyclerView updates.
 * Compares conversations by their unique ID and content.
 */
class ConversationsDiffCallback(
    private val oldList: List<Conversation>,
    private val newList: List<Conversation>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].conversationId == newList[newItemPosition].conversationId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return newItem == oldItem
    }
}
