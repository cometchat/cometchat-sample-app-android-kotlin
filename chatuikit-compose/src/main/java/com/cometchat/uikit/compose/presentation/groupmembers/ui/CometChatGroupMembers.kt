package com.cometchat.uikit.compose.presentation.groupmembers.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.groupmembers.style.CometChatGroupMembersStyle
import com.cometchat.uikit.compose.presentation.groupmembers.utils.GroupMembersUtils
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.shimmer.style.CometChatGroupMemberShimmerStyle
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatGroupMemberShimmer
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.ProvideShimmerAnimation
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatDialog
import com.cometchat.uikit.compose.presentation.shared.searchbox.CometChatSearchBox
import com.cometchat.uikit.compose.presentation.shared.searchbox.CometChatSearchBoxStyle
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbar
import com.cometchat.uikit.compose.presentation.shared.toolbar.CometChatToolbarStyle
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.factory.CometChatGroupMembersViewModelFactory
import com.cometchat.uikit.core.state.DialogState
import com.cometchat.uikit.core.state.GroupMembersUIState
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel
import com.cometchat.uikit.compose.presentation.groupmembers.state.CometChatGroupMembersState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * CometChatGroupMembers displays a list of group members with support for
 * real-time updates, member management (kick/ban/scope change), selection modes,
 * custom views, and full styling customization.
 *
 * This component follows Clean Architecture + MVVM pattern with StateFlow-based
 * state management. It shares the ViewModel from chatuikit-core with the XML Views
 * implementation.
 *
 * Features:
 * - Displays group members with avatar, name, and scope
 * - Supports pagination with infinite scroll
 * - Real-time updates via CometChat listeners
 * - Member management: kick, ban, change scope
 * - Selection modes: NONE, SINGLE, MULTIPLE
 * - Search with debouncing
 * - Fully customizable styling
 * - Custom view slots for all sections
 * - Accessibility support
 *
 * @param group The Group object for which to display members (required)
 * @param modifier Modifier applied to the parent container
 * @param viewModel The ViewModel managing member state (optional, creates default if not provided)
 * @param groupMembersRequestBuilder Custom request builder for fetching members
 * @param style Style configuration for the component
 * @param selectionMode Selection mode (NONE, SINGLE, MULTIPLE)
 * @param hideToolbar Whether to hide the entire toolbar
 * @param hideSearch Whether to hide the search box
 * @param hideBackButton Whether to hide the back navigation icon
 * @param hideSeparator Whether to hide item separators
 * @param hideUserStatus Whether to hide user online/offline status indicators
 * @param disableKick Whether to disable kick member action
 * @param disableBan Whether to disable ban member action
 * @param disableChangeScope Whether to disable change scope action
 * @param hideLoadingState Whether to hide loading state
 * @param hideEmptyState Whether to hide empty state
 * @param hideErrorState Whether to hide error state
 * @param title Toolbar title text
 * @param searchPlaceholderText Placeholder text for search box
 * @param searchRequestBuilder Custom request builder for searching members
 * @param excludeOwner Whether to exclude the group owner from the members list
 * @param emptyView Custom empty state composable
 * @param errorView Custom error state composable with exception parameter
 * @param loadingView Custom loading state composable
 * @param listItemView Custom item composable replacing entire item
 * @param leadingView Custom composable for the leading section (avatar area)
 * @param titleView Custom composable for the title section (member name)
 * @param subtitleView Custom subtitle section composable
 * @param tailView Custom trailing section composable
 * @param overflowMenu Custom overflow menu composable
 * @param options Function to provide custom menu options for each member (replaces defaults)
 * @param addOptions Function to provide additional menu options appended to defaults
 * @param onItemClick Callback for item clicks
 * @param onItemLongClick Callback for item long-clicks
 * @param onSelection Callback for selection completion with selected members list
 * @param onBackPress Callback for back navigation
 * @param onError Callback for errors
 * @param state Optional state holder for programmatic access to selection state. Use [rememberCometChatGroupMembersState] to create.
 *
 * @sample
 * ```
 * // Basic usage
 * CometChatGroupMembers(
 *     group = myGroup
 * )
 *
 * // With callbacks
 * CometChatGroupMembers(
 *     group = myGroup,
 *     onItemClick = { member -> viewMemberProfile(member) },
 *     onError = { exception -> showError(exception) }
 * )
 *
 * // With selection mode
 * CometChatGroupMembers(
 *     group = myGroup,
 *     selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
 *     onSelection = { selectedMembers -> handleSelection(selectedMembers) }
 * )
 *
 * // With custom styling
 * CometChatGroupMembers(
 *     group = myGroup,
 *     style = CometChatGroupMembersStyle.default(
 *         backgroundColor = Color.White,
 *         itemTitleTextColor = Color.Black
 *     )
 * )
 *
 * // With state holder for programmatic access
 * val groupMembersState = rememberCometChatGroupMembersState()
 *
 * CometChatGroupMembers(
 *     group = myGroup,
 *     selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
 *     state = groupMembersState
 * )
 *
 * // Access selected members programmatically
 * Button(onClick = {
 *     val selected = groupMembersState.getSelectedMembers()
 *     processSelectedMembers(selected)
 * }) {
 *     Text("Process Selection")
 * }
 * ```
 *
 * Migration from Java implementation:
 * - LiveData → StateFlow: All state is managed via Kotlin StateFlow/SharedFlow
 * - Java View → Compose: Replaces MaterialCardView-based implementation with composables
 * - Shared ViewModel: Uses [CometChatGroupMembersViewModel] from chatuikit-core
 * - Style: Uses [CometChatGroupMembersStyle] data class with `default()` companion function
 * - Custom views: Uses composable lambdas instead of ViewHolderListener interfaces
 *
 * @see CometChatGroupMembersViewModel
 * @see CometChatGroupMembersStyle
 * @see CometChatGroupMemberListItem
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CometChatGroupMembers(
    group: Group,
    modifier: Modifier = Modifier,
    viewModel: CometChatGroupMembersViewModel? = null,
    groupMembersRequestBuilder: GroupMembersRequest.GroupMembersRequestBuilder? = null,
    style: CometChatGroupMembersStyle = CometChatGroupMembersStyle.default(),
    selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
    hideToolbar: Boolean = false,
    hideSearch: Boolean = false,
    hideBackButton: Boolean = false,
    hideSeparator: Boolean = true,
    hideUserStatus: Boolean = false,
    disableKick: Boolean = false,
    disableBan: Boolean = false,
    disableChangeScope: Boolean = false,
    hideLoadingState: Boolean = false,
    hideEmptyState: Boolean = false,
    hideErrorState: Boolean = false,
    title: String? = null,
    searchPlaceholderText: String? = null,
    searchRequestBuilder: GroupMembersRequest.GroupMembersRequestBuilder? = null,
    excludeOwner: Boolean = false,
    emptyView: (@Composable () -> Unit)? = null,
    errorView: (@Composable (CometChatException) -> Unit)? = null,
    loadingView: (@Composable () -> Unit)? = null,
    listItemView: (@Composable (GroupMember) -> Unit)? = null,
    leadingView: (@Composable (GroupMember) -> Unit)? = null,
    titleView: (@Composable (GroupMember) -> Unit)? = null,
    subtitleView: (@Composable (GroupMember) -> Unit)? = null,
    tailView: (@Composable (GroupMember) -> Unit)? = null,
    overflowMenu: (@Composable () -> Unit)? = null,
    options: ((GroupMember) -> List<MenuItem>)? = null,
    addOptions: ((GroupMember) -> List<MenuItem>)? = null,
    onItemClick: ((GroupMember) -> Unit)? = null,
    onItemLongClick: ((GroupMember) -> Unit)? = null,
    onSelection: ((List<GroupMember>) -> Unit)? = null,
    onBackPress: (() -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null,
    state: CometChatGroupMembersState? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    // Use localized strings with fallback to provided values
    val localizedTitle = title ?: context.getString(R.string.cometchat_group_members)
    val localizedSearchPlaceholder = searchPlaceholderText ?: context.getString(R.string.cometchat_search)
    
    // Task 2.3.2: Initialize ViewModel with factory if not provided
    val effectiveViewModel = viewModel ?: viewModel(
        factory = CometChatGroupMembersViewModelFactory()
    )
    
    // Task 2.1: Wire state holder to ViewModel when provided
    // This enables programmatic access to selection state via the state holder
    LaunchedEffect(state, effectiveViewModel) {
        state?.setViewModel(effectiveViewModel)
    }
    
    // Task 2.3.3: Set group on ViewModel in LaunchedEffect
    LaunchedEffect(group, groupMembersRequestBuilder, searchRequestBuilder, excludeOwner) {
        effectiveViewModel.setGroup(group)
        groupMembersRequestBuilder?.let {
            effectiveViewModel.setGroupMembersRequestBuilder(it)
        }
        searchRequestBuilder?.let {
            effectiveViewModel.setSearchRequestBuilder(it)
        }
        effectiveViewModel.setExcludeOwner(excludeOwner)
        effectiveViewModel.fetchGroupMembers()
    }
    
    // Task 2.3.12: Collect and handle UI state from ViewModel
    // Task 2.3.16: Add lifecycle-aware state collection
    val uiState by effectiveViewModel.uiState.collectAsStateWithLifecycle()
    val members by effectiveViewModel.members.collectAsStateWithLifecycle()
    val selectedMembers by effectiveViewModel.selectedMembers.collectAsStateWithLifecycle()
    val dialogState by effectiveViewModel.dialogState.collectAsStateWithLifecycle()
    val hasMore by effectiveViewModel.hasMore.collectAsStateWithLifecycle()
    val isLoading by effectiveViewModel.isLoading.collectAsStateWithLifecycle()
    
    // Task 2.3.5: Search debounce using Job + delay (matches XML View pattern)
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val searchDebounceMs = 300L
    
    // Internal mutable selection mode — initialized from the parameter but can be
    // reset to NONE when the user taps the discard (cross) button, hiding checkboxes.
    var activeSelectionMode by remember { mutableStateOf(selectionMode) }
    
    // Sync internal state when the caller changes the parameter
    LaunchedEffect(selectionMode) {
        activeSelectionMode = selectionMode
    }
    
    // Task 2.3.13: Collect and handle events from ViewModel
    LaunchedEffect(Unit) {
        effectiveViewModel.events.collect { event ->
            // Events are handled automatically by the ViewModel's list operations
            // UI updates happen via the members StateFlow
        }
    }
    
    // Handle state callbacks
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GroupMembersUIState.Error -> onError?.invoke(state.exception)
            else -> { /* No callback for other states */ }
        }
    }
    
    // Lifecycle-aware pause/resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Refresh on resume if needed
                }
                Lifecycle.Event.ON_PAUSE -> {
                    // Pause operations if needed
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Create search box style from group members style
    val searchBoxStyle = CometChatSearchBoxStyle.default(
        backgroundColor = style.searchBackgroundColor,
        textColor = style.searchTextColor,
        textStyle = style.searchTextStyle,
        placeholderColor = style.searchPlaceholderColor,
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
        // Task 2.3.4: Implement toolbar with title and back button
        if (!hideToolbar) {
            GroupMembersToolbar(
                title = localizedTitle,
                style = style,
                hideBackButton = hideBackButton,
                selectionMode = activeSelectionMode,
                selectedCount = selectedMembers.size,
                overflowMenu = overflowMenu,
                onBackPress = onBackPress,
                onDiscardSelection = {
                    effectiveViewModel.clearSelection()
                    activeSelectionMode = UIKitConstants.SelectionMode.NONE
                },
                onSubmitSelection = {
                    onSelection?.invoke(selectedMembers.values.toList())
                }
            )
        }
        
        // Task 2.3.5: Implement search box with debouncing
        if (!hideSearch) {
            CometChatSearchBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                style = searchBoxStyle,
                onTextChange = { newText ->
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(searchDebounceMs)
                        effectiveViewModel.searchGroupMembers(newText.ifEmpty { null })
                    }
                },
                placeholderText = localizedSearchPlaceholder,
                onClear = {
                    searchJob?.cancel()
                    effectiveViewModel.searchGroupMembers(null)
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
                // Task 2.3.8: Implement loading state view
                is GroupMembersUIState.Loading -> {
                    if (!hideLoadingState) {
                        loadingView?.invoke() ?: GroupMembersLoadingState(style = style)
                    }
                }
                
                // Task 2.3.9: Implement empty state view
                is GroupMembersUIState.Empty -> {
                    if (!hideEmptyState) {
                        emptyView?.invoke() ?: CometChatEmptyState(
                            style = style.emptyStateStyle,
                            title = context.getString(R.string.cometchat_no_members_found),
                            subtitle = context.getString(R.string.cometchat_no_members_found_subtitle)
                        )
                    }
                }
                
                // Task 2.3.10: Implement error state view with retry
                is GroupMembersUIState.Error -> {
                    if (!hideErrorState) {
                        val exception = (uiState as GroupMembersUIState.Error).exception
                        errorView?.invoke(exception) ?: CometChatErrorState(
                            style = style.errorStateStyle,
                            title = context.getString(R.string.cometchat_error_loading_members),
                            subtitle = exception.message ?: context.getString(R.string.cometchat_something_went_wrong_please_try_again),
                            onRetry = { effectiveViewModel.fetchGroupMembers() }
                        )
                    }
                }
                
                // Task 2.3.6: Implement LazyColumn for member list
                // Task 2.3.7: Implement infinite scroll pagination
                is GroupMembersUIState.Content -> {
                    GroupMembersListContent(
                        members = members,
                        selectedMembers = selectedMembers,
                        selectionMode = activeSelectionMode,
                        style = style,
                        hideSeparator = hideSeparator,
                        hideUserStatus = hideUserStatus,
                        disableKick = disableKick,
                        disableBan = disableBan,
                        disableChangeScope = disableChangeScope,
                        listItemView = listItemView,
                        leadingView = leadingView,
                        titleView = titleView,
                        subtitleView = subtitleView,
                        tailView = tailView,
                        options = options,
                        addOptions = addOptions,
                        context = context,
                        viewModel = effectiveViewModel,
                        group = group,
                        hasMore = hasMore,
                        isLoading = isLoading,
                        onItemClick = { member ->
                            if (activeSelectionMode != UIKitConstants.SelectionMode.NONE) {
                                if (effectiveViewModel.isSelected(member)) {
                                    effectiveViewModel.deselectMember(member)
                                } else {
                                    if (activeSelectionMode == UIKitConstants.SelectionMode.SINGLE) {
                                        effectiveViewModel.clearSelection()
                                    }
                                    effectiveViewModel.selectMember(member)
                                }
                            } else {
                                onItemClick?.invoke(member)
                            }
                        },
                        onItemLongClick = onItemLongClick,
                        onLoadMore = { effectiveViewModel.fetchGroupMembers() }
                    )
                }
            }
        }
    }
    
    // Task 2.3.11: Implement confirmation dialogs (kick, ban, scope change)
    when (val state = dialogState) {
        is DialogState.ConfirmKick -> {
            CometChatDialog(
                title = context.getString(R.string.cometchat_kick) + " " + state.member.name + " ?",
                message = "Are You sure you want to " + context.getString(R.string.cometchat_kick).lowercase() + " " + (state.member.name ?: "") + "?",
                positiveButtonText = context.getString(R.string.cometchat_yes),
                negativeButtonText = context.getString(R.string.cometchat_no),
                icon = painterResource(R.drawable.cometchat_ic_delete),
                onPositiveClick = {
                    effectiveViewModel.kickMember(state.member)
                },
                onNegativeClick = {
                    effectiveViewModel.dismissDialog()
                }
            )
        }
        
        is DialogState.ConfirmBan -> {
            CometChatDialog(
                title = context.getString(R.string.cometchat_ban) + " " + state.member.name + " ?",
                message = "Are You sure you want to " + context.getString(R.string.cometchat_ban).lowercase() + " " + (state.member.name ?: "") + "?",
                positiveButtonText = context.getString(R.string.cometchat_yes),
                negativeButtonText = context.getString(R.string.cometchat_no),
                icon = painterResource(R.drawable.cometchat_ic_delete),
                onPositiveClick = {
                    effectiveViewModel.banMember(state.member)
                },
                onNegativeClick = {
                    effectiveViewModel.dismissDialog()
                }
            )
        }
        
        is DialogState.SelectScope -> {
            ScopeChangeBottomSheet(
                member = state.member,
                group = group,
                currentScope = state.currentScope,
                context = context,
                onScopeSelected = { newScope ->
                    effectiveViewModel.changeMemberScope(state.member, newScope)
                },
                onDismiss = {
                    effectiveViewModel.dismissDialog()
                }
            )
        }
        
        DialogState.Hidden -> {
            // No dialog to show
        }
    }
}

