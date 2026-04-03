package com.cometchat.sampleapp.compose.ui.groups

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatGroupEvent
import com.cometchat.uikit.core.factory.CometChatUsersViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel

/**
 * Full-screen for adding members to a group.
 * Matches the UI pattern from master-app-jetpack.
 *
 * @param groupId The group ID to add members to
 * @param onBackPress Callback when back is pressed
 * @param onMembersAdded Callback when members are added successfully
 */
@Composable
fun AddMembersScreen(
    groupId: String,
    onBackPress: () -> Unit,
    onMembersAdded: () -> Unit
) {
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val primaryColor = CometChatTheme.colorScheme.primary
    val textColorWhite = CometChatTheme.colorScheme.colorWhite
    val strokeColorLight = CometChatTheme.colorScheme.strokeColorLight
    val errorColor = CometChatTheme.colorScheme.errorColor

    // State for group
    var group by remember { mutableStateOf<Group?>(null) }
    var isLoadingGroup by remember { mutableStateOf(true) }

    // Load group data
    LaunchedEffect(groupId) {
        isLoadingGroup = true
        CometChat.getGroup(groupId, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(fetchedGroup: Group) {
                group = fetchedGroup
                isLoadingGroup = false
            }

            override fun onError(e: CometChatException) {
                isLoadingGroup = false
                onBackPress()
            }
        })
    }

    // Create a shared ViewModel for CometChatUsers
    val usersViewModel: CometChatUsersViewModel = viewModel(
        factory = CometChatUsersViewModelFactory()
    )

    // Collect selected users from the ViewModel
    val selectedUsersSet by usersViewModel.selectedUsers.collectAsState()
    val selectedUsers = selectedUsersSet.toList()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (isLoadingGroup) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryColor)
        }
        return
    }

    if (group == null) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // CometChatUsers takes most of the screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            CometChatUsers(
                usersViewModel = usersViewModel,
                title = "Add Members",
                hideBackIcon = false,
                selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                onBackPress = onBackPress
            )
        }

        // Bottom container with divider, error text, and add button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = strokeColorLight
            )

            // Error message (if any)
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = CometChatTheme.typography.caption1Regular,
                    color = errorColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                )
            }

            // Add Members button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                onClick = {
                    if (selectedUsers.isNotEmpty() && !isLoading) {
                        isLoading = true
                        errorMessage = null
                        addMembersToGroup(
                            group = group!!,
                            users = selectedUsers,
                            onSuccess = {
                                isLoading = false
                                onMembersAdded()
                            },
                            onError = { exception ->
                                isLoading = false
                                errorMessage = exception.message ?: "Failed to add members"
                            }
                        )
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            color = textColorWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Add Members",
                            style = CometChatTheme.typography.buttonMedium,
                            color = textColorWhite
                        )
                    }
                }
            }
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
        GroupMember(user.uid, CometChatConstants.SCOPE_PARTICIPANT).apply {
            avatar = user.avatar
            name = user.name
            status = user.status
        }
    }.toMutableList()

    CometChat.addMembersToGroup(
        group.guid,
        groupMembers,
        null,
        object : CometChat.CallbackListener<HashMap<String, String>>() {
            override fun onSuccess(result: HashMap<String, String>?) {
                Log.d("AddMembersScreen", "Members added successfully")

                // Calculate added count and emit event
                var addedCount = 0
                result?.forEach { (key, value) ->
                    if (value.startsWith("already_member")) {
                        groupMembers.removeIf { it.uid == key }
                    }
                    if (value == "success") addedCount++
                }
                group.membersCount += addedCount

                // Emit group event for other components to update
                val members: List<User> = ArrayList(groupMembers)
                val actions = members.map { user ->
                    createGroupActionMessage(user, group)
                }

                CometChatUIKit.getLoggedInUser()?.let { loggedInUser ->
                    CometChatEvents.emitGroupEvent(
                        CometChatGroupEvent.MembersAdded(
                            actions = actions,
                            users = members,
                            group = group,
                            addedBy = loggedInUser
                        )
                    )
                }

                onSuccess()
            }

            override fun onError(exception: CometChatException?) {
                Log.e("AddMembersScreen", "Failed to add members: ${exception?.message}")
                exception?.let { onError(it) }
            }
        }
    )
}

/**
 * Creates an Action message for group member added operation.
 */
private fun createGroupActionMessage(user: User, group: Group): Action {
    return Action().apply {
        setActionBy(CometChatUIKit.getLoggedInUser())
        setActionOn(user)
        setActionFor(group)
        receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
        setReceiver(group)
        category = CometChatConstants.CATEGORY_ACTION
        type = CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER
        action = CometChatConstants.ActionKeys.ACTION_MEMBER_ADDED
        receiverUid = group.guid
        sender = CometChatUIKit.getLoggedInUser()
        sentAt = System.currentTimeMillis() / 1000
        conversationId = "group_${group.guid}"
    }
}
