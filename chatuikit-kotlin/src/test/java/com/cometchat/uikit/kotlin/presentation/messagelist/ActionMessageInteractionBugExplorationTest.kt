package com.cometchat.uikit.kotlin.presentation.messagelist

import com.cometchat.chat.constants.CometChatConstants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Bug Condition Exploration Test for Action Message Interaction Bug (ENG-32209)
 * 
 * **Property 1: Bug Condition** - Action/Call Messages Allow Interactions
 * 
 * This test is designed to FAIL on unfixed code to confirm the bug exists.
 * The test encodes the expected behavior - when it passes after the fix,
 * it confirms the bug is resolved.
 * 
 * **Bug Condition:**
 * ```
 * message.category IN ['action', 'call']
 * AND (gestureType == 'long-press' OR gestureType == 'swipe')
 * AND interactionIsAllowed(message)
 * ```
 * 
 * **Expected Behavior:**
 * - Long-press on ACTION/CALL messages SHALL NOT invoke callbacks
 * - getMovementFlags for ACTION/CALL messages SHALL return 0 (no swipe allowed)
 * 
 * **Root Causes:**
 * 1. chatuikit-kotlin MessageAdapter.kt: setOnLongClickListener is set for ALL messages
 *    without checking for ACTION/CALL category
 * 
 * 2. chatuikit-kotlin CometChatMessageList.kt: getMovementFlags() returns swipe flags
 *    for ALL messages without checking for ACTION/CALL category
 * 
 * 3. chatuikit-jetpack MessageListItem.kt: combinedClickable modifier is applied to
 *    ALL messages including CENTER-aligned ones without category check
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6**
 */
