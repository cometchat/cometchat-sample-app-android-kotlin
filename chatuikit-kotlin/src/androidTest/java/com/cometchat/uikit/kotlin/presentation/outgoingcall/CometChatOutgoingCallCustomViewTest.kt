package com.cometchat.uikit.kotlin.presentation.outgoingcall

import android.Manifest
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Espresso tests for all custom view slots in CometChatOutgoingCall.
 *
 * Tests verify:
 * - setTitleView displays custom view
 * - setSubtitleView displays custom view
 * - setAvatarView displays custom view
 * - setEndCallView displays custom view
 *
 * Architecture:
 *   [Mock Call] → [Real CometChatOutgoingCall View] → [Custom view slots] → [Espresso assertions]
 *
 * Requirements: 11a.19
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.outgoingcall.CometChatOutgoingCallCustomViewTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatOutgoingCallCustomViewTest {

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
        name: String = "Default Receiver"
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
    // TEST 1: setTitleView displays custom view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setTitleView_displaysCustomView() {
        OutgoingCallHostFragment.useCustomTitleView = true
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Assert: Custom title view is displayed
        onView(withText("Custom Title View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: setSubtitleView displays custom view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setSubtitleView_displaysCustomView() {
        OutgoingCallHostFragment.useCustomSubtitleView = true
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Assert: Custom subtitle view is displayed
        onView(withText("Custom Subtitle View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: setAvatarView displays custom view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setAvatarView_displaysCustomView() {
        OutgoingCallHostFragment.useCustomAvatarView = true
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Assert: Custom avatar view is displayed
        onView(withText("Custom Avatar View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: setEndCallView displays custom view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setEndCallView_displaysCustomView() {
        OutgoingCallHostFragment.useCustomEndCallView = true
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Assert: Custom end call view is displayed
        onView(withText("Custom End Call View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }
}
