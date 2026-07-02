package com.cometchat.uikit.compose.screenshots

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.callbuttons.style.CallButtonStyle
import com.cometchat.uikit.compose.presentation.callbuttons.style.CometChatCallButtonsStyle
import com.cometchat.uikit.compose.presentation.callbuttons.ui.CometChatCallButtons
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.cometchat.uikit.core.domain.repository.CallButtonsRepository
import com.cometchat.uikit.core.domain.usecase.InitiateUserCallUseCase
import com.cometchat.uikit.core.domain.usecase.StartGroupCallUseCase
import com.cometchat.uikit.core.viewmodel.CometChatCallButtonsViewModel
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
 * Roborazzi screenshot tests for CometChatCallButtons (chatuikit-compose).
 *
 * Captures golden images for ALL visual states of the CometChatCallButtons composable.
 * Uses Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Applicable Sections (CallButtons is a utility component — no list/popup/selection/scroll/toolbar):
 * 1. UI States (Idle with user, Idle with group, with text labels)
 * 2. Visibility (voice only, video only, text labels, icon visibility)
 * 3. Custom Views (button text customization)
 * 4. Style (icon tint, text color, background color, corner radius, stroke, padding)
 * 5. Content Variants (user target, group target, full-featured)
 * 6. Dark Theme
 *
 * Validates: Requirements 19.1–19.11, 32.3, 32.4
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatCallButtonsScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatCallButtonsScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatCallButtonsScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/callbuttons"
        )
    )

    @Before
    fun setupFakeImageLoader() {
        val context = RuntimeEnvironment.getApplication()
        val interceptor = object : Interceptor {
            override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                return SuccessResult(
                    drawable = ColorDrawable(Color.TRANSPARENT),
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
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateIdleDefault() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel()
                )
            }
        }
    }

    @Test
    fun stateIdleWithUser() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man")
                )
            }
        }
    }

    @Test
    fun stateIdleWithGroup() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    group = createMockGroup("group-1", "Avengers")
                )
            }
        }
    }

    @Test
    fun stateWithButtonText() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Voice Call",
                    videoButtonText = "Video Call",
                    buttonTextVisibility = View.VISIBLE
                )
            }
        }
    }

    // ==================== Section 2: Visibility ====================

    @Test
    fun visibilityVoiceCallOnly() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    videoCallButtonVisibility = View.GONE
                )
            }
        }
    }

    @Test
    fun visibilityVideoCallOnly() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceCallButtonVisibility = View.GONE
                )
            }
        }
    }

    @Test
    fun visibilityWithTextLabels() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Voice",
                    videoButtonText = "Video",
                    buttonTextVisibility = View.VISIBLE
                )
            }
        }
    }

    @Test
    fun visibilityIconsHidden() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Voice Call",
                    videoButtonText = "Video Call",
                    buttonTextVisibility = View.VISIBLE,
                    buttonIconVisibility = View.GONE
                )
            }
        }
    }

    @Test
    fun visibilityBothButtonsHidden() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceCallButtonVisibility = View.GONE,
                    videoCallButtonVisibility = View.GONE
                )
            }
        }
    }

    // ==================== Section 3: Custom Views ====================

    @Test
    fun customVoiceButtonText() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Call Iron Man",
                    buttonTextVisibility = View.VISIBLE
                )
            }
        }
    }

    @Test
    fun customVideoButtonText() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    videoButtonText = "FaceTime",
                    buttonTextVisibility = View.VISIBLE
                )
            }
        }
    }

    @Test
    fun customBothButtonTexts() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    group = createMockGroup("group-1", "Avengers"),
                    voiceButtonText = "Audio Conference",
                    videoButtonText = "Video Conference",
                    buttonTextVisibility = View.VISIBLE
                )
            }
        }
    }

    // ==================== Section 4: Style ====================

    @Test
    fun styleCustomIconTint() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    style = CometChatCallButtonsStyle.default(
                        voiceCallIconTint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        videoCallIconTint = androidx.compose.ui.graphics.Color(0xFF2196F3)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomTextColors() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Voice",
                    videoButtonText = "Video",
                    buttonTextVisibility = View.VISIBLE,
                    style = CometChatCallButtonsStyle.default(
                        voiceCallTextColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        videoCallTextColor = androidx.compose.ui.graphics.Color(0xFF2196F3)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomBackgroundColors() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    style = CometChatCallButtonsStyle.default(
                        voiceCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                        videoCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomCornerRadius() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    style = CometChatCallButtonsStyle.default(
                        voiceCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                        videoCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD),
                        voiceCallCornerRadius = 32.dp,
                        videoCallCornerRadius = 32.dp
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomStroke() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    style = CometChatCallButtonsStyle.default(
                        voiceCallStrokeWidth = 2.dp,
                        voiceCallStrokeColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        videoCallStrokeWidth = 2.dp,
                        videoCallStrokeColor = androidx.compose.ui.graphics.Color(0xFF2196F3)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomPadding() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    style = CometChatCallButtonsStyle.default(
                        voiceCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                        videoCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD),
                        voiceCallButtonPadding = 24.dp,
                        videoCallButtonPadding = 24.dp
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomMarginBetweenButtons() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    style = CometChatCallButtonsStyle.default(
                        marginBetweenButtons = 48.dp
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomIconSize() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    style = CometChatCallButtonsStyle.default(
                        voiceCallIconSize = 48.dp,
                        videoCallIconSize = 48.dp
                    )
                )
            }
        }
    }

    @Test
    fun styleAllCustomProperties() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Voice",
                    videoButtonText = "Video",
                    buttonTextVisibility = View.VISIBLE,
                    style = CometChatCallButtonsStyle.default(
                        voiceCallIconTint = androidx.compose.ui.graphics.Color.White,
                        videoCallIconTint = androidx.compose.ui.graphics.Color.White,
                        voiceCallTextColor = androidx.compose.ui.graphics.Color.White,
                        videoCallTextColor = androidx.compose.ui.graphics.Color.White,
                        voiceCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        videoCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFF2196F3),
                        voiceCallCornerRadius = 16.dp,
                        videoCallCornerRadius = 16.dp,
                        voiceCallStrokeWidth = 2.dp,
                        videoCallStrokeWidth = 2.dp,
                        voiceCallStrokeColor = androidx.compose.ui.graphics.Color(0xFF388E3C),
                        videoCallStrokeColor = androidx.compose.ui.graphics.Color(0xFF1976D2),
                        voiceCallButtonPadding = 16.dp,
                        videoCallButtonPadding = 16.dp,
                        marginBetweenButtons = 24.dp
                    )
                )
            }
        }
    }

    // ==================== Section 5: Content Variants ====================

    @Test
    fun contentUserTargetWithLabels() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Call Iron Man",
                    videoButtonText = "Video Iron Man",
                    buttonTextVisibility = View.VISIBLE
                )
            }
        }
    }

    @Test
    fun contentGroupTargetWithLabels() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    group = createMockGroup("group-1", "Avengers"),
                    voiceButtonText = "Audio Conference",
                    videoButtonText = "Video Conference",
                    buttonTextVisibility = View.VISIBLE
                )
            }
        }
    }

    @Test
    fun contentMinimalIconsOnly() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man")
                )
            }
        }
    }

    @Test
    fun contentFullFeatured() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Voice Call",
                    videoButtonText = "Video Call",
                    buttonTextVisibility = View.VISIBLE,
                    style = CometChatCallButtonsStyle.default(
                        voiceCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                        videoCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD),
                        voiceCallCornerRadius = 12.dp,
                        videoCallCornerRadius = 12.dp
                    )
                )
            }
        }
    }

    // ==================== Section 6: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateIdleDefaultDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel()
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateWithButtonTextDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Voice Call",
                    videoButtonText = "Video Call",
                    buttonTextVisibility = View.VISIBLE
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateWithBackgroundsDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    style = CometChatCallButtonsStyle.default(
                        voiceCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                        videoCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFF0D47A1)
                    )
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun styleCustomColorsDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatCallButtons(
                    modifier = Modifier.wrapContentSize(),
                    viewModel = createViewModel(),
                    user = createMockUser("user-1", "Iron Man"),
                    voiceButtonText = "Voice",
                    videoButtonText = "Video",
                    buttonTextVisibility = View.VISIBLE,
                    style = CometChatCallButtonsStyle.default(
                        voiceCallIconTint = androidx.compose.ui.graphics.Color(0xFF81C784),
                        videoCallIconTint = androidx.compose.ui.graphics.Color(0xFF64B5F6),
                        voiceCallTextColor = androidx.compose.ui.graphics.Color(0xFF81C784),
                        videoCallTextColor = androidx.compose.ui.graphics.Color(0xFF64B5F6),
                        voiceCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                        videoCallBackgroundColor = androidx.compose.ui.graphics.Color(0xFF0D47A1),
                        voiceCallCornerRadius = 16.dp,
                        videoCallCornerRadius = 16.dp
                    )
                )
            }
        }
    }

    // ==================== Helper: Capture Composable ====================

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

    // ==================== ViewModel Factory ====================

    private fun createViewModel(): CometChatCallButtonsViewModel {
        val repository = object : CallButtonsRepository {
            override suspend fun initiateUserCall(receiverId: String, callType: String): Result<Call> {
                return Result.failure(RuntimeException("Not implemented in screenshot test"))
            }
            override suspend fun startGroupCall(groupId: String, callType: String): Result<CustomMessage> {
                return Result.failure(RuntimeException("Not implemented in screenshot test"))
            }
            override fun hasActiveCall(): Boolean = false
        }
        return CometChatCallButtonsViewModel(
            initiateUserCallUseCase = InitiateUserCallUseCase(repository),
            startGroupCallUseCase = StartGroupCallUseCase(repository),
            enableListeners = false
        )
    }

    // ==================== Mock Data Factories ====================

    private fun createMockUser(uid: String, name: String): User {
        val user = mock<User>()
        whenever(user.uid).thenReturn(uid)
        whenever(user.name).thenReturn(name)
        return user
    }

    private fun createMockGroup(guid: String, name: String): Group {
        val group = mock<Group>()
        whenever(group.guid).thenReturn(guid)
        whenever(group.name).thenReturn(name)
        return group
    }
}
