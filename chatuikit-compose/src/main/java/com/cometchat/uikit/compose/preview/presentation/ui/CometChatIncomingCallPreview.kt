package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.uikit.compose.presentation.incomingcall.style.CometChatIncomingCallStyle
import com.cometchat.uikit.compose.presentation.incomingcall.ui.CometChatIncomingCall
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Creates a mock Call object for preview purposes.
 */
private fun createMockCall(
    callType: String = CometChatConstants.CALL_TYPE_AUDIO,
    receiverType: String = CometChatConstants.RECEIVER_TYPE_USER
): Call {
    return Call(
        "receiver_1",
        receiverType,
        callType
    ).apply {
        sessionId = "preview_session_${System.currentTimeMillis()}"
        callInitiator = PreviewMockData.createMockUser(
            name = "Alice Smith",
            status = CometChatConstants.USER_STATUS_ONLINE
        )
        callReceiver = PreviewMockData.createMockUser(
            uid = "me",
            name = "Me"
        )
    }
}

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

/**
 * Preview showing an incoming audio call.
 */
@Preview(showBackground = true, name = "IncomingCall - Audio Call")
@Composable
fun PreviewIncomingCallAudio() {
    CometChatTheme {
        CometChatIncomingCall(
            call = createMockCall(callType = CometChatConstants.CALL_TYPE_AUDIO),
            disableSoundForCalls = true,
            onAcceptClick = { },
            onRejectClick = { }
        )
    }
}

/**
 * Preview showing an incoming video call.
 */
@Preview(showBackground = true, name = "IncomingCall - Video Call")
@Composable
fun PreviewIncomingCallVideo() {
    CometChatTheme {
        CometChatIncomingCall(
            call = createMockCall(callType = CometChatConstants.CALL_TYPE_VIDEO),
            disableSoundForCalls = true,
            onAcceptClick = { },
            onRejectClick = { }
        )
    }
}

// ============================================================================
// SECTION 2: CUSTOM VIEW OVERRIDES
// ============================================================================

/**
 * Preview with custom title view.
 */
@Preview(showBackground = true, name = "IncomingCall - Custom Title View")
@Composable
fun PreviewIncomingCallCustomTitleView() {
    CometChatTheme {
        CometChatIncomingCall(
            call = createMockCall(),
            disableSoundForCalls = true,
            titleView = { call ->
                Text(
                    text = "★ Incoming from ${(call.callInitiator as? com.cometchat.chat.models.User)?.name ?: "Unknown"}",
                    style = CometChatTheme.typography.heading4Bold,
                    color = CometChatTheme.colorScheme.primary,
                    modifier = Modifier.padding(4.dp)
                )
            },
            onAcceptClick = { },
            onRejectClick = { }
        )
    }
}

/**
 * Preview with custom subtitle view.
 */
@Preview(showBackground = true, name = "IncomingCall - Custom Subtitle View")
@Composable
fun PreviewIncomingCallCustomSubtitleView() {
    CometChatTheme {
        CometChatIncomingCall(
            call = createMockCall(),
            disableSoundForCalls = true,
            subtitleView = { _ ->
                Text(
                    text = "🔔 Ringing...",
                    style = CometChatTheme.typography.caption1Medium,
                    color = CometChatTheme.colorScheme.textColorSecondary,
                    modifier = Modifier.padding(4.dp)
                )
            },
            onAcceptClick = { },
            onRejectClick = { }
        )
    }
}

// ============================================================================
// SECTION 3: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom style.
 */
@Preview(showBackground = true, name = "IncomingCall - Custom Style")
@Composable
fun PreviewIncomingCallCustomStyle() {
    CometChatTheme {
        CometChatIncomingCall(
            call = createMockCall(),
            disableSoundForCalls = true,
            style = CometChatIncomingCallStyle.default(),
            onAcceptClick = { },
            onRejectClick = { }
        )
    }
}

// ============================================================================
// SECTION 4: SOUND CONFIGURATION PREVIEWS
// ============================================================================

/**
 * Preview with sound disabled.
 */
@Preview(showBackground = true, name = "IncomingCall - Sound Disabled")
@Composable
fun PreviewIncomingCallSoundDisabled() {
    CometChatTheme {
        CometChatIncomingCall(
            call = createMockCall(),
            disableSoundForCalls = true,
            onAcceptClick = { },
            onRejectClick = { }
        )
    }
}
