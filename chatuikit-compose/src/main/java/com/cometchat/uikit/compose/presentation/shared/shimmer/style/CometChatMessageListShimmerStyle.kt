package com.cometchat.uikit.compose.presentation.shared.shimmer.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.CometChatShimmerConfig

/**
 * Style configuration for message list shimmer placeholder.
 *
 * This style class defines the appearance of shimmer placeholders used in message lists.
 * It matches the layout structure from chatuikit-kotlin's cometchat_shimmer_message_list.xml.
 *
 * Reference dimensions from cometchat_shimmer_message_list.xml:
 * - Bubble height: 53dp
 * - Bubble corner radius: 12dp (from shimmer_gradient_rect drawable)
 * - Varying margins: 70dp, 120dp, 150dp, 160dp for different bubble widths
 * - Padding: 3dp (cometchat_padding_3)
 *
 * @property itemCount Number of message bubble placeholders to display. Defaults to 10
 * @property bubbleCornerRadius Corner radius for bubble placeholders. Defaults to 12.dp
 * @property minBubbleWidth Minimum bubble width as a fraction of available width (0.0f to 1.0f).
 *                          Defaults to 0.3f (30%)
 * @property maxBubbleWidth Maximum bubble width as a fraction of available width (0.0f to 1.0f).
 *                          Defaults to 0.7f (70%)
 * @property bubbleHeight Height of each bubble placeholder. Defaults to 53.dp
 * @property verticalPadding Vertical padding between bubbles. Defaults to 3.dp
 * @property horizontalPadding Horizontal padding for the message list. Defaults to 3.dp
 * @property shimmerConfig Shimmer animation configuration
 */
@Immutable
data class CometChatMessageListShimmerStyle(
    val itemCount: Int,
    val bubbleCornerRadius: Dp,
    val minBubbleWidth: Float,
    val maxBubbleWidth: Float,
    val bubbleHeight: Dp,
    val verticalPadding: Dp,
    val horizontalPadding: Dp,
    val shimmerConfig: CometChatShimmerConfig
) {
    init {
        require(itemCount > 0) { "itemCount must be positive, was: $itemCount" }
        require(minBubbleWidth in 0f..1f) { "minBubbleWidth must be between 0 and 1, was: $minBubbleWidth" }
        require(maxBubbleWidth in 0f..1f) { "maxBubbleWidth must be between 0 and 1, was: $maxBubbleWidth" }
        require(minBubbleWidth <= maxBubbleWidth) { "minBubbleWidth must be <= maxBubbleWidth" }
    }

    companion object {
        /**
         * Default number of message bubble placeholders.
         */
        const val DEFAULT_ITEM_COUNT = 10

        /**
         * Default corner radius for bubble placeholders.
         */
        val DEFAULT_BUBBLE_CORNER_RADIUS = 12.dp

        /**
         * Default minimum bubble width as fraction of available width.
         */
        const val DEFAULT_MIN_BUBBLE_WIDTH = 0.3f

        /**
         * Default maximum bubble width as fraction of available width.
         */
        const val DEFAULT_MAX_BUBBLE_WIDTH = 0.7f

        /**
         * Default bubble height matching chatuikit-kotlin (53dp).
         */
        val DEFAULT_BUBBLE_HEIGHT = 53.dp

        /**
         * Default vertical padding between bubbles.
         */
        val DEFAULT_VERTICAL_PADDING = 3.dp

        /**
         * Default horizontal padding for the message list.
         */
        val DEFAULT_HORIZONTAL_PADDING = 3.dp

        /**
         * Creates a default CometChatMessageListShimmerStyle with values matching
         * chatuikit-kotlin cometchat_shimmer_message_list.xml layout.
         *
         * @param itemCount Number of bubble placeholders. Defaults to 10
         * @param bubbleCornerRadius Corner radius for bubbles. Defaults to 12.dp
         * @param minBubbleWidth Minimum bubble width as fraction (0.0f to 1.0f). Defaults to 0.3f
         * @param maxBubbleWidth Maximum bubble width as fraction (0.0f to 1.0f). Defaults to 0.7f
         * @param bubbleHeight Height of each bubble. Defaults to 53.dp
         * @param verticalPadding Vertical padding between bubbles. Defaults to 3.dp
         * @param horizontalPadding Horizontal padding. Defaults to 3.dp
         * @param shimmerConfig Shimmer animation configuration
         * @return A configured CometChatMessageListShimmerStyle instance
         */
        @Composable
        fun default(
            itemCount: Int = DEFAULT_ITEM_COUNT,
            bubbleCornerRadius: Dp = DEFAULT_BUBBLE_CORNER_RADIUS,
            minBubbleWidth: Float = DEFAULT_MIN_BUBBLE_WIDTH,
            maxBubbleWidth: Float = DEFAULT_MAX_BUBBLE_WIDTH,
            bubbleHeight: Dp = DEFAULT_BUBBLE_HEIGHT,
            verticalPadding: Dp = DEFAULT_VERTICAL_PADDING,
            horizontalPadding: Dp = DEFAULT_HORIZONTAL_PADDING,
            shimmerConfig: CometChatShimmerConfig = CometChatShimmerConfig.default()
        ): CometChatMessageListShimmerStyle = CometChatMessageListShimmerStyle(
            itemCount = itemCount.coerceAtLeast(1),
            bubbleCornerRadius = bubbleCornerRadius,
            minBubbleWidth = minBubbleWidth.coerceIn(0f, 1f),
            maxBubbleWidth = maxBubbleWidth.coerceIn(0f, 1f),
            bubbleHeight = bubbleHeight,
            verticalPadding = verticalPadding,
            horizontalPadding = horizontalPadding,
            shimmerConfig = shimmerConfig
        )
    }
}
