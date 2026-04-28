package com.cometchat.uikit.compose.presentation.search.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount.BadgeCountStyle
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceiptsStyle
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for SearchConversationItem component.
 */
@Immutable
data class SearchConversationItemStyle(
    val backgroundColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val timestampTextColor: Color,
    val timestampTextStyle: TextStyle,
    val avatarStyle: CometChatAvatarStyle,
    val badgeStyle: BadgeCountStyle,
    val statusIndicatorStyle: CometChatStatusIndicatorStyle,
    val receiptStyle: CometChatReceiptsStyle
) {
    companion object {
        @Composable
        fun default(
            backgroundColor: Color = Color.Transparent,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            timestampTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            timestampTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default(),
            badgeStyle: BadgeCountStyle = BadgeCountStyle.default(),
            statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle.default(),
            receiptStyle: CometChatReceiptsStyle = CometChatReceiptsStyle.default()
        ): SearchConversationItemStyle = SearchConversationItemStyle(
            backgroundColor = backgroundColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            timestampTextColor = timestampTextColor,
            timestampTextStyle = timestampTextStyle,
            avatarStyle = avatarStyle,
            badgeStyle = badgeStyle,
            statusIndicatorStyle = statusIndicatorStyle,
            receiptStyle = receiptStyle
        )
    }
}
