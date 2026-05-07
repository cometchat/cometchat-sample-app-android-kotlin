package com.cometchat.uikit.core.viewmodel.listoperations

import com.cometchat.uikit.core.viewmodel.ListOperationsDelegate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy

/**
 * Tests for ListOperationsDelegate — the core list manipulation engine.
 *
 * This is a pure Kotlin class with no Android/SDK dependencies.
 * Tests cover: core operations, custom equality, batch operations, and debounce.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ListOperationsDelegateTest : FunSpec({

    // ==================== Core Operations (Immediate Mode) ====================

    test("addItem should append item to end of list") {
        val stateFlow = MutableStateFlow<List<String>>(emptyList())
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.addItem("A")
        delegate.addItem("B")

        stateFlow.value shouldBe listOf("A", "B")
    }

    test("addItems should append all items to end of list") {
        val stateFlow = MutableStateFlow(listOf("A"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.addItems(listOf("B", "C", "D"))

        stateFlow.value shouldBe listOf("A", "B", "C", "D")
    }

    test("removeItem should remove first matching item and return true") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        val removed = delegate.removeItem("B")

        removed shouldBe true
        stateFlow.value shouldBe listOf("A", "C")
    }

    test("removeItem should return false when item not found") {
        val stateFlow = MutableStateFlow(listOf("A", "B"))
        val delegate = ListOperationsDelegate(stateFlow)

        val removed = delegate.removeItem("Z")

        removed shouldBe false
        stateFlow.value shouldBe listOf("A", "B")
    }

    test("removeItemAt should remove item at index and return it") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        val removed = delegate.removeItemAt(1)

        removed shouldBe "B"
        stateFlow.value shouldBe listOf("A", "C")
    }

    test("removeItemAt should return null for out-of-bounds index") {
        val stateFlow = MutableStateFlow(listOf("A"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.removeItemAt(-1) shouldBe null
        delegate.removeItemAt(5) shouldBe null
        stateFlow.value shouldBe listOf("A")
    }

    test("updateItem should replace first matching item and return true") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        val updated = delegate.updateItem("B_UPDATED") { it == "B" }

        updated shouldBe true
        stateFlow.value shouldBe listOf("A", "B_UPDATED", "C")
    }

    test("updateItem should return false when no match found") {
        val stateFlow = MutableStateFlow(listOf("A", "B"))
        val delegate = ListOperationsDelegate(stateFlow)

        val updated = delegate.updateItem("Z") { it == "NOT_FOUND" }

        updated shouldBe false
        stateFlow.value shouldBe listOf("A", "B")
    }

    test("clearItems should empty the list") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.clearItems()

        stateFlow.value shouldBe emptyList()
    }

    test("getItems should return a copy of the current list") {
        val stateFlow = MutableStateFlow(listOf("A", "B"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.getItems() shouldBe listOf("A", "B")
    }

    test("getItemAt should return item at valid index") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.getItemAt(0) shouldBe "A"
        delegate.getItemAt(2) shouldBe "C"
    }

    test("getItemAt should return null for out-of-bounds index") {
        val stateFlow = MutableStateFlow(listOf("A"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.getItemAt(5) shouldBe null
        delegate.getItemAt(-1) shouldBe null
    }

    test("getItemCount should return current list size") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.getItemCount() shouldBe 3
    }

    // ==================== moveItemToTop ====================

    test("moveItemToTop should move existing item to index 0") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.moveItemToTop("C")

        stateFlow.value shouldBe listOf("C", "A", "B")
    }

    test("moveItemToTop should insert non-existing item at index 0") {
        val stateFlow = MutableStateFlow(listOf("A", "B"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.moveItemToTop("Z")

        stateFlow.value shouldBe listOf("Z", "A", "B")
    }

    test("moveItemToTop on already-top item should not change order") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.moveItemToTop("A")

        stateFlow.value shouldBe listOf("A", "B", "C")
    }

    // ==================== Custom Equality Checker ====================

    test("removeItem should use custom equalityChecker for matching") {
        data class Item(val id: String, val name: String)

        val stateFlow = MutableStateFlow(listOf(
            Item("1", "Alice"),
            Item("2", "Bob")
        ))
        val delegate = ListOperationsDelegate(
            stateFlow = stateFlow,
            equalityChecker = { a, b -> a.id == b.id }
        )

        // Remove by ID, even though name differs
        val removed = delegate.removeItem(Item("1", "DIFFERENT"))

        removed shouldBe true
        stateFlow.value shouldBe listOf(Item("2", "Bob"))
    }

    test("moveItemToTop should use custom equalityChecker") {
        data class Item(val id: String, val value: Int)

        val stateFlow = MutableStateFlow(listOf(
            Item("a", 1), Item("b", 2), Item("c", 3)
        ))
        val delegate = ListOperationsDelegate(
            stateFlow = stateFlow,
            equalityChecker = { a, b -> a.id == b.id }
        )

        // Move by ID match — the new item replaces the old one at top
        delegate.moveItemToTop(Item("c", 99))

        stateFlow.value shouldBe listOf(Item("c", 99), Item("a", 1), Item("b", 2))
    }

    // ==================== Batch Operations ====================

    test("batch should apply multiple operations and emit once") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.batch {
            add("D")
            remove("A")
            moveToTop("C")
        }

        stateFlow.value shouldBe listOf("C", "B", "D")
    }

    test("batch add and addAll should work correctly") {
        val stateFlow = MutableStateFlow<List<String>>(emptyList())
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.batch {
            add("A")
            addAll(listOf("B", "C"))
            add("D")
        }

        stateFlow.value shouldBe listOf("A", "B", "C", "D")
    }

    test("batch removeAt should remove at correct index") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C", "D"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.batch {
            removeAt(1) // removes "B"
            removeAt(1) // removes "C" (now at index 1)
        }

        stateFlow.value shouldBe listOf("A", "D")
    }

    test("batch update should replace matching item") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.batch {
            update("B_NEW") { it == "B" }
        }

        stateFlow.value shouldBe listOf("A", "B_NEW", "C")
    }

    test("batch clear should empty the list") {
        val stateFlow = MutableStateFlow(listOf("A", "B", "C"))
        val delegate = ListOperationsDelegate(stateFlow)

        delegate.batch {
            clear()
            add("X") // add after clear
        }

        stateFlow.value shouldBe listOf("X")
    }

    // ==================== Debounce Mode ====================

    test("debounced updates should not emit immediately") {
        val testScope = TestScope()
        val stateFlow = MutableStateFlow<List<String>>(emptyList())
        val delegate = ListOperationsDelegate(
            stateFlow = stateFlow,
            debounceMs = 200L,
            scope = testScope
        )

        delegate.addItem("A")
        delegate.addItem("B")

        // Before debounce fires
        stateFlow.value shouldBe emptyList()

        // After debounce
        testScope.advanceTimeBy(250)
        stateFlow.value shouldBe listOf("A", "B")

        delegate.cancel()
    }

    test("flush should force immediate emission of pending updates") {
        val testScope = TestScope()
        val stateFlow = MutableStateFlow<List<String>>(emptyList())
        val delegate = ListOperationsDelegate(
            stateFlow = stateFlow,
            debounceMs = 500L,
            scope = testScope
        )

        delegate.addItem("A")
        delegate.addItem("B")

        // Force flush before debounce
        delegate.flush()

        stateFlow.value shouldBe listOf("A", "B")

        delegate.cancel()
    }

    test("getItems should auto-flush when debounce is active") {
        val testScope = TestScope()
        val stateFlow = MutableStateFlow<List<String>>(emptyList())
        val delegate = ListOperationsDelegate(
            stateFlow = stateFlow,
            debounceMs = 500L,
            scope = testScope
        )

        delegate.addItem("A")

        // getItems triggers flush
        val items = delegate.getItems()
        items shouldBe listOf("A")

        delegate.cancel()
    }

    test("cancel should discard pending updates") {
        val testScope = TestScope()
        val stateFlow = MutableStateFlow<List<String>>(emptyList())
        val delegate = ListOperationsDelegate(
            stateFlow = stateFlow,
            debounceMs = 500L,
            scope = testScope
        )

        delegate.addItem("A")
        delegate.cancel()

        testScope.advanceTimeBy(600)

        stateFlow.value shouldBe emptyList()
    }
})
