package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage

/**
 * Repository interface defining data operations contract for message composer.
 * Lives in domain layer - no implementation details.
 *
 * This interface allows for custom implementations to be injected,
 * enabling flexibility in data sending strategies (remote, local, cached).
 *
 * The message composer component uses this repository to send text messages,
 * media messages, custom messages, and edit existing messages.
 *
 * All methods return Result type for proper error handling, allowing the
 * ViewModel to handle errors gracefully and maintain the last known valid state.
 */
interface MessageComposerRepository {

    /**
     * Sends a text message to the configured receiver.
     *
     * This method sends a text message containing the composed text content
     * to either a user or group receiver. The message may include quoted
     * message information for reply functionality.
     *
     * @param message The TextMessage object containing the message text and receiver information
     * @return Result containing the sent TextMessage with updated metadata (id, sentAt, etc.)
     *         on success, or error on failure
     */
    suspend fun sendTextMessage(message: TextMessage): Result<TextMessage>

    /**
     * Sends a media message (image, video, audio, file) to the configured receiver.
     *
     * This method sends a media message containing a file attachment to either
     * a user or group receiver. The message may include quoted message information
     * for reply functionality.
     *
     * @param message The MediaMessage object containing the media file and receiver information
     * @return Result containing the sent MediaMessage with updated metadata (id, sentAt, attachment URL, etc.)
     *         on success, or error on failure
     */
    suspend fun sendMediaMessage(message: MediaMessage): Result<MediaMessage>

    /**
     * Sends a custom message with custom data payload to the configured receiver.
     *
     * This method sends a custom message containing a custom data payload to either
     * a user or group receiver. Custom messages are used for implementing custom
     * message types beyond text and media.
     *
     * @param message The CustomMessage object containing custom data and receiver information
     * @return Result containing the sent CustomMessage with updated metadata (id, sentAt, etc.)
     *         on success, or error on failure
     */
    suspend fun sendCustomMessage(message: CustomMessage): Result<CustomMessage>

    /**
     * Edits an existing text message.
     *
     * This method updates the text content of an existing message. The message
     * must have been sent by the current user and must be a text message.
     *
     * @param message The TextMessage object with updated text content and the original message ID
     * @return Result containing the edited BaseMessage with updated metadata on success,
     *         or error on failure
     */
    suspend fun editMessage(message: TextMessage): Result<BaseMessage>
}
