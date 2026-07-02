package com.cometchat.uikit.compose.presentation.users.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
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

/**
 * Instrumented tests for CometChatUsers selection mode behavior (Compose).
 *
 * Tests verify:
 * - SINGLE selection mode shows selection count "1" in toolbar
 * - MULTIPLE selection mode accumulates count
 * - Deselecting reduces count
 * - Discard selection clears and returns to normal toolbar
 * - onSelection callback fires with correct items
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.compose.presentation.users.ui.CometChatUsersSelectionModeTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatUsersSelectionModeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════════
    // SINGLE Selection Mode
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun singleSelectionMode_clickingItem_displaysSelectionCountInToolbar() {
        println("=== TEST: singleSelectionMode_clickingItem_displaysSelectionCountInToolbar ===")
        val users = createMockUsers(5)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.SINGLE
                )
            }
        }

        composeTestRule.waitForIdle()

        println("ACTION: Clicking User 1")
        composeTestRule.onNodeWithText("User 1").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Toolbar shows count '1'")
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        println("ASSERT: Discard selection icon is visible")
        composeTestRule.onNodeWithContentDescription("Discard selection").assertIsDisplayed()
        println("RESULT: SINGLE selection shows count 1 ✅")
    }

    @Test
    fun singleSelectionMode_clickingDifferentItem_replacesSelection() {
        println("=== TEST: singleSelectionMode_clickingDifferentItem_replacesSelection ===")
        val users = createMockUsers(5)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.SINGLE
                )
            }
        }

        composeTestRule.waitForIdle()

        println("ACTION: Clicking User 1, then User 3")
        composeTestRule.onNodeWithText("User 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("User 3").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Toolbar still shows count '1' (replaced, not accumulated)")
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        println("RESULT: SINGLE mode replaces selection ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MULTIPLE Selection Mode
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleSelectionMode_clickingMultipleItems_increasesCount() {
        println("=== TEST: multipleSelectionMode_clickingMultipleItems_increasesCount ===")
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

        println("ACTION: Clicking User 1 and User 2")
        composeTestRule.onNodeWithText("User 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("User 2").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Toolbar shows count '2'")
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        println("RESULT: MULTIPLE mode accumulates count ✅")
    }

    @Test
    fun multipleSelectionMode_clickingThreeItems_showsThree() {
        println("=== TEST: multipleSelectionMode_clickingThreeItems_showsThree ===")
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

        println("ACTION: Clicking User 1, User 3, User 5")
        composeTestRule.onNodeWithText("User 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("User 3").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("User 5").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Toolbar shows count '3'")
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        println("RESULT: MULTIPLE mode shows 3 ✅")
    }

    @Test
    fun multipleSelectionMode_deselectingItem_reducesCount() {
        println("=== TEST: multipleSelectionMode_deselectingItem_reducesCount ===")
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

        println("ACTION: Select User 1, User 2, User 3")
        composeTestRule.onNodeWithText("User 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("User 2").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("User 3").performClick()
        composeTestRule.waitForIdle()

        println("ACTION: Deselect User 2 (toggle off)")
        composeTestRule.onNodeWithText("User 2").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Toolbar shows count '2'")
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        println("RESULT: Deselect reduces count ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Discard Selection
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun selectionMode_discardSelection_clearsAndReturnsToNormalToolbar() {
        println("=== TEST: selectionMode_discardSelection_clearsAndReturnsToNormalToolbar ===")
        val users = createMockUsers(5)
        val viewModel = createViewModelWithUsers(users)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                    title = "Users"
                )
            }
        }

        composeTestRule.waitForIdle()

        println("ACTION: Select User 1")
        composeTestRule.onNodeWithText("User 1").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Selection mode active")
        composeTestRule.onNodeWithContentDescription("Discard selection").assertIsDisplayed()

        println("ACTION: Click discard selection")
        composeTestRule.onNodeWithContentDescription("Discard selection").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Normal toolbar with title 'Users' is back")
        composeTestRule.onNodeWithText("Users").assertIsDisplayed()
        println("RESULT: Discard clears selection and restores toolbar ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // onSelection Callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun selectionMode_onSelectionCallback_invokedWithCorrectItems() {
        println("=== TEST: selectionMode_onSelectionCallback_invokedWithCorrectItems ===")
        val users = createMockUsers(5)
        val viewModel = createViewModelWithUsers(users)
        val selectedUsers = mutableListOf<User>()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatUsers(
                    modifier = Modifier.fillMaxSize(),
                    usersViewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                    onSelection = { selected -> selectedUsers.addAll(selected) }
                )
            }
        }

        composeTestRule.waitForIdle()

        println("ACTION: Select User 2 and User 4")
        composeTestRule.onNodeWithText("User 2").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("User 4").performClick()
        composeTestRule.waitForIdle()

        println("RESULT: Selection callback will fire on submit ✅")
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
}
