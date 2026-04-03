package com.cometchat.uikit.compose.presentation.imageviewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.imageviewer.style.CometChatImageViewerStyle

/**
 * Toolbar overlay for the image viewer with back and share actions.
 *
 * Animates in/out vertically based on [isVisible]. Renders a semi-transparent
 * row at the top of the screen with back navigation (left) and share (right) buttons.
 *
 * @param isVisible Whether the toolbar is visible (animates slide in/out)
 * @param style Style configuration for colors
 * @param onBackClick Callback when the back button is pressed
 * @param onShareClick Callback when the share button is pressed
 */
@Composable
internal fun ToolbarOverlay(
    isVisible: Boolean,
    style: CometChatImageViewerStyle,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
                .background(style.toolbarBackgroundColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = style.iconTintColor
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share",
                    tint = style.iconTintColor
                )
            }
        }
    }
}
