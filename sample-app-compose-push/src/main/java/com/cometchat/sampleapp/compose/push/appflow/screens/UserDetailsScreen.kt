package com.cometchat.sampleapp.compose.push.appflow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialog
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.appflow.components.AppFlowToolbar
import com.cometchat.sampleapp.compose.push.appflow.viewmodels.UserDetailsViewModel

/**
 * User details screen showing user profile and actions.
 * Styled to match master-app-kotlin2 activity_user_details.xml.
 *
 * @param user The user to display details for
 * @param lastMessage The last message in the conversation
 * @param onBackPress Callback when back is pressed
 * @param onChatDeleted Callback when chat is deleted
 */
@Composable
fun UserDetailsScreen(
    user: User,
    lastMessage: BaseMessage? = null,
    onBackPress: () -> Unit,
    onChatDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: UserDetailsViewModel = viewModel()
    val isBlocked by viewModel.isBlocked.collectAsState()
    val currentUser by viewModel.user.collectAsState()
    val callInitiated by viewModel.callInitiated.collectAsState()
    
    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var showDeleteChatDialog by remember { mutableStateOf(false) }
    
    // Initialize ViewModel
    LaunchedEffect(user) {
        viewModel.initialize(user, lastMessage)
    }
    
    // Handle call initiated - launch CometChatCallActivity
    LaunchedEffect(callInitiated) {
        callInitiated?.let { call ->
            com.cometchat.uikit.compose.calls.CometChatCallActivity.launchOutgoingCallScreen(context, call, null)
            viewModel.clearCallInitiated()
        }
    }
    
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val textColorPrimary = CometChatTheme.colorScheme.textColorPrimary
    val textColorSecondary = CometChatTheme.colorScheme.textColorSecondary
    val warningColor = CometChatTheme.colorScheme.warningColor
    val errorColor = CometChatTheme.colorScheme.errorColor
    val strokeColorLight = CometChatTheme.colorScheme.strokeColorLight
    val strokeColorDefault = CometChatTheme.colorScheme.strokeColorDefault
    val iconTintHighlight = CometChatTheme.colorScheme.iconTintHighlight
    
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            AppFlowToolbar(
                title = "User Info",
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
            // Blocked user banner
            if (isBlocked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(warningColor)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.cometchat_ic_info),
                        contentDescription = null,
                        tint = textColorPrimary,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp)
                    )
                    Text(
                        text = "You have blocked this user",
                        style = CometChatTheme.typography.bodyRegular,
                        color = textColorPrimary
                    )
                }
            }
            
            // User info section - 40dp top margin
            Spacer(modifier = Modifier.height(40.dp))
            
            // User avatar and info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar - 120dp
                CometChatAvatar(
                    modifier = Modifier.size(120.dp),
                    name = currentUser?.name ?: user.name,
                    avatarUrl = currentUser?.avatar ?: user.avatar,
                    style = AvatarStyle.default(cornerRadius = 60.dp)
                )
                
                // Name - 12dp top margin (cometchat_margin_3)
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = currentUser?.name ?: user.name,
                    style = CometChatTheme.typography.heading2Medium,
                    color = textColorPrimary,
                    textAlign = TextAlign.Center
                )
                
                // Status
                if (!isBlocked) {
                    Text(
                        text = if (currentUser?.status == "online") "Online" else "Offline",
                        style = CometChatTheme.typography.caption1Regular,
                        color = textColorSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Call buttons - 20dp top margin (cometchat_margin_5)
                if (!isBlocked) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Voice call button - MaterialCardView with stroke
                        CallActionCard(
                            modifier = Modifier.weight(1f),
                            icon = R.drawable.cometchat_ic_call_voice,
                            label = "Voice",
                            strokeColor = strokeColorDefault,
                            iconTint = iconTintHighlight,
                            onClick = { viewModel.startVoiceCall() }
                        )
                        
                        // Video call button
                        CallActionCard(
                            modifier = Modifier.weight(1f),
                            icon = R.drawable.cometchat_ic_call_video,
                            label = "Video",
                            strokeColor = strokeColorDefault,
                            iconTint = iconTintHighlight,
                            onClick = { viewModel.startVideoCall() }
                        )
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
            
            // Block/Unblock button with icon
            ActionButtonWithIcon(
                text = if (isBlocked) "Unblock" else "Block",
                icon = R.drawable.cometchat_ic_block,
                iconTint = errorColor,
                textColor = errorColor,
                onClick = {
                    if (isBlocked) {
                        showUnblockDialog = true
                    } else {
                        showBlockDialog = true
                    }
                }
            )
            
            // Delete chat button (only if lastMessage exists)
            if (lastMessage != null) {
                ActionButtonWithIcon(
                    text = "Delete Chat",
                    icon = R.drawable.cometchat_ic_delete,
                    iconTint = errorColor,
                    textColor = errorColor,
                    onClick = { showDeleteChatDialog = true }
                )
            }
        }
    }
    
    // Block confirmation dialog
    if (showBlockDialog) {
        CometChatDialog(
            title = "Block User",
            message = "Are you sure you want to block ${user.name}?",
            positiveButtonText = "Block",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                viewModel.blockUser()
                showBlockDialog = false
            },
            onNegativeClick = { showBlockDialog = false },
            onDismiss = { showBlockDialog = false }
        )
    }
    
    // Unblock confirmation dialog
    if (showUnblockDialog) {
        CometChatDialog(
            title = "Unblock User",
            message = "Are you sure you want to unblock ${user.name}?",
            positiveButtonText = "Unblock",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                viewModel.unblockUser()
                showUnblockDialog = false
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
 * Call action card matching MaterialCardView with stroke border.
 * Height: 73dp, cornerRadius: 8dp (cometchat_radius_2), strokeWidth: 1dp
 */
@Composable
private fun CallActionCard(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    strokeColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
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
 * Padding: 20dp horizontal (cometchat_margin_5), 12dp vertical (cometchat_margin_3)
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
