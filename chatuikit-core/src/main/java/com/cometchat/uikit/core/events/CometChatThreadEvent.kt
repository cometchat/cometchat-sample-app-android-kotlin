package com.cometchat.uikit.core.events

/**
 * Thread-subscription events on the kit event bus.
 *
 * Surfaces do not share message instances — the message list, the thread-header parent and a bubble
 * may each hold a separate copy of the same message — so writing the flag onto one object is not
 * seen by the others. This bus is the single channel that keeps them in agreement.
 *
 * The SDK no longer emits anything here: it has no thread listener, and a subscribe/unsubscribe call
 * resolving *is* the acknowledgement. Every event on this bus is published by the kit itself, from
 * [com.cometchat.uikit.core.utils.CometChatThreadSubscription].
 */
sealed class CometChatThreadEvent {
    /**
     * The logged-in user's subscription state for a thread changed.
     *
     * Every surface showing that thread should, on a matching [parentMessageId], update its own
     * reactive state **and** write the flag onto the message object(s) it holds
     * ([com.cometchat.chat.models.BaseMessage.setThreadSubscribed]) so direct reads stay coherent.
     *
     * @param parentMessageId The root message id of the affected thread.
     * @param subscribed The new subscription state.
     */
    data class SubscriptionChanged(
        val parentMessageId: Long,
        val subscribed: Boolean
    ) : CometChatThreadEvent()
}
