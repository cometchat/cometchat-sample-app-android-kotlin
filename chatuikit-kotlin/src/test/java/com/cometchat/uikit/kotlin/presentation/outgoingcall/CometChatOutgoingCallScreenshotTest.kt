package com.cometchat.uikit.kotlin.presentation.outgoingcall

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.outgoingcall.style.CometChatOutgoingCallStyle
import com.cometchat.uikit.kotlin.shared.interfaces.Function2
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatOutgoingCall (chatuikit-kotlin).
 *
 * Captures golden images for ALL visual states of the CometChatOutgoingCall component.
 * Uses Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * Applicable Sections (OutgoingCall is a call notification component — no list/popup/selection/scroll/toolbar):
 * 1. UI States (Calling audio, Calling video)
 * 2. Visibility (Custom views visible/hidden)
 * 3. Custom Views (titleView, subtitleView, avatarView, endCallView)
 * 4. Style (backgroundColor, cornerRadius, strokeWidth, strokeColor, titleTextColor,
 *           subtitleTextColor, endCallIconTint, endCallButtonBackgroundColor)
 * 5. Content Variants (audio call to user, video call to user, long receiver name, short name)
 * 6. Dark Theme (calling audio dark, calling video dark, custom style dark)
 * 7. Interaction Screenshots (Espresso) (cancel button clicked with feedback)
 *
 * Validates: Requirements 11a.1–11a.22, 12a.1–12a.15
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatOutgoingCallScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatOutgoingCallScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatOutgoingCallScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/outgoingcall"
        )
    )

    // ==================== Section 1: UI States ====================

    @Test
    fun stateCallingAudio() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            view
        }
    }

    @Test
    fun stateCallingVideo() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man")
            view.setCall(call)
            view
        }
    }

    // ==================== Section 2: Visibility ====================

    @Test
    fun visibilityCustomTitleViewVisible() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            view.setTitleView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "Custom Title: Calling Iron Man"
                    setTextColor(Color.parseColor("#FF5722"))
                    textSize = 18f
                }
            })
            view
        }
    }

    @Test
    fun visibilityCustomSubtitleViewVisible() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            view.setSubtitleView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "Custom Subtitle: Audio Call"
                    setTextColor(Color.parseColor("#9C27B0"))
                    textSize = 14f
                }
            })
            view
        }
    }

    @Test
    fun visibilityCustomAvatarViewVisible() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            view.setAvatarView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "🦸"
                    textSize = 64f
                    gravity = android.view.Gravity.CENTER
                }
            })
            view
        }
    }

    @Test
    fun visibilityCustomEndCallViewVisible() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            view.setEndCallView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "✖ Cancel"
                    setTextColor(Color.WHITE)
                    textSize = 16f
                    setBackgroundColor(Color.parseColor("#F44336"))
                    setPadding(32, 16, 32, 16)
                }
            })
            view
        }
    }

    @Test
    fun visibilityAllDefaultViewsVisible() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            // Default state — no custom views set, all default views visible
            view
        }
    }

    // ==================== Section 3: Custom Views ====================

    @Test
    fun customTitleView() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Thor")
            view.setCall(call)
            view.setTitleView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "⚡ Calling Thor ⚡"
                    setTextColor(Color.parseColor("#1565C0"))
                    textSize = 20f
                    setPadding(16, 24, 16, 24)
                }
            })
            view
        }
    }

    @Test
    fun customSubtitleView() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Hulk")
            view.setCall(call)
            view.setSubtitleView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "📹 Video Call Outgoing"
                    setTextColor(Color.parseColor("#388E3C"))
                    textSize = 14f
                }
            })
            view
        }
    }

    @Test
    fun customAvatarView() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Captain America")
            view.setCall(call)
            view.setAvatarView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "🛡️"
                    textSize = 72f
                    gravity = android.view.Gravity.CENTER
                    setPadding(0, 24, 0, 24)
                }
            })
            view
        }
    }

    @Test
    fun customEndCallView() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Hawkeye")
            view.setCall(call)
            view.setEndCallView(Function2 { ctx, _ ->
                TextView(ctx).apply {
                    text = "🏹 End Call"
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    setBackgroundColor(Color.parseColor("#D32F2F"))
                    setPadding(48, 24, 48, 24)
                }
            })
            view
        }
    }

    // ==================== Section 4: Style ====================

    @Test
    fun styleCustomBackgroundColor() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatOutgoingCallStyle.builder(activity)
                .setBackgroundColor(Color.parseColor("#1A1A2E"))
                .setTitleTextColor(Color.WHITE)
                .setSubtitleTextColor(Color.parseColor("#BBBBBB"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomCornerRadius() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatOutgoingCallStyle.builder(activity)
                .setCornerRadius(32f)
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomStroke() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatOutgoingCallStyle.builder(activity)
                .setStrokeWidth(4)
                .setStrokeColor(Color.parseColor("#4CAF50"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomTitleTextColor() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatOutgoingCallStyle.builder(activity)
                .setTitleTextColor(Color.parseColor("#E91E63"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomSubtitleTextColor() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatOutgoingCallStyle.builder(activity)
                .setSubtitleTextColor(Color.parseColor("#9C27B0"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomEndCallIconTint() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatOutgoingCallStyle.builder(activity)
                .setEndCallIconTint(Color.parseColor("#FF9800"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomEndCallButtonBackgroundColor() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatOutgoingCallStyle.builder(activity)
                .setEndCallButtonBackgroundColor(Color.parseColor("#B71C1C"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleAllCustomProperties() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man")
            view.setCall(call)
            val style = CometChatOutgoingCallStyle.builder(activity)
                .setBackgroundColor(Color.parseColor("#1A1A2E"))
                .setCornerRadius(24f)
                .setStrokeWidth(2)
                .setStrokeColor(Color.parseColor("#4A4A6A"))
                .setTitleTextColor(Color.WHITE)
                .setSubtitleTextColor(Color.parseColor("#AAAACC"))
                .setEndCallIconTint(Color.parseColor("#64B5F6"))
                .setEndCallButtonBackgroundColor(Color.parseColor("#FF1744"))
                .build()
            view.setStyle(style)
            view
        }
    }

    // ==================== Section 5: Content Variants ====================

    @Test
    fun contentAudioCallToUser() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Tony Stark")
            view.setCall(call)
            view
        }
    }

    @Test
    fun contentVideoCallToUser() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Natasha Romanoff")
            view.setCall(call)
            view
        }
    }

    @Test
    fun contentLongReceiverName() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall(
                "session-3",
                CometChatConstants.CALL_TYPE_AUDIO,
                "Dr. Stephen Vincent Strange, Sorcerer Supreme of the Multiverse"
            )
            view.setCall(call)
            view
        }
    }

    @Test
    fun contentShortReceiverName() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-4", CometChatConstants.CALL_TYPE_VIDEO, "X")
            view.setCall(call)
            view
        }
    }

    // ==================== Section 6: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateCallingAudioDark() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            view
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateCallingVideoDark() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man")
            view.setCall(call)
            view
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun styleCustomDark() {
        launchAndCapture { activity ->
            val view = createOutgoingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man")
            view.setCall(call)
            val style = CometChatOutgoingCallStyle.builder(activity)
                .setBackgroundColor(Color.parseColor("#0D1117"))
                .setCornerRadius(16f)
                .setStrokeWidth(1)
                .setStrokeColor(Color.parseColor("#30363D"))
                .setTitleTextColor(Color.parseColor("#F0F6FC"))
                .setSubtitleTextColor(Color.parseColor("#8B949E"))
                .setEndCallIconTint(Color.parseColor("#58A6FF"))
                .setEndCallButtonBackgroundColor(Color.parseColor("#DA3633"))
                .build()
            view.setStyle(style)
            view
        }
    }

    // ==================== Section 7: Interaction Screenshots (Espresso) ====================

    @Test
    fun interactionEndCallButtonClicked() {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = CometChatOutgoingCall(activity)
            val call = createMockCall("session-interact-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")

            // Set custom end call callback that shows a feedback TextView
            view.setOnEndCallClickListener {
                val feedbackView = TextView(activity).apply {
                    text = "❌ Call Cancelled"
                    setTextColor(Color.WHITE)
                    textSize = 16f
                    setBackgroundColor(Color.parseColor("#F44336"))
                    setPadding(32, 16, 32, 16)
                    id = View.generateViewId()
                }
                (view.parent as? FrameLayout)?.addView(feedbackView, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                    bottomMargin = 32
                })
            }

            view.setCall(call)

            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            container.addView(view, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
            activity.setContentView(container)

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            // Use Espresso to click the end call button
            onView(withContentDescription("End call"))
                .perform(click())

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            // Re-measure after feedback view added
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            // Capture the state after end call click (shows feedback)
            container.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Screenshot Capture ====================

    private fun launchAndCapture(configure: (ComponentActivity) -> CometChatOutgoingCall) {
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
            container.addView(view, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
            activity.setContentView(container)

            try {
                ShadowLooper.idleMainLooper()
            } catch (_: Exception) {
                // Swallow any internal ViewModel initialization errors
            }

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            try {
                ShadowLooper.idleMainLooper()
            } catch (_: Exception) { }

            view.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== View Factory ====================

    private fun createOutgoingCallView(activity: ComponentActivity): CometChatOutgoingCall {
        return CometChatOutgoingCall(activity)
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
