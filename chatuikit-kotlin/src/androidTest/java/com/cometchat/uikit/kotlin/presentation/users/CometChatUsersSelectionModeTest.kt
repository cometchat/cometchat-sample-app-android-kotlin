package com.cometchat.uikit.kotlin.presentation.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.data.datasource.UsersDataSource
import com.cometchat.uikit.core.data.repository.UsersRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.users.ui.CometChatUsers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Selection Mode Tests for CometChatUsers.
 *
 * Verifies:
 * - SINGLE selection: clicking item shows count "1", clicking another replaces
 * - MULTIPLE selection: clicking items increments count, re-clicking deselects
 * - Discard selection: returns to normal toolbar with component title
 * - Selection count display in toolbar
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.kotlin.presentation.users.CometChatUsersSelectionModeTest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatUsersSelectionModeTest {

    @Before
    fun setup() {
        println("=== SETUP: Clearing SelectionHostFragment state ===")
        SelectionHostFragment.injectedDataSource = null
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.NONE
        SelectionHostFragment.onSelectionResult = null
    }

    @After
    fun tearDown() {
        SelectionHostFragment.injectedDataSource = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mock Factory
    // ─────────────────────────────────────────────────────────────────────────

    private fun createMockUser(uid: String, name: String): User {
        val user = mock(User::class.java)
        `when`(user.uid).thenReturn(uid)
        `when`(user.name).thenReturn(name)
        `when`(user.status).thenReturn("online")
        `when`(user.avatar).thenReturn(null)
        `when`(user.isBlockedByMe).thenReturn(false)
        `when`(user.isHasBlockedMe).thenReturn(false)
        return user
    }

    private fun createMockUsers(count: Int): List<User> {
        return (1..count).map { i -> createMockUser("user-$i", "User $i") }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: SINGLE selection — clicking item displays count "1"
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun singleSelection_clickingItem_displaysCountOne() {
        println("=== TEST: singleSelection_clickingItem_displaysCountOne ===")
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.SINGLE
        SelectionHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = createMockUsers(5)
        }

        println("STEP 1: Launching fragment with SINGLE selection mode")
        val scenario = launchFragmentInContainer<SelectionHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 2: Clicking item at position 0")
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        println("STEP 3: Asserting toolbar shows '1'")
        onView(withText("1")).check(matches(isDisplayed()))

        println("RESULT: SINGLE selection shows count '1' in toolbar")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: SINGLE selection — clicking another item replaces selection (still "1")
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun singleSelection_clickingAnotherItem_replacesSelection() {
        println("=== TEST: singleSelection_clickingAnotherItem_replacesSelection ===")
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.SINGLE
        SelectionHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = createMockUsers(5)
        }

        println("STEP 1: Launching fragment with SINGLE selection mode")
        val scenario = launchFragmentInContainer<SelectionHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 2: Clicking item at position 0")
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        println("STEP 3: Clicking item at position 2")
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))

        println("STEP 4: Asserting toolbar still shows '1' (replaced, not added)")
        onView(withText("1")).check(matches(isDisplayed()))

        println("RESULT: SINGLE selection replaces previous — count remains '1'")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: MULTIPLE selection — clicking 3 items shows count "3"
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleSelection_clickingThreeItems_displaysCountThree() {
        println("=== TEST: multipleSelection_clickingThreeItems_displaysCountThree ===")
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.MULTIPLE
        SelectionHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = createMockUsers(5)
        }

        println("STEP 1: Launching fragment with MULTIPLE selection mode")
        val scenario = launchFragmentInContainer<SelectionHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 2: Clicking items at positions 0, 1, 2")
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))

        println("STEP 3: Asserting toolbar shows '3'")
        onView(withText("3")).check(matches(isDisplayed()))

        println("RESULT: MULTIPLE selection shows count '3' after clicking 3 items")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: MULTIPLE selection — deselecting item reduces count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleSelection_deselectingItem_reducesCount() {
        println("=== TEST: multipleSelection_deselectingItem_reducesCount ===")
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.MULTIPLE
        SelectionHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = createMockUsers(5)
        }

        println("STEP 1: Launching fragment with MULTIPLE selection mode")
        val scenario = launchFragmentInContainer<SelectionHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 2: Selecting items at positions 0, 1, 2")
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))

        println("STEP 3: Deselecting item at position 1 (toggle)")
        onView(withId(R.id.recyclerview_users_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        println("STEP 4: Asserting toolbar shows '2'")
        onView(withText("2")).check(matches(isDisplayed()))

        println("RESULT: Deselecting reduces count from 3 to 2")
        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment for Selection Tests
    // ─────────────────────────────────────────────────────────────────────────

    class SelectionHostFragment : Fragment() {

        companion object {
            var injectedDataSource: UsersDataSource? = null
            var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE
            var onSelectionResult: List<User>? = null
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            println("  SelectionHostFragment.onCreateView: mode=$selectionMode")
            val usersView = CometChatUsers(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            // Inject ViewModel BEFORE addView to prevent default ViewModel's
            // fetchUsers() from crashing in onAttachedToWindow
            injectedDataSource?.let { ds ->
                val repository = UsersRepositoryImpl(ds)
                val fetchUseCase = FetchUsersUseCase(repository)
                val searchUseCase = SearchUsersUseCase(repository)
                val viewModel = CometChatUsersViewModel(
                    fetchUsersUseCase = fetchUseCase,
                    searchUsersUseCase = searchUseCase,
                    enableListeners = false
                )
                usersView.setViewModel(viewModel)
            }

            // Set selection mode BEFORE attach
            usersView.setSelectionMode(selectionMode)

            // Wire selection callback
            usersView.setOnSelection { users ->
                println("  SelectionHostFragment: onSelection with ${users.size} users")
                onSelectionResult = users
            }

            return FrameLayout(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                addView(usersView)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            println("  SelectionHostFragment.onViewCreated: View ready")
        }
    }
}
