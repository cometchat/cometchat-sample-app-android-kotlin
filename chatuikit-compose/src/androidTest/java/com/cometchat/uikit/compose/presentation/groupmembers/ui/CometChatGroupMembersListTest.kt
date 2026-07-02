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
import com.cometchat.uikit.core.constants.UIKitConstants
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
 * Instrumented UI tests for CometChatGroupMembers Compose component.
 *
 * Tests verify composable rendering, toolbar, search, empty state,
 * error states, content display, item click, and selection mode.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.groupmembers.ui.CometChatGroupMembersListTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatGroupMembersListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Toolbar displays title
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_displaysToolbarWithTitle() {
        println("=== TEST: groupMembersList_displaysToolbarWithTitle ===")
        val viewModel = createViewModelWithMembers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    title = "Members"
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Title 'Members' is displayed")
        composeTestRule.onNodeWithText("Members").assertIsDisplayed()
        println("RESULT: Toolbar title 'Members' is displayed ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Toolbar hidden when configured
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_hidesToolbarWhenConfigured() {
        println("=== TEST: groupMembersList_hidesToolbarWhenConfigured ===")
        val viewModel = createViewModelWithMembers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    title = "Members",
                    hideToolbar = true
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Title does not exist when hideToolbar=true")
        composeTestRule.onNodeWithText("Members").assertDoesNotExist()
        println("RESULT: Toolbar hidden successfully ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Search box displays placeholder
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_displaysSearchPlaceholder() {
        println("=== TEST: groupMembersList_displaysSearchPlaceholder ===")
        val viewModel = createViewModelWithMembers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    searchPlaceholderText = "Find members..."
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Search placeholder is displayed")
        composeTestRule.onNodeWithText("Find members...").assertIsDisplayed()
        println("RESULT: Search placeholder 'Find members...' displayed ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Search box hidden when configured
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_hidesSearchBoxWhenConfigured() {
        println("=== TEST: groupMembersList_hidesSearchBoxWhenConfigured ===")
        val viewModel = createViewModelWithMembers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideSearch = true,
                    searchPlaceholderText = "Search"
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Search placeholder does not exist")
        composeTestRule.onNodeWithText("Search").assertDoesNotExist()
        println("RESULT: Search box hidden successfully ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Empty state displayed when no members
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_displaysEmptyState_whenNoMembers() {
        println("=== TEST: groupMembersList_displaysEmptyState_whenNoMembers ===")
        val viewModel = createViewModelWithMembers(emptyList())

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
        println("ASSERT: Empty state is displayed")
        composeTestRule.onNodeWithText("No Members Found").assertIsDisplayed()
        println("RESULT: Empty state displayed when no members ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Custom empty view replaces default
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_displaysCustomEmptyView() {
        println("=== TEST: groupMembersList_displaysCustomEmptyView ===")
        val viewModel = createViewModelWithMembers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    emptyView = {
                        Text("Custom: No group members found")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom empty view is displayed")
        composeTestRule.onNodeWithText("Custom: No group members found").assertIsDisplayed()
        println("RESULT: Custom empty view rendered correctly ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: onError callback fires
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_invokesOnErrorCallback() {
        println("=== TEST: groupMembersList_invokesOnErrorCallback ===")
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
        assert(errorRef.get() != null) { "onError callback should have been invoked" }
        assert(errorRef.get()?.code == "AUTH_ERR") { "Error code should be AUTH_ERR" }
        println("RESULT: onError fired with code='${errorRef.get()?.code}' ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Error state displays when fetch fails
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_displaysErrorState_whenFetchFails() {
        println("=== TEST: groupMembersList_displaysErrorState_whenFetchFails ===")
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
        println("ASSERT: Error state with Retry button is displayed")
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        println("RESULT: Error state with Retry button displayed ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Custom error view replaces default
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_displaysCustomErrorView() {
        println("=== TEST: groupMembersList_displaysCustomErrorView ===")
        val viewModel = createViewModelWithError("ERR", "Something went wrong")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    errorView = { exception ->
                        Text("Custom Error: Please try again")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom error view is displayed")
        composeTestRule.onNodeWithText("Custom Error: Please try again").assertIsDisplayed()
        println("RESULT: Custom error view rendered correctly ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Content state displays member items
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_displaysMemberItems_whenContentState() {
        println("=== TEST: groupMembersList_displaysMemberItems_whenContentState ===")
        val members = createMockMembers(3)
        val viewModel = createViewModelWithMembers(members)

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
        println("ASSERT: Member names are displayed")
        composeTestRule.onNodeWithText("Member 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Member 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Member 3").assertIsDisplayed()
        println("RESULT: All 3 members rendered in list ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: onItemClick callback fires with correct member
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_invokesOnItemClick_withCorrectMember() {
        println("=== TEST: groupMembersList_invokesOnItemClick_withCorrectMember ===")
        val clickedMember = AtomicReference<GroupMember?>(null)
        val members = createMockMembers(3)
        val viewModel = createViewModelWithMembers(members)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onItemClick = { member -> clickedMember.set(member) }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Clicking on 'Member 2'")
        composeTestRule.onNodeWithText("Member 2").performClick()

        composeTestRule.waitForIdle()
        println("ASSERT: onItemClick received member with uid user-2")
        assert(clickedMember.get() != null) { "onItemClick should have been invoked" }
        assert(clickedMember.get()?.uid == "user-2") { "Clicked member UID should be user-2, got ${clickedMember.get()?.uid}" }
        println("RESULT: onItemClick fired with UID='${clickedMember.get()?.uid}' ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Selection mode shows count in toolbar
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupMembersList_selectionMode_showsCountInToolbar() {
        println("=== TEST: groupMembersList_selectionMode_showsCountInToolbar ===")
        val members = createMockMembers(5)
        val viewModel = createViewModelWithMembers(members)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.MULTIPLE
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ACTION: Clicking Member 1 and Member 3")
        composeTestRule.onNodeWithText("Member 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Member 3").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Toolbar shows count '2'")
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        println("RESULT: Selection count '2' displayed in toolbar ✅")
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
            private var fetched = false
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?): Result<List<GroupMember>> {
                if (fetched) return Result.success(emptyList())
                fetched = true
                return Result.success(members)
            }
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = false
            override fun resetRequest() { fetched = false }
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
}
