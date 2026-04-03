package com.cometchat.uikit.compose.presentation.imageviewer.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatImageViewer component.
 *
 * This immutable data class encapsulates all visual styling properties for the image viewer,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the full-screen image viewer
 * @param toolbarBackgroundColor Background color for the toolbar overlay
 * @param iconTintColor Tint color for back and share icons
 * @param loadingIndicatorColor Color for the circular loading indicator
 */
@Immutable
data class CometChatImageViewerStyle(
    val backgroundColor: Color,
    val toolbarBackgroundColor: Color,
    val iconTintColor: Color,
    val loadingIndicatorColor: Color
) {
    companion object {
        /**
         * Creates a default CometChatImageViewerStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatImageViewerStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = Color.Black,
            toolbarBackgroundColor: Color = Color.Black.copy(alpha = 0.5f),
            iconTintColor: Color = CometChatTheme.colorScheme.iconTintWhite,
            loadingIndicatorColor: Color = CometChatTheme.colorScheme.primary
        ): CometChatImageViewerStyle = CometChatImageViewerStyle(
            backgroundColor = backgroundColor,
            toolbarBackgroundColor = toolbarBackgroundColor,
            iconTintColor = iconTintColor,
            loadingIndicatorColor = loadingIndicatorColor
        )
    }
}
