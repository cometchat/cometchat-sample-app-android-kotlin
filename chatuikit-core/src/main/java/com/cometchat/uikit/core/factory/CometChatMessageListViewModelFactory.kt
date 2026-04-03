package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.repository.MessageListRepositoryImpl
import com.cometchat.uikit.core.domain.repository.MessageListRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel

/**
 * Factory for creating CometChatMessageListViewModel instances.
 * Wires up the repository with proper dependency injection.
 * 
 * This factory is used by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * to create ViewModel instances with the correct dependencies.
 * 
 * @param repository The repository for message operations. Defaults to a new instance
 *                   of MessageListRepositoryImpl.
 * @param enableListeners Whether to enable CometChat listeners. Set to false for testing
 *                        to avoid side effects from real-time updates.
 */
class CometChatMessageListViewModelFactory(
    private val repository: MessageListRepository = MessageListRepositoryImpl(),
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatMessageListViewModel::class.java)) {
            return CometChatMessageListViewModel(
                repository = repository,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
