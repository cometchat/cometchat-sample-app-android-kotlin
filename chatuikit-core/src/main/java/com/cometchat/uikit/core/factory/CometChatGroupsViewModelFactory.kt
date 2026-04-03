package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.GroupsDataSourceImpl
import com.cometchat.uikit.core.data.repository.GroupsRepositoryImpl
import com.cometchat.uikit.core.domain.repository.GroupsRepository
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel

/**
 * Factory for creating CometChatGroupsViewModel with dependencies.
 * Enables dependency injection of custom repositories.
 *
 * @param repository The repository to use for data operations.
 *                   Defaults to GroupsRepositoryImpl with GroupsDataSourceImpl.
 * @param enableListeners Whether to enable CometChat listeners. Set to false for previews/testing.
 *                        Defaults to true for production use.
 */
class CometChatGroupsViewModelFactory(
    private val repository: GroupsRepository = GroupsRepositoryImpl(
        GroupsDataSourceImpl()
    ),
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatGroupsViewModel::class.java)) {
            // Create all use cases with the provided repository
            val fetchGroupsUseCase = FetchGroupsUseCase(repository)
            val joinGroupUseCase = JoinGroupUseCase(repository)

            return CometChatGroupsViewModel(
                fetchGroupsUseCase = fetchGroupsUseCase,
                joinGroupUseCase = joinGroupUseCase,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
