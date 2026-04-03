package com.cometchat.uikit.core.domain.model

import com.cometchat.chat.models.BaseMessage

/**
 * Result of fetching messages surrounding a target message.
 *
 * This data class is used by the [goToMessage] functionality to represent
 * the messages fetched around a specific target message. It contains:
 * - Messages older than the target (for context before)
 * - The target message itself
 * - Messages newer than the target (for context after)
 * - Pagination flags indicating if more messages are available in either direction
 *
 * ## Usage
 *
 * ```kotlin
 * val result = repository.fetchSurroundingMessages(messageId)
 * result.onSuccess { surroundingMessages ->
 *     val allMessages = surroundingMessages.olderMessages +
 *                       listOf(surroundingMessages.targetMessage) +
 *                       surroundingMessages.newerMessages
 *     // Update UI with combined messages
 * }
 * ```
 *
 * @property olderMessages Messages older than the target message, ordered chronologically (oldest first)
 * @property targetMessage The target message itself
 * @property newerMessages Messages newer than the target message, ordered chronologically (oldest first)
 * @property hasMorePrevious Whether there are more older messages available beyond [olderMessages]
 * @property hasMoreNext Whether there are more newer messages available beyond [newerMessages]
 *
 * @see com.cometchat.uikit.core.domain.repository.MessageListRepository.fetchSurroundingMessages
 */
data class SurroundingMessagesResult(
    /** Messages older than the target message */
    val olderMessages: List<BaseMessage>,
    /** The target message itself */
    val targetMessage: BaseMessage,
    /** Messages newer than the target message */
    val newerMessages: List<BaseMessage>,
    /** Whether there are more older messages available */
    val hasMorePrevious: Boolean,
    /** Whether there are more newer messages available */
    val hasMoreNext: Boolean
)
