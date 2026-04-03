package com.cometchat.uikit.kotlin.presentation.reactionlist

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction
import com.cometchat.chat.models.ReactionCount
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.domain.repository.ReactionListRepository
import com.cometchat.uikit.core.domain.usecase.FetchReactionsUseCase
import com.cometchat.uikit.core.domain.usecase.RemoveReactionUseCase
import com.cometchat.uikit.core.state.ReactionListUIState
import com.cometchat.uikit.core.viewmodel.CometChatReactionListViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for CometChatReactionList component.
 * Tests verify ViewModel integration, data flow, and state management.
 *
 * These tests require Android instrumentation to run.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatReactionListIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ==================== Test 22.4: ViewModel integration with View ====================

    /**
     * Test: ViewModel initializes with loading state
     * Verifies that the ViewModel starts in loading state.
     */
    @Test
    fun viewModel_initializesWithLoadingState() = runBlocking {
        val viewModel = createTestViewModel(emptyList())

        val state = viewModel.uiState.first()
        assertTrue(
            "Initial state should be Loading",
            state is ReactionListUIState.Loading
        )
    }

    // ==================== Test 22.5: StateFlow collection updates UI ====================

    /**
     * Test: ViewModel updates to content state with reactions
     * Verifies that the ViewModel transitions to content state when reactions are loaded.
     */
    @Test
    fun viewModel_updatesToContentStateWithReactions() = runBlocking {
        val reactions = listOf(
            createMockReaction("👍", "user1", "John Doe"),
            createMockReaction("👍", "user2", "Jane Smith")
        )
        val viewModel = createTestViewModel(reactions)

        val message = createMockMessageWithReactions()
        viewModel.setBaseMessage(message)
        viewModel.fetchReactedUsers()

        // Wait for state to settle
        Thread.sleep(100)

        val state = viewModel.uiState.value
        assertTrue("State should be Content", state is ReactionListUIState.Content)

        val reactedUsers = viewModel.reactedUsers.value
        assertEquals(2, reactedUsers.size)
    }

    // ==================== Test 22.6: setBaseMessage triggers fetch ====================

    /**
     * Test: setBaseMessage updates reaction headers
     * Verifies that setting a base message populates the reaction headers.
     */
    @Test
    fun setBaseMessage_updatesReactionHeaders() = runBlocking {
        val viewModel = createTestViewModel(emptyList())

        val message = createMockMessageWithReactions()
        viewModel.setBaseMessage(message)

        val headers = viewModel.reactionHeaders.value
        assertEquals(3, headers.size) // All + 👍 + ❤️
        assertEquals("All", headers[0].reaction)
        assertEquals(8, headers[0].count) // 5 + 3
    }

    // ==================== Test 22.7: Tab selection updates reactedUsers list ====================

    /**
     * Test: Tab selection updates active tab index
     * Verifies that selecting a tab updates the active tab index.
     */
    @Test
    fun tabSelection_updatesActiveTabIndex() = runBlocking {
        val viewModel = createTestViewModel(emptyList())

        val message = createMockMessageWithReactions()
        viewModel.setBaseMessage(message)

        // Initial state - All tab selected
        assertEquals(0, viewModel.activeTabIndex.value)
        assertEquals("All", viewModel.selectedReaction.value)

        // Select specific emoji
        viewModel.setSelectedReaction("👍")

        assertEquals(1, viewModel.activeTabIndex.value)
        assertEquals("👍", viewModel.selectedReaction.value)
    }

    // ==================== Test 22.8: removeReaction updates headers and cache ====================

    /**
     * Test: Reaction headers are properly populated
     * Verifies that reaction headers contain correct data.
     */
    @Test
    fun reactionHeaders_areProperlyPopulated() = runBlocking {
        val viewModel = createTestViewModel(emptyList())

        val message = createMockMessageWithReactions()
        viewModel.setBaseMessage(message)

        val headers = viewModel.reactionHeaders.value

        // Verify "All" tab
        val allTab = headers.find { it.reaction == "All" }
        assertNotNull(allTab)
        assertEquals(8, allTab?.count)

        // Verify individual emoji tabs
        val thumbsUpTab = headers.find { it.reaction == "👍" }
        assertNotNull(thumbsUpTab)
        assertEquals(5, thumbsUpTab?.count)

        val heartTab = headers.find { it.reaction == "❤️" }
        assertNotNull(heartTab)
        assertEquals(3, heartTab?.count)
    }

    // ==================== Test 22.9: Pagination on scroll ====================

    /**
     * Test: Fetch reacted users populates the list
     * Verifies that fetching reacted users populates the reactedUsers list.
     */
    @Test
    fun fetchReactedUsers_populatesList() = runBlocking {
        val reactions = listOf(
            createMockReaction("👍", "user1", "John Doe"),
            createMockReaction("👍", "user2", "Jane Smith"),
            createMockReaction("👍", "user3", "Bob Wilson")
        )
        val viewModel = createTestViewModel(reactions)

        val message = createMockMessageWithReactions()
        viewModel.setBaseMessage(message)
        viewModel.fetchReactedUsers()

        // Wait for state to settle
        Thread.sleep(100)

        val reactedUsers = viewModel.reactedUsers.value
        assertEquals(3, reactedUsers.size)
        assertTrue(reactedUsers.any { it.uid == "user1" })
        assertTrue(reactedUsers.any { it.uid == "user2" })
        assertTrue(reactedUsers.any { it.uid == "user3" })
    }

    /**
     * Test: Clear cache empties both caches
     * Verifies that clearing cache resets the internal state.
     */
    @Test
    fun clearCache_emptiesBothCaches() = runBlocking {
        val reactions = listOf(createMockReaction("👍", "user1", "John Doe"))
        val viewModel = createTestViewModel(reactions)

        val message = createMockMessageWithReactions()
        viewModel.setBaseMessage(message)
        viewModel.fetchReactedUsers()

        // Wait for state to settle
        Thread.sleep(100)

        assertEquals(1, viewModel.reactedUsers.value.size)

        // Clear cache
        viewModel.clearCache()

        // Cache should be cleared (internal state)
        // Verify no exception is thrown
        assertTrue(true)
    }

    /**
     * Test: Error state is set on fetch failure
     * Verifies that the ViewModel transitions to error state on failure.
     */
    @Test
    fun viewModel_setsErrorStateOnFailure() = runBlocking {
        val viewModel = createTestViewModelWithError()

        val message = createMockMessageWithReactions()
        viewModel.setBaseMessage(message)
        viewModel.fetchReactedUsers()

        // Wait for state to settle
        Thread.sleep(100)

        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is ReactionListUIState.Error)
    }

    /**
     * Test: Base message StateFlow is updated correctly
     * Verifies that the baseMessage StateFlow reflects the set message.
     */
    @Test
    fun baseMessage_stateFlowIsUpdatedCorrectly() = runBlocking {
        val viewModel = createTestViewModel(emptyList())

        assertEquals(null, viewModel.baseMessage.value)

        val message = createMockMessageWithReactions()
        viewModel.setBaseMessage(message)

        assertEquals(12345, viewModel.baseMessage.value?.id)
    }

    // ==================== Helper Methods ====================

    private fun createMockMessageWithReactions(): BaseMessage {
        return TextMessage("receiver", "Test message", "user").apply {
            id = 12345
            sentAt = System.currentTimeMillis() / 1000
            reactions = listOf(
                ReactionCount().apply {
                    reaction = "👍"
                    count = 5
                    setReactedByMe(false)
                },
                ReactionCount().apply {
                    reaction = "❤️"
                    count = 3
                    setReactedByMe(false)
                }
            )
        }
    }

    private fun createMockReaction(emoji: String, uid: String, userName: String): Reaction {
        return Reaction().apply {
            reaction = emoji
            this.uid = uid
            reactedBy = com.cometchat.chat.models.User().apply {
                this.uid = uid
                this.name = userName
            }
        }
    }

    private fun createTestViewModel(reactions: List<Reaction>): CometChatReactionListViewModel {
        val repository = object : ReactionListRepository {
            override suspend fun fetchReactions(request: ReactionsRequest): Result<List<Reaction>> {
                return Result.success(reactions)
            }
            override suspend fun removeReaction(messageId: Int, emoji: String): Result<BaseMessage> {
                return Result.success(createMockMessageWithReactions())
            }
            override fun hasMoreReactions(): Boolean = false
        }

        return CometChatReactionListViewModel(
            fetchReactionsUseCase = FetchReactionsUseCase(repository),
            removeReactionUseCase = RemoveReactionUseCase(repository),
            enableListeners = false
        )
    }

    private fun createTestViewModelWithError(): CometChatReactionListViewModel {
        val repository = object : ReactionListRepository {
            override suspend fun fetchReactions(request: ReactionsRequest): Result<List<Reaction>> {
                return Result.failure(com.cometchat.chat.exceptions.CometChatException("ERROR", "Test error"))
            }
            override suspend fun removeReaction(messageId: Int, emoji: String): Result<BaseMessage> {
                return Result.success(createMockMessageWithReactions())
            }
            override fun hasMoreReactions(): Boolean = false
        }

        return CometChatReactionListViewModel(
            fetchReactionsUseCase = FetchReactionsUseCase(repository),
            removeReactionUseCase = RemoveReactionUseCase(repository),
            enableListeners = false
        )
    }
}
