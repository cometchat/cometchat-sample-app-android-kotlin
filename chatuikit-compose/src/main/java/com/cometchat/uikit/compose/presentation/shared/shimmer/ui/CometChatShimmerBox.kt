package com.cometchat.uikit.compose.presentation.shared.shimmer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.CometChatShimmerConfig
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.useShimmerBrush

/**
 * Simple shimmer placeholder box composable.
 *
 * This composable renders a Box with an animated shimmer effect applied,
 * matching the visual appearance of the chatuikit-kotlin shimmer implementation.
 * It can be used to create basic loading placeholders with customizable size and shape.
 *
 * When used within a [ProvideShimmerAnimation] block, the shimmer animation will be
 * synchronized with other shimmer elements in the same container.
 *
 * Usage:
 * ```kotlin
 * // Basic usage with default config
 * CometChatShimmerBox(
 *     width = 100.dp,
 *     height = 16.dp
 * )
 *
 * // Circular shimmer placeholder
 * CometChatShimmerBox(
 *     width = 48.dp,
 *     height = 48.dp,
 *     shape = CircleShape
 * )
 *
 * // Custom configuration
 * CometChatShimmerBox(
 *     width = 200.dp,
 *     height = 24.dp,
 *     shape = RoundedCornerShape(8.dp),
 *     config = CometChatShimmerConfig.default(
 *         animationDuration = 1500
 *     )
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the shimmer box
 * @param width Width of the shimmer box. Defaults to 100.dp
 * @param height Height of the shimmer box. Defaults to 16.dp
 * @param shape Shape of the shimmer box (CircleShape, RoundedCornerShape, etc.).
 *              Defaults to RoundedCornerShape(4.dp)
 * @param config Shimmer configuration containing colors, direction, shape, and animation settings.
 *               If null, uses the shared config from [ProvideShimmerAnimation] or falls back to
 *               [CometChatShimmerConfig.default]
 * @param accessibilityLabel Content description for accessibility. Defaults to "Loading placeholder"
 */
@Composable
fun CometChatShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(4.dp),
    config: CometChatShimmerConfig? = null,
    accessibilityLabel: String = "Loading placeholder"
) {
    val brush = useShimmerBrush(config)

    Box(
        modifier = modifier
            .size(width, height)
            .graphicsLayer {
                // Enable hardware acceleration for smooth animation
                // This creates a separate render layer for the shimmer effect
            }
            .clip(shape)
            .background(brush)
            .semantics {
                contentDescription = accessibilityLabel
            }
    )
}
