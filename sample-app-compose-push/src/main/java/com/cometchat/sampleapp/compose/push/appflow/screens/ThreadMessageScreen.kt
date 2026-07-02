package com.cometchat.sampleapp.compose.push.appflow.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageList
import com.cometchat.uikit.compose.presentation.threadheader.ui.CometChatThreadHeader
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.appflow.viewmodels.ThreadMessageViewModel

/**
 * Thread message screen for viewing and replying to threaded messages.
 *
 * @param parentMessage The parent message of the thread
 * @param user The user for one-on-one conversations
 * @param group The group for group conversations
 * @param onBackPress Callback when back is pressed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadMessageScreen(
    parentMessage: BaseMessage,
    user: User? = null,
    group: Group? = null,
    onBackPress: () -> Unit
) {
    val viewModel: ThreadMessageViewModel = viewModel()
    val isBlocked by viewModel.isBlocked.collectAsState()
    
    // Initialize ViewModel
    LaunchedEffect(user) {
        viewModel.initialize(user)
    }
    
    val backgroundColor = CometChatTheme.colorScheme.backgroundColor1
    val textColorPrimary = CometChatTheme.colorScheme.textColorPrimary
    val textColorSecondary = CometChatTheme.colorScheme.textColorSecondary
    
    // Get conversation name for subtitle
    val conversationName = user?.name ?: group?.name ?: ""
    
    Scaffold(
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Thread",
                            style = CometChatTheme.typography.heading3Bold,
                            color = textColorPrimary
                        )
                        if (conversationName.isNotEmpty()) {
                            Text(
                                text = conversationName,
                                style = CometChatTheme.typography.caption1Regular,
                                color = textColorSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            painter = painterResource(id = R.drawable.cometchat_ic_back),
                            contentDescription = "Back",
                            tint = CometChatTheme.colorScheme.iconTintPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        // Calculate max height for thread header (35% of screen height)
        val configuration = LocalConfiguration.current
        val screenHeightDp = configuration.screenHeightDp.dp
        val maxThreadHeaderHeight = remember(screenHeightDp) {
            screenHeightDp * 0.35f
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Blocked user banner
            if (isBlocked) {
                BlockedUserBanner(
                    onUnblockClick = { viewModel.unblockUser() }
                )
            }
            
            // Thread Header - displays parent message with reply count
            CometChatThreadHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxThreadHeaderHeight),
                parentMessage = parentMessage,
                maxHeight = maxThreadHeaderHeight
            )
            
            // Thread Message List
            CometChatMessageList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                user = user,
                group = group,
                parentMessageId = parentMessage.id
            )
            
            // Message Composer (hidden when blocked)
            if (!isBlocked) {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth(),
                    user = user,
                    group = group,
                    parentMessageId = parentMessage.id,
                    enableRichTextFormatting = true,
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
                    // Show all attachment options (extension options are filtered out in threaded context by ViewModel)
                    hidePollOption = false,
                    hideCollaborativeDocumentOption = false,
                    hideCollaborativeWhiteboardOption = false
                )
            }
        }
    }
}

/**
 * Banner shown when user is blocked.
 */
@Composable
private fun BlockedUserBanner(
    onUnblockClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CometChatTheme.colorScheme.warningColor.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_info),
                contentDescription = null,
                tint = CometChatTheme.colorScheme.warningColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "You have blocked this user",
                style = CometChatTheme.typography.bodyRegular,
                color = CometChatTheme.colorScheme.textColorPrimary
            )
        }
        
        Text(
            text = "Unblock",
            style = CometChatTheme.typography.bodyMedium,
            color = CometChatTheme.colorScheme.primary,
            modifier = Modifier.clickable { onUnblockClick() }
        )
    }
}
