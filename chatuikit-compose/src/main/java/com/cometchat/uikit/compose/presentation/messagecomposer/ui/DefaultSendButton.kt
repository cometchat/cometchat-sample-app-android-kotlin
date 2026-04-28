package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Default send button composable for the message composer.
 * Shows different states based on the Java chatuikit reference:
 *
 * **Normal user:**
 * - Active (has text): primaryColor background, cometchat_ic_send_active icon, clickable
 * - Inactive (no text): backgroundColor4 background, cometchat_ic_send_active icon, NOT clickable
 *
 * **Agentic user:**
 * - Active (has text): secondaryButtonBackgroundColor background, cometchat_ic_arrow_narrow_up icon, clickable
 * - Inactive (no text): backgroundColor4 background, cometchat_ic_arrow_narrow_up icon, NOT clickable
 *
 * **AI Generating (stop state — both normal and agentic):**
 * - secondaryButtonBackgroundColor background, cometchat_ic_stop icon, NOT clickable
 *
 * Uses 32dp circular container with 32dp icon inside to match the Kotlin XML implementation.
 *
 * @param modifier Modifier for the button
 * @param isActive Whether the send button is active (has content to send)
 * @param isAIGenerating Whether AI is currently generating a response
 * @param isAgentChat Whether the current conversation is with an agentic (AI bot) user
 * @param style Style configuration for the button
 * @param onClick Callback when the button is clicked
 */
@Composable
fun DefaultSendButton(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isAIGenerating: Boolean = false,
    isAgentChat: Boolean = false,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    onClick: () -> Unit = {}
) {
    // Resolve colors from theme for agent-specific states
    val secondaryButtonBgColor = CometChatTheme.colorScheme.secondaryButtonBackgroundColor

    // Determine background color based on state
    val backgroundColor = when {
        isAIGenerating -> style.sendButtonActiveBackgroundColor
        isAgentChat && isActive -> style.sendButtonActiveBackgroundColor
        isActive -> style.sendButtonActiveBackgroundColor
        else -> style.sendButtonInactiveBackgroundColor
    }

    // Determine icon based on state
    val icon = when {
        isAIGenerating -> style.sendButtonStopIcon
        isAgentChat -> painterResource(R.drawable.cometchat_ic_arrow_narrow_up)
        else -> style.sendButtonActiveIcon
    }

    // Icon tint is always white
    val iconTint = style.sendButtonIconTint

    // Stop button is NOT clickable (Java: sendButton.setClickable(false))
    // Only the active send state is clickable
    val isClickable = when {
        isAIGenerating -> false
        isActive -> true
        else -> false
    }

    // Determine content description based on state
    val description = when {
        isAIGenerating -> "AI is generating"
        isActive -> "Send message"
        else -> "Send button disabled"
    }

    IconButton(
        onClick = {
            if (isClickable) {
                onClick()
            }
        },
        enabled = isClickable,
        modifier = modifier
            .size(32.dp)
            .semantics { contentDescription = description }
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            icon?.let {
                Icon(
                    painter = it,
                    contentDescription = description,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
