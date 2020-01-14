package com.inscripts.cometchatpulse.Repository

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import android.util.Log
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.Call
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.*
import com.inscripts.cometchatpulse.Activities.CallActivity
import com.inscripts.cometchatpulse.Activities.LocationActivity
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.Fragment.OneToOneFragment
import com.inscripts.cometchatpulse.Utils.CommonUtil
import com.cometchat.pro.models.MessageReceipt
import com.cometchat.pro.models.MediaMessage
import com.cometchat.pro.models.TextMessage
import com.cometchat.pro.models.BaseMessage
import com.inscripts.cometchatpulse.Activities.LocationMessageListener
import com.inscripts.cometchatpulse.Fragment.GroupFragment
import com.inscripts.cometchatpulse.Utils.DateUtil
import java.util.*
import kotlin.collections.ArrayList

class MessageRepository {

    var ownerId: String

    var messageRequest: MessagesRequest? = null

    var groupMessageRequest: MessagesRequest? = null

    var user: MutableLiveData<User> = MutableLiveData()

    init {
        ownerId = CometChat.getLoggedInUser().uid
    }

    var onetoOneMessageList: MutableLiveData<MutableList<BaseMessage>> = MutableLiveData()

    var filterLiveonetoOneMessageList: MutableLiveData<MutableList<BaseMessage>> = MutableLiveData()

    var groupMessageList: MutableLiveData<MutableList<BaseMessage>> = MutableLiveData()

    var filterLivegroupMessageList: MutableLiveData<MutableList<BaseMessage>> = MutableLiveData()

    var mutableOneToOneMessageList = ArrayList<BaseMessage>()

    var filterMutableOneToOneMessageList = mutableListOf<BaseMessage>()

    var mutableGroupMessageList = mutableListOf<BaseMessage>()

    var filterGroupMessageList = mutableListOf<BaseMessage>()

    var liveStartTypingIndicator: MutableLiveData<TypingIndicator> = MutableLiveData()

    var liveEndTypingIndicator: MutableLiveData<TypingIndicator> = MutableLiveData()

    var liveReadReceipts: MutableLiveData<MessageReceipt> = MutableLiveData()

    var liveDeliveryReceipts: MutableLiveData<MessageReceipt> = MutableLiveData()

    var liveMessageEdited: MutableLiveData<BaseMessage> = MutableLiveData()

    var liveMessageDeleted: MutableLiveData<BaseMessage> = MutableLiveData()

    private val TAG = "MessageRepository"

