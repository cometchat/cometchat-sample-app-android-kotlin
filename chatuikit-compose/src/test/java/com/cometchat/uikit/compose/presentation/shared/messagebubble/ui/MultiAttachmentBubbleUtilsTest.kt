package com.cometchat.uikit.compose.presentation.shared.messagebubble.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.MediaMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.formatter.MarkdownRenderer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ENG-36737 — Robolectric tests for the Compose multi-attachment bubble helpers in
 * [MultiAttachmentBubbleUtils]: attachment resolution, MIME kind predicates, voice-note
 * detection, `batchId` grouping metadata, thumbnail-extension URL handling, and markdown
 * caption rendering into an [androidx.compose.ui.text.AnnotatedString].
 *
 * The test lives in the same package as the utils so it can exercise the `internal` API the
 * bubbles (CometChatImagesBubble / VideosBubble / AudiosBubble / FilesBubble) consume.
 *
 * Run: ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*MultiAttachmentBubbleUtilsTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MultiAttachmentBubbleUtilsTest {

    private fun attachment(mime: String?, name: String = "f") = Attachment().apply {
        fileMimeType = mime
        fileName = name
        fileUrl = "https://cdn.example.com/$name"
    }

    // ==================== resolveAttachments ====================

    @Test
    fun `resolveAttachments prefers the SDK-parsed attachments list`() {
        val message = MediaMessage().apply {
            // Single attachment first — the SDK's setAttachment appends into the attachments list,
            // so it must run before the list is replaced.
            attachment = attachment("image/gif", "single.gif")
            attachments = listOf(attachment("image/jpeg", "a.jpg"), attachment("video/mp4", "b.mp4"))
        }

        assertEquals(listOf("a.jpg", "b.mp4"), resolveAttachments(message).map { it.fileName })
    }

    @Test
    fun `resolveAttachments falls back to legacy metadata attachments array`() {
        val message = MediaMessage().apply {
            metadata = JSONObject().put(
                "attachments",
                JSONArray().put(
                    JSONObject()
                        .put("url", "https://cdn.example.com/x.jpg")
                        .put("fileName", "x.jpg")
                        .put("extension", "jpg")
                        .put("mimeType", "image/jpeg")
                        .put("size", 1234L)
                )
            )
        }

        val resolved = resolveAttachments(message)

        assertEquals(1, resolved.size)
        assertEquals("x.jpg", resolved[0].fileName)
        assertEquals("image/jpeg", resolved[0].fileMimeType)
        assertEquals(1234, resolved[0].fileSize)
    }

    @Test
    fun `resolveAttachments falls back to the single legacy attachment, else empty`() {
        val single = MediaMessage().apply { attachment = attachment("image/jpeg", "solo.jpg") }
        assertEquals(listOf("solo.jpg"), resolveAttachments(single).map { it.fileName })
        assertTrue(resolveAttachments(MediaMessage()).isEmpty())
    }

    // ==================== kind predicates ====================

    @Test
    fun `kind predicates classify by MIME with file as the catch-all`() {
        assertTrue(attachment("image/jpeg").isImage())
        assertTrue(attachment("IMAGE/PNG").isImage())
        assertTrue(attachment("video/mp4").isVideo())
        assertTrue(attachment("audio/mpeg").isAudio())
        assertTrue(attachment("application/pdf").isFileKind())
        assertTrue(attachment(null).isFileKind())
        assertFalse(attachment("application/pdf").isImage())
    }

    // ==================== isVoiceNote ====================

    @Test
    fun `isVoiceNote reads the audioType contract key with legacy voiceNote fallback`() {
        val contract = MediaMessage().apply {
            metadata = JSONObject().put(
                UIKitConstants.JSONKeys.AUDIO_TYPE, UIKitConstants.JSONKeys.AUDIO_TYPE_VOICE_NOTE
            )
        }
        val legacy = MediaMessage().apply {
            metadata = JSONObject().put(UIKitConstants.JSONKeys.VOICE_NOTE, true)
        }
        val pickerAudio = MediaMessage().apply {
            metadata = JSONObject().put(UIKitConstants.JSONKeys.AUDIO_TYPE, "music")
        }

        assertTrue(contract.isVoiceNote())
        assertTrue(legacy.isVoiceNote())
        assertFalse(pickerAudio.isVoiceNote())
        assertFalse(MediaMessage().isVoiceNote()) // no metadata
    }

    // ==================== batchId ====================

    @Test
    fun `batchId returns the shared batch id used for list-adjacency grouping`() {
        val batched = MediaMessage().apply {
            metadata = JSONObject().put(UIKitConstants.JSONKeys.BATCH_ID, "batch-123")
        }
        assertEquals("batch-123", batched.batchId())
    }

    @Test
    fun `batchId is null for unbatched messages and blank values`() {
        assertNull(MediaMessage().batchId()) // no metadata
        val blank = MediaMessage().apply {
            metadata = JSONObject().put(UIKitConstants.JSONKeys.BATCH_ID, "")
        }
        assertNull(blank.batchId())
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
    fun `thumbnailUrl reads url_medium and strips the fat token`() {
        assertEquals(
            "https://thumbs.example.com/m.jpg?Signature=abc",
            messageWithThumbnail("https://thumbs.example.com/m.jpg?Signature=abc").thumbnailUrl()
        )
        assertEquals(
            "https://thumbs.example.com/m.jpg?Signature=abc",
            messageWithThumbnail("https://thumbs.example.com/m.jpg?Signature=abc&fat=TOKEN").thumbnailUrl()
        )
        assertEquals(
            "https://thumbs.example.com/m.jpg",
            messageWithThumbnail("https://thumbs.example.com/m.jpg?fat=TOKEN").thumbnailUrl()
        )
    }

    @Test
    fun `thumbnailUrl is null when the extension metadata is absent or empty`() {
        assertNull(MediaMessage().thumbnailUrl())
        assertNull(messageWithThumbnail("").thumbnailUrl())
    }

    // ==================== buildCaptionAnnotatedString ====================

    @Test
    fun `caption markdown is parsed with markers stripped and a bold span applied`() {
        val annotated = buildCaptionAnnotatedString("**bold** caption")

        assertEquals("bold caption", annotated.text)
        assertTrue(
            annotated.spanStyles.any {
                it.item.fontWeight == FontWeight.Bold && annotated.text.substring(it.start, it.end) == "bold"
            }
        )
    }

    @Test
    fun `multi-segment captions join with newlines and keep bullet prefixes`() {
        val annotated = buildCaptionAnnotatedString("line one\n- item")

        assertTrue(annotated.text.contains("line one"))
        assertTrue(annotated.text.contains("\n"))
        assertTrue(annotated.text.contains("• item"))
    }

    // ==================== formatter span overlay ====================

    /**
     * The formatter (mentions) runs before markdown, so its spans are indexed against text that
     * still carries the `**` markers. Stripping them shifts the mention left — the overlay has to
     * follow it.
     */
    @Test
    fun `formatter spans are remapped past stripped markdown markers`() {
        val formatted = buildAnnotatedString {
            append("**hi** @Alice")
            addStyle(SpanStyle(color = Color.Red), 7, 13) // "@Alice" in the un-stripped text
        }

        val annotated = buildCaptionAnnotatedString(formatted.text, formatted)

        assertEquals("hi @Alice", annotated.text)
        val mention = annotated.spanStyles.single { it.item.color == Color.Red }
        assertEquals("@Alice", annotated.text.substring(mention.start, mention.end))
    }

    @Test
    fun `formatter spans land on the right line of a multi-segment caption`() {
        val formatted = buildAnnotatedString {
            append("intro\n- ping @Bob")
            addStyle(SpanStyle(color = Color.Red), 13, 17) // "@Bob" on the bullet line
        }

        val annotated = buildCaptionAnnotatedString(formatted.text, formatted)

        val mention = annotated.spanStyles.single { it.item.color == Color.Red }
        assertEquals("@Bob", annotated.text.substring(mention.start, mention.end))
    }

    @Test
    fun `caption without formatter output still renders markdown`() {
        val annotated = buildCaptionAnnotatedString("_italic_", null)

        assertEquals("italic", annotated.text)
    }

    // ==================== mergeBlockquotes ====================

    /** Consecutive quote lines collapse into one segment so the block renderer draws one bar. */
    @Test
    fun `consecutive blockquote segments merge into one`() {
        val parsed = MarkdownRenderer.parse("> line one\n> line two")
        val merged = mergeBlockquotes(parsed)

        val quotes = merged.filterIsInstance<MarkdownRenderer.RenderedSegment.Blockquote>()
        assertEquals(1, quotes.size)
        assertTrue(quotes.single().text.contains("line one"))
        assertTrue(quotes.single().text.contains("line two"))
    }

    @Test
    fun `non-blockquote segments are left untouched by merge`() {
        val parsed = MarkdownRenderer.parse("para\n- item")
        val merged = mergeBlockquotes(parsed)

        assertEquals(parsed.size, merged.size)
    }
}
