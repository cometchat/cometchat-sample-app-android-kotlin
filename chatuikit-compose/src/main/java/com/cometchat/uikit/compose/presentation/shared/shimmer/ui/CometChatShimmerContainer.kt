package com.cometchat.uikit.compose.presentation.shared.shimmer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.CometChatShimmerConfig
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.ProvideShimmerAnimation
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.rememberShimmerBrush

/**
 * Container that applies shimmer effect over its children.
 * Uses a single shared animation for all children.
 *
 * This composable provides a container that can apply a shimmer overlay effect
 * over its content. All child composables using [useShimmerBrush] within this
 * container will share the same animation state, ensuring synchronized shimmer
 * effects across multiple elements.
 *
 * The container uses hardware layer rendering via [graphicsLayer] for optimal
 * performance during animation.
 *
 * Usage:
 * ```kotlin
 * // Basic usage with shimmer overlay
 * CometChatShimmerContainer(
 *     isShimmerVisible = isLoading
 * ) {
 *     // Content that will have shimmer overlay when loading
 *     Column {
 *         CometChatShimmerBox(width = 48.dp, height = 48.dp, shape = CircleShape)
 *         CometChatShimmerBox(width = 200.dp, height = 16.dp)
 *     }
 * }
 *
 * // Custom configuration
 * CometChatShimmerContainer(
 *     isShimmerVisible = true,
 *     config = CometChatShimmerConfig.default(
 *         direction = ShimmerDirection.TOP_TO_BOTTOM
 *     )
 * ) {
 *     // Content
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the container
 * @param isShimmerVisible Whether to show the shimmer overlay. When false, only the content
 *                         is rendered without the shimmer effect. Defaults to true
 * @param config Shimmer configuration containing colors, direction, shape, and animation settings.
 *               Defaults to [CometChatShimmerConfig.default] which sources colors from CometChatTheme
 * @param accessibilityLabel Content description for accessibility when shimmer is visible.
 *                           Defaults to "Loading content"
 * @param content Child composables that will share the shimmer animation
 */
@Composable
fun CometChatShimmerContainer(
    modifier: Modifier = Modifier,
    isShimmerVisible: Boolean = true,
    config: CometChatShimmerConfig = CometChatShimmerConfig.default(),
    accessibilityLabel: String = "Loading content",
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                // Enable hardware acceleration for smooth animation
                // This creates a separate render layer for the shimmer effect
            }
            .semantics {
                liveRegion = LiveRegionMode.Polite
                if (isShimmerVisible) {
                    contentDescription = accessibilityLabel
                }
            }
    ) {
        // Provide shared animation state to children
        ProvideShimmerAnimation(config = config) {
            content()
        }

        // Render shimmer overlay when visible
        if (isShimmerVisible) {
            val brush = rememberShimmerBrush(config)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        // Hardware acceleration for overlay
                        alpha = 0.5f
                    }
                    .background(brush)
            )
        }
    }
}
