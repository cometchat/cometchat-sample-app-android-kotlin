package com.cometchat.uikit.compose.presentation.stickerkeyboard.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatStickerKeyboard component.
 *
 * This immutable data class encapsulates all visual styling properties for the sticker keyboard,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the sticker keyboard container
 * @param separatorColor Color for the separator line between content and tabs
 * @param tabIconSize Size of the tab icons (width and height)
 * @param tabActiveIndicatorColor Color of the active tab indicator
 * @param stickerItemSize Size of individual sticker items in the grid
 * @param emptyStateTitleTextColor Text color for empty state title
 * @param emptyStateTitleTextStyle Text style for empty state title
 * @param emptyStateSubtitleTextColor Text color for empty state subtitle
 * @param emptyStateSubtitleTextStyle Text style for empty state subtitle
 * @param errorStateTextColor Text color for error state message
 * @param errorStateTextStyle Text style for error state message
 */
@Immutable
data class CometChatStickerKeyboardStyle(
    val backgroundColor: Color,
    val separatorColor: Color,
    val tabIconSize: Dp,
    val tabActiveIndicatorColor: Color,
    val stickerItemSize: Dp,
    val emptyStateTitleTextColor: Color,
    val emptyStateTitleTextStyle: TextStyle,
    val emptyStateSubtitleTextColor: Color,
    val emptyStateSubtitleTextStyle: TextStyle,
    val errorStateTextColor: Color,
    val errorStateTextStyle: TextStyle
) {
    companion object {
        /**
         * Creates a default CometChatStickerKeyboardStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatStickerKeyboardStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            tabIconSize: Dp = 36.dp,
            tabActiveIndicatorColor: Color = CometChatTheme.colorScheme.primary,
            stickerItemSize: Dp = 108.dp,
            emptyStateTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            emptyStateTitleTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            emptyStateSubtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            emptyStateSubtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            errorStateTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            errorStateTextStyle: TextStyle = CometChatTheme.typography.bodyRegular
        ): CometChatStickerKeyboardStyle = CometChatStickerKeyboardStyle(
            backgroundColor = backgroundColor,
            separatorColor = separatorColor,
            tabIconSize = tabIconSize,
            tabActiveIndicatorColor = tabActiveIndicatorColor,
            stickerItemSize = stickerItemSize,
            emptyStateTitleTextColor = emptyStateTitleTextColor,
            emptyStateTitleTextStyle = emptyStateTitleTextStyle,
            emptyStateSubtitleTextColor = emptyStateSubtitleTextColor,
            emptyStateSubtitleTextStyle = emptyStateSubtitleTextStyle,
            errorStateTextColor = errorStateTextColor,
            errorStateTextStyle = errorStateTextStyle
        )
    }
}
