package com.cometchat.sampleapp.kotlin.fcm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.UserListener
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.fcm.data.repository.Repository
import com.cometchat.sampleapp.kotlin.fcm.utils.AppConstants

class UserDetailsViewModel : ViewModel() {
    private val USER_LISTENER_ID: String = UserDetailsViewModel::class.java.simpleName
    private val baseMessage: MutableLiveData<BaseMessage?> = MutableLiveData()
    private val onCallStart = MutableLiveData<Call>()
    private val onStarCallError = MutableLiveData<String?>()
    val user: MutableLiveData<User?> = MutableLiveData()
    val isUserBlockedByMe: MutableLiveData<Boolean> = MutableLiveData()
    val isUserBlocked: MutableLiveData<Boolean> = MutableLiveData()
    val isUserUnblocked: MutableLiveData<Boolean> = MutableLiveData()
    val isChatDeleted: MutableLiveData<Boolean> = MutableLiveData()

    fun onCallStart(): MutableLiveData<Call> {
        return onCallStart
    }

    fun onCallStartError(): MutableLiveData<String?> {
        return onStarCallError
    }

    fun getBaseMessage(): MutableLiveData<BaseMessage?> {
        return baseMessage
    }

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
        Repository.getUser(mUser.uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(userObj: User) {
                user.value = userObj
                isUserBlockedByMe.value = userObj.isBlockedByMe
            }

            override fun onError(e: CometChatException) {
            }
        })
    }

    fun setBaseMessage(message: BaseMessage?) {
        baseMessage.value = message
    }

    fun blockUser() {
        if (user.value != null) {
            Repository.blockUser(user.value!!, object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(resultMap: HashMap<String, String>) {
                    if (AppConstants.SuccessConstants.SUCCESS.equals(
                            resultMap[user.value!!.uid], ignoreCase = true
                        )
                    ) {
                        isUserBlocked.value = true
                        isUserBlockedByMe.setValue(true)
                        user.value!!.isBlockedByMe = true
                        setUser(user.value!!)
                    } else {
                        isUserBlocked.setValue(false)
                    }
                }

                override fun onError(e: CometChatException) {
                    isUserBlocked.value = false
                }
            })
        } else {
            isUserBlocked.setValue(false)
        }
    }

    fun unblockUser() {
        if (user.value != null) {
            Repository.unblockUser(user.value!!, object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(resultMap: HashMap<String, String>) {
                    if (AppConstants.SuccessConstants.SUCCESS.equals(
                            resultMap[user.value!!.uid], ignoreCase = true
                        )
                    ) {
                        isUserUnblocked.value = true
                        isUserBlockedByMe.setValue(false)
                        user.value!!.isBlockedByMe = false
                        setUser(user.value!!)
                    } else {
                        isUserUnblocked.setValue(false)
                    }
                }

                override fun onError(e: CometChatException) {
                    isUserUnblocked.value = false
                }
            })
        } else {
            isUserUnblocked.setValue(false)
        }
    }

    fun deleteChat() {
        if (user.value != null) {
            Repository.deleteChat(user.value!!.uid,
                baseMessage.value,
                CometChatConstants.RECEIVER_TYPE_USER,
                object : CometChat.CallbackListener<String>() {
                    override fun onSuccess(s: String) {
                        isChatDeleted.value = true
                    }

                    override fun onError(e: CometChatException) {
                        isChatDeleted.value = false
                    }
                })
        } else {
            isChatDeleted.setValue(false)
        }
    }

    fun startCall(callType: String?) {
        if (user.value != null) {
            val call = Call(user.value!!.uid, CometChatConstants.RECEIVER_TYPE_USER, callType)
            Repository.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                override fun onSuccess(call: Call) {
                    onCallStart.value = call
                }

                override fun onError(e: CometChatException) {
                    onStarCallError.value = e.message
                }
            })
        } else {
            onStarCallError.setValue("User is null")
        }
    }
}
