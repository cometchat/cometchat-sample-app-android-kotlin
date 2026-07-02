package com.cometchat.uikit.kotlin.presentation.users

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
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * True UI Integration Test for CometChatUsers (XML/Kotlin).
 *
 * This test actually inflates the CometChatUsers view inside a Fragment,
 * injects a ViewModel backed by a fake DataSource, and uses Espresso to assert
 * on the rendered UI (RecyclerView items, state views, click interactions).
 *
 * Architecture:
 *   [Fake DataSource] → [Real Repository] → [Real UseCases] → [Real ViewModel]
 *       → [Real CometChatUsers View] → [Espresso assertions on rendered UI]
 *
 * The ONLY fake is the DataSource at the SDK boundary. Everything else is real
 * production code — Repository, UseCases, ViewModel, View, Adapter, ViewHolder.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.kotlin.presentation.users.CometChatUsersViewIntegrationTest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatUsersViewIntegrationTest {

    @Before
    fun setup() {
        println("=== SETUP: Clearing UsersHostFragment static state ===")
        UsersHostFragment.injectedDataSource = null
        UsersHostFragment.onItemClickUser = null
        UsersHostFragment.onItemLongClickUser = null
        UsersHostFragment.onErrorException = null
    }

    @After
    fun tearDown() {
        println("=== TEARDOWN: Clearing UsersHostFragment static state ===")
        UsersHostFragment.injectedDataSource = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mock Factory: Create SDK objects with Mockito (SDK has private constructors)
    // ─────────────────────────────────────────────────────────────────────────

    private fun createMockUser(uid: String, name: String, status: String = "online"): User {
        val user = mock(User::class.java)
        `when`(user.uid).thenReturn(uid)
        `when`(user.name).thenReturn(name)
        `when`(user.status).thenReturn(status)
        `when`(user.avatar).thenReturn(null)
        `when`(user.isBlockedByMe).thenReturn(false)
        `when`(user.isHasBlockedMe).thenReturn(false)
        return user
    }

    private fun createMockUsers(count: Int): List<User> {
        return (1..count).map { i ->
            createMockUser(uid = "user-$i", userName = "User $i")
        }
    }

    private fun createMockUser(uid: String, userName: String): User {
        return createMockUser(uid = uid, name = userName)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: Launch the fragment with injected DataSource
    // ─────────────────────────────────────────────────────────────────────────

    private fun launchWithDataSource(
        dataSource: UsersDataSource
    ): FragmentScenario<UsersHostFragment> {
        UsersHostFragment.injectedDataSource = dataSource
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Content state renders user items in RecyclerView
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun contentState_rendersUserItems_inRecyclerView() {
        println("=== TEST: contentState_rendersUserItems_inRecyclerView ===")
        println("STEP 1: Creating fake DataSource with 3 users")
        val users = createMockUsers(3)
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }

        println("STEP 2: Launching fragment with fake DataSource")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 3: Asserting RecyclerView is displayed")
        onView(withId(R.id.recyclerview_users_list))
            .check(matches(isDisplayed()))

        println("STEP 4: Asserting each user name is rendered")
        onView(withId(R.id.recyclerview_users_list))
            .check(matches(hasDescendant(withText("User 1"))))
        onView(withId(R.id.recyclerview_users_list))
            .check(matches(hasDescendant(withText("User 2"))))
        onView(withId(R.id.recyclerview_users_list))
            .check(matches(hasDescendant(withText("User 3"))))

        println("RESULT: All 3 users rendered successfully in RecyclerView")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Empty state shows empty view when DataSource returns empty list
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyState_showsEmptyView_whenNoUsers() {
        println("=== TEST: emptyState_showsEmptyView_whenNoUsers ===")
        println("STEP 1: Creating fake DataSource returning empty list")
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = emptyList<User>()
        }

        println("STEP 2: Launching fragment with empty DataSource")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 3: Asserting empty state view is displayed")
        onView(withId(R.id.empty_state_view))
            .check(matches(isDisplayed()))

        println("STEP 4: Asserting RecyclerView is NOT displayed")
        onView(withId(R.id.recyclerview_users_list))
            .check(matches(not(isDisplayed())))

        println("RESULT: Empty state displayed correctly when no users")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Error state shows error view when DataSource throws
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_showsErrorView_whenDataSourceThrows() {
        println("=== TEST: errorState_showsErrorView_whenDataSourceThrows ===")
        println("STEP 1: Creating fake DataSource that throws CometChatException")
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest): List<User> {
                throw CometChatException("NET_ERR", "Network error")
            }
        }

        println("STEP 2: Launching fragment with failing DataSource")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 3: Asserting error state view is displayed")
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        println("STEP 4: Asserting retry button is displayed")
        onView(withId(R.id.btn_retry))
            .check(matches(isDisplayed()))

        println("STEP 5: Asserting RecyclerView is NOT displayed")
        onView(withId(R.id.recyclerview_users_list))
            .check(matches(not(isDisplayed())))

        println("RESULT: Error state with retry button displayed correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Item click triggers onItemClick callback with correct User
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun itemClick_triggersCallback_withCorrectUser() {
        println("=== TEST: itemClick_triggersCallback_withCorrectUser ===")
        println("STEP 1: Creating fake DataSource with 3 users")
        val users = createMockUsers(3)
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }

        println("STEP 2: Launching fragment")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 3: Clicking item at position 0")
        onView(withId(R.id.recyclerview_users_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )

        println("STEP 4: Asserting onItemClick callback was triggered")
        assertNotNull(
            "onItemClick callback was not triggered",
            UsersHostFragment.onItemClickUser
        )

        println("STEP 5: Asserting correct User UID was delivered")
        assertEquals(
            "user-1",
            UsersHostFragment.onItemClickUser?.uid
        )

        println("RESULT: Item click delivered User with UID='user-1' to callback")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Item long click triggers onItemLongClick callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun itemLongClick_triggersCallback_withCorrectUser() {
        println("=== TEST: itemLongClick_triggersCallback_withCorrectUser ===")
        println("STEP 1: Creating fake DataSource with 3 users")
        val users = createMockUsers(3)
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }

        println("STEP 2: Launching fragment")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 3: Long-clicking item at position 1")
        onView(withId(R.id.recyclerview_users_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, longClick())
            )

        println("STEP 4: Asserting onItemLongClick callback was triggered")
        assertNotNull(
            "onItemLongClick callback was not triggered",
            UsersHostFragment.onItemLongClickUser
        )

        println("STEP 5: Asserting correct User UID was delivered")
        assertEquals(
            "user-2",
            UsersHostFragment.onItemLongClickUser?.uid
        )

        println("RESULT: Long click delivered User with UID='user-2' to callback")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Retry button triggers re-fetch and transitions to content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun retryButton_triggersFetch_andShowsContent() {
        println("=== TEST: retryButton_triggersFetch_andShowsContent ===")
        var callCount = 0
        val users = createMockUsers(2)
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest): List<User> {
                callCount++
                println("  DataSource.fetchUsers called (attempt #$callCount)")
                if (callCount == 1) {
                    throw CometChatException("NET_ERR", "Network error")
                }
                return users
            }
        }

        println("STEP 1: Launching fragment (first fetch will fail)")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 2: Verifying error state is displayed")
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        println("STEP 3: Clicking retry button")
        onView(withId(R.id.btn_retry))
            .perform(click())

        println("STEP 4: Asserting content is now displayed")
        onView(withId(R.id.recyclerview_users_list))
            .check(matches(isDisplayed()))
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        println("RESULT: Retry successfully transitioned from Error → Content state")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Toolbar is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun toolbar_isVisible() {
        println("=== TEST: toolbar_isVisible ===")
        val users = createMockUsers(1)
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }

        println("STEP 1: Launching fragment with 1 user")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 2: Asserting toolbar is displayed")
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        println("RESULT: Toolbar is visible")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Search box is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun searchBox_isVisible() {
        println("=== TEST: searchBox_isVisible ===")
        val users = createMockUsers(1)
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }

        println("STEP 1: Launching fragment with 1 user")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 2: Asserting search box layout is displayed")
        onView(withId(R.id.search_box_layout))
            .check(matches(isDisplayed()))

        println("RESULT: Search box is visible")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: RecyclerView has correct item count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun recyclerView_hasCorrectItemCount() {
        println("=== TEST: recyclerView_hasCorrectItemCount ===")
        println("STEP 1: Creating fake DataSource with 5 users")
        val users = createMockUsers(5)
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }

        println("STEP 2: Launching fragment")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 3: Asserting RecyclerView adapter has 5 items")
        onView(withId(R.id.recyclerview_users_list))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) throw noViewFoundException
                val recyclerView = view as RecyclerView
                assertEquals(
                    "RecyclerView should have 5 items",
                    5,
                    recyclerView.adapter?.itemCount
                )
                println("RESULT: RecyclerView adapter itemCount = ${recyclerView.adapter?.itemCount}")
            }

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 10: Clicking different items returns different users
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun clickingDifferentItems_returnsDifferentUsers() {
        println("=== TEST: clickingDifferentItems_returnsDifferentUsers ===")
        val users = createMockUsers(3)
        val dataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }

        println("STEP 1: Launching fragment with 3 users")
        val scenario = launchWithDataSource(dataSource)

        println("STEP 2: Clicking item at position 2")
        onView(withId(R.id.recyclerview_users_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click())
            )

        println("STEP 3: Asserting correct User UID for position 2")
        assertEquals(
            "user-3",
            UsersHostFragment.onItemClickUser?.uid
        )

        println("RESULT: Position 2 click delivered User with UID='user-3'")
        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment: Wraps CometChatUsers for testing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A minimal Fragment that hosts CometChatUsers.
     *
     * The DataSource is injected via the companion object before launch.
     * The ViewModel is built inside onViewCreated (on the main thread) to ensure
     * proper coroutine dispatching with viewModelScope.
     */
    class UsersHostFragment : Fragment() {

        companion object {
            /** Injected before fragment launch — cleared after each test */
            var injectedDataSource: UsersDataSource? = null
            var onItemClickUser: User? = null
            var onItemLongClickUser: User? = null
            var onErrorException: CometChatException? = null
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            println("  UsersHostFragment.onCreateView: Creating CometChatUsers view")
            val usersView = CometChatUsers(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            // Inject ViewModel BEFORE the view is attached to the window.
            // CometChatUsers.onAttachedToWindow() calls viewModel.fetchUsers(),
            // so the test ViewModel must be set before addView() triggers attachment.
            injectedDataSource?.let { ds ->
                println("  UsersHostFragment: DataSource injected, building chain BEFORE attach")
                val repository = UsersRepositoryImpl(ds)
                val fetchUseCase = FetchUsersUseCase(repository)
                val searchUseCase = SearchUsersUseCase(repository)
                val viewModel = CometChatUsersViewModel(
                    fetchUsersUseCase = fetchUseCase,
                    searchUsersUseCase = searchUseCase,
                    enableListeners = false
                )
                usersView.setViewModel(viewModel)
                println("  UsersHostFragment: ViewModel set on CometChatUsers view")
            }

            // Wire callbacks for assertion
            usersView.setOnItemClick { user ->
                println("  UsersHostFragment: onItemClick fired with UID='${user.uid}'")
                onItemClickUser = user
            }
            usersView.setOnItemLongClick { user ->
                println("  UsersHostFragment: onItemLongClick fired with UID='${user.uid}'")
                onItemLongClickUser = user
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
            println("  UsersHostFragment.onViewCreated: View ready")
        }
    }
}
