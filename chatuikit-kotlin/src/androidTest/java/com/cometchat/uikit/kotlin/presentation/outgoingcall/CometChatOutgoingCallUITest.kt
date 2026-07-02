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
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Espresso UI tests for CometChatOutgoingCall callbacks and custom view slots.
 *
 * Tests verify:
 * - Custom end call callback is invoked on end call click
 * - Custom title view replaces default title
 * - Custom subtitle view replaces default subtitle
 *
 * Architecture:
 *   [Mock Call] → [Real CometChatOutgoingCall View] → [Custom callbacks/views] → [Espresso assertions]
 *
 * Requirements: 11a.9, 11a.19
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.outgoingcall.CometChatOutgoingCallUITest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatOutgoingCallUITest {

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
    // TEST 1: Custom end call callback is invoked on end call click
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customEndCallCallback_invoked_onEndCallClick() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Click end call button
        onView(withContentDescription("End call"))
            .perform(click())

        // Assert: Custom end call callback was invoked
        assertTrue(
            "Custom end call callback was not invoked",
            OutgoingCallHostFragment.onEndCallClickResult
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Custom title view replaces default title
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customTitleView_replacesDefaultTitle() {
        OutgoingCallHostFragment.useCustomTitleView = true
        val receiver = createMockUser(name = "Bob Smith")
        val call = createMockCall(receiver = receiver)
        val scenario = launchWithCall(call)

        // Assert: Custom title view is displayed
        onView(withText("Custom Title View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Custom subtitle view replaces default subtitle
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customSubtitleView_replacesDefaultSubtitle() {
        OutgoingCallHostFragment.useCustomSubtitleView = true
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Assert: Custom subtitle view is displayed
        onView(withText("Custom Subtitle View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }
}
