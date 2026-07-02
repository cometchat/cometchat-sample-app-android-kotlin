package com.cometchat.sampleapp.compose.push.appflow.tabs

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.groups.ui.CometChatGroups
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import com.cometchat.sampleapp.compose.push.appflow.dialogs.CreateGroupDialog
import com.cometchat.sampleapp.compose.push.appflow.dialogs.JoinPasswordGroupDialog

/**
 * Groups tab displaying the groups list.
 * Supports joining groups (public, private, password-protected) and creating new groups.
 *
 * @param onGroupClick Callback when a group is clicked (after joining if needed)
 * @param contentPadding Padding from the scaffold
 */
@Composable
fun GroupsTab(
    onGroupClick: (Group) -> Unit,
    contentPadding: PaddingValues
) {
    var showJoinPasswordDialog by remember { mutableStateOf<Group?>(null) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var isJoining by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        CometChatGroups(
            modifier = Modifier.fillMaxSize(),
            title = "Groups",
            hideBackIcon = true,
            hideSeparator = true,
            overflowMenu = {
                // Create group button
                Icon(
                    painter = painterResource(id = com.cometchat.sampleapp.compose.push.R.drawable.ic_create_group),
                    contentDescription = "Create Group",
                    tint = CometChatTheme.colorScheme.iconTintHighlight,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showCreateGroupDialog = true }
                )
            },
            onItemClick = { group ->
                handleGroupClick(
                    group = group,
                    isJoining = isJoining,
                    onJoiningStateChange = { isJoining = it },
                    onShowPasswordDialog = { showJoinPasswordDialog = it },
                    onGroupClick = onGroupClick
                )
            },
            onError = { exception ->
                Log.e("GroupsTab", "Error: ${exception.message}")
            }
        )
    }
    
    // Join password group dialog
    showJoinPasswordDialog?.let { group ->
        JoinPasswordGroupDialog(
            group = group,
            onDismiss = { showJoinPasswordDialog = null },
            onJoinSuccess = { joinedGroup ->
                showJoinPasswordDialog = null
                onGroupClick(joinedGroup)
            },
            onJoinError = { exception ->
                Log.e("GroupsTab", "Join failed: ${exception.message}")
            }
        )
    }
    
    // Create group dialog
    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onGroupCreated = { createdGroup ->
                showCreateGroupDialog = false
                onGroupClick(createdGroup)
            },
            onError = { exception ->
                Log.e("GroupsTab", "Create group failed: ${exception.message}")
            }
        )
    }
}

/**
 * Handles group click based on join status and group type.
 */
private fun handleGroupClick(
    group: Group,
    isJoining: Boolean,
    onJoiningStateChange: (Boolean) -> Unit,
    onShowPasswordDialog: (Group?) -> Unit,
    onGroupClick: (Group) -> Unit
) {
    if (isJoining) return
    
    // Check if already joined
    if (group.isJoined) {
        onGroupClick(group)
        return
    }
    
    // Handle based on group type
    when (group.groupType) {
        CometChatConstants.GROUP_TYPE_PUBLIC -> {
            // Join public group directly
            joinGroup(
                group = group,
                password = "",
                onJoiningStateChange = onJoiningStateChange,
                onSuccess = onGroupClick,
                onError = { exception ->
                    Log.e("GroupsTab", "Join public group failed: ${exception.message}")
                }
            )
        }
        CometChatConstants.GROUP_TYPE_PASSWORD -> {
            // Show password dialog
            onShowPasswordDialog(group)
        }
        CometChatConstants.GROUP_TYPE_PRIVATE -> {
            // Private groups cannot be joined directly
            Log.d("GroupsTab", "Private group - cannot join directly")
        }
    }
}

/**
 * Joins a group with optional password.
 */
private fun joinGroup(
    group: Group,
    password: String,
    onJoiningStateChange: (Boolean) -> Unit,
    onSuccess: (Group) -> Unit,
    onError: (CometChatException) -> Unit
) {
    onJoiningStateChange(true)
    
    CometChat.joinGroup(
        group.guid,
        group.groupType,
        password,
        object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(joinedGroup: Group?) {
                onJoiningStateChange(false)
                joinedGroup?.let { 
                    // Emit member joined event so ConversationList can update
                    CometChatUIKit.getLoggedInUser()?.let { currentUser ->
                        CometChatEvents.emitGroupEvent(
                            CometChatGroupEvent.MemberJoined(currentUser, it)
                        )
                    }
                    onSuccess(it) 
                }
            }
            
            override fun onError(exception: CometChatException?) {
                onJoiningStateChange(false)
                exception?.let { onError(it) }
            }
        }
    )
}
