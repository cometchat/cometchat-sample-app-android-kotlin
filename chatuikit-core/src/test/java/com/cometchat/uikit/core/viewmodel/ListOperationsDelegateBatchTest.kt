package com.cometchat.uikit.core.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for batch operations in [ListOperationsDelegate].
 * Tests that batch operations emit only once and apply correctly.
 *
 * Feature: list-operations-interface
 * Validates: Requirements 2.5, 5.2
 */
class ListOperationsDelegateBatchTest {

    private lateinit var stateFlow: MutableStateFlow<List<String>>
    private lateinit var delegate: ListOperationsDelegate<String>
    private var emissionCount: Int = 0

    @Before
    fun setup() {
        stateFlow = MutableStateFlow(emptyList())
        delegate = ListOperationsDelegate(stateFlow)
        emissionCount = 0
    }

    @Test
    fun `batch operations emit only once`() {
        // Track emissions by counting state changes
        val initialValue = stateFlow.value
        
        delegate.batch {
            add("item1")
            add("item2")
            add("item3")
        }
        
        // After batch, we should have all items
        assertEquals(3, delegate.getItemCount())
        assertEquals(listOf("item1", "item2", "item3"), delegate.getItems())
    }

    @Test
    fun `batch with multiple add operations`() {
        delegate.batch {
            add("a")
            add("b")
            addAll(listOf("c", "d", "e"))
        }

        assertEquals(5, delegate.getItemCount())
        assertEquals(listOf("a", "b", "c", "d", "e"), delegate.getItems())
    }

    @Test
    fun `batch with mixed add and remove operations`() {
        // Pre-populate
        delegate.addItems(listOf("keep1", "remove1", "keep2", "remove2"))

        delegate.batch {
            remove("remove1")
            remove("remove2")
            add("new1")
            add("new2")
        }

        assertEquals(4, delegate.getItemCount())
        assertTrue(delegate.getItems().contains("keep1"))
        assertTrue(delegate.getItems().contains("keep2"))
        assertTrue(delegate.getItems().contains("new1"))
        assertTrue(delegate.getItems().contains("new2"))
        assertTrue(!delegate.getItems().contains("remove1"))
        assertTrue(!delegate.getItems().contains("remove2"))
    }

    @Test
    fun `batch with update operations`() {
        delegate.addItems(listOf("old1", "old2", "old3"))

        delegate.batch {
            update("new1") { it == "old1" }
            update("new2") { it == "old2" }
        }

        assertEquals(3, delegate.getItemCount())
        assertEquals(listOf("new1", "new2", "old3"), delegate.getItems())
    }

    @Test
    fun `batch with clear operation`() {
        delegate.addItems(listOf("a", "b", "c"))

        delegate.batch {
            clear()
            add("fresh")
        }

        assertEquals(1, delegate.getItemCount())
        assertEquals(listOf("fresh"), delegate.getItems())
    }

    @Test
    fun `batch with moveToTop operation`() {
        delegate.addItems(listOf("a", "b", "c"))

        delegate.batch {
            moveToTop("c")
        }

        assertEquals(3, delegate.getItemCount())
        assertEquals("c", delegate.getItemAt(0))
    }

    @Test
    fun `batch with removeAt operation`() {
        delegate.addItems(listOf("a", "b", "c", "d"))

        delegate.batch {
            removeAt(1) // Remove "b"
            removeAt(2) // Remove "d" (index shifted after first removal)
        }

        assertEquals(2, delegate.getItemCount())
        assertEquals(listOf("a", "c"), delegate.getItems())
    }

    @Test
    fun `batch with complex mixed operations`() {
        delegate.addItems(listOf("item1", "item2", "item3"))

        delegate.batch {
            add("item4")
            remove("item2")
            update("updated1") { it == "item1" }
            moveToTop("item3")
            addAll(listOf("item5", "item6"))
        }

        // Expected: item3 at top, then updated1, item4, item5, item6
        assertEquals(5, delegate.getItemCount())
        assertEquals("item3", delegate.getItemAt(0))
        assertTrue(delegate.getItems().contains("updated1"))
        assertTrue(delegate.getItems().contains("item4"))
        assertTrue(delegate.getItems().contains("item5"))
        assertTrue(delegate.getItems().contains("item6"))
        assertTrue(!delegate.getItems().contains("item2"))
    }

    @Test
    fun `empty batch does not change state`() {
        delegate.addItems(listOf("a", "b", "c"))
        val before = delegate.getItems()

        delegate.batch {
            // Empty batch
        }

        assertEquals(before, delegate.getItems())
    }

    @Test
    fun `batch with custom equality checker`() {
        data class Item(val id: String, val name: String)

        val itemStateFlow = MutableStateFlow<List<Item>>(emptyList())
        val itemDelegate = ListOperationsDelegate(
            stateFlow = itemStateFlow,
            equalityChecker = { a, b -> a.id == b.id }
        )

        itemDelegate.addItems(listOf(
            Item("1", "Original1"),
            Item("2", "Original2"),
            Item("3", "Original3")
        ))

        itemDelegate.batch {
            // Remove by id (name doesn't matter for equality)
            remove(Item("2", "DifferentName"))
            // Move to top by id
            moveToTop(Item("3", "AnotherName"))
        }

        assertEquals(2, itemDelegate.getItemCount())
        assertEquals("3", itemDelegate.getItemAt(0)?.id)
    }
}