/**
 * Internal composable for the group members toolbar.
 * Handles both normal mode and selection mode display.
 */
@Composable
private fun GroupMembersToolbar(
    title: String,
    style: CometChatGroupMembersStyle,
    hideBackButton: Boolean,
    selectionMode: UIKitConstants.SelectionMode,
    selectedCount: Int,
    overflowMenu: (@Composable () -> Unit)?,
    onBackPress: (() -> Unit)?,
    onDiscardSelection: () -> Unit,
    onSubmitSelection: () -> Unit
) {
    val context = LocalContext.current
    val isInSelectionMode = selectionMode != UIKitConstants.SelectionMode.NONE
    
    val toolbarStyle = CometChatToolbarStyle.default(
        backgroundColor = style.backgroundColor,
        titleTextColor = style.titleTextColor,
        titleTextStyle = style.titleTextStyle,
        navigationIcon = style.backIcon,
        navigationIconTint = style.backIconTint
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
                            .clickable (
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ){ onSubmitSelection() }
                            .semantics {
                                contentDescription = "Submit selection of $selectedCount members"
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
            title = title,
            style = toolbarStyle,
            hideBackIcon = hideBackButton,
            navigationContentDescription = "Go back",
            onNavigationClick = onBackPress,
            actions = if (overflowMenu != null) {
                { overflowMenu() }
            } else null
        )
    }
}

