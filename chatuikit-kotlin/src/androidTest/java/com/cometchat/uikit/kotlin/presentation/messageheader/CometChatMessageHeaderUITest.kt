package com.cometchat.uikit.kotlin.presentation.messageheader

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.core.testutils.MockFactory
import com.cometchat.uikit.kotlin.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instrumented UI tests for CometChatMessageHeader callbacks and interactions.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest --tests "*CometChatMessageHeaderUITest"
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class CometChatMessageHeaderUITest {

    @Before
    fun setup() {
        MessageHeaderHostFragment.injectedUser = null
        MessageHeaderHostFragment.injectedGroup = null
        MessageHeaderHostFragment.showBackButton = false
    }

    @After
    fun tearDown() {
        MessageHeaderHostFragment.injectedUser = null
        MessageHeaderHostFragment.injectedGroup = null
        MessageHeaderHostFragment.showBackButton = false
    }

    @Test
    fun backButton_clickTriggersCallback() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageHeaderHostFragment.injectedUser = user
        MessageHeaderHostFragment.showBackButton = true

        val backPressed = AtomicBoolean(false)
        MessageHeaderCallbackHostFragment.onBackPressCallback = { backPressed.set(true) }

        launchFragmentInContainer<MessageHeaderCallbackHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.ivMessageHeaderBack))
            .check(matches(isDisplayed()))
            .perform(click())

        assert(backPressed.get()) { "Back press callback was not invoked" }
        MessageHeaderCallbackHostFragment.onBackPressCallback = null
    }

    @Test
    fun menuIcon_isHiddenByDefault() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageHeaderHostFragment.injectedUser = user

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.messageHeaderMenuIcon))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun backButton_isHiddenByDefault() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageHeaderHostFragment.injectedUser = user

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.ivMessageHeaderBack))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun statusIndicator_isVisibleForOnlineUser() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice", status = CometChatConstants.USER_STATUS_ONLINE)
        MessageHeaderHostFragment.injectedUser = user

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.messageHeaderStatusIndicatorView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun typingIndicator_isHiddenByDefault() {
        val user = MockFactory.createUser(uid = "user-1", name = "Alice")
        MessageHeaderHostFragment.injectedUser = user

        launchFragmentInContainer<MessageHeaderHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.tvMessageHeaderTypingIndicator))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}

/**
 * Host Fragment with callback support for testing interactions.
 */
class MessageHeaderCallbackHostFragment : MessageHeaderHostFragment() {
    companion object {
        var onBackPressCallback: (() -> Unit)? = null
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val header = (view as android.widget.FrameLayout).getChildAt(0) as com.cometchat.uikit.kotlin.presentation.messageheader.ui.CometChatMessageHeader
        header.setBackButtonVisibility(View.VISIBLE)
        onBackPressCallback?.let { callback ->
            header.setOnBackPress(callback)
        }
    }
}
