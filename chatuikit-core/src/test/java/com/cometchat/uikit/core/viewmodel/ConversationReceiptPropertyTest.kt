package com.cometchat.uikit.core.viewmodel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.MutableStateFlow

// Constants for receiver types
private const val RECEIVER_TYPE_USER = "user"
private const val RECEIVER_TYPE_GROUP = "group"
private const val RECEIPT_TYPE_DELIVERED_TO_ALL = "deliveredToAll"
private const val RECEIPT_TYPE_READ_BY_ALL = "readByAll"

/**
 * Property-based tests for user receipt timestamp updates in conversation list.
 * 
 * Since CometChatConversationsViewModel depends on SDK classes with private constructors,
 * we test the core behavior using a test ViewModel that mirrors the production implementation.
 * 
 * Feature: conversations-viewmodel-parity
 * 
 * **Validates: Requirements 2.1, 2.2**
 */
class ConversationReceiptPropertyTest : FunSpec({

    // ==================== Test Data Classes ====================
    
    /**
     * Test data class that simulates a User.
     */
    data class TestUser(
        val uid: String,
        val name: String = "Test User"
    )
    
    /**
     * Test data class that simulates a Message with receipt timestamps.
     */
    data class TestMessage(
        val id: Long,
        val conversationId: String,
        var deliveredAt: Long = 0L,
        var readAt: Long = 0L,
        val senderId: String = "sender"
    ) {
        fun clone(): TestMessage = copy()
    }

    
    /**
     * Test data class that simulates Conversation with user-based matching.
     */
    data class TestConversation(
        val conversationId: String,
        val conversationType: String = RECEIVER_TYPE_USER,
        val conversationWith: TestUser,
        var lastMessage: TestMessage? = null,
        var unreadMessageCount: Int = 0
    ) {
        fun clone(): TestConversation = copy(lastMessage = lastMessage?.clone())
    }
    
    /**
     * Test data class that simulates a MessageReceipt.
     */
    data class TestMessageReceipt(
        val messageId: Long,
        val sender: TestUser,
        val receiverType: String = RECEIVER_TYPE_USER,
        val receiverId: String = "",
        val deliveredAt: Long = 0L,
        val readAt: Long = 0L,
        val receiptType: String = ""
    )
    
    /**
     * Test ViewModel that implements the updateDeliveredReceipts and updateReadReceipts logic
     * matching CometChatConversationsViewModel's behavior for user conversations.
     * 
     * This mirrors the production implementation where:
     * - For user conversations: match by receipt.sender.uid == conversationWith.uid
     * - Update lastMessage.deliveredAt when message ID matches for delivery receipts
     * - Update lastMessage.readAt when message ID matches for read receipts
     */
    class TestReceiptViewModel {
        private val _conversations = MutableStateFlow<List<TestConversation>>(emptyList())
        val conversations = _conversations
        
        /**
         * Sets the initial conversation list.
         */
        fun setConversations(list: List<TestConversation>) {
            _conversations.value = list
        }
        
        /**
         * Updates delivery receipts for user conversations.
         * Mirrors the production updateDeliveredReceipts method.
         * 
         * For user conversations: matches by receipt.sender.uid == conversationWith.uid
         * Updates lastMessage.deliveredAt when message ID matches.
         */
        fun updateDeliveredReceipts(receipt: TestMessageReceipt) {
            _conversations.value = _conversations.value.map { conversation ->
                val lastMessage = conversation.lastMessage
                
                // Skip if no last message or already delivered or message ID doesn't match
                if (lastMessage == null || lastMessage.deliveredAt != 0L || lastMessage.id != receipt.messageId) {
                    return@map conversation
                }
                
                val shouldUpdate = when (receipt.receiverType) {
                    RECEIVER_TYPE_USER -> {
                        // For user conversations: match by sender UID
                        if (conversation.conversationType == RECEIVER_TYPE_USER) {
                            conversation.conversationWith.uid == receipt.sender.uid
                        } else {
                            false
                        }
                    }
                    else -> false
                }
                
                if (shouldUpdate) {
                    // Clone conversation and update deliveredAt timestamp
                    conversation.clone().apply {
                        this.lastMessage = lastMessage.clone().apply {
                            deliveredAt = receipt.deliveredAt
                        }
                    }
                } else {
                    conversation
                }
            }
        }

        
        /**
         * Updates read receipts for user conversations.
         * Mirrors the production updateReadReceipts method.
         * 
         * For user conversations: matches by receipt.sender.uid == conversationWith.uid
         * Updates lastMessage.readAt when message ID matches.
         */
        fun updateReadReceipts(receipt: TestMessageReceipt) {
            _conversations.value = _conversations.value.map { conversation ->
                val lastMessage = conversation.lastMessage
                
                when (receipt.receiverType) {
                    RECEIVER_TYPE_USER -> {
                        if (conversation.conversationType == RECEIVER_TYPE_USER) {
                            // Check if receipt sender matches conversation user
                            if (conversation.conversationWith.uid == receipt.sender.uid) {
                                // Check if we should update readAt timestamp
                                if (lastMessage != null && 
                                    lastMessage.readAt == 0L && 
                                    lastMessage.id == receipt.messageId) {
                                    // Clone conversation and update readAt timestamp
                                    conversation.clone().apply {
                                        this.lastMessage = lastMessage.clone().apply {
                                            readAt = receipt.readAt
                                        }
                                    }
                                } else {
                                    conversation
                                }
                            } else {
                                conversation
                            }
                        } else {
                            conversation
                        }
                    }
                    else -> conversation
                }
            }
        }
    }
    
    // ==================== Generators ====================
    
    /**
     * Generator for a list of user conversations with guaranteed unique IDs.
     */
    fun userConversationListArb(minSize: Int = 2, maxSize: Int = 10): Arb<List<TestConversation>> {
        return Arb.int(minSize, maxSize).map { size ->
            (0 until size).map { index ->
                val user = TestUser(uid = "user_$index", name = "User $index")
                val message = TestMessage(
                    id = (index + 1).toLong() * 1000,
                    conversationId = "conv_$index",
                    deliveredAt = 0L,
                    readAt = 0L
                )
                TestConversation(
                    conversationId = "conv_$index",
                    conversationType = RECEIVER_TYPE_USER,
                    conversationWith = user,
                    lastMessage = message,
                    unreadMessageCount = (0..10).random()
                )
            }
        }
    }
    
    /**
     * Generator for delivery receipt timestamp.
     */
    fun deliveryTimestampArb(): Arb<Long> {
        return Arb.long(1000000000000L, 2000000000000L)
    }
    
    /**
     * Generator for read receipt timestamp.
     */
    fun readTimestampArb(): Arb<Long> {
        return Arb.long(1000000000000L, 2000000000000L)
    }


    // ==================== Property Tests ====================
    
    /**
     * Property 3: User Receipt Updates Timestamps - Delivery Receipt
     * 
     * *For any* user conversation where the last message ID matches the receipt's message ID,
     * when a delivery receipt is received the `deliveredAt` timestamp should be updated.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Delivery receipt updates deliveredAt timestamp for matching user conversation").config(invocations = 20) {
        checkAll(userConversationListArb(), Arb.int(0, 9), deliveryTimestampArb()) { conversations, targetIndex, deliveryTimestamp ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a delivery receipt matching the target conversation
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = targetConversation.conversationWith,
                receiverType = RECEIVER_TYPE_USER,
                deliveredAt = deliveryTimestamp
            )
            
            // Trigger delivery receipt update
            viewModel.updateDeliveredReceipts(receipt)
            
            // Assert: The deliveredAt timestamp should be updated
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.lastMessage?.deliveredAt shouldBe deliveryTimestamp
        }
    }
    
    /**
     * Property 3: User Receipt Updates Timestamps - Read Receipt
     * 
     * *For any* user conversation where the last message ID matches the receipt's message ID,
     * when a read receipt is received the `readAt` timestamp should be updated.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Read receipt updates readAt timestamp for matching user conversation").config(invocations = 20) {
        checkAll(userConversationListArb(), Arb.int(0, 9), readTimestampArb()) { conversations, targetIndex, readTimestamp ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a read receipt matching the target conversation
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = targetConversation.conversationWith,
                receiverType = RECEIVER_TYPE_USER,
                readAt = readTimestamp
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceipts(receipt)
            
            // Assert: The readAt timestamp should be updated
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.lastMessage?.readAt shouldBe readTimestamp
        }
    }

    
    /**
     * Property 3 (continued): Delivery receipt does not update non-matching message IDs
     * 
     * When a delivery receipt is received with a message ID that doesn't match the
     * conversation's last message ID, the deliveredAt timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Delivery receipt does not update non-matching message IDs").config(invocations = 20) {
        checkAll(userConversationListArb(), Arb.int(0, 9), deliveryTimestampArb(), Arb.long(900000L, 999999L)) { conversations, targetIndex, deliveryTimestamp, nonMatchingId ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a delivery receipt with non-matching message ID
            val receipt = TestMessageReceipt(
                messageId = nonMatchingId, // Different from any message ID
                sender = targetConversation.conversationWith,
                receiverType = RECEIVER_TYPE_USER,
                deliveredAt = deliveryTimestamp
            )
            
            // Trigger delivery receipt update
            viewModel.updateDeliveredReceipts(receipt)
            
            // Assert: The deliveredAt timestamp should NOT be updated (remains 0)
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.lastMessage?.deliveredAt shouldBe 0L
        }
    }
    
    /**
     * Property 3 (continued): Read receipt does not update non-matching message IDs
     * 
     * When a read receipt is received with a message ID that doesn't match the
     * conversation's last message ID, the readAt timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Read receipt does not update non-matching message IDs").config(invocations = 20) {
        checkAll(userConversationListArb(), Arb.int(0, 9), readTimestampArb(), Arb.long(900000L, 999999L)) { conversations, targetIndex, readTimestamp, nonMatchingId ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a read receipt with non-matching message ID
            val receipt = TestMessageReceipt(
                messageId = nonMatchingId, // Different from any message ID
                sender = targetConversation.conversationWith,
                receiverType = RECEIVER_TYPE_USER,
                readAt = readTimestamp
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceipts(receipt)
            
            // Assert: The readAt timestamp should NOT be updated (remains 0)
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.lastMessage?.readAt shouldBe 0L
        }
    }

    
    /**
     * Property 3 (continued): Delivery receipt does not update non-matching user
     * 
     * When a delivery receipt is received from a user that doesn't match the
     * conversation's user, the deliveredAt timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Delivery receipt does not update non-matching user").config(invocations = 20) {
        checkAll(userConversationListArb(), Arb.int(0, 9), deliveryTimestampArb()) { conversations, targetIndex, deliveryTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a delivery receipt with non-matching user
            val nonMatchingUser = TestUser(uid = "non_matching_user_${System.nanoTime()}", name = "Non-matching User")
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = nonMatchingUser, // Different user
                receiverType = RECEIVER_TYPE_USER,
                deliveredAt = deliveryTimestamp
            )
            
            // Trigger delivery receipt update
            viewModel.updateDeliveredReceipts(receipt)
            
            // Assert: The deliveredAt timestamp should NOT be updated (remains 0)
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.lastMessage?.deliveredAt shouldBe 0L
        }
    }
    
    /**
     * Property 3 (continued): Read receipt does not update non-matching user
     * 
     * When a read receipt is received from a user that doesn't match the
     * conversation's user, the readAt timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Read receipt does not update non-matching user").config(invocations = 20) {
        checkAll(userConversationListArb(), Arb.int(0, 9), readTimestampArb()) { conversations, targetIndex, readTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a read receipt with non-matching user
            val nonMatchingUser = TestUser(uid = "non_matching_user_${System.nanoTime()}", name = "Non-matching User")
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = nonMatchingUser, // Different user
                receiverType = RECEIVER_TYPE_USER,
                readAt = readTimestamp
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceipts(receipt)
            
            // Assert: The readAt timestamp should NOT be updated (remains 0)
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.lastMessage?.readAt shouldBe 0L
        }
    }

    
    /**
     * Property 3 (continued): Delivery receipt does not update already delivered messages
     * 
     * When a delivery receipt is received for a message that already has a non-zero
     * deliveredAt timestamp, the timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Delivery receipt does not update already delivered messages").config(invocations = 20) {
        checkAll(userConversationListArb(), Arb.int(0, 9), deliveryTimestampArb(), deliveryTimestampArb()) { conversations, targetIndex, existingTimestamp, newTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set an existing deliveredAt timestamp on the target conversation
            val conversationsWithDelivered = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(lastMessage = conv.lastMessage?.copy(deliveredAt = existingTimestamp))
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithDelivered[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversationsWithDelivered)
            
            // Create a delivery receipt for the already delivered message
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = targetConversation.conversationWith,
                receiverType = RECEIVER_TYPE_USER,
                deliveredAt = newTimestamp
            )
            
            // Trigger delivery receipt update
            viewModel.updateDeliveredReceipts(receipt)
            
            // Assert: The deliveredAt timestamp should remain unchanged (existing value)
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.lastMessage?.deliveredAt shouldBe existingTimestamp
        }
    }
    
    /**
     * Property 3 (continued): Read receipt does not update already read messages
     * 
     * When a read receipt is received for a message that already has a non-zero
     * readAt timestamp, the timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Read receipt does not update already read messages").config(invocations = 20) {
        checkAll(userConversationListArb(), Arb.int(0, 9), readTimestampArb(), readTimestampArb()) { conversations, targetIndex, existingTimestamp, newTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set an existing readAt timestamp on the target conversation
            val conversationsWithRead = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(lastMessage = conv.lastMessage?.copy(readAt = existingTimestamp))
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithRead[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversationsWithRead)
            
            // Create a read receipt for the already read message
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = targetConversation.conversationWith,
                receiverType = RECEIVER_TYPE_USER,
                readAt = newTimestamp
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceipts(receipt)
            
            // Assert: The readAt timestamp should remain unchanged (existing value)
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.lastMessage?.readAt shouldBe existingTimestamp
        }
    }

    
    /**
     * Property 3 (continued): Receipt updates only affect matching conversation
     * 
     * When a receipt is received, only the matching conversation should be updated.
     * All other conversations should remain unchanged.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Delivery receipt only affects matching conversation").config(invocations = 20) {
        checkAll(userConversationListArb(3, 10), Arb.int(0, 9), deliveryTimestampArb()) { conversations, targetIndex, deliveryTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            // Record original deliveredAt timestamps for all conversations
            val originalTimestamps = conversations.associate { 
                it.conversationId to (it.lastMessage?.deliveredAt ?: 0L)
            }
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a delivery receipt for the target conversation
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = targetConversation.conversationWith,
                receiverType = RECEIVER_TYPE_USER,
                deliveredAt = deliveryTimestamp
            )
            
            // Trigger delivery receipt update
            viewModel.updateDeliveredReceipts(receipt)
            
            // Assert: Only the target conversation should be updated
            val resultList = viewModel.conversations.value
            resultList.forEach { conv ->
                if (conv.conversationId == targetConversation.conversationId) {
                    conv.lastMessage?.deliveredAt shouldBe deliveryTimestamp
                } else {
                    conv.lastMessage?.deliveredAt shouldBe originalTimestamps[conv.conversationId]
                }
            }
        }
    }
    
    /**
     * Property 3 (continued): Read receipt only affects matching conversation
     * 
     * When a read receipt is received, only the matching conversation should be updated.
     * All other conversations should remain unchanged.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Read receipt only affects matching conversation").config(invocations = 20) {
        checkAll(userConversationListArb(3, 10), Arb.int(0, 9), readTimestampArb()) { conversations, targetIndex, readTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            // Record original readAt timestamps for all conversations
            val originalTimestamps = conversations.associate { 
                it.conversationId to (it.lastMessage?.readAt ?: 0L)
            }
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a read receipt for the target conversation
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = targetConversation.conversationWith,
                receiverType = RECEIVER_TYPE_USER,
                readAt = readTimestamp
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceipts(receipt)
            
            // Assert: Only the target conversation should be updated
            val resultList = viewModel.conversations.value
            resultList.forEach { conv ->
                if (conv.conversationId == targetConversation.conversationId) {
                    conv.lastMessage?.readAt shouldBe readTimestamp
                } else {
                    conv.lastMessage?.readAt shouldBe originalTimestamps[conv.conversationId]
                }
            }
        }
    }

    
    /**
     * Property 3 (continued): Conversation without last message is not affected
     * 
     * When a receipt is received, conversations without a last message should not be affected.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    test("Property 3: Conversation without last message is not affected by delivery receipt").config(invocations = 20) {
        checkAll(Arb.int(2, 10), deliveryTimestampArb()) { size, deliveryTimestamp ->
            // Create conversations where some have no last message
            val conversations = (0 until size).map { index ->
                val user = TestUser(uid = "user_$index", name = "User $index")
                val hasMessage = index % 2 == 0 // Only even indices have messages
                TestConversation(
                    conversationId = "conv_$index",
                    conversationType = RECEIVER_TYPE_USER,
                    conversationWith = user,
                    lastMessage = if (hasMessage) {
                        TestMessage(
                            id = (index + 1).toLong() * 1000,
                            conversationId = "conv_$index"
                        )
                    } else null
                )
            }
            
            val viewModel = TestReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a receipt for a conversation without a message
            val targetConversation = conversations.find { it.lastMessage == null }
            if (targetConversation != null) {
                val receipt = TestMessageReceipt(
                    messageId = 12345L,
                    sender = targetConversation.conversationWith,
                    receiverType = RECEIVER_TYPE_USER,
                    deliveredAt = deliveryTimestamp
                )
                
                // Trigger delivery receipt update
                viewModel.updateDeliveredReceipts(receipt)
                
                // Assert: Conversation without message should still have no message
                val resultList = viewModel.conversations.value
                val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
                updatedConversation?.lastMessage shouldBe null
            }
        }
    }
})
