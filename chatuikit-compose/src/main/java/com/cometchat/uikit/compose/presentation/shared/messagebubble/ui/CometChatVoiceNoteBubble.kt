package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudioBubbleStyle
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Renders a recorded voice note. Identical to the current single-audio look; distinguished from
 * picker audio ([CometChatAudiosBubble]) via `metadata.audioType == "voice_note"`. Part of the
 * ENG-36737 per-type multi-attachment bubbles.
 *
 * @param message The [MediaMessage] carrying the recorded voice note
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param style Style configuration for the audio player
 * @param onLongClick Callback when the bubble is long-pressed
 */
@Composable
fun CometChatVoiceNoteBubble(
    message: MediaMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatAudioBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatAudioBubbleStyle.outgoing()
        else -> CometChatAudioBubbleStyle.incoming()
    },
    onLongClick: (() -> Unit)? = null
) {
    CometChatAudioBubble(message = message, alignment = alignment, modifier = modifier, style = style, onLongClick = onLongClick)
}
