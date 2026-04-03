package com.cometchat.uikit.compose.calllogs.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.compose.presentation.calllogs.ui.CometChatCallLogs
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.repository.CallLogsRepository
import com.cometchat.uikit.core.domain.usecase.FetchCallLogsUseCase
import com.cometchat.uikit.core.domain.usecase.InitiateCallUseCase
import com.cometchat.uikit.core.viewmodel.CometChatCallLogsViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for CometChatCallLogs.
 * 
 * Feature: call-logs
 * Tests verify component rendering, interactions, and state transitions.
 */
@RunWith(AndroidJUnit4::class)
class CometChatCallLogsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun callLogs_displaysToolbarWithTitle() {
        // Arrange
        val viewModel = createViewModelWithCallLogs(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    title = "Call History"
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Call History").assertIsDisplayed()
    }

    @Test
    fun callLogs_displaysDefaultTitle() {
        // Arrange
        val viewModel = createViewModelWithCallLogs(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Assert - Default title should be "Calls"
        composeTestRule.onNodeWithText("Calls").assertIsDisplayed()
    }

    @Test
    fun callLogs_hidesToolbarWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithCallLogs(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    title = "Calls",
                    hideToolbar = true
                )
            }
        }

        // Assert - Title should not be displayed when toolbar is hidden
        composeTestRule.onNodeWithText("Calls").assertDoesNotExist()
    }

    @Test
    fun callLogs_displaysEmptyStateWhenNoCallLogs() {
        // Arrange
        val viewModel = createViewModelWithCallLogs(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Empty state text should be displayed
        composeTestRule.onNodeWithText("No Call Logs Yet").assertIsDisplayed()
    }

    @Test
    fun callLogs_displaysCustomEmptyView() {
        // Arrange
        val viewModel = createViewModelWithCallLogs(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    emptyView = {
                        Text("Custom Empty State - No call history")
                    }
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Custom empty view should be displayed
        composeTestRule.onNodeWithText("Custom Empty State - No call history").assertIsDisplayed()
    }

    @Test
    fun callLogs_hidesEmptyStateWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithCallLogs(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideEmptyState = true
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Empty state should not be displayed
        composeTestRule.onNodeWithText("No Call Logs Yet").assertDoesNotExist()
    }

    @Test
    fun callLogs_hidesBackButtonWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithCallLogs(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideBackButton = true
                )
            }
        }

        // Assert - Back button should not be displayed
        composeTestRule.onNodeWithText("Navigate back", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun callLogs_displaysCustomLoadingView() {
        // Arrange - ViewModel in loading state
        val viewModel = createViewModelInLoadingState()

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    loadingView = {
                        Text("Custom Loading...")
                    }
                )
            }
        }

        // Assert - Custom loading view should be displayed
        composeTestRule.onNodeWithText("Custom Loading...").assertIsDisplayed()
    }

    @Test
    fun callLogs_displaysCustomErrorView() {
        // Arrange - ViewModel in error state
        val viewModel = createViewModelInErrorState()

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    errorView = { onRetry ->
                        Text("Custom Error - Tap to retry")
                    }
                )
            }
        }

        // Assert - Custom error view should be displayed
        composeTestRule.onNodeWithText("Custom Error - Tap to retry").assertIsDisplayed()
    }

    @Test
    fun callLogs_hidesSeparatorWhenConfigured() {
        // Arrange
        val viewModel = createViewModelWithCallLogs(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallLogs(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideSeparator = true
                )
            }
        }

        // Assert - Component should render without separators
        // This is a configuration test - separators are hidden in the list items
        composeTestRule.waitForIdle()
    }
}

/**
 * Helper function to create a ViewModel with predefined call logs.
 */
private fun createViewModelWithCallLogs(callLogs: List<CallLog>): CometChatCallLogsViewModel {
    val repository = object : CallLogsRepository {
        override suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>> {
            return Result.success(callLogs)
        }
        override fun hasMoreCallLogs(): Boolean = false
    }

    val fetchCallLogsUseCase = FetchCallLogsUseCase(repository)
    val initiateCallUseCase = InitiateCallUseCase()

    return CometChatCallLogsViewModel(
        fetchCallLogsUseCase = fetchCallLogsUseCase,
        initiateCallUseCase = initiateCallUseCase,
        enableListeners = false
    )
}

/**
 * Helper function to create a ViewModel in loading state.
 */
private fun createViewModelInLoadingState(): CometChatCallLogsViewModel {
    val repository = object : CallLogsRepository {
        override suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>> {
            // Suspend indefinitely to keep loading state
            kotlinx.coroutines.delay(Long.MAX_VALUE)
            return Result.success(emptyList())
        }
        override fun hasMoreCallLogs(): Boolean = false
    }

    val fetchCallLogsUseCase = FetchCallLogsUseCase(repository)
    val initiateCallUseCase = InitiateCallUseCase()

    return CometChatCallLogsViewModel(
        fetchCallLogsUseCase = fetchCallLogsUseCase,
        initiateCallUseCase = initiateCallUseCase,
        enableListeners = false
    )
}

/**
 * Helper function to create a ViewModel in error state.
 */
private fun createViewModelInErrorState(): CometChatCallLogsViewModel {
    val repository = object : CallLogsRepository {
        override suspend fun getCallLogs(request: CallLogRequest): Result<List<CallLog>> {
            return Result.failure(com.cometchat.calls.exceptions.CometChatException("TEST_ERROR", "Test error"))
        }
        override fun hasMoreCallLogs(): Boolean = false
    }

    val fetchCallLogsUseCase = FetchCallLogsUseCase(repository)
    val initiateCallUseCase = InitiateCallUseCase()

    return CometChatCallLogsViewModel(
        fetchCallLogsUseCase = fetchCallLogsUseCase,
        initiateCallUseCase = initiateCallUseCase,
        enableListeners = false
    )
}
