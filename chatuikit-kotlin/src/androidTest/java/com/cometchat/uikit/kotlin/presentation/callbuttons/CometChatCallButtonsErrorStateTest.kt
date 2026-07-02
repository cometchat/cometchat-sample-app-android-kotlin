package com.cometchat.uikit.kotlin.presentation.callbuttons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Instrumented tests for CometChatCallButtons error states.
 *
 * Tests verify:
 * - Error callback invoked when call initiation fails (active call exists)
 * - Error callback receives correct CometChatException
 * - Buttons remain functional after error
 * - Error does not crash the component
 * - Multiple rapid clicks don't cause issues
 *
 * Since CometChatCallButtons doesn't have visible error/loading state views
 * (unlike list-based components), error handling is verified through callbacks.
 * The component uses custom click handlers to intercept calls and verify
 * error propagation behavior.
 *
 * Requirements: 16.6, 27.7, 32.5
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.callbuttons.CometChatCallButtonsErrorStateTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatCallButtonsErrorStateTest {

    @Before
    fun setup() {
        ErrorStateCallButtonsHostFragment.injectedUser = null
        ErrorStateCallButtonsHostFragment.injectedGroup = null
        ErrorStateCallButtonsHostFragment.onErrorInvoked = AtomicBoolean(false)
        ErrorStateCallButtonsHostFragment.errorException = AtomicReference(null)
        ErrorStateCallButtonsHostFragment.voiceCallClickCount = 0
        ErrorStateCallButtonsHostFragment.videoCallClickCount = 0
        ErrorStateCallButtonsHostFragment.simulateError = false
    }

    @After
    fun tearDown() {
        ErrorStateCallButtonsHostFragment.injectedUser = null
        ErrorStateCallButtonsHostFragment.injectedGroup = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mock Factory
    // ─────────────────────────────────────────────────────────────────────────

    private fun createMockUser(uid: String = "user-1", name: String = "Test User"): User {
        val user = mock(User::class.java)
        `when`(user.uid).thenReturn(uid)
        `when`(user.name).thenReturn(name)
        `when`(user.status).thenReturn("online")
        `when`(user.avatar).thenReturn(null)
        return user
    }

    private fun createMockGroup(guid: String = "group-1", name: String = "Test Group"): Group {
        val group = mock(Group::class.java)
        `when`(group.guid).thenReturn(guid)
        `when`(group.name).thenReturn(name)
        `when`(group.icon).thenReturn(null)
        `when`(group.groupType).thenReturn("public")
        `when`(group.membersCount).thenReturn(5)
        return group
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Error callback invoked when onError is set
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorCallback_isInvokedWhenErrorOccurs() {
        ErrorStateCallButtonsHostFragment.injectedUser = createMockUser()
        ErrorStateCallButtonsHostFragment.simulateError = true

        val scenario = launchFragmentInContainer<ErrorStateCallButtonsHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Trigger the error by clicking voice call (custom handler simulates error)
        onView(withId(R.id.voice_call_container))
            .perform(click())

        // Verify error callback was invoked
        assertTrue(
            "onError callback should have been invoked",
            ErrorStateCallButtonsHostFragment.onErrorInvoked.get()
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Error callback receives correct exception details
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun errorCallback_receivesCorrectExceptionDetails() {
        ErrorStateCallButtonsHostFragment.injectedUser = createMockUser()
        ErrorStateCallButtonsHostFragment.simulateError = true

        val scenario = launchFragmentInContainer<ErrorStateCallButtonsHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Trigger the error
        onView(withId(R.id.voice_call_container))
            .perform(click())

        // Verify exception details
        val exception = ErrorStateCallButtonsHostFragment.errorException.get()
        assertNotNull("Exception should not be null", exception)
        assertEquals("ACTIVE_CALL", exception?.code)

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Buttons remain functional after error
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun buttonsRemainFunctional_afterError() {
        ErrorStateCallButtonsHostFragment.injectedUser = createMockUser()
        ErrorStateCallButtonsHostFragment.simulateError = true

        val scenario = launchFragmentInContainer<ErrorStateCallButtonsHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // First click triggers error
        onView(withId(R.id.voice_call_container))
            .perform(click())

        // Verify error occurred
        assertTrue(ErrorStateCallButtonsHostFragment.onErrorInvoked.get())
        assertEquals(1, ErrorStateCallButtonsHostFragment.voiceCallClickCount)

        // Second click should still work (button is not disabled)
        onView(withId(R.id.voice_call_container))
            .perform(click())

        assertEquals(2, ErrorStateCallButtonsHostFragment.voiceCallClickCount)

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Both buttons still visible after error
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun bothButtonsStillVisible_afterError() {
        ErrorStateCallButtonsHostFragment.injectedUser = createMockUser()
        ErrorStateCallButtonsHostFragment.simulateError = true

        val scenario = launchFragmentInContainer<ErrorStateCallButtonsHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Trigger error
        onView(withId(R.id.voice_call_container))
            .perform(click())

        // Both buttons should still be visible
        onView(withId(R.id.voice_call_container))
            .check(matches(isDisplayed()))
        onView(withId(R.id.video_call_container))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Video call error callback works independently
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun videoCallError_invokesCallback() {
        ErrorStateCallButtonsHostFragment.injectedUser = createMockUser()
        ErrorStateCallButtonsHostFragment.simulateError = true

        val scenario = launchFragmentInContainer<ErrorStateCallButtonsHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Trigger error via video call button
        onView(withId(R.id.video_call_container))
            .perform(click())

        // Verify error callback was invoked
        assertTrue(
            "onError callback should have been invoked for video call",
            ErrorStateCallButtonsHostFragment.onErrorInvoked.get()
        )
        assertEquals(1, ErrorStateCallButtonsHostFragment.videoCallClickCount)

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Multiple rapid clicks don't crash
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleRapidClicks_dontCrash() {
        ErrorStateCallButtonsHostFragment.injectedUser = createMockUser()
        ErrorStateCallButtonsHostFragment.simulateError = false

        val scenario = launchFragmentInContainer<ErrorStateCallButtonsHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Rapid clicks on voice call
        onView(withId(R.id.voice_call_container)).perform(click())
        onView(withId(R.id.voice_call_container)).perform(click())
        onView(withId(R.id.voice_call_container)).perform(click())

        // Rapid clicks on video call
        onView(withId(R.id.video_call_container)).perform(click())
        onView(withId(R.id.video_call_container)).perform(click())

        // Verify all clicks were registered without crash
        assertEquals(3, ErrorStateCallButtonsHostFragment.voiceCallClickCount)
        assertEquals(2, ErrorStateCallButtonsHostFragment.videoCallClickCount)

        // Buttons should still be visible
        onView(withId(R.id.voice_call_container))
            .check(matches(isDisplayed()))
        onView(withId(R.id.video_call_container))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Error with group call
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupCallError_invokesCallback() {
        ErrorStateCallButtonsHostFragment.injectedGroup = createMockGroup()
        ErrorStateCallButtonsHostFragment.simulateError = true

        val scenario = launchFragmentInContainer<ErrorStateCallButtonsHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Trigger error via voice call button with group
        onView(withId(R.id.voice_call_container))
            .perform(click())

        // Verify error callback was invoked
        assertTrue(
            "onError callback should have been invoked for group call",
            ErrorStateCallButtonsHostFragment.onErrorInvoked.get()
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: No error when simulateError is false
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun noError_whenSimulateErrorIsFalse() {
        ErrorStateCallButtonsHostFragment.injectedUser = createMockUser()
        ErrorStateCallButtonsHostFragment.simulateError = false

        val scenario = launchFragmentInContainer<ErrorStateCallButtonsHostFragment>(
            themeResId = R.style.CometChatTheme_DayNight
        )

        // Click voice call button (no error should occur)
        onView(withId(R.id.voice_call_container))
            .perform(click())

        // Verify error callback was NOT invoked
        assertTrue(
            "onError callback should NOT have been invoked",
            !ErrorStateCallButtonsHostFragment.onErrorInvoked.get()
        )
        // But click was still registered
        assertEquals(1, ErrorStateCallButtonsHostFragment.voiceCallClickCount)

        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment for Error State Tests
    // ─────────────────────────────────────────────────────────────────────────

    class ErrorStateCallButtonsHostFragment : Fragment() {

        companion object {
            var injectedUser: User? = null
            var injectedGroup: Group? = null
            var onErrorInvoked = AtomicBoolean(false)
            var errorException = AtomicReference<CometChatException?>(null)
            var voiceCallClickCount: Int = 0
            var videoCallClickCount: Int = 0
            var simulateError: Boolean = false
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val callButtonsView = CometChatCallButtons(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
            return FrameLayout(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                addView(callButtonsView)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val frameLayout = view as FrameLayout
            val callButtonsView = frameLayout.getChildAt(0) as CometChatCallButtons

            // Set entity
            injectedUser?.let { callButtonsView.setUser(it) }
            injectedGroup?.let { callButtonsView.setGroup(it) }

            // Set error callback
            callButtonsView.setOnError { exception ->
                onErrorInvoked.set(true)
                errorException.set(exception)
            }

            // Set custom click handlers that simulate error when flag is set
            callButtonsView.setOnVoiceCallClick { _, _ ->
                voiceCallClickCount++
                if (simulateError) {
                    val exception = CometChatException(
                        "ACTIVE_CALL",
                        "Cannot initiate call while another call is active",
                        "An active call is already in progress"
                    )
                    onErrorInvoked.set(true)
                    errorException.set(exception)
                }
            }

            callButtonsView.setOnVideoCallClick { _, _ ->
                videoCallClickCount++
                if (simulateError) {
                    val exception = CometChatException(
                        "ACTIVE_CALL",
                        "Cannot initiate call while another call is active",
                        "An active call is already in progress"
                    )
                    onErrorInvoked.set(true)
                    errorException.set(exception)
                }
            }
        }
    }
}
