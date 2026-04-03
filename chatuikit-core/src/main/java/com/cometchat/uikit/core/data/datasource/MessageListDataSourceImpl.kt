package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.FlagDetail
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of MessageListDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw data fetching/saving.
 * This is the default implementation used for remote data operations.
 */
class MessageListDataSourceImpl : MessageListDataSource {

    override suspend fun fetchPreviousMessages(
        request: MessagesRequest
    ): List<BaseMessage> = suspendCancellableCoroutine { continuation ->
        request.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(messages: List<BaseMessage>) {
                continuation.resume(messages)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    override suspend fun fetchNextMessages(
        request: MessagesRequest
    ): List<BaseMessage> = suspendCancellableCoroutine { continuation ->
        request.fetchNext(object : CometChat.CallbackListener<List<BaseMessage>>() {
            override fun onSuccess(messages: List<BaseMessage>) {
                continuation.resume(messages)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    override suspend fun getConversation(
        id: String,
        type: String
    ): Conversation = suspendCancellableCoroutine { continuation ->
        CometChat.getConversation(id, type, object : CometChat.CallbackListener<Conversation>() {
            override fun onSuccess(conversation: Conversation) {
                continuation.resume(conversation)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    override suspend fun getMessage(
        messageId: Long
    ): BaseMessage = suspendCancellableCoroutine { continuation ->
        CometChat.getMessageDetails(messageId, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage) {
                continuation.resume(message)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    override suspend fun deleteMessage(
        messageId: Long
    ): BaseMessage = suspendCancellableCoroutine { continuation ->
        CometChat.deleteMessage(messageId, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(deletedMessage: BaseMessage) {
                continuation.resume(deletedMessage)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    override suspend fun flagMessage(
        messageId: Long,
        reason: String,
        remark: String
    ): Unit = suspendCancellableCoroutine { continuation ->
        val flagDetail = FlagDetail(reason, remark)
        CometChat.flagMessage(messageId, flagDetail, object : CometChat.CallbackListener<String>() {
            override fun onSuccess(result: String) {
                continuation.resume(Unit)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    override suspend fun addReaction(
        messageId: Long,
        emoji: String
    ): BaseMessage = suspendCancellableCoroutine { continuation ->
        CometChat.addReaction(messageId, emoji, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(updatedMessage: BaseMessage) {
                continuation.resume(updatedMessage)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    override suspend fun removeReaction(
        messageId: Long,
        emoji: String
    ): BaseMessage = suspendCancellableCoroutine { continuation ->
        CometChat.removeReaction(messageId, emoji, object : CometChat.CallbackListener<BaseMessage>() {
            override fun onSuccess(updatedMessage: BaseMessage) {
                continuation.resume(updatedMessage)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    override suspend fun markAsDelivered(message: BaseMessage) {
        CometChat.markAsDelivered(message)
    }

    override suspend fun markAsRead(
        message: BaseMessage
    ): Unit = suspendCancellableCoroutine { continuation ->
        CometChat.markAsRead(message, object : CometChat.CallbackListener<Void?>() {
            override fun onSuccess(unused: Void?) {
                continuation.resume(Unit)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    override suspend fun markAsUnread(
        message: BaseMessage
    ): Conversation = suspendCancellableCoroutine { continuation ->
        CometChat.markMessageAsUnread(message, object : CometChat.CallbackListener<Conversation>() {
            override fun onSuccess(conversation: Conversation) {
                continuation.resume(conversation)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }
}
