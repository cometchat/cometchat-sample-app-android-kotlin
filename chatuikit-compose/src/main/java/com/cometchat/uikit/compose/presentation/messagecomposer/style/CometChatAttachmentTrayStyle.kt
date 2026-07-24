package com.cometchat.uikit.compose.presentation.messagecomposer.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Style configuration for
 * [com.cometchat.uikit.compose.presentation.messagecomposer.ui.CometChatAttachmentTray].
 *
 * The tray is the composer strip that shows staged attachment tiles. It is only visible while ≥1
 * tile is staged.
 *
 * Use [default] to create an instance backed by CometChat theme tokens.
 *
 * @property backgroundColor Background of the whole tray strip.
 * @property contentPadding Padding around the row of tiles.
 * @property tileSpacing Horizontal gap between tiles.
 */
@Immutable
data class CometChatAttachmentTrayStyle(
    val backgroundColor: Color,
    val contentPadding: PaddingValues,
    val tileSpacing: Dp
) {
    companion object {

        /** Creates a default tray style using CometChat theme tokens. */
        @Composable
        fun default(
            backgroundColor: Color = Color.Transparent,
            contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            tileSpacing: Dp = 8.dp
        ): CometChatAttachmentTrayStyle = CometChatAttachmentTrayStyle(
            backgroundColor = backgroundColor,
            contentPadding = contentPadding,
            tileSpacing = tileSpacing
        )
    }
}
