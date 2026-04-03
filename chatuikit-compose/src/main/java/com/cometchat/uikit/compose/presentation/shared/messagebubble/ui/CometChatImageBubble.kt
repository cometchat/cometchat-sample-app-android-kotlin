package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImageBubbleStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import org.json.JSONObject

private const val TAG = "CometChatImageBubble"

/**
 * A composable that displays an image message bubble.
 *
 * This component renders image messages with support for:
 * - Single image display
 * - Multiple images in a grid layout (2x2 with "+N" overlay for 5+ images)
 * - Caption text
 * - Loading state with progress indicator
 * - GIF support
 *
 * Grid Layout Rules:
 * - 1 image: Full width single image
 * - 2 images: 2 columns side by side
 * - 3-4 images: 2x2 grid
 * - 5+ images: 2x2 grid with "+N" overlay on 4th item
 *
 * Example usage:
 * ```kotlin
 * CometChatImageBubble(
 *     message = mediaMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatImageBubbleStyle.incoming()
 * )
 * ```
 *
 * @param message The [MediaMessage] containing image attachment(s)
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance
 * @param onImageClick Callback when an image is clicked (index, attachment)
 * @param onMoreClick Callback when the "+N" overlay is clicked (all attachments)
 * @param onLongClick Callback when the bubble is long-pressed (propagates to parent message bubble)
 */
