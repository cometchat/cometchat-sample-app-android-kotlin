package com.cometchat.uikit.compose.presentation.users.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Instrumented tests for CometChatUsers custom composable slots (Compose).
 *
 * Tests verify:
 * - Custom empty view composable replaces default
 * - Custom error view composable replaces default
 * - Custom loading view composable replaces default
 * - Custom item view composable replaces default list item
 * - Custom leading view composable
 * - Custom title view composable
 * - Custom subtitle view composable
 * - Custom trailing view composable
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.compose.presentation.users.ui.CometChatUsersCustomViewTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatUsersCustomViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Empty View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customEmptyView_replacesDefaultEmptyState() {
        println("=== TEST: customEmptyView_replacesDefaultEmptyState ===")
        val viewModel = createViewModelWithUsers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    emptyView = {
                        Text("🔍 No contacts found. Try adding some friends!")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom empty view is displayed")
        composeTestRule.onNodeWithText("🔍 No contacts found. Try adding some friends!").assertIsDisplayed()
        println("ASSERT: Default empty text is NOT displayed")
        composeTestRule.onNodeWithText("No Users Available").assertDoesNotExist()
        println("RESULT: Custom empty view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Error View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customErrorView_replacesDefaultErrorState() {
        println("=== TEST: customErrorView_replacesDefaultErrorState ===")
        val viewModel = createViewModelWithError("ERR", "Failed")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    errorView = { onRetry ->
                        Text("⚠️ Connection lost. Pull down to refresh.")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom error view is displayed")
        composeTestRule.onNodeWithText("⚠️ Connection lost. Pull down to refresh.").assertIsDisplayed()
        println("ASSERT: Default Retry button is NOT displayed")
        composeTestRule.onNodeWithText("Retry").assertDoesNotExist()
        println("RESULT: Custom error view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Loading View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customLoadingView_replacesDefaultLoadingState() {
        println("=== TEST: customLoadingView_replacesDefaultLoadingState ===")
        val viewModel = createViewModelWithDelay()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    loadingView = {
                        Text("⏳ Fetching your contacts...")
                    }
                )
            }
        }

        println("ASSERT: Custom loading view is displayed")
        composeTestRule.onNodeWithText("⏳ Fetching your contacts...").assertIsDisplayed()
        println("RESULT: Custom loading view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Item View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customItemView_replacesDefaultListItem() {
        println("=== TEST: customItemView_replacesDefaultListItem ===")
        val users = createMockUsers(3)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    itemView = { user ->
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text("Custom: ${user.name} (${user.uid})")
                        }
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom item views are displayed")
        composeTestRule.onNodeWithText("Custom: User 1 (user-1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom: User 2 (user-2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom: User 3 (user-3)").assertIsDisplayed()
        println("RESULT: Custom item view replaces default list items ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Leading View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customLeadingView_replacesDefaultAvatar() {
        println("=== TEST: customLeadingView_replacesDefaultAvatar ===")
        val users = createMockUsers(2)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    leadingView = { user ->
                        Text("👤")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom leading view (emoji avatar) is displayed")
        composeTestRule.onAllNodesWithText("👤").onFirst().assertIsDisplayed()
        println("RESULT: Custom leading view replaces default avatar ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Title View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customTitleView_replacesDefaultTitle() {
        println("=== TEST: customTitleView_replacesDefaultTitle ===")
        val users = createMockUsers(2)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    titleView = { user ->
                        Text("@${user.uid}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom title view shows @uid format")
        composeTestRule.onNodeWithText("@user-1").assertIsDisplayed()
        composeTestRule.onNodeWithText("@user-2").assertIsDisplayed()
        println("RESULT: Custom title view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Subtitle View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customSubtitleView_replacesDefaultSubtitle() {
        println("=== TEST: customSubtitleView_replacesDefaultSubtitle ===")
        val users = createMockUsers(2)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    subtitleView = { user ->
                        Text("Status: ${user.status}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom subtitle view shows status")
        composeTestRule.onAllNodesWithText("Status: online").onFirst().assertIsDisplayed()
        println("RESULT: Custom subtitle view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Trailing View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customTrailingView_replacesDefaultTrailing() {
        println("=== TEST: customTrailingView_replacesDefaultTrailing ===")
        val users = createMockUsers(2)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    trailingView = { user ->
                        Text("→")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom trailing view (arrow) is displayed")
        composeTestRule.onAllNodesWithText("→").onFirst().assertIsDisplayed()
        println("RESULT: Custom trailing view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helper Functions
    // ═══════════════════════════════════════════════════════════════════════════

    private fun createMockUsers(count: Int): List<User> {
        return (1..count).map { i ->
            val user = mock<User>()
            whenever(user.uid).thenReturn("user-$i")
            whenever(user.name).thenReturn("User $i")
            whenever(user.status).thenReturn("online")
            whenever(user.avatar).thenReturn(null)
            whenever(user.isBlockedByMe).thenReturn(false)
            whenever(user.isHasBlockedMe).thenReturn(false)
            user
        }
    }

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
                kotlinx.coroutines.delay(10000)
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
