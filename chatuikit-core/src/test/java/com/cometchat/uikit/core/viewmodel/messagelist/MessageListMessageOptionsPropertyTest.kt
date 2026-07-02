package com.cometchat.uikit.core.viewmodel.messagelist

import android.content.Context
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.model.CometChatMessageOption
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Layer 5I — Message Options Property-Based Tests.
 *
 * Tests the CometChatMessageListViewModel's message options handling:
 * - setOptions non-null replaces all defaults
 * - setOptions null falls through, addOptions appends
 * - Both callbacks null → defaults unchanged
 * - handleMessageOptionClick for each option ID
 * - handleMessageOptionClick returns false for unknown ID
 *
 * Architecture:
 * - Mocks the MessageListRepository interface (layer directly below)
 * - Uses `enableListeners = false` to avoid SDK dependencies
 * - Uses UnconfinedTestDispatcher + Dispatchers.setMain/resetMain
 * - All PBT tests use `checkAll` with `Arb` generators
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListMessageOptionsPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListMessageOptionsPropertyTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var repository: MessageListRepository
    lateinit var mockContext: Context
    lateinit var cometChatMock: MockedStatic<CometChat>

    /**
     * Creates a ViewModel configured with a user and pre-loaded messages.
     * Uses gotoMessageId=1 to prevent markConversationRead from being called
     * during fetchMessages() first-fetch logic, avoiding CometChatEvents emissions
     * that leak coroutines via Dispatchers.Default between tests.
     */
    suspend fun createViewModelWithMessages(messages: List<BaseMessage>): CometChatMessageListViewModel {
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        val vm = CometChatMessageListViewModel(
            repository = repository,
            enableListeners = false
        )
        val user = MockFactory.createUser(uid = "test-user", name = "Test User")
        vm.setUser(user, gotoMessageId = 1L)
        vm.fetchMessages()
        return vm
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        mockContext = mock()
        // Mock CometChat.getLoggedInUser() to return a user with uid "test-user"
        cometChatMock = Mockito.mockStatic(CometChat::class.java)
        val loggedInUser = mock<User>()
        whenever(loggedInUser.uid).thenReturn("test-user")
        cometChatMock.`when`<User?> { CometChat.getLoggedInUser() }.thenReturn(loggedInUser)
        // Default stubs
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        whenever(repository.markAsRead(any())).thenReturn(Result.success(Unit))
        whenever(repository.markAsDelivered(any())).thenReturn(Result.success(Unit))
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        // Small delay to allow CometChatEvents.scope (Dispatchers.Default) coroutines
        // to complete before resetting Main dispatcher, preventing UncaughtExceptionsBeforeTest
        Thread.sleep(50)
        Dispatchers.resetMain()
        cometChatMock.close()
        println()
    }


    // ==================== 14.2 PBT: setOptions non-null replaces all defaults ====================

    test("PBT: setOptions with non-null callback replaces all default options") {
        checkAll(20, Arb.int(1..5)) { optionCount ->
            runTest {
                val message = MockFactory.createTextMessage(id = 1L, text = "Test", senderUid = "other-user", receiverId = "test-user")
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                val vm = createViewModelWithMessages(listOf(message))
                advanceUntilIdle()

                // Create custom options
                val customOptions = (1..optionCount).map { i ->
                    MockFactory.createMessageOption(
                        id = "custom-$i",
                        title = "Custom Option $i"
                    )
                }

                // Set options callback that returns non-null
                vm.setOptions { _ -> customOptions }

                // Resolve options with some defaults
                val defaultOptions = listOf(
                    MockFactory.createMessageOption(id = "copy", title = "Copy"),
                    MockFactory.createMessageOption(id = "delete", title = "Delete")
                )

                val resolved = vm.resolveMessageOptions(message, defaultOptions)

                // Should be ONLY the custom options (defaults replaced)
                resolved shouldHaveSize optionCount
                resolved.map { it.id } shouldBe customOptions.map { it.id }
                println("    → optionCount=$optionCount: setOptions replaces defaults")
            }
        }
        println("    ✅ PBT: setOptions non-null replaces all defaults")
    }


    // ==================== 14.3 PBT: setOptions null falls through, addOptions appends ====================

    test("PBT: setOptions returning null falls through to defaults") {
        checkAll(20, Arb.int(1..5)) { defaultCount ->
            runTest {
                val message = MockFactory.createTextMessage(id = 1L, text = "Test", senderUid = "other-user", receiverId = "test-user")
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                val vm = createViewModelWithMessages(listOf(message))
                advanceUntilIdle()

                // Set options callback that returns null (fall through)
                vm.setOptions { _ -> null }

                val defaultOptions = (1..defaultCount).map { i ->
                    MockFactory.createMessageOption(id = "default-$i", title = "Default $i")
                }

                val resolved = vm.resolveMessageOptions(message, defaultOptions)

                // Should be the default options (setOptions returned null)
                resolved shouldHaveSize defaultCount
                resolved.map { it.id } shouldBe defaultOptions.map { it.id }
                println("    → defaultCount=$defaultCount: setOptions null falls through to defaults")
            }
        }
        println("    ✅ PBT: setOptions null falls through to defaults")
    }

    test("PBT: addOptions appends to defaults when setOptions is null") {
        checkAll(20, Arb.int(1..3), Arb.int(1..3)) { defaultCount, addCount ->
            runTest {
                val message = MockFactory.createTextMessage(id = 1L, text = "Test", senderUid = "other-user", receiverId = "test-user")
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                val vm = createViewModelWithMessages(listOf(message))
                advanceUntilIdle()

                // Set options to null (fall through)
                vm.setOptions { _ -> null }

                // Add additional options
                val additionalOptions = (1..addCount).map { i ->
                    MockFactory.createMessageOption(id = "added-$i", title = "Added $i")
                }
                vm.addOptions { _ -> additionalOptions }

                val defaultOptions = (1..defaultCount).map { i ->
                    MockFactory.createMessageOption(id = "default-$i", title = "Default $i")
                }

                val resolved = vm.resolveMessageOptions(message, defaultOptions)

                // Should be defaults + added options
                resolved shouldHaveSize (defaultCount + addCount)
                // First part should be defaults
                resolved.take(defaultCount).map { it.id } shouldBe defaultOptions.map { it.id }
                // Second part should be added options
                resolved.drop(defaultCount).map { it.id } shouldBe additionalOptions.map { it.id }
                println("    → defaults=$defaultCount, added=$addCount: addOptions appends correctly")
            }
        }
        println("    ✅ PBT: addOptions appends to defaults")
    }

    test("PBT: setOptions non-null takes precedence over addOptions") {
        checkAll(20, Arb.int(1..3)) { customCount ->
            runTest {
                val message = MockFactory.createTextMessage(id = 1L, text = "Test", senderUid = "other-user", receiverId = "test-user")
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                val vm = createViewModelWithMessages(listOf(message))
                advanceUntilIdle()

                val customOptions = (1..customCount).map { i ->
                    MockFactory.createMessageOption(id = "custom-$i", title = "Custom $i")
                }

                // Set both callbacks
                vm.setOptions { _ -> customOptions }
                vm.addOptions { _ ->
                    listOf(MockFactory.createMessageOption(id = "added-1", title = "Added"))
                }

                val defaultOptions = listOf(
                    MockFactory.createMessageOption(id = "default-1", title = "Default")
                )

                val resolved = vm.resolveMessageOptions(message, defaultOptions)

                // setOptions takes full precedence — addOptions is ignored
                resolved shouldHaveSize customCount
                resolved.map { it.id } shouldBe customOptions.map { it.id }
                println("    → customCount=$customCount: setOptions takes precedence over addOptions")
            }
        }
        println("    ✅ PBT: setOptions non-null takes precedence over addOptions")
    }


    // ==================== 14.4 PBT: both callbacks null → defaults unchanged ====================

    test("PBT: both callbacks null returns defaults unchanged") {
        checkAll(20, Arb.int(1..6)) { defaultCount ->
            runTest {
                val message = MockFactory.createTextMessage(id = 1L, text = "Test", senderUid = "other-user", receiverId = "test-user")
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                val vm = createViewModelWithMessages(listOf(message))
                advanceUntilIdle()

                // Don't set any callbacks (both remain null)
                val defaultOptions = (1..defaultCount).map { i ->
                    MockFactory.createMessageOption(id = "default-$i", title = "Default $i")
                }

                val resolved = vm.resolveMessageOptions(message, defaultOptions)

                // Should be exactly the defaults
                resolved shouldHaveSize defaultCount
                resolved shouldBe defaultOptions
                println("    → defaultCount=$defaultCount: defaults unchanged when no callbacks set")
            }
        }
        println("    ✅ PBT: both callbacks null → defaults unchanged")
    }


    // ==================== 14.5 handleMessageOptionClick for each option ID ====================

    test("handleMessageOptionClick returns true for COPY option") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Copy me", senderUid = "other-user", receiverId = "test-user")

            // Mock clipboard service
            val clipboardManager = mock<android.content.ClipboardManager>()
            whenever(mockContext.getSystemService(Context.CLIPBOARD_SERVICE)).thenReturn(clipboardManager)

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.COPY, message)
            handled shouldBe true
            println("    ✅ COPY option handled (returns true)")
        }
    }

    test("handleMessageOptionClick returns true for EDIT option") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Edit me", senderUid = "test-user", receiverId = "other-user")

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.EDIT, message)
            handled shouldBe true
            println("    ✅ EDIT option handled (returns true)")
        }
    }

    test("handleMessageOptionClick returns true for REPLY option") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Reply to me", senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.REPLY, message)
            handled shouldBe true
            println("    ✅ REPLY option handled (returns true)")
        }
    }

    test("handleMessageOptionClick returns true for REPLY_TO_MESSAGE option") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Reply to me", senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.REPLY_TO_MESSAGE, message)
            handled shouldBe true
            println("    ✅ REPLY_TO_MESSAGE option handled (returns true)")
        }
    }

    test("handleMessageOptionClick returns true for DELETE option and emits deleteConfirmationRequest") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Delete me", senderUid = "test-user", receiverId = "other-user")

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            // Collect deleteConfirmationRequest
            var emittedMessage: BaseMessage? = null
            val job = launch(testDispatcher) {
                vm.deleteConfirmationRequest.collect { emittedMessage = it }
            }

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.DELETE, message)
            advanceUntilIdle()

            handled shouldBe true
            emittedMessage shouldBe message
            job.cancel()
            println("    ✅ DELETE option handled and emits deleteConfirmationRequest")
        }
    }

    test("handleMessageOptionClick returns true for MARK_AS_UNREAD option") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Mark unread", senderUid = "other-user", receiverId = "test-user")
            whenever(repository.markAsUnread(any())).thenReturn(Result.success(mock()))

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.MARK_AS_UNREAD, message)
            handled shouldBe true
            println("    ✅ MARK_AS_UNREAD option handled (returns true)")
        }
    }

    test("handleMessageOptionClick returns true for TRANSLATE option") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Translate me", senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.TRANSLATE, message)
            handled shouldBe true
            println("    ✅ TRANSLATE option handled (returns true)")
        }
    }


    // ==================== 14.6 handleMessageOptionClick returns false for unknown ID ====================

    test("handleMessageOptionClick returns false for unknown option ID") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Test", senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, "unknown_option_id", message)
            handled shouldBe false
            println("    ✅ Unknown option ID returns false")
        }
    }

    test("PBT: handleMessageOptionClick returns false for random unknown IDs") {
        checkAll(20, Arb.string(5..20)) { randomId ->
            runTest {
                // Skip known IDs
                val knownIds = setOf(
                    UIKitConstants.MessageOption.COPY,
                    UIKitConstants.MessageOption.EDIT,
                    UIKitConstants.MessageOption.REPLY,
                    UIKitConstants.MessageOption.REPLY_TO_MESSAGE,
                    UIKitConstants.MessageOption.DELETE,
                    UIKitConstants.MessageOption.MARK_AS_UNREAD,
                    UIKitConstants.MessageOption.TRANSLATE
                )
                if (randomId !in knownIds) {
                    val message = MockFactory.createTextMessage(id = 1L, text = "Test", senderUid = "other-user", receiverId = "test-user")
                    whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(listOf(message as BaseMessage)))

                    val vm = createViewModelWithMessages(listOf(message))
                    advanceUntilIdle()

                    val handled = vm.handleMessageOptionClick(mockContext, randomId, message)
                    handled shouldBe false
                    println("    → randomId='${randomId.take(10)}': returns false")
                }
            }
        }
        println("    ✅ PBT: unknown option IDs always return false")
    }

    test("handleMessageOptionClick returns false for REPLY_IN_THREAD (handled by presentation layer)") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Thread", senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.REPLY_IN_THREAD, message)
            handled shouldBe false
            println("    ✅ REPLY_IN_THREAD returns false (presentation layer handles)")
        }
    }

    test("handleMessageOptionClick returns false for MESSAGE_INFORMATION (handled by presentation layer)") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Info", senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.MESSAGE_INFORMATION, message)
            handled shouldBe false
            println("    ✅ MESSAGE_INFORMATION returns false (presentation layer handles)")
        }
    }

    test("handleMessageOptionClick returns false for SHARE (handled by presentation layer)") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L, text = "Share", senderUid = "other-user", receiverId = "test-user")

            val vm = createViewModelWithMessages(listOf(message as BaseMessage))
            advanceUntilIdle()

            val handled = vm.handleMessageOptionClick(mockContext, UIKitConstants.MessageOption.SHARE, message)
            handled shouldBe false
            println("    ✅ SHARE returns false (presentation layer handles)")
        }
    }
})
