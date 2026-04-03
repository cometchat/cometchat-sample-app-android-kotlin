package com.cometchat.uikit.core.data.repository

import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.TextMessage
import com.cometchat.uikit.core.data.datasource.MessageComposerDataSource
import com.cometchat.uikit.core.domain.repository.MessageComposerRepository

/**
 * Repository implementation that coordinates data sources for message composer.
 * Decides where data comes from and handles data transformation.
 * Accepts MessageComposerDataSource interface for flexibility.
 *
 * This implementation wraps data source calls in Result type for proper
 * error handling, allowing the ViewModel to handle errors gracefully
 * and maintain the last known valid state.
 *
 * @param dataSource The data source to send messages (interface, not concrete)
 */
class MessageComposerRepositoryImpl(
    private val dataSource: MessageComposerDataSource
) : MessageComposerRepository {

    /**
     * Sends a text message using the data source.
     *
     * Wraps the data source call in a Result type to provide proper error handling.
     * Both CometChatException and general exceptions are caught and wrapped as failures.
     *
     * @param message The TextMessage object containing the message text and receiver information
     * @return Result containing the sent TextMessage on success or error on failure
     */
    override suspend fun sendTextMessage(message: TextMessage): Result<TextMessage> {
        return try {
            val sentMessage = dataSource.sendTextMessage(message)
            Result.success(sentMessage)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a media message using the data source.
     *
     * Wraps the data source call in a Result type to provide proper error handling.
     * Both CometChatException and general exceptions are caught and wrapped as failures.
     *
     * @param message The MediaMessage object containing the media file and receiver information
     * @return Result containing the sent MediaMessage on success or error on failure
     */
    override suspend fun sendMediaMessage(message: MediaMessage): Result<MediaMessage> {
        return try {
            val sentMessage = dataSource.sendMediaMessage(message)
            Result.success(sentMessage)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a custom message using the data source.
     *
     * Wraps the data source call in a Result type to provide proper error handling.
     * Both CometChatException and general exceptions are caught and wrapped as failures.
     *
     * @param message The CustomMessage object containing custom data and receiver information
     * @return Result containing the sent CustomMessage on success or error on failure
     */
    override suspend fun sendCustomMessage(message: CustomMessage): Result<CustomMessage> {
        return try {
            val sentMessage = dataSource.sendCustomMessage(message)
            Result.success(sentMessage)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Edits an existing text message using the data source.
     *
     * Wraps the data source call in a Result type to provide proper error handling.
     * Both CometChatException and general exceptions are caught and wrapped as failures.
     *
     * @param message The TextMessage object with updated text content and the original message ID
     * @return Result containing the edited BaseMessage on success or error on failure
     */
    override suspend fun editMessage(message: TextMessage): Result<BaseMessage> {
        return try {
            val editedMessage = dataSource.editMessage(message)
            Result.success(editedMessage)
        } catch (e: CometChatException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
