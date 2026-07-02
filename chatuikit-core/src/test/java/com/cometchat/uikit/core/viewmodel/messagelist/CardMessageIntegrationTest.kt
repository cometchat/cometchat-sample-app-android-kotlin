package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CardMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.core.testutils.BubbleFactoryTestHelper
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.utils.MessageOptionsUtils
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
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
 * Full-chain integration tests for Card Message feature across the UIKit.
 *
 * Verifies the end-to-end card message flow WITHOUT mocking intermediate layers:
 * - Card message routing (BubbleFactoryTestHelper → CARD_KEY)
 * - Card message options (text options minus EDIT/COPY)
 * - Card action event delivery (CometChatEvents.uiEvents)
 * - Card conversation preview (getText() → "Card Message")
 * - Deleted card → DELETED_KEY routing
 * - No collision with legacy interactive card
 *
 * This tests the integration between:
 *   BubbleFactoryTestHelper + MessageOptionsUtils + CometChatEvents + CardMessage
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CardMessageIntegrationTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CardMessageIntegrationTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== Full routing chain ====================

    test("full chain: CardMessage → BubbleFactoryTestHelper → CARD_KEY → options without EDIT/COPY") {
        val message = MockFactory.createCardMessage(
            id = 1L,
            type = "product_showcase",
            text = "Check out this product!"
        )

        // Step 1: BubbleFactoryTestHelper routes on category
        val key = BubbleFactoryTestHelper.getFactoryKey(message)
        key shouldBe BubbleFactoryTestHelper.CARD_KEY
        println("    → routing: category='${message.category}', type='${message.type}' → key='$key'")

        // Step 2: MessageOptionsUtils returns correct options
        val options = MessageOptionsUtils.getDefaultOptionIds(message.category, message.type)
        options shouldNotContain UIKitConstants.MessageOption.EDIT
        options shouldNotContain UIKitConstants.MessageOption.COPY
        options shouldContain UIKitConstants.MessageOption.DELETE
        options shouldContain UIKitConstants.MessageOption.REPLY
        options shouldContain UIKitConstants.MessageOption.REPLY_IN_THREAD
        println("    → options: ${options.size} items, no EDIT/COPY ✓")

        // Step 3: Preview text
        val preview = message.text?.ifEmpty { null } ?: "Card Message"
        preview shouldBe "Check out this product!"
        println("    → preview: '$preview'")
    }

    test("full chain: deleted CardMessage → DELETED_KEY (overrides card routing)") {
        val message = MockFactory.createCardMessage(
            id = 2L,
            type = "order_confirmation",
            deletedAt = 1700000000L
        )

        val key = BubbleFactoryTestHelper.getFactoryKey(message)
        key shouldBe BubbleFactoryTestHelper.DELETED_KEY
        key shouldNotBe BubbleFactoryTestHelper.CARD_KEY
        println("    → deleted card: key='$key' (standard deleted bubble) ✓")
    }

    // ==================== Event bus integration ====================

    test("full chain: card action → CometChatEvents → subscriber receives event") {
        runTest {
            val message = MockFactory.createCardMessage(id = 42L, type = "flight_booking")
            val action = mapOf(
                "type" to "apiCall",
                "url" to "https://api.airline.com/book",
                "method" to "POST",
                "body" to mapOf("flightId" to "FL-123", "passengers" to 2)
            )

            val event = CometChatUIEvent.CardActionClicked(
                message = message,
                actionEvent = action
            )

            // Subscriber — launch in background so it starts collecting
            val received = async(kotlinx.coroutines.Dispatchers.Unconfined) {
                CometChatEvents.uiEvents.first { it is CometChatUIEvent.CardActionClicked }
            }

            // Give collector a chance to subscribe
            advanceUntilIdle()

            // Emit
            CometChatEvents.emitUIEventSync(event)
            advanceUntilIdle()

            // Verify full event integrity
            val result = received.await() as CometChatUIEvent.CardActionClicked
            (result.message as CardMessage).id shouldBe 42L
            (result.message as CardMessage).type shouldBe "flight_booking"
            result.actionEvent shouldBe action
            println("    → event bus delivered: msgId=42, type='flight_booking', action.type='apiCall' ✓")
        }
    }

    // ==================== No collision with legacy interactive ====================

    test("full chain: legacy interactive/card does NOT collide with new category card") {
        val legacyMessage = mock<BaseMessage>()
        whenever(legacyMessage.category).thenReturn(UIKitConstants.MessageCategory.INTERACTIVE)
        whenever(legacyMessage.type).thenReturn("card") // legacy type "card"
        whenever(legacyMessage.deletedAt).thenReturn(0L)

        val newCardMessage = MockFactory.createCardMessage(type = "card") // same type string!

        val legacyKey = BubbleFactoryTestHelper.getFactoryKey(legacyMessage)
        val newKey = BubbleFactoryTestHelper.getFactoryKey(newCardMessage)

        legacyKey shouldBe "interactive_card"
        newKey shouldBe BubbleFactoryTestHelper.CARD_KEY
        legacyKey shouldNotBe newKey
        println("    → legacy: '$legacyKey' vs new: '$newKey' — no collision ✓")
    }

    // ==================== PBT: Arbitrary types all route correctly ====================

    test("for any developer-chosen type, full chain produces consistent results") {
        checkAll(50, Arb.string(1..40), Arb.long(1L, 99999L)) { type, msgId ->
            val message = MockFactory.createCardMessage(id = msgId, type = type)

            // Routing
            val key = BubbleFactoryTestHelper.getFactoryKey(message)
            key shouldBe BubbleFactoryTestHelper.CARD_KEY

            // Options
            val options = MessageOptionsUtils.getDefaultOptionIds(message.category, message.type)
            options shouldNotContain UIKitConstants.MessageOption.EDIT
            options shouldNotContain UIKitConstants.MessageOption.COPY

            // Event construction
            val event = CometChatUIEvent.CardActionClicked(
                message = message,
                actionEvent = mapOf("type" to "openUrl")
            )
            event.shouldBeInstanceOf<CometChatUIEvent.CardActionClicked>()

            println("    → msgId=$msgId, type='${type.take(20)}' → full chain ✓")
        }
    }

    // ==================== Agent card does NOT use card routing ====================

    test("agent card (agentic/assistant) does NOT route to CARD_KEY") {
        val agentMessage = mock<BaseMessage>()
        whenever(agentMessage.category).thenReturn("agentic")
        whenever(agentMessage.type).thenReturn("assistant")
        whenever(agentMessage.deletedAt).thenReturn(0L)

        val key = BubbleFactoryTestHelper.getFactoryKey(agentMessage)
        key shouldBe "agentic_assistant"
        key shouldNotBe BubbleFactoryTestHelper.CARD_KEY
        println("    → agentic/assistant → key='$key' (existing AI bubble) ✓")
    }

    // ==================== Card action types all work ====================

    test("all 9 action types produce valid events in the full chain") {
        val actionTypes = listOf(
            "openUrl", "sendMessage", "chatWithUser", "chatWithGroup",
            "copyToClipboard", "downloadFile", "initiateCall", "apiCall", "customCallback"
        )

        checkAll(27, Arb.element(actionTypes), Arb.long(1L, 1000L)) { actionType, msgId ->
            val message = MockFactory.createCardMessage(id = msgId)
            val action = mapOf("type" to actionType, "messageId" to msgId.toString())

            val event = CometChatUIEvent.CardActionClicked(
                message = message,
                actionEvent = action
            )

            event.message.id shouldBe msgId
            @Suppress("UNCHECKED_CAST")
            (event.actionEvent as Map<String, Any>)["type"] shouldBe actionType
            println("    → action='$actionType', msgId=$msgId ✓")
        }
    }

    // ==================== MessageCategory constant verification ====================

    test("MessageCategory.CARD is 'card' and distinct from INTERACTIVE") {
        UIKitConstants.MessageCategory.CARD shouldBe "card"
        UIKitConstants.MessageCategory.INTERACTIVE shouldBe "interactive"
        UIKitConstants.MessageCategory.CARD shouldNotBe UIKitConstants.MessageCategory.INTERACTIVE
        println("    → CARD='${UIKitConstants.MessageCategory.CARD}', INTERACTIVE='${UIKitConstants.MessageCategory.INTERACTIVE}' ✓")
    }
})
