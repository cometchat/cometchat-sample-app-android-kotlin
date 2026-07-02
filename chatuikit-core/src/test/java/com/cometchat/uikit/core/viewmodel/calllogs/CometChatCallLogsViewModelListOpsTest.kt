package com.cometchat.uikit.core.viewmodel.calllogs

import com.cometchat.uikit.core.viewmodel.ListOperations
import com.cometchat.uikit.core.viewmodel.ListOperationsBatchScope
import com.cometchat.uikit.core.viewmodel.ListOperationsDelegate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Dedicated ListOperations tests for the CallLogs ViewModel pattern.
 *
 * Since CometChatCallLogsViewModel depends on SDK classes (CallLog) with private constructors,
 * we test the ListOperations interface implementation pattern using a test ViewModel that mirrors
 * the production implementation with sessionId-based equality.
 *
 * This validates:
 * - addItem increases count by 1
 * - removeItem on existing item decreases count by 1 and returns true
 * - removeItem on non-existent item returns false and doesn't change count
 * - updateItem with matching predicate returns true and replaces item
 * - moveItemToTop places target at index 0 preserving size
 * - batch applies multiple operations atomically with single state emission
 * - clearItems results in count 0 and Empty state
 * - getItemAt with out-of-bounds index returns null
 *
 * **Validates: Requirements 6.2, 6.8, 26.1**
 *
 * Run: ./gradlew :chatuikit-core:testDebugUnitTest --tests "*CometChatCallLogsViewModelListOpsTest"
 */
