package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.compose.presentation.conversations.style.CometChatConversationsStyle
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversations
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.conversations.utils.ConversationUtils
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.core.factory.CometChatConversationsViewModelFactory
import com.cometchat.uikit.compose.preview.data.repository.PreviewConversationListRepository
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.preview.presentation.viewmodels.PreviewViewModelFactory
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Helper to create a preview ViewModel with mock data using the factory pattern.
 */
@Composable
private fun rememberPreviewViewModel(
    conversations: List<Conversation> = PreviewMockData.createSampleConversations(),
    simulateError: Boolean = false,
    simulateEmpty: Boolean = false
): CometChatConversationsViewModel {
    val factory = remember(conversations, simulateError, simulateEmpty) {
        CometChatConversationsViewModelFactory(
            repository = PreviewConversationListRepository(
                initialConversations = conversations,
                simulateError = simulateError,
                simulateEmpty = simulateEmpty
            ),
            enableListeners = false
        )
    }
    return viewModel(factory = factory)
}

// ============================================================================
// SECTION 1: UI STATE PREVIEWS
// ============================================================================

/**
 * Preview showing the loading state.
 */
@Preview(showBackground = true, name = "State - Loading")
@Composable
fun PreviewConversationListLoading() {
    CometChatTheme {
        val style = CometChatConversationsStyle.default()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            CometChatToolbar(
                title = "Chats",
                style = CometChatToolbarStyle.default(
                    backgroundColor = style.backgroundColor,
                    titleTextColor = style.titleTextColor,
                    titleTextStyle = style.titleTextStyle
                ),
                hideBackIcon = true
            )
           CometChatLoadingState(style = style.loadingStateStyle)
        }
    }
}

/**
 * Preview showing the empty state.
 */
@Preview(showBackground = true, name = "State - Empty")
@Composable
fun PreviewConversationListEmpty() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(simulateEmpty = true)
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Chats",
            hideBackIcon = true
        )
    }
}

/**
 * Preview showing the error state.
 */
@Preview(showBackground = true, name = "State - Error")
@Composable
fun PreviewConversationListError() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(simulateError = true)
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Chats",
            hideBackIcon = true
        )
    }
}

/**
 * Preview showing the content state with conversations.
 */
@Preview(showBackground = true, name = "State - Content")
@Composable
fun PreviewConversationListContent() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel()
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Chats",
            hideBackIcon = true
        )
    }
}

// ============================================================================
// SECTION 2: CUSTOM VIEWMODEL PREVIEWS
// ============================================================================

/**
 * Preview using PreviewViewModelFactory with default ViewModel.
 */
@Preview(showBackground = true, name = "ViewModel - Default Factory")
@Composable
fun PreviewWithDefaultFactoryViewModel() {
    CometChatTheme {
        val viewModel = remember { PreviewViewModelFactory.createDefaultViewModel() }
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Default ViewModel",
            hideBackIcon = true
        )
    }
}

/**
 * Preview using custom ViewModel with high unread counts.
 */
@Preview(showBackground = true, name = "ViewModel - High Unread")
@Composable
fun PreviewWithHighUnreadViewModel() {
    CometChatTheme {
        val viewModel = remember { PreviewViewModelFactory.createHighUnreadViewModel() }
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "High Unread Counts",
            hideBackIcon = true
        )
    }
}

/**
 * Preview using custom ViewModel with only groups.
 */
@Preview(showBackground = true, name = "ViewModel - Groups Only")
@Composable
fun PreviewWithGroupsOnlyViewModel() {
    CometChatTheme {
        val viewModel = remember { PreviewViewModelFactory.createGroupsOnlyViewModel() }
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Groups Only",
            hideBackIcon = true
        )
    }
}

/**
 * Preview using custom ViewModel with only users.
 */
@Preview(showBackground = true, name = "ViewModel - Users Only")
@Composable
fun PreviewWithUsersOnlyViewModel() {
    CometChatTheme {
        val viewModel = remember { PreviewViewModelFactory.createUsersOnlyViewModel() }
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Users Only",
            hideBackIcon = true
        )
    }
}

/**
 * Preview using custom ViewModel with empty state.
 */
