package com.cometchat.pro.uikit.ui_components.shared.cometchatCalls

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.CometchatCallListItemBinding
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import java.util.*

/**
 * Purpose - CallListAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of calls. It helps to organize the list data in recyclerView.
 *
 * Created on - 23rd March 2020
 *
 * Modified on  - 02nd April 2020
 *
 */
class CometChatCallsAdapter(context: Context) : RecyclerView.Adapter<CometChatCallsAdapter.CallViewHolder>() {
    private var context: Context
    private val callList: MutableList<BaseMessage>? = ArrayList()
    private var fontUtils: FontUtils
    private val loggedInUser = CometChat.getLoggedInUser().uid

    /**
     * It is constructor which takes callList as parameter and bind it with callList in adapter.
     *
     * @param context is a object of Context.
     * @param callList is list of calls used in this adapter.
     */
    init {
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val callListRowBinding: CometchatCallListItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_call_list_item, parent, false)
        return CallViewHolder(callListRowBinding)
    }

    /**
     * This method is used to bind the ConversationViewHolder contents with conversation at given
     * position. It set avatar, name, lastMessage, unreadMessageCount and messageTime of conversation
     * in a respective ConversationViewHolder content. It checks whether conversation type is user
     * or group and set name and avatar as accordingly. It also checks whether last message is text, media
     * or file and modify txtUserMessage view accordingly.
     *
     * @param callViewHolder is a object of ConversationViewHolder.
     * @param position is a position of item in recyclerView.
     *
     * @see Conversation
     */
    override fun onBindViewHolder(callViewHolder: CallViewHolder, position: Int) {
        val baseMessage = callList!![position]
        val call = baseMessage as Call
        var avatar: String
        val uid: String
        val type: String
        val isIncoming: Boolean
        val isVideo: Boolean
        var isMissed = false
        var name: String
        var callMessageText: String
        var callType: String
        var callCategory: String
        if (call.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
            if ((call.callInitiator as User).uid == loggedInUser) {
                val callName = (call.callReceiver as User).name
                callViewHolder.callListRowBinding.callSenderName.text = callName
                callViewHolder.callListRowBinding.callSenderAvatar.setAvatar((call.callReceiver as User).avatar)
                if (call.callStatus == CometChatConstants.CALL_STATUS_UNANSWERED || call.callStatus == CometChatConstants.CALL_STATUS_CANCELLED) {
                    callMessageText = context.resources.getString(R.string.missed_call)
                    isMissed = true
                } else if (call.callStatus == CometChatConstants.CALL_STATUS_REJECTED) {
                    callMessageText = context.resources.getString(R.string.rejected_call)
                } else callMessageText = context.resources.getString(R.string.outgoing)
                uid = (call.callReceiver as User).uid
                isIncoming = false
            } else {
                val callName = (call.callInitiator as User).name
                callViewHolder.callListRowBinding.callSenderName.text = callName
                callViewHolder.callListRowBinding.callSenderAvatar.setAvatar((call.callInitiator as User))
                if (call.callStatus == CometChatConstants.CALL_STATUS_UNANSWERED || call.callStatus == CometChatConstants.CALL_STATUS_CANCELLED) {
                    callMessageText = context.resources.getString(R.string.missed_call)
                    isMissed = true
                } else if (call.callStatus == CometChatConstants.CALL_STATUS_REJECTED) {
                    callMessageText = context.resources.getString(R.string.rejected_call)
                } else callMessageText = context.resources.getString(R.string.incoming)
                uid = call.sender.uid
                isIncoming = true
            }
            type = CometChatConstants.RECEIVER_TYPE_USER
        } else {
            callViewHolder.callListRowBinding.callSenderName.text = (call.callReceiver as Group).name
            callViewHolder.callListRowBinding.callSenderAvatar.setAvatar((call.callReceiver as Group))
            if ((call.callInitiator as User).uid == loggedInUser) {
                if (call.callStatus == CometChatConstants.CALL_STATUS_UNANSWERED) {
                    callMessageText = context.resources.getString(R.string.missed_call)
                    isMissed = true
                } else if (call.callStatus == CometChatConstants.CALL_STATUS_REJECTED) {
                    callMessageText = context.resources.getString(R.string.rejected_call)
                } else callMessageText = context.resources.getString(R.string.incoming)
                isIncoming = false
            } else {
                if (call.callStatus == CometChatConstants.CALL_STATUS_UNANSWERED) {
                    callMessageText = context.resources.getString(R.string.missed_call)
                    isMissed = true
                } else if (call.callStatus == CometChatConstants.CALL_STATUS_REJECTED) {
                    callMessageText = context.resources.getString(R.string.rejected_call)
                } else callMessageText = context.resources.getString(R.string.incoming)
                isIncoming = true
            }
            uid = (call.callReceiver as Group).guid
            type = CometChatConstants.RECEIVER_TYPE_GROUP
        }
        if (call.type == CometChatConstants.CALL_TYPE_VIDEO) {
            callMessageText = callMessageText + " " + context.resources.getString(R.string.video_call)
            callViewHolder.callListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_incoming_video_call, 0, 0, 0)
            isVideo = true
        } else {
            callMessageText = callMessageText + " " + context.resources.getString(R.string.audio_call)
            callViewHolder.callListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_incoming_call, 0, 0, 0)
            isVideo = false
        }
