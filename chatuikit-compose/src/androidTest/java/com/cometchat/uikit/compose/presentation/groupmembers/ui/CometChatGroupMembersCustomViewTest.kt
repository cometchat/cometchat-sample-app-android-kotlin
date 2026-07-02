package com.cometchat.uikit.compose.presentation.groupmembers.ui

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

/**
 * Instrumented tests for CometChatGroupMembers custom composable slots (Compose).
 *
 * Tests verify:
 * - Custom empty view composable replaces default
 * - Custom error view composable replaces default
 * - Custom loading view composable replaces default
 * - Custom listItemView composable replaces default list item
 * - Custom leading view composable
 * - Custom title view composable
 * - Custom subtitle view composable
 * - Custom tail view composable
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.compose.presentation.groupmembers.ui.CometChatGroupMembersCustomViewTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatGroupMembersCustomViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Empty View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customEmptyView_replacesDefaultEmptyState() {
        println("=== TEST: customEmptyView_replacesDefaultEmptyState ===")
        val viewModel = createViewModelWithMembers(emptyList())

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    emptyView = {
                        Text("🔍 No members found. Try inviting some!")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom empty view is displayed")
        composeTestRule.onNodeWithText("🔍 No members found. Try inviting some!").assertIsDisplayed()
        println("ASSERT: Default empty text is NOT displayed")
        composeTestRule.onNodeWithText("No Members Found").assertDoesNotExist()
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
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    errorView = { exception ->
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
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    loadingView = {
                        Text("⏳ Fetching group members...")
                    }
                )
            }
        }

        println("ASSERT: Custom loading view is displayed")
        composeTestRule.onNodeWithText("⏳ Fetching group members...").assertIsDisplayed()
        println("RESULT: Custom loading view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom List Item View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customListItemView_replacesDefaultListItem() {
        println("=== TEST: customListItemView_replacesDefaultListItem ===")
        val members = createMockMembers(3)
        val viewModel = createViewModelWithMembers(members)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    listItemView = { member ->
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text("Custom: ${member.name} (${member.uid})")
                        }
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom list item views are displayed")
        composeTestRule.onNodeWithText("Custom: Member 1 (user-1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom: Member 2 (user-2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom: Member 3 (user-3)").assertIsDisplayed()
        println("RESULT: Custom listItemView replaces default list items ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Leading View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customLeadingView_replacesDefaultAvatar() {
        println("=== TEST: customLeadingView_replacesDefaultAvatar ===")
        val members = createMockMembers(2)
        val viewModel = createViewModelWithMembers(members)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    leadingView = { member ->
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
        val members = createMockMembers(2)
        val viewModel = createViewModelWithMembers(members)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    titleView = { member ->
                        Text("@${member.uid}")
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
        val members = createMockMembers(2)
        val viewModel = createViewModelWithMembers(members)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    subtitleView = { member ->
                        Text("Scope: ${member.scope}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom subtitle view shows scope")
        composeTestRule.onAllNodesWithText("Scope: ${CometChatConstants.SCOPE_PARTICIPANT}").onFirst().assertIsDisplayed()
        println("RESULT: Custom subtitle view replaces default ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Custom Tail View
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customTailView_replacesDefaultTrailing() {
        println("=== TEST: customTailView_replacesDefaultTrailing ===")
        val members = createMockMembers(2)
        val viewModel = createViewModelWithMembers(members)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    tailView = { member ->
                        Text("→")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        println("ASSERT: Custom tail view (arrow) is displayed")
        composeTestRule.onAllNodesWithText("→").onFirst().assertIsDisplayed()
        println("RESULT: Custom tail view replaces default ✅")
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
