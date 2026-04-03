package com.cometchat.uikit.compose.presentation.shared.defaultstates

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.CometChatShimmerConfig
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * Immutable style configuration for CometChatLoadingState.
 * Contains all visual styling properties for the loading state component.
 *
 * Now includes [shimmerConfig] for animated shimmer effect configuration.
 * The [shimmerColor] property is maintained for backward compatibility and
 * is derived from [shimmerConfig.baseColor].
 *
 * @property backgroundColor Background color of the loading state container
 * @property itemCount Number of shimmer placeholder items to display
 * @property avatarSize Size of the avatar placeholder
 * @property avatarShape Shape of the avatar placeholder (e.g., CircleShape)
 * @property placeholderCornerRadius Corner radius for rectangular placeholders
 * @property shimmerConfig Configuration for shimmer animation (colors, direction, duration, etc.)
 */
@Immutable
data class CometChatLoadingStateStyle(
    val backgroundColor: Color,
    val itemCount: Int,
    val avatarSize: Dp,
    val avatarShape: Shape,
    val placeholderCornerRadius: Dp,
    val shimmerConfig: CometChatShimmerConfig
) {
    /**
     * Backward compatibility property.
     * Returns the base color from shimmerConfig for existing code that uses shimmerColor.
     */
    val shimmerColor: Color
        get() = shimmerConfig.baseColor

    companion object {
        /**
         * Creates a default style configuration sourcing values from CometChatTheme.
         * Does NOT use Material Theme colors directly.
         *
         * @param backgroundColor Background color of the container.
         *                        Defaults to CometChatTheme.colorScheme.backgroundColor1
         * @param shimmerColor Deprecated: Use shimmerConfig.baseColor instead.
         *                     If provided, this will be used as the shimmerConfig.baseColor.
         *                     Defaults to null (uses shimmerConfig default).
         * @param itemCount Number of shimmer items to display. Defaults to 8.
         *                  Values less than 1 are coerced to 1.
         * @param avatarSize Size of the avatar placeholder. Defaults to 48.dp
         * @param avatarShape Shape of the avatar placeholder. Defaults to CircleShape
         * @param placeholderCornerRadius Corner radius for rectangular placeholders. Defaults to 4.dp
         * @param shimmerConfig Configuration for shimmer animation.
         *                      Defaults to CometChatShimmerConfig.default()
         * @return A configured CometChatLoadingStateStyle instance
         */
        @Composable
        fun default(
            backgroundColor: Color = CometChatTheme.colorScheme.backgroundColor1,
            shimmerColor: Color? = null,
            itemCount: Int = 8,
            avatarSize: Dp = 48.dp,
            avatarShape: Shape = CircleShape,
            placeholderCornerRadius: Dp = 4.dp,
            shimmerConfig: CometChatShimmerConfig = CometChatShimmerConfig.default()
        ): CometChatLoadingStateStyle {
            // If shimmerColor is explicitly provided, create a new config with that base color
            // This maintains backward compatibility with existing code
            val effectiveShimmerConfig = if (shimmerColor != null) {
                CometChatShimmerConfig.default(baseColor = shimmerColor)
            } else {
                shimmerConfig
            }

            return CometChatLoadingStateStyle(
                backgroundColor = backgroundColor,
                itemCount = itemCount.coerceAtLeast(1), // Ensure at least 1 item
                avatarSize = avatarSize,
                avatarShape = avatarShape,
                placeholderCornerRadius = placeholderCornerRadius,
                shimmerConfig = effectiveShimmerConfig
            )
        }
    }
}
