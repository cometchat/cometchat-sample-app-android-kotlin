package com.cometchat.uikit.compose.presentation.shared.searchbox

import androidx.compose.foundation.layout.PaddingValues
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
 * Style configuration for CometChatSearchBox.
 * Mirrors the styling capabilities of the View-based CometChatSearchBox.
 */
@Immutable
data class CometChatSearchBoxStyle(
    // Container styling
    val backgroundColor: Color,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val cornerRadius: Dp,
    
    // Text styling
    val textColor: Color,
    val textStyle: TextStyle,
    val placeholderColor: Color,
    val placeholderTextStyle: TextStyle,
    
    // Start icon (search icon)
    val startIcon: Painter?,
    val startIconTint: Color,
    
    // End icon (clear icon)
    val endIcon: Painter?,
    val endIconTint: Color,
    
    // Padding
    val contentPadding: PaddingValues
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            strokeColor: Color = Color.Transparent,
            strokeWidth: Dp = 0.dp,
            cornerRadius: Dp = 1000.dp, // Fully rounded corners (matches cometchat_radius_max)
            textColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            placeholderColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            placeholderTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            startIcon: Painter? = painterResource(R.drawable.cometchat_ic_search),
            startIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            endIcon: Painter? = painterResource(R.drawable.cometchat_ic_close),
            endIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ): CometChatSearchBoxStyle = CometChatSearchBoxStyle(
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            textColor = textColor,
            textStyle = textStyle,
            placeholderColor = placeholderColor,
            placeholderTextStyle = placeholderTextStyle,
            startIcon = startIcon,
            startIconTint = startIconTint,
            endIcon = endIcon,
            endIconTint = endIconTint,
            contentPadding = contentPadding
        )
    }
}
