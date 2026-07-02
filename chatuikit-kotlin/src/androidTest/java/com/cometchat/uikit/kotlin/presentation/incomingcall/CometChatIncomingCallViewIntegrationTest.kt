package com.cometchat.uikit.kotlin.presentation.incomingcall

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Espresso integration test for CometChatIncomingCall (XML/Kotlin).
 *
 * This test inflates the CometChatIncomingCall view inside a Fragment,
 * sets a mock Call, and uses Espresso to assert on the rendered UI
 * (caller name, call type, button visibility, click interactions).
 *
 * Architecture:
 *   [Mock Call] → [Real CometChatIncomingCall View] → [Espresso assertions]
 *
 * Since CometChatIncomingCall creates its own ViewModel internally,
 * we test through the public API: setCall, setOnAcceptClickListener, setOnRejectClickListener.
 * Custom click handlers override the default call behavior, allowing us to
 * verify button interactions without making real SDK calls.
 *
 * Requirements: 7a.1, 7a.4, 7a.5, 7a.6, 7a.7, 7a.10, 7a.11
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.incomingcall.CometChatIncomingCallViewIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatIncomingCallViewIntegrationTest {

    @Before
    fun setup() {
        IncomingCallHostFragment.injectedCall = null
        IncomingCallHostFragment.onAcceptClickResult = false
        IncomingCallHostFragment.onRejectClickResult = false
        IncomingCallHostFragment.onErrorResult = null
        IncomingCallHostFragment.useCustomItemView = false
        IncomingCallHostFragment.useCustomLeadingView = false
        IncomingCallHostFragment.useCustomTitleView = false
        IncomingCallHostFragment.useCustomSubtitleView = false
        IncomingCallHostFragment.useCustomTrailingView = false
    }

    @After
    fun tearDown() {
        IncomingCallHostFragment.injectedCall = null
        IncomingCallHostFragment.onAcceptClickResult = false
        IncomingCallHostFragment.onRejectClickResult = false
        IncomingCallHostFragment.onErrorResult = null
        IncomingCallHostFragment.useCustomItemView = false
        IncomingCallHostFragment.useCustomLeadingView = false
        IncomingCallHostFragment.useCustomTitleView = false
        IncomingCallHostFragment.useCustomSubtitleView = false
        IncomingCallHostFragment.useCustomTrailingView = false
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mock Factory
    // ─────────────────────────────────────────────────────────────────────────

    private fun createMockUser(
        uid: String = "user-1",
        name: String = "Test Caller"
    ): User {
        val user = mock(User::class.java)
        `when`(user.uid).thenReturn(uid)
        `when`(user.name).thenReturn(name)
        `when`(user.avatar).thenReturn(null)
        `when`(user.status).thenReturn("online")
        return user
    }

    private fun createMockCall(
        sessionId: String = "session-1",
        callType: String = CometChatConstants.CALL_TYPE_AUDIO,
        caller: User = createMockUser()
    ): Call {
        val call = mock(Call::class.java)
        `when`(call.sessionId).thenReturn(sessionId)
        `when`(call.type).thenReturn(callType)
        `when`(call.callInitiator).thenReturn(caller)
        return call
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: Launch the fragment
    // ─────────────────────────────────────────────────────────────────────────

    private fun launchWithCall(call: Call): FragmentScenario<IncomingCallHostFragment> {
        IncomingCallHostFragment.injectedCall = call
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Incoming call displays caller name
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_displaysCallerName() {
        val caller = createMockUser(name = "Alice Johnson")
        val call = createMockCall(caller = caller)
        val scenario = launchWithCall(call)

        onView(withText("Alice Johnson"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Incoming audio call displays correct call type text
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_displaysAudioCallTypeText() {
        val call = createMockCall(callType = CometChatConstants.CALL_TYPE_AUDIO)
        val scenario = launchWithCall(call)

        // The format is "Incoming %s Call" with %s = "audio"
        onView(withText(containsString("audio")))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Incoming video call displays correct call type text
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_displaysVideoCallTypeText() {
        val call = createMockCall(callType = CometChatConstants.CALL_TYPE_VIDEO)
        val scenario = launchWithCall(call)

        // The format is "Incoming %s Call" with %s = "video"
        onView(withText(containsString("video")))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Incoming call displays accept button
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_displaysAcceptButton() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Accept button has content description "Accept call"
        onView(withContentDescription("Accept call"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Incoming call displays decline button
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_displaysDeclineButton() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Decline button has content description "Decline call"
        onView(withContentDescription("Decline call"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Accept button click triggers callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_acceptButtonClick_triggersCallback() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        onView(withContentDescription("Accept call"))
            .perform(click())

        assertTrue(
            "Accept click callback was not triggered",
            IncomingCallHostFragment.onAcceptClickResult
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Decline button click triggers callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_declineButtonClick_triggersCallback() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        onView(withContentDescription("Decline call"))
            .perform(click())

        assertTrue(
            "Reject click callback was not triggered",
            IncomingCallHostFragment.onRejectClickResult
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Accept button displays correct text
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_acceptButton_displaysCorrectText() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Accept button text is "Accept"
        onView(withText("Accept"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: Decline button displays correct text
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_declineButton_displaysCorrectText() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Decline button text is "Decline"
        onView(withText("Decline"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 10: Caller avatar is displayed
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun incomingCall_displaysCallerAvatar() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Avatar has content description "Caller avatar"
        onView(withContentDescription("Caller avatar"))
            .check(matches(isDisplayed()))

        scenario.close()
    }
}
