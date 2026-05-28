package com.cometchat.uikit.compose.presentation.notificationfeed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.core.NotificationCategoriesRequest
import com.cometchat.chat.core.NotificationFeedRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.NotificationFeedItem
import com.cometchat.uikit.compose.presentation.notificationfeed.style.CometChatNotificationFeedStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.factory.CometChatNotificationFeedViewModelFactory
import com.cometchat.uikit.core.state.NotificationFeedUIState
import com.cometchat.uikit.core.viewmodel.CometChatNotificationFeedViewModel

/**
 * CometChatNotificationFeed displays a full-screen notification feed with
 * category-based filtering, timestamp grouping, rich card rendering,
 * real-time updates, and engagement reporting.
 *
 * @param modifier Modifier applied to the root container
 * @param viewModel The ViewModel managing feed state (optional, creates default if not provided)
 * @param title Header title text (default: "Notifications")
 * @param showHeader Whether to show the header bar
 * @param showBackButton Whether to show the back navigation button
 * @param showFilterChips Whether to show the category filter chips row
 * @param scrollToItemId Optional item ID for deep link (shows item in dialog)
 * @param style Style configuration for the component
 * @param feedRequestBuilder Optional custom request builder for feed items
 * @param categoriesRequestBuilder Optional custom request builder for categories
 * @param headerView Optional custom composable to replace the entire header
 * @param emptyStateView Optional custom composable for empty state
 * @param errorStateView Optional custom composable for error state
 * @param loadingStateView Optional custom composable for loading state
 * @param onItemClick Callback when a feed item card is tapped
 * @param onActionClick Callback when an action button within a card is tapped
 * @param onError Callback when an error occurs
 * @param onBackPress Callback for back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CometChatNotificationFeed(
    modifier: Modifier = Modifier,
    feedRequestBuilder: NotificationFeedRequest.NotificationFeedRequestBuilder? = null,
    categoriesRequestBuilder: NotificationCategoriesRequest.NotificationCategoriesRequestBuilder? = null,
    viewModel: CometChatNotificationFeedViewModel = viewModel(
        factory = CometChatNotificationFeedViewModelFactory(
            feedRequestBuilder = feedRequestBuilder,
            categoriesRequestBuilder = categoriesRequestBuilder
        )
    ),
    title: String = "Notifications",
    showHeader: Boolean = true,
    showBackButton: Boolean = false,
    showFilterChips: Boolean = true,
    style: CometChatNotificationFeedStyle = CometChatNotificationFeedStyle(),
    headerView: (@Composable () -> Unit)? = null,
    emptyStateView: (@Composable () -> Unit)? = null,
    errorStateView: (@Composable (CometChatException, () -> Unit) -> Unit)? = null,
    loadingStateView: (@Composable () -> Unit)? = null,
    onItemClick: ((NotificationFeedItem) -> Unit)? = null,
    onActionClick: ((NotificationFeedItem, Map<String, Any>) -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null,
    onBackPress: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterChips by viewModel.filterChips.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMorePages by viewModel.hasMorePages.collectAsState()
    val activeCategory by viewModel.activeCategory.collectAsState()
    val isPaginationError by viewModel.isPaginationError.collectAsState()

    // Resolve style from theme
    val resolvedStyle = CometChatNotificationFeedStyle.fromTheme(style)

    // Report errors via callback
    LaunchedEffect(uiState) {
        if (uiState is NotificationFeedUIState.Error) {
            onError?.invoke((uiState as NotificationFeedUIState.Error).exception)
        }
    }

    val backgroundColor = resolvedStyle.backgroundColor

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(resolvedStyle.headerBackgroundColor)
            .testTag("notification-feed-screen")
    ) {
        // Header
        if (showHeader) {
            if (headerView != null) {
                headerView()
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            color = resolvedStyle.headerTitleColor,
                            style = CometChatTheme.typography.heading1Bold,
                            modifier = Modifier.semantics {
                                contentDescription = title
                            }
                        )
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(
                                onClick = { onBackPress?.invoke() },
                                modifier = Modifier.testTag("notification-feed-back-button")
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                    contentDescription = "Back",
                                    tint = CometChatTheme.colorScheme.iconTintPrimary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = resolvedStyle.headerBackgroundColor,
                        scrolledContainerColor = resolvedStyle.headerBackgroundColor
                    ),
                    windowInsets = WindowInsets(0)
                )
                HorizontalDivider(
                    color = resolvedStyle.headerBorderColor,
                    thickness = 1.dp
                )
            }
        }

        // Content below header with gray background
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Filter Chips Row
            if (showFilterChips && filterChips.isNotEmpty()) {
                NotificationFeedFilterChips(
                    chips = filterChips,
                    style = resolvedStyle,
                    onChipClick = { chipId ->
                        val categoryId = if (chipId == "all") null else chipId
                        viewModel.switchCategory(categoryId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Content Area
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is NotificationFeedUIState.Loading -> {
                        if (loadingStateView != null) {
                            loadingStateView()
                        } else {
                            NotificationFeedLoadingState(
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    is NotificationFeedUIState.Empty -> {
                        if (emptyStateView != null) {
                            emptyStateView()
                        } else {
                            NotificationFeedEmptyState(
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    is NotificationFeedUIState.Error -> {
                        if (errorStateView != null) {
                            errorStateView(state.exception) {
                                viewModel.fetchInitialItems()
                            }
                        } else {
                            NotificationFeedErrorState(
                                exception = state.exception,
                                onRetry = { viewModel.fetchInitialItems() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    is NotificationFeedUIState.Content -> {
                        NotificationFeedList(
                            groupedItems = state.groupedItems,
                            isLoadingMore = isLoadingMore,
                            hasMorePages = hasMorePages,
                            isPaginationError = isPaginationError,
                            style = resolvedStyle,
                            activeCategory = activeCategory,
                            onLoadMore = { viewModel.fetchNextPage() },
                            onRetryPagination = { viewModel.fetchNextPage() },
                            onItemVisible = { item -> viewModel.onItemBecameVisible(item) },
                            onItemHidden = { item -> viewModel.onItemBecameHidden(item) },
                            onItemClick = { item ->
                                onItemClick?.invoke(item)
                            },
                            onActionClick = { item, action ->
                                onActionClick?.invoke(item, action)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
