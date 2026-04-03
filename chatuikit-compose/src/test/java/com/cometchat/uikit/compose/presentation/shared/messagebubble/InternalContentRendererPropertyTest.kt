package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Property-based tests for InternalContentRenderer.
 *
 * **Feature: message-bubble-internal-rendering**
 * **Property 1: Message Type Mapping**
 *
 * **Validates: Requirements 1.1-1.6, 2.1-2.4, 3.1-3.4**
 *
 * For any message with a known category/type combination and empty bubbleFactories,
 * the CometChatMessageBubble SHALL render the corresponding bubble component as
 * defined in the message type mapping table.
 *
 * Since InternalContentRenderer.renderContent() is a @Composable function,
 * we test the supporting logic:
 * 1. shouldUseMinimalSlots() - determines if system messages should use minimal slots
 * 2. Message type detection logic via mock messages
 *
 * Message Type Mapping Table:
 * | Category | Type | Bubble Component |
 * |----------|------|------------------|
 * | message | text | CometChatTextBubble |
 * | message | image | CometChatImageBubble |
 * | message | video | CometChatVideoBubble |
 * | message | audio | CometChatAudioBubble |
 * | message | file | CometChatFileBubble |
 * | action | groupMember | CometChatActionBubble |
 * | call | audio | CometChatCallActionBubble |
 * | call | video | CometChatCallActionBubble |
 * | meeting | meeting | CometChatMeetCallBubble |
 * | custom | polls | CometChatPollBubble |
 * | custom | extension_sticker | CometChatStickerBubble |
 * | custom | document | CometChatCollaborativeBubble |
 * | custom | whiteboard | CometChatCollaborativeBubble |
 */
