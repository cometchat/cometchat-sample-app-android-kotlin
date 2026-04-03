package com.cometchat.uikit.compose.presentation.shared.shimmer.config

/**
 * Gradient shape used for the shimmer effect.
 * Integer values match chatuikit-kotlin CometChatShimmer.Shape for consistency.
 *
 * @property value The integer value matching the kotlin implementation
 */
enum class ShimmerShape(val value: Int) {
    /**
     * Linear gradient shimmer effect.
     * Value: 0 (matches CometChatShimmer.Shape.LINEAR)
     */
    LINEAR(0),

    /**
     * Radial gradient shimmer effect (circular gradient from center).
     * Value: 1 (matches CometChatShimmer.Shape.RADIAL)
     */
    RADIAL(1);

    companion object {
        /**
         * Returns the ShimmerShape corresponding to the given integer value.
         * Defaults to LINEAR if the value doesn't match any shape.
         *
         * @param value The integer value to convert
         * @return The corresponding ShimmerShape
         */
        fun fromValue(value: Int): ShimmerShape {
            return entries.find { it.value == value } ?: LINEAR
        }
    }
}
