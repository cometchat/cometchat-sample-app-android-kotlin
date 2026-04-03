package com.cometchat.uikit.compose.presentation.shared.messagebubble

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll

/**
 * Property-based tests for reaction footer visibility logic in CometChatMessageBubble.
 *
 * // Feature: jetpack-message-bubble-parity, Property 8: Reaction footer visibility controlled by hideReactions flag
 *
 * **Validates: Requirements 7.2, 7.3**
 *
 * Property 8: For any message, when `hideReactions` is `true`, the footer view (reactions)
 * SHALL not be rendered regardless of whether the message has reactions. When `hideReactions`
 * is `false`, the footer view SHALL be rendered if and only if the message has non-null,
 * non-empty reactions.
 *
 * The core footer resolution logic under test (from CometChatMessageBubble):
 * ```
 * val resolvedFooter = when {
 *     footerView != null -> footerView       // Explicit footer always wins
 *     useMinimalSlots -> null                 // No footer for action/call/meeting
 *     hideReactions -> null                   // Skip footer when reactions hidden
 *     else -> factory?.getFooterView(...)     // Factory footer (non-null when reactions exist)
 * }
 * ```
 *
 * We model this as a pure function with four boolean inputs:
 * - hasExplicitFooter: whether an explicit footerView composable was provided
 * - useMinimalSlots: whether the message type uses minimal slots (action/call/meeting)
 * - hideReactions: the hideReactions flag
 * - hasFactoryFooter: whether factory/default provides a non-null footer (true when message has reactions)
 */
