package com.cometchat.uikit.compose.presentation.reactionlist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.reactionlist.style.CometChatReactionListStyle
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatReactionListShimmer
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.ProvideShimmerAnimation
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.factory.CometChatReactionListViewModelFactory
import com.cometchat.uikit.core.state.ReactionListUIState
import com.cometchat.uikit.core.viewmodel.CometChatReactionListViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * CometChatReactionList displays users who reacted to a message.
 *
 * This composable shows:
 * - Horizontal tabs for each reaction emoji with counts
 * - Vertical scrollable list of users who reacted
 * - Support for pagination, caching, and reaction removal
 *
 * Features:
 * - "All" tab showing total reaction count
 * - Individual emoji tabs with counts
 * - Infinite scroll pagination
 * - Logged-in user can remove their own reactions
 * - Shimmer loading state
 * - Error state with customization
 * - Real-time updates via listeners
 * - Custom view slots for item parts (leadingView, titleView, subtitleView, trailingView)
 *
 * @param baseMessage The message to display reactions for
 * @param modifier Modifier applied to the container
 * @param viewModel Optional ViewModel (creates default if not provided)
 * @param reactionsRequestBuilder Optional custom request builder for fetching reactions
 * @param selectedReaction The emoji to pre-select when the list opens. Pass `null` or
 *   [ALL_TAB_IDENTIFIER] to start on the "All" tab (default behavior).
 * @param style Style configuration for the component
 * @param hideSeparator Whether to hide the item separators in the user list (header separator is always visible)
 * @param hideLoadingState Whether to hide the loading state
 * @param hideErrorState Whether to hide the error state
 * @param loadingView Custom loading state composable
 * @param errorView Custom error state composable
 * @param itemView Custom composable to replace the entire item
 * @param leadingView Custom composable for the leading view (avatar area)
 * @param titleView Custom composable for the title view (user name)
 * @param subtitleView Custom composable for the subtitle view ("Tap to remove")
 * @param trailingView Custom composable for the trailing view (emoji)
 * @param onItemClick Callback invoked when a reaction item is clicked
 * @param onEmpty Callback invoked when the reaction list becomes empty
 * @param onError Callback invoked when an error occurs
 */
