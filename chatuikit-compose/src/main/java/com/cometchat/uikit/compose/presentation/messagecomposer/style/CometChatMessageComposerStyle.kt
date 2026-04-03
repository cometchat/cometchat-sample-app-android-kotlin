package com.cometchat.uikit.compose.presentation.messagecomposer.style

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
 * Immutable style configuration for CometChatMessageComposer.
 * Contains all visual styling properties for the message composer component.
 *
 * This style class follows the CometChat UIKit pattern of sourcing all default
 * values from CometChatTheme, never using Material Theme colors directly.
 *
 * @param backgroundColor Background color for the composer container
 * @param strokeColor Border/stroke color for the composer container
 * @param strokeWidth Border/stroke width for the composer container
 * @param cornerRadius Corner radius for the composer container
 * @param composeBoxBackgroundColor Background color for the compose box
 * @param composeBoxStrokeColor Border/stroke color for the compose box
 * @param composeBoxStrokeWidth Border/stroke width for the compose box
 * @param composeBoxCornerRadius Corner radius for the compose box
 * @param separatorColor Color for the separator between input and auxiliary buttons
 * @param attachmentIcon Icon for the attachment button
 * @param attachmentIconTint Tint color for the attachment icon
 * @param voiceRecordingIcon Icon for the voice recording button
 * @param voiceRecordingIconTint Tint color for the voice recording icon
 * @param aiIcon Icon for the AI button
 * @param aiIconTint Tint color for the AI icon
 * @param stickerIcon Icon for the sticker/emoji button
 * @param stickerIconTint Tint color for the sticker icon
 * @param sendButtonActiveIcon Icon for the send button when active
 * @param sendButtonInactiveIcon Icon for the send button when inactive
 * @param sendButtonActiveBackgroundColor Background color for active send button
 * @param sendButtonInactiveBackgroundColor Background color for inactive send button
 * @param sendButtonStopIcon Icon for the stop button during AI generation
 * @param editPreviewTitleTextColor Color for the edit preview title text
 * @param editPreviewTitleTextStyle Typography style for the edit preview title
 * @param editPreviewMessageTextColor Color for the edit preview message text
 * @param editPreviewMessageTextStyle Typography style for the edit preview message
 * @param editPreviewBackgroundColor Background color for the edit preview panel
 * @param editPreviewCornerRadius Corner radius for the edit preview panel
 * @param editPreviewStrokeColor Border/stroke color for the edit preview panel
 * @param editPreviewStrokeWidth Border/stroke width for the edit preview panel
 * @param editPreviewCloseIcon Icon for the edit preview close button
 * @param editPreviewCloseIconTint Tint color for the edit preview close icon
 */
