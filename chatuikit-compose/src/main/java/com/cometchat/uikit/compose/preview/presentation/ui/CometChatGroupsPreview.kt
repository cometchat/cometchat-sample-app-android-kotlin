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
import androidx.compose.material.icons.filled.Star
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
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.presentation.groups.style.CometChatGroupsStyle
import com.cometchat.uikit.compose.presentation.groups.ui.CometChatGroups
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatGroupsViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import com.cometchat.uikit.compose.preview.data.repository.PreviewGroupsRepository
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.preview.presentation.viewmodels.PreviewGroupsViewModelFactory
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Helper to create a preview ViewModel with mock data using the factory pattern.
 */
@Composable
private fun rememberPreviewGroupsViewModel(
    groups: List<Group> = PreviewMockData.createSampleGroups(),
    simulateError: Boolean = false,
    simulateEmpty: Boolean = false
): CometChatGroupsViewModel {
    val factory = remember(groups, simulateError, simulateEmpty) {
        CometChatGroupsViewModelFactory(
            repository = PreviewGroupsRepository(
                initialGroups = groups,
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
@Preview(showBackground = true, name = "Groups - State - Loading")
@Composable
fun PreviewGroupsLoading() {
    CometChatTheme {
        val style = CometChatGroupsStyle.default()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            CometChatToolbar(
                title = "Groups",
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
@Preview(showBackground = true, name = "Groups - State - Empty")
@Composable
fun PreviewGroupsEmpty() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(simulateEmpty = true)
        CometChatGroups(
            viewModel = viewModel,
            title = "Groups",
            hideBackIcon = true
        )
    }
}

/**
 * Preview showing the error state.
 */
@Preview(showBackground = true, name = "Groups - State - Error")
@Composable
fun PreviewGroupsError() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(simulateError = true)
        CometChatGroups(
            viewModel = viewModel,
            title = "Groups",
            hideBackIcon = true
        )
    }
}

/**
 * Preview showing the content state with groups.
 */
@Preview(showBackground = true, name = "Groups - State - Content")
@Composable
fun PreviewGroupsContent() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel()
        CometChatGroups(
            viewModel = viewModel,
            title = "Groups",
            hideBackIcon = true
        )
    }
}

// ============================================================================
// SECTION 2: GROUP TYPE PREVIEWS
// ============================================================================

/**
 * Preview with only public groups.
 */
@Preview(showBackground = true, name = "Groups - Type - Public Only")
@Composable
fun PreviewGroupsPublicOnly() {
    CometChatTheme {
        val viewModel = remember { PreviewGroupsViewModelFactory.createPublicGroupsViewModel() }
        CometChatGroups(
            viewModel = viewModel,
            title = "Public Groups",
            hideBackIcon = true
        )
    }
}

/**
 * Preview with only private groups.
 */
@Preview(showBackground = true, name = "Groups - Type - Private Only")
@Composable
fun PreviewGroupsPrivateOnly() {
    CometChatTheme {
        val viewModel = remember { PreviewGroupsViewModelFactory.createPrivateGroupsViewModel() }
        CometChatGroups(
            viewModel = viewModel,
            title = "Private Groups",
            hideBackIcon = true
        )
    }
}

/**
 * Preview with only password-protected groups.
 */
@Preview(showBackground = true, name = "Groups - Type - Password Only")
@Composable
fun PreviewGroupsPasswordOnly() {
    CometChatTheme {
        val viewModel = remember { PreviewGroupsViewModelFactory.createPasswordGroupsViewModel() }
        CometChatGroups(
            viewModel = viewModel,
            title = "Protected Groups",
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
@Preview(showBackground = true, name = "Groups - Selection - Single")
@Composable
fun PreviewGroupsSingleSelection() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(4)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "Single Selection",
            hideBackIcon = true,
            selectionMode = UIKitConstants.SelectionMode.SINGLE
        )
    }
}

/**
 * Preview showing multiple selection mode.
 */
@Preview(showBackground = true, name = "Groups - Selection - Multiple")
@Composable
fun PreviewGroupsMultipleSelection() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(4)
        )
        CometChatGroups(
            viewModel = viewModel,
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
@Preview(showBackground = true, name = "Groups - Visibility - No Toolbar")
@Composable
fun PreviewGroupsNoToolbar() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
            hideToolbar = true
        )
    }
}

/**
 * Preview without search box.
 */
@Preview(showBackground = true, name = "Groups - Visibility - No Search Box")
@Composable
fun PreviewGroupsNoSearchBox() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "No Search",
            hideBackIcon = true,
            hideSearchBox = true
        )
    }
}

/**
 * Preview without group type indicators.
 */
@Preview(showBackground = true, name = "Groups - Visibility - No Group Type")
@Composable
fun PreviewGroupsNoGroupType() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel()
        CometChatGroups(
            viewModel = viewModel,
            title = "Type Hidden",
            hideBackIcon = true,
            hideGroupType = true
        )
    }
}

/**
 * Preview without separators.
 */
