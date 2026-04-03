package com.cometchat.uikit.core.viewmodel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Property-based tests for [ListOperationsDelegate].
 * Each test validates a correctness property from the design document.
 *
 * Feature: list-operations-interface
 * Validates: Requirements 2.3, 2.5
 */
class ListOperationsDelegatePropertyTest : FunSpec({

    // Feature: list-operations-interface, Property 1: Add Item Invariant
    // *For any* list and any item, after calling addItem(item), the list size
    // SHALL increase by exactly 1 AND the item SHALL be present in the list.
    test("Property 1: addItem increases size by 1 and item is present") {
        checkAll(100, Arb.list(Arb.string(), 0..50), Arb.string()) { initialList, newItem ->
            val stateFlow = MutableStateFlow(initialList)
            val delegate = ListOperationsDelegate(stateFlow)
            val sizeBefore = delegate.getItemCount()

            delegate.addItem(newItem)

            delegate.getItemCount() shouldBe sizeBefore + 1
            delegate.getItems() shouldContain newItem
        }
    }

    // Feature: list-operations-interface, Property 2: Add Items Invariant
    // *For any* list and any collection of items, after calling addItems(items),
    // the list size SHALL increase by exactly items.size AND all items SHALL be present.
    test("Property 2: addItems increases size by items.size and all items are present") {
        checkAll(100, Arb.list(Arb.string(), 0..30), Arb.list(Arb.string(), 0..20)) { initialList, newItems ->
            val stateFlow = MutableStateFlow(initialList)
            val delegate = ListOperationsDelegate(stateFlow)
            val sizeBefore = delegate.getItemCount()

            delegate.addItems(newItems)

            delegate.getItemCount() shouldBe sizeBefore + newItems.size
            newItems.forEach { item ->
                delegate.getItems() shouldContain item
            }
        }
    }

    // Feature: list-operations-interface, Property 3: Remove Item Invariant
    // *For any* list containing an item, after calling removeItem(item),
    // the list size SHALL decrease by exactly 1 AND the occurrence count SHALL decrease by 1.
    test("Property 3: removeItem decreases size by 1 when item exists") {
        checkAll(100, Arb.list(Arb.string(), 1..50)) { items ->
            val stateFlow = MutableStateFlow(items)
            val delegate = ListOperationsDelegate(stateFlow)
            val itemToRemove = items.random()
            val sizeBefore = delegate.getItemCount()
            val countBefore = items.count { it == itemToRemove }

            val result = delegate.removeItem(itemToRemove)

            result shouldBe true
            delegate.getItemCount() shouldBe sizeBefore - 1
            // If there were duplicates, item may still be present (count decreased by 1)
            val countAfter = delegate.getItems().count { it == itemToRemove }
            countAfter shouldBe countBefore - 1
        }
    }

    // Feature: list-operations-interface, Property 3 (continued): Remove non-existent item
    // If the item was not in the list, the list SHALL remain unchanged.
    test("Property 3: removeItem returns false and list unchanged when item not found") {
        checkAll(100, Arb.list(Arb.string(), 0..50), Arb.string()) { items, itemToRemove ->
            // Ensure item is not in list
            val filteredItems = items.filter { it != itemToRemove }
            val stateFlow = MutableStateFlow(filteredItems)
            val delegate = ListOperationsDelegate(stateFlow)
            val sizeBefore = delegate.getItemCount()

            val result = delegate.removeItem(itemToRemove)

            result shouldBe false
            delegate.getItemCount() shouldBe sizeBefore
        }
    }

    // Feature: list-operations-interface, Property 4: Remove Item At Invariant
    // *For any* list and valid index, after calling removeItemAt(index),
    // the list size SHALL decrease by exactly 1.
    test("Property 4: removeItemAt decreases size by 1 for valid index") {
        checkAll(100, Arb.list(Arb.string(), 1..50)) { items ->
            val stateFlow = MutableStateFlow(items)
            val delegate = ListOperationsDelegate(stateFlow)
            val validIndex = (0 until items.size).random()
            val expectedItem = items[validIndex]
            val sizeBefore = delegate.getItemCount()

            val removed = delegate.removeItemAt(validIndex)

            removed shouldBe expectedItem
            delegate.getItemCount() shouldBe sizeBefore - 1
        }
    }

    // Feature: list-operations-interface, Property 4 (continued): Invalid index
    // For invalid indices, the list SHALL remain unchanged and null SHALL be returned.
    test("Property 4: removeItemAt returns null for invalid index") {
        checkAll(100, Arb.list(Arb.string(), 0..50), Arb.int(-100..-1)) { items, negativeIndex ->
            val stateFlow = MutableStateFlow(items)
            val delegate = ListOperationsDelegate(stateFlow)
            val sizeBefore = delegate.getItemCount()

            val removed = delegate.removeItemAt(negativeIndex)

            removed shouldBe null
            delegate.getItemCount() shouldBe sizeBefore
        }
    }


    // Feature: list-operations-interface, Property 5: Update Item Invariant
    // *For any* list and any item matching a predicate, after calling updateItem,
    // the list size SHALL remain unchanged AND the item SHALL be replaced.
    test("Property 5: updateItem keeps size unchanged and replaces matching item") {
        checkAll(100, Arb.list(Arb.string(), 1..50), Arb.string()) { items, newValue ->
            val stateFlow = MutableStateFlow(items)
            val delegate = ListOperationsDelegate(stateFlow)
            val itemToUpdate = items.random()
            val sizeBefore = delegate.getItemCount()

            val result = delegate.updateItem(newValue) { it == itemToUpdate }

            result shouldBe true
            delegate.getItemCount() shouldBe sizeBefore
            delegate.getItems() shouldContain newValue
        }
    }

    // Feature: list-operations-interface, Property 5 (continued): No match
    test("Property 5: updateItem returns false when no match found") {
        checkAll(100, Arb.list(Arb.string(), 0..50), Arb.string()) { items, newValue ->
            val stateFlow = MutableStateFlow(items)
            val delegate = ListOperationsDelegate(stateFlow)
            val sizeBefore = delegate.getItemCount()

            val result = delegate.updateItem(newValue) { false } // Never matches

            result shouldBe false
            delegate.getItemCount() shouldBe sizeBefore
        }
    }

    // Feature: list-operations-interface, Property 6: Clear Items Invariant
    // *For any* list, after calling clearItems(), the list SHALL be empty (size = 0).
    test("Property 6: clearItems empties the list") {
        checkAll(100, Arb.list(Arb.string(), 0..100)) { items ->
            val stateFlow = MutableStateFlow(items)
            val delegate = ListOperationsDelegate(stateFlow)

            delegate.clearItems()

            delegate.getItemCount() shouldBe 0
            delegate.getItems() shouldBe emptyList()
        }
    }

    // Feature: list-operations-interface, Property 7: Move Item To Top Invariant
    // *For any* list and any item that exists, after calling moveItemToTop(item),
    // list size SHALL remain unchanged AND item SHALL be at index 0.
    test("Property 7: moveItemToTop places existing item at index 0") {
        checkAll(100, Arb.list(Arb.string(), 2..50)) { items ->
            val stateFlow = MutableStateFlow(items)
            val delegate = ListOperationsDelegate(stateFlow)
            val itemToMove = items.random()
            val sizeBefore = delegate.getItemCount()

            delegate.moveItemToTop(itemToMove)

            delegate.getItemCount() shouldBe sizeBefore
            delegate.getItemAt(0) shouldBe itemToMove
        }
    }

    // Feature: list-operations-interface, Property 7 (continued): Non-existent item
    // If the item did not exist, list size SHALL increase by 1 AND item SHALL be at index 0.
    test("Property 7: moveItemToTop adds non-existent item at index 0") {
        checkAll(100, Arb.list(Arb.string(), 0..50), Arb.string()) { items, newItem ->
            // Ensure item is not in list
            val filteredItems = items.filter { it != newItem }
            val stateFlow = MutableStateFlow(filteredItems)
            val delegate = ListOperationsDelegate(stateFlow)
            val sizeBefore = delegate.getItemCount()

            delegate.moveItemToTop(newItem)

            delegate.getItemCount() shouldBe sizeBefore + 1
            delegate.getItemAt(0) shouldBe newItem
        }
    }

    // Feature: list-operations-interface, Property 8: Get Operations Consistency
    // getItems() SHALL return a list equal to the current state
    // getItemCount() SHALL return the size of getItems()
    // getItemAt(i) SHALL return getItems()[i] for valid indices, null otherwise
    test("Property 8: get operations are consistent with each other") {
        checkAll(100, Arb.list(Arb.string(), 0..50)) { items ->
            val stateFlow = MutableStateFlow(items)
            val delegate = ListOperationsDelegate(stateFlow)

            val retrievedItems = delegate.getItems()
            val count = delegate.getItemCount()

            count shouldBe retrievedItems.size
            retrievedItems.forEachIndexed { index, item ->
                delegate.getItemAt(index) shouldBe item
            }
            // Out of bounds should return null
            delegate.getItemAt(-1) shouldBe null
            delegate.getItemAt(count) shouldBe null
        }
    }
})
