package com.cometchat.uikit.compose.presentation.reactionlist.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatReactionListItem component.
 *
 * This immutable data class encapsulates all visual styling properties for the reaction list item,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the list item
 * @param titleTextColor Text color for the user name (or "You")
 * @param titleTextStyle Text style for the user name
 * @param subtitleTextColor Text color for the subtitle ("Tap to remove")
 * @param subtitleTextStyle Text style for the subtitle
 * @param tailViewTextColor Text color for the emoji in tail view
 * @param tailViewTextStyle Text style for the emoji in tail view
 * @param avatarStyle Style configuration for the avatar component
 * @param separatorColor Color for the item separator line
 * @param separatorHeight Height of the item separator line
 */
@Immutable
data class CometChatReactionListItemStyle(
    val backgroundColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val tailViewTextColor: Color,
    val tailViewTextStyle: TextStyle,
    val avatarStyle: CometChatAvatarStyle,
    val separatorColor: Color,
    val separatorHeight: Dp
) {
    companion object {
        /**
         * Creates a default CometChatReactionListItemStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatReactionListItemStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = Color.Transparent,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            tailViewTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            tailViewTextStyle: TextStyle = CometChatTheme.typography.heading2Regular,
            avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default(),
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            separatorHeight: Dp = 1.dp
        ): CometChatReactionListItemStyle = CometChatReactionListItemStyle(
            backgroundColor = backgroundColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            tailViewTextColor = tailViewTextColor,
            tailViewTextStyle = tailViewTextStyle,
            avatarStyle = avatarStyle,
            separatorColor = separatorColor,
            separatorHeight = separatorHeight
        )
    }
}
