package com.cometchat.uikit.kotlin.presentation.callbuttons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * True UI Integration Test for CometChatCallButtons (XML/Kotlin).
 *
 * This test inflates the CometChatCallButtons view inside a Fragment,
 * sets a User or Group, and uses Espresso to assert on the rendered UI
 * (button visibility, click interactions, callbacks).
 *
 * Architecture:
 *   [Real CometChatCallButtons View] → [Custom click callbacks] → [Espresso assertions]
 *
 * Since CometChatCallButtons creates its own ViewModel internally via the factory,
 * we test through the public API: setUser/setGroup, setOnVoiceCallClick/setOnVideoCallClick.
 * Custom click handlers override the default call initiation behavior, allowing us to
 * verify button interactions without making real SDK calls.
 *
 * Requirements: 16.1, 16.2, 16.4, 16.6, 27.7, 32.1, 32.2, 32.5
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.kotlin.presentation.callbuttons.CometChatCallButtonsViewIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CometChatCallButtonsViewIntegrationTest {

    @Before
    fun setup() {
        CallButtonsHostFragment.injectedUser = null
        CallButtonsHostFragment.injectedGroup = null
        CallButtonsHostFragment.onVoiceCallClickUser = null
        CallButtonsHostFragment.onVoiceCallClickGroup = null
        CallButtonsHostFragment.onVideoCallClickUser = null
        CallButtonsHostFragment.onVideoCallClickGroup = null
        CallButtonsHostFragment.voiceCallClicked = false
        CallButtonsHostFragment.videoCallClicked = false
        CallButtonsHostFragment.hideVoiceCall = false
        CallButtonsHostFragment.hideVideoCall = false
        CallButtonsHostFragment.voiceCallText = null
        CallButtonsHostFragment.videoCallText = null
    }

    @After
    fun tearDown() {
        CallButtonsHostFragment.injectedUser = null
        CallButtonsHostFragment.injectedGroup = null
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

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: Launch the fragment
    // ─────────────────────────────────────────────────────────────────────────

    private fun launchWithUser(user: User): FragmentScenario<CallButtonsHostFragment> {
        CallButtonsHostFragment.injectedUser = user
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    private fun launchWithGroup(group: Group): FragmentScenario<CallButtonsHostFragment> {
        CallButtonsHostFragment.injectedGroup = group
        return launchFragmentInContainer(
            themeResId = R.style.CometChatTheme_DayNight
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Both call buttons are displayed when user is set
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun userSet_bothCallButtonsDisplayed() {
        val user = createMockUser()
        val scenario = launchWithUser(user)

        // Assert: Both voice and video call containers are displayed
        onView(withId(R.id.voice_call_container))
            .check(matches(isDisplayed()))
        onView(withId(R.id.video_call_container))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Both call buttons are displayed when group is set
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun groupSet_bothCallButtonsDisplayed() {
        val group = createMockGroup()
        val scenario = launchWithGroup(group)

        // Assert: Both voice and video call containers are displayed
        onView(withId(R.id.voice_call_container))
            .check(matches(isDisplayed()))
        onView(withId(R.id.video_call_container))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Voice call button click triggers callback with correct user
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun voiceCallClick_triggersCallback_withCorrectUser() {
        val user = createMockUser(uid = "user-42", name = "Alice")
        val scenario = launchWithUser(user)

        // Click voice call button
        onView(withId(R.id.voice_call_container))
            .perform(click())

        // Assert: Callback was triggered
        assertTrue(
            "Voice call click callback was not triggered",
            CallButtonsHostFragment.voiceCallClicked
        )
        // Assert: Correct user was passed
        assertNotNull(CallButtonsHostFragment.onVoiceCallClickUser)
        assertEquals("user-42", CallButtonsHostFragment.onVoiceCallClickUser?.uid)
        // Assert: Group is null for user call
        assertNull(CallButtonsHostFragment.onVoiceCallClickGroup)

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Video call button click triggers callback with correct user
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun videoCallClick_triggersCallback_withCorrectUser() {
        val user = createMockUser(uid = "user-99", name = "Bob")
        val scenario = launchWithUser(user)

        // Click video call button
        onView(withId(R.id.video_call_container))
            .perform(click())

        // Assert: Callback was triggered
        assertTrue(
            "Video call click callback was not triggered",
            CallButtonsHostFragment.videoCallClicked
        )
        // Assert: Correct user was passed
        assertNotNull(CallButtonsHostFragment.onVideoCallClickUser)
        assertEquals("user-99", CallButtonsHostFragment.onVideoCallClickUser?.uid)
        // Assert: Group is null for user call
        assertNull(CallButtonsHostFragment.onVideoCallClickGroup)

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Voice call button click triggers callback with correct group
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun voiceCallClick_triggersCallback_withCorrectGroup() {
        val group = createMockGroup(guid = "group-7", name = "Dev Team")
        val scenario = launchWithGroup(group)

        // Click voice call button
        onView(withId(R.id.voice_call_container))
            .perform(click())

        // Assert: Callback was triggered
        assertTrue(
            "Voice call click callback was not triggered",
            CallButtonsHostFragment.voiceCallClicked
        )
        // Assert: Correct group was passed
        assertNotNull(CallButtonsHostFragment.onVoiceCallClickGroup)
        assertEquals("group-7", CallButtonsHostFragment.onVoiceCallClickGroup?.guid)
        // Assert: User is null for group call
        assertNull(CallButtonsHostFragment.onVoiceCallClickUser)

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Video call button click triggers callback with correct group
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun videoCallClick_triggersCallback_withCorrectGroup() {
        val group = createMockGroup(guid = "group-12", name = "QA Team")
        val scenario = launchWithGroup(group)

        // Click video call button
        onView(withId(R.id.video_call_container))
            .perform(click())

        // Assert: Callback was triggered
        assertTrue(
            "Video call click callback was not triggered",
            CallButtonsHostFragment.videoCallClicked
        )
        // Assert: Correct group was passed
        assertNotNull(CallButtonsHostFragment.onVideoCallClickGroup)
        assertEquals("group-12", CallButtonsHostFragment.onVideoCallClickGroup?.guid)
        // Assert: User is null for group call
        assertNull(CallButtonsHostFragment.onVideoCallClickUser)

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Voice call button hidden when visibility set to GONE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun voiceCallButton_hiddenWhenVisibilityGone() {
        CallButtonsHostFragment.hideVoiceCall = true
        val user = createMockUser()
        val scenario = launchWithUser(user)

        // Assert: Voice call container is NOT displayed
        onView(withId(R.id.voice_call_container))
            .check(matches(not(isDisplayed())))
        // Assert: Video call container IS still displayed
        onView(withId(R.id.video_call_container))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Video call button hidden when visibility set to GONE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun videoCallButton_hiddenWhenVisibilityGone() {
        CallButtonsHostFragment.hideVideoCall = true
        val user = createMockUser()
        val scenario = launchWithUser(user)

        // Assert: Video call container is NOT displayed
        onView(withId(R.id.video_call_container))
            .check(matches(not(isDisplayed())))
        // Assert: Voice call container IS still displayed
        onView(withId(R.id.voice_call_container))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: Voice call icons are displayed
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun callIcons_areDisplayed() {
        val user = createMockUser()
        val scenario = launchWithUser(user)

        // Assert: Both call icons are displayed
        onView(withId(R.id.voice_call_icon))
            .check(matches(isDisplayed()))
        onView(withId(R.id.video_call_icon))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 10: Button text displayed when set
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun buttonText_displayedWhenSet() {
        CallButtonsHostFragment.voiceCallText = "Voice"
        CallButtonsHostFragment.videoCallText = "Video"
        val user = createMockUser()
        val scenario = launchWithUser(user)

        // Assert: Text views are displayed
        onView(withId(R.id.voice_call_text))
            .check(matches(isDisplayed()))
        onView(withId(R.id.video_call_text))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Host Fragment: Wraps CometChatCallButtons for testing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A minimal Fragment that hosts CometChatCallButtons.
     *
     * The User/Group is injected via the companion object before launch.
     * Custom click handlers are set to capture callback invocations
     * without triggering real SDK calls.
     */
    class CallButtonsHostFragment : Fragment() {

        companion object {
            /** Injected before fragment launch — cleared after each test */
            var injectedUser: User? = null
            var injectedGroup: Group? = null

            /** Callback capture state */
            var onVoiceCallClickUser: User? = null
            var onVoiceCallClickGroup: Group? = null
            var onVideoCallClickUser: User? = null
            var onVideoCallClickGroup: Group? = null
            var voiceCallClicked: Boolean = false
            var videoCallClicked: Boolean = false

            /** Configuration flags */
            var hideVoiceCall: Boolean = false
            var hideVideoCall: Boolean = false
            var voiceCallText: String? = null
            var videoCallText: String? = null
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

            // Set custom click handlers to intercept calls
            callButtonsView.setOnVoiceCallClick { user, group ->
                voiceCallClicked = true
                onVoiceCallClickUser = user
                onVoiceCallClickGroup = group
            }
            callButtonsView.setOnVideoCallClick { user, group ->
                videoCallClicked = true
                onVideoCallClickUser = user
                onVideoCallClickGroup = group
            }

            // Apply visibility configuration
            if (hideVoiceCall) {
                callButtonsView.setVoiceCallButtonVisibility(View.GONE)
            }
            if (hideVideoCall) {
                callButtonsView.setVideoCallButtonVisibility(View.GONE)
            }

            // Apply text configuration
            voiceCallText?.let { callButtonsView.setVoiceButtonText(it) }
            videoCallText?.let { callButtonsView.setVideoButtonText(it) }
        }
    }
}
