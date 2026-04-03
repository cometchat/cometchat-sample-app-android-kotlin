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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle

/**
 * Default send button composable for the message composer.
 * Shows different states: active (can send), inactive (nothing to send), and AI generating (stop).
 * 
 * Based on chatuikit Java implementation:
 * - Active: Primary color background with white icon
 * - Inactive: backgroundColor4 background with inactive icon
 * - AI Generating: Secondary button background with white stop icon
 * 
 * Uses 32dp circular container with 32dp icon inside to match the Kotlin XML implementation.
 * The send icon vector (cometchat_ic_send_active.xml) is 32x32 with a 24x24 clip-path,
 * so the icon has built-in padding when rendered at 32dp.
 *
 * @param modifier Modifier for the button
 * @param isActive Whether the send button is active (has content to send)
 * @param isAIGenerating Whether AI is currently generating a response
 * @param style Style configuration for the button
 * @param onClick Callback when the button is clicked
 */
@Composable
fun DefaultSendButton(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isAIGenerating: Boolean = false,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    onClick: () -> Unit = {}
) {
    // Determine background color based on state (matching chatuikit Java)
    val backgroundColor = when {
        isAIGenerating -> style.sendButtonInactiveBackgroundColor
        isActive -> style.sendButtonActiveBackgroundColor
        else -> style.sendButtonInactiveBackgroundColor
    }

    // Determine icon based on state - use active icon for all states (same icon, different background)
    val icon = when {
        isAIGenerating -> style.sendButtonStopIcon
        else -> style.sendButtonActiveIcon  // Use active icon for both active and inactive states
    }

    // Icon tint is always white (from style)
    val iconTint = style.sendButtonIconTint

    // Determine content description based on state
    val description = when {
        isAIGenerating -> "Stop AI generation"
        isActive -> "Send message"
        else -> "Send button disabled"
    }

    IconButton(
        onClick = {
            // Only allow click when active or AI is generating (to stop)
            if (isActive || isAIGenerating) {
                onClick()
            }
        },
        enabled = isActive || isAIGenerating,
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
