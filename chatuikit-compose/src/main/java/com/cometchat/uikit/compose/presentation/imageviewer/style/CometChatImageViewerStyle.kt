package com.cometchat.uikit.compose.presentation.imageviewer.style

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatImageViewer component.
 *
 * This immutable data class encapsulates all visual styling properties for the image viewer,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the image viewer
 * @param toolbarBackgroundColor Background color for the toolbar overlay
 * @param iconTintColor Tint color for the toolbar icons (back, download, share)
 * @param loadingIndicatorColor Color for the circular loading indicator
 * @param downloadIcon Drawable for the download action. Defaults to the download glyph.
 * @param shareIcon Drawable for the share action.
 */
@Immutable
data class CometChatImageViewerStyle(
    val backgroundColor: Color,
    val toolbarBackgroundColor: Color,
    val iconTintColor: Color,
    val loadingIndicatorColor: Color,
    @DrawableRes val downloadIcon: Int = R.drawable.cometchat_download_icon,
    @DrawableRes val shareIcon: Int = R.drawable.cometchat_ic_share
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
            loadingIndicatorColor: Color = CometChatTheme.colorScheme.primary,
            @DrawableRes downloadIcon: Int = R.drawable.cometchat_download_icon,
            @DrawableRes shareIcon: Int = R.drawable.cometchat_ic_share
        ): CometChatImageViewerStyle = CometChatImageViewerStyle(
            backgroundColor = backgroundColor,
            toolbarBackgroundColor = toolbarBackgroundColor,
            iconTintColor = iconTintColor,
            loadingIndicatorColor = loadingIndicatorColor,
            downloadIcon = downloadIcon,
            shareIcon = shareIcon
        )
    }
}