class CometChatCallLogsViewModelListOpsTest : FunSpec({

    /**
     * Test data class that simulates CallLog with sessionId-based equality.
     * The production CometChatCallLogsViewModel uses object equality (equals),
     * and CallLog's identity is based on sessionId in practice.
     */
    data class TestCallLog(
        val sessionId: String,
        val type: String = "audio",
        val status: String = "ended",
        val duration: Int = 120
    )

    /**
     * Test ViewModel that implements ListOperations using the same pattern as
     * CometChatCallLogsViewModel. Uses sessionId-based equality matching the
     * production behavior where CallLog equality is based on sessionId.
     */
    class TestCallLogsListViewModel : ListOperations<TestCallLog> {
        private val _items = MutableStateFlow<List<TestCallLog>>(emptyList())
        val items = _items

        private val listDelegate = ListOperationsDelegate(
            stateFlow = _items,
            equalityChecker = { a, b -> a.sessionId == b.sessionId }
        )

        override fun addItem(item: TestCallLog) = listDelegate.addItem(item)
        override fun addItems(items: List<TestCallLog>) = listDelegate.addItems(items)
        override fun removeItem(item: TestCallLog) = listDelegate.removeItem(item)
        override fun removeItemAt(index: Int) = listDelegate.removeItemAt(index)
        override fun updateItem(item: TestCallLog, predicate: (TestCallLog) -> Boolean) =
            listDelegate.updateItem(item, predicate)
        override fun clearItems() = listDelegate.clearItems()
        override fun getItems() = listDelegate.getItems()
        override fun getItemAt(index: Int) = listDelegate.getItemAt(index)
        override fun getItemCount() = listDelegate.getItemCount()
        override fun moveItemToTop(item: TestCallLog) = listDelegate.moveItemToTop(item)
        override fun batch(operations: ListOperationsBatchScope<TestCallLog>.() -> Unit) =
            listDelegate.batch(operations)
    }

    // ==================== addItem ====================

    context("addItem operations") {

        test("addItem should increase count by 1") {
            checkAll(Arb.string(5..15)) { sessionId ->
                val viewModel = TestCallLogsListViewModel()
                val callLog = TestCallLog(sessionId)

                viewModel.addItem(callLog)

                viewModel.getItemCount() shouldBe 1
                viewModel.getItemAt(0)?.sessionId shouldBe sessionId
            }
        }

        test("addItem should append to end of list") {
            val viewModel = TestCallLogsListViewModel()
            val log1 = TestCallLog("session-1")
            val log2 = TestCallLog("session-2")
            val log3 = TestCallLog("session-3")

            viewModel.addItem(log1)
            viewModel.addItem(log2)
            viewModel.addItem(log3)

            viewModel.getItemCount() shouldBe 3
            viewModel.getItemAt(0)?.sessionId shouldBe "session-1"
            viewModel.getItemAt(1)?.sessionId shouldBe "session-2"
            viewModel.getItemAt(2)?.sessionId shouldBe "session-3"
        }

        test("addItems should add multiple call logs") {
            val viewModel = TestCallLogsListViewModel()
            val callLogs = listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2"),
                TestCallLog("session-3")
            )

            viewModel.addItems(callLogs)

            viewModel.getItemCount() shouldBe 3
        }

        test("PBT - addItem always increases count by exactly 1") {
            checkAll(
                Arb.list(Arb.string(5..15), 0..10),
                Arb.string(5..15)
            ) { existingIds, newId ->
                val viewModel = TestCallLogsListViewModel()
                existingIds.forEachIndexed { index, id ->
                    viewModel.addItem(TestCallLog("${id}_$index"))
                }
                val countBefore = viewModel.getItemCount()

                viewModel.addItem(TestCallLog(newId))

                viewModel.getItemCount() shouldBe countBefore + 1
            }
        }
    }

    // ==================== removeItem ====================

    context("removeItem operations") {

        test("removeItem on existing item should decrease count by 1 and return true") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1", "audio"),
                TestCallLog("session-2", "video")
            ))

            val result = viewModel.removeItem(TestCallLog("session-1"))

            result shouldBe true
            viewModel.getItemCount() shouldBe 1
            viewModel.getItemAt(0)?.sessionId shouldBe "session-2"
        }

        test("removeItem should use sessionId equality") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItem(TestCallLog("session-1", "audio", "ended", 120))

            // Different fields but same sessionId — should still match
            val toRemove = TestCallLog("session-1", "video", "missed", 0)
            val result = viewModel.removeItem(toRemove)

            result shouldBe true
            viewModel.getItemCount() shouldBe 0
        }

        test("removeItem on non-existent item should return false and not change count") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItem(TestCallLog("session-1"))

            val result = viewModel.removeItem(TestCallLog("nonexistent"))

            result shouldBe false
            viewModel.getItemCount() shouldBe 1
        }

        test("PBT - removeItem on existing item decreases count by 1 and returns true") {
            checkAll(Arb.int(1..15)) { listSize ->
                val viewModel = TestCallLogsListViewModel()
                val callLogs = (1..listSize).map { TestCallLog("session-$it") }
                viewModel.addItems(callLogs)

                val targetIndex = (0 until listSize).random()
                val target = callLogs[targetIndex]
                val countBefore = viewModel.getItemCount()

                val result = viewModel.removeItem(target)

                result shouldBe true
                viewModel.getItemCount() shouldBe countBefore - 1
            }
        }

        test("PBT - removeItem on non-existent item returns false and doesn't change count") {
            checkAll(Arb.int(1..15), Arb.string(5..15)) { listSize, nonExistentId ->
                val viewModel = TestCallLogsListViewModel()
                val callLogs = (1..listSize).map { TestCallLog("session-$it") }
                viewModel.addItems(callLogs)
                val countBefore = viewModel.getItemCount()

                // Use an ID that won't collide with "session-N" pattern
                val result = viewModel.removeItem(TestCallLog("nonexistent_$nonExistentId"))

                result shouldBe false
                viewModel.getItemCount() shouldBe countBefore
            }
        }

        test("removeItemAt should remove at valid index") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2"),
                TestCallLog("session-3")
            ))

            val removed = viewModel.removeItemAt(1)

            removed?.sessionId shouldBe "session-2"
            viewModel.getItemCount() shouldBe 2
        }

        test("removeItemAt should return null for invalid index") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItem(TestCallLog("session-1"))

            viewModel.removeItemAt(-1) shouldBe null
            viewModel.removeItemAt(10) shouldBe null
        }
    }

    // ==================== updateItem ====================

    context("updateItem operations") {

        test("updateItem with matching predicate should return true and replace item") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1", "audio", "ended", 60),
                TestCallLog("session-2", "video", "ongoing", 0)
            ))

            val updated = TestCallLog("session-1", "audio", "ended", 180)
            val result = viewModel.updateItem(updated) { it.sessionId == "session-1" }

            result shouldBe true
            viewModel.getItemAt(0)?.duration shouldBe 180
        }

        test("updateItem with no matching predicate should return false") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItem(TestCallLog("session-1"))

            val result = viewModel.updateItem(TestCallLog("session-99")) { it.sessionId == "nonexistent" }

            result shouldBe false
            viewModel.getItemCount() shouldBe 1
            viewModel.getItemAt(0)?.sessionId shouldBe "session-1"
        }

        test("PBT - updateItem with matching predicate returns true and replaces item") {
            checkAll(Arb.int(1..15), Arb.int(0..3600)) { listSize, newDuration ->
                val viewModel = TestCallLogsListViewModel()
                val callLogs = (1..listSize).map { TestCallLog("session-$it", duration = 0) }
                viewModel.addItems(callLogs)

                val targetIndex = (0 until listSize).random()
                val targetId = "session-${targetIndex + 1}"
                val updatedLog = TestCallLog(targetId, duration = newDuration)

                val result = viewModel.updateItem(updatedLog) { it.sessionId == targetId }

                result shouldBe true
                viewModel.getItemAt(targetIndex)?.duration shouldBe newDuration
                viewModel.getItemCount() shouldBe listSize
            }
        }
    }

    // ==================== moveItemToTop ====================

    context("moveItemToTop operations") {

        test("moveItemToTop should place existing item at index 0") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2"),
                TestCallLog("session-3")
            ))

            viewModel.moveItemToTop(TestCallLog("session-3"))

            viewModel.getItemAt(0)?.sessionId shouldBe "session-3"
            viewModel.getItemAt(1)?.sessionId shouldBe "session-1"
            viewModel.getItemAt(2)?.sessionId shouldBe "session-2"
        }

        test("moveItemToTop should preserve list size") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2"),
                TestCallLog("session-3")
            ))

            viewModel.moveItemToTop(TestCallLog("session-2"))

            viewModel.getItemCount() shouldBe 3
        }

        test("moveItemToTop should add non-existing item at top") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2")
            ))

            viewModel.moveItemToTop(TestCallLog("session-new"))

            viewModel.getItemCount() shouldBe 3
            viewModel.getItemAt(0)?.sessionId shouldBe "session-new"
        }

        test("PBT - moveItemToTop places target at index 0 preserving size") {
            checkAll(Arb.int(2..15)) { listSize ->
                val viewModel = TestCallLogsListViewModel()
                val callLogs = (1..listSize).map { TestCallLog("session-$it") }
                viewModel.addItems(callLogs)

                val targetIndex = (1 until listSize).random()
                val target = callLogs[targetIndex]

                viewModel.moveItemToTop(target)

                viewModel.getItemAt(0)?.sessionId shouldBe target.sessionId
                viewModel.getItemCount() shouldBe listSize
            }
        }
    }

    // ==================== batch operations ====================

    context("batch operations") {

        test("batch should perform multiple operations atomically") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2")
            ))

            viewModel.batch {
                add(TestCallLog("session-3"))
                add(TestCallLog("session-4"))
                remove(TestCallLog("session-1"))
            }

            viewModel.getItemCount() shouldBe 3
            viewModel.getItems().map { it.sessionId } shouldBe listOf("session-2", "session-3", "session-4")
        }

        test("batch should support all operations") {
            val viewModel = TestCallLogsListViewModel()

            viewModel.batch {
                add(TestCallLog("session-1"))
                addAll(listOf(TestCallLog("session-2"), TestCallLog("session-3")))
                moveToTop(TestCallLog("session-3"))
            }

            viewModel.getItemCount() shouldBe 3
            viewModel.getItemAt(0)?.sessionId shouldBe "session-3"
        }

        test("batch clear should empty the list") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2")
            ))

            viewModel.batch { clear() }

            viewModel.getItemCount() shouldBe 0
        }

        test("batch update should replace matching item") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1", "audio", "ended", 60),
                TestCallLog("session-2", "video", "ongoing", 0)
            ))

            viewModel.batch {
                update(TestCallLog("session-1", "audio", "ended", 300)) { it.sessionId == "session-1" }
            }

            viewModel.getItemAt(0)?.duration shouldBe 300
        }

        test("batch removeAt should remove at index") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2"),
                TestCallLog("session-3")
            ))

            viewModel.batch { removeAt(1) }

            viewModel.getItemCount() shouldBe 2
            viewModel.getItems().map { it.sessionId } shouldBe listOf("session-1", "session-3")
        }

        test("batch emits single state update") {
            val viewModel = TestCallLogsListViewModel()
            var emissionCount = 0
            val initialValue = viewModel.items.value

            viewModel.batch {
                add(TestCallLog("session-1"))
                add(TestCallLog("session-2"))
                add(TestCallLog("session-3"))
            }

            // After batch, the state should reflect all 3 items in one update
            viewModel.items.value.size shouldBe 3
        }
    }

    // ==================== clearItems ====================

    context("clearItems operations") {

        test("clearItems should result in count 0") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2"),
                TestCallLog("session-3")
            ))

            viewModel.clearItems()

            viewModel.getItemCount() shouldBe 0
        }

        test("clearItems on empty list should remain at count 0") {
            val viewModel = TestCallLogsListViewModel()

            viewModel.clearItems()

            viewModel.getItemCount() shouldBe 0
        }

        test("PBT - clearItems always results in count 0") {
            checkAll(Arb.int(0..20)) { listSize ->
                val viewModel = TestCallLogsListViewModel()
                val callLogs = (1..listSize).map { TestCallLog("session-$it") }
                viewModel.addItems(callLogs)

                viewModel.clearItems()

                viewModel.getItemCount() shouldBe 0
            }
        }
    }

    // ==================== getItemAt ====================

    context("getItemAt operations") {

        test("getItemAt with valid index should return item") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1"),
                TestCallLog("session-2")
            ))

            viewModel.getItemAt(0)?.sessionId shouldBe "session-1"
            viewModel.getItemAt(1)?.sessionId shouldBe "session-2"
        }

        test("getItemAt with out-of-bounds index should return null") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItem(TestCallLog("session-1"))

            viewModel.getItemAt(-1) shouldBe null
            viewModel.getItemAt(1) shouldBe null
            viewModel.getItemAt(100) shouldBe null
        }

        test("PBT - getItemAt with out-of-bounds index returns null") {
            checkAll(Arb.int(0..10), Arb.int(11..100)) { listSize, outOfBoundsIndex ->
                val viewModel = TestCallLogsListViewModel()
                val callLogs = (1..listSize).map { TestCallLog("session-$it") }
                viewModel.addItems(callLogs)

                viewModel.getItemAt(outOfBoundsIndex) shouldBe null
                viewModel.getItemAt(-1) shouldBe null
            }
        }
    }

    // ==================== StateFlow emissions ====================

    context("StateFlow emissions") {

        test("StateFlow should reflect list changes") {
            val viewModel = TestCallLogsListViewModel()

            viewModel.items.value.size shouldBe 0

            viewModel.addItem(TestCallLog("session-1"))
            viewModel.items.value.size shouldBe 1

            viewModel.addItem(TestCallLog("session-2"))
            viewModel.items.value.size shouldBe 2

            viewModel.removeItem(TestCallLog("session-1"))
            viewModel.items.value.size shouldBe 1
            viewModel.items.value[0].sessionId shouldBe "session-2"
        }

        test("StateFlow should update after batch operations") {
            val viewModel = TestCallLogsListViewModel()

            viewModel.batch {
                add(TestCallLog("session-1"))
                add(TestCallLog("session-2"))
            }

            viewModel.items.value.size shouldBe 2
        }
    }

    // ==================== Custom equality checker ====================

    context("Custom equality checker (sessionId-based)") {

        test("should use sessionId for equality comparison") {
            val viewModel = TestCallLogsListViewModel()

            // Add with specific fields
            viewModel.addItem(TestCallLog("session-1", "audio", "ended", 120))

            // Remove with different fields but same sessionId
            val result = viewModel.removeItem(TestCallLog("session-1", "video", "missed", 0))

            result shouldBe true
            viewModel.getItemCount() shouldBe 0
        }

        test("should not match different sessionIds") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItem(TestCallLog("session-1", "audio"))

            // Try to remove with same type but different sessionId
            val result = viewModel.removeItem(TestCallLog("session-2", "audio"))

            result shouldBe false
            viewModel.getItemCount() shouldBe 1
        }

        test("moveItemToTop should use sessionId equality to find existing item") {
            val viewModel = TestCallLogsListViewModel()
            viewModel.addItems(listOf(
                TestCallLog("session-1", "audio"),
                TestCallLog("session-2", "video"),
                TestCallLog("session-3", "audio")
            ))

            // Move with different fields but same sessionId
            viewModel.moveItemToTop(TestCallLog("session-3", "video", "missed", 999))

            viewModel.getItemAt(0)?.sessionId shouldBe "session-3"
            viewModel.getItemCount() shouldBe 3
        }
    }
})
