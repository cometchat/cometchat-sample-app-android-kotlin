package com.inscripts.cometchatpulse.ViewModel

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import android.widget.RelativeLayout
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Fragment.OneToOneFragment
import com.inscripts.cometchatpulse.Repository.MessageRepository
import com.inscripts.cometchatpulse.Repository.UserRepository
import org.json.JSONObject
import java.io.File
import java.util.ArrayList

class OnetoOneViewModel constructor(application: Application) : AndroidViewModel(application) {

    private val messageRepository: MessageRepository = MessageRepository()

    private val userRepository:UserRepository = UserRepository()

    val messageList: MutableLiveData<MutableList<BaseMessage>>

    var user: MutableLiveData<User>

    var liveStartTypingIndicator: MutableLiveData<TypingIndicator>

    var liveEndTypingIndicator: MutableLiveData<TypingIndicator>

    var liveReadReceipts: MutableLiveData<MessageReceipt>

    var liveDeliveryReceipts: MutableLiveData<MessageReceipt>

    var liveEditMessage:MutableLiveData<BaseMessage>

    var livefilter:MutableLiveData<MutableList<BaseMessage>>

    var liveDeletedMessage:MutableLiveData<BaseMessage>

    init {
        messageList = messageRepository.onetoOneMessageList
        liveStartTypingIndicator = messageRepository.liveStartTypingIndicator
        liveEndTypingIndicator = messageRepository.liveEndTypingIndicator
        liveReadReceipts = messageRepository.liveReadReceipts
        liveDeliveryReceipts = messageRepository.liveDeliveryReceipts
        liveDeletedMessage=messageRepository.liveMessageDeleted
        liveEditMessage=messageRepository.liveMessageEdited
        livefilter=messageRepository.filterLiveonetoOneMessageList


        user = messageRepository.user

    }

    fun fetchMessage(LIMIT: Int, userId: String) {
        messageRepository.fetchMessage(LIMIT, userId)
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun sendTextMessage(textMessage: TextMessage) {
        messageRepository.sendTextMessage(textMessage)
    }


    fun receiveMessageListener(listener: String, ownerId: String) {
        messageRepository.messageReceiveListener(listener)
    }

    fun removeMessageListener(listener: String) {
        messageRepository.removeMessageListener(listener)
    }

    fun addPresenceListener(listener: String) {
        messageRepository.addPresenceListener(listener)
    }

    fun removePresenceListener(listener: String) {
        messageRepository.removePresenceListener(listener)
    }

    fun sendMediaMessage(filePath: String?, type: String?, userId: String, oneToOneFragment: OneToOneFragment) {

        val path = File(filePath)
        Log.d("MediaMessage", " " + path.exists())

        val mediaMessage = MediaMessage(userId, path, type, CometChatConstants.RECEIVER_TYPE_USER)

        val jObject = JSONObject()

        jObject.put("path", filePath)

        Log.d("meta", jObject.toString())

        if (OneToOneFragment.isReply) {
            OneToOneFragment.isReply = false
            mediaMessage.metadata = OneToOneFragment.metaData?.put("path", filePath)
            oneToOneFragment.hideReplyContainer()
        } else {
            mediaMessage.metadata = jObject
        }
        messageRepository.sendMediaMessage(mediaMessage)
    }

    fun addCallListener(context: Context, call_event_listener: String, view: RelativeLayout?) {
        messageRepository.addCallListener(context, call_event_listener, view)
    }

    fun removeCallListener(call_event_listener: String) {
        messageRepository.removeCallListener(call_event_listener)
    }

    fun acceptCall(sessionID: String, view: RelativeLayout, activity: Activity) {
        messageRepository.acceptCall(sessionID, view, activity)
    }

    fun rejectCall(sessionID: String, call_status_rejected: String, activity: Activity) {
        messageRepository.rejectCall(sessionID, call_status_rejected, activity)
    }

    fun initCall(context: Context, userId: String, receiver_type: String, callType: String) {
        val call = Call(userId, receiver_type, callType)
        messageRepository.initiateCall(call, context)
    }

    fun sendTypingIndicator(userId: String, isEndTyping: Boolean = false) {
        val typingIndicator = TypingIndicator(userId, CometChatConstants.RECEIVER_TYPE_USER)
        messageRepository.sendTypingIndicator(typingIndicator, isEndTyping)
    }

    fun blockUser(userId: String) {
        val uidList:MutableList<String> = ArrayList()
          uidList.add(userId)
        userRepository.blockUser(uidList)

    }

    fun deleteMessage(textMessage:TextMessage) {
       messageRepository.deleteMessage(textMessage)
    }

    fun sendEditMessage(any: BaseMessage, messageText: String) {
        messageRepository.editMessage(any,messageText)
    }

    fun searchMessage(s: String, userId: String) {
        messageRepository.searchMessage(s,userId)
    }

}