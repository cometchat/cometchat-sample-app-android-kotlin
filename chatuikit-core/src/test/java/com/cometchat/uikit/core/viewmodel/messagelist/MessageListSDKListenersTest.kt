package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
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
 * Layer 5K — SDK Listeners Tests.
 *
 * Tests the CometChatMessageListViewModel's handling of SDK listener events.
 * Since we use `enableListeners = false`, we test the public methods that
 * the listeners would call (addMessage, updateMessage, removeMessage) and
 * verify the ViewModel's behavior for:
 * - Message received (text/media/custom) adds to list
 * - Message edited/deleted updates/removes from list
 * - Typing started/ended updates typingUsers (not testable without listeners)
 * - Messages for other conversations ignored
 * - Group action messages handled
 *
 * Architecture:
 * - Mocks the MessageListRepository interface (layer directly below)
 * - Uses `enableListeners = false` to avoid SDK dependencies
 * - Uses UnconfinedTestDispatcher + Dispatchers.setMain/resetMain
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListSDKListenersTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListSDKListenersTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var repository: MessageListRepository

    /**
     * Creates a ViewModel configured with a user and pre-loaded messages.
     */
    suspend fun createViewModelWithMessages(
        messages: List<BaseMessage>,
        userUid: String = "test-user"
    ): CometChatMessageListViewModel {
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        val vm = CometChatMessageListViewModel(
            repository = repository,
            enableListeners = false
        )
        val user = MockFactory.createUser(uid = userUid, name = "Test User")
        vm.setUser(user)
        vm.fetchMessages()
        return vm
    }

    /**
     * Creates a ViewModel configured for a group conversation.
     */
    suspend fun createGroupViewModel(
        messages: List<BaseMessage>,
        groupGuid: String = "test-group"
    ): CometChatMessageListViewModel {
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        val vm = CometChatMessageListViewModel(
            repository = repository,
            enableListeners = false
        )
        val group = MockFactory.createGroup(guid = groupGuid, name = "Test Group")
        vm.setGroup(group)
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
        try { runTest { } } catch (_: Exception) { }
    }


    // ==================== 16.2 Message received adds to list ====================

    test("addMessage adds text message to list for current user conversation") {
        runTest {
            val existingMessages = MockFactory.createMessages(3, startId = 1L, senderUid = "test-user", receiverId = "logged-in-user")

            val vm = createViewModelWithMessages(existingMessages.map { it as BaseMessage })
            advanceUntilIdle()

            val initialCount = vm.getItemCount()

            // Simulate incoming message from the configured user (test-user)
            // In a user conversation configured with setUser("test-user"),
            // isMessageForCurrentChat checks: id == message.sender?.uid
            val newMessage = MockFactory.createTextMessage(
                id = 10L,
                text = "New incoming",
                senderUid = "test-user",  // FROM the configured user
                receiverId = "logged-in-user",
                receiverType = CometChatConstants.RECEIVER_TYPE_USER
            )

            vm.addMessage(newMessage as BaseMessage)
            advanceUntilIdle()

            vm.getItemCount() shouldBe initialCount + 1
            println("    ✅ Text message added to list for current conversation")
        }
    }

    test("addMessage adds media message to list for current user conversation") {
        runTest {
            val existingMessages = MockFactory.createMessages(3, startId = 1L, senderUid = "test-user", receiverId = "logged-in-user")

            val vm = createViewModelWithMessages(existingMessages.map { it as BaseMessage })
            advanceUntilIdle()

            val initialCount = vm.getItemCount()

            val mediaMessage = MockFactory.createMediaMessage(
                id = 10L,
                type = CometChatConstants.MESSAGE_TYPE_IMAGE,
                senderUid = "test-user",  // FROM the configured user
                receiverId = "logged-in-user",
                receiverType = CometChatConstants.RECEIVER_TYPE_USER
            )

            vm.addMessage(mediaMessage as BaseMessage)
            advanceUntilIdle()

            vm.getItemCount() shouldBe initialCount + 1
            println("    ✅ Media message added to list for current conversation")
        }
    }

    test("addMessage adds custom message to list for current user conversation") {
        runTest {
            val existingMessages = MockFactory.createMessages(3, startId = 1L, senderUid = "test-user", receiverId = "logged-in-user")

            val vm = createViewModelWithMessages(existingMessages.map { it as BaseMessage })
            advanceUntilIdle()

            val initialCount = vm.getItemCount()

            val customMessage = MockFactory.createCustomMessage(
                id = 10L,
                customType = "extension_poll",
                senderUid = "test-user",  // FROM the configured user
                receiverId = "logged-in-user",
                receiverType = CometChatConstants.RECEIVER_TYPE_USER
            )

            vm.addMessage(customMessage as BaseMessage)
            advanceUntilIdle()

            vm.getItemCount() shouldBe initialCount + 1
            println("    ✅ Custom message added to list for current conversation")
        }
    }


    // ==================== 16.3 Message edited/deleted updates/removes from list ====================

    test("updateMessage updates existing message in list") {
        runTest {
            val messages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            // Create an edited version of message 3
            val editedMessage = MockFactory.createTextMessage(
                id = 3L,
                text = "Edited text",
                senderUid = "other-user",
                receiverId = "test-user",
                editedAt = 1700000100L
            )

            vm.updateMessage(editedMessage as BaseMessage)
            advanceUntilIdle()

            // Count should remain the same
            vm.getItemCount() shouldBe 5
            // The message at that position should be the edited one
            val updatedItem = vm.getItems().find { it.id == 3L }
            updatedItem shouldBe editedMessage
            println("    ✅ updateMessage updates existing message in list")
        }
    }

    test("removeMessage removes message from list") {
        runTest {
            val messages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val messageToRemove = messages[2]
            vm.removeMessage(messageToRemove as BaseMessage)
            advanceUntilIdle()

            vm.getItemCount() shouldBe 4
            vm.getItems().none { it.id == messageToRemove.id } shouldBe true
            println("    ✅ removeMessage removes message from list")
        }
    }

    test("removeMessage with hideDeleteMessage=true removes deleted message") {
        runTest {
            val messages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            vm.setHideDeleteMessage(true)

            val deletedMessage = MockFactory.createTextMessage(
                id = 3L,
                text = "Deleted",
                senderUid = "other-user",
                receiverId = "test-user",
                deletedAt = 1700000100L
            )

            // Simulate what SDK listener does: remove when hideDeleteMessage=true
            vm.removeMessage(deletedMessage as BaseMessage)
            advanceUntilIdle()

            vm.getItemCount() shouldBe 4
            println("    ✅ Deleted message removed when hideDeleteMessage=true")
        }
    }

    test("updateMessage with hideDeleteMessage=false updates deleted message in-place") {
        runTest {
            val messages = MockFactory.createMessages(5, startId = 1L, senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            vm.setHideDeleteMessage(false)

            val deletedMessage = MockFactory.createTextMessage(
                id = 3L,
                text = "This message was deleted",
                senderUid = "other-user",
                receiverId = "test-user",
                deletedAt = 1700000100L
            )

            // Simulate what SDK listener does: update when hideDeleteMessage=false
            vm.updateMessage(deletedMessage as BaseMessage)
            advanceUntilIdle()

            vm.getItemCount() shouldBe 5
            val updatedItem = vm.getItems().find { it.id == 3L }
            updatedItem shouldBe deletedMessage
            println("    ✅ Deleted message updated in-place when hideDeleteMessage=false")
        }
    }


    // ==================== 16.5 Delivery/read receipts update messages ====================

    test("updateMessageReadAt updates message readAt timestamp") {
        runTest {
            val messages = MockFactory.createMessages(5, startId = 1L, senderUid = "test-user", receiverId = "other-user")

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            // The ViewModel has an internal updateMessageReadAt method called by receipt handlers
            // We test the public updateMessage which is what the receipt handler ultimately calls
            val updatedMessage = MockFactory.createTextMessage(
                id = 3L,
                text = "Message 3",
                senderUid = "test-user",
                receiverId = "other-user",
                readAt = 1700000200L
            )

            vm.updateMessage(updatedMessage as BaseMessage)
            advanceUntilIdle()

            val item = vm.getItems().find { it.id == 3L }
            item shouldBe updatedMessage
            println("    ✅ Message readAt updated via updateMessage")
        }
    }


    // ==================== 16.7 Messages for other conversations ignored ====================

    test("addMessage ignores message for different user conversation") {
        runTest {
            val messages = MockFactory.createMessages(3, startId = 1L, senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val initialCount = vm.getItemCount()

            // Message for a different conversation (different sender AND receiver)
            val otherMessage = MockFactory.createTextMessage(
                id = 100L,
                text = "Not for me",
                senderUid = "stranger",
                receiverId = "another-user",
                receiverType = CometChatConstants.RECEIVER_TYPE_USER
            )

            vm.addMessage(otherMessage as BaseMessage)
            advanceUntilIdle()

            // Should NOT be added
            vm.getItemCount() shouldBe initialCount
            println("    ✅ Message for different user conversation ignored")
        }
    }

    test("addMessage ignores message for different group conversation") {
        runTest {
            val messages = MockFactory.createMessages(3, startId = 1L, senderUid = "other-user", receiverId = "test-group",
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP)

            val vm = createGroupViewModel(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val initialCount = vm.getItemCount()

            // Message for a different group
            val otherMessage = MockFactory.createTextMessage(
                id = 100L,
                text = "Not for my group",
                senderUid = "other-user",
                receiverId = "different-group",
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
            )

            vm.addMessage(otherMessage as BaseMessage)
            advanceUntilIdle()

            // Should NOT be added
            vm.getItemCount() shouldBe initialCount
            println("    ✅ Message for different group conversation ignored")
        }
    }

    test("PBT: messages for other conversations are always ignored") {
        checkAll(20, Arb.string(5..15), Arb.long(1L..1000L)) { otherUid, messageId ->
            runTest {
                val messages = MockFactory.createMessages(3, startId = 1L, senderUid = "other-user", receiverId = "test-user")
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages.map { it as BaseMessage }))

                val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
                advanceUntilIdle()

                val initialCount = vm.getItemCount()

                // Message between two other users (not involving test-user)
                val otherMessage = MockFactory.createTextMessage(
                    id = messageId,
                    text = "Other conversation",
                    senderUid = otherUid,
                    receiverId = "yet-another-user",
                    receiverType = CometChatConstants.RECEIVER_TYPE_USER
                )

                vm.addMessage(otherMessage as BaseMessage)
                advanceUntilIdle()

                vm.getItemCount() shouldBe initialCount
                println("    → otherUid=$otherUid, msgId=$messageId: ignored")
            }
        }
        println("    ✅ PBT: messages for other conversations always ignored")
    }


    // ==================== 16.8 Group action messages handled ====================

    test("addMessage adds group action message for current group") {
        runTest {
            val messages = MockFactory.createMessages(
                3, startId = 1L, senderUid = "other-user",
                receiverId = "test-group",
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
            )

            val vm = createGroupViewModel(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val initialCount = vm.getItemCount()

            val actionMessage = MockFactory.createActionMessage(
                id = 10L,
                action = CometChatConstants.ActionKeys.ACTION_JOINED,
                senderUid = "new-member",
                receiverId = "test-group"
            )

            vm.addMessage(actionMessage as BaseMessage)
            advanceUntilIdle()

            vm.getItemCount() shouldBe initialCount + 1
            println("    ✅ Group action message added for current group")
        }
    }

    test("addMessage ignores group action message for different group") {
        runTest {
            val messages = MockFactory.createMessages(
                3, startId = 1L, senderUid = "other-user",
                receiverId = "test-group",
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
            )

            val vm = createGroupViewModel(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val initialCount = vm.getItemCount()

            val actionMessage = MockFactory.createActionMessage(
                id = 10L,
                action = CometChatConstants.ActionKeys.ACTION_JOINED,
                senderUid = "new-member",
                receiverId = "other-group"  // Different group
            )

            vm.addMessage(actionMessage as BaseMessage)
            advanceUntilIdle()

            vm.getItemCount() shouldBe initialCount
            println("    ✅ Group action message for different group ignored")
        }
    }


    // ==================== Duplicate detection ====================

    test("addMessage deduplicates by message ID") {
        runTest {
            val messages = MockFactory.createMessages(3, startId = 1L, senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val initialCount = vm.getItemCount()

            // Try to add a message with same ID as existing
            val duplicateMessage = MockFactory.createTextMessage(
                id = 2L,  // Same ID as messages[1]
                text = "Duplicate",
                senderUid = "other-user",
                receiverId = "test-user",
                receiverType = CometChatConstants.RECEIVER_TYPE_USER
            )

            vm.addMessage(duplicateMessage as BaseMessage)
            advanceUntilIdle()

            // Should NOT increase count (updates instead)
            vm.getItemCount() shouldBe initialCount
            println("    ✅ addMessage deduplicates by message ID")
        }
    }

    test("addMessage deduplicates by muid") {
        runTest {
            val messages = MockFactory.createMessages(3, startId = 1L, senderUid = "other-user", receiverId = "test-user")
            // Set muid on first message
            whenever(messages[0].muid).thenReturn("unique-muid-1")

            val vm = createViewModelWithMessages(messages.map { it as BaseMessage })
            advanceUntilIdle()

            val initialCount = vm.getItemCount()

            // Try to add a message with same muid
            val duplicateMessage = MockFactory.createTextMessage(
                id = 100L,  // Different ID
                text = "Duplicate by muid",
                senderUid = "other-user",
                receiverId = "test-user",
                receiverType = CometChatConstants.RECEIVER_TYPE_USER
            )
            whenever(duplicateMessage.muid).thenReturn("unique-muid-1")  // Same muid

            vm.addMessage(duplicateMessage as BaseMessage)
            advanceUntilIdle()

            // Should NOT increase count (updates instead)
            vm.getItemCount() shouldBe initialCount
            println("    ✅ addMessage deduplicates by muid")
        }
    }
})
