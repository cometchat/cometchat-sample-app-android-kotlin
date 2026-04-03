package com.cometchat.uikit.compose.presentation.shared.statusindicator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatStatusIndicator.
 * Mirrors the styling capabilities of the View-based implementation.
 *
 * Corresponds to XML attributes in attr_cometchat_status_indicator.xml:
 * - cometchatStatusIndicatorStrokeWidth
 * - cometchatStatusIndicatorStrokeColor
 * - cometchatStatusIndicatorCornerRadius
 * - cometchatStatusIndicatorOnlineIcon
 * - cometchatStatusIndicatorPrivateGroupIcon
 * - cometchatStatusIndicatorProtectedGroupIcon
 *
 * @param strokeWidth Width of the border stroke around the indicator
 * @param strokeColor Color of the border stroke
 * @param cornerRadius Corner radius of the indicator
 * @param backgroundColor Background color of the indicator container
 * @param onlineIcon Icon to display when status is ONLINE
 * @param privateGroupIcon Icon to display when status is PRIVATE_GROUP
 * @param protectedGroupIcon Icon to display when status is PROTECTED_GROUP
 * @param size Size of the status indicator
 */
@Immutable
data class CometChatStatusIndicatorStyle(
    val strokeWidth: Dp,
    val strokeColor: Color,
    val cornerRadius: Dp,
    val backgroundColor: Color,
    val onlineIcon: Painter?,
    val privateGroupIcon: Painter?,
    val protectedGroupIcon: Painter?,
    val size: Dp
) {
    companion object {
        /**
         * Creates a default CometChatStatusIndicatorStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatStatusIndicatorStyle instance with theme-based default values
         */
        @Composable
        fun default(
            strokeWidth: Dp = 2.dp,
            strokeColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cornerRadius: Dp = 8.dp,
            backgroundColor: Color = Color.Transparent,
            onlineIcon: Painter? = painterResource(R.drawable.cometchat_ic_online),
            privateGroupIcon: Painter? = painterResource(R.drawable.cometchat_ic_private_group),
            protectedGroupIcon: Painter? = painterResource(R.drawable.cometchat_ic_protected_group),
            size: Dp = 15.dp
        ): CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle(
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            cornerRadius = cornerRadius,
            backgroundColor = backgroundColor,
            onlineIcon = onlineIcon,
            privateGroupIcon = privateGroupIcon,
            protectedGroupIcon = protectedGroupIcon,
            size = size
        )
    }
}
