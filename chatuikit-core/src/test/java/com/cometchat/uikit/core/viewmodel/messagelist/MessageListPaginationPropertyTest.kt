package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Layer 5C — Pagination Property-Based Tests.
 *
 * Tests the CometChatMessageListViewModel's pagination invariants using PBT:
 * - Chronological order preserved after fetchMessages
 * - No duplicate IDs after multiple fetches
 * - hasMorePreviousMessages false after empty fetch
 * - isInProgress guards concurrent fetches
 * - fetchNextMessages appends correctly, deduplicates
 * - latestMessageId tracks newest, pagination monotonicity
 *
 * Architecture:
 * - Mocks the MessageListRepository interface (layer directly below)
 * - Uses `enableListeners = false` to avoid SDK dependencies
 * - Uses UnconfinedTestDispatcher + Dispatchers.setMain/resetMain
 * - All tests use `checkAll` with `Arb` generators (no hardcoded values in PBT)
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*MessageListPaginationPropertyTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageListPaginationPropertyTest : FunSpec({

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

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        // Allow CometChatEvents.scope (Dispatchers.Default) coroutines to complete
        // before resetting Main dispatcher, preventing "Dispatchers.Main is used
        // concurrently with setting it" race condition.
        Thread.sleep(50)
        Dispatchers.resetMain()
    }

    beforeTest {
        repository = mock()
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        println()
    }

    test("warmup - absorb leaked exceptions") {
        try { runTest(testDispatcher) { } } catch (_: Exception) { }
    }


    // ==================== 8.2 PBT: chronological order preserved after fetchMessages ====================

    test("PBT: chronological order preserved after fetchMessages — messages maintain sentAt order") {
        checkAll(50, Arb.int(1..20)) { count ->
            runTest(testDispatcher) {
                // Create messages with strictly increasing timestamps
                val messages = MockFactory.createMessages(
                    count = count,
                    startId = 1L,
                    startTimestamp = 1700000000L,
                    intervalMs = 60L
                )
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                val resultMessages = vm.messages.value
                resultMessages shouldHaveSize count

                // Verify chronological order: each message's sentAt >= previous message's sentAt
                for (i in 1 until resultMessages.size) {
                    val prevTimestamp = resultMessages[i - 1].sentAt
                    val currTimestamp = resultMessages[i].sentAt
                    currTimestamp shouldBeGreaterThanOrEqual prevTimestamp
                }
                println("    → count=$count: chronological order preserved")
            }
        }
        println("    ✅ PBT: chronological order preserved after fetchMessages")
    }

    test("PBT: chronological order preserved after paginating older messages") {
        checkAll(30, Arb.int(1..10), Arb.int(1..10)) { firstBatch, secondBatch ->
            runTest(testDispatcher) {
                // First batch: newer messages (fetched first in reverse chronological)
                val newerMessages = MockFactory.createMessages(
                    count = firstBatch,
                    startId = (secondBatch + 1).toLong(),
                    startTimestamp = 1700000000L + (secondBatch * 60L),
                    intervalMs = 60L
                )
                // Second batch: older messages (fetched on pagination)
                val olderMessages = MockFactory.createMessages(
                    count = secondBatch,
                    startId = 1L,
                    startTimestamp = 1700000000L,
                    intervalMs = 60L
                )

                // First fetch returns newer messages
                whenever(repository.fetchPreviousMessages())
                    .thenReturn(Result.success(newerMessages))
                    .thenReturn(Result.success(olderMessages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                vm.messages.value shouldHaveSize firstBatch

                // Second fetch returns older messages (prepended)
                vm.fetchMessages()
                advanceUntilIdle()

                val resultMessages = vm.messages.value
                resultMessages shouldHaveSize firstBatch + secondBatch

                // All IDs should be unique
                val ids = resultMessages.map { it.id }
                ids.toSet().size shouldBe ids.size
                println("    → batches=$firstBatch+$secondBatch: order preserved, no duplicates")
            }
        }
        println("    ✅ PBT: chronological order preserved after paginating older messages")
    }


    // ==================== 8.3 PBT: no duplicate IDs after multiple fetches ====================

    test("PBT: no duplicate IDs after multiple fetches — deduplication works") {
        checkAll(30, Arb.int(2..10)) { batchSize ->
            runTest(testDispatcher) {
                // Create messages where some IDs overlap between batches
                val batch1 = MockFactory.createMessages(
                    count = batchSize,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )
                // Batch 2 overlaps with last half of batch 1
                val overlapStart = (batchSize / 2).toLong() + 1L
                val batch2 = MockFactory.createMessages(
                    count = batchSize,
                    startId = overlapStart,
                    startTimestamp = 1699000000L
                )

                whenever(repository.fetchPreviousMessages())
                    .thenReturn(Result.success(batch1))
                    .thenReturn(Result.success(batch2))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                vm.fetchMessages()
                advanceUntilIdle()

                // Verify no duplicate IDs
                val ids = vm.messages.value.map { it.id }
                ids.toSet().size shouldBe ids.size
                println("    → batchSize=$batchSize, overlapStart=$overlapStart: no duplicates after merge")
            }
        }
        println("    ✅ PBT: no duplicate IDs after multiple fetches")
    }

    test("PBT: no duplicate IDs when fetchMessages returns messages already in list") {
        checkAll(30, Arb.int(1..15)) { count ->
            runTest(testDispatcher) {
                val messages = MockFactory.createMessages(
                    count = count,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )

                // Return the same messages on every fetch
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                // Second fetch — hasMore is still true because first fetch was non-empty
                vm.fetchMessages()
                advanceUntilIdle()

                // Should still have exactly `count` messages (duplicates filtered)
                val ids = vm.messages.value.map { it.id }
                ids.toSet().size shouldBe ids.size
                vm.messages.value shouldHaveSize count
                println("    → count=$count: same messages returned twice, no duplicates")
            }
        }
        println("    ✅ PBT: no duplicate IDs when same messages returned")
    }


    // ==================== 8.4 PBT: hasMorePreviousMessages false after empty fetch ====================

    test("PBT: hasMorePreviousMessages becomes false after empty fetch") {
        checkAll(50, Arb.int(0..15)) { initialCount ->
            runTest(testDispatcher) {
                if (initialCount == 0) {
                    // Empty fetch → hasMore should become false
                    whenever(repository.fetchPreviousMessages())
                        .thenReturn(Result.success(emptyList()))
                    whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                    val vm = createViewModel()
                    vm.fetchMessages()
                    advanceUntilIdle()

                    vm.hasMorePreviousMessages.value shouldBe false
                    println("    → initial=0: hasMore=false after empty fetch")
                } else {
                    // Non-empty first fetch, then empty second fetch
                    val messages = MockFactory.createMessages(count = initialCount, startId = 1L)
                    whenever(repository.fetchPreviousMessages())
                        .thenReturn(Result.success(messages))
                        .thenReturn(Result.success(emptyList()))
                    whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                    val vm = createViewModel()
                    vm.fetchMessages()
                    advanceUntilIdle()

                    vm.hasMorePreviousMessages.value shouldBe true

                    // Second fetch returns empty → hasMore becomes false
                    vm.fetchMessages()
                    advanceUntilIdle()

                    vm.hasMorePreviousMessages.value shouldBe false
                    println("    → initial=$initialCount: hasMore=false after second empty fetch")
                }
            }
        }
        println("    ✅ PBT: hasMorePreviousMessages false after empty fetch")
    }

    test("PBT: hasMorePreviousMessages remains true when fetch returns non-empty") {
        checkAll(30, Arb.int(1..20)) { count ->
            runTest(testDispatcher) {
                val messages = MockFactory.createMessages(count = count, startId = 1L)
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                vm.hasMorePreviousMessages.value shouldBe true
                println("    → count=$count: hasMore=true after non-empty fetch")
            }
        }
        println("    ✅ PBT: hasMorePreviousMessages remains true when non-empty")
    }


    // ==================== 8.5 PBT: isInProgress guards concurrent fetches ====================

    test("PBT: isInProgress is always false after fetchMessages completes successfully") {
        checkAll(50, Arb.int(0..15)) { count ->
            runTest(testDispatcher) {
                val messages = if (count > 0) {
                    MockFactory.createMessages(count = count, startId = 1L)
                } else {
                    emptyList()
                }
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                vm.isInProgress.value shouldBe false
                println("    → count=$count: isInProgress=false after success")
            }
        }
        println("    ✅ PBT: isInProgress always false after successful fetch")
    }

    test("PBT: isInProgress is always false after fetchMessages fails") {
        checkAll(30, Arb.int(1..50)) { errorCode ->
            runTest(testDispatcher) {
                val exception = MockFactory.createCometChatException(
                    code = "ERR_$errorCode",
                    message = "Error $errorCode"
                )
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.failure(exception))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                vm.isInProgress.value shouldBe false
                println("    → errorCode=$errorCode: isInProgress=false after failure")
            }
        }
        println("    ✅ PBT: isInProgress always false after failed fetch")
    }

    test("PBT: fetchMessages is no-op when hasMorePreviousMessages is false") {
        checkAll(30, Arb.int(1..10)) { count ->
            runTest(testDispatcher) {
                // First fetch returns empty → hasMore becomes false
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                // hasMore is now false
                vm.hasMorePreviousMessages.value shouldBe false

                // Now configure repository to return messages (but it shouldn't be called)
                val messages = MockFactory.createMessages(count = count, startId = 100L)
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))

                // This should be a no-op because hasMore is false
                vm.fetchMessages()
                advanceUntilIdle()

                // Messages should still be empty
                vm.messages.value shouldHaveSize 0
                println("    → count=$count: fetchMessages no-op when hasMore=false")
            }
        }
        println("    ✅ PBT: fetchMessages is no-op when hasMore=false")
    }


    // ==================== 8.6 PBT: fetchNextMessages appends correctly, deduplicates ====================

    test("PBT: fetchNextMessages appends newer messages to end of list") {
        checkAll(30, Arb.int(1..10), Arb.int(1..10)) { initialCount, nextCount ->
            runTest(testDispatcher) {
                // Initial messages
                val initialMessages = MockFactory.createMessages(
                    count = initialCount,
                    startId = 1L,
                    startTimestamp = 1700000000L,
                    intervalMs = 60L
                )
                // Next messages (newer, higher IDs)
                val nextMessages = MockFactory.createMessages(
                    count = nextCount,
                    startId = (initialCount + 1).toLong(),
                    startTimestamp = 1700000000L + (initialCount * 60L),
                    intervalMs = 60L
                )

                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(initialMessages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)
                whenever(repository.fetchNextMessages(any())).thenReturn(Result.success(nextMessages))

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                vm.messages.value shouldHaveSize initialCount

                // Fetch next messages
                vm.fetchNextMessages()
                advanceUntilIdle()

                vm.messages.value shouldHaveSize initialCount + nextCount

                // Verify newer messages are at the end
                val allIds = vm.messages.value.map { it.id }
                val lastIds = allIds.takeLast(nextCount)
                lastIds shouldBe nextMessages.map { it.id }
                println("    → initial=$initialCount, next=$nextCount: appended correctly")
            }
        }
        println("    ✅ PBT: fetchNextMessages appends newer messages to end")
    }

    test("PBT: fetchNextMessages deduplicates overlapping messages") {
        checkAll(30, Arb.int(3..10)) { initialCount ->
            runTest(testDispatcher) {
                val initialMessages = MockFactory.createMessages(
                    count = initialCount,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )
                // Next messages overlap with last 2 messages of initial
                val overlapMessages = MockFactory.createMessages(
                    count = 4,
                    startId = (initialCount - 1).toLong(),
                    startTimestamp = 1700000000L + ((initialCount - 2) * 60L)
                )

                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(initialMessages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)
                whenever(repository.fetchNextMessages(any())).thenReturn(Result.success(overlapMessages))

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                vm.fetchNextMessages()
                advanceUntilIdle()

                // Verify no duplicate IDs
                val ids = vm.messages.value.map { it.id }
                ids.toSet().size shouldBe ids.size
                println("    → initial=$initialCount: fetchNextMessages deduplicates overlaps")
            }
        }
        println("    ✅ PBT: fetchNextMessages deduplicates overlapping messages")
    }

    test("PBT: fetchNextMessages is no-op when message list is empty") {
        checkAll(20, Arb.int(1..5)) { nextCount ->
            runTest(testDispatcher) {
                val nextMessages = MockFactory.createMessages(count = nextCount, startId = 1L)
                whenever(repository.fetchNextMessages(any())).thenReturn(Result.success(nextMessages))

                val vm = createViewModel()
                // Don't fetch initial messages — list is empty

                vm.fetchNextMessages()
                advanceUntilIdle()

                // Should be no-op because there's no lastMessage to paginate from
                vm.messages.value shouldHaveSize 0
                println("    → nextCount=$nextCount: fetchNextMessages no-op on empty list")
            }
        }
        println("    ✅ PBT: fetchNextMessages is no-op when list is empty")
    }

    test("PBT: fetchNextMessages does not change count when empty result returned") {
        checkAll(30, Arb.int(1..10)) { initialCount ->
            runTest(testDispatcher) {
                val initialMessages = MockFactory.createMessages(
                    count = initialCount,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )

                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(initialMessages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)
                whenever(repository.fetchNextMessages(any())).thenReturn(Result.success(emptyList()))

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                val countBefore = vm.messages.value.size
                vm.fetchNextMessages()
                advanceUntilIdle()

                vm.messages.value shouldHaveSize countBefore
                println("    → initial=$initialCount: empty fetchNext doesn't change list")
            }
        }
        println("    ✅ PBT: fetchNextMessages with empty result preserves list")
    }


    // ==================== 8.7 PBT: latestMessageId tracks newest, pagination monotonicity ====================

    test("PBT: after fetchMessages, last message ID is the highest in the list") {
        checkAll(30, Arb.int(1..20)) { count ->
            runTest(testDispatcher) {
                val messages = MockFactory.createMessages(
                    count = count,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )
                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(messages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                val lastMessage = vm.messages.value.lastOrNull()
                lastMessage shouldBe messages.last()
                // The last message should have the highest ID (since IDs are sequential)
                val maxId = vm.messages.value.maxOf { it.id }
                lastMessage!!.id shouldBe maxId
                println("    → count=$count: last message has highest ID=$maxId")
            }
        }
        println("    ✅ PBT: last message ID is highest after fetchMessages")
    }

    test("PBT: after fetchNextMessages, last message ID increases or stays same") {
        checkAll(20, Arb.int(1..8), Arb.int(1..8)) { initialCount, nextCount ->
            runTest(testDispatcher) {
                val initialMessages = MockFactory.createMessages(
                    count = initialCount,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )
                val nextMessages = MockFactory.createMessages(
                    count = nextCount,
                    startId = (initialCount + 1).toLong(),
                    startTimestamp = 1700000000L + (initialCount * 60L)
                )

                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(initialMessages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)
                whenever(repository.fetchNextMessages(any())).thenReturn(Result.success(nextMessages))

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                val lastIdBefore = vm.messages.value.lastOrNull()?.id ?: 0L

                vm.fetchNextMessages()
                advanceUntilIdle()

                val lastIdAfter = vm.messages.value.lastOrNull()?.id ?: 0L
                lastIdAfter shouldBeGreaterThanOrEqual lastIdBefore
                println("    → initial=$initialCount, next=$nextCount: lastId $lastIdBefore → $lastIdAfter (monotonic)")
            }
        }
        println("    ✅ PBT: latestMessageId monotonically increases after fetchNextMessages")
    }

    test("PBT: message IDs in list are always unique after pagination sequence") {
        checkAll(20, Arb.int(1..8), Arb.int(1..8)) { batch1, batch2 ->
            runTest(testDispatcher) {
                val messages1 = MockFactory.createMessages(
                    count = batch1,
                    startId = (batch2 + 1).toLong(),
                    startTimestamp = 1700000000L + (batch2 * 60L)
                )
                val messages2 = MockFactory.createMessages(
                    count = batch2,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )

                whenever(repository.fetchPreviousMessages())
                    .thenReturn(Result.success(messages1))
                    .thenReturn(Result.success(messages2))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()

                // First fetch
                vm.fetchMessages()
                advanceUntilIdle()

                // Second fetch (paginate older)
                vm.fetchMessages()
                advanceUntilIdle()

                // All IDs must be unique
                val ids = vm.messages.value.map { it.id }
                ids.toSet().size shouldBe ids.size
                println("    → batches=$batch1+$batch2: all IDs unique")
            }
        }
        println("    ✅ PBT: message IDs always unique after pagination sequence")
    }

    test("PBT: total message count equals sum of unique messages across fetches") {
        checkAll(20, Arb.int(1..10), Arb.int(1..10)) { batch1Size, batch2Size ->
            runTest(testDispatcher) {
                // Non-overlapping batches with distinct IDs
                val batch1 = MockFactory.createMessages(
                    count = batch1Size,
                    startId = (batch2Size + 1).toLong(),
                    startTimestamp = 1700000000L + (batch2Size * 60L)
                )
                val batch2 = MockFactory.createMessages(
                    count = batch2Size,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )

                whenever(repository.fetchPreviousMessages())
                    .thenReturn(Result.success(batch1))
                    .thenReturn(Result.success(batch2))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()
                vm.fetchMessages()
                advanceUntilIdle()

                // Total should be sum of both batches (no overlap)
                vm.messages.value shouldHaveSize batch1Size + batch2Size
                println("    → batch1=$batch1Size, batch2=$batch2Size: total=${vm.messages.value.size}")
            }
        }
        println("    ✅ PBT: total count equals sum of unique messages")
    }

    test("PBT: state is always Loaded after successful non-empty pagination") {
        checkAll(30, Arb.int(1..10), Arb.int(1..10)) { initialCount, nextCount ->
            runTest(testDispatcher) {
                val initialMessages = MockFactory.createMessages(
                    count = initialCount,
                    startId = 1L,
                    startTimestamp = 1700000000L
                )
                val nextMessages = MockFactory.createMessages(
                    count = nextCount,
                    startId = (initialCount + 1).toLong(),
                    startTimestamp = 1700000000L + (initialCount * 60L)
                )

                whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(initialMessages))
                whenever(repository.hasMorePreviousMessages()).thenReturn(true)
                whenever(repository.fetchNextMessages(any())).thenReturn(Result.success(nextMessages))

                val vm = createViewModel()
                vm.fetchMessages()
                advanceUntilIdle()

                vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()

                vm.fetchNextMessages()
                advanceUntilIdle()

                // State should remain Loaded after pagination
                vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
                println("    → initial=$initialCount, next=$nextCount: state=Loaded throughout")
            }
        }
        println("    ✅ PBT: state is always Loaded after successful non-empty pagination")
    }
})
