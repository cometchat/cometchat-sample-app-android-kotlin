package com.cometchat.uikit.kotlin.presentation.conversationlist.utils

import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.presentation.conversations.utils.ConversationsDiffCallback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Unit tests for ConversationsDiffCallback.
 * Tests verify DiffUtil calculations for efficient RecyclerView updates.
 */
class ConversationsDiffCallbackTest {

    private lateinit var mockUser1: User
    private lateinit var mockUser2: User
    private lateinit var mockGroup1: Group
    private lateinit var mockMessage1: TextMessage
    private lateinit var mockMessage2: TextMessage

    @Before
    fun setup() {
        // Setup mock users
        mockUser1 = mock(User::class.java).apply {
            `when`(uid).thenReturn("user1")
            `when`(name).thenReturn("User One")
        }
        mockUser2 = mock(User::class.java).apply {
            `when`(uid).thenReturn("user2")
            `when`(name).thenReturn("User Two")
        }
        
        // Setup mock group
        mockGroup1 = mock(Group::class.java).apply {
            `when`(guid).thenReturn("group1")
            `when`(name).thenReturn("Group One")
        }
        
        // Setup mock messages
        mockMessage1 = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(100)
            `when`(editedAt).thenReturn(0L)
            `when`(deletedAt).thenReturn(0L)
        }
        mockMessage2 = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(200)
            `when`(editedAt).thenReturn(0L)
            `when`(deletedAt).thenReturn(0L)
        }
    }

    private fun createMockConversation(
        conversationId: String,
        conversationWith: Any? = mockUser1,
        lastMessage: BaseMessage? = null,
        unreadCount: Int = 0
    ): Conversation {
        return mock(Conversation::class.java).apply {
            `when`(this.conversationId).thenReturn(conversationId)
            `when`(this.conversationWith).thenReturn(conversationWith as com.cometchat.chat.models.AppEntity)
            `when`(this.lastMessage).thenReturn(lastMessage)
            `when`(this.unreadMessageCount).thenReturn(unreadCount)
        }
    }

    // ==================== List Size Tests ====================

    @Test
    fun `getOldListSize should return correct size`() {
        val oldList = listOf(
            createMockConversation("conv1"),
            createMockConversation("conv2"),
            createMockConversation("conv3")
        )
        val newList = listOf(createMockConversation("conv1"))
        
        val callback = ConversationsDiffCallback(oldList, newList)
        
        assertEquals(3, callback.oldListSize)
    }

    @Test
    fun `getNewListSize should return correct size`() {
        val oldList = listOf(createMockConversation("conv1"))
        val newList = listOf(
            createMockConversation("conv1"),
            createMockConversation("conv2")
        )
        
        val callback = ConversationsDiffCallback(oldList, newList)
        
        assertEquals(2, callback.newListSize)
    }

    @Test
    fun `empty lists should return zero sizes`() {
        val callback = ConversationsDiffCallback(emptyList(), emptyList())
        
        assertEquals(0, callback.oldListSize)
        assertEquals(0, callback.newListSize)
    }

    // ==================== areItemsTheSame Tests ====================

    @Test
    fun `areItemsTheSame should return true for same conversationId`() {
        val oldList = listOf(createMockConversation("conv1"))
        val newList = listOf(createMockConversation("conv1"))
        
        val callback = ConversationsDiffCallback(oldList, newList)
        
        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `areItemsTheSame should return false for different conversationId`() {
        val oldList = listOf(createMockConversation("conv1"))
        val newList = listOf(createMockConversation("conv2"))
        
        val callback = ConversationsDiffCallback(oldList, newList)
        
        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `areItemsTheSame should handle multiple items correctly`() {
        val oldList = listOf(
            createMockConversation("conv1"),
            createMockConversation("conv2"),
            createMockConversation("conv3")
        )
        val newList = listOf(
            createMockConversation("conv2"),
            createMockConversation("conv1"),
            createMockConversation("conv4")
        )
        
        val callback = ConversationsDiffCallback(oldList, newList)
        
        // conv1 at old[0] vs conv2 at new[0] - different
        assertFalse(callback.areItemsTheSame(0, 0))
        // conv1 at old[0] vs conv1 at new[1] - same
        assertTrue(callback.areItemsTheSame(0, 1))
        // conv2 at old[1] vs conv2 at new[0] - same
        assertTrue(callback.areItemsTheSame(1, 0))
        // conv3 at old[2] vs conv4 at new[2] - different
        assertFalse(callback.areItemsTheSame(2, 2))
    }

    // ==================== areContentsTheSame Tests ====================

    @Test
    fun `areContentsTheSame should return true for identical conversations`() {
        val conversation1 = createMockConversation(
            conversationId = "conv1",
            lastMessage = mockMessage1,
            unreadCount = 5
        )
        val conversation2 = createMockConversation(
            conversationId = "conv1",
            lastMessage = mockMessage1,
            unreadCount = 5
        )
        
        val callback = ConversationsDiffCallback(listOf(conversation1), listOf(conversation2))
        
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false when lastMessage id differs`() {
        val conversation1 = createMockConversation(
            conversationId = "conv1",
            lastMessage = mockMessage1,
            unreadCount = 5
        )
        val conversation2 = createMockConversation(
            conversationId = "conv1",
            lastMessage = mockMessage2,
            unreadCount = 5
        )
        
        val callback = ConversationsDiffCallback(listOf(conversation1), listOf(conversation2))
        
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false when unreadCount differs`() {
        val conversation1 = createMockConversation(
            conversationId = "conv1",
            lastMessage = mockMessage1,
            unreadCount = 5
        )
        val conversation2 = createMockConversation(
            conversationId = "conv1",
            lastMessage = mockMessage1,
            unreadCount = 10
        )
        
        val callback = ConversationsDiffCallback(listOf(conversation1), listOf(conversation2))
        
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false when editedAt differs`() {
        val editedMessage = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(100)
            `when`(editedAt).thenReturn(1000L)
            `when`(deletedAt).thenReturn(0L)
        }
        
        val conversation1 = createMockConversation(
            conversationId = "conv1",
            lastMessage = mockMessage1,
            unreadCount = 0
        )
        val conversation2 = createMockConversation(
            conversationId = "conv1",
            lastMessage = editedMessage,
            unreadCount = 0
        )
        
        val callback = ConversationsDiffCallback(listOf(conversation1), listOf(conversation2))
        
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false when deletedAt differs`() {
        val deletedMessage = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(100)
            `when`(editedAt).thenReturn(0L)
            `when`(deletedAt).thenReturn(2000L)
        }
        
        val conversation1 = createMockConversation(
            conversationId = "conv1",
            lastMessage = mockMessage1,
            unreadCount = 0
        )
        val conversation2 = createMockConversation(
            conversationId = "conv1",
            lastMessage = deletedMessage,
            unreadCount = 0
        )
        
        val callback = ConversationsDiffCallback(listOf(conversation1), listOf(conversation2))
        
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should handle null lastMessage correctly`() {
        val conversation1 = createMockConversation(
            conversationId = "conv1",
            lastMessage = null,
            unreadCount = 0
        )
        val conversation2 = createMockConversation(
            conversationId = "conv1",
            lastMessage = null,
            unreadCount = 0
        )
        
        val callback = ConversationsDiffCallback(listOf(conversation1), listOf(conversation2))
        
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `areContentsTheSame should return false when one has null lastMessage`() {
        val conversation1 = createMockConversation(
            conversationId = "conv1",
            lastMessage = mockMessage1,
            unreadCount = 0
        )
        val conversation2 = createMockConversation(
            conversationId = "conv1",
            lastMessage = null,
            unreadCount = 0
        )
        
        val callback = ConversationsDiffCallback(listOf(conversation1), listOf(conversation2))
        
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    // ==================== Edge Cases ====================

    @Test
    fun `should handle conversation with user correctly`() {
        val conversation = createMockConversation(
            conversationId = "user_user1",
            conversationWith = mockUser1,
            lastMessage = mockMessage1
        )
        
        val callback = ConversationsDiffCallback(listOf(conversation), listOf(conversation))
        
        assertTrue(callback.areItemsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `should handle conversation with group correctly`() {
        val conversation = createMockConversation(
            conversationId = "group_group1",
            conversationWith = mockGroup1,
            lastMessage = mockMessage1
        )
        
        val callback = ConversationsDiffCallback(listOf(conversation), listOf(conversation))
        
        assertTrue(callback.areItemsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `should handle reordered list correctly`() {
        val conv1 = createMockConversation("conv1", lastMessage = mockMessage1)
        val conv2 = createMockConversation("conv2", lastMessage = mockMessage2)
        val conv3 = createMockConversation("conv3")
        
        val oldList = listOf(conv1, conv2, conv3)
        val newList = listOf(conv3, conv1, conv2)
        
        val callback = ConversationsDiffCallback(oldList, newList)
        
        // Verify items are identified correctly despite reordering
        assertEquals(3, callback.oldListSize)
        assertEquals(3, callback.newListSize)
        
        // conv1 at old[0] should match conv1 at new[1]
        assertTrue(callback.areItemsTheSame(0, 1))
        // conv2 at old[1] should match conv2 at new[2]
        assertTrue(callback.areItemsTheSame(1, 2))
        // conv3 at old[2] should match conv3 at new[0]
        assertTrue(callback.areItemsTheSame(2, 0))
    }

    @Test
    fun `should handle added items correctly`() {
        val conv1 = createMockConversation("conv1")
        val conv2 = createMockConversation("conv2")
        
        val oldList = listOf(conv1)
        val newList = listOf(conv1, conv2)
        
        val callback = ConversationsDiffCallback(oldList, newList)
        
        assertEquals(1, callback.oldListSize)
        assertEquals(2, callback.newListSize)
        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `should handle removed items correctly`() {
        val conv1 = createMockConversation("conv1")
        val conv2 = createMockConversation("conv2")
        
        val oldList = listOf(conv1, conv2)
        val newList = listOf(conv1)
        
        val callback = ConversationsDiffCallback(oldList, newList)
        
        assertEquals(2, callback.oldListSize)
        assertEquals(1, callback.newListSize)
        assertTrue(callback.areItemsTheSame(0, 0))
    }
}
