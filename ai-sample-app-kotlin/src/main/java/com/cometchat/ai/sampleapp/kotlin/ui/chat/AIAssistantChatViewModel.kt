package com.cometchat.ai.sampleapp.kotlin.ui.chat

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
 * ViewModel for the AI Assistant Chat screen.
 *
 * Subscribes to the v6 [CometChatEvents] Flow-based event API (replacing the v5
 * listener pattern) and surfaces UI-relevant signals to the Activity:
 *
 * - [sentMessage]      — emitted when the user sends a message. Used to hide the
 *                        keyboard in agent chats (one-turn UX).
 * - [openUserChat]     — emitted when another component requests opening a chat
 *                        with a specific user (e.g., a mention tap).
 * - [isExitActivity]   — emitted when the current conversation is deleted, so
 *                        the activity can `finish()`.
 * - [activeChatMessage] — the last baseMessage associated with the active chat
 *                        (mirrors v5's `ccActiveChatChanged.message`).
 */
class AIAssistantChatViewModel : ViewModel() {

    // ===== Outgoing state/events for the UI =====

    private val _sentMessage = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val sentMessage: SharedFlow<Boolean> = _sentMessage.asSharedFlow()

    private val _openUserChat = MutableSharedFlow<User?>(extraBufferCapacity = 1)
    val openUserChat: SharedFlow<User?> = _openUserChat.asSharedFlow()

    private val _isExitActivity = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val isExitActivity: SharedFlow<Boolean> = _isExitActivity.asSharedFlow()

    private val _activeChatMessage = MutableStateFlow<BaseMessage?>(null)
    val activeChatMessage: StateFlow<BaseMessage?> = _activeChatMessage.asStateFlow()

    // ===== Internal: subscription jobs =====

    private var messageEventsJob: Job? = null
    private var uiEventsJob: Job? = null
    private var conversationEventsJob: Job? = null

    /**
     * Starts listening to the v6 CometChatEvents flows.
     * Safe to call multiple times; previous subscriptions are cancelled.
     */
    fun addListener() {
        removeListener()

        messageEventsJob = viewModelScope.launch {
            CometChatEvents.messageEvents.collect { event ->
                when (event) {
                    is CometChatMessageEvent.MessageSent -> {
                        // Fire on any in-progress/sent state so the activity can hide keyboard
                        if (event.status == MessageStatus.IN_PROGRESS ||
                            event.status == MessageStatus.SUCCESS
                        ) {
                            _sentMessage.tryEmit(true)
                        }
                    }
                    else -> { /* no-op */ }
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
                    else -> { /* no-op */ }
                }
            }
        }

        conversationEventsJob = viewModelScope.launch {
            CometChatEvents.conversationEvents.collect { event ->
                when (event) {
                    is CometChatConversationEvent.ConversationDeleted -> {
                        _isExitActivity.tryEmit(true)
                    }
                    else -> { /* no-op */ }
                }
            }
        }
    }

    fun removeListener() {
        messageEventsJob?.cancel()
        uiEventsJob?.cancel()
        conversationEventsJob?.cancel()
        messageEventsJob = null
        uiEventsJob = null
        conversationEventsJob = null
    }

    override fun onCleared() {
        super.onCleared()
        removeListener()
    }
}
