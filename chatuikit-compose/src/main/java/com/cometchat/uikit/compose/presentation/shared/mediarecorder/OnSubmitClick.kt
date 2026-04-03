package com.cometchat.uikit.compose.presentation.shared.mediarecorder

import android.content.Context
import java.io.File

/**
 * Interface for handling submit click events in Jetpack Compose.
 */
fun interface OnSubmitClick {
    /**
     * Called when a submit action is triggered.
     *
     * @param file    the file associated with the submit action
     * @param context the context in which the submit action occurs
     */
    fun onClick(file: File?, context: Context)
}

/**
 * Typealias for lambda-based submit click handling
 */
typealias OnSubmitClickListener = (file: File?, context: Context) -> Unit