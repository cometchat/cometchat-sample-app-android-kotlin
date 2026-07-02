package com.cometchat.uikit.compose.presentation.messagecomposer

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.messagecomposer.style.CometChatMessageComposerStyle
import com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatMessageComposer
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.core.data.datasource.MessageComposerDataSource
import com.cometchat.uikit.core.data.repository.MessageComposerRepositoryImpl
import com.cometchat.uikit.core.domain.usecase.EditMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendCustomMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendMediaMessageUseCase
import com.cometchat.uikit.core.domain.usecase.SendTextMessageUseCase
import com.cometchat.uikit.core.viewmodel.CometChatMessageComposerViewModel
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi screenshot tests for CometChatMessageComposer (Compose).
 *
 * Captures golden images for ALL visual states of the CometChatMessageComposer composable
 * using Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Covers:
 * - Section 1: UI States (Idle, Replying, Editing, AI Generating)
 * - Section 6: Visibility Toggles (attachment, voice recording, AI, sticker buttons)
 * - Section 7: Custom Views (loading, empty, error, header, footer, auxiliary button)
 * - Section 8: Style & Theming (custom background, custom colors)
 * - Section 9: Content Variants (text entered, long text, emoji text)
 * - Section 10: Dark Theme (content dark, editing dark)
 *
 * MessageComposer omits Sections 2-5 (no popup menu, selection mode, scroll, or toolbar).
 *
 * Each test method produces one golden PNG in src/test/snapshots/messagecomposer/.
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatMessageComposerScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatMessageComposerScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatMessageComposerScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/messagecomposer"
        )
    )

    private companion object {
        private const val AVATAR_BASE_URL = "https://data-us.cometchat.io/assets/images/avatars/"
        // Fixed epoch timestamp (Jan 1, 2025 UTC) — deterministic across runs
        private const val FIXED_TIMESTAMP = 1735689600L
    }

    private lateinit var cometChatMock: MockedStatic<com.cometchat.chat.core.CometChat>

    /**
     * Configure a Coil ImageLoader with an interceptor that returns a transparent
     * drawable for all image requests. This ensures avatars render deterministically
     * in the Robolectric JVM environment where network requests are unavailable.
     *
     * Also mocks CometChat.getLoggedInUser() to return a fixed user.
     */
    @Before
    fun setupFakeImageLoader() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                return SuccessResult(
                    drawable = ColorDrawable(android.graphics.Color.TRANSPARENT),
                    request = chain.request,
                    dataSource = DataSource.MEMORY
                )
            }
        }
        val imageLoader = ImageLoader.Builder(context)
            .components { add(interceptor) }
            .crossfade(false)
            .build()
        Coil.setImageLoader(imageLoader)

        cometChatMock = Mockito.mockStatic(com.cometchat.chat.core.CometChat::class.java)
        val mockLoggedInUser = mock<User>()
        whenever(mockLoggedInUser.uid).thenReturn("logged-in-user")
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
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun stateReplying() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)
        viewModel.setReplyMessage(createReplyMessage())

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun stateEditing() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        val editMessage = mock<TextMessage>()
        whenever(editMessage.id).thenReturn(101)
        whenever(editMessage.text).thenReturn("Original message to edit")
        whenever(editMessage.sentAt).thenReturn(FIXED_TIMESTAMP)
        viewModel.setEditMessage(editMessage)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun stateAIGenerating() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)
        viewModel.setAIGenerating(true)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel
                )
            }
        }
    }

    // ==================== Section 6: Visibility Toggles ====================

    @Test
    fun visibilityNoAttachmentButton() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    hideAttachmentButton = true
                )
            }
        }
    }

    @Test
    fun visibilityNoVoiceRecordingButton() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    hideVoiceRecordingButton = true
                )
            }
        }
    }

    @Test
    fun visibilityNoAIButton() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    hideAuxiliaryButton = true
                )
            }
        }
    }

    @Test
    fun visibilityNoStickerButton() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    hideStickersButton = true
                )
            }
        }
    }

    // ==================== Section 7: Custom Views ====================

    @Test
    fun customLoadingView() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)
        viewModel.setAIGenerating(true)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    headerView = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Custom Loading...", fontSize = 16.sp)
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customEmptyView() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    headerView = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No messages yet!", fontSize = 16.sp)
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customErrorView() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    headerView = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFEBEE))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Custom Error View", fontSize = 16.sp, color = Color.Red)
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customHeaderView() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    headerView = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Custom Header View", fontSize = 14.sp, color = Color(0xFF1565C0))
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customFooterView() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    footerView = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF3E0))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Custom Footer View", fontSize = 14.sp, color = Color(0xFFE65100))
                        }
                    }
                )
            }
        }
    }

    @Test
    fun customAuxiliaryButtonView() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    auxiliaryButtonView = { _, _, _ ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50))
                                .padding(8.dp)
                        ) {
                            Text("AI", color = Color.White, fontSize = 12.sp)
                        }
                    }
                )
            }
        }
    }

    // ==================== Section 8: Style ====================

    @Test
    fun styleCustomBackground() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    style = CometChatMessageComposerStyle.default().copy(
                        backgroundColor = Color(0xFFF3E5F5)
                    )
                )
            }
        }
    }

    @Test
    fun stylingCustomColors() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel,
                    style = CometChatMessageComposerStyle.default().copy(
                        backgroundColor = Color(0xFFFFF8E1),
                        inputTextColor = Color(0xFFE65100),
                        separatorColor = Color(0xFFFF6D00)
                    )
                )
            }
        }
    }

    // ==================== Section 9: Content Variants ====================

    @Test
    fun contentTextEntered() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)
        viewModel.setComposeText("Hello, how are you doing today?")

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun contentLongText() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)
        viewModel.setComposeText(
            "This is a very long message that spans multiple lines to test how the " +
                "message composer handles multiline text input. It should wrap properly " +
                "and expand the input field to accommodate the additional content without " +
                "breaking the layout or overlapping other UI elements."
        )

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun contentEmojiText() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)
        viewModel.setComposeText("Hello! 👋🎉🚀 How are you? 😊❤️🔥")

        captureComposable {
            CometChatTheme {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel
                )
            }
        }
    }

    // ==================== Section 10: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)
        viewModel.setComposeText("Hello in dark mode!")

        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEditingDark() {
        val viewModel = createViewModel()
        val user = createTargetUser()
        viewModel.setUser(user)

        val editMessage = mock<TextMessage>()
        whenever(editMessage.id).thenReturn(101)
        whenever(editMessage.text).thenReturn("Original message to edit")
        whenever(editMessage.sentAt).thenReturn(FIXED_TIMESTAMP)
        viewModel.setEditMessage(editMessage)

        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatMessageComposer(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    user = user,
                    viewModel = viewModel
                )
            }
        }
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
                .findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0)
            composeView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== ViewModel Factory ====================

    /**
     * Creates a CometChatMessageComposerViewModel with a mocked data source.
     * Uses enableListeners = false to prevent SDK listener registration.
     */
    private fun createViewModel(): CometChatMessageComposerViewModel {
        val dataSource = mock<MessageComposerDataSource>()
        val repository = MessageComposerRepositoryImpl(dataSource)
        return CometChatMessageComposerViewModel(
            sendTextMessageUseCase = SendTextMessageUseCase(repository),
            sendMediaMessageUseCase = SendMediaMessageUseCase(repository),
            sendCustomMessageUseCase = SendCustomMessageUseCase(repository),
            editMessageUseCase = EditMessageUseCase(repository),
            enableListeners = false
        )
    }

    // ==================== Deterministic Mock Data ====================

    /**
     * Creates a deterministic target user for the message composer.
     * Uses real SDK User object with fixed properties.
     */
    private fun createTargetUser(): User {
        return User().apply {
            uid = "user_target"
            name = "Iron Man"
            avatar = "${AVATAR_BASE_URL}ironman.png"
            status = CometChatConstants.USER_STATUS_ONLINE
        }
    }

    /**
     * Creates a deterministic reply message for testing the replying state.
     * Uses a mocked TextMessage with fixed properties.
     */
    private fun createReplyMessage(): BaseMessage {
        val sender = User().apply {
            uid = "user_sender"
            name = "Captain America"
            avatar = "${AVATAR_BASE_URL}captainamerica.png"
            status = CometChatConstants.USER_STATUS_ONLINE
        }
        val message = mock<TextMessage>()
        whenever(message.id).thenReturn(202)
        whenever(message.text).thenReturn("Original message text")
        whenever(message.sentAt).thenReturn(FIXED_TIMESTAMP - 300L)
        whenever(message.sender).thenReturn(sender)
        whenever(message.receiverUid).thenReturn("user_target")
        whenever(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
        return message
    }
}
