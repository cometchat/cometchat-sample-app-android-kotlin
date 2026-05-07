package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.compose.presentation.messageinformation.ui.CometChatMessageInformation
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Creates a mock message for MessageInformation previews.
 */
private fun createMockMessageForInfo(): BaseMessage {
    return PreviewMockData.createMockTextMessage(
        text = "Hello! This is a test message for information view.",
        sender = PreviewMockData.createMockUser(name = "Alice Smith"),
        deliveredAt = System.currentTimeMillis() / 1000 - 60,
        readAt = System.currentTimeMillis() / 1000 - 30
    )
}

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

/**
 * Preview showing the default message information bottom sheet.
 */
@Preview(showBackground = true, name = "MessageInfo - Default")
@Composable
fun PreviewMessageInformationDefault() {
    CometChatTheme {
        CometChatMessageInformation(
            message = createMockMessageForInfo(),
            onDismiss = { }
        )
    }
}

// ============================================================================
// SECTION 2: VISIBILITY PREVIEWS
// ============================================================================

/**
 * Preview with toolbar hidden.
 */
@Preview(showBackground = true, name = "MessageInfo - No Toolbar")
@Composable
fun PreviewMessageInformationNoToolbar() {
    CometChatTheme {
        CometChatMessageInformation(
            message = createMockMessageForInfo(),
            hideToolBar = true,
            onDismiss = { }
        )
    }
}

// ============================================================================
// SECTION 3: TOOLBAR CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom toolbar title.
 */
@Preview(showBackground = true, name = "MessageInfo - Custom Title")
@Composable
fun PreviewMessageInformationCustomTitle() {
    CometChatTheme {
        CometChatMessageInformation(
            message = createMockMessageForInfo(),
            toolBarTitleText = "Receipt Details",
            onDismiss = { }
        )
    }
}

// ============================================================================
// SECTION 4: CUSTOM VIEW OVERRIDES
// ============================================================================

/**
 * Preview with custom bubble view.
 */
@Preview(showBackground = true, name = "MessageInfo - Custom Bubble View")
@Composable
fun PreviewMessageInformationCustomBubbleView() {
    CometChatTheme {
        CometChatMessageInformation(
            message = createMockMessageForInfo(),
            bubbleView = { message ->
                Text(
                    text = "Custom: ${(message as? com.cometchat.chat.models.TextMessage)?.text ?: "Message"}",
                    style = CometChatTheme.typography.bodyRegular,
                    color = CometChatTheme.colorScheme.textColorPrimary
                )
            },
            onDismiss = { }
        )
    }
}

// ============================================================================
// SECTION 5: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom style.
 */
@Preview(showBackground = true, name = "MessageInfo - Custom Style")
@Composable
fun PreviewMessageInformationCustomStyle() {
    CometChatTheme {
        CometChatMessageInformation(
            message = createMockMessageForInfo(),
            style = CometChatMessageInformationStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            ),
            onDismiss = { }
        )
    }
}
