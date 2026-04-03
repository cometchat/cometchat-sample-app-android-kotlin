package com.cometchat.uikit.kotlin.presentation.groupmembers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.GroupMember
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatGroupMemberListItemBinding
import com.cometchat.uikit.kotlin.presentation.groupmembers.style.CometChatGroupMembersStyle
import com.cometchat.uikit.kotlin.presentation.groupmembers.utils.GroupMembersDiffCallback
import com.cometchat.uikit.kotlin.presentation.groupmembers.utils.GroupMembersViewHolderListener
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * RecyclerView adapter for displaying group member items.
 *
 * Uses ViewBinding with `cometchat_group_member_list_item.xml` and integrates
 * with [GroupMembersViewHolderListener] for custom view callbacks following the
 * chatuikit Java pattern.
 *
 * Supports DiffUtil for efficient list updates, selection modes, click listeners,
 * and overflow menu with permission-based options.
 */
class GroupMembersAdapter(
    private val context: Context
) : RecyclerView.Adapter<GroupMembersAdapter.MemberViewHolder>() {

    companion object {
        private val TAG = GroupMembersAdapter::class.java.simpleName
    }

    // Data
    private var members: List<GroupMember> = emptyList()
    private var group: Group? = null

    // Selection
    private var selectedMembers: HashMap<GroupMember, Boolean> = HashMap()
    private var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE

    // Click listeners
    private var onItemClick: ((View, Int, GroupMember) -> Unit)? = null
    private var onItemLongClick: ((View, Int, GroupMember) -> Unit)? = null
    private var onOverflowMenuClick: ((GroupMember, View) -> Unit)? = null

    // Custom view listeners
    private var itemViewListener: GroupMembersViewHolderListener? = null
    private var leadingViewListener: GroupMembersViewHolderListener? = null
    private var titleViewListener: GroupMembersViewHolderListener? = null
    private var subtitleViewListener: GroupMembersViewHolderListener? = null
    private var trailingViewListener: GroupMembersViewHolderListener? = null

    // Style
    private var style: CometChatGroupMembersStyle? = null
    private var hideUserStatus: Boolean = false
    private var hideSeparator: Boolean = true
    @Dimension private var separatorHeight: Int = 0
    private var avatarStyle: com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle? = null
    @androidx.annotation.StyleRes private var itemTitleTextAppearance: Int = 0
    private var statusIndicatorStyle: com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle? = null

    // Scope chip colors (from theme, matching Java adapter)
    @ColorInt private val ownerScopeChipBackgroundColor = CometChatTheme.getPrimaryColor(context)
    @ColorInt private val ownerScopeChipTextColor = CometChatTheme.getColorWhite(context)
    @ColorInt private val scopeChipTextColor = CometChatTheme.getTextColorHighlight(context)
    @ColorInt private val scopeChipBackgroundColor = CometChatTheme.getExtendedPrimaryColor100(context)
    @ColorInt private val scopeChipStrokeColor = CometChatTheme.getPrimaryColor(context)
    @Dimension private val scopeChipStrokeWidth = context.resources.getDimensionPixelSize(R.dimen.cometchat_1dp)

    // Checkbox styling
    @Dimension private var checkBoxStrokeWidth: Int = 0
    @Dimension private var checkBoxCornerRadius: Int = 0
    @ColorInt private var checkBoxStrokeColor: Int = CometChatTheme.getStrokeColorDefault(context)
    @ColorInt private var checkBoxBackgroundColor: Int = CometChatTheme.getBackgroundColor1(context)
    @ColorInt private var checkBoxCheckedBackgroundColor: Int = CometChatTheme.getPrimaryColor(context)
    private var checkBoxSelectIcon: android.graphics.drawable.Drawable? = null
    @ColorInt private var checkBoxSelectIconTint: Int = CometChatTheme.getColorWhite(context)

    // region Public API - Data

    /**
     * Updates the full member list using DiffUtil for efficient updates.
     */
    fun updateMembers(newMembers: List<GroupMember>) {
        val diffCallback = GroupMembersDiffCallback(members, newMembers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        members = newMembers.toList()
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Adds a member to the top of the list.
     */
    fun addMember(member: GroupMember) {
        val mutableList = members.toMutableList()
        mutableList.add(0, member)
        members = mutableList
        notifyItemInserted(0)
    }

    /**
     * Removes a member from the list by UID.
     */
    fun removeMember(member: GroupMember) {
        val index = members.indexOfFirst { it.uid == member.uid }
        if (index != -1) {
            val mutableList = members.toMutableList()
            mutableList.removeAt(index)
            members = mutableList
            notifyItemRemoved(index)
        }
    }

    /**
     * Updates a member in the list (matched by UID).
     */
    fun updateMember(member: GroupMember) {
        val index = members.indexOfFirst { it.uid == member.uid }
        if (index != -1) {
            val mutableList = members.toMutableList()
            mutableList[index] = member
            members = mutableList
            notifyItemChanged(index)
        }
    }

    /**
     * Sets the member list directly (without DiffUtil).
     */
    fun setMemberList(list: List<GroupMember>) {
        members = list.toList()
        notifyDataSetChanged()
    }

    /**
     * Gets the current member list.
     */
    fun getMemberList(): List<GroupMember> = members

    /**
     * Gets a member at a specific position.
     */
    fun getMember(position: Int): GroupMember = members[position]

    /**
     * Sets the group this adapter is displaying members for.
     */
    fun setGroup(group: Group) {
        this.group = group
        notifyDataSetChanged()
    }

    // endregion

    // region Public API - Selection

    /**
     * Sets the selection mode for the adapter.
     */
    fun setSelectionMode(mode: UIKitConstants.SelectionMode) {
        selectionMode = mode
        if (mode == UIKitConstants.SelectionMode.NONE) {
            selectedMembers.clear()
        }
        notifyDataSetChanged()
    }

    /**
     * Updates the selected members map.
     */
    fun selectMembers(selected: HashMap<GroupMember, Boolean>) {
        selectedMembers = selected
        notifyDataSetChanged()
    }

    /**
     * Clears all selections.
     */
    fun clearSelection() {
        selectedMembers.clear()
        notifyDataSetChanged()
    }

    /**
     * Gets the currently selected members.
     */
    fun getSelectedMembers(): HashMap<GroupMember, Boolean> = selectedMembers

    /**
     * Sets whether selection UI (checkboxes) is enabled.
     * When enabled, sets selection mode to MULTIPLE so checkboxes become visible.
     * When disabled, clears selection mode and selected members.
     */
    fun setSelectionEnabled(enabled: Boolean) {
        if (enabled) {
            if (selectionMode == UIKitConstants.SelectionMode.NONE) {
                selectionMode = UIKitConstants.SelectionMode.MULTIPLE
            }
        } else {
            selectionMode = UIKitConstants.SelectionMode.NONE
            selectedMembers.clear()
        }
        notifyDataSetChanged()
    }

    // endregion

    // region Public API - Click Listeners

    /**
     * Sets the item click listener.
     */
    fun setOnItemClick(listener: (View, Int, GroupMember) -> Unit) {
        onItemClick = listener
    }

    /**
     * Sets the item long click listener.
     */
    fun setOnItemLongClick(listener: (View, Int, GroupMember) -> Unit) {
        onItemLongClick = listener
    }

    /**
     * Sets the overflow menu click listener.
     */
    fun setOnOverflowMenuClick(listener: (GroupMember, View) -> Unit) {
        onOverflowMenuClick = listener
    }

    // endregion

    // region Public API - Custom Views

    fun setItemView(listener: GroupMembersViewHolderListener?) {
        itemViewListener = listener
        notifyDataSetChanged()
    }

    fun setLeadingView(listener: GroupMembersViewHolderListener?) {
        leadingViewListener = listener
        notifyDataSetChanged()
    }

    fun setTitleView(listener: GroupMembersViewHolderListener?) {
        titleViewListener = listener
        notifyDataSetChanged()
    }

    fun setSubtitleView(listener: GroupMembersViewHolderListener?) {
        subtitleViewListener = listener
        notifyDataSetChanged()
    }

    fun setTrailingView(listener: GroupMembersViewHolderListener?) {
        trailingViewListener = listener
        notifyDataSetChanged()
    }

    // endregion

    // region Public API - Styling

    fun setStyle(style: CometChatGroupMembersStyle) {
        this.style = style
        applyCheckboxStyleFromStyle(style)
        notifyDataSetChanged()
    }

    fun setHideUserStatus(hide: Boolean) {
        hideUserStatus = hide
        notifyDataSetChanged()
    }

    fun setHideSeparator(hide: Boolean) {
        hideSeparator = hide
        notifyDataSetChanged()
    }

    /**
     * Sets the separator height.
     *
     * @param height The height in pixels
     */
    fun setSeparatorHeight(@Dimension height: Int) {
        separatorHeight = height
        notifyDataSetChanged()
    }

    /**
     * Sets the avatar style for list items.
     *
     * @param avatarStyle The avatar style to apply
     */
    fun setAvatarStyle(avatarStyle: com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle) {
        this.avatarStyle = avatarStyle
        notifyDataSetChanged()
    }

    /**
     * Sets the item title text appearance.
     *
     * @param appearance The style resource ID for text appearance
     */
    fun setItemTitleTextAppearance(@androidx.annotation.StyleRes appearance: Int) {
        itemTitleTextAppearance = appearance
        notifyDataSetChanged()
    }

    /**
     * Sets the status indicator style for list items.
     *
     * @param statusIndicatorStyle The status indicator style to apply
     */
    fun setStatusIndicatorStyle(statusIndicatorStyle: com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle) {
        this.statusIndicatorStyle = statusIndicatorStyle
        notifyDataSetChanged()
    }

    fun setCheckBoxStrokeWidth(@Dimension width: Int) {
        checkBoxStrokeWidth = width
        notifyDataSetChanged()
    }

    fun setCheckBoxCornerRadius(@Dimension radius: Int) {
        checkBoxCornerRadius = radius
        notifyDataSetChanged()
    }

    fun setCheckBoxStrokeColor(@ColorInt color: Int) {
        checkBoxStrokeColor = color
        notifyDataSetChanged()
    }

    fun setCheckBoxBackgroundColor(@ColorInt color: Int) {
        checkBoxBackgroundColor = color
        notifyDataSetChanged()
    }

    fun setCheckBoxCheckedBackgroundColor(@ColorInt color: Int) {
        checkBoxCheckedBackgroundColor = color
        notifyDataSetChanged()
    }

    fun setCheckBoxSelectIcon(icon: android.graphics.drawable.Drawable?) {
        checkBoxSelectIcon = icon
        notifyDataSetChanged()
    }

    fun setCheckBoxSelectIconTint(@ColorInt tint: Int) {
        checkBoxSelectIconTint = tint
        notifyDataSetChanged()
    }

    // endregion

    // region Adapter Overrides

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = CometchatGroupMemberListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member, position)
    }

    override fun getItemCount(): Int = members.size

    // endregion

    // region Private Helpers

    private fun applyCheckboxStyleFromStyle(style: CometChatGroupMembersStyle) {
        if (style.checkBoxStrokeWidth > 0) checkBoxStrokeWidth = style.checkBoxStrokeWidth
        if (style.checkBoxCornerRadius > 0) checkBoxCornerRadius = style.checkBoxCornerRadius
        if (style.checkBoxStrokeColor != 0) checkBoxStrokeColor = style.checkBoxStrokeColor
        if (style.checkBoxBackgroundColor != 0) checkBoxBackgroundColor = style.checkBoxBackgroundColor
        if (style.checkBoxCheckedBackgroundColor != 0) checkBoxCheckedBackgroundColor = style.checkBoxCheckedBackgroundColor
        style.checkBoxSelectIcon?.let { checkBoxSelectIcon = it }
        if (style.checkBoxSelectIconTint != 0) checkBoxSelectIconTint = style.checkBoxSelectIconTint
    }

    private fun isBlocked(member: GroupMember): Boolean {
        return member.isBlockedByMe || member.isHasBlockedMe
    }

    // endregion

    // region ViewHolder

    /**
     * ViewHolder for group member list items.
     * Uses ViewBinding with `cometchat_group_member_list_item.xml`.
     */
    inner class MemberViewHolder(
        private val binding: CometchatGroupMemberListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Custom views created by listeners (cached for reuse)
        private var customItemView: View? = null
        private var customLeadingView: View? = null
        private var customTitleView: View? = null
        private var customSubtitleView: View? = null
        private var customTrailingView: View? = null

        init {
            // Create custom views once during ViewHolder creation
            createCustomViews()
        }

        private fun createCustomViews() {
            if (itemViewListener != null) {
                customItemView = itemViewListener!!.createView(context, binding)
                binding.cometchatParentLayout.removeAllViews()
                Utils.handleView(binding.cometchatParentLayout, customItemView, true)
            } else {
                if (leadingViewListener != null) {
                    customLeadingView = leadingViewListener!!.createView(context, binding)
                    Utils.handleView(binding.cometchatLeadingView, customLeadingView, true)
                }
                if (titleViewListener != null) {
                    customTitleView = titleViewListener!!.createView(context, binding)
                    Utils.handleView(binding.cometchatTitleView, customTitleView, true)
                }
                if (subtitleViewListener != null) {
                    customSubtitleView = subtitleViewListener!!.createView(context, binding)
                    Utils.handleView(binding.cometchatSubtitleView, customSubtitleView, true)
                }
                if (trailingViewListener != null) {
                    customTrailingView = trailingViewListener!!.createView(context, binding)
                    Utils.handleView(binding.cometchatTailView, customTrailingView, true)
                }
            }
        }

        fun bind(member: GroupMember, position: Int) {
            // Full item replacement
            if (itemViewListener != null && customItemView != null) {
                itemViewListener!!.bindView(
                    context, customItemView!!, member, group,
                    this, members, position
                )
                setupClickListeners(member, position)
                return
            }

            // Selection checkbox
            bindSelection(member)

            // Leading view (avatar + status)
            bindLeadingView(member, position)

            // Title view (member name)
            bindTitleView(member, position)

            // Subtitle view (scope)
            bindSubtitleView(member, position)

            // Trailing view (overflow menu)
            bindTrailingView(member, position)

            // Separator
            bindSeparator(position)

            // Apply style
            applyStyle()

            // Click listeners
            setupClickListeners(member, position)

            // Tag for identification
            itemView.setTag(R.string.cometchat_member, member)
        }

        private fun bindSelection(member: GroupMember) {
            val isSelectionActive = selectionMode != UIKitConstants.SelectionMode.NONE

            if (isSelectionActive) {
                Utils.initMaterialCard(binding.cometchatCheckboxView)
                checkBoxSelectIcon?.let { binding.cometchatIvCheckbox.setImageDrawable(it) }
                if (checkBoxSelectIconTint != 0) binding.cometchatIvCheckbox.setColorFilter(checkBoxSelectIconTint)
                binding.cometchatCheckboxView.strokeWidth = checkBoxStrokeWidth
                binding.cometchatCheckboxView.strokeColor = checkBoxStrokeColor
                binding.cometchatCheckboxView.radius = checkBoxCornerRadius.toFloat()

                if (selectedMembers.isNotEmpty() && selectedMembers.containsKey(member)) {
                    binding.cometchatIvCheckbox.visibility = View.VISIBLE
                    binding.cometchatCheckboxView.strokeWidth = 0
                    binding.cometchatCheckboxView.setCardBackgroundColor(checkBoxCheckedBackgroundColor)
                    // Change item background when selected (matching ConversationListItem pattern)
                    val selectedBgColor = style?.selectedBackgroundColor?.takeIf { it != 0 }
                        ?: CometChatTheme.getBackgroundColor3(context)
                    binding.cometchatParentLayout.setBackgroundColor(selectedBgColor)
                } else {
                    binding.cometchatIvCheckbox.visibility = View.GONE
                    binding.cometchatCheckboxView.setCardBackgroundColor(checkBoxBackgroundColor)
                    // Reset item background when not selected
                    val defaultBgColor = style?.backgroundColor?.takeIf { it != 0 }
                        ?: android.graphics.Color.TRANSPARENT
                    binding.cometchatParentLayout.setBackgroundColor(defaultBgColor)
                }
                binding.cometchatCheckboxView.visibility = View.VISIBLE
            } else {
                binding.cometchatCheckboxView.visibility = View.GONE
                // Reset item background when selection mode is off
                val defaultBgColor = style?.backgroundColor?.takeIf { it != 0 }
                    ?: android.graphics.Color.TRANSPARENT
                binding.cometchatParentLayout.setBackgroundColor(defaultBgColor)
            }
        }

        private fun bindLeadingView(member: GroupMember, position: Int) {
            if (leadingViewListener != null && customLeadingView != null) {
                leadingViewListener!!.bindView(
                    context, customLeadingView!!, member, group,
                    this, members, position
                )
            } else {
                binding.cometchatMemberAvatar.setAvatar(member.name, member.avatar)

                // Status indicator
                if (member.status.equals(CometChatConstants.USER_STATUS_ONLINE, ignoreCase = true)
                    && !isBlocked(member) && !hideUserStatus
                ) {
                    binding.cometchatMemberStatusIndicator.setStatusIndicator(StatusIndicator.ONLINE)
                    binding.cometchatMemberStatusIndicator.visibility = View.VISIBLE
                } else {
                    binding.cometchatMemberStatusIndicator.setStatusIndicator(StatusIndicator.OFFLINE)
                    binding.cometchatMemberStatusIndicator.visibility = View.GONE
                }

                // Apply avatar style - prioritize directly set style over style object
                avatarStyle?.let {
                    binding.cometchatMemberAvatar.setStyle(it)
                } ?: style?.let {
                    if (it.avatarStyleResId != 0) {
                        binding.cometchatMemberAvatar.setStyle(it.avatarStyleResId)
                    }
                }

                // Apply status indicator style - prioritize directly set style over style object
                statusIndicatorStyle?.let {
                    binding.cometchatMemberStatusIndicator.setStyle(it)
                } ?: style?.let {
                    if (it.statusIndicatorStyleResId != 0) {
                        binding.cometchatMemberStatusIndicator.setStyle(it.statusIndicatorStyleResId)
                    }
                }
            }
        }

        private fun bindTitleView(member: GroupMember, position: Int) {
            if (titleViewListener != null && customTitleView != null) {
                titleViewListener!!.bindView(
                    context, customTitleView!!, member, group,
                    this, members, position
                )
            } else {
                val loggedInUser = try {
                    CometChatUIKit.getLoggedInUser()
                } catch (e: Exception) {
                    null
                }
                val displayName = if (loggedInUser != null && member.uid.equals(loggedInUser.uid, ignoreCase = true)) {
                    context.getString(R.string.cometchat_you)
                } else {
                    member.name
                }
                binding.cometchatMemberName.text = displayName
            }
        }

        private fun bindSubtitleView(member: GroupMember, position: Int) {
            if (subtitleViewListener != null && customSubtitleView != null) {
                subtitleViewListener!!.bindView(
                    context, customSubtitleView!!, member, group,
                    this, members, position
                )
                // Hide scope card when custom subtitle is used
                binding.cometchatScopeCard.visibility = View.GONE
            } else {
                // Always reset scope card visibility first (matches original Java implementation)
                binding.cometchatScopeCard.visibility = View.VISIBLE

                // Reset layout params to WRAP_CONTENT so the card re-measures for new text
                val layoutParams = binding.cometchatScopeCard.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.cometchatScopeCard.layoutParams = layoutParams

                val scope = member.scope
                if (CometChatConstants.SCOPE_PARTICIPANT.equals(scope, ignoreCase = true)) {
                    binding.cometchatScopeCard.visibility = View.GONE
                } else if (group != null && member.uid == group!!.owner) {
                    binding.cometchatScopeCard.setCardBackgroundColor(ownerScopeChipBackgroundColor)
                    binding.cometchatScopeText.setTextColor(ownerScopeChipTextColor)
                    binding.cometchatScopeCard.strokeColor = scopeChipStrokeColor
                    binding.cometchatScopeText.text = context.getString(R.string.cometchat_owner)
                } else if (UIKitConstants.GroupMemberScope.ADMIN.equals(scope)) {
                    binding.cometchatScopeCard.setCardBackgroundColor(scopeChipBackgroundColor)
                    binding.cometchatScopeCard.strokeWidth = scopeChipStrokeWidth
                    binding.cometchatScopeCard.strokeColor = scopeChipStrokeColor
                    val scopeText = scope.replaceFirstChar { it.uppercase() }
                    binding.cometchatScopeText.text = scopeText
                    binding.cometchatScopeText.setTextColor(scopeChipTextColor)
                } else if (UIKitConstants.GroupMemberScope.MODERATOR.equals(scope)) {
                    binding.cometchatScopeCard.setCardBackgroundColor(scopeChipBackgroundColor)
                    binding.cometchatScopeCard.strokeWidth = 0
                    val scopeText = scope.replaceFirstChar { it.uppercase() }
                    binding.cometchatScopeText.text = scopeText
                    binding.cometchatScopeText.setTextColor(scopeChipTextColor)
                } else {
                    // Unknown scope or any other value — hide the chip
                    binding.cometchatScopeCard.visibility = View.GONE
                }
            }
        }

        private fun bindTrailingView(member: GroupMember, position: Int) {
            if (trailingViewListener != null && customTrailingView != null) {
                binding.cometchatTailView.visibility = View.VISIBLE
                trailingViewListener!!.bindView(
                    context, customTrailingView!!, member, group,
                    this, members, position
                )
            } else {
                // No default overflow icon — popup menu is triggered by long press
                // on the item row, matching the original Java implementation.
                binding.cometchatTailView.visibility = View.GONE
            }
        }

        private fun bindSeparator(position: Int) {
            binding.cometchatSeparator.visibility =
                if (hideSeparator || position == members.lastIndex) View.GONE
                else View.VISIBLE

            // Apply separator height if set
            if (separatorHeight > 0) {
                val layoutParams = binding.cometchatSeparator.layoutParams
                layoutParams.height = separatorHeight
                binding.cometchatSeparator.layoutParams = layoutParams
            }

            style?.let {
                if (it.separatorColor != 0) {
                    binding.cometchatSeparator.setBackgroundColor(it.separatorColor)
                }
            }
        }

        private fun applyStyle() {
            style?.let { s ->
                s.applyToListItem(binding.cometchatMemberName)
            }
            // Apply item title text appearance if directly set
            if (itemTitleTextAppearance != 0) {
                binding.cometchatMemberName.setTextAppearance(itemTitleTextAppearance)
            }
        }

        private fun setupClickListeners(member: GroupMember, position: Int) {
            itemView.setOnClickListener { view ->
                onItemClick?.invoke(view, position, member)
            }
            itemView.setOnLongClickListener { view ->
                onItemLongClick?.invoke(view, position, member)
                true
            }
        }
    }

    // endregion
}
