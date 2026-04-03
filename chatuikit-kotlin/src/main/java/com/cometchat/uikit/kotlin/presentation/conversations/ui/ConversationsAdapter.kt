package com.cometchat.uikit.kotlin.presentation.conversations.ui

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.presentation.conversations.style.CometChatConversationListItemStyle
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationsDiffCallback
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationsViewHolderListener
import com.cometchat.uikit.kotlin.presentation.conversations.utils.TypingIndicator
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback
import java.text.SimpleDateFormat

/**
 * RecyclerView adapter for displaying conversation items.
 * 
 * This adapter uses CometChatConversationListItem as the row view and integrates
 * with ConversationsViewHolderListener for custom view callbacks following the
 * chatuikit Java pattern:
 * 
 * - createView() is called once during ViewHolder creation (onCreateViewHolder)
 * - bindView() is called during bind operations (onBindViewHolder) with conversation data
 * - Custom views replace default views when listeners are set
 * 
 * Implements DiffUtil for efficient list updates.
 * Supports custom item views and section views (leading, title, subtitle, trailing).
 */
class ConversationsAdapter(
) : RecyclerView.Adapter<ConversationsViewHolder>() {

    companion object {
        private val TAG = ConversationsAdapter::class.java.simpleName
    }

    // Current list of conversations
    private var conversations: List<Conversation> = emptyList()

    // Typing indicators map (conversationId -> TypingIndicator)
    // Uses TypingIndicator to support multiple users typing in groups
    private var typingIndicators: Map<String, TypingIndicator> = emptyMap()

    // Selection state
    private var selectedConversations: Map<Conversation, Boolean> = emptyMap()
    private var selectionEnabled: Boolean = false

    // Click listeners
    private var onItemClick: ((View, Int, Conversation) -> Unit)? = null
    private var onItemLongClick: ((View, Int, Conversation) -> Unit)? = null

    private var itemViewListener: ConversationsViewHolderListener? = null
    private var leadingViewListener: ConversationsViewHolderListener? = null
    private var titleViewListener: ConversationsViewHolderListener? = null
    private var subtitleViewListener: ConversationsViewHolderListener? = null
    private var trailingViewListener: ConversationsViewHolderListener? = null

    // Style
    private var itemStyle: CometChatConversationListItemStyle = CometChatConversationListItemStyle()

    // Text formatters
    private var textFormatters: List<CometChatTextFormatter> = emptyList()

    // Date formatter
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    // Visibility controls
    private var hideUserStatus: Boolean = false
    private var hideGroupType: Boolean = false
    private var hideReceipts: Boolean = false
    private var hideSeparator: Boolean = false

    // Checkbox styling properties
    @Dimension private var checkBoxStrokeWidth: Int = 0
    @Dimension private var checkBoxCornerRadius: Int = 0
    @ColorInt private var checkBoxStrokeColor: Int = 0
    @ColorInt private var checkBoxBackgroundColor: Int = 0
    @ColorInt private var checkBoxCheckedBackgroundColor: Int = 0
    private var checkBoxSelectIcon: Drawable? = null
    @ColorInt private var checkBoxSelectIconTint: Int = 0

    // Custom date format
    private var customDateFormat: SimpleDateFormat? = null

    /**
     * Updates the conversation list using DiffUtil for efficient updates.
     */
    fun setList(newList: List<Conversation>) {
        val diffCallback = ConversationsDiffCallback(conversations, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        conversations = newList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Gets the current list of conversations.
     */
    fun getList(): List<Conversation> = conversations

    /**
     * Updates typing indicators for conversations.
     * @deprecated Use setTypingIndicatorsFromViewModel() for proper matching logic
     */
    @Deprecated("Use setTypingIndicatorsFromViewModel() for proper matching logic")
    fun setTypingIndicators(indicators: Map<String, com.cometchat.chat.models.TypingIndicator>) {
        val oldIndicators = typingIndicators
        // Convert single SDK TypingIndicator to our TypingIndicator
        typingIndicators = indicators.mapValues { (_, indicator) ->
            TypingIndicator.fromSdkIndicator(indicator) ?: TypingIndicator(emptyList(), false)
        }.filterValues { it.isTyping }

        // Notify items that have typing indicator changes
        conversations.forEachIndexed { index, conversation ->
            val hasTyping = typingIndicators.containsKey(conversation.conversationId)
            val hadTyping = oldIndicators.containsKey(conversation.conversationId)
            if (hasTyping != hadTyping) {
                notifyItemChanged(index)
            }
        }
    }

    /**
     * Updates typing indicators from ViewModel's map format.
     * The ViewModel uses a composite key format: "${receiverType}_${receiverId}_${senderUid}"
     * This method correctly matches typing indicators to conversations by:
     * - For user conversations: matching the sender's UID with the conversation's user UID
     * - For group conversations: matching the receiverId with the group's GUID
     * 
     * For group conversations, this method collects ALL matching typing indicators
     * to support multiple users typing simultaneously.
     * 
     * This matches the Jetpack Compose implementation's getTypingIndicatorForConversation logic.
     */
    fun setTypingIndicatorsFromViewModel(viewModelTypingMap: Map<String, com.cometchat.chat.models.TypingIndicator>) {
        val oldIndicators = typingIndicators
        
        // Build a new map with conversationId as key for efficient lookup during bind
        val newIndicators = mutableMapOf<String, TypingIndicator>()
        
        conversations.forEach { conversation ->
            val conversationType = conversation.conversationType
            
            // Find ALL matching typing indicators for this conversation (not just the first one)
            // This is important for groups where multiple users can be typing simultaneously
            val matchingIndicators = viewModelTypingMap.values.filter { indicator ->
                when (conversationType) {
                    UIKitConstants.ConversationType.USERS -> {
                        // For user conversations, the typing indicator's sender is the one typing
                        // Match the sender's UID with the conversation's user UID
                        val conversationUser = conversation.conversationWith as? User
                        indicator.receiverType == conversationType &&
                            conversationUser?.uid == indicator.sender?.uid
                    }
                    UIKitConstants.ConversationType.GROUPS -> {
                        // For group conversations, match the receiverId with the group's GUID
                        val conversationGroup = conversation.conversationWith as? Group
                        indicator.receiverType == conversationType &&
                            indicator.receiverId == conversationGroup?.guid
                    }
                    else -> false
                }
            }
            
            // Create TypingIndicator from all matching indicators
            val typingInfo = TypingIndicator.fromSdkIndicators(matchingIndicators)
            if (typingInfo != null) {
                newIndicators[conversation.conversationId] = typingInfo
            }
        }
        
        typingIndicators = newIndicators
        
        // Notify items that have typing indicator changes
        conversations.forEachIndexed { index, conversation ->
            val hasTyping = newIndicators.containsKey(conversation.conversationId)
            val hadTyping = oldIndicators.containsKey(conversation.conversationId)
            if (hasTyping != hadTyping) {
                notifyItemChanged(index)
            }
        }
    }

    /**
     * Updates typing indicators using HashMap (for compatibility with ViewModel).
     * @deprecated Use setTypingIndicatorsFromViewModel() for proper matching logic
     */
    @Deprecated("Use setTypingIndicatorsFromViewModel() for proper matching logic")
    fun typing(typingMap: HashMap<Conversation, com.cometchat.chat.models.TypingIndicator>) {
        val newIndicators = mutableMapOf<String, TypingIndicator>()
        typingMap.forEach { (conversation, indicator) ->
            val typingInfo = TypingIndicator.fromSdkIndicator(indicator)
            if (typingInfo != null) {
                newIndicators[conversation.conversationId] = typingInfo
            }
        }
        
        val oldIndicators = typingIndicators
        typingIndicators = newIndicators

        // Notify items that have typing indicator changes
        conversations.forEachIndexed { index, conversation ->
            val hasTyping = newIndicators.containsKey(conversation.conversationId)
            val hadTyping = oldIndicators.containsKey(conversation.conversationId)
            if (hasTyping != hadTyping) {
                notifyItemChanged(index)
            }
        }
    }

    /**
     * Updates selection state for conversations.
     */
    fun selectConversation(selected: Map<Conversation, Boolean>) {
        selectedConversations = selected
        selectionEnabled = selected.isNotEmpty()

        // Notify all items to update selection state
        notifyDataSetChanged()
    }

    /**
     * Sets the item click listener.
     */
    fun setOnItemClick(listener: (View, Int, Conversation) -> Unit) {
        onItemClick = listener
    }

    /**
     * Sets the item long click listener.
     */
    fun setOnLongClick(listener: (View, Int, Conversation) -> Unit) {
        onItemLongClick = listener
    }

    /**
     * Sets custom item view listener for replacing entire item.
     */
    fun setItemView(listener: ConversationsViewHolderListener?) {
        itemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom leading view listener.
     */
    fun setLeadingView(listener: ConversationsViewHolderListener?) {
        leadingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom title view listener.
     */
    fun setTitleView(listener: ConversationsViewHolderListener?) {
        titleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom subtitle view listener.
     */
    fun setSubtitleView(listener: ConversationsViewHolderListener?) {
        subtitleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom trailing view listener.
     */
    fun setTrailingView(listener: ConversationsViewHolderListener?) {
        trailingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the item style.
     */
    fun setItemStyle(style: CometChatConversationListItemStyle) {
        itemStyle = style
        notifyDataSetChanged()
    }

    /**
     * Sets text formatters for message preview.
     */
    fun setTextFormatters(formatters: List<CometChatTextFormatter>) {
        textFormatters = formatters
        notifyDataSetChanged()
    }

    /**
     * Sets custom date/time formatter.
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        dateTimeFormatter = formatter
        notifyDataSetChanged()
    }

    /**
     * Gets the current date/time formatter.
     */
    fun getDateTimeFormatter(): DateTimeFormatterCallback? = dateTimeFormatter

    /**
     * Sets whether to hide user status indicator.
     */
    fun setHideUserStatus(hide: Boolean) {
        hideUserStatus = hide
        notifyDataSetChanged()
    }

    /**
     * Sets whether to hide group type indicator.
     */
    fun setHideGroupType(hide: Boolean) {
        hideGroupType = hide
        notifyDataSetChanged()
    }

    /**
     * Sets whether to hide message receipts.
     */
    fun setHideReceipts(hide: Boolean) {
        hideReceipts = hide
        notifyDataSetChanged()
    }

    /**
     * Sets whether to hide item separator.
     */
    fun setHideSeparator(hide: Boolean) {
        hideSeparator = hide
        notifyDataSetChanged()
    }

    // ==================== Checkbox Styling Methods ====================

    /**
     * Sets the stroke width for the checkbox.
     */
    fun setCheckBoxStrokeWidth(@Dimension width: Int) {
        checkBoxStrokeWidth = width
        notifyDataSetChanged()
    }

    /**
     * Sets the corner radius for the checkbox.
     */
    fun setCheckBoxCornerRadius(@Dimension radius: Int) {
        checkBoxCornerRadius = radius
        notifyDataSetChanged()
    }

    /**
     * Sets the stroke color for the checkbox.
     */
    fun setCheckBoxStrokeColor(@ColorInt color: Int) {
        checkBoxStrokeColor = color
        notifyDataSetChanged()
    }

    /**
     * Sets the background color for the checkbox.
     */
    fun setCheckBoxBackgroundColor(@ColorInt color: Int) {
        checkBoxBackgroundColor = color
        notifyDataSetChanged()
    }

    /**
     * Sets the checked background color for the checkbox.
     */
    fun setCheckBoxCheckedBackgroundColor(@ColorInt color: Int) {
        checkBoxCheckedBackgroundColor = color
        notifyDataSetChanged()
    }

    /**
     * Sets the select icon for the checkbox.
     */
    fun setCheckBoxSelectIcon(icon: Drawable?) {
        checkBoxSelectIcon = icon
        notifyDataSetChanged()
    }

    /**
     * Sets the select icon tint for the checkbox.
     */
    fun setCheckBoxSelectIconTint(@ColorInt tint: Int) {
        checkBoxSelectIconTint = tint
        notifyDataSetChanged()
    }

    // ==================== Date Format Method ====================

    /**
     * Sets a custom date format for conversation timestamps.
     */
    fun setDateFormat(dateFormat: SimpleDateFormat?) {
        customDateFormat = dateFormat
        notifyDataSetChanged()
    }

    /**
     * Enables or disables selection mode UI (checkboxes) on all items.
     * This is separate from the actual selection state - it controls whether
     * the selection UI is visible.
     *
     * @param enabled true to show selection checkboxes, false to hide them
     */
    fun setSelectionEnabled(enabled: Boolean) {
        selectionEnabled = enabled
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationsViewHolder {
        // Create ViewHolder with CometChatConversationListItem as the row view
        val holder = ConversationsViewHolder.create(parent)

        // Call createView() on all non-null listeners
        // This follows the chatuikit pattern where custom views are created once
        // during ViewHolder creation and cached for reuse
        holder.createCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )

        return holder
    }

    override fun onBindViewHolder(holder: ConversationsViewHolder, position: Int) {
        val conversation = conversations[position]
        val isSelected = selectedConversations[conversation] == true
        val typingIndicator = typingIndicators[conversation.conversationId]

        // Update custom views if listeners have changed
        // This ensures custom views are recreated when setSubtitleView etc. is called
        holder.createCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )

        // Bind data to the ViewHolder
        // This calls setConversation() on CometChatConversationListItem first,
        // then calls bindView() on all non-null listeners
        holder.bind(
            conversation = conversation,
            conversationList = conversations,
            position = position,
            typingIndicator = typingIndicator,
            isSelected = isSelected,
            selectionEnabled = selectionEnabled || selectedConversations.isNotEmpty(),
            hideUserStatus = hideUserStatus,
            hideGroupType = hideGroupType,
            hideReceipts = hideReceipts,
            hideSeparator = hideSeparator || position == conversations.lastIndex,
            textFormatters = textFormatters,
            dateTimeFormatter = dateTimeFormatter,
            style = itemStyle,
            itemViewListener = itemViewListener,
            leadingViewListener = leadingViewListener,
            titleViewListener = titleViewListener,
            subtitleViewListener = subtitleViewListener,
            trailingViewListener = trailingViewListener
        )

        // Setup click listeners on the CometChatConversationListItem
        holder.conversationListItem.setOnItemClick { conv ->
            onItemClick?.invoke(holder.itemView, position, conv)
        }

        holder.conversationListItem.setOnItemLongClick { conv ->
            onItemLongClick?.invoke(holder.itemView, position, conv)
        }
    }

    override fun getItemCount(): Int = conversations.size
}
