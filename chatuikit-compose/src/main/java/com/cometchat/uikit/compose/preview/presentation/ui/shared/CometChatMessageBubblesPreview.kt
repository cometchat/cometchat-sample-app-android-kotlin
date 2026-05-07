package com.cometchat.uikit.compose.preview.presentation.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatActionBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatCallActionBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatCollaborativeBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatDeleteBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatMeetCallBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatTextBubble
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CollaborativeType
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.MeetCallType
import com.cometchat.uikit.compose.presentation.shared.messagebubble.utils.CallType
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: TEXT BUBBLE PREVIEWS
// ============================================================================

/**
 * Preview showing an incoming text bubble.
 */
@Preview(showBackground = true, name = "TextBubble - Incoming")
@Composable
fun PreviewTextBubbleIncoming() {
    CometChatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CometChatTextBubble(
                message = PreviewMockData.createMockTextMessage(
                    text = "Hey! How are you doing today?",
                    sender = PreviewMockData.createMockUser(name = "Alice Smith")
                ),
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )
        }
    }
}

/**
 * Preview showing an outgoing text bubble.
 */
@Preview(showBackground = true, name = "TextBubble - Outgoing")
@Composable
fun PreviewTextBubbleOutgoing() {
    CometChatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CometChatTextBubble(
                message = PreviewMockData.createMockTextMessage(
                    text = "I'm doing great, thanks for asking!",
                    sender = PreviewMockData.createMockUser(uid = "me", name = "Me")
                ),
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
            )
        }
    }
}

/**
 * Preview showing a long text bubble.
 */
@Preview(showBackground = true, name = "TextBubble - Long Text")
@Composable
fun PreviewTextBubbleLongText() {
    CometChatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CometChatTextBubble(
                message = PreviewMockData.createMockTextMessage(
                    text = "This is a much longer message that demonstrates how the text bubble handles multi-line content. It should wrap properly and maintain good readability with appropriate padding and line spacing throughout the entire message.",
                    sender = PreviewMockData.createMockUser(name = "Alice Smith")
                ),
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )
        }
    }
}

/**
 * Preview showing a short text bubble.
 */
@Preview(showBackground = true, name = "TextBubble - Short Text")
@Composable
fun PreviewTextBubbleShortText() {
    CometChatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CometChatTextBubble(
                message = PreviewMockData.createMockTextMessage(
                    text = "OK 👍",
                    sender = PreviewMockData.createMockUser(name = "Alice Smith")
                ),
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )
        }
    }
}

/**
 * Preview showing incoming and outgoing text bubbles together.
 */
@Preview(showBackground = true, name = "TextBubble - Conversation")
@Composable
fun PreviewTextBubbleConversation() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CometChatTheme.colorScheme.backgroundColor1)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CometChatTextBubble(
                message = PreviewMockData.createMockTextMessage(
                    id = 1L,
                    text = "Hey! Are you coming to the meeting?",
                    sender = PreviewMockData.createMockUser(name = "Alice Smith")
                ),
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )
            CometChatTextBubble(
                message = PreviewMockData.createMockTextMessage(
                    id = 2L,
                    text = "Yes, I'll be there in 5 minutes!",
                    sender = PreviewMockData.createMockUser(uid = "me", name = "Me")
                ),
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
            )
            CometChatTextBubble(
                message = PreviewMockData.createMockTextMessage(
                    id = 3L,
                    text = "Great, see you soon! 🎉",
                    sender = PreviewMockData.createMockUser(name = "Alice Smith")
                ),
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )
        }
    }
}

// ============================================================================
// SECTION 2: ACTION BUBBLE PREVIEWS
// ============================================================================

/**
 * Preview showing action bubbles (group events).
 */
@Preview(showBackground = true, name = "ActionBubble - Group Events")
@Composable
fun PreviewActionBubbleGroupEvents() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CometChatActionBubble(text = "Alice Smith joined the group")
            CometChatActionBubble(text = "Bob Johnson left the group")
            CometChatActionBubble(text = "Charlie Brown was kicked by Alice Smith")
            CometChatActionBubble(text = "Diana Prince was made admin by Alice Smith")
        }
    }
}

// ============================================================================
// SECTION 3: DELETE BUBBLE PREVIEWS
// ============================================================================

/**
 * Preview showing incoming delete bubble.
 */
@Preview(showBackground = true, name = "DeleteBubble - Incoming")
@Composable
fun PreviewDeleteBubbleIncoming() {
    CometChatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CometChatDeleteBubble(
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )
        }
    }
}

/**
 * Preview showing outgoing delete bubble.
 */
@Preview(showBackground = true, name = "DeleteBubble - Outgoing")
@Composable
fun PreviewDeleteBubbleOutgoing() {
    CometChatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CometChatDeleteBubble(
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
            )
        }
    }
}

/**
 * Preview showing both incoming and outgoing delete bubbles.
 */
@Preview(showBackground = true, name = "DeleteBubble - Both Alignments")
@Composable
fun PreviewDeleteBubbleBothAlignments() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CometChatDeleteBubble(alignment = UIKitConstants.MessageBubbleAlignment.LEFT)
            CometChatDeleteBubble(alignment = UIKitConstants.MessageBubbleAlignment.RIGHT)
        }
    }
}

// ============================================================================
// SECTION 4: CALL ACTION BUBBLE PREVIEWS
// ============================================================================

/**
 * Preview showing all call action bubble types.
 */
