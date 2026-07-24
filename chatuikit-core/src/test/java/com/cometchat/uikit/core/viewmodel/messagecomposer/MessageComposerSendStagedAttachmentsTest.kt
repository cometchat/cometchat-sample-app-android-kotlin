package com.cometchat.uikit.core.viewmodel.messagecomposer

import android.util.Log
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UploadFileRequest
import com.cometchat.chat.core.Settings
import com.cometchat.chat.core.SettingsRepo
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.User
import com.cometchat.chat.upload.UploadFileListener
import com.cometchat.chat.upload.UploadFileItem
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.models.StagedAttachmentInput
import com.cometchat.uikit.core.models.defaultAttachmentCategory
import com.cometchat.uikit.core.state.MessageComposerUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
 * ENG-36737 — batch-send tests for [CometChatMessageComposerViewModel.sendStagedAttachments]:
 * the per-type message split (image → video → audio → file), `batchId` grouping metadata,
 * caption-on-last / reply-on-first placement, and the all-or-nothing send gate.
 *
 * Layer 2 (unit) + Layer 3 (PBT). Tiles reach DONE the same way they do in production — via the
 * captured SDK [UploadFileListener] — and sent messages are captured by
 * [RecordingSendMediaMessageUseCase], a real subclass override rather than a Mockito stub:
 * the use case is a suspend fun returning the inline class `kotlin.Result`, whose mangled
 * value-class ABI Mockito answers double-wrap.
 *
 * Note: plain-JVM unit tests get the stubbed `org.json.JSONObject` (methods return defaults), so
 * batch metadata is asserted structurally — non-null on batched messages, null on single sends —
 * not by reading back the `batchId` value. Metadata VALUE round-trips are covered by the
 * Robolectric utils tests in the kotlin/compose modules (real org.json).
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageComposerSendStagedAttachmentsTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageComposerSendStagedAttachmentsTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sendTextMessageUseCase: SendTextMessageUseCase
    lateinit var sendMediaMessageUseCase: RecordingSendMediaMessageUseCase
    lateinit var sendCustomMessageUseCase: SendCustomMessageUseCase
    lateinit var editMessageUseCase: EditMessageUseCase
    lateinit var logMock: MockedStatic<Log>
    lateinit var cometChatMock: MockedStatic<CometChat>
    lateinit var settingsRepoMock: MockedStatic<SettingsRepo>

    var uploadListener: UploadFileListener? = null

    fun createViewModel(): CometChatMessageComposerViewModel {
        val viewModel = CometChatMessageComposerViewModel(
            sendTextMessageUseCase = sendTextMessageUseCase,
            sendMediaMessageUseCase = sendMediaMessageUseCase,
            sendCustomMessageUseCase = sendCustomMessageUseCase,
            editMessageUseCase = editMessageUseCase,
            enableListeners = false
        )
        viewModel.setUser(mock<User>().also { whenever(it.uid).thenReturn("receiver-1") })
        return viewModel
    }

    fun input(name: String, mime: String, category: String? = null) = StagedAttachmentInput(
        file = File("/tmp/$name"),
        name = name,
        size = 100L,
        mimeType = mime,
        category = category ?: defaultAttachmentCategory(mime)
    )

    /** Stages [inputs] and drives every tile to DONE through the captured SDK listener. */
    fun stageAndComplete(viewModel: CometChatMessageComposerViewModel, inputs: List<StagedAttachmentInput>) {
        viewModel.stageAttachments(inputs)
        viewModel.attachmentTiles.value.forEach { tile ->
            uploadListener!!.onFileUploaded(
                tile.fileId,
                Attachment().apply {
                    fileUrl = "https://cdn.example.com/${tile.name}"
                    fileName = tile.name
                    fileMimeType = tile.mimeType
                    fileSize = 100
                }
            )
        }
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        logMock = Mockito.mockStatic(Log::class.java)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        settingsRepoMock = Mockito.mockStatic(SettingsRepo::class.java)
        sendTextMessageUseCase = mock()
        sendMediaMessageUseCase = RecordingSendMediaMessageUseCase()
        sendCustomMessageUseCase = mock()
        editMessageUseCase = mock()
        uploadListener = null

        val settings = mock<Settings>()
        whenever(settings.fileCount).thenReturn(20)
        settingsRepoMock.`when`<Settings?> { SettingsRepo.getSettings() }.thenReturn(settings)

        var batchSeq = 0
        cometChatMock.`when`<UploadFileRequest> {
            CometChat.createUploadFileRequest(any<String>(), any<String>())
        }.thenAnswer {
            val batchId = "batch-${batchSeq++}"
            mock<UploadFileRequest>().also { request ->
                whenever(request.batchId).thenReturn(batchId)
                whenever(request.setBatchId(any<String>())).thenReturn(request)
                whenever(request.setParentMessageId(any<Long>())).thenReturn(request)
                whenever(request.uploadAttachments(any<List<UploadFileItem>>(), any<UploadFileListener>()))
                    .thenAnswer { invocation ->
                        uploadListener = invocation.getArgument(1)
                        null
                    }
            }
        }
    }

    afterTest {
        Dispatchers.resetMain()
        logMock.close()
        cometChatMock.close()
        settingsRepoMock.close()
    }

    // ==================== Send gate ====================

    test("sendStagedAttachments is a no-op when nothing is staged") {
        runTest {
            val viewModel = createViewModel()

            viewModel.sendStagedAttachments("caption")
            advanceUntilIdle()

            sendMediaMessageUseCase.sent shouldHaveSize 0
        }
    }

    test("sendStagedAttachments is a no-op while any tile is not DONE (all-or-nothing gate)") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg"), input("b.jpg", "image/jpeg")))
            // Only the first finishes; the second is still uploading.
            uploadListener!!.onFileUploaded(
                "batch-0_0",
                Attachment().apply { fileName = "a.jpg"; fileMimeType = "image/jpeg" }
            )

            viewModel.sendStagedAttachments(null)
            advanceUntilIdle()

            sendMediaMessageUseCase.sent shouldHaveSize 0
            viewModel.attachmentTiles.value shouldHaveSize 2 // tray untouched
        }
    }

    // ==================== Single-type sends ====================

    test("homogeneous batch sends ONE message of that type with all attachments, no batch metadata") {
        runTest {
            val viewModel = createViewModel()
            stageAndComplete(
                viewModel,
                listOf(input("a.jpg", "image/jpeg"), input("b.png", "image/png"), input("c.gif", "image/gif"))
            )

            viewModel.sendStagedAttachments("  Trip photos  ")
            advanceUntilIdle()

            sendMediaMessageUseCase.sent shouldHaveSize 1
            val message = sendMediaMessageUseCase.sent.single()
            message.type shouldBe CometChatConstants.MESSAGE_TYPE_IMAGE
            message.attachments.map { it.fileName } shouldBe listOf("a.jpg", "b.png", "c.gif")
            message.caption shouldBe "Trip photos" // trimmed
            message.metadata.shouldBeNull()        // single bucket → no batchId
            viewModel.attachmentTiles.value shouldHaveSize 0
        }
    }

    test("a blank caption is omitted") {
        runTest {
            val viewModel = createViewModel()
            stageAndComplete(viewModel, listOf(input("a.jpg", "image/jpeg")))

            viewModel.sendStagedAttachments("   ")
            advanceUntilIdle()

            sendMediaMessageUseCase.sent.single().caption.shouldBeNull()
        }
    }

    // ==================== Mixed-type batch split ====================

    test("mixed batch splits into one message per type in image→video→audio→file order, regardless of staging order") {
        runTest {
            val viewModel = createViewModel()
            stageAndComplete(
                viewModel,
                listOf(
                    input("doc.pdf", "application/pdf"),
                    input("clip.mp4", "video/mp4"),
                    input("song.mp3", "audio/mpeg"),
                    input("a.jpg", "image/jpeg"),
                    input("b.jpg", "image/jpeg")
                )
            )

            viewModel.sendStagedAttachments("everything at once")
            advanceUntilIdle()

            val sent = sendMediaMessageUseCase.sent
            sent shouldHaveSize 4
            sent.map { it.type } shouldBe listOf(
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.MESSAGE_TYPE_VIDEO,
                CometChatConstants.MESSAGE_TYPE_AUDIO,
                CometChatConstants.MESSAGE_TYPE_FILE
            )
            sent[0].attachments.map { it.fileName } shouldBe listOf("a.jpg", "b.jpg")
            sent[1].attachments.map { it.fileName } shouldBe listOf("clip.mp4")
            sent[2].attachments.map { it.fileName } shouldBe listOf("song.mp3")
            sent[3].attachments.map { it.fileName } shouldBe listOf("doc.pdf")

            // Caption lives on the LAST message of the batch only.
            sent.dropLast(1).forEach { it.caption.shouldBeNull() }
            sent.last().caption shouldBe "everything at once"

            // Every split message carries batch metadata (the shared batchId).
            sent.forEach { it.metadata.shouldNotBeNull() }

            viewModel.attachmentTiles.value shouldHaveSize 0
        }
    }

    test("bucketing follows the picker-decided category, not the MIME type") {
        runTest {
            val viewModel = createViewModel()
            // An mp3 picked via the FILE option must be sent as a `file` message.
            stageAndComplete(
                viewModel,
                listOf(input("a.jpg", "image/jpeg"), input("song.mp3", "audio/mpeg", category = "file"))
            )

            viewModel.sendStagedAttachments(null)
            advanceUntilIdle()

            sendMediaMessageUseCase.sent.map { it.type } shouldBe listOf(
                CometChatConstants.MESSAGE_TYPE_IMAGE,
                CometChatConstants.MESSAGE_TYPE_FILE
            )
            sendMediaMessageUseCase.sent[1].attachments.single().fileName shouldBe "song.mp3"
        }
    }

    test("quoted reply rides the FIRST message of the batch only and is cleared on success") {
        runTest {
            val viewModel = createViewModel()
            val quoted = MockFactory.createTextMessage(id = 42L, text = "original")
            viewModel.setReplyMessage(quoted)
            stageAndComplete(viewModel, listOf(input("a.jpg", "image/jpeg"), input("doc.pdf", "application/pdf")))

            viewModel.sendStagedAttachments(null)
            advanceUntilIdle()

            val sent = sendMediaMessageUseCase.sent
            sent shouldHaveSize 2
            sent[0].quotedMessageId shouldBe 42L
            sent[0].quotedMessage shouldBe quoted
            sent[1].quotedMessageId shouldBe 0L
            sent[1].quotedMessage.shouldBeNull()
            viewModel.replyMessage.value.shouldBeNull()
        }
    }

    test("parentMessageId (thread) is propagated to every message of the batch") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setParentMessageId(77L)
            stageAndComplete(viewModel, listOf(input("a.jpg", "image/jpeg"), input("doc.pdf", "application/pdf")))

            viewModel.sendStagedAttachments(null)
            advanceUntilIdle()

            sendMediaMessageUseCase.sent shouldHaveSize 2
            sendMediaMessageUseCase.sent.forEach { it.parentMessageId shouldBe 77L }
        }
    }

    test("successful batch send ends Idle with sentMessage = the last split message") {
        runTest {
            val viewModel = createViewModel()
            stageAndComplete(viewModel, listOf(input("a.jpg", "image/jpeg"), input("doc.pdf", "application/pdf")))

            viewModel.sendStagedAttachments(null)
            advanceUntilIdle()

            viewModel.uiState.value shouldBe MessageComposerUIState.Idle
            viewModel.sentMessage.value shouldBe sendMediaMessageUseCase.sent.last()
        }
    }

    test("a mid-batch failure stops the remaining sends and surfaces Error state + errorEvent") {
        runTest {
            sendMediaMessageUseCase.failOnCallNumber = 2

            val viewModel = createViewModel()
            val errors = mutableListOf<CometChatException>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { errors.add(it) }
            }
            stageAndComplete(
                viewModel,
                listOf(
                    input("a.jpg", "image/jpeg"),
                    input("clip.mp4", "video/mp4"),
                    input("doc.pdf", "application/pdf")
                )
            )

            viewModel.sendStagedAttachments(null)
            advanceUntilIdle()

            sendMediaMessageUseCase.calls shouldBe 2 // image succeeded, video failed, file never attempted
            sendMediaMessageUseCase.sent shouldHaveSize 1
            viewModel.uiState.value.shouldBeInstanceOf<MessageComposerUIState.Error>()
            errors.single().code shouldBe "ERR_SEND"
            job.cancel()
        }
    }

    // ==================== PBT ====================

    test("PBT: for any category mix, one message per distinct category in fixed order, attachments partitioned intact") {
        val order = listOf(
            CometChatConstants.MESSAGE_TYPE_IMAGE,
            CometChatConstants.MESSAGE_TYPE_VIDEO,
            CometChatConstants.MESSAGE_TYPE_AUDIO,
            CometChatConstants.MESSAGE_TYPE_FILE
        )
        val mimeFor = mapOf(
            "image" to "image/jpeg", "video" to "video/mp4",
            "audio" to "audio/mpeg", "file" to "application/pdf"
        )
        checkAll(Arb.list(Arb.element(order), 1..10)) { categories ->
            runTest {
                sendMediaMessageUseCase = RecordingSendMediaMessageUseCase() // fresh recorder per iteration
                val viewModel = createViewModel()
                val inputs = categories.mapIndexed { index, category ->
                    input("f$index.bin", mimeFor.getValue(category), category = category)
                }
                stageAndComplete(viewModel, inputs)

                viewModel.sendStagedAttachments(null)
                advanceUntilIdle()

                val sent = sendMediaMessageUseCase.sent

                // One message per distinct category, in the canonical order.
                val expectedTypes = order.filter { it in categories }
                sent.map { it.type } shouldBe expectedTypes

                // No attachment is lost or duplicated across the split.
                sent.flatMap { it.attachments }.map { it.fileName }.sorted() shouldBe
                    inputs.map { it.name }.sorted()

                // Batch metadata appears exactly when the send was split.
                if (sent.size > 1) {
                    sent.forEach { it.metadata.shouldNotBeNull() }
                } else {
                    sent.single().metadata.shouldBeNull()
                }
            }
        }
    }
})

/**
 * Real subclass instead of a Mockito mock: `invoke` is a suspend fun returning the inline class
 * `kotlin.Result`, and Mockito answers (thenAnswer / doSuspendableAnswer) hand the caller a
 * double-wrapped Result through the value-class ABI. A compiled override keeps the ABI intact.
 */
private class RecordingSendMediaMessageUseCase : SendMediaMessageUseCase(mock()) {
    val sent = mutableListOf<MediaMessage>()
    var failOnCallNumber: Int? = null
    var calls = 0
        private set

    override suspend fun invoke(message: MediaMessage): Result<MediaMessage> {
        calls++
        if (calls == failOnCallNumber) {
            return Result.failure(CometChatException("ERR_SEND", "server down", "server down"))
        }
        sent.add(message)
        return Result.success(message)
    }
}
