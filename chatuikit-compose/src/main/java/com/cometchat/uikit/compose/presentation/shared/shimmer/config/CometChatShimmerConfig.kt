package com.cometchat.uikit.compose.presentation.shared.shimmer.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Configuration for shimmer animation and appearance.
 * Matches properties from chatuikit-kotlin CometChatShimmer.
 *
 * @property baseColor The background color of the shimmer placeholder
 * @property highlightColor The brighter color that sweeps across the shimmer (creates the "shine" effect)
 * @property direction The direction in which the shimmer animation travels
 * @property shape The gradient shape used for the shimmer effect (linear or radial)
 * @property animationDuration Duration of one shimmer animation cycle in milliseconds
 * @property tilt Angle in degrees for the shimmer gradient tilt
 * @property intensity Controls the highlight width (0.0f = narrow, higher = wider)
 * @property dropOff Controls gradient falloff (how quickly the highlight fades)
 * @property widthRatio Ratio of shimmer width relative to the view width (1.0f = full width)
 * @property heightRatio Ratio of shimmer height relative to the view height (1.0f = full height)
 */
@Immutable
data class CometChatShimmerConfig(
    val baseColor: Color,
    val highlightColor: Color,
    val direction: ShimmerDirection,
    val shape: ShimmerShape,
    val animationDuration: Long,
    val tilt: Float,
    val intensity: Float,
    val dropOff: Float,
    val widthRatio: Float,
    val heightRatio: Float
) {
    init {
        require(animationDuration > 0) { "Animation duration must be positive, was: $animationDuration" }
        require(intensity >= 0f) { "Intensity must be non-negative, was: $intensity" }
        require(dropOff >= 0f) { "DropOff must be non-negative, was: $dropOff" }
        require(widthRatio > 0f) { "Width ratio must be positive, was: $widthRatio" }
        require(heightRatio > 0f) { "Height ratio must be positive, was: $heightRatio" }
    }

    companion object {
        /**
         * Default animation duration in milliseconds.
         * Matches chatuikit-kotlin CometChatShimmer default.
         */
        const val DEFAULT_ANIMATION_DURATION = 1000L

        /**
         * Default tilt angle in degrees.
         * Matches chatuikit-kotlin CometChatShimmer default.
         */
        const val DEFAULT_TILT = 20f

        /**
         * Default intensity value.
         * Matches chatuikit-kotlin CometChatShimmer default.
         */
        const val DEFAULT_INTENSITY = 0f

        /**
         * Default dropOff value.
         * Matches chatuikit-kotlin CometChatShimmer default.
         */
        const val DEFAULT_DROP_OFF = 0.5f

        /**
         * Default width ratio.
         * Matches chatuikit-kotlin CometChatShimmer default.
         */
        const val DEFAULT_WIDTH_RATIO = 1f

        /**
         * Default height ratio.
         * Matches chatuikit-kotlin CometChatShimmer default.
         */
        const val DEFAULT_HEIGHT_RATIO = 1f

        /**
         * Minimum allowed animation duration in milliseconds.
         */
        const val MIN_ANIMATION_DURATION = 100L

        /**
         * Minimum allowed ratio value.
         */
        const val MIN_RATIO = 0.1f

        /**
         * Creates a default CometChatShimmerConfig with values sourced from CometChatTheme.
         * All values match the chatuikit-kotlin implementation defaults.
         *
         * @param baseColor The background color of the shimmer placeholder.
         *                  Defaults to CometChatTheme.colorScheme.backgroundColor3
         * @param highlightColor The highlight color that sweeps across.
         *                       Defaults to CometChatTheme.colorScheme.backgroundColor2
         * @param direction The direction of the shimmer animation.
         *                  Defaults to LEFT_TO_RIGHT
         * @param shape The gradient shape (linear or radial).
         *              Defaults to LINEAR
         * @param animationDuration Duration of one animation cycle in milliseconds.
         *                          Defaults to 1000ms. Values <= 0 are coerced to 100ms.
         * @param tilt Angle in degrees for the gradient tilt.
         *             Defaults to 20 degrees
         * @param intensity Controls the highlight width.
         *                  Defaults to 0.0f. Negative values are coerced to 0.
         * @param dropOff Controls gradient falloff.
         *                Defaults to 0.5f. Negative values are coerced to 0.
         * @param widthRatio Ratio of shimmer width.
         *                   Defaults to 1.0f. Values <= 0 are coerced to 0.1f.
         * @param heightRatio Ratio of shimmer height.
         *                    Defaults to 1.0f. Values <= 0 are coerced to 0.1f.
         * @return A configured CometChatShimmerConfig instance
         */
        @Composable
        fun default(
            baseColor: Color = CometChatTheme.colorScheme.backgroundColor3,
            highlightColor: Color = CometChatTheme.colorScheme.backgroundColor2,
            direction: ShimmerDirection = ShimmerDirection.LEFT_TO_RIGHT,
            shape: ShimmerShape = ShimmerShape.LINEAR,
            animationDuration: Long = DEFAULT_ANIMATION_DURATION,
            tilt: Float = DEFAULT_TILT,
            intensity: Float = DEFAULT_INTENSITY,
            dropOff: Float = DEFAULT_DROP_OFF,
            widthRatio: Float = DEFAULT_WIDTH_RATIO,
            heightRatio: Float = DEFAULT_HEIGHT_RATIO
        ): CometChatShimmerConfig = CometChatShimmerConfig(
            baseColor = baseColor,
            highlightColor = highlightColor,
            direction = direction,
            shape = shape,
            animationDuration = animationDuration.coerceAtLeast(MIN_ANIMATION_DURATION),
            tilt = tilt,
            intensity = intensity.coerceAtLeast(0f),
            dropOff = dropOff.coerceAtLeast(0f),
            widthRatio = widthRatio.coerceAtLeast(MIN_RATIO),
            heightRatio = heightRatio.coerceAtLeast(MIN_RATIO)
        )
    }
}
