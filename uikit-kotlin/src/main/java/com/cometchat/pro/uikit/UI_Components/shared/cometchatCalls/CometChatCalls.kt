package com.cometchat.pro.uikit.ui_components.shared.cometchatCalls

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.ClickListener
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener

/**
 * Purpose - CometChatCallList class is a subclass of recyclerview and used as component by
 * developer to display list of calls. Developer just need to fetchMessages whose type is ACTION_CALL
 * at their end and pass it to this component to display list of calls. It helps user to create call
 * list easily and saves their time.
 *
 * @see com.cometchat.pro.core.Call
 *
 * Created on - 23rd March 2020
 *
 * Modified on  - 02nd April 2020
 */
@BindingMethods(value = [BindingMethod(type = CometChatCalls::class, attribute = "app:calllist", method = "setCallList")])
class CometChatCalls : RecyclerView {
    private var c: Context? = null
    private var callViewModel: CometChatCallsViewModel? = null

    constructor(context: Context) : super(context) {
        this.c = context
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.c = context
        setViewModel()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        this.c = context
        setViewModel()
    }

    private fun setViewModel() {
        if (callViewModel == null) callViewModel = CometChatCallsViewModel(context, this)
    }

    /**
     * This method set the fetched list into the CometChatCallList Component.
     *
     * @param callList to set into the view CometChatCallList
     */
    fun setCallList(callList: List<BaseMessage?>?) {
        if (callViewModel != null) callViewModel!!.setCallList(callList)
    }

    /**
     * This methods updates the call item or add if not present in the list
     *
     *
     * @param call to be added or updated
     */
    fun update(call: Call?) {
        if (callViewModel != null) callViewModel!!.update(call)
    }

    /**
     * provide way to remove a particular call from the list
     *
     * @param call to be removed
     */
    fun remove(call: Call?) {
        if (callViewModel != null) callViewModel!!.remove(call)
    }

    /**
     * This method helps to get Click events of CometChatCallList
     *
     * @param onItemClickListener object of the OnItemClickListener
     */
    fun setItemClickListener(onItemClickListener: OnItemClickListener<Call?>?) {
        addOnItemTouchListener(RecyclerTouchListener(context, this, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val call = var1.getTag(R.string.call) as Call
                var1.findViewById<View>(R.id.user_detail_vw).setOnClickListener { if (onItemClickListener != null) onItemClickListener.OnItemClick(call, var2) else throw NullPointerException("OnItemClickListener<Call> is null") }
            }

            override fun onLongClick(var1: View, var2: Int) {
                val call = var1.getTag(R.string.call) as Call
                if (onItemClickListener != null) onItemClickListener.OnItemLongClick(call, var2) else throw NullPointerException("OnItemClickListener<Call> is null")
            }
        }))
    }

    /**
     * This method helps to get Click events of CometChatCallList
     *
     * @param onItemClickListener object of the OnItemClickListener
     */
    fun setItemCallClickListener(onItemClickListener: OnItemClickListener<Call?>?) {
        addOnItemTouchListener(RecyclerTouchListener(context, this, object : ClickListener() {
            override fun onClick(var1: View, var2: Int) {
                val uid: String
                val type: String
                val call = var1.getTag(R.string.call) as Call
                if (call.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    uid = if ((call.callInitiator as User).uid == CometChat.getLoggedInUser().uid) {
                        (call.callReceiver as User).uid
                    } else {
                        (call.callInitiator as User).uid
                    }
                    type = CometChatConstants.RECEIVER_TYPE_USER
                } else {
                    uid = (call.callReceiver as Group).guid
                    type = CometChatConstants.RECEIVER_TYPE_GROUP
                }
                var1.findViewById<View>(R.id.call_iv).setOnClickListener {
                    val callObj = Call(uid, type, CometChatConstants.CALL_TYPE_AUDIO)
                    if (onItemClickListener != null) onItemClickListener.OnItemClick(callObj, var2) else throw NullPointerException("OnItemClickListener<Call> is null")
                }
            }

            override fun onLongClick(var1: View, var2: Int) {}
        }))
    }

    fun size(): Int {
        return callViewModel!!.size()
    }
}