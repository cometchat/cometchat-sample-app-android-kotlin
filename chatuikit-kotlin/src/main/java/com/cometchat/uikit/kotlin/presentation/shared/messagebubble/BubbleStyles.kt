package com.cometchat.uikit.kotlin.presentation.shared.messagebubble

import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.actionbubble.CometChatActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.audiobubble.CometChatAudioBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.callactionbubble.CometChatCallActionBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.collaborativebubble.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.deletebubble.CometChatDeleteBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.filebubble.CometChatFileBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.imagebubble.CometChatImageBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.meetcallbubble.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.pollbubble.CometChatPollBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.stickerbubble.CometChatStickerBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.textbubble.CometChatTextBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.videobubble.CometChatVideoBubbleStyle
import com.cometchat.uikit.kotlin.presentation.shared.messagepreview.CometChatMessagePreviewStyle

/**
 * Container for optional per-bubble-type style overrides.
 *
 * When passed to [InternalContentRenderer] or [CometChatMessageBubble], each non-null
 * entry overrides the default alignment-based style for that bubble type. A `null` entry
 * means the renderer falls back to the alignment-based default (e.g.,
 * `CometChatTextBubbleStyle.incoming(context)`).
 *
 * [messageBubbleStyle] serves as the base style override for the outer message bubble
 * container. Per-bubble-type styles can have their sentinel CommonProperties resolved
 * against this base via [mergeWithBase].
 */
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
