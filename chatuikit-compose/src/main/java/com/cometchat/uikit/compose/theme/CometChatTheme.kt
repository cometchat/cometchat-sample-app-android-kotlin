package com.cometchat.uikit.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

object CometChatTheme {

    val colorScheme: CometChatColorScheme
        @Composable @ReadOnlyComposable get() = LocalColorScheme.current

    val typography: CometChatTypography
        @Composable @ReadOnlyComposable get() = LocalTypography.current

    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = LocalShapes.current

}