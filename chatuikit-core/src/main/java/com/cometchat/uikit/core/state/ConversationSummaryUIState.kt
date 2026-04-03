package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * Represents the UI state for conversation summary feature.
 *
 * This sealed class is used by the ViewModel to communicate the current
 * state of conversation summary fetching to the UI layer.
 *
 * @see com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
 */
sealed class ConversationSummaryUIState {
    
    /**
     * Idle state - no conversation summary request in progress or available.
     *
     * This is the initial state and the state after dismissing the summary.
     */
    object Idle : ConversationSummaryUIState()
    
    /**
     * Loading state - conversation summary is being fetched.
     *
     * The UI should display a loading indicator while in this state.
     */
    object Loading : ConversationSummaryUIState()
    
    /**
     * Loaded state - conversation summary has been loaded successfully.
     *
     * @param summary The AI-generated conversation summary text.
     */
    data class Loaded(val summary: String) : ConversationSummaryUIState()
    
    /**
     * Error state - an error occurred while fetching conversation summary.
     *
     * @param exception The exception that caused the error.
     */
    data class Error(val exception: CometChatException) : ConversationSummaryUIState()
}
