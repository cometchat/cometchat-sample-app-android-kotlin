package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.aiassistantchathistory.style.CometChatAIAssistantChatHistoryStyle
import com.cometchat.uikit.compose.presentation.aiassistantchathistory.ui.CometChatAIAssistantChatHistory
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

/**
 * Preview showing the default AI assistant chat history.
 */
@Preview(showBackground = true, name = "AIChatHistory - Default")
@Composable
fun PreviewAIChatHistoryDefault() {
    CometChatTheme {
        CometChatAIAssistantChatHistory(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            onCloseClick = { },
            onNewChatClick = { }
        )
    }
}

// ============================================================================
// SECTION 2: CUSTOM VIEW OVERRIDES
// ============================================================================

/**
 * Preview with custom empty state view.
 */
@Preview(showBackground = true, name = "AIChatHistory - Custom Empty View")
@Composable
fun PreviewAIChatHistoryCustomEmptyView() {
    CometChatTheme {
        CometChatAIAssistantChatHistory(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            emptyStateView = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No AI conversations yet",
                        style = CometChatTheme.typography.heading3Medium,
                        color = CometChatTheme.colorScheme.textColorSecondary
                    )
                }
            },
            onCloseClick = { },
            onNewChatClick = { }
        )
    }
}

/**
 * Preview with custom error state view.
 */
@Preview(showBackground = true, name = "AIChatHistory - Custom Error View")
@Composable
fun PreviewAIChatHistoryCustomErrorView() {
    CometChatTheme {
        CometChatAIAssistantChatHistory(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            errorStateView = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load chat history",
                        style = CometChatTheme.typography.bodyRegular,
                        color = CometChatTheme.colorScheme.errorColor
                    )
                }
            },
            onCloseClick = { },
            onNewChatClick = { }
        )
    }
}

/**
 * Preview with custom loading state view.
 */
@Preview(showBackground = true, name = "AIChatHistory - Custom Loading View")
@Composable
fun PreviewAIChatHistoryCustomLoadingView() {
    CometChatTheme {
        CometChatAIAssistantChatHistory(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            loadingStateView = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading conversations...",
                        style = CometChatTheme.typography.bodyRegular,
                        color = CometChatTheme.colorScheme.textColorSecondary
                    )
                }
            },
            onCloseClick = { },
            onNewChatClick = { }
        )
    }
}

// ============================================================================
// SECTION 3: CALLBACK PREVIEWS
// ============================================================================

/**
 * Preview with all callbacks configured.
 */
@Preview(showBackground = true, name = "AIChatHistory - All Callbacks")
@Composable
fun PreviewAIChatHistoryAllCallbacks() {
    CometChatTheme {
        CometChatAIAssistantChatHistory(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            onCloseClick = { },
            onNewChatClick = { },
            onItemClick = { },
            onItemLongClick = { }
        )
    }
}

// ============================================================================
// SECTION 4: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom style.
 */
@Preview(showBackground = true, name = "AIChatHistory - Custom Style")
@Composable
fun PreviewAIChatHistoryCustomStyle() {
    CometChatTheme {
        CometChatAIAssistantChatHistory(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            style = CometChatAIAssistantChatHistoryStyle.default(
                chatHistoryBackgroundColor = Color(0xFFF5F5F5)
            ),
            onCloseClick = { },
            onNewChatClick = { }
        )
    }
}
