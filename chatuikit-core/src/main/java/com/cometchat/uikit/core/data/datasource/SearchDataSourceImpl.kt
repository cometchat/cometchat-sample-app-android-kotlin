package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of SearchDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw data fetching.
 * This is the default implementation used for remote data operations.
 */
class SearchDataSourceImpl : SearchDataSource {

    /**
     * Fetches conversations from CometChat SDK.
     * @param request The configured ConversationsRequest
     * @return Raw list of Conversation objects from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun fetchConversations(
        request: ConversationsRequest
    ): List<Conversation> = suspendCancellableCoroutine { continuation ->
        request.fetchNext(object : CometChat.CallbackListener<List<Conversation>>() {
            override fun onSuccess(conversations: List<Conversation>) {
                continuation.resume(conversations)
            }

            override fun onError(exception: CometChatException) {
                continuation.resumeWithException(exception)
            }
        })
    }

    /**
     * Fetches messages from CometChat SDK.
     * @param request The configured MessagesRequest
     * @return Raw list of BaseMessage objects from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun fetchMessages(
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
}
