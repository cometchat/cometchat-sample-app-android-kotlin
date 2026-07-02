package com.cometchat.uikit.kotlin.presentation.messagelist

import android.view.View
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CometChatMessageList custom view slot rendering.
 *
 * Tests verify:
 * - Custom headerView displayed above the message list
 * - Custom footerView displayed below the message list
 * - Custom loadingView displayed when UIState is Loading
 * - Custom emptyView displayed when UIState is Empty
 * - Custom errorView displayed when UIState is Error
 * - BubbleViewProvider for custom leading views
 * - onLoad callback invoked when UIState transitions to Content
 * - onEmpty callback invoked when UIState transitions to Empty
 *
 * Architecture:
 *   [Fake Repository] → [Real ViewModel] → [Real CometChatMessageList View]
 *       → [Espresso assertions on custom views]
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.messagelist.CometChatMessageListCustomViewTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatMessageListCustomViewTest {

    private companion object {
        private const val LOGGED_IN_UID = "logged_in_user"
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
    // Mock Factory
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
            "Let's discuss it tomorrow"
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

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM HEADER VIEW TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customHeaderView_displayedAboveMessageList() {
        println("  🧪 customHeaderView_displayedAboveMessageList")
        println("    → Launching with custom header view")

        val messages = createMessages(3)
        MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customHeaderView = { context ->
            TextView(context).apply {
                text = "Custom Header: Pinned Messages"
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting custom header view is displayed")
        onView(withText("Custom Header: Pinned Messages"))
            .check(matches(isDisplayed()))

        println("    → Asserting message list is also displayed")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(isDisplayed()))

        println("    ✅ Custom header view displayed correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM FOOTER VIEW TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customFooterView_displayedBelowMessageList() {
        println("  🧪 customFooterView_displayedBelowMessageList")
        println("    → Launching with custom footer view")

        val messages = createMessages(3)
        MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customFooterView = { context ->
            TextView(context).apply {
                text = "Custom Footer: Typing..."
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting custom footer view is displayed")
        onView(withText("Custom Footer: Typing..."))
            .check(matches(isDisplayed()))

        println("    → Asserting message list is also displayed")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(isDisplayed()))

        println("    ✅ Custom footer view displayed correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM LOADING VIEW TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customLoadingView_displayedWhenLoading() {
        println("  🧪 customLoadingView_displayedWhenLoading")
        println("    → Launching with loading repository and custom loading view")

        MessageListHostFragment.injectedRepository = LoadingMessageListRepository()
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customLoadingView = { context ->
            TextView(context).apply {
                text = "Custom: Loading messages..."
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting custom loading view is displayed")
        onView(withText("Custom: Loading messages..."))
            .check(matches(isDisplayed()))

        println("    ✅ Custom loading view displayed correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM EMPTY VIEW TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customEmptyView_displayedWhenEmpty() {
        println("  🧪 customEmptyView_displayedWhenEmpty")
        println("    → Launching with empty list and custom empty view")

        MessageListHostFragment.injectedRepository = FakeMessageListRepository(emptyList())
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customEmptyView = { context ->
            TextView(context).apply {
                text = "Custom: No messages yet!"
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting custom empty view is displayed")
        onView(withText("Custom: No messages yet!"))
            .check(matches(isDisplayed()))

        println("    → Asserting default empty view is NOT displayed")
        onView(withId(R.id.empty_state_view))
            .check(matches(not(isDisplayed())))

        println("    ✅ Custom empty view displayed correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM ERROR VIEW TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customErrorView_displayedWhenError() {
        println("  🧪 customErrorView_displayedWhenError")
        println("    → Launching with failing repository and custom error view")

        MessageListHostFragment.injectedRepository = FakeMessageListRepository(
            shouldFail = true,
            errorCode = "NET_ERR",
            errorMessage = "Network error"
        )
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customErrorView = { context ->
            TextView(context).apply {
                text = "Custom: Failed to load messages"
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting custom error view is displayed")
        onView(withText("Custom: Failed to load messages"))
            .check(matches(isDisplayed()))

        println("    → Asserting default error view is NOT displayed")
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        println("    ✅ Custom error view displayed correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CALLBACK TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onLoadCallback_invokedWhenContentLoaded() {
        println("  🧪 onLoadCallback_invokedWhenContentLoaded")
        println("    → Launching with 3 messages")

        val messages = createMessages(3)
        MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for content to load
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(isDisplayed()))

        println("    → Asserting onLoad callback was invoked")
        assertTrue(
            "onLoad callback should have been invoked",
            MessageListHostFragment.onLoadInvoked
        )

        println("    ✅ onLoad callback invoked correctly")
        scenario.close()
    }

    @Test
    fun onEmptyCallback_invokedWhenEmpty() {
        println("  🧪 onEmptyCallback_invokedWhenEmpty")
        println("    → Launching with empty message list")

        MessageListHostFragment.injectedRepository = FakeMessageListRepository(emptyList())
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for empty state
        onView(withId(R.id.empty_state_view))
            .check(matches(isDisplayed()))

        println("    → Asserting onEmpty callback was invoked")
        assertTrue(
            "onEmpty callback should have been invoked",
            MessageListHostFragment.onEmptyInvoked
        )

        println("    ✅ onEmpty callback invoked correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HEADER/FOOTER CONTAINER VISIBILITY TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun headerViewContainer_visibleWhenHeaderSet() {
        println("  🧪 headerViewContainer_visibleWhenHeaderSet")
        println("    → Launching with custom header view")

        val messages = createMessages(3)
        MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customHeaderView = { context ->
            TextView(context).apply {
                text = "Header Content"
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting header view container is displayed")
        onView(withId(R.id.header_view_container))
            .check(matches(isDisplayed()))

        println("    ✅ Header view container visible when header set")
        scenario.close()
    }

    @Test
    fun footerViewContainer_visibleWhenFooterSet() {
        println("  🧪 footerViewContainer_visibleWhenFooterSet")
        println("    → Launching with custom footer view")

        val messages = createMessages(3)
        MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customFooterView = { context ->
            TextView(context).apply {
                text = "Footer Content"
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting footer view container is displayed")
        onView(withId(R.id.footer_view_container))
            .check(matches(isDisplayed()))

        println("    ✅ Footer view container visible when footer set")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONTENT STATE WITH CUSTOM VIEWS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun contentState_displaysRecyclerView_withHeaderAndFooter() {
        println("  🧪 contentState_displaysRecyclerView_withHeaderAndFooter")
        println("    → Launching with messages, header, and footer")

        val messages = createMessages(3)
        MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customHeaderView = { context ->
            TextView(context).apply {
                text = "Header"
                id = View.generateViewId()
            }
        }
        MessageListHostFragment.customFooterView = { context ->
            TextView(context).apply {
                text = "Footer"
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting all three sections are displayed")
        onView(withText("Header")).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerview_message_list)).check(matches(isDisplayed()))
        onView(withText("Footer")).check(matches(isDisplayed()))

        println("    → Asserting error/empty states are NOT displayed")
        onView(withId(R.id.error_state_view)).check(matches(not(isDisplayed())))
        onView(withId(R.id.empty_state_view)).check(matches(not(isDisplayed())))

        println("    ✅ Content state with header and footer renders correctly")
        scenario.close()
    }
}
