package com.cometchat.pro.uikit.ui_components.messages.message_information.message_receipts

import android.content.Context
import com.cometchat.pro.models.MessageReceipt

class CometChatReceiptViewModel {
    private val TAG = "ReceiptListViewModel"

    private var context: Context? = null

    private var cometChatReceiptAdapter: CometChatReceiptAdapter? = null

    private var receiptsList: CometChatReceiptsList? = null

    constructor(context: Context?, receiptsList: CometChatReceiptsList?,
                showDelivery: Boolean, showRead: Boolean) {
        this.receiptsList = receiptsList
        this.context = context
        setReceiptListAdapter(receiptsList, showDelivery, showRead)
    }

    constructor()

    private fun getAdapter(): CometChatReceiptAdapter? {
        if (cometChatReceiptAdapter == null) {
            cometChatReceiptAdapter = CometChatReceiptAdapter(context)
        }
        return cometChatReceiptAdapter
    }

    fun add(messageReceipt: MessageReceipt?) {
        if (cometChatReceiptAdapter != null) cometChatReceiptAdapter!!.add(messageReceipt)
    }
    fun add(index: Int, messageReceipt: MessageReceipt?) {
        if (cometChatReceiptAdapter != null) cometChatReceiptAdapter!!.addAtIndex(index, messageReceipt)
    }
    fun update(messageReceipt: MessageReceipt?) {
        if (cometChatReceiptAdapter != null) cometChatReceiptAdapter!!.updateReceipts(messageReceipt)
    }
    fun clear() {
        if (cometChatReceiptAdapter != null) cometChatReceiptAdapter!!.clear()
    }

    private fun setReceiptListAdapter(cometChatReceiptsList: CometChatReceiptsList?,
                                      showDelivery: Boolean, showRead: Boolean) {
        cometChatReceiptAdapter = CometChatReceiptAdapter(context)
        cometChatReceiptsList!!.showDelivery(showDelivery)
        cometChatReceiptsList!!.showRead(showRead)
        cometChatReceiptsList!!.adapter = cometChatReceiptAdapter
    }

    fun setReceiptList(messageReceiptsList: List<MessageReceipt>) {
        getAdapter()!!.updateList(messageReceiptsList)
    }
    fun update(index: Int, messageReceipt: MessageReceipt?) {
        if (cometChatReceiptAdapter != null) cometChatReceiptAdapter!!.updateReceiptsAtIndex(index, messageReceipt)
    }
}