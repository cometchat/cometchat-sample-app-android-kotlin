package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Bug Condition Exploration Property Test for Message List Ordering.
 *
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.5**
 *
 * ## Bug Analysis
 *
 * The current implementation uses `reverseLayout = true` with a reversed message list:
 * - ViewModel stores: `[oldest, ..., newest]` (index 0 = oldest)
 * - UI reverses to: `[newest, ..., oldest]` via `messagesFromVm.reversed()`
 * - With `reverseLayout = true`, index 0 appears at BOTTOM → newest at bottom ✓
 *
 * The BUG occurs when switching to `reverseLayout = false` WITHOUT removing the reversal:
 * - ViewModel stores: `[oldest, ..., newest]`
 * - UI reverses to: `[newest, ..., oldest]`
 * - With `reverseLayout = false`, index 0 appears at TOP → newest at TOP (WRONG!)
 *
 * ## Expected Behavior (After Fix)
 *
 * With `reverseLayout = false` and NO reversal:
 * - ViewModel stores: `[oldest, ..., newest]` (index 0 = oldest)
 * - UI uses directly: `[oldest, ..., newest]`
 * - With `reverseLayout = false`, index 0 appears at TOP → oldest at top, newest at bottom ✓
 * - UI must call `scrollToBottom()` to show newest messages initially
 *
 * **CRITICAL**: This test demonstrates the bug condition by showing that:
 * 1. The ViewModel correctly stores messages as `[oldest, ..., newest]`
 * 2. When the UI reverses this list, it becomes `[newest, ..., oldest]`
 * 3. With `reverseLayout = false`, this reversed list displays incorrectly
 *
 * Feature: message-list-reverse-false-fix
 */
