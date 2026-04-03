package com.cometchat.uikit.core.domain.usecase

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.repository.ConversationListRepository

/**
 * Use case for deleting a conversation.
 * Contains business logic for extracting conversation ID and type before deletion.
 * 
 * @param repository The repository to perform deletion
 */
open class DeleteConversationUseCase(
    private val repository: ConversationListRepository
) {
    /**
     * Deletes the specified conversation.
     * Extracts the conversation ID based on conversation type (user/group).
     * 
     * @param conversation The conversation to delete
     * @return Result indicating success or failure
     */
    open suspend operator fun invoke(conversation: Conversation): Result<Unit> {
        val conversationWith = when (conversation.conversationType) {
            CometChatConstants.CONVERSATION_TYPE_USER -> {
                (conversation.conversationWith as? User)?.uid
                    ?: return Result.failure(IllegalArgumentException("Invalid user conversation"))
            }
            CometChatConstants.CONVERSATION_TYPE_GROUP -> {
                (conversation.conversationWith as? Group)?.guid
                    ?: return Result.failure(IllegalArgumentException("Invalid group conversation"))
            }
            else -> return Result.failure(IllegalArgumentException("Invalid conversation type: ${conversation.conversationType}"))
        }
        
        return repository.deleteConversation(
            conversationWith = conversationWith,
            conversationType = conversation.conversationType
        )
    }
}
