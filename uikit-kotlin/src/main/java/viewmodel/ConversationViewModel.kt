package viewmodel

import adapter.ConversationListAdapter
import android.content.Context
import android.util.Log
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.MessageReceipt
import com.cometchat.pro.uikit.CometChatConversationList

class ConversationViewModel(context: Context?, cometChatConversationList: CometChatConversationList) {
    private var context: Context? = null
    private var conversationListAdapter: ConversationListAdapter? = null

    init {
        this.context = context
        setAdapter(cometChatConversationList)
    }

    private val adapter: ConversationListAdapter
        private get() {
            if (conversationListAdapter == null) {
                conversationListAdapter = ConversationListAdapter(context!!)
            }
            return conversationListAdapter!!
        }

    fun add(conversation: Conversation?) {
        if (conversationListAdapter != null) conversationListAdapter!!.add(conversation!!)
    }

    private fun setAdapter(cometChatConversationList: CometChatConversationList) {
        conversationListAdapter = ConversationListAdapter(context!!)
        cometChatConversationList.adapter = conversationListAdapter
    }

    fun setConversationList(conversationList: List<Conversation>?) {
        if (conversationListAdapter != null) {
            conversationListAdapter!!.updateList(conversationList!!)
        } else {
            Log.e("ERROR", "setConversationList: ERROR ")
        }
    }

    fun update(conversation: Conversation?) {
        if (conversationListAdapter != null) conversationListAdapter!!.update(conversation!!)
    }

    fun remove(conversation: Conversation?) {
        if (conversationListAdapter != null) conversationListAdapter!!.remove(conversation)
    }

    fun searchConversation(searchString: String?) {
        if (conversationListAdapter != null) conversationListAdapter!!.filter.filter(searchString)
    }

    fun setDeliveredReceipts(messageReceipt: MessageReceipt?) {
        if (conversationListAdapter != null) conversationListAdapter!!.setDeliveredReceipts(messageReceipt!!)
    }

    fun setReadReceipts(messageReceipt: MessageReceipt?) {
        if (conversationListAdapter != null) conversationListAdapter!!.setReadReceipts(messageReceipt!!)
    }

    fun clear() {
        if (conversationListAdapter != null) conversationListAdapter!!.resetAdapterList()
    }

    fun size(): Int {
        return conversationListAdapter!!.itemCount
    }
}