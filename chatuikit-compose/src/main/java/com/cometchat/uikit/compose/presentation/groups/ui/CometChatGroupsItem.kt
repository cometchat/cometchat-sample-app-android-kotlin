package com.cometchat.uikit.compose.presentation.groups.ui

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.cometchat.chat.models.Group
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.groups.style.CometChatGroupsItemStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicator
import com.cometchat.uikit.compose.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Default leading view composable that displays the avatar with group type indicator.
 *
 * @param group The group to display
 * @param hideGroupType Whether to hide the group type indicator
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultGroupLeadingView(
    group: Group,
    hideGroupType: Boolean,
    style: CometChatGroupsItemStyle
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
            name = group.name ?: "",
            avatarUrl = group.icon,
            style = style.avatarStyle
        )

        // Group type indicator (only show for private/password groups)
        if (!hideGroupType) {
            val statusIndicator = getStatusIndicatorFromGroup(group)

            // Only show indicator for private/password groups (not public)
            if (statusIndicator != StatusIndicator.PUBLIC_GROUP) {
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
 * Converts a Group to a StatusIndicator enum value based on its type.
 *
 * @param group The group to convert
 * @return The appropriate StatusIndicator enum value
 */
private fun getStatusIndicatorFromGroup(group: Group): StatusIndicator {
    return when (group.groupType) {
        CometChatConstants.GROUP_TYPE_PRIVATE -> StatusIndicator.PRIVATE_GROUP
        CometChatConstants.GROUP_TYPE_PASSWORD -> StatusIndicator.PROTECTED_GROUP
        else -> StatusIndicator.PUBLIC_GROUP
    }
}

/**
 * Default title view composable that displays the group name.
 *
 * @param group The group to display
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultGroupTitleView(
    group: Group,
    style: CometChatGroupsItemStyle
) {
    Text(
        text = group.name ?: "",
        color = style.titleTextColor,
        style = style.titleTextStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Default subtitle view composable that displays the member count.
 *
 * @param group The group to display
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultGroupSubtitleView(
    group: Group,
    style: CometChatGroupsItemStyle
) {
    val context = LocalContext.current
    val membersCount = group.membersCount

    val subtitleText = context.resources.getQuantityString(
        R.plurals.cometchat_members_count,
        membersCount,
        membersCount
    )

    Text(
        text = subtitleText,
        color = style.subtitleTextColor,
        style = style.subtitleTextStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Selection checkbox composable for multi-select mode.
 *
 * @param isSelected Whether the item is currently selected
 * @param groupName Name of the group for accessibility
 * @param context Android context for accessing string resources
 * @param style Style configuration for the component
 */
@Composable
internal fun GroupSelectionCheckbox(
    isSelected: Boolean,
    groupName: String = "",
    context: Context,
    style: CometChatGroupsItemStyle
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
                contentDescription = if (groupName.isNotEmpty()) {
                    "$groupName, $selectionStateDescription"
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
 * CometChatGroupsItem is a composable that displays a single group item in a list.
 * It matches the UI and functionality of the existing Android View-based GroupsViewHolder.
 *
 * Features:
 * - Displays avatar with group type indicator (public/private/password)
 * - Shows group name and member count
 * - Supports selection mode with checkbox
 * - Fully customizable through style and custom view lambdas
 * - Integrates with CometChatTheme for consistent styling
 *
 * @param group The CometChat Group object to display (required)
 * @param onItemClick Callback invoked when the item is clicked (required)
 * @param modifier Modifier applied to the parent container
 * @param onItemLongClick Optional callback for long-click events
 * @param isSelected Whether the item is currently selected
 * @param selectionMode The selection mode (NONE, SINGLE, MULTIPLE)
 * @param hideGroupType Whether to hide the group type indicator
 * @param hideSeparator Whether to hide the separator line
 * @param style Style configuration for the component
 * @param leadingView Optional custom composable for the leading section (avatar area)
 * @param titleView Optional custom composable for the title section (group name)
 * @param subtitleView Optional custom composable for the subtitle section (member count)
 * @param trailingView Optional custom composable for the trailing section
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatGroupsItem(
    group: Group,
    onItemClick: (Group) -> Unit,
    modifier: Modifier = Modifier,
    onItemLongClick: ((Group) -> Unit)? = null,
    isSelected: Boolean = false,
    selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
    hideGroupType: Boolean = false,
    hideSeparator: Boolean = false,
    style: CometChatGroupsItemStyle = CometChatGroupsItemStyle.default(),
    leadingView: (@Composable (Group) -> Unit)? = null,
    titleView: (@Composable (Group) -> Unit)? = null,
    subtitleView: (@Composable (Group) -> Unit)? = null,
    trailingView: (@Composable (Group) -> Unit)? = null
) {
    val context = LocalContext.current

    // Build content description for accessibility
    val groupName = group.name ?: ""
    val membersCount = group.membersCount
    val membersText = context.resources.getQuantityString(
        R.plurals.cometchat_members_count,
        membersCount,
        membersCount
    )
    val accessibilityDescription = buildString {
        append(groupName)
        append(", ")
        append(membersText)
        if (isSelected) {
            append(", ")
            append(context.getString(R.string.cometchat_selected))
        }
    }

    // Determine background color based on selection state
    val backgroundColor = if (isSelected) {
        style.selectedBackgroundColor
    } else {
        style.backgroundColor
    }

    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .focusable()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = { onItemClick(group) },
                    onLongClick = onItemLongClick?.let { { it(group) } }
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
                GroupSelectionCheckbox(
                    isSelected = isSelected,
                    groupName = groupName,
                    context = context,
                    style = style
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Leading view (avatar with group type indicator)
            if (leadingView != null) {
                leadingView(group)
            } else {
                DefaultGroupLeadingView(
                    group = group,
                    hideGroupType = hideGroupType,
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
                    titleView(group)
                } else {
                    DefaultGroupTitleView(
                        group = group,
                        style = style
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle view
                if (subtitleView != null) {
                    subtitleView(group)
                } else {
                    DefaultGroupSubtitleView(
                        group = group,
                        style = style
                    )
                }
            }

            // Trailing view (custom content)
            if (trailingView != null) {
                Spacer(modifier = Modifier.width(16.dp))
                trailingView(group)
            }
        }

        // Separator
        if (!hideSeparator && style.separatorHeight > 0.dp) {
            HorizontalDivider(
                color = style.separatorColor,
                thickness = style.separatorHeight,
                modifier = Modifier.padding(start = 76.dp)
            )
        }
    }
}
