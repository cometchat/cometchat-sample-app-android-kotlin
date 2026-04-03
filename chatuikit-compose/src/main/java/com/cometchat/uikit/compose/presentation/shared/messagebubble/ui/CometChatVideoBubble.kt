package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideoBubbleStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants

private const val TAG = "CometChatVideoBubble"

/**
 * A composable that displays a video message bubble.
 *
 * This component renders video messages with support for:
 * - Single video display with thumbnail and play button overlay
 * - Multiple videos in a grid layout (2x2 with "+N" overlay for 5+ videos)
 * - Caption text
 * - Loading state with progress indicator
 *
 * Grid Layout Rules:
 * - 1 video: Full width single video
 * - 2 videos: 2 columns side by side
 * - 3-4 videos: 2x2 grid
 * - 5+ videos: 2x2 grid with "+N" overlay on 4th item
 *
 * Example usage:
 * ```kotlin
 * CometChatVideoBubble(
 *     message = mediaMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatVideoBubbleStyle.incoming()
 * )
 * ```
 *
 * @param message The [MediaMessage] containing video attachment(s)
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance
 * @param caption Optional caption text to display below the video(s)
 * @param onVideoClick Callback when a video is clicked (index, attachment)
 * @param onMoreClick Callback when the "+N" overlay is clicked (all attachments)
 * @param onPlayClick Callback when the play button is clicked (attachment)
 * @param onLongClick Callback when the bubble is long-pressed (propagates to parent message bubble)
 */
private const val MAX_VISIBLE_ITEMS = 4

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatVideoBubble(
    message: MediaMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatVideoBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatVideoBubbleStyle.outgoing()
        else -> CometChatVideoBubbleStyle.incoming()
    },
    caption: String? = message.caption,
    onVideoClick: ((Int, Attachment) -> Unit)? = null,
    onMoreClick: ((List<Attachment>) -> Unit)? = null,
    onPlayClick: ((Attachment) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    // Extract attachments from message
    val attachments = remember(message.id) {
        extractVideoAttachments(message)
    }

    // Extract local file path from metadata (for in-progress uploads)
    val localFilePath = remember(message.id) {
        getLocalFilePathFromMetadata(message)
    }

    // Extract thumbnail URL from metadata (for completed uploads)
    val thumbnailUrl = remember(message.id) {
        extractThumbnailUrlFromMetadata(message)
    }

    // Debug logging
    Log.d(TAG, "=== CometChatVideoBubble Debug ===")
    Log.d(TAG, "Message ID: ${message.id}")
    Log.d(TAG, "Attachments count: ${attachments.size}")
    Log.d(TAG, "Local file path: $localFilePath")
    Log.d(TAG, "Thumbnail URL from metadata: $thumbnailUrl")
    Log.d(TAG, "Attachment URL: ${attachments.firstOrNull()?.fileUrl}")
    Log.d(TAG, "Metadata: ${message.metadata}")

    CometChatVideoBubbleContent(
        attachments = attachments,
        style = style,
        caption = caption,
        localFilePath = localFilePath,
        thumbnailUrl = thumbnailUrl,
        onVideoClick = onVideoClick,
        onMoreClick = onMoreClick,
        onPlayClick = onPlayClick,
        onLongClick = onLongClick,
        modifier = modifier
    )
}

