package com.cometchat.uikit.kotlin.presentation.conversations.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.kotlin.databinding.CometchatConversationsListItemsBinding
import com.cometchat.uikit.kotlin.presentation.conversations.style.CometChatConversationListItemStyle
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationsViewHolderListener
import com.cometchat.uikit.kotlin.presentation.conversations.utils.TypingIndicator
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * ViewHolder for conversation list items.
 * Uses CometChatConversationListItem as the row view and integrates with
 * ConversationsViewHolderListener for custom view callbacks.
 * 
 * This implementation follows the chatuikit Java pattern where:
 * - createView() is called once during ViewHolder creation for each non-null listener
 * - bindView() is called during bind operations with conversation data
 * - Custom views replace default views when listeners are set
 */
class ConversationsViewHolder(
    val conversationListItem: CometChatConversationListItem
) : RecyclerView.ViewHolder(conversationListItem) {

    companion object {
        private val TAG = ConversationsViewHolder::class.java.simpleName

        /**
         * Creates a new ConversationsViewHolder with CometChatConversationListItem as the row view.
         */
        fun create(parent: ViewGroup): ConversationsViewHolder {
            val context = parent.context
            val conversationListItem = CometChatConversationListItem(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            return ConversationsViewHolder(conversationListItem)
        }
    }

    private val context: Context = conversationListItem.context

    // Custom views created by listeners (cached for reuse across bind operations)
    private var customItemView: View? = null
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // Track which listeners were used to create custom views
    private var lastItemViewListener: ConversationsViewHolderListener? = null
    private var lastLeadingViewListener: ConversationsViewHolderListener? = null
    private var lastTitleViewListener: ConversationsViewHolderListener? = null
    private var lastSubtitleViewListener: ConversationsViewHolderListener? = null
    private var lastTrailingViewListener: ConversationsViewHolderListener? = null

    // Track whether custom views were actually applied (separate from listener reference)
    // This ensures restoration triggers when listener is null but custom view was applied
    private var hasCustomItemView: Boolean = false
    private var hasCustomLeadingView: Boolean = false
    private var hasCustomTitleView: Boolean = false
    private var hasCustomSubtitleView: Boolean = false
    private var hasCustomTrailingView: Boolean = false

    /**
     * Creates or updates custom views using the provided listeners.
     * Custom views are recreated when listeners change.
     * 
     * The change detection logic checks both:
     * 1. Listener reference change (listener !== lastListener)
     * 2. Need to restore defaults (listener == null && hasCustomView)
     * 
     * This ensures proper restoration when custom views are removed.
     */
    fun createCustomViews(
        itemViewListener: ConversationsViewHolderListener?,
        leadingViewListener: ConversationsViewHolderListener?,
        titleViewListener: ConversationsViewHolderListener?,
        subtitleViewListener: ConversationsViewHolderListener?,
        trailingViewListener: ConversationsViewHolderListener?
    ) {
        // Check if any listener has changed OR if we need to restore defaults
        // Pattern: viewChanged = listener !== lastListener || (listener == null && hasCustomView)
        val itemViewChanged = itemViewListener !== lastItemViewListener || 
            (itemViewListener == null && hasCustomItemView)
        val leadingViewChanged = leadingViewListener !== lastLeadingViewListener ||
            (leadingViewListener == null && hasCustomLeadingView)
        val titleViewChanged = titleViewListener !== lastTitleViewListener ||
            (titleViewListener == null && hasCustomTitleView)
        val subtitleViewChanged = subtitleViewListener !== lastSubtitleViewListener ||
            (subtitleViewListener == null && hasCustomSubtitleView)
        val trailingViewChanged = trailingViewListener !== lastTrailingViewListener ||
            (trailingViewListener == null && hasCustomTrailingView)

        // Update tracked listeners
        lastItemViewListener = itemViewListener
        lastLeadingViewListener = leadingViewListener
        lastTitleViewListener = titleViewListener
        lastSubtitleViewListener = subtitleViewListener
        lastTrailingViewListener = trailingViewListener

        // Handle item view (replaces entire item)
        if (itemViewChanged) {
            if (itemViewListener != null) {
                customItemView = itemViewListener.createView(context, getItemBinding())
                conversationListItem.getParentLayout().removeAllViews()
                conversationListItem.getParentLayout().addView(customItemView)
                hasCustomItemView = true
            } else {
                // Listener removed, restore default layout
                customItemView = null
                hasCustomItemView = false
                conversationListItem.restoreDefaultLayout()
            }
        }

        // Only handle section views if no full item replacement
        if (customItemView == null) {
            // Handle leading view
            if (leadingViewChanged) {
                if (leadingViewListener != null) {
                    customLeadingView = leadingViewListener.createView(context, getItemBinding())
                    conversationListItem.setLeadingView(customLeadingView)
                    hasCustomLeadingView = true
                } else {
                    customLeadingView = null
                    hasCustomLeadingView = false
                    conversationListItem.setLeadingView(null)
                }
            }

            // Handle title view
            if (titleViewChanged) {
                if (titleViewListener != null) {
                    customTitleView = titleViewListener.createView(context, getItemBinding())
                    conversationListItem.setTitleView(customTitleView)
                    hasCustomTitleView = true
                } else {
                    customTitleView = null
                    hasCustomTitleView = false
                    conversationListItem.setTitleView(null)
                }
            }

            // Handle subtitle view
            if (subtitleViewChanged) {
                if (subtitleViewListener != null) {
                    customSubtitleView = subtitleViewListener.createView(context, getItemBinding())
                    conversationListItem.setSubtitleView(customSubtitleView)
                    hasCustomSubtitleView = true
                } else {
                    customSubtitleView = null
                    hasCustomSubtitleView = false
                    conversationListItem.setSubtitleView(null)
                }
            }

            // Handle trailing view
            if (trailingViewChanged) {
                if (trailingViewListener != null) {
                    customTrailingView = trailingViewListener.createView(context, getItemBinding())
                    conversationListItem.setTrailingView(customTrailingView)
                    hasCustomTrailingView = true
                } else {
                    customTrailingView = null
                    hasCustomTrailingView = false
                    conversationListItem.setTrailingView(null)
                }
            }
        }
    }

    /**
     * Gets the binding from the CometChatConversationListItem for the ConversationsViewHolderListener.
     * This provides access to the internal views of CometChatConversationListItem.
     */
    private fun getItemBinding(): CometchatConversationsListItemsBinding {
        return conversationListItem.binding
    }

    /**
     * Binds conversation data to the views.
     * 
     * This method:
     * 1. Sets the conversation on CometChatConversationListItem (renders default data)
     * 2. Calls bindView() on all non-null listeners (allows custom views to update)
     */
    fun bind(
        conversation: Conversation,
        conversationList: List<Conversation>,
        position: Int,
        typingIndicator: TypingIndicator?,
        isSelected: Boolean,
        selectionEnabled: Boolean,
        hideUserStatus: Boolean,
        hideGroupType: Boolean,
        hideReceipts: Boolean,
        hideSeparator: Boolean,
        textFormatters: List<CometChatTextFormatter>,
        dateTimeFormatter: DateTimeFormatterCallback?,
        style: CometChatConversationListItemStyle,
        itemViewListener: ConversationsViewHolderListener?,
        leadingViewListener: ConversationsViewHolderListener?,
        titleViewListener: ConversationsViewHolderListener?,
        subtitleViewListener: ConversationsViewHolderListener?,
        trailingViewListener: ConversationsViewHolderListener?
    ) {
        // Apply style to the item
        conversationListItem.setStyle(style)

        // Apply visibility controls
        conversationListItem.setHideUserStatus(hideUserStatus)
        conversationListItem.setHideGroupType(hideGroupType)
        conversationListItem.setHideReceipts(hideReceipts)
        conversationListItem.setHideSeparator(hideSeparator)

        // Apply text formatters
        conversationListItem.setTextFormatters(textFormatters)

        // Apply date formatter
        conversationListItem.setDateTimeFormatter(dateTimeFormatter)

        // Set selection state
        conversationListItem.setSelectionMode(
            if (selectionEnabled) UIKitConstants.SelectionMode.MULTIPLE
            else UIKitConstants.SelectionMode.NONE
        )
        conversationListItem.setItemSelected(isSelected)

        // Handle custom item view (full replacement)
        if (itemViewListener != null && customItemView != null) {
            // For full item replacement, only call bindView on the listener
            itemViewListener.bindView(
                context,
                customItemView!!,
                conversation,
                typingIndicator,
                this,
                conversationList,
                position
            )
            return
        }

        // Set conversation data on the item (renders default views)
        conversationListItem.setConversation(conversation)
        conversationListItem.setTypingIndicator(typingIndicator)

        // Call bindView on custom section listeners (allows custom views to update with data)
        leadingViewListener?.let { listener ->
            customLeadingView?.let { view ->
                listener.bindView(context, view, conversation, typingIndicator, this, conversationList, position)
            }
        }

        titleViewListener?.let { listener ->
            customTitleView?.let { view ->
                listener.bindView(context, view, conversation, typingIndicator, this, conversationList, position)
            }
        }

        subtitleViewListener?.let { listener ->
            customSubtitleView?.let { view ->
                listener.bindView(context, view, conversation, typingIndicator, this, conversationList, position)
            }
        }

        trailingViewListener?.let { listener ->
            customTrailingView?.let { view ->
                listener.bindView(context, view, conversation, typingIndicator, this, conversationList, position)
            }
        }
    }

    /**
     * Applies style properties to the views.
     * @deprecated Use bind() method which applies style automatically
     */
    @Deprecated("Use bind() method which applies style automatically")
    fun applyStyles(style: CometChatConversationListItemStyle) {
        conversationListItem.setStyle(style)
    }
}
