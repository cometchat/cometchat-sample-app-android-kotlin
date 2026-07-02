package com.cometchat.uikit.compose.presentation.shared.messagebubble.cardbubble

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.shared.messagebubble.ui.CometChatCardBubble
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.MockFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Compose UI tests for CometChatCardBubble composable.
 *
 * Runs on a real device/emulator using Compose Test APIs to verify:
 * - Fallback text is displayed when card payload is empty
 * - Card composable renders without crash with valid JSON
 * - Theme integration works correctly
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*CometChatCardBubbleComposeUITest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatCardBubbleComposeUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Fallback text rendering ====================

    @Test
    fun cardBubble_withNullCard_displaysFallbackText() {
        val message = MockFactory.createCardMessage(
            id = 1L,
            cardJson = null,
            text = "Preview: Check out this product!",
            fallbackText = "Specific fallback text"
        )

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCardBubble(
                    message = message,
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Fallback text should be displayed (fallbackText takes priority)
        composeTestRule.onNodeWithText("Specific fallback text").assertIsDisplayed()
    }

    @Test
    fun cardBubble_withNullCardAndNullFallback_displaysTextPreview() {
        val message = MockFactory.createCardMessage(
            id = 2L,
            cardJson = null,
            text = "Card preview text",
            fallbackText = null
        )

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCardBubble(
                    message = message,
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composeTestRule.onNodeWithText("Card preview text").assertIsDisplayed()
    }

    @Test
    fun cardBubble_withEmptyEverything_displaysDefaultCardMessage() {
        val message = MockFactory.createEmptyCardMessage(
            id = 3L,
            text = null,
            fallbackText = null
        )

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCardBubble(
                    message = message,
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Should display the "Card Message" string resource
        composeTestRule.onNodeWithText("Card Message").assertIsDisplayed()
    }

    // ==================== Valid card rendering (no crash) ====================

    @Test
    fun cardBubble_withValidCardJson_rendersWithoutCrash() {
        val message = MockFactory.createCardMessage(
            id = 4L,
            cardJson = mapOf(
                "version" to "1.0",
                "body" to listOf(
                    mapOf("type" to "text", "content" to "Hello World")
                )
            )
        )

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCardBubble(
                    message = message,
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Card should render without crash — CometChatCardComposable handles the JSON
        composeTestRule.waitForIdle()
        // No assertion on specific card content since the renderer is a library component
    }


    // ==================== onCardAction callback ====================

    @Test
    fun cardBubble_onCardAction_canBeProvided() {
        var callbackReceived = false
        val message = MockFactory.createCardMessage(id = 7L, cardJson = null, text = "Action test")

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCardBubble(
                    message = message,
                    alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                    modifier = Modifier.fillMaxSize(),
                    onCardAction = { _, _ -> callbackReceived = true }
                )
            }
        }

        // Callback is wired — actual triggering depends on renderer interaction
        composeTestRule.waitForIdle()
        // Verify composable renders without crash even with callback provided
        composeTestRule.onNodeWithText("Action test").assertIsDisplayed()
    }
}
