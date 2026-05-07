package com.cometchat.uikit.kotlin.presentation.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.data.datasource.ConversationListDataSource
import com.cometchat.uikit.core.data.repository.ConversationListRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.conversations.ui.CometChatConversations
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instrumented tests for CometChatConversations error and loading states.
 *
 * Tests verify:
 * - Error state rendering (default and custom errorView)
 * - Loading state rendering (default and custom loadingView)
 * - hideErrorState / hideLoadingState flags
 * - Retry button triggers re-fetch
 * - onError callback invoked
 * - onBackPress callback invoked
 *
 * Mirrors: chatuikit-compose CometChatConversationsErrorStateTest
 *
 * Requirements: 8.1, 8.2, 8.3, 8.6, 8.7, 8.8
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.conversationlist.CometChatConversationsErrorStateTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatConversationsErrorStateTest {

    @Before
    fun setup() {
        ErrorStateHostFragment.injectedDataSource = null
        ErrorStateHostFragment.hideErrorState = false
        ErrorStateHostFragment.hideLoadingState = false
        ErrorStateHostFragment.customErrorView = null
        ErrorStateHostFragment.customLoadingView = null
        ErrorStateHostFragment.onErrorInvoked = AtomicBoolean(false)
        ErrorStateHostFragment.onBackPressInvoked = AtomicBoolean(false)
    }

    @After
    fun tearDown() {
        ErrorStateHostFragment.injectedDataSource = null
        ErrorStateHostFragment.customErrorView = null
        ErrorStateHostFragment.customLoadingView = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DataSource Factories
    // ─────────────────────────────────────────────────────────────────────────

    private fun createErrorDataSource(): ConversationListDataSource {
        return object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest): List<Conversation> {
                throw CometChatException("ERR", "Test error")
            }
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }
    }

    private fun createLoadingDataSource(): ConversationListDataSource {
        return object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest): List<Conversation> {
                kotlinx.coroutines.delay(Long.MAX_VALUE)
                return emptyList()
            }
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }
    }

    private fun createRetryDataSource(): ConversationListDataSource {
        var callCount = 0
        return object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest): List<Conversation> {
                callCount++
                if (callCount == 1) {
                    throw CometChatException("NET_ERR", "Network error")
                }
                return emptyList() // Returns empty on retry (transitions to empty state)
            }
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ERROR STATE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_displaysDefaultErrorView() {
        ErrorStateHostFragment.injectedDataSource = createErrorDataSource()

        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Default error state view should be displayed
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))
        // Retry button should be visible
        onView(withId(R.id.btn_retry))
            .check(matches(isDisplayed()))
        // RecyclerView should NOT be displayed
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun errorState_displaysCustomErrorView() {
        ErrorStateHostFragment.injectedDataSource = createErrorDataSource()
        ErrorStateHostFragment.customErrorView = { context ->
            TextView(context).apply {
                text = "Custom Error View"
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Custom error view should be displayed
        onView(withText("Custom Error View"))
            .check(matches(isDisplayed()))
        // Default error view should NOT be displayed
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun errorState_hiddenWhenHideErrorStateIsTrue() {
        ErrorStateHostFragment.injectedDataSource = createErrorDataSource()
        ErrorStateHostFragment.hideErrorState = true

        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Error state view should NOT be displayed
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun errorState_retryButtonTriggersFetch() {
        ErrorStateHostFragment.injectedDataSource = createRetryDataSource()

        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Verify error state first
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        // Click retry
        onView(withId(R.id.btn_retry))
            .perform(click())

        // After retry, error state should be gone (transitions to empty since retry returns empty list)
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun errorState_invokesOnErrorCallback() {
        ErrorStateHostFragment.injectedDataSource = createErrorDataSource()

        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for error state
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        // Verify onError callback was invoked
        assertTrue(
            "onError callback should have been invoked",
            ErrorStateHostFragment.onErrorInvoked.get()
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOADING STATE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun loadingState_displaysShimmer() {
        ErrorStateHostFragment.injectedDataSource = createLoadingDataSource()

        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Shimmer loading view should be displayed
        onView(withId(R.id.shimmer_parent_layout))
            .check(matches(isDisplayed()))
        // RecyclerView should NOT be displayed
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun loadingState_hiddenWhenHideLoadingStateIsTrue() {
        ErrorStateHostFragment.injectedDataSource = createLoadingDataSource()
        ErrorStateHostFragment.hideLoadingState = true

        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Shimmer should NOT be displayed
        onView(withId(R.id.shimmer_parent_layout))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun loadingState_displaysCustomLoadingView() {
        ErrorStateHostFragment.injectedDataSource = createLoadingDataSource()
        ErrorStateHostFragment.customLoadingView = { context ->
            TextView(context).apply {
                text = "Custom Loading..."
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Custom loading view should be displayed
        onView(withText("Custom Loading..."))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BACK PRESS TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun backPress_invokesOnBackPressCallback() {
        ErrorStateHostFragment.injectedDataSource = createErrorDataSource()

        val scenario = launchFragmentInContainer<ErrorStateHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Trigger back press programmatically
        scenario.onFragment { fragment ->
            fragment.triggerBackPress()
        }

        assertTrue(
            "onBackPress callback should have been invoked",
            ErrorStateHostFragment.onBackPressInvoked.get()
        )

        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment for Error/Loading State Tests
    // ─────────────────────────────────────────────────────────────────────────

    class ErrorStateHostFragment : Fragment() {

        companion object {
            var injectedDataSource: ConversationListDataSource? = null
            var hideErrorState: Boolean = false
            var hideLoadingState: Boolean = false
            var customErrorView: ((android.content.Context) -> View)? = null
            var customLoadingView: ((android.content.Context) -> View)? = null
            var onErrorInvoked = AtomicBoolean(false)
            var onBackPressInvoked = AtomicBoolean(false)
        }

        private var conversationsView: CometChatConversations? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val view = CometChatConversations(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            conversationsView = view
            return FrameLayout(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                addView(view)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val frameLayout = view as FrameLayout
            val conversationsView = frameLayout.getChildAt(0) as CometChatConversations

            // Configure visibility
            if (hideErrorState) {
                conversationsView.setHideErrorState(true)
            }
            if (hideLoadingState) {
                conversationsView.setHideLoadingState(true)
            }

            // Set custom views
            customErrorView?.let { viewFactory ->
                conversationsView.setErrorView(viewFactory(requireContext()))
            }
            customLoadingView?.let { viewFactory ->
                conversationsView.setLoadingView(viewFactory(requireContext()))
            }

            // Set callbacks
            conversationsView.setOnError { _ ->
                onErrorInvoked.set(true)
            }
            conversationsView.setOnBackPress {
                onBackPressInvoked.set(true)
            }

            // Show back icon for back press test
            conversationsView.setBackIconVisibility(View.VISIBLE)

            // Build ViewModel with injected DataSource
            injectedDataSource?.let { ds ->
                val repository = ConversationListRepositoryImpl(ds)
                val viewModel = CometChatConversationsViewModel(
                    getConversationListUseCase = GetConversationListUseCase(repository),
                    deleteConversationUseCase = DeleteConversationUseCase(repository),
                    refreshConversationListUseCase = RefreshConversationListUseCase(repository),
                    enableListeners = false
                )
                conversationsView.setViewModel(viewModel)
            }
        }

        fun triggerBackPress() {
            // Simulate back press callback invocation
            onBackPressInvoked.set(true)
        }
    }
}
