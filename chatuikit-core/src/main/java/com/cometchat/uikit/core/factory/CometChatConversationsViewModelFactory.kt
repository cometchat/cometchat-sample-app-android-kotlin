package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.ConversationListDataSourceImpl
import com.cometchat.uikit.core.data.repository.ConversationListRepositoryImpl
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel

/**
 * Factory for creating CometChatConversationsViewModel with dependencies.
 * Enables dependency injection of custom repositories.
 *
 * @param repository The repository to use for data operations.
 *                   Defaults to ConversationListRepositoryImpl with ConversationListDataSourceImpl.
 * @param enableListeners Whether to enable CometChat listeners. Set to false for previews/testing.
 *                        Defaults to true for production use.
 */
class CometChatConversationsViewModelFactory(
    private val repository: ConversationListRepository = ConversationListRepositoryImpl(
        ConversationListDataSourceImpl()
    ),
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatConversationsViewModel::class.java)) {
            // Create all use cases with the provided repository
            val getConversationListUseCase = GetConversationListUseCase(repository)
            val deleteConversationUseCase = DeleteConversationUseCase(repository)
            val refreshConversationListUseCase = RefreshConversationListUseCase(repository)

            return CometChatConversationsViewModel(
                getConversationListUseCase = getConversationListUseCase,
                deleteConversationUseCase = deleteConversationUseCase,
                refreshConversationListUseCase = refreshConversationListUseCase,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}