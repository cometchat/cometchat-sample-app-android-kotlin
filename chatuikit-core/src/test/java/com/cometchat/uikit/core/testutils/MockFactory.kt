package com.cometchat.uikit.core.testutils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.Action
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.Reaction
import com.cometchat.chat.models.ReactionEvent
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.domain.model.CometChatMessageOption
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
     * "order shipped" card (heading + Track Order button) with its own fallbackText
     * and style. This is the value of the message's `card` field.
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
     * Creates a mock TextMessage with full control over timestamps and metadata.
     * Supports MessageList test scenarios including pagination, receipts, threading,
     * deleted/edited states, and receiver type (user vs group).
     *
     * @param id Message ID
     * @param text Message text content
     * @param senderUid UID of the sender (creates a User mock)
     * @param receiverId ID of the receiver (user UID or group GUID)
     * @param receiverType Receiver type — CometChatConstants.RECEIVER_TYPE_USER or RECEIVER_TYPE_GROUP
     * @param sentAt Timestamp when the message was sent (epoch seconds)
     * @param readAt Timestamp when the message was read (0 = unread)
     * @param deliveredAt Timestamp when the message was delivered (0 = not delivered)
     * @param deletedAt Timestamp when the message was deleted (0 = not deleted)
     * @param editedAt Timestamp when the message was last edited (0 = not edited)
     * @param parentMessageId Parent message ID (>0 means threaded reply)
     */
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

    /**
     * Creates a mock MediaMessage for image, video, audio, or file messages.
     * Used in MessageList tests for rendering different bubble types.
     *
     * @param id Message ID
     * @param type Media type — MESSAGE_TYPE_IMAGE, MESSAGE_TYPE_VIDEO, MESSAGE_TYPE_AUDIO, MESSAGE_TYPE_FILE
     * @param senderUid UID of the sender
     * @param receiverId ID of the receiver (user UID or group GUID)
     * @param receiverType Receiver type — RECEIVER_TYPE_USER or RECEIVER_TYPE_GROUP
     * @param sentAt Timestamp when the message was sent (epoch seconds)
     */
    fun createMediaMessage(
        id: Long = 1L,
        type: String = CometChatConstants.MESSAGE_TYPE_IMAGE,
        senderUid: String = "user-1",
        receiverId: String = "user-2",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        sentAt: Long = System.currentTimeMillis() / 1000
    ): MediaMessage {
        val sender = createUser(uid = senderUid, name = "Sender")
        val message = mock<MediaMessage>()
        whenever(message.id).thenReturn(id)
        whenever(message.type).thenReturn(type)
        whenever(message.category).thenReturn(CometChatConstants.CATEGORY_MESSAGE)
        whenever(message.sender).thenReturn(sender)
        whenever(message.receiverUid).thenReturn(receiverId)
        whenever(message.receiverType).thenReturn(receiverType)
        whenever(message.sentAt).thenReturn(sentAt)
        whenever(message.readAt).thenReturn(0L)
        whenever(message.deliveredAt).thenReturn(0L)
        whenever(message.deletedAt).thenReturn(0L)
        whenever(message.editedAt).thenReturn(0L)
        whenever(message.parentMessageId).thenReturn(0L)
        return message
    }

    /**
     * Creates a mock CustomMessage for custom message types (polls, stickers, meetings, etc.).
     *
     * @param id Message ID
     * @param customType Custom type string (e.g., "extension_poll", "extension_sticker", "meeting")
     * @param senderUid UID of the sender
     * @param receiverId ID of the receiver (user UID or group GUID)
     * @param receiverType Receiver type — RECEIVER_TYPE_USER or RECEIVER_TYPE_GROUP
     * @param sentAt Timestamp when the message was sent (epoch seconds)
     */
    fun createCustomMessage(
        id: Long = 1L,
        customType: String = "extension_poll",
        senderUid: String = "user-1",
        receiverId: String = "user-2",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        sentAt: Long = System.currentTimeMillis() / 1000
    ): CustomMessage {
        val sender = createUser(uid = senderUid, name = "Sender")
        val message = mock<CustomMessage>()
        whenever(message.id).thenReturn(id)
        whenever(message.type).thenReturn(customType)
        whenever(message.category).thenReturn(CometChatConstants.CATEGORY_CUSTOM)
        whenever(message.sender).thenReturn(sender)
        whenever(message.receiverUid).thenReturn(receiverId)
        whenever(message.receiverType).thenReturn(receiverType)
        whenever(message.sentAt).thenReturn(sentAt)
        whenever(message.readAt).thenReturn(0L)
        whenever(message.deliveredAt).thenReturn(0L)
        whenever(message.deletedAt).thenReturn(0L)
        whenever(message.editedAt).thenReturn(0L)
        whenever(message.parentMessageId).thenReturn(0L)
        return message
    }

    /**
     * Creates a mock Action message for group action events (member joined, kicked, banned, etc.).
     *
     * @param id Message ID
     * @param action Action string (e.g., "joined", "kicked", "banned", "left")
     * @param actionOn The BaseMessage or User the action was performed on
     * @param senderUid UID of the user who performed the action
     * @param receiverId Group GUID where the action occurred
     * @param sentAt Timestamp when the action occurred (epoch seconds)
     */
    fun createActionMessage(
        id: Long = 1L,
        action: String = CometChatConstants.ActionKeys.ACTION_JOINED,
        actionOn: BaseMessage? = null,
        senderUid: String = "user-1",
        receiverId: String = "group-1",
        sentAt: Long = System.currentTimeMillis() / 1000
    ): Action {
        val sender = createUser(uid = senderUid, name = "Sender")
        val message = mock<Action>()
        whenever(message.id).thenReturn(id)
        whenever(message.action).thenReturn(action)
        whenever(message.actionOn).thenReturn(actionOn)
        whenever(message.sender).thenReturn(sender)
        whenever(message.receiverUid).thenReturn(receiverId)
        whenever(message.receiverType).thenReturn(CometChatConstants.RECEIVER_TYPE_GROUP)
        whenever(message.sentAt).thenReturn(sentAt)
        whenever(message.type).thenReturn(CometChatConstants.MESSAGE_TYPE_TEXT)
        whenever(message.category).thenReturn(CometChatConstants.CATEGORY_ACTION)
        whenever(message.readAt).thenReturn(0L)
        whenever(message.deliveredAt).thenReturn(0L)
        whenever(message.deletedAt).thenReturn(0L)
        whenever(message.parentMessageId).thenReturn(0L)
        return message
    }

    /**
     * Creates a chronological list of TextMessages for pagination and ordering tests.
     * Messages are generated with sequential IDs and timestamps spaced by [intervalMs].
     *
     * @param count Number of messages to generate
     * @param startId Starting message ID (increments by 1 for each message)
     * @param startTimestamp Starting timestamp in epoch seconds
     * @param intervalMs Interval between messages in seconds
     * @param senderUid UID of the sender for all messages
     * @param receiverId ID of the receiver for all messages
     * @param receiverType Receiver type for all messages
     */
    fun createMessages(
        count: Int,
        startId: Long = 1L,
        startTimestamp: Long = 1700000000L,
        intervalMs: Long = 60L,
        senderUid: String = "user-1",
        receiverId: String = "user-2",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER
    ): List<TextMessage> {
        return (0 until count).map { i ->
            createTextMessage(
                id = startId + i,
                text = "Message ${startId + i}",
                senderUid = senderUid,
                receiverId = receiverId,
                receiverType = receiverType,
                sentAt = startTimestamp + (i * intervalMs)
            )
        }
    }

    /**
     * Creates a mock Conversation with full control for unread anchor tests.
     * Supports both user and group conversations with unread count and message tracking.
     *
     * @param id Conversation ID
     * @param type Conversation type — RECEIVER_TYPE_USER or RECEIVER_TYPE_GROUP
     * @param unreadCount Number of unread messages
     * @param lastReadMessageId ID of the last read message (for unread anchor calculation)
     * @param latestMessageId ID of the latest message in the conversation
     * @param lastMessage The last BaseMessage in the conversation
     */
    fun createConversation(
        id: String = "conv-1",
        type: String = CometChatConstants.RECEIVER_TYPE_USER,
        unreadCount: Int = 0,
        lastReadMessageId: Long = 0L,
        latestMessageId: Long = 0L,
        lastMessage: BaseMessage? = null
    ): Conversation {
        val conversation = mock<Conversation>()
        whenever(conversation.conversationId).thenReturn(id)
        whenever(conversation.conversationType).thenReturn(type)
        whenever(conversation.unreadMessageCount).thenReturn(unreadCount)
        whenever(conversation.lastReadMessageId).thenReturn(lastReadMessageId)

        // Set conversationWith based on type
        if (type == CometChatConstants.RECEIVER_TYPE_USER) {
            val user = createUser(uid = "user-conv", name = "Conv User")
            whenever(conversation.conversationWith).thenReturn(user)
        } else {
            val group = createGroup(guid = "group-conv", name = "Conv Group")
            whenever(conversation.conversationWith).thenReturn(group)
        }

        // Create a lastMessage with the latestMessageId if not provided
        val resolvedLastMessage = lastMessage ?: if (latestMessageId > 0L) {
            createTextMessage(id = latestMessageId, text = "Latest")
        } else {
            null
        }
        whenever(conversation.lastMessage).thenReturn(resolvedLastMessage)
        return conversation
    }

    /**
     * Creates a surrounding messages result for GoToMessage tests.
     * Generates older messages + target message + newer messages in chronological order.
     *
     * @param targetId The message ID to navigate to (center of the result)
     * @param olderCount Number of messages older than the target
     * @param newerCount Number of messages newer than the target
     * @param senderUid UID of the sender for all messages
     * @param receiverId ID of the receiver for all messages
     */
    fun createSurroundingMessagesResult(
        targetId: Long = 50L,
        olderCount: Int = 15,
        newerCount: Int = 15,
        senderUid: String = "user-1",
        receiverId: String = "user-2"
    ): List<BaseMessage> {
        val baseTimestamp = 1700000000L
        val older = (0 until olderCount).map { i ->
            createTextMessage(
                id = targetId - olderCount + i,
                text = "Older ${i + 1}",
                senderUid = senderUid,
                receiverId = receiverId,
                sentAt = baseTimestamp + ((i) * 60L)
            ) as BaseMessage
        }
        val target = createTextMessage(
            id = targetId,
            text = "Target Message",
            senderUid = senderUid,
            receiverId = receiverId,
            sentAt = baseTimestamp + (olderCount * 60L)
        ) as BaseMessage
        val newer = (0 until newerCount).map { i ->
            createTextMessage(
                id = targetId + 1 + i,
                text = "Newer ${i + 1}",
                senderUid = senderUid,
                receiverId = receiverId,
                sentAt = baseTimestamp + ((olderCount + 1 + i) * 60L)
            ) as BaseMessage
        }
        return older + target + newer
    }

    /**
     * Creates a mock MessageReceipt for delivery/read receipt tests.
     *
     * @param messageId ID of the message the receipt is for
     * @param senderUid UID of the user who sent the receipt (the reader/receiver)
     * @param receiptType Receipt type — RECEIPT_TYPE_DELIVERED, RECEIPT_TYPE_READ, etc.
     * @param timestamp Timestamp of the receipt event (epoch seconds)
     * @param receiverId ID of the receiver (for group receipts)
     * @param receiverType Receiver type — RECEIVER_TYPE_USER or RECEIVER_TYPE_GROUP
     */
    fun createMessageReceipt(
        messageId: Long = 1L,
        senderUid: String = "user-2",
        receiptType: String = MessageReceipt.RECEIPT_TYPE_DELIVERED,
        timestamp: Long = System.currentTimeMillis() / 1000,
        receiverId: String = "user-1",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER
    ): MessageReceipt {
        val sender = createUser(uid = senderUid, name = "Receipt Sender")
        val receipt = mock<MessageReceipt>()
        whenever(receipt.messageId).thenReturn(messageId)
        whenever(receipt.sender).thenReturn(sender)
        whenever(receipt.receiptType).thenReturn(receiptType)
        whenever(receipt.deliveredAt).thenReturn(timestamp)
        whenever(receipt.readAt).thenReturn(timestamp)
        whenever(receipt.receiverId).thenReturn(receiverId)
        whenever(receipt.receiverType).thenReturn(receiverType)
        return receipt
    }

    /**
     * Creates a mock ReactionEvent for reaction add/remove tests.
     *
     * @param messageId ID of the message the reaction is on
     * @param emoji The reaction emoji (e.g., "👍", "❤️", "😂")
     * @param reactedByUid UID of the user who reacted
     * @param conversationId Conversation ID for filtering (defaults to "conv-1")
     */
    fun createReactionEvent(
        messageId: Long = 1L,
        emoji: String = "👍",
        reactedByUid: String = "user-2",
        conversationId: String = "conv-1"
    ): ReactionEvent {
        val reactedBy = createUser(uid = reactedByUid, name = "Reactor")
        val reaction = mock<Reaction>()
        whenever(reaction.messageId).thenReturn(messageId)
        whenever(reaction.reaction).thenReturn(emoji)
        whenever(reaction.reactedBy).thenReturn(reactedBy)

        val event = mock<ReactionEvent>()
        whenever(event.reaction).thenReturn(reaction)
        whenever(event.conversationId).thenReturn(conversationId)
        return event
    }

    /**
     * Creates a CometChatMessageOption for message options tests.
     * This is a data class (not an SDK class), so no mocking needed.
     *
     * @param id Option ID (e.g., UIKitConstants.MessageOption.COPY, EDIT, DELETE, etc.)
     * @param title Display title for the option
     * @param icon Drawable resource ID for the option icon
     */
    fun createMessageOption(
        id: String = "copy",
        title: String = "Copy",
        icon: Int = 0
    ): CometChatMessageOption {
        return CometChatMessageOption(
            id = id,
            title = title,
            icon = icon
        )
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

    // ==================== Call ====================

    /**
     * Creates a mock Call object.
     * Call is a CometChat SDK class with private constructor — must use Mockito mock.
     *
     * @param sessionId The call session ID
     * @param type The call type ("audio" or "video")
     * @param receiverUid The receiver's UID
     * @param receiverType The receiver type ("user" or "group")
     * @param callerUid The caller's UID (creates a User mock for callInitiator)
     * @param callerName The caller's display name
     */
    fun createCall(
        sessionId: String = "session-1",
        type: String = CometChatConstants.CALL_TYPE_AUDIO,
        receiverUid: String = "user-2",
        receiverType: String = CometChatConstants.RECEIVER_TYPE_USER,
        callerUid: String = "user-1",
        callerName: String = "Caller"
    ): com.cometchat.chat.core.Call {
        val call = mock<com.cometchat.chat.core.Call>()
        whenever(call.sessionId).thenReturn(sessionId)
        whenever(call.type).thenReturn(type)
        whenever(call.receiverUid).thenReturn(receiverUid)
        whenever(call.receiverType).thenReturn(receiverType)
        val caller = createUser(uid = callerUid, name = callerName)
        whenever(call.callInitiator).thenReturn(caller)
        whenever(call.sender).thenReturn(caller)
        return call
    }

    /**
     * Creates a list of mock Call objects with unique session IDs.
     */
    fun createCalls(count: Int, prefix: String = "session"): List<com.cometchat.chat.core.Call> {
        return (1..count).map { i ->
            createCall(
                sessionId = "$prefix-$i",
                callerUid = "caller-$i",
                callerName = "Caller $i"
            )
        }
    }

    // ==================== Card Message ====================

    /**
     * Creates a mock CardMessage for developer card (category "card") tests.
     * CardMessage is an SDK class with private constructor — must use Mockito mock.
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
    ): com.cometchat.chat.models.CardMessage {
        val sender = createUser(uid = senderUid, name = "Sender")
        val message = mock<com.cometchat.chat.models.CardMessage>()
        whenever(message.id).thenReturn(id)
        whenever(message.type).thenReturn(type)
        whenever(message.category).thenReturn("card")
        // org.json.JSONObject is not implemented in plain JVM unit tests, so the card
        // is a mock whose toString() returns the JSON — enough for these assertions.
        val cardObject: org.json.JSONObject? = when (cardJson) {
            null -> null
            is org.json.JSONObject -> cardJson
            is String -> if (cardJson.isEmpty()) null else mock<org.json.JSONObject>().also {
                whenever(it.toString()).thenReturn(cardJson)
            }
            is Map<*, *> -> mock<org.json.JSONObject>().also {
                val jsonStr = cardJson.entries.joinToString(",", "{", "}") { (k, v) -> "\"$k\":\"$v\"" }
                whenever(it.toString()).thenReturn(jsonStr)
            }
            else -> null
        }
        whenever(message.card).thenReturn(cardObject)
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
     * Creates a list of mock CardMessages with sequential IDs.
     */
    fun createCardMessages(
        count: Int,
        prefix: String = "card",
        startId: Long = 1L
    ): List<com.cometchat.chat.models.CardMessage> {
        return (0 until count).map { i ->
            createCardMessage(
                id = startId + i,
                type = "$prefix-type-$i",
                text = "Card $i preview"
            )
        }
    }

    /**
     * Creates a CardMessage with an empty/null card payload (triggers fallback rendering).
     */
    fun createEmptyCardMessage(
        id: Long = 1L,
        text: String? = null,
        fallbackText: String? = null
    ): com.cometchat.chat.models.CardMessage {
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
