package com.cometchat.uikit.compose.presentation.shared.erroralert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.erroralert.style.CometChatErrorAlertStyle
import kotlinx.coroutines.delay

/**
 * A compact, transient error alert banner — the CometChat equivalent of the iOS `CometChatSnackBar`.
 * Shows a short [message] with a trailing close (✕) button, slides up + fades in, and auto-dismisses
 * after [displayDurationMillis]. Styling is driven by [CometChatErrorAlertStyle] (error red / white
 * by default), so it stays consistent with the rest of the UI Kit and is fully restylable.
 *
 * Driven by a nullable [message]: pass the text to show it, and `null`/blank to hide it. The last
 * shown text is retained through the exit animation. The caller owns the state and is notified via
 * [onDismiss] both when the timer elapses and when the user taps ✕.
 *
 * @param message The message to show, or `null`/blank to hide.
 * @param onDismiss Invoked when the alert should be cleared (auto-dismiss timeout or ✕ tap).
 * @param modifier Modifier for the alert (usually alignment/padding within the hosting screen).
 * @param style Visual styling; defaults to theme-token-backed [CometChatErrorAlertStyle.default].
 * @param displayDurationMillis How long the alert stays before auto-dismissing.
 */
@Composable
fun CometChatErrorAlert(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    style: CometChatErrorAlertStyle = CometChatErrorAlertStyle.default(),
    displayDurationMillis: Long = 4000L
) {
    if (message.isNullOrBlank()) return
    // Auto-dismiss after the timeout. The caller owns visibility (it only composes this while there
    // is a message), so presence drives show/hide instead of an inner AnimatedVisibility — nesting
    // an exit animation inside a self-sizing Popup left the alert flashing on appear and stuck on ✕.
    LaunchedEffect(message) {
        delay(displayDurationMillis)
        onDismiss()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(style.cornerRadius))
            .background(style.backgroundColor)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            style = style.textStyle,
            color = style.contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.cometchat_ic_close),
            contentDescription = stringResource(R.string.cometchat_close),
            tint = style.closeIconTint,
            modifier = Modifier
                .size(20.dp)
                .clickable { onDismiss() }
        )
    }
}
