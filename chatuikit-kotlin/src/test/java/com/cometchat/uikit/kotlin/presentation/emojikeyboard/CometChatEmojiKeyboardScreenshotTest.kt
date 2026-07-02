package com.cometchat.uikit.kotlin.presentation.emojikeyboard

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.model.EmojiRepository
import com.cometchat.uikit.kotlin.presentation.emojikeyboard.ui.EmojiKeyBoardView
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.kotlin.presentation.utils.RoborazziConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatEmojiKeyboard (chatuikit-kotlin).
 *
 * Pre-loads emoji data from the bundled emoji.json asset before creating the view,
 * ensuring the EmojiRepository singleton has cached data available synchronously.
 * This guarantees the emoji grid renders with real emoji data in screenshots.
 *
 * States captured:
 * - Content state (emojis loaded and displayed)
 * - Dark theme variant
 * - Custom background color
 * - Custom styling colors
 * - Custom corner radius
 * - Custom stroke
 * - Tab selection
 * - Light/Dark theme defaults
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*CometChatEmojiKeyboardScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatEmojiKeyboardScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/emojikeyboard"
        )
    )

    @Before
    fun setup() {
        // Pre-load emoji data from the bundled emoji.json asset into the singleton cache.
        // This ensures EmojiRepository.getEmojiCategories() returns data synchronously
        // when EmojiKeyBoardView is created, avoiding the async loading issue in Robolectric.
        val context = RuntimeEnvironment.getApplication()
        EmojiRepository.loadAndSaveEmojis(context)
        // Idle the background thread and main looper to ensure data is fully loaded and cached
        ShadowLooper.runMainLooperToNextTask()
        Thread.sleep(500) // Allow background thread to complete JSON parsing
        ShadowLooper.idleMainLooper() // Process the Handler.post callback
    }

    // ==================== Section 1: UI States ====================

    @Test
    fun stateContent() {
        launchAndCapture { activity ->
            createEmojiKeyboardView(activity)
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            createEmojiKeyboardView(activity)
        }
    }

    // ==================== Section 2: Style Variants ====================

    @Test
    fun styleCustomBackground() {
        launchAndCapture { activity ->
            val view = createEmojiKeyboardView(activity)
            view.setBackgroundColor(Color.parseColor("#F5F5DC")) // Beige
            view
        }
    }

    @Test
    fun stylingCustomColors() {
        launchAndCapture { activity ->
            val view = createEmojiKeyboardView(activity)
            view.setBackgroundColor(Color.parseColor("#1A1A2E"))
            view.setSeparatorColor(Color.parseColor("#333333"))
            view.setCategoryIconTint(Color.parseColor("#888888"))
            view.setSelectedCategoryIconTint(Color.parseColor("#6851D6"))
            view.setSelectedCategoryBackgroundColor(Color.parseColor("#E8E0FF"))
            view.setCategoryTextColor(Color.parseColor("#AAAAAA"))
            view
        }
    }

    @Test
    fun styleCustomCornerRadius() {
        launchAndCapture { activity ->
            val view = createEmojiKeyboardView(activity)
            view.setCornerRadius(32)
            view
        }
    }

    @Test
    fun styleCustomStroke() {
        launchAndCapture { activity ->
            val view = createEmojiKeyboardView(activity)
            view.setStrokeWidth(4)
            view.setStrokeColor(Color.parseColor("#6851D6"))
            view
        }
    }

    // ==================== Section 3: Tab Selection ====================

    @Test
    fun tabSelectionFirst() {
        launchAndCapture { activity ->
            createEmojiKeyboardView(activity)
        }
    }

    // ==================== Section 4: Theme Variants ====================

    @Test
    fun theme_lightDefault() {
        launchAndCapture { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            createEmojiKeyboardView(activity)
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun theme_darkDefault() {
        launchAndCapture { activity ->
            activity.setTheme(R.style.CometChatTheme_DayNight)
            createEmojiKeyboardView(activity)
        }
    }

    // ==================== Helper Methods ====================

    private fun createEmojiKeyboardView(activity: ComponentActivity): EmojiKeyBoardView {
        activity.setTheme(R.style.CometChatTheme_DayNight)
        return EmojiKeyBoardView(activity)
    }

    private fun launchAndCapture(configure: (ComponentActivity) -> EmojiKeyBoardView) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { activity ->
            val view = configure(activity)
            val container = FrameLayout(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(view, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
            }
            activity.setContentView(container)
            ShadowLooper.idleMainLooper()
        }
        scenario.onActivity { activity ->
            // Extra idle to ensure tabs and emoji grid are fully rendered
            ShadowLooper.idleMainLooper()
            activity.window.decorView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        scenario.close()
    }
}
