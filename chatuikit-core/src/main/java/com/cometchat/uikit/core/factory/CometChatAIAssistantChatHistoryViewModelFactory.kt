package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.viewmodel.CometChatAIAssistantChatHistoryViewModel

/**
 * Factory for creating [CometChatAIAssistantChatHistoryViewModel] instances.
 * Since the ChatHistory ViewModel uses direct CometChat SDK calls (no repository pattern),
 * this factory only passes the [enableListeners] flag.
 *
 * @param enableListeners Whether to enable CometChat listeners. Set to false for previews/testing.
 *                        Defaults to true for production use.
 */
class CometChatAIAssistantChatHistoryViewModelFactory(
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatAIAssistantChatHistoryViewModel::class.java)) {
            return CometChatAIAssistantChatHistoryViewModel(
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
