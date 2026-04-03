package com.cometchat.uikit.core.state

import com.cometchat.calls.exceptions.CometChatException
import com.cometchat.calls.model.CallLog

/**
 * Sealed class representing UI states for the call logs screen.
 * Used by the ViewModel to communicate current state to the UI.
 * 
 * Follows the same pattern as UIState for ConversationList but is specific
 * to the CallLogs component to maintain separation of concerns.
 */
sealed class CallLogsUIState {
    /**
     * Loading state - displayed while fetching call logs.
     */
    object Loading : CallLogsUIState()
    
    /**
     * Empty state - displayed when no call logs exist.
     */
    object Empty : CallLogsUIState()
    
    /**
     * Error state - displayed when fetching fails.
     * @param exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : CallLogsUIState()
    
    /**
     * Content state - displayed when call logs are available.
     * @param callLogs The list of call logs to display
     */
    data class Content(val callLogs: List<CallLog>) : CallLogsUIState()
}
