package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.CallButtonsDataSourceImpl
import com.cometchat.uikit.core.data.repository.CallButtonsRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel

/**
 * Factory for creating [CometChatCallButtonsViewModel] instances with proper dependency injection.
 */
class CometChatCallButtonsViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatCallButtonsViewModel::class.java)) {
            val dataSource = CallButtonsDataSourceImpl()
            val repository = CallButtonsRepositoryImpl(dataSource)
            val initiateUserCallUseCase = InitiateUserCallUseCase(repository)
            val startGroupCallUseCase = StartGroupCallUseCase(repository)

            return CometChatCallButtonsViewModel(
                initiateUserCallUseCase,
                startGroupCallUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
