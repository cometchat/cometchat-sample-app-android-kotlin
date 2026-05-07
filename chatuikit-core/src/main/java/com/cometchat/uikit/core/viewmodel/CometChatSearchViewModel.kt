package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.enums.AttachmentType
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.uikit.core.constants.FilterGroup
import com.cometchat.uikit.core.constants.SearchFilter
import com.cometchat.uikit.core.constants.SearchMode
import com.cometchat.uikit.core.constants.SearchScope
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.FetchConversationsUseCase
import com.cometchat.uikit.core.domain.usecase.FetchMessagesUseCase
import com.cometchat.uikit.core.state.SearchUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the CometChatSearch component.
 * Manages search state, debouncing, and data fetching for both conversations and messages.
 *
 * Features:
 * - Dual search: Searches both Conversations AND Messages simultaneously
 * - Filter chips: Photos, Videos, Documents, Links, Audio, Groups, Unread
 * - Debounced search: 450ms delay to prevent excessive API calls
 * - Search scope configuration: Messages, Conversations, or Both
 * - UID/GUID filtering: Contextual search within specific conversations
 * - Pagination support for both conversations and messages
 *
 * @param fetchConversationsUseCase Use case for fetching conversations
 * @param fetchMessagesUseCase Use case for fetching messages
 */
