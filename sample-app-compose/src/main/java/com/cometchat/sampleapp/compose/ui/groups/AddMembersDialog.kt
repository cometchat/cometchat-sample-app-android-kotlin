package com.cometchat.sampleapp.compose.ui.groups

import android.util.Log
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Dialog for adding members to a group.
 *
 * Displays the CometChatUsers component with multi-selection mode,
 * allowing users to select and add multiple members to a group.
 *
 * @param group The group to add members to
 * @param onDismiss Callback when dialog is dismissed
 * @param onMembersAdded Callback when members are added successfully
 */
@Composable
fun AddMembersDialog(
    group: Group,
    onDismiss: () -> Unit,
    onMembersAdded: () -> Unit
) {
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            CometChatUsers(
                title = "Add Members",
                hideBackIcon = false,
                selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                onBackPress = onDismiss,
                onSelection = { selectedUsers ->
                    if (selectedUsers.isNotEmpty()) {
                        addMembersToGroup(
                            group = group,
                            users = selectedUsers,
                            onSuccess = onMembersAdded,
                            onError = { exception ->
                                Log.e("AddMembersDialog", "Failed to add members: ${exception.message}")
                            }
                        )
                    }
                }
            )
        }
    }
}

/**
 * Add selected users as members to the group.
 */
private fun addMembersToGroup(
    group: Group,
    users: List<User>,
    onSuccess: () -> Unit,
    onError: (CometChatException) -> Unit
) {
    val groupMembers = users.map { user ->
        GroupMember(user.uid, CometChatConstants.SCOPE_PARTICIPANT)
    }

    CometChat.addMembersToGroup(
        group.guid,
        groupMembers,
        null,
        object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(result: HashMap<String, String>?) {
                Log.d("AddMembersDialog", "Members added successfully")
                onSuccess()
            }

            override fun onError(exception: CometChatException?) {
                exception?.let { onError(it) }
            }
        }
    )
}
