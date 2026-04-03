package com.cometchat.uikit.compose.presentation.shared.shimmer.style

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.CometChatShimmerConfig

/**
 * Style configuration for list item shimmer placeholder.
 *
 * This style class defines the appearance of shimmer placeholders used in list items
 * such as conversations, users, and groups. It matches the layout structure from
 * chatuikit-kotlin's shimmer_list_base.xml.
 *
 * Reference dimensions from shimmer_list_base.xml:
 * - Avatar: 48dp circle
 * - Title: 60% width, ~16dp height
 * - Subtitle: 80% width, ~12dp height
 * - Trailing: optional, ~40dp width
 *
 * @property avatarSize Size of the avatar placeholder. Defaults to 48.dp
 * @property avatarShape Shape of the avatar placeholder. Defaults to CircleShape
 * @property titleWidth Width of the title placeholder as a fraction of available width (0.0f to 1.0f).
 *                      Defaults to 0.6f (60%)
 * @property titleHeight Height of the title placeholder. Defaults to 16.dp
 * @property subtitleWidth Width of the subtitle placeholder as a fraction of available width (0.0f to 1.0f).
 *                         Defaults to 0.8f (80%)
 * @property subtitleHeight Height of the subtitle placeholder. Defaults to 12.dp
 * @property showTrailingPlaceholder Whether to show the trailing placeholder. Defaults to true
 * @property trailingWidth Width of the trailing placeholder. Defaults to 40.dp
 * @property trailingHeight Height of the trailing placeholder. Defaults to 12.dp
 * @property placeholderCornerRadius Corner radius for rectangular placeholders. Defaults to 4.dp
 * @property horizontalPadding Horizontal padding for the list item. Defaults to 16.dp
 * @property verticalPadding Vertical padding for the list item. Defaults to 12.dp
 * @property contentSpacing Spacing between avatar and content. Defaults to 12.dp
 * @property titleSubtitleSpacing Spacing between title and subtitle. Defaults to 8.dp
 * @property shimmerConfig Shimmer animation configuration
 */
@Immutable
data class CometChatListItemShimmerStyle(
    val avatarSize: Dp,
    val avatarShape: Shape,
    val titleWidth: Float,
    val titleHeight: Dp,
    val subtitleWidth: Float,
    val subtitleHeight: Dp,
    val showTrailingPlaceholder: Boolean,
    val trailingWidth: Dp,
    val trailingHeight: Dp,
    val placeholderCornerRadius: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val contentSpacing: Dp,
    val titleSubtitleSpacing: Dp,
    val shimmerConfig: CometChatShimmerConfig
) {
    init {
        require(titleWidth in 0f..1f) { "titleWidth must be between 0 and 1, was: $titleWidth" }
        require(subtitleWidth in 0f..1f) { "subtitleWidth must be between 0 and 1, was: $subtitleWidth" }
    }

    companion object {
        /**
         * Default avatar size matching chatuikit-kotlin shimmer_list_base.xml.
         */
        val DEFAULT_AVATAR_SIZE = 48.dp

        /**
         * Default title width as fraction of available width.
         */
        const val DEFAULT_TITLE_WIDTH = 0.6f

        /**
         * Default title height.
         */
        val DEFAULT_TITLE_HEIGHT = 16.dp

        /**
         * Default subtitle width as fraction of available width.
         */
        const val DEFAULT_SUBTITLE_WIDTH = 0.8f

        /**
         * Default subtitle height.
         */
        val DEFAULT_SUBTITLE_HEIGHT = 12.dp

        /**
         * Default trailing placeholder width.
         */
        val DEFAULT_TRAILING_WIDTH = 60.dp

        /**
         * Default trailing placeholder height.
         */
        val DEFAULT_TRAILING_HEIGHT = 22.dp

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
         * Default spacing between title and subtitle.
         */
        val DEFAULT_TITLE_SUBTITLE_SPACING = 8.dp

        /**
         * Creates a default CometChatListItemShimmerStyle with values matching
         * chatuikit-kotlin shimmer_list_base.xml layout.
         *
         * @param avatarSize Size of the avatar placeholder. Defaults to 48.dp
         * @param avatarShape Shape of the avatar placeholder. Defaults to CircleShape
         * @param titleWidth Width of the title placeholder as a fraction (0.0f to 1.0f). Defaults to 0.6f
         * @param titleHeight Height of the title placeholder. Defaults to 16.dp
         * @param subtitleWidth Width of the subtitle placeholder as a fraction (0.0f to 1.0f). Defaults to 0.8f
         * @param subtitleHeight Height of the subtitle placeholder. Defaults to 12.dp
         * @param showTrailingPlaceholder Whether to show trailing placeholder. Defaults to true
         * @param trailingWidth Width of the trailing placeholder. Defaults to 40.dp
         * @param trailingHeight Height of the trailing placeholder. Defaults to 12.dp
         * @param placeholderCornerRadius Corner radius for rectangular placeholders. Defaults to 4.dp
         * @param horizontalPadding Horizontal padding. Defaults to 16.dp
         * @param verticalPadding Vertical padding. Defaults to 12.dp
         * @param contentSpacing Spacing between avatar and content. Defaults to 12.dp
         * @param titleSubtitleSpacing Spacing between title and subtitle. Defaults to 8.dp
         * @param shimmerConfig Shimmer animation configuration. Defaults to CometChatShimmerConfig.default()
         * @return A configured CometChatListItemShimmerStyle instance
         */
        @Composable
        fun default(
            avatarSize: Dp = DEFAULT_AVATAR_SIZE,
            avatarShape: Shape = CircleShape,
            titleWidth: Float = DEFAULT_TITLE_WIDTH,
            titleHeight: Dp = DEFAULT_TITLE_HEIGHT,
            subtitleWidth: Float = DEFAULT_SUBTITLE_WIDTH,
            subtitleHeight: Dp = DEFAULT_SUBTITLE_HEIGHT,
            showTrailingPlaceholder: Boolean = true,
            trailingWidth: Dp = DEFAULT_TRAILING_WIDTH,
            trailingHeight: Dp = DEFAULT_TRAILING_HEIGHT,
            placeholderCornerRadius: Dp = DEFAULT_PLACEHOLDER_CORNER_RADIUS,
            horizontalPadding: Dp = DEFAULT_HORIZONTAL_PADDING,
            verticalPadding: Dp = DEFAULT_VERTICAL_PADDING,
            contentSpacing: Dp = DEFAULT_CONTENT_SPACING,
            titleSubtitleSpacing: Dp = DEFAULT_TITLE_SUBTITLE_SPACING,
            shimmerConfig: CometChatShimmerConfig = CometChatShimmerConfig.default()
        ): CometChatListItemShimmerStyle = CometChatListItemShimmerStyle(
            avatarSize = avatarSize,
            avatarShape = avatarShape,
            titleWidth = titleWidth.coerceIn(0f, 1f),
            titleHeight = titleHeight,
            subtitleWidth = subtitleWidth.coerceIn(0f, 1f),
            subtitleHeight = subtitleHeight,
            showTrailingPlaceholder = showTrailingPlaceholder,
            trailingWidth = trailingWidth,
            trailingHeight = trailingHeight,
            placeholderCornerRadius = placeholderCornerRadius,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            contentSpacing = contentSpacing,
            titleSubtitleSpacing = titleSubtitleSpacing,
            shimmerConfig = shimmerConfig
        )
    }
}
