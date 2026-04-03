package com.cometchat.uikit.compose.presentation.stickerkeyboard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatShimmerBox
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.ProvideShimmerAnimation
import com.cometchat.uikit.compose.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.domain.model.Sticker
import com.cometchat.uikit.core.factory.CometChatStickerKeyboardViewModelFactory
import com.cometchat.uikit.core.state.StickerKeyboardUIState
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel

/**
 * CometChatStickerKeyboard displays a keyboard for selecting stickers.
 *
 * This composable shows:
 * - Horizontal pager for swipeable sticker set pages
 * - Tab bar for quick navigation between sticker sets
 * - Loading, empty, and error states with customization
 *
 * Features:
 * - Swipe between sticker sets
 * - Tab selection syncs with pager
 * - GIF animation support
 * - Custom view slots for states
 * - Style customization
 *
 * @param modifier Modifier applied to the container
 * @param viewModel Optional ViewModel (creates default if not provided)
 * @param style Style configuration for the component
 * @param hideLoadingState Whether to hide the loading state
 * @param hideEmptyState Whether to hide the empty state
 * @param hideErrorState Whether to hide the error state
 * @param loadingView Custom loading state composable
 * @param emptyView Custom empty state composable
 * @param errorView Custom error state composable with retry callback
 * @param onStickerClick Callback invoked when a sticker is clicked
 * @param onError Callback invoked when an error occurs
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatStickerKeyboard(
    modifier: Modifier = Modifier,
    viewModel: CometChatStickerKeyboardViewModel? = null,
    style: CometChatStickerKeyboardStyle = CometChatStickerKeyboardStyle.default(),
    // Visibility flags
    hideLoadingState: Boolean = false,
    hideEmptyState: Boolean = false,
    hideErrorState: Boolean = false,
    // Custom views
    loadingView: (@Composable () -> Unit)? = null,
    emptyView: (@Composable () -> Unit)? = null,
    errorView: (@Composable (onRetry: () -> Unit) -> Unit)? = null,
    // Callbacks
    onStickerClick: ((Sticker) -> Unit)? = null,
    onError: ((CometChatException) -> Unit)? = null
) {
    // Create default ViewModel if none provided
    val stickerKeyboardViewModel = viewModel ?: viewModel(
        factory = CometChatStickerKeyboardViewModelFactory()
    )

    // Set up sticker click callback
    LaunchedEffect(onStickerClick) {
        stickerKeyboardViewModel.onStickerClick = onStickerClick
    }

    // Collect state from ViewModel
    val uiState by stickerKeyboardViewModel.uiState.collectAsState()
    val stickerSets by stickerKeyboardViewModel.stickerSets.collectAsState()
    val selectedSetIndex by stickerKeyboardViewModel.selectedSetIndex.collectAsState()

    // Handle error callback
    LaunchedEffect(uiState) {
        if (uiState is StickerKeyboardUIState.Error) {
            onError?.invoke((uiState as StickerKeyboardUIState.Error).exception)
        }
    }

    // Main container
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(style.backgroundColor)
    ) {
        when (uiState) {
            is StickerKeyboardUIState.Loading -> {
                if (!hideLoadingState) {
                    loadingView?.invoke() ?: StickerKeyboardLoadingState(style = style)
                }
            }

            is StickerKeyboardUIState.Empty -> {
                if (!hideEmptyState) {
                    emptyView?.invoke() ?: StickerKeyboardEmptyState(style = style)
                }
            }

            is StickerKeyboardUIState.Error -> {
                if (!hideErrorState) {
                    errorView?.invoke { stickerKeyboardViewModel.retry() }
                        ?: StickerKeyboardErrorState(
                            style = style,
                            onRetry = { stickerKeyboardViewModel.retry() }
                        )
                }
            }

            is StickerKeyboardUIState.Content -> {
                StickerKeyboardContent(
                    stickerSets = stickerSets,
                    selectedSetIndex = selectedSetIndex,
                    style = style,
                    onTabSelected = { index -> stickerKeyboardViewModel.selectStickerSet(index) },
                    onStickerClick = { sticker -> stickerKeyboardViewModel.onStickerClicked(sticker) }
                )
            }
        }
    }
}


/**
 * Content state showing sticker sets with pager and tab bar.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StickerKeyboardContent(
    stickerSets: List<com.cometchat.uikit.core.domain.model.StickerSet>,
    selectedSetIndex: Int,
    style: CometChatStickerKeyboardStyle,
    onTabSelected: (Int) -> Unit,
    onStickerClick: (Sticker) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = selectedSetIndex,
        pageCount = { stickerSets.size }
    )

    // Sync pager with tab selection
    LaunchedEffect(selectedSetIndex) {
        if (pagerState.currentPage != selectedSetIndex) {
            pagerState.animateScrollToPage(selectedSetIndex)
        }
    }

    // Sync tab selection with pager swipe
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                if (page != selectedSetIndex) {
                    onTabSelected(page)
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticker grid pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            StickerGrid(
                stickers = stickerSets[page].stickers,
                stickerItemSize = style.stickerItemSize,
                onStickerClick = onStickerClick
            )
        }

        // Separator
        HorizontalDivider(
            color = style.separatorColor,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Tab bar
        StickerTabBar(
            stickerSets = stickerSets,
            selectedIndex = selectedSetIndex,
            tabIconSize = style.tabIconSize,
            activeIndicatorColor = style.tabActiveIndicatorColor,
            onTabSelected = onTabSelected
        )
    }
}

/**
 * Loading state with shimmer effect.
 */
@Composable
private fun StickerKeyboardLoadingState(
    style: CometChatStickerKeyboardStyle
) {
    ProvideShimmerAnimation {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
                .padding(16.dp)
                .semantics {
                    contentDescription = "Loading stickers, please wait"
                    liveRegion = LiveRegionMode.Polite
                }
        ) {
            // Shimmer grid (4x4)
            repeat(4) { row ->
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                ) {
                    repeat(4) {
                        CometChatShimmerBox(
                            width = 72.dp,
                            height = 72.dp,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Shimmer tab bar
            HorizontalDivider(
                color = style.separatorColor,
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                repeat(5) {
                    CometChatShimmerBox(
                        width = 36.dp,
                        height = 36.dp,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Empty state when no stickers are available.
 */
@Composable
private fun StickerKeyboardEmptyState(
    style: CometChatStickerKeyboardStyle
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.cometchat_no_stickers_available),
                style = style.emptyStateTitleTextStyle,
                color = style.emptyStateTitleTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.cometchat_you_don_t_have_any_stickers_yet),
                style = style.emptyStateSubtitleTextStyle,
                color = style.emptyStateSubtitleTextColor
            )
        }
    }
}

/**
 * Error state with retry button.
 */
@Composable
private fun StickerKeyboardErrorState(
    style: CometChatStickerKeyboardStyle,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.cometchat_looks_like_something_went_wrong_n_please_try_again),
                style = style.errorStateTextStyle,
                color = style.errorStateTextColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CometChatTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.cometchat_retry),
                    color = CometChatTheme.colorScheme.colorWhite
                )
            }
        }
    }
}
