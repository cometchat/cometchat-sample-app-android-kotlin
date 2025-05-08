package com.cometchat.sampleapp.kotlin.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.GroupListener
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.constants.UIKitConstants.DialogState
import com.cometchat.chatuikit.shared.events.CometChatGroupEvents
import com.cometchat.sampleapp.kotlin.R
import com.cometchat.sampleapp.kotlin.data.repository.Repository

/**
 * ViewModel for managing the state and data of a specific group in the chat
 * application.
 */
class GroupDetailsViewModel : ViewModel() {
    private val GROUP_LISTENER_ID = System.currentTimeMillis().toString() + "_" + javaClass.simpleName

    /**
     * Gets the LiveData for the dialog state related to group operations.
     *
     * @return MutableLiveData object containing the dialog state.
     */
    val dialogState: MutableLiveData<DialogState> = MutableLiveData()

    /**
     * Gets the LiveData for the confirmation dialog state.
     *
     * @return MutableLiveData object containing the confirmation dialog state.
     */
    val confirmDialogState: MutableLiveData<DialogState> = MutableLiveData()

    /**
     * Gets the LiveData for the transfer ownership dialog state.
     *
     * @return MutableLiveData object containing the transfer ownership dialog
     * state.
     */
    val transferOwnershipDialogState: MutableLiveData<DialogState> = MutableLiveData()

    /**
     * Gets the LiveData for error messages that occur during group operations.
     *
     * @return MutableLiveData object containing error messages.
     */
    val errorMessage: MutableLiveData<String?> = MutableLiveData()

    /**
     * Gets the LiveData for the updated group details.
     *
     * @return MutableLiveData object containing the updated Group.
     */
    val updatedGroup: MutableLiveData<Group?> = MutableLiveData()
    private var group: Group? = null

    /**
     * Sets the group for which details are being managed.
     *
     * @param group
     * The Group to be set.
     */
    fun setGroup(group: Group?) {
        this.group = group
    }

    /** Adds listeners for group-related events.  */
    fun addListeners() {
        CometChat.addGroupListener(GROUP_LISTENER_ID, object : GroupListener() {
            override fun onGroupMemberJoined(action: Action, user: User, group_: Group) {
                super.onGroupMemberJoined(action, user, group)
                if (group_.guid == group!!.guid) {
                    group!!.membersCount = group_.membersCount
                    group!!.updatedAt = group_.updatedAt
                    updatedGroup.value = group
                }
            }

            override fun onGroupMemberLeft(action: Action, user: User, group_: Group) {
                super.onGroupMemberLeft(action, user, group_)
                if (group_.guid == group!!.guid) {
                    group!!.membersCount = group_.membersCount
                    group!!.updatedAt = group_.updatedAt
                    updatedGroup.value = group
                }
            }

            override fun onGroupMemberKicked(action: Action, kickedUser: User, kickedBy: User, group_: Group) {
                super.onGroupMemberKicked(action, kickedUser, kickedBy, group_)
                if (group_.guid == group!!.guid) {
                    group!!.membersCount = group_.membersCount
                    group!!.updatedAt = group_.updatedAt

                    if (kickedUser.uid == CometChatUIKit.getLoggedInUser().uid) {
                        group!!.setHasJoined(false)
                    }
                    updatedGroup.value = group
                }
            }

            override fun onGroupMemberBanned(action: Action, bannedUser: User, bannedBy: User, group_: Group) {
                super.onGroupMemberBanned(action, bannedUser, bannedBy, group_)
                if (group_.guid == group!!.guid) {
                    group!!.membersCount = group_.membersCount
                    group!!.updatedAt = group_.updatedAt
                    if (bannedUser.uid == CometChatUIKit.getLoggedInUser().uid) group!!.setHasJoined(false)
                    updatedGroup.value = group
                }
            }

            override fun onGroupMemberScopeChanged(
                action: Action, updatedBy: User, updatedUser: User, scopeChangedTo: String, scopeChangedFrom: String, group_: Group
            ) {
                super.onGroupMemberScopeChanged(action, updatedBy, updatedUser, scopeChangedTo, scopeChangedFrom, group_)
                if (group_.guid == group!!.guid) {
                    if (updatedUser.uid == CometChatUIKit.getLoggedInUser().uid) {
                        group!!.scope = scopeChangedTo
                        updatedGroup.value = group
                    }
                }
            }

            override fun onMemberAddedToGroup(action: Action, user: User, user1: User, group_: Group) {
                if (group_.guid == group!!.guid) {
                    group!!.membersCount = group_.membersCount
                    group!!.updatedAt = group_.updatedAt
                    updatedGroup.value = group
                }
            }
        })

        CometChatGroupEvents.addGroupListener(GROUP_LISTENER_ID, object : CometChatGroupEvents() {
            override fun ccGroupMemberBanned(
                actionMessage: Action, bannedUser: User, bannedBy: User, bannedFrom: Group
            ) {
                updatedGroup.value = bannedFrom
            }

            override fun ccGroupMemberAdded(
                actionMessages: List<Action>, usersAdded: List<User>, userAddedIn: Group, addedBy: User
            ) {
                updatedGroup.value = userAddedIn
            }

            override fun ccOwnershipChanged(
                mGroup: Group, newOwner: GroupMember
            ) {
                updatedGroup.value = mGroup
            }

            override fun ccGroupMemberKicked(
                actionMessage: Action, kickedUser: User, kickedBy: User, kickedFrom: Group?
            ) {
                if (kickedFrom != null) updatedGroup.value = kickedFrom
            }
        })
    }

