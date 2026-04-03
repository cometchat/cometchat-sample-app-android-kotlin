package com.cometchat.uikit.compose.shared.defaultstates

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorStateStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

/**
 * Instrumented UI tests for CometChatErrorState.
 *
 * Feature: shared-default-states
 * Property 3: Error state retry callback invocation
 * Validates: Requirements 2.4
 */
@RunWith(AndroidJUnit4::class)
class CometChatErrorStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Property 3: Error state retry callback invocation
     *
     * For any CometChatErrorState instance with a given onRetry callback,
     * clicking the retry button SHALL invoke the onRetry callback exactly once per click.
     *
     * Feature: shared-default-states, Property 3: Error state retry callback invocation
     * Validates: Requirements 2.4
     */
    @Test
    fun errorState_retryButtonInvokesCallbackOnce() {
        // Arrange
        val retryCount = AtomicInteger(0)

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatErrorState(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = { retryCount.incrementAndGet() }
                )
            }
        }

        // Click retry button
        composeTestRule.onNodeWithText("Retry").performClick()

        // Assert - callback should be invoked exactly once
        assert(retryCount.get() == 1) { "onRetry callback should be invoked exactly once, but was invoked ${retryCount.get()} times" }
    }

    @Test
    fun errorState_multipleClicksInvokeCallbackMultipleTimes() {
        // Arrange
        val retryCount = AtomicInteger(0)

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatErrorState(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = { retryCount.incrementAndGet() }
                )
            }
        }

        // Click retry button 3 times
        repeat(3) {
            composeTestRule.onNodeWithText("Retry").performClick()
        }

        // Assert - callback should be invoked 3 times
        assert(retryCount.get() == 3) { "onRetry callback should be invoked 3 times, but was invoked ${retryCount.get()} times" }
    }

    @Test
    fun errorState_displaysTitle() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatErrorState(
                    modifier = Modifier.fillMaxSize(),
                    title = "Custom Error Title",
                    onRetry = { }
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Custom Error Title").assertIsDisplayed()
    }

    @Test
    fun errorState_displaysSubtitle() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatErrorState(
                    modifier = Modifier.fillMaxSize(),
                    subtitle = "Custom error message",
                    onRetry = { }
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Custom error message").assertIsDisplayed()
    }

    @Test
    fun errorState_displaysCustomRetryButtonText() {
        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatErrorState(
                    modifier = Modifier.fillMaxSize(),
                    style = CometChatErrorStateStyle.default(
                        retryButtonText = "Try Again"
                    ),
                    onRetry = { }
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
    }
}
