package com.cometchat.pro.uikit.ui_components.shared.cometchatConversations

import android.content.Context
import android.util.Log
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.MessageReceipt

class CometChatConversationsViewModel(context: Context?, cometChatConversation: CometChatConversation) {
    private var context: Context? = null
    private var cometChatConversationsAdapter: CometChatConversationsAdapter? = null

    init {
        this.context = context
        setAdapter(cometChatConversation)
    }

    private val adapter: CometChatConversationsAdapter
        private get() {
            if (cometChatConversationsAdapter == null) {
                cometChatConversationsAdapter = CometChatConversationsAdapter(context!!)
            }
            return cometChatConversationsAdapter!!
        }

    fun add(conversation: Conversation?) {
        if (cometChatConversationsAdapter != null) cometChatConversationsAdapter!!.add(conversation!!)
    }

    private fun setAdapter(cometChatConversation: CometChatConversation) {
        cometChatConversationsAdapter = CometChatConversationsAdapter(context!!)
        cometChatConversation.adapter = cometChatConversationsAdapter
    }

    fun setConversationList(conversationList: List<Conversation>?) {
        if (cometChatConversationsAdapter != null) {
            cometChatConversationsAdapter!!.updateList(conversationList!!)
        } else {
            Log.e("ERROR", "setConversationList: ERROR ")
        }
    }

    fun update(conversation: Conversation?) {
        if (cometChatConversationsAdapter != null) cometChatConversationsAdapter!!.update(conversation!!)
    }

    fun remove(conversation: Conversation?) {
        if (cometChatConversationsAdapter != null) cometChatConversationsAdapter!!.remove(conversation)
    }

    fun searchConversation(searchString: String?) {
        if (cometChatConversationsAdapter != null) cometChatConversationsAdapter!!.filter.filter(searchString)
    }

    fun setDeliveredReceipts(messageReceipt: MessageReceipt?) {
        if (cometChatConversationsAdapter != null) cometChatConversationsAdapter!!.setDeliveredReceipts(messageReceipt!!)
    }

    fun setReadReceipts(messageReceipt: MessageReceipt?) {
        if (cometChatConversationsAdapter != null) cometChatConversationsAdapter!!.setReadReceipts(messageReceipt!!)
    }

    fun clear() {
        if (cometChatConversationsAdapter != null) cometChatConversationsAdapter!!.resetAdapterList()
    }

    fun size(): Int {
        return cometChatConversationsAdapter!!.itemCount
    }
}