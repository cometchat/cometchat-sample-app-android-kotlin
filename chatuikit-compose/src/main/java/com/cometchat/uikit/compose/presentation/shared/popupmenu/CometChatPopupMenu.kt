package com.cometchat.uikit.compose.presentation.shared.popupmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.cometchat.uikit.compose.shared.views.popupmenu.CometChatPopupMenuStyle
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.shared.views.popupmenu.PopupMenuItem

/**
 * Enum defining the position of the popup menu relative to the anchor view.
 */
enum class PopupPosition {
    /** Show popup above the anchor view */
    ABOVE,
    /** Show popup below the anchor view */
    BELOW,
    /** Automatically determine position based on available screen space (default behavior) */
    AUTO
}

/**
 * Custom PopupPositionProvider that positions the popup above the anchor.
 * This bypasses DropdownMenu's internal positioning logic that clamps negative offsets.
 */
private class AboveAnchorPositionProvider(
    private val anchorBoundsInWindow: IntRect,
    private val offsetX: Dp,
    private val offsetY: Dp,
    private val density: Float
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val offsetXPx = (offsetX.value * density).toInt()
        val offsetYPx = (offsetY.value * density).toInt()
        
        // Position popup above the anchor
        // Y: anchor top - popup height + any user offset
        val y = anchorBoundsInWindow.top - popupContentSize.height + offsetYPx
        
        // X: align with anchor's left edge + any user offset
        var x = anchorBoundsInWindow.left + offsetXPx
        // Ensure popup doesn't go off screen on the right
        if (x + popupContentSize.width > windowSize.width) {
            x = windowSize.width - popupContentSize.width - (12 * density).toInt()
        }
        // Ensure popup doesn't go off screen on the left
        if (x < 0) {
            x = (12 * density).toInt()
        }
        
        // Ensure popup doesn't go off screen on top
        val finalY = maxOf(0, y)
        
        return IntOffset(x, finalY)
    }
}

/**
 * CometChatPopupMenu is a composable that displays a popup menu with customizable menu items.
 *
 * The popup menu is anchored to the content composable and displays a list of selectable
 * menu items with optional icons. It integrates with CometChatTheme for consistent styling.
 *
 * @param expanded Controls visibility of the popup menu. When true, the menu is displayed.
 * @param onDismissRequest Callback invoked when the menu should be dismissed (e.g., tap outside)
 * @param menuItems List of MenuItem objects to display in the popup menu
 * @param modifier Modifier for the anchor content
 * @param style CometChatPopupMenuStyle for customizing the popup menu appearance
 * @param position Position of the popup relative to the anchor (ABOVE, BELOW, or AUTO)
 * @param onMenuItemClick Global callback invoked when any menu item is clicked, with id and name
 * @param offset Offset from the anchor position for the popup menu
 * @param content The anchor composable content that triggers the popup menu
 *
 * Example usage:
 * ```
 * var expanded by remember { mutableStateOf(false) }
 *
 * CometChatPopupMenu(
 *     expanded = expanded,
 *     onDismissRequest = { expanded = false },
 *     menuItems = listOf(
 *         MenuItem.simple("edit", "Edit"),
 *         MenuItem.simple("delete", "Delete")
 *     ),
 *     position = PopupPosition.ABOVE,
 *     onMenuItemClick = { id, name -> println("Clicked: $name") }
 * ) {
 *     IconButton(onClick = { expanded = true }) {
 *         Icon(Icons.Default.MoreVert, contentDescription = "Menu")
 *     }
 * }
 * ```
 */
@Composable
fun CometChatPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier,
    style: CometChatPopupMenuStyle = CometChatPopupMenuStyle.default(),
    position: PopupPosition = PopupPosition.AUTO,
    onMenuItemClick: ((id: String, name: String) -> Unit)? = null,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(style.cornerRadius)
    val density = LocalDensity.current

    // Track anchor bounds in window coordinates for ABOVE positioning
    var anchorBoundsInWindow by remember { mutableStateOf(IntRect.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            anchorBoundsInWindow = IntRect(
                left = bounds.left.toInt(),
                top = bounds.top.toInt(),
                right = bounds.right.toInt(),
                bottom = bounds.bottom.toInt()
            )
        }
    ) {
        content()

        when (position) {
            PopupPosition.ABOVE -> {
                // Use custom Popup with AboveAnchorPositionProvider for accurate ABOVE positioning
                // DropdownMenu's internal positioning clamps negative offsets, so we bypass it
                if (expanded) {
                    Popup(
                        popupPositionProvider = AboveAnchorPositionProvider(
                            anchorBoundsInWindow = anchorBoundsInWindow,
                            offsetX = offset.x,
                            offsetY = offset.y,
                            density = density.density
                        ),
                        onDismissRequest = onDismissRequest,
                        properties = PopupProperties(
                            focusable = false, // Non-focusable to prevent keyboard dismissal when popup is shown
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true
                        )
                    ) {
                        // Use Surface for Material 3 elevation/shadow effect
                        Surface(
                            modifier = Modifier
                                .testTag("cometchat_popup_menu")
                                .widthIn(min = style.minWidth, max = style.minWidth + 80.dp)
                                .border(
                                    width = style.strokeWidth,
                                    color = style.strokeColor,
                                    shape = shape
                                ),
                            shape = shape,
                            color = style.backgroundColor,
                            shadowElevation = style.elevation,
                            tonalElevation = 0.dp
                        ) {
                            // Add 8dp vertical padding to match DropdownMenu's internal padding
                            Column(
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                                    .padding(vertical = 8.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                menuItems.forEach { item ->
                                    PopupMenuItem(
                                        item = item,
                                        style = style,
                                        onClick = {
                                            item.onClick?.invoke()
                                            onMenuItemClick?.invoke(item.id, item.name)
                                            onDismissRequest()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            PopupPosition.BELOW, PopupPosition.AUTO -> {
                // Use DropdownMenu for BELOW and AUTO (works correctly with nice UX)
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = onDismissRequest,
                    offset = offset,
                    modifier = Modifier
                        .testTag("cometchat_popup_menu")
                        .shadow(
                            elevation = style.elevation,
                            shape = shape,
                            clip = false
                        )
                        .clip(shape)
                        .border(
                            width = style.strokeWidth,
                            color = style.strokeColor,
                            shape = shape
                        )
                        .background(style.backgroundColor, shape)
                        .widthIn(min = style.minWidth, max = style.minWidth + 80.dp),
                    shape = shape,
                    containerColor = Color.Transparent,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    scrollState = rememberScrollState()
                ) {
                    menuItems.forEach { item ->
                        PopupMenuItem(
                            item = item,
                            style = style,
                            onClick = {
                                item.onClick?.invoke()
                                onMenuItemClick?.invoke(item.id, item.name)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}
