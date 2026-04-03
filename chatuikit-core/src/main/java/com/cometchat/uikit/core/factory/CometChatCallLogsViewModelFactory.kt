package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.CallLogsDataSourceImpl
import com.cometchat.uikit.core.data.repository.CallLogsRepositoryImpl
import com.cometchat.uikit.core.domain.repository.CallLogsRepository
import com.cometchat.uikit.core.domain.usecase.FetchCallLogsUseCase
import com.cometchat.uikit.core.domain.usecase.InitiateCallUseCase
import com.cometchat.uikit.core.viewmodel.CometChatCallLogsViewModel

/**
 * Factory for creating [CometChatCallLogsViewModel] instances.
 * Wires up all dependencies: DataSource → Repository → UseCases → ViewModel.
 * 
 * @param repository Optional custom repository (uses default if not provided)
 * @param enableListeners Whether to enable CometChat listeners (default: true)
 */
class CometChatCallLogsViewModelFactory(
    private val repository: CallLogsRepository? = null,
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatCallLogsViewModel::class.java)) {
            val callLogsRepository = repository ?: CallLogsRepositoryImpl(
                CallLogsDataSourceImpl()
            )
            val fetchCallLogsUseCase = FetchCallLogsUseCase(callLogsRepository)
            val initiateCallUseCase = InitiateCallUseCase()
            
            return CometChatCallLogsViewModel(
                fetchCallLogsUseCase = fetchCallLogsUseCase,
                initiateCallUseCase = initiateCallUseCase,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
