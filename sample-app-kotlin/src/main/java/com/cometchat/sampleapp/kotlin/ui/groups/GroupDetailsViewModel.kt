package com.cometchat.sampleapp.kotlin.ui.groups

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.GroupListener
import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.helpers.CometChatHelper
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.AppEntity
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatConversationEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class DialogState {
    INITIATED, SUCCESS, FAILURE
}

class GroupDetailsViewModel : ViewModel() {
    
    private val GROUP_LISTENER_ID = System.currentTimeMillis().toString() + "_" + javaClass.simpleName
    
    val dialogState: MutableLiveData<DialogState> = MutableLiveData()
    val confirmDialogState: MutableLiveData<DialogState> = MutableLiveData()
    val transferOwnershipDialogState: MutableLiveData<DialogState> = MutableLiveData()
    val errorMessage: MutableLiveData<String?> = MutableLiveData()
    val updatedGroup: MutableLiveData<Group?> = MutableLiveData()
    
    private var group: Group? = null
    private var baseMessage: BaseMessage? = null
    private var groupEventsJob: Job? = null
    
    fun setGroup(group: Group?) {
        this.group = group
    }
    
    fun setBaseMessage(baseMessage: BaseMessage?) {
        this.baseMessage = baseMessage
    }

    fun addListeners() {
        CometChat.addGroupListener(GROUP_LISTENER_ID, object : GroupListener() {
            override fun onGroupMemberJoined(action: Action, user: User, group_: Group) {
                if (group_.guid == group?.guid) {
                    group?.membersCount = group_.membersCount
                    group?.updatedAt = group_.updatedAt
                    updatedGroup.postValue(group)
                }
            }
            
            override fun onGroupMemberLeft(action: Action, user: User, group_: Group) {
                if (group_.guid == group?.guid) {
                    group?.membersCount = group_.membersCount
                    group?.updatedAt = group_.updatedAt
                    updatedGroup.postValue(group)
                }
            }
            
            override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, group_: Group) {
                if (group_.guid == group?.guid) {
                    group?.membersCount = group_.membersCount
                    group?.updatedAt = group_.updatedAt
                    if (kickedUser.uid == CometChatUIKit.getLoggedInUser()?.uid) {
                        group?.setHasJoined(false)
                    }
                    updatedGroup.postValue(group)
                }
            }
            
            override fun onGroupMemberBanned(action: Action, bannedUser: User, bannedBy: User, group_: Group) {
                if (group_.guid == group?.guid) {
                    group?.membersCount = group_.membersCount
                    group?.updatedAt = group_.updatedAt
                    if (bannedUser.uid == CometChatUIKit.getLoggedInUser()?.uid) {
                        group?.setHasJoined(false)
                    }
                    updatedGroup.postValue(group)
                }
            }
            
            override fun onGroupMemberScopeChanged(
                action: Action, updatedBy: User, updatedUser: User,
                scopeChangedTo: String, scopeChangedFrom: String, group_: Group
            ) {
                if (group_.guid == group?.guid) {
                    if (updatedUser.uid == CometChatUIKit.getLoggedInUser()?.uid) {
                        group?.scope = scopeChangedTo
                        updatedGroup.postValue(group)
                    }
                }
            }
            
            override fun onMemberAddedToGroup(action: Action, user: User, user1: User, group_: Group) {
                if (group_.guid == group?.guid) {
                    group?.membersCount = group_.membersCount
                    group?.updatedAt = group_.updatedAt
                    updatedGroup.postValue(group)
                }
            }
        })
        