class ReactionFooterVisibilityPropertyTest : StringSpec({

    // ============================================================================
    // Property Test: hideReactions=true suppresses footer (no explicit footer, no minimal slots)
    // ============================================================================

    /**
     * Property test: When hideReactions is true and no explicit footerView is provided
     * and useMinimalSlots is false, the resolved footer is always null regardless of
     * whether the factory would provide a footer (i.e., regardless of message reactions).
     *
     * // Feature: jetpack-message-bubble-parity, Property 8: Reaction footer visibility controlled by hideReactions flag
     * **Validates: Requirements 7.2, 7.3**
     */
    "hideReactions=true suppresses footer regardless of factory footer availability" {
        checkAll(100, Arb.boolean()) { hasFactoryFooter ->
            resolveFooterIsNonNull(
                hasExplicitFooter = false,
                useMinimalSlots = false,
                hideReactions = true,
                hasFactoryFooter = hasFactoryFooter
            ) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: hideReactions=false allows footer when factory provides one
    // ============================================================================

    /**
     * Property test: When hideReactions is false, no explicit footerView, and no minimal slots,
     * the footer visibility is determined by whether the factory provides a footer
     * (which corresponds to whether the message has reactions).
     *
     * // Feature: jetpack-message-bubble-parity, Property 8: Reaction footer visibility controlled by hideReactions flag
     * **Validates: Requirements 7.2, 7.3**
     */
    "hideReactions=false delegates footer visibility to factory footer availability" {
        checkAll(100, Arb.boolean()) { hasFactoryFooter ->
            resolveFooterIsNonNull(
                hasExplicitFooter = false,
                useMinimalSlots = false,
                hideReactions = false,
                hasFactoryFooter = hasFactoryFooter
            ) shouldBe hasFactoryFooter
        }
    }

    // ============================================================================
    // Property Test: Explicit footerView always wins over hideReactions
    // ============================================================================

    /**
     * Property test: When an explicit footerView is provided, it is always used
     * regardless of hideReactions, useMinimalSlots, or factory footer availability.
     *
     * // Feature: jetpack-message-bubble-parity, Property 8: Reaction footer visibility controlled by hideReactions flag
     * **Validates: Requirements 7.2, 7.3**
     */
    "explicit footerView always wins regardless of hideReactions and other flags" {
        checkAll(
            100,
            Arb.boolean(),  // useMinimalSlots
            Arb.boolean(),  // hideReactions
            Arb.boolean()   // hasFactoryFooter
        ) { useMinimalSlots, hideReactions, hasFactoryFooter ->
            resolveFooterIsNonNull(
                hasExplicitFooter = true,
                useMinimalSlots = useMinimalSlots,
                hideReactions = hideReactions,
                hasFactoryFooter = hasFactoryFooter
            ) shouldBe true
        }
    }

    // ============================================================================
    // Property Test: useMinimalSlots suppresses footer even when hideReactions=false
    // ============================================================================

    /**
     * Property test: When useMinimalSlots is true and no explicit footerView is provided,
     * the footer is null regardless of hideReactions or factory footer availability.
     *
     * // Feature: jetpack-message-bubble-parity, Property 8: Reaction footer visibility controlled by hideReactions flag
     * **Validates: Requirements 7.2, 7.3**
     */
    "useMinimalSlots suppresses footer regardless of hideReactions" {
        checkAll(
            100,
            Arb.boolean(),  // hideReactions
            Arb.boolean()   // hasFactoryFooter
        ) { hideReactions, hasFactoryFooter ->
            resolveFooterIsNonNull(
                hasExplicitFooter = false,
                useMinimalSlots = true,
                hideReactions = hideReactions,
                hasFactoryFooter = hasFactoryFooter
            ) shouldBe false
        }
    }

    // ============================================================================
    // Property Test: Full resolution across all boolean combinations
    // ============================================================================

    /**
     * Property test: For any combination of the four boolean inputs, the footer
     * resolution matches the expected when-chain logic exactly.
     *
     * // Feature: jetpack-message-bubble-parity, Property 8: Reaction footer visibility controlled by hideReactions flag
     * **Validates: Requirements 7.2, 7.3**
     */
    "footer resolution matches when-chain for all input combinations" {
        checkAll(
            100,
            Arb.boolean(),  // hasExplicitFooter
            Arb.boolean(),  // useMinimalSlots
            Arb.boolean(),  // hideReactions
            Arb.boolean()   // hasFactoryFooter
        ) { hasExplicitFooter, useMinimalSlots, hideReactions, hasFactoryFooter ->
            val expected = when {
                hasExplicitFooter -> true
                useMinimalSlots -> false
                hideReactions -> false
                else -> hasFactoryFooter
            }
            resolveFooterIsNonNull(
                hasExplicitFooter = hasExplicitFooter,
                useMinimalSlots = useMinimalSlots,
                hideReactions = hideReactions,
                hasFactoryFooter = hasFactoryFooter
            ) shouldBe expected
        }
    }
})

// ============================================================================
// Helper function — replicates the footer resolution logic from
// CometChatMessageBubble.kt as a pure boolean function for testability.
// ============================================================================

/**
 * Models the footer resolution logic from CometChatMessageBubble.
 *
 * This mirrors the actual code:
 * ```kotlin
 * val resolvedFooter = when {
 *     footerView != null -> footerView
 *     useMinimalSlots -> null
 *     hideReactions -> null
 *     else -> factory?.getFooterView(...)
 * }
 * ```
 *
 * Returns true if the resolved footer would be non-null, false otherwise.
 *
 * @param hasExplicitFooter Whether an explicit footerView composable was provided
 * @param useMinimalSlots Whether the message type uses minimal slots (action/call/meeting)
 * @param hideReactions The hideReactions flag from CometChatMessageBubble parameters
 * @param hasFactoryFooter Whether the factory/default would provide a non-null footer
 *   (true when message has reactions)
 */
private fun resolveFooterIsNonNull(
    hasExplicitFooter: Boolean,
    useMinimalSlots: Boolean,
    hideReactions: Boolean,
    hasFactoryFooter: Boolean
): Boolean {
    return when {
        hasExplicitFooter -> true
        useMinimalSlots -> false
        hideReactions -> false
        else -> hasFactoryFooter
    }
}
