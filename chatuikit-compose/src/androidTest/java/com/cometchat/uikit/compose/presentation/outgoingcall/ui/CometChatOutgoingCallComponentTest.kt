package com.cometchat.uikit.compose.presentation.outgoingcall.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.viewmodel.CometChatOutgoingCallViewModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Compose instrumented tests for CometChatOutgoingCall component.
 *
 * Tests verify:
 * - Receiver name is displayed
 * - Call type text ("Calling ...") is displayed
 * - End call button is displayed and clickable
 * - End call button click triggers callback
 * - End call button displays correct content description
 * - Component renders with dark theme without crash
 *
 * Architecture:
 *   [Mock Call] → [Real CometChatOutgoingCall Composable] → [Compose Test assertions]
 *
 * The ViewModel is created with enableListeners=false to avoid SDK initialization.
 * Custom onEndCallClick callback is used to avoid real SDK cancel calls.
 * Sound is disabled to avoid audio playback in test environment.
 *
 * Requirements: 11.1, 11.4, 11.5, 11.6, 11.7, 11.9
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.outgoingcall.ui.CometChatOutgoingCallComponentTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatOutgoingCallComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

    private fun createViewModel(): CometChatOutgoingCallViewModel {
        return CometChatOutgoingCallViewModel(enableListeners = false)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Outgoing call displays receiver name
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_displaysReceiverName() {
        val receiver = createMockUser(name = "Alice Johnson")
        val call = createMockCall(receiver = receiver)
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    onEndCallClick = { /* no-op to avoid SDK call */ }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Outgoing call displays call type text
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_displaysCallTypeText() {
        val call = createMockCall(callType = CometChatConstants.CALL_TYPE_AUDIO)
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    onEndCallClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()
        // The subtitle text is "calling ..." (from R.string.cometchat_calling + " ...")
        composeTestRule.onNodeWithText("calling", substring = true)
            .assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Outgoing call displays end call button
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_displaysEndCallButton() {
        val receiver = createMockUser(name = "Bob")
        val call = createMockCall(receiver = receiver)
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    onEndCallClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()
        // End call button has content description "End call with <receiverName>"
        composeTestRule.onNodeWithContentDescription("End call with Bob")
            .assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: End call button click triggers callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_endCallButtonClick_triggersCallback() {
        val receiver = createMockUser(name = "Diana")
        val call = createMockCall(receiver = receiver)
        val viewModel = createViewModel()
        val endCallClicked = AtomicBoolean(false)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    onEndCallClick = { _ ->
                        endCallClicked.set(true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("End call with Diana")
            .performClick()
        composeTestRule.waitForIdle()

        assertTrue(
            "End call click callback was not triggered",
            endCallClicked.get()
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: End call button displays correct content description
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun outgoingCall_endCallButton_displaysCorrectText() {
        val receiver = createMockUser(name = "Charlie")
        val call = createMockCall(receiver = receiver)
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    onEndCallClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()
        // Content description format: "End call with <receiverName>"
        composeTestRule.onNodeWithContentDescription("End call with Charlie")
            .assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Component renders with dark theme without crash
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun component_rendersWithDarkTheme() {
        val receiver = createMockUser(name = "Eve")
        val call = createMockCall(receiver = receiver)
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = com.cometchat.uikit.compose.theme.darkColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    onEndCallClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Eve").assertIsDisplayed()
    }
}