@Immutable
data class CometChatMessageComposerStyle(
    // Container styling
    val backgroundColor: Color,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val cornerRadius: Dp,

    // Compose box styling
    val composeBoxBackgroundColor: Color,
    val composeBoxStrokeColor: Color,
    val composeBoxStrokeWidth: Dp,
    val composeBoxCornerRadius: Dp,

    // Separator
    val separatorColor: Color,

    // Attachment button styling
    val attachmentIcon: Painter?,
    val attachmentIconTint: Color,

    // Voice recording button styling
    val voiceRecordingIcon: Painter?,
    val voiceRecordingIconTint: Color,

    // AI button styling
    val aiIcon: Painter?,
    val aiIconTint: Color,

    // Sticker button styling
    val stickerIcon: Painter?,
    val stickerIconTint: Color,

    // Send button styling
    val sendButtonActiveIcon: Painter?,
    val sendButtonInactiveIcon: Painter?,
    val sendButtonActiveBackgroundColor: Color,
    val sendButtonInactiveBackgroundColor: Color,
    val sendButtonIconTint: Color,
    val sendButtonStopIcon: Painter?,

    // Edit preview styling
    val editPreviewTitleTextColor: Color,
    val editPreviewTitleTextStyle: TextStyle,
    val editPreviewMessageTextColor: Color,
    val editPreviewMessageTextStyle: TextStyle,
    val editPreviewBackgroundColor: Color,
    val editPreviewCornerRadius: Dp,
    val editPreviewStrokeColor: Color,
    val editPreviewStrokeWidth: Dp,
    val editPreviewCloseIcon: Painter?,
    val editPreviewCloseIconTint: Color,

    // Message preview styling (for reply/quote)
    val messagePreviewSeparatorColor: Color,
    val messagePreviewTitleTextColor: Color,
    val messagePreviewTitleTextStyle: TextStyle,
    val messagePreviewSubtitleTextColor: Color,
    val messagePreviewSubtitleTextStyle: TextStyle,
    val messagePreviewBackgroundColor: Color,
    val messagePreviewCornerRadius: Dp,
    val messagePreviewStrokeColor: Color,
    val messagePreviewStrokeWidth: Dp,
    val messagePreviewCloseIcon: Painter?,
    val messagePreviewCloseIconTint: Color,

    // Info panel styling
    val infoIcon: Painter?,
    val infoIconTint: Color,
    val infoTextColor: Color,
    val infoTextStyle: TextStyle,
    val infoBackgroundColor: Color,
    val infoCornerRadius: Dp,
    val infoStrokeColor: Color,
    val infoStrokeWidth: Dp,

    // Input text styling
    val inputTextColor: Color,
    val inputTextStyle: TextStyle,
    val inputPlaceholderColor: Color,
    val inputPlaceholderStyle: TextStyle,

    // Rich text toolbar styling (toolbar is inside compose box, visibility is automatic)
    val richTextToolbarBackgroundColor: Color,
    val richTextToolbarIconTint: Color,
    val richTextToolbarActiveIconTint: Color,

    // Rich text toggle button styling (DEPRECATED - toggle button removed, toolbar visibility is automatic)
    // These properties are kept for backward compatibility but are no longer used
    val richTextToggleIcon: Painter?,
    val richTextToggleIconTint: Color,
    val richTextToggleIconActiveTint: Color,

    // Link edit dialog styling
    val linkDialogBackgroundColor: Color,
    val linkDialogTitleTextColor: Color,
    val linkDialogTitleTextStyle: TextStyle,
    val linkDialogInputBackgroundColor: Color,
    val linkDialogInputTextColor: Color,
    val linkDialogInputTextStyle: TextStyle,
    val linkDialogButtonTextColor: Color,
    val linkDialogButtonTextStyle: TextStyle
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Does NOT use Material Theme colors directly.
         *
         * @return A new CometChatMessageComposerStyle instance with theme-based default values
         */
        @Composable
        fun default(
            // Container styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            strokeColor: Color = Color.Transparent,
            strokeWidth: Dp = 0.dp,
            cornerRadius: Dp = 0.dp,

            // Compose box styling - 4dp border radius
            composeBoxBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            composeBoxStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            composeBoxStrokeWidth: Dp = 1.dp,
            composeBoxCornerRadius: Dp = 8.dp,

            // Separator
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,

            // Attachment button styling
            attachmentIcon: Painter? = painterResource(R.drawable.cometchat_ic_attachment_unselected),
            attachmentIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,

            // Voice recording button styling
            voiceRecordingIcon: Painter? = painterResource(R.drawable.cometchat_ic_mic),
            voiceRecordingIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,

            // AI button styling
            aiIcon: Painter? = painterResource(R.drawable.cometchat_ic_ai),
            aiIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,

            // Sticker button styling
            stickerIcon: Painter? = painterResource(R.drawable.cometchat_ic_sticker),
            stickerIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,

            // Send button styling
            sendButtonActiveIcon: Painter? = painterResource(R.drawable.cometchat_ic_send_active),
            sendButtonInactiveIcon: Painter? = painterResource(R.drawable.cometchat_ic_send_inactive),
            sendButtonActiveBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            sendButtonInactiveBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor4,
            sendButtonIconTint: Color = CometChatTheme.colorScheme.colorWhite,
            sendButtonStopIcon: Painter? = painterResource(R.drawable.cometchat_ic_stop),

            // Edit preview styling
            editPreviewTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            editPreviewTitleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            editPreviewMessageTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            editPreviewMessageTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            editPreviewBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            editPreviewCornerRadius: Dp = 8.dp,
            editPreviewStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            editPreviewStrokeWidth: Dp = 1.dp,
            editPreviewCloseIcon: Painter? = painterResource(R.drawable.cometchat_ic_close),
            editPreviewCloseIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,

            // Message preview styling (for reply/quote)
            messagePreviewSeparatorColor: Color = CometChatTheme.colorScheme.primary,
            messagePreviewTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            messagePreviewTitleTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            messagePreviewSubtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            messagePreviewSubtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            messagePreviewBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            messagePreviewCornerRadius: Dp = 8.dp,
            messagePreviewStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            messagePreviewStrokeWidth: Dp = 1.dp,
            messagePreviewCloseIcon: Painter? = painterResource(R.drawable.cometchat_ic_close),
            messagePreviewCloseIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,

            // Info panel styling
            infoIcon: Painter? = painterResource(R.drawable.cometchat_ic_info),
            infoIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            infoTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            infoTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            infoBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            infoCornerRadius: Dp = 8.dp,
            infoStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            infoStrokeWidth: Dp = 1.dp,

            // Input text styling
            inputTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            inputTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            inputPlaceholderColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            inputPlaceholderStyle: TextStyle = CometChatTheme.typography.bodyRegular,

            // Rich text toolbar styling (toolbar is inside compose box, visibility is automatic)
            richTextToolbarBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            richTextToolbarIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            richTextToolbarActiveIconTint: Color = CometChatTheme.colorScheme.primary,

            // Rich text toggle button styling (DEPRECATED - toggle button removed, toolbar visibility is automatic)
            // These properties are kept for backward compatibility but are no longer used
            richTextToggleIcon: Painter? = painterResource(R.drawable.cometchat_ic_edit),
            richTextToggleIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            richTextToggleIconActiveTint: Color = CometChatTheme.colorScheme.primary,

            // Link edit dialog styling
            linkDialogBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            linkDialogTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            linkDialogTitleTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            linkDialogInputBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            linkDialogInputTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            linkDialogInputTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            linkDialogButtonTextColor: Color = CometChatTheme.colorScheme.primary,
            linkDialogButtonTextStyle: TextStyle = CometChatTheme.typography.bodyMedium
        ): CometChatMessageComposerStyle = CometChatMessageComposerStyle(
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            composeBoxBackgroundColor = composeBoxBackgroundColor,
            composeBoxStrokeColor = composeBoxStrokeColor,
            composeBoxStrokeWidth = composeBoxStrokeWidth,
            composeBoxCornerRadius = composeBoxCornerRadius,
            separatorColor = separatorColor,
            attachmentIcon = attachmentIcon,
            attachmentIconTint = attachmentIconTint,
            voiceRecordingIcon = voiceRecordingIcon,
            voiceRecordingIconTint = voiceRecordingIconTint,
            aiIcon = aiIcon,
            aiIconTint = aiIconTint,
            stickerIcon = stickerIcon,
            stickerIconTint = stickerIconTint,
            sendButtonActiveIcon = sendButtonActiveIcon,
            sendButtonInactiveIcon = sendButtonInactiveIcon,
            sendButtonActiveBackgroundColor = sendButtonActiveBackgroundColor,
            sendButtonInactiveBackgroundColor = sendButtonInactiveBackgroundColor,
            sendButtonIconTint = sendButtonIconTint,
            sendButtonStopIcon = sendButtonStopIcon,
            editPreviewTitleTextColor = editPreviewTitleTextColor,
            editPreviewTitleTextStyle = editPreviewTitleTextStyle,
            editPreviewMessageTextColor = editPreviewMessageTextColor,
            editPreviewMessageTextStyle = editPreviewMessageTextStyle,
            editPreviewBackgroundColor = editPreviewBackgroundColor,
            editPreviewCornerRadius = editPreviewCornerRadius,
            editPreviewStrokeColor = editPreviewStrokeColor,
            editPreviewStrokeWidth = editPreviewStrokeWidth,
            editPreviewCloseIcon = editPreviewCloseIcon,
            editPreviewCloseIconTint = editPreviewCloseIconTint,
            messagePreviewSeparatorColor = messagePreviewSeparatorColor,
            messagePreviewTitleTextColor = messagePreviewTitleTextColor,
            messagePreviewTitleTextStyle = messagePreviewTitleTextStyle,
            messagePreviewSubtitleTextColor = messagePreviewSubtitleTextColor,
            messagePreviewSubtitleTextStyle = messagePreviewSubtitleTextStyle,
            messagePreviewBackgroundColor = messagePreviewBackgroundColor,
            messagePreviewCornerRadius = messagePreviewCornerRadius,
            messagePreviewStrokeColor = messagePreviewStrokeColor,
            messagePreviewStrokeWidth = messagePreviewStrokeWidth,
            messagePreviewCloseIcon = messagePreviewCloseIcon,
            messagePreviewCloseIconTint = messagePreviewCloseIconTint,
            infoIcon = infoIcon,
            infoIconTint = infoIconTint,
            infoTextColor = infoTextColor,
            infoTextStyle = infoTextStyle,
            infoBackgroundColor = infoBackgroundColor,
            infoCornerRadius = infoCornerRadius,
            infoStrokeColor = infoStrokeColor,
            infoStrokeWidth = infoStrokeWidth,
            inputTextColor = inputTextColor,
            inputTextStyle = inputTextStyle,
            inputPlaceholderColor = inputPlaceholderColor,
            inputPlaceholderStyle = inputPlaceholderStyle,
            richTextToolbarBackgroundColor = richTextToolbarBackgroundColor,
            richTextToolbarIconTint = richTextToolbarIconTint,
            richTextToolbarActiveIconTint = richTextToolbarActiveIconTint,
            richTextToggleIcon = richTextToggleIcon,
            richTextToggleIconTint = richTextToggleIconTint,
            richTextToggleIconActiveTint = richTextToggleIconActiveTint,
            linkDialogBackgroundColor = linkDialogBackgroundColor,
            linkDialogTitleTextColor = linkDialogTitleTextColor,
            linkDialogTitleTextStyle = linkDialogTitleTextStyle,
            linkDialogInputBackgroundColor = linkDialogInputBackgroundColor,
            linkDialogInputTextColor = linkDialogInputTextColor,
            linkDialogInputTextStyle = linkDialogInputTextStyle,
            linkDialogButtonTextColor = linkDialogButtonTextColor,
            linkDialogButtonTextStyle = linkDialogButtonTextStyle
        )
    }
}
