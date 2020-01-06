package com.inscripts.cometchatpulse.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Fragment.GroupFragment
import com.inscripts.cometchatpulse.Fragment.MemberFragment
import com.inscripts.cometchatpulse.Repository.GroupRepository
import com.inscripts.cometchatpulse.Repository.MessageRepository
import org.json.JSONObject
import java.io.File


class GroupChatViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepository: GroupRepository = GroupRepository()

    private val messageRepository: MessageRepository

    val messageList: MutableLiveData<MutableList<BaseMessage>>

    val filterMessageList: MutableLiveData<MutableList<BaseMessage>>

    val groupMemberList: MutableLiveData<MutableMap<String,GroupMember>>

    val banMemberList: MutableLiveData<MutableMap<String,GroupMember>>

    var liveReadReceipts: MutableLiveData<MessageReceipt>

    var liveDeliveryReceipts: MutableLiveData<MessageReceipt>

    var liveEditMessage:MutableLiveData<BaseMessage>

    var liveDeletedMessage:MutableLiveData<BaseMessage>

    var liveStartTypingIndicator: MutableLiveData<TypingIndicator>

    var liveEndTypingIndicator: MutableLiveData<TypingIndicator>


    init {
        messageRepository = MessageRepository()
        banMemberList = groupRepository.banMemberLiveData
        messageList = messageRepository.groupMessageList
        groupMemberList = groupRepository.groupMemberLiveData
        liveReadReceipts = messageRepository.liveReadReceipts
        liveDeliveryReceipts = messageRepository.liveDeliveryReceipts
        liveDeletedMessage=messageRepository.liveMessageDeleted
        liveEditMessage=messageRepository.liveMessageEdited
        liveStartTypingIndicator = messageRepository.liveStartTypingIndicator
        liveEndTypingIndicator = messageRepository.liveEndTypingIndicator
        filterMessageList=messageRepository.filterLivegroupMessageList
    }

    fun fetchMessage(LIMIT: Int, guid: String) {
        messageRepository.fetchGroupMessage(guid, LIMIT)
    }

    override fun onCleared() {
        super.onCleared()

    }

    fun sendTextMessage(textMessage: TextMessage) {
        messageRepository.sendTextMessage(textMessage)
    }

    fun addGroupEventListener(group_event_listener: String) {
        messageRepository.addGroupListener(group_event_listener)
    }

    fun sendMediaMessage(path: String?, type: String?, guid: String,groupFragment:GroupFragment) {
        val mediaMessage = MediaMessage(guid, File(path), type, CometChatConstants.RECEIVER_TYPE_GROUP)
        val jObject = JSONObject()
        jObject.put("path", path)
        mediaMessage.metadata = jObject

        if (GroupFragment.isReply){
            GroupFragment.isReply=false
            mediaMessage.metadata= GroupFragment.metaData?.put("path",path)
            groupFragment.hideReplyContainer()
        }
        else{
            mediaMessage.metadata=jObject
        }
        messageRepository.sendMediaMessage(mediaMessage)
    }

    fun removeGroupEventListener(group_event_listener: String) {
        messageRepository.removeGroupListener(group_event_listener)
    }



    fun getMembers(guid: String, LIMIT: Int) {
        groupRepository.getGroupMember(guid, LIMIT)
    }

    fun getBanedMember(guid: String, LIMIT: Int) {
        groupRepository.getBannedMember(guid, LIMIT)
    }

    fun banMember(uid: String?, guid: String) {
        uid?.let { groupRepository.banMember(it, guid) }
    }

    fun kickMember(uid: String?, guid: String) {
        uid?.let { groupRepository.kickMember(it, guid) }
    }

    fun unbanMember(uid: String?, guid: String) {
        uid?.let { groupRepository.unBanMember(guid, it) }
    }

    fun addGroupMessageListener(message_listener: String,guid:String) {
       messageRepository.messageReceiveListener(message_listener,guid)
    }

    fun removeMessageListener(message_listener: String) {
        messageRepository.removeMessageListener(message_listener)
    }

    fun removeCallListener(call_event_listener: String) {
        messageRepository.removeCallListener(call_event_listener)
    }

    fun updateScope(fragment:MemberFragment,uid: String, guid: String,scope:String) {
        groupRepository.updateScope(fragment,uid,guid,scope)
    }

    fun deleteMessage(textMessage: TextMessage) {
        messageRepository.deleteMessage(textMessage)
    }

    fun sendEditMessage(baseMessage: BaseMessage, messageText: String) {
         messageRepository.editMessage(baseMessage,messageText)
    }

    fun sendTypingIndicator(uid: String?,isEndTyping:Boolean=false) {
        val typingIndicator = uid?.let { TypingIndicator(it, CometChatConstants.RECEIVER_TYPE_GROUP) }
        typingIndicator?.let { messageRepository.sendTypingIndicator(it, isEndTyping) }
    }

    fun searchMessage(s: String, guid: String) {

        messageRepository.searchGroupMessage(s,guid)
    }

}