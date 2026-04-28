package com.cometchat.uikit.compose.presentation.shared.messagebubble

import com.cometchat.chat.models.AIAssistantMessage
import com.cometchat.uikit.core.domain.model.StreamMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean

/**
 * Property-based tests for status info hidden behavior on AI assistant messages.
 *
 * **Feature: agent-user-message-list-compose, Property 6: Status Info Hidden for AI Assistant Messages**
 *
 * The `InternalContentRenderer.shouldHideStatusInfo` function returns `true` for any
 * message that is an instance of `AIAssistantMessage`. Since `StreamMessage` extends
 * `AIAssistantMessage`, the `message is AIAssistantMessage` check covers both types.
 *
 * This means the status info view (timestamp + receipt) is always hidden for:
 * - `AIAssistantMessage` instances (completed AI responses)
 * - `StreamMessage` instances (in-progress streaming responses)
 *
 * **Validates: Requirements 11.1, 11.2**
 */
class StatusInfoHiddenPropertyTest : StringSpec({

    // ========================================================================
    // Arbitrary generators
    // ========================================================================

    /** Generates receiver UIDs. */
    val receiverUidArb: Arb<String> = Arb.string(1..100)

    /** Generates receiver types ("user" or "group"). */
    val receiverTypeArb: Arb<String> = Arb.element("user", "group")

    /** Generates message text content. */
    val textArb: Arb<String> = Arb.string(0..500)

    /** Exhaustive boolean for useMinimalSlots parameter. */
    val useMinimalSlotsExhaustive: Exhaustive<Boolean> = Exhaustive.boolean()

    /** Generates AIAssistantMessage instances with varying properties. */
    val aiAssistantMessageArb: Arb<AIAssistantMessage> = Arb.bind(
        receiverUidArb,
        receiverTypeArb,
        textArb
    ) { uid, type, text ->
        AIAssistantMessage(uid, type, text)
    }

    /** Generates StreamMessage instances with varying properties. */
    val streamMessageArb: Arb<StreamMessage> = Arb.bind(
        receiverUidArb,
        receiverTypeArb,
        textArb
    ) { uid, type, text ->
        StreamMessage(uid, type, text)
    }

    // ========================================================================
    // Property 6: Status Info Hidden for AI Assistant Messages
    // ========================================================================

    /**
     * **Feature: agent-user-message-list-compose, Property 6: Status Info Hidden for AI Assistant Messages**
     *
     * For any AIAssistantMessage instance, `shouldHideStatusInfo` SHALL return `true`
     * regardless of the `useMinimalSlots` parameter value.
     *
     * **Validates: Requirements 11.1**
     */
    "Property 6: shouldHideStatusInfo returns true for any AIAssistantMessage regardless of useMinimalSlots" {
        checkAll(100, aiAssistantMessageArb, useMinimalSlotsExhaustive) { message, useMinimalSlots ->
            InternalContentRenderer.shouldHideStatusInfo(message, useMinimalSlots) shouldBe true
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 6: Status Info Hidden for AI Assistant Messages**
     *
     * For any StreamMessage instance (which extends AIAssistantMessage),
     * `shouldHideStatusInfo` SHALL return `true` regardless of the `useMinimalSlots`
     * parameter value.
     *
     * **Validates: Requirements 11.2**
     */
    "Property 6: shouldHideStatusInfo returns true for any StreamMessage regardless of useMinimalSlots" {
        checkAll(100, streamMessageArb, useMinimalSlotsExhaustive) { message, useMinimalSlots ->
            InternalContentRenderer.shouldHideStatusInfo(message, useMinimalSlots) shouldBe true
        }
    }

    /**
     * **Feature: agent-user-message-list-compose, Property 6: Status Info Hidden for AI Assistant Messages**
     *
     * For any StreamMessage instance, the `message is AIAssistantMessage` check SHALL
     * evaluate to `true`, confirming that the single type check in `shouldHideStatusInfo`
     * covers both AIAssistantMessage and StreamMessage.
     *
     * **Validates: Requirements 11.1, 11.2**
     */
    "Property 6: StreamMessage is always an instance of AIAssistantMessage" {
        checkAll(100, streamMessageArb) { message ->
            (message is AIAssistantMessage) shouldBe true
        }
    }
})
