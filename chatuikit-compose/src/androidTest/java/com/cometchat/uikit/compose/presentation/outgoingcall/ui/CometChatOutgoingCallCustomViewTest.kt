package com.cometchat.uikit.compose.presentation.outgoingcall.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.viewmodel.CometChatOutgoingCallViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Compose instrumented tests for CometChatOutgoingCall custom view slots.
 *
 * Tests verify:
 * - Custom titleView replaces default title (receiver name)
 * - Custom subtitleView replaces default subtitle ("Calling...")
 * - Custom avatarView displays custom content
 * - Custom endCallView displays custom content
 * - Multiple custom views can coexist
 *
 * Architecture:
 *   [Mock Call] → [Real CometChatOutgoingCall Composable with custom view lambdas]
 *       → [Compose Test assertions]
 *
 * Requirements: 11.19
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.outgoingcall.ui.CometChatOutgoingCallCustomViewTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatOutgoingCallCustomViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

    private fun createViewModel(): CometChatOutgoingCallViewModel {
        return CometChatOutgoingCallViewModel(enableListeners = false)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Custom titleView replaces default title
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customTitleView_replacesDefaultTitle() {
        val receiver = createMockUser(name = "Original Name")
        val call = createMockCall(receiver = receiver)
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    titleView = { _ ->
                        Text("Custom Title")
                    },
                    onEndCallClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: Custom title view is displayed
        composeTestRule.onNodeWithText("Custom Title").assertIsDisplayed()

        // Assert: Default receiver name is NOT displayed
        composeTestRule.onNodeWithText("Original Name").assertDoesNotExist()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Custom subtitleView replaces default subtitle
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customSubtitleView_replacesDefaultSubtitle() {
        val call = createMockCall()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    subtitleView = { _ ->
                        Text("Custom Subtitle")
                    },
                    onEndCallClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: Custom subtitle view is displayed
        composeTestRule.onNodeWithText("Custom Subtitle").assertIsDisplayed()

        // Assert: Default "calling ..." text is NOT displayed
        composeTestRule.onNodeWithText("calling ...", substring = true)
            .assertDoesNotExist()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Custom avatarView displays custom content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customAvatarView_displaysCustomContent() {
        val call = createMockCall()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    avatarView = { _ ->
                        Text("Custom Avatar")
                    },
                    onEndCallClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: Custom avatar view is displayed
        composeTestRule.onNodeWithText("Custom Avatar").assertIsDisplayed()

        // Assert: Default content (receiver name) is still displayed
        // because avatarView doesn't replace the title/subtitle
        composeTestRule.onNodeWithText("Default Receiver").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Custom endCallView displays custom content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customEndCallView_displaysCustomContent() {
        val call = createMockCall()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    endCallView = { _ ->
                        Text("Custom End Call")
                    },
                    onEndCallClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: Custom end call view is displayed
        composeTestRule.onNodeWithText("Custom End Call").assertIsDisplayed()

        // Assert: Default content (receiver name) is still displayed
        // because endCallView only replaces the end call button area
        composeTestRule.onNodeWithText("Default Receiver").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Multiple custom views can coexist
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleCustomViews_canCoexist() {
        val call = createMockCall()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = call,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    titleView = { _ ->
                        Text("Title Custom")
                    },
                    subtitleView = { _ ->
                        Text("Subtitle Custom")
                    },
                    avatarView = { _ ->
                        Text("Avatar Custom")
                    },
                    endCallView = { _ ->
                        Text("End Call Custom")
                    },
                    onEndCallClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: All custom views are displayed
        composeTestRule.onNodeWithText("Title Custom").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subtitle Custom").assertIsDisplayed()
        composeTestRule.onNodeWithText("Avatar Custom").assertIsDisplayed()
        composeTestRule.onNodeWithText("End Call Custom").assertIsDisplayed()

        // Assert: Default receiver name is NOT displayed (replaced by custom title)
        composeTestRule.onNodeWithText("Default Receiver").assertDoesNotExist()
    }
}