@Preview(showBackground = true, name = "CallActionBubble - All Types")
@Composable
fun PreviewCallActionBubbleAllTypes() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CometChatCallActionBubble(
                callType = CallType.AUDIO_INCOMING,
                isMissed = false,
                statusText = "Audio call • 5:23"
            )
            CometChatCallActionBubble(
                callType = CallType.AUDIO_OUTGOING,
                isMissed = false,
                statusText = "Audio call • 2:15"
            )
            CometChatCallActionBubble(
                callType = CallType.VIDEO_INCOMING,
                isMissed = false,
                statusText = "Video call • 10:45"
            )
            CometChatCallActionBubble(
                callType = CallType.VIDEO_OUTGOING,
                isMissed = false,
                statusText = "Video call • 3:30"
            )
        }
    }
}

/**
 * Preview showing missed call action bubbles.
 */
@Preview(showBackground = true, name = "CallActionBubble - Missed Calls")
@Composable
fun PreviewCallActionBubbleMissedCalls() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CometChatCallActionBubble(
                callType = CallType.AUDIO_INCOMING,
                isMissed = true,
                statusText = "Missed audio call"
            )
            CometChatCallActionBubble(
                callType = CallType.VIDEO_INCOMING,
                isMissed = true,
                statusText = "Missed video call"
            )
        }
    }
}

// ============================================================================
// SECTION 5: COLLABORATIVE BUBBLE PREVIEWS
// ============================================================================

/**
 * Preview showing collaborative document bubble.
 */
@Preview(showBackground = true, name = "CollaborativeBubble - Document")
@Composable
fun PreviewCollaborativeBubbleDocument() {
    CometChatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CometChatCollaborativeBubble(
                title = "Collaborative Document",
                subtitle = "Open document to edit together",
                type = CollaborativeType.DOCUMENT,
                url = "https://example.com/doc/123",
                onJoinClick = { }
            )
        }
    }
}

/**
 * Preview showing collaborative whiteboard bubble.
 */
@Preview(showBackground = true, name = "CollaborativeBubble - Whiteboard")
@Composable
fun PreviewCollaborativeBubbleWhiteboard() {
    CometChatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CometChatCollaborativeBubble(
                title = "Collaborative Whiteboard",
                subtitle = "Open whiteboard to draw together",
                type = CollaborativeType.WHITEBOARD,
                url = "https://example.com/whiteboard/456",
                onJoinClick = { }
            )
        }
    }
}

// ============================================================================
// SECTION 6: MEET CALL BUBBLE PREVIEWS
// ============================================================================

/**
 * Preview showing all meet call bubble types.
 */
@Preview(showBackground = true, name = "MeetCallBubble - All Types")
@Composable
fun PreviewMeetCallBubbleAllTypes() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CometChatMeetCallBubble(
                title = "Group Audio Call",
                subtitle = "Tap to join",
                callType = MeetCallType.VOICE_INCOMING,
                sessionId = "session_1",
                onJoinClick = { }
            )
            CometChatMeetCallBubble(
                title = "Group Video Call",
                subtitle = "Tap to join",
                callType = MeetCallType.VIDEO_INCOMING,
                sessionId = "session_2",
                onJoinClick = { }
            )
            CometChatMeetCallBubble(
                title = "Outgoing Voice Call",
                subtitle = "Started by you",
                callType = MeetCallType.VOICE_OUTGOING,
                sessionId = "session_3",
                onJoinClick = { }
            )
            CometChatMeetCallBubble(
                title = "Outgoing Video Call",
                subtitle = "Started by you",
                callType = MeetCallType.VIDEO_OUTGOING,
                sessionId = "session_4",
                onJoinClick = { }
            )
        }
    }
}

// ============================================================================
// SECTION 7: COMPREHENSIVE BUBBLE SHOWCASE
// ============================================================================

/**
 * Preview showing all bubble types together.
 */
@Preview(showBackground = true, name = "MessageBubbles - All Types Showcase")
@Composable
fun PreviewMessageBubblesShowcase() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CometChatTheme.colorScheme.backgroundColor1)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Section label
            Text(
                text = "Text Bubbles",
                style = CometChatTheme.typography.caption1Bold,
                color = CometChatTheme.colorScheme.textColorSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            CometChatTextBubble(
                message = PreviewMockData.createMockTextMessage(
                    id = 1L, text = "Incoming text message",
                    sender = PreviewMockData.createMockUser(name = "Alice")
                ),
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT
            )
            CometChatTextBubble(
                message = PreviewMockData.createMockTextMessage(
                    id = 2L, text = "Outgoing text message",
                    sender = PreviewMockData.createMockUser(uid = "me", name = "Me")
                ),
                alignment = UIKitConstants.MessageBubbleAlignment.RIGHT
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Action & Delete Bubbles",
                style = CometChatTheme.typography.caption1Bold,
                color = CometChatTheme.colorScheme.textColorSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            CometChatActionBubble(text = "Alice joined the group")
            CometChatDeleteBubble(alignment = UIKitConstants.MessageBubbleAlignment.LEFT)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Call Bubbles",
                style = CometChatTheme.typography.caption1Bold,
                color = CometChatTheme.colorScheme.textColorSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            CometChatCallActionBubble(
                callType = CallType.AUDIO_INCOMING,
                isMissed = false,
                statusText = "Audio call • 5:23"
            )
            CometChatCallActionBubble(
                callType = CallType.VIDEO_INCOMING,
                isMissed = true,
                statusText = "Missed video call"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Collaborative Bubbles",
                style = CometChatTheme.typography.caption1Bold,
                color = CometChatTheme.colorScheme.textColorSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            CometChatCollaborativeBubble(
                title = "Collaborative Document",
                subtitle = "Open to edit together",
                type = CollaborativeType.DOCUMENT,
                url = "https://example.com/doc",
                onJoinClick = { }
            )
        }
    }
}
