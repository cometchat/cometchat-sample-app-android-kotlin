package com.cometchat.pro.uikit.ui_components.shared.cometchatCalls

import android.content.Context
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.BaseMessage

class CometChatCallsViewModel(context: Context?, cometChatCallList: CometChatCalls?) {
    private var context: Context? = null
    private var cometChatCallsAdapter: CometChatCallsAdapter? = null
    private var callListView: CometChatCalls? = null

    init {
        callListView = cometChatCallList
        this.context = context
        setAdapter()
    }

    private val adapter: CometChatCallsAdapter?
        private get() {
            if (cometChatCallsAdapter == null) {
                cometChatCallsAdapter = CometChatCallsAdapter(context!!)
            }
            return cometChatCallsAdapter
        }

    fun add(call: Call?) {
        if (cometChatCallsAdapter != null) cometChatCallsAdapter!!.add(call!!)
    }

    private fun setAdapter() {
        cometChatCallsAdapter = CometChatCallsAdapter(context!!)
        callListView!!.adapter = cometChatCallsAdapter
    }

    fun setCallList(callList: List<BaseMessage?>?) {
        if (cometChatCallsAdapter != null) cometChatCallsAdapter!!.updateList(callList!!)
    }

    fun update(call: Call?) {
        if (cometChatCallsAdapter != null) cometChatCallsAdapter!!.update(call!!)
    }

    fun remove(call: Call?) {
        if (cometChatCallsAdapter != null) cometChatCallsAdapter!!.remove(call)
    }

    fun size(): Int {
        return cometChatCallsAdapter!!.getItemCount()
    }
}