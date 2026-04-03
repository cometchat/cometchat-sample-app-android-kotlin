package com.cometchat.uikit.kotlin.presentation.calllogs.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.kotlin.presentation.calllogs.style.CometChatCallLogsListItemStyle
import com.cometchat.uikit.kotlin.presentation.calllogs.utils.CallLogsViewHolderListener
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * ViewHolder for call logs list items.
 * Uses CometChatCallLogsListItem as the row view and integrates with
 * CallLogsViewHolderListener for custom view callbacks.
 * 
 * This implementation follows the chatuikit Java pattern where:
 * - createView() is called once during ViewHolder creation for each non-null listener
 * - bindView() is called during bind operations with call log data
 * - Custom views replace default views when listeners are set
 */
class CallLogsViewHolder(
    val callLogsListItem: CometChatCallLogsListItem
) : RecyclerView.ViewHolder(callLogsListItem) {

    companion object {
        private val TAG = CallLogsViewHolder::class.java.simpleName
        
        /**
         * Creates a new CallLogsViewHolder with CometChatCallLogsListItem as the row view.
         */
        fun create(parent: ViewGroup): CallLogsViewHolder {
            val context = parent.context
            val callLogsListItem = CometChatCallLogsListItem(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            return CallLogsViewHolder(callLogsListItem)
        }
    }

    private val context: Context = callLogsListItem.context

    // Custom views created by listeners (cached for reuse across bind operations)
    private var customItemView: View? = null
    private var customLeadingView: View? = null
    private var customTitleView: View? = null
    private var customSubtitleView: View? = null
    private var customTrailingView: View? = null

    // Track which listeners were used to create custom views
    private var lastItemViewListener: CallLogsViewHolderListener? = null
    private var lastLeadingViewListener: CallLogsViewHolderListener? = null
    private var lastTitleViewListener: CallLogsViewHolderListener? = null
    private var lastSubtitleViewListener: CallLogsViewHolderListener? = null
    private var lastTrailingViewListener: CallLogsViewHolderListener? = null

    // Track whether custom views were actually applied
    private var hasCustomItemView: Boolean = false
    private var hasCustomLeadingView: Boolean = false
    private var hasCustomTitleView: Boolean = false
    private var hasCustomSubtitleView: Boolean = false
    private var hasCustomTrailingView: Boolean = false

    /**
     * Creates or updates custom views using the provided listeners.
     * Custom views are recreated when listeners change.
     */
    fun createCustomViews(
        itemViewListener: CallLogsViewHolderListener?,
        leadingViewListener: CallLogsViewHolderListener?,
        titleViewListener: CallLogsViewHolderListener?,
        subtitleViewListener: CallLogsViewHolderListener?,
        trailingViewListener: CallLogsViewHolderListener?
    ) {
        android.util.Log.d(TAG, "createCustomViews: itemView=${itemViewListener != null}, leading=${leadingViewListener != null}, title=${titleViewListener != null}, subtitle=${subtitleViewListener != null}, trailing=${trailingViewListener != null}")
        
        // Check if any listener has changed OR if we need to restore defaults
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
                customItemView = itemViewListener.createView(context, null)
                callLogsListItem.getParentLayout().removeAllViews()
                customItemView?.let { callLogsListItem.getParentLayout().addView(it) }
                hasCustomItemView = true
            } else {
                customItemView = null
                hasCustomItemView = false
                // Restore default layout by recreating the item
                callLogsListItem.resetLeadingView()
                callLogsListItem.resetTitleView()
                callLogsListItem.resetSubtitleView()
                callLogsListItem.resetTrailingView()
            }
        }

        // Only handle section views if no full item replacement
        if (customItemView == null) {
            if (leadingViewChanged) {
                if (leadingViewListener != null) {
                    customLeadingView = leadingViewListener.createView(context, null)
                    callLogsListItem.setLeadingView(customLeadingView)
                    hasCustomLeadingView = true
                } else {
                    customLeadingView = null
                    hasCustomLeadingView = false
                    callLogsListItem.setLeadingView(null)
                }
            }

            if (titleViewChanged) {
                if (titleViewListener != null) {
                    customTitleView = titleViewListener.createView(context, null)
                    callLogsListItem.setTitleView(customTitleView)
                    hasCustomTitleView = true
                } else {
                    customTitleView = null
                    hasCustomTitleView = false
                    callLogsListItem.setTitleView(null)
                }
            }

            if (subtitleViewChanged) {
                if (subtitleViewListener != null) {
                    customSubtitleView = subtitleViewListener.createView(context, null)
                    callLogsListItem.setSubtitleView(customSubtitleView)
                    hasCustomSubtitleView = true
                } else {
                    customSubtitleView = null
                    hasCustomSubtitleView = false
                    callLogsListItem.setSubtitleView(null)
                }
            }

            if (trailingViewChanged) {
                if (trailingViewListener != null) {
                    customTrailingView = trailingViewListener.createView(context, null)
                    callLogsListItem.setTrailingView(customTrailingView)
                    hasCustomTrailingView = true
                } else {
                    customTrailingView = null
                    hasCustomTrailingView = false
                    callLogsListItem.setTrailingView(null)
                }
            }
        }
    }

    /**
     * Binds call log data to the views.
     */
    fun bind(
        callLog: CallLog,
        callLogsList: List<CallLog>,
        position: Int,
        hideSeparator: Boolean,
        style: CometChatCallLogsListItemStyle,
        dateTimeFormatter: DateTimeFormatterCallback?,
        itemViewListener: CallLogsViewHolderListener?,
        leadingViewListener: CallLogsViewHolderListener?,
        titleViewListener: CallLogsViewHolderListener?,
        subtitleViewListener: CallLogsViewHolderListener?,
        trailingViewListener: CallLogsViewHolderListener?
    ) {
        // Apply style to the item
        callLogsListItem.setStyle(style)

        // Apply date/time formatter
        callLogsListItem.setDateTimeFormatter(dateTimeFormatter)

        // Apply visibility controls
        callLogsListItem.setHideSeparator(hideSeparator)

        // Handle custom item view (full replacement)
        if (itemViewListener != null && customItemView != null) {
            itemViewListener.bindView(context, customItemView!!, callLog)
            return
        }

        // Set call log data on the item (renders default views)
        callLogsListItem.setCallLog(callLog)

        // Call bindView on custom section listeners
        leadingViewListener?.let { listener ->
            customLeadingView?.let { view ->
                listener.bindView(context, view, callLog)
            }
        }

        titleViewListener?.let { listener ->
            customTitleView?.let { view ->
                listener.bindView(context, view, callLog)
            }
        }

        subtitleViewListener?.let { listener ->
            customSubtitleView?.let { view ->
                listener.bindView(context, view, callLog)
            }
        }

        trailingViewListener?.let { listener ->
            customTrailingView?.let { view ->
                listener.bindView(context, view, callLog)
            }
        }
    }
}
