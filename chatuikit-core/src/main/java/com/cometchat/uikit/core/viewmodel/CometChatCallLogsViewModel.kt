package com.cometchat.uikit.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.uikit.core.domain.usecase.FetchCallLogsUseCase
import com.cometchat.uikit.core.domain.usecase.InitiateCallUseCase
import com.cometchat.uikit.core.events.CometChatCallEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.state.CallLogsUIState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing call logs in the CometChat application.
 * Uses StateFlow for state management and SharedFlow for events.
 * Implements [ListOperations] for standardized list manipulation.
 * 
 * @param fetchCallLogsUseCase Use case for fetching call logs
 * @param initiateCallUseCase Use case for initiating calls
 * @param enableListeners Whether to enable CometChat listeners (set to false for testing)
 */
open class CometChatCallLogsViewModel(
    private val fetchCallLogsUseCase: FetchCallLogsUseCase,
    private val initiateCallUseCase: InitiateCallUseCase,
    private val enableListeners: Boolean = true
) : ViewModel(), ListOperations<CallLog> {
    
    // UI State
    private val _uiState = MutableStateFlow<CallLogsUIState>(CallLogsUIState.Loading)
    val uiState: StateFlow<CallLogsUIState> = _uiState.asStateFlow()
    
    // Call logs list
    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()
    
    // Initiated call event - emitted when a call is successfully initiated
    private val _initiatedCall = MutableSharedFlow<Call>()
    val initiatedCall: SharedFlow<Call> = _initiatedCall.asSharedFlow()
    
    // Scroll to top event - emits when list should scroll to top
    private val _scrollToTopEvent = MutableSharedFlow<Unit>()
    val scrollToTopEvent: SharedFlow<Unit> = _scrollToTopEvent.asSharedFlow()
    
    // Index-based events for RecyclerView adapter notifications (chatuikit-kotlin compatibility)
    // Insert at top event - emits index 0 when item added to top
    private val _insertAtTopEvent = MutableSharedFlow<Int>()
    val insertAtTopEvent: SharedFlow<Int> = _insertAtTopEvent.asSharedFlow()
    
    // Move to top event - emits old index when item moved to top
    private val _moveToTopEvent = MutableSharedFlow<Int>()
    val moveToTopEvent: SharedFlow<Int> = _moveToTopEvent.asSharedFlow()
    
    // Update call event - emits index of updated item
    private val _updateCallEvent = MutableSharedFlow<Int>()
    val updateCallEvent: SharedFlow<Int> = _updateCallEvent.asSharedFlow()
    
    // Remove call event - emits index of removed item
    private val _removeCallEvent = MutableSharedFlow<Int>()
    val removeCallEvent: SharedFlow<Int> = _removeCallEvent.asSharedFlow()
    
    // List operations delegate
    // Using object equality (equals) like the original Java implementation
    private val listDelegate = ListOperationsDelegate(
        stateFlow = _callLogs,
        equalityChecker = { a, b -> a == b }
    )
    
    // Fetching state - prevents concurrent fetches
    private var isFetching = false
    
    // Has more data flag - tracks if there are more call logs to fetch
    private var hasMoreData = true
    
    // Request configuration
    private var callLogRequest: CallLogRequest? = null
    private var callLogRequestBuilder: CallLogRequest.CallLogRequestBuilder? = null
    private var limit = 30
    
    // Listener tag for unique identification
    private var listenersTag: String? = null
    
    init {
        if (enableListeners) {
            addListeners()
        }
        fetchCallLogs()
    }
    
    /**
     * Fetches call logs with pagination support.
     * Shows loading state only on initial fetch.
     * Uses client's request builder if set, otherwise creates a default one.
     * Prevents concurrent fetches using isFetching flag.
     */
    fun fetchCallLogs() {
        // Prevent concurrent fetches and don't fetch if no more data
        if (isFetching || !hasMoreData) return
        
        viewModelScope.launch {
            isFetching = true
            
            if (_callLogs.value.isEmpty()) {
                _uiState.value = CallLogsUIState.Loading
            }
            
            // Create request on first fetch, reuse for pagination
            if (callLogRequest == null) {
                val builder = callLogRequestBuilder 
                    ?: CallLogRequest.CallLogRequestBuilder()
                        .setLimit(limit)
                        .setCallCategory(CometChatCallsConstants.CALL_CATEGORY_CALL)
                        .setAuthToken(CometChat.getUserAuthToken())
                callLogRequest = builder.build()
            }
            
            callLogRequest?.let { request ->
                fetchCallLogsUseCase(request)
                    .onSuccess { newCallLogs ->
                        if (newCallLogs.isNotEmpty()) {
                            val updatedList = _callLogs.value + newCallLogs
                            _callLogs.value = updatedList
                            _uiState.value = CallLogsUIState.Content(updatedList)
                        } else {
                            hasMoreData = false
                            if (_callLogs.value.isEmpty()) {
                                _uiState.value = CallLogsUIState.Empty
                            }
                        }
                    }
                    .onFailure { exception ->
                        _uiState.value = CallLogsUIState.Error(exception as CometChatException)
                    }
                
                isFetching = false
            } ?: run {
                isFetching = false
            }
        }
    }
    
    /**
     * Refreshes the call logs list from the beginning.
     * Clears existing data and fetches fresh.
     */
    fun refreshCallLogs() {
        hasMoreData = true
        isFetching = false
        callLogRequest = null
        
        viewModelScope.launch {
            if (_callLogs.value.isEmpty()) {
                _uiState.value = CallLogsUIState.Loading
            }
            
            val builder = callLogRequestBuilder 
                ?: CallLogRequest.CallLogRequestBuilder()
                    .setLimit(limit)
                    .setCallCategory(CometChatCallsConstants.CALL_CATEGORY_CALL)
                    .setAuthToken(CometChat.getUserAuthToken())
            
            callLogRequest = builder.build()
            
            callLogRequest?.let { request ->
                fetchCallLogsUseCase(request)
                    .onSuccess { callLogs ->
                        _callLogs.value = callLogs
                        _uiState.value = if (callLogs.isEmpty()) {
                            CallLogsUIState.Empty
                        } else {
                            CallLogsUIState.Content(callLogs)
                        }
                        _scrollToTopEvent.emit(Unit)
                    }
                    .onFailure { exception ->
                        if (_callLogs.value.isEmpty()) {
                            _uiState.value = CallLogsUIState.Error(exception as CometChatException)
                        }
                    }
            }
        }
    }
    
    /**
     * Initiates a call based on the call log.
     * Determines the receiver automatically based on who initiated the original call.
     * Uses the same call type as the original call from the call log.
     * 
     * @param callLog The call log to base the new call on
     */
    fun initiateCall(callLog: CallLog) {
        // Determine call type from the call log
        val callType = callLog.type ?: CometChatCallsConstants.CALL_TYPE_AUDIO
        initiateCall(callLog, callType)
    }
    
    /**
     * Initiates a call based on the call log with a specific call type.
     * Determines the receiver automatically based on who initiated the original call.
     * 
     * @param callLog The call log to base the new call on
     * @param callType The type of call to initiate (audio/video)
     */
    fun initiateCall(callLog: CallLog, callType: String) {
        viewModelScope.launch {
            initiateCallUseCase(callLog, callType)
                .onSuccess { call ->
                    _initiatedCall.emit(call)
                    // Emit outgoing call event
                    CometChatEvents.emitCallEvent(CometChatCallEvent.OutgoingCall(call))
                }
                .onFailure { exception ->
                    _uiState.value = CallLogsUIState.Error(exception as CometChatException)
                }
        }
    }
    
    /**
     * Sets the CallLogRequestBuilder for customizing fetch parameters.
     * Ensures call category is set to CALL_CATEGORY_CALL.
     * 
     * @param builder The custom request builder
     */
    fun setCallLogRequestBuilder(builder: CallLogRequest.CallLogRequestBuilder) {
        callLogRequestBuilder = builder
            .setCallCategory(CometChatCallsConstants.CALL_CATEGORY_CALL)
            .setAuthToken(CometChat.getUserAuthToken())
        callLogRequest = callLogRequestBuilder?.build()
    }
    
    /**
     * Sets the limit for the number of call logs to fetch per page.
     * 
     * @param limit The maximum number of call logs per fetch
     */
    fun setLimit(limit: Int) {
        this.limit = limit
    }
    
    /**
     * Clears all call logs from the list.
     */
    fun clear() {
        _callLogs.value = emptyList()
        _uiState.value = CallLogsUIState.Empty
    }
    
    // ==================== Index-based list operations ====================
    // These methods emit index events for RecyclerView adapter notifications
    // (chatuikit-kotlin XML views compatibility)
    
    /**
     * Adds a call log to the top of the list and emits insert event.
     * If the call log already exists, it will not be added.
     * 
     * @param callLog The call log to add
     */
    fun addToTop(callLog: CallLog) {
        val currentList = _callLogs.value
        // Check if already exists using equals
        val exists = currentList.contains(callLog)
        if (!exists) {
            _callLogs.value = listOf(callLog) + currentList
            updateUIStateFromList()
            viewModelScope.launch {
                _insertAtTopEvent.emit(0)
            }
        }
    }
    
    /**
     * Moves an existing call log to the top and emits move event with old index.
     * If the call log does not exist, no action is taken and no event is emitted.
     * 
     * @param callLog The call log to move
     */
    fun moveToTop(callLog: CallLog) {
        val currentList = _callLogs.value.toMutableList()
        val oldIndex = currentList.indexOf(callLog)
        if (oldIndex > 0) {
            // Only move if not already at top
            val item = currentList.removeAt(oldIndex)
            currentList.add(0, item)
            _callLogs.value = currentList
            viewModelScope.launch {
                _moveToTopEvent.emit(oldIndex)
                _scrollToTopEvent.emit(Unit)
            }
        }
    }
    
    /**
     * Updates a call log in the list and emits update event with index.
     * If the call log does not exist, no action is taken and no event is emitted.
     * 
     * @param callLog The call log to update
     */
    fun updateCall(callLog: CallLog) {
        val currentList = _callLogs.value.toMutableList()
        val index = currentList.indexOf(callLog)
        if (index >= 0) {
            currentList[index] = callLog
            _callLogs.value = currentList
            updateUIStateFromList()
            viewModelScope.launch {
                _updateCallEvent.emit(index)
            }
        }
    }
    
    /**
     * Removes a call log from the list and emits remove event with index.
     * If the call log does not exist, no action is taken and no event is emitted.
     * Updates UI state to Empty if list becomes empty.
     * 
     * @param callLog The call log to remove
     */
    fun removeCall(callLog: CallLog) {
        val currentList = _callLogs.value.toMutableList()
        val index = currentList.indexOf(callLog)
        if (index >= 0) {
            currentList.removeAt(index)
            _callLogs.value = currentList
            updateUIStateFromList()
            viewModelScope.launch {
                _removeCallEvent.emit(index)
            }
        }
    }
    
    // ListOperations implementation
    
    override fun addItem(item: CallLog) = listDelegate.addItem(item)
    
    override fun addItems(items: List<CallLog>) = listDelegate.addItems(items)
    
    override fun removeItem(item: CallLog): Boolean {
        val result = listDelegate.removeItem(item)
        updateUIStateFromList()
        return result
    }
    
    override fun removeItemAt(index: Int): CallLog? {
        val result = listDelegate.removeItemAt(index)
        updateUIStateFromList()
        return result
    }
    
    override fun updateItem(item: CallLog, predicate: (CallLog) -> Boolean): Boolean {
        val result = listDelegate.updateItem(item, predicate)
        updateUIStateFromList()
        return result
    }
    
    override fun clearItems() {
        listDelegate.clearItems()
        _uiState.value = CallLogsUIState.Empty
    }
    
    override fun getItems(): List<CallLog> = listDelegate.getItems()
    
    override fun getItemAt(index: Int): CallLog? = listDelegate.getItemAt(index)
    
    override fun getItemCount(): Int = listDelegate.getItemCount()
    
    override fun moveItemToTop(item: CallLog) {
        listDelegate.moveItemToTop(item)
        viewModelScope.launch {
            _scrollToTopEvent.emit(Unit)
        }
    }
    
    override fun batch(operations: ListOperationsBatchScope<CallLog>.() -> Unit) {
        listDelegate.batch(operations)
        updateUIStateFromList()
    }
    
    /**
     * Updates the UI state based on the current list contents.
     */
    private fun updateUIStateFromList() {
        val currentList = _callLogs.value
        _uiState.value = if (currentList.isEmpty()) {
            CallLogsUIState.Empty
        } else {
            CallLogsUIState.Content(currentList)
        }
    }
    
    // ==================== Real-time listeners ====================
    
    /**
     * Adds CometChat listeners for real-time call updates.
     * Called during initialization if enableListeners is true.
     */
    private fun addListeners() {
        listenersTag = "CallLogs_${System.currentTimeMillis()}"
        
        listenersTag?.let { tag ->
            // Call listener for real-time call events
            CometChat.addCallListener(tag, object : CometChat.CallListener() {
                override fun onIncomingCallReceived(call: Call) {
                    handleCallEvent(call)
                }
                
                override fun onOutgoingCallAccepted(call: Call) {
                    handleCallEvent(call)
                }
                
                override fun onOutgoingCallRejected(call: Call) {
                    handleCallEvent(call)
                }
                
                override fun onIncomingCallCancelled(call: Call) {
                    handleCallEvent(call)
                }
                
                override fun onCallEndedMessageReceived(call: Call) {
                    handleCallEvent(call)
                }
            })
            
            // Connection listener for reconnection - refresh list when reconnected
            CometChat.addConnectionListener(tag, object : CometChat.ConnectionListener {
                override fun onConnected() {
                    refreshCallLogs()
                }
                
                override fun onConnecting() {}
                override fun onDisconnected() {}
                override fun onFeatureThrottled() {}
                override fun onConnectionError(error: com.cometchat.chat.exceptions.CometChatException?) {}
            })
        }
    }
    
    /**
     * Removes all CometChat listeners.
     * Called when ViewModel is cleared.
     */
    private fun removeListeners() {
        listenersTag?.let { tag ->
            CometChat.removeCallListener(tag)
            CometChat.removeConnectionListener(tag)
        }
    }
    
    /**
     * Handles incoming call events by refreshing the call logs list.
     * Since Call and CallLog are different types, we refresh to get the latest data.
     * 
     * @param call The call event received
     */
    private fun handleCallEvent(call: Call) {
        // Refresh the call logs to get the latest data
        // This ensures we have the proper CallLog format from the server
        viewModelScope.launch {
            refreshCallLogs()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        if (enableListeners) {
            removeListeners()
        }
    }
}
