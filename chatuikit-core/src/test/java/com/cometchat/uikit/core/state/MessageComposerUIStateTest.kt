package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for MessageComposerUIState and ComposerPanelEvent sealed classes.
 * Layer 4 — Pure data class tests, no mocking needed for state construction.
 *
 * Covers:
 * - MessageComposerUIState: Idle, Sending, Editing, Replying, AIGenerating, Success, Error
 * - ComposerPanelEvent: ShowTopPanel, ShowBottomPanel, CloseTopPanel, CloseBottomPanel
 */
class MessageComposerUIStateTest : FunSpec({

    // ==================== MessageComposerUIState ====================

    beforeTest {
        println("\n  🧪 ${it.name.testName}")
        println("  ─────────────────────────────────────────────────")
    }

    afterTest {
        println()
    }

    context("MessageComposerUIState") {

        test("Idle should be a singleton object instance") {
            val state: MessageComposerUIState = MessageComposerUIState.Idle
            state.shouldBeInstanceOf<MessageComposerUIState.Idle>()
            println("    ✅ PASSED — MessageComposerUIState.Idle is singleton")
        }

        test("Sending should be a singleton object instance") {
            val state: MessageComposerUIState = MessageComposerUIState.Sending
            state.shouldBeInstanceOf<MessageComposerUIState.Sending>()
            println("    ✅ PASSED — MessageComposerUIState.Sending is singleton")
        }

        test("Editing should hold the original message being edited") {
            val mockMessage = mock<BaseMessage>()
            whenever(mockMessage.id).thenReturn(101)

            val state = MessageComposerUIState.Editing(mockMessage)

            state.shouldBeInstanceOf<MessageComposerUIState.Editing>()
            state.message shouldBe mockMessage
            state.message.id shouldBe 101
            println("    ✅ PASSED — Editing holds message.id=${state.message.id}")
        }

        test("Replying should hold the message being replied to") {
            val mockMessage = mock<BaseMessage>()
            whenever(mockMessage.id).thenReturn(202)

            val state = MessageComposerUIState.Replying(mockMessage)

            state.shouldBeInstanceOf<MessageComposerUIState.Replying>()
            state.message shouldBe mockMessage
            state.message.id shouldBe 202
            println("    ✅ PASSED — Replying holds message.id=${state.message.id}")
        }

        test("AIGenerating should be a singleton object instance") {
            val state: MessageComposerUIState = MessageComposerUIState.AIGenerating
            state.shouldBeInstanceOf<MessageComposerUIState.AIGenerating>()
            println("    ✅ PASSED — MessageComposerUIState.AIGenerating is singleton")
        }

        test("Success should hold the sent or edited message") {
            val mockMessage = mock<BaseMessage>()
            whenever(mockMessage.id).thenReturn(303)

            val state = MessageComposerUIState.Success(mockMessage)

            state.shouldBeInstanceOf<MessageComposerUIState.Success>()
            state.message shouldBe mockMessage
            state.message.id shouldBe 303
            println("    ✅ PASSED — Success holds message.id=${state.message.id}")
        }

        test("Error should hold the CometChatException") {
            val exception = CometChatException("ERR_SEND", "Send failed", "Send failed")

            val state = MessageComposerUIState.Error(exception)

            state.shouldBeInstanceOf<MessageComposerUIState.Error>()
            state.exception shouldBe exception
            state.exception.code shouldBe "ERR_SEND"
            println("    ✅ PASSED — Error holds exception.code=${state.exception.code}")
        }

        test("Editing and Replying are mutually exclusive states") {
            val editMessage = mock<BaseMessage>()
            val replyMessage = mock<BaseMessage>()

            val editState = MessageComposerUIState.Editing(editMessage)
            val replyState = MessageComposerUIState.Replying(replyMessage)

            // They should be different types
            (editState is MessageComposerUIState.Editing) shouldBe true
            (editState is MessageComposerUIState.Replying) shouldBe false
            (replyState is MessageComposerUIState.Replying) shouldBe true
            (replyState is MessageComposerUIState.Editing) shouldBe false
            println("    ✅ PASSED — Editing/Replying mutual exclusivity verified")
        }

        test("all state types should be distinguishable via when expression") {
            val states = listOf(
                MessageComposerUIState.Idle,
                MessageComposerUIState.Sending,
                MessageComposerUIState.Editing(mock()),
                MessageComposerUIState.Replying(mock()),
                MessageComposerUIState.AIGenerating,
                MessageComposerUIState.Success(mock()),
                MessageComposerUIState.Error(CometChatException("E", "e", "e"))
            )

            val typeNames = states.map { state ->
                when (state) {
                    is MessageComposerUIState.Idle -> "Idle"
                    is MessageComposerUIState.Sending -> "Sending"
                    is MessageComposerUIState.Editing -> "Editing"
                    is MessageComposerUIState.Replying -> "Replying"
                    is MessageComposerUIState.AIGenerating -> "AIGenerating"
                    is MessageComposerUIState.Success -> "Success"
                    is MessageComposerUIState.Error -> "Error"
                }
            }

            typeNames shouldBe listOf("Idle", "Sending", "Editing", "Replying", "AIGenerating", "Success", "Error")
            println("    ✅ PASSED — All 7 state types distinguishable: $typeNames")
        }
    }

    // ==================== ComposerPanelEvent ====================

    context("ComposerPanelEvent") {

        test("ShowTopPanel should hold a view provider function") {
            val viewProvider: (android.content.Context) -> android.view.View = { ctx ->
                android.view.View(ctx)
            }

            val event = ComposerPanelEvent.ShowTopPanel(viewProvider)

            event.shouldBeInstanceOf<ComposerPanelEvent.ShowTopPanel>()
            event.viewProvider shouldBe viewProvider
            println("    ✅ PASSED — ShowTopPanel holds viewProvider")
        }

        test("ShowBottomPanel should hold a view provider function") {
            val viewProvider: (android.content.Context) -> android.view.View = { ctx ->
                android.view.View(ctx)
            }

            val event = ComposerPanelEvent.ShowBottomPanel(viewProvider)

            event.shouldBeInstanceOf<ComposerPanelEvent.ShowBottomPanel>()
            event.viewProvider shouldBe viewProvider
            println("    ✅ PASSED — ShowBottomPanel holds viewProvider")
        }

        test("CloseTopPanel should be a singleton object instance") {
            val event: ComposerPanelEvent = ComposerPanelEvent.CloseTopPanel
            event.shouldBeInstanceOf<ComposerPanelEvent.CloseTopPanel>()
            println("    ✅ PASSED — CloseTopPanel is singleton")
        }

        test("CloseBottomPanel should be a singleton object instance") {
            val event: ComposerPanelEvent = ComposerPanelEvent.CloseBottomPanel
            event.shouldBeInstanceOf<ComposerPanelEvent.CloseBottomPanel>()
            println("    ✅ PASSED — CloseBottomPanel is singleton")
        }

        test("all panel event types should be distinguishable via when expression") {
            val viewProvider: (android.content.Context) -> android.view.View = { ctx ->
                android.view.View(ctx)
            }

            val events = listOf(
                ComposerPanelEvent.ShowTopPanel(viewProvider),
                ComposerPanelEvent.ShowBottomPanel(viewProvider),
                ComposerPanelEvent.CloseTopPanel,
                ComposerPanelEvent.CloseBottomPanel
            )

            val typeNames = events.map { event ->
                when (event) {
                    is ComposerPanelEvent.ShowTopPanel -> "ShowTopPanel"
                    is ComposerPanelEvent.ShowBottomPanel -> "ShowBottomPanel"
                    is ComposerPanelEvent.CloseTopPanel -> "CloseTopPanel"
                    is ComposerPanelEvent.CloseBottomPanel -> "CloseBottomPanel"
                }
            }

            typeNames shouldBe listOf("ShowTopPanel", "ShowBottomPanel", "CloseTopPanel", "CloseBottomPanel")
            println("    ✅ PASSED — All 4 panel event types distinguishable: $typeNames")
        }
    }
})
