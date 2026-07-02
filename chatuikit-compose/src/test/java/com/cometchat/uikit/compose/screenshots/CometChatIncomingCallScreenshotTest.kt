package com.cometchat.uikit.compose.screenshots

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.cometchat.uikit.compose.presentation.incomingcall.style.CometChatIncomingCallStyle
import com.cometchat.uikit.compose.presentation.incomingcall.ui.CometChatIncomingCall
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
 * Roborazzi screenshot tests for CometChatIncomingCall (chatuikit-compose).
 *
 * Captures golden images for ALL visual states of the CometChatIncomingCall composable.
 * Uses Robolectric for JVM-based rendering and Roborazzi for screenshot capture.
 *
 * Applicable Sections (IncomingCall is a call notification component — no list/popup/selection/scroll/toolbar):
 * 1. UI States (Ringing audio, Ringing video)
 * 2. Custom Views (itemView, leadingView, titleView, subtitleView, trailingView)
 * 3. Style (backgroundColor, cornerRadius, strokeWidth, strokeColor, titleTextColor,
 *           subtitleTextColor, iconTint, acceptButtonBackgroundColor, rejectButtonBackgroundColor,
 *           acceptButtonTextColor, rejectButtonTextColor)
 * 4. Content Variants (audio call from user, video call from user, long caller name)
 * 5. Dark Theme (ringing audio dark, ringing video dark, custom style dark)
 *
 * Validates: Requirements 7.1–7.21, 8.1–8.19
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*.CometChatIncomingCallScreenshotTest"
 *   ./gradlew :chatuikit-compose:verifyRoborazziDebug --tests "*.CometChatIncomingCallScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatIncomingCallScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/incomingcall"
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
    fun stateRingingAudio() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    fun stateRingingVideo() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    // ==================== Section 2: Custom Views ====================

    @Test
    fun customItemView() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Thor"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    itemView = { _ ->
                        Text(
                            text = "⚡ Thor is calling via Video ⚡",
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
    fun customLeadingView() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Captain America"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    leadingView = { _ ->
                        Text(
                            text = "🛡️",
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                )
            }
        }
    }

    @Test
    fun customTitleView() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Black Widow"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    titleView = { _ ->
                        Text(
                            text = "🕷️ Black Widow",
                            fontSize = 20.sp,
                            color = androidx.compose.ui.graphics.Color(0xFFD32F2F)
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
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Hulk"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    subtitleView = { _ ->
                        Text(
                            text = "📹 Video Call Incoming",
                            fontSize = 14.sp,
                            color = androidx.compose.ui.graphics.Color(0xFF388E3C)
                        )
                    }
                )
            }
        }
    }

    @Test
    fun customTrailingView() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Hawkeye"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    trailingView = { _ ->
                        Text(
                            text = "🏹",
                            fontSize = 36.sp
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
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
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
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
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
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
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
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
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
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
                        subtitleTextColor = androidx.compose.ui.graphics.Color(0xFF9C27B0)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomIconTint() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
                        iconTint = androidx.compose.ui.graphics.Color(0xFFFF9800)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomAcceptButtonColors() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
                        acceptButtonBackgroundColor = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                        acceptButtonTextColor = androidx.compose.ui.graphics.Color(0xFFC8E6C9)
                    )
                )
            }
        }
    }

    @Test
    fun styleCustomRejectButtonColors() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
                        rejectButtonBackgroundColor = androidx.compose.ui.graphics.Color(0xFFB71C1C),
                        rejectButtonTextColor = androidx.compose.ui.graphics.Color(0xFFFFCDD2)
                    )
                )
            }
        }
    }

    @Test
    fun styleAllCustomProperties() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFF1A1A2E),
                        cornerRadius = 24.dp,
                        strokeWidth = 2.dp,
                        strokeColor = androidx.compose.ui.graphics.Color(0xFF4A4A6A),
                        titleTextColor = androidx.compose.ui.graphics.Color.White,
                        subtitleTextColor = androidx.compose.ui.graphics.Color(0xFFAAAACC),
                        iconTint = androidx.compose.ui.graphics.Color(0xFF64B5F6),
                        acceptButtonBackgroundColor = androidx.compose.ui.graphics.Color(0xFF00C853),
                        acceptButtonTextColor = androidx.compose.ui.graphics.Color.White,
                        rejectButtonBackgroundColor = androidx.compose.ui.graphics.Color(0xFFFF1744),
                        rejectButtonTextColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        }
    }

    // ==================== Section 4: Content Variants ====================

    @Test
    fun contentAudioCallFromUser() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Tony Stark"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    fun contentVideoCallFromUser() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Natasha Romanoff"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    fun contentLongCallerName() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
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
    fun contentShortCallerName() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatIncomingCall(
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
    fun stateRingingAudioDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateRingingVideoDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatIncomingCall(
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
                CometChatIncomingCall(
                    call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man"),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    disableSoundForCalls = true,
                    style = CometChatIncomingCallStyle.default(
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFF0D1117),
                        cornerRadius = 16.dp,
                        strokeWidth = 1.dp,
                        strokeColor = androidx.compose.ui.graphics.Color(0xFF30363D),
                        titleTextColor = androidx.compose.ui.graphics.Color(0xFFF0F6FC),
                        subtitleTextColor = androidx.compose.ui.graphics.Color(0xFF8B949E),
                        iconTint = androidx.compose.ui.graphics.Color(0xFF58A6FF),
                        acceptButtonBackgroundColor = androidx.compose.ui.graphics.Color(0xFF238636),
                        acceptButtonTextColor = androidx.compose.ui.graphics.Color.White,
                        rejectButtonBackgroundColor = androidx.compose.ui.graphics.Color(0xFFDA3633),
                        rejectButtonTextColor = androidx.compose.ui.graphics.Color.White
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

    private fun createMockCall(sessionId: String, type: String, callerName: String): Call {
        val call = mock<Call>()
        whenever(call.sessionId).thenReturn(sessionId)
        whenever(call.type).thenReturn(type)
        val caller = mock<User>()
        whenever(caller.uid).thenReturn("caller-uid")
        whenever(caller.name).thenReturn(callerName)
        whenever(caller.avatar).thenReturn(null)
        whenever(call.callInitiator).thenReturn(caller)
        whenever(call.sender).thenReturn(caller)
        return call
    }
}
