package com.cometchat.uikit.compose.presentation.shared.defaultstates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.shimmer.style.CometChatListItemShimmerStyle
import com.cometchat.uikit.compose.presentation.shared.shimmer.ui.CometChatListItemShimmer
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.ProvideShimmerAnimation

/**
 * Reusable loading state composable for list components.
 * Displays animated shimmer effect placeholders representing loading content.
 *
 * This component uses [CometChatListItemShimmer] internally for each placeholder item,
 * wrapped in [ProvideShimmerAnimation] to ensure all items share a single synchronized
 * shimmer animation. This matches the visual appearance of chatuikit-kotlin's shimmer
 * implementation.
 *
 * The shimmer animation can be customized via [CometChatLoadingStateStyle.shimmerConfig],
 * which controls colors, direction, duration, and other animation properties.
 *
 * Usage:
 * ```kotlin
 * // Basic usage with default style
 * CometChatLoadingState()
 *
 * // Custom item count
 * CometChatLoadingState(
 *     style = CometChatLoadingStateStyle.default(itemCount = 5)
 * )
 *
 * // Custom shimmer configuration
 * CometChatLoadingState(
 *     style = CometChatLoadingStateStyle.default(
 *         shimmerConfig = CometChatShimmerConfig.default(
 *             animationDuration = 1500,
 *             direction = ShimmerDirection.TOP_TO_BOTTOM
 *         )
 *     )
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the component
 * @param style Style configuration for the component, including shimmer animation settings
 */
@Composable
fun CometChatLoadingState(
    modifier: Modifier = Modifier,
    style: CometChatLoadingStateStyle = CometChatLoadingStateStyle.default()
) {
    // Provide shared animation state for all shimmer items
    // This ensures all placeholders animate in sync
    ProvideShimmerAnimation(config = style.shimmerConfig) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(style.backgroundColor)
                .padding(horizontal = 16.dp)
                .semantics {
                    contentDescription = "Loading, please wait"
                    liveRegion = LiveRegionMode.Polite
                }
        ) {
            // Show shimmer items based on itemCount
            repeat(style.itemCount.coerceAtLeast(1)) {
                CometChatListItemShimmer(
                    style = CometChatListItemShimmerStyle.default(
                        avatarSize = style.avatarSize,
                        avatarShape = style.avatarShape,
                        placeholderCornerRadius = style.placeholderCornerRadius,
                        shimmerConfig = style.shimmerConfig
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
