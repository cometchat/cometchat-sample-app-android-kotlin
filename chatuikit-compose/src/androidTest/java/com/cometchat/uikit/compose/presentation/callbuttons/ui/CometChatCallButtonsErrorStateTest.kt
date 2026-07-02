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
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.CustomMessage
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Compose instrumented tests for CometChatCallButtons error states.
 *
 * Tests verify:
 * - onError callback invoked when call initiation fails (active call exists)
 * - onError callback receives correct CometChatException with error code
 * - Buttons remain functional after error
 * - Error on group call invokes callback
 * - Component doesn't crash on error
 *
 * Architecture:
 *   [Fake DataSource with error] → [Real Repository] → [Real UseCases] → [Real ViewModel]
 *       → [Real CometChatCallButtons Composable] → [Compose Test assertions on callbacks]
 *
 * Requirements: 17.1, 17.2, 17.4
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.callbuttons.ui.CometChatCallButtonsErrorStateTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatCallButtonsErrorStateTest {

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
    // ViewModel Factories
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a ViewModel with a DataSource that has an active call,
     * causing the UseCase to return an ACTIVE_CALL error.
     */
    private fun createViewModelWithActiveCall(): CometChatCallButtonsViewModel {
        val dataSource = object : CallButtonsDataSource {
            override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                return Result.success(createMockCall())
            }
            override suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage> {
                return Result.success(mock(CustomMessage::class.java))
            }
            override fun getActiveCall(): Call? = createMockCall() // Active call exists!
            override fun getActiveCallingExtensionCall(): Call? = null
            override fun isActiveMeeting(): Boolean = false
        }
        val repository = CallButtonsRepositoryImpl(dataSource)
        return CometChatCallButtonsViewModel(
            initiateUserCallUseCase = InitiateUserCallUseCase(repository),
            startGroupCallUseCase = StartGroupCallUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel with a DataSource that returns a failure result
     * when initiating a user call.
     */
    private fun createViewModelWithCallFailure(): CometChatCallButtonsViewModel {
        val dataSource = object : CallButtonsDataSource {
            override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                return Result.failure(CometChatException("CALL_FAILED", "Call initiation failed", "Network error"))
            }
            override suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage> {
                return Result.failure(CometChatException("GROUP_CALL_FAILED", "Group call failed", "Network error"))
            }
            override fun getActiveCall(): Call? = null
            override fun getActiveCallingExtensionCall(): Call? = null
            override fun isActiveMeeting(): Boolean = false
        }
        val repository = CallButtonsRepositoryImpl(dataSource)
        return CometChatCallButtonsViewModel(
            initiateUserCallUseCase = InitiateUserCallUseCase(repository),
            startGroupCallUseCase = StartGroupCallUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel with a DataSource that succeeds (no error).
     */
    private fun createViewModelWithSuccess(): CometChatCallButtonsViewModel {
        val dataSource = object : CallButtonsDataSource {
            override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                return Result.success(createMockCall())
            }
            override suspend fun sendGroupCallMessage(groupId: String, callType: String): Result<CustomMessage> {
                return Result.success(mock(CustomMessage::class.java))
            }
            override fun getActiveCall(): Call? = null
            override fun getActiveCallingExtensionCall(): Call? = null
            override fun isActiveMeeting(): Boolean = false
        }
        val repository = CallButtonsRepositoryImpl(dataSource)
        return CometChatCallButtonsViewModel(
            initiateUserCallUseCase = InitiateUserCallUseCase(repository),
            startGroupCallUseCase = StartGroupCallUseCase(repository),
            enableListeners = false
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: onError callback invoked when active call exists (user call)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onError_invokedWhenActiveCallExists_userCall() {
        val user = createMockUser()
        val viewModel = createViewModelWithActiveCall()
        val errorInvoked = AtomicBoolean(false)
        val errorRef = AtomicReference<CometChatException?>(null)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onError = { exception ->
                        errorInvoked.set(true)
                        errorRef.set(exception)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click voice call — should trigger ACTIVE_CALL error from UseCase
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()

        // Wait for async error propagation
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        assertTrue("onError should have been invoked", errorInvoked.get())
        assertNotNull("Exception should not be null", errorRef.get())
        assertEquals("ACTIVE_CALL", errorRef.get()?.code)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: onError callback invoked when active call exists (video call)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onError_invokedWhenActiveCallExists_videoCall() {
        val user = createMockUser()
        val viewModel = createViewModelWithActiveCall()
        val errorInvoked = AtomicBoolean(false)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onError = { _ ->
                        errorInvoked.set(true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click video call — should trigger ACTIVE_CALL error
        composeTestRule.onNodeWithContentDescription("Video Call").performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        assertTrue("onError should have been invoked for video call", errorInvoked.get())
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: onError callback invoked when call initiation fails
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onError_invokedWhenCallInitiationFails() {
        val user = createMockUser()
        val viewModel = createViewModelWithCallFailure()
        val errorInvoked = AtomicBoolean(false)
        val errorRef = AtomicReference<CometChatException?>(null)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onError = { exception ->
                        errorInvoked.set(true)
                        errorRef.set(exception)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click voice call — DataSource returns failure
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        assertTrue("onError should have been invoked", errorInvoked.get())
        assertNotNull("Exception should not be null", errorRef.get())
        assertEquals("CALL_FAILED", errorRef.get()?.code)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: onError callback invoked for group call failure
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun onError_invokedForGroupCallFailure() {
        val group = createMockGroup()
        val viewModel = createViewModelWithCallFailure()
        val errorInvoked = AtomicBoolean(false)
        val errorRef = AtomicReference<CometChatException?>(null)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    group = group,
                    onError = { exception ->
                        errorInvoked.set(true)
                        errorRef.set(exception)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click voice call with group — DataSource returns failure
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        assertTrue("onError should have been invoked for group call", errorInvoked.get())
        assertNotNull("Exception should not be null", errorRef.get())
        assertEquals("GROUP_CALL_FAILED", errorRef.get()?.code)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Buttons remain visible after error
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun buttonsRemainVisible_afterError() {
        val user = createMockUser()
        val viewModel = createViewModelWithActiveCall()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onError = { _ -> /* consume error */ }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Trigger error
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // Both buttons should still be visible
        composeTestRule.onNodeWithContentDescription("Voice Call").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Video Call").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Buttons remain clickable after error
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun buttonsRemainClickable_afterError() {
        val user = createMockUser()
        val viewModel = createViewModelWithActiveCall()
        val errorCount = AtomicReference(0)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onError = { _ ->
                        errorCount.set(errorCount.get() + 1)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // First click triggers error
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(300)

        // Second click should also work (button not disabled)
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(300)

        // Both errors should have been received
        assertTrue("Error count should be >= 2", errorCount.get() >= 2)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: No error when call succeeds
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun noError_whenCallSucceeds() {
        val user = createMockUser()
        val viewModel = createViewModelWithSuccess()
        val errorInvoked = AtomicBoolean(false)
        val callInitiated = AtomicBoolean(false)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onCallInitiated = { _ ->
                        callInitiated.set(true)
                    },
                    onError = { _ ->
                        errorInvoked.set(true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click voice call — should succeed
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // onError should NOT have been invoked
        assertTrue(
            "onError should NOT have been invoked on success",
            !errorInvoked.get()
        )
        // onCallInitiated should have been invoked
        assertTrue("onCallInitiated should have been invoked", callInitiated.get())
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Component doesn't crash when onError is null
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun component_doesNotCrash_whenOnErrorIsNull() {
        val user = createMockUser()
        val viewModel = createViewModelWithActiveCall()

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    viewModel = viewModel,
                    user = user,
                    onError = null // No error handler
                )
            }
        }

        composeTestRule.waitForIdle()

        // Click voice call — error occurs but no handler set
        // Should NOT crash
        composeTestRule.onNodeWithContentDescription("Voice Call").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // Component should still be functional
        composeTestRule.onNodeWithContentDescription("Voice Call").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Video Call").assertIsDisplayed()
    }
}
