package com.cometchat.uikit.compose.presentation.imageviewer.ui

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cometchat.uikit.compose.presentation.imageviewer.style.CometChatImageViewerStyle

/**
 * Top-level composable for the in-app (non full-screen) video player.
 *
 * Renders the videos in a [HorizontalPager] (one framework [VideoView] per page, no extra player
 * dependency) so a multi-attachment message can be swiped through, mirroring the image viewer.
 * Only the current page plays; the rest are paused as you swipe. The media is constrained within
 * the system bar insets so the preview never bleeds under the status/navigation bars. A
 * [ToolbarOverlay] provides back, download and share actions targeting the visible page; the
 * download button is shown by default and uses the share glyph unless overridden via
 * [CometChatImageViewerStyle.downloadIcon].
 *
 * @param videoUrls Urls of the videos to play, in order
 * @param fileNames Filenames parallel to [videoUrls], used for the download/share actions
 * @param mimeTypes MIME types parallel to [videoUrls], used for the share action
 * @param initialPage Index of the video shown first
 * @param style Visual style configuration (shared with the image viewer)
 * @param onBack Called when the viewer should close (back button)
 * @param onDownload Called when the download button is tapped, with the visible page's data
 * @param onShare Called when the share button is tapped, with the visible page's data
 */
@Composable
fun CometChatVideoViewerScreen(
    videoUrls: List<String>,
    fileNames: List<String>,
    mimeTypes: List<String>,
    initialPage: Int = 0,
    style: CometChatImageViewerStyle = CometChatImageViewerStyle.default(),
    onBack: () -> Unit,
    onDownload: (url: String, fileName: String, mimeType: String) -> Unit = { _, _, _ -> },
    onShare: (url: String, fileName: String, mimeType: String) -> Unit
) {
    if (videoUrls.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, videoUrls.lastIndex)
    ) { videoUrls.size }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // Non-video page (kind-mismatched attachment of a server-sent mixed payload) — show
            // the Google Drive-style "No preview available" page with its own Download button.
            // An EMPTY mime (legacy message) still tries the normal video playback.
            val pageMime = mimeTypes.getOrElse(page) { "" }.lowercase()
            if (pageMime.isNotEmpty() && !pageMime.startsWith("video/")) {
                NoPreviewPage(
                    onDownloadClick = {
                        onDownload(
                            videoUrls[page],
                            fileNames.getOrElse(page) { "" },
                            mimeTypes.getOrElse(page) { "" }
                        )
                    }
                )
                return@HorizontalPager
            }

            var isLoading by remember { mutableStateOf(true) }
            val isCurrent = page == pagerState.currentPage

            // The page's VideoView, captured so the lifecycle observer can pause/resume it.
            var videoView by remember { mutableStateOf<VideoView?>(null) }
            // Read the latest current-page flag inside the (once-created) observer without
            // re-registering it on every swipe.
            val isCurrentState by rememberUpdatedState(isCurrent)
            val lifecycleOwner = LocalLifecycleOwner.current

            // Pause playback when the host activity is backgrounded (Home / screen off) so audio
            // doesn't keep playing off-screen, and resume the visible page on return.
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> videoView?.pause()
                        Lifecycle.Event.ON_RESUME -> if (isCurrentState) videoView?.start()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        VideoView(context).apply {
                            val controller = MediaController(context)
                            controller.setAnchorView(this)
                            setMediaController(controller)
                            setVideoURI(Uri.parse(videoUrls[page]))
                            setOnPreparedListener { isLoading = false }
                            setOnErrorListener { _, _, _ ->
                                isLoading = false
                                false
                            }
                        }.also { videoView = it }
                    },
                    // VideoView's target-state machine plays/pauses even before it is prepared, so
                    // driving playback from the current-page flag is enough to keep only the
                    // visible video playing as the user swipes.
                    update = { view ->
                        if (isCurrent) view.start() else view.pause()
                    },
                    // Release the underlying MediaPlayer when this page leaves composition (pager
                    // recycles pages beyond its off-screen limit) so it doesn't leak.
                    onRelease = { view ->
                        view.stopPlayback()
                        if (videoView === view) videoView = null
                    },
                    // Keep the media within the system bar insets (not a full-bleed preview).
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
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
            isVisible = true,
            style = style,
            onBackClick = onBack,
            onDownloadClick = {
                val page = pagerState.currentPage
                onDownload(
                    videoUrls[page],
                    fileNames.getOrElse(page) { "" },
                    mimeTypes.getOrElse(page) { "" }
                )
            },
            onShareClick = {
                val page = pagerState.currentPage
                onShare(
                    videoUrls[page],
                    fileNames.getOrElse(page) { "" },
                    mimeTypes.getOrElse(page) { "" }
                )
            }
        )
    }
}

/**
 * Single-video overload kept for source compatibility — delegates to the pager-based viewer with
 * a one-element list.
 *
 * @param videoUrl Remote URL of the video to play
 * @param fileName Filename used for the download/share actions
 * @param mimeType MIME type used for the share action
 * @param style Visual style configuration
 * @param onBack Called when the viewer should close (back button)
 * @param onDownload Called when the download button is tapped
 * @param onShare Called when the share button is tapped
 */
@Composable
fun CometChatVideoViewerScreen(
    videoUrl: String,
    fileName: String,
    mimeType: String,
    style: CometChatImageViewerStyle = CometChatImageViewerStyle.default(),
    onBack: () -> Unit,
    onDownload: (url: String, fileName: String, mimeType: String) -> Unit = { _, _, _ -> },
    onShare: (url: String, fileName: String, mimeType: String) -> Unit
) {
    CometChatVideoViewerScreen(
        videoUrls = listOf(videoUrl),
        fileNames = listOf(fileName),
        mimeTypes = listOf(mimeType),
        initialPage = 0,
        style = style,
        onBack = onBack,
        onDownload = onDownload,
        onShare = onShare
    )
}
