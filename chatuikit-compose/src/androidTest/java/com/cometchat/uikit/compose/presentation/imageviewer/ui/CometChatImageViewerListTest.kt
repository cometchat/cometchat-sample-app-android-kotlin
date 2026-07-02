package com.cometchat.uikit.compose.presentation.imageviewer.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.imageviewer.style.CometChatImageViewerStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose instrumented tests for CometChatImageViewerScreen.
 *
 * Tests the composable rendering and interaction using Compose UI Test APIs:
 * - Screen renders with toolbar (back + share buttons)
 * - Back button invokes onBack callback
 * - Share button invokes onShare callback with correct parameters
 * - Loading indicator shown initially
 *
 * Architecture:
 *   [CometChatImageViewerScreen composable] → [Compose Test Rule assertions]
 *
 * Since CometChatImageViewerScreen is stateless (no ViewModel), we test through
 * the composable's public API: parameters → rendered UI → callback invocations.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.uikit.compose.presentation.imageviewer.ui.CometChatImageViewerListTest
 */
@RunWith(AndroidJUnit4::class)
class CometChatImageViewerListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 1: Screen renders with toolbar containing back button
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun screen_rendersBackButton() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 2: Screen renders with toolbar containing share button
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun screen_rendersShareButton() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Share")
            .assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 3: Back button click invokes onBack callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun screen_backButtonClick_invokesOnBack() {
        var onBackInvoked = false

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = { onBackInvoked = true },
                    onShare = { _, _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        composeTestRule.waitForIdle()

        assertTrue("onBack callback was not invoked", onBackInvoked)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 4: Share button click invokes onShare callback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun screen_shareButtonClick_invokesOnShare() {
        var onShareInvoked = false
        var receivedUrl = ""
        var receivedFileName = ""
        var receivedMimeType = ""

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { url, name, mime ->
                        onShareInvoked = true
                        receivedUrl = url
                        receivedFileName = name
                        receivedMimeType = mime
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Share")
            .performClick()

        composeTestRule.waitForIdle()

        assertTrue("onShare callback was not invoked", onShareInvoked)
        assertTrue("Received URL should match", receivedUrl == "https://example.com/photo.jpg")
        assertTrue("Received fileName should match", receivedFileName == "photo.jpg")
        assertTrue("Received mimeType should match", receivedMimeType == "image/jpeg")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 5: Screen renders with custom style
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun screen_rendersWithCustomStyle() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    style = CometChatImageViewerStyle(
                        backgroundColor = androidx.compose.ui.graphics.Color.DarkGray,
                        toolbarBackgroundColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
                        iconTintColor = androidx.compose.ui.graphics.Color.Yellow,
                        loadingIndicatorColor = androidx.compose.ui.graphics.Color.Red
                    ),
                    onBack = {},
                    onShare = { _, _, _ -> }
                )
            }
        }

        // Toolbar buttons should still be visible with custom style
        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share")
            .assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 6: Share button passes correct parameters for different inputs
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun screen_shareButtonPassesCorrectParameters_differentInput() {
        var receivedUrl = ""
        var receivedFileName = ""
        var receivedMimeType = ""

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://cdn.example.com/uploads/document.pdf",
                    fileName = "document.pdf",
                    mimeType = "application/pdf",
                    onBack = {},
                    onShare = { url, name, mime ->
                        receivedUrl = url
                        receivedFileName = name
                        receivedMimeType = mime
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Share")
            .performClick()

        composeTestRule.waitForIdle()

        assertTrue(
            "URL should be passed correctly",
            receivedUrl == "https://cdn.example.com/uploads/document.pdf"
        )
        assertTrue(
            "fileName should be passed correctly",
            receivedFileName == "document.pdf"
        )
        assertTrue(
            "mimeType should be passed correctly",
            receivedMimeType == "application/pdf"
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 7: Back button does not trigger share
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun screen_backButtonClick_doesNotTriggerShare() {
        var onShareInvoked = false

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = {},
                    onShare = { _, _, _ -> onShareInvoked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        composeTestRule.waitForIdle()

        assertTrue("onShare should NOT be invoked on back click", !onShareInvoked)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST 8: Share button does not trigger back
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun screen_shareButtonClick_doesNotTriggerBack() {
        var onBackInvoked = false

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatImageViewerScreen(
                    imageUrl = "https://example.com/photo.jpg",
                    fileName = "photo.jpg",
                    mimeType = "image/jpeg",
                    onBack = { onBackInvoked = true },
                    onShare = { _, _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Share")
            .performClick()

        composeTestRule.waitForIdle()

        assertTrue("onBack should NOT be invoked on share click", !onBackInvoked)
    }
}
