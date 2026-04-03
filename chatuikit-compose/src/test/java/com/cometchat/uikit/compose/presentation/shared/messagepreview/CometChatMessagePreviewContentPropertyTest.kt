package com.cometchat.uikit.compose.presentation.shared.messagepreview

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import com.cometchat.chat.models.Attachment
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.json.JSONObject
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Property-based tests for CometChatMessagePreview content resolution logic.
 *
 * Feature: jetpack-message-bubble-parity, Property 4: Message preview content resolution by message type
 *
 * **Validates: Requirements 2.4, 2.5**
 *
 * For any quoted message, the CometChatMessagePreview subtitle content SHALL be:
 * - the text content when the message is a TextMessage
 * - the attachment filename (or media type) with an appropriate icon when the message is a MediaMessage
 * - the localized type name with an appropriate icon when the message is a CustomMessage of a known extension type
 */
class CometChatMessagePreviewContentPropertyTest : StringSpec({

    // ============================================================================
    // Simulated localized strings for known custom message types
    // ============================================================================

    val LOCALIZED_POLL = "Poll"
    val LOCALIZED_STICKER = "Sticker"
    val LOCALIZED_DOCUMENT = "Document"
    val LOCALIZED_WHITEBOARD = "Collaborative Whiteboard"
    val LOCALIZED_MEETING = "Meeting"

    /**
     * Creates a mock Android Context that returns predictable localized strings.
     */
    fun createMockContext(): Context {
        val context = mock(Context::class.java)
        `when`(context.getString(R.string.cometchat_poll)).thenReturn(LOCALIZED_POLL)
        `when`(context.getString(R.string.cometchat_message_sticker)).thenReturn(LOCALIZED_STICKER)
        `when`(context.getString(R.string.cometchat_message_document)).thenReturn(LOCALIZED_DOCUMENT)
        `when`(context.getString(R.string.cometchat_collaborative_whiteboard)).thenReturn(LOCALIZED_WHITEBOARD)
        `when`(context.getString(R.string.cometchat_meeting)).thenReturn(LOCALIZED_MEETING)
        return context
    }

    // ============================================================================
    // Arbitrary generators
    // ============================================================================

    /**
     * Generates random non-empty text content for TextMessage.
     */
    val textContentArb = Arb.string(minSize = 1, maxSize = 200)

    /**
     * Generates random filenames for MediaMessage attachments.
     */
    val fileNameArb = Arb.element(
        "photo.jpg", "video.mp4", "recording.mp3", "document.pdf",
        "image_001.png", "clip.avi", "voice_note.ogg", "report.docx",
        "screenshot.webp", "presentation.pptx"
    )

    /**
     * Generates random media types (image, video, audio, file).
     */
    val mediaTypeArb = Arb.element(
        UIKitConstants.MessageType.IMAGE,
        UIKitConstants.MessageType.VIDEO,
        UIKitConstants.MessageType.AUDIO,
        UIKitConstants.MessageType.FILE
    )

    /**
     * Generates known custom message extension types.
     */
    val knownCustomTypeArb = Arb.element(
        UIKitConstants.MessageType.EXTENSION_POLL,
        UIKitConstants.MessageType.EXTENSION_STICKER,
        UIKitConstants.MessageType.EXTENSION_DOCUMENT,
        UIKitConstants.MessageType.EXTENSION_WHITEBOARD,
        UIKitConstants.MessageType.MEETING
    )

    /**
     * Generates unknown custom message types.
     */
    val unknownCustomTypeArb = Arb.element(
        "custom_game", "custom_location", "custom_contact",
        "unknown_extension", "my_custom_type"
    )

    val defaultAlignment = UIKitConstants.MessageBubbleAlignment.LEFT

    // ============================================================================
    // Property: TextMessage → subtitle = message.text
    // ============================================================================

    // Feature: jetpack-message-bubble-parity, Property 4: Message preview content resolution by message type
    // **Validates: Requirements 2.4**
    "TextMessage content resolution: subtitle should be the text content" {
        val context = createMockContext()

        checkAll(100, textContentArb) { textContent ->
            val message = mock(TextMessage::class.java)
            `when`(message.text).thenReturn(textContent)
            `when`(message.type).thenReturn(UIKitConstants.MessageType.TEXT)

            val (subtitle, iconRes) = resolveMessageContent(
                context, message, emptyList(), defaultAlignment
            )

            subtitle.text shouldBe textContent
            iconRes shouldBe null
        }
    }

    // ============================================================================
    // Property: MediaMessage with attachment → subtitle = attachment.fileName
    // ============================================================================

    // Feature: jetpack-message-bubble-parity, Property 4: Message preview content resolution by message type
    // **Validates: Requirements 2.5**
    "MediaMessage with attachment: subtitle should be the attachment fileName" {
        val context = createMockContext()

        checkAll(100, fileNameArb, mediaTypeArb) { fileName, mediaType ->
            val attachment = mock(Attachment::class.java)
            `when`(attachment.fileName).thenReturn(fileName)

            val message = mock(MediaMessage::class.java)
            `when`(message.attachment).thenReturn(attachment)
            `when`(message.type).thenReturn(mediaType)

            val (subtitle, iconRes) = resolveMessageContent(
                context, message, emptyList(), defaultAlignment
            )

            subtitle.text shouldBe fileName
            iconRes shouldNotBe null
        }
    }

    // ============================================================================
    // Property: MediaMessage without attachment → subtitle = message.type
    // ============================================================================

    // Feature: jetpack-message-bubble-parity, Property 4: Message preview content resolution by message type
    // **Validates: Requirements 2.5**
    "MediaMessage without attachment: subtitle should fall back to message type" {
        val context = createMockContext()

        checkAll(100, mediaTypeArb) { mediaType ->
            val message = mock(MediaMessage::class.java)
            `when`(message.attachment).thenReturn(null)
            `when`(message.type).thenReturn(mediaType)

            val (subtitle, iconRes) = resolveMessageContent(
                context, message, emptyList(), defaultAlignment
            )

            subtitle.text shouldBe mediaType
            iconRes shouldNotBe null
        }
    }

    // ============================================================================
    // Property: MediaMessage icon matches media type
    // ============================================================================

    // Feature: jetpack-message-bubble-parity, Property 4: Message preview content resolution by message type
    // **Validates: Requirements 2.5**
    "MediaMessage icon should correspond to the media type" {
        val context = createMockContext()

        checkAll(100, mediaTypeArb) { mediaType ->
            val message = mock(MediaMessage::class.java)
            `when`(message.attachment).thenReturn(null)
            `when`(message.type).thenReturn(mediaType)

            val (_, iconRes) = resolveMessageContent(
                context, message, emptyList(), defaultAlignment
            )

            val expectedIcon = when (mediaType) {
                UIKitConstants.MessageType.IMAGE -> R.drawable.cometchat_ic_message_preview_image
                UIKitConstants.MessageType.VIDEO -> R.drawable.cometchat_ic_message_preview_image
                UIKitConstants.MessageType.AUDIO -> R.drawable.cometchat_ic_message_preview_audio_mic
                UIKitConstants.MessageType.FILE -> R.drawable.cometchat_ic_message_preview_document
                else -> null
            }
            iconRes shouldBe expectedIcon
        }
    }

    // ============================================================================
    // Property: Known CustomMessage types → localized subtitle + icon
    // ============================================================================

    // Feature: jetpack-message-bubble-parity, Property 4: Message preview content resolution by message type
    // **Validates: Requirements 2.5**
    "Known CustomMessage types should resolve to localized subtitle with icon" {
        val context = createMockContext()

        checkAll(100, knownCustomTypeArb) { customType ->
            val message = mock(CustomMessage::class.java)
            `when`(message.type).thenReturn(customType)

            val (subtitle, iconRes) = resolveMessageContent(
                context, message, emptyList(), defaultAlignment
            )

            val (expectedSubtitle, expectedIcon) = when (customType) {
                UIKitConstants.MessageType.EXTENSION_POLL ->
                    LOCALIZED_POLL to R.drawable.cometchat_ic_message_preview_poll
                UIKitConstants.MessageType.EXTENSION_STICKER ->
                    LOCALIZED_STICKER to R.drawable.cometchat_ic_message_preview_sticker
                UIKitConstants.MessageType.EXTENSION_DOCUMENT ->
                    LOCALIZED_DOCUMENT to R.drawable.cometchat_ic_message_preview_collaborative_document
                UIKitConstants.MessageType.EXTENSION_WHITEBOARD ->
                    LOCALIZED_WHITEBOARD to R.drawable.cometchat_ic_conversations_collabrative_document
                UIKitConstants.MessageType.MEETING ->
                    LOCALIZED_MEETING to R.drawable.cometchat_ic_message_preview_call
                else -> "" to null
            }

            subtitle.text shouldBe expectedSubtitle
            iconRes shouldBe expectedIcon
        }
    }

    // ============================================================================
    // Property: Unknown CustomMessage types → fallback subtitle, no icon
    // ============================================================================

    // Feature: jetpack-message-bubble-parity, Property 4: Message preview content resolution by message type
    // **Validates: Requirements 2.5**
    "Unknown CustomMessage types should fall back to type with no icon" {
        val context = createMockContext()

        checkAll(100, unknownCustomTypeArb) { customType ->
            val message = mock(CustomMessage::class.java)
            `when`(message.type).thenReturn(customType)
            `when`(message.conversationText).thenReturn(null)
            `when`(message.metadata).thenReturn(null)

            val (subtitle, iconRes) = resolveMessageContent(
                context, message, emptyList(), defaultAlignment
            )

            subtitle.text shouldBe customType
            iconRes shouldBe null
        }
    }

    // ============================================================================
    // Property: Unknown CustomMessage with conversationText → uses conversationText
    // ============================================================================

    // Feature: jetpack-message-bubble-parity, Property 4: Message preview content resolution by message type
    // **Validates: Requirements 2.5**
    "Unknown CustomMessage with conversationText should use it as subtitle" {
        val context = createMockContext()
        val conversationTextArb = Arb.element(
            "Shared a location", "Sent a contact", "Custom game invite"
        )

        checkAll(100, unknownCustomTypeArb, conversationTextArb) { customType, convText ->
            val message = mock(CustomMessage::class.java)
            `when`(message.type).thenReturn(customType)
            `when`(message.conversationText).thenReturn(convText)
            `when`(message.metadata).thenReturn(null)

            val (subtitle, iconRes) = resolveMessageContent(
                context, message, emptyList(), defaultAlignment
            )

            subtitle.text shouldBe convText
            iconRes shouldBe null
        }
    }

    // ============================================================================
    // Property: Other BaseMessage types → subtitle = message.type
    // ============================================================================

    // Feature: jetpack-message-bubble-parity, Property 4: Message preview content resolution by message type
    // **Validates: Requirements 2.4**
    "Other BaseMessage types should use message.type as subtitle with no icon" {
        val context = createMockContext()
        val otherTypeArb = Arb.element("action", "call", "unknown", "interactive")

        checkAll(100, otherTypeArb) { messageType ->
            val message = mock(BaseMessage::class.java)
            `when`(message.type).thenReturn(messageType)

            val (subtitle, iconRes) = resolveMessageContent(
                context, message, emptyList(), defaultAlignment
            )

            subtitle.text shouldBe messageType
            iconRes shouldBe null
        }
    }
})
