package com.cometchat.uikit.compose.presentation.emojikeyboard.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatEmojiKeyboard component.
 *
 * This immutable data class encapsulates all visual styling properties for the emoji keyboard,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param backgroundColor Background color for the emoji keyboard container
 * @param cornerRadius Corner radius for the emoji keyboard container
 * @param strokeWidth Stroke width for the emoji keyboard container border
 * @param strokeColor Stroke color for the emoji keyboard container border
 * @param separatorColor Color for the separator line between content and tab bar
 * @param categoryTextColor Text color for category name headers
 * @param categoryTextStyle Text style for category name headers
 * @param categoryIconTint Tint color for unselected category tab icons
 * @param selectedCategoryIconTint Tint color for the selected category tab icon
 * @param selectedCategoryBackgroundColor Background color for the selected category tab (oval shape)
 * @param tabIconSize Size of the category tab icons (width and height)
 * @param tabBackgroundSize Size of the category tab background (oval container)
 */
@Immutable
data class CometChatEmojiKeyboardStyle(
    val backgroundColor: Color,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val strokeColor: Color,
    val separatorColor: Color,
    val categoryTextColor: Color,
    val categoryTextStyle: TextStyle,
    val categoryIconTint: Color,
    val selectedCategoryIconTint: Color,
    val selectedCategoryBackgroundColor: Color,
    val tabIconSize: Dp,
    val tabBackgroundSize: Dp
) {
    companion object {
        /**
         * Creates a default CometChatEmojiKeyboardStyle with values sourced from CometChatTheme.
         *
         * @return A new CometChatEmojiKeyboardStyle instance with theme-based default values
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            cornerRadius: Dp = 0.dp,
            strokeWidth: Dp = 0.dp,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            separatorColor: Color = CometChatTheme.colorScheme.strokeColorDefault,
            categoryTextColor: Color = CometChatTheme.colorScheme.textColorTertiary,
            categoryTextStyle: TextStyle = TextStyle.Default,
            categoryIconTint: Color = CometChatTheme.colorScheme.iconTintSecondary,
            selectedCategoryIconTint: Color = CometChatTheme.colorScheme.iconTintHighlight,
            selectedCategoryBackgroundColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor100,
            tabIconSize: Dp = 16.dp,
            tabBackgroundSize: Dp = 27.dp
        ): CometChatEmojiKeyboardStyle = CometChatEmojiKeyboardStyle(
            backgroundColor = backgroundColor,
            cornerRadius = cornerRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            separatorColor = separatorColor,
            categoryTextColor = categoryTextColor,
            categoryTextStyle = categoryTextStyle,
            categoryIconTint = categoryIconTint,
            selectedCategoryIconTint = selectedCategoryIconTint,
            selectedCategoryBackgroundColor = selectedCategoryBackgroundColor,
            tabIconSize = tabIconSize,
            tabBackgroundSize = tabBackgroundSize
        )
    }
}
