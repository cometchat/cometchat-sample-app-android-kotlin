package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Default reply preview composable that shows the message being replied to.
 * Displays a colored separator bar, sender name, message content, and a close button.
 * Matches the Java implementation logic from Utils.setReplyMessagePreview.
 * Runs the formatter pipeline to resolve mention tokens for TextMessages.
 *
 * @param modifier Modifier for the preview
 * @param message The message being replied to
 * @param textFormatters List of text formatters to resolve mention tokens
 * @param style Style configuration for the preview
 * @param onClose Callback when the close button is clicked
 */
@Composable
fun DefaultReplyPreview(
    modifier: Modifier = Modifier,
    message: BaseMessage,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Get sender name - show "You" if it's the logged-in user (matching Java logic)
    val loggedInUser = CometChatUIKit.getLoggedInUser()
    val senderName = if (message.sender?.uid == loggedInUser?.uid) {
        context.getString(R.string.cometchat_you)
    } else {
        message.sender?.name ?: ""
    }
    
    // Get message content based on type (matching Java Utils.setReplyMessagePreview logic)
    // Run formatter pipeline to resolve mention tokens for TextMessages
    val messageContent: CharSequence = remember(message, textFormatters) {
        when (message) {
            is TextMessage -> {
                if (message.deletedAt > 0) {
                    context.getString(R.string.cometchat_this_message_deleted)
                } else {
                    val rawText = message.text ?: ""
                    if (rawText.isEmpty() || textFormatters.isEmpty()) {
                        rawText
                    } else {
                        // Run each formatter through the pipeline with MESSAGE_COMPOSER type
                        var formattedText: AnnotatedString = AnnotatedString(rawText)
                        for (formatter in textFormatters) {
                            formattedText = formatter.prepareMessageString(
                                context,
                                message,
                                formattedText,
                                UIKitConstants.MessageBubbleAlignment.LEFT,
                                UIKitConstants.FormattingType.MESSAGE_COMPOSER
                            )
                        }
                        formattedText
                    }
                }
            }
            is MediaMessage -> {
                message.attachment?.fileName ?: when (message.type) {
                    "image" -> context.getString(R.string.cometchat_message_image)
                    "video" -> context.getString(R.string.cometchat_message_video)
                    "audio" -> context.getString(R.string.cometchat_message_audio)
                    "file" -> context.getString(R.string.cometchat_message_document)
                    else -> message.type ?: ""
                }
            }
            is CustomMessage -> {
                when (message.type) {
                    "extension_poll" -> context.getString(R.string.cometchat_poll)
                    "extension_sticker" -> context.getString(R.string.cometchat_message_sticker)
                    "location" -> context.getString(R.string.cometchat_message_location)
                    "extension_document" -> context.getString(R.string.cometchat_message_document)
                    "extension_whiteboard" -> context.getString(R.string.cometchat_collaborative_whiteboard)
                    "meeting" -> context.getString(R.string.cometchat_meeting)
                    else -> message.conversationText ?: message.type ?: ""
                }
            }
            else -> message.type ?: ""
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(
                color = style.messagePreviewBackgroundColor,
                shape = RoundedCornerShape(style.messagePreviewCornerRadius)
            )
            .then(
                if (style.messagePreviewStrokeWidth > 0.dp) {
                    Modifier.border(
                        width = style.messagePreviewStrokeWidth,
                        color = style.messagePreviewStrokeColor,
                        shape = RoundedCornerShape(style.messagePreviewCornerRadius)
                    )
                } else Modifier
            )
            .padding(8.dp)
            .semantics { contentDescription = "Reply Preview" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored separator bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .background(
                    color = style.messagePreviewSeparatorColor,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Sender name as title (matching Java logic - just the name, not "Replying to")
            Text(
                text = senderName,
                color = style.messagePreviewTitleTextColor,
                style = style.messagePreviewTitleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Message content
            when (messageContent) {
                is AnnotatedString -> Text(
                    text = messageContent,
                    color = style.messagePreviewSubtitleTextColor,
                    style = style.messagePreviewSubtitleTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                else -> Text(
                    text = messageContent.toString(),
                    color = style.messagePreviewSubtitleTextColor,
                    style = style.messagePreviewSubtitleTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp)
        ) {
            style.messagePreviewCloseIcon?.let { icon ->
                Icon(
                    painter = icon,
                    contentDescription = "Close reply preview",
                    tint = style.messagePreviewCloseIconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
