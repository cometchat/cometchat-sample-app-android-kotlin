package com.cometchat.uikit.compose.presentation.shared.shimmer.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.CometChatShimmerConfig
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.ShimmerDirection
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.ShimmerShape
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * CompositionLocal for sharing shimmer brush across multiple composables.
 * When provided, all composables using [useShimmerBrush] will share the same animation.
 */
val LocalShimmerBrush = compositionLocalOf<Brush?> { null }

/**
 * CompositionLocal for sharing shimmer configuration across multiple composables.
 */
val LocalShimmerConfig = compositionLocalOf<CometChatShimmerConfig?> { null }

/**
 * Creates and remembers an animated shimmer brush.
 * Uses [rememberInfiniteTransition] for continuous animation with [LinearEasing].
 *
 * The brush animates a gradient sweep effect that matches the chatuikit-kotlin
 * shimmer implementation. The gradient colors and positions are calculated based
 * on the configuration's intensity and dropOff values.
 *
 * @param config Shimmer configuration containing colors, direction, shape, and animation settings
 * @return Animated [Brush] for use with Modifier.background()
 */
@Composable
fun rememberShimmerBrush(
    config: CometChatShimmerConfig = CometChatShimmerConfig.default()
): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_transition")

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = config.animationDuration.toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    return remember(shimmerProgress, config) {
        when (config.shape) {
            ShimmerShape.LINEAR -> createLinearShimmerBrush(shimmerProgress, config)
            ShimmerShape.RADIAL -> createRadialShimmerBrush(shimmerProgress, config)
        }
    }
}

/**
 * Creates a linear gradient shimmer brush.
 *
 * The gradient colors follow the pattern: [baseColor, highlightColor, highlightColor, baseColor]
 * matching the chatuikit-kotlin CometChatShimmer.updateColors() for LINEAR shape.
 *
 * @param progress Animation progress from 0f to 1f
 * @param config Shimmer configuration
 * @return Linear gradient [Brush]
 */
internal fun createLinearShimmerBrush(
    progress: Float,
    config: CometChatShimmerConfig
): Brush {
    // Calculate gradient positions based on intensity and dropOff
    // Matches chatuikit-kotlin CometChatShimmer.updatePositions() for LINEAR shape
    val positions = calculateLinearPositions(config.intensity, config.dropOff)

    // Colors pattern for LINEAR: [baseColor, highlightColor, highlightColor, baseColor]
    val colors = listOf(
        config.baseColor,
        config.highlightColor,
        config.highlightColor,
        config.baseColor
    )

    val (startOffset, endOffset) = calculateGradientOffsets(progress, config)

    return Brush.linearGradient(
        colorStops = positions.zip(colors).toTypedArray(),
        start = startOffset,
        end = endOffset
    )
}

/**
 * Creates a radial gradient shimmer brush.
 *
 * The gradient colors follow the pattern: [highlightColor, highlightColor, baseColor, baseColor]
 * matching the chatuikit-kotlin CometChatShimmer.updateColors() for RADIAL shape.
 *
 * @param progress Animation progress from 0f to 1f
 * @param config Shimmer configuration
 * @return Radial gradient [Brush]
 */
internal fun createRadialShimmerBrush(
    progress: Float,
    config: CometChatShimmerConfig
): Brush {
    // Calculate gradient positions based on intensity and dropOff
    // Matches chatuikit-kotlin CometChatShimmer.updatePositions() for RADIAL shape
    val positions = calculateRadialPositions(config.intensity, config.dropOff)

    // Colors pattern for RADIAL: [highlightColor, highlightColor, baseColor, baseColor]
    val colors = listOf(
        config.highlightColor,
        config.highlightColor,
        config.baseColor,
        config.baseColor
    )

    // For radial gradient, we animate the center position
    val (centerOffset, _) = calculateGradientOffsets(progress, config)

    // Calculate radius based on a reference size (using a large value for full coverage)
    // The radius calculation matches chatuikit-kotlin: max(width, height) / sqrt(2)
    val referenceSize = 1000f
    val radius = (referenceSize / sqrt(2.0)).toFloat() * max(config.widthRatio, config.heightRatio)

    return Brush.radialGradient(
        colorStops = positions.zip(colors).toTypedArray(),
        center = centerOffset,
        radius = radius
    )
}

/**
 * Calculates gradient positions for LINEAR shape.
 * Matches chatuikit-kotlin CometChatShimmer.updatePositions() for LINEAR shape.
 *
 * @param intensity Controls the highlight width (0.0f = narrow, higher = wider)
 * @param dropOff Controls gradient falloff (how quickly the highlight fades)
 * @return Array of 4 position values
 */
