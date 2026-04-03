package com.cometchat.uikit.compose.presentation.users.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingStateStyle
import com.cometchat.uikit.compose.shared.views.popupmenu.CometChatPopupMenuStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatUsers.
 * Contains all visual styling properties for the users list component.
 */
@Immutable
data class CometChatUsersStyle(
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
    
    // Separator styling
    val separatorColor: Color,
    val separatorHeight: Dp,
    
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

    // Sticky header styling
    val stickyHeaderTextColor: Color,
    val stickyHeaderTextStyle: TextStyle,
    val stickyHeaderBackgroundColor: Color,
    
    // Selection mode styling
    val discardSelectionIcon: Painter?,
    val discardSelectionIconTint: Color,
    val submitSelectionIcon: Painter?,
    val submitSelectionIconTint: Color,
    val selectionCountTextColor: Color,
    val selectionCountTextStyle: TextStyle,
    
    // Item styling
    val itemStyle: CometChatUsersListItemStyle,
    
    // Popup menu styling
    val popupMenuStyle: CometChatPopupMenuStyle,
    
    // Shared state styles
    val emptyStateStyle: CometChatEmptyStateStyle,
    val errorStateStyle: CometChatErrorStateStyle,
    val loadingStateStyle: CometChatLoadingStateStyle
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
            
            // Separator styling - hidden by default
            separatorColor: Color = Color.Transparent,
            separatorHeight: Dp = 0.dp,
            
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
            
            // Sticky header styling
            stickyHeaderTextColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            stickyHeaderTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            stickyHeaderBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            
            // Selection mode styling
            discardSelectionIcon: Painter? = painterResource(R.drawable.cometchat_ic_close),
            discardSelectionIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            submitSelectionIcon: Painter? = painterResource(R.drawable.cometchat_ic_check),
            submitSelectionIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            selectionCountTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            selectionCountTextStyle: TextStyle = CometChatTheme.typography.heading4Medium,
            
            // Item styling
            itemStyle: CometChatUsersListItemStyle = CometChatUsersListItemStyle.default(),
            
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
            )
        ): CometChatUsersStyle = CometChatUsersStyle(
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
            separatorColor = separatorColor,
            separatorHeight = separatorHeight,
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
            stickyHeaderTextColor = stickyHeaderTextColor,
            stickyHeaderTextStyle = stickyHeaderTextStyle,
            stickyHeaderBackgroundColor = stickyHeaderBackgroundColor,
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
            loadingStateStyle = loadingStateStyle
        )
    }
}
