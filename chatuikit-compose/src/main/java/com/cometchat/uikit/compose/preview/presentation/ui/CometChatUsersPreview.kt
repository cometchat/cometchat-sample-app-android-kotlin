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
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.compose.presentation.users.style.CometChatUsersStyle
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatUsersViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import com.cometchat.uikit.compose.preview.data.repository.PreviewUsersRepository
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.preview.presentation.viewmodels.PreviewUsersViewModelFactory
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Helper to create a preview ViewModel with mock data using the factory pattern.
 */
@Composable
private fun rememberPreviewUsersViewModel(
    users: List<User> = PreviewMockData.createSampleUsers(),
    simulateError: Boolean = false,
    simulateEmpty: Boolean = false
): CometChatUsersViewModel {
    val factory = remember(users, simulateError, simulateEmpty) {
        CometChatUsersViewModelFactory(
            repository = PreviewUsersRepository(
                initialUsers = users,
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
@Preview(showBackground = true, name = "Users - State - Loading")
@Composable
fun PreviewUsersLoading() {
    CometChatTheme {
        val style = CometChatUsersStyle.default()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            CometChatToolbar(
                title = "Users",
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
@Preview(showBackground = true, name = "Users - State - Empty")
@Composable
fun PreviewUsersEmpty() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(simulateEmpty = true)
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Users",
            hideBackIcon = true
        )
    }
}

/**
 * Preview showing the error state.
 */
@Preview(showBackground = true, name = "Users - State - Error")
@Composable
fun PreviewUsersError() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(simulateError = true)
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Users",
            hideBackIcon = true
        )
    }
}

/**
 * Preview showing the content state with users.
 */
@Preview(showBackground = true, name = "Users - State - Content")
@Composable
fun PreviewUsersContent() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel()
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Users",
            hideBackIcon = true
        )
    }
}

// ============================================================================
// SECTION 2: CUSTOM VIEWMODEL PREVIEWS
// ============================================================================

/**
 * Preview using PreviewUsersViewModelFactory with default ViewModel.
 */
@Preview(showBackground = true, name = "Users - ViewModel - Default Factory")
@Composable
fun PreviewUsersWithDefaultFactory() {
    CometChatTheme {
        val viewModel = remember { PreviewUsersViewModelFactory.createDefaultViewModel() }
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Default Users",
            hideBackIcon = true
        )
    }
}

/**
 * Preview with only online users.
 */
@Preview(showBackground = true, name = "Users - ViewModel - Online Only")
@Composable
fun PreviewUsersOnlineOnly() {
    CometChatTheme {
        val viewModel = remember { PreviewUsersViewModelFactory.createOnlineUsersViewModel() }
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Online Users",
            hideBackIcon = true
        )
    }
}

/**
 * Preview with only offline users.
 */
@Preview(showBackground = true, name = "Users - ViewModel - Offline Only")
@Composable
fun PreviewUsersOfflineOnly() {
    CometChatTheme {
        val viewModel = remember { PreviewUsersViewModelFactory.createOfflineUsersViewModel() }
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Offline Users",
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
@Preview(showBackground = true, name = "Users - Selection - Single")
@Composable
fun PreviewUsersSingleSelection() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(4)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Single Selection",
            hideBackIcon = true,
            selectionMode = UIKitConstants.SelectionMode.SINGLE
        )
    }
}

/**
 * Preview showing multiple selection mode.
 */
@Preview(showBackground = true, name = "Users - Selection - Multiple")
@Composable
fun PreviewUsersMultipleSelection() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(4)
        )
        CometChatUsers(
            usersViewModel = viewModel,
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
@Preview(showBackground = true, name = "Users - Visibility - No Toolbar")
@Composable
fun PreviewUsersNoToolbar() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(3)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            hideToolbar = true
        )
    }
}

/**
 * Preview without search box.
 */
@Preview(showBackground = true, name = "Users - Visibility - No Search Box")
@Composable
fun PreviewUsersNoSearchBox() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(3)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "No Search",
            hideBackIcon = true,
            hideSearchBox = true
        )
    }
}

/**
 * Preview without status indicators.
 */
@Preview(showBackground = true, name = "Users - Visibility - No Status Indicator")
@Composable
fun PreviewUsersNoStatusIndicator() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel()
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Status Hidden",
            hideBackIcon = true,
            hideStatusIndicator = true
        )
    }
}

/**
 * Preview without sticky headers.
 */
@Preview(showBackground = true, name = "Users - Visibility - No Sticky Header")
@Composable
fun PreviewUsersNoStickyHeader() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel()
        CometChatUsers(
            usersViewModel = viewModel,
            title = "No Headers",
            hideBackIcon = true,
            hideStickyHeader = true
        )
    }
}

