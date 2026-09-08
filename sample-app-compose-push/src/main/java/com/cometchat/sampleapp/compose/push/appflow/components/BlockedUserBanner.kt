package com.cometchat.sampleapp.compose.push.appflow.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.sampleapp.compose.push.R

/**
 * Notice shown in place of the composer when the logged-in user has blocked this contact.
 *
 * Mirrors the Kotlin app's unblock_layout (activity_app_flow_messages.xml /
 * activity_thread_message.xml): a centered secondary-coloured line above a full-width outlined
 * card acting as the unblock button, with the label swapped for a spinner while the request is in
 * flight. Shared by the messages and thread screens so both stay in step.
 *
 * Callers supply the outer margin via [modifier] — the Kotlin layouts use 12dp on the messages
 * screen and 16dp on the thread screen.
 *
 * @param isUnblocking True while the unblock request is in flight; shows the spinner and blocks
 *   repeat taps, matching the Kotlin DialogState.INITIATED handling.
 * @param onUnblockClick Invoked when the unblock button is tapped.
 */
@Composable
internal fun BlockedUserBanner(
    isUnblocking: Boolean,
    onUnblockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.app_unblock_message),
            style = CometChatTheme.typography.bodyRegular,
            color = CometChatTheme.colorScheme.textColorSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = CometChatTheme.colorScheme.backgroundColor4
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, CometChatTheme.colorScheme.strokeColorDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable(enabled = !isUnblocking) { onUnblockClick() }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                if (isUnblocking) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = CometChatTheme.colorScheme.iconTintSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.app_unblock_user),
                        style = CometChatTheme.typography.bodyMedium,
                        color = CometChatTheme.colorScheme.textColorPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
