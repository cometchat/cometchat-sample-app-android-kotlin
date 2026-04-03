package com.cometchat.uikit.compose.presentation.users.style

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
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatUsersListItem component.
 *
 * This immutable data class encapsulates all visual styling properties for the user list item,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the list item
 * @param selectedBackgroundColor Background color when the item is selected
 * @param titleTextColor Text color for the user name
 * @param titleTextStyle Text style for the user name
 * @param subtitleTextColor Text color for the subtitle (if any)
 * @param subtitleTextStyle Text style for the subtitle
 * @param avatarStyle Style configuration for the avatar component
 * @param statusIndicatorStyle Style configuration for the status indicator
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
data class CometChatUsersListItemStyle(
    val backgroundColor: Color,
    val selectedBackgroundColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val avatarStyle: AvatarStyle,
    val statusIndicatorStyle: CometChatStatusIndicatorStyle,
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
         * Creates a default CometChatUsersListItemStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatUsersListItemStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = Color.Transparent,
            selectedBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor4,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            avatarStyle: AvatarStyle = AvatarStyle.default(),
            statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle.default(),
            checkBoxStrokeWidth: Dp = 1.5.dp,
            checkBoxCornerRadius: Dp = 4.dp,
            checkBoxStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            checkBoxBackgroundColor: Color = Color.Transparent,
            checkBoxCheckedBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            checkBoxSelectIcon: ImageVector = Icons.Default.Check,
            checkBoxSelectIconTint: Color = CometChatTheme.colorScheme.colorWhite,
            separatorColor: Color = Color.Transparent,
            separatorHeight: Dp = 0.dp
        ): CometChatUsersListItemStyle = CometChatUsersListItemStyle(
            backgroundColor = backgroundColor,
            selectedBackgroundColor = selectedBackgroundColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            avatarStyle = avatarStyle,
            statusIndicatorStyle = statusIndicatorStyle,
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
