package com.cometchat.uikit.compose.presentation.groups.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.groups.style.CometChatGroupsStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.presentation.shared.searchbox.CometChatSearchBox
import com.cometchat.uikit.compose.presentation.shared.searchbox.CometChatSearchBoxStyle
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatGroupsViewModelFactory
import com.cometchat.uikit.core.state.GroupsUIState
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import kotlinx.coroutines.delay

/**
 * CometChatGroups displays a list of groups with support for
 * real-time updates, selection modes, custom views, and full styling customization.
 *
 * @param modifier Modifier applied to the parent container
 * @param viewModel The ViewModel managing groups state (optional, creates default if not provided)
 * @param groupsRequestBuilder Custom request builder for fetching groups
 * @param searchRequestBuilder Custom request builder for searching groups
 * @param style Style configuration for the component
 * @param title Toolbar title text
 * @param hideToolbar Whether to hide the entire toolbar
 * @param hideBackIcon Whether to hide the back navigation icon
 * @param hideTitle Whether to hide the toolbar title (independent of selection mode)
 * @param hideToolbarSeparator Whether to hide the toolbar separator line
 * @param overflowMenu Optional custom overflow menu composable
 * @param hideSearchBox Whether to hide the search box
 * @param searchPlaceholderText Placeholder text for search box
 * @param searchKeyword Initial search keyword to pre-populate and trigger search
 * @param selectionMode Selection mode (NONE, SINGLE, MULTIPLE)
 * @param initialSelectedGroups Groups to pre-select on initial composition
 * @param hideGroupType Whether to hide group type indicators
 * @param hideSeparator Whether to hide item separators
 * @param hideLoadingState Whether to hide loading state
 * @param hideEmptyState Whether to hide empty state
 * @param hideErrorState Whether to hide error state
 * @param loadingView Custom loading state composable
 * @param emptyView Custom empty state composable
 * @param errorView Custom error state composable with retry callback
 * @param itemView Custom item composable replacing entire item
 * @param leadingView Custom leading section composable
 * @param titleView Custom title section composable
 * @param subtitleView Custom subtitle section composable
 * @param trailingView Custom trailing section composable
 * @param passwordGroupIcon Custom password group indicator composable
 * @param privateGroupIcon Custom private group indicator composable
 * @param options Function to replace all menu options
 * @param addOptions Function to add options to default menu
 * @param onItemClick Callback for item clicks
 * @param onItemLongClick Callback for item long-clicks
 * @param onError Callback for errors
 * @param onLoad Callback when groups are loaded
 * @param onEmpty Callback when list is empty
 * @param onBackPress Callback for back navigation
 * @param onSelection Callback for selection completion
 * @param onSearchChange Callback when search query changes (after debounce)
 */