@Preview(showBackground = true, name = "Groups - Visibility - No Separators")
@Composable
fun PreviewGroupsNoSeparators() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(4)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "No Separators",
            hideBackIcon = true,
            hideSeparator = true
        )
    }
}

/**
 * Preview with back button visible.
 */
@Preview(showBackground = true, name = "Groups - Visibility - With Back Button")
@Composable
fun PreviewGroupsWithBackButton() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
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
@Preview(showBackground = true, name = "Groups - Custom View - Empty")
@Composable
fun PreviewGroupsCustomEmptyView() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(simulateEmpty = true)
        CometChatGroups(
            viewModel = viewModel,
            title = "Custom Empty",
            hideBackIcon = true,
            emptyView = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "👥",
                            style = CometChatTheme.typography.heading1Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No groups yet",
                            style = CometChatTheme.typography.heading3Medium,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create or join a group to get started",
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
@Preview(showBackground = true, name = "Groups - Custom View - Leading")
@Composable
fun PreviewGroupsCustomLeadingView() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "Custom Leading",
            hideBackIcon = true,
            leadingView = { group ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CometChatTheme.colorScheme.infoColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.name?.take(2)?.uppercase() ?: "??",
                        style = CometChatTheme.typography.caption1Bold,
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
@Preview(showBackground = true, name = "Groups - Custom View - Title")
@Composable
fun PreviewGroupsCustomTitleView() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "Custom Title",
            hideBackIcon = true,
            titleView = { group ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CometChatTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = group.name ?: "Unknown",
                        style = CometChatTheme.typography.heading4Bold,
                        color = CometChatTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

/**
 * Preview with custom subtitle view showing member count.
 */
@Preview(showBackground = true, name = "Groups - Custom View - Subtitle")
@Composable
fun PreviewGroupsCustomSubtitleView() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "Custom Subtitle",
            hideBackIcon = true,
            subtitleView = { group ->
                Text(
                    text = "${group.membersCount} members • ${group.groupType}",
                    style = CometChatTheme.typography.caption1Regular,
                    color = CometChatTheme.colorScheme.textColorSecondary
                )
            }
        )
    }
}

/**
 * Preview with custom trailing view.
 */
@Preview(showBackground = true, name = "Groups - Custom View - Trailing")
@Composable
fun PreviewGroupsCustomTrailingView() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "Custom Trailing",
            hideBackIcon = true,
            trailingView = { group ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${group.membersCount}",
                        style = CometChatTheme.typography.heading4Bold,
                        color = CometChatTheme.colorScheme.primary
                    )
                    Text(
                        text = "members",
                        style = CometChatTheme.typography.caption2Regular,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
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
@Preview(showBackground = true, name = "Groups - Toolbar - Custom Title")
@Composable
fun PreviewGroupsCustomTitle() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(2)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "My Communities",
            hideBackIcon = true
        )
    }
}

/**
 * Preview with custom overflow menu.
 */
@Preview(showBackground = true, name = "Groups - Toolbar - Overflow Menu")
@Composable
fun PreviewGroupsOverflowMenu() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(2)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "Groups",
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
@Preview(showBackground = true, name = "Groups - Toolbar - Custom Search Placeholder")
@Composable
fun PreviewGroupsCustomSearchPlaceholder() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(2)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "Groups",
            hideBackIcon = true,
            searchPlaceholderText = "Find groups..."
        )
    }
}

// ============================================================================
// SECTION 7: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "Groups - Style - Custom Background")
@Composable
fun PreviewGroupsCustomBackground() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "Custom Background",
            hideBackIcon = true,
            style = CometChatGroupsStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            )
        )
    }
}

/**
 * Preview with custom title color.
 */
@Preview(showBackground = true, name = "Groups - Style - Custom Title Color")
@Composable
fun PreviewGroupsCustomTitleColor() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
            title = "Custom Title Color",
            hideBackIcon = true,
            style = CometChatGroupsStyle.default(
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
@Preview(showBackground = true, name = "Groups - Comprehensive - All Features")
@Composable
fun PreviewGroupsComprehensive() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel()
        CometChatGroups(
            viewModel = viewModel,
            title = "All Features",
            hideBackIcon = false,
            searchPlaceholderText = "Search groups...",
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
@Preview(showBackground = true, name = "Groups - Comprehensive - Minimal")
@Composable
fun PreviewGroupsMinimal() {
    CometChatTheme {
        val viewModel = rememberPreviewGroupsViewModel(
            groups = PreviewMockData.createSampleGroups().take(3)
        )
        CometChatGroups(
            viewModel = viewModel,
            hideToolbar = true,
            hideSearchBox = true,
            hideSeparator = true
        )
    }
}

/**
 * Preview showing large list for scroll testing.
 */
@Preview(showBackground = true, name = "Groups - Comprehensive - Large List")
@Composable
fun PreviewGroupsLargeList() {
    CometChatTheme {
        val viewModel = remember { PreviewGroupsViewModelFactory.createLargeListViewModel(20) }
        CometChatGroups(
            viewModel = viewModel,
            title = "Large List (20 groups)",
            hideBackIcon = true
        )
    }
}
