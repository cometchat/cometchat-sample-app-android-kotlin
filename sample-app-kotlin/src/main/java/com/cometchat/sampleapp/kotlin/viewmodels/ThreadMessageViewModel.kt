package com.cometchat.sampleapp.kotlin.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.shared.constants.UIKitConstants
import com.cometchat.chatuikit.shared.events.CometChatUserEvents
import com.cometchat.sampleapp.kotlin.data.repository.Repository
import com.cometchat.sampleapp.kotlin.utils.AppConstants

class ThreadMessageViewModel : ViewModel() {

    private val _parentMessage = MutableLiveData<BaseMessage>()
    val parentMessage: LiveData<BaseMessage> get() = _parentMessage

    private val _updateUser = MutableLiveData<User>()
    val userBlockStatus: LiveData<User> get() = _updateUser

    private val _unblockButtonState = MutableLiveData<UIKitConstants.DialogState>()
    val unblockButtonState: LiveData<UIKitConstants.DialogState> get() = _unblockButtonState

    private val listenerId = "${System.currentTimeMillis()}_${this::class.java.simpleName}"

    private var messageId: Int = -1
    private var user: User? = null

    fun getMessageId(): Int = messageId

    fun fetchMessageDetails(id: Int) {
        this.messageId = id
        Repository.fetchMessageInformation(id, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage?) {
                Log.i("TAG", "onSuccess: $message")
                message?.let { _parentMessage.postValue(it) }
            }

            override fun onError(e: CometChatException?) {
                Log.i("TAG", "onError: 1234 " + e?.message)
            }
        })
    }

    fun unblockUser() {
        val currentUser = user ?: return
        _unblockButtonState.value = UIKitConstants.DialogState.INITIATED

        Repository.unblockUser(currentUser, object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(resultMap: HashMap<String, String>?) {
                val result = resultMap?.get(currentUser.uid)
                _unblockButtonState.value =
                    if (AppConstants.SuccessConstants.SUCCESS.equals(result, ignoreCase = true)) {
                        UIKitConstants.DialogState.SUCCESS
                    } else {
                        UIKitConstants.DialogState.FAILURE
                    }
            }

            override fun onError(e: CometChatException?) {
                _unblockButtonState.value = UIKitConstants.DialogState.FAILURE
            }
        })
    }

    fun addUserListener() {
        CometChatUserEvents.addUserListener(listenerId, object : CometChatUserEvents() {
            override fun ccUserBlocked(user: User) {
                _updateUser.value = user
            }

            override fun ccUserUnblocked(user: User) {
                _updateUser.value = user
            }
        })
    }

    fun setUser(user: User) {
        this.user = user
    }
}