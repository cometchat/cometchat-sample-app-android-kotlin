package com.cometchat.uikit.core.data.datasource

/**
 * Interface defining data source operations for collaborative features (whiteboard and document).
 * Lives in data layer - defines contract for collaborative board creation.
 * Allows for different implementations (remote, local, mock).
 */
interface CollaborativeDataSource {

    /**
     * Creates a collaborative whiteboard via the CometChat Extensions API.
     *
     * @param receiverId The ID of the receiver (user UID or group GUID)
     * @param receiverType The type of receiver ("user" or "group")
     * @param quotedMessageId Optional ID of the message being replied to
     * @return Result containing success or error
     */
    suspend fun createWhiteboard(
        receiverId: String,
        receiverType: String,
        quotedMessageId: Long? = null
    ): Result<Unit>

    /**
     * Creates a collaborative document via the CometChat Extensions API.
     *
     * @param receiverId The ID of the receiver (user UID or group GUID)
     * @param receiverType The type of receiver ("user" or "group")
     * @param quotedMessageId Optional ID of the message being replied to
     * @return Result containing success or error
     */
    suspend fun createDocument(
        receiverId: String,
        receiverType: String,
        quotedMessageId: Long? = null
    ): Result<Unit>
}
