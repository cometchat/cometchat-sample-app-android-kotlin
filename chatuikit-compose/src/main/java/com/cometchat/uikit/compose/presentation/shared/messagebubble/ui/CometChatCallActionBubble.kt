package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.chat.core.Call
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCallActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.CallType
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.getCallType
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.isMissedCall

/**
 * A composable that displays a call action message bubble.
 *
 * This component renders call-related system messages with:
 * - Appropriate icon based on call type (audio/video) and direction (incoming/outgoing)
 * - Call status text
 * - Special styling for missed calls (red text and background)
 *
 * Call action bubbles are always centered.
 *
 * Example usage:
 * ```kotlin
 * CometChatCallActionBubble(
 *     message = callMessage,
 *     style = CometChatCallActionBubbleStyle.default()
 * )
 * ```
 *
 * @param message The [Call] message containing call data
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance (extends CometChatMessageBubbleStyle)
 */
@Composable
fun CometChatCallActionBubble(
    message: Call,
    modifier: Modifier = Modifier,
    style: CometChatCallActionBubbleStyle = CometChatCallActionBubbleStyle.default()
) {
    val callType = remember(message.id) {
        getCallType(message)
    }

    val isMissed = remember(callType) {
        isMissedCall(callType)
    }

    val callStatusText = remember(callType) {
        getCallStatusText(callType)
    }

    CometChatCallActionBubble(
        callType = callType,
        isMissed = isMissed,
        statusText = callStatusText,
        modifier = modifier,
        style = style
    )
}

/**
 * Overload for displaying call action bubble with direct parameters.
 *
 * @param callType The type of call (audio/video, incoming/outgoing, missed)
 * @param isMissed Whether the call was missed
 * @param statusText The status text to display
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance (extends CometChatMessageBubbleStyle)
 */
@Composable
fun CometChatCallActionBubble(
    callType: CallType,
    isMissed: Boolean,
    statusText: String,
    modifier: Modifier = Modifier,
    style: CometChatCallActionBubbleStyle = CometChatCallActionBubbleStyle.default()
) {
    CallActionBubbleContent(
        callType = callType,
        isMissed = isMissed,
        statusText = statusText,
        modifier = modifier,
        style = style
    )
}

@Composable
private fun CallActionBubbleContent(
    callType: CallType,
    isMissed: Boolean,
    statusText: String,
    modifier: Modifier,
    style: CometChatCallActionBubbleStyle
) {
    val shape = RoundedCornerShape(style.cornerRadius)

    // Determine colors based on missed status
    val backgroundColor = if (isMissed) style.missedCallBackgroundColor else style.backgroundColor
    val textColor = if (isMissed) style.missedCallTextColor else style.textColor
    val textStyle = if (isMissed) style.missedCallTextStyle else style.textStyle
    val iconTint = if (isMissed) style.missedCallIconTint else style.iconTint
    val strokeColor = if (isMissed) style.missedCallIconTint else style.strokeColor

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (style.strokeWidth > 0.dp && !isMissed) {
                    Modifier.border(
                        width = style.strokeWidth,
                        color = strokeColor,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics {
                contentDescription = "Call: $statusText"
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Call icon
            Icon(
                painter = painterResource(id = getCallIcon(callType)),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = iconTint
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Status text
            Text(
                text = statusText,
                style = textStyle,
                color = textColor
            )
        }
    }
}

/**
 * Gets the appropriate icon resource for a call type.
 */
private fun getCallIcon(callType: CallType): Int {
    return when (callType) {
        CallType.AUDIO_INCOMING -> R.drawable.cometchat_ic_incoming_voice_call
        CallType.AUDIO_OUTGOING -> R.drawable.cometchat_ic_outgoing_voice_call
        CallType.VIDEO_INCOMING -> R.drawable.cometchat_ic_incoming_video_call
        CallType.VIDEO_OUTGOING -> R.drawable.cometchat_ic_outgoing_video_call
        CallType.AUDIO_MISSED -> R.drawable.cometchat_ic_missed_voice_call
        CallType.VIDEO_MISSED -> R.drawable.cometchat_ic_missed_video_call
    }
}

/**
 * Gets the status text for a call type.
 */
private fun getCallStatusText(callType: CallType): String {
    return when (callType) {
        CallType.AUDIO_INCOMING -> "Incoming voice call"
        CallType.AUDIO_OUTGOING -> "Outgoing voice call"
        CallType.VIDEO_INCOMING -> "Incoming video call"
        CallType.VIDEO_OUTGOING -> "Outgoing video call"
        CallType.AUDIO_MISSED -> "Missed voice call"
        CallType.VIDEO_MISSED -> "Missed video call"
    }
}
