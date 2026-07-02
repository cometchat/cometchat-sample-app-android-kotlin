package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.cardbubble

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.utils.AgentChatDetector
import com.cometchat.uikit.core.utils.MessageOptionsUtils
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Verifies the Card + AI-agent feature at the **Kotlin (XML/View) layer** routing
 * boundary — the production [BubbleFactory] — across the three ENG-36415 contexts:
 * 1-1 developer card, BYOA (1-1 agent chat), and agent-in-group.
 *
 * The core module proves the same invariants on its own routing helper; this file
 * proves the *real* `BubbleFactory` the XML message list uses agrees:
 *  - a developer card → `BubbleFactory.CARD_KEY` in both 1-1 and group
 *  - a deleted card → `BubbleFactory.DELETED_KEY`
 *  - an `agentic` reply is never routed to the developer card bubble
 *  - card options exclude EDIT/COPY; agentic options are COPY only
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CardAgentContextPropertyTest"
 */
class CardAgentContextPropertyTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    val receiverTypeArb = Arb.element(
        CometChatConstants.RECEIVER_TYPE_USER,
        CometChatConstants.RECEIVER_TYPE_GROUP
    )

    fun agenticMessage(
        type: String = "assistant",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER
    ): BaseMessage = mock<BaseMessage>().also {
        whenever(it.category).thenReturn(UIKitConstants.MessageCategory.AGENTIC)
        whenever(it.type).thenReturn(type)
        whenever(it.receiverType).thenReturn(receiverType)
        whenever(it.deletedAt).thenReturn(0L)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Scenario 1 — developer card in 1-1
    // ──────────────────────────────────────────────────────────────────────

    test("Scenario 1 — a 1-1 developer card routes to BubbleFactory.CARD_KEY") {
        val card = MockFactory.createCardMessage(
            receiverType = CometChatConstants.RECEIVER_TYPE_USER,
            receiverId = "user-2"
        )
        BubbleFactory.getFactoryKey(card) shouldBe BubbleFactory.CARD_KEY
    }

    // ──────────────────────────────────────────────────────────────────────
    // Context invariance — developer card identical in 1-1 and group
    // ──────────────────────────────────────────────────────────────────────

    test("a developer card routes to CARD_KEY for any type in both 1-1 and group") {
        checkAll(100, receiverTypeArb, Arb.string(1..40)) { receiverType, cardType ->
            val card = MockFactory.createCardMessage(
                type = cardType,
                receiverType = receiverType,
                receiverId = if (receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) "group-1" else "user-2"
            )
            BubbleFactory.getFactoryKey(card) shouldBe BubbleFactory.CARD_KEY
        }
    }

    test("a deleted card routes to DELETED_KEY in both 1-1 and group") {
        checkAll(50, receiverTypeArb) { receiverType ->
            val card = MockFactory.createCardMessage(receiverType = receiverType, deletedAt = 1_700_000_000L)
            BubbleFactory.getFactoryKey(card) shouldBe BubbleFactory.DELETED_KEY
        }
    }

    test("card options exclude EDIT and COPY, keep REPLY and DELETE (any context)") {
        val options = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.CARD, "buy_button")
        options shouldNotContain UIKitConstants.MessageOption.EDIT
        options shouldNotContain UIKitConstants.MessageOption.COPY
        options shouldContain UIKitConstants.MessageOption.REPLY
        options shouldContain UIKitConstants.MessageOption.DELETE
    }

    // ──────────────────────────────────────────────────────────────────────
    // Scenario 2 — BYOA (1-1 agent chat)
    // ──────────────────────────────────────────────────────────────────────

    test("Scenario 2 — a 1-1 peer with @agentic role is an agent chat") {
        val agent = MockFactory.createUser(uid = "agent-1", name = "Bot")
        whenever(agent.role).thenReturn(UIKitConstants.AIConstants.AGENTIC_USER)
        AgentChatDetector.isAgentChat(agent) shouldBe true
    }

    test("a 1-1 peer with a non-agent role is not an agent chat") {
        checkAll(Arb.element("default", "admin", "moderator", "participant")) { role ->
            val human = MockFactory.createUser(uid = "user-2")
            whenever(human.role).thenReturn(role)
            AgentChatDetector.isAgentChat(human) shouldBe false
        }
    }

    test("an agentic reply exposes COPY only and never routes to the card bubble") {
        val options = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.AGENTIC, "assistant")
        options shouldContainExactly listOf(UIKitConstants.MessageOption.COPY)

        val reply = agenticMessage(receiverType = CometChatConstants.RECEIVER_TYPE_USER)
        BubbleFactory.getFactoryKey(reply) shouldNotBe BubbleFactory.CARD_KEY
    }

    // ──────────────────────────────────────────────────────────────────────
    // Scenario 3 — agent in groups
    // ──────────────────────────────────────────────────────────────────────

    test("Scenario 3 — an agentic group reply keeps COPY-only options") {
        val options = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.AGENTIC, "assistant")
        options shouldContainExactly listOf(UIKitConstants.MessageOption.COPY)
    }

    test("agentic routing key is identical for 1-1 and group receiver types") {
        checkAll(50, Arb.string(1..30)) { type ->
            val oneToOne = agenticMessage(type = type, receiverType = CometChatConstants.RECEIVER_TYPE_USER)
            val group = agenticMessage(type = type, receiverType = CometChatConstants.RECEIVER_TYPE_GROUP)
            BubbleFactory.getFactoryKey(group) shouldBe BubbleFactory.getFactoryKey(oneToOne)
        }
    }

    test("a group member with @agentic role is detected as an agent (drives badge)") {
        val agentMember = MockFactory.createUser(uid = "agent-1")
        whenever(agentMember.role).thenReturn(UIKitConstants.AIConstants.AGENTIC_USER)
        AgentChatDetector.isAgentChat(agentMember) shouldBe true
    }
})
