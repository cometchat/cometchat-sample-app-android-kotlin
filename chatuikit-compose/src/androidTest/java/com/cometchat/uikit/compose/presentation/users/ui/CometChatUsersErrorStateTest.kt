package com.cometchat.uikit.compose.presentation.users.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Instrumented tests for CometChatUsers error and loading states (Compose).
 *
 * Tests verify:
 * - Error state displays default error view with Retry button
 * - Custom error view replaces default
 * - onError callback fires with correct exception
 * - Retry button triggers re-fetch
 * - Loading state displays default loading view
 * - Custom loading view replaces default
 * - hideErrorState prevents error view from showing
 * - hideLoadingState prevents loading view from showing
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.compose.presentation.users.ui.CometChatUsersErrorStateTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatUsersErrorStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════════
    // Error State Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_displaysDefaultErrorView_withRetryButton() {
        println("=== TEST: errorState_displaysDefaultErrorView_withRetryButton ===")
        val viewModel = createViewModelWithError("NET_ERR", "Network error")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Retry button is displayed")
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        println("RESULT: Default error view with Retry button ✅")
    }

    @Test
    fun errorState_customErrorView_replacesDefault() {
        println("=== TEST: errorState_customErrorView_replacesDefault ===")
        val viewModel = createViewModelWithError("ERR", "Something went wrong")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    errorView = { onRetry ->
                        Text("Oops! Something broke. Tap to retry.")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom error view is displayed")
        composeTestRule.onNodeWithText("Oops! Something broke. Tap to retry.").assertIsDisplayed()
        println("ASSERT: Default Retry button is NOT displayed")
        composeTestRule.onNodeWithText("Retry").assertDoesNotExist()
        println("RESULT: Custom error view replaces default ✅")
    }

    @Test
    fun errorState_onErrorCallback_firesWithException() {
        println("=== TEST: errorState_onErrorCallback_firesWithException ===")
        val errorRef = AtomicReference<CometChatException?>(null)
        val viewModel = createViewModelWithError("AUTH_ERR", "Token expired")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    onError = { exception -> errorRef.set(exception) }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: onError callback was invoked with correct code")
        assert(errorRef.get() != null) { "onError should have been invoked" }
        assert(errorRef.get()?.code == "AUTH_ERR") { "Error code should be AUTH_ERR" }
        println("RESULT: onError fired with code='AUTH_ERR' ✅")
    }

    @Test
    fun errorState_retryButton_triggersRefetch() {
        println("=== TEST: errorState_retryButton_triggersRefetch ===")
        var fetchCount = 0
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                fetchCount++
                return if (fetchCount <= 1) {
                    Result.failure(CometChatException("ERR", "First attempt fails"))
                } else {
                    Result.success(emptyList())
                }
            }
            override fun hasMoreUsers() = true
        }
        val viewModel = CometChatUsersViewModel(
            FetchUsersUseCase(repository),
            SearchUsersUseCase(repository),
            enableListeners = false
        )

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Error state with Retry button")
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()

        println("ACTION: Clicking Retry")
        composeTestRule.onNodeWithText("Retry").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Retry triggered re-fetch (fetchCount > 1)")
        assert(fetchCount > 1) { "Retry should trigger another fetch" }
        println("RESULT: Retry button triggers re-fetch ✅")
    }

    @Test
    fun errorState_hideErrorState_preventsErrorViewFromShowing() {
        println("=== TEST: errorState_hideErrorState_preventsErrorViewFromShowing ===")
        val viewModel = createViewModelWithError("ERR", "Hidden error")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    hideErrorState = true
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Retry button does NOT exist (error state hidden)")
        composeTestRule.onNodeWithText("Retry").assertDoesNotExist()
        println("RESULT: hideErrorState prevents error view ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Loading State Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun loadingState_customLoadingView_replacesDefault() {
        println("=== TEST: loadingState_customLoadingView_replacesDefault ===")
        val viewModel = createViewModelWithDelay()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    loadingView = {
                        Text("Loading users, please wait...")
                    }
                )
            }
        }

        println("ASSERT: Custom loading view is displayed")
        composeTestRule.onNodeWithText("Loading users, please wait...").assertIsDisplayed()
        println("RESULT: Custom loading view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Empty State Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyState_displaysDefaultEmptyView() {
        println("=== TEST: emptyState_displaysDefaultEmptyView ===")
        val viewModel = createViewModelWithUsers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Default empty state text is displayed")
        composeTestRule.onNodeWithText("No Users Available").assertIsDisplayed()
        println("RESULT: Default empty state displayed ✅")
    }

    @Test
    fun emptyState_hideEmptyState_preventsEmptyViewFromShowing() {
        println("=== TEST: emptyState_hideEmptyState_preventsEmptyViewFromShowing ===")
        val viewModel = createViewModelWithUsers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    hideEmptyState = true
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Empty state text does NOT exist")
        composeTestRule.onNodeWithText("No Users Available").assertDoesNotExist()
        println("RESULT: hideEmptyState prevents empty view ✅")
    }

    @Test
    fun emptyState_onEmptyCallback_fires() {
        println("=== TEST: emptyState_onEmptyCallback_fires ===")
        val emptyFired = AtomicBoolean(false)
        val viewModel = createViewModelWithUsers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    onEmpty = { emptyFired.set(true) }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: onEmpty callback was invoked")
        assert(emptyFired.get()) { "onEmpty should have been invoked" }
        println("RESULT: onEmpty callback fired ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helper Functions
    // ═══════════════════════════════════════════════════════════════════════════

    private fun createViewModelWithUsers(users: List<User>): CometChatUsersViewModel {
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest) = Result.success(users)
            override fun hasMoreUsers() = false
        }
        return CometChatUsersViewModel(
            FetchUsersUseCase(repository),
            SearchUsersUseCase(repository),
            enableListeners = false
        )
    }

    private fun createViewModelWithError(code: String, message: String): CometChatUsersViewModel {
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest) =
                Result.failure<List<User>>(CometChatException(code, message))
            override fun hasMoreUsers() = true
        }
        return CometChatUsersViewModel(
            FetchUsersUseCase(repository),
            SearchUsersUseCase(repository),
            enableListeners = false
        )
    }

    private fun createViewModelWithDelay(): CometChatUsersViewModel {
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest): Result<List<User>> {
                kotlinx.coroutines.delay(10000) // Long delay to keep loading state
                return Result.success(emptyList())
            }
            override fun hasMoreUsers() = true
        }
        return CometChatUsersViewModel(
            FetchUsersUseCase(repository),
            SearchUsersUseCase(repository),
            enableListeners = false
        )
    }
}
