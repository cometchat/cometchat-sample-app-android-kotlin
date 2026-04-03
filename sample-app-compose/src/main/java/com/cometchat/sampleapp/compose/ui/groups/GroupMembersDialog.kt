package com.cometchat.sampleapp.compose.ui.groups

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
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.presentation.groupmembers.ui.CometChatGroupMembers
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Dialog for viewing group members.
 *
 * Displays the CometChatGroupMembers component in a dialog format,
 * allowing users to view all members of a group.
 *
 * @param group The group to show members for
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun GroupMembersDialog(
    group: Group,
    onDismiss: () -> Unit
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
                title = "Members",
                hideBackButton = false,
                onBackPress = onDismiss
            )
        }
    }
}
