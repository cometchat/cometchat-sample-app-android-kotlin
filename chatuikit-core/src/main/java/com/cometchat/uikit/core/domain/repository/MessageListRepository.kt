package com.cometchat.uikit.core.domain.repository

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.uikit.core.domain.model.SurroundingMessagesResult

/**
 * Repository interface defining the data operations contract for message list functionality.
 *
 * This interface lives in the domain layer and contains no implementation details,
 * following clean architecture principles. It allows for custom implementations to be
 * injected, enabling flexibility in data fetching strategies (remote, local, cached).
 *
 * ## Usage
 *
 * The repository must be configured for either a user or group conversation before
 * fetching messages:
 *
 * ```kotlin
 * // For user conversation
 * repository.configureForUser(user, messagesTypes, messagesCategories, parentMessageId, null)
 *
 * // For group conversation
 * repository.configureForGroup(group, messagesTypes, messagesCategories, parentMessageId, null)
 *
 * // Then fetch messages
 * val result = repository.fetchPreviousMessages()
 * ```
 *
 * ## Threading
 *
 * For threaded conversations, pass the parent message ID when configuring:
 * - Use `-1` for main conversation (non-threaded)
 * - Use the parent message's ID for threaded replies
 *
 * ## Pagination
 *
 * Messages are fetched in batches. Use [hasMorePreviousMessages] to check if more
 * messages are available before calling [fetchPreviousMessages].
 *
 * @see com.cometchat.uikit.core.data.repository.MessageListRepositoryImpl
 * @see com.cometchat.uikit.core.viewmodel.CometChatMessageListViewModel
 */
interface MessageListRepository {

    /**
     * Fetches previous (older) messages using the configured [MessagesRequest].
     *
     * This method fetches messages in reverse chronological order, starting from
     * the oldest message in the current list (or from the most recent if the list is empty).
     *
     * The repository must be configured via [configureForUser] or [configureForGroup]
     * before calling this method.
     *
     * @return [Result] containing a list of [BaseMessage] objects on success,
     *         or an error on failure. An empty list indicates no more messages are available.
     *
     * @see fetchNextMessages
     * @see hasMorePreviousMessages
     */
    suspend fun fetchPreviousMessages(): Result<List<BaseMessage>>

    /**
     * Fetches next (newer) messages starting from a specific message ID.
     *
     * This method is used to fetch messages that arrived after the specified message,
     * typically used when the user scrolls down to load newer messages or when
     * reconnecting after being offline.
     *
     * @param fromMessageId The message ID to start fetching from (exclusive).
     *                      Messages with IDs greater than this will be returned.
     * @return [Result] containing a list of [BaseMessage] objects on success,
     *         or an error on failure. An empty list indicates no newer messages are available.
     *
     * @see fetchPreviousMessages
     */
    suspend fun fetchNextMessages(fromMessageId: Long): Result<List<BaseMessage>>

    /**
     * Fetches conversation details including unread message count.
     *
     * This method retrieves the [Conversation] object which contains metadata
     * about the conversation, including:
     * - Unread message count
     * - Last read message ID
     * - Last message in the conversation
     *
     * @param id The User UID for user conversations, or Group GUID for group conversations.
     * @param type The conversation type: `"user"` for 1-on-1 conversations,
     *             `"group"` for group conversations.
     * @return [Result] containing the [Conversation] object on success,
     *         or an error on failure.
     */
    suspend fun getConversation(id: String, type: String): Result<Conversation>

    /**
     * Fetches a single message by its unique ID.
     *
     * This method is useful for refreshing a specific message's data,
     * such as after a reaction is added or removed.
     *
     * @param messageId The unique ID of the message to fetch.
     * @return [Result] containing the [BaseMessage] on success,
     *         or an error on failure (e.g., message not found).
     */
    suspend fun getMessage(messageId: Long): Result<BaseMessage>

    /**
     * Deletes a message from the server.
     *
     * The deleted message will have its `deletedAt` timestamp set and its content
     * may be replaced with a "message deleted" placeholder, depending on the
     * CometChat configuration.
     *
     * @param message The [BaseMessage] to delete. Only the sender can delete their own messages,
     *                or admins/moderators can delete messages in groups.
     * @return [Result] containing the deleted [BaseMessage] with updated metadata on success,
     *         or an error on failure (e.g., permission denied).
     */
    suspend fun deleteMessage(message: BaseMessage): Result<BaseMessage>

