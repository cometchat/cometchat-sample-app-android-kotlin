package com.cometchat.uikit.core.viewmodel.search

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.constants.SearchFilter
import com.cometchat.uikit.core.domain.repository.SearchRepository
import com.cometchat.uikit.core.domain.usecase.FetchConversationsUseCase
import com.cometchat.uikit.core.domain.usecase.FetchMessagesUseCase
import com.cometchat.uikit.core.viewmodel.CometChatSearchViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Creates Mockito-based mock use cases for search testing.
 * Avoids subclassing use case classes which can cause NoClassDefFoundError
 * when SDK AAR classes aren't on the test runtime classpath.
 */
private fun createMockFetchConversationsUseCase(): FetchConversationsUseCase {
    val mock = mock<FetchConversationsUseCase> {
        onBlocking { invoke(any()) } doReturn Result.success(emptyList())
        on { hasMore() } doReturn false
    }
    return mock
}

private fun createMockFetchMessagesUseCase(): FetchMessagesUseCase {
    val mock = mock<FetchMessagesUseCase> {
        onBlocking { invoke(any()) } doReturn Result.success(emptyList())
        on { hasMore() } doReturn false
    }
    return mock
}

/**
 * Bug Condition Exploration Test for Search Filter Chip Visibility
 * 
 * **Property 1: Bug Condition** - Filter Group Visibility Bug
 * 
 * This test is designed to FAIL on unfixed code to confirm the bug exists.
 * The bug is that the toggleFilter method in CometChatSearchViewModel simply adds/removes
 * filters without any group-based logic, causing:
 * 1. All 7 filter chips to remain visible regardless of selection
 * 2. Filters from different groups to be selected simultaneously
 * 
 * Filter Groups (expected behavior):
 * - CONVERSATION: Unread, Groups
 * - MEDIA: Photos, Videos
 * - DOCUMENT: Documents, Audio
 * - LINK: Links (standalone)
 * 
 * **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists.
 * 
 * **Validates: Requirements 1.1, 1.8**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchFilterChipVisibilityBugExplorationTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Thread.sleep(50)
        Dispatchers.resetMain()
    }

    /**
     * Bug Condition Test Case 1: Click "Unread" → assert only Unread and Groups are visible
     * 
     * Expected behavior (after fix): When clicking "Unread", only the CONVERSATION group
     * filters (Unread, Groups) should be visible.
     * 
     * Current buggy behavior: All 7 filters remain visible.
     * 
     * This test will FAIL on unfixed code because:
     * - The current toggleFilter() simply adds/removes filters without group logic
     * - There is no visibleFilters state to track which filters should be displayed
     * - The UI shows all 7 chips regardless of selection
     * 
     * **Validates: Requirement 1.1**
     */
    context("Property 1: Bug Condition - Filter Group Visibility Bug") {
        
        test("clicking Unread should result in only Unread and Groups being selected (CONVERSATION group)") {
            runTest(testDispatcher) {
                val viewModel = CometChatSearchViewModel(
                    fetchConversationsUseCase = createMockFetchConversationsUseCase(),
                    fetchMessagesUseCase = createMockFetchMessagesUseCase()
                )

                // Click "Unread" filter
                viewModel.toggleFilter(SearchFilter.UNREAD)
                advanceUntilIdle()

                // Get the selected filters
                val selectedFilters = viewModel.selectedFilters.value

                // BUG ASSERTION: After clicking Unread, only Unread should be selected
                // The expected behavior after fix would be that clicking a filter from
                // a different group clears other groups, but within the same group,
                // only the clicked filter is selected initially.
                selectedFilters shouldHaveSize 1
                selectedFilters shouldContainAll setOf(SearchFilter.UNREAD)

                // BUG CONDITION CHECK: The current implementation doesn't have visibleFilters
                // state, so we can't directly test visibility. However, we can verify that
                // the selectedFilters only contains filters from the CONVERSATION group.
                // 
                // After the fix, there should be a visibleFilters StateFlow that exposes
                // only Unread and Groups when a CONVERSATION filter is selected.
                //
                // Verify the visibleFilters state shows only CONVERSATION group filters
                val visibleFilters = viewModel.visibleFilters.value
                visibleFilters shouldHaveSize 2
                visibleFilters shouldContainAll listOf(SearchFilter.UNREAD, SearchFilter.GROUPS)
                
                // Verify no filters from other groups are selected
                val conversationFilters = setOf(SearchFilter.UNREAD, SearchFilter.GROUPS)
                val mediaFilters = setOf(SearchFilter.PHOTOS, SearchFilter.VIDEOS)
                val documentFilters = setOf(SearchFilter.DOCUMENTS, SearchFilter.AUDIO)
                val linkFilters = setOf(SearchFilter.LINKS)

                // All selected filters should be from CONVERSATION group only
                selectedFilters.all { it in conversationFilters } shouldBe true
                selectedFilters.none { it in mediaFilters } shouldBe true
                selectedFilters.none { it in documentFilters } shouldBe true
                selectedFilters.none { it in linkFilters } shouldBe true
            }
        }

        /**
         * Bug Condition Test Case 2: Select "Unread", then click "Photos" → assert Unread is cleared
         * 
         * Expected behavior (after fix): When "Unread" is selected and user clicks "Photos",
         * the system should clear Unread and select only Photos, showing only Photos and Videos.
         * 
         * Current buggy behavior: Both Unread and Photos remain selected.
         * 
         * This test will FAIL on unfixed code because:
         * - The current toggleFilter() doesn't clear filters from other groups
         * - It simply adds the new filter to the existing selection
         * - This results in filters from multiple groups being selected simultaneously
         * 
         * **Validates: Requirement 1.8**
         */
        test("selecting Unread then clicking Photos should clear Unread and select only Photos (cross-group selection)") {
            runTest(testDispatcher) {
                val viewModel = CometChatSearchViewModel(
                    fetchConversationsUseCase = createMockFetchConversationsUseCase(),
                    fetchMessagesUseCase = createMockFetchMessagesUseCase()
                )

                // Step 1: Click "Unread" filter
                viewModel.toggleFilter(SearchFilter.UNREAD)
                advanceUntilIdle()

                // Verify Unread is selected
                viewModel.selectedFilters.value shouldContainAll setOf(SearchFilter.UNREAD)

                // Step 2: Click "Photos" filter (from MEDIA group)
                viewModel.toggleFilter(SearchFilter.PHOTOS)
                advanceUntilIdle()

                // Get the selected filters after clicking Photos
                val selectedFilters = viewModel.selectedFilters.value

                // BUG ASSERTION: After clicking Photos (MEDIA group) while Unread (CONVERSATION group)
                // is selected, the expected behavior is:
                // - Unread should be cleared (different group)
                // - Only Photos should be selected
                //
                // Current buggy behavior: Both Unread AND Photos are selected
                // This test will FAIL because selectedFilters will contain both filters
                
                // Expected: Only Photos should be selected (Unread should be cleared)
                selectedFilters shouldHaveSize 1
                selectedFilters shouldContainAll setOf(SearchFilter.PHOTOS)

                // Verify Unread is NOT in the selection (it should have been cleared)
                selectedFilters.contains(SearchFilter.UNREAD) shouldBe false

                // Verify only MEDIA group filters are selected
                val mediaFilters = setOf(SearchFilter.PHOTOS, SearchFilter.VIDEOS)
                selectedFilters.all { it in mediaFilters } shouldBe true

                // Verify the visibleFilters state shows only MEDIA group filters
                val visibleFilters = viewModel.visibleFilters.value
                visibleFilters shouldHaveSize 2
                visibleFilters shouldContainAll listOf(SearchFilter.PHOTOS, SearchFilter.VIDEOS)
            }
        }
    }
})
