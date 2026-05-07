package com.cometchat.uikit.kotlin.presentation.conversations

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.repository.ConversationListRepository
import com.cometchat.uikit.core.domain.usecase.DeleteConversationUseCase
import com.cometchat.uikit.core.domain.usecase.GetConversationListUseCase
import com.cometchat.uikit.core.domain.usecase.RefreshConversationListUseCase
import com.cometchat.uikit.core.viewmodel.CometChatConversationsViewModel
import com.cometchat.uikit.kotlin.presentation.conversations.style.CometChatConversationsStyle
import com.cometchat.uikit.kotlin.presentation.conversations.ui.CometChatConversations
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatConversations.
 *
 * Captures golden images for ALL visual states of the CometChatConversations component
 * using Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * Each test method produces one golden PNG in src/test/snapshots/.
 *
 * Requirements: 3.1, 3.2, 3.3, 3.4
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatConversationsScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "src/test/snapshots"
        )
    )

    // ==================== UI States ====================

    @Test
    fun stateLoading() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            // Loading is the initial state before ViewModel fetches data.
            // We use a slow/never-resolving repository to keep it in Loading.
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateEmpty() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createErrorViewModel("LOAD_ERR", "Failed to load conversations")
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            view
        }
    }

    // ==================== Selection Modes ====================

    @Test
    fun selectionSingle() {
        launchAndCaptureWithPostAction(
            configure = { activity ->
                val view = createConversationsView(activity)
                val conversations = createMockUserConversations(5)
                val vm = createViewModel(conversations)
                view.setViewModel(vm)
                view.tag = conversations // stash for post-action
                view
            },
            postAction = { view ->
                @Suppress("UNCHECKED_CAST")
                val conversations = view.tag as List<Conversation>
                // Set selection mode and select AFTER the ViewModel's initial state has been observed
                view.setSelectionMode(UIKitConstants.SelectionMode.SINGLE)
                view.selectConversation(conversations[1], UIKitConstants.SelectionMode.SINGLE)
            }
        )
    }

    @Test
    fun selectionMultiple() {
        launchAndCaptureWithPostAction(
            configure = { activity ->
                val view = createConversationsView(activity)
                val conversations = createMockUserConversations(5)
                val vm = createViewModel(conversations)
                view.setViewModel(vm)
                view.tag = conversations // stash for post-action
                view
            },
            postAction = { view ->
                @Suppress("UNCHECKED_CAST")
                val conversations = view.tag as List<Conversation>
                // Set selection mode and select AFTER the ViewModel's initial state has been observed
                view.setSelectionMode(UIKitConstants.SelectionMode.MULTIPLE)
                view.selectConversation(conversations[0], UIKitConstants.SelectionMode.MULTIPLE)
                view.selectConversation(conversations[2], UIKitConstants.SelectionMode.MULTIPLE)
            }
        )
    }

    // ==================== Visibility Toggles ====================

    @Test
    fun visibilityNoToolbar() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            view.setToolbarVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSearchBox() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            view.setSearchBoxVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoSeparators() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            view.setSeparatorVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoUserStatus() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            view.setUserStatusVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoGroupType() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockGroupConversations(5))
            view.setViewModel(vm)
            view.setGroupTypeVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityNoReceipts() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            view.setReceiptsVisibility(View.GONE)
            view
        }
    }

    @Test
    fun visibilityWithBackButton() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            view.setBackIconVisibility(View.VISIBLE)
            view
        }
    }

    // ==================== Custom Views ====================

    @Test
    fun customLoadingView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val customLoading = TextView(activity).apply {
                text = "Custom Loading..."
                textSize = 18f
                setTextColor(Color.DKGRAY)
                setPadding(32, 64, 32, 64)
            }
            view.setLoadingView(customLoading)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun customEmptyView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val customEmpty = TextView(activity).apply {
                text = "No conversations yet!\nStart chatting now."
                textSize = 16f
                setTextColor(Color.GRAY)
                setPadding(32, 64, 32, 64)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            view.setEmptyView(customEmpty)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun customErrorView() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val customError = TextView(activity).apply {
                text = "Oops! Something went wrong.\nPlease try again later."
                textSize = 16f
                setTextColor(Color.RED)
                setPadding(32, 64, 32, 64)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            view.setErrorView(customError)
            val vm = createErrorViewModel("CUSTOM_ERR", "Custom error")
            view.setViewModel(vm)
            view
        }
    }

    // ==================== Toolbar ====================

    @Test
    fun toolbarCustomTitle() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            view.setTitle("My Chats")
            view
        }
    }

    // ==================== Style ====================

    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            val customStyle = CometChatConversationsStyle(
                backgroundColor = Color.parseColor("#F5F5DC")
            )
            view.setStyle(customStyle)
            view
        }
    }

    // ==================== Content Variants ====================

    @Test
    fun contentHighUnreadCounts() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val conversations = createMockUserConversationsWithUnread(5)
            val vm = createViewModel(conversations)
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun contentGroupsOnly() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockGroupConversations(5))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun contentUsersOnly() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockUserConversations(5))
            view.setViewModel(vm)
            view
        }
    }

    @Test
    fun contentLargeList() {
        launchAndCapture { activity ->
            val view = createConversationsView(activity)
            val vm = createViewModel(createMockMixedConversations(20))
            view.setViewModel(vm)
            view
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Launches an ActivityScenario, inflates CometChatConversations,
     * configures it via the provided block, and captures a screenshot.
     */
    private fun launchAndCapture(
        configure: (ComponentActivity) -> CometChatConversations
    ) {
        launchAndCaptureWithPostAction(configure = configure, postAction = null)
    }

    /**
     * Launches an ActivityScenario, inflates CometChatConversations,
     * configures it, idles the looper (so ViewModel flows are collected),
     * then runs a post-action (e.g., selection) before capturing.
     */
    private fun launchAndCaptureWithPostAction(
        configure: (ComponentActivity) -> CometChatConversations,
        postAction: ((CometChatConversations) -> Unit)? = null
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            // Apply CometChat Material theme to resolve all Material attributes
            activity.setTheme(com.cometchat.uikit.kotlin.R.style.CometChatTheme_DayNight)
            val view = configure(activity)

            // Attach to activity window for proper rendering
            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            container.addView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            activity.setContentView(container)

            // Idle the main looper so ViewModel flows are collected and adapter is populated
            ShadowLooper.idleMainLooper()

            // Run post-action (e.g., selection) AFTER ViewModel state has been observed
            postAction?.invoke(view)

            // Idle again to process any UI updates from the post-action
            ShadowLooper.idleMainLooper()

            // Measure and layout to ensure the view is rendered
            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            // Idle again after layout to process any pending UI updates
            ShadowLooper.idleMainLooper()

            // Capture screenshot
            view.captureRoboImage()
        }
        scenario.close()
    }

    /**
     * Creates a CometChatConversations view instance.
     */
    private fun createConversationsView(activity: ComponentActivity): CometChatConversations {
        return CometChatConversations(activity)
    }

    // ==================== Mock Data Factories ====================

    private fun createMockUserConversations(count: Int): List<Conversation> {
        return (1..count).map { i ->
            val user = mock<User>()
            whenever(user.uid).thenReturn("user-$i")
            whenever(user.name).thenReturn("User $i")
            whenever(user.status).thenReturn(if (i % 2 == 0) "online" else "offline")
            whenever(user.avatar).thenReturn(null)

            val conversation = mock<Conversation>()
            whenever(conversation.conversationId).thenReturn("conv-user-$i")
            whenever(conversation.conversationType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
            whenever(conversation.conversationWith).thenReturn(user)
            whenever(conversation.unreadMessageCount).thenReturn(0)
            whenever(conversation.lastMessage).thenReturn(null)
            conversation
        }
    }

    private fun createMockUserConversationsWithUnread(count: Int): List<Conversation> {
        return (1..count).map { i ->
            val user = mock<User>()
            whenever(user.uid).thenReturn("user-$i")
            whenever(user.name).thenReturn("User $i")
            whenever(user.status).thenReturn("online")
            whenever(user.avatar).thenReturn(null)

            val conversation = mock<Conversation>()
            whenever(conversation.conversationId).thenReturn("conv-user-$i")
            whenever(conversation.conversationType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
            whenever(conversation.conversationWith).thenReturn(user)
            whenever(conversation.unreadMessageCount).thenReturn(i * 10) // 10, 20, 30, 40, 50
            whenever(conversation.lastMessage).thenReturn(null)
            conversation
        }
    }

    private fun createMockGroupConversations(count: Int): List<Conversation> {
        return (1..count).map { i ->
            val group = mock<Group>()
            whenever(group.guid).thenReturn("group-$i")
            whenever(group.name).thenReturn("Group $i")
            whenever(group.groupType).thenReturn(
                if (i % 3 == 0) CometChatConstants.GROUP_TYPE_PASSWORD
                else if (i % 2 == 0) CometChatConstants.GROUP_TYPE_PRIVATE
                else CometChatConstants.GROUP_TYPE_PUBLIC
            )
            whenever(group.membersCount).thenReturn(i * 3)
            whenever(group.icon).thenReturn(null)

            val conversation = mock<Conversation>()
            whenever(conversation.conversationId).thenReturn("conv-group-$i")
            whenever(conversation.conversationType).thenReturn(CometChatConstants.RECEIVER_TYPE_GROUP)
            whenever(conversation.conversationWith).thenReturn(group)
            whenever(conversation.unreadMessageCount).thenReturn(0)
            whenever(conversation.lastMessage).thenReturn(null)
            conversation
        }
    }

    private fun createMockMixedConversations(count: Int): List<Conversation> {
        val userCount = count / 2
        val groupCount = count - userCount
        return createMockUserConversations(userCount) + createMockGroupConversations(groupCount)
    }

    // ==================== ViewModel Factories ====================

    private fun createViewModel(conversations: List<Conversation>): CometChatConversationsViewModel {
        val repository = object : ConversationListRepository {
            override suspend fun getConversations(request: ConversationsRequest) =
                Result.success(conversations)
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
                Result.success(Unit)
            override suspend fun markAsDelivered(conversation: Conversation) =
                Result.success(Unit)
            override fun hasMoreConversations() = false
        }
        return CometChatConversationsViewModel(
            getConversationListUseCase = GetConversationListUseCase(repository),
            deleteConversationUseCase = DeleteConversationUseCase(repository),
            refreshConversationListUseCase = RefreshConversationListUseCase(repository),
            enableListeners = false
        )
    }

    private fun createErrorViewModel(code: String, message: String): CometChatConversationsViewModel {
        val repository = object : ConversationListRepository {
            override suspend fun getConversations(request: ConversationsRequest) =
                Result.failure<List<Conversation>>(CometChatException(code, message))
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
                Result.success(Unit)
            override suspend fun markAsDelivered(conversation: Conversation) =
                Result.success(Unit)
            override fun hasMoreConversations() = false
        }
        return CometChatConversationsViewModel(
            getConversationListUseCase = GetConversationListUseCase(repository),
            deleteConversationUseCase = DeleteConversationUseCase(repository),
            refreshConversationListUseCase = RefreshConversationListUseCase(repository),
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that stays in Loading state by using a repository
     * that never completes (suspends indefinitely).
     */
    private fun createLoadingViewModel(): CometChatConversationsViewModel {
        val repository = object : ConversationListRepository {
            override suspend fun getConversations(request: ConversationsRequest): Result<List<Conversation>> {
                // Suspend indefinitely to keep the ViewModel in Loading state
                kotlinx.coroutines.awaitCancellation()
            }
            override suspend fun deleteConversation(conversationWith: String, conversationType: String) =
                Result.success(Unit)
            override suspend fun markAsDelivered(conversation: Conversation) =
                Result.success(Unit)
            override fun hasMoreConversations() = false
        }
        return CometChatConversationsViewModel(
            getConversationListUseCase = GetConversationListUseCase(repository),
            deleteConversationUseCase = DeleteConversationUseCase(repository),
            refreshConversationListUseCase = RefreshConversationListUseCase(repository),
            enableListeners = false
        )
    }
}
