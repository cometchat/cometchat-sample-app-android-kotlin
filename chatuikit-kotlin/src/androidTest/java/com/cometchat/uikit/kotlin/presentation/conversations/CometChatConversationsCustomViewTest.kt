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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.User
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instrumented tests for CometChatConversations custom view slot rendering.
 *
 * Tests verify:
 * - Custom loadingView displayed when UIState is Loading
 * - Custom errorView displayed when UIState is Error
 * - Custom emptyView displayed when UIState is Empty
 * - onLoad callback invoked when UIState transitions to Content
 * - onEmpty callback invoked when UIState transitions to Empty
 * - onError callback invoked when UIState transitions to Error
 * - hideToolbar hides the toolbar
 * - hideSearchBox hides the search box
 *
 * Mirrors: chatuikit-compose CometChatConversationsCustomViewTest
 *
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.conversationlist.CometChatConversationsCustomViewTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatConversationsCustomViewTest {

    @Before
    fun setup() {
        CustomViewHostFragment.injectedDataSource = null
        CustomViewHostFragment.customLoadingView = null
        CustomViewHostFragment.customErrorView = null
        CustomViewHostFragment.customEmptyView = null
        CustomViewHostFragment.hideToolbar = false
        CustomViewHostFragment.hideSearchBox = false
        CustomViewHostFragment.onLoadInvoked = AtomicBoolean(false)
        CustomViewHostFragment.onEmptyInvoked = AtomicBoolean(false)
        CustomViewHostFragment.onErrorInvoked = AtomicBoolean(false)
    }

    @After
    fun tearDown() {
        CustomViewHostFragment.injectedDataSource = null
        CustomViewHostFragment.customLoadingView = null
        CustomViewHostFragment.customErrorView = null
        CustomViewHostFragment.customEmptyView = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mock & DataSource Factories
    // ─────────────────────────────────────────────────────────────────────────

    private fun createMockUser(uid: String, name: String): User {
        val user = mock(User::class.java)
        `when`(user.uid).thenReturn(uid)
        `when`(user.name).thenReturn(name)
        `when`(user.status).thenReturn("online")
        `when`(user.avatar).thenReturn(null)
        return user
    }

    private fun createMockConversation(uid: String, userName: String): Conversation {
        val user = createMockUser(uid, userName)
        val conversation = mock(Conversation::class.java)
        `when`(conversation.conversationId).thenReturn("conv_$uid")
        `when`(conversation.conversationType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        `when`(conversation.conversationWith).thenReturn(user)
        `when`(conversation.unreadMessageCount).thenReturn(0)
        `when`(conversation.lastMessage).thenReturn(null)
        return conversation
    }

    private fun createMockConversations(count: Int): List<Conversation> {
        return (1..count).map { i ->
            createMockConversation(uid = "user-$i", userName = "User $i")
        }
    }

    private fun createContentDataSource(count: Int): ConversationListDataSource {
        val conversations = createMockConversations(count)
        return object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = conversations
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }
    }

    private fun createEmptyDataSource(): ConversationListDataSource {
        return object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = emptyList<Conversation>()
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }
    }

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

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM LOADING VIEW TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customLoadingView_displayedWhenLoading() {
        CustomViewHostFragment.injectedDataSource = createLoadingDataSource()
        CustomViewHostFragment.customLoadingView = { context ->
            TextView(context).apply { text = "My Custom Loader" }
        }

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withText("My Custom Loader"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM ERROR VIEW TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customErrorView_displayedWhenError() {
        CustomViewHostFragment.injectedDataSource = createErrorDataSource()
        CustomViewHostFragment.customErrorView = { context ->
            TextView(context).apply { text = "Custom Error" }
        }

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Custom error view should be displayed
        onView(withText("Custom Error"))
            .check(matches(isDisplayed()))
        // Default error view should NOT be displayed
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM EMPTY VIEW TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customEmptyView_displayedWhenEmpty() {
        CustomViewHostFragment.injectedDataSource = createEmptyDataSource()
        CustomViewHostFragment.customEmptyView = { context ->
            TextView(context).apply { text = "Custom Empty State" }
        }

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Custom empty view should be displayed
        onView(withText("Custom Empty State"))
            .check(matches(isDisplayed()))
        // Default empty view should NOT be displayed
        onView(withId(R.id.empty_state_view))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CALLBACK TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onLoadCallback_invokedWhenContentLoaded() {
        CustomViewHostFragment.injectedDataSource = createContentDataSource(3)

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for content to load
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(isDisplayed()))

        assertTrue(
            "onLoad callback should have been invoked",
            CustomViewHostFragment.onLoadInvoked.get()
        )

        scenario.close()
    }

    @Test
    fun onEmptyCallback_invokedWhenEmpty() {
        CustomViewHostFragment.injectedDataSource = createEmptyDataSource()

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for empty state
        onView(withId(R.id.empty_state_view))
            .check(matches(isDisplayed()))

        assertTrue(
            "onEmpty callback should have been invoked",
            CustomViewHostFragment.onEmptyInvoked.get()
        )

        scenario.close()
    }

    @Test
    fun onErrorCallback_invokedWhenErrorOccurs() {
        CustomViewHostFragment.injectedDataSource = createErrorDataSource()

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for error state
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        assertTrue(
            "onError callback should have been invoked",
            CustomViewHostFragment.onErrorInvoked.get()
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VISIBILITY CONTROL TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun hideToolbar_hidesToolbar() {
        CustomViewHostFragment.injectedDataSource = createContentDataSource(3)
        CustomViewHostFragment.hideToolbar = true

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Toolbar should NOT be displayed
        onView(withId(R.id.toolbar))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun hideSearchBox_hidesSearchBox() {
        CustomViewHostFragment.injectedDataSource = createContentDataSource(3)
        CustomViewHostFragment.hideSearchBox = true

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Search box layout should NOT be displayed
        onView(withId(R.id.search_box_layout))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun defaultState_showsToolbarAndSearchBox() {
        CustomViewHostFragment.injectedDataSource = createContentDataSource(3)

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Both toolbar and search box should be displayed by default
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
        onView(withId(R.id.search_box))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun emptyState_displaysDefaultEmptyView() {
        CustomViewHostFragment.injectedDataSource = createEmptyDataSource()

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Default empty state view should be displayed
        onView(withId(R.id.empty_state_view))
            .check(matches(isDisplayed()))
        // RecyclerView should NOT be displayed
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun contentState_displaysRecyclerView() {
        CustomViewHostFragment.injectedDataSource = createContentDataSource(3)

        val scenario = launchFragmentInContainer<CustomViewHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // RecyclerView should be displayed
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(isDisplayed()))
        // Empty and error states should NOT be displayed
        onView(withId(R.id.empty_state_view))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment for Custom View Tests
    // ─────────────────────────────────────────────────────────────────────────

    class CustomViewHostFragment : Fragment() {

        companion object {
            var injectedDataSource: ConversationListDataSource? = null
            var customLoadingView: ((android.content.Context) -> View)? = null
            var customErrorView: ((android.content.Context) -> View)? = null
            var customEmptyView: ((android.content.Context) -> View)? = null
            var hideToolbar: Boolean = false
            var hideSearchBox: Boolean = false
            var onLoadInvoked = AtomicBoolean(false)
            var onEmptyInvoked = AtomicBoolean(false)
            var onErrorInvoked = AtomicBoolean(false)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val conversationsView = CometChatConversations(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            return FrameLayout(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                addView(conversationsView)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val frameLayout = view as FrameLayout
            val conversationsView = frameLayout.getChildAt(0) as CometChatConversations

            // Configure visibility
            if (hideToolbar) {
                conversationsView.setToolbarVisibility(View.GONE)
            }
            if (hideSearchBox) {
                conversationsView.setSearchBoxVisibility(View.GONE)
            }

            // Set custom views
            customLoadingView?.let { viewFactory ->
                conversationsView.setLoadingView(viewFactory(requireContext()))
            }
            customErrorView?.let { viewFactory ->
                conversationsView.setErrorView(viewFactory(requireContext()))
            }
            customEmptyView?.let { viewFactory ->
                conversationsView.setEmptyView(viewFactory(requireContext()))
            }

            // Set callbacks
            conversationsView.setOnLoad { _ ->
                onLoadInvoked.set(true)
            }
            conversationsView.setOnEmpty {
                onEmptyInvoked.set(true)
            }
            conversationsView.setOnError { _ ->
                onErrorInvoked.set(true)
            }

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
    }
}
