package com.cometchat.uikit.kotlin.shared.interfaces

import com.cometchat.chat.models.BaseMessage

/**
 * Listener interface for quick reaction click events.
 *
 * This listener is invoked when a user clicks on a quick reaction emoji
 * in the message context menu's quick reaction bar.
 *
 * @see com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList.setQuickReactionClickListener
 */
fun interface ReactionClickListener {
    /**
     * Called when a quick reaction is clicked.
     *
     * @param message The message for which the reaction was clicked.
     * @param reaction The emoji reaction that was clicked (e.g., "👍", "❤️", "😂").
     */
    fun onReactionClick(message: BaseMessage, reaction: String)
}
