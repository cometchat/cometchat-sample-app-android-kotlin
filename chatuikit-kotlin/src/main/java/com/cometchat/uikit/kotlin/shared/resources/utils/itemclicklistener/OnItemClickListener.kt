package com.cometchat.uikit.kotlin.shared.resources.utils.itemclicklistener

/**
 * Generic interface for handling item click events in lists.
 * 
 * @param T The type of item being clicked
 */
interface OnItemClickListener<T> {
    /**
     * Called when an item is clicked.
     * 
     * @param item The clicked item
     * @param position The position of the item in the list
     */
    fun OnItemClick(item: T, position: Int)

    /**
     * Called when an item is long-clicked.
     * 
     * @param item The long-clicked item
     * @param position The position of the item in the list
     */
    fun OnItemLongClick(item: T, position: Int) {}
}
