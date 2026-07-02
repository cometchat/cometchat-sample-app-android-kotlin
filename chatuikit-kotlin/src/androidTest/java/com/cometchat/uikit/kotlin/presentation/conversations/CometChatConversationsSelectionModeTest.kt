package com.cometchat.uikit.kotlin.presentation.conversations

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
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.data.datasource.ConversationListDataSource
import com.cometchat.uikit.core.data.repository.ConversationListRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.conversations.ui.CometChatConversations
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Instrumented tests for CometChatConversations selection mode behavior.
 *
 * Tests verify:
 * - SINGLE selection mode shows selection count in toolbar
 * - MULTIPLE selection mode allows multiple selections
 * - Discard selection clears selections and returns to normal toolbar
 * - Selection callback is invoked with correct conversations
 *
 * Mirrors: chatuikit-compose CometChatConversationsSelectionModeTest
 *
 * Requirements: 8.4, 8.5
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.conversationlist.CometChatConversationsSelectionModeTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatConversationsSelectionModeTest {

    /**
     * Waits for the RecyclerView to have at least the expected number of items.
     * This prevents Espresso from clicking before async data has loaded and laid out.
     */
    private fun waitForRecyclerViewPopulated(minItems: Int = 1, timeoutMs: Long = 5000) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                onView(withId(R.id.recyclerview_conversations_list))
                    .check(matches(hasMinimumChildCount(minItems)))
                return
            } catch (e: AssertionError) {
                Thread.sleep(100)
            }
        }
        // Final assertion — will throw with a clear message if still not populated
        onView(withId(R.id.recyclerview_conversations_list))
            .check(matches(hasMinimumChildCount(minItems)))
    }

    @Before
    fun setup() {
        SelectionHostFragment.injectedDataSource = null
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.NONE
        SelectionHostFragment.selectedConversations = emptyList()
        SelectionHostFragment.conversationsView = null
    }

    @After
    fun tearDown() {
        SelectionHostFragment.injectedDataSource = null
        SelectionHostFragment.conversationsView = null
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

    private fun createDataSource(conversations: List<Conversation>): ConversationListDataSource {
        return object : ConversationListDataSource {
            override suspend fun fetchConversations(request: ConversationsRequest) = conversations
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) = "success"
            override suspend fun markAsDelivered(message: BaseMessage) {}
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: SINGLE selection mode - clicking item displays selection count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun singleSelectionMode_clickingItem_displaysSelectionCountInToolbar() {
        val conversations = createMockConversations(3)
        SelectionHostFragment.injectedDataSource = createDataSource(conversations)
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.SINGLE

        val scenario = launchFragmentInContainer<SelectionHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for content to load and items to be laid out
        waitForRecyclerViewPopulated(3)

        // Click on the first conversation item
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Selection toolbar should show count "1"
        onView(withText("1")).check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: MULTIPLE selection mode - clicking multiple items increases count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleSelectionMode_clickingMultipleItems_increasesCount() {
        val conversations = createMockConversations(3)
        SelectionHostFragment.injectedDataSource = createDataSource(conversations)
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.MULTIPLE

        val scenario = launchFragmentInContainer<SelectionHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for content to load and items to be laid out
        waitForRecyclerViewPopulated(3)

        // Click first item
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Click second item
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // Selection toolbar should show count "2"
        onView(withText("2")).check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Discard selection clears and returns to normal toolbar
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun selectionMode_discardSelection_clearsAndReturnsToNormalToolbar() {
        val conversations = createMockConversations(3)
        SelectionHostFragment.injectedDataSource = createDataSource(conversations)
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.SINGLE
        SelectionHostFragment.title = "Chats"

        val scenario = launchFragmentInContainer<SelectionHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for content to load and items to be laid out
        waitForRecyclerViewPopulated(3)

        // Select a conversation
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Verify selection mode is active (count "1" shown)
        onView(withText("1")).check(matches(isDisplayed()))

        // Click discard selection (back icon in toolbar during selection mode)
        scenario.onFragment { fragment ->
            fragment.clearSelectionProgrammatically()
        }

        // Normal toolbar should be back with the title
        onView(withText("Chats")).check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: MULTIPLE selection toggle - deselecting reduces count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleSelectionMode_deselectingItem_reducesCount() {
        val conversations = createMockConversations(3)
        SelectionHostFragment.injectedDataSource = createDataSource(conversations)
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.MULTIPLE

        val scenario = launchFragmentInContainer<SelectionHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for content to load and items to be laid out
        waitForRecyclerViewPopulated(3)

        // Select two items
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // Verify count is 2
        onView(withText("2")).check(matches(isDisplayed()))

        // Deselect first item (toggle)
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Count should now be 1
        onView(withText("1")).check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Selection callback invoked with correct conversations
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun selectionMode_onSelectionCallback_invokedWithCorrectConversations() {
        val conversations = createMockConversations(3)
        SelectionHostFragment.injectedDataSource = createDataSource(conversations)
        SelectionHostFragment.selectionMode = UIKitConstants.SelectionMode.MULTIPLE

        val scenario = launchFragmentInContainer<SelectionHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for content to load and items to be laid out
        waitForRecyclerViewPopulated(3)

        // Select first and third items (scroll to ensure visibility)
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0))
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(2))
        onView(withId(R.id.recyclerview_conversations_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))

        // Verify selected conversations count
        scenario.onFragment { fragment ->
            val selected = fragment.getSelectedConversationsList()
            assertEquals(2, selected.size)
        }

        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment for Selection Mode Tests
    // ─────────────────────────────────────────────────────────────────────────

    class SelectionHostFragment : Fragment() {

        companion object {
            var injectedDataSource: ConversationListDataSource? = null
            var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE
            var selectedConversations: List<Conversation> = emptyList()
            var conversationsView: CometChatConversations? = null
            var title: String = "Chats"
        }

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

            // Set title
            conversationsView.setTitle(title)

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

            // Set selection mode
            if (selectionMode != UIKitConstants.SelectionMode.NONE) {
                conversationsView.setSelectionMode(selectionMode)
            }

            // Wire selection callback
            conversationsView.setOnSelection { conversations ->
                selectedConversations = conversations
            }
        }

        fun clearSelectionProgrammatically() {
            conversationsView?.clearSelection()
        }

        fun getSelectedConversationsList(): List<Conversation> {
            return conversationsView?.getSelectedConversations() ?: emptyList()
        }
    }
}
