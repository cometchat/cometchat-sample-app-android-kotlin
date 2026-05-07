package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.presentation.threadheader.style.CometChatThreadHeaderStyle
import com.cometchat.uikit.compose.presentation.threadheader.ui.CometChatThreadHeader
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Creates a mock BaseMessage (TextMessage) for thread header previews.
 */
private fun createMockParentMessage(
    text: String = "This is the parent message that started the thread",
    replyCount: Int = 5
): BaseMessage {
    val msg = PreviewMockData.createMockTextMessage(
        text = text,
        sender = PreviewMockData.createMockUser(name = "Alice Smith")
    )
    msg.replyCount = replyCount
    return msg
}

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

/**
 * Preview showing the default thread header with a parent message.
 */
@Preview(showBackground = true, name = "ThreadHeader - Default")
@Composable
fun PreviewThreadHeaderDefault() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage()
        )
    }
}

/**
 * Preview showing thread header with single reply.
 */
@Preview(showBackground = true, name = "ThreadHeader - Single Reply")
@Composable
fun PreviewThreadHeaderSingleReply() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(replyCount = 1)
        )
    }
}

/**
 * Preview showing thread header with many replies.
 */
@Preview(showBackground = true, name = "ThreadHeader - Many Replies")
@Composable
fun PreviewThreadHeaderManyReplies() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(replyCount = 150)
        )
    }
}

/**
 * Preview showing thread header with no replies.
 */
@Preview(showBackground = true, name = "ThreadHeader - No Replies")
@Composable
fun PreviewThreadHeaderNoReplies() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(replyCount = 0)
        )
    }
}

// ============================================================================
// SECTION 2: VISIBILITY PREVIEWS
// ============================================================================

/**
 * Preview with reactions hidden.
 */
@Preview(showBackground = true, name = "ThreadHeader - Visibility - No Reactions")
@Composable
fun PreviewThreadHeaderNoReactions() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            hideReactions = true
        )
    }
}

/**
 * Preview with avatar hidden.
 */
@Preview(showBackground = true, name = "ThreadHeader - Visibility - No Avatar")
@Composable
fun PreviewThreadHeaderNoAvatar() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            hideAvatar = true
        )
    }
}

/**
 * Preview with receipts hidden.
 */
@Preview(showBackground = true, name = "ThreadHeader - Visibility - No Receipts")
@Composable
fun PreviewThreadHeaderNoReceipts() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            hideReceipts = true
        )
    }
}

/**
 * Preview with reply count hidden.
 */
@Preview(showBackground = true, name = "ThreadHeader - Visibility - No Reply Count")
@Composable
fun PreviewThreadHeaderNoReplyCount() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            hideReplyCount = true
        )
    }
}

/**
 * Preview with reply count bar hidden.
 */
@Preview(showBackground = true, name = "ThreadHeader - Visibility - No Reply Count Bar")
@Composable
fun PreviewThreadHeaderNoReplyCountBar() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            hideReplyCountBar = true
        )
    }
}

/**
 * Preview with all visibility flags hidden.
 */
@Preview(showBackground = true, name = "ThreadHeader - Visibility - All Hidden")
@Composable
fun PreviewThreadHeaderAllHidden() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            hideReactions = true,
            hideAvatar = true,
            hideReceipts = true,
            hideReplyCount = true
        )
    }
}

// ============================================================================
// SECTION 3: ALIGNMENT PREVIEWS
// ============================================================================

/**
 * Preview with standard alignment.
 */
@Preview(showBackground = true, name = "ThreadHeader - Alignment - Standard")
@Composable
fun PreviewThreadHeaderStandardAlignment() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            alignment = UIKitConstants.MessageListAlignment.STANDARD
        )
    }
}

/**
 * Preview with left-aligned messages.
 */
@Preview(showBackground = true, name = "ThreadHeader - Alignment - Left Aligned")
@Composable
fun PreviewThreadHeaderLeftAligned() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            alignment = UIKitConstants.MessageListAlignment.LEFT_ALIGNED
        )
    }
}

// ============================================================================
// SECTION 4: CUSTOM VIEW OVERRIDES
// ============================================================================

/**
 * Preview with custom reply count view.
 */
@Preview(showBackground = true, name = "ThreadHeader - Custom View - Reply Count")
@Composable
fun PreviewThreadHeaderCustomReplyCountView() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(replyCount = 12),
            replyCountView = { count ->
                Text(
                    text = "💬 $count responses in this thread",
                    style = CometChatTheme.typography.caption1Medium,
                    color = CometChatTheme.colorScheme.primary
                )
            }
        )
    }
}

/**
 * Preview with custom message bubble view.
 */
@Preview(showBackground = true, name = "ThreadHeader - Custom View - Message Bubble")
@Composable
fun PreviewThreadHeaderCustomBubbleView() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            messageBubbleView = { message ->
                Text(
                    text = "Custom bubble: ${(message as? com.cometchat.chat.models.TextMessage)?.text ?: "Message"}",
                    style = CometChatTheme.typography.bodyRegular,
                    color = CometChatTheme.colorScheme.textColorPrimary
                )
            }
        )
    }
}

// ============================================================================
// SECTION 5: MAX HEIGHT PREVIEWS
// ============================================================================

/**
 * Preview with constrained max height.
 */
@Preview(showBackground = true, name = "ThreadHeader - Max Height - 200dp")
@Composable
fun PreviewThreadHeaderMaxHeight200() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            maxHeight = 200.dp
        )
    }
}

/**
 * Preview with unconstrained height.
 */
@Preview(showBackground = true, name = "ThreadHeader - Max Height - Unspecified")
@Composable
fun PreviewThreadHeaderMaxHeightUnspecified() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            maxHeight = Dp.Unspecified
        )
    }
}

// ============================================================================
// SECTION 6: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "ThreadHeader - Style - Custom Background")
@Composable
fun PreviewThreadHeaderCustomBackground() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            style = CometChatThreadHeaderStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            )
        )
    }
}

// ============================================================================
// SECTION 7: COMPREHENSIVE PREVIEWS
// ============================================================================

/**
 * Preview showing all features combined.
 */
@Preview(showBackground = true, name = "ThreadHeader - Comprehensive - All Features")
@Composable
fun PreviewThreadHeaderComprehensive() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(replyCount = 25),
            maxHeight = 300.dp,
            alignment = UIKitConstants.MessageListAlignment.STANDARD,
            leftBubbleMargin = PaddingValues(start = 12.dp, end = 12.dp),
            rightBubbleMargin = PaddingValues(start = 12.dp, end = 12.dp)
        )
    }
}

/**
 * Preview showing minimal configuration.
 */
@Preview(showBackground = true, name = "ThreadHeader - Comprehensive - Minimal")
@Composable
fun PreviewThreadHeaderMinimal() {
    CometChatTheme {
        CometChatThreadHeader(
            parentMessage = createMockParentMessage(),
            hideReactions = true,
            hideAvatar = true,
            hideReceipts = true,
            hideReplyCountBar = true
        )
    }
}
