package com.cometchat.sampleapp.compose.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R as UiKitR
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialog
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * User details screen composable for displaying user profile information.
 *
 * This screen displays detailed information about a user:
 * - Custom toolbar with back icon and title (matching master-app-jetpack)
 * - Info message banner for blocked user warning
 * - User avatar (120dp), name, and online/offline status
 * - Voice and Video call cards (matching master-app-jetpack)
 * - Block/unblock user functionality
 * - Delete chat option (when lastMessage exists)
 *
 * ## Features:
 * - Display user profile information
 * - Show online/offline presence status
 * - Voice and Video call cards with proper icons
 * - Block/unblock user with CometChatDialog
 * - Delete chat option
 * - Real-time user status updates
 *
 * Matches master-app-jetpack UI parity.
 * Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.6, 10.7
 */
@Composable
fun UserDetailsScreen(
    userId: String,
    lastMessage: BaseMessage? = null,
    onBackPress: () -> Unit,
    onChatDeleted: () -> Unit = {},
    onMessageClick: (User) -> Unit
) {
    val context = LocalContext.current
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    // State for user
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isBlocked by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var showDeleteChatDialog by remember { mutableStateOf(false) }

    // User listener ID
    val userListenerId = remember { "UserDetailsScreen_UserListener_$userId" }

    // Load user data
    LaunchedEffect(userId) {
        isLoading = true
        CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(fetchedUser: User) {
                user = fetchedUser
                isBlocked = fetchedUser.isBlockedByMe
                isLoading = false
            }

            override fun onError(e: CometChatException) {
                isLoading = false
            }
        })
    }

    // Add user listener for real-time status updates
    DisposableEffect(userId) {
        CometChat.addUserListener(userListenerId, object : CometChat.UserListener() {
            override fun onUserOnline(onlineUser: User) {
                if (onlineUser.uid == userId) {
                    user = onlineUser
                }
            }

            override fun onUserOffline(offlineUser: User) {
                if (offlineUser.uid == userId) {
                    user = offlineUser
                }
            }
        })

        onDispose {
            CometChat.removeUserListener(userListenerId)
        }
    }

    // Block confirmation dialog
    if (showBlockDialog) {
        CometChatDialog(
            title = "Block User",
            message = "Are you sure you want to block ${user?.name}?",
            positiveButtonText = "Block",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                showBlockDialog = false
                user?.let { u ->
                    CometChat.blockUsers(
                        listOf(u.uid),
                        object : CometChat.CallbackListener<HashMap<String, String>>() {
                            override fun onSuccess(result: HashMap<String, String>) {
                                isBlocked = true
                                u.isBlockedByMe = true
                                user = u
                            }

                            override fun onError(e: CometChatException) {}
                        }
                    )
                }
            },
            onNegativeClick = { showBlockDialog = false },
            onDismiss = { showBlockDialog = false }
        )
    }

    // Unblock confirmation dialog
    if (showUnblockDialog) {
        CometChatDialog(
            title = "Unblock User",
            message = "Are you sure you want to unblock ${user?.name}?",
            positiveButtonText = "Unblock",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                showUnblockDialog = false
                user?.let { u ->
                    CometChat.unblockUsers(
                        listOf(u.uid),
                        object : CometChat.CallbackListener<HashMap<String, String>>() {
                            override fun onSuccess(result: HashMap<String, String>) {
                                isBlocked = false
                                u.isBlockedByMe = false
                                user = u
                            }

                            override fun onError(e: CometChatException) {}
                        }
                    )
                }
            },
            onNegativeClick = { showUnblockDialog = false },
            onDismiss = { showUnblockDialog = false }
        )
    }

    // Delete chat confirmation dialog
    if (showDeleteChatDialog) {
        CometChatDialog(
            title = "Delete Chat",
            message = "Are you sure you want to delete this chat? This action cannot be undone.",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                showDeleteChatDialog = false
                user?.let { u ->
                    CometChat.deleteConversation(
                        u.uid,
                        CometChatConstants.CONVERSATION_TYPE_USER,
                        object : CometChat.CallbackListener<String>() {
                            override fun onSuccess(result: String) {
                                onChatDeleted()
                            }

                            override fun onError(e: CometChatException) {}
                        }
                    )
                }
            },
            onNegativeClick = { showDeleteChatDialog = false },
            onDismiss = { showDeleteChatDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor1)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Custom Toolbar (matching master-app-jetpack)
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.iconTintPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "User Info",
                    style = typography.heading2Bold,
                    color = colorScheme.textColorPrimary
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = colorScheme.strokeColorLight
            )
        }

        if (!isLoading && user != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Info message banner (for blocked user warning)
                if (isBlocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorScheme.warningColor)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = UiKitR.drawable.cometchat_ic_info),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(24.dp),
                            tint = colorScheme.textColorPrimary
                        )
                        Text(
                            text = "You have blocked this user",
                            style = typography.bodyRegular,
                            color = colorScheme.textColorPrimary
                        )
                    }
                }

                // User Info Section - 40dp top margin
                Spacer(modifier = Modifier.height(40.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // User Avatar (120dp matching master-app-jetpack)
                    CometChatAvatar(
                        modifier = Modifier.size(120.dp),
                        name = user!!.name,
                        avatarUrl = user!!.avatar,
                        style = AvatarStyle.default(cornerRadius = 60.dp)
                    )

                    // User Name - 12dp top margin
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user!!.name,
                        style = typography.heading2Medium,
                        color = colorScheme.textColorPrimary,
                        textAlign = TextAlign.Center
                    )

                    // User Status (Online/Offline)
                    if (!isBlocked) {
                        Text(
                            text = if (user!!.status == CometChatConstants.USER_STATUS_ONLINE) "Online" else "Offline",
                            style = typography.caption1Regular,
                            color = colorScheme.textColorSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Voice/Video Call Cards (matching master-app-jetpack) - 20dp top margin
                    if (!isBlocked) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Voice Call Card
                            CallActionCard(
                                modifier = Modifier.weight(1f),
                                icon = UiKitR.drawable.cometchat_ic_call_voice,
                                label = "Voice",
                                strokeColor = colorScheme.strokeColorDefault,
                                iconTint = colorScheme.iconTintHighlight,
                                onClick = {
                                    user?.let { u ->
                                        val call = Call(u.uid, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_AUDIO)
                                        CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                                            override fun onSuccess(initiatedCall: Call) {
                                                com.cometchat.uikit.compose.calls.CometChatCallActivity.launchOutgoingCallScreen(context, initiatedCall, null)
                                            }
                                            override fun onError(e: CometChatException) {}
                                        })
                                    }
                                }
                            )
                            // Video Call Card
                            CallActionCard(
                                modifier = Modifier.weight(1f),
                                icon = UiKitR.drawable.cometchat_ic_call_video,
                                label = "Video",
                                strokeColor = colorScheme.strokeColorDefault,
                                iconTint = colorScheme.iconTintHighlight,
                                onClick = {
                                    user?.let { u ->
                                        val call = Call(u.uid, CometChatConstants.RECEIVER_TYPE_USER, CometChatConstants.CALL_TYPE_VIDEO)
                                        CometChat.initiateCall(call, object : CometChat.CallbackListener<Call>() {
                                            override fun onSuccess(initiatedCall: Call) {
                                                com.cometchat.uikit.compose.calls.CometChatCallActivity.launchOutgoingCallScreen(context, initiatedCall, null)
                                            }
                                            override fun onError(e: CometChatException) {}
                                        })
                                    }
                                }
                            )
                        }
                    }
                }

                // Separator - 20dp top margin
                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = colorScheme.strokeColorLight
                )

                // Block/Unblock action
                ActionButtonWithIcon(
                    text = if (isBlocked) "Unblock" else "Block",
                    icon = UiKitR.drawable.cometchat_ic_block,
                    iconTint = colorScheme.errorColor,
                    textColor = colorScheme.errorColor,
                    onClick = {
                        if (isBlocked) showUnblockDialog = true else showBlockDialog = true
                    }
                )

                // Delete Chat action (if lastMessage exists)
                if (lastMessage != null) {
                    ActionButtonWithIcon(
                        text = "Delete Chat",
                        icon = UiKitR.drawable.cometchat_ic_delete,
                        iconTint = colorScheme.errorColor,
                        textColor = colorScheme.errorColor,
                        onClick = { showDeleteChatDialog = true }
                    )
                }
            }
        }
    }
}

/**
 * Call action card matching MaterialCardView with stroke border.
 * Height: 73dp, cornerRadius: 8dp, strokeWidth: 1dp
 */
@Composable
private fun CallActionCard(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    strokeColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(73.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CometChatTheme.colorScheme.backgroundColor1
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = CometChatTheme.typography.buttonRegular,
                color = CometChatTheme.colorScheme.textColorSecondary
            )
        }
    }
}

/**
 * Action button with icon matching XML layout.
 * Uses Heading4Regular text style with 12dp drawable padding.
 * Padding: 20dp horizontal, 12dp vertical
 */
@Composable
private fun ActionButtonWithIcon(
    text: String,
    icon: Int,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp)) // drawablePadding
        Text(
            text = text,
            style = CometChatTheme.typography.heading4Regular,
            color = textColor
        )
    }
}
