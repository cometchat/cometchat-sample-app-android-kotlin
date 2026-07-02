package com.cometchat.sampleapp.compose.push.appflow.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.aiassistantchathistory.ui.CometChatAIAssistantChatHistory
import com.cometchat.uikit.core.factory.CometChatAIAssistantChatHistoryViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatAIAssistantChatHistoryViewModel

/**
 * Chat history screen composable for displaying AI assistant chat history.
 *
 * Displays the CometChatAIAssistantChatHistory component,
 * which shows a scrollable list of past AI assistant conversations grouped by date.
 *
 * ## Features:
 * - View past AI assistant conversations
 * - Navigate to a specific conversation
 * - Start a new AI chat
 * - Delete conversation history items
 *
 * @param user The resolved User whose AI chat history to display
 * @param onBackPress Callback when close or new chat button is pressed
 * @param onNavigateToMessages Callback to navigate to messages with a user ID
 */
@Composable
fun ChatHistoryScreen(
    user: User,
    onBackPress: () -> Unit,
    onNavigateToMessages: (String, Long?) -> Unit,
    onNewChat: (String) -> Unit
) {
    val chatHistoryViewModel: CometChatAIAssistantChatHistoryViewModel = viewModel(
        factory = CometChatAIAssistantChatHistoryViewModelFactory()
    )

    // Set user on the ViewModel
    LaunchedEffect(user) {
        chatHistoryViewModel.setUser(user)
    }

    CometChatAIAssistantChatHistory(
        modifier = Modifier.fillMaxSize(),
        viewModel = chatHistoryViewModel,
        onCloseClick = onBackPress,
        onNewChatClick = { onNewChat(user.uid) },
        onItemClick = { message ->
            onNavigateToMessages(user.uid, message.id.toLong())
        }
    )
}
