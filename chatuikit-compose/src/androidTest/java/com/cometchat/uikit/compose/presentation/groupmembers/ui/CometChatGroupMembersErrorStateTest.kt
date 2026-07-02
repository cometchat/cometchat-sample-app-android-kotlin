package com.cometchat.uikit.compose.presentation.groupmembers.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository
import com.cometchat.uikit.core.domain.usecase.BanGroupMemberUseCase
import com.cometchat.uikit.core.domain.usecase.ChangeMemberScopeUseCase
import com.cometchat.uikit.core.domain.usecase.FetchGroupMembersUseCase
import com.cometchat.uikit.core.domain.usecase.KickGroupMemberUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

/**
 * Instrumented tests for CometChatGroupMembers error and loading states (Compose).
 *
 * Tests verify:
 * - Error state displays default error view with Retry button
 * - Custom error view replaces default
 * - onError callback fires with correct exception
 * - hideErrorState prevents error view from showing
 * - Custom loading view replaces default
 * - hideEmptyState prevents empty view from showing
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.compose.presentation.groupmembers.ui.CometChatGroupMembersErrorStateTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatGroupMembersErrorStateTest {

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
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
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
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    errorView = { exception ->
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
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
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
    fun errorState_hideErrorState_preventsErrorViewFromShowing() {
        println("=== TEST: errorState_hideErrorState_preventsErrorViewFromShowing ===")
        val viewModel = createViewModelWithError("ERR", "Hidden error")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
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
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    loadingView = {
                        Text("Loading members, please wait...")
                    }
                )
            }
        }

        println("ASSERT: Custom loading view is displayed")
        composeTestRule.onNodeWithText("Loading members, please wait...").assertIsDisplayed()
        println("RESULT: Custom loading view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Empty State Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyState_hideEmptyState_preventsEmptyViewFromShowing() {
        println("=== TEST: emptyState_hideEmptyState_preventsEmptyViewFromShowing ===")
        val viewModel = createViewModelWithMembers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideEmptyState = true
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Empty state text does NOT exist")
        composeTestRule.onNodeWithText("No Members Available").assertDoesNotExist()
        println("RESULT: hideEmptyState prevents empty view ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helper Functions
    // ═══════════════════════════════════════════════════════════════════════════

    private fun createMockMembers(count: Int): List<GroupMember> {
        return (1..count).map { i ->
            GroupMember("user-$i", CometChatConstants.SCOPE_PARTICIPANT).apply { name = "Member $i" }
        }
    }

    private fun createMockGroup(): Group = Group().apply {
        guid = "test-group"
        name = "Test Group"
        membersCount = 10
    }

    private fun createViewModelWithMembers(members: List<GroupMember>): CometChatGroupMembersViewModel {
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = Result.success(members)
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = false
            override fun resetRequest() {}
        }
        val vm = CometChatGroupMembersViewModel(
            FetchGroupMembersUseCase(repository),
            KickGroupMemberUseCase(repository),
            BanGroupMemberUseCase(repository),
            ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
        vm.setGroup(createMockGroup())
        return vm
    }

    private fun createViewModelWithError(code: String, message: String): CometChatGroupMembersViewModel {
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) =
                Result.failure<List<GroupMember>>(CometChatException(code, message))
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = true
            override fun resetRequest() {}
        }
        val vm = CometChatGroupMembersViewModel(
            FetchGroupMembersUseCase(repository),
            KickGroupMemberUseCase(repository),
            BanGroupMemberUseCase(repository),
            ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
        vm.setGroup(createMockGroup())
        return vm
    }

    private fun createViewModelWithDelay(): CometChatGroupMembersViewModel {
        val repository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?): Result<List<GroupMember>> {
                kotlinx.coroutines.delay(10000)
                return Result.success(emptyList())
            }
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = true
            override fun resetRequest() {}
        }
        val vm = CometChatGroupMembersViewModel(
            FetchGroupMembersUseCase(repository),
            KickGroupMemberUseCase(repository),
            BanGroupMemberUseCase(repository),
            ChangeMemberScopeUseCase(repository),
            enableListeners = false
        )
        vm.setGroup(createMockGroup())
        return vm
    }
}
