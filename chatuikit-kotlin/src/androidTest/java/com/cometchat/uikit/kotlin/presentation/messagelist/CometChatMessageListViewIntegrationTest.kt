package com.cometchat.uikit.kotlin.presentation.messagelist

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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * True UI Integration Test for CometChatMessageList (XML/Kotlin).
 *
 * This test actually inflates the CometChatMessageList view inside a Fragment,
 * injects a ViewModel backed by a fake Repository, and uses Espresso to assert
 * on the rendered UI (RecyclerView items, state views, pagination).
 *
 * Architecture:
 *   [Fake Repository] → [Real ViewModel] → [Real CometChatMessageList View]
 *       → [Espresso assertions on rendered UI]
 *
 * The ONLY fake is the Repository at the data boundary. Everything else is real
 * production code — ViewModel, View, Adapter, ViewHolder.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.messagelist.CometChatMessageListViewIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatMessageListViewIntegrationTest {

    private companion object {
        private val LOGGED_IN_UID = MessageListTestSdkHelper.LOGGED_IN_USER_UID
        private const val OTHER_UID = "user-1"
        private const val OTHER_NAME = "Alice Johnson"
    }

    @Before
    fun setup() {
        MessageListTestSdkHelper.ensureInitialized()
        MessageListHostFragment.reset()
    }

    @After
    fun tearDown() {
        MessageListHostFragment.reset()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mock Factory: Create SDK objects (SDK has private constructors for some)
    // ─────────────────────────────────────────────────────────────────────────

    private fun createUser(uid: String, name: String): User {
        return User().apply {
            this.uid = uid
            this.name = name
            this.status = CometChatConstants.USER_STATUS_ONLINE
        }
    }

    private fun createTextMessage(
        id: Long,
        text: String,
        senderUid: String,
        senderName: String,
        sentAt: Long,
        receiverId: String = OTHER_UID,
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER
    ): TextMessage {
        val sender = User().apply {
            uid = senderUid
            name = senderName
            status = CometChatConstants.USER_STATUS_ONLINE
        }
        return TextMessage(receiverId, text, receiverType).apply {
            this.id = id
            this.sentAt = sentAt
            this.sender = sender
            this.receiverUid = receiverId
            this.type = CometChatConstants.MESSAGE_TYPE_TEXT
            this.category = CometChatConstants.CATEGORY_MESSAGE
        }
    }

    private fun createMessages(count: Int): List<BaseMessage> {
        val now = System.currentTimeMillis() / 1000
        val texts = listOf(
            "Hey, how are you?",
            "I'm doing great, thanks!",
            "Did you see the new update?",
            "Yes, it looks amazing!",
            "Let's discuss it tomorrow",
            "Sure, what time works?",
            "How about 3 PM?",
            "Perfect, see you then!",
            "Don't forget the docs",
            "Will do! 👍"
        )
        return (0 until count).map { i ->
            val isIncoming = i % 2 == 0
            createTextMessage(
                id = (i + 1).toLong(),
                text = texts[i % texts.size],
                senderUid = if (isIncoming) OTHER_UID else LOGGED_IN_UID,
                senderName = if (isIncoming) OTHER_NAME else "Me",
                sentAt = now - ((count - i) * 120L)
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: Launch the fragment with injected Repository
    // ─────────────────────────────────────────────────────────────────────────

    private fun launchWithMessages(
        messages: List<BaseMessage>,
        user: User = createUser(OTHER_UID, OTHER_NAME)
    ): FragmentScenario<MessageListHostFragment> {
        MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
        MessageListHostFragment.userToSet = user
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    private fun launchWithRepository(
        repository: FakeMessageListRepository,
        user: User = createUser(OTHER_UID, OTHER_NAME)
    ): FragmentScenario<MessageListHostFragment> {
        MessageListHostFragment.injectedRepository = repository
        MessageListHostFragment.userToSet = user
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Content state renders message items in RecyclerView
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun contentState_rendersMessageItems_inRecyclerView() {
        println("  🧪 contentState_rendersMessageItems_inRecyclerView")
        println("    → Creating 5 messages")

        val messages = createMessages(5)
        val scenario = launchWithMessages(messages)

        println("    → Asserting RecyclerView is displayed")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(isDisplayed()))

        println("    → Asserting message text is rendered")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(hasDescendant(withText("Hey, how are you?"))))

        println("    ✅ Content state renders correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Empty state shows empty view when Repository returns empty list
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyState_showsEmptyView_whenNoMessages() {
        println("  🧪 emptyState_showsEmptyView_whenNoMessages")
        println("    → Launching with empty message list")

        val scenario = launchWithMessages(emptyList())

        println("    → Asserting empty state view is displayed")
        onView(withId(R.id.empty_state_view))
            .check(matches(isDisplayed()))

        println("    → Asserting RecyclerView is NOT displayed")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(not(isDisplayed())))

        println("    ✅ Empty state renders correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Error state shows error view when Repository throws
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_showsErrorView_whenRepositoryThrows() {
        println("  🧪 errorState_showsErrorView_whenRepositoryThrows")
        println("    → Launching with failing repository")

        val repository = FakeMessageListRepository(
            shouldFail = true,
            errorCode = "NET_ERR",
            errorMessage = "Network error"
        )
        val scenario = launchWithRepository(repository)

        println("    → Asserting error state view is displayed")
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        println("    → Asserting RecyclerView is NOT displayed")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(not(isDisplayed())))

        println("    ✅ Error state renders correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: RecyclerView has correct item count
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun recyclerView_hasCorrectItemCount() {
        println("  🧪 recyclerView_hasCorrectItemCount")
        println("    → Creating 7 messages")

        val messages = createMessages(7)
        val scenario = launchWithMessages(messages)

        println("    → Asserting RecyclerView has 7 items")
        onView(withId(R.id.recyclerview_message_list))
            .check { view, noViewFoundException ->
                if (noViewFoundException != null) throw noViewFoundException
                val recyclerView = view as RecyclerView
                assertEquals(
                    "RecyclerView should have 7 items",
                    7,
                    recyclerView.adapter?.itemCount
                )
            }

        println("    ✅ Item count is correct")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Multiple messages render with correct text content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleMessages_renderWithCorrectText() {
        println("  🧪 multipleMessages_renderWithCorrectText")
        println("    → Creating 4 messages with known text")

        val messages = createMessages(4)
        val scenario = launchWithMessages(messages)

        println("    → Asserting first message text is visible")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(hasDescendant(withText("Hey, how are you?"))))

        println("    → Asserting second message text is visible")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(hasDescendant(withText("I'm doing great, thanks!"))))

        println("    ✅ Multiple messages render correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Loading state shows shimmer
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun loadingState_showsShimmer() {
        println("  🧪 loadingState_showsShimmer")
        println("    → Launching with loading repository (never completes)")

        // The shimmer animation keeps the main looper busy, so we need to
        // temporarily relax Espresso's idle policy to avoid AppNotIdleException.
        androidx.test.espresso.IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.SECONDS)
        androidx.test.espresso.IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.SECONDS)

        MessageListHostFragment.injectedRepository = LoadingMessageListRepository()
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Give the fragment time to render the loading state
        Thread.sleep(1000)

        println("    → Asserting loading state view is displayed")
        try {
            onView(withId(R.id.loading_state_view))
                .check(matches(isDisplayed()))
            println("    ✅ Loading state renders correctly")
        } catch (e: androidx.test.espresso.AppNotIdleException) {
            // If Espresso still can't idle due to shimmer, verify via the fragment directly
            println("    → Espresso idle timeout (expected with shimmer), verifying via scenario")
            scenario.onFragment { fragment ->
                val frameLayout = fragment.requireView() as FrameLayout
                val messageListView = frameLayout.getChildAt(0)
                val loadingView = messageListView.findViewById<View>(R.id.loading_state_view)
                assertEquals("Loading state view should be visible", View.VISIBLE, loadingView.visibility)
            }
            println("    ✅ Loading state renders correctly (verified via fragment)")
        } finally {
            // Reset idle policies to defaults
            androidx.test.espresso.IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS)
            androidx.test.espresso.IdlingPolicies.setIdlingResourceTimeout(26, TimeUnit.SECONDS)
            scenario.close()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: New message indicator is initially hidden
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun newMessageIndicator_initiallyHidden() {
        println("  🧪 newMessageIndicator_initiallyHidden")
        println("    → Launching with 5 messages")

        val messages = createMessages(5)
        val scenario = launchWithMessages(messages)

        println("    → Asserting new message indicator is NOT displayed")
        onView(withId(R.id.new_message_indicator))
            .check(matches(not(isDisplayed())))

        println("    ✅ New message indicator is hidden initially")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Content state hides loading and error views
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun contentState_hidesLoadingAndErrorViews() {
        println("  🧪 contentState_hidesLoadingAndErrorViews")
        println("    → Launching with 3 messages")

        val messages = createMessages(3)
        val scenario = launchWithMessages(messages)

        println("    → Asserting loading state is NOT displayed")
        onView(withId(R.id.loading_state_view))
            .check(matches(not(isDisplayed())))

        println("    → Asserting error state is NOT displayed")
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        println("    → Asserting empty state is NOT displayed")
        onView(withId(R.id.empty_state_view))
            .check(matches(not(isDisplayed())))

        println("    ✅ Content state hides other state views")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: Pagination indicator is initially hidden
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun paginationIndicator_initiallyHidden() {
        println("  🧪 paginationIndicator_initiallyHidden")
        println("    → Launching with 5 messages")

        val messages = createMessages(5)
        val scenario = launchWithMessages(messages)

        println("    → Asserting top pagination indicator is NOT displayed")
        onView(withId(R.id.top_pagination_indicator))
            .check(matches(not(isDisplayed())))

        println("    → Asserting bottom pagination indicator is NOT displayed")
        onView(withId(R.id.bottom_pagination_indicator))
            .check(matches(not(isDisplayed())))

        println("    ✅ Pagination indicators are hidden initially")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 10: onLoad callback invoked when content loads
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onLoadCallback_invokedWhenContentLoads() {
        println("  🧪 onLoadCallback_invokedWhenContentLoads")
        println("    → Launching with 3 messages")

        val messages = createMessages(3)
        val scenario = launchWithMessages(messages)

        // Wait for content to load
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(isDisplayed()))

        println("    → Asserting onLoad callback was invoked")
        assertEquals(
            "onLoad callback should have been invoked",
            true,
            MessageListHostFragment.onLoadInvoked
        )

        println("    ✅ onLoad callback invoked correctly")
        scenario.close()
    }
}
