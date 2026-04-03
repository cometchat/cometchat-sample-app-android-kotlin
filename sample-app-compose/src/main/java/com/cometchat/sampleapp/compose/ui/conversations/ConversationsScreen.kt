package com.cometchat.sampleapp.compose.ui.conversations

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.sampleapp.compose.R
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversations
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit

/**
 * Conversations screen composable for the Chats tab.
 *
 * This screen displays the list of conversations using the
 * CometChatConversationList component from the UIKit.
 *
 * ## Features:
 * - Displays all active conversations
 * - Shows last message preview
 * - Shows unread message count badges
 * - Shows user/group avatars and names
 * - Supports pull-to-refresh
 * - Handles conversation tap to navigate to messages
 * - Overflow menu with user avatar for logout and create conversation
 *
 * ## Usage:
 * ```kotlin
 * ConversationsScreen(
 *     onConversationClick = { conversation ->
 *         // Navigate to messages screen
 *     },
 *     onLogout = { /* Handle logout */ },
 *     onNewChatClick = { /* Navigate to new chat */ }
 * )
 * ```
 *
 * @param onConversationClick Callback when a conversation is tapped
 * @param onLogout Callback when logout is requested
 * @param onNewChatClick Callback when new chat is requested
 *
 * Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 8.4, 8.5
 */
@Composable
fun ConversationsScreen(
    onConversationClick: (Conversation) -> Unit,
    onLogout: () -> Unit = {},
    onNewChatClick: () -> Unit = {}
) {
    // Get logged in user for avatar
    val loggedInUser = remember { CometChatUIKit.getLoggedInUser() }
    
    // Overflow menu with user avatar (matching sample-app-kotlin)
    val overflowMenu: @Composable () -> Unit = {
        if (loggedInUser != null) {
            UserAvatarMenu(
                user = loggedInUser,
                onLogout = onLogout,
                onNewChatClick = onNewChatClick
            )
        }
    }

    CometChatConversations(
        modifier = Modifier.fillMaxSize(),
        // Show toolbar with title (matching sample-app-kotlin)
        hideToolbar = false,
        // Set overflow menu with user avatar
        overflowMenu = overflowMenu,
        // Callbacks
        onItemClick = { conversation ->
            onConversationClick(conversation)
        },
        onError = { exception ->
            // Error handling is done internally by the component
            // Additional error handling can be added here if needed
        }
    )
}

/**
 * User avatar menu composable that shows a dropdown with logout and create conversation options.
 * Matches the popup_user_menu.xml layout from sample-app-kotlin.
 *
 * @param user The logged-in user
 * @param onLogout Callback when logout is clicked
 * @param onNewChatClick Callback when create conversation is clicked
 *
 * Validates: Requirements 8.4, 8.5
 */
@Composable
private fun UserAvatarMenu(
    user: User,
    onLogout: () -> Unit,
    onNewChatClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // User avatar button
        CometChatAvatar(
            name = user.name,
            avatarUrl = user.avatar,
            modifier = Modifier
                .size(40.dp)
                .clickable { expanded = true }
        )

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = (-160).dp, y = 0.dp),
            modifier = Modifier
                .width(200.dp)
                .background(
                    color = CometChatTheme.colorScheme.backgroundColor1,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Surface(
                color = CometChatTheme.colorScheme.backgroundColor1,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Create Conversation option (first item - matching sample-app-kotlin order)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = false
                                onNewChatClick()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_start_conversation),
                            contentDescription = null,
                            tint = CometChatTheme.colorScheme.iconTintSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_create_conversation),
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                    }

                    HorizontalDivider(
                        color = CometChatTheme.colorScheme.strokeColorLight,
                        thickness = 1.dp
                    )

                    // User name (second item - matching sample-app-kotlin order)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user_profile),
                            contentDescription = null,
                            tint = CometChatTheme.colorScheme.iconTintSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = user.name,
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                    }

                    HorizontalDivider(
                        color = CometChatTheme.colorScheme.strokeColorLight,
                        thickness = 1.dp
                    )

                    // Logout option (third item - matching sample-app-kotlin order)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = false
                                // Handle logout - call CometChat.logout and navigate on success
                                // Matches master-app-jetpack behavior
                                CometChat.logout(object : CometChat.CallbackListener<String>() {
                                    override fun onSuccess(result: String?) {
                                        Log.d("ConversationsScreen", "Logout successful")
                                        // Navigate to Login screen and clear back stack
                                        onLogout()
                                    }

                                    override fun onError(exception: CometChatException?) {
                                        // Log error and remain on current screen
                                        Log.e("ConversationsScreen", "Logout failed: ${exception?.message}")
                                    }
                                })
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = null,
                            tint = CometChatTheme.colorScheme.errorColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_logout),
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.errorColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extension function to get the User from a Conversation.
 *
 * @return The User if this is a user conversation, null otherwise
 */
fun Conversation.getUser(): User? {
    return if (conversationType == CometChatConstants.CONVERSATION_TYPE_USER) {
        conversationWith as? User
    } else {
        null
    }
}

/**
 * Extension function to get the Group from a Conversation.
 *
 * @return The Group if this is a group conversation, null otherwise
 */
fun Conversation.getGroup(): Group? {
    return if (conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP) {
        conversationWith as? Group
    } else {
        null
    }
}
