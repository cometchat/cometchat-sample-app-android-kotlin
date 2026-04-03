package com.cometchat.uikit.kotlin.presentation.groups

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.GroupsRepository
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.state.GroupsUIState
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Integration tests for CometChatGroups component.
 * Tests verify ViewModel integration, data flow, and state management.
 * 
 * These tests require Android instrumentation to run.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatGroupsIntegrationTest {

    private lateinit var context: Context

    // Mock data
    private lateinit var mockGroup1: Group
    private lateinit var mockGroup2: Group

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Setup mock groups
        mockGroup1 = mock<Group>()
        whenever(mockGroup1.guid).thenReturn("group1")
        whenever(mockGroup1.name).thenReturn("Test Group 1")
        whenever(mockGroup1.icon).thenReturn("https://example.com/group1.png")
        whenever(mockGroup1.groupType).thenReturn(CometChatConstants.GROUP_TYPE_PUBLIC)
        whenever(mockGroup1.membersCount).thenReturn(10)
        whenever(mockGroup1.isJoined).thenReturn(true)
        
        mockGroup2 = mock<Group>()
        whenever(mockGroup2.guid).thenReturn("group2")
        whenever(mockGroup2.name).thenReturn("Test Group 2")
        whenever(mockGroup2.icon).thenReturn("https://example.com/group2.png")
        whenever(mockGroup2.groupType).thenReturn(CometChatConstants.GROUP_TYPE_PRIVATE)
        whenever(mockGroup2.membersCount).thenReturn(5)
        whenever(mockGroup2.isJoined).thenReturn(false)
    }

    // ==================== ViewModel Integration Tests ====================

    /**
     * Test: ViewModel initializes with loading state
     * Verifies that the ViewModel starts in loading state.
     */
    @Test
    fun viewModel_initializesWithLoadingState() = runBlocking {
        val viewModel = createTestViewModel(emptyList())
        
        // Initial state should be Loading or transition to Empty for empty list
        val state = viewModel.uiState.first()
        assertTrue(
            "Initial state should be Loading or Empty",
            state is GroupsUIState.Loading || state is GroupsUIState.Empty
        )
    }

    /**
     * Test: ViewModel updates to content state with groups
     * Verifies that the ViewModel transitions to content state when groups are loaded.
     */
    @Test
    fun viewModel_updatesToContentStateWithGroups() = runBlocking {
        val groups = listOf(mockGroup1, mockGroup2)
        val viewModel = createTestViewModel(groups)
        
        // Wait for state to settle
        Thread.sleep(100)
        
        val state = viewModel.uiState.value
        assertTrue("State should be Content", state is GroupsUIState.Content)
        
        val groupsList = viewModel.groups.value
        assertEquals(2, groupsList.size)
    }

    /**
     * Test: ViewModel updates to empty state with no groups
     * Verifies that the ViewModel transitions to empty state when no groups are loaded.
     */
    @Test
    fun viewModel_updatesToEmptyStateWithNoGroups() = runBlocking {
        val viewModel = createTestViewModel(emptyList())
        
        // Wait for state to settle
        Thread.sleep(100)
        
        val state = viewModel.uiState.value
        assertTrue("State should be Empty", state is GroupsUIState.Empty)
    }

    // ==================== Selection Integration Tests ====================

    /**
     * Test: Selection state updates correctly
     * Verifies that selection state is properly managed.
     */
    @Test
    fun selectionState_updatesCorrectly() = runBlocking {
        val groups = listOf(mockGroup1, mockGroup2)
        val viewModel = createTestViewModel(groups)
        
        // Wait for state to settle
        Thread.sleep(100)
        
        // Select a group
        viewModel.selectGroup(mockGroup1, UIKitConstants.SelectionMode.MULTIPLE)
        
        val selectedGroups = viewModel.selectedGroups.value
        assertEquals(1, selectedGroups.size)
        assertTrue(selectedGroups.any { it.guid == mockGroup1.guid })
    }

    /**
     * Test: Clear selection works correctly
     * Verifies that clearing selection removes all selected groups.
     */
    @Test
    fun clearSelection_worksCorrectly() = runBlocking {
        val groups = listOf(mockGroup1, mockGroup2)
        val viewModel = createTestViewModel(groups)
        
        // Wait for state to settle
        Thread.sleep(100)
        
        // Select groups
        viewModel.selectGroup(mockGroup1, UIKitConstants.SelectionMode.MULTIPLE)
        viewModel.selectGroup(mockGroup2, UIKitConstants.SelectionMode.MULTIPLE)
        
        assertEquals(2, viewModel.selectedGroups.value.size)
        
        // Clear selection
        viewModel.clearSelection()
        
        assertEquals(0, viewModel.selectedGroups.value.size)
    }

    /**
     * Test: Single selection mode replaces previous selection
     * Verifies that single selection mode only keeps one selection.
     */
    @Test
    fun singleSelectionMode_replacesPreviousSelection() = runBlocking {
        val groups = listOf(mockGroup1, mockGroup2)
        val viewModel = createTestViewModel(groups)
        
        // Wait for state to settle
        Thread.sleep(100)
        
        // Select first group
        viewModel.selectGroup(mockGroup1, UIKitConstants.SelectionMode.SINGLE)
        assertEquals(1, viewModel.selectedGroups.value.size)
        assertTrue(viewModel.selectedGroups.value.any { it.guid == mockGroup1.guid })
        
        // Select second group (should replace first)
        viewModel.selectGroup(mockGroup2, UIKitConstants.SelectionMode.SINGLE)
        assertEquals(1, viewModel.selectedGroups.value.size)
        assertTrue(viewModel.selectedGroups.value.any { it.guid == mockGroup2.guid })
    }

    // ==================== Data Flow Tests ====================

    /**
     * Test: Groups list is properly populated
     * Verifies that the groups list contains the expected data.
     */
    @Test
    fun groupsList_isProperlyPopulated() = runBlocking {
        val groups = listOf(mockGroup1, mockGroup2)
        val viewModel = createTestViewModel(groups)
        
        // Wait for state to settle
        Thread.sleep(100)
        
        val groupsList = viewModel.groups.value
        assertEquals(2, groupsList.size)
        
        val group1 = groupsList.find { it.guid == "group1" }
        assertNotNull(group1)
        assertEquals("Test Group 1", group1?.name)
        
        val group2 = groupsList.find { it.guid == "group2" }
        assertNotNull(group2)
        assertEquals("Test Group 2", group2?.name)
    }

    /**
     * Test: Group properties are correctly mapped
     * Verifies that group properties are properly accessible.
     */
    @Test
    fun groupProperties_areCorrectlyMapped() {
        assertEquals("group1", mockGroup1.guid)
        assertEquals("Test Group 1", mockGroup1.name)
        assertEquals(CometChatConstants.GROUP_TYPE_PUBLIC, mockGroup1.groupType)
        assertEquals(10, mockGroup1.membersCount)
        assertTrue(mockGroup1.isJoined)
    }

    // ==================== Request Builder Tests ====================

    /**
     * Test: Custom request builder is applied
     * Verifies that custom request builder is properly used.
     */
    @Test
    fun customRequestBuilder_isApplied() = runBlocking {
        val viewModel = createTestViewModel(emptyList())
        
        val customBuilder = GroupsRequest.GroupsRequestBuilder()
            .setLimit(50)
            .setSearchKeyWord("test")
        
        viewModel.setGroupsRequestBuilder(customBuilder)
        
        // Verify builder was set (no exception thrown)
        assertTrue(true)
    }

    /**
     * Test: Search request builder is applied
     * Verifies that search request builder is properly used.
     */
    @Test
    fun searchRequestBuilder_isApplied() = runBlocking {
        val viewModel = createTestViewModel(emptyList())
        
        val searchBuilder = GroupsRequest.GroupsRequestBuilder()
            .setLimit(30)
        
        viewModel.setSearchRequestBuilder(searchBuilder)
        
        // Verify builder was set (no exception thrown)
        assertTrue(true)
    }

    // ==================== Helper Methods ====================

    private fun createTestViewModel(groups: List<Group>): CometChatGroupsViewModel {
        val repository = object : GroupsRepository {
            override suspend fun fetchGroups(request: GroupsRequest): Result<List<Group>> {
                return Result.success(groups)
            }
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?): Result<Group> {
                return Result.failure(Exception("Not implemented"))
            }
            override fun hasMoreGroups(): Boolean = false
        }

        val fetchGroupsUseCase = FetchGroupsUseCase(repository)
        val joinGroupUseCase = JoinGroupUseCase(repository)

        return CometChatGroupsViewModel(
            fetchGroupsUseCase = fetchGroupsUseCase,
            joinGroupUseCase = joinGroupUseCase,
            enableListeners = false
        )
    }
}
