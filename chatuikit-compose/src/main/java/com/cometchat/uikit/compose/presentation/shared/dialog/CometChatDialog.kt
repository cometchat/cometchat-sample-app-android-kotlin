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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A reusable dialog component for confirmations, alerts, and other modal interactions.
 * Aligned with Java CometChatConfirmDialog implementation.
 * 
 * Provides customizable icon, title, message, and action buttons with full styling support
 * including backgrounds, borders, and progress indicators.
 *
 * @param title The dialog title text
 * @param message The dialog message/subtitle text
 * @param positiveButtonText Text for the positive/confirm action button
 * @param negativeButtonText Text for the negative/cancel button (default empty)
 * @param icon Optional icon to display at the top of the dialog
 * @param style Visual styling configuration
 * @param hideTitle Whether to hide the title (default false)
 * @param hideMessage Whether to hide the message (default false)
 * @param hideIcon Whether to hide the icon (default false)
 * @param showIconBackground Whether to show icon background container (default true)
 * @param hidePositiveButton Whether to hide the positive button (default false)
 * @param hideNegativeButton Whether to hide the negative button (default false)
 * @param showPositiveButtonProgress Whether to show progress on positive button (default false)
 * @param showNegativeButtonProgress Whether to show progress on negative button (default false)
 * @param dialogContentDescription Accessibility description for the dialog
 * @param positiveButtonContentDescription Accessibility description for positive button
 * @param negativeButtonContentDescription Accessibility description for negative button
 * @param onPositiveClick Callback invoked when positive button is clicked
 * @param onNegativeClick Callback invoked when negative button is clicked
 * @param onDismiss Callback invoked when dialog is dismissed
 */
@Composable
fun CometChatDialog(
    title: String,
    message: String,
    positiveButtonText: String,
    negativeButtonText: String = "",
    icon: Painter? = null,
    style: CometChatDialogStyle = CometChatDialogStyle.default(),
    hideTitle: Boolean = false,
    hideMessage: Boolean = false,
    hideIcon: Boolean = false,
    showIconBackground: Boolean = true,
    hidePositiveButton: Boolean = false,
    hideNegativeButton: Boolean = false,
    showPositiveButtonProgress: Boolean = false,
    showNegativeButtonProgress: Boolean = false,
    dialogContentDescription: String = title,
    positiveButtonContentDescription: String = positiveButtonText,
    negativeButtonContentDescription: String = negativeButtonText,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    onDismiss: () -> Unit = onNegativeClick
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
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
                // Icon section
                if (!hideIcon && icon != null) {
                    if (showIconBackground) {
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
                                    painter = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(style.iconSize),
                                    colorFilter = ColorFilter.tint(style.iconTint)
                                )
                            }
                        }
                    } else {
                        Image(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier.size(style.iconSize),
                            colorFilter = ColorFilter.tint(style.iconTint)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Title section
                if (!hideTitle) {
                    Text(
                        text = title,
                        style = style.titleTextStyle,
                        color = style.titleTextColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Message section
                if (!hideMessage) {
                    Text(
                        text = message,
                        style = style.messageTextStyle,
                        color = style.messageTextColor,
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
                    // Negative button
                    if (!hideNegativeButton) {
                        DialogButton(
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
                    
                    // Positive button
                    if (!hidePositiveButton) {
                        DialogButton(
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
 * Internal composable for dialog buttons with full styling support.
 */
@Composable
private fun DialogButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    textStyle: androidx.compose.ui.text.TextStyle,
    backgroundColor: Color,
    strokeColor: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp,
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

// Backward compatibility overload
/**
 * Backward compatible overload for existing code using old parameter names.
 * @deprecated Use the new parameter names (positiveButtonText, negativeButtonText, onPositiveClick, onNegativeClick)
 */
@Deprecated(
    message = "Use the new parameter names",
    replaceWith = ReplaceWith(
        "CometChatDialog(title, message, positiveButtonText = confirmButtonText, negativeButtonText = cancelButtonText, style = style, hideNegativeButton = !showCancelButton, onPositiveClick = onConfirm, onNegativeClick = onDismiss, onDismiss = onDismiss)"
    )
)
@Composable
fun CometChatDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    cancelButtonText: String = "",
    style: CometChatDialogStyle = CometChatDialogStyle.default(),
    showCancelButton: Boolean = true,
    dialogContentDescription: String = title,
    confirmButtonContentDescription: String = confirmButtonText,
    cancelButtonContentDescription: String = cancelButtonText,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    CometChatDialog(
        title = title,
        message = message,
        positiveButtonText = confirmButtonText,
        negativeButtonText = cancelButtonText,
        icon = null,
        style = style,
        hideTitle = false,
        hideMessage = false,
        hideIcon = false,
        showIconBackground = true,
        hidePositiveButton = false,
        hideNegativeButton = !showCancelButton,
        showPositiveButtonProgress = false,
        showNegativeButtonProgress = false,
        dialogContentDescription = dialogContentDescription,
        positiveButtonContentDescription = confirmButtonContentDescription,
        negativeButtonContentDescription = cancelButtonContentDescription,
        onPositiveClick = onConfirm,
        onNegativeClick = onDismiss,
        onDismiss = onDismiss
    )
}
