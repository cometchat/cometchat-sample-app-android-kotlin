package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * NOTE: CometChatOutgoingCall cannot be used directly in @Preview because it
 * internally creates CometChatSoundManager, CometChatOutgoingCallViewModel with
 * SDK listeners, and references CometChatCalls/CometChatOngoingCallActivity.
 *
 * These previews simulate the OutgoingCall appearance using basic Compose primitives.
 */

// ============================================================================
// Helper: Simulated Outgoing Call UI
// ============================================================================

@Composable
private fun SimulatedOutgoingCallUI(
    recipientName: String,
    recipientInitials: String,
    subtitleText: String = "Calling...",
    avatarColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor500
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CometChatTheme.colorScheme.backgroundColor1)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section: Name + subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Text(
                text = recipientName,
                style = CometChatTheme.typography.heading2Bold,
                color = CometChatTheme.colorScheme.textColorPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitleText,
                style = CometChatTheme.typography.bodyRegular,
                color = CometChatTheme.colorScheme.textColorSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Center: Avatar
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = recipientInitials,
                style = CometChatTheme.typography.heading1Bold,
                color = Color.White
            )
        }

        // Bottom: End call button
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(CometChatTheme.colorScheme.errorColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.cometchat_ic_end_call),
                contentDescription = "End Call",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "OutgoingCall - Audio Call")
@Composable
fun PreviewOutgoingCallAudio() {
    CometChatTheme {
        SimulatedOutgoingCallUI(
            recipientName = "Alice Smith",
            recipientInitials = "AS",
            subtitleText = "Calling..."
        )
    }
}

@Preview(showBackground = true, name = "OutgoingCall - Video Call")
@Composable
fun PreviewOutgoingCallVideo() {
    CometChatTheme {
        SimulatedOutgoingCallUI(
            recipientName = "Bob Johnson",
            recipientInitials = "BJ",
            subtitleText = "Calling..."
        )
    }
}

@Preview(showBackground = true, name = "OutgoingCall - Group Call")
@Composable
fun PreviewOutgoingCallGroup() {
    CometChatTheme {
        SimulatedOutgoingCallUI(
            recipientName = "Engineering Team",
            recipientInitials = "ET",
            subtitleText = "Calling...",
            avatarColor = CometChatTheme.colorScheme.infoColor
        )
    }
}

// ============================================================================
// SECTION 2: CUSTOM VIEW PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "OutgoingCall - Custom Title")
@Composable
fun PreviewOutgoingCallCustomTitle() {
    CometChatTheme {
        SimulatedOutgoingCallUI(
            recipientName = "★ Alice Smith",
            recipientInitials = "AS",
            subtitleText = "🔔 Ringing..."
        )
    }
}

@Preview(showBackground = true, name = "OutgoingCall - Long Name")
@Composable
fun PreviewOutgoingCallLongName() {
    CometChatTheme {
        SimulatedOutgoingCallUI(
            recipientName = "Alexander Hamilton Washington III",
            recipientInitials = "AH",
            subtitleText = "Calling..."
        )
    }
}