internal fun calculateLinearPositions(intensity: Float, dropOff: Float): List<Float> {
    return listOf(
        max((1f - intensity - dropOff) / 2f, 0f),
        max((1f - intensity - 0.001f) / 2f, 0f),
        min((1f + intensity + 0.001f) / 2f, 1f),
        min((1f + intensity + dropOff) / 2f, 1f)
    )
}

/**
 * Calculates gradient positions for RADIAL shape.
 * Matches chatuikit-kotlin CometChatShimmer.updatePositions() for RADIAL shape.
 *
 * @param intensity Controls the highlight width
 * @param dropOff Controls gradient falloff
 * @return Array of 4 position values
 */
internal fun calculateRadialPositions(intensity: Float, dropOff: Float): List<Float> {
    return listOf(
        0f,
        min(intensity, 1f),
        min(intensity + dropOff, 1f),
        1f
    )
}

/**
 * Calculates gradient start and end offsets based on animation progress and direction.
 * Matches the translation logic from chatuikit-kotlin CometChatShimmerDrawable.draw().
 *
 * @param progress Animation progress from 0f to 1f
 * @param config Shimmer configuration
 * @return Pair of (startOffset, endOffset) for the gradient
 */
internal fun calculateGradientOffsets(
    progress: Float,
    config: CometChatShimmerConfig
): Pair<Offset, Offset> {
    // Use a reference size for calculating offsets
    // In actual usage, this will be relative to the composable size
    val referenceWidth = 1000f * config.widthRatio
    val referenceHeight = 1000f * config.heightRatio

    // Calculate tilt tangent for offset calculation
    val tiltRadians = Math.toRadians(config.tilt.toDouble())
    val tiltTan = tan(tiltRadians).toFloat()

    // Calculate translation distances based on direction
    // Matches chatuikit-kotlin CometChatShimmerDrawable.draw()
    val translateWidth = referenceWidth + tiltTan * referenceHeight
    val translateHeight = referenceHeight + tiltTan * referenceWidth

    return when (config.direction) {
        ShimmerDirection.LEFT_TO_RIGHT -> {
            val dx = offset(-translateWidth, translateWidth, progress)
            Offset(dx, 0f) to Offset(dx + referenceWidth, referenceHeight)
        }
        ShimmerDirection.RIGHT_TO_LEFT -> {
            val dx = offset(translateWidth, -translateWidth, progress)
            Offset(dx + referenceWidth, 0f) to Offset(dx, referenceHeight)
        }
        ShimmerDirection.TOP_TO_BOTTOM -> {
            val dy = offset(-translateHeight, translateHeight, progress)
            Offset(0f, dy) to Offset(referenceWidth, dy + referenceHeight)
        }
        ShimmerDirection.BOTTOM_TO_TOP -> {
            val dy = offset(translateHeight, -translateHeight, progress)
            Offset(0f, dy + referenceHeight) to Offset(referenceWidth, dy)
        }
    }
}

/**
 * Interpolates between start and end values based on progress.
 * Matches chatuikit-kotlin CometChatShimmerDrawable.offset().
 *
 * @param start Starting value
 * @param end Ending value
 * @param percent Progress percentage (0f to 1f)
 * @return Interpolated value
 */
private fun offset(start: Float, end: Float, percent: Float): Float {
    return start + (end - start) * percent
}

/**
 * Provides a shared shimmer animation state for multiple composables.
 * All composables using [useShimmerBrush] within this provider will animate in sync.
 *
 * This is useful for containers like [CometChatLoadingState] where multiple
 * shimmer elements should have synchronized animation.
 *
 * @param config Shimmer configuration to use for all children
 * @param content Child composables that will share the animation
 */
@Composable
fun ProvideShimmerAnimation(
    config: CometChatShimmerConfig = CometChatShimmerConfig.default(),
    content: @Composable () -> Unit
) {
    val brush = rememberShimmerBrush(config)

    CompositionLocalProvider(
        LocalShimmerBrush provides brush,
        LocalShimmerConfig provides config
    ) {
        content()
    }
}

/**
 * Uses the shared shimmer brush if available, otherwise creates a new one.
 *
 * When called within a [ProvideShimmerAnimation] block, returns the shared brush
 * for synchronized animation. Otherwise, creates a new independent brush.
 *
 * @param config Shimmer configuration (used only if no shared brush is available)
 * @return Shimmer [Brush] for use with Modifier.background()
 */
@Composable
fun useShimmerBrush(
    config: CometChatShimmerConfig? = null
): Brush {
    val sharedBrush = LocalShimmerBrush.current
    val sharedConfig = LocalShimmerConfig.current

    return sharedBrush ?: rememberShimmerBrush(config ?: sharedConfig ?: CometChatShimmerConfig.default())
}
