package com.cometchat.sampleapp.compose.ui.newchat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.compose.R
import com.cometchat.uikit.compose.presentation.groups.ui.CometChatGroups
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * New Chat screen composable for starting new conversations.
 *
 * This screen displays a tabbed interface with Users and Groups tabs,
 * allowing users to select a contact or group to start a new conversation.
 *
 * ## Features:
 * - Tabbed interface with Users and Groups tabs (Requirement 7.1)
 * - CometChatUsers component showing non-blocked users (Requirement 7.2)
 * - CometChatGroups component showing only joined groups (Requirement 7.3)
 * - Navigation to MessagesScreen on user selection (Requirement 7.4)
 * - Navigation to MessagesScreen on group selection (Requirement 7.5)
 * - Back button to close the screen (Requirement 7.6)
 *
 * @param onBackPress Callback when back button is pressed
 * @param onUserSelected Callback when a user is selected from the list
 * @param onGroupSelected Callback when a group is selected from the list
 *
 * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6
 */
@Composable
fun NewChatScreen(
    onBackPress: () -> Unit,
    onUserSelected: (User) -> Unit,
    onGroupSelected: (Group) -> Unit
) {
    // Tab state: 0 = Users, 1 = Groups
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Tab titles
    val tabs = listOf(
        stringResource(R.string.tab_users),
        stringResource(R.string.tab_groups)
    )

    // Request builders for filtering
    // Validates: Requirement 7.2 - hide blocked users
    val usersRequestBuilder = remember {
        UsersRequest.UsersRequestBuilder()
            .hideBlockedUsers(true)
            .setLimit(30)
    }

    // Validates: Requirement 7.3 - show only joined groups
    val groupsRequestBuilder = remember {
        GroupsRequest.GroupsRequestBuilder()
            .joinedOnly(true)
            .setLimit(30)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CometChatTheme.colorScheme.backgroundColor1)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Custom Toolbar
        NewChatToolbar(
            onBackPress = onBackPress
        )

        // Separator line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CometChatTheme.colorScheme.strokeColorLight)
        )

        // Tab Layout Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = CometChatTheme.colorScheme.backgroundColor3
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = CometChatTheme.colorScheme.strokeColorLight
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    NewChatTab(
                        title = title,
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Tab Content
        NewChatTabContent(
            selectedTabIndex = selectedTabIndex,
            usersRequestBuilder = usersRequestBuilder,
            groupsRequestBuilder = groupsRequestBuilder,
            onUserSelected = onUserSelected,
            onGroupSelected = onGroupSelected,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
    }
}

/**
 * Custom toolbar matching the sample-app-kotlin design.
 */
@Composable
private fun NewChatToolbar(
    onBackPress: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Icon(
            painter = painterResource(id = com.cometchat.uikit.compose.R.drawable.cometchat_ic_back),
            contentDescription = stringResource(R.string.content_desc_back),
            tint = CometChatTheme.colorScheme.iconTintPrimary,
            modifier = Modifier
                .clickable { onBackPress() }
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Title
        Text(
            text = stringResource(R.string.new_chat),
            style = CometChatTheme.typography.heading2Bold,
            color = CometChatTheme.colorScheme.textColorPrimary
        )
    }
}

/**
 * Custom tab component matching the sample-app-kotlin pill-shaped tab design.
 */
@Composable
private fun NewChatTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        CometChatTheme.colorScheme.backgroundColor1
    } else {
        CometChatTheme.colorScheme.backgroundColor3
    }

    val textColor = if (selected) {
        CometChatTheme.colorScheme.primary
    } else {
        CometChatTheme.colorScheme.textColorSecondary
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = CometChatTheme.typography.heading4Medium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tab content composable that handles the animated visibility of Users and Groups tabs.
 */
@Composable
private fun NewChatTabContent(
    selectedTabIndex: Int,
    usersRequestBuilder: UsersRequest.UsersRequestBuilder,
    groupsRequestBuilder: GroupsRequest.GroupsRequestBuilder,
    onUserSelected: (User) -> Unit,
    onGroupSelected: (Group) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Users Tab - Validates: Requirements 7.2, 7.4
        AnimatedVisibility(
            visible = selectedTabIndex == 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CometChatUsers(
                modifier = Modifier.fillMaxSize(),
                // Hide toolbar since we have our own
                hideToolbar = true,
                // Hide separator since we have our own
                hideSeparator = true,
                // Show search box for filtering users
                hideSearchBox = false,
                // Show status indicator for online/offline
                hideStatusIndicator = false,
                // Configure request builder to hide blocked users
                usersRequestBuilder = usersRequestBuilder,
                // Handle user selection - Validates: Requirement 7.4
                onItemClick = { user ->
                    onUserSelected(user)
                },
                onError = { exception ->
                    // Error handling is done internally by the component
                }
            )
        }

        // Groups Tab - Validates: Requirements 7.3, 7.5
        AnimatedVisibility(
            visible = selectedTabIndex == 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CometChatGroups(
                modifier = Modifier.fillMaxSize(),
                // Hide toolbar since we have our own
                hideToolbar = true,
                // Hide separator since we have our own
                hideSeparator = true,
                // Show search box for filtering groups
                hideSearchBox = false,
                // Show group type indicators
                hideGroupType = false,
                // Configure request builder to show only joined groups
                groupsRequestBuilder = groupsRequestBuilder,
                // Handle group selection - Validates: Requirement 7.5
                onItemClick = { group ->
                    onGroupSelected(group)
                },
                onError = { exception ->
                    // Error handling is done internally by the component
                }
            )
        }
    }
}
