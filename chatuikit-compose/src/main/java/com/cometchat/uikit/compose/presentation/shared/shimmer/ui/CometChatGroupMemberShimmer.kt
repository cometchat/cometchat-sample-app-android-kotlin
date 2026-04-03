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
import com.cometchat.uikit.compose.presentation.shared.shimmer.style.CometChatGroupMemberShimmerStyle
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.useShimmerBrush

/**
 * Pre-built shimmer placeholder for group member lists.
 *
 * This composable renders a row with avatar (circle), name placeholder, and scope badge
 * placeholder with an animated shimmer effect. It matches the layout structure from
 * chatuikit-kotlin's cometchat_group_member_shimmer.xml.
 *
 * When used within a [ProvideShimmerAnimation] block, the shimmer animation will be
 * synchronized with other shimmer elements in the same container.
 *
 * Layout structure:
 * ```
 * [Avatar] [Name placeholder........] [Scope Badge]
 * ```
 *
 * Usage:
 * ```kotlin
 * // Basic usage with default style
 * CometChatGroupMemberShimmer()
 *
 * // Custom style
 * CometChatGroupMemberShimmer(
 *     style = CometChatGroupMemberShimmerStyle.default(
 *         avatarSize = 56.dp,
 *         scopeBadgeWidth = 80.dp
 *     )
 * )
 *
 * // Within a shared animation container for multiple items
 * ProvideShimmerAnimation {
 *     Column {
 *         repeat(5) {
 *             CometChatGroupMemberShimmer()
 *         }
 *     }
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the group member shimmer
 * @param style Style configuration for the shimmer placeholder.
 *              Defaults to [CometChatGroupMemberShimmerStyle.default]
 */
@Composable
fun CometChatGroupMemberShimmer(
    modifier: Modifier = Modifier,
    style: CometChatGroupMemberShimmerStyle = CometChatGroupMemberShimmerStyle.default()
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
                contentDescription = "Loading group member"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder (circle)
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

        // Name placeholder (60% width)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(style.nameWidth)
                .height(style.nameHeight)
                .graphicsLayer { }
                .clip(RoundedCornerShape(style.placeholderCornerRadius))
                .background(brush)
        )

        Spacer(modifier = Modifier.width(style.contentSpacing))

        // Scope badge placeholder on trailing side
        Box(
            modifier = Modifier
                .width(style.scopeBadgeWidth)
                .height(style.scopeBadgeHeight)
                .graphicsLayer { }
                .clip(RoundedCornerShape(style.placeholderCornerRadius))
                .background(brush)
        )
    }
}
