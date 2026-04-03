package com.cometchat.uikit.compose.presentation.groups.style

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatGroupsItem component.
 *
 * This immutable data class encapsulates all visual styling properties for the groups list item,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * Corresponds to XML attributes in attr_cometchat_groups.xml:
 * - cometchatGroupsItemBackgroundColor
 * - cometchatGroupsItemSelectedBackgroundColor
 * - cometchatGroupsItemTitleTextColor
 * - cometchatGroupsItemTitleTextAppearance
 * - cometchatGroupsSubtitleColor
 * - cometchatGroupsSubtitleTextAppearance
 * - cometchatGroupsAvatarStyle
 * - cometchatGroupsStatusIndicator
 * - cometchatGroupsCheckBox* properties
 *
 * @param backgroundColor Background color for the list item
 * @param selectedBackgroundColor Background color when the item is selected
 * @param titleTextColor Text color for the group name
 * @param titleTextStyle Text style for the group name
 * @param subtitleTextColor Text color for the subtitle (member count)
 * @param subtitleTextStyle Text style for the subtitle
 * @param avatarStyle Style configuration for the avatar component
 * @param statusIndicatorStyle Style configuration for the group type indicator
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
data class CometChatGroupsItemStyle(
    val backgroundColor: Color,
    val selectedBackgroundColor: Color,
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    val avatarStyle: CometChatAvatarStyle,
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
         * Creates a default CometChatGroupsItemStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatGroupsItemStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = Color.Transparent,
            selectedBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default(),
            statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle.default(),
            checkBoxStrokeWidth: Dp = 1.dp,
            checkBoxCornerRadius: Dp = 4.dp,
            checkBoxStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            checkBoxBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            checkBoxCheckedBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            checkBoxSelectIcon: ImageVector = Icons.Default.Check,
            checkBoxSelectIconTint: Color = CometChatTheme.colorScheme.colorWhite,
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            separatorHeight: Dp = 1.dp
        ): CometChatGroupsItemStyle = CometChatGroupsItemStyle(
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
