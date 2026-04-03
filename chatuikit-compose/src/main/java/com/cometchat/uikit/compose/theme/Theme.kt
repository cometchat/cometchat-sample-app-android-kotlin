package com.cometchat.uikit.compose.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

@Composable
fun CometChatTheme(
    colorScheme: CometChatColorScheme = CometChatTheme.colorScheme,
    shapes: Shapes = CometChatTheme.shapes,
    typography: CometChatTypography = CometChatTheme.typography,
    content: @Composable () -> Unit) {

    val rippleIndication = ripple()
    val selectionColors = remember(colorScheme.primary) {
        TextSelectionColors(
            handleColor = colorScheme.primary,
            backgroundColor = colorScheme.primary.copy(alpha = 0.4f)
        )
    }

    // Provide all theme values through CompositionLocalProvider
    CompositionLocalProvider(
        LocalColorScheme provides colorScheme,
        LocalIndication provides rippleIndication,
        LocalShapes provides shapes,
        LocalTextSelectionColors provides selectionColors,
        LocalTypography provides typography)
    {
        ProvideTextStyle(typography.titleRegular, content)
    }

}


