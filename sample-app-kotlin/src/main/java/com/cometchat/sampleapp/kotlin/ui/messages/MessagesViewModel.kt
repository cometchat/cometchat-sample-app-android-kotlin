package com.cometchat.sampleapp.kotlin.ui.messages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.GroupListener
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatConversationEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.core.events.CometChatUserEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the state and data of messages in a chat.
 * Handles user/group updates, block status, and navigation events.
 */
class MessagesViewModel : ViewModel() {

    companion object {
        private const val TAG = "MessagesViewModel"
    }
    
    private val listenerId = System.currentTimeMillis().toString() + javaClass.simpleName
    
    private var mUser: User? = null
    private var mGroup: Group? = null

    private val _baseMessage = MutableLiveData<BaseMessage?>()
    val baseMessage: LiveData<BaseMessage?> = _baseMessage

    private val _updatedGroup = MutableLiveData<Group>()
    val updatedGroup: LiveData<Group> = _updatedGroup

    private val _updateUser = MutableLiveData<User>()
    val updateUser: LiveData<User> = _updateUser

    private val _openUserChat = MutableLiveData<User?>()
    val openUserChat: LiveData<User?> = _openUserChat

    private val _isExitActivity = MutableLiveData<Boolean>()
    val isExitActivity: LiveData<Boolean> = _isExitActivity

    private val _unblockButtonState = MutableLiveData<UIKitConstants.DialogState>()
    val unblockButtonState: LiveData<UIKitConstants.DialogState> = _unblockButtonState

    // Coroutine jobs for Flow collection
    private var groupEventsJob: Job? = null
    private var userEventsJob: Job? = null
    private var uiEventsJob: Job? = null
    private var conversationEventsJob: Job? = null

    fun setGroup(group: Group?) {
        mGroup = group
    }

    fun setUser(user: User?) {
        mUser = user
        // Fetch fresh user data from server to ensure blocked status is current
        user?.let { fetchFreshUserData(it.uid) }
    }

    private fun fetchFreshUserData(uid: String) {
        CometChat.getUser(uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(freshUser: User) {
                mUser = freshUser
                _updateUser.postValue(freshUser)
            }

            override fun onError(e: CometChatException) {
                Log.e(TAG, "Failed to fetch fresh user data: ${e.message}")
            }
        })
    }

    fun getUser(): User? = mUser

    fun getGroup(): Group? = mGroup

    fun addListener() {
        // Group SDK listener for real-time events from server
        CometChat.addGroupListener(listenerId, object : GroupListener() {
            override fun onGroupMemberLeft(action: Action, user: User, group: Group) {
                updateGroupJoinedStatus(group, user, false)
            }

            override fun onGroupMemberKicked(action: Action, user: User, user1: User, group: Group) {
                updateGroupJoinedStatus(group, user, false)
            }

            override fun onGroupMemberBanned(action: Action, user: User, user1: User, group: Group) {
                updateGroupJoinedStatus(group, user, false)
            }

            override fun onMemberAddedToGroup(action: Action, addedBy: User, userAdded: User, addedTo: Group) {
                updateGroupJoinedStatus(addedTo, userAdded, true)
            }

            override fun onGroupMemberJoined(action: Action, user: User, group: Group) {
                updateGroupJoinedStatus(group, user, true)
            }
        })

        // UIKit Group Events - Flow-based collection
        groupEventsJob = viewModelScope.launch {
            CometChatEvents.groupEvents.collect { event ->
                when (event) {
                    is CometChatGroupEvent.GroupDeleted -> {
                        _isExitActivity.postValue(true)
                    }
                    is CometChatGroupEvent.GroupLeft -> {
                        _isExitActivity.postValue(true)
                    }
                    else -> { /* Handle other group events if needed */ }
                }
            }
        }

        // UIKit User Events - Flow-based collection
        userEventsJob = viewModelScope.launch {
            CometChatEvents.userEvents.collect { event ->
                when (event) {
                    is CometChatUserEvent.UserBlocked -> {
                        _updateUser.postValue(event.user)
                    }
                    is CometChatUserEvent.UserUnblocked -> {
                        _updateUser.postValue(event.user)
                    }
                }
            }
        }

        // UIKit UI Events - Flow-based collection
        uiEventsJob = viewModelScope.launch {
            CometChatEvents.uiEvents.collect { event ->
                when (event) {
                    is CometChatUIEvent.ActiveChatChanged -> {
                        _baseMessage.postValue(event.message)
                    }
                    is CometChatUIEvent.OpenChat -> {
                        _openUserChat.postValue(event.user)
                    }
                    else -> { /* Handle other UI events if needed */ }
                }
            }
        }

        // UIKit Conversation Events - Flow-based collection
        conversationEventsJob = viewModelScope.launch {
            CometChatEvents.conversationEvents.collect { event ->
                when (event) {
                    is CometChatConversationEvent.ConversationDeleted -> {
                        _isExitActivity.postValue(true)
                    }
                    else -> { /* Handle other conversation events if needed */ }
                }
            }
        }
    }

    fun removeListener() {
        CometChat.removeGroupListener(listenerId)
        // Cancel Flow collection jobs
        groupEventsJob?.cancel()
        userEventsJob?.cancel()
        uiEventsJob?.cancel()
        conversationEventsJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        removeListener()
    }

    private fun updateGroupJoinedStatus(group: Group?, user: User, isJoined: Boolean) {
        val loggedInUser = CometChat.getLoggedInUser()
        if (group != null && mGroup != null && group.guid == mGroup!!.guid) {
            if (loggedInUser != null && user.uid == loggedInUser.uid) {
                group.setHasJoined(isJoined)
                _updatedGroup.postValue(group)
            }
        }
    }

    fun unblockUser() {
        val user = mUser ?: return
        _unblockButtonState.value = UIKitConstants.DialogState.INITIATED
        
        val userIds = listOf(user.uid)
        CometChat.unblockUsers(userIds, object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(resultMap: HashMap<String, String>) {
                if ("success".equals(resultMap[user.uid], ignoreCase = true)) {
                    user.isBlockedByMe = false
                    CometChatEvents.emitUserEvent(CometChatUserEvent.UserUnblocked(user))
                    _updateUser.value = user
                    _unblockButtonState.value = UIKitConstants.DialogState.SUCCESS
                } else {
                    _unblockButtonState.value = UIKitConstants.DialogState.FAILURE
                }
            }

            override fun onError(e: CometChatException) {
                _unblockButtonState.value = UIKitConstants.DialogState.FAILURE
            }
        })
    }
}
