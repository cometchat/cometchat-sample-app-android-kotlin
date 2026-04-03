package com.cometchat.uikit.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cometchat.chat.core.GroupMembersRequest
import com.cometchat.uikit.core.data.datasource.GroupMembersDataSourceImpl
import com.cometchat.uikit.core.data.repository.GroupMembersRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.BanGroupMemberUseCase
import com.cometchat.uikit.core.domain.usecase.ChangeMemberScopeUseCase
import com.cometchat.uikit.core.domain.usecase.FetchGroupMembersUseCase
import com.cometchat.uikit.core.domain.usecase.KickGroupMemberUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel

/**
 * Factory for creating [CometChatGroupMembersViewModel] instances.
 * Handles dependency injection for the ViewModel.
 * 
 * @param groupMembersRequestBuilder Optional custom request builder for fetching members
 * @param enableListeners Whether to enable CometChat listeners (default: true, set false for testing)
 * @param repository Optional custom repository for data operations. If null, creates default from request builder.
 */
class CometChatGroupMembersViewModelFactory(
    private val groupMembersRequestBuilder: GroupMembersRequest.GroupMembersRequestBuilder? = null,
    private val enableListeners: Boolean = true,
    private val repository: com.cometchat.uikit.core.domain.repository.GroupMembersRepository? = null
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CometChatGroupMembersViewModel::class.java)) {
            // Use provided repository or create default
            val repo = repository ?: run {
                val requestBuilder = groupMembersRequestBuilder 
                    ?: GroupMembersRequest.GroupMembersRequestBuilder("")
                        .setLimit(30)
                val dataSource = GroupMembersDataSourceImpl(requestBuilder)
                GroupMembersRepositoryImpl(dataSource)
            }
            
            // Create use cases
            val fetchGroupMembersUseCase = FetchGroupMembersUseCase(repo)
            val kickGroupMemberUseCase = KickGroupMemberUseCase(repo)
            val banGroupMemberUseCase = BanGroupMemberUseCase(repo)
            val changeMemberScopeUseCase = ChangeMemberScopeUseCase(repo)
            
            // Create ViewModel
            return CometChatGroupMembersViewModel(
                fetchGroupMembersUseCase = fetchGroupMembersUseCase,
                kickGroupMemberUseCase = kickGroupMemberUseCase,
                banGroupMemberUseCase = banGroupMemberUseCase,
                changeMemberScopeUseCase = changeMemberScopeUseCase,
                enableListeners = enableListeners
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
