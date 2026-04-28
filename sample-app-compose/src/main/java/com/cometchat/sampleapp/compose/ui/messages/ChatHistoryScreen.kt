package com.cometchat.sampleapp.compose.ui.messages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.aiassistantchathistory.ui.CometChatAIAssistantChatHistory
import com.cometchat.uikit.core.factory.CometChatAIAssistantChatHistoryViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatAIAssistantChatHistoryViewModel

/**
 * Chat history screen composable for displaying AI assistant chat history.
 *
 * This screen displays the CometChatAIAssistantChatHistory component,
 * which shows a scrollable list of past AI assistant conversations grouped by date.
 *
 * ## Features:
 * - View past AI assistant conversations
 * - Navigate to a specific conversation
 * - Start a new AI chat
 * - Delete conversation history items
 *
 * ## Usage:
 * ```kotlin
 * ChatHistoryScreen(
 *     userId = "user123",
 *     onBackPress = { navController.popBackStack() },
 *     onNavigateToMessages = { userId ->
 *         navController.navigate(MessagesRoute(userId = userId, groupId = null))
 *     }
 * )
 * ```
 *
 * @param userId The UID of the user whose AI chat history to display
 * @param onBackPress Callback when close or new chat button is pressed
 * @param onNavigateToMessages Callback to navigate to messages with a user ID
 */
@Composable
fun ChatHistoryScreen(
    userId: String,
    onBackPress: () -> Unit,
    onNavigateToMessages: (String, Long?) -> Unit,
    onNewChat: (String) -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val chatHistoryViewModel: CometChatAIAssistantChatHistoryViewModel = viewModel(
        factory = CometChatAIAssistantChatHistoryViewModelFactory()
    )

    // Fetch user from CometChat SDK
    LaunchedEffect(userId) {
        isLoading = true
        CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(fetchedUser: User) {
                user = fetchedUser
                isLoading = false
            }

            override fun onError(e: CometChatException) {
                isLoading = false
            }
        })
    }

    // Set user on the ViewModel once loaded
    LaunchedEffect(user) {
        user?.let { chatHistoryViewModel.setUser(it) }
    }

    // Show chat history when user is loaded
    if (!isLoading && user != null) {
        CometChatAIAssistantChatHistory(
            modifier = Modifier.fillMaxSize(),
            viewModel = chatHistoryViewModel,
            onCloseClick = onBackPress,
            onNewChatClick = { user?.let { onNewChat(it.uid) } },
            onItemClick = { message ->
                // Pass message ID as parentMessageId so the message list loads the thread
                user?.let { onNavigateToMessages(it.uid, message.id.toLong()) }
            }
        )
    }
}
