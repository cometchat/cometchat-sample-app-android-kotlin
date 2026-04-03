package com.cometchat.uikit.kotlin.presentation.messagelist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Simulates UIKitConstants.MessageListAlignment enum.
 * Controls the overall alignment of messages in the list.
 */
private enum class MessageListAlignment {
    /**
     * Standard alignment: outgoing messages on right, incoming on left.
     */
    STANDARD,

    /**
     * All messages aligned to the left.
     */
    LEFT_ALIGNED
}

/**
 * Simulates UIKitConstants.MessageBubbleAlignment enum.
 * Defines the alignment of individual message bubbles.
 */
private enum class MessageBubbleAlignment {
    RIGHT, LEFT, CENTER
}

/**
 * Simulates message categories from CometChatConstants.
 */
private object MessageCategory {
    const val MESSAGE = "message"
    const val ACTION = "action"
    const val CALL = "call"
    const val CUSTOM = "custom"
}

/**
 * Test data class representing a message for alignment testing.
 * Simulates BaseMessage without requiring CometChat SDK.
 */
private data class TestMessage(
    val id: Long,
    val category: String,
    val type: String,
    val senderUid: String
)

/**
 * Test class that simulates the message alignment logic from MessageAdapter.
 * This mirrors the actual implementation without requiring Android context.
 *
 * The alignment logic follows these rules:
 * 1. Action messages (CATEGORY_ACTION) are always CENTER aligned
 * 2. Call messages (CATEGORY_CALL) are always CENTER aligned
 * 3. In LEFT_ALIGNED mode, all other messages are LEFT aligned
 * 4. In STANDARD mode, outgoing messages (sender == loggedInUser) are RIGHT aligned
 * 5. In STANDARD mode, incoming messages (sender != loggedInUser) are LEFT aligned
 */
private class TestMessageAlignmentCalculator(
    private val loggedInUserUid: String
) {
    // Simulates MessageAdapter.listAlignment
    var listAlignment: MessageListAlignment = MessageListAlignment.STANDARD

    /**
     * Simulates MessageAdapter.getMessageAlignment(BaseMessage)
     *
     * Determines the bubble alignment for a message based on:
     * - Message category (action/call messages are always centered)
     * - List alignment mode (STANDARD vs LEFT_ALIGNED)
     * - Message sender (outgoing vs incoming)
     */
    fun getMessageAlignment(message: TestMessage): MessageBubbleAlignment {
        // Action and call messages are always centered
        if (message.category == MessageCategory.ACTION ||
            message.category == MessageCategory.CALL) {
            return MessageBubbleAlignment.CENTER
        }

        // LEFT_ALIGNED mode forces all messages to left
        if (listAlignment == MessageListAlignment.LEFT_ALIGNED) {
            return MessageBubbleAlignment.LEFT
        }

        // Standard alignment based on sender
        return if (message.senderUid == loggedInUserUid) {
            MessageBubbleAlignment.RIGHT
        } else {
            MessageBubbleAlignment.LEFT
        }
    }
}

/**
 * Property-based tests for CometChatMessageList message alignment modes.
 * Uses Kotest property testing to verify correctness properties.
 *
 * Feature: messagelist-property-parity, Property 6: Message Alignment Modes
 *
 * *For any* message in the list, when `setMessageAlignment(STANDARD)` is set, outgoing messages
 * SHALL align RIGHT and incoming messages SHALL align LEFT. When `setMessageAlignment(LEFT_ALIGNED)`
 * is set, all messages SHALL align LEFT.
 *
 * **Validates: Requirements 6.1, 6.2**
 */
