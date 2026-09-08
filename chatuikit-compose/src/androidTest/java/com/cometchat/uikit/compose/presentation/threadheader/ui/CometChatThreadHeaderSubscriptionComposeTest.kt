package com.cometchat.uikit.compose.presentation.threadheader.ui

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.messagelist.ui.MessageListComposeTestHelper
import com.cometchat.uikit.compose.presentation.threadheader.viewmodel.ThreadHeaderViewModel
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.UIKitSettings
import com.cometchat.uikit.core.testutils.MockFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever

/**
 * Compose instrumented tests for the thread-subscription bell on CometChatThreadHeader (P3, ENG-37569).
 *
 * The bell (an un-subscribed thread renders bell-off → accessible name "Subscribe to thread") shows only
 * when the `enableThreadSubscription` gate is on, a parent message is present, and the control is not
 * hidden. The gate is set by initializing [CometChatUIKit] before composition.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*CometChatThreadHeaderSubscriptionComposeTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatThreadHeaderSubscriptionComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Accessible name of the bell when the thread is not subscribed (bell-off). Resolved from the
    // same string resource the bell renders, so the test tracks copy changes.
    private val unmuteDescription: String
        get() = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.cometchat_thread_unmute)

    @Before
    fun setup() {
        MessageListComposeTestHelper.ensureInitialized()
    }

    private fun setThreadSubscriptionGate(enabled: Boolean) {
        val settings = UIKitSettings.UIKitSettingsBuilder()
            .setAppId("278059f315a564b4")
            .setRegion("in")
            .setAuthKey("5bb2416b7eb003c1f94234c26178a4b053c66b97")
            .setEnableThreadSubscription(enabled)
            .build()
        CometChatUIKit.init(ApplicationProvider.getApplicationContext(), settings, null)
    }

    /**
     * The default fixture roots the thread in a group. [oneToOneParentMessage] covers the 1-1 case,
     * where the bell is offered too (ENG-38903) — the control follows threading's own scope.
     */
    private fun parentMessage(id: Long = 200L): BaseMessage {
        val message = MockFactory.createTextMessage(
            id = id,
            senderUid = "user-1",
            text = "Parent message",
            receiverId = "group-1",
            receiverType = CometChatConstants.RECEIVER_TYPE_GROUP,
            sentAt = 1735689600L
        )
        whenever(message.replyCount).thenReturn(3)
        return message
    }

    private fun oneToOneParentMessage(id: Long = 200L): BaseMessage {
        val message = MockFactory.createTextMessage(
            id = id,
            senderUid = "user-1",
            text = "Parent message",
            sentAt = 1735689600L
        )
        whenever(message.replyCount).thenReturn(3)
        return message
    }

    @Test
    fun bellIsShownWhenGateOn() {
        setThreadSubscriptionGate(true)
        val message = parentMessage()
        val viewModel = ThreadHeaderViewModel(enableListeners = false).apply { setParentMessage(message) }

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = message,
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(unmuteDescription).assertIsDisplayed()
    }

    @Test
    fun bellIsHiddenWhenGateOff() {
        setThreadSubscriptionGate(false)
        val message = parentMessage()
        val viewModel = ThreadHeaderViewModel(enableListeners = false).apply { setParentMessage(message) }

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = message,
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(unmuteDescription).assertDoesNotExist()
    }

    @Test
    fun bellIsHiddenWhenHideFlagSet() {
        setThreadSubscriptionGate(true)
        val message = parentMessage()
        val viewModel = ThreadHeaderViewModel(enableListeners = false).apply { setParentMessage(message) }

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = message,
                    viewModel = viewModel,
                    hideThreadSubscription = true
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(unmuteDescription).assertDoesNotExist()
    }
}
