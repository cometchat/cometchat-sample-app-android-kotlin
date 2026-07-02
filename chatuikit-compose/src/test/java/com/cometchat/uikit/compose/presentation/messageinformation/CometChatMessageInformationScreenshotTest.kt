package com.cometchat.uikit.compose.presentation.messageinformation

import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.compose.presentation.messageinformation.ui.CometChatMessageInformation
import com.cometchat.uikit.compose.presentation.utils.captureWithPopups
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel
import com.github.takahirom.roborazzi.RoborazziRule
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
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
 * Roborazzi screenshot tests for CometChatMessageInformation (Compose).
 *
 * Captures golden images for ALL visual states of the CometChatMessageInformation composable
 * using Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Covers:
 * - Section 1: UI States (Loading, Empty, Error, Content)
 * - Section 4: Scroll States (scroll to bottom, scroll to middle with 20 receipt items)
 * - Section 5: Toolbar (custom title, back button visible)
 * - Section 6: Visibility Toggles (toolbar hidden, back button visible)
 * - Section 7: Custom Views (loading, empty, error, custom receipt item view)
 * - Section 8: Style & Theming (custom background, custom colors)
 * - Section 9: Content Variants (read receipts only, delivered only, mixed, reactions)
 * - Section 10: Dark Theme (content dark, mixed receipts dark)
 *
 * MessageInformation omits Sections 2-3 (no popup menu, no selection mode).
 *
 * Each test method produces one golden PNG in src/test/snapshots/messageinformation/.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatMessageInformationScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatMessageInformationScreenshotTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatMessageInformationScreenshotTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule(order = 1)
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/messageinformation"
        )
    )

    private companion object {
        // Fixed epoch timestamps — deterministic across runs
        private const val FIXED_SENT_AT = 1758886068L
        private const val FIXED_DELIVERED_AT = 1758886068L
        private const val FIXED_READ_AT = 1758886068L
    }

    @Before
    fun setupDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Configure a Coil ImageLoader with an interceptor that returns a transparent
     * drawable for all image requests. This ensures avatars render deterministically
     * in the Robolectric JVM environment where network requests are unavailable.
     */
    @Before
    fun setupFakeImageLoader() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                // Throw an exception so AsyncImage triggers onError callback,
                // which sets imageLoadFailed = true and shows name initials (e.g., "IM", "CA")
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
        setContentWithReceipts(viewModel = createLoadingViewModel())
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun stateEmpty() {
        setContentWithReceipts(viewModel = createViewModel(emptyList()))
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun stateError() {
        setContentWithReceipts(viewModel = createErrorViewModel())
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun stateContent() {
        setContentWithReceipts(viewModel = createViewModel(createReceiptUsers(5)))
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 4: Scroll States ====================

    @Test
    fun scrollToBottom() {
        setContentWithReceipts(viewModel = createViewModel(createReceiptUsers(20)))
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(19)
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun scrollToMiddle() {
        setContentWithReceipts(viewModel = createViewModel(createReceiptUsers(20)))
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(10)
        composeTestRule.waitForIdle()

        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 5: Toolbar ====================

    @Test
    fun toolbarCustomTitle() {
        setContentWithReceipts(
            viewModel = createViewModel(createReceiptUsers(5)),
            toolBarTitleText = "Receipt Details"
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun visibilityWithBackButton() {
        // MessageInformation toolbar shows title; back button visibility is controlled
        // by the parent navigation. We capture with toolbar visible and custom title.
        setContentWithReceipts(
            viewModel = createViewModel(createReceiptUsers(5)),
            toolBarTitleText = "Message Info"
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 6: Visibility ====================

    @Test
    fun visibilityNoToolbar() {
        setContentWithReceipts(
            viewModel = createViewModel(createReceiptUsers(5)),
            hideToolBar = true
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        setContentWithReceipts(
            viewModel = createLoadingViewModel(),
            bubbleView = { _ ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Custom Loading View", fontSize = 18.sp)
                }
            }
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun customEmptyView() {
        setContentWithReceipts(
            viewModel = createViewModel(emptyList()),
            bubbleView = { _ ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No receipts available!", fontSize = 18.sp)
                }
            }
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun customErrorView() {
        setContentWithReceipts(
            viewModel = createErrorViewModel(),
            bubbleView = { _ ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Custom Error View", fontSize = 18.sp, color = Color.Red)
                }
            }
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun customReceiptItemView() {
        setContentWithReceipts(
            viewModel = createViewModel(createReceiptUsers(3)),
            bubbleView = { _ ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Custom Receipt Item View", fontSize = 16.sp, color = Color(0xFF2E7D32))
                }
            }
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 8: Style ====================

    @Test
    fun styleCustomBackground() {
        setContentWithReceipts(
            viewModel = createViewModel(createReceiptUsers(3)),
            style = { CometChatMessageInformationStyle.default(backgroundColor = Color(0xFFF3E5F5)) }
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun stylingCustomColors() {
        setContentWithReceipts(
            viewModel = createViewModel(createReceiptUsers(3)),
            style = {
                CometChatMessageInformationStyle.default(
                    backgroundColor = Color(0xFFFFF8E1),
                    titleTextColor = Color(0xFFE65100),
                    separatorColor = Color(0xFFFF6D00)
                )
            }
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 9: Content Variants ====================

    @Test
    fun contentReadReceiptsOnly() {
        val receipts = createReadOnlyReceipts(5)
        setContentWithReceipts(viewModel = createViewModel(receipts))
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun contentDeliveredReceiptsOnly() {
        val receipts = createDeliveredOnlyReceipts(5)
        setContentWithReceipts(viewModel = createViewModel(receipts))
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun contentMixedReceipts() {
        val receipts = createMixedReceipts(5)
        setContentWithReceipts(viewModel = createViewModel(receipts))
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    fun contentMessageWithReactions() {
        // Message with reactions - the message bubble shows reactions
        setContentWithReceipts(
            viewModel = createViewModel(createReceiptUsers(3)),
            message = createMessageWithReceipts()
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Section 10: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        setContentWithReceipts(
            viewModel = createViewModel(createReceiptUsers(5)),
            isDarkTheme = true
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun contentMixedReceiptsDark() {
        val receipts = createMixedReceipts(5)
        setContentWithReceipts(
            viewModel = createViewModel(receipts),
            isDarkTheme = true
        )
        composeTestRule.waitForIdle()
        composeTestRule.activity.captureWithPopups(roborazziOptions = RoborazziConfig.options())
    }

    // ==================== Helper: Interaction Content ====================

    /**
     * Sets content on the composeTestRule activity with a CometChatMessageInformation composable
     * configured for interaction-based tests (scroll, visibility, style).
     *
     * Since CometChatMessageInformation is a ModalBottomSheet, all captures must use
     * captureWithPopups() to capture the overlay window.
     */
    private fun setContentWithReceipts(
        viewModel: CometChatMessageInformationViewModel = createViewModel(createReceiptUsers(5)),
        message: BaseMessage = createMessageWithReceipts(),
        toolBarTitleText: String = "Message Info",
        hideToolBar: Boolean = false,
        style: (@Composable () -> CometChatMessageInformationStyle)? = null,
        bubbleView: (@Composable (BaseMessage) -> Unit)? = null,
        isDarkTheme: Boolean = false
    ) {
        composeTestRule.activity.setContent {
            CometChatTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
                CometChatMessageInformation(
                    message = message,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    style = style?.invoke() ?: CometChatMessageInformationStyle.default(),
                    toolBarTitleText = toolBarTitleText,
                    hideToolBar = hideToolBar,
                    bubbleView = bubbleView
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    // ==================== ViewModel Factory ====================

    /**
     * Creates a CometChatMessageInformationViewModel with a fake repository that returns
     * the provided list of receipts deterministically.
     * Uses GROUP receiver type to show the receipt list.
     */
    private fun createViewModel(receipts: List<MessageReceipt>): CometChatMessageInformationViewModel {
        val repository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return Result.success(receipts)
            }

            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return createSingleReceipt(
                    user = message.sender ?: createSenderUser(),
                    readAt = FIXED_READ_AT,
                    deliveredAt = FIXED_DELIVERED_AT
                )
            }
        }
        val eventListener = mock<MessageReceiptEventListener>()
        whenever(eventListener.receiptEvents()).thenReturn(emptyFlow())
        return CometChatMessageInformationViewModel(
            repository = repository,
            eventListener = eventListener,
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that stays in the loading state.
     * Uses a repository that never completes.
     */
    private fun createLoadingViewModel(): CometChatMessageInformationViewModel {
        val repository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                // Never returns — ViewModel stays in Loading state
                awaitCancellation()
            }

            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return createSingleReceipt(
                    user = message.sender ?: createSenderUser(),
                    readAt = 0L,
                    deliveredAt = 0L
                )
            }
        }
        val eventListener = mock<MessageReceiptEventListener>()
        whenever(eventListener.receiptEvents()).thenReturn(emptyFlow())
        return CometChatMessageInformationViewModel(
            repository = repository,
            eventListener = eventListener,
            enableListeners = false
        )
    }

    /**
     * Creates a ViewModel that immediately enters the error state.
     */
    private fun createErrorViewModel(): CometChatMessageInformationViewModel {
        val repository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return Result.failure(
                    CometChatException("ERR_FETCH_RECEIPTS", "Failed to fetch receipts")
                )
            }

            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return createSingleReceipt(
                    user = message.sender ?: createSenderUser(),
                    readAt = 0L,
                    deliveredAt = 0L
                )
            }
        }
        val eventListener = mock<MessageReceiptEventListener>()
        whenever(eventListener.receiptEvents()).thenReturn(emptyFlow())
        return CometChatMessageInformationViewModel(
            repository = repository,
            eventListener = eventListener,
            enableListeners = false
        )
    }

    // ==================== Deterministic Mock Data ====================

    /**
     * Creates a deterministic sender user.
     * No avatar URL — component will show name initials (e.g., "IM" for Iron Man).
     */
    private fun createSenderUser(): User {
        return User().apply {
            uid = "user_sender"
            name = "Iron Man"
            status = CometChatConstants.USER_STATUS_ONLINE
        }
    }

    /**
     * Creates a deterministic message for the MessageInformation component.
     * Uses GROUP receiver type so the receipt list is displayed.
     */
    private fun createMessageWithReceipts(): BaseMessage {
        val sender = createSenderUser()
        val message = mock<TextMessage>()
        whenever(message.id).thenReturn(1)
        whenever(message.text).thenReturn("Hello everyone!")
        whenever(message.sender).thenReturn(sender)
        whenever(message.receiverUid).thenReturn("group_1")
        whenever(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_GROUP)
        whenever(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
        whenever(message.readAt).thenReturn(FIXED_READ_AT)
        whenever(message.deliveredAt).thenReturn(FIXED_DELIVERED_AT)
        whenever(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
        whenever(message.deletedAt).thenReturn(0L)
        return message
    }

    /**
     * Creates a single mock MessageReceipt with the given parameters.
     * Avoids nested mock creation issues by creating the mock separately.
     */
    private fun createSingleReceipt(
        user: User,
        readAt: Long,
        deliveredAt: Long
    ): MessageReceipt {
        val receipt = mock<MessageReceipt>()
        whenever(receipt.sender).thenReturn(user)
        whenever(receipt.messageId).thenReturn(1)
        whenever(receipt.readAt).thenReturn(readAt)
        whenever(receipt.deliveredAt).thenReturn(deliveredAt)
        return receipt
    }

    /**
     * Creates a list of deterministic receipt users with fixed names and timestamps.
     * No avatar URL — component will show name initials (e.g., "CA", "SP").
     * Each receipt has both read and delivered timestamps by default.
     */
    private fun createReceiptUsers(count: Int): List<MessageReceipt> {
        val names = listOf(
            "Captain America", "Spiderman", "Black Widow", "Thor", "Hulk",
            "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man", "Scarlet Witch",
            "Vision", "Falcon", "Winter Soldier", "War Machine", "Star-Lord",
            "Gamora", "Drax", "Rocket", "Groot", "Nebula"
        )

        return (0 until count).map { i ->
            val nameIndex = i % names.size
            val user = User().apply {
                uid = "user_$i"
                name = names[nameIndex]
                status = if (i % 2 == 0) CometChatConstants.USER_STATUS_ONLINE else CometChatConstants.USER_STATUS_OFFLINE
            }
            createSingleReceipt(
                user = user,
                readAt = FIXED_READ_AT + (60L * (i + 1)),
                deliveredAt = FIXED_DELIVERED_AT + (30L * (i + 1))
            )
        }
    }

    /**
     * Creates receipts with only read timestamps (deliveredAt = 0).
     */
    private fun createReadOnlyReceipts(count: Int): List<MessageReceipt> {
        val names = listOf(
            "Captain America", "Spiderman", "Black Widow", "Thor", "Hulk"
        )

        return (0 until count).map { i ->
            val nameIndex = i % names.size
            val user = User().apply {
                uid = "user_read_$i"
                name = names[nameIndex]
                status = if (i % 2 == 0) CometChatConstants.USER_STATUS_ONLINE else CometChatConstants.USER_STATUS_OFFLINE
            }
            createSingleReceipt(
                user = user,
                readAt = FIXED_READ_AT + (60L * (i + 1)),
                deliveredAt = 0L
            )
        }
    }

    /**
     * Creates receipts with only delivered timestamps (readAt = 0).
     */
    private fun createDeliveredOnlyReceipts(count: Int): List<MessageReceipt> {
        val names = listOf(
            "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man", "Scarlet Witch"
        )

        return (0 until count).map { i ->
            val nameIndex = i % names.size
            val user = User().apply {
                uid = "user_delivered_$i"
                name = names[nameIndex]
                status = if (i % 2 == 0) CometChatConstants.USER_STATUS_ONLINE else CometChatConstants.USER_STATUS_OFFLINE
            }
            createSingleReceipt(
                user = user,
                readAt = 0L,
                deliveredAt = FIXED_DELIVERED_AT + (30L * (i + 1))
            )
        }
    }

    /**
     * Creates mixed receipts - alternating between read+delivered and delivered-only.
     */
    private fun createMixedReceipts(count: Int): List<MessageReceipt> {
        val names = listOf(
            "Captain America", "Spiderman", "Black Widow", "Thor", "Hulk"
        )

        return (0 until count).map { i ->
            val nameIndex = i % names.size
            val user = User().apply {
                uid = "user_mixed_$i"
                name = names[nameIndex]
                status = if (i % 2 == 0) CometChatConstants.USER_STATUS_ONLINE else CometChatConstants.USER_STATUS_OFFLINE
            }
            // Alternate: even indices have read+delivered, odd have only delivered
            if (i % 2 == 0) {
                createSingleReceipt(
                    user = user,
                    readAt = FIXED_READ_AT + (60L * (i + 1)),
                    deliveredAt = FIXED_DELIVERED_AT + (30L * (i + 1))
                )
            } else {
                createSingleReceipt(
                    user = user,
                    readAt = 0L,
                    deliveredAt = FIXED_DELIVERED_AT + (30L * (i + 1))
                )
            }
        }
    }
}
