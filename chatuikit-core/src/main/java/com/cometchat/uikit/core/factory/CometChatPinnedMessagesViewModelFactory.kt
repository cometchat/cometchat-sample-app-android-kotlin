package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.viewmodel.CometChatPinnedMessagesViewModel

/**
 * Factory for [CometChatPinnedMessagesViewModel].
 *
 * @param enableListeners Whether to enable live upkeep. Set false for previews/testing.
 */
class CometChatPinnedMessagesViewModelFactory(
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatPinnedMessagesViewModel::class.java)) {
            return CometChatPinnedMessagesViewModel(enableListeners = enableListeners) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
