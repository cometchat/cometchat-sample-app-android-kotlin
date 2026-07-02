package com.cometchat.uikit.compose.presentation.incomingcall.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.cometchat.uikit.core.viewmodel.CometChatIncomingCallViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Compose instrumented tests for CometChatIncomingCall custom view slots.
 *
 * Tests verify:
 * - Custom itemView replaces default content
 * - Custom titleView replaces default title (caller name)
 * - Custom subtitleView replaces default subtitle (call type)
 * - Custom leadingView displays custom content
 * - Custom trailingView displays custom content
 *
 * Architecture:
 *   [Mock Call] → [Real CometChatIncomingCall Composable with custom view lambdas]
 *       → [Compose Test assertions]
 *
 * Requirements: 7.18
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.incomingcall.ui.CometChatIncomingCallCustomViewTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatIncomingCallCustomViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

    private fun createViewModel(): CometChatIncomingCallViewModel {
        return CometChatIncomingCallViewModel(enableListeners = false)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Custom itemView replaces default content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customItemView_replacesDefaultContent() {
        val caller = createMockUser(name = "Hidden Caller")
        val call = createMockCall(caller = caller)
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = call,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    itemView = { _ ->
                        Text("Custom Item Content")
                    },
                    onRejectClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: Custom item view is displayed
        composeTestRule.onNodeWithText("Custom Item Content").assertIsDisplayed()

        // Assert: Default caller name is NOT displayed (replaced by custom item view)
        composeTestRule.onNodeWithText("Hidden Caller").assertDoesNotExist()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Custom titleView replaces default title
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customTitleView_replacesDefaultTitle() {
        val caller = createMockUser(name = "Original Name")
        val call = createMockCall(caller = caller)
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = call,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    titleView = { _ ->
                        Text("Custom Title")
                    },
                    onRejectClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: Custom title view is displayed
        composeTestRule.onNodeWithText("Custom Title").assertIsDisplayed()

        // Assert: Default caller name is NOT displayed
        composeTestRule.onNodeWithText("Original Name").assertDoesNotExist()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Custom subtitleView replaces default subtitle
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customSubtitleView_replacesDefaultSubtitle() {
        val call = createMockCall(callType = CometChatConstants.CALL_TYPE_AUDIO)
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = call,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    subtitleView = { _ ->
                        Text("Custom Subtitle")
                    },
                    onRejectClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: Custom subtitle view is displayed
        composeTestRule.onNodeWithText("Custom Subtitle").assertIsDisplayed()

        // Assert: Default call type text is NOT displayed
        composeTestRule.onNodeWithText("Incoming audio Call", substring = true)
            .assertDoesNotExist()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Custom leadingView displays custom content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customLeadingView_displaysCustomContent() {
        val call = createMockCall()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = call,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    leadingView = { _ ->
                        Text("Custom Leading")
                    },
                    onRejectClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: Custom leading view is displayed
        composeTestRule.onNodeWithText("Custom Leading").assertIsDisplayed()

        // Assert: Default content (caller name) is still displayed
        // because leadingView doesn't replace the title/subtitle
        composeTestRule.onNodeWithText("Default Caller").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Custom trailingView displays custom content
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun customTrailingView_displaysCustomContent() {
        val call = createMockCall()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = call,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    trailingView = { _ ->
                        Text("Custom Trailing")
                    },
                    onRejectClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: Custom trailing view is displayed
        composeTestRule.onNodeWithText("Custom Trailing").assertIsDisplayed()

        // Assert: Default content (caller name) is still displayed
        // because trailingView only replaces the avatar area
        composeTestRule.onNodeWithText("Default Caller").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Multiple custom views can coexist
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleCustomViews_canCoexist() {
        val call = createMockCall()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = call,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    leadingView = { _ ->
                        Text("Leading Custom")
                    },
                    titleView = { _ ->
                        Text("Title Custom")
                    },
                    subtitleView = { _ ->
                        Text("Subtitle Custom")
                    },
                    trailingView = { _ ->
                        Text("Trailing Custom")
                    },
                    onRejectClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: All custom views exist and are rendered
        // Use useUnmergedTree to find individual text nodes that may be merged
        // into parent semantics in the merged tree
        composeTestRule.onNodeWithText("Leading Custom", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Title Custom", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Subtitle Custom", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Trailing Custom", useUnmergedTree = true).assertExists()

        // Assert: Default caller name is NOT displayed (replaced by custom title)
        composeTestRule.onNodeWithText("Default Caller", useUnmergedTree = true).assertDoesNotExist()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: itemView takes precedence over individual custom views
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun itemView_takesPrecedence_overIndividualCustomViews() {
        val call = createMockCall()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = call,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    disableSoundForCalls = true,
                    itemView = { _ ->
                        Text("Full Custom Item")
                    },
                    // These should be ignored when itemView is set
                    titleView = { _ ->
                        Text("Should Not Appear Title")
                    },
                    subtitleView = { _ ->
                        Text("Should Not Appear Subtitle")
                    },
                    onRejectClick = { /* no-op */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert: itemView content is displayed
        composeTestRule.onNodeWithText("Full Custom Item").assertIsDisplayed()

        // Assert: Individual custom views are NOT displayed
        composeTestRule.onNodeWithText("Should Not Appear Title").assertDoesNotExist()
        composeTestRule.onNodeWithText("Should Not Appear Subtitle").assertDoesNotExist()
    }
}
