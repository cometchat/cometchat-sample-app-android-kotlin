package com.cometchat.uikit.compose.presentation.emojikeyboard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import com.cometchat.uikit.compose.presentation.emojikeyboard.model.Emoji

/**
 * A composable that displays emojis in a fixed-column grid layout.
 *
 * Uses a non-lazy Column+Row approach instead of LazyVerticalGrid because this
 * composable is rendered inside a LazyColumn item (EmojiCategoryList), and
 * nesting lazy layouts is not supported in Compose.
 *
 * Each emoji is rendered as a clickable Text composable displaying the unicode character.
 * Click and long-click are handled via [combinedClickable] when callbacks are provided.
 *
 * @param emojis List of emojis to display in the grid
 * @param modifier Modifier for the grid container
 * @param columns Number of columns in the grid (default: 8, matching existing GridLayoutManager)
 * @param onClick Callback invoked with the emoji unicode string when tapped; null disables click
 * @param onLongClick Callback invoked with the emoji unicode string on long-press; null disables long-click
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EmojiGrid(
    emojis: List<Emoji>,
    modifier: Modifier = Modifier,
    columns: Int = 8,
    onClick: ((String) -> Unit)?,
    onLongClick: ((String) -> Unit)?
) {
    val rows = emojis.chunked(columns)

    Column(modifier = modifier.fillMaxWidth()) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (emoji in row) {
                    EmojiItem(
                        emoji = emoji,
                        modifier = Modifier.weight(1f),
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
                // Fill remaining cells in the last row to maintain alignment
                repeat(columns - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * A single emoji item rendered as a clickable Text composable.
 *
 * When both [onClick] and [onLongClick] are null, the item is rendered without
 * any click handling. When either is provided, [combinedClickable] is used to
 * handle both gestures.
 *
 * @param emoji The emoji to display
 * @param modifier Modifier for the item container
 * @param onClick Callback for tap gesture
 * @param onLongClick Callback for long-press gesture
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmojiItem(
    emoji: Emoji,
    modifier: Modifier = Modifier,
    onClick: ((String) -> Unit)?,
    onLongClick: ((String) -> Unit)?
) {
    val hasClickHandler = onClick != null || onLongClick != null

    Box(
        modifier = modifier
            .then(
                if (hasClickHandler) {
                    Modifier.combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onClick?.invoke(emoji.emoji) },
                        onLongClick = { onLongClick?.invoke(emoji.emoji) }
                    )
                } else {
                    Modifier
                }
            )
            .semantics {
                contentDescription = emoji.emoji
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji.emoji,
            fontSize = 24.sp
        )
    }
}