    /**
     * Flags/reports a message for moderation.
     *
     * This method allows users to report inappropriate content. The flagged message
     * will be reviewed according to the app's moderation settings.
     *
     * @param messageId The unique ID of the message to flag.
     * @param reason The reason for flagging (e.g., "spam", "harassment", "inappropriate").
     * @param remark Additional remarks or context provided by the user (can be empty).
     * @return [Result] indicating success ([Result.success] with [Unit]) or failure.
     */
    suspend fun flagMessage(messageId: Long, reason: String, remark: String): Result<Unit>

    /**
     * Adds a reaction to a message.
     *
     * Reactions are typically emoji characters that users can add to express
     * quick responses to messages.
     *
     * @param messageId The unique ID of the message to react to.
     * @param emoji The reaction emoji string (e.g., "👍", "❤️", "😂").
     * @return [Result] containing the updated [BaseMessage] with the new reaction on success,
     *         or an error on failure.
     *
     * @see removeReaction
     */
    suspend fun addReaction(messageId: Long, emoji: String): Result<BaseMessage>

    /**
     * Removes a reaction from a message.
     *
     * Users can only remove their own reactions from messages.
     *
     * @param messageId The unique ID of the message to remove the reaction from.
     * @param emoji The reaction emoji string to remove.
     * @return [Result] containing the updated [BaseMessage] with the reaction removed on success,
     *         or an error on failure.
     *
     * @see addReaction
     */
    suspend fun removeReaction(messageId: Long, emoji: String): Result<BaseMessage>

    /**
     * Marks a message as delivered.
     *
     * This sends a delivery receipt to the message sender, indicating that
     * the message has been received on the current user's device.
     *
     * @param message The [BaseMessage] to mark as delivered.
     * @return [Result] indicating success ([Result.success] with [Unit]) or failure.
     *
     * @see markAsRead
     */
    suspend fun markAsDelivered(message: BaseMessage): Result<Unit>

    /**
     * Marks a message as read.
     *
     * This sends a read receipt to the message sender, indicating that the
     * current user has seen the message. Read receipts are only sent for
     * messages from other users (not the current user's own messages).
     *
     * @param message The [BaseMessage] to mark as read.
     * @return [Result] indicating success ([Result.success] with [Unit]) or failure.
     *
     * @see markAsUnread
     */
    suspend fun markAsRead(message: BaseMessage): Result<Unit>

    /**
     * Marks a message as unread.
     *
     * This allows users to mark a message as unread for later attention.
     * The conversation will show as having unread messages.
     *
     * @param message The [BaseMessage] to mark as unread.
     * @return [Result] containing the updated [Conversation] with correct unread count on success,
     *         or an error on failure.
     *
     * @see markAsRead
     */
    suspend fun markAsUnread(message: BaseMessage): Result<Conversation>

    /**
     * Checks if there are more previous (older) messages available to fetch.
     *
     * This method should be called before [fetchPreviousMessages] to avoid
     * unnecessary network requests when all messages have been loaded.
     *
     * @return `true` if more messages are available, `false` if all messages have been fetched.
     */
    fun hasMorePreviousMessages(): Boolean

    /**
     * Resets the messages request for fresh fetching.
     *
     * This method clears the internal pagination state and recreates the
     * [MessagesRequest], allowing messages to be fetched from the beginning.
     * Call this when switching conversations or when a full refresh is needed.
     */
    fun resetRequest()

    /**
     * Configures the repository for a user (1-on-1) conversation.
     *
     * This method must be called before fetching messages for a user conversation.
     * It sets up the internal [MessagesRequest] with the appropriate filters.
     *
     * @param user The [User] to fetch messages for.
     * @param messagesTypes List of message types to include (e.g., "text", "image", "video").
     *                      Use an empty list to include all types.
     * @param messagesCategories List of message categories to include (e.g., "message", "action", "call").
     *                           Use an empty list to include all categories.
     * @param parentMessageId Parent message ID for threaded conversations.
     *                        Use `-1` for the main conversation (non-threaded).
     * @param messagesRequestBuilder Optional custom [MessagesRequest.MessagesRequestBuilder]
     *                               for advanced configuration. If `null`, a default builder is used.
     *
     * @see configureForGroup
     */
    fun configureForUser(
        user: User,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    )

