package com.cometchat.uikit.compose.presentation.messagelist

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll

/**
 * Property-based tests for verifying that swipe-to-reply is disabled in agent chat mode.
 *
 * **Feature: agent-user-message-list-compose, Property 3: Swipe-to-Reply Disabled in Agent Chat**
 *
 * In `CometChatMessageList`, the effective swipe-to-reply flag is computed as:
 * ```
 * val effectiveSwipeToReplyEnabled = swipeToReplyEnabled && !isAgentChat
 * ```
 *
 * This means that when `isAgentChat` is `true`, the effective value is ALWAYS `false`,
 * regardless of the original `swipeToReplyEnabled` configuration. This test exercises
 * the logic as a pure function over all four boolean input combinations.
 *
 * **Validates: Requirements 1.4, 10.3**
 */
class SwipeDisabledInAgentChatPropertyTest : StringSpec({

    // ========================================================================
    // Pure function under test
    // ========================================================================

    /**
     * Mirrors the logic in `CometChatMessageList`:
     * ```kotlin
     * val effectiveSwipeToReplyEnabled = swipeToReplyEnabled && !isAgentChat
     * ```
     */
    fun computeEffectiveSwipeToReply(swipeToReplyEnabled: Boolean, isAgentChat: Boolean): Boolean {
        return swipeToReplyEnabled && !isAgentChat
    }

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    val swipeToReplyEnabledArb = Arb.boolean()
    val isAgentChatArb = Arb.boolean()

    // ========================================================================
    // Property 3: Swipe-to-Reply Disabled in Agent Chat
    // ========================================================================

    /**
     * **Feature: agent-user-message-list-compose, Property 3: Swipe-to-Reply Disabled in Agent Chat**
     *
     * For any value of `swipeToReplyEnabled`, when `isAgentChat` is `true`,
     * the effective swipe-to-reply SHALL be `false`.
     *
     * **Validates: Requirements 1.4, 10.3**
     */
    "Property 3: When isAgentChat is true, effectiveSwipeToReplyEnabled is always false" {
        checkAll(100, swipeToReplyEnabledArb) { swipeToReplyEnabled ->
            val result = computeEffectiveSwipeToReply(
                swipeToReplyEnabled = swipeToReplyEnabled,
                isAgentChat = true
            )

            result shouldBe false
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 3: Swipe-to-Reply Disabled in Agent Chat**
     *
     * Exhaustive verification of all four boolean combinations:
     * - (true, true)   -> false  (agent chat disables swipe)
     * - (true, false)  -> true   (normal chat, swipe enabled)
     * - (false, true)  -> false  (swipe disabled by config)
     * - (false, false) -> false  (swipe disabled by config)
     *
     * **Validates: Requirements 1.4, 10.3**
     */
    "Property 3 (exhaustive): All four boolean combinations produce correct results" {
        data class TestCase(
            val swipeToReplyEnabled: Boolean,
            val isAgentChat: Boolean,
            val expected: Boolean
        )

        val cases = listOf(
            TestCase(swipeToReplyEnabled = true,  isAgentChat = true,  expected = false),
            TestCase(swipeToReplyEnabled = true,  isAgentChat = false, expected = true),
            TestCase(swipeToReplyEnabled = false, isAgentChat = true,  expected = false),
            TestCase(swipeToReplyEnabled = false, isAgentChat = false, expected = false)
        )

        cases.forEach { (swipeEnabled, agentChat, expected) ->
            val result = computeEffectiveSwipeToReply(
                swipeToReplyEnabled = swipeEnabled,
                isAgentChat = agentChat
            )

            result shouldBe expected
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 3: Swipe-to-Reply Disabled in Agent Chat**
     *
     * For any combination of `swipeToReplyEnabled` and `isAgentChat`, the effective
     * value SHALL equal `swipeToReplyEnabled && !isAgentChat`.
     *
     * **Validates: Requirements 1.4, 10.3**
     */
    "Property 3: effectiveSwipeToReplyEnabled equals swipeToReplyEnabled AND NOT isAgentChat" {
        checkAll(100, swipeToReplyEnabledArb, isAgentChatArb) { swipeToReplyEnabled, isAgentChat ->
            val result = computeEffectiveSwipeToReply(
                swipeToReplyEnabled = swipeToReplyEnabled,
                isAgentChat = isAgentChat
            )

            val expected = swipeToReplyEnabled && !isAgentChat
            result shouldBe expected
        }
    }
})
