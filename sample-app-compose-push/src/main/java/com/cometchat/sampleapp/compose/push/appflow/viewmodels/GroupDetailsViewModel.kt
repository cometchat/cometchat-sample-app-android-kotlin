package com.cometchat.sampleapp.compose.push.appflow.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.Action
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Group Details screen.
 * Handles group operations like leave, delete, and member management.
 */
class GroupDetailsViewModel : ViewModel() {
    
    private val _group = MutableStateFlow<Group?>(null)
    val group: StateFlow<Group?> = _group.asStateFlow()
    
    private val _lastMessage = MutableStateFlow<BaseMessage?>(null)
    val lastMessage: StateFlow<BaseMessage?> = _lastMessage.asStateFlow()
    
    private val _isOwner = MutableStateFlow(false)
    val isOwner: StateFlow<Boolean> = _isOwner.asStateFlow()
    
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()
    
    private val _isModerator = MutableStateFlow(false)
    val isModerator: StateFlow<Boolean> = _isModerator.asStateFlow()
    
    private val _isMember = MutableStateFlow(true)
    val isMember: StateFlow<Boolean> = _isMember.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    companion object {
        private const val TAG = "GroupDetailsViewModel"
    }
    
    /**
     * Initialize the ViewModel with group and last message.
     */
    fun initialize(group: Group, lastMessage: BaseMessage?) {
        _group.value = group
        _lastMessage.value = lastMessage
        _isMember.value = group.isJoined
        
        // Determine user's role in the group
        val loggedInUser = CometChat.getLoggedInUser()
        _isOwner.value = group.owner == loggedInUser?.uid
        _isAdmin.value = group.scope == CometChatConstants.SCOPE_ADMIN
        _isModerator.value = group.scope == CometChatConstants.SCOPE_MODERATOR
        
        addGroupListener()
    }
    
    /**
     * Leave the group.
     */
    fun leaveGroup(onSuccess: () -> Unit) {
        val currentGroup = _group.value ?: return
        
        _isLoading.value = true
        
        CometChat.leaveGroup(currentGroup.guid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                _isLoading.value = false
                _isMember.value = false
                Log.d(TAG, "Left group successfully")
                onSuccess()
            }
            
            override fun onError(exception: CometChatException?) {
                _isLoading.value = false
                _error.value = exception?.message
                Log.e(TAG, "Failed to leave group: ${exception?.message}")
            }
        })
    }
    
    /**
     * Delete the group (owner only).
     */
    fun deleteGroup(onSuccess: () -> Unit) {
        val currentGroup = _group.value ?: return
        
        _isLoading.value = true
        
        CometChat.deleteGroup(currentGroup.guid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                _isLoading.value = false
                Log.d(TAG, "Group deleted successfully")
                onSuccess()
            }
            
            override fun onError(exception: CometChatException?) {
                _isLoading.value = false
                _error.value = exception?.message
                Log.e(TAG, "Failed to delete group: ${exception?.message}")
            }
        })
    }
    
    /**
     * Delete the chat history.
     */
    fun deleteChat(onSuccess: () -> Unit) {
        val currentGroup = _group.value ?: return
        
        _isLoading.value = true
        
        CometChat.deleteConversation(
            currentGroup.guid,
            CometChatConstants.CONVERSATION_TYPE_GROUP,
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
     * Transfer ownership to another member.
     */
    fun transferOwnership(newOwner: User, onSuccess: () -> Unit, onError: (CometChatException) -> Unit) {
        val currentGroup = _group.value ?: return
        
        _isLoading.value = true
        
        CometChat.transferGroupOwnership(
            currentGroup.guid,
            newOwner.uid,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String?) {
                    _isLoading.value = false
                    _isOwner.value = false
                    Log.d(TAG, "Ownership transferred successfully")
                    onSuccess()
                }
                
                override fun onError(exception: CometChatException?) {
                    _isLoading.value = false
                    _error.value = exception?.message
                    Log.e(TAG, "Failed to transfer ownership: ${exception?.message}")
                    exception?.let { onError(it) }
                }
            }
        )
    }
    
    /**
     * Add group listener for real-time updates.
     */
    private fun addGroupListener() {
        val listenerID = "GroupDetailsViewModel_GroupListener"
        
        CometChat.addGroupListener(listenerID, object : CometChat.GroupListener() {
            override fun onGroupMemberJoined(action: Action, joinedUser: User, joinedGroup: Group) {
                if (joinedGroup.guid == _group.value?.guid) {
                    _group.value = joinedGroup
                }
            }
            
            override fun onGroupMemberLeft(action: Action, leftUser: User, leftGroup: Group) {
                if (leftGroup.guid == _group.value?.guid) {
                    _group.value = leftGroup
                    val loggedInUser = CometChat.getLoggedInUser()
                    if (leftUser.uid == loggedInUser?.uid) {
                        _isMember.value = false
                    }
                }
            }
            
            override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, kickedFrom: Group) {
                if (kickedFrom.guid == _group.value?.guid) {
                    _group.value = kickedFrom
                    val loggedInUser = CometChat.getLoggedInUser()
                    if (kickedUser.uid == loggedInUser?.uid) {
                        _isMember.value = false
                    }
                }
            }
            
            override fun onGroupMemberBanned(action: Action, bannedUser: User, bannedBy: User, bannedFrom: Group) {
                if (bannedFrom.guid == _group.value?.guid) {
                    _group.value = bannedFrom
                    val loggedInUser = CometChat.getLoggedInUser()
                    if (bannedUser.uid == loggedInUser?.uid) {
                        _isMember.value = false
                    }
                }
            }
            
            override fun onGroupMemberScopeChanged(
                action: Action,
                updatedBy: User,
                updatedUser: User,
                scopeChangedTo: String,
                scopeChangedFrom: String,
                group: Group
            ) {
                if (group.guid == _group.value?.guid) {
                    _group.value = group
                    val loggedInUser = CometChat.getLoggedInUser()
                    if (updatedUser.uid == loggedInUser?.uid) {
                        _isAdmin.value = scopeChangedTo == CometChatConstants.SCOPE_ADMIN
                        _isModerator.value = scopeChangedTo == CometChatConstants.SCOPE_MODERATOR
                    }
                }
            }
            
            override fun onMemberAddedToGroup(action: Action, addedBy: User, userAdded: User, addedTo: Group) {
                if (addedTo.guid == _group.value?.guid) {
                    _group.value = addedTo
                }
            }
        })
    }
    
    /**
     * Remove listeners when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        CometChat.removeGroupListener("GroupDetailsViewModel_GroupListener")
    }
}
