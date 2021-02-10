package com.cometchat.pro.uikit.ui_components.shared.cometchatSmartReplies

import android.content.Context

class SmartReplyViewModel(private val context: Context, private val smartReplyList: CometChatSmartReply) {
    private var smartRepliesAdapter: SmartRepliesAdapter? = null
    private fun setSmartReplyAdapter(smartReplyList: CometChatSmartReply) {
        smartRepliesAdapter = SmartRepliesAdapter(context)
        smartReplyList.adapter = smartRepliesAdapter
    }

    private val adapter: SmartRepliesAdapter
        private get() {
            if (smartRepliesAdapter == null) {
                smartRepliesAdapter = SmartRepliesAdapter(context)
            }
            return smartRepliesAdapter!!
        }

    fun setSmartReplyList(replyList: List<String>?) {
        adapter.updateList(replyList!!)
    }

    companion object {
        private const val TAG = "SmartReplyViewModel"
    }

    init {
        setSmartReplyAdapter(smartReplyList)
    }
}