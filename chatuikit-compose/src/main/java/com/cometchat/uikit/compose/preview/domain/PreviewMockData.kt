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

    // ========================================================================
    // Users List Mock Data
    // ========================================================================

    /**
     * Creates a sample list of users for the Users component preview.
     * Includes a mix of online/offline users with alphabetically varied names.
     */
    fun createSampleUsers(): List<User> = listOf(
        createMockUser(uid = "u1", name = "Alice Smith", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "u2", name = "Bob Johnson", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "u3", name = "Charlie Brown", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "u4", name = "Diana Prince", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "u5", name = "Edward Norton", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "u6", name = "Fiona Apple", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "u7", name = "George Lucas", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "u8", name = "Hannah Montana", status = CometChatConstants.USER_STATUS_ONLINE)
    )

    /**
     * Creates a large list of users for pagination/scroll testing.
     */
    fun createLargeUserList(count: Int = 30): List<User> {
        val firstNames = listOf(
            "Alice", "Bob", "Charlie", "Diana", "Edward", "Fiona", "George", "Hannah",
            "Ivan", "Julia", "Kevin", "Laura", "Michael", "Nancy", "Oscar", "Patricia",
            "Quinn", "Rachel", "Steve", "Tina", "Uma", "Victor", "Wendy", "Xavier",
            "Yolanda", "Zach"
        )
        val lastNames = listOf(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Anderson", "Taylor", "Thomas", "Moore"
        )
        return (1..count).map { index ->
            createMockUser(
                uid = "user_$index",
                name = "${firstNames[index % firstNames.size]} ${lastNames[index % lastNames.size]}",
                status = if (index % 3 == 0) CometChatConstants.USER_STATUS_OFFLINE
                         else CometChatConstants.USER_STATUS_ONLINE
            )
        }
    }

    /**
     * Creates users with only online status for testing.
     */
    fun createOnlineUsers(): List<User> = listOf(
        createMockUser(uid = "on1", name = "Alice Online", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "on2", name = "Bob Online", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "on3", name = "Charlie Online", status = CometChatConstants.USER_STATUS_ONLINE)
    )

    /**
     * Creates users with only offline status for testing.
     */
    fun createOfflineUsers(): List<User> = listOf(
        createMockUser(uid = "off1", name = "Diana Offline", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "off2", name = "Edward Offline", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "off3", name = "Fiona Offline", status = CometChatConstants.USER_STATUS_OFFLINE)
    )

    // ========================================================================
    // Groups List Mock Data
    // ========================================================================

    /**
     * Creates a sample list of groups for the Groups component preview.
     * Includes a mix of public, private, and password-protected groups.
     */
    fun createSampleGroups(): List<Group> = listOf(
        createMockGroup(guid = "g1", name = "Engineering Team", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 12),
        createMockGroup(guid = "g2", name = "Design Squad", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 6),
        createMockGroup(guid = "g3", name = "VIP Lounge", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 3),
        createMockGroup(guid = "g4", name = "Marketing Hub", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 25),
        createMockGroup(guid = "g5", name = "Project Alpha", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8),
        createMockGroup(guid = "g6", name = "Support Channel", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 50),
        createMockGroup(guid = "g7", name = "Executive Board", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 5)
    )

    /**
     * Creates a large list of groups for pagination/scroll testing.
     */
    fun createLargeGroupList(count: Int = 30): List<Group> {
        val groupNames = listOf(
            "Engineering", "Design", "Marketing", "Sales", "Support",
            "Product", "HR", "Finance", "Legal", "Operations",
            "Research", "QA", "DevOps", "Security", "Analytics"
        )
        val types = listOf(
            CometChatConstants.GROUP_TYPE_PUBLIC,
            CometChatConstants.GROUP_TYPE_PRIVATE,
            CometChatConstants.GROUP_TYPE_PASSWORD
        )
        return (1..count).map { index ->
            createMockGroup(
                guid = "group_$index",
                name = "${groupNames[index % groupNames.size]} Team ${index / groupNames.size + 1}",
                groupType = types[index % types.size],
                membersCount = (2..50).random()
            )
        }
    }

    /**
     * Creates groups of only one type for testing group type indicators.
     */
    fun createPublicGroups(): List<Group> = listOf(
        createMockGroup(guid = "pub1", name = "Open Community", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 100),
        createMockGroup(guid = "pub2", name = "General Chat", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 45),
        createMockGroup(guid = "pub3", name = "Announcements", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 200)
    )

    fun createPrivateGroups(): List<Group> = listOf(
        createMockGroup(guid = "priv1", name = "Core Team", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8),
        createMockGroup(guid = "priv2", name = "Leadership", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 5),
        createMockGroup(guid = "priv3", name = "Founders", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 3)
    )

    fun createPasswordGroups(): List<Group> = listOf(
        createMockGroup(guid = "pwd1", name = "Secret Project", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 4),
        createMockGroup(guid = "pwd2", name = "VIP Access", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 10),
        createMockGroup(guid = "pwd3", name = "Beta Testers", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 15)
    )
}
