package com.cometchat.uikit.kotlin.presentation.search.ui.viewholders

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.R
import com.cometchat.uikit.kotlin.databinding.CometchatSearchConversationItemBinding
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationUtils
import com.cometchat.uikit.kotlin.presentation.shared.receipts.CometChatReceipt
import com.cometchat.uikit.kotlin.presentation.shared.receipts.MessageReceiptUtils
import com.cometchat.uikit.kotlin.presentation.search.style.CometChatSearchStyle
import com.cometchat.uikit.kotlin.presentation.search.utils.SearchConversationsViewHolderListener
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.formatters.FormatterUtils
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.utils.AgentChatDetector
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.badgecount.CometChatBadgeCount
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.CometChatDate
import com.cometchat.uikit.kotlin.presentation.shared.baseelements.date.DatePattern
import com.cometchat.uikit.kotlin.presentation.shared.statusindicator.StatusIndicator
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import com.cometchat.uikit.kotlin.shared.resources.utils.Utils

/**
 * ViewHolder for search conversation items in the search results list.
 *
 * This ViewHolder binds conversation data to the search conversation item layout,
 * displaying avatar, title, subtitle, timestamp, and unread badge.
 * 
 * Matches reference implementation (uikit-android-v5) by programmatically creating
 * subtitle and tail views instead of using inline XML children.
 *
 * @param binding The ViewBinding for the conversation item layout
 */
