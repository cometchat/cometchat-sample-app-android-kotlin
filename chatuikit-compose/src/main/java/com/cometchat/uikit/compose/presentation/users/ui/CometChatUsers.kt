package com.cometchat.uikit.compose.presentation.users.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.presentation.shared.searchbox.CometChatSearchBox
import com.cometchat.uikit.compose.presentation.shared.searchbox.CometChatSearchBoxStyle
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.compose.presentation.users.style.CometChatUsersStyle
import com.cometchat.uikit.compose.presentation.users.utils.UsersUtils
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatUsersViewModelFactory
import com.cometchat.uikit.core.state.UsersUIState
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * CometChatUsers displays a list of users with support for
 * real-time updates, selection modes, custom views, and full styling customization.
 *
 * @param modifier Modifier applied to the parent container
 * @param usersViewModel The ViewModel managing users state (optional, creates default if not provided)
 * @param usersRequestBuilder Custom request builder for fetching users (optional)
 * @param searchRequestBuilder Custom request builder for search operations (optional)
 * @param searchKeyword Programmatically set search keyword (optional)
 * @param style Style configuration for the component
 * @param title Toolbar title text
 * @param hideToolbar Whether to hide the entire toolbar
 * @param hideBackIcon Whether to hide the back navigation icon
 * @param overflowMenu Optional custom overflow menu composable
 * @param hideSearchBox Whether to hide the search box
 * @param searchPlaceholderText Placeholder text for search box
 * @param selectionMode Selection mode (NONE, SINGLE, MULTIPLE)
 * @param hideStatusIndicator Whether to hide user online/offline indicators
 * @param hideStickyHeader Whether to hide sticky alphabetical headers
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
 * @param options Function to replace all menu options
 * @param addOptions Function to add options to default menu
 * @param onItemClick Callback for item clicks
 * @param onItemLongClick Callback for item long-clicks
 * @param onError Callback for errors
 * @param onLoad Callback when users are loaded
 * @param onEmpty Callback when list is empty
 * @param onBackPress Callback for back navigation
 * @param onSelection Callback for selection completion
 */