    @WorkerThread
    fun fetchMessage(LIMIT: Int, userId: String, isRefresh: Boolean) {
        try {
            if (isRefresh) {
                messageRequest = null
            }

            if (messageRequest == null) {
                messageRequest = MessagesRequest.MessagesRequestBuilder().setUID(userId).setLimit(LIMIT).build()
            }
            fetchMessageRequest(messageRequest)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

    }

    private fun fetchMessageRequest(messagesRequest: MessagesRequest?) {

        messagesRequest?.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(p0: List<BaseMessage>?) {
                Log.e(TAG, "FETCH MESSAGE")
                if (p0 != null&& p0.isNotEmpty()) {

                     mutableOneToOneMessageList.addAll(0,p0)

                    onetoOneMessageList.value = mutableOneToOneMessageList

                    if (p0.isNotEmpty()) {
                        val message = p0[p0.size - 1]
                        CometChat.markAsRead(message.id, message.sender.uid, message.receiverType)
                    }
                }

                Log.d(TAG, "messageRequest onSuccess: ${p0?.size}")

            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "messageRequest onError: ${p0?.message}")
            }

        })
    }

    private fun fetchGroupMessageRequest(messagesRequest: MessagesRequest?) {

        messagesRequest!!.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(p0: List<BaseMessage>?) {
                Log.e("groupMessageRequest", "${p0?.size}")
                if (p0 != null&&p0.isNotEmpty()) {
                        mutableGroupMessageList.addAll(0,p0)

                        groupMessageList.value = mutableGroupMessageList

                        if (p0.isNotEmpty()) {
                            val message = p0[p0.size - 1]
                            CometChat.markAsRead(message.id, message.receiverUid, message.receiverType)
                        }
                }
            }


            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "fetchGroupMessage onError: " + p0?.message)
            }


        })
    }

    @WorkerThread
    fun fetchGroupMessage(guid: String, LIMIT: Int, isRefresh: Boolean) {

        if (isRefresh) {
            groupMessageRequest = null
        }

        if (groupMessageRequest == null) {
            groupMessageRequest = MessagesRequest.MessagesRequestBuilder().setGUID(guid).setLimit(LIMIT).build()
        }
        fetchGroupMessageRequest(groupMessageRequest)
    }

    @WorkerThread
    fun sendTextMessage(textMessage: TextMessage, context: Context? = null) {

        CometChat.sendMessage(textMessage, object : CometChat.CallbackListener<TextMessage>() {
            override fun onSuccess(p0: TextMessage?) {
                if (p0 != null) {
                    Toast.makeText(CometChatPro.applicationContext(), "Text Message Sent Successfully", Toast.LENGTH_SHORT).show()
                    Log.d("messageDao", "  $p0")
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList

                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList

                    }
                }

            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()

            }

        })
    }


    fun addGroupListener(group_event_listener: String, GUID: String?) {
        CometChat.addGroupListener(group_event_listener, object : CometChat.GroupListener() {

            override fun onGroupMemberScopeChanged(action: Action?, updatedBy: User?, updatedUser: User?, scopeChangedTo: String?, scopeChangedFrom: String?, group: Group?) {
                if (group?.guid == GUID) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList
                }
            }

            override fun onGroupMemberKicked(action: Action?, kickedUser: User?, kickedBy: User?, kickedFrom: Group?) {
                if (kickedFrom?.guid == GUID) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList
                }
            }

            override fun onGroupMemberUnbanned(action: Action?, unbannedUser: User?, unbannedBy: User?, unbannedFrom: Group?) {
                if (unbannedFrom?.guid == GUID) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList
                }
            }

            override fun onGroupMemberBanned(action: Action?, bannedUser: User?, bannedBy: User?, bannedFrom: Group?) {
                if (bannedFrom?.guid == GUID) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList
                }
            }

            override fun onGroupMemberLeft(action: Action?, joinedUser: User?, joinedGroup: Group?) {
                if (joinedGroup?.guid == GUID) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList
                }
            }

            override fun onGroupMemberJoined(action: Action?, joinedUser: User?, joinedGroup: Group?) {
                if (joinedGroup?.guid == GUID) {
                    action?.let { mutableGroupMessageList.add(it) }
                    groupMessageList.value = mutableGroupMessageList
                }
            }

        })
    }

    @WorkerThread
    fun messageReceiveListener(listener: String, userId: String) {

        CometChat.addMessageListener(listener, object : CometChat.MessageListener() {

            override fun onMessagesDelivered(messageReceipt: MessageReceipt?) {
                if (messageReceipt != null) {
                    if (messageReceipt.receivertype == CometChatConstants.RECEIVER_TYPE_USER) {
                        if (messageReceipt.sender.uid == userId)
                            liveDeliveryReceipts.value = messageReceipt
                    } else
                        if (messageReceipt.receiverId == userId)
                            liveDeliveryReceipts.value = messageReceipt
                }
            }

            override fun onMessagesRead(messageReceipt: MessageReceipt?) {
                if (messageReceipt != null) {
                    if (messageReceipt.receivertype == CometChatConstants.RECEIVER_TYPE_USER) {
                        if (messageReceipt.sender.uid == userId)
                            liveReadReceipts.value = messageReceipt
                    } else
                        if (messageReceipt.receiverId == userId)
                            liveReadReceipts.value = messageReceipt
                }
            }

            override fun onMessageEdited(message: BaseMessage?) {
                Log.d(TAG, "onMessageEdited: $message")
                if (message != null) {
                    if (message.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        if (message.sender.uid == userId)
                            liveMessageEdited.value = message
                    } else
                        if (message.receiverUid == userId)
                            liveMessageEdited.value = message
                }
            }

            override fun onMessageDeleted(message: BaseMessage?) {
                Log.d(TAG, "onMessageDeleted: $message")
                if (message != null) {
                    if (message.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        if (message.sender.uid == userId)
                            liveMessageDeleted.value = message
                    } else
                        if (message.receiverUid == userId)
                            liveMessageDeleted.value = message
                }
            }


            override fun onTypingEnded(typingIndicator: TypingIndicator?) {
                Log.d(TAG, "onTypingEnded: $typingIndicator")
                if (typingIndicator != null) {
                    if (typingIndicator.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        if (typingIndicator.sender.uid == userId)
                            liveEndTypingIndicator.value = typingIndicator
                    } else
                        if (typingIndicator.receiverId == userId) {
                            liveEndTypingIndicator.value = typingIndicator
                        }
                }
            }

            override fun onTypingStarted(typingIndicator: TypingIndicator?) {
                Log.d(TAG, "onTypingStarted: $typingIndicator")
                if (typingIndicator != null) {
                    if (typingIndicator.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        if (typingIndicator.sender.uid == userId)
                            liveStartTypingIndicator.value = typingIndicator
                    } else
                        if (typingIndicator.receiverId == userId) {
                            liveStartTypingIndicator.value = typingIndicator
                        }
                }
            }

            override fun onTextMessageReceived(p0: TextMessage?) {
                if (p0 != null) {

                    Log.d(TAG, "onTextMessageReceived:   $p0")

                    if (p0.receiverType != CometChatConstants.RECEIVER_TYPE_GROUP) {
                        if (p0.sender.uid == userId) {
                            try {
                                OneToOneFragment.scrollFlag = true
                                mutableOneToOneMessageList.add(p0)
                                onetoOneMessageList.value = mutableOneToOneMessageList
                                CometChat.markAsRead(p0.id, p0.sender.uid, p0.receiverType)
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        if (p0.receiverUid == userId) {
                            CometChat.markAsRead(p0.id, p0.receiverUid, p0.receiverType)
                            GroupFragment.scrollFlag = true
                            mutableGroupMessageList.add(p0)
                            groupMessageList.value = mutableGroupMessageList
                        }

                    }
                }
            }

            override fun onMediaMessageReceived(p0: MediaMessage?) {
                if (p0 != null) {

                    Log.d(TAG, "onMediaMessageReceived: ${p0}")
                    if (p0.receiverType != CometChatConstants.RECEIVER_TYPE_GROUP) {
                        if (p0.sender.uid == userId) {
                            OneToOneFragment.scrollFlag = true
                            mutableOneToOneMessageList.add(p0)
                            onetoOneMessageList.value = mutableOneToOneMessageList
                            CometChat.markAsRead(p0.id, p0.sender.uid, p0.receiverType)
                        }

                    } else {
                        if (p0.receiverUid == userId) {
                            CometChat.markAsRead(p0.id, p0.receiverUid, p0.receiverType)
                            GroupFragment.scrollFlag = true
                            mutableGroupMessageList.add(p0)
                            groupMessageList.value = mutableGroupMessageList
                        }
                    }
                }
            }

            override fun onCustomMessageReceived(p0: CustomMessage?) {
                if (p0 != null) {

                    Log.d(TAG, "onCustomMessageReceived:   $p0")

                    if (p0.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        if (p0.sender.uid == userId) {
                            OneToOneFragment.scrollFlag = true
                            mutableOneToOneMessageList.add(p0)
                            onetoOneMessageList.value = mutableOneToOneMessageList
                            CometChat.markAsRead(p0.id, p0.sender.uid, p0.receiverType)
                        }

                    } else {
                        if (p0.receiverUid == userId) {
                            CometChat.markAsRead(p0.id, p0.receiverUid, p0.receiverType)
                            GroupFragment.scrollFlag = true
                            mutableGroupMessageList.add(p0)
                            groupMessageList.value = mutableGroupMessageList
                        }

                    }
                }
            }
        })
    }

    @WorkerThread
    fun removeMessageListener(listener: String) {
        CometChat.removeMessageListener(listener)
    }

    @WorkerThread
    fun addPresenceListener(listener: String) {

        CometChat.addUserListener(listener, object : CometChat.UserListener() {
            override fun onUserOffline(p0: User?) {

                if (p0 != null) {
                    user.value = p0
                }
            }

            override fun onUserOnline(p0: User?) {
                if (p0 != null) {
                    user.value = p0
                }
            }

        })


    }


    @WorkerThread
    fun removePresenceListener(listener: String) {
        CometChat.removeUserListener(listener)
    }

    @WorkerThread
    fun sendMediaMessage(mediaMessage: MediaMessage) {

        CometChat.sendMediaMessage(mediaMessage, object : CometChat.CallbackListener<MediaMessage>() {
            override fun onSuccess(p0: MediaMessage?) {

                if (p0 != null) {
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }

                }
                Toast.makeText(CometChatPro.applicationContext(), "Media Message Sent Successfully", Toast.LENGTH_SHORT).show()
                Log.d("MediaMessage", "baseMessage")
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                p0?.printStackTrace()
            }

        })
    }

    @WorkerThread
    fun sendCustomMessage(customMessage: CustomMessage, context: Context?) {

        CometChat.sendCustomMessage(customMessage, object : CometChat.CallbackListener<CustomMessage>() {
            override fun onSuccess(p0: CustomMessage?) {
                Log.e("sendCustomMessage ", "")
                if (p0 != null) {
                    if (p0.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }
                    if (context != null) {
                        if (context is LocationActivity) {
                            context.finish()
                        }
                    }
                    Toast.makeText(CometChatPro.applicationContext(), "Custom Message Sent Successfully", Toast.LENGTH_SHORT).show()
                    Log.d("CustomMessage", p0!!.category + "\n" + p0.type + "\n" + p0.toString())

                }

            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                p0?.printStackTrace()
            }

        })
    }

    fun removeGroupListener(group_event_listener: String) {
        CometChat.removeGroupListener(group_event_listener)
    }

    fun addCallListener(context: Context, call_event_listener: String, view: RelativeLayout?) {

        CometChat.addCallListener(call_event_listener, object : CometChat.CallListener() {

            override fun onIncomingCallCancelled(p0: Call?) {
                Toast.makeText(CometChatPro.applicationContext(), "Incoming Call Cancelled", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onIncomingCallCancelled ")

                if (p0 != null) {
                    if (CometChat.getActiveCall() != null) {
                        if (p0.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                            mutableOneToOneMessageList.add(p0)
                            onetoOneMessageList.value = mutableOneToOneMessageList
                        } else {
                            mutableGroupMessageList.add(p0)
                            groupMessageList.value = mutableGroupMessageList
                        }
                    }
                }

                if (context is CallActivity) {

                    context.finish()
                }
            }

            override fun onOutgoingCallAccepted(p0: Call?) {

                Toast.makeText(CometChatPro.applicationContext(), "Outgoing Call Accepted", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onOutgoingCallAccepted: ")

                if (p0 != null) {
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }
                }
                p0?.sessionId?.let {
                    if (view != null) {
                        CometChat.startCall(context as Activity, it, view, object : CometChat.OngoingCallListener {

                            override fun onUserJoined(p0: User?) {
                                Toast.makeText(CometChatPro.applicationContext(), p0?.uid + " User joined", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "onUserJoined: " + p0?.uid)
                            }

                            override fun onUserLeft(p0: User?) {
                                Toast.makeText(CometChatPro.applicationContext(), p0?.uid + " User Left", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "onUserLeft: " + p0?.uid)
                            }

                            override fun onError(p0: CometChatException?) {
                                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "CallonError: " + p0?.message)
                            }

                            override fun onCallEnded(p0: Call?) {
                                Toast.makeText(CometChatPro.applicationContext(), "Call Ended " + p0?.toString(), Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "onCallEnded " + "onOutgoingCallAccepted ")

                                if (context is CallActivity) {

                                    context.finish()
                                }
                            }

                        })
                    }
                }
            }

            override fun onIncomingCallReceived(p0: Call?) {

                if (p0 != null) {
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }
                }
                Toast.makeText(CometChatPro.applicationContext(), "Incoming Call Recieved", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onIncomingCallReceived: ")
                if (p0 != null) {
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {

                        CommonUtil.startCallIntent(CometChatConstants.RECEIVER_TYPE_USER, context,
                                p0.callInitiator as User, p0.type, false, p0.sessionId)

                    } else if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                        CommonUtil.startCallIntent(CometChatConstants.RECEIVER_TYPE_GROUP, context,
                                p0.callReceiver as Group, p0.type, false, p0.sessionId)
                    }
                }
            }

            override fun onOutgoingCallRejected(p0: Call?) {

                if (p0 != null) {
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }
                }
                Toast.makeText(CometChatPro.applicationContext(), "Outgoing Call Rejected", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onOutgoingCallRejected: ")
                if (context is CallActivity) {

                    context.finish()
                }
            }

        })

    }

    fun removeCallListener(call_event_listener: String) {
        CometChat.removeCallListener(call_event_listener)
    }

    fun acceptCall(sessionID: String, view: RelativeLayout, activity: Activity) {


        CometChat.acceptCall(sessionID, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(p0: Call?) {


                if (p0 != null) {
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }
                }

                p0?.sessionId?.let {
                    CometChat.startCall(activity, it, view, object : CometChat.OngoingCallListener {
                        override fun onUserJoined(p0: User?) {
                            Toast.makeText(CometChatPro.applicationContext(), p0?.uid + " User Joined", Toast.LENGTH_LONG).show()
                            Log.d(TAG, "onUserJoined: " + p0?.uid)
                        }

                        override fun onUserLeft(p0: User?) {
                            Toast.makeText(CometChatPro.applicationContext(), p0?.uid + " User Left", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "onUserLeft: " + p0?.uid)
                        }

                        override fun onError(p0: CometChatException?) {
                            Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                        }

                        override fun onCallEnded(p0: Call?) {
                            Toast.makeText(CometChatPro.applicationContext(), "Call Ended", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "onCallEnded: ")

                            activity.finish()
                        }

                    })
                }
            }

            override fun onError(p0: CometChatException?) {

            }

        })
    }

    fun rejectCall(sessionID: String, call_status_rejected: String, activity: Activity) {
        CometChat.rejectCall(sessionID, call_status_rejected, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(p0: Call?) {
                Log.d(TAG, "onSuccess: ")
                Toast.makeText(CometChatPro.applicationContext(), "Call Rejected", Toast.LENGTH_SHORT).show()
                activity.finish()
                if (p0 != null) {
                    if (p0.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }
                }
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
            }

        })

    }

    fun initiateCall(call: Call, context: Context) {
        CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(p0: Call?) {
                Toast.makeText(CometChatPro.applicationContext(), "Call Initiated ", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onSuccess: ")
                if (p0 != null) {

                    if (p0.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        CommonUtil.startCallIntent(CometChatConstants.RECEIVER_TYPE_USER, context, p0.callReceiver as User, p0.type, true, p0.sessionId)
                    } else if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                        CommonUtil.startCallIntent(CometChatConstants.RECEIVER_TYPE_GROUP, context, p0.callReceiver as Group, p0.type, true, p0.sessionId)
                    }
                }

            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun sendTypingIndicator(typingIndicator: TypingIndicator, isEndTyping: Boolean) {

        if (isEndTyping)
            CometChat.endTyping(typingIndicator)
        else
            CometChat.startTyping(typingIndicator)

    }

    fun deleteMessage(textMessage: TextMessage) {

        CometChat.deleteMessage(textMessage.id, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(p0: BaseMessage?) {
                if (p0 != null) {
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }
                    Toast.makeText(CometChatPro.applicationContext(), "Delete Message Successfully", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "deleteMessage onError: ${p0?.message}")
            }

        })
    }

    fun editMessage(any: BaseMessage, messageText: String) {

        val textMessage = TextMessage(any.receiverUid, messageText, CometChatConstants.RECEIVER_TYPE_USER)

        textMessage.id = any.id

        Log.d(TAG, "editMessage: " + textMessage.id)

        CometChat.editMessage(textMessage, object : CometChat.CallbackListener<BaseMessage>() {

            override fun onSuccess(p0: BaseMessage?) {
                if (p0 != null) {
                    Toast.makeText(CometChatPro.applicationContext(), "Message Edited Successfully", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "editMessage: onSuccess " + p0.id)
                    if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                        mutableOneToOneMessageList.add(p0)
                        onetoOneMessageList.value = mutableOneToOneMessageList
                    } else {
                        mutableGroupMessageList.add(p0)
                        groupMessageList.value = mutableGroupMessageList
                    }
                }
            }

            override fun onError(p0: CometChatException?) {
                Toast.makeText(CometChatPro.applicationContext(), p0?.message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "editMessage onError: ${p0?.message}")
            }

        })
    }

    fun searchMessage(s: String, userId: String) {
        messageRequest = null

        val searchRequest = MessagesRequest.MessagesRequestBuilder().setSearchKeyword(s).setUID(userId).setLimit(30).build()

        searchRequest?.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {

            override fun onSuccess(p0: List<BaseMessage>?) {
                if (p0 != null) {
                    filterMutableOneToOneMessageList.clear()

                    for (baseMessage: BaseMessage in p0) {
                        Log.d(TAG, "baseMessage onSuccess: " + baseMessage.id)
                        if (!baseMessage.category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true)) {
                            filterMutableOneToOneMessageList.add(baseMessage)
                        }
                    }

                    filterLiveonetoOneMessageList.value = filterMutableOneToOneMessageList

                }
            }

            override fun onError(p0: CometChatException?) {
                Log.d(TAG, "onError: ${p0?.message}")
            }

        })
    }

    fun searchGroupMessage(s: String, guid: String) {

        messageRequest = null

        val searchRequest = MessagesRequest.MessagesRequestBuilder().setSearchKeyword(s).setGUID(guid).setLimit(30).build()

        searchRequest?.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {

            override fun onSuccess(p0: List<BaseMessage>?) {
                if (p0 != null) {
                    filterGroupMessageList.clear()

                    for (baseMessage: BaseMessage in p0) {
                        Log.d(TAG, "baseMessage onSuccess: " + baseMessage.id)
                        if (!baseMessage.category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true)) {
                            filterGroupMessageList.add(baseMessage)
                        }
                    }
                    filterLivegroupMessageList.value = filterGroupMessageList

                }
            }

            override fun onError(p0: CometChatException?) {
                Log.d(TAG, "onError: ${p0?.message}")
            }

        })
    }

    fun updateMap(userId: String?, p0: CustomMessage) {
        if (p0.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER)) {
            mutableOneToOneMessageList.add(p0)
            onetoOneMessageList.value = mutableOneToOneMessageList
        } else {
            mutableGroupMessageList.add(p0)
            groupMessageList.value = mutableGroupMessageList
        }
    }

}