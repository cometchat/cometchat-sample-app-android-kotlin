package com.cometchat.uikit.compose.presentation.messagelist.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property-based tests for verifying pagination logic in the message list.
 *
 * **Feature: message-list-pagination-direction**
 *
 * These tests verify the bidirectional pagination decision logic used in
 * CometChatMessageList. The pagination logic determines when to fetch
 * older or newer messages based on scroll position and state flags.
 *
 * With reverseLayout=true in LazyColumn:
 * - Index 0 = newest message (at bottom visually)
 * - Last index = oldest message (at top visually)
 * - Scrolling UP increases lastVisibleIndex toward totalItems-1
 * - Scrolling DOWN decreases firstVisibleIndex toward 0
 *
 * Pagination Threshold: 5 items from the edge
 */
class MessageListPaginationPropertyTest : StringSpec({

    // ============================================================================
    // Test Data Generator
    // ============================================================================

    /**
     * Data class representing the scroll state of the message list.
     *
     * @property totalItems Total number of items in the list
     * @property firstVisibleIndex Index of the first visible item (closest to bottom with reverseLayout=true)
     * @property lastVisibleIndex Index of the last visible item (closest to top with reverseLayout=true)
     * @property hasMoreNewMessages Whether more newer messages are available to fetch
     * @property hasMorePreviousMessages Whether more older messages are available to fetch
     * @property isInProgress Whether a fetch operation is currently in progress
     */
    data class ScrollState(
        val totalItems: Int,
        val firstVisibleIndex: Int,
        val lastVisibleIndex: Int,
        val hasMoreNewMessages: Boolean,
        val hasMorePreviousMessages: Boolean,
        val isInProgress: Boolean
    )

    /**
     * Arbitrary generator for scroll states.
     *
     * Generates valid scroll states where:
     * - totalItems is in range [0, 100]
     * - firstVisibleIndex is valid for the totalItems
     * - lastVisibleIndex is >= firstVisibleIndex and valid for totalItems
     * - Boolean flags are randomly generated
     */
    fun Arb.Companion.scrollState(): Arb<ScrollState> = arbitrary {
        val totalItems = Arb.int(0, 100).bind()
        val firstVisibleIndex = if (totalItems > 0) Arb.int(0, totalItems - 1).bind() else 0
        val visibleCount = Arb.int(1, minOf(10, totalItems.coerceAtLeast(1))).bind()
        val lastVisibleIndex = minOf(firstVisibleIndex + visibleCount - 1, totalItems - 1).coerceAtLeast(firstVisibleIndex)

        ScrollState(
            totalItems = totalItems,
            firstVisibleIndex = firstVisibleIndex,
            lastVisibleIndex = lastVisibleIndex,
            hasMoreNewMessages = Arb.boolean().bind(),
            hasMorePreviousMessages = Arb.boolean().bind(),
            isInProgress = Arb.boolean().bind()
        )
    }

    /**
     * Arbitrary generator for scroll states specifically near the bottom.
     *
     * Generates scroll states where firstVisibleIndex is in range [0, 10]
     * to test newer message pagination trigger conditions.
     */
    fun Arb.Companion.scrollStateNearBottom(): Arb<ScrollState> = arbitrary {
        val totalItems = Arb.int(11, 100).bind() // Ensure enough items
        val firstVisibleIndex = Arb.int(0, 10).bind() // Near bottom
        val visibleCount = Arb.int(1, minOf(10, totalItems - firstVisibleIndex)).bind()
        val lastVisibleIndex = minOf(firstVisibleIndex + visibleCount - 1, totalItems - 1)

        ScrollState(
            totalItems = totalItems,
            firstVisibleIndex = firstVisibleIndex,
            lastVisibleIndex = lastVisibleIndex,
            hasMoreNewMessages = Arb.boolean().bind(),
            hasMorePreviousMessages = Arb.boolean().bind(),
            isInProgress = Arb.boolean().bind()
        )
    }

    // ============================================================================
    // Property 1: Newer Message Pagination Triggers Correctly
    // ============================================================================

    /**
     * **Property 1: Newer Message Pagination Triggers Correctly**
     *
     * *For any* scroll state where `firstVisibleIndex <= 5` AND `hasMoreNewMessages == true`
     * AND `isInProgress == false` AND `totalItems > 0`, the pagination effect SHALL call
     * `fetchNextMessages()`.
     *
     * **Feature: message-list-pagination-direction, Property 1: Newer Message Pagination Triggers Correctly**
     *
     * **Validates: Requirements 1.1, 3.2**
     */
    "Property 1: Newer message pagination triggers when all conditions are met" {
        checkAll(100, Arb.scrollState()) { state ->
            val result = shouldFetchNewerMessages(
                totalItems = state.totalItems,
                firstVisibleIndex = state.firstVisibleIndex,
                hasMoreNewMessages = state.hasMoreNewMessages,
                isInProgress = state.isInProgress
            )

            // Calculate expected result based on all conditions
            val allConditionsMet = state.firstVisibleIndex <= PAGINATION_THRESHOLD &&
                    state.hasMoreNewMessages &&
                    !state.isInProgress &&
                    state.totalItems > 0

            result shouldBe allConditionsMet
        }
    }

    /**
     * Property 1 (specific): When firstVisibleIndex is exactly at threshold (5),
     * pagination should trigger if other conditions are met.
     *
     * **Validates: Requirements 1.1, 3.2**
     */
    "Property 1 (specific): firstVisibleIndex at threshold (5) should trigger pagination" {
        val result = shouldFetchNewerMessages(
            totalItems = 50,
            firstVisibleIndex = 5, // Exactly at threshold
            hasMoreNewMessages = true,
            isInProgress = false
        )

        result shouldBe true
    }

    /**
     * Property 1 (specific): When firstVisibleIndex is below threshold (0-4),
     * pagination should trigger if other conditions are met.
     *
     * **Validates: Requirements 1.1, 3.2**
     */
    "Property 1 (specific): firstVisibleIndex below threshold should trigger pagination" {
        checkAll(100, Arb.int(0, 4)) { firstVisibleIndex ->
            val result = shouldFetchNewerMessages(
                totalItems = 50,
                firstVisibleIndex = firstVisibleIndex,
                hasMoreNewMessages = true,
                isInProgress = false
            )

            result shouldBe true
        }
    }

    /**
     * Property 1 (specific): When firstVisibleIndex is above threshold (> 5),
     * pagination should NOT trigger even if other conditions are met.
     *
     * **Validates: Requirements 1.1, 3.2**
     */
    "Property 1 (specific): firstVisibleIndex above threshold should NOT trigger pagination" {
        checkAll(100, Arb.int(6, 50)) { firstVisibleIndex ->
            val result = shouldFetchNewerMessages(
                totalItems = 100,
                firstVisibleIndex = firstVisibleIndex,
                hasMoreNewMessages = true,
                isInProgress = false
            )

            result shouldBe false
        }
    }

    /**
     * Property 1 (specific): When at the very bottom (index 0 with reverseLayout=true),
     * pagination should trigger if hasMoreNewMessages is true.
     *
     * This tests the edge case from Requirements 4.2.
     *
     * **Validates: Requirements 1.1, 3.2, 4.2**
     */
    "Property 1 (edge case): At bottom (index 0) should trigger newer message pagination" {
        val result = shouldFetchNewerMessages(
            totalItems = 50,
            firstVisibleIndex = 0, // At the very bottom
            hasMoreNewMessages = true,
            isInProgress = false
        )

        result shouldBe true
    }

    /**
     * Property 1 (guard): When hasMoreNewMessages is false, pagination should NOT trigger
     * regardless of scroll position.
     *
     * **Validates: Requirements 1.3**
     */
    "Property 1 (guard): hasMoreNewMessages=false should prevent pagination" {
        checkAll(100, Arb.int(0, 5)) { firstVisibleIndex ->
            val result = shouldFetchNewerMessages(
                totalItems = 50,
                firstVisibleIndex = firstVisibleIndex,
                hasMoreNewMessages = false, // Guard condition
                isInProgress = false
            )

            result shouldBe false
        }
    }

    /**
     * Property 1 (guard): When isInProgress is true, pagination should NOT trigger
     * regardless of scroll position.
     *
     * **Validates: Requirements 1.5**
     */
    "Property 1 (guard): isInProgress=true should prevent pagination" {
        checkAll(100, Arb.int(0, 5)) { firstVisibleIndex ->
            val result = shouldFetchNewerMessages(
                totalItems = 50,
                firstVisibleIndex = firstVisibleIndex,
                hasMoreNewMessages = true,
                isInProgress = true // Guard condition
            )

            result shouldBe false
        }
    }

    /**
     * Property 1 (guard): When totalItems is 0 (empty list), pagination should NOT trigger.
     *
     * **Validates: Requirements 4.1**
     */
    "Property 1 (guard): Empty list (totalItems=0) should prevent pagination" {
        val result = shouldFetchNewerMessages(
            totalItems = 0, // Empty list
            firstVisibleIndex = 0,
            hasMoreNewMessages = true,
            isInProgress = false
        )

        result shouldBe false
    }

    /**
     * Property 1 (exhaustive): All guard condition combinations should be respected.
     *
     * Tests all 8 combinations of the three boolean-like conditions:
     * - hasMoreNewMessages (true/false)
     * - isInProgress (true/false)
     * - totalItems > 0 (true/false)
     *
     * **Validates: Requirements 1.1, 1.3, 1.5, 4.1**
     */
    "Property 1 (exhaustive): All guard condition combinations" {
        val combinations = listOf(
            // (hasMoreNewMessages, isInProgress, totalItems > 0) -> expected
            Triple(false, false, false) to false, // No messages, not in progress, empty list
            Triple(false, false, true) to false,  // No messages, not in progress, has items
            Triple(false, true, false) to false,  // No messages, in progress, empty list
            Triple(false, true, true) to false,   // No messages, in progress, has items
            Triple(true, false, false) to false,  // Has messages, not in progress, empty list
            Triple(true, false, true) to true,    // Has messages, not in progress, has items -> SHOULD TRIGGER
            Triple(true, true, false) to false,   // Has messages, in progress, empty list
            Triple(true, true, true) to false     // Has messages, in progress, has items
        )

        combinations.forEach { (conditions, expected) ->
            val (hasMoreNewMessages, isInProgress, hasItems) = conditions
            val result = shouldFetchNewerMessages(
                totalItems = if (hasItems) 50 else 0,
                firstVisibleIndex = 0, // At threshold
                hasMoreNewMessages = hasMoreNewMessages,
                isInProgress = isInProgress
            )

            result shouldBe expected
        }
    }

    /**
     * Property 1 (comprehensive): For any scroll state near the bottom,
     * the pagination decision should correctly evaluate all conditions.
     *
     * **Validates: Requirements 1.1, 3.2**
     */
    "Property 1 (comprehensive): Scroll states near bottom should correctly evaluate conditions" {
        checkAll(100, Arb.scrollStateNearBottom()) { state ->
            val result = shouldFetchNewerMessages(
                totalItems = state.totalItems,
                firstVisibleIndex = state.firstVisibleIndex,
                hasMoreNewMessages = state.hasMoreNewMessages,
                isInProgress = state.isInProgress
            )

            // Expected: all conditions must be met
            val expected = state.firstVisibleIndex <= PAGINATION_THRESHOLD &&
                    state.hasMoreNewMessages &&
                    !state.isInProgress &&
                    state.totalItems > 0

            result shouldBe expected
        }
    }

    /**
     * Property 1 (threshold boundary): Tests the exact boundary at threshold.
     *
     * - firstVisibleIndex = 5 should trigger (at threshold)
     * - firstVisibleIndex = 6 should NOT trigger (above threshold)
     *
     * **Validates: Requirements 3.2**
     */
    "Property 1 (boundary): Threshold boundary at 5 should be inclusive" {
        // At threshold (5) - should trigger
        val atThreshold = shouldFetchNewerMessages(
            totalItems = 50,
            firstVisibleIndex = 5,
            hasMoreNewMessages = true,
            isInProgress = false
        )
        atThreshold shouldBe true

        // Just above threshold (6) - should NOT trigger
        val aboveThreshold = shouldFetchNewerMessages(
            totalItems = 50,
            firstVisibleIndex = 6,
            hasMoreNewMessages = true,
            isInProgress = false
        )
        aboveThreshold shouldBe false
    }

    // ============================================================================
    // Property 2: Older Message Pagination Triggers Correctly
    // ============================================================================

    /**
     * Arbitrary generator for scroll states specifically near the top.
     *
     * Generates scroll states where lastVisibleIndex is near totalItems - 1
     * to test older message pagination trigger conditions.
     */
    fun Arb.Companion.scrollStateNearTop(): Arb<ScrollState> = arbitrary {
        val totalItems = Arb.int(11, 100).bind() // Ensure enough items
        val lastVisibleIndex = Arb.int(totalItems - 6, totalItems - 1).bind() // Near top
        val visibleCount = Arb.int(1, minOf(10, lastVisibleIndex + 1)).bind()
        val firstVisibleIndex = maxOf(0, lastVisibleIndex - visibleCount + 1)

        ScrollState(
            totalItems = totalItems,
            firstVisibleIndex = firstVisibleIndex,
            lastVisibleIndex = lastVisibleIndex,
            hasMoreNewMessages = Arb.boolean().bind(),
            hasMorePreviousMessages = Arb.boolean().bind(),
            isInProgress = Arb.boolean().bind()
        )
    }

    /**
     * **Property 2: Older Message Pagination Triggers Correctly**
     *
     * *For any* scroll state where `lastVisibleIndex >= totalItems - 5` AND `hasMorePreviousMessages == true`
     * AND `isInProgress == false` AND `totalItems > 0`, the pagination effect SHALL call
     * `fetchMessages()`.
     *
     * **Feature: message-list-pagination-direction, Property 2: Older Message Pagination Triggers Correctly**
     *
     * **Validates: Requirements 1.2, 3.3**
     */
    "Property 2: Older message pagination triggers when all conditions are met" {
        checkAll(100, Arb.scrollState()) { state ->
            val result = shouldFetchOlderMessages(
                totalItems = state.totalItems,
                lastVisibleIndex = state.lastVisibleIndex,
                hasMorePreviousMessages = state.hasMorePreviousMessages,
                isInProgress = state.isInProgress
            )

            // Calculate expected result based on all conditions
            val allConditionsMet = state.lastVisibleIndex >= state.totalItems - PAGINATION_THRESHOLD &&
                    state.hasMorePreviousMessages &&
                    !state.isInProgress &&
                    state.totalItems > 0

            result shouldBe allConditionsMet
        }
    }

    /**
     * Property 2 (specific): When lastVisibleIndex is exactly at threshold (totalItems - 5),
     * pagination should trigger if other conditions are met.
     *
     * **Validates: Requirements 1.2, 3.3**
     */
    "Property 2 (specific): lastVisibleIndex at threshold (totalItems - 5) should trigger pagination" {
        val totalItems = 50
        val result = shouldFetchOlderMessages(
            totalItems = totalItems,
            lastVisibleIndex = totalItems - 5, // Exactly at threshold (45)
            hasMorePreviousMessages = true,
            isInProgress = false
        )

        result shouldBe true
    }

    /**
     * Property 2 (specific): When lastVisibleIndex is above threshold (closer to totalItems - 1),
     * pagination should trigger if other conditions are met.
     *
     * **Validates: Requirements 1.2, 3.3**
     */
    "Property 2 (specific): lastVisibleIndex above threshold should trigger pagination" {
        checkAll(100, Arb.int(46, 49)) { lastVisibleIndex ->
            val totalItems = 50
            val result = shouldFetchOlderMessages(
                totalItems = totalItems,
                lastVisibleIndex = lastVisibleIndex, // Above threshold (46-49)
                hasMorePreviousMessages = true,
                isInProgress = false
            )

            result shouldBe true
        }
    }

    /**
     * Property 2 (specific): When lastVisibleIndex is below threshold (< totalItems - 5),
     * pagination should NOT trigger even if other conditions are met.
     *
     * **Validates: Requirements 1.2, 3.3**
     */
    "Property 2 (specific): lastVisibleIndex below threshold should NOT trigger pagination" {
        checkAll(100, Arb.int(0, 44)) { lastVisibleIndex ->
            val totalItems = 50
            val result = shouldFetchOlderMessages(
                totalItems = totalItems,
                lastVisibleIndex = lastVisibleIndex, // Below threshold (0-44)
                hasMorePreviousMessages = true,
                isInProgress = false
            )

            result shouldBe false
        }
    }

    /**
     * Property 2 (specific): When at the very top (lastVisibleIndex = totalItems - 1),
     * pagination should trigger if hasMorePreviousMessages is true.
     *
     * **Validates: Requirements 1.2, 3.3**
     */
    "Property 2 (edge case): At top (lastVisibleIndex = totalItems - 1) should trigger older message pagination" {
        val totalItems = 50
        val result = shouldFetchOlderMessages(
            totalItems = totalItems,
            lastVisibleIndex = totalItems - 1, // At the very top (49)
            hasMorePreviousMessages = true,
            isInProgress = false
        )

        result shouldBe true
    }

    /**
     * Property 2 (guard): When hasMorePreviousMessages is false, pagination should NOT trigger
     * regardless of scroll position.
     *
     * **Validates: Requirements 1.4**
     */
    "Property 2 (guard): hasMorePreviousMessages=false should prevent pagination" {
        checkAll(100, Arb.int(45, 49)) { lastVisibleIndex ->
            val totalItems = 50
            val result = shouldFetchOlderMessages(
                totalItems = totalItems,
                lastVisibleIndex = lastVisibleIndex,
                hasMorePreviousMessages = false, // Guard condition
                isInProgress = false
            )

            result shouldBe false
        }
    }

    /**
     * Property 2 (guard): When isInProgress is true, pagination should NOT trigger
     * regardless of scroll position.
     *
     * **Validates: Requirements 1.5**
     */
    "Property 2 (guard): isInProgress=true should prevent older message pagination" {
        checkAll(100, Arb.int(45, 49)) { lastVisibleIndex ->
            val totalItems = 50
            val result = shouldFetchOlderMessages(
                totalItems = totalItems,
                lastVisibleIndex = lastVisibleIndex,
                hasMorePreviousMessages = true,
                isInProgress = true // Guard condition
            )

            result shouldBe false
        }
    }

    /**
     * Property 2 (guard): When totalItems is 0 (empty list), pagination should NOT trigger.
     *
     * **Validates: Requirements 4.1**
     */
    "Property 2 (guard): Empty list (totalItems=0) should prevent older message pagination" {
        val result = shouldFetchOlderMessages(
            totalItems = 0, // Empty list
            lastVisibleIndex = 0,
            hasMorePreviousMessages = true,
            isInProgress = false
        )

        result shouldBe false
    }

    /**
     * Property 2 (exhaustive): All guard condition combinations should be respected.
     *
     * Tests all 8 combinations of the three boolean-like conditions:
     * - hasMorePreviousMessages (true/false)
     * - isInProgress (true/false)
     * - totalItems > 0 (true/false)
     *
     * **Validates: Requirements 1.2, 1.4, 1.5, 4.1**
     */
    "Property 2 (exhaustive): All guard condition combinations for older messages" {
        val combinations = listOf(
            // (hasMorePreviousMessages, isInProgress, totalItems > 0) -> expected
            Triple(false, false, false) to false, // No messages, not in progress, empty list
            Triple(false, false, true) to false,  // No messages, not in progress, has items
            Triple(false, true, false) to false,  // No messages, in progress, empty list
            Triple(false, true, true) to false,   // No messages, in progress, has items
            Triple(true, false, false) to false,  // Has messages, not in progress, empty list
            Triple(true, false, true) to true,    // Has messages, not in progress, has items -> SHOULD TRIGGER
            Triple(true, true, false) to false,   // Has messages, in progress, empty list
            Triple(true, true, true) to false     // Has messages, in progress, has items
        )

        combinations.forEach { (conditions, expected) ->
            val (hasMorePreviousMessages, isInProgress, hasItems) = conditions
            val totalItems = if (hasItems) 50 else 0
            val lastVisibleIndex = if (hasItems) totalItems - 1 else 0 // At threshold when has items
            val result = shouldFetchOlderMessages(
                totalItems = totalItems,
                lastVisibleIndex = lastVisibleIndex,
                hasMorePreviousMessages = hasMorePreviousMessages,
                isInProgress = isInProgress
            )

            result shouldBe expected
        }
    }

    /**
     * Property 2 (comprehensive): For any scroll state near the top,
     * the pagination decision should correctly evaluate all conditions.
     *
     * **Validates: Requirements 1.2, 3.3**
     */
    "Property 2 (comprehensive): Scroll states near top should correctly evaluate conditions" {
        checkAll(100, Arb.scrollStateNearTop()) { state ->
            val result = shouldFetchOlderMessages(
                totalItems = state.totalItems,
                lastVisibleIndex = state.lastVisibleIndex,
                hasMorePreviousMessages = state.hasMorePreviousMessages,
                isInProgress = state.isInProgress
            )

            // Expected: all conditions must be met
            val expected = state.lastVisibleIndex >= state.totalItems - PAGINATION_THRESHOLD &&
                    state.hasMorePreviousMessages &&
                    !state.isInProgress &&
                    state.totalItems > 0

            result shouldBe expected
        }
    }

    /**
     * Property 2 (threshold boundary): Tests the exact boundary at threshold.
     *
     * - lastVisibleIndex = totalItems - 5 should trigger (at threshold)
     * - lastVisibleIndex = totalItems - 6 should NOT trigger (below threshold)
     *
     * **Validates: Requirements 3.3**
     */
    "Property 2 (boundary): Threshold boundary at totalItems - 5 should be inclusive" {
        val totalItems = 50

        // At threshold (totalItems - 5 = 45) - should trigger
        val atThreshold = shouldFetchOlderMessages(
            totalItems = totalItems,
            lastVisibleIndex = totalItems - 5, // 45
            hasMorePreviousMessages = true,
            isInProgress = false
        )
        atThreshold shouldBe true

        // Just below threshold (totalItems - 6 = 44) - should NOT trigger
        val belowThreshold = shouldFetchOlderMessages(
            totalItems = totalItems,
            lastVisibleIndex = totalItems - 6, // 44
            hasMorePreviousMessages = true,
            isInProgress = false
        )
        belowThreshold shouldBe false
    }

    /**
     * Property 2 (small list): When totalItems is small (e.g., 6 items),
     * the threshold calculation should still work correctly.
     *
     * With totalItems = 6, threshold is at index 1 (6 - 5 = 1).
     *
     * **Validates: Requirements 1.2, 3.3**
     */
    "Property 2 (small list): Small list threshold calculation should work correctly" {
        val totalItems = 6

        // lastVisibleIndex = 1 (at threshold 6 - 5 = 1) - should trigger
        val atThreshold = shouldFetchOlderMessages(
            totalItems = totalItems,
            lastVisibleIndex = 1,
            hasMorePreviousMessages = true,
            isInProgress = false
        )
        atThreshold shouldBe true

        // lastVisibleIndex = 0 (below threshold) - should NOT trigger
        val belowThreshold = shouldFetchOlderMessages(
            totalItems = totalItems,
            lastVisibleIndex = 0,
            hasMorePreviousMessages = true,
            isInProgress = false
        )
        belowThreshold shouldBe false
    }

    /**
     * Property 2 (very small list): When totalItems is less than or equal to threshold (5),
     * any lastVisibleIndex should trigger pagination (since totalItems - 5 <= 0).
     *
     * **Validates: Requirements 1.2, 3.3**
     */
    "Property 2 (very small list): List with 5 or fewer items should always trigger at any position" {
        // With totalItems = 5, threshold is at index 0 (5 - 5 = 0)
        // Any lastVisibleIndex >= 0 should trigger
        listOf(1, 2, 3, 4, 5).forEach { totalItems ->
            (0 until totalItems).forEach { lastVisibleIndex ->
                val result = shouldFetchOlderMessages(
                    totalItems = totalItems,
                    lastVisibleIndex = lastVisibleIndex,
                    hasMorePreviousMessages = true,
                    isInProgress = false
                )

                // For small lists, lastVisibleIndex >= totalItems - 5 is always true
                // when lastVisibleIndex >= 0 and totalItems <= 5
                val expected = lastVisibleIndex >= totalItems - PAGINATION_THRESHOLD
                result shouldBe expected
            }
        }
    }

    // ============================================================================
    // Property 3: Guard Conditions Prevent Inappropriate Fetches
    // ============================================================================

    /**
     * **Property 3: Guard Conditions Prevent Inappropriate Fetches**
     *
     * *For any* scroll state:
     * - IF `hasMoreNewMessages == false`, THEN `fetchNextMessages()` SHALL NOT be called
     * - IF `hasMorePreviousMessages == false`, THEN `fetchMessages()` SHALL NOT be called
     * - IF `isInProgress == true`, THEN neither `fetchMessages()` nor `fetchNextMessages()` SHALL be called
     *
     * **Feature: message-list-pagination-direction, Property 3: Guard Conditions Prevent Inappropriate Fetches**
     *
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    "Property 3: Guard conditions prevent inappropriate fetches for any scroll state" {
        checkAll(100, Arb.scrollState()) { state ->
            val result = determinePaginationAction(
                totalItems = state.totalItems,
                firstVisibleIndex = state.firstVisibleIndex,
                lastVisibleIndex = state.lastVisibleIndex,
                hasMoreNewMessages = state.hasMoreNewMessages,
                hasMorePreviousMessages = state.hasMorePreviousMessages,
                isInProgress = state.isInProgress
            )

            // Guard: isInProgress should always result in NoAction
            if (state.isInProgress) {
                result shouldBe PaginationDecision.NoAction
            }

            // Guard: Empty list should always result in NoAction
            if (state.totalItems <= 0) {
                result shouldBe PaginationDecision.NoAction
            }

            // Guard: FetchNewerMessages should never be returned when hasMoreNewMessages is false
            if (!state.hasMoreNewMessages) {
                result shouldBe (if (result == PaginationDecision.FetchNewerMessages) {
                    throw AssertionError("FetchNewerMessages returned when hasMoreNewMessages is false")
                } else result)
            }

            // Guard: FetchOlderMessages should never be returned when hasMorePreviousMessages is false
            if (!state.hasMorePreviousMessages) {
                result shouldBe (if (result == PaginationDecision.FetchOlderMessages) {
                    throw AssertionError("FetchOlderMessages returned when hasMorePreviousMessages is false")
                } else result)
            }
        }
    }

    /**
     * Property 3 (specific): When isInProgress is true, determinePaginationAction
     * should always return NoAction regardless of scroll position or other flags.
     *
     * **Validates: Requirements 1.5**
     */
    "Property 3 (specific): isInProgress=true always returns NoAction" {
        checkAll(100, Arb.scrollState()) { state ->
            val result = determinePaginationAction(
                totalItems = state.totalItems,
                firstVisibleIndex = state.firstVisibleIndex,
                lastVisibleIndex = state.lastVisibleIndex,
                hasMoreNewMessages = state.hasMoreNewMessages,
                hasMorePreviousMessages = state.hasMorePreviousMessages,
                isInProgress = true // Force isInProgress to true
            )

            result shouldBe PaginationDecision.NoAction
        }
    }

    /**
     * Property 3 (specific): When hasMoreNewMessages is false, FetchNewerMessages
     * should never be returned even when near the bottom.
     *
     * **Validates: Requirements 1.3**
     */
    "Property 3 (specific): hasMoreNewMessages=false prevents FetchNewerMessages" {
        checkAll(100, Arb.int(0, PAGINATION_THRESHOLD)) { firstVisibleIndex ->
            val result = determinePaginationAction(
                totalItems = 50,
                firstVisibleIndex = firstVisibleIndex, // Near bottom
                lastVisibleIndex = firstVisibleIndex + 5, // Not near top
                hasMoreNewMessages = false, // Guard condition
                hasMorePreviousMessages = false,
                isInProgress = false
            )

            // Should be NoAction since hasMoreNewMessages is false
            result shouldBe PaginationDecision.NoAction
        }
    }

    /**
     * Property 3 (specific): When hasMorePreviousMessages is false, FetchOlderMessages
     * should never be returned even when near the top.
     *
     * **Validates: Requirements 1.4**
     */
    "Property 3 (specific): hasMorePreviousMessages=false prevents FetchOlderMessages" {
        checkAll(100, Arb.int(45, 49)) { lastVisibleIndex ->
            val totalItems = 50
            val result = determinePaginationAction(
                totalItems = totalItems,
                firstVisibleIndex = lastVisibleIndex - 5, // Not near bottom
                lastVisibleIndex = lastVisibleIndex, // Near top
                hasMoreNewMessages = false,
                hasMorePreviousMessages = false, // Guard condition
                isInProgress = false
            )

            // Should be NoAction since hasMorePreviousMessages is false
            result shouldBe PaginationDecision.NoAction
        }
    }

    /**
     * Property 3 (exhaustive): All guard condition combinations using determinePaginationAction.
     *
     * Tests all combinations of guard conditions to ensure they are properly enforced.
     *
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    "Property 3 (exhaustive): All guard condition combinations with determinePaginationAction" {
        // Test with scroll position near bottom (would trigger newer messages if allowed)
        val nearBottomCombinations = listOf(
            // (hasMoreNewMessages, hasMorePreviousMessages, isInProgress) -> expected
            Triple(false, false, false) to PaginationDecision.NoAction,
            Triple(false, false, true) to PaginationDecision.NoAction,
            Triple(false, true, false) to PaginationDecision.NoAction, // Not near top, so NoAction
            Triple(false, true, true) to PaginationDecision.NoAction,
            Triple(true, false, false) to PaginationDecision.FetchNewerMessages,
            Triple(true, false, true) to PaginationDecision.NoAction, // isInProgress blocks
            Triple(true, true, false) to PaginationDecision.FetchNewerMessages, // Not near top
            Triple(true, true, true) to PaginationDecision.NoAction // isInProgress blocks
        )

        nearBottomCombinations.forEach { (conditions, expected) ->
            val (hasMoreNewMessages, hasMorePreviousMessages, isInProgress) = conditions
            val result = determinePaginationAction(
                totalItems = 50,
                firstVisibleIndex = 0, // Near bottom
                lastVisibleIndex = 5, // Not near top (threshold is 45)
                hasMoreNewMessages = hasMoreNewMessages,
                hasMorePreviousMessages = hasMorePreviousMessages,
                isInProgress = isInProgress
            )

            result shouldBe expected
        }
    }

    /**
     * Property 3 (exhaustive): Guard conditions when near top.
     *
     * Tests all combinations of guard conditions when scroll position is near the top.
     *
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    "Property 3 (exhaustive): All guard condition combinations when near top" {
        // Test with scroll position near top (would trigger older messages if allowed)
        val nearTopCombinations = listOf(
            // (hasMoreNewMessages, hasMorePreviousMessages, isInProgress) -> expected
            Triple(false, false, false) to PaginationDecision.NoAction,
            Triple(false, false, true) to PaginationDecision.NoAction,
            Triple(false, true, false) to PaginationDecision.FetchOlderMessages,
            Triple(false, true, true) to PaginationDecision.NoAction, // isInProgress blocks
            Triple(true, false, false) to PaginationDecision.NoAction, // Not near bottom
            Triple(true, false, true) to PaginationDecision.NoAction,
            Triple(true, true, false) to PaginationDecision.FetchOlderMessages, // Priority: older first
            Triple(true, true, true) to PaginationDecision.NoAction // isInProgress blocks
        )

        nearTopCombinations.forEach { (conditions, expected) ->
            val (hasMoreNewMessages, hasMorePreviousMessages, isInProgress) = conditions
            val result = determinePaginationAction(
                totalItems = 50,
                firstVisibleIndex = 40, // Not near bottom
                lastVisibleIndex = 49, // Near top (>= 50 - 5 = 45)
                hasMoreNewMessages = hasMoreNewMessages,
                hasMorePreviousMessages = hasMorePreviousMessages,
                isInProgress = isInProgress
            )

            result shouldBe expected
        }
    }

    /**
     * Property 3 (empty list): Empty list guard condition.
     *
     * When totalItems is 0, determinePaginationAction should always return NoAction
     * regardless of other conditions.
     *
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    "Property 3 (empty list): Empty list always returns NoAction" {
        checkAll(100, Arb.boolean(), Arb.boolean(), Arb.boolean()) { hasMoreNew, hasMorePrev, inProgress ->
            val result = determinePaginationAction(
                totalItems = 0, // Empty list
                firstVisibleIndex = 0,
                lastVisibleIndex = 0,
                hasMoreNewMessages = hasMoreNew,
                hasMorePreviousMessages = hasMorePrev,
                isInProgress = inProgress
            )

            result shouldBe PaginationDecision.NoAction
        }
    }

    /**
     * Property 3 (comprehensive): For any scroll state, guard conditions are respected.
     *
     * This test verifies that the determinePaginationAction function correctly
     * enforces all guard conditions across randomly generated scroll states.
     *
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    "Property 3 (comprehensive): Guard conditions are always respected" {
        checkAll(100, Arb.scrollState()) { state ->
            val result = determinePaginationAction(
                totalItems = state.totalItems,
                firstVisibleIndex = state.firstVisibleIndex,
                lastVisibleIndex = state.lastVisibleIndex,
                hasMoreNewMessages = state.hasMoreNewMessages,
                hasMorePreviousMessages = state.hasMorePreviousMessages,
                isInProgress = state.isInProgress
            )

            // Verify guard conditions are respected
            when (result) {
                PaginationDecision.FetchNewerMessages -> {
                    // FetchNewerMessages requires: !isInProgress && totalItems > 0 && hasMoreNewMessages
                    state.isInProgress shouldBe false
                    (state.totalItems > 0) shouldBe true
                    state.hasMoreNewMessages shouldBe true
                }
                PaginationDecision.FetchOlderMessages -> {
                    // FetchOlderMessages requires: !isInProgress && totalItems > 0 && hasMorePreviousMessages
                    state.isInProgress shouldBe false
                    (state.totalItems > 0) shouldBe true
                    state.hasMorePreviousMessages shouldBe true
                }
                PaginationDecision.NoAction -> {
                    // NoAction is valid in any state - no additional assertions needed
                }
            }
        }
    }

    /**
     * Property 3 (inverse): When guard conditions are violated, no fetch action is taken.
     *
     * This test generates states where at least one guard condition is violated
     * and verifies that no fetch action is taken.
     *
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    "Property 3 (inverse): Violated guard conditions result in NoAction" {
        // Test case 1: isInProgress = true (violates guard)
        checkAll(100, Arb.scrollState()) { state ->
            val result = determinePaginationAction(
                totalItems = state.totalItems,
                firstVisibleIndex = state.firstVisibleIndex,
                lastVisibleIndex = state.lastVisibleIndex,
                hasMoreNewMessages = state.hasMoreNewMessages,
                hasMorePreviousMessages = state.hasMorePreviousMessages,
                isInProgress = true // Force guard violation
            )
            result shouldBe PaginationDecision.NoAction
        }

        // Test case 2: totalItems = 0 (violates guard)
        checkAll(100, Arb.boolean(), Arb.boolean()) { hasMoreNew, hasMorePrev ->
            val result = determinePaginationAction(
                totalItems = 0, // Force guard violation
                firstVisibleIndex = 0,
                lastVisibleIndex = 0,
                hasMoreNewMessages = hasMoreNew,
                hasMorePreviousMessages = hasMorePrev,
                isInProgress = false
            )
            result shouldBe PaginationDecision.NoAction
        }

        // Test case 3: Both hasMore flags are false (violates guard for both directions)
        checkAll(100, Arb.int(1, 100)) { totalItems ->
            val result = determinePaginationAction(
                totalItems = totalItems,
                firstVisibleIndex = 0, // Near bottom
                lastVisibleIndex = totalItems - 1, // Near top
                hasMoreNewMessages = false, // Force guard violation
                hasMorePreviousMessages = false, // Force guard violation
                isInProgress = false
            )
            result shouldBe PaginationDecision.NoAction
        }
    }

    // ============================================================================
    // Property 4: Priority When Both Conditions Met
    // ============================================================================

    /**
     * Arbitrary generator for scroll states where both pagination conditions are met.
     *
     * Generates scroll states where:
     * - firstVisibleIndex <= PAGINATION_THRESHOLD (near bottom)
     * - lastVisibleIndex >= totalItems - PAGINATION_THRESHOLD (near top)
     *
     * This happens with small lists where the visible window spans both thresholds.
     */
    fun Arb.Companion.scrollStateBothConditionsMet(): Arb<ScrollState> = arbitrary {
        // Small list where both conditions can be met
        // For both conditions to be met: totalItems must be small enough that
        // firstVisibleIndex <= 5 AND lastVisibleIndex >= totalItems - 5
        // This means: lastVisibleIndex - firstVisibleIndex >= totalItems - 10
        // With a visible window, this is achievable when totalItems <= 11
        val totalItems = Arb.int(1, 11).bind()
        val firstVisibleIndex = Arb.int(0, minOf(PAGINATION_THRESHOLD, totalItems - 1)).bind()
        val visibleCount = Arb.int(1, totalItems).bind()
        val lastVisibleIndex = minOf(firstVisibleIndex + visibleCount - 1, totalItems - 1)
            .coerceAtLeast(maxOf(0, totalItems - PAGINATION_THRESHOLD))

        ScrollState(
            totalItems = totalItems,
            firstVisibleIndex = firstVisibleIndex,
            lastVisibleIndex = lastVisibleIndex,
            hasMoreNewMessages = Arb.boolean().bind(),
            hasMorePreviousMessages = Arb.boolean().bind(),
            isInProgress = Arb.boolean().bind()
        )
    }

    /**
     * **Property 4: Priority When Both Conditions Met**
     *
     * *For any* scroll state where both pagination conditions are met simultaneously
     * (near top AND near bottom with few messages), the pagination effect SHALL call
     * `fetchMessages()` (older messages) and SHALL NOT call `fetchNextMessages()`
     * in the same collection cycle.
     *
     * **Feature: message-list-pagination-direction, Property 4: Priority When Both Conditions Met**
     *
     * **Validates: Requirements 4.4**
     */
    "Property 4: Older messages take priority when both conditions are met" {
        checkAll(100, Arb.scrollStateBothConditionsMet()) { state ->
            val nearTop = state.lastVisibleIndex >= state.totalItems - PAGINATION_THRESHOLD
            val nearBottom = state.firstVisibleIndex <= PAGINATION_THRESHOLD

            // Only test when both conditions are actually met
            if (nearTop && nearBottom && !state.isInProgress && state.totalItems > 0) {
                val result = determinePaginationAction(
                    totalItems = state.totalItems,
                    firstVisibleIndex = state.firstVisibleIndex,
                    lastVisibleIndex = state.lastVisibleIndex,
                    hasMoreNewMessages = state.hasMoreNewMessages,
                    hasMorePreviousMessages = state.hasMorePreviousMessages,
                    isInProgress = state.isInProgress
                )

                // When both conditions are met and hasMorePreviousMessages is true,
                // older messages should take priority
                if (state.hasMorePreviousMessages) {
                    result shouldBe PaginationDecision.FetchOlderMessages
                } else if (state.hasMoreNewMessages) {
                    // Only fetch newer if older is not available
                    result shouldBe PaginationDecision.FetchNewerMessages
                } else {
                    result shouldBe PaginationDecision.NoAction
                }
            }
        }
    }

    /**
     * Property 4 (specific): With a small list where both thresholds are met,
     * older messages should be fetched when both hasMore flags are true.
     *
     * **Validates: Requirements 4.4**
     */
    "Property 4 (specific): Small list with both hasMore flags true fetches older first" {
        // Small list: 6 items, all visible
        // firstVisibleIndex = 0 (near bottom, <= 5)
        // lastVisibleIndex = 5 (near top, >= 6 - 5 = 1)
        val result = determinePaginationAction(
            totalItems = 6,
            firstVisibleIndex = 0,
            lastVisibleIndex = 5,
            hasMoreNewMessages = true,
            hasMorePreviousMessages = true,
            isInProgress = false
        )

        // Older messages should take priority
        result shouldBe PaginationDecision.FetchOlderMessages
    }

    /**
     * Property 4 (specific): With a small list where both thresholds are met,
     * newer messages should be fetched only when hasMorePreviousMessages is false.
     *
     * **Validates: Requirements 4.4**
     */
    "Property 4 (specific): Small list fetches newer only when older not available" {
        // Small list: 6 items, all visible
        val result = determinePaginationAction(
            totalItems = 6,
            firstVisibleIndex = 0,
            lastVisibleIndex = 5,
            hasMoreNewMessages = true,
            hasMorePreviousMessages = false, // No older messages available
            isInProgress = false
        )

        // Should fetch newer since older is not available
        result shouldBe PaginationDecision.FetchNewerMessages
    }

    /**
     * Property 4 (specific): With a very small list (1 item), priority should still apply.
     *
     * **Validates: Requirements 4.4**
     */
    "Property 4 (specific): Single item list respects priority" {
        val result = determinePaginationAction(
            totalItems = 1,
            firstVisibleIndex = 0,
            lastVisibleIndex = 0,
            hasMoreNewMessages = true,
            hasMorePreviousMessages = true,
            isInProgress = false
        )

        // Older messages should take priority
        result shouldBe PaginationDecision.FetchOlderMessages
    }

    /**
     * Property 4 (exhaustive): All combinations when both conditions are met.
     *
     * Tests all 4 combinations of hasMore flags when both scroll conditions are met.
     *
     * **Validates: Requirements 4.4**
     */
    "Property 4 (exhaustive): All hasMore combinations when both conditions met" {
        val combinations = listOf(
            // (hasMoreNewMessages, hasMorePreviousMessages) -> expected
            Pair(false, false) to PaginationDecision.NoAction,
            Pair(false, true) to PaginationDecision.FetchOlderMessages,
            Pair(true, false) to PaginationDecision.FetchNewerMessages,
            Pair(true, true) to PaginationDecision.FetchOlderMessages // Priority: older first
        )

        combinations.forEach { (flags, expected) ->
            val (hasMoreNewMessages, hasMorePreviousMessages) = flags
            val result = determinePaginationAction(
                totalItems = 6, // Small list where both conditions are met
                firstVisibleIndex = 0, // Near bottom
                lastVisibleIndex = 5, // Near top (>= 6 - 5 = 1)
                hasMoreNewMessages = hasMoreNewMessages,
                hasMorePreviousMessages = hasMorePreviousMessages,
                isInProgress = false
            )

            result shouldBe expected
        }
    }

    /**
     * Property 4 (comprehensive): For any scroll state where both conditions are met,
     * older messages always take priority over newer messages.
     *
     * **Validates: Requirements 4.4**
     */
    "Property 4 (comprehensive): Priority is always respected when both conditions met" {
        checkAll(100, Arb.int(1, 11)) { totalItems ->
            // Create a state where both conditions are definitely met
            val firstVisibleIndex = 0 // Near bottom (always <= 5)
            val lastVisibleIndex = totalItems - 1 // Near top (always >= totalItems - 5 for small lists)

            // Verify both conditions are met
            val nearBottom = firstVisibleIndex <= PAGINATION_THRESHOLD
            val nearTop = lastVisibleIndex >= totalItems - PAGINATION_THRESHOLD

            if (nearBottom && nearTop) {
                // Test with both hasMore flags true
                val result = determinePaginationAction(
                    totalItems = totalItems,
                    firstVisibleIndex = firstVisibleIndex,
                    lastVisibleIndex = lastVisibleIndex,
                    hasMoreNewMessages = true,
                    hasMorePreviousMessages = true,
                    isInProgress = false
                )

                // Older messages should ALWAYS take priority
                result shouldBe PaginationDecision.FetchOlderMessages
            }
        }
    }

    /**
     * Property 4 (never newer when older available): When both conditions are met
     * and hasMorePreviousMessages is true, FetchNewerMessages should NEVER be returned.
     *
     * **Validates: Requirements 4.4**
     */
    "Property 4 (invariant): FetchNewerMessages never returned when older available and both conditions met" {
        checkAll(100, Arb.scrollStateBothConditionsMet()) { state ->
            val nearTop = state.lastVisibleIndex >= state.totalItems - PAGINATION_THRESHOLD
            val nearBottom = state.firstVisibleIndex <= PAGINATION_THRESHOLD

            // Only test when both conditions are met and older messages are available
            if (nearTop && nearBottom && state.hasMorePreviousMessages && !state.isInProgress && state.totalItems > 0) {
                val result = determinePaginationAction(
                    totalItems = state.totalItems,
                    firstVisibleIndex = state.firstVisibleIndex,
                    lastVisibleIndex = state.lastVisibleIndex,
                    hasMoreNewMessages = state.hasMoreNewMessages,
                    hasMorePreviousMessages = state.hasMorePreviousMessages,
                    isInProgress = state.isInProgress
                )

                // FetchNewerMessages should NEVER be returned when older is available
                result shouldBe PaginationDecision.FetchOlderMessages
            }
        }
    }

    /**
     * Property 4 (boundary): Tests priority at the exact boundary where both conditions
     * are just barely met.
     *
     * With totalItems = 11:
     * - Near bottom threshold: firstVisibleIndex <= 5
     * - Near top threshold: lastVisibleIndex >= 11 - 5 = 6
     *
     * **Validates: Requirements 4.4**
     */
    "Property 4 (boundary): Priority at exact boundary where both conditions just met" {
        // totalItems = 11, firstVisibleIndex = 5, lastVisibleIndex = 6
        // Near bottom: 5 <= 5 ✓
        // Near top: 6 >= 6 ✓
        val result = determinePaginationAction(
            totalItems = 11,
            firstVisibleIndex = 5, // Exactly at bottom threshold
            lastVisibleIndex = 6, // Exactly at top threshold
            hasMoreNewMessages = true,
            hasMorePreviousMessages = true,
            isInProgress = false
        )

        // Older messages should take priority
        result shouldBe PaginationDecision.FetchOlderMessages
    }

    /**
     * Property 4 (guard interaction): When isInProgress is true, no action should be taken
     * even when both conditions are met.
     *
     * **Validates: Requirements 4.4, 1.5**
     */
    "Property 4 (guard interaction): isInProgress blocks action even when both conditions met" {
        checkAll(100, Arb.scrollStateBothConditionsMet()) { state ->
            val result = determinePaginationAction(
                totalItems = state.totalItems,
                firstVisibleIndex = state.firstVisibleIndex,
                lastVisibleIndex = state.lastVisibleIndex,
                hasMoreNewMessages = state.hasMoreNewMessages,
                hasMorePreviousMessages = state.hasMorePreviousMessages,
                isInProgress = true // Force guard condition
            )

            result shouldBe PaginationDecision.NoAction
        }
    }

    // ============================================================================
    // Property 5: State Reactivity for hasMoreNewMessages
    // ============================================================================

    /**
     * Data class representing a state transition for hasMoreNewMessages.
     *
     * @property beforeState The scroll state before the transition (hasMoreNewMessages = false)
     * @property afterState The scroll state after the transition (hasMoreNewMessages = true)
     */
    data class StateTransition(
        val beforeState: ScrollState,
        val afterState: ScrollState
    )

    /**
     * Arbitrary generator for state transitions where hasMoreNewMessages changes from false to true.
     *
     * Generates pairs of scroll states where:
     * - beforeState has hasMoreNewMessages = false
     * - afterState has hasMoreNewMessages = true
     * - All other properties remain the same (simulating only the flag change)
     */
    fun Arb.Companion.stateTransitionForHasMoreNewMessages(): Arb<StateTransition> = arbitrary {
        val totalItems = Arb.int(1, 100).bind()
        val firstVisibleIndex = Arb.int(0, totalItems - 1).bind()
        val visibleCount = Arb.int(1, minOf(10, totalItems)).bind()
        val lastVisibleIndex = minOf(firstVisibleIndex + visibleCount - 1, totalItems - 1).coerceAtLeast(firstVisibleIndex)
        val hasMorePreviousMessages = Arb.boolean().bind()
        val isInProgress = Arb.boolean().bind()

        val beforeState = ScrollState(
            totalItems = totalItems,
            firstVisibleIndex = firstVisibleIndex,
            lastVisibleIndex = lastVisibleIndex,
            hasMoreNewMessages = false, // Before transition
            hasMorePreviousMessages = hasMorePreviousMessages,
            isInProgress = isInProgress
        )

        val afterState = beforeState.copy(hasMoreNewMessages = true) // After transition

        StateTransition(beforeState, afterState)
    }

    /**
     * Arbitrary generator for state transitions near the bottom of the list.
     *
     * Generates state transitions where firstVisibleIndex is within the pagination threshold,
     * ensuring the scroll position would trigger newer message pagination if allowed.
     */
    fun Arb.Companion.stateTransitionNearBottom(): Arb<StateTransition> = arbitrary {
        val totalItems = Arb.int(11, 100).bind() // Ensure enough items
        val firstVisibleIndex = Arb.int(0, PAGINATION_THRESHOLD).bind() // Near bottom
        val visibleCount = Arb.int(1, minOf(10, totalItems - firstVisibleIndex)).bind()
        val lastVisibleIndex = minOf(firstVisibleIndex + visibleCount - 1, totalItems - 1)
        val hasMorePreviousMessages = Arb.boolean().bind()

        val beforeState = ScrollState(
            totalItems = totalItems,
            firstVisibleIndex = firstVisibleIndex,
            lastVisibleIndex = lastVisibleIndex,
            hasMoreNewMessages = false, // Before transition
            hasMorePreviousMessages = hasMorePreviousMessages,
            isInProgress = false // Not in progress to allow action
        )

        val afterState = beforeState.copy(hasMoreNewMessages = true) // After transition

        StateTransition(beforeState, afterState)
    }

    /**
     * **Property 5: State Reactivity for hasMoreNewMessages**
     *
     * *For any* state transition where `hasMoreNewMessages` changes from `false` to `true`,
     * the pagination effect SHALL be able to trigger `fetchNextMessages()` on the next
     * scroll event that meets the threshold condition (firstVisibleIndex <= 5).
     *
     * **Feature: message-list-pagination-direction, Property 5: State Reactivity for hasMoreNewMessages**
     *
     * **Validates: Requirements 2.2**
     */
    "Property 5: State transition from hasMoreNewMessages=false to true enables pagination" {
        checkAll(100, Arb.stateTransitionForHasMoreNewMessages()) { transition ->
            val beforeResult = shouldFetchNewerMessages(
                totalItems = transition.beforeState.totalItems,
                firstVisibleIndex = transition.beforeState.firstVisibleIndex,
                hasMoreNewMessages = transition.beforeState.hasMoreNewMessages,
                isInProgress = transition.beforeState.isInProgress
            )

            val afterResult = shouldFetchNewerMessages(
                totalItems = transition.afterState.totalItems,
                firstVisibleIndex = transition.afterState.firstVisibleIndex,
                hasMoreNewMessages = transition.afterState.hasMoreNewMessages,
                isInProgress = transition.afterState.isInProgress
            )

            // Before transition: hasMoreNewMessages=false should always prevent pagination
            beforeResult shouldBe false

            // After transition: pagination should be possible if other conditions are met
            val expectedAfter = transition.afterState.firstVisibleIndex <= PAGINATION_THRESHOLD &&
                    !transition.afterState.isInProgress &&
                    transition.afterState.totalItems > 0

            afterResult shouldBe expectedAfter
        }
    }

    /**
     * Property 5 (specific): When hasMoreNewMessages transitions from false to true
     * and scroll position is near bottom, pagination should become possible.
     *
     * **Validates: Requirements 2.2**
     */
    "Property 5 (specific): Transition enables pagination when near bottom" {
        checkAll(100, Arb.stateTransitionNearBottom()) { transition ->
            val beforeResult = shouldFetchNewerMessages(
                totalItems = transition.beforeState.totalItems,
                firstVisibleIndex = transition.beforeState.firstVisibleIndex,
                hasMoreNewMessages = transition.beforeState.hasMoreNewMessages,
                isInProgress = transition.beforeState.isInProgress
            )

            val afterResult = shouldFetchNewerMessages(
                totalItems = transition.afterState.totalItems,
                firstVisibleIndex = transition.afterState.firstVisibleIndex,
                hasMoreNewMessages = transition.afterState.hasMoreNewMessages,
                isInProgress = transition.afterState.isInProgress
            )

            // Before: should NOT trigger (hasMoreNewMessages = false)
            beforeResult shouldBe false

            // After: should trigger (hasMoreNewMessages = true, near bottom, not in progress)
            afterResult shouldBe true
        }
    }

    /**
     * Property 5 (specific): When hasMoreNewMessages transitions from false to true
     * but scroll position is NOT near bottom, pagination should still not trigger.
     *
     * **Validates: Requirements 2.2**
     */
    "Property 5 (specific): Transition does not enable pagination when not near bottom" {
        checkAll(100, Arb.int(PAGINATION_THRESHOLD + 1, 50)) { firstVisibleIndex ->
            val beforeResult = shouldFetchNewerMessages(
                totalItems = 100,
                firstVisibleIndex = firstVisibleIndex, // Not near bottom
                hasMoreNewMessages = false,
                isInProgress = false
            )

            val afterResult = shouldFetchNewerMessages(
                totalItems = 100,
                firstVisibleIndex = firstVisibleIndex, // Still not near bottom
                hasMoreNewMessages = true, // Transitioned to true
                isInProgress = false
            )

            // Before: should NOT trigger
            beforeResult shouldBe false

            // After: should still NOT trigger (not near bottom)
            afterResult shouldBe false
        }
    }

    /**
     * Property 5 (determinePaginationAction): State transition should enable
     * FetchNewerMessages action when conditions are met.
     *
     * **Validates: Requirements 2.2**
     */
    "Property 5 (determinePaginationAction): Transition enables FetchNewerMessages action" {
        checkAll(100, Arb.stateTransitionNearBottom()) { transition ->
            // Ensure we're not near top (to avoid priority conflict)
            val adjustedTransition = if (transition.beforeState.lastVisibleIndex >= transition.beforeState.totalItems - PAGINATION_THRESHOLD) {
                // Adjust to not be near top
                val newLastVisibleIndex = minOf(transition.beforeState.firstVisibleIndex + 5, transition.beforeState.totalItems - PAGINATION_THRESHOLD - 1)
                    .coerceAtLeast(transition.beforeState.firstVisibleIndex)
                StateTransition(
                    beforeState = transition.beforeState.copy(
                        lastVisibleIndex = newLastVisibleIndex,
                        hasMorePreviousMessages = false // Disable older message fetching
                    ),
                    afterState = transition.afterState.copy(
                        lastVisibleIndex = newLastVisibleIndex,
                        hasMorePreviousMessages = false
                    )
                )
            } else {
                transition.copy(
                    beforeState = transition.beforeState.copy(hasMorePreviousMessages = false),
                    afterState = transition.afterState.copy(hasMorePreviousMessages = false)
                )
            }

            val beforeResult = determinePaginationAction(
                totalItems = adjustedTransition.beforeState.totalItems,
                firstVisibleIndex = adjustedTransition.beforeState.firstVisibleIndex,
                lastVisibleIndex = adjustedTransition.beforeState.lastVisibleIndex,
                hasMoreNewMessages = adjustedTransition.beforeState.hasMoreNewMessages,
                hasMorePreviousMessages = adjustedTransition.beforeState.hasMorePreviousMessages,
                isInProgress = adjustedTransition.beforeState.isInProgress
            )

            val afterResult = determinePaginationAction(
                totalItems = adjustedTransition.afterState.totalItems,
                firstVisibleIndex = adjustedTransition.afterState.firstVisibleIndex,
                lastVisibleIndex = adjustedTransition.afterState.lastVisibleIndex,
                hasMoreNewMessages = adjustedTransition.afterState.hasMoreNewMessages,
                hasMorePreviousMessages = adjustedTransition.afterState.hasMorePreviousMessages,
                isInProgress = adjustedTransition.afterState.isInProgress
            )

            // Before: should be NoAction (hasMoreNewMessages = false)
            beforeResult shouldBe PaginationDecision.NoAction

            // After: should be FetchNewerMessages (hasMoreNewMessages = true, near bottom)
            afterResult shouldBe PaginationDecision.FetchNewerMessages
        }
    }

    /**
     * Property 5 (invariant): Before transition, FetchNewerMessages is never returned.
     *
     * This test verifies that when hasMoreNewMessages is false, the pagination
     * decision never returns FetchNewerMessages, regardless of scroll position.
     *
     * **Validates: Requirements 2.2**
     */
    "Property 5 (invariant): FetchNewerMessages never returned when hasMoreNewMessages=false" {
        checkAll(100, Arb.scrollState()) { state ->
            val stateWithFalseFlag = state.copy(hasMoreNewMessages = false)

            val result = determinePaginationAction(
                totalItems = stateWithFalseFlag.totalItems,
                firstVisibleIndex = stateWithFalseFlag.firstVisibleIndex,
                lastVisibleIndex = stateWithFalseFlag.lastVisibleIndex,
                hasMoreNewMessages = stateWithFalseFlag.hasMoreNewMessages,
                hasMorePreviousMessages = stateWithFalseFlag.hasMorePreviousMessages,
                isInProgress = stateWithFalseFlag.isInProgress
            )

            // FetchNewerMessages should NEVER be returned when hasMoreNewMessages is false
            (result != PaginationDecision.FetchNewerMessages) shouldBe true
        }
    }

    /**
     * Property 5 (invariant): After transition, FetchNewerMessages CAN be returned
     * when all conditions are met.
     *
     * **Validates: Requirements 2.2**
     */
    "Property 5 (invariant): FetchNewerMessages can be returned when hasMoreNewMessages=true" {
        // Test specific case where all conditions are met
        val result = determinePaginationAction(
            totalItems = 50,
            firstVisibleIndex = 0, // Near bottom
            lastVisibleIndex = 5, // Not near top
            hasMoreNewMessages = true, // After transition
            hasMorePreviousMessages = false, // No older messages to avoid priority
            isInProgress = false
        )

        result shouldBe PaginationDecision.FetchNewerMessages
    }

    /**
     * Property 5 (exhaustive): All combinations of conditions after transition.
     *
     * Tests all combinations of guard conditions after hasMoreNewMessages
     * transitions to true.
     *
     * **Validates: Requirements 2.2**
     */
    "Property 5 (exhaustive): All condition combinations after transition" {
        val combinations = listOf(
            // (nearBottom, isInProgress, totalItems > 0) -> expected shouldFetchNewerMessages
            Triple(false, false, false) to false, // Not near bottom, not in progress, empty list
            Triple(false, false, true) to false,  // Not near bottom, not in progress, has items
            Triple(false, true, false) to false,  // Not near bottom, in progress, empty list
            Triple(false, true, true) to false,   // Not near bottom, in progress, has items
            Triple(true, false, false) to false,  // Near bottom, not in progress, empty list
            Triple(true, false, true) to true,    // Near bottom, not in progress, has items -> SHOULD TRIGGER
            Triple(true, true, false) to false,   // Near bottom, in progress, empty list
            Triple(true, true, true) to false     // Near bottom, in progress, has items
        )

        combinations.forEach { (conditions, expected) ->
            val (nearBottom, isInProgress, hasItems) = conditions
            val firstVisibleIndex = if (nearBottom) 0 else PAGINATION_THRESHOLD + 1
            val totalItems = if (hasItems) 50 else 0

            val result = shouldFetchNewerMessages(
                totalItems = totalItems,
                firstVisibleIndex = firstVisibleIndex,
                hasMoreNewMessages = true, // After transition
                isInProgress = isInProgress
            )

            result shouldBe expected
        }
    }

    /**
     * Property 5 (boundary): Tests the exact threshold boundary after transition.
     *
     * - firstVisibleIndex = 5 should trigger (at threshold)
     * - firstVisibleIndex = 6 should NOT trigger (above threshold)
     *
     * **Validates: Requirements 2.2**
     */
    "Property 5 (boundary): Threshold boundary after transition" {
        // At threshold (5) - should trigger after transition
        val atThreshold = shouldFetchNewerMessages(
            totalItems = 50,
            firstVisibleIndex = 5,
            hasMoreNewMessages = true, // After transition
            isInProgress = false
        )
        atThreshold shouldBe true

        // Just above threshold (6) - should NOT trigger even after transition
        val aboveThreshold = shouldFetchNewerMessages(
            totalItems = 50,
            firstVisibleIndex = 6,
            hasMoreNewMessages = true, // After transition
            isInProgress = false
        )
        aboveThreshold shouldBe false
    }

    /**
     * Property 5 (comprehensive): For any state transition, the reactivity
     * property holds - pagination becomes possible after transition when
     * threshold conditions are met.
     *
     * **Validates: Requirements 2.2**
     */
    "Property 5 (comprehensive): Reactivity property holds for all transitions" {
        checkAll(100, Arb.stateTransitionForHasMoreNewMessages()) { transition ->
            val beforeResult = determinePaginationAction(
                totalItems = transition.beforeState.totalItems,
                firstVisibleIndex = transition.beforeState.firstVisibleIndex,
                lastVisibleIndex = transition.beforeState.lastVisibleIndex,
                hasMoreNewMessages = transition.beforeState.hasMoreNewMessages,
                hasMorePreviousMessages = transition.beforeState.hasMorePreviousMessages,
                isInProgress = transition.beforeState.isInProgress
            )

            val afterResult = determinePaginationAction(
                totalItems = transition.afterState.totalItems,
                firstVisibleIndex = transition.afterState.firstVisibleIndex,
                lastVisibleIndex = transition.afterState.lastVisibleIndex,
                hasMoreNewMessages = transition.afterState.hasMoreNewMessages,
                hasMorePreviousMessages = transition.afterState.hasMorePreviousMessages,
                isInProgress = transition.afterState.isInProgress
            )

            // Before transition: FetchNewerMessages should never be returned
            (beforeResult != PaginationDecision.FetchNewerMessages) shouldBe true

            // After transition: FetchNewerMessages CAN be returned if conditions are met
            val nearBottom = transition.afterState.firstVisibleIndex <= PAGINATION_THRESHOLD
            val nearTop = transition.afterState.lastVisibleIndex >= transition.afterState.totalItems - PAGINATION_THRESHOLD
            val canFetchNewer = nearBottom &&
                    !transition.afterState.isInProgress &&
                    transition.afterState.totalItems > 0 &&
                    // Priority: older messages take precedence
                    !(nearTop && transition.afterState.hasMorePreviousMessages)

            if (canFetchNewer) {
                afterResult shouldBe PaginationDecision.FetchNewerMessages
            }
        }
    }

    /**
     * Property 5 (edge case): Transition at index 0 (very bottom of list).
     *
     * When at the very bottom of the list and hasMoreNewMessages transitions
     * to true, pagination should be enabled.
     *
     * **Validates: Requirements 2.2, 4.2**
     */
    "Property 5 (edge case): Transition at index 0 enables pagination" {
        val beforeResult = shouldFetchNewerMessages(
            totalItems = 50,
            firstVisibleIndex = 0, // At very bottom
            hasMoreNewMessages = false, // Before transition
            isInProgress = false
        )

        val afterResult = shouldFetchNewerMessages(
            totalItems = 50,
            firstVisibleIndex = 0, // Still at very bottom
            hasMoreNewMessages = true, // After transition
            isInProgress = false
        )

        beforeResult shouldBe false
        afterResult shouldBe true
    }

    /**
     * Property 5 (guard interaction): isInProgress blocks pagination even after transition.
     *
     * **Validates: Requirements 2.2, 1.5**
     */
    "Property 5 (guard interaction): isInProgress blocks pagination after transition" {
        checkAll(100, Arb.int(0, PAGINATION_THRESHOLD)) { firstVisibleIndex ->
            val result = shouldFetchNewerMessages(
                totalItems = 50,
                firstVisibleIndex = firstVisibleIndex, // Near bottom
                hasMoreNewMessages = true, // After transition
                isInProgress = true // Guard condition
            )

            // Should NOT trigger even after transition due to isInProgress
            result shouldBe false
        }
    }
})
