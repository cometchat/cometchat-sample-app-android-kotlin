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

    // Avatar URL pattern
    private const val AVATAR_BASE_URL = "https://data-us.cometchat.io/assets/images/avatars/"

    // Timestamp offsets (seconds)
    private val NOW: Long get() = System.currentTimeMillis() / 1000
    private val FIVE_MINUTES_AGO: Long get() = NOW - (5 * 60)
    private val TWO_HOURS_AGO: Long get() = NOW - (2 * 60 * 60)
    private val YESTERDAY: Long get() = NOW - (24 * 60 * 60)
    private val TWO_DAYS_AGO: Long get() = NOW - (2 * 24 * 60 * 60)
    private val FIVE_DAYS_AGO: Long get() = NOW - (5 * 24 * 60 * 60)

    /**
     * Creates a mock User for preview purposes.
     * Uses the SDK's User class with setter methods.
     */
    fun createMockUser(
        uid: String = "user_1",
        name: String = "Iron Man",
        avatar: String? = "${AVATAR_BASE_URL}ironman.png",
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
        name: String = "The Avengers",
        icon: String? = "${AVATAR_BASE_URL}avengers.png",
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
        sentAt: Long = FIVE_MINUTES_AGO,
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
     * Uses superhero-themed names, realistic messages, varied timestamps, and receipt states.
     */
    fun createSampleConversations(): List<Conversation> = listOf(
        createUserConversation(
            user = createMockUser(
                uid = "1",
                name = "Iron Man",
                avatar = "${AVATAR_BASE_URL}ironman.png",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = createMockTextMessage(
                text = "Hey, are we meeting today?",
                sentAt = NOW,
                deliveredAt = NOW,
                readAt = NOW
            ),
            unreadCount = 3
        ),
        createGroupConversation(
            group = createMockGroup(
                guid = "1",
                name = "The Avengers",
                icon = "${AVATAR_BASE_URL}avengers.png",
                groupType = CometChatConstants.GROUP_TYPE_PRIVATE
            ),
            lastMessage = createMockTextMessage(
                text = "The build passed ✅",
                sentAt = FIVE_MINUTES_AGO,
                deliveredAt = FIVE_MINUTES_AGO,
                readAt = 0
            ),
            unreadCount = 0
        ),
        createUserConversation(
            user = createMockUser(
                uid = "2",
                name = "Captain America",
                avatar = "${AVATAR_BASE_URL}captainamerica.png",
                status = CometChatConstants.USER_STATUS_OFFLINE
            ),
            lastMessage = createMockTextMessage(
                text = "Sure, let me check my schedule",
                sentAt = TWO_HOURS_AGO,
                deliveredAt = TWO_HOURS_AGO,
                readAt = 0
            ),
            unreadCount = 1
        ),
        createGroupConversation(
            group = createMockGroup(
                guid = "2",
                name = "Justice League",
                icon = "${AVATAR_BASE_URL}justiceleague.png",
                groupType = CometChatConstants.GROUP_TYPE_PASSWORD
            ),
            lastMessage = createMockTextMessage(
                text = "Shared a photo",
                sentAt = YESTERDAY,
                deliveredAt = 0,
                readAt = 0
            ),
            unreadCount = 15
        ),
        createUserConversation(
            user = createMockUser(
                uid = "3",
                name = "Spiderman",
                avatar = "${AVATAR_BASE_URL}spiderman.png",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = createMockTextMessage(
                text = "Let's catch up tomorrow",
                sentAt = TWO_DAYS_AGO,
                deliveredAt = TWO_DAYS_AGO,
                readAt = TWO_DAYS_AGO
            ),
            unreadCount = 0
        )
    )

    
    /**
     * Creates a large list of conversations for pagination testing.
     * Uses superhero-themed names and realistic data.
     */
    fun createLargeConversationList(count: Int = 50): List<Conversation> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor", "Hulk",
            "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man", "Scarlet Witch",
            "Vision", "Falcon", "Winter Soldier", "War Machine", "Star-Lord"
        )
        val avatarKeys = listOf(
            "ironman", "captainamerica", "spiderman", "blackwidow", "thor", "hulk",
            "hawkeye", "blackpanther", "doctorstrange", "antman", "scarletwitch",
            "vision", "falcon", "wintersoldier", "warmachine", "starlord"
        )
        val messages = listOf(
            "Hey, are we meeting today?", "Sure, let me check my schedule",
            "The build passed ✅", "Shared a photo", "Let's catch up tomorrow",
            "On my way!", "Got it, thanks!", "Sounds good 👍",
            "Can you review my PR?", "Meeting in 5 minutes", "Let me check", "Talk soon!"
        )
        val groupNames = listOf(
            "The Avengers", "Justice League", "Design Team", "Developers Hub",
            "S.H.I.E.L.D.", "X-Men", "Guardians", "Fantastic Four",
            "Wakanda Tech", "Stark Industries"
        )
        val groupAvatarKeys = listOf(
            "avengers", "justiceleague", "designteam", "developershub",
            "shield", "xmen", "guardians", "fantasticfour",
            "wakandatech", "starkindustries"
        )
        val timestamps = listOf(NOW, FIVE_MINUTES_AGO, TWO_HOURS_AGO, YESTERDAY, TWO_DAYS_AGO, FIVE_DAYS_AGO)
        
        return (1..count).map { index ->
            val timestamp = timestamps[index % timestamps.size]
            if (index % 3 == 0) {
                // Every 3rd item is a group
                val groupIndex = index % groupNames.size
                createGroupConversation(
                    group = createMockGroup(
                        guid = "group_$index",
                        name = groupNames[groupIndex],
                        icon = "${AVATAR_BASE_URL}${groupAvatarKeys[groupIndex]}.png",
                        groupType = when (index % 3) {
                            0 -> CometChatConstants.GROUP_TYPE_PUBLIC
                            1 -> CometChatConstants.GROUP_TYPE_PRIVATE
                            else -> CometChatConstants.GROUP_TYPE_PASSWORD
                        }
                    ),
                    lastMessage = createMockTextMessage(
                        text = messages[index % messages.size],
                        sentAt = timestamp,
                        deliveredAt = if (index % 2 == 0) timestamp else 0,
                        readAt = if (index % 4 == 0) timestamp else 0
                    ),
                    unreadCount = if (index % 4 == 0) (1..20).random() else 0
                )
            } else {
                val nameIndex = index % names.size
                createUserConversation(
                    user = createMockUser(
                        uid = "user_$index",
                        name = names[nameIndex],
                        avatar = "${AVATAR_BASE_URL}${avatarKeys[nameIndex]}.png",
                        status = if (index % 2 == 0) CometChatConstants.USER_STATUS_ONLINE 
                                else CometChatConstants.USER_STATUS_OFFLINE
                    ),
                    lastMessage = createMockTextMessage(
                        text = messages[index % messages.size],
                        sentAt = timestamp,
                        deliveredAt = if (index % 3 == 0) timestamp else 0,
                        readAt = if (index % 5 == 0) timestamp else 0
                    ),
                    unreadCount = if (index % 5 == 0) (1..99).random() else 0
                )
            }
        }
    }
    
    /**
     * Creates conversations with high unread counts for badge testing.
     * Uses realistic superhero-themed names.
     */
    fun createHighUnreadConversations(): List<Conversation> = listOf(
        createUserConversation(
            user = createMockUser(
                uid = "1",
                name = "Iron Man",
                avatar = "${AVATAR_BASE_URL}ironman.png",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = createMockTextMessage(
                text = "We need to talk about the mission",
                sentAt = NOW
            ),
            unreadCount = 999
        ),
        createGroupConversation(
            group = createMockGroup(
                guid = "1",
                name = "The Avengers",
                icon = "${AVATAR_BASE_URL}avengers.png"
            ),
            lastMessage = createMockTextMessage(
                text = "Assemble! 🦸",
                sentAt = FIVE_MINUTES_AGO
            ),
            unreadCount = 500
        ),
        createUserConversation(
            user = createMockUser(
                uid = "2",
                name = "Captain America",
                avatar = "${AVATAR_BASE_URL}captainamerica.png",
                status = CometChatConstants.USER_STATUS_OFFLINE
            ),
            lastMessage = createMockTextMessage(
                text = "I can do this all day",
                sentAt = TWO_HOURS_AGO
            ),
            unreadCount = 50
        ),
        createUserConversation(
            user = createMockUser(
                uid = "3",
                name = "Spiderman",
                avatar = "${AVATAR_BASE_URL}spiderman.png",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = createMockTextMessage(
                text = "With great power comes great responsibility",
                sentAt = YESTERDAY
            ),
            unreadCount = 5
        ),
        createUserConversation(
            user = createMockUser(
                uid = "4",
                name = "Black Widow",
                avatar = "${AVATAR_BASE_URL}blackwidow.png",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = createMockTextMessage(
                text = "Mission complete ✓",
                sentAt = TWO_DAYS_AGO
            ),
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
                name = "The Avengers",
                icon = "${AVATAR_BASE_URL}avengers.png",
                groupType = CometChatConstants.GROUP_TYPE_PUBLIC
            ),
            lastMessage = createMockTextMessage(text = "Avengers assemble!", sentAt = NOW)
        ),
        createGroupConversation(
            group = createMockGroup(
                guid = "private_1",
                name = "Design Team",
                icon = "${AVATAR_BASE_URL}designteam.png",
                groupType = CometChatConstants.GROUP_TYPE_PRIVATE
            ),
            lastMessage = createMockTextMessage(text = "New mockups ready for review", sentAt = TWO_HOURS_AGO)
        ),
        createGroupConversation(
            group = createMockGroup(
                guid = "password_1",
                name = "Developers Hub",
                icon = "${AVATAR_BASE_URL}developershub.png",
                groupType = CometChatConstants.GROUP_TYPE_PASSWORD
            ),
            lastMessage = createMockTextMessage(text = "Sprint planning at 3 PM", sentAt = YESTERDAY)
        )
    )
    
    /**
     * Creates conversations with various user statuses.
     */
    fun createUserStatusConversations(): List<Conversation> = listOf(
        createUserConversation(
            user = createMockUser(
                uid = "online_1",
                name = "Thor",
                avatar = "${AVATAR_BASE_URL}thor.png",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = createMockTextMessage(text = "Bring me Thanos!", sentAt = NOW)
        ),
        createUserConversation(
            user = createMockUser(
                uid = "offline_1",
                name = "Hulk",
                avatar = "${AVATAR_BASE_URL}hulk.png",
                status = CometChatConstants.USER_STATUS_OFFLINE
            ),
            lastMessage = createMockTextMessage(text = "Hulk smash! 💪", sentAt = YESTERDAY)
        )
    )

    /**
     * Creates conversations showing all receipt states:
     * - Sent only (deliveredAt=0, readAt=0)
     * - Delivered (deliveredAt > 0, readAt=0)
     * - Read (deliveredAt > 0, readAt > 0)
     */
    fun createConversationsWithReceipts(): List<Conversation> = listOf(
        // Sent only - message sent but not yet delivered
        createUserConversation(
            user = createMockUser(
                uid = "receipt_1",
                name = "Iron Man",
                avatar = "${AVATAR_BASE_URL}ironman.png",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = createMockTextMessage(
                text = "Hey, are we meeting today?",
                sentAt = NOW,
                deliveredAt = 0,
                readAt = 0
            ),
            unreadCount = 0
        ),
        // Delivered - message delivered but not read
        createUserConversation(
            user = createMockUser(
                uid = "receipt_2",
                name = "Captain America",
                avatar = "${AVATAR_BASE_URL}captainamerica.png",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = createMockTextMessage(
                text = "Sure, let me check my schedule",
                sentAt = FIVE_MINUTES_AGO,
                deliveredAt = FIVE_MINUTES_AGO,
                readAt = 0
            ),
            unreadCount = 0
        ),
        // Read - message delivered and read
        createUserConversation(
            user = createMockUser(
                uid = "receipt_3",
                name = "Spiderman",
                avatar = "${AVATAR_BASE_URL}spiderman.png",
                status = CometChatConstants.USER_STATUS_OFFLINE
            ),
            lastMessage = createMockTextMessage(
                text = "The build passed ✅",
                sentAt = TWO_HOURS_AGO,
                deliveredAt = TWO_HOURS_AGO,
                readAt = TWO_HOURS_AGO
            ),
            unreadCount = 0
        ),
        // Sent only - another example
        createGroupConversation(
            group = createMockGroup(
                guid = "receipt_g1",
                name = "The Avengers",
                icon = "${AVATAR_BASE_URL}avengers.png"
            ),
            lastMessage = createMockTextMessage(
                text = "Shared a photo",
                sentAt = YESTERDAY,
                deliveredAt = 0,
                readAt = 0
            ),
            unreadCount = 2
        ),
        // Delivered - group message
        createGroupConversation(
            group = createMockGroup(
                guid = "receipt_g2",
                name = "Developers Hub",
                icon = "${AVATAR_BASE_URL}developershub.png"
            ),
            lastMessage = createMockTextMessage(
                text = "Let's catch up tomorrow",
                sentAt = TWO_DAYS_AGO,
                deliveredAt = TWO_DAYS_AGO,
                readAt = 0
            ),
            unreadCount = 0
        )
    )

    /**
     * Creates conversations with @mention text in messages.
     */
    fun createConversationsWithMentions(): List<Conversation> = listOf(
        createGroupConversation(
            group = createMockGroup(
                guid = "mention_1",
                name = "The Avengers",
                icon = "${AVATAR_BASE_URL}avengers.png"
            ),
            lastMessage = createMockTextMessage(
                text = "@Iron Man can you review the PR?",
                sentAt = NOW
            ),
            unreadCount = 1
        ),
        createGroupConversation(
            group = createMockGroup(
                guid = "mention_2",
                name = "Developers Hub",
                icon = "${AVATAR_BASE_URL}developershub.png"
            ),
            lastMessage = createMockTextMessage(
                text = "@Captain America @Spiderman meeting at 3 PM",
                sentAt = FIVE_MINUTES_AGO
            ),
            unreadCount = 3
        ),
        createGroupConversation(
            group = createMockGroup(
                guid = "mention_3",
                name = "Design Team",
                icon = "${AVATAR_BASE_URL}designteam.png"
            ),
            lastMessage = createMockTextMessage(
                text = "@all Please check the new designs",
                sentAt = TWO_HOURS_AGO
            ),
            unreadCount = 5
        ),
        createUserConversation(
            user = createMockUser(
                uid = "mention_u1",
                name = "Black Widow",
                avatar = "${AVATAR_BASE_URL}blackwidow.png",
                status = CometChatConstants.USER_STATUS_ONLINE
            ),
            lastMessage = createMockTextMessage(
                text = "@Thor where are you?",
                sentAt = YESTERDAY
            ),
            unreadCount = 0
        )
    )


    // ========================================================================
    // Users List Mock Data
    // ========================================================================

    /**
     * Creates a sample list of users for the Users component preview.
     * Includes a mix of online/offline users with superhero-themed names.
     */
    fun createSampleUsers(): List<User> = listOf(
        createMockUser(uid = "u1", name = "Iron Man", avatar = "${AVATAR_BASE_URL}ironman.png", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "u2", name = "Captain America", avatar = "${AVATAR_BASE_URL}captainamerica.png", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "u3", name = "Spiderman", avatar = "${AVATAR_BASE_URL}spiderman.png", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "u4", name = "Black Widow", avatar = "${AVATAR_BASE_URL}blackwidow.png", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "u5", name = "Thor", avatar = "${AVATAR_BASE_URL}thor.png", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "u6", name = "Hulk", avatar = "${AVATAR_BASE_URL}hulk.png", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "u7", name = "Hawkeye", avatar = "${AVATAR_BASE_URL}hawkeye.png", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "u8", name = "Black Panther", avatar = "${AVATAR_BASE_URL}blackpanther.png", status = CometChatConstants.USER_STATUS_ONLINE)
    )

    /**
     * Creates a large list of users for pagination/scroll testing.
     */
    fun createLargeUserList(count: Int = 30): List<User> {
        val names = listOf(
            "Iron Man", "Captain America", "Spiderman", "Black Widow", "Thor", "Hulk",
            "Hawkeye", "Black Panther", "Doctor Strange", "Ant-Man", "Scarlet Witch",
            "Vision", "Falcon", "Winter Soldier", "War Machine", "Star-Lord",
            "Gamora", "Drax", "Rocket", "Groot", "Nebula", "Mantis",
            "Shang-Chi", "Ms. Marvel", "Moon Knight", "She-Hulk"
        )
        val avatarKeys = listOf(
            "ironman", "captainamerica", "spiderman", "blackwidow", "thor", "hulk",
            "hawkeye", "blackpanther", "doctorstrange", "antman", "scarletwitch",
            "vision", "falcon", "wintersoldier", "warmachine", "starlord",
            "gamora", "drax", "rocket", "groot", "nebula", "mantis",
            "shangchi", "msmarvel", "moonknight", "shehulk"
        )
        return (1..count).map { index ->
            val nameIndex = index % names.size
            createMockUser(
                uid = "user_$index",
                name = names[nameIndex],
                avatar = "${AVATAR_BASE_URL}${avatarKeys[nameIndex]}.png",
                status = if (index % 3 == 0) CometChatConstants.USER_STATUS_OFFLINE
                         else CometChatConstants.USER_STATUS_ONLINE
            )
        }
    }

    /**
     * Creates users with only online status for testing.
     */
    fun createOnlineUsers(): List<User> = listOf(
        createMockUser(uid = "on1", name = "Iron Man", avatar = "${AVATAR_BASE_URL}ironman.png", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "on2", name = "Thor", avatar = "${AVATAR_BASE_URL}thor.png", status = CometChatConstants.USER_STATUS_ONLINE),
        createMockUser(uid = "on3", name = "Spiderman", avatar = "${AVATAR_BASE_URL}spiderman.png", status = CometChatConstants.USER_STATUS_ONLINE)
    )

    /**
     * Creates users with only offline status for testing.
     */
    fun createOfflineUsers(): List<User> = listOf(
        createMockUser(uid = "off1", name = "Captain America", avatar = "${AVATAR_BASE_URL}captainamerica.png", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "off2", name = "Black Widow", avatar = "${AVATAR_BASE_URL}blackwidow.png", status = CometChatConstants.USER_STATUS_OFFLINE),
        createMockUser(uid = "off3", name = "Hulk", avatar = "${AVATAR_BASE_URL}hulk.png", status = CometChatConstants.USER_STATUS_OFFLINE)
    )


    // ========================================================================
    // Groups List Mock Data
    // ========================================================================

    /**
     * Creates a sample list of groups for the Groups component preview.
     * Includes a mix of public, private, and password-protected groups.
     */
    fun createSampleGroups(): List<Group> = listOf(
        createMockGroup(guid = "g1", name = "The Avengers", icon = "${AVATAR_BASE_URL}avengers.png", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 12),
        createMockGroup(guid = "g2", name = "Design Team", icon = "${AVATAR_BASE_URL}designteam.png", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 6),
        createMockGroup(guid = "g3", name = "Developers Hub", icon = "${AVATAR_BASE_URL}developershub.png", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 3),
        createMockGroup(guid = "g4", name = "Justice League", icon = "${AVATAR_BASE_URL}justiceleague.png", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 25),
        createMockGroup(guid = "g5", name = "S.H.I.E.L.D.", icon = "${AVATAR_BASE_URL}shield.png", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8),
        createMockGroup(guid = "g6", name = "X-Men", icon = "${AVATAR_BASE_URL}xmen.png", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 50),
        createMockGroup(guid = "g7", name = "Stark Industries", icon = "${AVATAR_BASE_URL}starkindustries.png", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 5)
    )

    /**
     * Creates a large list of groups for pagination/scroll testing.
     */
    fun createLargeGroupList(count: Int = 30): List<Group> {
        val groupNames = listOf(
            "The Avengers", "Justice League", "Design Team", "Developers Hub",
            "S.H.I.E.L.D.", "X-Men", "Guardians", "Fantastic Four",
            "Wakanda Tech", "Stark Industries", "Asgardians", "Defenders",
            "Inhumans", "Eternals", "Thunderbolts"
        )
        val avatarKeys = listOf(
            "avengers", "justiceleague", "designteam", "developershub",
            "shield", "xmen", "guardians", "fantasticfour",
            "wakandatech", "starkindustries", "asgardians", "defenders",
            "inhumans", "eternals", "thunderbolts"
        )
        val types = listOf(
            CometChatConstants.GROUP_TYPE_PUBLIC,
            CometChatConstants.GROUP_TYPE_PRIVATE,
            CometChatConstants.GROUP_TYPE_PASSWORD
        )
        return (1..count).map { index ->
            val nameIndex = index % groupNames.size
            createMockGroup(
                guid = "group_$index",
                name = groupNames[nameIndex],
                icon = "${AVATAR_BASE_URL}${avatarKeys[nameIndex]}.png",
                groupType = types[index % types.size],
                membersCount = (2..50).random()
            )
        }
    }

    /**
     * Creates groups of only one type for testing group type indicators.
     */
    fun createPublicGroups(): List<Group> = listOf(
        createMockGroup(guid = "pub1", name = "The Avengers", icon = "${AVATAR_BASE_URL}avengers.png", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 100),
        createMockGroup(guid = "pub2", name = "X-Men", icon = "${AVATAR_BASE_URL}xmen.png", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 45),
        createMockGroup(guid = "pub3", name = "Justice League", icon = "${AVATAR_BASE_URL}justiceleague.png", groupType = CometChatConstants.GROUP_TYPE_PUBLIC, membersCount = 200)
    )

    fun createPrivateGroups(): List<Group> = listOf(
        createMockGroup(guid = "priv1", name = "S.H.I.E.L.D.", icon = "${AVATAR_BASE_URL}shield.png", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 8),
        createMockGroup(guid = "priv2", name = "Design Team", icon = "${AVATAR_BASE_URL}designteam.png", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 5),
        createMockGroup(guid = "priv3", name = "Wakanda Tech", icon = "${AVATAR_BASE_URL}wakandatech.png", groupType = CometChatConstants.GROUP_TYPE_PRIVATE, membersCount = 3)
    )

    fun createPasswordGroups(): List<Group> = listOf(
        createMockGroup(guid = "pwd1", name = "Stark Industries", icon = "${AVATAR_BASE_URL}starkindustries.png", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 4),
        createMockGroup(guid = "pwd2", name = "Developers Hub", icon = "${AVATAR_BASE_URL}developershub.png", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 10),
        createMockGroup(guid = "pwd3", name = "Thunderbolts", icon = "${AVATAR_BASE_URL}thunderbolts.png", groupType = CometChatConstants.GROUP_TYPE_PASSWORD, membersCount = 15)
    )
}
