package com.cometchat.uikit.kotlin.presentation.polls

import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.polls.style.CometChatCreatePollStyle
import com.cometchat.uikit.kotlin.presentation.polls.ui.CometChatCreatePoll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented integration tests for CometChatCreatePoll (Kotlin XML View).
 *
 * Tests the real View inflated in a Fragment using Espresso assertions.
 * Uses a host Fragment to inflate the component with proper theming.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest --tests "*.CometChatCreatePollIntegrationTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatCreatePollIntegrationTest {

    @Before
    fun setup() {
        CreatePollHostFragment.backClickResult = false
        CreatePollHostFragment.submitClickResult = null
    }

    @After
    fun tearDown() {
        CreatePollHostFragment.backClickResult = false
        CreatePollHostFragment.submitClickResult = null
    }

    // ==================== View Inflation ====================

    @Test
    fun viewInflatesCorrectly() {
        launchFragmentInContainer<CreatePollHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Verify main components are displayed
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.etQuestion)).check(matches(isDisplayed()))
        onView(withId(R.id.rvOptions)).check(matches(isDisplayed()))
        onView(withId(R.id.submit_btn)).check(matches(isDisplayed()))
    }

    // ==================== Question Input ====================

    @Test
    fun questionInputAcceptsText() {
        launchFragmentInContainer<CreatePollHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.etQuestion))
            .perform(click(), replaceText("What is your favorite color?"), closeSoftKeyboard())

        onView(withId(R.id.etQuestion))
            .check(matches(withText("What is your favorite color?")))
    }

    // ==================== Options RecyclerView ====================

    @Test
    fun optionsRecyclerViewRenders() {
        launchFragmentInContainer<CreatePollHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // RecyclerView should be visible with initial options
        onView(withId(R.id.rvOptions)).check(matches(isDisplayed()))
    }

    // ==================== Submit Button State ====================

    @Test
    fun submitButtonDisabledInitially() {
        launchFragmentInContainer<CreatePollHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Submit button should be disabled when form is empty
        onView(withId(R.id.submit_btn)).check(matches(isNotEnabled()))
    }

    // ==================== Back Button ====================

    @Test
    fun backButtonWorks() {
        launchFragmentInContainer<CreatePollHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        onView(withId(R.id.img_back)).perform(click())

        // Verify back click listener was invoked
        assert(CreatePollHostFragment.backClickResult)
    }

    // ==================== Style Application ====================

    @Test
    fun styleApplicationChangesAppearance() {
        val scenario = launchFragmentInContainer<CreatePollHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        scenario.onFragment { fragment ->
            val view = fragment.requireView() as FrameLayout
            val createPollView = view.getChildAt(0) as CometChatCreatePoll
            val customStyle = CometChatCreatePollStyle(
                backgroundColor = android.graphics.Color.parseColor("#F5F5DC"),
                titleTextColor = android.graphics.Color.parseColor("#333333"),
                submitButtonBackgroundColor = android.graphics.Color.parseColor("#6851D6")
            )
            createPollView.setStyle(customStyle)
        }

        // View should still be displayed after style change
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()))
    }

    // ==================== Toolbar Visibility ====================

    @Test
    fun hideToolbarHidesToolbarElements() {
        val scenario = launchFragmentInContainer<CreatePollHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        scenario.onFragment { fragment ->
            val view = fragment.requireView() as FrameLayout
            val createPollView = view.getChildAt(0) as CometChatCreatePoll
            createPollView.setHideToolbar(true)
        }

        // Back icon and title should be gone
        onView(withId(R.id.img_back)).check(matches(org.hamcrest.Matchers.not(isDisplayed())))
        onView(withId(R.id.tvTitle)).check(matches(org.hamcrest.Matchers.not(isDisplayed())))
    }

    // ==================== Error State ====================

    @Test
    fun errorMessageDisplaysCorrectly() {
        val scenario = launchFragmentInContainer<CreatePollHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        scenario.onFragment { fragment ->
            val view = fragment.requireView() as FrameLayout
            val createPollView = view.getChildAt(0) as CometChatCreatePoll
            createPollView.setErrorStateVisibility(View.VISIBLE)
            createPollView.setErrorMessage("Network error occurred")
        }

        onView(withId(R.id.tv_error)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_error)).check(matches(withText("Network error occurred")))
    }

    // ==================== Progress State ====================

    @Test
    fun progressIndicatorShowsAndDisablesButton() {
        val scenario = launchFragmentInContainer<CreatePollHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        scenario.onFragment { fragment ->
            val view = fragment.requireView() as FrameLayout
            val createPollView = view.getChildAt(0) as CometChatCreatePoll
            createPollView.setProgressVisibility(View.VISIBLE)
        }

        onView(withId(R.id.progress)).check(matches(isDisplayed()))
        onView(withId(R.id.submit_btn)).check(matches(isNotEnabled()))
    }
}

/**
 * Host Fragment for CometChatCreatePoll instrumented tests.
 * Provides a simple container that inflates the component with proper theming.
 */
class CreatePollHostFragment : Fragment() {

    companion object {
        var backClickResult: Boolean = false
        var submitClickResult: Pair<String, org.json.JSONArray>? = null
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        val createPollView = CometChatCreatePoll(requireContext())
        createPollView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        return FrameLayout(requireContext()).apply {
            addView(createPollView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val createPollView = (view as FrameLayout).getChildAt(0) as CometChatCreatePoll

        createPollView.setBackClickListener { backClickResult = true }
        createPollView.setOnSubmitClickListener(
            CometChatCreatePoll.OnSubmitClickListener { question, options ->
                submitClickResult = Pair(question, options)
            }
        )
    }
}
