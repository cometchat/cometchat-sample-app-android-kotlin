package com.cometchat.uikit.core.viewmodel

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.constants.SearchFilter
import com.cometchat.uikit.core.domain.repository.SearchRepository
import com.cometchat.uikit.core.domain.usecase.FetchConversationsUseCase
import com.cometchat.uikit.core.domain.usecase.FetchMessagesUseCase
import com.cometchat.uikit.core.state.SearchUIState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Test implementation of SearchRepository for preservation testing.
 * Tracks search invocations to verify search is triggered correctly.
 */
private class PreservationSearchRepository : SearchRepository {
    var conversationSearchCount = 0
    var messageSearchCount = 0
    var lastConversationRequest: ConversationsRequest? = null
    var lastMessageRequest: MessagesRequest? = null
    
    override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
        conversationSearchCount++
        lastConversationRequest = request
        return Result.success(emptyList())
    }
    
    override suspend fun getMessages(request: MessagesRequest): Result<List<BaseMessage>> {
        messageSearchCount++
        lastMessageRequest = request
        return Result.success(emptyList())
    }
    
    override fun hasMoreConversations(): Boolean = false
    override fun hasMoreMessages(): Boolean = false
    
    fun reset() {
        conversationSearchCount = 0
        messageSearchCount = 0
        lastConversationRequest = null
        lastMessageRequest = null
    }
}

/**
 * Test implementation of FetchConversationsUseCase for preservation testing.
 */
private class PreservationFetchConversationsUseCase(
    private val repository: PreservationSearchRepository
) : FetchConversationsUseCase(repository) {
    override suspend fun invoke(request: ConversationsRequest): Result<List<Conversation>> =
        repository.getConversations(request)
    
    override fun hasMore(): Boolean = false
}

/**
 * Test implementation of FetchMessagesUseCase for preservation testing.
 */
private class PreservationFetchMessagesUseCase(
    private val repository: PreservationSearchRepository
) : FetchMessagesUseCase(repository) {
    override suspend fun invoke(request: MessagesRequest): Result<List<BaseMessage>> =
        repository.getMessages(request)
    
    override fun hasMore(): Boolean = false
}

