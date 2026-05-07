package com.cometchat.uikit.core.state

import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for UsersUIState sealed class.
 * Verifies construction, type checking, and property access for all variants.
 */
class UsersUIStateTest : FunSpec({

    test("Loading should be a singleton instance") {
        val state: UsersUIState = UsersUIState.Loading
        state.shouldBeInstanceOf<UsersUIState.Loading>()
    }

    test("Empty should be a singleton instance") {
        val state: UsersUIState = UsersUIState.Empty
        state.shouldBeInstanceOf<UsersUIState.Empty>()
    }

    test("Content should be a singleton instance") {
        val state: UsersUIState = UsersUIState.Content
        state.shouldBeInstanceOf<UsersUIState.Content>()
    }

    test("Error should hold the exception") {
        val exception = MockFactory.createCometChatException("ERR", "Something went wrong")
        val state = UsersUIState.Error(exception)

        state.shouldBeInstanceOf<UsersUIState.Error>()
        state.exception shouldBe exception
        state.exception.code shouldBe "ERR"
    }

    test("Error data class equality should work") {
        val ex1 = MockFactory.createCometChatException("ERR", "msg")
        val state1 = UsersUIState.Error(ex1)
        val state2 = UsersUIState.Error(ex1)

        state1 shouldBe state2
    }
})
