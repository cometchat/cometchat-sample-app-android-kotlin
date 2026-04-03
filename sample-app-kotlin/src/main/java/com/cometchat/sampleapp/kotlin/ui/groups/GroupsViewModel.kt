package com.cometchat.sampleapp.kotlin.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants.DialogState
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent

/**
 * ViewModel for managing the state and data of groups in the app flow.
 * Handles group join and create operations with dialog state management.
 *
 * **Requirements:**
 * - 6.10: Handle join/create operations with DialogState (INITIATED, SUCCESS, FAILURE)
 */
class GroupsViewModel : ViewModel() {

    private val _joinedGroup = MutableLiveData<Group>()
    val joinedGroup: LiveData<Group> = _joinedGroup

    private val _createdGroup = MutableLiveData<Group>()
    val createdGroup: LiveData<Group> = _createdGroup

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _dialogState = MutableLiveData<DialogState>()
    val dialogState: LiveData<DialogState> = _dialogState

    /**
     * Joins a password-protected group.
     *
     * @param group The Group to join
     * @param password The password for the group (empty for public groups)
     */
    fun joinPasswordGroup(group: Group, password: String?) {
        _dialogState.value = DialogState.INITIATED
        
        CometChat.joinGroup(
            group.guid,
            group.groupType,
            password,
            object : CometChat.CallbackListener<Group>() {
                override fun onSuccess(joinedGroup: Group) {
                    _dialogState.value = DialogState.SUCCESS
                    _joinedGroup.value = joinedGroup
                    // Emit member joined event so ConversationList can update
                    CometChatUIKit.getLoggedInUser()?.let { currentUser ->
                        CometChatEvents.emitGroupEvent(
                            CometChatGroupEvent.MemberJoined(currentUser, joinedGroup)
                        )
                    }
                }

                override fun onError(e: CometChatException) {
                    _dialogState.value = DialogState.FAILURE
                    _error.value = e.message
                }
            }
        )
    }

    /**
     * Creates a new group.
     *
     * @param group The Group to be created
     */
    fun createGroup(group: Group) {
        _dialogState.value = DialogState.INITIATED
        
        CometChat.createGroup(
            group,
            object : CometChat.CallbackListener<Group>() {
                override fun onSuccess(createdGroup: Group) {
                    _dialogState.value = DialogState.SUCCESS
                    _createdGroup.value = createdGroup
                    // Emit group created event so ConversationList can update
                    CometChatEvents.emitGroupEvent(CometChatGroupEvent.GroupCreated(createdGroup))
                }

                override fun onError(e: CometChatException) {
                    _dialogState.value = DialogState.FAILURE
                    _error.value = e.message
                }
            }
        )
    }

    /**
     * Dismisses any active dialog by resetting the dialog state.
     */
    fun dismissDialog() {
        // Reset error when dialog is dismissed
        _error.value = null
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _error.value = null
    }
}
