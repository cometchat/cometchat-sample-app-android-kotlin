package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of ConversationListDataSource that communicates with CometChat SDK.
 * Contains NO business logic - just raw data fetching/saving.
 * This is the default implementation used for remote data operations.
 */
class ConversationListDataSourceImpl : ConversationListDataSource {
    
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
     * Deletes a conversation via CometChat SDK.
     * @param conversationWith The ID of the user or group
     * @param conversationType The type of conversation (user/group)
     * @return Success message from SDK
     * @throws com.cometchat.chat.exceptions.CometChatException if SDK call fails
     */
    override suspend fun deleteConversation(
        conversationWith: String,
        conversationType: String
    ): String = suspendCancellableCoroutine { continuation ->
        CometChat.deleteConversation(
            conversationWith,
            conversationType,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(result: String) {
                    continuation.resume(result)
                }
                
                override fun onError(exception: CometChatException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
    
    /**
     * Marks messages as delivered via CometChat SDK.
     * @param message The message to mark as delivered
     */
    override suspend fun markAsDelivered(message: BaseMessage) {
        CometChat.markAsDelivered(message)
    }
}
