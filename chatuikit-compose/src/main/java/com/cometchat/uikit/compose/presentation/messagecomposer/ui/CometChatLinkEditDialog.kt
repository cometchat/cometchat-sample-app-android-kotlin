package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle

/**
 * A dialog composable for editing link text and URL.
 * Used when inserting or editing links in the message composer.
 *
 * @param modifier Modifier for the dialog content
 * @param style Style configuration for the dialog
 * @param initialText Initial text for the link (display text)
 * @param initialUrl Initial URL for the link
 * @param onApply Callback when the Apply button is clicked with (text, url)
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun CometChatLinkEditDialog(
    modifier: Modifier = Modifier,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    initialText: String = "",
    initialUrl: String = "",
    onApply: (text: String, url: String) -> Unit = { _, _ -> },
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
                .padding(20.dp)
                .semantics { contentDescription = "Link Edit Dialog" }
        ) {
            // Title
            Text(
                text = context.getString(R.string.cometchat_insert_link),
                color = style.linkDialogTitleTextColor,
                style = style.linkDialogTitleTextStyle
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Link text input
            OutlinedTextField(
                value = linkText,
                onValueChange = { linkText = it },
                label = {
                    Text(
                        text = context.getString(R.string.cometchat_link_text),
                        color = style.linkDialogInputTextColor.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = style.linkDialogInputTextStyle.copy(color = style.linkDialogInputTextColor),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = style.linkDialogInputBackgroundColor,
                    unfocusedContainerColor = style.linkDialogInputBackgroundColor,
                    focusedTextColor = style.linkDialogInputTextColor,
                    unfocusedTextColor = style.linkDialogInputTextColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // URL input
            OutlinedTextField(
                value = linkUrl,
                onValueChange = { linkUrl = it },
                label = {
                    Text(
                        text = context.getString(R.string.cometchat_link_url),
                        color = style.linkDialogInputTextColor.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = style.linkDialogInputTextStyle.copy(color = style.linkDialogInputTextColor),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = style.linkDialogInputBackgroundColor,
                    unfocusedContainerColor = style.linkDialogInputBackgroundColor,
                    focusedTextColor = style.linkDialogInputTextColor,
                    unfocusedTextColor = style.linkDialogInputTextColor
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                placeholder = {
                    Text(
                        text = "https://",
                        color = style.linkDialogInputTextColor.copy(alpha = 0.4f)
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = context.getString(R.string.cometchat_cancel),
                        color = style.linkDialogButtonTextColor.copy(alpha = 0.7f),
                        style = style.linkDialogButtonTextStyle
                    )
                }

                TextButton(
                    onClick = {
                        if (linkUrl.isNotBlank()) {
                            // Ensure URL has protocol
                            val finalUrl = if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
                                "https://$linkUrl"
                            } else {
                                linkUrl
                            }
                            val finalText = linkText.ifBlank { finalUrl }
                            onApply(finalText, finalUrl)
                        }
                    },
                    enabled = linkUrl.isNotBlank()
                ) {
                    Text(
                        text = context.getString(R.string.cometchat_apply),
                        color = if (linkUrl.isNotBlank()) {
                            style.linkDialogButtonTextColor
                        } else {
                            style.linkDialogButtonTextColor.copy(alpha = 0.4f)
                        },
                        style = style.linkDialogButtonTextStyle
                    )
                }
            }
        }
    }
}
