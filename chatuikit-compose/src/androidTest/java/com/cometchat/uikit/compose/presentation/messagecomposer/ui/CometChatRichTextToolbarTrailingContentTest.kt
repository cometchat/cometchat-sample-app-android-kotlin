package com.cometchat.uikit.compose.presentation.messagecomposer.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cometchat.uikit.compose.theme.CometChatTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Instrumented tests for the trailing custom-content slot on [CometChatRichTextToolbar].
 *
 * Verifies:
 * - Content passed via `trailingToolbarContent` is rendered.
 * - When the slot is absent, no trailing content is present (guard keeps the zero-content path
 *   identical to the built-in toolbar).
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest \
 *     --tests "com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatRichTextToolbarTrailingContentTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatRichTextToolbarTrailingContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun trailingContent_isRendered_whenProvided() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatRichTextToolbar(
                    trailingToolbarContent = { Text("TRAILING_MARKER") }
                )
            }
        }

        composeTestRule.onNodeWithText("TRAILING_MARKER").assertIsDisplayed()
    }

    @Test
    fun trailingContent_isAbsent_whenNotProvided() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatRichTextToolbar()
            }
        }

        composeTestRule.onNodeWithText("TRAILING_MARKER").assertDoesNotExist()
    }
}
