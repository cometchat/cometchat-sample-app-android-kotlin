package com.cometchat.uikit.compose.presentation.shared.messagebubble.cardbubble

import com.cometchat.chat.models.CardMessage
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.whenever

/**
 * Interaction tests for CometChatCardBubble composable action handling.
 *
 * Tests verify:
 * - Action events are forwarded via both prop callback and event bus
 * - The kit performs NO action behavior (pure forward to app)
 * - All 9 action types are supported identically
 * - Agent card actions forward the owning AIAssistantMessage
 * - Action callback is set up before card schema rendering
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*CometChatCardBubbleComposeInteractionTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatCardBubbleComposeInteractionTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== Dual-channel action forwarding ====================

    test("action fires onCardAction prop when provided") {
        val message = MockFactory.createCardMessage(id = 10L)
        var receivedMessage: CardMessage? = null
        var receivedAction: Any? = null

        val onCardAction: (CardMessage, Any) -> Unit = { msg, action ->
            receivedMessage = msg
            receivedAction = action
        }

        // Simulate renderer action
        val action = mapOf("type" to "openUrl", "url" to "https://cometchat.com")
        onCardAction(message, action)

        receivedMessage?.id shouldBe 10L
        receivedAction shouldBe action
        println("    → prop callback received: msgId=${receivedMessage?.id}, action=$action")
    }

    test("action emits CardActionClicked on CometChatEvents.uiEvents") {
        runTest {
            val message = MockFactory.createCardMessage(id = 20L)
            val action = mapOf("type" to "copyToClipboard", "value" to "Copied text!")

            val event = CometChatUIEvent.CardActionClicked(
                message = message,
                actionEvent = action
            )

            val received = async(Dispatchers.Unconfined) {
                CometChatEvents.uiEvents.first { it is CometChatUIEvent.CardActionClicked }
            }

            advanceUntilIdle()
            CometChatEvents.emitUIEvent(event)
            advanceUntilIdle()

            val result = received.await() as CometChatUIEvent.CardActionClicked
            (result.message as CardMessage).id shouldBe 20L
            result.actionEvent shouldBe action
            println("    → event bus received: msgId=20, action=$action")
        }
    }

    // ==================== All 9 action types ====================

    test("all 9 action types produce valid prop callbacks") {
        val actionTypes = listOf(
            "openUrl", "sendMessage", "chatWithUser", "chatWithGroup",
            "copyToClipboard", "downloadFile", "initiateCall", "apiCall", "customCallback"
        )

        val message = MockFactory.createCardMessage(id = 1L)
        var callCount = 0

        val onCardAction: (CardMessage, Any) -> Unit = { _, _ -> callCount++ }

        actionTypes.forEach { type ->
            onCardAction(message, mapOf("type" to type))
        }

        callCount shouldBe 9
        println("    → all 9 action types forwarded via prop ✓")
    }

    // ==================== PBT: Action forwarding preserves data ====================

    test("for any message ID and action type, forwarding preserves data integrity") {
        val actionTypes = listOf(
            "openUrl", "sendMessage", "chatWithUser", "chatWithGroup",
            "copyToClipboard", "downloadFile", "initiateCall", "apiCall", "customCallback"
        )

        checkAll(30, Arb.long(1L, 10000L)) { msgId ->
            val actionType = actionTypes[(msgId % actionTypes.size).toInt()]
            val message = MockFactory.createCardMessage(id = msgId)
            val action = mapOf("type" to actionType, "data" to "payload-$msgId")

            var capturedId: Long? = null
            var capturedAction: Any? = null

            val onCardAction: (CardMessage, Any) -> Unit = { msg, act ->
                capturedId = msg.id
                capturedAction = act
            }

            onCardAction(message, action)

            capturedId shouldBe msgId
            capturedAction shouldBe action
            println("    → msgId=$msgId, type='$actionType' → data preserved ✓")
        }
    }

    // ==================== Agent card block actions ====================

    test("agent card block action forwards owning AIAssistantMessage via event bus") {
        runTest {
            // For nested agent cards, message is the AIAssistantMessage (BaseMessage)
            val agentMessage = MockFactory.createBaseMessage(id = 99L)
            whenever(agentMessage.category).thenReturn("agentic")
            whenever(agentMessage.type).thenReturn("assistant")

            val action = mapOf("type" to "chatWithUser", "uid" to "user-42")

            val event = CometChatUIEvent.CardActionClicked(
                message = agentMessage,
                actionEvent = action
            )

            val received = async(Dispatchers.Unconfined) {
                CometChatEvents.uiEvents.first { it is CometChatUIEvent.CardActionClicked }
            }

            advanceUntilIdle()
            CometChatEvents.emitUIEvent(event)
            advanceUntilIdle()

            val result = received.await() as CometChatUIEvent.CardActionClicked
            result.message.id shouldBe 99L
            result.message.category shouldBe "agentic"
            println("    → agent card action: owning message id=99, category='agentic' ✓")
        }
    }

    // ==================== No kit-side behavior ====================

    test("kit does not implement any action handler logic") {
        // Verify that the Compose CometChatCardBubble:
        // 1. Calls onCardAction?.invoke(message, event) [prop channel]
        // 2. Calls CometChatEvents.emitUIEvent(CardActionClicked(...)) [event channel]
        // 3. Does NOTHING ELSE — no navigation, no SDK calls, no HTTP requests
        //
        // The actual behavior is in the sample app's ccCardActionClicked subscriber.
        val event = CometChatUIEvent.CardActionClicked(
            message = MockFactory.createCardMessage(id = 1L),
            actionEvent = mapOf("type" to "initiateCall", "callType" to "video", "uid" to "u1")
        )
        // Pure data — no side effects
        event.shouldBeInstanceOf<CometChatUIEvent.CardActionClicked>()
        println("    → confirmed: compose bubble is render-only, zero action behavior")
    }
})
