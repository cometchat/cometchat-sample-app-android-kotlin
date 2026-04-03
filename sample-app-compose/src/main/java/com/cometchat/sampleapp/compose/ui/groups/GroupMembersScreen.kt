package com.cometchat.sampleapp.compose.ui.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.presentation.groupmembers.ui.CometChatGroupMembers
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Full-screen composable for viewing group members.
 *
 * Displays the CometChatGroupMembers component in a full-screen format,
 * matching the master-app-jetpack behavior.
 *
 * @param groupId The GUID of the group to show members for
 * @param onBackPress Callback when back button is pressed
 */
@Composable
fun GroupMembersScreen(
    groupId: String,
    onBackPress: () -> Unit
) {
    val colorScheme = CometChatTheme.colorScheme
    var group by remember { mutableStateOf<Group?>(null) }

    // Fetch group data
    LaunchedEffect(groupId) {
        CometChat.getGroup(groupId, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(fetchedGroup: Group) {
                group = fetchedGroup
            }

            override fun onError(e: CometChatException) {
                // Handle error - navigate back
                onBackPress()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor1)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        group?.let { g ->
            CometChatGroupMembers(
                group = g,
                title = "Members",
                hideBackButton = false,
                onBackPress = onBackPress
            )
        }
    }
}
