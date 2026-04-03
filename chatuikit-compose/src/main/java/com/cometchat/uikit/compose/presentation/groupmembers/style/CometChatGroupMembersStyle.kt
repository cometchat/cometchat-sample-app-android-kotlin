package com.cometchat.uikit.compose.presentation.groupmembers.style

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
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingStateStyle
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatGroupMembers.
 * Contains all visual styling properties for the group members list component.
 *
 * Corresponds to XML attributes in attr_cometchat_group_members.xml:
 * - Container: backgroundColor, cornerRadius, strokeColor, strokeWidth
 * - Toolbar: titleTextColor, titleTextAppearance, backIconTint, backIcon
 * - Search: searchInput* attributes
 * - List Items: itemTitleTextColor, itemTitleTextAppearance, separatorColor
 * - Selection: discardSelectionIcon, submitSelectionIcon, checkBox* attributes
 * - Nested Styles: avatarStyle, statusIndicatorStyle
 * - States: emptyState*, errorState* attributes
 */
@Immutable
data class CometChatGroupMembersStyle(
    // Container styling
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeColor: Color,
    val strokeWidth: Dp,
    
    // Toolbar styling
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val backIcon: Painter?,
    val backIconTint: Color,
    
    // Search box styling
    val searchBackgroundColor: Color,
    val searchTextColor: Color,
    val searchTextStyle: TextStyle,
    val searchPlaceholderColor: Color,
    val searchStartIcon: Painter?,
    val searchStartIconTint: Color,
    val searchEndIcon: Painter?,
    val searchEndIconTint: Color,
    val searchCornerRadius: Dp,
    val searchStrokeWidth: Dp,
    val searchStrokeColor: Color,
    
    // List item styling
    val itemBackgroundColor: Color,
    val itemSelectedBackgroundColor: Color,
    val itemTitleTextColor: Color,
    val itemTitleTextStyle: TextStyle,
    val itemScopeTextColor: Color,
    val itemScopeTextStyle: TextStyle,
    
    // Scope chip styling (matches original Java MaterialCardView scope badge)
    val scopeChipOwnerBackgroundColor: Color,
    val scopeChipOwnerTextColor: Color,
    val scopeChipBackgroundColor: Color,
    val scopeChipTextColor: Color,
    val scopeChipStrokeColor: Color,
    val scopeChipStrokeWidth: Dp,
    val scopeChipCornerRadius: Dp,
    val scopeChipPaddingHorizontal: Dp,
    val scopeChipPaddingVertical: Dp,
    
    // Separator styling
    val separatorColor: Color,
    val separatorHeight: Dp,
    
    // Selection mode styling
    val discardSelectionIcon: Painter?,
    val discardSelectionIconTint: Color,
    val submitSelectionIcon: Painter?,
    val submitSelectionIconTint: Color,
    val selectionCountTextColor: Color,
    val selectionCountTextStyle: TextStyle,
    
    // Checkbox styling (for selection mode)
    val checkBoxStrokeWidth: Dp,
    val checkBoxCornerRadius: Dp,
    val checkBoxStrokeColor: Color,
    val checkBoxBackgroundColor: Color,
    val checkBoxCheckedBackgroundColor: Color,
    val checkBoxSelectIcon: Painter?,
    val checkBoxSelectIconTint: Color,
    
    // Nested component styles
    val avatarStyle: CometChatAvatarStyle,
    val statusIndicatorStyle: CometChatStatusIndicatorStyle,
    
    // State styles
    val emptyStateStyle: CometChatEmptyStateStyle,
    val errorStateStyle: CometChatErrorStateStyle,
    val loadingStateStyle: CometChatLoadingStateStyle
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Does NOT use Material Theme colors directly.
         *
         * All default values are sourced from CometChatTheme.colorScheme and CometChatTheme.typography
         * to ensure consistency with the design system and proper light/dark theme support.
         */
        @Composable
        fun default(
            // Container styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cornerRadius: Dp = 0.dp,
            strokeColor: Color = Color.Transparent,
            strokeWidth: Dp = 0.dp,
            
            // Toolbar styling
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading1Bold,
            backIcon: Painter? = painterResource(R.drawable.cometchat_ic_back),
            backIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            
            // Search box styling
            searchBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            searchTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            searchTextStyle: TextStyle = CometChatTheme.typography.heading4Regular,
            searchPlaceholderColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            searchStartIcon: Painter? = painterResource(R.drawable.cometchat_ic_search),
            searchStartIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            searchEndIcon: Painter? = null,
            searchEndIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            searchCornerRadius: Dp = 1000.dp, // Fully rounded corners (matches cometchat_radius_max)
            searchStrokeWidth: Dp = 1.dp,
            searchStrokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            
            // List item styling
            itemBackgroundColor: Color = Color.Transparent,
            itemSelectedBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            itemTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            itemTitleTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            itemScopeTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            itemScopeTextStyle: TextStyle = CometChatTheme.typography.caption1Regular,
            
            // Scope chip styling (matches original Java MaterialCardView scope badge)
            scopeChipOwnerBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            scopeChipOwnerTextColor: Color = CometChatTheme.colorScheme.textColorWhite,
            scopeChipBackgroundColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor100,
            scopeChipTextColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            scopeChipStrokeColor: Color = CometChatTheme.colorScheme.primary,
            scopeChipStrokeWidth: Dp = 1.dp,
            scopeChipCornerRadius: Dp = 1000.dp,
            scopeChipPaddingHorizontal: Dp = 12.dp,
            scopeChipPaddingVertical: Dp = 4.dp,
            
            // Separator styling - visible by default, matching XML implementation
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            separatorHeight: Dp = 0.6.dp,
            
            // Selection mode styling
            discardSelectionIcon: Painter? = painterResource(R.drawable.cometchat_ic_close),
            discardSelectionIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            submitSelectionIcon: Painter? = painterResource(R.drawable.cometchat_ic_check),
            submitSelectionIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            selectionCountTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            selectionCountTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            
            // Checkbox styling (for selection mode)
            checkBoxStrokeWidth: Dp = 1.dp,
            checkBoxCornerRadius: Dp = 4.dp,
            checkBoxStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            checkBoxBackgroundColor: Color = Color.Transparent,
            checkBoxCheckedBackgroundColor: Color = CometChatTheme.colorScheme.primary,
            checkBoxSelectIcon: Painter? = painterResource(R.drawable.cometchat_ic_check),
            checkBoxSelectIconTint: Color = CometChatTheme.colorScheme.textColorWhite,
            
            // Nested component styles
            avatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default(),
            statusIndicatorStyle: CometChatStatusIndicatorStyle = CometChatStatusIndicatorStyle.default(),
            
            // State styles - initialized from individual properties for backward compatibility
            emptyStateStyle: CometChatEmptyStateStyle = CometChatEmptyStateStyle.default(
                backgroundColor = backgroundColor,
                titleTextColor = CometChatTheme.colorScheme.textColorPrimary,
                titleTextStyle = CometChatTheme.typography.heading3Bold,
                subtitleTextColor = CometChatTheme.colorScheme.textColorSecondary,
                subtitleTextStyle = CometChatTheme.typography.bodyRegular
            ),
            errorStateStyle: CometChatErrorStateStyle = CometChatErrorStateStyle.default(
                backgroundColor = backgroundColor,
                titleTextColor = CometChatTheme.colorScheme.textColorPrimary,
                titleTextStyle = CometChatTheme.typography.heading3Bold,
                subtitleTextColor = CometChatTheme.colorScheme.textColorSecondary,
                subtitleTextStyle = CometChatTheme.typography.bodyRegular
            ),
            loadingStateStyle: CometChatLoadingStateStyle = CometChatLoadingStateStyle.default(
                backgroundColor = backgroundColor
            )
        ): CometChatGroupMembersStyle = CometChatGroupMembersStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            backIcon = backIcon,
            backIconTint = backIconTint,
            searchBackgroundColor = searchBackgroundColor,
            searchTextColor = searchTextColor,
            searchTextStyle = searchTextStyle,
            searchPlaceholderColor = searchPlaceholderColor,
            searchStartIcon = searchStartIcon,
            searchStartIconTint = searchStartIconTint,
            searchEndIcon = searchEndIcon,
            searchEndIconTint = searchEndIconTint,
            searchCornerRadius = searchCornerRadius,
            searchStrokeWidth = searchStrokeWidth,
            searchStrokeColor = searchStrokeColor,
            itemBackgroundColor = itemBackgroundColor,
            itemSelectedBackgroundColor = itemSelectedBackgroundColor,
            itemTitleTextColor = itemTitleTextColor,
            itemTitleTextStyle = itemTitleTextStyle,
            itemScopeTextColor = itemScopeTextColor,
            itemScopeTextStyle = itemScopeTextStyle,
            scopeChipOwnerBackgroundColor = scopeChipOwnerBackgroundColor,
            scopeChipOwnerTextColor = scopeChipOwnerTextColor,
            scopeChipBackgroundColor = scopeChipBackgroundColor,
            scopeChipTextColor = scopeChipTextColor,
            scopeChipStrokeColor = scopeChipStrokeColor,
            scopeChipStrokeWidth = scopeChipStrokeWidth,
            scopeChipCornerRadius = scopeChipCornerRadius,
            scopeChipPaddingHorizontal = scopeChipPaddingHorizontal,
            scopeChipPaddingVertical = scopeChipPaddingVertical,
            separatorColor = separatorColor,
            separatorHeight = separatorHeight,
            discardSelectionIcon = discardSelectionIcon,
            discardSelectionIconTint = discardSelectionIconTint,
            submitSelectionIcon = submitSelectionIcon,
            submitSelectionIconTint = submitSelectionIconTint,
            selectionCountTextColor = selectionCountTextColor,
            selectionCountTextStyle = selectionCountTextStyle,
            checkBoxStrokeWidth = checkBoxStrokeWidth,
            checkBoxCornerRadius = checkBoxCornerRadius,
            checkBoxStrokeColor = checkBoxStrokeColor,
            checkBoxBackgroundColor = checkBoxBackgroundColor,
            checkBoxCheckedBackgroundColor = checkBoxCheckedBackgroundColor,
            checkBoxSelectIcon = checkBoxSelectIcon,
            checkBoxSelectIconTint = checkBoxSelectIconTint,
            avatarStyle = avatarStyle,
            statusIndicatorStyle = statusIndicatorStyle,
            emptyStateStyle = emptyStateStyle,
            errorStateStyle = errorStateStyle,
            loadingStateStyle = loadingStateStyle
        )
    }
}
