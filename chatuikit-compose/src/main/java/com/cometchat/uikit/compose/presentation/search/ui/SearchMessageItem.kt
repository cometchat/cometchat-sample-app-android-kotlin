package com.cometchat.uikit.compose.presentation.search.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.search.style.SearchMessageItemStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val THUMBNAIL_SIZE = 70
private const val FILE_ICON_SIZE = 48
private const val ICON_SIZE = 32

/**
 * A composable that displays a message search result item.
 */
@Composable
fun SearchMessageItem(
    message: BaseMessage,
    onClick: (BaseMessage) -> Unit,
    modifier: Modifier = Modifier,
    style: SearchMessageItemStyle = SearchMessageItemStyle.default(),
    textFormatters: List<com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter> = emptyList(),
    uid: String? = null,
    guid: String? = null,
    textMessageView: (@Composable (TextMessage) -> Unit)? = null,
    imageMessageView: (@Composable (MediaMessage) -> Unit)? = null,
    videoMessageView: (@Composable (MediaMessage) -> Unit)? = null,
    audioMessageView: (@Composable (MediaMessage) -> Unit)? = null,
    documentMessageView: (@Composable (MediaMessage) -> Unit)? = null,
    linkMessageView: (@Composable (TextMessage) -> Unit)? = null
) {
    val senderName = message.sender?.name ?: ""
    val accessibilityDescription = buildAccessibilityDescription(message, senderName)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(style.backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onClick(message) }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = accessibilityDescription
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        MessageContent(
            message = message,
            style = style,
            textFormatters = textFormatters,
            uid = uid,
            guid = guid,
            textMessageView = textMessageView,
            imageMessageView = imageMessageView,
            videoMessageView = videoMessageView,
            audioMessageView = audioMessageView,
            documentMessageView = documentMessageView,
            linkMessageView = linkMessageView,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Only show trailing section (timestamp + thread) for non-image/video messages
        // Image and video items have the thumbnail on the right instead (matching reference)
        if (message.type != CometChatConstants.MESSAGE_TYPE_IMAGE &&
            message.type != CometChatConstants.MESSAGE_TYPE_VIDEO) {
            TrailingSection(message = message, style = style)
        }
    }
}

@Composable
private fun MessageContent(
    message: BaseMessage,
    style: SearchMessageItemStyle,
    textFormatters: List<com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter> = emptyList(),
    uid: String? = null,
    guid: String? = null,
    textMessageView: (@Composable (TextMessage) -> Unit)?,
    imageMessageView: (@Composable (MediaMessage) -> Unit)?,
    videoMessageView: (@Composable (MediaMessage) -> Unit)?,
    audioMessageView: (@Composable (MediaMessage) -> Unit)?,
    documentMessageView: (@Composable (MediaMessage) -> Unit)?,
    linkMessageView: (@Composable (TextMessage) -> Unit)?,
    modifier: Modifier = Modifier
) {
    when (message.type) {
        CometChatConstants.MESSAGE_TYPE_TEXT -> {
            val textMessage = message as? TextMessage
            if (textMessage != null) {
                if (hasLink(textMessage.text)) {
                    if (linkMessageView != null) {
                        linkMessageView(textMessage)
                    } else {
                        LinkMessageContent(message = textMessage, style = style, modifier = modifier)
                    }
                } else {
                    if (textMessageView != null) {
                        textMessageView(textMessage)
                    } else {
                        TextMessageContent(message = textMessage, style = style, textFormatters = textFormatters, uid = uid, guid = guid, modifier = modifier)
                    }
                }
            }
        }
        CometChatConstants.MESSAGE_TYPE_IMAGE -> {
            val mediaMessage = message as? MediaMessage
            if (mediaMessage != null) {
                if (imageMessageView != null) {
                    imageMessageView(mediaMessage)
                } else {
                    ImageMessageContent(message = mediaMessage, style = style, modifier = modifier)
                }
            }
        }
        CometChatConstants.MESSAGE_TYPE_VIDEO -> {
            val mediaMessage = message as? MediaMessage
            if (mediaMessage != null) {
                if (videoMessageView != null) {
                    videoMessageView(mediaMessage)
                } else {
                    VideoMessageContent(message = mediaMessage, style = style, modifier = modifier)
                }
            }
        }
        CometChatConstants.MESSAGE_TYPE_AUDIO -> {
            val mediaMessage = message as? MediaMessage
            if (mediaMessage != null) {
                if (audioMessageView != null) {
                    audioMessageView(mediaMessage)
                } else {
                    AudioMessageContent(message = mediaMessage, style = style, modifier = modifier)
                }
            }
        }
        CometChatConstants.MESSAGE_TYPE_FILE -> {
            val mediaMessage = message as? MediaMessage
            if (mediaMessage != null) {
                if (documentMessageView != null) {
                    documentMessageView(mediaMessage)
                } else {
                    DocumentMessageContent(message = mediaMessage, style = style, modifier = modifier)
                }
            }
        }
        else -> {
            DefaultMessageContent(message = message, style = style, modifier = modifier)
        }
    }
}

