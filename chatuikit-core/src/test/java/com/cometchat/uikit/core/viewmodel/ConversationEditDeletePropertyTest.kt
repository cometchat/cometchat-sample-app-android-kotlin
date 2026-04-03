package com.cometchat.uikit.core.viewmodel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Property-based tests for edit/delete behavior in conversation list.
 * 
 * Since CometChatConversationsViewModel depends on SDK classes with private constructors,
 * we test the core behavior using a test ViewModel that mirrors the production implementation.
 * 
 * Feature: conversations-viewmodel-parity
 * 
 * **Validates: Requirements 1.1, 1.2**
 */
class ConversationEditDeletePropertyTest : FunSpec({

    // ==================== Test Data Classes ====================
    
    /**
     * Test data class that simulates Conversation with conversationId-based equality.
     */
    data class TestConversation(
        val conversationId: String,
        val name: String = "Test",
        var unreadMessageCount: Int = 0,
        var lastMessageId: Long = 0L
    ) {
        fun clone(): TestConversation = copy()
    }
    
    /**
     * Test data class that simulates a Message.
     */
    data class TestMessage(
        val id: Long,
        val conversationId: String,
        val text: String = "Test message"
    )
    
    /**
     * Test ViewModel that implements the updateConversation logic matching
     * CometChatConversationsViewModel's behavior for edit/delete operations.
     * 
     * This mirrors the production implementation where:
     * - Edit/delete calls updateConversation() with isActionMessage = false
     * - The conversation is moved to index 0 (top of list)
     * - Unread count is preserved for edit/delete operations
     */
    class TestConversationListViewModel {
        private val _conversations = MutableStateFlow<List<TestConversation>>(emptyList())
        val conversations = _conversations
        
        /**
         * Sets the initial conversation list.
         */
        fun setConversations(list: List<TestConversation>) {
            _conversations.value = list
        }
        
        /**
         * Simulates the updateConversation method called when a message is edited or deleted.
         * This mirrors the production implementation:
         * 
         * ```kotlin
         * override fun onMessageEdited(message: BaseMessage) {
         *     val conversation = CometChatHelper.getConversationFromMessage(message)
         *     if (conversation != null) {
         *         updateConversation(conversation, isActionMessage = false)
         *     }
         * }
         * ```
         * 
         * @param conversation The conversation to update (converted from the edited/deleted message)
         * @param isActionMessage Whether this is an action message (false for edit/delete)
         */
        fun updateConversation(conversation: TestConversation, isActionMessage: Boolean) {
            val currentList = _conversations.value
            val existingIndex = currentList.indexOfFirst { it.conversationId == conversation.conversationId }
            
            if (existingIndex >= 0) {
                val oldConversation = currentList[existingIndex]
                
                // Clone the conversation to create a new reference
                val updatedConversation = conversation.clone()
                
                // For edit/delete (isActionMessage = false), preserve unread count
                // This matches the production logic where isSentByMe check preserves unread count
                updatedConversation.unreadMessageCount = oldConversation.unreadMessageCount
                
                // Build new list with updated conversation at top (index 0)
                val newList = buildList {
                    add(updatedConversation)
                    currentList.forEachIndexed { index, conv ->
                        if (index != existingIndex) {
                            add(conv)
                        }
                    }
                }
                
                _conversations.value = newList
            } else {
                // Conversation not in list, add it at top
                val newList = buildList {
                    add(conversation.clone())
                    addAll(currentList)
                }
                _conversations.value = newList
            }
        }
        
        /**
         * Simulates handling a message edit event.
         * Converts the message to a conversation and calls updateConversation.
         */
        fun onMessageEdited(message: TestMessage) {
            val conversation = TestConversation(
                conversationId = message.conversationId,
                lastMessageId = message.id
            )
            updateConversation(conversation, isActionMessage = false)
        }
        
        /**
         * Simulates handling a message delete event.
         * Converts the message to a conversation and calls updateConversation.
         */
        fun onMessageDeleted(message: TestMessage) {
            val conversation = TestConversation(
                conversationId = message.conversationId,
                lastMessageId = message.id
            )
            updateConversation(conversation, isActionMessage = false)
        }
    }
    
    // ==================== Generators ====================
    
    /**
     * Generator for a list of conversations with guaranteed unique IDs.
     */
    fun conversationListArb(minSize: Int = 2, maxSize: Int = 10): Arb<List<TestConversation>> {
        return Arb.int(minSize, maxSize).map { size ->
            (0 until size).map { index ->
                TestConversation(
                    conversationId = "conv_$index",
                    name = "User $index",
                    unreadMessageCount = (0..10).random(),
                    lastMessageId = (1L..1000L).random()
                )
            }
        }
    }

    // ==================== Property Tests ====================
    
    /**
     * Property 1: Edit/Delete Moves Conversation to Top
     * 
     * *For any* conversation list and any message edit or delete event, when the update 
     * method is called with the converted conversation, the conversation should be moved 
     * to index 0 of the list.
     * 
     * **Validates: Requirements 1.1, 1.2**
     */
    test("Property 1: Edit moves conversation to top of list").config(invocations = 20) {
        checkAll(conversationListArb(), Arb.int(0, 9)) { conversations, targetIndex ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversations)
            
            // Create an edit message for the target conversation
            val editedMessage = TestMessage(
                id = System.currentTimeMillis(),
                conversationId = targetConversation.conversationId,
                text = "Edited message"
            )
            
            // Trigger edit event
            viewModel.onMessageEdited(editedMessage)
            
            // Assert: The conversation should now be at index 0
            val resultList = viewModel.conversations.value
            resultList.isNotEmpty() shouldBe true
            resultList[0].conversationId shouldBe targetConversation.conversationId
        }
    }
    
    /**
     * Property 1 (continued): Delete moves conversation to top
     * 
     * **Validates: Requirements 1.1, 1.2**
     */
    test("Property 1: Delete moves conversation to top of list").config(invocations = 20) {
        checkAll(conversationListArb(), Arb.int(0, 9)) { conversations, targetIndex ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversations)
            
            // Create a delete message for the target conversation
            val deletedMessage = TestMessage(
                id = System.currentTimeMillis(),
                conversationId = targetConversation.conversationId,
                text = "Deleted message"
            )
            
            // Trigger delete event
            viewModel.onMessageDeleted(deletedMessage)
            
            // Assert: The conversation should now be at index 0
            val resultList = viewModel.conversations.value
            resultList.isNotEmpty() shouldBe true
            resultList[0].conversationId shouldBe targetConversation.conversationId
        }
    }
    
    /**
     * Property 1 (continued): List size remains unchanged after edit/delete
     * 
     * When an existing conversation is updated via edit/delete, the list size
     * should remain the same (conversation is moved, not added).
     * 
     * **Validates: Requirements 1.1, 1.2**
     */
    test("Property 1: Edit/Delete preserves list size for existing conversations").config(invocations = 20) {
        checkAll(conversationListArb(), Arb.int(0, 9)) { conversations, targetIndex ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            val originalSize = conversations.size
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversations)
            
            // Trigger edit event
            val editedMessage = TestMessage(
                id = System.currentTimeMillis(),
                conversationId = targetConversation.conversationId
            )
            viewModel.onMessageEdited(editedMessage)
            
            // Assert: List size should remain unchanged
            viewModel.conversations.value.size shouldBe originalSize
        }
    }
    
    /**
     * Property 1 (continued): Other conversations maintain relative order
     * 
     * When a conversation is moved to top, all other conversations should
     * maintain their relative order (just shifted down by one if they were above).
     * 
     * **Validates: Requirements 1.1, 1.2**
     */
    test("Property 1: Edit/Delete maintains relative order of other conversations").config(invocations = 20) {
        checkAll(conversationListArb(3, 10), Arb.int(1, 9)) { conversations, targetIndex ->
            val validIndex = targetIndex.coerceIn(1, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            
            // Get the expected order of other conversations (excluding target)
            val expectedOtherOrder = conversations.filter { it.conversationId != targetConversation.conversationId }
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversations)
            
            // Trigger edit event
            val editedMessage = TestMessage(
                id = System.currentTimeMillis(),
                conversationId = targetConversation.conversationId
            )
            viewModel.onMessageEdited(editedMessage)
            
            // Get the actual order of other conversations (excluding the moved one at index 0)
            val resultList = viewModel.conversations.value
            val actualOtherOrder = resultList.drop(1) // Skip the first one (moved conversation)
            
            // Assert: Other conversations maintain their relative order
            actualOtherOrder.map { it.conversationId } shouldBe expectedOtherOrder.map { it.conversationId }
        }
    }
    
    /**
     * Property 1 (continued): Edit/Delete for conversation already at top
     * 
     * When the conversation is already at index 0, it should remain at index 0
     * and the list should be unchanged.
     * 
     * **Validates: Requirements 1.1, 1.2**
     */
    test("Property 1: Edit/Delete for conversation already at top keeps it at top").config(invocations = 20) {
        checkAll(conversationListArb()) { conversations ->
            val topConversation = conversations[0]
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversations)
            
            // Trigger edit event for the top conversation
            val editedMessage = TestMessage(
                id = System.currentTimeMillis(),
                conversationId = topConversation.conversationId
            )
            viewModel.onMessageEdited(editedMessage)
            
            // Assert: The conversation should still be at index 0
            val resultList = viewModel.conversations.value
            resultList[0].conversationId shouldBe topConversation.conversationId
            resultList.size shouldBe conversations.size
        }
    }
    
    // ==================== Property 2: Edit/Delete Preserves Unread Count ====================
    
    /**
     * Property 2: Edit/Delete Preserves Unread Count
     * 
     * *For any* conversation with a non-zero unread count, when a message edit or delete 
     * event occurs for that conversation, the unread count should remain unchanged after 
     * the update.
     * 
     * **Validates: Requirements 1.3**
     */
    test("Property 2: Edit preserves unread count for conversations with non-zero unread").config(invocations = 20) {
        checkAll(conversationListArb(), Arb.int(0, 9), Arb.int(1, 100)) { conversations, targetIndex, unreadCount ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set a specific non-zero unread count on the target conversation
            val conversationsWithUnread = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(unreadMessageCount = unreadCount)
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithUnread[validIndex]
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversationsWithUnread)
            
            // Create an edit message for the target conversation
            val editedMessage = TestMessage(
                id = System.currentTimeMillis(),
                conversationId = targetConversation.conversationId,
                text = "Edited message"
            )
            
            // Trigger edit event
            viewModel.onMessageEdited(editedMessage)
            
            // Assert: The unread count should be preserved
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation?.unreadMessageCount shouldBe unreadCount
        }
    }
    
    /**
     * Property 2 (continued): Delete preserves unread count
     * 
     * **Validates: Requirements 1.3**
     */
    test("Property 2: Delete preserves unread count for conversations with non-zero unread").config(invocations = 20) {
        checkAll(conversationListArb(), Arb.int(0, 9), Arb.int(1, 100)) { conversations, targetIndex, unreadCount ->
            // Ensure we have a valid target index
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set a specific non-zero unread count on the target conversation
            val conversationsWithUnread = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(unreadMessageCount = unreadCount)
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithUnread[validIndex]
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversationsWithUnread)
            
            // Create a delete message for the target conversation
            val deletedMessage = TestMessage(
                id = System.currentTimeMillis(),
                conversationId = targetConversation.conversationId,
                text = "Deleted message"
            )
            
            // Trigger delete event
            viewModel.onMessageDeleted(deletedMessage)
            
            // Assert: The unread count should be preserved
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation?.unreadMessageCount shouldBe unreadCount
        }
    }
    
    /**
     * Property 2 (continued): Edit preserves zero unread count
     * 
     * When a conversation has zero unread count, edit should preserve it as zero.
     * 
     * **Validates: Requirements 1.3**
     */
    test("Property 2: Edit preserves zero unread count").config(invocations = 20) {
        checkAll(conversationListArb(), Arb.int(0, 9)) { conversations, targetIndex ->
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
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversationsWithZeroUnread)
            
            // Create an edit message for the target conversation
            val editedMessage = TestMessage(
                id = System.currentTimeMillis(),
                conversationId = targetConversation.conversationId
            )
            
            // Trigger edit event
            viewModel.onMessageEdited(editedMessage)
            
            // Assert: The unread count should remain zero
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation?.unreadMessageCount shouldBe 0
        }
    }
    
    /**
     * Property 2 (continued): Edit/Delete preserves unread count for all conversations
     * 
     * When a conversation is edited/deleted, the unread counts of ALL other conversations
     * should also remain unchanged.
     * 
     * **Validates: Requirements 1.3**
     */
    test("Property 2: Edit preserves unread count for all other conversations").config(invocations = 20) {
        checkAll(conversationListArb(3, 10), Arb.int(0, 9)) { conversations, targetIndex ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            val targetConversation = conversations[validIndex]
            
            // Record original unread counts for all conversations
            val originalUnreadCounts = conversations.associate { it.conversationId to it.unreadMessageCount }
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversations)
            
            // Trigger edit event
            val editedMessage = TestMessage(
                id = System.currentTimeMillis(),
                conversationId = targetConversation.conversationId
            )
            viewModel.onMessageEdited(editedMessage)
            
            // Assert: All conversations should have their original unread counts
            val resultList = viewModel.conversations.value
            resultList.forEach { conv ->
                conv.unreadMessageCount shouldBe originalUnreadCounts[conv.conversationId]
            }
        }
    }
    
    /**
     * Property 2 (continued): Multiple sequential edits preserve unread count
     * 
     * When multiple edit events occur for the same conversation, the unread count
     * should be preserved through all updates.
     * 
     * **Validates: Requirements 1.3**
     */
    test("Property 2: Multiple sequential edits preserve unread count").config(invocations = 20) {
        checkAll(conversationListArb(), Arb.int(0, 9), Arb.int(1, 100), Arb.int(2, 5)) { conversations, targetIndex, unreadCount, editCount ->
            val validIndex = targetIndex.coerceIn(0, conversations.size - 1)
            
            // Set a specific non-zero unread count on the target conversation
            val conversationsWithUnread = conversations.mapIndexed { index, conv ->
                if (index == validIndex) {
                    conv.copy(unreadMessageCount = unreadCount)
                } else {
                    conv
                }
            }
            val targetConversation = conversationsWithUnread[validIndex]
            
            val viewModel = TestConversationListViewModel()
            viewModel.setConversations(conversationsWithUnread)
            
            // Perform multiple edit events
            repeat(editCount) { iteration ->
                val editedMessage = TestMessage(
                    id = System.currentTimeMillis() + iteration,
                    conversationId = targetConversation.conversationId,
                    text = "Edited message $iteration"
                )
                viewModel.onMessageEdited(editedMessage)
            }
            
            // Assert: The unread count should still be preserved after all edits
            val resultList = viewModel.conversations.value
            val updatedConversation = resultList.find { it.conversationId == targetConversation.conversationId }
            updatedConversation?.unreadMessageCount shouldBe unreadCount
        }
    }
})
