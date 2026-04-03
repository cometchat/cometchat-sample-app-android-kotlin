package com.cometchat.uikit.compose.presentation.ongoingcall.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Immutable style configuration for CometChatOngoingCall.
 * Contains all visual styling properties for the ongoing call component.
 *
 * This style class provides parity with the CometChatOngoingCallStyle
 * data class in chatuikit-kotlin.
 *
 * @param backgroundColor Background color of the ongoing call container
 * @param progressIndicatorColor Color of the loading progress indicator
 * @param cornerRadius Corner radius of the ongoing call container
 * @param strokeWidth Width of the container border stroke
 * @param strokeColor Color of the container border stroke
 *
 * Validates: Requirement 15.6
 */
@Immutable
data class CometChatOngoingCallStyle(
    val backgroundColor: Color,
    val progressIndicatorColor: Color,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color
) {
    companion object {
        /**
         * Creates a default style configuration with theme-based default values.
         *
         * @param backgroundColor Background color of the ongoing call container.
         *                        Defaults to dark calling background (#141414)
         * @param progressIndicatorColor Color of the loading progress indicator.
         *                               Defaults to white
         * @param cornerRadius Corner radius of the ongoing call container.
         *                     Defaults to 0.dp
         * @param strokeWidth Width of the container border stroke.
         *                    Defaults to 0.dp
         * @param strokeColor Color of the container border stroke.
         *                    Defaults to transparent
         * @return A new CometChatOngoingCallStyle instance with the specified values
         */
        @Composable
        fun default(
            backgroundColor: Color = Color(0xFF141414), // cometchat_calling_background
            progressIndicatorColor: Color = Color.White,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent
        ): CometChatOngoingCallStyle = CometChatOngoingCallStyle(
            backgroundColor = backgroundColor,
            progressIndicatorColor = progressIndicatorColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor
        )
    }
}
