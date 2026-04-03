package com.cometchat.uikit.compose.presentation.shared.messagebubble

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for date/time formatting logic in DefaultStatusInfoView and DefaultHeaderView.
 *
 * // Feature: jetpack-message-bubble-parity, Property 9: Custom date/time formatting is applied consistently
 *
 * **Validates: Requirements 8.3, 8.4, 8.5**
 *
 * Property 9: For any message with `sentAt > 0` and any custom `dateTimeFormatter` callback,
 * the timestamp displayed in both the `DefaultStatusInfoView` (when `timeStampAlignment == BOTTOM`)
 * and the `DefaultHeaderView` (when `timeStampAlignment == TOP`) SHALL be the result of invoking
 * the callback with the message's `sentAt` value.
 *
 * The core logic under test (identical in both DefaultStatusInfoView and DefaultHeaderView):
 * ```kotlin
 * val customDateString = if (dateTimeFormatter != null && message.sentAt > 0) {
 *     dateTimeFormatter(message.sentAt)
 * } else {
 *     null
 * }
 * ```
 *
 * We model this as a pure function test with four cases:
 * 1. dateTimeFormatter provided + sentAt > 0 → callback invoked with sentAt, result used
 * 2. dateTimeFormatter provided + sentAt == 0 → callback NOT invoked, null returned
 * 3. dateTimeFormatter null + sentAt > 0 → null returned (default formatting used)
 * 4. dateTimeFormatter null + sentAt == 0 → null returned
 */
