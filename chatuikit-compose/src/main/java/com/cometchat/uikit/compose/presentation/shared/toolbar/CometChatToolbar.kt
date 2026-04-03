package com.cometchat.uikit.compose.presentation.shared.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R

/**
 * CometChatToolbar displays a customizable toolbar with title, navigation icon,
 * and optional action buttons. Supports selection mode with count display.
 *
 * This is a standalone reusable component that can be used across the UIKit.
 *
 * @param title The title text to display
 * @param modifier Modifier for the toolbar container
 * @param style Style configuration for the toolbar
 * @param hideBackIcon Whether to hide the back navigation icon
 * @param navigationIcon Optional custom navigation icon
 * @param navigationContentDescription Content description for the navigation icon (for accessibility)
 * @param onNavigationClick Callback when navigation icon is clicked
 * @param selectionMode Whether selection mode is enabled
 * @param selectionCount Number of selected items (used when selectionMode is true)
 * @param onDiscardSelection Callback when discard selection icon is clicked
 * @param onSubmitSelection Callback when submit selection icon is clicked
 * @param actions Optional composable for action buttons (used when selectionMode is false)
 */
@Composable
fun CometChatToolbar(
    title: String,
    modifier: Modifier = Modifier,
    style: CometChatToolbarStyle = CometChatToolbarStyle.default(),
    hideBackIcon: Boolean = false,
    navigationIcon: Painter? = null,
    navigationContentDescription: String = "Navigate back",
    onNavigationClick: (() -> Unit)? = null,
    selectionMode: Boolean = false,
    selectionCount: Int = 0,
    onDiscardSelection: (() -> Unit)? = null,
    onSubmitSelection: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    val icon = navigationIcon ?: style.navigationIcon
    val context = LocalContext.current
    
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(style.height),
            shadowElevation = style.elevation,
            color = style.backgroundColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left section: Navigation/Discard icon + Title/Selection count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectionMode) {
                        // Selection mode: Show discard icon
                        if (style.discardIcon != null) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .focusable()
                                    .clickable(
                                        enabled = onDiscardSelection != null,
                                        onClick = { onDiscardSelection?.invoke() }
                                    )
                                    .semantics {
                                        contentDescription = context.getString(R.string.cometchat_discard_selection)
                                        role = Role.Button
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = style.discardIcon,
                                    contentDescription = null,
                                    tint = style.discardIconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Selection count text - show just the count number (matches Java implementation)
                        Text(
                            text = selectionCount.toString(),
                            style = style.selectionCountTextStyle,
                            color = style.selectionCountTextColor,
                            modifier = Modifier
                                .padding(start = if (style.discardIcon == null) 12.dp else 0.dp)
                                .semantics { heading() }
                        )
                    } else {
                        // Normal mode: Show navigation icon
                        if (!hideBackIcon && icon != null) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .focusable()
                                    .clickable(
                                        enabled = onNavigationClick != null,
                                        onClick = { onNavigationClick?.invoke() }
                                    )
                                    .semantics { 
                                        contentDescription = navigationContentDescription
                                        role = Role.Button
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = icon,
                                    contentDescription = null,
                                    tint = style.navigationIconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Title - marked as heading for accessibility
                        Text(
                            text = title,
                            style = style.titleTextStyle,
                            color = style.titleTextColor,
                            modifier = Modifier
                                .padding(
                                    start = if (hideBackIcon || icon == null) 12.dp else 0.dp
                                )
                                .semantics { heading() }
                        )
                    }
                }
                
                // Right section: Actions or Submit icon
                if (selectionMode) {
                    // Selection mode: Show submit icon
                    if (style.submitIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .focusable()
                                .clickable(
                                    enabled = onSubmitSelection != null,
                                    onClick = { onSubmitSelection?.invoke() }
                                )
                                .semantics {
                                    contentDescription = context.getString(R.string.cometchat_submit_selection)
                                    role = Role.Button
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = style.submitIcon,
                                contentDescription = null,
                                tint = style.submitIconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else if (actions != null) {
                    // Normal mode: Show custom actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
            }
        }
        
        // Separator at bottom of toolbar
        if (style.showSeparator) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(style.separatorHeight)
                    .background(style.separatorColor)
            )
        }
    }
}
