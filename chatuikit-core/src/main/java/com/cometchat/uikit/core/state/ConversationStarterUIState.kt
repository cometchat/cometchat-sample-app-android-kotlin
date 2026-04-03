package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * Represents the UI state for conversation starter feature.
 *
 * This sealed class is used by the ViewModel to communicate the current
 * state of conversation starter fetching to the UI layer.
 *
 * @see com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
 */
sealed class ConversationStarterUIState {
    
    /**
     * Idle state - no conversation starter request in progress or available.
     *
     * This is the initial state and the state after clearing conversation starters.
     */
    object Idle : ConversationStarterUIState()
    
    /**
     * Loading state - conversation starters are being fetched.
     *
     * The UI should display a loading indicator while in this state.
     */
    object Loading : ConversationStarterUIState()
    
    /**
     * Loaded state - conversation starters have been loaded successfully.
     *
     * @param starters The list of conversation starter suggestions.
     */
    data class Loaded(val starters: List<String>) : ConversationStarterUIState()
    
    /**
     * Error state - an error occurred while fetching conversation starters.
     *
     * @param exception The exception that caused the error.
     */
    data class Error(val exception: CometChatException) : ConversationStarterUIState()
}
