package com.cometchat.sampleapp.compose.push.appflow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialog
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.R as AppR
import com.cometchat.sampleapp.compose.push.appflow.components.AppFlowToolbar
import com.cometchat.sampleapp.compose.push.appflow.dialogs.BannedMembersDialog
import com.cometchat.sampleapp.compose.push.appflow.dialogs.GroupMembersDialog
import com.cometchat.sampleapp.compose.push.appflow.dialogs.TransferOwnershipDialog
import com.cometchat.sampleapp.compose.push.appflow.viewmodels.GroupDetailsViewModel

/**
 * Group details screen showing group info and management options.
 * Styled to match master-app-kotlin2 activity_group_details.xml.
 *
 * @param group The group to display details for
 * @param lastMessage The last message in the conversation
 * @param onBackPress Callback when back is pressed
 * @param onGroupLeft Callback when user leaves the group
 * @param onGroupDeleted Callback when group is deleted
 * @param onChatDeleted Callback when chat is deleted
 * @param onNavigateToAddMembers Callback to navigate to add members screen
 */
@Composable
fun GroupDetailsScreen(
    group: Group,
    lastMessage: BaseMessage? = null,
    onBackPress: () -> Unit,
    onGroupLeft: () -> Unit = {},
    onGroupDeleted: () -> Unit = {},
    onChatDeleted: () -> Unit = {},
    onNavigateToAddMembers: (String) -> Unit = {}
) {
    val viewModel: GroupDetailsViewModel = viewModel()
    val currentGroup by viewModel.group.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val isModerator by viewModel.isModerator.collectAsState()
    val isMember by viewModel.isMember.collectAsState()
    
    var showMembersDialog by remember { mutableStateOf(false) }
    var showBannedMembersDialog by remember { mutableStateOf(false) }
    var showLeaveGroupDialog by remember { mutableStateOf(false) }
    var showDeleteGroupDialog by remember { mutableStateOf(false) }
    var showDeleteChatDialog by remember { mutableStateOf(false) }
    var showTransferOwnershipDialog by remember { mutableStateOf(false) }
    
    // Initialize ViewModel
    LaunchedEffect(group) {
        viewModel.initialize(group, lastMessage)
    }
    
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val textColorPrimary = CometChatTheme.colorScheme.textColorPrimary
    val textColorSecondary = CometChatTheme.colorScheme.textColorSecondary
    val warningColor = CometChatTheme.colorScheme.warningColor
    val errorColor = CometChatTheme.colorScheme.errorColor
    val strokeColorLight = CometChatTheme.colorScheme.strokeColorLight
    val iconTintHighlight = CometChatTheme.colorScheme.iconTintHighlight
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            AppFlowToolbar(
                title = "Group Info",
                onBackPress = onBackPress,
                titleStyle = CometChatTheme.typography.heading2Bold
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Non-member banner
            if (!isMember) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(warningColor)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = AppR.drawable.ic_info),
                        contentDescription = null,
                        tint = textColorPrimary,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp)
                    )
                    Text(
                        text = "You are no longer part of this group",
                        style = CometChatTheme.typography.bodyRegular,
                        color = textColorPrimary
                    )
                }
            }
            
            // Group info section - 40dp top margin
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar - 120dp with 40dp top margin
                Spacer(modifier = Modifier.height(40.dp))
                
                CometChatAvatar(
                    modifier = Modifier.size(120.dp),
                    name = currentGroup?.name ?: group.name,
                    avatarUrl = currentGroup?.icon ?: group.icon,
                    style = AvatarStyle.default(cornerRadius = 60.dp)
                )
                
                // Group name - 12dp top margin (cometchat_margin_3)
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = currentGroup?.name ?: group.name,
                    style = CometChatTheme.typography.heading2Medium,
                    color = textColorPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 3
                )
                
                // Member count - 4dp top margin (cometchat_margin_1)
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${currentGroup?.membersCount ?: group.membersCount} members",
                    style = CometChatTheme.typography.caption1Regular,
                    color = textColorSecondary,
                    textAlign = TextAlign.Center
                )
                
                // Action cards in horizontal row - 12dp top margin (cometchat_margin_3)
                if (isMember) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // View Members card
                        GroupActionCard(
                            modifier = Modifier.weight(1f),
                            icon = AppR.drawable.ic_group,
                            label = "View\nMembers",
                            strokeColor = strokeColorLight,
                            iconTint = iconTintHighlight,
                            onClick = { showMembersDialog = true }
                        )
                        
                        // Add Members card (admin/owner only)
                        if (isAdmin || isOwner) {
                            GroupActionCard(
                                modifier = Modifier.weight(1f),
                                icon = AppR.drawable.ic_create_group,
                                label = "Add\nMembers",
                                strokeColor = strokeColorLight,
                                iconTint = iconTintHighlight,
                                onClick = { onNavigateToAddMembers(group.guid) }
                            )
                        }
                        
                        // Banned Members card (admin/moderator/owner only)
                        if (isAdmin || isOwner || isModerator) {
                            GroupActionCard(
                                modifier = Modifier.weight(1f),
                                icon = AppR.drawable.ic_banned_members,
                                label = "Banned\nMembers",
                                strokeColor = strokeColorLight,
                                iconTint = iconTintHighlight,
                                onClick = { showBannedMembersDialog = true }
                            )
                        }
                    }
                }
            }
            
            // Separator - 20dp top margin (cometchat_margin_5)
            Spacer(modifier = Modifier.height(20.dp))
            
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = strokeColorLight
            )
            
            // Leave Group (non-owner members)
            if (isMember && !isOwner) {
                ActionButtonWithIcon(
                    text = "Leave",
                    icon = AppR.drawable.ic_leave_group,
                    iconTint = errorColor,
                    textColor = errorColor,
                    onClick = { showLeaveGroupDialog = true }
                )
            }
            
            // Delete Chat (always available for joined members)
            if (isMember) {
                ActionButtonWithIcon(
                    text = "Delete Chat",
                    icon = AppR.drawable.ic_delete,
                    iconTint = errorColor,
                    textColor = errorColor,
                    onClick = { showDeleteChatDialog = true }
                )
            }
            
            // Delete & Exit (owner only)
            if (isOwner) {
                ActionButtonWithIcon(
                    text = "Delete & Exit",
                    icon = AppR.drawable.ic_delete,
                    iconTint = errorColor,
                    textColor = errorColor,
                    onClick = {
                        val membersCount = currentGroup?.membersCount ?: group.membersCount
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
    
    // Dialogs
    if (showMembersDialog) {
        GroupMembersDialog(
            group = currentGroup ?: group,
            onDismiss = { showMembersDialog = false }
        )
    }
    
    if (showBannedMembersDialog) {
        BannedMembersDialog(
            group = currentGroup ?: group,
            onDismiss = { showBannedMembersDialog = false }
        )
    }
    
    if (showTransferOwnershipDialog) {
        TransferOwnershipDialog(
            group = currentGroup ?: group,
            onDismiss = { showTransferOwnershipDialog = false },
            onOwnershipTransferred = {
                showTransferOwnershipDialog = false
                showLeaveGroupDialog = true
            }
        )
    }
    
    if (showLeaveGroupDialog) {
        CometChatDialog(
            title = "Leave Group",
            message = "Are you sure you want to leave this group?",
            positiveButtonText = "Leave",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                viewModel.leaveGroup {
                    showLeaveGroupDialog = false
                    onGroupLeft()
                }
            },
            onNegativeClick = { showLeaveGroupDialog = false },
            onDismiss = { showLeaveGroupDialog = false }
        )
    }
    
    if (showDeleteGroupDialog) {
        CometChatDialog(
            title = "Delete Group",
            message = "Are you sure you want to delete this group? This action cannot be undone.",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                viewModel.deleteGroup {
                    showDeleteGroupDialog = false
                    onGroupDeleted()
                }
            },
            onNegativeClick = { showDeleteGroupDialog = false },
            onDismiss = { showDeleteGroupDialog = false }
        )
    }
    
    if (showDeleteChatDialog) {
        CometChatDialog(
            title = "Delete Chat",
            message = "Are you sure you want to delete this chat? This action cannot be undone.",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                viewModel.deleteChat {
                    showDeleteChatDialog = false
                    onChatDeleted()
                }
            },
            onNegativeClick = { showDeleteChatDialog = false },
            onDismiss = { showDeleteChatDialog = false }
        )
    }
}

/**
 * Group action card matching MaterialCardView with stroke border.
 * cornerRadius: 8dp (cometchat_radius_2), strokeWidth: 1dp, elevation: 0dp
 * Icon on top, text below, centered.
 * Note: Icons have their colors baked in, so we use Color.Unspecified for tint.
 */
@Composable
private fun GroupActionCard(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    strokeColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
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
 * Padding: 16dp all sides (cometchat_padding_4)
 * Note: Icons have their colors baked in, so we use Color.Unspecified for tint.
 */
@Composable
private fun ActionButtonWithIcon(
    text: String,
    icon: Int,
    iconTint: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
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
            color = textColor
        )
    }
}
