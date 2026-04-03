package com.cometchat.uikit.kotlin.shared.interfaces

import android.content.Context
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.kotlin.presentation.shared.messagebubble.BubbleFactory

/**
 * Listener interface for thread reply click events.
 *
 * This listener is invoked when a user clicks on the thread reply indicator
 * in a message bubble to navigate to the thread view.
 *
 * @see com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList.setOnThreadRepliesClick
 */
fun interface ThreadReplyClick {
    /**
     * Called when the thread reply indicator is clicked.
     *
     * @param context The Android context
     * @param baseMessage The parent message that has thread replies
     * @param factory The BubbleFactory for this message type (may be null)
     */
    fun onThreadReplyClick(
        context: Context,
        baseMessage: BaseMessage,
        factory: BubbleFactory?
    )
}