/**
 * Overload for displaying video bubble with a list of attachments directly.
 *
 * @param attachments List of video [Attachment]s to display
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance
 * @param caption Optional caption text to display below the video(s)
 * @param onVideoClick Callback when a video is clicked (index, attachment)
 * @param onMoreClick Callback when the "+N" overlay is clicked (all attachments)
 * @param onPlayClick Callback when the play button is clicked (attachment)
 * @param onLongClick Callback when the bubble is long-pressed (propagates to parent message bubble)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatVideoBubble(
    attachments: List<Attachment>,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatVideoBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatVideoBubbleStyle.outgoing()
        else -> CometChatVideoBubbleStyle.incoming()
    },
    caption: String? = null,
    onVideoClick: ((Int, Attachment) -> Unit)? = null,
    onMoreClick: ((List<Attachment>) -> Unit)? = null,
    onPlayClick: ((Attachment) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    CometChatVideoBubbleContent(
        attachments = attachments,
        style = style,
        caption = caption,
        onVideoClick = onVideoClick,
        onMoreClick = onMoreClick,
        onPlayClick = onPlayClick,
        onLongClick = onLongClick,
        modifier = modifier
    )
}

/**
 * Internal composable that renders the video bubble content.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CometChatVideoBubbleContent(
    attachments: List<Attachment>,
    style: CometChatVideoBubbleStyle,
    caption: String?,
    localFilePath: String? = null,
    thumbnailUrl: String? = null,
    onVideoClick: ((Int, Attachment) -> Unit)?,
    onMoreClick: ((List<Attachment>) -> Unit)?,
    onPlayClick: ((Attachment) -> Unit)?,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .padding(start = 4.dp, top = 4.dp, end = 4.dp)
            .semantics {
                contentDescription = "Video message with ${attachments.size} video(s)"
            }
    ) {
        when {
            attachments.isEmpty() -> {
                // No attachments - show placeholder
                SingleVideoView(
                    url = "",
                    style = style,
                    onClick = { },
                    onPlayClick = { },
                    onLongClick = onLongClick
                )
            }
            attachments.size == 1 -> {
                // Java flow (MessageBubbleUtils):
                // 1. Local file from metadata "path" → if exists, use it
                // 2. attachment.fileUrl (primary URL)
                // 3. If no local file AND thumbnail exists → override with thumbnail
                val attachmentUrl = getVideoThumbnailUrl(attachments[0])
                val effectiveUrl = when {
                    localFilePath != null -> localFilePath
                    thumbnailUrl != null -> thumbnailUrl
                    else -> attachmentUrl
                }
                // Fallback: if thumbnail URL fails (403), retry with attachment URL
                val fallbackUrl = if (localFilePath == null && thumbnailUrl != null) attachmentUrl else null
                
                Log.d(TAG, "=== SingleVideoView URL Resolution ===")
                Log.d(TAG, "Local file path: $localFilePath")
                Log.d(TAG, "Thumbnail URL from metadata: $thumbnailUrl")
                Log.d(TAG, "Attachment URL: $attachmentUrl")
                Log.d(TAG, "Effective URL: $effectiveUrl")
                Log.d(TAG, "Fallback URL: $fallbackUrl")
                
                SingleVideoView(
                    url = effectiveUrl,
                    fallbackUrl = fallbackUrl,
                    style = style,
                    onClick = { onVideoClick?.invoke(0, attachments[0]) },
                    onPlayClick = { onPlayClick?.invoke(attachments[0]) },
                    onLongClick = onLongClick
                )
            }
            else -> {
                // Multiple videos - grid layout
                VideoGrid(
                    attachments = attachments,
                    style = style,
                    onVideoClick = onVideoClick,
                    onMoreClick = onMoreClick,
                    onPlayClick = onPlayClick,
                    onLongClick = onLongClick
                )
            }
        }

        // Caption
        if (!caption.isNullOrEmpty()) {
            Text(
                text = caption,
                style = style.captionTextStyle,
                color = style.captionTextColor,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            )
        }
    }
}

/**
 * Extracts video attachments from a MediaMessage.
 * Checks both the single attachment and metadata for multiple attachments.
 */
private fun extractVideoAttachments(message: MediaMessage): List<Attachment> {
    // First check metadata for multiple attachments
    val metadataAttachments = extractAttachmentsFromMetadata(message)
    if (metadataAttachments != null && metadataAttachments.isNotEmpty()) {
        return metadataAttachments
    }

    // Fall back to single attachment
    val attachment = message.attachment
    return if (attachment != null) listOf(attachment) else emptyList()
}

/**
 * Extracts multiple attachments from message metadata if available.
 */
