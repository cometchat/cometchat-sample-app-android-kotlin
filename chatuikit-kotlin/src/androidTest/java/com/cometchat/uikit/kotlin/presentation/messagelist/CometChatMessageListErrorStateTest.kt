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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CometChatMessageList error and loading states.
 *
 * Tests verify:
 * - Error state rendering (default and custom errorView)
 * - Loading state rendering (default shimmer and custom loadingView)
 * - hideErrorState / hideLoadingState flags
 * - onError callback invoked
 *
 * Architecture:
 *   [Fake Repository] → [Real ViewModel] → [Real CometChatMessageList View]
 *       → [Espresso assertions on state views]
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.messagelist.CometChatMessageListErrorStateTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatMessageListErrorStateTest {

    private companion object {
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

    // ═══════════════════════════════════════════════════════════════════════════
    // ERROR STATE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorState_displaysDefaultErrorView() {
        println("  🧪 errorState_displaysDefaultErrorView")
        println("    → Launching with failing repository")

        MessageListHostFragment.injectedRepository = FakeMessageListRepository(
            shouldFail = true,
            errorCode = "NET_ERR",
            errorMessage = "Network error"
        )
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting error state view is displayed")
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        println("    → Asserting RecyclerView is NOT displayed")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(not(isDisplayed())))

        println("    ✅ Default error view displayed correctly")
        scenario.close()
    }

    @Test
    fun errorState_displaysCustomErrorView() {
        println("  🧪 errorState_displaysCustomErrorView")
        println("    → Launching with failing repository and custom error view")

        MessageListHostFragment.injectedRepository = FakeMessageListRepository(
            shouldFail = true,
            errorCode = "NET_ERR",
            errorMessage = "Network error"
        )
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customErrorView = { context ->
            TextView(context).apply {
                text = "Custom Error: Something went wrong"
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting custom error view is displayed")
        onView(withText("Custom Error: Something went wrong"))
            .check(matches(isDisplayed()))

        println("    → Asserting default error view is NOT displayed")
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        println("    ✅ Custom error view displayed correctly")
        scenario.close()
    }

    @Test
    fun errorState_hiddenWhenHideErrorStateIsTrue() {
        println("  🧪 errorState_hiddenWhenHideErrorStateIsTrue")
        println("    → Launching with failing repository and hideErrorState=true")

        MessageListHostFragment.injectedRepository = FakeMessageListRepository(
            shouldFail = true,
            errorCode = "NET_ERR",
            errorMessage = "Network error"
        )
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.hideErrorState = true

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting error state view is NOT displayed")
        onView(withId(R.id.error_state_view))
            .check(matches(not(isDisplayed())))

        println("    ✅ Error state hidden when hideErrorState=true")
        scenario.close()
    }

    @Test
    fun errorState_invokesOnErrorCallback() {
        println("  🧪 errorState_invokesOnErrorCallback")
        println("    → Launching with failing repository")

        MessageListHostFragment.injectedRepository = FakeMessageListRepository(
            shouldFail = true,
            errorCode = "NET_ERR",
            errorMessage = "Network error"
        )
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Wait for error state
        onView(withId(R.id.error_state_view))
            .check(matches(isDisplayed()))

        println("    → Asserting onError callback was invoked")
        assertNotNull(
            "onError callback should have been invoked",
            MessageListHostFragment.onErrorThrowable
        )

        println("    ✅ onError callback invoked correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOADING STATE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun loadingState_displaysShimmer() {
        println("  🧪 loadingState_displaysShimmer")
        println("    → Launching with loading repository (never completes)")

        MessageListHostFragment.injectedRepository = LoadingMessageListRepository()
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting loading state view is displayed")
        onView(withId(R.id.loading_state_view))
            .check(matches(isDisplayed()))

        println("    → Asserting RecyclerView is NOT displayed")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(not(isDisplayed())))

        println("    ✅ Loading shimmer displayed correctly")
        scenario.close()
    }

    @Test
    fun loadingState_displaysCustomLoadingView() {
        println("  🧪 loadingState_displaysCustomLoadingView")
        println("    → Launching with loading repository and custom loading view")

        MessageListHostFragment.injectedRepository = LoadingMessageListRepository()
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customLoadingView = { context ->
            TextView(context).apply {
                text = "Custom Loading Messages..."
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting custom loading view is displayed")
        onView(withText("Custom Loading Messages..."))
            .check(matches(isDisplayed()))

        println("    ✅ Custom loading view displayed correctly")
        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EMPTY STATE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun emptyState_displaysDefaultEmptyView() {
        println("  🧪 emptyState_displaysDefaultEmptyView")
        println("    → Launching with empty message list")

        MessageListHostFragment.injectedRepository = FakeMessageListRepository(emptyList())
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting empty state view is displayed")
        onView(withId(R.id.empty_state_view))
            .check(matches(isDisplayed()))

        println("    → Asserting RecyclerView is NOT displayed")
        onView(withId(R.id.recyclerview_message_list))
            .check(matches(not(isDisplayed())))

        println("    ✅ Default empty view displayed correctly")
        scenario.close()
    }

    @Test
    fun emptyState_displaysCustomEmptyView() {
        println("  🧪 emptyState_displaysCustomEmptyView")
        println("    → Launching with empty list and custom empty view")

        MessageListHostFragment.injectedRepository = FakeMessageListRepository(emptyList())
        MessageListHostFragment.userToSet = createUser(OTHER_UID, OTHER_NAME)
        MessageListHostFragment.customEmptyView = { context ->
            TextView(context).apply {
                text = "No messages yet. Start a conversation!"
                id = View.generateViewId()
            }
        }

        val scenario = launchFragmentInContainer<MessageListHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        println("    → Asserting custom empty view is displayed")
        onView(withText("No messages yet. Start a conversation!"))
            .check(matches(isDisplayed()))

        println("    → Asserting default empty view is NOT displayed")
        onView(withId(R.id.empty_state_view))
            .check(matches(not(isDisplayed())))

        println("    ✅ Custom empty view displayed correctly")
        scenario.close()
    }

    @Test
    fun emptyState_invokesOnEmptyCallback() {
        println("  🧪 emptyState_invokesOnEmptyCallback")
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
}
