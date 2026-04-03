package com.cometchat.uikit.compose.presentation.shared.shimmer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cometchat.uikit.compose.presentation.shared.shimmer.style.CometChatReactionListShimmerStyle
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.useShimmerBrush

/**
 * Pre-built shimmer placeholder for reaction lists.
 *
 * This composable renders a row with avatar (40dp circle), name placeholder (flexible width),
 * and action icon (24dp) with an animated shimmer effect. It matches the layout structure from
 * chatuikit-kotlin's shimmer_cometchat_reaction_list_items.xml.
 *
 * When used within a [ProvideShimmerAnimation] block, the shimmer animation will be
 * synchronized with other shimmer elements in the same container.
 *
 * Layout structure:
 * ```
 * [Avatar 40dp] [Name placeholder (flexible)...] [Action 24dp]
 * ```
 *
 * Usage:
 * ```kotlin
 * // Basic usage with default style
 * CometChatReactionListShimmer()
 *
 * // Custom style
 * CometChatReactionListShimmer(
 *     style = CometChatReactionListShimmerStyle.default(
 *         avatarSize = 48.dp,
 *         actionIconSize = 32.dp
 *     )
 * )
 *
 * // Within a shared animation container for multiple items
 * ProvideShimmerAnimation {
 *     Column {
 *         repeat(5) {
 *             CometChatReactionListShimmer()
 *         }
 *     }
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the reaction list shimmer
 * @param style Style configuration for the shimmer placeholder.
 *              Defaults to [CometChatReactionListShimmerStyle.default]
 */
@Composable
fun CometChatReactionListShimmer(
    modifier: Modifier = Modifier,
    style: CometChatReactionListShimmerStyle = CometChatReactionListShimmerStyle.default()
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
                contentDescription = "Loading reaction"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder (40dp circle)
        Box(
            modifier = Modifier
                .size(style.avatarSize)
                .graphicsLayer {
                    // Enable hardware acceleration for smooth animation
                }
                .clip(CircleShape)
                .background(brush)
        )

        Spacer(modifier = Modifier.width(style.contentSpacing))

        // Name placeholder (flexible width)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(style.nameHeight)
                .graphicsLayer { }
                .clip(RoundedCornerShape(style.placeholderCornerRadius))
                .background(brush)
        )

        Spacer(modifier = Modifier.width(style.contentSpacing))

        // Action icon placeholder (24dp circle) on trailing side
        Box(
            modifier = Modifier
                .size(style.actionIconSize)
                .graphicsLayer { }
                .clip(CircleShape)
                .background(brush)
        )
    }
}
