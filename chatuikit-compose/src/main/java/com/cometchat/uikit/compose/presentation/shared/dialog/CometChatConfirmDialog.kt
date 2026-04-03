package com.cometchat.uikit.compose.presentation.shared.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A confirmation dialog component for actions like delete confirmation, logout confirmation, etc.
 * 
 * This composable provides full parity with the Kotlin XML `CometChatConfirmDialog` implementation,
 * matching the visual layout and behavior:
 * - Centered layout with optional icon in circular background
 * - Title and subtitle text
 * - Positive and negative action buttons with progress indicator support
 * - Full styling customization via [CometChatConfirmDialogStyle]
 * 
 * Layout structure (matching XML layout):
 * ```
 * ┌─────────────────────────────────┐
 * │                                 │
 * │         ┌─────────┐             │
 * │         │  Icon   │             │
 * │         └─────────┘             │
 * │                                 │
 * │           Title                 │
 * │          Subtitle               │
 * │                                 │
 * │   ┌─────────┐  ┌─────────┐      │
 * │   │ Cancel  │  │ Confirm │      │
 * │   └─────────┘  └─────────┘      │
 * │                                 │
 * └─────────────────────────────────┘
 * ```
 *
 * @param title The dialog title text
 * @param subtitle The dialog subtitle/message text
 * @param positiveButtonText Text for the positive/confirm action button
 * @param negativeButtonText Text for the negative/cancel button
 * @param icon Optional icon painter to display at the top of the dialog
 * @param style Visual styling configuration via [CometChatConfirmDialogStyle]
 * @param hideTitle Whether to hide the title (default false)
 * @param hideSubtitle Whether to hide the subtitle (default false)
 * @param hideIcon Whether to hide the icon section (default false)
 * @param hidePositiveButton Whether to hide the positive button (default false)
 * @param hideNegativeButton Whether to hide the negative button (default false)
 * @param showPositiveButtonProgress Whether to show progress indicator on positive button
 * @param showNegativeButtonProgress Whether to show progress indicator on negative button
 * @param dismissOnBackPress Whether dialog dismisses on back press (default true)
 * @param dismissOnClickOutside Whether dialog dismisses on outside click (default false, matching Java)
 * @param dialogContentDescription Accessibility description for the dialog
 * @param positiveButtonContentDescription Accessibility description for positive button
 * @param negativeButtonContentDescription Accessibility description for negative button
 * @param onPositiveClick Callback invoked when positive button is clicked
 * @param onNegativeClick Callback invoked when negative button is clicked
 * @param onDismiss Callback invoked when dialog is dismissed
 */
@Composable
fun CometChatConfirmDialog(
    title: String,
    subtitle: String,
    positiveButtonText: String,
    negativeButtonText: String,
    icon: Painter? = null,
    style: CometChatConfirmDialogStyle = CometChatConfirmDialogStyle.default(),
    hideTitle: Boolean = false,
    hideSubtitle: Boolean = false,
    hideIcon: Boolean = false,
    hidePositiveButton: Boolean = false,
    hideNegativeButton: Boolean = false,
    showPositiveButtonProgress: Boolean = false,
    showNegativeButtonProgress: Boolean = false,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = false,
    dialogContentDescription: String = title,
    positiveButtonContentDescription: String = positiveButtonText,
    negativeButtonContentDescription: String = negativeButtonText,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    onDismiss: () -> Unit = onNegativeClick
) {
    // Use icon from style if not provided directly
    val displayIcon = icon ?: style.icon
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .semantics { contentDescription = dialogContentDescription },
            shape = RoundedCornerShape(style.cornerRadius),
            colors = CardDefaults.cardColors(containerColor = style.backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = style.elevation),
            border = if (style.strokeWidth > 0.dp) {
                BorderStroke(style.strokeWidth, style.strokeColor)
            } else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon section with circular background
                if (!hideIcon && displayIcon != null) {
                    Surface(
                        modifier = Modifier.size(style.iconBackgroundSize),
                        shape = CircleShape,
                        color = style.iconBackgroundColor
                    ) {
                        Box(
                            modifier = Modifier.size(style.iconBackgroundSize),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = displayIcon,
                                contentDescription = null,
                                modifier = Modifier.size(style.iconSize),
                                colorFilter = if (style.iconTint != Color.Unspecified) {
                                    ColorFilter.tint(style.iconTint)
                                } else null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Title section
                if (!hideTitle && title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = style.titleTextStyle,
                        color = style.titleTextColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Subtitle section
                if (!hideSubtitle && subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = style.subtitleTextStyle,
                        color = style.subtitleTextColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Buttons section
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Negative button (Cancel)
                    if (!hideNegativeButton) {
                        ConfirmDialogButton(
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = negativeButtonContentDescription },
                            text = negativeButtonText,
                            textColor = style.negativeButtonTextColor,
                            textStyle = style.negativeButtonTextStyle,
                            backgroundColor = style.negativeButtonBackgroundColor,
                            strokeColor = style.negativeButtonStrokeColor,
                            strokeWidth = style.negativeButtonStrokeWidth,
                            cornerRadius = style.negativeButtonCornerRadius,
                            showProgress = showNegativeButtonProgress,
                            onClick = onNegativeClick
                        )
                    }
                    
                    // Positive button (Confirm/Delete)
                    if (!hidePositiveButton) {
                        ConfirmDialogButton(
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = positiveButtonContentDescription },
                            text = positiveButtonText,
                            textColor = style.positiveButtonTextColor,
                            textStyle = style.positiveButtonTextStyle,
                            backgroundColor = style.positiveButtonBackgroundColor,
                            strokeColor = style.positiveButtonStrokeColor,
                            strokeWidth = style.positiveButtonStrokeWidth,
                            cornerRadius = style.positiveButtonCornerRadius,
                            showProgress = showPositiveButtonProgress,
                            onClick = onPositiveClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * Internal composable for confirm dialog buttons with full styling support.
 * Matches the button layout from the XML implementation.
 */
@Composable
private fun ConfirmDialogButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    textStyle: TextStyle,
    backgroundColor: Color,
    strokeColor: Color,
    strokeWidth: Dp,
    cornerRadius: Dp,
    showProgress: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (strokeWidth > 0.dp) {
            BorderStroke(strokeWidth, strokeColor)
        } else null,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = textColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = textStyle,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
