package com.cometchat.uikit.core.state

import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for UIState and DeleteState sealed classes used by the Conversations component.
 * Verifies construction, type checking, property access, and data class equality.
 *
 * Reference: UsersUIStateTest.kt
 */
class ConversationUIStateTest : FunSpec({

    // ==================== UIState ====================

    context("UIState") {

        test("Loading should be a singleton instance") {
            val state: UIState = UIState.Loading
            state.shouldBeInstanceOf<UIState.Loading>()
        }

        test("Empty should be a singleton instance") {
            val state: UIState = UIState.Empty
            state.shouldBeInstanceOf<UIState.Empty>()
        }

        test("Content should hold the conversations list") {
            val conversations = MockFactory.createUserConversations(3)
            val state = UIState.Content(conversations)

            state.shouldBeInstanceOf<UIState.Content>()
            state.conversations shouldBe conversations
            state.conversations.size shouldBe 3
        }

        test("Content data class equality should work") {
            val conversations = MockFactory.createUserConversations(2)
            val state1 = UIState.Content(conversations)
            val state2 = UIState.Content(conversations)

            state1 shouldBe state2
        }

        test("Error should hold the exception") {
            val exception = MockFactory.createCometChatException("ERR", "Something went wrong")
            val state = UIState.Error(exception)

            state.shouldBeInstanceOf<UIState.Error>()
            state.exception shouldBe exception
            state.exception.code shouldBe "ERR"
        }

        test("Error data class equality should work") {
            val ex = MockFactory.createCometChatException("ERR", "msg")
            val state1 = UIState.Error(ex)
            val state2 = UIState.Error(ex)

            state1 shouldBe state2
        }
    }

    // ==================== DeleteState ====================

    context("DeleteState") {

        test("Idle should be a singleton instance") {
            val state: DeleteState = DeleteState.Idle
            state.shouldBeInstanceOf<DeleteState.Idle>()
        }

        test("InProgress should be a singleton instance") {
            val state: DeleteState = DeleteState.InProgress
            state.shouldBeInstanceOf<DeleteState.InProgress>()
        }

        test("Success should be a singleton instance") {
            val state: DeleteState = DeleteState.Success
            state.shouldBeInstanceOf<DeleteState.Success>()
        }

        test("Failure should hold the exception") {
            val exception = MockFactory.createCometChatException("ERR_DELETE", "Delete failed")
            val state = DeleteState.Failure(exception)

            state.shouldBeInstanceOf<DeleteState.Failure>()
            state.exception shouldBe exception
            state.exception.code shouldBe "ERR_DELETE"
        }

        test("Failure data class equality should work") {
            val ex = MockFactory.createCometChatException("ERR", "msg")
            val state1 = DeleteState.Failure(ex)
            val state2 = DeleteState.Failure(ex)

            state1 shouldBe state2
        }
    }
})