/**
 * Preview without separators.
 */
@Preview(showBackground = true, name = "Users - Visibility - No Separators")
@Composable
fun PreviewUsersNoSeparators() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(4)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "No Separators",
            hideBackIcon = true,
            hideSeparator = true
        )
    }
}

/**
 * Preview with back button visible.
 */
@Preview(showBackground = true, name = "Users - Visibility - With Back Button")
@Composable
fun PreviewUsersWithBackButton() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(3)
        )
        CometChatUsers(
            usersViewModel = viewModel,
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
 * Preview with custom empty view.
 */
@Preview(showBackground = true, name = "Users - Custom View - Empty")
@Composable
fun PreviewUsersCustomEmptyView() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(simulateEmpty = true)
        CometChatUsers(
            usersViewModel = viewModel,
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
                            text = "No users found",
                            style = CometChatTheme.typography.heading3Medium,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try adjusting your search",
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
 * Preview with custom leading view.
 */
@Preview(showBackground = true, name = "Users - Custom View - Leading")
@Composable
fun PreviewUsersCustomLeadingView() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(3)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Custom Leading",
            hideBackIcon = true,
            leadingView = { user ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CometChatTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name?.firstOrNull()?.uppercase() ?: "?",
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
@Preview(showBackground = true, name = "Users - Custom View - Title")
@Composable
fun PreviewUsersCustomTitleView() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(3)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Custom Title",
            hideBackIcon = true,
            titleView = { user ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CometChatTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.name ?: "Unknown",
                        style = CometChatTheme.typography.heading4Bold,
                        color = CometChatTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

/**
 * Preview with custom trailing view.
 */
@Preview(showBackground = true, name = "Users - Custom View - Trailing")
@Composable
fun PreviewUsersCustomTrailingView() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(3)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Custom Trailing",
            hideBackIcon = true,
            trailingView = { user ->
                Text(
                    text = if (user.status == "online") "🟢" else "⚫",
                    modifier = Modifier.padding(4.dp)
                )
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
@Preview(showBackground = true, name = "Users - Toolbar - Custom Title")
@Composable
fun PreviewUsersCustomTitle() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(2)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "My Contacts",
            hideBackIcon = true
        )
    }
}

/**
 * Preview with custom overflow menu.
 */
@Preview(showBackground = true, name = "Users - Toolbar - Overflow Menu")
@Composable
fun PreviewUsersOverflowMenu() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(2)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Users",
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
@Preview(showBackground = true, name = "Users - Toolbar - Custom Search Placeholder")
@Composable
fun PreviewUsersCustomSearchPlaceholder() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(2)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Users",
            hideBackIcon = true,
            searchPlaceholderText = "Find people..."
        )
    }
}

// ============================================================================
// SECTION 7: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "Users - Style - Custom Background")
@Composable
fun PreviewUsersCustomBackground() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(3)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Custom Background",
            hideBackIcon = true,
            style = CometChatUsersStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            )
        )
    }
}

/**
 * Preview with custom title color.
 */
@Preview(showBackground = true, name = "Users - Style - Custom Title Color")
@Composable
fun PreviewUsersCustomTitleColor() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(3)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Custom Title Color",
            hideBackIcon = true,
            style = CometChatUsersStyle.default(
                titleTextColor = CometChatTheme.colorScheme.primary
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
@Preview(showBackground = true, name = "Users - Comprehensive - All Features")
@Composable
fun PreviewUsersComprehensive() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel()
        CometChatUsers(
            usersViewModel = viewModel,
            title = "All Features",
            hideBackIcon = false,
            searchPlaceholderText = "Search users...",
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
            onItemClick = { }
        )
    }
}

/**
 * Preview showing minimal configuration.
 */
@Preview(showBackground = true, name = "Users - Comprehensive - Minimal")
@Composable
fun PreviewUsersMinimal() {
    CometChatTheme {
        val viewModel = rememberPreviewUsersViewModel(
            users = PreviewMockData.createSampleUsers().take(3)
        )
        CometChatUsers(
            usersViewModel = viewModel,
            hideToolbar = true,
            hideSearchBox = true,
            hideSeparator = true,
            hideStickyHeader = true
        )
    }
}

/**
 * Preview showing large list for scroll testing.
 */
@Preview(showBackground = true, name = "Users - Comprehensive - Large List")
@Composable
fun PreviewUsersLargeList() {
    CometChatTheme {
        val viewModel = remember { PreviewUsersViewModelFactory.createLargeListViewModel(20) }
        CometChatUsers(
            usersViewModel = viewModel,
            title = "Large List (20 users)",
            hideBackIcon = true
        )
    }
}
