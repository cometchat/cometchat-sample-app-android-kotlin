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
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingStateStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatCallLogs.
 * Contains all visual styling properties for the call logs component.
 */
@Immutable
data class CometChatCallLogsStyle(
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
    
    // Item styling
    val itemStyle: CometChatCallLogsListItemStyle,
    
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
            emptyStateIcon: Painter? = painterResource(R.drawable.cometchat_ic_call_logs_empty_state),
            
            // Error state styling
            errorStateTitleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            errorStateTitleTextStyle: TextStyle = CometChatTheme.typography.heading3Bold,
            errorStateSubtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            errorStateSubtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            errorStateIcon: Painter? = null,
            
            // Item styling
            itemStyle: CometChatCallLogsListItemStyle = CometChatCallLogsListItemStyle.default(),
            
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
        ): CometChatCallLogsStyle = CometChatCallLogsStyle(
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
            itemStyle = itemStyle,
            emptyStateStyle = emptyStateStyle,
            errorStateStyle = errorStateStyle,
            loadingStateStyle = loadingStateStyle
        )
    }
}
