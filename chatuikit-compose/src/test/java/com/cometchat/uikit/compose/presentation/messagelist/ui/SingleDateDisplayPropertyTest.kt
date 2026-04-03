package com.cometchat.uikit.compose.presentation.messagelist.ui

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.checkAll

/**
 * Property-based tests for verifying single date display behavior at the top of the message list.
 *
 * **Feature: message-list-sticky-date-fixes**
 * **Property 1: Single Date Display at Top**
 *
 * **Validates: Requirements 1.1, 1.2**
 *
 * For any scroll position in the message list, the visible area at the top SHALL display
 * at most one date indicator (either the sticky header OR an inline date separator,
 * but not both showing the same date).
 *
 * The `isDateSeparatorHiddenByStickyHeader()` function ensures this property by:
 * - Hiding the inline date separator when it's at the top of the visible area
 * - Only hiding when the separator's date matches the sticky header's date
 * - Keeping separators visible when they're not at the top
 */
class SingleDateDisplayPropertyTest : StringSpec({

    // ============================================================================
    // Arbitrary generators for property-based testing
    // ============================================================================

    /**
     * Generates random message indices (0 to 999).
     */
    val messageIndexArb = Arb.int(0..999)

    /**
     * Generates random date IDs in YYYYMMDD format (valid date range).
     * Range: 20200101 to 20251231
     */
    val dateIdArb = Arb.long(20200101L..20251231L)

    /**
     * Generates random first visible indices (0 to 999) or null.
     */
    val firstVisibleIndexArb = Arb.int(0..999).orNull()

    /**
     * Generates nullable date IDs for sticky header.
     */
    val stickyHeaderDateArb = dateIdArb.orNull()

    // ============================================================================
    // Property 1: Single Date Display at Top
    // ============================================================================

    /**
     * Property test: When a message is at the top of the visible area (messageIndex == firstVisibleIndex)
     * AND its date matches the sticky header date, the separator SHALL be hidden.
     *
     * This ensures only one date indicator is shown at the top.
     *
     * **Feature: message-list-sticky-date-fixes**
     * **Property 1: Single Date Display at Top**
     *
     * **Validates: Requirements 1.1, 1.2**
     */
    "Property 1: Separator should be hidden when at top and date matches sticky header" {
        checkAll(100, messageIndexArb, dateIdArb) { index, dateId ->
            // When message is at top (index == firstVisibleIndex) and dates match
            val result = isDateSeparatorHiddenByStickyHeader(
                messageIndex = index,
                messageDate = dateId,
                stickyHeaderDate = dateId,  // Same date as message
                firstVisibleIndex = index   // Message is at top
            )

            // Separator should be hidden to prevent duplicate date display
            result shouldBe true
        }
    }

    /**
     * Property test: When a message is NOT at the top of the visible area
     * (messageIndex != firstVisibleIndex), the separator SHALL NOT be hidden,
     * regardless of whether dates match.
     *
     * This ensures date separators remain visible for messages below the top.
     *
     * **Feature: message-list-sticky-date-fixes**
     * **Property 1: Single Date Display at Top**
     *
     * **Validates: Requirements 1.2**
     */
    "Property 1: Separator should NOT be hidden when message is not at top" {
        checkAll(100, messageIndexArb, dateIdArb, messageIndexArb) { index, dateId, firstVisible ->
            // Skip cases where index equals firstVisible (covered by other test)
            if (index != firstVisible) {
                val result = isDateSeparatorHiddenByStickyHeader(
                    messageIndex = index,
                    messageDate = dateId,
                    stickyHeaderDate = dateId,  // Same date
                    firstVisibleIndex = firstVisible
                )

                // Separator should NOT be hidden when not at top
                result shouldBe false
            }
        }
    }

    /**
     * Property test: When dates don't match (messageDate != stickyHeaderDate),
     * the separator SHALL NOT be hidden, even if the message is at the top.
     *
     * This ensures different dates are both displayed when transitioning between days.
     *
     * **Feature: message-list-sticky-date-fixes**
     * **Property 1: Single Date Display at Top**
     *
     * **Validates: Requirements 1.2**
     */
    "Property 1: Separator should NOT be hidden when dates don't match" {
        checkAll(100, messageIndexArb, dateIdArb, dateIdArb) { index, messageDate, stickyDate ->
            // Skip cases where dates are equal (covered by other test)
            if (messageDate != stickyDate) {
                val result = isDateSeparatorHiddenByStickyHeader(
                    messageIndex = index,
                    messageDate = messageDate,
                    stickyHeaderDate = stickyDate,
                    firstVisibleIndex = index  // Message is at top
                )

                // Separator should NOT be hidden when dates differ
                result shouldBe false
            }
        }
    }

    /**
     * Property test: When stickyHeaderDate is null, the separator SHALL NOT be hidden.
     *
     * This handles the case when the sticky header is not visible or not initialized.
     *
     * **Feature: message-list-sticky-date-fixes**
     * **Property 1: Single Date Display at Top**
     *
     * **Validates: Requirements 1.2**
     */
    "Property 1: Separator should NOT be hidden when sticky header date is null" {
        checkAll(100, messageIndexArb, dateIdArb) { index, dateId ->
            val result = isDateSeparatorHiddenByStickyHeader(
                messageIndex = index,
                messageDate = dateId,
                stickyHeaderDate = null,  // No sticky header date
                firstVisibleIndex = index
            )

            // Separator should NOT be hidden when sticky header has no date
            result shouldBe false
        }
    }

    /**
     * Property test: When firstVisibleIndex is null, the separator SHALL NOT be hidden.
     *
     * This handles the case when the visible area is unknown or empty.
     *
     * **Feature: message-list-sticky-date-fixes**
     * **Property 1: Single Date Display at Top**
     *
     * **Validates: Requirements 1.2**
     */
    "Property 1: Separator should NOT be hidden when first visible index is null" {
        checkAll(100, messageIndexArb, dateIdArb) { index, dateId ->
            val result = isDateSeparatorHiddenByStickyHeader(
                messageIndex = index,
                messageDate = dateId,
                stickyHeaderDate = dateId,
                firstVisibleIndex = null  // Unknown visible area
            )

            // Separator should NOT be hidden when visible area is unknown
            result shouldBe false
        }
    }

    /**
     * Property test: When both stickyHeaderDate and firstVisibleIndex are null,
     * the separator SHALL NOT be hidden.
     *
     * **Feature: message-list-sticky-date-fixes**
     * **Property 1: Single Date Display at Top**
     *
     * **Validates: Requirements 1.2**
     */
    "Property 1: Separator should NOT be hidden when both sticky header date and first visible index are null" {
        checkAll(100, messageIndexArb, dateIdArb) { index, dateId ->
            val result = isDateSeparatorHiddenByStickyHeader(
                messageIndex = index,
                messageDate = dateId,
                stickyHeaderDate = null,
                firstVisibleIndex = null
            )

            // Separator should NOT be hidden when both are null
            result shouldBe false
        }
    }

    // ============================================================================
    // Exhaustive boundary tests
    // ============================================================================

    /**
     * Property test: Verify behavior at index boundaries (0, max values).
     *
     * **Validates: Requirements 1.1, 1.2**
     */
    "Property 1 (boundary): Behavior at index 0 should follow the same rules" {
        checkAll(100, dateIdArb) { dateId ->
            // At index 0, at top, same date -> should hide
            val hiddenResult = isDateSeparatorHiddenByStickyHeader(
                messageIndex = 0,
                messageDate = dateId,
                stickyHeaderDate = dateId,
                firstVisibleIndex = 0
            )
            hiddenResult shouldBe true

            // At index 0, not at top -> should not hide
            val visibleResult = isDateSeparatorHiddenByStickyHeader(
                messageIndex = 0,
                messageDate = dateId,
                stickyHeaderDate = dateId,
                firstVisibleIndex = 5
            )
            visibleResult shouldBe false
        }
    }

    /**
     * Property test: Verify the function is deterministic - same inputs always produce same output.
     *
     * **Validates: Requirements 1.1, 1.2**
     */
    "Property 1 (determinism): Same inputs should always produce same output" {
        checkAll(100, messageIndexArb, dateIdArb, stickyHeaderDateArb, firstVisibleIndexArb) { 
            index, messageDate, stickyDate, firstVisible ->
            
            val result1 = isDateSeparatorHiddenByStickyHeader(
                messageIndex = index,
                messageDate = messageDate,
                stickyHeaderDate = stickyDate,
                firstVisibleIndex = firstVisible
            )

            val result2 = isDateSeparatorHiddenByStickyHeader(
                messageIndex = index,
                messageDate = messageDate,
                stickyHeaderDate = stickyDate,
                firstVisibleIndex = firstVisible
            )

            result1 shouldBe result2
        }
    }

    /**
     * Property test: The function should return true ONLY when ALL conditions are met:
     * 1. messageIndex == firstVisibleIndex (message is at top)
     * 2. messageDate == stickyHeaderDate (dates match)
     * 3. Both stickyHeaderDate and firstVisibleIndex are non-null
     *
     * **Feature: message-list-sticky-date-fixes**
     * **Property 1: Single Date Display at Top**
     *
     * **Validates: Requirements 1.1, 1.2**
     */
    "Property 1 (comprehensive): True only when all conditions are met" {
        checkAll(100, messageIndexArb, dateIdArb, stickyHeaderDateArb, firstVisibleIndexArb) { 
            index, messageDate, stickyDate, firstVisible ->
            
            val result = isDateSeparatorHiddenByStickyHeader(
                messageIndex = index,
                messageDate = messageDate,
                stickyHeaderDate = stickyDate,
                firstVisibleIndex = firstVisible
            )

            // Calculate expected result based on all conditions
            val allConditionsMet = stickyDate != null &&
                firstVisible != null &&
                index == firstVisible &&
                messageDate == stickyDate

            result shouldBe allConditionsMet
        }
    }
})
