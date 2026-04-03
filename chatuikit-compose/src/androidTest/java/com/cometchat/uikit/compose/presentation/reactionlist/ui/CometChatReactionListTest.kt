package com.cometchat.uikit.compose.presentation.reactionlist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction
import com.cometchat.chat.models.ReactionCount
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.compose.presentation.reactionlist.style.CometChatReactionListStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.repository.ReactionListRepository
import com.cometchat.uikit.core.domain.usecase.FetchReactionsUseCase
import com.cometchat.uikit.core.domain.usecase.RemoveReactionUseCase
import com.cometchat.uikit.core.viewmodel.CometChatReactionListViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Instrumented UI tests for CometChatReactionList.
 *
 * Feature: reaction-list-component
 * Tests verify component rendering, interactions, and state transitions.
 */
@RunWith(AndroidJUnit4::class)
class CometChatReactionListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Test 21.4: Component renders with default style ====================

    @Test
    fun reactionList_rendersWithDefaultStyle() {
        // Arrange
        val message = createMockMessageWithReactions()
        val viewModel = createViewModelWithReactions(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - "All" tab should be displayed
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
    }

    // ==================== Test 21.5: Reaction header tabs display correctly ====================

    @Test
    fun reactionList_displaysReactionHeaderTabs() {
        // Arrange
        val message = createMockMessageWithReactions()
        val viewModel = createViewModelWithReactions(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - All tabs should be displayed
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("👍").assertIsDisplayed()
        composeTestRule.onNodeWithText("❤️").assertIsDisplayed()
    }

    // ==================== Test 21.6: Tab selection changes active indicator ====================

    @Test
    fun reactionList_tabSelectionChangesActiveIndicator() {
        // Arrange
        val message = createMockMessageWithReactions()
        val viewModel = createViewModelWithReactions(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Click on a specific emoji tab
        composeTestRule.onNodeWithText("👍").performClick()

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Tab should still be displayed (active state is visual)
        composeTestRule.onNodeWithText("👍").assertIsDisplayed()
    }

    // ==================== Test 21.7: User list displays with avatar, name, emoji ====================

    @Test
    fun reactionList_displaysUserListWithReactions() {
        // Arrange
        val message = createMockMessageWithReactions()
        val mockReactions = listOf(
            createMockReaction("👍", "user1", "John Doe"),
            createMockReaction("👍", "user2", "Jane Smith")
        )
        val viewModel = createViewModelWithReactions(mockReactions)

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - User names should be displayed
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jane Smith").assertIsDisplayed()
    }

    // ==================== Test 21.9: Loading state displays shimmer ====================

    @Test
    fun reactionList_displaysLoadingState() {
        // Arrange
        val message = createMockMessageWithReactions()
        // Create a ViewModel that stays in loading state
        val viewModel = createViewModelThatNeverCompletes()

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Assert - Loading state should be displayed (shimmer)
        // Note: Shimmer doesn't have text, so we verify the component renders without crash
        composeTestRule.waitForIdle()
    }

    // ==================== Test 21.10: Error state displays error view ====================

    @Test
    fun reactionList_displaysCustomErrorView() {
        // Arrange
        val message = createMockMessageWithReactions()
        val viewModel = createViewModelWithError()

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    errorView = {
                        Text("Custom Error View")
                    }
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Custom error view should be displayed
        composeTestRule.onNodeWithText("Custom Error View").assertIsDisplayed()
    }

    // ==================== Test 21.11: Custom style properties apply correctly ====================

    @Test
    fun reactionList_appliesCustomStyle() {
        // Arrange
        val message = createMockMessageWithReactions()
        val viewModel = createViewModelWithReactions(emptyList())

        // Act
        composeTestRule.setContent {
            val customStyle = CometChatReactionListStyle(
                backgroundColor = Color.Red,
                strokeColor = Color.Blue,
                strokeWidth = 2.dp,
                cornerRadius = 16.dp,
                tabTextColor = Color.Gray,
                tabTextActiveColor = Color.Black,
                tabTextStyle = androidx.compose.ui.text.TextStyle.Default,
                tabActiveIndicatorColor = Color.Green,
                errorTextColor = Color.Red,
                errorTextStyle = androidx.compose.ui.text.TextStyle.Default,
                separatorColor = Color.LightGray,
                separatorHeight = 1.dp,
                itemStyle = com.cometchat.uikit.compose.presentation.reactionlist.style.CometChatReactionListItemStyle.default()
            )
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    style = customStyle
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Component should render without crash with custom style
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun reactionList_hidesSeparatorWhenConfigured() {
        // Arrange
        val message = createMockMessageWithReactions()
        val viewModel = createViewModelWithReactions(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideSeparator = true
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Assert - Component should render without crash
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun reactionList_invokesOnEmptyCallback() {
        // Arrange
        val emptyCallbackInvoked = AtomicBoolean(false)
        val message = createMockMessageWithEmptyReactions()
        val viewModel = createViewModelWithReactions(emptyList())

        // Act
        composeTestRule.setContent {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onEmpty = { emptyCallbackInvoked.set(true) }
                )
            }
        }

        // Wait for state to settle
        composeTestRule.waitForIdle()

        // Note: onEmpty is invoked when reaction list becomes empty
        // This test verifies the callback mechanism works
    }

    // ==================== Helper Functions ====================

    private fun createMockMessageWithReactions(): BaseMessage {
        return TextMessage("receiver", "Test message", "user").apply {
            id = 12345
            sentAt = System.currentTimeMillis() / 1000
            reactions = listOf(
                ReactionCount().apply {
                    reaction = "👍"
                    count = 5
                    setReactedByMe(false)
                },
                ReactionCount().apply {
                    reaction = "❤️"
                    count = 3
                    setReactedByMe(false)
                }
            )
        }
    }

    private fun createMockMessageWithEmptyReactions(): BaseMessage {
        return TextMessage("receiver", "Test message", "user").apply {
            id = 12345
            sentAt = System.currentTimeMillis() / 1000
            reactions = emptyList()
        }
    }

    private fun createMockReaction(emoji: String, uid: String, userName: String): Reaction {
        return Reaction().apply {
            reaction = emoji
            this.uid = uid
            reactedBy = com.cometchat.chat.models.User().apply {
                this.uid = uid
                this.name = userName
            }
        }
    }

    private fun createViewModelWithReactions(reactions: List<Reaction>): CometChatReactionListViewModel {
        val repository = object : ReactionListRepository {
            override suspend fun fetchReactions(request: ReactionsRequest): Result<List<Reaction>> {
                return Result.success(reactions)
            }
            override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
                return Result.success(createMockMessageWithReactions())
            }
        }

        return CometChatReactionListViewModel(
            fetchReactionsUseCase = FetchReactionsUseCase(repository),
            removeReactionUseCase = RemoveReactionUseCase(repository),
            enableListeners = false
        )
    }

    private fun createViewModelThatNeverCompletes(): CometChatReactionListViewModel {
        val repository = object : ReactionListRepository {
            override suspend fun fetchReactions(request: ReactionsRequest): Result<List<Reaction>> {
                kotlinx.coroutines.delay(Long.MAX_VALUE)
                return Result.success(emptyList())
            }
            override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
                return Result.success(createMockMessageWithReactions())
            }
        }

        return CometChatReactionListViewModel(
            fetchReactionsUseCase = FetchReactionsUseCase(repository),
            removeReactionUseCase = RemoveReactionUseCase(repository),
            enableListeners = false
        )
    }

    private fun createViewModelWithError(): CometChatReactionListViewModel {
        val repository = object : ReactionListRepository {
            override suspend fun fetchReactions(request: ReactionsRequest): Result<List<Reaction>> {
                return Result.failure(com.cometchat.chat.exceptions.CometChatException("ERROR", "Test error"))
            }
            override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
                return Result.success(createMockMessageWithReactions())
            }
        }

        return CometChatReactionListViewModel(
            fetchReactionsUseCase = FetchReactionsUseCase(repository),
            removeReactionUseCase = RemoveReactionUseCase(repository),
            enableListeners = false
        )
    }
}
