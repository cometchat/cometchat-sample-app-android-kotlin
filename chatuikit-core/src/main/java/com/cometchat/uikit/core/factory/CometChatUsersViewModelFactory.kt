package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.UsersDataSourceImpl
import com.cometchat.uikit.core.data.repository.UsersRepositoryImpl
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel

/**
 * Factory for creating CometChatUsersViewModel with dependencies.
 * Enables dependency injection of custom repositories.
 *
 * @param repository The repository to use for data operations.
 *                   Defaults to UsersRepositoryImpl with UsersDataSourceImpl.
 * @param enableListeners Whether to enable CometChat listeners. Set to false for previews/testing.
 *                        Defaults to true for production use.
 */
class CometChatUsersViewModelFactory(
    private val repository: UsersRepository = UsersRepositoryImpl(
        UsersDataSourceImpl()
    ),
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatUsersViewModel::class.java)) {
            // Create all use cases with the provided repository
            val fetchUsersUseCase = FetchUsersUseCase(repository)
            val searchUsersUseCase = SearchUsersUseCase(repository)

            return CometChatUsersViewModel(
                fetchUsersUseCase = fetchUsersUseCase,
                searchUsersUseCase = searchUsersUseCase,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
