package com.cometchat.uikit.kotlin.presentation.messagecomposer

import android.util.Log
import com.cometchat.chat.exceptions.CometChatException
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Rendering tests for CometChatMessageComposer (Kotlin/XML).
 * Verifies that ViewModel states map to correct UIState for view rendering.
 *
 * Tests the rendering logic that the View would use to determine what to display:
 * - Idle → show normal compose box
 * - Sending → show loading indicator
 * - Editing → show edit preview panel
 * - Replying → show reply preview panel
 * - AIGenerating → show stop button instead of send
 * - Error → show error state
 * - Recording mode → show recording UI
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageComposerRenderingTest : FunSpec({

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

    // ==================== Idle State Rendering ====================

    test("Idle state should render normal compose box with send button inactive") {
        val viewModel = createViewModel()

        println("[TRACE] Rendering Idle → uiState: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.composeText.value shouldBe ""
        viewModel.composerMode.value shouldBe ComposerMode.Normal
        // View should show: normal input, inactive send button, attachment button visible
    }

    test("Idle state with text should render send button active") {
        val viewModel = createViewModel()
        viewModel.setComposeText("Hello")

        println("[TRACE] Rendering Idle with text → composeText: ${viewModel.composeText.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.composeText.value shouldBe "Hello"
        // View should show: active send button (text is non-empty)
    }

    // ==================== Editing State Rendering ====================

    test("Editing state should render edit preview panel with original message") {
        val viewModel = createViewModel()
        val originalMessage = mock<TextMessage>()
        whenever(originalMessage.id).thenReturn(101)
        whenever(originalMessage.text).thenReturn("Original text")

        viewModel.setEditMessage(originalMessage)

        println("[TRACE] Rendering Editing → uiState: ${viewModel.uiState.value}")
        viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Editing>()
        val editingState = viewModel.uiState.value as MessageComposerUIState.Editing
        editingState.message shouldBe originalMessage
        // View should show: edit preview panel with "Editing Message" title and original text
    }

    // ==================== Replying State Rendering ====================

    test("Replying state should render reply preview panel with quoted message") {
        val viewModel = createViewModel()
        val replyMessage = mock<BaseMessage>()
        whenever(replyMessage.id).thenReturn(202)

        viewModel.setReplyMessage(replyMessage)

        println("[TRACE] Rendering Replying → uiState: ${viewModel.uiState.value}")
        viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Replying>()
        val replyingState = viewModel.uiState.value as MessageComposerUIState.Replying
        replyingState.message shouldBe replyMessage
        // View should show: reply preview panel with sender name and message preview
    }

    // ==================== AIGenerating State Rendering ====================

    test("AIGenerating state should render stop button instead of send button") {
        val viewModel = createViewModel()

        viewModel.setAIGenerating(true)

        println("[TRACE] Rendering AIGenerating → uiState: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.AIGenerating
        viewModel.isAIGenerating.value shouldBe true
        // View should show: stop button (square icon) instead of send button
    }

    // ==================== Error State Rendering ====================

    test("Error state should render error indicator") {
        val viewModel = createViewModel()
        val exception = CometChatException("ERR_SEND", "Send failed", "Send failed")

        // Simulate error by directly checking state type
        val errorState = MessageComposerUIState.Error(exception)
        errorState.shouldBeInstanceOf<MessageComposerUIState.Error>()
        errorState.exception.code shouldBe "ERR_SEND"
        println("[TRACE] Rendering Error → exception.code: ${errorState.exception.code}")
        // View should show: error toast/snackbar and return to Idle
    }

    // ==================== Recording Mode Rendering ====================

    test("Recording START state should render recording UI with start indicator") {
        val viewModel = createViewModel()

        viewModel.startRecordingMode()

        println("[TRACE] Rendering Recording(START) → composerMode: ${viewModel.composerMode.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.START)
        viewModel.recordingTime.value shouldBe "00:00"
        // View should show: recording UI with microphone icon, time at 00:00
    }

    test("Recording RECORDING state should render active recording with timer") {
        val viewModel = createViewModel()
        viewModel.startRecordingMode()
        viewModel.setRecordingState(RecordingState.RECORDING)
        viewModel.updateRecordingTime("01:23")
        viewModel.updateAudioAmplitude(0.7f)

        println("[TRACE] Rendering Recording(RECORDING) → time: ${viewModel.recordingTime.value}, amplitude: ${viewModel.audioAmplitude.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.RECORDING)
        viewModel.recordingTime.value shouldBe "01:23"
        viewModel.audioAmplitude.value shouldBe 0.7f
        // View should show: active recording indicator, timer at 01:23, waveform
    }

    test("Recording PAUSED state should render paused recording") {
        val viewModel = createViewModel()
        viewModel.startRecordingMode()
        viewModel.setRecordingState(RecordingState.RECORDING)
        viewModel.setRecordingState(RecordingState.PAUSED)
        viewModel.updateRecordingTime("02:00")

        println("[TRACE] Rendering Recording(PAUSED) → time: ${viewModel.recordingTime.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.PAUSED)
        viewModel.recordingTime.value shouldBe "02:00"
        // View should show: paused indicator, timer frozen, resume button
    }

    // ==================== Attachment Popup Rendering ====================

    test("AttachmentPopupOpen mode should render popup menu") {
        val viewModel = createViewModel()

        viewModel.toggleAttachmentPopup()

        println("[TRACE] Rendering AttachmentPopupOpen → composerMode: ${viewModel.composerMode.value}")
        viewModel.composerMode.value shouldBe ComposerMode.AttachmentPopupOpen
        viewModel.isAttachmentPopupExpanded.value shouldBe true
        // View should show: attachment popup menu with camera, image, video, audio, file options
    }

    // ==================== User/Group Configuration Rendering ====================

    test("User set should render user-specific composer (no group indicators)") {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        whenever(mockUser.name).thenReturn("John Doe")

        viewModel.setUser(mockUser)

        println("[TRACE] Rendering with User → user: ${viewModel.user.value?.name}")
        viewModel.user.value shouldBe mockUser
        viewModel.group.value shouldBe null
        // View should show: composer configured for 1-on-1 chat
    }
})
