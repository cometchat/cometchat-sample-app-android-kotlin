package com.cometchat.uikit.compose.presentation.messagecomposer

import android.util.Log
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.state.MessageComposerUIState
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.cometchat.uikit.core.viewmodel.ComposerMode
import com.cometchat.uikit.core.viewmodel.RecordingState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Interaction tests for CometChatMessageComposer (Compose).
 * Verifies that user interactions in the Composable produce correct ViewModel state changes.
 *
 * Simulates Compose user actions:
 * - Text field value change → composeText updates
 * - Send button click → message sent
 * - Attachment icon click → popup opens
 * - Voice recording icon click → recording mode
 * - Edit close click → edit cleared
 * - Reply close click → reply cleared
 * - AI stop click → AI generation cancelled
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageComposerInteractionTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sendTextMessageUseCase: SendTextMessageUseCase
    lateinit var sendMediaMessageUseCase: SendMediaMessageUseCase
    lateinit var sendCustomMessageUseCase: SendCustomMessageUseCase
    lateinit var editMessageUseCase: EditMessageUseCase
    lateinit var logMock: MockedStatic<Log>

    fun createViewModel(): CometChatMessageComposerViewModel {
        return CometChatMessageComposerViewModel(
            sendTextMessageUseCase = sendTextMessageUseCase,
            sendMediaMessageUseCase = sendMediaMessageUseCase,
            sendCustomMessageUseCase = sendCustomMessageUseCase,
            editMessageUseCase = editMessageUseCase,
            enableListeners = false
        )
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        logMock = Mockito.mockStatic(Log::class.java)
        sendTextMessageUseCase = mock()
        sendMediaMessageUseCase = mock()
        sendCustomMessageUseCase = mock()
        editMessageUseCase = mock()
    }

    afterTest {
        Dispatchers.resetMain()
        logMock.close()
    }

    // ==================== Text Input Interactions ====================

    test("text field value change should update composeText") {
        val viewModel = createViewModel()

        // Simulate onValueChange from BasicTextField
        viewModel.setComposeText("Compose text input")

        println("[TRACE] Compose text change → composeText: ${viewModel.composeText.value}")
        viewModel.composeText.value shouldBe "Compose text input"
    }

    test("clearing text field should reset composeText") {
        val viewModel = createViewModel()
        viewModel.setComposeText("Some text")

        viewModel.clearComposeText()

        println("[TRACE] Compose text cleared → composeText: '${viewModel.composeText.value}'")
        viewModel.composeText.value shouldBe ""
    }

    // ==================== Send Button Interactions ====================

    test("send button click with text should send message") {
        runTest {
            val sentMessage = mock<TextMessage>()
            whenever(sentMessage.id).thenReturn(101)
            whenever(sendTextMessageUseCase.invoke(any())).thenReturn(Result.success(sentMessage))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            // Simulate send button onClick
            viewModel.sendTextMessage("Hello from Compose")
            advanceUntilIdle()

            println("[TRACE] Compose send click → sentMessage: ${viewModel.sentMessage.value?.id}")
            viewModel.sentMessage.value shouldBe sentMessage
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        }
    }

    // ==================== Attachment Interactions ====================

    test("attachment icon click should open popup") {
        val viewModel = createViewModel()

        // Simulate onAttachmentClick
        viewModel.toggleAttachmentPopup()

        println("[TRACE] Compose attachment click → popup: ${viewModel.isAttachmentPopupExpanded.value}")
        viewModel.composerMode.value shouldBe ComposerMode.AttachmentPopupOpen
        viewModel.isAttachmentPopupExpanded.value shouldBe true
    }

    test("attachment icon click when popup open should close popup") {
        val viewModel = createViewModel()
        viewModel.toggleAttachmentPopup()

        // Simulate second click
        viewModel.toggleAttachmentPopup()

        println("[TRACE] Compose attachment click again → popup: ${viewModel.isAttachmentPopupExpanded.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Normal
        viewModel.isAttachmentPopupExpanded.value shouldBe false
    }

    // ==================== Voice Recording Interactions ====================

    test("voice recording icon click should enter recording mode") {
        val viewModel = createViewModel()

        // Simulate onVoiceRecordClick
        viewModel.startRecordingMode()

        println("[TRACE] Compose voice record click → mode: ${viewModel.composerMode.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.START)
    }

    test("exit recording should return to normal mode") {
        val viewModel = createViewModel()
        viewModel.startRecordingMode()
        viewModel.setRecordingState(RecordingState.RECORDING)

        // Simulate cancel/delete recording
        viewModel.exitRecordingMode()

        println("[TRACE] Compose exit recording → mode: ${viewModel.composerMode.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Normal
    }

    // ==================== Edit Mode Interactions ====================

    test("edit close button click should clear edit mode") {
        val viewModel = createViewModel()
        val editMessage = mock<TextMessage>()
        viewModel.setEditMessage(editMessage)

        // Simulate onClose click in editPreviewView
        viewModel.clearEditMessage()

        println("[TRACE] Compose edit close → state: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.editMessage.value shouldBe null
    }

    test("edit submit should send edited message") {
        runTest {
            val originalMessage = mock<TextMessage>()
            whenever(originalMessage.id).thenReturn(101)
            whenever(originalMessage.receiverUid).thenReturn("user-1")
            whenever(originalMessage.receiverType).thenReturn("user")

            val editedMessage = mock<BaseMessage>()
            whenever(editedMessage.id).thenReturn(101)
            whenever(editMessageUseCase.invoke(any())).thenReturn(Result.success(editedMessage))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)
            viewModel.setEditMessage(originalMessage)

            // Simulate send button click in edit mode
            viewModel.editMessage("Updated from Compose")
            advanceUntilIdle()

            println("[TRACE] Compose edit submit → state: ${viewModel.uiState.value}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.editMessage.value shouldBe null
        }
    }

    // ==================== Reply Mode Interactions ====================

    test("reply close button click should clear reply mode") {
        val viewModel = createViewModel()
        val replyMessage = mock<BaseMessage>()
        viewModel.setReplyMessage(replyMessage)

        // Simulate onClose click in replyPreviewView
        viewModel.clearReplyMessage()

        println("[TRACE] Compose reply close → state: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.replyMessage.value shouldBe null
    }

    // ==================== AI Interactions ====================

    test("AI stop button click should cancel generation") {
        val viewModel = createViewModel()
        viewModel.setAIGenerating(true)

        // Simulate stop button click
        viewModel.setAIGenerating(false)

        println("[TRACE] Compose AI stop → state: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.isAIGenerating.value shouldBe false
    }
})
