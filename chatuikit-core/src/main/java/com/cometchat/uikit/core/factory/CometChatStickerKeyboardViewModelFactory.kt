package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.uikit.core.data.datasource.StickerDataSourceImpl
import com.cometchat.uikit.core.data.repository.StickerRepositoryImpl
import com.cometchat.uikit.core.domain.repository.StickerRepository
import com.cometchat.uikit.core.domain.usecase.GetStickersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatStickerKeyboardViewModel

/**
 * Factory for creating CometChatStickerKeyboardViewModel with dependencies.
 * Enables dependency injection of custom repositories.
 *
 * @param repository The repository to use for data operations.
 *                   Defaults to StickerRepositoryImpl with StickerDataSourceImpl.
 */
class CometChatStickerKeyboardViewModelFactory(
    private val repository: StickerRepository = StickerRepositoryImpl(
        StickerDataSourceImpl()
    )
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatStickerKeyboardViewModel::class.java)) {
            // Create use case with the provided repository
            val getStickersUseCase = GetStickersUseCase(repository)

            return CometChatStickerKeyboardViewModel(
                getStickersUseCase = getStickersUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
