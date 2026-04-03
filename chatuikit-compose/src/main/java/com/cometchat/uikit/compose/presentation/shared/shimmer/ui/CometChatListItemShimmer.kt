package com.cometchat.uikit.compose.presentation.shared.shimmer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cometchat.uikit.compose.presentation.shared.shimmer.style.CometChatListItemShimmerStyle
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.useShimmerBrush

/**
 * Pre-built shimmer placeholder for list items (conversations, users, groups).
 *
 * This composable renders a row with avatar, title, subtitle, and optional trailing
 * placeholders with an animated shimmer effect. It matches the layout structure from
 * chatuikit-kotlin's shimmer_list_base.xml.
 *
 * When used within a [ProvideShimmerAnimation] block, the shimmer animation will be
 * synchronized with other shimmer elements in the same container, ensuring all
 * placeholders animate together.
 *
 * Layout structure:
 * ```
 * [Avatar] [Title............] [Trailing]
 *          [Subtitle..........]
 * ```
 *
 * Usage:
 * ```kotlin
 * // Basic usage with default style
 * CometChatListItemShimmer()
 *
 * // Custom style
 * CometChatListItemShimmer(
 *     style = CometChatListItemShimmerStyle.default(
 *         avatarSize = 56.dp,
 *         showTrailingPlaceholder = false
 *     )
 * )
 *
 * // Within a shared animation container
 * ProvideShimmerAnimation {
 *     Column {
 *         repeat(5) {
 *             CometChatListItemShimmer()
 *         }
 *     }
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the list item shimmer
 * @param style Style configuration for the shimmer placeholder.
 *              Defaults to [CometChatListItemShimmerStyle.default]
 */
@Composable
fun CometChatListItemShimmer(
    modifier: Modifier = Modifier,
    style: CometChatListItemShimmerStyle = CometChatListItemShimmerStyle.default()
) {
    // Use shared shimmer brush for synchronized animation across all elements
    val brush = useShimmerBrush(style.shimmerConfig)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = style.horizontalPadding,
                vertical = style.verticalPadding
            )
            .semantics {
                contentDescription = "Loading list item"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(style.avatarSize)
                .graphicsLayer {
                    // Enable hardware acceleration for smooth animation
                }
                .clip(style.avatarShape)
                .background(brush)
        )

        Spacer(modifier = Modifier.width(style.contentSpacing))

        // Content column (title and subtitle)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(style.titleWidth)
                    .height(style.titleHeight)
                    .graphicsLayer { }
                    .clip(RoundedCornerShape(style.placeholderCornerRadius))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(style.titleSubtitleSpacing))

            // Subtitle placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(style.subtitleWidth)
                    .height(style.subtitleHeight)
                    .graphicsLayer { }
                    .clip(RoundedCornerShape(style.placeholderCornerRadius))
                    .background(brush)
            )
        }

        // Trailing placeholder (optional)
        if (style.showTrailingPlaceholder) {
            Spacer(modifier = Modifier.width(style.contentSpacing))

            Box(
                modifier = Modifier
                    .width(style.trailingWidth)
                    .height(style.trailingHeight)
                    .graphicsLayer { }
                    .clip(RoundedCornerShape(style.placeholderCornerRadius))
                    .background(brush)
            )
        }
    }
}
