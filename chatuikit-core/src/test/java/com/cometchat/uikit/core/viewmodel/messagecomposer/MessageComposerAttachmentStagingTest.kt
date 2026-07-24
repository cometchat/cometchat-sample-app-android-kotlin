package com.cometchat.uikit.core.viewmodel.messagecomposer

import android.util.Log
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UploadFileRequest
import com.cometchat.chat.core.Settings
import com.cometchat.chat.core.SettingsRepo
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chat.upload.UploadFileListener
import com.cometchat.chat.upload.UploadFileItem
import com.cometchat.chat.upload.UploadResult
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.models.AttachmentSource
import com.cometchat.uikit.core.models.AttachmentUploadStatus
import com.cometchat.uikit.core.models.StagedAttachmentInput
import com.cometchat.uikit.core.models.defaultAttachmentCategory
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
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
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * ENG-36737 — attachment staging tests for [CometChatMessageComposerViewModel]
 * (the multi-attachment composer tray state machine).
 *
 * Layer 2 (unit) + Layer 3 (PBT). The SDK upload pipeline is intercepted at the static
 * `UploadFileRequest` seam: `CometChat.createUploadFileRequest` returns a mocked request whose
 * `uploadAttachments` stub captures the [UploadFileListener]; fileIds are app-minted by the
 * ViewModel (`batchId_seq`), so tests drive upload lifecycle events exactly the way the SDK would.
 *
 * Categories:
 * A. Staging — tile minting, order, picker-decided category, localUri fallback
 * B. Count guard — server cap, developer tighten-only override, trim + error event
 * C. Upload lifecycle — onProgress / onFileUploaded / onFileFailure / onFileError / onComplete
 * D. Remove / retry / clear — cancel semantics, retry gating, chat-switch cleanup
 * E. PBT — category derivation, count-guard invariant, all-uploaded gate
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageComposerAttachmentStagingTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageComposerAttachmentStagingTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sendTextMessageUseCase: SendTextMessageUseCase
    lateinit var sendMediaMessageUseCase: SendMediaMessageUseCase
    lateinit var sendCustomMessageUseCase: SendCustomMessageUseCase
    lateinit var editMessageUseCase: EditMessageUseCase
    lateinit var logMock: MockedStatic<Log>
    lateinit var cometChatMock: MockedStatic<CometChat>
    lateinit var settingsRepoMock: MockedStatic<SettingsRepo>
    lateinit var uploadRequestMock: UploadFileRequest

    // Captured by the uploadAttachments stub — the ViewModel's shared listener, used to
    // simulate SDK upload callbacks.
    var uploadListener: UploadFileListener? = null

    fun createViewModel(): CometChatMessageComposerViewModel = CometChatMessageComposerViewModel(
        sendTextMessageUseCase = sendTextMessageUseCase,
        sendMediaMessageUseCase = sendMediaMessageUseCase,
        sendCustomMessageUseCase = sendCustomMessageUseCase,
        editMessageUseCase = editMessageUseCase,
        enableListeners = false
    )

    fun input(
        name: String,
        mime: String,
        category: String? = null
    ) = StagedAttachmentInput(
        file = File("/tmp/$name"),
        name = name,
        size = 100L,
        mimeType = mime,
        category = category ?: defaultAttachmentCategory(mime),
        source = AttachmentSource.PICKER
    )

    fun attachmentFor(name: String, mime: String): Attachment = Attachment().apply {
        fileUrl = "https://cdn.example.com/$name"
        fileName = name
        fileMimeType = mime
        fileSize = 100
    }

    fun serverFileCount(count: Int?) {
        if (count == null) {
            settingsRepoMock.`when`<Settings?> { SettingsRepo.getSettings() }.thenReturn(null)
        } else {
            val settings = mock<Settings>()
            whenever(settings.fileCount).thenReturn(count)
            settingsRepoMock.`when`<Settings?> { SettingsRepo.getSettings() }.thenReturn(settings)
        }
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        logMock = Mockito.mockStatic(Log::class.java)
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        settingsRepoMock = Mockito.mockStatic(SettingsRepo::class.java)
        sendTextMessageUseCase = mock()
        sendMediaMessageUseCase = mock()
        sendCustomMessageUseCase = mock()
        editMessageUseCase = mock()
        uploadListener = null

        serverFileCount(null) // → DEFAULT_MAX_ATTACHMENT_COUNT (10) unless a test overrides

        uploadRequestMock = mock()
        whenever(uploadRequestMock.batchId).thenReturn("batch-1")
        whenever(uploadRequestMock.setBatchId(any<String>())).thenReturn(uploadRequestMock)
        whenever(uploadRequestMock.setParentMessageId(any<Long>())).thenReturn(uploadRequestMock)
        whenever(uploadRequestMock.uploadAttachments(any<List<UploadFileItem>>(), any<UploadFileListener>()))
            .thenAnswer { invocation ->
                uploadListener = invocation.getArgument(1)
                null
            }
        cometChatMock.`when`<UploadFileRequest> {
            CometChat.createUploadFileRequest(any<String>(), any<String>())
        }.thenReturn(uploadRequestMock)
    }

    afterTest {
        Dispatchers.resetMain()
        logMock.close()
        cometChatMock.close()
        settingsRepoMock.close()
    }

    // ==================== A. Staging ====================

    test("stageAttachments mints one UPLOADING tile per input, in order, keyed by app-minted fileIds") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(
                listOf(input("a.jpg", "image/jpeg"), input("b.mp4", "video/mp4"), input("c.pdf", "application/pdf"))
            )

            val tiles = viewModel.attachmentTiles.value
            tiles shouldHaveSize 3
            tiles.map { it.name } shouldBe listOf("a.jpg", "b.mp4", "c.pdf")
            tiles.map { it.fileId } shouldBe listOf("batch-1_0", "batch-1_1", "batch-1_2")
            tiles.all { it.status == AttachmentUploadStatus.UPLOADING } shouldBe true
            viewModel.hasStagedAttachments.value shouldBe true
        }
    }

    test("tile category is the picker-decided input category, not the MIME type") {
        runTest {
            val viewModel = createViewModel()
            // An mp3 chosen through the FILE picker must stay `file` (document tile / file message).
            viewModel.stageAttachments(listOf(input("song.mp3", "audio/mpeg", category = "file")))

            viewModel.attachmentTiles.value.single().category shouldBe "file"
        }
    }

    test("tile localUri falls back to the staged file's path when not supplied") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))

            viewModel.attachmentTiles.value.single().localUri shouldBe File("/tmp/a.jpg").absolutePath
        }
    }

    test("stageAttachments with empty input is a no-op — the SDK upload is never started") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(emptyList())

            viewModel.attachmentTiles.value shouldHaveSize 0
            cometChatMock.verify(
                { CometChat.createUploadFileRequest(any<String>(), any<String>()) },
                Mockito.never()
            )
        }
    }

    test("upload start failure surfaces on errorEvent and stages nothing") {
        runTest {
            whenever(uploadRequestMock.uploadAttachments(any<List<UploadFileItem>>(), any<UploadFileListener>()))
                .thenThrow(RuntimeException("no network"))

            val viewModel = createViewModel()
            val errors = mutableListOf<CometChatException>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { errors.add(it) }
            }

            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))
            advanceUntilIdle()

            viewModel.attachmentTiles.value shouldHaveSize 0
            errors shouldHaveSize 1
            errors.single().code shouldBe "UPLOAD_ERROR"
            job.cancel()
        }
    }

    // ==================== B. Count guard ====================

    test("staging beyond the cap trims to the remaining slots and emits MAX_ATTACHMENTS_EXCEEDED") {
        runTest {
            serverFileCount(3)
            val viewModel = createViewModel()
            val errors = mutableListOf<CometChatException>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { errors.add(it) }
            }

            viewModel.stageAttachments((1..5).map { input("img$it.jpg", "image/jpeg") })
            advanceUntilIdle()

            viewModel.attachmentTiles.value shouldHaveSize 3
            viewModel.attachmentTiles.value.map { it.name } shouldBe listOf("img1.jpg", "img2.jpg", "img3.jpg")
            errors shouldHaveSize 1
            errors.single().code shouldBe CometChatMessageComposerViewModel.ERROR_MAX_ATTACHMENTS_EXCEEDED
            job.cancel()
        }
    }

    test("staging into a full tray stages nothing, emits the error, and never calls the SDK again") {
        runTest {
            serverFileCount(2)
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg"), input("b.jpg", "image/jpeg")))

            val errors = mutableListOf<CometChatException>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { errors.add(it) }
            }

            viewModel.stageAttachments(listOf(input("c.jpg", "image/jpeg")))
            advanceUntilIdle()

            viewModel.attachmentTiles.value shouldHaveSize 2
            errors.single().code shouldBe CometChatMessageComposerViewModel.ERROR_MAX_ATTACHMENTS_EXCEEDED
            verify(uploadRequestMock, times(1))
                .uploadAttachments(any<List<UploadFileItem>>(), any<UploadFileListener>())
            job.cancel()
        }
    }

    test("maxAttachmentCount reflects the server-configured file count") {
        runTest {
            serverFileCount(5)
            createViewModel().maxAttachmentCount shouldBe 5
        }
    }

    test("missing or unavailable server settings fall back to the default cap of 10") {
        runTest {
            serverFileCount(null)
            createViewModel().maxAttachmentCount shouldBe
                CometChatMessageComposerViewModel.DEFAULT_MAX_ATTACHMENT_COUNT
        }
    }

    // ==================== C. Upload lifecycle ====================

    test("onProgress updates percent and bytes while the tile stays UPLOADING") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))

            uploadListener!!.onFileProgress("batch-1_0", 40L, 100L, 40)

            val tile = viewModel.attachmentTiles.value.single()
            tile.status shouldBe AttachmentUploadStatus.UPLOADING
            tile.percent shouldBe 40
            tile.loaded shouldBe 40L
            tile.total shouldBe 100L
        }
    }

    test("onFileUploaded marks the tile DONE at 100% and attaches the uploaded Attachment") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))

            val attachment = attachmentFor("a.jpg", "image/jpeg")
            uploadListener!!.onFileUploaded("batch-1_0", attachment)

            val tile = viewModel.attachmentTiles.value.single()
            tile.status shouldBe AttachmentUploadStatus.DONE
            tile.percent shouldBe 100
            tile.attachment shouldBe attachment
        }
    }

    test("onFileFailure marks the tile FAILED (retryable) without touching the others") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg"), input("b.jpg", "image/jpeg")))

            uploadListener!!.onFileFailure("batch-1_0", CometChatException("ERR_TRANSFER", "network reset", "network reset"))

            val tiles = viewModel.attachmentTiles.value
            tiles[0].status shouldBe AttachmentUploadStatus.FAILED
            tiles[0].error?.code shouldBe "ERR_TRANSFER"
            tiles[1].status shouldBe AttachmentUploadStatus.UPLOADING
        }
    }

    test("onFileError marks the tile REJECTED (not retryable) and surfaces the error event") {
        runTest {
            val viewModel = createViewModel()
            val errors = mutableListOf<CometChatException>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { errors.add(it) }
            }
            viewModel.stageAttachments(listOf(input("big.zip", "application/zip")))

            uploadListener!!.onFileError("batch-1_0", CometChatException("ERR_FILE_SIZE", "too large", "too large"))
            advanceUntilIdle()

            viewModel.attachmentTiles.value.single().status shouldBe AttachmentUploadStatus.REJECTED
            errors.single().code shouldBe "ERR_FILE_SIZE"
            job.cancel()
        }
    }

    test("onComplete sweeps stragglers into FAILED/REJECTED but never demotes a DONE tile") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(
                listOf(input("a.jpg", "image/jpeg"), input("b.jpg", "image/jpeg"), input("c.jpg", "image/jpeg"))
            )
            uploadListener!!.onFileUploaded("batch-1_0", attachmentFor("a.jpg", "image/jpeg"))

            val failure = UploadResult.Failure("batch-1_1", CometChatException("ERR_T", "t", "t"))
            val rejection = UploadResult.Failure("batch-1_2", CometChatException("ERR_R", "r", "r"))
            uploadListener!!.onComplete(UploadResult("batch-1", emptyList(), listOf(rejection), listOf(failure)))

            val tiles = viewModel.attachmentTiles.value
            tiles[0].status shouldBe AttachmentUploadStatus.DONE
            tiles[1].status shouldBe AttachmentUploadStatus.FAILED
            tiles[2].status shouldBe AttachmentUploadStatus.REJECTED
        }
    }

    test("attachmentsAllUploaded is the all-or-nothing send gate") {
        runTest {
            val viewModel = createViewModel()
            viewModel.attachmentsAllUploaded.value shouldBe false // empty tray never gates open

            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg"), input("b.jpg", "image/jpeg")))
            viewModel.attachmentsAllUploaded.value shouldBe false // still uploading

            uploadListener!!.onFileUploaded("batch-1_0", attachmentFor("a.jpg", "image/jpeg"))
            viewModel.attachmentsAllUploaded.value shouldBe false // one of two done

            uploadListener!!.onFileUploaded("batch-1_1", attachmentFor("b.jpg", "image/jpeg"))
            viewModel.attachmentsAllUploaded.value shouldBe true  // all done
        }
    }

    test("a callback for an unknown fileId is ignored") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))

            uploadListener!!.onFileUploaded("batch-1_999", attachmentFor("ghost.jpg", "image/jpeg"))

            viewModel.attachmentTiles.value.single().status shouldBe AttachmentUploadStatus.UPLOADING
        }
    }

    // ==================== D. Remove / retry / clear ====================

    test("removeAttachment aborts an in-flight upload before dropping the tile") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))

            viewModel.removeAttachment(viewModel.attachmentTiles.value.single())

            viewModel.attachmentTiles.value shouldHaveSize 0
            verify(uploadRequestMock).removeAttachment("batch-1_0")
        }
    }

    test("removeAttachment on a DONE tile drops it from the SDK batch too") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))
            uploadListener!!.onFileUploaded("batch-1_0", attachmentFor("a.jpg", "image/jpeg"))

            viewModel.removeAttachment(viewModel.attachmentTiles.value.single())

            viewModel.attachmentTiles.value shouldHaveSize 0
            // New SDK semantics: an uploaded file is dropped from the batch on removal.
            verify(uploadRequestMock).removeAttachment("batch-1_0")
        }
    }

    test("retryAttachment resets a FAILED tile to UPLOADING and retries via the SDK") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))
            uploadListener!!.onFileProgress("batch-1_0", 50L, 100L, 50)
            uploadListener!!.onFileFailure("batch-1_0", CometChatException("ERR_T", "t", "t"))

            viewModel.retryAttachment(viewModel.attachmentTiles.value.single())

            val tile = viewModel.attachmentTiles.value.single()
            tile.status shouldBe AttachmentUploadStatus.UPLOADING
            tile.percent shouldBe 0
            tile.loaded shouldBe 0L
            tile.error shouldBe null
            verify(uploadRequestMock).uploadAttachment(eq("batch-1_0"), any<File>(), any<UploadFileListener>())
        }
    }

    test("retryAttachment puts the tile back to FAILED when the SDK retry call throws") {
        runTest {
            doThrow(RuntimeException("still offline"))
                .whenever(uploadRequestMock).uploadAttachment(any<String>(), any<File>(), any<UploadFileListener>())

            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))
            uploadListener!!.onFileFailure("batch-1_0", CometChatException("ERR_T", "t", "t"))

            viewModel.retryAttachment(viewModel.attachmentTiles.value.single())

            val tile = viewModel.attachmentTiles.value.single()
            tile.status shouldBe AttachmentUploadStatus.FAILED
            tile.error?.code shouldBe "RETRY_ERROR"
        }
    }

    test("clearAttachments releases the SDK batch and empties the tray") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg"), input("b.jpg", "image/jpeg")))
            uploadListener!!.onFileUploaded("batch-1_1", attachmentFor("b.jpg", "image/jpeg"))

            viewModel.clearAttachments()

            viewModel.attachmentTiles.value shouldHaveSize 0
            viewModel.hasStagedAttachments.value shouldBe false
            // One clearAll() releases the whole batch (aborting the in-flight upload).
            verify(uploadRequestMock).clearAll()
        }
    }

    test("switching chats clears the staged tray; re-setting the same receiver keeps it") {
        runTest {
            val userA = mock<User>().also { whenever(it.uid).thenReturn("user-a") }
            val userB = mock<User>().also { whenever(it.uid).thenReturn("user-b") }
            val group = mock<Group>().also { whenever(it.guid).thenReturn("group-1") }

            val viewModel = createViewModel()
            viewModel.setUser(userA)
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))

            viewModel.setUser(userA) // same receiver — tray survives
            viewModel.attachmentTiles.value shouldHaveSize 1

            viewModel.setUser(userB) // different user — tray cleared
            viewModel.attachmentTiles.value shouldHaveSize 0

            viewModel.stageAttachments(listOf(input("b.jpg", "image/jpeg")))
            viewModel.setGroup(group) // user → group — tray cleared
            viewModel.attachmentTiles.value shouldHaveSize 0
        }
    }

    test("changing the thread target (parentMessageId) clears the staged tray; same target keeps it") {
        runTest {
            val viewModel = createViewModel()
            viewModel.setParentMessageId(10L)
            viewModel.stageAttachments(listOf(input("a.jpg", "image/jpeg")))
            viewModel.attachmentTiles.value shouldHaveSize 1

            viewModel.setParentMessageId(10L) // same thread — tray survives
            viewModel.attachmentTiles.value shouldHaveSize 1

            viewModel.setParentMessageId(99L) // different thread — tray cleared, SDK batch released
            viewModel.attachmentTiles.value shouldHaveSize 0
            verify(uploadRequestMock).clearAll()
        }
    }

    // ==================== E. PBT ====================

    test("PBT: defaultAttachmentCategory maps any MIME to exactly image/video/audio/file") {
        checkAll(
            Arb.element(
                "image/jpeg" to "image", "image/png" to "image", "IMAGE/GIF" to "image",
                "video/mp4" to "video", "video/quicktime" to "video",
                "audio/mpeg" to "audio", "audio/ogg" to "audio",
                "application/pdf" to "file", "text/plain" to "file", "application/zip" to "file",
                "" to "file", "garbage" to "file"
            )
        ) { (mime, expected) ->
            defaultAttachmentCategory(mime) shouldBe expected
        }
    }

    test("PBT: for any staging burst the tray never exceeds maxAttachmentCount") {
        checkAll(Arb.int(1..8), Arb.int(0..25)) { cap, requested ->
            serverFileCount(cap)
            val viewModel = createViewModel()

            viewModel.stageAttachments((1..requested).map { input("f$it.jpg", "image/jpeg") })

            viewModel.attachmentTiles.value.size shouldBe minOf(requested, cap)
        }
    }

    test("PBT: attachmentsAllUploaded is true iff the tray is non-empty and every tile is DONE") {
        checkAll(Arb.list(Arb.element(true, false), 0..8)) { doneFlags ->
            serverFileCount(20)
            val viewModel = createViewModel()
            viewModel.stageAttachments(doneFlags.indices.map { input("f$it.jpg", "image/jpeg") })

            val tiles = viewModel.attachmentTiles.value
            doneFlags.forEachIndexed { index, done ->
                if (done) uploadListener!!.onFileUploaded(
                    tiles[index].fileId, attachmentFor("f$index.jpg", "image/jpeg")
                )
            }

            viewModel.attachmentsAllUploaded.value shouldBe (doneFlags.isNotEmpty() && doneFlags.all { it })
        }
    }

    // ==================== F. ENG-37004 QA regressions ====================

    test("ENG-37010: staging exactly the max count keeps every file — the last tile is not trimmed or errored") {
        runTest {
            serverFileCount(10)
            val viewModel = createViewModel()
            val errors = mutableListOf<CometChatException>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.errorEvent.collect { errors.add(it) }
            }

            viewModel.stageAttachments((1..10).map { input("f$it.jpg", "image/jpeg") })
            advanceUntilIdle()

            // A full-but-not-over batch must stage cleanly: no trim, no count error
            viewModel.attachmentTiles.value shouldHaveSize 10
            errors shouldHaveSize 0

            // Drive every upload to success, exactly as the SDK would, then complete the group
            viewModel.attachmentTiles.value.forEachIndexed { index, tile ->
                uploadListener!!.onFileUploaded(tile.fileId, attachmentFor("f${index + 1}.jpg", "image/jpeg"))
            }
            uploadListener!!.onComplete(mock<UploadResult>())

            // The QA repro was the LAST tile flipping to an error state — every tile must be DONE
            val tiles = viewModel.attachmentTiles.value
            tiles shouldHaveSize 10
            tiles.all { it.status == AttachmentUploadStatus.DONE } shouldBe true
            tiles.last().error shouldBe null
            viewModel.attachmentsAllUploaded.value shouldBe true
            job.cancel()
        }
    }

    test("ENG-37011: the SDK size-limit rejection message survives verbatim on the REJECTED tile") {
        runTest {
            val viewModel = createViewModel()
            viewModel.stageAttachments(listOf(input("big.mp4", "video/mp4"), input("ok.jpg", "image/jpeg")))

            // Server-side rejection for an oversized file — the SDK message carries the limit
            val sdkMessage = "File size exceeds the maximum allowed limit of 100 MB"
            uploadListener!!.onFileError("batch-1_0", CometChatException("ERR_FILE_SIZE_EXCEEDED", sdkMessage, sdkMessage))
            uploadListener!!.onFileUploaded("batch-1_1", attachmentFor("ok.jpg", "image/jpeg"))

            // The UI surfaces tile.error.message verbatim when the rejected tile is tapped
            // (snackbar in both UIKits) — losing it here is what left QA with no error at all
            val rejected = viewModel.attachmentTiles.value.first()
            rejected.status shouldBe AttachmentUploadStatus.REJECTED
            rejected.error?.message shouldBe sdkMessage

            // A rejected tile blocks the send gate until it is removed
            viewModel.attachmentsAllUploaded.value shouldBe false
            viewModel.removeAttachment(rejected)
            viewModel.attachmentsAllUploaded.value shouldBe true
        }
    }
})
