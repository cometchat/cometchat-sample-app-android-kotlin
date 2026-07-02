package com.cometchat.uikit.compose.screenshots

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi screenshot tests for CometChatCreatePoll (Compose).
 *
 * Uses the simulated-view approach (same as chatuikit-kotlin) to render mock data
 * directly, avoiding SDK/ViewModel dependencies.
 *
 * States captured:
 * - Idle state (empty form, submit button disabled)
 * - With question text
 * - With options filled
 * - Submitting (progress)
 * - Error state
 * - Dark theme
 * - Custom style
 * - Visibility toggles (no toolbar)
 * - Content variants (many options)
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatCreatePollScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatCreatePollScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/createpoll"
        )
    )

    // ==================== Colors ====================

    private val BG_COLOR = Color.White
    private val BG_COLOR_DARK = Color(0xFF1A1A2E)
    private val TEXT_PRIMARY = Color(0xFF1A1A1A)
    private val TEXT_PRIMARY_DARK = Color(0xFFEEEEEE)
    private val TEXT_SECONDARY = Color(0xFF666666)
    private val TEXT_SECONDARY_DARK = Color(0xFFAAAAAA)
    private val TEXT_HINT = Color(0xFF999999)
    private val SEPARATOR_COLOR = Color(0xFFE8E8E8)
    private val SEPARATOR_COLOR_DARK = Color(0xFF333344)
    private val INPUT_BORDER = Color(0xFFE0E0E0)
    private val INPUT_BORDER_DARK = Color(0xFF444444)
    private val BUTTON_ENABLED = Color(0xFF6851D6)
    private val BUTTON_DISABLED = Color(0xFFCCCCCC)
    private val BUTTON_DISABLED_DARK = Color(0xFF444444)
    private val ICON_TINT = Color(0xFF666666)
    private val ICON_TINT_DARK = Color(0xFFAAAAAA)
    private val ERROR_COLOR = Color(0xFFF44336)

    // ==================== UI States ====================

    @Test
    fun stateIdle() {
        captureComposable {
            CreatePollScreen(dark = false) {
                QuestionSection(dark = false)
                OptionsSection(dark = false, options = listOf("", ""))
                SubmitButton(enabled = false, dark = false)
            }
        }
    }

    @Test
    fun stateWithQuestion() {
        captureComposable {
            CreatePollScreen(dark = false) {
                QuestionSection(dark = false, questionText = "Where should we go for the team outing this weekend?")
                OptionsSection(dark = false, options = listOf("", ""))
                SubmitButton(enabled = false, dark = false)
            }
        }
    }

    @Test
    fun stateWithOptions() {
        captureComposable {
            CreatePollScreen(dark = false) {
                QuestionSection(dark = false, questionText = "What's your favorite programming language?")
                OptionsSection(dark = false, options = listOf("Kotlin", "Swift"))
                SubmitButton(enabled = true, dark = false)
            }
        }
    }

    @Test
    fun stateSubmitting() {
        captureComposable {
            CreatePollScreen(dark = false) {
                QuestionSection(dark = false, questionText = "What's your favorite programming language?")
                OptionsSection(dark = false, options = listOf("Kotlin", "Swift"))
                SubmitButton(enabled = true, dark = false, showProgress = true)
            }
        }
    }

    @Test
    fun stateError() {
        captureComposable {
            CreatePollScreen(dark = false) {
                QuestionSection(dark = false, questionText = "What's your favorite programming language?")
                OptionsSection(dark = false, options = listOf("Kotlin", "Swift"))
                ErrorMessage("Failed to create poll. Please try again.")
                SubmitButton(enabled = true, dark = false)
            }
        }
    }

    // ==================== Dark Theme ====================

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        captureComposable {
            CreatePollScreen(dark = true) {
                QuestionSection(dark = true)
                OptionsSection(dark = true, options = listOf("", ""))
                SubmitButton(enabled = false, dark = true)
            }
        }
    }

    // ==================== Style ====================

    @Test
    fun styleCustomBackground() {
        val customBg = Color(0xFFF5F5DC)
        captureComposable {
            CreatePollScreen(dark = false, bgColor = customBg) {
                QuestionSection(dark = false)
                OptionsSection(dark = false, options = listOf("", ""))
                SubmitButton(enabled = false, dark = false)
            }
        }
    }

    // ==================== Visibility Toggles ====================

    @Test
    fun visibilityNoToolbar() {
        captureComposable {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BG_COLOR)
                    .padding(16.dp)
            ) {
                QuestionSection(dark = false)
                OptionsSection(dark = false, options = listOf("", ""))
                SubmitButton(enabled = false, dark = false)
            }
        }
    }

    // ==================== Content Variants ====================

    @Test
    fun contentManyOptions() {
        captureComposable {
            CreatePollScreen(dark = false) {
                QuestionSection(dark = false, questionText = "Which day works best for the sprint retrospective?")
                OptionsSection(dark = false, options = listOf("Monday", "Wednesday", "Friday"))
                SubmitButton(enabled = true, dark = false)
            }
        }
    }

    // ==================== Helper: Capture ====================

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

    // ==================== Composable Builders ====================

    @Composable
    private fun CreatePollScreen(
        dark: Boolean,
        bgColor: Color? = null,
        content: @Composable () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor ?: if (dark) BG_COLOR_DARK else BG_COLOR)
        ) {
            Toolbar(dark = dark)
            Divider(color = if (dark) SEPARATOR_COLOR_DARK else SEPARATOR_COLOR, thickness = 1.dp)
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }

    @Composable
    private fun Toolbar(dark: Boolean) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = if (dark) ICON_TINT_DARK else ICON_TINT,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Create Poll",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY,
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun QuestionSection(dark: Boolean, questionText: String? = null) {
        Column {
            Text(
                text = "Question",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, if (dark) INPUT_BORDER_DARK else INPUT_BORDER, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = questionText ?: "Ask a question",
                    fontSize = 14.sp,
                    color = if (questionText != null) {
                        if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY
                    } else TEXT_HINT
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    @Composable
    private fun OptionsSection(dark: Boolean, options: List<String>) {
        Column {
            Text(
                text = "Set the answers",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY
            )
            Spacer(modifier = Modifier.height(8.dp))
            options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Drag",
                        tint = if (dark) ICON_TINT_DARK else ICON_TINT,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (dark) INPUT_BORDER_DARK else INPUT_BORDER, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = option.ifEmpty { "Option ${index + 1}" },
                            fontSize = 14.sp,
                            color = if (option.isNotEmpty()) {
                                if (dark) TEXT_PRIMARY_DARK else TEXT_PRIMARY
                            } else TEXT_HINT
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    @Composable
    private fun SubmitButton(enabled: Boolean, dark: Boolean, showProgress: Boolean = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (enabled) BUTTON_ENABLED
                    else if (dark) BUTTON_DISABLED_DARK else BUTTON_DISABLED
                ),
            contentAlignment = Alignment.Center
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Create Poll",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    private fun ErrorMessage(message: String) {
        Text(
            text = message,
            fontSize = 12.sp,
            color = ERROR_COLOR,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
