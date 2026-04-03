package com.cometchat.uikit.compose.shared.views.popupmenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for CometChatPopupMenu component.
 *
 * This immutable data class encapsulates all visual styling properties for the popup menu,
 * following Kotlin standards and integrating with the CometChatTheme system.
 *
 * @param elevation Shadow elevation for the popup menu container
 * @param cornerRadius Corner radius for the popup menu container
 * @param backgroundColor Background color for the popup menu container
 * @param strokeColor Border/stroke color for the popup menu container
 * @param strokeWidth Border/stroke width for the popup menu container
 * @param itemTextColor Default text color for menu items
 * @param itemTextStyle Default text style for menu items
 * @param startIconTint Default tint color for start icons in menu items
 * @param endIconTint Default tint color for end icons in menu items
 * @param itemPaddingHorizontal Horizontal padding for each menu item
 * @param itemPaddingVertical Vertical padding for each menu item
 * @param minWidth Minimum width for the popup menu
 */
@Immutable
data class CometChatPopupMenuStyle(
    val elevation: Dp,
    val cornerRadius: Dp,
    val backgroundColor: Color,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val itemTextColor: Color,
    val itemTextStyle: TextStyle,
    val startIconTint: Color,
    val endIconTint: Color,
    val itemPaddingHorizontal: Dp,
    val itemPaddingVertical: Dp,
    val minWidth: Dp
) {
    companion object {
        /**
         * Creates a default CometChatPopupMenuStyle with values sourced from CometChatTheme.
         *
         * @param elevation Shadow elevation for the popup menu. Default is 8.dp (matching Kotlin)
         * @param cornerRadius Corner radius for the popup menu. Default is 16.dp (matching Kotlin cometchat_radius_4)
         * @param backgroundColor Background color sourced from CometChatTheme.colorScheme.backgroundColor1
         * @param strokeColor Stroke color sourced from CometChatTheme.colorScheme.strokeColorLight
         * @param strokeWidth Border width. Default is 1.dp
         * @param itemTextColor Text color sourced from CometChatTheme.colorScheme.textColorPrimary
         * @param itemTextStyle Text style sourced from CometChatTheme.typography.heading4Regular (matching Kotlin)
         * @param startIconTint Start icon tint sourced from CometChatTheme.colorScheme.iconTintPrimary
         * @param endIconTint End icon tint sourced from CometChatTheme.colorScheme.iconTintPrimary
         * @param itemPaddingHorizontal Horizontal padding for items. Default is 16.dp
         * @param itemPaddingVertical Vertical padding for items. Default is 8.dp
         * @param minWidth Minimum width for the popup. Default is 128.dp
         * @return A new CometChatPopupMenuStyle instance with the specified or default values
         */
        @Composable
        fun default(
            elevation: Dp = 8.dp,
            cornerRadius: Dp = 16.dp,
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            strokeColor: Color = CometChatTheme.colorScheme.strokeColorLight,
            strokeWidth: Dp = 1.dp,
            itemTextColor: Color = CometChatTheme.colorScheme.textColorPrimary,
            itemTextStyle: TextStyle = CometChatTheme.typography.heading4Regular,
            startIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            endIconTint: Color = CometChatTheme.colorScheme.iconTintPrimary,
            itemPaddingHorizontal: Dp = 16.dp,
            itemPaddingVertical: Dp = 8.dp,
            minWidth: Dp = 128.dp
        ): CometChatPopupMenuStyle = CometChatPopupMenuStyle(
            elevation = elevation,
            cornerRadius = cornerRadius,
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            itemTextColor = itemTextColor,
            itemTextStyle = itemTextStyle,
            startIconTint = startIconTint,
            endIconTint = endIconTint,
            itemPaddingHorizontal = itemPaddingHorizontal,
            itemPaddingVertical = itemPaddingVertical,
            minWidth = minWidth
        )
    }
}
