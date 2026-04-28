package com.cometchat.uikit.compose.presentation.search.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for SearchMessageItem component.
 */
@Immutable
data class SearchMessageItemStyle(
    val backgroundColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val timestampTextColor: Color,
    val timestampTextStyle: TextStyle,
    val linkTextColor: Color,
    val linkTextStyle: TextStyle,
    val threadIconTint: Color,
    val threadIcon: Painter?
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
            linkTextColor: Color = CometChatTheme.colorScheme.infoColor,
            linkTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            threadIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            threadIcon: Painter? = null
        ): SearchMessageItemStyle = SearchMessageItemStyle(
            backgroundColor = backgroundColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            timestampTextColor = timestampTextColor,
            timestampTextStyle = timestampTextStyle,
            linkTextColor = linkTextColor,
            linkTextStyle = linkTextStyle,
            threadIconTint = threadIconTint,
            threadIcon = threadIcon
        )
    }
}
