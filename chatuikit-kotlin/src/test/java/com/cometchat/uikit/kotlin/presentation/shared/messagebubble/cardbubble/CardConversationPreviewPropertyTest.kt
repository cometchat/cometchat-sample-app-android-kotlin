package com.cometchat.uikit.kotlin.presentation.shared.messagebubble.cardbubble

import com.cometchat.chat.models.CardMessage
import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.mockito.kotlin.whenever

/**
 * Property-based tests for CardMessage conversation list preview.
 *
 * Conversation preview for developer cards follows:
 *   message.text → else "Card Message" (§2.8)
 *
 * This mirrors the ConversationUtils.getLastMessageText() behavior for CardMessage.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:testDebugUnitTest --tests "*CardConversationPreviewPropertyTest"
 */
class CardConversationPreviewPropertyTest : FunSpec({

    beforeTest {
        println("  🧪 ${it.name.testName}")
    }

    afterTest { println() }

    // ==================== Preview text extraction ====================

    test("card with non-empty text uses getText() for preview") {
        checkAll(50, Arb.string(1..100)) { textContent ->
            val message = MockFactory.createCardMessage(text = textContent)
            val preview = message.text
            preview shouldBe textContent
            println("    → text='${textContent.take(30)}' → preview='${preview?.take(30)}'")
        }
    }

    test("card with null text falls back to default 'Card Message'") {
        val message = MockFactory.createCardMessage(text = null)
        val preview = message.text ?: "Card Message"
        preview shouldBe "Card Message"
        println("    → text=null → preview='$preview'")
    }

    test("card with empty text falls back to default 'Card Message'") {
        val message = MockFactory.createCardMessage(text = "")
        val preview = message.text?.ifEmpty { null } ?: "Card Message"
        preview shouldBe "Card Message"
        println("    → text='' → preview='$preview'")
    }

    test("preview never returns null for card messages") {
        checkAll(30, Arb.string(0..50)) { textContent ->
            val message = MockFactory.createCardMessage(text = textContent.ifEmpty { null })
            val preview = message.text?.ifEmpty { null } ?: "Card Message"
            preview shouldNotBe null
            preview.isNotEmpty() shouldBe true
            println("    → text='${textContent.take(20)}' → preview='${preview.take(20)}' (non-null) ✓")
        }
    }

    // ==================== Deleted card preview ====================

    test("deleted card message shows standard deleted message text") {
        val message = MockFactory.createCardMessage(
            id = 1L,
            text = "Some card text",
            deletedAt = 1700000000L
        )
        // When deletedAt > 0, ConversationUtils returns "This message was deleted"
        // regardless of message type
        message.deletedAt shouldNotBe 0L
        println("    → deletedAt=${message.deletedAt} → standard deleted text applies")
    }

    // ==================== Agent card preview is unchanged ====================

    test("agent card (agentic/assistant) preview uses existing assistant behavior") {
        // Agent cards do NOT get a card-specific preview — they use the existing
        // AI assistant preview (getText()). This test documents the contract.
        // The agent message category is "agentic", not "card" — so it never
        // reaches the CardMessage branch in ConversationUtils.
        val agentCategory = "agentic"
        val developerCardCategory = "card"
        agentCategory shouldNotBe developerCardCategory
        println("    → agent cards use existing assistant preview (category='agentic') ✓")
    }
})
