package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.PollDataSourceImpl
import com.cometchat.uikit.core.data.repository.PollRepositoryImpl
import com.cometchat.uikit.core.domain.repository.PollRepository
import com.cometchat.uikit.core.domain.usecase.CreatePollUseCase
import com.cometchat.uikit.core.viewmodel.CometChatCreatePollViewModel

/**
 * Factory for creating CometChatCreatePollViewModel with dependencies.
 * Enables dependency injection of custom repositories.
 *
 * @param repository The repository to use for data operations.
 *                   Defaults to PollRepositoryImpl with PollDataSourceImpl.
 */
class CometChatCreatePollViewModelFactory(
    private val repository: PollRepository = PollRepositoryImpl(
        PollDataSourceImpl()
    )
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatCreatePollViewModel::class.java)) {
            // Create use case with the provided repository
            val createPollUseCase = CreatePollUseCase(repository)

            return CometChatCreatePollViewModel(
                createPollUseCase = createPollUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
