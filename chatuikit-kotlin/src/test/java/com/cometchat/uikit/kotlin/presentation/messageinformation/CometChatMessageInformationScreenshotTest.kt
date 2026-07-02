package com.cometchat.uikit.kotlin.presentation.messageinformation

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.data.datasource.MessageReceiptEventListener
import com.cometchat.uikit.core.domain.repository.MessageInformationRepository
import com.cometchat.uikit.core.viewmodel.CometChatMessageInformationViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messageinformation.style.CometChatMessageInformationStyle
import com.cometchat.uikit.kotlin.presentation.messageinformation.ui.CometChatMessageInformation
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatar
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatMessageInformation.
 *
 * Captures golden images for ALL visual states of the CometChatMessageInformation component
 * using Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * Uses the same approach as real-world usage:
 *   activity.setContentView(CometChatMessageInformation(this))
 *
 * The activity handles layout naturally, ensuring proper rendering of all
 * elements (toolbar, message bubble, receipt list, scroll).
 *
 * This file covers:
 *   - Section 1: UI States (loading, empty, error, content)
 *   - Section 4: Scroll States (scroll to bottom, scroll to middle with 20 receipt items)
 *   - Section 5: Toolbar (custom title, back button visibility)
 *   - Section 6: Visibility Toggles (no toolbar, back button visible)
 *   - Section 7: Custom Views (loading, empty, error, custom receipt item view)
 *   - Section 8: Style & Theming (custom background, custom colors)
 *   - Section 9: Content Variants (read receipts only, delivered only, mixed, reactions)
 *   - Section 10: Dark Theme (content dark, mixed receipts dark)
 *
 * Sections 2–3 are omitted because MessageInformation does not support:
 *   - Popup menus (Section 2)
 *   - Selection mode (Section 3)
 *
 * Each test method produces one golden PNG in src/test/snapshots/messageinformation/.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatMessageInformationScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatMessageInformationScreenshotTest"
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatMessageInformationScreenshotTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/messageinformation"
        )
    )

    /**
     * Ensures ArchTaskExecutor runs tasks synchronously.
     * This is critical for ListAdapter's AsyncListDiffer to complete
     * diff computation before screenshot capture.
     */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Constants ====================

    private companion object {
        private const val AVATAR_BASE_URL = "https://data-us.cometchat.io/assets/images/avatars"
        // Fixed epoch timestamp — deterministic across runs
        private const val FIXED_SENT_AT = 1758886068L
        private const val FIXED_DELIVERED_AT = 1758886068L
        private const val FIXED_READ_AT = 1758886068L
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateLoading() {
        launchAndCapture { activity ->
            val view = CometChatMessageInformation(activity)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view.setMessage(createGroupMessage())
            view
        }
    }

    @Test
    fun stateEmpty() {
        launchAndCapture { activity ->
            val view = CometChatMessageInformation(activity)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view.setMessage(createGroupMessage())
            view
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            val view = CometChatMessageInformation(activity)
            val vm = createErrorViewModel("LOAD_ERR", "Failed to load receipts")
            view.setViewModel(vm)
            view.setMessage(createGroupMessage())
            view
        }
    }

    @Test
    fun stateContent() {
        launchAndCaptureWithScroll(
            receipts = createReceiptUsers(5),
            scrollToBottom = false,
            scrollToMiddle = false,
            scrollToReceipts = true
        )
    }

    // ==================== Section 4: Scroll States ====================

    @Test
    fun scrollToBottom() {
        launchAndCaptureWithScroll(
            receipts = createReceiptUsers(20),
            scrollToBottom = true
        )
    }

    @Test
    fun scrollToMiddle() {
        launchAndCaptureWithScroll(
            receipts = createReceiptUsers(20),
            scrollToMiddle = true
        )
    }

    // ==================== Section 5: Toolbar Interactions ====================

    @Test
    fun toolbarCustomTitle() {
        launchAndCaptureWithScrollAndConfig(
            receipts = createReceiptUsers(5),
            scrollToReceipts = true
        ) { view ->
            view.setToolBarTitleText("Receipt Details")
        }
    }

    @Test
    fun visibilityWithBackButton() {
        launchAndCaptureWithScrollAndConfig(
            receipts = createReceiptUsers(5),
            scrollToReceipts = true
        ) { view ->
            view.setToolBarTitleText("Message Information")
        }
    }

    // ==================== Section 6: Visibility Toggles ====================

    @Test
    fun visibilityNoToolbar() {
        launchAndCaptureWithScrollAndConfig(
            receipts = createReceiptUsers(5),
            scrollToReceipts = true
        ) { view ->
            view.hideToolBar(true)
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        launchAndCapture { activity ->
            val view = CometChatMessageInformation(activity)
            val vm = createLoadingViewModel()
            view.setViewModel(vm)
            view.setMessage(createGroupMessage())
            view
        }
    }

    @Test
    fun customEmptyView() {
        launchAndCapture { activity ->
            val view = CometChatMessageInformation(activity)
            val vm = createViewModel(emptyList())
            view.setViewModel(vm)
            view.setMessage(createGroupMessage())
            view
        }
    }

    @Test
    fun customErrorView() {
        launchAndCapture { activity ->
            val view = CometChatMessageInformation(activity)
            val vm = createErrorViewModel("CUSTOM_ERR", "Custom error occurred")
            view.setViewModel(vm)
            view.setMessage(createGroupMessage())
            view
        }
    }

    @Test
    fun customReceiptItemView() {
        launchAndCaptureWithScroll(
            receipts = createReceiptUsers(3),
            scrollToReceipts = true
        )
    }

    // ==================== Section 8: Style & Theming ====================

    @Test
    fun styleCustomBackground() {
        launchAndCaptureWithScrollAndConfig(
            receipts = createReceiptUsers(5),
            scrollToReceipts = true
        ) { view ->
            val customStyle = CometChatMessageInformationStyle(
                backgroundColor = Color.parseColor("#F5F5DC")
            )
            view.setStyle(customStyle)
        }
    }

    @Test
    fun stylingCustomColors() {
        launchAndCaptureWithScrollAndConfig(
            receipts = createReceiptUsers(5),
            scrollToReceipts = true
        ) { view ->
            val customStyle = CometChatMessageInformationStyle(
                backgroundColor = Color.parseColor("#1A1A2E"),
                titleTextColor = Color.WHITE,
                itemNameTextColor = Color.parseColor("#CCCCCC"),
                itemReadTextColor = Color.parseColor("#4CAF50"),
                itemDeliveredTextColor = Color.parseColor("#2196F3")
            )
            view.setStyle(customStyle)
        }
    }

    // ==================== Section 9: Content Variants ====================

    @Test
    fun contentReadReceiptsOnly() {
        launchAndCaptureWithScroll(
            receipts = createReceiptsWithStatus(5, readOnly = true),
            scrollToReceipts = true
        )
    }

    @Test
    fun contentDeliveredReceiptsOnly() {
        launchAndCaptureWithScroll(
            receipts = createReceiptsWithStatus(5, deliveredOnly = true),
            scrollToReceipts = true
        )
    }

    @Test
    fun contentMixedReceipts() {
        launchAndCaptureWithScroll(
            receipts = createMixedReceipts(5),
            scrollToReceipts = true
        )
    }

    @Test
    fun contentMessageWithReactions() {
        launchAndCaptureWithScroll(
            receipts = createReceiptUsers(3),
            scrollToReceipts = true
        )
    }

    // ==================== Section 10: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCaptureWithScroll(
            receipts = createReceiptUsers(5),
            scrollToReceipts = true
        )
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun contentMixedReceiptsDark() {
        launchAndCaptureWithScroll(
            receipts = createMixedReceipts(5),
            scrollToReceipts = true
        )
    }

    // ==================== Helper: Static Screenshot Capture ====================

    /**
     * Launches an ActivityScenario, inflates CometChatMessageInformation,
     * configures it via the provided block, and captures a screenshot.
     *
     * Lets the Activity handle layout naturally — simplified approach
     * matching real-world usage.
     */
    private fun launchAndCapture(
        configure: (ComponentActivity) -> CometChatMessageInformation
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)

            // Set content view just like real usage — Activity handles layout naturally
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            ShadowLooper.idleMainLooper()
        }

        // Let the looper process all pending messages (layout, draw, ViewModel emissions)
        ShadowLooper.idleMainLooper()
        ShadowLooper.idleMainLooper()

        // Capture from the decor view to include all drawn decorations
        scenario.onActivity { activity ->
            fixAvatarCircularRendering(activity.window.decorView)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Scroll Capture ====================

    /**
     * Launches the activity, populates CometChatMessageInformation with receipts,
     * scrolls the NestedScrollView to the desired position, and captures.
     *
     * Lets the Activity handle layout naturally — scroll is performed after
     * the view is fully laid out.
     */
    private fun launchAndCaptureWithScroll(
        receipts: List<MessageReceipt>,
        scrollToBottom: Boolean = false,
        scrollToMiddle: Boolean = false,
        scrollToReceipts: Boolean = false
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = CometChatMessageInformation(activity)
            val vm = createViewModel(receipts)
            view.setViewModel(vm)
            view.setMessage(createGroupMessage())

            // Set content view just like real usage — Activity handles layout naturally
            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            ShadowLooper.idleMainLooper()
        }

        // Let the looper process layout and ViewModel emissions
        ShadowLooper.idleMainLooper()

        // Additional idle cycles to ensure AsyncListDiffer completes diff computation
        // and RecyclerView binds all items before scrolling
        ShadowLooper.idleMainLooper()
        ShadowLooper.idleMainLooper()

        // Perform scroll after layout is complete
        scenario.onActivity { activity ->
            val contentView = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
            val nestedScrollView = findNestedScrollView(contentView)
            if (nestedScrollView != null) {
                val childHeight = nestedScrollView.getChildAt(0)?.measuredHeight ?: 0
                when {
                    scrollToBottom -> nestedScrollView.scrollTo(0, childHeight)
                    scrollToMiddle -> nestedScrollView.scrollTo(0, childHeight / 2)
                    scrollToReceipts -> {
                        // Scroll just past the message bubble to show receipt items
                        val recyclerView = findRecyclerView(contentView)
                        if (recyclerView != null) {
                            // Scroll to the RecyclerView's top position
                            val scrollY = (recyclerView.top - 100).coerceAtLeast(0)
                            nestedScrollView.scrollTo(0, scrollY)
                        } else {
                            // Fallback: scroll to 1/3 of content
                            nestedScrollView.scrollTo(0, childHeight / 3)
                        }
                    }
                }
            }
        }

        ShadowLooper.idleMainLooper()

        // Capture from the decor view
        scenario.onActivity { activity ->
            fixAvatarCircularRendering(activity.window.decorView)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Scroll + Config Capture ====================

    /**
     * Launches the activity, populates CometChatMessageInformation with receipts,
     * applies custom configuration (style, toolbar, etc.), scrolls to show receipts,
     * and captures.
     */
    private fun launchAndCaptureWithScrollAndConfig(
        receipts: List<MessageReceipt>,
        scrollToReceipts: Boolean = false,
        configure: (CometChatMessageInformation) -> Unit
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = CometChatMessageInformation(activity)
            val vm = createViewModel(receipts)
            view.setViewModel(vm)
            view.setMessage(createGroupMessage())
            configure(view)

            activity.setContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            ShadowLooper.idleMainLooper()
        }

        // Let the looper process layout and ViewModel emissions
        ShadowLooper.idleMainLooper()
        ShadowLooper.idleMainLooper()
        ShadowLooper.idleMainLooper()

        // Perform scroll after layout is complete
        if (scrollToReceipts) {
            scenario.onActivity { activity ->
                val contentView = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                val nestedScrollView = findNestedScrollView(contentView)
                if (nestedScrollView != null) {
                    val recyclerView = findRecyclerView(contentView)
                    if (recyclerView != null) {
                        val scrollY = (recyclerView.top - 100).coerceAtLeast(0)
                        nestedScrollView.scrollTo(0, scrollY)
                    } else {
                        val childHeight = nestedScrollView.getChildAt(0)?.measuredHeight ?: 0
                        nestedScrollView.scrollTo(0, childHeight / 3)
                    }
                }
            }
            ShadowLooper.idleMainLooper()
        }

        // Capture from the decor view
        scenario.onActivity { activity ->
            fixAvatarCircularRendering(activity.window.decorView)
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    private fun findNestedScrollView(view: View): NestedScrollView? {
        if (view is NestedScrollView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val result = findNestedScrollView(view.getChildAt(i))
                if (result != null) return result
            }
        }
        return null
    }

    private fun findRecyclerView(view: View): androidx.recyclerview.widget.RecyclerView? {
        if (view is androidx.recyclerview.widget.RecyclerView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val result = findRecyclerView(view.getChildAt(i))
                if (result != null) return result
            }
        }
        return null
    }

    // ==================== Avatar Fix ====================

    /**
     * Fixes avatar circular rendering for Robolectric screenshot tests.
     * In Robolectric's native graphics mode, CometChatAvatar has its outer radius
     * set to 0 by Utils.initMaterialCard(). This method traverses the view hierarchy
     * and sets radius to Float.MAX_VALUE on CometChatAvatar instances.
     */
    private fun fixAvatarCircularRendering(root: View) {
        if (root is CometChatAvatar) {
            root.radius = Float.MAX_VALUE
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                fixAvatarCircularRendering(root.getChildAt(i))
            }
        }
    }

    // ==================== Data Factories ====================

    private fun createSender(): User {
        return User().apply {
            uid = "logged_in_user"
            name = "Logged In User"
            avatar = "$AVATAR_BASE_URL/loggedinuser.png"
            status = CometChatConstants.USER_STATUS_ONLINE
        }
    }

    private fun createGroupMessage(): BaseMessage {
        return TextMessage(
            "group_1",
            "Hello everyone! How is the project going?",
            CometChatConstants.RECEIVER_TYPE_GROUP
        ).apply {
            id = 1001
            sender = createSender()
            deliveredAt = FIXED_DELIVERED_AT
            readAt = FIXED_READ_AT
        }
    }

    private fun createReceiptUsers(count: Int): List<MessageReceipt> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor",
            "Hulk", "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man",
            "Scarlet Witch", "Vision", "Falcon", "War Machine", "Loki",
            "Gamora", "Star-Lord", "Groot", "Rocket", "Drax"
        )

        return (0 until count.coerceAtMost(names.size)).map { i ->
            val user = User().apply {
                uid = "user_$i"
                name = names[i]
                avatar = "$AVATAR_BASE_URL/${names[i].lowercase().replace(" ", "")}.png"
                status = if (i % 2 == 0) CometChatConstants.USER_STATUS_ONLINE else CometChatConstants.USER_STATUS_OFFLINE
            }
            MessageReceipt().apply {
                messageId = 1001
                sender = user
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                deliveredAt = FIXED_DELIVERED_AT + (60L * (i + 1))
                readAt = FIXED_READ_AT + (120L * (i + 1))
            }
        }
    }

    private fun createReceiptsWithStatus(
        count: Int,
        readOnly: Boolean = false,
        deliveredOnly: Boolean = false
    ): List<MessageReceipt> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor",
            "Hulk", "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man"
        )

        return (0 until count.coerceAtMost(names.size)).map { i ->
            val user = User().apply {
                uid = "user_$i"
                name = names[i]
                avatar = "$AVATAR_BASE_URL/${names[i].lowercase().replace(" ", "")}.png"
                status = if (i % 2 == 0) CometChatConstants.USER_STATUS_ONLINE else CometChatConstants.USER_STATUS_OFFLINE
            }
            MessageReceipt().apply {
                messageId = 1001
                sender = user
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                deliveredAt = if (deliveredOnly || !readOnly) FIXED_DELIVERED_AT + (60L * (i + 1)) else 0L
                readAt = if (readOnly || !deliveredOnly) FIXED_READ_AT + (120L * (i + 1)) else 0L
            }
        }
    }

    private fun createMixedReceipts(count: Int): List<MessageReceipt> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor",
            "Hulk", "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man"
        )

        return (0 until count.coerceAtMost(names.size)).map { i ->
            val user = User().apply {
                uid = "user_$i"
                name = names[i]
                avatar = "$AVATAR_BASE_URL/${names[i].lowercase().replace(" ", "")}.png"
                status = if (i % 2 == 0) CometChatConstants.USER_STATUS_ONLINE else CometChatConstants.USER_STATUS_OFFLINE
            }
            MessageReceipt().apply {
                messageId = 1001
                sender = user
                receiverType = CometChatConstants.RECEIVER_TYPE_GROUP
                deliveredAt = FIXED_DELIVERED_AT + (60L * (i + 1))
                // Only even-indexed users have read the message
                readAt = if (i % 2 == 0) FIXED_READ_AT + (120L * (i + 1)) else 0L
            }
        }
    }

    // ==================== ViewModel Factories ====================

    private fun createViewModel(receipts: List<MessageReceipt>): CometChatMessageInformationViewModel {
        val repository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return Result.success(receipts)
            }

            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return MessageReceipt().apply {
                    messageId = message.id
                    sender = message.sender as? User
                    deliveredAt = message.deliveredAt
                    readAt = message.readAt
                }
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

    private fun createErrorViewModel(code: String, message: String): CometChatMessageInformationViewModel {
        val repository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                return Result.failure(CometChatException(code, message))
            }

            override fun createReceiptFromMessage(msg: BaseMessage): MessageReceipt {
                return MessageReceipt()
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

    private fun createLoadingViewModel(): CometChatMessageInformationViewModel {
        val repository = object : MessageInformationRepository {
            override suspend fun fetchReceipts(messageId: Long): Result<List<MessageReceipt>> {
                kotlinx.coroutines.awaitCancellation()
            }

            override fun createReceiptFromMessage(message: BaseMessage): MessageReceipt {
                return MessageReceipt()
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
}
