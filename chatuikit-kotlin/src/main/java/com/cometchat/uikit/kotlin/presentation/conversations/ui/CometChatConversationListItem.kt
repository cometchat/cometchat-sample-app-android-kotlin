package com.cometchat.uikit.kotlin.presentation.conversations.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatConversationListItemSubtitleBinding
import com.cometchat.uikit.kotlin.databinding.CometchatConversationListItemTailBinding
import com.cometchat.uikit.kotlin.databinding.CometchatConversationsListItemsBinding
import com.cometchat.uikit.kotlin.presentation.conversations.style.CometChatConversationListItemStyle
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationUtils
import com.cometchat.uikit.kotlin.presentation.conversations.utils.TypingIndicator
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.avatar.CometChatAvatarStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount.CometChatBadgeCount
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount.CometChatBadgeCountStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDateStyle
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceipt
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceiptStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.CometChatStatusIndicatorStyle
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.kotlin.presentation.shared.typingindicator.CometChatTypingIndicatorStyle
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils
import com.cometchat.uikit.kotlin.theme.CometChatTheme

/**
 * CometChatConversationListItem is a custom Android View that displays a single conversation item
 * with avatar, status indicator, title, subtitle (last message), date, and unread badge.
 *
 * This component can be used standalone or within a RecyclerView for displaying conversation lists.
 * It supports full customization through styles and custom view slots.
 *
 * Usage in XML:
 * ```xml
 * <com.cometchat.uikit.kotlin.presentation.ui.conversations.CometChatConversationListItem
 *     android:id="@+id/conversationItem"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:cometchatConversationListItemStyle="@style/CometChatConversationListItemStyle" />
 * ```
 *
 * Usage in Kotlin:
 * ```kotlin
 * val conversationItem = CometChatConversationListItem(context)
 * conversationItem.setConversation(conversation)
 * conversationItem.setOnItemClick { conversation ->
 *     // Handle click
 * }
 * ```
 */
class CometChatConversationListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = CometChatConversationListItem::class.java.simpleName
    }

    // View Binding - internal visibility for ViewHolder access
    // Inflate layout
    internal val binding: CometchatConversationsListItemsBinding = CometchatConversationsListItemsBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    // View Bindings for subtitle and tail views (inflated from XML)
    private var subtitleBinding: CometchatConversationListItemSubtitleBinding? = null
    private var tailBinding: CometchatConversationListItemTailBinding? = null

    // Current conversation
    private var conversation: Conversation? = null

    // Typing indicator info (supports multiple users typing)
    private var typingIndicatorInfo: TypingIndicator? = null

    // Selection state
    private var isSelected: Boolean = false
    private var selectionMode: UIKitConstants.SelectionMode = UIKitConstants.SelectionMode.NONE

    // Callbacks
    private var onItemClick: ((Conversation) -> Unit)? = null
    private var onItemLongClick: ((Conversation) -> Unit)? = null

    // Custom views
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // Text formatters
    private var textFormatters: List<CometChatTextFormatter> = emptyList()

    // Date formatter
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    // Visibility controls
    private var hideUserStatus: Boolean = false
    private var hideGroupType: Boolean = false
    private var hideReceipts: Boolean = false
    private var hideSeparator: Boolean = false

    // Single style object - NO individual style properties
    private var style: CometChatConversationListItemStyle = CometChatConversationListItemStyle()

    // View references obtained from bindings
    private val receiptView: CometChatReceipt?
        get() = subtitleBinding?.receiptView

    private val senderPrefixTextView: TextView?
        get() = subtitleBinding?.tvSenderPrefix

    private val messageTypeIconView: ImageView?
        get() = subtitleBinding?.ivMessageTypeIcon

    private val subtitleTextView: TextView?
        get() = subtitleBinding?.tvSubtitle

    private val typingIndicatorTextView: TextView?
        get() = subtitleBinding?.tvTypingIndicator

    private val dateView: CometChatDate?
        get() = tailBinding?.dateView

    private val badgeView: CometChatBadgeCount?
        get() = tailBinding?.badgeView

    // Separator view
    private var separatorView: View? = null

    init {

        // Apply XML attributes
        applyStyleAttributes(attrs, defStyleAttr)

        // Initialize default views
        initDefaultSubtitleViews()
        initDefaultTrailingViews()
        initSeparatorView()

        // Setup click listeners
        setupClickListeners()
    }

    /**
     * Applies style attributes from XML using the style class factory method.
     */
    private fun applyStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatConversations, defStyleAttr, 0
        )
        val styleResId = typedArray.getResourceId(
            R.styleable.CometChatConversations_cometchatConversationsStyle, 0
        )
        typedArray.recycle()

        typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.CometChatConversations, defStyleAttr, styleResId
        )
        // fromTypedArray handles recycling internally
        style = CometChatConversationListItemStyle.fromTypedArray(context, typedArray)
        applyStyle()
    }

    /**
     * Applies all style properties from the style object to views.
     */
    private fun applyStyle() {
        // Background
        if (style.backgroundColor != 0) {
            binding.parentLayout.setBackgroundColor(style.backgroundColor)
        }

        // Title
        if (style.titleTextColor != 0) {
            binding.tvConversationsTitle.setTextColor(style.titleTextColor)
        }
        if (style.titleTextAppearance != 0) {
            binding.tvConversationsTitle.setTextAppearance(style.titleTextAppearance)
        }

        // Apply styles to inflated subtitle and tail views
        applySubtitleStyles()
        applyTailStyles()
        
        // Apply separator styles
        applySeparatorStyles()
        
        // Apply nested component styles
        applyNestedComponentStyles()
    }
    
    /**
     * Applies nested component styles (avatar, status indicator, date, badge, receipt).
     */
    private fun applyNestedComponentStyles() {
        // Apply avatar style
        binding.conversationsAvatar.setStyle(style.avatarStyle)
        
        // Apply status indicator style
        binding.conversationsStatusAndTypeIndicator.setStyle(style.statusIndicatorStyle)
        
        // Apply date style to tail view if available
        dateView?.setStyle(style.dateStyle)
        
        // Apply badge style to tail view if available
        badgeView?.setStyle(style.badgeCountStyle)
        
        // Apply receipt style to subtitle view if available
        receiptView?.setStyle(style.receiptStyle)
    }
    
    /**
     * Applies styles to the separator view.
     */
    private fun applySeparatorStyles() {
        separatorView?.let { view ->
            if (style.separatorColor != 0) {
                view.setBackgroundColor(style.separatorColor)
            }
            if (style.separatorHeight != 0) {
                view.layoutParams = (view.layoutParams as? LayoutParams)?.apply {
                    height = style.separatorHeight
                }
            }
            view.visibility = if (hideSeparator) GONE else VISIBLE
        }
    }

    private fun initDefaultSubtitleViews() {
        binding.subtitleView.removeAllViews()

        // Inflate subtitle layout from XML
        subtitleBinding = CometchatConversationListItemSubtitleBinding.inflate(
            LayoutInflater.from(context),
            binding.subtitleView,
            true
        )

        // Apply initial styles to inflated views
        applySubtitleStyles()
    }

    /**
     * Applies styles to the subtitle views.
     */
    private fun applySubtitleStyles() {
        subtitleBinding?.let { binding ->
            // Apply subtitle text color and appearance
            if (style.subtitleTextColor != 0) {
                binding.tvSubtitle.setTextColor(style.subtitleTextColor)
            }
            if (style.subtitleTextAppearance != 0) {
                binding.tvSubtitle.setTextAppearance(style.subtitleTextAppearance)
            }

            // Apply sender prefix styling (color and bold typeface)
            if (style.subtitleTextColor != 0) {
                binding.tvSenderPrefix.setTextColor(style.subtitleTextColor)
            }
            binding.tvSenderPrefix.setTypeface(binding.tvSenderPrefix.typeface, Typeface.BOLD)
            if (style.subtitleTextAppearance != 0) {
                binding.tvSenderPrefix.setTextAppearance(style.subtitleTextAppearance)
            }

            // Apply message type icon tint
            if (style.messageTypeIconTint != 0) {
                binding.ivMessageTypeIcon.setColorFilter(style.messageTypeIconTint)
            } else {
                binding.ivMessageTypeIcon.setColorFilter(CometChatTheme.getTextColorSecondary(context))
            }
        }
    }

    private fun initDefaultTrailingViews() {
        binding.tailView.removeAllViews()

        // Inflate tail layout from XML
        tailBinding = CometchatConversationListItemTailBinding.inflate(
            LayoutInflater.from(context),
            binding.tailView,
            true
        )

        // Apply initial styles to inflated views
        applyTailStyles()
    }

    /**
     * Applies styles to the tail views.
     */
    private fun applyTailStyles() {
        tailBinding?.let { binding ->
            // Apply date view styling
            binding.dateView.setDateTextAlignment(TEXT_ALIGNMENT_VIEW_END)
            binding.dateView.setDateTextColor(CometChatTheme.getTextColorSecondary(context))
            binding.dateView.setTransparentBackground(true)

            // Apply badge view styling
            binding.badgeView.setBadgeCornerRadius(resources.getDimension(R.dimen.cometchat_radius_max))
        }
    }

    private fun initSeparatorView() {
        // Add separator at the bottom
        val separatorHeightValue = if (style.separatorHeight != 0) style.separatorHeight else Utils.convertDpToPx(context, 1)
        val separatorColorValue = if (style.separatorColor != 0) style.separatorColor else CometChatTheme.getStrokeColorLight(context)
        
        separatorView = View(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                separatorHeightValue
            ).apply {
                topToBottom = binding.parentLayout.id
                startToStart = LayoutParams.PARENT_ID
                endToEnd = LayoutParams.PARENT_ID
            }
            setBackgroundColor(separatorColorValue)
            visibility = if (hideSeparator) GONE else VISIBLE
        }
        addView(separatorView)
    }

    /**
     * Sets up click listeners for the item.
     */
    private fun setupClickListeners() {
        binding.parentLayout.setOnClickListener {
            conversation?.let { conv ->
                onItemClick?.invoke(conv)
            }
        }

        binding.parentLayout.setOnLongClickListener {
            conversation?.let { conv ->
                onItemLongClick?.invoke(conv)
            }
            true
        }
    }

    // ==================== Public API Methods ====================

    /**
     * Sets the conversation to display.
     */
    fun setConversation(conversation: Conversation) {
        this.conversation = conversation
        bindConversation()
    }

    /**
     * Sets the typing indicator info for this conversation.
     * Supports multiple users typing simultaneously (common in group conversations).
     */
    fun setTypingIndicator(indicator: TypingIndicator?) {
        this.typingIndicatorInfo = indicator
        bindSubtitle()
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
    fun setOnItemClick(callback: (Conversation) -> Unit) {
        onItemClick = callback
    }

    /**
     * Sets the item long click callback.
     */
    fun setOnItemLongClick(callback: (Conversation) -> Unit) {
        onItemLongClick = callback
    }

    /**
     * Sets custom leading view (replaces avatar section).
     * Pass null to restore the default leading view.
     */
    fun setLeadingView(view: View?) {
        customLeadingView = view
        if (view != null) {
            binding.conversationLeadingView.removeAllViews()
            binding.conversationLeadingView.addView(view)
        } else {
            // Restore default leading view
            resetLeadingView()
        }
    }

    /**
     * Sets custom title view.
     * Pass null to restore the default title view.
     */
    fun setTitleView(view: View?) {
        customTitleView = view
        if (view != null) {
            binding.conversationsTitleView.removeAllViews()
            binding.conversationsTitleView.addView(view)
        } else {
            // Restore default title view
            resetTitleView()
        }
    }

    /**
     * Sets custom subtitle view.
     * Pass null to restore the default subtitle view.
     */
    fun setSubtitleView(view: View?) {
        customSubtitleView = view
        if (view != null) {
            // Clear subtitle binding reference when custom view is set
            subtitleBinding = null
            binding.subtitleView.removeAllViews()
            binding.subtitleView.addView(view)
        } else {
            // Restore default subtitle view
            resetSubtitleView()
        }
    }

    /**
     * Sets custom trailing view (replaces date/badge section).
     * Pass null to restore the default trailing view.
     */
    fun setTrailingView(view: View?) {
        customTrailingView = view
        if (view != null) {
            // Clear tail binding reference when custom view is set
            tailBinding = null
            binding.tailView.removeAllViews()
            binding.tailView.addView(view)
        } else {
            // Restore default trailing view
            resetTrailingView()
        }
    }

    /**
     * Sets text formatters for message preview.
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        textFormatters = formatters
        bindSubtitle()
    }

    /**
     * Sets custom date/time formatter.
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        dateTimeFormatter = formatter
        bindTrailing()
    }

    /**
     * Sets whether to hide user status indicator.
     */
    fun setHideUserStatus(hide: Boolean) {
        hideUserStatus = hide
        bindLeading()
    }

    /**
     * Sets whether to hide group type indicator.
     */
    fun setHideGroupType(hide: Boolean) {
        hideGroupType = hide
        bindLeading()
    }

    /**
     * Sets whether to hide message receipts.
     */
    fun setHideReceipts(hide: Boolean) {
        hideReceipts = hide
        bindSubtitle()
    }

    /**
     * Sets whether to hide item separator.
     */
    fun setHideSeparator(hide: Boolean) {
        hideSeparator = hide
        separatorView?.visibility = if (hide) GONE else VISIBLE
    }

    /**
     * Sets the style from a style object.
     */
    fun setStyle(style: CometChatConversationListItemStyle) {
        this.style = style
        applyStyle()
        bindConversation()
    }

    /**
     * Sets the style from a style resource.
     */
    fun setStyle(@StyleRes styleRes: Int) {
        if (styleRes != 0) {
            val typedArray = context.theme.obtainStyledAttributes(
                styleRes, R.styleable.CometChatConversations
            )
            // fromTypedArray handles recycling internally
            setStyle(CometChatConversationListItemStyle.fromTypedArray(context, typedArray))
        }
    }

    // ==================== Getters (read from style object) ====================

    /**
     * Gets the background color.
     */
    fun getItemBackgroundColor(): Int = style.backgroundColor

    /**
     * Gets the selected background color.
     */
    fun getSelectedBackgroundColor(): Int = style.selectedBackgroundColor

    /**
     * Gets the title text color.
     */
    fun getTitleTextColor(): Int = style.titleTextColor

    /**
     * Gets the title text appearance.
     */
    fun getTitleTextAppearance(): Int = style.titleTextAppearance

    /**
     * Gets the subtitle text color.
     */
    fun getSubtitleTextColor(): Int = style.subtitleTextColor

    /**
     * Gets the subtitle text appearance.
     */
    fun getSubtitleTextAppearance(): Int = style.subtitleTextAppearance

    /**
     * Gets the message type icon tint.
     */
    fun getMessageTypeIconTint(): Int = style.messageTypeIconTint

    /**
     * Gets the separator color.
     */
    fun getSeparatorColor(): Int = style.separatorColor

    /**
     * Gets the separator height.
     */
    fun getSeparatorHeight(): Int = style.separatorHeight

    /**
     * Gets the avatar style.
     */
    fun getAvatarStyle(): CometChatAvatarStyle = style.avatarStyle

    /**
     * Gets the status indicator style.
     */
    fun getStatusIndicatorStyle(): CometChatStatusIndicatorStyle = style.statusIndicatorStyle

    /**
     * Gets the date style.
     */
    fun getDateStyle(): CometChatDateStyle = style.dateStyle

    /**
     * Gets the badge count style.
     */
    fun getBadgeCountStyle(): CometChatBadgeCountStyle = style.badgeCountStyle

    /**
     * Gets the receipt style.
     */
    fun getReceiptStyle(): CometChatReceiptStyle = style.receiptStyle

    /**
     * Gets the typing indicator style.
     */
    fun getTypingIndicatorStyle(): CometChatTypingIndicatorStyle = style.typingIndicatorStyle

    // ==================== Setters (update style object + apply) ====================

    /**
     * Sets the background color.
     */
    fun setItemBackgroundColor(@ColorInt color: Int) {
        style = style.copy(backgroundColor = color)
        if (color != 0) binding.parentLayout.setBackgroundColor(color)
    }

    /**
     * Sets the selected background color.
     */
    fun setSelectedBackgroundColor(@ColorInt color: Int) {
        style = style.copy(selectedBackgroundColor = color)
        updateSelectionState()
    }

    /**
     * Sets the title text color.
     */
    fun setTitleTextColor(@ColorInt color: Int) {
        style = style.copy(titleTextColor = color)
        if (color != 0) binding.tvConversationsTitle.setTextColor(color)
    }

    /**
     * Sets the title text appearance.
     */
    fun setTitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(titleTextAppearance = appearance)
        if (appearance != 0) binding.tvConversationsTitle.setTextAppearance(appearance)
    }

    /**
     * Sets the subtitle text color.
     */
    fun setSubtitleTextColor(@ColorInt color: Int) {
        style = style.copy(subtitleTextColor = color)
        if (color != 0) {
            subtitleBinding?.tvSubtitle?.setTextColor(color)
            subtitleBinding?.tvSenderPrefix?.setTextColor(color)
        }
    }

    /**
     * Sets the subtitle text appearance.
     */
    fun setSubtitleTextAppearance(@StyleRes appearance: Int) {
        style = style.copy(subtitleTextAppearance = appearance)
        if (appearance != 0) {
            subtitleBinding?.tvSubtitle?.setTextAppearance(appearance)
            subtitleBinding?.tvSenderPrefix?.setTextAppearance(appearance)
        }
    }

    /**
     * Sets the message type icon tint.
     */
    fun setMessageTypeIconTint(@ColorInt color: Int) {
        style = style.copy(messageTypeIconTint = color)
        if (color != 0) subtitleBinding?.ivMessageTypeIcon?.setColorFilter(color)
    }

    /**
     * Sets the separator color.
     */
    fun setSeparatorColor(@ColorInt color: Int) {
        style = style.copy(separatorColor = color)
        if (color != 0) separatorView?.setBackgroundColor(color)
    }

    /**
     * Sets the separator height.
     */
    fun setSeparatorHeight(@Dimension height: Int) {
        style = style.copy(separatorHeight = height)
        if (height != 0) {
            separatorView?.layoutParams = (separatorView?.layoutParams as? LayoutParams)?.apply {
                this.height = height
            }
        }
    }

    /**
     * Sets the avatar style.
     */
    fun setAvatarStyle(avatarStyle: CometChatAvatarStyle) {
        style = style.copy(avatarStyle = avatarStyle)
        binding.conversationsAvatar.setStyle(avatarStyle)
    }

    /**
     * Sets the status indicator style.
     */
    fun setStatusIndicatorStyle(statusIndicatorStyle: CometChatStatusIndicatorStyle) {
        style = style.copy(statusIndicatorStyle = statusIndicatorStyle)
        binding.conversationsStatusAndTypeIndicator.setStyle(statusIndicatorStyle)
    }

    /**
     * Sets the date style.
     */
    fun setDateStyle(dateStyle: CometChatDateStyle) {
        style = style.copy(dateStyle = dateStyle)
        dateView?.setStyle(dateStyle)
    }

    /**
     * Sets the badge count style.
     */
    fun setBadgeCountStyle(badgeCountStyle: CometChatBadgeCountStyle) {
        style = style.copy(badgeCountStyle = badgeCountStyle)
        badgeView?.setStyle(badgeCountStyle)
    }

    /**
     * Sets the receipt style.
     */
    fun setReceiptStyle(receiptStyle: CometChatReceiptStyle) {
        style = style.copy(receiptStyle = receiptStyle)
        receiptView?.setStyle(receiptStyle)
    }

    /**
     * Sets the typing indicator style.
     */
    fun setTypingIndicatorStyle(typingIndicatorStyle: CometChatTypingIndicatorStyle) {
        style = style.copy(typingIndicatorStyle = typingIndicatorStyle)
        // Typing indicator text view styling is applied in bindSubtitle()
    }

    /**
     * Gets the current conversation.
     */
    fun getConversation(): Conversation? = conversation

    /**
     * Gets the current typing indicator info.
     */
    fun getTypingIndicator(): TypingIndicator? = typingIndicatorInfo

    /**
     * Gets the leading view container for custom view placement.
     */
    fun getLeadingViewContainer(): ViewGroup = binding.conversationLeadingView

    /**
     * Gets the title view container for custom view placement.
     */
    fun getTitleViewContainer(): ViewGroup = binding.conversationsTitleView

    /**
     * Gets the subtitle view container for custom view placement.
     */
    fun getSubtitleViewContainer(): ViewGroup = binding.subtitleView

    /**
     * Gets the trailing view container for custom view placement.
     */
    fun getTrailingViewContainer(): ViewGroup = binding.tailView

    /**
     * Gets the parent layout for full item replacement.
     */
    fun getParentLayout(): ViewGroup = binding.parentLayout

    /**
     * Resets the leading view to default.
     */
    fun resetLeadingView() {
        customLeadingView = null
        binding.conversationLeadingView.removeAllViews()
        // Re-add default avatar and status indicator
        binding.conversationLeadingView.addView(binding.conversationsAvatar)
        binding.conversationLeadingView.addView(binding.conversationsStatusAndTypeIndicator)
        bindLeading()
    }

    /**
     * Resets the title view to default.
     */
    fun resetTitleView() {
        customTitleView = null
        binding.conversationsTitleView.removeAllViews()
        binding.conversationsTitleView.addView(binding.tvConversationsTitle)
        bindTitle()
    }

    /**
     * Resets the subtitle view to default.
     */
    fun resetSubtitleView() {
        customSubtitleView = null
        initDefaultSubtitleViews()
        bindSubtitle()
    }

    /**
     * Resets the trailing view to default.
     */
    fun resetTrailingView() {
        customTrailingView = null
        initDefaultTrailingViews()
        bindTrailing()
    }

    /**
     * Restores the entire default layout when a custom item view is removed.
     * This method rebuilds all default views (leading, title, subtitle, trailing)
     * and re-binds the current conversation data.
     */
    fun restoreDefaultLayout() {
        // Clear all custom views
        customLeadingView = null
        customTitleView = null
        customSubtitleView = null
        customTrailingView = null

        // The parent layout contains the entire item structure
        // We need to restore the default views in each container
        
        // Restore leading view (avatar and status indicator)
        binding.conversationLeadingView.removeAllViews()
        binding.conversationLeadingView.addView(binding.conversationsAvatar)
        binding.conversationLeadingView.addView(binding.conversationsStatusAndTypeIndicator)
        
        // Restore title view
        binding.conversationsTitleView.removeAllViews()
        binding.conversationsTitleView.addView(binding.tvConversationsTitle)
        
        // Restore subtitle view with default layout
        initDefaultSubtitleViews()
        
        // Restore trailing view with default layout
        initDefaultTrailingViews()
        
        // Re-apply styles
        applyStyle()
        
        // Re-bind conversation data if available
        conversation?.let { bindConversation() }
    }

    // ==================== Private Binding Methods ====================

    private fun bindConversation() {
        conversation?.let { conv ->
            bindLeading()
            bindTitle()
            bindSubtitle()
            bindTrailing()
            updateSelectionState()
        }
    }

    private fun bindLeading() {
        if (customLeadingView != null) return

        conversation?.let { conv ->
            when (val conversationWith = conv.conversationWith) {
                is User -> {
                    binding.conversationsAvatar.setAvatar(conversationWith.name, conversationWith.avatar)
                    if (!hideUserStatus) {
                        binding.conversationsStatusAndTypeIndicator.visibility = VISIBLE
                        val statusIndicator = if (conversationWith.status == CometChatConstants.USER_STATUS_ONLINE) {
                            StatusIndicator.ONLINE
                        } else {
                            StatusIndicator.OFFLINE
                        }
                        binding.conversationsStatusAndTypeIndicator.setStatusIndicator(statusIndicator)
                    } else {
                        binding.conversationsStatusAndTypeIndicator.visibility = GONE
                    }
                }
                is Group -> {
                    binding.conversationsAvatar.setAvatar(conversationWith.name, conversationWith.icon)
                    binding.conversationsStatusAndTypeIndicator.visibility = GONE
                }
            }
        }
    }

    private fun bindTitle() {
        if (customTitleView != null) return

        conversation?.let { conv ->
            val name = when (val conversationWith = conv.conversationWith) {
                is User -> conversationWith.name
                is Group -> conversationWith.name
                else -> ""
            }
            binding.tvConversationsTitle.text = name
        }
    }

    private fun bindSubtitle() {
        if (customSubtitleView != null) return

        // Show typing indicator if present
        if (typingIndicatorInfo != null && typingIndicatorInfo!!.isTyping) {
            receiptView?.visibility = GONE
            senderPrefixTextView?.visibility = GONE
            messageTypeIconView?.visibility = GONE
            subtitleTextView?.visibility = GONE
            typingIndicatorTextView?.visibility = VISIBLE

            val typingText = buildTypingText(typingIndicatorInfo!!)
            typingIndicatorTextView?.text = typingText
            typingIndicatorTextView?.setTextColor(CometChatTheme.getPrimaryColor(context))
            return
        }

        // Hide typing indicator, show normal subtitle
        typingIndicatorTextView?.visibility = GONE
        subtitleTextView?.visibility = VISIBLE

        conversation?.let { conv ->
            val lastMessage = conv.lastMessage
            if (lastMessage != null) {
                // Show receipt for outgoing messages
                val isOutgoing = isOutgoingMessage(lastMessage)
                if (!hideReceipts && isOutgoing) {
                    receiptView?.visibility = VISIBLE
                    receiptView?.setReceipt(lastMessage)
                } else {
                    receiptView?.visibility = GONE
                }

                // Show sender prefix for group messages (e.g., "You: " or "John: ")
                val prefix = ConversationUtils.getMessagePrefix(context, lastMessage)
                if (prefix.isNotEmpty()) {
                    senderPrefixTextView?.visibility = VISIBLE
                    senderPrefixTextView?.text = prefix
                } else {
                    senderPrefixTextView?.visibility = GONE
                }

                // Show message type icon (e.g., photo, video, audio, document icons)
                val messageIcon = ConversationUtils.getLastMessageIcon(lastMessage)
                if (messageIcon != null) {
                    messageTypeIconView?.visibility = VISIBLE
                    messageTypeIconView?.setImageResource(messageIcon)
                } else {
                    messageTypeIconView?.visibility = GONE
                }

                // Format and display last message text (without prefix since it's shown separately)
                val formattedText = ConversationUtils.getFormattedLastMessageText(context, lastMessage, textFormatters)
                subtitleTextView?.text = formattedText
            } else {
                // No last message - show "Tap to start conversation" hint
                receiptView?.visibility = GONE
                senderPrefixTextView?.visibility = GONE
                messageTypeIconView?.visibility = GONE
                subtitleTextView?.text = context.getString(R.string.cometchat_start_conv_hint)
            }
        }
    }

    /**
     * Builds the typing indicator text based on the number of users typing.
     * - For 1-to-1 conversations: "typing..."
     * - For groups with 1 user: "John is typing..."
     * - For groups with 2+ users: "X people are typing..."
     */
    private fun buildTypingText(typingInfo: TypingIndicator): String {
        val typingUsers = typingInfo.typingUsers
        val isGroupConversation = conversation?.conversationType == UIKitConstants.ConversationType.GROUPS

        return when {
            typingUsers.isEmpty() -> ""
            !isGroupConversation -> {
                // 1-to-1 conversation: just show "typing..."
                context.getString(R.string.cometchat_typing)
            }
            typingUsers.size == 1 -> {
                // Group with 1 user typing: "John is typing..."
                "${typingUsers[0].name ?: ""} ${context.getString(R.string.cometchat_is_typing)}"
            }
            else -> {
                // Group with 2+ users typing: "X people are typing..."
                "${typingUsers.size} ${context.getString(R.string.cometchat_users_are_typing)}"
            }
        }
    }

    private fun bindTrailing() {
        if (customTrailingView != null) return

        conversation?.let { conv ->
            // Date - use conversation's updatedAt timestamp (not last message's sentAt)
            // This ensures date is always shown, even for new conversations without messages
            val timestamp = conv.updatedAt
            if (timestamp > 0) {
                dateView?.setDate(timestamp, DatePattern.DAY_DATE_TIME)
                dateView?.visibility = VISIBLE
            } else {
                dateView?.visibility = GONE
            }

            // Unread badge
            val unreadCount = conv.unreadMessageCount
            if (unreadCount > 0) {
                badgeView?.visibility = VISIBLE
                badgeView?.setCount(unreadCount)
            } else {
                badgeView?.visibility = GONE
            }
        }
    }

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

    private fun isOutgoingMessage(message: BaseMessage): Boolean {
        return message.sender?.uid == CometChatUIKit.getLoggedInUser()?.uid
    }
}
