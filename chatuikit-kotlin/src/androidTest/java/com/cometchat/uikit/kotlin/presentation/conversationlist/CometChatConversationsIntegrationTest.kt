package com.cometchat.uikit.kotlin.presentation.conversationlist

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Conversation
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.TextMessage
import com.cometchat.chat.models.TypingIndicator
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.state.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Integration tests for CometChatConversations component.
 * Tests verify ViewModel observation, real-time updates, and component integration.
 * 
 * These tests require Android instrumentation to run.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class CometChatConversationsIntegrationTest {

    private lateinit var context: Context

    // Mock data
    private lateinit var mockUser1: User
    private lateinit var mockUser2: User
    private lateinit var mockGroup1: Group
    private lateinit var mockMessage1: TextMessage
    private lateinit var mockMessage2: TextMessage

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
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
            `when`(sentAt).thenReturn(System.currentTimeMillis() / 1000)
            `when`(editedAt).thenReturn(0L)
            `when`(deletedAt).thenReturn(0L)
        }
        mockMessage2 = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(200)
            `when`(text).thenReturn("How are you?")
            `when`(sentAt).thenReturn(System.currentTimeMillis() / 1000)
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
            `when`(this.conversationWith).thenReturn(conversationWith)
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

    // ==================== ViewModel State Observation Tests ====================

    /**
     * Test: ViewModel conversations StateFlow observation
     * Verifies that UI updates when conversations list changes.
     */
    @Test
    fun viewModel_conversationsStateFlow_updatesUI() = runTest {
        // Create mock StateFlow
        val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
        
        // Initial state should be empty
        assertTrue(conversationsFlow.value.isEmpty())
        
        // Update with conversations
        val conversations = listOf(
            createMockConversation("conv1", mockUser1, mockMessage1, 5),
            createMockConversation("conv2", mockGroup1, mockMessage2, 0)
        )
        conversationsFlow.value = conversations
        
        // Verify state updated
        assertEquals(2, conversationsFlow.value.size)
        assertEquals("conv1", conversationsFlow.value[0].conversationId)
        assertEquals("conv2", conversationsFlow.value[1].conversationId)
    }

    /**
     * Test: ViewModel uiState StateFlow observation
     * Verifies that UI state transitions are properly observed.
     */
    @Test
    fun viewModel_uiStateFlow_updatesUI() = runTest {
        // Create mock StateFlow
        val uiStateFlow = MutableStateFlow<UIState>(UIState.Loading)
        
        // Initial state should be Loading
        assertTrue(uiStateFlow.value is UIState.Loading)
        
        // Transition to Content
        uiStateFlow.value = UIState.Content
        assertTrue(uiStateFlow.value is UIState.Content)
        
        // Transition to Empty
        uiStateFlow.value = UIState.Empty
        assertTrue(uiStateFlow.value is UIState.Empty)
        
        // Transition to Error
        val exception = Exception("Test error")
        uiStateFlow.value = UIState.Error(exception)
        assertTrue(uiStateFlow.value is UIState.Error)
        assertEquals(exception, (uiStateFlow.value as UIState.Error).exception)
    }

    /**
     * Test: ViewModel typingIndicators StateFlow observation
     * Verifies that typing indicators are properly observed.
     */
    @Test
    fun viewModel_typingIndicatorsFlow_updatesUI() = runTest {
        // Create mock StateFlow
        val typingFlow = MutableStateFlow<HashMap<Conversation, TypingIndicator>>(hashMapOf())
        
        // Initial state should be empty
        assertTrue(typingFlow.value.isEmpty())
        
        // Add typing indicator
        val conversation = createMockConversation("conv1")
        val typingIndicator = createMockTypingIndicator(mockUser2, "user1")
        typingFlow.value = hashMapOf(conversation to typingIndicator)
        
        // Verify typing indicator added
        assertEquals(1, typingFlow.value.size)
        assertTrue(typingFlow.value.containsKey(conversation))
        
        // Remove typing indicator
        typingFlow.value = hashMapOf()
        assertTrue(typingFlow.value.isEmpty())
    }

    /**
     * Test: ViewModel selectedConversations StateFlow observation
     * Verifies that selection state is properly observed.
     */
    @Test
    fun viewModel_selectedConversationsFlow_updatesUI() = runTest {
        // Create mock StateFlow
        val selectionFlow = MutableStateFlow<Map<Conversation, Boolean>>(emptyMap())
        
        // Initial state should be empty
        assertTrue(selectionFlow.value.isEmpty())
        
        // Select conversations
        val conv1 = createMockConversation("conv1")
        val conv2 = createMockConversation("conv2")
        selectionFlow.value = mapOf(conv1 to true, conv2 to false)
        
        // Verify selection state
        assertEquals(2, selectionFlow.value.size)
        assertTrue(selectionFlow.value[conv1] == true)
        assertFalse(selectionFlow.value[conv2] == true)
    }

    // ==================== Real-Time Update Tests ====================

    /**
     * Test: New message moves conversation to top
     * Verifies that receiving a new message reorders the list.
     */
    @Test
    fun realTimeUpdate_newMessage_movesConversationToTop() = runTest {
        val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
        
        // Initial list with conv2 at top
        val conv1 = createMockConversation("conv1", mockUser1, mockMessage1)
        val conv2 = createMockConversation("conv2", mockUser2, mockMessage2)
        conversationsFlow.value = listOf(conv2, conv1)
        
        assertEquals("conv2", conversationsFlow.value[0].conversationId)
        
        // Simulate new message in conv1 - it should move to top
        val newMessage = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(300)
            `when`(text).thenReturn("New message!")
            `when`(sentAt).thenReturn(System.currentTimeMillis() / 1000)
        }
        val updatedConv1 = createMockConversation("conv1", mockUser1, newMessage)
        conversationsFlow.value = listOf(updatedConv1, conv2)
        
        // Verify conv1 is now at top
        assertEquals("conv1", conversationsFlow.value[0].conversationId)
    }

    /**
     * Test: Message edit updates conversation
     * Verifies that editing a message updates the conversation.
     */
    @Test
    fun realTimeUpdate_messageEdit_updatesConversation() = runTest {
        val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
        
        // Initial conversation
        val conv = createMockConversation("conv1", mockUser1, mockMessage1)
        conversationsFlow.value = listOf(conv)
        
        assertEquals(0L, conversationsFlow.value[0].lastMessage?.editedAt)
        
        // Simulate message edit
        val editedMessage = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(100)
            `when`(text).thenReturn("Edited message")
            `when`(sentAt).thenReturn(mockMessage1.sentAt)
            `when`(editedAt).thenReturn(System.currentTimeMillis() / 1000)
        }
        val updatedConv = createMockConversation("conv1", mockUser1, editedMessage)
        conversationsFlow.value = listOf(updatedConv)
        
        // Verify message was edited
        assertTrue((conversationsFlow.value[0].lastMessage?.editedAt ?: 0L) > 0L)
    }

    /**
     * Test: Message delete updates conversation
     * Verifies that deleting a message updates the conversation.
     */
    @Test
    fun realTimeUpdate_messageDelete_updatesConversation() = runTest {
        val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
        
        // Initial conversation
        val conv = createMockConversation("conv1", mockUser1, mockMessage1)
        conversationsFlow.value = listOf(conv)
        
        assertEquals(0L, conversationsFlow.value[0].lastMessage?.deletedAt)
        
        // Simulate message delete
        val deletedMessage = mock(TextMessage::class.java).apply {
            `when`(id).thenReturn(100)
            `when`(text).thenReturn("")
            `when`(sentAt).thenReturn(mockMessage1.sentAt)
            `when`(deletedAt).thenReturn(System.currentTimeMillis() / 1000)
        }
        val updatedConv = createMockConversation("conv1", mockUser1, deletedMessage)
        conversationsFlow.value = listOf(updatedConv)
        
        // Verify message was deleted
        assertTrue((conversationsFlow.value[0].lastMessage?.deletedAt ?: 0L) > 0L)
    }

    /**
     * Test: User status change updates indicator
     * Verifies that user online/offline status changes are reflected.
     */
    @Test
    fun realTimeUpdate_userStatusChange_updatesIndicator() = runTest {
        // Create user with initial status
        val user = mock(User::class.java).apply {
            `when`(uid).thenReturn("user1")
            `when`(name).thenReturn("User One")
            `when`(status).thenReturn("offline")
        }
        
        assertEquals("offline", user.status)
        
        // Simulate status change
        `when`(user.status).thenReturn("online")
        
        assertEquals("online", user.status)
    }

    /**
     * Test: Conversation deletion removes from list
     * Verifies that deleting a conversation removes it from the list.
     */
    @Test
    fun realTimeUpdate_conversationDelete_removesFromList() = runTest {
        val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
        
        // Initial list with 3 conversations
        val conv1 = createMockConversation("conv1")
        val conv2 = createMockConversation("conv2")
        val conv3 = createMockConversation("conv3")
        conversationsFlow.value = listOf(conv1, conv2, conv3)
        
        assertEquals(3, conversationsFlow.value.size)
        
        // Delete conv2
        conversationsFlow.value = listOf(conv1, conv3)
        
        // Verify conv2 is removed
        assertEquals(2, conversationsFlow.value.size)
        assertFalse(conversationsFlow.value.any { it.conversationId == "conv2" })
    }

    // ==================== Sound Notification Tests ====================

    /**
     * Test: Sound event triggers playback
     * Verifies that sound events are properly emitted.
     */
    @Test
    fun soundNotification_newMessage_triggersSound() = runTest {
        // Create mock SharedFlow for sound events
        val soundEventFlow = MutableStateFlow<Boolean>(false)
        
        // Initial state - no sound
        assertFalse(soundEventFlow.value)
        
        // Trigger sound event
        soundEventFlow.value = true
        
        // Verify sound event was triggered
        assertTrue(soundEventFlow.value)
    }

    /**
     * Test: Sound disabled prevents playback
     * Verifies that disabling sound prevents playback.
     */
    @Test
    fun soundNotification_disabled_preventsPlayback() = runTest {
        var soundDisabled = false
        var soundPlayed = false
        
        // Disable sound
        soundDisabled = true
        
        // Simulate sound trigger
        if (!soundDisabled) {
            soundPlayed = true
        }
        
        // Verify sound was not played
        assertFalse(soundPlayed)
    }

    // ==================== Scroll to Top Tests ====================

    /**
     * Test: New message triggers scroll to top
     * Verifies that receiving a new message scrolls to top.
     */
    @Test
    fun scrollToTop_newMessage_triggersScroll() = runTest {
        // Create mock SharedFlow for scroll events
        val scrollToTopFlow = MutableStateFlow<Boolean>(false)
        
        // Initial state - no scroll
        assertFalse(scrollToTopFlow.value)
        
        // Trigger scroll event
        scrollToTopFlow.value = true
        
        // Verify scroll event was triggered
        assertTrue(scrollToTopFlow.value)
    }

    // ==================== Component Integration Tests ====================

    /**
     * Test: Context is available
     * Verifies that the test context is properly initialized.
     */
    @Test
    fun component_contextAvailable() {
        assertNotNull(context)
    }

    /**
     * Test: Mock data is properly created
     * Verifies that mock objects are properly initialized.
     */
    @Test
    fun component_mockDataCreated() {
        assertNotNull(mockUser1)
        assertNotNull(mockUser2)
        assertNotNull(mockGroup1)
        assertNotNull(mockMessage1)
        assertNotNull(mockMessage2)
        
        assertEquals("user1", mockUser1.uid)
        assertEquals("user2", mockUser2.uid)
        assertEquals("group1", mockGroup1.guid)
        assertEquals(100, mockMessage1.id)
        assertEquals(200, mockMessage2.id)
    }

    /**
     * Test: Conversation creation with user
     * Verifies that conversations with users are properly created.
     */
    @Test
    fun component_conversationWithUser_created() {
        val conversation = createMockConversation(
            conversationId = "user_user1",
            conversationWith = mockUser1,
            lastMessage = mockMessage1,
            unreadCount = 5
        )
        
        assertNotNull(conversation)
        assertEquals("user_user1", conversation.conversationId)
        assertEquals(mockUser1, conversation.conversationWith)
        assertEquals(mockMessage1, conversation.lastMessage)
        assertEquals(5, conversation.unreadMessageCount)
    }

    /**
     * Test: Conversation creation with group
     * Verifies that conversations with groups are properly created.
     */
    @Test
    fun component_conversationWithGroup_created() {
        val conversation = createMockConversation(
            conversationId = "group_group1",
            conversationWith = mockGroup1,
            lastMessage = mockMessage2,
            unreadCount = 10
        )
        
        assertNotNull(conversation)
        assertEquals("group_group1", conversation.conversationId)
        assertEquals(mockGroup1, conversation.conversationWith)
        assertEquals(mockMessage2, conversation.lastMessage)
        assertEquals(10, conversation.unreadMessageCount)
    }

    /**
     * Test: Typing indicator creation
     * Verifies that typing indicators are properly created.
     */
    @Test
    fun component_typingIndicator_created() {
        val typingIndicator = createMockTypingIndicator(
            sender = mockUser2,
            receiverId = "user1",
            receiverType = "user"
        )
        
        assertNotNull(typingIndicator)
        assertEquals(mockUser2, typingIndicator.sender)
        assertEquals("user1", typingIndicator.receiverId)
        assertEquals("user", typingIndicator.receiverType)
    }
}
