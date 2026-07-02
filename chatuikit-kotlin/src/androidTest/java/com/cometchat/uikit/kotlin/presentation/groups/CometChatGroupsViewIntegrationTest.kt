package com.cometchat.uikit.kotlin.presentation.groups

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
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.GroupsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.data.datasource.GroupsDataSource
import com.cometchat.uikit.core.data.repository.GroupsRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.FetchGroupsUseCase
import com.cometchat.uikit.core.domain.usecase.JoinGroupUseCase
import com.cometchat.uikit.core.viewmodel.CometChatGroupsViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.groups.ui.CometChatGroups
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Integration Tests for CometChatGroups (XML/Kotlin).
 *
 * Architecture:
 *   [Fake DataSource] → [Real Repository] → [Real UseCases] → [Real ViewModel]
 *       → [Real CometChatGroups View] → [Espresso assertions]
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.groups.CometChatGroupsViewIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatGroupsViewIntegrationTest {

    @Before
    fun setup() {
        println("=== SETUP: Clearing GroupsHostFragment state ===")
        GroupsHostFragment.injectedDataSource = null
        GroupsHostFragment.onItemClickGroup = null
        GroupsHostFragment.onItemLongClickGroup = null
        GroupsHostFragment.selectionMode = UIKitConstants.SelectionMode.NONE
    }

    @After
    fun tearDown() {
        GroupsHostFragment.injectedDataSource = null
    }

    private fun createMockGroup(guid: String, name: String, type: String = CometChatConstants.GROUP_TYPE_PUBLIC): Group {
        return Group(guid, name, type, "", null, "")
    }

    private fun createMockGroups(count: Int): List<Group> {
        return (1..count).map { i -> createMockGroup("group-$i", "Group $i") }
    }

    private fun launchFragment(): FragmentScenario<GroupsHostFragment> {
        return launchFragmentInContainer(themeResId = R.style.CometChatTheme_DayNight)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Content state renders group items
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun contentState_rendersGroupItems_inRecyclerView() {
        println("=== TEST: contentState_rendersGroupItems_inRecyclerView ===")
        val groups = createMockGroups(3)
        GroupsHostFragment.injectedDataSource = object : GroupsDataSource {
            override suspend fun fetchGroups(request: GroupsRequest) = groups
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = groups[0]
        }

        println("STEP 1: Launching fragment with 3 groups")
        val scenario = launchFragment()

        println("STEP 2: Asserting RecyclerView is displayed")
        onView(withId(R.id.recyclerview_groups_list)).check(matches(isDisplayed()))

        println("STEP 3: Asserting group names are rendered")
        onView(withId(R.id.recyclerview_groups_list)).check(matches(hasDescendant(withText("Group 1"))))
        onView(withId(R.id.recyclerview_groups_list)).check(matches(hasDescendant(withText("Group 2"))))
        onView(withId(R.id.recyclerview_groups_list)).check(matches(hasDescendant(withText("Group 3"))))

        println("RESULT: All 3 groups rendered ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Empty state shows empty view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyState_showsEmptyView_whenNoGroups() {
        println("=== TEST: emptyState_showsEmptyView_whenNoGroups ===")
        GroupsHostFragment.injectedDataSource = object : GroupsDataSource {
            override suspend fun fetchGroups(request: GroupsRequest) = emptyList<Group>()
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = createMockGroup("x", "x")
        }

        val scenario = launchFragment()

        println("STEP 1: Asserting empty state view is displayed")
        onView(withId(R.id.empty_state_view)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerview_groups_list)).check(matches(not(isDisplayed())))

        println("RESULT: Empty state displayed ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Error state shows error view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_showsErrorView_whenDataSourceThrows() {
        println("=== TEST: errorState_showsErrorView_whenDataSourceThrows ===")
        GroupsHostFragment.injectedDataSource = object : GroupsDataSource {
            override suspend fun fetchGroups(request: GroupsRequest): List<Group> {
                throw CometChatException("NET_ERR", "Network error")
            }
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = createMockGroup("x", "x")
        }

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
    fun itemClick_triggersCallback_withCorrectGroup() {
        println("=== TEST: itemClick_triggersCallback_withCorrectGroup ===")
        val groups = createMockGroups(3)
        GroupsHostFragment.injectedDataSource = object : GroupsDataSource {
            override suspend fun fetchGroups(request: GroupsRequest) = groups
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = groups[0]
        }

        val scenario = launchFragment()

        println("STEP 1: Clicking item at position 0")
        onView(withId(R.id.recyclerview_groups_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        println("STEP 2: Asserting callback received correct group")
        assertNotNull("onItemClick should have fired", GroupsHostFragment.onItemClickGroup)
        assertEquals("group-1", GroupsHostFragment.onItemClickGroup?.guid)

        println("RESULT: Item click delivered group-1 ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Selection mode shows count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun selectionMode_clickingItems_showsCount() {
        println("=== TEST: selectionMode_clickingItems_showsCount ===")
        val groups = createMockGroups(5)
        GroupsHostFragment.selectionMode = UIKitConstants.SelectionMode.MULTIPLE
        GroupsHostFragment.injectedDataSource = object : GroupsDataSource {
            override suspend fun fetchGroups(request: GroupsRequest) = groups
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = groups[0]
        }

        val scenario = launchFragment()

        println("STEP 1: Clicking items at positions 0 and 1")
        onView(withId(R.id.recyclerview_groups_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.recyclerview_groups_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        println("STEP 2: Asserting toolbar shows '2'")
        onView(withText("2")).check(matches(isDisplayed()))

        println("RESULT: Selection count '2' displayed ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Toolbar is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun toolbar_isVisible() {
        println("=== TEST: toolbar_isVisible ===")
        val groups = createMockGroups(1)
        GroupsHostFragment.injectedDataSource = object : GroupsDataSource {
            override suspend fun fetchGroups(request: GroupsRequest) = groups
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = groups[0]
        }

        val scenario = launchFragment()
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        println("RESULT: Toolbar visible ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Search box is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun searchBox_isVisible() {
        println("=== TEST: searchBox_isVisible ===")
        val groups = createMockGroups(1)
        GroupsHostFragment.injectedDataSource = object : GroupsDataSource {
            override suspend fun fetchGroups(request: GroupsRequest) = groups
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = groups[0]
        }

        val scenario = launchFragment()
        onView(withId(R.id.search_box_layout)).check(matches(isDisplayed()))
        println("RESULT: Search box visible ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: RecyclerView has correct item count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun recyclerView_hasCorrectItemCount() {
        println("=== TEST: recyclerView_hasCorrectItemCount ===")
        val groups = createMockGroups(5)
        GroupsHostFragment.injectedDataSource = object : GroupsDataSource {
            override suspend fun fetchGroups(request: GroupsRequest) = groups
            override suspend fun joinGroup(groupId: String, groupType: String, password: String?) = groups[0]
        }

        val scenario = launchFragment()
        onView(withId(R.id.recyclerview_groups_list)).check { view, _ ->
            val rv = view as RecyclerView
            assertEquals(5, rv.adapter?.itemCount)
        }
        println("RESULT: RecyclerView has 5 items ✅")
        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment
    // ─────────────────────────────────────────────────────────────────────────

    class GroupsHostFragment : Fragment() {
        companion object {
            var injectedDataSource: GroupsDataSource? = null
            var onItemClickGroup: Group? = null
            var onItemLongClickGroup: Group? = null
            var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            // Use applicationContext (not a LifecycleOwner) wrapped in theme
            // so that initViewModel() returns early without creating the default
            // ViewModel that crashes due to ClassCastException in fetchGroups()
            val themedContext = android.view.ContextThemeWrapper(
                requireContext().applicationContext,
                R.style.CometChatTheme_DayNight
            )
            val groupsView = CometChatGroups(themedContext).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }

            // Inject ViewModel BEFORE addView
            injectedDataSource?.let { ds ->
                val repository = GroupsRepositoryImpl(ds)
                val fetchUseCase = FetchGroupsUseCase(repository)
                val joinUseCase = JoinGroupUseCase(repository)
                val viewModel = CometChatGroupsViewModel(fetchUseCase, joinUseCase, enableListeners = false)
                groupsView.setViewModel(viewModel)
            }

            if (selectionMode != UIKitConstants.SelectionMode.NONE) {
                groupsView.setSelectionMode(selectionMode)
            }

            if (selectionMode == UIKitConstants.SelectionMode.NONE) {
                groupsView.setOnItemClick { group ->
                    println("  GroupsHostFragment: onItemClick guid='${group.guid}'")
                    onItemClickGroup = group
                }
            }
            groupsView.setOnItemLongClick { group ->
                println("  GroupsHostFragment: onItemLongClick guid='${group.guid}'")
                onItemLongClickGroup = group
            }

            return FrameLayout(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                addView(groupsView)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            println("  GroupsHostFragment.onViewCreated: View ready")
        }
    }
}
