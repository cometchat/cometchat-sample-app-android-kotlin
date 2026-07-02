package com.cometchat.uikit.core.viewmodel.messagelist

import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.state.MessageListUIState
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
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
 * Layer 5B — ViewModel ListOperations Tests.
 *
 * Tests the CometChatMessageListViewModel's ListOperations interface:
 * - addItem, addItems, removeItem, removeItemAt, updateItem, clearItems
 * - getItems, getItemAt, getItemCount, moveItemToTop
 * - batch applies multiple operations atomically (single emission)
 * - PBT: size invariants (add +1, remove -1, clear =0)
 *
 * Architecture:
 * - Mocks the MessageListRepository interface (layer directly below)
 * - Uses `enableListeners = false` to avoid SDK dependencies
 * - Uses UnconfinedTestDispatcher + Dispatchers.setMain/resetMain
 * - Equality is based on message ID (a.id == b.id)
 *
 * Run with:
 *   ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatMessageListViewModelListOpsTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CometChatMessageListViewModelListOpsTest : FunSpec({

    isolationMode = IsolationMode.SingleInstance

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var repository: MessageListRepository

    /**
     * Creates a fresh ViewModel with no messages loaded.
     */
    fun createEmptyViewModel(): CometChatMessageListViewModel {
        return CometChatMessageListViewModel(
            repository = repository,
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel pre-loaded with the given messages via addItems.
     */
    fun createViewModelWithMessages(messages: List<BaseMessage>): CometChatMessageListViewModel {
        val vm = CometChatMessageListViewModel(
            repository = repository,
            enableListeners = false
        )
        if (messages.isNotEmpty()) {
            vm.addItems(messages)
        }
        return vm
    }

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        whenever(repository.hasMorePreviousMessages()).thenReturn(true)
        whenever(repository.fetchPreviousMessages()).thenReturn(Result.success(emptyList()))
        println("  🧪 ${it.name.testName}")
    }

    afterTest {
        Dispatchers.resetMain()
        println()
    }


    // ==================== 7.2 addItem, addItems, removeItem, removeItemAt, updateItem, clearItems ====================

    test("addItem should add a single message to the list") {
        runTest {
            val vm = createEmptyViewModel()
            val message = MockFactory.createTextMessage(id = 1L, text = "Hello")

            println("    → Adding single message with id=1")
            vm.addItem(message)

            vm.getItemCount() shouldBe 1
            vm.getItemAt(0)?.id shouldBe 1L
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            println("    ✅ addItem: count=1, state=Loaded")
        }
    }

    test("addItem should append to end of existing list") {
        runTest {
            val messages = MockFactory.createMessages(count = 3, startId = 1L)
            val vm = createViewModelWithMessages(messages)
            val newMessage = MockFactory.createTextMessage(id = 10L, text = "New")

            println("    → Adding message id=10 to list of 3")
            vm.addItem(newMessage)

            vm.getItemCount() shouldBe 4
            vm.getItemAt(3)?.id shouldBe 10L
            println("    ✅ addItem appends to end: last item id=10")
        }
    }

    test("addItems should add multiple messages to the list") {
        runTest {
            val vm = createEmptyViewModel()
            val messages = MockFactory.createMessages(count = 5, startId = 1L)

            println("    → Adding 5 messages")
            vm.addItems(messages)

            vm.getItemCount() shouldBe 5
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            println("    ✅ addItems: count=5, state=Loaded")
        }
    }

    test("addItems with empty list should not change state to Loaded") {
        runTest {
            val vm = createEmptyViewModel()

            println("    → Adding empty list")
            vm.addItems(emptyList())

            vm.getItemCount() shouldBe 0
            // State remains Loading (initial state) since addItems with empty doesn't set Loaded
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loading>()
            println("    ✅ addItems(empty): count=0, state unchanged")
        }
    }

    test("removeItem should remove message by ID equality") {
        runTest {
            val messages = MockFactory.createMessages(count = 3, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            // Create a different mock with same ID — should match by ID
            val toRemove = MockFactory.createTextMessage(id = 2L, text = "Different text")

            println("    → Removing message with id=2")
            val result = vm.removeItem(toRemove)

            result shouldBe true
            vm.getItemCount() shouldBe 2
            vm.getItems().map { it.id } shouldContainExactly listOf(1L, 3L)
            println("    ✅ removeItem: removed id=2, remaining=[1, 3]")
        }
    }

    test("removeItem should return false when message not found") {
        runTest {
            val messages = MockFactory.createMessages(count = 2, startId = 1L)
            val vm = createViewModelWithMessages(messages)
            val nonExistent = MockFactory.createTextMessage(id = 999L)

            println("    → Removing non-existent message id=999")
            val result = vm.removeItem(nonExistent)

            result shouldBe false
            vm.getItemCount() shouldBe 2
            println("    ✅ removeItem: not found, count unchanged=2")
        }
    }

    test("removeItem should set Empty state when last item removed") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L)
            val vm = createViewModelWithMessages(listOf(message))

            println("    → Removing last message")
            vm.removeItem(message)

            vm.getItemCount() shouldBe 0
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
            println("    ✅ removeItem: list empty → state=Empty")
        }
    }

    test("removeItemAt should remove message at valid index") {
        runTest {
            val messages = MockFactory.createMessages(count = 3, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            println("    → Removing item at index 1")
            val removed = vm.removeItemAt(1)

            removed?.id shouldBe 2L
            vm.getItemCount() shouldBe 2
            vm.getItems().map { it.id } shouldContainExactly listOf(1L, 3L)
            println("    ✅ removeItemAt(1): removed id=2, remaining=[1, 3]")
        }
    }

    test("removeItemAt should return null for negative index") {
        runTest {
            val messages = MockFactory.createMessages(count = 2, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            println("    → Removing item at index -1")
            val removed = vm.removeItemAt(-1)

            removed shouldBe null
            vm.getItemCount() shouldBe 2
            println("    ✅ removeItemAt(-1): null, count unchanged")
        }
    }

    test("removeItemAt should return null for out-of-bounds index") {
        runTest {
            val messages = MockFactory.createMessages(count = 2, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            println("    → Removing item at index 10")
            val removed = vm.removeItemAt(10)

            removed shouldBe null
            vm.getItemCount() shouldBe 2
            println("    ✅ removeItemAt(10): null, count unchanged")
        }
    }

    test("removeItemAt should set Empty state when last item removed") {
        runTest {
            val message = MockFactory.createTextMessage(id = 1L)
            val vm = createViewModelWithMessages(listOf(message))

            println("    → Removing item at index 0 (last item)")
            vm.removeItemAt(0)

            vm.getItemCount() shouldBe 0
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
            println("    ✅ removeItemAt(0): list empty → state=Empty")
        }
    }

    test("updateItem should replace matching message") {
        runTest {
            val messages = MockFactory.createMessages(count = 3, startId = 1L)
            val vm = createViewModelWithMessages(messages)
            val updatedMessage = MockFactory.createTextMessage(id = 2L, text = "Updated text")

            println("    → Updating message with id=2")
            val result = vm.updateItem(updatedMessage) { it.id == 2L }

            result shouldBe true
            vm.getItemAt(1)?.id shouldBe 2L
            println("    ✅ updateItem: replaced message at index 1")
        }
    }

    test("updateItem should return false when no match found") {
        runTest {
            val messages = MockFactory.createMessages(count = 2, startId = 1L)
            val vm = createViewModelWithMessages(messages)
            val updatedMessage = MockFactory.createTextMessage(id = 999L)

            println("    → Updating non-existent message id=999")
            val result = vm.updateItem(updatedMessage) { it.id == 999L }

            result shouldBe false
            vm.getItemCount() shouldBe 2
            println("    ✅ updateItem: no match, count unchanged")
        }
    }

    test("clearItems should remove all messages and set Empty state") {
        runTest {
            val messages = MockFactory.createMessages(count = 5, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            println("    → Clearing all items")
            vm.clearItems()

            vm.getItemCount() shouldBe 0
            vm.getItems() shouldHaveSize 0
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
            println("    ✅ clearItems: count=0, state=Empty")
        }
    }


    // ==================== 7.3 getItems, getItemAt, getItemCount, moveItemToTop ====================

    test("getItems should return copy of all messages") {
        runTest {
            val messages = MockFactory.createMessages(count = 3, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            println("    → Getting all items")
            val items = vm.getItems()

            items shouldHaveSize 3
            items.map { it.id } shouldContainExactly listOf(1L, 2L, 3L)
            println("    ✅ getItems: returns [1, 2, 3]")
        }
    }

    test("getItems should return empty list when no messages") {
        runTest {
            val vm = createEmptyViewModel()

            println("    → Getting items from empty list")
            val items = vm.getItems()

            items shouldHaveSize 0
            println("    ✅ getItems: returns empty list")
        }
    }

    test("getItemAt should return message at valid index") {
        runTest {
            val messages = MockFactory.createMessages(count = 3, startId = 10L)
            val vm = createViewModelWithMessages(messages)

            println("    → Getting item at index 0, 1, 2")
            vm.getItemAt(0)?.id shouldBe 10L
            vm.getItemAt(1)?.id shouldBe 11L
            vm.getItemAt(2)?.id shouldBe 12L
            println("    ✅ getItemAt: correct items at each index")
        }
    }

    test("getItemAt should return null for invalid index") {
        runTest {
            val messages = MockFactory.createMessages(count = 2, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            println("    → Getting item at invalid indices")
            vm.getItemAt(-1) shouldBe null
            vm.getItemAt(5) shouldBe null
            vm.getItemAt(100) shouldBe null
            println("    ✅ getItemAt: null for -1, 5, 100")
        }
    }

    test("getItemCount should return correct count") {
        runTest {
            val vm = createEmptyViewModel()
            println("    → Checking count at various stages")

            vm.getItemCount() shouldBe 0
            println("    → Empty: count=0")

            vm.addItem(MockFactory.createTextMessage(id = 1L))
            vm.getItemCount() shouldBe 1
            println("    → After add 1: count=1")

            vm.addItems(MockFactory.createMessages(count = 3, startId = 10L))
            vm.getItemCount() shouldBe 4
            println("    → After add 3 more: count=4")

            vm.removeItemAt(0)
            vm.getItemCount() shouldBe 3
            println("    ✅ After remove 1: count=3")
        }
    }

    test("moveItemToTop should move existing message to index 0") {
        runTest {
            val messages = MockFactory.createMessages(count = 4, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            // Move message with id=3 to top
            val messageToMove = MockFactory.createTextMessage(id = 3L)
            println("    → Moving message id=3 to top")
            vm.moveItemToTop(messageToMove)

            vm.getItemAt(0)?.id shouldBe 3L
            vm.getItemAt(1)?.id shouldBe 1L
            vm.getItemAt(2)?.id shouldBe 2L
            vm.getItemAt(3)?.id shouldBe 4L
            vm.getItemCount() shouldBe 4
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            println("    ✅ moveItemToTop: order=[3, 1, 2, 4], state=Loaded")
        }
    }

    test("moveItemToTop should add non-existing message at top") {
        runTest {
            val messages = MockFactory.createMessages(count = 2, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            val newMessage = MockFactory.createTextMessage(id = 99L, text = "New at top")
            println("    → Moving non-existing message id=99 to top")
            vm.moveItemToTop(newMessage)

            vm.getItemCount() shouldBe 3
            vm.getItemAt(0)?.id shouldBe 99L
            vm.getItemAt(1)?.id shouldBe 1L
            vm.getItemAt(2)?.id shouldBe 2L
            println("    ✅ moveItemToTop (new): order=[99, 1, 2], count=3")
        }
    }

    test("moveItemToTop on empty list should add item") {
        runTest {
            val vm = createEmptyViewModel()
            val message = MockFactory.createTextMessage(id = 1L)

            println("    → Moving item to top of empty list")
            vm.moveItemToTop(message)

            vm.getItemCount() shouldBe 1
            vm.getItemAt(0)?.id shouldBe 1L
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            println("    ✅ moveItemToTop (empty list): count=1, state=Loaded")
        }
    }


    // ==================== 7.4 batch applies multiple operations atomically ====================

    test("batch should perform add and remove atomically") {
        runTest {
            val messages = MockFactory.createMessages(count = 3, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            val newMessage = MockFactory.createTextMessage(id = 10L, text = "Batch added")
            val toRemove = MockFactory.createTextMessage(id = 2L)

            println("    → Batch: add id=10, remove id=2")
            vm.batch {
                add(newMessage)
                remove(toRemove)
            }

            vm.getItemCount() shouldBe 3
            vm.getItems().map { it.id } shouldContainExactly listOf(1L, 3L, 10L)
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            println("    ✅ batch: result=[1, 3, 10], state=Loaded")
        }
    }

    test("batch should support addAll operation") {
        runTest {
            val vm = createEmptyViewModel()
            val messages = MockFactory.createMessages(count = 3, startId = 1L)

            println("    → Batch: addAll 3 messages")
            vm.batch {
                addAll(messages)
            }

            vm.getItemCount() shouldBe 3
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            println("    ✅ batch addAll: count=3, state=Loaded")
        }
    }

    test("batch should support removeAt operation") {
        runTest {
            val messages = MockFactory.createMessages(count = 4, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            println("    → Batch: removeAt index 1 and 2")
            vm.batch {
                removeAt(2)
                removeAt(1)
            }

            vm.getItemCount() shouldBe 2
            vm.getItems().map { it.id } shouldContainExactly listOf(1L, 4L)
            println("    ✅ batch removeAt: result=[1, 4]")
        }
    }

    test("batch should support update operation") {
        runTest {
            val messages = MockFactory.createMessages(count = 3, startId = 1L)
            val vm = createViewModelWithMessages(messages)
            val updatedMessage = MockFactory.createTextMessage(id = 2L, text = "Batch updated")

            println("    → Batch: update message id=2")
            vm.batch {
                update(updatedMessage) { it.id == 2L }
            }

            vm.getItemAt(1)?.id shouldBe 2L
            vm.getItemCount() shouldBe 3
            println("    ✅ batch update: message at index 1 updated")
        }
    }

    test("batch should support moveToTop operation") {
        runTest {
            val messages = MockFactory.createMessages(count = 3, startId = 1L)
            val vm = createViewModelWithMessages(messages)
            val messageToMove = MockFactory.createTextMessage(id = 3L)

            println("    → Batch: moveToTop id=3")
            vm.batch {
                moveToTop(messageToMove)
            }

            vm.getItemAt(0)?.id shouldBe 3L
            vm.getItemCount() shouldBe 3
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            println("    ✅ batch moveToTop: first item id=3")
        }
    }

    test("batch clear should empty list and set Empty state") {
        runTest {
            val messages = MockFactory.createMessages(count = 5, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            println("    → Batch: clear")
            vm.batch { clear() }

            vm.getItemCount() shouldBe 0
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
            println("    ✅ batch clear: count=0, state=Empty")
        }
    }

    test("batch with multiple mixed operations should apply all atomically") {
        runTest {
            val messages = MockFactory.createMessages(count = 5, startId = 1L)
            val vm = createViewModelWithMessages(messages)

            val newMsg1 = MockFactory.createTextMessage(id = 20L, text = "New 1")
            val newMsg2 = MockFactory.createTextMessage(id = 21L, text = "New 2")
            val removeTarget = MockFactory.createTextMessage(id = 3L)
            val updateTarget = MockFactory.createTextMessage(id = 1L, text = "Updated 1")

            println("    → Batch: add 2, remove 1, update 1")
            vm.batch {
                add(newMsg1)
                add(newMsg2)
                remove(removeTarget)
                update(updateTarget) { it.id == 1L }
            }

            // Original: [1, 2, 3, 4, 5]
            // After batch: remove 3 → [1, 2, 4, 5], add 20, 21 → [1, 2, 4, 5, 20, 21], update 1
            vm.getItemCount() shouldBe 6
            val ids = vm.getItems().map { it.id }
            ids shouldContainExactly listOf(1L, 2L, 4L, 5L, 20L, 21L)
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            println("    ✅ batch mixed: result=$ids, state=Loaded")
        }
    }

    test("batch on empty list resulting in empty should set Empty state") {
        runTest {
            val vm = createViewModelWithMessages(listOf(MockFactory.createTextMessage(id = 1L)))

            println("    → Batch: remove only item")
            vm.batch {
                remove(MockFactory.createTextMessage(id = 1L))
            }

            vm.getItemCount() shouldBe 0
            vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
            println("    ✅ batch resulting in empty: state=Empty")
        }
    }


    // ==================== 7.5 PBT: size invariants (add +1, remove -1, clear =0) ====================

    test("PBT: addItem always increases count by 1") {
        checkAll(50, Arb.int(0..15)) { initialCount ->
            runTest {
                val messages = if (initialCount > 0) {
                    MockFactory.createMessages(count = initialCount, startId = 1L)
                } else {
                    emptyList()
                }
                val vm = createViewModelWithMessages(messages)

                val newMessage = MockFactory.createTextMessage(id = (initialCount + 100).toLong())
                vm.addItem(newMessage)

                println("    → initial=$initialCount, after addItem=${vm.getItemCount()}")
                vm.getItemCount() shouldBe initialCount + 1
                vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
            }
        }
        println("    ✅ PBT: addItem always increases count by 1")
    }

    test("PBT: addItems increases count by items.size") {
        checkAll(30, Arb.int(0..10), Arb.int(1..10)) { initialCount, addCount ->
            runTest {
                val messages = if (initialCount > 0) {
                    MockFactory.createMessages(count = initialCount, startId = 1L)
                } else {
                    emptyList()
                }
                val vm = createViewModelWithMessages(messages)

                val newMessages = MockFactory.createMessages(
                    count = addCount,
                    startId = (initialCount + 100).toLong()
                )
                vm.addItems(newMessages)

                println("    → initial=$initialCount, added=$addCount, total=${vm.getItemCount()}")
                vm.getItemCount() shouldBe initialCount + addCount
            }
        }
        println("    ✅ PBT: addItems increases count by items.size")
    }

    test("PBT: removeItem decreases count by 1 when item exists") {
        checkAll(30, Arb.int(2..15)) { initialCount ->
            runTest {
                val messages = MockFactory.createMessages(count = initialCount, startId = 1L)
                val vm = createViewModelWithMessages(messages)

                // Remove the first message (always exists)
                val toRemove = MockFactory.createTextMessage(id = 1L)
                val result = vm.removeItem(toRemove)

                println("    → initial=$initialCount, after removeItem=${vm.getItemCount()}")
                result shouldBe true
                vm.getItemCount() shouldBe initialCount - 1
            }
        }
        println("    ✅ PBT: removeItem decreases count by 1")
    }

    test("PBT: removeItemAt decreases count by 1 for valid index") {
        checkAll(30, Arb.int(2..15)) { initialCount ->
            runTest {
                val messages = MockFactory.createMessages(count = initialCount, startId = 1L)
                val vm = createViewModelWithMessages(messages)

                // Remove at index 0 (always valid)
                val removed = vm.removeItemAt(0)

                println("    → initial=$initialCount, removed id=${removed?.id}, count=${vm.getItemCount()}")
                removed shouldBe messages[0]
                vm.getItemCount() shouldBe initialCount - 1
            }
        }
        println("    ✅ PBT: removeItemAt decreases count by 1")
    }

    test("PBT: clearItems always results in count=0 and Empty state") {
        checkAll(30, Arb.int(0..20)) { initialCount ->
            runTest {
                val messages = if (initialCount > 0) {
                    MockFactory.createMessages(count = initialCount, startId = 1L)
                } else {
                    emptyList()
                }
                val vm = createViewModelWithMessages(messages)

                vm.clearItems()

                println("    → initial=$initialCount, after clear=${vm.getItemCount()}")
                vm.getItemCount() shouldBe 0
                vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
            }
        }
        println("    ✅ PBT: clearItems always results in count=0, state=Empty")
    }

    test("PBT: moveItemToTop preserves count when item exists") {
        checkAll(30, Arb.int(2..15)) { initialCount ->
            runTest {
                val messages = MockFactory.createMessages(count = initialCount, startId = 1L)
                val vm = createViewModelWithMessages(messages)

                // Move last item to top
                val lastId = initialCount.toLong()
                val toMove = MockFactory.createTextMessage(id = lastId)
                vm.moveItemToTop(toMove)

                println("    → initial=$initialCount, moved id=$lastId to top, count=${vm.getItemCount()}")
                vm.getItemCount() shouldBe initialCount
                vm.getItemAt(0)?.id shouldBe lastId
            }
        }
        println("    ✅ PBT: moveItemToTop preserves count for existing items")
    }

    test("PBT: moveItemToTop increases count by 1 when item does not exist") {
        checkAll(30, Arb.int(0..10)) { initialCount ->
            runTest {
                val messages = if (initialCount > 0) {
                    MockFactory.createMessages(count = initialCount, startId = 1L)
                } else {
                    emptyList()
                }
                val vm = createViewModelWithMessages(messages)

                // Move a non-existing item to top
                val newItem = MockFactory.createTextMessage(id = 999L)
                vm.moveItemToTop(newItem)

                println("    → initial=$initialCount, moveToTop new item, count=${vm.getItemCount()}")
                vm.getItemCount() shouldBe initialCount + 1
                vm.getItemAt(0)?.id shouldBe 999L
            }
        }
        println("    ✅ PBT: moveItemToTop adds 1 when item doesn't exist")
    }

    test("PBT: batch add+remove net effect is correct") {
        checkAll(30, Arb.int(3..10), Arb.int(1..3), Arb.int(1..2)) { initialCount, addCount, removeCount ->
            runTest {
                val messages = MockFactory.createMessages(count = initialCount, startId = 1L)
                val vm = createViewModelWithMessages(messages)

                val newMessages = MockFactory.createMessages(
                    count = addCount,
                    startId = (initialCount + 100).toLong()
                )
                // Remove first N items (guaranteed to exist)
                val actualRemoveCount = removeCount.coerceAtMost(initialCount)

                vm.batch {
                    addAll(newMessages)
                    for (i in 0 until actualRemoveCount) {
                        remove(MockFactory.createTextMessage(id = (i + 1).toLong()))
                    }
                }

                val expectedCount = initialCount + addCount - actualRemoveCount
                println("    → initial=$initialCount, +$addCount, -$actualRemoveCount = ${vm.getItemCount()}")
                vm.getItemCount() shouldBe expectedCount
            }
        }
        println("    ✅ PBT: batch add+remove net effect is correct")
    }

    test("PBT: updateItem preserves count") {
        checkAll(30, Arb.int(1..15)) { initialCount ->
            runTest {
                val messages = MockFactory.createMessages(count = initialCount, startId = 1L)
                val vm = createViewModelWithMessages(messages)

                val updatedMessage = MockFactory.createTextMessage(id = 1L, text = "Updated")
                vm.updateItem(updatedMessage) { it.id == 1L }

                println("    → initial=$initialCount, after update=${vm.getItemCount()}")
                vm.getItemCount() shouldBe initialCount
            }
        }
        println("    ✅ PBT: updateItem preserves count")
    }

    test("PBT: state is Loaded when count > 0, Empty when count == 0") {
        checkAll(50, Arb.int(0..15)) { count ->
            runTest {
                val messages = if (count > 0) {
                    MockFactory.createMessages(count = count, startId = 1L)
                } else {
                    emptyList()
                }
                val vm = createViewModelWithMessages(messages)

                // If we added items, state should be Loaded; if empty, state is Loading (initial)
                if (count > 0) {
                    vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
                    // Now clear and verify Empty
                    vm.clearItems()
                    vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Empty>()
                    // Add back and verify Loaded
                    vm.addItem(MockFactory.createTextMessage(id = 100L))
                    vm.uiState.value.shouldBeInstanceOf<MessageListUIState.Loaded>()
                }
                println("    → count=$count: state transitions verified")
            }
        }
        println("    ✅ PBT: state is Loaded when count > 0, Empty when count == 0")
    }
})
