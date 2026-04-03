package com.cometchat.uikit.compose.groups.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.presentation.groups.ui.CometChatGroups
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.repository.GroupsRepository
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instrumented UI tests for CometChatGroups.
 * 
 * Feature: groups-compose
 * Tests verify component rendering, interactions, and state transitions.
 */
@RunWith(AndroidJUnit4::class)
class CometChatGroupsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun groupsList_displaysToolbarWithTitle() {
        // Arrange
        val viewModel = createViewModelWithGroups(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    title = "My Groups"
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("My Groups").assertIsDisplayed()
    }

    @Test
    fun groupsList_hidesToolbarWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithGroups(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    title = "Groups",
                    hideToolbar = true
                )
            }
        }

        // Assert - Title should not be displayed when toolbar is hidden
        composeTestRule.onNodeWithText("Groups").assertDoesNotExist()
    }

    @Test
    fun groupsList_displaysSearchBoxByDefault() {
        // Arrange
        val viewModel = createViewModelWithGroups(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    searchPlaceholderText = "Search groups"
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Search groups").assertIsDisplayed()
    }

    @Test
    fun groupsList_hidesSearchBoxWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithGroups(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideSearchBox = true,
                    searchPlaceholderText = "Search"
                )
            }
        }

        // Assert - Search placeholder should not be displayed
        composeTestRule.onNodeWithText("Search").assertDoesNotExist()
    }

    @Test
    fun groupsList_displaysEmptyStateWhenNoGroups() {
        // Arrange
        val viewModel = createViewModelWithGroups(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Empty state text should be displayed
        composeTestRule.onNodeWithText("No Groups Found").assertIsDisplayed()
    }

    @Test
    fun groupsList_displaysCustomEmptyView() {
        // Arrange
        val viewModel = createViewModelWithGroups(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    emptyView = {
                        Text("Custom Empty State")
                    }
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Custom empty view should be displayed
        composeTestRule.onNodeWithText("Custom Empty State").assertIsDisplayed()
    }

    @Test
    fun groupsList_hidesEmptyStateWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithGroups(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideEmptyState = true
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Empty state should not be displayed
        composeTestRule.onNodeWithText("No Groups Found").assertDoesNotExist()
    }

    @Test
    fun groupsList_invokesOnEmptyCallback() {
        // Arrange
        val emptyCallbackInvoked = AtomicBoolean(false)
        val viewModel = createViewModelWithGroups(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onEmpty = { emptyCallbackInvoked.set(true) }
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert
        assert(emptyCallbackInvoked.get()) { "onEmpty callback should have been invoked" }
    }

    @Test
    fun groupsList_searchBoxAcceptsInput() {
        // Arrange
        val viewModel = createViewModelWithGroups(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroups(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    searchPlaceholderText = "Search"
                )
            }
        }

        // Type in search box
        composeTestRule.onNodeWithText("Search").performTextInput("test query")

        // Wait for debounce
        composeTestRule.waitForIdle()

        // Assert - Search text should be visible
        composeTestRule.onNodeWithText("test query").assertIsDisplayed()
    }
}

/**
 * Helper function to create a ViewModel with predefined groups.
 */
private fun createViewModelWithGroups(groups: List<Group>): CometChatGroupsViewModel {
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
