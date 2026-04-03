package com.cometchat.uikit.kotlin.shared.spans

import android.content.Context

/**
 * Interface for handling tag click events.
 */
fun interface OnTagClick<T> {
    fun onClick(context: Context, item: T)
}
