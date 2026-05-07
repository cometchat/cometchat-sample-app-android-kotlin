package com.cometchat.uikit.compose.preview.presentation.viewmodels

import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.preview.data.repository.PreviewMessageHeaderRepository
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.core.domain.usecase.GetGroupUseCase
import com.cometchat.uikit.core.domain.usecase.GetUserUseCase
import com.cometchat.uikit.core.viewmodel.CometChatMessageHeaderViewModel

/**
 * Factory object for creating preview-specific MessageHeader ViewModels.
 * These ViewModels are designed for Compose previews and testing different UI states.
 */
object PreviewMessageHeaderViewModelFactory {

    /**
     * Creates a ViewModel for a user conversation header.
     */
    fun createUserHeaderViewModel(
        user: User = PreviewMockData.createMockUser(name = "Alice Smith")
    ): CometChatMessageHeaderViewModel {
        val repository = PreviewMessageHeaderRepository(mockUser = user)
        return CometChatMessageHeaderViewModel(
            getUserUseCase = GetUserUseCase(repository),
            getGroupUseCase = GetGroupUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel for a group conversation header.
     */
    fun createGroupHeaderViewModel(
        group: Group = PreviewMockData.createMockGroup(
            name = "Engineering Team",
            membersCount = 12
        )
    ): CometChatMessageHeaderViewModel {
        val repository = PreviewMessageHeaderRepository(mockGroup = group)
        return CometChatMessageHeaderViewModel(
            getUserUseCase = GetUserUseCase(repository),
            getGroupUseCase = GetGroupUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that simulates an error state.
     */
    fun createErrorViewModel(): CometChatMessageHeaderViewModel {
        val repository = PreviewMessageHeaderRepository(simulateError = true)
        return CometChatMessageHeaderViewModel(
            getUserUseCase = GetUserUseCase(repository),
            getGroupUseCase = GetGroupUseCase(repository),
            enableListeners = false
        )
    }
}
