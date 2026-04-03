package com.cometchat.sampleapp.compose.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * CometChat brand colors.
 *
 * These colors are used as the primary palette for the sample app
 * and can be customized to match your brand.
 */
private val CometChatPrimary = Color(0xFF6851D6)
private val CometChatPrimaryVariant = Color(0xFF5A45C0)
private val CometChatSecondary = Color(0xFF03DAC6)

/**
 * Light color scheme for the sample app.
 */
private val LightColorScheme = lightColorScheme(
    primary = CometChatPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E0FF),
    onPrimaryContainer = Color(0xFF1F0057),
    secondary = CometChatSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFCEFAF8),
    onSecondaryContainer = Color(0xFF00201F),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

/**
 * Dark color scheme for the sample app.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCFBCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF003738),
    secondaryContainer = Color(0xFF004F50),
    onSecondaryContainer = Color(0xFF70F7FF),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

/**
 * Sample App theme composable.
 *
 * This theme wraps the app content with both Material 3 theming and
 * CometChat UIKit theming. It supports:
 * - Light and dark mode
 * - Dynamic colors on Android 12+ (optional)
 * - CometChat component styling
 *
 * ## Theme Customization:
 * To customize the theme, modify the color schemes above or create
 * custom CometChat color schemes.
 *
 * ## Usage:
 * ```kotlin
 * @Composable
 * fun App() {
 *     SampleAppTheme {
 *         // Your app content
 *         AppNavGraph()
 *     }
 * }
 * ```
 *
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use dynamic colors on Android 12+ (defaults to false)
 * @param content The composable content to wrap with the theme
 *
 * Validates: Requirements 13.1, 13.3, 13.5
 */
@Composable
fun SampleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Set to false by default to use CometChat brand colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Wrap with Material 3 theme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = {
            // Wrap with CometChat theme for UIKit components
            CometChatTheme {
                content()
            }
        }
    )
}
