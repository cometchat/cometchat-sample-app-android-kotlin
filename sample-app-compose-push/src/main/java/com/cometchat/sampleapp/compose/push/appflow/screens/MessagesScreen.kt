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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import com.cometchat.sampleapp.compose.push.appflow.components.BlockedUserBanner
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
    onNavigateToPinnedMessages: ((String?, String?) -> Unit)? = null,
    onNewChat: ((String) -> Unit)? = null
) {
    val viewModel: MessagesViewModel = viewModel()
    val isBlocked by viewModel.isBlocked.collectAsState()
    val isGroupMember by viewModel.isGroupMember.collectAsState()
    val isBlockedByMe by viewModel.isBlockedByMe.collectAsState()
    val isUnblocking by viewModel.isLoading.collectAsState()
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
                            },
                            onPinnedMessagesClick = {
                                showOverflowMenu = false
                                onNavigateToPinnedMessages?.invoke(user?.uid, group?.guid)
                            }
                        )
                    }
                }
            )

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
            
            // Unblock notice (at bottom, replacing composer)
            if (isBlockedByMe) {
                BlockedUserBanner(
                    isUnblocking = isUnblocking,
                    onUnblockClick = { viewModel.unblockUser() },
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Non-member notice (at bottom, replacing composer)
            if (!isBlockedByMe && group != null && !isGroupMember) {
                NonMemberBanner()
            }

            // Message Composer (hidden when blocked or not a member)
            if (!isBlockedByMe && (group == null || isGroupMember)) {
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
    onDetailsClick: () -> Unit,
    onPinnedMessagesClick: () -> Unit
) {
    val menuItems = buildList {
        add(
            MenuItem(
                id = "search",
                name = "Search",
                startIcon = painterResource(id = R.drawable.cometchat_ic_search),
                onClick = onSearchClick
            )
        )
        // Pinned messages — gated on the SDK Pin Message feature flag.
        if (com.cometchat.uikit.core.CometChatUIKit.isPinMessageEnabled()) {
            add(
                MenuItem(
                    id = "pinned_messages",
                    name = "Pinned messages",
                    startIcon = painterResource(id = com.cometchat.uikit.core.R.drawable.cometchat_ic_pin),
                    onClick = onPinnedMessagesClick
                )
            )
        }
        add(
            MenuItem(
                id = "details",
                name = "Details",
                startIcon = painterResource(id = R.drawable.cometchat_ic_info),
                onClick = onDetailsClick
            )
        )
    }

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
 * Notice shown in place of the composer when the user is no longer a member of the group.
 * Mirrors the Kotlin app's info_layout in activity_app_flow_messages.xml: a top separator over
 * centered body text, so both UIKits present the non-member state identically.
 */
@Composable
private fun NonMemberBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CometChatTheme.colorScheme.backgroundColor1)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = CometChatTheme.colorScheme.strokeColorLight
        )
        Text(
            // Qualified: the file-level `R` import is the UIKit's, not this app's.
            text = stringResource(id = com.cometchat.sampleapp.compose.push.R.string.app_block_user_unable_to_send_message),
            style = CometChatTheme.typography.bodyRegular,
            color = CometChatTheme.colorScheme.textColorPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp)
        )
    }
}
