package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.UsersRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants
import com.cometchat.uikit.core.domain.usecase.FetchUsersUseCase
import com.cometchat.uikit.core.domain.usecase.SearchUsersUseCase
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatUserEvent
import com.cometchat.uikit.core.state.UsersUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that holds UI state and calls use cases for Users.
 * Does NOT fetch data directly - delegates to use cases.
 * 
 * Implements [ListOperations] interface for standardized list manipulation.
 * All list operation methods are open for client override.
 * 
 * @param fetchUsersUseCase Use case for fetching users
 * @param searchUsersUseCase Use case for searching users
 * @param enableListeners Whether to enable CometChat listeners (set to false for testing)
 */
open class CometChatUsersViewModel(
    private val fetchUsersUseCase: FetchUsersUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val enableListeners: Boolean = true
) : ViewModel(), ListOperations<User> {
    
    // UI State
    private val _uiState = MutableStateFlow<UsersUIState>(UsersUIState.Loading)
    val uiState: StateFlow<UsersUIState> = _uiState.asStateFlow()
    
    // Users list
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    // Fetching state - prevents concurrent fetches
    private var isFetching = false
    
    // Has more data flag - tracks if there are more users to fetch
    private var hasMoreData = true
    
    // List operations delegate - handles internal list manipulation
    private val listDelegate = ListOperationsDelegate(
        stateFlow = _users,
        equalityChecker = { a, b -> a.uid == b.uid }
    )
    
    // Selection state
    private val _selectedUsers = MutableStateFlow<Set<User>>(emptySet())
    val selectedUsers: StateFlow<Set<User>> = _selectedUsers.asStateFlow()
    
    // Scroll to top event - emits when list should scroll to top
    private val _scrollToTopEvent = MutableSharedFlow<Unit>()
    val scrollToTopEvent: SharedFlow<Unit> = _scrollToTopEvent.asSharedFlow()
    
    // Configuration
    private var usersRequest: UsersRequest? = null
    private var usersRequestBuilder: UsersRequest.UsersRequestBuilder? = null
    private var searchUsersRequestBuilder: UsersRequest.UsersRequestBuilder? = null
    private var listenersTag: String? = null
    private val limit = 30
    
    // Local event listener jobs
    private var userEventsJob: Job? = null
    
    // Search debounce job - cancels pending search when new search is initiated
    private var searchJob: Job? = null
    
    init {
        if (enableListeners) {
            addListeners()
        }
        fetchUsers()
    }
    
    /**
     * Fetches users with pagination support.
     * Shows loading state only on initial fetch.
     * Uses client's request builder if set, otherwise creates a default one.
     * Prevents concurrent fetches using isFetching flag.
     */
    fun fetchUsers() {
        // Prevent concurrent fetches and don't fetch if no more data
        if (isFetching || !hasMoreData) return
        
        // Set isFetching BEFORE launching coroutine to prevent race conditions
        isFetching = true
        
        viewModelScope.launch {
            if (_users.value.isEmpty()) {
                _uiState.value = UsersUIState.Loading
            }
            
            // Create request on first fetch, reuse for pagination
            // Use client's builder if provided, otherwise create default
            if (usersRequest == null) {
                val builder = usersRequestBuilder 
                    ?: UsersRequest.UsersRequestBuilder().setLimit(limit)
                usersRequest = builder.build()
            }
            
            usersRequest?.let { request ->
                fetchUsersUseCase(request)
                    .onSuccess { newUsers ->
                        // Only append if we got new users
                        if (newUsers.isNotEmpty()) {
                            addList(newUsers)
                            _uiState.value = UsersUIState.Content
                        } else {
                            // No new users means we've reached the end
                            hasMoreData = false
                            if (_users.value.isEmpty()) {
                                _uiState.value = UsersUIState.Empty
                            }
                        }
                    }
                    .onFailure { exception ->
                        if (_users.value.isEmpty()) {
                            _uiState.value = UsersUIState.Error(exception as CometChatException)
                        }
                    }
                
                isFetching = false
            } ?: run {
                isFetching = false
            }
        }
    }
    
    /**
     * Refreshes the user list from the beginning.
     * Clears existing data and fetches fresh.
     */
    fun refreshList() {
        // Reset pagination state for fresh fetch
        hasMoreData = true
        isFetching = false
        
        viewModelScope.launch {
            if (_users.value.isEmpty()) {
                _uiState.value = UsersUIState.Loading
            }
            
            // Use client's builder if provided, otherwise create default
            val builder = usersRequestBuilder 
                ?: UsersRequest.UsersRequestBuilder().setLimit(limit)
            usersRequest = builder.build()
            
            clearItems()
            
            usersRequest?.let { request ->
                fetchUsersUseCase(request)
                    .onSuccess { users ->
                        addList(users)
                        _uiState.value = if (users.isEmpty()) {
                            UsersUIState.Empty
                        } else {
                            UsersUIState.Content
                        }
                        
                        // Emit scroll to top event after refresh
                        _scrollToTopEvent.emit(Unit)
                    }
                    .onFailure { exception ->
                        if (_users.value.isEmpty()) {
                            _uiState.value = UsersUIState.Error(exception as CometChatException)
                        }
                    }
            }
        }
    }
    
    /**
     * Searches for users based on the specified keyword.
     * Implements seamless search experience with:
     * - Debouncing (300ms) to reduce API calls during rapid typing
     * - Keeps existing content visible during search (no flicker)
     * - Conditional loading state (only when list is empty)
     * - Atomic list replacement when results arrive
     *
     * @param keyword The search keyword (null or empty to reset)
     */
    fun searchUsers(keyword: String?) {
        // Cancel any pending search job (debouncing)
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            // Reset pagination state for new search
            hasMoreData = true
            
            if (keyword.isNullOrEmpty()) {
                // Reset to normal fetch - clear list and fetch fresh
                _users.value = emptyList()
                usersRequest = usersRequestBuilder?.build() 
                    ?: UsersRequest.UsersRequestBuilder().setLimit(limit).build()
                isFetching = false
                fetchUsers()
                return@launch
            }
            
            // Debounce: wait 300ms before making API call
            // This reduces unnecessary API calls during rapid typing
            delay(300)
            
            // Only show loading state if list is currently empty AND we're not already showing empty state
            // This prevents flicker when:
            // 1. Content is already displayed (list not empty)
            // 2. Empty state is already displayed (previous search had no results)
            if (_users.value.isEmpty() && _uiState.value !is UsersUIState.Empty) {
                _uiState.value = UsersUIState.Loading
            }
            
            val builder = searchUsersRequestBuilder ?: usersRequestBuilder 
                ?: UsersRequest.UsersRequestBuilder().setLimit(limit)
            
            searchUsersUseCase(keyword, builder)
                .onSuccess { users ->
                    // Atomic list replacement - replace entire list at once
                    // This prevents flicker from clear-then-add pattern
                    _users.value = users
                    
                    // Direct state transition: Content → Empty without Loading in between
                    _uiState.value = if (users.isEmpty()) {
                        UsersUIState.Empty
                    } else {
                        UsersUIState.Content
                    }
                    
                    // Update request for pagination
                    usersRequest = builder.setSearchKeyword(keyword).build()
                }
                .onFailure { exception ->
                    // Only show error if we don't have existing content
                    // This provides graceful degradation - keep showing old results on error
                    if (_users.value.isEmpty()) {
                        _uiState.value = UsersUIState.Error(exception as CometChatException)
                    }
                }
        }
    }
    
    /**
     * Selects or deselects a user based on selection mode.
     */
    fun selectUser(user: User, mode: UIKitConstants.SelectionMode) {
        when (mode) {
            UIKitConstants.SelectionMode.SINGLE -> {
                _selectedUsers.value = setOf(user)
            }
            UIKitConstants.SelectionMode.MULTIPLE -> {
                val current = _selectedUsers.value.toMutableSet()
                if (current.any { it.uid == user.uid }) {
                    current.removeAll { it.uid == user.uid }
                } else {
                    current.add(user)
                }
                _selectedUsers.value = current
            }
            UIKitConstants.SelectionMode.NONE -> {
                // Do nothing
            }
        }
    }
    
    /**
     * Clears all selected users.
     */
    fun clearSelection() {
        _selectedUsers.value = emptySet()
    }
    
    /**
     * Returns the list of currently selected users.
     */
    fun getSelectedUsers(): List<User> = _selectedUsers.value.toList()
    
    /**
     * Checks if a user is selected.
     */
    fun isSelected(user: User): Boolean = 
        _selectedUsers.value.any { it.uid == user.uid }
    
    /**
     * Sets the users request builder for customizing fetch parameters.
     *
     * @param builder The custom request builder provided by the client
     */
    fun setUsersRequestBuilder(builder: UsersRequest.UsersRequestBuilder) {
        usersRequestBuilder = builder
        usersRequest = builder.build()
    }
    
    /**
     * Sets the search request builder for customizing search parameters.
     *
     * @param builder The custom request builder for search
     */
    fun setSearchRequestBuilder(builder: UsersRequest.UsersRequestBuilder?) {
        searchUsersRequestBuilder = builder
    }
    
    /**
     * Adds a list of users to the user list.
     * Updates existing users if they already exist.
     *
     * @param userList The list of users to add
     */
    private fun addList(userList: List<User>) {
        val currentList = _users.value.toMutableList()
        for (user in userList) {
            val index = currentList.indexOfFirst { it.uid == user.uid }
            if (index >= 0) {
                currentList[index] = user
            } else {
                currentList.add(user)
            }
        }
        _users.value = currentList
    }
    
    /**
     * Moves a user to the top of the list.
     */
    fun moveUserToTop(user: User) {
        val currentList = _users.value.toMutableList()
        val index = currentList.indexOfFirst { it.uid == user.uid }
        if (index >= 0) {
            currentList.removeAt(index)
            currentList.add(0, user)
            _users.value = currentList
            
            viewModelScope.launch {
                _scrollToTopEvent.emit(Unit)
            }
        }
    }
    
    /**
     * Updates a user in the list.
     */
    fun updateUser(user: User) {
        val currentList = _users.value.toMutableList()
        val index = currentList.indexOfFirst { it.uid == user.uid }
        if (index >= 0) {
            currentList[index] = user
            _users.value = currentList
        }
    }
    
    /**
     * Removes a user from the list.
     */
    fun removeUser(user: User) {
        val currentList = _users.value.toMutableList()
        val index = currentList.indexOfFirst { it.uid == user.uid }
        if (index >= 0) {
            currentList.removeAt(index)
            _users.value = currentList
            
            if (currentList.isEmpty()) {
                _uiState.value = UsersUIState.Empty
            }
        }
    }
    
    /**
     * Adds a user to the top of the list.
     */
    fun addUserToTop(user: User) {
        val currentList = _users.value.toMutableList()
        if (!currentList.any { it.uid == user.uid }) {
            currentList.add(0, user)
            _users.value = currentList
            
            viewModelScope.launch {
                _scrollToTopEvent.emit(Unit)
            }
        }
    }
    
    // ==================== ListOperations Implementation ====================
    
    override fun addItem(item: User) = listDelegate.addItem(item)
    
    override fun addItems(items: List<User>) = listDelegate.addItems(items)
    
    override fun removeItem(item: User): Boolean = listDelegate.removeItem(item)
    
    override fun removeItemAt(index: Int): User? = listDelegate.removeItemAt(index)
    
    override fun updateItem(item: User, predicate: (User) -> Boolean): Boolean = 
        listDelegate.updateItem(item, predicate)
    
    override fun clearItems() = listDelegate.clearItems()
    
    override fun getItems(): List<User> = listDelegate.getItems()
    
    override fun getItemAt(index: Int): User? = listDelegate.getItemAt(index)
    
    override fun getItemCount(): Int = listDelegate.getItemCount()
    
    override fun moveItemToTop(item: User) = listDelegate.moveItemToTop(item)
    
    override fun batch(operations: ListOperationsBatchScope<User>.() -> Unit) = 
        listDelegate.batch(operations)
    
    // ==================== Listeners ====================
    
    /**
     * Adds CometChat listeners for real-time updates.
     */
    private fun addListeners() {
        listenersTag = "UsersList_${System.currentTimeMillis()}"
        
        listenersTag?.let { tag ->
            // User listener for online/offline status
            CometChat.addUserListener(tag, object : CometChat.UserListener() {
                override fun onUserOnline(user: User) {
                    if (!isBlocked(user)) {
                        moveUserToTop(user)
                    }
                }
                
                override fun onUserOffline(user: User) {
                    if (!isBlocked(user)) {
                        updateUser(user)
                    }
                }
            })
            
            // Connection listener for reconnection
            CometChat.addConnectionListener(tag, object : CometChat.ConnectionListener {
                override fun onConnected() {
                    refreshList()
                }
                
                override fun onConnecting() {
                    // No action needed
                }
                
                override fun onDisconnected() {
                    // No action needed
                }
                
                override fun onFeatureThrottled() {
                    // No action needed
                }
                
                override fun onConnectionError(error: CometChatException?) {
                    // No action needed
                }
            })
        }
        
        // Add local event listeners
        addLocalEventListeners()
    }
    
    /**
     * Adds local event listeners for UI-triggered events.
     */
    private fun addLocalEventListeners() {
        userEventsJob = viewModelScope.launch {
            CometChatEvents.userEvents.collect { event ->
                when (event) {
                    is CometChatUserEvent.UserBlocked -> {
                        updateUser(event.user)
                    }
                    is CometChatUserEvent.UserUnblocked -> {
                        updateUser(event.user)
                    }
                }
            }
        }
    }
    
    /**
     * Removes all CometChat listeners.
     */
    private fun removeListeners() {
        listenersTag?.let { tag ->
            CometChat.removeUserListener(tag)
            CometChat.removeConnectionListener(tag)
        }
        userEventsJob?.cancel()
    }
    
    /**
     * Checks if a user is blocked.
     */
    private fun isBlocked(user: User): Boolean {
        return user.isBlockedByMe || user.isHasBlockedMe
    }
    
    override fun onCleared() {
        super.onCleared()
        if (enableListeners) {
            removeListeners()
        }
        searchJob?.cancel()
        listDelegate.cancel()
    }
}
