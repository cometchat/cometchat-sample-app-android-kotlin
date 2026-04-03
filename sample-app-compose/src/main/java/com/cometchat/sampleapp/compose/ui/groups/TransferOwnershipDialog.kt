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
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.compose.presentation.groupmembers.ui.CometChatGroupMembers
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Dialog for transferring group ownership to another member.
 *
 * This dialog is shown when the owner tries to delete a group that has
 * more than one member. The owner must transfer ownership before leaving.
 *
 * @param group The group to transfer ownership for
 * @param onDismiss Callback when dialog is dismissed
 * @param onOwnershipTransferred Callback when ownership is transferred successfully
 */
@Composable
fun TransferOwnershipDialog(
    group: Group,
    onDismiss: () -> Unit,
    onOwnershipTransferred: () -> Unit
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
            CometChatGroupMembers(
                group = group,
                title = "Transfer Ownership",
                hideBackButton = false,
                selectionMode = UIKitConstants.SelectionMode.SINGLE,
                onBackPress = onDismiss,
                onSelection = { selectedMembers ->
                    if (selectedMembers.isNotEmpty()) {
                        val newOwner = selectedMembers.first()
                        transferOwnership(
                            group = group,
                            newOwner = newOwner,
                            onSuccess = onOwnershipTransferred,
                            onError = { exception ->
                                Log.e("TransferOwnershipDialog", "Failed to transfer: ${exception.message}")
                            }
                        )
                    }
                }
            )
        }
    }
}

/**
 * Transfer ownership to the selected member.
 */
private fun transferOwnership(
    group: Group,
    newOwner: GroupMember,
    onSuccess: () -> Unit,
    onError: (CometChatException) -> Unit
) {
    CometChat.transferGroupOwnership(
        group.guid,
        newOwner.uid,
        object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                Log.d("TransferOwnershipDialog", "Ownership transferred successfully")
                onSuccess()
            }

            override fun onError(exception: CometChatException?) {
                exception?.let { onError(it) }
            }
        }
    )
}