private fun extractAttachmentsFromMetadata(message: MediaMessage): List<Attachment>? {
    return try {
        val metadata = message.metadata ?: return null
        if (metadata.has("attachments")) {
            val attachmentsArray = metadata.getJSONArray("attachments")
            val result = mutableListOf<Attachment>()
            for (i in 0 until attachmentsArray.length()) {
                val attachmentJson = attachmentsArray.getJSONObject(i)
                val attachment = Attachment().apply {
                    fileUrl = attachmentJson.optString("url", "")
                    fileName = attachmentJson.optString("fileName", "")
                    fileExtension = attachmentJson.optString("extension", "")
                    fileMimeType = attachmentJson.optString("mimeType", "")
                    fileSize = attachmentJson.optLong("size", 0).toInt()
                }
                result.add(attachment)
            }
            if (result.isNotEmpty()) result else null
        } else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Gets the thumbnail URL for a video attachment.
 * For videos, we use the file URL as the thumbnail source.
 * In a real implementation, this might extract a frame or use a thumbnail service.
 */
private fun getVideoThumbnailUrl(attachment: Attachment): String {
    return attachment.fileUrl ?: ""
}

/**
 * Extracts the thumbnail URL from the message metadata if available.
 * Looks for the thumbnail-generation extension at:
 * metadata.@injected.extensions.thumbnail-generation.url_medium
 *
 * @param message The MediaMessage to extract thumbnail URL from
 * @return The thumbnail URL if found, null otherwise
 */
private fun extractThumbnailUrlFromMetadata(message: MediaMessage): String? {
    return try {
        val metadata = message.metadata ?: return null
        val injected = metadata.optJSONObject("@injected") ?: return null
        val extensions = injected.optJSONObject("extensions") ?: return null
        val thumbnailGeneration = extensions.optJSONObject("thumbnail-generation") ?: return null
        val urlMedium = thumbnailGeneration.optString("url_medium", null)
        if (urlMedium.isNullOrEmpty()) null else urlMedium
    } catch (e: Exception) {
        null
    }
}

/**
 * Extracts the local file path from the message metadata if available.
 * The local file path is stored in metadata.path during upload.
 *
 * @param message The MediaMessage to extract local file path from
 * @return The local file path if found and file exists, null otherwise
 */
private fun getLocalFilePathFromMetadata(message: MediaMessage): String? {
    return try {
        val metadata = message.metadata ?: return null
        val path = metadata.optString("path", null)
        if (path.isNullOrEmpty()) return null
        val file = java.io.File(path)
        if (file.exists()) path else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Composable for displaying a single video with thumbnail and play overlay.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SingleVideoView(
    url: String,
    style: CometChatVideoBubbleStyle,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    fallbackUrl: String? = null,
    onLongClick: (() -> Unit)? = null
) {
    var useFallback by remember(url) { mutableStateOf(false) }
    val effectiveUrl = if (useFallback && !fallbackUrl.isNullOrEmpty()) fallbackUrl else url
    
    Log.d(TAG, "SingleVideoView loading URL: $effectiveUrl (useFallback=$useFallback)")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(style.videoCornerRadius))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        // Video thumbnail
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(effectiveUrl)
                .crossfade(true)
                .listener(
                    onStart = { Log.d(TAG, "Coil: Started loading: $effectiveUrl") },
                    onSuccess = { _, _ -> Log.d(TAG, "Coil: Successfully loaded: $effectiveUrl") },
                    onError = { _, result ->
                        Log.e(TAG, "Coil: Failed to load: $effectiveUrl, error: ${result.throwable}")
                        if (!useFallback && !fallbackUrl.isNullOrEmpty()) {
                            Log.d(TAG, "Coil: Switching to fallback URL: $fallbackUrl")
                            useFallback = true
                        }
                    }
                )
                .build(),
            contentDescription = "Video thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = style.progressIndicatorColor
                    )
                }
            },
            error = {
                // Only show error if fallback also failed or no fallback available
                if (useFallback || fallbackUrl.isNullOrEmpty()) {
                    Log.e(TAG, "SubcomposeAsyncImage error state for URL: $effectiveUrl (fallback exhausted)")
                    VideoErrorPlaceholder(style = style)
                }
            }
        )

        // Play button overlay
        PlayButtonOverlay(
            style = style,
            onClick = onPlayClick,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Composable for displaying the video grid layout.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoGrid(
    attachments: List<Attachment>,
    style: CometChatVideoBubbleStyle,
    onVideoClick: ((Int, Attachment) -> Unit)?,
    onMoreClick: ((List<Attachment>) -> Unit)?,
    onPlayClick: ((Attachment) -> Unit)?,
    onLongClick: (() -> Unit)? = null
) {
    val visibleCount = minOf(attachments.size, MAX_VISIBLE_ITEMS)
    val hasMore = attachments.size > MAX_VISIBLE_ITEMS
    val moreCount = attachments.size - MAX_VISIBLE_ITEMS

    Column(
        verticalArrangement = Arrangement.spacedBy(style.gridSpacing)
    ) {
        when (visibleCount) {
            2 -> {
                // 2 videos: side by side
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridVideoItem(
                        attachment = attachments[0],
                        index = 0,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onVideoClick?.invoke(0, attachments[0]) },
                        onPlayClick = { onPlayClick?.invoke(attachments[0]) },
                        onLongClick = onLongClick
                    )
                    GridVideoItem(
                        attachment = attachments[1],
                        index = 1,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onVideoClick?.invoke(1, attachments[1]) },
                        onPlayClick = { onPlayClick?.invoke(attachments[1]) },
                        onLongClick = onLongClick
                    )
                }
            }
            3 -> {
                // 3 videos: 2 on top, 1 on bottom
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridVideoItem(
                        attachment = attachments[0],
                        index = 0,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onVideoClick?.invoke(0, attachments[0]) },
                        onPlayClick = { onPlayClick?.invoke(attachments[0]) },
                        onLongClick = onLongClick
                    )
                    GridVideoItem(
                        attachment = attachments[1],
                        index = 1,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onVideoClick?.invoke(1, attachments[1]) },
                        onPlayClick = { onPlayClick?.invoke(attachments[1]) },
                        onLongClick = onLongClick
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridVideoItem(
                        attachment = attachments[2],
                        index = 2,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onVideoClick?.invoke(2, attachments[2]) },
                        onPlayClick = { onPlayClick?.invoke(attachments[2]) },
                        onLongClick = onLongClick
                    )
                    // Empty space for symmetry
                    Box(modifier = Modifier.weight(1f))
                }
            }
            else -> {
                // 4+ videos: 2x2 grid
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridVideoItem(
                        attachment = attachments[0],
                        index = 0,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onVideoClick?.invoke(0, attachments[0]) },
                        onPlayClick = { onPlayClick?.invoke(attachments[0]) },
                        onLongClick = onLongClick
                    )
                    GridVideoItem(
                        attachment = attachments[1],
                        index = 1,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onVideoClick?.invoke(1, attachments[1]) },
                        onPlayClick = { onPlayClick?.invoke(attachments[1]) },
                        onLongClick = onLongClick
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridVideoItem(
                        attachment = attachments[2],
                        index = 2,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onVideoClick?.invoke(2, attachments[2]) },
                        onPlayClick = { onPlayClick?.invoke(attachments[2]) },
                        onLongClick = onLongClick
                    )
                    // Last item with potential "+N" overlay
                    GridVideoItem(
                        attachment = attachments[3],
                        index = 3,
                        style = style,
                        modifier = Modifier.weight(1f),
                        showMoreOverlay = hasMore,
                        moreCount = moreCount,
                        onClick = {
                            if (hasMore) {
                                onMoreClick?.invoke(attachments)
                            } else {
                                onVideoClick?.invoke(3, attachments[3])
                            }
                        },
                        onPlayClick = {
                            if (!hasMore) {
                                onPlayClick?.invoke(attachments[3])
                            }
                        },
                        onLongClick = onLongClick
                    )
                }
            }
        }
    }
}

