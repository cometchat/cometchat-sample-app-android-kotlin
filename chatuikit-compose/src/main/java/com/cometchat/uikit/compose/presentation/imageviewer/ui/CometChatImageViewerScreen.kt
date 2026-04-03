package com.cometchat.uikit.compose.presentation.imageviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cometchat.uikit.compose.presentation.imageviewer.style.CometChatImageViewerStyle

/**
 * Top-level composable for the full-screen image viewer.
 *
 * Composes the image preview with gesture handling, a toolbar overlay with
 * back and share actions, and a loading indicator. The background alpha
 * fades proportionally to the vertical drag distance during drag-to-dismiss.
 *
 * @param imageUrl Remote URL of the image to display
 * @param fileName Filename used for the share action
 * @param mimeType MIME type used for the share action
 * @param style Visual style configuration
 * @param onBack Called when the viewer should close (back button or dismiss gesture)
 * @param onShare Called when the share button is tapped
 */
@Composable
fun CometChatImageViewerScreen(
    imageUrl: String,
    fileName: String,
    mimeType: String,
    style: CometChatImageViewerStyle = CometChatImageViewerStyle.default(),
    onBack: () -> Unit,
    onShare: (url: String, fileName: String, mimeType: String) -> Unit
) {
    var backgroundAlpha by remember { mutableFloatStateOf(1f) }
    var toolbarVisible by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor.copy(alpha = backgroundAlpha))
    ) {
        CometChatImagePreview(
            imageUrl = imageUrl,
            modifier = Modifier.fillMaxSize(),
            style = style,
            onDragStart = { toolbarVisible = false },
            onDragEnd = { toolbarVisible = true },
            onDismiss = onBack,
            onDragProgress = { fraction ->
                // fraction is |dragOffsetY| / screenHeight from CometChatImagePreview
                backgroundAlpha = 1f - fraction
            },
            onLoadingStateChange = { loading -> isLoading = loading }
        )

        ToolbarOverlay(
            isVisible = toolbarVisible,
            style = style,
            onBackClick = onBack,
            onShareClick = { onShare(imageUrl, fileName, mimeType) }
        )

        if (isLoading) {
            CircularProgressIndicator(
                color = style.loadingIndicatorColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
