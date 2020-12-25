package com.cometchat.pro.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.uikit.CometChatUserList
import listeners.ClickListener
import listeners.OnItemClickListener
import listeners.RecyclerTouchListener
import viewmodel.SmartReplyViewModel

/**
 * Purpose - SmartReply class is a subclass of recyclerview and used as component by
 * developer to display Smart Reply in his message list. Developer just need to pass the list of reply at their end
 * recieved at their end. It helps user show smart reply at thier end easily.
 *
 *
 * Created on - 23th January 2020
 *
 * Modified on  - 23rd March 2020
 *
 */
@BindingMethods(value = [BindingMethod(type = CometChatUserList::class, attribute = "app:replylist", method = "setSmartReplyList")])
class SmartReplyList : RecyclerView {
    private var c: Context? = null
    private var smartReplyViewModel: SmartReplyViewModel? = null

    constructor(context: Context) : super(context) {
        setContext(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setContext(context)
        getAttributes(attrs)
        setSmartReplyViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setContext(context)
        getAttributes(attrs)
        setSmartReplyViewModel()
    }

    private fun getAttributes(attributeSet: AttributeSet?) {
        val a = getContext().theme.obtainStyledAttributes(attributeSet, R.styleable.SmartReplyList, 0, 0)
    }

    private fun setContext(context: Context) {
        this.c = context
    }

    private fun setSmartReplyViewModel() {
        if (smartReplyViewModel == null) {
            smartReplyViewModel = SmartReplyViewModel(context!!, this)
        }
    }

    /**
     * This method is used to set list of replies in SmartReplyComponent.
     * @param replyList is object of List<String> . It is list of smart replies.
    </String> */
    fun setSmartReplyList(replyList: List<String>?) {
        if (smartReplyViewModel != null) {
            smartReplyViewModel!!.setSmartReplyList(replyList)
        }
    }

    /**
     * This method is used to give events on click of item in given smart reply list.
     * @param itemClickListener
     */
    fun setItemClickListener(itemClickListener: OnItemClickListener<String?>?) {
        addOnItemTouchListener(RecyclerTouchListener(context, this, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val reply = var1.getTag(R.string.replyTxt) as String
                if (itemClickListener != null) itemClickListener.OnItemClick(reply, var2) else throw NullPointerException(resources.getString(R.string.smart_reply_itemclick_error))
            }

            override fun onLongClick(var1: View, var2: Int) {
                val reply = var1.getTag(R.string.replyTxt) as String
                if (itemClickListener != null) itemClickListener.OnItemLongClick(reply, var2) else throw NullPointerException(resources.getString(R.string.smart_reply_itemclick_error))
            }
        }))
    }
}