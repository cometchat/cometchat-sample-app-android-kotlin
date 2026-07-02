package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult
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
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Layer 5D — GoToMessage Property-Based Tests.
 *
 * Tests the CometChatMessageListViewModel's goToMessage functionality using PBT:
 * - Target message always in result list, scrollToMessageId set correctly
 * - highlightScroll reflects highlight parameter
 * - Pagination flags reflect surrounding result
 * - goToMessage failure handling (getMessage/fetchSurrounding errors)
 * - clearScrollToMessage and clearHighlightScroll
 *
 * Architecture:
 * - Mocks the MessageListRepository interface (layer directly below)
 * - Uses `enableListeners = false` to avoid SDK dependencies
 * - Uses UnconfinedTestDispatcher + Dispatchers.setMain/resetMain
 * - All PBT tests use `checkAll` with `Arb` generators (no hardcoded values in PBT)
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListGoToMessagePropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListGoToMessagePropertyTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var repository: MessageListRepository

    /**
     * Creates a fresh ViewModel with default repository stubs.
     */
    fun createViewModel(): CometChatMessageListViewModel {
        return CometChatMessageListViewModel(
            repository = repository,
            enableListeners = false
        )
    }

    /**
     * Creates a SurroundingMessagesResult with the given parameters.
     * Messages are ordered chronologically: older (ascending) + target + newer (ascending).
     */
    fun createSurroundingResult(
        targetId: Long,
        olderCount: Int,
        newerCount: Int,
        hasMorePrevious: Boolean = true,
        hasMoreNext: Boolean = true
    ): SurroundingMessagesResult {
        val baseTimestamp = 1700000000L
        val olderMessages = (0 until olderCount).map { i ->
            MockFactory.createTextMessage(
                id = targetId - olderCount + i,
                text = "Older ${i + 1}",
                senderUid = "user-1",
                receiverId = "user-2",
                sentAt = baseTimestamp + (i * 60L)
            ) as BaseMessage
        }
        val targetMessage = MockFactory.createTextMessage(
            id = targetId,
            text = "Target Message",
            senderUid = "user-1",
            receiverId = "user-2",
            sentAt = baseTimestamp + (olderCount * 60L)
        ) as BaseMessage
        val newerMessages = (0 until newerCount).map { i ->
            MockFactory.createTextMessage(
                id = targetId + 1 + i,
                text = "Newer ${i + 1}",
                senderUid = "user-1",
                receiverId = "user-2",
                sentAt = baseTimestamp + ((olderCount + 1 + i) * 60L)
            ) as BaseMessage
        }
        return SurroundingMessagesResult(
            olderMessages = olderMessages,
            targetMessage = targetMessage,
            newerMessages = newerMessages,
            hasMorePrevious = hasMorePrevious,
            hasMoreNext = hasMoreNext
        )
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        // Small delay to allow CometChatEvents.scope (Dispatchers.Default) coroutines
        // to complete before resetting Main dispatcher, preventing UncaughtExceptionsBeforeTest
        Thread.sleep(50)
        Dispatchers.resetMain()
        println()
    }


    // ==================== 9.2 PBT: target message always in result list, scrollToMessageId set correctly ====================

    test("PBT: target message is always present in the message list after goToMessage") {
        checkAll(50, Arb.long(50L..500L), Arb.int(0..15), Arb.int(0..15)) { targetId, olderCount, newerCount ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = olderCount,
                    newerCount = newerCount
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId)
                advanceUntilIdle()

                val messageIds = vm.messages.value.map { it.id }
                messageIds shouldContain targetId
                println("    → targetId=$targetId, older=$olderCount, newer=$newerCount: target present in list")
            }
        }
        println("    ✅ PBT: target message always present in result list")
    }

    test("PBT: scrollToMessageId is set to the target messageId after successful goToMessage") {
        checkAll(50, Arb.long(1L..10000L)) { messageId ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = messageId,
                    olderCount = 5,
                    newerCount = 5
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(messageId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(messageId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()

                // Initially null
                vm.scrollToMessageId.value shouldBe null

                vm.goToMessage(messageId)
                advanceUntilIdle()

                vm.scrollToMessageId.value shouldBe messageId
                println("    → messageId=$messageId: scrollToMessageId set correctly")
            }
        }
        println("    ✅ PBT: scrollToMessageId set to target messageId on success")
    }

    test("PBT: message list size equals olderCount + 1 (target) + newerCount") {
        checkAll(50, Arb.long(50L..500L), Arb.int(0..15), Arb.int(0..15)) { targetId, olderCount, newerCount ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = olderCount,
                    newerCount = newerCount
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId)
                advanceUntilIdle()

                val expectedSize = olderCount + 1 + newerCount
                vm.messages.value shouldHaveSize expectedSize
                println("    → targetId=$targetId: list size=$expectedSize (older=$olderCount + 1 + newer=$newerCount)")
            }
        }
        println("    ✅ PBT: message list size equals combined snapshot size")
    }

    test("PBT: messages are in chronological order (older + target + newer) after goToMessage") {
        checkAll(30, Arb.long(50L..500L), Arb.int(1..10), Arb.int(1..10)) { targetId, olderCount, newerCount ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = olderCount,
                    newerCount = newerCount
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId)
                advanceUntilIdle()

                val messages = vm.messages.value
                // Verify chronological order: IDs should be ascending
                for (i in 1 until messages.size) {
                    val prevId = messages[i - 1].id
                    val currId = messages[i].id
                    (currId > prevId) shouldBe true
                }
                println("    → targetId=$targetId: chronological order preserved")
            }
        }
        println("    ✅ PBT: messages in chronological order after goToMessage")
    }

    test("PBT: goToMessage replaces existing message list completely") {
        checkAll(30, Arb.long(100L..500L), Arb.int(1..8), Arb.int(1..8)) { targetId, olderCount, newerCount ->
            runTest {
                // Set up initial messages
                val initialMessages = MockFactory.createMessages(
                    count = 5,
                    startId = 1000L,
                    startTimestamp = 1700000000L
                )
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(initialMessages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                vm.messages.value shouldHaveSize 5

                // Now goToMessage
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = olderCount,
                    newerCount = newerCount
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                vm.goToMessage(targetId)
                advanceUntilIdle()

                // Old messages (IDs 1000-1004) should not be present
                val messageIds = vm.messages.value.map { it.id }
                messageIds.none { it >= 1000L } shouldBe true
                // New list should have correct size
                vm.messages.value shouldHaveSize olderCount + 1 + newerCount
                println("    → targetId=$targetId: list completely replaced")
            }
        }
        println("    ✅ PBT: goToMessage replaces existing message list")
    }

    test("PBT: scrollToMessageId is not emitted on failure") {
        checkAll(50, Arb.long(1L..10000L)) { messageId ->
            runTest {
                whenever(repository.getMessage(messageId)).thenReturn(
                    Result.failure(Exception("Message not found"))
                )

                val vm = createViewModel()
                vm.goToMessage(messageId)
                advanceUntilIdle()

                vm.scrollToMessageId.value shouldBe null
                println("    → messageId=$messageId: scrollToMessageId remains null on failure")
            }
        }
        println("    ✅ PBT: scrollToMessageId not emitted on failure")
    }


    // ==================== 9.3 PBT: highlightScroll reflects highlight parameter ====================

    test("PBT: highlightScroll is true when highlight=true on successful goToMessage") {
        checkAll(50, Arb.long(50L..500L)) { targetId ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = 5,
                    newerCount = 5
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()

                // Initially false
                vm.highlightScroll.value shouldBe false

                vm.goToMessage(targetId, highlight = true)
                advanceUntilIdle()

                vm.highlightScroll.value shouldBe true
                println("    → targetId=$targetId: highlightScroll=true when highlight=true")
            }
        }
        println("    ✅ PBT: highlightScroll is true when highlight=true")
    }

    test("PBT: highlightScroll remains false when highlight=false on successful goToMessage") {
        checkAll(50, Arb.long(50L..500L)) { targetId ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = 5,
                    newerCount = 5
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId, highlight = false)
                advanceUntilIdle()

                vm.highlightScroll.value shouldBe false
                println("    → targetId=$targetId: highlightScroll=false when highlight=false")
            }
        }
        println("    ✅ PBT: highlightScroll remains false when highlight=false")
    }

    test("PBT: highlightScroll reflects the highlight parameter for any boolean value") {
        checkAll(50, Arb.long(50L..500L), Arb.boolean()) { targetId, highlight ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = 3,
                    newerCount = 3
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId, highlight = highlight)
                advanceUntilIdle()

                vm.highlightScroll.value shouldBe highlight
                println("    → targetId=$targetId, highlight=$highlight: highlightScroll=$highlight")
            }
        }
        println("    ✅ PBT: highlightScroll reflects highlight parameter for any boolean")
    }

    test("PBT: highlightScroll is not set on goToMessage failure") {
        checkAll(50, Arb.long(1L..10000L), Arb.boolean()) { messageId, highlight ->
            runTest {
                whenever(repository.getMessage(messageId)).thenReturn(
                    Result.failure(Exception("Not found"))
                )

                val vm = createViewModel()
                vm.goToMessage(messageId, highlight = highlight)
                advanceUntilIdle()

                vm.highlightScroll.value shouldBe false
                println("    → messageId=$messageId, highlight=$highlight: highlightScroll=false on failure")
            }
        }
        println("    ✅ PBT: highlightScroll not set on failure")
    }


    // ==================== 9.4 PBT: pagination flags reflect surrounding result ====================

    test("PBT: hasMorePreviousMessages reflects result.hasMorePrevious") {
        checkAll(50, Arb.long(50L..500L), Arb.boolean()) { targetId, hasMorePrevious ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = 5,
                    newerCount = 5,
                    hasMorePrevious = hasMorePrevious,
                    hasMoreNext = true
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId)
                advanceUntilIdle()

                vm.hasMorePreviousMessages.value shouldBe hasMorePrevious
                println("    → targetId=$targetId: hasMorePrevious=$hasMorePrevious")
            }
        }
        println("    ✅ PBT: hasMorePreviousMessages reflects result.hasMorePrevious")
    }

    test("PBT: hasMoreNewMessages reflects result.hasMoreNext") {
        checkAll(50, Arb.long(50L..500L), Arb.boolean()) { targetId, hasMoreNext ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = 5,
                    newerCount = 5,
                    hasMorePrevious = true,
                    hasMoreNext = hasMoreNext
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId)
                advanceUntilIdle()

                vm.hasMoreNewMessages.value shouldBe hasMoreNext
                println("    → targetId=$targetId: hasMoreNext=$hasMoreNext")
            }
        }
        println("    ✅ PBT: hasMoreNewMessages reflects result.hasMoreNext")
    }

    test("PBT: both pagination flags are updated correctly for any combination") {
        checkAll(30, Arb.long(50L..500L), Arb.boolean(), Arb.boolean()) { targetId, hasMorePrev, hasMoreNext ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = 3,
                    newerCount = 3,
                    hasMorePrevious = hasMorePrev,
                    hasMoreNext = hasMoreNext
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId)
                advanceUntilIdle()

                vm.hasMorePreviousMessages.value shouldBe hasMorePrev
                vm.hasMoreNewMessages.value shouldBe hasMoreNext
                println("    → targetId=$targetId: hasMorePrev=$hasMorePrev, hasMoreNext=$hasMoreNext")
            }
        }
        println("    ✅ PBT: both pagination flags updated correctly")
    }

    test("PBT: rebuildRequestFromMessageId called with oldest message ID on success") {
        checkAll(30, Arb.long(50L..500L), Arb.int(1..10), Arb.int(0..10)) { targetId, olderCount, newerCount ->
            runTest {
                val localRepo: MessageListRepository = mock()
                whenever(localRepo.hasMorePreviousMessages()).thenReturn(true)
                whenever(localRepo.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))

                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = olderCount,
                    newerCount = newerCount
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(localRepo.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(localRepo.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = CometChatMessageListViewModel(
                    repository = localRepo,
                    enableListeners = false
                )
                vm.goToMessage(targetId)
                advanceUntilIdle()

                // The oldest message ID is targetId - olderCount
                val expectedOldestId = targetId - olderCount
                verify(localRepo).rebuildRequestFromMessageId(expectedOldestId)
                println("    → targetId=$targetId, olderCount=$olderCount: rebuilt from ID=$expectedOldestId")
            }
        }
        println("    ✅ PBT: rebuildRequestFromMessageId called with oldest message ID")
    }

    test("PBT: rebuildRequestFromMessageId called with targetId when no older messages") {
        checkAll(30, Arb.long(50L..500L), Arb.int(0..10)) { targetId, newerCount ->
            runTest {
                val localRepo: MessageListRepository = mock()
                whenever(localRepo.hasMorePreviousMessages()).thenReturn(true)
                whenever(localRepo.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))

                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = 0,
                    newerCount = newerCount
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(localRepo.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(localRepo.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = CometChatMessageListViewModel(
                    repository = localRepo,
                    enableListeners = false
                )
                vm.goToMessage(targetId)
                advanceUntilIdle()

                // When no older messages, the first message is the target itself
                verify(localRepo).rebuildRequestFromMessageId(targetId)
                println("    → targetId=$targetId, olderCount=0: rebuilt from targetId")
            }
        }
        println("    ✅ PBT: rebuildRequestFromMessageId called with targetId when no older messages")
    }


    // ==================== 9.5 Test goToMessage failure handling ====================

    test("PBT: uiState transitions to Error when getMessage fails") {
        checkAll(50, Arb.long(1L..10000L)) { messageId ->
            runTest {
                val errorMessage = "Message not found: $messageId"
                whenever(repository.getMessage(messageId)).thenReturn(
                    Result.failure(Exception(errorMessage))
                )

                val vm = createViewModel()
                vm.goToMessage(messageId)
                advanceUntilIdle()

                vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
                val errorState = vm.uiState.value as MessageListUIState.Error
                errorState.exception.message shouldBe errorMessage
                println("    → messageId=$messageId: Error state on getMessage failure")
            }
        }
        println("    ✅ PBT: uiState transitions to Error when getMessage fails")
    }

    test("PBT: uiState transitions to Error when fetchSurroundingMessages fails") {
        checkAll(50, Arb.long(1L..10000L)) { messageId ->
            runTest {
                val targetMessage = MockFactory.createTextMessage(
                    id = messageId,
                    text = "Target",
                    senderUid = "user-1",
                    receiverId = "user-2"
                ) as BaseMessage
                val errorMessage = "Failed to fetch surrounding: $messageId"

                whenever(repository.getMessage(messageId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(messageId)).thenReturn(
                    Result.failure(Exception(errorMessage))
                )

                val vm = createViewModel()
                vm.goToMessage(messageId)
                advanceUntilIdle()

                vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Error>()
                val errorState = vm.uiState.value as MessageListUIState.Error
                errorState.exception.message shouldBe errorMessage
                println("    → messageId=$messageId: Error state on fetchSurrounding failure")
            }
        }
        println("    ✅ PBT: uiState transitions to Error when fetchSurroundingMessages fails")
    }

    test("PBT: fetchSurroundingMessages is not called when getMessage fails") {
        checkAll(30, Arb.long(1L..10000L)) { messageId ->
            runTest {
                whenever(repository.getMessage(messageId)).thenReturn(
                    Result.failure(Exception("Not found"))
                )

                val vm = createViewModel()
                vm.goToMessage(messageId)
                advanceUntilIdle()

                verify(repository, never()).fetchSurroundingMessages(any())
                println("    → messageId=$messageId: fetchSurrounding not called on getMessage failure")
            }
        }
        println("    ✅ PBT: fetchSurroundingMessages not called when getMessage fails")
    }

    test("PBT: rebuildRequestFromMessageId is not called on failure") {
        checkAll(30, Arb.long(1L..10000L)) { messageId ->
            runTest {
                whenever(repository.getMessage(messageId)).thenReturn(
                    Result.failure(Exception("Error"))
                )

                val vm = createViewModel()
                vm.goToMessage(messageId)
                advanceUntilIdle()

                verify(repository, never()).rebuildRequestFromMessageId(any())
                println("    → messageId=$messageId: rebuildRequest not called on failure")
            }
        }
        println("    ✅ PBT: rebuildRequestFromMessageId not called on failure")
    }

    test("PBT: uiState transitions to Loaded on successful goToMessage with messages") {
        checkAll(30, Arb.long(50L..500L), Arb.int(0..10), Arb.int(0..10)) { targetId, olderCount, newerCount ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = olderCount,
                    newerCount = newerCount
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId)
                advanceUntilIdle()

                // Since there's always at least the target message, state should be Loaded
                vm.uiState.value shouldBe MessageListUIState.Loaded
                println("    → targetId=$targetId: uiState=Loaded on success")
            }
        }
        println("    ✅ PBT: uiState transitions to Loaded on successful goToMessage")
    }

    test("PBT: message list is not modified on getMessage failure") {
        checkAll(30, Arb.long(1L..10000L)) { messageId ->
            runTest {
                // Set up initial messages
                val initialMessages = MockFactory.createMessages(
                    count = 5,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(initialMessages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                val initialIds = vm.messages.value.map { it.id }

                // Now goToMessage fails
                whenever(repository.getMessage(messageId)).thenReturn(
                    Result.failure(Exception("Error"))
                )

                vm.goToMessage(messageId)
                advanceUntilIdle()

                // Message list should remain unchanged
                vm.messages.value.map { it.id } shouldBe initialIds
                println("    → messageId=$messageId: message list unchanged on failure")
            }
        }
        println("    ✅ PBT: message list not modified on getMessage failure")
    }


    // ==================== 9.6 Test clearScrollToMessage and clearHighlightScroll ====================

    test("clearScrollToMessage resets scrollToMessageId to null after goToMessage") {
        runTest {
            val targetId = 100L
            val surroundingResult = createSurroundingResult(
                targetId = targetId,
                olderCount = 5,
                newerCount = 5
            )
            val targetMessage = surroundingResult.targetMessage

            whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
            whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

            val vm = createViewModel()

            vm.goToMessage(targetId)
            advanceUntilIdle()

            vm.scrollToMessageId.value shouldBe targetId

            vm.clearScrollToMessage()

            vm.scrollToMessageId.value shouldBe null
            println("    → clearScrollToMessage: scrollToMessageId reset to null")
        }
        println("    ✅ clearScrollToMessage resets scrollToMessageId to null")
    }

    test("clearScrollToMessage is idempotent when already null") {
        runTest {
            val vm = createViewModel()

            vm.scrollToMessageId.value shouldBe null

            vm.clearScrollToMessage()

            vm.scrollToMessageId.value shouldBe null
            println("    → clearScrollToMessage: idempotent when already null")
        }
        println("    ✅ clearScrollToMessage is idempotent")
    }

    test("clearHighlightScroll resets highlightScroll to false after goToMessage") {
        runTest {
            val targetId = 200L
            val surroundingResult = createSurroundingResult(
                targetId = targetId,
                olderCount = 5,
                newerCount = 5
            )
            val targetMessage = surroundingResult.targetMessage

            whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
            whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

            val vm = createViewModel()

            vm.goToMessage(targetId, highlight = true)
            advanceUntilIdle()

            vm.highlightScroll.value shouldBe true

            vm.clearHighlightScroll()

            vm.highlightScroll.value shouldBe false
            println("    → clearHighlightScroll: highlightScroll reset to false")
        }
        println("    ✅ clearHighlightScroll resets highlightScroll to false")
    }

    test("clearHighlightScroll is idempotent when already false") {
        runTest {
            val vm = createViewModel()

            vm.highlightScroll.value shouldBe false

            vm.clearHighlightScroll()

            vm.highlightScroll.value shouldBe false
            println("    → clearHighlightScroll: idempotent when already false")
        }
        println("    ✅ clearHighlightScroll is idempotent")
    }

    test("PBT: clearScrollToMessage works after any successful goToMessage") {
        checkAll(30, Arb.long(50L..500L)) { targetId ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = 3,
                    newerCount = 3
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId)
                advanceUntilIdle()

                vm.scrollToMessageId.value shouldBe targetId
                vm.clearScrollToMessage()
                vm.scrollToMessageId.value shouldBe null
                println("    → targetId=$targetId: clearScrollToMessage works")
            }
        }
        println("    ✅ PBT: clearScrollToMessage works after any successful goToMessage")
    }

    test("PBT: clearHighlightScroll works after any highlighted goToMessage") {
        checkAll(30, Arb.long(50L..500L)) { targetId ->
            runTest {
                val surroundingResult = createSurroundingResult(
                    targetId = targetId,
                    olderCount = 3,
                    newerCount = 3
                )
                val targetMessage = surroundingResult.targetMessage

                whenever(repository.getMessage(targetId)).thenReturn(Result.success(targetMessage))
                whenever(repository.fetchSurroundingMessages(targetId)).thenReturn(Result.success(surroundingResult))

                val vm = createViewModel()
                vm.goToMessage(targetId, highlight = true)
                advanceUntilIdle()

                vm.highlightScroll.value shouldBe true
                vm.clearHighlightScroll()
                vm.highlightScroll.value shouldBe false
                println("    → targetId=$targetId: clearHighlightScroll works")
            }
        }
        println("    ✅ PBT: clearHighlightScroll works after any highlighted goToMessage")
    }

    test("Multiple goToMessage calls — each replaces list and updates scroll target") {
        runTest {
            // First goToMessage
            val targetId1 = 100L
            val result1 = createSurroundingResult(targetId = targetId1, olderCount = 3, newerCount = 3)
            whenever(repository.getMessage(targetId1)).thenReturn(Result.success(result1.targetMessage))
            whenever(repository.fetchSurroundingMessages(targetId1)).thenReturn(Result.success(result1))

            val vm = createViewModel()
            vm.goToMessage(targetId1)
            advanceUntilIdle()

            vm.messages.value shouldHaveSize 7
            vm.scrollToMessageId.value shouldBe targetId1
            vm.messages.value.map { it.id } shouldContain targetId1

            // Second goToMessage
            val targetId2 = 500L
            val result2 = createSurroundingResult(targetId = targetId2, olderCount = 2, newerCount = 2)
            whenever(repository.getMessage(targetId2)).thenReturn(Result.success(result2.targetMessage))
            whenever(repository.fetchSurroundingMessages(targetId2)).thenReturn(Result.success(result2))

            vm.goToMessage(targetId2)
            advanceUntilIdle()

            vm.messages.value shouldHaveSize 5
            vm.scrollToMessageId.value shouldBe targetId2
            vm.messages.value.map { it.id } shouldContain targetId2
            // First target should no longer be in list
            vm.messages.value.none { it.id == targetId1 } shouldBe true
            println("    → Multiple goToMessage: each replaces list correctly")
        }
        println("    ✅ Multiple goToMessage calls replace list and update scroll target")
    }
})
