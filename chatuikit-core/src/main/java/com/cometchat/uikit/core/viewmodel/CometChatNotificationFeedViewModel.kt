package com.cometchat.uikit.core.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.NotificationCategoriesRequest
import com.cometchat.chat.core.NotificationFeedListener
import com.cometchat.chat.core.NotificationFeedRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.NotificationCategory
import com.cometchat.chat.models.NotificationFeedItem
import com.cometchat.uikit.core.state.FilterChipState
import com.cometchat.uikit.core.state.NotificationFeedUIState
import com.cometchat.uikit.core.state.TimestampGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * ViewModel for the CometChatNotificationFeed component.
 * Manages notification feed data, categories, engagement reporting,
 * real-time updates, and unread count polling.
 *
 * Shared by both Jetpack Compose and XML View implementations.
 *
 * @param feedRequestBuilder Optional custom request builder for feed items
 * @param categoriesRequestBuilder Optional custom request builder for categories
 * @param enableListeners Whether to enable WebSocket listeners (false for testing)
 * @param pollingIntervalMs Interval for unread count polling (default: 30000ms)
 */
open class CometChatNotificationFeedViewModel(
    private val feedRequestBuilder: NotificationFeedRequest.NotificationFeedRequestBuilder? = null,
    private val categoriesRequestBuilder: NotificationCategoriesRequest.NotificationCategoriesRequestBuilder? = null,
    private val enableListeners: Boolean = true,
    private val pollingIntervalMs: Long = 30_000L
) : ViewModel(), ListOperations<NotificationFeedItem> {

    // region State Flows

    /** Current screen state (Loading, Content, Empty, Error) */
    private val _uiState = MutableStateFlow<NotificationFeedUIState>(NotificationFeedUIState.Loading)
    val uiState: StateFlow<NotificationFeedUIState> = _uiState.asStateFlow()

    /** Raw feed items list (ungrouped) */
    private val _feedItems = MutableStateFlow<List<NotificationFeedItem>>(emptyList())
    val feedItems: StateFlow<List<NotificationFeedItem>> = _feedItems.asStateFlow()

    /** Available categories from server */
    private val _categories = MutableStateFlow<List<NotificationCategory>>(emptyList())
    val categories: StateFlow<List<NotificationCategory>> = _categories.asStateFlow()

    /** Currently active category filter (null = "All") */
    private val _activeCategory = MutableStateFlow<String?>(null)
    val activeCategory: StateFlow<String?> = _activeCategory.asStateFlow()

    /** Filter chip states (computed from categories + active + unread counts) */
    private val _filterChips = MutableStateFlow<List<FilterChipState>>(emptyList())
    val filterChips: StateFlow<List<FilterChipState>> = _filterChips.asStateFlow()

    /** Total unread count */
    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()

    /** Per-category unread counts */
    private val _categoryUnreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val categoryUnreadCounts: StateFlow<Map<String, Int>> = _categoryUnreadCounts.asStateFlow()

    /** Whether more pages are available */
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    /** Whether a pagination fetch is in progress */
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    /** Whether a refresh is in progress */
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** Whether pagination failed (show retry) */
    private val _isPaginationError = MutableStateFlow(false)
    val isPaginationError: StateFlow<Boolean> = _isPaginationError.asStateFlow()

    /** Event emitted when a new item is received via WebSocket (for scroll-to-top) */
    private val _newItemEvent = MutableSharedFlow<Unit>()
    val newItemEvent: SharedFlow<Unit> = _newItemEvent.asSharedFlow()

    // endregion

    // region Internal State

    private var feedRequest: NotificationFeedRequest? = null
    private var categoriesRequest: NotificationCategoriesRequest? = null
    private var isFetching = false
    private var pollingJob: Job? = null
    private val listenerId = "NotificationFeedListener_${UUID.randomUUID()}"
    private val userLimit: Int = feedRequestBuilder?.build()?.limit ?: 20
    private val userReadState: com.cometchat.chat.enums.FeedReadState? = feedRequestBuilder?.build()?.readState

    /** Tracks items currently visible in viewport with their visibility start time */
    private val visibleItems = ConcurrentHashMap<String, Long>()

    /** Tracks items that have already been reported as delivered */
    private val deliveredItemIds = mutableSetOf<String>()

    /** Tracks items that have already been reported as read */
    private val readItemIds = mutableSetOf<String>()

    // List operations delegate
    private val listDelegate = ListOperationsDelegate(
        stateFlow = _feedItems,
        equalityChecker = { a, b -> a.id == b.id }
    )

    // endregion

    // region Lifecycle

    init {
        if (enableListeners) {
            registerListener()
            fetchCategories()
            fetchInitialItems()
            startUnreadCountPolling()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (enableListeners) {
            CometChat.removeNotificationFeedListener(listenerId)
        }
        stopUnreadCountPolling()
    }

    // endregion

    // region Data Operations

    /**
     * Fetches notification categories from the server.
     * Populates filter chips with "All" + server categories.
     */
    fun fetchCategories() {
        viewModelScope.launch {
            val request = categoriesRequest ?: run {
                val builder = categoriesRequestBuilder
                    ?: NotificationCategoriesRequest.NotificationCategoriesRequestBuilder()
                        .setLimit(50)
                builder.build().also { categoriesRequest = it }
            }

            request.fetchNext(object : CometChat.CallbackListener<List<NotificationCategory>>() {
                override fun onSuccess(result: List<NotificationCategory>) {
                    _categories.value = result
                    updateFilterChips()
                }

                override fun onError(e: CometChatException) {
                    // Categories fetch failure is non-fatal — show "All" chip only
                    _categories.value = emptyList()
                    updateFilterChips()
                }
            })
        }
    }

    /**
     * Fetches the initial page of feed items.
     * Shows loading state, then transitions to content/empty/error.
     */
    fun fetchInitialItems() {
        if (isFetching) return
        isFetching = true
        _uiState.value = NotificationFeedUIState.Loading

        viewModelScope.launch {
            createFeedRequest()

            feedRequest?.fetchNext(object : CometChat.CallbackListener<List<NotificationFeedItem>>() {
                override fun onSuccess(items: List<NotificationFeedItem>) {
                    isFetching = false
                    if (items.isEmpty()) {
                        _hasMorePages.value = false
                        _feedItems.value = emptyList()
                        _uiState.value = NotificationFeedUIState.Empty
                    } else {
                        _feedItems.value = items
                        _hasMorePages.value = true
                        updateUIStateFromItems()
                        reportDeliveredBatch(items)
                    }
                }

                override fun onError(e: CometChatException) {
                    isFetching = false
                    _uiState.value = NotificationFeedUIState.Error(e)
                }
            })
        }
    }

    /**
     * Fetches the next page of feed items (infinite scroll trigger).
     * Appends results to existing items.
     */
    fun fetchNextPage() {
        if (isFetching || !_hasMorePages.value || _isLoadingMore.value) return

        _isLoadingMore.value = true
        _isPaginationError.value = false

        feedRequest?.fetchNext(object : CometChat.CallbackListener<List<NotificationFeedItem>>() {
            override fun onSuccess(items: List<NotificationFeedItem>) {
                _isLoadingMore.value = false
                if (items.isEmpty()) {
                    _hasMorePages.value = false
                } else {
                    _feedItems.value = _feedItems.value + items
                    updateUIStateFromItems()
                    reportDeliveredBatch(items)
                }
            }

            override fun onError(e: CometChatException) {
                _isLoadingMore.value = false
                _isPaginationError.value = true
            }
        })
    }

    /**
     * Pull-to-refresh: resets pagination and fetches fresh data.
     */
    fun refresh() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true

        viewModelScope.launch {
            createFeedRequest()
            feedRequest?.fetchNext(object : CometChat.CallbackListener<List<NotificationFeedItem>>() {
                override fun onSuccess(items: List<NotificationFeedItem>) {
                    _isRefreshing.value = false
                    if (items.isEmpty()) {
                        _hasMorePages.value = false
                        _feedItems.value = emptyList()
                        _uiState.value = NotificationFeedUIState.Empty
                    } else {
                        _feedItems.value = items
                        _hasMorePages.value = true
                        updateUIStateFromItems()
                        reportDeliveredBatch(items)
                    }
                }

                override fun onError(e: CometChatException) {
                    _isRefreshing.value = false
                    _uiState.value = NotificationFeedUIState.Error(e)
                }
            })
        }
    }

    /**
     * Switches the active category filter.
     * Resets pagination and fetches fresh data for the selected category.
     *
     * @param categoryId Category ID to filter by, or null for "All"
     */
    fun switchCategory(categoryId: String?) {
        if (_activeCategory.value == categoryId) return
        _activeCategory.value = categoryId
        updateFilterChips()
        // Reset and fetch with new category
        _feedItems.value = emptyList()
        _hasMorePages.value = true
        feedRequest = null
        fetchInitialItems()
    }

    // endregion

    // region Engagement Reporting

    /**
     * Reports delivery for a batch of items (fire-and-forget).
     */
    private fun reportDeliveredBatch(items: List<NotificationFeedItem>) {
        items.forEach { item ->
            if (item.id != null && !deliveredItemIds.contains(item.id) && item.deliveredAt == null) {
                deliveredItemIds.add(item.id)
                reportDelivered(item)
            }
        }
    }

    /**
     * Reports a single item as delivered.
     */
    fun reportDelivered(item: NotificationFeedItem) {
        CometChat.markFeedItemAsDelivered(item, object : CometChat.CallbackListener<Void?>() {
            override fun onSuccess(result: Void?) {
                // Fire-and-forget success
            }

            override fun onError(e: CometChatException) {
                // Silent failure — log warning
            }
        })
    }

    /**
     * Reports an item as read (visible for ≥1 second).
     * Updates local read state and unread counts.
     */
    private fun reportRead(item: NotificationFeedItem) {
        if (item.id == null || readItemIds.contains(item.id) || item.isRead) return
        readItemIds.add(item.id)

        CometChat.markFeedItemAsRead(item, object : CometChat.CallbackListener<Void?>() {
            override fun onSuccess(result: Void?) {
                updateItemReadState(item.id)
            }

            override fun onError(e: CometChatException) {
                readItemIds.remove(item.id)
            }
        })
    }

    // endregion

    // region Visibility Tracking

    /**
     * Called when a feed item becomes visible in the viewport.
     * Starts 1-second timer for "read" reporting.
     *
     * @param item The item that became visible
     */
    fun onItemBecameVisible(item: NotificationFeedItem) {
        if (item.id == null) return

        // Start 1-second timer for read
        if (!item.isRead && !readItemIds.contains(item.id)) {
            visibleItems[item.id] = System.currentTimeMillis()
            viewModelScope.launch {
                delay(1000L)
                // Check if item is still visible after 1 second
                if (visibleItems.containsKey(item.id)) {
                    reportRead(item)
                    visibleItems.remove(item.id)
                }
            }
        }
    }

    /**
     * Called when a feed item leaves the viewport.
     * Cancels the 1-second read timer if it hasn't fired yet.
     *
     * @param item The item that left the viewport
     */
    fun onItemBecameHidden(item: NotificationFeedItem) {
        if (item.id == null) return
        visibleItems.remove(item.id)
    }

    // endregion

    // region Real-Time Updates

    private fun registerListener() {
        CometChat.addNotificationFeedListener(listenerId, object : NotificationFeedListener() {
            override fun onFeedItemReceived(feedItem: NotificationFeedItem) {
                Log.i("TAG", "onFeedItemReceived: item received via ws: ${feedItem.category}")
                onNewFeedItemReceived(feedItem)
            }
        })
    }

    /**
     * Handles a new feed item received via WebSocket.
     * Inserts at top of list and reports delivery.
     */
    fun onNewFeedItemReceived(item: NotificationFeedItem) {
        viewModelScope.launch {
            // Check if item matches current category filter
            val currentCategory = _activeCategory.value
            if (currentCategory != null && item.category != currentCategory) {
                // Item doesn't match current filter — update unread counts only
                updateUnreadCountForNewItem(item)
                return@launch
            }

            // Insert at top
            _feedItems.value = listOf(item) + _feedItems.value
            updateUIStateFromItems()

            // Report delivered
            if (item.id != null && !deliveredItemIds.contains(item.id) && item.deliveredAt == null) {
                deliveredItemIds.add(item.id)
                reportDelivered(item)
            }

            // Update unread counts
            updateUnreadCountForNewItem(item)

            // Emit scroll-to-top event
            _newItemEvent.emit(Unit)
        }
    }

    // endregion

    // region Unread Count Polling

    /**
     * Starts polling for unread counts at the configured interval.
     */
    fun startUnreadCountPolling() {
        stopUnreadCountPolling()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(pollingIntervalMs)
                fetchUnreadCount()
            }
        }
    }

    /**
     * Stops the unread count polling.
     */
    fun stopUnreadCountPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun fetchUnreadCount() {
        CometChat.getNotificationFeedUnreadCount(object : CometChat.CallbackListener<Int>() {
            override fun onSuccess(count: Int) {
                _totalUnreadCount.value = count
                updateFilterChips()
            }

            override fun onError(e: CometChatException) {
                // Silent failure
            }
        })
    }

    // endregion

    // region Timestamp Grouping

    /**
     * Groups feed items by their sentAt timestamp into labeled sections.
     *
     * Rules:
     * - sentAt is today → "Today"
     * - sentAt is yesterday → "Yesterday"
     * - sentAt is within this week → Day name (e.g., "Monday")
     * - sentAt is older → Localized date (e.g., "Jan 15, 2025")
     *
     * @param items List of feed items to group
     * @param locale Locale for date formatting (default: system default)
     * @return List of TimestampGroups ordered newest to oldest
     */
    fun groupByTimestamp(
        items: List<NotificationFeedItem>,
        locale: Locale = Locale.getDefault()
    ): List<TimestampGroup> {
        if (items.isEmpty()) return emptyList()

        val now = Calendar.getInstance()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val yesterday = Calendar.getInstance().apply {
            time = today.time
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val startOfWeek = Calendar.getInstance().apply {
            time = today.time
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }

        val dayNameFormat = SimpleDateFormat("EEEE", locale)
        val dateFormat = SimpleDateFormat("MMM d, yyyy", locale)

        val grouped = items.groupBy { item ->
            val itemCal = Calendar.getInstance().apply {
                timeInMillis = item.sentAt * 1000
            }
            val itemDay = Calendar.getInstance().apply {
                timeInMillis = item.sentAt * 1000
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            when {
                !itemDay.before(today) -> "Today"
                !itemDay.before(yesterday) -> "Yesterday"
                !itemDay.before(startOfWeek) -> dayNameFormat.format(Date(item.sentAt * 1000))
                else -> dateFormat.format(Date(item.sentAt * 1000))
            }
        }

        // Maintain order: groups should be newest first
        return grouped.map { (label, groupItems) ->
            TimestampGroup(
                label = label,
                items = groupItems.sortedByDescending { it.sentAt }
            )
        }
    }

    // endregion

    // region Internal Helpers

    private fun createFeedRequest() {
        val builder = NotificationFeedRequest.NotificationFeedRequestBuilder()
            .setLimit(userLimit)

        _activeCategory.value?.let { category ->
            builder.setCategory(category)
        }

        userReadState?.let { readState ->
            builder.setReadState(readState)
        }

        feedRequest = builder.build()
    }

    private fun updateUIStateFromItems() {
        val items = _feedItems.value
        if (items.isEmpty()) {
            _uiState.value = NotificationFeedUIState.Empty
        } else {
            val grouped = groupByTimestamp(items)
            _uiState.value = NotificationFeedUIState.Content(grouped)
        }
    }

    private fun updateFilterChips() {
        val cats = _categories.value
        val active = _activeCategory.value
        val unreadCounts = _categoryUnreadCounts.value

        val chips = mutableListOf<FilterChipState>()

        // "All" chip always first
        chips.add(
            FilterChipState(
                id = "all",
                label = "All",
                isActive = active == null,
                unreadCount = _totalUnreadCount.value
            )
        )

        // Server-provided categories
        cats.forEach { category ->
            chips.add(
                FilterChipState(
                    id = category.name ?: category.id ?: "",
                    label = category.name ?: "",
                    isActive = active == (category.name ?: category.id),
                    unreadCount = unreadCounts[category.name ?: category.id] ?: 0
                )
            )
        }

        _filterChips.value = chips
    }

    private fun updateItemReadState(itemId: String) {
        val now = System.currentTimeMillis() / 1000
        var readCategory: String? = null
        val updatedItems = _feedItems.value.map { item ->
            if (item.id == itemId && !item.isRead) {
                readCategory = item.category
                item.apply { readAt = now }
            } else {
                item
            }
        }
        _feedItems.value = updatedItems
        updateUIStateFromItems()

        // Decrement total unread count
        val currentTotal = _totalUnreadCount.value
        if (currentTotal > 0) {
            _totalUnreadCount.value = currentTotal - 1
        }

        // Decrement per-category unread count
        readCategory?.let { category ->
            val counts = _categoryUnreadCounts.value.toMutableMap()
            val currentCategoryCount = counts[category] ?: 0
            if (currentCategoryCount > 0) {
                counts[category] = currentCategoryCount - 1
            }
            _categoryUnreadCounts.value = counts
        }

        updateFilterChips()
    }

    private fun updateUnreadCountForNewItem(item: NotificationFeedItem) {
        if (!item.isRead) {
            _totalUnreadCount.value = _totalUnreadCount.value + 1
            item.category?.let { category ->
                val counts = _categoryUnreadCounts.value.toMutableMap()
                counts[category] = (counts[category] ?: 0) + 1
                _categoryUnreadCounts.value = counts
            }
            updateFilterChips()
        }
    }

    // endregion

    // region ListOperations Implementation

    override fun addItem(item: NotificationFeedItem) = listDelegate.addItem(item)
    override fun addItems(items: List<NotificationFeedItem>) = listDelegate.addItems(items)
    override fun removeItem(item: NotificationFeedItem): Boolean = listDelegate.removeItem(item)
    override fun removeItemAt(index: Int): NotificationFeedItem? = listDelegate.removeItemAt(index)
    override fun updateItem(item: NotificationFeedItem, predicate: (NotificationFeedItem) -> Boolean): Boolean =
        listDelegate.updateItem(item, predicate)
    override fun clearItems() = listDelegate.clearItems()
    override fun getItems(): List<NotificationFeedItem> = listDelegate.getItems()
    override fun getItemAt(index: Int): NotificationFeedItem? = listDelegate.getItemAt(index)
    override fun getItemCount(): Int = listDelegate.getItemCount()
    override fun moveItemToTop(item: NotificationFeedItem) = listDelegate.moveItemToTop(item)
    override fun batch(operations: ListOperationsBatchScope<NotificationFeedItem>.() -> Unit) =
        listDelegate.batch(operations)

    // endregion
}
