package com.cometchat.uikit.kotlin.presentation.shared.receipts

/**
 * Enum representing message receipt/delivery status.
 */
enum class ReceiptStatus {
    /**
     * Message is being sent (in progress)
     */
    IN_PROGRESS,

    /**
     * Message has been sent to the server
     */
    SENT,

    /**
     * Message has been delivered to the recipient
     */
    DELIVERED,

    /**
     * Message has been read by the recipient
     */
    READ,

    /**
     * Message failed to send
     */
    ERROR
}
