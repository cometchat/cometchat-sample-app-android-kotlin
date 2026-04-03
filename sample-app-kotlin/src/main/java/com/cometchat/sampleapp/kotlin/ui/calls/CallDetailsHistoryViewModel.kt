package com.cometchat.sampleapp.kotlin.ui.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.core.CometChat
import com.cometchat.uikit.core.CometChatUIKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class representing the UI state for call history
 */
sealed class CallHistoryUIState {
    object Loading : CallHistoryUIState()
    data class Content(val callLogs: List<CallLog>) : CallHistoryUIState()
    object Empty : CallHistoryUIState()
    data class Error(val exception: CometChatException) : CallHistoryUIState()
}

/**
 * ViewModel for call details history tab.
 * Custom implementation that doesn't auto-fetch on init, allowing proper
 * configuration of the call log request before fetching.
 *
 * Validates: Requirements 2.7, 2.8
 */
class CallDetailsHistoryViewModel : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow<CallHistoryUIState>(CallHistoryUIState.Loading)
    val uiState: StateFlow<CallHistoryUIState> = _uiState.asStateFlow()
    
    // Call logs list
    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()
    
    private var callLogRequest: CallLogRequest? = null
    private var isFetching = false
    private var hasMore = true
    private val callLogsList = mutableListOf<CallLog>()
    
    /**
     * Sets up the call log request builder based on the provided call log.
     * Determines the user to filter by based on whether the logged-in user
     * was the initiator or receiver of the call.
     */
    fun setCallLog(callLog: CallLog) {
        val initiator = callLog.initiator as CallUser
        val isLoggedInUser = CometChatUIKit.getLoggedInUser()?.uid == initiator.uid
        val user = if (isLoggedInUser) callLog.receiver as CallUser else initiator
        
        callLogRequest = CallLogRequest.CallLogRequestBuilder()
            .setUid(user.uid)
            .setLimit(30)
            .setCallCategory(CometChatCallsConstants.CALL_CATEGORY_CALL)
            .setAuthToken(CometChat.getUserAuthToken())
            .build()
        
        // Reset state for new user
        callLogsList.clear()
        hasMore = true
        isFetching = false
        _callLogs.value = emptyList()
        _uiState.value = CallHistoryUIState.Loading
    }
    
    /**
     * Fetches call logs with pagination support.
     */
    fun fetchCallLogs() {
        if (isFetching || !hasMore || callLogRequest == null) return
        
        isFetching = true
        
        viewModelScope.launch {
            callLogRequest?.fetchNext(object : CometChatCalls.CallbackListener<List<CallLog>>() {
                override fun onSuccess(list: List<CallLog>) {
                    isFetching = false
                    if (list.isEmpty()) {
                        hasMore = false
                        if (callLogsList.isEmpty()) {
                            _uiState.value = CallHistoryUIState.Empty
                        }
                    } else {
                        callLogsList.addAll(list)
                        _callLogs.value = callLogsList.toList()
                        _uiState.value = CallHistoryUIState.Content(callLogsList.toList())
                    }
                }
                
                override fun onError(e: CometChatException) {
                    isFetching = false
                    _uiState.value = CallHistoryUIState.Error(e)
                }
            })
        }
    }
}