@Preview(showBackground = true, name = "ViewModel - Empty State")
@Composable
fun PreviewWithEmptyStateViewModel() {
    CometChatTheme {
        val viewModel = remember { PreviewViewModelFactory.createEmptyStateViewModel() }
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Empty State",
            hideBackIcon = true
        )
    }
}

/**
 * Preview using custom ViewModel with error state.
 */
@Preview(showBackground = true, name = "ViewModel - Error State")
@Composable
fun PreviewWithErrorStateViewModel() {
    CometChatTheme {
        val viewModel = remember { 
            PreviewViewModelFactory.createErrorStateViewModel("Network connection failed") 
        }
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Error State",
            hideBackIcon = true
        )
    }
}

// ============================================================================
// SECTION 3: SELECTION MODE PREVIEWS
// ============================================================================

/**
 * Preview showing single selection mode.
 */
@Preview(showBackground = true, name = "Selection - Single")
@Composable
fun PreviewSingleSelection() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(4)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Single Selection",
            hideBackIcon = true,
            selectionMode = UIKitConstants.SelectionMode.SINGLE
        )
    }
}

/**
 * Preview showing multiple selection mode.
 */
@Preview(showBackground = true, name = "Selection - Multiple")
@Composable
fun PreviewMultipleSelection() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(4)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Multiple Selection",
            hideBackIcon = true,
            selectionMode = UIKitConstants.SelectionMode.MULTIPLE
        )
    }
}

// ============================================================================
// SECTION 4: VISIBILITY PROPS PREVIEWS
// ============================================================================

/**
 * Preview without toolbar.
 */
@Preview(showBackground = true, name = "Visibility - No Toolbar")
@Composable
fun PreviewNoToolbar() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            hideToolbar = true
        )
    }
}

/**
 * Preview without search box.
 */
@Preview(showBackground = true, name = "Visibility - No Search Box")
@Composable
fun PreviewNoSearchBox() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "No Search",
            hideBackIcon = true,
            hideSearchBox = true
        )
    }
}

/**
 * Preview without separators.
 */
@Preview(showBackground = true, name = "Visibility - No Separators")
@Composable
fun PreviewNoSeparators() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(4)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "No Separators",
            hideBackIcon = true,
            hideSeparator = true
        )
    }
}

/**
 * Preview with hidden user status.
 */
@Preview(showBackground = true, name = "Visibility - No User Status")
@Composable
fun PreviewHideUserStatus() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createUserStatusConversations()
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Status Hidden",
            hideBackIcon = true,
            hideUserStatus = true
        )
    }
}

/**
 * Preview with hidden group type.
 */
@Preview(showBackground = true, name = "Visibility - No Group Type")
@Composable
fun PreviewHideGroupType() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createGroupTypeConversations()
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Group Type Hidden",
            hideBackIcon = true,
            hideGroupType = true
        )
    }
}

/**
 * Preview with hidden receipts.
 */
@Preview(showBackground = true, name = "Visibility - No Receipts")
@Composable
fun PreviewHideReceipts() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel()
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Receipts Hidden",
            hideBackIcon = true,
            hideReceipts = true
        )
    }
}

/**
 * Preview with back button visible.
 */
@Preview(showBackground = true, name = "Visibility - With Back Button")
@Composable
fun PreviewWithBackButton() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "With Back",
            hideBackIcon = false,
            onBackPress = { }
        )
    }
}


// ============================================================================
// SECTION 5: CUSTOM VIEW OVERRIDES
// ============================================================================

/**
 * Preview with custom loading view.
 */
@Preview(showBackground = true, name = "Custom View - Loading")
@Composable
fun PreviewCustomLoadingView() {
    CometChatTheme {
        val style = CometChatConversationsStyle.default()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
           CometChatToolbar(
                title = "Custom Loading",
                style = CometChatToolbarStyle.default(
                    backgroundColor = style.backgroundColor,
                    titleTextColor = style.titleTextColor
                ),
                hideBackIcon = true
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = CometChatTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading conversations...",
                        style = CometChatTheme.typography.bodyRegular,
                        color = CometChatTheme.colorScheme.textColorSecondary
                    )
                }
            }
        }
    }
}

