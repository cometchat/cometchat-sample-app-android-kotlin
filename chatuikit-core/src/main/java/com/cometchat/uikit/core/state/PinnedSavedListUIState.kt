package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * UI states shared by the Pinned Messages and Saved Messages list screens.
 */
sealed class PinnedSavedListUIState {
    /** Displayed while the first page is loading. */
    object Loading : PinnedSavedListUIState()

    /** Displayed when there is at least one message to show. */
    object Content : PinnedSavedListUIState()

    /** Displayed when the list is empty. */
    object Empty : PinnedSavedListUIState()

    /** Displayed when loading fails. */
    data class Error(val exception: CometChatException) : PinnedSavedListUIState()
}
