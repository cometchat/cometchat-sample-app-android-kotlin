package com.cometchat.uikit.compose.presentation.shared.shimmer.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.CometChatShimmerConfig

/**
 * Style configuration for group member shimmer placeholder.
 *
 * This style class defines the appearance of shimmer placeholders used in group member lists.
 * It matches the layout structure from chatuikit-kotlin's cometchat_group_member_shimmer.xml.
 *
 * Reference dimensions from cometchat_group_member_shimmer.xml:
 * - Avatar: 48dp circle
 * - Name: weight 2 (approximately 60% of remaining width)
 * - Scope badge: weight 1 (approximately 40% of remaining width), 22dp height
 * - Padding: 4dp horizontal (cometchat_padding_4), 3dp vertical (cometchat_padding_3)
 *
 * @property avatarSize Size of the avatar placeholder. Defaults to 48.dp
 * @property nameWidth Width of the name placeholder as a fraction of available width (0.0f to 1.0f).
 *                     Defaults to 0.6f (60%)
 * @property nameHeight Height of the name placeholder. Defaults to 22.dp
 * @property scopeBadgeWidth Width of the scope badge placeholder. Defaults to 60.dp
 * @property scopeBadgeHeight Height of the scope badge placeholder. Defaults to 22.dp
 * @property placeholderCornerRadius Corner radius for rectangular placeholders. Defaults to 4.dp
 * @property horizontalPadding Horizontal padding for the list item. Defaults to 16.dp
 * @property verticalPadding Vertical padding for the list item. Defaults to 12.dp
 * @property contentSpacing Spacing between avatar and content. Defaults to 12.dp
 * @property shimmerConfig Shimmer animation configuration
 */
@Immutable
data class CometChatGroupMemberShimmerStyle(
    val avatarSize: Dp,
    val nameWidth: Float,
    val nameHeight: Dp,
    val scopeBadgeWidth: Dp,
    val scopeBadgeHeight: Dp,
    val placeholderCornerRadius: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val contentSpacing: Dp,
    val shimmerConfig: CometChatShimmerConfig
) {
    init {
        require(nameWidth in 0f..1f) { "nameWidth must be between 0 and 1, was: $nameWidth" }
    }

    companion object {
        /**
         * Default avatar size matching chatuikit-kotlin cometchat_group_member_shimmer.xml.
         */
        val DEFAULT_AVATAR_SIZE = 48.dp

        /**
         * Default name width as fraction of available width.
         */
        const val DEFAULT_NAME_WIDTH = 0.6f

        /**
         * Default name height matching chatuikit-kotlin (22dp).
         */
        val DEFAULT_NAME_HEIGHT = 22.dp

        /**
         * Default scope badge width.
         */
        val DEFAULT_SCOPE_BADGE_WIDTH = 60.dp

        /**
         * Default scope badge height matching chatuikit-kotlin (22dp).
         */
        val DEFAULT_SCOPE_BADGE_HEIGHT = 22.dp

        /**
         * Default corner radius for rectangular placeholders.
         */
        val DEFAULT_PLACEHOLDER_CORNER_RADIUS = 4.dp

        /**
         * Default horizontal padding.
         */
        val DEFAULT_HORIZONTAL_PADDING = 16.dp

        /**
         * Default vertical padding.
         */
        val DEFAULT_VERTICAL_PADDING = 12.dp

        /**
         * Default spacing between avatar and content.
         */
        val DEFAULT_CONTENT_SPACING = 12.dp

        /**
         * Creates a default CometChatGroupMemberShimmerStyle with values matching
         * chatuikit-kotlin cometchat_group_member_shimmer.xml layout.
         *
         * @param avatarSize Size of the avatar placeholder. Defaults to 48.dp
         * @param nameWidth Width of the name placeholder as fraction (0.0f to 1.0f). Defaults to 0.6f
         * @param nameHeight Height of the name placeholder. Defaults to 22.dp
         * @param scopeBadgeWidth Width of the scope badge placeholder. Defaults to 60.dp
         * @param scopeBadgeHeight Height of the scope badge placeholder. Defaults to 22.dp
         * @param placeholderCornerRadius Corner radius for rectangular placeholders. Defaults to 4.dp
         * @param horizontalPadding Horizontal padding. Defaults to 16.dp
         * @param verticalPadding Vertical padding. Defaults to 12.dp
         * @param contentSpacing Spacing between avatar and content. Defaults to 12.dp
         * @param shimmerConfig Shimmer animation configuration
         * @return A configured CometChatGroupMemberShimmerStyle instance
         */
        @Composable
        fun default(
            avatarSize: Dp = DEFAULT_AVATAR_SIZE,
            nameWidth: Float = DEFAULT_NAME_WIDTH,
            nameHeight: Dp = DEFAULT_NAME_HEIGHT,
            scopeBadgeWidth: Dp = DEFAULT_SCOPE_BADGE_WIDTH,
            scopeBadgeHeight: Dp = DEFAULT_SCOPE_BADGE_HEIGHT,
            placeholderCornerRadius: Dp = DEFAULT_PLACEHOLDER_CORNER_RADIUS,
            horizontalPadding: Dp = DEFAULT_HORIZONTAL_PADDING,
            verticalPadding: Dp = DEFAULT_VERTICAL_PADDING,
            contentSpacing: Dp = DEFAULT_CONTENT_SPACING,
            shimmerConfig: CometChatShimmerConfig = CometChatShimmerConfig.default()
        ): CometChatGroupMemberShimmerStyle = CometChatGroupMemberShimmerStyle(
            avatarSize = avatarSize,
            nameWidth = nameWidth.coerceIn(0f, 1f),
            nameHeight = nameHeight,
            scopeBadgeWidth = scopeBadgeWidth,
            scopeBadgeHeight = scopeBadgeHeight,
            placeholderCornerRadius = placeholderCornerRadius,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            contentSpacing = contentSpacing,
            shimmerConfig = shimmerConfig
        )
    }
}
