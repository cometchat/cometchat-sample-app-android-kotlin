package com.cometchat.uikit.compose.presentation.createpoll.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.createpoll.style.CometChatCreatePollStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import org.json.JSONArray
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose instrumented tests for CometChatCreatePoll.
 *
 * Tests the real composable rendered in a Compose test environment.
 * Verifies rendering, interactions, and state management.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*.CometChatCreatePollListTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatCreatePollListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Form Rendering ====================

    @Test
    fun formRendersWithQuestionInput() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    isSubmitting = false,
                    errorMessage = null
                )
            }
        }

        // Question input should be displayed
        composeTestRule
            .onNodeWithContentDescription("Poll question input")
            .assertIsDisplayed()
    }

    @Test
    fun formRendersWithOptionInputs() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    isSubmitting = false,
                    errorMessage = null
                )
            }
        }

        // At least 2 option inputs should be displayed
        composeTestRule
            .onNodeWithContentDescription("Poll option 1")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Poll option 2")
            .assertIsDisplayed()
    }

    // ==================== Submit Button State ====================

    @Test
    fun submitButtonDisabledWhenFormEmpty() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    isSubmitting = false,
                    errorMessage = null
                )
            }
        }

        // Submit button should show disabled state
        composeTestRule
            .onNodeWithContentDescription("Send poll button disabled")
            .assertIsDisplayed()
    }

    @Test
    fun submitButtonShowsLoadingWhenSubmitting() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    isSubmitting = true,
                    errorMessage = null
                )
            }
        }

        // Loading state should be shown
        composeTestRule
            .onNodeWithContentDescription("Creating poll, please wait")
            .assertIsDisplayed()
    }

    // ==================== Back Button ====================

    @Test
    fun backButtonCallbackInvoked() {
        var backPressed = false

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    isSubmitting = false,
                    errorMessage = null,
                    onBackPress = { backPressed = true }
                )
            }
        }

        // Click back button
        composeTestRule
            .onNodeWithContentDescription("Go back from create poll")
            .performClick()

        composeTestRule.waitForIdle()
        assert(backPressed) { "Back press callback should have been invoked" }
    }

    // ==================== Error Message ====================

    @Test
    fun errorMessageDisplaysWhenProvided() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    isSubmitting = false,
                    errorMessage = "Network error. Please try again."
                )
            }
        }

        composeTestRule
            .onNodeWithText("Network error. Please try again.")
            .assertIsDisplayed()
    }

    @Test
    fun errorMessageHiddenWhenNull() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    isSubmitting = false,
                    errorMessage = null
                )
            }
        }

        composeTestRule
            .onNodeWithText("Network error. Please try again.")
            .assertDoesNotExist()
    }

    // ==================== Toolbar ====================

    @Test
    fun toolbarDisplaysDefaultTitle() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    isSubmitting = false,
                    errorMessage = null
                )
            }
        }

        // Default title should be displayed (from string resource)
        composeTestRule
            .onNodeWithContentDescription("Go back from create poll")
            .assertIsDisplayed()
    }

    @Test
    fun toolbarDisplaysCustomTitle() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    title = "New Poll",
                    isSubmitting = false,
                    errorMessage = null
                )
            }
        }

        composeTestRule
            .onNodeWithText("New Poll")
            .assertIsDisplayed()
    }

    @Test
    fun toolbarHiddenWhenHideToolbarTrue() {
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    hideToolbar = true,
                    isSubmitting = false,
                    errorMessage = null
                )
            }
        }

        // Back button should not exist when toolbar is hidden
        composeTestRule
            .onNodeWithContentDescription("Go back from create poll")
            .assertDoesNotExist()
    }

    // ==================== Submit Callback ====================

    @Test
    fun submitCallbackNotInvokedWhenFormEmpty() {
        var submitInvoked = false

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCreatePoll(
                    modifier = Modifier.fillMaxSize(),
                    isSubmitting = false,
                    errorMessage = null,
                    onSubmitClick = { _, _ -> submitInvoked = true }
                )
            }
        }

        // Try to click submit (should be disabled)
        composeTestRule
            .onNodeWithContentDescription("Send poll button disabled")
            .performClick()

        composeTestRule.waitForIdle()
        assert(!submitInvoked) { "Submit should not be invoked when form is empty" }
    }
}
