package com.cometchat.uikit.core.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Central singleton object managing all CometChat event flows.
 * Provides a reactive, type-safe event distribution system using Kotlin SharedFlow.
 *
 * This event bus supports:
 * - Multiple subscribers for each event type
 * - Thread-safe event emission
 * - Type-safe event handling through sealed classes
 * - Automatic buffer management with DROP_OLDEST overflow strategy
 *
 * Usage:
 * ```kotlin
 * // Subscribe to events
 * CometChatEvents.messageEvents.collect { event ->
 *     when (event) {
 *         is CometChatMessageEvent.MessageSent -> handleMessageSent(event)
 *         is CometChatMessageEvent.MessageReceived -> handleMessageReceived(event)
 *         // ... handle other events
 *     }
 * }
 *
 * // Emit events
 * CometChatEvents.emitMessageEvent(CometChatMessageEvent.MessageSent(message, status))
 * ```
 */
object CometChatEvents {
    /**
     * Internal coroutine scope for event emission.
     * Uses SupervisorJob to prevent child failures from cancelling the scope.
     * Uses Default dispatcher for background-safe event delivery.
     * Note: Subscribers should switch to Main dispatcher if needed for UI updates.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ==================== Message Events ====================

    /**
     * Internal mutable flow for message events.
     * Buffer capacity of 64 to handle high-frequency message events.
     */
    private val _messageEvents = MutableSharedFlow<CometChatMessageEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Public read-only flow for message events.
     * Subscribe to this flow to receive message-related events.
     */
    val messageEvents: SharedFlow<CometChatMessageEvent> = _messageEvents.asSharedFlow()


    // ==================== User Events ====================

    /**
     * Internal mutable flow for user events.
     * Buffer capacity of 16 for user-related events.
     */
    private val _userEvents = MutableSharedFlow<CometChatUserEvent>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Public read-only flow for user events.
     * Subscribe to this flow to receive user-related events.
     */
    val userEvents: SharedFlow<CometChatUserEvent> = _userEvents.asSharedFlow()

    // ==================== Group Events ====================

    /**
     * Internal mutable flow for group events.
     * Buffer capacity of 32 for group-related events.
     */
    private val _groupEvents = MutableSharedFlow<CometChatGroupEvent>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Public read-only flow for group events.
     * Subscribe to this flow to receive group-related events.
     */
    val groupEvents: SharedFlow<CometChatGroupEvent> = _groupEvents.asSharedFlow()

    // ==================== Call Events ====================

    /**
     * Internal mutable flow for call events.
     * Buffer capacity of 8 for call-related events.
     */
    private val _callEvents = MutableSharedFlow<CometChatCallEvent>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Public read-only flow for call events.
     * Subscribe to this flow to receive call-related events.
     */
    val callEvents: SharedFlow<CometChatCallEvent> = _callEvents.asSharedFlow()

    // ==================== Conversation Events ====================

    /**
     * Internal mutable flow for conversation events.
     * Buffer capacity of 16 for conversation-related events.
     */
    private val _conversationEvents = MutableSharedFlow<CometChatConversationEvent>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Public read-only flow for conversation events.
     * Subscribe to this flow to receive conversation-related events.
     */
    val conversationEvents: SharedFlow<CometChatConversationEvent> = _conversationEvents.asSharedFlow()

    // ==================== UI Events ====================

    /**
     * Internal mutable flow for UI events.
     * Buffer capacity of 16 for UI-related events.
     */
    private val _uiEvents = MutableSharedFlow<CometChatUIEvent>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Public read-only flow for UI events.
     * Subscribe to this flow to receive UI-related events.
     */
    val uiEvents: SharedFlow<CometChatUIEvent> = _uiEvents.asSharedFlow()


    // ==================== Emit Functions ====================

    /**
     * Emits a message event to all active subscribers.
     * Thread-safe: can be called from any thread.
     *
     * @param event The message event to emit
     */
    fun emitMessageEvent(event: CometChatMessageEvent) {
        scope.launch {
            _messageEvents.emit(event)
        }
    }

    /**
     * Emits a user event to all active subscribers.
     * Thread-safe: can be called from any thread.
     *
     * @param event The user event to emit
     */
    fun emitUserEvent(event: CometChatUserEvent) {
        scope.launch {
            _userEvents.emit(event)
        }
    }

    /**
     * Emits a group event to all active subscribers.
     * Thread-safe: can be called from any thread.
     *
     * @param event The group event to emit
     */
    fun emitGroupEvent(event: CometChatGroupEvent) {
        scope.launch {
            _groupEvents.emit(event)
        }
    }

    /**
     * Emits a call event to all active subscribers.
     * Thread-safe: can be called from any thread.
     *
     * @param event The call event to emit
     */
    fun emitCallEvent(event: CometChatCallEvent) {
        scope.launch {
            _callEvents.emit(event)
        }
    }

    /**
     * Emits a conversation event to all active subscribers.
     * Thread-safe: can be called from any thread.
     *
     * @param event The conversation event to emit
     */
    fun emitConversationEvent(event: CometChatConversationEvent) {
        scope.launch {
            _conversationEvents.emit(event)
        }
    }

    /**
     * Emits a UI event to all active subscribers.
     * Thread-safe: can be called from any thread.
     *
     * @param event The UI event to emit
     */
    fun emitUIEvent(event: CometChatUIEvent) {
        scope.launch {
            _uiEvents.emit(event)
        }
    }

    // ==================== Testing Support ====================

    /**
     * Internal function for testing - allows direct emission without launching a coroutine.
     * This is useful for synchronous testing scenarios.
     *
     * @param event The message event to emit
     * @return true if the event was emitted successfully
     */
    internal suspend fun emitMessageEventSync(event: CometChatMessageEvent): Boolean {
        return _messageEvents.tryEmit(event) || run {
            _messageEvents.emit(event)
            true
        }
    }

    /**
     * Internal function for testing - allows direct emission without launching a coroutine.
     *
     * @param event The user event to emit
     * @return true if the event was emitted successfully
     */
    internal suspend fun emitUserEventSync(event: CometChatUserEvent): Boolean {
        return _userEvents.tryEmit(event) || run {
            _userEvents.emit(event)
            true
        }
    }

    /**
     * Internal function for testing - allows direct emission without launching a coroutine.
     *
     * @param event The group event to emit
     * @return true if the event was emitted successfully
     */
    internal suspend fun emitGroupEventSync(event: CometChatGroupEvent): Boolean {
        return _groupEvents.tryEmit(event) || run {
            _groupEvents.emit(event)
            true
        }
    }

    /**
     * Internal function for testing - allows direct emission without launching a coroutine.
     *
     * @param event The call event to emit
     * @return true if the event was emitted successfully
     */
    internal suspend fun emitCallEventSync(event: CometChatCallEvent): Boolean {
        return _callEvents.tryEmit(event) || run {
            _callEvents.emit(event)
            true
        }
    }

    /**
     * Internal function for testing - allows direct emission without launching a coroutine.
     *
     * @param event The conversation event to emit
     * @return true if the event was emitted successfully
     */
    internal suspend fun emitConversationEventSync(event: CometChatConversationEvent): Boolean {
        return _conversationEvents.tryEmit(event) || run {
            _conversationEvents.emit(event)
            true
        }
    }

    /**
     * Internal function for testing - allows direct emission without launching a coroutine.
     *
     * @param event The UI event to emit
     * @return true if the event was emitted successfully
     */
    internal suspend fun emitUIEventSync(event: CometChatUIEvent): Boolean {
        return _uiEvents.tryEmit(event) || run {
            _uiEvents.emit(event)
            true
        }
    }
}
