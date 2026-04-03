package com.cometchat.uikit.compose.presentation.threadheader.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatThreadHeader composable.
 *
 * This style class defines all visual styling properties for the thread header component,
 * allowing customization of:
 * - Container appearance (background, stroke, corner radius)
 * - Reply count text styling (color, style, background)
 * - Message bubble styles (incoming/outgoing)
 *
 * ## Usage
 *
 * ```kotlin
 * CometChatThreadHeader(
 *     parentMessage = message,
 *     style = CometChatThreadHeaderStyle.default(
 *         backgroundColor = CometChatTheme.colorScheme.backgroundColor3,
 *         replyCountTextColor = CometChatTheme.colorScheme.textColorSecondary
 *     )
 * )
 * ```
 *
 * ## Factory Functions
 *
 * - [default]: Creates a style with theme defaults
 *
 * @property backgroundColor Background color for the thread header container
 * @property strokeColor Border/stroke color for the container
 * @property strokeWidth Border/stroke width for the container
 * @property cornerRadius Corner radius for the container
 * @property replyCountBackgroundColor Background color for the reply count bar
 * @property replyCountTextColor Text color for the reply count text
 * @property replyCountTextStyle Text style for the reply count text
 * @property incomingMessageBubbleStyle Style for incoming message bubbles, or null to use default
 * @property outgoingMessageBubbleStyle Style for outgoing message bubbles, or null to use default
 *
 * **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 7.2, 7.3**
 *
 * @see CometChatTheme
 */
@Immutable
data class CometChatThreadHeaderStyle(
    // Container styling
    val backgroundColor: Color,
    val strokeColor: Color,
    val strokeWidth: Dp,
    val cornerRadius: Dp,

    // Reply count styling
    val replyCountBackgroundColor: Color,
    val replyCountTextColor: Color,
    val replyCountTextStyle: TextStyle,

    // Message bubble styles (nested component style objects)
    val incomingMessageBubbleStyle: CometChatMessageBubbleStyle?,
    val outgoingMessageBubbleStyle: CometChatMessageBubbleStyle?
) {
    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Does NOT use Material Theme colors directly.
         *
         * @return A new CometChatThreadHeaderStyle instance with theme-based default values
         */
        @Composable
        fun default(
            // Container styling
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            strokeColor: Color = Color.Transparent,
            strokeWidth: Dp = 0.dp,
            cornerRadius: Dp = 0.dp,

            // Reply count styling
            replyCountBackgroundColor: Color = CometChatTheme.colorScheme.extendedPrimaryColor100,
            replyCountTextColor: Color = CometChatTheme.colorScheme.textColorSecondary,
            replyCountTextStyle: TextStyle = CometChatTheme.typography.caption1Medium,

            // Message bubble styles
            incomingMessageBubbleStyle: CometChatMessageBubbleStyle? = null,
            outgoingMessageBubbleStyle: CometChatMessageBubbleStyle? = null
        ): CometChatThreadHeaderStyle = CometChatThreadHeaderStyle(
            backgroundColor = backgroundColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            cornerRadius = cornerRadius,
            replyCountBackgroundColor = replyCountBackgroundColor,
            replyCountTextColor = replyCountTextColor,
            replyCountTextStyle = replyCountTextStyle,
            incomingMessageBubbleStyle = incomingMessageBubbleStyle,
            outgoingMessageBubbleStyle = outgoingMessageBubbleStyle
        )
    }
}