@Composable
private fun TextMessageContent(
    message: TextMessage,
    style: SearchMessageItemStyle,
    textFormatters: List<com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter> = emptyList(),
    uid: String? = null,
    guid: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Apply text formatters for rich text (mentions, etc.) matching reference
    val formattedText: androidx.compose.ui.text.AnnotatedString = if (textFormatters.isNotEmpty() && !message.text.isNullOrEmpty()) {
        com.cometchat.uikit.compose.presentation.shared.formatters.FormatterUtils.getFormattedText(
            context = context,
            baseMessage = message,
            formattingType = com.cometchat.uikit.core.constants.UIKitConstants.FormattingType.CONVERSATIONS,
            alignment = com.cometchat.uikit.core.constants.UIKitConstants.MessageBubbleAlignment.LEFT,
            text = message.text,
            formatters = textFormatters
        )
    } else {
        androidx.compose.ui.text.AnnotatedString(message.text ?: "")
    }

    // Build subtitle with sender prefix matching reference:
    // When scoped (uid/guid set): no prefix
    // When not scoped: ALWAYS add "SenderName: " or "You: " prefix
    val subtitle: androidx.compose.ui.text.AnnotatedString = if (uid == null && guid == null) {
        val currentUser = try { com.cometchat.uikit.core.CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
        val senderName = if (currentUser != null && message.sender?.uid == currentUser.uid) {
            "You"
        } else {
            message.sender?.name ?: ""
        }
        if (senderName.isNotEmpty()) {
            androidx.compose.ui.text.buildAnnotatedString {
                append("$senderName: ")
                append(formattedText)
            }
        } else {
            formattedText
        }
    } else {
        // Scoped search — no sender prefix
        formattedText
    }

    // Title: matching reference getConversationTitle logic
    val title = if (uid != null || guid != null) {
        val currentUser = try { com.cometchat.uikit.core.CometChatUIKit.getLoggedInUser() } catch (e: Exception) { null }
        if (message.sender?.uid == currentUser?.uid) "You" else message.sender?.name ?: ""
    } else {
        val receiver = message.receiver
        if (receiver is com.cometchat.chat.models.Group) {
            receiver.name ?: ""
        } else {
            message.sender?.name ?: ""
        }
    }

    Column(modifier = modifier) {
        Text(
            text = title,
            color = style.titleTextColor,
            style = style.titleTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            text = subtitle,
            color = style.subtitleTextColor,
            style = style.subtitleTextStyle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ImageMessageContent(
    message: MediaMessage,
    style: SearchMessageItemStyle,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Text content on LEFT (matching reference layout)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.sender?.name ?: "",
                color = style.titleTextColor,
                style = style.titleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = message.attachment?.fileName ?: "Photo",
                color = style.subtitleTextColor,
                style = style.subtitleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Thumbnail on RIGHT (matching reference: 70dp in MaterialCardView)
        androidx.compose.material3.Card(
            modifier = Modifier.size(THUMBNAIL_SIZE.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(message.attachment?.fileUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image",
                modifier = Modifier
                    .size(THUMBNAIL_SIZE.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun VideoMessageContent(
    message: MediaMessage,
    style: SearchMessageItemStyle,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Text content on LEFT (matching reference layout)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.sender?.name ?: "",
                color = style.titleTextColor,
                style = style.titleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = message.attachment?.fileName ?: "Video",
                color = style.subtitleTextColor,
                style = style.subtitleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Thumbnail + play button on RIGHT (matching reference: 70dp in MaterialCardView)
        androidx.compose.material3.Card(
            modifier = Modifier.size(THUMBNAIL_SIZE.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier.size(THUMBNAIL_SIZE.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(message.attachment?.fileUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Video thumbnail",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Icon(
                    painter = painterResource(R.drawable.cometchat_play_icon),
                    contentDescription = "Play",
                    tint = style.titleTextColor,
                    modifier = Modifier.size(ICON_SIZE.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioMessageContent(
    message: MediaMessage,
    style: SearchMessageItemStyle,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.cometchat_ic_audio),
            contentDescription = "Audio",
            modifier = Modifier.size(FILE_ICON_SIZE.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.sender?.name ?: "",
                color = style.titleTextColor,
                style = style.titleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = message.attachment?.fileName ?: "Audio",
                color = style.subtitleTextColor,
                style = style.subtitleTextStyle,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DocumentMessageContent(
    message: MediaMessage,
    style: SearchMessageItemStyle,
    modifier: Modifier = Modifier
) {
    val fileIconRes = getDocumentFileIcon(message)

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(fileIconRes),
            contentDescription = "Document",
            modifier = Modifier.size(FILE_ICON_SIZE.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.sender?.name ?: "",
                color = style.titleTextColor,
                style = style.titleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = message.attachment?.fileName ?: "Document",
                color = style.subtitleTextColor,
                style = style.subtitleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LinkMessageContent(
    message: TextMessage,
    style: SearchMessageItemStyle,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = message.sender?.name ?: "",
            color = style.titleTextColor,
            style = style.titleTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            text = message.text ?: "",
            color = style.linkTextColor,
            style = style.linkTextStyle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DefaultMessageContent(
    message: BaseMessage,
    style: SearchMessageItemStyle,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = message.sender?.name ?: "",
            color = style.titleTextColor,
            style = style.titleTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            text = "Message",
            color = style.subtitleTextColor,
            style = style.subtitleTextStyle,
            maxLines = 1
        )
    }
}

@Composable
private fun TrailingSection(
    message: BaseMessage,
    style: SearchMessageItemStyle
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        val timestamp = message.updatedAt
        if (timestamp > 0) {
            Text(
                text = formatTimestamp(timestamp),
                color = style.timestampTextColor,
                style = style.timestampTextStyle,
                maxLines = 1
            )
        }

        if (message.parentMessageId > 0) {
            Spacer(modifier = Modifier.size(4.dp))
            val threadIcon = style.threadIcon ?: painterResource(R.drawable.cometchat_ic_thread)
            Icon(
                painter = threadIcon,
                contentDescription = "Thread",
                tint = style.threadIconTint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun hasLink(text: String?): Boolean {
    if (text.isNullOrEmpty()) return false
    val urlPattern = "(https?://[\\w\\-._~:/?#\\[\\]@!'()*+,;=%]+)".toRegex()
    return urlPattern.containsMatchIn(text)
}

private fun formatTimestamp(timestamp: Long): String {
    // Match Java reference: always "dd MMM, yyyy" using updatedAt
    val date = Date(timestamp * 1000)
    return SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(date)
}

/**
 * Returns the appropriate file icon drawable resource based on the document's MIME type.
 * Matches the Java reference implementation's file type detection logic.
 */
private fun getDocumentFileIcon(message: MediaMessage): Int {
    val attachment = message.attachment ?: return R.drawable.cometchat_unknown_file_icon
    val mimeType = attachment.fileMimeType ?: return R.drawable.cometchat_unknown_file_icon
    val fileUrl = attachment.fileUrl ?: ""

    return when {
        mimeType.contains("video") -> R.drawable.cometchat_video_file_icon
        mimeType.contains("octet-stream") -> {
            when {
                fileUrl.endsWith(".doc") || fileUrl.endsWith(".docx") -> R.drawable.cometchat_word_file_icon
                fileUrl.endsWith(".ppt") || fileUrl.endsWith(".pptx") -> R.drawable.cometchat_ppt_file_icon
                fileUrl.endsWith(".xls") || fileUrl.endsWith(".xlsx") -> R.drawable.cometchat_xlsx_file_icon
                else -> R.drawable.cometchat_unknown_file_icon
            }
        }
        mimeType.contains("pdf") -> R.drawable.cometchat_pdf_file_icon
        mimeType.contains("zip") -> R.drawable.cometchat_zip_file_icon
        fileUrl.contains(".csv") -> R.drawable.cometchat_text_file_icon
        mimeType.contains("audio") -> R.drawable.cometchat_audio_file_icon
        mimeType.contains("image") -> R.drawable.cometchat_image_file_icon
        mimeType.contains("text") -> R.drawable.cometchat_text_file_icon
        mimeType.contains("link") -> R.drawable.cometchat_link_file_icon
        else -> R.drawable.cometchat_unknown_file_icon
    }
}

private fun buildAccessibilityDescription(
    message: BaseMessage,
    senderName: String
): String {
    return buildString {
        append("Message from $senderName")
        when (message.type) {
            CometChatConstants.MESSAGE_TYPE_TEXT -> {
                val text = (message as? TextMessage)?.text
                if (!text.isNullOrEmpty()) {
                    append(", ")
                    append(text.take(100))
                }
            }
            CometChatConstants.MESSAGE_TYPE_IMAGE -> append(", Photo")
            CometChatConstants.MESSAGE_TYPE_VIDEO -> append(", Video")
            CometChatConstants.MESSAGE_TYPE_AUDIO -> append(", Audio")
            CometChatConstants.MESSAGE_TYPE_FILE -> append(", Document")
        }
        if (message.parentMessageId > 0) {
            append(", in thread")
        }
    }
}