class SearchConversationViewHolder(
    val binding: CometchatSearchConversationItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        /**
         * Creates a new SearchConversationViewHolder.
         *
         * @param parent The parent ViewGroup
         * @return A new SearchConversationViewHolder instance
         */
        fun create(parent: ViewGroup): SearchConversationViewHolder {
            val binding = CometchatSearchConversationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SearchConversationViewHolder(binding)
        }
    }

    private val context: Context = binding.root.context

    // Programmatically created views for subtitle and tail (matching reference implementation)
    private var receiptView: CometChatReceipt? = null
    private var senderNameView: TextView? = null
    private var subtitleIconView: ImageView? = null
    private var subtitleTextView: TextView? = null
    private var timestampDateView: CometChatDate? = null
    private var badgeView: CometChatBadgeCount? = null

    // Custom views created by listeners
    private var customItemView: View? = null
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // Track listener references for change detection
    private var lastItemViewListener: SearchConversationsViewHolderListener? = null
    private var lastLeadingViewListener: SearchConversationsViewHolderListener? = null
    private var lastTitleViewListener: SearchConversationsViewHolderListener? = null
    private var lastSubtitleViewListener: SearchConversationsViewHolderListener? = null
    private var lastTrailingViewListener: SearchConversationsViewHolderListener? = null

    init {
        // Create default subtitle and tail views programmatically (matching reference)
        createDefaultSubtitleView()
        createDefaultTailView()
    }

    /**
     * Creates the default subtitle view programmatically.
     * Matches reference: ConversationsUtils.getSubtitleViewContainer(context)
     * Creates a horizontal container matching the Java SubtitleView layout:
     * [Receipt] [SenderName (bold)] [MessageTypeIcon] [LastMessageText]
     */
    private fun createDefaultSubtitleView() {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            minimumHeight = context.resources.getDimensionPixelSize(R.dimen.cometchat_20dp)
        }

        // Receipt icon (matching Java SubtitleView: messageReceipts view)
        receiptView = CometChatReceipt(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = context.resources.getDimensionPixelSize(R.dimen.cometchat_margin)
            }
            visibility = View.GONE
        }

        // Sender name (bold) — "You: " or "SenderName: "
        senderNameView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = context.resources.getDimensionPixelSize(R.dimen.cometchat_margin)
            }
            gravity = android.view.Gravity.CENTER_VERTICAL
            minHeight = context.resources.getDimensionPixelSize(R.dimen.cometchat_16dp)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            visibility = View.GONE
        }

        // Message type icon (16dp)
        subtitleIconView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                context.resources.getDimensionPixelSize(R.dimen.cometchat_16dp),
                context.resources.getDimensionPixelSize(R.dimen.cometchat_16dp)
            ).apply {
                marginEnd = context.resources.getDimensionPixelSize(R.dimen.cometchat_margin)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
        }

        // Last message text
        subtitleTextView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        container.addView(receiptView)
        container.addView(senderNameView)
        container.addView(subtitleIconView)
        container.addView(subtitleTextView)
        binding.subtitleView.addView(container)
    }

    /**
     * Creates the default tail view programmatically.
     * Matches reference: ConversationsUtils.getConversationTailViewContainer(context)
     * which inflates cometchat_tail.xml with gravity="end" and vertical orientation.
     */
    private fun createDefaultTailView() {
        // Create a container matching Java's cometchat_tail.xml layout
        val tailContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create timestamp CometChatDate
        timestampDateView = CometChatDate(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        tailContainer.addView(timestampDateView)

        // Create badge view (matching Java: height=20dp, minWidth=20dp, marginTop=4dp)
        badgeView = CometChatBadgeCount(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                context.resources.getDimensionPixelSize(R.dimen.cometchat_20dp)
            ).apply {
                topMargin = context.resources.getDimensionPixelSize(R.dimen.cometchat_margin_1)
            }
            minimumWidth = context.resources.getDimensionPixelSize(R.dimen.cometchat_20dp)
            visibility = View.GONE
        }
        tailContainer.addView(badgeView)

        binding.tailView.addView(tailContainer)
    }

    /**
     * Creates custom views using the provided listeners.
     * Custom views are recreated when listeners change.
     */
    fun createCustomViews(
        itemViewListener: SearchConversationsViewHolderListener?,
        leadingViewListener: SearchConversationsViewHolderListener?,
        titleViewListener: SearchConversationsViewHolderListener?,
        subtitleViewListener: SearchConversationsViewHolderListener?,
        trailingViewListener: SearchConversationsViewHolderListener?
    ) {
        // Handle item view (replaces entire item)
        if (itemViewListener !== lastItemViewListener) {
            lastItemViewListener = itemViewListener
            if (itemViewListener != null) {
                customItemView = itemViewListener.createView(context, binding)
                binding.parentLayout.removeAllViews()
                binding.parentLayout.addView(customItemView)
            } else {
                customItemView = null
            }
        }

        // Only handle section views if no full item replacement
        if (customItemView == null) {
            // Handle leading view
            if (leadingViewListener !== lastLeadingViewListener) {
                lastLeadingViewListener = leadingViewListener
                if (leadingViewListener != null) {
                    customLeadingView = leadingViewListener.createView(context, binding)
                    binding.conversationLeadingView.removeAllViews()
                    binding.conversationLeadingView.addView(customLeadingView)
                } else {
                    customLeadingView = null
                }
            }

            // Handle title view
            if (titleViewListener !== lastTitleViewListener) {
                lastTitleViewListener = titleViewListener
                if (titleViewListener != null) {
                    customTitleView = titleViewListener.createView(context, binding)
                    binding.conversationsTitleView.removeAllViews()
                    binding.conversationsTitleView.addView(customTitleView)
                } else {
                    customTitleView = null
                }
            }

            // Handle subtitle view
            if (subtitleViewListener !== lastSubtitleViewListener) {
                lastSubtitleViewListener = subtitleViewListener
                if (subtitleViewListener != null) {
                    customSubtitleView = subtitleViewListener.createView(context, binding)
                    binding.subtitleView.removeAllViews()
                    binding.subtitleView.addView(customSubtitleView)
                } else {
                    customSubtitleView = null
                }
            }

            // Handle trailing view
            if (trailingViewListener !== lastTrailingViewListener) {
                lastTrailingViewListener = trailingViewListener
                if (trailingViewListener != null) {
                    customTrailingView = trailingViewListener.createView(context, binding)
                    binding.tailView.removeAllViews()
                    binding.tailView.addView(customTrailingView)
                } else {
                    customTrailingView = null
                }
            }
        }
    }

    /**
     * Binds conversation data to the views.
     *
     * @param conversation The conversation to display
     * @param conversationList The full list of conversations
     * @param position The position in the list
     * @param style The style configuration
     * @param dateTimeFormatter Optional custom date/time formatter
     * @param hideUserStatus Whether to hide user online status
     * @param hideGroupType Whether to hide group type indicator
     * @param onClick Click callback for the item
     * @param itemViewListener Listener for custom item view
     * @param leadingViewListener Listener for custom leading view
     * @param titleViewListener Listener for custom title view
     * @param subtitleViewListener Listener for custom subtitle view
     * @param trailingViewListener Listener for custom trailing view
     */
    fun bind(
        conversation: Conversation,
        conversationList: List<Conversation>,
        position: Int,
        style: CometChatSearchStyle?,
        dateTimeFormatter: DateTimeFormatterCallback?,
        hideUserStatus: Boolean,
        hideGroupType: Boolean,
        onClick: ((Conversation) -> Unit)?,
        itemViewListener: SearchConversationsViewHolderListener?,
        leadingViewListener: SearchConversationsViewHolderListener?,
        titleViewListener: SearchConversationsViewHolderListener?,
        subtitleViewListener: SearchConversationsViewHolderListener?,
        trailingViewListener: SearchConversationsViewHolderListener?,
        textFormatters: List<CometChatTextFormatter> = emptyList()
    ) {
        // Handle custom item view (full replacement)
        if (itemViewListener != null && customItemView != null) {
            itemViewListener.bindView(
                context,
                customItemView!!,
                conversation,
                this,
                conversationList,
                position
            )
            return
        }

        // Apply style
        style?.let { applyStyle(it) }

        // Bind conversation data
        bindConversationData(conversation, dateTimeFormatter, hideUserStatus, hideGroupType, textFormatters)

        // Set click listener
        binding.root.setOnClickListener {
            onClick?.invoke(conversation)
        }

        // Call bindView on custom section listeners
        leadingViewListener?.let { listener ->
            customLeadingView?.let { view ->
                listener.bindView(context, view, conversation, this, conversationList, position)
            }
        }

        titleViewListener?.let { listener ->
            customTitleView?.let { view ->
                listener.bindView(context, view, conversation, this, conversationList, position)
            }
        }

        subtitleViewListener?.let { listener ->
            customSubtitleView?.let { view ->
                listener.bindView(context, view, conversation, this, conversationList, position)
            }
        }

        trailingViewListener?.let { listener ->
            customTrailingView?.let { view ->
                listener.bindView(context, view, conversation, this, conversationList, position)
            }
        }
    }

    /**
     * Binds conversation data to the default views.
     */
    private fun bindConversationData(
        conversation: Conversation,
        dateTimeFormatter: DateTimeFormatterCallback?,
        hideUserStatus: Boolean,
        hideGroupType: Boolean,
        textFormatters: List<CometChatTextFormatter> = emptyList()
    ) {
        val conversationWith = conversation.conversationWith

        when (conversationWith) {
            is User -> bindUserConversation(conversationWith, hideUserStatus)
            is Group -> bindGroupConversation(conversationWith, hideGroupType)
        }

        // Set subtitle matching Java SubtitleView layout: [Receipt] [SenderName:] [TypeIcon] [MessageText]
        val lastMessage = conversation.lastMessage

        // 0. Receipt icon — show for outgoing messages (matching Java bindSubtitleView)
        if (lastMessage != null && !MessageReceiptUtils.shouldHideReceipt(lastMessage)) {
            receiptView?.visibility = View.VISIBLE
            receiptView?.setReceipt(lastMessage)
        } else {
            receiptView?.visibility = View.GONE
        }

        // 1. Sender name (bold, separate from message text) — matching Java bindSubtitleView
        val senderPrefix = ConversationUtils.getMessagePrefix(context, lastMessage ?: return)
        if (senderPrefix.isNotEmpty()) {
            senderNameView?.text = senderPrefix
            senderNameView?.visibility = View.VISIBLE
        } else {
            senderNameView?.visibility = View.GONE
        }

        // 2. Message type icon — matching Java getLastMessageData + getIconDrawableRes
        val iconRes = ConversationUtils.getLastMessageIcon(lastMessage)
        // Check for thread messages — override icon to thread
        val effectiveIconRes = if (lastMessage != null && lastMessage.parentMessageId != 0L) {
            R.drawable.cometchat_ic_conversations_thread
        } else {
            iconRes
        }
        if (effectiveIconRes != null) {
            subtitleIconView?.setImageResource(effectiveIconRes)
            subtitleIconView?.visibility = View.VISIBLE
        } else {
            subtitleIconView?.visibility = View.GONE
        }

        // 3. Last message text — use FormatterUtils for mentions if formatters available
        val messageText = ConversationUtils.getLastMessageText(context, lastMessage)
        // For deleted/not-supported messages, use plain text (matching Java: no formatters for these)
        val isDeletedOrNotSupported = lastMessage != null && lastMessage.deletedAt > 0
        if (!isDeletedOrNotSupported && lastMessage is com.cometchat.chat.models.TextMessage && textFormatters.isNotEmpty() && !lastMessage.text.isNullOrEmpty()) {
            val formattedText = FormatterUtils.getFormattedText(
                context = context,
                baseMessage = lastMessage,
                formattingType = UIKitConstants.FormattingType.CONVERSATIONS,
                alignment = UIKitConstants.MessageBubbleAlignment.LEFT,
                text = messageText,
                formatters = textFormatters
            )
            subtitleTextView?.text = formattedText
        } else {
            subtitleTextView?.text = messageText
        }

        // Set timestamp using CometChatDate with DAY_DATE_TIME pattern (matching reference)
        val timestamp = conversation.updatedAt
        timestampDateView?.apply {
            dateTimeFormatter?.let { setDateTimeFormatterCallback(it) }
            setDate(timestamp, DatePattern.DAY_DATE_TIME)
        }

        // Set unread badge (hide for agent chats since last message is also hidden)
        val user = conversation.conversationWith as? User
        val isAgentChat = user != null && AgentChatDetector.isAgentChat(user)
        val unreadCount = conversation.unreadMessageCount
        if (unreadCount > 0 && !isAgentChat) {
            badgeView?.visibility = View.VISIBLE
            badgeView?.setCount(unreadCount)
        } else {
            badgeView?.visibility = View.GONE
        }
    }

    /**
     * Binds user conversation data.
     * Matches reference: checks Utils.isBlocked() before showing ONLINE status.
     */
    private fun bindUserConversation(user: User, hideUserStatus: Boolean) {
        // Set avatar
        binding.conversationsAvatar.setAvatar(user.name ?: "", user.avatar)

        // Set title
        binding.tvConversationsTitle.text = user.name

        // Set status indicator (matching reference: check blocked status before showing ONLINE)
        if (!hideUserStatus && user.status == CometChatConstants.USER_STATUS_ONLINE) {
            if (!Utils.isBlocked(user)) {
                binding.conversationsStatusAndTypeIndicator.visibility = View.VISIBLE
                binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.ONLINE)
            } else {
                binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.OFFLINE)
            }
        } else {
            binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.OFFLINE)
        }
    }

    /**
     * Binds group conversation data.
     * Matches reference: shows PUBLIC_GROUP indicator for public groups,
     * and wraps entire logic inside if (!hideGroupType) guard.
     */
    private fun bindGroupConversation(group: Group, hideGroupType: Boolean) {
        // Set avatar
        binding.conversationsAvatar.setAvatar(group.name ?: "", group.icon)

        // Set title
        binding.tvConversationsTitle.text = group.name

        // Set group type indicator (matching reference: if (!groupTypeVisibility) { ... })
        if (!hideGroupType) {
            binding.conversationsStatusAndTypeIndicator.visibility = View.VISIBLE
            when (group.groupType) {
                CometChatConstants.GROUP_TYPE_PRIVATE -> {
                    binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.PRIVATE_GROUP)
                }
                CometChatConstants.GROUP_TYPE_PASSWORD -> {
                    binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.PROTECTED_GROUP)
                }
                else -> {
                    // PUBLIC group — show indicator (matching reference: StatusIndicator.PUBLIC_GROUP)
                    binding.conversationsStatusAndTypeIndicator.setStatusIndicator(StatusIndicator.PUBLIC_GROUP)
                }
            }
        }
        // When hideGroupType is true, simply don't set any indicator (matching reference pattern)
    }

    /**
     * Applies style configuration to the views.
     * 
     * Style application follows the reference implementation pattern:
     * 1. Apply textAppearance first (ALWAYS, even if 0 - matching reference)
     * 2. Apply textColor second (only if non-zero - overrides any color from textAppearance)
     * 
     * When style properties are null, default theme values are applied.
     */
    private fun applyStyle(style: CometChatSearchStyle) {
        // Apply background color
        style.conversationItemBackgroundColor?.let {
            binding.parentLayout.setBackgroundColor(it)
        }

        // Apply title style - textAppearance FIRST, then textColor to ensure color takes precedence
        // ALWAYS apply textAppearance (matching reference: binding.tvConversationsTitle.setTextAppearance(conversationTitleTextAppearance))
        style.conversationTitleTextAppearance?.let {
            binding.tvConversationsTitle.setTextAppearance(it)
        }
        // Only apply textColor if provided and non-zero (matching reference: if (conversationTitleTextColor != 0))
        style.conversationTitleTextColor?.let {
            if (it != 0) binding.tvConversationsTitle.setTextColor(it)
        }

        // Apply subtitle style - textAppearance FIRST, then textColor to ensure color takes precedence
        // Sender name uses same text appearance and color as subtitle (matching Java bindSubtitleView)
        style.conversationSubtitleTextAppearance?.let {
            subtitleTextView?.setTextAppearance(it)
            senderNameView?.setTextAppearance(it)
            // Re-apply bold after text appearance (text appearance may reset it)
            senderNameView?.setTypeface(senderNameView?.typeface, android.graphics.Typeface.BOLD)
        }
        style.conversationSubtitleTextColor?.let {
            if (it != 0) {
                subtitleTextView?.setTextColor(it)
                senderNameView?.setTextColor(it)
            }
        }

        // Apply timestamp style - use CometChatDate style methods
        // CometChatDate internally handles the order: textAppearance first, then textColor
        style.conversationTimestampTextAppearance?.let {
            timestampDateView?.setDateTextAppearance(it)
        }
        style.conversationTimestampTextColor?.let {
            if (it != 0) timestampDateView?.setDateTextColor(it)
        }

        // Apply avatar style
        style.avatarStyle?.let {
            binding.conversationsAvatar.setStyle(it)
        }

        // Apply badge style
        style.badgeStyle?.let {
            badgeView?.setStyle(it)
        }

        // Apply status indicator style
        style.statusIndicatorStyle?.let {
            binding.conversationsStatusAndTypeIndicator.setStyle(it)
        }
    }
}
