package com.cometchat.uikit.core.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Delegate class that handles the internal implementation of list operations.
 * This class encapsulates all list manipulation logic and is used internally
 * by ViewModels that implement [ListOperations] interface.
 *
 * Supports batch operations and debouncing for high-frequency updates.
 *
 * @param T The type of items in the list
 * @param stateFlow The MutableStateFlow that holds the list state
 * @param equalityChecker Optional custom equality checker for items (defaults to equals())
 * @param debounceMs Debounce delay in milliseconds (0 = no debounce, immediate updates)
 * @param scope CoroutineScope for debounce operations (defaults to Dispatchers.Default)
 */
class ListOperationsDelegate<T>(
    private val stateFlow: MutableStateFlow<List<T>>,
    private val equalityChecker: (T, T) -> Boolean = { a, b -> a == b },
    private val debounceMs: Long = 0L,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val pendingUpdates = mutableListOf<(List<T>) -> List<T>>()
    private var debounceJob: Job? = null
    private val lock = Any()

    // ==================== Core Operations (Task 2.1) ====================

    /**
     * Adds a single item to the end of the list.
     *
     * @param item The item to add
     */
    fun addItem(item: T) {
        applyUpdate { it + item }
    }

    /**
     * Adds multiple items to the end of the list.
     *
     * @param items The items to add
     */
    fun addItems(items: List<T>) {
        applyUpdate { it + items }
    }

    /**
     * Removes the first occurrence of an item from the list.
     *
     * @param item The item to remove
     * @return true if item was found and removed, false otherwise
     */
    fun removeItem(item: T): Boolean {
        val currentList = stateFlow.value
        val index = currentList.indexOfFirst { equalityChecker(it, item) }
        return if (index >= 0) {
            applyUpdate { list -> list.filterIndexed { i, _ -> i != index } }
            true
        } else {
            false
        }
    }

    /**
     * Removes an item at the specified index.
     *
     * @param index The index of the item to remove
     * @return The removed item, or null if index is out of bounds
     */
    fun removeItemAt(index: Int): T? {
        val currentList = stateFlow.value
        return if (index in currentList.indices) {
            val removed = currentList[index]
            applyUpdate { list -> list.filterIndexed { i, _ -> i != index } }
            removed
        } else {
            null
        }
    }


    // ==================== Remaining Operations (Task 2.2) ====================

    /**
     * Updates the first item matching the predicate with the new item.
     *
     * @param item The new item to replace with
     * @param predicate Function to find the item to update
     * @return true if an item was found and updated, false otherwise
     */
    fun updateItem(item: T, predicate: (T) -> Boolean): Boolean {
        val currentList = stateFlow.value
        val index = currentList.indexOfFirst(predicate)
        return if (index >= 0) {
            applyUpdate { list ->
                list.mapIndexed { i, existing ->
                    if (i == index) item else existing
                }
            }
            true
        } else {
            false
        }
    }

    /**
     * Removes all items from the list.
     */
    fun clearItems() {
        applyUpdate { emptyList() }
    }

    /**
     * Returns a copy of all items in the list.
     * If debouncing is enabled, flushes pending updates first.
     *
     * @return Immutable list of all items
     */
    fun getItems(): List<T> {
        if (debounceMs > 0) flush()
        return stateFlow.value.toList()
    }

    /**
     * Returns the item at the specified index, or null if out of bounds.
     *
     * @param index The index of the item
     * @return The item at the index, or null if out of bounds
     */
    fun getItemAt(index: Int): T? {
        if (debounceMs > 0) flush()
        return stateFlow.value.getOrNull(index)
    }

    /**
     * Returns the number of items in the list.
     *
     * @return The item count
     */
    fun getItemCount(): Int {
        if (debounceMs > 0) flush()
        return stateFlow.value.size
    }

    /**
     * Moves an item to the top of the list.
     * If the item exists, it's moved to index 0.
     * If the item doesn't exist, it's added at index 0.
     *
     * @param item The item to move to top
     */
    fun moveItemToTop(item: T) {
        applyUpdate { currentList ->
            val index = currentList.indexOfFirst { equalityChecker(it, item) }
            if (index >= 0) {
                buildList {
                    add(item)
                    currentList.forEachIndexed { i, existing ->
                        if (i != index) add(existing)
                    }
                }
            } else {
                buildList {
                    add(item)
                    addAll(currentList)
                }
            }
        }
    }


    // ==================== Batch Operations (Task 2.3) ====================

    /**
     * Performs multiple operations in a single batch, emitting only once.
     * Critical for performance when receiving many updates rapidly.
     *
     * @param operations Lambda that performs multiple list operations on BatchScope
     */
    fun batch(operations: BatchScope.() -> Unit) {
        val batchScope = BatchScope(stateFlow.value)
        operations(batchScope)
        stateFlow.value = batchScope.result
    }

    /**
     * Scope for batch operations. All operations modify a local copy,
     * and the result is emitted only once when batch completes.
     * Implements [ListOperationsBatchScope] for type-safe batch operations.
     */
    inner class BatchScope(initialList: List<T>) : ListOperationsBatchScope<T> {
        private var currentList = initialList
        
        /**
         * The final result after all batch operations.
         */
        val result: List<T> get() = currentList

        override fun add(item: T) {
            currentList = currentList + item
        }

        override fun addAll(items: List<T>) {
            currentList = currentList + items
        }

        override fun remove(item: T) {
            val index = currentList.indexOfFirst { equalityChecker(it, item) }
            if (index >= 0) {
                currentList = currentList.filterIndexed { i, _ -> i != index }
            }
        }

        override fun removeAt(index: Int) {
            if (index in currentList.indices) {
                currentList = currentList.filterIndexed { i, _ -> i != index }
            }
        }

        override fun update(item: T, predicate: (T) -> Boolean) {
            val index = currentList.indexOfFirst(predicate)
            if (index >= 0) {
                currentList = currentList.mapIndexed { i, existing ->
                    if (i == index) item else existing
                }
            }
        }

        override fun clear() {
            currentList = emptyList()
        }

        override fun moveToTop(item: T) {
            val index = currentList.indexOfFirst { equalityChecker(it, item) }
            currentList = if (index >= 0) {
                buildList {
                    add(item)
                    currentList.forEachIndexed { i, existing ->
                        if (i != index) add(existing)
                    }
                }
            } else {
                buildList {
                    add(item)
                    addAll(currentList)
                }
            }
        }
    }


    // ==================== Debounce Mechanism (Task 2.4) ====================

    /**
     * Applies an update to the list, either immediately or debounced.
     * When debounceMs > 0, updates are collected and applied after the delay.
     *
     * @param transform Function that transforms the current list to the new list
     */
    private fun applyUpdate(transform: (List<T>) -> List<T>) {
        if (debounceMs <= 0) {
            // Immediate update - no debouncing
            stateFlow.value = transform(stateFlow.value)
        } else {
            // Debounced update - collect updates and apply after delay
            synchronized(lock) {
                pendingUpdates.add(transform)
            }
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(debounceMs)
                flushPendingUpdates()
            }
        }
    }

    /**
     * Flushes all pending updates in a single emission.
     * Called automatically after debounce delay or manually via [flush].
     */
    private fun flushPendingUpdates() {
        synchronized(lock) {
            if (pendingUpdates.isEmpty()) return
            var result = stateFlow.value
            pendingUpdates.forEach { transform ->
                result = transform(result)
            }
            pendingUpdates.clear()
            stateFlow.value = result
        }
    }

    /**
     * Forces immediate flush of any pending debounced updates.
     * Useful when you need the latest state immediately.
     */
    fun flush() {
        debounceJob?.cancel()
        flushPendingUpdates()
    }

    /**
     * Cancels any pending debounce operations.
     * Call this when the ViewModel is cleared to prevent memory leaks.
     */
    fun cancel() {
        debounceJob?.cancel()
        synchronized(lock) {
            pendingUpdates.clear()
        }
    }
}
