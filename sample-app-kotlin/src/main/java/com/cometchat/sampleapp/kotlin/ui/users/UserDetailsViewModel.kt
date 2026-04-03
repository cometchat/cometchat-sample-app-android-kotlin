package com.cometchat.sampleapp.kotlin.ui.users

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.UserListener
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.helpers.CometChatHelper
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.events.CometChatConversationEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUserEvent

class UserDetailsViewModel : ViewModel() {
    
    companion object {
        private const val SUCCESS = "success"
    }
    
    private val USER_LISTENER_ID: String = UserDetailsViewModel::class.java.simpleName
    private val _baseMessage: MutableLiveData<BaseMessage?> = MutableLiveData()
    private val _onCallStart = MutableLiveData<Call>()
    private val _onCallStartError = MutableLiveData<String?>()
    
    val user: MutableLiveData<User?> = MutableLiveData()
    val isUserBlockedByMe: MutableLiveData<Boolean> = MutableLiveData()
    val isUserBlocked: MutableLiveData<Boolean> = MutableLiveData()
    val isUserUnblocked: MutableLiveData<Boolean> = MutableLiveData()
    val isChatDeleted: MutableLiveData<Boolean> = MutableLiveData()
    
    fun onCallStart(): MutableLiveData<Call> = _onCallStart
    fun onCallStartError(): MutableLiveData<String?> = _onCallStartError
    fun getBaseMessage(): MutableLiveData<BaseMessage?> = _baseMessage
    
    fun addListeners() {
        CometChat.addUserListener(USER_LISTENER_ID, object : UserListener() {
            override fun onUserOnline(mUser: User) {
                if (user.value != null && user.value!!.uid == mUser.uid) {
                    user.value = mUser
                }
            }
            
            override fun onUserOffline(mUser: User) {
                if (user.value != null && user.value!!.uid == mUser.uid) {
                    user.value = mUser
                }
            }
        })
    }
    
    fun removeListeners() {
        CometChat.removeUserListener(USER_LISTENER_ID)
    }
    
    fun setUser(mUser: User) {
        user.value = mUser
        isUserBlockedByMe.value = mUser.isBlockedByMe
        CometChat.getUser(mUser.uid, object : CometChat.CallbackListener<User?>() {
            override fun onSuccess(userObj: User?) {
                userObj?.let {
                    user.value = it
                    isUserBlockedByMe.value = it.isBlockedByMe
                }
            }
            
            override fun onError(e: CometChatException) {
                // Keep existing user data
            }
        })
    }
    
    fun setBaseMessage(message: BaseMessage?) {
        _baseMessage.value = message
    }
    
    fun blockUser() {
        val currentUser = user.value
        if (currentUser != null) {
            CometChat.blockUsers(listOf(currentUser.uid), object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(resultMap: HashMap<String, String>) {
                    if (SUCCESS.equals(resultMap[currentUser.uid], ignoreCase = true)) {
                        isUserBlocked.value = true
                        isUserBlockedByMe.value = true
                        currentUser.isBlockedByMe = true
                        CometChatEvents.emitUserEvent(CometChatUserEvent.UserBlocked(currentUser))
                        setUser(currentUser)
                    } else {
                        isUserBlocked.value = false
                    }
                }
                
                override fun onError(e: CometChatException) {
                    isUserBlocked.value = false
                }
            })
        } else {
            isUserBlocked.value = false
        }
    }
    
    fun unblockUser() {
        val currentUser = user.value
        if (currentUser != null) {
            CometChat.unblockUsers(listOf(currentUser.uid), object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(resultMap: HashMap<String, String>) {
                    if (SUCCESS.equals(resultMap[currentUser.uid], ignoreCase = true)) {
                        isUserUnblocked.value = true
                        isUserBlockedByMe.value = false
                        currentUser.isBlockedByMe = false
                        CometChatEvents.emitUserEvent(CometChatUserEvent.UserUnblocked(currentUser))
                        setUser(currentUser)
                    } else {
                        isUserUnblocked.value = false
                    }
                }
                
                override fun onError(e: CometChatException) {
                    isUserUnblocked.value = false
                }
            })
        } else {
            isUserUnblocked.value = false
        }
    }
    
    fun deleteChat() {
        val currentUser = user.value
        if (currentUser != null) {
            CometChat.deleteConversation(
                currentUser.uid,
                CometChatConstants.CONVERSATION_TYPE_USER,
                object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(s: String) {
                        val baseMsg = _baseMessage.value
                        if (baseMsg != null) {
                            CometChatEvents.emitConversationEvent(
                                CometChatConversationEvent.ConversationDeleted(
                                    CometChatHelper.getConversationFromMessage(baseMsg)
                                )
                            )
                        } else {
                            CometChatEvents.emitConversationEvent(
                                CometChatConversationEvent.ConversationDeleted(
                                    Conversation("", CometChatConstants.CONVERSATION_TYPE_USER)
                                )
                            )
                        }
                        isChatDeleted.value = true
                    }
                    
                    override fun onError(e: CometChatException) {
                        isChatDeleted.value = false
                    }
                }
            )
        } else {
            isChatDeleted.value = false
        }
    }
    
    fun startCall(callType: String?) {
        val currentUser = user.value
        if (currentUser != null) {
            val call = Call(currentUser.uid, CometChatConstants.RECEIVER_TYPE_USER, callType)
            CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                override fun onSuccess(call: Call) {
                    _onCallStart.value = call
                }
                
                override fun onError(e: CometChatException) {
                    _onCallStartError.value = e.message
                }
            })
        } else {
            _onCallStartError.value = "User is null"
        }
    }
}
