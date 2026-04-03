package com.cometchat.uikit.compose.presentation.shared.defaultstates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatErrorState.
 * Contains all visual styling properties for the error state component.
 */
@Immutable
data class CometChatErrorStateStyle(
    val backgroundColor: Color,
    val icon: Painter?,
    val iconTint: Color,
    val iconSize: Dp,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val retryButtonText: String,
    val retryButtonBackgroundColor: Color,
    val retryButtonTextColor: Color,
    val retryButtonTextStyle: TextStyle
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Does NOT use Material Theme colors directly.
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            icon: Painter? = null,
            iconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            iconSize: Dp = 80.dp,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            retryButtonText: String = "Retry",
            retryButtonBackgroundColor: Color = CometChatTheme.colorScheme.primaryButtonBackgroundColor,
            retryButtonTextColor: Color = CometChatTheme.colorScheme.textColorWhite,
            retryButtonTextStyle: TextStyle = CometChatTheme.typography.buttonMedium
        ): CometChatErrorStateStyle = CometChatErrorStateStyle(
            backgroundColor = backgroundColor,
            icon = icon,
            iconTint = iconTint,
            iconSize = iconSize,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            retryButtonText = retryButtonText,
            retryButtonBackgroundColor = retryButtonBackgroundColor,
            retryButtonTextColor = retryButtonTextColor,
            retryButtonTextStyle = retryButtonTextStyle
        )
    }
}
