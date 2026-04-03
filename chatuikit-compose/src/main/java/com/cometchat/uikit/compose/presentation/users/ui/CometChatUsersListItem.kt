package com.cometchat.uikit.compose.presentation.users.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.User
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicator
import com.cometchat.uikit.compose.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.compose.presentation.users.style.CometChatUsersListItemStyle
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Default leading view composable that displays the avatar with status indicator.
 *
 * @param user The user to display
 * @param hideStatusIndicator Whether to hide the online/offline status indicator
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultLeadingView(
    user: User,
    hideStatusIndicator: Boolean,
    style: CometChatUsersListItemStyle
) {
    val avatarSize = 48.dp
    val statusIndicatorSize = style.statusIndicatorStyle.size
    
    Box(
        modifier = Modifier.size(avatarSize),
        contentAlignment = Alignment.Center
    ) {
        // Avatar
        CometChatAvatar(
            modifier = Modifier.size(avatarSize),
            name = user.name ?: "",
            avatarUrl = user.avatar,
            style = style.avatarStyle
        )
        
        // Status Indicator overlay at bottom-end
        if (!hideStatusIndicator) {
            val statusIndicator = getStatusIndicatorFromUser(user)
            if (statusIndicator == StatusIndicator.ONLINE) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(statusIndicatorSize)
                ) {
                    CometChatStatusIndicator(
                        status = statusIndicator,
                        style = style.statusIndicatorStyle
                    )
                }
            }
        }
    }
}

/**
 * Converts a User to a StatusIndicator enum value.
 *
 * @param user The user to convert
 * @return The appropriate StatusIndicator enum value
 */
private fun getStatusIndicatorFromUser(user: User): StatusIndicator {
    // Blocked users always show offline
    if (user.isBlockedByMe || user.isHasBlockedMe) {
        return StatusIndicator.OFFLINE
    }
    
    return if (user.status == CometChatConstants.USER_STATUS_ONLINE) {
        StatusIndicator.ONLINE
    } else {
        StatusIndicator.OFFLINE
    }
}

/**
 * Default title view composable that displays the user name.
 *
 * @param user The user to display
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultTitleView(
    user: User,
    style: CometChatUsersListItemStyle
) {
    Text(
        text = user.name ?: "",
        color = style.titleTextColor,
        style = style.titleTextStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}


/**
 * Default subtitle view composable (empty by default for users).
 *
 * @param user The user to display
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultSubtitleView(
    user: User,
    style: CometChatUsersListItemStyle
) {
    // Users don't have a default subtitle, but custom subtitleView can be provided
}

/**
 * Default trailing view composable (empty by default for users).
 *
 * @param user The user to display
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultTrailingView(
    user: User,
    style: CometChatUsersListItemStyle
) {
    // Users don't have a default trailing view, but custom trailingView can be provided
}

/**
 * Selection checkbox composable for multi-select mode.
 *
 * @param isSelected Whether the item is currently selected
 * @param userName Name of the user for accessibility
 * @param context Android context for accessing string resources
 * @param style Style configuration for the component
 */
@Composable
internal fun SelectionCheckbox(
    isSelected: Boolean,
    userName: String = "",
    context: Context,
    style: CometChatUsersListItemStyle
) {
    val checkboxSize = 20.dp
    val shape = RoundedCornerShape(style.checkBoxCornerRadius)
    val selectionStateDescription = if (isSelected) {
        context.getString(R.string.cometchat_selected)
    } else {
        context.getString(R.string.cometchat_not_selected)
    }
    
    Box(
        modifier = Modifier
            .size(checkboxSize)
            .clip(shape)
            .background(
                if (isSelected) style.checkBoxCheckedBackgroundColor 
                else style.checkBoxBackgroundColor
            )
            .border(
                width = style.checkBoxStrokeWidth,
                color = if (isSelected) style.checkBoxCheckedBackgroundColor 
                        else style.checkBoxStrokeColor,
                shape = shape
            )
            .focusable()
            .semantics {
                role = Role.Checkbox
                selected = isSelected
                stateDescription = selectionStateDescription
                contentDescription = if (userName.isNotEmpty()) {
                    "$userName, $selectionStateDescription"
                } else {
                    selectionStateDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = style.checkBoxSelectIcon,
                contentDescription = null,
                tint = style.checkBoxSelectIconTint,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * CometChatUsersListItem is a composable that displays a single user item in a list.
 * It matches the UI and functionality of the existing Android View-based UsersViewHolder.
 *
 * Features:
 * - Displays avatar with status indicator
 * - Shows user name
 * - Supports selection mode with checkbox
 * - Fully customizable through style and custom view lambdas
 * - Integrates with CometChatTheme for consistent styling
 *
 * @param user The CometChat User object to display (required)
 * @param onItemClick Callback invoked when the item is clicked (required)
 * @param modifier Modifier applied to the parent container
 * @param onItemLongClick Optional callback for long-click events
 * @param isSelected Whether the item is currently selected
 * @param selectionMode The selection mode (NONE, SINGLE, MULTIPLE)
 * @param hideStatusIndicator Whether to hide the online/offline status indicator
 * @param style Style configuration for the component
 * @param leadingView Optional custom composable for the leading section
 * @param titleView Optional custom composable for the title section
 * @param subtitleView Optional custom composable for the subtitle section
 * @param trailingView Optional custom composable for the trailing section
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatUsersListItem(
    user: User,
    onItemClick: (User) -> Unit,
    modifier: Modifier = Modifier,
    onItemLongClick: ((User) -> Unit)? = null,
    isSelected: Boolean = false,
    selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
    hideStatusIndicator: Boolean = false,
    style: CometChatUsersListItemStyle = CometChatUsersListItemStyle.default(),
    leadingView: (@Composable (User) -> Unit)? = null,
    titleView: (@Composable (User) -> Unit)? = null,
    subtitleView: (@Composable (User) -> Unit)? = null,
    trailingView: (@Composable (User) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Build content description for accessibility
    val userName = user.name ?: ""
    val statusText = if (user.status == CometChatConstants.USER_STATUS_ONLINE) {
        context.getString(R.string.cometchat_online)
    } else {
        context.getString(R.string.cometchat_offline)
    }
    val accessibilityDescription = buildString {
        append(userName)
        if (!hideStatusIndicator) {
            append(", ")
            append(statusText)
        }
        if (isSelected) {
            append(", selected")
        }
    }
    
    // Determine background color based on selection state
    val backgroundColor = if (isSelected) {
        style.selectedBackgroundColor
    } else {
        style.backgroundColor
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .focusable()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onItemClick(user) },
                onLongClick = onItemLongClick?.let { { it(user) } }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = accessibilityDescription
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox (when selection mode is active)
        if (selectionMode != UIKitConstants.SelectionMode.NONE) {
            SelectionCheckbox(
                isSelected = isSelected,
                userName = userName,
                context = context,
                style = style
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        // Leading view (avatar with status indicator)
        if (leadingView != null) {
            leadingView(user)
        } else {
            DefaultLeadingView(
                user = user,
                hideStatusIndicator = hideStatusIndicator,
                style = style
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content area (title and subtitle)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Title view
            if (titleView != null) {
                titleView(user)
            } else {
                DefaultTitleView(
                    user = user,
                    style = style
                )
            }
            
            // Subtitle view (if provided)
            if (subtitleView != null) {
                Spacer(modifier = Modifier.size(2.dp))
                subtitleView(user)
            }
        }
        
        // Trailing view (if provided)
        if (trailingView != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailingView(user)
        }
    }
}
