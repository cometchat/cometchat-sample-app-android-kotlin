package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.presentation.shared.messagebubble.InternalContentRenderer
import com.cometchat.uikit.compose.presentation.shared.messagebubble.style.CometChatMessageBubbleStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.core.constants.UIKitConstants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ENG-38585 — the timestamp a bubble actually renders.
 *
 * The bug this guards against is not "the formatter is wrong", it is "the formatter never
 * arrives". `MessageListItem` and `CometChatMessageBubble` both accepted a `timeFormat` and a
 * `dateTimeFormatter` that nothing ever set, so the parameters looked correct while every
 * bubble rendered the hardcoded `h:mm a`. Asserting the resolver in isolation would not have
 * caught that; these tests assert the string on screen.
 *
 * `DefaultStatusInfoView` is the composable the bubbles delegate their timestamp to, and it is
 * the point the wiring from `CometChatMessageList` has to reach.
 *
 * Layer 1 (Component). Runs on Robolectric in `src/test`, so it executes in the JVM unit-test
 * job rather than the instrumented suite.
 *
 * Run: ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*MessageBubbleTimestampRenderingTest"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MessageBubbleTimestampRenderingTest {

    @get:Rule
    val composeRule = createComposeRule()

    // An afternoon timestamp, so the 12-hour and 24-hour renderings cannot coincide.
    private val sentAtSeconds = 1_772_034_060L

    private fun formatted(pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(sentAtSeconds * 1000))

    private fun message() = TextMessage(
        "receiver-uid",
        "hello",
        CometChatConstants.RECEIVER_TYPE_USER
    ).apply { sentAt = sentAtSeconds }

    private fun renderStatusInfo(
        timeFormat: String? = null,
        dateTimeFormatter: ((Long) -> String)? = null
    ) {
        composeRule.setContent {
            CometChatTheme {
                InternalContentRenderer.DefaultStatusInfoView(
                    message = message(),
                    alignment = UIKitConstants.MessageBubbleAlignment.RIGHT,
                    style = CometChatMessageBubbleStyle.default(),
                    showReceipt = false,
                    timeFormat = timeFormat,
                    dateTimeFormatter = dateTimeFormatter
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `an explicit time pattern reaches the rendered timestamp`() {
        renderStatusInfo(timeFormat = "HH:mm")

        composeRule.onNodeWithText(formatted("HH:mm")).assertIsDisplayed()
        composeRule.onNodeWithText(formatted("h:mm a")).assertDoesNotExist()
    }

    @Test
    fun `a dateTimeFormatter reaches the rendered timestamp`() {
        renderStatusInfo(dateTimeFormatter = { "sent recently" })

        composeRule.onNodeWithText("sent recently").assertIsDisplayed()
    }

    @Test
    fun `the formatter wins over an explicit pattern`() {
        renderStatusInfo(timeFormat = "HH:mm", dateTimeFormatter = { "sent recently" })

        composeRule.onNodeWithText("sent recently").assertIsDisplayed()
        composeRule.onNodeWithText(formatted("HH:mm")).assertDoesNotExist()
    }

    @Test
    fun `with nothing supplied a 24-hour device gets 24-hour time`() {
        ShadowSettings.set24HourTimeFormat(true)

        renderStatusInfo()

        composeRule.onNodeWithText(formatted("HH:mm")).assertIsDisplayed()
    }

    @Test
    fun `with nothing supplied a 12-hour device still gets AM PM`() {
        ShadowSettings.set24HourTimeFormat(false)

        renderStatusInfo()

        composeRule.onNodeWithText(formatted("h:mm a")).assertIsDisplayed()
    }
}
