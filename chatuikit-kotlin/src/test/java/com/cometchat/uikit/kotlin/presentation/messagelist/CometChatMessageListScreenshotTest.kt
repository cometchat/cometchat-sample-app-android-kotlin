package com.cometchat.uikit.kotlin.presentation.messagelist

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.kotlin.R
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Basic Roborazzi screenshot test for CometChatMessageList bubble rendering (chatuikit-kotlin).
 *
 * Renders each bubble type individually using Android Views so every bubble is fully visible
 * without scrolling. Covers left (incoming), right (outgoing), and center (action/system)
 * alignments for all message types.
 *
 * Same approach as the chatuikit-compose CometChatMessageListScreenshotTest — simulated
 * bubbles with fixed mock data to check visual rendering of all bubble types.
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*.CometChatMessageListScreenshotTest"
 *   ./gradlew :chatuikit-kotlin:verifyRoborazziDebug --tests "*.CometChatMessageListScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatMessageListScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/messagelist"
        )
    )

    // ==================== Colors ====================

    private val BG_COLOR = Color.parseColor("#F5F5F5")
    private val BG_COLOR_DARK = Color.parseColor("#1A1A2E")
    private val INCOMING_BG = Color.WHITE
    private val INCOMING_BG_DARK = Color.parseColor("#2D2D44")
    private val OUTGOING_BG = Color.parseColor("#6851D6")
    private val OUTGOING_BG_DARK = Color.parseColor("#7C6BD6")
    private val AVATAR_BG = Color.parseColor("#6851D6")
    private val TEXT_PRIMARY = Color.parseColor("#1A1A1A")
    private val TEXT_PRIMARY_DARK = Color.parseColor("#EEEEEE")
    private val TEXT_SECONDARY = Color.parseColor("#666666")
    private val TEXT_SECONDARY_DARK = Color.parseColor("#AAAAAA")
    private val TEXT_TERTIARY = Color.parseColor("#999999")
    private val SEPARATOR_BG = Color.parseColor("#E8E8E8")
    private val SEPARATOR_BG_DARK = Color.parseColor("#333344")
    private val ACTION_BG = Color.parseColor("#EEEEEE")
    private val ACTION_BG_DARK = Color.parseColor("#2A2A3E")

    // ==================== Section 1: Text Bubbles ====================

    @Test
    fun textBubble_incomingAndOutgoing() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR) {
                addDateSeparator(activity, "January 1, 2025")
                addIncomingTextBubble(activity, "Alice Smith", "Hey! How are you doing today?", "10:30 AM")
                addOutgoingTextBubble(activity, "I'm doing great, thanks! Working on the new feature.", "10:31 AM")
                addIncomingTextBubble(activity, "Bob Johnson", "That sounds exciting! Let me know if you need any help with the implementation.", "10:32 AM")
                addOutgoingTextBubble(activity, "Sure, let's discuss tomorrow! 👍🎉", "10:33 AM")
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun textBubble_incomingAndOutgoing_dark() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR_DARK) {
                addDateSeparator(activity, "January 1, 2025", dark = true)
                addIncomingTextBubble(activity, "Alice Smith", "Hey! How are you doing today?", "10:30 AM", dark = true)
                addOutgoingTextBubble(activity, "I'm doing great, thanks! Working on the new feature.", "10:31 AM", dark = true)
                addIncomingTextBubble(activity, "Bob Johnson", "That sounds exciting! Let me know if you need any help.", "10:32 AM", dark = true)
                addOutgoingTextBubble(activity, "Sure, let's discuss tomorrow! 👍🎉", "10:33 AM", dark = true)
            }
        }
    }

    // ==================== Section 2: Image Bubbles ====================

    @Test
    fun imageBubble_incomingAndOutgoing() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR) {
                addDateSeparator(activity, "Today")
                addIncomingMediaBubble(activity, "Alice Smith", "🖼️ Image", "10:34 AM", Color.parseColor("#E3F2FD"))
                addOutgoingMediaBubble(activity, "🖼️ Image", "10:35 AM", Color.parseColor("#E8F5E9"))
                addIncomingMediaBubble(activity, "Bob Johnson", "🖼️ Screenshot.png", "10:36 AM", Color.parseColor("#F3E5F5"))
                addOutgoingMediaBubble(activity, "🖼️ Photo_2025.jpg", "10:37 AM", Color.parseColor("#FFF9C4"))
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun imageBubble_incomingAndOutgoing_dark() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR_DARK) {
                addDateSeparator(activity, "Today", dark = true)
                addIncomingMediaBubble(activity, "Alice Smith", "🖼️ Image", "10:34 AM", Color.parseColor("#1A237E"), dark = true)
                addOutgoingMediaBubble(activity, "🖼️ Image", "10:35 AM", Color.parseColor("#1B5E20"), dark = true)
            }
        }
    }

    // ==================== Section 3: Video Bubbles ====================

    @Test
    fun videoBubble_incomingAndOutgoing() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR) {
                addDateSeparator(activity, "Today")
                addIncomingMediaBubble(activity, "Charlie Brown", "🎬 Video (2:15)", "11:00 AM", Color.parseColor("#E0F7FA"))
                addOutgoingMediaBubble(activity, "🎬 Video (0:45)", "11:01 AM", Color.parseColor("#FFF9C4"))
                addIncomingMediaBubble(activity, "Alice Smith", "🎬 Recording.mp4 (5:30)", "11:02 AM", Color.parseColor("#E8EAF6"))
                addOutgoingMediaBubble(activity, "🎬 Screen_record.mp4 (1:20)", "11:03 AM", Color.parseColor("#E0F2F1"))
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun videoBubble_incomingAndOutgoing_dark() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR_DARK) {
                addDateSeparator(activity, "Today", dark = true)
                addIncomingMediaBubble(activity, "Charlie Brown", "🎬 Video (2:15)", "11:00 AM", Color.parseColor("#004D40"), dark = true)
                addOutgoingMediaBubble(activity, "🎬 Video (0:45)", "11:01 AM", Color.parseColor("#1A237E"), dark = true)
            }
        }
    }

    // ==================== Section 4: Audio Bubbles ====================

    @Test
    fun audioBubble_incomingAndOutgoing() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR) {
                addDateSeparator(activity, "Today")
                addIncomingAudioBubble(activity, "Alice Smith", "0:32", "10:38 AM")
                addOutgoingAudioBubble(activity, "1:05", "10:39 AM")
                addIncomingAudioBubble(activity, "Bob Johnson", "2:48", "10:40 AM")
                addOutgoingAudioBubble(activity, "0:15", "10:41 AM")
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun audioBubble_incomingAndOutgoing_dark() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR_DARK) {
                addDateSeparator(activity, "Today", dark = true)
                addIncomingAudioBubble(activity, "Alice Smith", "0:32", "10:38 AM", dark = true)
                addOutgoingAudioBubble(activity, "1:05", "10:39 AM", dark = true)
            }
        }
    }

    // ==================== Section 5: File/Document Bubbles ====================

    @Test
    fun fileBubble_incomingAndOutgoing() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR) {
                addDateSeparator(activity, "Today")
                addIncomingFileBubble(activity, "Bob Johnson", "project_spec.pdf", "2.4 MB", "10:36 AM")
                addOutgoingFileBubble(activity, "design_doc.pdf", "1.8 MB", "10:37 AM")
                addIncomingFileBubble(activity, "Alice Smith", "meeting_notes.docx", "540 KB", "10:42 AM")
                addOutgoingFileBubble(activity, "report_final.xlsx", "3.1 MB", "10:43 AM")
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun fileBubble_incomingAndOutgoing_dark() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR_DARK) {
                addDateSeparator(activity, "Today", dark = true)
                addIncomingFileBubble(activity, "Bob Johnson", "project_spec.pdf", "2.4 MB", "10:36 AM", dark = true)
                addOutgoingFileBubble(activity, "design_doc.pdf", "1.8 MB", "10:37 AM", dark = true)
            }
        }
    }

    // ==================== Section 6: Custom Bubbles ====================

    @Test
    fun customBubble_incomingAndOutgoing() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR) {
                addDateSeparator(activity, "Today")
                addIncomingCustomBubble(activity, "Alice Smith", "📊 Poll: Team Lunch", "Where should we eat today?", "11:00 AM")
                addOutgoingCustomBubble(activity, "📍 Location Shared", "123 Main Street, City Center", "11:01 AM")
                addIncomingCustomBubble(activity, "Bob Johnson", "📅 Meeting Invite", "Sprint Planning - 3:00 PM", "11:05 AM")
                addOutgoingCustomBubble(activity, "🎫 Sticker", "👋 Hello!", "11:06 AM")
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun customBubble_incomingAndOutgoing_dark() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR_DARK) {
                addDateSeparator(activity, "Today", dark = true)
                addIncomingCustomBubble(activity, "Alice Smith", "📊 Poll: Team Lunch", "Where should we eat today?", "11:00 AM", dark = true)
                addOutgoingCustomBubble(activity, "📍 Location Shared", "123 Main Street, City Center", "11:01 AM", dark = true)
            }
        }
    }

    // ==================== Section 7: Action/Center Bubbles ====================

    @Test
    fun actionBubble_centerAligned() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR) {
                addDateSeparator(activity, "January 1, 2025")
                addActionBubble(activity, "Alice Smith joined the group")
                addIncomingTextBubble(activity, "Alice Smith", "Hi everyone!", "10:00 AM")
                addActionBubble(activity, "Bob Johnson was added by Admin")
                addActionBubble(activity, "📞 Voice call ended • 5:32")
                addActionBubble(activity, "📹 Video call ended • 12:05")
                addActionBubble(activity, "Charlie left the group")
                addDateSeparator(activity, "Today")
                addActionBubble(activity, "Group name changed to \"Project Alpha\"")
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun actionBubble_centerAligned_dark() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR_DARK) {
                addDateSeparator(activity, "January 1, 2025", dark = true)
                addActionBubble(activity, "Alice Smith joined the group", dark = true)
                addActionBubble(activity, "📞 Voice call ended • 5:32", dark = true)
                addActionBubble(activity, "📹 Video call ended • 12:05", dark = true)
                addActionBubble(activity, "Charlie left the group", dark = true)
                addDateSeparator(activity, "Today", dark = true)
                addActionBubble(activity, "Group name changed to \"Project Alpha\"", dark = true)
            }
        }
    }

    // ==================== Section 8: Deleted Message Bubbles ====================

    @Test
    fun deletedBubble_incomingAndOutgoing() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR) {
                addDateSeparator(activity, "Today")
                addIncomingTextBubble(activity, "Alice Smith", "Check this out!", "10:30 AM")
                addDeletedBubbleIncoming(activity, "Alice Smith")
                addOutgoingTextBubble(activity, "What was that?", "10:32 AM")
                addDeletedBubbleOutgoing(activity)
                addIncomingTextBubble(activity, "Bob Johnson", "Never mind 😅", "10:33 AM")
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun deletedBubble_incomingAndOutgoing_dark() {
        launchAndCapture { activity ->
            buildMessageListLayout(activity, BG_COLOR_DARK) {
                addDateSeparator(activity, "Today", dark = true)
                addDeletedBubbleIncoming(activity, "Alice Smith", dark = true)
                addDeletedBubbleOutgoing(activity, dark = true)
            }
        }
    }

    // ==================== Helper: Capture ====================

    private fun launchAndCapture(configure: (ComponentActivity) -> View) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            val view = configure(activity)
            activity.setContentView(view)
            ShadowLooper.idleMainLooper()

            val widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, 1080, 2160)
            ShadowLooper.idleMainLooper()

            view.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }

    // ==================== Layout Builder ====================

    private fun buildMessageListLayout(
        activity: ComponentActivity,
        bgColor: Int,
        builder: LinearLayout.() -> Unit
    ): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(bgColor)
            setPadding(0, dp(8), 0, dp(8))
            builder()
        }
    }

    // ==================== Bubble Builders ====================

    private fun LinearLayout.addDateSeparator(activity: ComponentActivity, text: String, dark: Boolean = false) {
        val wrapper = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dp(8), 0, dp(8)) }
        }
        val label = TextView(activity).apply {
            this.text = text
            textSize = 12f
            setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            setPadding(dp(12), dp(4), dp(12), dp(4))
            background = roundedBg(if (dark) SEPARATOR_BG_DARK else SEPARATOR_BG, dp(4))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER }
        }
        wrapper.addView(label)
        addView(wrapper)
    }

    private fun LinearLayout.addActionBubble(activity: ComponentActivity, text: String, dark: Boolean = false) {
        val wrapper = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
        }
        val label = TextView(activity).apply {
            this.text = text
            textSize = 12f
            setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY)
            setPadding(dp(12), dp(6), dp(12), dp(6))
            background = roundedBg(if (dark) ACTION_BG_DARK else ACTION_BG, dp(8))
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER }
        }
        wrapper.addView(label)
        addView(wrapper)
    }

    private fun LinearLayout.addIncomingTextBubble(
        activity: ComponentActivity, senderName: String, text: String, time: String, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.START
        }

        row.addView(createAvatar(activity, senderName))
        row.addView(spacer(activity, dp(8)))

        val col = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(50)
            }
        }
        col.addView(TextView(activity).apply {
            this.text = senderName
            textSize = 11f
            setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY)
            setPadding(dp(12), 0, 0, dp(2))
        })

        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(if (dark) INCOMING_BG_DARK else INCOMING_BG, dp(12), topStart = dp(4))
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        bubble.addView(TextView(activity).apply {
            this.text = text
            textSize = 14f
            setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
        })
        bubble.addView(TextView(activity).apply {
            this.text = time
            textSize = 10f
            setTextColor(TEXT_TERTIARY)
            gravity = Gravity.END
            setPadding(0, dp(4), 0, 0)
        })
        col.addView(bubble)
        row.addView(col)
        addView(row)
    }

    private fun LinearLayout.addOutgoingTextBubble(
        activity: ComponentActivity, text: String, time: String, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.END
        }

        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(if (dark) OUTGOING_BG_DARK else OUTGOING_BG, dp(12), topEnd = dp(4))
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginStart = dp(50) }
        }
        bubble.addView(TextView(activity).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.WHITE)
        })
        bubble.addView(TextView(activity).apply {
            this.text = time
            textSize = 10f
            setTextColor(Color.argb(180, 255, 255, 255))
            gravity = Gravity.END
            setPadding(0, dp(4), 0, 0)
        })
        row.addView(bubble)
        addView(row)
    }

    private fun LinearLayout.addIncomingMediaBubble(
        activity: ComponentActivity, senderName: String, mediaType: String, time: String, mediaColor: Int, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.START
        }
        row.addView(createAvatar(activity, senderName))
        row.addView(spacer(activity, dp(8)))

        val col = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(50) }
        }
        col.addView(TextView(activity).apply {
            this.text = senderName; textSize = 11f
            setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY)
            setPadding(dp(12), 0, 0, dp(2))
        })

        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(if (dark) INCOMING_BG_DARK else INCOMING_BG, dp(12), topStart = dp(4))
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        val mediaBox = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(120))
            background = roundedBg(mediaColor, dp(8))
        }
        mediaBox.addView(TextView(activity).apply {
            this.text = mediaType; textSize = 12f; setTextColor(TEXT_SECONDARY); gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        })
        bubble.addView(mediaBox)
        bubble.addView(TextView(activity).apply {
            this.text = time; textSize = 10f; setTextColor(TEXT_TERTIARY); gravity = Gravity.END
            setPadding(0, dp(4), dp(8), 0)
        })
        col.addView(bubble)
        row.addView(col)
        addView(row)
    }

    private fun LinearLayout.addOutgoingMediaBubble(
        activity: ComponentActivity, mediaType: String, time: String, mediaColor: Int, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.END
        }
        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(Color.argb(25, 104, 81, 214), dp(12), topEnd = dp(4))
            setPadding(dp(4), dp(4), dp(4), dp(4))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(50) }
        }
        val mediaBox = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(dp(200), dp(120))
            background = roundedBg(mediaColor, dp(8))
        }
        mediaBox.addView(TextView(activity).apply {
            this.text = mediaType; textSize = 12f; setTextColor(TEXT_SECONDARY); gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        })
        bubble.addView(mediaBox)
        bubble.addView(TextView(activity).apply {
            this.text = time; textSize = 10f; setTextColor(TEXT_TERTIARY); gravity = Gravity.END
            setPadding(0, dp(4), dp(8), 0)
        })
        row.addView(bubble)
        addView(row)
    }

    private fun LinearLayout.addIncomingAudioBubble(
        activity: ComponentActivity, senderName: String, duration: String, time: String, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.START
        }
        row.addView(createAvatar(activity, senderName))
        row.addView(spacer(activity, dp(8)))

        val col = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(50) }
        }
        col.addView(TextView(activity).apply { this.text = senderName; textSize = 11f; setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY); setPadding(dp(12), 0, 0, dp(2)) })

        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            background = roundedBg(if (dark) INCOMING_BG_DARK else INCOMING_BG, dp(12), topStart = dp(4))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            gravity = Gravity.CENTER_VERTICAL
        }
        // Play button
        val playBtn = TextView(activity).apply {
            this.text = "▶"; textSize = 16f; setTextColor(AVATAR_BG); gravity = Gravity.CENTER
            val size = dp(36)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = roundedBg(Color.argb(50, 104, 81, 214), size / 2)
        }
        bubble.addView(playBtn)
        bubble.addView(spacer(activity, dp(8)))

        val info = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        // Waveform bar
        info.addView(View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(4))
            background = roundedBg(Color.argb(75, 104, 81, 214), dp(2))
        })
        val timeRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(0, dp(4), 0, 0)
        }
        timeRow.addView(TextView(activity).apply { this.text = duration; textSize = 10f; setTextColor(TEXT_TERTIARY); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) })
        timeRow.addView(TextView(activity).apply { this.text = time; textSize = 10f; setTextColor(TEXT_TERTIARY) })
        info.addView(timeRow)
        bubble.addView(info)
        col.addView(bubble)
        row.addView(col)
        addView(row)
    }

    private fun LinearLayout.addOutgoingAudioBubble(
        activity: ComponentActivity, duration: String, time: String, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.END
        }
        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            background = roundedBg(if (dark) OUTGOING_BG_DARK else OUTGOING_BG, dp(12), topEnd = dp(4))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(50) }
        }
        val playBtn = TextView(activity).apply {
            this.text = "▶"; textSize = 16f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            val size = dp(36)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = roundedBg(Color.argb(75, 255, 255, 255), size / 2)
        }
        bubble.addView(playBtn)
        bubble.addView(spacer(activity, dp(8)))

        val info = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(dp(140), LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        info.addView(View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(4))
            background = roundedBg(Color.argb(100, 255, 255, 255), dp(2))
        })
        val timeRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(0, dp(4), 0, 0)
        }
        timeRow.addView(TextView(activity).apply { this.text = duration; textSize = 10f; setTextColor(Color.argb(180, 255, 255, 255)); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) })
        timeRow.addView(TextView(activity).apply { this.text = time; textSize = 10f; setTextColor(Color.argb(180, 255, 255, 255)) })
        info.addView(timeRow)
        bubble.addView(info)
        row.addView(bubble)
        addView(row)
    }

    private fun LinearLayout.addIncomingFileBubble(
        activity: ComponentActivity, senderName: String, fileName: String, fileSize: String, time: String, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.START
        }
        row.addView(createAvatar(activity, senderName))
        row.addView(spacer(activity, dp(8)))

        val col = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(50) }
        }
        col.addView(TextView(activity).apply { this.text = senderName; textSize = 11f; setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY); setPadding(dp(12), 0, 0, dp(2)) })

        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            background = roundedBg(if (dark) INCOMING_BG_DARK else INCOMING_BG, dp(12), topStart = dp(4))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            gravity = Gravity.CENTER_VERTICAL
        }
        val icon = TextView(activity).apply {
            this.text = "📄"; textSize = 20f; gravity = Gravity.CENTER
            val size = dp(40)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = roundedBg(Color.parseColor("#FFF3E0"), dp(8))
        }
        bubble.addView(icon)
        bubble.addView(spacer(activity, dp(10)))
        val info = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        info.addView(TextView(activity).apply { this.text = fileName; textSize = 12f; setTypeface(null, Typeface.BOLD); setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY) })
        info.addView(TextView(activity).apply { this.text = fileSize; textSize = 10f; setTextColor(TEXT_TERTIARY) })
        bubble.addView(info)
        bubble.addView(TextView(activity).apply { this.text = time; textSize = 10f; setTextColor(TEXT_TERTIARY) })
        col.addView(bubble)
        row.addView(col)
        addView(row)
    }

    private fun LinearLayout.addOutgoingFileBubble(
        activity: ComponentActivity, fileName: String, fileSize: String, time: String, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.END
        }
        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            background = roundedBg(if (dark) OUTGOING_BG_DARK else OUTGOING_BG, dp(12), topEnd = dp(4))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(50) }
        }
        val icon = TextView(activity).apply {
            this.text = "📄"; textSize = 20f; gravity = Gravity.CENTER
            val size = dp(40)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = roundedBg(Color.argb(50, 255, 255, 255), dp(8))
        }
        bubble.addView(icon)
        bubble.addView(spacer(activity, dp(10)))
        val info = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        info.addView(TextView(activity).apply { this.text = fileName; textSize = 12f; setTypeface(null, Typeface.BOLD); setTextColor(Color.WHITE) })
        info.addView(TextView(activity).apply { this.text = fileSize; textSize = 10f; setTextColor(Color.argb(180, 255, 255, 255)) })
        bubble.addView(info)
        bubble.addView(TextView(activity).apply { this.text = time; textSize = 10f; setTextColor(Color.argb(180, 255, 255, 255)) })
        row.addView(bubble)
        addView(row)
    }

    private fun LinearLayout.addIncomingCustomBubble(
        activity: ComponentActivity, senderName: String, title: String, subtitle: String, time: String, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.START
        }
        row.addView(createAvatar(activity, senderName))
        row.addView(spacer(activity, dp(8)))

        val col = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(50) }
        }
        col.addView(TextView(activity).apply { this.text = senderName; textSize = 11f; setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY); setPadding(dp(12), 0, 0, dp(2)) })

        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(if (dark) INCOMING_BG_DARK else INCOMING_BG, dp(12), topStart = dp(4))
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        bubble.addView(TextView(activity).apply { this.text = title; textSize = 12f; setTypeface(null, Typeface.BOLD); setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY) })
        bubble.addView(TextView(activity).apply { this.text = subtitle; textSize = 14f; setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY); setPadding(0, dp(4), 0, 0) })
        bubble.addView(TextView(activity).apply { this.text = time; textSize = 10f; setTextColor(TEXT_TERTIARY); gravity = Gravity.END; setPadding(0, dp(4), 0, 0) })
        col.addView(bubble)
        row.addView(col)
        addView(row)
    }

    private fun LinearLayout.addOutgoingCustomBubble(
        activity: ComponentActivity, title: String, subtitle: String, time: String, dark: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.END
        }
        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(if (dark) OUTGOING_BG_DARK else OUTGOING_BG, dp(12), topEnd = dp(4))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(50) }
        }
        bubble.addView(TextView(activity).apply { this.text = title; textSize = 12f; setTypeface(null, Typeface.BOLD); setTextColor(Color.WHITE) })
        bubble.addView(TextView(activity).apply { this.text = subtitle; textSize = 14f; setTextColor(Color.argb(216, 255, 255, 255)); setPadding(0, dp(4), 0, 0) })
        bubble.addView(TextView(activity).apply { this.text = time; textSize = 10f; setTextColor(Color.argb(180, 255, 255, 255)); gravity = Gravity.END; setPadding(0, dp(4), 0, 0) })
        row.addView(bubble)
        addView(row)
    }

    private fun LinearLayout.addDeletedBubbleIncoming(activity: ComponentActivity, senderName: String, dark: Boolean = false) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.START
        }
        row.addView(createAvatar(activity, senderName))
        row.addView(spacer(activity, dp(8)))

        val col = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(50) }
        }
        col.addView(TextView(activity).apply { this.text = senderName; textSize = 11f; setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY); setPadding(dp(12), 0, 0, dp(2)) })

        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(Color.argb(if (dark) 75 else 128, if (dark) 45 else 255, if (dark) 45 else 255, if (dark) 68 else 255), dp(12), topStart = dp(4))
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        bubble.addView(TextView(activity).apply {
            this.text = "⊘ This message was deleted"; textSize = 14f
            setTextColor(TEXT_TERTIARY); setTypeface(null, Typeface.ITALIC)
        })
        col.addView(bubble)
        row.addView(col)
        addView(row)
    }

    private fun LinearLayout.addDeletedBubbleOutgoing(activity: ComponentActivity, dark: Boolean = false) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(16), dp(8), dp(16), dp(8)) }
            gravity = Gravity.END
        }
        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(Color.argb(75, 104, 81, 214), dp(12), topEnd = dp(4))
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(50) }
        }
        bubble.addView(TextView(activity).apply {
            this.text = "⊘ You deleted this message"; textSize = 14f
            setTextColor(Color.argb(180, 255, 255, 255)); setTypeface(null, Typeface.ITALIC)
        })
        row.addView(bubble)
        addView(row)
    }

    // ==================== Utility ====================

    private fun createAvatar(activity: ComponentActivity, name: String): View {
        val size = dp(32)
        return TextView(activity).apply {
            this.text = name.take(1).uppercase()
            textSize = 12f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = roundedBg(AVATAR_BG, size / 2)
        }
    }

    private fun spacer(activity: ComponentActivity, width: Int): View {
        return View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(width, 1)
        }
    }

    private fun dp(value: Int): Int = (value * 2.625f).toInt() // xxhdpi = 3x density approx

    private fun roundedBg(color: Int, radius: Int, topStart: Int = radius, topEnd: Int = radius): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadii = floatArrayOf(
                topStart.toFloat(), topStart.toFloat(),
                topEnd.toFloat(), topEnd.toFloat(),
                radius.toFloat(), radius.toFloat(),
                radius.toFloat(), radius.toFloat()
            )
        }
    }
}
