package com.cometchat.uikit.kotlin.presentation.incomingcall

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
import com.cometchat.uikit.kotlin.presentation.incomingcall.style.CometChatIncomingCallStyle
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
 * Roborazzi screenshot tests for CometChatIncomingCall (chatuikit-kotlin).
 *
 * Captures golden images for ALL visual states of the CometChatIncomingCall component.
 * Uses Robolectric for JVM-based view inflation and Roborazzi for screenshot capture.
 *
 * Applicable Sections (IncomingCall is a call notification component — no list/popup/selection/scroll/toolbar):
 * 1. UI States (Ringing audio, Ringing video)
 * 2. Visibility (Custom views visible/hidden)
 * 3. Custom Views (itemView, leadingView, titleView, subtitleView, trailingView)
 * 4. Style (backgroundColor, cornerRadius, strokeWidth, strokeColor, titleTextColor,
 *           subtitleTextColor, iconTint, acceptButtonBackgroundColor, rejectButtonBackgroundColor,
 *           acceptButtonTextColor, rejectButtonTextColor)
 * 5. Content Variants (audio call from user, video call from user, long caller name)
 * 6. Dark Theme (ringing audio dark, ringing video dark, custom style dark)
 *
 * Validates: Requirements 7a.1–7a.20, 8a.1–8a.19
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatIncomingCallScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatIncomingCallScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatIncomingCallScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/incomingcall"
        )
    )

    // ==================== Section 1: UI States ====================

    @Test
    fun stateRingingAudio() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            view
        }
    }

    @Test
    fun stateRingingVideo() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man")
            view.setCall(call)
            view
        }
    }

    // ==================== Section 2: Visibility ====================

    @Test
    fun visibilityCustomItemViewVisible() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "Custom Item View"
                setTextColor(Color.BLACK)
                setPadding(16, 16, 16, 16)
            }
            view.setItemView(customView)
            view
        }
    }

    @Test
    fun visibilityCustomLeadingViewVisible() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "★"
                setTextColor(Color.parseColor("#FFD700"))
                textSize = 24f
            }
            view.setLeadingView(customView)
            view
        }
    }

    @Test
    fun visibilityCustomTitleViewVisible() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "Custom Title: Iron Man Calling"
                setTextColor(Color.parseColor("#FF5722"))
                textSize = 18f
            }
            view.setTitleView(customView)
            view
        }
    }

    @Test
    fun visibilityCustomSubtitleViewVisible() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "Custom Subtitle: Audio Call"
                setTextColor(Color.parseColor("#9C27B0"))
                textSize = 14f
            }
            view.setSubtitleView(customView)
            view
        }
    }

    @Test
    fun visibilityCustomTrailingViewVisible() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "🎯"
                textSize = 32f
            }
            view.setTrailingView(customView)
            view
        }
    }

    @Test
    fun visibilityAllCustomViewsHidden() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            // Default state — no custom views set, all default views visible
            view
        }
    }

    // ==================== Section 3: Custom Views ====================

    @Test
    fun customItemView() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Thor")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "⚡ Thor is calling via Video ⚡"
                setTextColor(Color.parseColor("#1565C0"))
                textSize = 20f
                setPadding(16, 24, 16, 24)
            }
            view.setItemView(customView)
            view
        }
    }

    @Test
    fun customLeadingView() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Captain America")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "🛡️"
                textSize = 28f
                setPadding(0, 0, 12, 0)
            }
            view.setLeadingView(customView)
            view
        }
    }

    @Test
    fun customTitleView() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Black Widow")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "🕷️ Black Widow"
                setTextColor(Color.parseColor("#D32F2F"))
                textSize = 20f
            }
            view.setTitleView(customView)
            view
        }
    }

    @Test
    fun customSubtitleView() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Hulk")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "📹 Video Call Incoming"
                setTextColor(Color.parseColor("#388E3C"))
                textSize = 14f
            }
            view.setSubtitleView(customView)
            view
        }
    }

    @Test
    fun customTrailingView() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Hawkeye")
            view.setCall(call)
            val customView = TextView(activity).apply {
                text = "🏹"
                textSize = 36f
            }
            view.setTrailingView(customView)
            view
        }
    }

    // ==================== Section 4: Style ====================

    @Test
    fun styleCustomBackgroundColor() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
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
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
                .setCornerRadius(32f)
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomStroke() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
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
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
                .setTitleTextColor(Color.parseColor("#E91E63"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomSubtitleTextColor() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
                .setSubtitleTextColor(Color.parseColor("#9C27B0"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomIconTint() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
                .setIconTint(Color.parseColor("#FF9800"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomAcceptButtonColors() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
                .setAcceptButtonBackgroundColor(Color.parseColor("#1B5E20"))
                .setAcceptButtonTextColor(Color.parseColor("#C8E6C9"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleCustomRejectButtonColors() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
                .setRejectButtonBackgroundColor(Color.parseColor("#B71C1C"))
                .setRejectButtonTextColor(Color.parseColor("#FFCDD2"))
                .build()
            view.setStyle(style)
            view
        }
    }

    @Test
    fun styleAllCustomProperties() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
                .setBackgroundColor(Color.parseColor("#1A1A2E"))
                .setCornerRadius(24f)
                .setStrokeWidth(2)
                .setStrokeColor(Color.parseColor("#4A4A6A"))
                .setTitleTextColor(Color.WHITE)
                .setSubtitleTextColor(Color.parseColor("#AAAACC"))
                .setIconTint(Color.parseColor("#64B5F6"))
                .setAcceptButtonBackgroundColor(Color.parseColor("#00C853"))
                .setAcceptButtonTextColor(Color.WHITE)
                .setRejectButtonBackgroundColor(Color.parseColor("#FF1744"))
                .setRejectButtonTextColor(Color.WHITE)
                .build()
            view.setStyle(style)
            view
        }
    }

    // ==================== Section 5: Content Variants ====================

    @Test
    fun contentAudioCallFromUser() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Tony Stark")
            view.setCall(call)
            view
        }
    }

    @Test
    fun contentVideoCallFromUser() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Natasha Romanoff")
            view.setCall(call)
            view
        }
    }

    @Test
    fun contentLongCallerName() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
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
    fun contentShortCallerName() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-4", CometChatConstants.CALL_TYPE_VIDEO, "X")
            view.setCall(call)
            view
        }
    }

    // ==================== Section 6: Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateRingingAudioDark() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")
            view.setCall(call)
            view
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateRingingVideoDark() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-2", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man")
            view.setCall(call)
            view
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun styleCustomDark() {
        launchAndCapture { activity ->
            val view = createIncomingCallView(activity)
            val call = createMockCall("session-1", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man")
            view.setCall(call)
            val style = CometChatIncomingCallStyle.builder(activity)
                .setBackgroundColor(Color.parseColor("#0D1117"))
                .setCornerRadius(16f)
                .setStrokeWidth(1)
                .setStrokeColor(Color.parseColor("#30363D"))
                .setTitleTextColor(Color.parseColor("#F0F6FC"))
                .setSubtitleTextColor(Color.parseColor("#8B949E"))
                .setIconTint(Color.parseColor("#58A6FF"))
                .setAcceptButtonBackgroundColor(Color.parseColor("#238636"))
                .setAcceptButtonTextColor(Color.WHITE)
                .setRejectButtonBackgroundColor(Color.parseColor("#DA3633"))
                .setRejectButtonTextColor(Color.WHITE)
                .build()
            view.setStyle(style)
            view
        }
    }

    // ==================== Section 7: Interaction Screenshots (Espresso) ====================

    @Test
    fun interactionAcceptButtonClicked() {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = CometChatIncomingCall(activity)
            val call = createMockCall("session-interact-1", CometChatConstants.CALL_TYPE_AUDIO, "Iron Man")

            // Set custom accept callback that shows a feedback TextView
            view.setOnAcceptClickListener {
                val feedbackView = TextView(activity).apply {
                    text = "✅ Call Accepted"
                    setTextColor(Color.WHITE)
                    textSize = 16f
                    setBackgroundColor(Color.parseColor("#4CAF50"))
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

            view.setOnRejectClickListener { /* no-op */ }
            view.setCall(call)

            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            container.addView(view, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
            activity.setContentView(container)

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            // Use Espresso to click the accept button
            onView(withContentDescription("Accept call"))
                .perform(click())

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            // Re-measure after feedback view added
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            // Capture the state after accept click (shows feedback)
            container.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    @Test
    fun interactionDeclineButtonClicked() {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)

            val view = CometChatIncomingCall(activity)
            val call = createMockCall("session-interact-2", CometChatConstants.CALL_TYPE_VIDEO, "Iron Man")

            view.setOnAcceptClickListener { /* no-op */ }

            // Set custom reject callback that shows a feedback TextView
            view.setOnRejectClickListener {
                val feedbackView = TextView(activity).apply {
                    text = "❌ Call Declined"
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
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
            activity.setContentView(container)

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            // Use Espresso to click the decline button
            onView(withContentDescription("Decline call"))
                .perform(click())

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            // Re-measure after feedback view added
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, 2160)

            try { ShadowLooper.idleMainLooper() } catch (_: Exception) { }

            // Capture the state after decline click (shows feedback)
            container.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Helper: Screenshot Capture ====================

    private fun launchAndCapture(configure: (ComponentActivity) -> CometChatIncomingCall) {
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
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
            activity.setContentView(container)

            try {
                ShadowLooper.idleMainLooper()
            } catch (_: Exception) {
                // Swallow any internal ViewModel initialization errors
            }

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.AT_MOST)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, 1080, container.measuredHeight)

            try {
                ShadowLooper.idleMainLooper()
            } catch (_: Exception) { }

            view.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== View Factory ====================

    private fun createIncomingCallView(activity: ComponentActivity): CometChatIncomingCall {
        return CometChatIncomingCall(activity)
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
