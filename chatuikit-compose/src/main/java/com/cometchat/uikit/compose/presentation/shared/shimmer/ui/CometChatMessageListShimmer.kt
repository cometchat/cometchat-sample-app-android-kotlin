package com.cometchat.uikit.compose.presentation.shared.shimmer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cometchat.uikit.compose.presentation.shared.shimmer.style.CometChatMessageListShimmerStyle
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.useShimmerBrush

/**
 * Pre-built shimmer placeholder for message lists.
 *
 * This composable renders alternating left-aligned and right-aligned bubble placeholders
 * with an animated shimmer effect. It matches the layout structure from
 * chatuikit-kotlin's cometchat_shimmer_message_list.xml.
 *
 * The bubble widths vary to simulate different message lengths, creating a more
 * realistic loading state appearance. When used within a [ProvideShimmerAnimation]
 * block, the shimmer animation will be synchronized across all bubbles.
 *
 * Layout pattern (alternating alignment):
 * ```
 * [Bubble.................] (right-aligned, outgoing)
 *          [Bubble........] (left-aligned, incoming)
 * [Bubble.......] (right-aligned, outgoing)
 *    [Bubble..............] (left-aligned, incoming)
 * ```
 *
 * Usage:
 * ```kotlin
 * // Basic usage with default style
 * CometChatMessageListShimmer()
 *
 * // Custom style
 * CometChatMessageListShimmer(
 *     style = CometChatMessageListShimmerStyle.default(
 *         itemCount = 15,
 *         bubbleHeight = 60.dp
 *     )
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the message list shimmer
 * @param style Style configuration for the shimmer placeholder.
 *              Defaults to [CometChatMessageListShimmerStyle.default]
 */
@Composable
fun CometChatMessageListShimmer(
    modifier: Modifier = Modifier,
    style: CometChatMessageListShimmerStyle = CometChatMessageListShimmerStyle.default()
) {
    // Use shared shimmer brush for synchronized animation across all bubbles
    val brush = useShimmerBrush(style.shimmerConfig)

    // Pre-calculate bubble widths for consistent rendering
    // Uses a deterministic pattern based on index to vary widths
    val bubbleWidths = remember(style.itemCount, style.minBubbleWidth, style.maxBubbleWidth) {
        List(style.itemCount) { index ->
            calculateBubbleWidth(index, style.minBubbleWidth, style.maxBubbleWidth)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = style.horizontalPadding)
            .semantics {
                contentDescription = "Loading messages"
            }
    ) {
        repeat(style.itemCount) { index ->
            // Alternate alignment: even indices = right (outgoing), odd indices = left (incoming)
            val isOutgoing = index % 2 == 0
            val widthFraction = bubbleWidths[index]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = style.verticalPadding),
                horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(widthFraction)
                        .height(style.bubbleHeight)
                        .graphicsLayer {
                            // Enable hardware acceleration for smooth animation
                        }
                        .clip(RoundedCornerShape(style.bubbleCornerRadius))
                        .background(brush)
                )
            }
        }
    }
}

/**
 * Calculates bubble width for a given index to create varying widths.
 *
 * Uses a deterministic pattern based on the index to ensure consistent
 * rendering across recompositions while providing visual variety.
 *
 * @param index The bubble index
 * @param minWidth Minimum width as fraction (0.0f to 1.0f)
 * @param maxWidth Maximum width as fraction (0.0f to 1.0f)
 * @return Width fraction for the bubble
 */
private fun calculateBubbleWidth(index: Int, minWidth: Float, maxWidth: Float): Float {
    // Use a pattern that creates visual variety while being deterministic
    // Pattern inspired by chatuikit-kotlin margins: 70dp, 120dp, 150dp, 160dp
    val patterns = listOf(0.7f, 0.5f, 0.4f, 0.55f, 0.6f, 0.7f, 0.55f, 0.7f, 0.7f, 0.4f)
    val patternValue = patterns[index % patterns.size]
    
    // Scale the pattern value to fit within min/max range
    return minWidth + (maxWidth - minWidth) * patternValue
}
