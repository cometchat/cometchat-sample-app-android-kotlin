package com.cometchat.uikit.compose.presentation.groupmembers.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.groupmembers.style.CometChatGroupMembersStyle
import com.cometchat.uikit.compose.presentation.groupmembers.utils.GroupMembersUtils
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.popupmenu.CometChatPopupMenu
import com.cometchat.uikit.compose.presentation.shared.popupmenu.PopupPosition
import com.cometchat.uikit.compose.presentation.shared.statusindicator.CometChatStatusIndicator
import com.cometchat.uikit.compose.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.compose.shared.views.popupmenu.MenuItem
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Default leading view composable that displays the avatar with status indicator.
 *
 * @param member The group member to display
 * @param hideUserStatus Whether to hide the user online/offline status indicator
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultLeadingView(
    member: GroupMember,
    hideUserStatus: Boolean,
    style: CometChatGroupMembersStyle
) {
    val avatarSize = 40.dp
    val statusIndicatorSize = 15.dp
    
    Box(
        modifier = Modifier.size(avatarSize),
        contentAlignment = Alignment.Center
    ) {
        // Avatar
        CometChatAvatar(
            modifier = Modifier.size(avatarSize),
            name = member.name ?: "",
            avatarUrl = member.avatar,
            style = style.avatarStyle
        )
        
        // Status Indicator overlay at bottom-end (only render if not OFFLINE)
        if (!hideUserStatus) {
            val statusIndicator = if (member.status == CometChatConstants.USER_STATUS_ONLINE) {
                StatusIndicator.ONLINE
            } else {
                StatusIndicator.OFFLINE
            }
            
            if (statusIndicator != StatusIndicator.OFFLINE) {
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
 * Default title view composable that displays the member name.
 * Shows "You" for the logged-in user, matching the XML adapter implementation.
 *
 * @param member The group member to display
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultTitleView(
    member: GroupMember,
    style: CometChatGroupMembersStyle
) {
    val context = LocalContext.current
    val loggedInUser = remember {
        try {
            com.cometchat.uikit.core.CometChatUIKit.getLoggedInUser()
        } catch (e: Exception) {
            null
        }
    }
    val displayName = if (loggedInUser != null && member.uid.equals(loggedInUser.uid, ignoreCase = true)) {
        context.getString(R.string.cometchat_you)
    } else {
        member.name ?: ""
    }
    Text(
        text = displayName,
        color = style.itemTitleTextColor,
        style = style.itemTitleTextStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Default subtitle view composable — empty by default.
 * The scope is displayed as a chip in the trailing area, matching the original Java implementation.
 *
 * @param member The group member to display
 * @param context Android context for accessing string resources
 * @param style Style configuration for the component
 */
@Composable
internal fun DefaultSubtitleView(
    member: GroupMember,
    context: Context,
    style: CometChatGroupMembersStyle
) {
    // Subtitle area is intentionally empty in the default implementation.
    // Scope is shown as a chip badge to the right of the content area,
    // matching the original Java implementation.
}

/**
 * Scope chip badge composable that displays the member scope as a pill/badge.
 * Matches the original Java implementation's MaterialCardView scope card.
 *
 * Styling per scope:
 * - Owner: primary background, white text, primary stroke
 * - Admin: extendedPrimaryColor100 background, highlight text, primary stroke (1dp)
 * - Moderator: extendedPrimaryColor100 background, highlight text, no stroke
 * - Participant: hidden (not rendered)
 *
 * @param member The group member to display
 * @param context Android context for accessing string resources
 * @param style Style configuration for the component
 */
@Composable
internal fun ScopeChipBadge(
    member: GroupMember,
    context: Context,
    style: CometChatGroupMembersStyle,
    group: Group? = null
) {
    val scope = member.scope ?: return
    val isOwner = group?.owner != null && member.uid == group.owner
    
    // Determine chip colors based on scope
    val chipBackgroundColor: Color
    val chipTextColor: Color
    val chipStrokeWidth: Dp
    
    when {
        isOwner -> {
            chipBackgroundColor = style.scopeChipOwnerBackgroundColor
            chipTextColor = style.scopeChipOwnerTextColor
            chipStrokeWidth = style.scopeChipStrokeWidth
        }
        CometChatConstants.SCOPE_ADMIN.equals(scope, ignoreCase = true) -> {
            chipBackgroundColor = style.scopeChipBackgroundColor
            chipTextColor = style.scopeChipTextColor
            chipStrokeWidth = style.scopeChipStrokeWidth
        }
        CometChatConstants.SCOPE_MODERATOR.equals(scope, ignoreCase = true) -> {
            chipBackgroundColor = style.scopeChipBackgroundColor
            chipTextColor = style.scopeChipTextColor
            chipStrokeWidth = 0.dp
        }
        else -> {
            // Fallback for any other non-participant scope
            chipBackgroundColor = style.scopeChipBackgroundColor
            chipTextColor = style.scopeChipTextColor
            chipStrokeWidth = 0.dp
        }
    }
    
    val scopeText = if (isOwner) {
        context.getString(R.string.cometchat_owner)
    } else {
        GroupMembersUtils.getScopeDisplayName(context, scope)
    }
    val shape = RoundedCornerShape(style.scopeChipCornerRadius)
    
    Box(
        modifier = Modifier
            .clip(shape)
            .background(chipBackgroundColor)
            .then(
                if (chipStrokeWidth > 0.dp) {
                    Modifier.border(
                        width = chipStrokeWidth,
                        color = style.scopeChipStrokeColor,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = style.scopeChipPaddingHorizontal,
                vertical = style.scopeChipPaddingVertical
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = scopeText,
            color = chipTextColor,
            style = style.itemScopeTextStyle,
            maxLines = 1
        )
    }
}

/**
 * Default trailing view — intentionally empty.
 * The popup menu is triggered by long press on the item row,
 * matching the original Java CometChatGroupMembers implementation.
 * No visible overflow icon is shown in the default item layout.
 */

/**
 * Selection checkbox composable for multi-select mode.
 *
 * @param isSelected Whether the item is currently selected
 * @param memberName Name of the member for accessibility
 * @param context Android context for accessing string resources
 * @param style Style configuration for the component
 */
@Composable
internal fun SelectionCheckbox(
    isSelected: Boolean,
    memberName: String = "",
    context: Context,
    style: CometChatGroupMembersStyle
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
                contentDescription = if (memberName.isNotEmpty()) {
                    "$memberName, $selectionStateDescription"
                } else {
                    selectionStateDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected && style.checkBoxSelectIcon != null) {
            Icon(
                painter = style.checkBoxSelectIcon,
                contentDescription = null,
                tint = style.checkBoxSelectIconTint,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * CometChatGroupMemberListItem is a composable that displays a single group member item in a list.
 * It matches the UI and functionality of the existing Android View-based implementation.
 *
 * Features:
 * - Displays avatar with status indicator
 * - Shows member name and scope (owner/admin/moderator/participant)
 * - Supports selection mode with checkbox
 * - Displays overflow menu with actions (kick, ban, change scope)
 * - Fully customizable through style and custom view lambdas
 * - Integrates with CometChatTheme for consistent styling
 *
 * @param member The CometChat GroupMember object to display (required)
 * @param onItemClick Callback invoked when the item is clicked (required)
 * @param modifier Modifier applied to the parent container
 * @param onItemLongClick Optional callback for long-click events
 * @param isSelected Whether the item is currently selected
 * @param selectionMode The selection mode (NONE, SINGLE, MULTIPLE)
 * @param hideUserStatus Whether to hide the user online/offline status indicator
 * @param menuItems List of menu items to display in the overflow menu
 * @param onMenuItemClick Callback invoked when a menu item is clicked
 * @param style Style configuration for the component
 * @param leadingView Optional custom composable for the leading section (avatar area)
 * @param titleView Optional custom composable for the title section (member name)
 * @param subtitleView Optional custom composable for the subtitle section (member scope)
 * @param tailView Optional custom composable for the tail section (replaces overflow menu)
 *
 * @sample
 * ```
 * // Basic usage
 * CometChatGroupMemberListItem(
 *     member = groupMember,
 *     onItemClick = { member -> viewMemberProfile(member) }
 * )
 *
 * // With selection mode
 * CometChatGroupMemberListItem(
 *     member = groupMember,
 *     onItemClick = { member -> toggleSelection(member) },
 *     isSelected = selectedMembers.contains(groupMember),
 *     selectionMode = SelectionMode.MULTIPLE
 * )
 *
 * // With overflow menu
 * CometChatGroupMemberListItem(
 *     member = groupMember,
 *     onItemClick = { member -> viewMemberProfile(member) },
 *     menuItems = listOf(
 *         MenuItem.simple("kick", "Kick"),
 *         MenuItem.simple("ban", "Ban")
 *     ),
 *     onMenuItemClick = { id, name -> handleMenuAction(id, groupMember) }
 * )
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CometChatGroupMemberListItem(
    member: GroupMember,
    onItemClick: (GroupMember) -> Unit,
    modifier: Modifier = Modifier,
    group: Group? = null,
    onItemLongClick: ((GroupMember) -> Unit)? = null,
    isSelected: Boolean = false,
    selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE,
    hideUserStatus: Boolean = false,
    menuItems: List<MenuItem> = emptyList(),
    onMenuItemClick: ((String, String) -> Unit)? = null,
    style: CometChatGroupMembersStyle = CometChatGroupMembersStyle.default(),
    leadingView: (@Composable (GroupMember) -> Unit)? = null,
    titleView: (@Composable (GroupMember) -> Unit)? = null,
    subtitleView: (@Composable (GroupMember) -> Unit)? = null,
    tailView: (@Composable (GroupMember) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Build content description for accessibility
    val memberName = member.name ?: ""
    val scopeText = GroupMembersUtils.getScopeDisplayName(context, member.scope)
    
    val accessibilityDescription = buildString {
        append(memberName)
        if (scopeText.isNotEmpty()) {
            append(", ")
            append(scopeText)
        }
        if (isSelected) {
            append(", ")
            append(context.getString(R.string.cometchat_selected))
        }
    }
    
    // Determine background color based on selection state
    val backgroundColor = if (isSelected) {
        style.itemSelectedBackgroundColor
    } else {
        style.itemBackgroundColor
    }

    // Popup menu state — triggered by long press, matching original Java implementation
    var showPopupMenu by remember { mutableStateOf(false) }

    // Default long press behavior: show popup menu if no custom handler and menu items exist
    val effectiveLongClick: ((GroupMember) -> Unit)? = if (onItemLongClick != null) {
        onItemLongClick
    } else if (menuItems.isNotEmpty()) {
        { showPopupMenu = true }
    } else {
        null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onItemClick(member) },
                onLongClick = effectiveLongClick?.let { { it(member) } }
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
                memberName = memberName,
                context = context,
                style = style
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        // Leading view (avatar with status indicator)
        if (leadingView != null) {
            leadingView(member)
        } else {
            DefaultLeadingView(
                member = member,
                hideUserStatus = hideUserStatus,
                style = style
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content area (title and optional subtitle)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Title view (member name)
            if (titleView != null) {
                titleView(member)
            } else {
                DefaultTitleView(
                    member = member,
                    style = style
                )
            }
            
            // Subtitle view (custom only — default is empty since scope is shown as chip)
            if (subtitleView != null) {
                Spacer(modifier = Modifier.size(2.dp))
                subtitleView(member)
            }
        }
        
        // Tail area: scope chip + popup menu anchor
        // The popup menu Box is placed here (end of row) so DropdownMenu
        // anchors to the right side of the item, matching the Java implementation
        // where popupMenu.show(view) positions at the anchor's right edge.
        if (tailView == null) {
            val isParticipant = CometChatConstants.SCOPE_PARTICIPANT.equals(member.scope, ignoreCase = true)
            if (!isParticipant) {
                Spacer(modifier = Modifier.width(16.dp))
                ScopeChipBadge(
                    member = member,
                    context = context,
                    style = style,
                    group = group
                )
            }
            // Popup menu anchored at the tail area, shown on long press
            if (menuItems.isNotEmpty()) {
                CometChatPopupMenu(
                    expanded = showPopupMenu,
                    onDismissRequest = { showPopupMenu = false },
                    menuItems = menuItems,
                    position = PopupPosition.BELOW,
                    onMenuItemClick = { id, name ->
                        onMenuItemClick?.invoke(id, name)
                        showPopupMenu = false
                    }
                ) {
                    // Zero-size anchor — popup positions relative to this Box
                    // at the end of the row (right side), matching Java behavior
                }
            }
        }
        
        // Tail view (custom only — no default overflow icon)
        if (tailView != null) {
            Spacer(modifier = Modifier.width(16.dp))
            tailView(member)
        }
    }
}
