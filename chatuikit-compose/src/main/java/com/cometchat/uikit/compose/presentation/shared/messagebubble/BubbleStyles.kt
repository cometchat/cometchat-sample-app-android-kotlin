package com.cometchat.uikit.compose.presentation.shared.messagebubble

import androidx.compose.runtime.Immutable
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudioBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCallActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatDeleteBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagepreview.CometChatMessagePreviewStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFileBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatPollBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatStickerBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideoBubbleStyle

/**
 * Container for all bubble style overrides.
 *
 * This data class holds optional style configurations for each bubble type.
 * It is passed from CometChatMessageList to CometChatMessageBubble to InternalContentRenderer,
 * enabling centralized style configuration at the list level while allowing
 * individual bubble types to use alignment-based defaults when no style is provided.
 *
 * When a style property is null, the InternalContentRenderer will use the appropriate
 * alignment-based default style (incoming(), outgoing(), or default()) based on
 * the message bubble alignment.
 *
 * Example usage:
 * ```kotlin
 * // Configure specific bubble styles
 * val styles = BubbleStyles(
 *     textBubbleStyle = CometChatTextBubbleStyle.incoming().copy(
 *         backgroundColor = Color.LightGray
 *     ),
 *     imageBubbleStyle = CometChatImageBubbleStyle.incoming().copy(
 *         cornerRadius = 16.dp
 *     )
 * )
 *
 * // Pass to InternalContentRenderer
 * InternalContentRenderer.renderContent(message, alignment, styles)
 * ```
 *
 * @property messageBubbleStyle Base style for all message bubbles, used as fallback for CommonProperties
 * @property textBubbleStyle Style for text message bubbles
 * @property imageBubbleStyle Style for image message bubbles
 * @property videoBubbleStyle Style for video message bubbles
 * @property audioBubbleStyle Style for audio message bubbles
 * @property fileBubbleStyle Style for file message bubbles
 * @property deleteBubbleStyle Style for deleted message bubbles
 * @property actionBubbleStyle Style for action message bubbles (e.g., group member events)
 * @property callActionBubbleStyle Style for call action bubbles (audio/video calls)
 * @property meetCallBubbleStyle Style for meet call bubbles (meeting messages)
 * @property pollBubbleStyle Style for poll message bubbles
 * @property stickerBubbleStyle Style for sticker message bubbles
 * @property collaborativeBubbleStyle Style for collaborative bubbles (document/whiteboard)
 * @property incomingMessagePreviewStyle Style for the message preview in incoming message bubbles
 * @property outgoingMessagePreviewStyle Style for the message preview in outgoing message bubbles
 */
@Immutable
data class BubbleStyles(
    val messageBubbleStyle: CometChatMessageBubbleStyle? = null,
    val textBubbleStyle: CometChatTextBubbleStyle? = null,
    val imageBubbleStyle: CometChatImageBubbleStyle? = null,
    val videoBubbleStyle: CometChatVideoBubbleStyle? = null,
    val audioBubbleStyle: CometChatAudioBubbleStyle? = null,
    val fileBubbleStyle: CometChatFileBubbleStyle? = null,
    val deleteBubbleStyle: CometChatDeleteBubbleStyle? = null,
    val actionBubbleStyle: CometChatActionBubbleStyle? = null,
    val callActionBubbleStyle: CometChatCallActionBubbleStyle? = null,
    val meetCallBubbleStyle: CometChatMeetCallBubbleStyle? = null,
    val pollBubbleStyle: CometChatPollBubbleStyle? = null,
    val stickerBubbleStyle: CometChatStickerBubbleStyle? = null,
    val collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle? = null,
    val incomingMessagePreviewStyle: CometChatMessagePreviewStyle? = null,
    val outgoingMessagePreviewStyle: CometChatMessagePreviewStyle? = null
)
