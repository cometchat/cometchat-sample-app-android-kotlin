package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.MessageInformationDataSourceImpl
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.data.repository.MessageInformationRepositoryImpl
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel

/**
 * Factory for creating CometChatMessageInformationViewModel instances.
 * Provides default implementations of dependencies if not specified.
 *
 * @param repository Optional custom repository implementation
 * @param eventListener Optional custom event listener implementation
 * @param enableListeners Whether to enable real-time listeners (default true)
 */
class CometChatMessageInformationViewModelFactory(
    private val repository: MessageInformationRepository? = null,
    private val eventListener: MessageReceiptEventListener? = null,
    private val enableListeners: Boolean = true
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatMessageInformationViewModel::class.java)) {
            val dataSource = MessageInformationDataSourceImpl()
            val repo = repository ?: MessageInformationRepositoryImpl(dataSource)
            val listener = eventListener ?: MessageReceiptEventListener()

            return CometChatMessageInformationViewModel(
                repository = repo,
                eventListener = listener,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
