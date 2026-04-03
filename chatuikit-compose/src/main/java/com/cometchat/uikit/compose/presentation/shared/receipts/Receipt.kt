package com.cometchat.uikit.compose.presentation.shared.receipts

/**
 * Enum representing the receipt status of a message.
 * Mirrors the View-based Receipt enum.
 */
enum class Receipt {
    /**
     * The message has been sent.
     */
    SENT,

    /**
     * The message has been delivered.
     */
    DELIVERED,

    /**
     * The message has been read.
     */
    READ,

    /**
     * An error occurred while sending the message.
     */
    ERROR,

    /**
     * The message is currently in progress (being sent).
     */
    IN_PROGRESS
}
