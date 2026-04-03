package com.cometchat.uikit.compose.presentation.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * A generic ViewModel class for managing a list of items.
 * This ViewModel provides methods for manipulating and managing the list of items.
 */
open class ListBase<T> {

    private val _items = MutableLiveData<List<T>>()
    val items: LiveData<List<T>> get() = _items

    /**
     * Adds an item to the list.
     *
     * @param item The item to add to the list.
     */
    fun add(item: T) {
        val currentList = _items.value ?: emptyList()
        _items.value = currentList + item
    }

    /**
     * Removes a specified item from the list.
     *
     * @param item The item to remove from the list.
     */
    fun remove(item: T) {
        val currentList = _items.value ?: emptyList()
        _items.value = currentList - item
    }

    /**
     * Adds a list of items to the current list.
     *
     * @param items The list of items to add to the list.
     */
    fun addAll(items: List<T>) {
        val currentList = _items.value ?: emptyList()
        _items.value = currentList + items
    }

    /**
     * Removes an item at a specific index from the list.
     *
     * @param index The index of the item to remove.
     */
    fun removeAt(index: Int) {
        val currentList = _items.value ?: emptyList()
        if (index in currentList.indices) {
            _items.value = currentList - currentList[index]
        }
    }

    /**
     * Updates an item at a specific index in the list.
     *
     * @param index The index of the item to update.
     * @param newItem The new item to replace the old item at the specified index.
     */
    fun update(index: Int, newItem: T) {
        val currentList = _items.value ?: emptyList()
        if (index in currentList.indices) {
            val updatedList = currentList.toMutableList()
            updatedList[index] = newItem
            _items.value = updatedList
        }
    }

    /**
     * Inserts an item at a specific index in the list.
     *
     * @param index The index where the new item should be inserted.
     * @param newItem The item to insert at the specified index.
     */
    fun insertAt(index: Int, newItem: T) {
        val currentList = _items.value ?: emptyList()
        if (index in currentList.indices) {
            val updatedList = currentList.toMutableList()
            updatedList.add(index, newItem)
            _items.value = updatedList
        }
    }

    /**
     * Adds a list of items at the beginning of the current list.
     *
     * @param items The list of items to add at the start.
     */
    fun addAllAtStart(items: List<T>) {
        val currentList = _items.value ?: emptyList()
        _items.value = items + currentList
    }

    /**
     * Swaps two items at the specified indices in the list.
     *
     * @param index1 The first index of the item to swap.
     * @param index2 The second index of the item to swap.
     */
    fun swapItems(index1: Int, index2: Int) {
        val currentList = _items.value ?: emptyList()
        if (index1 in currentList.indices && index2 in currentList.indices) {
            val updatedList = currentList.toMutableList()
            val temp = updatedList[index1]
            updatedList[index1] = updatedList[index2]
            updatedList[index2] = temp
            _items.value = updatedList
        }
    }

    /**
     * Replaces all the items in the list with a new list.
     *
     * @param newItems The new list of items.
     */
    fun replaceAll(newItems: List<T>) {
        _items.value = newItems
    }

    /**
     * Finds the first occurrence of an item that matches the given predicate.
     *
     * @param predicate The condition to match the item.
     * @return The first item that matches the condition, or `null` if none match.
     */
    fun findFirst(predicate: (T) -> Boolean): T? {
        return _items.value?.find(predicate)
    }

    /**
     * Filters the list based on the given condition.
     *
     * @param predicate The condition to filter the items.
     */
    fun filterItems(predicate: (T) -> Boolean) {
        val currentList = _items.value ?: emptyList()
        _items.value = currentList.filter(predicate)
    }

    /**
     * Checks if the list contains a specified item.
     *
     * @param item The item to check for.
     * @return `true` if the list contains the item, `false` otherwise.
     */
    fun containsItem(item: T): Boolean {
        return _items.value?.contains(item) == true
    }

    /**
     * Removes items that match a specified condition.
     *
     * @param predicate The condition to identify items to remove.
     */
    fun removeIf(predicate: (T) -> Boolean) {
        val currentList = _items.value ?: emptyList()
        _items.value = currentList.filterNot(predicate)
    }

    /**
     * Reverses the order of items in the list.
     */
    fun reverse() {
        val currentList = _items.value ?: emptyList()
        _items.value = currentList.reversed()
    }

    /**
     * Shuffles the items in the list randomly.
     */
    fun shuffle() {
        val currentList = _items.value ?: emptyList()
        _items.value = currentList.shuffled()
    }

    /**
     * Clears items from the list if they match a specific condition.
     *
     * @param predicate The condition to identify items to clear.
     */
    fun clearIf(predicate: (T) -> Boolean) {
        val currentList = _items.value ?: emptyList()
        _items.value = currentList.filterNot(predicate)
    }

    /**
     * Returns a sublist from the list, based on the given index range.
     *
     * @param fromIndex The starting index of the sublist.
     * @param toIndex The ending index of the sublist.
     */
    fun subList(fromIndex: Int, toIndex: Int) {
        val currentList = _items.value ?: emptyList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            _items.value = currentList.subList(fromIndex, toIndex)
        }
    }

    /**
     * Replaces the first occurrence of a specified item with a new item.
     *
     * @param oldItem The item to be replaced.
     * @param newItem The new item to replace the old one.
     */
    fun replaceFirst(oldItem: T, newItem: T) {
        val currentList = _items.value ?: emptyList()
        val updatedList = currentList.toMutableList()
        val index = updatedList.indexOf(oldItem)
        if (index >= 0) {
            updatedList[index] = newItem
            _items.value = updatedList
        }
    }

    /**
     * Adds an item only if it doesn't already exist in the list.
     *
     * @param item The item to add if it's not already present.
     */
    fun addUnique(item: T) {
        val currentList = _items.value ?: emptyList()
        if (item !in currentList) {
            _items.value = currentList + item
        }
    }

    /**
     * Retrieves the item at a specific index or returns `null` if the index is out of bounds.
     *
     * @param index The index of the item.
     * @return The item at the specified index, or `null` if out of bounds.
     */
    fun getItemAt(index: Int): T? {
        return _items.value?.getOrNull(index)
    }

    /**
     * Updates multiple items within a specified index range.
     *
     * @param startIndex The starting index for the update.
     * @param endIndex The ending index for the update.
     * @param newItems The list of new items to update in the range.
     */
    fun updateRange(startIndex: Int, endIndex: Int, newItems: List<T>) {
        val currentList = _items.value ?: emptyList()
        if (startIndex in currentList.indices && endIndex in currentList.indices) {
            val updatedList = currentList.toMutableList()
            for (i in startIndex..endIndex) {
                if (i - startIndex < newItems.size) {
                    updatedList[i] = newItems[i - startIndex]
                }
            }
            _items.value = updatedList
        }
    }

    /**
     * Adds an item to the list only if the list is not empty.
     *
     * @param item The item to add if the list is not empty.
     */
    fun addIfNotEmpty(item: T) {
        val currentList = _items.value ?: emptyList()
        if (currentList.isNotEmpty()) {
            _items.value = currentList + item
        }
    }

    /**
     * Clears the list by setting it to an empty list.
     */
    fun clear() {
        _items.value = emptyList()
    }
}