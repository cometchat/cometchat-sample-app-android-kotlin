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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFilesBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.formatFileSubtitle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.getFileType
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.getFileTypeIcon
import com.cometchat.uikit.core.constants.UIKitConstants

/** Number of file cards shown before the "+N more" toggle appears. */
private const val COLLAPSED_FILE_COUNT = 3

/**
 * Renders every non-media attachment of [message] as a vertical stack of file cards. When there are
 * more than [COLLAPSED_FILE_COUNT] files the extra cards collapse behind a "+N more" / "Show less"
 * toggle. Part of the ENG-36737 per-type multi-attachment bubbles.
 *
 * @param message The [MediaMessage] whose non-media attachments are rendered
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param style Style configuration for the file cards + caption
 * @param caption Optional caption (defaults to the message caption, shown under the cards)
 * @param showDownloadIcon Whether to show the per-card download icon
 * @param onFileClick Callback when a file card is tapped (index into the full file list)
 * @param onDownloadClick Callback when a card's download icon is tapped (index)
 * @param onLongClick Callback when the bubble is long-pressed
 */
@Composable
fun CometChatFilesBubble(
    message: MediaMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatFilesBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatFilesBubbleStyle.outgoing()
        else -> CometChatFilesBubbleStyle.incoming()
    },
    caption: String? = message.caption,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    showDownloadIcon: Boolean = true,
    onFileClick: ((Int) -> Unit)? = null,
    onDownloadClick: ((Int) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    // The renderer routes here for message type "file", so render every attachment as a file card
    // regardless of its MIME (e.g. an image sent as a "file" message still shows up).
    val files = remember(message.id) { resolveAttachments(message) }
    if (files.isEmpty()) return

    var expanded by remember(message.id) { mutableStateOf(false) }
    val canCollapse = files.size > COLLAPSED_FILE_COUNT
    val visibleCount = if (!canCollapse || expanded) files.size else COLLAPSED_FILE_COUNT
    val hiddenCount = files.size - visibleCount

    // Cards are a translucent OVERLAY on the message bubble, not an opaque panel (the
    // incoming/outgoing tint lives in the style factories). A single file needs no card
    // separation, so it sits directly on the bubble background regardless of the style.
    val cardColor = if (files.size == 1) Color.Transparent else style.cardBackgroundColor

    Column(
        modifier = modifier
            .width(MULTI_ATTACHMENT_BUBBLE_WIDTH)
            // Match the media bubbles' inset so files/images/videos in one batch align identically.
            // 0 bottom while the timestamp row below provides the visual gap; pad it when the row
            // is hidden (non-last batch bubble) so the cards aren't flush with the bubble edge.
            .padding(
                start = 5.dp,
                top = 5.dp,
                end = 5.dp,
                bottom = if (LocalTimestampHidden.current) 4.dp else 0.dp
            ),
        verticalArrangement = Arrangement.spacedBy(style.itemSpacing)
    ) {
        for (index in 0 until visibleCount) {
            FileCard(
                attachment = files[index],
                style = style,
                cardColor = cardColor,
                showDownloadIcon = showDownloadIcon,
                onClick = { onFileClick?.invoke(index) },
                onDownloadClick = { onDownloadClick?.invoke(index) },
                onLongClick = onLongClick
            )
        }

        if (canCollapse) {
            FileExpandToggle(
                expanded = expanded,
                hiddenCount = hiddenCount,
                style = style,
                cardColor = cardColor,
                onToggle = { expanded = !expanded }
            )
        }

        MultiAttachmentCaption(
            caption, style.captionTextColor, style.captionTextStyle,
            isEdited = message.editedAt > 0,
            message = message, alignment = alignment, textFormatters = textFormatters
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileCard(
    attachment: Attachment,
    style: CometChatFilesBubbleStyle,
    cardColor: Color,
    showDownloadIcon: Boolean,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onLongClick: (() -> Unit)?
) {
    val fileType = remember(attachment.fileMimeType, attachment.fileUrl) {
        getFileType(attachment.fileMimeType, attachment.fileUrl)
    }
    val icon = remember(fileType) { getFileTypeIcon(fileType) }
    val subtitle = remember(attachment.fileSize, attachment.fileName) {
        formatFileSubtitle(attachment.fileSize.toLong(), attachment.fileName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(style.cardCornerRadius))
            .background(cardColor)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(style.fileIconSize)
                .clip(RoundedCornerShape(style.fileIconCornerRadius))
                .background(style.fileIconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "File type: ${fileType.name}",
                modifier = Modifier.size(style.fileIconSize - 14.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.fileName ?: "Unknown file",
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

        if (showDownloadIcon) {
            Spacer(modifier = Modifier.size(8.dp))
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

@Composable
private fun FileExpandToggle(
    expanded: Boolean,
    hiddenCount: Int,
    style: CometChatFilesBubbleStyle,
    cardColor: Color,
    onToggle: () -> Unit
) {
    val label = if (expanded) stringResource(R.string.cometchat_show_less)
    else stringResource(R.string.cometchat_show_n_more, hiddenCount)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(style.cardCornerRadius))
            .background(cardColor)
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.cometchat_ic_arrow_down),
            contentDescription = null,
            tint = style.toggleTextColor,
            modifier = Modifier
                .size(18.dp)
                .rotate(if (expanded) 180f else 0f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = style.toggleTextStyle,
            color = style.toggleTextColor
        )
    }
}
