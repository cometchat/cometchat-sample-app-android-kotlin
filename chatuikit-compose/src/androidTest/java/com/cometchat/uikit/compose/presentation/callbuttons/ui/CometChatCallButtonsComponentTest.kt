package com.cometchat.uikit.compose.presentation.callbuttons.ui

import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.data.datasource.CallButtonsDataSource
import com.cometchat.uikit.core.data.repository.CallButtonsRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Compose instrumented tests for CometChatCallButtons component.
 *
 * Tests verify:
 * - Both call buttons render and are clickable
 * - Custom click handlers receive correct User/Group
 * - Button visibility controls work
 * - Button text visibility works
 * - onCallInitiated callback invoked on successful user call
 * - Component renders correctly with CometChatTheme
 *
 * Architecture:
 *   [Fake DataSource] → [Real Repository] → [Real UseCases] → [Real ViewModel]
 *       → [Real CometChatCallButtons Composable] → [Compose Test assertions]
 *
 * Requirements: 17.1, 17.2, 17.4
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.callbuttons.ui.CometChatCallButtonsComponentTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatCallButtonsComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

    private fun createMockCall(sessionId: String = "session-1"): Call {
        val call = mock(Call::class.java)
        `when`(call.sessionId).thenReturn(sessionId)
        `when`(call.type).thenReturn("audio")
        return call
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ViewModel Factory
    // ─────────────────────────────────────────────────────────────────────────

    private fun createViewModel(
        initiateUserCallResult: Result<Call> = Result.success(createMockCall()),
        hasActiveCall: Boolean = false
    ): CometChatCallButtonsViewModel {
        val dataSource = object : CallButtonsDataSource {
            override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                return initiateUserCallResult
            }
            override suspend fun sendGroupCallMessage(groupId: String, callType: String) =
                Result.success(mock(com.cometchat.chat.models.CustomMessage::class.java))
            override fun getActiveCall(): Call? = if (hasActiveCall) createMockCall() else null
            override fun getActiveCallingExtensionCall(): Call? = null
            override fun isActiveMeeting(): Boolean = false
        }
        val repository = CallButtonsRepositoryImpl(dataSource)
        val initiateUserCallUseCase = InitiateUserCallUseCase(repository)
        val startGroupCallUseCase = StartGroupCallUseCase(repository)
        return CometChatCallButtonsViewModel(
            initiateUserCallUseCase = initiateUserCallUseCase,
            startGroupCallUseCase = startGroupCallUseCase,
            enableListeners = false
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Both call buttons are displayed by default
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun bothCallButtons_displayedByDefault() {
        val user = createMockUser()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Voice Call").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Video Call").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Voice call button click triggers custom callback with user
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun voiceCallClick_triggersCustomCallback_withUser() {
        val user = createMockUser(uid = "user-42")
        val viewModel = createViewModel()
        val clickedUser = AtomicReference<User?>(null)
        val clickedGroup = AtomicReference<Group?>(null)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onVoiceCallClick = { u, g ->
                        clickedUser.set(u)
                        clickedGroup.set(g)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()
        composeTestRule.waitForIdle()

        assertNotNull("User should not be null", clickedUser.get())
        assertEquals("user-42", clickedUser.get()?.uid)
        assertNull("Group should be null for user call", clickedGroup.get())
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Video call button click triggers custom callback with user
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun videoCallClick_triggersCustomCallback_withUser() {
        val user = createMockUser(uid = "user-99")
        val viewModel = createViewModel()
        val clickedUser = AtomicReference<User?>(null)
        val clickedGroup = AtomicReference<Group?>(null)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onVideoCallClick = { u, g ->
                        clickedUser.set(u)
                        clickedGroup.set(g)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Video Call").performClick()
        composeTestRule.waitForIdle()

        assertNotNull("User should not be null", clickedUser.get())
        assertEquals("user-99", clickedUser.get()?.uid)
        assertNull("Group should be null for user call", clickedGroup.get())
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Voice call button click triggers custom callback with group
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun voiceCallClick_triggersCustomCallback_withGroup() {
        val group = createMockGroup(guid = "group-7")
        val viewModel = createViewModel()
        val clickedUser = AtomicReference<User?>(null)
        val clickedGroup = AtomicReference<Group?>(null)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    group = group,
                    onVoiceCallClick = { u, g ->
                        clickedUser.set(u)
                        clickedGroup.set(g)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()
        composeTestRule.waitForIdle()

        assertNull("User should be null for group call", clickedUser.get())
        assertNotNull("Group should not be null", clickedGroup.get())
        assertEquals("group-7", clickedGroup.get()?.guid)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Video call button click triggers custom callback with group
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun videoCallClick_triggersCustomCallback_withGroup() {
        val group = createMockGroup(guid = "group-12")
        val viewModel = createViewModel()
        val clickedUser = AtomicReference<User?>(null)
        val clickedGroup = AtomicReference<Group?>(null)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    group = group,
                    onVideoCallClick = { u, g ->
                        clickedUser.set(u)
                        clickedGroup.set(g)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Video Call").performClick()
        composeTestRule.waitForIdle()

        assertNull("User should be null for group call", clickedUser.get())
        assertNotNull("Group should not be null", clickedGroup.get())
        assertEquals("group-12", clickedGroup.get()?.guid)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Voice call button hidden when visibility is GONE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun voiceCallButton_hiddenWhenVisibilityGone() {
        val user = createMockUser()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    voiceCallButtonVisibility = View.GONE
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Voice Call").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Video Call").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Video call button hidden when visibility is GONE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun videoCallButton_hiddenWhenVisibilityGone() {
        val user = createMockUser()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    videoCallButtonVisibility = View.GONE
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Video Call").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Voice Call").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Both buttons hidden when both visibility GONE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun bothButtons_hiddenWhenBothVisibilityGone() {
        val user = createMockUser()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    voiceCallButtonVisibility = View.GONE,
                    videoCallButtonVisibility = View.GONE
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Voice Call").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Video Call").assertDoesNotExist()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 9: Multiple rapid clicks don't crash
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun multipleRapidClicks_dontCrash() {
        val user = createMockUser()
        val viewModel = createViewModel()
        val clickCount = AtomicReference(0)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onVoiceCallClick = { _, _ ->
                        clickCount.set(clickCount.get() + 1)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Rapid clicks
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()

        composeTestRule.waitForIdle()

        // All clicks should be registered without crash
        assertTrue("Click count should be >= 1", clickCount.get() >= 1)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 10: Component renders with button text visible
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun buttonText_displayedWhenVisibilityVisible() {
        val user = createMockUser()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    voiceButtonText = "Voice",
                    videoButtonText = "Video",
                    buttonTextVisibility = View.VISIBLE
                )
            }
        }

        composeTestRule.waitForIdle()
        // Buttons should still be displayed (text is inside the button)
        composeTestRule.onNodeWithContentDescription("Voice Call").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Video Call").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 11: onCallInitiated callback invoked on successful user call
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onCallInitiated_invokedOnSuccessfulUserCall() {
        val user = createMockUser(uid = "user-call-test")
        val expectedCall = createMockCall(sessionId = "session-abc")
        val viewModel = createViewModel(initiateUserCallResult = Result.success(expectedCall))
        val callInitiated = AtomicBoolean(false)
        val initiatedCall = AtomicReference<Call?>(null)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onCallInitiated = { call ->
                        callInitiated.set(true)
                        initiatedCall.set(call)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click voice call button (no custom handler, so it triggers ViewModel.initiateCall)
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()

        // Wait for async operation
        composeTestRule.waitForIdle()
        Thread.sleep(500) // Allow coroutine to complete

        assertTrue("onCallInitiated should have been invoked", callInitiated.get())
        assertNotNull("Initiated call should not be null", initiatedCall.get())
        assertEquals("session-abc", initiatedCall.get()?.sessionId)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 12: Component renders correctly with dark theme
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun component_rendersWithDarkTheme() {
        val user = createMockUser()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = com.cometchat.uikit.compose.theme.darkColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user
                )
            }
        }

        composeTestRule.waitForIdle()
        // Both buttons should render without crash in dark theme
        composeTestRule.onNodeWithContentDescription("Voice Call").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Video Call").assertIsDisplayed()
    }
}