    /** Removes the added group listeners.  */
    fun removeListeners() {
        CometChat.removeGroupListener(GROUP_LISTENER_ID)
        CometChatGroupEvents.removeListener(GROUP_LISTENER_ID)
    }

    /**
     * Adds members to the current group.
     *
     * @param users
     * The list of Users to be added to the group.
     */
    fun addMembersToGroup(context: Context, users: List<User>) {
        dialogState.value = DialogState.INITIATED
        val groupMembers: MutableList<GroupMember> = ArrayList()

        for (user in users) {
            groupMembers.add(userToGroupMember(user, false, ""))
        }
        if (group == null) return
        Repository.addMembersToGroup(group!!, groupMembers, object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(successMap: HashMap<String, String>) {
                dialogState.value = DialogState.SUCCESS
            }

            override fun onError(e: CometChatException) {
                dialogState.value = DialogState.FAILURE
                handleError(context, e.message!!)
            }
        })
    }

    private fun handleError(context: Context, error: String) {
        val pattern = Regex("UID (.+?) .*?GUID ([^\\s.]+)")
        val matchResult = pattern.find(error)

        if (matchResult == null) {
            postGenericError(context)
            return
        }

        val (uid, guid) = matchResult.destructured

        if (uid.isBlank() || guid.isBlank()) {
            postGenericError(context)
            return
        }

        Repository.getUser(uid, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(user: User) {
                fetchGroupAndPostErrorMessage(context, user, guid)
            }

            override fun onError(e: CometChatException?) {
                postGenericError(context)
            }
        })
    }

    private fun fetchGroupAndPostErrorMessage(context: Context, user: User, guid: String) {
        Repository.getGroup(guid, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                postCompleteError(context, group.name, user.name)
            }

            override fun onError(e: CometChatException?) {
                postGenericError(context)
            }
        })
    }

    private fun postCompleteError(context: Context, groupName: String?, userName: String) {
        val message = if (!groupName.isNullOrBlank()) {
            context.getString(R.string.participant_scope_with_group, userName, groupName)
        } else {
            context.getString(R.string.participant_scope_without_group, userName)
        }

        errorMessage.value = message
    }

    private fun postGenericError(context: Context) {
        errorMessage.value = context.getString(com.cometchat.chatuikit.R.string.cometchat_something_went_wrong)
    }

    /**
     * Allows a user to leave the specified group.
     *
     * @param group
     * The Group to leave.
     */
    fun leaveGroup(group: Group) {
        confirmDialogState.value = DialogState.INITIATED
        Repository.leaveGroup(group, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(s: String) {
                confirmDialogState.value = DialogState.SUCCESS
            }

            override fun onError(e: CometChatException) {
                confirmDialogState.value = DialogState.FAILURE
            }
        })
    }

    /**
     * Deletes the specified group.
     *
     * @param group
     * The Group to delete.
     */
    fun deleteGroup(group: Group) {
        confirmDialogState.value = DialogState.INITIATED
        Repository.deleteGroup(group, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(s: String) {
                confirmDialogState.value = DialogState.SUCCESS
            }

            override fun onError(e: CometChatException) {
                confirmDialogState.value = DialogState.FAILURE
            }
        })
    }

    /**
     * Fetches the current group members and initiates the ownership transfer
     * process.
     */
    fun fetchAndTransferOwnerShip() {
        transferOwnershipDialogState.value = DialogState.INITIATED
        if (group == null) return
        Repository.fetchGroupMembers(group!!, object : CometChat.CallbackListener<List<GroupMember>>() {
            override fun onSuccess(groupMembers: List<GroupMember>) {
                if (groupMembers.isNotEmpty()) {
                    transferOwnership(groupMembers[0])
                } else {
                    transferOwnershipDialogState.setValue(DialogState.FAILURE)
                }
            }

            override fun onError(e: CometChatException) {
                transferOwnershipDialogState.value = DialogState.FAILURE
            }
        })
    }

    /**
     * Transfers ownership of the group to a new owner.
     *
     * @param newOwner
     * The new GroupMember who will become the owner.
     */
    fun transferOwnership(newOwner: GroupMember) {
        transferOwnershipDialogState.value = DialogState.INITIATED
        if (group == null) return
        Repository.transferOwnership(group!!, newOwner, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(s: String?) {
                transferOwnershipDialogState.value = DialogState.SUCCESS
            }

            override fun onError(e: CometChatException) {
                transferOwnershipDialogState.value = DialogState.FAILURE
            }
        })
    }

    /**
     * Converts a User object to a GroupMember object.
     *
     * @param user
     * The User to be converted.
     * @param isScopeUpdate
     * Indicates if this is a scope update.
     * @param newScope
     * The new scope if it is a scope update.
     * @return The converted GroupMember.
     */
    fun userToGroupMember(
        user: User, isScopeUpdate: Boolean, newScope: String?
    ): GroupMember {
        val groupMember = if (isScopeUpdate) GroupMember(user.uid, newScope)
        else GroupMember(user.uid, CometChatConstants.SCOPE_PARTICIPANT)
        groupMember.avatar = user.avatar
        groupMember.name = user.name
        groupMember.status = user.status
        return groupMember
    }
}
