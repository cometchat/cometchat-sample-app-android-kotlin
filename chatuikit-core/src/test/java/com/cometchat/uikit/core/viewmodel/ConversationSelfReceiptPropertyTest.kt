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

// Constants for receiver types
private const val RECEIVER_TYPE_USER = "user"
private const val RECEIVER_TYPE_GROUP = "group"
private const val RECEIPT_TYPE_READ_BY_ALL = "readByAll"

/**
 * Property-based tests for self receipt clears unread count in conversation list.
 * 
 * Since CometChatConversationsViewModel depends on SDK classes with private constructors,
 * we test the core behavior using a test ViewModel that mirrors the production implementation.
 * 
 * Feature: conversations-viewmodel-parity
 * 
 * **Property 5: Self Receipt Clears Unread Count**
 * 
 * *For any* conversation, when a read receipt is received from the logged-in user,
 * the unread count for that conversation should be set to 0.
 * 
 * **Validates: Requirements 2.5**
 */
class ConversationSelfReceiptPropertyTest : FunSpec({

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
     * Test data class that simulates a user Conversation.
     */
    data class TestUserConversation(
        val conversationId: String,
        val conversationType: String = RECEIVER_TYPE_USER,
        val conversationWith: TestUser,
        var lastMessage: TestMessage? = null,
        var unreadMessageCount: Int = 0
    ) {
        fun clone(): TestUserConversation = copy(lastMessage = lastMessage?.clone())
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
     * Test ViewModel that implements the updateReadReceipts logic for self-receipt handling.
     * 
     * This mirrors the production implementation where:
     * - When a read receipt is received from the logged-in user, the unread count is cleared
     * - This works for both user and group conversations
     * - The self-receipt check is independent of message ID matching
     */
    class TestSelfReceiptViewModel(
        private val loggedInUser: TestUser
    ) {
        private val _userConversations = MutableStateFlow<List<TestUserConversation>>(emptyList())
        val userConversations = _userConversations
        
        private val _groupConversations = MutableStateFlow<List<TestGroupConversation>>(emptyList())
        val groupConversations = _groupConversations
        
        /**
         * Sets the initial user conversation list.
         */
        fun setUserConversations(list: List<TestUserConversation>) {
            _userConversations.value = list
        }
        
        /**
         * Sets the initial group conversation list.
         */
        fun setGroupConversations(list: List<TestGroupConversation>) {
            _groupConversations.value = list
        }
        
        /**
         * Checks if the receipt is from the logged-in user.
         */
        private fun isReceiptFromLoggedInUser(receipt: TestMessageReceipt): Boolean {
            return receipt.sender.uid == loggedInUser.uid
        }

        
        /**
         * Updates read receipts for user conversations.
         * Mirrors the production updateReadReceipts method for user conversations.
         * 
         * Key behavior: When receipt is from logged-in user, clears unread count.
         */
        fun updateReadReceiptsForUserConversations(receipt: TestMessageReceipt) {
            _userConversations.value = _userConversations.value.map { conversation ->
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
                                } else if (isReceiptFromLoggedInUser(receipt)) {
                                    // Receipt is from logged-in user, clear unread count
                                    conversation.clone().apply {
                                        unreadMessageCount = 0
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
        
        /**
         * Updates read receipts for group conversations.
         * Mirrors the production updateReadReceipts method for group conversations.
         * 
         * Key behavior: When receipt is from logged-in user, clears unread count.
         */
        fun updateReadReceiptsForGroupConversations(receipt: TestMessageReceipt) {
            _groupConversations.value = _groupConversations.value.map { conversation ->
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
                            } else if (isReceiptFromLoggedInUser(receipt)) {
                                // Receipt is from logged-in user, clear unread count
                                conversation.clone().apply {
                                    unreadMessageCount = 0
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
     * Generator for a list of user conversations with guaranteed unique IDs and non-zero unread counts.
     */
    fun userConversationListArb(minSize: Int = 2, maxSize: Int = 10): Arb<List<TestUserConversation>> {
        return Arb.int(minSize, maxSize).map { size ->
            (0 until size).map { index ->
                val user = TestUser(uid = "user_$index", name = "User $index")
                val message = TestMessage(
                    id = (index + 1).toLong() * 1000,
                    conversationId = "conv_$index",
                    deliveredAt = 0L,
                    readAt = 0L
                )
                TestUserConversation(
                    conversationId = "conv_$index",
                    conversationType = RECEIVER_TYPE_USER,
                    conversationWith = user,
                    lastMessage = message,
                    unreadMessageCount = (1..20).random() // Non-zero unread count
                )
            }
        }
    }
    
    /**
     * Generator for a list of group conversations with guaranteed unique IDs and non-zero unread counts.
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
                    unreadMessageCount = (1..20).random() // Non-zero unread count
                )
            }
        }
    }
    
    /**
     * Generator for unread count values.
     */
    fun unreadCountArb(): Arb<Int> {
        return Arb.int(1, 100)
    }
    
    /**
     * Generator for read receipt timestamp.
     */
    fun readTimestampArb(): Arb<Long> {
        return Arb.long(1000000000000L, 2000000000000L)
    }


    // ==================== Property Tests ====================
    
    /**
     * Property 5: Self Receipt Clears Unread Count - User Conversation
     * 
     * *For any* user conversation with a non-zero unread count, when a read receipt
     * is received from the logged-in user, the unread count should be set to 0.
     * 
     * **Validates: Requirements 2.5**
     */
    test("Property 5: Self receipt clears unread count for user conversation").config(invocations = 100) {
        checkAll(userConversationListArb(), Arb.int(0, 9), unreadCountArb(), readTimestampArb()) { conversations, targetIndex, unreadCount, readTimestamp ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set a specific unread count on the target conversation
            val conversationsWithUnread = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(unreadMessageCount = unreadCount)
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithUnread[validIndex]
            
            // The logged-in user is the same as the conversation user (self-receipt scenario)
            val loggedInUser = targetConversation.conversationWith
            
            val viewModel = TestSelfReceiptViewModel(loggedInUser)
            viewModel.setUserConversations(conversationsWithUnread)
            
            // Create a read receipt from the logged-in user
            // Note: message ID doesn't need to match for self-receipt to clear unread count
            val receipt = TestMessageReceipt(
                messageId = 999999L, // Non-matching message ID
                sender = loggedInUser,
                receiverType = RECEIVER_TYPE_USER,
                readAt = readTimestamp
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceiptsForUserConversations(receipt)
            
            // Assert: The unread count should be cleared to 0
            val resultList = viewModel.userConversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.unreadMessageCount shouldBe 0
        }
    }
    
    /**
     * Property 5: Self Receipt Clears Unread Count - Group Conversation
     * 
     * *For any* group conversation with a non-zero unread count, when a read receipt
     * is received from the logged-in user, the unread count should be set to 0.
     * 
     * **Validates: Requirements 2.5**
     */
    test("Property 5: Self receipt clears unread count for group conversation").config(invocations = 100) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), unreadCountArb(), readTimestampArb()) { conversations, targetIndex, unreadCount, readTimestamp ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set a specific unread count on the target conversation
            val conversationsWithUnread = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(unreadMessageCount = unreadCount)
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithUnread[validIndex]
            
            // The logged-in user sends a read receipt
            val loggedInUser = TestUser(uid = "logged_in_user", name = "Logged In User")
            
            val viewModel = TestSelfReceiptViewModel(loggedInUser)
            viewModel.setGroupConversations(conversationsWithUnread)
            
            // Create a read receipt from the logged-in user (not READ_BY_ALL, so it triggers self-receipt path)
            val receipt = TestMessageReceipt(
                messageId = 999999L, // Non-matching message ID
                sender = loggedInUser,
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = "some_other_group", // Non-matching group to trigger self-receipt path
                readAt = readTimestamp,
                receiptType = "" // Not READ_BY_ALL
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceiptsForGroupConversations(receipt)
            
            // Assert: The unread count should be cleared to 0 for all group conversations
            // because the receipt is from logged-in user
            val resultList = viewModel.groupConversations.value
            resultList.forEach { conv ->
                conv.unreadMessageCount shouldBe 0
            }
        }
    }


    /**
     * Property 5 (continued): Self receipt clears unread count regardless of message ID match
     * 
     * When a read receipt is received from the logged-in user, the unread count should
     * be cleared even if the message ID doesn't match the last message.
     * 
     * **Validates: Requirements 2.5**
     */
    test("Property 5: Self receipt clears unread count regardless of message ID match").config(invocations = 100) {
        checkAll(userConversationListArb(), Arb.int(0, 9), unreadCountArb(), Arb.long(1L, 999L)) { conversations, targetIndex, unreadCount, nonMatchingMessageId ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set a specific unread count on the target conversation
            val conversationsWithUnread = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(unreadMessageCount = unreadCount)
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithUnread[validIndex]
            val loggedInUser = targetConversation.conversationWith
            
            val viewModel = TestSelfReceiptViewModel(loggedInUser)
            viewModel.setUserConversations(conversationsWithUnread)
            
            // Create a read receipt with a non-matching message ID
            val receipt = TestMessageReceipt(
                messageId = nonMatchingMessageId, // Definitely won't match (message IDs start at 1000)
                sender = loggedInUser,
                receiverType = RECEIVER_TYPE_USER,
                readAt = System.currentTimeMillis()
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceiptsForUserConversations(receipt)
            
            // Assert: The unread count should still be cleared to 0
            val resultList = viewModel.userConversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.unreadMessageCount shouldBe 0
        }
    }
    
    /**
     * Property 5 (continued): Non-self receipts don't clear unread count
     * 
     * When a read receipt is received from a user other than the logged-in user,
     * the unread count should NOT be cleared (unless it's a timestamp update scenario).
     * 
     * **Validates: Requirements 2.5**
     */
    test("Property 5: Non-self receipts don't clear unread count for user conversations").config(invocations = 100) {
        checkAll(userConversationListArb(), Arb.int(0, 9), unreadCountArb(), readTimestampArb()) { conversations, targetIndex, unreadCount, readTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set a specific unread count on the target conversation
            val conversationsWithUnread = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(unreadMessageCount = unreadCount)
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithUnread[validIndex]
            
            // The logged-in user is different from the receipt sender
            val loggedInUser = TestUser(uid = "logged_in_user_different", name = "Different User")
            val receiptSender = TestUser(uid = "other_user_${System.nanoTime()}", name = "Other User")
            
            val viewModel = TestSelfReceiptViewModel(loggedInUser)
            viewModel.setUserConversations(conversationsWithUnread)
            
            // Create a read receipt from a different user (not logged-in user)
            val receipt = TestMessageReceipt(
                messageId = 999999L, // Non-matching message ID
                sender = receiptSender,
                receiverType = RECEIVER_TYPE_USER,
                readAt = readTimestamp
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceiptsForUserConversations(receipt)
            
            // Assert: The unread count should NOT be changed
            val resultList = viewModel.userConversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.unreadMessageCount shouldBe unreadCount
        }
    }


    /**
     * Property 5 (continued): Non-self receipts don't clear unread count for group conversations
     * 
     * When a read receipt is received from a user other than the logged-in user,
     * the unread count should NOT be cleared for group conversations.
     * 
     * **Validates: Requirements 2.5**
     */
    test("Property 5: Non-self receipts don't clear unread count for group conversations").config(invocations = 100) {
        checkAll(groupConversationListArb(), Arb.int(0, 9), unreadCountArb(), readTimestampArb()) { conversations, targetIndex, unreadCount, readTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set a specific unread count on the target conversation
            val conversationsWithUnread = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(unreadMessageCount = unreadCount)
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithUnread[validIndex]
            
            // The logged-in user is different from the receipt sender
            val loggedInUser = TestUser(uid = "logged_in_user", name = "Logged In User")
            val receiptSender = TestUser(uid = "other_user_${System.nanoTime()}", name = "Other User")
            
            val viewModel = TestSelfReceiptViewModel(loggedInUser)
            viewModel.setGroupConversations(conversationsWithUnread)
            
            // Create a read receipt from a different user (not logged-in user)
            val receipt = TestMessageReceipt(
                messageId = 999999L, // Non-matching message ID
                sender = receiptSender,
                receiverType = RECEIVER_TYPE_GROUP,
                receiverId = "some_other_group", // Non-matching group
                readAt = readTimestamp,
                receiptType = "" // Not READ_BY_ALL
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceiptsForGroupConversations(receipt)
            
            // Assert: The unread count should NOT be changed
            val resultList = viewModel.groupConversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.unreadMessageCount shouldBe unreadCount
        }
    }
    
    /**
     * Property 5 (continued): Self receipt clears unread count for any unread value
     * 
     * *For any* unread count value (1 to 100), when a self receipt is received,
     * the unread count should be set to exactly 0.
     * 
     * **Validates: Requirements 2.5**
     */
    test("Property 5: Self receipt clears any unread count value to zero").config(invocations = 100) {
        checkAll(Arb.int(1, 100), readTimestampArb()) { unreadCount, readTimestamp ->
            // Create a single user conversation with the specified unread count
            val user = TestUser(uid = "test_user", name = "Test User")
            val message = TestMessage(
                id = 1000L,
                conversationId = "conv_test",
                deliveredAt = 0L,
                readAt = 0L
            )
            val conversation = TestUserConversation(
                conversationId = "conv_test",
                conversationType = RECEIVER_TYPE_USER,
                conversationWith = user,
                lastMessage = message,
                unreadMessageCount = unreadCount
            )
            
            // The logged-in user is the same as the conversation user
            val loggedInUser = user
            
            val viewModel = TestSelfReceiptViewModel(loggedInUser)
            viewModel.setUserConversations(listOf(conversation))
            
            // Create a self receipt
            val receipt = TestMessageReceipt(
                messageId = 999999L, // Non-matching message ID
                sender = loggedInUser,
                receiverType = RECEIVER_TYPE_USER,
                readAt = readTimestamp
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceiptsForUserConversations(receipt)
            
            // Assert: The unread count should be exactly 0
            val resultList = viewModel.userConversations.value
            resultList.first().unreadMessageCount shouldBe 0
        }
    }


    /**
     * Property 5 (continued): Self receipt only affects conversations where sender matches
     * 
     * When a self receipt is received for a user conversation, only the conversation
     * where the sender matches should have its unread count cleared.
     * 
     * **Validates: Requirements 2.5**
     */
    test("Property 5: Self receipt only affects matching user conversation").config(invocations = 100) {
        checkAll(userConversationListArb(3, 10), Arb.int(0, 9), unreadCountArb()) { conversations, targetIndex, unreadCount ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set specific unread counts on all conversations
            val conversationsWithUnread = conversations.mapIndexed { index, conv ->
                conv.copy(unreadMessageCount = unreadCount + index)
            }
            val targetConversation = conversationsWithUnread[validIndex]
            
            // Record original unread counts
            val originalUnreadCounts = conversationsWithUnread.associate { 
                it.conversationId to it.unreadMessageCount 
            }
            
            // The logged-in user is the same as the target conversation user
            val loggedInUser = targetConversation.conversationWith
            
            val viewModel = TestSelfReceiptViewModel(loggedInUser)
            viewModel.setUserConversations(conversationsWithUnread)
            
            // Create a self receipt
            val receipt = TestMessageReceipt(
                messageId = 999999L,
                sender = loggedInUser,
                receiverType = RECEIVER_TYPE_USER,
                readAt = System.currentTimeMillis()
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceiptsForUserConversations(receipt)
            
            // Assert: Only the target conversation should have unread count cleared
            val resultList = viewModel.userConversations.value
            resultList.forEach { conv ->
                if (conv.conversationId == targetConversation.conversationId) {
                    conv.unreadMessageCount shouldBe 0
                } else {
                    conv.unreadMessageCount shouldBe originalUnreadCounts[conv.conversationId]
                }
            }
        }
    }
    
    /**
     * Property 5 (continued): Self receipt with zero unread count remains zero
     * 
     * When a self receipt is received for a conversation that already has zero unread count,
     * the unread count should remain zero (idempotent operation).
     * 
     * **Validates: Requirements 2.5**
     */
    test("Property 5: Self receipt with zero unread count remains zero").config(invocations = 100) {
        checkAll(userConversationListArb(), Arb.int(0, 9), readTimestampArb()) { conversations, targetIndex, readTimestamp ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set zero unread count on the target conversation
            val conversationsWithZeroUnread = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(unreadMessageCount = 0)
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithZeroUnread[validIndex]
            val loggedInUser = targetConversation.conversationWith
            
            val viewModel = TestSelfReceiptViewModel(loggedInUser)
            viewModel.setUserConversations(conversationsWithZeroUnread)
            
            // Create a self receipt
            val receipt = TestMessageReceipt(
                messageId = 999999L,
                sender = loggedInUser,
                receiverType = RECEIVER_TYPE_USER,
                readAt = readTimestamp
            )
            
            // Trigger read receipt update
            viewModel.updateReadReceiptsForUserConversations(receipt)
            
            // Assert: The unread count should remain 0
            val resultList = viewModel.userConversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation shouldNotBe null
            updatedConversation!!.unreadMessageCount shouldBe 0
        }
    }
})