    /**
     * Configures the repository for a group conversation.
     *
     * This method must be called before fetching messages for a group conversation.
     * It sets up the internal [MessagesRequest] with the appropriate filters.
     *
     * @param group The [Group] to fetch messages for.
     * @param messagesTypes List of message types to include (e.g., "text", "image", "video").
     *                      Use an empty list to include all types.
     * @param messagesCategories List of message categories to include (e.g., "message", "action", "call").
     *                           Use an empty list to include all categories.
     * @param parentMessageId Parent message ID for threaded conversations.
     *                        Use `-1` for the main conversation (non-threaded).
     * @param messagesRequestBuilder Optional custom [MessagesRequest.MessagesRequestBuilder]
     *                               for advanced configuration. If `null`, a default builder is used.
     *
     * @see configureForUser
     */
    fun configureForGroup(
        group: Group,
        messagesTypes: List<String>,
        messagesCategories: List<String>,
        parentMessageId: Long,
        messagesRequestBuilder: MessagesRequest.MessagesRequestBuilder?
    )

    /**
     * Fetches messages surrounding a specific message ID.
     *
     * This method is used by the `goToMessage` functionality to fetch messages
     * both before and after a target message, providing context around it.
     * The result includes:
     * - Older messages (fetched using `fetchPrevious` with the target message ID)
     * - The target message itself
     * - Newer messages (fetched using `fetchNext` with the target message ID)
     *
     * The repository must be configured via [configureForUser] or [configureForGroup]
     * before calling this method.
     *
     * @param messageId The unique ID of the target message to fetch surrounding messages for.
     * @return [Result] containing a [SurroundingMessagesResult] on success,
     *         or an error on failure (e.g., message not found, network error).
     *
     * @see SurroundingMessagesResult
     * @see fetchPreviousMessages
     * @see fetchNextMessages
     */
    suspend fun fetchSurroundingMessages(messageId: Long): Result<SurroundingMessagesResult>

    /**
     * Fetches action messages (edits, deletes) since a specific message ID.
     *
     * This method is used to update the message list after reconnection by fetching
     * ACTION category messages that indicate edits, deletions, or other modifications
     * to existing messages. The `actionOn` property of each action message contains
     * the updated message data.
     *
     * @param fromMessageId The message ID to start fetching from (exclusive).
     *                      Action messages with IDs greater than this will be returned.
     * @return [Result] containing a list of [BaseMessage] objects of ACTION category on success,
     *         or an error on failure.
     *
     * @see fetchNextMessages
     */
    suspend fun fetchActionMessages(fromMessageId: Long): Result<List<BaseMessage>>

    /**
     * Rebuilds the messages request to paginate from a specific message ID.
     *
     * This method is called after `goToMessage` to enable continued scrolling
     * from the new position. It recreates the internal [MessagesRequest] with
     * the provided message ID as the starting point for pagination.
     *
     * The user/group configuration is preserved from the previous configuration.
     *
     * @param messageId The message ID to use as the starting point for pagination.
     *                  Subsequent calls to [fetchPreviousMessages] will fetch messages
     *                  older than this ID.
     *
     * @see fetchPreviousMessages
     */
    fun rebuildRequestFromMessageId(messageId: Long)

    /**
     * Gets the current latest message ID tracked by the repository.
     *
     * This ID is used for real-time message guards to determine if the user
     * is viewing the latest messages. When a new real-time message arrives,
     * it should only be added to the list if the user is at the "latest" position
     * (i.e., the last message in the list has this ID).
     *
     * @return The latest message ID, or `-1` if not set.
     *
     * @see setLatestMessageId
     */
    fun getLatestMessageId(): Long

    /**
     * Sets the latest message ID for real-time message guards.
     *
     * This method should be called:
     * - When the conversation is first loaded (from `conversation.lastMessage.id`)
     * - When a new message is successfully added to the list
     *
     * @param messageId The ID of the latest message in the conversation.
     *
     * @see getLatestMessageId
     */
    fun setLatestMessageId(messageId: Long)
}
