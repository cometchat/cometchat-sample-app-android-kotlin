package com.cometchat.uikit.compose.presentation.shared.toolbar

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
 * Style configuration for CometChatToolbar.
 * Contains all visual styling properties for the toolbar component.
 */
@Immutable
data class CometChatToolbarStyle(
    // Background
    val backgroundColor: Color,
    
    // Title styling
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    
    // Navigation icon styling
    val navigationIcon: Painter?,
    val navigationIconTint: Color,
    
    // Elevation
    val elevation: Dp,
    
    // Height
    val height: Dp,
    
    // Separator styling
    val separatorColor: Color,
    val separatorHeight: Dp,
    val showSeparator: Boolean,
    
    // Selection mode - Discard icon styling
    val discardIcon: Painter?,
    val discardIconTint: Color,
    
    // Selection mode - Submit icon styling
    val submitIcon: Painter?,
    val submitIconTint: Color,
    
    // Selection mode - Selection count text styling
    val selectionCountTextColor: Color,
    val selectionCountTextStyle: TextStyle
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading2Bold,
            navigationIcon: Painter? = painterResource(R.drawable.cometchat_ic_back),
            navigationIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            elevation: Dp = 0.dp,
            height: Dp = 56.dp,
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            separatorHeight: Dp = 1.dp,
            showSeparator: Boolean = true,
            discardIcon: Painter? = painterResource(R.drawable.cometchat_ic_close),
            discardIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            submitIcon: Painter? = painterResource(R.drawable.cometchat_ic_check),
            submitIconTint: Color = CometChatTheme.colorScheme.primary,
            selectionCountTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            selectionCountTextStyle: TextStyle = CometChatTheme.typography.heading2Bold
        ): CometChatToolbarStyle = CometChatToolbarStyle(
            backgroundColor = backgroundColor,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            navigationIcon = navigationIcon,
            navigationIconTint = navigationIconTint,
            elevation = elevation,
            height = height,
            separatorColor = separatorColor,
            separatorHeight = separatorHeight,
            showSeparator = showSeparator,
            discardIcon = discardIcon,
            discardIconTint = discardIconTint,
            submitIcon = submitIcon,
            submitIconTint = submitIconTint,
            selectionCountTextColor = selectionCountTextColor,
            selectionCountTextStyle = selectionCountTextStyle
        )
    }
}
