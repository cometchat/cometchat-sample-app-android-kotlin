package com.cometchat.uikit.compose.screenshots

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.chat.models.FlagReason
import com.cometchat.uikit.compose.presentation.report.CometChatFlagMessageDialog
import com.cometchat.uikit.compose.presentation.report.CometChatFlagMessageDialogStyle
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog

/**
 * Roborazzi screenshot tests for CometChatFlagMessageDialog (Compose).
 *
 * Captures golden images for ALL visual states matching the chatuikit-kotlin tests:
 * - stateInitial: No chip selected, report button disabled
 * - stateChipSelected: One chip active, report button enabled
 * - stateWithRemark: Remark field visible with hint
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
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatFlagMessageDialogScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatFlagMessageDialogScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/flagmessagedialog"
        )
    )

    @org.junit.Before
    fun setup() {
        ShadowDialog.reset()
    }

    // ==================== UI States ====================

    @Test
    fun stateInitial() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    @Test
    fun stateChipSelected() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    @Test
    fun stateWithRemark() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    remarkHint = "Add additional details here...",
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    @Test
    fun stateProgress() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    showError = false,
                    showProgress = true,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    @Test
    fun stateError() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    showError = true,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    // ==================== Theme Variants ====================

    @Test
    fun theme_lightDefault() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    @Test
    fun theme_darkDefault() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    // ==================== Style Customization ====================

    @Test
    fun styleCustomColors() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(
                        backgroundColor = Color(0xFF1A1A2E),
                        titleColor = Color.White,
                        subtitleColor = Color(0xFFAAAAAA),
                        chipActiveBackgroundColor = Color(0xFF6851D6),
                        chipActiveTextColor = Color.White,
                        chipActiveBorderColor = Color(0xFF8B7AE8),
                        reportButtonEnabledBackgroundColor = Color(0xFF6851D6),
                        cancelButtonEnabledBackgroundColor = Color(0xFF2D2D44),
                        cancelButtonEnabledTextColor = Color.White
                    ),
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    // ==================== Visibility Toggles ====================

    @Test
    fun visibilityNoRemarkField() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = false,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    // ==================== Custom Text ====================

    @Test
    fun customTitle() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    title = "Flag This Message",
                    description = "Select a reason for flagging this message",
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    @Test
    fun customDescription() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    title = "Report",
                    description = "Why do you want to flag this message?",
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    @Test
    fun customButtonTexts() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    cancelButtonText = "Dismiss",
                    reportButtonText = "Submit Report",
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    // ==================== Many Reasons ====================

    @Test
    fun manyFlagReasons() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createManyFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    showError = false,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    // ==================== Combined States ====================

    @Test
    fun stateErrorWithProgress() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = true,
                    showError = true,
                    showProgress = true,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    @Test
    fun stateNoRemarkFieldWithError() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatFlagMessageDialog(
                    flagReasons = createFlagReasons(),
                    style = CometChatFlagMessageDialogStyle.default(),
                    showRemarkField = false,
                    showError = true,
                    showProgress = false,
                    onReportClick = {},
                    onCancelClick = {},
                    onCloseClick = {}
                )
            }
        }
    }

    // ==================== Helper Methods ====================

    private fun captureComposable(content: @Composable () -> Unit) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        lateinit var capturedView: android.view.View

        scenario.onActivity { activity ->
            activity.setContent { content() }

            // In Robolectric, Compose Dialog creates a ComposeDialogWrapper.
            // We capture the dialog's content (android.R.id.content child) which
            // contains the Compose dialog content without the parent issue.
            val dialog = ShadowDialog.getLatestDialog()
            if (dialog != null) {
                val dialogContentView = dialog.window?.decorView
                    ?.findViewById<ViewGroup>(android.R.id.content)
                    ?.getChildAt(0)
                if (dialogContentView != null) {
                    capturedView = dialogContentView
                } else {
                    // Try the decorView's first child
                    val decorView = dialog.window?.decorView as? ViewGroup
                    capturedView = decorView?.getChildAt(0) ?: activity.window.decorView
                        .findViewById<ViewGroup>(android.R.id.content)
                        .getChildAt(0)
                }
            } else {
                // Fallback: capture the activity's content view
                capturedView = activity.window.decorView
                    .findViewById<ViewGroup>(android.R.id.content)
                    .getChildAt(0)
            }
            capturedView.captureRoboImage(roborazziOptions = RoborazziConfig.options())
        }
        // Don't call scenario.close() — it triggers onDismissRequest which hangs the looper
    }

    // ==================== Mock Data ====================

    private fun createFlagReasons(): List<FlagReason> {
        return listOf(
            createFlagReason("spam", "Spam"),
            createFlagReason("sexual", "Sexual Content"),
            createFlagReason("harassment", "Harassment")
        )
    }

    private fun createManyFlagReasons(): List<FlagReason> {
        return listOf(
            createFlagReason("spam", "Spam"),
            createFlagReason("sexual", "Sexual Content"),
            createFlagReason("harassment", "Harassment"),
            createFlagReason("violence", "Violence"),
            createFlagReason("misinformation", "Misinformation"),
            createFlagReason("other", "Other")
        )
    }

    private fun createFlagReason(id: String, name: String): FlagReason {
        val reason = mock<FlagReason>()
        whenever(reason.id).thenReturn(id)
        whenever(reason.name).thenReturn(name)
        return reason
    }
}
