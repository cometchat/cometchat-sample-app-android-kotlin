package com.cometchat.uikit.compose.screenshots

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.conversations.ui.CometChatConversations
import com.cometchat.uikit.compose.presentation.utils.captureWithPopups
import com.cometchat.uikit.compose.preview.presentation.ui.*
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi screenshot tests for CometChatConversations (Compose).
 *
 * Captures golden images for ALL visual states of the CometChatConversations composable
 * using Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Includes:
 * - Static Preview-based tests (captureComposable)
 * - Interaction-driven tests with popup menus and selection modes (captureWithPopups)
 *
 * Each test method produces one golden PNG in src/test/snapshots/.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatConversationsScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatConversationsScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatConversationsScreenshotTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule(order = 1)
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/conversations"
        )
    )

    private companion object {
        private const val AVATAR_BASE_URL = "https://data-us.cometchat.io/assets/images/avatars"
        // Fixed epoch timestamp (Jan 1, 2025 UTC) — deterministic across runs
        private const val FIXED_TIMESTAMP = 1735689600L
    }

    /**
     * Configure a Coil ImageLoader with an interceptor that forces image load failure.
     * This ensures the CometChatAvatar composable falls back to showing name initials
     * (e.g., "IM" for Iron Man) instead of a blank transparent image.
     */
    @Before
    fun setupFakeImageLoader() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                // Throw an exception so AsyncImage triggers onError callback,
                // which sets imageLoadFailed = true and shows name initials
                throw Exception("Fake image load failure for screenshot tests")
            }
        }
        val imageLoader = ImageLoader.Builder(context)
            .components { add(interceptor) }
            .crossfade(false)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateLoading() {
        captureComposable { PreviewConversationListLoading() }
    }

    @Test
    fun stateEmpty() {
        captureComposable { PreviewConversationListEmpty() }
    }

    @Test
    fun stateError() {
        captureComposable { PreviewConversationListError() }
    }

    @Test
    fun stateContent() {
        captureComposable { PreviewConversationListContent() }
    }

    // ==================== Section 3: Selection Mode ====================

    @Test
    fun selectionModeSingle() {
        setContentWithConversations(selectionMode = UIKitConstants.SelectionMode.SINGLE)
        composeTestRule.waitForIdle()

        // Tap the second item to select it
        composeTestRule.onNodeWithText("Captain America").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun selectionModeMultiple() {
        setContentWithConversations(selectionMode = UIKitConstants.SelectionMode.MULTIPLE)
        composeTestRule.waitForIdle()

        // Tap multiple items to select them
        composeTestRule.onNodeWithText("Iron Man").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Spiderman").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Thor").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 4: Custom ViewModels ====================

    @Test
    fun viewModelDefaultFactory() {
        captureComposable { PreviewWithDefaultFactoryViewModel() }
    }

    @Test
    fun viewModelHighUnread() {
        captureComposable { PreviewWithHighUnreadViewModel() }
    }

    @Test
    fun viewModelGroupsOnly() {
        captureComposable { PreviewWithGroupsOnlyViewModel() }
    }

    @Test
    fun viewModelUsersOnly() {
        captureComposable { PreviewWithUsersOnlyViewModel() }
    }

    @Test
    fun viewModelEmptyState() {
        captureComposable { PreviewWithEmptyStateViewModel() }
    }

    @Test
    fun viewModelErrorState() {
        captureComposable { PreviewWithErrorStateViewModel() }
    }

    // ==================== Section 5: Visibility Props ====================

    @Test
    fun visibilityNoToolbar() {
        captureComposable { PreviewNoToolbar() }
    }

    @Test
    fun visibilityNoSearchBox() {
        captureComposable { PreviewNoSearchBox() }
    }

    @Test
    fun visibilityNoSeparators() {
        captureComposable { PreviewNoSeparators() }
    }

    @Test
    fun visibilityNoUserStatus() {
        captureComposable { PreviewHideUserStatus() }
    }

    @Test
    fun visibilityNoGroupType() {
        captureComposable { PreviewHideGroupType() }
    }

    @Test
    fun visibilityNoReceipts() {
        captureComposable { PreviewHideReceipts() }
    }

    @Test
    fun visibilityWithBackButton() {
        captureComposable { PreviewWithBackButton() }
    }

    // ==================== Section 6: Custom View Overrides ====================

    @Test
    fun customLoadingView() {
        captureComposable { PreviewCustomLoadingView() }
    }

    @Test
    fun customEmptyView() {
        captureComposable { PreviewCustomEmptyView() }
    }

    @Test
    fun customErrorView() {
        captureComposable { PreviewCustomErrorView() }
    }

    @Test
    fun customItemView() {
        captureComposable { PreviewCustomItemView() }
    }

    @Test
    fun customLeadingView() {
        captureComposable { PreviewCustomLeadingView() }
    }

    @Test
    fun customTitleView() {
        captureComposable { PreviewListCustomTitleView() }
    }

    @Test
    fun customSubtitleView() {
        captureComposable { PreviewListCustomSubtitleView() }
    }

    @Test
    fun customTrailingView() {
        captureComposable { PreviewListCustomTrailingView() }
    }

    // ==================== Section 7: Toolbar Customization ====================

    @Test
    fun toolbarCustomTitle() {
        captureComposable { PreviewCustomTitle() }
    }

    @Test
    fun toolbarCustomOverflowMenu() {
        captureComposable { PreviewCustomOverflowMenu() }
    }

    @Test
    fun toolbarCustomSearchPlaceholder() {
        captureComposable { PreviewCustomSearchPlaceholder() }
    }

    // ==================== Section 8: Style Customization ====================

    @Test
    fun styleCustomBackground() {
        captureComposable { PreviewCustomBackgroundStyle() }
    }

    @Test
    fun styleCustomTitleColor() {
        captureComposable { PreviewCustomTitleColorStyle() }
    }

    @Test
    fun styleDarkTheme() {
        captureComposable { PreviewDarkThemeStyle() }
    }

    // ==================== Section 9: Comprehensive ====================

    @Test
    fun comprehensiveAllFeatures() {
        captureComposable { PreviewComprehensive() }
    }

    @Test
    fun comprehensiveMinimal() {
        captureComposable { PreviewMinimal() }
    }

    @Test
    fun comprehensiveLargeList() {
        captureComposable { PreviewLargeList() }
    }

    @Test
    fun comprehensiveAllCustomViews() {
        captureComposable { PreviewListAllCustomViews() }
    }

    // ==================== Section 10: Dark Theme ====================


    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun selectionModeSingleDark() {
        setContentWithConversations(selectionMode = UIKitConstants.SelectionMode.SINGLE, isDarkTheme = true)
        composeTestRule.waitForIdle()

        // Tap an item to select it
        composeTestRule.onNodeWithText("Iron Man").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Helper: Static Capture ====================

    /**
     * Launches a fresh Activity, sets Compose content, and captures a screenshot.
     * Each call gets a fresh Activity with a clean ViewModelStore.
     */
    private fun captureComposable(content: @Composable () -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setContent { content() }
        }
        scenario.onActivity { activity ->
            val composeView = activity.window.decorView
                .findViewById<android.view.ViewGroup>(android.R.id.content)
                .getChildAt(0)
            composeView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Interaction Content ====================

    private fun setContentWithConversations(
        selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
        options: ((Context, Conversation) -> List<MenuItem>)? = null,
        isDarkTheme: Boolean = false
    ) {
        val viewModel = createViewModel(sharedConversations)

        composeTestRule.activity.setContent {
            CometChatTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
                CometChatConversations(
                    modifier = Modifier.fillMaxSize(),
                    conversationListViewModel = viewModel,
                    selectionMode = selectionMode,
                    options = options
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    // ==================== Deterministic Mock Data ====================

    private val sharedConversations: List<Conversation>
        get() = createDeterministicConversations()

    private fun createDeterministicConversations(): List<Conversation> {
        val userNames = listOf("Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor")
        val messages = listOf("Hey!", "Sure thing", "Build passed", "On my way", "Sounds good")
        val statuses = listOf(
            CometChatConstants.USER_STATUS_ONLINE,
            CometChatConstants.USER_STATUS_OFFLINE,
            CometChatConstants.USER_STATUS_ONLINE,
            CometChatConstants.USER_STATUS_OFFLINE,
            CometChatConstants.USER_STATUS_ONLINE
        )
        val unreadCounts = listOf(3, 0, 0, 0, 0)

        val userConversations = userNames.mapIndexed { i, name ->
            val user = mock<User>()
            whenever(user.uid).thenReturn("user_$i")
            whenever(user.name).thenReturn(name)
            // No avatar URL — component will show name initials (e.g., "IM", "CA")
            whenever(user.status).thenReturn(statuses[i])

            val message = mock<TextMessage>()
            whenever(message.id).thenReturn((i + 1).toLong())
            whenever(message.text).thenReturn(messages[i])
            whenever(message.sentAt).thenReturn(FIXED_TIMESTAMP - (i * 300L))
            whenever(message.sender).thenReturn(user)
            whenever(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
            whenever(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
            whenever(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)

            val conversation = mock<Conversation>()
            whenever(conversation.conversationId).thenReturn("conv_$i")
            whenever(conversation.conversationType).thenReturn(CometChatConstants.CONVERSATION_TYPE_USER)
            whenever(conversation.conversationWith).thenReturn(user)
            whenever(conversation.lastMessage).thenReturn(message)
            whenever(conversation.unreadMessageCount).thenReturn(unreadCounts[i])
            conversation
        }

        val groupNames = listOf("The Avengers", "Design Team", "Developers Hub")
        val groupUnreads = listOf(5, 0, 0)

        val groupConversations = groupNames.mapIndexed { i, name ->
            val group = mock<Group>()
            whenever(group.guid).thenReturn("group_$i")
            whenever(group.name).thenReturn(name)
            // No icon URL — component will show name initials (e.g., "TA", "DT")
            whenever(group.groupType).thenReturn(CometChatConstants.GROUP_TYPE_PUBLIC)

            val message = mock<TextMessage>()
            whenever(message.id).thenReturn((i + 100).toLong())
            whenever(message.text).thenReturn("Group message $i")
            whenever(message.sentAt).thenReturn(FIXED_TIMESTAMP - ((i + 5) * 300L))
            whenever(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_GROUP)
            whenever(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
            whenever(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)

            val conversation = mock<Conversation>()
            whenever(conversation.conversationId).thenReturn("conv_group_$i")
            whenever(conversation.conversationType).thenReturn(CometChatConstants.CONVERSATION_TYPE_GROUP)
            whenever(conversation.conversationWith).thenReturn(group)
            whenever(conversation.lastMessage).thenReturn(message)
            whenever(conversation.unreadMessageCount).thenReturn(groupUnreads[i])
            conversation
        }

        return userConversations + groupConversations
    }

    // ==================== ViewModel Factory ====================

    private fun createViewModel(conversations: List<Conversation>): CometChatConversationsViewModel {
        var fetched = false
        val repository = object : ConversationListRepository {
            override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
                return if (!fetched) {
                    fetched = true
                    Result.success(conversations)
                } else {
                    Result.success(emptyList())
                }
            }
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
                Result.success(Unit)
            override suspend fun markAsDelivered(conversation: Conversation) =
                Result.success(Unit)
            override fun hasMoreConversations() = !fetched
        }
        return CometChatConversationsViewModel(
            getConversationListUseCase = GetConversationListUseCase(repository),
            deleteConversationUseCase = DeleteConversationUseCase(repository),
            refreshConversationListUseCase = RefreshConversationListUseCase(repository),
            enableListeners = false
        )
    }
}
