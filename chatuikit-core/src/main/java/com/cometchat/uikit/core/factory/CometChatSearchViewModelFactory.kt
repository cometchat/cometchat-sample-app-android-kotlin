package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.SearchDataSourceImpl
import com.cometchat.uikit.core.data.repository.SearchRepositoryImpl
import com.cometchat.uikit.core.domain.repository.SearchRepository
import com.cometchat.uikit.core.domain.usecase.FetchConversationsUseCase
import com.cometchat.uikit.core.domain.usecase.FetchMessagesUseCase
import com.cometchat.uikit.core.viewmodel.CometChatSearchViewModel

/**
 * Factory for creating CometChatSearchViewModel with dependencies.
 * Enables dependency injection of custom repositories.
 *
 * @param repository The repository to use for data operations.
 *                   Defaults to SearchRepositoryImpl with SearchDataSourceImpl.
 */
class CometChatSearchViewModelFactory(
    private val repository: SearchRepository = SearchRepositoryImpl(
        SearchDataSourceImpl()
    )
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatSearchViewModel::class.java)) {
            // Create all use cases with the provided repository
            val fetchConversationsUseCase = FetchConversationsUseCase(repository)
            val fetchMessagesUseCase = FetchMessagesUseCase(repository)

            return CometChatSearchViewModel(
                fetchConversationsUseCase = fetchConversationsUseCase,
                fetchMessagesUseCase = fetchMessagesUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
