package com.cometchat.sampleapp.compose.push.appflow.tabs

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.cometchat.chat.core.CometChat
import com.cometchat.pushnotification.CometChatPushNotifications
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversations
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.shared.views.popupmenu.CometChatPopupMenuStyle
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.R

/**
 * Chats tab displaying the conversation list.
 * Includes overflow menu with user avatar for logout and new chat options.
 *
 * @param onConversationClick Callback when a conversation is clicked (user, group, lastMessageId)
 * @param onNewChatClick Callback when new chat is clicked
 * @param onSearchClick Callback when search is clicked
 * @param onLogout Callback when logout is successful, used to navigate to Login screen
 * @param contentPadding Padding from the scaffold
 */
@Composable
fun ChatsTab(
    onConversationClick: (User?, Group?, Long?) -> Unit,
    onNewChatClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLogout: () -> Unit,
    contentPadding: PaddingValues
) {
    val loggedInUser = CometChat.getLoggedInUser()
    
    var showOverflowMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        CometChatConversations(
            modifier = Modifier.fillMaxSize(),
            title = "Chats",
            hideBackIcon = true,
            overflowMenu = {
                // User avatar as overflow menu trigger
                UserAvatarOverflowMenu(
                    user = loggedInUser,
                    showMenu = showOverflowMenu,
                    onMenuToggle = { showOverflowMenu = it },
                    onNewChatClick = {
                        showOverflowMenu = false
                        onNewChatClick()
                    },
                    onLogoutClick = {
                        showOverflowMenu = false
                        // Always unregister the FCM token BEFORE logging out (prevents ghost pushes).
                        val doLogout = {
                            CometChat.logout(object : CometChat.CallbackListener<String>() {
                                override fun onSuccess(p0: String?) {
                                    Log.d("ChatsTab", "Logout successful")
                                    // Navigate to Login screen and clear back stack
                                    onLogout()
                                }
                                override fun onError(p0: com.cometchat.chat.exceptions.CometChatException?) {
                                    // Log error and remain on current screen
                                    Log.e("ChatsTab", "Logout failed: ${p0?.message}")
                                }
                            })
                        }
                        CometChatPushNotifications.unregisterToken(
                            onSuccess = { Log.d("ChatsTab", "FCM token unregistered"); doLogout() },
                            onError = { e ->
                                Log.e("ChatsTab", "Token unregister failed (proceeding): ${e.message}")
                                doLogout()
                            }
                        )
                    }
                )
            },
            onSearchClick = {
                onSearchClick()
            },
            onItemClick = { conversation ->
                val conversationWith = conversation.conversationWith
                val lastMessageId = conversation.lastMessage?.id?.toLong()
                when (conversationWith) {
                    is User -> onConversationClick(conversationWith, null, lastMessageId)
                    is Group -> onConversationClick(null, conversationWith, lastMessageId)
                }
            },
            onError = { exception ->
                Log.e("ChatsTab", "Error: ${exception.message}")
            }
        )
    }
}


/**
 * User avatar that acts as an overflow menu trigger.
 * Styled to match the reference app (master-app-kotlin) with icons and proper colors.
 */
@Composable
private fun UserAvatarOverflowMenu(
    user: User?,
    showMenu: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    onNewChatClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // Create menu items with icons matching the reference app
    val menuItems = listOf(
        // Create Conversation - with icon, primary text color
        MenuItem(
            id = "new_chat",
            name = "Create conversation",
            startIcon = painterResource(id = R.drawable.ic_start_conversation),
            startIconTint = CometChatTheme.colorScheme.iconTintSecondary,
            textColor = CometChatTheme.colorScheme.textColorPrimary,
            onClick = onNewChatClick
        ),
        // User Name - with icon, primary text color
        MenuItem(
            id = "user_name",
            name = user?.name ?: "User",
            startIcon = painterResource(id = R.drawable.ic_user_profile),
            startIconTint = CometChatTheme.colorScheme.iconTintSecondary,
            textColor = CometChatTheme.colorScheme.textColorPrimary,
            onClick = { /* No action */ }
        ),
        // Logout - with icon, error color for both text and icon
        MenuItem(
            id = "logout",
            name = "Logout",
            startIcon = painterResource(id = R.drawable.ic_logout),
            startIconTint = CometChatTheme.colorScheme.errorColor,
            textColor = CometChatTheme.colorScheme.errorColor,
            onClick = onLogoutClick
        )
    )
    
    // Custom style matching the reference app
    val popupMenuStyle = CometChatPopupMenuStyle.default(
        backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
        strokeColor = CometChatTheme.colorScheme.strokeColorDefault,
        strokeWidth = 1.dp,
        cornerRadius = 8.dp,
        elevation = 5.dp,
        itemTextColor = CometChatTheme.colorScheme.textColorPrimary,
        itemTextStyle = CometChatTheme.typography.bodyRegular,
        minWidth = 200.dp
    )
    
    Box {
        CometChatAvatar(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onMenuToggle(!showMenu) },
            name = user?.name ?: "U",
            avatarUrl = user?.avatar,
            style = AvatarStyle.default(
                cornerRadius = 20.dp
            )
        )
        
        CometChatPopupMenu(
            expanded = showMenu,
            onDismissRequest = { onMenuToggle(false) },
            menuItems = menuItems,
            style = popupMenuStyle,
            offset = DpOffset((-160).dp, 8.dp),
            onMenuItemClick = { _, _ ->
                onMenuToggle(false)
            }
        ) {
            // Empty anchor - menu is positioned relative to parent Box
        }
    }
}
