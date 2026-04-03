package com.cometchat.uikit.kotlin.shared.interfaces

import com.cometchat.chat.models.BaseMessage

/**
 * Listener interface for message option click events.
 *
 * This listener is invoked when a user clicks on any message option
 * in the message context menu (e.g., reply, copy, edit, delete, etc.).
 *
 * @see com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList.setMessageOptionClickListener
 */
fun interface MessageOptionClickListener {
    /**
     * Called when a message option is clicked.
     *
     * @param message The message for which the option was clicked.
     * @param optionId The ID of the clicked option (e.g., "reply", "copy", "edit", "delete").
     * @param optionName The display name of the clicked option.
     */
    fun onMessageOptionClick(message: BaseMessage, optionId: String, optionName: String)
}
