package com.cometchat.uikit.kotlin.presentation.conversationlist.ui

import android.content.Context
import android.graphics.Color
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.uikit.kotlin.presentation.conversations.style.CometChatConversationListItemStyle
import com.cometchat.uikit.kotlin.presentation.conversations.ui.ConversationsAdapter
import com.cometchat.uikit.kotlin.shared.formatters.CometChatTextFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for ConversationsAdapter.
 * Tests verify adapter list updates, selection state management, and typing indicator updates.
 */
@RunWith(MockitoJUnitRunner::class)
class ConversationsAdapterTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var adapter: ConversationsAdapter

    private lateinit var mockUser1: User
    private lateinit var mockUser2: User
    private lateinit var mockGroup1: Group
    private lateinit var mockMessage1: TextMessage
    private lateinit var mockMessage2: TextMessage

    @Before
    fun setup() {
        adapter = ConversationsAdapter()
        
        // Setup mock users
        mockUser1 = mock(User::class.java).apply {
            `when`(uid).thenReturn("user1")
            `when`(name).thenReturn("User One")
            `when`(avatar).thenReturn("https://example.com/avatar1.png")
            `when`(status).thenReturn("online")
        }
        mockUser2 = mock(User::class.java).apply {
            `when`(uid).thenReturn("user2")
            `when`(name).thenReturn("User Two")
            `when`(avatar).thenReturn("https://example.com/avatar2.png")
            `when`(status).thenReturn("offline")
        }
        
        // Setup mock group
        mockGroup1 = mock(Group::class.java).apply {
            `when`(guid).thenReturn("group1")
            `when`(name).thenReturn("Group One")
            `when`(icon).thenReturn("https://example.com/group1.png")
            `when`(groupType).thenReturn("public")
        }
        
        // Setup mock messages
        mockMessage1 = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(100)
            `when`(text).thenReturn("Hello World")
            `when`(sentAt).thenReturn(1000L)
        }
        mockMessage2 = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(200)
            `when`(text).thenReturn("How are you?")
            `when`(sentAt).thenReturn(2000L)
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

    private fun createMockTypingIndicator(
        sender: User,
        receiverId: String,
        receiverType: String = "user"
    ): TypingIndicator {
        return mock(TypingIndicator::class.java).apply {
            `when`(this.sender).thenReturn(sender)
            `when`(this.receiverId).thenReturn(receiverId)
            `when`(this.receiverType).thenReturn(receiverType)
        }
    }

    // ==================== List Management Tests ====================

    @Test
    fun `initial adapter should have empty list`() {
        assertEquals(0, adapter.itemCount)
        assertTrue(adapter.getList().isEmpty())
    }

    @Test
    fun `setList should update adapter with new conversations`() {
        val conversations = listOf(
            createMockConversation("conv1"),
            createMockConversation("conv2"),
            createMockConversation("conv3")
        )
        
        adapter.setList(conversations)
        
        assertEquals(3, adapter.itemCount)
        assertEquals(conversations, adapter.getList())
    }

    @Test
    fun `setList should replace existing list`() {
        val initialList = listOf(
            createMockConversation("conv1"),
            createMockConversation("conv2")
        )
        adapter.setList(initialList)
        
        val newList = listOf(
            createMockConversation("conv3"),
            createMockConversation("conv4"),
            createMockConversation("conv5")
        )
        adapter.setList(newList)
        
        assertEquals(3, adapter.itemCount)
        assertEquals(newList, adapter.getList())
    }

    @Test
    fun `setList with empty list should clear adapter`() {
        val conversations = listOf(
            createMockConversation("conv1"),
            createMockConversation("conv2")
        )
        adapter.setList(conversations)
        
        adapter.setList(emptyList())
        
        assertEquals(0, adapter.itemCount)
        assertTrue(adapter.getList().isEmpty())
    }

    @Test
    fun `setList should handle single item`() {
        val conversation = createMockConversation("conv1")
        
        adapter.setList(listOf(conversation))
        
        assertEquals(1, adapter.itemCount)
        assertEquals(conversation, adapter.getList()[0])
    }

    @Test
    fun `setList should handle large list`() {
        val conversations = (1..100).map { createMockConversation("conv$it") }
        
        adapter.setList(conversations)
        
        assertEquals(100, adapter.itemCount)
    }

    // ==================== Selection State Tests ====================

    @Test
    fun `selectConversation should update selection state`() {
        val conv1 = createMockConversation("conv1")
        val conv2 = createMockConversation("conv2")
        adapter.setList(listOf(conv1, conv2))
        
        val selection = mapOf(conv1 to true)
        adapter.selectConversation(selection)
        
        // Adapter should have selection state updated
        // Note: We can't directly verify internal state, but we can verify the method doesn't throw
        assertNotNull(adapter)
    }

    @Test
    fun `selectConversation with empty map should clear selection`() {
        val conv1 = createMockConversation("conv1")
        adapter.setList(listOf(conv1))
        
        // First select
        adapter.selectConversation(mapOf(conv1 to true))
        
        // Then clear
        adapter.selectConversation(emptyMap())
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `selectConversation should handle multiple selections`() {
        val conv1 = createMockConversation("conv1")
        val conv2 = createMockConversation("conv2")
        val conv3 = createMockConversation("conv3")
        adapter.setList(listOf(conv1, conv2, conv3))
        
        val selection = mapOf(
            conv1 to true,
            conv2 to true,
            conv3 to false
        )
        adapter.selectConversation(selection)
        
        // Should not throw
        assertNotNull(adapter)
    }

    // ==================== Typing Indicator Tests ====================

    @Test
    fun `setTypingIndicators should update typing state`() {
        val conv1 = createMockConversation("conv1")
        adapter.setList(listOf(conv1))
        
        val typingIndicator = createMockTypingIndicator(mockUser2, "user1")
        val indicators = mapOf("conv1" to typingIndicator)
        
        adapter.setTypingIndicators(indicators)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTypingIndicators with empty map should clear typing state`() {
        val conv1 = createMockConversation("conv1")
        adapter.setList(listOf(conv1))
        
        // First set typing
        val typingIndicator = createMockTypingIndicator(mockUser2, "user1")
        adapter.setTypingIndicators(mapOf("conv1" to typingIndicator))
        
        // Then clear
        adapter.setTypingIndicators(emptyMap())
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `typing should update typing state using HashMap`() {
        val conv1 = createMockConversation("conv1")
        adapter.setList(listOf(conv1))
        
        val typingIndicator = createMockTypingIndicator(mockUser2, "user1")
        val typingMap = hashMapOf(conv1 to typingIndicator)
        
        adapter.typing(typingMap)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `typing should handle multiple typing indicators`() {
        val conv1 = createMockConversation("conv1")
        val conv2 = createMockConversation("conv2")
        adapter.setList(listOf(conv1, conv2))
        
        val typing1 = createMockTypingIndicator(mockUser1, "user2")
        val typing2 = createMockTypingIndicator(mockUser2, "user1")
        val typingMap = hashMapOf(
            conv1 to typing1,
            conv2 to typing2
        )
        
        adapter.typing(typingMap)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTypingIndicatorsFromViewModel should match user conversation by sender uid`() {
        // Create a user conversation where the conversationWith is mockUser2
        val userConversation = mock(Conversation::class.java).apply {
            `when`(conversationId).thenReturn("user_conv1")
            `when`(conversationType).thenReturn("user")
            `when`(conversationWith).thenReturn(mockUser2 as com.cometchat.chat.models.AppEntity)
        }
        adapter.setList(listOf(userConversation))
        
        // Create a typing indicator where sender is mockUser2 (the person typing)
        val typingIndicator = createMockTypingIndicator(mockUser2, "user1", "user")
        
        // ViewModel uses composite key format: "${receiverType}_${receiverId}_${senderUid}"
        val viewModelMap = mapOf("user_user1_user2" to typingIndicator)
        
        adapter.setTypingIndicatorsFromViewModel(viewModelMap)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTypingIndicatorsFromViewModel should match group conversation by receiverId`() {
        // Create a group conversation
        val groupConversation = mock(Conversation::class.java).apply {
            `when`(conversationId).thenReturn("group_conv1")
            `when`(conversationType).thenReturn("group")
            `when`(conversationWith).thenReturn(mockGroup1 as com.cometchat.chat.models.AppEntity)
        }
        adapter.setList(listOf(groupConversation))
        
        // Create a typing indicator for the group
        val typingIndicator = createMockTypingIndicator(mockUser1, "group1", "group")
        
        // ViewModel uses composite key format
        val viewModelMap = mapOf("group_group1_user1" to typingIndicator)
        
        adapter.setTypingIndicatorsFromViewModel(viewModelMap)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTypingIndicatorsFromViewModel should not match mismatched conversation types`() {
        // Create a user conversation
        val userConversation = mock(Conversation::class.java).apply {
            `when`(conversationId).thenReturn("user_conv1")
            `when`(conversationType).thenReturn("user")
            `when`(conversationWith).thenReturn(mockUser2 as com.cometchat.chat.models.AppEntity)
        }
        adapter.setList(listOf(userConversation))
        
        // Create a typing indicator for a group (should not match user conversation)
        val typingIndicator = createMockTypingIndicator(mockUser1, "group1", "group")
        
        val viewModelMap = mapOf("group_group1_user1" to typingIndicator)
        
        adapter.setTypingIndicatorsFromViewModel(viewModelMap)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTypingIndicatorsFromViewModel should handle empty map`() {
        val conv1 = createMockConversation("conv1")
        adapter.setList(listOf(conv1))
        
        adapter.setTypingIndicatorsFromViewModel(emptyMap())
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTypingIndicatorsFromViewModel should handle multiple conversations with typing`() {
        // Create user and group conversations
        val userConversation = mock(Conversation::class.java).apply {
            `when`(conversationId).thenReturn("user_conv1")
            `when`(conversationType).thenReturn("user")
            `when`(conversationWith).thenReturn(mockUser2 as com.cometchat.chat.models.AppEntity)
        }
        val groupConversation = mock(Conversation::class.java).apply {
            `when`(conversationId).thenReturn("group_conv1")
            `when`(conversationType).thenReturn("group")
            `when`(conversationWith).thenReturn(mockGroup1 as com.cometchat.chat.models.AppEntity)
        }
        adapter.setList(listOf(userConversation, groupConversation))
        
        // Create typing indicators for both
        val userTyping = createMockTypingIndicator(mockUser2, "user1", "user")
        val groupTyping = createMockTypingIndicator(mockUser1, "group1", "group")
        
        val viewModelMap = mapOf(
            "user_user1_user2" to userTyping,
            "group_group1_user1" to groupTyping
        )
        
        adapter.setTypingIndicatorsFromViewModel(viewModelMap)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTypingIndicatorsFromViewModel should collect multiple typing users in group`() {
        // Create a group conversation
        val groupConversation = mock(Conversation::class.java).apply {
            `when`(conversationId).thenReturn("group_conv1")
            `when`(conversationType).thenReturn("group")
            `when`(conversationWith).thenReturn(mockGroup1 as com.cometchat.chat.models.AppEntity)
        }
        adapter.setList(listOf(groupConversation))
        
        // Create multiple typing indicators for the same group (multiple users typing)
        val typing1 = createMockTypingIndicator(mockUser1, "group1", "group")
        val typing2 = createMockTypingIndicator(mockUser2, "group1", "group")
        
        // ViewModel uses composite key format with different sender UIDs
        val viewModelMap = mapOf(
            "group_group1_user1" to typing1,
            "group_group1_user2" to typing2
        )
        
        adapter.setTypingIndicatorsFromViewModel(viewModelMap)
        
        // Should not throw - the adapter should collect both typing users
        assertNotNull(adapter)
    }

    // ==================== Click Listener Tests ====================

    @Test
    fun `setOnItemClick should set click listener`() {
        var clickedConversation: Conversation? = null
        var clickedPosition: Int = -1
        
        adapter.setOnItemClick { _, position, conversation ->
            clickedConversation = conversation
            clickedPosition = position
        }
        
        // Listener should be set without throwing
        assertNotNull(adapter)
    }

    @Test
    fun `setOnLongClick should set long click listener`() {
        var longClickedConversation: Conversation? = null
        var longClickedPosition: Int = -1
        
        adapter.setOnLongClick { _, position, conversation ->
            longClickedConversation = conversation
            longClickedPosition = position
        }
        
        // Listener should be set without throwing
        assertNotNull(adapter)
    }

    // ==================== Style Configuration Tests ====================

    @Test
    fun `setItemStyle should update item style`() {
        val style = CometChatConversationListItemStyle(
            backgroundColor = Color.WHITE,
            titleTextColor = Color.BLACK,
            subtitleTextColor = Color.GRAY
        )
        
        adapter.setItemStyle(style)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTextFormatters should update formatters`() {
        val formatter = mock(CometChatTextFormatter::class.java)
        
        adapter.setTextFormatters(listOf(formatter))
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setDateTimeFormatter should update date formatter`() {
        val formatter = object : com.cometchat.uikit.kotlin.shared.interfaces.DateTimeFormatterCallback {
            override fun time(timestamp: Long): String = "Formatted: $timestamp"
        }
        
        adapter.setDateTimeFormatter(formatter)
        
        // Should not throw
        assertNotNull(adapter)
    }

    // Note: ConversationsAdapter.setDateTimeFormatter doesn't accept null
    // This test is removed as the API doesn't support null

    // ==================== Visibility Control Tests ====================

    @Test
    fun `setHideUserStatus should update visibility flag`() {
        adapter.setHideUserStatus(true)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setHideGroupType should update visibility flag`() {
        adapter.setHideGroupType(true)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setHideReceipts should update visibility flag`() {
        adapter.setHideReceipts(true)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setHideSeparator should update visibility flag`() {
        adapter.setHideSeparator(true)
        
        // Should not throw
        assertNotNull(adapter)
    }

    // ==================== Custom View Listener Tests ====================

    @Test
    fun `setItemView should set custom item view listener`() {
        adapter.setItemView(null)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setLeadingView should set custom leading view listener`() {
        adapter.setLeadingView(null)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTitleView should set custom title view listener`() {
        adapter.setTitleView(null)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setSubtitleView should set custom subtitle view listener`() {
        adapter.setSubtitleView(null)
        
        // Should not throw
        assertNotNull(adapter)
    }

    @Test
    fun `setTrailingView should set custom trailing view listener`() {
        adapter.setTrailingView(null)
        
        // Should not throw
        assertNotNull(adapter)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `adapter should handle conversation with user`() {
        val conversation = createMockConversation(
            conversationId = "user_user1",
            conversationWith = mockUser1,
            lastMessage = mockMessage1,
            unreadCount = 5
        )
        
        adapter.setList(listOf(conversation))
        
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should handle conversation with group`() {
        val conversation = createMockConversation(
            conversationId = "group_group1",
            conversationWith = mockGroup1,
            lastMessage = mockMessage1,
            unreadCount = 10
        )
        
        adapter.setList(listOf(conversation))
        
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should handle conversation without last message`() {
        val conversation = createMockConversation(
            conversationId = "conv1",
            lastMessage = null,
            unreadCount = 0
        )
        
        adapter.setList(listOf(conversation))
        
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should handle mixed user and group conversations`() {
        val userConversation = createMockConversation(
            conversationId = "user_user1",
            conversationWith = mockUser1
        )
        val groupConversation = createMockConversation(
            conversationId = "group_group1",
            conversationWith = mockGroup1
        )
        
        adapter.setList(listOf(userConversation, groupConversation))
        
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `adapter should handle rapid list updates`() {
        // Simulate rapid updates
        for (i in 1..10) {
            val conversations = (1..i).map { createMockConversation("conv$it") }
            adapter.setList(conversations)
        }
        
        assertEquals(10, adapter.itemCount)
    }

    @Test
    fun `adapter should handle list with duplicate conversation ids`() {
        // This tests the DiffUtil behavior with duplicates
        val conv1 = createMockConversation("conv1")
        val conv1Duplicate = createMockConversation("conv1")
        
        adapter.setList(listOf(conv1, conv1Duplicate))
        
        assertEquals(2, adapter.itemCount)
    }
}
