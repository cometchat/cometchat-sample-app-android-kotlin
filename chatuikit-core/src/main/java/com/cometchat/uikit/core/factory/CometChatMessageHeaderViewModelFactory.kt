package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.MessageHeaderDataSourceImpl
import com.cometchat.uikit.core.data.repository.MessageHeaderRepositoryImpl
import com.cometchat.uikit.core.domain.repository.MessageHeaderRepository
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel

/**
 * Factory for creating CometChatMessageHeaderViewModel instances.
 * Wires up the repository and use cases with proper dependency injection.
 * 
 * This factory is used by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * to create ViewModel instances with the correct dependencies.
 * 
 * @param repository The repository for fetching user/group data. Defaults to a new instance
 *                   with the default data source implementation.
 * @param enableListeners Whether to enable CometChat listeners. Set to false for testing
 *                        to avoid side effects from real-time updates.
 */
class CometChatMessageHeaderViewModelFactory(
    private val repository: MessageHeaderRepository = MessageHeaderRepositoryImpl(
        MessageHeaderDataSourceImpl()
    ),
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatMessageHeaderViewModel::class.java)) {
            val getUserUseCase = GetUserUseCase(repository)
            val getGroupUseCase = GetGroupUseCase(repository)
            return CometChatMessageHeaderViewModel(
                getUserUseCase = getUserUseCase,
                getGroupUseCase = getGroupUseCase,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
