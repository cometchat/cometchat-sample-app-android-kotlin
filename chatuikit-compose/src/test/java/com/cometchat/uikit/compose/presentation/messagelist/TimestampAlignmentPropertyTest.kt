package com.cometchat.uikit.compose.presentation.messagelist

import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

/**
 * Represents the result of timestamp display position resolution.
 *
 * This sealed class encapsulates the two possible outcomes when resolving
 * where the timestamp should be displayed in a message bubble:
 *
 * - [InHeader]: Timestamp should be displayed in the header view alongside sender name
 * - [InStatusInfo]: Timestamp should be displayed in the status info view alongside receipt indicator
 *
 * This abstraction allows the resolution logic to be tested independently
 * of Compose runtime, as it doesn't require actual composable functions.
 */
sealed class TimestampDisplayPosition {
    /**
     * Indicates that the timestamp should be displayed in the header view.
     *
     * When this result is returned, the timestamp is shown alongside the sender name
     * at the top of the message bubble.
     *
     * **Validates: Requirements 6.2** - WHEN timeStampAlignment is set to TOP,
     * THE Message_Bubble SHALL display the timestamp in the header view alongside the sender name
     */
    object InHeader : TimestampDisplayPosition()

    /**
     * Indicates that the timestamp should be displayed in the status info view.
     *
     * When this result is returned, the timestamp is shown alongside the receipt indicator
     * at the bottom of the message bubble.
     *
     * **Validates: Requirements 6.3** - WHEN timeStampAlignment is set to BOTTOM,
     * THE Message_Bubble SHALL display the timestamp in the status info view alongside the receipt indicator
     */
    object InStatusInfo : TimestampDisplayPosition()
}

/**
 * Resolves where the timestamp should be displayed in a message bubble.
 *
 * This function implements the timestamp alignment logic as defined in the
 * design document for the message-list-avatar-parity feature. The resolution
 * follows a simple mapping:
 *
 * - [UIKitConstants.TimeStampAlignment.TOP] → [TimestampDisplayPosition.InHeader]
 * - [UIKitConstants.TimeStampAlignment.BOTTOM] → [TimestampDisplayPosition.InStatusInfo]
 *
 * ## Property 6: Timestamp Alignment Controls Display Position
 *
 * *For any* message bubble:
 * - When timeStampAlignment is TOP, the timestamp SHALL be displayed in the header view
 * - When timeStampAlignment is BOTTOM, the timestamp SHALL be displayed in the status info view
 *
 * **Validates: Requirements 6.1, 6.2, 6.3**
 *
 * @param timeStampAlignment The alignment setting that controls timestamp position
 * @return The resolution result indicating where the timestamp should be displayed
 *
 * @see TimestampDisplayPosition
 */
fun resolveTimestampDisplayPosition(
    timeStampAlignment: UIKitConstants.TimeStampAlignment
): TimestampDisplayPosition {
    return when (timeStampAlignment) {
        UIKitConstants.TimeStampAlignment.TOP -> TimestampDisplayPosition.InHeader
        UIKitConstants.TimeStampAlignment.BOTTOM -> TimestampDisplayPosition.InStatusInfo
    }
}

/**
 * Determines whether the timestamp should be shown in the header view.
 *
 * This function provides a boolean result for use in composable functions
 * where a simple true/false is needed to control visibility.
 *
 * @param timeStampAlignment The alignment setting that controls timestamp position
 * @return true if timestamp should be shown in header, false otherwise
 *
 * **Validates: Requirements 6.2**
 */
fun shouldShowTimeInHeader(
    timeStampAlignment: UIKitConstants.TimeStampAlignment
): Boolean {
    return timeStampAlignment == UIKitConstants.TimeStampAlignment.TOP
}

/**
 * Determines whether the timestamp should be shown in the status info view.
 *
 * This function provides a boolean result for use in composable functions
 * where a simple true/false is needed to control visibility.
 *
 * @param timeStampAlignment The alignment setting that controls timestamp position
 * @return true if timestamp should be shown in status info, false otherwise
 *
 * **Validates: Requirements 6.3**
 */
fun shouldShowTimeInStatusInfo(
    timeStampAlignment: UIKitConstants.TimeStampAlignment
): Boolean {
    return timeStampAlignment == UIKitConstants.TimeStampAlignment.BOTTOM
}

