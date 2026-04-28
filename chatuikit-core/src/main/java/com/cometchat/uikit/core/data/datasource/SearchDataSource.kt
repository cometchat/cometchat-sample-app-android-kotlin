package com.cometchat.uikit.core.data.datasource

import com.cometchat.chat.core.ConversationsRequest
import com.cometchat.chat.core.MessagesRequest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation

/**
 * Interface defining data source operations for search functionality.
 * Lives in data layer - defines contract for data fetching.
 * Allows for different implementations (remote, local, mock).
 */
interface SearchDataSource {

    /**
     * Fetches conversations from the data source based on search criteria.
     * @param request The configured ConversationsRequest
     * @return Raw list of Conversation objects
     * @throws Exception if fetching fails
     */
    suspend fun fetchConversations(request: ConversationsRequest): List<Conversation>

    /**
     * Fetches messages from the data source based on search criteria.
     * @param request The configured MessagesRequest
     * @return Raw list of BaseMessage objects
     * @throws Exception if fetching fails
     */
    suspend fun fetchMessages(request: MessagesRequest): List<BaseMessage>
}
