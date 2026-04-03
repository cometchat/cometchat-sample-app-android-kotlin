package com.cometchat.uikit.compose.presentation.emojikeyboard.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cometchat.uikit.compose.presentation.emojikeyboard.model.EmojiCategory
import com.cometchat.uikit.compose.presentation.emojikeyboard.style.CometChatEmojiKeyboardStyle

/**
 * Vertically scrolling list of emoji category sections.
 *
 * Each section contains a category name header followed by an 8-column emoji grid.
 * Compose equivalent of the outer RecyclerView with LinearLayoutManager in the
 * existing EmojiKeyBoardView implementation.
 *
 * @param categories List of emoji categories to display
 * @param listState LazyListState exposed for scroll-tab synchronization
 * @param modifier Modifier for the list container
 * @param style Style configuration for category sections
 * @param onClick Callback invoked with the emoji unicode string when tapped
 * @param onLongClick Callback invoked with the emoji unicode string on long-press
 */
@Composable
internal fun EmojiCategoryList(
    categories: List<EmojiCategory>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    style: CometChatEmojiKeyboardStyle,
    onClick: ((String) -> Unit)?,
    onLongClick: ((String) -> Unit)?
) {
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        itemsIndexed(
            items = categories,
            key = { _, category -> category.id },
            contentType = { _, _ -> "emoji_category_section" }
        ) { _, category ->
            EmojiCategorySection(
                category = category,
                style = style,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
    }
}
