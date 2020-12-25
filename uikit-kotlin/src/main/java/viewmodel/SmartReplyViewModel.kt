package viewmodel

import adapter.SmartReplyListAdapter
import android.content.Context
import com.cometchat.pro.uikit.SmartReplyList

class SmartReplyViewModel(private val context: Context, private val smartReplyList: SmartReplyList) {
    private var smartReplyListAdapter: SmartReplyListAdapter? = null
    private fun setSmartReplyAdapter(smartReplyList: SmartReplyList) {
        smartReplyListAdapter = SmartReplyListAdapter(context)
        smartReplyList.adapter = smartReplyListAdapter
    }

    private val adapter: SmartReplyListAdapter
        private get() {
            if (smartReplyListAdapter == null) {
                smartReplyListAdapter = SmartReplyListAdapter(context)
            }
            return smartReplyListAdapter!!
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