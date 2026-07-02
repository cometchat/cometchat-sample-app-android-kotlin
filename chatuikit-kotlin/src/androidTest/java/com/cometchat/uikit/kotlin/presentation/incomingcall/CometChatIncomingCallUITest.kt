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
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Espresso UI tests for CometChatIncomingCall callbacks and custom view slots.
 *
 * Tests verify:
 * - Custom accept callback is invoked on accept click
 * - Custom reject callback is invoked on reject click
 * - Custom item view replaces default content
 * - Custom title view replaces default title
 * - Custom subtitle view replaces default subtitle
 *
 * Architecture:
 *   [Mock Call] → [Real CometChatIncomingCall View] → [Custom callbacks/views] → [Espresso assertions]
 *
 * Requirements: 7a.10, 7a.11, 7a.18
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.incomingcall.CometChatIncomingCallUITest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatIncomingCallUITest {

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
    // TEST 1: Custom accept callback is invoked on accept click
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customAcceptCallback_invoked_onAcceptClick() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Click accept button
        onView(withContentDescription("Accept call"))
            .perform(click())

        // Assert: Custom accept callback was invoked
        assertTrue(
            "Custom accept callback was not invoked",
            IncomingCallHostFragment.onAcceptClickResult
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Custom reject callback is invoked on reject click
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customRejectCallback_invoked_onRejectClick() {
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Click decline button
        onView(withContentDescription("Decline call"))
            .perform(click())

        // Assert: Custom reject callback was invoked
        assertTrue(
            "Custom reject callback was not invoked",
            IncomingCallHostFragment.onRejectClickResult
        )

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Custom item view replaces default content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customItemView_replacesDefaultContent() {
        IncomingCallHostFragment.useCustomItemView = true
        val caller = createMockUser(name = "Bob Smith")
        val call = createMockCall(caller = caller)
        val scenario = launchWithCall(call)

        // Assert: Custom item view is displayed
        onView(withText("Custom Item View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Custom title view replaces default title
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customTitleView_replacesDefaultTitle() {
        IncomingCallHostFragment.useCustomTitleView = true
        val caller = createMockUser(name = "Charlie Brown")
        val call = createMockCall(caller = caller)
        val scenario = launchWithCall(call)

        // Assert: Custom title view is displayed
        onView(withText("Custom Title View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Custom subtitle view replaces default subtitle
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customSubtitleView_replacesDefaultSubtitle() {
        IncomingCallHostFragment.useCustomSubtitleView = true
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Assert: Custom subtitle view is displayed
        onView(withText("Custom Subtitle View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }
}
