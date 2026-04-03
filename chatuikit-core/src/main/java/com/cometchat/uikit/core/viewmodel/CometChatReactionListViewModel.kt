package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ReactionsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Reaction
import com.cometchat.chat.models.ReactionCount
import com.cometchat.chat.models.ReactionEvent
import com.cometchat.uikit.core.CometChatUIKit
import com.cometchat.uikit.core.domain.usecase.FetchReactionsUseCase
import com.cometchat.uikit.core.domain.usecase.RemoveReactionUseCase
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.MessageStatus
import com.cometchat.uikit.core.state.ReactionListUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing reaction list state.
 * Handles fetching reactions, caching per emoji tab, and reaction removal.
 * Uses StateFlow for reactive state management.
 *
 * @param fetchReactionsUseCase Use case for fetching reactions
 * @param removeReactionUseCase Use case for removing reactions
 * @param enableListeners Whether to enable CometChat listeners (set to false for testing)
 */
open class CometChatReactionListViewModel(
    private val fetchReactionsUseCase: FetchReactionsUseCase,
    private val removeReactionUseCase: RemoveReactionUseCase,
    private val enableListeners: Boolean = true
) : ViewModel() {

    companion object {
        private const val TAG = "ReactionListViewModel"
        private const val DEFAULT_LIMIT = 10
        internal const val ALL_TAB_KEY = "All"
    }

    // UI State
    private val _uiState = MutableStateFlow<ReactionListUIState>(ReactionListUIState.Loading)
    val uiState: StateFlow<ReactionListUIState> = _uiState.asStateFlow()

    // Reaction headers (tabs) - includes "All" tab as first item
    private val _reactionHeaders = MutableStateFlow<List<ReactionCount>>(emptyList())
    val reactionHeaders: StateFlow<List<ReactionCount>> = _reactionHeaders.asStateFlow()

    // Reacted users list for current tab
    private val _reactedUsers = MutableStateFlow<List<Reaction>>(emptyList())
    val reactedUsers: StateFlow<List<Reaction>> = _reactedUsers.asStateFlow()

    // Selected reaction (emoji or "All")
    private val _selectedReaction = MutableStateFlow<String?>(null)
    val selectedReaction: StateFlow<String?> = _selectedReaction.asStateFlow()

    // Active tab index
    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex: StateFlow<Int> = _activeTabIndex.asStateFlow()

    // Base message
    private val _baseMessage = MutableStateFlow<BaseMessage?>(null)
    val baseMessage: StateFlow<BaseMessage?> = _baseMessage.asStateFlow()

    // Caching - keyed by emoji (or "All" for all reactions)
    private val reactionRequestCache = mutableMapOf<String, ReactionsRequest>()
    private val reactedUserCache = mutableMapOf<String, MutableList<Reaction>>()

    // State flags to prevent concurrent operations
    private var isFetching = false
    private var isRemovingReaction = false

    // Track the reaction being removed for state updates
    private var currentRemovedReaction: String? = null
    private var currentSelectedReactionTab: String? = null

    // Configuration
    private var reactionsRequestBuilder: ReactionsRequest.ReactionsRequestBuilder? = null

    // Local event listener job
    private var messageEventsJob: Job? = null

    init {
        if (enableListeners) {
            addListeners()
        }
    }

    // ==================== Public API Methods ====================

    /**
     * Sets the base message and extracts reaction headers.
     * This initializes the reaction tabs from the message's reaction counts.
     *
     * @param message The message to display reactions for
     */
    fun setBaseMessage(message: BaseMessage) {
        _baseMessage.value = message
        
        // Extract reaction counts from message and create headers
        val reactionCounts = message.reactions ?: emptyList()
        setReactionHeaders(reactionCounts)
        
        // Initialize with "All" tab selected
        if (reactionCounts.isNotEmpty()) {
            currentSelectedReactionTab = ALL_TAB_KEY
            _selectedReaction.value = ALL_TAB_KEY
            _activeTabIndex.value = 0
        }
    }

    /**
     * Sets the reactions request builder for custom fetch configuration.
     *
     * @param builder The custom request builder
     */
    fun setReactionsRequestBuilder(builder: ReactionsRequest.ReactionsRequestBuilder) {
        reactionsRequestBuilder = builder
    }

    /**
     * Sets the selected reaction and updates the active tab index.
     * This triggers fetching reactions for the selected emoji.
     *
     * @param reaction The emoji to filter by, or "All" for all reactions
     */
    fun setSelectedReaction(reaction: String?) {
        val reactionToSet = reaction ?: ALL_TAB_KEY
        _selectedReaction.value = reactionToSet
        currentSelectedReactionTab = reactionToSet
        
        // Update active tab index based on selected reaction
        val headers = _reactionHeaders.value
        if (headers.isNotEmpty()) {
            val foundIndex = headers.indexOfFirst { it.reaction == reactionToSet }
            if (foundIndex != -1) {
                _activeTabIndex.value = foundIndex
            }
        }
    }

    /**
     * Fetches reacted users for the current or specified reaction filter.
     * Uses caching to avoid redundant network requests.
     *
     * @param reactionFilter The emoji to filter by, or null for current selection
     * @param customBuilder Optional custom request builder
     */
    fun fetchReactedUsers(
        reactionFilter: String? = null,
        customBuilder: ReactionsRequest.ReactionsRequestBuilder? = null
    ) {
        // Prevent concurrent fetches
        if (isFetching) return

        val message = _baseMessage.value ?: return

        // Update selection if filter provided
        if (reactionFilter != null) {
            currentSelectedReactionTab = reactionFilter
            _selectedReaction.value = reactionFilter
            // Clear current list when switching tabs
            _reactedUsers.value = emptyList()
        }

        val tabKey = currentSelectedReactionTab ?: ALL_TAB_KEY

        // Check cache first - display cached data immediately
        if (reactedUserCache.containsKey(tabKey)) {
            _reactedUsers.value = reactedUserCache[tabKey] ?: emptyList()
        }

        // Show loading state only if no cached data
        if (_reactedUsers.value.isEmpty()) {
            _uiState.value = ReactionListUIState.Loading
        }

        // Get or create request for this tab
        val reactionsRequest = getOrCreateRequest(tabKey, message.id, customBuilder)

        viewModelScope.launch {
            isFetching = true

            fetchReactionsUseCase(reactionsRequest)
                .onSuccess { reactions ->
                    handleFetchSuccess(tabKey, reactions)
                }
                .onFailure { exception ->
                    handleFetchError(exception as CometChatException)
                }

            isFetching = false
        }
    }

    /**
     * Removes a reaction from the message.
     * Only the logged-in user's own reactions can be removed.
     *
     * @param message The message to remove reaction from
     * @param emoji The emoji reaction to remove
     */
    fun removeReaction(message: BaseMessage, emoji: String) {
        // Prevent concurrent removals
        if (isRemovingReaction) return

        isRemovingReaction = true
        currentRemovedReaction = emoji

        viewModelScope.launch {
            removeReactionUseCase(message.id, emoji)
                .onSuccess { updatedMessage ->
                    handleReactionRemovedByMe(emoji)
                    isRemovingReaction = false
                }
                .onFailure { exception ->
                    // Reset flag on failure to allow retry
                    isRemovingReaction = false
                }
        }
    }

    /**
     * Clears both caches (request and user caches).
     * Call this when the message changes or component is reset.
     */
    fun clearCache() {
        reactionRequestCache.clear()
        reactedUserCache.clear()
    }

    // ==================== Private Methods ====================

    /**
     * Sets the reaction headers from reaction counts.
     * Creates an "All" tab as the first item with total count.
     */
    private fun setReactionHeaders(reactionCounts: List<ReactionCount>) {
        if (reactionCounts.isEmpty()) {
            _reactionHeaders.value = emptyList()
            return
        }

        val headers = mutableListOf<ReactionCount>()

        // Create "All" tab with total count
        val totalCount = reactionCounts.sumOf { it.count }
        val allTab = ReactionCount().apply {
            reaction = ALL_TAB_KEY
            count = totalCount
            setReactedByMe(false)
        }
        headers.add(allTab)

        // Add individual emoji tabs
        headers.addAll(reactionCounts)

        _reactionHeaders.value = headers
    }

    /**
     * Gets or creates a ReactionsRequest for the specified tab.
     */
    private fun getOrCreateRequest(
        tabKey: String,
        messageId: Long,
        customBuilder: ReactionsRequest.ReactionsRequestBuilder?
    ): ReactionsRequest {
        // Return cached request if exists
        reactionRequestCache[tabKey]?.let { return it }

        // Create new request
        val builder = customBuilder
            ?: reactionsRequestBuilder
            ?: ReactionsRequest.ReactionsRequestBuilder().setLimit(DEFAULT_LIMIT)

        builder.setMessageId(messageId)

        // Set reaction filter if not "All" tab
        if (tabKey != ALL_TAB_KEY) {
            builder.setReaction(tabKey)
        }

        val request = builder.build()
        reactionRequestCache[tabKey] = request
        return request
    }

    /**
     * Handles successful fetch of reactions.
     */
    private fun handleFetchSuccess(tabKey: String, reactions: List<Reaction>) {
        if (reactions.isNotEmpty()) {
            // Update cache
            if (reactedUserCache.containsKey(tabKey)) {
                reactedUserCache[tabKey]?.addAll(reactions)
            } else {
                reactedUserCache[tabKey] = reactions.toMutableList()
            }

            // Update UI
            _reactedUsers.value = reactedUserCache[tabKey] ?: emptyList()
        }

        _uiState.value = ReactionListUIState.Content
    }

    /**
     * Handles fetch error.
     */
    private fun handleFetchError(exception: CometChatException) {
        // Don't show error for "request in progress" errors
        if (exception.code != CometChatConstants.Errors.ERROR_REQUEST_IN_PROGRESS) {
            _uiState.value = ReactionListUIState.Error(exception)
        }
    }

    /**
     * Handles reaction removal by the logged-in user.
     * Updates headers and cache accordingly.
     */
    private fun handleReactionRemovedByMe(removedReaction: String) {
        val loggedInUserId = getLoggedInUserSafe()?.uid ?: return
        var newActiveTab = -2

        val currentHeaders = _reactionHeaders.value.toMutableList()
        if (currentHeaders.isEmpty()) return

        // Find and update the removed reaction's header
        val reactionHeaderIndex = currentHeaders.indexOfFirst { it.reaction == removedReaction }
        if (reactionHeaderIndex != -1) {
            val rc = currentHeaders[reactionHeaderIndex]
            if (rc.reactedByMe) {
                if (rc.count == 1) {
                    // Remove the tab entirely
                    newActiveTab = 0
                    currentHeaders.removeAt(reactionHeaderIndex)
                } else {
                    // Decrease count
                    if (currentSelectedReactionTab != ALL_TAB_KEY) {
                        newActiveTab = reactionHeaderIndex
                    }
                    currentHeaders[reactionHeaderIndex] = ReactionCount().apply {
                        reaction = rc.reaction
                        count = rc.count - 1
                        setReactedByMe(false)
                    }
                }
            }

            // Update "All" tab count
            if (currentHeaders.isNotEmpty()) {
                val allTab = currentHeaders[0]
                if (allTab.count == 1) {
                    // Remove "All" tab (list is now empty)
                    newActiveTab = -1
                    currentHeaders.removeAt(0)
                } else {
                    currentHeaders[0] = ReactionCount().apply {
                        reaction = ALL_TAB_KEY
                        count = allTab.count - 1
                        setReactedByMe(false)
                    }
                }
            }
        }

        // Update cache
        if (newActiveTab == -1) {
            // All reactions removed - clear cache
            reactedUserCache.clear()
        } else {
            // Remove user from specific reaction cache
            removeUserFromCache(removedReaction, loggedInUserId, isAllTab = false)
            // Remove user from "All" cache
            removeUserFromCache(removedReaction, loggedInUserId, isAllTab = true)
        }

        // Update headers
        _reactionHeaders.value = currentHeaders

        // Update active tab and selection
        when {
            newActiveTab == -1 || newActiveTab == 0 -> {
                _activeTabIndex.value = if (currentHeaders.isEmpty()) -1 else 0
                _selectedReaction.value = ALL_TAB_KEY
                currentSelectedReactionTab = ALL_TAB_KEY
            }
            newActiveTab > 0 -> {
                _selectedReaction.value = removedReaction
            }
        }

        // Update UI state
        if (currentHeaders.isEmpty()) {
            _uiState.value = ReactionListUIState.Empty
        }
    }

    /**
     * Handles reaction removal by another user (real-time update).
     */
    private fun handleReactionRemoved(removedReaction: String, uid: String) {
        val tabIndex = getReactionIndexFromHeader(currentSelectedReactionTab ?: ALL_TAB_KEY)

        // Update reaction header based on the removed reaction
        updateReactionHeaderList(tabIndex, getReactionIndexFromHeader(removedReaction))

        // Remove the user from the cache for the specific reaction and "All" reactions
        removeUserFromCache(removedReaction, uid, isAllTab = false)
        removeUserFromCache(removedReaction, uid, isAllTab = true)
    }

    /**
     * Finds the index of a specified reaction in the header list.
     */
    private fun getReactionIndexFromHeader(reaction: String): Int {
        return _reactionHeaders.value.indexOfFirst { it.reaction == reaction }
    }

    /**
     * Updates the reaction header list by adjusting counts or removing reactions.
     */
    private fun updateReactionHeaderList(tabIndex: Int, removedReactionIndex: Int) {
        if (removedReactionIndex == -1) return

        val currentHeaders = _reactionHeaders.value.toMutableList()
        if (currentHeaders.isEmpty()) return

        var newActiveTab = tabIndex

        // Decrease the count of the removed reaction
        val removedReactionObj = currentHeaders[removedReactionIndex]
        val newCount = removedReactionObj.count - 1

        if (newCount < 1) {
            // Remove the reaction from the list
            if (currentSelectedReactionTab == removedReactionObj.reaction) {
                newActiveTab = 0
            }
            currentHeaders.removeAt(removedReactionIndex)
        } else {
            currentHeaders[removedReactionIndex] = ReactionCount().apply {
                reaction = removedReactionObj.reaction
                count = newCount
                setReactedByMe(removedReactionObj.reactedByMe)
            }
        }

        // Handle the "All" tab adjustments
        if (currentHeaders.isNotEmpty()) {
            val allTab = currentHeaders[0]
            val allCount = allTab.count - 1

            if (allCount < 1) {
                currentHeaders.removeAt(0)
            } else {
                currentHeaders[0] = ReactionCount().apply {
                    reaction = ALL_TAB_KEY
                    count = allCount
                    setReactedByMe(false)
                }
            }
        }

        // Update state
        _reactionHeaders.value = currentHeaders
        _activeTabIndex.value = newActiveTab

        if (currentHeaders.isEmpty()) {
            _uiState.value = ReactionListUIState.Empty
        }
    }

    /**
     * Removes a user's reaction from the cache.
     */
    private fun removeUserFromCache(removedReaction: String, uid: String, isAllTab: Boolean) {
        val cacheKey = if (isAllTab) ALL_TAB_KEY else removedReaction

        reactedUserCache[cacheKey]?.let { reactions ->
            val iterator = reactions.iterator()
            while (iterator.hasNext()) {
                val reaction = iterator.next()
                if (reaction.reaction == removedReaction && reaction.uid == uid) {
                    iterator.remove()
                    break
                }
            }
        }

        // Update displayed list if viewing affected tab
        if (currentSelectedReactionTab == cacheKey) {
            _reactedUsers.value = reactedUserCache[cacheKey] ?: emptyList()
        }
    }

    /**
     * Safely gets the logged-in user, returning null if SDK is not initialized.
     */
    private fun getLoggedInUserSafe(): com.cometchat.chat.models.User? {
        return try {
            CometChatUIKit.getLoggedInUser()
        } catch (e: Exception) {
            null
        }
    }

    // ==================== Listener Management ====================

    /**
     * Adds listeners for real-time reaction updates.
     */
    private fun addListeners() {
        addLocalEventListeners()
    }

    /**
     * Adds local event listeners for UI-triggered message events.
     */
    private fun addLocalEventListeners() {
        messageEventsJob = viewModelScope.launch {
            CometChatEvents.messageEvents.collect { event ->
                when (event) {
                    is CometChatMessageEvent.MessageEdited -> {
                        // Handle reaction removed by me (via SDK callback)
                        val message = _baseMessage.value
                        if (message != null && message.id == event.message.id) {
                            if (event.status == MessageStatus.SUCCESS && currentRemovedReaction != null) {
                                // Reaction removal was successful
                                val loggedInUserId = getLoggedInUserSafe()?.uid
                                if (loggedInUserId != null) {
                                    handleReactionRemovedByMe(currentRemovedReaction!!)
                                }
                                currentRemovedReaction = null
                            }
                        }
                    }

                    is CometChatMessageEvent.ReactionRemoved -> {
                        // Handle reaction removed by another user
                        val message = _baseMessage.value
                        if (message != null && message.id == event.event.reaction.messageId) {
                            handleReactionRemoved(
                                event.event.reaction.reaction,
                                event.event.reaction.uid
                            )
                        }
                    }

                    else -> {
                        // Ignore other events
                    }
                }
            }
        }
    }

    /**
     * Removes all listeners.
     */
    private fun removeListeners() {
        messageEventsJob?.cancel()
        messageEventsJob = null
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
        clearCache()
    }
}
