package com.cometchat.uikit.kotlin.presentation.messagecomposer

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
 * Interaction tests for CometChatMessageComposer (Kotlin/XML).
 * Verifies that user interactions produce correct ViewModel state changes.
 *
 * Simulates user actions:
 * - Typing text → composeText updates
 * - Clicking send → message sent, text cleared
 * - Clicking attachment → popup opens
 * - Starting voice recording → recording mode
 * - Clicking edit close → edit mode cleared
 * - Clicking reply close → reply mode cleared
 * - Toggling attachment options visibility
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

    test("user typing text should update composeText state") {
        val viewModel = createViewModel()

        // Simulate user typing character by character
        viewModel.setComposeText("H")
        viewModel.composeText.value shouldBe "H"

        viewModel.setComposeText("He")
        viewModel.composeText.value shouldBe "He"

        viewModel.setComposeText("Hello")
        viewModel.composeText.value shouldBe "Hello"

        println("[TRACE] User typing → composeText updated progressively")
    }

    test("user clearing text should reset composeText to empty") {
        val viewModel = createViewModel()
        viewModel.setComposeText("Hello World")

        viewModel.clearComposeText()

        viewModel.composeText.value shouldBe ""
        println("[TRACE] User cleared text → composeText is empty")
    }

    // ==================== Send Button Interactions ====================

    test("user clicking send with text should send message and clear text") {
        runTest {
            val sentMessage = mock<TextMessage>()
            whenever(sentMessage.id).thenReturn(101)
            whenever(sendTextMessageUseCase.invoke(any())).thenReturn(Result.success(sentMessage))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)
            viewModel.setComposeText("Hello")

            // Simulate send button click
            viewModel.sendTextMessage(viewModel.composeText.value)
            advanceUntilIdle()

            println("[TRACE] Send button clicked → message sent, state: ${viewModel.uiState.value}")
            viewModel.sentMessage.value shouldBe sentMessage
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        }
    }

    // ==================== Attachment Button Interactions ====================

    test("user clicking attachment button should open popup") {
        val viewModel = createViewModel()

        // Simulate attachment button click
        viewModel.toggleAttachmentPopup()

        println("[TRACE] Attachment button clicked → popup open: ${viewModel.isAttachmentPopupExpanded.value}")
        viewModel.composerMode.value shouldBe ComposerMode.AttachmentPopupOpen
        viewModel.isAttachmentPopupExpanded.value shouldBe true
    }

    test("user clicking attachment button again should close popup") {
        val viewModel = createViewModel()
        viewModel.toggleAttachmentPopup() // open

        // Simulate second click
        viewModel.toggleAttachmentPopup() // close

        println("[TRACE] Attachment button clicked again → popup closed")
        viewModel.composerMode.value shouldBe ComposerMode.Normal
        viewModel.isAttachmentPopupExpanded.value shouldBe false
    }

    // ==================== Voice Recording Interactions ====================

    test("user clicking voice recording button should enter recording mode") {
        val viewModel = createViewModel()

        // Simulate voice recording button click
        viewModel.startRecordingMode()

        println("[TRACE] Voice recording clicked → mode: ${viewModel.composerMode.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.START)
        viewModel.isInRecordingMode() shouldBe true
    }

    test("user clicking stop during recording should stop recording") {
        val viewModel = createViewModel()
        viewModel.startRecordingMode()
        viewModel.setRecordingState(RecordingState.RECORDING)

        // Simulate stop button click
        viewModel.setRecordingState(RecordingState.STOPPED)

        println("[TRACE] Stop clicked during recording → state: ${viewModel.composerMode.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.STOPPED)
    }

    test("user clicking delete/cancel during recording should exit recording mode") {
        val viewModel = createViewModel()
        viewModel.startRecordingMode()
        viewModel.setRecordingState(RecordingState.RECORDING)

        // Simulate delete/cancel button click
        viewModel.exitRecordingMode()

        println("[TRACE] Delete clicked during recording → mode: ${viewModel.composerMode.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Normal
        viewModel.isInRecordingMode() shouldBe false
    }

    // ==================== Edit Mode Interactions ====================

    test("user clicking close on edit preview should clear edit mode") {
        val viewModel = createViewModel()
        val editMessage = mock<TextMessage>()
        whenever(editMessage.id).thenReturn(101)
        viewModel.setEditMessage(editMessage)
        viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Editing>()

        // Simulate close button click on edit preview
        viewModel.clearEditMessage()

        println("[TRACE] Edit close clicked → state: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.editMessage.value shouldBe null
    }

    test("user submitting edit should send edited message") {
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

            // Simulate user editing text and clicking send
            viewModel.editMessage("Updated text")
            advanceUntilIdle()

            println("[TRACE] Edit submitted → state: ${viewModel.uiState.value}, editMessage: ${viewModel.editMessage.value}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.editMessage.value shouldBe null
        }
    }

    // ==================== Reply Mode Interactions ====================

    test("user clicking close on reply preview should clear reply mode") {
        val viewModel = createViewModel()
        val replyMessage = mock<BaseMessage>()
        whenever(replyMessage.id).thenReturn(202)
        viewModel.setReplyMessage(replyMessage)
        viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Replying>()

        // Simulate close button click on reply preview
        viewModel.clearReplyMessage()

        println("[TRACE] Reply close clicked → state: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.replyMessage.value shouldBe null
    }

    // ==================== AI Interactions ====================

    test("user clicking stop AI button should cancel AI generation") {
        val viewModel = createViewModel()
        viewModel.setAIGenerating(true)
        viewModel.uiState.value shouldBe MessageComposerUIState.AIGenerating

        // Simulate stop AI button click
        viewModel.setAIGenerating(false)

        println("[TRACE] Stop AI clicked → state: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.isAIGenerating.value shouldBe false
    }

    // ==================== Attachment Option Visibility Interactions ====================

    test("hiding camera option should update visibility state") {
        val viewModel = createViewModel()
        viewModel.showCameraOption.value shouldBe true

        // Simulate programmatic hide
        viewModel.setCameraOptionVisibility(false)

        println("[TRACE] Camera option hidden → visible: ${viewModel.showCameraOption.value}")
        viewModel.showCameraOption.value shouldBe false
    }

    test("enabling poll option should update visibility state") {
        val viewModel = createViewModel()
        viewModel.showPollOption.value shouldBe false

        // Simulate enabling poll extension
        viewModel.setPollOptionVisibility(true)

        println("[TRACE] Poll option enabled → visible: ${viewModel.showPollOption.value}")
        viewModel.showPollOption.value shouldBe true
    }
})