/**
 * Preview with custom empty view.
 */
@Preview(showBackground = true, name = "Custom View - Empty")
@Composable
fun PreviewCustomEmptyView() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(simulateEmpty = true)
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Custom Empty",
            hideBackIcon = true,
            emptyView = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(CometChatTheme.colorScheme.backgroundColor3),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = CometChatTheme.colorScheme.iconTintSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No conversations yet",
                            style = CometChatTheme.typography.heading3Medium,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start a new chat!",
                            style = CometChatTheme.typography.bodyRegular,
                            color = CometChatTheme.colorScheme.textColorSecondary
                        )
                    }
                }
            }
        )
    }
}

/**
 * Preview with custom error view.
 */
@Preview(showBackground = true, name = "Custom View - Error")
@Composable
fun PreviewCustomErrorView() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(simulateError = true)
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Custom Error",
            hideBackIcon = true,
            errorView = { onRetry ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(CometChatTheme.colorScheme.errorColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "!",
                                style = CometChatTheme.typography.heading1Bold,
                                color = CometChatTheme.colorScheme.errorColor
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Something went wrong",
                            style = CometChatTheme.typography.heading3Medium,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                    }
                }
            }
        )
    }
}

/**
 * Preview with custom item view.
 */
@Preview(showBackground = true, name = "Custom View - Item")
@Composable
fun PreviewCustomItemView() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Custom Items",
            hideBackIcon = true,
            itemView = { conversation, _ ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CometChatTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = ConversationUtils.getConversationTitle(conversation),
                            style = CometChatTheme.typography.heading4Medium,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                    }
                }
            }
        )
    }
}

/**
 * Preview with custom leading view.
 */
@Preview(showBackground = true, name = "Custom View - Leading")
@Composable
fun PreviewCustomLeadingView() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Custom Leading",
            hideBackIcon = true,
            leadingView = { conversation, _ ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CometChatTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ConversationUtils.getConversationTitle(conversation)
                            .firstOrNull()?.uppercase() ?: "?",
                        style = CometChatTheme.typography.heading3Bold,
                        color = CometChatTheme.colorScheme.colorWhite
                    )
                }
            }
        )
    }
}

/**
 * Preview with custom title view.
 */
@Preview(showBackground = true, name = "Custom View - Title")
@Composable
fun PreviewListCustomTitleView() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Custom Title",
            hideBackIcon = true,
            titleView = { conversation, _ ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CometChatTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ConversationUtils.getConversationTitle(conversation),
                        style = CometChatTheme.typography.heading4Bold,
                        color = CometChatTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

/**
 * Preview with custom subtitle view.
 */
@Preview(showBackground = true, name = "Custom View - Subtitle")
@Composable
fun PreviewListCustomSubtitleView() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Custom Subtitle",
            hideBackIcon = true,
            subtitleView = { conversation, typingIndicator ->
                Text(
                    text = if (typingIndicator?.isTyping == true) "✍️ typing..." else "Custom subtitle",
                    style = CometChatTheme.typography.caption1Medium,
                    color = if (typingIndicator?.isTyping == true) 
                        CometChatTheme.colorScheme.primary 
                    else 
                        CometChatTheme.colorScheme.textColorSecondary
                )
            }
        )
    }
}

/**
 * Preview with custom trailing view.
 */
@Preview(showBackground = true, name = "Custom View - Trailing")
@Composable
fun PreviewListCustomTrailingView() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Custom Trailing",
            hideBackIcon = true,
            trailingView = { conversation, _ ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Now",
                        style = CometChatTheme.typography.caption1Regular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                    if (conversation.unreadMessageCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(CometChatTheme.colorScheme.errorColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${conversation.unreadMessageCount}",
                                style = CometChatTheme.typography.caption2Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        )
    }
}


// ============================================================================
// SECTION 6: TOOLBAR CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom title text.
 */
@Preview(showBackground = true, name = "Toolbar - Custom Title")
@Composable
fun PreviewCustomTitle() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(2)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "My Conversations",
            hideBackIcon = true
        )
    }
}

/**
 * Preview with custom overflow menu.
 */
