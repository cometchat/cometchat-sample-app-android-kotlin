package com.cometchat.ai.sampleapp.compose.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.events.CometChatConversationEvent
import com.cometchat.uikit.core.events.CometChatEvents
import com.cometchat.uikit.core.events.CometChatMessageEvent
import com.cometchat.uikit.core.events.CometChatUIEvent
import com.cometchat.uikit.core.events.MessageStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the AI Assistant chat screen (Compose).
 *
 * Mirrors the behaviour of the XML activity version: subscribes to v6
 * [CometChatEvents] flows and exposes UI-relevant signals.
 *
 * - [sentMessage]        — emitted when the user sends a message. Used to hide
 *                          the keyboard in agent chats (single-turn UX).
 * - [openUserChat]       — emitted when another component requests opening a
 *                          chat with a specific user (e.g. a mention tap).
 * - [isExitActivity]     — emitted when the current conversation is deleted.
 * - [activeChatMessage]  — last baseMessage associated with the active chat
 *                          (mirrors v5's `ccActiveChatChanged.message`).
 */
class AIAssistantChatViewModel : ViewModel() {

    private val _sentMessage = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val sentMessage: SharedFlow<Boolean> = _sentMessage.asSharedFlow()

    private val _openUserChat = MutableSharedFlow<User?>(extraBufferCapacity = 1)
    val openUserChat: SharedFlow<User?> = _openUserChat.asSharedFlow()

    private val _isExitActivity = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val isExitActivity: SharedFlow<Boolean> = _isExitActivity.asSharedFlow()

    private val _activeChatMessage = MutableStateFlow<BaseMessage?>(null)
    val activeChatMessage: StateFlow<BaseMessage?> = _activeChatMessage.asStateFlow()

    private var messageEventsJob: Job? = null
    private var uiEventsJob: Job? = null
    private var conversationEventsJob: Job? = null

    fun addListeners() {
        removeListeners()

        messageEventsJob = viewModelScope.launch {
            CometChatEvents.messageEvents.collect { event ->
                if (event is CometChatMessageEvent.MessageSent &&
                    (event.status == MessageStatus.IN_PROGRESS ||
                        event.status == MessageStatus.SUCCESS)
                ) {
                    _sentMessage.tryEmit(true)
                }
            }
        }

        uiEventsJob = viewModelScope.launch {
            CometChatEvents.uiEvents.collect { event ->
                when (event) {
                    is CometChatUIEvent.ActiveChatChanged -> {
                        _activeChatMessage.value = event.message
                    }
                    is CometChatUIEvent.OpenChat -> {
                        _openUserChat.tryEmit(event.user)
                    }
                    else -> {}
                }
            }
        }

        conversationEventsJob = viewModelScope.launch {
            CometChatEvents.conversationEvents.collect { event ->
                if (event is CometChatConversationEvent.ConversationDeleted) {
                    _isExitActivity.tryEmit(true)
                }
            }
        }
    }

    fun removeListeners() {
        messageEventsJob?.cancel()
        uiEventsJob?.cancel()
        conversationEventsJob?.cancel()
        messageEventsJob = null
        uiEventsJob = null
        conversationEventsJob = null
    }

    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}
