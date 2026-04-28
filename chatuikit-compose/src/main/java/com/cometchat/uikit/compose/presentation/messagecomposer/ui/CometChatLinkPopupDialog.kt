package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * A popup dialog shown when the user taps the Link toolbar button while the cursor
 * is inside an existing link span. Displays the URL and provides Edit / Remove actions.
 *
 * Layout (v5 parity):
 * - Title: "Link" (bold, textColorPrimary)
 * - URL displayed as clickable text (primary color, underlined)
 * - Two buttons: "Edit" (outlined, strokeColorDefault border) + "Remove" (filled, errorColor bg, white text)
 * - No input fields, no X close button
 * - Rounded corners 16dp, backgroundColor1 background
 */
@Composable
fun CometChatLinkPopupDialog(
    url: String,
    style: CometChatMessageComposerStyle = CometChatMessageComposerStyle.default(),
    onEdit: () -> Unit = {},
    onRemove: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = style.linkDialogBackgroundColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
                .semantics { contentDescription = "Link Popup Dialog" }
        ) {
            // Title: "Link"
            Text(
                text = context.getString(R.string.cometchat_link),
                color = style.linkDialogTitleTextColor,
                style = style.linkDialogTitleTextStyle
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Clickable URL text
            Text(
                text = url,
                color = CometChatTheme.colorScheme.primary,
                style = style.linkDialogInputTextStyle.copy(
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier.clickable {
                    try {
                        uriHandler.openUri(url)
                    } catch (_: Exception) {
                        // Ignore if URL can't be opened
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons: Edit (outlined) + Remove (filled red)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit button — outlined with strokeColorDefault border
                OutlinedButton(
                    onClick = onEdit,
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
                        text = context.getString(R.string.cometchat_edit),
                        style = style.linkDialogButtonTextStyle,
                        color = CometChatTheme.colorScheme.textColorPrimary
                    )
                }

                // Remove button — filled with errorColor background, white text
                Button(
                    onClick = onRemove,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CometChatTheme.colorScheme.errorColor,
                        contentColor = CometChatTheme.colorScheme.textColorWhite
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = context.getString(R.string.cometchat_remove),
                        style = style.linkDialogButtonTextStyle,
                        color = CometChatTheme.colorScheme.textColorWhite
                    )
                }
            }
        }
    }
}
