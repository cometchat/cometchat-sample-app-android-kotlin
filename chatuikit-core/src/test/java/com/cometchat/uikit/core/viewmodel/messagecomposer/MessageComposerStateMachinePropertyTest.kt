package com.cometchat.uikit.core.viewmodel.messagecomposer

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
import android.util.Log
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
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
 * Property-based tests for MessageComposer state machine invariants.
 *
 * Tests domain invariants:
 * - Recording state machine transitions are always valid
 * - Edit/Reply mutual exclusivity is maintained
 * - Audio amplitude is always clamped [0, 1]
 * - Composer mode transitions are consistent
 * - UI state transitions follow expected patterns
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageComposerStateMachinePropertyTest : FunSpec({

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
        println("\n  🧪 ${it.name.testName}")
        println("  ─────────────────────────────────────────────────")
    }

    afterTest {
        Dispatchers.resetMain()
        logMock.close()
        println()
    }

    // ==================== Recording State Machine PBT ====================

    test("PBT: recording state machine - START only transitions to RECORDING") {
        checkAll(20, Arb.element(RecordingState.values().toList())) { targetState ->
            val viewModel = createViewModel()
            viewModel.startRecordingMode()

            viewModel.setRecordingState(targetState)

            if (targetState == RecordingState.RECORDING) {
                viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.RECORDING)
            } else {
                viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.START)
            }
        }
    }

    test("PBT: recording state machine - RECORDING transitions to PAUSED or STOPPED only") {
        checkAll(20, Arb.element(RecordingState.values().toList())) { targetState ->
            val viewModel = createViewModel()
            viewModel.startRecordingMode()
            viewModel.setRecordingState(RecordingState.RECORDING)

            viewModel.setRecordingState(targetState)

            when (targetState) {
                RecordingState.PAUSED, RecordingState.STOPPED -> {
                    viewModel.composerMode.value shouldBe ComposerMode.Recording(targetState)
                }
                else -> {
                    viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.RECORDING)
                }
            }
        }
    }

    test("PBT: recording state machine - PAUSED transitions to RECORDING or STOPPED only") {
        checkAll(20, Arb.element(RecordingState.values().toList())) { targetState ->
            val viewModel = createViewModel()
            viewModel.startRecordingMode()
            viewModel.setRecordingState(RecordingState.RECORDING)
            viewModel.setRecordingState(RecordingState.PAUSED)

            viewModel.setRecordingState(targetState)

            when (targetState) {
                RecordingState.RECORDING, RecordingState.STOPPED -> {
                    viewModel.composerMode.value shouldBe ComposerMode.Recording(targetState)
                }
                else -> {
                    viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.PAUSED)
                }
            }
        }
    }

    // ==================== Audio Amplitude Clamping PBT ====================

    test("PBT: audio amplitude is always clamped between 0.0 and 1.0") {
        checkAll(50, Arb.float(-10f..10f)) { amplitude ->
            val viewModel = createViewModel()

            viewModel.updateAudioAmplitude(amplitude)

            val result = viewModel.audioAmplitude.value
            (result >= 0f) shouldBe true
            (result <= 1f) shouldBe true

            when {
                amplitude < 0f -> result shouldBe 0f
                amplitude > 1f -> result shouldBe 1f
                else -> result shouldBe amplitude
            }
        }
    }

    // ==================== Edit/Reply Mutual Exclusivity PBT ====================

    test("PBT: setEditMessage always clears replyMessage regardless of order") {
        checkAll(20, Arb.boolean()) { setReplyFirst ->
            val viewModel = createViewModel()
            val editMessage = mock<TextMessage>()
            val replyMessage = mock<BaseMessage>()

            if (setReplyFirst) {
                viewModel.setReplyMessage(replyMessage)
                viewModel.replyMessage.value shouldBe replyMessage
            }

            viewModel.setEditMessage(editMessage)

            viewModel.editMessage.value shouldBe editMessage
            viewModel.replyMessage.value shouldBe null
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Editing>()
        }
    }

    test("PBT: setReplyMessage always clears editMessage regardless of order") {
        checkAll(20, Arb.boolean()) { setEditFirst ->
            val viewModel = createViewModel()
            val editMessage = mock<TextMessage>()
            val replyMessage = mock<BaseMessage>()

            if (setEditFirst) {
                viewModel.setEditMessage(editMessage)
                viewModel.editMessage.value shouldBe editMessage
            }

            viewModel.setReplyMessage(replyMessage)

            viewModel.replyMessage.value shouldBe replyMessage
            viewModel.editMessage.value shouldBe null
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Replying>()
        }
    }

    // ==================== Compose Text Preservation PBT ====================

    test("PBT: setComposeText preserves any arbitrary string") {
        checkAll(50, Arb.string(0..500)) { text ->
            val viewModel = createViewModel()

            viewModel.setComposeText(text)

            viewModel.composeText.value shouldBe text
        }
    }

    test("PBT: clearComposeText always results in empty string") {
        checkAll(20, Arb.string(1..100)) { text ->
            val viewModel = createViewModel()
            viewModel.setComposeText(text)

            viewModel.clearComposeText()

            viewModel.composeText.value shouldBe ""
        }
    }

    // ==================== Composer Mode Toggle PBT ====================

    test("PBT: toggleAttachmentPopup is idempotent after two toggles") {
        checkAll(10, Arb.boolean()) { _ ->
            val viewModel = createViewModel()

            viewModel.composerMode.value shouldBe ComposerMode.Normal

            viewModel.toggleAttachmentPopup()
            viewModel.composerMode.value shouldBe ComposerMode.AttachmentPopupOpen

            viewModel.toggleAttachmentPopup()
            viewModel.composerMode.value shouldBe ComposerMode.Normal
        }
    }

    test("PBT: toggleAttachmentPopup has no effect during recording mode") {
        checkAll(10, Arb.boolean()) { _ ->
            val viewModel = createViewModel()
            viewModel.startRecordingMode()

            val stateBefore = viewModel.composerMode.value
            viewModel.toggleAttachmentPopup()

            viewModel.composerMode.value shouldBe stateBefore
        }
    }

    // ==================== AI State Toggle PBT ====================

    test("PBT: setAIGenerating toggles between AIGenerating and Idle") {
        checkAll(20, Arb.boolean()) { generating ->
            val viewModel = createViewModel()

            viewModel.setAIGenerating(generating)

            if (generating) {
                viewModel.isAIGenerating.value shouldBe true
                viewModel.uiState.value shouldBe MessageComposerUIState.AIGenerating
            } else {
                viewModel.isAIGenerating.value shouldBe false
                viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            }
        }
    }
})
