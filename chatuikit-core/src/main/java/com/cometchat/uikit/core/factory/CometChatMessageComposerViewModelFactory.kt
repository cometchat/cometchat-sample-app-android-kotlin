package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.MessageComposerDataSourceImpl
import com.cometchat.uikit.core.data.repository.MessageComposerRepositoryImpl
import com.cometchat.uikit.core.domain.repository.MessageComposerRepository
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel

/**
 * Factory for creating CometChatMessageComposerViewModel instances.
 * Wires up the repository and use cases with proper dependency injection.
 * 
 * This factory is used by both chatuikit-jetpack (Compose) and chatuikit-kotlin (Views)
 * to create ViewModel instances with the correct dependencies.
 * 
 * @param repository The repository for sending messages. Defaults to a new instance
 *                   with the default data source implementation.
 * @param enableListeners Whether to enable CometChat listeners. Set to false for testing
 *                        to avoid side effects from real-time updates.
 */
class CometChatMessageComposerViewModelFactory(
    private val repository: MessageComposerRepository = MessageComposerRepositoryImpl(
        MessageComposerDataSourceImpl()
    ),
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatMessageComposerViewModel::class.java)) {
            val sendTextMessageUseCase = SendTextMessageUseCase(repository)
            val sendMediaMessageUseCase = SendMediaMessageUseCase(repository)
            val sendCustomMessageUseCase = SendCustomMessageUseCase(repository)
            val editMessageUseCase = EditMessageUseCase(repository)
            return CometChatMessageComposerViewModel(
                sendTextMessageUseCase = sendTextMessageUseCase,
                sendMediaMessageUseCase = sendMediaMessageUseCase,
                sendCustomMessageUseCase = sendCustomMessageUseCase,
                editMessageUseCase = editMessageUseCase,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