/**
 * Composable for a single video item in the grid.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridVideoItem(
    attachment: Attachment,
    index: Int,
    style: CometChatVideoBubbleStyle,
    modifier: Modifier = Modifier,
    showMoreOverlay: Boolean = false,
    moreCount: Int = 0,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(style.videoCornerRadius))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        // Video thumbnail
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(getVideoThumbnailUrl(attachment))
                .crossfade(true)
                .build(),
            contentDescription = "Video ${index + 1}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = style.progressIndicatorColor,
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                VideoErrorPlaceholder(style = style)
            }
        )

        // Play button overlay (only show if not showing "+N" overlay)
        if (!showMoreOverlay) {
            PlayButtonOverlay(
                style = style,
                onClick = onPlayClick,
                modifier = Modifier.align(Alignment.Center),
                isSmall = true
            )
        }

        // "+N" overlay for more videos
        if (showMoreOverlay && moreCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(style.moreOverlayBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$moreCount",
                    style = style.moreOverlayTextStyle,
                    color = style.moreOverlayTextColor
                )
            }
        }
    }
}

/**
 * Composable for the play button overlay.
 */
@Composable
private fun PlayButtonOverlay(
    style: CometChatVideoBubbleStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSmall: Boolean = false
) {
    val size = if (isSmall) 36.dp else 56.dp
    val iconSize = if (isSmall) 20.dp else 28.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(style.playIconBackgroundColor)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Play video"
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.cometchat_play_icon),
            contentDescription = null,
            tint = style.playIconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Composable for the video error placeholder.
 */
@Composable
private fun VideoErrorPlaceholder(style: CometChatVideoBubbleStyle) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CometChatTheme.colorScheme.backgroundColor3),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.cometchat_ic_video_outlined),
            contentDescription = "Video unavailable",
            tint = CometChatTheme.colorScheme.iconTintTertiary,
            modifier = Modifier.size(48.dp)
        )
    }
}

/**
 * Data class representing grid configuration for video layout.
 */
data class VideoGridConfig(
    val columns: Int,
    val rows: Int,
    val showMore: Boolean,
    val moreCount: Int = 0
)

/**
 * Calculates the grid layout configuration based on attachment count.
 *
 * @param attachmentCount The number of video attachments
 * @return [VideoGridConfig] with the appropriate layout configuration
 */
fun calculateVideoGridLayout(attachmentCount: Int): VideoGridConfig {
    return when {
        attachmentCount == 1 -> VideoGridConfig(columns = 1, rows = 1, showMore = false)
        attachmentCount == 2 -> VideoGridConfig(columns = 2, rows = 1, showMore = false)
        attachmentCount == 3 -> VideoGridConfig(columns = 2, rows = 2, showMore = false)
        attachmentCount == 4 -> VideoGridConfig(columns = 2, rows = 2, showMore = false)
        else -> VideoGridConfig(columns = 2, rows = 2, showMore = true, moreCount = attachmentCount - MAX_VISIBLE_ITEMS)
    }
}
