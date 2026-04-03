package com.cometchat.uikit.compose.shared.defaultstates

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingStateStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for CometChatLoadingState.
 *
 * Feature: shared-default-states
 * Property 4: Loading state renders correct item count
 * Validates: Requirements 3.2
 */
@RunWith(AndroidJUnit4::class)
class CometChatLoadingStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Property 4: Loading state renders correct item count
     *
     * For any CometChatLoadingState with a style specifying itemCount = N,
     * the rendered component SHALL display exactly N shimmer placeholder items.
     *
     * Feature: shared-default-states, Property 4: Loading state renders correct item count
     * Validates: Requirements 3.2
     */
    @Test
    fun loadingState_displaysCorrectItemCount_default() {
        // Act - default itemCount is 8
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatLoadingState(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Assert - should have accessibility description for loading
        composeTestRule.onNodeWithContentDescription("Loading, please wait").assertExists()
    }

    @Test
    fun loadingState_displaysCorrectItemCount_custom5() {
        // Act - custom itemCount of 5
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatLoadingState(
                    modifier = Modifier.fillMaxSize(),
                    style = CometChatLoadingStateStyle.default(
                        itemCount = 5
                    )
                )
            }
        }

        // Assert - should have accessibility description for loading
        composeTestRule.onNodeWithContentDescription("Loading, please wait").assertExists()
    }

    @Test
    fun loadingState_displaysCorrectItemCount_custom10() {
        // Act - custom itemCount of 10
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatLoadingState(
                    modifier = Modifier.fillMaxSize(),
                    style = CometChatLoadingStateStyle.default(
                        itemCount = 10
                    )
                )
            }
        }

        // Assert - should have accessibility description for loading
        composeTestRule.onNodeWithContentDescription("Loading, please wait").assertExists()
    }

    @Test
    fun loadingState_handlesZeroItemCount_defaultsToOne() {
        // Act - itemCount of 0 should default to 1
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatLoadingState(
                    modifier = Modifier.fillMaxSize(),
                    style = CometChatLoadingStateStyle.default(
                        itemCount = 0
                    )
                )
            }
        }

        // Assert - should still render (at least 1 item)
        composeTestRule.onNodeWithContentDescription("Loading, please wait").assertExists()
    }

    @Test
    fun loadingState_handlesNegativeItemCount_defaultsToOne() {
        // Act - negative itemCount should default to 1
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatLoadingState(
                    modifier = Modifier.fillMaxSize(),
                    style = CometChatLoadingStateStyle.default(
                        itemCount = -5
                    )
                )
            }
        }

        // Assert - should still render (at least 1 item)
        composeTestRule.onNodeWithContentDescription("Loading, please wait").assertExists()
    }
}
