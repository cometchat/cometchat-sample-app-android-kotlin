package com.cometchat.uikit.kotlin.presentation.calllogs.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.calls.model.CallLog
import com.cometchat.uikit.kotlin.presentation.calllogs.style.CometChatCallLogsListItemStyle
import com.cometchat.uikit.kotlin.presentation.calllogs.utils.CallLogsDiffCallback
import com.cometchat.uikit.kotlin.presentation.calllogs.utils.CallLogsViewHolderListener
import com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback

/**
 * RecyclerView adapter for displaying call log items.
 * 
 * This adapter uses CometChatCallLogsListItem as the row view and integrates
 * with CallLogsViewHolderListener for custom view callbacks following the
 * chatuikit Java pattern:
 * 
 * - createView() is called once during ViewHolder creation (onCreateViewHolder)
 * - bindView() is called during bind operations (onBindViewHolder) with call log data
 * - Custom views replace default views when listeners are set
 * 
 * Implements DiffUtil for efficient list updates.
 * Supports custom item views and section views (leading, title, subtitle, trailing).
 */
class CallLogsAdapter : RecyclerView.Adapter<CallLogsViewHolder>() {

    companion object {
        private val TAG = CallLogsAdapter::class.java.simpleName
    }

    // Current list of call logs
    private var callLogs: List<CallLog> = emptyList()

    // Click listeners
    private var onItemClick: ((android.view.View, Int, CallLog) -> Unit)? = null
    private var onItemLongClick: ((android.view.View, Int, CallLog) -> Unit)? = null
    private var onCallTypeIconClick: ((android.view.View, Int, CallLog) -> Unit)? = null

    // Custom view listeners
    private var itemViewListener: CallLogsViewHolderListener? = null
    private var leadingViewListener: CallLogsViewHolderListener? = null
    private var titleViewListener: CallLogsViewHolderListener? = null
    private var subtitleViewListener: CallLogsViewHolderListener? = null
    private var trailingViewListener: CallLogsViewHolderListener? = null

    // Style - initialized lazily with context from first ViewHolder creation
    private var itemStyle: CometChatCallLogsListItemStyle? = null

    // Visibility controls
    private var hideSeparator: Boolean = false

    // Date/time formatter
    private var dateTimeFormatter: DateTimeFormatterCallback? = null

    /**
     * Updates the call logs list using DiffUtil for efficient updates.
     */
    fun setList(newList: List<CallLog>) {
        val diffCallback = CallLogsDiffCallback(callLogs, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        callLogs = newList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Gets the current list of call logs.
     */
    fun getList(): List<CallLog> = callLogs

    /**
     * Sets the item click listener.
     */
    fun setOnItemClick(listener: (android.view.View, Int, CallLog) -> Unit) {
        onItemClick = listener
    }

    /**
     * Sets the item long click listener.
     */
    fun setOnItemLongClick(listener: (android.view.View, Int, CallLog) -> Unit) {
        onItemLongClick = listener
    }

    /**
     * Sets the call type icon click listener (for initiating calls).
     */
    fun setOnCallTypeIconClick(listener: (android.view.View, Int, CallLog) -> Unit) {
        onCallTypeIconClick = listener
    }

    /**
     * Sets custom item view listener for replacing entire item.
     */
    fun setItemView(listener: CallLogsViewHolderListener?) {
        android.util.Log.d(TAG, "setItemView: listener=${if (listener != null) "SET" else "NULL"}")
        itemViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom leading view listener.
     */
    fun setLeadingView(listener: CallLogsViewHolderListener?) {
        android.util.Log.d(TAG, "setLeadingView: listener=${if (listener != null) "SET" else "NULL"}")
        leadingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom title view listener.
     */
    fun setTitleView(listener: CallLogsViewHolderListener?) {
        android.util.Log.d(TAG, "setTitleView: listener=${if (listener != null) "SET" else "NULL"}")
        titleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom subtitle view listener.
     */
    fun setSubtitleView(listener: CallLogsViewHolderListener?) {
        android.util.Log.d(TAG, "setSubtitleView: listener=${if (listener != null) "SET" else "NULL"}")
        subtitleViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets custom trailing view listener.
     */
    fun setTrailingView(listener: CallLogsViewHolderListener?) {
        android.util.Log.d(TAG, "setTrailingView: listener=${if (listener != null) "SET" else "NULL"}")
        trailingViewListener = listener
        notifyDataSetChanged()
    }

    /**
     * Sets the item style.
     */
    fun setItemStyle(style: CometChatCallLogsListItemStyle) {
        android.util.Log.d(TAG, "setItemStyle: incomingCallIcon=${style.incomingCallIcon}, outgoingCallIcon=${style.outgoingCallIcon}, missedCallIcon=${style.missedCallIcon}")
        android.util.Log.d(TAG, "setItemStyle: audioCallIcon=${style.audioCallIcon}, videoCallIcon=${style.videoCallIcon}")
        itemStyle = style
        notifyDataSetChanged()
    }

    /**
     * Sets whether to hide item separator.
     */
    fun setHideSeparator(hide: Boolean) {
        hideSeparator = hide
        notifyDataSetChanged()
    }

    /**
     * Sets custom date/time formatter callback for formatting dates in the list items.
     */
    fun setDateTimeFormatter(formatter: DateTimeFormatterCallback?) {
        dateTimeFormatter = formatter
        notifyDataSetChanged()
    }

    /**
     * Gets the current date/time formatter callback.
     */
    fun getDateTimeFormatter(): DateTimeFormatterCallback? = dateTimeFormatter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogsViewHolder {
        val holder = CallLogsViewHolder.create(parent)

        // Initialize style with context if not already set
        if (itemStyle == null) {
            itemStyle = CometChatCallLogsListItemStyle.default(parent.context)
        }

        // Call createView() on all non-null listeners
        holder.createCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )

        return holder
    }

    override fun onBindViewHolder(holder: CallLogsViewHolder, position: Int) {
        val callLog = callLogs[position]

        // Update custom views if listeners have changed
        holder.createCustomViews(
            itemViewListener,
            leadingViewListener,
            titleViewListener,
            subtitleViewListener,
            trailingViewListener
        )

        // Bind data to the ViewHolder
        holder.bind(
            callLog = callLog,
            callLogsList = callLogs,
            position = position,
            hideSeparator = hideSeparator || position == callLogs.lastIndex,
            style = itemStyle ?: CometChatCallLogsListItemStyle.default(holder.itemView.context),
            dateTimeFormatter = dateTimeFormatter,
            itemViewListener = itemViewListener,
            leadingViewListener = leadingViewListener,
            titleViewListener = titleViewListener,
            subtitleViewListener = subtitleViewListener,
            trailingViewListener = trailingViewListener
        )

        // Setup click listeners on the CometChatCallLogsListItem
        holder.callLogsListItem.setOnItemClick { log ->
            onItemClick?.invoke(holder.itemView, position, log)
        }

        holder.callLogsListItem.setOnItemLongClick { log ->
            onItemLongClick?.invoke(holder.itemView, position, log)
        }

        holder.callLogsListItem.setOnCallTypeIconClick { log ->
            onCallTypeIconClick?.invoke(holder.itemView, position, log)
        }
    }

    override fun getItemCount(): Int = callLogs.size
}
