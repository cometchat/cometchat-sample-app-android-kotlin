package com.cometchat.uikit.core.events

/**
 * Enum representing the status of a message operation.
 * Maps to the Java MessageStatus annotation values for compatibility.
 */
enum class MessageStatus(val value: Int) {
    /**
     * The message operation is in progress.
     */
    IN_PROGRESS(0),

    /**
     * The message operation was successful.
     */
    SUCCESS(1),

    /**
     * The message operation encountered an error.
     */
    ERROR(-1);

    companion object {
        /**
         * Converts an integer value to the corresponding MessageStatus.
         * @param value The integer value to convert
         * @return The corresponding MessageStatus, defaults to IN_PROGRESS if not found
         */
        fun fromInt(value: Int): MessageStatus = entries.find { it.value == value } ?: IN_PROGRESS
    }
}
