package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.viewmodel.CometChatIncomingCallViewModel

/**
 * Factory for creating [CometChatIncomingCallViewModel] instances.
 * 
 * This factory is used by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * to create ViewModel instances with the correct configuration.
 * 
 * @param enableListeners Whether to enable CometChat call listeners. Set to false for testing
 *                        to avoid side effects from real-time call events.
 * 
 * Validates: Requirements 13.1, 13.2, 13.3
 */
class CometChatIncomingCallViewModelFactory(
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatIncomingCallViewModel::class.java)) {
            return CometChatIncomingCallViewModel(
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
