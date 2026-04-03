package com.cometchat.uikit.compose.presentation.messagelist.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatAudioBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCallActionBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatCollaborativeBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatDeleteBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatFileBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatImageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMeetCallBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatPollBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatStickerBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatTextBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatVideoBubbleStyle
import com.cometchat.uikit.compose.presentation.shared.dialog.CometChatConfirmDialogStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for [CometChatMessageList] composable.
 *
 * This immutable data class defines the visual appearance of the message list,
 * including container styling, error state, empty chat greeting, date separator,
 * new message indicator, and nested bubble styles.
 *
 * ## Usage
 *
 * Use the companion object's [default] factory function to create instances
 * with [CometChatTheme] tokens:
 *
 * ```kotlin
 * // Use default style
 * CometChatMessageList(
 *     user = user,
 *     style = CometChatMessageListStyle.default()
 * )
 *
 * // Customize specific properties
 * CometChatMessageList(
 *     user = user,
 *     style = CometChatMessageListStyle.default(
 *         backgroundColor = Color.White,
 *         dateSeparatorCornerRadius = 12.dp
 *     )
 * )
 *
 * // Customize per-bubble-type styles
 * CometChatMessageList(
 *     user = user,
 *     style = CometChatMessageListStyle.default(
 *         textBubbleStyle = CometChatTextBubbleStyle.default(
 *             textColor = Color.Blue
 *         )
 *     )
 * )
 * ```
 *
 * ## Property Categories
 *
 * ### Container Styling
 * - [backgroundColor], [cornerRadius], [strokeWidth], [strokeColor]
 *
 * ### Error State
 * - [errorStateTitleTextColor], [errorStateTitleTextStyle]
 * - [errorStateSubtitleTextColor], [errorStateSubtitleTextStyle]
 *
 * ### Empty Chat Greeting
 * - [emptyChatGreetingTitleTextColor], [emptyChatGreetingTitleTextStyle]
 * - [emptyChatGreetingSubtitleTextColor], [emptyChatGreetingSubtitleTextStyle]
 *
 * ### Date Separator
 * - [dateSeparatorTextColor], [dateSeparatorTextStyle]
 * - [dateSeparatorBackgroundColor], [dateSeparatorCornerRadius]
 *
 * ### New Message Indicator
 * - [newMessageIndicatorBackgroundColor], [newMessageIndicatorTextColor]
 * - [newMessageIndicatorTextStyle], [newMessageIndicatorCornerRadius]
 * - [newMessageIndicatorElevation], [newMessageIndicatorStrokeColor]
 * - [newMessageIndicatorStrokeWidth], [newMessageIndicatorIconTint]
 * - [newMessageIndicatorIconSize], [newMessageIndicatorPadding]
 *
 * ### New Messages Separator (Unread Anchor)
 * - [newMessagesSeparatorTextColor], [newMessagesSeparatorTextStyle]
 * - [newMessagesSeparatorLineColor], [newMessagesSeparatorLineHeight]
 * - [newMessagesSeparatorVerticalPadding]
 *
 * ### Message Bubble Styles
 * - [messageBubbleStyle]: Base style for all message bubbles (wrapper properties)
 * - [incomingMessageBubbleStyle]: Style for incoming (left-aligned) message bubbles (nullable, uses alignment default if null)
 * - [outgoingMessageBubbleStyle]: Style for outgoing (right-aligned) message bubbles (nullable, uses alignment default if null)
 * - [textBubbleStyle]: Style for text message bubbles (nullable, uses default if null)
 * - [imageBubbleStyle]: Style for image message bubbles (nullable, uses default if null)
 * - [videoBubbleStyle]: Style for video message bubbles (nullable, uses default if null)
 * - [audioBubbleStyle]: Style for audio message bubbles (nullable, uses default if null)
 * - [fileBubbleStyle]: Style for file message bubbles (nullable, uses default if null)
 * - [deleteBubbleStyle]: Style for deleted message bubbles (nullable, uses default if null)
 * - [stickerBubbleStyle]: Style for sticker message bubbles (nullable, uses default if null)
 * - [pollBubbleStyle]: Style for poll message bubbles (nullable, uses default if null)
 * - [collaborativeBubbleStyle]: Style for collaborative message bubbles (nullable, uses default if null)
 * - [meetCallBubbleStyle]: Style for meet call message bubbles (nullable, uses default if null)
 * - [actionBubbleStyle]: Style for action message bubbles (nullable, uses default if null)
 * - [callActionBubbleStyle]: Style for call action message bubbles (nullable, uses default if null)
 *
 * @property backgroundColor The background color of the message list container.
 * @property cornerRadius The corner radius of the message list container.
 * @property strokeWidth The width of the border stroke (0.dp for no border).
 * @property strokeColor The color of the border stroke.
 * @property errorStateTitleTextColor The color of error state title text.
 * @property errorStateTitleTextStyle The text style for error state title.
 * @property errorStateSubtitleTextColor The color of error state subtitle text.
 * @property errorStateSubtitleTextStyle The text style for error state subtitle.
 * @property emptyChatGreetingTitleTextColor The color of empty chat greeting title text.
 * @property emptyChatGreetingTitleTextStyle The text style for empty chat greeting title.
 * @property emptyChatGreetingSubtitleTextColor The color of empty chat greeting subtitle text.
 * @property emptyChatGreetingSubtitleTextStyle The text style for empty chat greeting subtitle.
 * @property dateSeparatorTextColor The color of date separator text.
 * @property dateSeparatorTextStyle The text style for date separator.
 * @property dateSeparatorBackgroundColor The background color of date separator.
 * @property dateSeparatorCornerRadius The corner radius of date separator.
 * @property dateSeparatorStrokeWidth The stroke width of date separator border.
 * @property dateSeparatorStrokeColor The stroke color of date separator border.
 * @property newMessageIndicatorBackgroundColor The background color of new message indicator.
 * @property newMessageIndicatorTextColor The text color of new message indicator (deprecated, kept for backward compatibility).
 * @property newMessageIndicatorTextStyle The text style for new message indicator (deprecated, kept for backward compatibility).
 * @property newMessageIndicatorCornerRadius The corner radius of new message indicator.
 * @property newMessageIndicatorElevation The elevation/shadow depth of new message indicator.
 * @property newMessageIndicatorStrokeColor The border color of new message indicator.
 * @property newMessageIndicatorStrokeWidth The border width of new message indicator.
 * @property newMessageIndicatorIconTint The tint color for the arrow icon in new message indicator.
 * @property newMessageIndicatorIconSize The size of the arrow icon in new message indicator.
 * @property newMessageIndicatorPadding The padding inside the new message indicator container.
 * @property newMessagesSeparatorTextColor The text color of the "New" separator above unread messages.
 * @property newMessagesSeparatorTextStyle The text style for the "New" separator.
 * @property newMessagesSeparatorLineColor The color of the horizontal lines in the separator.
 * @property newMessagesSeparatorLineHeight The height/thickness of the separator lines.
 * @property newMessagesSeparatorVerticalPadding The vertical padding around the separator.
 * @property messageBubbleStyle The base style for all message bubbles containing shared wrapper properties.
 * @property incomingMessageBubbleStyle The style for incoming (left-aligned) message bubbles, or null to use alignment-based defaults.
 * @property outgoingMessageBubbleStyle The style for outgoing (right-aligned) message bubbles, or null to use alignment-based defaults.
 * @property textBubbleStyle The style for text message bubbles, or null to use factory defaults.
 * @property imageBubbleStyle The style for image message bubbles, or null to use factory defaults.
 * @property videoBubbleStyle The style for video message bubbles, or null to use factory defaults.
 * @property audioBubbleStyle The style for audio message bubbles, or null to use factory defaults.
 * @property fileBubbleStyle The style for file message bubbles, or null to use factory defaults.
 * @property deleteBubbleStyle The style for deleted message bubbles, or null to use factory defaults.
 * @property stickerBubbleStyle The style for sticker message bubbles, or null to use factory defaults.
 * @property pollBubbleStyle The style for poll message bubbles, or null to use factory defaults.
 * @property collaborativeBubbleStyle The style for collaborative message bubbles, or null to use factory defaults.
 * @property meetCallBubbleStyle The style for meet call message bubbles, or null to use factory defaults.
 * @property actionBubbleStyle The style for action message bubbles, or null to use factory defaults.
 * @property callActionBubbleStyle The style for call action message bubbles, or null to use factory defaults.
 *
 * @see CometChatMessageList
 * @see CometChatMessageBubbleStyle
 * @see CometChatTheme
 */
