package com.cometchat.uikit.compose.presentation.threadheader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.compose.presentation.messagelist.style.CometChatMessageListStyle
import com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageList
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.compose.presentation.threadheader.style.CometChatThreadHeaderStyle
import com.cometchat.uikit.compose.presentation.threadheader.viewmodel.ThreadHeaderViewModel
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import java.text.SimpleDateFormat

/**
 * CometChatThreadScreen provides a complete thread conversation view.
 *
 * This composable combines three main components vertically:
 * 1. A toolbar with back button and title
 * 2. CometChatThreadHeader displaying the parent message
 * 3. CometChatMessageList showing thread replies
 * 4. CometChatMessageComposer for composing new replies
 *
 * The screen automatically configures the MessageList and MessageComposer with
 * the parent message ID to ensure replies are properly associated with the thread.
 *
 * ## Layout Structure
 *
 * ```
 * ┌─────────────────────────────────┐
 * │     Back Button + Title         │  ← Toolbar
 * ├─────────────────────────────────┤
 * │                                 │
 * │    CometChatThreadHeader        │  ← Parent message + reply count
 * │    (max 35% screen height)      │
 * │                                 │
 * ├─────────────────────────────────┤
 * │                                 │
 * │    CometChatMessageList         │  ← Thread replies
 * │    (parentMessageId set)        │
 * │                                 │
 * ├─────────────────────────────────┤
 * │    CometChatMessageComposer     │  ← Reply composer
 * │    (parentMessageId set)        │
 * └─────────────────────────────────┘
 * ```
 *
 * ## Usage
 *
 * ```kotlin
 * // For a user conversation thread
 * CometChatThreadScreen(
 *     parentMessage = message,
 *     user = user,
 *     onBackPress = { navController.popBackStack() }
 * )
 *
 * // For a group conversation thread
 * CometChatThreadScreen(
 *     parentMessage = message,
 *     group = group,
 *     onBackPress = { navController.popBackStack() }
 * )
 * ```
 *
 * @param modifier Modifier applied to the root container
 * @param parentMessage The parent message that started the thread (required)
 * @param user The User for one-on-one conversation threads (provide either user or group)
 * @param group The Group for group conversation threads (provide either user or group)
 * @param title Custom title for the toolbar (defaults to "Thread")
 * @param hideToolbar Whether to hide the toolbar with back button
 * @param onBackPress Callback when back button is pressed
 * @param toolbarStyle Style configuration for the toolbar
 * @param threadHeaderStyle Style configuration for the thread header
 * @param messageListStyle Style configuration for the message list
 * @param messageComposerStyle Style configuration for the message composer
 * @param threadHeaderViewModel ViewModel for the thread header (optional)
 * @param hideReactions Whether to hide reactions on the parent message
 * @param hideAvatar Whether to hide avatars
 * @param hideReceipts Whether to hide read receipts
 * @param hideReplyCount Whether to hide the reply count in thread header
 * @param hideReplyCountBar Whether to hide the reply count bar in thread header
 * @param alignment Message alignment mode
 * @param textFormatters List of text formatters for mentions and markdown
 * @param timeFormat Custom time format for timestamps
 * @param maxThreadHeaderHeightFraction Maximum height fraction for thread header (0.0 to 1.0, default 0.35)
 * @param threadHeaderView Custom composable for the thread header section
 * @param messageListView Custom composable for the message list section
 * @param messageComposerView Custom composable for the message composer section
 *
 * **Validates: Requirements 14.1, 14.2, 14.3, 14.4, 14.5**
 *
 * @see CometChatThreadHeader
 * @see CometChatMessageList
 * @see CometChatMessageComposer
 */
@Composable
fun CometChatThreadScreen(
    modifier: Modifier = Modifier,
    parentMessage: BaseMessage,
    // User/Group context - provide one
    user: User? = null,
    group: Group? = null,
    // Toolbar configuration
    title: String? = null,
    hideToolbar: Boolean = false,
    onBackPress: (() -> Unit)? = null,
    // Styles
    toolbarStyle: CometChatToolbarStyle = CometChatToolbarStyle.default(),
    threadHeaderStyle: CometChatThreadHeaderStyle = CometChatThreadHeaderStyle.default(),
    messageListStyle: CometChatMessageListStyle = CometChatMessageListStyle.default(),
    messageComposerStyle: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    // Thread header configuration
    threadHeaderViewModel: ThreadHeaderViewModel = viewModel(),
    hideReactions: Boolean = false,
    hideAvatar: Boolean = false,
    hideReceipts: Boolean = false,
    hideReplyCount: Boolean = false,
    hideReplyCountBar: Boolean = false,
    // Alignment and formatters
    alignment: UIKitConstants.MessageListAlignment = UIKitConstants.MessageListAlignment.STANDARD,
    textFormatters: List<CometChatTextFormatter>? = null,
    timeFormat: SimpleDateFormat? = null,
    // Thread header height constraint (fraction of screen height)
    maxThreadHeaderHeightFraction: Float = 0.35f,
    // Custom view slots
    threadHeaderView: (@Composable (BaseMessage) -> Unit)? = null,
    messageListView: (@Composable (Long, User?, Group?) -> Unit)? = null,
    messageComposerView: (@Composable (Long, User?, Group?) -> Unit)? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    // Calculate max height for thread header (35% of screen height by default)
    val screenHeightDp = configuration.screenHeightDp.dp
    val maxThreadHeaderHeight = remember(screenHeightDp, maxThreadHeaderHeightFraction) {
        screenHeightDp * maxThreadHeaderHeightFraction
    }
    
    // Get parent message ID for configuring child components
    val parentMessageId = parentMessage.id
    
    // Default title
    val toolbarTitle = title ?: stringResource(R.string.cometchat_reply_uppercase)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CometChatTheme.colorScheme.backgroundColor1)
    ) {
        // Toolbar with back button
        if (!hideToolbar) {
            CometChatToolbar(
                title = toolbarTitle,
                style = toolbarStyle,
                onNavigationClick = onBackPress,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Thread Header - displays parent message with reply count
        if (threadHeaderView != null) {
            threadHeaderView(parentMessage)
        } else {
            CometChatThreadHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxThreadHeaderHeight),
                parentMessage = parentMessage,
                viewModel = threadHeaderViewModel,
                style = threadHeaderStyle,
                hideReactions = hideReactions,
                hideAvatar = hideAvatar,
                hideReceipts = hideReceipts,
                hideReplyCount = hideReplyCount,
                hideReplyCountBar = hideReplyCountBar,
                maxHeight = maxThreadHeaderHeight,
                alignment = alignment,
                textFormatters = textFormatters,
                timeFormat = timeFormat
            )
        }
        
        // Message List - displays thread replies
        if (messageListView != null) {
            messageListView(parentMessageId, user, group)
        } else {
            CometChatMessageList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                user = user,
                group = group,
                parentMessageId = parentMessageId,
                style = messageListStyle,
                hideAvatar = hideAvatar,
                hideReceipts = hideReceipts,
                messageAlignment = alignment,
                // Hide thread reply option in thread view to avoid nested threads
                hideReplyInThreadOption = true
            )
        }
        
        // Message Composer - for composing thread replies
        if (messageComposerView != null) {
            messageComposerView(parentMessageId, user, group)
        } else {
            CometChatMessageComposer(
                modifier = Modifier.fillMaxWidth(),
                user = user,
                group = group,
                parentMessageId = parentMessageId,
                style = messageComposerStyle
            )
        }
    }
}
