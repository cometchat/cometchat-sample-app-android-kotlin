package com.cometchat.uikit.compose.presentation.conversations.style

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.AvatarStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount.BadgeCountStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.date.DateStyle
import com.cometchat.uikit.compose.presentation.shared.receipts.CometChatReceiptsStyle
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatConversationListItem component.
 *
 * This immutable data class encapsulates all visual styling properties for the conversation list item,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the list item
 * @param selectedBackgroundColor Background color when the item is selected
 * @param titleTextColor Text color for the conversation title
 * @param titleTextStyle Text style for the conversation title
 * @param subtitleTextColor Text color for the subtitle (last message preview)
 * @param subtitleTextStyle Text style for the subtitle
 * @param messageTypeIconTint Tint color for message type icons (photo, video, etc.)
 * @param avatarStyle Style configuration for the avatar component
 * @param statusIndicatorStyle Style configuration for the status indicator
 * @param dateStyle Style configuration for the date component
 * @param badgeStyle Style configuration for the unread badge component
 * @param receiptStyle Style configuration for message receipts
 * @param typingIndicatorStyle Style configuration for typing indicator
 * @param checkBoxStrokeWidth Stroke width for the selection checkbox
 * @param checkBoxCornerRadius Corner radius for the selection checkbox
 * @param checkBoxStrokeColor Stroke color for the unselected checkbox
 * @param checkBoxBackgroundColor Background color for the unselected checkbox
 * @param checkBoxCheckedBackgroundColor Background color for the selected checkbox
 * @param checkBoxSelectIcon Icon to display when checkbox is selected
 * @param checkBoxSelectIconTint Tint color for the checkbox select icon
 * @param separatorColor Color for the item separator line
 * @param separatorHeight Height of the item separator line
 */
@Immutable
data class CometChatConversationListItemStyle(
    val backgroundColor: Color,
    val selectedBackgroundColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val messageTypeIconTint: Color,
    val avatarStyle: AvatarStyle,
    val statusIndicatorStyle: CometChatStatusIndicatorStyle,
    val dateStyle: DateStyle,
    val badgeStyle: BadgeCountStyle,
    val receiptStyle: CometChatReceiptsStyle,
    val typingIndicatorStyle: TypingIndicatorStyle,
    val checkBoxStrokeWidth: Dp,
    val checkBoxCornerRadius: Dp,
    val checkBoxStrokeColor: Color,
    val checkBoxBackgroundColor: Color,
    val checkBoxCheckedBackgroundColor: Color,
    val checkBoxSelectIcon: ImageVector,
    val checkBoxSelectIconTint: Color,
    val separatorColor: Color,
    val separatorHeight: Dp
) {
    companion object {
        /**
         * Creates a default CometChatConversationListItemStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatConversationListItemStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = Color.Transparent,
            selectedBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor4,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            messageTypeIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            avatarStyle: AvatarStyle = AvatarStyle.default(),
            statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle.default(),
            dateStyle: DateStyle = DateStyle.default(),
            badgeStyle: BadgeCountStyle = BadgeCountStyle.default(),
            receiptStyle: CometChatReceiptsStyle = CometChatReceiptsStyle.default(),
            typingIndicatorStyle: TypingIndicatorStyle = TypingIndicatorStyle.default(),
            checkBoxStrokeWidth: Dp = 1.5.dp,
            checkBoxCornerRadius: Dp = 4.dp,
            checkBoxStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            checkBoxBackgroundColor: Color = Color.Transparent,
            checkBoxCheckedBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            checkBoxSelectIcon: ImageVector = Icons.Default.Check,
            checkBoxSelectIconTint: Color = CometChatTheme.colorScheme.colorWhite,
            separatorColor: Color = Color.Transparent,
            separatorHeight: Dp = 0.dp
        ): CometChatConversationListItemStyle = CometChatConversationListItemStyle(
            backgroundColor = backgroundColor,
            selectedBackgroundColor = selectedBackgroundColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            messageTypeIconTint = messageTypeIconTint,
            avatarStyle = avatarStyle,
            statusIndicatorStyle = statusIndicatorStyle,
            dateStyle = dateStyle,
            badgeStyle = badgeStyle,
            receiptStyle = receiptStyle,
            typingIndicatorStyle = typingIndicatorStyle,
            checkBoxStrokeWidth = checkBoxStrokeWidth,
            checkBoxCornerRadius = checkBoxCornerRadius,
            checkBoxStrokeColor = checkBoxStrokeColor,
            checkBoxBackgroundColor = checkBoxBackgroundColor,
            checkBoxCheckedBackgroundColor = checkBoxCheckedBackgroundColor,
            checkBoxSelectIcon = checkBoxSelectIcon,
            checkBoxSelectIconTint = checkBoxSelectIconTint,
            separatorColor = separatorColor,
            separatorHeight = separatorHeight
        )
    }
}