package com.cometchat.uikit.kotlin.presentation.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.UsersDataSource
import com.cometchat.uikit.core.data.repository.UsersRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.users.ui.CometChatUsers
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Custom View Slot Tests for CometChatUsers.
 *
 * Verifies that custom views replace default content when set via:
 * - setEmptyView / setErrorView / setLoadingView
 * - setOverflowMenu
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.kotlin.presentation.users.CometChatUsersCustomViewTest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatUsersCustomViewTest {

    @Before
    fun setup() {
        println("=== SETUP: Clearing CustomViewHostFragment state ===")
        CustomViewHostFragment.injectedDataSource = null
        CustomViewHostFragment.customEmptyView = null
        CustomViewHostFragment.customErrorView = null
    }

    @After
    fun tearDown() {
        CustomViewHostFragment.injectedDataSource = null
    }

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

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Custom empty view replaces default empty state
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customEmptyView_replacesDefaultEmptyState() {
        println("=== TEST: customEmptyView_replacesDefaultEmptyState ===")
        println("STEP 1: Setting up custom empty view with text 'No contacts available'")
        CustomViewHostFragment.useCustomEmptyView = true
        CustomViewHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = emptyList<User>()
        }

        println("STEP 2: Launching fragment")
        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 3: Asserting custom empty view text is displayed")
        onView(withText("No contacts available")).check(matches(isDisplayed()))

        println("RESULT: Custom empty view rendered correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Content state hides empty view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun contentState_hidesEmptyView() {
        println("=== TEST: contentState_hidesEmptyView ===")
        val users = (1..3).map { i -> createMockUser("user-$i", "User $i") }
        CustomViewHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }
        CustomViewHostFragment.useCustomEmptyView = false

        println("STEP 1: Launching fragment with 3 users")
        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 2: Asserting empty state view is NOT displayed")
        onView(withId(R.id.empty_state_view)).check(matches(not(isDisplayed())))

        println("STEP 3: Asserting RecyclerView IS displayed")
        onView(withId(R.id.recyclerview_users_list)).check(matches(isDisplayed()))

        println("RESULT: Content state correctly hides empty view")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Overflow menu is displayed when set
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun overflowMenu_isDisplayed_whenSet() {
        println("=== TEST: overflowMenu_isDisplayed_whenSet ===")
        CustomViewHostFragment.useOverflowMenu = true
        val users = (1..2).map { i -> createMockUser("user-$i", "User $i") }
        CustomViewHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }

        println("STEP 1: Launching fragment with overflow menu")
        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 2: Asserting overflow menu text is displayed")
        onView(withText("MENU")).check(matches(isDisplayed()))

        println("RESULT: Custom overflow menu rendered in toolbar")
        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment
    // ─────────────────────────────────────────────────────────────────────────

    class CustomViewHostFragment : Fragment() {

        companion object {
            var injectedDataSource: UsersDataSource? = null
            var customEmptyView: View? = null
            var customErrorView: View? = null
            var useCustomEmptyView: Boolean = false
            var useOverflowMenu: Boolean = false
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
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

            // Set custom empty view if requested
            if (useCustomEmptyView) {
                val customEmpty = TextView(requireContext()).apply {
                    text = "No contacts available"
                    textSize = 18f
                }
                usersView.setEmptyView(customEmpty)
                println("  CustomViewHostFragment: Custom empty view set")
            }

            // Set overflow menu if requested
            if (useOverflowMenu) {
                val menuView = TextView(requireContext()).apply {
                    text = "MENU"
                    textSize = 14f
                }
                usersView.setOverflowMenu(menuView)
                println("  CustomViewHostFragment: Overflow menu set")
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
            println("  CustomViewHostFragment.onViewCreated: View ready")
        }
    }
}
