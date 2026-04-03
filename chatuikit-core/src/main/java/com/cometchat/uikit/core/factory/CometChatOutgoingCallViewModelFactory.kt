package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.viewmodel.CometChatOutgoingCallViewModel

/**
 * Factory for creating [CometChatOutgoingCallViewModel] instances.
 * 
 * This factory is used by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * to create ViewModel instances with the correct configuration.
 * 
 * @param enableListeners Whether to enable CometChat call listeners. Set to false for testing
 *                        to avoid side effects from real-time call events.
 * 
 * Validates: Requirements 13.4, 13.5, 13.6
 */
class CometChatOutgoingCallViewModelFactory(
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatOutgoingCallViewModel::class.java)) {
            return CometChatOutgoingCallViewModel(
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
