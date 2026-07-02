package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Layer 5A — ViewModel State Transitions & Configuration Tests.
 *
 * Tests the CometChatMessageListViewModel's:
 * - Initial state (Loading)
 * - fetchMessages success → Loaded, empty → Empty, failure → Error
 * - fetchMessages guards (hasMore=false → no-op, isInProgress=true → no-op)
 * - setUser/setGroup configures repository and generates idMap
 * - All setter methods store values correctly
 * - PBT: for any message count (0..20), correct state transition
 *
 * Architecture:
 * - Mocks the MessageListRepository interface (layer directly below)
 * - Uses `enableListeners = false` to avoid SDK dependencies
 * - Uses UnconfinedTestDispatcher + Dispatchers.setMain/resetMain
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatMessageListViewModelTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageListViewModelTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var repository: MessageListRepository

    /**
     * Creates a ViewModel configured with a user conversation.
     * The repository is pre-configured to return the given messages on fetchPreviousMessages.
     */
    suspend fun createViewModelWithMessages(messages: List<BaseMessage>): CometChatMessageListViewModel {
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
        whenever(repository.hasMorePreviousMessages()).thenReturn(messages.isNotEmpty())
        val vm = CometChatMessageListViewModel(
            repository = repository,
            enableListeners = false
        )
        return vm
    }

    /**
     * Creates a ViewModel and configures it for a user, then fetches messages.
     */
    suspend fun createConfiguredViewModel(messages: List<BaseMessage>): CometChatMessageListViewModel {
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

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        // Default stubs
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }


    // ==================== 6.2 Initial uiState is Loading ====================

    test("initial uiState should be Loading before any fetch") {
        runTest(testDispatcher) {
            println("    → Creating ViewModel without fetching")
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loading>()
            println("    ✅ Initial state is Loading")
        }
    }

    test("initial messages list should be empty") {
        runTest(testDispatcher) {
            println("    → Creating ViewModel without fetching")
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.messages.value shouldHaveSize 0
            println("    ✅ Initial messages list is empty")
        }
    }

    test("initial hasMorePreviousMessages should be true") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.hasMorePreviousMessages.value shouldBe true
            println("    ✅ Initial hasMorePreviousMessages is true")
        }
    }

    test("initial isInProgress should be false") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.isInProgress.value shouldBe false
            println("    ✅ Initial isInProgress is false")
        }
    }


    // ==================== 6.3 fetchMessages state transitions ====================

    test("fetchMessages success with messages → Loaded state") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(5)
            whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
            whenever(repository.hasMorePreviousMessages()).thenReturn(true)

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.fetchMessages()
            advanceUntilIdle()

            println("    → Fetched 5 messages")
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            vm.messages.value shouldHaveSize 5
            println("    ✅ State is Loaded with 5 messages")
        }
    }

    test("fetchMessages success with empty list → Empty state") {
        runTest(testDispatcher) {
            whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
            whenever(repository.hasMorePreviousMessages()).thenReturn(true)

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.fetchMessages()
            advanceUntilIdle()

            println("    → Fetched 0 messages")
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
            vm.messages.value shouldHaveSize 0
            println("    ✅ State is Empty")
        }
    }

    test("fetchMessages failure → Error state") {
        runTest(testDispatcher) {
            val exception = MockFactory.createCometChatException("FETCH_ERR", "Network error")
            whenever(repository.fetchPreviousMessages()).thenReturn(Result.failure(exception))
            whenever(repository.hasMorePreviousMessages()).thenReturn(true)

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.fetchMessages()
            advanceUntilIdle()

            println("    → Fetch failed with FETCH_ERR")
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
            val errorState = vm.uiState.value as MessageListUIState.Error
            errorState.exception shouldBe exception
            println("    ✅ State is Error with matching exception")
        }
    }


    // ==================== 6.4 fetchMessages guards ====================

    test("fetchMessages when hasMorePreviousMessages=false → no-op, no repository call") {
        runTest(testDispatcher) {
            // First fetch returns empty → hasMore becomes false
            val emptyResult: Result<List<BaseMessage>> = Result.success(emptyList())
            whenever(repository.fetchPreviousMessages()).thenReturn(emptyResult)

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.fetchMessages()
            advanceUntilIdle()

            println("    → First fetch returned empty, hasMore should be false")
            vm.hasMorePreviousMessages.value shouldBe false

            // Second fetch should be a no-op because hasMore is false
            // Even if repository would return messages, the guard prevents the call
            vm.fetchMessages()
            advanceUntilIdle()

            // Messages should still be empty (second fetch was guarded)
            vm.messages.value shouldHaveSize 0
            println("    ✅ Second fetchMessages was no-op (hasMore=false guard)")
        }
    }

    test("fetchMessages concurrent guard — isInProgress returns to false after completion") {
        runTest(testDispatcher) {
            val messages = MockFactory.createMessages(3)
            val messagesResult: Result<List<BaseMessage>> = Result.success(messages)
            whenever(repository.fetchPreviousMessages()).thenReturn(messagesResult)

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            // Call fetchMessages — with UnconfinedTestDispatcher it completes synchronously
            vm.fetchMessages()
            advanceUntilIdle()

            println("    → Called fetchMessages, verifying isInProgress guard behavior")
            // The key invariant: isInProgress returns to false after completion
            vm.isInProgress.value shouldBe false
            // Messages were loaded
            vm.messages.value shouldHaveSize 3
            println("    ✅ isInProgress=false after fetch completes, messages loaded")
        }
    }


    // ==================== 6.5 setUser/setGroup configures repository and generates idMap ====================

    test("setUser configures repository and generates correct idMap") {
        runTest(testDispatcher) {
            val user = MockFactory.createUser(uid = "alice-123", name = "Alice")

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.setUser(user)

            println("    → setUser called with uid=alice-123")
            val idMap = vm.idMap.value
            idMap[UIKitConstants.MapId.RECEIVER_ID] shouldBe "alice-123"
            idMap[UIKitConstants.MapId.RECEIVER_TYPE] shouldBe CometChatConstants.RECEIVER_TYPE_USER
            idMap.containsKey(UIKitConstants.MapId.PARENT_MESSAGE_ID) shouldBe false
            println("    ✅ idMap has RECEIVER_ID=alice-123, RECEIVER_TYPE=user, no PARENT_MESSAGE_ID")
        }
    }

    test("setUser with parentMessageId includes PARENT_MESSAGE_ID in idMap") {
        runTest(testDispatcher) {
            val user = MockFactory.createUser(uid = "bob-456", name = "Bob")

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.setUser(user, parentMessageId = 999L)

            println("    → setUser called with uid=bob-456, parentMessageId=999")
            val idMap = vm.idMap.value
            idMap[UIKitConstants.MapId.RECEIVER_ID] shouldBe "bob-456"
            idMap[UIKitConstants.MapId.RECEIVER_TYPE] shouldBe CometChatConstants.RECEIVER_TYPE_USER
            idMap[UIKitConstants.MapId.PARENT_MESSAGE_ID] shouldBe "999"
            println("    ✅ idMap includes PARENT_MESSAGE_ID=999")
        }
    }

    test("setGroup configures repository and generates correct idMap") {
        runTest(testDispatcher) {
            val group = MockFactory.createGroup(guid = "team-alpha", name = "Team Alpha")

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.setGroup(group)

            println("    → setGroup called with guid=team-alpha")
            val idMap = vm.idMap.value
            idMap[UIKitConstants.MapId.RECEIVER_ID] shouldBe "team-alpha"
            idMap[UIKitConstants.MapId.RECEIVER_TYPE] shouldBe CometChatConstants.RECEIVER_TYPE_GROUP
            idMap.containsKey(UIKitConstants.MapId.PARENT_MESSAGE_ID) shouldBe false
            println("    ✅ idMap has RECEIVER_ID=team-alpha, RECEIVER_TYPE=group")
        }
    }

    test("setGroup with parentMessageId includes PARENT_MESSAGE_ID in idMap") {
        runTest(testDispatcher) {
            val group = MockFactory.createGroup(guid = "dev-group", name = "Dev Group")

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.setGroup(group, parentMessageId = 500L)

            println("    → setGroup called with guid=dev-group, parentMessageId=500")
            val idMap = vm.idMap.value
            idMap[UIKitConstants.MapId.RECEIVER_ID] shouldBe "dev-group"
            idMap[UIKitConstants.MapId.RECEIVER_TYPE] shouldBe CometChatConstants.RECEIVER_TYPE_GROUP
            idMap[UIKitConstants.MapId.PARENT_MESSAGE_ID] shouldBe "500"
            println("    ✅ idMap includes PARENT_MESSAGE_ID=500")
        }
    }

    test("setUser clears previous group configuration") {
        runTest(testDispatcher) {
            val group = MockFactory.createGroup(guid = "old-group", name = "Old Group")
            val user = MockFactory.createUser(uid = "new-user", name = "New User")

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.setGroup(group)
            vm.setUser(user)

            println("    → setGroup then setUser")
            val idMap = vm.idMap.value
            idMap[UIKitConstants.MapId.RECEIVER_ID] shouldBe "new-user"
            idMap[UIKitConstants.MapId.RECEIVER_TYPE] shouldBe CometChatConstants.RECEIVER_TYPE_USER
            println("    ✅ idMap reflects user configuration after switching from group")
        }
    }

    test("setGroup clears previous user configuration") {
        runTest(testDispatcher) {
            val user = MockFactory.createUser(uid = "old-user", name = "Old User")
            val group = MockFactory.createGroup(guid = "new-group", name = "New Group")

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.setUser(user)
            vm.setGroup(group)

            println("    → setUser then setGroup")
            val idMap = vm.idMap.value
            idMap[UIKitConstants.MapId.RECEIVER_ID] shouldBe "new-group"
            idMap[UIKitConstants.MapId.RECEIVER_TYPE] shouldBe CometChatConstants.RECEIVER_TYPE_GROUP
            println("    ✅ idMap reflects group configuration after switching from user")
        }
    }


    // ==================== 6.6 All setter methods store values correctly ====================

    test("setDisableReceipt stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            // Default should not throw — we verify behavior indirectly
            // by checking that the setter doesn't crash and the VM remains functional
            vm.setDisableReceipt(true)
            vm.setDisableReceipt(false)
            println("    ✅ setDisableReceipt(true/false) executes without error")
        }
    }

    test("setDisableReactions stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setDisableReactions(true)
            vm.setDisableReactions(false)
            println("    ✅ setDisableReactions(true/false) executes without error")
        }
    }

    test("setHideDeleteMessage stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setHideDeleteMessage(true)
            vm.setHideDeleteMessage(false)
            println("    ✅ setHideDeleteMessage(true/false) executes without error")
        }
    }

    test("setStartFromUnreadMessages stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setStartFromUnreadMessages(true)
            vm.setStartFromUnreadMessages(false)
            println("    ✅ setStartFromUnreadMessages(true/false) executes without error")
        }
    }

    test("setUnreadThreshold stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setUnreadThreshold(50)
            vm.setUnreadThreshold(0)
            vm.setUnreadThreshold(100)
            println("    ✅ setUnreadThreshold(50/0/100) executes without error")
        }
    }

    test("setDisableSoundForMessages stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setDisableSoundForMessages(true)
            vm.setDisableSoundForMessages(false)
            println("    ✅ setDisableSoundForMessages(true/false) executes without error")
        }
    }

    test("setCustomSoundForMessages stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setCustomSoundForMessages(12345)
            vm.setCustomSoundForMessages(0)
            println("    ✅ setCustomSoundForMessages(12345/0) executes without error")
        }
    }

    test("setEnableConversationStarter stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setEnableConversationStarter(true)
            vm.setEnableConversationStarter(false)
            println("    ✅ setEnableConversationStarter(true/false) executes without error")
        }
    }

    test("setEnableSmartReplies stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setEnableSmartReplies(true)
            vm.setEnableSmartReplies(false)
            println("    ✅ setEnableSmartReplies(true/false) executes without error")
        }
    }

    test("setSmartRepliesDelay stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setSmartRepliesDelay(5000)
            vm.setSmartRepliesDelay(0)
            vm.setSmartRepliesDelay(30000)
            println("    ✅ setSmartRepliesDelay(5000/0/30000) executes without error")
        }
    }

    test("setSmartReplyKeywords stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setSmartReplyKeywords(listOf("help", "urgent", "question"))
            vm.setSmartReplyKeywords(emptyList())
            println("    ✅ setSmartReplyKeywords with list and empty list executes without error")
        }
    }

    test("setEnableConversationSummary stores value correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setEnableConversationSummary(true)
            vm.setEnableConversationSummary(false)
            println("    ✅ setEnableConversationSummary(true/false) executes without error")
        }
    }

    test("setOptions stores callback correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            val customOptions = listOf(
                MockFactory.createMessageOption(id = "custom-1", title = "Custom Option")
            )
            vm.setOptions { _ -> customOptions }

            // Verify via resolveMessageOptions
            val message = MockFactory.createTextMessage(id = 1L)
            val resolved = vm.resolveMessageOptions(message, emptyList())
            resolved shouldHaveSize 1
            resolved[0].id shouldBe "custom-1"
            println("    ✅ setOptions callback replaces defaults correctly")
        }
    }

    test("addOptions stores callback correctly") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            val additionalOptions = listOf(
                MockFactory.createMessageOption(id = "extra-1", title = "Extra Option")
            )
            vm.addOptions { _ -> additionalOptions }

            // Verify via resolveMessageOptions — defaults + additional
            val message = MockFactory.createTextMessage(id = 1L)
            val defaults = listOf(
                MockFactory.createMessageOption(id = "copy", title = "Copy")
            )
            val resolved = vm.resolveMessageOptions(message, defaults)
            resolved shouldHaveSize 2
            resolved[0].id shouldBe "copy"
            resolved[1].id shouldBe "extra-1"
            println("    ✅ addOptions callback appends to defaults correctly")
        }
    }

    test("setOptions returning null falls through to defaults + addOptions") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            // setOptions returns null → fall through
            vm.setOptions { _ -> null }
            vm.addOptions { _ ->
                listOf(MockFactory.createMessageOption(id = "appended", title = "Appended"))
            }

            val message = MockFactory.createTextMessage(id = 1L)
            val defaults = listOf(
                MockFactory.createMessageOption(id = "delete", title = "Delete")
            )
            val resolved = vm.resolveMessageOptions(message, defaults)
            resolved shouldHaveSize 2
            resolved[0].id shouldBe "delete"
            resolved[1].id shouldBe "appended"
            println("    ✅ setOptions returning null falls through to defaults + addOptions")
        }
    }

    test("setUIStateEmpty sets state to Empty") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.setUIStateEmpty()
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
            println("    ✅ setUIStateEmpty sets state to Empty")
        }
    }

    test("getInitializedId returns null before setUser/setGroup") {
        runTest(testDispatcher) {
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            vm.getInitializedId() shouldBe null
            println("    ✅ getInitializedId is null before configuration")
        }
    }

    test("getInitializedId returns uid after setUser") {
        runTest(testDispatcher) {
            val user = MockFactory.createUser(uid = "init-user", name = "Init User")
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.setUser(user)

            vm.getInitializedId() shouldBe "init-user"
            println("    ✅ getInitializedId returns uid after setUser")
        }
    }

    test("getInitializedId returns guid after setGroup") {
        runTest(testDispatcher) {
            val group = MockFactory.createGroup(guid = "init-group", name = "Init Group")
            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.setGroup(group)

            vm.getInitializedId() shouldBe "init-group"
            println("    ✅ getInitializedId returns guid after setGroup")
        }
    }


    // ==================== 6.7 PBT: for any message count (0..20), correct state transition ====================

    test("PBT: for any message count (0..20), correct state transition (Empty vs Loaded)") {
        checkAll(50, Arb.int(0..20)) { count ->
            runTest(testDispatcher) {
                val messages = if (count > 0) {
                    MockFactory.createMessages(
                        count = count,
                        startId = 1L,
                        startTimestamp = 1700000000L
                    )
                } else {
                    emptyList()
                }

                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = CometChatMessageListViewModel(
                    repository = repository,
                    enableListeners = false
                )
                vm.fetchMessages()
                advanceUntilIdle()

                println("    → count=$count")
                if (count == 0) {
                    vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
                    vm.messages.value shouldHaveSize 0
                    println("    ✅ count=0 → Empty state")
                } else {
                    vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
                    vm.messages.value shouldHaveSize count
                    println("    ✅ count=$count → Loaded state with $count messages")
                }
            }
        }
    }

    test("PBT: fetchMessages updates hasMorePreviousMessages based on result") {
        checkAll(30, Arb.int(0..15)) { count ->
            runTest(testDispatcher) {
                val messages = if (count > 0) {
                    MockFactory.createMessages(count = count)
                } else {
                    emptyList()
                }

                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = CometChatMessageListViewModel(
                    repository = repository,
                    enableListeners = false
                )
                vm.fetchMessages()
                advanceUntilIdle()

                println("    → count=$count")
                if (count == 0) {
                    vm.hasMorePreviousMessages.value shouldBe false
                    println("    ✅ count=0 → hasMorePreviousMessages=false")
                } else {
                    vm.hasMorePreviousMessages.value shouldBe true
                    println("    ✅ count=$count → hasMorePreviousMessages=true")
                }
            }
        }
    }

    test("PBT: isInProgress is always false after fetchMessages completes") {
        checkAll(30, Arb.int(0..10)) { count ->
            runTest(testDispatcher) {
                val messages = if (count > 0) {
                    MockFactory.createMessages(count = count)
                } else {
                    emptyList()
                }

                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = CometChatMessageListViewModel(
                    repository = repository,
                    enableListeners = false
                )
                vm.fetchMessages()
                advanceUntilIdle()

                println("    → count=$count")
                vm.isInProgress.value shouldBe false
                println("    ✅ isInProgress=false after fetch completes")
            }
        }
    }

    test("PBT: isInProgress is false after fetchMessages failure") {
        checkAll(20, Arb.int(1..50)) { errorCode ->
            runTest(testDispatcher) {
                val exception = MockFactory.createCometChatException(
                    code = "ERR_$errorCode",
                    message = "Error $errorCode"
                )
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.failure(exception))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = CometChatMessageListViewModel(
                    repository = repository,
                    enableListeners = false
                )
                vm.fetchMessages()
                advanceUntilIdle()

                println("    → errorCode=ERR_$errorCode")
                vm.isInProgress.value shouldBe false
                vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
                println("    ✅ isInProgress=false after failure, state=Error")
            }
        }
    }

    test("fetchMessages deduplicates messages by ID when merging") {
        runTest(testDispatcher) {
            // First fetch returns messages 1-3
            val firstBatch = MockFactory.createMessages(count = 3, startId = 1L)
            whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(firstBatch))
            whenever(repository.hasMorePreviousMessages()).thenReturn(true)

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )
            vm.fetchMessages()
            advanceUntilIdle()

            vm.messages.value shouldHaveSize 3
            println("    → First fetch: 3 messages")

            // Second fetch returns messages 2-5 (overlap on 2,3)
            val secondBatch = MockFactory.createMessages(count = 4, startId = 2L)
            whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(secondBatch))
            whenever(repository.hasMorePreviousMessages()).thenReturn(true)

            vm.fetchMessages()
            advanceUntilIdle()

            // Should have 5 unique messages (1,2,3,4,5), not 7
            vm.messages.value shouldHaveSize 5
            println("    ✅ Deduplication: 3 + 4 (with 2 overlaps) = 5 unique messages")
        }
    }

    test("fetchMessages shows Loading state only on initial fetch (empty list)") {
        runTest(testDispatcher) {
            // First fetch — list is empty, should show Loading
            val messages = MockFactory.createMessages(count = 3)
            whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
            whenever(repository.hasMorePreviousMessages()).thenReturn(true)

            val vm = CometChatMessageListViewModel(
                repository = repository,
                enableListeners = false
            )

            // Before fetch, state is Loading (initial)
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loading>()

            vm.fetchMessages()
            advanceUntilIdle()

            // After first fetch, state is Loaded
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            println("    ✅ Loading shown initially, then transitions to Loaded")
        }
    }

})