open class CometChatSearchViewModel(
    private val fetchConversationsUseCase: FetchConversationsUseCase,
    private val fetchMessagesUseCase: FetchMessagesUseCase
) : ViewModel() {

    companion object {
        const val DEFAULT_LIMIT = 15
        const val DEBOUNCE_DELAY_MS = 450L
    }

    // UI State
    private val _uiState = MutableStateFlow<SearchUIState>(SearchUIState.Initial)
    val uiState: StateFlow<SearchUIState> = _uiState.asStateFlow()

    // Conversations list
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Messages list
    private val _messages = MutableStateFlow<List<BaseMessage>>(emptyList())
    val messages: StateFlow<List<BaseMessage>> = _messages.asStateFlow()

    // Pagination flags
    private val _hasMoreConversations = MutableStateFlow(true)
    val hasMoreConversations: StateFlow<Boolean> = _hasMoreConversations.asStateFlow()

    private val _hasMoreMessages = MutableStateFlow(true)
    val hasMoreMessages: StateFlow<Boolean> = _hasMoreMessages.asStateFlow()

    // Selected filters
    private val _selectedFilters = MutableStateFlow<Set<SearchFilter>>(emptySet())
    val selectedFilters: StateFlow<Set<SearchFilter>> = _selectedFilters.asStateFlow()

    // Visible filters - shows all 7 when no selection, or only same-group filters when selected
    private val _visibleFilters = MutableStateFlow<List<SearchFilter>>(SearchFilter.entries.toList())
    val visibleFilters: StateFlow<List<SearchFilter>> = _visibleFilters.asStateFlow()

    // Search configuration
    private var searchScopes: List<SearchScope> = listOf(SearchScope.MESSAGES, SearchScope.CONVERSATIONS)
    private var uid: String? = null
    private var guid: String? = null

    // Request builders
    private var conversationsRequestBuilder: ConversationsRequest.ConversationsRequestBuilder? = null
    private var messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder? = null

    // Built requests for pagination
    private var currentConversationsRequest: ConversationsRequest? = null
    private var currentMessagesRequest: MessagesRequest? = null

    // Debounce handling
    private var searchJob: Job? = null

    // Request state tracking
    private var isConversationRequestPending = false
    private var isMessageRequestPending = false
    private var conversationRequestFailed = false
    private var messageRequestFailed = false

    // Last search parameters for pagination
    private var lastSearchText: String = ""
    private var lastFilters: Set<SearchFilter> = emptySet()

    // Last error for error state
    private var lastException: CometChatException? = null

    /**
     * Initiates a search for conversations and/or messages based on the search text and filters.
     * Implements 450ms debounce for text input, but executes immediately for filter changes.
     *
     * @param searchText The search query text
     * @param filters The set of selected filters
     */
    fun searchConversationsAndMessages(searchText: String, filters: Set<SearchFilter>) {
        if (searchText.isEmpty() && filters.isEmpty()) {
            clear()
            _uiState.value = SearchUIState.Initial
            return
        }

        // Skip re-execution if same search text and filters, and we already have results
        // This preserves expanded "See More" results across configuration changes
        if (searchText == lastSearchText && filters == lastFilters &&
            (_uiState.value is SearchUIState.Content || _uiState.value is SearchUIState.Empty)) {
            return
        }

        searchJob?.cancel()

        val isFilterChange = _selectedFilters.value != filters
        _selectedFilters.value = filters

        searchJob = viewModelScope.launch {
            // Immediate execution for filter changes, debounced for text input
            if (!isFilterChange && searchText.isNotEmpty()) {
                delay(DEBOUNCE_DELAY_MS)
            }

            _uiState.value = SearchUIState.Loading
            clearLists()

            lastSearchText = searchText
            lastFilters = filters

            val searchMode = getSearchMode(searchText, filters)
            performSearch(searchText, filters, searchMode)
        }
    }

    /**
     * Performs the actual search based on the determined search mode.
     */
    private suspend fun performSearch(
        searchText: String,
        filters: Set<SearchFilter>,
        searchMode: SearchMode
    ) {
        resetRequestStates()

        when (searchMode) {
            SearchMode.BOTH -> {
                isConversationRequestPending = true
                isMessageRequestPending = true
                coroutineScope {
                    launch { fetchConversations(searchText, filters) }
                    launch { fetchMessages(searchText, filters) }
                }
            }
            SearchMode.CONVERSATIONS -> {
                isConversationRequestPending = true
                fetchConversations(searchText, filters)
            }
            SearchMode.MESSAGES -> {
                isMessageRequestPending = true
                fetchMessages(searchText, filters)
            }
            SearchMode.NONE -> {
                _uiState.value = SearchUIState.Empty
            }
        }
    }

    /**
     * Determines the search mode based on search text, filters, and configuration.
     *
     * @param searchText The search query text
     * @param filters The set of selected filters
     * @return The appropriate SearchMode
     */
    fun getSearchMode(searchText: String, filters: Set<SearchFilter>): SearchMode {
        val messageFilters = filters.filter { it.isMessageFilter() }
        val conversationFilters = filters.filter { it.isConversationFilter() }

        val hasSearchText = searchText.isNotEmpty()
        val hasMessageFilters = messageFilters.isNotEmpty()
        val hasConversationFilters = conversationFilters.isNotEmpty()

        // UID/GUID forces messages-only search
        if (uid != null || guid != null) {
            return SearchMode.MESSAGES
        }

        val scopeConversation = searchScopes.contains(SearchScope.CONVERSATIONS)
        val scopeMessage = searchScopes.contains(SearchScope.MESSAGES)

        return when {
            // Search text without filters - use configured scopes
            hasSearchText && !hasMessageFilters && !hasConversationFilters -> {
                when {
                    scopeConversation && scopeMessage -> SearchMode.BOTH
                    scopeConversation -> SearchMode.CONVERSATIONS
                    scopeMessage -> SearchMode.MESSAGES
                    else -> SearchMode.NONE
                }
            }
            // No search text, only conversation filters
            !hasSearchText && hasConversationFilters && !hasMessageFilters -> SearchMode.CONVERSATIONS
            // No search text, only message filters
            !hasSearchText && !hasConversationFilters && hasMessageFilters -> SearchMode.MESSAGES
            // Search text with conversation filters only
            hasSearchText && hasConversationFilters && !hasMessageFilters -> SearchMode.CONVERSATIONS
            // Search text with message filters only
            hasSearchText && !hasConversationFilters && hasMessageFilters -> SearchMode.MESSAGES
            // Both filter types selected - this is a conflict, return NONE
            else -> SearchMode.NONE
        }
    }

    /**
     * Fetches conversations based on search text and filters.
     */
    private suspend fun fetchConversations(searchText: String, filters: Set<SearchFilter>) {
        val builder = conversationsRequestBuilder?.let {
            ConversationsRequest.ConversationsRequestBuilder()
        } ?: ConversationsRequest.ConversationsRequestBuilder()

        configureConversationRequest(builder, searchText, filters)
        currentConversationsRequest = builder.build()

        currentConversationsRequest?.let { request ->
            fetchConversationsUseCase(request)
                .onSuccess { conversations ->
                    // Filter out conversations with no last message when UNREAD filter is active
                    // The SDK returns joined groups with system-only "unread" counts (join notifications)
                    // but no actual chat messages — these appear as empty conversations in the UI
                    val filteredConversations = if (filters.contains(SearchFilter.UNREAD)) {
                        conversations.filter { it.lastMessage != null }
                    } else {
                        conversations
                    }
                    _conversations.value = _conversations.value + filteredConversations
                    _hasMoreConversations.value = fetchConversationsUseCase.hasMore()
                    isConversationRequestPending = false
                    conversationRequestFailed = false
                    updateFinalStateIfCompleted()
                }
                .onFailure { e ->
                    isConversationRequestPending = false
                    conversationRequestFailed = true
                    lastException = e as? CometChatException
                    updateFinalStateIfCompleted()
                }
        }
    }

    /**
     * Fetches messages based on search text and filters.
     */
    private suspend fun fetchMessages(searchText: String, filters: Set<SearchFilter>) {
        val builder = messagesRequestBuilder?.let {
            MessagesRequest.MessagesRequestBuilder()
        } ?: MessagesRequest.MessagesRequestBuilder()

        configureMessageRequest(builder, searchText, filters)
        currentMessagesRequest = builder.build()

        currentMessagesRequest?.let { request ->
            fetchMessagesUseCase(request)
                .onSuccess { messages ->
                    // Reverse messages for chronological display
                    _messages.value = _messages.value + messages.reversed()
                    _hasMoreMessages.value = fetchMessagesUseCase.hasMore()
                    isMessageRequestPending = false
                    messageRequestFailed = false
                    updateFinalStateIfCompleted()
                }
                .onFailure { e ->
                    isMessageRequestPending = false
                    messageRequestFailed = true
                    lastException = e as? CometChatException
                    updateFinalStateIfCompleted()
                }
        }
    }

    /**
     * Configures the conversation request builder with search text and filters.
     */
    private fun configureConversationRequest(
        builder: ConversationsRequest.ConversationsRequestBuilder,
        searchText: String,
        filters: Set<SearchFilter>
    ) {
        // Adjust limit based on whether filters are present
        val limit = if (filters.isEmpty() && searchText.isNotEmpty()) 3 else DEFAULT_LIMIT
        builder.setLimit(limit)

        if (searchText.isNotEmpty() && searchScopes.contains(SearchScope.CONVERSATIONS)) {
            builder.setSearchKeyword(searchText)
        } else {
            builder.setSearchKeyword(null)
        }

        // Apply conversation filters
        applyConversationFilters(builder, filters)
    }

    /**
     * Applies conversation filters to the request builder.
     * Matches the reference Java implementation.
     */
    private fun applyConversationFilters(
        builder: ConversationsRequest.ConversationsRequestBuilder,
        filters: Set<SearchFilter>
    ) {
        // Set unread filter
        builder.setUnread(filters.contains(SearchFilter.UNREAD))

        // Set conversation type filter for groups
        if (filters.contains(SearchFilter.GROUPS)) {
            builder.setConversationType(CometChatConstants.CONVERSATION_TYPE_GROUP)
        } else {
            builder.setConversationType(null)
        }
    }

    /**
     * Configures the message request builder with search text and filters.
     */
    private fun configureMessageRequest(
        builder: MessagesRequest.MessagesRequestBuilder,
        searchText: String,
        filters: Set<SearchFilter>
    ) {
        // Adjust limit based on whether filters are present
        val limit = if (filters.isEmpty() && searchText.isNotEmpty()) 3 else DEFAULT_LIMIT
        builder.setLimit(limit)

        // Set default message types when no filters
        builder.setTypes(listOf(
            UIKitConstants.MessageType.AUDIO,
            UIKitConstants.MessageType.FILE,
            UIKitConstants.MessageType.IMAGE,
            UIKitConstants.MessageType.VIDEO,
            UIKitConstants.MessageType.TEXT
        ))
        builder.hideDeletedMessages(true)

        // Apply UID/GUID if set
        uid?.let { builder.setUID(it) }
        guid?.let { builder.setGUID(it) }

        if (searchText.isNotEmpty() && searchScopes.contains(SearchScope.MESSAGES)) {
            builder.setSearchKeyword(searchText)
        } else {
            builder.setSearchKeyword(null)
        }

        // Apply message filters
        applyMessageFilters(builder, filters)
    }

    /**
     * Applies message filters to the request builder.
     * Matches the reference Java implementation using AttachmentTypes and hasLinks.
     */
    private fun applyMessageFilters(
        builder: MessagesRequest.MessagesRequestBuilder,
        filters: Set<SearchFilter>
    ) {
        val attachmentTypes = mutableListOf<AttachmentType>()

        if (filters.contains(SearchFilter.PHOTOS)) {
            attachmentTypes.add(AttachmentType.IMAGE)
        }
        if (filters.contains(SearchFilter.VIDEOS)) {
            attachmentTypes.add(AttachmentType.VIDEO)
        }
        if (filters.contains(SearchFilter.DOCUMENTS)) {
            attachmentTypes.add(AttachmentType.FILE)
        }
        if (filters.contains(SearchFilter.AUDIO)) {
            attachmentTypes.add(AttachmentType.AUDIO)
        }

        if (attachmentTypes.isNotEmpty()) {
            builder.setAttachmentTypes(attachmentTypes)
            builder.hasLinks(false)
        } else {
            builder.setAttachmentTypes(null)
        }

        // Handle LINKS filter separately - it overrides attachment types
        if (filters.contains(SearchFilter.LINKS)) {
            builder.setAttachmentTypes(null)
            builder.hasLinks(true)
        } else if (attachmentTypes.isEmpty()) {
            builder.hasLinks(false)
        }

        // Set message types based on filters
        val filteredMessageTypes = getMessageTypesFromFilters(filters)
        if (filteredMessageTypes.isNotEmpty()) {
            builder.setTypes(filteredMessageTypes)
        } else {
            builder.setTypes(null)
        }
    }

    /**
     * Gets message types from the selected filters.
     */
    private fun getMessageTypesFromFilters(filters: Set<SearchFilter>): List<String> {
        val messageTypes = mutableListOf<String>()
        for (filter in filters) {
            when (filter) {
                SearchFilter.PHOTOS -> messageTypes.add(UIKitConstants.MessageType.IMAGE)
                SearchFilter.VIDEOS -> messageTypes.add(UIKitConstants.MessageType.VIDEO)
                SearchFilter.DOCUMENTS -> messageTypes.add(UIKitConstants.MessageType.FILE)
                SearchFilter.AUDIO -> messageTypes.add(UIKitConstants.MessageType.AUDIO)
                SearchFilter.LINKS -> messageTypes.add(UIKitConstants.MessageType.TEXT)
                else -> { /* Not a message filter */ }
            }
        }
        return messageTypes
    }

    /**
     * Updates the final UI state after all pending requests complete.
     */
    private fun updateFinalStateIfCompleted() {
        if (isConversationRequestPending || isMessageRequestPending) return

        _uiState.value = when {
            conversationRequestFailed && messageRequestFailed -> {
                SearchUIState.Error(lastException ?: CometChatException(
                    "SEARCH_ERROR",
                    "Search failed",
                    "Both conversation and message search failed"
                ))
            }
            _conversations.value.isEmpty() && _messages.value.isEmpty() -> SearchUIState.Empty
            else -> SearchUIState.Content
        }
    }

    /**
     * Resets request state tracking flags.
     */
    private fun resetRequestStates() {
        isConversationRequestPending = false
        isMessageRequestPending = false
        conversationRequestFailed = false
        messageRequestFailed = false
        lastException = null
    }

    /**
     * Fetches more conversations for pagination.
     */
    fun fetchMoreConversations() {
        if (!_hasMoreConversations.value || isConversationRequestPending) return

        viewModelScope.launch {
            isConversationRequestPending = true

            currentConversationsRequest?.let { request ->
                fetchConversationsUseCase(request)
                    .onSuccess { conversations ->
                        // Filter out conversations with no last message when UNREAD filter is active
                        val filteredConversations = if (_selectedFilters.value.contains(SearchFilter.UNREAD)) {
                            conversations.filter { it.lastMessage != null }
                        } else {
                            conversations
                        }
                        _conversations.value = _conversations.value + filteredConversations
                        _hasMoreConversations.value = fetchConversationsUseCase.hasMore()
                        isConversationRequestPending = false
                    }
                    .onFailure {
                        isConversationRequestPending = false
                    }
            } ?: run {
                isConversationRequestPending = false
            }
        }
    }

    /**
     * Fetches more messages for pagination.
     */
    fun fetchMoreMessages() {
        if (!_hasMoreMessages.value || isMessageRequestPending) return

        viewModelScope.launch {
            isMessageRequestPending = true

            currentMessagesRequest?.let { request ->
                fetchMessagesUseCase(request)
                    .onSuccess { messages ->
                        // Reverse messages for chronological display
                        _messages.value = _messages.value + messages.reversed()
                        _hasMoreMessages.value = fetchMessagesUseCase.hasMore()
                        isMessageRequestPending = false
                    }
                    .onFailure {
                        isMessageRequestPending = false
                    }
            } ?: run {
                isMessageRequestPending = false
            }
        }
    }

    /**
     * Sets the search scopes to determine which types of data to search.
     *
     * @param scopes List of SearchScope values (MESSAGES, CONVERSATIONS, or both)
     */
    fun setSearchScopes(scopes: List<SearchScope>) {
        searchScopes = scopes
    }

    /**
     * Sets the UID for contextual search within a specific user conversation.
     * When set, only messages will be searched (conversations are ignored).
     *
     * @param uid The user ID to filter messages by, or null to clear
     */
    fun setUid(uid: String?) {
        this.uid = uid
        updateVisibleFiltersForContext()
    }

    /**
     * Sets the GUID for contextual search within a specific group conversation.
     * When set, only messages will be searched (conversations are ignored).
     *
     * @param guid The group ID to filter messages by, or null to clear
     */
    fun setGuid(guid: String?) {
        this.guid = guid
        updateVisibleFiltersForContext()
    }

    /**
     * Updates visible filters based on uid/guid context.
     * When uid or guid is set, conversation filters (GROUPS, UNREAD) are excluded
     * since only message search is applicable in contextual search.
     */
    private fun updateVisibleFiltersForContext() {
        if (uid != null || guid != null) {
            // In contextual search (uid/guid), only message filters are relevant
            _visibleFilters.value = SearchFilter.entries.filter { it.isMessageFilter() }
            // Clear any selected conversation filters that are no longer valid
            val currentFilters = _selectedFilters.value
            val invalidFilters = currentFilters.filter { it.isConversationFilter() }
            if (invalidFilters.isNotEmpty()) {
                _selectedFilters.value = currentFilters - invalidFilters.toSet()
            }
        } else {
            // Global search - show all filters (unless a filter group is already selected)
            if (_selectedFilters.value.isEmpty()) {
                _visibleFilters.value = SearchFilter.entries.toList()
            }
        }
    }

    /**
     * Sets a custom ConversationsRequest builder for customizing conversation fetch parameters.
     *
     * @param builder The custom request builder
     */
    fun setConversationsRequestBuilder(builder: ConversationsRequest.ConversationsRequestBuilder) {
        conversationsRequestBuilder = builder
    }

    /**
     * Sets a custom MessagesRequest builder for customizing message fetch parameters.
     *
     * @param builder The custom request builder
     */
    fun setMessagesRequestBuilder(builder: MessagesRequest.MessagesRequestBuilder) {
        messagesRequestBuilder = builder
    }

    /**
     * Toggles a filter on or off with group-based visibility logic.
     *
     * When clicking a filter from a different group than currently selected filters:
     * 1. Clear all selected filters first
     * 2. Then add the clicked filter
     *
     * When clicking a filter from the same group:
     * - Toggle the filter (add if not selected, remove if selected)
     *
     * After toggling, updates visible filters to show only same-group filters
     * or all filters if no selection.
     *
     * @param filter The filter to toggle
     */
    fun toggleFilter(filter: SearchFilter) {
        val currentFilters = _selectedFilters.value.toMutableSet()
        val clickedGroup = filter.group

        // Check if we have any selected filters from a different group
        val hasFiltersFromDifferentGroup = currentFilters.any { it.group != clickedGroup }

        if (hasFiltersFromDifferentGroup) {
            // Clear all filters when selecting from a new group
            currentFilters.clear()
            currentFilters.add(filter)
        } else {
            // Same group or no filters selected - toggle normally
            if (currentFilters.contains(filter)) {
                currentFilters.remove(filter)
            } else {
                currentFilters.add(filter)
            }
        }

        // Update visible filters based on current selection
        updateVisibleFilters(currentFilters)

        searchConversationsAndMessages(lastSearchText, currentFilters)
    }

    /**
     * Updates the visible filters based on the current selection.
     *
     * - When no filters are selected: show all 7 filters
     * - When filters are selected: show only filters from the same group
     *
     * @param currentFilters The current set of selected filters
     */
    private fun updateVisibleFilters(currentFilters: Set<SearchFilter>) {
        if (currentFilters.isEmpty()) {
            // No selection - show all filters
            _visibleFilters.value = SearchFilter.entries.toList()
        } else {
            // Get the group of the selected filters (all should be same group)
            val selectedGroup = currentFilters.first().group
            _visibleFilters.value = SearchFilter.getFiltersInGroup(selectedGroup)
        }
    }

    /**
     * Clears all search state including lists, filters, and request state.
     */
    fun clear() {
        searchJob?.cancel()
        clearLists()
        _selectedFilters.value = emptySet()
        _visibleFilters.value = SearchFilter.entries.toList()
        resetRequestStates()
        currentConversationsRequest = null
        currentMessagesRequest = null
        lastSearchText = ""
        lastFilters = emptySet()
    }

    /**
     * Clears the conversation and message lists and resets pagination flags.
     */
    private fun clearLists() {
        _conversations.value = emptyList()
        _messages.value = emptyList()
        _hasMoreConversations.value = true
        _hasMoreMessages.value = true
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
