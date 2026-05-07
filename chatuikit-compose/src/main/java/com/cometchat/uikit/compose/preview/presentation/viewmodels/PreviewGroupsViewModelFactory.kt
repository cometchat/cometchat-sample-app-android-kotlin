package com.cometchat.uikit.compose.preview.presentation.viewmodels

import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.preview.data.repository.PreviewGroupsRepository
import com.cometchat.uikit.compose.preview.domain.PreviewMockData
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel

/**
 * Factory object for creating preview-specific Groups ViewModels.
 * These ViewModels are designed for Compose previews and testing different UI states.
 */
object PreviewGroupsViewModelFactory {

    /**
     * Creates a ViewModel with default sample groups.
     */
    fun createDefaultViewModel(): CometChatGroupsViewModel {
        val repository = PreviewGroupsRepository()
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that shows the empty state.
     */
    fun createEmptyStateViewModel(): CometChatGroupsViewModel {
        val repository = PreviewGroupsRepository(simulateEmpty = true)
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that shows the error state.
     */
    fun createErrorStateViewModel(): CometChatGroupsViewModel {
        val repository = PreviewGroupsRepository(simulateError = true)
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel with custom groups.
     */
    fun createCustomGroupsViewModel(groups: List<Group>): CometChatGroupsViewModel {
        val repository = PreviewGroupsRepository(initialGroups = groups)
        return CometChatGroupsViewModel(
            fetchGroupsUseCase = FetchGroupsUseCase(repository),
            joinGroupUseCase = JoinGroupUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel with only public groups.
     */
    fun createPublicGroupsViewModel(): CometChatGroupsViewModel {
        return createCustomGroupsViewModel(PreviewMockData.createPublicGroups())
    }

    /**
     * Creates a ViewModel with only private groups.
     */
    fun createPrivateGroupsViewModel(): CometChatGroupsViewModel {
        return createCustomGroupsViewModel(PreviewMockData.createPrivateGroups())
    }

    /**
     * Creates a ViewModel with only password-protected groups.
     */
    fun createPasswordGroupsViewModel(): CometChatGroupsViewModel {
        return createCustomGroupsViewModel(PreviewMockData.createPasswordGroups())
    }

    /**
     * Creates a ViewModel with a large list for scroll testing.
     */
    fun createLargeListViewModel(count: Int = 30): CometChatGroupsViewModel {
        return createCustomGroupsViewModel(PreviewMockData.createLargeGroupList(count))
    }
}