//        if (isVideo) {
//            callViewHolder.callListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_video_call, 0, 0, 0)
//        } else {
//            if (isIncoming && isMissed) {
//                callViewHolder.callListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_incoming_24dp, 0, 0, 0)
//            } else if (isIncoming && !isMissed) {
//                callViewHolder.callListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_incoming_24dp, 0, 0, 0)
//            } else if (!isIncoming && isMissed) {
//                callViewHolder.callListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_outgoing_24dp, 0, 0, 0)
//            } else {
//                callViewHolder.callListRowBinding.callMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_outgoing_24dp, 0, 0, 0)
//            }
//        }
        callViewHolder.callListRowBinding.calltimeTv.text = Utils.getLastMessageDate(call.initiatedAt)
        callViewHolder.callListRowBinding.callMessage.text = callMessageText
        callViewHolder.callListRowBinding.root.setTag(R.string.call, call)

        FeatureRestriction.isOneOnOneAudioCallEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (p0) callViewHolder.callListRowBinding.callIv.visibility = View.VISIBLE else callViewHolder.callListRowBinding.callIv.visibility = View.GONE
            }

        })
        FeatureRestriction.isOneOnOneVideoCallEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (p0) callViewHolder.callListRowBinding.callIv.visibility = View.VISIBLE else callViewHolder.callListRowBinding.callIv.visibility = View.GONE
            }

        })
        callViewHolder.callListRowBinding.callIv.imageTintList = ColorStateList.valueOf(Color.parseColor(UIKitSettings.color))
    }

    override fun getItemCount(): Int {
        return callList!!.size
    }

    /**
     * This method is used to update the callList with new calls and avoid
     * duplicates call entries.
     *
     * @param calls is a list of calls which will be updated in adapter.
     */
    fun updateList(calls: List<BaseMessage?>) {
        callList!!.addAll(filterList(calls))
        notifyDataSetChanged()
    }

    private fun filterList(messageList: List<BaseMessage?>): List<BaseMessage> {
        val filteredList = ArrayList<BaseMessage>()
        for (baseMessage in messageList) {
            if ((baseMessage as Call).callStatus == CometChatConstants.CALL_STATUS_UNANSWERED || baseMessage.callStatus == CometChatConstants.CALL_STATUS_ENDED
                    || baseMessage.callStatus == CometChatConstants.CALL_STATUS_REJECTED
                    || baseMessage.callStatus == CometChatConstants.CALL_STATUS_CANCELLED) {
                filteredList.add(baseMessage)
            }
        }
        return filteredList
    }

    /**
     * This method is used to remove the call from callList
     *
     * @param call is a object of Call.
     *
     * @see Call
     */
    fun remove(call: Call?) {
        val position = callList!!.indexOf(call!!)
        callList.remove(call)
        notifyItemRemoved(position)
    }

    /**
     * This method is used to update call in callList.
     *
     * @param call is an object of Call. It is used to update the previous call
     * in list
     * @see Call
     */
    fun update(call: Call) {
        if (callList!!.contains(call)) {
            val oldCall = callList[callList.indexOf(call)] as Call
            callList.remove(oldCall)
            callList.add(0, call)
        } else {
            callList.add(0, call)
        }
        notifyDataSetChanged()
    }

    /**
     * This method is used to add call in list.
     *
     * @param call is an object of Call. It will be added to callList.
     *
     * @see Call
     */
    fun add(call: Call) {
        if (callList != null) callList.add(call)
    }

    /**
     * This method is used to reset the adapter by clearing filterConversationList.
     */
    fun resetAdapterList() {
        callList!!.clear()
        notifyDataSetChanged()
    }

    inner class CallViewHolder(var callListRowBinding: CometchatCallListItemBinding) : RecyclerView.ViewHolder(callListRowBinding.root)
}