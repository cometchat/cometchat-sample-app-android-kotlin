package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage

/**
 * Interface defining data source operations for message composer.
 * Lives in data layer - defines contract for message sending operations.
 * Allows for different implementations (remote, local, mock).
 * 
 * This interface provides methods for:
 * - Sending text messages
 * - Sending media messages (images, videos, audio, files)
 * - Sending custom messages with custom data payloads
 * - Editing existing text messages
 * 
 * @see MessageComposerDataSourceImpl for the default implementation using CometChatUIKit
 */
interface MessageComposerDataSource {
    
    /**
     * Sends a text message to the configured receiver.
     * 
     * @param message The TextMessage object containing the message text and receiver information
     * @return The sent TextMessage with updated metadata (id, sentAt, etc.)
     * @throws com.cometchat.chat.exceptions.CometChatException if sending fails
     */
    suspend fun sendTextMessage(message: TextMessage): TextMessage
    
    /**
     * Sends a media message (image, video, audio, file) to the configured receiver.
     * 
     * @param message The MediaMessage object containing the media file and receiver information
     * @return The sent MediaMessage with updated metadata (id, sentAt, attachment URL, etc.)
     * @throws com.cometchat.chat.exceptions.CometChatException if sending fails
     */
    suspend fun sendMediaMessage(message: MediaMessage): MediaMessage
    
    /**
     * Sends a custom message with custom data payload to the configured receiver.
     * 
     * @param message The CustomMessage object containing custom data and receiver information
     * @return The sent CustomMessage with updated metadata (id, sentAt, etc.)
     * @throws com.cometchat.chat.exceptions.CometChatException if sending fails
     */
    suspend fun sendCustomMessage(message: CustomMessage): CustomMessage
    
    /**
     * Edits an existing text message.
     * 
     * @param message The TextMessage object with updated text content and the original message ID
     * @return The edited BaseMessage with updated metadata
     * @throws com.cometchat.chat.exceptions.CometChatException if editing fails
     */
    suspend fun editMessage(message: TextMessage): BaseMessage
}
