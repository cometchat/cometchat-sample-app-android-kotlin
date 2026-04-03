package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFileBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.formatFileSubtitle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.getFileType
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.getFileTypeIcon

/**
 * A composable that displays a file message bubble.
 *
 * This component renders file attachments with support for:
 * - Single file display with file type icon, name, size, and download button
 * - Multiple files in a vertical stack layout
 * - "Download All" button for multi-file messages
 * - File type detection based on MIME type and extension
 *
 * Multi-File Layout Rules:
 * - Top item: 8dp top corners, 2dp bottom corners
 * - Middle items: 2dp all corners
 * - Bottom item: 2dp top corners, 8dp bottom corners
 * - 1dp gap between items
 *
 * Example usage:
 * ```kotlin
 * CometChatFileBubble(
 *     message = mediaMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatFileBubbleStyle.incoming()
 * )
 * ```
 *
 * @param message The [MediaMessage] containing file attachment(s)
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance. Since [CometChatFileBubbleStyle]
 *              extends [CometChatMessageBubbleStyle], all wrapper properties (backgroundColor,
 *              cornerRadius, etc.) are directly accessible on the style object.
 * @param showDownloadIcon Whether to show the download icon
 * @param onFileClick Callback when a file item is clicked (index)
 * @param onDownloadClick Callback when the download icon is clicked (index)
 * @param onDownloadAllClick Callback when the "Download All" button is clicked
 * @param onLongClick Callback when the bubble is long-pressed (propagates to parent message bubble)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatFileBubble(
    message: MediaMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatFileBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatFileBubbleStyle.outgoing()
        else -> CometChatFileBubbleStyle.incoming()
    },
    showDownloadIcon: Boolean = true,
    onFileClick: ((Int) -> Unit)? = null,
    onDownloadClick: ((Int) -> Unit)? = null,
    onDownloadAllClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    // Extract attachments from message
    val attachments = remember(message.id) {
        extractFileAttachments(message)
    }

    CometChatFileBubble(
        attachments = attachments,
        alignment = alignment,
        modifier = modifier,
        style = style,
        showDownloadIcon = showDownloadIcon,
        showDownloadAllButton = attachments.size > 1,
        onFileClick = onFileClick,
        onDownloadClick = onDownloadClick,
        onDownloadAllClick = onDownloadAllClick,
        onLongClick = onLongClick
    )
}

/**
 * Overload for displaying file bubble with a list of attachments directly.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatFileBubble(
    attachments: List<Attachment>,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatFileBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatFileBubbleStyle.outgoing()
        else -> CometChatFileBubbleStyle.incoming()
    },
    showDownloadIcon: Boolean = true,
    showDownloadAllButton: Boolean = attachments.size > 1,
    downloadAllButtonText: String = "Download All",
    onFileClick: ((Int) -> Unit)? = null,
    onDownloadClick: ((Int) -> Unit)? = null,
    onDownloadAllClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .semantics {
                contentDescription = "File message with ${attachments.size} file(s)"
            }
    ) {
        when {
            attachments.isEmpty() -> {
                // No attachments - show placeholder
                SingleFileItem(
                    fileName = "Unknown file",
                    fileSize = 0,
                    mimeType = null,
                    style = style,
                    showDownloadIcon = false,
                    cornerRadius = RoundedCornerShape(style.cornerRadius),
                    onClick = { },
                    onDownloadClick = { },
                    onLongClick = onLongClick
                )
            }
            attachments.size == 1 -> {
                // Single file
                val attachment = attachments[0]
                SingleFileItem(
                    fileName = attachment.fileName ?: "Unknown file",
                    fileSize = attachment.fileSize.toLong(),
                    mimeType = attachment.fileMimeType,
                    fileUrl = attachment.fileUrl,
                    style = style,
                    showDownloadIcon = showDownloadIcon,
                    cornerRadius = RoundedCornerShape(style.cornerRadius),
                    onClick = { onFileClick?.invoke(0) },
                    onDownloadClick = { onDownloadClick?.invoke(0) },
                    onLongClick = onLongClick
                )
            }
            else -> {
                // Multiple files - vertical stack
                Column(
                    verticalArrangement = Arrangement.spacedBy(style.itemSpacing)
                ) {
                    attachments.forEachIndexed { index, attachment ->
                        val itemCornerRadius = getItemCornerRadius(
                            index = index,
                            totalCount = attachments.size,
                            outerRadius = style.cornerRadius,
                            innerRadius = style.innerCornerRadius
                        )
                        
                        SingleFileItem(
                            fileName = attachment.fileName ?: "Unknown file",
                            fileSize = attachment.fileSize.toLong(),
                            mimeType = attachment.fileMimeType,
                            fileUrl = attachment.fileUrl,
                            style = style,
                            showDownloadIcon = showDownloadIcon,
                            cornerRadius = itemCornerRadius,
                            onClick = { onFileClick?.invoke(index) },
                            onDownloadClick = { onDownloadClick?.invoke(index) },
                            onLongClick = onLongClick
                        )
                    }
                }

                // Download All button
                if (showDownloadAllButton) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onDownloadAllClick?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(style.downloadAllButtonHeight),
                        shape = RoundedCornerShape(style.downloadAllButtonCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = style.downloadAllButtonBackgroundColor
                        )
                    ) {
                        Text(
                            text = downloadAllButtonText,
                            style = style.downloadAllButtonTextStyle,
                            color = style.downloadAllButtonTextColor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SingleFileItem(
    fileName: String,
    fileSize: Long,
    mimeType: String?,
    fileUrl: String? = null,
    style: CometChatFileBubbleStyle,
    showDownloadIcon: Boolean,
    cornerRadius: Shape,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val fileType = remember(mimeType, fileUrl) {
        getFileType(mimeType, fileUrl)
    }
    val fileIcon = remember(fileType) {
        getFileTypeIcon(fileType)
    }
    val subtitle = remember(fileSize, fileName) {
        formatFileSubtitle(fileSize, fileName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cornerRadius)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(start = 8.dp, top = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File type icon with background
        Box(
            modifier = Modifier
                .size(style.fileIconSize)
                .clip(RoundedCornerShape(style.fileIconCornerRadius))
                .background(style.fileIconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = fileIcon),
                contentDescription = "File type: ${fileType.name}",
                modifier = Modifier.size(style.fileIconSize - 8.dp),
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // File info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = fileName,
                style = style.titleTextStyle,
                color = style.titleTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = style.subtitleTextStyle,
                color = style.subtitleTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Download icon
        if (showDownloadIcon) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.cometchat_download_icon),
                contentDescription = "Download file",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onDownloadClick),
                tint = style.downloadIconTint
            )
        }
    }
}

/**
 * Calculates the corner radius for a file item based on its position in the list.
 */
private fun getItemCornerRadius(
    index: Int,
    totalCount: Int,
    outerRadius: androidx.compose.ui.unit.Dp,
    innerRadius: androidx.compose.ui.unit.Dp
): RoundedCornerShape {
    return when {
        totalCount == 1 -> RoundedCornerShape(outerRadius)
        index == 0 -> RoundedCornerShape(
            topStart = outerRadius,
            topEnd = outerRadius,
            bottomStart = innerRadius,
            bottomEnd = innerRadius
        )
        index == totalCount - 1 -> RoundedCornerShape(
            topStart = innerRadius,
            topEnd = innerRadius,
            bottomStart = outerRadius,
            bottomEnd = outerRadius
        )
        else -> RoundedCornerShape(innerRadius)
    }
}

/**
 * Extracts file attachments from a MediaMessage.
 * Checks both the single attachment and metadata for multiple attachments.
 */
private fun extractFileAttachments(message: MediaMessage): List<Attachment> {
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
