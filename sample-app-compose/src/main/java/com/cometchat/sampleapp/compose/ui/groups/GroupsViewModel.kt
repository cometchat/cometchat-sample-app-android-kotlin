package com.cometchat.sampleapp.compose.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants.DialogState
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the state and data of groups in the app flow.
 * Handles group join and create operations with dialog state management.
 */
class GroupsViewModel : ViewModel() {

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> = _dialogState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _joinedGroup = MutableSharedFlow<Group>()
    val joinedGroup: SharedFlow<Group> = _joinedGroup.asSharedFlow()

    private val _createdGroup = MutableSharedFlow<Group>()
    val createdGroup: SharedFlow<Group> = _createdGroup.asSharedFlow()

    /**
     * Joins a password-protected group.
     *
     * @param group The Group to join
     * @param password The password for the group (empty for public groups)
     */
    fun joinPasswordGroup(group: Group, password: String?) {
        _dialogState.value = DialogState.INITIATED
        _error.value = null

        CometChat.joinGroup(
            group.guid,
            group.groupType,
            password,
            object : CometChat.CallbackListener<Group>() {
                override fun onSuccess(joinedGroup: Group) {
                    _dialogState.value = DialogState.SUCCESS
                    viewModelScope.launch {
                        _joinedGroup.emit(joinedGroup)
                    }
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
        _error.value = null

        CometChat.createGroup(
            group,
            object : CometChat.CallbackListener<Group>() {
                override fun onSuccess(createdGroup: Group) {
                    _dialogState.value = DialogState.SUCCESS
                    viewModelScope.launch {
                        _createdGroup.emit(createdGroup)
                    }
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
     * Resets the dialog state.
     */
    fun resetDialogState() {
        _dialogState.value = null
        _error.value = null
    }
}