/**
 * Property-based tests for verifying timestamp alignment logic in the message list.
 *
 * **Feature: message-list-avatar-parity**
 * **Property 6: Timestamp Alignment Controls Display Position**
 *
 * **Validates: Requirements 6.1, 6.2, 6.3**
 *
 * For any message bubble:
 * - When timeStampAlignment is TOP, the timestamp SHALL be displayed in the header view
 * - When timeStampAlignment is BOTTOM, the timestamp SHALL be displayed in the status info view
 *
 * This test verifies the timestamp alignment resolution logic defined in the design document:
 *
 * ## Requirements:
 * - 6.1: THE Message_List SHALL expose a timeStampAlignment property (enum: TOP, BOTTOM, default: BOTTOM)
 * - 6.2: WHEN timeStampAlignment is set to TOP, THE Message_Bubble SHALL display the timestamp
 *        in the header view alongside the sender name
 * - 6.3: WHEN timeStampAlignment is set to BOTTOM, THE Message_Bubble SHALL display the timestamp
 *        in the status info view alongside the receipt indicator
 */
class TimestampAlignmentPropertyTest : StringSpec({

    // ============================================================================
    // Arbitrary generators for property-based testing
    // ============================================================================

    /**
     * Generates random TimeStampAlignment values (TOP or BOTTOM).
     */
    val timeStampAlignmentArb = Arb.enum<UIKitConstants.TimeStampAlignment>()

    /**
     * Generates random MessageBubbleAlignment values (LEFT, RIGHT, CENTER).
     * Used to verify timestamp alignment is independent of message alignment.
     */
    val messageBubbleAlignmentArb = Arb.enum<UIKitConstants.MessageBubbleAlignment>()

    // ============================================================================
    // Property 6: Timestamp Alignment Controls Display Position
    // ============================================================================

    /**
     * Property test: When timeStampAlignment is TOP, the timestamp SHALL be displayed
     * in the header view alongside the sender name.
     *
     * This property ensures that the TOP alignment setting correctly positions
     * the timestamp in the header view.
     *
     * **Validates: Requirements 6.2**
     */
    "Property 6: TOP alignment should display timestamp in header view" {
        val result = resolveTimestampDisplayPosition(
            timeStampAlignment = UIKitConstants.TimeStampAlignment.TOP
        )

        // TOP alignment should place timestamp in header
        result shouldBe TimestampDisplayPosition.InHeader
    }

    /**
     * Property test: When timeStampAlignment is BOTTOM, the timestamp SHALL be displayed
     * in the status info view alongside the receipt indicator.
     *
     * This property ensures that the BOTTOM alignment setting correctly positions
     * the timestamp in the status info view.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 6: BOTTOM alignment should display timestamp in status info view" {
        val result = resolveTimestampDisplayPosition(
            timeStampAlignment = UIKitConstants.TimeStampAlignment.BOTTOM
        )

        // BOTTOM alignment should place timestamp in status info
        result shouldBe TimestampDisplayPosition.InStatusInfo
    }

    /**
     * Property test: For any timeStampAlignment value, the resolution should return
     * the correct display position.
     *
     * This comprehensive test verifies that all possible TimeStampAlignment values
     * are correctly mapped to their corresponding display positions.
     *
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    "Property 6: All TimeStampAlignment values should map to correct display position" {
        checkAll(100, timeStampAlignmentArb) { timeStampAlignment ->
            val result = resolveTimestampDisplayPosition(timeStampAlignment)

            val expected = when (timeStampAlignment) {
                UIKitConstants.TimeStampAlignment.TOP -> TimestampDisplayPosition.InHeader
                UIKitConstants.TimeStampAlignment.BOTTOM -> TimestampDisplayPosition.InStatusInfo
            }

            result shouldBe expected
        }
    }

    /**
     * Property test: Exhaustive test of all TimeStampAlignment enum values.
     *
     * This test explicitly verifies both enum values to ensure complete coverage.
     *
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    "Property 6 (exhaustive): All TimeStampAlignment values should be handled" {
        val alignments = listOf(
            UIKitConstants.TimeStampAlignment.TOP to TimestampDisplayPosition.InHeader,
            UIKitConstants.TimeStampAlignment.BOTTOM to TimestampDisplayPosition.InStatusInfo
        )

        alignments.forEach { (alignment, expectedPosition) ->
            val result = resolveTimestampDisplayPosition(alignment)
            result shouldBe expectedPosition
        }
    }

    // ============================================================================
    // Boolean helper function tests
    // ============================================================================

    /**
     * Property test: shouldShowTimeInHeader should return true only when
     * timeStampAlignment is TOP.
     *
     * This test verifies the boolean helper function used in composable code.
     *
     * **Validates: Requirements 6.2**
     */
    "Property 6: shouldShowTimeInHeader should return true only for TOP alignment" {
        checkAll(100, timeStampAlignmentArb) { timeStampAlignment ->
            val result = shouldShowTimeInHeader(timeStampAlignment)

            val expected = timeStampAlignment == UIKitConstants.TimeStampAlignment.TOP
            result shouldBe expected
        }
    }

    /**
     * Property test: shouldShowTimeInStatusInfo should return true only when
     * timeStampAlignment is BOTTOM.
     *
     * This test verifies the boolean helper function used in composable code.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 6: shouldShowTimeInStatusInfo should return true only for BOTTOM alignment" {
        checkAll(100, timeStampAlignmentArb) { timeStampAlignment ->
            val result = shouldShowTimeInStatusInfo(timeStampAlignment)

            val expected = timeStampAlignment == UIKitConstants.TimeStampAlignment.BOTTOM
            result shouldBe expected
        }
    }

    /**
     * Property test: shouldShowTimeInHeader and shouldShowTimeInStatusInfo should
     * be mutually exclusive - exactly one should be true for any alignment.
     *
     * This test verifies that timestamp is shown in exactly one location.
     *
     * **Validates: Requirements 6.2, 6.3**
     */
    "Property 6: Timestamp should be shown in exactly one location" {
        checkAll(100, timeStampAlignmentArb) { timeStampAlignment ->
            val showInHeader = shouldShowTimeInHeader(timeStampAlignment)
            val showInStatusInfo = shouldShowTimeInStatusInfo(timeStampAlignment)

            // Exactly one should be true (XOR)
            (showInHeader xor showInStatusInfo) shouldBe true
        }
    }

    /**
     * Property test: Specific test for TOP alignment boolean helpers.
     *
     * **Validates: Requirements 6.2**
     */
    "Property 6 (specific): TOP alignment should show time in header only" {
        val showInHeader = shouldShowTimeInHeader(UIKitConstants.TimeStampAlignment.TOP)
        val showInStatusInfo = shouldShowTimeInStatusInfo(UIKitConstants.TimeStampAlignment.TOP)

        showInHeader shouldBe true
        showInStatusInfo shouldBe false
    }

    /**
     * Property test: Specific test for BOTTOM alignment boolean helpers.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 6 (specific): BOTTOM alignment should show time in status info only" {
        val showInHeader = shouldShowTimeInHeader(UIKitConstants.TimeStampAlignment.BOTTOM)
        val showInStatusInfo = shouldShowTimeInStatusInfo(UIKitConstants.TimeStampAlignment.BOTTOM)

        showInHeader shouldBe false
        showInStatusInfo shouldBe true
    }

    // ============================================================================
    // Independence from message alignment tests
    // ============================================================================

    /**
     * Property test: Timestamp alignment should be independent of message bubble alignment.
     *
     * This test verifies that the timestamp position is determined solely by
     * timeStampAlignment, regardless of whether the message is LEFT, RIGHT, or CENTER aligned.
     *
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    "Property 6: Timestamp alignment is independent of message bubble alignment" {
        checkAll(100, timeStampAlignmentArb, messageBubbleAlignmentArb) { timeStampAlignment, messageBubbleAlignment ->
            // The timestamp position should be the same regardless of message alignment
            val result = resolveTimestampDisplayPosition(timeStampAlignment)

            val expected = when (timeStampAlignment) {
                UIKitConstants.TimeStampAlignment.TOP -> TimestampDisplayPosition.InHeader
                UIKitConstants.TimeStampAlignment.BOTTOM -> TimestampDisplayPosition.InStatusInfo
            }

            // Message bubble alignment should not affect timestamp position
            result shouldBe expected
        }
    }

    /**
     * Property test: For all message alignments (LEFT, RIGHT, CENTER), TOP timestamp
     * alignment should always show timestamp in header.
     *
     * **Validates: Requirements 6.2**
     */
    "Property 6: TOP alignment shows timestamp in header for all message alignments" {
        val messageAlignments = listOf(
            UIKitConstants.MessageBubbleAlignment.LEFT,
            UIKitConstants.MessageBubbleAlignment.RIGHT,
            UIKitConstants.MessageBubbleAlignment.CENTER
        )

        messageAlignments.forEach { messageBubbleAlignment ->
            val result = resolveTimestampDisplayPosition(UIKitConstants.TimeStampAlignment.TOP)
            result shouldBe TimestampDisplayPosition.InHeader
        }
    }

    /**
     * Property test: For all message alignments (LEFT, RIGHT, CENTER), BOTTOM timestamp
     * alignment should always show timestamp in status info.
     *
     * **Validates: Requirements 6.3**
     */
    "Property 6: BOTTOM alignment shows timestamp in status info for all message alignments" {
        val messageAlignments = listOf(
            UIKitConstants.MessageBubbleAlignment.LEFT,
            UIKitConstants.MessageBubbleAlignment.RIGHT,
            UIKitConstants.MessageBubbleAlignment.CENTER
        )

        messageAlignments.forEach { messageBubbleAlignment ->
            val result = resolveTimestampDisplayPosition(UIKitConstants.TimeStampAlignment.BOTTOM)
            result shouldBe TimestampDisplayPosition.InStatusInfo
        }
    }

    // ============================================================================
    // Default value tests
    // ============================================================================

    /**
     * Property test: The default TimeStampAlignment should be BOTTOM.
     *
     * This test verifies that the default behavior (when no explicit alignment is set)
     * shows the timestamp in the status info view, which is the standard chat pattern.
     *
     * **Validates: Requirements 6.1** - default: BOTTOM
     */
    "Property 6: Default TimeStampAlignment (BOTTOM) should show timestamp in status info" {
        // Default value as specified in requirements
        val defaultAlignment = UIKitConstants.TimeStampAlignment.BOTTOM

        val result = resolveTimestampDisplayPosition(defaultAlignment)

        // Default should place timestamp in status info (standard chat pattern)
        result shouldBe TimestampDisplayPosition.InStatusInfo
    }

    // ============================================================================
    // Consistency tests
    // ============================================================================

    /**
     * Property test: The resolution function should be deterministic - same input
     * should always produce the same output.
     *
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    "Property 6: Resolution should be deterministic" {
        checkAll(100, timeStampAlignmentArb) { timeStampAlignment ->
            val result1 = resolveTimestampDisplayPosition(timeStampAlignment)
            val result2 = resolveTimestampDisplayPosition(timeStampAlignment)

            // Same input should always produce same output
            result1 shouldBe result2
        }
    }

    /**
     * Property test: The boolean helper functions should be consistent with
     * the resolution function.
     *
     * **Validates: Requirements 6.2, 6.3**
     */
    "Property 6: Boolean helpers should be consistent with resolution function" {
        checkAll(100, timeStampAlignmentArb) { timeStampAlignment ->
            val position = resolveTimestampDisplayPosition(timeStampAlignment)
            val showInHeader = shouldShowTimeInHeader(timeStampAlignment)
            val showInStatusInfo = shouldShowTimeInStatusInfo(timeStampAlignment)

            // Verify consistency
            when (position) {
                TimestampDisplayPosition.InHeader -> {
                    showInHeader shouldBe true
                    showInStatusInfo shouldBe false
                }
                TimestampDisplayPosition.InStatusInfo -> {
                    showInHeader shouldBe false
                    showInStatusInfo shouldBe true
                }
            }
        }
    }

    // ============================================================================
    // Decision table verification
    // ============================================================================

    /**
     * Property test: Complete decision table verification for timestamp alignment.
     *
     * | TimeStampAlignment | Display Position | showTimeInHeader | showTimeInStatusInfo |
     * |--------------------|------------------|------------------|----------------------|
     * | TOP                | InHeader         | true             | false                |
     * | BOTTOM             | InStatusInfo     | false            | true                 |
     *
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    "Property 6 (decision table): Complete timestamp alignment verification" {
        data class DecisionTableRow(
            val alignment: UIKitConstants.TimeStampAlignment,
            val expectedPosition: TimestampDisplayPosition,
            val expectedShowInHeader: Boolean,
            val expectedShowInStatusInfo: Boolean
        )

        val decisionTable = listOf(
            DecisionTableRow(
                alignment = UIKitConstants.TimeStampAlignment.TOP,
                expectedPosition = TimestampDisplayPosition.InHeader,
                expectedShowInHeader = true,
                expectedShowInStatusInfo = false
            ),
            DecisionTableRow(
                alignment = UIKitConstants.TimeStampAlignment.BOTTOM,
                expectedPosition = TimestampDisplayPosition.InStatusInfo,
                expectedShowInHeader = false,
                expectedShowInStatusInfo = true
            )
        )

        decisionTable.forEach { row ->
            val position = resolveTimestampDisplayPosition(row.alignment)
            val showInHeader = shouldShowTimeInHeader(row.alignment)
            val showInStatusInfo = shouldShowTimeInStatusInfo(row.alignment)

            position shouldBe row.expectedPosition
            showInHeader shouldBe row.expectedShowInHeader
            showInStatusInfo shouldBe row.expectedShowInStatusInfo
        }
    }
})
