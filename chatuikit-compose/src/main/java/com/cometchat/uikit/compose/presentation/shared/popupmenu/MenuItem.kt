package com.cometchat.uikit.compose.shared.views.popupmenu

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle

/**
 * Data class representing a single item in the CometChatPopupMenu.
 *
 * @param id Unique identifier for the menu item
 * @param name Display text for the menu item
 * @param startIcon Optional icon displayed at the start of the menu item row (use painterResource or rememberVectorPainter)
 * @param endIcon Optional icon displayed at the end of the menu item row (use painterResource or rememberVectorPainter)
 * @param startIconTint Optional tint color for the start icon. If null, uses global style
 * @param endIconTint Optional tint color for the end icon. If null, uses global style
 * @param textColor Optional text color for the menu item. If null, uses global style
 * @param textStyle Optional text style for the menu item. If null, uses global style
 * @param onClick Optional callback invoked when the menu item is clicked
 */
data class MenuItem(
    val id: String,
    val name: String,
    val startIcon: Painter? = null,
    val endIcon: Painter? = null,
    val startIconTint: Color? = null,
    val endIconTint: Color? = null,
    val textColor: Color? = null,
    val textStyle: TextStyle? = null,
    val onClick: (() -> Unit)? = null
) {
    companion object {
        /**
         * Creates a simple MenuItem with just id, name, and optional click handler.
         *
         * @param id Unique identifier for the menu item
         * @param name Display text for the menu item
         * @param onClick Optional callback invoked when the menu item is clicked
         * @return A new MenuItem instance with minimal configuration
         */
        fun simple(
            id: String,
            name: String,
            onClick: (() -> Unit)? = null
        ): MenuItem = MenuItem(
            id = id,
            name = name,
            onClick = onClick
        )

        /**
         * Creates a MenuItem with icons.
         *
         * @param id Unique identifier for the menu item
         * @param name Display text for the menu item
         * @param startIcon Optional icon displayed at the start of the menu item row (use painterResource or rememberVectorPainter)
         * @param endIcon Optional icon displayed at the end of the menu item row (use painterResource or rememberVectorPainter)
         * @param onClick Optional callback invoked when the menu item is clicked
         * @return A new MenuItem instance with icon configuration
         */
        fun withIcons(
            id: String,
            name: String,
            startIcon: Painter? = null,
            endIcon: Painter? = null,
            onClick: (() -> Unit)? = null
        ): MenuItem = MenuItem(
            id = id,
            name = name,
            startIcon = startIcon,
            endIcon = endIcon,
            onClick = onClick
        )
    }
}
