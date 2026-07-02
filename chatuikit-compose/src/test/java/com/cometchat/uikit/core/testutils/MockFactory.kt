package com.cometchat.uikit.core.testutils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CardMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import org.json.JSONObject
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Shared MockFactory for chatuikit-compose unit test source set.
 * Mirrors the MockFactory from chatuikit-core/src/test/ since
 * unit tests cannot access test source sets of other modules.
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
        status: String = CometChatConstants.USER_STATUS_ONLINE,
        isBlockedByMe: Boolean = false,
        hasBlockedMe: Boolean = false
    ): User {
        val user = mock<User>()
        whenever(user.uid).thenReturn(uid)
        whenever(user.name).thenReturn(name)
        whenever(user.status).thenReturn(status)
        whenever(user.avatar).thenReturn(null)
        whenever(user.isBlockedByMe).thenReturn(isBlockedByMe)
        whenever(user.isHasBlockedMe).thenReturn(hasBlockedMe)
        return user
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

    // ==================== Card Message ====================

    /**
     * Creates a mock CardMessage for developer card (category "card") tests.
     *
     * @param id Message ID
     * @param type Developer-chosen type (arbitrary — routing ignores this)
     * @param cardJson Raw card JSON payload from getCard() (null = empty/fallback)
     * @param text Text preview for conversation list / quoted reply
     * @param fallbackText Fallback text when renderer fails
     * @param senderUid UID of the sender
     * @param receiverId ID of the receiver (user UID or group GUID)
     * @param receiverType Receiver type — RECEIVER_TYPE_USER or RECEIVER_TYPE_GROUP
     * @param sentAt Timestamp when the message was sent (epoch seconds)
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

        // Convert cardJson to a mocked JSONObject for test use (org.json is not
        // implemented in plain JVM unit tests, so we stub toString()).
        val jsonObject: JSONObject? = when (cardJson) {
            null -> null
            is JSONObject -> cardJson
            is String -> if (cardJson.isEmpty()) null else mock<JSONObject>().also {
                whenever(it.toString()).thenReturn(cardJson)
            }
            is Map<*, *> -> {
                val mockJson = mock<JSONObject>()
                // Use a simple JSON-like string representation for test assertions
                val jsonStr = cardJson.entries.joinToString(", ", "{", "}") { (k, v) -> "\"$k\":\"$v\"" }
                whenever(mockJson.toString()).thenReturn(jsonStr)
                mockJson
            }
            else -> null
        }
        whenever(message.card).thenReturn(jsonObject)
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
