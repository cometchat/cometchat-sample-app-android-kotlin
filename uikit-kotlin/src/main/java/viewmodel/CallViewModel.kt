package viewmodel

import adapter.CallListAdapter
import android.content.Context
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.uikit.CometChatCallList

class CallViewModel(context: Context?,cometChatCallList: CometChatCallList?) {
    private var context: Context? = null
    private var callListAdapter: CallListAdapter? = null
    private var callListView: CometChatCallList? = null

    init {
        callListView = cometChatCallList
        this.context = context
        setAdapter()
    }

    private val adapter: CallListAdapter?
        private get() {
            if (callListAdapter == null) {
                callListAdapter = CallListAdapter(context!!)
            }
            return callListAdapter
        }

    fun add(call: Call?) {
        if (callListAdapter != null) callListAdapter!!.add(call!!)
    }

    private fun setAdapter() {
        callListAdapter = CallListAdapter(context!!)
        callListView!!.adapter = callListAdapter
    }

    fun setCallList(callList: List<BaseMessage?>?) {
        if (callListAdapter != null) callListAdapter!!.updateList(callList!!)
    }

    fun update(call: Call?) {
        if (callListAdapter != null) callListAdapter!!.update(call!!)
    }

    fun remove(call: Call?) {
        if (callListAdapter != null) callListAdapter!!.remove(call)
    }

    fun size(): Int {
        return callListAdapter!!.getItemCount()
    }
}