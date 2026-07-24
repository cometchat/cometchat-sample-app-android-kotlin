package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment

import android.os.Looper
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
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.models.StagedAttachmentInput
import com.cometchat.uikit.core.models.defaultAttachmentCategory
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * ENG-36737 — Robolectric round-trip tests for the multi-attachment METADATA VALUES the composer
 * ViewModel writes: the shared `batchId` across a split send and the voice-note
 * `audioType = "voice_note"` stamp.
 *
 * These complement the plain-JVM core tests (`MessageComposerSendStagedAttachmentsTest`), which
 * can only assert metadata structurally because chatuikit-core unit tests run against the stubbed
 * `org.json.JSONObject`. Here org.json is real, so the tests verify the actual cross-platform
 * contract — including that what the WRITER stamps is what the READER
 * ([MultiAttachmentUtils.isVoiceNote] / batch grouping) detects.
 *
 * Run: ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*MultiAttachmentBatchMetadataTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MultiAttachmentBatchMetadataTest {

    private lateinit var cometChatMock: MockedStatic<CometChat>
    private lateinit var settingsRepoMock: MockedStatic<SettingsRepo>
    private lateinit var sendMediaMessageUseCase: RecordingSendMediaMessageUseCase
    private lateinit var viewModel: CometChatMessageComposerViewModel

    private var uploadListener: UploadFileListener? = null

    @Before
    fun setup() {
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        settingsRepoMock = Mockito.mockStatic(SettingsRepo::class.java)

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

        sendMediaMessageUseCase = RecordingSendMediaMessageUseCase()
        viewModel = CometChatMessageComposerViewModel(
            sendTextMessageUseCase = mock(),
            sendMediaMessageUseCase = sendMediaMessageUseCase,
            sendCustomMessageUseCase = mock(),
            editMessageUseCase = mock(),
            enableListeners = false
        )
        viewModel.setUser(mock<User>().also { whenever(it.uid).thenReturn("receiver-1") })
    }

    @After
    fun tearDown() {
        cometChatMock.close()
        settingsRepoMock.close()
    }

    private fun input(name: String, mime: String) = StagedAttachmentInput(
        file = File("/tmp/$name"),
        name = name,
        size = 100L,
        mimeType = mime,
        category = defaultAttachmentCategory(mime)
    )

    /** Stages [inputs], drives every tile to DONE, sends, and flushes the main looper. */
    private fun stageCompleteAndSend(inputs: List<StagedAttachmentInput>, caption: String? = null) {
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
        viewModel.sendStagedAttachments(caption)
        shadowOf(Looper.getMainLooper()).idle()
    }

    // ==================== batchId round trip ====================

    @Test
    fun `split send stamps the SAME non-blank batchId on every per-type message`() {
        stageCompleteAndSend(
            listOf(input("a.jpg", "image/jpeg"), input("clip.mp4", "video/mp4"), input("doc.pdf", "application/pdf")),
            caption = "mixed"
        )

        val sent = sendMediaMessageUseCase.sent
        assertEquals(3, sent.size)

        val batchIds = sent.map { it.metadata!!.optString(UIKitConstants.JSONKeys.BATCH_ID, "") }
        assertTrue("batchId must not be blank", batchIds.all { it.isNotBlank() })
        assertEquals("batchId must be shared across the batch", 1, batchIds.distinct().size)

        // Caption rides the LAST message — as a real string, not just structurally.
        assertNull(sent[0].caption)
        assertNull(sent[1].caption)
        assertEquals("mixed", sent[2].caption)
    }

    @Test
    fun `two different sends never share a batchId`() {
        stageCompleteAndSend(listOf(input("a.jpg", "image/jpeg"), input("doc.pdf", "application/pdf")))
        stageCompleteAndSend(listOf(input("b.jpg", "image/jpeg"), input("song.mp3", "audio/mpeg")))

        val sent = sendMediaMessageUseCase.sent
        assertEquals(4, sent.size)
        val firstBatch = sent[0].metadata!!.optString(UIKitConstants.JSONKeys.BATCH_ID)
        val secondBatch = sent[2].metadata!!.optString(UIKitConstants.JSONKeys.BATCH_ID)
        assertTrue(firstBatch.isNotBlank() && secondBatch.isNotBlank())
        assertTrue("each send must mint a fresh batchId", firstBatch != secondBatch)
    }

    @Test
    fun `single-type send carries NO batchId — it behaves like a normal message`() {
        stageCompleteAndSend(listOf(input("a.jpg", "image/jpeg"), input("b.jpg", "image/jpeg")))

        val message = sendMediaMessageUseCase.sent.single()
        assertTrue(
            "single-bucket send must not be batched",
            message.metadata == null || !message.metadata!!.has(UIKitConstants.JSONKeys.BATCH_ID)
        )
    }

    // ==================== voice-note stamp round trip ====================

    @Test
    fun `recorder send stamps audioType voice_note and the bubble-side reader detects it`() {
        val recorded = File.createTempFile("voice", ".m4a").apply {
            writeBytes(ByteArray(16) { 1 })
            deleteOnExit()
        }

        viewModel.sendMediaMessage(recorded, CometChatConstants.MESSAGE_TYPE_AUDIO, isVoiceNote = true)
        shadowOf(Looper.getMainLooper()).idle()

        val message = sendMediaMessageUseCase.sent.single()
        assertEquals(
            UIKitConstants.JSONKeys.AUDIO_TYPE_VOICE_NOTE,
            message.metadata!!.optString(UIKitConstants.JSONKeys.AUDIO_TYPE)
        )
        // Writer ↔ reader contract: the receive-side helper must route this to the voice-note bubble.
        assertTrue(MultiAttachmentUtils.isVoiceNote(message))
    }

    @Test
    fun `plain media send does NOT stamp the voice-note key`() {
        val picked = File.createTempFile("song", ".mp3").apply {
            writeBytes(ByteArray(16) { 1 })
            deleteOnExit()
        }

        viewModel.sendMediaMessage(picked, CometChatConstants.MESSAGE_TYPE_AUDIO, isVoiceNote = false)
        shadowOf(Looper.getMainLooper()).idle()

        val message = sendMediaMessageUseCase.sent.single()
        assertTrue(
            message.metadata == null || !message.metadata!!.has(UIKitConstants.JSONKeys.AUDIO_TYPE)
        )
        assertFalse(MultiAttachmentUtils.isVoiceNote(message))
    }
}

/**
 * Real subclass instead of a Mockito mock — `invoke` is a suspend fun returning the inline class
 * `kotlin.Result`, whose value-class ABI Mockito answers double-wrap (see TEST-SETUP.md
 * "Known Pitfalls When Writing Tests").
 */
private class RecordingSendMediaMessageUseCase : SendMediaMessageUseCase(mock()) {
    val sent = mutableListOf<MediaMessage>()

    override suspend fun invoke(message: MediaMessage): Result<MediaMessage> {
        sent.add(message)
        return Result.success(message)
    }
}