/**
 * Preservation Property Tests for Search Filter Chip Visibility
 * 
 * **Property 2: Preservation** - Search Functionality
 * 
 * These tests verify baseline behaviors that MUST be preserved after the fix:
 * 1. Clicking a filter triggers search with updated selection
 * 2. With no filters selected, all 7 chips are visible (all SearchFilter values available)
 * 
 * **EXPECTED OUTCOME**: Tests PASS on unfixed code (confirms baseline behavior to preserve)
 * 
 * **Validates: Requirements 3.1, 3.3**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchFilterChipPreservationTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    context("Property 2: Preservation - Search Functionality") {
        
        /**
         * Preservation Test Case 1: Clicking a filter triggers search with updated selection
         * 
         * This test verifies that when a filter is clicked, the search is triggered
         * with the updated filter selection. This is core functionality that must
         * be preserved after the fix.
         * 
         * **EXPECTED OUTCOME**: Test PASSES on unfixed code
         * 
         * **Validates: Requirement 3.1**
         */
        test("clicking a filter triggers search with updated selection") {
            runTest(testDispatcher) {
                val repository = PreservationSearchRepository()
                val viewModel = CometChatSearchViewModel(
                    fetchConversationsUseCase = PreservationFetchConversationsUseCase(repository),
                    fetchMessagesUseCase = PreservationFetchMessagesUseCase(repository)
                )

                // Initial state - no searches yet
                repository.conversationSearchCount shouldBe 0
                repository.messageSearchCount shouldBe 0

                // Click "Unread" filter (conversation filter)
                viewModel.toggleFilter(SearchFilter.UNREAD)
                advanceUntilIdle()

                // Verify search was triggered
                // Unread is a conversation filter, so conversation search should be triggered
                repository.conversationSearchCount shouldBe 1
                
                // Verify the filter is in the selected filters
                viewModel.selectedFilters.value shouldContainAll setOf(SearchFilter.UNREAD)

                // Reset and test with a message filter
                repository.reset()
                viewModel.clear()
                advanceUntilIdle()

                // Click "Photos" filter (message filter)
                viewModel.toggleFilter(SearchFilter.PHOTOS)
                advanceUntilIdle()

                // Verify search was triggered
                // Photos is a message filter, so message search should be triggered
                repository.messageSearchCount shouldBe 1
                
                // Verify the filter is in the selected filters
                viewModel.selectedFilters.value shouldContainAll setOf(SearchFilter.PHOTOS)
            }
        }

        /**
         * Preservation Test Case 2: With no filters selected, all 7 SearchFilter values are available
         * 
         * This test verifies that when no filters are selected, all 7 SearchFilter enum
         * values are available for selection. This represents the initial state where
         * all filter chips should be visible.
         * 
         * **EXPECTED OUTCOME**: Test PASSES on unfixed code
         * 
         * **Validates: Requirement 3.3**
         */
        test("with no filters selected, all 7 SearchFilter values are available") {
            runTest(testDispatcher) {
                val repository = PreservationSearchRepository()
                val viewModel = CometChatSearchViewModel(
                    fetchConversationsUseCase = PreservationFetchConversationsUseCase(repository),
                    fetchMessagesUseCase = PreservationFetchMessagesUseCase(repository)
                )

                // Verify initial state - no filters selected
                viewModel.selectedFilters.value shouldHaveSize 0

                // Verify all 7 SearchFilter values exist and are available
                val allFilters = SearchFilter.values()
                allFilters shouldHaveSize 7

                // Verify each filter type exists
                allFilters shouldContainAll listOf(
                    SearchFilter.UNREAD,
                    SearchFilter.GROUPS,
                    SearchFilter.PHOTOS,
                    SearchFilter.VIDEOS,
                    SearchFilter.DOCUMENTS,
                    SearchFilter.AUDIO,
                    SearchFilter.LINKS
                )

                // Verify UI state is Initial when no filters and no search text
                viewModel.uiState.value shouldBe SearchUIState.Initial

                // Verify we can toggle any filter from the initial state
                // This confirms all filters are "available" for selection
                for (filter in allFilters) {
                    viewModel.clear()
                    advanceUntilIdle()
                    
                    viewModel.toggleFilter(filter)
                    advanceUntilIdle()
                    
                    // Verify the filter was successfully added
                    viewModel.selectedFilters.value.contains(filter) shouldBe true
                }
            }
        }

        /**
         * Preservation Test Case 3: Search is triggered with correct filter when toggling
         * 
         * This test verifies that toggling a filter on and off correctly updates
         * the search with the appropriate filter selection.
         * 
         * **EXPECTED OUTCOME**: Test PASSES on unfixed code
         * 
         * **Validates: Requirement 3.1**
         */
        test("toggling filter on and off correctly updates search") {
            runTest(testDispatcher) {
                val repository = PreservationSearchRepository()
                val viewModel = CometChatSearchViewModel(
                    fetchConversationsUseCase = PreservationFetchConversationsUseCase(repository),
                    fetchMessagesUseCase = PreservationFetchMessagesUseCase(repository)
                )

                // Toggle Unread ON
                viewModel.toggleFilter(SearchFilter.UNREAD)
                advanceUntilIdle()

                viewModel.selectedFilters.value shouldContainAll setOf(SearchFilter.UNREAD)
                viewModel.selectedFilters.value shouldHaveSize 1

                // Toggle Unread OFF
                viewModel.toggleFilter(SearchFilter.UNREAD)
                advanceUntilIdle()

                viewModel.selectedFilters.value shouldHaveSize 0
                viewModel.selectedFilters.value.contains(SearchFilter.UNREAD) shouldBe false

                // UI should return to Initial state when no filters
                viewModel.uiState.value shouldBe SearchUIState.Initial
            }
        }

        /**
         * Preservation Test Case 4: Multiple filters can be selected (current behavior)
         * 
         * This test documents the current behavior where multiple filters can be
         * selected. Note: After the fix, cross-group selection will be restricted,
         * but intra-group multi-selection should still work.
         * 
         * **EXPECTED OUTCOME**: Test PASSES on unfixed code
         * 
         * **Validates: Requirement 3.1**
         */
        test("multiple filters can be selected and search is triggered") {
            runTest(testDispatcher) {
                val repository = PreservationSearchRepository()
                val viewModel = CometChatSearchViewModel(
                    fetchConversationsUseCase = PreservationFetchConversationsUseCase(repository),
                    fetchMessagesUseCase = PreservationFetchMessagesUseCase(repository)
                )

                // Select Unread (conversation filter)
                viewModel.toggleFilter(SearchFilter.UNREAD)
                advanceUntilIdle()

                val searchCountAfterFirst = repository.conversationSearchCount

                // Select Groups (also conversation filter - same group)
                viewModel.toggleFilter(SearchFilter.GROUPS)
                advanceUntilIdle()

                // Verify both filters are selected
                viewModel.selectedFilters.value shouldContainAll setOf(
                    SearchFilter.UNREAD,
                    SearchFilter.GROUPS
                )
                viewModel.selectedFilters.value shouldHaveSize 2

                // Verify search was triggered again
                repository.conversationSearchCount shouldBe searchCountAfterFirst + 1
            }
        }
    }
})
