package com.cometchat.uikit.core.state

import com.cometchat.chat.exceptions.CometChatException

/**
 * Sealed class representing UI states for the sticker keyboard.
 * Used by the CometChatStickerKeyboardViewModel to communicate current state to the UI.
 *
 * ## State Transitions
 *
 * ```
 * [Initial] --> Loading: Component displayed
 * Loading --> Content: Fetch success (non-empty)
 * Loading --> Empty: Fetch success (empty)
 * Loading --> Error: Fetch failed
 * Error --> Loading: Retry clicked
 * ```
 */
sealed class StickerKeyboardUIState {
    /**
     * Loading state - displayed while fetching stickers from the server.
     * The UI should show a shimmer effect or loading indicator.
     */
    object Loading : StickerKeyboardUIState()

    /**
     * Content state - displayed when stickers are available.
     * The actual sticker data is stored in the ViewModel's stickerSets StateFlow,
     * this state just indicates that content is ready to display.
     */
    object Content : StickerKeyboardUIState()

    /**
     * Empty state - displayed when no stickers are available.
     * The UI should show an empty state message.
     */
    object Empty : StickerKeyboardUIState()

    /**
     * Error state - displayed when fetching stickers fails.
     * The UI should show an error message with a retry button.
     *
     * @property exception The exception that caused the error
     */
    data class Error(val exception: CometChatException) : StickerKeyboardUIState()
}
