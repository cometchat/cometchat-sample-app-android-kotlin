package com.cometchat.ai.sampleapp.compose.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cometchat.ai.sampleapp.compose.app.AIAssistantApplication
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.aiassistantchathistory.ui.CometChatAIAssistantChatHistory
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.compose.presentation.messageheader.ui.CometChatMessageHeader
import com.cometchat.uikit.compose.presentation.messagelist.ui.CometChatMessageList
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.domain.model.ComposerLayoutMode
import com.cometchat.uikit.core.factory.CometChatAIAssistantChatHistoryViewModelFactory
import com.cometchat.uikit.core.viewmodel.CometChatAIAssistantChatHistoryViewModel
import kotlinx.coroutines.launch

/**
 * AI Assistant chat screen (Compose).
 *
 * - Uses a [ModalNavigationDrawer] with the chat history on the end side
 *   (mirrors the XML DrawerLayout).
 * - Auto-detects agent chats and hides attachment/voice/sticker buttons and
 *   rich-text toolbar via UIKit's built-in agent-chat handling.
 * - Fetches the [User] from [CometChat] when the screen first composes.
 */
@Composable
fun AIAssistantChatScreen(
    userId: String,
    parentMessageId: Long?,
    onBackPress: () -> Unit,
    /**
     * Invoked when the user requests to open a different conversation
     * (new chat, historical conversation, mention tap, etc.).
     */
    onOpenConversation: (userId: String, parentMessageId: Long?) -> Unit,
    viewModel: AIAssistantChatViewModel = viewModel()
) {
    val colorScheme = CometChatTheme.colorScheme

    // Fetch user
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        isLoading = true
        CometChat.getUser(userId, object : CometChat.CallbackListener<User>() {
            override fun onSuccess(p0: User) {
                user = p0
                isLoading = false
            }

            override fun onError(exception: CometChatException?) {
                isLoading = false
            }
        })
    }

    // Track current open chat for in-app notification suppression
    DisposableEffect(userId) {
        AIAssistantApplication.currentOpenChatId = userId
        onDispose { AIAssistantApplication.currentOpenChatId = null }
    }

    // Subscribe to UIKit events for agent-chat UX side effects.
    DisposableEffect(viewModel) {
        viewModel.addListeners()
        onDispose { viewModel.removeListeners() }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Hide keyboard on send for agent chats
    LaunchedEffect(viewModel) {
        viewModel.sentMessage.collect {
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
        }
    }

    // Handle navigation-to-different-chat requests from UIKit
    LaunchedEffect(viewModel) {
        viewModel.openUserChat.collect { u ->
            if (u != null) onOpenConversation(u.uid, null)
        }
    }

    // Exit on conversation deletion
    LaunchedEffect(viewModel) {
        viewModel.isExitActivity.collect { exit ->
            if (exit) onBackPress()
        }
    }

    if (isLoading || user == null) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.backgroundColor1),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
        return
    }

    // Drawer setup — chat history is the end-drawer content.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Create chat-history ViewModel once per user (fresh history per agent)
    val chatHistoryViewModel: CometChatAIAssistantChatHistoryViewModel = viewModel(
        key = "chatHistory-$userId",
        factory = CometChatAIAssistantChatHistoryViewModelFactory()
    )

    // Bind the user on the chat-history ViewModel when the drawer opens
    LaunchedEffect(user) {
        user?.let { chatHistoryViewModel.setUser(it) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen, // only while open, matches XML lock mode
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colorScheme.backgroundColor1,
                drawerContentColor = colorScheme.textColorPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                CometChatAIAssistantChatHistory(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = chatHistoryViewModel,
                    onCloseClick = { scope.launch { drawerState.close() } },
                    onNewChatClick = {
                        scope.launch { drawerState.close() }
                        onOpenConversation(userId, null)
                    },
                    onItemClick = { message ->
                        onOpenConversation(userId, message.id.toLong())
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = colorScheme.backgroundColor1,
            contentWindowInsets = WindowInsets.statusBars
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                CometChatMessageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    user = user,
                    hideBackButton = false,
                    onBackPress = onBackPress,
                    onNewChatClick = {
                        // Fresh conversation with the same agent
                        onOpenConversation(userId, null)
                    },
                    onChatHistoryClick = {
                        scope.launch { drawerState.open() }
                    }
                )

                CometChatMessageList(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    user = user,
                    parentMessageId = parentMessageId ?: -1
                )

                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth(),
                    user = user,
                    parentMessageId = parentMessageId ?: -1,
                    layoutMode = ComposerLayoutMode.SINGLE_LINE,
                    // Agent-chat detection already hides attachments / voice / stickers
                    // and disables rich text; these explicit flags keep the UX compact
                    // regardless of detection.
                    hideAttachmentButton = true,
                    hideVoiceRecordingButton = true,
                    hideStickersButton = true,
                    enableRichTextFormatting = false
                )
            }
        }
    }
}
