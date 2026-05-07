package com.cometchat.uikit.kotlin.presentation.conversations

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * True UI Integration Test for CometChatConversations (XML/Kotlin).
 *
 * This test actually inflates the CometChatConversations view inside a Fragment,
 * injects a ViewModel backed by a fake DataSource, and uses Espresso to assert
 * on the rendered UI (RecyclerView items, state views, click interactions).
 *
 * Architecture:
 *   [Fake DataSource] → [Real Repository] → [Real UseCases] → [Real ViewModel]
 *       → [Real CometChatConversations View] → [Espresso assertions on rendered UI]
 *
 * The ONLY fake is the DataSource at the SDK boundary. Everything else is real
 * production code — Repository, UseCases, ViewModel, View, Adapter, ViewHolder.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.conversationlist.CometChatConversationsViewIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatConversationsViewIntegrationTest {

    @Before
    fun setup() {
        ConversationsHostFragment.injectedDataSource = null
        ConversationsHostFragment.onItemClickConversation = null
        ConversationsHostFragment.onItemLongClickConversation = null
    }

    @After
    fun tearDown() {
        ConversationsHostFragment.injectedDataSource = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mock Factory: Create SDK objects with Mockito (SDK has private constructors)
    // ─────────────────────────────────────────────────────────────────────────

    private fun createMockUser(uid: String, name: String): User {
        val user = mock(User::class.java)
        `when`(user.uid).thenReturn(uid)
        `when`(user.name).thenReturn(name)
        `when`(user.status).thenReturn("online")
        `when`(user.avatar).thenReturn(null)
        return user
    }

    private fun createMockConversation(
        uid: String,
        userName: String,
        unreadCount: Int = 0
    ): Conversation {
        val user = createMockUser(uid, userName)
        val conversation = mock(Conversation::class.java)
        `when`(conversation.conversationId).thenReturn("conv_$uid")
        `when`(conversation.conversationType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        `when`(conversation.conversationWith).thenReturn(user)
        `when`(conversation.unreadMessageCount).thenReturn(unreadCount)
        `when`(conversation.lastMessage).thenReturn(null)
        return conversation
    }

    private fun createMockConversations(count: Int): List<Conversation> {
        return (1..count).map { i ->
            createMockConversation(uid = "user-$i", userName = "User $i")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: Launch the fragment with injected DataSource
    // ─────────────────────────────────────────────────────────────────────────

    private fun launchWithDataSource(
        dataSource: ConversationListDataSource
    ): FragmentScenario<ConversationsHostFragment> {
        ConversationsHostFragment.injectedDataSource = dataSource
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Content state renders conversation items in RecyclerView
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun contentState_rendersConversationItems_inRecyclerView() {
        val conversations = createMockConversations(3)
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = conversations
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        // Assert: RecyclerView is displayed
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(isDisplayed()))

        // Assert: Each conversation title is rendered on screen
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(hasDescendant(withText("User 1"))))
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(hasDescendant(withText("User 2"))))
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(hasDescendant(withText("User 3"))))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Empty state shows empty view when DataSource returns empty list
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyState_showsEmptyView_whenNoConversations() {
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = emptyList<Conversation>()
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        onView(withId(R.id.empty_state_view))
            .check(matches(isDisplayed()))
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Error state shows error view when DataSource throws
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_showsErrorView_whenDataSourceThrows() {
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest): List<Conversation> {
                throw CometChatException("NET_ERR", "Network error")
            }
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))
        onView(withId(R.id.btn_retry))
            .check(matches(isDisplayed()))
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Item click triggers onItemClick callback with correct conversation
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun itemClick_triggersCallback_withCorrectConversation() {
        val conversations = createMockConversations(3)
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = conversations
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        onView(withId(R.id.recyclerview_conversations_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )

        assertNotNull(
            "onItemClick callback was not triggered",
            ConversationsHostFragment.onItemClickConversation
        )
        assertEquals(
            conversations[0].conversationId,
            ConversationsHostFragment.onItemClickConversation?.conversationId
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Item long click triggers onItemLongClick callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun itemLongClick_triggersCallback_withCorrectConversation() {
        val conversations = createMockConversations(3)
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = conversations
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        onView(withId(R.id.recyclerview_conversations_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, longClick())
            )

        assertNotNull(
            "onItemLongClick callback was not triggered",
            ConversationsHostFragment.onItemLongClickConversation
        )
        assertEquals(
            conversations[1].conversationId,
            ConversationsHostFragment.onItemLongClickConversation?.conversationId
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Retry button triggers re-fetch and transitions to content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun retryButton_triggersFetch_andShowsContent() {
        var callCount = 0
        val conversations = createMockConversations(2)
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest): List<Conversation> {
                callCount++
                if (callCount == 1) {
                    throw CometChatException("NET_ERR", "Network error")
                }
                return conversations
            }
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        // Verify error state first
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        // Click retry
        onView(withId(R.id.btn_retry))
            .perform(click())

        // Assert: Content is now displayed
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(isDisplayed()))
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Toolbar is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun toolbar_isVisible() {
        val conversations = createMockConversations(1)
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = conversations
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Search box is visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun searchBox_isVisible() {
        val conversations = createMockConversations(1)
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = conversations
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        onView(withId(R.id.search_box))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: RecyclerView has correct item count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun recyclerView_hasCorrectItemCount() {
        val conversations = createMockConversations(5)
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = conversations
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        onView(withId(R.id.recyclerview_conversations_list))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) throw noViewFoundException
                val recyclerView = view as RecyclerView
                assertEquals(
                    "RecyclerView should have 5 items",
                    5,
                    recyclerView.adapter?.itemCount
                )
            }

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 10: Clicking different items returns different conversations
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun clickingDifferentItems_returnsDifferentConversations() {
        val conversations = createMockConversations(3)
        val dataSource = object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = conversations
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }

        val scenario = launchWithDataSource(dataSource)

        onView(withId(R.id.recyclerview_conversations_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click())
            )

        assertEquals(
            conversations[2].conversationId,
            ConversationsHostFragment.onItemClickConversation?.conversationId
        )

        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment: Wraps CometChatConversations for testing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A minimal Fragment that hosts CometChatConversations.
     *
     * The DataSource is injected via the companion object before launch.
     * The ViewModel is built inside onViewCreated (on the main thread) to ensure
     * proper coroutine dispatching with viewModelScope.
     */
    class ConversationsHostFragment : Fragment() {

        companion object {
            /** Injected before fragment launch — cleared after each test */
            var injectedDataSource: ConversationListDataSource? = null
            var onItemClickConversation: Conversation? = null
            var onItemLongClickConversation: Conversation? = null
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

            // Build ViewModel on main thread with the injected DataSource
            injectedDataSource?.let { ds ->
                val repository = ConversationListRepositoryImpl(ds)
                val getUseCase = GetConversationListUseCase(repository)
                val deleteUseCase = DeleteConversationUseCase(repository)
                val refreshUseCase = RefreshConversationListUseCase(repository)
                val viewModel = CometChatConversationsViewModel(
                    getConversationListUseCase = getUseCase,
                    deleteConversationUseCase = deleteUseCase,
                    refreshConversationListUseCase = refreshUseCase,
                    enableListeners = false
                )
                conversationsView.setViewModel(viewModel)
            }

            // Wire callbacks for assertion
            conversationsView.setOnItemClick { conversation ->
                onItemClickConversation = conversation
            }
            conversationsView.setOnItemLongClick { conversation ->
                onItemLongClickConversation = conversation
            }
        }
    }
}
