package com.cometchat.uikit.kotlin.presentation.groupmembers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.domain.repository.GroupMembersRepository
import com.cometchat.uikit.core.domain.usecase.BanGroupMemberUseCase
import com.cometchat.uikit.core.domain.usecase.ChangeMemberScopeUseCase
import com.cometchat.uikit.core.domain.usecase.FetchGroupMembersUseCase
import com.cometchat.uikit.core.domain.usecase.KickGroupMemberUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupMembersViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.groupmembers.ui.CometChatGroupMembers
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Integration Tests for CometChatGroupMembers (XML/Kotlin).
 *
 * Architecture:
 *   [Fake Repository] → [Real UseCases] → [Real ViewModel]
 *       → [Real CometChatGroupMembers View] → [Espresso assertions]
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.groupmembers.CometChatGroupMembersViewIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatGroupMembersViewIntegrationTest {

    @Before
    fun setup() {
        println("=== SETUP: Clearing GroupMembersHostFragment state ===")
        GroupMembersHostFragment.injectedRepository = null
        GroupMembersHostFragment.onItemClickMember = null
        GroupMembersHostFragment.group = null
    }

    @After
    fun tearDown() {
        GroupMembersHostFragment.injectedRepository = null
    }

    private fun createMockMembers(count: Int): List<GroupMember> {
        return (1..count).map { i ->
            GroupMember("user-$i", CometChatConstants.SCOPE_PARTICIPANT).apply {
                name = "Member $i"
            }
        }
    }

    private fun createMockGroup(): Group {
        return Group().apply {
            guid = "test-group"
            name = "Test Group"
            membersCount = 10
        }
    }

    private fun createSuccessRepository(members: List<GroupMember>): GroupMembersRepository {
        return object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?) = Result.success(members)
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = false
            override fun resetRequest() {}
        }
    }

    private fun launchFragment(): FragmentScenario<GroupMembersHostFragment> {
        return launchFragmentInContainer(themeResId = R.style.CometChatTheme_DayNight)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Content state renders member items
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun contentState_rendersMemberItems() {
        println("=== TEST: contentState_rendersMemberItems ===")
        val members = createMockMembers(3)
        GroupMembersHostFragment.injectedRepository = createSuccessRepository(members)
        GroupMembersHostFragment.group = createMockGroup()

        println("STEP 1: Launching fragment with 3 members")
        val scenario = launchFragment()

        println("STEP 2: Asserting RecyclerView is displayed")
        onView(withId(R.id.recyclerview_group_members)).check(matches(isDisplayed()))

        println("STEP 3: Asserting member names are rendered")
        onView(withId(R.id.recyclerview_group_members)).check(matches(hasDescendant(withText("Member 1"))))
        onView(withId(R.id.recyclerview_group_members)).check(matches(hasDescendant(withText("Member 2"))))
        onView(withId(R.id.recyclerview_group_members)).check(matches(hasDescendant(withText("Member 3"))))

        println("RESULT: All 3 members rendered ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Empty state shows empty view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyState_showsEmptyView() {
        println("=== TEST: emptyState_showsEmptyView ===")
        GroupMembersHostFragment.injectedRepository = createSuccessRepository(emptyList())
        GroupMembersHostFragment.group = createMockGroup()

        val scenario = launchFragment()

        println("STEP 1: Asserting empty state view is displayed")
        onView(withId(R.id.empty_state_view)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerview_group_members)).check(matches(not(isDisplayed())))

        println("RESULT: Empty state displayed ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Error state shows error view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_showsErrorView() {
        println("=== TEST: errorState_showsErrorView ===")
        GroupMembersHostFragment.injectedRepository = object : GroupMembersRepository {
            override suspend fun fetchGroupMembers(guid: String, limit: Int, searchKeyword: String?): Result<List<GroupMember>> {
                return Result.failure(CometChatException("NET_ERR", "Network error"))
            }
            override suspend fun kickMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun banMember(guid: String, uid: String) = Result.success(Unit)
            override suspend fun changeMemberScope(guid: String, uid: String, scope: String) = Result.success(Unit)
            override fun hasMore() = true
            override fun resetRequest() {}
        }
        GroupMembersHostFragment.group = createMockGroup()

        val scenario = launchFragment()

        println("STEP 1: Asserting error state view is displayed")
        onView(withId(R.id.error_state_view)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_retry)).check(matches(isDisplayed()))

        println("RESULT: Error state with retry button displayed ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Item click triggers callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun itemClick_triggersCallback() {
        println("=== TEST: itemClick_triggersCallback ===")
        val members = createMockMembers(3)
        GroupMembersHostFragment.injectedRepository = createSuccessRepository(members)
        GroupMembersHostFragment.group = createMockGroup()

        val scenario = launchFragment()

        println("STEP 1: Clicking item at position 0")
        onView(withId(R.id.recyclerview_group_members))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        println("STEP 2: Asserting callback received correct member")
        assertNotNull("onItemClick should have fired", GroupMembersHostFragment.onItemClickMember)
        assertEquals("user-1", GroupMembersHostFragment.onItemClickMember?.uid)

        println("RESULT: Item click delivered member user-1 ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Toolbar is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun toolbar_isVisible() {
        println("=== TEST: toolbar_isVisible ===")
        GroupMembersHostFragment.injectedRepository = createSuccessRepository(createMockMembers(1))
        GroupMembersHostFragment.group = createMockGroup()

        val scenario = launchFragment()
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        println("RESULT: Toolbar visible ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Search box is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun searchBox_isVisible() {
        println("=== TEST: searchBox_isVisible ===")
        GroupMembersHostFragment.injectedRepository = createSuccessRepository(createMockMembers(1))
        GroupMembersHostFragment.group = createMockGroup()

        val scenario = launchFragment()
        onView(withId(R.id.search_box_layout)).check(matches(isDisplayed()))
        println("RESULT: Search box visible ✅")
        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment
    // ─────────────────────────────────────────────────────────────────────────

    class GroupMembersHostFragment : Fragment() {
        companion object {
            var injectedRepository: GroupMembersRepository? = null
            var onItemClickMember: GroupMember? = null
            var group: Group? = null
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val view = CometChatGroupMembers(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
            return FrameLayout(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                addView(view)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val groupMembersView = (view as FrameLayout).getChildAt(0) as CometChatGroupMembers

            injectedRepository?.let { repo ->
                val fetchUseCase = FetchGroupMembersUseCase(repo)
                val kickUseCase = KickGroupMemberUseCase(repo)
                val banUseCase = BanGroupMemberUseCase(repo)
                val changeScopeUseCase = ChangeMemberScopeUseCase(repo)
                val viewModel = CometChatGroupMembersViewModel(
                    fetchGroupMembersUseCase = fetchUseCase,
                    kickGroupMemberUseCase = kickUseCase,
                    banGroupMemberUseCase = banUseCase,
                    changeMemberScopeUseCase = changeScopeUseCase,
                    enableListeners = false
                )
                groupMembersView.setViewModel(viewModel)
            }

            group?.let { g ->
                groupMembersView.setGroup(g)
            }

            groupMembersView.setOnItemClick { member ->
                println("  GroupMembersHostFragment: onItemClick uid='${member.uid}'")
                onItemClickMember = member
            }
        }
    }
}
