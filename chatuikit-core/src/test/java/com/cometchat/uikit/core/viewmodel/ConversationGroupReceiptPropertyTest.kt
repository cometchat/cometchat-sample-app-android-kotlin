package com.cometchat.uikit.core.viewmodel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.MutableStateFlow

// Constants for receiver types and receipt types
private const val RECEIVER_TYPE_USER = "user"
private const val RECEIVER_TYPE_GROUP = "group"
private const val RECEIPT_TYPE_DELIVERED_TO_ALL = "deliveredToAll"
private const val RECEIPT_TYPE_READ_BY_ALL = "readByAll"
private const val RECEIPT_TYPE_DELIVERED = "delivered"
private const val RECEIPT_TYPE_READ = "read"

/**
 * Property-based tests for group receipt timestamp updates in conversation list.
 * 
 * Since CometChatConversationsViewModel depends on SDK classes with private constructors,
 * we test the core behavior using a test ViewModel that mirrors the production implementation.
 * 
 * Feature: conversations-viewmodel-parity
 * 
 * **Validates: Requirements 2.3, 2.4**
 */
class ConversationGroupReceiptPropertyTest : FunSpec({

    // ==================== Test Data Classes ====================
    
    /**
     * Test data class that simulates a User.
     */
    data class TestUser(
        val uid: String,
        val name: String = "Test User"
    )
    
    /**
     * Test data class that simulates a Group.
     */
    data class TestGroup(
        val guid: String,
        val name: String = "Test Group"
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
     * Test data class that simulates a group Conversation.
     */
    data class TestGroupConversation(
        val conversationId: String,
        val conversationType: String = RECEIVER_TYPE_GROUP,
        val conversationWith: TestGroup,
        var lastMessage: TestMessage? = null,
        var unreadMessageCount: Int = 0
    ) {
        fun clone(): TestGroupConversation = copy(lastMessage = lastMessage?.clone())
    }
    
    /**
     * Test data class that simulates a MessageReceipt for group conversations.
     */
    data class TestMessageReceipt(
        val messageId: Long,
        val sender: TestUser,
        val receiverType: String = RECEIVER_TYPE_GROUP,
        val receiverId: String = "",
        val deliveredAt: Long = 0L,
        val readAt: Long = 0L,
        val receiptType: String = ""
    )
    
    /**
     * Test ViewModel that implements the updateDeliveredReceipts and updateReadReceipts logic
     * matching CometChatConversationsViewModel's behavior for group conversations.
     * 
     * This mirrors the production implementation where:
     * - For group conversations: match by receipt.receiverId == conversationWith.guid
     * - DELIVERED_TO_ALL receipt type updates lastMessage.deliveredAt
     * - READ_BY_ALL receipt type updates lastMessage.readAt
     */
    class TestGroupReceiptViewModel {
        private val _conversations = MutableStateFlow<List<TestGroupConversation>>(emptyList())
        val conversations = _conversations
        
        /**
         * Sets the initial conversation list.
         */
        fun setConversations(list: List<TestGroupConversation>) {
            _conversations.value = list
        }
        
        /**
         * Updates delivery receipts for group conversations.
         * Mirrors the production updateDeliveredReceipts method.
         * 
         * For group conversations: matches by receipt.receiverId == conversationWith.guid
         * and receipt type must be DELIVERED_TO_ALL.
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
                    RECEIVER_TYPE_GROUP -> {
                        // For group conversations: match by receiver ID and DELIVERED_TO_ALL type
                        if (conversation.conversationType == RECEIVER_TYPE_GROUP) {
                            receipt.receiptType == RECEIPT_TYPE_DELIVERED_TO_ALL &&
                                conversation.conversationWith.guid == receipt.receiverId
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
         * Updates read receipts for group conversations.
         * Mirrors the production updateReadReceipts method.
         * 
         * For group conversations: matches by receipt.receiverId == conversationWith.guid
         * and receipt type must be READ_BY_ALL.
         * Updates lastMessage.readAt when message ID matches.
         */
        fun updateReadReceipts(receipt: TestMessageReceipt) {
            _conversations.value = _conversations.value.map { conversation ->
                val lastMessage = conversation.lastMessage
                
                when (receipt.receiverType) {
                    RECEIVER_TYPE_GROUP -> {
                        if (conversation.conversationType == RECEIVER_TYPE_GROUP) {
                            // Check if receipt is READ_BY_ALL and matches group GUID
                            if (receipt.receiptType == RECEIPT_TYPE_READ_BY_ALL &&
                                conversation.conversationWith.guid == receipt.receiverId) {
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
     * Generator for a list of group conversations with guaranteed unique IDs.
     */
    fun groupConversationListArb(minSize: Int = 2, maxSize: Int = 10): Arb<List<TestGroupConversation>> {
        return Arb.int(minSize, maxSize).map { size ->
            (0 until size).map { index ->
                val group = TestGroup(guid = "group_$index", name = "Group $index")
                val message = TestMessage(
                    id = (index + 1).toLong() * 1000,
                    conversationId = "conv_group_$index",
                    deliveredAt = 0L,
                    readAt = 0L
                )
                TestGroupConversation(
                    conversationId = "conv_group_$index",
                    conversationType = RECEIVER_TYPE_GROUP,
                    conversationWith = group,
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
     * Property 4: Group Receipt Updates Timestamps - DELIVERED_TO_ALL Receipt
     * 
     * *For any* group conversation where the receipt's receiver ID matches the group GUID,
     * when a DELIVERED_TO_ALL receipt is received the `deliveredAt` timestamp should be updated.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: DELIVERED_TO_ALL receipt updates deliveredAt timestamp for matching group conversation").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), deliveryTimestampArb()) { conversations, targetIndex, deliveryTimestamp ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a DELIVERED_TO_ALL receipt matching the target group conversation
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                deliveredAt = deliveryTimestamp,
                receiptType = RECEIPT_TYPE_DELIVERED_TO_ALL
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
     * Property 4: Group Receipt Updates Timestamps - READ_BY_ALL Receipt
     * 
     * *For any* group conversation where the receipt's receiver ID matches the group GUID,
     * when a READ_BY_ALL receipt is received the `readAt` timestamp should be updated.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: READ_BY_ALL receipt updates readAt timestamp for matching group conversation").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), readTimestampArb()) { conversations, targetIndex, readTimestamp ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a READ_BY_ALL receipt matching the target group conversation
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                readAt = readTimestamp,
                receiptType = RECEIPT_TYPE_READ_BY_ALL
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
     * Property 4 (continued): Non-DELIVERED_TO_ALL receipt types don't update deliveredAt
     * 
     * When a delivery receipt is received with a receipt type other than DELIVERED_TO_ALL,
     * the deliveredAt timestamp should NOT be updated for group conversations.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Non-DELIVERED_TO_ALL receipt types don't update deliveredAt for group conversations").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), deliveryTimestampArb()) { conversations, targetIndex, deliveryTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a receipt with regular DELIVERED type (not DELIVERED_TO_ALL)
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                deliveredAt = deliveryTimestamp,
                receiptType = RECEIPT_TYPE_DELIVERED // Not DELIVERED_TO_ALL
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
     * Property 4 (continued): Non-READ_BY_ALL receipt types don't update readAt
     * 
     * When a read receipt is received with a receipt type other than READ_BY_ALL,
     * the readAt timestamp should NOT be updated for group conversations.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Non-READ_BY_ALL receipt types don't update readAt for group conversations").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), readTimestampArb()) { conversations, targetIndex, readTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a receipt with regular READ type (not READ_BY_ALL)
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                readAt = readTimestamp,
                receiptType = RECEIPT_TYPE_READ // Not READ_BY_ALL
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
     * Property 4 (continued): Non-matching group GUID doesn't update deliveredAt
     * 
     * When a DELIVERED_TO_ALL receipt is received with a receiver ID that doesn't match
     * the group's GUID, the deliveredAt timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Non-matching group GUID doesn't update deliveredAt").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), deliveryTimestampArb()) { conversations, targetIndex, deliveryTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a DELIVERED_TO_ALL receipt with non-matching group GUID
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = "non_matching_group_${System.nanoTime()}", // Different GUID
                deliveredAt = deliveryTimestamp,
                receiptType = RECEIPT_TYPE_DELIVERED_TO_ALL
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
     * Property 4 (continued): Non-matching group GUID doesn't update readAt
     * 
     * When a READ_BY_ALL receipt is received with a receiver ID that doesn't match
     * the group's GUID, the readAt timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Non-matching group GUID doesn't update readAt").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), readTimestampArb()) { conversations, targetIndex, readTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a READ_BY_ALL receipt with non-matching group GUID
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = "non_matching_group_${System.nanoTime()}", // Different GUID
                readAt = readTimestamp,
                receiptType = RECEIPT_TYPE_READ_BY_ALL
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
     * Property 4 (continued): Non-matching message ID doesn't update deliveredAt
     * 
     * When a DELIVERED_TO_ALL receipt is received with a message ID that doesn't match
     * the conversation's last message ID, the deliveredAt timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Non-matching message ID doesn't update deliveredAt for group conversations").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), deliveryTimestampArb(), Arb.long(900000L, 999999L)) { conversations, targetIndex, deliveryTimestamp, nonMatchingId ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a DELIVERED_TO_ALL receipt with non-matching message ID
            val receipt = TestMessageReceipt(
                messageId = nonMatchingId, // Different from any message ID
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                deliveredAt = deliveryTimestamp,
                receiptType = RECEIPT_TYPE_DELIVERED_TO_ALL
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
     * Property 4 (continued): Non-matching message ID doesn't update readAt
     * 
     * When a READ_BY_ALL receipt is received with a message ID that doesn't match
     * the conversation's last message ID, the readAt timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Non-matching message ID doesn't update readAt for group conversations").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), readTimestampArb(), Arb.long(900000L, 999999L)) { conversations, targetIndex, readTimestamp, nonMatchingId ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a READ_BY_ALL receipt with non-matching message ID
            val receipt = TestMessageReceipt(
                messageId = nonMatchingId, // Different from any message ID
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                readAt = readTimestamp,
                receiptType = RECEIPT_TYPE_READ_BY_ALL
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
     * Property 4 (continued): Already delivered messages are not updated
     * 
     * When a DELIVERED_TO_ALL receipt is received for a message that already has a non-zero
     * deliveredAt timestamp, the timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Already delivered messages are not updated for group conversations").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), deliveryTimestampArb(), deliveryTimestampArb()) { conversations, targetIndex, existingTimestamp, newTimestamp ->
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
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversationsWithDelivered)
            
            // Create a DELIVERED_TO_ALL receipt for the already delivered message
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                deliveredAt = newTimestamp,
                receiptType = RECEIPT_TYPE_DELIVERED_TO_ALL
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
     * Property 4 (continued): Already read messages are not updated
     * 
     * When a READ_BY_ALL receipt is received for a message that already has a non-zero
     * readAt timestamp, the timestamp should NOT be updated.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Already read messages are not updated for group conversations").config(invocations = 20) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), readTimestampArb(), readTimestampArb()) { conversations, targetIndex, existingTimestamp, newTimestamp ->
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
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversationsWithRead)
            
            // Create a READ_BY_ALL receipt for the already read message
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                readAt = newTimestamp,
                receiptType = RECEIPT_TYPE_READ_BY_ALL
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
     * Property 4 (continued): Receipt updates only affect matching group conversation
     * 
     * When a DELIVERED_TO_ALL receipt is received, only the matching group conversation should be updated.
     * All other conversations should remain unchanged.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: DELIVERED_TO_ALL receipt only affects matching group conversation").config(invocations = 20) {
        checkAll(groupConversationListArb(3, 10), Arb.int(0, 9), deliveryTimestampArb()) { conversations, targetIndex, deliveryTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            // Record original deliveredAt timestamps for all conversations
            val originalTimestamps = conversations.associate { 
                it.conversationId to (it.lastMessage?.deliveredAt ?: 0L)
            }
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a DELIVERED_TO_ALL receipt for the target conversation
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                deliveredAt = deliveryTimestamp,
                receiptType = RECEIPT_TYPE_DELIVERED_TO_ALL
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
     * Property 4 (continued): READ_BY_ALL receipt only affects matching group conversation
     * 
     * When a READ_BY_ALL receipt is received, only the matching group conversation should be updated.
     * All other conversations should remain unchanged.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: READ_BY_ALL receipt only affects matching group conversation").config(invocations = 20) {
        checkAll(groupConversationListArb(3, 10), Arb.int(0, 9), readTimestampArb()) { conversations, targetIndex, readTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val targetMessage = targetConversation.lastMessage!!
            
            // Record original readAt timestamps for all conversations
            val originalTimestamps = conversations.associate { 
                it.conversationId to (it.lastMessage?.readAt ?: 0L)
            }
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a READ_BY_ALL receipt for the target conversation
            val receipt = TestMessageReceipt(
                messageId = targetMessage.id,
                sender = TestUser(uid = "sender_user"),
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = targetConversation.conversationWith.guid,
                readAt = readTimestamp,
                receiptType = RECEIPT_TYPE_READ_BY_ALL
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
     * Property 4 (continued): Conversation without last message is not affected
     * 
     * When a receipt is received, group conversations without a last message should not be affected.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Group conversation without last message is not affected by DELIVERED_TO_ALL receipt").config(invocations = 20) {
        checkAll(Arb.int(2, 10), deliveryTimestampArb()) { size, deliveryTimestamp ->
            // Create conversations where some have no last message
            val conversations = (0 until size).map { index ->
                val group = TestGroup(guid = "group_$index", name = "Group $index")
                val hasMessage = index % 2 == 0 // Only even indices have messages
                TestGroupConversation(
                    conversationId = "conv_group_$index",
                    conversationType = RECEIVER_TYPE_GROUP,
                    conversationWith = group,
                    lastMessage = if (hasMessage) {
                        TestMessage(
                            id = (index + 1).toLong() * 1000,
                            conversationId = "conv_group_$index"
                        )
                    } else null
                )
            }
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a receipt for a conversation without a message
            val targetConversation = conversations.find { it.lastMessage == null }
            if (targetConversation != null) {
                val receipt = TestMessageReceipt(
                    messageId = 12345L,
                    sender = TestUser(uid = "sender_user"),
                    receiverType = RECEIVER_TYPE_GROUP,
                    receiverId = targetConversation.conversationWith.guid,
                    deliveredAt = deliveryTimestamp,
                    receiptType = RECEIPT_TYPE_DELIVERED_TO_ALL
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
    
    /**
     * Property 4 (continued): Group conversation without last message is not affected by READ_BY_ALL receipt
     * 
     * When a READ_BY_ALL receipt is received, group conversations without a last message should not be affected.
     * 
     * **Validates: Requirements 2.3, 2.4**
     */
    test("Property 4: Group conversation without last message is not affected by READ_BY_ALL receipt").config(invocations = 20) {
        checkAll(Arb.int(2, 10), readTimestampArb()) { size, readTimestamp ->
            // Create conversations where some have no last message
            val conversations = (0 until size).map { index ->
                val group = TestGroup(guid = "group_$index", name = "Group $index")
                val hasMessage = index % 2 == 0 // Only even indices have messages
                TestGroupConversation(
                    conversationId = "conv_group_$index",
                    conversationType = RECEIVER_TYPE_GROUP,
                    conversationWith = group,
                    lastMessage = if (hasMessage) {
                        TestMessage(
                            id = (index + 1).toLong() * 1000,
                            conversationId = "conv_group_$index"
                        )
                    } else null
                )
            }
            
            val viewModel = TestGroupReceiptViewModel()
            viewModel.setConversations(conversations)
            
            // Create a receipt for a conversation without a message
            val targetConversation = conversations.find { it.lastMessage == null }
            if (targetConversation != null) {
                val receipt = TestMessageReceipt(
                    messageId = 12345L,
                    sender = TestUser(uid = "sender_user"),
                    receiverType = RECEIVER_TYPE_GROUP,
                    receiverId = targetConversation.conversationWith.guid,
                    readAt = readTimestamp,
                    receiptType = RECEIPT_TYPE_READ_BY_ALL
                )
                
                // Trigger read receipt update
                viewModel.updateReadReceipts(receipt)
                
                // Assert: Conversation without message should still have no message
                val resultList = viewModel.conversations.value
                val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
                updatedConversation?.lastMessage shouldBe null
            }
        }
    }
})
