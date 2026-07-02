package com.cometchat.sampleapp.compose.push.appflow.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.pushnotification.CometChatPushNotifications
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageList
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.domain.model.ComposerLayoutMode
import com.cometchat.sampleapp.compose.push.appflow.viewmodels.MessagesViewModel

/**
 * Messages screen displaying MessageHeader, MessageList, and MessageComposer.
 *
 * @param user The user for one-on-one conversations
 * @param group The group for group conversations
 * @param goToMessageId Optional message ID to scroll to
 * @param lastMessageId The last message ID from the conversation (fallback for details navigation)
 * @param onBackPress Callback when back is pressed
 * @param onNavigateToUserDetails Callback to navigate to user details (userId, lastMessageId)
 * @param onNavigateToGroupDetails Callback to navigate to group details (groupId, lastMessageId)
 * @param onNavigateToThread Callback to navigate to thread (parentMessageId, userId, groupId)
 * @param onNavigateToSearch Callback to navigate to search (userId, groupId)
 * @param onNavigateToChatHistory Callback to navigate to chat history (userId)
 */
@Composable
fun MessagesScreen(
    user: User? = null,
    group: Group? = null,
    goToMessageId: Long? = null,
    lastMessageId: Long? = null,
    parentMessageId: Long? = null,
    onBackPress: () -> Unit,
    onNavigateToUserDetails: (String, Long?) -> Unit,
    onNavigateToGroupDetails: (String, Long?) -> Unit,
    onNavigateToThread: (Long, String?, String?) -> Unit,
    onNavigateToSearch: (String?, String?) -> Unit,
    onNavigateToChatHistory: ((String) -> Unit)? = null,
    onNewChat: ((String) -> Unit)? = null
) {
    val viewModel: MessagesViewModel = viewModel()
    val isBlocked by viewModel.isBlocked.collectAsState()
    val isGroupMember by viewModel.isGroupMember.collectAsState()
    val lastMessage by viewModel.lastMessage.collectAsState()
    
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    // Initialize ViewModel with user/group
    LaunchedEffect(user?.uid, group?.guid) {
        viewModel.initialize(user, group)
    }

    // Suppress push notifications for the conversation currently on screen.
    val openChatId = user?.uid ?: group?.guid
    DisposableEffect(openChatId) {
        openChatId?.let { CometChatPushNotifications.setCurrentOpenChatId(it) }
        onDispose { CometChatPushNotifications.setCurrentOpenChatId(null) }
    }

    // Use lastMessage from ViewModel if available, otherwise use the passed lastMessageId
    val effectiveLastMessageId = lastMessage?.id?.toLong() ?: lastMessageId
    
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
            // Message Header
            CometChatMessageHeader(
                modifier = Modifier.fillMaxWidth(),
                user = user,
                group = group,
                hideBackButton = false,
                hideVideoCallButton = isBlocked,
                hideVoiceCallButton = isBlocked,
                onBackPress = onBackPress,
                onChatHistoryClick = {
                    user?.let { onNavigateToChatHistory?.invoke(it.uid) }
                },
                onNewChatClick = {
                    user?.let { onNewChat?.invoke(it.uid) }
                },
                trailingView = { _, _ ->
                    // Overflow menu
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
                            onSearchClick = {
                                showOverflowMenu = false
                                onNavigateToSearch(user?.uid, group?.guid)
                            },
                            onDetailsClick = {
                                showOverflowMenu = false
                                user?.let { onNavigateToUserDetails(it.uid, effectiveLastMessageId) }
                                group?.let { onNavigateToGroupDetails(it.guid, effectiveLastMessageId) }
                            }
                        )
                    }
                }
            )
            
            // Non-member banner for groups
            if (group != null && !isGroupMember) {
                NonMemberBanner()
            }
            
            // Message List
            CometChatMessageList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                user = user,
                group = group,
                parentMessageId = parentMessageId ?: -1,
                goToMessageId = goToMessageId,
                startFromUnreadMessages = true,
                onThreadRepliesClick = { message ->
                    onNavigateToThread(message.id.toLong(), user?.uid, group?.guid)
                }
            )
            
            // Blocked user banner (at bottom, replacing composer)
            if (isBlocked) {
                BlockedUserBanner(
                    onUnblockClick = { viewModel.unblockUser() }
                )
            }
            
            // Message Composer (hidden when blocked or not a member)
            if (!isBlocked && (group == null || isGroupMember)) {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth(),
                    user = user,
                    group = group,
                    parentMessageId = parentMessageId ?: -1,
                    enableRichTextFormatting = true,
                    layoutMode = ComposerLayoutMode.MULTI_LINE,
                    enabledFormats = setOf(
                        com.cometchat.uikit.core.formatter.RichTextFormat.BOLD,
                        com.cometchat.uikit.core.formatter.RichTextFormat.ITALIC,
                        com.cometchat.uikit.core.formatter.RichTextFormat.UNDERLINE,
                        com.cometchat.uikit.core.formatter.RichTextFormat.STRIKETHROUGH,
                        com.cometchat.uikit.core.formatter.RichTextFormat.INLINE_CODE,
                        com.cometchat.uikit.core.formatter.RichTextFormat.CODE_BLOCK,
                        com.cometchat.uikit.core.formatter.RichTextFormat.LINK,
                        com.cometchat.uikit.core.formatter.RichTextFormat.BULLET_LIST,
                        com.cometchat.uikit.core.formatter.RichTextFormat.ORDERED_LIST,
                        com.cometchat.uikit.core.formatter.RichTextFormat.BLOCKQUOTE
                    ),
                    // Show all attachment options
                    hidePollOption = false,
                    hideCollaborativeDocumentOption = false,
                    hideCollaborativeWhiteboardOption = false
                )
            }
        }
    }
}

/**
 * Overflow menu for messages screen.
 */
@Composable
private fun MessagesOverflowMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    onSearchClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val menuItems = listOf(
        MenuItem(
            id = "search",
            name = "Search",
            startIcon = painterResource(id = R.drawable.cometchat_ic_search),
            onClick = onSearchClick
        ),
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
        // Empty anchor
    }
}

/**
 * Banner shown when user is blocked.
 */
@Composable
private fun BlockedUserBanner(
    onUnblockClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "You have blocked this user",
            style = CometChatTheme.typography.bodyRegular,
            color = CometChatTheme.colorScheme.textColorSecondary
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))

        androidx.compose.material3.OutlinedButton(
            onClick = onUnblockClick,
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = CometChatTheme.colorScheme.backgroundColor4
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                CometChatTheme.colorScheme.strokeColorDark
            )
        ) {
            Text(
                text = "Unblock User",
                style = CometChatTheme.typography.bodyMedium,
                color = CometChatTheme.colorScheme.textColorPrimary
            )
        }
    }
}

/**
 * Banner shown when user is not a member of the group.
 */
@Composable
private fun NonMemberBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CometChatTheme.colorScheme.infoColor.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.cometchat_ic_info),
            contentDescription = null,
            tint = CometChatTheme.colorScheme.infoColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "You are not a member of this group",
            style = CometChatTheme.typography.bodyRegular,
            color = CometChatTheme.colorScheme.textColorPrimary
        )
    }
}
