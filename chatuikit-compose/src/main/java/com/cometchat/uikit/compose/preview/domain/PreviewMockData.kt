package com.cometchat.uikit.compose.preview.domain

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User

/**
 * Preview-safe mock data factory for Compose previews.
 * 
 * This object provides factory methods to create CometChat SDK model instances
 * for use in @Preview composables. The SDK models are created using reflection
 * and setter methods to avoid any SDK initialization requirements.
 * 
 * Note: These mocks are only for preview purposes and should not be used
 * in production code.
 */
object PreviewMockData {
    
    /**
     * Creates a mock User for preview purposes.
     * Uses the SDK's User class with setter methods.
     */
    fun createMockUser(
        uid: String = "user_1",
        name: String = "John Doe",
        avatar: String? = null,
        status: String = CometChatConstants.USER_STATUS_ONLINE
    ): User {
        return User().apply {
            this.uid = uid
            this.name = name
            this.avatar = avatar
            this.status = status
        }
    }
    
    /**
     * Creates a mock Group for preview purposes.
     */
    fun createMockGroup(
        guid: String = "group_1",
        name: String = "Team Chat",
        icon: String? = null,
        groupType: String = CometChatConstants.GROUP_TYPE_PUBLIC,
        membersCount: Int = 5
    ): Group {
        return Group().apply {
            this.guid = guid
            this.name = name
            this.icon = icon
            this.groupType = groupType
            this.membersCount = membersCount
        }
    }
    
    /**
     * Creates a mock TextMessage for preview purposes.
     */
    fun createMockTextMessage(
        id: Long = 1L,
        text: String = "Hello! How are you?",
        sentAt: Long = System.currentTimeMillis() / 1000,
        deliveredAt: Long = 0,
        readAt: Long = 0,
        sender: User? = null
    ): TextMessage {
        val receiverId = "receiver_1"
        val receiverType = CometChatConstants.RECEIVER_TYPE_USER
        return TextMessage(receiverId, text, receiverType).apply {
            this.id = id
            this.sentAt = sentAt
            this.deliveredAt = deliveredAt
            this.readAt = readAt
            this.sender = sender ?: createMockUser()
        }
    }
    
    /**
     * Creates a mock Conversation with a User.
     */
    fun createUserConversation(
        user: User = createMockUser(),
        lastMessage: TextMessage? = createMockTextMessage(),
        unreadCount: Int = 0
    ): Conversation {
        val conversationId = "conv_user_${user.uid}"
        val conversationType = CometChatConstants.CONVERSATION_TYPE_USER
        return Conversation(conversationId, conversationType).apply {
            this.conversationWith = user
            this.lastMessage = lastMessage
            this.unreadMessageCount = unreadCount
        }
    }
    
    /**
     * Creates a mock Conversation with a Group.
     */
    fun createGroupConversation(
        group: Group = createMockGroup(),
        lastMessage: TextMessage? = createMockTextMessage(),
        unreadCount: Int = 0
    ): Conversation {
        val conversationId = "conv_group_${group.guid}"
        val conversationType = CometChatConstants.CONVERSATION_TYPE_GROUP
        return Conversation(conversationId, conversationType).apply {
            this.conversationWith = group
            this.lastMessage = lastMessage
            this.unreadMessageCount = unreadCount
        }
    }
    
    /**
     * Creates a list of sample conversations for preview.
     */
    fun createSampleConversations(): List<Conversation> = listOf(
        createUserConversation(
            user = createMockUser(uid = "1", name = "Alice Smith", status = CometChatConstants.USER_STATUS_ONLINE),
            lastMessage = createMockTextMessage(text = "Hey! Are you coming to the meeting?"),
            unreadCount = 3
        ),
        createGroupConversation(
            group = createMockGroup(guid = "1", name = "Engineering Team", groupType = CometChatConstants.GROUP_TYPE_PRIVATE),
            lastMessage = createMockTextMessage(text = "The deployment is complete!"),
            unreadCount = 0
        ),
        createUserConversation(
            user = createMockUser(uid = "2", name = "Bob Johnson", status = CometChatConstants.USER_STATUS_OFFLINE),
            lastMessage = createMockTextMessage(text = "See you tomorrow!"),
            unreadCount = 1
        ),
        createGroupConversation(
            group = createMockGroup(guid = "2", name = "Project Alpha", groupType = CometChatConstants.GROUP_TYPE_PASSWORD),
            lastMessage = createMockTextMessage(text = "Let's discuss the roadmap"),
            unreadCount = 15
        ),
        createUserConversation(
            user = createMockUser(uid = "3", name = "Charlie Brown", status = CometChatConstants.USER_STATUS_ONLINE),
            lastMessage = createMockTextMessage(text = "Thanks for your help!"),
            unreadCount = 0
        )
    )
    