private const val MAX_VISIBLE_ITEMS = 4

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatImageBubble(
    message: MediaMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatImageBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatImageBubbleStyle.outgoing()
        else -> CometChatImageBubbleStyle.incoming()
    },
    caption: String? = message.caption,
    onImageClick: ((Int, Attachment) -> Unit)? = null,
    onMoreClick: ((List<Attachment>) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    // Extract attachments from message
    val attachments = remember(message.id) {
        extractAttachments(message)
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
    Log.d(TAG, "=== CometChatImageBubble Debug ===")
    Log.d(TAG, "Message ID: ${message.id}")
    Log.d(TAG, "Attachments count: ${attachments.size}")
    Log.d(TAG, "Local file path: $localFilePath")
    Log.d(TAG, "Thumbnail URL from metadata: $thumbnailUrl")
    Log.d(TAG, "Attachment URL: ${attachments.firstOrNull()?.fileUrl}")
    Log.d(TAG, "Metadata: ${message.metadata}")

    Column(
        modifier = modifier
            .width(240.dp)
            .padding(start = 4.dp, top = 4.dp, end = 4.dp)
            .semantics {
                contentDescription = "Image message with ${attachments.size} image(s)"
            }
    ) {
        when {
            attachments.isEmpty() -> {
                // No attachments - show placeholder
                SingleImageView(
                    url = "",
                    style = style,
                    onClick = { },
                    onLongClick = onLongClick
                )
            }
            attachments.size == 1 -> {
                // Java flow (MessageBubbleUtils):
                // 1. Local file from metadata "path" → if exists, use it
                // 2. attachment.fileUrl (primary URL)
                // 3. If no local file AND thumbnail exists → override with thumbnail
                val attachmentUrl = attachments[0].fileUrl ?: ""
                val effectiveUrl = when {
                    localFilePath != null -> localFilePath
                    thumbnailUrl != null -> thumbnailUrl
                    else -> attachmentUrl
                }
                // Fallback: if thumbnail URL fails (403), retry with attachment URL
                val fallbackUrl = if (localFilePath == null && thumbnailUrl != null) attachmentUrl else null
                
                Log.d(TAG, "=== SingleImageView URL Resolution ===")
                Log.d(TAG, "Local file path: $localFilePath")
                Log.d(TAG, "Thumbnail URL from metadata: $thumbnailUrl")
                Log.d(TAG, "Attachment URL: $attachmentUrl")
                Log.d(TAG, "Effective URL: $effectiveUrl")
                Log.d(TAG, "Fallback URL: $fallbackUrl")
                
                SingleImageView(
                    url = effectiveUrl,
                    fallbackUrl = fallbackUrl,
                    style = style,
                    onClick = { onImageClick?.invoke(0, attachments[0]) },
                    onLongClick = onLongClick
                )
            }
            else -> {
                // Multiple images - grid layout
                ImageGrid(
                    attachments = attachments,
                    style = style,
                    onImageClick = onImageClick,
                    onMoreClick = onMoreClick,
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
 * Extracts attachments from a MediaMessage.
 * Checks both the single attachment and metadata for multiple attachments.
 */
private fun extractAttachments(message: MediaMessage): List<Attachment> {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SingleImageView(
    url: String,
    style: CometChatImageBubbleStyle,
    onClick: () -> Unit,
    fallbackUrl: String? = null,
    onLongClick: (() -> Unit)? = null
) {
    var useFallback by remember(url) { mutableStateOf(false) }
    val effectiveUrl = if (useFallback && !fallbackUrl.isNullOrEmpty()) fallbackUrl else url
    
    Log.d(TAG, "SingleImageView loading URL: $effectiveUrl (useFallback=$useFallback)")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(224.dp)
            .clip(RoundedCornerShape(style.imageCornerRadius))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
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
            contentDescription = "Image",
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CometChatTheme.colorScheme.backgroundColor3),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Failed to load",
                            style = CometChatTheme.typography.caption1Regular,
                            color = CometChatTheme.colorScheme.textColorTertiary
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageGrid(
    attachments: List<Attachment>,
    style: CometChatImageBubbleStyle,
    onImageClick: ((Int, Attachment) -> Unit)?,
    onMoreClick: ((List<Attachment>) -> Unit)?,
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
                // 2 images: side by side
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridImageItem(
                        attachment = attachments[0],
                        index = 0,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onImageClick?.invoke(0, attachments[0]) },
                        onLongClick = onLongClick
                    )
                    GridImageItem(
                        attachment = attachments[1],
                        index = 1,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onImageClick?.invoke(1, attachments[1]) },
                        onLongClick = onLongClick
                    )
                }
            }
            3 -> {
                // 3 images: 2 on top, 1 on bottom
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridImageItem(
                        attachment = attachments[0],
                        index = 0,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onImageClick?.invoke(0, attachments[0]) },
                        onLongClick = onLongClick
                    )
                    GridImageItem(
                        attachment = attachments[1],
                        index = 1,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onImageClick?.invoke(1, attachments[1]) },
                        onLongClick = onLongClick
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridImageItem(
                        attachment = attachments[2],
                        index = 2,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onImageClick?.invoke(2, attachments[2]) },
                        onLongClick = onLongClick
                    )
                    // Empty space for symmetry
                    Box(modifier = Modifier.weight(1f))
                }
            }
            else -> {
                // 4+ images: 2x2 grid
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridImageItem(
                        attachment = attachments[0],
                        index = 0,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onImageClick?.invoke(0, attachments[0]) },
                        onLongClick = onLongClick
                    )
                    GridImageItem(
                        attachment = attachments[1],
                        index = 1,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onImageClick?.invoke(1, attachments[1]) },
                        onLongClick = onLongClick
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GridImageItem(
                        attachment = attachments[2],
                        index = 2,
                        style = style,
                        modifier = Modifier.weight(1f),
                        onClick = { onImageClick?.invoke(2, attachments[2]) },
                        onLongClick = onLongClick
                    )
                    // Last item with potential "+N" overlay
                    GridImageItem(
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
                                onImageClick?.invoke(3, attachments[3])
                            }
                        },
                        onLongClick = onLongClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridImageItem(
    attachment: Attachment,
    index: Int,
    style: CometChatImageBubbleStyle,
    modifier: Modifier = Modifier,
    showMoreOverlay: Boolean = false,
    moreCount: Int = 0,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(style.imageCornerRadius))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(attachment.fileUrl ?: "")
                .crossfade(true)
                .build(),
            contentDescription = "Image ${index + 1}",
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CometChatTheme.colorScheme.backgroundColor3),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        style = CometChatTheme.typography.bodyMedium,
                        color = CometChatTheme.colorScheme.textColorTertiary
                    )
                }
            }
        )

        // "+N" overlay for more images
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
 * Overload for displaying image bubble with a list of attachments directly.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatImageBubble(
    attachments: List<Attachment>,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatImageBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatImageBubbleStyle.outgoing()
        else -> CometChatImageBubbleStyle.incoming()
    },
    caption: String? = null,
    onImageClick: ((Int, Attachment) -> Unit)? = null,
    onMoreClick: ((List<Attachment>) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .widthIn(max = style.maxGridWidth)
            .semantics {
                contentDescription = "Image message with ${attachments.size} image(s)"
            }
    ) {
        when {
            attachments.isEmpty() -> {
                SingleImageView(
                    url = "",
                    style = style,
                    onClick = { },
                    onLongClick = onLongClick
                )
            }
            attachments.size == 1 -> {
                SingleImageView(
                    url = attachments[0].fileUrl ?: "",
                    style = style,
                    onClick = { onImageClick?.invoke(0, attachments[0]) },
                    onLongClick = onLongClick
                )
            }
            else -> {
                ImageGrid(
                    attachments = attachments,
                    style = style,
                    onImageClick = onImageClick,
                    onMoreClick = onMoreClick,
                    onLongClick = onLongClick
                )
            }
        }

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