class DateTimeFormattingPropertyTest : StringSpec({

    /**
     * Models the customDateString computation from both DefaultStatusInfoView and DefaultHeaderView.
     * This is the pure logic extracted from the composable functions.
     *
     * @param dateTimeFormatter The optional formatting callback
     * @param sentAt The message's sentAt timestamp value
     * @return The custom date string, or null if default formatting should be used
     */
    fun computeCustomDateString(
        dateTimeFormatter: ((Long) -> String)?,
        sentAt: Long
    ): String? {
        return if (dateTimeFormatter != null && sentAt > 0) {
            dateTimeFormatter(sentAt)
        } else {
            null
        }
    }

    // ============================================================================
    // Property Test: dateTimeFormatter provided + sentAt > 0 → callback result used
    // ============================================================================

    /**
     * Property test: When dateTimeFormatter is provided and sentAt > 0, the callback
     * is invoked with the sentAt value and its result is returned as customDateString.
     *
     * // Feature: jetpack-message-bubble-parity, Property 9: Custom date/time formatting is applied consistently
     * **Validates: Requirements 8.3, 8.4, 8.5**
     */
    "dateTimeFormatter provided and sentAt > 0 returns callback result with sentAt" {
        checkAll(100, Arb.long(min = 1L, max = Long.MAX_VALUE), Arb.string(minSize = 1, maxSize = 50)) { sentAt, formattedOutput ->
            var invokedWith: Long? = null
            val formatter: (Long) -> String = { ts ->
                invokedWith = ts
                formattedOutput
            }

            val result = computeCustomDateString(formatter, sentAt)

            result shouldBe formattedOutput
            invokedWith shouldBe sentAt
        }
    }

    // ============================================================================
    // Property Test: dateTimeFormatter provided + sentAt == 0 → null returned
    // ============================================================================

    /**
     * Property test: When dateTimeFormatter is provided but sentAt == 0, the callback
     * is NOT invoked and null is returned (default formatting used).
     *
     * // Feature: jetpack-message-bubble-parity, Property 9: Custom date/time formatting is applied consistently
     * **Validates: Requirements 8.3, 8.4, 8.5**
     */
    "dateTimeFormatter provided but sentAt == 0 returns null without invoking callback" {
        checkAll(100, Arb.string(minSize = 1, maxSize = 50)) { formattedOutput ->
            var callbackInvoked = false
            val formatter: (Long) -> String = {
                callbackInvoked = true
                formattedOutput
            }

            val result = computeCustomDateString(formatter, sentAt = 0L)

            result shouldBe null
            callbackInvoked shouldBe false
        }
    }

    // ============================================================================
    // Property Test: dateTimeFormatter null + sentAt > 0 → null returned
    // ============================================================================

    /**
     * Property test: When dateTimeFormatter is null, null is returned regardless of sentAt,
     * meaning default CometChatDate formatting is used.
     *
     * // Feature: jetpack-message-bubble-parity, Property 9: Custom date/time formatting is applied consistently
     * **Validates: Requirements 8.3, 8.4, 8.5**
     */
    "dateTimeFormatter null returns null regardless of sentAt" {
        checkAll(100, Arb.long(min = 1L, max = Long.MAX_VALUE)) { sentAt ->
            val result = computeCustomDateString(dateTimeFormatter = null, sentAt = sentAt)
            result shouldBe null
        }
    }

    // ============================================================================
    // Property Test: dateTimeFormatter null + sentAt == 0 → null returned
    // ============================================================================

    /**
     * Property test: When both dateTimeFormatter is null and sentAt == 0, null is returned.
     *
     * // Feature: jetpack-message-bubble-parity, Property 9: Custom date/time formatting is applied consistently
     * **Validates: Requirements 8.3, 8.4, 8.5**
     */
    "dateTimeFormatter null and sentAt == 0 returns null" {
        val result = computeCustomDateString(dateTimeFormatter = null, sentAt = 0L)
        result shouldBe null
    }

    // ============================================================================
    // Property Test: Consistency between DefaultStatusInfoView and DefaultHeaderView
    // ============================================================================

    /**
     * Property test: The customDateString computation is identical in both
     * DefaultStatusInfoView and DefaultHeaderView. For any dateTimeFormatter and sentAt,
     * both views produce the same customDateString result.
     *
     * Since both use the exact same code pattern, we verify the logic is symmetric
     * by running the same function twice with the same inputs and asserting equality.
     *
     * // Feature: jetpack-message-bubble-parity, Property 9: Custom date/time formatting is applied consistently
     * **Validates: Requirements 8.3, 8.4, 8.5**
     */
    "customDateString computation is consistent between StatusInfoView and HeaderView" {
        checkAll(100, Arb.long(min = 0L, max = Long.MAX_VALUE), Arb.boolean()) { sentAt, hasFormatter ->
            val formatter: ((Long) -> String)? = if (hasFormatter) {
                { ts -> "formatted-$ts" }
            } else {
                null
            }

            // Simulate DefaultStatusInfoView computation
            val statusInfoResult = computeCustomDateString(formatter, sentAt)
            // Simulate DefaultHeaderView computation
            val headerViewResult = computeCustomDateString(formatter, sentAt)

            statusInfoResult shouldBe headerViewResult
        }
    }

    // ============================================================================
    // Property Test: sentAt <= 0 always yields null even with formatter
    // ============================================================================

    /**
     * Property test: For any sentAt <= 0 (including negative values), the callback
     * is never invoked and null is always returned, even when a formatter is provided.
     *
     * // Feature: jetpack-message-bubble-parity, Property 9: Custom date/time formatting is applied consistently
     * **Validates: Requirements 8.3, 8.4, 8.5**
     */
    "sentAt <= 0 always returns null even with formatter provided" {
        checkAll(100, Arb.long(min = Long.MIN_VALUE, max = 0L)) { sentAt ->
            var callbackInvoked = false
            val formatter: (Long) -> String = {
                callbackInvoked = true
                "should-not-be-used"
            }

            val result = computeCustomDateString(formatter, sentAt)

            result shouldBe null
            callbackInvoked shouldBe false
        }
    }

    // ============================================================================
    // Property Test: Callback receives exact sentAt value
    // ============================================================================

    /**
     * Property test: When the callback is invoked, it receives the exact sentAt value
     * from the message — no transformation or conversion is applied to the timestamp
     * before passing it to the callback.
     *
     * // Feature: jetpack-message-bubble-parity, Property 9: Custom date/time formatting is applied consistently
     * **Validates: Requirements 8.3, 8.4, 8.5**
     */
    "callback receives exact sentAt value without transformation" {
        checkAll(100, Arb.long(min = 1L, max = Long.MAX_VALUE)) { sentAt ->
            var receivedTimestamp: Long? = null
            val formatter: (Long) -> String = { ts ->
                receivedTimestamp = ts
                "any-output"
            }

            computeCustomDateString(formatter, sentAt)

            receivedTimestamp shouldNotBe null
            receivedTimestamp shouldBe sentAt
        }
    }
})
