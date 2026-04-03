package com.cometchat.uikit.compose.shared.views.popupmenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Internal composable that renders a single menu item row.
 *
 * This composable displays a menu item with optional start and end icons,
 * applying styles from CometChatPopupMenuStyle with MenuItem overrides taking precedence.
 *
 * @param item The MenuItem data to render
 * @param style The CometChatPopupMenuStyle providing default styling
 * @param onClick Callback invoked when the menu item is clicked
 */
@Composable
internal fun PopupMenuItem(
    item: MenuItem,
    style: CometChatPopupMenuStyle,
    onClick: () -> Unit
) {
    // Resolve colors with MenuItem overrides taking precedence over global style
    val textColor = item.textColor ?: style.itemTextColor
    val textStyle = item.textStyle ?: style.itemTextStyle
    val startIconTint = item.startIconTint ?: style.startIconTint
    val endIconTint = item.endIconTint ?: style.endIconTint

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(
                horizontal = style.itemPaddingHorizontal,
                vertical = style.itemPaddingVertical
            )
            .semantics {
                contentDescription = item.name
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Start icon - only rendered if present
        if (item.startIcon != null) {
            Icon(
                painter = item.startIcon,
                contentDescription = null,
                tint = startIconTint,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("popup_menu_item_start_icon_${item.id}")
            )
            // 8dp margin between icon and text (matching View-based cometchat_margin_2)
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Menu item text
        Text(
            text = item.name,
            color = textColor,
            style = textStyle,
            modifier = Modifier.weight(1f)
        )

        // End icon - only rendered if present
        if (item.endIcon != null) {
            // 8dp margin between text and icon (matching View-based cometchat_margin_2)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = item.endIcon,
                contentDescription = null,
                tint = endIconTint,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("popup_menu_item_end_icon_${item.id}")
            )
        }
    }
}