/**
 * Internal composable for the member list content with infinite scroll.
 * Task 2.3.6 & 2.3.7: LazyColumn with pagination
 * Task 2.3.14: Custom view slots
 * Task 2.3.15: Wire up all callbacks
 */
@Composable
private fun GroupMembersListContent(
    members: List<GroupMember>,
    selectedMembers: Map<String, GroupMember>,
    selectionMode: UIKitConstants.SelectionMode,
    style: CometChatGroupMembersStyle,
    hideSeparator: Boolean,
    hideUserStatus: Boolean,
    disableKick: Boolean,
    disableBan: Boolean,
    disableChangeScope: Boolean,
    listItemView: (@Composable (GroupMember) -> Unit)?,
    leadingView: (@Composable (GroupMember) -> Unit)?,
    titleView: (@Composable (GroupMember) -> Unit)?,
    subtitleView: (@Composable (GroupMember) -> Unit)?,
    tailView: (@Composable (GroupMember) -> Unit)?,
    options: ((GroupMember) -> List<MenuItem>)?,
    addOptions: ((GroupMember) -> List<MenuItem>)?,
    context: Context,
    viewModel: CometChatGroupMembersViewModel,
    group: Group,
    hasMore: Boolean,
    isLoading: Boolean,
    onItemClick: (GroupMember) -> Unit,
    onItemLongClick: ((GroupMember) -> Unit)?,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    
    // Infinite scroll detection
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null &&
                    lastVisibleItem.index >= members.size - 3 &&
                    hasMore &&
                    !isLoading
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = members,
            key = { member -> member.uid }
        ) { member ->
            // Task 2.3.14: Implement custom view slots
            if (listItemView != null) {
                listItemView(member)
            } else {
                val menuItems = buildMenuItems(
                    member = member,
                    context = context,
                    disableKick = disableKick,
                    disableBan = disableBan,
                    disableChangeScope = disableChangeScope,
                    options = options,
                    addOptions = addOptions,
                    viewModel = viewModel,
                    group = group
                )
                
                CometChatGroupMemberListItem(
                    member = member,
                    onItemClick = onItemClick,
                    group = group,
                    onItemLongClick = onItemLongClick,
                    isSelected = selectedMembers.containsKey(member.uid),
                    selectionMode = selectionMode,
                    hideUserStatus = hideUserStatus,
                    menuItems = menuItems,
                    onMenuItemClick = { id, _ ->
                        handleMenuItemClick(id, member, viewModel)
                    },
                    style = style,
                    leadingView = leadingView,
                    titleView = titleView,
                    subtitleView = subtitleView,
                    tailView = tailView
                )
                
                // Separator — hidden for last item, matching XML adapter behavior
                val isLastItem = member.uid == members.lastOrNull()?.uid
                if (!hideSeparator && !isLastItem && style.separatorHeight > 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(style.separatorHeight)
                            .background(style.separatorColor)
                    )
                }
            }
        }
    }
}