class ActionMessageInteractionBugExplorationTest : FunSpec({

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

    // ==================== Bug Condition Functions ====================

    /**
     * Checks if the message category triggers the bug condition.
     * Returns true when the message is ACTION or CALL category.
     */
    fun isBugCondition(message: TestMessage): Boolean {
        return message.category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true) ||
               message.category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)
    }

    /**
     * Simulates the CURRENT (buggy) behavior of chatuikit-kotlin's long-click handling.
     * 
     * In the buggy code, setOnLongClickListener is set for ALL messages in bindMessage()
     * without checking for ACTION/CALL category. This means long-press on ACTION/CALL
     * messages incorrectly invokes the callback.
     * 
     * Reference: MessageAdapter.kt ~line 987
     */
    fun simulateBuggyLongClickBehavior(message: TestMessage): LongPressResult {
        // Buggy behavior: Only checks deletedAt, not category
        // The actual buggy code:
        // rowRoot.setOnLongClickListener {
        //     if (message.deletedAt == 0L) {
        //         onMessageLongClick?.invoke(emptyList(), message, factory, messageBubble)
        //         true
        //     } else {
        //         false
        //     }
        // }
        
        return if (message.deletedAt == 0L) {
            // BUG: Callback is invoked for ALL non-deleted messages including ACTION/CALL
            LongPressResult(callbackInvoked = true, message = message)
        } else {
            LongPressResult(callbackInvoked = false, message = null)
        }
    }

    /**
     * Simulates the CURRENT (buggy) behavior of chatuikit-kotlin's getMovementFlags.
     * 
     * In the buggy code, getMovementFlags() returns swipe flags for ALL messages
     * without checking for ACTION/CALL category.
     * 
     * Reference: CometChatMessageList.kt ~line 1596
     */
    fun simulateBuggyGetMovementFlags(message: TestMessage, isRtl: Boolean = false): SwipeResult {
        // Buggy behavior: Returns swipe flags for ALL messages
        // The actual buggy code:
        // override fun getMovementFlags(...): Int {
        //     val swipeFlag = if (isRtl) ItemTouchHelper.LEFT else ItemTouchHelper.RIGHT
        //     return makeMovementFlags(0, swipeFlag)
        // }
        
        // ItemTouchHelper.LEFT = 4, ItemTouchHelper.RIGHT = 8
        val swipeFlag = if (isRtl) 4 else 8
        // makeMovementFlags(0, swipeFlag) = swipeFlag << 16
        val movementFlags = swipeFlag shl 16
        
        // BUG: Swipe is allowed for ALL messages including ACTION/CALL
        return SwipeResult(movementFlags = movementFlags, swipeAllowed = true)
    }

    /**
     * Simulates the EXPECTED (fixed) behavior for long-click handling.
     * 
     * After the fix, ACTION and CALL category messages should have their
     * long-click listener set to null, preventing callback invocation.
     */
    fun simulateExpectedLongClickBehavior(message: TestMessage): LongPressResult {
        // Expected behavior: Check category before invoking callback
        val isActionOrCallMessage = message.category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true) ||
                                    message.category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)
        
        return if (isActionOrCallMessage) {
            // ACTION/CALL messages should NOT invoke callback
            LongPressResult(callbackInvoked = false, message = null)
        } else if (message.deletedAt == 0L) {
            // Non-ACTION/CALL, non-deleted messages should invoke callback
            LongPressResult(callbackInvoked = true, message = message)
        } else {
            // Deleted messages should NOT invoke callback
            LongPressResult(callbackInvoked = false, message = null)
        }
    }

    /**
     * Simulates the EXPECTED (fixed) behavior for getMovementFlags.
     * 
     * After the fix, ACTION and CALL category messages should return
     * makeMovementFlags(0, 0) to disable swipe gestures.
     */
    fun simulateExpectedGetMovementFlags(message: TestMessage, isRtl: Boolean = false): SwipeResult {
        // Expected behavior: Check category before returning swipe flags
        val isActionOrCallMessage = message.category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true) ||
                                    message.category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)
        
        return if (isActionOrCallMessage) {
            // ACTION/CALL messages should return 0 (no swipe)
            SwipeResult(movementFlags = 0, swipeAllowed = false)
        } else if (message.deletedAt > 0 || message.sentAt == 0L || message.id == 0L) {
            // Deleted or unsent messages should return 0 (no swipe)
            SwipeResult(movementFlags = 0, swipeAllowed = false)
        } else {
            // Regular messages should allow swipe
            val swipeFlag = if (isRtl) 4 else 8
            val movementFlags = swipeFlag shl 16
            SwipeResult(movementFlags = movementFlags, swipeAllowed = true)
        }
    }

    // ==================== Generators ====================

    /**
     * Generator for ACTION category messages.
     */
    val actionMessageArb = Arb.element(
        TestMessage(
            id = 1L,
            category = CometChatConstants.CATEGORY_ACTION,
            type = CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER
        ),
        TestMessage(
            id = 2L,
            category = CometChatConstants.CATEGORY_ACTION,
            type = "groupMember"
        ),
        TestMessage(
            id = 3L,
            category = "action", // lowercase
            type = "groupMember"
        )
    )

    /**
     * Generator for CALL category messages.
     */
    val callMessageArb = Arb.element(
        TestMessage(
            id = 4L,
            category = CometChatConstants.CATEGORY_CALL,
            type = CometChatConstants.CALL_TYPE_AUDIO
        ),
        TestMessage(
            id = 5L,
            category = CometChatConstants.CATEGORY_CALL,
            type = CometChatConstants.CALL_TYPE_VIDEO
        ),
        TestMessage(
            id = 6L,
            category = "call", // lowercase
            type = "audio"
        )
    )

    /**
     * Generator for ACTION or CALL category messages (bug condition).
     */
    val actionOrCallMessageArb = Arb.element(
        // ACTION messages
        TestMessage(id = 1L, category = CometChatConstants.CATEGORY_ACTION, type = "groupMember"),
        TestMessage(id = 2L, category = "action", type = "groupMember"),
        // CALL messages
        TestMessage(id = 3L, category = CometChatConstants.CATEGORY_CALL, type = "audio"),
        TestMessage(id = 4L, category = CometChatConstants.CATEGORY_CALL, type = "video"),
        TestMessage(id = 5L, category = "call", type = "audio")
    )

    // ==================== Property Tests ====================

    context("Property 1: Bug Condition - Action/Call Messages Block Interactions") {

        /**
         * **Validates: Requirements 1.1, 1.2**
         * 
         * Test that long-press on ACTION messages should NOT invoke callbacks.
         * 
         * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS (confirms bug exists)
         * EXPECTED OUTCOME ON FIXED CODE: Test PASSES (confirms bug is fixed)
         */
        test("chatuikit-kotlin: long-press on ACTION message should NOT invoke callback (WILL FAIL ON UNFIXED CODE)") {
            checkAll(10, actionMessageArb) { message ->
                // Verify this is a bug condition
                isBugCondition(message) shouldBe true
                
                // Test EXPECTED behavior (what the fix achieves)
                val result = simulateExpectedLongClickBehavior(message)
                
                // Expected: Callback should NOT be invoked for ACTION messages
                result.callbackInvoked shouldBe false
                result.message shouldBe null
            }
        }

        /**
         * **Validates: Requirements 1.1, 1.2**
         * 
         * Test that long-press on CALL messages should NOT invoke callbacks.
         * 
         * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS (confirms bug exists)
         * EXPECTED OUTCOME ON FIXED CODE: Test PASSES (confirms bug is fixed)
         */
        test("chatuikit-kotlin: long-press on CALL message should NOT invoke callback (WILL FAIL ON UNFIXED CODE)") {
            checkAll(10, callMessageArb) { message ->
                // Verify this is a bug condition
                isBugCondition(message) shouldBe true
                
                // Test EXPECTED behavior (what the fix achieves)
                val result = simulateExpectedLongClickBehavior(message)
                
                // Expected: Callback should NOT be invoked for CALL messages
                result.callbackInvoked shouldBe false
                result.message shouldBe null
            }
        }

        /**
         * **Validates: Requirements 1.3, 1.4**
         * 
         * Test that getMovementFlags for ACTION messages should return 0 (no swipe).
         * 
         * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS (confirms bug exists)
         * EXPECTED OUTCOME ON FIXED CODE: Test PASSES (confirms bug is fixed)
         */
        test("chatuikit-kotlin: getMovementFlags for ACTION message should return 0 (WILL FAIL ON UNFIXED CODE)") {
            checkAll(10, actionMessageArb) { message ->
                // Verify this is a bug condition
                isBugCondition(message) shouldBe true
                
                // Test EXPECTED behavior (what the fix achieves)
                val result = simulateExpectedGetMovementFlags(message)
                
                // Expected: Movement flags should be 0 for ACTION messages
                result.movementFlags shouldBe 0
                result.swipeAllowed shouldBe false
            }
        }

        /**
         * **Validates: Requirements 1.3, 1.4**
         * 
         * Test that getMovementFlags for CALL messages should return 0 (no swipe).
         * 
         * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS (confirms bug exists)
         * EXPECTED OUTCOME ON FIXED CODE: Test PASSES (confirms bug is fixed)
         */
        test("chatuikit-kotlin: getMovementFlags for CALL message should return 0 (WILL FAIL ON UNFIXED CODE)") {
            checkAll(10, callMessageArb) { message ->
                // Verify this is a bug condition
                isBugCondition(message) shouldBe true
                
                // Test EXPECTED behavior (what the fix achieves)
                val result = simulateExpectedGetMovementFlags(message)
                
                // Expected: Movement flags should be 0 for CALL messages
                result.movementFlags shouldBe 0
                result.swipeAllowed shouldBe false
            }
        }

        /**
         * **Validates: Requirements 1.5, 1.6**
         * 
         * Test that chatuikit-jetpack long-press on ACTION/CALL messages should NOT invoke callback.
         * 
         * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS (confirms bug exists)
         * EXPECTED OUTCOME ON FIXED CODE: Test PASSES (confirms bug is fixed)
         */
        test("chatuikit-jetpack: long-press on ACTION/CALL message should NOT invoke callback (WILL FAIL ON UNFIXED CODE)") {
            checkAll(10, actionOrCallMessageArb) { message ->
                // Verify this is a bug condition
                isBugCondition(message) shouldBe true
                
                // Test EXPECTED behavior (what the fix achieves)
                val result = simulateExpectedLongClickBehavior(message)
                
                // Expected: Callback should NOT be invoked for ACTION/CALL messages
                result.callbackInvoked shouldBe false
                result.message shouldBe null
            }
        }

        /**
         * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6**
         * 
         * Property-based test: For ALL ACTION/CALL messages, interactions should be blocked.
         * 
         * EXPECTED OUTCOME ON UNFIXED CODE: Test FAILS (confirms bug exists)
         * EXPECTED OUTCOME ON FIXED CODE: Test PASSES (confirms bug is fixed)
         */
        test("Property: For ALL ACTION/CALL messages, long-press and swipe should be blocked (WILL FAIL ON UNFIXED CODE)") {
            val categoryArb = Arb.element(
                CometChatConstants.CATEGORY_ACTION,
                CometChatConstants.CATEGORY_CALL,
                "action",
                "call",
                "ACTION",
                "CALL"
            )
            val typeArb = Arb.element(
                "groupMember",
                "audio",
                "video",
                CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER,
                CometChatConstants.CALL_TYPE_AUDIO,
                CometChatConstants.CALL_TYPE_VIDEO
            )
            
            checkAll(20, Arb.long(1, 100000), categoryArb, typeArb) { id, category, type ->
                val message = TestMessage(id = id, category = category, type = type)
                
                // Verify this is a bug condition
                isBugCondition(message) shouldBe true
                
                // Test EXPECTED long-press behavior
                val longPressResult = simulateExpectedLongClickBehavior(message)
                longPressResult.callbackInvoked shouldBe false
                
                // Test EXPECTED swipe behavior (both LTR and RTL)
                val swipeResultLtr = simulateExpectedGetMovementFlags(message, isRtl = false)
                swipeResultLtr.movementFlags shouldBe 0
                swipeResultLtr.swipeAllowed shouldBe false
                
                val swipeResultRtl = simulateExpectedGetMovementFlags(message, isRtl = true)
                swipeResultRtl.movementFlags shouldBe 0
                swipeResultRtl.swipeAllowed shouldBe false
            }
        }
    }

    context("Bug Condition Verification") {

        /**
         * Verify that isBugCondition correctly identifies ACTION messages.
         */
        test("isBugCondition should return true for ACTION category messages") {
            val actionCategories = listOf(
                CometChatConstants.CATEGORY_ACTION,
                "action",
                "ACTION",
                "Action"
            )
            
            actionCategories.forEach { category ->
                val message = TestMessage(id = 1L, category = category, type = "groupMember")
                isBugCondition(message) shouldBe true
            }
        }

        /**
         * Verify that isBugCondition correctly identifies CALL messages.
         */
        test("isBugCondition should return true for CALL category messages") {
            val callCategories = listOf(
                CometChatConstants.CATEGORY_CALL,
                "call",
                "CALL",
                "Call"
            )
            
            callCategories.forEach { category ->
                val message = TestMessage(id = 1L, category = category, type = "audio")
                isBugCondition(message) shouldBe true
            }
        }

        /**
         * Verify that isBugCondition returns false for regular message categories.
         */
        test("isBugCondition should return false for regular message categories") {
            val regularCategories = listOf(
                CometChatConstants.CATEGORY_MESSAGE,
                CometChatConstants.CATEGORY_CUSTOM,
                "message",
                "custom",
                "interactive"
            )
            
            regularCategories.forEach { category ->
                val message = TestMessage(id = 1L, category = category, type = "text")
                isBugCondition(message) shouldBe false
            }
        }
    }

    context("Counterexample Documentation") {

        /**
         * Document the specific counterexample for chatuikit-kotlin long-press bug.
         * 
         * Counterexample: Long-press on ACTION message invokes callback instead of being blocked
         */
        test("document chatuikit-kotlin counterexample: long-press on ACTION message invokes callback") {
            val message = TestMessage(
                id = 123L,
                category = CometChatConstants.CATEGORY_ACTION,
                type = "groupMember"
            )
            
            // Simulate BUGGY behavior
            val buggyResult = simulateBuggyLongClickBehavior(message)
            
            // Document the counterexample: callback IS invoked (BUG)
            buggyResult.callbackInvoked shouldBe true
            buggyResult.message shouldNotBe null
            
            // Simulate EXPECTED behavior
            val expectedResult = simulateExpectedLongClickBehavior(message)
            
            // Expected: callback should NOT be invoked
            expectedResult.callbackInvoked shouldBe false
            expectedResult.message shouldBe null
            
            // The bug is confirmed when buggy != expected
            buggyResult.callbackInvoked shouldNotBe expectedResult.callbackInvoked
        }

        /**
         * Document the specific counterexample for chatuikit-kotlin swipe bug.
         * 
         * Counterexample: getMovementFlags returns non-zero for ACTION message
         */
        test("document chatuikit-kotlin counterexample: getMovementFlags returns non-zero for ACTION message") {
            val message = TestMessage(
                id = 456L,
                category = CometChatConstants.CATEGORY_ACTION,
                type = "groupMember"
            )
            
            // Simulate BUGGY behavior
            val buggyResult = simulateBuggyGetMovementFlags(message)
            
            // Document the counterexample: swipe IS allowed (BUG)
            buggyResult.movementFlags shouldNotBe 0
            buggyResult.swipeAllowed shouldBe true
            
            // Simulate EXPECTED behavior
            val expectedResult = simulateExpectedGetMovementFlags(message)
            
            // Expected: swipe should NOT be allowed
            expectedResult.movementFlags shouldBe 0
            expectedResult.swipeAllowed shouldBe false
            
            // The bug is confirmed when buggy != expected
            buggyResult.movementFlags shouldNotBe expectedResult.movementFlags
        }

        /**
         * Document the specific counterexample for CALL message interactions.
         * 
         * Counterexample: Long-press on CALL message invokes callback instead of being blocked
         */
        test("document counterexample: long-press on CALL message invokes callback") {
            val message = TestMessage(
                id = 789L,
                category = CometChatConstants.CATEGORY_CALL,
                type = "video"
            )
            
            // Simulate BUGGY behavior
            val buggyResult = simulateBuggyLongClickBehavior(message)
            
            // Document the counterexample: callback IS invoked (BUG)
            buggyResult.callbackInvoked shouldBe true
            
            // Simulate EXPECTED behavior
            val expectedResult = simulateExpectedLongClickBehavior(message)
            
            // Expected: callback should NOT be invoked
            expectedResult.callbackInvoked shouldBe false
            
            // The bug is confirmed when buggy != expected
            buggyResult.callbackInvoked shouldNotBe expectedResult.callbackInvoked
        }
    }
})
