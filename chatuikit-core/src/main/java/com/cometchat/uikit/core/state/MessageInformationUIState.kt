package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * Sealed class representing the UI states for the MessageInformation component.
 * Per design doc: States enum: LOADING, LOADED, EMPTY, ERROR
 */
sealed class MessageInformationUIState {
    /**
     * Loading state - fetching receipts from SDK.
     */
    data object Loading : MessageInformationUIState()

    /**
     * Loaded state - receipts available and displayed.
     */
    data object Loaded : MessageInformationUIState()

    /**
     * Empty state - no receipts available.
     */
    data object Empty : MessageInformationUIState()

    /**
     * Error state - error occurred during fetch.
     * @param exception The exception that occurred
     */
    data class Error(val exception: CometChatException) : MessageInformationUIState()
}
