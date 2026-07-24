package com.cometchat.uikit.compose.presentation.imageviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
 * Renders the images in a [HorizontalPager] (one zoomable [CometChatImagePreview] per page) so a
 * multi-attachment grid preview can be swiped through, with a toolbar overlay whose share action
 * targets the currently visible page. The background alpha fades proportionally to the vertical
 * drag distance during drag-to-dismiss. Page swiping only competes with single-finger drags at
 * minimum zoom — a zoomed-in image pans instead of paging.
 *
 * @param imageUrls Urls of the images to display, in order
 * @param fileNames Filenames parallel to [imageUrls], used for the share action
 * @param mimeTypes MIME types parallel to [imageUrls], used for the share action
 * @param initialPage Index of the image shown first
 * @param style Visual style configuration
 * @param onBack Called when the viewer should close (back button or dismiss gesture)
 * @param onDownload Called when the download button is tapped, with the visible page's data
 * @param onShare Called when the share button is tapped, with the visible page's data
 */
@Composable
fun CometChatImageViewerScreen(
    imageUrls: List<String>,
    fileNames: List<String>,
    mimeTypes: List<String>,
    initialPage: Int = 0,
    style: CometChatImageViewerStyle = CometChatImageViewerStyle.default(),
    onBack: () -> Unit,
    onDownload: (url: String, fileName: String, mimeType: String) -> Unit = { _, _, _ -> },
    onShare: (url: String, fileName: String, mimeType: String) -> Unit
) {
    if (imageUrls.isEmpty()) return

    var backgroundAlpha by remember { mutableFloatStateOf(1f) }
    var toolbarVisible by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, imageUrls.lastIndex)
    ) { imageUrls.size }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor.copy(alpha = backgroundAlpha))
    ) {
        HorizontalPager(
            state = pagerState,
            // Keep the media within the system bar insets so the preview never bleeds
            // under the status/navigation bars (not a full-bleed, edge-to-edge preview).
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) { page ->
            // Non-image page (kind-mismatched attachment of a server-sent mixed payload) — show
            // the Google Drive-style "No preview available" page with its own Download button.
            // An EMPTY mime (legacy message) still tries the normal image preview.
            val pageMime = mimeTypes.getOrElse(page) { "" }.lowercase()
            if (pageMime.isNotEmpty() && !pageMime.startsWith("image/")) {
                NoPreviewPage(
                    onDownloadClick = {
                        onDownload(
                            imageUrls[page],
                            fileNames.getOrElse(page) { "" },
                            mimeTypes.getOrElse(page) { "" }
                        )
                    }
                )
                return@HorizontalPager
            }

            var isLoading by remember { mutableStateOf(true) }

            Box(modifier = Modifier.fillMaxSize()) {
                CometChatImagePreview(
                    imageUrl = imageUrls[page],
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

                if (isLoading) {
                    CircularProgressIndicator(
                        color = style.loadingIndicatorColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        ToolbarOverlay(
            isVisible = toolbarVisible,
            style = style,
            onBackClick = onBack,
            onDownloadClick = {
                val page = pagerState.currentPage
                onDownload(
                    imageUrls[page],
                    fileNames.getOrElse(page) { "" },
                    mimeTypes.getOrElse(page) { "" }
                )
            },
            onShareClick = {
                val page = pagerState.currentPage
                onShare(
                    imageUrls[page],
                    fileNames.getOrElse(page) { "" },
                    mimeTypes.getOrElse(page) { "" }
                )
            }
        )
    }
}

/**
 * Single-image overload kept for source compatibility — delegates to the pager-based viewer with
 * a one-element list.
 *
 * @param imageUrl Remote URL of the image to display
 * @param fileName Filename used for the share action
 * @param mimeType MIME type used for the share action
 * @param style Visual style configuration
 * @param onBack Called when the viewer should close (back button or dismiss gesture)
 * @param onDownload Called when the download button is tapped
 * @param onShare Called when the share button is tapped
 */
@Composable
fun CometChatImageViewerScreen(
    imageUrl: String,
    fileName: String,
    mimeType: String,
    style: CometChatImageViewerStyle = CometChatImageViewerStyle.default(),
    onBack: () -> Unit,
    onDownload: (url: String, fileName: String, mimeType: String) -> Unit = { _, _, _ -> },
    onShare: (url: String, fileName: String, mimeType: String) -> Unit
) {
    CometChatImageViewerScreen(
        imageUrls = listOf(imageUrl),
        fileNames = listOf(fileName),
        mimeTypes = listOf(mimeType),
        initialPage = 0,
        style = style,
        onBack = onBack,
        onDownload = onDownload,
        onShare = onShare
    )
}
