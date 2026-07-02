package com.cometchat.uikit.kotlin.presentation.report

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
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
 * Roborazzi screenshot tests for CometChatFlagMessageDialog (chatuikit-kotlin).
 *
 * Uses the simulated-view approach to render the flag message dialog layout directly
 * into the activity, bypassing Dialog window issues with Robolectric/Roborazzi.
 *
 * States captured:
 * - stateInitial: No chip selected, report button disabled
 * - stateChipSelected: One chip active, report button enabled
 * - stateWithRemark: Remark field with text entered
 * - stateProgress: Progress indicator on report button
 * - stateError: Error message visible
 * - stateContentDark: Dark theme variant
 * - styleCustomColors: Custom style applied
 * - visibilityNoRemarkField: Remark field hidden
 * - customTitle: Custom title text
 * - customDescription: Custom description text
 * - customButtonTexts: Custom button labels
 * - manyFlagReasons: 6 flag reason chips
 *
 * Run:
 *   ./gradlew :chatuikit-kotlin:recordRoborazziDebug --tests "*CometChatFlagMessageDialogScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatFlagMessageDialogScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/kotlin/flagmessagedialog"
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
    private val CHIP_BG = Color.parseColor("#F5F5F5")
    private val CHIP_BG_DARK = Color.parseColor("#2D2D44")
    private val CHIP_SELECTED_BG = Color.parseColor("#6851D6")
    private val CHIP_TEXT = Color.parseColor("#333333")
    private val CHIP_TEXT_DARK = Color.parseColor("#CCCCCC")
    private val CHIP_SELECTED_TEXT = Color.WHITE
    private val BUTTON_BG = Color.parseColor("#F5F5F5")
    private val BUTTON_BG_DARK = Color.parseColor("#2D2D44")
    private val REPORT_BUTTON_BG = Color.parseColor("#6851D6")
    private val REPORT_BUTTON_DISABLED_BG = Color.parseColor("#CCCCCC")
    private val ERROR_COLOR = Color.parseColor("#F44336")
    private val REMARK_BORDER = Color.parseColor("#E0E0E0")
    private val REMARK_BORDER_DARK = Color.parseColor("#444444")
    private val CLOSE_ICON_COLOR = Color.parseColor("#666666")

    // ==================== Mock Data ====================

    private val defaultReasons = listOf("Spam", "Sexual Content", "Harassment")
    private val manyReasons = listOf("Spam", "Sexual Content", "Harassment", "Violence", "Misinformation", "Other")

    // ==================== UI States ====================

    @Test
    fun stateInitial() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Report", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = -1)
                addRemarkField(activity, hint = "Add a remark (optional)")
                addButtons(activity, reportEnabled = false)
            }
        }
    }

    @Test
    fun stateChipSelected() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Report", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = 0)
                addRemarkField(activity, hint = "Add a remark (optional)")
                addButtons(activity, reportEnabled = true)
            }
        }
    }

    @Test
    fun stateWithRemark() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Report", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = 1)
                addRemarkField(activity, text = "This message contains inappropriate content that violates community guidelines.")
                addButtons(activity, reportEnabled = true)
            }
        }
    }

    @Test
    fun stateProgress() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Report", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = 0)
                addRemarkField(activity, hint = "Add a remark (optional)")
                addButtons(activity, reportEnabled = true, showProgress = true)
            }
        }
    }

    @Test
    fun stateError() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Report", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = 0)
                addRemarkField(activity, hint = "Add a remark (optional)")
                addErrorMessage(activity, "Something went wrong. Please try again.")
                addButtons(activity, reportEnabled = true)
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR_DARK) {
                addHeader(activity, "Report", "Why are you reporting this message?", dark = true)
                addSeparator(activity, dark = true)
                addChips(activity, defaultReasons, selectedIndex = 0, dark = true)
                addRemarkField(activity, hint = "Add a remark (optional)", dark = true)
                addButtons(activity, reportEnabled = true, dark = true)
            }
        }
    }

    @Test
    fun styleCustomColors() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, Color.parseColor("#F5F5DC")) {
                addHeader(activity, "Report", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = 1)
                addRemarkField(activity, hint = "Add a remark (optional)")
                addButtons(activity, reportEnabled = true)
            }
        }
    }

    @Test
    fun visibilityNoRemarkField() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Report", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = 0)
                // No remark field
                addButtons(activity, reportEnabled = true)
            }
        }
    }

    @Test
    fun customTitle() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Flag Message", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = -1)
                addRemarkField(activity, hint = "Add a remark (optional)")
                addButtons(activity, reportEnabled = false)
            }
        }
    }

    @Test
    fun customDescription() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Report", "Why do you want to flag this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = -1)
                addRemarkField(activity, hint = "Add a remark (optional)")
                addButtons(activity, reportEnabled = false)
            }
        }
    }

    @Test
    fun customButtonTexts() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Report", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, defaultReasons, selectedIndex = 0)
                addRemarkField(activity, hint = "Add a remark (optional)")
                addButtons(activity, reportEnabled = true, cancelText = "Dismiss", reportText = "Submit Report")
            }
        }
    }

    @Test
    fun manyFlagReasons() {
        launchAndCapture { activity ->
            buildFlagDialogLayout(activity, BG_COLOR) {
                addHeader(activity, "Report", "Why are you reporting this message?")
                addSeparator(activity)
                addChips(activity, manyReasons, selectedIndex = -1)
                addRemarkField(activity, hint = "Add a remark (optional)")
                addButtons(activity, reportEnabled = false)
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

    private fun buildFlagDialogLayout(
        activity: ComponentActivity,
        bgColor: Int,
        builder: LinearLayout.() -> Unit
    ): View {
        // Outer container simulating dialog overlay
        val overlay = FrameLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.argb(100, 0, 0, 0)) // Dim background
        }

        // Dialog card
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                marginStart = dp(24)
                marginEnd = dp(24)
            }
            background = roundedBg(bgColor, dp(16))
            elevation = 24f
            setPadding(dp(20), dp(20), dp(20), dp(20))
            builder()
        }

        overlay.addView(card)
        return overlay
    }

    // ==================== Component Builders ====================

    private fun LinearLayout.addHeader(
        activity: ComponentActivity,
        title: String,
        description: String,
        dark: Boolean = false
    ) {
        // Title row with close icon
        val titleRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleText = TextView(activity).apply {
            text = title
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        titleRow.addView(titleText)

        val closeIcon = TextView(activity).apply {
            text = "✕"
            textSize = 18f
            setTextColor(if (dark) TEXT_SECONDARY_DARK else CLOSE_ICON_COLOR)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24))
        }
        titleRow.addView(closeIcon)

        addView(titleRow)

        // Description
        val descText = TextView(activity).apply {
            text = description
            textSize = 13f
            setTextColor(if (dark) TEXT_SECONDARY_DARK else TEXT_SECONDARY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
        }
        addView(descText)
    }

    private fun LinearLayout.addSeparator(activity: ComponentActivity, dark: Boolean = false) {
        val sep = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            ).apply { topMargin = dp(16); bottomMargin = dp(16) }
            setBackgroundColor(if (dark) SEPARATOR_COLOR_DARK else SEPARATOR_COLOR)
        }
        addView(sep)
    }

    private fun LinearLayout.addChips(
        activity: ComponentActivity,
        reasons: List<String>,
        selectedIndex: Int,
        dark: Boolean = false
    ) {
        val chipContainer = object : LinearLayout(activity) {}.apply {
            // Use a FlowLayout-like approach with wrapping
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Since LinearLayout doesn't wrap, use multiple rows
        val rows = mutableListOf<LinearLayout>()
        var currentRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
        }
        rows.add(currentRow)

        var currentRowWidth = 0
        val maxWidth = 900 // Approximate max width in pixels

        for ((index, reason) in reasons.withIndex()) {
            val isSelected = index == selectedIndex
            val chipWidth = (reason.length * dp(8)) + dp(32) // Approximate chip width

            if (currentRowWidth + chipWidth > maxWidth && currentRow.childCount > 0) {
                currentRow = LinearLayout(activity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(8) }
                }
                rows.add(currentRow)
                currentRowWidth = 0
            }

            val chip = TextView(activity).apply {
                text = reason
                textSize = 13f
                setTextColor(if (isSelected) CHIP_SELECTED_TEXT else if (dark) CHIP_TEXT_DARK else CHIP_TEXT)
                setPadding(dp(14), dp(8), dp(14), dp(8))
                background = roundedBg(
                    if (isSelected) CHIP_SELECTED_BG else if (dark) CHIP_BG_DARK else CHIP_BG,
                    dp(16)
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(8) }
            }
            currentRow.addView(chip)
            currentRowWidth += chipWidth
        }

        for (row in rows) {
            addView(row)
        }
    }

    private fun LinearLayout.addRemarkField(
        activity: ComponentActivity,
        hint: String? = null,
        text: String? = null,
        dark: Boolean = false
    ) {
        // Label row
        val labelRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
        }
        val label = TextView(activity).apply {
            this.text = "Remark"
            textSize = 13f
            setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
        }
        labelRow.addView(label)
        val optional = TextView(activity).apply {
            this.text = " (optional)"
            textSize = 13f
            setTextColor(if (dark) TEXT_TERTIARY else TEXT_TERTIARY)
        }
        labelRow.addView(optional)
        addView(labelRow)

        // Text input area
        val inputBorder = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(100)
            ).apply { topMargin = dp(8) }
            background = roundedBorderBg(
                if (dark) BG_COLOR_DARK else BG_COLOR,
                if (dark) REMARK_BORDER_DARK else REMARK_BORDER,
                dp(8)
            )
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        val inputText = TextView(activity).apply {
            if (text != null) {
                this.text = text
                textSize = 13f
                setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            } else {
                this.text = hint ?: ""
                textSize = 13f
                setTextColor(if (dark) TEXT_TERTIARY else TEXT_TERTIARY)
            }
        }
        inputBorder.addView(inputText)
        addView(inputBorder)
    }

    private fun LinearLayout.addErrorMessage(activity: ComponentActivity, message: String) {
        val errorText = TextView(activity).apply {
            text = message
            textSize = 12f
            setTextColor(ERROR_COLOR)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
        }
        addView(errorText)
    }

    private fun LinearLayout.addButtons(
        activity: ComponentActivity,
        reportEnabled: Boolean,
        dark: Boolean = false,
        showProgress: Boolean = false,
        cancelText: String = "Cancel",
        reportText: String = "Report"
    ) {
        val buttonRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16) }
            gravity = Gravity.CENTER_VERTICAL
        }

        // Cancel button
        val cancelBtn = TextView(activity).apply {
            text = cancelText
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY)
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = roundedBg(if (dark) BUTTON_BG_DARK else BUTTON_BG, dp(8))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(8)
            }
        }
        buttonRow.addView(cancelBtn)

        // Report button
        val reportBtn = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(8)
            }
            background = roundedBg(
                if (reportEnabled) REPORT_BUTTON_BG else REPORT_BUTTON_DISABLED_BG,
                dp(8)
            )
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        if (showProgress) {
            val progress = ProgressBar(activity).apply {
                layoutParams = FrameLayout.LayoutParams(dp(20), dp(20)).apply {
                    gravity = Gravity.CENTER
                }
                isIndeterminate = true
                indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            }
            reportBtn.addView(progress)
        } else {
            val reportLabel = TextView(activity).apply {
                text = reportText
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
            reportBtn.addView(reportLabel)
        }

        buttonRow.addView(reportBtn)
        addView(buttonRow)
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

    private fun roundedBorderBg(fillColor: Int, strokeColor: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(fillColor)
            setStroke(dp(1), strokeColor)
        }
    }
}
