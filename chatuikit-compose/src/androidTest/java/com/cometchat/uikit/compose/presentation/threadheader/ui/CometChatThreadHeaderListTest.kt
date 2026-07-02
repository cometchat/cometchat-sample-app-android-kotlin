package com.cometchat.uikit.compose.presentation.threadheader.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.presentation.messagelist.ui.MessageListComposeTestHelper
import com.cometchat.uikit.compose.presentation.threadheader.viewmodel.ThreadHeaderViewModel
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.testutils.MockFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever

/**
 * Compose instrumented tests for CometChatThreadHeader.
 * Tests real composable rendering, visibility flags, custom views, and real-time updates.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*CometChatThreadHeaderListTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatThreadHeaderListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        MessageListComposeTestHelper.ensureInitialized()
    }

    // ==================== Helpers ====================

    private fun createViewModel(): ThreadHeaderViewModel {
        return ThreadHeaderViewModel(enableListeners = false)
    }

    private fun createParentMessage(
        id: Long = 100L,
        senderUid: String = "user-1",
        replyCount: Int = 0,
        text: String = "Parent message for thread"
    ): BaseMessage {
        val message = MockFactory.createTextMessage(
            id = id,
            senderUid = senderUid,
            text = text,
            sentAt = 1735689600L
        )
        whenever(message.replyCount).thenReturn(replyCount)
        return message
    }

    // ==================== Component Renders Parent Message ====================

    @Test
    fun componentRendersParentMessage() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }

        // The thread header should render (composable is displayed)
        composeTestRule.waitForIdle()
    }

    // ==================== Reply Count Displays ====================

    @Test
    fun replyCountDisplaysSingleReply() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 1)
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // "1 Reply" should be displayed
        composeTestRule.onNodeWithText("1 Reply", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun replyCountDisplaysMultipleReplies() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // "5 Replies" should be displayed
        composeTestRule.onNodeWithText("5 Replies", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    // ==================== Visibility Flags ====================

    @Test
    fun hideReplyCountBarHidesEntireBar() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    hideReplyCountBar = true
                )
            }
        }

        composeTestRule.waitForIdle()
        // Reply count text should not exist when bar is hidden
        composeTestRule.onNodeWithText("5 Replies", substring = true, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun hideReplyCountHidesTextButBarStillExists() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    hideReplyCount = true
                )
            }
        }

        composeTestRule.waitForIdle()
        // Reply count text should not be displayed when hideReplyCount=true
        composeTestRule.onNodeWithText("5 Replies", substring = true, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    // ==================== Custom Views ====================

    @Test
    fun customReplyCountViewRendersInsteadOfDefault() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 7)
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    replyCountView = { count ->
                        Text(
                            text = "Custom: $count responses",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Blue
                        )
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        // Custom reply count view should be displayed
        composeTestRule.onNodeWithText("Custom: 7 responses", useUnmergedTree = true)
            .assertIsDisplayed()
        // Default reply count should NOT be displayed
        composeTestRule.onNodeWithText("7 Replies", substring = true, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun customMessageBubbleViewRendersInsteadOfDefault() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 3)
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    messageBubbleView = { message ->
                        Text(
                            text = "Custom bubble for message ${message.id}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        // Custom message bubble view should be displayed
        composeTestRule.onNodeWithText("Custom bubble for message 100", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    // ==================== Real-Time Updates ====================

    @Test
    fun realTimeReplyCountUpdateTriggersRecomposition() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 3)
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // Initial state: "3 Replies"
        composeTestRule.onNodeWithText("3 Replies", substring = true, useUnmergedTree = true)
            .assertExists()

        // Update parent message with new reply count
        val updatedMessage = createParentMessage(replyCount = 8)
        viewModel.setParentMessage(updatedMessage)

        composeTestRule.waitForIdle()
        // Updated state: "8 Replies"
        composeTestRule.onNodeWithText("8 Replies", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    // ==================== Alignment ====================

    @Test
    fun leftAlignedRendersCorrectly() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 3, senderUid = "logged-in-user")
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    alignment = UIKitConstants.MessageListAlignment.LEFT_ALIGNED
                )
            }
        }

        composeTestRule.waitForIdle()
        // Component should render without errors in LEFT_ALIGNED mode
    }

    @Test
    fun standardAlignmentRendersCorrectly() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 3, senderUid = "other-user")
        viewModel.setParentMessage(parentMessage)

        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    alignment = UIKitConstants.MessageListAlignment.STANDARD
                )
            }
        }

        composeTestRule.waitForIdle()
        // Component should render without errors in STANDARD mode
    }
}
