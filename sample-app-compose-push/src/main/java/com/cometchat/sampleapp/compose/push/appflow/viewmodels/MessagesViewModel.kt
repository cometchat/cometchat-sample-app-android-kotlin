package com.cometchat.sampleapp.compose.push.appflow.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Action
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUserEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Messages screen.
 * Handles user block status, group membership, and related operations.
 */
class MessagesViewModel : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group.asStateFlow()
    
    private val _isBlocked = MutableStateFlow(false)
    val isBlocked: StateFlow<Boolean> = _isBlocked.asStateFlow()
    
    private val _isGroupMember = MutableStateFlow(true)
    val isGroupMember: StateFlow<Boolean> = _isGroupMember.asStateFlow()
    
    private val _lastMessage = MutableStateFlow<BaseMessage?>(null)
    val lastMessage: StateFlow<BaseMessage?> = _lastMessage.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    companion object {
        private const val TAG = "MessagesViewModel"
    }
    
    /**
     * Initialize the ViewModel with user or group.
     */
    fun initialize(user: User?, group: Group?) {
        _user.value = user
        _group.value = group
        
        user?.let {
            _isBlocked.value = it.isBlockedByMe || it.isHasBlockedMe
            addUserListener()

            // Fetch fresh user data from server to get current blocked status
            CometChat.getUser(it.uid, object : CometChat.CallbackListener<User>() {
                override fun onSuccess(freshUser: User) {
                    _user.value = freshUser
                    _isBlocked.value = freshUser.isBlockedByMe || freshUser.isHasBlockedMe
                }

                override fun onError(e: CometChatException) {
                    Log.e(TAG, "Failed to fetch fresh user data: ${e.message}")
                }
            })
        }
        
        group?.let {
            _isGroupMember.value = it.isJoined
            addGroupListener()
        }
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
                    currentUser.isBlockedByMe = true
                    CometChatEvents.emitUserEvent(CometChatUserEvent.UserBlocked(currentUser))
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
     * Update the last message.
     */
    fun updateLastMessage(message: BaseMessage?) {
        _lastMessage.value = message
    }
    
    /**
     * Add user listener for real-time updates.
     */
    private fun addUserListener() {
        val listenerID = "MessagesViewModel_UserListener"
        
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
     * Add group listener for real-time updates.
     */
    private fun addGroupListener() {
        val listenerID = "MessagesViewModel_GroupListener"
        
        CometChat.addGroupListener(listenerID, object : CometChat.GroupListener() {
            override fun onGroupMemberJoined(action: Action, joinedUser: User, joinedGroup: Group) {
                if (joinedGroup.guid == _group.value?.guid) {
                    _group.value = joinedGroup
                    val loggedInUser = CometChat.getLoggedInUser()
                    if (joinedUser.uid == loggedInUser?.uid) {
                        _isGroupMember.value = true
                    }
                }
            }
            
            override fun onGroupMemberLeft(action: Action, leftUser: User, leftGroup: Group) {
                if (leftGroup.guid == _group.value?.guid) {
                    _group.value = leftGroup
                    val loggedInUser = CometChat.getLoggedInUser()
                    if (leftUser.uid == loggedInUser?.uid) {
                        _isGroupMember.value = false
                    }
                }
            }
            
            override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, kickedFrom: Group) {
                if (kickedFrom.guid == _group.value?.guid) {
                    _group.value = kickedFrom
                    val loggedInUser = CometChat.getLoggedInUser()
                    if (kickedUser.uid == loggedInUser?.uid) {
                        _isGroupMember.value = false
                    }
                }
            }
            
            override fun onGroupMemberBanned(action: Action, bannedUser: User, bannedBy: User, bannedFrom: Group) {
                if (bannedFrom.guid == _group.value?.guid) {
                    _group.value = bannedFrom
                    val loggedInUser = CometChat.getLoggedInUser()
                    if (bannedUser.uid == loggedInUser?.uid) {
                        _isGroupMember.value = false
                    }
                }
            }
        })
    }
    
    /**
     * Remove listeners when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        CometChat.removeUserListener("MessagesViewModel_UserListener")
        CometChat.removeGroupListener("MessagesViewModel_GroupListener")
    }
}
