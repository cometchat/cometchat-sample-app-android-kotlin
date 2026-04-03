package com.cometchat.uikit.compose.presentation.messageheader.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.compose.shared.views.popupmenu.CometChatPopupMenuStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatMessageHeader.
 * Contains all visual styling properties for the message header component.
 * 
 * This style class follows the CometChat UIKit pattern of sourcing all default
 * values from CometChatTheme, never using Material Theme colors directly.
 * 
 * @param backgroundColor Background color for the header container
 * @param strokeColor Border/stroke color for the header container
 * @param strokeWidth Border/stroke width for the header container
 * @param cornerRadius Corner radius for the header container
 * @param titleTextColor Color for the title text (user/group name)
 * @param titleTextStyle Typography style for the title text
 * @param subtitleTextColor Color for the subtitle text (status/member count)
 * @param subtitleTextStyle Typography style for the subtitle text
 * @param backIcon Icon for the back navigation button
 * @param backIconTint Tint color for the back icon
 * @param menuIcon Icon for the overflow menu button
 * @param menuIconTint Tint color for the menu icon
 * @param typingIndicatorTextColor Color for the typing indicator text
 * @param typingIndicatorTextStyle Typography style for the typing indicator text
 * @param avatarStyle Style configuration for the avatar component
 * @param statusIndicatorStyle Style configuration for the status indicator component
 * @param newChatIcon Icon for the AI new chat button
 * @param newChatIconTint Tint color for the new chat icon
 * @param chatHistoryIcon Icon for the AI chat history button
 * @param chatHistoryIconTint Tint color for the chat history icon
 * @param videoCallIcon Icon for the video call button
 * @param videoCallIconTint Tint color for the video call icon
 * @param voiceCallIcon Icon for the voice call button
 * @param voiceCallIconTint Tint color for the voice call icon
 * @param popupMenuStyle Style configuration for the popup menu component
 */
@Immutable
data class CometChatMessageHeaderStyle(
    // Container styling
    val backgroundColor: Color,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val cornerRadius: Dp,
    
    // Title styling
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    
    // Subtitle styling
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    
    // Back icon styling
    val backIcon: Painter?,
    val backIconTint: Color,
    
    // Menu icon styling
    val menuIcon: Painter?,
    val menuIconTint: Color,
    
    // Typing indicator styling
    val typingIndicatorTextColor: Color,
    val typingIndicatorTextStyle: TextStyle,
    
    // Component styles
    val avatarStyle: AvatarStyle,
    val statusIndicatorStyle: CometChatStatusIndicatorStyle,
    
    // AI assistant button styling
    val newChatIcon: Painter?,
    val newChatIconTint: Color,
    val chatHistoryIcon: Painter?,
    val chatHistoryIconTint: Color,
    
    // Call button styling
    val videoCallIcon: Painter?,
    val videoCallIconTint: Color,
    val voiceCallIcon: Painter?,
    val voiceCallIconTint: Color,
    
    // Popup menu style
    val popupMenuStyle: CometChatPopupMenuStyle
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Does NOT use Material Theme colors directly.
         * 
         * @return A new CometChatMessageHeaderStyle instance with theme-based default values
         */
        @Composable
        fun default(
            // Container styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            strokeColor: Color = Color.Transparent,
            strokeWidth: Dp = 0.dp,
            cornerRadius: Dp = 0.dp,
            
            // Title styling
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            
            // Subtitle styling
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // Back icon styling
            backIcon: Painter? = painterResource(R.drawable.cometchat_ic_back),
            backIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            
            // Menu icon styling
            menuIcon: Painter? = painterResource(R.drawable.cometchat_ic_menu_dots),
            menuIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            
            // Typing indicator styling
            typingIndicatorTextColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            typingIndicatorTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // Component styles
            avatarStyle: AvatarStyle = AvatarStyle.default(),
            statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle.default(),
            
            // AI assistant button styling
            newChatIcon: Painter? = painterResource(R.drawable.cometchat_ic_new_chat),
            newChatIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            chatHistoryIcon: Painter? = painterResource(R.drawable.cometchat_ic_chat_history),
            chatHistoryIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            
            // Call button styling - use iconTintPrimary to match original Java chatuikit behavior
            videoCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_call_video),
            videoCallIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            voiceCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_call_voice),
            voiceCallIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            
            // Popup menu style
            popupMenuStyle: CometChatPopupMenuStyle = CometChatPopupMenuStyle.default()
        ): CometChatMessageHeaderStyle = CometChatMessageHeaderStyle(
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            backIcon = backIcon,
            backIconTint = backIconTint,
            menuIcon = menuIcon,
            menuIconTint = menuIconTint,
            typingIndicatorTextColor = typingIndicatorTextColor,
            typingIndicatorTextStyle = typingIndicatorTextStyle,
            avatarStyle = avatarStyle,
            statusIndicatorStyle = statusIndicatorStyle,
            newChatIcon = newChatIcon,
            newChatIconTint = newChatIconTint,
            chatHistoryIcon = chatHistoryIcon,
            chatHistoryIconTint = chatHistoryIconTint,
            videoCallIcon = videoCallIcon,
            videoCallIconTint = videoCallIconTint,
            voiceCallIcon = voiceCallIcon,
            voiceCallIconTint = voiceCallIconTint,
            popupMenuStyle = popupMenuStyle
        )
    }
}
