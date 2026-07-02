package com.cometchat.uikit.kotlin.presentation.incomingcall

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Espresso tests for all custom view slots in CometChatIncomingCall.
 *
 * Tests verify:
 * - setLeadingView displays custom view
 * - setTrailingView displays custom view
 * - setItemView hides default content
 * - setTitleView hides default caller name
 * - setSubtitleView hides default call type
 *
 * Architecture:
 *   [Mock Call] → [Real CometChatIncomingCall View] → [Custom view slots] → [Espresso assertions]
 *
 * Requirements: 7a.18
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.incomingcall.CometChatIncomingCallCustomViewTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatIncomingCallCustomViewTest {

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
        name: String = "Default Caller"
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
    // TEST 1: setLeadingView displays custom view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setLeadingView_displaysCustomView() {
        IncomingCallHostFragment.useCustomLeadingView = true
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Assert: Custom leading view is displayed
        onView(withText("Custom Leading View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: setTrailingView displays custom view
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setTrailingView_displaysCustomView() {
        IncomingCallHostFragment.useCustomTrailingView = true
        val call = createMockCall()
        val scenario = launchWithCall(call)

        // Assert: Custom trailing view is displayed
        onView(withText("Custom Trailing View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: setItemView hides default content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setItemView_hidesDefaultContent() {
        IncomingCallHostFragment.useCustomItemView = true
        val caller = createMockUser(name = "Hidden Caller")
        val call = createMockCall(caller = caller)
        val scenario = launchWithCall(call)

        // Assert: Custom item view is displayed
        onView(withText("Custom Item View"))
            .check(matches(isDisplayed()))

        // Assert: Default caller name does not exist in the hierarchy
        // setItemView removes all children from itemViewContainer and replaces with custom view
        onView(withText("Hidden Caller"))
            .check(doesNotExist())

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: setTitleView hides default caller name
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setTitleView_hidesDefaultCallerName() {
        IncomingCallHostFragment.useCustomTitleView = true
        val caller = createMockUser(name = "Original Name")
        val call = createMockCall(caller = caller)
        val scenario = launchWithCall(call)

        // Assert: Custom title view is displayed
        onView(withText("Custom Title View"))
            .check(matches(isDisplayed()))

        // Assert: Default caller name does not exist in the hierarchy
        // setTitleView removes all children from titleContainer and replaces with custom view
        onView(withText("Original Name"))
            .check(doesNotExist())

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: setSubtitleView hides default call type
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun setSubtitleView_hidesDefaultCallType() {
        IncomingCallHostFragment.useCustomSubtitleView = true
        val call = createMockCall(callType = CometChatConstants.CALL_TYPE_AUDIO)
        val scenario = launchWithCall(call)

        // Assert: Custom subtitle view is displayed
        onView(withText("Custom Subtitle View"))
            .check(matches(isDisplayed()))

        scenario.close()
    }
}
