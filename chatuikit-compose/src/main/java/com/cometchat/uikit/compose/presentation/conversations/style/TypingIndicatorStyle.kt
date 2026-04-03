package com.cometchat.uikit.compose.presentation.conversations.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Style configuration for the typing indicator.
 *
 * The typing indicator shows when other users are typing in a conversation.
 *
 * @param textColor Color for the typing indicator text
 * @param textStyle Text style for the typing indicator
 */
@Immutable
data class TypingIndicatorStyle(
    val textColor: Color,
    val textStyle: TextStyle
) {
    companion object {
        /**
         * Creates a default TypingIndicatorStyle with values sourced from CometChatTheme.
         *
         * @return A new TypingIndicatorStyle instance with theme-based default values
         */
        @Composable
        fun default(
            textColor: Color = CometChatTheme.colorScheme.textColorHighlight,
            textStyle: TextStyle = CometChatTheme.typography.bodyRegular
        ): TypingIndicatorStyle = TypingIndicatorStyle(
            textColor = textColor,
            textStyle = textStyle
        )
    }
}