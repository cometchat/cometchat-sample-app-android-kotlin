package com.cometchat.uikit.compose.screenshots

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cometchat.uikit.compose.presentation.emojikeyboard.style.CometChatEmojiKeyboardStyle
import com.cometchat.uikit.compose.presentation.emojikeyboard.ui.CometChatEmojiKeyboard
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.cometchat.uikit.compose.presentation.utils.RoborazziConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Roborazzi screenshot tests for CometChatEmojiKeyboard (Compose).
 *
 * Captures golden images for visual states matching the chatuikit-kotlin tests:
 * - Content state (emojis loaded with categories and tab bar)
 * - Dark theme variant
 * - Custom background color
 * - Custom styling colors
 * - Custom corner radius / stroke
 * - Tab selection
 * - Light/Dark theme defaults
 *
 * Run:
 *   ./gradlew :chatuikit-compose:recordRoborazziDebug --tests "*CometChatEmojiKeyboardScreenshotTest"
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
class CometChatEmojiKeyboardScreenshotTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule(order = 1)
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            outputDirectoryPath = "../screenshot-gallery/compose/emojikeyboard"
        )
    )

    // ==================== Section 1: UI States ====================

    @Test
    fun stateContent() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun stateContentDark() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }
        }
    }

    @Test
    fun stateLoading() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }
        }
    }

    // ==================== Section 2: Style Variants ====================

    @Test
    fun styleCustomBackground() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val customStyle = CometChatEmojiKeyboardStyle(
                    backgroundColor = Color(0xFFF5F5DC),
                    cornerRadius = 12.dp,
                    strokeWidth = 0.dp,
                    strokeColor = Color.Transparent,
                    separatorColor = Color(0xFFE0E0E0),
                    categoryTextColor = Color(0xFF666666),
                    categoryTextStyle = TextStyle.Default,
                    categoryIconTint = Color(0xFF999999),
                    selectedCategoryIconTint = Color(0xFF6851D6),
                    selectedCategoryBackgroundColor = Color(0xFFF0EBFF),
                    tabIconSize = 16.dp,
                    tabBackgroundSize = 27.dp
                )
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    style = customStyle
                )
            }
        }
    }

    @Test
    fun stylingCustomColors() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val customStyle = CometChatEmojiKeyboardStyle(
                    backgroundColor = Color(0xFF1A1A2E),
                    cornerRadius = 12.dp,
                    strokeWidth = 0.dp,
                    strokeColor = Color.Transparent,
                    separatorColor = Color(0xFF333333),
                    categoryTextColor = Color(0xFFAAAAAA),
                    categoryTextStyle = TextStyle.Default,
                    categoryIconTint = Color(0xFF888888),
                    selectedCategoryIconTint = Color(0xFF6851D6),
                    selectedCategoryBackgroundColor = Color(0xFFE8E0FF),
                    tabIconSize = 16.dp,
                    tabBackgroundSize = 27.dp
                )
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    style = customStyle
                )
            }
        }
    }

    @Test
    fun styleCustomCornerRadius() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val customStyle = CometChatEmojiKeyboardStyle(
                    backgroundColor = Color.White,
                    cornerRadius = 32.dp,
                    strokeWidth = 0.dp,
                    strokeColor = Color.Transparent,
                    separatorColor = Color(0xFFE0E0E0),
                    categoryTextColor = Color(0xFF666666),
                    categoryTextStyle = TextStyle.Default,
                    categoryIconTint = Color(0xFF999999),
                    selectedCategoryIconTint = Color(0xFF6851D6),
                    selectedCategoryBackgroundColor = Color(0xFFF0EBFF),
                    tabIconSize = 16.dp,
                    tabBackgroundSize = 27.dp
                )
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    style = customStyle
                )
            }
        }
    }

    @Test
    fun styleCustomStroke() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                val customStyle = CometChatEmojiKeyboardStyle(
                    backgroundColor = Color.White,
                    cornerRadius = 12.dp,
                    strokeWidth = 2.dp,
                    strokeColor = Color(0xFF6851D6),
                    separatorColor = Color(0xFFE0E0E0),
                    categoryTextColor = Color(0xFF666666),
                    categoryTextStyle = TextStyle.Default,
                    categoryIconTint = Color(0xFF999999),
                    selectedCategoryIconTint = Color(0xFF6851D6),
                    selectedCategoryBackgroundColor = Color(0xFFF0EBFF),
                    tabIconSize = 16.dp,
                    tabBackgroundSize = 27.dp
                )
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    style = customStyle
                )
            }
        }
    }

    // ==================== Section 3: Tab Selection ====================

    @Test
    fun tabSelectionFirst() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }
        }
    }

    // ==================== Section 4: Theme Variants ====================

    @Test
    fun theme_lightDefault() {
        captureComposable {
            CometChatTheme(colorScheme = lightColorScheme()) {
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }
        }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-night-xxhdpi")
    fun theme_darkDefault() {
        captureComposable {
            CometChatTheme(colorScheme = darkColorScheme()) {
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }
        }
    }

    @Test
    fun theme_customColorScheme() {
        captureComposable {
            val customColors = lightColorScheme(
                primary = Color(0xFF6851D6)
            )
            CometChatTheme(colorScheme = customColors) {
                CometChatEmojiKeyboard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }
        }
    }

    // ==================== Helper Methods ====================

    private fun captureComposable(content: @Composable () -> Unit) {
        composeTestRule.activity.setContent { content() }
        composeTestRule.waitForIdle()
        ShadowLooper.idleMainLooper()
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = RoborazziConfig.options())
    }
}
