package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage

/**
 * Sealed class representing UI states for the thread header component.
 * Used by the ThreadHeaderViewModel to communicate current state to the UI.
 *
 * The thread header displays the parent message of a thread conversation,
 * along with the reply count, supporting loading, content display, and error states.
 *
 * @see com.cometchat.uikit.core.viewmodel.ThreadHeaderViewModel
 */
sealed class ThreadHeaderUIState {

    /**
     * Loading state - displayed while initializing the thread header.
     * This is the initial state before the parent message is set.
     */
    object Loading : ThreadHeaderUIState()

    /**
     * Content state - displayed when the parent message is available.
     * Contains the parent message and the current reply count for the thread.
     *
     * @param parentMessage The BaseMessage that started the thread conversation
     * @param replyCount The number of replies in the thread
     */
    data class Content(
        val parentMessage: BaseMessage,
        val replyCount: Int
    ) : ThreadHeaderUIState()

    /**
     * Error state - displayed when loading or updating the thread header fails.
     * The UI should handle this state by showing an error message or
     * maintaining the last known valid state.
     *
     * @param exception The CometChatException that caused the error
     */
    data class Error(val exception: CometChatException) : ThreadHeaderUIState()
}
