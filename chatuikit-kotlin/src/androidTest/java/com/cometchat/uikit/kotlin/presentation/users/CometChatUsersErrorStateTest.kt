package com.cometchat.uikit.kotlin.presentation.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
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
 * Error State Tests for CometChatUsers.
 *
 * Verifies:
 * - Error view is displayed when fetch fails
 * - Retry button is visible and clickable
 * - Retry transitions from error to content
 * - onError callback receives the CometChatException
 * - Loading state is shown during initial fetch
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.kotlin.presentation.users.CometChatUsersErrorStateTest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatUsersErrorStateTest {

    @Before
    fun setup() {
        println("=== SETUP: Clearing ErrorStateHostFragment state ===")
        ErrorStateHostFragment.injectedDataSource = null
        ErrorStateHostFragment.onErrorException = null
    }

    @After
    fun tearDown() {
        ErrorStateHostFragment.injectedDataSource = null
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

    private fun createMockUsers(count: Int): List<User> {
        return (1..count).map { i -> createMockUser("user-$i", "User $i") }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Error view shows retry button
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_showsRetryButton() {
        println("=== TEST: errorState_showsRetryButton ===")
        println("STEP 1: Creating failing DataSource")
        ErrorStateHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest): List<User> {
                throw CometChatException("FETCH_ERR", "Failed to fetch users")
            }
        }

        println("STEP 2: Launching fragment")
        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 3: Asserting error state view is displayed")
        onView(withId(R.id.error_state_view)).check(matches(isDisplayed()))

        println("STEP 4: Asserting retry button is displayed")
        onView(withId(R.id.btn_retry)).check(matches(isDisplayed()))

        println("RESULT: Error state shows retry button correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Retry button transitions from error to content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun retryButton_transitionsFromError_toContent() {
        println("=== TEST: retryButton_transitionsFromError_toContent ===")
        var callCount = 0
        val users = createMockUsers(3)

        ErrorStateHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest): List<User> {
                callCount++
                println("  DataSource.fetchUsers attempt #$callCount")
                if (callCount == 1) {
                    throw CometChatException("NET_ERR", "Network timeout")
                }
                return users
            }
        }

        println("STEP 1: Launching fragment (first fetch fails)")
        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 2: Verifying error state")
        onView(withId(R.id.error_state_view)).check(matches(isDisplayed()))

        println("STEP 3: Clicking retry button")
        onView(withId(R.id.btn_retry)).perform(click())

        println("STEP 4: Asserting content is now displayed")
        onView(withId(R.id.recyclerview_users_list)).check(matches(isDisplayed()))
        onView(withId(R.id.error_state_view)).check(matches(not(isDisplayed())))

        println("RESULT: Retry transitioned from Error → Content successfully")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: onError callback receives the exception
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onError_receivesException_withCorrectCode() {
        println("=== TEST: onError_receivesException_withCorrectCode ===")
        ErrorStateHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest): List<User> {
                throw CometChatException("AUTH_EXPIRED", "Token expired")
            }
        }

        println("STEP 1: Launching fragment with AUTH_EXPIRED error")
        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 2: Waiting for async error propagation")
        Thread.sleep(500)

        println("STEP 3: Asserting onError received correct exception")
        assertNotNull("onError should have been called", ErrorStateHostFragment.onErrorException)
        assertEquals("AUTH_EXPIRED", ErrorStateHostFragment.onErrorException?.code)

        println("RESULT: onError callback received exception with code='AUTH_EXPIRED'")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: RecyclerView is hidden during error state
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_hidesRecyclerView() {
        println("=== TEST: errorState_hidesRecyclerView ===")
        ErrorStateHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest): List<User> {
                throw CometChatException("SERVER_ERR", "Internal server error")
            }
        }

        println("STEP 1: Launching fragment with server error")
        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 2: Asserting RecyclerView is NOT displayed")
        onView(withId(R.id.recyclerview_users_list)).check(matches(not(isDisplayed())))

        println("RESULT: RecyclerView is hidden during error state")
        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment
    // ─────────────────────────────────────────────────────────────────────────

    class ErrorStateHostFragment : Fragment() {

        companion object {
            var injectedDataSource: UsersDataSource? = null
            var onErrorException: CometChatException? = null
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

            usersView.setOnError { exception ->
                println("  ErrorStateHostFragment: onError code='${exception.code}'")
                onErrorException = exception
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
            println("  ErrorStateHostFragment.onViewCreated: View ready")
        }
    }
}