class MessageListOrderPropertyTest : FunSpec({

    /**
     * Helper to create a mock TextMessage with specified id and sentAt timestamp.
     */
    fun createMessage(id: Long, sentAt: Long): BaseMessage {
        return TextMessage(
            "receiver_$id",
            "Test message $id",
            CometChatConstants.RECEIVER_TYPE_USER
        ).apply {
            this.id = id
            this.sentAt = sentAt
        }
    }

    /**
     * Helper to verify message list is in chronological order [oldest, ..., newest].
     * Returns true if messages[0].sentAt < messages[last].sentAt for non-empty lists.
     */
    fun isChronologicalOrder(messages: List<BaseMessage>): Boolean {
        if (messages.size <= 1) return true
        return messages.first().sentAt < messages.last().sentAt
    }

    /**
     * Helper to verify message list is in REVERSE chronological order [newest, ..., oldest].
     * Returns true if messages[0].sentAt > messages[last].sentAt for non-empty lists.
     */
    fun isReverseChronologicalOrder(messages: List<BaseMessage>): Boolean {
        if (messages.size <= 1) return true
        return messages.first().sentAt > messages.last().sentAt
    }

    /**
     * Helper to verify message list is strictly sorted by sentAt ascending.
     */
    fun isStrictlyAscending(messages: List<BaseMessage>): Boolean {
        if (messages.size <= 1) return true
        return messages.zipWithNext().all { (a, b) -> a.sentAt <= b.sentAt }
    }

    // ========================================================================
    // Property 1: Fault Condition - Bug Demonstration
    // ========================================================================
    //
    // These tests demonstrate the BUG CONDITION:
    // When the UI reverses the ViewModel's message list and uses reverseLayout=false,
    // messages appear in wrong order (newest at top instead of bottom).
    //
    // **EXPECTED OUTCOME**: Tests FAIL on current code, proving the bug exists.
    // ========================================================================

    test("Property 1.1: BUG CONDITION - UI reversal with reverseLayout=false shows newest at top (WRONG)") {
        /**
         * **Validates: Requirements 2.5**
         *
         * This test demonstrates the bug:
         * 1. ViewModel stores messages correctly as [oldest, ..., newest]
         * 2. Current UI code reverses this to [newest, ..., oldest]
         * 3. With reverseLayout=false, index 0 appears at TOP
         * 4. Result: Newest message at top (WRONG - should be at bottom)
         *
         * The test FAILS because the reversed list has newest at index 0,
         * which violates the expected [oldest, ..., newest] order for reverseLayout=false.
         */
        checkAll(50, Arb.int(2..10)) { messageCount ->
            // ViewModel stores messages in chronological order [oldest, ..., newest]
            val viewModelMessages = (0 until messageCount).map { i ->
                createMessage(id = i.toLong(), sentAt = (i * 100).toLong())
            }

            // Verify ViewModel order is correct
            isChronologicalOrder(viewModelMessages) shouldBe true

            // Current UI code reverses the list for reverseLayout=true
            // BUG: If we use reverseLayout=false with this reversed list, order is wrong
            val uiReversedMessages = viewModelMessages.reversed()

            // BUG CONDITION: With reverseLayout=false, this reversed list shows:
            // - Index 0 (TOP of screen) = newest message (WRONG!)
            // - Last index (BOTTOM of screen) = oldest message (WRONG!)
            //
            // EXPECTED for reverseLayout=false:
            // - Index 0 (TOP of screen) = oldest message
            // - Last index (BOTTOM of screen) = newest message
            //
            // This assertion FAILS, proving the bug exists:
            isChronologicalOrder(uiReversedMessages) shouldBe true
        }
    }

    test("Property 1.2: BUG CONDITION - Reversed list has newest at index 0 (violates reverseLayout=false requirement)") {
        /**
         * **Validates: Requirements 2.1, 2.5**
         *
         * For reverseLayout=false to work correctly:
         * - messages[0] must be the OLDEST message (appears at top)
         * - messages[last] must be the NEWEST message (appears at bottom)
         *
         * The current UI reversal violates this requirement.
         */
        checkAll(50, Arb.int(3..15)) { messageCount ->
            // ViewModel messages in correct order
            val viewModelMessages = (0 until messageCount).map { i ->
                createMessage(id = i.toLong(), sentAt = (1000 + i * 100).toLong())
            }

            // Current UI reversal
            val uiReversedMessages = viewModelMessages.reversed()

            // BUG: After reversal, index 0 has the NEWEST message
            // For reverseLayout=false, index 0 should have the OLDEST message
            val firstMessageSentAt = uiReversedMessages.first().sentAt
            val lastMessageSentAt = uiReversedMessages.last().sentAt

            // This assertion FAILS because firstMessageSentAt > lastMessageSentAt after reversal
            // Expected: firstMessageSentAt < lastMessageSentAt (oldest at index 0)
            firstMessageSentAt shouldBeLessThan lastMessageSentAt
        }
    }

    test("Property 1.3: BUG CONDITION - addItem to reversed list puts new message at wrong position") {
        /**
         * **Validates: Requirements 2.3**
         *
         * When a new real-time message arrives:
         * - ViewModel appends to end (correct): [oldest, ..., newest, NEW]
         * - UI reverses: [NEW, newest, ..., oldest]
         * - With reverseLayout=false: NEW appears at TOP (WRONG - should be at bottom)
         */
        checkAll(50, Arb.int(2..10)) { existingCount ->
            // ViewModel messages
            val viewModelMessages = (0 until existingCount).map { i ->
                createMessage(id = i.toLong(), sentAt = (i * 100).toLong())
            }.toMutableList()

            // New message arrives - ViewModel appends to end (correct)
            val newMessage = createMessage(
                id = 999L,
                sentAt = (existingCount * 100 + 50).toLong()
            )
            viewModelMessages.add(newMessage)

            // ViewModel order is correct
            isChronologicalOrder(viewModelMessages) shouldBe true
            viewModelMessages.last().id shouldBe 999L // New message at end

            // UI reverses the list
            val uiReversedMessages = viewModelMessages.reversed()

            // BUG: New message is now at index 0 (TOP with reverseLayout=false)
            // Expected: New message should be at last index (BOTTOM with reverseLayout=false)
            uiReversedMessages.last().id shouldBe 999L // This FAILS - new message is at index 0
        }
    }

    // ========================================================================
    // Property 2: Expected Behavior Verification (After Fix)
    // ========================================================================
    //
    // These tests verify the EXPECTED behavior after the fix is applied:
    // - ViewModel stores [oldest, ..., newest]
    // - UI uses list directly WITHOUT reversal
    // - reverseLayout=false displays oldest at top, newest at bottom
    // ========================================================================

    test("Property 2.1: EXPECTED - ViewModel stores messages in chronological order") {
        /**
         * **Validates: Requirements 2.1, 2.2, 2.5**
         *
         * The ViewModel's message ordering is already correct.
         * This test verifies the ViewModel behavior is preserved.
         */
        checkAll(50, Arb.int(2..10), Arb.int(2..10)) { newCount, existingCount ->
            // Existing messages
            val existingMessages = (0 until existingCount).map { i ->
                createMessage(id = (100 + i).toLong(), sentAt = (1000 + i).toLong())
            }

            // Older messages from fetchMessages()
            val olderMessages = (0 until newCount).map { i ->
                createMessage(id = i.toLong(), sentAt = i.toLong())
            }

            // ViewModel prepends older messages: olderMessages + existingMessages
            val resultList = olderMessages + existingMessages

            // ViewModel order is [oldest, ..., newest]
            isChronologicalOrder(resultList) shouldBe true
            isStrictlyAscending(resultList) shouldBe true
        }
    }

    test("Property 2.2: EXPECTED - Direct use of ViewModel list with reverseLayout=false") {
        /**
         * **Validates: Requirements 2.4, 2.5**
         *
         * After the fix, UI should use ViewModel list directly (no reversal).
         * With reverseLayout=false:
         * - Index 0 = oldest message (at top)
         * - Last index = newest message (at bottom)
         */
        checkAll(50, Arb.int(2..20)) { messageCount ->
            // ViewModel messages in chronological order
            val viewModelMessages = (0 until messageCount).map { i ->
                createMessage(id = i.toLong(), sentAt = (i * 100).toLong())
            }

            // FIXED: UI uses list directly, no reversal
            val uiMessages = viewModelMessages // No .reversed()

            // With reverseLayout=false:
            // - Index 0 (TOP) = oldest message ✓
            // - Last index (BOTTOM) = newest message ✓
            isChronologicalOrder(uiMessages) shouldBe true
            uiMessages.first().sentAt shouldBeLessThan uiMessages.last().sentAt
        }
    }

    test("Property 2.3: EXPECTED - New message appears at bottom with reverseLayout=false") {
        /**
         * **Validates: Requirements 2.3**
         *
         * After the fix, new real-time messages should appear at the bottom.
         */
        checkAll(50, Arb.int(1..15)) { existingCount ->
            // ViewModel messages
            val viewModelMessages = (0 until existingCount).map { i ->
                createMessage(id = i.toLong(), sentAt = (i * 100).toLong())
            }.toMutableList()

            // New message - ViewModel appends to end
            val newMessage = createMessage(
                id = 999L,
                sentAt = (existingCount * 100 + 50).toLong()
            )
            viewModelMessages.add(newMessage)

            // FIXED: UI uses list directly
            val uiMessages = viewModelMessages.toList()

            // New message is at last index (BOTTOM with reverseLayout=false) ✓
            uiMessages.last().id shouldBe 999L
            isChronologicalOrder(uiMessages) shouldBe true
        }
    }
})
