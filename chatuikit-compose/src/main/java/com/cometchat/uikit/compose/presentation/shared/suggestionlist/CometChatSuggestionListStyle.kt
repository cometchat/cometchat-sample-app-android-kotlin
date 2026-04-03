package com.cometchat.uikit.compose.presentation.shared.suggestionlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatSuggestionList component.
 * 
 * @property backgroundColor Background color of the suggestion list container
 * @property strokeColor Border/stroke color of the container
 * @property strokeWidth Border/stroke width
 * @property cornerRadius Corner radius of the container
 * @property maxHeight Maximum height of the suggestion list
 * @property itemBackgroundColor Background color of each suggestion item
 * @property itemTextColor Text color for the suggestion item name
 * @property itemTextStyle Text style for the suggestion item name
 * @property itemInfoTextColor Text color for the suggestion item info/subtitle
 * @property itemInfoTextStyle Text style for the suggestion item info/subtitle
 * @property avatarStyle Style for the avatar in each suggestion item
 * @property shimmerBaseColor Base color for shimmer loading effect
 * @property shimmerHighlightColor Highlight color for shimmer loading effect
 * @property separatorColor Color of the separator between items
 * @property separatorHeight Height of the separator
 */
@Immutable
data class CometChatSuggestionListStyle(
    val backgroundColor: Color,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val cornerRadius: Dp,
    val maxHeight: Dp,
    val itemBackgroundColor: Color,
    val itemTextColor: Color,
    val itemTextStyle: TextStyle,
    val itemInfoTextColor: Color,
    val itemInfoTextStyle: TextStyle,
    val avatarStyle: CometChatAvatarStyle,
    val shimmerBaseColor: Color,
    val shimmerHighlightColor: Color,
    val separatorColor: Color,
    val separatorHeight: Dp
) {
    companion object {
        /**
         * Creates a default CometChatSuggestionListStyle using CometChatTheme colors.
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            strokeColor: Color = CometChatTheme.colorScheme.borderColorLight,
            strokeWidth: Dp = 1.dp,
            cornerRadius: Dp = 8.dp,
            maxHeight: Dp = 250.dp,
            itemBackgroundColor: Color = Color.Transparent,
            itemTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            itemTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            itemInfoTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            itemInfoTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default(),
            shimmerBaseColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            shimmerHighlightColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            separatorColor: Color = CometChatTheme.colorScheme.borderColorLight,
            separatorHeight: Dp = 0.5.dp
        ): CometChatSuggestionListStyle = CometChatSuggestionListStyle(
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            maxHeight = maxHeight,
            itemBackgroundColor = itemBackgroundColor,
            itemTextColor = itemTextColor,
            itemTextStyle = itemTextStyle,
            itemInfoTextColor = itemInfoTextColor,
            itemInfoTextStyle = itemInfoTextStyle,
            avatarStyle = avatarStyle,
            shimmerBaseColor = shimmerBaseColor,
            shimmerHighlightColor = shimmerHighlightColor,
            separatorColor = separatorColor,
            separatorHeight = separatorHeight
        )
    }
}