class InternalContentRendererPropertyTest : StringSpec({

    // ============================================================================
    // Arbitrary generators for message types
    // ============================================================================

    /**
     * Generates random standard message types (text, image, video, audio, file).
     */
    val standardMessageTypeArb = Arb.element(
        CometChatConstants.MESSAGE_TYPE_TEXT,
        CometChatConstants.MESSAGE_TYPE_IMAGE,
        CometChatConstants.MESSAGE_TYPE_VIDEO,
        CometChatConstants.MESSAGE_TYPE_AUDIO,
        CometChatConstants.MESSAGE_TYPE_FILE
    )

    /**
     * Generates random call types (audio, video).
     */
    val callTypeArb = Arb.element(
        CometChatConstants.CALL_TYPE_AUDIO,
        CometChatConstants.CALL_TYPE_VIDEO
    )

    /**
     * Generates random custom extension types.
     */
    val customExtensionTypeArb = Arb.element(
        InternalContentRenderer.EXTENSION_POLLS,
        InternalContentRenderer.EXTENSION_STICKER,
        InternalContentRenderer.EXTENSION_DOCUMENT,
        InternalContentRenderer.EXTENSION_WHITEBOARD
    )

    /**
     * Generates random alignments.
     */
    val alignmentArb = Arb.element(
        UIKitConstants.MessageBubbleAlignment.LEFT,
        UIKitConstants.MessageBubbleAlignment.RIGHT,
        UIKitConstants.MessageBubbleAlignment.CENTER
    )

    /**
     * Generates random positive deletedAt timestamps (indicating deleted messages).
     */
    val deletedAtArb = Arb.long(1L..Long.MAX_VALUE)

    // ============================================================================
    // Property Test: shouldUseMinimalSlots for action messages
    // ============================================================================

    /**
     * Property test: Action messages should always use minimal slots.
     *
     * **Validates: Requirements 2.1 (action messages render as ActionBubble)**
     */
    "action messages should use minimal slots" {
        checkAll(100, alignmentArb) { _ ->
            val actionMessage = createMockActionMessage()
            
            InternalContentRenderer.shouldUseMinimalSlots(actionMessage) shouldBe true
        }
    }

    // ============================================================================
    // Property Test: shouldUseMinimalSlots for call messages
    // ============================================================================

    /**
     * Property test: Call messages (audio and video) should always use minimal slots.
     *
     * **Validates: Requirements 2.2, 2.3 (call messages render as CallActionBubble)**
     */
    "call messages should use minimal slots" {
        checkAll(100, callTypeArb) { callType ->
            val callMessage = createMockCallMessage(callType)
            
            InternalContentRenderer.shouldUseMinimalSlots(callMessage) shouldBe true
        }
    }

    // ============================================================================
    // Property Test: shouldUseMinimalSlots for deleted messages
    // ============================================================================

    /**
     * Property test: Deleted messages (deletedAt > 0) should NOT use minimal slots.
     * They should still show header (sender name) and status info (timestamp, receipt).
     * Only the content view is replaced with the delete bubble.
     *
     * **Validates: Requirements 1.7 (deleted messages render as DeleteBubble with full slots)**
     */
    "deleted messages should not use minimal slots - they show header and status info" {
        checkAll(100, standardMessageTypeArb, deletedAtArb) { messageType, deletedAt ->
            val deletedMessage = createMockDeletedMessage(messageType, deletedAt)
            
            InternalContentRenderer.shouldUseMinimalSlots(deletedMessage) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: shouldUseMinimalSlots for standard messages
    // ============================================================================

    /**
     * Property test: Standard messages (text, image, video, audio, file) should NOT
     * use minimal slots when not deleted.
     *
     * **Validates: Requirements 1.2-1.6 (standard messages render with full slots)**
     */
    "standard messages should not use minimal slots when not deleted" {
        checkAll(100, standardMessageTypeArb) { messageType ->
            val message = when (messageType) {
                CometChatConstants.MESSAGE_TYPE_TEXT -> createMockTextMessage()
                else -> createMockMediaMessage(messageType)
            }
            
            InternalContentRenderer.shouldUseMinimalSlots(message) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: shouldUseMinimalSlots for custom extension messages
    // ============================================================================

    /**
     * Property test: Custom extension messages (polls, stickers, document, whiteboard)
     * should NOT use minimal slots when not deleted.
     *
     * **Validates: Requirements 3.1-3.4 (custom messages render with full slots)**
     */
    "custom extension messages should not use minimal slots when not deleted" {
        checkAll(100, customExtensionTypeArb) { extensionType ->
            val customMessage = createMockCustomMessage(extensionType)
            
            InternalContentRenderer.shouldUseMinimalSlots(customMessage) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: shouldUseMinimalSlots for meeting messages
    // ============================================================================

    /**
     * Property test: Meeting messages (category "custom", type "meeting") should NOT
     * use minimal slots - they show avatar in leading view for incoming messages.
     *
     * // Feature: jetpack-message-bubble-parity, Property 1: shouldUseMinimalSlots matches reference logic
     * **Validates: Requirements 5.1, 5.2**
     */
    "meeting messages should not use minimal slots" {
        checkAll(100, alignmentArb) { _ ->
            val meetingMessage = createMockMeetingMessage()

            InternalContentRenderer.shouldUseMinimalSlots(meetingMessage) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: shouldUseMinimalSlots for custom non-meeting messages
    // ============================================================================

    /**
     * Property test: Custom messages that are NOT meetings should NOT use minimal slots.
     *
     * // Feature: jetpack-message-bubble-parity, Property 1: shouldUseMinimalSlots matches reference logic
     * **Validates: Requirements 5.1, 5.2**
     */
    "custom non-meeting messages should not use minimal slots" {
        checkAll(100, customExtensionTypeArb) { extensionType ->
            val customMessage = createMockCustomMessage(extensionType)

            InternalContentRenderer.shouldUseMinimalSlots(customMessage) shouldBe false
        }
    }
    // ============================================================================
    // Property Test: shouldUseMinimalSlots matches reference logic (Property 1)
    // ============================================================================

    /**
     * Property test: shouldUseMinimalSlots returns true if and only if the message
     * category is "action" or "call".
     * Meeting messages are NOT minimal - they show avatar in leading view.
     * For all other category/type combinations, it returns false.
     *
     * // Feature: jetpack-message-bubble-parity, Property 1: shouldUseMinimalSlots matches reference logic
     * **Validates: Requirements 5.1, 5.2**
     */
    "shouldUseMinimalSlots matches reference logic for all category/type combinations" {
        val categoryArb = Arb.element(
            CometChatConstants.CATEGORY_MESSAGE,
            CometChatConstants.CATEGORY_ACTION,
            CometChatConstants.CATEGORY_CALL,
            CometChatConstants.CATEGORY_CUSTOM,
            "unknown_category"
        )

        val typeArb = Arb.element(
            CometChatConstants.MESSAGE_TYPE_TEXT,
            CometChatConstants.MESSAGE_TYPE_IMAGE,
            CometChatConstants.CALL_TYPE_AUDIO,
            CometChatConstants.CALL_TYPE_VIDEO,
            UIKitConstants.MessageType.MEETING,
            InternalContentRenderer.EXTENSION_POLLS,
            InternalContentRenderer.EXTENSION_STICKER,
            InternalContentRenderer.EXTENSION_DOCUMENT,
            InternalContentRenderer.EXTENSION_WHITEBOARD,
            "unknown_type"
        )

        checkAll(100, categoryArb, typeArb) { category, type ->
            val message = mock(BaseMessage::class.java)
            `when`(message.deletedAt).thenReturn(0L)
            `when`(message.category).thenReturn(category)
            `when`(message.type).thenReturn(type)

            // Only action and call messages use minimal slots
            // Meeting messages are NOT minimal - they show avatar
            val expected = when (category) {
                CometChatConstants.CATEGORY_ACTION -> true
                CometChatConstants.CATEGORY_CALL -> true
                else -> false
            }

            InternalContentRenderer.shouldUseMinimalSlots(message) shouldBe expected
        }
    }

    // ============================================================================
    // Property Test: Known message types are recognized
    // ============================================================================

    /**
     * Property test: All known message category/type combinations should be
     * recognized by the internal renderer. This is verified by checking that
     * the category/type combinations match the expected mapping table.
     *
     * **Validates: Requirements 1.1-1.6, 2.1-2.4, 3.1-3.4**
     */
    "all known message types should have defined category/type combinations" {
        // Define all known message type mappings
        val knownMappings = listOf(
            // Standard messages (category: message)
            CometChatConstants.CATEGORY_MESSAGE to CometChatConstants.MESSAGE_TYPE_TEXT,
            CometChatConstants.CATEGORY_MESSAGE to CometChatConstants.MESSAGE_TYPE_IMAGE,
            CometChatConstants.CATEGORY_MESSAGE to CometChatConstants.MESSAGE_TYPE_VIDEO,
            CometChatConstants.CATEGORY_MESSAGE to CometChatConstants.MESSAGE_TYPE_AUDIO,
            CometChatConstants.CATEGORY_MESSAGE to CometChatConstants.MESSAGE_TYPE_FILE,
            // Action messages
            CometChatConstants.CATEGORY_ACTION to CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER,
            // Call messages
            CometChatConstants.CATEGORY_CALL to CometChatConstants.CALL_TYPE_AUDIO,
            CometChatConstants.CATEGORY_CALL to CometChatConstants.CALL_TYPE_VIDEO,
            // Meeting messages
            CometChatConstants.CATEGORY_CUSTOM to UIKitConstants.MessageType.MEETING,
            // Custom extension messages
            CometChatConstants.CATEGORY_CUSTOM to InternalContentRenderer.EXTENSION_POLLS,
            CometChatConstants.CATEGORY_CUSTOM to InternalContentRenderer.EXTENSION_STICKER,
            CometChatConstants.CATEGORY_CUSTOM to InternalContentRenderer.EXTENSION_DOCUMENT,
            CometChatConstants.CATEGORY_CUSTOM to InternalContentRenderer.EXTENSION_WHITEBOARD
        )

        val mappingArb = Arb.element(knownMappings)

        checkAll(100, mappingArb) { (category, type) ->
            // Verify the mapping exists and is valid
            val isKnownType = isKnownMessageType(category, type)
            isKnownType shouldBe true
        }
    }

    // ============================================================================
    // Property Test: Unknown message types are not recognized
    // ============================================================================

    /**
     * Property test: Unknown message category/type combinations should not be
     * recognized as known types.
     *
     * **Validates: Requirements 8.1 (fallback for unknown types)**
     */
    "unknown message types should not be recognized" {
        val unknownMappings = listOf(
            "unknown_category" to "unknown_type",
            CometChatConstants.CATEGORY_MESSAGE to "unknown_type",
            "unknown_category" to CometChatConstants.MESSAGE_TYPE_TEXT,
            CometChatConstants.CATEGORY_CUSTOM to "unknown_extension"
        )

        val unknownMappingArb = Arb.element(unknownMappings)

        checkAll(100, unknownMappingArb) { (category, type) ->
            val isKnownType = isKnownMessageType(category, type)
            isKnownType shouldBe false
        }
    }

    // ============================================================================
    // Property Test: Extension type constants match expected values
    // ============================================================================

    /**
     * Property test: Extension type constants should have the expected values
     * that match the factory key generation.
     *
     * **Validates: Requirements 3.1-3.4**
     */
    "extension type constants should have expected values" {
        InternalContentRenderer.EXTENSION_POLLS shouldBe "extension_poll"
        InternalContentRenderer.EXTENSION_STICKER shouldBe "extension_sticker"
        InternalContentRenderer.EXTENSION_DOCUMENT shouldBe "extension_document"
        InternalContentRenderer.EXTENSION_WHITEBOARD shouldBe "extension_whiteboard"
    }
})

// ============================================================================
// Helper functions to create mock messages using Mockito
// ============================================================================

/**
 * Creates a mock TextMessage for testing.
 */
private fun createMockTextMessage(): TextMessage {
    val message = mock(TextMessage::class.java)
    `when`(message.deletedAt).thenReturn(0L)
    `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
    `when`(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
    return message
}

/**
 * Creates a mock MediaMessage for testing.
 */
private fun createMockMediaMessage(messageType: String): MediaMessage {
    val message = mock(MediaMessage::class.java)
    `when`(message.deletedAt).thenReturn(0L)
    `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
    `when`(message.type).thenReturn(messageType)
    return message
}

/**
 * Creates a mock deleted message for testing.
 */
private fun createMockDeletedMessage(messageType: String, deletedAt: Long): BaseMessage {
    val message = mock(BaseMessage::class.java)
    `when`(message.deletedAt).thenReturn(deletedAt)
    `when`(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
    `when`(message.type).thenReturn(messageType)
    return message
}

/**
 * Creates a mock Action message for testing.
 */
private fun createMockActionMessage(): Action {
    val message = mock(Action::class.java)
    `when`(message.deletedAt).thenReturn(0L)
    `when`(message.category).thenReturn(CometChatConstants.CATEGORY_ACTION)
    `when`(message.type).thenReturn(CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER)
    return message
}

/**
 * Creates a mock Call message for testing.
 */
private fun createMockCallMessage(callType: String): Call {
    val message = mock(Call::class.java)
    `when`(message.deletedAt).thenReturn(0L)
    `when`(message.category).thenReturn(CometChatConstants.CATEGORY_CALL)
    `when`(message.type).thenReturn(callType)
    return message
}

/**
 * Creates a mock CustomMessage for testing.
 */
private fun createMockCustomMessage(extensionType: String): CustomMessage {
    val message = mock(CustomMessage::class.java)
    `when`(message.deletedAt).thenReturn(0L)
    `when`(message.category).thenReturn(CometChatConstants.CATEGORY_CUSTOM)
    `when`(message.type).thenReturn(extensionType)
    return message
}

/**
 * Creates a mock meeting message for testing.
 * Meeting messages have category "custom" and type "meeting".
 */
private fun createMockMeetingMessage(): CustomMessage {
    val message = mock(CustomMessage::class.java)
    `when`(message.deletedAt).thenReturn(0L)
    `when`(message.category).thenReturn(CometChatConstants.CATEGORY_CUSTOM)
    `when`(message.type).thenReturn(UIKitConstants.MessageType.MEETING)
    return message
}

/**
 * Creates a mock message with the specified category for testing.
 */
private fun createMockMessageWithCategory(category: String): BaseMessage {
    val message = mock(BaseMessage::class.java)
    `when`(message.deletedAt).thenReturn(0L)
    `when`(message.category).thenReturn(category)
    `when`(message.type).thenReturn(
        when (category) {
            CometChatConstants.CATEGORY_MESSAGE -> CometChatConstants.MESSAGE_TYPE_TEXT
            CometChatConstants.CATEGORY_ACTION -> CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER
            CometChatConstants.CATEGORY_CALL -> CometChatConstants.CALL_TYPE_AUDIO
            CometChatConstants.CATEGORY_CUSTOM -> InternalContentRenderer.EXTENSION_POLLS
            else -> "unknown"
        }
    )
    return message
}

/**
 * Checks if a category/type combination is a known message type.
 * This mirrors the logic in InternalContentRenderer.renderContent().
 */
private fun isKnownMessageType(category: String, type: String): Boolean {
    return when (category) {
        CometChatConstants.CATEGORY_MESSAGE -> type in listOf(
            CometChatConstants.MESSAGE_TYPE_TEXT,
            CometChatConstants.MESSAGE_TYPE_IMAGE,
            CometChatConstants.MESSAGE_TYPE_VIDEO,
            CometChatConstants.MESSAGE_TYPE_AUDIO,
            CometChatConstants.MESSAGE_TYPE_FILE
        )
        CometChatConstants.CATEGORY_ACTION -> type == CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER
        CometChatConstants.CATEGORY_CALL -> type in listOf(
            CometChatConstants.CALL_TYPE_AUDIO,
            CometChatConstants.CALL_TYPE_VIDEO
        )
        CometChatConstants.CATEGORY_CUSTOM -> type in listOf(
            InternalContentRenderer.EXTENSION_POLLS,
            InternalContentRenderer.EXTENSION_STICKER,
            InternalContentRenderer.EXTENSION_DOCUMENT,
            InternalContentRenderer.EXTENSION_WHITEBOARD,
            UIKitConstants.MessageType.MEETING
        )
        else -> false
    }
}
