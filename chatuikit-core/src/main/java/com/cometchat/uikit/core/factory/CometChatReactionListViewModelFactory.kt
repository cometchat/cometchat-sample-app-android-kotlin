package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.ReactionListDataSourceImpl
import com.cometchat.uikit.core.data.repository.ReactionListRepositoryImpl
import com.cometchat.uikit.core.domain.repository.ReactionListRepository
import com.cometchat.uikit.core.domain.usecase.FetchReactionsUseCase
import com.cometchat.uikit.core.domain.usecase.RemoveReactionUseCase
import com.cometchat.uikit.core.viewmodel.CometChatReactionListViewModel

/**
 * Factory for creating CometChatReactionListViewModel with dependencies.
 * Enables dependency injection of custom repositories.
 *
 * @param repository The repository to use for data operations.
 *                   Defaults to ReactionListRepositoryImpl with ReactionListDataSourceImpl.
 * @param enableListeners Whether to enable CometChat listeners. Set to false for previews/testing.
 *                        Defaults to true for production use.
 */
class CometChatReactionListViewModelFactory(
    private val repository: ReactionListRepository = ReactionListRepositoryImpl(
        ReactionListDataSourceImpl()
    ),
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatReactionListViewModel::class.java)) {
            // Create all use cases with the provided repository
            val fetchReactionsUseCase = FetchReactionsUseCase(repository)
            val removeReactionUseCase = RemoveReactionUseCase(repository)

            return CometChatReactionListViewModel(
                fetchReactionsUseCase = fetchReactionsUseCase,
                removeReactionUseCase = removeReactionUseCase,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