        // Subscribe to UIKit group events using Flow
        groupEventsJob = viewModelScope.launch {
            CometChatEvents.groupEvents.collect { event ->
                when (event) {
                    is CometChatGroupEvent.MemberBanned -> {
                        if (event.group.guid == group?.guid) {
                            updatedGroup.postValue(event.group)
                        }
                    }
                    is CometChatGroupEvent.MembersAdded -> {
                        if (event.group.guid == group?.guid) {
                            updatedGroup.postValue(event.group)
                        }
                    }
                    is CometChatGroupEvent.OwnershipChanged -> {
                        if (event.group.guid == group?.guid) {
                            updatedGroup.postValue(event.group)
                        }
                    }
                    is CometChatGroupEvent.MemberKicked -> {
                        if (event.group.guid == group?.guid) {
                            updatedGroup.postValue(event.group)
                        }
                    }
                    else -> { /* Handle other events if needed */ }
                }
            }
        }
    }
    
    fun removeListeners() {
        CometChat.removeGroupListener(GROUP_LISTENER_ID)
        groupEventsJob?.cancel()
        groupEventsJob = null
    }

    fun addMembersToGroup(context: Context, users: List<User>) {
        dialogState.value = DialogState.INITIATED
        val groupMembers = users.map { userToGroupMember(it, false, null) }.toMutableList()
        
        if (group == null) return
        
        CometChat.addMembersToGroup(
            group!!.guid,
            groupMembers,
            null,
            object : CometChat.CallbackListener<HashMap<String, String>>() {
                override fun onSuccess(successMap: HashMap<String, String>) {
                    var addedCount = 0
                    for ((key, value) in successMap) {
                        if (value.startsWith("already_member")) {
                            groupMembers.removeIf { it.uid == key }
                        }
                        if (value == "success") addedCount++
                    }
                    group!!.membersCount += addedCount
                    
                    val members: List<User> = ArrayList(groupMembers)
                    val actions = members.map { user ->
                        getGroupActionMessage(user, group!!, group!!, group!!.guid).apply {
                            action = CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED
                        }
                    }
                    
                    CometChatEvents.emitGroupEvent(
                        CometChatGroupEvent.MembersAdded(
                            actions = actions,
                            users = members,
                            group = group!!,
                            addedBy = CometChatUIKit.getLoggedInUser() ?: return
                        )
                    )
                    dialogState.value = DialogState.SUCCESS
                }
                
                override fun onError(e: CometChatException) {
                    dialogState.value = DialogState.FAILURE
                    handleError(context, e.message ?: "")
                }
            }
        )
    }
    
    private fun handleError(context: Context, error: String) {
        val pattern = Regex("UID (.+?) .*?GUID ([^\\s.]+)")
        val matchResult = pattern.find(error)
        
        if (matchResult == null) {
            errorMessage.value = context.getString(com.cometchat.uikit.kotlin.R.string.cometchat_something_went_wrong)
            return
        }
        
        val (uid, guid) = matchResult.destructured
        if (uid.isBlank() || guid.isBlank()) {
            errorMessage.value = context.getString(com.cometchat.uikit.kotlin.R.string.cometchat_something_went_wrong)
            return
        }
        
        CometChat.getUser(uid, object : CometChat.CallbackListener<User?>() {
            override fun onSuccess(user: User?) {
                user?.let { fetchGroupAndPostErrorMessage(context, it, guid) }
                    ?: run { errorMessage.value = context.getString(com.cometchat.uikit.kotlin.R.string.cometchat_something_went_wrong) }
            }
            
            override fun onError(e: CometChatException?) {
                errorMessage.value = context.getString(com.cometchat.uikit.kotlin.R.string.cometchat_something_went_wrong)
            }
        })
    }
    
    private fun fetchGroupAndPostErrorMessage(context: Context, user: User, guid: String) {
        CometChat.getGroup(guid, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                errorMessage.value = context.getString(R.string.participant_scope_with_group, user.name, group.name)
            }
            
            override fun onError(e: CometChatException?) {
                errorMessage.value = context.getString(R.string.participant_scope_without_group, user.name)
            }
        })
    }

    fun leaveGroup(group: Group) {
        confirmDialogState.value = DialogState.INITIATED
        CometChat.leaveGroup(group.guid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(s: String) {
                group.setHasJoined(false)
                group.membersCount -= 1
                val action = getGroupActionMessage(
                    CometChatUIKit.getLoggedInUser(), group, group, group.guid
                )
                action.action = CometChatConstants.ActionKeys.ACTION_LEFT
                CometChatEvents.emitGroupEvent(
                    CometChatGroupEvent.GroupLeft(
                        action = action,
                        user = CometChatUIKit.getLoggedInUser() ?: return,
                        group = group
                    )
                )
                confirmDialogState.value = DialogState.SUCCESS
            }
            
            override fun onError(e: CometChatException) {
                confirmDialogState.value = DialogState.FAILURE
            }
        })
    }
    
    fun deleteGroup(group: Group) {
        confirmDialogState.value = DialogState.INITIATED
        CometChat.deleteGroup(group.guid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(s: String) {
                CometChatEvents.emitGroupEvent(
                    CometChatGroupEvent.GroupDeleted(group = group)
                )
                confirmDialogState.value = DialogState.SUCCESS
            }
            
            override fun onError(e: CometChatException) {
                confirmDialogState.value = DialogState.FAILURE
            }
        })
    }
    
    fun deleteChat() {
        confirmDialogState.value = DialogState.INITIATED
        if (group == null) {
            confirmDialogState.value = DialogState.FAILURE
            return
        }
        
        CometChat.deleteConversation(
            group!!.guid,
            CometChatConstants.CONVERSATION_TYPE_GROUP,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(s: String) {
                    val conversation = if (baseMessage != null) {
                        CometChatHelper.getConversationFromMessage(baseMessage)
                    } else {
                        Conversation("", CometChatConstants.CONVERSATION_TYPE_GROUP)
                    }
                    CometChatEvents.emitConversationEvent(
                        CometChatConversationEvent.ConversationDeleted(conversation = conversation)
                    )
                    confirmDialogState.value = DialogState.SUCCESS
                }
                
                override fun onError(e: CometChatException) {
                    confirmDialogState.value = DialogState.FAILURE
                }
            }
        )
    }
    
    fun fetchAndTransferOwnerShip() {
        transferOwnershipDialogState.value = DialogState.INITIATED
        if (group == null) return
        
        val request = GroupMembersRequest.GroupMembersRequestBuilder(group!!.guid)
            .setLimit(30)
            .build()
        
        request.fetchNext(object : CometChat.CallbackListener<MutableList<GroupMember>>() {
            override fun onSuccess(groupMembers: MutableList<GroupMember>) {
                groupMembers.removeIf { it.uid == CometChatUIKit.getLoggedInUser()?.uid }
                if (groupMembers.isNotEmpty()) {
                    transferOwnership(groupMembers[0])
                } else {
                    transferOwnershipDialogState.value = DialogState.FAILURE
                }
            }
            
            override fun onError(e: CometChatException) {
                transferOwnershipDialogState.value = DialogState.FAILURE
            }
        })
    }
    
    fun transferOwnership(newOwner: GroupMember) {
        transferOwnershipDialogState.value = DialogState.INITIATED
        if (group == null) return
        
        CometChat.transferGroupOwnership(group!!.guid, newOwner.uid, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(s: String?) {
                group!!.owner = newOwner.uid
                group!!.scope = UIKitConstants.GroupMemberScope.ADMIN
                CometChatEvents.emitGroupEvent(
                    CometChatGroupEvent.OwnershipChanged(
                        group = group!!,
                        newOwner = newOwner
                    )
                )
                transferOwnershipDialogState.value = DialogState.SUCCESS
            }
            
            override fun onError(e: CometChatException) {
                transferOwnershipDialogState.value = DialogState.FAILURE
            }
        })
    }
    
    private fun userToGroupMember(user: User, isScopeUpdate: Boolean, newScope: String?): GroupMember {
        val groupMember = if (isScopeUpdate && newScope != null) {
            GroupMember(user.uid, newScope)
        } else {
            GroupMember(user.uid, CometChatConstants.SCOPE_PARTICIPANT)
        }
        groupMember.avatar = user.avatar
        groupMember.name = user.name
        groupMember.status = user.status
        return groupMember
    }
    
    /**
     * Creates an Action message for group operations.
     * This is a local implementation replacing Utils.getGroupActionMessage from the Java UIKit.
     */
    private fun getGroupActionMessage(actionOn: AppEntity?, actionFor: Group, receiver: Group, uid: String): Action {
        return Action().apply {
            setActionBy(CometChatUIKit.getLoggedInUser())
            setActionOn(actionOn)
            setActionFor(actionFor)
            receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
            setReceiver(receiver)
            category = CometChatConstants.CATEGORY_ACTION
            type = CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER
            receiverUid = uid
            sender = CometChatUIKit.getLoggedInUser()
            sentAt = System.currentTimeMillis() / 1000
            conversationId = "group_${actionFor.guid}"
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}
