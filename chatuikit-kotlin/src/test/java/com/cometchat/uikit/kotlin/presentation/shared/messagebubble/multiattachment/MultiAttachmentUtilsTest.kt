package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.multiattachment

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ENG-36737 — Robolectric tests for [MultiAttachmentUtils], the shared helpers behind the Views
 * per-type multi-attachment bubbles (Images/Videos/Files), previews, and search rows.
 *
 * Covers the receive-side contract:
 * - attachment resolution precedence (`getAttachments` → `metadata.attachments[]` → single)
 * - MIME-based kind classification
 * - voice-note detection (`audioType == "voice_note"`, legacy `voiceNote` Bool fallback)
 * - Thumbnail Generation extension `url_medium` extraction with `fat=` token stripping
 * - count labels for summarized previews
 * - file-type icon resolution (final icon set) by MIME and by extension
 * - markdown caption rendering (markers stripped, text preserved)
 *
 * Run: ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*MultiAttachmentUtilsTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MultiAttachmentUtilsTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun attachment(mime: String?, name: String = "f", url: String = "https://cdn.example.com/$name") =
        Attachment().apply {
            fileMimeType = mime
            fileName = name
            fileUrl = url
        }

    // ==================== resolveAttachments ====================

    @Test
    fun `resolveAttachments prefers the SDK-parsed attachments list`() {
        val message = MediaMessage().apply {
            // Single attachment first — the SDK's setAttachment appends into the attachments list,
            // so it must run before the list is replaced.
            attachment = attachment("image/gif", "single.gif")
            attachments = listOf(attachment("image/jpeg", "a.jpg"), attachment("image/png", "b.png"))
        }

        val resolved = MultiAttachmentUtils.resolveAttachments(message)

        assertEquals(listOf("a.jpg", "b.png"), resolved.map { it.fileName })
    }

    @Test
    fun `resolveAttachments falls back to legacy metadata attachments array`() {
        val message = MediaMessage().apply {
            metadata = JSONObject().put(
                "attachments",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("url", "https://cdn.example.com/x.jpg")
                            .put("fileName", "x.jpg")
                            .put("extension", "jpg")
                            .put("mimeType", "image/jpeg")
                            .put("size", 1234L)
                    )
                    .put(
                        JSONObject()
                            .put("url", "https://cdn.example.com/y.pdf")
                            .put("fileName", "y.pdf")
                            .put("extension", "pdf")
                            .put("mimeType", "application/pdf")
                            .put("size", 99L)
                    )
            )
        }

        val resolved = MultiAttachmentUtils.resolveAttachments(message)

        assertEquals(2, resolved.size)
        assertEquals("x.jpg", resolved[0].fileName)
        assertEquals("https://cdn.example.com/x.jpg", resolved[0].fileUrl)
        assertEquals("image/jpeg", resolved[0].fileMimeType)
        assertEquals("jpg", resolved[0].fileExtension)
        assertEquals(1234, resolved[0].fileSize)
        assertEquals("y.pdf", resolved[1].fileName)
    }

    @Test
    fun `resolveAttachments falls back to the single legacy attachment`() {
        val message = MediaMessage().apply {
            attachment = attachment("image/jpeg", "solo.jpg")
        }

        val resolved = MultiAttachmentUtils.resolveAttachments(message)

        assertEquals(listOf("solo.jpg"), resolved.map { it.fileName })
    }

    @Test
    fun `resolveAttachments returns empty when the message carries no attachments at all`() {
        assertTrue(MultiAttachmentUtils.resolveAttachments(MediaMessage()).isEmpty())
    }

    // ==================== kindOf ====================

    @Test
    fun `kindOf classifies by attachment MIME, case-insensitively, defaulting to file`() {
        assertEquals(MultiAttachmentUtils.KIND_IMAGE, MultiAttachmentUtils.kindOf(attachment("image/jpeg")))
        assertEquals(MultiAttachmentUtils.KIND_IMAGE, MultiAttachmentUtils.kindOf(attachment("IMAGE/PNG")))
        assertEquals(MultiAttachmentUtils.KIND_VIDEO, MultiAttachmentUtils.kindOf(attachment("video/mp4")))
        assertEquals(MultiAttachmentUtils.KIND_AUDIO, MultiAttachmentUtils.kindOf(attachment("audio/mpeg")))
        assertEquals(MultiAttachmentUtils.KIND_FILE, MultiAttachmentUtils.kindOf(attachment("application/pdf")))
        assertEquals(MultiAttachmentUtils.KIND_FILE, MultiAttachmentUtils.kindOf(attachment(null)))
    }

    // ==================== isVoiceNote ====================

    @Test
    fun `isVoiceNote is true for the audioType voice_note contract key`() {
        val message = MediaMessage().apply {
            metadata = JSONObject().put(
                UIKitConstants.JSONKeys.AUDIO_TYPE, UIKitConstants.JSONKeys.AUDIO_TYPE_VOICE_NOTE
            )
        }
        assertTrue(MultiAttachmentUtils.isVoiceNote(message))
    }

    @Test
    fun `isVoiceNote honours the legacy voiceNote boolean for old messages`() {
        val message = MediaMessage().apply {
            metadata = JSONObject().put(UIKitConstants.JSONKeys.VOICE_NOTE, true)
        }
        assertTrue(MultiAttachmentUtils.isVoiceNote(message))
    }

    @Test
    fun `isVoiceNote is false for picker audio and messages without metadata`() {
        assertFalse(MultiAttachmentUtils.isVoiceNote(MediaMessage())) // no metadata
        val pickerAudio = MediaMessage().apply {
            metadata = JSONObject().put(UIKitConstants.JSONKeys.AUDIO_TYPE, "music")
        }
        assertFalse(MultiAttachmentUtils.isVoiceNote(pickerAudio))
    }

    // ==================== thumbnailUrl ====================

    private fun messageWithThumbnail(urlMedium: String) = MediaMessage().apply {
        metadata = JSONObject().put(
            "@injected",
            JSONObject().put(
                "extensions",
                JSONObject().put(
                    "thumbnail-generation",
                    JSONObject().put("url_medium", urlMedium)
                )
            )
        )
    }

    @Test
    fun `thumbnailUrl reads url_medium from the thumbnail-generation extension`() {
        val message = messageWithThumbnail("https://thumbs.example.com/m.jpg?Signature=abc")
        assertEquals("https://thumbs.example.com/m.jpg?Signature=abc", MultiAttachmentUtils.thumbnailUrl(message))
    }

    @Test
    fun `thumbnailUrl strips the fat token that breaks the CloudFront signature`() {
        assertEquals(
            "https://thumbs.example.com/m.jpg?Signature=abc",
            MultiAttachmentUtils.thumbnailUrl(
                messageWithThumbnail("https://thumbs.example.com/m.jpg?Signature=abc&fat=TOKEN")
            )
        )
        assertEquals(
            "https://thumbs.example.com/m.jpg",
            MultiAttachmentUtils.thumbnailUrl(
                messageWithThumbnail("https://thumbs.example.com/m.jpg?fat=TOKEN")
            )
        )
    }

    @Test
    fun `thumbnailUrl is null when the extension metadata is absent or empty`() {
        assertNull(MultiAttachmentUtils.thumbnailUrl(MediaMessage()))
        assertNull(MultiAttachmentUtils.thumbnailUrl(messageWithThumbnail("")))
    }

    // ==================== mediaPreviewSubtitle ====================

    @Test
    fun `mediaPreviewSubtitle summarizes multiple attachments with the caption`() {
        val message = MediaMessage().apply {
            type = UIKitConstants.MessageType.IMAGE
            attachments = listOf(attachment("image/jpeg", "a.jpg"), attachment("image/png", "b.png"))
            caption = "holiday pics"
        }

        assertEquals("2 Images · holiday pics", MultiAttachmentUtils.mediaPreviewSubtitle(context, message).toString())
    }

    @Test
    fun `mediaPreviewSubtitle shows only the count label when there is no caption`() {
        val message = MediaMessage().apply {
            type = UIKitConstants.MessageType.FILE
            attachments = listOf(attachment("application/pdf", "a.pdf"), attachment("application/pdf", "b.pdf"))
        }

        assertEquals("2 Files", MultiAttachmentUtils.mediaPreviewSubtitle(context, message).toString())
    }

    @Test
    fun `mediaPreviewSubtitle prefers the caption over the file name for a single attachment`() {
        val message = MediaMessage().apply {
            type = UIKitConstants.MessageType.IMAGE
            attachment = attachment("image/jpeg", "a.jpg")
            caption = "just one"
        }

        assertEquals("just one", MultiAttachmentUtils.mediaPreviewSubtitle(context, message).toString())
    }

    @Test
    fun `mediaPreviewSubtitle falls back to the file name for a single caption-less attachment`() {
        val message = MediaMessage().apply {
            type = UIKitConstants.MessageType.FILE
            attachment = attachment("application/pdf", "report.pdf")
        }

        assertEquals("report.pdf", MultiAttachmentUtils.mediaPreviewSubtitle(context, message).toString())
    }

    // ==================== edited label ====================

    @Test
    fun `bindEditedLabel shows the label only for an edited message with a caption`() {
        val label = MultiAttachmentUtils.createEditedLabel(context)
        val message = MediaMessage().apply {
            attachment = attachment("image/jpeg", "a.jpg")
            caption = "now edited"
            editedAt = 1720000000L
        }

        MultiAttachmentUtils.bindEditedLabel(label, message, 0)

        assertEquals(android.view.View.VISIBLE, label.visibility)
    }

    @Test
    fun `bindEditedLabel stays hidden when the message is not edited or has no caption`() {
        val label = MultiAttachmentUtils.createEditedLabel(context)

        // Edited but caption-less — nothing the label could refer to.
        MultiAttachmentUtils.bindEditedLabel(label, MediaMessage().apply {
            attachment = attachment("image/jpeg", "a.jpg")
            editedAt = 1720000000L
        }, 0)
        assertEquals(android.view.View.GONE, label.visibility)

        // Caption present but never edited.
        MultiAttachmentUtils.bindEditedLabel(label, MediaMessage().apply {
            attachment = attachment("image/jpeg", "a.jpg")
            caption = "hello"
        }, 0)
        assertEquals(android.view.View.GONE, label.visibility)
    }

    // ==================== countLabel ====================

    @Test
    fun `countLabel formats the summarized preview by message type`() {
        assertEquals("3 Images", MultiAttachmentUtils.countLabel(context, UIKitConstants.MessageType.IMAGE, 3))
        assertEquals("2 Videos", MultiAttachmentUtils.countLabel(context, UIKitConstants.MessageType.VIDEO, 2))
        assertEquals("2 Audio", MultiAttachmentUtils.countLabel(context, UIKitConstants.MessageType.AUDIO, 2))
        assertEquals("5 Files", MultiAttachmentUtils.countLabel(context, UIKitConstants.MessageType.FILE, 5))
        assertEquals("4 Files", MultiAttachmentUtils.countLabel(context, null, 4)) // unknown type → Files
    }

    // ==================== fileIconRes ====================

    @Test
    fun `fileIconRes resolves the final icon set by MIME keyword`() {
        assertEquals(R.drawable.cometchat_pdf_file_icon, MultiAttachmentUtils.fileIconRes("application/pdf", null))
        assertEquals(
            R.drawable.cometchat_word_file_icon,
            MultiAttachmentUtils.fileIconRes("application/msword", null)
        )
        assertEquals(
            R.drawable.cometchat_xlsx_file_icon,
            MultiAttachmentUtils.fileIconRes("application/vnd.ms-excel", null)
        )
        assertEquals(
            R.drawable.cometchat_zip_file_icon,
            MultiAttachmentUtils.fileIconRes("application/zip", null)
        )
        assertEquals(R.drawable.cometchat_audio_file_icon, MultiAttachmentUtils.fileIconRes("audio/mpeg", null))
        assertEquals(R.drawable.cometchat_video_file_icon, MultiAttachmentUtils.fileIconRes("video/mp4", null))
        assertEquals(R.drawable.cometchat_image_file_icon, MultiAttachmentUtils.fileIconRes("image/png", null))
        assertEquals(R.drawable.cometchat_text_file_icon, MultiAttachmentUtils.fileIconRes("text/plain", null))
    }

    @Test
    fun `fileIconRes falls back to the file extension when the MIME is missing`() {
        assertEquals(
            R.drawable.cometchat_pdf_file_icon,
            MultiAttachmentUtils.fileIconRes(null, "https://cdn.example.com/report.pdf")
        )
        // An unrecognized extension on an http(s) url resolves as a link…
        assertEquals(
            R.drawable.cometchat_link_file_icon,
            MultiAttachmentUtils.fileIconRes(null, "https://cdn.example.com/data.bin")
        )
        // …while a local path (or nothing at all) falls through to unknown.
        assertEquals(
            R.drawable.cometchat_unknown_file_icon,
            MultiAttachmentUtils.fileIconRes(null, "/storage/emulated/0/data.bin")
        )
        assertEquals(R.drawable.cometchat_unknown_file_icon, MultiAttachmentUtils.fileIconRes(null, null))
    }

    // ==================== renderCaption ====================

    @Test
    fun `renderCaption parses markdown back to display text with markers stripped`() {
        val rendered = MultiAttachmentUtils.renderCaption(context, "**bold** and _italic_ caption")

        val text = rendered.toString()
        assertFalse(text.contains("**"))
        assertTrue(text.contains("bold"))
        assertTrue(text.contains("italic"))
    }
}
