package com.cometchat.sampleapp.compose.push.ui

import android.app.Activity
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * A composable that handles system bar (status bar and navigation bar) appearance
 * based on the current theme.
 *
 * This effect configures the icon appearance (light/dark) based on the theme.
 * The system bars are transparent, allowing the app content to show through.
 *
 * @param isDarkTheme Whether the current theme is dark mode
 */
@Composable
fun SystemBarsEffect(
    isDarkTheme: Boolean
) {
    val view = LocalView.current
    
    if (view.isInEditMode) {
        return
    }

    val activity = view.context as? Activity
    if (activity == null) {
        Log.w("SystemBarsEffect", "Unable to obtain Activity context for system bar theming")
        return
    }

    SideEffect {
        val window = activity.window
        
        // Make system bars transparent so app content shows through
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Set the icon appearance based on theme
        // For light theme (light background), we need dark icons (isAppearanceLightStatusBars = true)
        // For dark theme (dark background), we need light icons (isAppearanceLightStatusBars = false)
        val windowInsetsController = WindowCompat.getInsetsController(window, view)
        windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme
        windowInsetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }
}

// Overload for backward compatibility
@Composable
fun SystemBarsEffect(
    isDarkTheme: Boolean,
    statusBarColor: androidx.compose.ui.graphics.Color,
    navigationBarColor: androidx.compose.ui.graphics.Color
) {
    // Ignore the color parameters and just use the theme-based approach
    SystemBarsEffect(isDarkTheme)
}