@Immutable
data class CometChatMessageListStyle(
    // Container
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color,
    
    // Error state
    val errorStateTitleTextColor: Color,
    val errorStateTitleTextStyle: TextStyle,
    val errorStateSubtitleTextColor: Color,
    val errorStateSubtitleTextStyle: TextStyle,
    
    // Empty chat greeting
    val emptyChatGreetingTitleTextColor: Color,
    val emptyChatGreetingTitleTextStyle: TextStyle,
    val emptyChatGreetingSubtitleTextColor: Color,
    val emptyChatGreetingSubtitleTextStyle: TextStyle,
    
    // Date separator
    val dateSeparatorTextColor: Color,
    val dateSeparatorTextStyle: TextStyle,
    val dateSeparatorBackgroundColor: Color,
    val dateSeparatorCornerRadius: Dp,
    val dateSeparatorStrokeWidth: Dp,
    val dateSeparatorStrokeColor: Color,
    
    // New message indicator
    val newMessageIndicatorBackgroundColor: Color,
    val newMessageIndicatorTextColor: Color,
    val newMessageIndicatorTextStyle: TextStyle,
    val newMessageIndicatorCornerRadius: Dp,
    val newMessageIndicatorElevation: Dp,
    val newMessageIndicatorStrokeColor: Color,
    val newMessageIndicatorStrokeWidth: Dp,
    val newMessageIndicatorIconTint: Color,
    val newMessageIndicatorIconSize: Dp,
    val newMessageIndicatorPadding: Dp,
    
    // New messages separator (unread anchor indicator)
    val newMessagesSeparatorTextColor: Color,
    val newMessagesSeparatorTextStyle: TextStyle,
    val newMessagesSeparatorLineColor: Color,
    val newMessagesSeparatorLineHeight: Dp,
    val newMessagesSeparatorVerticalPadding: Dp,
    
    // Message bubble style (base style for all bubbles)
    val messageBubbleStyle: CometChatMessageBubbleStyle,
    
    // Incoming/outgoing bubble style objects
    val incomingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    val outgoingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
    
    // Per-bubble-type styles (nullable — null means use factory defaults)
    val textBubbleStyle: CometChatTextBubbleStyle? = null,
    val imageBubbleStyle: CometChatImageBubbleStyle? = null,
    val videoBubbleStyle: CometChatVideoBubbleStyle? = null,
    val audioBubbleStyle: CometChatAudioBubbleStyle? = null,
    val fileBubbleStyle: CometChatFileBubbleStyle? = null,
    val deleteBubbleStyle: CometChatDeleteBubbleStyle? = null,
    val pollBubbleStyle: CometChatPollBubbleStyle? = null,
    val stickerBubbleStyle: CometChatStickerBubbleStyle? = null,
    val collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle? = null,
    val meetCallBubbleStyle: CometChatMeetCallBubbleStyle? = null,
    val actionBubbleStyle: CometChatActionBubbleStyle? = null,
    val callActionBubbleStyle: CometChatCallActionBubbleStyle? = null,
    
    // Message information style
    val messageInformationStyle: CometChatMessageInformationStyle? = null,
    
    // Delete dialog style
    val deleteDialogStyle: CometChatConfirmDialogStyle? = null
) {
    companion object {
        /**
         * Creates a default message list style using CometChat theme tokens.
         *
         * All parameters have sensible defaults from [CometChatTheme] and can be
         * overridden individually.
         *
         * @param backgroundColor The background color, defaults to backgroundColor3
         * @param cornerRadius The corner radius, defaults to 0.dp
         * @param strokeWidth The border width, defaults to 0.dp (no border)
         * @param strokeColor The border color, defaults to transparent
         * @param errorStateTitleTextColor The error title color, defaults to textColorPrimary
         * @param errorStateTitleTextStyle The error title style, defaults to heading3Bold
         * @param errorStateSubtitleTextColor The error subtitle color, defaults to textColorSecondary
         * @param errorStateSubtitleTextStyle The error subtitle style, defaults to bodyRegular
         * @param emptyChatGreetingTitleTextColor The greeting title color, defaults to textColorPrimary
         * @param emptyChatGreetingTitleTextStyle The greeting title style, defaults to heading2Bold
         * @param emptyChatGreetingSubtitleTextColor The greeting subtitle color, defaults to textColorSecondary
         * @param emptyChatGreetingSubtitleTextStyle The greeting subtitle style, defaults to bodyRegular
         * @param dateSeparatorTextColor The date separator text color, defaults to textColorPrimary
         * @param dateSeparatorTextStyle The date separator text style, defaults to caption1Medium
         * @param dateSeparatorBackgroundColor The date separator background, defaults to backgroundColor2
         * @param dateSeparatorCornerRadius The date separator corner radius, defaults to 4.dp
         * @param dateSeparatorStrokeWidth The date separator stroke width, defaults to 1.dp
         * @param dateSeparatorStrokeColor The date separator stroke color, defaults to strokeColorDark
         * @param newMessageIndicatorBackgroundColor The indicator background, defaults to backgroundColor1
         * @param newMessageIndicatorTextColor The indicator text color, defaults to textColorWhite (deprecated)
         * @param newMessageIndicatorTextStyle The indicator text style, defaults to caption1Medium (deprecated)
         * @param newMessageIndicatorCornerRadius The indicator corner radius, defaults to 28.dp (pill shape)
         * @param newMessageIndicatorElevation The indicator elevation/shadow, defaults to 8.dp
         * @param newMessageIndicatorStrokeColor The indicator border color, defaults to strokeColorLight
         * @param newMessageIndicatorStrokeWidth The indicator border width, defaults to 1.dp
         * @param newMessageIndicatorIconTint The indicator icon tint, defaults to iconTintSecondary
         * @param newMessageIndicatorIconSize The indicator icon size, defaults to 24.dp
         * @param newMessageIndicatorPadding The indicator padding, defaults to 12.dp
         * @param newMessagesSeparatorTextColor The separator text color, defaults to errorColor
         * @param newMessagesSeparatorTextStyle The separator text style, defaults to caption1Medium
         * @param newMessagesSeparatorLineColor The separator line color, defaults to errorColor
         * @param newMessagesSeparatorLineHeight The separator line height, defaults to 1.dp
         * @param newMessagesSeparatorVerticalPadding The separator vertical padding, defaults to 8.dp
         * @param messageBubbleStyle The base message bubble style, defaults to default()
         * @param incomingMessageBubbleStyle The incoming (left-aligned) message bubble style, defaults to null (use alignment-based defaults)
         * @param outgoingMessageBubbleStyle The outgoing (right-aligned) message bubble style, defaults to null (use alignment-based defaults)
         * @param textBubbleStyle The text bubble style, defaults to null (use factory defaults)
         * @param imageBubbleStyle The image bubble style, defaults to null (use factory defaults)
         * @param videoBubbleStyle The video bubble style, defaults to null (use factory defaults)
         * @param audioBubbleStyle The audio bubble style, defaults to null (use factory defaults)
         * @param fileBubbleStyle The file bubble style, defaults to null (use factory defaults)
         * @param deleteBubbleStyle The delete bubble style, defaults to null (use factory defaults)
         * @param pollBubbleStyle The poll bubble style, defaults to null (use factory defaults)
         * @param stickerBubbleStyle The sticker bubble style, defaults to null (use factory defaults)
         * @param collaborativeBubbleStyle The collaborative bubble style, defaults to null (use factory defaults)
         * @param meetCallBubbleStyle The meet call bubble style, defaults to null (use factory defaults)
         * @param actionBubbleStyle The action bubble style, defaults to null (use factory defaults)
         * @param callActionBubbleStyle The call action bubble style, defaults to null (use factory defaults)
         * @return A new [CometChatMessageListStyle] instance
         */
        @Composable
        fun default(
            // Container
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = Color.Transparent,
            
            // Error state
            errorStateTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            errorStateTitleTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            errorStateSubtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            errorStateSubtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // Empty chat greeting
            emptyChatGreetingTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            emptyChatGreetingTitleTextStyle: TextStyle = CometChatTheme.typography.heading2Bold,
            emptyChatGreetingSubtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            emptyChatGreetingSubtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // Date separator
            dateSeparatorTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            dateSeparatorTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            dateSeparatorBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            dateSeparatorCornerRadius: Dp = 4.dp,
            dateSeparatorStrokeWidth: Dp = 1.dp,
            dateSeparatorStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDark,
            
            // New message indicator
            newMessageIndicatorBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            newMessageIndicatorTextColor: Color = CometChatTheme.colorScheme.textColorWhite,
            newMessageIndicatorTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            newMessageIndicatorCornerRadius: Dp = 28.dp,
            newMessageIndicatorElevation: Dp = 8.dp,
            newMessageIndicatorStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            newMessageIndicatorStrokeWidth: Dp = 1.dp,
            newMessageIndicatorIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            newMessageIndicatorIconSize: Dp = 24.dp,
            newMessageIndicatorPadding: Dp = 12.dp,
            
            // New messages separator (unread anchor indicator)
            newMessagesSeparatorTextColor: Color = CometChatTheme.colorScheme.errorColor,
            newMessagesSeparatorTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            newMessagesSeparatorLineColor: Color = CometChatTheme.colorScheme.errorColor,
            newMessagesSeparatorLineHeight: Dp = 1.dp,
            newMessagesSeparatorVerticalPadding: Dp = 8.dp,
            
            // Message bubble style (base style for all bubbles)
            messageBubbleStyle: CometChatMessageBubbleStyle = CometChatMessageBubbleStyle.default(),
            
            // Incoming/outgoing bubble style objects
            incomingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
            outgoingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
            
            // Per-bubble-type styles (nullable — null means use factory defaults)
            textBubbleStyle: CometChatTextBubbleStyle? = null,
            imageBubbleStyle: CometChatImageBubbleStyle? = null,
            videoBubbleStyle: CometChatVideoBubbleStyle? = null,
            audioBubbleStyle: CometChatAudioBubbleStyle? = null,
            fileBubbleStyle: CometChatFileBubbleStyle? = null,
            deleteBubbleStyle: CometChatDeleteBubbleStyle? = null,
            pollBubbleStyle: CometChatPollBubbleStyle? = null,
            stickerBubbleStyle: CometChatStickerBubbleStyle? = null,
            collaborativeBubbleStyle: CometChatCollaborativeBubbleStyle? = null,
            meetCallBubbleStyle: CometChatMeetCallBubbleStyle? = null,
            actionBubbleStyle: CometChatActionBubbleStyle? = null,
            callActionBubbleStyle: CometChatCallActionBubbleStyle? = null,
            
            // Message information style
            messageInformationStyle: CometChatMessageInformationStyle? = null,
            
            // Delete dialog style
            deleteDialogStyle: CometChatConfirmDialogStyle? = null
        ): CometChatMessageListStyle = CometChatMessageListStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            errorStateTitleTextColor = errorStateTitleTextColor,
            errorStateTitleTextStyle = errorStateTitleTextStyle,
            errorStateSubtitleTextColor = errorStateSubtitleTextColor,
            errorStateSubtitleTextStyle = errorStateSubtitleTextStyle,
            emptyChatGreetingTitleTextColor = emptyChatGreetingTitleTextColor,
            emptyChatGreetingTitleTextStyle = emptyChatGreetingTitleTextStyle,
            emptyChatGreetingSubtitleTextColor = emptyChatGreetingSubtitleTextColor,
            emptyChatGreetingSubtitleTextStyle = emptyChatGreetingSubtitleTextStyle,
            dateSeparatorTextColor = dateSeparatorTextColor,
            dateSeparatorTextStyle = dateSeparatorTextStyle,
            dateSeparatorBackgroundColor = dateSeparatorBackgroundColor,
            dateSeparatorCornerRadius = dateSeparatorCornerRadius,
            dateSeparatorStrokeWidth = dateSeparatorStrokeWidth,
            dateSeparatorStrokeColor = dateSeparatorStrokeColor,
            newMessageIndicatorBackgroundColor = newMessageIndicatorBackgroundColor,
            newMessageIndicatorTextColor = newMessageIndicatorTextColor,
            newMessageIndicatorTextStyle = newMessageIndicatorTextStyle,
            newMessageIndicatorCornerRadius = newMessageIndicatorCornerRadius,
            newMessageIndicatorElevation = newMessageIndicatorElevation,
            newMessageIndicatorStrokeColor = newMessageIndicatorStrokeColor,
            newMessageIndicatorStrokeWidth = newMessageIndicatorStrokeWidth,
            newMessageIndicatorIconTint = newMessageIndicatorIconTint,
            newMessageIndicatorIconSize = newMessageIndicatorIconSize,
            newMessageIndicatorPadding = newMessageIndicatorPadding,
            newMessagesSeparatorTextColor = newMessagesSeparatorTextColor,
            newMessagesSeparatorTextStyle = newMessagesSeparatorTextStyle,
            newMessagesSeparatorLineColor = newMessagesSeparatorLineColor,
            newMessagesSeparatorLineHeight = newMessagesSeparatorLineHeight,
            newMessagesSeparatorVerticalPadding = newMessagesSeparatorVerticalPadding,
            messageBubbleStyle = messageBubbleStyle,
            incomingMessageBubbleStyle = incomingMessageBubbleStyle,
            outgoingMessageBubbleStyle = outgoingMessageBubbleStyle,
            textBubbleStyle = textBubbleStyle,
            imageBubbleStyle = imageBubbleStyle,
            videoBubbleStyle = videoBubbleStyle,
            audioBubbleStyle = audioBubbleStyle,
            fileBubbleStyle = fileBubbleStyle,
            deleteBubbleStyle = deleteBubbleStyle,
            pollBubbleStyle = pollBubbleStyle,
            stickerBubbleStyle = stickerBubbleStyle,
            collaborativeBubbleStyle = collaborativeBubbleStyle,
            meetCallBubbleStyle = meetCallBubbleStyle,
            actionBubbleStyle = actionBubbleStyle,
            callActionBubbleStyle = callActionBubbleStyle,
            messageInformationStyle = messageInformationStyle,
            deleteDialogStyle = deleteDialogStyle
        )
    }
}
