package com.cometchat.uikit.core.state

import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs

/**
 * Tests for MessageHeaderUIState sealed class.
 * Layer 4 — pure data class tests, no mocking needed.
 *
 * Reference: ConversationUIStateTest.kt
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageHeaderUIStateTest"
 */
class MessageHeaderUIStateTest : FunSpec({

    beforeTest { println("  🧪 ${it.name.testName}") }
    afterTest { println() }

    // ==================== Loading ====================

    test("Loading should be a singleton object instance") {
        val state: MessageHeaderUIState = MessageHeaderUIState.Loading
        println("    → state=$state")
        state.shouldBeInstanceOf<MessageHeaderUIState.Loading>()
    }

    test("Loading should always return the same instance") {
        val state1 = MessageHeaderUIState.Loading
        val state2 = MessageHeaderUIState.Loading
        println("    → state1===state2: ${state1 === state2}")
        state1 shouldBeSameInstanceAs state2
    }

    // ==================== UserContent ====================

    test("UserContent should hold the user object") {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        val state = MessageHeaderUIState.UserContent(user)
        println("    → state.user.uid=${state.user.uid}, name=${state.user.name}")
        state.shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
        state.user.uid shouldBe "user-1"
        state.user.name shouldBe "Alice"
    }

    test("UserContent data class equality should work correctly") {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        val state1 = MessageHeaderUIState.UserContent(user)
        val state2 = MessageHeaderUIState.UserContent(user)
        println("    → state1==state2: ${state1 == state2}")
        state1 shouldBe state2
    }

    test("UserContent with different users should not be equal") {
        val user1 = MockFactory.createUser(uid = "user-1", name = "Alice")
        val user2 = MockFactory.createUser(uid = "user-2", name = "Bob")
        val state1 = MessageHeaderUIState.UserContent(user1)
        val state2 = MessageHeaderUIState.UserContent(user2)
        println("    → state1==state2: ${state1 == state2}")
        (state1 == state2) shouldBe false
    }

    // ==================== GroupContent ====================

    test("GroupContent should hold the group object") {
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers", membersCount = 10)
        val state = MessageHeaderUIState.GroupContent(group)
        println("    → state.group.guid=${state.group.guid}, name=${state.group.name}, members=${state.group.membersCount}")
        state.shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
        state.group.guid shouldBe "group-1"
        state.group.name shouldBe "Developers"
        state.group.membersCount shouldBe 10
    }

    test("GroupContent data class equality should work correctly") {
        val group = MockFactory.createGroup(guid = "group-1", name = "Developers")
        val state1 = MessageHeaderUIState.GroupContent(group)
        val state2 = MessageHeaderUIState.GroupContent(group)
        println("    → state1==state2: ${state1 == state2}")
        state1 shouldBe state2
    }

    test("GroupContent with different groups should not be equal") {
        val group1 = MockFactory.createGroup(guid = "group-1", name = "Developers")
        val group2 = MockFactory.createGroup(guid = "group-2", name = "Designers")
        val state1 = MessageHeaderUIState.GroupContent(group1)
        val state2 = MessageHeaderUIState.GroupContent(group2)
        println("    → state1==state2: ${state1 == state2}")
        (state1 == state2) shouldBe false
    }

    // ==================== Error ====================

    test("Error should hold the exception") {
        val exception = MockFactory.createCometChatException("ERR_FETCH", "Failed to fetch")
        val state = MessageHeaderUIState.Error(exception)
        println("    → state.exception.code=${state.exception.code}, message=${state.exception.message}")
        state.shouldBeInstanceOf<MessageHeaderUIState.Error>()
        state.exception.code shouldBe "ERR_FETCH"
        state.exception.message shouldBe "Failed to fetch"
    }

    test("Error data class equality should work correctly") {
        val exception = MockFactory.createCometChatException("ERR", "Error")
        val state1 = MessageHeaderUIState.Error(exception)
        val state2 = MessageHeaderUIState.Error(exception)
        println("    → state1==state2: ${state1 == state2}")
        state1 shouldBe state2
    }

    // ==================== Type discrimination ====================

    test("all states should be distinguishable via when expression") {
        val states = listOf(
            MessageHeaderUIState.Loading,
            MessageHeaderUIState.UserContent(MockFactory.createUser()),
            MessageHeaderUIState.GroupContent(MockFactory.createGroup()),
            MessageHeaderUIState.Error(MockFactory.createCometChatException())
        )

        states.forEachIndexed { index, state ->
            val typeName = when (state) {
                is MessageHeaderUIState.Loading -> "Loading"
                is MessageHeaderUIState.UserContent -> "UserContent"
                is MessageHeaderUIState.GroupContent -> "GroupContent"
                is MessageHeaderUIState.Error -> "Error"
            }
            println("    → states[$index] = $typeName")
        }

        states[0].shouldBeInstanceOf<MessageHeaderUIState.Loading>()
        states[1].shouldBeInstanceOf<MessageHeaderUIState.UserContent>()
        states[2].shouldBeInstanceOf<MessageHeaderUIState.GroupContent>()
        states[3].shouldBeInstanceOf<MessageHeaderUIState.Error>()
    }
})
