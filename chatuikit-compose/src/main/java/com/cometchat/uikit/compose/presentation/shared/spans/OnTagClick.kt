package com.cometchat.uikit.compose.presentation.shared.spans

import android.content.Context

/**
 * Interface for handling tag click events.
 *
 * @param T The type of data associated with the tag
 */
fun interface OnTagClick<T> {
    /**
     * Called when a tag is clicked.
     *
     * @param context The context in which the click occurred
     * @param data The data associated with the clicked tag
     */
    fun onClick(context: Context, data: T)
}
