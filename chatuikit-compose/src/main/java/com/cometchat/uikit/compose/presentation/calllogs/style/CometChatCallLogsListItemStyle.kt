package com.cometchat.uikit.compose.presentation.calllogs.style

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
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.DateStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatCallLogsListItem component.
 *
 * This immutable data class encapsulates all visual styling properties for the call logs list item,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the list item
 * @param titleTextColor Text color for the caller/callee name
 * @param titleTextStyle Text style for the caller/callee name
 * @param missedCallTitleColor Text color for missed call title (error color)
 * @param subtitleTextColor Text color for the subtitle (date/time)
 * @param subtitleTextStyle Text style for the subtitle
 * @param incomingCallIcon Icon for incoming calls
 * @param incomingCallIconTint Tint color for incoming call icon
 * @param outgoingCallIcon Icon for outgoing calls
 * @param outgoingCallIconTint Tint color for outgoing call icon
 * @param missedCallIcon Icon for missed calls
 * @param missedCallIconTint Tint color for missed call icon
 * @param audioCallIcon Icon for audio calls (trailing)
 * @param audioCallIconTint Tint color for audio call icon
 * @param videoCallIcon Icon for video calls (trailing)
 * @param videoCallIconTint Tint color for video call icon
 * @param avatarStyle Style configuration for the avatar component
 * @param dateStyle Style configuration for the date component
 * @param separatorColor Color for the item separator line
 * @param separatorHeight Height of the item separator line
 */
@Immutable
data class CometChatCallLogsListItemStyle(
    val backgroundColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val missedCallTitleColor: Color,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val incomingCallIcon: Painter?,
    val incomingCallIconTint: Color,
    val outgoingCallIcon: Painter?,
    val outgoingCallIconTint: Color,
    val missedCallIcon: Painter?,
    val missedCallIconTint: Color,
    val audioCallIcon: Painter?,
    val audioCallIconTint: Color,
    val videoCallIcon: Painter?,
    val videoCallIconTint: Color,
    val avatarStyle: CometChatAvatarStyle,
    val dateStyle: DateStyle,
    val separatorColor: Color,
    val separatorHeight: Dp
) {
    companion object {
        /**
         * Creates a default CometChatCallLogsListItemStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatCallLogsListItemStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = Color.Transparent,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            missedCallTitleColor: Color = CometChatTheme.colorScheme.errorColor,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            incomingCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_incoming_call),
            incomingCallIconTint: Color = CometChatTheme.colorScheme.successColor,
            outgoingCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_outgoing_call),
            outgoingCallIconTint: Color = CometChatTheme.colorScheme.successColor,
            missedCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_missed_call),
            missedCallIconTint: Color = CometChatTheme.colorScheme.errorColor,
            audioCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_call_voice),
            audioCallIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            videoCallIcon: Painter? = painterResource(R.drawable.cometchat_ic_video_call),
            videoCallIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default(),
            dateStyle: DateStyle = DateStyle.default(),
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            separatorHeight: Dp = 1.dp
        ): CometChatCallLogsListItemStyle = CometChatCallLogsListItemStyle(
            backgroundColor = backgroundColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            missedCallTitleColor = missedCallTitleColor,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            incomingCallIcon = incomingCallIcon,
            incomingCallIconTint = incomingCallIconTint,
            outgoingCallIcon = outgoingCallIcon,
            outgoingCallIconTint = outgoingCallIconTint,
            missedCallIcon = missedCallIcon,
            missedCallIconTint = missedCallIconTint,
            audioCallIcon = audioCallIcon,
            audioCallIconTint = audioCallIconTint,
            videoCallIcon = videoCallIcon,
            videoCallIconTint = videoCallIconTint,
            avatarStyle = avatarStyle,
            dateStyle = dateStyle,
            separatorColor = separatorColor,
            separatorHeight = separatorHeight
        )
    }
}
