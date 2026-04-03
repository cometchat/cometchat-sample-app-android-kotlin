package com.cometchat.sampleapp.compose.ui.groups

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cometchat.chat.core.BannedGroupMembersRequest
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsersListItem
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Dialog for viewing and managing banned members.
 *
 * Displays a list of banned members with the option to unban them.
 *
 * @param group The group to show banned members for
 * @param onDismiss Callback when dialog is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannedMembersDialog(
    group: Group,
    onDismiss: () -> Unit
) {
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val textColorPrimary = CometChatTheme.colorScheme.textColorPrimary

    var bannedMembers by remember { mutableStateOf<List<GroupMember>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch banned members
    LaunchedEffect(group) {
        fetchBannedMembers(
            group = group,
            onSuccess = { members ->
                bannedMembers = members
                isLoading = false
            },
            onError = { exception ->
                error = exception.message
                isLoading = false
            }
        )
    }

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
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Banned Members",
                            style = CometChatTheme.typography.heading3Bold,
                            color = textColorPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(id = R.drawable.cometchat_ic_back),
                                contentDescription = "Back",
                                tint = CometChatTheme.colorScheme.iconTintPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor
                    )
                )

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = CometChatTheme.colorScheme.primary
                            )
                        }
                    }
                    bannedMembers.isEmpty() -> {
                        CometChatEmptyState(
                            title = "No Banned Members",
                            subtitle = "There are no banned members in this group"
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(bannedMembers, key = { it.uid }) { member ->
                                BannedMemberItem(
                                    member = member,
                                    group = group,
                                    onUnban = {
                                        bannedMembers = bannedMembers.filter { it.uid != member.uid }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item for displaying a banned member with unban option.
 */
@Composable
private fun BannedMemberItem(
    member: GroupMember,
    group: Group,
    onUnban: () -> Unit
) {
    CometChatUsersListItem(
        user = member,
        onItemClick = { /* No action */ },
        trailingView = {
            IconButton(
                onClick = {
                    unbanMember(
                        group = group,
                        member = member,
                        onSuccess = onUnban,
                        onError = { exception ->
                            Log.e("BannedMembersDialog", "Failed to unban: ${exception.message}")
                        }
                    )
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cometchat_ic_close),
                    contentDescription = "Unban",
                    tint = CometChatTheme.colorScheme.errorColor
                )
            }
        }
    )
}

/**
 * Fetch banned members from the group.
 */
private fun fetchBannedMembers(
    group: Group,
    onSuccess: (List<GroupMember>) -> Unit,
    onError: (CometChatException) -> Unit
) {
    val request = BannedGroupMembersRequest.BannedGroupMembersRequestBuilder(group.guid)
        .setLimit(30)
        .build()

    request.fetchNext(object : CometChat.CallbackListener<List<GroupMember>>() {
        override fun onSuccess(members: List<GroupMember>?) {
            onSuccess(members ?: emptyList())
        }

        override fun onError(exception: CometChatException?) {
            exception?.let { onError(it) }
        }
    })
}

/**
 * Unban a member from the group.
 */
private fun unbanMember(
    group: Group,
    member: GroupMember,
    onSuccess: () -> Unit,
    onError: (CometChatException) -> Unit
) {
    CometChat.unbanGroupMember(
        member.uid,
        group.guid,
        object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String?) {
                Log.d("BannedMembersDialog", "Member unbanned successfully")
                onSuccess()
            }

            override fun onError(exception: CometChatException?) {
                exception?.let { onError(it) }
            }
        }
    )
}
