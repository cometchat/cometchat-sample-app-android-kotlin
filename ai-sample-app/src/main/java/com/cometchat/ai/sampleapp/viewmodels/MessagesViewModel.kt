package com.cometchat.ai.sampleapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.shared.constants.MessageStatus
import com.cometchat.chatuikit.shared.events.CometChatConversationEvents
import com.cometchat.chatuikit.shared.events.CometChatMessageEvents
import com.cometchat.chatuikit.shared.events.CometChatUIEvents

/** ViewModel for managing the state and data of messages in a group chat.  */
class MessagesViewModel : ViewModel() {
    private val listenerId = System.currentTimeMillis().toString() + javaClass.simpleName

    /**
     * Gets the LiveData for sent messages.
     *
     * @return MutableLiveData object containing sent BaseMessage data.
     */
    private var _sentMessage: MutableLiveData<Boolean> = MutableLiveData()
    val sentMessage : MutableLiveData<Boolean> get() = _sentMessage

    /**
     * Gets the LiveData for the base message.
     *
     * @return MutableLiveData object containing the BaseMessage data.
     */
    val baseMessage: MutableLiveData<BaseMessage?> = MutableLiveData()

    private val openUserChat = MutableLiveData<User?>()

    /**
     * Gets the LiveData that indicates whether to exit the activity.
     *
     * @return MutableLiveData object containing a boolean indicating exit state.
     */
    val isExitActivity: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * Gets the LiveData for opening a user chat.
     *
     * @return MutableLiveData object containing the User data for the chat to be opened.
     */
    fun openUserChat(): MutableLiveData<User?> {
        return openUserChat
    }

    /** Adds listeners for group and user events.  */
    fun addListener() {
        CometChatMessageEvents.addListener(listenerId, object : CometChatMessageEvents() {
            override fun ccMessageSent(baseMessage: BaseMessage?, status: Int) {
                if (baseMessage != null && status == MessageStatus.IN_PROGRESS) {
                    sentMessage.value = true
                }
            }
        })

        CometChatUIEvents.addListener(listenerId, object : CometChatUIEvents() {
            override fun ccActiveChatChanged(
                id: HashMap<String, String>, message: BaseMessage?, user: User?, group: Group?
            ) {
                baseMessage.value = message
            }

            override fun ccOpenChat(
                user: User?, group: Group?
            ) {
                openUserChat.value = user
            }
        })

        CometChatConversationEvents.addListener(listenerId, object : CometChatConversationEvents() {
            override fun ccConversationDeleted(conversation: Conversation) {
                isExitActivity.value = true
            }
        })
    }

    /** Removes listeners for group and user events.  */
    fun removeListener() {
        CometChatMessageEvents.removeListener(listenerId)
        CometChatUIEvents.removeListener(listenerId)
        CometChatConversationEvents.removeListener(listenerId)
    }
}
