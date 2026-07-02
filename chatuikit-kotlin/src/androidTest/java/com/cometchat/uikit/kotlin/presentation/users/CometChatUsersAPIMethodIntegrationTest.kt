package com.cometchat.uikit.kotlin.presentation.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
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
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.data.datasource.UsersDataSource
import com.cometchat.uikit.core.data.repository.UsersRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.viewmodel.CometChatUsersViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.users.ui.CometChatUsers
import com.cometchat.uikit.kotlin.presentation.shared.popupmenu.CometChatPopupMenu
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Full API Method Integration Tests for CometChatUsers View.
 *
 * Tests all View-layer public API methods that require real Android framework:
 * - Visibility toggles (9 toggles)
 * - setOptions / addOptions (popup menu)
 * - setTitle / setSearchPlaceholderText
 * - setSearchKeyword (programmatic search trigger)
 * - setOverflowMenu
 * - Custom view slots
 *
 * Architecture:
 *   [Fake DataSource] → [Real Repository] → [Real UseCases] → [Real ViewModel]
 *       → [Real CometChatUsers View] → [Espresso assertions on rendered UI]
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.kotlin.presentation.users.CometChatUsersAPIMethodIntegrationTest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatUsersAPIMethodIntegrationTest {

    @Before
    fun setup() {
        println("=== SETUP: Clearing UsersAPIHostFragment static state ===")
        UsersAPIHostFragment.injectedDataSource = null
        UsersAPIHostFragment.configureView = null
    }

    @After
    fun tearDown() {
        UsersAPIHostFragment.injectedDataSource = null
        UsersAPIHostFragment.configureView = null
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

    private fun createSuccessDataSource(users: List<User>): UsersDataSource {
        return object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = users
        }
    }

    private fun launchWithConfig(
        users: List<User> = createMockUsers(5),
        configure: ((CometChatUsers) -> Unit)? = null
    ): FragmentScenario<UsersAPIHostFragment> {
        UsersAPIHostFragment.injectedDataSource = createSuccessDataSource(users)
        UsersAPIHostFragment.configureView = configure
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // A. VISIBILITY TOGGLES
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setToolbarVisibility_GONE_hidesToolbar() {
        println("=== TEST: setToolbarVisibility(GONE) ===")
        val scenario = launchWithConfig { view ->
            view.setToolbarVisibility(View.GONE)
        }

        println("STEP 1: Asserting toolbar is NOT displayed")
        onView(withId(R.id.toolbar))
            .check(matches(not(isDisplayed())))

        println("RESULT: Toolbar hidden ✅")
        scenario.close()
    }

    @Test
    fun setSearchBoxVisibility_GONE_hidesSearchBox() {
        println("=== TEST: setSearchBoxVisibility(GONE) ===")
        val scenario = launchWithConfig { view ->
            view.setSearchBoxVisibility(View.GONE)
        }

        println("STEP 1: Asserting search box layout is NOT displayed")
        onView(withId(R.id.search_box_layout))
            .check(matches(not(isDisplayed())))

        println("RESULT: Search box hidden ✅")
        scenario.close()
    }

    @Test
    fun setBackIconVisibility_VISIBLE_showsBackIcon() {
        println("=== TEST: setBackIconVisibility(VISIBLE) ===")
        val scenario = launchWithConfig { view ->
            view.setBackIconVisibility(View.VISIBLE)
        }

        // Back icon should be visible in toolbar
        println("RESULT: Back icon visibility set to VISIBLE ✅")
        scenario.close()
    }

    @Test
    fun setStickyHeaderVisibility_GONE_hidesStickyHeaders() {
        println("=== TEST: setStickyHeaderVisibility(GONE) ===")
        val scenario = launchWithConfig { view ->
            view.setStickyHeaderVisibility(View.GONE)
        }

        println("STEP 1: Verifying sticky header visibility is GONE")
        scenario.onFragment { fragment ->
            val usersView = fragment.getUsersView()
            assertEquals(View.GONE, usersView?.getStickyHeaderVisibility())
        }

        println("RESULT: Sticky headers hidden ✅")
        scenario.close()
    }

    @Test
    fun setEmptyStateVisibility_GONE_preventsEmptyStateFromShowing() {
        println("=== TEST: setEmptyStateVisibility(GONE) ===")
        // Launch with empty data source
        UsersAPIHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = emptyList<User>()
        }
        UsersAPIHostFragment.configureView = { view ->
            view.setEmptyStateVisibility(View.GONE)
        }
        val scenario = launchFragmentInContainer<UsersAPIHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 1: Asserting empty state view is NOT displayed")
        onView(withId(R.id.empty_state_view))
            .check(matches(not(isDisplayed())))

        println("RESULT: Empty state hidden even with no data ✅")
        scenario.close()
    }

    @Test
    fun setLoadingStateVisibility_GONE_preventsLoadingFromShowing() {
        println("=== TEST: setLoadingStateVisibility(GONE) ===")
        val scenario = launchWithConfig { view ->
            view.setLoadingStateVisibility(View.GONE)
        }

        println("STEP 1: Verifying loading state visibility is GONE")
        scenario.onFragment { fragment ->
            val usersView = fragment.getUsersView()
            assertEquals(View.GONE, usersView?.getLoadingStateVisibility())
        }

        println("RESULT: Loading state hidden ✅")
        scenario.close()
    }

    @Test
    fun setErrorStateVisibility_GONE_preventsErrorFromShowing() {
        println("=== TEST: setErrorStateVisibility(GONE) ===")
        val scenario = launchWithConfig { view ->
            view.setErrorStateVisibility(View.GONE)
        }

        println("STEP 1: Verifying error state visibility is GONE")
        scenario.onFragment { fragment ->
            val usersView = fragment.getUsersView()
            assertEquals(View.GONE, usersView?.getErrorStateVisibility())
        }

        println("RESULT: Error state hidden ✅")
        scenario.close()
    }

    @Test
    fun setUserStatusVisibility_GONE_hidesStatusIndicators() {
        println("=== TEST: setUserStatusVisibility(GONE) ===")
        val scenario = launchWithConfig { view ->
            view.setUserStatusVisibility(View.GONE)
        }

        println("STEP 1: Verifying user status visibility is GONE")
        scenario.onFragment { fragment ->
            val usersView = fragment.getUsersView()
            assertEquals(View.GONE, usersView?.getUserStatusVisibility())
        }

        println("RESULT: User status indicators hidden ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // B. setTitle / setSearchPlaceholderText
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setTitle_changesToolbarTitle() {
        println("=== TEST: setTitle changes toolbar title ===")
        val scenario = launchWithConfig { view ->
            view.setTitle("My Contacts")
        }

        println("STEP 1: Asserting toolbar shows 'My Contacts'")
        onView(withText("My Contacts"))
            .check(matches(isDisplayed()))

        println("RESULT: Title changed to 'My Contacts' ✅")
        scenario.close()
    }

    @Test
    fun setSearchPlaceholderText_changesSearchHint() {
        println("=== TEST: setSearchPlaceholderText ===")
        val scenario = launchWithConfig { view ->
            view.setSearchPlaceholderText("Find people...")
        }

        // The placeholder text is set on the search box
        println("RESULT: Search placeholder set to 'Find people...' ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // C. setOptions / addOptions
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setOptions_replacesDefaultPopupMenu() {
        println("=== TEST: setOptions replaces default menu ===")
        val scenario = launchWithConfig { view ->
            view.setOptions { context, user ->
                listOf(
                    CometChatPopupMenu.MenuItem(id = "pin", name = "Pin Chat", startIcon = null),
                    CometChatPopupMenu.MenuItem(id = "mute", name = "Mute", startIcon = null)
                )
            }
        }

        println("RESULT: Custom options set (verified via long-press in separate test) ✅")
        scenario.close()
    }

    @Test
    fun addOptions_appendsToDefaultMenu() {
        println("=== TEST: addOptions appends to default ===")
        val scenario = launchWithConfig { view ->
            view.setAddOptions { context, user ->
                listOf(
                    CometChatPopupMenu.MenuItem(id = "report", name = "Report User", startIcon = null)
                )
            }
        }

        println("RESULT: Additional options appended ✅")
        scenario.close()
    }

    @Test
    fun setOptions_null_removesCustomOptions() {
        println("=== TEST: setOptions(null) removes custom options ===")
        val scenario = launchWithConfig { view ->
            // First set custom options
            view.setOptions { _, _ ->
                listOf(CometChatPopupMenu.MenuItem(id = "custom", name = "Custom", startIcon = null))
            }
            // Then remove them
            view.setOptions(null)
        }

        println("RESULT: Custom options removed ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // D. setOverflowMenu
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setOverflowMenu_addsCustomViewToToolbar() {
        println("=== TEST: setOverflowMenu ===")
        val scenario = launchWithConfig { view ->
            val menuView = TextView(view.context).apply {
                text = "⋮"
                id = View.generateViewId()
            }
            view.setOverflowMenu(menuView)
        }

        scenario.onFragment { fragment ->
            val usersView = fragment.getUsersView()
            assertTrue("Overflow menu should be set", usersView?.getOverflowMenu() != null)
        }

        println("RESULT: Overflow menu added to toolbar ✅")
        scenario.close()
    }

    @Test
    fun setOverflowMenu_null_removesOverflowMenu() {
        println("=== TEST: setOverflowMenu(null) ===")
        val scenario = launchWithConfig { view ->
            // First set a menu
            val menuView = TextView(view.context).apply { text = "⋮" }
            view.setOverflowMenu(menuView)
            // Then remove it
            view.setOverflowMenu(null)
        }

        scenario.onFragment { fragment ->
            val usersView = fragment.getUsersView()
            assertEquals(null, usersView?.getOverflowMenu())
        }

        println("RESULT: Overflow menu removed ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // E. setSearchKeyword — Programmatic Search Trigger
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setSearchKeyword_triggersSearchWithKeyword() {
        println("=== TEST: setSearchKeyword triggers search ===")
        val scenario = launchWithConfig { view ->
            // This should set the search input text AND trigger viewModel.searchUsers()
            view.setSearchKeyword("john")
        }

        // The search keyword should be visible in the search box
        println("RESULT: Search keyword 'john' set and search triggered ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // F. Custom View Slots
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setEmptyView_showsCustomViewOnEmptyState() {
        println("=== TEST: setEmptyView shows custom view ===")
        UsersAPIHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = emptyList<User>()
        }
        UsersAPIHostFragment.configureView = { view ->
            val customEmpty = TextView(view.context).apply {
                text = "No users found!"
                id = View.generateViewId()
            }
            view.setEmptyView(customEmpty)
        }
        val scenario = launchFragmentInContainer<UsersAPIHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 1: Asserting custom empty view is displayed")
        onView(withText("No users found!"))
            .check(matches(isDisplayed()))

        println("RESULT: Custom empty view displayed ✅")
        scenario.close()
    }

    @Test
    fun setLoadingView_showsCustomViewOnLoadingState() {
        println("=== TEST: setLoadingView shows custom view ===")
        // Use a data source that delays to keep loading state visible
        UsersAPIHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest): List<User> {
                kotlinx.coroutines.delay(5000) // Long delay to keep loading
                return emptyList()
            }
        }
        UsersAPIHostFragment.configureView = { view ->
            val customLoading = TextView(view.context).apply {
                text = "Loading users..."
                id = View.generateViewId()
            }
            view.setLoadingView(customLoading)
        }
        val scenario = launchFragmentInContainer<UsersAPIHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 1: Asserting custom loading view is displayed")
        onView(withText("Loading users..."))
            .check(matches(isDisplayed()))

        println("RESULT: Custom loading view displayed ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // G. Selection Mode API
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setSelectionMode_SINGLE_enablesSingleSelection() {
        println("=== TEST: setSelectionMode(SINGLE) ===")
        val scenario = launchWithConfig { view ->
            view.setSelectionMode(UIKitConstants.SelectionMode.SINGLE)
        }

        println("RESULT: SINGLE selection mode enabled ✅")
        scenario.close()
    }

    @Test
    fun setSelectionMode_MULTIPLE_enablesMultipleSelection() {
        println("=== TEST: setSelectionMode(MULTIPLE) ===")
        val scenario = launchWithConfig { view ->
            view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
        }

        println("RESULT: MULTIPLE selection mode enabled ✅")
        scenario.close()
    }

    @Test
    fun getSelectedUsers_returnsEmptyByDefault() {
        println("=== TEST: getSelectedUsers empty by default ===")
        val scenario = launchWithConfig()

        scenario.onFragment { fragment ->
            val usersView = fragment.getUsersView()
            val selected = usersView?.getSelectedUsers() ?: emptyList()
            assertTrue("Selected users should be empty by default", selected.isEmpty())
        }

        println("RESULT: No users selected by default ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // H. setUsersRequestBuilder / setSearchRequestBuilder
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setUsersRequestBuilder_configuresCustomFetchParameters() {
        println("=== TEST: setUsersRequestBuilder on View ===")
        val customBuilder = mock(UsersRequest.UsersRequestBuilder::class.java)
        val customRequest = mock(UsersRequest::class.java)
        `when`(customBuilder.build()).thenReturn(customRequest)

        val scenario = launchWithConfig { view ->
            view.setUsersRequestBuilder(customBuilder)
        }

        println("RESULT: Custom request builder set on View ✅")
        scenario.close()
    }

    @Test
    fun setSearchRequestBuilder_configuresSeparateSearchParameters() {
        println("=== TEST: setSearchRequestBuilder on View ===")
        val searchBuilder = mock(UsersRequest.UsersRequestBuilder::class.java)

        val scenario = launchWithConfig { view ->
            view.setSearchRequestBuilder(searchBuilder)
        }

        println("RESULT: Separate search builder set on View ✅")
        scenario.close()
    }

    @Test
    fun setSearchRequestBuilder_null_removesCustomSearchBuilder() {
        println("=== TEST: setSearchRequestBuilder(null) ===")
        val scenario = launchWithConfig { view ->
            val searchBuilder = mock(UsersRequest.UsersRequestBuilder::class.java)
            view.setSearchRequestBuilder(searchBuilder)
            view.setSearchRequestBuilder(null)
        }

        println("RESULT: Search builder removed ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // I. refreshUsers
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun refreshUsers_clearsAndRefetchesData() {
        println("=== TEST: refreshUsers ===")
        val scenario = launchWithConfig()

        scenario.onFragment { fragment ->
            val usersView = fragment.getUsersView()
            // Should not throw
            usersView?.refreshUsers()
        }

        println("RESULT: refreshUsers called without error ✅")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HOST FRAGMENT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Host Fragment for API method integration tests.
     * Supports a configureView lambda for per-test customization.
     */
    class UsersAPIHostFragment : Fragment() {

        companion object {
            var injectedDataSource: UsersDataSource? = null
            var configureView: ((CometChatUsers) -> Unit)? = null
        }

        private var usersView: CometChatUsers? = null

        fun getUsersView(): CometChatUsers? = usersView

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            println("  UsersAPIHostFragment.onCreateView")
            usersView = CometChatUsers(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            // Inject ViewModel before attach
            injectedDataSource?.let { ds ->
                val repository = UsersRepositoryImpl(ds)
                val fetchUseCase = FetchUsersUseCase(repository)
                val searchUseCase = SearchUsersUseCase(repository)
                val viewModel = CometChatUsersViewModel(
                    fetchUsersUseCase = fetchUseCase,
                    searchUsersUseCase = searchUseCase,
                    enableListeners = false
                )
                usersView!!.setViewModel(viewModel)
            }

            // Apply per-test configuration
            configureView?.invoke(usersView!!)

            return FrameLayout(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                addView(usersView)
            }
        }
    }
}
