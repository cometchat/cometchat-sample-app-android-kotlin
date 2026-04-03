package com.cometchat.sampleapp.kotlin.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants.DialogState

/**
 * ViewModel for ThreadMessageActivity.
 * Handles parent message state and user blocking/unblocking.
 */
class ThreadMessageViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "ThreadMessageViewModel"
        private const val USER_LISTENER_ID = "ThreadMessageViewModel_UserListener"
    }
    
    private val _parentMessage = MutableLiveData<BaseMessage>()
    val parentMessage: LiveData<BaseMessage> = _parentMessage
    
    private val _userBlockStatus = MutableLiveData<User>()
    val userBlockStatus: LiveData<User> = _userBlockStatus
    
    private val _unblockButtonState = MutableLiveData<DialogState>()
    val unblockButtonState: LiveData<DialogState> = _unblockButtonState
    
    private var user: User? = null
    
    fun setParentMessage(message: BaseMessage) {
        _parentMessage.value = message
    }
    
    fun setUser(user: User) {
        this.user = user
    }
    
    fun addUserListener() {
        CometChat.addUserListener(USER_LISTENER_ID, object : CometChat.UserListener() {
            override fun onUserOnline(user: User) {
                // User came online
            }
            
            override fun onUserOffline(user: User) {
                // User went offline
            }
        })
    }
    
    fun unblockUser() {
        val currentUser = user ?: return
        
        _unblockButtonState.value = DialogState.INITIATED
        
        CometChat.unblockUsers(listOf(currentUser.uid), object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(result: HashMap<String, String>) {
                _unblockButtonState.value = DialogState.SUCCESS
                currentUser.isBlockedByMe = false
                _userBlockStatus.value = currentUser
            }
            
            override fun onError(e: CometChatException) {
                _unblockButtonState.value = DialogState.FAILURE
            }
        })
    }
    
    override fun onCleared() {
        super.onCleared()
        CometChat.removeUserListener(USER_LISTENER_ID)
    }
}
