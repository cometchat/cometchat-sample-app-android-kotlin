package com.cometchat.uikit.compose.presentation.shared.messagepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.formatters.FormatterUtils
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * A composable that renders a quoted/replied-to message preview.
 *
 * Displays the sender name as title, message content summary as subtitle,
 * a vertical separator bar, an optional message type icon, and an optional close icon.
 *
 * @param message The quoted message to preview
 * @param modifier Compose modifier
 * @param style Visual style configuration
 * @param showCloseIcon Whether to show the close/dismiss icon
 * @param textFormatters Text formatters for text message content
 * @param alignment Bubble alignment for formatter context
 * @param onCloseClick Callback when close icon is tapped
 * @param onClick Callback when the preview area is tapped
 */
@Composable
fun CometChatMessagePreview(
    message: BaseMessage,
    modifier: Modifier = Modifier,
    style: CometChatMessagePreviewStyle = CometChatMessagePreviewStyle.default(),
    showCloseIcon: Boolean = true,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    alignment: UIKitConstants.MessageBubbleAlignment = UIKitConstants.MessageBubbleAlignment.LEFT,
    onCloseClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Resolve title: sender name or "You" for logged-in user
    val title = remember(message.id, message.sender?.uid) {
        val loggedInUid = try {
            CometChatUIKit.getLoggedInUser()?.uid
        } catch (e: Exception) {
            null
        }
        if (message.sender?.uid != loggedInUid) {
            message.sender?.name ?: ""
        } else {
            context.getString(R.string.cometchat_you)
        }
    }

    // Resolve subtitle and icon based on message type
    val (subtitle, iconRes) = remember(message.id, message.type) {
        resolveMessageContent(context, message, textFormatters, alignment)
    }

    val shape = RoundedCornerShape(style.cornerRadius)

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .then(
                if (style.strokeWidth > 0.dp) {
                    Modifier.border(style.strokeWidth, style.strokeColor, shape)
                } else {
                    Modifier
                }
            )
            .background(color = style.backgroundColor, shape = shape)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .semantics {
                contentDescription = "Message preview: $title - ${subtitle.text}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Separator bar: 2dp wide, full height (matches kotlin-uikit)
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(color = style.separatorColor)
        )

        // Content area with padding (matches kotlin-uikit cometchat_padding_2)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(style.padding)
        ) {
            // Title row with close icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title (sender name) - maxLines=2 to match kotlin-uikit
                Text(
                    text = title,
                    color = style.titleTextColor,
                    style = style.titleTextStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Close icon - inside title row to match kotlin-uikit layout
                if (showCloseIcon && onCloseClick != null) {
                    Icon(
                        painter = painterResource(id = R.drawable.cometchat_ic_close),
                        contentDescription = "Close preview",
                        tint = style.closeIconTint,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onCloseClick() }
                    )
                }
            }

            // Subtitle row with optional message type icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = style.messageIconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = subtitle,
                    color = style.subtitleTextColor,
                    style = style.subtitleTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Resolves the subtitle text and optional icon resource for a message preview
 * based on the message type.
 *
 * @return A pair of (subtitle AnnotatedString, icon drawable resource ID or null)
 */
internal fun resolveMessageContent(
    context: android.content.Context,
    message: BaseMessage,
    textFormatters: List<CometChatTextFormatter>,
    alignment: UIKitConstants.MessageBubbleAlignment
): Pair<AnnotatedString, Int?> {
    return when (message) {
        is TextMessage -> {
            val text = message.text ?: ""
            val formatted = if (textFormatters.isNotEmpty()) {
                FormatterUtils.getFormattedText(
                    context = context,
                    baseMessage = message,
                    formattingType = UIKitConstants.FormattingType.MESSAGE_BUBBLE,
                    alignment = alignment,
                    text = text,
                    formatters = textFormatters
                )
            } else {
                AnnotatedString(text)
            }
            formatted to null
        }

        is MediaMessage -> {
            val subtitle = message.attachment?.fileName
                ?: message.type
                ?: ""
            val iconRes = when (message.type?.lowercase()) {
                UIKitConstants.MessageType.IMAGE -> R.drawable.cometchat_ic_message_preview_image
                UIKitConstants.MessageType.VIDEO -> R.drawable.cometchat_ic_message_preview_image
                UIKitConstants.MessageType.AUDIO -> R.drawable.cometchat_ic_message_preview_audio_mic
                UIKitConstants.MessageType.FILE -> R.drawable.cometchat_ic_message_preview_document
                else -> null
            }
            AnnotatedString(subtitle) to iconRes
        }

        is CustomMessage -> resolveCustomMessageContent(context, message)

        else -> {
            AnnotatedString(message.type ?: "") to null
        }
    }
}

/**
 * Resolves subtitle and icon for custom message types (polls, stickers, documents, etc.).
 */
internal fun resolveCustomMessageContent(
    context: android.content.Context,
    message: CustomMessage
): Pair<AnnotatedString, Int?> {
    return when (message.type) {
        UIKitConstants.MessageType.EXTENSION_POLL -> {
            AnnotatedString(context.getString(R.string.cometchat_poll)) to
                R.drawable.cometchat_ic_message_preview_poll
        }

        UIKitConstants.MessageType.EXTENSION_STICKER -> {
            AnnotatedString(context.getString(R.string.cometchat_message_sticker)) to
                R.drawable.cometchat_ic_message_preview_sticker
        }

        UIKitConstants.MessageType.EXTENSION_DOCUMENT -> {
            AnnotatedString(context.getString(R.string.cometchat_message_document)) to
                R.drawable.cometchat_ic_message_preview_collaborative_document
        }

        UIKitConstants.MessageType.EXTENSION_WHITEBOARD -> {
            AnnotatedString(context.getString(R.string.cometchat_collaborative_whiteboard)) to
                R.drawable.cometchat_ic_conversations_collabrative_document
        }

        UIKitConstants.MessageType.MEETING -> {
            AnnotatedString(context.getString(R.string.cometchat_meeting)) to
                R.drawable.cometchat_ic_message_preview_call
        }

        else -> {
            // Fallback: conversationText → metadata pushNotification → type
            val subtitle = message.conversationText?.takeIf { it.isNotEmpty() }
                ?: message.metadata?.optString("pushNotification")?.takeIf { it.isNotEmpty() }
                ?: message.type
                ?: ""
            AnnotatedString(subtitle) to null
        }
    }
}