@Composable
fun CometChatUsers(
    modifier: Modifier = Modifier,
    usersViewModel: CometChatUsersViewModel? = null,
    // Request builder customization
    usersRequestBuilder: UsersRequest.UsersRequestBuilder? = null,
    searchRequestBuilder: UsersRequest.UsersRequestBuilder? = null,
    // Programmatic search
    searchKeyword: String? = null,
    style: CometChatUsersStyle = CometChatUsersStyle.default(),
    // Toolbar configuration
    title: String? = null,
    hideToolbar: Boolean = false,
    hideBackIcon: Boolean = true,
    overflowMenu: (@Composable () -> Unit)? = null,
    // Search configuration
    hideSearchBox: Boolean = false,
    searchPlaceholderText: String? = null,
    // Selection mode
    selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
    // Visibility controls
    hideStatusIndicator: Boolean = false,
    hideStickyHeader: Boolean = false,
    hideSeparator: Boolean = false,
    hideLoadingState: Boolean = false,
    hideEmptyState: Boolean = false,
    hideErrorState: Boolean = false,
    // Custom views
    loadingView: (@Composable () -> Unit)? = null,
    emptyView: (@Composable () -> Unit)? = null,
    errorView: (@Composable (onRetry: () -> Unit) -> Unit)? = null,
    itemView: (@Composable (User) -> Unit)? = null,
    leadingView: (@Composable (User) -> Unit)? = null,
    titleView: (@Composable (User) -> Unit)? = null,
    subtitleView: (@Composable (User) -> Unit)? = null,
    trailingView: (@Composable (User) -> Unit)? = null,
    // Menu options
    options: ((Context, User) -> List<MenuItem>)? = null,
    addOptions: ((Context, User) -> List<MenuItem>)? = null,
    // Callbacks
    onItemClick: ((User) -> Unit)? = null,
    onItemLongClick: ((User) -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null,
    onLoad: ((List<User>) -> Unit)? = null,
    onEmpty: (() -> Unit)? = null,
    onBackPress: (() -> Unit)? = null,
    onSelection: ((List<User>) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Use localized strings with fallback to provided values
    val localizedTitle = title ?: context.getString(R.string.cometchat_users)
    val localizedSearchPlaceholder = searchPlaceholderText ?: context.getString(R.string.cometchat_search)
    
    // Create a new ViewModel instance each time the composable is mounted
    // This ensures fresh state when navigating back to this screen (e.g., tab switch)
    // Matches Java v5 behavior: ViewModelProvider.NewInstanceFactory().create()
    val viewModel = usersViewModel ?: remember {
        CometChatUsersViewModelFactory().create(CometChatUsersViewModel::class.java)
    }
    
    // Apply users request builder when it changes
    LaunchedEffect(usersRequestBuilder) {
        usersRequestBuilder?.let { builder ->
            viewModel.setUsersRequestBuilder(builder)
            viewModel.refreshList()
        }
    }
    
    // Apply search request builder when it changes
    LaunchedEffect(searchRequestBuilder) {
        searchRequestBuilder?.let { builder ->
            viewModel.setSearchRequestBuilder(builder)
        }
    }
    
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val users by viewModel.users.collectAsState()
    val selectedUsers by viewModel.selectedUsers.collectAsState()
    
    // Search state
    var searchQuery by remember { mutableStateOf(searchKeyword ?: "") }
    
    // Handle programmatic search keyword changes
    LaunchedEffect(searchKeyword) {
        if (searchKeyword != null) {
            searchQuery = searchKeyword
            viewModel.searchUsers(searchKeyword.ifEmpty { null })
        }
    }
    
    // Popup menu state
    var showPopupMenu by remember { mutableStateOf(false) }
    var popupMenuUser by remember { mutableStateOf<User?>(null) }
    
    // Handle state callbacks
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UsersUIState.Content -> onLoad?.invoke(users)
            is UsersUIState.Empty -> onEmpty?.invoke()
            is UsersUIState.Error -> onError?.invoke(state.exception)
            is UsersUIState.Loading -> { /* No callback for loading */ }
        }
    }
    
    // Create search box style from users style
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
            UsersToolbar(
                title = localizedTitle,
                style = style,
                hideBackIcon = hideBackIcon,
                selectionMode = selectionMode,
                selectedCount = selectedUsers.size,
                overflowMenu = overflowMenu,
                onBackPress = onBackPress,
                onDiscardSelection = { viewModel.clearSelection() },
                onSubmitSelection = { onSelection?.invoke(viewModel.getSelectedUsers()) }
            )
        }
        
        // Search Box
        if (!hideSearchBox) {
            CometChatSearchBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = searchBoxStyle,
                placeholderText = localizedSearchPlaceholder,
                text = searchQuery,
                onTextChange = { query ->
                    searchQuery = query
                    viewModel.searchUsers(query.ifEmpty { null })
                }
            )
        }
        
        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (uiState) {
                is UsersUIState.Loading -> {
                    if (!hideLoadingState) {
                        loadingView?.invoke() ?: CometChatLoadingState(style = style.loadingStateStyle)
                    }
                }
                
                is UsersUIState.Empty -> {
                    if (!hideEmptyState) {
                        emptyView?.invoke() ?: CometChatEmptyState(
                            style = style.emptyStateStyle,
                            title = context.getString(R.string.cometchat_user_list_empty_state_title),
                            subtitle = context.getString(R.string.cometchat_user_list_empty_state_subtitle)
                        )
                    }
                }
                
                is UsersUIState.Error -> {
                    if (!hideErrorState) {
                        errorView?.invoke { viewModel.fetchUsers() }
                            ?: CometChatErrorState(
                                style = style.errorStateStyle,
                                title = context.getString(R.string.cometchat_error_conversations_title),
                                subtitle = context.getString(R.string.cometchat_something_went_wrong_please_try_again),
                                onRetry = { viewModel.fetchUsers() }
                            )
                    }
                }
                
                is UsersUIState.Content -> {
                    UsersListContent(
                        users = users,
                        selectedUsers = selectedUsers,
                        selectionMode = selectionMode,
                        style = style,
                        hideStatusIndicator = hideStatusIndicator,
                        hideStickyHeader = hideStickyHeader,
                        hideSeparator = hideSeparator,
                        itemView = itemView,
                        leadingView = leadingView,
                        titleView = titleView,
                        subtitleView = subtitleView,
                        trailingView = trailingView,
                        onItemClick = { user ->
                            if (selectionMode != UIKitConstants.SelectionMode.NONE) {
                                viewModel.selectUser(user, selectionMode)
                            } else {
                                onItemClick?.invoke(user)
                            }
                        },
                        onItemLongClick = { user ->
                            if (options != null || addOptions != null) {
                                popupMenuUser = user
                                showPopupMenu = true
                            }
                            onItemLongClick?.invoke(user)
                        },
                        onLoadMore = { viewModel.fetchUsers() },
                        scrollToTopEvent = viewModel.scrollToTopEvent
                    )
                }
            }
        }
    }
    
    // Popup menu
    if (showPopupMenu && popupMenuUser != null) {
        val menuItems = buildMenuItems(
            context = context,
            user = popupMenuUser!!,
            style = style,
            options = options,
            addOptions = addOptions
        )
        
        if (menuItems.isNotEmpty()) {
            CometChatPopupMenu(
                expanded = showPopupMenu,
                onDismissRequest = {
                    showPopupMenu = false
                    popupMenuUser = null
                },
                menuItems = menuItems,
                style = style.popupMenuStyle,
                onMenuItemClick = { _, _ ->
                    showPopupMenu = false
                    popupMenuUser = null
                },
                content = { /* Anchor content - empty since we're showing as overlay */ }
            )
        }
    }
}


/**
 * Internal composable for the users list toolbar.
 * Handles both normal mode and selection mode display.
 */
