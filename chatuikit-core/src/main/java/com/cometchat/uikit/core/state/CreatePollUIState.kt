package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * Sealed class representing UI states for the CreatePoll component.
 * Used by the CometChatCreatePollViewModel to communicate current state to the UI.
 *
 * ## State Transitions
 *
 * ```
 * [Initial] --> Idle: Component displayed
 * Idle --> Submitting: User clicks submit
 * Submitting --> Success: Poll created successfully
 * Submitting --> Error: Poll creation failed
 * Error --> Idle: User dismisses error
 * Success --> Idle: Reset for new poll
 * ```
 */
sealed class CreatePollUIState {
    /**
     * Idle state - the default state when the component is ready for input.
     * The UI should show the form ready for user input.
     */
    object Idle : CreatePollUIState()

    /**
     * Submitting state - displayed while creating the poll.
     * The UI should show a loading indicator and disable the submit button.
     */
    object Submitting : CreatePollUIState()

    /**
     * Success state - displayed when the poll is created successfully.
     * The UI should dismiss the dialog/sheet and notify the consumer.
     */
    object Success : CreatePollUIState()

    /**
     * Error state - displayed when poll creation fails.
     * The UI should show an error message with option to retry.
     *
     * @property exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : CreatePollUIState()
}
