package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatDeleteBubbleStyle

/**
 * A composable that displays a deleted message bubble.
 *
 * This component renders a placeholder for deleted messages with:
 * - A delete icon
 * - "This message was deleted" text in italic style
 * - Distinct styling to differentiate from regular messages
 *
 * Example usage:
 * ```kotlin
 * CometChatDeleteBubble(
 *     message = baseMessage,
 *     alignment = MessageBubbleAlignment.LEFT,
 *     style = CometChatDeleteBubbleStyle.incoming()
 * )
 * ```
 *
 * @param message The [BaseMessage] that was deleted (used for context)
 * @param alignment The bubble alignment (LEFT, RIGHT, or CENTER)
 * @param modifier Modifier for the bubble container
 * @param style Style configuration for the bubble appearance. Since [CometChatDeleteBubbleStyle]
 *   extends [CometChatMessageBubbleStyle], all wrapper properties (backgroundColor, cornerRadius,
 *   strokeWidth, strokeColor) are directly accessible on the style object.
 * @param text Custom text to display (defaults to "This message was deleted")
 */
@Composable
fun CometChatDeleteBubble(
    message: BaseMessage,
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatDeleteBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatDeleteBubbleStyle.outgoing()
        else -> CometChatDeleteBubbleStyle.incoming()
    },
    text: String = stringResource(id = R.string.cometchat_this_message_deleted)
) {
    DeleteBubbleContent(
        modifier = modifier,
        style = style,
        text = text
    )
}

/**
 * Overload for displaying delete bubble without a message object.
 * Useful for previews and testing.
 */
@Composable
fun CometChatDeleteBubble(
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    style: CometChatDeleteBubbleStyle = when (alignment) {
        UIKitConstants.MessageBubbleAlignment.RIGHT -> CometChatDeleteBubbleStyle.outgoing()
        else -> CometChatDeleteBubbleStyle.incoming()
    },
    text: String = stringResource(id = R.string.cometchat_this_message_deleted)
) {
    DeleteBubbleContent(
        modifier = modifier,
        style = style,
        text = text
    )
}

@Composable
private fun DeleteBubbleContent(
    modifier: Modifier,
    style: CometChatDeleteBubbleStyle,
    text: String
) {
    Row(
        modifier = modifier
            .padding(12.dp)
            .semantics {
                contentDescription = "Deleted message"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.cometchat_ic_delete_bubble),
            contentDescription = "Delete icon",
            modifier = Modifier.size(16.dp),
            tint = style.iconTint
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = style.textStyle,
            color = style.textColor
        )
    }
}
