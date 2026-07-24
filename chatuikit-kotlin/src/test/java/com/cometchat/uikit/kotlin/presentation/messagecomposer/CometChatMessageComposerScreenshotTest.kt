package com.cometchat.uikit.kotlin.presentation.messagecomposer

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.MessageComposerRepository
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.kotlin.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.kotlin.presentation.messagecomposer.utils.MessageComposerViewHolderListener
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
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
 * Roborazzi screenshot tests for CometChatMessageComposer.
 *
 * Captures golden images for ALL visual states of the CometChatMessageComposer component
 * using Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * This file covers:
 *   - Section 1: UI States (idle, replying, editing, AI generating)
 *   - Section 6: Visibility Toggles (attachment, voice recording, AI, sticker buttons)
 *   - Section 7: Custom Views (loading, empty, error, header, footer, auxiliary button)
 *   - Section 8: Style & Theming (custom background, custom colors)
 *   - Section 9: Content Variants (text entered, long text, emoji text)
 *   - Section 10: Dark Theme (content dark, editing dark)
 *
 * Sections 2–5 are omitted because MessageComposer does not support:
 *   - Popup menus (Section 2)
 *   - Selection mode (Section 3)
 *   - Scroll states (Section 4)
 *   - Toolbar interactions (Section 5)
 *
 * Each test method produces one golden PNG in src/test/snapshots/messagecomposer/.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatMessageComposerScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatMessageComposerScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatMessageComposerScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/messagecomposer"
        )
    )

    // ==================== Constants ====================

    private companion object {
        private const val FIXED_TIMESTAMP = 1735689600L // Jan 1, 2025 UTC
    }

    // ==================== Setup & Teardown ====================

    private lateinit var cometChatMock: org.mockito.MockedStatic<com.cometchat.chat.core.CometChat>

    @Before
    fun setup() {
        cometChatMock = org.mockito.Mockito.mockStatic(com.cometchat.chat.core.CometChat::class.java)
        val mockLoggedInUser = mock<User>()
        whenever(mockLoggedInUser.uid).thenReturn("logged-in-user")
        whenever(mockLoggedInUser.name).thenReturn("Logged In User")
        cometChatMock.`when`<User?> { com.cometchat.chat.core.CometChat.getLoggedInUser() }
            .thenReturn(mockLoggedInUser)
    }

    @After
    fun tearDown() {
        cometChatMock.close()
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateIdle() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
            }
        }
    }

    @Test
    fun stateReplying() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                val vm = createViewModel()
                setViewModel(vm)
                setReplyMessage(createReplyMessage())
            }
        }
    }

    @Test
    fun stateEditing() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                val vm = createViewModel()
                setViewModel(vm)
                val editMsg = TextMessage(
                    "user-1",
                    CometChatConstants.RECEIVER_TYPE_USER,
                    "This is a message being edited"
                ).apply {
                    id = 1001
                    sender = createTargetUser()
                    sentAt = FIXED_TIMESTAMP
                }
                setEditMessage(editMsg)
            }
        }
    }

    @Test
    fun stateAIGenerating() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                val vm = createViewModel()
                vm.setAIGenerating(true)
                setViewModel(vm)
            }
        }
    }

    // ==================== Section 6: Visibility Toggles ====================

    @Test
    fun visibilityNoAttachmentButton() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setHideAttachmentButton(true)
            }
        }
    }

    @Test
    fun visibilityNoVoiceRecordingButton() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setHideVoiceRecordingButton(true)
            }
        }
    }

    @Test
    fun visibilityNoAIButton() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setHideAIButton(true)
            }
        }
    }

    @Test
    fun visibilityNoStickerButton() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setHideStickerButton(true)
            }
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                val vm = createViewModel()
                vm.setAIGenerating(true)
                setViewModel(vm)
                setHeaderViewListener(object : MessageComposerViewHolderListener {
                    override fun createView(context: Context, user: User?, group: com.cometchat.chat.models.Group?): View {
                        return TextView(context).apply {
                            text = "Custom Loading..."
                            textSize = 16f
                            setTextColor(Color.DKGRAY)
                            setPadding(32, 16, 32, 16)
                        }
                    }
                })
            }
        }
    }

    @Test
    fun customEmptyView() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setFooterViewListener(object : MessageComposerViewHolderListener {
                    override fun createView(context: Context, user: User?, group: com.cometchat.chat.models.Group?): View {
                        return TextView(context).apply {
                            text = "No messages yet.\nStart a conversation!"
                            textSize = 14f
                            setTextColor(Color.GRAY)
                            setPadding(32, 16, 32, 16)
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                        }
                    }
                })
            }
        }
    }

    @Test
    fun customErrorView() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setFooterViewListener(object : MessageComposerViewHolderListener {
                    override fun createView(context: Context, user: User?, group: com.cometchat.chat.models.Group?): View {
                        return TextView(context).apply {
                            text = "⚠️ Error: Unable to send message.\nPlease try again."
                            textSize = 14f
                            setTextColor(Color.RED)
                            setPadding(32, 16, 32, 16)
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                        }
                    }
                })
            }
        }
    }

    @Test
    fun customHeaderView() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setHeaderViewListener(object : MessageComposerViewHolderListener {
                    override fun createView(context: Context, user: User?, group: com.cometchat.chat.models.Group?): View {
                        return TextView(context).apply {
                            text = "📝 Custom Header View"
                            textSize = 14f
                            setTextColor(Color.parseColor("#6851D6"))
                            setPadding(32, 16, 32, 16)
                            setBackgroundColor(Color.parseColor("#F0ECFF"))
                        }
                    }
                })
            }
        }
    }

    @Test
    fun customFooterView() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setFooterViewListener(object : MessageComposerViewHolderListener {
                    override fun createView(context: Context, user: User?, group: com.cometchat.chat.models.Group?): View {
                        return TextView(context).apply {
                            text = "🔒 End-to-end encrypted"
                            textSize = 12f
                            setTextColor(Color.parseColor("#888888"))
                            setPadding(32, 8, 32, 8)
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                        }
                    }
                })
            }
        }
    }

    @Test
    fun customAuxiliaryButtonView() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setAuxiliaryButtonViewListener(object : MessageComposerViewHolderListener {
                    override fun createView(context: Context, user: User?, group: com.cometchat.chat.models.Group?): View {
                        return TextView(context).apply {
                            text = "📎"
                            textSize = 20f
                            setPadding(16, 8, 16, 8)
                            setBackgroundColor(Color.parseColor("#E8E8E8"))
                        }
                    }
                })
            }
        }
    }

    // ==================== Section 8: Style & Theming ====================

    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                val customStyle = CometChatMessageComposerStyle(
                    backgroundColor = Color.parseColor("#F5F5DC")
                )
                setStyle(customStyle)
            }
        }
    }

    @Test
    fun stylingCustomColors() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                val customStyle = CometChatMessageComposerStyle(
                    backgroundColor = Color.parseColor("#1A1A2E"),
                    composeBoxBackgroundColor = Color.parseColor("#16213E"),
                    composeBoxStrokeColor = Color.parseColor("#0F3460"),
                    inputTextColor = Color.WHITE,
                    inputPlaceholderColor = Color.parseColor("#AAAAAA"),
                    separatorColor = Color.parseColor("#333333")
                )
                setStyle(customStyle)
            }
        }
    }

    // ==================== Section 9: Content Variants ====================

    @Test
    fun contentTextEntered() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setText("Hello, how are you doing today?")
            }
        }
    }

    @Test
    fun contentLongText() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setText(
                    "This is a very long message that spans multiple lines.\n" +
                        "It demonstrates how the composer handles multiline text input.\n" +
                        "The composer should expand vertically to accommodate the text.\n" +
                        "This helps verify the layout behavior with longer content."
                )
            }
        }
    }

    @Test
    fun contentEmojiText() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setText("Hello! 👋🎉🚀 How are you? 😊❤️🌟")
            }
        }
    }

    // ==================== Section 10: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                setViewModel(createViewModel())
                setText("Hello from dark mode!")
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEditingDark() {
        launchAndCapture { activity ->
            CometChatMessageComposer(activity).apply {
                setUser(createTargetUser())
                val vm = createViewModel()
                setViewModel(vm)
                val editMsg = TextMessage(
                    "user-1",
                    CometChatConstants.RECEIVER_TYPE_USER,
                    "Editing this message in dark mode"
                ).apply {
                    id = 1002
                    sender = createTargetUser()
                    sentAt = FIXED_TIMESTAMP
                }
                setEditMessage(editMsg)
            }
        }
    }

    // ==================== Helper: Static Screenshot Capture ====================

    private fun launchAndCapture(
        configure: (ComponentActivity) -> CometChatMessageComposer
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)

            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            container.addView(
                view,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
                )
            )
            activity.setContentView(container)

            ShadowLooper.idleMainLooper()

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            ShadowLooper.idleMainLooper()

            view.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Data Factories ====================

    private fun createTargetUser(): User {
        return User().apply {
            uid = "user-1"
            name = "John Doe"
            avatar = "https://data-us.cometchat.io/assets/images/avatars/johndoe.png"
            status = CometChatConstants.USER_STATUS_ONLINE
        }
    }

    private fun createReplyMessage(): BaseMessage {
        val sender = User().apply {
            uid = "user-1"
            name = "John Doe"
            avatar = "https://data-us.cometchat.io/assets/images/avatars/johndoe.png"
            status = CometChatConstants.USER_STATUS_ONLINE
        }
        return TextMessage(
            "logged-in-user",
            CometChatConstants.RECEIVER_TYPE_USER,
            "Hey, did you see the latest update?"
        ).apply {
            id = 2001
            this.sender = sender
            sentAt = FIXED_TIMESTAMP - 300L
        }
    }

    // ==================== ViewModel Factory ====================

    private fun createViewModel(): CometChatMessageComposerViewModel {
        val repository = object : MessageComposerRepository {
            override suspend fun sendTextMessage(message: TextMessage): Result<TextMessage> {
                return Result.success(message)
            }

            override suspend fun sendMediaMessage(message: MediaMessage): Result<MediaMessage> {
                return Result.success(message)
            }

            override suspend fun sendCustomMessage(message: CustomMessage): Result<CustomMessage> {
                return Result.success(message)
            }

            override suspend fun editMessage(message: BaseMessage): Result<BaseMessage> {
                return Result.success(message)
            }
        }
        return CometChatMessageComposerViewModel(
            sendTextMessageUseCase = SendTextMessageUseCase(repository),
            sendMediaMessageUseCase = SendMediaMessageUseCase(repository),
            sendCustomMessageUseCase = SendCustomMessageUseCase(repository),
            editMessageUseCase = EditMessageUseCase(repository),
            enableListeners = false
        )
    }
}