/**
 * Builds the menu items for the overflow menu.
 * Uses [GroupMembersUtils] for permission checks, matching the XML Views implementation.
 * Supports custom options and default actions (kick, ban, change scope).
 */
@Composable
private fun buildMenuItems(
    member: GroupMember,
    context: Context,
    disableKick: Boolean,
    disableBan: Boolean,
    disableChangeScope: Boolean,
    options: ((GroupMember) -> List<MenuItem>)?,
    addOptions: ((GroupMember) -> List<MenuItem>)?,
    viewModel: CometChatGroupMembersViewModel,
    group: Group
): List<MenuItem> {
    // If custom options are provided, use them exclusively
    if (options != null) {
        return options(member)
    }
    
    val loggedInUser = try {
        CometChatUIKit.getLoggedInUser()
    } catch (e: Exception) {
        null
    } ?: return emptyList()
    
    // Determine logged-in user's scope
    val loggedInScope = if (loggedInUser.uid == group.owner) {
        CometChatConstants.SCOPE_ADMIN
    } else {
        viewModel.getItems().find { it.uid == loggedInUser.uid }?.scope
            ?: CometChatConstants.SCOPE_PARTICIPANT
    }
    
    val menuItems = GroupMembersUtils.getDefaultGroupMemberOptions(
        context = context,
        groupMember = member,
        group = group,
        loggedInUserId = loggedInUser.uid,
        loggedInUserScope = loggedInScope,
        disableKick = disableKick,
        disableBan = disableBan,
        disableChangeScope = disableChangeScope
    ).toMutableList()
    
    // Append additional options if provided
    addOptions?.let { menuItems.addAll(it(member)) }
    
    return menuItems
}