@Composable
fun CometChatReactionList(
    baseMessage: BaseMessage,
    modifier: Modifier = Modifier,
    viewModel: CometChatReactionListViewModel? = null,
    reactionsRequestBuilder: ReactionsRequest.ReactionsRequestBuilder? = null,
    selectedReaction: String? = null,
    style: CometChatReactionListStyle = CometChatReactionListStyle.default(),
    // Visibility flags
    hideSeparator: Boolean = true,
    hideLoadingState: Boolean = false,
    hideErrorState: Boolean = false,
    // Custom views - State views
    loadingView: (@Composable () -> Unit)? = null,
    errorView: (@Composable () -> Unit)? = null,
    // Custom views - Item parts
    itemView: (@Composable (Reaction) -> Unit)? = null,
    leadingView: (@Composable (Reaction) -> Unit)? = null,
    titleView: (@Composable (Reaction) -> Unit)? = null,
    subtitleView: (@Composable (Reaction) -> Unit)? = null,
    trailingView: (@Composable (Reaction) -> Unit)? = null,
    // Callbacks
    onItemClick: ((Reaction, BaseMessage) -> Unit)? = null,
    onEmpty: (() -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null
) {
    val context = LocalContext.current

    // Create default ViewModel if none provided
    val reactionListViewModel = viewModel ?: viewModel(
        factory = CometChatReactionListViewModelFactory()
    )

    // Collect state from ViewModel
    val uiState by reactionListViewModel.uiState.collectAsState()
    val reactionHeaders by reactionListViewModel.reactionHeaders.collectAsState()
    val reactedUsers by reactionListViewModel.reactedUsers.collectAsState()
    val activeTabIndex by reactionListViewModel.activeTabIndex.collectAsState()

    // Get logged-in user for identifying current user's reactions
    val loggedInUserId = remember {
        try {
            CometChatUIKit.getLoggedInUser()?.uid
        } catch (e: Exception) {
            null
        }
    }

    // Initialize ViewModel with base message, request builder, and optional pre-selected reaction
    LaunchedEffect(baseMessage) {
        reactionListViewModel.setBaseMessage(baseMessage)
        reactionsRequestBuilder?.let {
            reactionListViewModel.setReactionsRequestBuilder(it)
        }
        // Pre-select the reaction tab if specified, otherwise default to "All"
        val initialReaction = if (!selectedReaction.isNullOrEmpty() && selectedReaction != ALL_TAB_IDENTIFIER) {
            reactionListViewModel.setSelectedReaction(selectedReaction)
            selectedReaction
        } else {
            ALL_TAB_IDENTIFIER
        }
        reactionListViewModel.fetchReactedUsers(initialReaction, reactionsRequestBuilder)
    }

    // Handle state callbacks
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ReactionListUIState.Empty -> onEmpty?.invoke()
            is ReactionListUIState.Error -> onError?.invoke(state.exception)
            else -> { /* No callback for Loading or Content */ }
        }
    }

    // Main container
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(
                RoundedCornerShape(
                    topStart = style.cornerRadius,
                    topEnd = style.cornerRadius,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            )
            .background(style.backgroundColor)
    ) {
        // Reaction Header Tabs
        if (reactionHeaders.isNotEmpty()) {
            ReactionHeaderTabs(
                reactionCounts = reactionHeaders.drop(1), // Drop the "All" tab from headers, it's created internally
                activeTabIndex = activeTabIndex,
                onTabSelected = { index, reaction ->
                    reactionListViewModel.setSelectedReaction(reaction)
                    reactionListViewModel.fetchReactedUsers(reaction, reactionsRequestBuilder)
                },
                style = style,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Header Separator between tabs and list - always visible
        // hideSeparator only affects item separators, not the header separator
        HorizontalDivider(
            color = style.separatorColor,
            thickness = style.separatorHeight,
            modifier = Modifier.fillMaxWidth()
        )

        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (uiState) {
                is ReactionListUIState.Loading -> {
                    if (!hideLoadingState) {
                        loadingView?.invoke() ?: ReactionListAnimatedShimmer(style = style)
                    }
                }

                is ReactionListUIState.Error -> {
                    if (!hideErrorState) {
                        errorView?.invoke() ?: ReactionListErrorState(
                            style = style,
                            errorText = stringResource(R.string.cometchat_reaction_list_error)
                        )
                    }
                }

                is ReactionListUIState.Empty -> {
                    // Empty state - list is empty, onEmpty callback already invoked
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.cometchat_no_reactions),
                            style = style.errorTextStyle,
                            color = style.errorTextColor
                        )
                    }
                }

                is ReactionListUIState.Content -> {
                    ReactionUsersList(
                        reactedUsers = reactedUsers,
                        baseMessage = baseMessage,
                        loggedInUserId = loggedInUserId,
                        style = style,
                        itemView = itemView,
                        leadingView = leadingView,
                        titleView = titleView,
                        subtitleView = subtitleView,
                        trailingView = trailingView,
                        hideSeparator = hideSeparator,
                        onItemClick = { reaction ->
                            // If it's the logged-in user's reaction, remove it
                            if (reaction.uid == loggedInUserId) {
                                reactionListViewModel.removeReaction(baseMessage, reaction.reaction)
                            }
                            onItemClick?.invoke(reaction, baseMessage)
                        },
                        onLoadMore = {
                            reactionListViewModel.fetchReactedUsers(null, reactionsRequestBuilder)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Internal composable for the reaction users list with infinite scroll.
 */
@Composable
private fun ReactionUsersList(
    reactedUsers: List<Reaction>,
    baseMessage: BaseMessage,
    loggedInUserId: String?,
    style: CometChatReactionListStyle,
    itemView: (@Composable (Reaction) -> Unit)?,
    leadingView: (@Composable (Reaction) -> Unit)?,
    titleView: (@Composable (Reaction) -> Unit)?,
    subtitleView: (@Composable (Reaction) -> Unit)?,
    trailingView: (@Composable (Reaction) -> Unit)?,
    hideSeparator: Boolean,
    onItemClick: (Reaction) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    // Detect when we need to load more (infinite scroll)
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad && reactedUsers.isNotEmpty()) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = reactedUsers,
            key = { reaction -> "${reaction.uid}_${reaction.reaction}_${reaction.reactedAt}" }
        ) { reaction ->
            val isCurrentUser = reaction.uid == loggedInUserId

            ReactedUserItem(
                reaction = reaction,
                isCurrentUser = isCurrentUser,
                onClick = { onItemClick(reaction) },
                style = style.itemStyle,
                itemView = itemView,
                leadingView = leadingView,
                titleView = titleView,
                subtitleView = subtitleView,
                trailingView = trailingView
            )

            // Item separator - only show if not hidden and not the last item
            // Uses the same separatorColor from main style for consistency with header separator
            if (!hideSeparator && reactedUsers.last() != reaction) {
                HorizontalDivider(
                    color = style.separatorColor,
                    thickness = style.itemStyle.separatorHeight,
                    modifier = Modifier.padding(start = 68.dp)
                )
            }
        }
    }
}

/**
 * Animated shimmer loading state for the reaction list.
 * Uses CometChatReactionListShimmer with shared animation for synchronized effect.
 */
@Composable
private fun ReactionListAnimatedShimmer(
    style: CometChatReactionListStyle
) {
    ProvideShimmerAnimation {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
                .padding(horizontal = 16.dp)
                .semantics {
                    contentDescription = "Loading reactions, please wait"
                    liveRegion = LiveRegionMode.Polite
                }
        ) {
            repeat(5) {
                CometChatReactionListShimmer()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Error state for the reaction list.
 */
@Composable
private fun ReactionListErrorState(
    style: CometChatReactionListStyle,
    errorText: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorText,
            style = style.errorTextStyle,
            color = style.errorTextColor
        )
    }
}
