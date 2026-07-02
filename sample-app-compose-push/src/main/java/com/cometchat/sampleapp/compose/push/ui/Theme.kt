package com.cometchat.sampleapp.compose.push.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cometchat.uikit.compose.theme.CometChatTheme
import com.cometchat.uikit.compose.theme.CometChatTypography
import com.cometchat.uikit.compose.theme.darkColorScheme
import com.cometchat.uikit.compose.theme.lightColorScheme


val lightScheme = lightColorScheme(
//    primary = Color.Yellow,
//    extendedPrimaryColor50 = Color.Blue
)

val darkScheme = darkColorScheme(
//    primary = Color.Blue
)

@Composable
fun AppTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Get the colors for dark or light theme
    val colors = if (isDarkTheme) {
        darkScheme // Dark theme colors
    } else {
        lightScheme // Light theme colors
    }

    val colorScheme = if (isDarkTheme) {
        androidx.compose.material3.darkColorScheme(onSurface = Color.Red)
    } else {
        androidx.compose.material3.lightColorScheme(onSurface = Color.Red)
    }

    MaterialTheme(colorScheme, typography = MaterialTheme.typography, content = content)

    CometChatTheme(
        colors,
        typography = CometChatTypography()
    ) {
        // Apply system bar theming based on current theme
        SystemBarsEffect(isDarkTheme = isDarkTheme)
        content()
    }
}
