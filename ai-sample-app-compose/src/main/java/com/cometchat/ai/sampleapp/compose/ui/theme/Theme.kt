package com.cometchat.ai.sampleapp.compose.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Application theme for the AI Assistant Sample App (Compose).
 *
 * Wraps the app content with both Material3 + the CometChat Compose theme
 * so that all UIKit components inherit CometChat tokens.
 */
private val CometChatPrimary = Color(0xFF6851D6)
private val CometChatPrimaryVariant = Color(0xFF5A45C0)

private val LightColors = lightColorScheme(
    primary = CometChatPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E0FF),
    onPrimaryContainer = Color(0xFF1F0057),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCFBCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)

@Composable
fun AIAssistantSampleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        val cometColors = if (darkTheme) {
            com.cometchat.uikit.compose.theme.darkColorScheme()
        } else {
            com.cometchat.uikit.compose.theme.lightColorScheme()
        }
        CometChatTheme(colorScheme = cometColors) {
            content()
        }
    }
}
