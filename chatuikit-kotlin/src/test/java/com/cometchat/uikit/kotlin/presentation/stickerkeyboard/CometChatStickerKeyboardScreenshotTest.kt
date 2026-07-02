package com.cometchat.uikit.kotlin.presentation.stickerkeyboard

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
 * Screenshot tests for CometChatStickerKeyboard (chatuikit-kotlin).
 *
 * Uses the simulated-view approach to render sticker keyboard states since the actual
 * component depends on CometChatStickerKeyboardViewModel which fetches stickers via
 * GetStickersUseCase (network call). Simulated views ensure all visual elements are
 * fully rendered without requiring real SDK/ViewModel calls.
 *
 * The layout mirrors the actual component structure from cometchat_sticker_keyboard.xml:
 * - RelativeLayout container
 *   - empty_sticker_layout (LinearLayout): Empty state with icon + title + subtitle
 *   - error_sticker_layout (LinearLayout): Error state with message + retry button
 *   - shimmer_effect_frame: Loading shimmer grid (4-column GridLayoutManager, 16 items)
 *   - sticker_custom_layout (LinearLayout): Custom state container
 *   - stickers_view (LinearLayout): Content state
 *     - view_pager (ViewPager2): Swipeable sticker pages
 *     - separator (View): 1dp divider line
 *     - rv_tab_bar (RecyclerView): Horizontal tab bar for sticker set navigation
 *
 * Mock sticker data follows the CometChat sticker extension customData format:
 * {"sticker_name": "gery", "sticker_url": "https://data-in.cc-cluster-2.io/stickers/gery/gery_1.png"}
 *
 * States captured:
 * - stateLoading / stateLoadingDark: Shimmer grid placeholder
 * - stateContent / stateContentDark: Sticker grid with tab bar
 * - stateContentSecondTab: Second sticker set selected
 * - stateEmpty / stateEmptyDark: No stickers available message
 * - stateError / stateErrorDark: Error with retry button
 * - styleCustomColors: Custom theming applied
 *
 * Run with:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*CometChatStickerKeyboardScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatStickerKeyboardScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/stickerkeyboard"
        )
    )

    // ==================== Colors ====================

    private val BG_COLOR = Color.WHITE
    private val BG_COLOR_DARK = Color.parseColor("#1A1A2E")
    private val TEXT_PRIMARY = Color.parseColor("#1A1A1A")
    private val TEXT_PRIMARY_DARK = Color.parseColor("#EEEEEE")
    private val TEXT_SECONDARY = Color.parseColor("#666666")
    private val TEXT_SECONDARY_DARK = Color.parseColor("#AAAAAA")
    private val TEXT_TERTIARY = Color.parseColor("#999999")
    private val SEPARATOR_COLOR = Color.parseColor("#E8E8E8")
    private val SEPARATOR_COLOR_DARK = Color.parseColor("#333344")
    private val TAB_ACTIVE_COLOR = Color.parseColor("#6851D6")
    private val TAB_INACTIVE_BG = Color.parseColor("#F5F5F5")
    private val TAB_INACTIVE_BG_DARK = Color.parseColor("#2D2D44")
    private val STICKER_PLACEHOLDER_BG = Color.parseColor("#F0F0F0")
    private val STICKER_PLACEHOLDER_BG_DARK = Color.parseColor("#2A2A3E")
    private val SHIMMER_BASE = Color.parseColor("#EEEEEE")
    private val SHIMMER_BASE_DARK = Color.parseColor("#2D2D44")
    private val BUTTON_COLOR = Color.parseColor("#6851D6")

    // ==================== Mock Sticker Data ====================

    private data class MockSticker(val name: String, val url: String, val setName: String)
    private data class MockStickerSet(val name: String, val stickers: List<MockSticker>, val iconUrl: String)

    private val mockStickerSets = listOf(
        MockStickerSet(
            name = "gery",
            stickers = (1..8).map { i ->
                MockSticker(
                    name = "gery",
                    url = "https://data-in.cc-cluster-2.io/stickers/gery/gery_$i.png",
                    setName = "gery"
                )
            },
            iconUrl = "https://data-in.cc-cluster-2.io/stickers/gery/gery_1.png"
        ),
        MockStickerSet(
            name = "cool_monkey",
            stickers = (1..8).map { i ->
                MockSticker(
                    name = "cool_monkey",
                    url = "https://data-in.cc-cluster-2.io/stickers/cool_monkey/cool_monkey_$i.png",
                    setName = "cool_monkey"
                )
            },
            iconUrl = "https://data-in.cc-cluster-2.io/stickers/cool_monkey/cool_monkey_1.png"
        ),
        MockStickerSet(
            name = "love",
            stickers = (1..8).map { i ->
                MockSticker(
                    name = "love",
                    url = "https://data-in.cc-cluster-2.io/stickers/love/love_$i.png",
                    setName = "love"
                )
            },
            iconUrl = "https://data-in.cc-cluster-2.io/stickers/love/love_1.png"
        ),
        MockStickerSet(
            name = "funny",
            stickers = (1..8).map { i ->
                MockSticker(
                    name = "funny",
                    url = "https://data-in.cc-cluster-2.io/stickers/funny/funny_$i.png",
                    setName = "funny"
                )
            },
            iconUrl = "https://data-in.cc-cluster-2.io/stickers/funny/funny_1.png"
        )
    )

    // Emoji representations for sticker placeholders in the grid
    private val stickerEmojis = mapOf(
        "gery" to listOf("😀", "😂", "🤣", "😊", "😇", "🥰", "😍", "🤩"),
        "cool_monkey" to listOf("🐵", "🙈", "🙉", "🙊", "🐒", "🦍", "🦧", "🐵"),
        "love" to listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍"),
        "funny" to listOf("😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔")
    )

    // ==================== Section 1: UI States ====================

    @Test
    fun stateLoading() {
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, BG_COLOR) {
                addShimmerGrid(activity)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateLoadingDark() {
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, BG_COLOR_DARK) {
                addShimmerGrid(activity, dark = true)
            }
        }
    }

    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, BG_COLOR) {
                addStickerGrid(activity, mockStickerSets[0])
                addSeparator(activity)
                addTabBar(activity, mockStickerSets, selectedIndex = 0)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, BG_COLOR_DARK) {
                addStickerGrid(activity, mockStickerSets[0], dark = true)
                addSeparator(activity, dark = true)
                addTabBar(activity, mockStickerSets, selectedIndex = 0, dark = true)
            }
        }
    }

    @Test
    fun stateContentSecondTab() {
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, BG_COLOR) {
                addStickerGrid(activity, mockStickerSets[1])
                addSeparator(activity)
                addTabBar(activity, mockStickerSets, selectedIndex = 1)
            }
        }
    }

    @Test
    fun stateEmpty() {
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, BG_COLOR) {
                addEmptyState(activity)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateEmptyDark() {
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, BG_COLOR_DARK) {
                addEmptyState(activity, dark = true)
            }
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, BG_COLOR) {
                addErrorState(activity)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateErrorDark() {
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, BG_COLOR_DARK) {
                addErrorState(activity, dark = true)
            }
        }
    }

    // ==================== Section 2: Style Variants ====================

    @Test
    fun styleCustomColors() {
        val customBg = Color.parseColor("#1A1A2E")
        val customSeparator = Color.parseColor("#333333")
        val customTabActive = Color.parseColor("#6851D6")
        launchAndCapture { activity ->
            buildStickerKeyboardLayout(activity, customBg) {
                addStickerGrid(activity, mockStickerSets[0], dark = true)
                addSeparator(activity, color = customSeparator)
                addTabBar(activity, mockStickerSets, selectedIndex = 0, dark = true, activeColor = customTabActive)
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

    /**
     * Builds the root sticker keyboard layout matching the MaterialCardView container
     * from the actual CometChatStickerKeyboard component.
     */
    private fun buildStickerKeyboardLayout(
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

    /**
     * Simulates the stickers_view content state:
     * - ViewPager2 content area showing a 4-column sticker grid
     * - Each sticker cell has a placeholder emoji and sticker name label
     */
    private fun LinearLayout.addStickerGrid(
        activity: ComponentActivity,
        stickerSet: MockStickerSet,
        dark: Boolean = false
    ) {
        val gridContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            setPadding(dp(8), dp(16), dp(8), dp(8))
        }

        val emojis = stickerEmojis[stickerSet.name] ?: stickerEmojis["gery"]!!

        // Create 4-column grid (2 rows of 4) matching GridLayoutManager(context, 4)
        for (row in 0..1) {
            val rowLayout = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
                gravity = Gravity.CENTER
            }

            for (col in 0..3) {
                val index = row * 4 + col
                if (index < emojis.size) {
                    val stickerCell = FrameLayout(activity).apply {
                        layoutParams = LinearLayout.LayoutParams(0, dp(80), 1f).apply {
                            marginStart = dp(4)
                            marginEnd = dp(4)
                        }
                        background = roundedBg(
                            if (dark) STICKER_PLACEHOLDER_BG_DARK else STICKER_PLACEHOLDER_BG,
                            dp(12)
                        )
                    }

                    val stickerLabel = TextView(activity).apply {
                        text = emojis[index]
                        textSize = 32f
                        gravity = Gravity.CENTER
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    }
                    stickerCell.addView(stickerLabel)

                    // Sticker name label at bottom of cell
                    val nameLabel = TextView(activity).apply {
                        text = "${stickerSet.name}_${index + 1}"
                        textSize = 9f
                        setTextColor(TEXT_TERTIARY)
                        gravity = Gravity.CENTER
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        ).apply { gravity = Gravity.BOTTOM }
                        setPadding(0, 0, 0, dp(4))
                    }
                    stickerCell.addView(nameLabel)

                    rowLayout.addView(stickerCell)
                }
            }

            gridContainer.addView(rowLayout)
        }

        addView(gridContainer)
    }

    /**
     * Simulates the shimmer_effect_frame loading state:
     * - 4x4 grid of shimmer placeholder cells (16 items matching CometChatShimmerAdapter count)
     * - Shimmer tab bar at bottom
     */
    private fun LinearLayout.addShimmerGrid(activity: ComponentActivity, dark: Boolean = false) {
        val gridContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            setPadding(dp(8), dp(16), dp(8), dp(8))
        }

        // 4 rows of 4 shimmer placeholders (16 total matching shimmer adapter)
        for (row in 0..3) {
            val rowLayout = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
                gravity = Gravity.CENTER
            }

            for (col in 0..3) {
                val shimmerCell = View(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, dp(80), 1f).apply {
                        marginStart = dp(4)
                        marginEnd = dp(4)
                    }
                    background = roundedBg(
                        if (dark) SHIMMER_BASE_DARK else SHIMMER_BASE,
                        dp(12)
                    )
                }
                rowLayout.addView(shimmerCell)
            }

            gridContainer.addView(rowLayout)
        }

        addView(gridContainer)
        addSeparator(activity, dark = dark)
        addShimmerTabBar(activity, dark = dark)
    }

    /**
     * Simulates the shimmer tab bar (loading state for rv_tab_bar).
     */
    private fun LinearLayout.addShimmerTabBar(activity: ComponentActivity, dark: Boolean = false) {
        val tabBarLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(8), dp(8), dp(8), dp(8))
            gravity = Gravity.CENTER_VERTICAL
        }

        for (i in 0..5) {
            val tabShimmer = View(activity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(36), dp(36)).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
                background = roundedBg(
                    if (dark) SHIMMER_BASE_DARK else SHIMMER_BASE,
                    dp(18)
                )
            }
            tabBarLayout.addView(tabShimmer)
        }

        addView(tabBarLayout)
    }

    /**
     * Simulates the separator view (1dp divider between ViewPager2 and tab bar).
     */
    private fun LinearLayout.addSeparator(activity: ComponentActivity, dark: Boolean = false, color: Int? = null) {
        val separator = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            )
            setBackgroundColor(color ?: if (dark) SEPARATOR_COLOR_DARK else SEPARATOR_COLOR)
        }
        addView(separator)
    }

    /**
     * Simulates the rv_tab_bar (horizontal RecyclerView with StickerTabAdapter).
     * Each tab shows a sticker set icon with active indicator for selected tab.
     */
    private fun LinearLayout.addTabBar(
        activity: ComponentActivity,
        stickerSets: List<MockStickerSet>,
        selectedIndex: Int,
        dark: Boolean = false,
        activeColor: Int = TAB_ACTIVE_COLOR
    ) {
        val tabBarLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(8), dp(8), dp(8), dp(8))
            gravity = Gravity.CENTER_VERTICAL
        }

        for ((index, set) in stickerSets.withIndex()) {
            val isSelected = index == selectedIndex
            val tabItem = FrameLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
                background = if (isSelected) {
                    roundedBg(Color.argb(30, 104, 81, 214), dp(22))
                } else {
                    roundedBg(if (dark) TAB_INACTIVE_BG_DARK else TAB_INACTIVE_BG, dp(22))
                }
            }

            // Tab icon (emoji representation of sticker set)
            val tabEmoji = when (set.name) {
                "gery" -> "😀"
                "cool_monkey" -> "🐵"
                "love" -> "❤️"
                "funny" -> "😜"
                else -> "📦"
            }

            val tabLabel = TextView(activity).apply {
                text = tabEmoji
                textSize = 18f
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            tabItem.addView(tabLabel)

            // Active indicator line below selected tab
            if (isSelected) {
                val indicator = View(activity).apply {
                    layoutParams = FrameLayout.LayoutParams(dp(24), dp(3)).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                    background = roundedBg(activeColor, dp(2))
                }
                tabItem.addView(indicator)
            }

            tabBarLayout.addView(tabItem)
        }

        addView(tabBarLayout)
    }

    /**
     * Simulates the empty_sticker_layout state matching the XML layout:
     * - ImageView with sticker icon (represented as emoji)
     * - tv_empty_sticker_title: "No Stickers Available"
     * - tv_empty_sticker_subtitle: "You don't have any stickers yet"
     */
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

        // Sticker icon (iv_empty_sticker in XML uses cometchat_ic_filled_sticker)
        val iconView = TextView(activity).apply {
            text = "🎭"
            textSize = 48f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(16)
            }
        }
        content.addView(iconView)

        // tv_empty_sticker_title
        val title = TextView(activity).apply {
            text = "No Stickers Available"
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

        // tv_empty_sticker_subtitle
        val subtitle = TextView(activity).apply {
            text = "You don't have any stickers yet"
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

    /**
     * Simulates the error_sticker_layout state matching the XML layout:
     * - tv_error_sticker_title: "Looks like something went wrong.\nPlease try again."
     * - retry_btn (MaterialButton): "RETRY" button
     */
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

        // tv_error_sticker_title
        val errorText = TextView(activity).apply {
            text = "Looks like something went wrong.\nPlease try again."
            textSize = 14f
            setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(16)
            }
        }
        content.addView(errorText)

        // retry_btn (MaterialButton with cometchatPrimaryColor background)
        val retryButton = TextView(activity).apply {
            text = "RETRY"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(10), dp(24), dp(10))
            background = roundedBg(BUTTON_COLOR, dp(20))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER_HORIZONTAL }
        }
        content.addView(retryButton)

        container.addView(content)
        addView(container)
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
