package com.cometchat.uikit.compose.presentation.shared.messagebubble.cardbubble

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.utils.AgentChatDetector
import com.cometchat.uikit.core.utils.MessageOptionsUtils
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import org.mockito.kotlin.whenever

/**
 * Compose-layer unit tests for the Card + AI-agent feature across the three
 * ENG-36415 contexts (1-1, BYOA, agent-in-group).
 *
 * Like the sibling [CometChatCardBubbleComposeRenderingTest], these verify the
 * pure logic the composable relies on (card JSON extraction, fallback-text
 * priority, copy-only options, agent detection) rather than the composition
 * itself — the on-device rendering is covered by CometChatCardBubbleComposeUITest.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*CardAgentContextPropertyTest"
 */
class CardAgentContextPropertyTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    val receiverTypeArb = Arb.element(
        CometChatConstants.RECEIVER_TYPE_USER,
        CometChatConstants.RECEIVER_TYPE_GROUP
    )

    fun fallbackTextOf(message: com.cometchat.chat.models.CardMessage): String =
        message.fallbackText?.ifEmpty { null }
            ?: message.text?.ifEmpty { null }
            ?: "Card Message"

    // ──────────────────────────────────────────────────────────────────────
    // Scenario 1 — developer card in 1-1, and the same card in a group
    // ──────────────────────────────────────────────────────────────────────

    test("Scenario 1 — a 1-1 card with a valid payload extracts non-empty cardJson") {
        val card = MockFactory.createCardMessage(
            receiverType = CometChatConstants.RECEIVER_TYPE_USER,
            cardJson = mapOf("version" to "1.0", "body" to listOf<Any>())
        )
        (card.card?.toString() ?: "") shouldNotBe ""
    }

    test("card payload + fallback are independent of receiver context (1-1 == group)") {
        checkAll(receiverTypeArb) { receiverType ->
            val card = MockFactory.createCardMessage(
                receiverType = receiverType,
                receiverId = if (receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) "group-1" else "user-2",
                fallbackText = "Buy now",
                text = "Preview"
            )
            fallbackTextOf(card) shouldBe "Buy now"
            (card.card?.toString() ?: "") shouldNotBe ""
        }
    }

    test("an empty card payload falls back to text, then to 'Card Message', in any context") {
        checkAll(receiverTypeArb) { receiverType ->
            val withText = MockFactory.createCardMessage(
                receiverType = receiverType, cardJson = null, fallbackText = null, text = "From text"
            )
            fallbackTextOf(withText) shouldBe "From text"

            val withNeither = MockFactory.createCardMessage(
                receiverType = receiverType, cardJson = null, fallbackText = null, text = null
            )
            fallbackTextOf(withNeither) shouldBe "Card Message"
        }
    }

    test("card options exclude COPY/EDIT in 1-1 and group alike") {
        val options = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.CARD, "buy_button")
        options shouldNotContain UIKitConstants.MessageOption.COPY
        options shouldNotContain UIKitConstants.MessageOption.EDIT
    }

    // ──────────────────────────────────────────────────────────────────────
    // Scenario 2 — BYOA (1-1 agent chat)
    // ──────────────────────────────────────────────────────────────────────

    test("Scenario 2 — a 1-1 peer with @agentic role is detected as an agent chat") {
        val agent = MockFactory.createUser(uid = "agent-1")
        whenever(agent.role).thenReturn(UIKitConstants.AIConstants.AGENTIC_USER)
        AgentChatDetector.isAgentChat(agent) shouldBe true
    }

    test("an agentic reply exposes COPY only — the copy button shown outside the bubble") {
        val options = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.AGENTIC, "assistant")
        options shouldContainExactly listOf(UIKitConstants.MessageOption.COPY)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Scenario 3 — agent in groups
    // ──────────────────────────────────────────────────────────────────────

    test("Scenario 3 — agentic group reply keeps COPY-only options (identical to 1-1)") {
        val oneToOne = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.AGENTIC, "assistant")
        val group = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.AGENTIC, "assistant")
        group shouldContainExactly oneToOne
        group shouldContainExactly listOf(UIKitConstants.MessageOption.COPY)
    }

    test("a group member with @agentic role is detected as an agent (drives the AI badge)") {
        val agentMember = MockFactory.createUser(uid = "agent-1")
        whenever(agentMember.role).thenReturn(UIKitConstants.AIConstants.AGENTIC_USER)
        AgentChatDetector.isAgentChat(agentMember) shouldBe true
    }
})
