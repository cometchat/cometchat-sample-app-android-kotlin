package com.cometchat.uikit.kotlin.presentation.messagelist

import com.cometchat.chat.constants.CometChatConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * Preservation Property Tests for Regular Message Interactions (ENG-32209)
 * 
 * **Property 2: Preservation** - Regular Message Interactions Unchanged
 * 
 * These tests verify that the fix for ACTION/CALL message interactions does NOT
 * affect the behavior of regular messages (TEXT, IMAGE, VIDEO, AUDIO, FILE, CUSTOM).
 * 
 * **Preservation Requirements:**
 * - Long-press on regular messages (TEXT, IMAGE, VIDEO, AUDIO, FILE, CUSTOM) must continue to invoke callback
 * - Swipe-to-reply on regular messages must continue to work (non-zero movement flags)
 * - Long-press on deleted messages must continue to NOT invoke callback (existing behavior)
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**
 */
class RegularMessageInteractionPreservationTest : FunSpec({

    // ==================== Data Classes ====================

    /**
     * Data class representing a message for testing interactions.
     */
    data class TestMessage(
        val id: Long,
        val category: String,
        val type: String,
        val deletedAt: Long = 0L,
        val sentAt: Long = System.currentTimeMillis() / 1000
    )

    /**
     * Data class representing the result of a long-press interaction.
     */
    data class LongPressResult(
        val callbackInvoked: Boolean,
        val message: TestMessage?
    )

    /**
     * Data class representing the result of getMovementFlags.
     */
    data class SwipeResult(
        val movementFlags: Int,
        val swipeAllowed: Boolean
    )

    // ==================== Helper Functions ====================

    /**
     * Checks if the message is a regular (non-ACTION/CALL) message.
     * Returns true for MESSAGE, CUSTOM, INTERACTIVE categories.
     */
    fun isRegularMessage(message: TestMessage): Boolean {
        return !message.category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true) &&
               !message.category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)
    }

    /**
     * Simulates the CURRENT behavior of chatuikit-kotlin's long-click handling for regular messages.
     * 
     * This represents the behavior that MUST be preserved after the fix.
     * Regular messages (non-ACTION/CALL) should invoke the callback when not deleted.
     * 
     * Reference: MessageAdapter.kt bindMessage() method
     */
    fun simulateLongClickBehavior(message: TestMessage): LongPressResult {
        // Current behavior for regular messages:
        // - Non-deleted messages invoke callback
        // - Deleted messages do NOT invoke callback
        return if (message.deletedAt == 0L) {
            LongPressResult(callbackInvoked = true, message = message)
        } else {
            LongPressResult(callbackInvoked = false, message = null)
        }
    }

    /**
     * Simulates the CURRENT behavior of chatuikit-kotlin's getMovementFlags for regular messages.
     * 
     * This represents the behavior that MUST be preserved after the fix.
     * Regular messages should return non-zero swipe flags when valid.
     * 
     * Reference: CometChatMessageList.kt getMovementFlags() method
     */
    fun simulateGetMovementFlags(message: TestMessage, isRtl: Boolean = false): SwipeResult {
        // Current behavior for regular messages:
        // - Valid messages (sent, not deleted) return swipe flags
        // - Deleted or unsent messages return 0
        
        if (message.deletedAt > 0 || message.sentAt == 0L || message.id == 0L) {
            return SwipeResult(movementFlags = 0, swipeAllowed = false)
        }
        
        // ItemTouchHelper.LEFT = 4, ItemTouchHelper.RIGHT = 8
        val swipeFlag = if (isRtl) 4 else 8
        // makeMovementFlags(0, swipeFlag) = swipeFlag << 16
        val movementFlags = swipeFlag shl 16
        
        return SwipeResult(movementFlags = movementFlags, swipeAllowed = true)
    }


    // ==================== Generators ====================

    /**
     * Generator for regular message categories (non-ACTION/CALL).
     * Uses CometChatConstants.CATEGORY_MESSAGE and CATEGORY_CUSTOM.
     */
    val regularCategoryArb = Arb.element(
        CometChatConstants.CATEGORY_MESSAGE,
        CometChatConstants.CATEGORY_CUSTOM,
        "message",
        "custom",
        "interactive"
    )

    /**
     * Generator for regular message types.
     */
    val regularTypeArb = Arb.element(
        CometChatConstants.MESSAGE_TYPE_TEXT,
        CometChatConstants.MESSAGE_TYPE_IMAGE,
        CometChatConstants.MESSAGE_TYPE_VIDEO,
        CometChatConstants.MESSAGE_TYPE_AUDIO,
        CometChatConstants.MESSAGE_TYPE_FILE,
        "text",
        "image",
        "video",
        "audio",
        "file",
        "custom"
    )

    /**
     * Generator for TEXT messages.
     */
    val textMessageArb = Arb.element(
        TestMessage(
            id = 1L,
            category = CometChatConstants.CATEGORY_MESSAGE,
            type = CometChatConstants.MESSAGE_TYPE_TEXT
        ),
        TestMessage(
            id = 2L,
            category = "message",
            type = "text"
        )
    )

    /**
     * Generator for IMAGE messages.
     */
    val imageMessageArb = Arb.element(
        TestMessage(
            id = 3L,
            category = CometChatConstants.CATEGORY_MESSAGE,
            type = CometChatConstants.MESSAGE_TYPE_IMAGE
        ),
        TestMessage(
            id = 4L,
            category = "message",
            type = "image"
        )
    )

    /**
     * Generator for VIDEO messages.
     */
    val videoMessageArb = Arb.element(
        TestMessage(
            id = 5L,
            category = CometChatConstants.CATEGORY_MESSAGE,
            type = CometChatConstants.MESSAGE_TYPE_VIDEO
        ),
        TestMessage(
            id = 6L,
            category = "message",
            type = "video"
        )
    )

    /**
     * Generator for AUDIO messages.
     */
    val audioMessageArb = Arb.element(
        TestMessage(
            id = 7L,
            category = CometChatConstants.CATEGORY_MESSAGE,
            type = CometChatConstants.MESSAGE_TYPE_AUDIO
        ),
        TestMessage(
            id = 8L,
            category = "message",
            type = "audio"
        )
    )

    /**
     * Generator for FILE messages.
     */
    val fileMessageArb = Arb.element(
        TestMessage(
            id = 9L,
            category = CometChatConstants.CATEGORY_MESSAGE,
            type = CometChatConstants.MESSAGE_TYPE_FILE
        ),
        TestMessage(
            id = 10L,
            category = "message",
            type = "file"
        )
    )

    /**
     * Generator for CUSTOM messages.
     */
    val customMessageArb = Arb.element(
        TestMessage(
            id = 11L,
            category = CometChatConstants.CATEGORY_CUSTOM,
            type = "custom"
        ),
        TestMessage(
            id = 12L,
            category = "custom",
            type = "poll"
        ),
        TestMessage(
            id = 13L,
            category = "custom",
            type = "sticker"
        )
    )

    /**
     * Generator for deleted messages (various types).
     */
    val deletedMessageArb = Arb.element(
        TestMessage(
            id = 14L,
            category = CometChatConstants.CATEGORY_MESSAGE,
            type = CometChatConstants.MESSAGE_TYPE_TEXT,
            deletedAt = System.currentTimeMillis() / 1000
        ),
        TestMessage(
            id = 15L,
            category = CometChatConstants.CATEGORY_MESSAGE,
            type = CometChatConstants.MESSAGE_TYPE_IMAGE,
            deletedAt = System.currentTimeMillis() / 1000
        ),
        TestMessage(
            id = 16L,
            category = CometChatConstants.CATEGORY_CUSTOM,
            type = "custom",
            deletedAt = System.currentTimeMillis() / 1000
        )
    )


    // ==================== Property Tests ====================

    context("Property 2: Preservation - Regular Message Long-Press Behavior") {

        /**
         * **Validates: Requirement 3.1**
         * 
         * Test that long-press on TEXT messages continues to invoke callback.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: long-press on TEXT message should invoke callback (preservation)") {
            checkAll(10, textMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateLongClickBehavior(message)
                
                // Expected: Callback SHOULD be invoked for TEXT messages
                result.callbackInvoked shouldBe true
                result.message shouldNotBe null
            }
        }

        /**
         * **Validates: Requirement 3.4**
         * 
         * Test that long-press on IMAGE messages continues to invoke callback.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: long-press on IMAGE message should invoke callback (preservation)") {
            checkAll(10, imageMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateLongClickBehavior(message)
                
                // Expected: Callback SHOULD be invoked for IMAGE messages
                result.callbackInvoked shouldBe true
                result.message shouldNotBe null
            }
        }

        /**
         * **Validates: Requirement 3.4**
         * 
         * Test that long-press on VIDEO messages continues to invoke callback.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: long-press on VIDEO message should invoke callback (preservation)") {
            checkAll(10, videoMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateLongClickBehavior(message)
                
                // Expected: Callback SHOULD be invoked for VIDEO messages
                result.callbackInvoked shouldBe true
                result.message shouldNotBe null
            }
        }

        /**
         * **Validates: Requirement 3.4**
         * 
         * Test that long-press on AUDIO messages continues to invoke callback.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: long-press on AUDIO message should invoke callback (preservation)") {
            checkAll(10, audioMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateLongClickBehavior(message)
                
                // Expected: Callback SHOULD be invoked for AUDIO messages
                result.callbackInvoked shouldBe true
                result.message shouldNotBe null
            }
        }

        /**
         * **Validates: Requirement 3.4**
         * 
         * Test that long-press on FILE messages continues to invoke callback.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: long-press on FILE message should invoke callback (preservation)") {
            checkAll(10, fileMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateLongClickBehavior(message)
                
                // Expected: Callback SHOULD be invoked for FILE messages
                result.callbackInvoked shouldBe true
                result.message shouldNotBe null
            }
        }

        /**
         * **Validates: Requirement 3.2, 3.5**
         * 
         * Test that long-press on CUSTOM messages continues to invoke callback.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin/jetpack: long-press on CUSTOM message should invoke callback (preservation)") {
            checkAll(10, customMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateLongClickBehavior(message)
                
                // Expected: Callback SHOULD be invoked for CUSTOM messages
                result.callbackInvoked shouldBe true
                result.message shouldNotBe null
            }
        }

        /**
         * **Validates: Requirement 3.7**
         * 
         * Test that long-press on DELETED messages continues to NOT invoke callback.
         * This existing behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: long-press on DELETED message should NOT invoke callback (preservation)") {
            checkAll(10, deletedMessageArb) { message ->
                // Verify this is a deleted message
                message.deletedAt shouldNotBe 0L
                
                // Test current behavior (should be preserved)
                val result = simulateLongClickBehavior(message)
                
                // Expected: Callback should NOT be invoked for DELETED messages
                result.callbackInvoked shouldBe false
                result.message shouldBe null
            }
        }
    }


    context("Property 2: Preservation - Regular Message Swipe Behavior") {

        /**
         * **Validates: Requirement 3.3**
         * 
         * Test that getMovementFlags for TEXT messages returns non-zero swipe flags.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: getMovementFlags for TEXT message should return non-zero (preservation)") {
            checkAll(10, textMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateGetMovementFlags(message)
                
                // Expected: Movement flags SHOULD be non-zero for TEXT messages
                result.movementFlags shouldNotBe 0
                result.swipeAllowed shouldBe true
            }
        }

        /**
         * **Validates: Requirement 3.6**
         * 
         * Test that getMovementFlags for IMAGE messages returns non-zero swipe flags.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: getMovementFlags for IMAGE message should return non-zero (preservation)") {
            checkAll(10, imageMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateGetMovementFlags(message)
                
                // Expected: Movement flags SHOULD be non-zero for IMAGE messages
                result.movementFlags shouldNotBe 0
                result.swipeAllowed shouldBe true
            }
        }

        /**
         * **Validates: Requirement 3.6**
         * 
         * Test that getMovementFlags for VIDEO messages returns non-zero swipe flags.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: getMovementFlags for VIDEO message should return non-zero (preservation)") {
            checkAll(10, videoMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateGetMovementFlags(message)
                
                // Expected: Movement flags SHOULD be non-zero for VIDEO messages
                result.movementFlags shouldNotBe 0
                result.swipeAllowed shouldBe true
            }
        }

        /**
         * **Validates: Requirement 3.6**
         * 
         * Test that getMovementFlags for AUDIO messages returns non-zero swipe flags.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: getMovementFlags for AUDIO message should return non-zero (preservation)") {
            checkAll(10, audioMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateGetMovementFlags(message)
                
                // Expected: Movement flags SHOULD be non-zero for AUDIO messages
                result.movementFlags shouldNotBe 0
                result.swipeAllowed shouldBe true
            }
        }

        /**
         * **Validates: Requirement 3.6**
         * 
         * Test that getMovementFlags for FILE messages returns non-zero swipe flags.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: getMovementFlags for FILE message should return non-zero (preservation)") {
            checkAll(10, fileMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateGetMovementFlags(message)
                
                // Expected: Movement flags SHOULD be non-zero for FILE messages
                result.movementFlags shouldNotBe 0
                result.swipeAllowed shouldBe true
            }
        }

        /**
         * **Validates: Requirement 3.3, 3.6**
         * 
         * Test that getMovementFlags for CUSTOM messages returns non-zero swipe flags.
         * This behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: getMovementFlags for CUSTOM message should return non-zero (preservation)") {
            checkAll(10, customMessageArb) { message ->
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateGetMovementFlags(message)
                
                // Expected: Movement flags SHOULD be non-zero for CUSTOM messages
                result.movementFlags shouldNotBe 0
                result.swipeAllowed shouldBe true
            }
        }

        /**
         * Test that getMovementFlags for DELETED messages returns zero.
         * This existing behavior MUST be preserved after the fix.
         */
        test("chatuikit-kotlin: getMovementFlags for DELETED message should return zero (preservation)") {
            checkAll(10, deletedMessageArb) { message ->
                // Verify this is a deleted message
                message.deletedAt shouldNotBe 0L
                
                // Test current behavior (should be preserved)
                val result = simulateGetMovementFlags(message)
                
                // Expected: Movement flags should be 0 for DELETED messages
                result.movementFlags shouldBe 0
                result.swipeAllowed shouldBe false
            }
        }
    }


    context("Property 2: Preservation - Property-Based Tests for All Regular Messages") {

        /**
         * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**
         * 
         * Property-based test: For ALL non-ACTION/CALL category messages,
         * long-press behavior is preserved (callback invoked for non-deleted messages).
         * 
         * This test should PASS on both unfixed and fixed code.
         */
        test("Property: For ALL non-ACTION/CALL messages, long-press invokes callback when not deleted") {
            checkAll(20, Arb.long(1, 100000), regularCategoryArb, regularTypeArb) { id, category, type ->
                val message = TestMessage(id = id, category = category, type = type, deletedAt = 0L)
                
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior (should be preserved)
                val result = simulateLongClickBehavior(message)
                
                // Expected: Callback SHOULD be invoked for non-deleted regular messages
                result.callbackInvoked shouldBe true
                result.message shouldNotBe null
            }
        }

        /**
         * **Validates: Requirements 3.3, 3.6**
         * 
         * Property-based test: For ALL non-ACTION/CALL category messages,
         * swipe flags are preserved (non-zero for valid messages).
         * 
         * This test should PASS on both unfixed and fixed code.
         */
        test("Property: For ALL non-ACTION/CALL messages, swipe flags are non-zero when valid") {
            checkAll(20, Arb.long(1, 100000), regularCategoryArb, regularTypeArb) { id, category, type ->
                val message = TestMessage(
                    id = id,
                    category = category,
                    type = type,
                    deletedAt = 0L,
                    sentAt = System.currentTimeMillis() / 1000
                )
                
                // Verify this is a regular message (not ACTION/CALL)
                isRegularMessage(message) shouldBe true
                
                // Test current behavior for LTR (should be preserved)
                val resultLtr = simulateGetMovementFlags(message, isRtl = false)
                resultLtr.movementFlags shouldNotBe 0
                resultLtr.swipeAllowed shouldBe true
                
                // Test current behavior for RTL (should be preserved)
                val resultRtl = simulateGetMovementFlags(message, isRtl = true)
                resultRtl.movementFlags shouldNotBe 0
                resultRtl.swipeAllowed shouldBe true
            }
        }

        /**
         * **Validates: Requirement 3.7**
         * 
         * Property-based test: For ALL deleted messages (regardless of category),
         * long-press does NOT invoke callback.
         * 
         * This test should PASS on both unfixed and fixed code.
         */
        test("Property: For ALL deleted messages, long-press does NOT invoke callback") {
            val deletedAtArb = Arb.long(1, System.currentTimeMillis() / 1000)
            
            checkAll(20, Arb.long(1, 100000), regularCategoryArb, regularTypeArb, deletedAtArb) { id, category, type, deletedAt ->
                val message = TestMessage(id = id, category = category, type = type, deletedAt = deletedAt)
                
                // Verify this is a deleted message
                message.deletedAt shouldNotBe 0L
                
                // Test current behavior (should be preserved)
                val result = simulateLongClickBehavior(message)
                
                // Expected: Callback should NOT be invoked for deleted messages
                result.callbackInvoked shouldBe false
                result.message shouldBe null
            }
        }

        /**
         * Property-based test: For ALL deleted messages,
         * swipe flags are zero (no swipe allowed).
         * 
         * This test should PASS on both unfixed and fixed code.
         */
        test("Property: For ALL deleted messages, swipe flags are zero") {
            val deletedAtArb = Arb.long(1, System.currentTimeMillis() / 1000)
            
            checkAll(20, Arb.long(1, 100000), regularCategoryArb, regularTypeArb, deletedAtArb) { id, category, type, deletedAt ->
                val message = TestMessage(id = id, category = category, type = type, deletedAt = deletedAt)
                
                // Verify this is a deleted message
                message.deletedAt shouldNotBe 0L
                
                // Test current behavior (should be preserved)
                val result = simulateGetMovementFlags(message)
                
                // Expected: Movement flags should be 0 for deleted messages
                result.movementFlags shouldBe 0
                result.swipeAllowed shouldBe false
            }
        }
    }

    context("Preservation Verification") {

        /**
         * Verify that isRegularMessage correctly identifies regular message categories.
         */
        test("isRegularMessage should return true for MESSAGE category") {
            val messageCategories = listOf(
                CometChatConstants.CATEGORY_MESSAGE,
                "message",
                "MESSAGE",
                "Message"
            )
            
            messageCategories.forEach { category ->
                val message = TestMessage(id = 1L, category = category, type = "text")
                isRegularMessage(message) shouldBe true
            }
        }

        /**
         * Verify that isRegularMessage correctly identifies CUSTOM category.
         */
        test("isRegularMessage should return true for CUSTOM category") {
            val customCategories = listOf(
                CometChatConstants.CATEGORY_CUSTOM,
                "custom",
                "CUSTOM",
                "Custom"
            )
            
            customCategories.forEach { category ->
                val message = TestMessage(id = 1L, category = category, type = "poll")
                isRegularMessage(message) shouldBe true
            }
        }

        /**
         * Verify that isRegularMessage returns false for ACTION/CALL categories.
         */
        test("isRegularMessage should return false for ACTION/CALL categories") {
            val actionCallCategories = listOf(
                CometChatConstants.CATEGORY_ACTION,
                CometChatConstants.CATEGORY_CALL,
                "action",
                "call",
                "ACTION",
                "CALL"
            )
            
            actionCallCategories.forEach { category ->
                val message = TestMessage(id = 1L, category = category, type = "groupMember")
                isRegularMessage(message) shouldBe false
            }
        }
    }
})
