package viewmodel

import adapter.ReceiptListAdapter
import android.content.Context
import com.cometchat.pro.models.MessageReceipt
import com.cometchat.pro.uikit.CometChatReceiptsList

class ReceiptListViewModel {
    private val TAG = "ReceiptListViewModel"

    private var context: Context? = null

    private var receiptListAdapter: ReceiptListAdapter? = null

    private var receiptsList: CometChatReceiptsList? = null

    constructor(context: Context?, receiptsList: CometChatReceiptsList?,
                showDelivery: Boolean, showRead: Boolean) {
        this.receiptsList = receiptsList
        this.context = context
        setReceiptListAdapter(receiptsList, showDelivery, showRead)
    }

    constructor()

    private fun getAdapter(): ReceiptListAdapter? {
        if (receiptListAdapter == null) {
            receiptListAdapter = ReceiptListAdapter(context)
        }
        return receiptListAdapter
    }

    fun add(messageReceipt: MessageReceipt?) {
        if (receiptListAdapter != null) receiptListAdapter!!.add(messageReceipt)
    }
    fun add(index: Int, messageReceipt: MessageReceipt?) {
        if (receiptListAdapter != null) receiptListAdapter!!.addAtIndex(index, messageReceipt)
    }
    fun update(messageReceipt: MessageReceipt?) {
        if (receiptListAdapter != null) receiptListAdapter!!.updateReceipts(messageReceipt)
    }
    fun clear() {
        if (receiptListAdapter != null) receiptListAdapter!!.clear()
    }

    private fun setReceiptListAdapter(cometChatReceiptsList: CometChatReceiptsList?,
                                      showDelivery: Boolean, showRead: Boolean) {
        receiptListAdapter = ReceiptListAdapter(context)
        cometChatReceiptsList!!.showDelivery(showDelivery)
        cometChatReceiptsList!!.showRead(showRead)
        cometChatReceiptsList!!.adapter = receiptListAdapter
    }

    fun setReceiptList(messageReceiptsList: List<MessageReceipt>) {
        getAdapter()!!.updateList(messageReceiptsList)
    }
    fun update(index: Int, messageReceipt: MessageReceipt?) {
        if (receiptListAdapter != null) receiptListAdapter!!.updateReceiptsAtIndex(index, messageReceipt)
    }
}