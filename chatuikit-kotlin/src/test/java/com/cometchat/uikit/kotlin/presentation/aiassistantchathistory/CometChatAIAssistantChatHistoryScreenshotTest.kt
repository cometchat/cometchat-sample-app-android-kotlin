package com.cometchat.uikit.kotlin.presentation.aiassistantchathistory

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
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
 * Screenshot tests for CometChatAIAssistantChatHistory (chatuikit-kotlin).
 *
 * Renders simulated views with mock data to capture golden images for all visual states:
 * - Empty state (no conversations history)
 * - Content state (messages with date separators)
 * - Dark mode variants
 * - Custom styling
 * - Loading (shimmer) state
 * - Error state
 *
 * Uses the same simulated-view approach as CometChatMessageListScreenshotTest to ensure
 * all visual elements are fully rendered without requiring real SDK calls.
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*CometChatAIAssistantChatHistoryScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatAIAssistantChatHistoryScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/aiassistantchathistory"
        )
    )

    // ==================== Colors ====================

    private val BG_COLOR = Color.WHITE
    private val BG_COLOR_DARK = Color.parseColor("#1A1A2E")
    private val HEADER_BG = Color.WHITE
    private val HEADER_BG_DARK = Color.parseColor("#1A1A2E")
    private val TEXT_PRIMARY = Color.parseColor("#1A1A1A")
    private val TEXT_PRIMARY_DARK = Color.parseColor("#EEEEEE")
    private val TEXT_SECONDARY = Color.parseColor("#666666")
    private val TEXT_SECONDARY_DARK = Color.parseColor("#AAAAAA")
    private val TEXT_TERTIARY = Color.parseColor("#999999")
    private val TEXT_TERTIARY_DARK = Color.parseColor("#777777")
    private val DIVIDER_COLOR = Color.parseColor("#E8E8E8")
    private val DIVIDER_COLOR_DARK = Color.parseColor("#333344")
    private val ICON_TINT = Color.parseColor("#666666")
    private val ICON_TINT_DARK = Color.parseColor("#AAAAAA")
    private val DATE_BG = Color.parseColor("#F5F5F5")
    private val DATE_BG_DARK = Color.parseColor("#2A2A3E")
    private val ACCENT_COLOR = Color.parseColor("#6851D6")
    private val ERROR_COLOR = Color.parseColor("#F44336")
    private val SHIMMER_BASE = Color.parseColor("#EEEEEE")
    private val SHIMMER_BASE_DARK = Color.parseColor("#2D2D44")

    // ==================== Mock Data ====================

    private val mockMessages = listOf(
        MockChatHistoryItem("How do I implement push notifications in Android?", 1706745600L), // Jan 31, 2024
        MockChatHistoryItem("Can you explain the difference between LiveData and StateFlow?", 1706745900L),
        MockChatHistoryItem("What's the best way to handle configuration changes?", 1706746200L),
        MockChatHistoryItem("Help me write a unit test for my ViewModel", 1706659200L), // Jan 30, 2024
        MockChatHistoryItem("How to use Hilt for dependency injection?", 1706659500L),
        MockChatHistoryItem("Explain coroutine scopes and structured concurrency", 1706659800L),
        MockChatHistoryItem("What are the best practices for Room database migrations?", 1706572800L), // Jan 29, 2024
        MockChatHistoryItem("How to implement pagination with Paging 3 library?", 1706573100L),
        MockChatHistoryItem("Can you help me debug this RecyclerView performance issue?", 1706573400L),
        MockChatHistoryItem("What's the recommended architecture for a multi-module project?", 1706573700L)
    )

    private data class MockChatHistoryItem(val text: String, val sentAt: Long)

    // ==================== UI States ====================

    @Test
    fun stateEmpty() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR) {
                addHeader(activity)
                addDivider(activity)
                addNewChatRow(activity)
                addEmptyState(activity)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEmptyDark() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR_DARK) {
                addHeader(activity, dark = true)
                addDivider(activity, dark = true)
                addNewChatRow(activity, dark = true)
                addEmptyState(activity, dark = true)
            }
        }
    }

    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR) {
                addHeader(activity)
                addDivider(activity)
                addNewChatRow(activity)
                addDateSeparator(activity, "Jan 31, 2024")
                addMessageItem(activity, mockMessages[0].text)
                addMessageItem(activity, mockMessages[1].text)
                addMessageItem(activity, mockMessages[2].text)
                addDateSeparator(activity, "Jan 30, 2024")
                addMessageItem(activity, mockMessages[3].text)
                addMessageItem(activity, mockMessages[4].text)
                addMessageItem(activity, mockMessages[5].text)
                addDateSeparator(activity, "Jan 29, 2024")
                addMessageItem(activity, mockMessages[6].text)
                addMessageItem(activity, mockMessages[7].text)
                addMessageItem(activity, mockMessages[8].text)
                addMessageItem(activity, mockMessages[9].text)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR_DARK) {
                addHeader(activity, dark = true)
                addDivider(activity, dark = true)
                addNewChatRow(activity, dark = true)
                addDateSeparator(activity, "Jan 31, 2024", dark = true)
                addMessageItem(activity, mockMessages[0].text, dark = true)
                addMessageItem(activity, mockMessages[1].text, dark = true)
                addMessageItem(activity, mockMessages[2].text, dark = true)
                addDateSeparator(activity, "Jan 30, 2024", dark = true)
                addMessageItem(activity, mockMessages[3].text, dark = true)
                addMessageItem(activity, mockMessages[4].text, dark = true)
                addMessageItem(activity, mockMessages[5].text, dark = true)
                addDateSeparator(activity, "Jan 29, 2024", dark = true)
                addMessageItem(activity, mockMessages[6].text, dark = true)
                addMessageItem(activity, mockMessages[7].text, dark = true)
                addMessageItem(activity, mockMessages[8].text, dark = true)
                addMessageItem(activity, mockMessages[9].text, dark = true)
            }
        }
    }

    @Test
    fun stateLoading() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR) {
                addHeader(activity)
                addDivider(activity)
                addNewChatRow(activity)
                addShimmerItems(activity, count = 10)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateLoadingDark() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR_DARK) {
                addHeader(activity, dark = true)
                addDivider(activity, dark = true)
                addNewChatRow(activity, dark = true)
                addShimmerItems(activity, count = 10, dark = true)
            }
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR) {
                addHeader(activity)
                addDivider(activity)
                addNewChatRow(activity)
                addErrorState(activity)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateErrorDark() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR_DARK) {
                addHeader(activity, dark = true)
                addDivider(activity, dark = true)
                addNewChatRow(activity, dark = true)
                addErrorState(activity, dark = true)
            }
        }
    }

    // ==================== Style Variants ====================

    @Test
    fun styleCustomBackground() {
        val customBg = Color.parseColor("#F5F5DC") // Beige
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, customBg) {
                addHeader(activity, headerBg = customBg)
                addDivider(activity)
                addNewChatRow(activity)
                addDateSeparator(activity, "Jan 31, 2024")
                addMessageItem(activity, mockMessages[0].text)
                addMessageItem(activity, mockMessages[1].text)
                addMessageItem(activity, mockMessages[2].text)
                addDateSeparator(activity, "Jan 30, 2024")
                addMessageItem(activity, mockMessages[3].text)
                addMessageItem(activity, mockMessages[4].text)
            }
        }
    }

    @Test
    fun stylingCustomColors() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR) {
                addHeader(activity, textColor = ACCENT_COLOR)
                addDivider(activity, color = ACCENT_COLOR)
                addNewChatRow(activity, textColor = ACCENT_COLOR, iconTint = ACCENT_COLOR)
                addDateSeparator(activity, "Jan 31, 2024", textColor = ACCENT_COLOR)
                addMessageItem(activity, mockMessages[0].text, textColor = ACCENT_COLOR)
                addMessageItem(activity, mockMessages[1].text, textColor = ACCENT_COLOR)
                addMessageItem(activity, mockMessages[2].text, textColor = ACCENT_COLOR)
                addDateSeparator(activity, "Jan 30, 2024", textColor = ACCENT_COLOR)
                addMessageItem(activity, mockMessages[3].text, textColor = ACCENT_COLOR)
                addMessageItem(activity, mockMessages[4].text, textColor = ACCENT_COLOR)
            }
        }
    }

    // ==================== Content Variants ====================

    @Test
    fun contentWithDateSeparators() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR) {
                addHeader(activity)
                addDivider(activity)
                addNewChatRow(activity)
                addDateSeparator(activity, "Today")
                addMessageItem(activity, "What's the latest version of Kotlin?")
                addMessageItem(activity, "How to use sealed interfaces?")
                addDateSeparator(activity, "Yesterday")
                addMessageItem(activity, "Explain Compose recomposition")
                addMessageItem(activity, "Best practices for state hoisting")
                addDateSeparator(activity, "Jan 28, 2024")
                addMessageItem(activity, "How to implement dark theme properly?")
                addMessageItem(activity, "Material 3 dynamic colors setup")
                addDateSeparator(activity, "Jan 25, 2024")
                addMessageItem(activity, "Jetpack Navigation with deep links")
                addMessageItem(activity, "How to handle back navigation in Compose?")
            }
        }
    }

    @Test
    fun contentSingleMessage() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR) {
                addHeader(activity)
                addDivider(activity)
                addNewChatRow(activity)
                addDateSeparator(activity, "Today")
                addMessageItem(activity, "How do I get started with CometChat SDK?")
            }
        }
    }

    @Test
    fun contentLongMessages() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR) {
                addHeader(activity)
                addDivider(activity)
                addNewChatRow(activity)
                addDateSeparator(activity, "Today")
                addMessageItem(activity, "Can you explain the complete lifecycle of an Android Activity including all the callbacks and when they are triggered during configuration changes, process death, and normal navigation?")
                addMessageItem(activity, "What is the recommended way to implement a complex multi-step form with validation, state persistence across configuration changes, and proper error handling in Jetpack Compose?")
                addMessageItem(activity, "Help me understand the differences between ViewModelScope, lifecycleScope, and GlobalScope in Kotlin coroutines and when to use each one")
                addDateSeparator(activity, "Yesterday")
                addMessageItem(activity, "How to properly implement offline-first architecture with Room database, WorkManager for sync, and proper conflict resolution strategies?")
                addMessageItem(activity, "Explain the complete flow of dependency injection with Hilt including custom scopes, assisted injection, and multi-module setup")
            }
        }
    }

    // ==================== Popup Menu ====================

    @Test
    fun popupMenuDelete() {
        launchAndCapture { activity ->
            buildChatHistoryLayout(activity, BG_COLOR) {
                addHeader(activity)
                addDivider(activity)
                addNewChatRow(activity)
                addDateSeparator(activity, "Today")
                addMessageItem(activity, mockMessages[0].text)
                addMessageItem(activity, mockMessages[1].text, highlighted = true)
                // Simulated popup menu overlay
                addPopupMenu(activity, listOf("Delete"))
                addMessageItem(activity, mockMessages[2].text)
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

    private fun buildChatHistoryLayout(
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
            builder()
        }
    }

    // ==================== Component Builders ====================

    private fun LinearLayout.addHeader(
        activity: ComponentActivity,
        dark: Boolean = false,
        headerBg: Int? = null,
        textColor: Int? = null
    ) {
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(headerBg ?: if (dark) HEADER_BG_DARK else HEADER_BG)
        }

        // Close icon
        val closeIcon = TextView(activity).apply {
            text = "✕"
            textSize = 18f
            setTextColor(if (dark) ICON_TINT_DARK else ICON_TINT)
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                marginEnd = dp(12)
            }
            gravity = Gravity.CENTER
        }
        header.addView(closeIcon)

        // Title
        val title = TextView(activity).apply {
            text = "Chat History"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(textColor ?: if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        header.addView(title)

        addView(header)
    }

    private fun LinearLayout.addDivider(activity: ComponentActivity, dark: Boolean = false, color: Int? = null) {
        val divider = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            )
            setBackgroundColor(color ?: if (dark) DIVIDER_COLOR_DARK else DIVIDER_COLOR)
        }
        addView(divider)
    }

    private fun LinearLayout.addNewChatRow(
        activity: ComponentActivity,
        dark: Boolean = false,
        textColor: Int? = null,
        iconTint: Int? = null
    ) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        // New chat icon
        val icon = TextView(activity).apply {
            text = "✎"
            textSize = 16f
            setTextColor(iconTint ?: if (dark) ICON_TINT_DARK else ICON_TINT)
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                marginEnd = dp(12)
            }
            gravity = Gravity.CENTER
        }
        row.addView(icon)

        // New Chat text
        val label = TextView(activity).apply {
            text = "New Chat"
            textSize = 14f
            setTextColor(textColor ?: if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
        }
        row.addView(label)

        addView(row)
    }

    private fun LinearLayout.addDateSeparator(
        activity: ComponentActivity,
        dateText: String,
        dark: Boolean = false,
        textColor: Int? = null
    ) {
        val wrapper = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(16), dp(12), dp(16), dp(4))
            setBackgroundColor(if (dark) DATE_BG_DARK else DATE_BG)
        }

        val label = TextView(activity).apply {
            text = dateText
            textSize = 12f
            setTextColor(textColor ?: if (dark) TEXT_TERTIARY_DARK else TEXT_TERTIARY)
            setTypeface(null, Typeface.BOLD)
        }
        wrapper.addView(label)

        addView(wrapper)
    }

    private fun LinearLayout.addMessageItem(
        activity: ComponentActivity,
        text: String,
        dark: Boolean = false,
        textColor: Int? = null,
        highlighted: Boolean = false
    ) {
        val item = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dp(3), 0, dp(3)) }
            if (highlighted) {
                setBackgroundColor(if (dark) Color.parseColor("#2D2D44") else Color.parseColor("#E8E8F8"))
            }
        }

        val messageText = TextView(activity).apply {
            this.text = text
            textSize = 14f
            setTextColor(textColor ?: if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        item.addView(messageText)

        addView(item)
    }

    private fun LinearLayout.addShimmerItems(activity: ComponentActivity, count: Int, dark: Boolean = false) {
        for (i in 0 until count) {
            val item = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(dp(16), dp(12), dp(16), dp(12))
            }

            // Shimmer bar with varying widths
            val widthFraction = when (i % 4) {
                0 -> 0.85f
                1 -> 0.65f
                2 -> 0.75f
                else -> 0.55f
            }
            val shimmerBar = View(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, dp(14), widthFraction
                )
                background = roundedBg(if (dark) SHIMMER_BASE_DARK else SHIMMER_BASE, dp(4))
            }
            item.addView(shimmerBar)

            // Spacer to fill remaining width
            val spacer = View(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(14), 1f - widthFraction)
            }
            item.addView(spacer)

            addView(item)
        }
    }

    private fun LinearLayout.addEmptyState(activity: ComponentActivity, dark: Boolean = false) {
        val container = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Empty icon placeholder
        val iconPlaceholder = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(dp(80), dp(80)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(16)
            }
            background = roundedBg(if (dark) SHIMMER_BASE_DARK else SHIMMER_BASE, dp(40))
        }
        content.addView(iconPlaceholder)

        val title = TextView(activity).apply {
            text = "No conversations history"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(8)
            }
        }
        content.addView(title)

        val subtitle = TextView(activity).apply {
            text = "Start a new conversation with the AI assistant"
            textSize = 13f
            setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER_HORIZONTAL }
        }
        content.addView(subtitle)

        container.addView(content)
        addView(container)
    }

    private fun LinearLayout.addErrorState(activity: ComponentActivity, dark: Boolean = false) {
        val container = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Error icon placeholder
        val iconPlaceholder = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(dp(80), dp(80)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(16)
            }
            background = roundedBg(if (dark) Color.parseColor("#442222") else Color.parseColor("#FFEBEE"), dp(40))
        }
        content.addView(iconPlaceholder)

        val title = TextView(activity).apply {
            text = "Something went wrong"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(8)
            }
        }
        content.addView(title)

        val subtitle = TextView(activity).apply {
            text = "Unable to load conversations. Please try again."
            textSize = 13f
            setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER_HORIZONTAL }
        }
        content.addView(subtitle)

        container.addView(content)
        addView(container)
    }

    private fun LinearLayout.addPopupMenu(activity: ComponentActivity, options: List<String>) {
        val menuContainer = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
                marginEnd = dp(16)
                topMargin = dp(-8)
            }
        }

        val menu = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(Color.WHITE, dp(8))
            elevation = 8f
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        for (option in options) {
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(10), dp(24), dp(10))
            }

            val icon = TextView(activity).apply {
                text = "🗑️"
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(8) }
            }
            row.addView(icon)

            val label = TextView(activity).apply {
                text = option
                textSize = 14f
                setTextColor(ERROR_COLOR)
            }
            row.addView(label)

            menu.addView(row)
        }

        menuContainer.addView(menu)
        addView(menuContainer)
    }

    // ==================== Utility Methods ====================

    private fun dp(value: Int): Int = (value * 2.75f).toInt() // xxhdpi scale

    private fun roundedBg(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(color)
        }
    }
}
