package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Wrapper composable that applies row-level spacing around a message bubble,
 * matching the Java reference's cometchat_left/right/center_message_row.xml layouts.
 *
 * - **LEFT**: 16dp horizontal padding, 8dp vertical padding, start-aligned content.
 * - **RIGHT**: 16dp horizontal padding, 8dp vertical padding, end-aligned content
 *   with an additional 50dp start padding on the bubble to prevent full-width outgoing bubbles.
 * - **CENTER**: 16dp horizontal padding, 8dp vertical padding, center-aligned content.
 *
 * The highlight background is applied at this level (outside the padding) to ensure
 * the highlight covers the full width of the row, matching the Java implementation
 * where the highlight is applied to the parent LinearLayout.
 *
 * @param alignment The message alignment (LEFT, RIGHT, CENTER)
 * @param modifier Modifier for the outer wrapper Box
 * @param highlightColor The background color for highlighting (Color.Transparent when not highlighted)
 * @param content The bubble content to wrap
 */
@Composable
internal fun MessageBubbleWrapper(
    alignment: UIKitConstants.MessageBubbleAlignment,
    modifier: Modifier = Modifier,
    highlightColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    when (alignment) {
        UIKitConstants.MessageBubbleAlignment.LEFT -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(highlightColor)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                content()
            }
        }
        UIKitConstants.MessageBubbleAlignment.RIGHT -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(highlightColor)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(modifier = Modifier.padding(start = 50.dp)) {
                    content()
                }
            }
        }
        UIKitConstants.MessageBubbleAlignment.CENTER -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(highlightColor)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}
