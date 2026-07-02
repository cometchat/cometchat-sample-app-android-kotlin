package com.cometchat.uikit.core.utils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CardMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.BubbleFactoryTestHelper
import com.cometchat.uikit.core.testutils.MockFactory
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
 * Property/example tests for the Card + AI-agent feature across the three
 * conversation contexts shipped in ENG-36415 ("Agent, card in group, BYOA, 1-1"):
 *
 *  1. **1-1**  — a developer [CardMessage] (category `"card"`) in a normal
 *                user-to-user conversation. NOT an agent chat.
 *  2. **BYOA** — "Bring Your Own Agent": a 1-1 conversation whose peer [User] has
 *                role `@agentic`. [AgentChatDetector.isAgentChat] returns true; the
 *                agent's replies are `agentic` messages exposing COPY only.
 *  3. **Agent in groups** — an `agentic` message delivered with
 *                `receiverType == group`. The message still renders (copy-only,
 *                no receiverType guard), but the conversation is NOT an agent chat.
 *
 * These are the context-invariants that must hold at the core (logic) layer:
 *  - Developer-card routing + options depend on **category only** — identical in
 *    1-1 and group.
 *  - `agentic` messages expose **COPY only** in BYOA and in groups alike.
 *  - Agent detection keys off `User.role == "@agentic"` (case-insensitive) and is
 *    inherently a 1-1 (user) concept — a [Group] is never an "agent chat".
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CardAgentContextPropertyTest"
 */
class CardAgentContextPropertyTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    // Both receiver contexts a card / agent message can arrive in.
    val receiverTypeArb = Arb.element(
        CometChatConstants.RECEIVER_TYPE_USER,   // 1-1
        CometChatConstants.RECEIVER_TYPE_GROUP   // group
    )

    /** Builds a mock `agentic` message (category "agentic") for the given receiver type. */
    fun agenticMessage(
        type: String = "assistant",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        deletedAt: Long = 0L
    ): BaseMessage {
        val message = mock<BaseMessage>()
        whenever(message.category).thenReturn(UIKitConstants.MessageCategory.AGENTIC)
        whenever(message.type).thenReturn(type)
        whenever(message.receiverType).thenReturn(receiverType)
        whenever(message.deletedAt).thenReturn(deletedAt)
        return message
    }

    // ════════════════════════════════════════════════════════════════════════
    // Scenario 1 — DEVELOPER CARD in 1-1 (and the same card in a group)
    // ════════════════════════════════════════════════════════════════════════

    context("Scenario 1 — developer card in 1-1") {

        test("a card in a 1-1 conversation routes to CARD_KEY") {
            val card = MockFactory.createCardMessage(
                receiverType = CometChatConstants.RECEIVER_TYPE_USER,
                receiverId = "user-2"
            )
            BubbleFactoryTestHelper.getFactoryKey(card) shouldBe BubbleFactoryTestHelper.CARD_KEY
        }

        test("card options in 1-1 exclude EDIT and COPY but keep REPLY and DELETE") {
            val options = MessageOptionsUtils.getDefaultOptionIds(
                UIKitConstants.MessageCategory.CARD, "buy_button"
            )
            options shouldNotContain UIKitConstants.MessageOption.EDIT
            options shouldNotContain UIKitConstants.MessageOption.COPY
            options shouldContain UIKitConstants.MessageOption.REPLY
            options shouldContain UIKitConstants.MessageOption.DELETE
        }

        test("a 1-1 peer with a normal role is NOT an agent chat") {
            val human = MockFactory.createUser(uid = "user-2", name = "Alice")
            whenever(human.role).thenReturn("default")
            AgentChatDetector.isAgentChat(human) shouldBe false
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Context invariance — a developer card behaves identically in 1-1 and group
    // ════════════════════════════════════════════════════════════════════════

    context("developer-card routing + options are context-independent") {

        test("for any receiver type and any developer type, a card routes to CARD_KEY") {
            checkAll(100, receiverTypeArb, Arb.string(1..40)) { receiverType, cardType ->
                val card = MockFactory.createCardMessage(
                    type = cardType,
                    receiverType = receiverType,
                    receiverId = if (receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) "group-1" else "user-2"
                )
                BubbleFactoryTestHelper.getFactoryKey(card) shouldBe BubbleFactoryTestHelper.CARD_KEY
            }
        }

        test("a deleted card routes to DELETED_KEY in both 1-1 and group") {
            checkAll(50, receiverTypeArb) { receiverType ->
                val card = MockFactory.createCardMessage(
                    receiverType = receiverType,
                    deletedAt = 1_700_000_000L
                )
                val key = BubbleFactoryTestHelper.getFactoryKey(card)
                key shouldBe BubbleFactoryTestHelper.DELETED_KEY
                key shouldNotBe BubbleFactoryTestHelper.CARD_KEY
            }
        }

        test("card options are byte-for-byte identical regardless of receiver context") {
            // Options route on category alone, so a 1-1 card and a group card share options.
            val oneToOne = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.CARD, "buy_button")
            val group = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.CARD, "order_status")
            group shouldContainExactly oneToOne
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Scenario 2 — BYOA: 1-1 agent chat (peer role == @agentic)
    // ════════════════════════════════════════════════════════════════════════

    context("Scenario 2 — BYOA (1-1 agent chat)") {

        test("a 1-1 peer whose role is @agentic IS an agent chat") {
            val agent = MockFactory.createUser(uid = "agent-1", name = "Support Bot")
            whenever(agent.role).thenReturn(UIKitConstants.AIConstants.AGENTIC_USER)
            AgentChatDetector.isAgentChat(agent) shouldBe true
        }

        test("agent role detection is case-insensitive") {
            checkAll(
                Arb.element("@AGENTIC", "@Agentic", "@aGeNtIc", "@agentic")
            ) { roleVariant ->
                val agent = MockFactory.createUser(uid = "agent-1")
                whenever(agent.role).thenReturn(roleVariant)
                AgentChatDetector.isAgentChat(agent) shouldBe true
            }
        }

        test("an agentic reply in a BYOA chat exposes COPY only") {
            val options = MessageOptionsUtils.getDefaultOptionIds(
                UIKitConstants.MessageCategory.AGENTIC, "assistant"
            )
            options shouldContainExactly listOf(UIKitConstants.MessageOption.COPY)
            options shouldNotContain UIKitConstants.MessageOption.REPLY
            options shouldNotContain UIKitConstants.MessageOption.DELETE
            options shouldNotContain UIKitConstants.MessageOption.EDIT
        }

        test("an agentic message in a 1-1 is not routed to the developer card bubble") {
            val agentReply = agenticMessage(receiverType = CometChatConstants.RECEIVER_TYPE_USER)
            val key = BubbleFactoryTestHelper.getFactoryKey(agentReply)
            // agentic uses the AI Assistant bubble, never the developer CARD bubble
            key shouldNotBe BubbleFactoryTestHelper.CARD_KEY
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Scenario 3 — AGENT IN GROUPS (agentic message with receiverType == group)
    // ════════════════════════════════════════════════════════════════════════

    context("Scenario 3 — agent in groups") {

        test("an agentic group message still exposes COPY only (same as 1-1 BYOA)") {
            // Options are category-driven, so the agent-in-group reply is copy-only too.
            val groupAgentOptions = MessageOptionsUtils.getDefaultOptionIds(
                UIKitConstants.MessageCategory.AGENTIC, "assistant"
            )
            groupAgentOptions shouldContainExactly listOf(UIKitConstants.MessageOption.COPY)
        }

        test("agentic copy-only options are identical in 1-1 and in a group") {
            val oneToOne = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.AGENTIC, "assistant")
            val group = MessageOptionsUtils.getDefaultOptionIds(UIKitConstants.MessageCategory.AGENTIC, "assistant")
            group shouldContainExactly oneToOne
        }

        test("agentic routing key does not depend on receiver type (1-1 == group)") {
            checkAll(50, Arb.string(1..30)) { type ->
                val inOneToOne = agenticMessage(type = type, receiverType = CometChatConstants.RECEIVER_TYPE_USER)
                val inGroup = agenticMessage(type = type, receiverType = CometChatConstants.RECEIVER_TYPE_GROUP)
                BubbleFactoryTestHelper.getFactoryKey(inGroup) shouldBe BubbleFactoryTestHelper.getFactoryKey(inOneToOne)
            }
        }

        test("a Group is never an agent chat even when it contains an agent member") {
            // Agent detection is a User concept; the conversation-level agent-chat mode
            // is 1-1 only. A group member may have the @agentic role, but the GROUP
            // conversation is not classified as an agent chat.
            val agentMember = MockFactory.createUser(uid = "agent-1")
            whenever(agentMember.role).thenReturn(UIKitConstants.AIConstants.AGENTIC_USER)

            // The member itself is detected as an agent (this drives badge/attribution)...
            AgentChatDetector.isAgentChat(agentMember) shouldBe true
            // ...but a developer card posted in that same group is still a plain card,
            // keeping the standard card options (no EDIT / no COPY).
            val groupCardOptions = MessageOptionsUtils.getDefaultOptionIds(
                UIKitConstants.MessageCategory.CARD, "buy_button"
            )
            groupCardOptions shouldNotContain UIKitConstants.MessageOption.EDIT
            groupCardOptions shouldNotContain UIKitConstants.MessageOption.COPY
        }
    }
})
