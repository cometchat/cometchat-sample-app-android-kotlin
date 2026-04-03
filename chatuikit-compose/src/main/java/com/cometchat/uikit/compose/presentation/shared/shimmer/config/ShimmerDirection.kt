package com.cometchat.uikit.compose.presentation.shared.shimmer.config

/**
 * Direction in which the shimmer animation travels.
 * Integer values match chatuikit-kotlin CometChatShimmer.Direction for consistency.
 *
 * @property value The integer value matching the kotlin implementation
 */
enum class ShimmerDirection(val value: Int) {
    /**
     * Shimmer animation travels from left to right.
     * Value: 0 (matches CometChatShimmer.Direction.LEFT_TO_RIGHT)
     */
    LEFT_TO_RIGHT(0),

    /**
     * Shimmer animation travels from top to bottom.
     * Value: 1 (matches CometChatShimmer.Direction.TOP_TO_BOTTOM)
     */
    TOP_TO_BOTTOM(1),

    /**
     * Shimmer animation travels from right to left.
     * Value: 2 (matches CometChatShimmer.Direction.RIGHT_TO_LEFT)
     */
    RIGHT_TO_LEFT(2),

    /**
     * Shimmer animation travels from bottom to top.
     * Value: 3 (matches CometChatShimmer.Direction.BOTTOM_TO_TOP)
     */
    BOTTOM_TO_TOP(3);

    companion object {
        /**
         * Returns the ShimmerDirection corresponding to the given integer value.
         * Defaults to LEFT_TO_RIGHT if the value doesn't match any direction.
         *
         * @param value The integer value to convert
         * @return The corresponding ShimmerDirection
         */
        fun fromValue(value: Int): ShimmerDirection {
            return entries.find { it.value == value } ?: LEFT_TO_RIGHT
        }
    }
}
