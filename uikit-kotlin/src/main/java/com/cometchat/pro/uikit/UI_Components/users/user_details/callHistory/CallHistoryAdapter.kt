package com.cometchat.pro.uikit.ui_components.users.user_details.callHistory

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.databinding.CometchatCallHistoryItemBinding
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import java.util.*

/**
 * Purpose - CallHistoryAdapter is a subclass of RecyclerView Adapter which is used to display
 * the list of calls. It helps to organize the list data in recyclerView.
 *
 * Created on - 23rd March 2020
 *
 * Modified on  - 24th March 2020
 *
 */
class CallHistoryAdapter(context: Context, callList: List<BaseMessage>): RecyclerView.Adapter<CallHistoryAdapter.CallViewHolder>() {
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
        updateList(callList!!)
        this.context = context
        fontUtils = FontUtils.getInstance(context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val callHistoryRowBinding: CometchatCallHistoryItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cometchat_call_history_item, parent, false)
        return CallViewHolder(callHistoryRowBinding)
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
        var isIncoming = false
        var isVideo = false
        var isMissed = false
        var callMessageText = ""
        if (call.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
            if (call.sender.uid == loggedInUser) {
                if (call.callStatus == CometChatConstants.CALL_STATUS_UNANSWERED) {
                    callMessageText = context.resources.getString(R.string.missed_call)
                    isMissed = true
                } else if (call.callStatus == CometChatConstants.CALL_STATUS_REJECTED) {
                    callMessageText = context.resources.getString(R.string.rejected_call)
                } else callMessageText = context.resources.getString(R.string.outgoing)
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
        }
        //        else {
//            if (call.getSender().getUid().equals(loggedInUser))
//            {
//                callMessageText = context.getResources().getString(R.string.outgoing);
//                isIncoming = false;
//            }
//            else
//            {
//                callMessageText = context.getResources().getString(R.string.incoming);
//                isIncoming = true;
//            }
//        }
        if (call.type == CometChatConstants.CALL_TYPE_VIDEO) {
            callMessageText = callMessageText + " " + context.resources.getString(R.string.video_call)
            isVideo = true
        } else {
            callMessageText = callMessageText + " " + context.resources.getString(R.string.audio_call)
            isVideo = false
        }
        if (isVideo) {
            callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_videocam_24dp, 0, 0, 0)
        } else {
            if (isIncoming && isMissed) {
                callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_incoming_24dp, 0, 0, 0)
            } else if (isIncoming && !isMissed) {
                callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_incoming_24dp, 0, 0, 0)
            } else if (!isIncoming && isMissed) {
                callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_outgoing_24dp, 0, 0, 0)
            } else {
                callViewHolder.callHistoryRowBinding.callInfoTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_outgoing_24dp, 0, 0, 0)
            }
        }
        callViewHolder.callHistoryRowBinding.callTimeTv.text = Utils.getHeaderDate(call.initiatedAt * 1000)
        callViewHolder.callHistoryRowBinding.callInfoTv.text = callMessageText
        callViewHolder.callHistoryRowBinding.callDateTv.text = Utils.getDate(call.sentAt * 1000L)
        //        callViewHolder.callListRowBinding.executePendingBindings();
//        callType = call.getType();
//        callCategory = call.getCategory();
//        callViewHolder.callListRowBinding.callMessage.setText(call.getAction());
//        callViewHolder.callListRowBinding.callMessage.setTypeface(fontUtils.getTypeFace(FontUtils.robotoRegular));
//        callViewHolder.callListRowBinding.callSenderName.setTypeface(fontUtils.getTypeFace(FontUtils.robotoMedium));
//        if (conversation.getConversationType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
//            name = ((User) conversation.getConversationWith()).getName();
//            avatar = ((User) conversation.getConversationWith()).getAvatar();
//        } else {
//            name = ((Group) conversation.getConversationWith()).getName();
//            avatar = ((Group) conversation.getConversationWith()).getIcon();
//        }
//
//        conversationViewHolder.conversationListRowBinding.messageCount.setCount(conversation.getUnreadMessageCount());
//        conversationViewHolder.conversationListRowBinding.txtUserName.setText(name);
//        conversationViewHolder.conversationListRowBinding.avUser.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
//
//        if (avatar != null && !avatar.isEmpty()) {
//            conversationViewHolder.conversationListRowBinding.avUser.setAvatar(avatar);
//        } else {
//            conversationViewHolder.conversationListRowBinding.avUser.setInitials(name);
//        }
        callViewHolder.callHistoryRowBinding.root.setTag(R.string.call, call)
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
    fun updateList(calls: List<BaseMessage>) {
        callList!!.addAll(filterList(calls))
        notifyDataSetChanged()
    }

    private fun filterList(messageList: List<BaseMessage>): List<BaseMessage> {
        val filteredList = ArrayList<BaseMessage>()
        for (baseMessage in messageList) {
            if ((baseMessage as Call).callStatus == CometChatConstants.CALL_STATUS_INITIATED || baseMessage.callStatus == CometChatConstants.CALL_STATUS_UNANSWERED) {
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
        val position:Int  = callList!!.indexOf(call!!)
        callList!!.remove(call)
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
        callList?.add(call)
    }

    /**
     * This method is used to reset the adapter by clearing filterConversationList.
     */
    fun resetAdapterList() {
        callList!!.clear()
        notifyDataSetChanged()
    }

    inner class CallViewHolder(var callHistoryRowBinding: CometchatCallHistoryItemBinding) : RecyclerView.ViewHolder(callHistoryRowBinding.root)
}