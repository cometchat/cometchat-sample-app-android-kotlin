package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle

/**
 * Animated attachment button that transitions between "+" and "x" icons.
 * The icon rotates 45 degrees when the attachment popup is expanded,
 * creating a smooth visual transition from "+" to "x".
 *
 * Uses 24dp icon size to match Figma design specifications.
 *
 * @param modifier Modifier for the button
 * @param isExpanded Whether the attachment popup is currently expanded
 * @param style Style configuration for the button
 * @param onClick Callback when the button is clicked
 */
@Composable
fun AnimatedAttachmentButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    onClick: () -> Unit = {}
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "attachment_icon_rotation"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(24.dp)
            .semantics { 
                contentDescription = if (isExpanded) "Close attachments" else "Open attachments"
            }
    ) {
        style.attachmentIcon?.let { icon ->
            Icon(
                painter = icon,
                contentDescription = null,
                tint = style.attachmentIconTint,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
            )
        }
    }
}

/**
 * Default secondary button composable for the message composer.
 * Contains only the attachment button (left side of compose box).
 * Voice recording is now in the auxiliary button area to match chatuikit-kotlin layout.
 * 
 * Uses 24dp icon size to match Figma design specifications.
 *
 * @param modifier Modifier for the button row
 * @param hideAttachmentButton Whether to hide the attachment button
 * @param isAttachmentPopupExpanded Whether the attachment popup is currently expanded
 * @param style Style configuration for the buttons
 * @param onAttachmentClick Callback when the attachment button is clicked
 */
@Composable
fun DefaultSecondaryButton(
    modifier: Modifier = Modifier,
    hideAttachmentButton: Boolean = false,
    hideVoiceRecordingButton: Boolean = false, // Kept for API compatibility, but not used here
    isAttachmentPopupExpanded: Boolean = false,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    onAttachmentClick: () -> Unit = {},
    onVoiceRecordClick: () -> Unit = {} // Kept for API compatibility, but not used here
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animated attachment button (left side)
        if (!hideAttachmentButton) {
            AnimatedAttachmentButton(
                isExpanded = isAttachmentPopupExpanded,
                style = style,
                onClick = onAttachmentClick
            )
        }
    }
}