@Composable
private fun UsersToolbar(
    title: String,
    style: CometChatUsersStyle,
    hideBackIcon: Boolean,
    selectionMode: UIKitConstants.SelectionMode,
    selectedCount: Int,
    overflowMenu: (@Composable () -> Unit)?,
    onBackPress: (() -> Unit)?,
    onDiscardSelection: () -> Unit,
    onSubmitSelection: () -> Unit
) {
    val isInSelectionMode = selectionMode != UIKitConstants.SelectionMode.NONE && selectedCount > 0
    
    val toolbarStyle = CometChatToolbarStyle.default(
        backgroundColor = style.backgroundColor,
        titleTextColor = style.titleTextColor,
        titleTextStyle = style.titleTextStyle,
        navigationIcon = style.backIcon,
        navigationIconTint = style.backIconTint,
        separatorColor = style.toolbarSeparatorColor,
        separatorHeight = style.toolbarSeparatorHeight,
        showSeparator = style.showToolbarSeparator
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
            navigationContentDescription = "Discard selection",
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
                                contentDescription = "Submit selection of $selectedCount users"
                                role = Role.Button
                            }
                    ) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = style.submitSelectionIconTint,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(12.dp)
                        )
                    }
                }
            }
        )
    } else {
        // Normal mode toolbar
        CometChatToolbar(
            title = title,
            style = toolbarStyle,
            hideBackIcon = hideBackIcon,
            navigationContentDescription = "Go back",
            onNavigationClick = onBackPress,
            actions = if (overflowMenu != null) {
                { overflowMenu() }
            } else null
        )
    }
}

/**
 * Internal composable for the users list content with sticky headers.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UsersListContent(
    users: List<User>,
    selectedUsers: Set<User>,
    selectionMode: UIKitConstants.SelectionMode,
    style: CometChatUsersStyle,
    hideStatusIndicator: Boolean,
    hideStickyHeader: Boolean,
    hideSeparator: Boolean,
    itemView: (@Composable (User) -> Unit)?,
    leadingView: (@Composable (User) -> Unit)?,
    titleView: (@Composable (User) -> Unit)?,
    subtitleView: (@Composable (User) -> Unit)?,
    trailingView: (@Composable (User) -> Unit)?,
    onItemClick: (User) -> Unit,
    onItemLongClick: (User) -> Unit,
    onLoadMore: () -> Unit,
    scrollToTopEvent: kotlinx.coroutines.flow.SharedFlow<Unit>
) {
    val listState = rememberLazyListState()
    
    // Group users by first letter while preserving order (important for online users at top)
    val groupedUsers = remember(users) {
        UsersUtils.groupUsersByFirstLetterPreservingOrder(users)
    }
    
    // Handle scroll to top events
    LaunchedEffect(scrollToTopEvent) {
        scrollToTopEvent.collect {
            listState.animateScrollToItem(0)
        }
    }
    
    // Detect when we need to load more
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad) {
                    onLoadMore()
                }
            }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        groupedUsers.forEachIndexed { groupIndex, (letter, usersInGroup) ->
            // Sticky header - use groupIndex to ensure unique keys when same letter appears multiple times
            if (!hideStickyHeader) {
                stickyHeader(key = "header_${letter}_$groupIndex") {
                    StickyHeader(
                        letter = letter,
                        style = style
                    )
                }
            }
            
            // Users in this group
            items(
                items = usersInGroup,
                key = { user -> user.uid }
            ) { user ->
                val isSelected = selectedUsers.any { it.uid == user.uid }
                
                if (itemView != null) {
                    itemView(user)
                } else {
                    CometChatUsersListItem(
                        user = user,
                        onItemClick = onItemClick,
                        onItemLongClick = { onItemLongClick(it) },
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        hideStatusIndicator = hideStatusIndicator,
                        style = style.itemStyle,
                        leadingView = leadingView,
                        titleView = titleView,
                        subtitleView = subtitleView,
                        trailingView = trailingView
                    )
                }
                
                // Separator
                if (!hideSeparator && usersInGroup.last() != user) {
                    Divider(
                        color = style.separatorColor,
                        thickness = style.separatorHeight,
                        modifier = Modifier.padding(start = 76.dp)
                    )
                }
            }
        }
    }
}

/**
 * Sticky header composable for alphabetical grouping.
 */
@Composable
private fun StickyHeader(
    letter: Char,
    style: CometChatUsersStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.stickyHeaderBackgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = letter.toString(),
            color = style.stickyHeaderTextColor,
            style = style.stickyHeaderTextStyle
        )
    }
}

/**
 * Builds the menu items for the popup menu.
 */
@Composable
private fun buildMenuItems(
    context: Context,
    user: User,
    style: CometChatUsersStyle,
    options: ((Context, User) -> List<MenuItem>)?,
    addOptions: ((Context, User) -> List<MenuItem>)?
): List<MenuItem> {
    // If custom options are provided, use them exclusively
    if (options != null) {
        return options(context, user)
    }
    
    val menuItems = mutableListOf<MenuItem>()
    
    // Add additional options if provided
    if (addOptions != null) {
        menuItems.addAll(addOptions(context, user))
    }
    
    return menuItems
}
