package com.cometchat.uikit.kotlin.presentation.shared.permission.builder

import android.content.Intent
import com.cometchat.uikit.kotlin.presentation.shared.permission.listener.ActivityResultListener

/**
 * Builder interface for handling activity results.
 * 
 * This interface provides a fluent API for launching intents and receiving
 * activity results without requiring the caller to register ActivityResultLauncher
 * in their onCreate lifecycle.
 * 
 * Usage:
 * ```kotlin
 * CometChatPermissionHandler.withContext(context)
 *     .registerListener { result ->
 *         if (result.resultCode == Activity.RESULT_OK) {
 *             // Handle result
 *         }
 *     }
 *     .withIntent(intent)
 *     .launch()
 * ```
 */
interface ActivityResultHandlerBuilder {
    
    /**
     * Sets the intent to launch.
     * @param intent The intent to launch for result
     * @return This builder for chaining
     */
    fun withIntent(intent: Intent): ActivityResultHandlerBuilder
    
    /**
     * Registers a listener for activity results.
     * @param listener The listener to receive activity results
     * @return This builder for chaining
     */
    fun registerListener(listener: ActivityResultListener): ActivityResultHandlerBuilder
    
    /**
     * Launches the intent and waits for result.
     * The result will be delivered to the registered listener.
     */
    fun launch()
}
