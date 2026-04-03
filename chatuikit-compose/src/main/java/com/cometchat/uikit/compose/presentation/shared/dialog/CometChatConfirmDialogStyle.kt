package com.cometchat.uikit.compose.presentation.shared.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatConfirmDialog.
 * 
 * This style class provides full parity with the XML attributes defined in
 * `attr_cometchat_confirm_dialog.xml` from chatuikit-kotlin.
 * 
 * Contains all visual styling properties for the confirmation dialog component
 * including container, icon, title, subtitle, and button styling.
 */
@Immutable
data class CometChatConfirmDialogStyle(
    // Container styling
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val elevation: Dp,
    
    // Icon styling
    val icon: Painter?,
    val iconTint: Color,
    val iconBackgroundColor: Color,
    val iconSize: Dp,
    val iconBackgroundSize: Dp,
    
    // Title styling
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    
    // Subtitle styling
    val subtitleTextColor: Color,
    val subtitleTextStyle: TextStyle,
    
    // Positive button styling
    val positiveButtonTextColor: Color,
    val positiveButtonTextStyle: TextStyle,
    val positiveButtonBackgroundColor: Color,
    val positiveButtonStrokeColor: Color,
    val positiveButtonStrokeWidth: Dp,
    val positiveButtonCornerRadius: Dp,
    
    // Negative button styling
    val negativeButtonTextColor: Color,
    val negativeButtonTextStyle: TextStyle,
    val negativeButtonBackgroundColor: Color,
    val negativeButtonStrokeColor: Color,
    val negativeButtonStrokeWidth: Dp,
    val negativeButtonCornerRadius: Dp
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * 
         * Default values match the Java CometChatConfirmDialog implementation:
         * - Container: backgroundColor1 with strokeColorLight border
         * - Icon: backgroundColor2 circular background
         * - Title: textColorPrimary
         * - Subtitle: textColorSecondary
         * - Positive button: white text on errorColor background (for destructive actions)
         * - Negative button: primary text on white background with dark stroke
         */
        @Composable
        fun default(
            // Container
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cornerRadius: Dp = 16.dp,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            strokeWidth: Dp = 0.dp,
            elevation: Dp = 0.dp,
            
            // Icon
            icon: Painter? = null,
            iconTint: Color = CometChatTheme.colorScheme.errorColor,
            iconBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            iconSize: Dp = 48.dp,
            iconBackgroundSize: Dp = 80.dp,
            
            // Title
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading3Medium,
            
            // Subtitle
            subtitleTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            subtitleTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // Positive button (matches Java: white text on error background for delete actions)
            positiveButtonTextColor: Color = CometChatTheme.colorScheme.colorWhite,
            positiveButtonTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            positiveButtonBackgroundColor: Color = CometChatTheme.colorScheme.errorColor,
            positiveButtonStrokeColor: Color = Color.Transparent,
            positiveButtonStrokeWidth: Dp = 0.dp,
            positiveButtonCornerRadius: Dp = 8.dp,
            
            // Negative button (matches Java: primary text on textColorWhite with dark stroke)
            negativeButtonTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            negativeButtonTextStyle: TextStyle = CometChatTheme.typography.bodyMedium,
            negativeButtonBackgroundColor: Color = CometChatTheme.colorScheme.textColorWhite,
            negativeButtonStrokeColor: Color = CometChatTheme.colorScheme.strokeColorDark,
            negativeButtonStrokeWidth: Dp = 1.dp,
            negativeButtonCornerRadius: Dp = 8.dp
        ): CometChatConfirmDialogStyle = CometChatConfirmDialogStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            elevation = elevation,
            icon = icon,
            iconTint = iconTint,
            iconBackgroundColor = iconBackgroundColor,
            iconSize = iconSize,
            iconBackgroundSize = iconBackgroundSize,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            subtitleTextColor = subtitleTextColor,
            subtitleTextStyle = subtitleTextStyle,
            positiveButtonTextColor = positiveButtonTextColor,
            positiveButtonTextStyle = positiveButtonTextStyle,
            positiveButtonBackgroundColor = positiveButtonBackgroundColor,
            positiveButtonStrokeColor = positiveButtonStrokeColor,
            positiveButtonStrokeWidth = positiveButtonStrokeWidth,
            positiveButtonCornerRadius = positiveButtonCornerRadius,
            negativeButtonTextColor = negativeButtonTextColor,
            negativeButtonTextStyle = negativeButtonTextStyle,
            negativeButtonBackgroundColor = negativeButtonBackgroundColor,
            negativeButtonStrokeColor = negativeButtonStrokeColor,
            negativeButtonStrokeWidth = negativeButtonStrokeWidth,
            negativeButtonCornerRadius = negativeButtonCornerRadius
        )
        
        /**
         * Creates a style configuration for delete confirmation dialogs.
         * 
         * Uses error color for the positive button to indicate destructive action.
         */
        @Composable
        fun deleteConfirmation(
            icon: Painter? = null
        ): CometChatConfirmDialogStyle = default(
            icon = icon,
            iconTint = CometChatTheme.colorScheme.errorColor,
            positiveButtonBackgroundColor = CometChatTheme.colorScheme.errorColor,
            positiveButtonTextColor = CometChatTheme.colorScheme.colorWhite
        )
    }
}
