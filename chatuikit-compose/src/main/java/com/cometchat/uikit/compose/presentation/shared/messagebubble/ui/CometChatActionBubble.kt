package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.Action
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatActionBubbleStyle

/**
 * A composable that displays an action message bubble.
 *
 * This component renders system action messages (like "User joined the group")
 * with a centered, distinct visual style to differentiate from regular messages.
 *
 * Features:
 * - Centered text display
 * - Support for AnnotatedString (mentions, formatting)
 * - Distinct visual style
 *
 * Example usage:
 * ```kotlin
 * CometChatActionBubble(
 *     message = actionMessage,
 *     style = CometChatActionBubbleStyle.default()
 * )
 * ```
 *
 * @param message The [Action] message to display
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance (extends CometChatMessageBubbleStyle,
 *              so all wrapper properties like backgroundColor, cornerRadius are directly accessible)
 */
@Composable
fun CometChatActionBubble(
    message: Action,
    modifier: Modifier = Modifier,
    style: CometChatActionBubbleStyle = CometChatActionBubbleStyle.default()
) {
    val actionText = remember(message.id) {
        message.message ?: ""
    }

    CometChatActionBubble(
        text = actionText,
        modifier = modifier,
        style = style
    )
}

/**
 * Overload for displaying action bubble with a plain string.
 *
 * @param text The action text to display
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance (extends CometChatMessageBubbleStyle,
 *              so all wrapper properties like backgroundColor, cornerRadius are directly accessible)
 */
@Composable
fun CometChatActionBubble(
    text: String,
    modifier: Modifier = Modifier,
    style: CometChatActionBubbleStyle = CometChatActionBubbleStyle.default()
) {
    ActionBubbleContent(
        text = text,
        modifier = modifier,
        style = style
    )
}

/**
 * Overload for displaying action bubble with an AnnotatedString.
 * Useful for messages with mentions or other formatting.
 *
 * @param text The annotated action text to display
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance (extends CometChatMessageBubbleStyle,
 *              so all wrapper properties like backgroundColor, cornerRadius are directly accessible)
 */
@Composable
fun CometChatActionBubble(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: CometChatActionBubbleStyle = CometChatActionBubbleStyle.default()
) {
    ActionBubbleAnnotatedContent(
        text = text,
        modifier = modifier,
        style = style
    )
}

@Composable
private fun ActionBubbleContent(
    text: String,
    modifier: Modifier,
    style: CometChatActionBubbleStyle
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(style.cornerRadius))
                .background(style.backgroundColor)
                .then(
                    if (style.strokeWidth > 0.dp) {
                        Modifier.border(
                            width = style.strokeWidth,
                            color = style.strokeColor,
                            shape = RoundedCornerShape(style.cornerRadius)
                        )
                    } else Modifier
                )
                .padding(style.padding)
                .semantics {
                    contentDescription = "Action message: $text"
                }
        ) {
            Text(
                text = text,
                style = style.textStyle,
                color = style.textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionBubbleAnnotatedContent(
    text: AnnotatedString,
    modifier: Modifier,
    style: CometChatActionBubbleStyle
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(style.cornerRadius))
                .background(style.backgroundColor)
                .then(
                    if (style.strokeWidth > 0.dp) {
                        Modifier.border(
                            width = style.strokeWidth,
                            color = style.strokeColor,
                            shape = RoundedCornerShape(style.cornerRadius)
                        )
                    } else Modifier
                )
                .padding(style.padding)
                .semantics {
                    contentDescription = "Action message: ${text.text}"
                }
        ) {
            Text(
                text = text,
                style = style.textStyle,
                color = style.textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
