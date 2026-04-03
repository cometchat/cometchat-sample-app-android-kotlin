package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.CometChatUIKit
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of MessageComposerDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw message sending operations.
 * This is the default implementation used for remote data operations.
 * 
 * Uses [CometChatUIKit] for sending text, media, and custom messages which provides
 * additional UIKit-specific functionality like setting sender, muid, and sentAt.
 * Uses [CometChat] directly for editing messages.
 * 
 * All methods use [suspendCancellableCoroutine] to convert callback-based SDK APIs
 * to suspend functions for use with Kotlin coroutines.
 * 
 * @see MessageComposerDataSource for the interface definition
 */
class MessageComposerDataSourceImpl : MessageComposerDataSource {
    
    /**
     * Sends a text message using CometChatUIKit.
     * 
     * CometChatUIKit.sendTextMessage automatically:
     * - Sets the sender to the logged-in user if not set
     * - Generates a muid (message unique ID) if not set
     * - Sets the sentAt timestamp if not set
     * 
     * @param message The TextMessage object to send
     * @return The sent TextMessage with updated metadata from the server
     * @throws CometChatException if the SDK call fails
     */
    override suspend fun sendTextMessage(message: TextMessage): TextMessage = 
        suspendCancellableCoroutine { continuation ->
            CometChatUIKit.sendTextMessage(
                message,
                object : CometChat.CallbackListener<TextMessage>() {
                    override fun onSuccess(textMessage: TextMessage) {
                        continuation.resume(textMessage)
                    }
                    
                    override fun onError(exception: CometChatException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    
    /**
     * Sends a media message (image, video, audio, file) using CometChatUIKit.
     * 
     * CometChatUIKit.sendMediaMessage automatically:
     * - Sets the sender to the logged-in user if not set
     * - Generates a muid (message unique ID) if not set
     * - Sets the sentAt timestamp if not set
     * - Uploads the media file and attaches the URL to the message
     * 
     * @param message The MediaMessage object containing the file to send
     * @return The sent MediaMessage with updated metadata including attachment URL
     * @throws CometChatException if the SDK call fails
     */
    override suspend fun sendMediaMessage(message: MediaMessage): MediaMessage = 
        suspendCancellableCoroutine { continuation ->
            CometChatUIKit.sendMediaMessage(
                message,
                object : CometChat.CallbackListener<MediaMessage>() {
                    override fun onSuccess(mediaMessage: MediaMessage) {
                        continuation.resume(mediaMessage)
                    }
                    
                    override fun onError(exception: CometChatException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    
    /**
     * Sends a custom message with custom data payload using CometChatUIKit.
     * 
     * CometChatUIKit.sendCustomMessage automatically:
     * - Sets the sender to the logged-in user if not set
     * - Generates a muid (message unique ID) if not set
     * - Sets the sentAt timestamp if not set
     * - Enables push notifications for the message
     * 
     * @param message The CustomMessage object containing custom data to send
     * @return The sent CustomMessage with updated metadata from the server
     * @throws CometChatException if the SDK call fails
     */
    override suspend fun sendCustomMessage(message: CustomMessage): CustomMessage = 
        suspendCancellableCoroutine { continuation ->
            CometChatUIKit.sendCustomMessage(
                message,
                object : CometChat.CallbackListener<CustomMessage>() {
                    override fun onSuccess(customMessage: CustomMessage) {
                        continuation.resume(customMessage)
                    }
                    
                    override fun onError(exception: CometChatException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    
    /**
     * Edits an existing text message using CometChat SDK directly.
     * 
     * The message object should have:
     * - The original message ID set
     * - The updated text content
     * 
     * @param message The TextMessage object with updated content
     * @return The edited BaseMessage with updated metadata from the server
     * @throws CometChatException if the SDK call fails
     */
    override suspend fun editMessage(message: TextMessage): BaseMessage = 
        suspendCancellableCoroutine { continuation ->
            CometChat.editMessage(
                message,
                object : CometChat.CallbackListener<BaseMessage>() {
                    override fun onSuccess(baseMessage: BaseMessage) {
                        continuation.resume(baseMessage)
                    }
                    
                    override fun onError(exception: CometChatException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
}
