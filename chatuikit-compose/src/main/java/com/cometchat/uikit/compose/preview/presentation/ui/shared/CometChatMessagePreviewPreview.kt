package com.cometchat.uikit.compose.preview.presentation.ui.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.messagepreview.CometChatMessagePreview
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.compose.theme.CometChatTheme

@Preview(showBackground = true, name = "MessagePreview - Default")
@Composable
fun PreviewMessagePreviewDefault() {
    CometChatTheme {
        CometChatMessagePreview(
            message = PreviewMockData.createMockTextMessage(
                text = "Hey! Are you coming to the meeting?",
                sender = PreviewMockData.createMockUser(name = "Alice Smith")
            ),
            modifier = Modifier.padding(16.dp),
            onCloseClick = { }
        )
    }
}

@Preview(showBackground = true, name = "MessagePreview - No Close Icon")
@Composable
fun PreviewMessagePreviewNoCloseIcon() {
    CometChatTheme {
        CometChatMessagePreview(
            message = PreviewMockData.createMockTextMessage(
                text = "See you tomorrow!",
                sender = PreviewMockData.createMockUser(name = "Bob Johnson")
            ),
            modifier = Modifier.padding(16.dp),
            showCloseIcon = false
        )
    }
}

@Preview(showBackground = true, name = "MessagePreview - Long Message")
@Composable
fun PreviewMessagePreviewLongMessage() {
    CometChatTheme {
        CometChatMessagePreview(
            message = PreviewMockData.createMockTextMessage(
                text = "This is a very long message that should be truncated in the preview to show how the component handles overflow text gracefully without breaking the layout.",
                sender = PreviewMockData.createMockUser(name = "Charlie Brown")
            ),
            modifier = Modifier.padding(16.dp),
            onCloseClick = { }
        )
    }
}

@Preview(showBackground = true, name = "MessagePreview - With Click")
@Composable
fun PreviewMessagePreviewWithClick() {
    CometChatTheme {
        CometChatMessagePreview(
            message = PreviewMockData.createMockTextMessage(
                text = "Tap to scroll to this message",
                sender = PreviewMockData.createMockUser(name = "Diana Prince")
            ),
            modifier = Modifier.padding(16.dp),
            onCloseClick = { },
            onClick = { }
        )
    }
}