@Composable
fun CometChatGroups(
    modifier: Modifier = Modifier,
    viewModel: CometChatGroupsViewModel? = null,
    groupsRequestBuilder: GroupsRequest.GroupsRequestBuilder? = null,
    searchRequestBuilder: GroupsRequest.GroupsRequestBuilder? = null,
    style: CometChatGroupsStyle = CometChatGroupsStyle.default(),
    // Toolbar configuration
    title: String? = null,
    hideToolbar: Boolean = false,
    hideBackIcon: Boolean = true,
    hideTitle: Boolean = false,
    hideToolbarSeparator: Boolean = false,
    overflowMenu: (@Composable () -> Unit)? = null,
    // Search configuration
    hideSearchBox: Boolean = false,
    searchPlaceholderText: String? = null,
    searchKeyword: String? = null,
    // Selection mode
    selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
    initialSelectedGroups: List<Group>? = null,
    // Visibility controls
    hideGroupType: Boolean = false,
    hideSeparator: Boolean = false,
    hideLoadingState: Boolean = false,
    hideEmptyState: Boolean = false,
    hideErrorState: Boolean = false,
    // Custom views
    loadingView: (@Composable () -> Unit)? = null,
    emptyView: (@Composable () -> Unit)? = null,
    errorView: (@Composable (onRetry: () -> Unit) -> Unit)? = null,
    itemView: (@Composable (Group) -> Unit)? = null,
    leadingView: (@Composable (Group) -> Unit)? = null,
    titleView: (@Composable (Group) -> Unit)? = null,
    subtitleView: (@Composable (Group) -> Unit)? = null,
    trailingView: (@Composable (Group) -> Unit)? = null,
    passwordGroupIcon: (@Composable () -> Unit)? = null,
    privateGroupIcon: (@Composable () -> Unit)? = null,
    // Menu options
    options: ((Context, Group) -> List<MenuItem>)? = null,
    addOptions: ((Context, Group) -> List<MenuItem>)? = null,
    // Callbacks
    onItemClick: ((Group) -> Unit)? = null,
    onItemLongClick: ((Group) -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null,
    onLoad: ((List<Group>) -> Unit)? = null,
    onEmpty: (() -> Unit)? = null,
    onBackPress: (() -> Unit)? = null,
    onSelection: ((List<Group>) -> Unit)? = null,
    onSearchChange: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    // Use localized strings with fallback to provided values
    val localizedTitle = title ?: context.getString(R.string.cometchat_groups)
    val localizedSearchPlaceholder = searchPlaceholderText ?: context.getString(R.string.cometchat_search)

    // Create a new ViewModel instance each time the composable is mounted
    // This ensures fresh state when navigating back to this screen
    // Matches Java v5 behavior: ViewModelProvider.NewInstanceFactory().create()
    val groupsViewModel = viewModel ?: remember {
        CometChatGroupsViewModelFactory().create(CometChatGroupsViewModel::class.java)
    }

    // Apply custom request builders if provided
    LaunchedEffect(groupsRequestBuilder) {
        groupsRequestBuilder?.let { groupsViewModel.setGroupsRequestBuilder(it) }
    }

    LaunchedEffect(searchRequestBuilder) {
        searchRequestBuilder?.let { groupsViewModel.setSearchRequestBuilder(it) }
    }

    // Collect state from ViewModel
    val uiState by groupsViewModel.uiState.collectAsState()
    val groups by groupsViewModel.groups.collectAsState()
    val selectedGroups by groupsViewModel.selectedGroups.collectAsState()

    // Search state with debounce
    var searchQuery by remember { mutableStateOf(searchKeyword ?: "") }
    var hasSearched by remember { mutableStateOf(searchKeyword?.isNotEmpty() == true) }

    // Handle searchKeyword parameter changes
    LaunchedEffect(searchKeyword) {
        searchKeyword?.let {
            if (it != searchQuery) {
                searchQuery = it
                if (it.isNotEmpty()) {
                    groupsViewModel.searchGroups(it)
                    hasSearched = true
                }
            }
        }
    }

    // Handle initial selected groups
    LaunchedEffect(initialSelectedGroups, selectionMode) {
        if (selectionMode != UIKitConstants.SelectionMode.NONE && initialSelectedGroups != null) {
            if (selectionMode == UIKitConstants.SelectionMode.SINGLE && initialSelectedGroups.isNotEmpty()) {
                // In SINGLE mode, only select the first group
                groupsViewModel.selectGroup(initialSelectedGroups.first(), selectionMode)
            } else {
                // In MULTIPLE mode, select all provided groups
                initialSelectedGroups.forEach { group ->
                    groupsViewModel.selectGroup(group, selectionMode)
                }
            }
        }
    }

    // Debounced search - only trigger after user interaction
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(300) // 300ms debounce
            groupsViewModel.searchGroups(searchQuery)
            hasSearched = true
            onSearchChange?.invoke(searchQuery)
        } else if (hasSearched) {
            // Only refresh if user cleared a previous search
            delay(300)
            groupsViewModel.refreshList()
            hasSearched = false
            onSearchChange?.invoke("")
        }
        // Don't refresh on initial empty state
    }

    // Handle state callbacks
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GroupsUIState.Content -> onLoad?.invoke(state.groups)
            is GroupsUIState.Empty -> onEmpty?.invoke()
            is GroupsUIState.Error -> onError?.invoke(state.exception)
            is GroupsUIState.Loading -> { /* No callback for loading */ }
        }
    }

    // Create search box style from groups style
    val searchBoxStyle = CometChatSearchBoxStyle.default(
        backgroundColor = style.searchBackgroundColor,
        textColor = style.searchTextColor,
        textStyle = style.searchTextStyle,
        placeholderColor = style.searchPlaceholderColor,
        placeholderTextStyle = style.searchPlaceholderTextStyle,
        startIcon = style.searchStartIcon,
        startIconTint = style.searchStartIconTint,
        endIcon = style.searchEndIcon,
        endIconTint = style.searchEndIconTint,
        cornerRadius = style.searchCornerRadius,
        strokeWidth = style.searchStrokeWidth,
        strokeColor = style.searchStrokeColor
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(style.backgroundColor)
    ) {
        // Toolbar
        if (!hideToolbar) {
            GroupsToolbar(
                title = localizedTitle,
                style = style,
                hideBackIcon = hideBackIcon,
                hideTitle = hideTitle,
                hideToolbarSeparator = hideToolbarSeparator,
                selectionMode = selectionMode,
                selectedCount = selectedGroups.size,
                overflowMenu = overflowMenu,
                onBackPress = onBackPress,
                onDiscardSelection = { groupsViewModel.clearSelection() },
                onSubmitSelection = { onSelection?.invoke(groupsViewModel.getSelectedGroups()) }
            )
        }

        // Search Box
        if (!hideSearchBox) {
            CometChatSearchBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = searchBoxStyle,
                text = searchQuery,
                onTextChange = { searchQuery = it },
                placeholderText = localizedSearchPlaceholder,
                enabled = true
            )
        }

        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (uiState) {
                is GroupsUIState.Loading -> {
                    if (!hideLoadingState) {
                        loadingView?.invoke() ?: CometChatLoadingState(style = style.loadingStateStyle)
                    }
                }

                is GroupsUIState.Empty -> {
                    if (!hideEmptyState) {
                        emptyView?.invoke() ?: CometChatEmptyState(
                            style = style.emptyStateStyle,
                            title = context.getString(R.string.cometchat_group_list_empty_state_title),
                            subtitle = context.getString(R.string.cometchat_group_list_empty_state_subtitle)
                        )
                    }
                }

                is GroupsUIState.Error -> {
                    if (!hideErrorState) {
                        errorView?.invoke { groupsViewModel.fetchGroups() }
                            ?: CometChatErrorState(
                                style = style.errorStateStyle,
                                title = context.getString(R.string.cometchat_group_list_error_state_title),
                                subtitle = context.getString(R.string.cometchat_something_went_wrong_please_try_again),
                                onRetry = { groupsViewModel.fetchGroups() }
                            )
                    }
                }

                is GroupsUIState.Content -> {
                    GroupsListContent(
                        groups = groups,
                        selectedGroups = selectedGroups,
                        selectionMode = selectionMode,
                        style = style,
                        hideGroupType = hideGroupType,
                        hideSeparator = hideSeparator,
                        itemView = itemView,
                        leadingView = leadingView,
                        titleView = titleView,
                        subtitleView = subtitleView,
                        trailingView = trailingView,
                        options = options,
                        addOptions = addOptions,
                        onItemClick = { group ->
                            if (selectionMode != UIKitConstants.SelectionMode.NONE) {
                                groupsViewModel.selectGroup(group, selectionMode)
                            } else {
                                onItemClick?.invoke(group)
                            }
                        },
                        onItemLongClick = { group ->
                            onItemLongClick?.invoke(group)
                        },
                        onLoadMore = { groupsViewModel.fetchGroups() },
                        scrollToTopEvent = groupsViewModel.scrollToTopEvent
                    )
                }
            }
        }
    }
}


