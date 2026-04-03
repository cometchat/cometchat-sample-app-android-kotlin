package com.cometchat.uikit.compose.presentation.outgoingcall.style

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
 * Immutable style configuration for CometChatOutgoingCall.
 * Contains all visual styling properties for the outgoing call component.
 *
 * This style class provides parity with the XML attributes defined in
 * `attr_cometchat_outgoing_call.xml` in chatuikit-kotlin.
 *
 * @param backgroundColor Background color of the outgoing call container
 * @param cornerRadius Corner radius of the outgoing call container
 * @param strokeWidth Width of the container border stroke
 * @param strokeColor Color of the container border stroke
 * @param titleTextColor Color of the recipient name text
 * @param titleTextStyle Typography style for the recipient name text
 * @param subtitleTextColor Color of the "Calling..." subtitle text
 * @param subtitleTextStyle Typography style for the subtitle text
 * @param endCallIcon Icon displayed on the end call button
 * @param endCallIconTint Tint color for the end call icon
 * @param endCallButtonBackgroundColor Background color of the end call button
 * @param avatarStyle Style configuration for the recipient's avatar
 */
@Immutable
data class CometChatOutgoingCallStyle(
    // Container styling
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color,
    
    // Title styling (recipient name)
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    
    // Subtitle styling ("Calling...")
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    
    // End call button styling
    val endCallIcon: Painter?,
    val endCallIconTint: Color,
    val endCallButtonBackgroundColor: Color,
    
    // Avatar styling
    val avatarStyle: CometChatAvatarStyle
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Does NOT use Material Theme colors directly.
         *
         * @param backgroundColor Background color of the outgoing call container
         * @param cornerRadius Corner radius of the outgoing call container
         * @param strokeWidth Width of the container border stroke
         * @param strokeColor Color of the container border stroke
         * @param titleTextColor Color of the recipient name text
         * @param titleTextStyle Typography style for the recipient name text
         * @param subtitleTextColor Color of the "Calling..." subtitle text
         * @param subtitleTextStyle Typography style for the subtitle text
         * @param endCallIcon Icon displayed on the end call button
         * @param endCallIconTint Tint color for the end call icon
         * @param endCallButtonBackgroundColor Background color of the end call button
         * @param avatarStyle Style configuration for the recipient's avatar
         * @return A new CometChatOutgoingCallStyle instance with theme-based default values
         */
        @Composable
        fun default(
            // Container styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent,
            
            // Title styling (recipient name)
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading2Bold,
            
            // Subtitle styling ("Calling...")
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // End call button styling - error color for end call action
            endCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_end_call),
            endCallIconTint: Color = CometChatTheme.colorScheme.colorWhite,
            endCallButtonBackgroundColor: Color = CometChatTheme.colorScheme.errorColor,
            
            // Avatar styling
            avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default()
        ): CometChatOutgoingCallStyle = CometChatOutgoingCallStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            endCallIcon = endCallIcon,
            endCallIconTint = endCallIconTint,
            endCallButtonBackgroundColor = endCallButtonBackgroundColor,
            avatarStyle = avatarStyle
        )
    }
}
