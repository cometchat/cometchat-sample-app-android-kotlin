package com.cometchat.uikit.compose.presentation.messagelist.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for the message-specific popup menu overlay.
 *
 * This style is used by [CometChatMessagePopupMenu] composable to control
 * the appearance of the option list card shown on long-press of a message bubble.
 *
 * It maps to the XML `CometChatPopupMenu` styleable attributes:
 * - `cometchatPopupMenuBackgroundColor` → [backgroundColor]
 * - `cometchatPopupMenuCornerRadius` → [cornerRadius]
 * - `cometchatPopupMenuElevation` → [elevation]
 * - `cometchatPopupMenuItemTextColor` → [textColor]
 * - `cometchatPopupMenuItemTextAppearance` → [textStyle]
 * - `cometchatPopupMenuStrokeColor` → [strokeColor]
 * - `cometchatPopupMenuStrokeWidth` → [strokeWidth]
 * - `cometchatPopupMenuItemStartIconTint` → [startIconTint]
 * - `cometchatPopupMenuItemEndIconTint` → [endIconTint]
 *
 * @param backgroundColor Background color for the option list card
 * @param cornerRadius Corner radius for the option list card
 * @param elevation Shadow elevation for the option list card
 * @param textColor Default text color for option labels
 * @param textStyle Default text style for option labels
 * @param strokeColor Border/stroke color for the option list card
 * @param strokeWidth Border/stroke width for the option list card
 * @param startIconTint Default tint color for start icons in option rows
 * @param endIconTint Default tint color for end icons in option rows
 */
@Immutable
data class CometChatMessagePopupMenuStyle(
    val backgroundColor: Color = Color.Unspecified,
    val cornerRadius: Dp = 0.dp,
    val elevation: Dp = 0.dp,
    val textColor: Color = Color.Unspecified,
    val textStyle: TextStyle = TextStyle.Default,
    val strokeColor: Color = Color.Unspecified,
    val strokeWidth: Dp = 0.dp,
    val startIconTint: Color = Color.Unspecified,
    val endIconTint: Color = Color.Unspecified
) {
    companion object {
        /**
         * Creates a default [CometChatMessagePopupMenuStyle] using [CometChatTheme] tokens.
         *
         * Default values:
         * - [backgroundColor] → `CometChatTheme.colorScheme.backgroundColor1`
         * - [textColor] → `CometChatTheme.colorScheme.textColorPrimary`
         * - [strokeColor] → `CometChatTheme.colorScheme.strokeColorLight`
         * - [startIconTint] → `CometChatTheme.colorScheme.iconTintSecondary`
         * - [endIconTint] → `CometChatTheme.colorScheme.iconTintSecondary`
         *
         * @return A new [CometChatMessagePopupMenuStyle] instance with theme defaults
         */
        @Composable
        fun default(): CometChatMessagePopupMenuStyle = CometChatMessagePopupMenuStyle(
            backgroundColor = CometChatTheme.colorScheme.backgroundColor1,
            cornerRadius = 16.dp,
            elevation = 8.dp,
            textColor = CometChatTheme.colorScheme.textColorPrimary,
            strokeColor = CometChatTheme.colorScheme.strokeColorLight,
            strokeWidth = 1.dp,
            startIconTint = CometChatTheme.colorScheme.iconTintSecondary,
            endIconTint = CometChatTheme.colorScheme.iconTintSecondary
        )
    }
}
