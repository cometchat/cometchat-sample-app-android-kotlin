package com.cometchat.uikit.compose.presentation.messagecomposer

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
 * Rendering tests for CometChatMessageComposer (Compose).
 * Verifies that ViewModel states map to correct UIState for composable rendering.
 *
 * Tests the rendering logic that the Composable would use to determine what to display:
 * - Idle → show normal compose box with placeholder
 * - Editing → show edit preview composable
 * - Replying → show reply preview composable
 * - AIGenerating → show stop button composable
 * - Recording → show recording UI composable
 * - Attachment popup → show popup menu composable
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

    // ==================== Idle State ====================

    test("Idle state should render compose box with placeholder text") {
        val viewModel = createViewModel()

        println("[TRACE] Compose Rendering Idle → uiState: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.composeText.value shouldBe ""
        // Composable should show: BasicTextField with placeholder, inactive send button
    }

    test("Idle state with text should render active send button") {
        val viewModel = createViewModel()
        viewModel.setComposeText("Hello from Compose")

        println("[TRACE] Compose Rendering Idle+text → composeText: ${viewModel.composeText.value}")
        viewModel.composeText.value shouldBe "Hello from Compose"
        // Composable should show: active send button (primary color background)
    }

    // ==================== Editing State ====================

    test("Editing state should render editPreviewView composable") {
        val viewModel = createViewModel()
        val originalMessage = mock<TextMessage>()
        whenever(originalMessage.id).thenReturn(101)
        whenever(originalMessage.text).thenReturn("Original message text")

        viewModel.setEditMessage(originalMessage)

        println("[TRACE] Compose Rendering Editing → uiState: ${viewModel.uiState.value}")
        viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Editing>()
        (viewModel.uiState.value as MessageComposerUIState.Editing).message shouldBe originalMessage
        // Composable should show: edit preview with close button, original text
    }

    // ==================== Replying State ====================

    test("Replying state should render replyPreviewView composable") {
        val viewModel = createViewModel()
        val replyMessage = mock<BaseMessage>()
        whenever(replyMessage.id).thenReturn(202)

        viewModel.setReplyMessage(replyMessage)

        println("[TRACE] Compose Rendering Replying → uiState: ${viewModel.uiState.value}")
        viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Replying>()
        (viewModel.uiState.value as MessageComposerUIState.Replying).message shouldBe replyMessage
        // Composable should show: reply preview with separator, sender name, message preview
    }

    // ==================== AIGenerating State ====================

    test("AIGenerating state should render stop button instead of send") {
        val viewModel = createViewModel()

        viewModel.setAIGenerating(true)

        println("[TRACE] Compose Rendering AIGenerating → isAIGenerating: ${viewModel.isAIGenerating.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.AIGenerating
        viewModel.isAIGenerating.value shouldBe true
        // Composable should show: stop icon button instead of send button
    }

    // ==================== Recording Mode ====================

    test("Recording mode should render recording UI composable") {
        val viewModel = createViewModel()

        viewModel.startRecordingMode()
        viewModel.setRecordingState(RecordingState.RECORDING)
        viewModel.updateRecordingTime("01:30")

        println("[TRACE] Compose Rendering Recording → mode: ${viewModel.composerMode.value}, time: ${viewModel.recordingTime.value}")
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.RECORDING)
        viewModel.recordingTime.value shouldBe "01:30"
        // Composable should show: recording waveform, timer, pause/stop buttons
    }

    // ==================== Attachment Popup ====================

    test("AttachmentPopupOpen should render popup menu composable") {
        val viewModel = createViewModel()

        viewModel.toggleAttachmentPopup()

        println("[TRACE] Compose Rendering AttachmentPopup → expanded: ${viewModel.isAttachmentPopupExpanded.value}")
        viewModel.composerMode.value shouldBe ComposerMode.AttachmentPopupOpen
        viewModel.isAttachmentPopupExpanded.value shouldBe true
        // Composable should show: CometChatPopupMenu with attachment options
    }

    // ==================== Attachment Option Visibility ====================

    test("Hidden camera option should not appear in attachment options") {
        val viewModel = createViewModel()
        viewModel.setCameraOptionVisibility(false)

        println("[TRACE] Compose Rendering hidden camera → showCameraOption: ${viewModel.showCameraOption.value}")
        viewModel.showCameraOption.value shouldBe false
        // Composable should NOT render camera option in popup menu
    }

    test("Enabled poll option should appear in attachment options") {
        val viewModel = createViewModel()
        viewModel.setPollOptionVisibility(true)

        println("[TRACE] Compose Rendering poll enabled → showPollOption: ${viewModel.showPollOption.value}")
        viewModel.showPollOption.value shouldBe true
        // Composable should render poll option in popup menu
    }

    // ==================== User/Group Rendering ====================

    test("User configured should render user-specific composer") {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-1")
        whenever(mockUser.name).thenReturn("Alice")

        viewModel.setUser(mockUser)

        println("[TRACE] Compose Rendering with User → user: ${viewModel.user.value?.name}")
        viewModel.user.value shouldBe mockUser
        viewModel.group.value shouldBe null
        // Composable should render composer for 1-on-1 chat context
    }
})
