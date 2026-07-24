package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.data.datasource.MessageComposerDataSource
import com.cometchat.uikit.core.data.repository.MessageComposerRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Instrumented Compose UI tests for CometChatMessageComposer.
 * Uses Compose Test Rule to verify composable rendering and interactions.
 *
 * Covers:
 * - Composer renders with placeholder text
 * - Attachment button is displayed
 * - Send button is displayed
 * - Voice recording button is displayed
 * - Hiding buttons works correctly
 * - Edit preview displays when in edit mode
 * - Reply preview displays when in reply mode
 * - Custom placeholder text renders
 */
@RunWith(AndroidJUnit4::class)
class CometChatMessageComposerListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createFakeDataSource(): MessageComposerDataSource {
        return object : MessageComposerDataSource {
            override suspend fun sendTextMessage(message: TextMessage): TextMessage {
                val sent = mock<TextMessage>()
                whenever(sent.id).thenReturn(1)
                return sent
            }
            override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = mock()
            override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage = mock()
            override suspend fun editMessage(message: BaseMessage): BaseMessage = mock()
        }
    }

    /**
     * Test-safe ViewModel subclass that overrides startTyping/endTyping to avoid
     * CometChat.init() requirement during UI tests.
     */
    private class TestMessageComposerViewModel(
        sendTextMessageUseCase: SendTextMessageUseCase,
        sendMediaMessageUseCase: SendMediaMessageUseCase,
        sendCustomMessageUseCase: SendCustomMessageUseCase,
        editMessageUseCase: EditMessageUseCase
    ) : CometChatMessageComposerViewModel(
        sendTextMessageUseCase = sendTextMessageUseCase,
        sendMediaMessageUseCase = sendMediaMessageUseCase,
        sendCustomMessageUseCase = sendCustomMessageUseCase,
        editMessageUseCase = editMessageUseCase,
        enableListeners = false
    ) {
        override fun startTyping() {
            // No-op: avoids CometChat.startTyping() which requires CometChat.init()
        }

        override fun endTyping() {
            // No-op: avoids CometChat.endTyping() which requires CometChat.init()
        }
    }

    private fun createViewModel(): CometChatMessageComposerViewModel {
        val repository = MessageComposerRepositoryImpl(createFakeDataSource())
        return TestMessageComposerViewModel(
            sendTextMessageUseCase = SendTextMessageUseCase(repository),
            sendMediaMessageUseCase = SendMediaMessageUseCase(repository),
            sendCustomMessageUseCase = SendCustomMessageUseCase(repository),
            editMessageUseCase = EditMessageUseCase(repository)
        )
    }

    // ==================== Basic Rendering ====================

    @Test
    fun composer_displaysPlaceholderText() {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        viewModel.setUser(mockUser)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = mockUser,
                    viewModel = viewModel,
                    placeholderText = "Type a message..."
                )
            }
        }

        composeTestRule.onNodeWithText("Type a message...").assertIsDisplayed()
    }

    @Test
    fun composer_displaysAttachmentButton() {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        viewModel.setUser(mockUser)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = mockUser,
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Open attachments").assertIsDisplayed()
    }

    @Test
    fun composer_hideAttachmentButton_removesFromUI() {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        viewModel.setUser(mockUser)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = mockUser,
                    viewModel = viewModel,
                    hideAttachmentButton = true
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Open attachments").assertDoesNotExist()
    }

    @Test
    fun composer_hideVoiceRecordingButton_removesFromUI() {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        viewModel.setUser(mockUser)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = mockUser,
                    viewModel = viewModel,
                    hideVoiceRecordingButton = true
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Voice Recording").assertDoesNotExist()
    }

    @Test
    fun composer_hideSendButton_removesFromUI() {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        viewModel.setUser(mockUser)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = mockUser,
                    viewModel = viewModel,
                    hideSendButton = true
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Send message").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Send button disabled").assertDoesNotExist()
    }

    // ==================== Edit Mode ====================

    @Test
    fun composer_editMode_displaysEditPreview() {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        viewModel.setUser(mockUser)

        val editMessage = mock<TextMessage>()
        whenever(editMessage.id).thenReturn(101)
        whenever(editMessage.text).thenReturn("Message to edit")
        viewModel.setEditMessage(editMessage)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = mockUser,
                    viewModel = viewModel
                )
            }
        }

        // Edit preview should be visible
        composeTestRule.onNodeWithContentDescription("Edit Preview").assertIsDisplayed()
    }

    // ==================== Reply Mode ====================

    @Test
    fun composer_replyMode_displaysReplyPreview() {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        viewModel.setUser(mockUser)

        val replyMessage = mock<BaseMessage>()
        whenever(replyMessage.id).thenReturn(202)
        viewModel.setReplyMessage(replyMessage)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = mockUser,
                    viewModel = viewModel,
                    replyPreviewView = { _, _ ->
                        Box(modifier = Modifier.semantics { contentDescription = "Reply Preview" }) {
                            Text("Replying")
                        }
                    }
                )
            }
        }

        // Reply preview should be visible
        composeTestRule.onNodeWithContentDescription("Reply Preview").assertIsDisplayed()
    }

    // ==================== Custom Placeholder ====================

    @Test
    fun composer_customPlaceholder_displaysCorrectText() {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        viewModel.setUser(mockUser)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = mockUser,
                    viewModel = viewModel,
                    placeholderText = "Write something..."
                )
            }
        }

        composeTestRule.onNodeWithText("Write something...").assertIsDisplayed()
    }

    // ==================== Theming ====================

    @Test
    fun composer_rendersWithCometChatTheme() {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        viewModel.setUser(mockUser)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = mockUser,
                    viewModel = viewModel
                )
            }
        }

        // Composer should render without crash with CometChatTheme
        composeTestRule.onNodeWithContentDescription("Open attachments").assertIsDisplayed()
    }
}
