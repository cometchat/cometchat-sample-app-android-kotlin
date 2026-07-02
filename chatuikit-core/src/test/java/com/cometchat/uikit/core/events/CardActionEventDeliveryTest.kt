package com.cometchat.uikit.core.events

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CardMessage
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for CometChatUIEvent.CardActionClicked event delivery via CometChatEvents.
 *
 * Verifies that card action events are properly emitted and received through the
 * UIKit event bus. This is the primary mechanism for the app to receive card actions
 * from both developer cards and nested agent card blocks (§2.6.1).
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CardActionEventDeliveryTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CardActionEventDeliveryTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== Event Construction ====================

    test("CardActionClicked holds the correct message reference") {
        val cardMessage = mock<CardMessage>()
        whenever(cardMessage.id).thenReturn(42L)
        whenever(cardMessage.category).thenReturn("card")

        val actionEvent = object {} // Simulated renderer action event
        val event = CometChatUIEvent.CardActionClicked(
            message = cardMessage,
            actionEvent = actionEvent
        )

        event.message shouldBe cardMessage
        event.actionEvent shouldBe actionEvent
        (event.message as CardMessage).id shouldBe 42L
        println("    → event.message.id = ${(event.message as CardMessage).id}")
    }

    test("CardActionClicked is a CometChatUIEvent subtype") {
        val event = CometChatUIEvent.CardActionClicked(
            message = mock<CardMessage>(),
            actionEvent = "test_action"
        )
        event.shouldBeInstanceOf<CometChatUIEvent>()
        event.shouldBeInstanceOf<CometChatUIEvent.CardActionClicked>()
        println("    → confirmed: CardActionClicked is CometChatUIEvent")
    }

    test("CardActionClicked supports BaseMessage for agent card (AIAssistantMessage)") {
        // Agent cards forward the owning AIAssistantMessage as BaseMessage
        val agentMessage = mock<BaseMessage>()
        whenever(agentMessage.id).thenReturn(99L)
        whenever(agentMessage.category).thenReturn("agentic")
        whenever(agentMessage.type).thenReturn("assistant")

        val event = CometChatUIEvent.CardActionClicked(
            message = agentMessage,
            actionEvent = mapOf("type" to "openUrl", "url" to "https://example.com")
        )

        event.message shouldBe agentMessage
        event.message.category shouldBe "agentic"
        println("    → agent card event: message.category='${event.message.category}'")
    }

    // ==================== Event Bus Delivery ====================

    test("CardActionClicked emitted on uiEvents flow is received by subscriber") {
        runTest {
            val cardMessage = mock<CardMessage>()
            whenever(cardMessage.id).thenReturn(7L)
            val actionPayload = "openUrl:https://cometchat.com"

            val event = CometChatUIEvent.CardActionClicked(
                message = cardMessage,
                actionEvent = actionPayload
            )

            // Start subscriber before emit — use Unconfined so it immediately starts collecting
            val received = async(kotlinx.coroutines.Dispatchers.Unconfined) {
                CometChatEvents.uiEvents.first { it is CometChatUIEvent.CardActionClicked }
            }

            // Give collector a chance to subscribe
            advanceUntilIdle()

            // Emit via the sync path for testing
            CometChatEvents.emitUIEventSync(event)
            advanceUntilIdle()

            val result = received.await()
            result.shouldBeInstanceOf<CometChatUIEvent.CardActionClicked>()
            val cardEvent = result as CometChatUIEvent.CardActionClicked
            cardEvent.actionEvent shouldBe actionPayload
            (cardEvent.message as CardMessage).id shouldBe 7L
            println("    → received event: messageId=${(cardEvent.message as CardMessage).id}, action=$actionPayload")
        }
    }

    // ==================== PBT: Event data integrity ====================

    test("for any message ID and action string, event preserves data through construction") {
        checkAll(50, Arb.long(1L, 100000L), Arb.string(1..100)) { msgId, actionStr ->
            val message = mock<CardMessage>()
            whenever(message.id).thenReturn(msgId)

            val event = CometChatUIEvent.CardActionClicked(
                message = message,
                actionEvent = actionStr
            )

            (event.message as CardMessage).id shouldBe msgId
            event.actionEvent shouldBe actionStr
            println("    → msgId=$msgId, action='${actionStr.take(20)}...'")
        }
    }

    // ==================== Event Type Discrimination ====================

    test("CardActionClicked is distinguishable from other UIEvents via when-expression") {
        val events: List<CometChatUIEvent> = listOf(
            CometChatUIEvent.CardActionClicked(
                message = mock<CardMessage>(),
                actionEvent = "test"
            ),
            CometChatUIEvent.OpenChat(user = MockFactory.createUser(), group = null),
            CometChatUIEvent.ComposeMessage(id = "1", text = "hello")
        )

        var cardActionCount = 0
        for (event in events) {
            when (event) {
                is CometChatUIEvent.CardActionClicked -> cardActionCount++
                else -> { /* other events */ }
            }
        }

        cardActionCount shouldBe 1
        println("    → found $cardActionCount CardActionClicked in ${events.size} events")
    }
})