    /**
     * Creates a large list of conversations for pagination testing.
     */
    fun createLargeConversationList(count: Int = 50): List<Conversation> {
        val names = listOf(
            "Alice", "Bob", "Charlie", "Diana", "Edward", "Fiona", "George", "Hannah",
            "Ivan", "Julia", "Kevin", "Laura", "Michael", "Nancy", "Oscar", "Patricia"
        )
        val messages = listOf(
            "Hey there!", "How are you?", "See you later!", "Thanks!",
            "Got it!", "On my way", "Let me check", "Sounds good!",
            "Perfect!", "Will do", "No problem", "Talk soon"
        )
        val groupNames = listOf(
            "Engineering", "Design", "Marketing", "Sales", "Support",
            "Product", "HR", "Finance", "Legal", "Operations"
        )
        
        return (1..count).map { index ->
            if (index % 3 == 0) {
                // Every 3rd item is a group
                createGroupConversation(
                    group = createMockGroup(
                        guid = "group_$index",
                        name = "${groupNames[index % groupNames.size]} Team",
                        groupType = when (index % 3) {
                            0 -> CometChatConstants.GROUP_TYPE_PUBLIC
                            1 -> CometChatConstants.GROUP_TYPE_PRIVATE
                            else -> CometChatConstants.GROUP_TYPE_PASSWORD
                        }
                    ),
                    lastMessage = createMockTextMessage(text = messages[index % messages.size]),
                    unreadCount = if (index % 4 == 0) (1..20).random() else 0
                )
            } else {
                createUserConversation(
                    user = createMockUser(
                        uid = "user_$index",
                        name = "${names[index % names.size]} ${('A'..'Z').random()}.",
                        status = if (index % 2 == 0) CometChatConstants.USER_STATUS_ONLINE 
                                else CometChatConstants.USER_STATUS_OFFLINE
                    ),
                    lastMessage = createMockTextMessage(text = messages[index % messages.size]),
                    unreadCount = if (index % 5 == 0) (1..99).random() else 0
                )
            }
        }
    }
    
    /**
     * Creates conversations with high unread counts for badge testing.
     */
    fun createHighUnreadConversations(): List<Conversation> = listOf(
        createUserConversation(
            user = createMockUser(uid = "1", name = "Very Active User"),
            unreadCount = 999
        ),
        createGroupConversation(
            group = createMockGroup(guid = "1", name = "Busy Group"),
            unreadCount = 500
        ),
        createUserConversation(
            user = createMockUser(uid = "2", name = "Moderate Activity"),
            unreadCount = 50
        ),
        createUserConversation(
            user = createMockUser(uid = "3", name = "Low Activity"),
            unreadCount = 5
        ),
        createUserConversation(
            user = createMockUser(uid = "4", name = "No Unread"),
            unreadCount = 0
        )
    )
    
    /**
     * Creates conversations with various group types for testing.
     */
    fun createGroupTypeConversations(): List<Conversation> = listOf(
        createGroupConversation(
            group = createMockGroup(
                guid = "public_1",
                name = "Public Community",
                groupType = CometChatConstants.GROUP_TYPE_PUBLIC
            )
        ),
        createGroupConversation(
            group = createMockGroup(
                guid = "private_1",
                name = "Private Team",
                groupType = CometChatConstants.GROUP_TYPE_PRIVATE
            )
        ),
        createGroupConversation(
            group = createMockGroup(
                guid = "password_1",
                name = "Protected Channel",
                groupType = CometChatConstants.GROUP_TYPE_PASSWORD
            )
        )
    )
    
    /**
     * Creates conversations with various user statuses.
     */
    fun createUserStatusConversations(): List<Conversation> = listOf(
        createUserConversation(
            user = createMockUser(
                uid = "online_1",
                name = "Online User",
                status = CometChatConstants.USER_STATUS_ONLINE
            )
        ),
        createUserConversation(
            user = createMockUser(
                uid = "offline_1",
                name = "Offline User",
                status = CometChatConstants.USER_STATUS_OFFLINE
            )
        )
    )
}
