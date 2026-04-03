package com.cometchat.uikit.compose.components.shared.baseelements

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.theme.CometChatTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CometChatAvatarInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysInitialsWhenNoAvatarUrlProvided() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(name = "John Doe")
            }
        }
        composeTestRule.onNodeWithText("JD").assertIsDisplayed()
    }

    @Test
    fun displaysImageWhenAvatarUrlIsValid() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(
                    name = "John Doe",
                    avatarUrl = "https://i0.wp.com/www.thewrap.com/wp-content/uploads/2022/06/Avatar-The-Last-Airbender.jpg?fit=1200%2C675&quality=89&ssl=1"
                )
            }
        }

        // Wait for async image loading to complete
        composeTestRule.waitForIdle()
        Thread.sleep(2000) // Wait for image to load successfully
        
        // When image URL loads successfully, initials should NOT be displayed
        // The image should be shown instead
        composeTestRule.onNodeWithText("JD").assertIsNotDisplayed()
    }

    @Test
    fun displaysInitialsWhenAvatarUrlFails() {
        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(
                    name = "John Doe",
                    avatarUrl = "https://invalid-url-that-does-not-exist-xyz123.com/avatar.jpg"
                )
            }
        }

        // Wait for async image loading to fail and fallback to initials
        composeTestRule.waitForIdle()
        Thread.sleep(1500) // Wait for image loading to timeout/fail
        
        // When image URL fails to load, initials should be displayed as fallback
        composeTestRule.onNodeWithText("JD").assertIsDisplayed()
    }

    @Test
    fun fallsBackToInitialsWhenImageFailsToLoad() {
        // Test with an empty/invalid URL to trigger fallback immediately
        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(
                    name = "Jane Smith",
                    avatarUrl = "" // Empty URL forces initials to show
                )
            }
        }

        composeTestRule.waitForIdle()

        // Fallback to initials when image URL is empty or invalid
        // For multi-word names, initials are first letter of each word: "JS"
        composeTestRule.onNodeWithText("JS").assertIsDisplayed()
    }

    @Test
    fun updatesDisplayedInitialsWhenNameChanges() {
        val nameState = mutableStateOf("John Doe")

        composeTestRule.setContent {
            CometChatTheme {
                CometChatAvatar(name = nameState.value)
            }
        }

        composeTestRule.onNodeWithText("JD").assertIsDisplayed()

        composeTestRule.runOnIdle {
            nameState.value = "Alice"
        }

        composeTestRule.waitForIdle()

        // For single-word names, initials are first two characters: "AL"
        composeTestRule.onNodeWithText("AL").assertIsDisplayed()
        composeTestRule.onNodeWithText("JD").assertIsNotDisplayed()
    }
}
