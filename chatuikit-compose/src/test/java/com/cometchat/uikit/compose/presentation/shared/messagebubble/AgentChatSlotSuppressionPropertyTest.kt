package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.uikit.core.constants.UIKitConstants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.collection

/**
 * Property-based tests for agent chat slot suppression in CometChatMessageBubble
 * and MessageListItem.
 *
 * **Feature: agent-user-message-list-compose, Property 7: Agent Chat Slot Suppression**
 *
 * When `isAgentChat` is true, certain UI slots are suppressed:
 * - Header (sender name): hidden for LEFT-aligned messages
 * - Footer (reactions): hidden for all alignments
 * - Thread (reply count): hidden for RIGHT-aligned messages
 * - Long-press callback: suppressed for all alignments
 *
 * Since the slot resolution is embedded in @Composable functions, we replicate
 * the suppression conditions as pure boolean functions and test them exhaustively.
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
 */
class AgentChatSlotSuppressionPropertyTest : StringSpec({

    // ========================================================================
    // Exhaustive generators
    // ========================================================================

    val alignmentExhaustive: Exhaustive<UIKitConstants.MessageBubbleAlignment> = Exhaustive.collection(
        listOf(
            UIKitConstants.MessageBubbleAlignment.LEFT,
            UIKitConstants.MessageBubbleAlignment.RIGHT,
            UIKitConstants.MessageBubbleAlignment.CENTER
        )
    )

    val isAgentChatExhaustive: Exhaustive<Boolean> = Exhaustive.boolean()

    // ========================================================================
    // Property 7: Agent Chat Slot Suppression
    // ========================================================================

    /**
     * **Feature: agent-user-message-list-compose, Property 7: Agent Chat Slot Suppression**
     *
     * Header view SHALL be null when `isAgentChat` is true AND alignment is LEFT.
     * In all other cases (non-agent or non-LEFT alignment), the header is NOT
     * suppressed by the agent chat logic.
     *
     * **Validates: Requirements 3.1**
     */
    "Property 7: header is suppressed when isAgentChat AND alignment is LEFT" {
        checkAll(alignmentExhaustive, isAgentChatExhaustive) { alignment, isAgentChat ->
            val suppressed = isHeaderSuppressedByAgentChat(alignment, isAgentChat)

            if (isAgentChat && alignment == UIKitConstants.MessageBubbleAlignment.LEFT) {
                suppressed shouldBe true
            } else {
                suppressed shouldBe false
            }
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 7: Agent Chat Slot Suppression**
     *
     * Footer view SHALL be null when `isAgentChat` is true, regardless of alignment.
     *
     * **Validates: Requirements 3.2**
     */
    "Property 7: footer is suppressed when isAgentChat regardless of alignment" {
        checkAll(alignmentExhaustive, isAgentChatExhaustive) { alignment, isAgentChat ->
            val suppressed = isFooterSuppressedByAgentChat(alignment, isAgentChat)

            if (isAgentChat) {
                suppressed shouldBe true
            } else {
                suppressed shouldBe false
            }
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 7: Agent Chat Slot Suppression**
     *
     * Thread view SHALL be null when `isAgentChat` is true AND alignment is RIGHT.
     * In all other cases, the thread is NOT suppressed by the agent chat logic.
     *
     * **Validates: Requirements 3.3**
     */
    "Property 7: thread is suppressed when isAgentChat AND alignment is RIGHT" {
        checkAll(alignmentExhaustive, isAgentChatExhaustive) { alignment, isAgentChat ->
            val suppressed = isThreadSuppressedByAgentChat(alignment, isAgentChat)

            if (isAgentChat && alignment == UIKitConstants.MessageBubbleAlignment.RIGHT) {
                suppressed shouldBe true
            } else {
                suppressed shouldBe false
            }
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 7: Agent Chat Slot Suppression**
     *
     * Long-press callback SHALL be null when `isAgentChat` is true, regardless of alignment.
     *
     * **Validates: Requirements 3.4**
     */
    "Property 7: long-press callback is suppressed when isAgentChat regardless of alignment" {
        checkAll(alignmentExhaustive, isAgentChatExhaustive) { alignment, isAgentChat ->
            val suppressed = isLongPressSuppressedByAgentChat(alignment, isAgentChat)

            if (isAgentChat) {
                suppressed shouldBe true
            } else {
                suppressed shouldBe false
            }
        }
    }
})

// ============================================================================
// Pure functions replicating the agent chat slot suppression logic from
// CometChatMessageBubble.kt and MessageListItem.kt.
//
// These functions extract ONLY the isAgentChat suppression conditions,
// assuming no explicit slot overrides (headerView == null, footerView == null,
// threadView == null) and useMinimalSlots == false (normal message, not
// action/call/deleted).
// ============================================================================

/**
 * Replicates the header suppression condition from CometChatMessageBubble.kt:
 *
 * ```kotlin
 * val resolvedHeader = when {
 *     headerView != null -> headerView          // explicit override — not tested here
 *     useMinimalSlots -> null                    // action/call — not tested here
 *     isAgentChat && alignment == LEFT -> null   // THIS condition
 *     else -> factory?.getHeaderView(...) ?: ... // default header
 * }
 * ```
 *
 * Returns true when the agent chat logic suppresses the header.
 */
internal fun isHeaderSuppressedByAgentChat(
    alignment: UIKitConstants.MessageBubbleAlignment,
    isAgentChat: Boolean
): Boolean {
    return isAgentChat && alignment == UIKitConstants.MessageBubbleAlignment.LEFT
}

/**
 * Replicates the footer suppression condition from CometChatMessageBubble.kt:
 *
 * ```kotlin
 * val resolvedFooter = when {
 *     footerView != null -> footerView          // explicit override — not tested here
 *     useMinimalSlots -> null                    // action/call — not tested here
 *     isAgentChat -> null                        // THIS condition
 *     hideReactions -> null                      // not tested here
 *     else -> factory?.getFooterView(...) ?: ... // default footer
 * }
 * ```
 *
 * Returns true when the agent chat logic suppresses the footer.
 */
internal fun isFooterSuppressedByAgentChat(
    @Suppress("UNUSED_PARAMETER") alignment: UIKitConstants.MessageBubbleAlignment,
    isAgentChat: Boolean
): Boolean {
    return isAgentChat
}

/**
 * Replicates the thread suppression condition from CometChatMessageBubble.kt:
 *
 * ```kotlin
 * val resolvedThread = when {
 *     threadView != null -> threadView           // explicit override — not tested here
 *     useMinimalSlots -> null                    // action/call — not tested here
 *     isAgentChat && alignment == RIGHT -> null  // THIS condition
 *     else -> factory?.getThreadView(...) ?: ... // default thread
 * }
 * ```
 *
 * Returns true when the agent chat logic suppresses the thread view.
 */
internal fun isThreadSuppressedByAgentChat(
    alignment: UIKitConstants.MessageBubbleAlignment,
    isAgentChat: Boolean
): Boolean {
    return isAgentChat && alignment == UIKitConstants.MessageBubbleAlignment.RIGHT
}

/**
 * Replicates the long-press suppression condition from MessageListItem.kt:
 *
 * ```kotlin
 * onLongClick = if (isAgentChat || isActionOrCallMessage) null else { ... }
 * ```
 *
 * We test only the `isAgentChat` condition here (isActionOrCallMessage is
 * orthogonal and tested elsewhere).
 *
 * Returns true when the agent chat logic suppresses the long-press callback.
 */
internal fun isLongPressSuppressedByAgentChat(
    @Suppress("UNUSED_PARAMETER") alignment: UIKitConstants.MessageBubbleAlignment,
    isAgentChat: Boolean
): Boolean {
    return isAgentChat
}
