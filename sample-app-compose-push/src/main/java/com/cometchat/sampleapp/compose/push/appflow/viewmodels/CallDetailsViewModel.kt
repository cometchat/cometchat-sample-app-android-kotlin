package com.cometchat.sampleapp.compose.push.appflow.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.calls.constants.CometChatCallsConstants
import com.cometchat.calls.core.CallLogRequest
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog
import com.cometchat.calls.model.CallUser
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.models.User
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
 * ViewModel for call details screen.
 * Handles user fetching and call history fetching.
 */
class CallDetailsViewModel : ViewModel() {
    
    // User state
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    // History UI State
    private val _historyState = MutableStateFlow<CallHistoryUIState>(CallHistoryUIState.Loading)
    val historyState: StateFlow<CallHistoryUIState> = _historyState.asStateFlow()
    
    // Call logs list
    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()
    
    private var callLogRequest: CallLogRequest? = null
    private var isFetching = false
    private var hasMore = true
    private val callLogsList = mutableListOf<CallLog>()
    
    /**
     * Initializes the ViewModel with the call log.
     * Fetches the user details and sets up the history request.
     */
    fun initialize(callLog: CallLog) {
        val initiator = callLog.initiator as? CallUser
        val isLoggedInUser = CometChatUIKit.getLoggedInUser()?.uid == initiator?.uid
        val callUser = if (isLoggedInUser) callLog.receiver as? CallUser else initiator
        
        // Fetch user details
        callUser?.uid?.let { uid ->
            CometChat.getUser(uid, object : CometChat.CallbackListener<User>() {
                override fun onSuccess(fetchedUser: User) {
                    _user.value = fetchedUser
                }
                
                override fun onError(e: com.cometchat.chat.exceptions.CometChatException) {
                    // User fetch failed, continue without user details
                }
            })
        }
        
        // Setup history request
        setupHistoryRequest(callUser)
    }
    
    private fun setupHistoryRequest(callUser: CallUser?) {
        callUser?.uid?.let { uid ->
            callLogRequest = CallLogRequest.CallLogRequestBuilder()
                .setUid(uid)
                .setLimit(30)
                .setCallCategory(CometChatCallsConstants.CALL_CATEGORY_CALL)
                .build()
            
            // Reset state
            callLogsList.clear()
            hasMore = true
            isFetching = false
            _callLogs.value = emptyList()
            _historyState.value = CallHistoryUIState.Loading
        }
    }
    
    /**
     * Fetches call history with pagination support.
     */
    fun fetchCallHistory() {
        if (isFetching || !hasMore || callLogRequest == null) return
        
        isFetching = true
        
        viewModelScope.launch {
            callLogRequest?.fetchNext(object : CometChatCalls.CallbackListener<List<CallLog>>() {
                override fun onSuccess(list: List<CallLog>) {
                    isFetching = false
                    if (list.isEmpty()) {
                        hasMore = false
                        if (callLogsList.isEmpty()) {
                            _historyState.value = CallHistoryUIState.Empty
                        }
                    } else {
                        callLogsList.addAll(list)
                        _callLogs.value = callLogsList.toList()
                        _historyState.value = CallHistoryUIState.Content(callLogsList.toList())
                    }
                }
                
                override fun onError(e: CometChatException) {
                    isFetching = false
                    _historyState.value = CallHistoryUIState.Error(e)
                }
            })
        }
    }
    
    /**
     * Checks if the given CallUser is the logged-in user.
     */
    fun isLoggedInUser(user: CallUser?): Boolean {
        return CometChatUIKit.getLoggedInUser()?.uid == (user?.uid ?: "")
    }
}
