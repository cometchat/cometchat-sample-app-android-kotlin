package com.cometchat.uikit.compose.presentation.groupmembers.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
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

/**
 * Instrumented tests for CometChatGroupMembers selection mode behavior (Compose).
 *
 * Tests verify:
 * - SINGLE selection mode shows selection count "1" in toolbar
 * - MULTIPLE selection mode accumulates count
 * - Deselecting reduces count
 * - Discard selection clears and returns to normal toolbar
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.compose.presentation.groupmembers.ui.CometChatGroupMembersSelectionModeTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatGroupMembersSelectionModeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════════
    // SINGLE Selection Mode
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun singleSelectionMode_clickingItem_displaysSelectionCountInToolbar() {
        println("=== TEST: singleSelectionMode_clickingItem_displaysSelectionCountInToolbar ===")
        val members = createMockMembers(5)
        val viewModel = createViewModelWithMembers(members)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.SINGLE
                )
            }
        }

        composeTestRule.waitForIdle()

        println("ACTION: Clicking Member 1")
        composeTestRule.onNodeWithText("Member 1").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Toolbar shows count '1'")
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        println("ASSERT: Discard selection icon is visible")
        composeTestRule.onNodeWithContentDescription("Discard selection").assertIsDisplayed()
        println("RESULT: SINGLE selection shows count 1 ✅")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MULTIPLE Selection Mode
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleSelectionMode_clickingMultipleItems_increasesCount() {
        println("=== TEST: multipleSelectionMode_clickingMultipleItems_increasesCount ===")
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

        println("ACTION: Clicking Member 1 and Member 2")
        composeTestRule.onNodeWithText("Member 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Member 2").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Toolbar shows count '2'")
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        println("RESULT: MULTIPLE mode accumulates count ✅")
    }

    @Test
    fun multipleSelectionMode_deselectingItem_reducesCount() {
        println("=== TEST: multipleSelectionMode_deselectingItem_reducesCount ===")
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

        println("ACTION: Select Member 1, Member 2, Member 3")
        composeTestRule.onNodeWithText("Member 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Member 2").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Member 3").performClick()
        composeTestRule.waitForIdle()

        println("ACTION: Deselect Member 2 (toggle off)")
        composeTestRule.onNodeWithText("Member 2").performClick()
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
        val members = createMockMembers(5)
        val viewModel = createViewModelWithMembers(members)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatGroupMembers(
                    group = createMockGroup(),
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    selectionMode = UIKitConstants.SelectionMode.MULTIPLE,
                    title = "Members"
                )
            }
        }

        composeTestRule.waitForIdle()

        println("ACTION: Select Member 1")
        composeTestRule.onNodeWithText("Member 1").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Selection mode active")
        composeTestRule.onNodeWithContentDescription("Discard selection").assertIsDisplayed()

        println("ACTION: Click discard selection")
        composeTestRule.onNodeWithContentDescription("Discard selection").performClick()
        composeTestRule.waitForIdle()

        println("ASSERT: Normal toolbar with title 'Members' is back")
        composeTestRule.onNodeWithText("Members").assertIsDisplayed()
        println("RESULT: Discard clears selection and restores toolbar ✅")
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
}
