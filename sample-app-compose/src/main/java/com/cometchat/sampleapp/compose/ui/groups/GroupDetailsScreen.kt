package com.cometchat.sampleapp.compose.ui.groups

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.sampleapp.compose.R
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialog
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Group details screen composable for displaying group information.
 *
 * This screen displays detailed information about a group:
 * - Custom toolbar with back icon and title (matching master-app-jetpack)
 * - Info message banner for "no longer part of this group"
 * - Group avatar (120dp), name, and member count
 * - Three action cards: View Members, Add Members, Banned Members
 * - Leave group option (for non-owners)
 * - Delete & Exit option (for owners)
 * - Delete chat option (when lastMessage exists)
 *
 * Matches master-app-jetpack UI parity.
 * Validates: Requirements 7.4, 7.5, 7.6, 11.1, 11.2, 11.3, 11.4, 11.6, 11.7, 11.8, 11.9, 11.10
 */
@Composable
fun GroupDetailsScreen(
    groupId: String,
    lastMessage: BaseMessage? = null,
    onBackPress: () -> Unit,
    onLeaveGroup: () -> Unit,
    onGroupDeleted: () -> Unit = {},
    onChatDeleted: () -> Unit = {},
    onMemberClick: ((GroupMember) -> Unit)? = null,
    onNavigateToAddMembers: (String) -> Unit = {},
    onNavigateToMembers: (String) -> Unit = {},
    onNavigateToBannedMembers: (String) -> Unit = {}
) {
    val colorScheme = CometChatTheme.colorScheme
    val typography = CometChatTheme.typography

    // State for group
    var group by remember { mutableStateOf<Group?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showDeleteChatDialog by remember { mutableStateOf(false) }
    var showDeleteGroupDialog by remember { mutableStateOf(false) }
    var showTransferOwnershipDialog by remember { mutableStateOf(false) }
    var isLeaving by remember { mutableStateOf(false) }
    var isNotMember by remember { mutableStateOf(false) }

    // User role state
    var isOwner by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var isModerator by remember { mutableStateOf(false) }
    var isMember by remember { mutableStateOf(false) }

    // Load group data
    LaunchedEffect(groupId) {
        isLoading = true
        CometChat.getGroup(groupId, object : CometChat.CallbackListener<Group>() {
            override fun onSuccess(fetchedGroup: Group) {
                group = fetchedGroup
                val loggedInUser = CometChat.getLoggedInUser()
                isOwner = fetchedGroup.owner == loggedInUser?.uid
                
                // Check scope
                when (fetchedGroup.scope) {
                    UIKitConstants.GroupMemberScope.ADMIN -> {
                        isAdmin = true
                        isModerator = false
                    }
                    UIKitConstants.GroupMemberScope.MODERATOR -> {
                        isAdmin = false
                        isModerator = true
                    }
                    else -> {
                        isAdmin = false
                        isModerator = false
                    }
                }
                
                // Owner is always admin
                if (isOwner) isAdmin = true
                
                isMember = fetchedGroup.isJoined
                isNotMember = !fetchedGroup.isJoined
                isLoading = false
            }

            override fun onError(e: CometChatException) {
                isLoading = false
            }
        })
    }

    // Leave group confirmation dialog
    if (showLeaveDialog) {
        CometChatDialog(
            title = "Leave Group",
            message = "Are you sure you want to leave this group?",
            positiveButtonText = "Leave",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                showLeaveDialog = false
                isLeaving = true
                group?.let { g ->
                    CometChat.leaveGroup(g.guid, object : CometChat.CallbackListener<String>() {
                        override fun onSuccess(result: String) {
                            isLeaving = false
                            onLeaveGroup()
                        }

                        override fun onError(e: CometChatException) {
                            isLeaving = false
                        }
                    })
                }
            },
            onNegativeClick = { showLeaveDialog = false },
            onDismiss = { showLeaveDialog = false }
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
                group?.let { g ->
                    CometChat.deleteConversation(
                        g.guid,
                        CometChatConstants.CONVERSATION_TYPE_GROUP,
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

    // Delete group confirmation dialog
    if (showDeleteGroupDialog) {
        CometChatDialog(
            title = "Delete Group",
            message = "Are you sure you want to delete this group? This action cannot be undone.",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                showDeleteGroupDialog = false
                group?.let { g ->
                    CometChat.deleteGroup(g.guid, object : CometChat.CallbackListener<String>() {
                        override fun onSuccess(result: String) {
                            onGroupDeleted()
                        }

                        override fun onError(e: CometChatException) {}
                    })
                }
            },
            onNegativeClick = { showDeleteGroupDialog = false },
            onDismiss = { showDeleteGroupDialog = false }
        )
    }

    // Transfer ownership dialog (shown when owner tries to delete group with multiple members)
    if (showTransferOwnershipDialog && group != null) {
        TransferOwnershipDialog(
            group = group!!,
            onDismiss = { showTransferOwnershipDialog = false },
            onOwnershipTransferred = {
                showTransferOwnershipDialog = false
                // After transferring ownership, show leave dialog
                showLeaveDialog = true
            }
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
                    text = "Group Info",
                    style = typography.heading2Bold,
                    color = colorScheme.textColorPrimary
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = colorScheme.strokeColorLight
            )
        }

        if (!isLoading && group != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Info message banner (for "no longer part of this group")
                if (isNotMember) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorScheme.warningColor)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(24.dp),
                            tint = colorScheme.textColorPrimary
                        )
                        Text(
                            text = "You are no longer part of this group",
                            style = typography.bodyRegular,
                            color = colorScheme.textColorPrimary
                        )
                    }
                }

                // Group Info Section - 40dp top margin
                Spacer(modifier = Modifier.height(40.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Group Avatar (120dp matching master-app-jetpack)
                    CometChatAvatar(
                        modifier = Modifier.size(120.dp),
                        name = group!!.name,
                        avatarUrl = group!!.icon,
                        style = AvatarStyle.default(cornerRadius = 60.dp)
                    )

                    // Group Name - 12dp top margin
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = group!!.name,
                        style = typography.heading2Medium,
                        color = colorScheme.textColorPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 3
                    )

                    // Member Count - 4dp top margin
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${group!!.membersCount} members",
                        style = typography.caption1Regular,
                        color = colorScheme.textColorSecondary,
                        textAlign = TextAlign.Center
                    )

                    // Three Action Cards (matching master-app-jetpack) - 12dp top margin
                    if (isMember) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // View Members Card
                            GroupActionCard(
                                modifier = Modifier.weight(1f),
                                icon = R.drawable.ic_group,
                                label = "View\nMembers",
                                strokeColor = colorScheme.strokeColorLight,
                                onClick = { onNavigateToMembers(groupId) }
                            )
                            
                            // Add Members Card (admin/owner only)
                            if (isAdmin || isOwner) {
                                GroupActionCard(
                                    modifier = Modifier.weight(1f),
                                    icon = R.drawable.ic_create_group,
                                    label = "Add\nMembers",
                                    strokeColor = colorScheme.strokeColorLight,
                                    onClick = { onNavigateToAddMembers(groupId) }
                                )
                            }
                            
                            // Banned Members Card (admin/moderator/owner only)
                            if (isAdmin || isOwner || isModerator) {
                                GroupActionCard(
                                    modifier = Modifier.weight(1f),
                                    icon = R.drawable.ic_banned_members,
                                    label = "Banned\nMembers",
                                    strokeColor = colorScheme.strokeColorLight,
                                    onClick = { onNavigateToBannedMembers(groupId) }
                                )
                            }
                        }
                    }
                }

                // Separator - 20dp top margin
                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = colorScheme.strokeColorLight
                )

                // Leave Group action (if member and not owner)
                if (isMember && !isOwner) {
                    ActionButtonWithIcon(
                        text = "Leave",
                        icon = R.drawable.ic_leave_group,
                        onClick = { showLeaveDialog = true }
                    )
                }

                // Delete Chat action (if lastMessage exists)
                if (lastMessage != null) {
                    ActionButtonWithIcon(
                        text = "Delete Chat",
                        icon = R.drawable.ic_delete,
                        onClick = { showDeleteChatDialog = true }
                    )
                }

                // Delete & Exit action (owner only)
                if (isOwner) {
                    ActionButtonWithIcon(
                        text = "Delete & Exit",
                        icon = R.drawable.ic_delete,
                        onClick = {
                            // If group has more than 1 member, show transfer ownership dialog first
                            val membersCount = group?.membersCount ?: 0
                            if (membersCount > 1) {
                                showTransferOwnershipDialog = true
                            } else {
                                showDeleteGroupDialog = true
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Group action card matching MaterialCardView with stroke border.
 * cornerRadius: 8dp, strokeWidth: 1dp, elevation: 0dp
 * Icon on top, text below, centered.
 * Note: Icons have their colors baked in, so we use Color.Unspecified for tint.
 */
@Composable
private fun GroupActionCard(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    strokeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
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
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = CometChatTheme.typography.caption1Regular,
                color = CometChatTheme.colorScheme.textColorSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Action button with icon matching XML layout.
 * Uses Heading4Regular text style with 12dp margin between icon and text.
 * Padding: 16dp all sides
 * Note: Icons have their colors baked in, so we use Color.Unspecified for tint.
 */
@Composable
private fun ActionButtonWithIcon(
    text: String,
    icon: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = CometChatTheme.typography.heading4Regular,
            color = CometChatTheme.colorScheme.errorColor
        )
    }
}
