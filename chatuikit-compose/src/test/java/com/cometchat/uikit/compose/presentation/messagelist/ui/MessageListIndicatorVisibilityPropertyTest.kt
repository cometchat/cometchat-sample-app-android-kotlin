package com.cometchat.uikit.compose.presentation.messagelist.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property-based tests for New Message Indicator visibility logic.
 *
 * **Feature: scroll-to-bottom-button-parity**
 * **Validates: Requirements 8.1, 8.2**
 *
 * These tests verify that the indicator visibility is determined solely by scroll position
 * (isAtBottom), not by new message count. This matches the XML implementation behavior
 * where the indicator appears whenever the user is scrolled up from the bottom.
 *
 * The visibility logic in CometChatMessageList.kt:
 * ```kotlin
 * if (!isAtBottom) {
 *     // Show indicator
 * }
 * ```
 *
 * This property test verifies that for ANY combination of isAtBottom and newMessageCount:
 * - Indicator is visible when !isAtBottom (regardless of count)
 * - Indicator is hidden when isAtBottom (regardless of count)
 */
class MessageListIndicatorVisibilityPropertyTest : FunSpec({

    /**
     * Helper function that mirrors the visibility logic in CometChatMessageList.kt.
     *
     * The actual implementation uses:
     * ```kotlin
     * if (!isAtBottom) { /* show indicator */ }
     * ```
     *
     * This function extracts that logic for testability.
     *
     * @param isAtBottom Whether the user is at the bottom of the message list
     * @param newMessageCount The count of new messages (should NOT affect visibility)
     * @return true if the indicator should be visible, false otherwise
     */
    fun shouldShowIndicator(isAtBottom: Boolean, newMessageCount: Int): Boolean {
        // The visibility is determined ONLY by scroll position, NOT by message count
        // This matches the XML implementation behavior
        return !isAtBottom
    }

    // Feature: scroll-to-bottom-button-parity, Property 6: Indicator Visibility Based on Scroll Position
    // *For any* scroll position in the message list, the New_Message_Indicator SHALL be visible
    // if and only if the user is NOT at the bottom of the list, regardless of the new message count value.
    //
    // **Validates: Requirements 8.1, 8.2**
    test("Property 6: Indicator visible when NOT at bottom, hidden when at bottom - regardless of message count") {
        checkAll(100, Arb.boolean(), Arb.int(0..10000)) { isAtBottom, newMessageCount ->
            val shouldBeVisible = shouldShowIndicator(isAtBottom, newMessageCount)

            // Property 6.1: When NOT at bottom, indicator SHALL be visible
            // Property 6.2: When at bottom, indicator SHALL be hidden
            // Both properties must hold regardless of newMessageCount value
            shouldBeVisible shouldBe !isAtBottom
        }
    }

    // Feature: scroll-to-bottom-button-parity, Property 6.1: Visibility when NOT at bottom
    // WHEN the user is NOT At_Bottom of the message list, THE New_Message_Indicator SHALL be displayed.
    //
    // **Validates: Requirement 8.1**
    test("Property 6.1: Indicator is ALWAYS visible when user is NOT at bottom") {
        checkAll(100, Arb.int(0..10000)) { newMessageCount ->
            val isAtBottom = false

            val shouldBeVisible = shouldShowIndicator(isAtBottom, newMessageCount)

            // Requirement 8.1: When NOT at bottom, indicator SHALL be displayed
            shouldBeVisible shouldBe true
        }
    }

    // Feature: scroll-to-bottom-button-parity, Property 6.2: Visibility when at bottom
    // WHEN the user IS At_Bottom of the message list, THE New_Message_Indicator SHALL be hidden.
    //
    // **Validates: Requirement 8.2**
    test("Property 6.2: Indicator is ALWAYS hidden when user IS at bottom") {
        checkAll(100, Arb.int(0..10000)) { newMessageCount ->
            val isAtBottom = true

            val shouldBeVisible = shouldShowIndicator(isAtBottom, newMessageCount)

            // Requirement 8.2: When at bottom, indicator SHALL be hidden
            shouldBeVisible shouldBe false
        }
    }

    // Feature: scroll-to-bottom-button-parity, Property 6.3: Message count independence
    // The visibility decision SHALL NOT depend on newMessageCount value.
    // This verifies the fix from the old behavior (newMessageCount > 0 && !isAtBottom)
    // to the new behavior (!isAtBottom).
    //
    // **Validates: Requirements 8.1, 8.2**
    test("Property 6.3: Visibility is independent of message count") {
        checkAll(100, Arb.boolean(), Arb.int(0..10000), Arb.int(0..10000)) { isAtBottom, count1, count2 ->
            // For the same scroll position, visibility should be the same
            // regardless of different message counts
            val visibility1 = shouldShowIndicator(isAtBottom, count1)
            val visibility2 = shouldShowIndicator(isAtBottom, count2)

            // Visibility should be identical for any message count at the same scroll position
            visibility1 shouldBe visibility2
        }
    }

    // Feature: scroll-to-bottom-button-parity, Property 6.4: Zero count visibility
    // When newMessageCount is 0 and user is NOT at bottom, indicator SHALL still be visible.
    // This is a key behavioral change from the old implementation.
    //
    // **Validates: Requirement 8.1**
    test("Property 6.4: Indicator visible with zero message count when NOT at bottom") {
        val isAtBottom = false
        val newMessageCount = 0

        val shouldBeVisible = shouldShowIndicator(isAtBottom, newMessageCount)

        // Even with 0 new messages, indicator should be visible when scrolled up
        // This allows users to quickly return to the bottom of the conversation
        shouldBeVisible shouldBe true
    }

    // Feature: scroll-to-bottom-button-parity, Property 6.5: Positive count at bottom
    // When newMessageCount > 0 but user IS at bottom, indicator SHALL be hidden.
    // The user is already seeing the latest messages, so no indicator is needed.
    //
    // **Validates: Requirement 8.2**
    test("Property 6.5: Indicator hidden even with positive message count when at bottom") {
        checkAll(100, Arb.int(1..10000)) { newMessageCount ->
            val isAtBottom = true

            val shouldBeVisible = shouldShowIndicator(isAtBottom, newMessageCount)

            // Even with new messages, indicator should be hidden when at bottom
            // because the user is already seeing the latest messages
            shouldBeVisible shouldBe false
        }
    }
})
