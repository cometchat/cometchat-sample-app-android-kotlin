package com.cometchat.sampleapp.compose.push.appflow.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the User Details screen.
 * Handles user operations like block/unblock, call initiation, and chat deletion.
 */
class UserDetailsViewModel : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isBlocked = MutableStateFlow(false)
    val isBlocked: StateFlow<Boolean> = _isBlocked.asStateFlow()
    
    private val _lastMessage = MutableStateFlow<BaseMessage?>(null)
    val lastMessage: StateFlow<BaseMessage?> = _lastMessage.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _callInitiated = MutableStateFlow<Call?>(null)
    val callInitiated: StateFlow<Call?> = _callInitiated.asStateFlow()
    
    companion object {
        private const val TAG = "UserDetailsViewModel"
    }
    
    /**
     * Initialize the ViewModel with user and last message.
     */
    fun initialize(user: User, lastMessage: BaseMessage?) {
        _user.value = user
        _isBlocked.value = user.isBlockedByMe
        _lastMessage.value = lastMessage
        addUserListener()
    }
    
    /**
     * Block the current user.
     */
    fun blockUser() {
        val currentUser = _user.value ?: return
        
        _isLoading.value = true
        
        CometChat.blockUsers(
            listOf(currentUser.uid),
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(result: HashMap<String, String>?) {
                    _isLoading.value = false
                    _isBlocked.value = true
                    Log.d(TAG, "User blocked successfully")
                }
                
                override fun onError(exception: CometChatException?) {
                    _isLoading.value = false
                    _error.value = exception?.message
                    Log.e(TAG, "Failed to block user: ${exception?.message}")
                }
            }
        )
    }
    
    /**
     * Unblock the current user.
     */
    fun unblockUser() {
        val currentUser = _user.value ?: return
        
        _isLoading.value = true
        
        CometChat.unblockUsers(
            listOf(currentUser.uid),
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(result: HashMap<String, String>?) {
                    _isLoading.value = false
                    _isBlocked.value = false
                    Log.d(TAG, "User unblocked successfully")
                }
                
                override fun onError(exception: CometChatException?) {
                    _isLoading.value = false
                    _error.value = exception?.message
                    Log.e(TAG, "Failed to unblock user: ${exception?.message}")
                }
            }
        )
    }
    
    /**
     * Start a voice call with the user.
     */
    fun startVoiceCall() {
        startCall(CometChatConstants.CALL_TYPE_AUDIO)
    }
    
    /**
     * Start a video call with the user.
     */
    fun startVideoCall() {
        startCall(CometChatConstants.CALL_TYPE_VIDEO)
    }
    
    /**
     * Start a call with the specified type.
     */
    private fun startCall(callType: String) {
        val currentUser = _user.value ?: return
        
        val call = Call(currentUser.uid, CometChatConstants.RECEIVER_TYPE_USER, callType)
        
        _isLoading.value = true
        
        CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
            override fun onSuccess(initiatedCall: Call?) {
                _isLoading.value = false
                Log.d(TAG, "Call initiated successfully")
                initiatedCall?.let { _callInitiated.value = it }
            }
            
            override fun onError(exception: CometChatException?) {
                _isLoading.value = false
                _error.value = exception?.message
                Log.e(TAG, "Failed to initiate call: ${exception?.message}")
            }
        })
    }
    
    /**
     * Delete the chat with the user.
     */
    fun deleteChat(onSuccess: () -> Unit) {
        val currentUser = _user.value ?: return
        
        _isLoading.value = true
        
        CometChat.deleteConversation(
            currentUser.uid,
            CometChatConstants.CONVERSATION_TYPE_USER,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String?) {
                    _isLoading.value = false
                    Log.d(TAG, "Chat deleted successfully")
                    onSuccess()
                }
                
                override fun onError(exception: CometChatException?) {
                    _isLoading.value = false
                    _error.value = exception?.message
                    Log.e(TAG, "Failed to delete chat: ${exception?.message}")
                }
            }
        )
    }
    
    /**
     * Add user listener for real-time updates.
     */
    private fun addUserListener() {
        val listenerID = "UserDetailsViewModel_UserListener"
        
        CometChat.addUserListener(listenerID, object : CometChat.UserListener() {
            override fun onUserOnline(user: User?) {
                if (user?.uid == _user.value?.uid) {
                    _user.value = user
                }
            }
            
            override fun onUserOffline(user: User?) {
                if (user?.uid == _user.value?.uid) {
                    _user.value = user
                }
            }
        })
    }
    
    /**
     * Remove listeners when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        CometChat.removeUserListener("UserDetailsViewModel_UserListener")
    }
    
    /**
     * Clear the call initiated state after it's been handled.
     */
    fun clearCallInitiated() {
        _callInitiated.value = null
    }
}
