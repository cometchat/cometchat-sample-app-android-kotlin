package com.cometchat.uikit.kotlin.presentation.groups.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Group
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatGroupsListItemBinding
import com.cometchat.uikit.kotlin.presentation.groups.style.CometChatGroupsItemStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.StatusIndicator

/**
 * CometChatGroupsItem is a custom Android View that displays a single group item
 * with avatar, status indicator (group type), title, and subtitle (member count).
 *
 * This component can be used standalone or within a RecyclerView for displaying group lists.
 * It supports full customization through styles and custom view slots.
 */
class CometChatGroupsItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // View Binding
    internal val binding: CometchatGroupsListItemBinding = CometchatGroupsListItemBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    // Current group
    private var group: Group? = null

    // Selection state
    private var isItemSelected: Boolean = false
    private var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE

    // Callbacks
    private var onItemClick: ((Group) -> Unit)? = null
    private var onItemLongClick: ((Group) -> Unit)? = null

    // Custom views
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // Visibility controls
    private var hideGroupType: Boolean = false
    private var hideSeparator: Boolean = false

    // Style
    private var style: CometChatGroupsItemStyle = CometChatGroupsItemStyle()

    init {
        setupClickListeners()
    }

    // ==================== Public API Methods ====================

    /**
     * Sets the group to display.
     */
    fun setGroup(group: Group) {
        this.group = group
        bindGroup()
    }

    /**
     * Gets the current group.
     */
    fun getGroup(): Group? = group

    /**
     * Sets whether this item is selected.
     */
    fun setItemSelected(selected: Boolean) {
        this.isItemSelected = selected
        updateSelectionState()
    }

    /**
     * Sets the selection mode.
     */
    fun setSelectionMode(mode: UIKitConstants.SelectionMode) {
        this.selectionMode = mode
        updateSelectionState()
    }

    /**
     * Sets the item click callback.
     */
    fun setOnItemClick(callback: (Group) -> Unit) {
        onItemClick = callback
    }

    /**
     * Sets the item long click callback.
     */
    fun setOnItemLongClick(callback: (Group) -> Unit) {
        onItemLongClick = callback
    }

    /**
     * Sets a custom leading view (replaces avatar area).
     */
    fun setLeadingView(view: View?) {
        customLeadingView = view
        if (view != null) {
            binding.leadingViewContainer.removeAllViews()
            binding.leadingViewContainer.addView(view)
            binding.groupsAvatar.visibility = View.GONE
            binding.groupsTypeIndicator.visibility = View.GONE
        } else {
            resetLeadingView()
        }
    }

    /**
     * Sets a custom title view (replaces group name).
     */
    fun setTitleView(view: View?) {
        customTitleView = view
        if (view != null) {
            binding.titleViewContainer.removeAllViews()
            binding.titleViewContainer.addView(view)
            binding.tvGroupsTitle.visibility = View.GONE
        } else {
            resetTitleView()
        }
    }

    /**
     * Sets a custom subtitle view (replaces member count).
     */
    fun setSubtitleView(view: View?) {
        customSubtitleView = view
        if (view != null) {
            binding.subtitleViewContainer.removeAllViews()
            binding.subtitleViewContainer.addView(view)
            binding.tvGroupsSubtitle.visibility = View.GONE
        } else {
            resetSubtitleView()
        }
    }

    /**
     * Sets a custom trailing view.
     */
    fun setTrailingView(view: View?) {
        customTrailingView = view
        if (view != null) {
            binding.trailingViewContainer.removeAllViews()
            binding.trailingViewContainer.addView(view)
            binding.trailingViewContainer.visibility = View.VISIBLE
        } else {
            binding.trailingViewContainer.removeAllViews()
            binding.trailingViewContainer.visibility = View.GONE
        }
    }

    /**
     * Sets whether to hide the group type indicator.
     */
    fun setHideGroupType(hide: Boolean) {
        hideGroupType = hide
        bindLeading()
    }

    /**
     * Sets whether to hide the separator.
     */
    fun setHideSeparator(hide: Boolean) {
        hideSeparator = hide
        binding.separator.visibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Sets the style for this item.
     */
    fun setStyle(style: CometChatGroupsItemStyle) {
        this.style = style
        applyStyle()
        bindGroup()
    }

    // ==================== Private Methods ====================

    private fun setupClickListeners() {
        binding.parentLayout.setOnClickListener {
            group?.let { g -> onItemClick?.invoke(g) }
        }
        binding.parentLayout.setOnLongClickListener {
            group?.let { g -> onItemLongClick?.invoke(g) }
            true
        }
    }

    private fun bindGroup() {
        group?.let {
            bindLeading()
            bindTitle()
            bindSubtitle()
            updateSelectionState()
            updateAccessibility()
        }
    }

    private fun bindLeading() {
        if (customLeadingView != null) return

        group?.let { g ->
            binding.groupsAvatar.visibility = View.VISIBLE
            binding.groupsAvatar.setAvatar(g.name, g.icon)
            binding.groupsAvatar.setStyle(style.avatarStyle)

            if (!hideGroupType) {
                val indicator = when (g.groupType) {
                    CometChatConstants.GROUP_TYPE_PRIVATE -> StatusIndicator.PRIVATE_GROUP
                    CometChatConstants.GROUP_TYPE_PASSWORD -> StatusIndicator.PROTECTED_GROUP
                    else -> null // Public groups don't show indicator
                }
                
                if (indicator != null) {
                    binding.groupsTypeIndicator.visibility = View.VISIBLE
                    binding.groupsTypeIndicator.setStatusIndicator(indicator)
                    binding.groupsTypeIndicator.setStyle(style.statusIndicatorStyle)
                } else {
                    binding.groupsTypeIndicator.visibility = View.GONE
                }
            } else {
                binding.groupsTypeIndicator.visibility = View.GONE
            }
        }
    }

    private fun bindTitle() {
        if (customTitleView != null) return

        group?.let { g ->
            binding.tvGroupsTitle.visibility = View.VISIBLE
            binding.tvGroupsTitle.text = g.name
            // Apply title text color - use style value or fall back to theme default
            val titleColor = if (style.titleTextColor != 0) style.titleTextColor else com.cometchat.uikit.kotlin.theme.CometChatTheme.getTextColorPrimary(context)
            binding.tvGroupsTitle.setTextColor(titleColor)
            // Apply title text appearance - use style value or fall back to theme default
            val titleAppearance = if (style.titleTextAppearance != 0) style.titleTextAppearance else com.cometchat.uikit.kotlin.theme.CometChatTheme.getTextAppearanceHeading4Medium(context)
            binding.tvGroupsTitle.setTextAppearance(titleAppearance)
        }
    }

    private fun bindSubtitle() {
        if (customSubtitleView != null) return

        group?.let { g ->
            binding.tvGroupsSubtitle.visibility = View.VISIBLE
            val membersText = context.getString(R.string.cometchat_members_count, g.membersCount)
            binding.tvGroupsSubtitle.text = membersText
            // Apply subtitle text color - use style value or fall back to theme default
            val subtitleColor = if (style.subtitleTextColor != 0) style.subtitleTextColor else com.cometchat.uikit.kotlin.theme.CometChatTheme.getTextColorSecondary(context)
            binding.tvGroupsSubtitle.setTextColor(subtitleColor)
            // Apply subtitle text appearance - use style value or fall back to theme default
            val subtitleAppearance = if (style.subtitleTextAppearance != 0) style.subtitleTextAppearance else com.cometchat.uikit.kotlin.theme.CometChatTheme.getTextAppearanceBodyRegular(context)
            binding.tvGroupsSubtitle.setTextAppearance(subtitleAppearance)
        }
    }

    private fun updateSelectionState() {
        // Update checkbox visibility and styling (matches ConversationListItem implementation)
        if (selectionMode != UIKitConstants.SelectionMode.NONE) {
            // Initialize MaterialCardView styling - use dimension resources to match Java implementation
            val checkBoxStrokeWidth = com.cometchat.uikit.kotlin.shared.resources.utils.Utils.convertDpToPx(context, 1)
            val checkBoxCornerRadius = resources.getDimension(R.dimen.cometchat_radius_2)
            val checkBoxStrokeColor = com.cometchat.uikit.kotlin.theme.CometChatTheme.getStrokeColorDefault(context)
            val checkBoxBackgroundColor = com.cometchat.uikit.kotlin.theme.CometChatTheme.getBackgroundColor1(context)
            val checkBoxCheckedBackgroundColor = com.cometchat.uikit.kotlin.theme.CometChatTheme.getPrimaryColor(context)
            val checkBoxSelectIconTint = com.cometchat.uikit.kotlin.theme.CometChatTheme.getColorWhite(context)
            
            binding.checkboxView.radius = checkBoxCornerRadius
            binding.ivCheckbox.setImageResource(R.drawable.cometchat_ic_check)
            binding.ivCheckbox.setColorFilter(checkBoxSelectIconTint)
            
            if (isItemSelected) {
                binding.ivCheckbox.visibility = View.VISIBLE
                binding.checkboxView.strokeWidth = 0
                binding.checkboxView.setCardBackgroundColor(checkBoxCheckedBackgroundColor)
                val selectedBgColor = if (style.selectedBackgroundColor != 0) style.selectedBackgroundColor else com.cometchat.uikit.kotlin.theme.CometChatTheme.getBackgroundColor3(context)
                binding.parentLayout.setBackgroundColor(selectedBgColor)
            } else {
                binding.ivCheckbox.visibility = View.GONE
                binding.checkboxView.strokeWidth = checkBoxStrokeWidth
                binding.checkboxView.strokeColor = checkBoxStrokeColor
                binding.checkboxView.setCardBackgroundColor(checkBoxBackgroundColor)
                binding.parentLayout.setBackgroundColor(style.backgroundColor)
            }
            binding.checkboxView.visibility = View.VISIBLE
        } else {
            binding.checkboxView.visibility = View.GONE
            binding.parentLayout.setBackgroundColor(style.backgroundColor)
        }
    }

    private fun updateAccessibility() {
        group?.let { g ->
            val description = buildString {
                append(g.name)
                append(", ")
                append(context.getString(R.string.cometchat_members_count, g.membersCount))
                when (g.groupType) {
                    CometChatConstants.GROUP_TYPE_PRIVATE -> append(", private group")
                    CometChatConstants.GROUP_TYPE_PASSWORD -> append(", password protected group")
                    else -> append(", public group")
                }
                if (isItemSelected) append(", selected")
            }
            binding.parentLayout.contentDescription = description
        }
    }

    private fun applyStyle() {
        // Apply separator style - use style value or fall back to theme default
        val separatorColor = if (style.separatorColor != 0) style.separatorColor else com.cometchat.uikit.kotlin.theme.CometChatTheme.getStrokeColorLight(context)
        binding.separator.setBackgroundColor(separatorColor)
        if (style.separatorHeight > 0) {
            val params = binding.separator.layoutParams
            params.height = style.separatorHeight
            binding.separator.layoutParams = params
        }
    }

    private fun resetLeadingView() {
        // Remove any custom views from container
        binding.leadingViewContainer.removeAllViews()
        
        // Re-add default views - ensure they're not attached to another parent
        (binding.groupsAvatar.parent as? android.view.ViewGroup)?.removeView(binding.groupsAvatar)
        (binding.groupsTypeIndicator.parent as? android.view.ViewGroup)?.removeView(binding.groupsTypeIndicator)
        
        binding.leadingViewContainer.addView(binding.groupsAvatar)
        binding.leadingViewContainer.addView(binding.groupsTypeIndicator)
        binding.groupsAvatar.visibility = View.VISIBLE
        
        // Re-bind to update with current group data
        bindLeading()
    }

    private fun resetTitleView() {
        // Remove any custom views from container
        binding.titleViewContainer.removeAllViews()
        
        // Re-add default view - ensure it's not attached to another parent
        (binding.tvGroupsTitle.parent as? android.view.ViewGroup)?.removeView(binding.tvGroupsTitle)
        
        binding.titleViewContainer.addView(binding.tvGroupsTitle)
        binding.tvGroupsTitle.visibility = View.VISIBLE
        
        // Re-bind to update with current group data
        bindTitle()
    }

    private fun resetSubtitleView() {
        // Remove any custom views from container
        binding.subtitleViewContainer.removeAllViews()
        
        // Re-add default view - ensure it's not attached to another parent
        (binding.tvGroupsSubtitle.parent as? android.view.ViewGroup)?.removeView(binding.tvGroupsSubtitle)
        
        binding.subtitleViewContainer.addView(binding.tvGroupsSubtitle)
        binding.tvGroupsSubtitle.visibility = View.VISIBLE
        
        // Re-bind to update with current group data
        bindSubtitle()
    }

    /**
     * Restores the entire default layout when a custom item view is removed.
     * This method rebuilds all default views (leading, title, subtitle, trailing)
     * and re-binds the current group data.
     */
    fun restoreDefaultLayout() {
        // Clear all custom views
        customLeadingView = null
        customTitleView = null
        customSubtitleView = null
        customTrailingView = null

        // Restore leading view (avatar and status indicator)
        binding.leadingViewContainer.removeAllViews()
        (binding.groupsAvatar.parent as? android.view.ViewGroup)?.removeView(binding.groupsAvatar)
        (binding.groupsTypeIndicator.parent as? android.view.ViewGroup)?.removeView(binding.groupsTypeIndicator)
        binding.leadingViewContainer.addView(binding.groupsAvatar)
        binding.leadingViewContainer.addView(binding.groupsTypeIndicator)
        binding.groupsAvatar.visibility = View.VISIBLE
        
        // Restore title view
        binding.titleViewContainer.removeAllViews()
        (binding.tvGroupsTitle.parent as? android.view.ViewGroup)?.removeView(binding.tvGroupsTitle)
        binding.titleViewContainer.addView(binding.tvGroupsTitle)
        binding.tvGroupsTitle.visibility = View.VISIBLE
        
        // Restore subtitle view
        binding.subtitleViewContainer.removeAllViews()
        (binding.tvGroupsSubtitle.parent as? android.view.ViewGroup)?.removeView(binding.tvGroupsSubtitle)
        binding.subtitleViewContainer.addView(binding.tvGroupsSubtitle)
        binding.tvGroupsSubtitle.visibility = View.VISIBLE
        
        // Restore trailing view (empty by default)
        binding.trailingViewContainer.removeAllViews()
        binding.trailingViewContainer.visibility = View.GONE
        
        // Re-apply styles
        applyStyle()
        
        // Re-bind group data if available
        group?.let { bindGroup() }
    }
}
