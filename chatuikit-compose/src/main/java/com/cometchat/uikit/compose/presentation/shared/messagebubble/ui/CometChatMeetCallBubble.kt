package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.CustomMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enum representing the type of meeting call.
 */
enum class MeetCallType {
    VOICE_INCOMING,
    VOICE_OUTGOING,
    VIDEO_INCOMING,
    VIDEO_OUTGOING
}

/**
 * A composable that displays a meeting/call invitation message bubble.
 *
 * This component renders meeting call messages with:
 * - Call type icon (voice/video) with direction indicator
 * - Call title (e.g., "Video Call", "Voice Call")
 * - Subtitle with timestamp
 * - "Join" button with separator
 *
 * Example usage:
 * ```kotlin
 * CometChatMeetCallBubble(
 *     message = customMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatMeetCallBubbleStyle.incoming(),
 *     onJoinClick = { sessionId -> /* Handle join */ }
 * )
 * ```
 *
 * @param message The [CustomMessage] containing meeting call data
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance (includes wrapper properties inherited from parent)
 * @param onJoinClick Callback when the "Join" button is clicked with the session ID
 */
@Composable
fun CometChatMeetCallBubble(
    message: CustomMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatMeetCallBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatMeetCallBubbleStyle.outgoing()
        else -> CometChatMeetCallBubbleStyle.incoming()
    },
    onJoinClick: ((String) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val meetCallData = remember(message.id) {
        extractMeetCallData(message)
    }

    CometChatMeetCallBubble(
        title = meetCallData.title,
        subtitle = meetCallData.subtitle,
        callType = meetCallData.callType,
        sessionId = meetCallData.sessionId,
        modifier = modifier,
        style = style,
        onJoinClick = onJoinClick,
        onLongClick = onLongClick
    )
}

/**
 * Overload for displaying meet call bubble with direct parameters.
 *
 * @param title The call title (e.g., "Video Call", "Voice Call")
 * @param subtitle The call subtitle (typically timestamp)
 * @param callType The type of call (voice/video, incoming/outgoing)
 * @param sessionId The session ID for joining the call
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance (includes wrapper properties inherited from parent)
 * @param onJoinClick Callback when the "Join" button is clicked with the session ID
 */
@Composable
fun CometChatMeetCallBubble(
    title: String,
    subtitle: String,
    callType: MeetCallType,
    sessionId: String,
    modifier: Modifier = Modifier,
    style: CometChatMeetCallBubbleStyle = CometChatMeetCallBubbleStyle.default(),
    onJoinClick: ((String) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    MeetCallBubbleContent(
        title = title,
        subtitle = subtitle,
        callType = callType,
        sessionId = sessionId,
        modifier = modifier,
        style = style,
        onJoinClick = onJoinClick,
        onLongClick = onLongClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MeetCallBubbleContent(
    title: String,
    subtitle: String,
    callType: MeetCallType,
    sessionId: String,
    modifier: Modifier,
    style: CometChatMeetCallBubbleStyle,
    onJoinClick: ((String) -> Unit)?,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(style.cornerRadius)

    // Determine the icon based on call type
    val iconResId = when (callType) {
        MeetCallType.VOICE_INCOMING -> R.drawable.cometchat_ic_incoming_voice_call
        MeetCallType.VOICE_OUTGOING -> R.drawable.cometchat_ic_outgoing_voice_call
        MeetCallType.VIDEO_INCOMING -> R.drawable.cometchat_ic_incoming_video_call
        MeetCallType.VIDEO_OUTGOING -> R.drawable.cometchat_ic_outgoing_video_call
    }

    val accessibilityDescription = when (callType) {
        MeetCallType.VOICE_INCOMING -> "Incoming voice call: $title"
        MeetCallType.VOICE_OUTGOING -> "Outgoing voice call: $title"
        MeetCallType.VIDEO_INCOMING -> "Incoming video call: $title"
        MeetCallType.VIDEO_OUTGOING -> "Outgoing video call: $title"
    }

    Column(
        modifier = modifier
            .width(240.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(4.dp)
            .semantics {
                contentDescription = accessibilityDescription
            }
    ) {
        // Content row with icon and text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Call icon with circular background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(style.iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = style.callIconTint
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Title and subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = style.titleTextStyle,
                    color = style.titleTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = style.subtitleTextStyle,
                        color = style.subtitleTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Separator
        Divider(
            color = style.separatorColor,
            thickness = 1.dp
        )

        // Join button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable { onJoinClick?.invoke(sessionId) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = context.getString(R.string.cometchat_join),
                style = style.joinButtonTextStyle,
                color = style.joinButtonTextColor
            )
        }
    }
}

/**
 * Data class for meeting call data extracted from CustomMessage.
 */
private data class MeetCallData(
    val title: String,
    val subtitle: String,
    val callType: MeetCallType,
    val sessionId: String
)

/**
 * Extracts meeting call data from a CustomMessage.
 *
 * @param message The CustomMessage containing meeting call data
 * @return [MeetCallData] with extracted values
 */
private fun extractMeetCallData(message: CustomMessage): MeetCallData {
    return try {
        val customData = message.customData
        
        // Extract call type from customData
        val callTypeString = customData?.optString("callType", CometChatConstants.CALL_TYPE_AUDIO) 
            ?: CometChatConstants.CALL_TYPE_AUDIO
        val isVideo = callTypeString == CometChatConstants.CALL_TYPE_VIDEO
        
        // Determine direction based on sender vs logged-in user
        val loggedInUserId = CometChatUIKit.getLoggedInUser()?.uid
        val isIncoming = message.sender?.uid != loggedInUserId
        
        // Determine call type enum
        val callType = when {
            isVideo && isIncoming -> MeetCallType.VIDEO_INCOMING
            isVideo -> MeetCallType.VIDEO_OUTGOING
            isIncoming -> MeetCallType.VOICE_INCOMING
            else -> MeetCallType.VOICE_OUTGOING
        }
        
        // Extract session ID
        val sessionId = customData?.optString("sessionId", "") ?: ""
        
        // Extract or generate title
        val title = customData?.optString("title", null) 
            ?: if (isVideo) "Video Call" else "Voice Call"
        
        // Format subtitle from sentAt timestamp
        val subtitle = formatTimestamp(message.sentAt)

        MeetCallData(
            title = title,
            subtitle = subtitle,
            callType = callType,
            sessionId = sessionId
        )
    } catch (e: Exception) {
        MeetCallData(
            title = "Voice Call",
            subtitle = "",
            callType = MeetCallType.VOICE_OUTGOING,
            sessionId = ""
        )
    }
}

/**
 * Formats a timestamp (in seconds) to a human-readable date string.
 *
 * @param seconds The timestamp in seconds since epoch
 * @return Formatted date string (e.g., "15 Jan, 10:30 AM")
 */
private fun formatTimestamp(seconds: Long): String {
    return try {
        val milliseconds = seconds * 1000
        val date = Date(milliseconds)
        val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        ""
    }
}

/**
 * Gets the drawable resource ID for a given meet call type.
 *
 * @param callType The meet call type
 * @return The drawable resource ID for the call type icon
 */
fun getMeetCallTypeIcon(callType: MeetCallType): Int {
    return when (callType) {
        MeetCallType.VOICE_INCOMING -> R.drawable.cometchat_ic_incoming_voice_call
        MeetCallType.VOICE_OUTGOING -> R.drawable.cometchat_ic_outgoing_voice_call
        MeetCallType.VIDEO_INCOMING -> R.drawable.cometchat_ic_incoming_video_call
        MeetCallType.VIDEO_OUTGOING -> R.drawable.cometchat_ic_outgoing_video_call
    }
}

/**
 * Checks if the meet call type represents a video call.
 *
 * @param callType The meet call type
 * @return True if the call is a video call
 */
fun isVideoMeetCall(callType: MeetCallType): Boolean {
    return callType == MeetCallType.VIDEO_INCOMING || callType == MeetCallType.VIDEO_OUTGOING
}

/**
 * Checks if the meet call type represents an incoming call.
 *
 * @param callType The meet call type
 * @return True if the call is incoming
 */
fun isIncomingMeetCall(callType: MeetCallType): Boolean {
    return callType == MeetCallType.VOICE_INCOMING || callType == MeetCallType.VIDEO_INCOMING
}
