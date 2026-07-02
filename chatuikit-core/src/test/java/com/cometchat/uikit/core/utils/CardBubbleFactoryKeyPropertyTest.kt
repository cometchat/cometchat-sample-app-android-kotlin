package com.cometchat.uikit.core.utils

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CardMessage
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.BubbleFactoryTestHelper
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Property-based tests for BubbleFactory.getFactoryKey() card message routing.
 *
 * Card messages route on category alone — the developer-chosen type is arbitrary
 * and ignored during routing. This ensures a single card view handles all developer types.
 *
 * Key invariants:
 * - category "card" → BubbleFactoryTestHelper.CARD_KEY (= "card") regardless of type
 * - deleted card messages → BubbleFactoryTestHelper.DELETED_KEY (same as any deleted message)
 * - non-card messages → standard "category_type" format
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CardBubbleFactoryKeyPropertyTest"
 */
class CardBubbleFactoryKeyPropertyTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== Card category routing ====================

    test("card message with any arbitrary type routes to CARD_KEY") {
        checkAll(100, Arb.string(1..50)) { arbitraryType ->
            val message = mock<CardMessage>()
            whenever(message.category).thenReturn(UIKitConstants.MessageCategory.CARD)
            whenever(message.type).thenReturn(arbitraryType)
            whenever(message.deletedAt).thenReturn(0L)

            val key = BubbleFactoryTestHelper.getFactoryKey(message)
            println("    → type='$arbitraryType' → key='$key'")

            key shouldBe BubbleFactoryTestHelper.CARD_KEY
        }
    }

    test("CARD_KEY constant equals 'card'") {
        println("    → CARD_KEY = '${BubbleFactoryTestHelper.CARD_KEY}'")
        BubbleFactoryTestHelper.CARD_KEY shouldBe "card"
    }

    test("deleted card message routes to DELETED_KEY, not CARD_KEY") {
        val message = mock<CardMessage>()
        whenever(message.category).thenReturn(UIKitConstants.MessageCategory.CARD)
        whenever(message.type).thenReturn("buy_button")
        whenever(message.deletedAt).thenReturn(1700000000L) // deleted

        val key = BubbleFactoryTestHelper.getFactoryKey(message)
        println("    → deleted card → key='$key'")

        key shouldBe BubbleFactoryTestHelper.DELETED_KEY
        key shouldNotBe BubbleFactoryTestHelper.CARD_KEY
    }

    test("non-card message uses standard category_type key format") {
        val textMessage = MockFactory.createTextMessage(id = 1L, text = "Hello")
        val key = BubbleFactoryTestHelper.getFactoryKey(textMessage)
        println("    → text message → key='$key'")

        key shouldBe "message_text"
        key shouldNotBe BubbleFactoryTestHelper.CARD_KEY
    }

    test("card message with empty type still routes to CARD_KEY") {
        val message = mock<CardMessage>()
        whenever(message.category).thenReturn(UIKitConstants.MessageCategory.CARD)
        whenever(message.type).thenReturn("")
        whenever(message.deletedAt).thenReturn(0L)

        val key = BubbleFactoryTestHelper.getFactoryKey(message)
        println("    → empty type → key='$key'")

        key shouldBe BubbleFactoryTestHelper.CARD_KEY
    }

    // ==================== No collision with legacy interactive card ====================

    test("interactive card (legacy) does NOT route to CARD_KEY") {
        val message = mock<BaseMessage>()
        whenever(message.category).thenReturn(UIKitConstants.MessageCategory.INTERACTIVE)
        whenever(message.type).thenReturn("card") // legacy type "card" under interactive
        whenever(message.deletedAt).thenReturn(0L)

        val key = BubbleFactoryTestHelper.getFactoryKey(message)
        println("    → interactive/card (legacy) → key='$key'")

        key shouldBe "interactive_card"
        key shouldNotBe BubbleFactoryTestHelper.CARD_KEY
    }

    // ==================== getKey utility ====================

    test("getKey creates category_type format") {
        val key = BubbleFactoryTestHelper.getKey("agentic", "assistant")
        println("    → getKey('agentic', 'assistant') = '$key'")
        key shouldBe "agentic_assistant"
    }
})
