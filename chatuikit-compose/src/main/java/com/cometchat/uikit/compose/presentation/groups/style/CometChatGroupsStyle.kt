package com.cometchat.uikit.compose.presentation.groups.style

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
import com.cometchat.uikit.compose.shared.views.popupmenu.CometChatPopupMenuStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatGroups.
 * Contains all visual styling properties for the groups list component.
 *
 * Corresponds to XML attributes in attr_cometchat_groups.xml.
 *
 * @param backgroundColor Background color for the component container
 * @param strokeColor Stroke color for the component border
 * @param strokeWidth Stroke width for the component border
 * @param cornerRadius Corner radius for the component container
 * @param titleTextColor Text color for the toolbar title
 * @param titleTextStyle Text style for the toolbar title
 * @param backIcon Icon for the back button
 * @param backIconTint Tint color for the back icon
 * @param toolbarSeparatorColor Color for the toolbar separator line
 * @param toolbarSeparatorHeight Height of the toolbar separator line
 * @param showToolbarSeparator Whether to show the toolbar separator
 * @param emptyStateTitleTextColor Text color for empty state title
 * @param emptyStateTitleTextStyle Text style for empty state title
 * @param emptyStateSubtitleTextColor Text color for empty state subtitle
 * @param emptyStateSubtitleTextStyle Text style for empty state subtitle
 * @param emptyStateIcon Icon for empty state
 * @param errorStateTitleTextColor Text color for error state title
 * @param errorStateTitleTextStyle Text style for error state title
 * @param errorStateSubtitleTextColor Text color for error state subtitle
 * @param errorStateSubtitleTextStyle Text style for error state subtitle
 * @param errorStateIcon Icon for error state
 * @param retryButtonTextColor Text color for retry button
 * @param retryButtonTextStyle Text style for retry button
 * @param retryButtonBackgroundColor Background color for retry button
 * @param retryButtonStrokeColor Stroke color for retry button
 * @param retryButtonStrokeWidth Stroke width for retry button
 * @param retryButtonCornerRadius Corner radius for retry button
 * @param searchBackgroundColor Background color for search box
 * @param searchTextColor Text color for search input
 * @param searchTextStyle Text style for search input
 * @param searchPlaceholderColor Placeholder text color for search input
 * @param searchPlaceholderTextStyle Placeholder text style for search input
 * @param searchStartIcon Start icon for search box
 * @param searchStartIconTint Tint color for search start icon
 * @param searchEndIcon End icon for search box
 * @param searchEndIconTint Tint color for search end icon
 * @param searchCornerRadius Corner radius for search box
 * @param searchStrokeWidth Stroke width for search box
 * @param searchStrokeColor Stroke color for search box
 * @param discardSelectionIcon Icon for discard selection action
 * @param discardSelectionIconTint Tint color for discard selection icon
 * @param submitSelectionIcon Icon for submit selection action
 * @param submitSelectionIconTint Tint color for submit selection icon
 * @param selectionCountTextColor Text color for selection count
 * @param selectionCountTextStyle Text style for selection count
 * @param itemStyle Style configuration for list items
 * @param popupMenuStyle Style configuration for popup menu
 * @param emptyStateStyle Style configuration for empty state
 * @param errorStateStyle Style configuration for error state
 * @param loadingStateStyle Style configuration for loading state
 * @param selectedGroupsAvatarStyle Style for avatars in selected groups list
 * @param selectedGroupsItemTextColor Text color for selected groups item
 * @param selectedGroupsItemTextStyle Text style for selected groups item
 * @param selectedGroupsRemoveIcon Icon for removing selected group
 * @param selectedGroupsRemoveIconTint Tint color for remove icon
 */
