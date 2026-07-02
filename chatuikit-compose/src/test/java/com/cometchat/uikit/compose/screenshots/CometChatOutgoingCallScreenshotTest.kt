package com.cometchat.uikit.compose.screenshots

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.presentation.outgoingcall.style.CometChatOutgoingCallStyle
import com.cometchat.uikit.compose.presentation.outgoingcall.ui.CometChatOutgoingCall
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
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
 * Roborazzi screenshot tests for CometChatOutgoingCall (chatuikit-compose).
 *
 * Captures golden images for ALL visual states of the CometChatOutgoingCall composable.
 * Uses Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Applicable Sections (OutgoingCall is a call notification component — no list/popup/selection/scroll/toolbar):
 * 1. UI States (Calling audio, Calling video)
 * 2. Custom Views (titleView, subtitleView, avatarView, endCallView)
 * 3. Style (backgroundColor, cornerRadius, strokeWidth, strokeColor, titleTextColor,
 *           subtitleTextColor, endCallIconTint, endCallButtonBackgroundColor)
 * 4. Content Variants (audio call to user, video call to user, long receiver name, short name)
 * 5. Dark Theme (calling audio dark, calling video dark, custom style dark)
 *
 * Validates: Requirements 11.1–11.21, 12.1–12.15
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatOutgoingCallScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatOutgoingCallScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatOutgoingCallScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/outgoingcall"
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
    fun stateCallingAudio() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    fun stateCallingVideo() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    // ==================== Section 2: Custom Views ====================

    @Test
    fun customTitleView() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Thor"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    titleView = { _ ->
                        Text(
                            text = "⚡ Calling Thor ⚡",
                            fontSize = 20.sp,
                            color = androidx.compose.ui.graphics.Color(0xFF1565C0),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                )
            }
        }
    }

    @Test
    fun customSubtitleView() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Hulk"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    subtitleView = { _ ->
                        Text(
                            text = "📹 Video Call Outgoing",
                            fontSize = 14.sp,
                            color = androidx.compose.ui.graphics.Color(0xFF388E3C)
                        )
                    }
                )
            }
        }
    }

    @Test
    fun customAvatarView() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Captain America"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    avatarView = { _ ->
                        Text(
                            text = "🛡️",
                            fontSize = 72.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                )
            }
        }
    }

    @Test
    fun customEndCallView() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Hawkeye"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    endCallView = { _ ->
                        Text(
                            text = "🏹 End Call",
                            fontSize = 18.sp,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(horizontal = 48.dp, vertical = 24.dp)
                        )
                    }
                )
            }
        }
    }

    // ==================== Section 3: Style ====================

    @Test
    fun styleCustomBackgroundColor() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatOutgoingCallStyle.default(
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFF1A1A2E),
                        titleTextColor = androidx.compose.ui.graphics.Color.White,
                        subtitleTextColor = androidx.compose.ui.graphics.Color(0xFFBBBBBB)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomCornerRadius() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatOutgoingCallStyle.default(
                        cornerRadius = 32.dp
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomStroke() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatOutgoingCallStyle.default(
                        strokeWidth = 4.dp,
                        strokeColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomTitleTextColor() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatOutgoingCallStyle.default(
                        titleTextColor = androidx.compose.ui.graphics.Color(0xFFE91E63)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomSubtitleTextColor() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatOutgoingCallStyle.default(
                        subtitleTextColor = androidx.compose.ui.graphics.Color(0xFF9C27B0)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomEndCallIconTint() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatOutgoingCallStyle.default(
                        endCallIconTint = androidx.compose.ui.graphics.Color(0xFFFF9800)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomEndCallButtonBackgroundColor() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatOutgoingCallStyle.default(
                        endCallButtonBackgroundColor = androidx.compose.ui.graphics.Color(0xFFB71C1C)
                    )
                )
            }
        }
    }

    @Test
    fun styleAllCustomProperties() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatOutgoingCallStyle.default(
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFF1A1A2E),
                        cornerRadius = 24.dp,
                        strokeWidth = 2.dp,
                        strokeColor = androidx.compose.ui.graphics.Color(0xFF4A4A6A),
                        titleTextColor = androidx.compose.ui.graphics.Color.White,
                        subtitleTextColor = androidx.compose.ui.graphics.Color(0xFFAAAACC),
                        endCallIconTint = androidx.compose.ui.graphics.Color(0xFF64B5F6),
                        endCallButtonBackgroundColor = androidx.compose.ui.graphics.Color(0xFFFF1744)
                    )
                )
            }
        }
    }

    // ==================== Section 4: Content Variants ====================

    @Test
    fun contentAudioCallToUser() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Tony Stark"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    fun contentVideoCallToUser() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Natasha Romanoff"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    fun contentLongReceiverName() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall(
                        "session-3",
                        CometChatConstants.CALL_TYPE_AUDIO,
                        "Dr. Stephen Vincent Strange, Sorcerer Supreme of the Multiverse"
                    ),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    fun contentShortReceiverName() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-4", CometChatConstants.CALL_TYPE_VIDEO, "X"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    // ==================== Section 5: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateCallingAudioDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateCallingVideoDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun styleCustomDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatOutgoingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatOutgoingCallStyle.default(
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFF0D1117),
                        cornerRadius = 16.dp,
                        strokeWidth = 1.dp,
                        strokeColor = androidx.compose.ui.graphics.Color(0xFF30363D),
                        titleTextColor = androidx.compose.ui.graphics.Color(0xFFF0F6FC),
                        subtitleTextColor = androidx.compose.ui.graphics.Color(0xFF8B949E),
                        endCallIconTint = androidx.compose.ui.graphics.Color(0xFF58A6FF),
                        endCallButtonBackgroundColor = androidx.compose.ui.graphics.Color(0xFFDA3633)
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
                .findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0)
            composeView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Mock Data Factories ====================

    private fun createMockCall(sessionId: String, type: String, receiverName: String): Call {
        val call = mock<Call>()
        whenever(call.sessionId).thenReturn(sessionId)
        whenever(call.type).thenReturn(type)
        whenever(call.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        val receiver = mock<User>()
        whenever(receiver.uid).thenReturn("receiver-uid")
        whenever(receiver.name).thenReturn(receiverName)
        whenever(receiver.avatar).thenReturn(null)
        whenever(call.receiver).thenReturn(receiver)
        whenever(call.callReceiver).thenReturn(receiver)
        return call
    }
}
