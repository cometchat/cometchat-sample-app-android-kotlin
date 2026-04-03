package com.cometchat.uikit.kotlin.presentation.shared.permission.listener

import androidx.activity.result.ActivityResult

/**
 * Listener interface for receiving activity results.
 */
fun interface ActivityResultListener {
    /**
     * Called when an activity result is received.
     * @param result The activity result containing result code and data
     */
    fun onActivityResult(result: ActivityResult)
}
