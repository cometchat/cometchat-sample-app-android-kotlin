package com.cometchat.uikit.compose.preview.presentation.ui

import android.view.View
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.compose.presentation.callbuttons.style.CometChatCallButtonsStyle
import com.cometchat.uikit.compose.presentation.callbuttons.ui.CometChatCallButtons
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

/**
 * Preview showing call buttons for a user conversation.
 */
@Preview(showBackground = true, name = "CallButtons - User")
@Composable
fun PreviewCallButtonsUser() {
    CometChatTheme {
        CometChatCallButtons(
            modifier = Modifier.padding(16.dp),
            user = PreviewMockData.createMockUser(name = "Alice Smith"),
            onVoiceCallClick = { _, _ -> },
            onVideoCallClick = { _, _ -> }
        )
    }
}

/**
 * Preview showing call buttons for a group conversation.
 */
@Preview(showBackground = true, name = "CallButtons - Group")
@Composable
fun PreviewCallButtonsGroup() {
    CometChatTheme {
        CometChatCallButtons(
            modifier = Modifier.padding(16.dp),
            group = PreviewMockData.createMockGroup(name = "Engineering Team"),
            onVoiceCallClick = { _, _ -> },
            onVideoCallClick = { _, _ -> }
        )
    }
}

// ============================================================================
// SECTION 2: VISIBILITY PREVIEWS
// ============================================================================

/**
 * Preview with only voice call button visible.
 */
@Preview(showBackground = true, name = "CallButtons - Voice Only")
@Composable
fun PreviewCallButtonsVoiceOnly() {
    CometChatTheme {
        CometChatCallButtons(
            modifier = Modifier.padding(16.dp),
            user = PreviewMockData.createMockUser(name = "Alice Smith"),
            voiceCallButtonVisibility = View.VISIBLE,
            videoCallButtonVisibility = View.GONE,
            onVoiceCallClick = { _, _ -> },
            onVideoCallClick = { _, _ -> }
        )
    }
}

/**
 * Preview with only video call button visible.
 */
@Preview(showBackground = true, name = "CallButtons - Video Only")
@Composable
fun PreviewCallButtonsVideoOnly() {
    CometChatTheme {
        CometChatCallButtons(
            modifier = Modifier.padding(16.dp),
            user = PreviewMockData.createMockUser(name = "Alice Smith"),
            voiceCallButtonVisibility = View.GONE,
            videoCallButtonVisibility = View.VISIBLE,
            onVoiceCallClick = { _, _ -> },
            onVideoCallClick = { _, _ -> }
        )
    }
}

/**
 * Preview with button text visible.
 */
@Preview(showBackground = true, name = "CallButtons - With Text")
@Composable
fun PreviewCallButtonsWithText() {
    CometChatTheme {
        CometChatCallButtons(
            modifier = Modifier.padding(16.dp),
            user = PreviewMockData.createMockUser(name = "Alice Smith"),
            voiceButtonText = "Voice",
            videoButtonText = "Video",
            buttonTextVisibility = View.VISIBLE,
            onVoiceCallClick = { _, _ -> },
            onVideoCallClick = { _, _ -> }
        )
    }
}

/**
 * Preview with text only (no icons).
 */
@Preview(showBackground = true, name = "CallButtons - Text Only No Icons")
@Composable
fun PreviewCallButtonsTextOnlyNoIcons() {
    CometChatTheme {
        CometChatCallButtons(
            modifier = Modifier.padding(16.dp),
            user = PreviewMockData.createMockUser(name = "Alice Smith"),
            voiceButtonText = "Voice Call",
            videoButtonText = "Video Call",
            buttonTextVisibility = View.VISIBLE,
            buttonIconVisibility = View.GONE,
            onVoiceCallClick = { _, _ -> },
            onVideoCallClick = { _, _ -> }
        )
    }
}
