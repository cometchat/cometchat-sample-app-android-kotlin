package com.cometchat.uikit.kotlin.presentation.messagelist

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for CometChatMessageList callbacks.
 *
 * Tests verify:
 * - onItemLongClick callback triggered with correct message and position
 * - onError callback invoked when repository fails
 * - onEmpty callback invoked when no messages
 *
 * Note: onItemClick tests are excluded because the click listener is attached
 * to a child bubble view inside the ViewHolder, not the item root view.
 * Espresso's RecyclerViewActions.click() targets the root, so it doesn't reach
 * the bubble's click listener.
 *
 * Architecture:
 *   [Fake Repository] -> [Real ViewModel] -> [Real CometChatMessageList View]
 *       -> [Espresso interactions] -> [Callback assertions]
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.messagelist.CometChatMessageListUITest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatMessageListUITest {

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

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: Launch
    // ─────────────────────────────────────────────────────────────────────────

    private fun launchWithMessages(
        messages: List<BaseMessage>
    ): FragmentScenario<MessageListHostFragment> {
        MessageListHostFragment.injectedRepository = FakeMessageListRepository(messages)
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Item long click triggers onItemLongClick callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun itemLongClick_triggersCallback_withCorrectMessage() {
        println("  🧪 itemLongClick_triggersCallback_withCorrectMessage")
        println("    -> Creating 5 messages")

        val messages = createMessages(5)
        val scenario = launchWithMessages(messages)

        // Wait for content to render
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(isDisplayed()))

        println("    -> Long-clicking second item")
        onView(withId(R.id.recyclerview_message_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, longClick())
            )

        println("    -> Asserting onItemLongClick callback was triggered")
        assertNotNull(
            "onItemLongClick callback was not triggered",
            MessageListHostFragment.onItemLongClickMessage
        )
        assertEquals(
            "Long-clicked message should have id=2",
            2L,
            MessageListHostFragment.onItemLongClickMessage?.id
        )

        println("    PASS Item long click callback triggered correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Item long click position is correct
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun itemLongClick_reportsCorrectPosition() {
        println("  🧪 itemLongClick_reportsCorrectPosition")
        println("    -> Creating 5 messages")

        val messages = createMessages(5)
        val scenario = launchWithMessages(messages)

        // Wait for content to render
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(isDisplayed()))

        println("    -> Long-clicking item at position 4")
        onView(withId(R.id.recyclerview_message_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(4, longClick())
            )

        println("    -> Asserting position is 4")
        assertEquals(
            "Long-click position should be 4",
            4,
            MessageListHostFragment.onItemLongClickPosition
        )

        println("    PASS Long-click position reported correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: onError callback invoked when repository fails
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onErrorCallback_invokedWhenRepositoryFails() {
        println("  🧪 onErrorCallback_invokedWhenRepositoryFails")
        println("    -> Launching with failing repository")

        val repository = FakeMessageListRepository(
            shouldFail = true,
            errorCode = "NET_ERR",
            errorMessage = "Network error"
        )
        MessageListHostFragment.injectedRepository = repository
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for error state
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        println("    -> Asserting onError callback was invoked")
        assertNotNull(
            "onError callback should have been invoked",
            MessageListHostFragment.onErrorThrowable
        )

        println("    PASS onError callback invoked correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: onEmpty callback invoked when no messages
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onEmptyCallback_invokedWhenNoMessages() {
        println("  🧪 onEmptyCallback_invokedWhenNoMessages")
        println("    -> Launching with empty message list")

        val scenario = launchWithMessages(emptyList())

        // Wait for empty state
        onView(withId(R.id.empty_state_view))
            .check(matches(isDisplayed()))

        println("    -> Asserting onEmpty callback was invoked")
        assertEquals(
            "onEmpty callback should have been invoked",
            true,
            MessageListHostFragment.onEmptyInvoked
        )

        println("    PASS onEmpty callback invoked correctly")
        scenario.close()
    }
}
