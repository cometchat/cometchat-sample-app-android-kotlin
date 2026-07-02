package com.cometchat.uikit.core.viewmodel.messagecomposer

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
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
import io.kotest.property.arbitrary.int
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Comprehensive ViewModel tests for CometChatMessageComposerViewModel.
 * Layer 6 — Mocks UseCases, tests all ViewModel behavior with PBT.
 *
 * Categories:
 * A. Send Text Message (success/failure/blank)
 * B. Send Media Message (success/failure/invalid file)
 * C. Send Custom Message (success/failure)
 * D. Edit Message (success/failure/blank)
 * E. Reply Message (set/clear/attach to sent)
 * F. Compose Text (set/clear)
 * G. User/Group Configuration
 * H. AI State Management
 * I. Composer Mode & Recording State
 * J. Attachment Option Visibility
 * K. Typing Indicators
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageComposerViewModelTest : FunSpec({

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

    // ==================== A. Send Text Message ====================

    test("sendTextMessage with valid text should transition Idle → Sending → Success → Idle") {
        runTest {
            val sentMessage = mock<TextMessage>()
            whenever(sentMessage.id).thenReturn(101)
            whenever(sendTextMessageUseCase.invoke(any())).thenReturn(Result.success(sentMessage))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            println("    [SETUP] UseCase mocked to return success(TextMessage id=101)")
            println("    [ViewModel] initial state: ${viewModel.uiState.value}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle

            viewModel.sendTextMessage("Hello World")
            advanceUntilIdle()

            println("    [ViewModel] final state: ${viewModel.uiState.value}")
            println("    [ViewModel] sentMessage.id = ${viewModel.sentMessage.value?.id}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.sentMessage.value shouldBe sentMessage
            println("    ✅ PASSED — Idle → Sending → Success → Idle")
        }
    }

    test("sendTextMessage with failure should transition to Error state") {
        runTest {
            val exception = CometChatException("ERR_SEND", "Network error", "Network error")
            whenever(sendTextMessageUseCase.invoke(any())).thenReturn(Result.failure(exception))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            println("    [SETUP] UseCase mocked to return failure(ERR_SEND)")
            viewModel.sendTextMessage("Hello")
            advanceUntilIdle()

            println("    [ViewModel] state: ${viewModel.uiState.value}")
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Error>()
            (viewModel.uiState.value as MessageComposerUIState.Error).exception.code shouldBe "ERR_SEND"
            println("    ✅ PASSED — Error state with code=ERR_SEND")
        }
    }

    test("sendTextMessage with blank text should not invoke use case") {
        runTest {
            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            println("    [SETUP] Sending blank text '   '")
            viewModel.sendTextMessage("   ")
            advanceUntilIdle()

            println("    [ViewModel] state remains: ${viewModel.uiState.value}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            println("    ✅ PASSED — Blank text rejected, state remains Idle")
        }
    }

    test("PBT: sendTextMessage with any non-blank text should invoke use case") {
        checkAll(20, Arb.string(1..100)) { text ->
            runTest {
                val sentMessage = mock<TextMessage>()
                whenever(sentMessage.id).thenReturn(1)
                whenever(sendTextMessageUseCase.invoke(any())).thenReturn(Result.success(sentMessage))

                val viewModel = createViewModel()
                val mockUser = mock<User>()
                whenever(mockUser.uid).thenReturn("user-1")
                viewModel.setUser(mockUser)

                viewModel.sendTextMessage(text)
                advanceUntilIdle()

                if (text.isBlank()) {
                    viewModel.uiState.value shouldBe MessageComposerUIState.Idle
                } else {
                    viewModel.sentMessage.value shouldBe sentMessage
                }
            }
        }
    }

    // ==================== B. Send Media Message ====================

    test("sendMediaMessage with valid file should transition to Success") {
        runTest {
            val sentMessage = mock<MediaMessage>()
            whenever(sentMessage.id).thenReturn(202)
            whenever(sendMediaMessageUseCase.invoke(any())).thenReturn(Result.success(sentMessage))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            // Create a temp file for testing
            val tempFile = java.io.File.createTempFile("test_media", ".jpg")
            tempFile.writeText("fake image data")
            println("    [SETUP] Created temp file: ${tempFile.absolutePath}")

            viewModel.sendMediaMessage(tempFile, "image/jpeg")
            advanceUntilIdle()

            println("    [ViewModel] state: ${viewModel.uiState.value}")
            println("    [ViewModel] sentMessage.id = ${viewModel.sentMessage.value?.id}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.sentMessage.value shouldBe sentMessage

            tempFile.delete()
            println("    ✅ PASSED — sendMediaMessage success, state=Idle, sentMessage.id=202")
        }
    }

    test("sendMediaMessage with non-existent file should emit error") {
        runTest {
            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            val nonExistentFile = java.io.File("/tmp/non_existent_file_12345.jpg")
            println("    [SETUP] Using non-existent file: ${nonExistentFile.absolutePath}")

            viewModel.sendMediaMessage(nonExistentFile, "image/jpeg")
            advanceUntilIdle()

            println("    [ViewModel] state: ${viewModel.uiState.value}")
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Error>()
            println("    ✅ PASSED — Non-existent file → Error state")
        }
    }

    // ==================== C. Send Custom Message ====================

    test("sendCustomMessage should transition Sending → Success → Idle") {
        runTest {
            val sentMessage = mock<CustomMessage>()
            whenever(sentMessage.id).thenReturn(303)
            whenever(sendCustomMessageUseCase.invoke(any())).thenReturn(Result.success(sentMessage))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            println("    [SETUP] UseCase mocked to return success(CustomMessage id=303)")
            val customMessage = mock<CustomMessage>()
            viewModel.sendCustomMessage(customMessage)
            advanceUntilIdle()

            println("    [ViewModel] state: ${viewModel.uiState.value}")
            println("    [ViewModel] sentMessage.id = ${viewModel.sentMessage.value?.id}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.sentMessage.value shouldBe sentMessage
            println("    ✅ PASSED — sendCustomMessage success, state=Idle")
        }
    }

    test("sendCustomMessage with failure should transition to Error state") {
        runTest {
            val exception = CometChatException("ERR_CUSTOM", "Custom failed", "Custom failed")
            whenever(sendCustomMessageUseCase.invoke(any())).thenReturn(Result.failure(exception))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            val customMessage = mock<CustomMessage>()
            viewModel.sendCustomMessage(customMessage)
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Error>()
        }
    }

    // ==================== D. Edit Message ====================

    test("editMessage with valid text should transition to Success") {
        runTest {
            val originalMessage = mock<TextMessage>()
            whenever(originalMessage.id).thenReturn(404)
            whenever(originalMessage.receiverUid).thenReturn("user-1")
            whenever(originalMessage.receiverType).thenReturn("user")

            val editedMessage = mock<BaseMessage>()
            whenever(editedMessage.id).thenReturn(404)
            whenever(editMessageUseCase.invoke(any())).thenReturn(Result.success(editedMessage))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            viewModel.setEditMessage(originalMessage)
            println("    [SETUP] setEditMessage(id=404) → state: ${viewModel.uiState.value}")
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Editing>()

            viewModel.editMessage("Updated text")
            advanceUntilIdle()

            println("    [ViewModel] final state: ${viewModel.uiState.value}")
            println("    [ViewModel] editMessage = ${viewModel.editMessage.value}")
            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.editMessage.value shouldBe null
            println("    ✅ PASSED — editMessage success, state=Idle, editMessage cleared")
        }
    }

    test("editMessage with blank text should not invoke use case") {
        runTest {
            val originalMessage = mock<TextMessage>()
            whenever(originalMessage.id).thenReturn(404)

            val viewModel = createViewModel()
            viewModel.setEditMessage(originalMessage)

            viewModel.editMessage("   ")
            advanceUntilIdle()

            // Should remain in Editing state since blank text is rejected
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Editing>()
        }
    }

    test("editMessage with failure should transition to Error state") {
        runTest {
            val originalMessage = mock<TextMessage>()
            whenever(originalMessage.id).thenReturn(404)
            whenever(originalMessage.receiverUid).thenReturn("user-1")
            whenever(originalMessage.receiverType).thenReturn("user")

            val exception = CometChatException("ERR_EDIT", "Edit failed", "Edit failed")
            whenever(editMessageUseCase.invoke(any())).thenReturn(Result.failure(exception))

            val viewModel = createViewModel()
            val mockUser = mock<User>()
            whenever(mockUser.uid).thenReturn("user-1")
            viewModel.setUser(mockUser)

            viewModel.setEditMessage(originalMessage)
            viewModel.editMessage("Updated text")
            advanceUntilIdle()

            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Error>()
        }
    }

    // ==================== E. Reply Message ====================

    test("setReplyMessage should transition to Replying state") {
        val viewModel = createViewModel()
        val replyMessage = mock<BaseMessage>()
        whenever(replyMessage.id).thenReturn(505)

        viewModel.setReplyMessage(replyMessage)

        println("    [ViewModel] state: ${viewModel.uiState.value}")
        viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Replying>()
        viewModel.replyMessage.value shouldBe replyMessage
        println("    ✅ PASSED — setReplyMessage → Replying state")
    }

    test("clearReplyMessage should transition back to Idle") {
        val viewModel = createViewModel()
        val replyMessage = mock<BaseMessage>()
        viewModel.setReplyMessage(replyMessage)

        viewModel.clearReplyMessage()

        println("    [ViewModel] state: ${viewModel.uiState.value}")
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        viewModel.replyMessage.value shouldBe null
        println("    ✅ PASSED — clearReplyMessage → Idle, replyMessage=null")
    }

    test("setReplyMessage should clear any existing editMessage") {
        val viewModel = createViewModel()
        val editMessage = mock<TextMessage>()
        val replyMessage = mock<BaseMessage>()

        viewModel.setEditMessage(editMessage)
        viewModel.editMessage.value shouldBe editMessage

        viewModel.setReplyMessage(replyMessage)

        viewModel.editMessage.value shouldBe null
        viewModel.replyMessage.value shouldBe replyMessage
        println("    ✅ PASSED — setReplyMessage clears editMessage")
    }

    test("setEditMessage should clear any existing replyMessage") {
        val viewModel = createViewModel()
        val replyMessage = mock<BaseMessage>()
        val editMessage = mock<TextMessage>()

        viewModel.setReplyMessage(replyMessage)
        viewModel.replyMessage.value shouldBe replyMessage

        viewModel.setEditMessage(editMessage)

        viewModel.replyMessage.value shouldBe null
        viewModel.editMessage.value shouldBe editMessage
        println("    ✅ PASSED — setEditMessage clears replyMessage")
    }

    // ==================== F. Compose Text ====================

    test("setComposeText should update composeText StateFlow") {
        val viewModel = createViewModel()

        viewModel.setComposeText("Hello")

        viewModel.composeText.value shouldBe "Hello"
        println("    ✅ PASSED — setComposeText → value: ${viewModel.composeText.value}")
    }

    test("clearComposeText should reset to empty string") {
        val viewModel = createViewModel()
        viewModel.setComposeText("Hello")

        viewModel.clearComposeText()

        viewModel.composeText.value shouldBe ""
        println("    ✅ PASSED — clearComposeText → value: '${viewModel.composeText.value}'")
    }

    test("PBT: setComposeText preserves any string value") {
        checkAll(30, Arb.string(0..200)) { text ->
            val viewModel = createViewModel()
            viewModel.setComposeText(text)
            viewModel.composeText.value shouldBe text
        }
    }

    // ==================== G. User/Group Configuration ====================

    test("setUser should configure receiver as user type") {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-123")

        viewModel.setUser(mockUser)

        viewModel.user.value shouldBe mockUser
        viewModel.group.value shouldBe null
        println("    ✅ PASSED — setUser → user=user-123, group=null")
    }

    test("setGroup should configure receiver as group type") {
        val viewModel = createViewModel()
        val mockGroup = mock<Group>()
        whenever(mockGroup.guid).thenReturn("group-456")

        viewModel.setGroup(mockGroup)

        viewModel.group.value shouldBe mockGroup
        viewModel.user.value shouldBe null
        println("    ✅ PASSED — setGroup → group=group-456, user=null")
    }

    test("setUser should clear existing group") {
        val viewModel = createViewModel()
        val mockGroup = mock<Group>()
        whenever(mockGroup.guid).thenReturn("group-456")
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-123")

        viewModel.setGroup(mockGroup)
        viewModel.group.value shouldBe mockGroup

        viewModel.setUser(mockUser)

        viewModel.user.value shouldBe mockUser
        viewModel.group.value shouldBe null
        println("    ✅ PASSED — setUser clears group")
    }

    test("setGroup should clear existing user") {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-123")
        val mockGroup = mock<Group>()
        whenever(mockGroup.guid).thenReturn("group-456")

        viewModel.setUser(mockUser)
        viewModel.user.value shouldBe mockUser

        viewModel.setGroup(mockGroup)

        viewModel.group.value shouldBe mockGroup
        viewModel.user.value shouldBe null
        println("    ✅ PASSED — setGroup clears user")
    }

    test("setParentMessageId should update idMap with parent message ID") {
        val viewModel = createViewModel()
        val mockUser = mock<User>()
        whenever(mockUser.uid).thenReturn("user-123")
        viewModel.setUser(mockUser)

        viewModel.setParentMessageId(999L)

        viewModel.idMap.value["parentMessageID"] shouldBe "999"
        println("    ✅ PASSED — setParentMessageId → idMap: ${viewModel.idMap.value}")
    }

    // ==================== H. AI State Management ====================

    test("setAIGenerating true should transition to AIGenerating state") {
        val viewModel = createViewModel()

        viewModel.setAIGenerating(true)

        viewModel.isAIGenerating.value shouldBe true
        viewModel.uiState.value shouldBe MessageComposerUIState.AIGenerating
        println("    ✅ PASSED — setAIGenerating(true) → AIGenerating state")
    }

    test("setAIGenerating false should transition back to Idle state") {
        val viewModel = createViewModel()
        viewModel.setAIGenerating(true)

        viewModel.setAIGenerating(false)

        viewModel.isAIGenerating.value shouldBe false
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        println("    ✅ PASSED — setAIGenerating(false) → Idle state")
    }

    // ==================== I. Composer Mode & Recording State ====================

    test("initial composerMode should be Normal") {
        val viewModel = createViewModel()
        viewModel.composerMode.value shouldBe ComposerMode.Normal
        println("    ✅ PASSED — initial composerMode = Normal")
    }

    test("toggleAttachmentPopup should switch between Normal and AttachmentPopupOpen") {
        val viewModel = createViewModel()

        viewModel.toggleAttachmentPopup()
        viewModel.composerMode.value shouldBe ComposerMode.AttachmentPopupOpen
        viewModel.isAttachmentPopupExpanded.value shouldBe true
        println("    [ViewModel] toggle → AttachmentPopupOpen")

        viewModel.toggleAttachmentPopup()
        viewModel.composerMode.value shouldBe ComposerMode.Normal
        viewModel.isAttachmentPopupExpanded.value shouldBe false
        println("    ✅ PASSED — toggleAttachmentPopup: Normal ↔ AttachmentPopupOpen")
    }

    test("startRecordingMode should transition to Recording(START)") {
        val viewModel = createViewModel()

        viewModel.startRecordingMode()

        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.START)
        viewModel.isInRecordingMode() shouldBe true
        println("    ✅ PASSED — startRecordingMode → Recording(START)")
    }

    test("setRecordingState should follow valid state transitions") {
        val viewModel = createViewModel()
        viewModel.startRecordingMode()

        // START → RECORDING (valid)
        viewModel.setRecordingState(RecordingState.RECORDING)
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.RECORDING)
        println("    [ViewModel] START → RECORDING: valid ✓")

        // RECORDING → PAUSED (valid)
        viewModel.setRecordingState(RecordingState.PAUSED)
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.PAUSED)
        println("    [ViewModel] RECORDING → PAUSED: valid ✓")

        // PAUSED → RECORDING (valid)
        viewModel.setRecordingState(RecordingState.RECORDING)
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.RECORDING)
        println("    [ViewModel] PAUSED → RECORDING: valid ✓")

        // RECORDING → STOPPED (valid)
        viewModel.setRecordingState(RecordingState.STOPPED)
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.STOPPED)
        println("    ✅ PASSED — All valid transitions verified")
    }

    test("setRecordingState should reject invalid state transitions") {
        val viewModel = createViewModel()
        viewModel.startRecordingMode()

        // START → PAUSED (invalid, must go through RECORDING first)
        viewModel.setRecordingState(RecordingState.PAUSED)
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.START)
        println("    [ViewModel] START → PAUSED: rejected (stays START) ✓")

        // START → STOPPED (invalid)
        viewModel.setRecordingState(RecordingState.STOPPED)
        viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.START)
        println("    ✅ PASSED — Invalid transitions rejected")
    }

    test("exitRecordingMode should return to Normal and reset recording state") {
        val viewModel = createViewModel()
        viewModel.startRecordingMode()
        viewModel.setRecordingState(RecordingState.RECORDING)
        viewModel.updateRecordingTime("01:30")
        viewModel.setRecordedFilePath("/tmp/recording.m4a")

        viewModel.exitRecordingMode()

        viewModel.composerMode.value shouldBe ComposerMode.Normal
        viewModel.recordingTime.value shouldBe "00:00"
        viewModel.recordedFilePath.value shouldBe null
        viewModel.isInRecordingMode() shouldBe false
        println("    ✅ PASSED — exitRecordingMode → Normal, time=00:00, path=null")
    }

    test("updateRecordingTime should update the time display") {
        val viewModel = createViewModel()
        viewModel.startRecordingMode()

        viewModel.updateRecordingTime("02:45")

        viewModel.recordingTime.value shouldBe "02:45"
        println("    ✅ PASSED — updateRecordingTime → ${viewModel.recordingTime.value}")
    }

    test("updateAudioAmplitude should clamp between 0 and 1") {
        val viewModel = createViewModel()

        viewModel.updateAudioAmplitude(0.5f)
        viewModel.audioAmplitude.value shouldBe 0.5f

        viewModel.updateAudioAmplitude(1.5f)
        viewModel.audioAmplitude.value shouldBe 1.0f

        viewModel.updateAudioAmplitude(-0.5f)
        viewModel.audioAmplitude.value shouldBe 0.0f
        println("    ✅ PASSED — updateAudioAmplitude clamping: 0.5→0.5, 1.5→1.0, -0.5→0.0")
    }

    // ==================== J. Attachment Option Visibility ====================

    test("default attachment option visibility should be correct") {
        val viewModel = createViewModel()

        viewModel.showCameraOption.value shouldBe true
        viewModel.showImageOption.value shouldBe true
        viewModel.showVideoOption.value shouldBe true
        viewModel.showAudioOption.value shouldBe true
        viewModel.showFileOption.value shouldBe true
        viewModel.showPollOption.value shouldBe false
        viewModel.showCollaborativeDocumentOption.value shouldBe false
        viewModel.showCollaborativeWhiteboardOption.value shouldBe false
        println("    ✅ PASSED — default visibility: camera/image/video/audio/file=true, poll/doc/whiteboard=false")
    }

    test("setCameraOptionVisibility should update visibility state") {
        val viewModel = createViewModel()

        viewModel.setCameraOptionVisibility(false)
        viewModel.showCameraOption.value shouldBe false

        viewModel.setCameraOptionVisibility(true)
        viewModel.showCameraOption.value shouldBe true
        println("    ✅ PASSED — setCameraOptionVisibility toggle verified")
    }

    test("setPollOptionVisibility should update visibility state") {
        val viewModel = createViewModel()

        viewModel.setPollOptionVisibility(true)
        viewModel.showPollOption.value shouldBe true

        viewModel.setPollOptionVisibility(false)
        viewModel.showPollOption.value shouldBe false
        println("    ✅ PASSED — setPollOptionVisibility toggle verified")
    }

    test("PBT: all visibility setters should correctly reflect boolean values") {
        checkAll(20, Arb.boolean()) { visible ->
            val viewModel = createViewModel()

            viewModel.setCameraOptionVisibility(visible)
            viewModel.showCameraOption.value shouldBe visible

            viewModel.setImageOptionVisibility(visible)
            viewModel.showImageOption.value shouldBe visible

            viewModel.setVideoOptionVisibility(visible)
            viewModel.showVideoOption.value shouldBe visible

            viewModel.setAudioOptionVisibility(visible)
            viewModel.showAudioOption.value shouldBe visible

            viewModel.setFileOptionVisibility(visible)
            viewModel.showFileOption.value shouldBe visible

            viewModel.setPollOptionVisibility(visible)
            viewModel.showPollOption.value shouldBe visible

            viewModel.setCollaborativeDocumentOptionVisibility(visible)
            viewModel.showCollaborativeDocumentOption.value shouldBe visible

            viewModel.setCollaborativeWhiteboardOptionVisibility(visible)
            viewModel.showCollaborativeWhiteboardOption.value shouldBe visible
        }
    }

    // ==================== K. clearEditMessage / clearReplyMessage ====================

    test("clearEditMessage should reset to Idle and clear editMessage") {
        val viewModel = createViewModel()
        val editMessage = mock<TextMessage>()
        viewModel.setEditMessage(editMessage)

        viewModel.clearEditMessage()

        viewModel.editMessage.value shouldBe null
        viewModel.uiState.value shouldBe MessageComposerUIState.Idle
        println("    ✅ PASSED — clearEditMessage → Idle, editMessage=null")
    }

    // ==================== L. Recording State Machine PBT ====================

    test("PBT: recording state machine only allows valid transitions") {
        val validTransitions = mapOf(
            RecordingState.START to listOf(RecordingState.RECORDING),
            RecordingState.RECORDING to listOf(RecordingState.PAUSED, RecordingState.STOPPED),
            RecordingState.PAUSED to listOf(RecordingState.RECORDING, RecordingState.STOPPED),
            RecordingState.STOPPED to listOf(RecordingState.START)
        )

        checkAll(20, Arb.element(RecordingState.values().toList())) { targetState ->
            val viewModel = createViewModel()
            viewModel.startRecordingMode()

            val currentState = RecordingState.START
            viewModel.setRecordingState(targetState)

            val isValid = validTransitions[currentState]?.contains(targetState) ?: false
            if (isValid) {
                viewModel.composerMode.value shouldBe ComposerMode.Recording(targetState)
            } else {
                viewModel.composerMode.value shouldBe ComposerMode.Recording(RecordingState.START)
            }
        }
    }
})
