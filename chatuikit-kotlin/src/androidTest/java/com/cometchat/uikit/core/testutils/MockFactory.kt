package com.cometchat.uikit.core.testutils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CardMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import org.json.JSONObject
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

    /**
     * Default developer-card payload used by [createCardMessage] — a realistic
     * "order shipped" card (heading + Track Order button). This is the value of
     * the message's `card` field.
     */
    const val DEFAULT_CARD_JSON: String =
        """{"version":"1.0","body":[{"id":"txt_1","type":"text","content":"Your order has shipped!","variant":"heading3"},{"id":"btn_1","type":"button","label":"Track Order","action":{"type":"openUrl","url":"https://example.com/track"}}],"fallbackText":"Your order has shipped! Track at https://example.com/track","style":{"borderRadius":12,"padding":12}}"""

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

    fun createUserConversations(count: Int, prefix: String = "user"): List<Conversation> {
        return (1..count).map { i ->
            createUserConversation(uid = "$prefix-$i", name = "User $i")
        }
    }

    fun createGroupConversations(count: Int, prefix: String = "group"): List<Conversation> {
        return (1..count).map { i ->
            createGroupConversation(guid = "$prefix-$i", groupName = "Group $i")
        }
    }

    fun createMixedConversations(count: Int): List<Conversation> {
        val userCount = count / 2
        val groupCount = count - userCount
        return createUserConversations(userCount) + createGroupConversations(groupCount)
    }

    // ==================== Messages ====================

    fun createTextMessage(
        id: Long = 1L,
        text: String = "Hello",
        senderUid: String = "user-1",
        receiverId: String = "user-2",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        sentAt: Long = System.currentTimeMillis() / 1000,
        readAt: Long = 0L,
        deliveredAt: Long = 0L,
        deletedAt: Long = 0L,
        editedAt: Long = 0L,
        parentMessageId: Long = 0L
    ): TextMessage {
        val sender = createUser(uid = senderUid, name = "Sender")
        val message = mock<TextMessage>()
        whenever(message.id).thenReturn(id)
        whenever(message.text).thenReturn(text)
        whenever(message.sender).thenReturn(sender)
        whenever(message.receiverUid).thenReturn(receiverId)
        whenever(message.receiverType).thenReturn(receiverType)
        whenever(message.sentAt).thenReturn(sentAt)
        whenever(message.readAt).thenReturn(readAt)
        whenever(message.deliveredAt).thenReturn(deliveredAt)
        whenever(message.deletedAt).thenReturn(deletedAt)
        whenever(message.editedAt).thenReturn(editedAt)
        whenever(message.parentMessageId).thenReturn(parentMessageId)
        whenever(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
        whenever(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
        return message
    }

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

    // ==================== Card Message ====================

    /**
     * Creates a mock CardMessage for developer card (category "card") tests.
     *
     * @param id Message ID
     * @param type Card type identifier
     * @param cardJson The card payload — accepts Map, JSONObject, JSON String, or null
     * @param text Plain text fallback
     * @param fallbackText Specific fallback text (higher priority than text)
     * @param senderUid Sender user ID
     * @param receiverId Receiver ID
     * @param receiverType Receiver type (user/group)
     * @param sentAt Sent timestamp
     * @param deletedAt Timestamp when the message was deleted (0 = not deleted)
     */
    fun createCardMessage(
        id: Long = 1L,
        type: String = "",
        cardJson: Any? = DEFAULT_CARD_JSON,
        text: String? = "Check out this card",
        fallbackText: String? = "Card Message",
        senderUid: String = "user-1",
        receiverId: String = "user-2",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        sentAt: Long = System.currentTimeMillis() / 1000,
        deletedAt: Long = 0L
    ): CardMessage {
        val sender = createUser(uid = senderUid, name = "Sender")
        val message = mock<CardMessage>()
        whenever(message.id).thenReturn(id)
        whenever(message.type).thenReturn(type)
        whenever(message.category).thenReturn("card")
        val jsonObj: JSONObject? = when (cardJson) {
            is JSONObject -> cardJson
            is Map<*, *> -> JSONObject(cardJson)
            is String -> if (cardJson.isNotEmpty()) {
                try { JSONObject(cardJson) } catch (_: Exception) { null }
            } else null
            else -> null
        }
        whenever(message.card).thenReturn(jsonObj)
        whenever(message.text).thenReturn(text)
        whenever(message.fallbackText).thenReturn(fallbackText)
        whenever(message.sender).thenReturn(sender)
        whenever(message.receiverUid).thenReturn(receiverId)
        whenever(message.receiverType).thenReturn(receiverType)
        whenever(message.sentAt).thenReturn(sentAt)
        whenever(message.readAt).thenReturn(0L)
        whenever(message.deliveredAt).thenReturn(0L)
        whenever(message.deletedAt).thenReturn(deletedAt)
        whenever(message.editedAt).thenReturn(0L)
        whenever(message.parentMessageId).thenReturn(0L)
        return message
    }

    /**
     * Creates a CardMessage with an empty/null card payload (triggers fallback rendering).
     */
    fun createEmptyCardMessage(
        id: Long = 1L,
        text: String? = null,
        fallbackText: String? = null
    ): CardMessage {
        return createCardMessage(
            id = id,
            cardJson = null,
            text = text,
            fallbackText = fallbackText
        )
    }

    // ==================== Exceptions ====================

    fun createCometChatException(
        code: String = "ERROR",
        message: String = "Test error"
    ): CometChatException {
        return CometChatException(code, message)
    }
}
