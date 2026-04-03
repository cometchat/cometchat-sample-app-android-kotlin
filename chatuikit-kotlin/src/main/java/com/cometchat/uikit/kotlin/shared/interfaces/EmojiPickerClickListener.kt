package com.cometchat.uikit.kotlin.shared.interfaces

/**
 * Listener interface for emoji picker click events.
 *
 * This listener is invoked when a user clicks on the emoji picker button
 * (typically the "+" or "add more reactions" button) in the message context menu.
 *
 * @see com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList.setEmojiPickerClick
 */
fun interface EmojiPickerClickListener {
    /**
     * Called when the emoji picker is opened.
     */
    fun onEmojiPickerClick()
}
