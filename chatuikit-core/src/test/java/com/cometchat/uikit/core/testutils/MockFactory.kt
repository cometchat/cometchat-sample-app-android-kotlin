package com.cometchat.uikit.core.testutils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Shared factory for creating mock CometChat SDK objects.
 * Used across all test classes to avoid duplication.
 *
 * SDK classes have private constructors — Mockito creates proxy objects
 * that return whatever we configure via `whenever().thenReturn()`.
 */
object MockFactory {

    // ==================== User ====================

    fun createUser(
        uid: String = "user-1",
        name: String = "Test User",
        status: String = "online",
        isBlockedByMe: Boolean = false,
        hasBlockedMe: Boolean = false
    ): User {
        val user = mock<User>()
        whenever(user.uid).thenReturn(uid)
        whenever(user.name).thenReturn(name)
        whenever(user.status).thenReturn(status)
        whenever(user.isBlockedByMe).thenReturn(isBlockedByMe)
        whenever(user.isHasBlockedMe).thenReturn(hasBlockedMe)
        return user
    }

    fun createUsers(count: Int, prefix: String = "user"): List<User> {
        return (1..count).map { i ->
            createUser(uid = "$prefix-$i", name = "User $i")
        }
    }

    // ==================== Group ====================

    fun createGroup(
        guid: String = "group-1",
        name: String = "Test Group",
        type: String = CometChatConstants.GROUP_TYPE_PUBLIC,
        membersCount: Int = 5
    ): Group {
        val group = mock<Group>()
        whenever(group.guid).thenReturn(guid)
        whenever(group.name).thenReturn(name)
        whenever(group.groupType).thenReturn(type)
        whenever(group.membersCount).thenReturn(membersCount)
        return group
    }

    // ==================== Conversation ====================

    /**
     * Creates a mock Conversation for a user chat.
     *
     * @param id Conversation ID (defaults to "conv_user-{uid}")
     * @param user The User entity for conversationWith
     * @param unreadCount Unread message count
     * @param lastMessage Optional last message in the conversation
     */
    fun createUserConversation(
        uid: String = "user-1",
        name: String = "Test User",
        id: String? = null,
        unreadCount: Int = 0,
        lastMessage: BaseMessage? = null
    ): Conversation {
        val user = createUser(uid = uid, name = name)
        val conversation = mock<Conversation>()
        val conversationId = id ?: "conv_user-$uid"
        whenever(conversation.conversationId).thenReturn(conversationId)
        whenever(conversation.conversationType).thenReturn(CometChatConstants.RECEIVER_TYPE_USER)
        whenever(conversation.conversationWith).thenReturn(user)
        whenever(conversation.unreadMessageCount).thenReturn(unreadCount)
        whenever(conversation.lastMessage).thenReturn(lastMessage)
        return conversation
    }

    /**
     * Creates a mock Conversation for a group chat.
     *
     * @param guid Group GUID
     * @param groupName Group display name
     * @param id Conversation ID (defaults to "conv_group-{guid}")
     * @param unreadCount Unread message count
     * @param lastMessage Optional last message in the conversation
     */
    fun createGroupConversation(
        guid: String = "group-1",
        groupName: String = "Test Group",
        id: String? = null,
        unreadCount: Int = 0,
        lastMessage: BaseMessage? = null
    ): Conversation {
        val group = createGroup(guid = guid, name = groupName)
        val conversation = mock<Conversation>()
        val conversationId = id ?: "conv_group-$guid"
        whenever(conversation.conversationId).thenReturn(conversationId)
        whenever(conversation.conversationType).thenReturn(CometChatConstants.RECEIVER_TYPE_GROUP)
        whenever(conversation.conversationWith).thenReturn(group)
        whenever(conversation.unreadMessageCount).thenReturn(unreadCount)
        whenever(conversation.lastMessage).thenReturn(lastMessage)
        return conversation
    }

    /**
     * Creates a list of mock user conversations.
     * Each conversation has a unique user with uid "user-{i}" and conversationId "conv_user-user-{i}".
     */
    fun createUserConversations(count: Int, prefix: String = "user"): List<Conversation> {
        return (1..count).map { i ->
            createUserConversation(uid = "$prefix-$i", name = "User $i")
        }
    }

    /**
     * Creates a list of mock group conversations.
     * Each conversation has a unique group with guid "group-{i}" and conversationId "conv_group-group-{i}".
     */
    fun createGroupConversations(count: Int, prefix: String = "group"): List<Conversation> {
        return (1..count).map { i ->
            createGroupConversation(guid = "$prefix-$i", groupName = "Group $i")
        }
    }

    /**
     * Creates a mixed list of user and group conversations.
     * First half are user conversations, second half are group conversations.
     */
    fun createMixedConversations(count: Int): List<Conversation> {
        val userCount = count / 2
        val groupCount = count - userCount
        return createUserConversations(userCount) + createGroupConversations(groupCount)
    }

    // ==================== Messages ====================

    /**
     * Creates a mock TextMessage.
     *
     * @param id Message ID
     * @param text Message text content
     * @param senderUid UID of the sender (creates a User mock)
     * @param receiverUid UID of the receiver
     * @param parentMessageId Parent message ID (>0 means threaded reply)
     */
    fun createTextMessage(
        id: Long = 1L,
        text: String = "Hello",
        senderUid: String = "user-1",
        receiverUid: String = "user-2",
        parentMessageId: Long = 0L
    ): TextMessage {
        val sender = createUser(uid = senderUid, name = "Sender")
        val message = mock<TextMessage>()
        whenever(message.id).thenReturn(id)
        whenever(message.text).thenReturn(text)
        whenever(message.sender).thenReturn(sender)
        whenever(message.receiverUid).thenReturn(receiverUid)
        whenever(message.parentMessageId).thenReturn(parentMessageId)
        whenever(message.readAt).thenReturn(0L)
        whenever(message.deliveredAt).thenReturn(0L)
        return message
    }

    /**
     * Creates a mock BaseMessage (generic message).
     *
     * @param id Message ID
     * @param senderUid UID of the sender
     * @param parentMessageId Parent message ID (>0 means threaded reply)
     */
    fun createBaseMessage(
        id: Long = 1L,
        senderUid: String = "user-1",
        parentMessageId: Long = 0L
    ): BaseMessage {
        val sender = createUser(uid = senderUid, name = "Sender")
        val message = mock<BaseMessage>()
        whenever(message.id).thenReturn(id)
        whenever(message.sender).thenReturn(sender)
        whenever(message.parentMessageId).thenReturn(parentMessageId)
        whenever(message.readAt).thenReturn(0L)
        whenever(message.deliveredAt).thenReturn(0L)
        return message
    }

    // ==================== Typing Indicator ====================

    /**
     * Creates a mock TypingIndicator.
     *
     * @param senderUid UID of the user who is typing
     * @param receiverId ID of the receiver (user UID or group GUID)
     * @param receiverType "user" or "group"
     */
    fun createTypingIndicator(
        senderUid: String = "user-1",
        receiverId: String = "user-2",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER
    ): TypingIndicator {
        val sender = createUser(uid = senderUid, name = "Typing User")
        val indicator = mock<TypingIndicator>()
        whenever(indicator.sender).thenReturn(sender)
        whenever(indicator.receiverId).thenReturn(receiverId)
        whenever(indicator.receiverType).thenReturn(receiverType)
        return indicator
    }

    // ==================== Exceptions ====================

    fun createCometChatException(
        code: String = "ERROR",
        message: String = "Test error"
    ): CometChatException {
        return CometChatException(code, message)
    }
}
