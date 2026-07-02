package com.cometchat.uikit.kotlin.presentation.threadheader

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
 * Roborazzi screenshot tests for CometChatThreadHeader (chatuikit-kotlin).
 *
 * Uses the simulated-view approach to render the thread header component
 * with mock data, avoiding SDK dependencies that cause RuntimeExceptions.
 *
 * The thread header shows:
 * - Parent message bubble (avatar + sender name + message text + timestamp)
 * - Reaction bar (emoji reactions)
 * - Reply count bar ("X replies")
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*CometChatThreadHeaderScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatThreadHeaderScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/threadheader"
        )
    )

    // ==================== Colors ====================

    private val BG_COLOR = Color.parseColor("#F5F5F5")
    private val BG_COLOR_DARK = Color.parseColor("#1A1A2E")
    private val BUBBLE_BG = Color.WHITE
    private val BUBBLE_BG_DARK = Color.parseColor("#2D2D44")
    private val TEXT_PRIMARY = Color.parseColor("#1A1A1A")
    private val TEXT_PRIMARY_DARK = Color.parseColor("#EEEEEE")
    private val TEXT_SECONDARY = Color.parseColor("#666666")
    private val TEXT_SECONDARY_DARK = Color.parseColor("#AAAAAA")
    private val TEXT_TERTIARY = Color.parseColor("#999999")
    private val AVATAR_BG = Color.parseColor("#6851D6")
    private val REPLY_COUNT_BG = Color.parseColor("#EEEEEE")
    private val REPLY_COUNT_BG_DARK = Color.parseColor("#2A2A3E")
    private val REPLY_COUNT_TEXT = Color.parseColor("#6851D6")
    private val SEPARATOR_COLOR = Color.parseColor("#E0E0E0")
    private val SEPARATOR_COLOR_DARK = Color.parseColor("#333344")
    private val REACTION_BG = Color.parseColor("#F0F0F0")
    private val REACTION_BG_DARK = Color.parseColor("#333344")

    // ==================== Mock Data ====================

    private val parentMessageText = "This is the parent message that started the thread conversation."
    private val senderName = "Alice Smith"
    private val timestamp = "10:30 AM"

    // ==================== UI States ====================

    @Test
    fun stateWithMessage() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, senderName, parentMessageText, timestamp)
                addReactions(activity)
                addReplyCountBar(activity, 5)
            }
        }
    }

    @Test
    fun stateWithReplyCount() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, senderName, parentMessageText, timestamp)
                addReactions(activity)
                addReplyCountBar(activity, 12)
            }
        }
    }

    @Test
    fun stateNoReplyCount() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, senderName, parentMessageText, timestamp)
                addReactions(activity)
                // No reply count bar
            }
        }
    }

    // ==================== Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR_DARK) {
                addParentBubble(activity, senderName, parentMessageText, timestamp, dark = true)
                addReactions(activity, dark = true)
                addReplyCountBar(activity, 3, dark = true)
            }
        }
    }

    // ==================== Style ====================

    @Test
    fun styleCustomBackground() {
        val customBg = Color.parseColor("#F5F5DC")
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, customBg) {
                addParentBubble(activity, senderName, parentMessageText, timestamp)
                addReactions(activity)
                addReplyCountBar(activity, 5)
            }
        }
    }

    // ==================== Visibility Toggles ====================

    @Test
    fun visibilityNoReactions() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, senderName, parentMessageText, timestamp)
                // No reactions
                addReplyCountBar(activity, 5)
            }
        }
    }

    @Test
    fun visibilityNoAvatar() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, senderName, parentMessageText, timestamp, showAvatar = false)
                addReactions(activity)
                addReplyCountBar(activity, 5)
            }
        }
    }

    @Test
    fun visibilityNoReplyCountBar() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, senderName, parentMessageText, timestamp)
                addReactions(activity)
                // No reply count bar
            }
        }
    }

    // ==================== Alignment ====================

    @Test
    fun alignmentLeftAligned() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, senderName, parentMessageText, timestamp, alignment = Gravity.START)
                addReactions(activity)
                addReplyCountBar(activity, 3)
            }
        }
    }

    @Test
    fun alignmentStandard() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, "Bob Johnson", "Hey, can we discuss the new feature?", "11:45 AM", alignment = Gravity.END, isOutgoing = true)
                addReactions(activity)
                addReplyCountBar(activity, 3)
            }
        }
    }

    // ==================== Reply Count Variants ====================

    @Test
    fun replyCountSingle() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, senderName, parentMessageText, timestamp)
                addReactions(activity)
                addReplyCountBar(activity, 1)
            }
        }
    }

    @Test
    fun replyCountLarge() {
        launchAndCapture { activity ->
            buildThreadHeaderLayout(activity, BG_COLOR) {
                addParentBubble(activity, senderName, parentMessageText, timestamp)
                addReactions(activity)
                addReplyCountBar(activity, 999)
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

    private fun buildThreadHeaderLayout(
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
            setPadding(dp(16), dp(16), dp(16), dp(16))
            builder()
        }
    }

    // ==================== Component Builders ====================

    private fun LinearLayout.addParentBubble(
        activity: ComponentActivity,
        senderName: String,
        messageText: String,
        time: String,
        dark: Boolean = false,
        showAvatar: Boolean = true,
        alignment: Int = Gravity.START,
        isOutgoing: Boolean = false
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = alignment
        }

        if (showAvatar && !isOutgoing) {
            // Avatar
            val avatar = TextView(activity).apply {
                text = senderName.first().toString()
                textSize = 14f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                val size = dp(36)
                layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = dp(8) }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(AVATAR_BG)
                }
            }
            row.addView(avatar)
        }

        // Bubble
        val bubbleBg = if (isOutgoing) Color.parseColor("#6851D6") else if (dark) BUBBLE_BG_DARK else BUBBLE_BG
        val bubble = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(bubbleBg, dp(12))
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (isOutgoing) marginStart = dp(50) else marginEnd = dp(50)
            }
        }

        if (!isOutgoing) {
            val nameText = TextView(activity).apply {
                text = senderName
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY)
            }
            bubble.addView(nameText)
        }

        val msgText = TextView(activity).apply {
            text = messageText
            textSize = 14f
            setTextColor(if (isOutgoing) Color.WHITE else if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(4) }
        }
        bubble.addView(msgText)

        val timeText = TextView(activity).apply {
            text = time
            textSize = 10f
            setTextColor(if (isOutgoing) Color.argb(180, 255, 255, 255) else TEXT_TERTIARY)
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(4) }
        }
        bubble.addView(timeText)

        row.addView(bubble)
        addView(row)
    }

    private fun LinearLayout.addReactions(activity: ComponentActivity, dark: Boolean = false) {
        val reactionsRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
                marginStart = dp(44) // Align with bubble (after avatar)
            }
        }

        val reactions = listOf("👍 3", "❤️ 2", "😂 1")
        for (reaction in reactions) {
            val chip = TextView(activity).apply {
                text = reaction
                textSize = 12f
                setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
                setPadding(dp(8), dp(4), dp(8), dp(4))
                background = roundedBg(if (dark) REACTION_BG_DARK else REACTION_BG, dp(12))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(6) }
            }
            reactionsRow.addView(chip)
        }

        addView(reactionsRow)
    }

    private fun LinearLayout.addReplyCountBar(activity: ComponentActivity, count: Int, dark: Boolean = false) {
        val separator = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            ).apply { topMargin = dp(12) }
            setBackgroundColor(if (dark) SEPARATOR_COLOR_DARK else SEPARATOR_COLOR)
        }
        addView(separator)

        val replyText = if (count == 1) "1 reply" else "$count replies"
        val replyBar = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = roundedBg(if (dark) REPLY_COUNT_BG_DARK else REPLY_COUNT_BG, dp(8))
            gravity = Gravity.CENTER_VERTICAL
        }

        val replyLabel = TextView(activity).apply {
            text = replyText
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(REPLY_COUNT_TEXT)
        }
        replyBar.addView(replyLabel)

        addView(replyBar)
    }

    // ==================== Utility Methods ====================

    private fun dp(value: Int): Int = (value * 2.75f).toInt()

    private fun roundedBg(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(color)
        }
    }
}
