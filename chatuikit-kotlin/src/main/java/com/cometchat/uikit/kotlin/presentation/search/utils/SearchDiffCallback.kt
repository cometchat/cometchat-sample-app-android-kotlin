package com.cometchat.uikit.kotlin.presentation.search.utils

import androidx.recyclerview.widget.DiffUtil
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation

/**
 * DiffUtil callback for conversation list updates.
 *
 * This callback is used to efficiently update the conversation RecyclerView
 * by calculating the minimal set of changes needed.
 */
class ConversationDiffCallback(
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
        
        return oldItem.conversationId == newItem.conversationId &&
               oldItem.updatedAt == newItem.updatedAt &&
               oldItem.unreadMessageCount == newItem.unreadMessageCount &&
               oldItem.lastMessage?.id == newItem.lastMessage?.id
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        
        val payload = mutableMapOf<String, Any?>()
        
        if (oldItem.updatedAt != newItem.updatedAt) {
            payload["updatedAt"] = newItem.updatedAt
        }
        if (oldItem.unreadMessageCount != newItem.unreadMessageCount) {
            payload["unreadMessageCount"] = newItem.unreadMessageCount
        }
        if (oldItem.lastMessage?.id != newItem.lastMessage?.id) {
            payload["lastMessage"] = newItem.lastMessage
        }
        
        return if (payload.isNotEmpty()) payload else null
    }
}

/**
 * DiffUtil callback for message list updates.
 *
 * This callback is used to efficiently update the message RecyclerView
 * by calculating the minimal set of changes needed.
 */
class MessageDiffCallback(
    private val oldList: List<BaseMessage>,
    private val newList: List<BaseMessage>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        
        return oldItem.id == newItem.id &&
               oldItem.sentAt == newItem.sentAt &&
               oldItem.updatedAt == newItem.updatedAt &&
               oldItem.deletedAt == newItem.deletedAt
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        
        val payload = mutableMapOf<String, Any?>()
        
        if (oldItem.updatedAt != newItem.updatedAt) {
            payload["updatedAt"] = newItem.updatedAt
        }
        if (oldItem.deletedAt != newItem.deletedAt) {
            payload["deletedAt"] = newItem.deletedAt
        }
        
        return if (payload.isNotEmpty()) payload else null
    }
}

/**
 * DiffUtil.ItemCallback for conversation list in ListAdapter.
 */
class ConversationItemCallback : DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.conversationId == newItem.conversationId
    }

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.conversationId == newItem.conversationId &&
               oldItem.updatedAt == newItem.updatedAt &&
               oldItem.unreadMessageCount == newItem.unreadMessageCount &&
               oldItem.lastMessage?.id == newItem.lastMessage?.id
    }
}

/**
 * DiffUtil.ItemCallback for message list in ListAdapter.
 */
class MessageItemCallback : DiffUtil.ItemCallback<BaseMessage>() {
    override fun areItemsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
        return oldItem.id == newItem.id &&
               oldItem.sentAt == newItem.sentAt &&
               oldItem.updatedAt == newItem.updatedAt &&
               oldItem.deletedAt == newItem.deletedAt
    }
}
