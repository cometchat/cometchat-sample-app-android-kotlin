package com.cometchat.uikit.kotlin.presentation.notificationfeed.ui

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cometchat.chat.models.NotificationFeedItem
import com.cometchat.uikit.core.state.TimestampGroup
import com.cometchat.uikit.kotlin.presentation.notificationfeed.style.CometChatNotificationFeedStyle
import com.cometchat.uikit.kotlin.theme.CometChatTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * RecyclerView adapter for the notification feed list.
 * Handles both timestamp group headers and feed item cards.
 * Uses DiffUtil for efficient updates.
 */
class NotificationFeedAdapter(
    private var style: CometChatNotificationFeedStyle,
    private val onItemClick: (NotificationFeedItem) -> Unit,
    private val onActionClick: (NotificationFeedItem, Map<String, Any>) -> Unit,
    private val onItemVisible: (NotificationFeedItem) -> Unit,
    private val onItemHidden: (NotificationFeedItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_LOADING = 2
        private const val VIEW_TYPE_ERROR = 3
    }

    private val displayItems = mutableListOf<DisplayItem>()
    private var showLoading = false
    private var showError = false
    private var onRetryClick: (() -> Unit)? = null
    private var activeCategory: String? = null

    sealed class DisplayItem {
        data class Header(val label: String) : DisplayItem()
        data class FeedItem(val item: NotificationFeedItem) : DisplayItem()
        data object Loading : DisplayItem()
        data object Error : DisplayItem()
    }

    fun submitGroupedItems(groups: List<TimestampGroup>) {
        val newItems = mutableListOf<DisplayItem>()
        // Flat list — no headers (matching Compose which doesn't show "Today"/"Yesterday" headers)
        groups.forEach { group ->
            group.items.forEach { item ->
                newItems.add(DisplayItem.FeedItem(item))
            }
        }
        if (showLoading) newItems.add(DisplayItem.Loading)
        if (showError) newItems.add(DisplayItem.Error)

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = displayItems.size
            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                val old = displayItems[oldPos]
                val new = newItems[newPos]
                return when {
                    old is DisplayItem.Header && new is DisplayItem.Header -> old.label == new.label
                    old is DisplayItem.FeedItem && new is DisplayItem.FeedItem -> old.item.id == new.item.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                val old = displayItems[oldPos]
                val new = newItems[newPos]
                return when {
                    old is DisplayItem.Header && new is DisplayItem.Header -> old == new
                    old is DisplayItem.FeedItem && new is DisplayItem.FeedItem ->
                        old.item.id == new.item.id &&
                        old.item.readAt == new.item.readAt
                    else -> false
                }
            }
        })

        displayItems.clear()
        displayItems.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateStyle(newStyle: CometChatNotificationFeedStyle) {
        this.style = newStyle
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is DisplayItem.Header -> VIEW_TYPE_HEADER
            is DisplayItem.FeedItem -> VIEW_TYPE_ITEM
            is DisplayItem.Loading -> VIEW_TYPE_LOADING
            is DisplayItem.Error -> VIEW_TYPE_ERROR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(createHeaderView(parent))
            VIEW_TYPE_ITEM -> FeedItemViewHolder(createItemView(parent))
            VIEW_TYPE_LOADING -> LoadingViewHolder(createLoadingView(parent))
            VIEW_TYPE_ERROR -> ErrorViewHolder(createErrorView(parent))
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is DisplayItem.Header -> (holder as HeaderViewHolder).bind(item.label)
            is DisplayItem.FeedItem -> (holder as FeedItemViewHolder).bind(item.item)
            is DisplayItem.Loading -> { /* nothing to bind */ }
            is DisplayItem.Error -> (holder as ErrorViewHolder).bind()
        }
    }

    override fun getItemCount(): Int = displayItems.size

    // region ViewHolders

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView as TextView

        fun bind(label: String) {
            titleText.text = label
            if (style.timestampTextColor != 0) {
                titleText.setTextColor(style.timestampTextColor)
            }
        }
    }

    inner class FeedItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView as LinearLayout
        private val categoryTimestampRow: LinearLayout = container.getChildAt(0) as LinearLayout
        private val categoryText: TextView = categoryTimestampRow.getChildAt(0) as TextView
        private val timeText: TextView = categoryTimestampRow.getChildAt(1) as TextView
        private val cardContainer: android.widget.FrameLayout = container.getChildAt(1) as android.widget.FrameLayout

        fun bind(item: NotificationFeedItem) {
            // Category — hide when viewing a specific category
            if (activeCategory != null || item.category.isNullOrEmpty() || item.category == "null") {
                categoryText.text = ""  // Keep visible with weight to push timestamp right
            } else {
                categoryText.text = item.category
            }

            // Relative time
            timeText.text = getRelativeTime(item.sentAt)

            // Card content — render using CometChatCardView
            cardContainer.removeAllViews()
            val cardJson = item.content?.toString() ?: ""
            if (cardJson.isNotEmpty()) {
                val cardView = com.cometchat.cards.CometChatCardView(cardContainer.context).apply {
                    layoutParams = android.widget.FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setThemeMode(com.cometchat.cards.models.CometChatCardThemeMode.AUTO)
                    setActionCallback(com.cometchat.cards.actions.CometChatCardActionCallback { event ->
                        onActionClick(
                            item,
                            mapOf(
                                "type" to (event.action.javaClass.simpleName ?: "unknown"),
                                "elementId" to event.elementId
                            )
                        )
                    })
                    setCardSchema(cardJson)
                }
                cardContainer.addView(cardView)
            } else {
                val fallbackText = TextView(cardContainer.context).apply {
                    text = "Unable to display notification"
                    textSize = 14f
                    setTextColor(CometChatTheme.getTextColorSecondary(cardContainer.context))
                    setPadding(
                        (16 * cardContainer.resources.displayMetrics.density).toInt(),
                        (24 * cardContainer.resources.displayMetrics.density).toInt(),
                        (16 * cardContainer.resources.displayMetrics.density).toInt(),
                        (24 * cardContainer.resources.displayMetrics.density).toInt()
                    )
                }
                cardContainer.addView(fallbackText)
            }

            // Click handler
            container.setOnClickListener {
                onItemClick(item)
            }

            // Visibility tracking
            container.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    onItemVisible(item)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    onItemHidden(item)
                }
            })
        }
    }

    // endregion

    // region View Creation

    private fun createHeaderView(parent: ViewGroup): View {
        return TextView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(48, 24, 48, 8)
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(style.timestampTextColor.takeIf { it != 0 } ?: android.graphics.Color.GRAY)
        }
    }

    private fun createItemView(parent: ViewGroup): View {
        val ctx = parent.context
        val density = ctx.resources.displayMetrics.density

        // Outer container — vertical, full width, padding
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding((16 * density).toInt(), (8 * density).toInt(), (16 * density).toInt(), (8 * density).toInt())
        }

        // Row: Category (left) + Timestamp (right)
        val categoryTimestampRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, (4 * density).toInt())
        }

        // Category text
        val categoryText = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            textSize = 12f
            setTextColor(CometChatTheme.getTextColorSecondary(ctx))
        }
        categoryTimestampRow.addView(categoryText)

        // Time text
        val timeText = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 12f
            setTextColor(CometChatTheme.getTextColorSecondary(ctx))
        }
        categoryTimestampRow.addView(timeText)

        container.addView(categoryTimestampRow)

        // Card content container — no styling, card renderer handles everything
        val cardContainer = android.widget.FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(cardContainer)

        return container
    }

    // endregion

    fun setActiveCategory(category: String?) {
        this.activeCategory = category
        notifyDataSetChanged()
    }

    // region Public Methods for Loading/Error States

    fun showPaginationLoading() {
        showLoading = true
        showError = false
        if (displayItems.lastOrNull() !is DisplayItem.Loading) {
            displayItems.add(DisplayItem.Loading)
            notifyItemInserted(displayItems.size - 1)
        }
    }

    fun showPaginationError(retry: () -> Unit) {
        showLoading = false
        showError = true
        onRetryClick = retry
        // Remove loading if present
        val loadingIndex = displayItems.indexOfLast { it is DisplayItem.Loading }
        if (loadingIndex >= 0) {
            displayItems.removeAt(loadingIndex)
            notifyItemRemoved(loadingIndex)
        }
        if (displayItems.lastOrNull() !is DisplayItem.Error) {
            displayItems.add(DisplayItem.Error)
            notifyItemInserted(displayItems.size - 1)
        }
    }

    fun hidePaginationFooter() {
        showLoading = false
        showError = false
        val removeIndex = displayItems.indexOfLast { it is DisplayItem.Loading || it is DisplayItem.Error }
        if (removeIndex >= 0) {
            displayItems.removeAt(removeIndex)
            notifyItemRemoved(removeIndex)
        }
    }

    // endregion

    // region Loading/Error ViewHolders

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class ErrorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener { onRetryClick?.invoke() }
        }
    }

    // endregion

    // region Loading/Error View Creation

    private fun createLoadingView(parent: ViewGroup): View {
        val ctx = parent.context
        val density = ctx.resources.displayMetrics.density
        val sizePx = (40 * density).toInt()

        val container = android.widget.LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, (24 * density).toInt(), 0, (24 * density).toInt())
        }

        // Material CircularProgressIndicator with trackColor — matches Compose exactly
        val progressIndicator = com.google.android.material.progressindicator.CircularProgressIndicator(ctx).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(sizePx, sizePx)
            isIndeterminate = true
            trackThickness = (4 * density).toInt()
            setIndicatorColor(CometChatTheme.getPrimaryColor(ctx))
            trackColor = CometChatTheme.getStrokeColorDefault(ctx)
            indicatorSize = sizePx
        }
        container.addView(progressIndicator)

        val textView = android.widget.TextView(ctx).apply {
            text = "Loading..."
            textSize = 14f
            setTextColor(CometChatTheme.getTextColorSecondary(ctx))
            gravity = Gravity.CENTER
            setPadding(0, (12 * density).toInt(), 0, 0)
        }
        container.addView(textView)

        return container
    }

    private fun createErrorView(parent: ViewGroup): View {
        val container = android.widget.LinearLayout(parent.context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 48, 0, 48)
        }

        val retryIcon = android.widget.ImageView(parent.context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(64, 64)
            setImageResource(com.cometchat.uikit.kotlin.R.drawable.cometchat_ic_retry)
            setColorFilter(
                android.graphics.Color.parseColor("#F44336"),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
        container.addView(retryIcon)

        val errorText = android.widget.TextView(parent.context).apply {
            text = "Couldn't load more"
            textSize = 14f
            setTextColor(style.timestampTextColor.takeIf { it != 0 } ?: android.graphics.Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }
        container.addView(errorText)

        val retryText = android.widget.TextView(parent.context).apply {
            text = "Tap to retry"
            textSize = 14f
            setTextColor(
                style.chipActiveBackgroundColor.takeIf { it != 0 }
                    ?: android.graphics.Color.parseColor("#6852D6")
            )
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        container.addView(retryText)

        return container
    }

    // endregion

    private fun getRelativeTime(sentAtSeconds: Long): String {
        val timestampMs = sentAtSeconds * 1000
        val date = Date(timestampMs)
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date

        val today = java.util.Calendar.getInstance()
        val currentYear = today.get(java.util.Calendar.YEAR)
        val inputYear = calendar.get(java.util.Calendar.YEAR)

        val isToday = calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)

        val format = when {
            isToday -> SimpleDateFormat("h:mm a", Locale.getDefault())
            inputYear == currentYear -> SimpleDateFormat("d MMMM, h:mm a", Locale.getDefault())
            else -> SimpleDateFormat("d MMMM yyyy, h:mm a", Locale.getDefault())
        }

        return format.format(date)
    }
}
