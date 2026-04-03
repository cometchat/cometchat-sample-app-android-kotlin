package com.cometchat.uikit.compose.presentation.emojikeyboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.emojikeyboard.model.EmojiCategory
import com.cometchat.uikit.compose.presentation.emojikeyboard.style.CometChatEmojiKeyboardStyle

/**
 * A single emoji category section containing a category name header and an emoji grid.
 *
 * Compose equivalent of `cometchat_emoji_container.xml` layout (category_name TextView +
 * emoji_list_view RecyclerView) used in [EmojiAdapter.EmojiViewHolder].
 *
 * @param category The emoji category to display
 * @param modifier Modifier for the section container
 * @param style Style configuration controlling text color and text style for the header
 * @param onClick Callback invoked with the emoji unicode string when tapped
 * @param onLongClick Callback invoked with the emoji unicode string on long-press
 */
@Composable
internal fun EmojiCategorySection(
    category: EmojiCategory,
    modifier: Modifier = Modifier,
    style: CometChatEmojiKeyboardStyle,
    onClick: ((String) -> Unit)?,
    onLongClick: ((String) -> Unit)?
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = category.name,
            color = style.categoryTextColor,
            style = style.categoryTextStyle,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
        EmojiGrid(
            emojis = category.emojis,
            onClick = onClick,
            onLongClick = onLongClick
        )
    }
}
