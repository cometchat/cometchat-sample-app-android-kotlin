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
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.MediaMessage
import com.cometchat.pro.models.TextMessage
import com.cometchat.pro.models.User
import com.inscripts.cometchatpulse.Fragment.OneToOneFragment
import com.inscripts.cometchatpulse.Repository.MessageRepository
import org.json.JSONObject
import java.io.File

class  OnetoOneViewModel  constructor(application: Application) : AndroidViewModel(application) {

    private val messageRepository: MessageRepository

    val messageList: MutableLiveData<MutableList<BaseMessage>>

     var user:MutableLiveData<User>



    init {
        messageRepository = MessageRepository()
        messageList = messageRepository.onetoOneMessageList
        user=messageRepository.user

    }

    fun fetchMessage(LIMIT: Int,userId: String) {
        messageRepository.fetchMessage(LIMIT, userId)
    }


    override fun onCleared() {
        super.onCleared()

    }

    fun sendTextMessage(textMessage: TextMessage) {
        messageRepository.sendTextMessage(textMessage)
    }


    fun receiveMessageListener(listener:String,ownerId: String) {
          messageRepository.messageReceiveListener(listener,ownerId)
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

    fun sendMediaMessage(filePath:String?,type:String?,userId: String,oneToOneFragment:OneToOneFragment) {

        val path = File(filePath)
        Log.d("MediaMessage", " " + path.exists())

        val mediaMessage = MediaMessage(userId, path, type, CometChatConstants.RECEIVER_TYPE_USER)
        val jObject= JSONObject()
        jObject.put("path",filePath)

        Log.d("meta",jObject.toString())

          if (OneToOneFragment.isReply){
              OneToOneFragment.isReply=false
              mediaMessage.metadata= OneToOneFragment.metaData?.put("path",filePath)
              oneToOneFragment.hideReplyContainer()
          }
        else{
              mediaMessage.metadata=jObject
          }

        messageRepository.sendMediaMessage(mediaMessage)
    }

     fun addCallListener(context: Context,call_event_listener: String,view:RelativeLayout?) {
          messageRepository.addCallListener(context,call_event_listener, view)
     }

     fun removeCallListener(call_event_listener: String) {
        messageRepository.removeCallListener(call_event_listener)
     }

     fun acceptCall(sessionID: String,view:RelativeLayout,activity: Activity) {
         messageRepository.acceptCall(sessionID,view,activity)
     }

     fun rejectCall(sessionID: String, call_status_rejected: String,activity: Activity) {
         messageRepository.rejectCall(sessionID,call_status_rejected,activity)
     }

     fun initCall(context: Context,userId: String, receiver_type: String, callType: String) {
         val call=Call(userId,receiver_type,callType)
         messageRepository.initiateCall(call,context)
     }

 }