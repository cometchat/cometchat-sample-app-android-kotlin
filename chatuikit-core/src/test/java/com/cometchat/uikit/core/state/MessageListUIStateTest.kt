package com.cometchat.uikit.core.state

import com.cometchat.uikit.core.testutils.MockFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for MessageListUIState, MessageDeleteState, MessageFlagState sealed classes
 * and MessageAlignment enum used by the MessageList component.
 * Pure data class tests — no mocking needed (except for BaseMessage in Success states).
 *
 * Reference: ConversationUIStateTest.kt
 */
class MessageListUIStateTest : FunSpec({

    // ==================== MessageListUIState ====================

    context("MessageListUIState") {

        test("Loading should be a singleton instance") {
            println("TRACE: Creating MessageListUIState.Loading")
            val state: MessageListUIState = MessageListUIState.Loading
            println("TRACE: Verifying instance type is Loading")
            state.shouldBeInstanceOf<MessageListUIState.Loading>()
        }

        test("Empty should be a singleton instance") {
            println("TRACE: Creating MessageListUIState.Empty")
            val state: MessageListUIState = MessageListUIState.Empty
            println("TRACE: Verifying instance type is Empty")
            state.shouldBeInstanceOf<MessageListUIState.Empty>()
        }

        test("Loaded should be a singleton instance") {
            println("TRACE: Creating MessageListUIState.Loaded")
            val state: MessageListUIState = MessageListUIState.Loaded
            println("TRACE: Verifying instance type is Loaded")
            state.shouldBeInstanceOf<MessageListUIState.Loaded>()
        }

        test("Error should hold the exception") {
            println("TRACE: Creating CometChatException with code ERR_FETCH")
            val exception = MockFactory.createCometChatException("ERR_FETCH", "Failed to load messages")
            println("TRACE: Creating MessageListUIState.Error with exception")
            val state = MessageListUIState.Error(exception)

            println("TRACE: Verifying instance type is Error")
            state.shouldBeInstanceOf<MessageListUIState.Error>()
            println("TRACE: Verifying exception is stored correctly")
            state.exception shouldBe exception
        }

        test("Error data class equality should work") {
            println("TRACE: Creating two Error states with same exception")
            val exception = MockFactory.createCometChatException("ERR", "msg")
            val state1 = MessageListUIState.Error(exception)
            val state2 = MessageListUIState.Error(exception)

            println("TRACE: Verifying equality")
            state1 shouldBe state2
        }

        test("Error data class inequality with different exceptions") {
            println("TRACE: Creating two Error states with different exceptions")
            val ex1 = MockFactory.createCometChatException("ERR_1", "First error")
            val ex2 = MockFactory.createCometChatException("ERR_2", "Second error")
            val state1 = MessageListUIState.Error(ex1)
            val state2 = MessageListUIState.Error(ex2)

            println("TRACE: Verifying inequality")
            (state1 == state2) shouldBe false
        }

        test("All four states should be distinct types") {
            println("TRACE: Creating all four MessageListUIState variants")
            val loading: MessageListUIState = MessageListUIState.Loading
            val empty: MessageListUIState = MessageListUIState.Empty
            val loaded: MessageListUIState = MessageListUIState.Loaded
            val error: MessageListUIState = MessageListUIState.Error(
                MockFactory.createCometChatException("ERR", "error")
            )

            println("TRACE: Verifying each is a distinct type via when expression")
            val states = listOf(loading, empty, loaded, error)
            val typeNames = states.map { state ->
                when (state) {
                    is MessageListUIState.Loading -> "Loading"
                    is MessageListUIState.Empty -> "Empty"
                    is MessageListUIState.Loaded -> "Loaded"
                    is MessageListUIState.Error -> "Error"
                }
            }
            typeNames shouldBe listOf("Loading", "Empty", "Loaded", "Error")
        }
    }

    // ==================== MessageDeleteState ====================

    context("MessageDeleteState") {

        test("Idle should be a singleton instance") {
            println("TRACE: Creating MessageDeleteState.Idle")
            val state: MessageDeleteState = MessageDeleteState.Idle
            println("TRACE: Verifying instance type is Idle")
            state.shouldBeInstanceOf<MessageDeleteState.Idle>()
        }

        test("InProgress should be a singleton instance") {
            println("TRACE: Creating MessageDeleteState.InProgress")
            val state: MessageDeleteState = MessageDeleteState.InProgress
            println("TRACE: Verifying instance type is InProgress")
            state.shouldBeInstanceOf<MessageDeleteState.InProgress>()
        }

        test("Success should hold the deleted message") {
            println("TRACE: Creating a mock TextMessage for delete success")
            val message = MockFactory.createTextMessage(
                id = 42L,
                text = "Message to delete",
                senderUid = "user-1",
                receiverId = "user-2"
            )
            println("TRACE: Creating MessageDeleteState.Success with message")
            val state = MessageDeleteState.Success(message)

            println("TRACE: Verifying instance type is Success")
            state.shouldBeInstanceOf<MessageDeleteState.Success>()
            println("TRACE: Verifying message is stored correctly")
            state.message shouldBe message
        }

        test("Success data class equality should work") {
            println("TRACE: Creating two Success states with same message")
            val message = MockFactory.createTextMessage(id = 1L)
            val state1 = MessageDeleteState.Success(message)
            val state2 = MessageDeleteState.Success(message)

            println("TRACE: Verifying equality")
            state1 shouldBe state2
        }

        test("Error should hold the exception") {
            println("TRACE: Creating CometChatException for delete failure")
            val exception = MockFactory.createCometChatException("ERR_DELETE", "Delete failed")
            println("TRACE: Creating MessageDeleteState.Error with exception")
            val state = MessageDeleteState.Error(exception)

            println("TRACE: Verifying instance type is Error")
            state.shouldBeInstanceOf<MessageDeleteState.Error>()
            println("TRACE: Verifying exception is stored correctly")
            state.exception shouldBe exception
        }

        test("Error data class equality should work") {
            println("TRACE: Creating two Error states with same exception")
            val ex = MockFactory.createCometChatException("ERR", "msg")
            val state1 = MessageDeleteState.Error(ex)
            val state2 = MessageDeleteState.Error(ex)

            println("TRACE: Verifying equality")
            state1 shouldBe state2
        }

        test("All four states should be distinct types") {
            println("TRACE: Creating all four MessageDeleteState variants")
            val idle: MessageDeleteState = MessageDeleteState.Idle
            val inProgress: MessageDeleteState = MessageDeleteState.InProgress
            val success: MessageDeleteState = MessageDeleteState.Success(
                MockFactory.createTextMessage(id = 1L)
            )
            val error: MessageDeleteState = MessageDeleteState.Error(
                MockFactory.createCometChatException("ERR", "error")
            )

            println("TRACE: Verifying each is a distinct type via when expression")
            val states = listOf(idle, inProgress, success, error)
            val typeNames = states.map { state ->
                when (state) {
                    is MessageDeleteState.Idle -> "Idle"
                    is MessageDeleteState.InProgress -> "InProgress"
                    is MessageDeleteState.Success -> "Success"
                    is MessageDeleteState.Error -> "Error"
                }
            }
            typeNames shouldBe listOf("Idle", "InProgress", "Success", "Error")
        }
    }

    // ==================== MessageFlagState ====================

    context("MessageFlagState") {

        test("Idle should be a singleton instance") {
            println("TRACE: Creating MessageFlagState.Idle")
            val state: MessageFlagState = MessageFlagState.Idle
            println("TRACE: Verifying instance type is Idle")
            state.shouldBeInstanceOf<MessageFlagState.Idle>()
        }

        test("InProgress should be a singleton instance") {
            println("TRACE: Creating MessageFlagState.InProgress")
            val state: MessageFlagState = MessageFlagState.InProgress
            println("TRACE: Verifying instance type is InProgress")
            state.shouldBeInstanceOf<MessageFlagState.InProgress>()
        }

        test("Success should be a singleton instance") {
            println("TRACE: Creating MessageFlagState.Success")
            val state: MessageFlagState = MessageFlagState.Success
            println("TRACE: Verifying instance type is Success")
            state.shouldBeInstanceOf<MessageFlagState.Success>()
        }

        test("Error should hold the exception") {
            println("TRACE: Creating CometChatException for flag failure")
            val exception = MockFactory.createCometChatException("ERR_FLAG", "Flag failed")
            println("TRACE: Creating MessageFlagState.Error with exception")
            val state = MessageFlagState.Error(exception)

            println("TRACE: Verifying instance type is Error")
            state.shouldBeInstanceOf<MessageFlagState.Error>()
            println("TRACE: Verifying exception is stored correctly")
            state.exception shouldBe exception
        }

        test("Error data class equality should work") {
            println("TRACE: Creating two Error states with same exception")
            val ex = MockFactory.createCometChatException("ERR", "msg")
            val state1 = MessageFlagState.Error(ex)
            val state2 = MessageFlagState.Error(ex)

            println("TRACE: Verifying equality")
            state1 shouldBe state2
        }

        test("All four states should be distinct types") {
            println("TRACE: Creating all four MessageFlagState variants")
            val idle: MessageFlagState = MessageFlagState.Idle
            val inProgress: MessageFlagState = MessageFlagState.InProgress
            val success: MessageFlagState = MessageFlagState.Success
            val error: MessageFlagState = MessageFlagState.Error(
                MockFactory.createCometChatException("ERR", "error")
            )

            println("TRACE: Verifying each is a distinct type via when expression")
            val states = listOf(idle, inProgress, success, error)
            val typeNames = states.map { state ->
                when (state) {
                    is MessageFlagState.Idle -> "Idle"
                    is MessageFlagState.InProgress -> "InProgress"
                    is MessageFlagState.Success -> "Success"
                    is MessageFlagState.Error -> "Error"
                }
            }
            typeNames shouldBe listOf("Idle", "InProgress", "Success", "Error")
        }
    }

    // ==================== MessageAlignment ====================

    context("MessageAlignment") {

        test("LEFT value should exist") {
            println("TRACE: Accessing MessageAlignment.LEFT")
            val alignment = MessageAlignment.LEFT
            println("TRACE: Verifying name is LEFT")
            alignment.name shouldBe "LEFT"
        }

        test("RIGHT value should exist") {
            println("TRACE: Accessing MessageAlignment.RIGHT")
            val alignment = MessageAlignment.RIGHT
            println("TRACE: Verifying name is RIGHT")
            alignment.name shouldBe "RIGHT"
        }

        test("CENTER value should exist") {
            println("TRACE: Accessing MessageAlignment.CENTER")
            val alignment = MessageAlignment.CENTER
            println("TRACE: Verifying name is CENTER")
            alignment.name shouldBe "CENTER"
        }

        test("MessageAlignment should have exactly 3 values") {
            println("TRACE: Getting all MessageAlignment enum values")
            val values = MessageAlignment.values()
            println("TRACE: Verifying count is 3")
            values.size shouldBe 3
        }

        test("MessageAlignment ordinals should be sequential") {
            println("TRACE: Verifying ordinal values")
            MessageAlignment.LEFT.ordinal shouldBe 0
            MessageAlignment.RIGHT.ordinal shouldBe 1
            MessageAlignment.CENTER.ordinal shouldBe 2
        }

        test("MessageAlignment valueOf should resolve correctly") {
            println("TRACE: Testing valueOf for each alignment")
            MessageAlignment.valueOf("LEFT") shouldBe MessageAlignment.LEFT
            MessageAlignment.valueOf("RIGHT") shouldBe MessageAlignment.RIGHT
            MessageAlignment.valueOf("CENTER") shouldBe MessageAlignment.CENTER
        }
    }
})
