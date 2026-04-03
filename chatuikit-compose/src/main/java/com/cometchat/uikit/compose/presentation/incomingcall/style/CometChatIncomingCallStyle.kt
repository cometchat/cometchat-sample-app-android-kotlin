package com.cometchat.uikit.compose.presentation.incomingcall.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatIncomingCall.
 * Contains all visual styling properties for the incoming call component.
 *
 * This style class provides parity with the XML attributes defined in
 * `attr_cometchat_incoming_call.xml` in chatuikit-kotlin.
 *
 * @param backgroundColor Background color of the incoming call container
 * @param cornerRadius Corner radius of the incoming call container
 * @param strokeWidth Width of the container border stroke
 * @param strokeColor Color of the container border stroke
 * @param titleTextColor Color of the caller name text
 * @param titleTextStyle Typography style for the caller name text
 * @param subtitleTextColor Color of the call type subtitle text
 * @param subtitleTextStyle Typography style for the call type subtitle text
 * @param iconTint Tint color for the call type icon (audio/video)
 * @param voiceCallIcon Icon displayed for voice/audio calls
 * @param videoCallIcon Icon displayed for video calls
 * @param acceptButtonBackgroundColor Background color of the accept call button
 * @param acceptButtonTextColor Text color of the accept call button
 * @param acceptButtonTextStyle Typography style for the accept button text
 * @param rejectButtonBackgroundColor Background color of the reject/decline call button
 * @param rejectButtonTextColor Text color of the reject/decline call button
 * @param rejectButtonTextStyle Typography style for the reject button text
 * @param avatarStyle Style configuration for the caller's avatar
 */
@Immutable
data class CometChatIncomingCallStyle(
    // Container styling
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color,
    
    // Title styling (caller name)
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    
    // Subtitle styling (call type)
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    
    // Icon styling
    val iconTint: Color,
    val voiceCallIcon: Painter?,
    val videoCallIcon: Painter?,
    
    // Accept button styling
    val acceptButtonBackgroundColor: Color,
    val acceptButtonTextColor: Color,
    val acceptButtonTextStyle: TextStyle,
    
    // Reject button styling
    val rejectButtonBackgroundColor: Color,
    val rejectButtonTextColor: Color,
    val rejectButtonTextStyle: TextStyle,
    
    // Avatar styling
    val avatarStyle: CometChatAvatarStyle
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Does NOT use Material Theme colors directly.
         *
         * @param backgroundColor Background color of the incoming call container
         * @param cornerRadius Corner radius of the incoming call container
         * @param strokeWidth Width of the container border stroke
         * @param strokeColor Color of the container border stroke
         * @param titleTextColor Color of the caller name text
         * @param titleTextStyle Typography style for the caller name text
         * @param subtitleTextColor Color of the call type subtitle text
         * @param subtitleTextStyle Typography style for the call type subtitle text
         * @param iconTint Tint color for the call type icon
         * @param voiceCallIcon Icon displayed for voice/audio calls
         * @param videoCallIcon Icon displayed for video calls
         * @param acceptButtonBackgroundColor Background color of the accept call button
         * @param acceptButtonTextColor Text color of the accept call button
         * @param acceptButtonTextStyle Typography style for the accept button text
         * @param rejectButtonBackgroundColor Background color of the reject/decline call button
         * @param rejectButtonTextColor Text color of the reject/decline call button
         * @param rejectButtonTextStyle Typography style for the reject button text
         * @param avatarStyle Style configuration for the caller's avatar
         * @return A new CometChatIncomingCallStyle instance with theme-based default values
         */
        @Composable
        fun default(
            // Container styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent,
            
            // Title styling (caller name)
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading2Bold,
            
            // Subtitle styling (call type)
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // Icon styling
            iconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            voiceCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_call_voice),
            videoCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_call_video),
            
            // Accept button styling - success color for accept action
            acceptButtonBackgroundColor: Color = CometChatTheme.colorScheme.successColor,
            acceptButtonTextColor: Color = CometChatTheme.colorScheme.colorWhite,
            acceptButtonTextStyle: TextStyle = CometChatTheme.typography.buttonMedium,
            
            // Reject button styling - error color for reject action
            rejectButtonBackgroundColor: Color = CometChatTheme.colorScheme.errorColor,
            rejectButtonTextColor: Color = CometChatTheme.colorScheme.colorWhite,
            rejectButtonTextStyle: TextStyle = CometChatTheme.typography.buttonMedium,
            
            // Avatar styling
            avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default()
        ): CometChatIncomingCallStyle = CometChatIncomingCallStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            iconTint = iconTint,
            voiceCallIcon = voiceCallIcon,
            videoCallIcon = videoCallIcon,
            acceptButtonBackgroundColor = acceptButtonBackgroundColor,
            acceptButtonTextColor = acceptButtonTextColor,
            acceptButtonTextStyle = acceptButtonTextStyle,
            rejectButtonBackgroundColor = rejectButtonBackgroundColor,
            rejectButtonTextColor = rejectButtonTextColor,
            rejectButtonTextStyle = rejectButtonTextStyle,
            avatarStyle = avatarStyle
        )
    }
}