/**
 * Handles menu item clicks using UIKitConstants.GroupMemberOption IDs,
 * matching the XML Views implementation.
 */
private fun handleMenuItemClick(
    id: String,
    member: GroupMember,
    viewModel: CometChatGroupMembersViewModel
) {
    when {
        id.equals(UIKitConstants.GroupMemberOption.KICK, ignoreCase = true) ->
            viewModel.showKickConfirmation(member)
        id.equals(UIKitConstants.GroupMemberOption.BAN, ignoreCase = true) ->
            viewModel.showBanConfirmation(member)
        id.equals(UIKitConstants.GroupMemberOption.CHANGE_SCOPE, ignoreCase = true) ->
            viewModel.showScopeSelection(member)
    }
}

/**
 * Scope change bottom sheet composable matching the Kotlin XML BottomSheetDialog behavior exactly.
 *
 * Key parity with chatuikit-kotlin CometChatScopeChange:
 * - Text on LEFT, radio button on RIGHT (matching cometchat_list_scope_change_item_radio.xml)
 * - Moderators cannot assign Admin scope: radio disabled + dimmed text (ScopeAdapter logic)
 * - Progress indicator on Save button during scope change (matches setDialogState(INITIATED))
 * - Drag handle, scope icon, title, subtitle layout matches cometchat_scope_change_layout.xml
 *
 * Dimension mapping from XML:
 * - padding_5 = 20dp (root horizontal + bottom padding)
 * - margin_3 = 12dp (drag handle top, title top, radio card top, buttons top)
 * - margin_6 = 24dp (icon card top from drag handle)
 * - margin_2 = 8dp (subtitle top, button inner vertical padding)
 * - margin_5 = 20dp (button inner horizontal padding)
 * - padding_4 = 16dp (radio item horizontal padding)
 * - padding_2 = 8dp (radio item vertical padding)
 * - radius_2 = 8dp (card corner radius)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScopeChangeBottomSheet(
    member: GroupMember,
    group: Group,
    currentScope: String,
    context: Context,
    onScopeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedScope by remember(member.uid, currentScope) { mutableStateOf(currentScope) }
    var isSaving by remember { mutableStateOf(false) }

    val scopes = listOf(
        CometChatConstants.SCOPE_ADMIN,
        CometChatConstants.SCOPE_MODERATOR,
        CometChatConstants.SCOPE_PARTICIPANT
    )

    // Moderator cannot assign Admin scope — matches ScopeAdapter logic
    val isModeratorViewing = CometChatConstants.SCOPE_MODERATOR.equals(group.scope, ignoreCase = true)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = sheetState,
        containerColor = CometChatTheme.colorScheme.backgroundColor1,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            // Drag handle: marginTop=12dp, 32x4dp, rounded
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CometChatTheme.colorScheme.neutralColor500)
            )
        },
        modifier = Modifier.semantics { contentDescription = "Change Scope" }
    ) {
        // Inner content: paddingStart/End=20dp, paddingBottom=20dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon card: marginTop=24dp from drag handle
            Spacer(modifier = Modifier.height(24.dp))

            // Scope icon in circular background (80dp, inner padding=16dp → icon=48dp)
            Card(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = CometChatTheme.colorScheme.backgroundColor2
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cometchat_ic_change_scope),
                        contentDescription = null,
                        tint = CometChatTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Title: marginTop=12dp
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = context.getString(R.string.cometchat_scope_change),
                style = CometChatTheme.typography.heading2Medium,
                color = CometChatTheme.colorScheme.textColorPrimary,
                textAlign = TextAlign.Center
            )

            // Subtitle: marginTop=8dp
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = context.getString(R.string.cometchat_change_scope_subtitle),
                style = CometChatTheme.typography.bodyRegular,
                color = CometChatTheme.colorScheme.textColorSecondary,
                textAlign = TextAlign.Center
            )

            // Radio card: marginTop=12dp, cornerRadius=8dp, stroke=1dp, transparent bg
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(
                    1.dp,
                    CometChatTheme.colorScheme.strokeColorLight
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    scopes.forEach { scope ->
                        // Moderator cannot assign Admin scope
                        val isDisabled = isModeratorViewing &&
                                scope.equals(CometChatConstants.SCOPE_ADMIN, ignoreCase = true)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (!isDisabled) {
                                    Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        selectedScope = scope
                                    }
                                } else {
                                    Modifier
                                })
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Text on LEFT (weight=1), matching XML layout
                            Text(
                                text = scope.replaceFirstChar { it.uppercase() },
                                style = CometChatTheme.typography.heading4Medium,
                                color = if (isDisabled) {
                                    CometChatTheme.colorScheme.textColorSecondary
                                } else {
                                    CometChatTheme.colorScheme.textColorPrimary
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Radio button on RIGHT (26x26dp), matching XML layout
                            RadioButton(
                                selected = scope.equals(selectedScope, ignoreCase = true),
                                onClick = if (!isDisabled) {
                                    { selectedScope = scope }
                                } else null,
                                enabled = !isDisabled,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CometChatTheme.colorScheme.primary,
                                    unselectedColor = if (isDisabled) {
                                        CometChatTheme.colorScheme.strokeColorDefault
                                    } else {
                                        CometChatTheme.colorScheme.strokeColorDefault
                                    },
                                    disabledSelectedColor = CometChatTheme.colorScheme.strokeColorDefault,
                                    disabledUnselectedColor = CometChatTheme.colorScheme.strokeColorDefault
                                ),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            // Buttons row: marginTop=12dp
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cancel button: cornerRadius=8dp, stroke=1dp, bg=backgroundColor1
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = !isSaving) {
                        },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CometChatTheme.colorScheme.backgroundColor1
                    ),
                    border = BorderStroke(
                        1.dp,
                        CometChatTheme.colorScheme.strokeColorLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = context.getString(R.string.cometchat_cancel),
                        style = CometChatTheme.typography.buttonMedium,
                        color = CometChatTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Save button: cornerRadius=8dp, stroke=1dp, bg=primary
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = !isSaving) {
                            if (!selectedScope.equals(currentScope, ignoreCase = true)) {
                                isSaving = true
                                onScopeSelected(selectedScope)
                            } else {
                                onDismiss()
                            }
                        },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CometChatTheme.colorScheme.primary
                    ),
                    border = BorderStroke(
                        1.dp,
                        CometChatTheme.colorScheme.strokeColorLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSaving) {
                            // Progress indicator matching Kotlin XML save button progress
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = CometChatTheme.colorScheme.iconTintSecondary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = context.getString(R.string.cometchat_save),
                                style = CometChatTheme.typography.buttonMedium,
                                color = CometChatTheme.colorScheme.colorWhite,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Internal composable for the group members loading state with animated shimmer.
 *
 * Uses [ProvideShimmerAnimation] to share a single animation across all shimmer items,
 * ensuring synchronized animation. Renders multiple [CometChatGroupMemberShimmer] items
 * to simulate the loading state of a group members list.
 *
 * @param style The group members style containing shimmer configuration
 * @param itemCount Number of shimmer items to display. Defaults to 8
 */
@Composable
private fun GroupMembersLoadingState(
    style: CometChatGroupMembersStyle,
    itemCount: Int = 8
) {
    val shimmerStyle = CometChatGroupMemberShimmerStyle.default(
        shimmerConfig = style.loadingStateStyle.shimmerConfig
    )

    ProvideShimmerAnimation(config = style.loadingStateStyle.shimmerConfig) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        ) {
            repeat(itemCount) {
                CometChatGroupMemberShimmer(style = shimmerStyle)
            }
        }
    }
}