@Immutable
data class CometChatGroupsStyle(
    // Container styling
    val backgroundColor: Color,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val cornerRadius: Dp,

    // Toolbar styling
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    val backIcon: Painter?,
    val backIconTint: Color,
    val toolbarSeparatorColor: Color,
    val toolbarSeparatorHeight: Dp,
    val showToolbarSeparator: Boolean,

    // Empty state styling
    val emptyStateTitleTextColor: Color,
    val emptyStateTitleTextStyle: TextStyle,
    val emptyStateSubtitleTextColor: Color,
    val emptyStateSubtitleTextStyle: TextStyle,
    val emptyStateIcon: Painter?,

    // Error state styling
    val errorStateTitleTextColor: Color,
    val errorStateTitleTextStyle: TextStyle,
    val errorStateSubtitleTextColor: Color,
    val errorStateSubtitleTextStyle: TextStyle,
    val errorStateIcon: Painter?,

    // Retry button styling
    val retryButtonTextColor: Color,
    val retryButtonTextStyle: TextStyle,
    val retryButtonBackgroundColor: Color,
    val retryButtonStrokeColor: Color,
    val retryButtonStrokeWidth: Dp,
    val retryButtonCornerRadius: Dp,

    // Search box styling
    val searchBackgroundColor: Color,
    val searchTextColor: Color,
    val searchTextStyle: TextStyle,
    val searchPlaceholderColor: Color,
    val searchPlaceholderTextStyle: TextStyle,
    val searchStartIcon: Painter?,
    val searchStartIconTint: Color,
    val searchEndIcon: Painter?,
    val searchEndIconTint: Color,
    val searchCornerRadius: Dp,
    val searchStrokeWidth: Dp,
    val searchStrokeColor: Color,

    // Selection mode styling
    val discardSelectionIcon: Painter?,
    val discardSelectionIconTint: Color,
    val submitSelectionIcon: Painter?,
    val submitSelectionIconTint: Color,
    val selectionCountTextColor: Color,
    val selectionCountTextStyle: TextStyle,

    // Item styling
    val itemStyle: CometChatGroupsItemStyle,

    // Popup menu styling
    val popupMenuStyle: CometChatPopupMenuStyle,

    // Shared state styles
    val emptyStateStyle: CometChatEmptyStateStyle,
    val errorStateStyle: CometChatErrorStateStyle,
    val loadingStateStyle: CometChatLoadingStateStyle,

    // Selected groups list styling
    val selectedGroupsAvatarStyle: CometChatAvatarStyle,
    val selectedGroupsItemTextColor: Color,
    val selectedGroupsItemTextStyle: TextStyle,
    val selectedGroupsRemoveIcon: Painter?,
    val selectedGroupsRemoveIconTint: Color
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Does NOT use Material Theme colors directly.
         */
        @Composable
        fun default(
            // Container styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            strokeColor: Color = Color.Transparent,
            strokeWidth: Dp = 0.dp,
            cornerRadius: Dp = 0.dp,

            // Toolbar styling
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading1Bold,
            backIcon: Painter? = painterResource(R.drawable.cometchat_ic_back),
            backIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            toolbarSeparatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            toolbarSeparatorHeight: Dp = 1.dp,
            showToolbarSeparator: Boolean = true,

            // Empty state styling
            emptyStateTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            emptyStateTitleTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            emptyStateSubtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            emptyStateSubtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            emptyStateIcon: Painter? = null,

            // Error state styling
            errorStateTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            errorStateTitleTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            errorStateSubtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            errorStateSubtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            errorStateIcon: Painter? = null,

            // Retry button styling
            retryButtonTextColor: Color = CometChatTheme.colorScheme.primary,
            retryButtonTextStyle: TextStyle = CometChatTheme.typography.buttonMedium,
            retryButtonBackgroundColor: Color = Color.Transparent,
            retryButtonStrokeColor: Color = CometChatTheme.colorScheme.primary,
            retryButtonStrokeWidth: Dp = 1.dp,
            retryButtonCornerRadius: Dp = 8.dp,

            // Search box styling
            searchBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            searchTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            searchTextStyle: TextStyle = CometChatTheme.typography.heading4Regular,
            searchPlaceholderColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            searchPlaceholderTextStyle: TextStyle = CometChatTheme.typography.heading4Regular,
            searchStartIcon: Painter? = painterResource(R.drawable.cometchat_ic_search),
            searchStartIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            searchEndIcon: Painter? = null,
            searchEndIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            searchCornerRadius: Dp = 1000.dp,
            searchStrokeWidth: Dp = 0.dp,
            searchStrokeColor: Color = Color.Transparent,

            // Selection mode styling
            discardSelectionIcon: Painter? = painterResource(R.drawable.cometchat_ic_close),
            discardSelectionIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            submitSelectionIcon: Painter? = painterResource(R.drawable.cometchat_ic_check),
            submitSelectionIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            selectionCountTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            selectionCountTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,

            // Item styling
            itemStyle: CometChatGroupsItemStyle = CometChatGroupsItemStyle.default(),

            // Popup menu styling
            popupMenuStyle: CometChatPopupMenuStyle = CometChatPopupMenuStyle.default(),

            // Shared state styles
            emptyStateStyle: CometChatEmptyStateStyle = CometChatEmptyStateStyle.default(
                backgroundColor = backgroundColor,
                icon = emptyStateIcon,
                iconTint = emptyStateSubtitleTextColor,
                titleTextColor = emptyStateTitleTextColor,
                titleTextStyle = emptyStateTitleTextStyle,
                subtitleTextColor = emptyStateSubtitleTextColor,
                subtitleTextStyle = emptyStateSubtitleTextStyle
            ),
            errorStateStyle: CometChatErrorStateStyle = CometChatErrorStateStyle.default(
                backgroundColor = backgroundColor,
                icon = errorStateIcon,
                iconTint = errorStateSubtitleTextColor,
                titleTextColor = errorStateTitleTextColor,
                titleTextStyle = errorStateTitleTextStyle,
                subtitleTextColor = errorStateSubtitleTextColor,
                subtitleTextStyle = errorStateSubtitleTextStyle
            ),
            loadingStateStyle: CometChatLoadingStateStyle = CometChatLoadingStateStyle.default(
                backgroundColor = backgroundColor
            ),

            // Selected groups list styling
            selectedGroupsAvatarStyle: CometChatAvatarStyle = CometChatAvatarStyle.default(),
            selectedGroupsItemTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            selectedGroupsItemTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,
            selectedGroupsRemoveIcon: Painter? = painterResource(R.drawable.cometchat_ic_close),
            selectedGroupsRemoveIconTint: Color = CometChatTheme.colorScheme.colorWhite
        ): CometChatGroupsStyle = CometChatGroupsStyle(
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            backIcon = backIcon,
            backIconTint = backIconTint,
            toolbarSeparatorColor = toolbarSeparatorColor,
            toolbarSeparatorHeight = toolbarSeparatorHeight,
            showToolbarSeparator = showToolbarSeparator,
            emptyStateTitleTextColor = emptyStateTitleTextColor,
            emptyStateTitleTextStyle = emptyStateTitleTextStyle,
            emptyStateSubtitleTextColor = emptyStateSubtitleTextColor,
            emptyStateSubtitleTextStyle = emptyStateSubtitleTextStyle,
            emptyStateIcon = emptyStateIcon,
            errorStateTitleTextColor = errorStateTitleTextColor,
            errorStateTitleTextStyle = errorStateTitleTextStyle,
            errorStateSubtitleTextColor = errorStateSubtitleTextColor,
            errorStateSubtitleTextStyle = errorStateSubtitleTextStyle,
            errorStateIcon = errorStateIcon,
            retryButtonTextColor = retryButtonTextColor,
            retryButtonTextStyle = retryButtonTextStyle,
            retryButtonBackgroundColor = retryButtonBackgroundColor,
            retryButtonStrokeColor = retryButtonStrokeColor,
            retryButtonStrokeWidth = retryButtonStrokeWidth,
            retryButtonCornerRadius = retryButtonCornerRadius,
            searchBackgroundColor = searchBackgroundColor,
            searchTextColor = searchTextColor,
            searchTextStyle = searchTextStyle,
            searchPlaceholderColor = searchPlaceholderColor,
            searchPlaceholderTextStyle = searchPlaceholderTextStyle,
            searchStartIcon = searchStartIcon,
            searchStartIconTint = searchStartIconTint,
            searchEndIcon = searchEndIcon,
            searchEndIconTint = searchEndIconTint,
            searchCornerRadius = searchCornerRadius,
            searchStrokeWidth = searchStrokeWidth,
            searchStrokeColor = searchStrokeColor,
            discardSelectionIcon = discardSelectionIcon,
            discardSelectionIconTint = discardSelectionIconTint,
            submitSelectionIcon = submitSelectionIcon,
            submitSelectionIconTint = submitSelectionIconTint,
            selectionCountTextColor = selectionCountTextColor,
            selectionCountTextStyle = selectionCountTextStyle,
            itemStyle = itemStyle,
            popupMenuStyle = popupMenuStyle,
            emptyStateStyle = emptyStateStyle,
            errorStateStyle = errorStateStyle,
            loadingStateStyle = loadingStateStyle,
            selectedGroupsAvatarStyle = selectedGroupsAvatarStyle,
            selectedGroupsItemTextColor = selectedGroupsItemTextColor,
            selectedGroupsItemTextStyle = selectedGroupsItemTextStyle,
            selectedGroupsRemoveIcon = selectedGroupsRemoveIcon,
            selectedGroupsRemoveIconTint = selectedGroupsRemoveIconTint
        )
    }
}
