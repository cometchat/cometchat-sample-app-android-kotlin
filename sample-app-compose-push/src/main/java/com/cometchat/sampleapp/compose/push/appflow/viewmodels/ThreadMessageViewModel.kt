package com.cometchat.sampleapp.compose.push.appflow.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUserEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Thread Message screen.
 * Handles user block status for thread conversations.
 */
class ThreadMessageViewModel : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isBlocked = MutableStateFlow(false)
    val isBlocked: StateFlow<Boolean> = _isBlocked.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    companion object {
        private const val TAG = "ThreadMessageViewModel"
    }
    
    /**
     * Initialize the ViewModel with user.
     */
    fun initialize(user: User?) {
        _user.value = user
        _isBlocked.value = user?.isBlockedByMe ?: false
        
        user?.let { addUserListener() }
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
                    currentUser.isBlockedByMe = false
                    CometChatEvents.emitUserEvent(CometChatUserEvent.UserUnblocked(currentUser))
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
     * Add user listener for real-time updates.
     */
    private fun addUserListener() {
        val listenerID = "ThreadMessageViewModel_UserListener"
        
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
        CometChat.removeUserListener("ThreadMessageViewModel_UserListener")
    }
}