/**
 * Internal composable for the groups list toolbar.
 * Handles both normal mode and selection mode display.
 */
@Composable
private fun GroupsToolbar(
    title: String,
    style: CometChatGroupsStyle,
    hideBackIcon: Boolean,
    hideTitle: Boolean,
    hideToolbarSeparator: Boolean,
    selectionMode: UIKitConstants.SelectionMode,
    selectedCount: Int,
    overflowMenu: (@Composable () -> Unit)?,
    onBackPress: (() -> Unit)?,
    onDiscardSelection: () -> Unit,
    onSubmitSelection: () -> Unit
) {
    val context = LocalContext.current
    val isInSelectionMode = selectionMode != UIKitConstants.SelectionMode.NONE && selectedCount > 0

    val toolbarStyle = CometChatToolbarStyle.default(
        backgroundColor = style.backgroundColor,
        titleTextColor = style.titleTextColor,
        titleTextStyle = style.titleTextStyle,
        navigationIcon = style.backIcon,
        navigationIconTint = style.backIconTint,
        separatorColor = style.toolbarSeparatorColor,
        separatorHeight = style.toolbarSeparatorHeight,
        showSeparator = !hideToolbarSeparator && style.showToolbarSeparator
    )

    if (isInSelectionMode) {
        // Selection mode toolbar - show just the count number (matches Java implementation)
        CometChatToolbar(
            title = selectedCount.toString(),
            style = toolbarStyle.copy(
                titleTextColor = style.selectionCountTextColor,
                titleTextStyle = style.selectionCountTextStyle
            ),
            hideBackIcon = false,
            navigationIcon = style.discardSelectionIcon,
            navigationContentDescription = context.getString(R.string.cometchat_discard_selection),
            onNavigationClick = onDiscardSelection,
            actions = {
                // Submit selection button
                style.submitSelectionIcon?.let { icon ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .focusable()
                            .clickable { onSubmitSelection() }
                            .semantics {
                                contentDescription = context.getString(
                                    R.string.cometchat_submit_selection_description,
                                    selectedCount
                                )
                                role = Role.Button
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = style.submitSelectionIconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        )
    } else {
        // Normal mode toolbar
        CometChatToolbar(
            title = if (hideTitle) "" else title,
            style = toolbarStyle,
            hideBackIcon = hideBackIcon,
            navigationContentDescription = context.getString(R.string.cometchat_go_back),
            onNavigationClick = onBackPress,
            actions = if (overflowMenu != null) {
                { overflowMenu() }
            } else null
        )
    }
}


/**
 * Internal composable for the groups list content.
 * Uses LazyColumn with pagination support.
 */
@Composable
private fun GroupsListContent(
    groups: List<Group>,
    selectedGroups: Set<Group>,
    selectionMode: UIKitConstants.SelectionMode,
    style: CometChatGroupsStyle,
    hideGroupType: Boolean,
    hideSeparator: Boolean,
    itemView: (@Composable (Group) -> Unit)?,
    leadingView: (@Composable (Group) -> Unit)?,
    titleView: (@Composable (Group) -> Unit)?,
    subtitleView: (@Composable (Group) -> Unit)?,
    trailingView: (@Composable (Group) -> Unit)?,
    options: ((Context, Group) -> List<MenuItem>)?,
    addOptions: ((Context, Group) -> List<MenuItem>)?,
    onItemClick: (Group) -> Unit,
    onItemLongClick: (Group) -> Unit,
    onLoadMore: () -> Unit,
    scrollToTopEvent: kotlinx.coroutines.flow.SharedFlow<Unit>? = null
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Detect when we need to load more items
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= groups.size - 5
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && groups.isNotEmpty()) {
            onLoadMore()
        }
    }

    // Auto-scroll to top when new group is added (if first 3 items are visible)
    LaunchedEffect(scrollToTopEvent) {
        scrollToTopEvent?.collect {
            if (listState.firstVisibleItemIndex < 3) {
                listState.animateScrollToItem(0)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor)
            .semantics {
                contentDescription = context.getString(
                    R.string.cometchat_groups_list_description,
                    groups.size
                )
                collectionInfo = CollectionInfo(
                    rowCount = groups.size,
                    columnCount = 1
                )
            }
    ) {
        itemsIndexed(
            items = groups,
            key = { _, group -> group.guid }
        ) { index, group ->
            val isSelected = selectedGroups.any { it.guid == group.guid }

            // Local state for popup menu for this item
            var showPopupMenu by remember { mutableStateOf(false) }

            // Build menu items for this group
            val menuItems = buildGroupMenuItems(
                context = context,
                group = group,
                style = style,
                options = options,
                addOptions = addOptions
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (itemView != null) {
                    // Use custom item view
                    itemView(group)
                } else {
                    // Use default CometChatGroupsItem
                    CometChatGroupsItem(
                        group = group,
                        onItemClick = { onItemClick(group) },
                        onItemLongClick = {
                            if (selectionMode == UIKitConstants.SelectionMode.NONE && menuItems.isNotEmpty()) {
                                showPopupMenu = true
                            }
                            onItemLongClick(group)
                        },
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        hideGroupType = hideGroupType,
                        hideSeparator = hideSeparator || index == groups.size - 1,
                        style = style.itemStyle,
                        leadingView = leadingView,
                        titleView = titleView,
                        subtitleView = subtitleView,
                        trailingView = trailingView
                    )
                }

                // Popup menu anchored to the right side of the item
                if (menuItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 32.dp)
                    ) {
                        CometChatPopupMenu(
                            expanded = showPopupMenu,
                            onDismissRequest = { showPopupMenu = false },
                            menuItems = menuItems,
                            style = style.popupMenuStyle,
                            offset = DpOffset(0.dp, 32.dp),
                            onMenuItemClick = { _, _ -> showPopupMenu = false }
                        ) {
                            // Empty anchor - the popup will appear at this position
                        }
                    }
                }
            }
        }
    }
}

/**
 * Builds the menu items for the popup menu.
 * Supports custom options and additional options.
 */
@Composable
private fun buildGroupMenuItems(
    context: Context,
    group: Group,
    style: CometChatGroupsStyle,
    options: ((Context, Group) -> List<MenuItem>)?,
    addOptions: ((Context, Group) -> List<MenuItem>)?
): List<MenuItem> {
    // If custom options are provided, use them exclusively
    if (options != null) {
        return options(context, group)
    }

    val menuItems = mutableListOf<MenuItem>()

    // Add additional options if provided
    if (addOptions != null) {
        menuItems.addAll(addOptions(context, group))
    }

    return menuItems
}
