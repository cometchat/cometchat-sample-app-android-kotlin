package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.cometchat.uikit.compose.presentation.imageviewer.style.CometChatImageViewerStyle
import com.cometchat.uikit.compose.presentation.imageviewer.ui.CometChatImageViewerScreen
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

/**
 * Preview showing the default image viewer.
 * Note: The image won't load in preview since it requires a real URL,
 * but the UI structure (toolbar, loading indicator) will be visible.
 */
@Preview(showBackground = true, name = "ImageViewer - Default")
@Composable
fun PreviewImageViewerDefault() {
    CometChatTheme {
        CometChatImageViewerScreen(
            imageUrl = "https://example.com/sample-image.jpg",
            fileName = "sample-image.jpg",
            mimeType = "image/jpeg",
            onBack = { },
            onShare = { _, _, _ -> }
        )
    }
}

/**
 * Preview showing image viewer with PNG file.
 */
@Preview(showBackground = true, name = "ImageViewer - PNG File")
@Composable
fun PreviewImageViewerPng() {
    CometChatTheme {
        CometChatImageViewerScreen(
            imageUrl = "https://example.com/screenshot.png",
            fileName = "screenshot.png",
            mimeType = "image/png",
            onBack = { },
            onShare = { _, _, _ -> }
        )
    }
}

// ============================================================================
// SECTION 2: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "ImageViewer - Custom Background")
@Composable
fun PreviewImageViewerCustomBackground() {
    CometChatTheme {
        CometChatImageViewerScreen(
            imageUrl = "https://example.com/sample-image.jpg",
            fileName = "sample-image.jpg",
            mimeType = "image/jpeg",
            style = CometChatImageViewerStyle.default(
                backgroundColor = Color(0xFF1A1A1A)
            ),
            onBack = { },
            onShare = { _, _, _ -> }
        )
    }
}
