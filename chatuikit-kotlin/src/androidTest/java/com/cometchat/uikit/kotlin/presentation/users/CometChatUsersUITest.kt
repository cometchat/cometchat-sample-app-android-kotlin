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
import com.cometchat.chat.exceptions.CometChatException
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * UI Tests for CometChatUsers — verifies callbacks, selection mode,
 * and error handling behaviors via Espresso.
 *
 * Architecture:
 *   [Fake DataSource] → [Real Repository] → [Real UseCases] → [Real ViewModel]
 *       → [Real CometChatUsers View] → [Espresso assertions]
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.kotlin.presentation.users.CometChatUsersUITest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatUsersUITest {

    @Before
    fun setup() {
        println("=== SETUP: Clearing UsersUIHostFragment static state ===")
        UsersUIHostFragment.injectedDataSource = null
        UsersUIHostFragment.onItemClickUser = null
        UsersUIHostFragment.onSelectionList = null
        UsersUIHostFragment.onErrorException = null
        UsersUIHostFragment.onEmptyFired = false
        UsersUIHostFragment.onLoadList = null
        UsersUIHostFragment.selectionMode = UIKitConstants.SelectionMode.NONE
    }

    @After
    fun tearDown() {
        UsersUIHostFragment.injectedDataSource = null
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

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: onError callback fires when DataSource throws
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onError_firesWithException_whenFetchFails() {
        println("=== TEST: onError_firesWithException_whenFetchFails ===")
        println("STEP 1: Creating failing DataSource")
        UsersUIHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest): List<User> {
                throw CometChatException("AUTH_ERR", "Authentication failed")
            }
        }

        println("STEP 2: Launching fragment")
        val scenario = launchFragmentInContainer<UsersUIHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 3: Asserting onError callback was fired")
        // Give time for async operation
        Thread.sleep(500)
        assertNotNull("onError should have been called", UsersUIHostFragment.onErrorException)
        assertEquals("AUTH_ERR", UsersUIHostFragment.onErrorException?.code)

        println("RESULT: onError fired with code='AUTH_ERR'")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: onEmpty callback fires when list is empty
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onEmpty_fires_whenNoUsersReturned() {
        println("=== TEST: onEmpty_fires_whenNoUsersReturned ===")
        println("STEP 1: Creating empty DataSource")
        UsersUIHostFragment.injectedDataSource = object : UsersDataSource {
            override suspend fun fetchUsers(request: UsersRequest) = emptyList<User>()
        }

        println("STEP 2: Launching fragment")
        val scenario = launchFragmentInContainer<UsersUIHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 3: Asserting onEmpty callback was fired")
        Thread.sleep(500)
        assertTrue("onEmpty should have been called", UsersUIHostFragment.onEmptyFired)

        println("RESULT: onEmpty callback fired successfully")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: onLoad callback fires with loaded user list
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onLoad_firesWithUserList_whenFetchSucceeds() {
        println("=== TEST: onLoad_firesWithUserList_whenFetchSucceeds ===")
        println("STEP 1: Creating DataSource with 3 users")
        val users = createMockUsers(3)
        UsersUIHostFragment.injectedDataSource = createSuccessDataSource(users)

        println("STEP 2: Launching fragment")
        val scenario = launchFragmentInContainer<UsersUIHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 3: Waiting for content to render, then asserting onLoad was fired")
        // Wait for RecyclerView to display content (Espresso syncs with main thread)
        onView(withId(R.id.recyclerview_users_list))
            .check(matches(isDisplayed()))
        // After content is rendered, onLoad should have been called
        assertNotNull("onLoad should have been called", UsersUIHostFragment.onLoadList)

        println("RESULT: onLoad fired with ${UsersUIHostFragment.onLoadList?.size} users")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Selection mode SINGLE — clicking item selects it
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun selectionModeSingle_clickingItem_selectsIt() {
        println("=== TEST: selectionModeSingle_clickingItem_selectsIt ===")
        println("STEP 1: Setting selection mode to SINGLE")
        UsersUIHostFragment.selectionMode = UIKitConstants.SelectionMode.SINGLE
        val users = createMockUsers(3)
        UsersUIHostFragment.injectedDataSource = createSuccessDataSource(users)

        println("STEP 2: Launching fragment")
        val scenario = launchFragmentInContainer<UsersUIHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 3: Clicking item at position 0")
        onView(withId(R.id.recyclerview_users_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )

        println("STEP 4: Asserting selection toolbar shows count '1'")
        // In selection mode, clicking selects the item and shows count
        onView(withText("1")).check(matches(isDisplayed()))

        println("RESULT: Single selection mode shows count '1' after clicking item")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST: Selection mode MULTIPLE — clicking multiple items increases count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun selectionModeMultiple_clickingMultipleItems_increasesCount() {
        println("=== TEST: selectionModeMultiple_clickingMultipleItems_increasesCount ===")
        println("STEP 1: Setting selection mode to MULTIPLE")
        UsersUIHostFragment.selectionMode = UIKitConstants.SelectionMode.MULTIPLE
        val users = createMockUsers(5)
        UsersUIHostFragment.injectedDataSource = createSuccessDataSource(users)

        println("STEP 2: Launching fragment")
        val scenario = launchFragmentInContainer<UsersUIHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("STEP 3: Clicking items at positions 0 and 1")
        onView(withId(R.id.recyclerview_users_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )
        onView(withId(R.id.recyclerview_users_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click())
            )

        println("STEP 4: Asserting selection toolbar shows count '2'")
        onView(withText("2")).check(matches(isDisplayed()))

        println("RESULT: Multiple selection mode shows count '2' after clicking 2 items")
        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment for UI Tests
    // ─────────────────────────────────────────────────────────────────────────

    class UsersUIHostFragment : Fragment() {

        companion object {
            var injectedDataSource: UsersDataSource? = null
            var onItemClickUser: User? = null
            var onSelectionList: List<User>? = null
            var onErrorException: CometChatException? = null
            var onEmptyFired: Boolean = false
            var onLoadList: List<User>? = null
            var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            println("  UsersUIHostFragment.onCreateView")
            val usersView = CometChatUsers(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            // Inject ViewModel BEFORE addView to prevent default ViewModel's
            // fetchUsers() from crashing in onAttachedToWindow
            injectedDataSource?.let { ds ->
                println("  UsersUIHostFragment: Injecting ViewModel BEFORE attach")
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
            if (selectionMode != UIKitConstants.SelectionMode.NONE) {
                usersView.setSelectionMode(selectionMode)
            }

            // Wire all callbacks
            usersView.setOnItemClick { user ->
                println("  UsersUIHostFragment: onItemClick UID='${user.uid}'")
                onItemClickUser = user
            }
            usersView.setOnError { exception ->
                println("  UsersUIHostFragment: onError code='${exception.code}'")
                onErrorException = exception
            }
            usersView.setOnEmpty {
                println("  UsersUIHostFragment: onEmpty fired")
                onEmptyFired = true
            }
            usersView.setOnLoad { users ->
                println("  UsersUIHostFragment: onLoad fired with ${users.size} users")
                onLoadList = users
            }
            usersView.setOnSelection { users ->
                println("  UsersUIHostFragment: onSelection fired with ${users.size} users")
                onSelectionList = users
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
            println("  UsersUIHostFragment.onViewCreated: View ready")
        }
    }
}
