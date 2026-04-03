package com.cometchat.uikit.compose.presentation.callbuttons.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for an individual call button (voice or video).
 */
@Immutable
data class CallButtonStyle(
    val icon: Painter?,
    val iconTint: Color,
    val iconSize: Dp,
    val textColor: Color,
    val textStyle: TextStyle,
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color,
    val buttonPadding: Dp
)

/**
 * Style configuration for the CometChatCallButtons component.
 * Contains styles for both voice and video call buttons, plus layout configuration.
 */
@Immutable
data class CometChatCallButtonsStyle(
    val voiceCallButtonStyle: CallButtonStyle,
    val videoCallButtonStyle: CallButtonStyle,
    val marginBetweenButtons: Dp
) {
    companion object {
        /**
         * Creates a default style configuration using CometChatTheme tokens.
         */
        @Composable
        fun default(
            // Voice call button defaults
            voiceCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_call_voice),
            voiceCallIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            voiceCallIconSize: Dp = 24.dp,
            voiceCallTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            voiceCallTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            voiceCallBackgroundColor: Color = Color.Transparent,
            voiceCallCornerRadius: Dp = 8.dp,
            voiceCallStrokeWidth: Dp = 0.dp,
            voiceCallStrokeColor: Color = Color.Transparent,
            voiceCallButtonPadding: Dp = 8.dp,

            // Video call button defaults
            videoCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_call_video),
            videoCallIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            videoCallIconSize: Dp = 24.dp,
            videoCallTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            videoCallTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            videoCallBackgroundColor: Color = Color.Transparent,
            videoCallCornerRadius: Dp = 8.dp,
            videoCallStrokeWidth: Dp = 0.dp,
            videoCallStrokeColor: Color = Color.Transparent,
            videoCallButtonPadding: Dp = 8.dp,

            // Layout
            marginBetweenButtons: Dp = 16.dp
        ): CometChatCallButtonsStyle = CometChatCallButtonsStyle(
            voiceCallButtonStyle = CallButtonStyle(
                icon = voiceCallIcon,
                iconTint = voiceCallIconTint,
                iconSize = voiceCallIconSize,
                textColor = voiceCallTextColor,
                textStyle = voiceCallTextStyle,
                backgroundColor = voiceCallBackgroundColor,
                cornerRadius = voiceCallCornerRadius,
                strokeWidth = voiceCallStrokeWidth,
                strokeColor = voiceCallStrokeColor,
                buttonPadding = voiceCallButtonPadding
            ),
            videoCallButtonStyle = CallButtonStyle(
                icon = videoCallIcon,
                iconTint = videoCallIconTint,
                iconSize = videoCallIconSize,
                textColor = videoCallTextColor,
                textStyle = videoCallTextStyle,
                backgroundColor = videoCallBackgroundColor,
                cornerRadius = videoCallCornerRadius,
                strokeWidth = videoCallStrokeWidth,
                strokeColor = videoCallStrokeColor,
                buttonPadding = videoCallButtonPadding
            ),
            marginBetweenButtons = marginBetweenButtons
        )
    }
}
