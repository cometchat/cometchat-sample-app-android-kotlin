package com.cometchat.uikit.compose.presentation.shared.shimmer.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.CometChatShimmerConfig

/**
 * Modifier that applies an animated shimmer effect to any composable.
 *
 * This modifier creates a continuous shimmer animation that sweeps across the composable,
 * matching the visual appearance of the chatuikit-kotlin shimmer implementation.
 *
 * The shimmer effect uses hardware-accelerated rendering via [graphicsLayer] for optimal
 * performance, and supports all four [ShimmerDirection] values.
 *
 * Usage:
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .size(100.dp, 16.dp)
 *         .shimmer(
 *             config = CometChatShimmerConfig.default(),
 *             shape = RoundedCornerShape(4.dp)
 *         )
 * )
 * ```
 *
 * @param config Shimmer configuration containing colors, direction, shape, and animation settings.
 *               Defaults to [CometChatShimmerConfig.default] which sources colors from CometChatTheme.
 * @param shape Shape to clip the shimmer effect. Defaults to [RectangleShape].
 * @return Modifier with shimmer effect applied
 */
fun Modifier.shimmer(
    config: CometChatShimmerConfig,
    shape: Shape = RectangleShape
): Modifier = composed {
    val brush = rememberShimmerBrush(config)

    this
        .graphicsLayer {
            // Enable hardware acceleration for smooth animation
            // This creates a separate render layer for the shimmer effect
        }
        .clip(shape)
        .shimmerBackground(brush)
}

/**
 * Modifier that applies an animated shimmer effect using the shared shimmer brush if available.
 *
 * When used within a [ProvideShimmerAnimation] block, this modifier will use the shared
 * brush for synchronized animation across multiple elements. Otherwise, it creates a new
 * independent shimmer animation.
 *
 * This is useful for list items where all shimmer placeholders should animate together.
 *
 * Usage:
 * ```kotlin
 * ProvideShimmerAnimation(config = CometChatShimmerConfig.default()) {
 *     Column {
 *         // All these boxes will animate in sync
 *         Box(modifier = Modifier.size(48.dp).shimmerWithSharedAnimation(CircleShape))
 *         Box(modifier = Modifier.size(100.dp, 16.dp).shimmerWithSharedAnimation())
 *     }
 * }
 * ```
 *
 * @param shape Shape to clip the shimmer effect. Defaults to [RectangleShape].
 * @param config Optional shimmer configuration. If null, uses the shared config from
 *               [ProvideShimmerAnimation] or falls back to [CometChatShimmerConfig.default].
 * @return Modifier with shimmer effect applied
 */
fun Modifier.shimmerWithSharedAnimation(
    shape: Shape = RectangleShape,
    config: CometChatShimmerConfig? = null
): Modifier = composed {
    val brush = useShimmerBrush(config)

    this
        .graphicsLayer {
            // Enable hardware acceleration for smooth animation
        }
        .clip(shape)
        .shimmerBackground(brush)
}

/**
 * Internal modifier that applies a brush as background.
 * Separated for reusability and testing.
 */
private fun Modifier.shimmerBackground(brush: Brush): Modifier {
    return this.drawWithCache {
        onDrawBehind {
            drawRect(brush)
        }
    }
}
