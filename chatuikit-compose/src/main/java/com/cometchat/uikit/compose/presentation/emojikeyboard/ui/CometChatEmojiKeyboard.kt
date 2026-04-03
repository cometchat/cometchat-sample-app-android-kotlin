package com.cometchat.uikit.compose.presentation.emojikeyboard.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.emojikeyboard.model.EmojiCategory
import com.cometchat.uikit.compose.presentation.emojikeyboard.model.EmojiRepository
import com.cometchat.uikit.compose.presentation.emojikeyboard.style.CometChatEmojiKeyboardStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Top-level composable that renders the emoji picker panel.
 *
 * Faithful Compose equivalent of `EmojiKeyBoardView` (MaterialCardView). Layout is a
 * vertical column with the emoji category list on top, a 1dp separator, and the
 * category tab bar at the bottom. Scroll position and selected tab are synchronized
 * bidirectionally, matching the existing View-based implementation.
 *
 * @param modifier Modifier applied to the keyboard container
 * @param style Style configuration controlling all visual properties
 * @param onClick Callback invoked with the emoji unicode string when a user taps an emoji
 * @param onLongClick Callback invoked with the emoji unicode string on long-press
 */
@Composable
fun CometChatEmojiKeyboard(
    modifier: Modifier = Modifier,
    style: CometChatEmojiKeyboardStyle = CometChatEmojiKeyboardStyle.default(),
    onClick: ((String) -> Unit)? = null,
    onLongClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    // Trigger emoji data loading (no-op if already cached)
    LaunchedEffect(Unit) {
        EmojiRepository.loadAndSaveEmojis(context)
    }

    // Reactive state: poll until data is available (background thread loads it)
    var categories by remember { mutableStateOf(EmojiRepository.getEmojiCategories()) }
    LaunchedEffect(Unit) {
        while (categories.isNullOrEmpty()) {
            delay(50)
            categories = EmojiRepository.getEmojiCategories()
        }
    }

    if (categories.isNullOrEmpty()) {
        // Loading state: show a centered progress indicator while data loads
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val loadedCategories = categories!!

    // --- Scroll-tab synchronization state ---
    val listState: LazyListState = rememberLazyListState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Flag to suppress scroll-driven tab updates during programmatic (tab-driven) scrolls.
    // This prevents the circular sync loop: tab tap → scroll → scroll listener → tab update.
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Scroll-driven tab update:
    // Observe the last visible item index via snapshotFlow (efficient, no recomposition overhead).
    // Only update the tab when the user is actively scrolling (not during programmatic scrolls).
    // This mirrors the existing onScrolled → findLastVisibleItemPosition() logic.
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect { lastVisibleIndex ->
            if (lastVisibleIndex != null && listState.isScrollInProgress && !isProgrammaticScroll) {
                selectedTabIndex = lastVisibleIndex
            }
        }
    }

    // Reset the programmatic scroll flag when scrolling settles.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling) {
                    isProgrammaticScroll = false
                }
            }
    }

    // --- Container styling (MaterialCardView equivalent) ---
    val shape = remember(style.cornerRadius) { RoundedCornerShape(style.cornerRadius) }
    val containerModifier = modifier
        .clip(shape)
        .then(
            if (style.strokeWidth > 0.dp) {
                Modifier.border(style.strokeWidth, style.strokeColor, shape).padding(10.dp)
            } else {
                Modifier
            }
        )
        .drawBehind { drawRect(style.backgroundColor) }

    Column(modifier = containerModifier) {
        // Emoji category list (scrollable content) — takes remaining vertical space
        EmojiCategoryList(
            categories = loadedCategories,
            listState = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            style = style,
            onClick = onClick,
            onLongClick = onLongClick
        )

        // 1dp separator between content and tab bar
        HorizontalDivider(
            thickness = 1.dp,
            color = style.separatorColor,
            modifier = Modifier.fillMaxWidth()
        )

        // Category tab bar at the bottom
        EmojiCategoryTabBar(
            categories = loadedCategories,
            selectedIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            style = style,
            onTabSelected = { index ->
                // Tab-driven scroll: set tab immediately, then scroll programmatically.
                // Mirrors the existing onTabSelected logic: if (!isScrolling) scrollToPosition().
                selectedTabIndex = index
                isProgrammaticScroll = true
                coroutineScope.launch {
                    listState.scrollToItem(index)
                }
            }
        )
    }
}
