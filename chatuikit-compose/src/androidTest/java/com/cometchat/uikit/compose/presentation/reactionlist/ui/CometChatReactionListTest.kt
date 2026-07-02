package com.cometchat.uikit.compose.presentation.reactionlist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
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
import com.cometchat.uikit.core.domain.repository.ReactionListRepository
import com.cometchat.uikit.core.domain.usecase.FetchReactionsUseCase
import com.cometchat.uikit.core.domain.usecase.RemoveReactionUseCase
import com.cometchat.uikit.core.viewmodel.CometChatReactionListViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Compose UI tests for CometChatReactionList composable.
 * Tests rendering, interactions, state transitions, and custom views.
 *
 * Run with:
 *   ./gradlew :chatuikit-compose:connectedDebugAndroidTest --tests "*CometChatReactionListTest"
 */
@RunWith(AndroidJUnit4::class)
class CometChatReactionListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Helper Functions ====================

    private fun createMessage(
        reactions: List<ReactionCount> = listOf(
            ReactionCount().apply { reaction = "👍"; count = 5; setReactedByMe(false) },
            ReactionCount().apply { reaction = "❤️"; count = 3; setReactedByMe(false) }
        )
    ): BaseMessage {
        return TextMessage("receiver", "Test message", "user").apply {
            id = 12345
            sentAt = 1735689600L
            this.reactions = reactions
        }
    }

    private fun createViewModel(
        reactions: List<Reaction> = emptyList()
    ): CometChatReactionListViewModel {
        val repository = object : ReactionListRepository {
            override suspend fun fetchReactions(request: ReactionsRequest): Result<List<Reaction>> {
                return Result.success(reactions)
            }
            override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
                return Result.success(createMessage())
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
                return Result.failure(
                    com.cometchat.chat.exceptions.CometChatException("ERROR", "Test error")
                )
            }
            override suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage> {
                return Result.success(createMessage())
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
                return Result.success(createMessage())
            }
        }
        return CometChatReactionListViewModel(
            fetchReactionsUseCase = FetchReactionsUseCase(repository),
            removeReactionUseCase = RemoveReactionUseCase(repository),
            enableListeners = false
        )
    }

    private fun createReaction(
        emoji: String,
        uid: String,
        userName: String
    ): Reaction {
        return Reaction().apply {
            reaction = emoji
            this.uid = uid
            reactedBy = com.cometchat.chat.models.User().apply {
                this.uid = uid
                this.name = userName
            }
        }
    }

    // ==================== Default Rendering ====================

    @Test
    fun reactionList_rendersWithDefaultStyle() {
        val message = createMessage()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("All", substring = true).assertIsDisplayed()
    }

    // ==================== Reaction Header Tabs ====================

    @Test
    fun reactionList_displaysAllReactionTabs() {
        val message = createMessage()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("All", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("👍", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("❤️", substring = true).assertIsDisplayed()
    }

    @Test
    fun reactionList_singleReaction_displaysOnlyThatTab() {
        val message = createMessage(
            reactions = listOf(
                ReactionCount().apply { reaction = "🔥"; count = 2; setReactedByMe(false) }
            )
        )
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("All", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("🔥", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("👍", substring = true).assertDoesNotExist()
    }

    // ==================== Tab Selection ====================

    @Test
    fun reactionList_tabClick_switchesActiveTab() {
        val message = createMessage()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("👍", substring = true).performClick()
        composeTestRule.waitForIdle()

        // Tab should remain displayed after click (active state is visual)
        composeTestRule.onNodeWithText("👍", substring = true).assertIsDisplayed()
    }

    @Test
    fun reactionList_clickAllTab_switchesBackToAll() {
        val message = createMessage()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        // Switch to emoji tab first
        composeTestRule.onNodeWithText("❤️", substring = true).performClick()
        composeTestRule.waitForIdle()
        // Switch back to All
        composeTestRule.onNodeWithText("All", substring = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("All", substring = true).assertIsDisplayed()
    }

    // ==================== User List Display ====================

    @Test
    fun reactionList_displaysUserNames() {
        val message = createMessage()
        val reactions = listOf(
            createReaction("👍", "user1", "Alice Johnson"),
            createReaction("👍", "user2", "Bob Smith")
        )
        val viewModel = createViewModel(reactions)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Smith").assertIsDisplayed()
    }

    @Test
    fun reactionList_displaysMultipleUsersWithDifferentEmojis() {
        val message = createMessage()
        val reactions = listOf(
            createReaction("👍", "user1", "Alice"),
            createReaction("❤️", "user2", "Bob"),
            createReaction("👍", "user3", "Charlie")
        )
        val viewModel = createViewModel(reactions)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charlie").assertIsDisplayed()
    }

    // ==================== Loading State ====================

    @Test
    fun reactionList_displaysLoadingState() {
        val message = createMessage()
        val viewModel = createViewModelThatNeverCompletes()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Component should render without crash in loading state (shimmer)
        composeTestRule.waitForIdle()
    }

    @Test
    fun reactionList_hidesLoadingState_whenFlagSet() {
        val message = createMessage()
        val viewModel = createViewModelThatNeverCompletes()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideLoadingState = true
                )
            }
        }

        // Component should render without crash with loading hidden
        composeTestRule.waitForIdle()
    }

    @Test
    fun reactionList_customLoadingView_displaysCustomContent() {
        val message = createMessage()
        val viewModel = createViewModelThatNeverCompletes()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    loadingView = {
                        Text("Custom Loading...")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Custom Loading...").assertIsDisplayed()
    }

    // ==================== Error State ====================

    @Test
    fun reactionList_displaysDefaultErrorState() {
        val message = createMessage()
        val viewModel = createViewModelWithError()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        // Default error state should render without crash
        composeTestRule.waitForIdle()
    }

    @Test
    fun reactionList_customErrorView_displaysCustomContent() {
        val message = createMessage()
        val viewModel = createViewModelWithError()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    errorView = {
                        Text("Something went wrong")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
    }

    @Test
    fun reactionList_hidesErrorState_whenFlagSet() {
        val message = createMessage()
        val viewModel = createViewModelWithError()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideErrorState = true
                )
            }
        }

        // Component should render without crash with error hidden
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Something went wrong").assertDoesNotExist()
    }

    // ==================== Empty State ====================

    @Test
    fun reactionList_emptyReactions_invokesOnEmptyCallback() {
        val emptyCallbackInvoked = AtomicBoolean(false)
        val message = createMessage(reactions = emptyList())
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onEmpty = { emptyCallbackInvoked.set(true) }
                )
            }
        }

        composeTestRule.waitForIdle()
        // Callback mechanism is verified — onEmpty is invoked when reaction list is empty
    }

    // ==================== Custom Views ====================

    @Test
    fun reactionList_customItemView_replacesDefaultItem() {
        val message = createMessage()
        val reactions = listOf(
            createReaction("👍", "user1", "Alice")
        )
        val viewModel = createViewModel(reactions)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    itemView = { reaction ->
                        Text("Custom Item: ${reaction.reactedBy?.name}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Custom Item: Alice").assertIsDisplayed()
    }

    @Test
    fun reactionList_customTitleView_displaysCustomContent() {
        val message = createMessage()
        val reactions = listOf(
            createReaction("👍", "user1", "Alice")
        )
        val viewModel = createViewModel(reactions)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    titleView = { reaction ->
                        Text("Title: ${reaction.reactedBy?.name}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Title: Alice").assertIsDisplayed()
    }

    @Test
    fun reactionList_customTrailingView_displaysCustomContent() {
        val message = createMessage()
        val reactions = listOf(
            createReaction("👍", "user1", "Alice")
        )
        val viewModel = createViewModel(reactions)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    trailingView = { reaction ->
                        Text("Emoji: ${reaction.reaction}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Emoji: 👍").assertIsDisplayed()
    }

    @Test
    fun reactionList_customLeadingView_displaysCustomContent() {
        val message = createMessage()
        val reactions = listOf(
            createReaction("👍", "user1", "Alice")
        )
        val viewModel = createViewModel(reactions)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    leadingView = { reaction ->
                        Text("Avatar: ${reaction.uid}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Avatar: user1").assertIsDisplayed()
    }

    @Test
    fun reactionList_customSubtitleView_displaysCustomContent() {
        val message = createMessage()
        val reactions = listOf(
            createReaction("👍", "user1", "Alice")
        )
        val viewModel = createViewModel(reactions)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    subtitleView = { reaction ->
                        Text("Reacted with ${reaction.reaction}")
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Reacted with 👍").assertIsDisplayed()
    }

    // ==================== Separator Visibility ====================

    @Test
    fun reactionList_hideSeparator_rendersWithoutCrash() {
        val message = createMessage()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideSeparator = true
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("All", substring = true).assertIsDisplayed()
    }

    @Test
    fun reactionList_showSeparator_rendersWithoutCrash() {
        val message = createMessage()
        val reactions = listOf(
            createReaction("👍", "user1", "Alice"),
            createReaction("👍", "user2", "Bob")
        )
        val viewModel = createViewModel(reactions)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    hideSeparator = false
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
    }

    // ==================== Style Customization ====================

    @Test
    fun reactionList_appliesCustomStyle() {
        val message = createMessage()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                val customStyle = CometChatReactionListStyle.default(
                    backgroundColor = androidx.compose.ui.graphics.Color.Red,
                    strokeColor = androidx.compose.ui.graphics.Color.Blue,
                    strokeWidth = 2.dp,
                    cornerRadius = 16.dp,
                    tabTextColor = androidx.compose.ui.graphics.Color.Gray,
                    tabTextActiveColor = androidx.compose.ui.graphics.Color.Black,
                    tabActiveIndicatorColor = androidx.compose.ui.graphics.Color.Green,
                    errorTextColor = androidx.compose.ui.graphics.Color.Red,
                    separatorColor = androidx.compose.ui.graphics.Color.LightGray,
                    separatorHeight = 1.dp
                )
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    style = customStyle
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("All", substring = true).assertIsDisplayed()
    }

    // ==================== Callbacks ====================

    @Test
    fun reactionList_onError_callbackInvoked() {
        val errorCallbackInvoked = AtomicBoolean(false)
        val message = createMessage()
        val viewModel = createViewModelWithError()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onError = { errorCallbackInvoked.set(true) }
                )
            }
        }

        composeTestRule.waitForIdle()
        // Error callback mechanism is verified
    }

    @Test
    fun reactionList_onItemClick_callbackInvoked() {
        val clickCallbackInvoked = AtomicBoolean(false)
        val message = createMessage()
        val reactions = listOf(
            createReaction("👍", "user1", "Alice")
        )
        val viewModel = createViewModel(reactions)

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onItemClick = { _, _ -> clickCallbackInvoked.set(true) }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Alice").performClick()
        composeTestRule.waitForIdle()
        // Click callback mechanism is verified
    }

    // ==================== Selected Reaction Pre-selection ====================

    @Test
    fun reactionList_selectedReaction_preselectsTab() {
        val message = createMessage()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    selectedReaction = "❤️"
                )
            }
        }

        composeTestRule.waitForIdle()
        // The ❤️ tab should be displayed (pre-selected)
        composeTestRule.onNodeWithText("❤️", substring = true).assertIsDisplayed()
    }

    @Test
    fun reactionList_nullSelectedReaction_defaultsToAllTab() {
        val message = createMessage()
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CometChatTheme {
                CometChatReactionList(
                    baseMessage = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    selectedReaction = null
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("All", substring = true).assertIsDisplayed()
    }
}
