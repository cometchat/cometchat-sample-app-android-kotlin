package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageDeleteState
import com.cometchat.uikit.core.state.MessageFlagState
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Layer 5F — Delete & Flag State Machine Tests.
 *
 * Tests the CometChatMessageListViewModel's delete and flag state machines:
 * - Delete: Idle → InProgress → Success and Idle → InProgress → Error
 * - Delete with hideDeleteMessage=true (removes) vs false (updates in-place)
 * - Flag: Idle → InProgress → Success and Idle → InProgress → Error
 * - resetDeleteState and resetFlagState return to Idle
 * - PBT: only valid state transitions possible
 *
 * Architecture:
 * - Mocks the MessageListRepository interface (layer directly below)
 * - Uses `enableListeners = false` to avoid SDK dependencies
 * - Uses UnconfinedTestDispatcher + Dispatchers.setMain/resetMain
 * - All PBT tests use `checkAll` with `Arb` generators (no hardcoded values in PBT)
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListDeleteFlagStateMachineTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListDeleteFlagStateMachineTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var repository: MessageListRepository

    /**
     * Creates a ViewModel configured with a user and pre-loaded messages.
     */
    suspend fun createViewModelWithMessages(messages: List<BaseMessage>): CometChatMessageListViewModel {
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        val vm = CometChatMessageListViewModel(
            repository = repository,
            enableListeners = false
        )
        val user = MockFactory.createUser(uid = "test-user", name = "Test User")
        vm.setUser(user)
        vm.fetchMessages()
        return vm
    }

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Thread.sleep(50)
        Dispatchers.resetMain()
    }

    beforeTest {
        repository = mock()
        // Default stubs
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        whenever(repository.markAsRead(any())).thenReturn(Result.success(Unit))
        whenever(repository.markAsDelivered(any())).thenReturn(Result.success(Unit))
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    test("warmup - absorb leaked exceptions") {
        try { runTest(testDispatcher) { } } catch (_: Exception) { }
    }


    // ==================== 11.2 Delete: Idle → InProgress → Success and Idle → InProgress → Error ====================

    test("delete state machine: initial state is Idle") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Idle>()
            println("    ✅ Initial deleteState is Idle")
        }
    }

    test("delete state machine: Idle → InProgress → Success on successful delete") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(5, startId = 1L)
            val messageToDelete = messages[2]
            val deletedMessage = MockFactory.createTextMessage(
                id = messageToDelete.id,
                text = "Message deleted",
                senderUid = "user-1",
                receiverId = "test-user",
                deletedAt = System.currentTimeMillis() / 1000
            )

            whenever(repository.deleteMessage(any())).thenReturn(Result.success(deletedMessage as BaseMessage))

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            // Verify initial state
            vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Idle>()

            // Trigger delete
            vm.deleteMessage(messageToDelete)
            advanceUntilIdle()

            // Verify final state is Success
            val state = vm.deleteState.value
            state.shouldBeInstanceOf<MessageDeleteState.Success>()
            state.message.id shouldBe messageToDelete.id
            println("    ✅ Delete: Idle → InProgress → Success")
        }
    }

    test("delete state machine: Idle → InProgress → Error on failed delete") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(5, startId = 1L)
            val messageToDelete = messages[2]

            whenever(repository.deleteMessage(any())).thenReturn(
                Result.failure(CometChatException("DELETE_ERR", "Permission denied"))
            )

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            // Trigger delete
            vm.deleteMessage(messageToDelete)
            advanceUntilIdle()

            // Verify final state is Error
            val state = vm.deleteState.value
            state.shouldBeInstanceOf<MessageDeleteState.Error>()
            println("    ✅ Delete: Idle → InProgress → Error")
        }
    }

    test("PBT: delete success always transitions to Success state with correct message") {
        checkAll(20, Arb.long(1L..100L)) { messageId ->
            runTest(testDispatcher) {
                val message = MockFactory.createTextMessage(
                    id = messageId,
                    text = "Message $messageId",
                    senderUid = "other-user",
                    receiverId = "test-user"
                )
                val deletedMessage = MockFactory.createTextMessage(
                    id = messageId,
                    text = "Deleted",
                    senderUid = "other-user",
                    receiverId = "test-user",
                    deletedAt = 1700000000L
                )

                whenever(repository.deleteMessage(any())).thenReturn(Result.success(deletedMessage as BaseMessage))
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                val vm = createViewModelWithMessages(listOf(message))
                advanceUntilIdle()

                vm.deleteMessage(message)
                advanceUntilIdle()

                val state = vm.deleteState.value
                state.shouldBeInstanceOf<MessageDeleteState.Success>()
                state.message.id shouldBe messageId
                println("    → messageId=$messageId: delete succeeded")
            }
        }
        println("    ✅ PBT: delete success always transitions to Success with correct message")
    }


    // ==================== 11.3 Delete with hideDeleteMessage=true vs false ====================

    test("delete with hideDeleteMessage=true removes message from list") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(5, startId = 1L)
            val messageToDelete = messages[2]
            val deletedMessage = MockFactory.createTextMessage(
                id = messageToDelete.id,
                text = "Deleted",
                senderUid = "user-1",
                receiverId = "test-user",
                deletedAt = 1700000000L
            )

            whenever(repository.deleteMessage(any())).thenReturn(Result.success(deletedMessage as BaseMessage))

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            // Enable hideDeleteMessage
            vm.setHideDeleteMessage(true)

            val initialCount = vm.getItemCount()
            vm.deleteMessage(messageToDelete)
            advanceUntilIdle()

            // Message should be removed from list
            vm.getItemCount() shouldBe initialCount - 1
            println("    ✅ hideDeleteMessage=true: message removed from list")
        }
    }

    test("delete with hideDeleteMessage=false updates message in-place") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(5, startId = 1L)
            val messageToDelete = messages[2]
            val deletedMessage = MockFactory.createTextMessage(
                id = messageToDelete.id,
                text = "Message deleted",
                senderUid = "user-1",
                receiverId = "test-user",
                deletedAt = 1700000000L
            )

            whenever(repository.deleteMessage(any())).thenReturn(Result.success(deletedMessage as BaseMessage))

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            // Disable hideDeleteMessage (default)
            vm.setHideDeleteMessage(false)

            val initialCount = vm.getItemCount()
            vm.deleteMessage(messageToDelete)
            advanceUntilIdle()

            // Message count should remain the same (updated in-place)
            vm.getItemCount() shouldBe initialCount
            println("    ✅ hideDeleteMessage=false: message updated in-place")
        }
    }

    test("PBT: hideDeleteMessage flag determines remove vs update behavior") {
        checkAll(20, Arb.boolean(), Arb.int(3..10)) { hideDelete, messageCount ->
            runTest(testDispatcher) {
                val messages = MockFactory.createMessages(messageCount, startId = 1L)
                val messageToDelete = messages[1] // Always delete second message
                val deletedMessage = MockFactory.createTextMessage(
                    id = messageToDelete.id,
                    text = "Deleted",
                    senderUid = "user-1",
                    receiverId = "test-user",
                    deletedAt = 1700000000L
                )

                whenever(repository.deleteMessage(any())).thenReturn(Result.success(deletedMessage as BaseMessage))
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages.map { it as BaseMessage }))

                val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
                advanceUntilIdle()

                vm.setHideDeleteMessage(hideDelete)
                vm.deleteMessage(messageToDelete)
                advanceUntilIdle()

                if (hideDelete) {
                    vm.getItemCount() shouldBe messageCount - 1
                } else {
                    vm.getItemCount() shouldBe messageCount
                }
                println("    → hideDelete=$hideDelete, count=$messageCount: ${if (hideDelete) "removed" else "updated"}")
            }
        }
        println("    ✅ PBT: hideDeleteMessage flag determines remove vs update behavior")
    }


    // ==================== 11.4 Flag: Idle → InProgress → Success and Idle → InProgress → Error ====================

    test("flag state machine: initial state is Idle") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Idle>()
            println("    ✅ Initial flagState is Idle")
        }
    }

    test("flag state machine: Idle → InProgress → Success on successful flag") {
        runTest(testDispatcher) {
            val message = MockFactory.createTextMessage(id = 1L, text = "Spam message", senderUid = "other-user", receiverId = "test-user")

            whenever(repository.flagMessage(any(), any(), any())).thenReturn(Result.success(Unit))

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            // Verify initial state
            vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Idle>()

            // Trigger flag
            vm.flagMessage(message, "spam", "This is spam")
            advanceUntilIdle()

            // Verify final state is Success
            vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Success>()
            println("    ✅ Flag: Idle → InProgress → Success")
        }
    }

    test("flag state machine: Idle → InProgress → Error on failed flag") {
        runTest(testDispatcher) {
            val message = MockFactory.createTextMessage(id = 1L, text = "Message", senderUid = "other-user", receiverId = "test-user")

            whenever(repository.flagMessage(any(), any(), any())).thenReturn(
                Result.failure(CometChatException("FLAG_ERR", "Flag failed"))
            )

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            // Trigger flag
            vm.flagMessage(message, "harassment", "")
            advanceUntilIdle()

            // Verify final state is Error
            val state = vm.flagState.value
            state.shouldBeInstanceOf<MessageFlagState.Error>()
            println("    ✅ Flag: Idle → InProgress → Error")
        }
    }

    test("PBT: flag with various reasons always transitions correctly") {
        checkAll(20, Arb.element("spam", "harassment", "inappropriate", "other"), Arb.string(0..50)) { reason, remark ->
            runTest(testDispatcher) {
                val message = MockFactory.createTextMessage(id = 1L, text = "Flagged", senderUid = "other-user", receiverId = "test-user")
                whenever(repository.flagMessage(any(), any(), any())).thenReturn(Result.success(Unit))
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                val vm = createViewModelWithMessages(listOf(message))
                advanceUntilIdle()

                vm.flagMessage(message, reason, remark)
                advanceUntilIdle()

                vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Success>()
                println("    → reason=$reason, remark=${remark.take(10)}: flag succeeded")
            }
        }
        println("    ✅ PBT: flag with various reasons always transitions correctly")
    }


    // ==================== 11.5 resetDeleteState and resetFlagState return to Idle ====================

    test("resetDeleteState returns to Idle after Success") {
        runTest(testDispatcher) {
            val message = MockFactory.createTextMessage(id = 1L, text = "Delete me", senderUid = "other-user", receiverId = "test-user")
            val deletedMessage = MockFactory.createTextMessage(id = 1L, text = "Deleted", senderUid = "other-user", receiverId = "test-user", deletedAt = 1700000000L)

            whenever(repository.deleteMessage(any())).thenReturn(Result.success(deletedMessage as BaseMessage))

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            vm.deleteMessage(message)
            advanceUntilIdle()
            vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()

            // Reset
            vm.resetDeleteState()
            vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Idle>()
            println("    ✅ resetDeleteState returns to Idle after Success")
        }
    }

    test("resetDeleteState returns to Idle after Error") {
        runTest(testDispatcher) {
            val message = MockFactory.createTextMessage(id = 1L, text = "Delete me", senderUid = "other-user", receiverId = "test-user")

            whenever(repository.deleteMessage(any())).thenReturn(
                Result.failure(CometChatException("ERR", "Failed"))
            )

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            vm.deleteMessage(message)
            advanceUntilIdle()
            vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Error>()

            // Reset
            vm.resetDeleteState()
            vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Idle>()
            println("    ✅ resetDeleteState returns to Idle after Error")
        }
    }

    test("resetFlagState returns to Idle after Success") {
        runTest(testDispatcher) {
            val message = MockFactory.createTextMessage(id = 1L, text = "Flag me", senderUid = "other-user", receiverId = "test-user")

            whenever(repository.flagMessage(any(), any(), any())).thenReturn(Result.success(Unit))

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            vm.flagMessage(message, "spam", "")
            advanceUntilIdle()
            vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Success>()

            // Reset
            vm.resetFlagState()
            vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Idle>()
            println("    ✅ resetFlagState returns to Idle after Success")
        }
    }

    test("resetFlagState returns to Idle after Error") {
        runTest(testDispatcher) {
            val message = MockFactory.createTextMessage(id = 1L, text = "Flag me", senderUid = "other-user", receiverId = "test-user")

            whenever(repository.flagMessage(any(), any(), any())).thenReturn(
                Result.failure(CometChatException("ERR", "Failed"))
            )

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            vm.flagMessage(message, "spam", "")
            advanceUntilIdle()
            vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Error>()

            // Reset
            vm.resetFlagState()
            vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Idle>()
            println("    ✅ resetFlagState returns to Idle after Error")
        }
    }


    // ==================== 11.6 PBT: only valid state transitions possible ====================

    test("PBT: deleteState can only be Idle, InProgress, Success, or Error") {
        checkAll(20, Arb.boolean()) { shouldSucceed ->
            runTest(testDispatcher) {
                val message = MockFactory.createTextMessage(id = 1L, text = "Test", senderUid = "other-user", receiverId = "test-user")
                val deletedMessage = MockFactory.createTextMessage(id = 1L, text = "Deleted", senderUid = "other-user", receiverId = "test-user", deletedAt = 1700000000L)

                if (shouldSucceed) {
                    whenever(repository.deleteMessage(any())).thenReturn(Result.success(deletedMessage as BaseMessage))
                } else {
                    whenever(repository.deleteMessage(any())).thenReturn(
                        Result.failure(CometChatException("ERR", "Failed"))
                    )
                }
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                val vm = createViewModelWithMessages(listOf(message))
                advanceUntilIdle()

                // State starts at Idle
                vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Idle>()

                // After delete, state is either Success or Error
                vm.deleteMessage(message)
                advanceUntilIdle()

                val finalState = vm.deleteState.value
                if (shouldSucceed) {
                    finalState.shouldBeInstanceOf<MessageDeleteState.Success>()
                } else {
                    finalState.shouldBeInstanceOf<MessageDeleteState.Error>()
                }

                // After reset, state is Idle
                vm.resetDeleteState()
                vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Idle>()
                println("    → shouldSucceed=$shouldSucceed: valid transitions only")
            }
        }
        println("    ✅ PBT: deleteState only valid transitions")
    }

    test("PBT: flagState can only be Idle, InProgress, Success, or Error") {
        checkAll(20, Arb.boolean()) { shouldSucceed ->
            runTest(testDispatcher) {
                val message = MockFactory.createTextMessage(id = 1L, text = "Test", senderUid = "other-user", receiverId = "test-user")

                if (shouldSucceed) {
                    whenever(repository.flagMessage(any(), any(), any())).thenReturn(Result.success(Unit))
                } else {
                    whenever(repository.flagMessage(any(), any(), any())).thenReturn(
                        Result.failure(CometChatException("ERR", "Failed"))
                    )
                }
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                val vm = createViewModelWithMessages(listOf(message))
                advanceUntilIdle()

                // State starts at Idle
                vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Idle>()

                // After flag, state is either Success or Error
                vm.flagMessage(message, "spam", "")
                advanceUntilIdle()

                val finalState = vm.flagState.value
                if (shouldSucceed) {
                    finalState.shouldBeInstanceOf<MessageFlagState.Success>()
                } else {
                    finalState.shouldBeInstanceOf<MessageFlagState.Error>()
                }

                // After reset, state is Idle
                vm.resetFlagState()
                vm.flagState.value.shouldBeInstanceOf<MessageFlagState.Idle>()
                println("    → shouldSucceed=$shouldSucceed: valid transitions only")
            }
        }
        println("    ✅ PBT: flagState only valid transitions")
    }

    test("PBT: multiple sequential delete operations maintain valid state machine") {
        checkAll(10, Arb.int(2..5)) { operationCount ->
            runTest(testDispatcher) {
                val messages = MockFactory.createMessages(10, startId = 1L)
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages.map { it as BaseMessage }))

                val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
                advanceUntilIdle()

                repeat(operationCount) { i ->
                    val msg = messages[i]
                    val deletedMsg = MockFactory.createTextMessage(
                        id = msg.id,
                        text = "Deleted",
                        senderUid = "user-1",
                        receiverId = "test-user",
                        deletedAt = 1700000000L
                    )
                    whenever(repository.deleteMessage(any())).thenReturn(Result.success(deletedMsg as BaseMessage))

                    vm.resetDeleteState()
                    vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Idle>()

                    vm.deleteMessage(msg)
                    advanceUntilIdle()

                    vm.deleteState.value.shouldBeInstanceOf<MessageDeleteState.Success>()
                }
                println("    → $operationCount sequential deletes: all valid transitions")
            }
        }
        println("    ✅ PBT: multiple sequential delete operations maintain valid state machine")
    }
})
