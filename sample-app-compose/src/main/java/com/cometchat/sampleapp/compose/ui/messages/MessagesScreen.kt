package com.cometchat.sampleapp.compose.ui.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageList
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Messages screen composable for one-on-one and group messaging.
 *
 * This screen displays the complete chat interface using CometChat UI Kit components:
 * - CometChatMessageHeader: Shows user/group info, typing indicators, and navigation
 * - CometChatMessageList: Displays message history with real-time updates
 * - CometChatMessageComposer: Allows composing and sending messages
 *
 * ## Features:
 * - Real-time message sending and receiving
 * - Support for text, images, videos, audio, and files
 * - Message delivery and read receipts
 * - Message reactions
 * - Thread replies
 * - Typing indicators
 * - Pull-to-refresh for message history
 *
 * ## Usage:
 * ```kotlin
 * MessagesScreen(
 *     userId = "user123",
 *     groupId = null,
 *     onBackPress = { navController.popBackStack() },
 *     onUserDetailsClick = { user -> /* Navigate to user details */ },
 *     onGroupDetailsClick = { group -> /* Navigate to group details */ },
 *     onThreadClick = { message -> /* Navigate to thread */ }
 * )
 * ```
 *
 * @param userId The UID of the user for one-on-one chat (null for group chat)
 * @param groupId The GUID of the group for group chat (null for one-on-one chat)
 * @param onBackPress Callback when back button is pressed
 * @param onUserDetailsClick Callback when user header is tapped (for user details navigation)
 * @param onGroupDetailsClick Callback when group header is tapped (for group details navigation)
 * @param onThreadClick Callback when thread indicator is tapped
 *
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 7.1, 7.2, 7.3
 */
@Composable
fun MessagesScreen(
    userId: String? = null,
    groupId: String? = null,
    onBackPress: () -> Unit,
    onUserDetailsClick: ((User) -> Unit)? = null,
    onGroupDetailsClick: ((Group) -> Unit)? = null,
    onThreadClick: ((BaseMessage) -> Unit)? = null
) {
    // State for user/group
    var user by remember { mutableStateOf<User?>(null) }
    var group by remember { mutableStateOf<Group?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch user or group based on provided IDs
    LaunchedEffect(userId, groupId) {
        isLoading = true
        error = null

        when {
            userId != null -> {
                CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
                    override fun onSuccess(fetchedUser: User) {
                        user = fetchedUser
                        group = null
                        isLoading = false
                    }

                    override fun onError(e: CometChatException) {
                        error = e.message
                        isLoading = false
                    }
                })
            }
            groupId != null -> {
                CometChat.getGroup(groupId, object : CometChat.CallbackListener<Group>() {
                    override fun onSuccess(fetchedGroup: Group) {
                        group = fetchedGroup
                        user = null
                        isLoading = false
                    }

                    override fun onError(e: CometChatException) {
                        error = e.message
                        isLoading = false
                    }
                })
            }
            else -> {
                error = "No user or group ID provided"
                isLoading = false
            }
        }
    }

    // Show content when user/group is loaded
    if (!isLoading && (user != null || group != null)) {
        MessagesContent(
            user = user,
            group = group,
            onBackPress = onBackPress,
            onUserDetailsClick = onUserDetailsClick,
            onGroupDetailsClick = onGroupDetailsClick,
            onThreadClick = onThreadClick
        )
    }
}

/**
 * Internal composable that displays the messages content.
 *
 * This is separated from MessagesScreen to ensure the UI Kit components
 * receive non-null user/group values.
 */
@Composable
private fun MessagesContent(
    user: User?,
    group: Group?,
    onBackPress: () -> Unit,
    onUserDetailsClick: ((User) -> Unit)?,
    onGroupDetailsClick: ((Group) -> Unit)?,
    onThreadClick: ((BaseMessage) -> Unit)?
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    
    Scaffold(
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Message Header - shows user/group info, typing indicators
            // Validates: Requirements 6.3, 7.3, 10.8
            // Note: Details navigation is now via menu instead of header tap
            CometChatMessageHeader(
                modifier = Modifier.fillMaxWidth(),
                user = user,
                group = group,
                // Hide call buttons - no VoIP in sample apps
                hideVideoCallButton = true,
                hideVoiceCallButton = true,
                // Show back button for navigation
                hideBackButton = false,
                // Hide built-in menu icon - we use custom trailingView
                hideMenuIcon = true,
                onBackPress = onBackPress,
                // Custom trailing view with overflow menu
                trailingView = { _, _ ->
                    Box {
                        Icon(
                            painter = painterResource(id = R.drawable.cometchat_ic_menu_dots),
                            contentDescription = "Menu",
                            tint = CometChatTheme.colorScheme.iconTintPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { showOverflowMenu = true }
                        )
                        
                        MessagesOverflowMenu(
                            showMenu = showOverflowMenu,
                            onDismiss = { showOverflowMenu = false },
                            onDetailsClick = {
                                showOverflowMenu = false
                                user?.let { onUserDetailsClick?.invoke(it) }
                                group?.let { onGroupDetailsClick?.invoke(it) }
                            }
                        )
                    }
                }
            )

            // Message List - displays message history
            // Validates: Requirements 6.1, 6.4, 6.5, 6.7, 6.8, 7.1, 7.2
            CometChatMessageList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                user = user,
                group = group,
                // Enable real-time updates
                scrollToBottomOnNewMessage = true,
                // Enable swipe to reply
                swipeToReplyEnabled = true,
                // Show receipts for delivery/read status (Requirement 6.7)
                hideReceipts = false,
                // Show reactions (Requirement 6.8)
                hideMessageReactionOption = false,
                // Show thread replies option (Requirement 6.9)
                hideReplyInThreadOption = false,
                // Callbacks
                onThreadRepliesClick = { message ->
                    onThreadClick?.invoke(message)
                },
                onError = { exception ->
                    // Error handling is done internally by the component
                }
            )

            // Message Composer - for composing and sending messages
            // Validates: Requirements 6.2, 6.6
            CometChatMessageComposer(
                modifier = Modifier.fillMaxWidth(),
                user = user,
                group = group,
                // Enable all attachment types (Requirement 6.6)
                hideAttachmentButton = false,
                hideVoiceRecordingButton = false,
                hideSendButton = false,
                // Enable stickers
                hideStickersButton = false,
                onError = { exception ->
                    // Error handling is done internally by the component
                }
            )
        }
    }
}

/**
 * Overflow menu for messages screen.
 * 
 * @param showMenu Whether the menu is visible
 * @param onDismiss Callback when menu is dismissed
 * @param onDetailsClick Callback when Details option is clicked
 * 
 * Validates: Requirements 7.3, 10.8
 */
@Composable
private fun MessagesOverflowMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val menuItems = listOf(
        MenuItem(
            id = "details",
            name = "Details",
            startIcon = painterResource(id = R.drawable.cometchat_ic_info),
            onClick = onDetailsClick
        )
    )
    
    CometChatPopupMenu(
        expanded = showMenu,
        onDismissRequest = onDismiss,
        menuItems = menuItems,
        offset = DpOffset(0.dp, 8.dp),
        onMenuItemClick = { _, _ ->
            onDismiss()
        }
    ) {
        // Empty anchor - the menu icon is rendered in trailingView
    }
}
