package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.viewmodel.CometChatThreadHeaderViewModel

/**
 * Factory for creating CometChatThreadHeaderViewModel instances.
 *
 * This factory is used by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * to create ViewModel instances.
 *
 * @param enableListeners Whether to enable CometChat listeners. Set to false for testing
 *                        to avoid side effects from real-time updates.
 */
class CometChatThreadHeaderViewModelFactory(
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatThreadHeaderViewModel::class.java)) {
            return CometChatThreadHeaderViewModel(enableListeners = enableListeners) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
