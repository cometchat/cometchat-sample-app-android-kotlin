package com.cometchat.uikit.compose.screenshots

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import coil.decode.DataSource
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.threadheader.style.CometChatThreadHeaderStyle
import com.cometchat.uikit.compose.presentation.threadheader.ui.CometChatThreadHeader
import com.cometchat.uikit.compose.presentation.threadheader.viewmodel.ThreadHeaderViewModel
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.constants.UIKitConstants
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
 * Roborazzi screenshot tests for CometChatThreadHeader (chatuikit-compose).
 *
 * Captures golden images for all visual states matching the chatuikit-kotlin tests:
 * - Parent message with reply count
 * - No reply count
 * - Dark theme variant
 * - Custom background style
 * - Visibility toggles (no reactions, no avatar, no reply count bar)
 * - Alignment variants (left-aligned, standard)
 * - Reply count variants (single, large)
 * - Custom reply count view
 * - Max height constrained
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatThreadHeaderScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatThreadHeaderScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/threadheader"
        )
    )

    private lateinit var cometChatMock: MockedStatic<com.cometchat.chat.core.CometChat>

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                return SuccessResult(
                    drawable = ColorDrawable(android.graphics.Color.TRANSPARENT),
                    request = chain.request,
                    dataSource = DataSource.MEMORY_CACHE
                )
            }
        }
        val imageLoader = ImageLoader.Builder(context)
            .components { add(interceptor) }
            .crossfade(false)
            .build()
        Coil.setImageLoader(imageLoader)

        // Mock CometChat.getLoggedInUser() to avoid CometChat.init() requirement
        cometChatMock = Mockito.mockStatic(com.cometchat.chat.core.CometChat::class.java)
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

    // ==================== Helpers ====================

    private fun createViewModel(): ThreadHeaderViewModel {
        return ThreadHeaderViewModel(enableListeners = false)
    }

    private fun createParentMessage(
        id: Long = 100L,
        senderUid: String = "user-1",
        senderName: String = "Alice Smith",
        replyCount: Int = 0,
        text: String = "This is the parent message that started the thread conversation."
    ): BaseMessage {
        val sender = mock<User>()
        whenever(sender.uid).thenReturn(senderUid)
        whenever(sender.name).thenReturn(senderName)
        val message = mock<TextMessage>()
        whenever(message.id).thenReturn(id)
        whenever(message.text).thenReturn(text)
        whenever(message.sender).thenReturn(sender)
        whenever(message.type).thenReturn("text")
        whenever(message.category).thenReturn("message")
        whenever(message.sentAt).thenReturn(1735689600L)
        whenever(message.readAt).thenReturn(0L)
        whenever(message.deliveredAt).thenReturn(0L)
        whenever(message.deletedAt).thenReturn(0L)
        whenever(message.editedAt).thenReturn(0L)
        whenever(message.parentMessageId).thenReturn(0L)
        whenever(message.replyCount).thenReturn(replyCount)
        return message
    }

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

    // ==================== UI States ====================

    @Test
    fun stateWithMessage() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun stateWithReplyCount() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 12)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun stateNoReplyCount() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 0)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    hideReplyCount = true
                )
            }
        }
    }

    // ==================== Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 3)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }
    }

    // ==================== Style ====================

    @Test
    fun styleCustomBackground() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            val customColors = lightColorScheme(
                primary = Color(0xFF6851D6)
            )
            CometChatTheme(colorScheme = customColors) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }
    }

    // ==================== Theming ====================

    @Test
    fun theme_lightDefault() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun theme_darkDefault() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }
    }

    // ==================== Visibility Toggles ====================

    @Test
    fun visibilityNoReactions() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    hideReactions = true
                )
            }
        }
    }

    @Test
    fun visibilityNoAvatar() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    hideAvatar = true
                )
            }
        }
    }

    @Test
    fun visibilityNoReplyCountBar() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 5)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    hideReplyCountBar = true
                )
            }
        }
    }

    // ==================== Custom Views ====================

    @Test
    fun customReplyCountView() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 7)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    replyCountView = { count ->
                        Text(
                            text = "$count responses in thread",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Blue
                        )
                    }
                )
            }
        }
    }

    // ==================== Alignment ====================

    @Test
    fun alignmentLeftAligned() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 3, senderUid = "logged-in-user")
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    alignment = UIKitConstants.MessageListAlignment.LEFT_ALIGNED
                )
            }
        }
    }

    @Test
    fun alignmentStandard() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(
            replyCount = 3,
            senderUid = "other-user",
            senderName = "Bob Johnson",
            text = "Hey, can we discuss the new feature?"
        )
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    alignment = UIKitConstants.MessageListAlignment.STANDARD
                )
            }
        }
    }

    // ==================== Reply Count Variants ====================

    @Test
    fun replyCountSingle() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 1)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun replyCountLarge() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(replyCount = 999)
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel
                )
            }
        }
    }

    // ==================== Max Height ====================

    @Test
    fun maxHeightConstrained() {
        val viewModel = createViewModel()
        val parentMessage = createParentMessage(
            replyCount = 5,
            text = "This is a very long parent message that should be constrained by max height. " +
                "It contains multiple sentences to ensure the content exceeds the max height limit. " +
                "The thread header should scroll within the constrained height."
        )
        viewModel.setParentMessage(parentMessage)

        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatThreadHeader(
                    modifier = Modifier.fillMaxWidth(),
                    parentMessage = parentMessage,
                    viewModel = viewModel,
                    maxHeight = 200.dp
                )
            }
        }
    }
}
