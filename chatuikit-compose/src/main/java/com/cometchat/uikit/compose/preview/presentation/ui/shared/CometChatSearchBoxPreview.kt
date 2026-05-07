package com.cometchat.uikit.compose.preview.presentation.ui.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.searchbox.CometChatSearchBox
import com.cometchat.uikit.compose.presentation.shared.searchbox.CometChatSearchBoxStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

@Preview(showBackground = true, name = "SearchBox - Default")
@Composable
fun PreviewSearchBoxDefault() {
    CometChatTheme {
        CometChatSearchBox(
            modifier = Modifier.padding(16.dp),
            onTextChange = { }
        )
    }
}

@Preview(showBackground = true, name = "SearchBox - Custom Placeholder")
@Composable
fun PreviewSearchBoxCustomPlaceholder() {
    CometChatTheme {
        CometChatSearchBox(
            modifier = Modifier.padding(16.dp),
            placeholderText = "Find people...",
            onTextChange = { }
        )
    }
}

@Preview(showBackground = true, name = "SearchBox - With Text")
@Composable
fun PreviewSearchBoxWithText() {
    CometChatTheme {
        CometChatSearchBox(
            modifier = Modifier.padding(16.dp),
            text = "Alice",
            onTextChange = { },
            onClear = { }
        )
    }
}

@Preview(showBackground = true, name = "SearchBox - Disabled (Navigation Mode)")
@Composable
fun PreviewSearchBoxDisabled() {
    CometChatTheme {
        CometChatSearchBox(
            modifier = Modifier.padding(16.dp),
            enabled = false,
            onClick = { }
        )
    }
}

@Preview(showBackground = true, name = "SearchBox - Custom Background")
@Composable
fun PreviewSearchBoxCustomBackground() {
    CometChatTheme {
        CometChatSearchBox(
            modifier = Modifier.padding(16.dp),
            style = CometChatSearchBoxStyle.default(
                backgroundColor = Color(0xFFF0F4FF)
            ),
            onTextChange = { }
        )
    }
}
