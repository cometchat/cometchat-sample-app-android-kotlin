package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * Represents the UI state for smart replies feature.
 *
 * This sealed class is used by the ViewModel to communicate the current
 * state of smart replies fetching to the UI layer.
 *
 * @see com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
 */
sealed class SmartRepliesUIState {
    
    /**
     * Idle state - no smart replies request in progress or available.
     *
     * This is the initial state and the state after clearing smart replies.
     */
    object Idle : SmartRepliesUIState()
    
    /**
     * Loading state - smart replies are being fetched.
     *
     * The UI should display a loading indicator while in this state.
     */
    object Loading : SmartRepliesUIState()
    
    /**
     * Loaded state - smart replies have been loaded successfully.
     *
     * @param replies The list of smart reply suggestions.
     */
    data class Loaded(val replies: List<String>) : SmartRepliesUIState()
    
    /**
     * Error state - an error occurred while fetching smart replies.
     *
     * @param exception The exception that caused the error.
     */
    data class Error(val exception: CometChatException) : SmartRepliesUIState()
}