@Preview(showBackground = true, name = "Toolbar - Custom Overflow Menu")
@Composable
fun PreviewCustomOverflowMenu() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(2)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Chats",
            hideBackIcon = true,
            overflowMenu = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = CometChatTheme.colorScheme.iconTintPrimary
                    )
                }
            }
        )
    }
}

/**
 * Preview with custom search placeholder.
 */
@Preview(showBackground = true, name = "Toolbar - Custom Search Placeholder")
@Composable
fun PreviewCustomSearchPlaceholder() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(2)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Chats",
            hideBackIcon = true,
            searchPlaceholderText = "Find conversations..."
        )
    }
}

// ============================================================================
// SECTION 7: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "Style - Custom Background")
@Composable
fun PreviewCustomBackgroundStyle() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Custom Background",
            hideBackIcon = true,
            style = CometChatConversationsStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            )
        )
    }
}

/**
 * Preview with custom title text color.
 */
@Preview(showBackground = true, name = "Style - Custom Title Color")
@Composable
fun PreviewCustomTitleColorStyle() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Custom Title Color",
            hideBackIcon = true,
            style = CometChatConversationsStyle.default(
                titleTextColor = CometChatTheme.colorScheme.primary
            )
        )
    }
}

/**
 * Preview with dark theme simulation.
 */
@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Style - Dark Theme")
@Composable
fun PreviewDarkThemeStyle() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Dark Theme",
            hideBackIcon = true,
            style = CometChatConversationsStyle.default(
                backgroundColor = Color(0xFF1A1A1A),
                titleTextColor = Color.White
            )
        )
    }
}

// ============================================================================
// SECTION 8: COMPREHENSIVE PREVIEWS
// ============================================================================

/**
 * Preview showing all features combined.
 */
@Preview(showBackground = true, name = "Comprehensive - All Features")
@Composable
fun PreviewComprehensive() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel()
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "All Features",
            hideBackIcon = false,
            searchPlaceholderText = "Search chats...",
            overflowMenu = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = CometChatTheme.colorScheme.iconTintPrimary
                    )
                }
            },
            onBackPress = { },
            onItemClick = { },
            onSearchClick = { }
        )
    }
}

/**
 * Preview showing minimal configuration.
 */
@Preview(showBackground = true, name = "Comprehensive - Minimal")
@Composable
fun PreviewMinimal() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            hideToolbar = true,
            hideSearchBox = true,
            hideSeparator = true
        )
    }
}

/**
 * Preview showing large list for scroll testing.
 */
@Preview(showBackground = true, name = "Comprehensive - Large List")
@Composable
fun PreviewLargeList() {
    CometChatTheme {
        val viewModel = remember { PreviewViewModelFactory.createLargeListViewModel(20) }
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "Large List (20 items)",
            hideBackIcon = true
        )
    }
}

/**
 * Preview showing all custom views combined.
 */
@Preview(showBackground = true, name = "Comprehensive - All Custom Views")
@Composable
fun PreviewListAllCustomViews() {
    CometChatTheme {
        val viewModel = rememberPreviewViewModel(
            conversations = PreviewMockData.createSampleConversations().take(3)
        )
        CometChatConversations(
            conversationListViewModel = viewModel,
            title = "All Custom Views",
            hideBackIcon = true,
            leadingView = { conversation, _ ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CometChatTheme.colorScheme.infoColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ConversationUtils.getConversationTitle(conversation)
                            .take(2).uppercase(),
                        style = CometChatTheme.typography.caption1Bold,
                        color = Color.White
                    )
                }
            },
            titleView = { conversation, _ ->
                Text(
                    text = "★ ${ConversationUtils.getConversationTitle(conversation)}",
                    style = CometChatTheme.typography.heading4Bold,
                    color = CometChatTheme.colorScheme.primary
                )
            },
            subtitleView = { _, _ ->
                Text(
                    text = "Custom subtitle text",
                    style = CometChatTheme.typography.caption1Regular,
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            },
            trailingView = { conversation, _ ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "12:30 PM",
                        style = CometChatTheme.typography.caption2Regular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                    if (conversation.unreadMessageCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📬 ${conversation.unreadMessageCount}",
                            style = CometChatTheme.typography.caption2Medium,
                            color = CometChatTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }
}
