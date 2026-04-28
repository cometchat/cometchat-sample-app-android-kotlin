package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * A dialog composable for adding/editing a link.
 * Matches v5 CometChatCompactMessageComposer's Add Link dialog layout:
 * - Header with title + close (X) button
 * - "Text" outlined input field
 * - "Link" outlined input field
 * - Cancel (outlined) + Save (filled primary) buttons side by side
 * - In edit mode: title shows "Edit Link", and a "Remove" button is shown
 *
 * @param modifier Modifier for the dialog content
 * @param style Style configuration for the dialog
 * @param initialText Initial text for the link (display text)
 * @param initialUrl Initial URL for the link
 * @param isEditMode When true, shows "Edit Link" title and a Remove button
 * @param onApply Callback when the Save button is clicked with (text, url)
 * @param onRemove Callback when the Remove button is clicked (edit mode only)
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun CometChatLinkEditDialog(
    modifier: Modifier = Modifier,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    initialText: String = "",
    initialUrl: String = "",
    isEditMode: Boolean = false,
    onApply: (text: String, url: String) -> Unit = { _, _ -> },
    onRemove: (() -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    var linkText by remember { mutableStateOf(initialText) }
    var linkUrl by remember { mutableStateOf(initialUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = style.linkDialogBackgroundColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
                .semantics { contentDescription = "Link Edit Dialog" }
        ) {
            // Header: Title + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(
                        if (isEditMode) R.string.cometchat_edit_link else R.string.cometchat_add_link
                    ),
                    color = style.linkDialogTitleTextColor,
                    style = style.linkDialogTitleTextStyle
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cometchat_ic_close),
                        contentDescription = context.getString(R.string.cometchat_close),
                        tint = CometChatTheme.colorScheme.iconTintSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text input field
            OutlinedTextField(
                value = linkText,
                onValueChange = { linkText = it },
                label = {
                    Text(
                        text = context.getString(R.string.cometchat_text),
                        color = CometChatTheme.colorScheme.textColorSecondary,
                        style = style.linkDialogInputTextStyle
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = style.linkDialogInputTextStyle.copy(color = style.linkDialogInputTextColor),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = style.linkDialogBackgroundColor,
                    unfocusedContainerColor = style.linkDialogBackgroundColor,
                    focusedTextColor = style.linkDialogInputTextColor,
                    unfocusedTextColor = style.linkDialogInputTextColor,
                    focusedBorderColor = CometChatTheme.colorScheme.strokeColorDefault,
                    unfocusedBorderColor = CometChatTheme.colorScheme.strokeColorDefault,
                    focusedLabelColor = CometChatTheme.colorScheme.textColorSecondary,
                    unfocusedLabelColor = CometChatTheme.colorScheme.textColorSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Link input field
            OutlinedTextField(
                value = linkUrl,
                onValueChange = { linkUrl = it },
                label = {
                    Text(
                        text = context.getString(R.string.cometchat_link),
                        color = CometChatTheme.colorScheme.textColorSecondary,
                        style = style.linkDialogInputTextStyle
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = style.linkDialogInputTextStyle.copy(color = style.linkDialogInputTextColor),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = style.linkDialogBackgroundColor,
                    unfocusedContainerColor = style.linkDialogBackgroundColor,
                    focusedTextColor = style.linkDialogInputTextColor,
                    unfocusedTextColor = style.linkDialogInputTextColor,
                    focusedBorderColor = CometChatTheme.colorScheme.strokeColorDefault,
                    unfocusedBorderColor = CometChatTheme.colorScheme.strokeColorDefault,
                    focusedLabelColor = CometChatTheme.colorScheme.textColorSecondary,
                    unfocusedLabelColor = CometChatTheme.colorScheme.textColorSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons: Cancel/Remove (outlined) + Save (filled)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditMode && onRemove != null) {
                    // Remove button — outlined, destructive
                    OutlinedButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            CometChatTheme.colorScheme.errorColor
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = style.linkDialogBackgroundColor,
                            contentColor = CometChatTheme.colorScheme.errorColor
                        )
                    ) {
                        Text(
                            text = context.getString(R.string.cometchat_remove),
                            style = style.linkDialogButtonTextStyle,
                            color = CometChatTheme.colorScheme.errorColor
                        )
                    }
                } else {
                    // Cancel button — outlined
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            CometChatTheme.colorScheme.strokeColorDefault
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = style.linkDialogBackgroundColor,
                            contentColor = CometChatTheme.colorScheme.textColorPrimary
                        )
                    ) {
                        Text(
                            text = context.getString(R.string.cometchat_cancel),
                            style = style.linkDialogButtonTextStyle,
                            color = CometChatTheme.colorScheme.textColorPrimary
                        )
                    }
                }

                // Save button — filled primary
                Button(
                    onClick = {
                        if (linkUrl.isNotBlank()) {
                            val finalUrl = if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
                                "https://$linkUrl"
                            } else {
                                linkUrl
                            }
                            val finalText = linkText.ifBlank { finalUrl }
                            onApply(finalText, finalUrl)
                        }
                    },
                    enabled = linkUrl.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CometChatTheme.colorScheme.primary,
                        contentColor = CometChatTheme.colorScheme.textColorWhite,
                        disabledContainerColor = CometChatTheme.colorScheme.primary.copy(alpha = 0.4f),
                        disabledContentColor = CometChatTheme.colorScheme.textColorWhite.copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = context.getString(R.string.cometchat_save),
                        style = style.linkDialogButtonTextStyle,
                        color = CometChatTheme.colorScheme.textColorWhite
                    )
                }
            }
        }
    }
}
