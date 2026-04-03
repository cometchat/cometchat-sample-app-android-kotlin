package com.cometchat.uikit.compose.presentation.shared.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatDialog.
 * Contains all visual styling properties for the dialog component.
 * Aligned with Java CometChatConfirmDialog implementation.
 */
@Immutable
data class CometChatDialogStyle(
    // Container styling
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val elevation: Dp,
    
    // Icon styling
    val iconBackgroundColor: Color,
    val iconTint: Color,
    val iconSize: Dp,
    val iconBackgroundSize: Dp,
    
    // Title styling
    val titleTextColor: Color,
    val titleTextStyle: TextStyle,
    
    // Message styling
    val messageTextColor: Color,
    val messageTextStyle: TextStyle,
    
    // Positive button styling (renamed from confirm)
    val positiveButtonTextColor: Color,
    val positiveButtonTextStyle: TextStyle,
    val positiveButtonBackgroundColor: Color,
    val positiveButtonStrokeColor: Color,
    val positiveButtonStrokeWidth: Dp,
    val positiveButtonCornerRadius: Dp,
    
    // Negative button styling (renamed from cancel)
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
         * Aligned with Java CometChatConfirmDialog default values.
         */
        @Composable
        fun default(
            // Container
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cornerRadius: Dp = 16.dp,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            strokeWidth: Dp = 1.dp,
            elevation: Dp = 6.dp,
            
            // Icon
            iconBackgroundColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            iconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            iconSize: Dp = 48.dp,
            iconBackgroundSize: Dp = 80.dp,
            
            // Title
            titleTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            titleTextStyle: TextStyle = CometChatTheme.typography.heading3Medium,
            
            // Message
            messageTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            messageTextStyle: TextStyle = CometChatTheme.typography.bodyRegular,
            
            // Positive button (matches Java: white text on error background)
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
        ): CometChatDialogStyle = CometChatDialogStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            elevation = elevation,
            iconBackgroundColor = iconBackgroundColor,
            iconTint = iconTint,
            iconSize = iconSize,
            iconBackgroundSize = iconBackgroundSize,
            titleTextColor = titleTextColor,
            titleTextStyle = titleTextStyle,
            messageTextColor = messageTextColor,
            messageTextStyle = messageTextStyle,
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
    }
}
