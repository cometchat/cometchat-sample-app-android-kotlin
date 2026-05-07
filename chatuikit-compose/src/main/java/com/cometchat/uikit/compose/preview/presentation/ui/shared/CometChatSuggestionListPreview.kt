package com.cometchat.uikit.compose.preview.presentation.ui.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.suggestionlist.CometChatSuggestionList
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Creates sample suggestion items for previews.
 */
private fun createSampleSuggestions(): List<SuggestionItem> = listOf(
    SuggestionItem(id = "1", name = "Alice Smith", promptText = "@Alice Smith", underlyingText = "<@uid:u1>"),
    SuggestionItem(id = "2", name = "Bob Johnson", promptText = "@Bob Johnson", underlyingText = "<@uid:u2>"),
    SuggestionItem(id = "3", name = "Charlie Brown", promptText = "@Charlie Brown", underlyingText = "<@uid:u3>"),
    SuggestionItem(id = "4", name = "Diana Prince", promptText = "@Diana Prince", underlyingText = "<@uid:u4>"),
    SuggestionItem(id = "5", name = "Edward Norton", promptText = "@Edward Norton", underlyingText = "<@uid:u5>")
)

@Preview(showBackground = true, name = "SuggestionList - Default")
@Composable
fun PreviewSuggestionListDefault() {
    CometChatTheme {
        CometChatSuggestionList(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            suggestions = createSampleSuggestions(),
            onItemClick = { }
        )
    }
}

@Preview(showBackground = true, name = "SuggestionList - Empty")
@Composable
fun PreviewSuggestionListEmpty() {
    CometChatTheme {
        CometChatSuggestionList(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            suggestions = emptyList()
        )
    }
}

@Preview(showBackground = true, name = "SuggestionList - Single Item")
@Composable
fun PreviewSuggestionListSingleItem() {
    CometChatTheme {
        CometChatSuggestionList(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            suggestions = listOf(
                SuggestionItem(id = "1", name = "Alice Smith", promptText = "@Alice Smith", underlyingText = "<@uid:u1>")
            ),
            onItemClick = { }
        )
    }
}

@Preview(showBackground = true, name = "SuggestionList - Loading")
@Composable
fun PreviewSuggestionListLoading() {
    CometChatTheme {
        CometChatSuggestionList(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            suggestions = createSampleSuggestions(),
            isLoading = true,
            onItemClick = { }
        )
    }
}

@Preview(showBackground = true, name = "SuggestionList - No Avatar")
@Composable
fun PreviewSuggestionListNoAvatar() {
    CometChatTheme {
        CometChatSuggestionList(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            suggestions = createSampleSuggestions(),
            showAvatar = false,
            onItemClick = { }
        )
    }
}
