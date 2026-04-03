package com.cometchat.uikit.compose.presentation.reactionlist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.Reaction
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.reactionlist.style.CometChatReactionListItemStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar

/**
 * ReactedUserItem displays a single user who reacted to a message.
 *
 * This composable shows:
 * - User's avatar on the left
 * - User's name (or "You" for the logged-in user) in the center
 * - "Tap to remove" subtitle for the logged-in user
 * - The emoji reaction on the right
 *
 * Features:
 * - Displays CometChatAvatar with user's avatar/name
 * - Shows "You" label for the logged-in user
 * - Shows "Tap to remove" subtitle for the logged-in user's reactions
 * - Displays the emoji in the tail view
 * - Handles click events for reaction removal
 * - Accessibility support with content descriptions
 * - Support for custom view slots (itemView, leadingView, titleView, subtitleView, trailingView)
 *
 * @param reaction The Reaction object containing user and emoji information
 * @param isCurrentUser Whether this reaction belongs to the logged-in user
 * @param onClick Callback invoked when the item is clicked
 * @param style Style configuration for the item
 * @param modifier Modifier applied to the container
 * @param itemView Custom composable to replace the entire item
 * @param leadingView Custom composable for the leading view (avatar area)
 * @param titleView Custom composable for the title view (user name)
 * @param subtitleView Custom composable for the subtitle view ("Tap to remove")
 * @param trailingView Custom composable for the trailing view (emoji)
 */
@Composable
internal fun ReactedUserItem(
    reaction: Reaction,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    style: CometChatReactionListItemStyle,
    modifier: Modifier = Modifier,
    itemView: (@Composable (Reaction) -> Unit)? = null,
    leadingView: (@Composable (Reaction) -> Unit)? = null,
    titleView: (@Composable (Reaction) -> Unit)? = null,
    subtitleView: (@Composable (Reaction) -> Unit)? = null,
    trailingView: (@Composable (Reaction) -> Unit)? = null
) {
    val youText = stringResource(R.string.cometchat_you)
    val tapToRemoveText = stringResource(R.string.cometchat_tap_to_remove)
    
    // Build accessibility description
    val accessibilityDescription = if (isCurrentUser) {
        "$youText, ${reaction.reaction}, $tapToRemoveText"
    } else {
        "${reaction.reactedBy.name}, ${reaction.reaction}"
    }
    
    // If custom itemView is provided, use it instead of default layout
    if (itemView != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = onClick
                )
                .semantics {
                    this.contentDescription = accessibilityDescription
                    this.role = Role.Button
                }
        ) {
            itemView(reaction)
        }
        return
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics {
                this.contentDescription = accessibilityDescription
                this.role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading view - Avatar or custom
        if (leadingView != null) {
            leadingView(reaction)
        } else {
            CometChatAvatar(
                name = reaction.reactedBy.name,
                avatarUrl = reaction.reactedBy.avatar,
                style = style.avatarStyle,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Title and subtitle column
        Column(modifier = Modifier.weight(1f)) {
            // Title - User name or "You" or custom
            if (titleView != null) {
                titleView(reaction)
            } else {
                Text(
                    text = if (isCurrentUser) youText else reaction.reactedBy.name,
                    style = style.titleTextStyle,
                    color = style.titleTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Subtitle - "Tap to remove" for logged-in user only or custom
            if (subtitleView != null) {
                subtitleView(reaction)
            } else if (isCurrentUser) {
                Text(
                    text = tapToRemoveText,
                    style = style.subtitleTextStyle,
                    color = style.subtitleTextColor,
                    maxLines = 1
                )
            }
        }
        
        // Tail view - Emoji or custom
        if (trailingView != null) {
            trailingView(reaction)
        } else {
            Text(
                text = reaction.reaction,
                style = style.tailViewTextStyle,
                color = style.tailViewTextColor
            )
        }
    }
}
