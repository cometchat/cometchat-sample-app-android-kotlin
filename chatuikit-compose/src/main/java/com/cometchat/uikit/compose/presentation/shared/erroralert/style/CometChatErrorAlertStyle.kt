package com.cometchat.uikit.compose.presentation.shared.erroralert.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for
 * [com.cometchat.uikit.compose.presentation.shared.erroralert.CometChatErrorAlert].
 *
 * An error alert is a compact, transient banner that surfaces a short error message (e.g. why a
 * staged attachment was rejected) with a close affordance. The default matches the design system:
 * the theme's error red with white text — the same look as the iOS `CometChatSnackBar`. Use
 * [default] for a theme-backed instance, or override individual properties, as with every other
 * `CometChat…Style`.
 *
 * @property backgroundColor Background of the error alert surface.
 * @property contentColor Color of the message text.
 * @property closeIconTint Tint of the trailing close (✕) icon.
 * @property cornerRadius Corner radius of the error alert surface.
 * @property textStyle Text style of the message.
 */
@Immutable
data class CometChatErrorAlertStyle(
    val backgroundColor: Color,
    val contentColor: Color,
    val closeIconTint: Color,
    val cornerRadius: Dp,
    val textStyle: TextStyle
) {
    companion object {

        /** Creates a default error alert style using CometChat theme tokens (error red / white). */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.errorColor,
            contentColor: Color = CometChatTheme.colorScheme.colorWhite,
            closeIconTint: Color = CometChatTheme.colorScheme.colorWhite,
            cornerRadius: Dp = 8.dp,
            textStyle: TextStyle = CometChatTheme.typography.caption1Medium
        ): CometChatErrorAlertStyle = CometChatErrorAlertStyle(
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            closeIconTint = closeIconTint,
            cornerRadius = cornerRadius,
            textStyle = textStyle
        )
    }
}
