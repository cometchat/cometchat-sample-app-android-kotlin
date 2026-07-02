package com.cometchat.uikit.kotlin.presentation.outgoingcall

import android.Manifest
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
import androidx.test.rule.GrantPermissionRule
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Espresso integration test for CometChatOutgoingCall (XML/Kotlin).
 *
 * This test inflates the CometChatOutgoingCall view inside a Fragment,
 * sets a mock Call, and uses Espresso to assert on the rendered UI
 * (receiver name, call type, end call button visibility, click interactions).
 *
 * Architecture:
 *   [Mock Call] → [Real CometChatOutgoingCall View] → [Espresso assertions]
 *
 * Since CometChatOutgoingCall creates its own ViewModel internally,
 * we test through the public API: setCall, setOnEndCallClickListener.
 * Custom click handlers override the default call behavior, allowing us to
 * verify button interactions without making real SDK calls.
 *
 * Requirements: 11a.1, 11a.4, 11a.5, 11a.6, 11a.7, 11a.9, 11a.10
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.outgoingcall.CometChatOutgoingCallViewIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatOutgoingCallViewIntegrationTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    @Before
    fun setup() {
        OutgoingCallHostFragment.injectedCall = null
        OutgoingCallHostFragment.onEndCallClickResult = false
        OutgoingCallHostFragment.onErrorResult = null
        OutgoingCallHostFragment.useCustomTitleView = false
        OutgoingCallHostFragment.useCustomSubtitleView = false
        OutgoingCallHostFragment.useCustomAvatarView = false
        OutgoingCallHostFragment.useCustomEndCallView = false
    }

    @After
    fun tearDown() {
        OutgoingCallHostFragment.injectedCall = null
        OutgoingCallHostFragment.onEndCallClickResult = false
        OutgoingCallHostFragment.onErrorResult = null
        OutgoingCallHostFragment.useCustomTitleView = false
        OutgoingCallHostFragment.useCustomSubtitleView = false
        OutgoingCallHostFragment.useCustomAvatarView = false
        OutgoingCallHostFragment.useCustomEndCallView = false
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mock Factory
    // ─────────────────────────────────────────────────────────────────────────

    private fun createMockUser(
        uid: String = "user-1",
        name: String = "Test Receiver"
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
        receiver: User = createMockUser()
    ): Call {
        val call = mock(Call::class.java)
        `when`(call.sessionId).thenReturn(sessionId)
        `when`(call.type).thenReturn(callType)
        `when`(call.receiver).thenReturn(receiver)
        `when`(call.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        return call
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: Launch the fragment
    // ─────────────────────────────────────────────────────────────────────────

    private fun launchWithCall(call: Call): FragmentScenario<OutgoingCallHostFragment> {
        OutgoingCallHostFragment.injectedCall = call
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Outgoing call displays receiver name
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_displaysReceiverName() {
        val receiver = createMockUser(name = "Alice Johnson")
        val call = createMockCall(receiver = receiver)
        val scenario = launchWithCall(call)

        onView(withText("Alice Johnson"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Outgoing audio call displays correct call type text
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_displaysAudioCallTypeText() {
        val call = createMockCall(callType = CometChatConstants.CALL_TYPE_AUDIO)
        val scenario = launchWithCall(call)

        // The subtitle text is "calling ..." (from R.string.cometchat_calling + " ...")
        onView(withText(containsString("calling")))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Outgoing video call displays correct call type text
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_displaysVideoCallTypeText() {
        val call = createMockCall(callType = CometChatConstants.CALL_TYPE_VIDEO)
        val scenario = launchWithCall(call)

        // The subtitle text is "calling ..." regardless of call type
        onView(withText(containsString("calling")))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Outgoing call displays end call button
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_displaysEndCallButton() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // End call button has content description "End call"
        onView(withContentDescription("End call"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: End call button click triggers callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_endCallButtonClick_triggersCallback() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        onView(withContentDescription("End call"))
            .perform(click())

        assertTrue(
            "End call click callback was not triggered",
            OutgoingCallHostFragment.onEndCallClickResult
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Outgoing call displays receiver avatar
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_displaysReceiverAvatar() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Avatar has content description "Recipient avatar"
        onView(withContentDescription("Recipient avatar"))
            .check(matches(isDisplayed()))

        scenario.close()
    }
}
