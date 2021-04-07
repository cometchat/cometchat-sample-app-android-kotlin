package com.cometchat.pro.uikit.ui_components.messages.message_information.message_receipts

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.models.MessageReceipt
import com.cometchat.pro.uikit.R

@BindingMethods(value = [BindingMethod(type = CometChatReceiptsList::class, attribute = "app:receiptlist", method = "setReceiptsList")])
class CometChatReceiptsList : RecyclerView {
    private var mContext : Context? = null

    private var cometChatReceiptViewModel: CometChatReceiptViewModel? = null

    private var showDelivery = false

    private var showRead = false

    constructor(context: Context) : super(context){
        mContext = context
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        mContext = context
        getAttributes(attrs)
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        mContext = context
        getAttributes(attrs)
        setViewModel()
    }

    fun setMessageReceiptList(messageReceiptList: List<MessageReceipt>) {
        if (cometChatReceiptViewModel != null) cometChatReceiptViewModel!!.setReceiptList(messageReceiptList)
    }
    private fun setViewModel() {
        if (cometChatReceiptViewModel == null) {
            cometChatReceiptViewModel = CometChatReceiptViewModel(context, this, showDelivery, showRead)
        }
    }

    private fun getAttributes(attributeSet: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attributeSet, R.styleable.CometChatReceiptsList, 0, 0)
        showDelivery = a.getBoolean(R.styleable.CometChatReceiptsList_showDeliveryReceipt, true)
        showRead = a.getBoolean(R.styleable.CometChatReceiptsList_showReadReceipt, true)
    }

    fun add(index: Int, messageReceipt: MessageReceipt?) {
        if (cometChatReceiptViewModel != null) cometChatReceiptViewModel!!.add(index, messageReceipt)
    }
    fun add(messageReceipt: MessageReceipt?) {
        if (cometChatReceiptViewModel != null) cometChatReceiptViewModel!!.add(messageReceipt)
    }
    fun update(messageReceipt: MessageReceipt?) {
        if (cometChatReceiptViewModel != null) cometChatReceiptViewModel!!.update(messageReceipt)
    }
    fun update(index: Int, messageReceipt: MessageReceipt?) {
        if (cometChatReceiptViewModel != null) cometChatReceiptViewModel!!.update(index, messageReceipt)
    }
    fun clear() {
        cometChatReceiptViewModel!!.clear()
    }

    fun showDelivery(showDelivery: Boolean) {
        this.showDelivery = showDelivery
    }

    fun showRead(showRead: Boolean) {
        this.showRead = showRead
    }
}