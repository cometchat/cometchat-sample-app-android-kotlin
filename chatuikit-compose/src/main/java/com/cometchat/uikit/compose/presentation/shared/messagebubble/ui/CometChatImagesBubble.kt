package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.compose.presentation.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImagesBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Renders every image attachment of [message] as a grid. Part of the ENG-36737 per-type
 * multi-attachment bubbles; see [MediaBubbleContent] for the shared grid.
 *
 * @param message The [MediaMessage] whose image attachments are rendered
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param style Style configuration for the media grid + caption
 * @param caption Optional caption (defaults to the message caption, shown under the grid)
 * @param onMediaClick Callback when an image is tapped (index, attachment)
 * @param onMoreClick Callback when the "+N" overflow tile is tapped (all attachments)
 * @param onLongClick Callback when the bubble is long-pressed
 */
@Composable
fun CometChatImagesBubble(
    message: MediaMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatImagesBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatImagesBubbleStyle.outgoing()
        else -> CometChatImagesBubbleStyle.incoming()
    },
    caption: String? = message.caption,
    textFormatters: List<CometChatTextFormatter> = emptyList(),
    onMediaClick: ((Int, Attachment) -> Unit)? = null,
    onMoreClick: ((List<Attachment>) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    // Every attachment stays in the grid — a kind-mismatched one (server-sent mixed payload)
    // renders as a broken tile in MediaTile rather than being dropped.
    val images = remember(message.id) { resolveAttachments(message) }
    val thumbnailUrl = remember(message.id) { message.thumbnailUrl() }
    MediaBubbleContent(
        images, style.toGridAppearance(), isVideo = false, caption,
        onMediaClick, onMoreClick, onLongClick,
        firstTileThumbnailUrl = thumbnailUrl,
        isEdited = message.editedAt > 0, modifier = modifier,
        message = message, alignment = alignment, textFormatters = textFormatters
    )
}
