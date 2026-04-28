package com.cometchat.uikit.core.domain.model

/**
 * Represents the WebSocket connection status of the [CometChatAIStreamService].
 *
 * Used to track and observe connection state changes so that active streams
 * can be properly interrupted and listeners notified on disconnection.
 */
enum class ConnectionState {
    /** WebSocket connection is active and operational. */
    CONNECTED,

    /** WebSocket connection is being established. */
    CONNECTING,

    /** WebSocket connection has been lost. */
    DISCONNECTED,

    /** WebSocket connection encountered an error. */
    ERROR
}
