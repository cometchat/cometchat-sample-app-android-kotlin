package com.cometchat.uikit.core.viewmodel

/**
 * Generic interface defining standard list manipulation operations.
 * ViewModels implementing this interface provide a consistent API for
 * list management that clients can intercept and customize.
 *
 * This interface enables:
 * - Consistent list operations across all list-based ViewModels
 * - Client interception by overriding methods in extended ViewModels
 * - Batch operations for efficient handling of high-frequency updates
 *
 * @param T The type of items in the list
 */
interface ListOperations<T> {

    /**
     * Adds a single item to the list.
     * Default behavior: appends to the end of the list.
     *
     * @param item The item to add
     */
    fun addItem(item: T)

    /**
     * Adds multiple items to the list.
     * Default behavior: appends all items to the end of the list.
     *
     * @param items The items to add
     */
    fun addItems(items: List<T>)

    /**
     * Removes a single item from the list.
     * Uses the configured equality checker for comparison.
     *
     * @param item The item to remove
     * @return true if item was removed, false if not found
     */
    fun removeItem(item: T): Boolean

    /**
     * Removes an item at the specified index.
     *
     * @param index The index of the item to remove
     * @return The removed item, or null if index is out of bounds
     */
    fun removeItemAt(index: Int): T?

    /**
     * Updates an item in the list that matches the predicate.
     * Replaces the first matching item with the new item.
     *
     * @param item The new item to replace with
     * @param predicate Function to find the item to update
     * @return true if an item was updated, false if no match found
     */
    fun updateItem(item: T, predicate: (T) -> Boolean): Boolean

    /**
     * Removes all items from the list.
     */
    fun clearItems()

    /**
     * Returns a copy of all items in the list.
     *
     * @return Immutable list of all items
     */
    fun getItems(): List<T>

    /**
     * Returns the item at the specified index.
     *
     * @param index The index of the item
     * @return The item at the index, or null if out of bounds
     */
    fun getItemAt(index: Int): T?

    /**
     * Returns the number of items in the list.
     *
     * @return The item count
     */
    fun getItemCount(): Int

    /**
     * Moves an item to the top (index 0) of the list.
     * If the item doesn't exist, it is added at the top.
     *
     * @param item The item to move to top
     */
    fun moveItemToTop(item: T)

    /**
     * Performs multiple operations in a single batch, emitting only once.
     * Critical for performance when receiving many updates rapidly
     * (e.g., 20 messages from listener within a second).
     *
     * Example usage:
     * ```kotlin
     * viewModel.batch {
     *     add(item1)
     *     add(item2)
     *     remove(oldItem)
     *     moveToTop(importantItem)
     * }
     * ```
     *
     * @param operations Lambda that performs multiple list operations
     */
    fun batch(operations: ListOperationsBatchScope<T>.() -> Unit)
}

/**
 * Scope interface for batch operations.
 * Provides methods to modify the list within a batch context.
 * All operations are collected and applied as a single update.
 */
interface ListOperationsBatchScope<T> {

    /**
     * Adds a single item to the list within the batch.
     *
     * @param item The item to add
     */
    fun add(item: T)

    /**
     * Adds multiple items to the list within the batch.
     *
     * @param items The items to add
     */
    fun addAll(items: List<T>)

    /**
     * Removes a single item from the list within the batch.
     *
     * @param item The item to remove
     */
    fun remove(item: T)

    /**
     * Removes an item at the specified index within the batch.
     *
     * @param index The index of the item to remove
     */
    fun removeAt(index: Int)

    /**
     * Updates an item matching the predicate within the batch.
     *
     * @param item The new item to replace with
     * @param predicate Function to find the item to update
     */
    fun update(item: T, predicate: (T) -> Boolean)

    /**
     * Removes all items from the list within the batch.
     */
    fun clear()

    /**
     * Moves an item to the top of the list within the batch.
     *
     * @param item The item to move to top
     */
    fun moveToTop(item: T)
}
