package com.cometchat.uikit.core.testutils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.CardMessage
import com.cometchat.chat.models.User
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Test-only MockFactory for chatuikit-kotlin unit tests.
 *
 * The canonical MockFactory lives in chatuikit-core's test sources, which are not
 * available on the chatuikit-kotlin unit test classpath (only main sources are
 * shared via testImplementation(project(":chatuikit-core"))).
 *
 * This provides the same mock-creation utilities needed by card bubble tests.
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
        name: String = "Test User"
    ): User {
        val user = mock<User>()
        whenever(user.uid).thenReturn(uid)
        whenever(user.name).thenReturn(name)
        return user
    }

    // ==================== Card Message ====================

    /**
     * Creates a mock CardMessage for developer card (category "card") tests.
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
        // Convert cardJson to JSONObject if it's a Map, pass null/JSONObject through directly
        val jsonObj: org.json.JSONObject? = when (cardJson) {
            is org.json.JSONObject -> cardJson
            is Map<*, *> -> org.json.JSONObject(cardJson)
            is String -> if (cardJson.isNotEmpty()) {
                try { org.json.JSONObject(cardJson) } catch (_: Exception) { null }
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
}
