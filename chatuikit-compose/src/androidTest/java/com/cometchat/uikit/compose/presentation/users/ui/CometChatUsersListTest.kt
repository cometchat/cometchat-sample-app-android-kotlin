package com.cometchat.uikit.compose.presentation.users.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.UsersRepository
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Instrumented UI tests for CometChatUsers Compose component.
 *
 * Tests verify composable rendering, toolbar, search, empty state,
 * selection mode, error states, and custom composable slots.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.users.ui.CometChatUsersListTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatUsersListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Toolbar displays title
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_displaysToolbarWithTitle() {
        println("=== TEST: usersList_displaysToolbarWithTitle ===")
        println("STEP 1: Creating ViewModel with empty list")
        val viewModel = createViewModelWithUsers(emptyList())

        println("STEP 2: Setting content with title 'My Contacts'")
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    title = "My Contacts"
                )
            }
        }

        println("STEP 3: Asserting title is displayed")
        composeTestRule.onNodeWithText("My Contacts").assertIsDisplayed()
        println("RESULT: Toolbar title 'My Contacts' is displayed")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Toolbar hidden when configured
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_hidesToolbarWhenConfigured() {
        println("=== TEST: usersList_hidesToolbarWhenConfigured ===")
        val viewModel = createViewModelWithUsers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    title = "Users",
                    hideToolbar = true
                )
            }
        }

        println("ACTION: Checking title does not exist when hideToolbar=true")
        composeTestRule.onNodeWithText("Users").assertDoesNotExist()
        println("RESULT: Toolbar hidden successfully")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Search box displays placeholder
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_displaysSearchPlaceholder() {
        println("=== TEST: usersList_displaysSearchPlaceholder ===")
        val viewModel = createViewModelWithUsers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    searchPlaceholderText = "Find users..."
                )
            }
        }

        println("ACTION: Checking search placeholder is displayed")
        composeTestRule.onNodeWithText("Find users...").assertIsDisplayed()
        println("RESULT: Search placeholder 'Find users...' displayed")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Search box hidden when configured
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_hidesSearchBoxWhenConfigured() {
        println("=== TEST: usersList_hidesSearchBoxWhenConfigured ===")
        val viewModel = createViewModelWithUsers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    hideSearchBox = true,
                    searchPlaceholderText = "Search"
                )
            }
        }

        println("ACTION: Checking search placeholder does not exist")
        composeTestRule.onNodeWithText("Search").assertDoesNotExist()
        println("RESULT: Search box hidden successfully")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Empty state displayed when no users
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_displaysEmptyState_whenNoUsers() {
        println("=== TEST: usersList_displaysEmptyState_whenNoUsers ===")
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
        println("ACTION: Checking empty state is displayed")
        // The default empty state text from the component
        composeTestRule.onNodeWithText("No Users Available").assertIsDisplayed()
        println("RESULT: Empty state displayed when no users")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Custom empty view replaces default
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_displaysCustomEmptyView() {
        println("=== TEST: usersList_displaysCustomEmptyView ===")
        val viewModel = createViewModelWithUsers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    emptyView = {
                        Text("Custom: No contacts available")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Checking custom empty view is displayed")
        composeTestRule.onNodeWithText("Custom: No contacts available").assertIsDisplayed()
        println("RESULT: Custom empty view rendered correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: onEmpty callback fires
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_invokesOnEmptyCallback() {
        println("=== TEST: usersList_invokesOnEmptyCallback ===")
        val emptyCallbackInvoked = AtomicBoolean(false)
        val viewModel = createViewModelWithUsers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    onEmpty = { emptyCallbackInvoked.set(true) }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Checking onEmpty callback was invoked")
        assert(emptyCallbackInvoked.get()) { "onEmpty callback should have been invoked" }
        println("RESULT: onEmpty callback fired successfully")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Error state displays error view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_displaysErrorState_whenFetchFails() {
        println("=== TEST: usersList_displaysErrorState_whenFetchFails ===")
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
        println("ACTION: Checking error state is displayed")
        // Default error state has a retry button
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        println("RESULT: Error state with Retry button displayed")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Custom error view replaces default
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_displaysCustomErrorView() {
        println("=== TEST: usersList_displaysCustomErrorView ===")
        val viewModel = createViewModelWithError("ERR", "Something went wrong")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    errorView = { onRetry ->
                        Text("Custom Error: Please try again")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Checking custom error view is displayed")
        composeTestRule.onNodeWithText("Custom Error: Please try again").assertIsDisplayed()
        println("RESULT: Custom error view rendered correctly")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: onError callback fires with exception
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_invokesOnErrorCallback() {
        println("=== TEST: usersList_invokesOnErrorCallback ===")
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
        println("ACTION: Checking onError callback was invoked")
        assert(errorRef.get() != null) { "onError callback should have been invoked" }
        assert(errorRef.get()?.code == "AUTH_ERR") { "Error code should be AUTH_ERR" }
        println("RESULT: onError fired with code='${errorRef.get()?.code}'")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Content state displays user items
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_displaysUserItems_whenContentState() {
        println("=== TEST: usersList_displaysUserItems_whenContentState ===")
        val users = createMockUsers(3)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Checking user names are displayed")
        composeTestRule.onNodeWithText("User 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("User 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("User 3").assertIsDisplayed()
        println("RESULT: All 3 users rendered in LazyColumn")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: onItemClick callback fires with correct user
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_invokesOnItemClick_withCorrectUser() {
        println("=== TEST: usersList_invokesOnItemClick_withCorrectUser ===")
        val clickedUser = AtomicReference<User?>(null)
        val users = createMockUsers(3)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    onItemClick = { user -> clickedUser.set(user) }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Clicking on 'User 2'")
        composeTestRule.onNodeWithText("User 2").performClick()

        composeTestRule.waitForIdle()
        println("STEP: Asserting onItemClick received user-2")
        assert(clickedUser.get() != null) { "onItemClick should have been invoked" }
        assert(clickedUser.get()?.uid == "user-2") { "Clicked user UID should be user-2, got ${clickedUser.get()?.uid}" }
        println("RESULT: onItemClick fired with UID='${clickedUser.get()?.uid}'")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Selection mode shows count in toolbar
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun usersList_selectionMode_showsCountInToolbar() {
        println("=== TEST: usersList_selectionMode_showsCountInToolbar ===")
        val users = createMockUsers(5)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.MULTIPLE
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Clicking User 1 and User 3")
        composeTestRule.onNodeWithText("User 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("User 3").performClick()
        composeTestRule.waitForIdle()

        println("STEP: Asserting toolbar shows '2'")
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        println("RESULT: Selection count '2' displayed in toolbar")
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
        val fetchUseCase = FetchUsersUseCase(repository)
        val searchUseCase = SearchUsersUseCase(repository)
        return CometChatUsersViewModel(fetchUseCase, searchUseCase, enableListeners = false)
    }

    private fun createViewModelWithError(code: String, message: String): CometChatUsersViewModel {
        val repository = object : UsersRepository {
            override suspend fun getUsers(request: UsersRequest) =
                Result.failure<List<User>>(CometChatException(code, message))
            override fun hasMoreUsers() = true
        }
        val fetchUseCase = FetchUsersUseCase(repository)
        val searchUseCase = SearchUsersUseCase(repository)
        return CometChatUsersViewModel(fetchUseCase, searchUseCase, enableListeners = false)
    }
}