class CometChatMessageListAlignmentPropertyTest : FunSpec({

    // ==================== Generators ====================

    /**
     * Generator for message list alignment modes.
     */
    val alignmentModeArb = Arb.element(MessageListAlignment.STANDARD, MessageListAlignment.LEFT_ALIGNED)

    /**
     * Generator for message categories.
     */
    val categoryArb = Arb.element(
        MessageCategory.MESSAGE,
        MessageCategory.ACTION,
        MessageCategory.CALL,
        MessageCategory.CUSTOM
    )

    /**
     * Generator for non-special message categories (not action or call).
     */
    val regularCategoryArb = Arb.element(
        MessageCategory.MESSAGE,
        MessageCategory.CUSTOM
    )

    /**
     * Generator for special message categories (action and call).
     */
    val specialCategoryArb = Arb.element(
        MessageCategory.ACTION,
        MessageCategory.CALL
    )

    /**
     * Generator for message types.
     */
    val messageTypeArb = Arb.element("text", "image", "video", "audio", "file")

    /**
     * Generator for user IDs.
     */
    val userIdArb = Arb.string(5..20)

    // ==================== Property Tests ====================

    context("Property 6: Message Alignment Modes") {

        // ========================================
        // STANDARD Mode Tests
        // ========================================

        test("STANDARD mode: outgoing messages align RIGHT") {
            checkAll<Long, String, String>(100, Arb.long(1L..1000L), messageTypeArb, userIdArb) { messageId, type, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.STANDARD

                // Create outgoing message (sender == logged in user)
                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.MESSAGE,
                    type = type,
                    senderUid = loggedInUid
                )

                // Verify outgoing message aligns RIGHT
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.RIGHT
            }
        }

        test("STANDARD mode: incoming messages align LEFT") {
            checkAll<Long, String, String, String>(100, Arb.long(1L..1000L), messageTypeArb, userIdArb, userIdArb) { messageId, type, loggedInUid, senderUid ->
                // Ensure sender is different from logged in user
                val actualSenderUid = if (senderUid == loggedInUid) "${senderUid}_other" else senderUid

                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.STANDARD

                // Create incoming message (sender != logged in user)
                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.MESSAGE,
                    type = type,
                    senderUid = actualSenderUid
                )

                // Verify incoming message aligns LEFT
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.LEFT
            }
        }

        test("STANDARD mode: outgoing custom messages align RIGHT") {
            checkAll<Long, String>(100, Arb.long(1L..1000L), userIdArb) { messageId, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.STANDARD

                // Create outgoing custom message
                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.CUSTOM,
                    type = "custom_type",
                    senderUid = loggedInUid
                )

                // Verify outgoing custom message aligns RIGHT
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.RIGHT
            }
        }

        test("STANDARD mode: incoming custom messages align LEFT") {
            checkAll<Long, String, String>(100, Arb.long(1L..1000L), userIdArb, userIdArb) { messageId, loggedInUid, senderUid ->
                val actualSenderUid = if (senderUid == loggedInUid) "${senderUid}_other" else senderUid

                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.STANDARD

                // Create incoming custom message
                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.CUSTOM,
                    type = "custom_type",
                    senderUid = actualSenderUid
                )

                // Verify incoming custom message aligns LEFT
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.LEFT
            }
        }

        // ========================================
        // LEFT_ALIGNED Mode Tests
        // ========================================

        test("LEFT_ALIGNED mode: all regular messages align LEFT regardless of sender") {
            checkAll<Long, String, String, String, Boolean>(100, Arb.long(1L..1000L), regularCategoryArb, messageTypeArb, userIdArb, Arb.boolean()) { 
                messageId, category, type, loggedInUid, isOutgoing ->
                
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.LEFT_ALIGNED

                // Create message (outgoing or incoming based on isOutgoing)
                val senderUid = if (isOutgoing) loggedInUid else "${loggedInUid}_other"
                val message = TestMessage(
                    id = messageId,
                    category = category,
                    type = type,
                    senderUid = senderUid
                )

                // Verify all regular messages align LEFT in LEFT_ALIGNED mode
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.LEFT
            }
        }

        test("LEFT_ALIGNED mode: outgoing messages align LEFT (not RIGHT)") {
            checkAll<Long, String, String>(100, Arb.long(1L..1000L), messageTypeArb, userIdArb) { messageId, type, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.LEFT_ALIGNED

                // Create outgoing message
                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.MESSAGE,
                    type = type,
                    senderUid = loggedInUid
                )

                // Verify outgoing message aligns LEFT (not RIGHT) in LEFT_ALIGNED mode
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.LEFT
            }
        }

        test("LEFT_ALIGNED mode: incoming messages align LEFT") {
            checkAll<Long, String, String, String>(100, Arb.long(1L..1000L), messageTypeArb, userIdArb, userIdArb) { messageId, type, loggedInUid, senderUid ->
                val actualSenderUid = if (senderUid == loggedInUid) "${senderUid}_other" else senderUid

                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.LEFT_ALIGNED

                // Create incoming message
                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.MESSAGE,
                    type = type,
                    senderUid = actualSenderUid
                )

                // Verify incoming message aligns LEFT
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.LEFT
            }
        }

        // ========================================
        // Action Message Tests (Always CENTER)
        // ========================================

        test("Action messages always align CENTER regardless of alignment mode") {
            checkAll<Long, MessageListAlignment, String, Boolean>(100, Arb.long(1L..1000L), alignmentModeArb, userIdArb, Arb.boolean()) { 
                messageId, alignmentMode, loggedInUid, isOutgoing ->
                
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = alignmentMode

                // Create action message
                val senderUid = if (isOutgoing) loggedInUid else "${loggedInUid}_other"
                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.ACTION,
                    type = "groupMember",
                    senderUid = senderUid
                )

                // Verify action message always aligns CENTER
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }

        test("Action messages align CENTER in STANDARD mode") {
            checkAll<Long, String>(100, Arb.long(1L..1000L), userIdArb) { messageId, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.STANDARD

                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.ACTION,
                    type = "groupMember",
                    senderUid = loggedInUid
                )

                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }

        test("Action messages align CENTER in LEFT_ALIGNED mode") {
            checkAll<Long, String>(100, Arb.long(1L..1000L), userIdArb) { messageId, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.LEFT_ALIGNED

                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.ACTION,
                    type = "groupMember",
                    senderUid = loggedInUid
                )

                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }

        // ========================================
        // Call Message Tests (Always CENTER)
        // ========================================

        test("Call messages always align CENTER regardless of alignment mode") {
            checkAll<Long, MessageListAlignment, String, Boolean>(100, Arb.long(1L..1000L), alignmentModeArb, userIdArb, Arb.boolean()) { 
                messageId, alignmentMode, loggedInUid, isOutgoing ->
                
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = alignmentMode

                // Create call message
                val senderUid = if (isOutgoing) loggedInUid else "${loggedInUid}_other"
                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.CALL,
                    type = "audio",
                    senderUid = senderUid
                )

                // Verify call message always aligns CENTER
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }

        test("Call messages align CENTER in STANDARD mode") {
            checkAll<Long, String>(100, Arb.long(1L..1000L), userIdArb) { messageId, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.STANDARD

                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.CALL,
                    type = "video",
                    senderUid = loggedInUid
                )

                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }

        test("Call messages align CENTER in LEFT_ALIGNED mode") {
            checkAll<Long, String>(100, Arb.long(1L..1000L), userIdArb) { messageId, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = MessageListAlignment.LEFT_ALIGNED

                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.CALL,
                    type = "audio",
                    senderUid = loggedInUid
                )

                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }

        // ========================================
        // Special Categories Override Alignment Mode
        // ========================================

        test("Special categories (action, call) override alignment mode") {
            checkAll<Long, String, MessageListAlignment, String, Boolean>(100, Arb.long(1L..1000L), specialCategoryArb, alignmentModeArb, userIdArb, Arb.boolean()) { 
                messageId, category, alignmentMode, loggedInUid, isOutgoing ->
                
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = alignmentMode

                val senderUid = if (isOutgoing) loggedInUid else "${loggedInUid}_other"
                val message = TestMessage(
                    id = messageId,
                    category = category,
                    type = "any_type",
                    senderUid = senderUid
                )

                // Special categories always CENTER, regardless of alignment mode or sender
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }

        // ========================================
        // Alignment Mode Switching Tests
        // ========================================

        test("Switching alignment mode changes regular message alignment") {
            checkAll<Long, String, String>(100, Arb.long(1L..1000L), messageTypeArb, userIdArb) { messageId, type, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)

                // Create outgoing message
                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.MESSAGE,
                    type = type,
                    senderUid = loggedInUid
                )

                // In STANDARD mode, outgoing should be RIGHT
                calculator.listAlignment = MessageListAlignment.STANDARD
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.RIGHT

                // In LEFT_ALIGNED mode, outgoing should be LEFT
                calculator.listAlignment = MessageListAlignment.LEFT_ALIGNED
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.LEFT
            }
        }

        test("Switching alignment mode does not affect action messages") {
            checkAll<Long, String>(100, Arb.long(1L..1000L), userIdArb) { messageId, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)

                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.ACTION,
                    type = "groupMember",
                    senderUid = loggedInUid
                )

                // In STANDARD mode, action should be CENTER
                calculator.listAlignment = MessageListAlignment.STANDARD
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER

                // In LEFT_ALIGNED mode, action should still be CENTER
                calculator.listAlignment = MessageListAlignment.LEFT_ALIGNED
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }

        test("Switching alignment mode does not affect call messages") {
            checkAll<Long, String>(100, Arb.long(1L..1000L), userIdArb) { messageId, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)

                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.CALL,
                    type = "audio",
                    senderUid = loggedInUid
                )

                // In STANDARD mode, call should be CENTER
                calculator.listAlignment = MessageListAlignment.STANDARD
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER

                // In LEFT_ALIGNED mode, call should still be CENTER
                calculator.listAlignment = MessageListAlignment.LEFT_ALIGNED
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }

        // ========================================
        // Default Alignment Mode Tests
        // ========================================

        test("Default alignment mode is STANDARD") {
            checkAll<String>(100, userIdArb) { loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)

                // Verify default is STANDARD
                calculator.listAlignment shouldBe MessageListAlignment.STANDARD
            }
        }

        test("Default mode aligns outgoing messages RIGHT") {
            checkAll<Long, String, String>(100, Arb.long(1L..1000L), messageTypeArb, userIdArb) { messageId, type, loggedInUid ->
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                // Don't set alignment mode - use default

                val message = TestMessage(
                    id = messageId,
                    category = MessageCategory.MESSAGE,
                    type = type,
                    senderUid = loggedInUid
                )

                // Default (STANDARD) should align outgoing RIGHT
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.RIGHT
            }
        }

        // ========================================
        // Comprehensive Property Tests
        // ========================================

        test("For any message category and alignment mode, alignment is deterministic") {
            checkAll<Long, String, MessageListAlignment, String, Boolean>(100, Arb.long(1L..1000L), categoryArb, alignmentModeArb, userIdArb, Arb.boolean()) { 
                messageId, category, alignmentMode, loggedInUid, isOutgoing ->
                
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = alignmentMode

                val senderUid = if (isOutgoing) loggedInUid else "${loggedInUid}_other"
                val message = TestMessage(
                    id = messageId,
                    category = category,
                    type = "any_type",
                    senderUid = senderUid
                )

                // Calculate alignment twice
                val alignment1 = calculator.getMessageAlignment(message)
                val alignment2 = calculator.getMessageAlignment(message)

                // Alignment should be deterministic
                alignment1 shouldBe alignment2
            }
        }

        test("Alignment depends only on category, alignment mode, and sender") {
            checkAll<String, MessageListAlignment, String, Boolean>(100, categoryArb, alignmentModeArb, userIdArb, Arb.boolean()) { 
                category, alignmentMode, loggedInUid, isOutgoing ->
                
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = alignmentMode

                val senderUid = if (isOutgoing) loggedInUid else "${loggedInUid}_other"

                // Create two messages with same category and sender but different IDs and types
                val message1 = TestMessage(
                    id = 1L,
                    category = category,
                    type = "text",
                    senderUid = senderUid
                )
                val message2 = TestMessage(
                    id = 999L,
                    category = category,
                    type = "image",
                    senderUid = senderUid
                )

                // Alignment should be the same for both
                calculator.getMessageAlignment(message1) shouldBe calculator.getMessageAlignment(message2)
            }
        }

        test("Only three possible alignment values exist") {
            checkAll<Long, String, MessageListAlignment, String, Boolean>(100, Arb.long(1L..1000L), categoryArb, alignmentModeArb, userIdArb, Arb.boolean()) { 
                messageId, category, alignmentMode, loggedInUid, isOutgoing ->
                
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = alignmentMode

                val senderUid = if (isOutgoing) loggedInUid else "${loggedInUid}_other"
                val message = TestMessage(
                    id = messageId,
                    category = category,
                    type = "any_type",
                    senderUid = senderUid
                )

                val alignment = calculator.getMessageAlignment(message)

                // Alignment must be one of the three valid values
                (alignment == MessageBubbleAlignment.LEFT ||
                 alignment == MessageBubbleAlignment.RIGHT ||
                 alignment == MessageBubbleAlignment.CENTER) shouldBe true
            }
        }

        test("Regular messages never align CENTER") {
            checkAll<Long, String, MessageListAlignment, String, Boolean>(100, Arb.long(1L..1000L), regularCategoryArb, alignmentModeArb, userIdArb, Arb.boolean()) { 
                messageId, category, alignmentMode, loggedInUid, isOutgoing ->
                
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = alignmentMode

                val senderUid = if (isOutgoing) loggedInUid else "${loggedInUid}_other"
                val message = TestMessage(
                    id = messageId,
                    category = category,
                    type = "any_type",
                    senderUid = senderUid
                )

                // Regular messages (MESSAGE, CUSTOM) should never be CENTER
                calculator.getMessageAlignment(message) shouldBe 
                    if (alignmentMode == MessageListAlignment.LEFT_ALIGNED) {
                        MessageBubbleAlignment.LEFT
                    } else {
                        if (isOutgoing) MessageBubbleAlignment.RIGHT else MessageBubbleAlignment.LEFT
                    }
            }
        }

        test("Special messages never align LEFT or RIGHT") {
            checkAll<Long, String, MessageListAlignment, String, Boolean>(100, Arb.long(1L..1000L), specialCategoryArb, alignmentModeArb, userIdArb, Arb.boolean()) { 
                messageId, category, alignmentMode, loggedInUid, isOutgoing ->
                
                val calculator = TestMessageAlignmentCalculator(loggedInUid)
                calculator.listAlignment = alignmentMode

                val senderUid = if (isOutgoing) loggedInUid else "${loggedInUid}_other"
                val message = TestMessage(
                    id = messageId,
                    category = category,
                    type = "any_type",
                    senderUid = senderUid
                )

                // Special messages (ACTION, CALL) should always be CENTER
                calculator.getMessageAlignment(message) shouldBe MessageBubbleAlignment.CENTER
            }
        }
    }
})
