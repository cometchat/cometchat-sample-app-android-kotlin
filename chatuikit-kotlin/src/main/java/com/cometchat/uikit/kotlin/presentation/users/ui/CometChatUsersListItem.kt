package com.cometchat.uikit.kotlin.presentation.users.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatUsersListItemBinding
import com.cometchat.uikit.kotlin.presentation.users.style.CometChatUsersListItemStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * CometChatUsersListItem is a custom Android View that displays a single user item
 * with avatar, status indicator, and name.
 *
 * This component can be used standalone or within a RecyclerView for displaying user lists.
 * It supports full customization through styles and custom view slots.
 */
class CometChatUsersListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatUsersListItem::class.java.simpleName
    }

    // View Binding - Standard layout inflation (not merge)
    internal val binding: CometchatUsersListItemBinding = CometchatUsersListItemBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    // Current user
    private var user: User? = null

    // Selection state
    private var isSelected: Boolean = false
    private var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE

    // Callbacks
    private var onItemClick: ((User) -> Unit)? = null
    private var onItemLongClick: ((User) -> Unit)? = null

    // Custom views
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // Visibility controls
    private var hideUserStatus: Boolean = false
    private var hideSeparator: Boolean = false

    // Style
    private var style: CometChatUsersListItemStyle = CometChatUsersListItemStyle()

    init {
        applyStyleAttributes(attrs, defStyleAttr)
        setupClickListeners()
    }

    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatUsers, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatUsers_cometchatUsersStyle, 0
        )
        typedArray.recycle()

        if (styleResId != 0) {
            typedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.CometChatUsers, defStyleAttr, styleResId
            )
            style = CometChatUsersListItemStyle.fromTypedArray(context, typedArray)
        } else {
            style = CometChatUsersListItemStyle.default(context)
        }
        applyStyle()
    }

    private fun applyStyle() {
        // Background
        if (style.backgroundColor != 0) {
            binding.parentLayout.setBackgroundColor(style.backgroundColor)
        }

        // Title
        if (style.titleTextColor != 0) {
            binding.tvUserName.setTextColor(style.titleTextColor)
        }
        if (style.titleTextAppearance != 0) {
            binding.tvUserName.setTextAppearance(style.titleTextAppearance)
        }

        // Avatar style
        binding.userAvatar.setStyle(style.avatarStyle)

        // Status indicator style
        binding.userStatusIndicator.setStyle(style.statusIndicatorStyle)
        
        // Separator styles - matches CometChatGroupsItem implementation
        applySeparatorStyles()
    }

    /**
     * Applies styles to the separator view.
     * Note: Visibility is NOT set here - it's controlled by setHideSeparator() to match
     * CometChatGroupsItem behavior and ensure proper toggle functionality.
     */
    private fun applySeparatorStyles() {
        // Apply separator style - use style value or fall back to theme default
        val separatorColor = if (style.separatorColor != 0) style.separatorColor else CometChatTheme.getStrokeColorLight(context)
        binding.separator.setBackgroundColor(separatorColor)
        if (style.separatorHeight > 0) {
            val params = binding.separator.layoutParams
            params.height = style.separatorHeight
            binding.separator.layoutParams = params
        }
    }

    private fun setupClickListeners() {
        binding.parentLayout.setOnClickListener {
            user?.let { u ->
                onItemClick?.invoke(u)
            }
        }

        binding.parentLayout.setOnLongClickListener {
            user?.let { u ->
                onItemLongClick?.invoke(u)
            }
            true
        }
    }

    // ==================== Public API Methods ====================

    /**
     * Sets the user to display.
     */
    fun setUser(user: User) {
        this.user = user
        bindUser()
    }

    /**
     * Sets the selection state.
     */
    fun setItemSelected(selected: Boolean) {
        this.isSelected = selected
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
    fun setOnItemClick(callback: (User) -> Unit) {
        onItemClick = callback
    }

    /**
     * Sets the item long click callback.
     */
    fun setOnItemLongClick(callback: (User) -> Unit) {
        onItemLongClick = callback
    }

    /**
     * Sets custom leading view (replaces avatar section).
     */
    fun setLeadingView(view: View?) {
        customLeadingView = view
        if (view != null) {
            binding.userLeadingView.removeAllViews()
            binding.userLeadingView.addView(view)
        } else {
            resetLeadingView()
        }
    }

    /**
     * Sets custom title view.
     */
    fun setTitleView(view: View?) {
        customTitleView = view
        if (view != null) {
            binding.userTitleView.removeAllViews()
            binding.userTitleView.addView(view)
        } else {
            resetTitleView()
        }
    }

    /**
     * Sets custom subtitle view.
     */
    fun setSubtitleView(view: View?) {
        customSubtitleView = view
        if (view != null) {
            binding.subtitleView.removeAllViews()
            binding.subtitleView.addView(view)
            binding.subtitleView.visibility = View.VISIBLE
        } else {
            binding.subtitleView.removeAllViews()
            binding.subtitleView.visibility = View.GONE
        }
    }

    /**
     * Sets custom trailing view.
     */
    fun setTrailingView(view: View?) {
        customTrailingView = view
        if (view != null) {
            binding.tailView.removeAllViews()
            binding.tailView.addView(view)
        } else {
            binding.tailView.removeAllViews()
        }
    }

    /**
     * Sets whether to hide user status indicator.
     */
    fun setHideUserStatus(hide: Boolean) {
        hideUserStatus = hide
        bindLeading()
    }

    /**
     * Sets whether to hide item separator.
     * Matches CometChatGroupsItem implementation.
     */
    fun setHideSeparator(hide: Boolean) {
        hideSeparator = hide
        binding.separator.visibility = if (hide) View.GONE else View.VISIBLE
    }

    /**
     * Sets the separator color.
     */
    fun setSeparatorColor(@androidx.annotation.ColorInt color: Int) {
        style = style.copy(separatorColor = color)
        if (color != 0) binding.separator.setBackgroundColor(color)
    }

    /**
     * Sets the separator height.
     */
    fun setSeparatorHeight(@androidx.annotation.Dimension height: Int) {
        style = style.copy(separatorHeight = height)
        if (height > 0) {
            val params = binding.separator.layoutParams
            params.height = height
            binding.separator.layoutParams = params
        }
    }

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatUsersListItemStyle) {
        this.style = style
        applyStyle()
        user?.let { bindUser() }
    }

    /**
     * Gets the parent layout for full item replacement.
     */
    fun getParentLayout(): ViewGroup = binding.parentLayout

    /**
     * Restores the entire default layout.
     */
    fun restoreDefaultLayout() {
        customLeadingView = null
        customTitleView = null
        customSubtitleView = null
        customTrailingView = null

        resetLeadingView()
        resetTitleView()
        binding.subtitleView.removeAllViews()
        binding.subtitleView.visibility = View.GONE
        binding.tailView.removeAllViews()

        applyStyle()
        user?.let { bindUser() }
    }

    private fun resetLeadingView() {
        customLeadingView = null
        binding.userLeadingView.removeAllViews()
        binding.userLeadingView.addView(binding.userAvatar)
        binding.userLeadingView.addView(binding.userStatusIndicator)
        bindLeading()
    }

    private fun resetTitleView() {
        customTitleView = null
        binding.userTitleView.removeAllViews()
        binding.userTitleView.addView(binding.tvUserName)
        bindTitle()
    }

    // ==================== Private Binding Methods ====================

    private fun bindUser() {
        user?.let {
            bindLeading()
            bindTitle()
            updateSelectionState()
        }
    }

    private fun bindLeading() {
        if (customLeadingView != null) return

        user?.let { u ->
            binding.userAvatar.setAvatar(u.name ?: "", u.avatar)
            if (!hideUserStatus) {
                binding.userStatusIndicator.visibility = View.VISIBLE
                // Blocked users always show offline, matching original chatuikit implementation
                val statusIndicator = if (Utils.isBlocked(u)) {
                    StatusIndicator.OFFLINE
                } else if (u.status == CometChatConstants.USER_STATUS_ONLINE) {
                    StatusIndicator.ONLINE
                } else {
                    StatusIndicator.OFFLINE
                }
                binding.userStatusIndicator.setStatusIndicator(statusIndicator)
            } else {
                binding.userStatusIndicator.visibility = View.GONE
            }
        }
    }

    private fun bindTitle() {
        if (customTitleView != null) return

        user?.let { u ->
            binding.tvUserName.text = u.name ?: ""
        }
    }

    /**
     * Updates the selection state - matches ConversationListItem implementation.
     */
    private fun updateSelectionState() {
        // Update checkbox visibility and styling (matches Java chatuikit implementation)
        if (selectionMode != UIKitConstants.SelectionMode.NONE) {
            // Initialize MaterialCardView styling - use dimension resources to match Java implementation
            val checkBoxStrokeWidth = Utils.convertDpToPx(context, 1)
            val checkBoxCornerRadius = resources.getDimension(R.dimen.cometchat_radius_2)
            val checkBoxStrokeColor = CometChatTheme.getStrokeColorDefault(context)
            val checkBoxBackgroundColor = CometChatTheme.getBackgroundColor1(context)
            val checkBoxCheckedBackgroundColor = CometChatTheme.getPrimaryColor(context)
            val checkBoxSelectIconTint = CometChatTheme.getColorWhite(context)
            
            binding.checkboxView.radius = checkBoxCornerRadius
            binding.ivCheckbox.setImageResource(R.drawable.cometchat_ic_check)
            binding.ivCheckbox.setColorFilter(checkBoxSelectIconTint)
            
            if (isSelected) {
                binding.ivCheckbox.visibility = VISIBLE
                binding.checkboxView.strokeWidth = 0
                binding.checkboxView.setCardBackgroundColor(checkBoxCheckedBackgroundColor)
                val selectedBgColor = if (style.selectedBackgroundColor != 0) style.selectedBackgroundColor else CometChatTheme.getBackgroundColor3(context)
                binding.parentLayout.setBackgroundColor(selectedBgColor)
            } else {
                binding.ivCheckbox.visibility = GONE
                binding.checkboxView.strokeWidth = checkBoxStrokeWidth
                binding.checkboxView.strokeColor = checkBoxStrokeColor
                binding.checkboxView.setCardBackgroundColor(checkBoxBackgroundColor)
                binding.parentLayout.setBackgroundColor(style.backgroundColor)
            }
            binding.checkboxView.visibility = VISIBLE
        } else {
            binding.checkboxView.visibility = GONE
            binding.parentLayout.setBackgroundColor(style.backgroundColor)
        }
    }
}
